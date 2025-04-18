package experiments;

import ai.Board;
import ai.Evaluation;
import utils.Util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ConditionResult  {

    private int[] depthReachedByAnyTreeSearcher;
    private int drawnMatches; //draws are defined from the player perspective as opposed to the color perspective, i.e. as matches where more than 1 player achieved the highest reward. if more than 1 color belonging to the same player achieve the highest reward, it is NOT considered a draw.
    private HashSet<Board> endPositions = new HashSet<>();
    private int numberOfColors;
    private int[] overtimeMatchesForColor;
    private int[] overtimeMatchesForPlayer;
    private int[][] playerPlayedAtMoveNumber;
    private long[][] playerTotalTimeAtMoveNumber;
    private String[] players;
    private double[] sumOfMatchResultsForColor;
    private double[] sumOfMatchResultsForPlayer;
    private int totalMatchesPlayed;
    private int totalNumberOfTreeSearcherMoves;
    private long totalSumOfTreeDepthsOfTreeSearchers;
    private long totalSumOfTreeSizesOfTreeSearchers;
    private long[] totalTimeForColor;
    private long[] totalTimeForPlayer;
    private long[] totalMovesForColor;
    private long[] totalMovesForPlayer;
    private boolean printAverageDurationOfMoveSearch = true;
    private List<Integer> timesForPlayer0;
    private List<Integer> timesForPlayer1;

    public ConditionResult(String playerOne, String playerTwo, int numberOfColors) {
        sumOfMatchResultsForColor = new double[numberOfColors];
        sumOfMatchResultsForPlayer = new double[2];
        overtimeMatchesForColor = new int[numberOfColors];
        overtimeMatchesForPlayer = new int[2];
        players = new String[2];
        playerPlayedAtMoveNumber = new int[2][500];
        playerTotalTimeAtMoveNumber = new long[2][500];
        totalTimeForColor = new long[numberOfColors];
        totalTimeForPlayer = new long[2];
        totalMovesForColor = new long[numberOfColors];
        totalMovesForPlayer = new long[2];
        depthReachedByAnyTreeSearcher = new int[21];
        timesForPlayer0 = new ArrayList<>();
        timesForPlayer1 = new ArrayList<>();

        this.numberOfColors = numberOfColors;
        players[0] = playerOne;
        players[1] = playerTwo;
    }

    public void addDepthReachedByAnyTreeSearcher(int depth, int numberOfMoves) {
        depthReachedByAnyTreeSearcher[depth] += numberOfMoves;
    }

    private void addDraw() {
        this.drawnMatches++;
    }

    private void addMatchPlayed() {
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

    public void addMovesForColor(int color, long totalMovesForColor) {
        this.totalMovesForColor[color] += totalMovesForColor;
    }

    public void addTimeForPlayer(int player, long totalTimeByPlayer) {
        if(player==0) {
            timesForPlayer0.add((int) totalTimeByPlayer);
        } else {
            timesForPlayer1.add((int) totalTimeByPlayer);
        }
    }

    public void addMovesForPlayer(int player, long totalMovesByPlayer) {
        this.totalMovesForPlayer[player] += totalMovesByPlayer;
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

    public double getAverageResultOfPlayer(int player) {
        return sumOfMatchResultsForPlayer[player]/totalMatchesPlayed;
    }

    public HashSet<Board> getEndPositions() {
        return endPositions;
    }

    public String getPlayerOne() {
        return players[0];
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

    private boolean timingConsidered() {
        return totalTimeForPlayer[0] > 0;
    }

    public String toString() {
        return "ConditionResult for: Player one = "+players[0]+", player two = "+players[1];
    }

    public synchronized String toString(boolean countNumberOfDifferentEndPositions, boolean timingInformation, boolean onlyWinrate) {
        String result = "";
        if(onlyWinrate) {
            return ""+Util.round((sumOfMatchResultsForPlayer[0]/totalMatchesPlayed)*100,2);
        }
        if(players[0].equals(players[1])) {
            //ONE PLAYER IN SELF-PLAY
            result += "Self-play of: "+ players[0] + System.getProperty("line.separator");
            for(int color=0; color<numberOfColors; color++) {
                result += String.format("winrate as color "+color+" %.1f/%d = %.1f%% (%d games over time)",
                        sumOfMatchResultsForColor[color],
                        totalMatchesPlayed,
                        (sumOfMatchResultsForColor[color] / (totalMatchesPlayed)) * 100,
                        overtimeMatchesForColor[color]
                ) + System.getProperty("line.separator");
            }
            result += String.format("draws %.1f%%",
                    (((double) (drawnMatches)) / (totalMatchesPlayed)) * 100
            ) + System.getProperty("line.separator");
        } else {
            //TWO DIFFERENT PLAYERS
            result += players[0] + " vs." + System.getProperty("line.separator");
            result += players[1] + System.getProperty("line.separator");
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
                if(printAverageDurationOfMoveSearch) {
                    result += String.format("average search duration for player one: %.1f ms (%d moves); for player two: %.1f ms (%d moves)",
                            totalTimeForPlayer[0]/(double)totalMovesForPlayer[0],
                            totalMovesForPlayer[0],
                            totalTimeForPlayer[1]/(double)totalMovesForPlayer[1],
                            totalMovesForPlayer[1]
                    ) + System.getProperty("line.separator");
                    com.google.common.math.Stats stats0 = com.google.common.math.Stats.of(timesForPlayer0);
                    com.google.common.math.Stats stats1 = com.google.common.math.Stats.of(timesForPlayer1);
                    result += String.format("average search duration for player one: %.1f ms (standard deviation %.1f); for player two: %.1f ms (standard deviation %.1f)",
                            stats0.mean(),
                            stats0.sampleStandardDeviation(),
                            stats1.mean(),
                            stats1.sampleStandardDeviation()
                    ) + System.getProperty("line.separator");
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
                if(printAverageDurationOfMoveSearch) {
                    result += String.format("average search duration for player one: %.1f ms; for player two: %.1f ms",
                            totalTimeForPlayer[0]/(double)totalMovesForPlayer[0],
                            totalTimeForPlayer[1]/(double)totalMovesForPlayer[1]
                    ) + System.getProperty("line.separator");
                }
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
