package ai.evaluation;

import ai.Board;
import ai.Evaluation;
import ai.Game;
import ai.SimulationLog;
import ai.nodes.SearchNode;
import utils.Util;

import java.util.Random;

public abstract class StaticEvaluator extends BoardEvaluator {

    @Override
    public Evaluation eval(Game game, Board board, SearchNode currentNode, SimulationLog log, Random random) {
        if(log!=null) {
            log.add(board, currentNode, null);
        }
        Evaluation staticEval = staticEval(board);
        if (Util.isDebug()) {
            double[] evalForColor = staticEval.getValueForColor();
            String output = "";
            output += "Board evaluated, values for the players: [";
            for(int player=0; player<evalForColor.length; player++) {
                output += evalForColor[player]+", ";
            }
            output = output.substring(0, output.length()-2);
            output +="]";
            System.out.println(output);
        }
        return staticEval;
    }

    abstract public Evaluation staticEval(Board board);

}
