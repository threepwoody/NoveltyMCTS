package ai.terminalboardevaluation;

import ai.Board;
import ai.Evaluation;
import ai.TerminalBoardEvaluator;

import java.util.HashSet;
import java.util.Set;

public class PiececountTerminalBoardEvaluator extends BasicTerminalBoardEvaluator {

    private double maxBonusAsFractionOfDifferenceBetweenResults = 0.2;

    public PiececountTerminalBoardEvaluator(int numberOfColors) {
        super(numberOfColors);
    }

    @Override
    public TerminalBoardEvaluator copy() {
        PiececountTerminalBoardEvaluator copy = new PiececountTerminalBoardEvaluator(getNumberOfColors());
        copy.setMaxBonusAsFractionOfDifferenceBetweenResults(maxBonusAsFractionOfDifferenceBetweenResults);
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (! super.equals(o)) return false;
        if (o == null || getClass() != o.getClass()) return false;
        PiececountTerminalBoardEvaluator that = (PiececountTerminalBoardEvaluator) o;
        return maxBonusAsFractionOfDifferenceBetweenResults == that.maxBonusAsFractionOfDifferenceBetweenResults;
    }

    @Override
    public Evaluation evalOfTerminalBoard(Board terminalBoard) { //TODO not debugged for multiplayer games
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
        int[] numberOfPieces = new int[numberOfColors];
        int totalNumberOfPieces = 0;
        for(int color=0; color<numberOfColors; color++) {
            numberOfPieces[color] = ((PieceCountBoard)terminalBoard).numberOfPieces(color);
            numberOfPieces[color] = Math.max(numberOfPieces[color], 1);
            totalNumberOfPieces += numberOfPieces[color];
        }
        double maxMalus = (1.0/winners-1.0/(winners+1))/(1.0/maxBonusAsFractionOfDifferenceBetweenResults);
        double maxBonus = maxMalus*winners/losers;
        for(int color = 0; color<numberOfColors; color++) { //TODO doesn't work for games with more outcomes than wins and draws
            if(winningColors.contains(color)) {
                evalForColor[color] = (1.0/winners) - maxMalus*((totalNumberOfPieces-numberOfPieces[color])/(double)totalNumberOfPieces);
            } else if(winningColors.isEmpty()) {
                evalForColor[color] = 1.0/numberOfColors;
            } else {
                evalForColor[color] = 0 + maxBonus*(numberOfPieces[color]/(double)totalNumberOfPieces);
            }
        }
        return new Evaluation(evalForColor);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Double.valueOf(maxBonusAsFractionOfDifferenceBetweenResults).hashCode();
        return result;
    }

    public void setMaxBonusAsFractionOfDifferenceBetweenResults(double maxBonusAsFractionOfDifferenceBetweenResults) {
        this.maxBonusAsFractionOfDifferenceBetweenResults = maxBonusAsFractionOfDifferenceBetweenResults;
    }

}
