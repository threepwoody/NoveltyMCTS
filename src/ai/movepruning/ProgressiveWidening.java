package ai.movepruning;

import ai.Board;
import ai.MCTS;
import ai.Move;
import ai.MoveSorter;
import ai.nodes.SearchNode;
import utils.Util;

import java.util.ArrayList;
import java.util.List;

public class ProgressiveWidening implements MovePruner {

    private boolean differentWideningForOpponents = false;
    private int fixedOpponentBranchingFactor = 1;
    private MoveSorter moveSorter;
    private double opponentWideningAlpha = 0.5;
    private double opponentWideningC = 1;
    //if true, opponents have a fixed branching factor, and only the root player widens
    private boolean opponentsSetToFixedBranchingFactor = false;
    private List<Move> prunedMoves = new ArrayList<>();
    private int rootColorOfCurrentSearch;
    private List<Move> sortedMoves = new ArrayList<>();
    private double wideningAlpha = 0.5;
    private double wideningC = 0.5;
    //if true, root player moves are always chosen from the full legal move set, and opponents widen
    private boolean wideningOnlyForOpponents = false;

    public ProgressiveWidening(MoveSorter moveSorter) {
        this.moveSorter = moveSorter;
    }

    @Override
    public List<Move> getPrunedMoves(SearchNode node, Board board) {
        List<Move> legalMoves = board.getLegalMoves();
        sortedMoves.clear();
        sortedMoves.addAll(legalMoves);
        int maxMoveIndex = legalMoves.size()-1;
        if(opponentsSetToFixedBranchingFactor && board.getColorToPlay()!=rootColorOfCurrentSearch) {
            maxMoveIndex = Math.min(legalMoves.size()-1, fixedOpponentBranchingFactor-1);
        } else {
            if (!wideningOnlyForOpponents || board.getColorToPlay() != rootColorOfCurrentSearch) {
                if(!differentWideningForOpponents || board.getColorToPlay() == rootColorOfCurrentSearch) {
                    maxMoveIndex = Math.min(legalMoves.size() - 1, maxMoveIndex(node, wideningC, wideningAlpha) - 1);
                } else {
                    maxMoveIndex = Math.min(legalMoves.size() - 1, maxMoveIndex(node, opponentWideningC, opponentWideningAlpha) - 1);
                }
            }
        }
        maxMoveIndex = Math.max(maxMoveIndex, 0);
        if(Util.isDebug()) System.out.println("progressive widening will look at this many moves: "+(maxMoveIndex+1)+" in node "+node.getHash());
        if(maxMoveIndex<legalMoves.size()-1) {
            sortedMoves = moveSorter.sortMoves(sortedMoves, board);
        }
        prunedMoves.clear();
        for(int i=0; i<=maxMoveIndex; i++) {
            prunedMoves.add(sortedMoves.get(i));
        }
        return prunedMoves;
    }

    @Override
    public void initializeSearch(MCTS mcts) {
        setRootColorOfCurrentSearch(mcts.getRootColorOfCurrentSearch());
    }

    //progressive widening according to Adrien Couetoux et al., "Continuous Upper Confidence Trees"
    private int maxMoveIndex(SearchNode node, double wideningC, double wideningAlpha) {
        //visit count for widening needs to start at 1 - correction only necessary in root node which might have never been visited (in first rollout)
        int t = Math.max(1,node.getRealChildrenSamples());
        return (int) Math.ceil(wideningC * Math.pow(t,wideningAlpha));
    }

    @Override
    public boolean randomizeAfterPruning() {
        return false;
    }

    public void setDifferentWideningForOpponents(boolean differentWideningForOpponents) {
        this.differentWideningForOpponents = differentWideningForOpponents;
    }

    public void setFixedOpponentBranchingFactor(int fixedOpponentBranchingFactor) {
        this.fixedOpponentBranchingFactor = fixedOpponentBranchingFactor;
    }

    public void setOpponentWideningAlpha(double opponentWideningAlpha) {
        this.opponentWideningAlpha = opponentWideningAlpha;
    }

    public void setOpponentWideningC(double opponentWideningC) {
        this.opponentWideningC = opponentWideningC;
    }

    public void setOpponentsSetToFixedBranchingFactor(boolean opponentsSetToFixedBranchingFactor) {
        this.opponentsSetToFixedBranchingFactor = opponentsSetToFixedBranchingFactor;
    }

    public void setRootColorOfCurrentSearch(int rootColorOfCurrentSearch) {
        this.rootColorOfCurrentSearch = rootColorOfCurrentSearch;
    }

    public void setWideningAlpha(double wideningAlpha) {
        this.wideningAlpha = wideningAlpha;
    }

    public void setWideningC(double wideningC) {
        this.wideningC = wideningC;
    }

    public void setWideningOnlyForOpponents(boolean wideningOnlyForOpponents) {
        this.wideningOnlyForOpponents = wideningOnlyForOpponents;
    }

}
