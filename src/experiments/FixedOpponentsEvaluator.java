package experiments;

import ai.Evaluation;

import java.util.*;

import static experiments.ExperimentConfiguration.RESULTS_DIRECTORY;
import static experiments.ExperimentConfiguration.numberOfRandomOpeningTurnsPerColor;
import static utils.Util.findGameInPlayerString;
import static utils.Util.playerAssignmentsToColorsForNumbersOfColors;

//in order to evaluate a player, runs a full match against the stored fixed opponents. makes sure to (randomly) play batches of all assignments of players to colors when called repeatedly with the same player; OR plays all of them at the first call when batchEvaluation=true
public class FixedOpponentsEvaluator implements FitnessEvaluator {

    private List<String> fixedOpponents;
    private int numberOfColorsInGame;
    private Map<String, List<Integer>> playersToUnfinishedMatchIndices;
    private Random random;
    private boolean batchEvaluation = true;

    public FixedOpponentsEvaluator(List<String> fixedOpponents, Random random) {
        this.fixedOpponents = new ArrayList<>(fixedOpponents);
        this.random = random;
        playersToUnfinishedMatchIndices = new HashMap<>();
        this.numberOfColorsInGame = findGameInPlayerString(this.fixedOpponents.get(0)).getNumberOfColors();
    }

    @Override
    public double evaluateFitness(String player) {
        if(batchEvaluation) {
            double totalEvaluation = 0;
            for(int nextMatchIndex = 0; nextMatchIndex<playerAssignmentsToColorsForNumbersOfColors.get(numberOfColorsInGame).length * fixedOpponents.size(); nextMatchIndex++) {
                totalEvaluation += runMatch(player, nextMatchIndex);
            }
            return totalEvaluation/(playerAssignmentsToColorsForNumbersOfColors.get(numberOfColorsInGame).length * fixedOpponents.size());
        } else {
            int nextMatchIndex = nextMatchIndex(player);
            return runMatch(player, nextMatchIndex);
        }
    }

    private int nextMatchIndex(String player) {
        int nextMatchIndex;
        synchronized (playersToUnfinishedMatchIndices) {
            if (!playersToUnfinishedMatchIndices.containsKey(player) || playersToUnfinishedMatchIndices.get(player).isEmpty()) {
                List<Integer> unfinishedMatchIndices = new ArrayList<>();
                for(int matchIndex=0; matchIndex<playerAssignmentsToColorsForNumbersOfColors.get(numberOfColorsInGame).length * fixedOpponents.size(); matchIndex++) {
                    unfinishedMatchIndices.add(matchIndex);
                }
                playersToUnfinishedMatchIndices.put(player, unfinishedMatchIndices);
            }
            nextMatchIndex = playersToUnfinishedMatchIndices.get(player).remove(random.nextInt(playersToUnfinishedMatchIndices.get(player).size()));
        }
        return nextMatchIndex;
    }

    @Override
    public void reset() {
        synchronized (playersToUnfinishedMatchIndices) {
            playersToUnfinishedMatchIndices = new HashMap<>();
        }
    }

    @Override
    public int matchesPerEvaluation() {
        if(batchEvaluation) {
            return playerAssignmentsToColorsForNumbersOfColors.get(numberOfColorsInGame).length * fixedOpponents.size();
        } else {
            return 1;
        }
    }

    private double runMatch(String playerOne, int nextMatchIndex) {
        int assignmentOfPlayersToColors = nextMatchIndex/fixedOpponents.size();
        String playerTwo = fixedOpponents.get(nextMatchIndex%fixedOpponents.size());
        String matchFileName = RESULTS_DIRECTORY + "match" + random.nextInt(1000000) + System.currentTimeMillis() + ".game";
        int[] playerOfColor = playerAssignmentsToColorsForNumbersOfColors.get(numberOfColorsInGame)[assignmentOfPlayersToColors];
        Match match = new Match(matchFileName, playerOne, playerTwo, playerOfColor);
        setNumberOfRandomOpeningTurns(match);
        Evaluation matchResult = match.play();
        return matchResult.getEvalForPlayer(playerOfColor, 0);
    }

    private void setNumberOfRandomOpeningTurns(Match match) {
        long randomSeed = 0;
        if(numberOfRandomOpeningTurnsPerColor>0) {
            Random random = new Random();
            randomSeed = random.nextLong();
            match.setNumberOfRandomOpeningTurnsPerColor(numberOfRandomOpeningTurnsPerColor, randomSeed);
        }
    }

    public String toString() {
        String result = "Evaluator: FixedOpponentsEvaluator with opponents=";
        for(String opponent : fixedOpponents) {
            result += opponent +", ";
        }
        result = result.substring(0,result.length()-2);
        return result;
    }

}
