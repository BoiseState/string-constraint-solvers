package edu.boisestate.cs.automaton;

public abstract class AutomatonModelFactory {

    static protected AutomatonModelFactory instance = null;

    static public AutomatonModelFactory getInstance(int modelVersion) {

        if (modelVersion == 1) {
            UnboundedAutomatonModelFactory.setInstance();
        } else if (modelVersion == 2) {
            BoundedAutomatonModelFactory.setInstance();
        } else if (modelVersion == 3) {
            AggregateAutomatonModelFactory.setInstance();
        } else if (modelVersion == 4) {
            WeightedAutomatonModelFactory.setInstance();
        }

        return instance;
    }

    public abstract AutomatonModel createString(String string);

    public abstract AutomatonModel createAnyString(int boundingLength);

    public abstract AutomatonModel createAnyString();

    public abstract AutomatonModel createEmpty();
}
