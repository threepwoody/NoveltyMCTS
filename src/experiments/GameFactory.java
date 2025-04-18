package experiments;

import ai.Game;
import utils.UnknownPropertyException;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GameFactory {

    public static Game createGame(String args) {
        Game game = null;
            Scanner scanner = new Scanner(args).useDelimiter("-");
            List<String> properties = new ArrayList<>();
            String rawName = scanner.next();
            if (!rawName.startsWith("games.")) {
                rawName = "games." + rawName;
            }
            List<Class> argumentClasses = new ArrayList<>();
            List<Integer> integerArguments = new ArrayList<>();
            //first scanning arguments on number of colors and/or board size
            while(scanner.hasNextInt()) {
                integerArguments.add(scanner.nextInt());
                argumentClasses.add(int.class);
            }
            //then scanning additional properties
            while(scanner.hasNext()) {
                properties.add(scanner.next());
            }
            if(integerArguments.size()>0) {
                //game that specifies the number of colors and/or the board size
                Object[] argumentArray = integerArguments.toArray();
                Class[] argumentClassArray = argumentClasses.toArray(new Class[0]);
                try {
                    game = (Game) Class.forName(rawName).getDeclaredConstructor(argumentClassArray).newInstance(argumentArray);
                } catch (Exception e) {
                    System.err.println("Cannot construct game: " + rawName);
                    e.printStackTrace();
                }
            } else {
                //default game without parameters
                try {
                    game = (Game) Class.forName(rawName).getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    System.err.println("Cannot construct game: " + rawName);
                    e.printStackTrace();
                }
            }
            //setting additional parameters
        try {
            for (int i = 0; i < properties.size(); i++) {
                game.setProperty(properties.get(i), "");
            }
        } catch(UnknownPropertyException e) {
            System.err.println("In "+args+": ");
            System.err.println(e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }
        return game;
    }

}
