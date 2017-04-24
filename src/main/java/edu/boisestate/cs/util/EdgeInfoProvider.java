package edu.boisestate.cs.util;

import org.jgrapht.ext.EdgeNameProvider;
import edu.boisestate.cs.graph.SymbolicEdge;

public class EdgeInfoProvider implements EdgeNameProvider<SymbolicEdge> {

    @Override
    public String getEdgeName(SymbolicEdge symbolicEdge) {
        return symbolicEdge.getType();
    }
}
