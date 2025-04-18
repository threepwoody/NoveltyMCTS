package experiments.AlphaZero;

import ai.nodes.SearchNodeBuilder;

public class DeepLearningSearchNodeBuilder extends SearchNodeBuilder {

    @Override
    public DeepLearningSearchNode buildSearchNode() {
        return new DeepLearningSearchNode();
    }

    @Override
    public String toString() {
        return "DeepLearningSearchNodeBuilder";
    }

}