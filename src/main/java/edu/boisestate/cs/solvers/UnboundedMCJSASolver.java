package edu.boisestate.cs.solvers;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import edu.boisestate.cs.automatonModel.operations.StringModelCounter;
import edu.boisestate.cs.automatonModel.operations.PreciseSuffix;
import edu.boisestate.cs.automatonModel.operations.PrecisePrefix;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("Duplicates")
public class UnboundedMCJSASolver
        extends UnboundedEJSASolver
        implements ModelCountSolver {

    protected final Map<Integer, Integer> boundMap = new HashMap<>();

    public UnboundedMCJSASolver() {
        super(0);

        // setup automaton options
        // set minimization algorithm as huffman
        Automaton.setMinimization(0);
    }

    public UnboundedMCJSASolver(int bound) {
        super(bound);

        // setup automaton options
        // set minimization algorithm as huffman
        Automaton.setMinimization(0);
    }

    @Override
    public void append(int id, int base, int arg, int start, int end) {

        super.append(id, base, arg, start, end);

        // updated bounding map
        int baseBound = this.boundMap.get(base);
        int newBound = baseBound + (end - start);
        this.boundMap.put(id, newBound);
    }

    @Override
    public void append(int id, int base, int arg) {

        super.append( id, base, arg);

        // updated bounding map
        int baseBound = this.boundMap.get(base);
        int argBound = this.boundMap.get(arg);
        int newBound = baseBound + argBound;
        this.boundMap.put(id, newBound);
    }

    @Override
    public void delete(int id, int base, int start, int end) {

        // initialize new bound value
        int newBound = this.boundMap.get(base);

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

            // set new bound value based on valid start and end indices
            newBound = end - start;
        }

        // store result automaton
        this.symbolicStringMap.put(id, automaton);

        // updated bounding map
        this.boundMap.put(id, newBound);
    }

    @Override
    public void deleteCharAt(int id, int base, int loc) {

        super.deleteCharAt(id, base, loc);

        // updated bounding map
        int baseBound = this.boundMap.get(base);
        int newBound = baseBound - 1;
        if (newBound < 0) {
            newBound = 0;
        }
        this.boundMap.put(id, newBound);
    }

    @Override
    public Set<String> getAllVales(int id) {

        // get automaton
        Automaton automaton = this.symbolicStringMap.get(id);

        // get bounding length from map
        int boundingLength = this.boundMap.get(id);

        // create bounding automaton
        Automaton boundingAutomaton = Automaton.makeAnyChar();
        boundingAutomaton = boundingAutomaton.repeat(0, boundingLength);

        // bound automaton
        Automaton boundedAutomaton = automaton.intersection(boundingAutomaton);

        // return all finite strings
        return boundedAutomaton.getFiniteStrings();
    }

    @Override
    public long getModelCount(int id) {

        // get automaton
        Automaton automaton = this.symbolicStringMap.get(id);

        // get bounding length from map
        int boundingLength = this.boundMap.get(id);

        // get model count
        BigInteger count =
                StringModelCounter.ModelCount(automaton, boundingLength);

        // return count as an integer
        return count.intValue();
    }

    @Override
    public void insert(int id, int base, int arg, int offset) {

        super.insert(id, base, arg, offset);

        // updated bounding map
        int baseBound = this.boundMap.get(base);
        int argBound = this.boundMap.get(arg);
        int newBound = baseBound + argBound;
        this.boundMap.put(id, newBound);
    }

    @Override
    public void insert(int id,
                       int base,
                       int arg,
                       int offset,
                       int start,
                       int end) {

        super.insert(id, base, arg, offset, start, end);

        // updated bounding map
        int baseBound = this.boundMap.get(base);
        int newBound = baseBound + (end - start);
        this.boundMap.put(id, newBound);
    }

    @Override
    public void newConcreteString(int id, String string) {

        super.newConcreteString(id, string);

        // get new bound length
        int newBound = 0;
        if (string != null) {
            newBound = string.length();
        }

        // set new bound
        this.boundMap.put(id, newBound);
    }

    @Override
    public void newSymbolicString(int id) {

        super.newSymbolicString(id);

        // set new bound
        this.boundMap.put(id, this.initialBound);
    }

    @Override
    public void propagateSymbolicString(int id, int base) {

        super.propagateSymbolicString(id, base);

        // updated bounding map
        this.boundMap.put(id, this.boundMap.get(base));
    }

    @Override
    public void replaceCharKnown(int id, int base, char find, char replace) {
        super.replaceCharKnown(id, base, find, replace);

        // update bound value
        this.boundMap.put(id, this.boundMap.get(base));
    }

    @Override
    public void replaceCharFindKnown(int id, int base, char find) {
        super.replaceCharFindKnown(id, base, find);

        // update bound value
        this.boundMap.put(id, this.boundMap.get(base));
    }

    @Override
    public void replaceCharReplaceKnown(int id, int base, char replace) {
        super.replaceCharReplaceKnown(id, base, replace);

        // update bound value
        this.boundMap.put(id, this.boundMap.get(base));
    }

    @Override
    public void replaceCharUnknown(int id, int base) {
        super.replaceCharUnknown(id, base);

        // update bound value
        this.boundMap.put(id, this.boundMap.get(base));
    }

    @Override
    public void replaceStrings(int id, int base, int arg1, int arg2) {

        super.replaceStrings(id, base, arg1, arg2);

        // update bound value
        this.boundMap.put(id, this.boundMap.get(base));
        // TODO: bound value update without approximation
    }

    @Override
    public void reverse(int id, int base) {

        super.reverse(id, base);

        // updated bounding map
        this.boundMap.put(id, this.boundMap.get(base));
    }

    @Override
    public void setCharAt(int id, int base, int arg, int offset) {

        super.setCharAt(id, base, arg, offset);

        // updated bounding map
        this.boundMap.put(id, this.boundMap.get(base));
    }

    @Override
    public void setLength(int id, int base, int length) {

        super.setLength(id, base, length);

        // updated bounding map
        this.boundMap.put(id, length);
    }

    @Override
    public void substring(int id, int base, int start) {

        super.substring(id, base, start);

        // updated bounding map
        int baseBound = this.boundMap.get(base);
        int newBound = baseBound - start;
        this.boundMap.put(id, newBound);
    }

    @Override
    public void substring(int id, int base, int start, int end) {

        super.substring(id, base, start, end);

        // updated bounding map
        this.boundMap.put(id, end - start);
    }

    @Override
    public void toLowerCase(int id, int base) {

        super.toLowerCase(id, base);

        // updated bounding map
        this.boundMap.put(id, this.boundMap.get(base));
    }

    @Override
    public void toUpperCase(int id, int base) {

        super.toUpperCase(id, base);

        // updated bounding map
        this.boundMap.put(id, this.boundMap.get(base));
    }

    @Override
    public void trim(int id, int base) {

        super.trim(id, base);

        // updated bounding map
        this.boundMap.put(id, this.boundMap.get(base));
        // TODO: bound value update without approximation
    }
}
