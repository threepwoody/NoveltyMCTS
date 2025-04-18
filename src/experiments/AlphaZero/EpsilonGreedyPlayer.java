package experiments.AlphaZero;

import ai.*;
import utils.UnknownPropertyException;

import java.util.List;
import java.util.Random;

import static utils.Util.isClass;
import static experiments.GameFactory.createGame;

public class EpsilonGreedyPlayer implements Player {

    private double epsilon;
    private Game game;
    private Player greedyPlayer;
    //if randomized=false, this seed is used for the RNG
    private int pseudoRandomSeed;
    private Random random;
    private int randomSeed;
    //if false, every run of the algorithm will produce the same game. only for debugging
    private boolean randomized;

    public EpsilonGreedyPlayer() {
        pseudoRandomSeed = 3;
        randomized = true;
        epsilon = 0;
    }

    public EpsilonGreedyPlayer(Game game) {
        this();
        setGame(game);
    }

    @Override
    public void acceptPlayMove(Move m) {
        greedyPlayer.acceptPlayMove(m);
    }

    @Override
    public Move bestMove() throws NullMoveException {
        if(random.nextDouble()<epsilon) {
            return randomMove();
        } else {
            return greedyPlayer.bestMove();
        }
    }

    @Override
    public Board getBoard() {
        return greedyPlayer.getBoard();
    }

    @Override
    public Game getGame() {
        return game;
    }

    @Override
    public void setGame(Game game) {
        this.game = game;
    }

    @Override
    public void initialize() {
        initializeRNG();
        initializeGreedyPlayer();
    }

    private void initializeGreedyPlayer() {
        greedyPlayer.setGame(game);
        greedyPlayer.initialize();
    }

    private void initializeRNG() {
        if(random==null) {
            if (randomized) {
                randomSeed = new Random().nextInt();
                random = new Random(randomSeed);
            } else {
                random = new Random(pseudoRandomSeed);
            }
        } else {
            if (!randomized) {
                random.setSeed(pseudoRandomSeed);
            }
        }
    }

    private Move randomMove() {
        List<Move> legalMoves = getBoard().getLegalMoves();
        int randomIndex = random.nextInt(legalMoves.size());
        Move randomMove = legalMoves.get(randomIndex);
        return randomMove;
    }

    private void setGreedyPlayer(Player prototype) {
        greedyPlayer = prototype;
    }

    @Override
    public void setProperty(String property, String value) throws UnknownPropertyException {
        if(property.equals("epsilon")) {
            epsilon = Double.parseDouble(value);
        } else if (property.equals("randomized")) {
            randomized = Boolean.parseBoolean(value);
        } else if (property.equals("randomseed")) {
            pseudoRandomSeed = Integer.parseInt(value);
        } else if (property.equals("game")) {
            Game prototype = createGame(value);
            setGame(prototype);
        } else if (property.equals("greedyplayer")) {
            Player prototype = null;
            if (!value.startsWith("experiments.AlphaZero.")) {
                String newvalue = "experiments.AlphaZero." + value;
                if(isClass(newvalue)) {
                    try {
                        prototype = (Player) Class.forName(newvalue).getDeclaredConstructor().newInstance();
                    } catch (Exception e1) {
                        System.err.println("Cannot construct greedy player: " + newvalue);
                        e1.printStackTrace();
                        System.exit(1);
                    }
                    setGreedyPlayer(prototype);
                    return;
                }
            }
            if (!value.startsWith("experiments.DeepMonteCarlo.")) {
                String newvalue = "experiments.DeepMonteCarlo." + value;
                if(isClass(newvalue)) {
                    try {
                        prototype = (Player) Class.forName(newvalue).getDeclaredConstructor().newInstance();
                    } catch (Exception e1) {
                        System.err.println("Cannot construct greedy player: " + newvalue);
                        e1.printStackTrace();
                        System.exit(1);
                    }
                    setGreedyPlayer(prototype);
                    return;
                }
            }
            System.err.println("Cannot find policy: " + value);
            System.exit(1);
        } else if(greedyPlayer!=null) {
            greedyPlayer.setProperty(property, value);
        } else if(property.startsWith("color")) {
        } else {
            throw new UnknownPropertyException(property + " is not a known property for EpsilonGreedy players.");
        }
    }
}
