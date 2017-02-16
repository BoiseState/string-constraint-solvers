/**
 * An extended EJSA operation for a more precise substring operation.
 */
package edu.boisestate.cs.automatonModel.operations;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.string.charset.CharSet;
import dk.brics.string.stringoperations.UnaryOperation;

public class PreciseSubstring
        extends UnaryOperation {
    private int end;
    private int start;

    public PreciseSubstring(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public CharSet charsetTransfer(CharSet arg0) {
        return arg0;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PreciseSubstring;
    }

    @Override
    public int getPriority() {
        return 4;
    }

    @Override
    public Automaton op(Automaton automaton) {
        //eas per documentation:
        //StringIndexOutOfBoundsException - if start is negative, greater
        // than length(), or greater than end.

        // if start is greater than end or automaton is empty
        if (start < 0 || end < 0 || start > end || automaton.isEmpty()) {
            // return empty automaton (exception)
            return BasicAutomata.makeEmpty();
        }

        PrecisePrefix prefix = new PrecisePrefix(end);
        Automaton prefixAutomaton = prefix.op(automaton);

        PreciseSuffix suffix = new PreciseSuffix(start);
        return suffix.op(prefixAutomaton);
    }

    @Override
    public String toString() {
        return "precise substring";
    }

}
