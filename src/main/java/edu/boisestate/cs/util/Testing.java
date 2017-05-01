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
//        aggregateAutomatonOperationTesting();
        stringTesting();
    }

    private static void automatonTesting() {

        Alphabet alphabet = new Alphabet("A-D");
        int boundingLength = 3;
        Automaton anyChar = BasicAutomata.makeCharSet(alphabet.getCharSet());
        anyChar.minimize();

        Automaton uniform = anyChar.repeat(0,boundingLength);
        uniform.minimize();

        Automaton x1 = BasicAutomata.makeChar('A').concatenate(anyChar.repeat());
        Automaton x2 = anyChar.repeat().concatenate(BasicAutomata.makeChar('A'));

        Automaton startsWith = uniform.intersection(x1);
        startsWith.minimize();
        DotToGraph.outputDotFileAndPng(startsWith.toDot(), "starts-with");

        Automaton endsWith = uniform.intersection(x2);
        endsWith.minimize();
        DotToGraph.outputDotFileAndPng(endsWith.toDot(), "ends-with");


    }

    private static void stringTesting() {

        Alphabet alphabet = new Alphabet("A-E");

        List<String> even = alphabet.allStrings(0, 4);

        List<String> unevenA = new ArrayList<>();
        List<String> unevenB = new ArrayList<>();
        List<String> unevenC = new ArrayList<>();
        List<String> unevenD = new ArrayList<>();
        List<String> unevenE = new ArrayList<>();
        for (String str : even) {
            if (str.contains("A")) {
                unevenA.add(str);
            }
            if (str.contains("B")) {
                unevenB.add(str);
            }
            if (str.contains("C")) {
                unevenC.add(str);
            }
            if (str.contains("D")) {
                unevenD.add(str);
            }
            if (str.contains("E")) {
                unevenE.add(str);
            }
        }

        Lambda2<Tuple<Boolean, Boolean>, String, List<String>> contains = new Lambda2<Tuple<Boolean, Boolean>, String, List<String>>() {
            @Override
            public Tuple<Boolean, Boolean> execute(String str, List<String> strings) {
                boolean tFlag = false;
                boolean fFlag = false;
                for (String substr : strings) {
                    if (str.contains(substr)) {
                        tFlag = true;
                    } else {
                        fFlag = true;
                    }
                    if (tFlag && fFlag) {
                        break;
                    }
                }
                return new Tuple<>(tFlag, fFlag);
            }
        };

        Lambda2<Tuple<Boolean, Boolean>, String, List<String>> equals = new Lambda2<Tuple<Boolean, Boolean>, String, List<String>>() {
            @Override
            public Tuple<Boolean, Boolean> execute(String str, List<String> strings) {
                boolean tFlag = false;
                boolean fFlag = false;
                for (String substr : strings) {
                    if (str.equals(substr)) {
                        tFlag = true;
                    } else {
                        fFlag = true;
                    }
                    if (tFlag && fFlag) {
                        break;
                    }
                }
                return new Tuple<>(tFlag, fFlag);
            }
        };

        LinkedList<Quintuple<String, List<String>, List<String>, List<String>, Tuple<Lambda2<Tuple<Boolean, Boolean>, String, List<String>>, List<String>>>> args = new LinkedList<>();
        args.add(new Quintuple<>("54-U-E-E-contains(S-T)", unevenB, even, even, new Tuple<>(contains, Collections.singletonList("C"))));
        args.add(new Quintuple<>("54-U-E-E-contains(S-F)", unevenB, even, even, new Tuple<>(contains, Collections.singletonList("ACEADD"))));
        args.add(new Quintuple<>("54-U-E-E-contains(E-T)", unevenB, even, even, new Tuple<>(contains, even)));
        args.add(new Quintuple<>("54-U-E-E-contains(E-F)", unevenB, even, even, new Tuple<>(contains, even)));
        args.add(new Quintuple<>("54-U-E-E-contains(U-T)", unevenB, even, even, new Tuple<>(contains, unevenC)));
        args.add(new Quintuple<>("54-U-E-E-contains(U-F)", unevenB, even, even, new Tuple<>(contains, unevenA)));
        args.add(new Quintuple<>("54-U-E-E-contains(S-T)", unevenB, even, even, new Tuple<>(equals, Collections.singletonList("AACB"))));
        args.add(new Quintuple<>("54-U-E-E-contains(S-F)", unevenB, even, even, new Tuple<>(equals, Collections.singletonList("EA"))));
        args.add(new Quintuple<>("54-U-E-E-contains(E-T)", unevenB, even, even, new Tuple<>(equals, even)));
        args.add(new Quintuple<>("54-U-E-E-contains(E-F)", unevenB, even, even, new Tuple<>(equals, even)));
        args.add(new Quintuple<>("54-U-E-E-contains(U-T)", unevenB, even, even, new Tuple<>(equals, unevenB)));
        args.add(new Quintuple<>("54-U-E-E-contains(U-F)", unevenB, even, even, new Tuple<>(equals, unevenA)));
        args.add(new Quintuple<>("54-U-E-U-contains(S-T)", unevenB, even, unevenB, new Tuple<>(contains, Collections.singletonList("E"))));
        args.add(new Quintuple<>("54-U-E-U-contains(S-F)", unevenB, even, unevenB, new Tuple<>(contains, Collections.singletonList("ACEADD"))));
        args.add(new Quintuple<>("54-U-E-U-contains(E-T)", unevenB, even, unevenB, new Tuple<>(contains, even)));
        args.add(new Quintuple<>("54-U-E-U-contains(E-F)", unevenB, even, unevenB, new Tuple<>(contains, even)));
        args.add(new Quintuple<>("54-U-E-U-contains(U-T)", unevenB, even, unevenB, new Tuple<>(contains, unevenA)));
        args.add(new Quintuple<>("54-U-E-U-contains(U-F)", unevenB, even, unevenB, new Tuple<>(contains, unevenA)));
        args.add(new Quintuple<>("54-U-E-U-contains(S-T)", unevenB, even, unevenB, new Tuple<>(equals, Collections.singletonList("AACBCCBE"))));
        args.add(new Quintuple<>("54-U-E-U-contains(S-F)", unevenB, even, unevenB, new Tuple<>(equals, Collections.singletonList("EA"))));
        args.add(new Quintuple<>("54-U-E-U-contains(E-T)", unevenB, even, unevenB, new Tuple<>(equals, even)));
        args.add(new Quintuple<>("54-U-E-U-contains(E-F)", unevenB, even, unevenB, new Tuple<>(equals, even)));
        args.add(new Quintuple<>("54-U-E-U-contains(U-T)", unevenB, even, unevenB, new Tuple<>(equals, unevenE)));
        args.add(new Quintuple<>("54-U-E-U-contains(U-F)", unevenB, even, unevenB, new Tuple<>(equals, unevenD)));
        args.add(new Quintuple<>("55-U-U-E-contains(S-T)", unevenB, unevenC, even, new Tuple<>(contains, Collections.singletonList("C"))));
        args.add(new Quintuple<>("55-U-U-E-contains(S-F)", unevenB, unevenC, even, new Tuple<>(contains, Collections.singletonList("ACEADD"))));
        args.add(new Quintuple<>("55-U-U-E-contains(E-T)", unevenB, unevenC, even, new Tuple<>(contains, even)));
        args.add(new Quintuple<>("55-U-U-E-contains(E-F)", unevenB, unevenC, even, new Tuple<>(contains, even)));
        args.add(new Quintuple<>("55-U-U-E-contains(U-T)", unevenB, unevenC, even, new Tuple<>(contains, unevenE)));
        args.add(new Quintuple<>("55-U-U-E-contains(U-F)", unevenB, unevenC, even, new Tuple<>(contains, unevenA)));
        args.add(new Quintuple<>("55-U-U-E-contains(S-T)", unevenB, unevenC, even, new Tuple<>(equals, Collections.singletonList("AACBCCBE"))));
        args.add(new Quintuple<>("55-U-U-E-contains(S-F)", unevenB, unevenC, even, new Tuple<>(equals, Collections.singletonList("EA"))));
        args.add(new Quintuple<>("55-U-U-E-contains(E-T)", unevenB, unevenC, even, new Tuple<>(equals, even)));
        args.add(new Quintuple<>("55-U-U-E-contains(E-F)", unevenB, unevenC, even, new Tuple<>(equals, even)));
        args.add(new Quintuple<>("55-U-U-E-contains(U-T)", unevenB, unevenC, even, new Tuple<>(equals, unevenE)));
        args.add(new Quintuple<>("55-U-U-E-contains(U-F)", unevenB, unevenC, even, new Tuple<>(equals, unevenA)));
        args.add(new Quintuple<>("55-U-U-U-contains(S-T)", unevenB, unevenC, unevenC, new Tuple<>(contains, Collections.singletonList("B"))));
        args.add(new Quintuple<>("55-U-U-U-contains(S-F)", unevenB, unevenC, unevenC, new Tuple<>(contains, Collections.singletonList("ACEADD"))));
        args.add(new Quintuple<>("55-U-U-U-contains(E-T)", unevenB, unevenC, unevenC, new Tuple<>(contains, even)));
        args.add(new Quintuple<>("55-U-U-U-contains(E-F)", unevenB, unevenC, unevenC, new Tuple<>(contains, even)));
        args.add(new Quintuple<>("55-U-U-U-contains(U-T)", unevenB, unevenC, unevenC, new Tuple<>(contains, unevenE)));
        args.add(new Quintuple<>("55-U-U-U-contains(U-F)", unevenB, unevenC, unevenC, new Tuple<>(contains, unevenE)));
        args.add(new Quintuple<>("55-U-U-U-contains(S-T)", unevenB, unevenC, unevenC, new Tuple<>(equals, Collections.singletonList("AACBCCBECCBE"))));
        args.add(new Quintuple<>("55-U-U-U-contains(S-F)", unevenB, unevenC, unevenC, new Tuple<>(equals, Collections.singletonList("EA"))));
        args.add(new Quintuple<>("55-U-U-U-contains(E-T)", unevenB, unevenC, unevenC, new Tuple<>(equals, even)));
        args.add(new Quintuple<>("55-U-U-U-contains(E-F)", unevenB, unevenC, unevenC, new Tuple<>(equals, even)));
        args.add(new Quintuple<>("55-U-U-U-contains(U-T)", unevenB, unevenC, unevenC, new Tuple<>(equals, unevenC)));
        args.add(new Quintuple<>("55-U-U-U-contains(U-F)", unevenB, unevenC, unevenC, new Tuple<>(equals, unevenE)));
        args.add(new Quintuple<>("102-E-E-E-contains(S-T)", even, even, even, new Tuple<>(contains, Collections.singletonList("A"))));
        args.add(new Quintuple<>("102-E-E-E-contains(S-F)", even, even, even, new Tuple<>(contains, Collections.singletonList("ACEADD"))));
        args.add(new Quintuple<>("102-E-E-E-contains(E-T)", even, even, even, new Tuple<>(contains, even)));
        args.add(new Quintuple<>("102-E-E-E-contains(E-F)", even, even, even, new Tuple<>(contains, even)));
        args.add(new Quintuple<>("102-E-E-E-contains(U-T)", even, even, even, new Tuple<>(contains, unevenA)));
        args.add(new Quintuple<>("102-E-E-E-contains(U-F)", even, even, even, new Tuple<>(contains, unevenA)));
        args.add(new Quintuple<>("102-E-E-E-contains(S-T)", even, even, even, new Tuple<>(equals, Collections.singletonList("BDBA"))));
        args.add(new Quintuple<>("102-E-E-E-contains(S-F)", even, even, even, new Tuple<>(equals, Collections.singletonList("EA"))));
        args.add(new Quintuple<>("102-E-E-E-contains(E-T)", even, even, even, new Tuple<>(equals, even)));
        args.add(new Quintuple<>("102-E-E-E-contains(E-F)", even, even, even, new Tuple<>(equals, even)));
        args.add(new Quintuple<>("102-E-E-E-contains(U-T)", even, even, even, new Tuple<>(equals, unevenD)));
        args.add(new Quintuple<>("102-E-E-E-contains(U-F)", even, even, even, new Tuple<>(equals, unevenB)));
        args.add(new Quintuple<>("102-E-E-U-contains(S-T)", even, even, unevenB, new Tuple<>(contains, Collections.singletonList("B"))));
        args.add(new Quintuple<>("102-E-E-U-contains(S-F)", even, even, unevenB, new Tuple<>(contains, Collections.singletonList("ACEADD"))));
        args.add(new Quintuple<>("102-E-E-U-contains(E-T)", even, even, unevenB, new Tuple<>(contains, even)));
        args.add(new Quintuple<>("102-E-E-U-contains(E-F)", even, even, unevenB, new Tuple<>(contains, even)));
        args.add(new Quintuple<>("102-E-E-U-contains(U-T)", even, even, unevenB, new Tuple<>(contains, unevenB)));
        args.add(new Quintuple<>("102-E-E-U-contains(U-F)", even, even, unevenB, new Tuple<>(contains, unevenE)));
        args.add(new Quintuple<>("102-E-E-U-contains(S-T)", even, even, unevenB, new Tuple<>(equals, Collections.singletonList("BDBACCBE"))));
        args.add(new Quintuple<>("102-E-E-U-contains(S-F)", even, even, unevenB, new Tuple<>(equals, Collections.singletonList("EA"))));
        args.add(new Quintuple<>("102-E-E-U-contains(E-T)", even, even, unevenB, new Tuple<>(equals, even)));
        args.add(new Quintuple<>("102-E-E-U-contains(E-F)", even, even, unevenB, new Tuple<>(equals, even)));
        args.add(new Quintuple<>("102-E-E-U-contains(U-T)", even, even, unevenB, new Tuple<>(equals, unevenA)));
        args.add(new Quintuple<>("102-E-E-U-contains(U-F)", even, even, unevenB, new Tuple<>(equals, unevenE)));
        args.add(new Quintuple<>("103-E-U-E-contains(S-T)", even, unevenC, even, new Tuple<>(contains, Collections.singletonList("A"))));
        args.add(new Quintuple<>("103-E-U-E-contains(S-F)", even, unevenC, even, new Tuple<>(contains, Collections.singletonList("ACEADD"))));
        args.add(new Quintuple<>("103-E-U-E-contains(E-T)", even, unevenC, even, new Tuple<>(contains, even)));
        args.add(new Quintuple<>("103-E-U-E-contains(E-F)", even, unevenC, even, new Tuple<>(contains, even)));
        args.add(new Quintuple<>("103-E-U-E-contains(U-T)", even, unevenC, even, new Tuple<>(contains, unevenD)));
        args.add(new Quintuple<>("103-E-U-E-contains(U-F)", even, unevenC, even, new Tuple<>(contains, unevenA)));
        args.add(new Quintuple<>("103-E-U-E-contains(S-T)", even, unevenC, even, new Tuple<>(equals, Collections.singletonList("BDBACCBE"))));
        args.add(new Quintuple<>("103-E-U-E-contains(S-F)", even, unevenC, even, new Tuple<>(equals, Collections.singletonList("EA"))));
        args.add(new Quintuple<>("103-E-U-E-contains(E-T)", even, unevenC, even, new Tuple<>(equals, even)));
        args.add(new Quintuple<>("103-E-U-E-contains(E-F)", even, unevenC, even, new Tuple<>(equals, even)));
        args.add(new Quintuple<>("103-E-U-E-contains(U-T)", even, unevenC, even, new Tuple<>(equals, unevenB)));
        args.add(new Quintuple<>("103-E-U-E-contains(U-F)", even, unevenC, even, new Tuple<>(equals, unevenE)));
        args.add(new Quintuple<>("103-E-U-U-contains(S-T)", even, unevenC, unevenE, new Tuple<>(contains, Collections.singletonList("A"))));
        args.add(new Quintuple<>("103-E-U-U-contains(S-F)", even, unevenC, unevenE, new Tuple<>(contains, Collections.singletonList("ACEADD"))));
        args.add(new Quintuple<>("103-E-U-U-contains(E-T)", even, unevenC, unevenE, new Tuple<>(contains, even)));
        args.add(new Quintuple<>("103-E-U-U-contains(E-F)", even, unevenC, unevenE, new Tuple<>(contains, even)));
        args.add(new Quintuple<>("103-E-U-U-contains(U-T)", even, unevenC, unevenE, new Tuple<>(contains, unevenE)));
        args.add(new Quintuple<>("103-E-U-U-contains(U-F)", even, unevenC, unevenE, new Tuple<>(contains, unevenA)));
        args.add(new Quintuple<>("103-E-U-U-contains(S-T)", even, unevenC, unevenE, new Tuple<>(equals, Collections.singletonList("BDBACCBECCBE"))));
        args.add(new Quintuple<>("103-E-U-U-contains(S-F)", even, unevenC, unevenE, new Tuple<>(equals, Collections.singletonList("EA"))));
        args.add(new Quintuple<>("103-E-U-U-contains(E-T)", even, unevenC, unevenE, new Tuple<>(equals, even)));
        args.add(new Quintuple<>("103-E-U-U-contains(E-F)", even, unevenC, unevenE, new Tuple<>(equals, even)));
        args.add(new Quintuple<>("103-E-U-U-contains(U-T)", even, unevenC, unevenE, new Tuple<>(equals, unevenB)));
        args.add(new Quintuple<>("103-E-U-U-contains(U-F)", even, unevenC, unevenE, new Tuple<>(equals, unevenA)));

        while (!args.isEmpty()) {
            Quintuple<String, List<String>, List<String>, List<String>, Tuple<Lambda2<Tuple<Boolean, Boolean>, String, List<String>>, List<String>>> arg = args.removeFirst();
            long inCount = 0;
            long trueCount = 0;
            long falseCount = 0;
            for (String strBase : arg.get2()) {
                for(String strConcat1 : arg.get3()) {
                    String concat1Result = strBase.concat(strConcat1);
                    for (String strConcat2 : arg.get4()) {
                        String concat2Result = concat1Result.concat(strConcat2);
                        inCount += 1;
                        Tuple<Lambda2<Tuple<Boolean, Boolean>, String, List<String>>, List<String>> pred = arg.get5();
                        Tuple<Boolean, Boolean> predResults = pred.get1().execute(concat2Result, pred.get2());
                        if (predResults.get1()) {
                            trueCount += 1;
                        }
                        if (predResults.get2()) {
                            falseCount += 1;
                        }
                    }
                }
            }
            System.out.printf("%s\t%d\t%d\t%d\n",
                              arg.get1(),
                              inCount,
                              trueCount,
                              falseCount);
        }
    }

    private static void aggregateAutomatonOperationTesting() {

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