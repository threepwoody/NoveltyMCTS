package ai.noveltymcts;

import ai.Evaluation;
import ai.Board;
import utils.UnknownPropertyException;

public interface NoveltyFunction {

    void clearObservations();

    void initialize(int width, int height, int numberOfColors);

    //returns value between 0 and 1, where higher means more novel
    double novelty(Board board, Evaluation evaluation);

    void setProperty(String name, String value) throws UnknownPropertyException;

    void setSearchingColor(int color);

}
