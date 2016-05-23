package edu.boisestate.cs.automaton;

import dk.brics.automaton.Automaton;

public class BoundedAutomatonModel
        extends UnboundedAutomatonModel {

    BoundedAutomatonModel(Automaton model) {
        super(model);
    }

    @Override
    public AutomatonModel clone() {
        return null;
    }
}
