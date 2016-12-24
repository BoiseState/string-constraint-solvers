package edu.boisestate.cs.automatonModel;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.string.stringoperations.UnaryOperation;
import edu.boisestate.cs.Alphabet;

import java.math.BigInteger;
import java.util.Set;

public abstract class AutomatonModelManager {

    static protected AutomatonModelManager instance = null;
    protected Alphabet alphabet;

    public Alphabet getAlphabet() {
        return this.alphabet;
    }

    static public AutomatonModelManager getInstance(Alphabet alphabet,
                                                    int modelVersion,
                                                    int initialBoundLength) {

        if (modelVersion == 1) {
            UnboundedAutomatonModelManager.setInstance(alphabet,
                                                       initialBoundLength);
        } else if (modelVersion == 2) {
            BoundedAutomatonModelManager.setInstance(alphabet,
                                                     initialBoundLength);
        } else if (modelVersion == 3) {
            AggregateAutomatonModelManager.setInstance(alphabet,
                                                       initialBoundLength);
        }

        return instance;
    }

    public abstract AutomatonModel createString(String string);

    public abstract AutomatonModel createAnyString(int initialBound);

    public abstract AutomatonModel createAnyString();

    public abstract AutomatonModel createEmptyString();

    public abstract AutomatonModel createAnyString(int min, int max);

    public abstract AutomatonModel createEmpty();
}
