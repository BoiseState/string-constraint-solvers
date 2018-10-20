package edu.boisestate.cs.automaton.acyclic;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import edu.boisestate.cs.automaton.BasicWeightedOperations;

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
					WeightedTransition r = new WeightedTransition(t.getSymb(), m.get(t.getToState()), t.getWeight());
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
}
