package edu.boisestate.cs.automaton.acyclic;

import java.util.Set;

/**
 * This class provides static methods to perform
 * some basic instantiations of <code>AcyclicWeightedAutomaton</code>
 * 
 * @author elenasherman
 *
 */
final public class BasicAcyclicWeightedAutomaton {
	
	private BasicAcyclicWeightedAutomaton(){};
	
	public static AcyclicWeightedAutomaton makeAnyString(){
		AcyclicWeightedAutomaton a = new AcyclicWeightedAutomaton();
		//TODO: create an automaton.
		return a;
	}
	
	/**
	 * Return a new (deterministic) automaton that accepts a single character
	 * in the given set.
	 * @param set
	 * @return
	 */
	public static AcyclicWeightedAutomaton makeCharSet(String set){
		AcyclicWeightedAutomaton ret = new AcyclicWeightedAutomaton();
		WeightedState s1 = new WeightedState();
		WeightedState s2 = new WeightedState();
		ret.initial = s1;
		//add the transitions from s1 to s2
		s2.setAccept(true);
		//the weight should be default one 1/1
		Set<WeightedTransition> s1Trans = s1.getTransitions();
		for(int i=0; i < set.length(); i++){
			s1Trans.add(new WeightedTransition(s1,set.charAt(i), s2));
		}
		return ret;
	}

	/**
	 * Creates an automaton with the language containing the empty string only.
	 * @return
	 */
	public static AcyclicWeightedAutomaton makeEmptyString() {
		AcyclicWeightedAutomaton ret = new AcyclicWeightedAutomaton();
		WeightedState s1 = new WeightedState();
		s1.setAccept(true);
		ret.initial = s1;
		return ret;
	}

	/**
	 * Creates an automaton with the empty language
	 * @return
	 */
	public static AcyclicWeightedAutomaton makeEmpty() {
		AcyclicWeightedAutomaton ret = new AcyclicWeightedAutomaton();
		WeightedState s1 = new WeightedState();
		ret.initial = s1;
		return ret;
	}

	/**
	 * Creates an automaton for the given string, all weights are set to 1/1.
	 * In other implementations it stored as string, i.e., singleton
	 * Here we convert everything to an automaton.
	 * @param string
	 * @return
	 */
	public static AcyclicWeightedAutomaton makeString(String string) {
		AcyclicWeightedAutomaton ret = new AcyclicWeightedAutomaton();
		//connect states with chars from string
		WeightedState s1 = new WeightedState();
		ret.initial = s1;
		for(int i=0; i < string.length(); i++){
			WeightedState s2 = new WeightedState();
			Set<WeightedTransition> s1Trans = s1.getTransitions();
			s1Trans.add(new WeightedTransition(s1,string.charAt(i), s2));
			s1 = s2;
		}
		//the last state should be the final state
		s1.setAccept(true);
		
		return ret;
	}

}
