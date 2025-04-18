package ai.timing;

import ai.MCTS;

public class MillisecondLimitPerSearchTimer implements SearchTimer {

    int millisecondsPerSearch = 0;

    public MillisecondLimitPerSearchTimer() {
        millisecondsPerSearch = 0;
    }

    public MillisecondLimitPerSearchTimer(int budget) {
        millisecondsPerSearch = budget;
    }

    @Override
    public int getSearchBudget() {
        return millisecondsPerSearch;
    }

    @Override
    public void setSearchBudget(int budget) {
        millisecondsPerSearch = budget;
    }

    @Override
    public boolean shouldKeepRunning(MCTS mcts) {
        return (System.currentTimeMillis()- mcts.getStartTimeOfCurrentSearch())<millisecondsPerSearch;
    }

    public String toString() {
        return "Time limit of "+millisecondsPerSearch+" ms per search.";
    }

}
