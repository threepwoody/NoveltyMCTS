package experiments;

public interface FitnessEvaluator {

    double evaluateFitness(String player);

    void reset();

    int matchesPerEvaluation();
}
