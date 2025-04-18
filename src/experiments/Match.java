package experiments;

import ai.*;
import ai.policies.RandomPolicy;
import utils.UnknownPropertyException;
import utils.Util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Random;

import static ai.djl.Model.newInstance;
import static experiments.ExperimentConfiguration.logMatches;


public class Match {

    private Board board;
    private int[] depthReachedByAnyTreeSearcher;
    private long[] durationForColor;
    private String fileName;
    private Game game;
    private MatchSummaryLogger matchLogger = null;
    private int movesByAllTreeSearchers;
    private int[] movesForColor;
    private int numberOfRandomOpeningTurnsPerColor = 0;
    private Player[] playerForColor;
    private String[] playerNames;
    private int[] playerNumberForColor;
    private long[] playoutsForColor;
    private long randomSeed;
    private int totalTreeDepthOfAllTreeSearchers;
    private int totalTreeSizeOfAllTreeSearchers;
    private TransitionLogger transitionLogger = null;

    public Match(String fileName, String playerOne, String playerTwo, int[] playerNumberForColor) {
        this.fileName = fileName;
        playerNames = new String[2];
        playerNames[0] = playerOne;
        playerNames[1] = playerTwo;
        this.playerNumberForColor = playerNumberForColor;
        if(logMatches) {
            matchLogger = new MatchSummaryLogger(this, fileName);
        }
    }

    private void createPlayers() throws UnknownPropertyException {
        String playersForColorString = "";
        for(int color=0;color<playerForColor.length;color++) {
            playersForColorString += "color"+color+"="+playerNames[playerNumberForColor[color]].replace('=', ':').replace(' ',',')+" ";
        }
        for(int color=0;color<playerForColor.length;color++) {
            String[] playerArguments = (playerNames[playerNumberForColor[color]]+" "+playersForColorString).split(" ");
            playerForColor[color] = PlayerFactory.createPlayer(playerArguments);
        }
    }

    public Board getBoard() {
        return board;
    }

    public int[] getDepthReachedByAnyTreeSearcher() {
        return depthReachedByAnyTreeSearcher;
    }

    public long[] getDurationForColor() {
        return durationForColor;
    }

    public String getFileName() {
        return fileName;
    }

    public Game getGame() {
        return game;
    }

    public MatchSummaryLogger getMatchLogger() {
        return matchLogger;
    }

    public int getMovesByAllTreeSearchers() {
        return movesByAllTreeSearchers;
    }

    public int[] getMovesForColor() {
        return movesForColor;
    }

    public int getNumberOfRandomOpeningTurnsPerColor() {
        return numberOfRandomOpeningTurnsPerColor;
    }

    public Player[] getPlayerForColor() {
        return playerForColor;
    }

    public String[] getPlayerNames() {
        return playerNames;
    }

    public int[] getPlayerNumberForColor() {
        return playerNumberForColor;
    }

    public long[] getPlayoutsForColor() {
        return playoutsForColor;
    }

    public long getRandomSeed() {
        return randomSeed;
    }

    public int getTotalTreeDepthOfAllTreeSearchers() {
        return totalTreeDepthOfAllTreeSearchers;
    }

    public int getTotalTreeSizeOfAllTreeSearchers() {
        return totalTreeSizeOfAllTreeSearchers;
    }

    public TransitionLogger getTransitionLogger() {
        return transitionLogger;
    }

    public void setTransitionLogger(TransitionLogger transitionLogger) {
        this.transitionLogger = transitionLogger;
    }

    private void handleMatchEnd() {
        for(Player player : playerForColor) {
            player.endGame();
        }
        if(matchLogger!=null) {
            matchLogger.logMatchEnd();
        }
        if(transitionLogger !=null) {
            transitionLogger.logMatchEnd();
        }
    }

    private void initializeMatchStats(Game game) {
        durationForColor = new long[game.getNumberOfColors()];
        playoutsForColor = new long[game.getNumberOfColors()];
        movesForColor = new int[game.getNumberOfColors()];
        totalTreeSizeOfAllTreeSearchers = 0;
        totalTreeDepthOfAllTreeSearchers = 0;
        movesByAllTreeSearchers = 0;
        depthReachedByAnyTreeSearcher = new int[1000];
        playerForColor = new Player[game.getNumberOfColors()];
    }

    public Evaluation play() {
        prepareMatch();
        playMatch();
        handleMatchEnd();
        return board.evalOfTerminalBoard();
    }

    /**
     * we can just replace this method in a subclass.
     * this class has to give all parameters of the match as command line arguments to a process that will run a "matchProcessController".
     * the controller accepts command line arguments, builds the actual playable match from it, and returns the result of the game.
     * the match starts the process with a processBuilder, hands all arguments to it (probably best directly in the command line, so no further input from the outside is necessary), and uses a processListener to get the result of the game, turning it into the eval of the terminal board.
     */

    private void playMatch() {
        while (!board.isTerminalBoard() /*&& !(board.getTurn()>=2)*/) {
            long start = System.currentTimeMillis();
            if(matchLogger!=null) {
                matchLogger.logRequestingMove();
            }
            if(transitionLogger !=null) {
                transitionLogger.requestingMove();
            }
            Move move = null;
            try {
                move = playerForColor[board.getColorToPlay()].bestMove();
            } catch (Exception | NullMoveException e) {
                System.out.println("Exception at turn " + board.getTurn());
                System.out.println("Color throwing the exception: " + board.getColorToPlay());
                System.out.println("Player:");
                System.out.println(playerForColor[board.getColorToPlay()]);
                System.out.println("Exception: " + e);
                System.out.println("Stack trace: ");
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                System.out.println(sw);
            }
            long stop = System.currentTimeMillis();
            if(matchLogger!=null) {
                matchLogger.logMoveReceived(move);
            }
            if(transitionLogger !=null) {
                transitionLogger.moveReceived();
            }
            long duration = stop - start;
            updateMatchStats(duration);
            for(int color=0;color<playerForColor.length;color++) {
                playerForColor[color].acceptPlayMove(move);
            }
            board.play(move);
        }
    }

    private void playRandomOpeningMoves() {
        Random random = new Random(randomSeed);
        RandomPolicy randomPolicy = new RandomPolicy();
        for(int move=0; move<numberOfRandomOpeningTurnsPerColor; move++) {
            for (int color=0; color<playerForColor.length; color++) {
                Move randomMove = randomPolicy.selectMove(game, board, random, null);
                for(int c=0; c<playerForColor.length; c++) {
                    playerForColor[c].acceptPlayMove(randomMove);
                }
                int colorToPlayBefore = board.getColorToPlay();
                board.play(randomMove);
                int colorToPlayAfter = board.getColorToPlay();
                if(colorToPlayBefore==colorToPlayAfter) {
                    color--;
                }
            }
        }
    }

    protected void prepareMatch() {
        try {
            game = Util.findGameInPlayerString(playerNames[0]);
            initializeMatchStats(game);
            createPlayers();
            board = game.newBoard();
            if(numberOfRandomOpeningTurnsPerColor>0) {
                playRandomOpeningMoves();
            }
            if(matchLogger!=null) {
                matchLogger.logMatchStart();
            }
        } catch (Exception e) {
            System.err.println("In " + fileName + ":");
            System.err.println(board);
            e.printStackTrace(System.err);
            if(matchLogger!=null) {
                matchLogger.close();
            }
            System.exit(1);
        }
    }

    public void setNumberOfRandomOpeningTurnsPerColor(int numberOfRandomOpeningMovesPerColor, long randomSeed) {
        this.numberOfRandomOpeningTurnsPerColor = numberOfRandomOpeningMovesPerColor;
        this.randomSeed = randomSeed;
    }

    private void updateMatchStats(long duration) {
        int colorToPlay = board.getColorToPlay();
        if (playerForColor[colorToPlay] instanceof BasicMCTS) {
            MCTS currentMCTSPlayer = (MCTS) playerForColor[colorToPlay];
            playoutsForColor[colorToPlay] += currentMCTSPlayer.getSimulationsInSearch();
        }
        if(playerForColor[colorToPlay] instanceof TreeSearchAlgorithm) {
            TreeSearchAlgorithm currentTreeSearch = (TreeSearchAlgorithm) playerForColor[colorToPlay];
            totalTreeSizeOfAllTreeSearchers += currentTreeSearch.treeSize();
            movesByAllTreeSearchers++;
        }
        if(playerForColor[colorToPlay] instanceof DepthLimitedTreeSearchAlgorithm) {
            DepthLimitedTreeSearchAlgorithm currentTreeSearch = (DepthLimitedTreeSearchAlgorithm) playerForColor[colorToPlay];
            int treeDepthReached = currentTreeSearch.treeDepth();
            totalTreeDepthOfAllTreeSearchers += treeDepthReached;
            if(treeDepthReached<depthReachedByAnyTreeSearcher.length) {
                for(int depth=0;depth<=treeDepthReached;depth++) {
                    depthReachedByAnyTreeSearcher[depth]++;
                }
            }
        }
        durationForColor[colorToPlay] += duration;
        movesForColor[colorToPlay]++;
    }

}
