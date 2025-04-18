package ai.noveltymcts;

import ai.Move;
import ai.nodes.SearchNode;

public interface NoveltySearchNode extends SearchNode {

    double getNoveltyOf(Move move);

    double getStateNovelty();

}
