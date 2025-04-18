package experiments;

import ai.BasicMCTS;
import ai.Board;
import ai.Evaluation;
import ai.Move;
import ai.nodes.ValueEstimate;
import games.Connect4NoTranspositionsBoard;
import experiments.AlphaZero.AlphaZeroBoard;
import experiments.AlphaZero.AlphaZeroGame;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.*;

import static utils.Util.round;

public class TransitionLogger {

    private final static Object writeLock = new Object();
    private Match match;
    private boolean printShortCSV = false;
    private List<TransitionInfo> trajectory;
    private TransitionLogConfiguration transitionLogConfiguration;

    public TransitionLogger(Match match, TransitionLogConfiguration logConfiguration) {
        this.match = match;
        this.transitionLogConfiguration = logConfiguration;
        if(transitionLogConfiguration.isLoggingAnything()) {
            trajectory = new ArrayList<>();
        }
    }

    Map<Integer, String> CSVOfAllFutureActionSymmetries(AlphaZeroGame game, int index, List<Integer> timeOffsetsOfFutureActionsToLog) {
        Map<Integer, String> result = new HashMap<>();
        for(int symmetry=1; symmetry<=game.numberOfBoardSymmetries(); symmetry++) {
            String futureActions = "";
            for(int timeOffset : timeOffsetsOfFutureActionsToLog) {
                if(index+timeOffset<trajectory.size()) {
                    futureActions += game.moveIndex(symmetry, Collections.max(trajectory.get(index+timeOffset).policyTarget.getTargetValuesOfMoves().entrySet(), Map.Entry.comparingByValue()).getKey()) + ",";
                } else {
                    futureActions += "-1,";
                }
            }
            result.put(symmetry, futureActions);
        }
        return result;
    }

    Map<Integer, String> CSVOfAllPolicyTargetSymmetries(AlphaZeroGame game, PolicyTarget policyTarget) {
        Map<Integer, String> result = new HashMap<>();
        Map<Move, Double> targetValuesOfMoves = policyTarget.getTargetValuesOfMoves();
        for(int symmetry=1; symmetry<=game.numberOfBoardSymmetries(); symmetry++) {
            double[] targetArray;
            String targetString = "";
            for(int moveDimension = 0; moveDimension < game.numberOfMoveDimensions(); moveDimension++) {
                targetArray = new double[game.numberOfMoveCSVIndicesPerDimension()[moveDimension]];
                for (Move move : targetValuesOfMoves.keySet()) {
                    targetArray[game.moveIndex(symmetry, move)[moveDimension]] += targetValuesOfMoves.get(move);
                }
                for (int i = 0; i < targetArray.length; i++) {
                    targetString += round(targetArray[i], 5) + ",";
                }
            }
            result.put(symmetry, targetString);
        }
        return result;
    }

    private String additionalTransitionLogging(TransitionInfo transition) {
        String result = "";
        if(transitionLogConfiguration.isLoggingStateActionInputs()) {
            result += match.getBoard().evalOfTerminalBoard().toCSV();
        } else {
            if (transitionLogConfiguration.isLoggingMatchResultsWithTransitions()) {
                result += match.getBoard().evalOfTerminalBoard().toCSV();
            }
            if (transitionLogConfiguration.isLoggingValueEstimatesWithTransitions()) {
                result += transition.valueEstimate.averageResultsToCSV();
            }
        }
        return result;
    }

    private void logAllTransitions() {
        File file = logFileForAllTransitions();
        if(printShortCSV) {
            synchronized (writeLock) {
                PrintWriter out = newLogPrintWriter(file);
                for (int index = 0; index < trajectory.size(); index++) {
                    TransitionInfo transition = trajectory.get(index);
                    if (transition.toBeIncludedInTrainingData) {
                        writeShortTransitionToFile(index, out);
                    }
                }
                out.flush();
                out.close();
            }
        } else {
            synchronized (writeLock) {
                PrintWriter out = newLogPrintWriter(file);
                for(int index = 0; index < trajectory.size(); index++) {
                    TransitionInfo transition = trajectory.get(index);
                    if(transition.toBeIncludedInTrainingData) {
                        writeTransitionToFile(index, out);
                    }
                }
                out.flush();
                out.close();
            }
        }
    }

    private File logFileForAllTransitions() {
        String fileName;
        if(transitionLogConfiguration.isUsingSingleOutputFile()) {
            fileName = System.getProperty("user.dir") + File.separator + transitionLogConfiguration.getOutputFileName();
        } else {
            fileName = match.getFileName().substring(0, match.getFileName().length()-5).concat(".csv");
        }
        return new File(fileName);
    }

    private File logFileForTransition(int index) {
        String fileName;
        if(transitionLogConfiguration.isUsingSingleOutputFile()) {
            fileName = System.getProperty("user.dir") + File.separator + transitionLogConfiguration.getOutputFileName();
        } else {
            fileName = match.getFileName().substring(0, match.getFileName().length()-5).concat("_"+index).concat(".csv");
        }
        return new File(fileName);
    }

    public void logMatchEnd() {
        if(transitionLogConfiguration.getLogFrequency()==LogFrequency.ONETRANSITIONPERMATCH) {
            Random random = new Random();
            int startIndex = random.nextInt(trajectory.size());
            for(int index = startIndex; index < trajectory.size(); index++) {
                if(trajectory.get(index).toBeIncludedInTrainingData) {
                    logTransition(startIndex);
                }
            }
            for(int index = 0; index < startIndex; index++) {
                if(trajectory.get(index).toBeIncludedInTrainingData) {
                    logTransition(startIndex);
                }
            }
        } else if(transitionLogConfiguration.getLogFrequency()==LogFrequency.ALLTRANSITIONS) {
            logAllTransitions();
        }
    }

    private void logTransition(int index) {
        File file = logFileForTransition(index);
        try {
            synchronized (writeLock) {
                PrintWriter out = newLogPrintWriter(file);
                writeTransitionToFile(index, out);
                out.flush();
                out.close();
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    public void moveReceived() {
        if(transitionLogConfiguration.isLoggingAnything()) {
            if (transitionLogConfiguration.isPlayerToLog(match.getPlayerNames()[match.getPlayerNumberForColor()[match.getBoard().getColorToPlay()]])) {
                BasicMCTS player = (BasicMCTS) match.getPlayerForColor()[match.getBoard().getColorToPlay()];
                if (transitionLogConfiguration.isLoggingEvalsWithTransitions()) {
                    Evaluation staticEval = player.evaluateRoot();
                    trajectory.get(trajectory.size() - 1).staticEval = staticEval;
                }
                if (transitionLogConfiguration.isLoggingValueEstimatesWithTransitions()) {
                    ValueEstimate valueEstimate = player.valueEstimateOfLastSearch();
                    trajectory.get(trajectory.size() - 1).valueEstimate = valueEstimate;
                }
                if (transitionLogConfiguration.isLoggingPolicyTargetsWithTransitions()) {
                    PolicyTarget policyTarget = player.policyTarget();
                    trajectory.get(trajectory.size() - 1).policyTarget = policyTarget;
                }
                if (player.lastSearchWasLoggable()) {
                    trajectory.get(trajectory.size() - 1).toBeIncludedInTrainingData = true;
                } else {
                    trajectory.get(trajectory.size() - 1).toBeIncludedInTrainingData = false;
                }
            }
        }
    }

    private PrintWriter newLogPrintWriter(File file) {
        PrintWriter out = null;
        try {
            if (transitionLogConfiguration.isUsingSingleOutputFile()) {
                out = new PrintWriter(new FileOutputStream(file, true));
            } else {
                out = new PrintWriter(file);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return out;
    }

    public void requestingMove() {
        if(transitionLogConfiguration.isLoggingAnything()) {
            if(transitionLogConfiguration.isPlayerToLog(match.getPlayerNames()[match.getPlayerNumberForColor()[match.getBoard().getColorToPlay()]])) {
                Board visitedBoard = match.getGame().newBoard();
                visitedBoard.copyDataFrom(match.getBoard());
                TransitionInfo transitionInfo = new TransitionInfo();
                transitionInfo.startingBoard = visitedBoard;
                trajectory.add(transitionInfo);
            }
        }
    }

    private void writeShortTransitionToFile(int index, PrintWriter out) {
        TransitionInfo transition = trajectory.get(index);
        Board startingBoard = transition.startingBoard;
        Map<Integer, String> boardSymmetries = ((AlphaZeroBoard) startingBoard).shortCSVOfAllBoardSymmetries();
        for (Integer symmetry : boardSymmetries.keySet()) {
            out.print(boardSymmetries.get(symmetry));
            if(transitionLogConfiguration.isLoggingEvalsWithTransitions()) {
                out.print(round(transition.staticEval.getValueForColor(0),5)+",");
            }
            out.print(round(transition.valueEstimate.getAverageResultForColor(0), 5));
            out.println();
        }
    }

    private void writeTransitionToFile(int index, PrintWriter out) {
        TransitionInfo transition = trajectory.get(index);
        Board startingBoard = transition.startingBoard;

        if(transitionLogConfiguration.isLoggingStateActionInputs()) {

        } else {
            Map<Integer, String> boardSymmetries = ((AlphaZeroBoard) startingBoard).CSVOfAllBoardSymmetries();
            Map<Integer, String> policyTargetSymmetries = null;
            Map<Integer, String> futureActionSymmetries = null;
            Map<Integer, String> winningSquareSymmetries = null;

            if (transitionLogConfiguration.isLoggingPolicyTargetsWithTransitions()) {
                policyTargetSymmetries = CSVOfAllPolicyTargetSymmetries((AlphaZeroGame) match.getGame(), transition.policyTarget);
            }
            if (!transitionLogConfiguration.getTimeOffsetsOfFutureActionsToLog().isEmpty()) {
                futureActionSymmetries = CSVOfAllFutureActionSymmetries((AlphaZeroGame) match.getGame(), index, transitionLogConfiguration.getTimeOffsetsOfFutureActionsToLog());
            }
            if(transitionLogConfiguration.isLoggingWinningSquare()) {
                winningSquareSymmetries = CSVOfAllWinningSquareSymmetries(match);
            }
            for (Integer symmetry : boardSymmetries.keySet()) {
                out.print(boardSymmetries.get(symmetry));
                out.print(additionalTransitionLogging(transition));
                if (transitionLogConfiguration.isLoggingPolicyTargetsWithTransitions()) {
                    out.print(policyTargetSymmetries.get(symmetry));
                }
                if (!transitionLogConfiguration.getTimeOffsetsOfFutureActionsToLog().isEmpty()) {
                    out.print(futureActionSymmetries.get(symmetry));
                }
                if(transitionLogConfiguration.isLoggingWinningSquare()) {
                    out.print(winningSquareSymmetries.get(symmetry));
                }
                out.println();
            }

        }
    }

    private Map<Integer, String> CSVOfAllWinningSquareSymmetries(Match match) {
        Move winningSquare = ((Connect4NoTranspositionsBoard)match.getBoard()).winningMove();
        AlphaZeroGame game = (AlphaZeroGame) match.getGame();
        Map<Integer, String> result = new HashMap<>();
        for(int symmetry=1; symmetry<=game.numberOfBoardSymmetries(); symmetry++) {
            String targetString = "";
            for(int moveDimension = 0; moveDimension < game.numberOfMoveDimensions(); moveDimension++) {
                double[] targetArray = new double[game.numberOfMoveCSVIndicesPerDimension()[moveDimension]];
                targetArray[game.moveIndex(symmetry, winningSquare)[moveDimension]] = 1;
                for (int i = 0; i < targetArray.length; i++) {
                    targetString += targetArray[i] + ",";
                }
            }
            result.put(symmetry, targetString);
        }
        return result;
    }

    class TransitionInfo {
        PolicyTarget policyTarget;
        Board startingBoard;
        boolean toBeIncludedInTrainingData;
        ValueEstimate valueEstimate;
        Evaluation staticEval;
    }

}
