package ai.selection;

import ai.*;
import ai.nodes.SearchNode;

import java.util.Random;

public interface SelectionPolicy {

    default void initializeSearch(MCTS mcts) {}

    /** Returns the best move to make from here during a playout (in the tree). */
    Move selectMove(SearchNode node, Board board, Random random, SimulationLog log) throws NullMoveException;

}
