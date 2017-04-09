package edu.boisestate.cs.util;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.automaton.SpecialOperations;
import dk.brics.string.stringoperations.Substring;
import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automaton.WeightedAutomaton;
import edu.boisestate.cs.automaton.WeightedState;
import edu.boisestate.cs.automaton.WeightedTransition;
import edu.boisestate.cs.automatonModel.operations.PreciseTrim;

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
        nonUniform.minimize();

        DotToGraph.outputDotFileAndPng(nonUniform.toDot(), "nonUniform");

        Automaton overlap = SpecialOperations.overlap(nonUniform, nonUniform);
        overlap.minimize();
        DotToGraph.outputDotFileAndPng(overlap.toDot(), "overlap");
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