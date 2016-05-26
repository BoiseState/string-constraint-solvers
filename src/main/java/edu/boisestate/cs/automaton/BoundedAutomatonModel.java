package edu.boisestate.cs.automaton;

import dk.brics.automaton.Automaton;
import edu.boisestate.cs.Alphabet;

public class BoundedAutomatonModel
        extends UnboundedAutomatonModel {

    BoundedAutomatonModel(Automaton model, Alphabet alphabet, int boundLength) {
        super(model, alphabet, boundLength);
    }

    @Override
    public AutomatonModel clone() {
        return super.clone();
    }
}
