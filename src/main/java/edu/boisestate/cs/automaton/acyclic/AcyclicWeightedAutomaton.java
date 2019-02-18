package edu.boisestate.cs.automaton.acyclic;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.math3.fraction.Fraction;

import edu.boisestate.cs.util.DotToGraph;

public class AcyclicWeightedAutomaton implements Serializable, Cloneable{
	
	WeightedState initial;
	
	public AcyclicWeightedAutomaton(){
		
	}
	
	public void setInitialState(WeightedState initState){
		initial = initState;
	}
	
	public WeightedState getInitialState(){
		return initial;
	}
	
	/**
	 * Returns the set of states that are reachable from
	 * the initial state
	 * Implemented as in dk.brics.automaton
	 * @return set of WeightedSate objects
	 */
	public Set<WeightedState> getStates(){
		//do the traversion from the initial state;
		Set<WeightedState> visited = new HashSet<WeightedState>();
		
		LinkedList<WeightedState> worklist = new LinkedList<WeightedState>();
		worklist.add(initial);
		visited.add(initial);
		while(worklist.size() > 0){
			WeightedState s = worklist.removeFirst();
			Set<WeightedTransition> tr = s.getTransitions();
			for(WeightedTransition t : tr){
				if(!visited.contains(t.getToState())){
					visited.add(t.getToState());
					worklist.add(t.getToState());
				}
			}
		}
		
		return visited;
	}
	
	public Set<WeightedState> getAcceptStates(){
		Set<WeightedState> accepts = new HashSet<WeightedState>();
		Set<WeightedState> visited = new HashSet<WeightedState>();
		LinkedList<WeightedState> worklist = new LinkedList<WeightedState>();
		worklist.add(initial);
		visited.add(initial);
		while(worklist.size() > 0){
			WeightedState s = worklist.removeFirst();
			if(s.isAccept()){
				accepts.add(s);
			}
			for(WeightedTransition t : s.getTransitions()){
				WeightedState toState = t.getToState();
				if(!visited.contains(toState)){
					worklist.add(toState);
				}
			}
		}
		return accepts;
	}
	
	/**
	 * Concatenate this with other and return the result
	 * @param other
	 * @return
	 */
	public AcyclicWeightedAutomaton concatenate(AcyclicWeightedAutomaton other){
		return BasicAcyclicWeightedOperations.concantenate(this, other);
	}
	
	public AcyclicWeightedAutomaton union(AcyclicWeightedAutomaton other){
		return BasicAcyclicWeightedOperations.union(this, other);
	} 
	
	@Override
	public AcyclicWeightedAutomaton clone(){
		try{
			AcyclicWeightedAutomaton a = (AcyclicWeightedAutomaton) super.clone();
			//copy transitions
			HashMap<WeightedState, WeightedState> m = new HashMap<WeightedState, WeightedState>();
			Set<WeightedState> states = getStates();
			//create all necessary states first
			for(WeightedState s : states){
				WeightedState p = new WeightedState(s.getWeight(), s.isAccept());
				m.put(s, p);
				if(s.equals(initial)){
					a.setInitialState(p);
				}
			}
			//now we can create transitions between those states
			for(WeightedState s : states){
				WeightedState p = m.get(s);
				for(WeightedTransition t : s.getTransitions()){
					WeightedTransition r = new WeightedTransition(p,t.getSymb(), m.get(t.getToState()), t.getWeight());
					p.addTransition(r);
				}
			}
			return a;
			
		} catch (CloneNotSupportedException e){
			throw new RuntimeException(e);
		}
	}
	
	

	/**
     * Returns a string representation of this automaton.
     */
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        
            Set<WeightedState> states = getStates();
            setStateNumbers(states);
            b.append("initial state: ")
             .append(initial.getNumber())
             .append("\n");
            for (WeightedState s : states) {
                b.append(s.toString());
            }
        return b.toString();
    }
    
    /**
     * Assigns consecutive numbers to the given states
     * @param states
     */
    static void setStateNumbers(Set<WeightedState> states){
    	int number = 0;
    	for(WeightedState s : states){
    		s.setNumber(number++);
    	}
    }
    
    /**
    * Returns <a href="http://www.research.att.com/sw/tools/graphviz/"
    * target="_top">Graphviz Dot</a> representation of this automaton.
    */
   public String toDot() {
       StringBuilder b = new StringBuilder("digraph Automaton {\n");
       b.append("  rankdir = LR;\n");
       Set<WeightedState> states = getStates();
       setStateNumbers(states);
       for (WeightedState s : states) {
           b.append("  ").append(s.getNumber());
           if (s.isAccept()) {
               b.append(" [shape=doubleoctagon");
           } else {
               b.append(" [shape=ellipse");
           }
           b.append(",label=\"").append(s.getNumber()).append(",").append(s.getWeight()).append("\"];\n");
           if (s == initial) {
               b.append("  initial [shape=plaintext,label=\"");
               b.append("\"];\n");
               b.append("  initial -> ").append(s.getNumber()).append("\n");
           }
           for (WeightedTransition t : s.getTransitions()) {
               b.append("  ").append(s.getNumber());
               t.appendDot(b);
           }
       }
       return b.append("}\n").toString();
   }
    
    
    
    public void determinize(){
    	BasicAcyclicWeightedOperations.determinize(this);
    }
    
    public void normalize(){
    	BasicAcyclicWeightedOperations.normalize(this);
    }
    
    public void minimize(){
    	//make sure it normalized first
    	normalize();
    	BasicAcyclicWeightedOperations.minimize(this);
    }

    /**
     * Computes incoming edges of state s
     * @param s
     * @return
     */
	public Set<WeightedTransition> getIncoming(WeightedState s) {
		Set<WeightedTransition> tr = new HashSet<WeightedTransition>();
		for(WeightedState curr : getStates()){
			for(WeightedTransition t : curr.getTransitions()){
				if(t.getToState().equals(s)){
					tr.add(t);
				}
			}
		}
		return tr;
	}

	/**
	 * Creates an automaton that repeats this one from min to max
	 * @param min
	 * @param max
	 * @return
	 */
	public AcyclicWeightedAutomaton repeat(int min, int max) {
		
		return BasicAcyclicWeightedOperations.repeat(this, min, max);
	}

	/**
	 * Computes the intersection of two weighted automata
	 * @param automaton
	 * @return
	 */
	public AcyclicWeightedAutomaton intersection(AcyclicWeightedAutomaton a) {
		return BasicAcyclicWeightedOperations.intersection(this, a);
	}

	public boolean isEmpty() {
		//find at least one path from the start
		return !findPathFrom(initial);
		//return BasicAcyclicWeightedOperations.isEmpty(this);
	}
	
	private boolean findPathFrom(WeightedState curr){
		boolean ret = false;
		if(curr.isAccept()){
			ret = true;
		} else {
			for(WeightedTransition w : curr.getTransitions()){
				ret = findPathFrom(w.getToState());
				if(ret){ //found at least one path
					break;
				}
			}
		}
		return ret;
	}

	public boolean run(String s) {
		// TODO Auto-generated method stub
		return BasicAcyclicWeightedOperations.run(this, s);
	}

	/**
	 * returns the max length of the accepted string.
	 */
	public int getMaxLenght() {
		return traverseMaxLenght(initial,-1, -1);
	}
	
	private int traverseMaxLenght(WeightedState curr, int currentMax, int currentLength){
		currentLength++;
		if(curr.isAccept()){
			currentMax = currentLength;
		}
		for(WeightedTransition w : curr.getTransitions()){
			int ret = traverseMaxLenght(w.getToState(), currentMax, currentLength);
			if(ret > currentMax){
				currentMax = ret;
			}
		}
		return currentMax;
	}
	
	
	public BigInteger getStringCount(){
		//currPrefixes are set to 1 due to the empty string
		//System.out.println(this);
		Fraction count = countStrings(initial, new Fraction(1));
		//System.out.println(count);
		return BigInteger.valueOf(count.longValue());
	}
	
	public BigInteger getStringCountFromState(WeightedState from){
		//currPrefixes are set to 1 due to the empty string
		//System.out.println(this);
		Fraction count = countStrings(from, new Fraction(1));
		//System.out.println(count);
		return BigInteger.valueOf(count.longValue());
	}
	
	private Fraction countStrings(WeightedState curr, Fraction currPrefixes){
		try {
		Fraction currCount = new Fraction(0);
		//System.out.println("In " + curr + " " + currCount + " " + currPrefixes);
		if(curr.isAccept()){
			currCount = currCount.add(currPrefixes.multiply(curr.getWeight()));
		}
		//figure out which are going to the same state and thus just multiply
		//by the sum of their weights
		HashMap<WeightedState, Fraction> theSameState = new HashMap<WeightedState, Fraction>();
		for(WeightedTransition w : curr.getTransitions()){
			WeightedState toState = w.getToState();
			Fraction updatedWeight = w.getWeight();
			if(theSameState.containsKey(toState)){
				updatedWeight = theSameState.get(toState).add(updatedWeight);
				
			}
				theSameState.put(toState, updatedWeight);
		}
		//now iterate over the entry and explore properly
		for(Entry<WeightedState, Fraction> toState : theSameState.entrySet()){
			
			currCount = currCount.add(countStrings(toState.getKey(), currPrefixes.multiply(toState.getValue())));
			//currCount = currCount.add(countStrings(w.getToState(), currPrefixes.multiply(w.getWeight())))
		}
		return currCount;
		} catch (Exception e){
			return  new Fraction(Integer.MAX_VALUE);
		}
		
		//System.out.println("count " + currCount);
	}

	/**
	 * Removes weights from transitions and final states.
	 */
	public AcyclicWeightedAutomaton flatten() {
		return BasicAcyclicWeightedOperations.flatten(this);
	}

	/**
	 * Converts to an equivalent automata where
	 * each state has transition on each symbol
	 * of the alphabet up to the given bound.
	 */
	public AcyclicWeightedAutomaton complete(int bound, String symbols) {
		
		return BasicAcyclicWeightedOperations.complete(this, bound, symbols);	
	}
	
	
	
	public AcyclicWeightedAutomaton complement(){
		return BasicAcyclicWeightedOperations.complement(this);
	}

	public AcyclicWeightedAutomaton minus(AcyclicWeightedAutomaton a) {
		return BasicAcyclicWeightedOperations.minus(this, a);
	}


	
	
}
