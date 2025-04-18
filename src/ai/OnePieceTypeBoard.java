package ai;

public abstract class OnePieceTypeBoard extends BasicBoard {

    public OnePieceTypeBoard(BasicGame game) {
        super(game);
    }

    public static int colorOfPiece(int piece) {
        return piece-1;
    }

    public static int pieceOfColor(int color) {
        return color+1;
    }

    public String pieceToString(int piece) {
        if(piece==OFF_BOARD) return ",";
        if(piece==NO_PIECE) return ".";
        return ""+(piece-1);
    }

}
