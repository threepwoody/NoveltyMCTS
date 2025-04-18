package ai.nodes;

import ai.Evaluation;
import ai.Move;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

public abstract class BasicSearchNode implements SearchNode {

    private LinkedHashMap<Move, SearchNode> childNodes;
    private int colorToPlay;
    private boolean fresh;
    private long hash;
    private boolean marked;
    private SearchNode next;
    private int numberOfLegalMoves;
    private int realChildrenSamples;

    public BasicSearchNode() {
        childNodes = new LinkedHashMap<>();
    }

    @Override
    public void addChildForMove(SearchNode child, Move move) {
        childNodes.put(move,child);
    }

    @Override
    public SearchNode getChildForMove(Move move) {
        return childNodes.get(move);
    }

    @Override
    public Collection<SearchNode> getChildNodes() {
        return childNodes.values();
    }

    @Override
    public int getColorToPlay() {
        return colorToPlay;
    }

    @Override
    public void setColorToPlay(int colorToPlay) {
        this.colorToPlay = colorToPlay;
    }

    @Override
    public List<Move> getExpandedMoves() {
        return new ArrayList<>(childNodes.keySet());
    }

    @Override
    public long getHash() {
        return hash;
    }

    @Override
    public SearchNode getNext() {
        return next;
    }

    @Override
    public void setNext(SearchNode next) {
        this.next = next;
    }

    @Override
    public int getNumberOfLegalMoves() {
        return numberOfLegalMoves;
    }

    @Override
    public int getRealChildrenSamples() {
        return realChildrenSamples;
    }

    @Override
    public void setRealChildrenSamples(int realChildrenSamples) {
        this.realChildrenSamples = realChildrenSamples;
    }

    @Override
    public boolean hasChildForMove(Move move) {
        return childNodes.containsKey(move);
    }

    @Override
    public boolean isFresh() {
        return fresh;
    }

    @Override
    public boolean isMarked() {
        return marked;
    }

    @Override
    public void setMarked(boolean marked) {
        this.marked = marked;
    }

    @Override
    public void recordEvaluation(Evaluation evaluation, Move move) {
        fresh = false;
    }

    @Override
    public void recordEvaluation(Evaluation evaluation, Move move, int weight) {
        fresh = false;
    }

    @Override
    public void reset(long hash, SearchNodeBuilder builder, int numberOfColors, int numberOfLegalMoves) {
        this.hash = hash;
        childNodes.clear();
        marked = false;
        fresh = true;
        realChildrenSamples = 0;
        this.numberOfLegalMoves = numberOfLegalMoves;
    }

}
