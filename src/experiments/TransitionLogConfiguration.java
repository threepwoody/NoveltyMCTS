package experiments;

import java.util.ArrayList;
import java.util.List;

import static experiments.LogFrequency.NONE;

public class TransitionLogConfiguration {

    private boolean isLoggingWinningSquare = false;
    private LogFrequency logFrequency = NONE;
    private boolean loggingEvalsWithTransitions = false;
    private boolean loggingMatchResultsWithTransitions = false;
    private boolean loggingPolicyTargetsWithTransitions = false;
    private boolean loggingStateActionInputs = false;
    private boolean loggingValueEstimatesWithTransitions = false;
    private String outputFileName = "data.csv";
    private List<String> playersToLog = new ArrayList<>();
    private List<Integer> timeOffsetsOfFutureActionsToLog = new ArrayList<>();
    private boolean usingSingleOutputFile = false;

    public void addPlayerToLog(String player) {
        playersToLog.add(player);
    }

    public void addTimeOffsetOfFutureActionToLog(int timeOffset) {
        timeOffsetsOfFutureActionsToLog.add(timeOffset);
    }

    public LogFrequency getLogFrequency() {
        return logFrequency;
    }

    public void setLogFrequency(LogFrequency logFrequency) {
        this.logFrequency = logFrequency;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    public void setOutputFileName(String name) {
        outputFileName = name;
    }

    public List<Integer> getTimeOffsetsOfFutureActionsToLog() {
        return timeOffsetsOfFutureActionsToLog;
    }

    public boolean isLoggingAnything() {
        return logFrequency != NONE;
    }

    public boolean isLoggingEvalsWithTransitions() {
        return loggingEvalsWithTransitions;
    }

    public void setLoggingEvalsWithTransitions(boolean loggingEvalsWithTransitions) {
        this.loggingEvalsWithTransitions = loggingEvalsWithTransitions;
    }

    public boolean isLoggingMatchResultsWithTransitions() {
        return loggingMatchResultsWithTransitions;
    }

    public void setLoggingMatchResultsWithTransitions(boolean loggingMatchResultsWithTransitions) {
        this.loggingMatchResultsWithTransitions = loggingMatchResultsWithTransitions;
    }

    public boolean isLoggingPolicyTargetsWithTransitions() {
        return loggingPolicyTargetsWithTransitions;
    }

    public void setLoggingPolicyTargetsWithTransitions(boolean loggingPolicyTargetsWithTransitions) {
        this.loggingPolicyTargetsWithTransitions = loggingPolicyTargetsWithTransitions;
    }

    public boolean isLoggingStateActionInputs() {
        return loggingStateActionInputs;
    }

    public void setLoggingStateActionInputs(boolean loggingStateActionInputs) {
        this.loggingStateActionInputs = loggingStateActionInputs;
    }

    public boolean isLoggingValueEstimatesWithTransitions() {
        return loggingValueEstimatesWithTransitions;
    }

    public void setLoggingValueEstimatesWithTransitions(boolean loggingValueEstimatesWithTransitions) {
        this.loggingValueEstimatesWithTransitions = loggingValueEstimatesWithTransitions;
    }

    public boolean isLoggingWinningSquare() {
        return isLoggingWinningSquare;
    }

    public void setLoggingWinningSquare(boolean loggingWinningSquare) {
        isLoggingWinningSquare = loggingWinningSquare;
    }

    public boolean isPlayerToLog(String player) {
        return playersToLog.contains(player);
    }

    public boolean isUsingSingleOutputFile() {
        return usingSingleOutputFile;
    }

    public void setUsingSingleOutputFile(boolean usingSingleOutputFile) {
        this.usingSingleOutputFile = usingSingleOutputFile;
    }

}
