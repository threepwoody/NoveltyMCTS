package experiments;

import ai.Player;
import utils.UnknownPropertyException;

import java.util.ArrayList;

import static utils.Util.isClass;

public class PlayerFactory {

    public static synchronized Player createPlayer(String[] args) throws UnknownPropertyException {
        Player player = buildAndInitializePlayer(args);
        player.setProperty("randomized", "true");
        player.initialize();
        return player;
    }

    private static Player buildAndInitializePlayer(String[] args) throws UnknownPropertyException {
        Player player = null;
        ArrayList<String> properties = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();
        // default setting
        String playerClass = "MCTS";
        // Parse arguments
        for (int i = 0; i < args.length; i++) {
            String argument = args[i];
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
            // Handle properties
            if (left.equals("player")) {
                playerClass = right;
            } else { // Let the player set this property
                properties.add(left);
                values.add(right);
            }
        }
        try { // Create player from string
            String qualifiedPlayerClass = playerClass;
            Player prototype = null;
            if (!qualifiedPlayerClass.startsWith("ai.")) {
                String newvalue = "ai." + qualifiedPlayerClass;
                if(isClass(newvalue)) {
                    try {
                        prototype = (Player) Class.forName(newvalue).getDeclaredConstructor().newInstance();
                    } catch (Exception e) {
                        System.err.println("Cannot construct player: " + newvalue);
                        e.printStackTrace();
                        System.exit(1);
                    }
                    player = prototype;
                }
            }
            if (player==null && !qualifiedPlayerClass.startsWith("ai.multiplayer.")) {
                String newvalue = "ai.multiplayer." + qualifiedPlayerClass;
                if(isClass(newvalue)) {
                    try {
                        prototype = (Player) Class.forName(newvalue).getDeclaredConstructor().newInstance();
                    } catch (Exception e) {
                        System.err.println("Cannot construct player: " + newvalue);
                        e.printStackTrace();
                        System.exit(1);
                    }
                    player = prototype;
                }
            }
            if (player==null && !qualifiedPlayerClass.startsWith("experiments.AlphaZero")) {
                String newvalue = "experiments.AlphaZero." + qualifiedPlayerClass;
                if(isClass(newvalue)) {
                    try {
                        prototype = (Player) Class.forName(newvalue).getDeclaredConstructor().newInstance();
                    } catch (Exception e) {
                        System.err.println("Cannot construct player: " + newvalue);
                        e.printStackTrace();
                        System.exit(1);
                    }
                    player = prototype;
                }
            }
        } catch (Exception e) {
            System.err.println("Does the player class have only a zero-argument constructor? It must!");
            e.printStackTrace();
            System.exit(1);
        }
        if (player == null) {
            // We didn't manage to find a class for our player
            throw new IllegalArgumentException(String.format("Could not create a player for class %s.", playerClass));
        }
        // Let the player set all other properties
        for (int i=0;i<properties.size();i++) {
            player.setProperty(properties.get(i), values.get(i));
        }
        return player;
    }

}