package ai;

public abstract class OnePieceTypeTwoPlayerBoard extends OnePieceTypeBoard {

    public static final int BLACK_PIECE = 2;
    public static final int WHITE_PIECE = 1;

    public OnePieceTypeTwoPlayerBoard(BasicGame game) {
        super(game);
        if(game.getNumberOfColors()!=2) {
            throw new IllegalArgumentException("Error: Trying to create a TwoPlayerBoard with a different player number than 2");
        }
    }

    public static int oppositePiece(int piece) {
        return piece==WHITE_PIECE ? BLACK_PIECE:WHITE_PIECE;
    }

    @Override
    public String pieceToString(int piece) {
        if(piece==NO_PIECE) return ".";
        if(piece==WHITE_PIECE) return "O";
        return "X";
    }

}
