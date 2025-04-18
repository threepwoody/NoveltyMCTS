package experiments;

import ai.BasicMCTS;
import ai.Evaluation;
import ai.Move;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class MatchSummaryLogger {

    private String fileName;
    private PrintWriter out;
    private long lastMoveRequest;
    private long lastMoveReceipt;
    private long startOfGame;
    private boolean loggingTreeSizes = false;
    private Match match;

    public MatchSummaryLogger(Match match, String fileName) {
        this.match = match;
        this.fileName = fileName;
    }

    public void close() {
        out.flush();
        out.close();
    }

    public void logMatchEnd() {
        long endOfMatch = System.currentTimeMillis();
        out.println("Game over!");
        out.println(match.getBoard());
        out.println();
        out.println("total game time: " + (endOfMatch - startOfGame));
        for(int color=0;color<match.getPlayerForColor().length;color++) {
            out.println("for this match, color "+color+" needed msec: "+ match.getDurationForColor()[color]);
        }
        for(int color=0;color<match.getPlayerForColor().length;color++) {
            if (match.getPlayerForColor()[color] instanceof BasicMCTS) {
                out.println("color " + color + " did " + match.getMovesForColor()[color] + " moves, with on average " + (match.getPlayoutsForColor()[color] / (double) match.getMovesForColor()[color]) + " simulations and " + (match.getDurationForColor()[color] / (double)match.getMovesForColor()[color]) + " milliseconds.");
            } else {
                out.println("color " + color + " did " + match.getMovesForColor()[color] + " moves, with on average " + (match.getDurationForColor()[color] / (double) match.getMovesForColor()[color]) + " milliseconds.");
            }
        }
        if(loggingTreeSizes) {
            logTreeSizes();
        }
        out.println();
        Evaluation evaluation = match.getBoard().evalOfTerminalBoard();
        out.println("match results:");
        for(int color=0;color<match.getPlayerForColor().length;color++) {
            out.println("for color "+color+": "+evaluation.getValueForColor(color));
        }
        out.close();
    }

    private void logTreeSizes() {
        out.println();
        out.println("moves of all tree searchers: "+match.getMovesByAllTreeSearchers());
        out.println("total tree size of all tree searchers: "+match.getTotalTreeSizeOfAllTreeSearchers());
        out.println("total tree depth of all tree searchers: "+match.getTotalTreeDepthOfAllTreeSearchers());
        for(int depth=1;depth<=20;depth++) {
            out.println("searches that completed depth "+depth+": "+match.getDepthReachedByAnyTreeSearcher()[depth]);
        }
    }

    public void logMatchStart() {
        File file = new File(fileName);
        file.getParentFile().mkdirs();
        try {
            out = new PrintWriter(file);
            out.println("player 0: " + match.getPlayerNames()[0]);
            out.println("player 1: " + match.getPlayerNames()[1]);
            for (int color = 0; color < match.getPlayerNumberForColor().length; color++) {
                out.println("color " + color + ": player " + match.getPlayerNumberForColor()[color]);
            }
            out.println();
            out.println(match.getBoard());
            out.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        startOfGame = System.currentTimeMillis();
    }

    public void logMoveReceived(Move move) {
        lastMoveReceipt = System.currentTimeMillis();
        out.println("move received: " + lastMoveReceipt);
        out.println("move by color " + match.getBoard().getColorToPlay() + ": " + move);
        out.flush();
    }

    public void logRequestingMove() {
        lastMoveRequest = System.currentTimeMillis();
        out.println("before requesting move: " + lastMoveRequest);
        out.flush();
    }
}
