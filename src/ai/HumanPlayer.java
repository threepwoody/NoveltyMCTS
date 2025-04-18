package ai;

import utils.MalformedMoveException;
import utils.UnknownPropertyException;

import java.util.Scanner;

public class HumanPlayer implements Player {

    private Board board;
    private Game game;
    private Scanner user_input;

    public HumanPlayer() {
        user_input = new Scanner(System.in);
    }

    @Override
    public void acceptPlayMove(Move move) {
        board.play(move);
    }

    @Override
    public Move bestMove() {
        Move move = null;
        while(true) {
            System.out.print("Enter your move: ");
            try {
                move = board.toMove(user_input.next());
            } catch (MalformedMoveException e) {
                e.printStackTrace();
            }
            if(board.isLegalMove(move)) {
                break;
            } else {
                System.out.println("Not a legal move.");
            }
        }
        return move;
    }

    @Override
    public void setProperty(String property, String value) throws UnknownPropertyException {
        if (property.equals("game")) {
            Game prototype = null;
            if (!value.startsWith("games.")) {
                value = "games." + value;
            }
            try {
                prototype = (Game) Class.forName(value).getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            setGame(prototype);
        } else {
            throw new UnknownPropertyException(property + " is not a known property");
        }
    }

    @Override
    public void initialize() {
    }

    @Override
    public Board getBoard() {
        return board;
    }

    @Override
    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        board = game.newBoard();
        this.game = game;
    }

}
