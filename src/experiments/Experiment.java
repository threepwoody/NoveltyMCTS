package experiments;

import ai.Evaluation;
import ai.Game;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

import static experiments.ExperimentConfiguration.*;
import static experiments.LogFrequency.NONE;
import static utils.Util.*;

public class Experiment implements Runnable {

    static List<ConditionResult> CONDITION_RESULTS;
    static List<List<Match>> REMAINING_MATCHES_PER_CONDITION;
    static Map<String, PlayerResult> RESULTS_PER_PLAYER_ONE;
    static int currentBatch = 1;
    static int fileNameCounter = 1;
    static int finishedMatches = 0;
    static LogFrequency logFrequency = NONE; //normally NONE
    static boolean logMatchResultsWithTransitions = true;
    static boolean logPolicyTargetsWithTransitions = false;
    static boolean logValueEstimatesWithTransitions = true;
    static boolean logEvalsWithTransitions = false; //normally false
    static int progressUpdateBatchSize = 100;
    static boolean usingSingleOutputFileLog = true;
    static boolean matchesPerConditionApplyToEverySingleOpponent = false; //If false, MATCHES_PER_CONDITION get distributed over all opponents, i.e. with MATCHES_PER_CONDITION=1000 and 2 opponents each would play 500 matches. If true, they both play 1000 matches.

    static {
        List<List<String>> PLAYER_ONES_PER_LINE = new ArrayList<>();
        for(int line = 0; line< PLAYER_ONE_TEMPLATES.length; line++) {
            List<String> playerOnes = expandTemplateIntoPlayers(new ArrayList<>(),"",Arrays.asList(PLAYER_ONE_TEMPLATES[line].split(" ")));
            Collections.sort(playerOnes);
            PLAYER_ONES_PER_LINE.add(playerOnes);
        }

        List<List<String>> PLAYER_TWOS_PER_LINE = new ArrayList<>();
        for(int line = 0; line < PLAYER_TWO_TEMPLATES.length; line++) {
            for(int opp = 0; opp < PLAYER_TWO_TEMPLATES[line].length; opp++) {
                List<String> playerTwos = expandTemplateIntoPlayers(new ArrayList<>(), "", Arrays.asList(PLAYER_TWO_TEMPLATES[line][opp].split(" ")));
                Collections.sort(playerTwos);
                if(PLAYER_TWOS_PER_LINE.size()==line+1) {
                    playerTwos.addAll(PLAYER_TWOS_PER_LINE.get(line));
                    Collections.sort(playerTwos);
                    PLAYER_TWOS_PER_LINE.set(line, playerTwos);
                } else {
                    PLAYER_TWOS_PER_LINE.add(playerTwos);
                }
            }
        }

        CONDITION_RESULTS = new ArrayList<>();
        REMAINING_MATCHES_PER_CONDITION = new ArrayList<>();
        RESULTS_PER_PLAYER_ONE = new HashMap<>();
        for(int line = 0; line < PLAYER_ONE_TEMPLATES.length; line++) {
            for(String playerOne : PLAYER_ONES_PER_LINE.get(line)) {
                Game game = findGameInPlayerString(playerOne);
                int numberOfColors = game.getNumberOfColors();
                int batchSize = playerAssignmentsToColorsForNumbersOfColors.get(numberOfColors).length;
                int numberOfBatchesPerCondition = 0;
                if(matchesPerConditionApplyToEverySingleOpponent) {
                    numberOfBatchesPerCondition = (int) (Math.ceil(MATCHES_PER_CONDITION / ((double) batchSize)));
                } else {
                    numberOfBatchesPerCondition = (int) (Math.ceil(MATCHES_PER_CONDITION/((double)batchSize*PLAYER_TWOS_PER_LINE.get(line).size())));
                }
                PlayerResult playerOneResult = new PlayerResult(playerOne, numberOfColors);
                RESULTS_PER_PLAYER_ONE.put(playerOne, playerOneResult);
                for(String playerTwo : PLAYER_TWOS_PER_LINE.get(line)) {
                    playerOneResult.addOpponent(playerTwo);
                    List<Match> remainingMatchesForThisCondition = new ArrayList<>();
                    REMAINING_MATCHES_PER_CONDITION.add(remainingMatchesForThisCondition);
                    CONDITION_RESULTS.add(new ConditionResult(playerOne, playerTwo, numberOfColors));
                    //actual matches for a condition can be more than MATCHES_PER_CONDITION, because we always need to play an equal number of games with every possible assignment of players to colors, i.e. we need to play a multiple of batchSize. partial batches would skew results.
                    for(int batch=0; batch<numberOfBatchesPerCondition; batch++) {
                        long randomSeed = 0;
                        if(numberOfRandomOpeningTurnsPerColor>0) {
                            Random random = new Random();
                            randomSeed = random.nextLong();
                        }
                        for(int matchNumber=0; matchNumber<batchSize; matchNumber++) {
                            String fileName = RESULTS_DIRECTORY + fileNameCounter + ".game";
                            fileNameCounter++;
                            int[] playerOfColor = playerAssignmentsToColorsForNumbersOfColors.get(numberOfColors)[matchNumber];
                            Match match = new Match(fileName,playerOne,playerTwo,playerOfColor);
                            TransitionLogConfiguration transitionLogConfiguration = new TransitionLogConfiguration();
                            transitionLogConfiguration.setLoggingMatchResultsWithTransitions(logMatchResultsWithTransitions);
                            transitionLogConfiguration.setLoggingValueEstimatesWithTransitions(logValueEstimatesWithTransitions);
                            transitionLogConfiguration.setLoggingEvalsWithTransitions(logEvalsWithTransitions);
                            transitionLogConfiguration.setLoggingPolicyTargetsWithTransitions(logPolicyTargetsWithTransitions);
                            transitionLogConfiguration.setUsingSingleOutputFile(usingSingleOutputFileLog);
                            transitionLogConfiguration.setLogFrequency(logFrequency);
                            transitionLogConfiguration.addPlayerToLog(playerOne);
                            transitionLogConfiguration.addPlayerToLog(playerTwo);
                            TransitionLogger transitionLogger = new TransitionLogger(match, transitionLogConfiguration);
                            match.setTransitionLogger(transitionLogger);
                            if(numberOfRandomOpeningTurnsPerColor>0) {
                                match.setNumberOfRandomOpeningTurnsPerColor(numberOfRandomOpeningTurnsPerColor, randomSeed);
                            }
                            remainingMatchesForThisCondition.add(match);
                        }
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        //run experiment
        Thread[] experimentThreads = new Thread[MATCHES_IN_PARALLEL];
        for (int i = 0; i < MATCHES_IN_PARALLEL; i++) {
            experimentThreads[i] = new Thread(new Experiment());
            experimentThreads[i].start();
        }
        try {
            for (int i = 0; i < MATCHES_IN_PARALLEL; i++) {
                experimentThreads[i].join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //print result summary
        printProgress();
    }

    private static synchronized Match nextMatch(int condition) {
        if(REMAINING_MATCHES_PER_CONDITION.get(condition).isEmpty()) {
            return null;
        } else {
            return REMAINING_MATCHES_PER_CONDITION.get(condition).remove(0);
        }
    }

    private static synchronized void printProgress() {
        File file = new File(System.getProperty("user.dir") + File.separator + "results_summary.txt");
        file.getParentFile().mkdirs();
        PrintWriter out;
        try {
            out = new PrintWriter(file);
            for(ConditionResult condition : CONDITION_RESULTS) {
                if(condition.getTotalMatchesPlayed()>0) {
                    out.print(condition.toString(false, false, false));
                }
            }
            List<PlayerResult> playerResults = new ArrayList(RESULTS_PER_PLAYER_ONE.values());
            Collections.sort(playerResults, Comparator.comparing(PlayerResult::getPlayer));
            for(PlayerResult playerResult : playerResults) {
                if(playerResult.numberOfOpponents()>1 && playerResult.getTotalMatchesPlayed()>0) {
                    out.print(playerResult.toString(false, false));
                }
            }
            out.flush();
            out.close();
//            ResultsCollector.processLogDirectory(RESULTS_DIRECTORY);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static synchronized void progressUpdate() {
        if(finishedMatches>= progressUpdateBatchSize *currentBatch) {
            printProgress();
            currentBatch++;
        }
    }

    private static synchronized void updateWithMatchResult(int condition, Match match, Evaluation matchResult) {
        ConditionResult conditionResult = CONDITION_RESULTS.get(condition);
        conditionResult.addMatchResult(matchResult, match.getPlayerNumberForColor());
        PlayerResult playerOneResult = RESULTS_PER_PLAYER_ONE.get(conditionResult.getPlayerOne());
        playerOneResult.addMatchResult(matchResult, match.getPlayerNumberForColor());
        finishedMatches++;
    }

    @Override
    public void run() {
        for(int condition=0; condition<REMAINING_MATCHES_PER_CONDITION.size(); condition++) {
            Match match = nextMatch(condition);
            while(match != null) {
                Evaluation matchResult;
                matchResult = match.play();
                updateWithMatchResult(condition, match, matchResult);
                progressUpdate();
                match = nextMatch(condition);
            }
        }
    }

}