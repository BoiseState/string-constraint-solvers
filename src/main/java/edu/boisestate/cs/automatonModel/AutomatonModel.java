package edu.boisestate.cs.automatonModel;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.BasicAutomata;
import dk.brics.string.stringoperations.UnaryOperation;
import edu.boisestate.cs.Alphabet;

import java.math.BigInteger;
import java.util.Set;

public abstract class AutomatonModel
        implements Cloneable {

    protected final Alphabet alphabet;
    protected int boundLength;
    protected AutomatonModelManager modelManager;

    public abstract String getAcceptedStringExample();

    public int getBoundLength() {
        return boundLength;
    }

    public abstract Set<String> getFiniteStrings();

    public abstract boolean isEmpty();

    public abstract boolean isSingleton();

    public void setBoundLength(int boundLength) {
        this.boundLength = boundLength;
    }

    protected AutomatonModel(Alphabet alphabet,
                             int initialBoundLength) {

        this.alphabet = alphabet;
        this.boundLength = initialBoundLength;
    }

    static Automaton performUnaryOperation(Automaton automaton,
                                           UnaryOperation operation,
                                           Alphabet alphabet) {
        // use operation
        Automaton result = operation.op(automaton);

        // bound resulting automaton to alphabet
        String charSet = alphabet.getCharSet();
        Automaton anyChar = BasicAutomata.makeCharSet(charSet).repeat();
        result = result.intersection(anyChar);

        //eas: even so the operation return the minimized automaton
        //the intersection might mess it up.
        //no need to call determinize since a minimize does
        //call that method first - only deterministic FA
        //can be minimized.
        result.minimize();

        // return resulting automaton
        return result;
    }

    public abstract AutomatonModel assertContainedInOther(AutomatonModel containingModel);

    public abstract AutomatonModel assertContainsOther(AutomatonModel containedModel);

    public abstract AutomatonModel assertEmpty();

    public abstract AutomatonModel assertEndsOther(AutomatonModel baseModel);

    public abstract AutomatonModel assertEndsWith(AutomatonModel endingModel);

    public abstract AutomatonModel assertEquals(AutomatonModel equalModel);

    public abstract AutomatonModel assertEqualsIgnoreCase(AutomatonModel equalModel);

    public abstract AutomatonModel assertHasLength(int min, int max);

    public abstract AutomatonModel assertNotContainedInOther(AutomatonModel notContainingModel);

    public abstract AutomatonModel assertNotContainsOther(AutomatonModel notContainedModel);

    public abstract AutomatonModel assertNotEmpty();

    public abstract AutomatonModel assertNotEndsOther(AutomatonModel notEndingModel);

    public abstract AutomatonModel assertNotEndsWith(AutomatonModel notEndingModel);

    public abstract AutomatonModel assertNotEquals(AutomatonModel notEqualModel);

    public abstract AutomatonModel assertNotEqualsIgnoreCase(AutomatonModel notEqualModel);

    public abstract AutomatonModel assertNotStartsOther(AutomatonModel notStartingModel);

    public abstract AutomatonModel assertNotStartsWith(AutomatonModel notStartsModel);

    public abstract AutomatonModel assertStartsOther(AutomatonModel startingModel);

    public abstract AutomatonModel assertStartsWith(AutomatonModel startingModel);

    public abstract AutomatonModel concatenate(AutomatonModel arg);

    public abstract boolean containsString(String actualValue);

    public abstract AutomatonModel delete(int start, int end);

    public abstract boolean equals(AutomatonModel arg);

    public abstract AutomatonModel intersect(AutomatonModel arg);

    public abstract AutomatonModel insert(int offset, AutomatonModel argModel);

    public abstract BigInteger modelCount();

    public abstract AutomatonModel replace(char find, char replace);

    public abstract AutomatonModel replace(String find, String replace);

    public abstract AutomatonModel replaceChar();

    public abstract AutomatonModel replaceFindKnown(char find);

    public abstract AutomatonModel replaceReplaceKnown(char replace);

    public abstract AutomatonModel reverse();

    public abstract AutomatonModel substring(int start, int end);

    public abstract AutomatonModel setCharAt(int offset, AutomatonModel argModel);

    public abstract AutomatonModel setLength(int length);

    public abstract AutomatonModel suffix(int start);

    public abstract AutomatonModel toLowercase();

    public abstract AutomatonModel toUppercase();

    public abstract AutomatonModel trim();

    public abstract AutomatonModel clone();
}
