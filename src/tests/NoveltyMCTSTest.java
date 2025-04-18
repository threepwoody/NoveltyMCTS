package tests;

import ai.*;
import games.*;
import org.junit.Test;
import utils.UnknownPropertyException;
import utils.Util;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class NoveltyMCTSTest {

    @Test
    public void testRawStatePseudocountNoveltyConnect4() throws UnknownPropertyException, NullMoveException {

        Game game = new Connect4(9);

        MCTS playerOne = new BasicMCTS();
        playerOne.setGame(game);
        playerOne.setProperty("noveltymcts", "true");
        playerOne.setProperty("simulations", "1000");
        playerOne.setProperty("numberofnodes", "1100");
        playerOne.setProperty("evaluation", "static");
        playerOne.setProperty("noveltyfunction", "RawStatePseudocountNovelty");
        playerOne.setProperty("exploration", "0.4");
        playerOne.setProperty("noveltybeta", "0.01");
        playerOne.setProperty("noveltyweight", "2");
        playerOne.setProperty("randomized", "false");
        playerOne.setProperty("movechoice", "MaxValue");
        playerOne.initialize();

        MCTS playerTwo = new BasicMCTS();
        playerTwo.setGame(game);
        playerTwo.setProperty("noveltymcts", "true");
        playerTwo.setProperty("simulations", "1000");
        playerTwo.setProperty("numberofnodes", "1100");
        playerTwo.setProperty("evaluation", "static");
        playerTwo.setProperty("noveltyfunction", "RawStatePseudocountNovelty");
        playerTwo.setProperty("exploration", "0.4");
        playerTwo.setProperty("noveltybeta", "0.01");
        playerTwo.setProperty("noveltyweight", "2");
        playerTwo.setProperty("randomized", "false");
        playerTwo.setProperty("movechoice", "MaxValue");
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
        Board expectedResult = game.newBoard("expected test results\\testRawStatePseudocountNoveltyConnect4.txt", true);
        System.out.println(refereeBoard);
        assertEquals(39074, totalRolloutsForBestMoves);
        assertEquals(Arrays.deepHashCode(((Connect4NoTranspositionsBoard) expectedResult).getBoard()),Arrays.deepHashCode(((Connect4NoTranspositionsBoard) refereeBoard).getBoard()));

    }

    @Test
    public void testRawStateThresholdNoveltyKnightthrough() throws UnknownPropertyException, NullMoveException {

        Game game = new Knightthrough(8);

        MCTS playerOne = new BasicMCTS();
        playerOne.setGame(game);
        playerOne.setProperty("noveltymcts", "true");
        playerOne.setProperty("simulations", "1000");
        playerOne.setProperty("numberofnodes", "1100");
        playerOne.setProperty("evaluation", "static");
        playerOne.setProperty("noveltyfunction", "RawStateThresholdNovelty");
        playerOne.setProperty("noveltybeta", "0.003");
        playerOne.setProperty("noveltyfrequencylimit", "0.02");
        playerOne.setProperty("noveltyweight", "0.2");
        playerOne.setProperty("exploration", "0.2");
        playerOne.setProperty("randomized", "false");
        playerOne.setProperty("movechoice", "MaxSamples");
        playerOne.initialize();

        MCTS playerTwo = new BasicMCTS();
        playerTwo.setGame(game);
        playerTwo.setProperty("noveltymcts", "true");
        playerTwo.setProperty("simulations", "1000");
        playerTwo.setProperty("numberofnodes", "1100");
        playerTwo.setProperty("evaluation", "static");
        playerTwo.setProperty("noveltyfunction", "RawStateThresholdNovelty");
        playerTwo.setProperty("noveltybeta", "0.003");
        playerTwo.setProperty("noveltyfrequencylimit", "0.02");
        playerTwo.setProperty("noveltyweight", "0.2");
        playerTwo.setProperty("exploration", "0.2");
        playerTwo.setProperty("randomized", "false");
        playerTwo.setProperty("movechoice", "MaxSamples");
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
        Board expectedResult = game.newBoard("expected test results\\testRawStateThresholdNoveltyKnightthrough.txt", true);
        System.out.println(refereeBoard);
        assertEquals(5314, totalRolloutsForBestMoves);
        assertEquals(Arrays.deepHashCode(((KnightthroughNoTranspositionsBoard) expectedResult).getBoard()),Arrays.deepHashCode(((KnightthroughNoTranspositionsBoard) refereeBoard).getBoard()));

    }

    @Test
    public void testRawStateThresholdNoveltyPUCTAtariGo() throws UnknownPropertyException, NullMoveException {

        Game game = new AtariGo(7);

        MCTS playerOne = new BasicMCTS();
        playerOne.setGame(game);
        playerOne.setProperty("noveltypuct", "true");
        playerOne.setProperty("simulations", "250");
        playerOne.setProperty("numberofnodes", "600");
        playerOne.setProperty("evaluation", "network_7x7_atarigo.param");
        playerOne.setProperty("noveltyfunction", "RawStateThresholdNovelty");
        playerOne.setProperty("noveltybeta", "0.2");
        playerOne.setProperty("noveltyfrequencylimit", "0.2");
        playerOne.setProperty("noveltyweight", "0.2");
        playerOne.setProperty("exploration", "0.2");
        playerOne.setProperty("randomized", "false");
        playerOne.setProperty("movechoice", "MaxSamples");
        playerOne.initialize();

        MCTS playerTwo = new BasicMCTS();
        playerTwo.setGame(game);
        playerTwo.setProperty("noveltypuct", "true");
        playerTwo.setProperty("simulations", "250");
        playerTwo.setProperty("numberofnodes", "600");
        playerTwo.setProperty("evaluation", "network_7x7_atarigo.param");
        playerTwo.setProperty("noveltyfunction", "RawStateThresholdNovelty");
        playerTwo.setProperty("noveltybeta", "0.2");
        playerTwo.setProperty("noveltyfrequencylimit", "0.2");
        playerTwo.setProperty("noveltyweight", "0.2");
        playerTwo.setProperty("exploration", "0.2");
        playerTwo.setProperty("randomized", "false");
        playerTwo.setProperty("movechoice", "MaxSamples");
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
        Board expectedResult = game.newBoard("expected test results\\testRawStateThresholdNoveltyPUCTAtariGo.txt", true);
        System.out.println(refereeBoard);
        assertEquals(5328, totalRolloutsForBestMoves);
        assertEquals(Arrays.deepHashCode(((AtariGoNoTranspositionsBoard) expectedResult).getBoard()),Arrays.deepHashCode(((AtariGoNoTranspositionsBoard) refereeBoard).getBoard()));

    }

    @Test
    public void testBreakthroughFeatureBasedPseudocountNoveltyEvaluator() throws UnknownPropertyException, NullMoveException {

        Util.DEBUG = false;
        Game game = new Breakthrough(8);

        MCTS playerOne = new BasicMCTS();
        playerOne.setGame(game);
        playerOne.setProperty("noveltymcts", "true");
        playerOne.setProperty("noveltyfunction", "BreakthroughFeatureBasedPseudocountNoveltyEvaluator");
        playerOne.setProperty("simulations", "1000");
        playerOne.setProperty("numberofnodes", "1100");
        playerOne.setProperty("exploration", "0.1");
        playerOne.setProperty("noveltybeta", "0.01");
        playerOne.setProperty("noveltyweight", "0.01");
        playerOne.setProperty("randomized", "false");
        playerOne.setProperty("movechoice", "MaxValue");
        playerOne.initialize();

        MCTS playerTwo = new BasicMCTS();
        playerTwo.setGame(game);
        playerTwo.setProperty("noveltymcts", "true");
        playerTwo.setProperty("noveltyfunction", "BreakthroughFeatureBasedPseudocountNoveltyEvaluator");
        playerTwo.setProperty("simulations", "1000");
        playerTwo.setProperty("numberofnodes", "1100");
        playerTwo.setProperty("exploration", "0.1");
        playerTwo.setProperty("noveltybeta", "0.01");
        playerTwo.setProperty("noveltyweight", "0.01");
        playerTwo.setProperty("randomized", "false");
        playerTwo.setProperty("movechoice", "MaxValue");
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
        Board expectedResult = game.newBoard("expected test results\\testBreakthroughFeatureBasedPseudocountNoveltyEvaluator.txt", true);
        System.out.println(refereeBoard);
        assertEquals(10109, totalRolloutsForBestMoves);
        assertEquals(Arrays.deepHashCode(((BreakthroughNoTranspositionsBoard) expectedResult).getBoard()),Arrays.deepHashCode(((BreakthroughNoTranspositionsBoard) refereeBoard).getBoard()));

    }

}
