package ai.terminalboardevaluation;

import ai.Board;
import ai.Evaluation;
import ai.TerminalBoardEvaluator;

import java.util.HashSet;
import java.util.Set;

public class MobilityTerminalBoardEvaluator extends BasicTerminalBoardEvaluator {

    private double maxBonusAsFractionOfDifferenceBetweenResults = 0.2;
    private int maxMovesPerPosition;

    public MobilityTerminalBoardEvaluator(int numberOfColors, int maxMovesPerPosition) {
        super(numberOfColors);
        this.maxMovesPerPosition = maxMovesPerPosition;
    }

    @Override
    public TerminalBoardEvaluator copy() {
        MobilityTerminalBoardEvaluator copy = new MobilityTerminalBoardEvaluator(getNumberOfColors(), maxMovesPerPosition);
        copy.setMaxBonusAsFractionOfDifferenceBetweenResults(maxBonusAsFractionOfDifferenceBetweenResults);
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (! super.equals(o)) return false;
        if (o == null || getClass() != o.getClass()) return false;
        MobilityTerminalBoardEvaluator that = (MobilityTerminalBoardEvaluator) o;
        return maxBonusAsFractionOfDifferenceBetweenResults == that.maxBonusAsFractionOfDifferenceBetweenResults;
    }

    @Override
    public Evaluation evalOfTerminalBoard(Board terminalBoard) {
        int numberOfColors = getNumberOfColors();
        double[] evalForColor = terminalBoard.getTerminalValues();
        Set<Integer> winningColors = new HashSet<>();
        for(int color = 0; color<numberOfColors; color++) {
            if(evalForColor[color] > 0) {
                winningColors.add(color);
            }
        }
        int winners = winningColors.size();
        if(winners==numberOfColors) {
            return new Evaluation(evalForColor);
        }
        int losers = numberOfColors-winners;
        double maxMalus = 0.2;
        double maxBonus = maxMalus*winners/losers;
        int totalNumberOfMovesOfWinners = 0;
        for(int color = 0; color<numberOfColors; color++) {
            if(winningColors.contains(color)) {
                totalNumberOfMovesOfWinners += terminalBoard.getLegalMovesFor(color).size();
            }
        }
        for(int color = 0; color<numberOfColors; color++) { //TODO doesn't work for games with more outcomes than wins and draws
            if(winningColors.contains(color)) {
                evalForColor[color] = (1.0/winners) - maxMalus*(10.0-Math.min(10,terminalBoard.getLegalMovesFor(color).size()))/10.0;
            } else if(winningColors.isEmpty()) {
                evalForColor[color] = 1.0/numberOfColors;
            } else {
                evalForColor[color] = 0 + maxBonus*(winners*10.0-Math.min(winners*10,totalNumberOfMovesOfWinners))/(winners*10.0);
            }
        }
        return new Evaluation(evalForColor);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Double.valueOf(maxBonusAsFractionOfDifferenceBetweenResults).hashCode();
        result = 31 * result + maxMovesPerPosition;
        return result;
    }

    public void setMaxBonusAsFractionOfDifferenceBetweenResults(double maxBonusAsFractionOfDifferenceBetweenResults) {
        this.maxBonusAsFractionOfDifferenceBetweenResults = maxBonusAsFractionOfDifferenceBetweenResults;
    }

}
