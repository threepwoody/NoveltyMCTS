package ai.policies;

import ai.Board;
import ai.Game;
import ai.Move;
import ai.SimulationLog;

import java.util.List;
import java.util.Random;

public class DecisiveMovesPolicy implements Policy {

    @Override
    public Move selectMove(Game game, Board board, Random random, SimulationLog log) {
        List<Move> legalMoves = board.getLegalMoves();
        Board testBoard = game.newBoard();
        for(Move legalMove : legalMoves) {
            testBoard.copyDataFrom(board);
            testBoard.play(legalMove);
            if(testBoard.isTerminalBoard() && testBoard.evalOfTerminalBoard().getValueForColor(board.getColorToPlay())==1) {
                return legalMove;
            }
        }
        int randomIndex = random.nextInt(legalMoves.size());
        Move randomMove = legalMoves.get(randomIndex);
        return randomMove;
    }

    @Override
    public String toString() {
        return "DecisiveMovesPolicy";
    }

}
