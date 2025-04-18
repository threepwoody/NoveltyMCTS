package ai.policies;

import ai.*;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MoveSorterPolicy implements Policy {

    MoveSorter moveSorter;
    private double epsilon = 0.05;

    public MoveSorterPolicy(MoveSorter moveSorter) {
        this.moveSorter = moveSorter;
    }

    @Override
    public Move selectMove(Game game, Board board, Random random, SimulationLog log) {
        List<Move> moveCandidates = board.getLegalMoves();
        if(random.nextDouble()<epsilon) {
            int randomIndex = random.nextInt(moveCandidates.size());
            Move randomMove = moveCandidates.get(randomIndex);
            return randomMove;
        }
        Collections.shuffle(moveCandidates, random);
        moveCandidates = moveSorter.sortMoves(moveCandidates, board);
        return moveCandidates.get(0);
    }

}
