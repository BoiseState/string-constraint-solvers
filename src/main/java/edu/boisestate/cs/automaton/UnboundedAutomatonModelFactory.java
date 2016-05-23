package edu.boisestate.cs.automaton;

import edu.boisestate.cs.Alphabet;

public class UnboundedAutomatonModelFactory
        extends AutomatonModelFactory {

    private UnboundedAutomatonModelFactory(Alphabet alphabet) {
        this.alphabet = alphabet;
    }

    static void setInstance(Alphabet alphabet) {
        instance = new UnboundedAutomatonModelFactory(alphabet);
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
    public AutomatonModel createEmptyString() {
        return null;
    }
}
