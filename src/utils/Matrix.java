package utils;

import java.util.ArrayList;
import java.util.List;

public class Matrix {

    private double[][] data;
    private int rows, columns;

    public Matrix(int rows, int columns, boolean initializeRandomly) {
        data= new double[rows][columns];
        this.rows=rows;
        this.columns = columns;
        if(initializeRandomly) {
            for(int i=0;i<rows;i++) {
                for(int j = 0; j< columns; j++) {
                    data[i][j]=Math.random()*2-1;
                }
            }
        }
    }

    public static Matrix fromList(List<Double> x) {
        Matrix temp = new Matrix(x.size(),1, false);
        for(int i=0;i<x.size();i++) {
            temp.data[i][0] = x.get(i);
        }
        return temp;
    }

    public static Matrix multiply(Matrix a, Matrix b) {
        Matrix temp=new Matrix(a.rows,b.columns, false);
        for(int i=0;i<temp.rows;i++) {
            for(int j = 0; j<temp.columns; j++) {
                double sum=0;
                for(int k = 0; k<a.columns; k++) {
                    sum+=a.data[i][k]*b.data[k][j];
                }
                temp.data[i][j]=sum;
            }
        }
        return temp;
    }

    public static Matrix subtract(Matrix a, Matrix b) {
        Matrix temp=new Matrix(a.rows,a.columns, false);
        for(int i=0;i<a.rows;i++) {
            for(int j = 0; j<a.columns; j++) {
                temp.data[i][j]=a.data[i][j]-b.data[i][j];
            }
        }
        return temp;
    }

    public static Matrix transpose(Matrix a) {
        Matrix temp=new Matrix(a.columns,a.rows, false);
        for(int i=0;i<a.rows;i++) {
            for(int j = 0; j<a.columns; j++) {
                temp.data[j][i]=a.data[i][j];
            }
        }
        return temp;
    }

    public void add(int scalar) {
        for(int i=0;i<rows;i++) {
            for(int j = 0; j< columns; j++) {
                this.data[i][j]+=scalar;
            }
        }
    }

    public void add(Matrix m) {
        if(columns !=m.columns || rows!=m.rows) {
            System.out.println("Shape Mismatch");
            return;
        }
        for(int i=0;i<rows;i++) {
            for(int j = 0; j< columns; j++) {
                this.data[i][j]+=m.data[i][j];
            }
        }
    }

    public Matrix addBias() {
        if(columns !=1) {
            System.out.println("Unexpected shape for adding bias - should be vector with 1 column, has "+ columns +" columns");
            return null;
        }
        Matrix temp = new Matrix(rows+1, columns, false);
        for(int i=0;i<rows;i++) {
            temp.data[i+1][0] = data[i][0];
        }
        temp.data[0][0] = 1;
        return temp;
    }

    public Matrix dsigmoid() {
        Matrix temp=new Matrix(rows, columns, false);
        for(int i=0;i<rows;i++) {
            for(int j = 0; j< columns; j++)
                temp.data[i][j] = this.data[i][j] * (1-this.data[i][j]);
        }
        return temp;
    }

    public int getColumns() {
        return columns;
    }

    public int getRows() {
        return rows;
    }

    public void multiply(double a) {
        for(int i=0;i<rows;i++) {
            for(int j = 0; j< columns; j++) {
                this.data[i][j]*=a;
            }
        }
    }

    public void multiply(Matrix a) {
        for(int i=0;i<a.rows;i++) {
            for(int j = 0; j<a.columns; j++) {
                this.data[i][j]*=a.data[i][j];
            }
        }
    }

    public void setData(int row, int column, double value) {
        data[row][column] = value;
    }

    public void sigmoid() {
        for(int i=0;i<rows;i++) {
            for(int j = 0; j< columns; j++)
                this.data[i][j] = 1/(1+Math.exp(-this.data[i][j]));
        }
    }

    public void sign() {
        for(int i=0;i<rows;i++) {
            for(int j = 0; j< columns; j++)
                this.data[i][j] = this.data[i][j]>=0 ? 1 : -1;
        }
    }

    public int size() {
        return rows * columns;
    }

    public List<Double> toList() {
        List<Double> temp = new ArrayList<>();
        for(int i=0; i<rows; i++) {
            for(int j = 0; j< columns; j++) {
                temp.add(data[i][j]);
            }
        }
        return temp;
    }

    public String toString() {
        String result = "";
        for(int i=0;i<rows;i++) {
            for(int j = 0; j< columns; j++) {
                result += this.data[i][j]+" ";
            }
            result += "\r\n";
        }
        return result;
    }

}
