package games;

import ai.Board;
import ai.Move;
import ai.OnePieceTypeTwoPlayerBoard;
import ai.RectangularIntBoard;
import ai.terminalboardevaluation.PieceCountBoard;
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
import java.util.List;
import java.util.Map;

import static ai.Game.*;

public class NeoCrossingsNoTranspositionsBoard extends OnePieceTypeTwoPlayerBoard implements AlphaZeroBoard, PieceCountBoard, RectangularIntBoard {

    private int blackPieces = 0;
    //Contains WHITE_PIECE, BLACK_PIECE or NO_PIECE for the individual squares.
    private int[][] board;
    private final long[] colorToPlayKeys;
    private int maxTurnsPerMatch;
    private int whitePieces = 0;
    //one distinct key for each possible piece at each possible location. this means transpositions are possible
    //dimensions to look up keys: [x][y][color]
    private final long[][][] zobristKeys;

    public NeoCrossingsNoTranspositionsBoard(NeoCrossings game) {
        super(game);
        colorToPlayKeys = NeoCrossings.getColorToPlayKeys();
        zobristKeys = NeoCrossings.getZobristKeys();
        initialiseStartingBoard(game.getBoardWidth(), game.getBoardHeight(), game.getStartingColor(), game.getMaxTurnsPerMatch());
        setHashOfStartingBoard();
    }

    public NeoCrossingsNoTranspositionsBoard(NeoCrossings game, String board, boolean isFileName) {
        super(game);
        colorToPlayKeys = NeoCrossings.getColorToPlayKeys();
        zobristKeys = NeoCrossings.getZobristKeys();
        if(isFileName) {
            try {
                String boardInFile = new String(Files.readAllBytes(Paths.get(board)));
                initializeBoardFromString(boardInFile, game.getBoardWidth(), game.getBoardHeight(), game.getMaxTurnsPerMatch());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            initializeBoardFromString(board, game.getBoardWidth(), game.getBoardHeight(), game.getMaxTurnsPerMatch());
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

    private void addAllMovesToTheBottom(int x, int y, int piece, int color, int oppPiece, List<Move> legalMoves) {
        int lengthOfOwnPhalanx = 1;
        int lengthOfOppPhalanx = 0;
        int distanceOfMove = 0;
        int counter=1;
        for(;y-counter>=0;counter++)  {
            if(board[x][y-counter]==piece) {
                lengthOfOwnPhalanx++;
            } else {
                break;
            }
        }
        for(;y-counter>=0;counter++) {
            distanceOfMove++;
            if(distanceOfMove>lengthOfOwnPhalanx) {
                return;
            }
            if(board[x][y-counter]==NO_PIECE) {
                legalMoves.add(new NeoCrossingsMove(x,y,4,distanceOfMove,color));
            } else {
                break;
            }
        }
        for(;y-counter>=0;counter++) {
            if(board[x][y-counter]==oppPiece) {
                lengthOfOppPhalanx++;
            } else {
                break;
            }
        }
        if(lengthOfOppPhalanx>0 && lengthOfOppPhalanx<lengthOfOwnPhalanx) {
            legalMoves.add(new NeoCrossingsMove(x,y,4,distanceOfMove,color));
        }
    }

    private void addAllMovesToTheBottomLeft(int x, int y, int piece, int color, int oppPiece, List<Move> legalMoves) {
        int lengthOfOwnPhalanx = 1;
        int lengthOfOppPhalanx = 0;
        int distanceOfMove = 0;
        int counter=1;
        for(;x-counter>=0 && y-counter>=0;counter++)  {
            if(board[x-counter][y-counter]==piece) {
                lengthOfOwnPhalanx++;
            } else {
                break;
            }
        }
        for(;x-counter>=0 && y-counter>=0;counter++)  {
            distanceOfMove++;
            if(distanceOfMove>lengthOfOwnPhalanx) {
                return;
            }
            if(board[x-counter][y-counter]==NO_PIECE) {
                legalMoves.add(new NeoCrossingsMove(x,y,5,distanceOfMove,color));
            } else {
                break;
            }
        }
        for(;x-counter>=0 && y-counter>=0;counter++)  {
            if(board[x-counter][y-counter]==oppPiece) {
                lengthOfOppPhalanx++;
            } else {
                break;
            }
        }
        if(lengthOfOppPhalanx>0 && lengthOfOppPhalanx<lengthOfOwnPhalanx) {
            legalMoves.add(new NeoCrossingsMove(x,y,5,distanceOfMove,color));
        }
    }

    private void addAllMovesToTheBottomRight(int x, int y, int piece, int color, int oppPiece, List<Move> legalMoves) {
        int lengthOfOwnPhalanx = 1;
        int lengthOfOppPhalanx = 0;
        int distanceOfMove = 0;
        int counter=1;
        for(;x+counter<getWidth() && y-counter>=0;counter++)  {
            if(board[x+counter][y-counter]==piece) {
                lengthOfOwnPhalanx++;
            } else {
                break;
            }
        }
        for(;x+counter<getWidth() && y-counter>=0;counter++)  {
            distanceOfMove++;
            if(distanceOfMove>lengthOfOwnPhalanx) {
                return;
            }
            if(board[x+counter][y-counter]==NO_PIECE) {
                legalMoves.add(new NeoCrossingsMove(x,y,3,distanceOfMove,color));
            } else {
                break;
            }
        }
        for(;x+counter<getWidth() && y-counter>=0;counter++)  {
            if(board[x+counter][y-counter]==oppPiece) {
                lengthOfOppPhalanx++;
            } else {
                break;
            }
        }
        if(lengthOfOppPhalanx>0 && lengthOfOppPhalanx<lengthOfOwnPhalanx) {
            legalMoves.add(new NeoCrossingsMove(x,y,3,distanceOfMove,color));
        }
    }

    private void addAllMovesToTheLeft(int x, int y, int piece, int color, int oppPiece, List<Move> legalMoves) {
        int lengthOfOwnPhalanx = 1;
        int lengthOfOppPhalanx = 0;
        int distanceOfMove = 0;
        int counter=1;
        for(;x-counter>=0;counter++) {
            if(board[x-counter][y]==piece) {
                lengthOfOwnPhalanx++;
            } else {
                break;
            }
        }
        for(;x-counter>=0;counter++) {
            distanceOfMove++;
            if(distanceOfMove>lengthOfOwnPhalanx) {
                return;
            }
            if(board[x-counter][y]==NO_PIECE) {
                legalMoves.add(new NeoCrossingsMove(x,y,6,distanceOfMove,color));
            } else {
                break;
            }
        }
        for(;x-counter>=0;counter++) {
            if(board[x-counter][y]==oppPiece) {
                lengthOfOppPhalanx++;
            } else {
                break;
            }
        }
        if(lengthOfOppPhalanx>0 && lengthOfOppPhalanx<lengthOfOwnPhalanx) {
            legalMoves.add(new NeoCrossingsMove(x,y,6,distanceOfMove,color));
        }
    }

    private void addAllMovesToTheRight(int x, int y, int piece, int color, int oppPiece, List<Move> legalMoves) {
        int lengthOfOwnPhalanx = 1;
        int lengthOfOppPhalanx = 0;
        int distanceOfMove = 0;
        int counter=1;
        for(;x+counter<getWidth();counter++) {
            if(board[x+counter][y]==piece) {
                lengthOfOwnPhalanx++;
            } else {
                break;
            }
        }
        for(;x+counter<getWidth();counter++) {
            distanceOfMove++;
            if(distanceOfMove>lengthOfOwnPhalanx) {
                return;
            }
            if(board[x+counter][y]==NO_PIECE) {
                legalMoves.add(new NeoCrossingsMove(x,y,2,distanceOfMove,color));
            } else {
                break;
            }
        }
        for(;x+counter<getWidth();counter++) {
            if(board[x+counter][y]==oppPiece) {
                lengthOfOppPhalanx++;
            } else {
                break;
            }
        }
        if(lengthOfOppPhalanx>0 && lengthOfOppPhalanx<lengthOfOwnPhalanx) {
            legalMoves.add(new NeoCrossingsMove(x,y,2,distanceOfMove,color));
        }
    }

    private void addAllMovesToTheTop(int x, int y, int piece, int color, int oppPiece, List<Move> legalMoves) {
        int lengthOfOwnPhalanx = 1;
        int lengthOfOppPhalanx = 0;
        int distanceOfMove = 0;
        int counter=1;
        for(;y+counter<getHeight();counter++) {
            if(board[x][y+counter]==piece) {
                lengthOfOwnPhalanx++;
            } else {
                break;
            }
        }
        for(;y+counter<getHeight();counter++) {
            distanceOfMove++;
            if(distanceOfMove>lengthOfOwnPhalanx) {
                return;
            }
            if(board[x][y+counter]==NO_PIECE) {
                legalMoves.add(new NeoCrossingsMove(x,y,0,distanceOfMove,color));
            } else {
                break;
            }
        }
        for(;y+counter<getHeight();counter++) {
            if(board[x][y+counter]==oppPiece) {
                lengthOfOppPhalanx++;
            } else {
                break;
            }
        }
        if(lengthOfOppPhalanx>0 && lengthOfOppPhalanx<lengthOfOwnPhalanx) {
            legalMoves.add(new NeoCrossingsMove(x,y,0,distanceOfMove,color));
        }
    }

    private void addAllMovesToTheTopLeft(int x, int y, int piece, int color, int oppPiece, List<Move> legalMoves) {
        int lengthOfOwnPhalanx = 1;
        int lengthOfOppPhalanx = 0;
        int distanceOfMove = 0;
        int counter=1;
        for(;x-counter>=0 && y+counter<getHeight();counter++)  {
            if(board[x-counter][y+counter]==piece) {
                lengthOfOwnPhalanx++;
            } else {
                break;
            }
        }
        for(;x-counter>=0 && y+counter<getHeight();counter++) {
            distanceOfMove++;
            if(distanceOfMove>lengthOfOwnPhalanx) {
                return;
            }
            if(board[x-counter][y+counter]==NO_PIECE) {
                legalMoves.add(new NeoCrossingsMove(x,y,7,distanceOfMove,color));
            } else {
                break;
            }
        }
        for(;x-counter>=0 && y+counter<getHeight();counter++) {
            if(board[x-counter][y+counter]==oppPiece) {
                lengthOfOppPhalanx++;
            } else {
                break;
            }
        }
        if(lengthOfOppPhalanx>0 && lengthOfOppPhalanx<lengthOfOwnPhalanx) {
            legalMoves.add(new NeoCrossingsMove(x,y,7,distanceOfMove,color));
        }
    }

    private void addAllMovesToTheTopRight(int x, int y, int piece, int color, int oppPiece, List<Move> legalMoves) {
        int lengthOfOwnPhalanx = 1;
        int lengthOfOppPhalanx = 0;
        int distanceOfMove = 0;
        int counter=1;
        for(;x+counter<getWidth() && y+counter<getHeight();counter++)  {
            if(board[x+counter][y+counter]==piece) {
                lengthOfOwnPhalanx++;
            } else {
                break;
            }
        }
        for(;x+counter<getWidth() && y+counter<getHeight();counter++)  {
            distanceOfMove++;
            if(distanceOfMove>lengthOfOwnPhalanx) {
                return;
            }
            if(board[x+counter][y+counter]==NO_PIECE) {
                legalMoves.add(new NeoCrossingsMove(x,y,1,distanceOfMove,color));
            } else {
                break;
            }
        }
        for(;x+counter<getWidth() && y+counter<getHeight();counter++)  {
            if(board[x+counter][y+counter]==oppPiece) {
                lengthOfOppPhalanx++;
            } else {
                break;
            }
        }
        if(lengthOfOppPhalanx>0 && lengthOfOppPhalanx<lengthOfOwnPhalanx) {
            legalMoves.add(new NeoCrossingsMove(x,y,1,distanceOfMove,color));
        }
    }

    @Override
    public void copyDataFrom(Board thatboard) {
        super.copyDataFrom(thatboard);
        NeoCrossingsNoTranspositionsBoard that = (NeoCrossingsNoTranspositionsBoard) thatboard;
        for(int x=0;x<board.length;x++) {
            System.arraycopy(that.board[x], 0, board[x], 0, board[0].length);
        }
        blackPieces = that.blackPieces;
        whitePieces = that.whitePieces;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (! super.equals(o)) return false;
        if (getClass() != o.getClass()) return false;
        NeoCrossingsNoTranspositionsBoard that = (NeoCrossingsNoTranspositionsBoard) o;
        return Arrays.deepEquals(board, that.board);
    }

    @Override
    public int[][] getBoard() {
        return board;
    }

    @Override
    public int getHeight() {
        return board[0].length;
    }

    @Override
    public int getSquare(int x, int y) {
        if(x<0 || x>board.length-1 || y<0 || y>board[0].length-1) {
            return OFF_BOARD;
        }
        return board[x][y];
    }

    @Override
    public int getWidth() {
        return board.length;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Arrays.deepHashCode(board);
        return result;
    }

    protected void initialiseStartingBoard(int width, int height, int startingColor, int maxTurnsPerMatch) {
        this.maxTurnsPerMatch = maxTurnsPerMatch;
        board = new int[width][height];
        //black pieces
        for(int row=1;row<=2;row++) {
            for(int i=0;i<width;i++) {
                board[i][height-row] = BLACK_PIECE;
                blackPieces++;
            }
        }
        //white pieces
        for(int row=0;row<=1;row++) {
            for(int i=0;i<width;i++) {
                board[i][row] = WHITE_PIECE;
                whitePieces++;
            }
        }
        setColorToPlay(startingColor);
        initializeLegalMoves();
    }

    protected void initializeBoardFromString(String boardString, int boardWidth, int boardHeight, int maxTurnsPerMatch) {
        this.maxTurnsPerMatch = maxTurnsPerMatch;
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
    }

    protected void initializeLegalMoves() {
        getLegalMovesFor(WHITE).clear();
        getLegalMovesFor(BLACK).clear();
        for(int y=0;y<board[0].length;y++) {
            for(int x=0;x<board.length;x++) {
                int piece = board[x][y];
                if(piece!=NO_PIECE) {
                    int color = colorOfPiece(piece);
                    List<Move> legalMoves = piece==WHITE_PIECE ? getLegalMovesFor(WHITE) : getLegalMovesFor(BLACK);
                    //check for any possible phalanx moves pushed by this piece (at most as many squares as the phalanx is long, and captures are only possible if the first opponent piece met is part of a smaller phalanx in the direction of movement)
                    int oppPiece = oppositePiece(piece);
                    addAllMovesToTheTop(x, y, piece, color, oppPiece, legalMoves);
                    addAllMovesToTheTopRight(x, y, piece, color, oppPiece, legalMoves);
                    addAllMovesToTheRight(x, y, piece, color, oppPiece, legalMoves);
                    addAllMovesToTheBottomRight(x, y, piece, color, oppPiece, legalMoves);
                    addAllMovesToTheBottom(x, y, piece, color, oppPiece, legalMoves);
                    addAllMovesToTheBottomLeft(x, y, piece, color, oppPiece, legalMoves);
                    addAllMovesToTheLeft(x, y, piece, color, oppPiece, legalMoves);
                    addAllMovesToTheTopLeft(x, y, piece, color, oppPiece, legalMoves);
                }
            }
        }
    }

    @Override
    public boolean isTerminalBoard() {
        if(getTurn()==maxTurnsPerMatch) {
            return true;
        }
        if(blackPieces==0) {
            setWinner(WHITE);
            return true;
        } else if(whitePieces==0) {
            setWinner(BLACK);
            return true;
        }
        int whitePiecesOnFinalRow = 0;
        int blackPiecesOnFinalRow = 0;
        for(int x=0;x<board.length;x++) {
            if(board[x][board[0].length-1]==WHITE_PIECE) {
                whitePiecesOnFinalRow++;
            }
        }
        for(int x=0;x<board.length;x++) {
            if(board[x][0]==BLACK_PIECE) {
                blackPiecesOnFinalRow++;
            }
        }
        if(getColorToPlay()==WHITE && whitePiecesOnFinalRow>blackPiecesOnFinalRow) {
            setWinner(WHITE);
            return true;
        }
        if(getColorToPlay()==BLACK && blackPiecesOnFinalRow>whitePiecesOnFinalRow) {
            setWinner(BLACK);
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
        NeoCrossingsMove m = (NeoCrossingsMove) move;
        //update board[][]
        int pushingPieceX = m.getFromX();
        int pushingPieceY = m.getFromY();
        int distance = m.getPushDistance();
        //TODO think of subtracting pieces if captures happen
        switch (m.getPushDirection()) {
            case 0 -> pushToTop(pushingPieceX, pushingPieceY, distance);
            case 1 -> pushToTopRight(pushingPieceX, pushingPieceY, distance);
            case 2 -> pushToRight(pushingPieceX, pushingPieceY, distance);
            case 3 -> pushToBottomRight(pushingPieceX, pushingPieceY, distance);
            case 4 -> pushToBottom(pushingPieceX, pushingPieceY, distance);
            case 5 -> pushToBottomLeft(pushingPieceX, pushingPieceY, distance);
            case 6 -> pushToLeft(pushingPieceX, pushingPieceY, distance);
            case 7 -> pushToTopLeft(pushingPieceX, pushingPieceY, distance);
            default -> System.out.println("Nonexistent direction in move " + m);
        }
        //update legal moves
        initializeLegalMoves();
        //update hash with color to play, and advance color to play
        setHash(getHash() ^ colorToPlayKeys[getColorToPlay()]);
        setColorToPlay(nextColor());
        setHash(getHash() ^ colorToPlayKeys[getColorToPlay()]);
        setTurn(getTurn()+1);
    }

    private void pushToBottom(int pushingPieceX, int pushingPieceY, int distance) {
        int ownPiece = board[pushingPieceX][pushingPieceY];
        int oppPiece = oppositePiece(ownPiece);
        boolean capture = false;
        int colorToPlay = getColorToPlay();
        int i=0;
        for(;i<distance;i++) {
            board[pushingPieceX][pushingPieceY -i] = NO_PIECE;
            setHash(getHash() ^ zobristKeys[pushingPieceX][pushingPieceY -i][colorToPlay]);
        }
        while(board[pushingPieceX][pushingPieceY -i]==ownPiece) {
            i++;
        }
        int j=i;
        for(;j<i+distance;j++) {
            if (board[pushingPieceX][pushingPieceY -j]==oppPiece) {
                capture = true;
                if (oppPiece==WHITE_PIECE) {
                    whitePieces--;
                } else {
                    blackPieces--;
                }
                setHash(getHash() ^ zobristKeys[pushingPieceX][pushingPieceY -j][oppositeColor(colorToPlay)]);
            }
            board[pushingPieceX][pushingPieceY -j] = ownPiece;
            setHash(getHash() ^ zobristKeys[pushingPieceX][pushingPieceY -j][colorToPlay]);
        }
        if (capture) {
            while(getSquare(pushingPieceX, pushingPieceY -j)==oppPiece) {
                if (oppPiece==WHITE_PIECE) {
                    whitePieces--;
                } else {
                    blackPieces--;
                }
                setHash(getHash() ^ zobristKeys[pushingPieceX][pushingPieceY -j][oppositeColor(colorToPlay)]);
                board[pushingPieceX][pushingPieceY -j] = NO_PIECE;
            }
        }
    }

    private void pushToBottomLeft(int pushingPieceX, int pushingPieceY, int distance) {
        int ownPiece = board[pushingPieceX][pushingPieceY];
        int oppPiece = oppositePiece(ownPiece);
        boolean capture = false;
        int colorToPlay = getColorToPlay();
        int i=0;
        for(;i<distance;i++) {
            board[pushingPieceX -i][pushingPieceY -i] = NO_PIECE;
            setHash(getHash() ^ zobristKeys[pushingPieceX -i][pushingPieceY -i][colorToPlay]);
        }
        while(board[pushingPieceX -i][pushingPieceY -i]==ownPiece) {
            i++;
        }
        int j=i;
        for(;j<i+distance;j++) {
            if (board[pushingPieceX -j][pushingPieceY -j]==oppPiece) {
                capture = true;
                if (oppPiece==WHITE_PIECE) {
                    whitePieces--;
                } else {
                    blackPieces--;
                }
                setHash(getHash() ^ zobristKeys[pushingPieceX -j][pushingPieceY -j][oppositeColor(colorToPlay)]);
            }
            board[pushingPieceX -j][pushingPieceY -j] = ownPiece;
            setHash(getHash() ^ zobristKeys[pushingPieceX -j][pushingPieceY -j][colorToPlay]);
        }
        if (capture) {
            while (getSquare(pushingPieceX - j, pushingPieceY - j) == oppPiece) {
                if (oppPiece == WHITE_PIECE) {
                    whitePieces--;
                } else {
                    blackPieces--;
                }
                setHash(getHash() ^ zobristKeys[pushingPieceX -j][pushingPieceY -j][oppositeColor(colorToPlay)]);
                board[pushingPieceX - j][pushingPieceY - j] = NO_PIECE;
            }
        }
    }

    private void pushToBottomRight(int pushingPieceX, int pushingPieceY, int distance) {
        int ownPiece = board[pushingPieceX][pushingPieceY];
        int oppPiece = oppositePiece(ownPiece);
        boolean capture = false;
        int colorToPlay = getColorToPlay();
        int i=0;
        for(;i<distance;i++) {
            board[pushingPieceX +i][pushingPieceY -i] = NO_PIECE;
            setHash(getHash() ^ zobristKeys[pushingPieceX +i][pushingPieceY -i][colorToPlay]);
        }
        while(board[pushingPieceX +i][pushingPieceY -i]==ownPiece) {
            i++;
        }
        int j=i;
        for(;j<i+distance;j++) {
            if (board[pushingPieceX +j][pushingPieceY -j]==oppPiece) {
                capture = true;
                if (oppPiece==WHITE_PIECE) {
                    whitePieces--;
                } else {
                    blackPieces--;
                }
                setHash(getHash() ^ zobristKeys[pushingPieceX +j][pushingPieceY -j][oppositeColor(colorToPlay)]);
            }
            board[pushingPieceX +j][pushingPieceY -j] = ownPiece;
            setHash(getHash() ^ zobristKeys[pushingPieceX +j][pushingPieceY -j][colorToPlay]);
        }
        if (capture) {
            while (getSquare(pushingPieceX + j, pushingPieceY - j) == oppPiece) {
                if (oppPiece == WHITE_PIECE) {
                    whitePieces--;
                } else {
                    blackPieces--;
                }
                setHash(getHash() ^ zobristKeys[pushingPieceX +j][pushingPieceY -j][oppositeColor(colorToPlay)]);
                board[pushingPieceX + j][pushingPieceY - j] = NO_PIECE;
            }
        }
    }

    private void pushToLeft(int pushingPieceX, int pushingPieceY, int distance) {
        int ownPiece = board[pushingPieceX][pushingPieceY];
        int oppPiece = oppositePiece(ownPiece);
        boolean capture = false;
        int colorToPlay = getColorToPlay();
        int i=0;
        for(;i<distance;i++) {
            board[pushingPieceX -i][pushingPieceY] = NO_PIECE;
            setHash(getHash() ^ zobristKeys[pushingPieceX -i][pushingPieceY][colorToPlay]);
        }
        while(board[pushingPieceX -i][pushingPieceY]==ownPiece) {
            i++;
        }
        int j=i;
        for(;j<i+distance;j++) {
            if (board[pushingPieceX -j][pushingPieceY]==oppPiece) {
                capture = true;
                if (oppPiece==WHITE_PIECE) {
                    whitePieces--;
                } else {
                    blackPieces--;
                }
                setHash(getHash() ^ zobristKeys[pushingPieceX -j][pushingPieceY][oppositeColor(colorToPlay)]);
            }
            board[pushingPieceX -j][pushingPieceY] = ownPiece;
            setHash(getHash() ^ zobristKeys[pushingPieceX -j][pushingPieceY][colorToPlay]);
        }
        if (capture) {
            while (getSquare(pushingPieceX - j, pushingPieceY) == oppPiece) {
                if (oppPiece == WHITE_PIECE) {
                    whitePieces--;
                } else {
                    blackPieces--;
                }
                setHash(getHash() ^ zobristKeys[pushingPieceX -j][pushingPieceY][oppositeColor(colorToPlay)]);
                board[pushingPieceX - j][pushingPieceY] = NO_PIECE;
            }
        }
    }

    private void pushToRight(int pushingPieceX, int pushingPieceY, int distance) {
        int ownPiece = board[pushingPieceX][pushingPieceY];
        int oppPiece = oppositePiece(ownPiece);
        boolean capture = false;
        int colorToPlay = getColorToPlay();
        int i=0;
        for(;i<distance;i++) {
            board[pushingPieceX +i][pushingPieceY] = NO_PIECE;
            setHash(getHash() ^ zobristKeys[pushingPieceX +i][pushingPieceY][colorToPlay]);
        }
        while(board[pushingPieceX +i][pushingPieceY]==ownPiece) {
            i++;
        }
        int j=i;
        for(;j<i+distance;j++) {
            if (board[pushingPieceX +j][pushingPieceY]==oppPiece) {
                capture = true;
                if (oppPiece==WHITE_PIECE) {
                    whitePieces--;
                } else {
                    blackPieces--;
                }
                setHash(getHash() ^ zobristKeys[pushingPieceX +j][pushingPieceY][oppositeColor(colorToPlay)]);
            }
            board[pushingPieceX +j][pushingPieceY] = ownPiece;
            setHash(getHash() ^ zobristKeys[pushingPieceX +j][pushingPieceY][colorToPlay]);
        }
        if (capture) {
            while (getSquare(pushingPieceX + j, pushingPieceY) == oppPiece) {
                if (oppPiece == WHITE_PIECE) {
                    whitePieces--;
                } else {
                    blackPieces--;
                }
                setHash(getHash() ^ zobristKeys[pushingPieceX +j][pushingPieceY][oppositeColor(colorToPlay)]);
                board[pushingPieceX + j][pushingPieceY] = NO_PIECE;
            }
        }
    }

    private void pushToTop(int pushingPieceX, int pushingPieceY, int distance) {
        int ownPiece = board[pushingPieceX][pushingPieceY];
        int oppPiece = oppositePiece(ownPiece);
        boolean capture = false;
        int colorToPlay = getColorToPlay();
        int i=0;
        for(;i<distance;i++) {
            board[pushingPieceX][pushingPieceY +i] = NO_PIECE;
            setHash(getHash() ^ zobristKeys[pushingPieceX][pushingPieceY +i][colorToPlay]);
        }
        while(board[pushingPieceX][pushingPieceY +i]==ownPiece) {
            i++;
        }
        int j=i;
        for(;j<i+distance;j++) {
            if (board[pushingPieceX][pushingPieceY +j]==oppPiece) {
                capture = true;
                if (oppPiece==WHITE_PIECE) {
                    whitePieces--;
                } else {
                    blackPieces--;
                }
                setHash(getHash() ^ zobristKeys[pushingPieceX][pushingPieceY +j][oppositeColor(colorToPlay)]);
            }
            board[pushingPieceX][pushingPieceY +j] = ownPiece;
            setHash(getHash() ^ zobristKeys[pushingPieceX][pushingPieceY +j][colorToPlay]);
        }
        if (capture) {
            while (getSquare(pushingPieceX, pushingPieceY + j) == oppPiece) {
                if (oppPiece == WHITE_PIECE) {
                    whitePieces--;
                } else {
                    blackPieces--;
                }
                setHash(getHash() ^ zobristKeys[pushingPieceX][pushingPieceY +j][oppositeColor(colorToPlay)]);
                board[pushingPieceX][pushingPieceY + j] = NO_PIECE;
            }
        }
    }

    private void pushToTopLeft(int pushingPieceX, int pushingPieceY, int distance) {
        int ownPiece = board[pushingPieceX][pushingPieceY];
        int oppPiece = oppositePiece(ownPiece);
        boolean capture = false;
        int colorToPlay = getColorToPlay();
        int i=0;
        for(;i<distance;i++) {
            board[pushingPieceX -i][pushingPieceY +i] = NO_PIECE;
            setHash(getHash() ^ zobristKeys[pushingPieceX -i][pushingPieceY +i][colorToPlay]);
        }
        while(board[pushingPieceX -i][pushingPieceY +i]==ownPiece) {
            i++;
        }
        int j=i;
        for(;j<i+distance;j++) {
            if (board[pushingPieceX -j][pushingPieceY +j]==oppPiece) {
                capture = true;
                if (oppPiece==WHITE_PIECE) {
                    whitePieces--;
                } else {
                    blackPieces--;
                }
                setHash(getHash() ^ zobristKeys[pushingPieceX -j][pushingPieceY +j][oppositeColor(colorToPlay)]);
            }
            board[pushingPieceX -j][pushingPieceY +j] = ownPiece;
            setHash(getHash() ^ zobristKeys[pushingPieceX -j][pushingPieceY +j][colorToPlay]);
        }
        if (capture) {
            while (getSquare(pushingPieceX - j, pushingPieceY + j) == oppPiece) {
                if (oppPiece == WHITE_PIECE) {
                    whitePieces--;
                } else {
                    blackPieces--;
                }
                setHash(getHash() ^ zobristKeys[pushingPieceX -j][pushingPieceY +j][oppositeColor(colorToPlay)]);
                board[pushingPieceX - j][pushingPieceY + j] = NO_PIECE;
            }
        }
    }

    private void pushToTopRight(int pushingPieceX, int pushingPieceY, int distance) {
        int ownPiece = board[pushingPieceX][pushingPieceY];
        int oppPiece = oppositePiece(ownPiece);
        boolean capture = false;
        int colorToPlay = getColorToPlay();
        int i=0;
        for(;i<distance;i++) {
            board[pushingPieceX +i][pushingPieceY +i] = NO_PIECE;
            setHash(getHash() ^ zobristKeys[pushingPieceX +i][pushingPieceY +i][colorToPlay]);
        }
        while(board[pushingPieceX +i][pushingPieceY +i]==ownPiece) {
            i++;
        }
        int j=i;
        for(;j<i+distance;j++) {
            if (board[pushingPieceX +j][pushingPieceY +j]==oppPiece) {
                capture = true;
                if (oppPiece==WHITE_PIECE) {
                    whitePieces--;
                } else {
                    blackPieces--;
                }
                setHash(getHash() ^ zobristKeys[pushingPieceX +j][pushingPieceY +j][oppositeColor(colorToPlay)]);
            }
            board[pushingPieceX +j][pushingPieceY +j] = ownPiece;
            setHash(getHash() ^ zobristKeys[pushingPieceX +j][pushingPieceY +j][colorToPlay]);
        }
        if (capture) {
            while (getSquare(pushingPieceX + j, pushingPieceY + j) == oppPiece) {
                if (oppPiece == WHITE_PIECE) {
                    whitePieces--;
                } else {
                    blackPieces--;
                }
                setHash(getHash() ^ zobristKeys[pushingPieceX +j][pushingPieceY +j][oppositeColor(colorToPlay)]);
                board[pushingPieceX + j][pushingPieceY + j] = NO_PIECE;
            }
        }
    }

    private void setHashOfStartingBoard() {
        long startPositionHash = 0L;
        for(int x=0;x<board.length;x++) {
            for(int y=0;y<board[0].length;y++) {
                int piece = board[x][y];
                if(piece!=NO_PIECE) {
                    int color = colorOfPiece(piece);
                    startPositionHash ^= zobristKeys[x][y][color];
                }
            }
        }
        startPositionHash ^= colorToPlayKeys[getColorToPlay()];
        setHash(startPositionHash);
    }

    @Override
    public Move toMove(String moveString) throws MalformedMoveException {
        return new NeoCrossingsMove(moveString, getColorToPlay());
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
//        result += "WHITE moves:\r\n";
//        for(Move move : getLegalMovesFor(WHITE)) {
//            result += move + "\r\n";
//        }
//        result += "BLACK moves:\r\n";
//        for(Move move : getLegalMovesFor(BLACK)) {
//            result += move + "\r\n";
//        }
        return result;
    }

}
