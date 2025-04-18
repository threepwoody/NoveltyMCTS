package games;

import ai.Move;

import static ai.Game.*;
import static ai.Game.BLACK;

public class BreakthroughWithTranspositionsBoard extends BreakthroughNoTranspositionsBoard {

    public BreakthroughWithTranspositionsBoard(Breakthrough game) {
        super(game);
    }

    public BreakthroughWithTranspositionsBoard(Breakthrough game, String board, boolean isFileName) {
        super(game, board, isFileName);
    }

    @Override
    public void play(Move move) {
        BreakthroughMove m = (BreakthroughMove) move;
        //update board[][]
        int colorToPlay = getColorToPlay();
        int movingPiece = pieceOfColor(colorToPlay);
        int otherColor = oppositeColor(colorToPlay);
        getBoard()[m.getFromX()][m.getFromY()] = NO_PIECE;
        getBoard()[m.getToX()][m.getToY()] = movingPiece;
        setHash(getHash() ^ zobristKeys[0][m.getFromX()][m.getFromY()][colorToPlay]);
        setHash(getHash() ^ zobristKeys[0][m.getToX()][m.getToY()][colorToPlay]);
        //update legal moves
        initializeLegalMoves();
        if(m.isCapture()) {
            setHash(getHash() ^ zobristKeys[0][m.getToX()][m.getToY()][otherColor]);
            if(colorToPlay==WHITE) {
                setBlackPieces(getBlackPieces()-1);
            } else {
                setWhitePieces(getWhitePieces()-1);
            }
        }
        getMovesHashForColor()[m.getColorOfMove()] ^= zobristKeys[getTurn()][m.getFromX()][m.getFromY()][m.getMoveType()];
        getPiecesHashForColor()[colorToPlay] ^= zobristKeys[1][m.getFromX()][m.getFromY()][colorToPlay];
        getPiecesHashForColor()[colorToPlay] ^= zobristKeys[1][m.getToX()][m.getToY()][colorToPlay];
        if(m.isCapture()) {
            getPiecesHashForColor()[otherColor] ^= zobristKeys[1][m.getToX()][m.getToY()][otherColor];
        }
        setColorToPlay(nextColor());
        setTurn(getTurn()+1);
    }

    @Override
    protected void setHashOfStartingBoard() {
        long startPositionHash = 0L;
        for(int x=0;x<getBoard().length;x++) {
            for(int y=0;y<getBoard()[0].length;y++) {
                if(getBoard()[x][y]==WHITE_PIECE) {
                    getPiecesHashForColor()[WHITE] ^= zobristKeys[1][x][y][WHITE];
                    startPositionHash ^= zobristKeys[0][x][y][WHITE];
                } else if(getBoard()[x][y]==BLACK_PIECE) {
                    getPiecesHashForColor()[BLACK] ^= zobristKeys[1][x][y][BLACK];
                    startPositionHash ^= zobristKeys[0][x][y][BLACK];
                }
            }
        }
        setHash(startPositionHash);
    }
    @Override
    public void undo(Move move) {
        BreakthroughMove m = (BreakthroughMove) move;
        //update board[][]
        int colorOfMove = previousColor();
        int otherColor = oppositeColor(colorOfMove);
        int movedPiece = pieceOfColor(colorOfMove);
        int opponentPiece = oppositePiece(movedPiece);
        getBoard()[m.getFromX()][m.getFromY()] = movedPiece;
        setHash(getHash() ^ zobristKeys[0][m.getToX()][m.getToY()][colorOfMove]);
        setHash(getHash() ^ zobristKeys[0][m.getFromX()][m.getFromY()][colorOfMove]);
        if(m.isCapture()) {
            getBoard()[m.getToX()][m.getToY()] = opponentPiece;
            setHash(getHash() ^ zobristKeys[0][m.getToX()][m.getToY()][otherColor]);
            if(colorOfMove==WHITE) {
                setBlackPieces(getBlackPieces()+1);
            } else {
                setWhitePieces(getWhitePieces()+1);
            }
        } else {
            getBoard()[m.getToX()][m.getToY()] = NO_PIECE;
        }
        //update legal moves
        initializeLegalMoves();
        setTurn(getTurn()-1);
        getMovesHashForColor()[m.getColorOfMove()] ^= zobristKeys[getTurn()][m.getFromX()][m.getFromY()][m.getMoveType()];
        getPiecesHashForColor()[colorOfMove] ^= zobristKeys[1][m.getFromX()][m.getFromY()][colorOfMove];
        getPiecesHashForColor()[colorOfMove] ^= zobristKeys[1][m.getToX()][m.getToY()][colorOfMove];
        if(m.isCapture()) {
            getPiecesHashForColor()[otherColor] ^= zobristKeys[1][m.getToX()][m.getToY()][otherColor];
        }
        setColorToPlay(previousColor());
    }

}
