package edu.boisestate.cs.automaton;

public class AggregateAutomatonModelFactory
        extends AutomatonModelFactory {

    private AggregateAutomatonModelFactory() {
    }

    static void setInstance() {
        instance = new AggregateAutomatonModelFactory();
    }

    @Override
    public AutomatonModel createString(String string) {
        return new AggregateAutomataModel(string);
    }

    @Override
    public AutomatonModel createAnyString(int boundingLength) {
        return new AggregateAutomataModel(boundingLength);
    }

    @Override
    public AutomatonModel createAnyString() {
        return null;
    }

    @Override
    public AutomatonModel createEmpty() {
        return null;
    }
}
