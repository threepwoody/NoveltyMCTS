package ai.policies;

import ai.Board;
import ai.Game;
import ai.Move;
import ai.SimulationLog;

import java.util.List;
import java.util.Random;

public class RandomPolicy implements Policy {

    @Override
    public Move selectMove(Game game, Board board, Random random, SimulationLog log) {
        List<Move> legalMoves = board.getLegalMoves();
        int randomIndex = random.nextInt(legalMoves.size());
        Move randomMove = legalMoves.get(randomIndex);
        return randomMove;
    }

    @Override
    public String toString() {
        return "RandomPolicy";
    }

}
