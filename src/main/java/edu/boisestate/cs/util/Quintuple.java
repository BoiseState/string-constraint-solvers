package edu.boisestate.cs.util;

public class Quintuple<TElement1, TElement2, TElement3, TElement4, TElement5> {

    private final TElement1 element1;
    private final TElement2 element2;
    private final TElement3 element3;
    private final TElement4 element4;
    private final TElement5 element5;

    public TElement1 get1() {
        return element1;
    }

    public TElement2 get2() {
        return element2;
    }

    public TElement3 get3() {
        return element3;
    }

    public TElement4 get4() {
        return element4;
    }

    public TElement5 get5() {
        return element5;
    }

    public Quintuple(TElement1 element1,
                     TElement2 element2,
                     TElement3 element3,
                     TElement4 element4,
                     TElement5 element5) {
        this.element1 = element1;
        this.element2 = element2;
        this.element3 = element3;
        this.element4 = element4;
        this.element5 = element5;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Quintuple<?, ?, ?, ?, ?> quintuple = (Quintuple<?, ?, ?, ?, ?>) o;

        if (element1 != null ?
            !element1.equals(quintuple.element1) :
            quintuple.element1 != null) {
            return false;
        }
        if (element2 != null ?
            !element2.equals(quintuple.element2) :
            quintuple.element2 != null) {
            return false;
        }
        if (element3 != null ?
            !element3.equals(quintuple.element3) :
            quintuple.element3 != null) {
            return false;
        }
        return element4 != null ?
               element4.equals(quintuple.element4) :
               quintuple.element4 == null;
    }

    @Override
    public int hashCode() {
        int result = element1 != null ? element1.hashCode() : 0;
        result = 31 * result + (element2 != null ? element2.hashCode() : 0);
        result = 31 * result + (element3 != null ? element3.hashCode() : 0);
        result = 31 * result + (element4 != null ? element4.hashCode() : 0);
        result = 31 * result + (element5 != null ? element5.hashCode() : 0);
        return result;
    }
}
