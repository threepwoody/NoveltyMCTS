package ai.evaluation;

import ai.*;
import ai.nodes.SearchNode;
import ai.policies.Policy;
import ai.policies.RandomPolicy;
import utils.Util;

import java.util.Random;

public class CutoffRolloutEvaluator extends BoardEvaluator {

    private int maxRolloutSteps; //>=1; for 0 just use the StaticEvaluator alone
    private Policy policy;
    private StaticEvaluator staticEvaluator;

    public CutoffRolloutEvaluator(Policy policy) {
        this.policy = policy;
        maxRolloutSteps = Integer.MAX_VALUE;
    }

    public CutoffRolloutEvaluator(Policy policy, StaticEvaluator staticEvaluator, int maxRolloutSteps) {
        this.policy = policy;
        this.staticEvaluator = staticEvaluator;
        this.maxRolloutSteps = maxRolloutSteps;
    }

    public CutoffRolloutEvaluator() {
        this.policy = new RandomPolicy();
        maxRolloutSteps = Integer.MAX_VALUE;
    }

    @Override
    public Evaluation eval(Game game, Board simulationBoard, SearchNode currentNode, SimulationLog log, Random random) {
        if(Util.isDebug()) System.out.println("rollout phase:");
        int rolloutSteps = 0;
        Move move = policy.selectMove(game, simulationBoard, random, log);
        if(log!=null) {
            log.add(simulationBoard, currentNode, move);
        }
        simulationBoard.play(move);
        rolloutSteps++;
        while (!simulationBoard.isTerminalBoard() && rolloutSteps<maxRolloutSteps) {
            move = policy.selectMove(game, simulationBoard, random, log);
            if(log!=null) {
                log.add(simulationBoard, null, move);
            }
            simulationBoard.play(move);
            rolloutSteps++;
        }
        if(simulationBoard.isTerminalBoard()) {
            Evaluation eval = simulationBoard.evalOfTerminalBoard();
            if (Util.isDebug()) {
                System.out.println("game over, simulation ends here with result: " + eval + ": ");
                System.out.println(simulationBoard);
            }
            return eval;
        } else {
            Evaluation eval = staticEvaluator.evaluate(game, simulationBoard, null, log, random);
            if (Util.isDebug()) {
                System.out.println(rolloutSteps+"rollout steps made, simulation ends here with result: " + eval + ": ");
                System.out.println(simulationBoard);
            }
            return eval;
        }
    }

    public Policy getPolicy() {
        return policy;
    }

    @Override
    public String toString() {
        return "Rollout evaluator with policy: "+policy;
    }

}
