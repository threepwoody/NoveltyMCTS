package experiments.NTBO;

import ai.Evaluation;
import ai.Game;
import experiments.ConditionResult;
import experiments.Match;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.util.*;

import static experiments.ExperimentConfiguration.*;
import static utils.Util.findGameInPlayerString;
import static utils.Util.playerAssignmentsToColorsForNumbersOfColors;


public class NTBOResultTester implements Runnable {

    //this many matches are played for every result logged by NTBO optimization runs
    static final int MINIMUM_MATCHES_PER_CONDITION = 1000;
    //can either test every intermediate result that was logged (to track how the optimization worked over time), or only the final outcomes
    static final boolean onlyTestingFinalConditions = true;
    static Map<String, List<List<Match>>> FILE_TO_REMAINING_MATCHES_PER_CONDITION = new HashMap<>();
    static Map<String, List<ConditionResult>> FILE_TO_RESULTS_PER_CONDITION = new HashMap<>();
    static Set<String> FINISHED_FILES = new HashSet<>();
    static int fileNameCounter = 1;
    //experiments are read from any files that start with this prefix (outputs of NTBOExperiment)
    static String inputPrefix = "NTBO_summary_";
    static PrintWriter out;
    static String outputPrefix = "evaluation_of_";

    static {
        File directoryPath = new File(System.getProperty("user.dir")+File.separator);
        FilenameFilter fileNameFilter = (dir, name) -> name.startsWith(inputPrefix);
        File[] filesWithExperiments = directoryPath.listFiles(fileNameFilter);

        for(File file : filesWithExperiments) {
            String fileName = file.getName();
            FILE_TO_REMAINING_MATCHES_PER_CONDITION.put(fileName, new ArrayList<>());
            FILE_TO_RESULTS_PER_CONDITION.put(fileName,  new ArrayList<>());
            List<String> PLAYER_ONES = new ArrayList<>();
            List<String> PLAYER_TWOS = null;
            Scanner s = null;
            boolean readingSolutions = false;
            try {
                s = new Scanner(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            while (s.hasNextLine()) {
                String line = s.nextLine();
                if(readingSolutions) {
                    PLAYER_ONES.add(line);
                } else if(line.contains("opponents=")) {
                    int opponentsIndex = line.indexOf("opponents=")+10;
                    PLAYER_TWOS = Arrays.asList(line.substring(opponentsIndex).split(", "));
                } else if(line.equals("Solutions:")) {
                    readingSolutions = true;
                }
            }
            if(onlyTestingFinalConditions) {
                PLAYER_ONES.subList(0, PLAYER_ONES.size()-1).clear();
            }
            for(String playerOne : PLAYER_ONES) {
                for(String playerTwo : PLAYER_TWOS) {
                    Game game = findGameInPlayerString(playerOne);
                    int numberOfColors = game.getNumberOfColors();
                    FILE_TO_RESULTS_PER_CONDITION.get(fileName).add(new ConditionResult(playerOne, playerTwo, numberOfColors));
                    List<Match> matchesForCondition = new ArrayList<>();
                    int batchSize = playerAssignmentsToColorsForNumbersOfColors.get(numberOfColors).length;
                    int numberOfBatchesForThisCondition = (int) (Math.ceil(MINIMUM_MATCHES_PER_CONDITION/((double)batchSize*PLAYER_TWOS.size())));
                    //actual matches for a condition can be more than MATCHES_PER_CONDITION, because we always need to play an equal number of games with every possible assignment of players to colors, i.e. we need to play a multiple of batchSize. partial batches would skew results.
                    for(int batch=0; batch<numberOfBatchesForThisCondition; batch++) {
                        long randomSeed = 0;
                        if(numberOfRandomOpeningTurnsPerColor>0) {
                            Random random = new Random();
                            randomSeed = random.nextLong();
                        }
                        for(int matchNumber=0; matchNumber<batchSize; matchNumber++) {
                            String matchFileName = RESULTS_DIRECTORY + "NTBOResultTester" + fileNameCounter + ".game";
                            fileNameCounter++;
                            int[] playerOfColor = playerAssignmentsToColorsForNumbersOfColors.get(numberOfColors)[matchNumber];
                            Match match = new Match(matchFileName,playerOne,playerTwo,playerOfColor);
                            if(numberOfRandomOpeningTurnsPerColor>0) {
                                match.setNumberOfRandomOpeningTurnsPerColor(numberOfRandomOpeningTurnsPerColor, randomSeed);
                            }
                            matchesForCondition.add(match);
                        }
                    }
                    FILE_TO_REMAINING_MATCHES_PER_CONDITION.get(fileName).add(matchesForCondition);
                }
            }
        }
    }

    public static void main(String[] args) {
        //run experiment
        Thread[] experimentThreads = new Thread[MATCHES_IN_PARALLEL];
        for (int i = 0; i < MATCHES_IN_PARALLEL; i++) {
            experimentThreads[i] = new Thread(new NTBOResultTester());
            experimentThreads[i].start();
        }
        try {
            for (int i = 0; i < MATCHES_IN_PARALLEL; i++) {
                experimentThreads[i].join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        printResultsOfExperiment();
    }

    private static synchronized Match nextMatch(String fileName, int condition) {
        if(FILE_TO_REMAINING_MATCHES_PER_CONDITION.get(fileName).get(condition).isEmpty()) {
            return null;
        } else {
            return FILE_TO_REMAINING_MATCHES_PER_CONDITION.get(fileName).get(condition).remove(0);
        }
    }

    public static void printResultsOfExperiment() {
        for(Map.Entry<String, List<List<Match>>> fileAndRemainingMatches : FILE_TO_REMAINING_MATCHES_PER_CONDITION.entrySet()) {
            String fileName = fileAndRemainingMatches.getKey();
            printResultsOfFile(fileName);
        }
    }

    private static synchronized void printResultsOfFile(String fileName) {
        if(!FINISHED_FILES.contains(fileName)) {
            FINISHED_FILES.add(fileName);
            List<ConditionResult> fileResults = FILE_TO_RESULTS_PER_CONDITION.get(fileName);
            File file = new File(System.getProperty("user.dir") + File.separator + outputPrefix + fileName);
            file.getParentFile().mkdirs();
            try {
                out = new PrintWriter(file);
                for (ConditionResult result : fileResults) {
                    out.println(result.toString(false, false, true));
                }
                out.flush();
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        for(Map.Entry<String, List<List<Match>>> fileAndRemainingMatches : FILE_TO_REMAINING_MATCHES_PER_CONDITION.entrySet()) {
            String fileName = fileAndRemainingMatches.getKey();
            List<List<Match>> remainingMatchesPerCondition = fileAndRemainingMatches.getValue();
            for (int condition = 0; condition < remainingMatchesPerCondition.size(); condition++) {
                Match match = nextMatch(fileName, condition);
                while (match != null) {
                    Evaluation matchResult;
                    matchResult = match.play();
                    ConditionResult result = FILE_TO_RESULTS_PER_CONDITION.get(fileName).get(condition);
                    result.addMatchResult(matchResult, match.getPlayerNumberForColor());
                    match = nextMatch(fileName, condition);
                }
            }
        }
    }

}

