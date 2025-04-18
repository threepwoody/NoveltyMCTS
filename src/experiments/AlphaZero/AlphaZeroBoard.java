package experiments.AlphaZero;

import ai.Board;

import java.util.Map;

public interface AlphaZeroBoard extends Board {

    //symmetry 1 has to be the board as-is
    Map<Integer, String> CSVOfAllBoardSymmetries();

    default Map<Integer, String> shortCSVOfAllBoardSymmetries() {
        return CSVOfAllBoardSymmetries();
    };

}
