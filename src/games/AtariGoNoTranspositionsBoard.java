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
import static games.AtariGo.KNIGHT_NEIGHBORHOOD_OFFSETS;

public class AtariGoNoTranspositionsBoard extends OnePieceTypeTwoPlayerBoard implements AlphaZeroBoard, RectangularIntBoard {

    //Contains WHITE_PIECE, BLACK_PIECE or NO_PIECE for the individual squares.
    private int[][] board;
    boolean[][] checked;
    private AtariGoMove lastMove;
    private long[] movesHashForColor;
    private long[] piecesHashForColor;
    //one distinct key for each possible move at each possible turn. this makes transpositions impossible and turns the game graph into a tree
    //dimensions to look up keys: [turn][x][y]
    private long[][][] zobristKeys;

    public AtariGoNoTranspositionsBoard(AtariGo game) {
        super(game);
        zobristKeys = game.getZobristKeys();
        initialiseStartingBoard(game.getBoardWidth(), game.getBoardHeight(), game.getStartingColor());
        setHashOfStartingBoard();
    }

    public AtariGoNoTranspositionsBoard(AtariGo game, String board, boolean isFileName) {
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
    }

    private static String print2DArray(boolean[][] checked, int piece) {
        String result = "";
        String symbol = piece == WHITE_PIECE ? "O" : "X";
        for (int y = checked[0].length - 1; y >= 0; y--) {
            for (int x = 0; x < checked.length; x++) {
                result += checked[x][y] ? symbol : ".";
                result += " ";
            }
            result += "\r\n";
        }
        return result;
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
        AtariGoNoTranspositionsBoard that = (AtariGoNoTranspositionsBoard) thatboard;
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
        AtariGoNoTranspositionsBoard that = (AtariGoNoTranspositionsBoard) o;
        return Arrays.deepEquals(board, that.board);
    }

    //TODO how do we efficiently detect suicide moves? do we need to incrementally keep liberty counts of all groups, or even concrete lists of liberties?
    //let's go with a naive approach first
    private void findLegalMoves() {
        getLegalMovesFor(WHITE).clear();
        getLegalMovesFor(BLACK).clear();
        for(int x=0;x<board.length;x++) {
            for(int y=0;y<board[0].length;y++) {
                int piece = board[x][y];
                if(piece==NO_PIECE) {
                    if(!isSuicideForPlayer(x,y,BLACK)) {
                        getLegalMovesFor(BLACK).add(new AtariGoMove(x, y, BLACK));
                    }
                    if(!isSuicideForPlayer(x,y,WHITE)) {
                        getLegalMovesFor(WHITE).add(new AtariGoMove(x, y, WHITE));
                    }
                }
            }
        }
    }

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

    private boolean hasALiberty(int x, int y) {
        if(getSquare(x+1,y)==NO_PIECE) {
            return true;
        }
        if(getSquare(x-1,y)==NO_PIECE) {
            return true;
        }
        if(getSquare(x,y+1)==NO_PIECE) {
            return true;
        }
        if(getSquare(x,y-1)==NO_PIECE) {
            return true;
        }
        return false;
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

    public boolean isCapture(int x, int y, int player) {
        boolean result = false;
        board[x][y] = pieceOfColor(player);
        int opponentPiece = oppositePiece(pieceOfColor(player));
        if(getSquare(x+1,y)==opponentPiece && !isPartOfGroupWithALiberty(x+1,y,opponentPiece)) {
            result = true;
        } else if(getSquare(x-1,y)==opponentPiece && !isPartOfGroupWithALiberty(x-1,y,opponentPiece)) {
            result = true;
        } else if(getSquare(x,y+1)==opponentPiece && !isPartOfGroupWithALiberty(x,y+1,opponentPiece)) {
            result = true;
        } else if(getSquare(x,y-1)==opponentPiece && !isPartOfGroupWithALiberty(x,y-1,opponentPiece)) {
            result = true;
        }
        board[x][y] = NO_PIECE;
        return result;
    }

    public boolean isEscapingCapture(int x, int y, int player) {
        boolean result = false;
        int piece = pieceOfColor(player);
        if(getSquare(x+1,y)!=piece && getSquare(x,y+1)!=piece && getSquare(x-1,y)!=piece && getSquare(x,y-1)!=piece) {
            return false;
        }
        int opponentPiece = oppositePiece(piece);
        board[x][y] = opponentPiece;
        if(getSquare(x+1,y)==piece && !isPartOfGroupWithALiberty(x+1,y,piece)) {
            result = true;
        } else if(getSquare(x-1,y)==piece && !isPartOfGroupWithALiberty(x-1,y,piece)) {
            result = true;
        } else if(getSquare(x,y+1)==piece && !isPartOfGroupWithALiberty(x,y+1,piece)) {
            result = true;
        } else if(getSquare(x,y-1)==piece && !isPartOfGroupWithALiberty(x,y-1,piece)) {
            result = true;
        }
        board[x][y] = NO_PIECE;
        return result;
    }

    public boolean isFeasible(Move m) {
        AtariGoMove move = (AtariGoMove) m;
        if(!isOnFirstLine(move) || isWithinKnightsMoveOfAnotherPiece(move)) {
            return true;
        }
        return false;
    }

    private boolean isOnFirstLine(AtariGoMove move) {
        int x = move.getX();
        if(x==0 || x==board.length-1) {
            return true;
        }
        int y = move.getY();
        if(y==0 || y==board[0].length-1) {
            return true;
        }
        return false;
    }

    private boolean isPartOfGroupWithALiberty(int x, int y, int piece) {
        checked = new boolean[board.length][board[0].length];
        return isPartOfGroupWithALibertyRecursive(x,y,piece);
    }

    private boolean isPartOfGroupWithALibertyRecursive(int x, int y, int piece) {
        checked[x][y] = true;
        int neighbor = getSquare(x+1,y);
        if(neighbor==NO_PIECE && !checked[x+1][y]) {
            return true;
        } else if(neighbor==piece && !checked[x+1][y]) {
            if(isPartOfGroupWithALibertyRecursive(x+1, y, piece)) {
                return true;
            }
        }
        neighbor = getSquare(x-1,y);
        if(neighbor==NO_PIECE && !checked[x-1][y]) {
            return true;
        } else if(neighbor==piece && !checked[x-1][y]) {
            if(isPartOfGroupWithALibertyRecursive(x-1, y, piece)) {
                return true;
            }
        }
        neighbor = getSquare(x,y+1);
        if(neighbor==NO_PIECE && !checked[x][y+1]) {
            return true;
        } else if(neighbor==piece && !checked[x][y+1]) {
            if(isPartOfGroupWithALibertyRecursive(x, y+1, piece)) {
                return true;
            }
        }
        neighbor = getSquare(x,y-1);
        if(neighbor==NO_PIECE && !checked[x][y-1]) {
            return true;
        } else if(neighbor==piece && !checked[x][y-1]) {
            if(isPartOfGroupWithALibertyRecursive(x, y-1, piece)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSuicideForPlayer(int x, int y, int player) {
        int piece = pieceOfColor(player);
        if(hasALiberty(x,y)) {
            return false;
        }
        if(isPartOfGroupWithALiberty(x,y,piece)) {
            return false;
        }
        if(isCapture(x,y,player)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isTerminalBoard() {
        if(getLegalMoves().size()==0) {
            setWinner(previousColor());
            return true;
        }
        for(int x=0; x<board.length;x++) {
            for (int y=0; y<board[0].length; y++) {
                if(board[x][y]!=NO_PIECE) {
                    if(!isPartOfGroupWithALiberty(x,y,board[x][y])) {
                        setWinner(lastMove.getColorOfMove());
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isWithinKnightsMoveOfAnotherPiece(AtariGoMove move) {
        int x = move.getX();
        int y = move.getY();
        for(int neighbor=0;neighbor<KNIGHT_NEIGHBORHOOD_OFFSETS.length;neighbor++) {
            int neighborX = KNIGHT_NEIGHBORHOOD_OFFSETS[neighbor][0];
            int neighborY = KNIGHT_NEIGHBORHOOD_OFFSETS[neighbor][1];
            if(getSquare(neighborX, neighborY)==BLACK_PIECE || getSquare(neighborX,neighborY)==WHITE_PIECE) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void play(Move move) {
        AtariGoMove m = (AtariGoMove) move;
        //update board
        int colorToPlay = getColorToPlay();
        int movingPiece = pieceOfColor(colorToPlay);
        board[m.getX()][m.getY()] = movingPiece;
        //update legal moves
        findLegalMoves();
        //update hashes
        setHash(getHash() ^ zobristKeys[getTurn()][m.getX()][m.getY()]);
        movesHashForColor[m.getColorOfMove()] ^= zobristKeys[getTurn()][m.getX()][m.getY()];
        piecesHashForColor[colorToPlay] ^= zobristKeys[1][m.getX()][m.getY()];
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
        return new AtariGoMove(input, getColorToPlay());
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
