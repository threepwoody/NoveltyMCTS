package ai.nodes;

public class StateActionValueSearchNodeBuilder extends SearchNodeBuilder {

    @Override
    public StateActionValueSearchNode buildSearchNode() {
        return new StateActionValueSearchNode();
    }

    @Override
    public String toString() {
        return "StateActionValueSearchNodeBuilder";
    }

}