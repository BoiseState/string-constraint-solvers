package edu.boisestate.cs.util;

import edu.boisestate.cs.graph.PrintConstraint;
import org.jgrapht.ext.VertexNameProvider;

public class ConstraintNameProvider implements
                                    VertexNameProvider<PrintConstraint> {

    @Override
    public String getVertexName(PrintConstraint constraint) {
        String value = constraint.getValue();
        value = value.replace("\\\"", "\"").replace("\"", "\\\"");
//        value = value.replaceAll("[^\\\\]\\\\\"", "\\\"");
        return String.format("%s!-!%d", value, constraint.getId());
    }
}
