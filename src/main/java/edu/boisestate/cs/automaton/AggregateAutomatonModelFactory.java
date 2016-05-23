package edu.boisestate.cs.automaton;

import edu.boisestate.cs.Alphabet;

public class AggregateAutomatonModelFactory
        extends AutomatonModelFactory {

    private AggregateAutomatonModelFactory(Alphabet alphabet) {
        this.alphabet = alphabet;
    }

    static void setInstance(Alphabet alphabet) {
        instance = new AggregateAutomatonModelFactory(alphabet);
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
    public AutomatonModel createEmptyString() {
        return null;
    }
}
