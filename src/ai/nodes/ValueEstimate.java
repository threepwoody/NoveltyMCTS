package ai.nodes;

import ai.Evaluation;

import java.util.Arrays;

import static utils.Util.round;

public class ValueEstimate {

    private double[] averageResultForColor;
    private double[] averageSquaredResultForColor;
    private double[] initialResultForColor;
    private int initialSamples;
    private int samples;

    public ValueEstimate(SearchNodeBuilder builder, int numberOfColors) {
        averageResultForColor = new double[numberOfColors];
        averageSquaredResultForColor = new double[numberOfColors];
        initialResultForColor = new double[numberOfColors];
        if(builder!=null) {
            for (int color = 0; color < numberOfColors; color++) {
                averageResultForColor[color] = builder.getInitialResultForColor(color);
                initialResultForColor[color] = builder.getInitialResultForColor(color);
                averageSquaredResultForColor[color] = builder.getInitialSquaredResultForColor(color);
            }
            this.initialSamples = builder.getInitialSamples();
        }
        this.samples = initialSamples;
    }

    public ValueEstimate(ValueEstimate other) {
        initialResultForColor = Arrays.copyOf(other.initialResultForColor, other.initialResultForColor.length);
        averageResultForColor = Arrays.copyOf(other.averageResultForColor, other.averageResultForColor.length);
        averageSquaredResultForColor = Arrays.copyOf(other.averageSquaredResultForColor, other.averageSquaredResultForColor.length);
        this.initialSamples = other.initialSamples;
        this.samples = other.samples;
    }

    public String averageResultsToCSV() {
        String result = "";
        for(int i = 0; i< averageResultForColor.length; i++) {
            result += round(averageResultForColor[i],5)+",";
        }
        return result;
    }

    public double getAverageResultForColor(int color) {
        return averageResultForColor[color];
    }

    public double[] getAverageResults() { return averageResultForColor; }

    public double getAverageSquaredResultForColor(int color) {
        return averageSquaredResultForColor[color];
    }

    public int getSamples() {
        return samples;
    }

    public void setSamples(int newSamples) {
        samples = newSamples;
    }

    public void recordEvaluation(Evaluation evaluation) {
        samples++;
        for(int color=0; color<evaluation.getNumberOfColors(); color++) {
            double evalForColor = round(evaluation.getValueForColor(color),10);
            averageResultForColor[color] = averageResultForColor[color] + (evalForColor-averageResultForColor[color])/samples;
            averageSquaredResultForColor[color] = averageSquaredResultForColor[color] + (evalForColor*evalForColor-averageSquaredResultForColor[color])/samples;
        }
    }

    public void recordEvaluation(Evaluation evaluation, int samples) {
        for(int color=0; color<evaluation.getNumberOfColors(); color++) {
            double evalForColor = round(evaluation.getValueForColor(color),10);
            averageResultForColor[color] = (averageResultForColor[color]*this.samples + evalForColor*samples)/(double)(this.samples + samples);
            averageSquaredResultForColor[color] = (averageSquaredResultForColor[color]*this.samples + evalForColor*evalForColor*samples)/(double)(this.samples + samples);
        }
        this.samples += samples;
    }

}
