# NoveltyMCTS

This is the code corresponding to the paper:\
**Hendrik Baier, Michael Kaisers: _Novelty in Monte Carlo Tree Search_. IEEE Transactions on Games. Accepted.**

**Gradle** is recommended for build automation.\
The file `build.gradle.kts` is currently set to run `experiments.Experiment`.\
The settings for this experiment are in `experiments.ExperimentConfiguration`; they are currently set to replicating the results of Table I-IV in the paper, using 5000 MCTS simulations per move.\
The file `SLURM script.txt` gives an example for how to run an experiment such as this on a computer cluster using the **Slurm Workload Manager**, in particular the Dutch national supercomputer **Snellius**, where the paper's experiments were run.
