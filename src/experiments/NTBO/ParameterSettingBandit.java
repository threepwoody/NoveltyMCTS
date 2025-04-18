package experiments.NTBO;

import experiments.Stats;

import java.util.*;

import static java.lang.Math.*;

//C is probably always going to be String, but keeping it generic for now
class ParameterSettingBandit<C> {

    private boolean allArmsExpanded;
    private Map<Map<String,C>, Stats> armStatistics;
    private double initialAverageResultForArms;
    private double initialAverageSquaredResultForArms;
    private int initialSamplesForArms;
    private Map<String, List<C>> possibleValuesForParameters;
    private Random random;
    private int totalSamples;
    public ParameterSettingBandit(Map<String, List<C>> possibleValuesForParameters, Random random, int initialSamplesForArms, double initialAverageResultForArms, double initialAverageSquaredResultForArms) {
        armStatistics = new HashMap<>();
        totalSamples = 1;
        this.possibleValuesForParameters = possibleValuesForParameters;
        allArmsExpanded = false;
        this.random = random;
        this.initialSamplesForArms = initialSamplesForArms;
        this.initialAverageResultForArms = initialAverageResultForArms;
        this.initialAverageSquaredResultForArms = initialAverageSquaredResultForArms;
    }

    public synchronized double UCB1Value(Map<String,C> arm) {
        double averageResult = getAverageResult(arm);
        double averageSquaredResult = getAverageSquaredResult(arm);
        int samples = (int) getSamples(arm);
        double logParentRunCount = log(totalSamples);
        double term1 = averageSquaredResult;
        double term2 = -(averageResult*averageResult);
        double term3 = sqrt(2 * logParentRunCount / samples);
        double v = term1 + term2 + term3; // This equation is above Eq. 1
        double factor1 = logParentRunCount / samples;
        double factor2 = min(0.25, v);
        return averageResult+ NTBOExperiment.explorationFactor*sqrt(factor1 * factor2);
    }

    public synchronized void addArmResult(Map<String,C> arm, double result) {
        if(!armStatistics.containsKey(arm)) {
            armStatistics.put(arm,new Stats(initialAverageResultForArms, initialAverageSquaredResultForArms, initialSamplesForArms));
        }
        totalSamples++;
        armStatistics.get(arm).addResult(result);
    }

    public synchronized double getAverageResult(Map<String,C> arm) {
        boolean armExists = armStatistics.containsKey(arm);
        double averageResult = armExists ? armStatistics.get(arm).getAverageResult() : initialAverageResultForArms;
        return averageResult;
    }

    public synchronized double getAverageSquaredResult(Map<String,C> arm) {
        boolean armExists = armStatistics.containsKey(arm);
        double averageSquaredResult = armExists ? armStatistics.get(arm).getAverageSquaredResult() : initialAverageSquaredResultForArms;
        return averageSquaredResult;
    }

    public Map<String, List<C>> getPossibleValuesForParameters() {
        return possibleValuesForParameters;
    }

    public synchronized double getSamples(Map<String,C> arm) {
        boolean armExists = armStatistics.containsKey(arm);
        double samples = armExists ? armStatistics.get(arm).getSamples() : initialSamplesForArms;
        return samples;
    }

    //generates a new parameter setting that has not been expanded as bandit arm yet
    //if all settings have been expanded, returns null
    private Map<String,C> randomUntriedArm() {
        List<String> parameters = new ArrayList<>(possibleValuesForParameters.keySet());
        Collections.shuffle(parameters, random);
        Map<String, C> result = recursivelyFindUntriedArm(new HashMap<>(), parameters);
        return result;
    }

    private Map<String,C> recursivelyFindUntriedArm(HashMap<String,C> candidateUntriedArm, List<String> parameters) {
        if(parameters.isEmpty()) {
            if(!armStatistics.keySet().contains(candidateUntriedArm)) {
                return candidateUntriedArm;
            } else {
                return null;
            }
        } else {
            List<C> valueList = new ArrayList<>(possibleValuesForParameters.get(parameters.get(0)));
            Collections.shuffle(valueList, random);
            for(C value : valueList) {
                candidateUntriedArm.put(parameters.get(0), value);
                Map<String,C> untriedArm = recursivelyFindUntriedArm(candidateUntriedArm, parameters.subList(1, parameters.size()));
                if(untriedArm==null) {
                    candidateUntriedArm.remove(parameters.get(0));
                } else {
                    return untriedArm;
                }
            }
        }
        return null;
    }

    public synchronized void removeArmResult(Map<String,C> arm, double result) {
        totalSamples--;
        armStatistics.get(arm).removeResult(result);
    }

    //samples either an already existing arm or picks a random new one, depending on UCB1 values
    public synchronized Map<String,C> sampleArm() {
        //using the empty map as an example of a hypothetical untried arm
        Map<String, C> untriedArm = Collections.emptyMap();
        Map<String, C> bestArm = untriedArm;
        double bestValue = UCB1Value(bestArm);
        Map<String, C> bestExpandedArm = null;
        double bestExpandedArmValue = Double.NEGATIVE_INFINITY;
        List<Map<String, C>> expandedArms = new ArrayList<>(armStatistics.keySet());
        Collections.shuffle(expandedArms, random);
        for(Map<String,C> arm : expandedArms) {
            double value = UCB1Value(arm);
            if(value>bestValue) {
                bestValue = value;
                bestArm = arm;
            }
            if(value>bestExpandedArmValue) {
                bestExpandedArmValue = value;
                bestExpandedArm = arm;
            }
        }
        if(bestArm==untriedArm && !allArmsExpanded) {
            //if the hypothetical untried arm came out as best arm, we need to find out if one exists
            Map<String, C> randomUntriedArm = randomUntriedArm();
            if(randomUntriedArm!=null) {
                //if untried arms exist, we return a random one
                return randomUntriedArm;
            } else {
                //if no untried arm exists, we return the best expanded arm
                allArmsExpanded = true;
                return bestExpandedArm;
            }
        } else {
            //if an expanded arm came out as best arm, or if all arms are expanded anyway, we return it
            return bestExpandedArm;
        }
    }

    //prints all arms either in their natural order or sorted by samples
    public String toString(boolean sortedBySamples) {
        String result = "";
        if(sortedBySamples) {
            Comparator<Map.Entry<Map<String,C>,Stats>> armComparator = (o1, o2) -> o2.getValue().getSamples() - o1.getValue().getSamples();
            List<Map.Entry<Map<String,C>,Stats>> listOfEntries = new ArrayList<>(armStatistics.entrySet());
            Collections.sort(listOfEntries,armComparator);
            for(Map.Entry<Map<String,C>,Stats> entry : listOfEntries) {
                result += entry.getKey()+": "+entry.getValue().realSamplesWithoutInitialization()+" matches, average result "+entry.getValue().getAverageResult()+ System.getProperty("line.separator");
            }
        } else {
            for(Map.Entry<Map<String,C>,Stats> entry : armStatistics.entrySet()) {
                result += entry.getKey()+": "+entry.getValue().realSamplesWithoutInitialization()+" matches, average result "+entry.getValue().getAverageResult()+ System.getProperty("line.separator");
            }
        }
        return result;
    }

    public String toString() {
        return ""+possibleValuesForParameters.keySet();
    }

}
