package edu.boisestate.cs.automaton;

public class WeightedAutomatonModelFactory
        extends AutomatonModelFactory {

    private WeightedAutomatonModelFactory() {
    }

    static void setInstance() {
        instance = new WeightedAutomatonModelFactory();
    }

    @Override
    public AutomatonModel createString(String string) {
        return null;
    }

    @Override
    public AutomatonModel createAnyString(int boundingLength) {
        return null;
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
