/*
 * dk.brics.automaton
 * 
 * Copyright (c) 2001-2011 Anders Moeller
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.boisestate.cs.automaton;

import java.util.*;

/**
 * Construction of basic automata.
 */
final public class BasicWeightedAutomata {

    private BasicWeightedAutomata() {
    }

    /**
     * Constructs sub-automaton corresponding to decimal numbers of
     * length x.substring(n).length().
     */
    private static WeightedState anyOfRightLength(String x, int n) {
        WeightedState s = new WeightedState();
        if (x.length() == n) {
            s.setAccept(true);
        } else {
            s.addTransition(new WeightedTransition('0', '9', anyOfRightLength(x, n + 1)));
        }
        return s;
    }

    /**
     * Constructs sub-automaton corresponding to decimal numbers of value
     * at least x.substring(n) and length x.substring(n).length().
     */
    private static WeightedState atLeast(String x, int n, Collection<WeightedState> initials, boolean zeros) {
        WeightedState s = new WeightedState();
        if (x.length() == n) {
            s.setAccept(true);
        } else {
            if (zeros) {
                initials.add(s);
            }
            char c = x.charAt(n);
            s.addTransition(new WeightedTransition(c, atLeast(x, n + 1, initials, zeros && c == '0')));
            if (c < '9') {
                s.addTransition(new WeightedTransition((char) (c + 1), '9', anyOfRightLength(x, n + 1)));
            }
        }
        return s;
    }

    /**
     * Constructs sub-automaton corresponding to decimal numbers of value
     * at most x.substring(n) and length x.substring(n).length().
     */
    private static WeightedState atMost(String x, int n) {
        WeightedState s = new WeightedState();
        if (x.length() == n) {
            s.setAccept(true);
        } else {
            char c = x.charAt(n);
            s.addTransition(new WeightedTransition(c, atMost(x, (char) n + 1)));
            if (c > '0') {
                s.addTransition(new WeightedTransition('0', (char) (c - 1), anyOfRightLength(x, n + 1)));
            }
        }
        return s;
    }

    /**
     * Constructs sub-automaton corresponding to decimal numbers of value
     * between x.substring(n) and y.substring(n) and of length
     * x.substring(n).length() (which must be equal to y.substring(n).length
     * ()).
     */
    private static WeightedState between(String x, String y, int n, Collection<WeightedState> initials, boolean zeros) {
        WeightedState s = new WeightedState();
        if (x.length() == n) {
            s.setAccept(true);
        } else {
            if (zeros) {
                initials.add(s);
            }
            char cx = x.charAt(n);
            char cy = y.charAt(n);
            if (cx == cy) {
                s.addTransition(new WeightedTransition(cx, between(x, y, n + 1, initials, zeros && cx == '0')));
            } else { // cx<cy
                s.addTransition(new WeightedTransition(cx, atLeast(x, n + 1, initials, zeros && cx == '0')));
                s.addTransition(new WeightedTransition(cy, atMost(y, n + 1)));
                if (cx + 1 < cy) {
                    s.addTransition(new WeightedTransition((char) (cx + 1), (char) (cy - 1), anyOfRightLength(x, n + 1)));
                }
            }
        }
        return s;
    }

    /**
     * Returns a new (deterministic) automaton that accepts any single
     * character.
     */
    public static WeightedAutomaton makeAnyChar() {
        return makeCharRange(Character.MIN_VALUE, Character.MAX_VALUE);
    }

    /**
     * Returns a new (deterministic) automaton that accepts a single char
     * whose value is in the given interval (including both end points).
     */
    public static WeightedAutomaton makeCharRange(char min, char max) {
        if (min == max) {
            return makeChar(min);
        }
        WeightedAutomaton a = new WeightedAutomaton();
        WeightedState s1 = new WeightedState();
        WeightedState s2 = new WeightedState();
        a.initial = s1;
        s2.setAccept(true);
        if (min <= max) {
            s1.getTransitions().add(new WeightedTransition(min, max, s2));
        }
        a.deterministic = true;
        return a;
    }

    /**
     * Returns a new (deterministic) automaton that accepts a single character
     * of the given value.
     */
    public static WeightedAutomaton makeChar(char c) {
        WeightedAutomaton a = new WeightedAutomaton();
        a.singleton = Character.toString(c);
        a.deterministic = true;
        return a;
    }

    /**
     * Returns a new (deterministic) automaton that accepts all strings.
     */
    public static WeightedAutomaton makeAnyString() {
        WeightedAutomaton a = new WeightedAutomaton();
        WeightedState s = new WeightedState();
        a.initial = s;
        s.setAccept(true);
        s.getTransitions()
         .add(new WeightedTransition(Character.MIN_VALUE, Character.MAX_VALUE, s));
        a.deterministic = true;
        return a;
    }

    /**
     * Returns a new (deterministic) automaton that accepts a single character
     * in the given set.
     */
    public static WeightedAutomaton makeCharSet(String set) {
        if (set.length() == 1) {
            return makeChar(set.charAt(0));
        }
        WeightedAutomaton a = new WeightedAutomaton();
        WeightedState s1 = new WeightedState();
        WeightedState s2 = new WeightedState();
        a.initial = s1;
        s2.setAccept(true);
        for (int i = 0; i < set.length(); i++) {
            s1.getTransitions().add(new WeightedTransition(set.charAt(i), s2));
        }
        a.deterministic = true;
        a.reduce();
        return a;
    }

    /**
     * Constructs automaton that accept strings representing the given decimal
     * number. Surrounding whitespace is permitted.
     *
     * @param value
     *         string representation of decimal number
     */
    public static WeightedAutomaton makeDecimalValue(String value) {
        boolean minus = false;
        int i = 0;
        while (i < value.length()) {
            char c = value.charAt(i);
            if (c == '-') {
                minus = true;
            }
            if ((c >= '1' && c <= '9') || c == '.') {
                break;
            }
            i++;
        }
        StringBuilder b1 = new StringBuilder();
        StringBuilder b2 = new StringBuilder();
        int p = value.indexOf('.', i);
        if (p == -1) {
            b1.append(value.substring(i));
        } else {
            b1.append(value.substring(i, p));
            i = value.length() - 1;
            while (i > p) {
                char c = value.charAt(i);
                if (c >= '1' && c <= '9') {
                    break;
                }
                i--;
            }
            b2.append(value.substring(p + 1, i + 1));
        }
        if (b1.length() == 0) {
            b1.append("0");
        }
        WeightedAutomaton s;
        if (minus) {
            s = WeightedAutomaton.makeChar('-');
        } else {
            s = WeightedAutomaton.makeChar('+').optional();
        }
        WeightedAutomaton d;
        if (b2.length() == 0) {
            d = WeightedAutomaton.makeChar('.')
                                 .concatenate(WeightedAutomaton.makeChar('0')
                                                               .repeat(1))
                                 .optional();
        } else {
            d = WeightedAutomaton.makeChar('.')
                                 .concatenate(WeightedAutomaton.makeString(b2.toString()))
                                 .concatenate(
                                         WeightedAutomaton.makeChar('0')
                                                          .repeat());
        }
        WeightedAutomaton ws = Datatypes.getWhitespaceAutomaton();
        return WeightedAutomaton.minimize(ws.concatenate(s.concatenate(
                WeightedAutomaton.makeChar('0').repeat()).concatenate(
                WeightedAutomaton.makeString(b1.toString())).concatenate(d))
                                            .concatenate(ws));
    }

    /**
     * Returns a new (deterministic) automaton that accepts only the empty
     * string.
     */
    public static WeightedAutomaton makeEmptyString() {
        WeightedAutomaton a = new WeightedAutomaton();
        a.singleton = "";
        a.deterministic = true;
        return a;
    }

    /**
     * Constructs automaton that accept strings representing decimal numbers
     * that can be written with at most the given number of digits in the
     * fraction part. Surrounding whitespace is permitted.
     *
     * @param i
     *         max number of necessary fraction digits
     */
    public static WeightedAutomaton makeFractionDigits(int i) {
        return WeightedAutomaton.minimize(
                (new RegExp("[ \t\n\r]*[-+]?[0-9]+(\\.[0-9]{0," + i + "}0*)?[ \t\n\r]*"))
                        .toAutomaton());
    }

    /**
     * Constructs automaton that accept strings representing the given integer.
     * Surrounding whitespace is permitted.
     *
     * @param value
     *         string representation of integer
     */
    public static WeightedAutomaton makeIntegerValue(String value) {
        boolean minus = false;
        int i = 0;
        while (i < value.length()) {
            char c = value.charAt(i);
            if (c == '-') {
                minus = true;
            }
            if (c >= '1' && c <= '9') {
                break;
            }
            i++;
        }
        StringBuilder b = new StringBuilder();
        b.append(value.substring(i));
        if (b.length() == 0) {
            b.append("0");
        }
        WeightedAutomaton s;
        if (minus) {
            s = WeightedAutomaton.makeChar('-');
        } else {
            s = WeightedAutomaton.makeChar('+').optional();
        }
        WeightedAutomaton ws = Datatypes.getWhitespaceAutomaton();
        return WeightedAutomaton.minimize(ws.concatenate(s.concatenate(
                WeightedAutomaton.makeChar('0').repeat()).concatenate(
                WeightedAutomaton.makeString(b.toString()))).concatenate(ws));
    }

    /**
     * Returns a new automaton that accepts strings representing
     * decimal non-negative integers in the given interval.
     *
     * @param min
     *         minimal value of interval
     * @param max
     *         maximal value of inverval (both end points are included in the
     *         interval)
     * @param digits
     *         if >0, use fixed number of digits (strings must be prefixed by
     *         0's to obtain the right length) - otherwise, the number of
     *         digits
     *         is not fixed
     *
     * @throws IllegalArgumentException
     *         if min>max or if numbers in the interval cannot be expressed
     *         with
     *         the given fixed number of digits
     */
    public static WeightedAutomaton makeInterval(int min, int max, int digits)
            throws IllegalArgumentException {
        WeightedAutomaton a = new WeightedAutomaton();
        String x = Integer.toString(min);
        String y = Integer.toString(max);
        if (min > max || (digits > 0 && y.length() > digits)) {
            throw new IllegalArgumentException();
        }
        int d;
        if (digits > 0) {
            d = digits;
        } else {
            d = y.length();
        }
        StringBuilder bx = new StringBuilder();
        for (int i = x.length(); i < d; i++) {
            bx.append('0');
        }
        bx.append(x);
        x = bx.toString();
        StringBuilder by = new StringBuilder();
        for (int i = y.length(); i < d; i++) {
            by.append('0');
        }
        by.append(y);
        y = by.toString();
        Collection<WeightedState> initials = new ArrayList<WeightedState>();
        a.initial = between(x, y, 0, initials, digits <= 0);
        if (digits <= 0) {
            ArrayList<WeightedStatePair> pairs = new ArrayList<WeightedStatePair>();
            for (WeightedState p : initials) {
                if (a.initial != p) {
                    pairs.add(new WeightedStatePair(a.initial, p));
                }
            }
            a.addEpsilons(pairs);
            a.initial.addTransition(new WeightedTransition('0', a.initial));
            a.deterministic = false;
        } else {
            a.deterministic = true;
        }
        a.checkMinimizeAlways();
        return a;
    }

    /**
     * Constructs automaton that accept strings representing nonnegative
     * integers that are not larger than the given value.
     *
     * @param n
     *         string representation of maximum value
     */
    public static WeightedAutomaton makeMaxInteger(String n) {
        int i = 0;
        while (i < n.length() && n.charAt(i) == '0') {
            i++;
        }
        StringBuilder b = new StringBuilder();
        b.append("0*(0|");
        if (i < n.length()) {
            b.append("[0-9]{1," + (n.length() - i - 1) + "}|");
        }
        maxInteger(n.substring(i), 0, b);
        b.append(")");
        return WeightedAutomaton.minimize((new RegExp(b.toString()))
												  .toAutomaton());
    }

    /**
     * Constructs automaton that accept strings representing nonnegative
     * integers that are not less that the given value.
     *
     * @param n
     *         string representation of minimum value
     */
    public static WeightedAutomaton makeMinInteger(String n) {
        int i = 0;
        while (i + 1 < n.length() && n.charAt(i) == '0') {
            i++;
        }
        StringBuilder b = new StringBuilder();
        b.append("0*");
        minInteger(n.substring(i), 0, b);
        b.append("[0-9]*");
        return WeightedAutomaton.minimize((new RegExp(b.toString()))
												  .toAutomaton());
    }

    /**
     * Returns a new (deterministic) automaton that accepts the single given
     * string.
     */
    public static WeightedAutomaton makeString(String s) {
        WeightedAutomaton a = new WeightedAutomaton();
        a.singleton = s;
        a.deterministic = true;
        return a;
    }

    /**
     * Constructs deterministic automaton that matches strings that contain the
     * given substring.
     */
    public static WeightedAutomaton makeStringMatcher(String s) {
        WeightedAutomaton a = new WeightedAutomaton();
        WeightedState[] states = new WeightedState[s.length() + 1];
        states[0] = a.initial;
        for (int i = 0; i < s.length(); i++) {
            states[i + 1] = new WeightedState();
        }
        WeightedState f = states[s.length()];
        f.setAccept(true);
        f.getTransitions()
         .add(new WeightedTransition(Character.MIN_VALUE,
                                     Character.MAX_VALUE,
                                     f));
        for (int i = 0; i < s.length(); i++) {
            Set<Character> done = new HashSet<Character>();
            char c = s.charAt(i);
            states[i].getTransitions()
                     .add(new WeightedTransition(c, states[i + 1]));
            done.add(c);
            for (int j = i; j >= 1; j--) {
                char d = s.charAt(j - 1);
                if (!done.contains(d) &&
                    s.substring(0, j - 1).equals(s.substring(i - j + 1, i))) {
                    states[i].getTransitions()
                             .add(new WeightedTransition(d, states[j]));
                    done.add(d);
                }
            }
            char[] da = new char[done.size()];
            int h = 0;
            for (char w : done) {
                da[h++] = w;
            }
            Arrays.sort(da);
            int from = Character.MIN_VALUE;
            int k = 0;
            while (from <= Character.MAX_VALUE) {
                while (k < da.length && da[k] == from) {
                    k++;
                    from++;
                }
                if (from <= Character.MAX_VALUE) {
                    int to = Character.MAX_VALUE;
                    if (k < da.length) {
                        to = da[k] - 1;
                        k++;
                    }
                    states[i].getTransitions()
                             .add(new WeightedTransition((char) from,
                                                         (char) to,
                                                         states[0]));
                    from = to + 2;
                }
            }
        }
        a.deterministic = true;
        return a;
    }

    /**
     * Returns a new (deterministic and minimal) automaton that accepts the
     * union of the given set of strings. The input character sequences are
     * internally sorted in-place, so the input array is modified.
     *
     * @see StringUnionOperations
     */
    public static WeightedAutomaton makeStringUnion(CharSequence... strings) {
        if (strings.length == 0) {
            return makeEmpty();
        }
        Arrays.sort(strings, StringUnionOperations.LEXICOGRAPHIC_ORDER);
        WeightedAutomaton a = new WeightedAutomaton();
        a.setInitialState(StringUnionOperations.build(strings));
        a.setDeterministic(true);
        a.reduce();
        a.recomputeHashCode();
        return a;
    }

    /**
     * Returns a new (deterministic) automaton with the empty language.
     */
    public static WeightedAutomaton makeEmpty() {
        WeightedAutomaton a = new WeightedAutomaton();
        WeightedState s = new WeightedState();
        a.initial = s;
        a.deterministic = true;
        return a;
    }

    /**
     * Constructs automaton that accept strings representing decimal numbers
     * that can be written with at most the given number of digits.
     * Surrounding whitespace is permitted.
     *
     * @param i
     *         max number of necessary digits
     */
    public static WeightedAutomaton makeTotalDigits(int i) {
        return WeightedAutomaton.minimize((new RegExp("[ \t\n\r]*[-+]?0*" +
                                                      "([0-9]{0," + i + "}|((" +
                                                      "([0-9]\\.*){0," + i +
                                                      "})&@\\.@)0*)[ \t\n\r]*"))
                                                  .toAutomaton());
    }

    private static void maxInteger(String n, int i, StringBuilder b) {
        b.append('(');
        if (i < n.length()) {
            char c = n.charAt(i);
            if (c != '0') {
                b.append("[0-" +
                         (char) (c - 1) +
                         "][0-9]{" +
                         (n.length() - i - 1) +
                         "}|");
            }
            b.append(c);
            maxInteger(n, i + 1, b);
        }
        b.append(')');
    }

    private static void minInteger(String n, int i, StringBuilder b) {
        b.append('(');
        if (i < n.length()) {
            char c = n.charAt(i);
            if (c != '9') {
                b.append("[" +
                         (char) (c + 1) +
                         "-9][0-9]{" +
                         (n.length() - i - 1) +
                         "}|");
            }
            b.append(c);
            minInteger(n, i + 1, b);
        }
        b.append(')');
    }
}
