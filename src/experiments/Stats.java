package experiments;

public class Stats {

    private double averageResult;
    private double averageSquaredResult;
    private int samples;
    private int initialSamples;

    public Stats(double averageResult, double averageSquaredResult, int samples) {
        this.averageResult = averageResult;
        this.averageSquaredResult = averageSquaredResult;
        this.samples = samples;
        this.initialSamples = samples;
    }

    //TODO synchronized needed?
    public synchronized void addResult(double result) {
        averageResult = (averageResult * samples + result)/(samples+1);
        averageSquaredResult = (averageSquaredResult * samples + result*result)/(samples+1);
        samples++;
    }

    public double getAverageResult() {
        return averageResult;
    }

    public double getAverageSquaredResult() {
        return averageSquaredResult;
    }

    public int getSamples() {
        return samples;
    }

    public int realSamplesWithoutInitialization() {
        return samples-initialSamples;
    }

    public synchronized void removeResult(double result) {
        averageResult = (averageResult * samples - result)/(samples-1);
        averageSquaredResult = (averageSquaredResult * samples - result*result)/(samples-1);
        samples--;
    }

    public String toString() {
        return "samples: "+realSamplesWithoutInitialization()+", avg result: "+averageResult;
    }

}
