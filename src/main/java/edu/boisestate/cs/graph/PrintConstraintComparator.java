package edu.boisestate.cs.graph;

import java.util.Comparator;

public class PrintConstraintComparator implements Comparator<PrintConstraint> {
    @Override
    public int compare(PrintConstraint o1, PrintConstraint o2) {
        return Integer.compare(o1.getId(), o2.getId());
    }
}
