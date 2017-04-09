package edu.boisestate.cs.util;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automaton.WeightedAutomaton;
import edu.boisestate.cs.automaton.WeightedState;
import edu.boisestate.cs.automaton.WeightedTransition;
import edu.boisestate.cs.automatonModel.operations.*;

import java.util.*;

public class Testing {

    public static void main(String[] args) {
//        automatonTesting();
        automatonOperationTesting();
//        stringTesting();

//        String s1 = "a";
//        String s2 = "a";
//        System.out.printf("%b\n", s1.contains(s2));
    }

    private static void automatonTesting() {

        Alphabet alphabet = new Alphabet("A-D");

        // crete states
        WeightedState q0 = new WeightedState();
        WeightedState q1 = new WeightedState();
        WeightedState q2 = new WeightedState();
        WeightedState q3 = new WeightedState();

        // set accept state
        q0.setAccept(true);
        q1.setAccept(true);
        q2.setAccept(true);
        q3.setAccept(true);

        // add transitions
        q0.addTransition(new WeightedTransition('A', q1, 1));
        q0.addTransition(new WeightedTransition('A', 'B', q1, 1));
        q0.addTransition(new WeightedTransition('A', 'C', q1, 1));
        q0.addTransition(new WeightedTransition('A', 'D', q1, 1));
        q0.addTransition(new WeightedTransition('B', q1, 1));
        q0.addTransition(new WeightedTransition('C', q1, 1));
        q0.addTransition(new WeightedTransition('D', q1, 1));
        q1.addTransition(new WeightedTransition('A', 'D', q2, 1));
        q1.addTransition(new WeightedTransition('C', q2, 1));
        q2.addTransition(new WeightedTransition('A', 'D', q3, 1));
        q2.addTransition(new WeightedTransition('A', q3, 1));
        q2.addTransition(new WeightedTransition('D', q3, 1));

        // create automaton
        WeightedAutomaton automaton = new WeightedAutomaton();
        automaton.setInitialState(q0);

        for (WeightedTransition t : q0.getSortedTransitions(true)) {
            if (t.getMin() == t.getMax()) {
                System.out.printf("('%c', %d) -> %d\n",
                                  t.getMin(),
                                  t.getWeight(),
                                  t.getDest().getNumber());
            } else {
                System.out.printf("('%c'-'%c', %d) -> %d\n",
                                  t.getMin(),
                                  t.getMax(),
                                  t.getWeight(),
                                  t.getDest().getNumber());
            }
        }

        DotToGraph.outputDotFileAndPng(automaton.toDot(), "before");

        automaton.reduce();

        DotToGraph.outputDotFileAndPng(automaton.toDot(), "after");
    }

    private static void stringTesting() {

        Alphabet alphabet = new Alphabet("A-D");

        List<String> empty = new ArrayList<>();
        List<String> emptyString = new ArrayList<>();
        emptyString.add("");
        List<String> concrete = new ArrayList<>();
        concrete.add("ABC");
        List<String> concreteStringLower = new ArrayList<>();
        concreteStringLower.add("abc");
        List<String> concreteStringUpper = new ArrayList<>();
        concreteStringUpper.add("ABC");
        List<String> concreteStringNonWhitespace = new ArrayList<>();
        concreteStringNonWhitespace.add("ABC");
        List<String> concreteStringWhitespace = new ArrayList<>();
        concreteStringWhitespace.add(" B ");

        List<String> uniformStrings = alphabet.allStrings(0, 3);

        List<String> nonUniformStrings = new ArrayList<>();
        for (String str : uniformStrings) {
            if (str.contains("A")) {
                nonUniformStrings.add(str);
            }
        }

        Map<String, List<String>> stringList = new HashMap<>();
        stringList.put("Empty", empty);
        stringList.put("Empty String", emptyString);
        stringList.put("Concrete", concrete);
//        stringList.put("Concrete Lower", concreteStringLower);
//        stringList.put("Concrete Upper", concreteStringUpper);
//        stringList.put("Concrete Whitespace", concreteStringWhitespace);
//        stringList.put("Concrete Non-Whitespace", concreteStringNonWhitespace);
        stringList.put("Uniform", uniformStrings);
        stringList.put("Non-Uniform", nonUniformStrings);

        // perform operation
        Map<Object[], List<String>> resultMap = new HashMap<>();
        for (String baseKey : stringList.keySet()) {
            for (String argKey : stringList.keySet()) {
                List<String> baseStrings = stringList.get(baseKey);
                List<String> argStrings = stringList.get(argKey);
                Object[] resultKey = new Object[]{baseKey, argKey};
                List<String> resultStrings = resultMap.get(resultKey);
                if (resultStrings == null) {
                    resultStrings = new ArrayList<>();
                    resultMap.put(resultKey, resultStrings);
                }
                // perform binary operation
                for (String baseString : baseStrings) {
                    boolean flag = false;
                    for (String argString : argStrings) {
                        String result = baseString + argString;
                        resultStrings.add(result);
                    }
                }
            }
        }


        // output expected result
        List<Object[]> keys = new ArrayList<>(resultMap.keySet());
        Collections.sort(keys, new sortByStringName());
        for (Object[] resultKey : keys) {
            List<String> resultStrings = resultMap.get(resultKey);
            for (int i = 0; i < resultKey.length; i++) {
                if (resultKey[i] instanceof String) {
                    System.out.print("\"" + resultKey[i] + "\"");
                } else if (resultKey[i] instanceof Character) {
                    System.out.print("\'" + resultKey[i] + "\'");
                } else {
                    System.out.print(resultKey[i]);
                }
                System.out.print(", ");
            }
            System.out.print(resultStrings.size() + "\n");
        }
    }

    private static void automatonOperationTesting() {

        int boundingLength = 3;
        Automaton anyChar = BasicAutomata.makeCharSet("ABCD");
        Automaton uniform = anyChar.repeat(0,boundingLength);
        Automaton x = anyChar.repeat().concatenate(BasicAutomata.makeChar('A')).concatenate(anyChar.repeat());
        Automaton nonUniform = uniform.intersection(x);
        uniform.minimize();
        nonUniform.minimize();

        Automaton[] automata_u = new Automaton[boundingLength + 1];
        for (int i = 0; i < automata_u.length; i ++) {
            Automaton bounding = anyChar.repeat(i,i);
            automata_u[i] = uniform.intersection(bounding);
            automata_u[i].minimize();
            DotToGraph.outputDotFileAndPng(automata_u[i].toDot(), "uniform-" + i);
        }

        Automaton[] automata_n = new Automaton[boundingLength + 1];
        for (int i = 0; i < automata_n.length; i ++) {
            Automaton bounding = anyChar.repeat(i,i);
            automata_n[i] = nonUniform.intersection(bounding);
            automata_n[i].minimize();
            DotToGraph.outputDotFileAndPng(automata_n[i].toDot(), "nonUniform-" + i);
        }

        // Concat
        Automaton[] concat_u_u = new Automaton[automata_u.length];
        Automaton[] concat_u_n = new Automaton[automata_u.length];
        Automaton[] concat_n_u = new Automaton[automata_u.length];
        Automaton[] concat_n_n = new Automaton[automata_u.length];
        for (int i = 0; i < concat_u_u.length; i ++) {
            concat_u_u[i] = automata_u[i].concatenate(uniform);
            concat_u_u[i].minimize();
            DotToGraph.outputDotFileAndPng(concat_u_u[i].toDot(), "concat-uniform-" + i + "-uniform");

            concat_u_n[i] = automata_u[i].concatenate(nonUniform);
            concat_u_n[i].minimize();
            DotToGraph.outputDotFileAndPng(concat_u_n[i].toDot(), "concat-uniform-" + i + "-nonUniform");

            concat_n_u[i] = automata_n[i].concatenate(uniform);
            concat_n_u[i].minimize();
            DotToGraph.outputDotFileAndPng(concat_n_u[i].toDot(), "concat-nonUniform-" + i + "-uniform");

            concat_n_n[i] = automata_n[i].concatenate(nonUniform);
            concat_n_n[i].minimize();
            DotToGraph.outputDotFileAndPng(concat_n_n[i].toDot(), "concat-nonUniform-" + i + "-nonUniform");
        }

        // Delete
        Map<Tuple<Integer, Integer>, Automaton[]> delete_u = new HashMap<>();
        Map<Tuple<Integer, Integer>, Automaton[]> delete_n = new HashMap<>();
        for (int i = 0; i <= 3; i++) {
            for (int j = i; j <= 3; j++) {
                delete_u.put(new Tuple<>(i, j), new Automaton[automata_u.length]);
                delete_n.put(new Tuple<>(i, j), new Automaton[automata_u.length]);
            }
        }
        for (Tuple<Integer, Integer> tuple : delete_u.keySet()) {
            Automaton[] a_u = delete_u.get(tuple);
            Automaton[] a_n = delete_n.get(tuple);
            PreciseDelete delete = new PreciseDelete(tuple.get1(), tuple.get2());
            for (int i = 0; i < a_u.length; i++) {
                a_u[i] = delete.op(automata_u[i]);
                a_u[i].minimize();
                DotToGraph.outputDotFileAndPng(a_u[i].toDot(), "delete-uniform-" + i + "-" + tuple .get1() + "-" + tuple.get2());

                a_n[i] = delete.op(automata_n[i]);
                a_n[i].minimize();
                DotToGraph.outputDotFileAndPng(a_n[i].toDot(), "delete-nonUniform-" + i + "-" + tuple .get1() + "-" + tuple.get2());
            }
        }

        // Insert
        Map<Integer,Automaton[]> insert_u_u = new TreeMap<>();
        Map<Integer,Automaton[]> insert_u_n = new TreeMap<>();
        Map<Integer,Automaton[]> insert_n_u = new TreeMap<>();
        Map<Integer,Automaton[]> insert_n_n = new TreeMap<>();
        for (int i = 0; i <= 3; i++) {
                insert_u_u.put(i, new Automaton[automata_u.length]);
                insert_u_n.put(i, new Automaton[automata_u.length]);
                insert_n_u.put(i, new Automaton[automata_n.length]);
                insert_n_n.put(i, new Automaton[automata_n.length]);
        }
        for (int offset : insert_n_n.keySet()) {
            Automaton[] a_u_u = insert_u_u.get(offset);
            Automaton[] a_u_n = insert_u_n.get(offset);
            Automaton[] a_n_u = insert_n_u.get(offset);
            Automaton[] a_n_n = insert_n_n.get(offset);
            PreciseInsert insert = new PreciseInsert(offset);
            for (int i = 0; i < a_u_u.length; i++) {
                a_u_u[i] = insert.op(automata_u[i], uniform);
                a_u_u[i].minimize();
                DotToGraph.outputDotFileAndPng(a_u_u[i].toDot(), "insert-uniform-" + i + "-" + offset + "-uniform");

                a_u_n[i] = insert.op(automata_u[i], nonUniform);
                a_u_n[i].minimize();
                DotToGraph.outputDotFileAndPng(a_u_n[i].toDot(), "insert-uniform-" + i + "-" + offset + "-nonUniform");

                a_n_u[i] = insert.op(automata_n[i], uniform);
                a_n_u[i].minimize();
                DotToGraph.outputDotFileAndPng(a_n_u[i].toDot(), "insert-nonUniform-" + i + "-" + offset + "-uniform");

                a_n_n[i] = insert.op(automata_n[i], nonUniform);
                a_n_n[i].minimize();
                DotToGraph.outputDotFileAndPng(a_n_n[i].toDot(), "insert-nonUniform-" + i + "-" + offset + "-nonUniform");
            }
        }

        // Substring
        Map<Tuple<Integer, Integer>, Automaton[]> substring_u = new HashMap<>();
        Map<Tuple<Integer, Integer>, Automaton[]> substring_n = new HashMap<>();
        for (int i = 0; i <= 3; i++) {
            for (int j = i; j <= 3; j++) {
                substring_u.put(new Tuple<>(i, j), new Automaton[automata_u.length]);
                substring_n.put(new Tuple<>(i, j), new Automaton[automata_u.length]);
            }
        }
        for (Tuple<Integer, Integer> tuple : substring_u.keySet()) {
            Automaton[] a_u = substring_u.get(tuple);
            Automaton[] a_n = substring_n.get(tuple);
            PreciseSubstring substring = new PreciseSubstring(tuple.get1(), tuple.get2());
            for (int i = 0; i < a_u.length; i++) {
                a_u[i] = substring.op(automata_u[i]);
                a_u[i].minimize();
                DotToGraph.outputDotFileAndPng(a_u[i].toDot(), "substring-uniform-" + i + "-" + tuple .get1() + "-" + tuple.get2());

                a_n[i] = substring.op(automata_n[i]);
                a_n[i].minimize();
                DotToGraph.outputDotFileAndPng(a_n[i].toDot(), "substring-nonUniform-" + i + "-" + tuple .get1() + "-" + tuple.get2());
            }
        }

        // SetLength
        Map<Integer, Automaton[]> setLength_u = new TreeMap<>();
        Map<Integer, Automaton[]> setLength_n = new TreeMap<>();
        for (int i = 0; i <= 3; i++) {
            setLength_u.put(i, new Automaton[automata_u.length]);
            setLength_n.put(i, new Automaton[automata_u.length]);
        }
        for (int length : setLength_u.keySet()) {
            Automaton[] a_u = setLength_u.get(length);
            Automaton[] a_n = setLength_n.get(length);
            PreciseSetLength setLength = new PreciseSetLength(length);
            for (int i = 0; i < a_u.length; i++) {
                a_u[i] = setLength.op(automata_u[i]);
                a_u[i].minimize();
                DotToGraph.outputDotFileAndPng(a_u[i].toDot(), "setLength-uniform-" + i + "-" + length);

                a_n[i] = setLength.op(automata_n[i]);
                a_n[i].minimize();
                DotToGraph.outputDotFileAndPng(a_n[i].toDot(), "setLength-nonUniform-" + i + "-" + length);
            }
        }

        // Trim
        Automaton[] trim_u = new Automaton[automata_u.length];
        Automaton[] trim_n = new Automaton[automata_u.length];
        PreciseTrim trim = new PreciseTrim();
        for (int i = 0; i < trim_u.length; i ++) {
            trim_u[i] = trim.op(automata_u[i]);
            trim_u[i].minimize();
            DotToGraph.outputDotFileAndPng(trim_u[i].toDot(), "trim-uniform-" + i);

            trim_n[i] = trim.op(automata_n[i]);
            trim_n[i].minimize();
            DotToGraph.outputDotFileAndPng(trim_n[i].toDot(), "trim-nonUniform-" + i);
        }
    }

    private static class sortByStringName
            implements Comparator<Object[]> {

        @Override
        public int compare(Object[] o1, Object[] o2) {
            if (o1.length < o2.length) {
                return -1;
            } else if (o1.length > o2.length) {
                return 1;
            }

            for (int i = 0; i < o1.length; i++) {
                if (o1[i] instanceof String && o2[i] instanceof String) {
                    String s1 = (String) o1[i];
                    String s2 = (String) o2[i];
                    if (!s1.equals(s2)) {
                        if (s1.equals("Empty")) {
                            return -1;
                        } else if (s1.equals("Empty String")) {
                            if (s2.equals("Empty")) {
                                return 1;
                            } else {
                                return -1;
                            }
                        } else if (s1.equals("Concrete")) {
                            if (s2.contains("Empty")) {
                                return 1;
                            } else {
                                return -1;
                            }
                        } else if (s1.equals("Concrete Whitespace")) {
                            if (s2.contains("Uniform")) {
                                return -1;
                            } else {
                                return 1;
                            }
                        } else if (s1.equals("Concrete Lower")) {
                            if (s2.contains("Empty")) {
                                return 1;
                            } else {
                                return -1;
                            }
                        } else if (s1.equals("Concrete Upper")) {
                            if (s2.contains("Uniform")) {
                                return -1;
                            } else {
                                return 1;
                            }
                        } else if (s1.equals("Uniform")) {
                            if (s2.equals("Non-Uniform")) {
                                return -1;
                            } else {
                                return 1;
                            }
                        } else {
                            return 1;
                        }
                    }
                } else if (o1[i] instanceof Character &&
                           o2[i] instanceof Character) {
                    char s1 = (Character) o1[i];
                    char s2 = (Character) o2[i];
                    if (s1 != s2) {
                        return s1 - s2;
                    }
                } else if (o1[i] instanceof Number &&
                           o2[i] instanceof Number) {
                    int s1 = (Integer) o1[i];
                    int s2 = (Integer) o2[i];
                    if (s1 != s2) {
                        return s1 - s2;
                    }
                }
            }

            return 0;
        }
    }
}