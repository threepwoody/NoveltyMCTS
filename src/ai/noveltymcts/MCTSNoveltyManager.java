package ai.noveltymcts;

import ai.Evaluation;
import ai.MCTS;
import experiments.AlphaZero.MovePriorEvaluation;
import utils.UnknownPropertyException;

import java.util.List;

public class MCTSNoveltyManager {

    private NoveltyFunction noveltyFunction;

    public MCTSNoveltyManager(NoveltyFunction noveltyFunction) {
        this.noveltyFunction = noveltyFunction;
    }

    public Evaluation addNoveltyToEvaluation(Evaluation evaluation, MCTS mcts) {
        if(!(evaluation instanceof NoveltyEvaluation)) {
            double[] evalForColors = evaluation.getValueForColor();
            double novelty = 0;
            if(!mcts.getSimulationBoard().isTerminalBoard() || !(noveltyFunction instanceof FeatureBasedPseudocountNoveltyEvaluator)) {
                novelty = noveltyFunction.novelty(mcts.getSimulationBoard(), evaluation);
            }
            if(evaluation instanceof MovePriorEvaluation) {
                List<Double[]> movePriors = ((MovePriorEvaluation) evaluation).getMovePriors();
                return new NoveltyMovePriorEvaluation(evalForColors, movePriors, novelty);
            } else {
                return new BasicNoveltyEvaluation(evalForColors, novelty);
            }
        } else {
            return evaluation;
        }
    }

    public void afterSearch() {
        noveltyFunction.clearObservations();
    }

    public void beforeSearch(MCTS mcts) {
        noveltyFunction.setSearchingColor(mcts.getBoard().getColorToPlay());
        Evaluation rootEvaluation = mcts.getBoardEvaluator().evaluate(mcts.getGame(), mcts.getBoard(), null, mcts.getSimulationLog(), mcts.getRandom());
        rootEvaluation = addNoveltyToEvaluation(rootEvaluation, mcts);
    }

    public void initialize(int width, int height, int numberOfColors) {
        noveltyFunction.initialize(width, height, numberOfColors);
    }

    public void setNoveltyFunction(NoveltyFunction noveltyFunction) {
        this.noveltyFunction = noveltyFunction;
    }

    public void setNoveltyFunctionProperty(String name, String value) throws UnknownPropertyException {
        noveltyFunction.setProperty(name, value);
    }

}
