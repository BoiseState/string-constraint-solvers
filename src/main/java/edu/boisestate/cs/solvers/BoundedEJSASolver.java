package edu.boisestate.cs.solvers;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;

public class BoundedEJSASolver
        extends UnboundedEJSASolver {

    public BoundedEJSASolver() {
    }

    public BoundedEJSASolver(int initialBound) {
        super(initialBound);
    }

    @Override
    public void newSymbolicString(int id) {

        // create new appropriate bound automaton
        Automaton automaton =
                BasicAutomata.makeAnyChar().repeat(0, this.initialBound);

        // store new automaton
        this.symbolicStringMap.put(id, automaton);
    }
}
