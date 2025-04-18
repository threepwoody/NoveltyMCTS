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

import static ai.Game.BLACK;
import static ai.Game.WHITE;

public class OthelloNoTranspositionsBoard extends OnePieceTypeTwoPlayerBoard implements AlphaZeroBoard, PieceCountBoard, RectangularIntBoard, UndoBoard {

    private int blackPieces = 0;
    private int[][] board;
    Set<Move> legalMoves = new HashSet<>();
    private long[] movesHashForColor;
    private int numberOfPasses;
    private long[] piecesHashForColor;
    private int whitePieces = 0;
    //one distinct key for each possible move at each possible turn. this makes transpositions impossible and turns the game graph into a tree
    //dimensions to look up keys: [turn][X][Y] - special X/Y values reserved for a PASS move
    private final long[][][] zobristKeys;
    private Stack<Set<OthelloMove>> flippedPieces;

    public OthelloNoTranspositionsBoard(Othello game) {
        super(game);
        zobristKeys = Othello.getZobristKeys();
        initialiseStartingBoard(game.getBoardWidth(), game.getBoardHeight(), game.getStartingColor());
        setHashOfStartingBoard();
    }

    public OthelloNoTranspositionsBoard(Othello game, String board, boolean isFileName) {
        super(game);
        zobristKeys = Othello.getZobristKeys();
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
        result.put(2, firstDiagonalSymmetry);

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
        result.put(3, secondDiagonalSymmetry);

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
        OthelloNoTranspositionsBoard that = (OthelloNoTranspositionsBoard) thatboard;
        for(int x=0;x<board.length;x++) {
            System.arraycopy(that.board[x], 0, board[x], 0, board[0].length);
        }
        flippedPieces = new Stack<>();
        for(Set<OthelloMove> sets : that.flippedPieces) {
            flippedPieces.add(new HashSet<>(sets));
        }
        blackPieces = that.blackPieces;
        whitePieces = that.whitePieces;
        numberOfPasses = that.numberOfPasses;
        System.arraycopy(that.movesHashForColor, 0, movesHashForColor, 0, movesHashForColor.length);
        System.arraycopy(that.piecesHashForColor, 0, piecesHashForColor, 0, piecesHashForColor.length);
    }

    private boolean cornersEmpty() {
        if(board[0][0]!=NO_PIECE) {
            return false;
        }
        if(board[getWidth()-1][0]!=NO_PIECE) {
            return false;
        }
        if(board[0][getHeight()-1]!=NO_PIECE) {
            return false;
        }
        return board[getWidth() - 1][getHeight() - 1] == NO_PIECE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (! super.equals(o)) return false;
        if (getClass() != o.getClass()) return false;
        OthelloNoTranspositionsBoard that = (OthelloNoTranspositionsBoard) o;
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

    public int getNumberOfPasses() {
        return numberOfPasses;
    }

    public void setNumberOfPasses(int numberOfPasses) {
        this.numberOfPasses = numberOfPasses;
    }

    @Override
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
        //black pieces
        board[getWidth()/2-1][getHeight()/2-1] = BLACK_PIECE;
        board[getWidth()/2][getHeight()/2] = BLACK_PIECE;
        blackPieces += 2;
        //white pieces
        board[getWidth()/2][getHeight()/2-1] = WHITE_PIECE;
        board[getWidth()/2-1][getHeight()/2] = WHITE_PIECE;
        whitePieces += 2;
        setColorToPlay(startingColor);
        initializeLegalMoves();
        numberOfPasses = 0;
        movesHashForColor = new long[getNumberOfColors()];
        piecesHashForColor = new long[getNumberOfColors()];
        flippedPieces = new Stack<>();
    }

    //TODO debug
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
                        whitePieces++;
                    } else if (c == 'X' || c == 'B' || c == 'b') {
                        board[x][y] = BLACK_PIECE;
                        blackPieces++;
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
        numberOfPasses = 0; //TODO would need a way to read this from file, just assuming it's 0 here
        movesHashForColor = new long[getNumberOfColors()];
        piecesHashForColor = new long[getNumberOfColors()];
        flippedPieces = new Stack<>();
    }

    protected void initializeLegalMoves() {
        initializeLegalMovesFor(WHITE);
        initializeLegalMovesFor(BLACK);
    }

    private void initializeLegalMovesFor(int color) {
        int OWN_PIECE;
        int OPP_PIECE;
        legalMoves.clear();
        if(color==WHITE) {
            OWN_PIECE = WHITE_PIECE;
            OPP_PIECE = BLACK_PIECE;
        } else {
            OWN_PIECE = BLACK_PIECE;
            OPP_PIECE = WHITE_PIECE;
        }
        for(int y=0;y<getHeight();y++) {
            for(int x=0;x<getWidth();x++) {
                int piece = board[x][y];
                if(piece==OWN_PIECE) {
                    //check in all 8 directions if there is a possible move using this piece
                    //to the left
                    boolean foundAtLeastOneFlippablePiece = false;
                    for(int i=1;x-i>=0;i++) {
                        if(board[x-i][y]==OPP_PIECE) {
                            foundAtLeastOneFlippablePiece = true;
                        } else if(board[x-i][y]==NO_PIECE) {
                            if(foundAtLeastOneFlippablePiece) {
                                legalMoves.add(new OthelloMove(x-i,y,color));
                            }
                            break;
                        } else {
                            break;
                        }
                    }
                    //top left
                    foundAtLeastOneFlippablePiece = false;
                    for(int i=1;x-i>=0 && y+i<getHeight();i++) {
                        if(board[x-i][y+i]==OPP_PIECE) {
                            foundAtLeastOneFlippablePiece = true;
                        } else if(board[x-i][y+i]==NO_PIECE) {
                            if(foundAtLeastOneFlippablePiece) {
                                legalMoves.add(new OthelloMove(x-i, y+i,color));
                            }
                            break;
                        } else {
                            break;
                        }
                    }
                    //top
                    foundAtLeastOneFlippablePiece = false;
                    for(int i=1;y+i<getHeight();i++) {
                        if(board[x][y+i]==OPP_PIECE) {
                            foundAtLeastOneFlippablePiece = true;
                        } else if(board[x][y+i]==NO_PIECE) {
                            if(foundAtLeastOneFlippablePiece) {
                                legalMoves.add(new OthelloMove(x, y+i,color));
                            }
                            break;
                        } else {
                            break;
                        }
                    }
                    //top right
                    foundAtLeastOneFlippablePiece = false;
                    for(int i=1;x+i<getWidth() && y+i<getHeight();i++) {
                        if(board[x+i][y+i]==OPP_PIECE) {
                            foundAtLeastOneFlippablePiece = true;
                        } else if(board[x+i][y+i]==NO_PIECE) {
                            if(foundAtLeastOneFlippablePiece) {
                                legalMoves.add(new OthelloMove(x+i, y+i,color));
                            }
                            break;
                        } else {
                            break;
                        }
                    }
                    //right
                    foundAtLeastOneFlippablePiece = false;
                    for(int i=1;x+i<getWidth();i++) {
                        if(board[x+i][y]==OPP_PIECE) {
                            foundAtLeastOneFlippablePiece = true;
                        } else if(board[x+i][y]==NO_PIECE) {
                            if(foundAtLeastOneFlippablePiece) {
                                legalMoves.add(new OthelloMove(x+i, y,color));
                            }
                            break;
                        } else {
                            break;
                        }
                    }
                    //bottom right
                    foundAtLeastOneFlippablePiece = false;
                    for(int i=1;x+i<getWidth() && y-i>=0;i++) {
                        if(board[x+i][y-i]==OPP_PIECE) {
                            foundAtLeastOneFlippablePiece = true;
                        } else if(board[x+i][y-i]==NO_PIECE) {
                            if(foundAtLeastOneFlippablePiece) {
                                legalMoves.add(new OthelloMove(x+i, y-i,color));
                            }
                            break;
                        } else {
                            break;
                        }
                    }
                    //bottom
                    foundAtLeastOneFlippablePiece = false;
                    for(int i=1;y-i>=0;i++) {
                        if(board[x][y-i]==OPP_PIECE) {
                            foundAtLeastOneFlippablePiece = true;
                        } else if(board[x][y-i]==NO_PIECE) {
                            if(foundAtLeastOneFlippablePiece) {
                                legalMoves.add(new OthelloMove(x, y-i,color));
                            }
                            break;
                        } else {
                            break;
                        }
                    }
                    //bottom left
                    foundAtLeastOneFlippablePiece = false;
                    for(int i=1;x-i>=0 && y-i>=0;i++) {
                        if(board[x-i][y-i]==OPP_PIECE) {
                            foundAtLeastOneFlippablePiece = true;
                        } else if(board[x-i][y-i]==NO_PIECE) {
                            if(foundAtLeastOneFlippablePiece) {
                                legalMoves.add(new OthelloMove(x-i, y-i,color));
                            }
                            break;
                        } else {
                            break;
                        }
                    }
                }
            }
        }
        if(legalMoves.size()==0) {
            legalMoves.add(new OthelloMove()); //passing move
        }
        getLegalMovesFor(color).clear();
        getLegalMovesFor(color).addAll(legalMoves);
    }

    @Override
    public boolean isTerminalBoard() {
        if(blackPieces+whitePieces==getWidth()*getHeight() || numberOfPasses==2) {
            if(blackPieces>whitePieces) {
                setWinner(BLACK);
            } else if(whitePieces>blackPieces) {
                setWinner(WHITE);
            } else {
                Set<Integer> winningColors = new HashSet<>();
                for (int color = 0; color < getNumberOfColors(); color++) {
                    winningColors.add(color);
                }
                setWinners(winningColors);
            }
            return true;
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
        OthelloMove m = (OthelloMove) move;
        Set<OthelloMove> flipped = new HashSet<>();
        //update board[][]
        if(m.isPass()) {
            numberOfPasses++;
        } else {
            numberOfPasses = 0;
            int ownColor = getColorToPlay();
            int OWN_PIECE = pieceOfColor(ownColor);
            int oppColor = previousColor();
            int OPP_PIECE = pieceOfColor(oppColor);
            int numberOfFlippedPieces = 0;
            int x = m.getX();
            int y = m.getY();
            board[x][y] = OWN_PIECE;
            piecesHashForColor[ownColor] ^= zobristKeys[1][m.getX()][m.getY()];
            //to the left
            boolean foundAtLeastOneFlippablePiece = false;
            for(int i=1;x-i>=0;i++) {
                if(board[x-i][y]==OPP_PIECE) {
                    foundAtLeastOneFlippablePiece = true;
                } else if(board[x-i][y]==OWN_PIECE) {
                    if(foundAtLeastOneFlippablePiece) {
                        for(int j=i-1;j>=1;j--) {
                            board[x-j][y] = OWN_PIECE;
                            piecesHashForColor[ownColor] ^= zobristKeys[1][x-j][y];
                            piecesHashForColor[oppColor] ^= zobristKeys[1][x-j][y];
                            numberOfFlippedPieces++;
                            flipped.add(new OthelloMove(x-j,y,oppColor));
                        }
                    }
                    break;
                } else {
                    break;
                }
            }
            //top left
            foundAtLeastOneFlippablePiece = false;
            for(int i=1;x-i>=0 && y+i<getHeight();i++) {
                if(board[x-i][y+i]==OPP_PIECE) {
                    foundAtLeastOneFlippablePiece = true;
                } else if(board[x-i][y+i]==OWN_PIECE) {
                    if(foundAtLeastOneFlippablePiece) {
                        for(int j=i-1;j>=1;j--) {
                            board[x-j][y+j] = OWN_PIECE;
                            piecesHashForColor[ownColor] ^= zobristKeys[1][x-j][y+j];
                            piecesHashForColor[oppColor] ^= zobristKeys[1][x-j][y+j];
                            numberOfFlippedPieces++;
                            flipped.add(new OthelloMove(x-j,y+j,oppColor));
                        }
                    }
                    break;
                } else {
                    break;
                }
            }
            //top
            foundAtLeastOneFlippablePiece = false;
            for(int i=1;y+i<getHeight();i++) {
                if(board[x][y+i]==OPP_PIECE) {
                    foundAtLeastOneFlippablePiece = true;
                } else if(board[x][y+i]==OWN_PIECE) {
                    if(foundAtLeastOneFlippablePiece) {
                        for(int j=i-1;j>=1;j--) {
                            board[x][y+j] = OWN_PIECE;
                            piecesHashForColor[ownColor] ^= zobristKeys[1][x][y+j];
                            piecesHashForColor[oppColor] ^= zobristKeys[1][x][y+j];
                            numberOfFlippedPieces++;
                            flipped.add(new OthelloMove(x,y+j,oppColor));
                        }
                    }
                    break;
                } else {
                    break;
                }
            }
            //top right
            foundAtLeastOneFlippablePiece = false;
            for(int i=1;x+i<getWidth() && y+i<getHeight();i++) {
                if(board[x+i][y+i]==OPP_PIECE) {
                    foundAtLeastOneFlippablePiece = true;
                } else if(board[x+i][y+i]==OWN_PIECE) {
                    if(foundAtLeastOneFlippablePiece) {
                        for(int j=i-1;j>=1;j--) {
                            board[x+j][y+j] = OWN_PIECE;
                            piecesHashForColor[ownColor] ^= zobristKeys[1][x+j][y+j];
                            piecesHashForColor[oppColor] ^= zobristKeys[1][x+j][y+j];
                            numberOfFlippedPieces++;
                            flipped.add(new OthelloMove(x+j,y+j,oppColor));
                        }
                    }
                    break;
                } else {
                    break;
                }
            }
            //right
            foundAtLeastOneFlippablePiece = false;
            for(int i=1;x+i<getWidth();i++) {
                if(board[x+i][y]==OPP_PIECE) {
                    foundAtLeastOneFlippablePiece = true;
                } else if(board[x+i][y]==OWN_PIECE) {
                    if(foundAtLeastOneFlippablePiece) {
                        for(int j=i-1;j>=1;j--) {
                            board[x+j][y] = OWN_PIECE;
                            piecesHashForColor[ownColor] ^= zobristKeys[1][x+j][y];
                            piecesHashForColor[oppColor] ^= zobristKeys[1][x+j][y];
                            numberOfFlippedPieces++;
                            flipped.add(new OthelloMove(x+j,y,oppColor));
                        }
                    }
                    break;
                } else {
                    break;
                }
            }
            //bottom right
            foundAtLeastOneFlippablePiece = false;
            for(int i=1;x+i<getWidth() && y-i>=0;i++) {
                if(board[x+i][y-i]==OPP_PIECE) {
                    foundAtLeastOneFlippablePiece = true;
                } else if(board[x+i][y-i]==OWN_PIECE) {
                    if(foundAtLeastOneFlippablePiece) {
                        for(int j=i-1;j>=1;j--) {
                            board[x+j][y-j] = OWN_PIECE;
                            piecesHashForColor[ownColor] ^= zobristKeys[1][x+j][y-j];
                            piecesHashForColor[oppColor] ^= zobristKeys[1][x+j][y-j];
                            numberOfFlippedPieces++;
                            flipped.add(new OthelloMove(x+j,y-j,oppColor));
                        }
                    }
                    break;
                } else {
                    break;
                }
            }
            //bottom
            foundAtLeastOneFlippablePiece = false;
            for(int i=1;y-i>=0;i++) {
                if(board[x][y-i]==OPP_PIECE) {
                    foundAtLeastOneFlippablePiece = true;
                } else if(board[x][y-i]==OWN_PIECE) {
                    if(foundAtLeastOneFlippablePiece) {
                        for(int j=i-1;j>=1;j--) {
                            board[x][y-j] = OWN_PIECE;
                            piecesHashForColor[ownColor] ^= zobristKeys[1][x][y-j];
                            piecesHashForColor[oppColor] ^= zobristKeys[1][x][y-j];
                            numberOfFlippedPieces++;
                            flipped.add(new OthelloMove(x,y-j,oppColor));
                        }
                    }
                    break;
                } else {
                    break;
                }
            }
            //bottom left
            foundAtLeastOneFlippablePiece = false;
            for(int i=1;x-i>=0 && y-i>=0;i++) {
                if(board[x-i][y-i]==OPP_PIECE) {
                    foundAtLeastOneFlippablePiece = true;
                } else if(board[x-i][y-i]==OWN_PIECE) {
                    if(foundAtLeastOneFlippablePiece) {
                        for(int j=i-1;j>=1;j--) {
                            board[x-j][y-j] = OWN_PIECE;
                            piecesHashForColor[ownColor] ^= zobristKeys[1][x-j][y-j];
                            piecesHashForColor[oppColor] ^= zobristKeys[1][x-j][y-j];
                            numberOfFlippedPieces++;
                            flipped.add(new OthelloMove(x-j,y-j,oppColor));
                        }
                    }
                    break;
                } else {
                    break;
                }
            }
            if(ownColor==WHITE) {
                whitePieces+=(numberOfFlippedPieces+1);
                blackPieces-=numberOfFlippedPieces;
            } else {
                blackPieces+=(numberOfFlippedPieces+1);
                whitePieces-=numberOfFlippedPieces;
            }
        }
        flippedPieces.push(flipped);
        //update legal moves TODO would be great to do incrementally
        initializeLegalMoves();
        //update hashes
        setHash(getHash() ^ zobristKeys[getTurn()][m.isPass() ? getWidth() : m.getX()][m.isPass() ? getHeight() : m.getY()]);
        movesHashForColor[m.getColorOfMove()] ^= zobristKeys[getTurn()][m.isPass() ? getWidth() : m.getX()][m.isPass() ? getHeight() : m.getY()];
        setColorToPlay(nextColor());
        setTurn(getTurn()+1);
    }

    private void setHashOfStartingBoard() {
        long startPositionHash = 0L;
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

    //this only loosely approximates stability - it also counts discs as stable if you can reach one side of the board from them only finding your own discs, without checking if those are all stable. if they are not, neither is the piece at hand
    public int stableDiscs(int color) {
        int numberOfStableDiscs = 0;
        int ownPiece = pieceOfColor(color);
        if(cornersEmpty()) {
            return 0;
        }
        for(int x=0;x<getWidth();x++) {
            for(int y=0;y<getHeight();y++) {
                if(board[x][y]!=ownPiece) {
                    continue;
                }
                //check whether board[x][y] is stable
                //for all 4 lines through the square you have to
                //a) either reach both sides of the board without finding an empty square or
                //b) reach one side of the board only finding your own discs TODO this is actually not true - if those own discs aren't stable
                int safeLines = 0;

                //first: vertical line
                boolean onlyOwnDiscsToBorderInDirectionOne = true;
                boolean noEmptySquaresToBorderInDirectionOne = true;
                for(int yvar=y+1;yvar<getHeight();yvar++) {
                    if(board[x][yvar]!=ownPiece) {
                        onlyOwnDiscsToBorderInDirectionOne = false;
                        if(board[x][yvar]==NO_PIECE) {
                            noEmptySquaresToBorderInDirectionOne = false;
                            break;
                        }
                    }
                }
                boolean onlyOwnDiscsToBorderInDirectionTwo = true;
                boolean noEmptySquaresToBorderInDirectionTwo = true;
                for(int yvar=y-1;yvar>=0;yvar--) {
                    if(board[x][yvar]!=ownPiece) {
                        onlyOwnDiscsToBorderInDirectionTwo = false;
                        if(board[x][yvar]==NO_PIECE) {
                            noEmptySquaresToBorderInDirectionTwo = false;
                            break;
                        }
                    }
                }
                if(onlyOwnDiscsToBorderInDirectionOne || onlyOwnDiscsToBorderInDirectionTwo || (noEmptySquaresToBorderInDirectionOne && noEmptySquaresToBorderInDirectionTwo)) {
                    safeLines++;
                }

                //hoizontal line
                onlyOwnDiscsToBorderInDirectionOne = true;
                noEmptySquaresToBorderInDirectionOne = true;
                for(int xvar=x+1;xvar<getWidth();xvar++) {
                    if(board[xvar][y]!=ownPiece) {
                        onlyOwnDiscsToBorderInDirectionOne = false;
                        if(board[xvar][y]==NO_PIECE) {
                            noEmptySquaresToBorderInDirectionOne = false;
                            break;
                        }
                    }
                }
                onlyOwnDiscsToBorderInDirectionTwo = true;
                noEmptySquaresToBorderInDirectionTwo = true;
                for(int xvar=x-1;xvar>=0;xvar--) {
                    if(board[xvar][y]!=ownPiece) {
                        onlyOwnDiscsToBorderInDirectionTwo = false;
                        if(board[xvar][y]==NO_PIECE) {
                            noEmptySquaresToBorderInDirectionTwo = false;
                            break;
                        }
                    }
                }
                if(onlyOwnDiscsToBorderInDirectionOne || onlyOwnDiscsToBorderInDirectionTwo || (noEmptySquaresToBorderInDirectionOne && noEmptySquaresToBorderInDirectionTwo)) {
                    safeLines++;
                }

                //first diagonal
                onlyOwnDiscsToBorderInDirectionOne = true;
                noEmptySquaresToBorderInDirectionOne = true;
                for(int offset=1; x+offset<getWidth() && y+offset<getHeight(); offset++) {
                    if(board[x+offset][y+offset]!=ownPiece) {
                        onlyOwnDiscsToBorderInDirectionOne = false;
                        if(board[x+offset][y+offset]==NO_PIECE) {
                            noEmptySquaresToBorderInDirectionOne = false;
                            break;
                        }
                    }
                }
                onlyOwnDiscsToBorderInDirectionTwo = true;
                noEmptySquaresToBorderInDirectionTwo = true;
                for(int offset=1; x-offset>=0 && y-offset>=0; offset++) {
                    if(board[x-offset][y-offset]!=ownPiece) {
                        onlyOwnDiscsToBorderInDirectionTwo = false;
                        if(board[x-offset][y-offset]==NO_PIECE) {
                            noEmptySquaresToBorderInDirectionTwo = false;
                            break;
                        }
                    }
                }
                if(onlyOwnDiscsToBorderInDirectionOne || onlyOwnDiscsToBorderInDirectionTwo || (noEmptySquaresToBorderInDirectionOne && noEmptySquaresToBorderInDirectionTwo)) {
                    safeLines++;
                }

                //second diagonal
                onlyOwnDiscsToBorderInDirectionOne = true;
                noEmptySquaresToBorderInDirectionOne = true;
                for(int offset=1; x-offset>=0 && y+offset<getHeight(); offset++) {
                    if(board[x-offset][y+offset]!=ownPiece) {
                        onlyOwnDiscsToBorderInDirectionOne = false;
                        if(board[x-offset][y+offset]==NO_PIECE) {
                            noEmptySquaresToBorderInDirectionOne = false;
                            break;
                        }
                    }
                }
                onlyOwnDiscsToBorderInDirectionTwo = true;
                noEmptySquaresToBorderInDirectionTwo = true;
                for(int offset=1; x+offset<getWidth() && y-offset>=0; offset++) {
                    if(board[x+offset][y-offset]!=ownPiece) {
                        onlyOwnDiscsToBorderInDirectionTwo = false;
                        if(board[x+offset][y-offset]==NO_PIECE) {
                            noEmptySquaresToBorderInDirectionTwo = false;
                            break;
                        }
                    }
                }
                if(onlyOwnDiscsToBorderInDirectionOne || onlyOwnDiscsToBorderInDirectionTwo || (noEmptySquaresToBorderInDirectionOne && noEmptySquaresToBorderInDirectionTwo)) {
                    safeLines++;
                }

                if(safeLines==4) {
                    numberOfStableDiscs++;
                }

            }
        }
        return numberOfStableDiscs;
    }

    @Override
    public Move toMove(String moveString) throws MalformedMoveException {
        return new OthelloMove(moveString, getColorToPlay());
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
        OthelloMove m = (OthelloMove)move;
        Set<OthelloMove> flipped = flippedPieces.pop();
        if(m.isPass()) {
            numberOfPasses--;
        } else {
            numberOfPasses = 0; //TODO pass number also needs to be restored
            board[m.getX()][m.getY()] = NO_PIECE;
            int ownColor = m.getColorOfMove();
            int oppColor = ownColor==BLACK ? WHITE : BLACK;
            piecesHashForColor[ownColor] ^= zobristKeys[1][m.getX()][m.getY()];
            int oppPiece = m.getColorOfMove()==BLACK ? WHITE_PIECE : BLACK_PIECE;
            for(OthelloMove m2 : flipped) {
                board[m2.getX()][m2.getY()] = oppPiece;
                piecesHashForColor[ownColor] ^= zobristKeys[1][m2.getX()][m2.getY()];
                piecesHashForColor[oppColor] ^= zobristKeys[1][m2.getX()][m2.getY()];
            }
            if(ownColor==WHITE) {
                whitePieces-=(flipped.size()+1);
                blackPieces+=flipped.size();
            } else {
                blackPieces-=(flipped.size()+1);
                whitePieces+=flipped.size();
            }
        }
        //update legal moves TODO would be great to do incrementally
        initializeLegalMoves();
        //update hashes
        setTurn(getTurn()-1);
        setHash(getHash() ^ zobristKeys[getTurn()][m.isPass() ? getWidth() : m.getX()][m.isPass() ? getHeight() : m.getY()]);
        movesHashForColor[m.getColorOfMove()] ^= zobristKeys[getTurn()][m.isPass() ? getWidth() : m.getX()][m.isPass() ? getHeight() : m.getY()];
        setColorToPlay(previousColor());
    }
}
