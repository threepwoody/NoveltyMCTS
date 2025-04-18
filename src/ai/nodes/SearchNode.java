package ai.nodes;

import ai.Evaluation;
import ai.Move;

import java.util.Collection;
import java.util.List;

public interface SearchNode extends Poolable<SearchNode> {

    void addChildForMove(SearchNode child, Move move);

    SearchNode getChildForMove(Move move);

    Collection<SearchNode> getChildNodes();

    int getColorToPlay();

    void setColorToPlay(int colorToPlay);

    List<Move> getExpandedMoves();

    long getHash();

    int getNumberOfLegalMoves();

    //actual sum of samples through all children, not counting virtual samples - as needed by progressive widening & such
    int getRealChildrenSamples();

    void setRealChildrenSamples(int visits);

    //sum of samples through all possible children, whether expanded as nodes or not - as needed by selection functions. includes potential virtual samples assigned to unvisited children, which then have to be counted for unexpanded children as well.
    int getTotalChildrenSamples();

    ValueEstimate getValueEstimateOf(Move move);

    boolean hasChildForMove(Move move);

    boolean isFresh();

    boolean isMarked();

    void setMarked(boolean marked);

    //if no move made from the node (e.g. because a terminal position was found in the tree, or because a static eval was used at the node), move will be null.
    void recordEvaluation(Evaluation evaluation, Move move);

    void recordEvaluation(Evaluation evaluation, Move move, int weight);

    void reset(long hash, SearchNodeBuilder builder, int numberOfColors, int numberOfLegalMoves);

}
