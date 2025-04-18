package ai;

public interface TreeSearchAlgorithm {

    //returns the size of the search tree constructed for the last search
    //if using iterative deepening: returns the size of the largest tree completed for the last search
    int treeSize();

    //returns an array with the node counts for every depth in the search tree constructed for the last search
    //if using iterative deepening: relates to the largest tree completed for the last search
    int[] getNodesAtDepth();

}
