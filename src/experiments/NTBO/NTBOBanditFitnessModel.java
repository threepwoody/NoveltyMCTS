package experiments.NTBO;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NTBOBanditFitnessModel implements NTBOFitnessModel {

    private List<Integer> banditDimensionalities;
    //contains bandits for single parameters, pairs of parameters, triples of parameters... etc
    private Map<Set<String>, ParameterSettingBandit<String>> banditsForParameterSets;
    private List<String> parameters;
    private Random random;

    public NTBOBanditFitnessModel(Map<String, List<String>> possibleValuesForParameters, Set<Integer> banditDimensionalities, Random random, int numberOfColorsInGame) {
        banditsForParameterSets = new HashMap<>();
        double initialAverageResultForArms = 1.0/numberOfColorsInGame;
        double initialAverageSquaredResultForArms = initialAverageResultForArms*initialAverageResultForArms;
        int initialSamplesForArms = 1;
        for(Integer size : banditDimensionalities) {
            Set<Set<String>> banditParameterSets = createSubsetsOfSize(possibleValuesForParameters.keySet(), size);
            for(Set<String> banditParameterSet : banditParameterSets) {
                Map<String, List<String>> possibleValueForBanditParameters =
                        banditParameterSet.stream()
                        .collect(Collectors.toMap(Function.identity(), possibleValuesForParameters::get));
                banditsForParameterSets.put(banditParameterSet, new ParameterSettingBandit<>(possibleValueForBanditParameters, random, initialSamplesForArms, initialAverageResultForArms, initialAverageSquaredResultForArms));
            }
        }
        parameters = new ArrayList<>(possibleValuesForParameters.keySet());
        banditsForParameterSets.put(new HashSet<>(parameters), new ParameterSettingBandit<>(possibleValuesForParameters, random, initialSamplesForArms, initialAverageResultForArms, initialAverageSquaredResultForArms));
        this.random = random;
        this.banditDimensionalities = new ArrayList<>(banditDimensionalities);
        this.banditDimensionalities.add(parameters.size());
        Collections.sort(this.banditDimensionalities);
    }

    @Override
    public void addEvaluationForSolution(double evaluation, Map<String, String> solution) {
        for(Set<String> parameterSet : banditsForParameterSets.keySet()) {
            ParameterSettingBandit bandit = banditsForParameterSets.get(parameterSet);
            Map<String, String> settingsForBandit = new HashMap<>();
            for(String parameter : parameterSet) {
                settingsForBandit.put(parameter, solution.get(parameter));
            }
            bandit.addArmResult(settingsForBandit, evaluation);
        }
    }

    private <C> Set<Set<C>> createSubsetsOfSize(Set<C> inputSet, Integer size) {
        Set<Set<C>> result = new HashSet<>();
        List<C> inputList = new ArrayList<>(inputSet);
        List<C> prefix = new ArrayList<>();
        result = recursivelyCreateSubsetsOfSize(inputList, size, prefix, result);
        return result;
    }

    @Override
    public synchronized double predictFitnessOfSolution(Map<String, String> solution, boolean output, boolean withExploration) {
        double result = 0;
        if(output) {
            System.out.println("predicting fitness for solution: " + solution);
        }
        for(Set<String> parameterSet : banditsForParameterSets.keySet()) {
            ParameterSettingBandit bandit = banditsForParameterSets.get(parameterSet);
            Map<String, String> settingsForBandit = new HashMap<>();
            for(String parameter : parameterSet) {
                settingsForBandit.put(parameter, solution.get(parameter));
            }
            double banditContributionToFitness = withExploration ? bandit.UCB1Value(settingsForBandit) : bandit.getAverageResult(settingsForBandit);
            if(output) {
                System.out.println("from bandit " + settingsForBandit + ": " + banditContributionToFitness + " with samples: " + bandit.getSamples(settingsForBandit));
            }
            double weightForBandit = 1;
            result += banditContributionToFitness*weightForBandit;
        }
        result /= banditsForParameterSets.size();
        if(output) {
            System.out.println("predicted fitness: "+result);
        }
        return result;
    }

    private <C> Set<Set<C>> recursivelyCreateSubsetsOfSize(List<C> parameters, Integer size, List<C> prefix, Set<Set<C>> result) {
        if(size==1) {
            for(C element : parameters) {
                List<C> newSubset = new ArrayList<>(prefix);
                newSubset.add(element);
                result.add(new HashSet<>(newSubset));
            }
        } else {
            for(C element : parameters) {
                List<C> newPrefix = new ArrayList<>(prefix);
                newPrefix.add(element);
                List<C> newParameters = new ArrayList<>(parameters);
                newParameters.remove(element);
                result.addAll(recursivelyCreateSubsetsOfSize(newParameters, size-1, newPrefix, result));
            }
        }
        return result;
    }

    @Override
    public void removeEvaluationForSolution(double evaluation, Map<String, String> solution) {
        for(Set<String> parameterSet : banditsForParameterSets.keySet()) {
            ParameterSettingBandit bandit = banditsForParameterSets.get(parameterSet);
            Map<String, String> settingsForBandit = new HashMap<>();
            for(String parameter : parameterSet) {
                settingsForBandit.put(parameter, solution.get(parameter));
            }
            bandit.removeArmResult(settingsForBandit, evaluation);
        }
    }

    @Override
    public synchronized Map<String, String> sampleNextSolution() {
        //TODO for now, picks dimensionality of bandits (their specificity = number of parameters) to pick the next candidate in a crude way: with fixed probabilities. a smarter selection of bandits would converge to the full-dimensional bandit (most specific) in the limit.
        Map<String, String> nextSolution = new HashMap<>();
        int banditDimensionality;
        //static alternative that gives equal probability to all bandit dimensionalities
//        banditDimensionality = banditDimensionalities.get(random.nextInt(banditDimensionalities.size()));
        //static alternative that puts much more emphasis on smaller dimensionalities
        if(random.nextDouble()<0.5) {
            banditDimensionality = 1;
        } else if(random.nextDouble()<1.0) {
            banditDimensionality = 2;
        } else {
            banditDimensionality = parameters.size();
        }
        //then, randomly go through parameters and set them with bandits of that dimensionality. if a number of unset parameters smaller than the chosen dimensionality are left over, pick them with the corresponding bandit of lower dimensionality.
        List<String> parametersToSet = new ArrayList<>(parameters);
        Collections.shuffle(parametersToSet, random);
        List<String> parametersAlreadySet = new ArrayList<>();
        for(String parameter : parametersToSet) {
            //if parameter not already set in solution
            if(!parametersAlreadySet.contains(parameter)) {
                //find all bandit of chosen dimensionality that contain this parameter and no already set parameter
                List<Map.Entry<Set<String>, ParameterSettingBandit<String>>> banditList = banditsForParameterSets.entrySet().stream()
                        .filter(x -> x.getKey().size()==banditDimensionality)
                        .filter(x -> x.getKey().contains(parameter) && Collections.disjoint(x.getKey(), parametersAlreadySet))
                        .collect(Collectors.toList());
                if(banditList.isEmpty()) {
                    //if there is no bandit of the chosen dimensionality that can contribute additional parameter values, find the bandit of lower dimensionality that can complete the solution instead
                    Set<String> leftoverParameters = parametersToSet.stream()
                            .filter(x -> !parametersAlreadySet.contains(x))
                            .collect(Collectors.toSet());
                    ParameterSettingBandit<String> bandit = banditsForParameterSets.get(leftoverParameters);
                    Map<String, String> parameterSettings = bandit.sampleArm();
                    nextSolution.putAll(parameterSettings);
                    break;
                } else {
                    //if there are bandits of the chosen dimensionality that can contribute additional parameter values, pick a random bandit from them and let it choose its parameter values
                    ParameterSettingBandit<String> bandit = banditList.get(random.nextInt(banditList.size())).getValue();
                    Map<String, String> parameterSettings = bandit.sampleArm();
                    //add the chosen parameters to parametersAlreadySet, and with their chosen values to the solution being sampled
                    parametersAlreadySet.addAll(parameterSettings.keySet());
                    nextSolution.putAll(parameterSettings);
                }
            }
        }
        return nextSolution;
    }

    public String toString(boolean includingBandits) {
        String result = "BanditFitnessModel with bandits:\r\n";
        for(Map.Entry<Set<String>, ParameterSettingBandit<String>> entry : banditsForParameterSets.entrySet()) {
            if(includingBandits) {
                result += entry.getKey() + ":\r\n";
                result += entry.getValue().toString(true);
            } else {
                result += entry.getKey() + ", ";
            }
        }
        result = result.substring(0,result.length()-2);
        return result;
    }

}
