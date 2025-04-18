package ai;

import java.util.List;

public interface MoveSorter {

    int compareMoves(Move move1, Move move2, Board board);;

    default void prepareSort(List<Move> legalMoves, Board board) {};

    default List<Move> sortMoves(List<Move> legalMoves, Board board) {
        prepareSort(legalMoves, board);
        legalMoves.sort((m1, m2) -> compareMoves(m2, m1, board));
        return legalMoves;
    }

}
