package edu.boisestate.cs.automaton;

public class UnboundedAutomatonModelFactory
        extends AutomatonModelFactory {

    private UnboundedAutomatonModelFactory() {
    }

    static void setInstance() {
        instance = new UnboundedAutomatonModelFactory();
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
