package ai;

import ai.nodes.SearchNode;
import utils.Util;

import java.util.ArrayList;
import java.util.List;

//during selection and potentially rollout phase, collects all information from a single simulation that will be needed in the backpropagation phase for updating the MCTS tree (and potentially other data structures)
public class SimulationLog {

    List<SimulationStep> log;

    public SimulationLog() {
        log = new ArrayList<>();
    }

    //called whenever a move is made from a given board, both in selection and rollout phases. updates all relevant data structures for this log.
    //if a board is traversed without making any further move (at terminal states or when applying a StaticEvaluator to the state), set the move parameter to null
    public void add(Board board, SearchNode node, Move move) {
        if(Util.isDebug()) System.out.println("added to simulation log: move "+move+" at node "+(node!=null?node.getHash():"null"));
        log.add(new SimulationStep(board.getHash(), node, move));
    }

    //clears this log for the next simulation
    public void clear() {
        log.clear();
    }

    public int getLengthOfSimulation() {
        return log.size();
    }

    public Long getBoardHashAtStep(int t) {
        return log.get(t).boardHash;
    }

    public boolean inTreeInStep(int t) {
        return log.get(t).node!=null;
    }

    public Move getMoveAtStep(int t) {
        return log.get(t).move;
    }

    public SearchNode getNodeAtStep(int t) { return log.get(t).node; }

    private class SimulationStep {

        private final Move move;
        private final long boardHash;
        private final SearchNode node;

        public SimulationStep(long boardHash, SearchNode node, Move move) {
            this.boardHash = boardHash;
            this.node = node;
            this.move = move;
        }

    }

}
