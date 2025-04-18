package experiments;
/** Defines some system-dependent constants for experiments. */
public class ExperimentConfiguration {

    public static final String EXPERIMENT_NAME = "Tables_1-4_5000_simulations";

    public static final String RESULTS_DIRECTORY = 	System.getProperty("os.name").contains("Windows") ? "C://results//"+EXPERIMENT_NAME+"//" :
            "/home/baierh/results/"+EXPERIMENT_NAME+"/";

    /**
     * Number of games to run in parallel.
     */
    public static final int MATCHES_IN_PARALLEL = System.getProperty("os.name").contains("Windows") ? 1 : 180;

    //this many games are going to be played for each experimental condition or, if using bandit tuning, for each bandit/line specified below.
    public static final int MATCHES_PER_CONDITION = 2000;

    public static boolean logMatches = false;

    //helpful when comparing (near-)deterministic players
    public static int numberOfRandomOpeningTurnsPerColor = 1;

    /** Command line arguments for the various conditions in the experiment. The game has to be specified first.
     * Every line is either one player or one template for a set of players, using comma-separated lists of parameter values.
     * All value combinations resulting from these templates will be tested separately.
     * If using bandit tuning, each line specifies one bandit, with all value combinations in it being the available arms. */
    public static final String[] PLAYER_ONE_TEMPLATES = {

            "player=BasicMCTS game=Breakthrough noveltymcts=true simulations=5000 numberofnodes=5100 noveltyfunction=RawStatePseudocountNovelty evaluation=static noveltybeta=0.002 noveltyweight=0.02 exploration=0.2",
            "player=BasicMCTS game=Othello noveltymcts=true simulations=5000 numberofnodes=5100 noveltyfunction=RawStatePseudocountNovelty evaluation=static noveltybeta=0.0003 noveltyweight=0.01 exploration=0.06",
            "player=BasicMCTS game=Connect4-7 noveltymcts=true simulations=5000 numberofnodes=5100 noveltyfunction=RawStatePseudocountNovelty evaluation=static noveltybeta=0.0001 noveltyweight=0.3 exploration=0.6",
            "player=BasicMCTS game=Knightthrough noveltymcts=true simulations=5000 numberofnodes=5100 noveltyfunction=RawStatePseudocountNovelty evaluation=static noveltybeta=0.02 noveltyweight=2 exploration=0.3",
            "player=BasicMCTS game=Gomoku3-9 noveltymcts=true simulations=5000 numberofnodes=5100 noveltyfunction=RawStatePseudocountNovelty evaluation=static noveltybeta=0.006 noveltyweight=0.0001 exploration=0.06",

            "player=BasicMCTS game=Breakthrough noveltymcts=true simulations=5000 numberofnodes=5100 noveltyfunction=BreakthroughFeatureBasedPseudocountNoveltyEvaluator evaluation=static noveltybeta=0.003 noveltyweight=0.1 exploration=0.2",
            "player=BasicMCTS game=Othello noveltymcts=true simulations=5000 numberofnodes=5100 noveltyfunction=OthelloFeatureBasedPseudocountNoveltyEvaluator evaluation=static noveltybeta=0.0003 noveltyweight=0.0002 exploration=0.03",
            "player=BasicMCTS game=Connect4-7 noveltymcts=true simulations=5000 numberofnodes=5100 noveltyfunction=Connect4FeatureBasedPseudocountNoveltyEvaluator evaluation=static noveltybeta=0.006 noveltyweight=0.2 exploration=0.6",
            "player=BasicMCTS game=Knightthrough noveltymcts=true simulations=5000 numberofnodes=5100 noveltyfunction=KnightthroughFeatureBasedPseudocountNoveltyEvaluator evaluation=static noveltybeta=0.001 noveltyweight=2 exploration=0.3",
            "player=BasicMCTS game=Gomoku3-9 noveltymcts=true simulations=5000 numberofnodes=5100 noveltyfunction=GomokuFeatureBasedPseudocountNoveltyEvaluator evaluation=static noveltybeta=0.06 noveltyweight=0.0002 exploration=0.06",

            "player=BasicMCTS game=Breakthrough noveltymcts=true simulations=5000 numberofnodes=5100 noveltyfunction=RawStateEvaluationNovelty evaluation=static noveltybeta=0.06 noveltyweight=0.06 exploration=0.2",
            "player=BasicMCTS game=Othello noveltymcts=true simulations=5000 numberofnodes=5100 noveltyfunction=RawStateEvaluationNovelty evaluation=static noveltybeta=0.0001 noveltyweight=0.0003 exploration=0.03",
            "player=BasicMCTS game=Connect4-7 noveltymcts=true simulations=5000 numberofnodes=5100 noveltyfunction=RawStateEvaluationNovelty evaluation=static noveltybeta=0.6 noveltyweight=0.02 exploration=0.6",
            "player=BasicMCTS game=Knightthrough noveltymcts=true simulations=5000 numberofnodes=5100 noveltyfunction=RawStateEvaluationNovelty evaluation=static noveltybeta=0.002 noveltyweight=2 exploration=0.3",
            "player=BasicMCTS game=Gomoku3-9 noveltymcts=true simulations=5000 numberofnodes=5100 noveltyfunction=RawStateEvaluationNovelty evaluation=static noveltybeta=0.06 noveltyweight=0.0001 exploration=0.06",

            "player=BasicMCTS game=Breakthrough noveltymcts=true simulations=5000 numberofnodes=5100 noveltyfunction=RawStateThresholdNovelty evaluation=static noveltybeta=0.06 noveltyfrequencylimit=0.0006 noveltyweight=0.06 exploration=0.2",
            "player=BasicMCTS game=Othello noveltymcts=true simulations=5000 numberofnodes=5100 noveltyfunction=RawStateThresholdNovelty evaluation=static noveltybeta=0.0003 noveltyfrequencylimit=0.02 noveltyweight=0.01 exploration=0.06",
            "player=BasicMCTS game=Connect4-7 noveltymcts=true simulations=5000 numberofnodes=5100 noveltyfunction=RawStateThresholdNovelty evaluation=static noveltybeta=0.03 noveltyfrequencylimit=0.03 noveltyweight=0.001 exploration=0.6",
            "player=BasicMCTS game=Knightthrough noveltymcts=true simulations=5000 numberofnodes=5100 noveltyfunction=RawStateThresholdNovelty evaluation=static noveltybeta=0.03 noveltyfrequencylimit=0.002 noveltyweight=2 exploration=0.3",
            "player=BasicMCTS game=Gomoku3-9 noveltymcts=true simulations=5000 numberofnodes=5100 noveltyfunction=RawStateThresholdNovelty evaluation=static noveltybeta=0.003 noveltyfrequencylimit=0.00003 noveltyweight=0.006 exploration=0.1",

};

    /** Command line arguments for the various opponent conditions in the experiment.
     * Every line is either one player or one template for a set of players, using comma-separated lists of parameter values.
     * Every line (template) here corresponds to one line (template) in PLAYER_ONE_TEMPLATES. The Cartesian product of all players 1 and players 2 of corresponding templates will be playing.*/
    public static final String[][] PLAYER_TWO_TEMPLATES = {


            {"player=BasicMCTS game=Breakthrough simulations=5000 numberofnodes=5100 evaluation=static exploration=0.2"},
            {"player=BasicMCTS game=Othello simulations=5000 numberofnodes=5100 evaluation=static exploration=0.06"},
            {"player=BasicMCTS game=Connect4-7 simulations=5000 numberofnodes=5100 evaluation=static exploration=0.3"},
            {"player=BasicMCTS game=Knightthrough simulations=5000 numberofnodes=5100 evaluation=static exploration=0.1"},
            {"player=BasicMCTS game=Gomoku3-9 simulations=5000 numberofnodes=5100 evaluation=static exploration=0.06"},

            {"player=BasicMCTS game=Breakthrough simulations=5000 numberofnodes=5100 evaluation=static exploration=0.2"},
            {"player=BasicMCTS game=Othello simulations=5000 numberofnodes=5100 evaluation=static exploration=0.06"},
            {"player=BasicMCTS game=Connect4-7 simulations=5000 numberofnodes=5100 evaluation=static exploration=0.3"},
            {"player=BasicMCTS game=Knightthrough simulations=5000 numberofnodes=5100 evaluation=static exploration=0.1"},
            {"player=BasicMCTS game=Gomoku3-9 simulations=5000 numberofnodes=5100 evaluation=static exploration=0.06"},

            {"player=BasicMCTS game=Breakthrough simulations=5000 numberofnodes=5100 evaluation=static exploration=0.2"},
            {"player=BasicMCTS game=Othello simulations=5000 numberofnodes=5100 evaluation=static exploration=0.06"},
            {"player=BasicMCTS game=Connect4-7 simulations=5000 numberofnodes=5100 evaluation=static exploration=0.3"},
            {"player=BasicMCTS game=Knightthrough simulations=5000 numberofnodes=5100 evaluation=static exploration=0.1"},
            {"player=BasicMCTS game=Gomoku3-9 simulations=5000 numberofnodes=5100 evaluation=static exploration=0.06"},

            {"player=BasicMCTS game=Breakthrough simulations=5000 numberofnodes=5100 evaluation=static exploration=0.2"},
            {"player=BasicMCTS game=Othello simulations=5000 numberofnodes=5100 evaluation=static exploration=0.06"},
            {"player=BasicMCTS game=Connect4-7 simulations=5000 numberofnodes=5100 evaluation=static exploration=0.3"},
            {"player=BasicMCTS game=Knightthrough simulations=5000 numberofnodes=5100 evaluation=static exploration=0.1"},
            {"player=BasicMCTS game=Gomoku3-9 simulations=5000 numberofnodes=5100 evaluation=static exploration=0.06"},


    };

}
