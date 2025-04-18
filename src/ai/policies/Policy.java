package ai.policies;

import ai.Board;
import ai.Game;
import ai.Move;
import ai.SimulationLog;

import java.util.Random;

public interface Policy {

    Move selectMove(Game game, Board board, Random random, SimulationLog log);

}
