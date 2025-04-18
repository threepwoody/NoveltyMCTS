package ai;

public class SearchForEveryMove implements SearchTrigger {

    @Override
    public Move chooseMoveWithoutSearch(MCTS mcts) {
        return null;
    }

    @Override
    public boolean searchNow(MCTS mcts) {
        return true;
    }

    @Override
    public void updateAfterSearch(MCTS mcts, Move bestMove) {
    }

}
