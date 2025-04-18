package games;

import ai.Board;
import ai.Move;
import ai.MoveSorter;

public class OthelloMoveSorter implements MoveSorter {

    private int[][] locationValues6x6 =
            {
                    {50,-20,5,5,-20,50},
                    {-20,-50,-2,-2,-50,-20},
                    {5,-2,-1,-1,-2,5},
                    {5,-2,-1,-1,-2,5},
                    {-20,-50,-2,-2,-50,-20},
                    {50,-20,5,5,-20,50},
            };
    private int[][] locationValues8x8 =
            {
                    {100,-20,10,5,5,10,-20,100},
                    {-20,-50,-2,-2,-2,-2,-50,-20},
                    {10,-2,-1,-1,-1,-1,-2,10},
                    {5,-2,-1,-1,-1,-1,-2,5},
                    {5,-2,-1,-1,-1,-1,-2,5},
                    {10,-2,-1,-1,-1,-1,-2,10},
                    {-20,-50,-2,-2,-2,-2,-50,-20},
                    {100,-20,10,5,5,10,-20,100}
            };

    @Override
    public int compareMoves(Move move1, Move move2, Board board) {
        OthelloMove m1 = (OthelloMove) move1;
        OthelloMove m2 = (OthelloMove) move2;
        int[][] locationValues = null;
        if(board.getHeight()==8) {
            locationValues = locationValues8x8;
        } else if(board.getHeight()==6) {
            locationValues = locationValues6x6;
        } else {
            System.out.println("Unsupported board size");
            System.exit(1);
        }
        int pieceValueOfMove1 = locationValues[m1.getX()][m1.getY()];
        int pieceValueOfMove2 = locationValues[m2.getX()][m2.getY()];
        return Integer.compare(pieceValueOfMove1, pieceValueOfMove2);
    }

}
