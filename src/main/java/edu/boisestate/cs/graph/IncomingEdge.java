package edu.boisestate.cs.graph;

/**
 *
 */
public class IncomingEdge {

    private int source;
    private String type;

    public int getSource() {
        return source;
    }

    public String getType() {
        return type;
    }

    public IncomingEdge(int source, String type) {
        this.source = source;
        this.type = type;
    }
}
