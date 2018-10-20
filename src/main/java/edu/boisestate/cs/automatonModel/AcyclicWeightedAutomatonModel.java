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
	public AcyclicWeightedAutomatonModel assertContainedInOther(AcyclicWeightedAutomatonModel containingModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel assertContainsOther(AcyclicWeightedAutomatonModel containedModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel assertEmpty() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel assertEndsOther(AcyclicWeightedAutomatonModel baseModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel assertEndsWith(AcyclicWeightedAutomatonModel endingModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel assertEquals(AcyclicWeightedAutomatonModel equalModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel assertEqualsIgnoreCase(AcyclicWeightedAutomatonModel equalModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel assertHasLength(int min, int max) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel assertNotContainedInOther(AcyclicWeightedAutomatonModel notContainingModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel assertNotContainsOther(AcyclicWeightedAutomatonModel notContainedModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel assertNotEmpty() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel assertNotEndsOther(AcyclicWeightedAutomatonModel notEndingModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel assertNotEndsWith(AcyclicWeightedAutomatonModel notEndingModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel assertNotEquals(AcyclicWeightedAutomatonModel notEqualModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel assertNotEqualsIgnoreCase(AcyclicWeightedAutomatonModel notEqualModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel assertNotStartsOther(AcyclicWeightedAutomatonModel notStartingModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel assertNotStartsWith(AcyclicWeightedAutomatonModel notStartsModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel assertStartsOther(AcyclicWeightedAutomatonModel startingModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel assertStartsWith(AcyclicWeightedAutomatonModel startingModel) {
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
	public AcyclicWeightedAutomatonModel delete(int start, int end) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean equals(AcyclicWeightedAutomatonModel arg) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AcyclicWeightedAutomatonModel intersect(AcyclicWeightedAutomatonModel arg) {
		AcyclicWeightedAutomaton res = this.automaton.intersection(arg.automaton);
		return new AcyclicWeightedAutomatonModel(res, alphabet);
	}

	@Override
	public AcyclicWeightedAutomatonModel insert(int offset, AcyclicWeightedAutomatonModel argModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BigInteger modelCount() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel replace(char find, char replace) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel replace(String find, String replace) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel replaceChar() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel replaceFindKnown(char find) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel replaceReplaceKnown(char replace) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel reverse() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel substring(int start, int end) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel setCharAt(int offset, AcyclicWeightedAutomatonModel argModel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel setLength(int length) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel suffix(int start) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel toLowercase() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel toUppercase() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel trim() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AcyclicWeightedAutomatonModel clone() {
		// TODO Auto-generated method stub
		return null;
	}

}
