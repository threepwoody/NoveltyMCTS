package experiments.NTBO;

import java.util.Map;

public interface NTBOFitnessModel {

    Map<String, String> sampleNextSolution();

    String toString(boolean includingBandits);

    void addEvaluationForSolution(double evaluation, Map<String, String> solution);

    double predictFitnessOfSolution(Map<String, String> solution, boolean output, boolean withExploration);

    void removeEvaluationForSolution(double evaluation, Map<String, String> solution);

}
