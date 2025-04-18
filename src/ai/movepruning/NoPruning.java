package ai.movepruning;

import ai.Board;
import ai.Move;
import ai.nodes.SearchNode;

import java.util.List;

public class NoPruning implements MovePruner {

    @Override
    public List<Move> getPrunedMoves(SearchNode node, Board board) {
        return board.getLegalMoves();
    }

    @Override
    public boolean randomizeAfterPruning() {
        return true;
    }

}
