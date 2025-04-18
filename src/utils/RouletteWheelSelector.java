package utils;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class RouletteWheelSelector {

    public static <T> T selectItem(List<T> items, FitnessEvaluator<T> fitnessEvaluator, double temperature, Random random) {
        double[] cumulativeFitnesses = new double[items.size()];
        cumulativeFitnesses[0] = Math.pow(fitnessEvaluator.fitness(items.get(0)), 1/temperature);
        for (int i=1; i<items.size(); i++) {
            double fitness = fitnessEvaluator.fitness(items.get(i));
            cumulativeFitnesses[i] = cumulativeFitnesses[i-1] + Math.pow(fitness, 1/temperature);
        }
        double randomFitness = random.nextDouble()*cumulativeFitnesses[cumulativeFitnesses.length-1];
        int index = Arrays.binarySearch(cumulativeFitnesses, randomFitness);
        if (index < 0) {
            // Convert negative insertion point to array index.
            index = Math.abs(index + 1);
        }
        return items.get(index);
    }

}
