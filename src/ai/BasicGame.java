package ai;

import ai.terminalboardevaluation.BasicTerminalBoardEvaluator;
import ai.terminalboardevaluation.PiececountTerminalBoardEvaluator;
import ai.terminalboardevaluation.WrappingTerminalBoardEvaluator;
import games.StaticEvaluatorGame;
import utils.UnknownPropertyException;

import java.util.ArrayList;
import java.util.List;

public abstract class BasicGame implements Game {

    private final int numberOfColors;
    private List<String> properties;
    private TerminalBoardEvaluator terminalBoardEvaluator;

    public BasicGame(int numberOfColors) {
        this.numberOfColors = numberOfColors;
        this.terminalBoardEvaluator = new BasicTerminalBoardEvaluator(numberOfColors);
        properties = new ArrayList<>();
    }

    @Override
    public void addProperty(String property) {
        properties.add(property);
    }

    @Override
    public String getName() {
        return getNameWithoutProperties()+printProperties();
    };

    abstract public String getNameWithoutProperties();

    @Override
    public int getNumberOfColors() {
        return numberOfColors;
    }

    @Override
    public TerminalBoardEvaluator newTerminalBoardEvaluator() {
        return terminalBoardEvaluator.copy();
    }

    public String printProperties() {
        String result = "";
        for(String property : properties) {
            result += "-"+property;
        }
        return result;
    }

    @Override
    public void setProperty(String property, String value) throws UnknownPropertyException {
        if(property.equals("p")) {
            PiececountTerminalBoardEvaluator newEvaluator = new PiececountTerminalBoardEvaluator(numberOfColors);
            setTerminalBoardEvaluator(newEvaluator);
            addProperty("p");
        } else if(property.equals("w")) {
            WrappingTerminalBoardEvaluator newEvaluator = new WrappingTerminalBoardEvaluator(numberOfColors, ((StaticEvaluatorGame)this).newStaticEvaluator());
            setTerminalBoardEvaluator(newEvaluator);
            addProperty("w");
        } else {
            throw new UnknownPropertyException(property + " is not a known property for games.");
        }
    }

    @Override
    public void setTerminalBoardEvaluator(TerminalBoardEvaluator evaluator) {
        this.terminalBoardEvaluator = evaluator;
    }

}
