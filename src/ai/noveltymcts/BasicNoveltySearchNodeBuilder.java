package ai.noveltymcts;

import ai.nodes.SearchNodeBuilder;

public class BasicNoveltySearchNodeBuilder extends SearchNodeBuilder {

    @Override
    public BasicNoveltySearchNode buildSearchNode() {
        return new BasicNoveltySearchNode();
    }

    @Override
    public String toString() {
        return "BasicNoveltySearchNodeBuilder";
    }

}
