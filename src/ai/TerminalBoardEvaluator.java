package ai;

public interface TerminalBoardEvaluator {

    Evaluation evalOfTerminalBoard(Board terminalBoard);

    TerminalBoardEvaluator copy();

}
