package edu.boisestate.cs.solvers;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;

public class BoundedMCJSASolver
        extends UnboundedMCJSASolver {

    public BoundedMCJSASolver() {
    }

    public BoundedMCJSASolver(int bound) {
        super(bound);
    }

    @Override
    public void newSymbolicString(int id) {

        // create new automaton
        Automaton automaton =
                BasicAutomata.makeAnyChar().repeat(0, this.initialBound);

        // store new automaton
        this.symbolicStringMap.put(id, automaton);

        // set new bound
        this.boundMap.put(id, this.initialBound);
    }
}
