package edu.boisestate.cs.old;

import org.jgrapht.ext.EdgeNameProvider;
import edu.boisestate.cs.analysis.SymbolicEdge;

public class EdgeInfoProvider implements EdgeNameProvider<SymbolicEdge> {

    @Override
    public String getEdgeName(SymbolicEdge symbolicEdge) {
        return symbolicEdge.getType();
    }
}
