package ai.nodes;

public class StateValueSearchNodeBuilder extends SearchNodeBuilder {

    @Override
    public StateValueSearchNode buildSearchNode() {
        return new StateValueSearchNode();
    }

    @Override
    public String toString() {
        return "StateValueSearchNodeBuilder";
    }

}
