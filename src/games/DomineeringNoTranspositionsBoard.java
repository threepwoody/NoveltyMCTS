package games;

import ai.Board;
import ai.Move;
import ai.OnePieceTypeTwoPlayerBoard;
import ai.RectangularIntBoard;
import experiments.AlphaZero.AlphaZeroBoard;
import utils.MalformedMoveException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static ai.Game.BLACK;
import static ai.Game.WHITE;

public class DomineeringNoTranspositionsBoard extends OnePieceTypeTwoPlayerBoard implements AlphaZeroBoard, RectangularIntBoard {

    //Contains WHITE_PIECE, BLACK_PIECE or NO_PIECE for the individual squares.
    private int[][] board;
    private DomineeringMove lastMove;
    private long[] movesHashForColor;
    private long[] piecesHashForColor;
    //one distinct key for each possible move at each possible turn. this makes transpositions impossible and turns the game graph into a tree
    //dimensions to look up keys: [turn][x][y]
    private long[][][] zobristKeys;

    public DomineeringNoTranspositionsBoard(Domineering game) {
        super(game);
        zobristKeys = game.getZobristKeys();
        initialiseStartingBoard(game.getBoardWidth(), game.getBoardHeight(), game.getStartingColor());
        setHashOfStartingBoard();
    }

    public DomineeringNoTranspositionsBoard(Domineering game, String board, boolean isFileName) {
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
        result.put(4, rotational180Symmetry);
        return result;
    }

    @Override
    public void copyDataFrom(Board thatboard) {
        super.copyDataFrom(thatboard);
        DomineeringNoTranspositionsBoard that = (DomineeringNoTranspositionsBoard) thatboard;
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
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (! super.equals(o)) return false;
        if (o == null || getClass() != o.getClass()) return false;
        DomineeringNoTranspositionsBoard that = (DomineeringNoTranspositionsBoard) o;
        return Arrays.deepEquals(board, that.board);
    }

    private void findLegalMoves() {
        getLegalMovesFor(WHITE).clear();
        getLegalMovesFor(BLACK).clear();
        for(int x=0;x<board.length;x++) {
            for(int y=1;y<board[0].length;y++) {
                if(board[x][y]==NO_PIECE && board[x][y-1]==NO_PIECE) {
                    getLegalMovesFor(BLACK).add(new DomineeringMove(x, y, BLACK));
                }
            }
        }
        for(int x=0;x<board.length-1;x++) {
            for(int y=0;y<board[0].length;y++) {
                if(board[x][y]==NO_PIECE && board[x+1][y]==NO_PIECE) {
                    getLegalMovesFor(WHITE).add(new DomineeringMove(x, y, WHITE));
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
    }

    private void initializeBoardFromString(String boardString, int boardWidth, int boardHeight) {
        board = new int[boardWidth][boardHeight];
        InputStreamReader input = new InputStreamReader(new ByteArrayInputStream(boardString.getBytes()));
        BufferedReader lineInput = new BufferedReader(input);

        try {
            for(int y=boardHeight-1;y>=0;y--) {
                for(int x=0;x<boardWidth;x++) {
                    char c = (char) input.read();
                    while (c == ' ' || c == '\r' || c == '\n') {
                        c = (char) input.read();
                    }
                    if (c == 'O' || c == 'W' || c == 'w') {
                        board[x][y] = WHITE_PIECE;
                    } else if (c == 'X' || c == 'B' || c == 'b') {
                        board[x][y] = BLACK_PIECE;
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

    @Override
    public boolean isTerminalBoard() {
        if(getLegalMoves().size()==0) {
            setWinner(previousColor());
            return true;
        }
        return false;
    }

    @Override
    public void play(Move move) {
        DomineeringMove m = (DomineeringMove) move;
        //update board
        boolean vertical = getColorToPlay()==BLACK;
        int piece = pieceOfColor(getColorToPlay());
        board[m.getX()][m.getY()] = piece;
        if(vertical) {
            board[m.getX()][m.getY()-1] = piece;
        } else {
            board[m.getX()+1][m.getY()] = piece;
        }
        //update legal moves
        findLegalMoves();
        //update hashes
        setHash(getHash() ^ zobristKeys[getTurn()][m.getX()][m.getY()]);
        movesHashForColor[m.getColorOfMove()] ^= zobristKeys[getTurn()][m.getX()][m.getY()];
        if(vertical) {
            piecesHashForColor[BLACK] ^= zobristKeys[1][m.getX()][m.getY()];
            piecesHashForColor[BLACK] ^= zobristKeys[1][m.getX()][m.getY()-1];
        } else {
            piecesHashForColor[WHITE] ^= zobristKeys[1][m.getX()][m.getY()];
            piecesHashForColor[WHITE] ^= zobristKeys[1][m.getX()+1][m.getY()];
        }
        setColorToPlay(nextColor());
        setTurn(getTurn()+1);
        lastMove = m;
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

    @Override
    public Move toMove(String input) throws MalformedMoveException {
        return new DomineeringMove(input, getColorToPlay());
    }

    @Override
    public String toString() {
        String result = "";
        for (int y = board[0].length - 1; y >= 0; y--) {
            for (int x = 0; x < board.length; x++) {
                result += pieceToString(board[x][y]);
                result += " ";
            }
            result += "\r\n";
        }
        for (char alphabet = 'a'; alphabet < 'a' + board.length; alphabet++) {
            result += alphabet + " ";
        }
        result += "\r\n";
        result = result.concat("to move: ");
        result = getColorToPlay() == WHITE ? result.concat("White (O)\r\n") : result.concat("Black (X)\r\n");
        return result;
    }

}
