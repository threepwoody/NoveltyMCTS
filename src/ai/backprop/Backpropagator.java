package ai.backprop;

import ai.Evaluation;
import ai.SimulationLog;

public interface Backpropagator {

    void backpropagate(Evaluation evaluation, SimulationLog log);

}
