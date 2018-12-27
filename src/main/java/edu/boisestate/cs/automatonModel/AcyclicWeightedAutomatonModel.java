package edu.boisestate.cs.automatonModel;

import java.io.File;
import java.math.BigInteger;
import java.util.Set;

import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automaton.WeightedState;
import edu.boisestate.cs.automaton.WeightedTransition;
import edu.boisestate.cs.automaton.acyclic.AcyclicWeightedAutomaton;
import edu.boisestate.cs.automaton.acyclic.BasicAcyclicWeightedAutomaton;
import edu.boisestate.cs.util.DotToGraph;

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
	     //this has substring of other
		AcyclicWeightedAutomaton other = containingModel.automaton;
		//eas: as of 12-26-18, return unchanged
		return new AcyclicWeightedAutomatonModel(automaton, alphabet, boundLength);
	}



	@Override
	public AcyclicWeightedAutomatonModel assertContainsOther(AcyclicWeightedAutomatonModel containedModel) {
		//a common approach is to surround containedModel with all strings and then 
		//take an intersection of this with that model
		//here we limit to weighted automata with finite language, thus cannot append any string
		//instead we surround with weighted automata with strings up to that max length
		//of this automaton
		//step 1 find max (or we can use number of states + 1 value, which might create larger automata)
		int maxLength = this.automaton.getMaxLenght();
		automaton.determinize();
		//automaton.normalize();
		//System.out.println("This: " + automaton);
		//DotToGraph.outputDotFile(automaton.toDot(), "this");
		automaton.normalize();
		//DotToGraph.outputDotFile(automaton.toDot(), "thisN");
		
		//System.out.println("Max L " + maxLength);
		AcyclicWeightedAutomaton prefix = BasicAcyclicWeightedAutomaton.makeCharSet(alphabet.getCharSet()).repeat(0, maxLength);
		prefix.determinize();
		//prefix.normalize();
		//System.out.println("Prefix\n" + prefix);
		//DotToGraph.outputDotFile(prefix.toDot(), "prefix");
		AcyclicWeightedAutomaton suffix = BasicAcyclicWeightedAutomaton.makeCharSet(alphabet.getCharSet()).repeat(0, maxLength);
		suffix.determinize();
		//suffix.normalize();
		//System.out.println("Suffix\n" + suffix);
		//DotToGraph.outputDotFile(suffix.toDot(), "suffix");
		AcyclicWeightedAutomaton contained = containedModel.automaton;
		contained.determinize();
		//contained.normalize();
		//System.out.println("Other: " + contained);
		//DotToGraph.outputDotFile(contained.toDot(), "other");;
		AcyclicWeightedAutomaton x = prefix.concatenate(contained);
		x.determinize();
		//x.normalize();
		//System.out.println("X1: " + x);
		//DotToGraph.outputDotFile(x.toDot(), "X1");
		x = x.concatenate(suffix);
		//System.out.println("X2: " + x);
		//DotToGraph.outputDotFile(x.toDot(), "X2");
		x.determinize();
		//x.normalize();
		//System.out.println("X2D: " + x);
		//DotToGraph.outputDotFile(x.toDot(), "X2D");
		x.normalize();
		//DotToGraph.outputDotFile(x.toDot(), "X2DN");
		//for a just a single string we can remove the weights
		x.flatten();
		AcyclicWeightedAutomaton ret = this.automaton.intersection(x);
		//System.out.println("Ret: " + ret);
		//DotToGraph.outputDotFile(ret.toDot(), "RET");
		ret.determinize();
		ret.normalize();
		//DotToGraph.outputDotFile(ret.toDot(), "RET_DN");
		//System.out.println("Ret: " + ret);
		//System.exit(2);
		return new AcyclicWeightedAutomatonModel(ret, alphabet, boundLength);
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
		//create the set minus of this and notContainingModel
		//1. complete nonContainingModel
		AcyclicWeightedAutomaton notContaining = notContainingModel.automaton;
		notContaining.complete(automaton.getMaxLenght(), alphabet.getCharSet());
		//2.
		
		this.automaton.
		return new AcyclicWeightedAutomatonModel(automaton, alphabet, boundLength);
	}

	@Override
	public AcyclicWeightedAutomatonModel assertNotContainsOther(AcyclicWeightedAutomatonModel notContainedModel) {
		// TODO Auto-generated method stub
		// eas: as of 12-26-18 returns as is
		return new AcyclicWeightedAutomatonModel(automaton, alphabet, boundLength);
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
		AcyclicWeightedAutomaton res = this.automaton.concatenate(arg.automaton);
		//res.minimize();
		return new AcyclicWeightedAutomatonModel(res, this.alphabet);
	}

	@Override
	public boolean containsString(String actualValue) {
		
		return automaton.run(actualValue);
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
		return automaton.getStringCount();
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

	@Override
	public String getAutomaton() {
		return automaton.toString();
	}
	

}
