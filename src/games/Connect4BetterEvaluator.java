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

public class Connect4BetterEvaluator extends StaticEvaluator {

    private double[] comboValues = {2,6,30,1000000};
    private double logisticGrowthRate = 400;

    private double getMaxValue(Connect4NoTranspositionsBoard board) {
        //very loose upper bound: opponent has no pieces on board, i have three pieces in every possible row of four.
        return comboValues[2]*((board.getWidth()-3)*board.getHeight()+(board.getHeight()-3)*board.getWidth()+2*(board.getHeight()*board.getWidth()-7)*(board.getHeight()-3));
    }

    public double linearCombinationEvalForWhite(Board boardToEvaluate) {
        Connect4NoTranspositionsBoard board = (Connect4NoTranspositionsBoard) boardToEvaluate;
        int boardWidth = board.getWidth();
        int boardHeight = board.getHeight();
        double result = 0;
        //check all possible 4 in a row on the entire board - give no points for empty ones or impossible ones, give X points for 1 piece, Y for 2 pieces, Z for 3 pieces
        //horizontals
        for (int y = 0; y < boardHeight; y++) {
            int[] whitePiecesIn4 = new int[boardWidth-3];
            int[] blackPiecesIn4 = new int[boardWidth-3];
            for(int x = 0; x < boardWidth; x++) {
                int piece = board.getSquare(x,y);
                if(piece==WHITE_PIECE) {
                    for(int i=Math.max(0,x-3);i<=Math.min(x,boardWidth-4);i++) {
                        whitePiecesIn4[i]++;
                    }
                } else if(piece==BLACK_PIECE) {
                    for(int i=Math.max(0,x-3);i<=Math.min(x,boardWidth-4);i++) {
                        blackPiecesIn4[i]++;
                    }
                }
            }
            for(int i=0;i<whitePiecesIn4.length;i++) {
                if(whitePiecesIn4[i]>0 && blackPiecesIn4[i]==0) {
                    result += comboValues[whitePiecesIn4[i]-1];
                } else if(whitePiecesIn4[i]==0 && blackPiecesIn4[i]>0) {
                    result -= comboValues[blackPiecesIn4[i]-1];
                }
            }
        }
        //verticals
        for (int x = 0; x < boardWidth; x++) {
            int[] whitePiecesIn4 = new int[boardHeight - 3];
            int[] blackPiecesIn4 = new int[boardHeight - 3];
            for (int y = 0; y < boardHeight; y++) {
                int piece = board.getSquare(x, y);
                if (piece == WHITE_PIECE) {
                    for (int i = Math.max(0, y - 3); i <= Math.min(y, boardHeight - 4); i++) {
                        whitePiecesIn4[i]++;
                    }
                } else if (piece == BLACK_PIECE) {
                    for (int i = Math.max(0, y - 3); i <= Math.min(y, boardHeight - 4); i++) {
                        blackPiecesIn4[i]++;
                    }
                }
            }
            for (int i = 0; i < whitePiecesIn4.length; i++) {
                if (whitePiecesIn4[i] > 0 && blackPiecesIn4[i] == 0) {
                    result += comboValues[whitePiecesIn4[i] - 1];
                } else if (whitePiecesIn4[i] == 0 && blackPiecesIn4[i] > 0) {
                    result -= comboValues[blackPiecesIn4[i] - 1];
                }
            }
        }
        //diagonal to top right
        for (int x = 4-boardHeight; x <= boardWidth-4; x++) {
            int[] whitePiecesIn4 = new int[boardHeight-3];
            int[] blackPiecesIn4 = new int[boardHeight-3];
            for(int offset = 0; offset < boardHeight; offset++) {
                int piece = board.getSquare(x+offset,offset);
                if(piece==OFF_BOARD) {
                    for(int i=Math.max(0,offset-3);i<=Math.min(offset,boardHeight-4);i++) {
                        whitePiecesIn4[i] = -10;
                        blackPiecesIn4[i] = -10;
                    }
                } else if(piece==WHITE_PIECE) {
                    for(int i=Math.max(0,offset-3);i<=Math.min(offset,boardHeight-4);i++) {
                        whitePiecesIn4[i]++;
                    }
                } else if(piece==BLACK_PIECE) {
                    for(int i=Math.max(0,offset-3);i<=Math.min(offset,boardHeight-4);i++) {
                        blackPiecesIn4[i]++;
                    }
                }
            }
            for(int i=0;i<whitePiecesIn4.length;i++) {
                if(whitePiecesIn4[i]>0 && blackPiecesIn4[i]==0) {
                    result += comboValues[whitePiecesIn4[i]-1];
                } else if(whitePiecesIn4[i]==0 && blackPiecesIn4[i]>0) {
                    result -= comboValues[blackPiecesIn4[i]-1];
                }
            }
        }
        //diagonal to top left
        for (int x = (boardWidth-1)-(4-boardHeight); x >= 3; x--) {
            int[] whitePiecesIn4 = new int[boardHeight-3];
            int[] blackPiecesIn4 = new int[boardHeight-3];
            for(int offset = 0; offset < boardHeight; offset++) {
                int piece = board.getSquare(x-offset,offset);
                if(piece==OFF_BOARD) {
                    for(int i=Math.max(0,offset-3);i<=Math.min(offset,boardHeight-4);i++) {
                        whitePiecesIn4[i] = -10;
                        blackPiecesIn4[i] = -10;
                    }
                } else if(piece==WHITE_PIECE) {
                    for(int i=Math.max(0,offset-3);i<=Math.min(offset,boardHeight-4);i++) {
                        whitePiecesIn4[i]++;
                    }
                } else if(piece==BLACK_PIECE) {
                    for(int i=Math.max(0,offset-3);i<=Math.min(offset,boardHeight-4);i++) {
                        blackPiecesIn4[i]++;
                    }
                }
            }
            for(int i=0;i<whitePiecesIn4.length;i++) {
                if(whitePiecesIn4[i]>0 && blackPiecesIn4[i]==0) {
                    result += comboValues[whitePiecesIn4[i]-1];
                } else if(whitePiecesIn4[i]==0 && blackPiecesIn4[i]>0) {
                    result -= comboValues[blackPiecesIn4[i]-1];
                }
            }
        }
        result = (result+getMaxValue(board))/(2*getMaxValue(board));
        return result;
    }

    public void setComboValues(int index, double comboValue) {
        comboValues[index] = comboValue;
    }

    public void setLogisticGrowthRate(double logisticGrowthRate) {
        this.logisticGrowthRate = logisticGrowthRate;
    }

    public void setValueForOnePieceIn4(double value) {
        comboValues[0] = value;
    }

    public void setValueForThreePiecesIn4(double value) {
        comboValues[2] = value;
    }

    public void setValueForTwoPiecesIn4(double value) {
        comboValues[1] = value;
    }

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
        String result = "Connect4BetterEvaluator (static) with values for connect-1: "+comboValues[0]+", connect-2: "+comboValues[1]+", connect-3: "+comboValues[2];
        return result;
    }

}
