package experiments.BanditTuning;

import experiments.FitnessEvaluator;
import experiments.FixedOpponentsEvaluator;

import java.io.*;
import java.util.*;

import static experiments.ExperimentConfiguration.*;
import static utils.Util.expandTemplateIntoPlayers;


//For the BanditTuningExperiment, every line in ExperimentConfiguration represents a bandit problem - with a range of player 1s to choose from, and one constant player 2.
public class BanditTuningExperiment implements Runnable {

    static double banditExplorationFactor = 1.4;
    static List<BanditTuner> banditTuners;
    static int logBatchSize = 100;
    static String banditFileNamePrefix = "results_summary";
    static String summaryFileName = "results_summary_all_bandits.txt";

    static {
        banditTuners = new ArrayList<>();
        List<List<String>> OPPONENTS_PER_LINE = new ArrayList<>();
        int banditTunerID = 0;
        for(int line = 0; line < PLAYER_TWO_TEMPLATES.length; line++) {
            for(int opp = 0; opp < PLAYER_TWO_TEMPLATES[line].length; opp++) {
                List<String> opponents = expandTemplateIntoPlayers(new ArrayList<>(), "", Arrays.asList(PLAYER_TWO_TEMPLATES[line][opp].split(" ")));
                Collections.sort(opponents);
                if(OPPONENTS_PER_LINE.size()==line+1) {
                    opponents.addAll(OPPONENTS_PER_LINE.get(line));
                    Collections.sort(opponents);
                    OPPONENTS_PER_LINE.set(line, opponents);
                } else {
                    OPPONENTS_PER_LINE.add(opponents);
                }
            }
            String playerOneTemplate = PLAYER_ONE_TEMPLATES[line];
            Random random = new Random();
            FitnessEvaluator fitnessEvaluator = new FixedOpponentsEvaluator(OPPONENTS_PER_LINE.get(line), random);
            BanditTuner banditTuner = new BanditTuner(fitnessEvaluator, playerOneTemplate, banditTunerID, logBatchSize, banditExplorationFactor, System.getProperty("user.dir")+ File.separator+banditFileNamePrefix+String.format("%03d" , banditTunerID)+".txt");
            banditTuners.add(banditTuner);
            banditTunerID++;
        }
    }

    public static void main(String[] args) throws IOException {
        //prepare threads
        Thread[] experimentThreads = new Thread[MATCHES_IN_PARALLEL];
        for (int i = 0; i < MATCHES_IN_PARALLEL; i++) {
            experimentThreads[i] = new Thread(new BanditTuningExperiment());
            experimentThreads[i].start();
        }
        //run experiment
        try {
            for (int i = 0; i < MATCHES_IN_PARALLEL; i++) {
                experimentThreads[i].join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //summarize results in 1 file
        if(banditTuners.size()>1) {
            File directoryPath = new File(System.getProperty("user.dir")+File.separator);
            FilenameFilter fileNameFilter = (dir, name) -> name.startsWith(banditFileNamePrefix);
            File[] filesWithBanditResult = directoryPath.listFiles(fileNameFilter);
            Arrays.sort(filesWithBanditResult);
            File summaryFile = new File(System.getProperty("user.dir") + File.separator + summaryFileName);
            PrintWriter out = new PrintWriter(summaryFile);
            for(File file : filesWithBanditResult) {
                BufferedReader br = new BufferedReader(new FileReader(file));
                // Read from current file
                String line = br.readLine();
                while (line != null) {
                    // write to the output file
                    out.println(line);
                    line = br.readLine();
                }
                out.flush();
            }
        }
    }

    @Override
    public void run() {
        for(BanditTuner banditTuner : banditTuners) {
            while(!banditTuner.isFinishedSampling()) {
                String nextSolutionToEvaluate = banditTuner.sampleNextSolution();
                double evaluation = banditTuner.evaluateSolution(nextSolutionToEvaluate);
                banditTuner.updateWithEvaluationForSolution(evaluation, nextSolutionToEvaluate);
            }
        }
    }

}

