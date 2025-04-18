package ai;

public abstract class BasicRectangularBoardGame extends BasicGame implements RectangularBoardGame {

    private final int boardHeight;
    private final int boardWidth;

    public BasicRectangularBoardGame(int numberOfColors, int boardWidth, int boardHeight) {
        super(numberOfColors);
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
    }

    public int getBoardHeight() {
        return boardHeight;
    }

    public int getBoardWidth() {
        return boardWidth;
    }

}
