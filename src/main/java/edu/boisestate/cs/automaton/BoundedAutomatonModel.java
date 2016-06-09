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

        // create new model from existing automata
        Automaton cloneAutomaton = this.automaton.clone();
        return new BoundedAutomatonModel(cloneAutomaton,
                                           this.alphabet,
                                           this.boundLength);
    }
}
