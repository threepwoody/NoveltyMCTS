package ai.timing;

import ai.MCTS;

public class SimulationLimitPerSearchTimer implements SearchTimer {

    int simulationsPerSearch;

    public SimulationLimitPerSearchTimer() {
        simulationsPerSearch = 0;
    }

    public SimulationLimitPerSearchTimer(int budget) {
        simulationsPerSearch = budget;
    }

    @Override
    public int getSearchBudget() {
        return simulationsPerSearch;
    }

    @Override
    public void setSearchBudget(int budget) {
        simulationsPerSearch = budget;
    }

    @Override
    public boolean shouldKeepRunning(MCTS mcts) {
        return mcts.getSimulationsInSearch() < simulationsPerSearch;
    }

    public String toString() {
        return "Simulation limit of "+simulationsPerSearch+" simulations per search.";
    }

}
