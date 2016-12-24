package edu.boisestate.cs.solvers;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.string.stringoperations.*;
import edu.boisestate.cs.automatonModel.operations.IgnoreCase;
import edu.boisestate.cs.automatonModel.operations.PreciseSuffix;
import edu.boisestate.cs.automatonModel.operations.PreciseSubstring;
import edu.boisestate.cs.automatonModel.operations.PrecisePrefix;

import java.util.Set;

@SuppressWarnings("Duplicates")
public class UnboundedEJSASolver
        extends ExtendedSolver<Automaton> {

    public UnboundedEJSASolver() {

        // setup automaton options
        // set minimization algorithm as huffman
        Automaton.setMinimization(0);
    }

    public UnboundedEJSASolver(int initialBound) {
        super(initialBound);
    }

    @Override
    public void append(int id, int base, int arg, int start, int end) {

        // get automata
        Automaton baseAutomaton = this.symbolicStringMap.get(base);
        Automaton argAutomaton = this.symbolicStringMap.get(arg);

        // get substring
        PreciseSubstring substr = new PreciseSubstring(start, end);
        Automaton subAutomaton = substr.op(argAutomaton);

        // perform operation
        baseAutomaton = baseAutomaton.concatenate(subAutomaton);

        // store result automaton
        this.symbolicStringMap.put(id, baseAutomaton);
    }

    @Override
    public void append(int id, int base, int arg) {

        // get automata
        Automaton baseAutomaton = this.symbolicStringMap.get(base);
        Automaton argAutomaton = this.symbolicStringMap.get(arg);

        // perform operation
        baseAutomaton = baseAutomaton.concatenate(argAutomaton);

        // store result automaton
        this.symbolicStringMap.put(id, baseAutomaton);
    }

    @Override
    public void contains(boolean result, int base, int arg) {

        // get automata
        Automaton baseAutomaton = this.symbolicStringMap.get(base);
        Automaton argAutomaton = this.symbolicStringMap.get(arg);

        if (result) {

            // get satisfying base automaton
            AssertContainsOther contains = new AssertContainsOther();
            baseAutomaton = contains.op(baseAutomaton, argAutomaton);

            // get satisfying arg automaton
            AssertContainedInOther cioOp = new AssertContainedInOther();
            argAutomaton = cioOp.op(argAutomaton, baseAutomaton);
        } else {

            // initialize temp as base
            Automaton temp = baseAutomaton;

            // if more than one finite string represented
            if (argAutomaton.getFiniteStrings(1) != null) {

                // generate automaton for any string containing strings
                // from arg automaton
                Automaton x = BasicAutomata.makeAnyString();
                x = x.concatenate(argAutomaton);
                x = x.concatenate(BasicAutomata.makeAnyString());

                // get satisfying base automaton
                temp = baseAutomaton.minus(x);
            }

            // if more than one finite string represented
            if (baseAutomaton.getFiniteStrings(1) != null) {

                // get satisfying arg automaton
                Automaton complementBase = baseAutomaton.complement();
                argAutomaton = argAutomaton.intersection(complementBase);
            }

            // set base from temp
            baseAutomaton = temp;
        }

        // store result automaton
        this.symbolicStringMap.put(base, baseAutomaton);
        this.symbolicStringMap.put(arg, argAutomaton);
    }

    @Override
    public void delete(int id, int base, int start, int end) {

        // get automaton
        Automaton automaton = this.symbolicStringMap.get(base);

        if (start < end) {

            // declare start automaton
            Automaton startAutomaton;

            // if start index is 0
            if (start == 0) {

                // set start automaton as empty string
                startAutomaton = BasicAutomata.makeEmptyString();
            } else {

                // get start substring automaton
                PrecisePrefix suffix = new PrecisePrefix(start);
                startAutomaton = suffix.op(automaton);
            }

            // get end substring automaton
            PreciseSuffix prefix = new PreciseSuffix(end);
            Automaton endAutomaton = prefix.op(automaton);

            // concat start and end automata
            automaton = startAutomaton.concatenate(endAutomaton);
        }

        // store result automaton
        this.symbolicStringMap.put(id, automaton);
    }

    @Override
    public void deleteCharAt(int id, int base, int loc) {

        // get automaton
        Automaton automaton = this.symbolicStringMap.get(base);

        // get start substring automaton
        PrecisePrefix suffix = new PrecisePrefix(loc);
        Automaton startAutomaton = suffix.op(automaton);

        // gt end substring automaton
        PreciseSuffix prefix = new PreciseSuffix(loc + 1);
        Automaton endAutomaton = prefix.op(automaton);

        // concat start and end automata
        automaton = startAutomaton.concatenate(endAutomaton);

        // store result automaton
        this.symbolicStringMap.put(id, automaton);
    }

    @Override
    public void endsWith(boolean result, int base, int arg) {

        // get automata
        Automaton baseAutomaton = this.symbolicStringMap.get(base);
        Automaton argAutomaton = this.symbolicStringMap.get(arg);

        if (result) {

            // get satisfying base automaton
            AssertEndsWith ends = new AssertEndsWith();
            baseAutomaton = ends.op(baseAutomaton, argAutomaton);

            // get satisfying arg automaton
            Postfix postfix = new Postfix();
            Automaton temp = postfix.op(baseAutomaton);
            AssertContainedInOther cioOp = new AssertContainedInOther();
            argAutomaton = cioOp.op(argAutomaton, temp);

        } else {

            // initialize temp as base
            Automaton temp = baseAutomaton;

            // if more than one finite string represented
            if (argAutomaton.getFiniteStrings(1) != null) {

                // get satisfying arg automaton
                Automaton x = BasicAutomata.makeAnyString();
                x = x.concatenate(argAutomaton);
                temp = baseAutomaton.minus(x);
            }

            // if more than one finite string represented
            if (baseAutomaton.getFiniteStrings(1) != null) {

                // get satisfying arg automaton
                Postfix postfix = new Postfix();
                Automaton x = postfix.op(baseAutomaton);
                Automaton argComplement = argAutomaton.complement();
                argAutomaton = x.intersection(argComplement);
            }

            // set base from temp
            baseAutomaton = temp;
        }

        // store result automaton
        this.symbolicStringMap.put(base, baseAutomaton);
        this.symbolicStringMap.put(arg, argAutomaton);
    }

    @Override
    public void equals(boolean result, int base, int arg) {

        // get automata
        Automaton baseAutomaton = this.symbolicStringMap.get(base);
        Automaton argAutomaton = this.symbolicStringMap.get(arg);

        if (result) {

            // get satisfying base automaton
            AssertEquals eq = new AssertEquals();
            baseAutomaton = eq.op(baseAutomaton, argAutomaton);

            // get satisfying arg automaton
            argAutomaton = baseAutomaton;
        } else {

            // get satisfying base automaton
            AssertNotEquals neq = new AssertNotEquals();
            Automaton temp = neq.op(baseAutomaton, argAutomaton);

            // get satisfying arg automaton
            argAutomaton = neq.op(argAutomaton, baseAutomaton);

            // if more than one finite string represented
            if (baseAutomaton.getFiniteStrings(1) != null) {

                // get satisfying arg automaton
                Automaton baseComplement = baseAutomaton.complement();
                argAutomaton = argAutomaton.intersection(baseComplement);
            }

            // set base from temp
            baseAutomaton = temp;
        }

        // store result automaton
        this.symbolicStringMap.put(base, baseAutomaton);
        this.symbolicStringMap.put(arg, argAutomaton);
    }

    @Override
    public void equalsIgnoreCase(boolean result, int base, int arg) {

        // get automata
        Automaton baseAutomaton = this.symbolicStringMap.get(base);
        Automaton argAutomaton = this.symbolicStringMap.get(arg);

        // get ignore case equivalent automata
        IgnoreCase ignoreCase = new IgnoreCase();
        Automaton baseIgnoreCase = ignoreCase.op(baseAutomaton);
        Automaton argIgnoreCase = ignoreCase.op(argAutomaton);

        if (result) {

            // get satisfying base automaton
            AssertEquals eq = new AssertEquals();
            baseAutomaton = eq.op(baseAutomaton, argIgnoreCase);

            // get satisfying arg automaton
            argAutomaton = eq.op(argAutomaton, baseIgnoreCase);
        } else {

            // get satisfying base automaton
            AssertNotEquals neq = new AssertNotEquals();
            baseAutomaton = neq.op(baseAutomaton, argIgnoreCase);

            // get satisfying arg automaton
            argAutomaton = neq.op(argAutomaton, baseIgnoreCase);
        }

        // store result automaton
        this.symbolicStringMap.put(base, baseAutomaton);
        this.symbolicStringMap.put(arg, argAutomaton);
    }

    @Override
    public String getSatisfiableResult(int id) {
        Automaton automaton = this.symbolicStringMap.get(id);
        return automaton.getShortestExample(true);
    }

    @Override
    public void insert(int id, int base, int arg, int offset) {

        // get automata
        Automaton baseAutomaton = this.symbolicStringMap.get(base);
        Automaton argAutomaton = this.symbolicStringMap.get(arg);

        if (offset >= 0) {

            // get start substring
            PreciseSubstring substr = new PreciseSubstring(0, offset);
            Automaton startAutomaton = substr.op(baseAutomaton);

            // get end substring
            PreciseSuffix prefix = new PreciseSuffix(offset);
            Automaton endAutomaton = prefix.op(baseAutomaton);

            // concat automata
            baseAutomaton = startAutomaton.concatenate(argAutomaton)
                                          .concatenate(endAutomaton);
        } else {
            baseAutomaton = argAutomaton.concatenate(baseAutomaton);
        }

        // store result automaton
        this.symbolicStringMap.put(id, baseAutomaton);
    }

    @Override
    public void insert(int id,
                       int base,
                       int arg,
                       int offset,
                       int start,
                       int end) {

        // get automata
        Automaton baseAutomaton = this.symbolicStringMap.get(base);
        Automaton argAutomaton = this.symbolicStringMap.get(arg);

        // get substring
        PreciseSubstring substr = new PreciseSubstring(start, end);
        argAutomaton = substr.op(argAutomaton);

        if (offset >= 0) {

            // get start substring
            PreciseSubstring startSubstr = new PreciseSubstring(0, offset);
            Automaton startAutomaton = startSubstr.op(baseAutomaton);

            // get end substring
            PreciseSuffix prefix = new PreciseSuffix(offset);
            Automaton endAutomaton = prefix.op(baseAutomaton);

            // concat automata
            baseAutomaton = startAutomaton.concatenate(argAutomaton)
                                          .concatenate(endAutomaton);
        } else {

            // insert before base automaton
            baseAutomaton = argAutomaton.concatenate(baseAutomaton);
        }

        // store result automaton
        this.symbolicStringMap.put(id, baseAutomaton);
    }

    @Override
    public void isEmpty(boolean result, int base) {

        // get automaton
        Automaton baseAutomaton = this.symbolicStringMap.get(base);

        if (result) {

            // get satisfying automaton
            AssertEmpty empty = new AssertEmpty();
            baseAutomaton = empty.op(baseAutomaton);

        } else {

            // get satisfying automaton
            AssertNotEmpty notEmpty = new AssertNotEmpty();
            baseAutomaton = notEmpty.op(baseAutomaton);

        }

        // store result automaton
        this.symbolicStringMap.put(base, baseAutomaton);
    }

    @Override
    public boolean isSatisfiable(int id) {
        Automaton automaton = this.symbolicStringMap.get(id);
        return !automaton.isEmpty();
    }

    @Override
    public boolean isSingleton(int id) {

        // get automaton from id
        Automaton automaton = this.symbolicStringMap.get(id);

        // get up to the first finite string, null if more
        Set<String> strings = automaton.getFiniteStrings(1);

        // return if single non-null string in automaton
        return strings != null &&
               strings.size() == 1 &&
               strings.iterator().next() != null;
    }

    @Override
    public boolean isSingleton(int id, String actualValue) {
        return this.isSingleton(id);
    }

    @Override
    public boolean isSound(int id, String actualValue) {

        // compute intersection of automaton and string value
        Automaton automaton = this.symbolicStringMap.get(id);
        Automaton value = BasicAutomata.makeString(actualValue);
        Automaton intersection = automaton.intersection(value);

        // sound if intersection is not empty
        return !intersection.isEmpty();
    }

    @Override
    public void newConcreteString(int id, String string) {
        Automaton automaton;

        if (string == null) {
            automaton = BasicAutomata.makeEmpty();
        } else if (string.equals("")) {
            automaton = BasicAutomata.makeEmptyString();
        } else {
            automaton = BasicAutomata.makeString(string);
        }

        this.symbolicStringMap.put(id, automaton);
        this.concreteStringMap.put(id, string);
    }

    @Override
    public void newSymbolicString(int id) {
        Automaton automaton = BasicAutomata.makeAnyString();
        this.symbolicStringMap.put(id, automaton);
    }

    @Override
    public void propagateSymbolicString(int id, int base) {
        Automaton baseAutomaton = this.symbolicStringMap.get(base);
        Automaton cloneAutomaton = baseAutomaton.clone();
        this.symbolicStringMap.put(id, cloneAutomaton);
    }

    @Override
    public void replaceCharKnown(int id, int base, char find, char replace) {

    }

    @Override
    public void replaceCharFindKnown(int id, int base, char find) {

    }

    @Override
    public void replaceCharReplaceKnown(int id, int base, char replace) {

    }

    @Override
    public void replaceCharUnknown(int id, int base) {

    }

    @Override
    public void replaceStrings(int id, int base, int arg1, int arg2) {

        // get automata
        Automaton baseAutomaton = this.symbolicStringMap.get(base);
        String arg1String = this.concreteStringMap.get(arg1);
        String arg2String = this.concreteStringMap.get(arg2);

        // check args constants
        if (arg1String != null &&
            arg1String.length() == 1 &&
            arg2String != null &&
            arg2String.length() == 1) {

            char oldChar;
            char newChar;
            boolean isOldUnknown = false;
            boolean isNewUnknown = false;

            // determine if old char is known
            try {
                int tempVal = Integer.parseInt(arg1String);

                if (tempVal < 10 && tempVal >= 0) {
                    isOldUnknown = true;
                }

                oldChar = ((char) tempVal);
            } catch (NumberFormatException e) {
                oldChar = arg1String.charAt(0);
            }

            // determine if new char is known
            try {
                int tempVal = Integer.parseInt(arg2String);

                if (tempVal < 10 && tempVal >= 0) {
                    isNewUnknown = true;
                }

                newChar = ((char) tempVal);
            } catch (NumberFormatException e) {
                newChar = arg2String.charAt(0);
            }

            // determine correct character replace operation
            UnaryOperation replace;
            if (isOldUnknown && isNewUnknown) {
                replace = new Replace4();
            } else if (isOldUnknown) {
                replace = new Replace3(newChar);
            } else if (isNewUnknown) {
                replace = new Replace2(oldChar);
            } else {
                replace = new Replace1(oldChar, newChar);
            }

            // perform character replace operation
            baseAutomaton = replace.op(baseAutomaton);

        } else if (arg1String != null && arg2String != null) {

            // get operation
            Replace6 replace = new Replace6(arg1String, arg2String);

            // perform operation
            baseAutomaton = replace.op(baseAutomaton);

        } else {
            System.err.print("EJSASolver.replace: at least one concrete" +
                             " string is null");
        }

        // store result automaton
        this.symbolicStringMap.put(id, baseAutomaton);
    }

    @Override
    public String replaceEscapes(String value) {
        // all unicode characters supported
        return value;
    }

    @Override
    public void reverse(int id, int base) {

        // get automaton
        Automaton automaton = this.symbolicStringMap.get(base);

        // get operation
        Reverse op = new Reverse();

        // perform operation
        Automaton resultAutomaton = op.op(automaton);

        // store result automaton
        this.symbolicStringMap.put(id, resultAutomaton);
    }

    @Override
    public void setCharAt(int id, int base, int arg, int offset) {

        // get automata
        Automaton baseAutomaton = this.symbolicStringMap.get(base);
        Automaton argAutomaton = this.symbolicStringMap.get(arg);

        if (offset >= 0) {

            // get start substring
            PreciseSubstring substr = new PreciseSubstring(0, offset);
            Automaton startAutomaton = substr.op(baseAutomaton);

            // get end substring
            PreciseSuffix prefix = new PreciseSuffix(offset + 1);
            Automaton endAutomaton = prefix.op(baseAutomaton);

            // set character at offset
            baseAutomaton = startAutomaton.concatenate(argAutomaton)
                                          .concatenate(endAutomaton);
        } else {

            // get end substring
            PreciseSuffix prefix = new PreciseSuffix(1);
            Automaton endAutomaton = prefix.op(baseAutomaton);

            // set first character
            baseAutomaton = argAutomaton.concatenate(endAutomaton);
        }

        // store result automaton
        this.symbolicStringMap.put(id, baseAutomaton);
    }

    @Override
    public void setLength(int id, int base, int length) {

        // get automaton
        Automaton automaton = this.symbolicStringMap.get(base);

        if (length == 0) {

            // set automaton as empty string automaton
            automaton = BasicAutomata.makeEmptyString();
        } else {

            // perform operation
            Automaton anyStr = BasicAutomata.makeAnyString();
            automaton = automaton.concatenate(anyStr);
            AssertHasLength hasLength = new AssertHasLength(0, length);
            automaton = hasLength.op(automaton);
        }

        // store result automaton
        this.symbolicStringMap.put(id, automaton);
    }

    @Override
    public void shutDown() {
        // nothing needed
    }

    @Override
    public void startsWith(boolean result, int base, int arg) {

        // get automata
        Automaton baseAutomaton = this.symbolicStringMap.get(base);
        Automaton argAutomaton = this.symbolicStringMap.get(arg);

        if (result) {

            // get satisfying base automaton
            AssertStartsWith starts = new AssertStartsWith();
            baseAutomaton = starts.op(baseAutomaton, argAutomaton);

            // get satisfying arg automaton
            Prefix prefix = new Prefix();
            Automaton temp = prefix.op(baseAutomaton);
            AssertContainedInOther cioOp = new AssertContainedInOther();
            argAutomaton = cioOp.op(argAutomaton, temp);
        } else {

            // initialize temp as base
            Automaton temp = baseAutomaton;

            // if more than one finite string represented
            if (argAutomaton.getFiniteStrings(1) != null) {

                // get satisfying base automaton
                Automaton x = BasicAutomata.makeAnyString();
                x = argAutomaton.concatenate(x);
                temp = baseAutomaton.minus(x);
            }

            // if more than one finite string represented
            if (baseAutomaton.getFiniteStrings(1) != null) {

                // get satisfying arg automaton
                Automaton baseComplement = baseAutomaton.complement();
                argAutomaton = argAutomaton.intersection(baseComplement);
            }

            // set base from temp
            baseAutomaton = temp;
        }

        // store result automaton
        this.symbolicStringMap.put(base, baseAutomaton);
        this.symbolicStringMap.put(arg, argAutomaton);
    }

    @Override
    public void substring(int id, int base, int start) {

        // get automaton
        Automaton automaton = this.symbolicStringMap.get(base);

        // get operation
        PreciseSuffix prefix = new PreciseSuffix(start);

        // perform operation
        automaton = prefix.op(automaton);

        // store result automaton
        this.symbolicStringMap.put(id, automaton);
    }

    @Override
    public void substring(int id, int base, int start, int end) {

        // get automaton
        Automaton automaton = this.symbolicStringMap.get(base);

        // get operation
        PreciseSubstring substr = new PreciseSubstring(start, end);

        // perform operation
        automaton = substr.op(automaton);

        // store result automaton
        this.symbolicStringMap.put(id, automaton);
    }

    @Override
    public void toLowerCase(int id, int base) {

        // get automaton
        Automaton automaton = this.symbolicStringMap.get(base);

        // get operation
        ToLowerCase op = new ToLowerCase();

        // perform operation
        Automaton resultAutomaton = op.op(automaton);

        // store result automaton
        this.symbolicStringMap.put(id, resultAutomaton);
    }

    @Override
    public void toUpperCase(int id, int base) {

        // get automaton
        Automaton automaton = this.symbolicStringMap.get(base);

        // get operation
        ToUpperCase op = new ToUpperCase();

        // perform operation
        Automaton resultAutomaton = op.op(automaton);

        // store result automaton
        this.symbolicStringMap.put(id, resultAutomaton);
    }

    @Override
    public void trim(int id, int base) {

        // get automaton
        Automaton automaton = this.symbolicStringMap.get(base);

        // check automaton with length of one?
        AssertHasLength hasLength = new AssertHasLength(1, 1);
        Automaton hasLenAuto = hasLength.op(automaton);
        Automaton temp = automaton.intersection(hasLenAuto);

        if (temp.equals(automaton)) {

            // union empty string automaton?
            Automaton emptyStr = BasicAutomata.makeEmptyString();
            automaton = temp.union(emptyStr);
        } else {

            // perform operation
            Trim trimOp = new Trim();
            automaton = trimOp.op(automaton);
        }

        // store result automaton
        this.symbolicStringMap.put(id, automaton);
    }
}
