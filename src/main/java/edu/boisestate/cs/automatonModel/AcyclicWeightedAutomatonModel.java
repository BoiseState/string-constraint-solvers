package edu.boisestate.cs.automatonModel;

import java.math.BigInteger;
import java.util.Set;

import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automaton.acyclic.AcyclicWeightedAutomaton;

public class AcyclicWeightedAutomatonModel extends AutomatonModel<AcyclicWeightedAutomatonModel>{
	
	private AcyclicWeightedAutomaton automaton;

	protected AcyclicWeightedAutomatonModel(AcyclicWeightedAutomaton automaton, 
			Alphabet alphabet, int initialBoundLength) {
		super(alphabet, initialBoundLength);
		this.automaton = automaton;
	}
	
	protected AcyclicWeightedAutomatonModel(AcyclicWeightedAutomaton automaton, 
			Alphabet alphabet) {
		super(alphabet, 0);
		this.automaton = automaton;
	}

	@Override
	public String getAcceptedStringExample() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getFiniteStrings() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSingleton() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AutomatonModel assertContainedInOther(AutomatonModel containingModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AutomatonModel assertContainsOther(AutomatonModel containedModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AutomatonModel assertEmpty() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AutomatonModel assertEndsOther(AutomatonModel baseModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AutomatonModel assertEndsWith(AutomatonModel endingModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AutomatonModel assertEquals(AutomatonModel equalModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AutomatonModel assertEqualsIgnoreCase(AutomatonModel equalModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AutomatonModel assertHasLength(int min, int max) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AutomatonModel assertNotContainedInOther(AutomatonModel notContainingModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AutomatonModel assertNotContainsOther(AutomatonModel notContainedModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AutomatonModel assertNotEmpty() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AutomatonModel assertNotEndsOther(AutomatonModel notEndingModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AutomatonModel assertNotEndsWith(AutomatonModel notEndingModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AutomatonModel assertNotEquals(AutomatonModel notEqualModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AutomatonModel assertNotEqualsIgnoreCase(AutomatonModel notEqualModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AutomatonModel assertNotStartsOther(AutomatonModel notStartingModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AutomatonModel assertNotStartsWith(AutomatonModel notStartsModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AutomatonModel assertStartsOther(AutomatonModel startingModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AutomatonModel assertStartsWith(AutomatonModel startingModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel concatenate(AcyclicWeightedAutomatonModel arg) {
		
		return null;
	}

	@Override
	public boolean containsString(String actualValue) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AutomatonModel delete(int start, int end) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean equals(AutomatonModel arg) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AutomatonModel intersect(AutomatonModel arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AutomatonModel insert(int offset, AutomatonModel argModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BigInteger modelCount() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AutomatonModel replace(char find, char replace) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AutomatonModel replace(String find, String replace) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AutomatonModel replaceChar() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AutomatonModel replaceFindKnown(char find) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AutomatonModel replaceReplaceKnown(char replace) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AutomatonModel reverse() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AutomatonModel substring(int start, int end) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AutomatonModel setCharAt(int offset, AutomatonModel argModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AutomatonModel setLength(int length) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AutomatonModel suffix(int start) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AutomatonModel toLowercase() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AutomatonModel toUppercase() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AutomatonModel trim() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AutomatonModel clone() {
		// TODO Auto-generated method stub
		return null;
	}

}
