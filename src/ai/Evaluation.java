package ai;

import static utils.Util.round;

public class Evaluation {

    private boolean hasQuantiles;
    private double[] lowerQuantileForColors;
    private double[] upperQuantileForColors;
    private double[] valueForColor;

    public Evaluation(double[] valueForColor) {
        this.valueForColor = valueForColor;
        this.hasQuantiles = false;
    }

    public Evaluation copy() {
        double[] newValueForColor = new double[valueForColor.length];
        System.arraycopy(valueForColor, 0, newValueForColor, 0, valueForColor.length);
        return new Evaluation(newValueForColor);
    }

    public double getEvalForPlayer(int[] playerOfColor, int player) {
        double result = 0;
        for(int color=0;color<playerOfColor.length;color++) {
            if(playerOfColor[color]==player) {
                double eval = getValueForColor(color);
                result += eval;
            }
        }
        return result;
    }

    public double getLowerQuantileForColor(int color) {
        return lowerQuantileForColors[color];
    }

    public double[] getLowerQuantileForColors() {
        return lowerQuantileForColors;
    }

    public void setLowerQuantileForColors(double[] lowerQuantileForColors) {
        this.lowerQuantileForColors = lowerQuantileForColors;
    }

    public int getNumberOfColors() { return valueForColor.length; }

    public double getUpperQuantileForColor(int color) {
        return upperQuantileForColors[color];
    }

    public double[] getUpperQuantileForColors() {
        return upperQuantileForColors;
    }

    public void setUpperQuantileForColors(double[] upperQuantileForColors) {
        this.upperQuantileForColors = upperQuantileForColors;
    }

    public double getValueForColor(int color) {
        return valueForColor[color];
    }

    public double[] getValueForColor() { return valueForColor; }

    public void setValueForColor(double[] valueForColor) {
        this.valueForColor = valueForColor;
    }

    public boolean hasQuantiles() {
        return hasQuantiles;
    }

    public void setHasQuantiles(boolean hasQuantiles) {
        this.hasQuantiles = hasQuantiles;
    }

    public void setQuantiles( double[] lowerQuantileForColors, double[] upperQuantileForColors) {
        this.hasQuantiles = true;
        this.lowerQuantileForColors = lowerQuantileForColors;
        this.upperQuantileForColors = upperQuantileForColors;
    }

    public String toCSV() {
        String result = "";
        for(int i = 0; i< valueForColor.length; i++) {
            result += round(valueForColor[i],5)+",";
        }
        return result;
    }

    public String toString() {
        String result = "[";
        for(int i = 0; i< valueForColor.length-1; i++) {
            result += "color "+i+": "+round(valueForColor[i],5)+", ";
        }
        result += "color "+(valueForColor.length-1)+": "+round(valueForColor[valueForColor.length-1],5);
        result += "]";
        return result;
    }

}
