package edu.boisestate.cs.solvers;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import edu.boisestate.cs.automaton.AutomatonOperations;

public class BoundedEJSASolver
        extends UnboundedEJSASolver {

    public BoundedEJSASolver() {
    }

    public BoundedEJSASolver(int initialBound) {
        super(initialBound);
    }

    @Override
    public void newSymbolicString(int id) {
        Automaton automaton = BasicAutomata.makeAnyString();
        automaton = AutomatonOperations.boundAutomaton(automaton,
                                                       this.initialBound);
        this.symbolicStringMap.put(id, automaton);
    }
}
