package experiments.NTBO;

import experiments.ExperimentConfiguration;
import experiments.FitnessEvaluator;
import utils.LimitedSizeQueue;
import utils.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class NTBO {

    private int ID;
    private LimitedSizeQueue<Double> averagingWindow;
    private int averagingWindowSize = 100; //in matches
    private int currentBatch;
    private Set<Map<String, String>> evaluatedSolutions;
    private int finishedMatches;
    //maps an agent (String) to its evaluation
    private FitnessEvaluator fitnessEvaluator;
    //predicts value of a set of parameter settings (Map<String, String>) with the help of various bandits
    private NTBOFitnessModel fitnessModel;
    private int logBatchSize;
    private String logFileName;
    private boolean loggingMovingAverageWinrate = false;
    private PrintWriter out;
    private String playerOneTemplate;
    private boolean printedProblem = false;

    public NTBO(FitnessEvaluator fitnessEvaluator, NTBOFitnessModel fitnessModel, String playerOneTemplate, int ID, int logBatchSize) {
        this.fitnessEvaluator = fitnessEvaluator;
        this.fitnessModel = fitnessModel;
        this.playerOneTemplate = playerOneTemplate;
        evaluatedSolutions = new HashSet<>();
        this.ID = ID;
        this.logBatchSize = logBatchSize;
        finishedMatches = 0;
        currentBatch = 1;
        if(loggingMovingAverageWinrate) {
            averagingWindow = new LimitedSizeQueue<>(averagingWindowSize);
        }
        logFileName = System.getProperty("user.dir")+File.separator+"NTBO_summary_"+ID+".txt";
    }

    public synchronized Map<String, String> bestSolution() {
        Set<Map<String, String>> resultCandidates = new HashSet<>(evaluatedSolutions);
        Map<String, String> result = resultCandidates.stream().sorted(Comparator.comparingDouble(s -> -fitnessModel.predictFitnessOfSolution(s, false, false))).collect(Collectors.toList()).get(0);
        return result;
    }

    public double evaluateSolution(Map<String, String> solution) {
        synchronized(this) {
            fitnessModel.addEvaluationForSolution(0, solution);
            finishedMatches += fitnessEvaluator.matchesPerEvaluation();
            evaluatedSolutions.add(solution);
        }
        String playerOne = Util.fillInAgentTemplate(playerOneTemplate, solution);
        return fitnessEvaluator.evaluateFitness(playerOne);
    }

    public NTBOFitnessModel getFitnessModel() {
        return fitnessModel;
    }

    public int getID() {
        return ID;
    }

    public synchronized boolean isFinishedSampling() {
        boolean result = finishedMatches >= ExperimentConfiguration.MATCHES_PER_CONDITION;
        if(result) {
            fitnessEvaluator.reset();
        }
        return result;
    }

    private synchronized void logBestSolutionAfterBatch() {
        File file = new File(logFileName);
        file.getParentFile().mkdirs();
        try {
            out = new PrintWriter(new FileOutputStream(file, true));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        out.print(Util.fillInAgentTemplate(playerOneTemplate, bestSolution()) + "\r\n");
        out.flush();
        out.close();
    }

    private synchronized void logProgress() {
        if(!printedProblem) {
            printProblem();
            printedProblem = true;
        }
        if(loggingMovingAverageWinrate) {
            logWindowedAverageWinrate();
        }
        if(finishedMatches>=logBatchSize*currentBatch) {
            logBestSolutionAfterBatch();
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

    private synchronized void printProblem() {
        File file = new File(logFileName);
        file.getParentFile().mkdirs();
        try {
            out = new PrintWriter(new FileOutputStream(file, false));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        out.println(this);
        out.println("Solutions:");
        out.flush();
        out.close();
    }

    public synchronized Map<String, String> sampleNextSolution() {
        return fitnessModel.sampleNextSolution();
    }

    public String toString() {
        String result = "";
        result += "Problem: "+ playerOneTemplate + "\r\n";
        result += fitnessEvaluator + "\r\n";
        result += fitnessModel.toString(false) + "\r\n";
        return result;
    }

    public synchronized void updateWithEvaluationForSolution(double evaluation, Map<String, String> solution) {
        fitnessModel.removeEvaluationForSolution(0, solution);
        fitnessModel.addEvaluationForSolution(evaluation, solution);
        if(loggingMovingAverageWinrate) {
            averagingWindow.add(evaluation);
        }
        logProgress();
    }

}
