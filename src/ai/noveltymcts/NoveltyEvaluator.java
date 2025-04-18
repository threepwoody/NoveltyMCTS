package ai.noveltymcts;

import ai.Board;
import ai.Evaluation;
import ai.Game;
import ai.SimulationLog;
import ai.evaluation.BoardEvaluator;
import ai.nodes.SearchNode;

import java.util.Random;

public class NoveltyEvaluator extends BoardEvaluator {

    private BoardEvaluator evaluator;
    private NoveltyFunction noveltyFunction;

    public NoveltyEvaluator(BoardEvaluator evaluator, NoveltyFunction noveltyFunction) {
        this.evaluator = evaluator;
        this.noveltyFunction = noveltyFunction;
    }

    @Override
    public Evaluation eval(Game game, Board board, SearchNode currentNode, SimulationLog log, Random random) {
        Evaluation evaluation = evaluator.evaluate(game, board, currentNode, log, random);
        double[] evalForColors = evaluation.getValueForColor();
        double novelty = noveltyFunction.novelty(board, evaluation);
        return new BasicNoveltyEvaluation(evalForColors, novelty);
    }

    public BoardEvaluator getEvaluator() {
        return evaluator;
    }

    public void setEvaluator(BoardEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    public void setNoveltyFunction(NoveltyFunction noveltyFunction) {
        this.noveltyFunction = noveltyFunction;
    }

}
