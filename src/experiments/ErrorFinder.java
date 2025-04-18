package experiments;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ErrorFinder {

    private static void findProblems(String resultsDirectory) throws FileNotFoundException {
        File dir = new File(resultsDirectory);
        int numberOfGamesScanned = 0;
        for (String name : dir.list()) {
            if (name.endsWith(".game")) {
                numberOfGamesScanned++;
                if (numberOfGamesScanned % 100 == 0) {
                    System.out.println(numberOfGamesScanned + " games scanned");
                }
                File game = new File(resultsDirectory + name);
                Scanner s = new Scanner(game);
                while (s.hasNextLine()) {
                    String line = s.nextLine();
                    if (line.contains("Error") || line.contains("Exception")) {
                        System.out.println(name);
                    }
                }
                s.close();
            }
        }
    }


    public static void main(String[] args) throws FileNotFoundException {
            findProblems(experiments.ExperimentConfiguration.RESULTS_DIRECTORY);
    }

}
