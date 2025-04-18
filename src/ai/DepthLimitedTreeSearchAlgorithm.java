package ai;

import ai.TreeSearchAlgorithm;

public interface DepthLimitedTreeSearchAlgorithm extends TreeSearchAlgorithm {

    //returns the depth of the search tree constructed for the last search
    //if using iterative deepening: returns the maximum depth completed for the last search
    int treeDepth();

}
