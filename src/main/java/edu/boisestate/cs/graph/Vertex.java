package edu.boisestate.cs.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 */
public class Vertex {

    private final String actualValue;
    private final List<IncomingEdge> incomingEdges;
    private final int num;
    private final List<Integer> sourceConstraints;
    private final long timeStamp;
    private final int type;
    private final String value;
    private int id;

    public String getActualValue() {
        return actualValue;
    }

    public int getId() {
        return id;
    }

    public List<IncomingEdge> getIncomingEdges() {
        return incomingEdges;
    }

    public int getNum() {
        return num;
    }

    public List<Integer> getSourceConstraints() {
        return sourceConstraints;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public int getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public Vertex(int id,
                  String actualValue,
                  int num,
                  long timeStamp,
                  int type,
                  String value,
                  Collection<Integer> sourceConstraints) {

        this.id = id;
        this.actualValue = actualValue;
        this.num = num;
        this.timeStamp = timeStamp;
        this.type = type;
        this.value = value;

        this.sourceConstraints = new ArrayList<>(sourceConstraints);
        this.incomingEdges = new ArrayList<>();
    }
}
