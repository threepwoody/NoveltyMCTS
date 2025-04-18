package tests;

import ai.*;
import games.*;
import org.junit.Test;
import experiments.AlphaZero.StateNetworkPolicyOutputPlayer;
import experiments.AlphaZero.StateNetworkValueOutputPlayer;
import utils.UnknownPropertyException;
import utils.Util;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class NetworkPlayerTest {

    @Test
    public void testStateNetworkPolicyOutputPlayerDomineering() throws UnknownPropertyException, NullMoveException {

        Util.DEBUG = false;
        Domineering game = new Domineering(8);

        StateNetworkPolicyOutputPlayer playerOne = new StateNetworkPolicyOutputPlayer();
        playerOne.setGame(game);
        playerOne.setProperty("evaluation", "expected test results\\network_domineering_8x8.param");
        playerOne.initialize();

        StateNetworkPolicyOutputPlayer playerTwo = new StateNetworkPolicyOutputPlayer();
        playerTwo.setGame(game);
        playerTwo.setProperty("evaluation", "expected test results\\network_domineering_8x8.param");
        playerTwo.initialize();

        Board refereeBoard = game.newBoard();
        Player currentPlayer = playerOne;

        while(!refereeBoard.isTerminalBoard()) {
            Move move = currentPlayer.bestMove();
            currentPlayer.acceptPlayMove(move);
            ((currentPlayer==playerOne) ? playerTwo : playerOne).acceptPlayMove(move);
            refereeBoard.play(move);
            currentPlayer = (currentPlayer==playerOne) ? playerTwo : playerOne;
        }
        Board expectedResult = game.newBoard("expected test results\\testStateNetworkPolicyOutputPlayerDomineering.txt", true);
        System.out.println(refereeBoard);
        assertEquals(Arrays.deepHashCode(((DomineeringNoTranspositionsBoard) expectedResult).getBoard()),Arrays.deepHashCode(((DomineeringNoTranspositionsBoard) refereeBoard).getBoard()));

    }

    @Test
    public void testStateNetworkPolicyOutputPlayerNoGo() throws UnknownPropertyException, NullMoveException {

        Util.DEBUG = false;
        NoGo game = new NoGo(7);

        StateNetworkPolicyOutputPlayer playerOne = new StateNetworkPolicyOutputPlayer();
        playerOne.setGame(game);
        playerOne.setProperty("evaluation", "expected test results\\network_nogo_7x7.param");
        playerOne.initialize();

        StateNetworkPolicyOutputPlayer playerTwo = new StateNetworkPolicyOutputPlayer();
        playerTwo.setGame(game);
        playerTwo.setProperty("evaluation", "expected test results\\network_nogo_7x7.param");
        playerTwo.initialize();

        Board refereeBoard = game.newBoard();
        Player currentPlayer = playerOne;

        while(!refereeBoard.isTerminalBoard()) {
            Move move = currentPlayer.bestMove();
            currentPlayer.acceptPlayMove(move);
            ((currentPlayer==playerOne) ? playerTwo : playerOne).acceptPlayMove(move);
            refereeBoard.play(move);
            currentPlayer = (currentPlayer==playerOne) ? playerTwo : playerOne;
        }
        Board expectedResult = game.newBoard("expected test results\\testStateNetworkPolicyOutputPlayerNoGo.txt", true);
        System.out.println(refereeBoard);
        assertEquals(Arrays.deepHashCode(((NoGoNoTranspositionsBoard) expectedResult).getBoard()),Arrays.deepHashCode(((NoGoNoTranspositionsBoard) refereeBoard).getBoard()));

    }

    @Test
    public void testStateNetworkPolicyOutputPlayerKnightthrough() throws UnknownPropertyException, NullMoveException {

        Util.DEBUG = false;
        Knightthrough game = new Knightthrough(8);

        StateNetworkPolicyOutputPlayer playerOne = new StateNetworkPolicyOutputPlayer();
        playerOne.setGame(game);
        playerOne.setProperty("evaluation", "expected test results\\network_knightthrough_8x8.param");
        playerOne.initialize();

        StateNetworkPolicyOutputPlayer playerTwo = new StateNetworkPolicyOutputPlayer();
        playerTwo.setGame(game);
        playerTwo.setProperty("evaluation", "expected test results\\network_knightthrough_8x8.param");
        playerTwo.initialize();

        Board refereeBoard = game.newBoard();
        Player currentPlayer = playerOne;

        while(!refereeBoard.isTerminalBoard()) {
            Move move = currentPlayer.bestMove();
            currentPlayer.acceptPlayMove(move);
            ((currentPlayer==playerOne) ? playerTwo : playerOne).acceptPlayMove(move);
            refereeBoard.play(move);
            currentPlayer = (currentPlayer==playerOne) ? playerTwo : playerOne;
        }
        Board expectedResult = game.newBoard("expected test results\\testStateNetworkPolicyOutputPlayerKnightthrough.txt", true);
        System.out.println(refereeBoard);
        assertEquals(Arrays.deepHashCode(((KnightthroughNoTranspositionsBoard) expectedResult).getBoard()),Arrays.deepHashCode(((KnightthroughNoTranspositionsBoard) refereeBoard).getBoard()));

    }

    @Test
    public void testStateNetworkValueOutputPlayerDomineering() throws UnknownPropertyException, NullMoveException {

        Util.DEBUG = false;
        Domineering game = new Domineering(8);

        StateNetworkValueOutputPlayer playerOne = new StateNetworkValueOutputPlayer();
        playerOne.setGame(game);
        playerOne.setProperty("evaluation", "expected test results\\network_domineering_8x8.param");
        playerOne.initialize();

        StateNetworkValueOutputPlayer playerTwo = new StateNetworkValueOutputPlayer();
        playerTwo.setGame(game);
        playerTwo.setProperty("evaluation", "expected test results\\network_domineering_8x8.param");
        playerTwo.initialize();

        Board refereeBoard = game.newBoard();
        Player currentPlayer = playerOne;

        while(!refereeBoard.isTerminalBoard()) {
            Move move = currentPlayer.bestMove();
            currentPlayer.acceptPlayMove(move);
            ((currentPlayer==playerOne) ? playerTwo : playerOne).acceptPlayMove(move);
            refereeBoard.play(move);
            currentPlayer = (currentPlayer==playerOne) ? playerTwo : playerOne;
        }
        Board expectedResult = game.newBoard("expected test results\\testStateNetworkValueOutputPlayerDomineering.txt", true);
        System.out.println(refereeBoard);
        assertEquals(Arrays.deepHashCode(((DomineeringNoTranspositionsBoard) expectedResult).getBoard()),Arrays.deepHashCode(((DomineeringNoTranspositionsBoard) refereeBoard).getBoard()));

    }

    @Test
    public void testStateNetworkValueOutputPlayerNoGo() throws UnknownPropertyException, NullMoveException {

        Util.DEBUG = false;
        NoGo game = new NoGo(7);

        StateNetworkValueOutputPlayer playerOne = new StateNetworkValueOutputPlayer();
        playerOne.setGame(game);
        playerOne.setProperty("evaluation", "expected test results\\network_nogo_7x7.param");
        playerOne.initialize();

        StateNetworkValueOutputPlayer playerTwo = new StateNetworkValueOutputPlayer();
        playerTwo.setGame(game);
        playerTwo.setProperty("evaluation", "expected test results\\network_nogo_7x7.param");
        playerTwo.initialize();

        Board refereeBoard = game.newBoard();
        Player currentPlayer = playerOne;

        while(!refereeBoard.isTerminalBoard()) {
            Move move = currentPlayer.bestMove();
            currentPlayer.acceptPlayMove(move);
            ((currentPlayer==playerOne) ? playerTwo : playerOne).acceptPlayMove(move);
            refereeBoard.play(move);
            currentPlayer = (currentPlayer==playerOne) ? playerTwo : playerOne;
        }
        Board expectedResult = game.newBoard("expected test results\\testStateNetworkValueOutputPlayerNoGo.txt", true);
        System.out.println(refereeBoard);
        assertEquals(Arrays.deepHashCode(((NoGoNoTranspositionsBoard) expectedResult).getBoard()),Arrays.deepHashCode(((NoGoNoTranspositionsBoard) refereeBoard).getBoard()));

    }

    @Test
    public void testStateNetworkValueOutputPlayerKnightthrough() throws UnknownPropertyException, NullMoveException {

        Util.DEBUG = false;
        Knightthrough game = new Knightthrough(8);

        StateNetworkValueOutputPlayer playerOne = new StateNetworkValueOutputPlayer();
        playerOne.setGame(game);
        playerOne.setProperty("evaluation", "expected test results\\network_knightthrough_8x8.param");
        playerOne.initialize();

        StateNetworkValueOutputPlayer playerTwo = new StateNetworkValueOutputPlayer();
        playerTwo.setGame(game);
        playerTwo.setProperty("evaluation", "expected test results\\network_knightthrough_8x8.param");
        playerTwo.initialize();

        Board refereeBoard = game.newBoard();
        Player currentPlayer = playerOne;

        while(!refereeBoard.isTerminalBoard()) {
            Move move = currentPlayer.bestMove();
            currentPlayer.acceptPlayMove(move);
            ((currentPlayer==playerOne) ? playerTwo : playerOne).acceptPlayMove(move);
            refereeBoard.play(move);
            currentPlayer = (currentPlayer==playerOne) ? playerTwo : playerOne;
        }
        Board expectedResult = game.newBoard("expected test results\\testStateNetworkValueOutputPlayerKnightthrough.txt", true);
        System.out.println(refereeBoard);
        assertEquals(Arrays.deepHashCode(((KnightthroughNoTranspositionsBoard) expectedResult).getBoard()),Arrays.deepHashCode(((KnightthroughNoTranspositionsBoard) refereeBoard).getBoard()));

    }

}
