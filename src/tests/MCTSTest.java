package tests;

import ai.*;
import games.*;
import org.junit.Test;
import utils.UnknownPropertyException;
import utils.Util;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;


public class MCTSTest {

    @Test
    public void testStateActionValueNodesRolloutEvaluatorAtariGo() throws UnknownPropertyException, NullMoveException {

        Util.DEBUG = false;
        AtariGo game = new AtariGo(8);

        MCTS playerOne = new BasicMCTS();
        playerOne.setGame(game);
        playerOne.setProperty("simulations", "100");
        playerOne.setProperty("numberofnodes", "110");
        playerOne.setProperty("evaluation", "ai.evaluation.RolloutEvaluator");
        playerOne.setProperty("nodes", "StateAction");
        playerOne.setProperty("selection", "UCB1Tuned");
        playerOne.setProperty("exploration", "1");
        playerOne.setProperty("movechoice", "MaxSamples");
        playerOne.setProperty("randomized", "false");
        playerOne.initialize();

        MCTS playerTwo = new BasicMCTS();
        playerTwo.setGame(game);
        playerTwo.setProperty("simulations", "100");
        playerTwo.setProperty("numberofnodes", "110");
        playerTwo.setProperty("evaluation", "ai.evaluation.RolloutEvaluator");
        playerTwo.setProperty("nodes", "StateAction");
        playerTwo.setProperty("selection", "UCB1Tuned");
        playerTwo.setProperty("exploration", "1");
        playerTwo.setProperty("movechoice", "MaxSamples");
        playerTwo.setProperty("randomized", "false");
        playerTwo.initialize();

        Board refereeBoard = game.newBoard();
        Player currentPlayer = playerOne;
        long totalRolloutsForBestMoves = 0;

        while(!refereeBoard.isTerminalBoard()) {
            Move move = currentPlayer.bestMove();
            if(currentPlayer instanceof BasicMCTS) {
                MCTS currentMCTSPlayer = (MCTS) currentPlayer;
                totalRolloutsForBestMoves += currentMCTSPlayer.getRoot().getValueEstimateOf(move).getSamples();
            }
            currentPlayer.acceptPlayMove(move);
            ((currentPlayer==playerOne) ? playerTwo : playerOne).acceptPlayMove(move);
            refereeBoard.play(move);
            currentPlayer = (currentPlayer==playerOne) ? playerTwo : playerOne;
        }
        System.out.println(refereeBoard);
        Board expectedResult = game.newBoard("expected test results\\testStateActionValueNodesRolloutEvaluatorAtariGo.txt", true);
        assertEquals(255, totalRolloutsForBestMoves);
        assertEquals(Arrays.deepHashCode(((AtariGoNoTranspositionsBoard) expectedResult).getBoard()),Arrays.deepHashCode(((AtariGoNoTranspositionsBoard) refereeBoard).getBoard()));

    }

    @Test
    public void testStateActionValueNodesRolloutEvaluatorBreakthrough() throws UnknownPropertyException, NullMoveException {

        Util.DEBUG = false;
        Breakthrough game = new Breakthrough(8);

        MCTS playerOne = new BasicMCTS();
        playerOne.setGame(game);
        playerOne.setProperty("simulations", "1000");
        playerOne.setProperty("numberofnodes", "1100");
        playerOne.setProperty("evaluation", "ai.evaluation.RolloutEvaluator");
        playerOne.setProperty("nodes", "StateAction");
        playerOne.setProperty("selection", "UCB1Tuned");
        playerOne.setProperty("exploration", "1");
        playerOne.setProperty("movechoice", "MaxSamples");
        playerOne.setProperty("randomized", "false");
        playerOne.initialize();

        MCTS playerTwo = new BasicMCTS();
        playerTwo.setGame(game);
        playerTwo.setProperty("simulations", "1000");
        playerTwo.setProperty("numberofnodes", "1100");
        playerTwo.setProperty("evaluation", "ai.evaluation.RolloutEvaluator");
        playerTwo.setProperty("nodes", "StateAction");
        playerTwo.setProperty("selection", "UCB1Tuned");
        playerTwo.setProperty("exploration", "1");
        playerTwo.setProperty("movechoice", "MaxSamples");
        playerTwo.setProperty("randomized", "false");
        playerTwo.initialize();

        Board refereeBoard = game.newBoard();
        Player currentPlayer = playerOne;
        long totalRolloutsForBestMoves = 0;

        while(!refereeBoard.isTerminalBoard()) {
            Move move = currentPlayer.bestMove();
            if(currentPlayer instanceof BasicMCTS) {
                MCTS currentMCTSPlayer = (MCTS) currentPlayer;
                totalRolloutsForBestMoves += currentMCTSPlayer.getRoot().getValueEstimateOf(move).getSamples();
            }
            currentPlayer.acceptPlayMove(move);
            ((currentPlayer==playerOne) ? playerTwo : playerOne).acceptPlayMove(move);
            refereeBoard.play(move);
            currentPlayer = (currentPlayer==playerOne) ? playerTwo : playerOne;
        }
        Board expectedResult = game.newBoard("expected test results\\testStateActionValueNodesRolloutEvaluatorBreakthrough.txt", true);
        System.out.println(refereeBoard);
        assertEquals(7672, totalRolloutsForBestMoves);
        assertEquals(Arrays.deepHashCode(((BreakthroughNoTranspositionsBoard) expectedResult).getBoard()),Arrays.deepHashCode(((BreakthroughNoTranspositionsBoard) refereeBoard).getBoard()));

    }

    @Test
    public void testStateActionValueNodesRolloutEvaluatorConnect4() throws UnknownPropertyException, NullMoveException {

        Util.DEBUG = false;
        Connect4 game = new Connect4(9);

        MCTS playerOne = new BasicMCTS();
        playerOne.setGame(game);
        playerOne.setProperty("simulations", "1000");
        playerOne.setProperty("numberofnodes", "1100");
        playerOne.setProperty("evaluation", "ai.evaluation.RolloutEvaluator");
        playerOne.setProperty("nodes", "StateAction");
        playerOne.setProperty("selection", "UCB1Tuned");
        playerOne.setProperty("exploration", "1");
        playerOne.setProperty("movechoice", "MaxSamples");
        playerOne.setProperty("randomized", "false");
        playerOne.initialize();

        MCTS playerTwo = new BasicMCTS();
        playerTwo.setGame(game);
        playerTwo.setProperty("simulations", "1000");
        playerTwo.setProperty("numberofnodes", "1100");
        playerTwo.setProperty("evaluation", "ai.evaluation.RolloutEvaluator");
        playerTwo.setProperty("nodes", "StateAction");
        playerTwo.setProperty("selection", "UCB1Tuned");
        playerTwo.setProperty("exploration", "1");
        playerTwo.setProperty("movechoice", "MaxSamples");
        playerTwo.setProperty("randomized", "false");
        playerTwo.initialize();

        Board refereeBoard = game.newBoard();
        Player currentPlayer = playerOne;
        long totalRolloutsForBestMoves = 0;

        while(!refereeBoard.isTerminalBoard()) {
            Move move = currentPlayer.bestMove();
            if(currentPlayer instanceof BasicMCTS) {
                MCTS currentMCTSPlayer = (MCTS) currentPlayer;
                totalRolloutsForBestMoves += currentMCTSPlayer.getRoot().getValueEstimateOf(move).getSamples();
            }
            currentPlayer.acceptPlayMove(move);
            ((currentPlayer==playerOne) ? playerTwo : playerOne).acceptPlayMove(move);
            refereeBoard.play(move);
            currentPlayer = (currentPlayer==playerOne) ? playerTwo : playerOne;
        }
        Board expectedResult = game.newBoard("expected test results\\testStateActionValueNodesRolloutEvaluatorConnect4.txt", true);
        System.out.println(refereeBoard);
        assertEquals(25758, totalRolloutsForBestMoves);
        assertEquals(Arrays.deepHashCode(((Connect4NoTranspositionsBoard) expectedResult).getBoard()),Arrays.deepHashCode(((Connect4NoTranspositionsBoard) refereeBoard).getBoard()));

    }

    @Test
    public void testStateActionValueNodesRolloutEvaluatorDomineering() throws UnknownPropertyException, NullMoveException {

        Util.DEBUG = false;
        Domineering game = new Domineering();

        MCTS playerOne = new BasicMCTS();
        playerOne.setGame(game);
        playerOne.setProperty("simulations", "1000");
        playerOne.setProperty("numberofnodes", "1100");
        playerOne.setProperty("evaluation", "ai.evaluation.RolloutEvaluator");
        playerOne.setProperty("nodes", "StateAction");
        playerOne.setProperty("selection", "UCB1Tuned");
        playerOne.setProperty("exploration", "1");
        playerOne.setProperty("movechoice", "MaxSamples");
        playerOne.setProperty("randomized", "false");
        playerOne.initialize();

        MCTS playerTwo = new BasicMCTS();
        playerTwo.setGame(game);
        playerTwo.setProperty("simulations", "1000");
        playerTwo.setProperty("numberofnodes", "1100");
        playerTwo.setProperty("evaluation", "ai.evaluation.RolloutEvaluator");
        playerTwo.setProperty("nodes", "StateAction");
        playerTwo.setProperty("selection", "UCB1Tuned");
        playerTwo.setProperty("exploration", "1");
        playerTwo.setProperty("movechoice", "MaxSamples");
        playerTwo.setProperty("randomized", "false");
        playerTwo.initialize();

        Board refereeBoard = game.newBoard();
        Player currentPlayer = playerOne;
        long totalRolloutsForBestMoves = 0;

        while(!refereeBoard.isTerminalBoard()) {
            Move move = currentPlayer.bestMove();
            if(currentPlayer instanceof BasicMCTS) {
                MCTS currentMCTSPlayer = (MCTS) currentPlayer;
                totalRolloutsForBestMoves += currentMCTSPlayer.getRoot().getValueEstimateOf(move).getSamples();
            }
            currentPlayer.acceptPlayMove(move);
            ((currentPlayer==playerOne) ? playerTwo : playerOne).acceptPlayMove(move);
            refereeBoard.play(move);
            currentPlayer = (currentPlayer==playerOne) ? playerTwo : playerOne;
        }
        System.out.println(refereeBoard);
        Board expectedResult = game.newBoard("expected test results\\testStateActionValueNodesRolloutEvaluatorDomineering.txt", true);
        assertEquals(7554, totalRolloutsForBestMoves);
        assertEquals(Arrays.deepHashCode(((DomineeringNoTranspositionsBoard) expectedResult).getBoard()),Arrays.deepHashCode(((DomineeringNoTranspositionsBoard) refereeBoard).getBoard()));

    }

    @Test
    public void testStateActionValueNodesRolloutEvaluatorKnightthrough() throws UnknownPropertyException, NullMoveException {

        Util.DEBUG = false;
        Knightthrough game = new Knightthrough(8);

        MCTS playerOne = new BasicMCTS();
        playerOne.setGame(game);
        playerOne.setProperty("simulations", "1000");
        playerOne.setProperty("numberofnodes", "1100");
        playerOne.setProperty("evaluation", "ai.evaluation.RolloutEvaluator");
        playerOne.setProperty("nodes", "StateAction");
        playerOne.setProperty("selection", "UCB1Tuned");
        playerOne.setProperty("exploration", "1");
        playerOne.setProperty("movechoice", "MaxSamples");
        playerOne.setProperty("randomized", "false");
        playerOne.initialize();

        MCTS playerTwo = new BasicMCTS();
        playerTwo.setGame(game);
        playerTwo.setProperty("simulations", "1000");
        playerTwo.setProperty("numberofnodes", "1100");
        playerTwo.setProperty("evaluation", "ai.evaluation.RolloutEvaluator");
        playerTwo.setProperty("nodes", "StateAction");
        playerTwo.setProperty("selection", "UCB1Tuned");
        playerTwo.setProperty("exploration", "1");
        playerTwo.setProperty("movechoice", "MaxSamples");
        playerTwo.setProperty("randomized", "false");
        playerTwo.initialize();

        Board refereeBoard = game.newBoard();
        Player currentPlayer = playerOne;
        long totalRolloutsForBestMoves = 0;

        while(!refereeBoard.isTerminalBoard()) {
            Move move = currentPlayer.bestMove();
            if(currentPlayer instanceof BasicMCTS) {
                MCTS currentMCTSPlayer = (MCTS) currentPlayer;
                totalRolloutsForBestMoves += currentMCTSPlayer.getRoot().getValueEstimateOf(move).getSamples();
            }
            currentPlayer.acceptPlayMove(move);
            ((currentPlayer==playerOne) ? playerTwo : playerOne).acceptPlayMove(move);
            refereeBoard.play(move);
            currentPlayer = (currentPlayer==playerOne) ? playerTwo : playerOne;
        }
        System.out.println(refereeBoard);
        Board expectedResult = game.newBoard("expected test results\\testStateActionValueNodesRolloutEvaluatorKnightthrough.txt", true);
        assertEquals(4686, totalRolloutsForBestMoves);
        assertEquals(Arrays.deepHashCode(((KnightthroughNoTranspositionsBoard) expectedResult).getBoard()),Arrays.deepHashCode(((KnightthroughNoTranspositionsBoard) refereeBoard).getBoard()));

    }

    @Test
    public void testStateActionValueNodesRolloutEvaluatorNoGo() throws UnknownPropertyException, NullMoveException {

        Util.DEBUG = false;
        NoGo game = new NoGo(8);

        MCTS playerOne = new BasicMCTS();
        playerOne.setGame(game);
        playerOne.setProperty("simulations", "100");
        playerOne.setProperty("numberofnodes", "110");
        playerOne.setProperty("evaluation", "ai.evaluation.RolloutEvaluator");
        playerOne.setProperty("nodes", "StateAction");
        playerOne.setProperty("selection", "UCB1Tuned");
        playerOne.setProperty("exploration", "1");
        playerOne.setProperty("movechoice", "MaxSamples");
        playerOne.setProperty("randomized", "false");
        playerOne.initialize();

        MCTS playerTwo = new BasicMCTS();
        playerTwo.setGame(game);
        playerTwo.setProperty("simulations", "100");
        playerTwo.setProperty("numberofnodes", "110");
        playerTwo.setProperty("evaluation", "ai.evaluation.RolloutEvaluator");
        playerTwo.setProperty("nodes", "StateAction");
        playerTwo.setProperty("selection", "UCB1Tuned");
        playerTwo.setProperty("exploration", "1");
        playerTwo.setProperty("movechoice", "MaxSamples");
        playerTwo.setProperty("randomized", "false");
        playerTwo.initialize();

        Board refereeBoard = game.newBoard();
        Player currentPlayer = playerOne;
        long totalRolloutsForBestMoves = 0;

        while(!refereeBoard.isTerminalBoard()) {
            Move move = currentPlayer.bestMove();
            if(currentPlayer instanceof BasicMCTS) {
                MCTS currentMCTSPlayer = (MCTS) currentPlayer;
                totalRolloutsForBestMoves += currentMCTSPlayer.getRoot().getValueEstimateOf(move).getSamples();
            }
            currentPlayer.acceptPlayMove(move);
            ((currentPlayer==playerOne) ? playerTwo : playerOne).acceptPlayMove(move);
            refereeBoard.play(move);
            currentPlayer = (currentPlayer==playerOne) ? playerTwo : playerOne;
        }
        System.out.println(refereeBoard);
        Board expectedResult = game.newBoard("expected test results\\testStateActionValueNodesRolloutEvaluatorNoGo.txt", true);
        assertEquals(1179, totalRolloutsForBestMoves);
        assertEquals(Arrays.deepHashCode(((NoGoNoTranspositionsBoard) expectedResult).getBoard()),Arrays.deepHashCode(((NoGoNoTranspositionsBoard) refereeBoard).getBoard()));

    }

    @Test
    public void testStateActionValueNodesRolloutEvaluatorOthello() throws UnknownPropertyException, NullMoveException {

        Util.DEBUG = false;
        Othello game = new Othello(8);

        MCTS playerOne = new BasicMCTS();
        playerOne.setGame(game);
        playerOne.setProperty("simulations", "200");
        playerOne.setProperty("numberofnodes", "210");
        playerOne.setProperty("evaluation", "ai.evaluation.RolloutEvaluator");
        playerOne.setProperty("nodes", "StateAction");
        playerOne.setProperty("selection", "UCB1Tuned");
        playerOne.setProperty("exploration", "1");
        playerOne.setProperty("movechoice", "MaxSamples");
        playerOne.setProperty("randomized", "false");
        playerOne.initialize();

        MCTS playerTwo = new BasicMCTS();
        playerTwo.setGame(game);
        playerTwo.setProperty("simulations", "200");
        playerTwo.setProperty("numberofnodes", "210");
        playerTwo.setProperty("evaluation", "ai.evaluation.RolloutEvaluator");
        playerTwo.setProperty("nodes", "StateAction");
        playerTwo.setProperty("selection", "UCB1Tuned");
        playerTwo.setProperty("exploration", "1");
        playerTwo.setProperty("movechoice", "MaxSamples");
        playerTwo.setProperty("randomized", "false");
        playerTwo.initialize();

        Board refereeBoard = game.newBoard();
        Player currentPlayer = playerOne;
        long totalRolloutsForBestMoves = 0;

        while(!refereeBoard.isTerminalBoard()) {
            Move move = currentPlayer.bestMove();
            if(currentPlayer instanceof BasicMCTS) {
                MCTS currentMCTSPlayer = (MCTS) currentPlayer;
                totalRolloutsForBestMoves += currentMCTSPlayer.getRoot().getValueEstimateOf(move).getSamples();
            }
            currentPlayer.acceptPlayMove(move);
            ((currentPlayer==playerOne) ? playerTwo : playerOne).acceptPlayMove(move);
            refereeBoard.play(move);
            currentPlayer = (currentPlayer==playerOne) ? playerTwo : playerOne;
        }
        Board expectedResult = game.newBoard("expected test results\\testStateActionValueNodesRolloutEvaluatorOthello.txt", true);
        System.out.println(refereeBoard);
        assertEquals(4125, totalRolloutsForBestMoves);
        assertEquals(Arrays.deepHashCode(((OthelloNoTranspositionsBoard) expectedResult).getBoard()),Arrays.deepHashCode(((OthelloNoTranspositionsBoard) refereeBoard).getBoard()));

    }

    @Test
    public void testStateActionValueNodesStaticEvaluatorBreakthrough() throws UnknownPropertyException, NullMoveException {

        Util.DEBUG = false;
        Breakthrough game = new Breakthrough(8);

        MCTS playerOne = new BasicMCTS();
        playerOne.setGame(game);
        playerOne.setProperty("simulations", "1000");
        playerOne.setProperty("numberofnodes", "1100");
        playerOne.setProperty("evaluation", "games.BreakthroughEvaluator");
        playerOne.setProperty("nodes", "StateAction");
        playerOne.setProperty("selection", "UCB1Tuned");
        playerOne.setProperty("exploration", "1");
        playerOne.setProperty("movechoice", "MaxSamples");
        playerOne.setProperty("randomized", "false");
        playerOne.initialize();

        MCTS playerTwo = new BasicMCTS();
        playerTwo.setGame(game);
        playerTwo.setProperty("simulations", "1000");
        playerTwo.setProperty("numberofnodes", "1100");
        playerTwo.setProperty("evaluation", "games.BreakthroughEvaluator");
        playerTwo.setProperty("nodes", "StateAction");
        playerTwo.setProperty("selection", "UCB1Tuned");
        playerTwo.setProperty("exploration", "1");
        playerTwo.setProperty("movechoice", "MaxSamples");
        playerTwo.setProperty("randomized", "false");
        playerTwo.initialize();

        Board refereeBoard = game.newBoard();
        Player currentPlayer = playerOne;
        long totalRolloutsForBestMoves = 0;

        while(!refereeBoard.isTerminalBoard()) {
            Move move = currentPlayer.bestMove();
            if(currentPlayer instanceof BasicMCTS) {
                MCTS currentMCTSPlayer = (MCTS) currentPlayer;
                totalRolloutsForBestMoves += currentMCTSPlayer.getRoot().getValueEstimateOf(move).getSamples();
            }
            currentPlayer.acceptPlayMove(move);
            ((currentPlayer==playerOne) ? playerTwo : playerOne).acceptPlayMove(move);
            refereeBoard.play(move);
            currentPlayer = (currentPlayer==playerOne) ? playerTwo : playerOne;
        }
        Board expectedResult = game.newBoard("expected test results\\testStateActionValueNodesStaticEvaluatorBreakthrough.txt", true);
        System.out.println(refereeBoard);
        assertEquals(9807, totalRolloutsForBestMoves);
        assertEquals(Arrays.deepHashCode(((BreakthroughNoTranspositionsBoard) expectedResult).getBoard()),Arrays.deepHashCode(((BreakthroughNoTranspositionsBoard) refereeBoard).getBoard()));

    }

    @Test
    public void testStateActionValueNodesStaticEvaluatorConnect4() throws UnknownPropertyException, NullMoveException {

        Util.DEBUG = false;
        Connect4 game = new Connect4(9);

        MCTS playerOne = new BasicMCTS();
        playerOne.setGame(game);
        playerOne.setProperty("simulations", "1000");
        playerOne.setProperty("numberofnodes", "1100");
        playerOne.setProperty("evaluation", "games.Connect4BetterEvaluator");
        playerOne.setProperty("nodes", "StateAction");
        playerOne.setProperty("selection", "UCB1Tuned");
        playerOne.setProperty("exploration", "1");
        playerOne.setProperty("movechoice", "MaxSamples");
        playerOne.setProperty("randomized", "false");
        playerOne.initialize();

        MCTS playerTwo = new BasicMCTS();
        playerTwo.setGame(game);
        playerTwo.setProperty("simulations", "1000");
        playerTwo.setProperty("numberofnodes", "1100");
        playerTwo.setProperty("evaluation", "games.Connect4BetterEvaluator");
        playerTwo.setProperty("nodes", "StateAction");
        playerTwo.setProperty("selection", "UCB1Tuned");
        playerTwo.setProperty("exploration", "1");
        playerTwo.setProperty("movechoice", "MaxSamples");
        playerTwo.setProperty("randomized", "false");
        playerTwo.initialize();

        Board refereeBoard = game.newBoard();
        Player currentPlayer = playerOne;
        long totalRolloutsForBestMoves = 0;

        while(!refereeBoard.isTerminalBoard()) {
            Move move = currentPlayer.bestMove();
            if(currentPlayer instanceof BasicMCTS) {
                MCTS currentMCTSPlayer = (MCTS) currentPlayer;
                totalRolloutsForBestMoves += currentMCTSPlayer.getRoot().getValueEstimateOf(move).getSamples();
            }
            currentPlayer.acceptPlayMove(move);
            ((currentPlayer==playerOne) ? playerTwo : playerOne).acceptPlayMove(move);
            refereeBoard.play(move);
            currentPlayer = (currentPlayer==playerOne) ? playerTwo : playerOne;
        }
        Board expectedResult = game.newBoard("expected test results\\testStateActionValueNodesStaticEvaluatorConnect4.txt", true);
        System.out.println(refereeBoard);
        assertEquals(27027, totalRolloutsForBestMoves);
        assertEquals(Arrays.deepHashCode(((Connect4NoTranspositionsBoard) expectedResult).getBoard()),Arrays.deepHashCode(((Connect4NoTranspositionsBoard) refereeBoard).getBoard()));

    }

    @Test
    public void testStateActionValueNodesStaticEvaluatorKnightthrough() throws UnknownPropertyException, NullMoveException {

        Util.DEBUG = false;
        Knightthrough game = new Knightthrough(8);

        MCTS playerOne = new BasicMCTS();
        playerOne.setGame(game);
        playerOne.setProperty("simulations", "1000");
        playerOne.setProperty("numberofnodes", "1100");
        playerOne.setProperty("evaluation", "games.KnightthroughEvaluator");
        playerOne.setProperty("nodes", "StateAction");
        playerOne.setProperty("selection", "UCB1Tuned");
        playerOne.setProperty("exploration", "1");
        playerOne.setProperty("movechoice", "MaxSamples");
        playerOne.setProperty("randomized", "false");
        playerOne.initialize();

        MCTS playerTwo = new BasicMCTS();
        playerTwo.setGame(game);
        playerTwo.setProperty("simulations", "1000");
        playerTwo.setProperty("numberofnodes", "1100");
        playerTwo.setProperty("evaluation", "games.KnightthroughEvaluator");
        playerTwo.setProperty("nodes", "StateAction");
        playerTwo.setProperty("selection", "UCB1Tuned");
        playerTwo.setProperty("exploration", "1");
        playerTwo.setProperty("movechoice", "MaxSamples");
        playerTwo.setProperty("randomized", "false");
        playerTwo.initialize();

        Board refereeBoard = game.newBoard();
        Player currentPlayer = playerOne;
        long totalRolloutsForBestMoves = 0;

        while(!refereeBoard.isTerminalBoard()) {
            Move move = currentPlayer.bestMove();
            if(currentPlayer instanceof BasicMCTS) {
                MCTS currentMCTSPlayer = (MCTS) currentPlayer;
                totalRolloutsForBestMoves += currentMCTSPlayer.getRoot().getValueEstimateOf(move).getSamples();
            }
            currentPlayer.acceptPlayMove(move);
            ((currentPlayer==playerOne) ? playerTwo : playerOne).acceptPlayMove(move);
            refereeBoard.play(move);
            currentPlayer = (currentPlayer==playerOne) ? playerTwo : playerOne;
        }
        System.out.println(refereeBoard);
        Board expectedResult = game.newBoard("expected test results\\testStateActionValueNodesStaticEvaluatorKnightthrough.txt", true);
        assertEquals(1763, totalRolloutsForBestMoves);
        assertEquals(Arrays.deepHashCode(((KnightthroughNoTranspositionsBoard) expectedResult).getBoard()),Arrays.deepHashCode(((KnightthroughNoTranspositionsBoard) refereeBoard).getBoard()));

    }

    @Test
    public void testStateActionValueNodesStaticEvaluatorOthello() throws UnknownPropertyException, NullMoveException {

        Util.DEBUG = false;
        Othello game = new Othello(8);

        MCTS playerOne = new BasicMCTS();
        playerOne.setGame(game);
        playerOne.setProperty("simulations", "1000");
        playerOne.setProperty("numberofnodes", "1100");
        playerOne.setProperty("evaluation", "games.OthelloEvaluator");
        playerOne.setProperty("nodes", "StateAction");
        playerOne.setProperty("selection", "UCB1Tuned");
        playerOne.setProperty("exploration", "1");
        playerOne.setProperty("movechoice", "MaxSamples");
        playerOne.setProperty("randomized", "false");
        playerOne.initialize();

        MCTS playerTwo = new BasicMCTS();
        playerTwo.setGame(game);
        playerTwo.setProperty("simulations", "1000");
        playerTwo.setProperty("numberofnodes", "1100");
        playerTwo.setProperty("evaluation", "games.OthelloEvaluator");
        playerTwo.setProperty("nodes", "StateAction");
        playerTwo.setProperty("selection", "UCB1Tuned");
        playerTwo.setProperty("exploration", "1");
        playerTwo.setProperty("movechoice", "MaxSamples");
        playerTwo.setProperty("randomized", "false");
        playerTwo.initialize();

        Board refereeBoard = game.newBoard();
        Player currentPlayer = playerOne;
        long totalRolloutsForBestMoves = 0;

        while(!refereeBoard.isTerminalBoard()) {
            Move move = currentPlayer.bestMove();
            if(currentPlayer instanceof BasicMCTS) {
                MCTS currentMCTSPlayer = (MCTS) currentPlayer;
                totalRolloutsForBestMoves += currentMCTSPlayer.getRoot().getValueEstimateOf(move).getSamples();
            }
            currentPlayer.acceptPlayMove(move);
            ((currentPlayer==playerOne) ? playerTwo : playerOne).acceptPlayMove(move);
            refereeBoard.play(move);
            currentPlayer = (currentPlayer==playerOne) ? playerTwo : playerOne;
        }
        Board expectedResult = game.newBoard("expected test results\\testStateActionValueNodesStaticEvaluatorOthello.txt", true);
        System.out.println(refereeBoard);
        assertEquals(12372, totalRolloutsForBestMoves);
        assertEquals(Arrays.deepHashCode(((OthelloNoTranspositionsBoard) expectedResult).getBoard()),Arrays.deepHashCode(((OthelloNoTranspositionsBoard) refereeBoard).getBoard()));

    }

    @Test
    public void testStateValueNodesRolloutEvaluatorAtariGo() throws UnknownPropertyException, NullMoveException {

        Util.DEBUG = false;
        AtariGo game = new AtariGo(8);

        MCTS playerOne = new BasicMCTS();
        playerOne.setGame(game);
        playerOne.setProperty("simulations", "100");
        playerOne.setProperty("numberofnodes", "110");
        playerOne.setProperty("evaluation", "ai.evaluation.RolloutEvaluator");
        playerOne.setProperty("nodes", "State");
        playerOne.setProperty("exploration", "1");
        playerOne.setProperty("movechoice", "MaxSamples");
        playerOne.setProperty("randomized", "false");
        playerOne.initialize();

        MCTS playerTwo = new BasicMCTS();
        playerTwo.setGame(game);
        playerTwo.setProperty("simulations", "100");
        playerTwo.setProperty("numberofnodes", "110");
        playerTwo.setProperty("evaluation", "ai.evaluation.RolloutEvaluator");
        playerTwo.setProperty("nodes", "State");
        playerTwo.setProperty("exploration", "1");
        playerTwo.setProperty("movechoice", "MaxSamples");
        playerTwo.setProperty("randomized", "false");
        playerTwo.initialize();

        Board refereeBoard = game.newBoard();
        Player currentPlayer = playerOne;
        long totalRolloutsForBestMoves = 0;

        while(!refereeBoard.isTerminalBoard()) {
            Move move = currentPlayer.bestMove();
            if(currentPlayer instanceof BasicMCTS) {
                MCTS currentMCTSPlayer = (MCTS) currentPlayer;
                totalRolloutsForBestMoves += currentMCTSPlayer.getRoot().getValueEstimateOf(move).getSamples();
            }
            currentPlayer.acceptPlayMove(move);
            ((currentPlayer==playerOne) ? playerTwo : playerOne).acceptPlayMove(move);
            refereeBoard.play(move);
            currentPlayer = (currentPlayer==playerOne) ? playerTwo : playerOne;
        }
        System.out.println(refereeBoard);
        Board expectedResult = game.newBoard("expected test results\\testStateValueNodesRolloutEvaluatorAtariGo.txt", true);
        assertEquals(132, totalRolloutsForBestMoves);
        assertEquals(Arrays.deepHashCode(((AtariGoNoTranspositionsBoard) expectedResult).getBoard()),Arrays.deepHashCode(((AtariGoNoTranspositionsBoard) refereeBoard).getBoard()));

    }

    @Test
    public void testStateValueNodesRolloutEvaluatorBreakthrough() throws UnknownPropertyException, NullMoveException {

        Util.DEBUG = false;
        Breakthrough game = new Breakthrough(8);

        MCTS playerOne = new BasicMCTS();
        playerOne.setGame(game);
        playerOne.setProperty("simulations", "1000");
        playerOne.setProperty("numberofnodes", "1100");
        playerOne.setProperty("evaluation", "ai.evaluation.RolloutEvaluator");
        playerOne.setProperty("nodes", "State");
        playerOne.setProperty("exploration", "1");
        playerOne.setProperty("movechoice", "MaxSamples");
        playerOne.setProperty("randomized", "false");
        playerOne.initialize();

        MCTS playerTwo = new BasicMCTS();
        playerTwo.setGame(game);
        playerTwo.setProperty("simulations", "1000");
        playerTwo.setProperty("numberofnodes", "1100");
        playerTwo.setProperty("evaluation", "ai.evaluation.RolloutEvaluator");
        playerTwo.setProperty("nodes", "State");
        playerTwo.setProperty("exploration", "1");
        playerTwo.setProperty("movechoice", "MaxSamples");
        playerTwo.setProperty("randomized", "false");
        playerTwo.initialize();

        Board refereeBoard = game.newBoard();
        Player currentPlayer = playerOne;
        long totalRolloutsForBestMoves = 0;

        while(!refereeBoard.isTerminalBoard()) {
            Move move = currentPlayer.bestMove();
            if(currentPlayer instanceof BasicMCTS) {
                MCTS currentMCTSPlayer = (MCTS) currentPlayer;
                totalRolloutsForBestMoves += currentMCTSPlayer.getRoot().getValueEstimateOf(move).getSamples();
            }
            currentPlayer.acceptPlayMove(move);
            ((currentPlayer==playerOne) ? playerTwo : playerOne).acceptPlayMove(move);
            refereeBoard.play(move);
            currentPlayer = (currentPlayer==playerOne) ? playerTwo : playerOne;
        }
        Board expectedResult = game.newBoard("expected test results\\testStateValueNodesRolloutEvaluatorBreakthrough.txt", true);
        System.out.println(refereeBoard);
        assertEquals(8616, totalRolloutsForBestMoves);
        assertEquals(Arrays.deepHashCode(((BreakthroughNoTranspositionsBoard) expectedResult).getBoard()),Arrays.deepHashCode(((BreakthroughNoTranspositionsBoard) refereeBoard).getBoard()));

    }

    @Test
    public void testStateValueNodesRolloutEvaluatorConnect4() throws UnknownPropertyException, NullMoveException {

        Util.DEBUG = false;
        Connect4 game = new Connect4(9);

        MCTS playerOne = new BasicMCTS();
        playerOne.setGame(game);
        playerOne.setProperty("simulations", "1000");
        playerOne.setProperty("numberofnodes", "1100");
        playerOne.setProperty("evaluation", "ai.evaluation.RolloutEvaluator");
        playerOne.setProperty("nodes", "State");
        playerOne.setProperty("exploration", "1");
        playerOne.setProperty("movechoice", "MaxSamples");
        playerOne.setProperty("randomized", "false");
        playerOne.initialize();

        MCTS playerTwo = new BasicMCTS();
        playerTwo.setGame(game);
        playerTwo.setProperty("simulations", "1000");
        playerTwo.setProperty("numberofnodes", "1100");
        playerTwo.setProperty("evaluation", "ai.evaluation.RolloutEvaluator");
        playerTwo.setProperty("nodes", "State");
        playerTwo.setProperty("exploration", "1");
        playerTwo.setProperty("movechoice", "MaxSamples");
        playerTwo.setProperty("randomized", "false");
        playerTwo.initialize();

        Board refereeBoard = game.newBoard();
        Player currentPlayer = playerOne;
        long totalRolloutsForBestMoves = 0;

        while(!refereeBoard.isTerminalBoard()) {
            Move move = currentPlayer.bestMove();
            if(currentPlayer instanceof BasicMCTS) {
                MCTS currentMCTSPlayer = (MCTS) currentPlayer;
                totalRolloutsForBestMoves += currentMCTSPlayer.getRoot().getValueEstimateOf(move).getSamples();
            }
            currentPlayer.acceptPlayMove(move);
            ((currentPlayer==playerOne) ? playerTwo : playerOne).acceptPlayMove(move);
            refereeBoard.play(move);
            currentPlayer = (currentPlayer==playerOne) ? playerTwo : playerOne;
        }
        Board expectedResult = game.newBoard("expected test results\\testStateValueNodesRolloutEvaluatorConnect4.txt", true);
        System.out.println(refereeBoard);
        assertEquals(15648, totalRolloutsForBestMoves);
        assertEquals(Arrays.deepHashCode(((Connect4NoTranspositionsBoard) expectedResult).getBoard()),Arrays.deepHashCode(((Connect4NoTranspositionsBoard) refereeBoard).getBoard()));

    }

    @Test
    public void testStateValueNodesRolloutEvaluatorDomineering() throws UnknownPropertyException, NullMoveException {

        Util.DEBUG = false;
        Domineering game = new Domineering();

        MCTS playerOne = new BasicMCTS();
        playerOne.setGame(game);
        playerOne.setProperty("simulations", "1000");
        playerOne.setProperty("numberofnodes", "1100");
        playerOne.setProperty("evaluation", "ai.evaluation.RolloutEvaluator");
        playerOne.setProperty("nodes", "State");
        playerOne.setProperty("exploration", "1");
        playerOne.setProperty("movechoice", "MaxSamples");
        playerOne.setProperty("randomized", "false");
        playerOne.initialize();

        MCTS playerTwo = new BasicMCTS();
        playerTwo.setGame(game);
        playerTwo.setProperty("simulations", "1000");
        playerTwo.setProperty("numberofnodes", "1100");
        playerTwo.setProperty("evaluation", "ai.evaluation.RolloutEvaluator");
        playerTwo.setProperty("nodes", "State");
        playerTwo.setProperty("exploration", "1");
        playerTwo.setProperty("movechoice", "MaxSamples");
        playerTwo.setProperty("randomized", "false");
        playerTwo.initialize();

        Board refereeBoard = game.newBoard();
        Player currentPlayer = playerOne;
        long totalRolloutsForBestMoves = 0;

        while(!refereeBoard.isTerminalBoard()) {
            Move move = currentPlayer.bestMove();
            if(currentPlayer instanceof BasicMCTS) {
                MCTS currentMCTSPlayer = (MCTS) currentPlayer;
                totalRolloutsForBestMoves += currentMCTSPlayer.getRoot().getValueEstimateOf(move).getSamples();
            }
            currentPlayer.acceptPlayMove(move);
            ((currentPlayer==playerOne) ? playerTwo : playerOne).acceptPlayMove(move);
            refereeBoard.play(move);
            currentPlayer = (currentPlayer==playerOne) ? playerTwo : playerOne;
        }
        System.out.println(refereeBoard);
        Board expectedResult = game.newBoard("expected test results\\testStateValueNodesRolloutEvaluatorDomineering.txt", true);
        assertEquals(10851, totalRolloutsForBestMoves);
        assertEquals(Arrays.deepHashCode(((DomineeringNoTranspositionsBoard) expectedResult).getBoard()),Arrays.deepHashCode(((DomineeringNoTranspositionsBoard) refereeBoard).getBoard()));

    }

    @Test
    public void testStateValueNodesRolloutEvaluatorKnightthrough() throws UnknownPropertyException, NullMoveException {

        Util.DEBUG = false;
        Knightthrough game = new Knightthrough(8);

        MCTS playerOne = new BasicMCTS();
        playerOne.setGame(game);
        playerOne.setProperty("simulations", "1000");
        playerOne.setProperty("numberofnodes", "1100");
        playerOne.setProperty("evaluation", "ai.evaluation.RolloutEvaluator");
        playerOne.setProperty("nodes", "State");
        playerOne.setProperty("exploration", "1");
        playerOne.setProperty("movechoice", "MaxSamples");
        playerOne.setProperty("randomized", "false");
        playerOne.initialize();

        MCTS playerTwo = new BasicMCTS();
        playerTwo.setGame(game);
        playerTwo.setProperty("simulations", "1000");
        playerTwo.setProperty("numberofnodes", "1100");
        playerTwo.setProperty("evaluation", "ai.evaluation.RolloutEvaluator");
        playerTwo.setProperty("nodes", "State");
        playerTwo.setProperty("exploration", "1");
        playerTwo.setProperty("movechoice", "MaxSamples");
        playerTwo.setProperty("randomized", "false");
        playerTwo.initialize();

        Board refereeBoard = game.newBoard();
        Player currentPlayer = playerOne;
        long totalRolloutsForBestMoves = 0;

        while(!refereeBoard.isTerminalBoard()) {
            Move move = currentPlayer.bestMove();
            if(currentPlayer instanceof BasicMCTS) {
                MCTS currentMCTSPlayer = (MCTS) currentPlayer;
                totalRolloutsForBestMoves += currentMCTSPlayer.getRoot().getValueEstimateOf(move).getSamples();
            }
            currentPlayer.acceptPlayMove(move);
            ((currentPlayer==playerOne) ? playerTwo : playerOne).acceptPlayMove(move);
            refereeBoard.play(move);
            currentPlayer = (currentPlayer==playerOne) ? playerTwo : playerOne;
        }
        System.out.println(refereeBoard);
        Board expectedResult = game.newBoard("expected test results\\testStateValueNodesRolloutEvaluatorKnightthrough.txt", true);
        assertEquals(2572, totalRolloutsForBestMoves);
        assertEquals(Arrays.deepHashCode(((KnightthroughNoTranspositionsBoard) expectedResult).getBoard()),Arrays.deepHashCode(((KnightthroughNoTranspositionsBoard) refereeBoard).getBoard()));

    }

    @Test
    public void testStateValueNodesRolloutEvaluatorNoGo() throws UnknownPropertyException, NullMoveException {

        Util.DEBUG = false;
        NoGo game = new NoGo(8);

        MCTS playerOne = new BasicMCTS();
        playerOne.setGame(game);
        playerOne.setProperty("simulations", "100");
        playerOne.setProperty("numberofnodes", "110");
        playerOne.setProperty("evaluation", "ai.evaluation.RolloutEvaluator");
        playerOne.setProperty("nodes", "State");
        playerOne.setProperty("exploration", "1");
        playerOne.setProperty("movechoice", "MaxSamples");
        playerOne.setProperty("randomized", "false");
        playerOne.initialize();

        MCTS playerTwo = new BasicMCTS();
        playerTwo.setGame(game);
        playerTwo.setProperty("simulations", "100");
        playerTwo.setProperty("numberofnodes", "110");
        playerTwo.setProperty("evaluation", "ai.evaluation.RolloutEvaluator");
        playerTwo.setProperty("nodes", "State");
        playerTwo.setProperty("exploration", "1");
        playerTwo.setProperty("movechoice", "MaxSamples");
        playerTwo.setProperty("randomized", "false");
        playerTwo.initialize();

        Board refereeBoard = game.newBoard();
        Player currentPlayer = playerOne;
        long totalRolloutsForBestMoves = 0;

        while(!refereeBoard.isTerminalBoard()) {
            Move move = currentPlayer.bestMove();
            if(currentPlayer instanceof BasicMCTS) {
                MCTS currentMCTSPlayer = (MCTS) currentPlayer;
                totalRolloutsForBestMoves += currentMCTSPlayer.getRoot().getValueEstimateOf(move).getSamples();
            }
            currentPlayer.acceptPlayMove(move);
            ((currentPlayer==playerOne) ? playerTwo : playerOne).acceptPlayMove(move);
            refereeBoard.play(move);
            currentPlayer = (currentPlayer==playerOne) ? playerTwo : playerOne;
        }
        System.out.println(refereeBoard);
        Board expectedResult = game.newBoard("expected test results\\testStateValueNodesRolloutEvaluatorNoGo.txt", true);
        assertEquals(969, totalRolloutsForBestMoves);
        assertEquals(Arrays.deepHashCode(((NoGoNoTranspositionsBoard) expectedResult).getBoard()),Arrays.deepHashCode(((NoGoNoTranspositionsBoard) refereeBoard).getBoard()));

    }

    @Test
    public void testStateValueNodesRolloutEvaluatorOthello() throws UnknownPropertyException, NullMoveException {

        Util.DEBUG = false;
        Othello game = new Othello(8);

        MCTS playerOne = new BasicMCTS();
        playerOne.setGame(game);
        playerOne.setProperty("simulations", "200");
        playerOne.setProperty("numberofnodes", "210");
        playerOne.setProperty("evaluation", "ai.evaluation.RolloutEvaluator");
        playerOne.setProperty("nodes", "State");
        playerOne.setProperty("exploration", "1");
        playerOne.setProperty("movechoice", "MaxSamples");
        playerOne.setProperty("randomized", "false");
        playerOne.initialize();

        MCTS playerTwo = new BasicMCTS();
        playerTwo.setGame(game);
        playerTwo.setProperty("simulations", "200");
        playerTwo.setProperty("numberofnodes", "210");
        playerTwo.setProperty("evaluation", "ai.evaluation.RolloutEvaluator");
        playerTwo.setProperty("nodes", "State");
        playerTwo.setProperty("exploration", "1");
        playerTwo.setProperty("movechoice", "MaxSamples");
        playerTwo.setProperty("randomized", "false");
        playerTwo.initialize();

        Board refereeBoard = game.newBoard();
        Player currentPlayer = playerOne;
        long totalRolloutsForBestMoves = 0;

        while(!refereeBoard.isTerminalBoard()) {
            Move move = currentPlayer.bestMove();
            if(currentPlayer instanceof BasicMCTS) {
                MCTS currentMCTSPlayer = (MCTS) currentPlayer;
                totalRolloutsForBestMoves += currentMCTSPlayer.getRoot().getValueEstimateOf(move).getSamples();
            }
            currentPlayer.acceptPlayMove(move);
            ((currentPlayer==playerOne) ? playerTwo : playerOne).acceptPlayMove(move);
            refereeBoard.play(move);
            currentPlayer = (currentPlayer==playerOne) ? playerTwo : playerOne;
        }
        Board expectedResult = game.newBoard("expected test results\\testStateValueNodesRolloutEvaluatorOthello.txt", true);
        System.out.println(refereeBoard);
        assertEquals(3871, totalRolloutsForBestMoves);
        assertEquals(Arrays.deepHashCode(((OthelloNoTranspositionsBoard) expectedResult).getBoard()),Arrays.deepHashCode(((OthelloNoTranspositionsBoard) refereeBoard).getBoard()));

    }

    @Test
    public void testStateValueNodesStaticEvaluatorBreakthrough() throws UnknownPropertyException, NullMoveException {

        Util.DEBUG = false;
        Breakthrough game = new Breakthrough(8);

        MCTS playerOne = new BasicMCTS();
        playerOne.setGame(game);
        playerOne.setProperty("simulations", "1000");
        playerOne.setProperty("numberofnodes", "1100");
        playerOne.setProperty("evaluation", "games.BreakthroughEvaluator");
        playerOne.setProperty("nodes", "State");
        playerOne.setProperty("exploration", "1");
        playerOne.setProperty("movechoice", "MaxSamples");
        playerOne.setProperty("randomized", "false");
        playerOne.initialize();

        MCTS playerTwo = new BasicMCTS();
        playerTwo.setGame(game);
        playerTwo.setProperty("simulations", "1000");
        playerTwo.setProperty("numberofnodes", "1100");
        playerTwo.setProperty("evaluation", "games.BreakthroughEvaluator");
        playerTwo.setProperty("nodes", "State");
        playerTwo.setProperty("exploration", "1");
        playerTwo.setProperty("movechoice", "MaxSamples");
        playerTwo.setProperty("randomized", "false");
        playerTwo.initialize();

        Board refereeBoard = game.newBoard();
        Player currentPlayer = playerOne;
        long totalRolloutsForBestMoves = 0;

        while(!refereeBoard.isTerminalBoard()) {
            Move move = currentPlayer.bestMove();
            if(currentPlayer instanceof BasicMCTS) {
                MCTS currentMCTSPlayer = (MCTS) currentPlayer;
                totalRolloutsForBestMoves += currentMCTSPlayer.getRoot().getValueEstimateOf(move).getSamples();
            }
            currentPlayer.acceptPlayMove(move);
            ((currentPlayer==playerOne) ? playerTwo : playerOne).acceptPlayMove(move);
            refereeBoard.play(move);
            currentPlayer = (currentPlayer==playerOne) ? playerTwo : playerOne;
        }
        Board expectedResult = game.newBoard("expected test results\\testStateValueNodesStaticEvaluatorBreakthrough.txt", true);
        System.out.println(refereeBoard);
        assertEquals(9807, totalRolloutsForBestMoves);
        assertEquals(Arrays.deepHashCode(((BreakthroughNoTranspositionsBoard) expectedResult).getBoard()),Arrays.deepHashCode(((BreakthroughNoTranspositionsBoard) refereeBoard).getBoard()));

    }

    @Test
    public void testStateValueNodesStaticEvaluatorConnect4() throws UnknownPropertyException, NullMoveException {

        Util.DEBUG = false;
        Connect4 game = new Connect4(9);

        MCTS playerOne = new BasicMCTS();
        playerOne.setGame(game);
        playerOne.setProperty("simulations", "1000");
        playerOne.setProperty("numberofnodes", "1100");
        playerOne.setProperty("evaluation", "games.Connect4BetterEvaluator");
        playerOne.setProperty("nodes", "State");
        playerOne.setProperty("exploration", "1");
        playerOne.setProperty("movechoice", "MaxSamples");
        playerOne.setProperty("randomized", "false");
        playerOne.initialize();

        MCTS playerTwo = new BasicMCTS();
        playerTwo.setGame(game);
        playerTwo.setProperty("simulations", "1000");
        playerTwo.setProperty("numberofnodes", "1100");
        playerTwo.setProperty("evaluation", "games.Connect4BetterEvaluator");
        playerTwo.setProperty("nodes", "State");
        playerTwo.setProperty("exploration", "1");
        playerTwo.setProperty("movechoice", "MaxSamples");
        playerTwo.setProperty("randomized", "false");
        playerTwo.initialize();

        Board refereeBoard = game.newBoard();
        Player currentPlayer = playerOne;
        long totalRolloutsForBestMoves = 0;

        while(!refereeBoard.isTerminalBoard()) {
            Move move = currentPlayer.bestMove();
            if(currentPlayer instanceof BasicMCTS) {
                MCTS currentMCTSPlayer = (MCTS) currentPlayer;
                totalRolloutsForBestMoves += currentMCTSPlayer.getRoot().getValueEstimateOf(move).getSamples();
            }
            currentPlayer.acceptPlayMove(move);
            ((currentPlayer==playerOne) ? playerTwo : playerOne).acceptPlayMove(move);
            refereeBoard.play(move);
            currentPlayer = (currentPlayer==playerOne) ? playerTwo : playerOne;
        }
        Board expectedResult = game.newBoard("expected test results\\testStateValueNodesStaticEvaluatorConnect4.txt", true);
        System.out.println(refereeBoard);
        assertEquals(27027, totalRolloutsForBestMoves);
        assertEquals(Arrays.deepHashCode(((Connect4NoTranspositionsBoard) expectedResult).getBoard()),Arrays.deepHashCode(((Connect4NoTranspositionsBoard) refereeBoard).getBoard()));

    }

    @Test
    public void testStateValueNodesStaticEvaluatorKnightthrough() throws UnknownPropertyException, NullMoveException {

        Util.DEBUG = false;
        Knightthrough game = new Knightthrough(8);

        MCTS playerOne = new BasicMCTS();
        playerOne.setGame(game);
        playerOne.setProperty("simulations", "1000");
        playerOne.setProperty("numberofnodes", "1100");
        playerOne.setProperty("evaluation", "games.KnightthroughEvaluator");
        playerOne.setProperty("nodes", "State");
        playerOne.setProperty("exploration", "1");
        playerOne.setProperty("movechoice", "MaxSamples");
        playerOne.setProperty("randomized", "false");
        playerOne.initialize();

        MCTS playerTwo = new BasicMCTS();
        playerTwo.setGame(game);
        playerTwo.setProperty("simulations", "1000");
        playerTwo.setProperty("numberofnodes", "1100");
        playerTwo.setProperty("evaluation", "games.KnightthroughEvaluator");
        playerTwo.setProperty("nodes", "State");
        playerTwo.setProperty("exploration", "1");
        playerTwo.setProperty("movechoice", "MaxSamples");
        playerTwo.setProperty("randomized", "false");
        playerTwo.initialize();

        Board refereeBoard = game.newBoard();
        Player currentPlayer = playerOne;
        long totalRolloutsForBestMoves = 0;

        while(!refereeBoard.isTerminalBoard()) {
            Move move = currentPlayer.bestMove();
            if(currentPlayer instanceof BasicMCTS) {
                MCTS currentMCTSPlayer = (MCTS) currentPlayer;
                totalRolloutsForBestMoves += currentMCTSPlayer.getRoot().getValueEstimateOf(move).getSamples();
            }
            currentPlayer.acceptPlayMove(move);
            ((currentPlayer==playerOne) ? playerTwo : playerOne).acceptPlayMove(move);
            refereeBoard.play(move);
            currentPlayer = (currentPlayer==playerOne) ? playerTwo : playerOne;
        }
        System.out.println(refereeBoard);
        Board expectedResult = game.newBoard("expected test results\\testStateValueNodesStaticEvaluatorKnightthrough.txt", true);
        assertEquals(1763, totalRolloutsForBestMoves);
        assertEquals(Arrays.deepHashCode(((KnightthroughNoTranspositionsBoard) expectedResult).getBoard()),Arrays.deepHashCode(((KnightthroughNoTranspositionsBoard) refereeBoard).getBoard()));

    }

    @Test
    public void testStateValueNodesStaticEvaluatorOthello() throws UnknownPropertyException, NullMoveException {

        Util.DEBUG = false;
        Othello game = new Othello(8);

        MCTS playerOne = new BasicMCTS();
        playerOne.setGame(game);
        playerOne.setProperty("simulations", "1000");
        playerOne.setProperty("numberofnodes", "1100");
        playerOne.setProperty("evaluation", "games.OthelloEvaluator");
        playerOne.setProperty("nodes", "State");
        playerOne.setProperty("exploration", "1");
        playerOne.setProperty("movechoice", "MaxSamples");
        playerOne.setProperty("randomized", "false");
        playerOne.initialize();

        MCTS playerTwo = new BasicMCTS();
        playerTwo.setGame(game);
        playerTwo.setProperty("simulations", "1000");
        playerTwo.setProperty("numberofnodes", "1100");
        playerTwo.setProperty("evaluation", "games.OthelloEvaluator");
        playerTwo.setProperty("nodes", "State");
        playerTwo.setProperty("exploration", "1");
        playerTwo.setProperty("movechoice", "MaxSamples");
        playerTwo.setProperty("randomized", "false");
        playerTwo.initialize();

        Board refereeBoard = game.newBoard();
        Player currentPlayer = playerOne;
        long totalRolloutsForBestMoves = 0;

        while(!refereeBoard.isTerminalBoard()) {
            Move move = currentPlayer.bestMove();
            if(currentPlayer instanceof BasicMCTS) {
                MCTS currentMCTSPlayer = (MCTS) currentPlayer;
                totalRolloutsForBestMoves += currentMCTSPlayer.getRoot().getValueEstimateOf(move).getSamples();
            }
            currentPlayer.acceptPlayMove(move);
            ((currentPlayer==playerOne) ? playerTwo : playerOne).acceptPlayMove(move);
            refereeBoard.play(move);
            currentPlayer = (currentPlayer==playerOne) ? playerTwo : playerOne;
        }
        Board expectedResult = game.newBoard("expected test results\\testStateValueNodesStaticEvaluatorOthello.txt", true);
        System.out.println(refereeBoard);
        assertEquals(12372, totalRolloutsForBestMoves);
        assertEquals(Arrays.deepHashCode(((OthelloNoTranspositionsBoard) expectedResult).getBoard()),Arrays.deepHashCode(((OthelloNoTranspositionsBoard) refereeBoard).getBoard()));

    }


}
