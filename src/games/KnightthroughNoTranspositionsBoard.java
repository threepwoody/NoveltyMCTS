package games;

import ai.*;
import ai.terminalboardevaluation.PieceCountBoard;
import experiments.AlphaZero.AlphaZeroBoard;
import utils.MalformedMoveException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static ai.Game.*;

public class KnightthroughNoTranspositionsBoard extends OnePieceTypeTwoPlayerBoard implements AlphaZeroBoard, PieceCountBoard, RectangularIntBoard, UndoBoard {

    private int blackPieces = 0;
    //Contains WHITE_PIECE, BLACK_PIECE or NO_PIECE for the individual squares.
    private int[][] board;
    private long[] movesHashForColor;
    private long[] piecesHashForColor;
    private int whitePieces = 0;
    //one distinct key for each possible move at each possible turn. this makes transpositions impossible and turns the game graph into a tree
    //dimensions to look up keys: [turn][fromX][fromY][movetype 0-7]
    private long[][][][] zobristKeys;

    public KnightthroughNoTranspositionsBoard(Knightthrough game) {
        super(game);
        zobristKeys = game.getZobristKeys();
        initialiseStartingBoard(game.getBoardWidth(), game.getBoardHeight(), game.getRowsOfPieces(), game.getStartingColor());
        setHashOfStartingBoard();
    }

    public KnightthroughNoTranspositionsBoard(Knightthrough game, String board, boolean isFileName) {
        super(game);
        zobristKeys = game.getZobristKeys();
        if(isFileName) {
            try {
                String boardInFile = new String(Files.readAllBytes(Paths.get(board)));
                initializeBoardFromString(boardInFile, game.getBoardWidth(), game.getBoardHeight(), game.getRowsOfPieces());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            initializeBoardFromString(board, game.getBoardWidth(), game.getBoardHeight(), game.getRowsOfPieces());
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
        result.put(2, verticalSymmetry);
        return result;
    }

    @Override
    public void copyDataFrom(Board thatboard) {
        super.copyDataFrom(thatboard);
        KnightthroughNoTranspositionsBoard that = (KnightthroughNoTranspositionsBoard) thatboard;
        for(int x=0;x<board.length;x++) {
            for(int y=0;y<board[0].length;y++) {
                board[x][y] = that.board[x][y];
            }
        }
        blackPieces = that.blackPieces;
        whitePieces = that.whitePieces;
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
        KnightthroughNoTranspositionsBoard that = (KnightthroughNoTranspositionsBoard) o;
        return Arrays.deepEquals(board, that.board);
    }

    public int getBlackPieces() {
        return blackPieces;
    }

    public void setBlackPieces(int blackPieces) {
        this.blackPieces = blackPieces;
    }

    @Override
    public int[][] getBoard() {
        return board;
    }

    public int getHeight() {
        return board[0].length;
    }

    public int getSquare(int x, int y) {
        if(x<0 || x>board.length-1 || y<0 || y>board[0].length-1) {
            return OFF_BOARD;
        }
        return board[x][y];
    }

    public int getWhitePieces() {
        return whitePieces;
    }

    public void setWhitePieces(int whitePieces) {
        this.whitePieces = whitePieces;
    }

    public int getWidth() {
        return board.length;
    }

    protected long[][][][] getZobristKeys() {
        return zobristKeys;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Arrays.deepHashCode(board);
        return result;
    }

    private void initialiseStartingBoard(int width, int height, int rowsOfPieces, int startingColor) {
        board = new int[width][height];
        //black pieces
        for(int row=1;row<=rowsOfPieces;row++) {
            for(int i=0;i<width;i++) {
                board[i][height-row] = BLACK_PIECE;
                blackPieces++;
            }
        }
        //white pieces
        for(int row=0;row<=rowsOfPieces-1;row++) {
            for(int i=0;i<width;i++) {
                board[i][row] = WHITE_PIECE;
                whitePieces++;
            }
        }
        setColorToPlay(startingColor);
        initializeLegalMoves();
        movesHashForColor = new long[getNumberOfColors()];
        piecesHashForColor = new long[getNumberOfColors()];
    }

    private void initializeBoardFromString(String boardString, int boardWidth, int boardHeight, int rowsOfPieces) {
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
                        whitePieces++;
                    } else if (c == 'X' || c == 'B' || c == 'b') {
                        board[x][y] = BLACK_PIECE;
                        blackPieces++;
                    } else if (c == '.') {
                    }
                }
            }
            lineInput.readLine();
            lineInput.readLine();
            char toplay = (char)input.read();
            if(toplay=='0' || toplay=='s' || toplay=='S' || toplay=='B' || toplay=='b' || toplay=='X' || toplay=='x') {
                setColorToPlay(BLACK);
            } else if(toplay=='1' || toplay=='w' || toplay=='W' || toplay=='O' || toplay=='o') {
                setColorToPlay(WHITE);
            } else {
                String line = lineInput.readLine();
                if(line.contains("(X)") || line.contains("Black") || line.contains("black")) {
                    setColorToPlay(BLACK);
                } else if(line.contains("(O)") || line.contains("White") || line.contains("white")) {
                    setColorToPlay(WHITE);
                }
            }
            input.close();
        } catch (IOException e) {
            System.err.println("Cannot read board from String: "+boardString);
            e.printStackTrace();
            System.exit(1);
        }
        initializeLegalMoves();
        movesHashForColor = new long[getNumberOfColors()];
        piecesHashForColor = new long[getNumberOfColors()];
    }

    protected void initializeLegalMoves() {
        getLegalMovesFor(WHITE).clear();
        getLegalMovesFor(BLACK).clear();
        for(int y=0;y<board[0].length;y++) {
            for(int x=0;x<board.length;x++) {
                int piece = board[x][y];
                if(piece!=NO_PIECE) {
                    int moveDirection = (piece==WHITE_PIECE ? 1 : -1);
                    List<Move> legalMoves = piece==WHITE_PIECE ? getLegalMovesFor(WHITE) : getLegalMovesFor(BLACK);
                    //check the four possible locations a piece could move to.
                    //legal moves aren't needed anymore when a player has reached his opponent's home row, so there are always potential moves.
                    if(x<board.length-1) {
                        if(getSquare(x+1,y+2*moveDirection)==NO_PIECE) {
                            legalMoves.add(new KnightthroughMove(x,y,x+1,y+2*moveDirection,false));
                        } else if(getSquare(x+1,y+2*moveDirection)!=OFF_BOARD && getSquare(x+1,y+2*moveDirection)!= getSquare(x,y)) {
                            legalMoves.add(new KnightthroughMove(x,y,x+1,y+2*moveDirection,true));
                        }
                    }
                    if(x<board.length-2) {
                        if(getSquare(x+2,y+moveDirection)==NO_PIECE) {
                            legalMoves.add(new KnightthroughMove(x,y,x+2,y+moveDirection,false));
                        } else if(getSquare(x+2,y+moveDirection)!=OFF_BOARD && getSquare(x+2,y+moveDirection)!= getSquare(x,y)) {
                            legalMoves.add(new KnightthroughMove(x,y,x+2,y+moveDirection,true));
                        }
                    }
                    if(x>0) {
                        if(getSquare(x-1,y+2*moveDirection)==NO_PIECE) {
                            legalMoves.add(new KnightthroughMove(x,y,x-1,y+2*moveDirection,false));
                        } else if(getSquare(x-1,y+2*moveDirection)!=OFF_BOARD && getSquare(x-1,y+2*moveDirection)!= getSquare(x,y)) {
                            legalMoves.add(new KnightthroughMove(x,y,x-1,y+2*moveDirection,true));
                        }
                    }
                    if(x>1) {
                        if(getSquare(x-2,y+moveDirection)==NO_PIECE) {
                            legalMoves.add(new KnightthroughMove(x,y,x-2,y+moveDirection,false));
                        } else if(getSquare(x-2,y+moveDirection)!=OFF_BOARD && getSquare(x-2,y+moveDirection)!= getSquare(x,y)) {
                            legalMoves.add(new KnightthroughMove(x,y,x-2,y+moveDirection,true));
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean isTerminalBoard() {
        if(blackPieces==0) {
            setWinner(WHITE);
            return true;
        } else if(whitePieces==0) {
            setWinner(BLACK);
            return true;
        }
        for(int x=0;x<board.length;x++) {
            if(board[x][board[0].length-1]==WHITE_PIECE) {
                setWinner(WHITE);
                return true;
            }
        }
        for(int x=0;x<board.length;x++) {
            if(board[x][0]==BLACK_PIECE) {
                setWinner(BLACK);
                return true;
            }
        }
        return false;
    }

    @Override
    public int numberOfPieces(int color) {
        if(color==WHITE) return whitePieces;
        return blackPieces;
    }

    @Override
    public void play(Move move) {
        KnightthroughMove m = (KnightthroughMove) move;
        //update board[][]
        int colorToPlay = getColorToPlay();
        int otherColor = previousColor();
        int movingPiece = pieceOfColor(colorToPlay);
        board[m.getFromX()][m.getFromY()] = NO_PIECE;
        board[m.getToX()][m.getToY()] = movingPiece;
        //update legal moves
        initializeLegalMoves();
        if(m.isCapture()) {
            if(colorToPlay==WHITE) {
                blackPieces--;
            } else {
                whitePieces--;
            }
        }
        //update hashes
        setHash(getHash() ^ zobristKeys[getTurn()][m.getFromX()][m.getFromY()][m.getMoveType()]);
        movesHashForColor[m.getColorOfMove()] ^= zobristKeys[getTurn()][m.getFromX()][m.getFromY()][m.getMoveType()];
        piecesHashForColor[colorToPlay] ^= zobristKeys[1][m.getFromX()][m.getFromY()][colorToPlay];
        piecesHashForColor[colorToPlay] ^= zobristKeys[1][m.getToX()][m.getToY()][colorToPlay];
        if(m.isCapture()) {
            piecesHashForColor[otherColor] ^= zobristKeys[1][m.getToX()][m.getToY()][otherColor];
        }
        setColorToPlay(nextColor());
        setTurn(getTurn()+1);
    }

    private void setHashOfStartingBoard() {
        long startPositionHash = 0l;
        setHash(startPositionHash);
        for(int x=0;x<board.length;x++) {
            for(int y=0;y<board[0].length;y++) {
                if(board[x][y]==WHITE_PIECE) {
                    piecesHashForColor[WHITE] ^= zobristKeys[1][x][y][WHITE];
                } else if(board[x][y]==BLACK_PIECE) {
                    piecesHashForColor[BLACK] ^= zobristKeys[1][x][y][BLACK];
                }
            }
        }
    }

    protected void setPiecesHashForColor(int color, long hash) {
        piecesHashForColor[color] = hash;
    }

    @Override
    public Move toMove(String input) throws MalformedMoveException {
        return new KnightthroughMove(input);
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
        for(char alphabet='a'; alphabet<'a'+board.length; alphabet++) {
            result += alphabet+" ";
        }
        result += "\r\n";
        result = result.concat("to move: ");
        result = getColorToPlay()==WHITE ? result.concat("White (O)\r\n") : result.concat("Black (X)\r\n");
        return result;
    }

    @Override
    public void undo(Move move) {
        KnightthroughMove m = (KnightthroughMove) move;
        //update board[][]
        int colorOfMove = previousColor();
        int otherColor = oppositeColor(colorOfMove);
        int movedPiece = pieceOfColor(colorOfMove);
        int opponentPiece = oppositePiece(movedPiece);
        board[m.getFromX()][m.getFromY()] = movedPiece;
        board[m.getToX()][m.getToY()] = m.isCapture() ? opponentPiece : NO_PIECE;
        //update legal moves
        initializeLegalMoves();
        if(m.isCapture()) {
            if(colorOfMove==WHITE) {
                blackPieces++;
            } else {
                whitePieces++;
            }
        }
        setTurn(getTurn()-1);
        //update hashes
        setHash(getHash() ^ zobristKeys[getTurn()][m.getFromX()][m.getFromY()][m.getMoveType()]);
        movesHashForColor[m.getColorOfMove()] ^= zobristKeys[getTurn()][m.getFromX()][m.getFromY()][m.getMoveType()];
        piecesHashForColor[colorOfMove] ^= zobristKeys[1][m.getFromX()][m.getFromY()][colorOfMove];
        piecesHashForColor[colorOfMove] ^= zobristKeys[1][m.getToX()][m.getToY()][colorOfMove];
        if(m.isCapture()) {
            piecesHashForColor[otherColor] ^= zobristKeys[1][m.getToX()][m.getToY()][otherColor];
        }
        setColorToPlay(previousColor());
    }

}
