package ai.backprop;

import ai.Evaluation;
import ai.Move;
import ai.SimulationLog;
import ai.nodes.SearchNode;
import utils.Util;

//works for both 1- and 2-player games
public class DefaultBackpropagator implements Backpropagator {

    @Override
    public void backpropagate(Evaluation evaluation, SimulationLog log) {
        if (Util.isDebug()) {
            System.out.println("backpropagation phase, DefaultBackpropagator:");
            System.out.println("length of stored simulation:" + log.getLengthOfSimulation());
        }
        Move move;
        for (int t = 0; t < log.getLengthOfSimulation(); t++) {
            move = log.getMoveAtStep(t);
            SearchNode node = log.getNodeAtStep(t);
            if (node == null) {
                if(Util.isDebug()) System.out.println("no node for this state yet, stopping backpropagate");
                return;
            } else {
                node.recordEvaluation(evaluation, move);
            }
        }
    }

    @Override
    public String toString() {
        return "DefaultBackpropagator";
    }

}
