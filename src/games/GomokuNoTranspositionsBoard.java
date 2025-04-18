package games;

import ai.*;
import experiments.AlphaZero.AlphaZeroBoard;
import utils.MalformedMoveException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static ai.Game.BLACK;
import static ai.Game.WHITE;
import static games.Gomoku.FIVE_IN_A_ROW_OFFSETS_FOR_GIVEN_SQUARE;

public class GomokuNoTranspositionsBoard extends OnePieceTypeTwoPlayerBoard implements AlphaZeroBoard, RectangularIntBoard {

    //Contains WHITE_PIECE, BLACK_PIECE or NO_PIECE for the individual squares.
    private int[][] board;
    private GomokuMove lastMove;
    private long[] movesHashForColor;
    private long[] piecesHashForColor;
    private int[] piecesOfColor;
    //one distinct key for each possible move at each possible turn. this makes transpositions impossible and turns the game graph into a tree
    //dimensions to look up keys: [turn][x][y]
    private long[][][] zobristKeys;

    public GomokuNoTranspositionsBoard(Gomoku game) {
        super(game);
        zobristKeys = game.getZobristKeys();
        initialiseStartingBoard(game.getBoardWidth(), game.getBoardHeight(), game.getStartingColor());
        setHashOfStartingBoard();
    }

    public GomokuNoTranspositionsBoard(Gomoku game, String board, boolean isFileName) {
        super(game);
        zobristKeys = game.getZobristKeys();
        if(isFileName) {
            try {
                String boardInFile = new String(Files.readAllBytes(Paths.get(board)));
                initializeBoardFromString(boardInFile, game.getBoardWidth(), game.getBoardHeight());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            initializeBoardFromString(board, game.getBoardWidth(), game.getBoardHeight());
        }
        setHashOfStartingBoard();
        isTerminalBoard();
    }

    private static final int pieceToPlayer(int piece) {
        if(piece==WHITE_PIECE) return WHITE;
        if(piece==BLACK_PIECE) return BLACK;
        else return Game.NO_COLOR;
    }

    @Override
    public Map<Integer, String> CSVOfAllBoardSymmetries() {
        Map<Integer, String> result = new HashMap<>();
        String plainBoard = "";
        for(int y=board[0].length-1;y>=0;y--) {
            for(int x=0;x<board.length;x++) {
                plainBoard += board[x][y]==WHITE_PIECE?"1,":"0,";
            }
        }
        for(int y=board[0].length-1;y>=0;y--) {
            for(int x=0;x<board.length;x++) {
                plainBoard += board[x][y]==BLACK_PIECE?"1,":"0,";
            }
        }
        plainBoard += getColorToPlay()==WHITE ? "1," : "0,";
        plainBoard += getColorToPlay()==BLACK ? "1," : "0,";
        result.put(1, plainBoard);

        String horizontalSymmetry = "";
        for(int y=0;y<board[0].length;y++) {
            for(int x=0;x<board.length;x++) {
                horizontalSymmetry += board[x][y]==WHITE_PIECE?"1,":"0,";
            }
        }
        for(int y=0;y<board[0].length;y++) {
            for(int x=0;x<board.length;x++) {
                horizontalSymmetry += board[x][y]==BLACK_PIECE?"1,":"0,";
            }
        }
        horizontalSymmetry += getColorToPlay()==WHITE ? "1," : "0,";
        horizontalSymmetry += getColorToPlay()==BLACK ? "1," : "0,";
        result.put(2, horizontalSymmetry);

        String verticalSymmetry = "";
        for(int y=board[0].length-1;y>=0;y--) {
            for(int x=board.length-1;x>=0;x--) {
                verticalSymmetry += board[x][y]==WHITE_PIECE?"1,":"0,";
            }
        }
        for(int y=board[0].length-1;y>=0;y--) {
            for(int x=board.length-1;x>=0;x--) {
                verticalSymmetry += board[x][y]==BLACK_PIECE?"1,":"0,";
            }
        }
        verticalSymmetry += getColorToPlay()==WHITE ? "1," : "0,";
        verticalSymmetry += getColorToPlay()==BLACK ? "1," : "0,";
        result.put(3, verticalSymmetry);

        String firstDiagonalSymmetry = "";
        for(int x=board.length-1;x>=0;x--) {
            for(int y=0;y<board[0].length;y++) {
                firstDiagonalSymmetry += board[x][y]==WHITE_PIECE?"1,":"0,";
            }
        }
        for(int x=board.length-1;x>=0;x--) {
            for(int y=0;y<board[0].length;y++) {
                firstDiagonalSymmetry += board[x][y]==BLACK_PIECE?"1,":"0,";
            }
        }
        firstDiagonalSymmetry += getColorToPlay()==WHITE ? "1," : "0,";
        firstDiagonalSymmetry += getColorToPlay()==BLACK ? "1," : "0,";
        result.put(4, firstDiagonalSymmetry);

        String secondDiagonalSymmetry = "";
        for(int x=0;x<board.length;x++) {
            for(int y=board[0].length-1;y>=0;y--) {
                secondDiagonalSymmetry += board[x][y]==WHITE_PIECE?"1,":"0,";
            }
        }
        for(int x=0;x<board.length;x++) {
            for(int y=board[0].length-1;y>=0;y--) {
                secondDiagonalSymmetry += board[x][y]==BLACK_PIECE?"1,":"0,";
            }
        }
        secondDiagonalSymmetry += getColorToPlay()==WHITE ? "1," : "0,";
        secondDiagonalSymmetry += getColorToPlay()==BLACK ? "1," : "0,";
        result.put(5, secondDiagonalSymmetry);

        String rotational180Symmetry = "";
        for(int y=0;y<board[0].length;y++) {
            for(int x=board.length-1;x>=0;x--) {
                rotational180Symmetry += board[x][y]==WHITE_PIECE?"1,":"0,";
            }
        }
        for(int y=0;y<board[0].length;y++) {
            for(int x=board.length-1;x>=0;x--) {
                rotational180Symmetry += board[x][y]==BLACK_PIECE?"1,":"0,";
            }
        }
        rotational180Symmetry += getColorToPlay()==WHITE ? "1," : "0,";
        rotational180Symmetry += getColorToPlay()==BLACK ? "1," : "0,";
        result.put(6, rotational180Symmetry);

        String rotational90Symmetry = "";
        for(int x=board.length-1;x>=0;x--) {
            for(int y=board[0].length-1;y>=0;y--) {
                rotational90Symmetry += board[x][y]==WHITE_PIECE?"1,":"0,";
            }
        }
        for(int x=board.length-1;x>=0;x--) {
            for(int y=board[0].length-1;y>=0;y--) {
                rotational90Symmetry += board[x][y]==BLACK_PIECE?"1,":"0,";
            }
        }
        rotational90Symmetry += getColorToPlay()==WHITE ? "1," : "0,";
        rotational90Symmetry += getColorToPlay()==BLACK ? "1," : "0,";
        result.put(7, rotational90Symmetry);

        String rotational270Symmetry = "";
        for(int x=0;x<board.length;x++) {
            for(int y=0;y<board[0].length;y++) {
                rotational270Symmetry += board[x][y]==WHITE_PIECE?"1,":"0,";
            }
        }
        for(int x=0;x<board.length;x++) {
            for(int y=0;y<board[0].length;y++) {
                rotational270Symmetry += board[x][y]==BLACK_PIECE?"1,":"0,";
            }
        }
        rotational270Symmetry += getColorToPlay()==WHITE ? "1," : "0,";
        rotational270Symmetry += getColorToPlay()==BLACK ? "1," : "0,";
        result.put(8, rotational270Symmetry);
        return result;
    }

    @Override
    public void copyDataFrom(Board thatboard) {
        super.copyDataFrom(thatboard);
        GomokuNoTranspositionsBoard that = (GomokuNoTranspositionsBoard) thatboard;
        for(int x=0;x<board.length;x++) {
            for(int y=0;y<board[0].length;y++) {
                board[x][y] = that.board[x][y];
            }
        }
        lastMove = that.lastMove;
        for(int i=0; i<movesHashForColor.length; i++) {
            movesHashForColor[i] = that.movesHashForColor[i];
        }
        for(int i=0; i<piecesHashForColor.length; i++) {
            piecesHashForColor[i] = that.piecesHashForColor[i];
        }
        for(int i=0; i< piecesOfColor.length; i++) {
            piecesOfColor[i] = that.piecesOfColor[i];
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (! super.equals(o)) return false;
        if (o == null || getClass() != o.getClass()) return false;
        GomokuNoTranspositionsBoard that = (GomokuNoTranspositionsBoard) o;
        return Arrays.deepEquals(board, that.board);
    }

    protected void findLegalMoves() {
        getLegalMovesFor(WHITE).clear();
        getLegalMovesFor(BLACK).clear();
        for(int x=0;x<board.length;x++) {
            for(int y=0;y<board[0].length;y++) {
                int piece = board[x][y];
                if(piece==NO_PIECE) {
                    getLegalMovesFor(BLACK).add(new GomokuMove(x,y,BLACK));
                    getLegalMovesFor(WHITE).add(new GomokuMove(x,y,WHITE));
                }
            }
        }
    }

    @Override
    public int[][] getBoard() {
        return board;
    }

    public int getHeight() {
        return board[0].length;
    }

    /*
     * returns OFFBOARD for squares outside the board, otherwise board[x][y] (NO_PIECE for empty squares)
     */
    public int getSquare(int x, int y) {
        if(x<0 || x>board.length-1 || y<0 || y>board[0].length-1) {
            return OFF_BOARD;
        }
        return board[x][y];
    }

    public int getWidth() {
        return board.length;
    }

    public long[][][] getZobristKeys() {
        return zobristKeys;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Arrays.deepHashCode(board);
        return result;
    }

    private void initialiseStartingBoard(int width, int height, int startingColor) {
        board = new int[width][height];
        for(int i=0;i<width;i++) {
            for(int j=0;j<height;j++) {
                board[i][j] = NO_PIECE;
            }
        }
        setColorToPlay(startingColor);
        findLegalMoves();
        lastMove = null;
        movesHashForColor = new long[getNumberOfColors()];
        piecesHashForColor = new long[getNumberOfColors()];
        piecesOfColor = new int[2];
    }

    //TODO debug
    private void initializeBoardFromString(String boardString, int boardWidth, int boardHeight) {
        board = new int[boardWidth][boardHeight];
        InputStreamReader input = new InputStreamReader(new ByteArrayInputStream(boardString.getBytes()));
        BufferedReader lineInput = new BufferedReader(input);
        piecesOfColor = new int[2];

        try {
            for(int y=boardHeight-1;y>=0;y--) {
                for(int x=0;x<boardWidth;x++) {
                    char c = (char) input.read();
                    while (c == ' ' || c == '\r' || c == '\n') {
                        c = (char) input.read();
                    }
                    if (c == 'O' || c == 'W' || c == 'w') {
                        board[x][y] = WHITE_PIECE;
                        piecesOfColor[WHITE]++;
                    } else if (c == 'X' || c == 'B' || c == 'b') {
                        board[x][y] = BLACK_PIECE;
                        piecesOfColor[BLACK]++;
                    } else if (c == '.') {
                    }
                }
            }
            lineInput.readLine();
            lineInput.readLine();
            char toplay = (char) input.read();
            if (toplay == '0' || toplay == 's' || toplay == 'S' || toplay == 'B' || toplay == 'b' || toplay == 'X' || toplay == 'x') {
                setColorToPlay(BLACK);
            } else if (toplay == '1' || toplay == 'w' || toplay == 'W' || toplay == 'O' || toplay == 'o') {
                setColorToPlay(WHITE);
            } else {
                String line = lineInput.readLine();
                if (line.contains("(X)") || line.contains("Black") || line.contains("black")) {
                    setColorToPlay(BLACK);
                } else if (line.contains("(O)") || line.contains("White") || line.contains("white")) {
                    setColorToPlay(WHITE);
                }
            }
            input.close();
        } catch (IOException e) {
            System.err.println("Cannot read board from String: "+boardString);
            e.printStackTrace();
            System.exit(1);
        }

        findLegalMoves();
        lastMove = null;
        movesHashForColor = new long[getNumberOfColors()];
        piecesHashForColor = new long[getNumberOfColors()];
    }

    private boolean isDraw() {
        if((piecesOfColor[WHITE]+piecesOfColor[BLACK])==getHeight()*getWidth()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isTerminalBoard() {
        if(isDraw()) {
            Set<Integer> winningColors = new HashSet<>();
            for (int color = 0; color < getNumberOfColors(); color++) {
                winningColors.add(color);
            }
            setWinners(winningColors);
            return true;
        } else {
            int winner = winner();
            if (winner != Game.NO_COLOR) {
                setWinner(winner);
                return true;
            }
        }
        return false;
    }

    @Override
    public void play(Move move) {
        GomokuMove m = (GomokuMove) move;
        //update board & legal moves
        int movingPiece = pieceOfColor(getColorToPlay());
        board[m.getX()][m.getY()] = movingPiece;
        piecesOfColor[getColorToPlay()]++;
        updateLegalMoves(m);
        //update hashes
        setHash(getHash() ^ zobristKeys[getTurn()][m.getX()][m.getY()]);
        movesHashForColor[m.getColorOfMove()] ^= zobristKeys[getTurn()][m.getX()][m.getY()];
        piecesHashForColor[getColorToPlay()] ^= zobristKeys[1][m.getX()][m.getY()];
        setColorToPlay(nextColor());
        setTurn(getTurn()+1);
        lastMove = m;
    }

    protected void updateLegalMoves(GomokuMove m) {
        getLegalMovesFor(WHITE).remove(new GomokuMove(m.getX(),m.getY(),WHITE));
        getLegalMovesFor(BLACK).remove(new GomokuMove(m.getX(),m.getY(),BLACK));
    }

    private void setHashOfStartingBoard() {
        long startPositionHash = 0l;
        setHash(startPositionHash);
        for(int x=0;x<board.length;x++) {
            for(int y=0;y<board[0].length;y++) {
                if(board[x][y]==WHITE_PIECE) {
                    piecesHashForColor[WHITE] ^= zobristKeys[1][x][y];
                } else if(board[x][y]==BLACK_PIECE) {
                    piecesHashForColor[BLACK] ^= zobristKeys[1][x][y];
                }
            }
        }
    }

    protected void setPiecesHashForColor(int color, long hash) {
        piecesHashForColor[color] = hash;
    }

    @Override
    public Move toMove(String moveString) throws MalformedMoveException {
        return new GomokuMove(moveString, getColorToPlay());
    }

    @Override
    public String toString() {
        String result = "";
        for(int y=board[0].length-1;y>=0;y--) {
            for(int x=0;x<board.length;x++) {
                result += pieceToString(board[x][y]);
                result += " ";
            }
            result += "\r\n";
        }
        for(int coordinate=0; coordinate<getWidth(); coordinate++) {
            result += coordinate+" ";
        }
        result += "\r\n";
        result = result.concat("to move: ");
        result = getColorToPlay()==WHITE ? result.concat("White (O)\r\n") : result.concat("Black (X)\r\n");
        return result;
    }

    public void undoLastMove(Move move) {
        GomokuMove m = (GomokuMove) move;
        setTurn(getTurn()-1);
        setColorToPlay(previousColor());
        //roll back hashes
        setHash(getHash() ^ zobristKeys[getTurn()][m.getX()][m.getY()]);
        piecesHashForColor[getColorToPlay()] ^= zobristKeys[1][m.getX()][m.getY()];
        //roll back legal moves
        getLegalMovesFor(WHITE).add(new GomokuMove(m.getX(),m.getY(),WHITE));
        getLegalMovesFor(BLACK).add(new GomokuMove(m.getX(),m.getY(),BLACK));
        piecesOfColor[getColorToPlay()]--;
        //roll back board
        board[m.getX()][m.getY()] = NO_PIECE;
        lastMove = null;
    }

    private int winner() {
        if(lastMove!=null) { //TODO make sure to set to null in any board initialization method
            //check possible 5 in a row that could have been finished by last move
            int x = lastMove.getX();
            int y = lastMove.getY();
            int ownPiece = getSquare(x,y);
fives:      for(int[][] fives : FIVE_IN_A_ROW_OFFSETS_FOR_GIVEN_SQUARE) {
                for(int[] offsets : fives) {
                    if(getSquare(x+offsets[0], y+offsets[1])!=ownPiece) {
                        continue fives;
                    }
                }
                return lastMove.getColorOfMove();
            }
        } else {
            //no last move; check all possible 5 in a row on the entire board
            //horizontals
ranks:      for (int y = 0; y < board[0].length; y++) {
                int piecesInARow = 0;
                int candidatePiece = NO_PIECE;
                for(int x = 0; x < board.length; x++) {
                    int piece = getSquare(x,y);
                    if(piece==NO_PIECE) {
                        piecesInARow = 0;
                        continue;
                    }
                    if(piece!=candidatePiece) {
                        if(x<=board.length-5) {
                            candidatePiece = piece;
                            piecesInARow = 1;
                        } else {
                            continue ranks;
                        }
                    } else {
                        piecesInARow++;
                    }
                    if(piecesInARow==5) {
                        return pieceToPlayer(candidatePiece);
                    }
                }
            }
            //verticals
files:      for (int x = 0; x < board.length; x++) {
                int piecesInARow = 0;
                int candidatePiece = NO_PIECE;
                for(int y = 0; y < board[x].length; y++) {
                    int piece = getSquare(x,y);
                    if(piece==NO_PIECE) {
                        continue files;
                    }
                    if(piece!=candidatePiece) {
                        if(y<=board[x].length-5) {
                            candidatePiece = piece;
                            piecesInARow = 1;
                        } else {
                            continue files;
                        }
                    } else {
                        piecesInARow++;
                    }
                    if(piecesInARow==5) {
                        return pieceToPlayer(candidatePiece);
                    }
                }
            }
            //diagonal to top right
diagonals:  for (int x = 5-board[0].length; x <= board.length-5; x++) {
                int piecesInARow = 0;
                int candidatePiece = NO_PIECE;
                for(int offset = 0; offset < board[0].length; offset++) {
                    int piece = getSquare(x+offset,offset);
                    if(piece==NO_PIECE || piece==OFF_BOARD) {
                        piecesInARow = 0;
                        continue;
                    }
                    if(piece!=candidatePiece) {
                        if(offset<board[0].length-4) {
                            candidatePiece = piece;
                            piecesInARow = 1;
                        } else {
                            continue diagonals;
                        }
                    } else {
                        piecesInARow++;
                    }
                    if(piecesInARow==5) {
                        return pieceToPlayer(candidatePiece);
                    }
                }
            }
            //diagonal to top left
diagonals:  for (int x = (board.length-1)-(4-board[0].length); x >= 4; x--) {
                int piecesInARow = 0;
                int candidatePiece = NO_PIECE;
                for(int offset = 0; offset < board[0].length; offset++) {
                    int piece = getSquare(x-offset,offset);
                    if(piece==NO_PIECE || piece==OFF_BOARD) {
                        piecesInARow = 0;
                        continue;
                    }
                    if(piece!=candidatePiece) {
                        if(offset<board[0].length-4) {
                            candidatePiece = piece;
                            piecesInARow = 1;
                        } else {
                            continue diagonals;
                        }
                    } else {
                        piecesInARow++;
                    }
                    if(piecesInARow==5) {
                        return pieceToPlayer(candidatePiece);
                    }
                }
            }
        }
        return Game.NO_COLOR;
    }

}
