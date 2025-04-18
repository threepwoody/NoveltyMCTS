package games;

import ai.Game;
import ai.evaluation.StaticEvaluator;

public interface StaticEvaluatorGame extends Game {

    StaticEvaluator newStaticEvaluator();

}
