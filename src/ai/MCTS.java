package ai;

import ai.backprop.Backpropagator;
import ai.evaluation.BoardEvaluator;
import ai.movechoice.FinalMoveChooser;
import ai.nodes.SearchNode;
import ai.nodes.SearchNodeBuilder;
import ai.nodes.TranspositionTable;
import ai.nodes.ValueEstimate;
import ai.selection.SelectionPolicy;
import ai.timing.SearchTimer;

import java.util.Random;

public interface MCTS extends Player, TreeSearchAlgorithm {

    void afterSearch(Move bestMove);

    default void afterSimulation() {};

    void beforeSearch();

    void beforeSimulation();

    Evaluation evaluate();

    Backpropagator getBackpropPolicy();

    void setBackpropPolicy(Backpropagator backpropPolicy);

    BoardEvaluator getBoardEvaluator();

    void setBoardEvaluator(BoardEvaluator boardEvaluator);

    SearchNode getCurrentNode();

    FinalMoveChooser getFinalMoveChooser();

    void setFinalMoveChooser(FinalMoveChooser finalMoveChooser);

    Move getGreedyMoveOfLastSearch();

    ValueEstimate getGreedyValueEstimateOfLastSearch();

    SearchNodeBuilder getNodeBuilder();

    void setNodeBuilder(SearchNodeBuilder nodeBuilder);

    Random getRandom();

    SearchNode getRoot();

    int getRootColorOfCurrentSearch();

    SearchTimer getSearchTimer();

    void setSearchTimer(SearchTimer searchTimer);

    SelectionPolicy getSelectionPolicy();

    void setSelectionPolicy(SelectionPolicy selectionPolicy);

    Board getSimulationBoard();

    void setSimulationBoard(Board simulationBoard);

    SimulationLog getSimulationLog();

    void setSimulationLog(SimulationLog simulationLog);

    int getSimulationsInSearch();

    long getStartTimeOfCurrentSearch();

    TranspositionTable getTable();

    void performSimulation() throws NullMoveException;

    void setBoard(Board board);

    void treeDescent() throws NullMoveException;

}
