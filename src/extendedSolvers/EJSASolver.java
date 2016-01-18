package extendedSolvers;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import dk.brics.string.stringoperations.*;
import old.PrecisePrefix;
import old.PreciseSubstring;
import old.PreciseSuffix;

import java.util.ArrayList;
import java.util.Set;

@SuppressWarnings("Duplicates")
public class EJSASolver extends ExtendedSolver<Automaton> {

    @Override
    public void newSymbolicString(int id) {
        Automaton automaton = BasicAutomata.makeAnyString();
        this.symbolicStringMap.put(id, automaton);
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
    }

    @Override
    public String replaceEscapes(String value) {
        // all unicode characters supported
        return value;
    }

    @Override
    public void propagateSymbolicString(int id, int base) {
        Automaton baseAutomaton = this.symbolicStringMap.get(base);
        this.symbolicStringMap.put(id, baseAutomaton);
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
    public void substring(int id, int base, int start) {

        // get automaton
        Automaton automaton = this.symbolicStringMap.get(base);

        // get operation
        PrecisePrefix prefix = new PrecisePrefix(start);

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
    public void insert(int id, int base, int arg, int offset) {

        // get automata
        Automaton baseAutomaton = this.symbolicStringMap.get(base);
        Automaton argAutomaton = this.symbolicStringMap.get(arg);

        if (offset >= 0) {

            // get start substring
            PreciseSubstring substr = new PreciseSubstring(0, offset);
            Automaton startAutomaton = substr.op(baseAutomaton);

            // get end substring
            PrecisePrefix prefix = new PrecisePrefix(offset);
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
            PrecisePrefix prefix = new PrecisePrefix(offset);
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
    public void setCharAt(int id, int base, int arg, int offset) {

        // get automata
        Automaton baseAutomaton = this.symbolicStringMap.get(base);
        Automaton argAutomaton = this.symbolicStringMap.get(arg);

        if (offset >= 0) {

            // get start substring
            PreciseSubstring substr = new PreciseSubstring(0, offset);
            Automaton startAutomaton = substr.op(baseAutomaton);

            // get end substring
            PrecisePrefix prefix = new PrecisePrefix(offset + 1);
            Automaton endAutomaton = prefix.op(baseAutomaton);

            // set character at offset
            baseAutomaton = startAutomaton.concatenate(argAutomaton)
                                          .concatenate(endAutomaton);
        } else {

            // get end substring
            PrecisePrefix prefix = new PrecisePrefix(1);
            Automaton endAutomaton = prefix.op(baseAutomaton);

            // set first character
            baseAutomaton = argAutomaton.concatenate(endAutomaton);
        }

        // store result automaton
        this.symbolicStringMap.put(id, baseAutomaton);
    }

    @Override
    public void trim(int id, int base) {

        // get automaton
        Automaton automaton = this.symbolicStringMap.get(base);

        // check automaton with length of one?
        AssertHasLength hasLength = new AssertHasLength(1, 1);
        Automaton temp = hasLength.op(automaton);

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
                PreciseSuffix suffix = new PreciseSuffix(start);
                startAutomaton = suffix.op(automaton);
            }

            // get end substring automaton
            PrecisePrefix prefix = new PrecisePrefix(end);
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
        PreciseSuffix suffix = new PreciseSuffix(loc);
        Automaton startAutomaton = suffix.op(automaton);

        // gt end substring automaton
        PrecisePrefix prefix = new PrecisePrefix(loc + 1);
        Automaton endAutomaton = prefix.op(automaton);

        // concat start and end automata
        automaton = startAutomaton.concatenate(endAutomaton);

        // store result automaton
        this.symbolicStringMap.put(id, automaton);
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
    public void replace(int id, int base, int arg1, int arg2) {

        // get automata
        Automaton baseAutomaton = this.symbolicStringMap.get(base);
        Automaton arg1Automaton = this.symbolicStringMap.get(arg1);
        Automaton arg2Automaton = this.symbolicStringMap.get(arg2);

        // TODO: fix parser to allow implementation of JSA replace operation

        // store result automaton
        this.symbolicStringMap.put(id, baseAutomaton);
    }

    @Override
    public void contains(boolean result, int base, int arg) {

        // set current base and args
        this.setLast(base, arg);

        // get automata
        Automaton baseAutomaton = this.symbolicStringMap.get(base);
        Automaton argAutomaton = this.symbolicStringMap.get(arg);

        if (result) {

            //
            AssertContainsOther contains = new AssertContainsOther();
            baseAutomaton = contains.op(baseAutomaton, argAutomaton);

            //
            AssertContainedInOther cioOp = new AssertContainedInOther();
            argAutomaton = cioOp.op(baseAutomaton, argAutomaton);
        } else {

            // initialize temp as base
            Automaton temp = baseAutomaton;

            // if more than one finite string represented
            // TODO: Check if getFiniteStrings method is used properly here
            if (argAutomaton.getFiniteStrings(1) != null) {

                //
                Automaton x = BasicAutomata.makeAnyString();
                x = x.concatenate(argAutomaton);
                x = x.concatenate(BasicAutomata.makeAnyString());
                temp = baseAutomaton.minus(x);
            }

            // if more than one finite string represented
            if (baseAutomaton.getFiniteStrings(1) != null) {

                //
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
    public void endsWith(boolean result, int base, int arg) {

        // set current base and args
        this.setLast(base, arg);

        // get automata
        Automaton baseAutomaton = this.symbolicStringMap.get(base);
        Automaton argAutomaton = this.symbolicStringMap.get(arg);

        if (result) {

            //
            AssertEndsWith ends = new AssertEndsWith();
            baseAutomaton = ends.op(baseAutomaton, argAutomaton);

            //
            Postfix postfix = new Postfix();
            Automaton temp = postfix.op(baseAutomaton);
            AssertContainedInOther cioOp = new AssertContainedInOther();
            argAutomaton = cioOp.op(temp, argAutomaton);

        } else {

            // initialize temp as base
            Automaton temp = baseAutomaton;

            // if more than one finite string represented
            if (argAutomaton.getFiniteStrings(1) != null) {

                //
                Automaton x = BasicAutomata.makeAnyString();
                x = x.concatenate(argAutomaton);
                temp = baseAutomaton.minus(x);
            }

            // if more than one finite string represented
            if (baseAutomaton.getFiniteStrings(1) != null) {

                //
                Postfix postfix = new Postfix();
                baseAutomaton = postfix.op(baseAutomaton);
                Automaton argComplement = argAutomaton.complement();
                argAutomaton = baseAutomaton.intersection(argComplement);
            }

            // set base from temp
            baseAutomaton = temp;
        }

        // store result automaton
        this.symbolicStringMap.put(base, baseAutomaton);
        this.symbolicStringMap.put(arg, argAutomaton);
    }

    @Override
    public void startsWith(boolean result, int base, int arg) {

        // set current base and args
        this.setLast(base, arg);

        // get automata
        Automaton baseAutomaton = this.symbolicStringMap.get(base);
        Automaton argAutomaton = this.symbolicStringMap.get(arg);

        if (result) {

            //
            AssertStartsWith starts = new AssertStartsWith();
            baseAutomaton = starts.op(baseAutomaton, argAutomaton);

            //
            Prefix prefix = new Prefix();
            Automaton temp = prefix.op(baseAutomaton);
            AssertContainedInOther cioOp = new AssertContainedInOther();
            argAutomaton = cioOp.op(argAutomaton, temp);
        } else {

            // initialize temp as base
            Automaton temp = baseAutomaton;

            // if more than one finite string represented
            if (argAutomaton.getFiniteStrings(1) != null) {

                //
                Automaton x = BasicAutomata.makeAnyString();
                x = argAutomaton.concatenate(x);
                temp = baseAutomaton.minus(x);
            }

            // if more than one finite string represented
            if (baseAutomaton.getFiniteStrings(1) != null) {

                //
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
    public void equals(boolean result, int base, int arg) {

        // set current base and args
        this.setLast(base, arg);

        // get automata
        Automaton baseAutomaton = this.symbolicStringMap.get(base);
        Automaton argAutomaton = this.symbolicStringMap.get(arg);

        if (result) {

            // equality branch result
            AssertEquals eq = new AssertEquals();
            baseAutomaton = eq.op(baseAutomaton, argAutomaton);

            // not sure why setting arg as previous base
            argAutomaton = baseAutomaton;
        } else {

            // inequality branch result
            AssertNotEquals neq = new AssertNotEquals();
            Automaton temp = neq.op(baseAutomaton, argAutomaton);

            // set arg automaton
            argAutomaton = neq.op(argAutomaton, baseAutomaton);

            // if more than one finite string represented
            if (baseAutomaton.getFiniteStrings(1) != null) {

                //
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

        // set current base and args
        this.setLast(base, arg);

        // get automata
        Automaton baseAutomaton = this.symbolicStringMap.get(base);
        Automaton argAutomaton = this.symbolicStringMap.get(arg);

        // get ignore case equivalent automata
        Automaton baseIgnoreCase = ignoreCase(baseAutomaton);
        Automaton argIgnoreCase = ignoreCase(argAutomaton);

        if (result) {

            // equality branch result
            AssertEquals eq = new AssertEquals();
            baseAutomaton = eq.op(baseAutomaton, argIgnoreCase);

            // inequality branch result?
            argAutomaton = eq.op(argAutomaton, baseIgnoreCase);
        } else {

            // inequality branch result
            AssertNotEquals neq = new AssertNotEquals();
            baseAutomaton = neq.op(baseAutomaton, argIgnoreCase);

            // equality branch result?
            argAutomaton = neq.op(argAutomaton, baseIgnoreCase);
        }

        // store result automaton
        this.symbolicStringMap.put(base, baseAutomaton);
        this.symbolicStringMap.put(arg, argAutomaton);
    }

    @Override
    public void isEmpty(boolean result, int base) {

        // set current base and args
        this.setLast(base, -1);

        // get automaton
        Automaton baseAutomaton = this.symbolicStringMap.get(base);

        if (result) {

            // empty branch result
            AssertEmpty empty = new AssertEmpty();
            baseAutomaton = empty.op(baseAutomaton);
        } else {

            // not empty branch result
            AssertNotEmpty notEmpty = new AssertNotEmpty();
            baseAutomaton = notEmpty.op(baseAutomaton);
        }

        // store result automaton
        this.symbolicStringMap.put(base, baseAutomaton);
    }

    @Override
    public String getSatisfiableResult(int id) {
        Automaton automaton = this.symbolicStringMap.get(id);
        return automaton.getShortestExample(true);
    }

    @Override
    public boolean isSatisfiable(int id) {
        Automaton automaton = this.symbolicStringMap.get(id);
        return automaton.isEmpty();
    }

    @Override
    public boolean isSingleton(int id) {
        Automaton automaton = this.symbolicStringMap.get(id);
        String singleton = automaton.getSingleton();
        return singleton != null;
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
    public void shutDown() {

    }

    static private Automaton ignoreCase(Automaton automaton) {

        // clone automaton
        Automaton clone = automaton.clone();

        // for all states
        for (State state : clone.getStates()) {

            // all transitions from state
            Set<Transition> transitions = state.getTransitions();

            // for all transitions in current set of transitions
            for (Transition t : new ArrayList<Transition>(transitions)) {

                // get transition values
                char min = t.getMin();
                char max = t.getMax();
                State dest = t.getDest();

                // if transition represents subset of characters
                if (min != Character.MIN_VALUE || max != Character.MAX_VALUE) {

                    // for each character represented in transition
                    for (int i = min; i <= max; i++) {

                        // get i as char
                        char c = (char) i;

                        // if char is uppercase
                        if (Character.isUpperCase(c)) {

                            // add corresponding lowercase transition
                            char lc = Character.toLowerCase(c);
                            Transition lcTrans = new Transition(lc, dest);
                            transitions.add(lcTrans);
                        }

                        // if char is lowercase
                        if (Character.isLowerCase(c)) {

                            // add corresponding uppercase transition
                            char uc = Character.toUpperCase(c);
                            Transition ucTrans = new Transition(uc, dest);
                            transitions.add(ucTrans);
                        }
                    }
                }
            }
        }

        clone.setDeterministic(false);
        clone.reduce();
        clone.minimize();

        return clone;
    }
}
