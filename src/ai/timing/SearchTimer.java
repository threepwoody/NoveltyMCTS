package ai.timing;

import ai.MCTS;

public interface SearchTimer {

    int getSearchBudget();

    void setSearchBudget(int budget);

    boolean shouldKeepRunning(MCTS mcts);

    default void updateAfterSearch(MCTS mcts) {};

    default void updateBeforeSearch(MCTS mcts) {};

}
