package experiments;

import ai.Board;
import ai.Evaluation;
import utils.Util;

import java.util.HashSet;
import java.util.Set;

class PlayerResult  {

    private int[] depthReachedByAnyTreeSearcher;
    private int drawnMatches; //draws are defined from the player perspective as opposed to the color perspective, i.e. as matches where more than 1 player achieved the highest reward. if more than 1 color belonging to the same player achieve the highest reward, it is NOT considered a draw.
    private HashSet<Board> endPositions = new HashSet<>();
    private int numberOfColors;
    private Set<String> opponents;
    private int[] overtimeMatchesForColor;
    private int[] overtimeMatchesForPlayer;
    private String player;
    private int[][] playerPlayedAtMoveNumber;
    private long[][] playerTotalTimeAtMoveNumber;
    private double[] sumOfMatchResultsForColor;
    private double[] sumOfMatchResultsForPlayer;
    private int totalMatchesPlayed;
    private int totalNumberOfTreeSearcherMoves;
    private long totalSumOfTreeDepthsOfTreeSearchers;
    private long totalSumOfTreeSizesOfTreeSearchers;
    private long[] totalTimeForColor;
    private long[] totalTimeForPlayer;

    PlayerResult(String player, int numberOfColors) {
        sumOfMatchResultsForColor = new double[numberOfColors];
        sumOfMatchResultsForPlayer = new double[2];
        overtimeMatchesForColor = new int[numberOfColors];
        overtimeMatchesForPlayer = new int[2];
        playerPlayedAtMoveNumber = new int[2][500];
        playerTotalTimeAtMoveNumber = new long[2][500];
        totalTimeForColor = new long[numberOfColors];
        totalTimeForPlayer = new long[2];
        depthReachedByAnyTreeSearcher = new int[21];
        this.numberOfColors = numberOfColors;
        this.player = player;
        opponents = new HashSet<>();
    }

    public void addDepthReachedByAnyTreeSearcher(int depth, int numberOfMoves) {
        depthReachedByAnyTreeSearcher[depth] += numberOfMoves;
    }

    public void addDraw() {
        this.drawnMatches++;
    }

    public void addMatchPlayed() {
        this.totalMatchesPlayed++;
    }

    public synchronized void addMatchResult(Evaluation matchResult, int[] playerOfColor) {
        addMatchPlayed();
        for(int color=0;color<numberOfColors;color++) {
            double eval = matchResult.getValueForColor(color);
            addMatchResultForColor(color, eval);
            addMatchResultForPlayer(playerOfColor[color], eval);
        }
        if(isDrawBetweenPlayers(matchResult, playerOfColor)) {
            addDraw();
        }
    }

    private void addMatchResultForColor(int color, double matchResult) {
        this.sumOfMatchResultsForColor[color] += matchResult;
    }

    private void addMatchResultForPlayer(int player, double matchResult) {
        this.sumOfMatchResultsForPlayer[player] += matchResult;
    }

    public void addOpponent(String opponent) {
        opponents.add(opponent);
    }

    public void addOvertimeMatchForColor(int color) {
        this.overtimeMatchesForColor[color]++;
    }

    public void addOvertimeMatchForPlayer(int player) {
        this.overtimeMatchesForPlayer[player]++;
    }

    public void addPlayerPlayedAtMoveNumber(int player, int number) {
        playerPlayedAtMoveNumber[player][number]++;
    }

    public void addPlayerTotalTimeAtMoveNumber(int player, int number, long time) {
        playerTotalTimeAtMoveNumber[player][number] += time;
    }

    public void addTotalTimeForColor(int color, long totalTimeForColor) {
        this.totalTimeForColor[color] += totalTimeForColor;
    }

    public void addTotalTimeForPlayer(int player, long totalTimeByPlayer) {
        this.totalTimeForPlayer[player] += totalTimeByPlayer;
    }

    public void addTreeDepths(long sumOfTreeDepths) {
        totalSumOfTreeDepthsOfTreeSearchers += sumOfTreeDepths;
    }

    public void addTreeSearcherMoves(int treeSearcherMoves) {
        totalNumberOfTreeSearcherMoves += treeSearcherMoves;
    }

    public void addTreeSizes(long sumOfTreeSizes) {
        totalSumOfTreeSizesOfTreeSearchers += sumOfTreeSizes;
    }

    public HashSet<Board> getEndPositions() {
        return endPositions;
    }

    public String getPlayer() {
        return player;
    }

    private int getPlayerPlayedAtMoveNumber(int player, int number) {
        return playerPlayedAtMoveNumber[player][number];
    }

    private long getPlayerTotalTimeAtMoveNumber(int player, int number) {
        return playerTotalTimeAtMoveNumber[player][number];
    }

    public int getTotalMatchesPlayed() {
        return totalMatchesPlayed;
    }

    private boolean isDrawBetweenPlayers(Evaluation matchResult, int[] playerOfColor) {
        double bestResultForColorPlayedByPlayer[] = new double[2];
        for(int color = 0; color< numberOfColors; color++) {
            double eval = matchResult.getValueForColor(color);
            if(eval>bestResultForColorPlayedByPlayer[playerOfColor[color]]) {
                bestResultForColorPlayedByPlayer[playerOfColor[color]] = eval;
            }
        }
        if(bestResultForColorPlayedByPlayer[0]==bestResultForColorPlayedByPlayer[1]) {
            return true;
        }
        return false;
    }

    public int numberOfOpponents() {
        return opponents.size();
    }

    private boolean timingConsidered() {
        return totalTimeForPlayer[0] > 0;
    }

    public synchronized String toString(boolean countNumberOfDifferentEndPositions, boolean timingInformation) {
        String result = "";
        //TWO DIFFERENT PLAYERS
        result += player + System.getProperty("line.separator");
        result += "averaged over "+numberOfOpponents()+" opponents:" + System.getProperty("line.separator");
        if (timingConsidered()) {
            result += String.format("winrate %.1f/%d = %.1f%% (confidence interval %.1f%%-%.1f%%, %d games over time, average time %d) --- winrate %.1f/%d = %.1f%% (confidence interval %.1f%%-%.1f%%, %d games over time, average time %d) - difference %.1f%%; draws %.1f%%",
                    sumOfMatchResultsForPlayer[0],
                    totalMatchesPlayed,
                    (sumOfMatchResultsForPlayer[0] / (totalMatchesPlayed)) * 100,
                    Util.lowerConfidenceBound(sumOfMatchResultsForPlayer[0], totalMatchesPlayed, 0.95) * 100,
                    Util.upperConfidenceBound(sumOfMatchResultsForPlayer[0], totalMatchesPlayed, 0.95) * 100,
                    overtimeMatchesForPlayer[0],
                    Math.round((double) totalTimeForPlayer[0] / totalMatchesPlayed),
                    sumOfMatchResultsForPlayer[1],
                    totalMatchesPlayed,
                    (sumOfMatchResultsForPlayer[1] / (totalMatchesPlayed)) * 100,
                    Util.lowerConfidenceBound(sumOfMatchResultsForPlayer[1], totalMatchesPlayed, 0.95) * 100,
                    Util.upperConfidenceBound(sumOfMatchResultsForPlayer[1], totalMatchesPlayed, 0.95) * 100,
                    overtimeMatchesForPlayer[1],
                    Math.round((double) totalTimeForPlayer[1] / totalMatchesPlayed),
                    (sumOfMatchResultsForPlayer[0] / (totalMatchesPlayed)) * 100 - (sumOfMatchResultsForPlayer[1] / (totalMatchesPlayed)) * 100,
                    (((double) (drawnMatches)) / (totalMatchesPlayed)) * 100
            ) + System.getProperty("line.separator");
            result += String.format("(%.1f,x) -= (%.1f,0) += (%.1f,0)",
                    (sumOfMatchResultsForPlayer[0] / (totalMatchesPlayed)) * 100,
                    (sumOfMatchResultsForPlayer[0] / (totalMatchesPlayed)) * 100 - Util.lowerConfidenceBound(sumOfMatchResultsForPlayer[0], totalMatchesPlayed, 0.95) * 100,
                    Util.upperConfidenceBound(sumOfMatchResultsForPlayer[0], totalMatchesPlayed, 0.95) * 100 - (sumOfMatchResultsForPlayer[0] / (totalMatchesPlayed)) * 100
            ) + System.getProperty("line.separator");
            for(int color=0; color<numberOfColors; color++) {
                result += String.format("winrate for color "+color+" %.1f/%d = %.1f%% (%d games over time)",
                        sumOfMatchResultsForColor[color],
                        totalMatchesPlayed,
                        (sumOfMatchResultsForColor[color] / (totalMatchesPlayed)) * 100,
                        overtimeMatchesForColor[color]
                ) + System.getProperty("line.separator");
            }
            if (countNumberOfDifferentEndPositions) {
                result += String.format("number of different end positions: %d\n",
                        endPositions.size()
                ) + System.getProperty("line.separator");
            }
            if (timingInformation) {
                result += "Timing statistics for player one:" + System.getProperty("line.separator");
                for (int moveNumber = 0; moveNumber < 500; moveNumber++) {
                    if (getPlayerPlayedAtMoveNumber(0, moveNumber) > 0) {
                        result += /*moveNumber+", "+*/(getPlayerTotalTimeAtMoveNumber(0, moveNumber) / (double) getPlayerPlayedAtMoveNumber(0, moveNumber)) + System.getProperty("line.separator");
                    }
                }
                result += "Timing statistics for player two:" + System.getProperty("line.separator");
                for (int moveNumber = 0; moveNumber < 500; moveNumber++) {
                    if (getPlayerPlayedAtMoveNumber(1, moveNumber) > 0) {
                        result += /*moveNumber+", "+*/(getPlayerTotalTimeAtMoveNumber(1, moveNumber) / (double) getPlayerPlayedAtMoveNumber(1, moveNumber)) + System.getProperty("line.separator");
                    }
                }
            }
        } else {
            result += String.format("winrate %.1f/%d = %.1f%% (confidence interval %.1f%%-%.1f%%) --- winrate %.1f/%d = %.1f%% (confidence interval %.1f%%-%.1f%%) - difference %.1f%%; draws %.1f%%",
                    sumOfMatchResultsForPlayer[0],
                    totalMatchesPlayed,
                    (sumOfMatchResultsForPlayer[0] / (totalMatchesPlayed)) * 100,
                    Util.lowerConfidenceBound(sumOfMatchResultsForPlayer[0], totalMatchesPlayed, 0.95) * 100,
                    Util.upperConfidenceBound(sumOfMatchResultsForPlayer[0], totalMatchesPlayed, 0.95) * 100,
                    sumOfMatchResultsForPlayer[1],
                    totalMatchesPlayed,
                    (sumOfMatchResultsForPlayer[1] / (totalMatchesPlayed)) * 100,
                    Util.lowerConfidenceBound(sumOfMatchResultsForPlayer[1], totalMatchesPlayed, 0.95) * 100,
                    Util.upperConfidenceBound(sumOfMatchResultsForPlayer[1], totalMatchesPlayed, 0.95) * 100,
                    (sumOfMatchResultsForPlayer[0] / (totalMatchesPlayed)) * 100 - (sumOfMatchResultsForPlayer[1] / (totalMatchesPlayed)) * 100,
                    (((double) (drawnMatches)) / (totalMatchesPlayed)) * 100
            ) + System.getProperty("line.separator");
            result += String.format("(%.1f,x) -= (%.1f,0) += (%.1f,0)",
                    (sumOfMatchResultsForPlayer[0] / (totalMatchesPlayed)) * 100,
                    (sumOfMatchResultsForPlayer[0] / (totalMatchesPlayed)) * 100 - Util.lowerConfidenceBound(sumOfMatchResultsForPlayer[0], totalMatchesPlayed, 0.95) * 100,
                    Util.upperConfidenceBound(sumOfMatchResultsForPlayer[0], totalMatchesPlayed, 0.95) * 100 - (sumOfMatchResultsForPlayer[0] / (totalMatchesPlayed)) * 100
            ) + System.getProperty("line.separator");
            for(int color=0; color<numberOfColors; color++) {
                result += String.format("winrate for color "+color+" %.1f/%d = %.1f%% (%d games over time)",
                        sumOfMatchResultsForColor[color],
                        totalMatchesPlayed,
                        (sumOfMatchResultsForColor[color] / (totalMatchesPlayed)) * 100,
                        overtimeMatchesForColor[color]
                ) + System.getProperty("line.separator");
            }
            if (countNumberOfDifferentEndPositions) {
                result += String.format("number of different end positions: %d",
                        endPositions.size()
                ) + System.getProperty("line.separator");
            }
        }
        if(totalNumberOfTreeSearcherMoves>0) {
            result += String.format("average tree size %.1f",
                    ((double) (totalSumOfTreeSizesOfTreeSearchers)) / (totalNumberOfTreeSearcherMoves)
            ) + System.getProperty("line.separator");
            result += String.format("average tree depth %.2f",
                    ((double) (totalSumOfTreeDepthsOfTreeSearchers)) / (totalNumberOfTreeSearcherMoves)
            ) + System.getProperty("line.separator");
        }
        return result;
    }

}
