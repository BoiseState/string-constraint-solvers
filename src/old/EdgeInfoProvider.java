package old;

import org.jgrapht.ext.EdgeNameProvider;
import stringSymbolic.SymbolicEdge;

public class EdgeInfoProvider implements EdgeNameProvider<SymbolicEdge> {

    @Override
    public String getEdgeName(SymbolicEdge symbolicEdge) {
        return symbolicEdge.getType();
    }
}
