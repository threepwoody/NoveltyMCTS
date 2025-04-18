package ai;

public interface SearchTrigger {

    Move chooseMoveWithoutSearch(MCTS mcts);

    boolean searchNow(MCTS mcts);

    void updateAfterSearch(MCTS mcts, Move bestMove);

}
