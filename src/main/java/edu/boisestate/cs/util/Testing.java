package edu.boisestate.cs.util;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.string.stringoperations.Trim;
import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automaton.*;
import edu.boisestate.cs.automatonModel.operations.PreciseDelete;
import edu.boisestate.cs.automatonModel.operations.PreciseTrim;
import edu.boisestate.cs.automatonModel.operations.StringModelCounter;
import edu.boisestate.cs.automatonModel.operations.weighted
        .WeightedPreciseDelete;

import java.math.BigInteger;
import java.util.*;

public class Testing {

    public static void main(String[] args) {
        automatonTesting();
//        stringTesting();
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

    private static void automatonTesting() {

        int boundingLength = 3;
        Automaton anyChar = BasicAutomata.makeCharSet(" ABCD");
        Automaton uniform = anyChar.repeat(0,boundingLength);
        Automaton x = anyChar.repeat().concatenate(BasicAutomata.makeChar('A')) .concatenate(anyChar.repeat());
        Automaton nonUniform = uniform.intersection(x);

        Automaton[] automata = new Automaton[boundingLength+1];
        for (int i = 0; i < automata.length; i++) {
            automata[i] = nonUniform.intersection(anyChar.repeat(i,i));
            automata[i].minimize();
            DotToGraph.outputDotFileAndPng(automata[i].toDot(), "before" + i);
        }

        PreciseTrim operation = new PreciseTrim();
        for (int i = 0; i < automata.length; i ++) {
            Automaton result = operation.op(automata[i]);
            result.minimize();
            DotToGraph.outputDotFileAndPng(result.toDot(), "after" + i);
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