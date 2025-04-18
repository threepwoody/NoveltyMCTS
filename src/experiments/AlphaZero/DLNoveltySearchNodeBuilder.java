package experiments.AlphaZero;

import ai.nodes.SearchNodeBuilder;

public class DLNoveltySearchNodeBuilder extends SearchNodeBuilder {

    @Override
    public DLNoveltySearchNode buildSearchNode() {
        return new DLNoveltySearchNode();
    }

    @Override
    public String toString() {
        return "DLNoveltySearchNodeBuilder";
    }

}
