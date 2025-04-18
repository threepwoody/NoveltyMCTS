package experiments;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.*;

public class UCB1TunedBandit<C> {

    private Map<C, Stats> armStatistics;
    private double explorationFactor;
    private int initialSamplesForArms;
    private int maxSamples;
    private String name;
    private int numberOfVirtualLosses;
    private int samples;
    private boolean stopWhenArmWithMaxSamplesIsIdentified = false;
    private boolean usingVirtualLoss = false;

    public UCB1TunedBandit(String name, int maxSamples, Collection<C> arms, double initialAverageResultForArms, double initialAverageSquaredResultForArms, int initialSamplesForArms, double explorationFactor) {
        this.name = name;
        this.maxSamples = maxSamples;
        armStatistics = new HashMap<>();
        for(C arm : arms) {
            armStatistics.put(arm,new Stats(initialAverageResultForArms, initialAverageSquaredResultForArms, initialSamplesForArms));
        }
        samples = armStatistics.size()*initialSamplesForArms;
        this.explorationFactor = explorationFactor;
        this.initialSamplesForArms = initialSamplesForArms;
    }

    private synchronized double UCB1TunedValue(Stats stats) {
        double averageResult = stats.getAverageResult();
        double logParentRunCount = log(samples);
        double term1 = stats.getAverageSquaredResult();
        double term2 = -(stats.getAverageResult()*stats.getAverageResult());
        double term3 = sqrt(2 * logParentRunCount / stats.getSamples());
        double v = term1 + term2 + term3; // This equation is above Eq. 1
        double factor1 = logParentRunCount / stats.getSamples();
        double factor2 = min(0.25, v);
        return averageResult+explorationFactor*sqrt(factor1 * factor2);
    }

    public synchronized void addArmResult(C arm, double result) {
        samples++;
        armStatistics.get(arm).addResult(result);
    }

    public synchronized C armWithMaxAverageReturn() {
        return armStatistics.entrySet().stream().sorted(Comparator.comparingDouble(e -> -e.getValue().getAverageResult())).collect(Collectors.toList()).get(0).getKey();
    }

    public String getName() {
        return name;
    }

    public int getNumberOfVirtualLosses() {
        return numberOfVirtualLosses;
    }

    public void setNumberOfVirtualLosses(int numberOfVirtualLosses) {
        this.numberOfVirtualLosses = numberOfVirtualLosses;
    }

    public int getSamples() {
        return samples;
    }

    public void setSamples(int samples) {
        this.samples = samples;
    }

    public synchronized Stats getStatsForArm(C arm) {
        return armStatistics.get(arm);
    }

    public synchronized boolean isFinishedSampling() {
        boolean finished = false;
        if(stopWhenArmWithMaxSamplesIsIdentified) {
            List<Map.Entry<C, Stats>> sortedEntryList = armStatistics.entrySet().stream().sorted(Comparator.comparingDouble(e -> -e.getValue().getSamples())).collect(Collectors.toList());
            int highestSamples = sortedEntryList.get(0).getValue().getSamples();
            int secondHighestSamples = sortedEntryList.get(1).getValue().getSamples();
            if(maxSamples-realSamplesWithoutInitialization()<highestSamples-secondHighestSamples) {
                finished = true;
            }
        } else {
            finished = realSamplesWithoutInitialization() >= maxSamples;
        }
        return finished;
    }

    public boolean isUsingVirtualLoss() {
        return usingVirtualLoss;
    }

    public void setUsingVirtualLoss(boolean usingVirtualLoss) {
        this.usingVirtualLoss = usingVirtualLoss;
    }

    public synchronized int realSamplesWithoutInitialization() {
        return samples-armStatistics.size()*initialSamplesForArms;
    }

    public synchronized void removeArmResult(C arm, double result) {
        samples--;
        armStatistics.get(arm).removeResult(result);
    }

    //uses UCB1-Tuned formula to pick next arm
    public synchronized C sampleNextArm() {
        Map.Entry<C, Stats> maxEntry = null;
        double maxValue = Double.NEGATIVE_INFINITY;
        for (Map.Entry<C, Stats> entry : armStatistics.entrySet()) {
            if(entry.getValue().getSamples()==initialSamplesForArms) {
                if(usingVirtualLoss) {
                    for (int i = 0; i < numberOfVirtualLosses; i++) {
                        addArmResult(entry.getKey(), 0);
                    }
                }
                return entry.getKey();
            }
            double value = UCB1TunedValue(entry.getValue());
            if (maxEntry == null || (value > maxValue)) {
                maxEntry = entry;
                maxValue = value;
            }
        }
        if(usingVirtualLoss) {
            for (int i = 0; i < numberOfVirtualLosses; i++) {
                addArmResult(maxEntry.getKey(), 0);
            }
        }
        return maxEntry.getKey();
    }

    public void setStopWhenArmWithMaxSamplesIsIdentified(boolean stopWhenArmWithMaxSamplesIsIdentified) {
        this.stopWhenArmWithMaxSamplesIsIdentified = stopWhenArmWithMaxSamplesIsIdentified;
    }

    //prints all arms either in their natural order or sorted by samples
    public String toString(boolean sortedBySamples) {
        String result = "";
        if(sortedBySamples) {
            Comparator<Map.Entry<C,Stats>> armComparator = (o1, o2) -> o2.getValue().getSamples() - o1.getValue().getSamples();
            List<Map.Entry<C,Stats>> listOfEntries = new ArrayList<>(armStatistics.entrySet());
            Collections.sort(listOfEntries,armComparator);
            for(Map.Entry<C,Stats> entry : listOfEntries) {
                result += entry.getKey()+": "+entry.getValue().realSamplesWithoutInitialization()+" (real) samples, average result "+entry.getValue().getAverageResult()+ System.getProperty("line.separator");
            }
        } else {
            for(Map.Entry<C,Stats> entry : armStatistics.entrySet()) {
                result += entry.getKey()+": "+entry.getValue().realSamplesWithoutInitialization()+" (real) samples, average result "+entry.getValue().getAverageResult()+ System.getProperty("line.separator");
            }
        }
        return result;
    }

}
