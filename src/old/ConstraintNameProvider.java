package old;

import analysis.PrintConstraint;
import org.jgrapht.ext.VertexNameProvider;

public class ConstraintNameProvider implements
                                    VertexNameProvider<PrintConstraint> {

    @Override
    public String getVertexName(PrintConstraint constraint) {
        String value = constraint.getValue();
        value = value.replace("\"", "\\\"");
        return String.format("%s!-!%d", value, constraint.getId());
    }
}
