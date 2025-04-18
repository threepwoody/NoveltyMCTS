package experiments.NTBO;

import experiments.FitnessEvaluator;
import experiments.FixedOpponentsEvaluator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

import static experiments.ExperimentConfiguration.*;
import static utils.Util.expandTemplateIntoPlayers;
import static utils.Util.findGameInPlayerString;


public class NTBOExperiment implements Runnable {

    static final double explorationFactor = 2;
    //current best result is written to file every logBatchSize matches
    static final int logBatchSize = 500;
    //for multi-start experiments: runs every single optimization problem this often, then picks the final evaluation results produced by TextFileExperiment and runs another independent evaluation of the best result per problem.
    static final int numberOfRunsPerProblem = 8;
    static List<NTBO> NTBOS;
    static Set<Integer> banditDimensionalities = new HashSet<>(Arrays.asList(1, 2));
    static boolean isNeuralNetworkExperiment = false;
    static boolean isTimingExperiment = false;
    static PrintWriter out;

    static {
        NTBOS = new ArrayList<>();
        List<List<String>> OPPONENTS_PER_LINE = new ArrayList<>();
        int ntboID = 0;
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
            int numberOfColorsInGame = findGameInPlayerString(playerOneTemplate).getNumberOfColors();
            for(int run = 0; run<numberOfRunsPerProblem; run++) {
                Random random = new Random();
                FitnessEvaluator fitnessEvaluator = new FixedOpponentsEvaluator(OPPONENTS_PER_LINE.get(line), random);
                NTBOFitnessModel fitnessModel = new NTBOBanditFitnessModel(extractSettingsForTunableParameters(playerOneTemplate), banditDimensionalities, random, numberOfColorsInGame);
                NTBO NTBO = new NTBO(fitnessEvaluator, fitnessModel, playerOneTemplate, ntboID++, logBatchSize);
                NTBOS.add(NTBO);
            }
        }
    }

    public static Map<String, List<String>> extractSettingsForTunableParameters(String playerOneTemplate) {
        Map<String, List<String>> settingsForParameters = new HashMap<>();
        List<String> tokens =  Arrays.asList(playerOneTemplate.split(" "));
        for(String token : tokens) {
            if(token.contains("=")) {
                int j = token.indexOf('=');
                String setting = token.substring(j+1);
                if(setting.contains(",")) {
                    String parameter = token.substring(0,j);
                    String[] settings = setting.split(",");
                    List<String> settingSet = new ArrayList<>(Arrays.asList(settings));
                    settingsForParameters.put(parameter, settingSet);
                }
            }
        }
        return settingsForParameters;
    }

    public static void main(String[] args) {
        //run NTBO experiment
        Thread[] experimentThreads = new Thread[MATCHES_IN_PARALLEL];
        for (int i = 0; i < MATCHES_IN_PARALLEL; i++) {
            experimentThreads[i] = new Thread(new NTBOExperiment());
            experimentThreads[i].start();
        }
        try {
            for (int i = 0; i < MATCHES_IN_PARALLEL; i++) {
                experimentThreads[i].join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //do evaluation of intermediate and/or final best solutions logged by the NTBOs
        experimentThreads = new Thread[MATCHES_IN_PARALLEL];
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
        NTBOResultTester.printResultsOfExperiment();

        if(numberOfRunsPerProblem>1) {
            //do additional evaluation of best solutions found per multi-start NTBO experiment
            experimentThreads = new Thread[MATCHES_IN_PARALLEL];
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
            NTBOMultistartFinalizer.printResultsOfExperiment();
        }

    }

    private static void printBandits() {
        for(NTBO ntbo : NTBOS) {
            File file = new File(System.getProperty("user.dir") + File.separator + "banditsInNTBO" + ntbo.getID());
            file.getParentFile().mkdirs();
            try {
                out = new PrintWriter(file);
                out.println(ntbo.getFitnessModel().toString(true));
                out.flush();
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        for(NTBO NTBO : NTBOS) {
            while(!NTBO.isFinishedSampling()) {
                Map<String, String> nextSolutionToEvaluate = NTBO.sampleNextSolution();
                double evaluation = NTBO.evaluateSolution(nextSolutionToEvaluate);
                NTBO.updateWithEvaluationForSolution(evaluation, nextSolutionToEvaluate);
            }
        }
    }

}

