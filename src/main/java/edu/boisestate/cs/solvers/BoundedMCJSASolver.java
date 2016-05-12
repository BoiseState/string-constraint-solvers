package edu.boisestate.cs.solvers;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import edu.boisestate.cs.automaton.AutomatonOperations;

public class BoundedMCJSASolver extends UnboundedMCJSASolver {

    public BoundedMCJSASolver() {
    }

    public BoundedMCJSASolver(int bound) {
        super(bound);
    }

    @Override
    public void newSymbolicString(int id) {
        Automaton automaton = BasicAutomata.makeAnyString();
        automaton = AutomatonOperations.boundAutomaton(automaton,
                                                       this.initialBound);
        this.symbolicStringMap.put(id, automaton);

        // set new bound
        this.boundMap.put(id, this.initialBound);
    }
}
