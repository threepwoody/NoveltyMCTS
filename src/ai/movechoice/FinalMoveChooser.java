package ai.movechoice;

import ai.Move;
import ai.nodes.SearchNode;

import java.util.List;
import java.util.Random;

public abstract class FinalMoveChooser {

    private double randomMoveProbability = 0;

    public void setRandomMoveProbability(double randomMoveProbability) {
        this.randomMoveProbability = randomMoveProbability;
    }

    public Move selectMove(SearchNode node, Random random) {
        if(randomMoveProbability>0) {
            if(random.nextDouble()<randomMoveProbability) {
                List<Move> moves =  node.getExpandedMoves();
                return moves.get(random.nextInt(moves.size()));
            }
        }
        return selectMoveWithExploration(node, random);
    }

    abstract protected Move selectMoveWithExploration(SearchNode node, Random random);

    public Move selectGreedyMove(SearchNode node, Random random) {
        return selectMoveWithExploration(node, random);
    }

}
