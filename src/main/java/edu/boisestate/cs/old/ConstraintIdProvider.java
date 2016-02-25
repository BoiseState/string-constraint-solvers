package edu.boisestate.cs.old;

import edu.boisestate.cs.analysis.PrintConstraint;
import org.jgrapht.ext.VertexNameProvider;

public class ConstraintIdProvider implements VertexNameProvider<PrintConstraint> {

    @Override
    public String getVertexName(PrintConstraint constraint) {
        return String.valueOf(constraint.getId());
    }
}
