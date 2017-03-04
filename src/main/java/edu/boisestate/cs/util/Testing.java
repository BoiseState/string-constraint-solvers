package edu.boisestate.cs.util;

import edu.boisestate.cs.Alphabet;

import java.util.*;

public class Testing {

    public static void main(String[] args) {

        Alphabet alphabet = new Alphabet("A-D,a-d");

        List<String> empty = new ArrayList<>();
        List<String> emptyString = new ArrayList<>();
        emptyString.add("");
        List<String> concreteStringLower = new ArrayList<>();
        concreteStringLower.add("abc");
        List<String> concreteStringUpper = new ArrayList<>();
        concreteStringUpper.add("ABC");

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
        stringList.put("Concrete Lower", concreteStringLower);
        stringList.put("Concrete Upper", concreteStringUpper);
        stringList.put("Uniform", uniformStrings);
        stringList.put("Non-Uniform", nonUniformStrings);

        // perform operation
        Map<Object[], List<String>> resultMap = new HashMap<>();
        for (String baseKey : stringList.keySet()) {
            List<String> baseStrings = stringList.get(baseKey);
            Object[] resultKey = new Object[]{baseKey};
            List<String> resultStrings = resultMap.get(resultKey);
            if (resultStrings == null) {
                resultStrings = new ArrayList<>();
                resultMap.put(resultKey, resultStrings);
            }
            // perform binary operation
            for (String baseString : baseStrings) {
                String result = baseString.toUpperCase();
                resultStrings.add(result);
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
