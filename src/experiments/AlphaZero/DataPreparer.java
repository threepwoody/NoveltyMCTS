package experiments.AlphaZero;

import ai.djl.ndarray.NDList;

public interface DataPreparer {

    NDList datum();

    NDList label();

    void setCSVInput(String CSVLine);

}
