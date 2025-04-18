package utils;

import ai.Game;
import ai.djl.Device;
import ai.djl.engine.Engine;
import ai.djl.ndarray.NDList;
import experiments.GameFactory;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;


public class Util {

    public static boolean DEBUG = false;
    public static HashMap<String, Integer> fileLetterToNumber;
    public static HashMap<Integer,String> fileNumberToLetter;
    public static Map<Integer, int[][]> playerAssignmentsToColorsForNumbersOfColors;
    //some percentiles of the standard normal distribution
    static Map<Double, Double> zValueForConfidenceLevel;

    static {
        zValueForConfidenceLevel = new HashMap<>();
        zValueForConfidenceLevel.put(0.5, 0.67449);
        zValueForConfidenceLevel.put(0.7, 1.04);
        zValueForConfidenceLevel.put(0.75, 1.15035);
        zValueForConfidenceLevel.put(0.8, 1.282);
        zValueForConfidenceLevel.put(0.85, 1.44);
        zValueForConfidenceLevel.put(0.9, 1.64485);
        zValueForConfidenceLevel.put(0.92, 1.75);
        zValueForConfidenceLevel.put(0.95, 1.95996);
        zValueForConfidenceLevel.put(0.97, 2.17009);
        zValueForConfidenceLevel.put(0.98, 2.326);
        zValueForConfidenceLevel.put(0.99, 2.57583);
        zValueForConfidenceLevel.put(0.999, 3.29053);

        fileNumberToLetter = new HashMap<>();
        fileNumberToLetter.put(0, "a");
        fileNumberToLetter.put(1, "b");
        fileNumberToLetter.put(2, "c");
        fileNumberToLetter.put(3, "d");
        fileNumberToLetter.put(4, "e");
        fileNumberToLetter.put(5, "f");
        fileNumberToLetter.put(6, "g");
        fileNumberToLetter.put(7, "h");
        fileNumberToLetter.put(8, "i");
        fileNumberToLetter.put(9, "j");
        fileNumberToLetter.put(10, "k");
        fileNumberToLetter.put(11, "l");
        fileNumberToLetter.put(12, "m");
        fileNumberToLetter.put(13, "n");
        fileNumberToLetter.put(14, "o");
        fileNumberToLetter.put(15, "p");
        fileNumberToLetter.put(16, "q");

        fileLetterToNumber = new HashMap<>();
        fileLetterToNumber.put("a", 0);
        fileLetterToNumber.put("b", 1);
        fileLetterToNumber.put("c", 2);
        fileLetterToNumber.put("d", 3);
        fileLetterToNumber.put("e", 4);
        fileLetterToNumber.put("f", 5);
        fileLetterToNumber.put("g", 6);
        fileLetterToNumber.put("h", 7);
        fileLetterToNumber.put("i", 8);
        fileLetterToNumber.put("j", 9);
        fileLetterToNumber.put("k", 10);
        fileLetterToNumber.put("l", 11);
        fileLetterToNumber.put("m", 12);
        fileLetterToNumber.put("n", 13);
        fileLetterToNumber.put("o", 14);
        fileLetterToNumber.put("p", 15);
        fileLetterToNumber.put("q", 16);

        playerAssignmentsToColorsForNumbersOfColors = new HashMap<>();
        int[][] playerAssignmentsToColorsFor2Colors = {
                {0,1},
                {1,0}
        };
        int[][] playerAssignmentsToColorsFor3Colors = {
                {1,0,0},
                {0,1,0},
                {0,0,1},
                {1,1,0},
                {1,0,1},
                {0,1,1}
        };
        int[][] playerAssignmentsToColorsFor4Colors = {
                {1,0,0,0},
                {0,1,0,0},
                {0,0,1,0},
                {0,0,0,1},
                {1,1,0,0},
                {0,1,1,0},
                {0,0,1,1},
                {1,0,1,0},
                {0,1,0,1},
                {1,0,0,1},
                {1,1,1,0},
                {0,1,1,1},
                {1,1,0,1},
                {1,0,1,1},
        };
        int[][] playerAssignmentsToColorsFor6Colors = {
                {1,0,0,0,0,0},
                {0,1,0,0,0,0},
                {0,0,1,0,0,0},
                {0,0,0,1,0,0},
                {0,0,0,0,1,0},
                {0,0,0,0,0,1},
                {1,1,0,0,0,0},
                {0,1,1,0,0,0},
                {0,0,1,1,0,0},
                {0,0,0,1,1,0},
                {0,0,0,0,1,1},
                {1,0,1,0,0,0},
                {0,1,0,1,0,0},
                {0,0,1,0,1,0},
                {0,0,0,1,0,1},
                {1,0,0,1,0,0},
                {0,1,0,0,1,0},
                {0,0,1,0,0,1},
                {1,0,0,0,1,0},
                {0,1,0,0,0,1},
                {1,0,0,0,0,1},
                {1,1,1,0,0,0},
                {0,1,1,1,0,0},
                {0,0,1,1,1,0},
                {0,0,0,1,1,1},
                {1,1,0,1,0,0},
                {0,1,1,0,1,0},
                {0,0,1,1,0,1},
                {1,1,0,0,1,0},
                {0,1,1,0,0,1},
                {1,1,0,0,0,1},
                {1,0,1,1,0,0},
                {0,1,0,1,1,0},
                {0,0,1,0,1,1},
                {1,0,0,1,1,0},
                {0,1,0,0,1,1},
                {1,0,0,0,1,1},
                {1,0,1,0,1,0},
                {0,1,0,1,0,1},
                {1,0,0,1,0,1},
                {1,0,1,0,0,1},
                {0,0,1,1,1,1},
                {1,0,0,1,1,1},
                {1,1,0,0,1,1},
                {1,1,1,0,0,1},
                {1,1,1,1,0,0},
                {0,1,0,1,1,1},
                {1,0,1,0,1,1},
                {1,1,0,1,0,1},
                {1,1,1,0,1,0},
                {0,1,1,0,1,1},
                {1,0,1,1,0,1},
                {1,1,0,1,1,0},
                {0,1,1,1,0,1},
                {1,0,1,1,1,0},
                {0,1,1,1,1,0},
                {0,1,1,1,1,1},
                {1,0,1,1,1,1},
                {1,1,0,1,1,1},
                {1,1,1,0,1,1},
                {1,1,1,1,0,1},
                {1,1,1,1,1,0}
        };
        playerAssignmentsToColorsForNumbersOfColors.put(2, playerAssignmentsToColorsFor2Colors);
        playerAssignmentsToColorsForNumbersOfColors.put(3, playerAssignmentsToColorsFor3Colors);
        playerAssignmentsToColorsForNumbersOfColors.put(4, playerAssignmentsToColorsFor4Colors);
        playerAssignmentsToColorsForNumbersOfColors.put(6, playerAssignmentsToColorsFor6Colors);
    }

    public static Device bestAvailableDevice() {
        return Engine.getInstance().getGpuCount() > 0 ? Device.gpu(0) : Device.cpu();
    }

    public static Device[] bestAvailableDevices() {
       int GPUCount = Engine.getInstance().getGpuCount();
       if(GPUCount == 0) {
           return new Device[]{Device.cpu()};
       }
       Device[] devices = new Device[GPUCount];
       for(int i = 0; i < GPUCount; i++) {
           devices[i] = Device.gpu(i);
       }
       return devices;
    }

    //creates a 64-bit zobrist key from a 64-char array
    public static long charArrayToZobristKey(char[] array) {
        long key = 0;
        int power = 0;
        if(array[0]=='0') {
            for(int l=63;l>=1;l--) {
                key = key + ((array[l]=='1' ? 1 : 0)*(long)(Math.pow(2.0,power)));
                power += 1;
            }
        }
        if(array[0]=='1') {
            key = (long)-Math.pow(2.0,63);
            for(int l=63;l>=1;l--) {
                key = key + ((array[l]=='1' ? 1 : 0)*(long)(Math.pow(2.0,power)));
                power += 1;
            }
        }
        return key;
    }

    public static Double[] convertFloatArrayToDoubleArray(float[] input) {
        Double[] output = new Double[input.length];
        for (int i = 0; i < input.length; i++)
        {
            output[i] = Double.valueOf(input[i]);
        }
        return output;
    }

    public static void copyFile(File source, File dest) throws IOException {
        FileChannel sourceChannel = null;
        FileChannel destChannel = null;
        try {
            sourceChannel = new FileInputStream(source).getChannel();
            destChannel = new FileOutputStream(dest).getChannel();
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        }finally{
            sourceChannel.close();
            destChannel.close();
        }
    }

    public static List<String> expandTemplateIntoPlayers(List<String> players, String prefixOfPlayer, List<String> restOfTemplate) {
        if(restOfTemplate.isEmpty()) {
            players.add(prefixOfPlayer);
        } else {
            String argument = restOfTemplate.get(0);
            // Split argument at the equals sign
            int j = argument.indexOf('=');
            String left, right;
            if (j > 0) {
                left = argument.substring(0, j);
                right = argument.substring(j + 1);
            } else {
                left = argument;
                right = "true";
            }
            //If this argument is a template (has multiple values), recursively add all values to the known prefix
            if (!right.contains(",")) {
                expandTemplateIntoPlayers(players,  prefixOfPlayer + (prefixOfPlayer.isEmpty() ? "" : " ") + argument, restOfTemplate.subList(1, restOfTemplate.size()));
            } else {
                for (String value : right.split(",")) {
                    expandTemplateIntoPlayers(players, prefixOfPlayer + (prefixOfPlayer.isEmpty() ? "" : " ") + left + "=" + value, restOfTemplate.subList(1, restOfTemplate.size()));
                }
            }
        }
        return players;
    }

    public static String fillInAgentTemplate(String template, Map<String, String> solution) {
        String result = "";
        String[] tokens = template.split(" ");
        for(String token : tokens) {
            if(token.contains("=")) {
                int j = token.indexOf('=');
                String value = token.substring(j+1);
                if(value.contains(",")) {
                    String parameter = token.substring(0,j);
                    result += parameter + "=" + solution.get(parameter) + " ";
                } else {
                    result += token + " ";
                }
            } else {
                result += token + " ";
            }
        }
        result = result.substring(0,result.length()-1);
        return result;
    }

    public static Game findGameInPlayerString(String player) {
        if (player.contains("game=")) {
            String gameClass = player.substring(player.indexOf("game="));
            int endIndex = gameClass.indexOf(" ");
            if(endIndex==-1) endIndex = gameClass.length();
            gameClass = gameClass.substring(0, endIndex);
            gameClass = gameClass.substring(gameClass.indexOf("=") + 1);
            return GameFactory.createGame(gameClass);
        }
        return null;
    }

    public static boolean isClass(String className) {
        try  {
            Class.forName(className);
            return true;
        }  catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isDebug() {
        return DEBUG;
    }

    public static boolean isInteger(String s) {
        return isInteger(s,10);
    }

    public static boolean isInteger(String s, int radix) {
        if(s.isEmpty()) return false;
        for(int i = 0; i < s.length(); i++) {
            if(i == 0 && s.charAt(i) == '-') {
                if(s.length() == 1) return false;
                else continue;
            }
            if(Character.digit(s.charAt(i),radix) < 0) return false;
        }
        return true;
    }

    public static double logisticFunction(double input, double growthRate, double midPoint) {
        return 1/(1+Math.exp(-growthRate*(input-midPoint)));
    }

    //computes the lower bound of the wilson score interval with continuity correction, 95% confidence (two-tailed)
    public static double lowerConfidenceBound(double count, int sampleSize, double confidenceLevel) {
        if(sampleSize==0) {
            return 0;
        }
        double proportion = count/(double)sampleSize;
        if(proportion==0.0) {
            return 0;
        }
        //rule of three, generalized to other confidence levels (https://en.wikipedia.org/wiki/Rule_of_three_(statistics))
        if(proportion==1.0) {
            return Math.max(0,1-(-Math.log(1-confidenceLevel))/sampleSize);
        }
        double z = zValueForConfidenceLevel.get(confidenceLevel);
        double denominator = 2*(sampleSize + Math.pow(z, 2));
        double term1 = 2*sampleSize*proportion;
        double term2 = Math.pow(z, 2);
        double term3 = Math.pow(z, 2) - 1.0/sampleSize + 4*sampleSize*proportion*(1-proportion) + (4*proportion-2);
        double term4 = z*Math.sqrt(term3)+1;
        double numerator = term1 + term2 - term4;
        return Math.max(0, numerator/denominator);
    }

    public static void main(String[] args) {
        List<Object> allArguments = new ArrayList<>();
        allArguments.add(5);
        System.out.println(allArguments);
        Object[] argumentArray = allArguments.toArray();
        for(int i=0;i< argumentArray.length;i++) {
            System.out.println(argumentArray[i]);
        }
        String rawName = "games.Breakthrough";
        Game result = null;
        try {
            result = (Game) Class.forName(rawName).getDeclaredConstructor(int.class).newInstance(argumentArray);
        } catch (Exception e) {
            System.err.println("Cannot construct game: " + rawName);
            e.printStackTrace();
        }
        System.out.println(result.getName());
    }

    //matrix width must be equal to vector length
    public static double[] matrixVectorMultiplication(double[][] matrix, double[] vector) {
        if(matrix[0].length!=vector.length) {
            System.err.println("matrix-vector multiplication is defined for matrices with the same width as the vector length");
            System.err.println("matrix width given: "+matrix[0].length);
            System.err.println("vector length given: "+vector.length);
            System.exit(1);
        }
        double[] result = new double[matrix.length];
        for(int row=0; row<matrix.length; row++) {
            for(int column=0; column<matrix[0].length; column++) {
                result[row] += matrix[row][column] * vector[column];
            }
        }
        return result;
    }

    public static String printMatrix(double[][] matrix) {
        String result = "";
        for(int rowColor=0; rowColor<matrix.length; rowColor++) {
            for(int columnColor=0; columnColor<matrix[rowColor].length; columnColor++) {
                result += matrix[rowColor][columnColor] + " ";
            }
            result += "\r\n";
        }
        return result;
    }

    public static void printStringToFile(String string, String fileName, boolean append) {
        File file = new File(System.getProperty("user.dir") + File.separator + fileName);
        file.getParentFile().mkdirs();
        PrintWriter out;
        try {
            out = new PrintWriter(new FileOutputStream(file, append));
            out.print(string);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static String print3DimFloatNDList(NDList NDlist) {
        String result = "";
        for(int i=0; i<NDlist.size(); i++) {
            result += i+":\r\n\r\n";
            long[] shape = NDlist.get(i).getShape().getShape();
            for(int j=0; j<shape[0]; j++) {
                for(int k=0; k<shape[1]; k++) {
                    for(int l=0; l<shape[2]; l++) {
                        result += NDlist.get(i).get(j, k, l).getFloat()+" ";
                    }
                    result += "\r\n";
                }
                result += "\r\n";
            }
            result += "\r\n";
        }
        return result;
    }

    public static String print1DimFloatNDList(NDList NDlist) {
        String result = "";
        for(int i=0; i<NDlist.size(); i++) {
            result += i+":\r\n\r\n";
            long[] shape = NDlist.get(i).getShape().getShape();
            for(int j=0; j<shape[0]; j++) {
                result += NDlist.get(i).get(j).getFloat()+" ";
            }
            result += "\r\n";
        }
        return result;
    }

    public static String printVector(double[] vector) {
        String result = "";
        for(int color=0; color<vector.length; color++) {
            result += vector[color] + " ";
        }
        return result;
    }

    public static String printVector(float[] vector) {
        String result = "";
        for(int color=0; color<vector.length; color++) {
            result += vector[color] + " ";
        }
        return result;
    }

    public static String printVector(int[] vector) {
        String result = "";
        for(int color=0; color<vector.length; color++) {
            result += vector[color] + " ";
        }
        return result;
    }

    public static String printVector(long[] vector) {
        String result = "";
        for(int color=0; color<vector.length; color++) {
            result += vector[color] + " ";
        }
        return result;
    }

    public static double round(double value, int places) {
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public static double[] softMax(double[] inputs, double temperature) {
        double denominator = 0;
        double midPoint = 1.0/inputs.length;
        for(double input : inputs) {
            denominator += Math.exp((input-midPoint)/temperature);
        }
        double[] outputs = new double[inputs.length];
        for(int i=0; i<outputs.length; i++) {
            outputs[i] = Math.exp((inputs[i]-midPoint)/temperature)/denominator;
        }
        return outputs;
    }

    public static String tail( File file ) {
        try {
            RandomAccessFile fileHandler = new RandomAccessFile( file, "r" );
            long fileLength = file.length() - 1;
            StringBuilder sb = new StringBuilder();

            for( long filePointer = fileLength; filePointer != -1; filePointer-- ) {
                fileHandler.seek( filePointer );
                int readByte = fileHandler.readByte();

                if( readByte == 0xA ) {
                    if( filePointer == fileLength ) {
                        continue;
                    } else {
                        break;
                    }
                } else if( readByte == 0xD ) {
                    if( filePointer == fileLength - 1 ) {
                        continue;
                    } else {
                        break;
                    }
                }

                sb.append( ( char ) readByte );
            }

            String lastLine = sb.reverse().toString();
            fileHandler.close();
            return lastLine;
        } catch( java.io.FileNotFoundException e ) {
            e.printStackTrace();
            return null;
        } catch( java.io.IOException e ) {
            e.printStackTrace();
            return null;
        }
    }

    //computes the upper bound of the wilson score interval with continuity correction, 95% confidence (two-tailed)
    public static double upperConfidenceBound(double count, int sampleSize, double confidenceLevel) {
        if(sampleSize==0) {
            return 0;
        }
        double proportion = count/(double)sampleSize;
        //rule of three, generalized to other confidence levels (https://en.wikipedia.org/wiki/Rule_of_three_(statistics))
        if(proportion==0.0) {
            return Math.min(1,(-Math.log(1-confidenceLevel))/sampleSize);
        }
        if(proportion==1.0) {
            return 1;
        }
        double z = zValueForConfidenceLevel.get(confidenceLevel);
        double denominator = 2*(sampleSize + Math.pow(z, 2));
        double term1 = 2*sampleSize*proportion;
        double term2 = Math.pow(z, 2);
        double term3 = Math.pow(z, 2) - 1.0/sampleSize + 4*sampleSize*proportion*(1-proportion) - (4*proportion-2);
        double term4 = z*Math.sqrt(term3)+1;
        double numerator = term1 + term2 + term4;
        return Math.min(1, numerator/denominator);
    }

    //both vectors must have the same length
    public static double[] vectorAddition(double[] vector1, double[] vector2) {
        if(vector1.length!=vector2.length) {
            System.err.println("vector addition is defined on vectors of equal length");
            System.err.println("vector 1 length given: "+vector1.length);
            System.err.println("vector 2 length given: "+vector2.length);
            System.exit(1);
        }
        double[] result = new double[vector1.length];
        for(int i=0; i<result.length; i++) {
            result[i] = vector1[i]+vector2[i];
        }
        return result;
    }

}
