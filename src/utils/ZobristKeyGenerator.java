package utils;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Random;

public class ZobristKeyGenerator {

    private int numberOfKeys = 400000;
    private int bitsPerKey = 64;
    private String directory = "C://results//";
    private String fileName = "zobristkeys.txt";
    private Random random = new Random();
    private PrintWriter out;

    public ZobristKeyGenerator() {
        try {
            out = new PrintWriter(directory+fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void generateKeys() {
        for(int line=0;line<numberOfKeys;line++) {
            for(int bit=0;bit<bitsPerKey;bit++) {
                int nextBit = random.nextBoolean() ? 1 : 0;
                out.print(nextBit);
            }
            out.println();
        }
        out.flush();
        out.close();
    }

    public static void main(String[] args) {
        ZobristKeyGenerator generator = new ZobristKeyGenerator();
        generator.generateKeys();
    }

}
