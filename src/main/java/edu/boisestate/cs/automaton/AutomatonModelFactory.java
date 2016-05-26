package edu.boisestate.cs.automaton;

import edu.boisestate.cs.Alphabet;

public abstract class AutomatonModelFactory {

    static protected AutomatonModelFactory instance = null;
    protected Alphabet alphabet;

    public Alphabet getAlphabet() {
        return this.alphabet;
    }

    static public AutomatonModelFactory getInstance(Alphabet alphabet,
                                                    int modelVersion,
                                                    int initialBoundLength) {

        if (modelVersion == 1) {
            UnboundedAutomatonModelFactory.setInstance(alphabet,
                                                       initialBoundLength);
        } else if (modelVersion == 2) {
            BoundedAutomatonModelFactory.setInstance(alphabet,
                                                     initialBoundLength);
        } else if (modelVersion == 3) {
            AggregateAutomatonModelFactory.setInstance(alphabet,
                                                       initialBoundLength);
        }

        return instance;
    }

    public abstract AutomatonModel createString(String string);

    public abstract AutomatonModel createAnyString(int boundingLength);

    public abstract AutomatonModel createAnyString();

    public abstract AutomatonModel createEmptyString();
}
