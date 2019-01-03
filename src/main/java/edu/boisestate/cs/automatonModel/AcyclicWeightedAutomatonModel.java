package edu.boisestate.cs.automatonModel;

import java.io.File;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.math3.fraction.Fraction;

import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automaton.acyclic.WeightedState;
import edu.boisestate.cs.automaton.acyclic.WeightedTransition;
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
		DotToGraph.outputDotFile(automaton.toDot(), "containedInOtherRet");
		return new AcyclicWeightedAutomatonModel(automaton.clone(), alphabet, boundLength);
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
		System.out.println("Here 1 ");
		//DotToGraph.outputDotFile(automaton.toDot(), "this");
		automaton.normalize();
		//DotToGraph.outputDotFile(automaton.toDot(), "containsInBase");
		
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
		x = x.flatten();
		//DotToGraph.outputDotFile(x.toDot(), "containsInArg");
		//System.out.println("Here 2 ");
		AcyclicWeightedAutomaton ret = automaton.intersection(x);
		//System.out.println("Here 3 ");
		//System.out.println("Ret: " + ret);
		//System.out.println(ret.toDot());
		DotToGraph.outputDotFile(ret.toDot(), "RET");
		//System.out.println("Here 4 ");
		ret.determinize();
		//System.out.println("Here 5 ");
		ret.normalize();
		DotToGraph.outputDotFile(ret.toDot(), "containsOtherRet");
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
	public AcyclicWeightedAutomatonModel assertNotContainsOther(AcyclicWeightedAutomatonModel notContainingModel) {
		//create the set minus of this and notContainingModel
		//1. complete nonContainingModel
		AcyclicWeightedAutomaton notContaining = notContainingModel.automaton.flatten();
		//up to the maxLenght of notContaining should work since we will surround it
		//with  .*
		//System.out.println("ABC " + alphabet.getCharSet() + " length " + notContaining.getMaxLenght());
		notContaining = notContaining.complete(notContaining.getMaxLenght(), alphabet.getCharSet());
		//notContaining.determinize();
		//DotToGraph.outputDotFile(notContaining.toDot(), "notContainsOtherArg1");
		
		int maxLength = automaton.getMaxLenght();
		//DotToGraph.outputDotFile(automaton.toDot(), "notContainsOtherBase");
		
		//2. concatenate it with .* on both sides
		AcyclicWeightedAutomaton prefix = BasicAcyclicWeightedAutomaton.makeCharSet(alphabet.getCharSet()).repeat(0, maxLength);
		prefix.determinize();
		AcyclicWeightedAutomaton suffix = BasicAcyclicWeightedAutomaton.makeCharSet(alphabet.getCharSet()).repeat(0, maxLength);
		suffix.determinize();
		notContaining = prefix.concatenate(notContaining).concatenate(suffix);
		notContaining.determinize();
		//DotToGraph.outputDotFile(notContaining.toDot(), "notContainsInArg2");
		AcyclicWeightedAutomaton ret = automaton.minus(notContaining);
		DotToGraph.outputDotFile(ret.toDot(), "notContainsOtherRet");
		return new AcyclicWeightedAutomatonModel(ret, alphabet, boundLength);
	}

	@Override
	public AcyclicWeightedAutomatonModel assertNotContainedInOther(AcyclicWeightedAutomatonModel notContainedModel) {
		// TODO Auto-generated method stub
		// eas: as of 12-26-18 returns as is
		DotToGraph.outputDotFile(automaton.toDot(), "notContainedInOtherRet");
		return new AcyclicWeightedAutomatonModel(automaton.clone(), alphabet, boundLength);
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
		/*
		 * Removes the characters in a substring of this sequence. 
		 * The substring begins at the specified start and extends 
		 * to the character at index end - 1 or to the end of the 
		 * sequence if no such character exists. 
		 * If start is equal to end, no changes are made.
		 * 
		 * StringIndexOutOfBoundsException - if start is negative, 
		 * greater than length(), or greater than end.
		 */
		AcyclicWeightedAutomaton res = null;
		
		automaton.determinize();
		automaton.normalize();
		if(start < 0 || start > end || automaton.isEmpty() || start > automaton.getMaxLenght()){
			res = BasicAcyclicWeightedAutomaton.makeEmpty();
		} else  if( start == end){
			//return automaton with string with
			//greater or equal length than start
			res = automaton.clone();
				WeightedState nextState = res.getInitialState();
				if(start > 0){
					//make nextState nonfinal since the empty string would throw an
					//exception
					nextState.setAccept(false);
				}
				//start - 2, the actual indices of the strings which toStates
				//should be set to non-final
				removeDFS(nextState, start - 2, 0);
			
		} else {
			//we will do dfs algorithm since we also need to count
			//the weight of each "removed" substring
			res = automaton.clone();
			DotToGraph.outputDotFile(res.toDot(), "origRes");
			//System.out.println("RES " + res);
			WeightedState nextState = res.getInitialState();
			int index = 0;//starting index
			WeightedState connectFromState = null;//state to create epsilon transitions from
			//the weight of the given path, start as 1
			Fraction weight = new Fraction(1,1);
			deleteDFS1(nextState, start, index, end-1, connectFromState, weight);
		}
		
		System.out.println("DELETE " + start + "\t" + end);
		DotToGraph.outputDotFile(automaton.toDot(), "orig");
		DotToGraph.outputDotFile(res.toDot(), "deletedOrig");
		res.determinize();
		res.normalize();
		DotToGraph.outputDotFile(res.toDot(), "deleted");
		//stop for now
//		if(start == 1 &&  end == 2){
//			System.exit(2);
//		}
		return new AcyclicWeightedAutomatonModel(res, this.alphabet);
	}
	
	private void removeDFS(WeightedState currState, int depth, int index) {
		if(depth >= index){
			Set<WeightedTransition> tr = new HashSet<WeightedTransition>();
			tr.addAll(currState.getTransitions());
			for(WeightedTransition t : tr){
				WeightedState toState = t.getToState();
				if(toState.isAccept()){
					//create an non-final version of it
					WeightedState copy = new WeightedState();
					//add the transitions to it
					for(WeightedTransition tTo : toState.getTransitions()){
						copy.addTransition(new WeightedTransition(copy, tTo.getSymb(),tTo.getToState(), tTo.getWeight()));
					}
					//update the transition
					t.setToState(copy);
					toState = copy;
				} // end if toState accept
				//call removeDFS on it again
				removeDFS(toState, depth, index+1);
			}
		}
		
	}

	private void deleteDFS1(WeightedState currState, int start, int index, int end, WeightedState connectFromState,
			Fraction weight) {
		//System.out.println("CurrState " + currState);
		//System.out.println("start " + start + "\tindex " + index + "\t end " + end + "\tconncectFrom " + connectFromState);
		if(start > index){
			//have not reached the point where we need to remove the states
			//thus create a non-final copy of a toState and explore it further, increment index
			Set<WeightedTransition> tr = new HashSet<WeightedTransition>();
			tr.addAll(currState.getTransitions());
			//further optimizaition create a copyState per set of 
			//transitions with the same toState
			Map<WeightedState, Set<WeightedTransition>> copySet = new HashMap<WeightedState, Set<WeightedTransition>>();
			for(WeightedTransition t : tr){
				WeightedState toState = t.getToState();
				Set<WeightedTransition> trSet = null;
				if(copySet.containsKey(toState)){
					trSet = copySet.get(toState);
				} else {
					trSet = new HashSet<WeightedTransition>();
					copySet.put(toState, trSet);
				}
				//add the transition to it
				trSet.add(t);
				
			}
			//now create a new nonfinal state for each set
			for(Entry<WeightedState, Set<WeightedTransition>> sTr : copySet.entrySet()){
				WeightedState tempState = new WeightedState();
				//however if the next index is actually start then conserve the acceptance
				if(index + 1 == end){
					tempState.setAccept(sTr.getKey().isAccept());
				}
				//add the transitions of toState
				for(WeightedTransition tOld : sTr.getKey().getTransitions()){
					tempState.addTransition(new WeightedTransition(tempState, tOld.getSymb(), tOld.getToState(), tOld.getWeight()));
				}
				//update the transitions for the currentState
				for(WeightedTransition tCurr : sTr.getValue()){
					//change toState toCurr
					tCurr.setToState(tempState);
				}
				
				//now call DFS on tempState, and increment the index count, i.e., the depth
				deleteDFS1(tempState, start, index + 1, end, connectFromState, weight);
			}
		} else if (start == index && index != end){
			//reached the point where we need to start skipping the states
			connectFromState = currState;
			//the case when the currentState is the staring state
			//but its transition is not the ending state
			Set<WeightedTransition> tr = new HashSet<WeightedTransition>();
			tr.addAll(currState.getTransitions());
			for(WeightedTransition t : tr){
				WeightedState toState = t.getToState();
				//remove the current outgoing transition from conncetFromState
				connectFromState.getTransitions().remove(t);
				//if toState is accepting then count as a middle transition
				if(toState.isAccept()){
					WeightedState tempState = new WeightedState();
					tempState.setAccept(true);
					tempState.setWeight(toState.getWeight());
					connectFromState.addEpsilonTransition(automaton.getIncoming(connectFromState), tempState, weight.multiply(t.getWeight()));
					//System.out.println("ConnectFromUpdatedMiddle " + connectFromState);
				}
				
				//just call on toState with updated weight
				//if the skipped state happens to be final do not include its weight into
				//the calculation - those are "short" strings, just perform the weight update
				//base on the weight of the edges
				deleteDFS1(toState, start, index+1, end, connectFromState, weight.multiply(t.getWeight()));
			}
			
		} else if (start == index && index == end){
			//delete the transition between the currentState and toState and
			//add epsilon weighted transition between them
			Set<WeightedTransition> tr = new HashSet<WeightedTransition>();
			tr.addAll(currState.getTransitions());
			for(WeightedTransition t : tr){
				WeightedState toState = t.getToState();
				currState.removeTransition(t);
				currState.addEpsilonTransition(automaton.getIncoming(currState), toState, weight.multiply(t.getWeight()));
			}
			
		} else if (start < index && index < end){
			//in between
			Set<WeightedTransition> tr = new HashSet<WeightedTransition>();
			tr.addAll(currState.getTransitions());
			for(WeightedTransition t : tr){
				WeightedState toState = t.getToState();
				//the case for shorter strings that do have valid start but shorter than end
				//we need to create an epsilon transition to a new final state with the same weight
				//and no outgoing edges
				if(toState.isAccept()){
					WeightedState tempState = new WeightedState();
					tempState.setAccept(true);
					tempState.setWeight(toState.getWeight());
					connectFromState.addEpsilonTransition(automaton.getIncoming(connectFromState), tempState, weight.multiply(t.getWeight()));
				}
				//System.out.println("ConnectFromUpdatedMiddle " + connectFromState);
				deleteDFS1(toState, start, index+1, end, connectFromState, weight.multiply(t.getWeight()));
			}
			
		} else if (start != index && index == end){
			//the case when we found the last transitions
			//all toStates should be epsilon connected with connectFromState
			Set<WeightedTransition> tr = new HashSet<WeightedTransition>();
			tr.addAll(currState.getTransitions());
			for(WeightedTransition t : tr){
				WeightedState toState = t.getToState();
				connectFromState.addEpsilonTransition(automaton.getIncoming(connectFromState), toState, weight.multiply(t.getWeight()));
				//System.out.println("ConnectFromUpdatedEnd " + connectFromState);
			}
		}
		//in all other cases return back
	}

	private void deleteDFS(WeightedState currState, int start, int index, int end, WeightedState connectFromState,
			Fraction weight) {
		//System.out.println("CurrState " + currState);
		//System.out.println("start " + start + "\tindex " + index + "\t end " + end + "\tconncectFrom " + connectFromState);
		Set<WeightedTransition> tr = new HashSet<WeightedTransition>();
		tr.addAll(currState.getTransitions());
		for(WeightedTransition t : tr){ 
			
			WeightedState toState  = t.getToState();
			//System.out.println("toState " + toState);
			if(start > index){
				//have not reached the point where we need to remove the states
				//thus create a non-final copy of a toState and explore it further, increment index
				WeightedState tempState = new WeightedState();
				for(WeightedTransition tTo : toState.getTransitions()){
					//create a copy of transitions
					tempState.addTransition(new WeightedTransition(tempState, tTo.getSymb(), tTo.getToState(), tTo.getWeight()));
				}
				//remove the current transition for currState
				currState.getTransitions().remove(t);
				//add the transition to tempState
				currState.addTransition(new WeightedTransition(currState, t.getSymb(), tempState, t.getWeight()));
				//class DFS
				deleteDFS(tempState, start, index + 1, end, connectFromState, weight);
			} else if (start == index){
				//reached the point where we need to start skipping the states
				connectFromState = currState;
				//remove the current outgoing transition from conncetFromState
				connectFromState.getTransitions().remove(t);
				
				//just call on toState with updated weight
				//if the skipped state happens to be final do not include its weight into
				//the calculation - those are "short" strings, just perform the weight update
				//base on the weight of the edges
				deleteDFS(toState, start, index+1, end, connectFromState, weight.multiply(t.getWeight()));
				
				//the case for shorter strings that do have valid start but shorted than end
				//we need to create an epsilon transition to a new final state with the same weight
				//and no outgoing edges
				if(index != end && toState.isAccept()){ //not the last state since we connect it differently
//					//make connectFrom as accept
//					connectFromState.setAccept(true);
//					//update the weight
//					connectFromState.setWeight(connectFromState.getWeight().add(weight.multiply(t.getWeight()).multiply(toState.getWeight())));
					WeightedState tempState = new WeightedState();
					tempState.setAccept(true);
					tempState.setWeight(toState.getWeight());
//					System.out.println("Adding epsilon "+ connectFromState);
					connectFromState.addEpsilonTransition(automaton.getIncoming(connectFromState), tempState, weight.multiply(t.getWeight()));
//					System.out.println("Added epsilon "+ connectFromState);
				}
			} else if (start < index && index < end){

				
				//do the same us previous case just don't update conncetFromState value or remove transitions
				deleteDFS(toState, start, index+1, end, connectFromState, weight.multiply(t.getWeight()));
				
				//the case for shorter strings that do have valid start but shorted than end
				//we need to create an epsilon transition to a new final state with the same weight
				//and no outgoing edges
				if(toState.isAccept()){
//					//make connectFrom as accept
//					connectFromState.setAccept(true);
//					//update the weight
//					connectFromState.setWeight(connectFromState.getWeight().add(weight.multiply(t.getWeight()).multiply(toState.getWeight())));
					WeightedState tempState = new WeightedState();
					tempState.setAccept(true);
					tempState.setWeight(toState.getWeight());
					connectFromState.addEpsilonTransition(automaton.getIncoming(connectFromState), tempState, weight.multiply(t.getWeight()));
				}
			} 
			
			//check separately from other conditions
			//the condition for string of length that contain the entire substring
			if (index == end){
				//System.out.println("connectFrom " + connectFromState+ " \n toState " + toState + " w " + weight);
				
				//do not call recursion on toState, instead create and epsilon transition with the given weight
				connectFromState.addEpsilonTransition(automaton.getIncoming(connectFromState), toState, weight);
			}
		}//end for each transition
	

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
		
		AcyclicWeightedAutomaton res = automaton.clone();
		res.determinize();
		DotToGraph.outputDotFile(res.toDot(), "replaceBefore");
		//find all transitions with that char
		//remove from fromState and add it back with the updated 
		//char
		for(WeightedState s : res.getStates()){
			Set<WeightedTransition> transSet = new HashSet<WeightedTransition>();
			transSet.addAll(s.getTransitions());
			for(WeightedTransition wt : transSet){
				if(wt.getSymb() == find){
					s.removeTransition(wt);
					wt.setSybmol(replace);
					s.addTransition(wt);
				}
			}
		}
		DotToGraph.outputDotFile(res.toDot(), "replaceAfter");
		res.determinize();
		//res.normalize();
		DotToGraph.outputDotFile(res.toDot(), "replaceAfterDet");
		System.exit(2);
		return new AcyclicWeightedAutomatonModel(res, alphabet);
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
