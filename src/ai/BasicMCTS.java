package ai;

import ai.backprop.Backpropagator;
import ai.backprop.DefaultBackpropagator;
import ai.evaluation.BoardEvaluator;
import ai.evaluation.CutoffRolloutEvaluator;
import ai.evaluation.RolloutEvaluator;
import ai.movechoice.FinalMoveChooser;
import ai.movechoice.MaxSamples;
import ai.movechoice.NormalizedCounts;
import ai.movepruning.ProgressiveWidening;
import ai.nodes.*;
import ai.noveltymcts.*;
import ai.policies.*;
import ai.selection.*;
import ai.timing.MillisecondLimitPerSearchTimer;
import ai.timing.SearchTimer;
import ai.timing.SimulationLimitPerSearchTimer;
import games.*;
import experiments.AlphaZero.*;
import experiments.GameFactory;
import experiments.PolicyTarget;
import utils.UnknownPropertyException;
import utils.Util;

import java.util.List;
import java.util.Random;

import static utils.Util.isClass;

public class BasicMCTS implements MCTS {

    private Backpropagator backpropPolicy;
    private Board board;
    private BoardEvaluator boardEvaluator;
    private SearchNode currentNode;
    private MCTSDLManager dLManager = null;
    private FinalMoveChooser finalMoveChooser;
    private Game game;
    private Move greedyMoveOfLastSearch;
    private ValueEstimate greedyValueEstimateOfLastSearch;
    private boolean keepingTreeBetweenMoves;
    private SearchNodeBuilder nodeBuilder;
    private int[] nodesAtDepth;
    private MCTSNoveltyManager noveltyManager = null;
    private int numberOfNodesInTree;
    //if randomized=false, this seed is used for the RNG
    private int pseudoRandomSeed = 3;
    //this is the only RNG used by every component of MCTS - i.e. if it is set to a specific seed, the behavior of MCTS is entirely deterministic
    private Random random;
    private int randomSeed;
    //if false (and if the timing scheme is based on simulations), every run of the algorithm will produce the same game. only for debugging
    private boolean randomized;
    private int rootColorOfCurrentSearch;
    private SearchTimer searchTimer;
    private SearchTrigger searchTrigger;
    private SelectionPolicy selectionPolicy;
    private Board simulationBoard;
    private SimulationLog simulationLog;
    private int simulationsInSearch;
    private long startTimeOfCurrentSearch;
    private TranspositionTable table;
    private int transpositionTableSize;

    public BasicMCTS() {
        randomized = true;
        transpositionTableSize = 500;
        keepingTreeBetweenMoves = false;
        numberOfNodesInTree = 1;
        this.searchTimer = new SimulationLimitPerSearchTimer(1000);
        this.finalMoveChooser = new MaxSamples();
        this.boardEvaluator = new RolloutEvaluator();
        this.selectionPolicy = new UCB1Tuned();
        this.backpropPolicy = new DefaultBackpropagator();
        this.nodeBuilder = new StateActionValueSearchNodeBuilder();
        this.simulationLog = new SimulationLog();
        this.searchTrigger = new SearchForEveryMove();
    }

    public BasicMCTS(Game game) {
        this();
        setGame(game);
    }

    @Override
    public void acceptPlayMove(Move m) {
        board.play(m);
    }

    @Override
    public void afterSearch(Move bestMove) {
        if(dLManager !=null) {
            dLManager.afterSearch(this);
        }
        if(noveltyManager != null) {
            noveltyManager.afterSearch();
        }
        //store the best move without explorative noise, and store its value estimate as well
        greedyMoveOfLastSearch = finalMoveChooser.selectGreedyMove(getRoot(), random);
        greedyValueEstimateOfLastSearch = getRoot().getValueEstimateOf(greedyMoveOfLastSearch);
        searchTimer.updateAfterSearch(this);
        searchTrigger.updateAfterSearch(this, bestMove);
    }

    @Override
    public void afterSimulation() {
    }

    @Override
    public void beforeSearch() {
        startTimeOfCurrentSearch = System.currentTimeMillis();
        SearchNode root = getRoot();
        if (keepingTreeBetweenMoves && root != null) {
            table.markNodesReachableFrom(root);
        }
        table.sweep();
        numberOfNodesInTree = 1;
        simulationsInSearch = 0;
        rootColorOfCurrentSearch = board.getColorToPlay();
        nodesAtDepth = new int[1000];
        nodesAtDepth[0] = 1;
        searchTimer.updateBeforeSearch(this);
        simulationBoard.copyDataFrom(board);
        simulationLog.clear();
        Evaluation evaluation = getBoardEvaluator().evaluate(getGame(), getSimulationBoard(), getRoot(), getSimulationLog(), getRandom());
        if(noveltyManager!=null) {
            evaluation = noveltyManager.addNoveltyToEvaluation(evaluation, this);
        }
        if(dLManager !=null) {
            dLManager.beforeSearch(this);
        }
        if(noveltyManager != null) {
            noveltyManager.beforeSearch(this);
        }
        selectionPolicy.initializeSearch(this);
    }

    @Override
    public void beforeSimulation() {
    }

    @Override
    public Move bestMove() throws NullMoveException {
        if(searchTrigger.searchNow(this)) {
            return search();
        } else {
            return searchTrigger.chooseMoveWithoutSearch(this);
        }
    }

    void checkChildValidity(SearchNode node, SearchNode child, Move move) throws NullMoveException {
        if(child!=null && !node.hasChildForMove(move)) {
            //should never happen
            throw new NullMoveException();
        }
        if (child == null) {
            //should never happen
            throw new NullMoveException();
        }
    }

    @Override
    public void endGame() {
        boardEvaluator.endGame();
    }

    @Override
    public Evaluation evaluate() {
        Evaluation evaluation;
        if (simulationBoard.isTerminalBoard()) {
            simulationLog.add(simulationBoard, currentNode, null);
            evaluation = simulationBoard.evalOfTerminalBoard();
        } else {
            evaluation = boardEvaluator.evaluate(game, simulationBoard, currentNode, simulationLog, random);
        }
        if(noveltyManager!=null) {
            evaluation = noveltyManager.addNoveltyToEvaluation(evaluation, this);
        }
        if(dLManager !=null && evaluation instanceof MovePriorEvaluation) {
            dLManager.handleEvaluation((MovePriorEvaluation) evaluation,this);
        }
        return evaluation;
    }

    public Evaluation evaluateRoot() {
        Board evalBoard = game.newBoard();
        evalBoard.copyDataFrom(board);
        Evaluation evaluation = boardEvaluator.evaluate(game, evalBoard, null, null, random);
        return evaluation;
    }

    @Override
    public Backpropagator getBackpropPolicy() {
        return backpropPolicy;
    }

    @Override
    public void setBackpropPolicy(Backpropagator backpropPolicy) {
        this.backpropPolicy = backpropPolicy;
    }

    public Board getBoard() {
        return board;
    }

    @Override
    public void setBoard(Board board) {
        this.board = board;
    }

    @Override
    public BoardEvaluator getBoardEvaluator() {
        return boardEvaluator;
    }

    @Override
    public void setBoardEvaluator(BoardEvaluator boardEvaluator) {
        this.boardEvaluator = boardEvaluator;
    }

    @Override
    public SearchNode getCurrentNode() {
        return currentNode;
    }

    public void setCurrentNode(SearchNode currentNode) {
        this.currentNode = currentNode;
    }

    @Override
    public FinalMoveChooser getFinalMoveChooser() {
        return finalMoveChooser;
    }

    @Override
    public void setFinalMoveChooser(FinalMoveChooser finalMoveChooser) {
        this.finalMoveChooser = finalMoveChooser;
    }

    public Game getGame() {
        return game;
    }

    @Override
    public void setGame(Game game) {
        this.game = game;
        board = game.newBoard();
        simulationBoard = game.newBoard();
        nodeBuilder.setDefaultInitialResult(1.0/game.getNumberOfColors());
        nodeBuilder.setDefaultInitialSquaredResult(1.0/Math.pow(game.getNumberOfColors(),2));
        if(noveltyManager!=null) {
            noveltyManager.initialize(board.getWidth(), board.getHeight(), game.getNumberOfColors());
        }
    }

    @Override
    public Move getGreedyMoveOfLastSearch() {
        return greedyMoveOfLastSearch;
    }

    @Override
    public ValueEstimate getGreedyValueEstimateOfLastSearch() {
        return greedyValueEstimateOfLastSearch;
    }

    @Override
    public SearchNodeBuilder getNodeBuilder() {
        return nodeBuilder;
    }

    @Override
    public void setNodeBuilder(SearchNodeBuilder nodeBuilder) {
        this.nodeBuilder = nodeBuilder;
    }

    @Override
    public int[] getNodesAtDepth() {
        return nodesAtDepth;
    }

    @Override
    public Random getRandom() {
        return random;
    }

    /** Returns the node at the root of the search tree. */
    @Override
    public SearchNode getRoot() {
        SearchNode root =  table.findOrAllocate(getBoard().getHash(), game.getNumberOfColors(), getBoard().getLegalMoves().size());
        return root;
    }

    @Override
    public int getRootColorOfCurrentSearch() {
        return rootColorOfCurrentSearch;
    }

    @Override
    public SearchTimer getSearchTimer() {
        return searchTimer;
    }

    @Override
    public void setSearchTimer(SearchTimer searchTimer) {
        this.searchTimer = searchTimer;
    }

    @Override
    public SelectionPolicy getSelectionPolicy() {
        return selectionPolicy;
    }

    @Override
    public void setSelectionPolicy(SelectionPolicy selectionPolicy) {
        this.selectionPolicy = selectionPolicy;
    }

    @Override
    public Board getSimulationBoard() {
        return simulationBoard;
    }

    @Override
    public void setSimulationBoard(Board simulationBoard) {
        this.simulationBoard = simulationBoard;
    }

    @Override
    public SimulationLog getSimulationLog() {
        return simulationLog;
    }

    @Override
    public void setSimulationLog(SimulationLog simulationLog) {
        this.simulationLog = simulationLog;
    }

    @Override
    public int getSimulationsInSearch() {
        return simulationsInSearch;
    }

    @Override
    public long getStartTimeOfCurrentSearch() {
        return startTimeOfCurrentSearch;
    }

    @Override
    public TranspositionTable getTable() {
        return table;
    }

    void handleFreshChildNode(SearchNode child, Board simulationBoard, int depth) {
        numberOfNodesInTree++;
        nodesAtDepth[depth]++;
        if(Util.DEBUG) System.out.println("added new node");
        if(Util.DEBUG) System.out.println("leaving tree");
    }

    @Override
    public void initialize() {
        if(table==null) {
            table = new TranspositionTable(transpositionTableSize, nodeBuilder);
        }
        table.sweep();
        initializeRNG();
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

    public boolean lastSearchWasLoggable() {
        if(dLManager!=null) {
            return dLManager.lastSearchWasLoggable();
        } else {
            return true;
        }
    }

    @Override
    public void performSimulation() throws NullMoveException {
        beforeSimulation();
        treeDescent();
        Evaluation evaluation = evaluate();
        backpropPolicy.backpropagate(evaluation, simulationLog);
        afterSimulation();
    }

    //extracts the sample distribution at the root node
    public PolicyTarget policyTarget() {
        SearchNode root = getRoot();
        PolicyTarget policyTarget = new PolicyTarget();
        List<Move> moves =  root.getExpandedMoves();
        double totalSamples = 0;
        for(Move move : moves) {
            totalSamples += root.getValueEstimateOf(move).getSamples();
        }
        for(Move move : moves) {
            policyTarget.setTargetValueOfMove(move, root.getValueEstimateOf(move).getSamples()/totalSamples);
        }
        return policyTarget;
    }

    private Move search() throws NullMoveException {
        beforeSearch();
        do {
            performSimulation();
            simulationsInSearch++;
        } while (searchTimer.shouldKeepRunning(this));
        Move bestMove = finalMoveChooser.selectMove(getRoot(), random);
        afterSearch(bestMove);
        return bestMove;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setProperty(String property, String value) throws UnknownPropertyException {
        if (property.equals("backprop") || property.equals("backpropagator")) {
            Backpropagator prototype = null;
            if (!value.startsWith("ai.backprop.")) {
                String newvalue = "ai.backprop." + value;
                if(isClass(newvalue)) {
                    try {
                        prototype = (Backpropagator) Class.forName(newvalue).getDeclaredConstructor().newInstance();
                    } catch (Exception e) {
                        System.err.println("Cannot construct backpropagator: " + newvalue);
                        e.printStackTrace();
                        System.exit(1);
                    }
                    backpropPolicy = prototype;
                    return;
                }
            }
            if (!value.startsWith("ai.rave.")) {
                String newvalue = "ai.rave." + value;
                if(isClass(newvalue)) {
                    try {
                        prototype = (Backpropagator) Class.forName(newvalue).getDeclaredConstructor().newInstance();
                    } catch (Exception e) {
                        System.err.println("Cannot construct backpropagator: " + newvalue);
                        e.printStackTrace();
                        System.exit(1);
                    }
                    backpropPolicy = prototype;
                    return;
                }
            }
            System.err.println("Cannot find backpropagator: " + value);
            System.exit(1);
        } else if (property.equals("movechoice") || property.equals("movechooser")) {
            FinalMoveChooser prototype = null;
            if (!value.startsWith("ai.movechoice.")) {
                // set default path if it isn't given
                value = "ai.movechoice." + value;
            }
            try {
                prototype = (FinalMoveChooser) Class.forName(value).getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                System.err.println("Cannot construct move chooser: " + value);
                e.printStackTrace();
                System.exit(1);
            }
            finalMoveChooser = prototype;
        } else if (property.equals("evaluation")) {
            if(value.startsWith("networkrollout")) {
                int maxRolloutSteps = Character.getNumericValue(property.charAt(14));
                StateNetworkEvaluator evaluator = ((AlphaZeroGame) getGame()).newStateNetworkEvaluator(value, true, false);
                StateNetworkPolicyOutputPolicy policy = new StateNetworkPolicyOutputPolicy(evaluator);
                setBoardEvaluator(new CutoffRolloutEvaluator(policy, evaluator, maxRolloutSteps));
            } else if(value.contains("qnetwork")) {
                setBoardEvaluator(((AlphaZeroGame) getGame()).newStateNetworkEvaluator(value, true, true));
            } else if(value.contains("network")) {
                setBoardEvaluator(((AlphaZeroGame) getGame()).newStateNetworkEvaluator(value, true, false));
            } else if(value.equals("static")) {
                setBoardEvaluator(((StaticEvaluatorGame)game).newStaticEvaluator());
            } else if(value.equals("movesorterpolicy")) {
                boardEvaluator = new RolloutEvaluator(new MoveSorterPolicy(((MoveSortingGame)game).getMoveSorter()));
            } else if(value.equals("randompolicy") || value.equals("randomrollouts")) {
                boardEvaluator = new RolloutEvaluator();
            } else if(value.equals("decisivemovespolicy")) {
                boardEvaluator = new RolloutEvaluator(new DecisiveMovesPolicy());
            } else {
                BoardEvaluator prototype = null;
                if (!value.startsWith("games.")) {
                    String newvalue = "games." + value;
                    if (isClass(newvalue)) {
                        try {
                            prototype = (BoardEvaluator) Class.forName(newvalue).getDeclaredConstructor().newInstance();
                        } catch (Exception e) {
                            System.err.println("Cannot construct evaluator: " + newvalue);
                            e.printStackTrace();
                            System.exit(1);
                        }
                        setBoardEvaluator(prototype);
                        return;
                    }
                }
                try {
                    prototype = (BoardEvaluator) Class.forName(value).getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    System.err.println("Cannot construct evaluator: " + value);
                    e.printStackTrace();
                    System.exit(1);
                }
                setBoardEvaluator(prototype);
            }
        } else if (property.equals("policyignoringevaluation")) {
            if(value.contains("network")) {
                setBoardEvaluator(((AlphaZeroGame) getGame()).newStateNetworkEvaluator(value, false, false));
            }
        } else if (property.equals("selection") || property.equals("selectionpolicy")) {
            SelectionPolicy prototype = null;
            if (!value.startsWith("ai.selection.")) {
                String newvalue = "ai.selection." + value;
                if (isClass(newvalue)) {
                    try {
                        prototype = (SelectionPolicy) Class.forName(newvalue).getDeclaredConstructor().newInstance();
                    } catch (Exception e1) {
                        try {
                            prototype = (SelectionPolicy) Class.forName(newvalue).getDeclaredConstructor(MoveSorter.class).newInstance(((MoveSortingGame) game).getMoveSorter());
                        } catch (Exception e2) {
                            System.err.println("Cannot construct selection policy: " + newvalue);
                            e1.printStackTrace();
                            e2.printStackTrace();
                            System.exit(1);
                        }
                    }
                    setSelectionPolicy(prototype);
                    return;
                }
            }
            if (!value.startsWith("ai.noveltymcts.")) {
                String newvalue = "ai.noveltymcts." + value;
                if (isClass(newvalue)) {
                    try {
                        prototype = (SelectionPolicy) Class.forName(newvalue).getDeclaredConstructor().newInstance();
                    } catch (Exception e) {
                        System.err.println("Cannot construct selection policy: " + newvalue);
                        e.printStackTrace();
                        System.exit(1);
                    }
                    setSelectionPolicy(prototype);
                    return;
                }
            }
            if (!value.startsWith("experiments.AlphaZero.")) {
                String newvalue = "experiments.AlphaZero." + value;
                if (isClass(newvalue)) {
                    try {
                        prototype = (SelectionPolicy) Class.forName(newvalue).getDeclaredConstructor(AlphaZeroGame.class).newInstance(getGame());
                    } catch (Exception e) {
                        System.err.println("Cannot construct selection policy: " + newvalue);
                        e.printStackTrace();
                        System.exit(1);
                    }
                    setSelectionPolicy(prototype);
                    return;
                }
            }
            System.err.println("Cannot find policy: " + value);
            System.exit(1);
        } else if (property.equals("nodes")) {
            SearchNodeBuilder prototype = null;
            if (!value.endsWith("ValueSearchNodeBuilder")) {
                // set default path if it isn't given
                value = value + "ValueSearchNodeBuilder";
            }
            if (!value.startsWith("ai.nodes.")) {
                String newvalue = "ai.nodes." + value;
                if(isClass(newvalue)) {
                    try {
                        prototype = (SearchNodeBuilder) Class.forName(newvalue).getDeclaredConstructor().newInstance();
                    } catch (Exception e) {
                        System.err.println("Cannot construct node builder: " + newvalue);
                        e.printStackTrace();
                        System.exit(1);
                    }
                    nodeBuilder = prototype;
                    return;
                }
            }
            if (!value.startsWith("ai.rave.")) {
                String newvalue = "ai.rave." + value;
                if(isClass(newvalue)) {
                    try {
                        prototype = (SearchNodeBuilder) Class.forName(newvalue).getDeclaredConstructor().newInstance();
                    } catch (Exception e) {
                        System.err.println("Cannot construct node builder: " + newvalue);
                        e.printStackTrace();
                        System.exit(1);
                    }
                    nodeBuilder = prototype;
                    return;
                }
            }
            System.err.println("Cannot find node builder: " + value);
            System.exit(1);
        } else if (property.equals("positionfile")) { //TODO currently only works if correct game set before positionfile
            board = game.newBoard(value, true);
            simulationBoard = game.newBoard(value, true);
        } else if (property.equals("simulations") || property.equals("playouts") || property.equals("rollouts")) {
            searchTimer = new SimulationLimitPerSearchTimer(Integer.parseInt(value));
        } else if (property.equals("msec")) {
            searchTimer = new MillisecondLimitPerSearchTimer(Integer.parseInt(value));
        } else if (property.equals("sec")) {
            searchTimer = new MillisecondLimitPerSearchTimer(Integer.parseInt(value)*1000);
        } else if(property.equals("dirichletnoise")) {
            dLManager.setUsingDirichletNoise(Boolean.parseBoolean(value));
        } else if(property.equals("shortsearchsimulations")) {
            dLManager.setShortSearchSimulations(Integer.parseInt(value));
        } else if(property.equals("usingpriortemp")) {
            dLManager.setUsingTemperatureForPriors(Boolean.parseBoolean(value));
        } else if(property.equals("priortemp")) {
            dLManager.setPriorTemperature(Double.parseDouble(value));
        } else if(property.equals("maskingillegalmoves")) {
            dLManager.setMaskingIllegalMoves(Boolean.parseBoolean(value));
        } else if(property.equals("shortsearchprobability")) {
            dLManager.setShortSearchProbability(Double.parseDouble(value));
        } else if(property.equals("loggingprobability")) {
            dLManager.setLoggingProbability(Double.parseDouble(value));
        } else if (property.equals("numberofnodes")) {
            transpositionTableSize = Integer.parseInt(value);
        } else if (property.equals("initialnodesamples")) {
            nodeBuilder.setInitialSamples(Integer.parseInt(value));
        } else if (property.equals("initialnoderesult")) {
            nodeBuilder.setDefaultInitialResult(Double.parseDouble(value));
            nodeBuilder.setDefaultInitialSquaredResult(Math.pow(Double.parseDouble(value),2));
        } else if (property.startsWith("initialnoderesult")) {
            int color = Character.getNumericValue(property.charAt(17));
            nodeBuilder.setInitialResultForColor(color, Double.parseDouble(value));
            nodeBuilder.setInitialSquaredResultForColor(color, Math.pow(Double.parseDouble(value),2));
        } else if (property.equals("sampleallmovesonce")) {
            ((ValueMaximizingSelectionPolicy) selectionPolicy).setSamplingAllMovesOnce(Boolean.parseBoolean(value));
        } else if (property.equals("randomseed")) {
            pseudoRandomSeed = Integer.parseInt(value);
        } else if (property.equals("exploration")) {
            if(selectionPolicy instanceof ExplorationExploitationPolicy) {
                ((ExplorationExploitationPolicy) selectionPolicy).setExplorationParameter(Double.parseDouble(value));
            }
            if(getBoardEvaluator() instanceof  RolloutEvaluator && ((RolloutEvaluator)getBoardEvaluator()).getPolicy() instanceof ExplorationExploitationPolicy) {
                ((ExplorationExploitationPolicy) ((RolloutEvaluator) getBoardEvaluator()).getPolicy()).setExplorationParameter(Double.parseDouble(value));
            }
        } else if (property.equals("randomized")) {
            randomized = Boolean.parseBoolean(value);
        } else if (property.equals("game")) {
            Game prototype = GameFactory.createGame(value);
            setGame(prototype);
        } else if (property.equals("keeptree")) {
            keepingTreeBetweenMoves = Boolean.parseBoolean(value);
        } else if(property.equals("softmaxtemperature")) {
            getBoardEvaluator().setSoftmaxTemperature(Double.parseDouble(value));
        } else if(property.equals("softmaxeval")) {
            getBoardEvaluator().setUsingSoftmaxEval(Boolean.parseBoolean(value));
        } else if (property.startsWith("squarevalue")) {
            int x = Character.getNumericValue(property.charAt(11));
            int y = Character.getNumericValue(property.charAt(12));
            int squarevalue = Integer.parseInt(value);
            ((KnightthroughEvaluator)getBoardEvaluator()).setSquareValue(x, y, squarevalue);
        } else if(property.equals("combovalue1")) {
            ((GomokuEvaluator)getBoardEvaluator()).setValueForOnePieceIn5(Integer.parseInt(value));
        } else if(property.equals("combovalue2")) {
            ((GomokuEvaluator)getBoardEvaluator()).setValueForTwoPiecesIn5(Integer.parseInt(value));
        } else if(property.equals("combovalue3")) {
            ((GomokuEvaluator)getBoardEvaluator()).setValueForThreePiecesIn5(Integer.parseInt(value));
        } else if(property.equals("combovalue4")) {
            ((GomokuEvaluator)getBoardEvaluator()).setValueForFourPiecesIn5(Integer.parseInt(value));
        } else if(property.equals("loggrowth")) {
            ((GomokuEvaluator)getBoardEvaluator()).setLogisticGrowthRate(Integer.parseInt(value));
        } else if(property.equals("wideningc")) {
            ((ProgressiveWidening)((ValueMaximizingSelectionPolicy)selectionPolicy).getMovePruner()).setWideningC(Double.parseDouble(value));
        } else if(property.equals("wideningalpha")) {
            ((ProgressiveWidening)((ValueMaximizingSelectionPolicy)selectionPolicy).getMovePruner()).setWideningAlpha(Double.parseDouble(value));
        } else if(property.equals("progressivewidening")) {
            if(value.equals("true")) {
                ((PruningSelectionPolicy) selectionPolicy).setMovePruner(new ProgressiveWidening(((MoveSortingGame) game).getMoveSorter()));
            }
        } else if(property.equals("oppwideningc")) {
            ((ProgressiveWidening)((ValueMaximizingSelectionPolicy)selectionPolicy).getMovePruner()).setOpponentWideningC(Double.parseDouble(value));
        } else if(property.equals("oppwideningalpha")) {
            ((ProgressiveWidening)((ValueMaximizingSelectionPolicy)selectionPolicy).getMovePruner()).setOpponentWideningAlpha(Double.parseDouble(value));
        } else if(property.equals("differentoppwidening")) {
            ((ProgressiveWidening)((ValueMaximizingSelectionPolicy)selectionPolicy).getMovePruner()).setDifferentWideningForOpponents(Boolean.parseBoolean(value));
        } else if(property.equals("wideningonlyopponents")) {
            ((ProgressiveWidening)((ValueMaximizingSelectionPolicy)selectionPolicy).getMovePruner()).setWideningOnlyForOpponents(Boolean.parseBoolean(value));
        } else if(property.equals("opponentssettofixedbranching")) {
            ((ProgressiveWidening)((ValueMaximizingSelectionPolicy)selectionPolicy).getMovePruner()).setOpponentsSetToFixedBranchingFactor(Boolean.parseBoolean(value));
        } else if(property.equals("fixedbranchingforopponents")) {
            ((ProgressiveWidening)((ValueMaximizingSelectionPolicy)selectionPolicy).getMovePruner()).setFixedOpponentBranchingFactor(Integer.parseInt(value));
        } else if (property.equals("randommoveprobability")) {
            finalMoveChooser.setRandomMoveProbability(Double.parseDouble(value));
        } else if (property.equals("normalizedcountstemp")) {
            ((NormalizedCounts)finalMoveChooser).setTemperature(Double.parseDouble(value));
        } else if (property.equals("priornodes")) {
            if(value.equals("true")) {
                dLManager = new MCTSDLManager();
                setNodeBuilder(new DeepLearningSearchNodeBuilder());
            }
        } else if(property.startsWith("color")) {
        } else if (property.equals("noveltymcts")) {
            if(value.equals("true")) {
               noveltyManager = new MCTSNoveltyManager(null);
               setNodeBuilder(new BasicNoveltySearchNodeBuilder());
               setSelectionPolicy(new NoveltyUCB1Tuned());
            }
        } else if (property.equals("noveltypuct")) {
            if(value.equals("true")) {
                noveltyManager = new MCTSNoveltyManager(null);
                dLManager = new MCTSDLManager();
                setNodeBuilder(new DLNoveltySearchNodeBuilder());
                setSelectionPolicy(new NoveltyPUCT((AlphaZeroGame) game));
            }
        } else if (property.equals("noveltyweight")) {
            ((NoveltyUCB1Tuned)getSelectionPolicy()).setNoveltyWeight(Double.parseDouble(value));
        } else if (property.equals("noveltybeta") ||
                property.equals("noveltyfrequencylimit")) {
            if(noveltyManager!=null) {
                noveltyManager.setNoveltyFunctionProperty(property, value);
            } else {
                ((NoveltyFunction)getBoardEvaluator()).setProperty(property, value);
            }
        } else if (property.equals("noveltyfunction")) {
            NoveltyFunction prototype = null;
            if (!value.startsWith("ai.noveltymcts.")) {
                String newvalue = "ai.noveltymcts." + value;
                if(isClass(newvalue)) {
                    try {
                        prototype = (NoveltyFunction) Class.forName(newvalue).getDeclaredConstructor().newInstance();
                    } catch (Exception e) {
                        System.err.println("Cannot construct novelty function: " + newvalue);
                        e.printStackTrace();
                        System.exit(1);
                    }
                }
            }
            if (!value.startsWith("games.")) {
                String newvalue = "games." + value;
                if(isClass(newvalue)) {
                    try {
                        prototype = (NoveltyFunction) Class.forName(newvalue).getDeclaredConstructor().newInstance();
                    } catch (Exception e) {
                        System.err.println("Cannot construct novelty function: " + newvalue);
                        e.printStackTrace();
                        System.exit(1);
                    }
                }
            }
            noveltyManager.setNoveltyFunction(prototype);
            if(prototype instanceof FeatureBasedPseudocountNoveltyEvaluator) {
                setBoardEvaluator((FeatureBasedPseudocountNoveltyEvaluator)prototype);
            }
            noveltyManager.initialize(board.getWidth(), board.getHeight(), game.getNumberOfColors());
        } else {
            throw new UnknownPropertyException(property + " is not a known property for MCTS players.");
        }
    }

    @Override
    public String toString() {
        String result = "";
        result += "MCTS with settings:\r\n";
        result += "game: "+game+"\r\n";
        result += "random seed: "+(randomized ? randomSeed : pseudoRandomSeed)+"\r\n";
        result += "timing scheme: "+ searchTimer +"\r\n";
        result += "size of transposition table: "+transpositionTableSize+"\r\n";
        result += "move chooser: "+ finalMoveChooser +"\r\n";
        result += "board evaluator: "+boardEvaluator+"\r\n";
        result += "selection policy: "+selectionPolicy+"\r\n";
        result += "backprop policy: "+backpropPolicy+"\r\n";
        result += "node builder: "+nodeBuilder+"\r\n";
        result += "simulations in search: "+simulationsInSearch;
        return result;
    }

    @Override
    public void treeDescent() throws NullMoveException {
        currentNode = getRoot();
        currentNode.setColorToPlay(board.getColorToPlay());
        simulationBoard.copyDataFrom(board);
        simulationLog.clear();
        if(Util.DEBUG) System.out.println("----------------------------------------------------------------------------------------------------------------------------");
        if(Util.DEBUG) System.out.println("playout "+ simulationsInSearch +" begins with selection phase, turn "+getBoard().getTurn()+", board:");
        if(Util.DEBUG) System.out.println(simulationBoard);
        int depth = 0;
        while (!simulationBoard.isTerminalBoard()) {
            Move move;
            move = selectionPolicy.selectMove(currentNode, simulationBoard, random, simulationLog);
            simulationLog.add(simulationBoard, currentNode, move);
            simulationBoard.play(move);
            depth++;
            if(Util.DEBUG) {
                System.out.println("took move: "+move+", resulting board:");
                System.out.println(simulationBoard);
                System.out.println("hash: "+simulationBoard.getHash());
            }
            SearchNode child = getTable().findIfPresent(simulationBoard.getHash());
            // a child will only be created on the second pass through the node
            if (!currentNode.hasChildForMove(move)) {
                child = getTable().findOrAllocate(simulationBoard.getHash(), game.getNumberOfColors(), simulationBoard.getLegalMoves().size());
                if (child == null) {
                    if(Util.DEBUG) System.out.println("No nodes left in pool");
                    return; // No nodes left in pool
                }
                currentNode.addChildForMove(child, move);
                child.setColorToPlay(simulationBoard.getColorToPlay());
                if(child.isFresh()) {
                    handleFreshChildNode(child, simulationBoard, depth);
                    currentNode = child;
                    return;
                }
            }
            checkChildValidity(currentNode, child, move);
            currentNode = child;
        }
        if(Util.DEBUG) System.out.println("game ended in tree, leaving tree");
    }

    @Override
    public int treeSize() {
        return numberOfNodesInTree;
    }

    public ValueEstimate valueEstimateOfLastSearch() {
        return greedyValueEstimateOfLastSearch;
    }

}

