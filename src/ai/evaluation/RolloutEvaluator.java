package ai.evaluation;

import ai.*;
import ai.nodes.SearchNode;
import ai.policies.Policy;
import ai.policies.RandomPolicy;
import utils.Util;

import java.util.Random;

public class RolloutEvaluator extends BoardEvaluator {

    Policy policy;

    public RolloutEvaluator(Policy policy) {
        this.policy = policy;
    }

    public RolloutEvaluator() {
        this.policy = new RandomPolicy();
    }

    @Override
    public Evaluation eval(Game game, Board simulationBoard, SearchNode currentNode, SimulationLog log, Random random) {
        if(Util.isDebug()) System.out.println("rollout phase:");
        Move move = policy.selectMove(game, simulationBoard, random, log);
        if(log!=null) {
            log.add(simulationBoard, currentNode, move);
        }
        simulationBoard.play(move);
        while (!simulationBoard.isTerminalBoard()) {
            move = policy.selectMove(game, simulationBoard, random, log);
            if(log!=null) {
                log.add(simulationBoard, null, move);
            }
            simulationBoard.play(move);
        }
        if(Util.isDebug()) {
            System.out.println("game over, simulation ends here with result: "+simulationBoard.evalOfTerminalBoard()+": ");
            if(Util.isDebug()) System.out.println(simulationBoard);
        }
        return simulationBoard.evalOfTerminalBoard();
    }

    public Policy getPolicy() {
        return policy;
    }

    @Override
    public String toString() {
        return "Rollout evaluator with policy: "+policy;
    }

}
