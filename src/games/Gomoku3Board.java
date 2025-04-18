package games;

import static ai.Game.BLACK;
import static ai.Game.WHITE;

public class Gomoku3Board extends GomokuNoTranspositionsBoard {

    public Gomoku3Board(Gomoku game) {
        super(game);
    }

    public Gomoku3Board(Gomoku game, String board, boolean isFileName) {
        super(game, board, isFileName);
    }

    @Override
    protected void findLegalMoves() {
        getLegalMovesFor(WHITE).clear();
        getLegalMovesFor(BLACK).clear();
        getLegalMovesFor(BLACK).add(new GomokuMove(getWidth()/2,getHeight()/2,BLACK));
        for (int x = 0; x < getBoard().length; x++) {
            for (int y = 0; y < getBoard()[0].length; y++) {
                int piece = getBoard()[x][y];
                if (piece == NO_PIECE) {
                    getLegalMovesFor(WHITE).add(new GomokuMove(x, y, WHITE));
                }
            }
        }
    }

    @Override
    protected void updateLegalMoves(GomokuMove m) {
        if(getTurn()==0) {
            //black just played in the middle of the board
            getLegalMovesFor(WHITE).remove(new GomokuMove(m.getX(), m.getY(), WHITE));
            getLegalMovesFor(BLACK).remove(new GomokuMove(m.getX(), m.getY(), BLACK));
            for (int x = 0; x < getBoard().length; x++) {
                for (int y = 0; y < getBoard()[0].length; y++) {
                    if(Math.abs(getWidth()/2-x)<3 && Math.abs(getHeight()/2-y)<3) {
                        continue;
                    }
                    int piece = getBoard()[x][y];
                    if (piece == NO_PIECE) {
                        getLegalMovesFor(BLACK).add(new GomokuMove(x,y,BLACK));
                    }
                }
            }
        } else if(getTurn()==2) {
            getLegalMovesFor(WHITE).remove(new GomokuMove(m.getX(), m.getY(), WHITE));
            getLegalMovesFor(BLACK).clear();
            for (int x = 0; x < getBoard().length; x++) {
                for (int y = 0; y < getBoard()[0].length; y++) {
                    int piece = getBoard()[x][y];
                    if (piece == NO_PIECE) {
                        getLegalMovesFor(BLACK).add(new GomokuMove(x, y, BLACK));
                    }
                }
            }
        } else {
            getLegalMovesFor(WHITE).remove(new GomokuMove(m.getX(), m.getY(), WHITE));
            getLegalMovesFor(BLACK).remove(new GomokuMove(m.getX(), m.getY(), BLACK));
        }
    }

}
