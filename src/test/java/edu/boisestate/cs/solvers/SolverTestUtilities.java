package edu.boisestate.cs.solvers;

import edu.boisestate.cs.Alphabet;

import java.util.ArrayList;
import java.util.List;

public class SolverTestUtilities {
    public static ConcreteValues getEmptyValues(Alphabet alphabet) {
        return new ConcreteValues(alphabet, 0);
    }

    public static ConcreteValues getEmptyStringValues(Alphabet alphabet) {
        return new ConcreteValues(alphabet, 0, "");
    }

    public static ConcreteValues getConcreteValues(Alphabet alphabet, String string) {
        return new ConcreteValues(alphabet, string.length(), string);
    }

    public static ConcreteValues getUniformValues(Alphabet alphabet, int initialBoundLength) {
        List<String> strings = alphabet.allStrings(0, initialBoundLength);
        return new ConcreteValues(alphabet, initialBoundLength, strings);
    }

    public static ConcreteValues getNonUniformValues(Alphabet alphabet, int initialBoundLength) {
        List<String> strings = new ArrayList<>();
        String c = String.valueOf(alphabet.getCharSet().charAt(0));
        for (String s : alphabet.allStrings(0, initialBoundLength)) {
            if (s.contains(c)) {
                strings.add(s);
            }
        }
        return new ConcreteValues(alphabet, initialBoundLength, strings);
    }
}
