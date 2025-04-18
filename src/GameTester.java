import ai.*;
import ai.evaluation.BoardEvaluator;
import ai.policies.RandomPolicy;
import games.*;
import utils.UnknownPropertyException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static utils.Util.printVector;
import static utils.Util.round;

public class GameTester {

    static int depthForColor[];
    static long durationForColor[];
    static List<List<Double>> evaluationsPerTurnPerMatch;
    static BoardEvaluator evaluator;
    static Game game;
    static long maxDurationForColor[];
    static long maxPlayoutsForColor[];
    static int movesForColor[];
    static int numberOfMatches = 1;
    static int maxNumberOfTurns = 1000000;
    static Player[] players;
    static long playoutsForColor[];
    static boolean printLog = true;
    static Board refereeBoard;
    static long startOfMatch;
    static String testPositionName = "chch_bug.txt";
    static String testPositionWithPath = "c://Users//hendr//Dropbox//Eclipse Current Workspace//StateValueMCTSwithDJL//" + testPositionName;
    static int totalBranchingFactor = 0;
    static long totalCallsAtAnyDepthForAllColors;
    static long[] totalCallsAtDepthForAllColors;
    static int totalDepthForColor[];
    static long totalDurationForColor[];
    static int totalMovesForColor[];
    static long totalPlayoutsForColor[];
    static long totalTime = 0;
    static boolean useEvaluator = false;
    static boolean useTestPosition = false;
    static double winsForStartingColor = 0;
    static int numberOfRandomOpeningTurnsPerColor = 1;

    private static void addToEvaluationsPerTurnPerMatch(double value) {
        evaluationsPerTurnPerMatch.get(evaluationsPerTurnPerMatch.size()-1).add(value);
    }

    private static Game createGame(int testNumber) throws UnknownPropertyException {
//        Util.DEBUG = true;
        Game game = null;
        switch (testNumber) {
            case 0:
                game = new Breakthrough(8);
                break;
            case 1:
                game = new Connect4(7);
                break;
            default:
                break;
        }
//        game.setProperty("h", "");
//        game.setProperty("w", "");
        return game;
    }

    private static Player createPlayer(int color, int testNumber) {
        Player player;
        if(color==0) {
            player = new BasicMCTS();
        } else {
            player = new BasicMCTS();
        }
        return player;
    }

    private static Move findMove() {
        long start = System.currentTimeMillis();
        Move move = null;
        int colorToPlay = refereeBoard.getColorToPlay();
        try {
            move = players[colorToPlay].bestMove();
        } catch (NullMoveException e) {
            System.out.println("Exception appeared at turn: "+refereeBoard.getTurn());
            System.out.println("Color to play: "+colorToPlay+". Player:");
            System.out.println(players[colorToPlay]);
        }
        long stop = System.currentTimeMillis();
        long duration = stop - start;
        durationForColor[colorToPlay] += duration;
        movesForColor[colorToPlay]++;
        if(duration> maxDurationForColor[colorToPlay]) {
            maxDurationForColor[colorToPlay] = duration;
        }
        totalBranchingFactor += refereeBoard.getLegalMoves().size();
        if(players[colorToPlay] instanceof BasicMCTS) {
            MCTS currentMCTSPlayer = (MCTS) players[colorToPlay];
            double value = currentMCTSPlayer.getRoot().getValueEstimateOf(move).getAverageResultForColor(colorToPlay);
            int rolloutsForBestMove = currentMCTSPlayer.getRoot().getValueEstimateOf(move).getSamples();
            playoutsForColor[colorToPlay] += currentMCTSPlayer.getSimulationsInSearch();
            if(currentMCTSPlayer.getSimulationsInSearch()> maxPlayoutsForColor[colorToPlay]) {
                maxPlayoutsForColor[colorToPlay] = currentMCTSPlayer.getSimulationsInSearch();
            }
            if(printLog) System.out.println("thought for "+duration+" milliseconds, simulations:" + currentMCTSPlayer.getSimulationsInSearch()+", nodes: " + currentMCTSPlayer.treeSize()+", move: "+move+", estimated value: "+ printVector(currentMCTSPlayer.getRoot().getValueEstimateOf(move).getAverageResults()) + " ("+rolloutsForBestMove+" samples)");
            addToEvaluationsPerTurnPerMatch(value);
        }
        if(players[colorToPlay] instanceof TreeSearchAlgorithm) {
            int[] callsAtDepth = ((TreeSearchAlgorithm)players[colorToPlay]).getNodesAtDepth();
            for(int i=0;i<1000;i++) {
                totalCallsAtDepthForAllColors[i] += callsAtDepth[i];
                totalCallsAtAnyDepthForAllColors += callsAtDepth[i];
            }
        }
        return move;
    }

    private static void handleEndOfMatch(int matchNumber) {
        for(int color=0; color<players.length;color++) {
            totalDurationForColor[color] += durationForColor[color];
            totalPlayoutsForColor[color] += playoutsForColor[color];
            totalMovesForColor[color] += movesForColor[color];
            totalDepthForColor[color] += depthForColor[color];
        }
        long endOfMatch = System.currentTimeMillis();
        Evaluation matchResult = refereeBoard.evalOfTerminalBoard();
        double resultForStartingColor = matchResult.getValueForColor(game.getStartingColor());
        totalTime += (endOfMatch- startOfMatch);
        winsForStartingColor += resultForStartingColor;
    }

    private static void handleEndOfTest() {
        System.out.println("-----------------------------------------");
        System.out.println("Result over all matches: "+(winsForStartingColor /(double) numberOfMatches)+" for the starting color");
        System.out.println("Average match time: "+(totalTime/ numberOfMatches));
        int totalMovesByAllColors = 0;
        for(int c=0; c<players.length;c++) {
            totalMovesByAllColors += totalMovesForColor[c];
        }
        System.out.println("Average moves per game: "+(totalMovesByAllColors/(double)numberOfMatches));
        for(int color=0; color<players.length;color++) {
            if(players[color] instanceof BasicMCTS) {
                System.out.println("Over all matches, color " + color + " did " + (totalMovesForColor[color]/(double)numberOfMatches) + " moves on average, with on average " + (totalPlayoutsForColor[color] / (double) totalMovesForColor[color]) + " simulations and " + (totalDurationForColor[color] / (double) totalMovesForColor[color]) + " milliseconds.");
                System.out.println("Highest number of simulations for a move for color " + color + ": " + maxPlayoutsForColor[color]);
                System.out.println("Longest duration for a move for color " + color + ": " + maxDurationForColor[color]);
            } else {
                System.out.println("Over all matches, color " + color + " did " + (totalMovesForColor[color]/(double)numberOfMatches) + " moves on average, with on average a depth of " + round((totalDepthForColor[color] / (double) totalMovesForColor[color]),3) + " and " + (totalDurationForColor[color] / (double) totalMovesForColor[color]) + " milliseconds.");
                System.out.println("Longest duration for a move for color " + color + ": " + maxDurationForColor[color]);
                System.out.println("States visited per depth:");
            }
        }
        for(int i=0;i<31;i++) {
            System.out.println("("+i+", "+round((totalCallsAtDepthForAllColors[i]/(double)totalMovesByAllColors),1)+")");
        }
        System.out.println("Average branching factor over all moves: "+(totalBranchingFactor/(double)totalMovesByAllColors));
    }

    private static void handleStartOfTest(Game game) {
        evaluationsPerTurnPerMatch = new ArrayList<>();
        maxDurationForColor = new long[game.getNumberOfColors()];
        maxPlayoutsForColor = new long[game.getNumberOfColors()];
        players = new Player[game.getNumberOfColors()];
        totalDurationForColor = new long[game.getNumberOfColors()];
        totalMovesForColor = new int[game.getNumberOfColors()];
        totalPlayoutsForColor = new long[game.getNumberOfColors()];
        totalDepthForColor = new int[game.getNumberOfColors()];
        totalCallsAtDepthForAllColors = new long[1000];
        winsForStartingColor = 0;
        totalTime = 0;
        totalBranchingFactor = 0;
    }

    private static void initializeMatch(int testNumber) throws UnknownPropertyException {
        for(int color=0; color<game.getNumberOfColors();color++) {
            players[color] = initializePlayer(color, testNumber);
        }
        refereeBoard = useTestPosition ? game.newBoard(testPositionWithPath, true) : game.newBoard();
        startOfMatch = System.currentTimeMillis();
        resetStatsForGame(game);
        evaluationsPerTurnPerMatch.add(new ArrayList<>());
        if(numberOfRandomOpeningTurnsPerColor>0) {
            playRandomOpeningMoves();
        }
    }

    private static void playRandomOpeningMoves() {
        Random random = new Random();
        RandomPolicy randomPolicy = new RandomPolicy();
        for(int move=0; move<numberOfRandomOpeningTurnsPerColor; move++) {
            for (int color=0; color<game.getNumberOfColors(); color++) {
                Move randomMove = randomPolicy.selectMove(game, refereeBoard, random, null);
                for(Player player : players) {
                    player.acceptPlayMove(randomMove);
                }
                int colorToPlayBefore = refereeBoard.getColorToPlay();
                refereeBoard.play(randomMove);
                int colorToPlayAfter = refereeBoard.getColorToPlay();
                if(colorToPlayBefore==colorToPlayAfter) { //TODO this is for taking multiple moves in 1 turn
                    color--;
                }
            }
        }
    }

    private static Player initializePlayer(int color, int testNumber) throws UnknownPropertyException {
        Player player = createPlayer(color, testNumber);
        setProperties(player, color, testNumber);
        player.initialize();
        return player;
    }
//
    public static void main(String[] args) throws Exception {
        for(int i=0;i<1;i++) {
            runGameTest(i);
        }
    }

    private static void runGameTest(int testNumber) throws UnknownPropertyException {
        game = createGame(testNumber);
        handleStartOfTest(game);
        for(int match = 0; match< numberOfMatches; match++) {
            initializeMatch(testNumber);
            int numberOfTurns = 0;
            while(!refereeBoard.isTerminalBoard() && numberOfTurns<maxNumberOfTurns) {
                Move move = findMove();
                playMove(move);
                numberOfTurns++;
                if(useEvaluator) {
                    System.out.println(evaluator.evaluate(game, refereeBoard, null,null,null));
                }
            }
            handleEndOfMatch(match);
        }
        handleEndOfTest();
    }

    private static void playMove(Move move) {
        for(Player player : players) {
            player.acceptPlayMove(move);
        }
        refereeBoard.play(move);
        if(printLog) {
            System.out.println("after move:");
            System.out.println(refereeBoard);
            System.out.println("move no.: " + refereeBoard.getTurn());
            System.out.println();
            System.out.println("---");
            System.out.println();
        }
    }

    //TODO this only works for 2 players so far
    private static void printEvaluationsPerTurn() {
        for(List<Double> game : evaluationsPerTurnPerMatch) {
            boolean lastPlayerWins = game.get(game.size() - 1) >= 0.9;
            boolean secondToLastPlayerWins = game.get(game.size() - 1) <= 0.1;
            boolean evenTurnPlayerWins = false;
            boolean oddTurnPlayerWins = (game.size() % 2 == 0 && lastPlayerWins) || (game.size() % 2 == 1 && secondToLastPlayerWins);
            if((game.size()%2==1 && lastPlayerWins) || (game.size()%2==0 && secondToLastPlayerWins)) {
                evenTurnPlayerWins = true;
            }
            for(int index=0;index<game.size();index++) {
                if(index%2==0 && oddTurnPlayerWins) {
                    game.set(index, 1-game.get(index));
                }
                if(index%2==1 && evenTurnPlayerWins) {
                    game.set(index, 1-game.get(index));
                }
            }
            for(Double value : game) {
                if(printLog) System.out.print(round(value,3)+", ");
            }
            if(printLog) System.out.println();
        }
    }

    private static void resetStatsForGame(Game game) {
        durationForColor = new long[game.getNumberOfColors()];
        playoutsForColor = new long[game.getNumberOfColors()];
        movesForColor = new int[game.getNumberOfColors()];
        depthForColor = new int[game.getNumberOfColors()];
    }

    //    player=BasicMCTS game=Breakthrough noveltymcts=true simulations=5000 numberofnodes=5100 noveltyfunction=RawStateEvaluationNovelty evaluation=static noveltybeta=0.06 noveltyweight=0.06 exploration=0.2 vs.
//            player=BasicMCTS game=Breakthrough simulations=5000 numberofnodes=5100 evaluation=static exploration=0.2

    private static void setProperties(Player player, int color, int testNumber) throws UnknownPropertyException {
        player.setProperty("game", game.getName());
        player.setProperty("randomized", "true");
        switch(testNumber) {
            case 0:
                if(color==0) {
                    player.setProperty("noveltymcts", "true");
                    player.setProperty("simulations", "5000");
                    player.setProperty("numberofnodes", "5100");
                    player.setProperty("noveltyfunction", "RawStateEvaluationNovelty");
                    player.setProperty("evaluation", "static");
                    player.setProperty("noveltybeta", "0.06");
                    player.setProperty("noveltyweight", "0.06");
                    player.setProperty("exploration", "0.2");
                } else {
                    player.setProperty("simulations", "5000");
                    player.setProperty("numberofnodes", "5100");
                    player.setProperty("evaluation", "static");
                    player.setProperty("exploration", "0.2");
                }
                break;
            case 1:

                break;
            default:
                break;
        }
        if(useTestPosition) {
            player.setProperty("positionfile", testPositionWithPath);
        }
    }

}









