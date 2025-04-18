package experiments.AlphaZero;

import ai.Move;
import ai.noveltymcts.BasicNoveltySearchNode;
import ai.noveltymcts.NoveltySearchNode;

public class DLNoveltySearchNode extends DeepLearningSearchNode implements NoveltySearchNode {

    public DLNoveltySearchNode() {
        setNode(new BasicNoveltySearchNode());
    }

    @Override
    public double getNoveltyOf(Move move) {
        return ((BasicNoveltySearchNode)getNode()).getNoveltyOf(move);
    }

    @Override
    public double getStateNovelty() {
        return ((BasicNoveltySearchNode)getNode()).getStateNovelty();
    }

}
