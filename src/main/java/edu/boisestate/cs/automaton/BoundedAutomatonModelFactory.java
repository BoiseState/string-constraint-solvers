package edu.boisestate.cs.automaton;

public class BoundedAutomatonModelFactory
        extends AutomatonModelFactory {

    private BoundedAutomatonModelFactory() {
    }

    static void setInstance() {
        instance = new BoundedAutomatonModelFactory();
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
