package experiments.BanditTuning;

import experiments.FitnessEvaluator;
import experiments.UCB1TunedBandit;
import utils.LimitedSizeQueue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static experiments.ExperimentConfiguration.MATCHES_PER_CONDITION;
import static utils.Util.expandTemplateIntoPlayers;
import static utils.Util.findGameInPlayerString;

public class BanditTuner {

    private int ID;
    private LimitedSizeQueue<Double> averagingWindow;
    private int averagingWindowSize = 100; //in matches
    private UCB1TunedBandit<String> bandit;
    private int currentBatch;
    private int finishedMatches;
    //maps an agent (String) to its evaluation
    private FitnessEvaluator fitnessEvaluator;
    private int logBatchSize;
    private String logFileName;
    private boolean loggingMovingAverageWinrate = false;
    private PrintWriter out;

    public BanditTuner(FitnessEvaluator fitnessEvaluator, String playerOneTemplate, int ID, int logBatchSize, double banditExplorationFactor, String outputFileName) {
        this.fitnessEvaluator = fitnessEvaluator;
        this.ID = ID;
        this.logBatchSize = logBatchSize;
        finishedMatches = 0;
        currentBatch = 1;
        if(loggingMovingAverageWinrate) {
            averagingWindow = new LimitedSizeQueue<>(averagingWindowSize);
        }
        logFileName = outputFileName;
        List<String> possiblePlayers = expandTemplateIntoPlayers(new ArrayList<>(),"", Arrays.asList(playerOneTemplate.split(" ")));
        Collections.sort(possiblePlayers);
        int numberOfColorsInGame = findGameInPlayerString(playerOneTemplate).getNumberOfColors();
        double initialAverageResultForArms = 1.0/numberOfColorsInGame;
        double initialAverageSquaredResultForArms = Math.pow(initialAverageResultForArms, 2);
        int initialSamplesForArms = 1;
        bandit = new UCB1TunedBandit<>(playerOneTemplate, MATCHES_PER_CONDITION, possiblePlayers, initialAverageResultForArms, initialAverageSquaredResultForArms, initialSamplesForArms, banditExplorationFactor);
        bandit.setUsingVirtualLoss(true);
        bandit.setNumberOfVirtualLosses(1);
    }

    private static List<String> excludeIllegalPlayers(List<String> possiblePlayers) {
        List<String> legalPlayers = new ArrayList<>();
        for(String possiblePlayer : possiblePlayers) {
            if(isLegalPlayer(possiblePlayer)) {
                legalPlayers.add(possiblePlayer);
            }
        }
        return legalPlayers;
    }

    private static int intValueForAttribute(String player, String attribute) {
        String substring = player.substring(player.indexOf(attribute+"=")+attribute.length()+1);
        substring = substring.substring(0,substring.indexOf(" "));
        int value = Integer.parseInt(substring);
        return value;
    }

    private static boolean isLegalPlayer(String player) {
        if(intValueForAttribute(player, "colorsallowedtoselect")==0) {
            if(intValueForAttribute(player, "movesallowedforselectors")!=0) {
                return false;
            }
        } else if(intValueForAttribute(player, "colorsallowedtoselect")>0 && intValueForAttribute(player, "movesallowedforselectors")<=intValueForAttribute(player, "movesallowedfornonselectors")) {
            return false;
        }
        return true;
    }

    public double evaluateSolution(String solution) {
        synchronized(this) {
            finishedMatches += fitnessEvaluator.matchesPerEvaluation();
        }
        return fitnessEvaluator.evaluateFitness(solution);
    }

    public synchronized boolean isFinishedSampling() {
        boolean result = bandit.isFinishedSampling();
        if(result) {
            fitnessEvaluator.reset();
        }
        return result;
    }

    private synchronized void logProgress() {
        if(loggingMovingAverageWinrate) {
            logWindowedAverageWinrate();
        }
        if(finishedMatches>=logBatchSize*currentBatch) {
            prettyPrint();
            currentBatch++;
        }
    }

    private synchronized void logWindowedAverageWinrate() {
        if(averagingWindow.isFull()) {
            File file = new File(System.getProperty("user.dir")+File.separator+"moving_average_"+ID+".txt");
            file.getParentFile().mkdirs();
            try {
                out = new PrintWriter(new FileOutputStream(file, true));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            out.print(movingAverageWinrate() + "\r\n");
            out.flush();
            out.close();
        }
    }

    private synchronized double movingAverageWinrate() {
        return averagingWindow.stream()
                .mapToDouble(Double::valueOf)
                .average()
                .getAsDouble();
    }

    private synchronized void prettyPrint() {
        File file = new File(logFileName);
        file.getParentFile().mkdirs();
        PrintWriter out;
        try {
            out = new PrintWriter(file);
            out.println("Total (real) games played so far: "+bandit.realSamplesWithoutInitialization());
            out.println("Bandit: "+bandit.getName()+": ");
            out.print(bandit.toString(true));
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public synchronized String sampleNextSolution() {
        return bandit.sampleNextArm();
    }

    public synchronized void updateWithEvaluationForSolution(double evaluation, String solution) {
        if(bandit.isUsingVirtualLoss()) {
            for (int i = 0; i < bandit.getNumberOfVirtualLosses(); i++) {
                bandit.removeArmResult(solution, 0);
            }
        }
        bandit.addArmResult(solution, evaluation);
        logProgress();
    }

}
