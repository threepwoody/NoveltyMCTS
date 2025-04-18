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
import static games.Connect4.FOUR_IN_A_ROW_OFFSETS_FOR_GIVEN_SQUARE;

public class Connect4NoTranspositionsBoard extends OnePieceTypeTwoPlayerBoard implements AlphaZeroBoard, RectangularIntBoard, UndoBoard {

    //Contains WHITE_PIECE, BLACK_PIECE or NO_PIECE for the individual squares.
    private int[][] board;
    private Stack<Connect4Move> moves;
    private long[] movesHashForColor;
    private long[] piecesHashForColor;
    private int[] piecesOfColor;
    //one distinct key for each possible move at each possible turn. this makes transpositions impossible and turns the game graph into a tree
    //dimensions to look up keys: [turn][x][y]
    private long[][][] zobristKeys;

    public Connect4NoTranspositionsBoard(Connect4 game) {
        super(game);
        zobristKeys = game.getZobristKeys();
        initialiseStartingBoard(game.getBoardWidth(), game.getBoardHeight(), game.getStartingColor());
        setHashOfStartingBoard();
    }

    public Connect4NoTranspositionsBoard(Connect4 game, String board, boolean isFileName) {
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

    //only call when game has ended
    public Move winningMove() {
        return moves.peek();
    }

    @Override
    public void copyDataFrom(Board thatboard) {
        super.copyDataFrom(thatboard);
        Connect4NoTranspositionsBoard that = (Connect4NoTranspositionsBoard) thatboard;
        for(int x=0;x<board.length;x++) {
            for(int y=0;y<board[0].length;y++) {
                board[x][y] = that.board[x][y];
            }
        }
        moves = (Stack<Connect4Move>) that.moves.clone();
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
        Connect4NoTranspositionsBoard that = (Connect4NoTranspositionsBoard) o;
        return Arrays.deepEquals(board, that.board);
    }

    private void findLegalMoves() {
        getLegalMovesFor(WHITE).clear();
        getLegalMovesFor(BLACK).clear();
files:  for(int x=0;x<board.length;x++) {
            for(int y=0;y<board[0].length;y++) {
                int piece = board[x][y];
                if(piece==NO_PIECE) {
                    getLegalMovesFor(BLACK).add(new Connect4Move(x,y,BLACK));
                    getLegalMovesFor(WHITE).add(new Connect4Move(x,y,WHITE));
                    continue files;
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
        moves = new Stack<>();
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
        moves = new Stack<>();
        movesHashForColor = new long[getNumberOfColors()];
        piecesHashForColor = new long[getNumberOfColors()];
    }

    private boolean isDraw() {
        for (int row = 0; row < board.length; row++) {
            if (getSquare(row, board[0].length - 1) == NO_PIECE) {
                return false;
            }
        }
        return true;
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
        Connect4Move m = (Connect4Move) move;
        //update board
        int movingPiece = pieceOfColor(getColorToPlay());
        board[m.getX()][m.getY()] = movingPiece;
        piecesOfColor[getColorToPlay()]++;
        //update legal moves
        getLegalMovesFor(WHITE).remove(new Connect4Move(m.getX(),m.getY(),WHITE));
        getLegalMovesFor(BLACK).remove(new Connect4Move(m.getX(),m.getY(),BLACK));
        if(m.getY()<board[0].length-1) {
            getLegalMovesFor(WHITE).add(new Connect4Move(m.getX(),m.getY()+1,WHITE));
            getLegalMovesFor(BLACK).add(new Connect4Move(m.getX(),m.getY()+1,BLACK));
        }
        //update hashes
        setHash(getHash() ^ zobristKeys[getTurn()][m.getX()][m.getY()]);
        movesHashForColor[m.getColorOfMove()] ^= zobristKeys[getTurn()][m.getX()][m.getY()];
        piecesHashForColor[getColorToPlay()] ^= zobristKeys[1][m.getX()][m.getY()];
        setColorToPlay(nextColor());
        setTurn(getTurn()+1);
        moves.push(m);
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
        return new Connect4Move(moveString, getColorToPlay());
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

    public void undo(Move move) {
        Connect4Move m = (Connect4Move) move;
        setTurn(getTurn()-1);
        setColorToPlay(previousColor());
        //roll back hashes
        setHash(getHash() ^ zobristKeys[getTurn()][m.getX()][m.getY()]);
        piecesHashForColor[getColorToPlay()] ^= zobristKeys[1][m.getX()][m.getY()];
        //roll back legal moves
        getLegalMovesFor(WHITE).add(new Connect4Move(m.getX(),m.getY(),WHITE));
        getLegalMovesFor(BLACK).add(new Connect4Move(m.getX(),m.getY(),BLACK));
        if(m.getY()<board[0].length-1) {
            getLegalMovesFor(WHITE).remove(new Connect4Move(m.getX(),m.getY()+1,WHITE));
            getLegalMovesFor(BLACK).remove(new Connect4Move(m.getX(),m.getY()+1,BLACK));
        }
        piecesOfColor[getColorToPlay()]--;
        //roll back board
        board[m.getX()][m.getY()] = NO_PIECE;
        moves.pop();
    }

    private int winner() {
        if(!moves.empty()) {
            //check possible 4 in a row that could have been finished by last move
            int x = moves.peek().getX();
            int y = moves.peek().getY();
            int ownPiece = getSquare(x,y);
fours:      for(int[][] fours : FOUR_IN_A_ROW_OFFSETS_FOR_GIVEN_SQUARE) {
                for(int[] offsets : fours) {
                    if(getSquare(x+offsets[0], y+offsets[1])!=ownPiece) {
                        continue fours;
                    }
                }
                return moves.peek().getColorOfMove();
            }
        } else {
            //check all possible 4 in a row on the entire board
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
                        if(x<=board.length-4) {
                            candidatePiece = piece;
                            piecesInARow = 1;
                        } else {
                            continue ranks;
                        }
                    } else {
                        piecesInARow++;
                    }
                    if(piecesInARow==4) {
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
                        if(y<=board[x].length-4) {
                            candidatePiece = piece;
                            piecesInARow = 1;
                        } else {
                            continue files;
                        }
                    } else {
                        piecesInARow++;
                    }
                    if(piecesInARow==4) {
                        return pieceToPlayer(candidatePiece);
                    }
                }
            }
            //diagonal to top right
diagonals:  for (int x = 4-board[0].length; x <= board.length-4; x++) {
                int piecesInARow = 0;
                int candidatePiece = NO_PIECE;
                for(int offset = 0; offset < board[0].length; offset++) {
                    int piece = getSquare(x+offset,offset);
                    if(piece==NO_PIECE || piece==OFF_BOARD) {
                        piecesInARow = 0;
                        continue;
                    }
                    if(piece!=candidatePiece) {
                        if(offset<board[0].length-3) {
                            candidatePiece = piece;
                            piecesInARow = 1;
                        } else {
                            continue diagonals;
                        }
                    } else {
                        piecesInARow++;
                    }
                    if(piecesInARow==4) {
                        return pieceToPlayer(candidatePiece);
                    }
                }
            }
            //diagonal to top left
diagonals:  for (int x = (board.length-1)-(4-board[0].length); x >= 3; x--) {
                int piecesInARow = 0;
                int candidatePiece = NO_PIECE;
                for(int offset = 0; offset < board[0].length; offset++) {
                    int piece = getSquare(x-offset,offset);
                    if(piece==NO_PIECE || piece==OFF_BOARD) {
                        piecesInARow = 0;
                        continue;
                    }
                    if(piece!=candidatePiece) {
                        if(offset<board[0].length-3) {
                            candidatePiece = piece;
                            piecesInARow = 1;
                        } else {
                            continue diagonals;
                        }
                    } else {
                        piecesInARow++;
                    }
                    if(piecesInARow==4) {
                        return pieceToPlayer(candidatePiece);
                    }
                }
            }
        }
        return Game.NO_COLOR;
    }

}
