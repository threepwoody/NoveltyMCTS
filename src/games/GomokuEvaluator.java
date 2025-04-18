package games;

import ai.Board;
import ai.Evaluation;
import ai.evaluation.StaticEvaluator;
import utils.Util;

import static ai.BasicBoard.OFF_BOARD;
import static ai.Game.BLACK;
import static ai.Game.WHITE;
import static ai.OnePieceTypeTwoPlayerBoard.BLACK_PIECE;
import static ai.OnePieceTypeTwoPlayerBoard.WHITE_PIECE;

public class GomokuEvaluator extends StaticEvaluator {

    private double[] comboValues = {2,8,32,128};
    private double logisticGrowthRate = 400;

    private double getMaxValue(GomokuNoTranspositionsBoard board) {
        //very loose upper bound: opponent has no pieces on board, i have four pieces in every possible row of five.
        return comboValues[3]*((board.getWidth()-3)*board.getHeight()+(board.getHeight()-3)*board.getWidth()+2*(board.getHeight()*board.getWidth()-7)*(board.getHeight()-3));
    }

    public double linearCombinationEvalForWhite(Board boardToEvaluate) {
        GomokuNoTranspositionsBoard board = (GomokuNoTranspositionsBoard) boardToEvaluate;
        int boardWidth = board.getWidth();
        int boardHeight = board.getHeight();
        double result = 0;
        //check all possible 5 in a row on the entire board - give no points for empty ones or impossible ones, give A points for 1 piece, B for 2 pieces, C for 3 pieces, D for 4 pieces
        //horizontals
        for (int y = 0; y < boardHeight; y++) {
            int[] whitePiecesIn5 = new int[boardWidth-4];
            int[] blackPiecesIn5 = new int[boardWidth-4];
            for(int x = 0; x < boardWidth; x++) {
                int piece = board.getSquare(x,y);
                if(piece==WHITE_PIECE) {
                    for(int i=Math.max(0,x-4);i<=Math.min(x,boardWidth-5);i++) {
                        whitePiecesIn5[i]++;
                    }
                } else if(piece==BLACK_PIECE) {
                    for(int i=Math.max(0,x-4);i<=Math.min(x,boardWidth-5);i++) {
                        blackPiecesIn5[i]++;
                    }
                }
            }
            for(int i=0;i<whitePiecesIn5.length;i++) {
                if(whitePiecesIn5[i]>0 && blackPiecesIn5[i]==0) {
                    result += comboValues[whitePiecesIn5[i]-1];
                } else if(whitePiecesIn5[i]==0 && blackPiecesIn5[i]>0) {
                    result -= comboValues[blackPiecesIn5[i]-1];
                }
            }
        }
        //verticals
        for (int x = 0; x < boardWidth; x++) {
            int[] whitePiecesIn5 = new int[boardHeight - 4];
            int[] blackPiecesIn5 = new int[boardHeight - 4];
            for (int y = 0; y < boardHeight; y++) {
                int piece = board.getSquare(x, y);
                if (piece == WHITE_PIECE) {
                    for (int i = Math.max(0, y - 4); i <= Math.min(y, boardHeight - 5); i++) {
                        whitePiecesIn5[i]++;
                    }
                } else if (piece == BLACK_PIECE) {
                    for (int i = Math.max(0, y - 4); i <= Math.min(y, boardHeight - 5); i++) {
                        blackPiecesIn5[i]++;
                    }
                }
            }
            for (int i = 0; i < whitePiecesIn5.length; i++) {
                if (whitePiecesIn5[i] > 0 && blackPiecesIn5[i] == 0) {
                    result += comboValues[whitePiecesIn5[i] - 1];
                } else if (whitePiecesIn5[i] == 0 && blackPiecesIn5[i] > 0) {
                    result -= comboValues[blackPiecesIn5[i] - 1];
                }
            }
        }
        //diagonal to top right
        for (int x = 5-boardHeight; x <= boardWidth-5; x++) {
            int[] whitePiecesIn5 = new int[boardHeight-4];
            int[] blackPiecesIn5 = new int[boardHeight-4];
            for(int offset = 0; offset < boardHeight; offset++) {
                int piece = board.getSquare(x+offset,offset);
                if(piece==OFF_BOARD) {
                    for(int i=Math.max(0,offset-4);i<=Math.min(offset,boardHeight-5);i++) {
                        whitePiecesIn5[i] = -10;
                        blackPiecesIn5[i] = -10;
                    }
                } else if(piece==WHITE_PIECE) {
                    for(int i=Math.max(0,offset-4);i<=Math.min(offset,boardHeight-5);i++) {
                        whitePiecesIn5[i]++;
                    }
                } else if(piece==BLACK_PIECE) {
                    for(int i=Math.max(0,offset-4);i<=Math.min(offset,boardHeight-5);i++) {
                        blackPiecesIn5[i]++;
                    }
                }
            }
            for(int i=0;i<whitePiecesIn5.length;i++) {
                if(whitePiecesIn5[i]>0 && blackPiecesIn5[i]==0) {
                    result += comboValues[whitePiecesIn5[i]-1];
                } else if(whitePiecesIn5[i]==0 && blackPiecesIn5[i]>0) {
                    result -= comboValues[blackPiecesIn5[i]-1];
                }
            }
        }
        //diagonal to top left
        for (int x = (boardWidth-1)-(5-boardHeight); x >= 4; x--) {
            int[] whitePiecesIn5 = new int[boardHeight-4];
            int[] blackPiecesIn5 = new int[boardHeight-4];
            for(int offset = 0; offset < boardHeight; offset++) {
                int piece = board.getSquare(x-offset,offset);
                if(piece==OFF_BOARD) {
                    for(int i=Math.max(0,offset-4);i<=Math.min(offset,boardHeight-5);i++) {
                        whitePiecesIn5[i] = -10;
                        blackPiecesIn5[i] = -10;
                    }
                } else if(piece==WHITE_PIECE) {
                    for(int i=Math.max(0,offset-4);i<=Math.min(offset,boardHeight-5);i++) {
                        whitePiecesIn5[i]++;
                    }
                } else if(piece==BLACK_PIECE) {
                    for(int i=Math.max(0,offset-4);i<=Math.min(offset,boardHeight-5);i++) {
                        blackPiecesIn5[i]++;
                    }
                }
            }
            for(int i=0;i<whitePiecesIn5.length;i++) {
                if(whitePiecesIn5[i]>0 && blackPiecesIn5[i]==0) {
                    result += comboValues[whitePiecesIn5[i]-1];
                } else if(whitePiecesIn5[i]==0 && blackPiecesIn5[i]>0) {
                    result -= comboValues[blackPiecesIn5[i]-1];
                }
            }
        }
        result = (result+getMaxValue(board))/(2*getMaxValue(board));
        return result;
    }

    public void setValueForFourPiecesIn5(double value) {
        comboValues[3] = value;
    }

    public void setValueForOnePieceIn5(double value) {
        comboValues[0] = value;
    }

    public void setValueForThreePiecesIn5(double value) {
        comboValues[2] = value;
    }

    public void setValueForTwoPiecesIn5(double value) {
        comboValues[1] = value;
    }

    public void setLogisticGrowthRate(int value) {logisticGrowthRate = value;}

    @Override
    public Evaluation staticEval(Board board) {
        double[] evalForColor = new double[2];
        double whiteEval = Util.logisticFunction(linearCombinationEvalForWhite(board), logisticGrowthRate, 0.5);
        evalForColor[WHITE] = whiteEval;
        evalForColor[BLACK] = 1 - whiteEval;
        return new Evaluation(evalForColor);
    }

    @Override
    public String toString() {
        String result = "GomokuEvaluator (static) with values for connect-1: "+comboValues[0]+", connect-2: "+comboValues[1]+", connect-3: "+comboValues[2];
        return result;
    }

}
