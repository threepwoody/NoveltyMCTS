package ai.selection;

import ai.*;
import ai.movepruning.MovePruner;
import ai.movepruning.NoPruning;
import ai.nodes.SearchNode;

import java.util.List;
import java.util.Random;

public abstract class PruningSelectionPolicy implements SelectionPolicy {

    private MovePruner movePruner = new NoPruning();

    public MovePruner getMovePruner() {
        return movePruner;
    }

    public void setMovePruner(MovePruner movePruner) {
        this.movePruner = movePruner;
    }

    public abstract Move selectFromPrunedMoves(List<Move> prunedMoves, SearchNode node, Board board, Random random, SimulationLog log, boolean randomizeOrder) throws NullMoveException;

    @Override
    public Move selectMove(SearchNode node, Board board, Random random, SimulationLog log) throws NullMoveException {
        List<Move> legalMoves = movePruner.getPrunedMoves(node, board);
        return selectFromPrunedMoves(legalMoves, node, board, random, log, movePruner.randomizeAfterPruning());
    }

    @Override
    public void initializeSearch(MCTS mcts) {
        movePruner.initializeSearch(mcts);
    }

}
