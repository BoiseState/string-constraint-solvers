package edu.boisestate.cs.automaton.acyclic;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
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
					p.getTransitions().add(r);
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
		// TODO Auto-generated method stub
		return BasicAcyclicWeightedOperations.isEmpty(this);
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
	
	private Fraction countStrings(WeightedState curr, Fraction currPrefixes){
		Fraction currCount = new Fraction(0);
		//System.out.println("In " + curr + " " + currCount + " " + currPrefixes);
		if(curr.isAccept()){
			currCount = currCount.add(currPrefixes.multiply(curr.getWeight()));
		}
		for(WeightedTransition w : curr.getTransitions()){
			currCount = currCount.add(countStrings(w.getToState(), currPrefixes.multiply(w.getWeight())));
		}
		
		//System.out.println("count " + currCount);
		return currCount;
	}

	/**
	 * Removes weights from transitions and final states.
	 */
	public void flatten() {
		for(WeightedState s : getStates()){
			s.setWeight(new Fraction(1,1));
			for(WeightedTransition t : s.getTransitions()){
				t.setWeight(new Fraction(1,1));
			}
		}
		
	}

	/**
	 * Converts to an equivalent automata where
	 * each state has transition on each symbol
	 * of the alphabet up to the given bound.
	 */
	public void complete(int bound, String symbols) {
		//1. create and automaton that accepts
		//all strings up to the bound
		//create a single one
		AcyclicWeightedAutomaton compl = BasicAcyclicWeightedAutomaton.makeCharSet(symbols);
		DotToGraph.outputDotFile(compl.toDot(), "compl1");
		//create set for abc
		Set<Character> abc = new HashSet<Character>();
		for(WeightedTransition t : compl.initial.getTransitions()){
			abc.add(t.getSymb());
		}
		//repeat it bound times.
		compl = compl.repeat(0, bound);
		DotToGraph.outputDotFile(compl.toDot(), "compl2");
		//2. negate it
		compl.complement();
		DotToGraph.outputDotFile(compl.toDot(), "compl3");
		//3. create a map that for each depth of compl records its state
		Map<Integer, WeightedState> depthState = new HashMap<Integer, WeightedState>();
		//there should only one single toState
		WeightedState next = compl.initial;
		//there will be bound+1 states, so from 0 to bound incl
		for(int depthCount = 0; depthCount < bound; depthCount++){
			depthState.put(depthCount, next);
			//there should be no exception throws since we know the depth
			next = next.getTransitions().iterator().next().getToState();
		}
		//add the last one that don't have transitions
		depthState.put(bound, next);
		//call recursive algorithm on this automaton
		//System.out.println("map " + depthState);
		completeDFS(depthState, 1, abc, initial, new HashSet<WeightedState>());
	}
	
	private void completeDFS(Map<Integer, WeightedState> depthState, int depth, Set<Character> abc, WeightedState s, Set<WeightedState> visited){
		if(!visited.contains(s)){
			//System.out.println(depth + " s " + s + " possible "  + depthState.get(depth));

			//1. get all the symbols of the outgoing transition for s
			//2. find the difference between two
			//Do it by adding all elements of the alphabet and
			//remove for which the state has transitions in place.
			Set<Character> transSet = new HashSet<Character>();
			transSet.addAll(abc);
			for(WeightedTransition wt : s.getTransitions()){
				transSet.remove(wt.getSymb());
			}

			//3. call recursively on its children at the greater depths
			depth++;
			for(WeightedTransition wt : s.getTransitions()){
				//System.out.println("Next " + depth + " abc " + abc + " " + wt.getToState());
				completeDFS(depthState, depth, abc, wt.getToState(), visited);
			}
			visited.add(s);
			//now we can modify the transition set
			//4. for the remaining symbols create the transitions to the appropriate depth state
			//if not the max state
			WeightedState toState = depthState.get(depth-1);
			if(toState != null){
				for(Character c : transSet){
					WeightedTransition t = new WeightedTransition(s, c, toState);
					s.addTransition(t);
				}
			}
		}
	}
	
	/**
	 * Creates the complement of the flatten version
	 * of this automaton. There is not definition
	 * for the complement of weighted automaton since
	 * not all semiring have negation defined.
	 * eas: 12-27-18, negation is defined on rational
	 * semiring Q, i.e., Q is a field, 
	 * but more likely how one would deal with negative
	 * weights? If we would know the "upper" limit, e.g,
	 * 100 strings of "AA" is the max, and this automaton
	 * accepts 25/1 of them, then the negated should accept 75/1 
	 * of them. Simply using the negation of -25/1 does not
	 * make any sense.
	 */
	public void complement(){
		//flatten first
		flatten();
		//negate accept to non-accept and vice versa
		for(WeightedState s : getStates()){
			if(s.isAccept()){
				s.setAccept(false);
			} else {
				s.setAccept(true);
				
			}
			
		}
	}
	
	
}
