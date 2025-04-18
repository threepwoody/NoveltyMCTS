package experiments.NTBO;

import ai.Evaluation;
import ai.Game;
import experiments.ConditionResult;
import experiments.Match;

import java.io.*;
import java.util.*;

import static experiments.ExperimentConfiguration.*;
import static experiments.NTBO.NTBOExperiment.numberOfRunsPerProblem;
import static utils.Util.findGameInPlayerString;
import static utils.Util.playerAssignmentsToColorsForNumbersOfColors;

public class NTBOMultistartFinalizer implements Runnable {

    //this many matches are played for the best result found by each multi-start NTBO experiment
    static final int MINIMUM_MATCHES_PER_CONDITION = 2000;
    static Map<String, List<List<Match>>> FILE_TO_REMAINING_MATCHES_PER_CONDITION = new HashMap<>();
    static Map<String, List<ConditionResult>> FILE_TO_RESULTS_PER_CONDITION = new HashMap<>();
    static Set<String> FINISHED_FILES = new HashSet<>();
    static String conditionInputFilePrefix = "NTBO_summary_";
    static int fileNameCounter = 1;
    static PrintWriter out;
    static String outputFilePrefix = "final_results";
    //experiments are read from any files that start with this prefix (outputs of NTBOResultTester)
    static String resultInputFilePrefix = "evaluation_of_NTBO_summary_";

    static {
        Map<Integer, Double> bestPerformancePerLine = new HashMap<>();
        Map<Integer, Integer> bestNTBOIDPerLine = new HashMap<>();
        File directoryPath = new File(System.getProperty("user.dir")+File.separator);
        FilenameFilter fileNameFilter = (dir, name) -> name.startsWith(resultInputFilePrefix);
        File[] filesWithMultiStartResults = directoryPath.listFiles(fileNameFilter);
        for(File file : filesWithMultiStartResults) {
            //go through all files that match resultInputFilePrefix. average their lines. find the highest average for each batch of size numberOfRunsPerProblem, corresponding to a single line in ExperimentConfiguration. the corresponding file starting with conditionInputFilePrefix ends with the condition to be tested again, and contains the correct opponents as well.
            String fileName = file.getName();
            int ntboID = Integer.parseInt(fileName.substring(27,fileName.length()-4));
            int playerOneTemplateLine = ntboID/numberOfRunsPerProblem;
            try {
                BufferedReader br = new BufferedReader(new FileReader(fileName));
                String lastLine = "";
                String sCurrentLine;
                double result = 0;
                int counter = 0;
                while ((sCurrentLine = br.readLine()) != null) {
                    lastLine = sCurrentLine;
                    result += Double.parseDouble(lastLine);
                    counter++;
                }
                result /= counter;
                if(!bestPerformancePerLine.containsKey(playerOneTemplateLine) || result > bestPerformancePerLine.get(playerOneTemplateLine)) {
                    bestPerformancePerLine.put(playerOneTemplateLine, result);
                    bestNTBOIDPerLine.put(playerOneTemplateLine, ntboID);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for(int ntboID : bestNTBOIDPerLine.values()) {
            String fileName = conditionInputFilePrefix + ntboID + ".txt";
            FILE_TO_REMAINING_MATCHES_PER_CONDITION.put(fileName, new ArrayList<>());
            FILE_TO_RESULTS_PER_CONDITION.put(fileName,  new ArrayList<>());
            List<String> PLAYER_ONES = new ArrayList<>();
            List<String> PLAYER_TWOS = null;
            Scanner s = null;
            boolean readingSolutions = false;
            try {
                s = new Scanner(new File(fileName));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            while (s.hasNextLine()) {
                String fileLine = s.nextLine();
                if(readingSolutions) {
                    PLAYER_ONES.add(fileLine);
                } else if(fileLine.contains("opponents=")) {
                    int opponentsIndex = fileLine.indexOf("opponents=")+10;
                    PLAYER_TWOS = Arrays.asList(fileLine.substring(opponentsIndex).split(", "));
                } else if(fileLine.equals("Solutions:")) {
                    readingSolutions = true;
                }
            }
            PLAYER_ONES.subList(0, PLAYER_ONES.size()-1).clear();
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
                            String matchFileName = RESULTS_DIRECTORY + "NTBOMultistartFinalizer" + fileNameCounter + ".game";
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
            experimentThreads[i] = new Thread(new NTBOMultistartFinalizer());
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
        SortedMap<Integer, String> finalResults = new TreeMap<>();
        for(Map.Entry<String, List<List<Match>>> fileAndRemainingMatches : FILE_TO_REMAINING_MATCHES_PER_CONDITION.entrySet()) {
            String fileName = fileAndRemainingMatches.getKey();
            int experimentConfigurationLine = Integer.parseInt(fileName.substring(conditionInputFilePrefix.length(), fileName.length()-4))/numberOfRunsPerProblem;
            finalResults.put(experimentConfigurationLine, summarizeResultsOfFile(fileName));
        }
        File file = new File(System.getProperty("user.dir") + File.separator + outputFilePrefix + ".txt");
        file.getParentFile().mkdirs();
        try {
            out = new PrintWriter(file);
            Collection<String> results = finalResults.values();
            for(String result : results) {
                out.print(result);
            }
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static synchronized String summarizeResultsOfFile(String fileName) {
        String result = "";
        if(!FINISHED_FILES.contains(fileName)) {
            FINISHED_FILES.add(fileName);
            List<ConditionResult> fileResults = FILE_TO_RESULTS_PER_CONDITION.get(fileName);
            for (ConditionResult conditionResult : fileResults) {
                result += conditionResult.toString(false, false, false);
            }
        }
        return result;
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
