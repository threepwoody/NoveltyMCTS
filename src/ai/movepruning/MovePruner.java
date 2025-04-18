package ai.movepruning;

import ai.Board;
import ai.MCTS;
import ai.Move;
import ai.nodes.SearchNode;

import java.util.List;

public interface MovePruner {

    List<Move> getPrunedMoves(SearchNode node, Board board);

    default void initializeSearch(MCTS mcts) {}

    boolean randomizeAfterPruning();

}
