package edu.boisestate.cs.util;

public class Triple<TElement1, TElement2, TElement3> {

    private final TElement1 element1;
    private final TElement2 element2;
    private final TElement3 element3;

    public TElement1 get1() {
        return element1;
    }

    public TElement2 get2() {
        return element2;
    }

    public TElement3 get3() {
        return element3;
    }

    public Triple(TElement1 element1, TElement2 element2, TElement3 element3) {
        this.element1 = element1;
        this.element2 = element2;
        this.element3 = element3;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Triple<?, ?, ?> triple = (Triple<?, ?, ?>) o;

        if (element1 != null ?
            !element1.equals(triple.element1) :
            triple.element1 != null) {
            return false;
        }
        if (element2 != null ?
            !element2.equals(triple.element2) :
            triple.element2 != null) {
            return false;
        }
        return element3 != null ?
               element3.equals(triple.element3) :
               triple.element3 == null;
    }

    @Override
    public int hashCode() {
        int result = element1 != null ? element1.hashCode() : 0;
        result = 31 * result + (element2 != null ? element2.hashCode() : 0);
        result = 31 * result + (element3 != null ? element3.hashCode() : 0);
        return result;
    }
}
