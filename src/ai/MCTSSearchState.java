package ai;

import ai.nodes.SearchNode;

public class MCTSSearchState {

    private Board currentBoard;
    private SearchNode currentNode;

    public MCTSSearchState(Board currentBoard, SearchNode currentNode) {
        this.currentBoard = currentBoard;
        this.currentNode = currentNode;
    }

    public Board getCurrentBoard() {
        return currentBoard;
    }

    public SearchNode getCurrentNode() {
        return currentNode;
    }

}
