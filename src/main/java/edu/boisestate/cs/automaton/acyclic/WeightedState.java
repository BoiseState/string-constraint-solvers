package edu.boisestate.cs.automaton.acyclic;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math3.fraction.Fraction;

/**
 * <tt>Weighted automaton</tt> state
 * Besides a name it also has a fraction
 * weight assigned to it.
 * @author elenasherman
 *
 */
public class WeightedState implements Serializable, Comparable<WeightedState>{

	/*accepting or nonaccepting state*/
	private boolean accept;
	/*set of transitions */
	private Set<WeightedTransition> transitions;
	/*state number in the automata*/
	private int number;
	/*weight of the state*/
	private Fraction w;
	/* state id */
	private int id;
	/* what id to use for the new state */
	private static int nextId;

	public WeightedState(){
		this(new Fraction(1,1), false);
	}

	public WeightedState(Fraction weight, boolean isFinal){
		resetTransitions();
		id = nextId++;
		w = weight;
		this.accept = isFinal;
	}

	final void resetTransitions(){
		transitions = new HashSet<WeightedTransition>();
	}

	/**
	 * Update the numbering of the state
	 * Note - different from id, used
	 * in displaying automaton
	 * @param number 
	 */
	public void setNumber(int newNumber){
		number = newNumber;
	}

	/**
	 * Set the weight of the state
	 * @param newWeight
	 */
	public void setWeight(Fraction newWeight){
		w = newWeight;
	}

	/**
	 * Gets the weight of the state
	 * @return
	 */
	public Fraction getWeight(){
		return w;
	}



	/** 
	 * Returns the set of outgoing transitions. 
	 * Subsequent changes are reflected in the automaton.
	 * @return transition set
	 */
	public Set<WeightedTransition> getTransitions(){
		return transitions;
	}

	/**
	 * Adding multiple e-transitions treats the update
	 * of this's weight more careful when it is a final state
	 * @param toStates
	 */
	public void addEpsilonTransitions(WeightedState ... toStates){
		//get all transitions
		Set<WeightedTransition> trans = new HashSet<WeightedTransition>();
		Fraction sum = new Fraction(0,1);
		for(WeightedState toState : toStates){
			trans.addAll(toState.getTransitions());
			if(toState.accept){
				sum = sum.add(toState.w);
			}
		}
		//iterate over each transition
		for(WeightedTransition t : trans){
			//calculate the weight of the transition
			//the weight of epsilon transition is always one
			//thus newWeight is the weight of t.
			Fraction newWeight = t.getWeight();
			WeightedTransition newT = new WeightedTransition(this,t.getSymb(), t.getToState(), newWeight);
			//right now we assume if there are other transitions to to.getToState() from this
			//then we will have duplicate transitions.
			transitions.add(newT);
		}

		if(!sum.equals(new Fraction(0,1))){
			//if at least one toState is an accepting state
			if(accept){
				//if this state is accepting one, then update its weight
				//by multiplying its weight by the sum
				w = w.multiply(sum);
			} else {
				//assign the value of the sum and
				//make it a final state
				w = sum;
				accept = true;
			}
		}
	}

	/**
	 * 
	 * @param incoming the incoming transition of this state
	 * @param toState the destination of the epsilon transition from this state
	 * @param weight the weight of the epsilon transition
	 */
//	public void addEpsilonTransition(Set<WeightedTransition> incoming, WeightedState toState, Fraction weight) {
//		//System.out.println("Epsilon " + this + " with " + toState + " weight " + weight);
//		//1. Scenario when this state has no incoming transitions
//		if(incoming.isEmpty()){
//			for(WeightedTransition t : toState.getTransitions()){
//				//unless this state is final, then 
//				WeightedTransition newT = new WeightedTransition(this,t.getSymb(), t.getToState(),t.getWeight().multiply(weight));
//				//right now we assume if there are other transitions to to.getToState() from this
//				//then we will have duplicate transitions.
//				addTransition(newT);
//			}
//			//for general weighted automata should also update the incoming edges,
//			//but for acyclic no need to do so - for union we connect to the start
//			//states that have no incoming edges and for the concat
//			//we connect to the start state again.
//
//			//update the weights if toState is final
//			if(toState.isAccept()){
//				if(accept){
//					//							System.out.println("This is accept and toState is accept");
//					//							System.out.println("This state is " + this);
//					//							System.out.println("To state is " + toState);
//					//adding 1 because of two copies to accept epsilon transition
//					//							w = w.add(1).multiply(toState.getWeight()).multiply(weight); <--- incorrect as of Jan 23'19
//					//the weight of epslion trans should be multiplied by the weight
//					//of the final state and added it to the weight of this state
//					//Basically the number w of copies could state in this state
//					//and the number of copies weight*toState.weight can go to toState
//					//w = w.add(weight.multiply(toState.getWeight())); //still now working, just multiple 
//					if(toState.getTransitions().isEmpty()){
//						w = w.multiply(weight).multiply(toState.getWeight()); // multiply is for concatenation
//						//I think regular epsilon transitions should be added
//					} else {
//						//already accounted for the weight by propagating it to the transitions.
//						w = w.multiply(toState.getWeight());
//					}
//				} else {
//					accept = true;
//					if(toState.getTransitions().isEmpty()){
//						w = toState.getWeight().multiply(weight);
//					} else {
//						//already accounted for weights by propagating them to the transitions
//						w = toState.getWeight();
//					}
//				}
//			}
//		} else {
//			//2. Scenario when there are incoming transition
//			// Re-route them to toState and update the weight if this
//			//state is the final state
//			for(WeightedTransition in : incoming){
//				Fraction newWeight = in.getWeight().multiply(weight);
//				if(accept){
//					//multiple the the weight by the weight of the
//					//accepting state
//					newWeight = newWeight.multiply(w);
//				}
//				WeightedTransition newT = new WeightedTransition(in.getFromState(), in.getSymb(), toState, newWeight);
//				//add the transition to the source of the incoming state
//				in.getFromState().addTransition(newT);
//			}
//		}
//
//	}

	/**
	 * When adding one transition at a time, e.g.,
	 * concat operation
	 * @param incoming - the transitions incoming into this state
	 * @param toState - the state to create epsilong transtion to
	 */
	public void addEpsilonTransition(Set<WeightedTransition> incoming, WeightedState toState){
		//first create the transitions

		//1. Scenario when this state has no incoming transitions
		if(incoming.isEmpty()){
			for(WeightedTransition t : toState.getTransitions()){
				//unless this state is final, then 
				Fraction transWeight = t.getWeight();
				if(accept){
					transWeight = t.getWeight().multiply(w);
				}
				WeightedTransition newT = new WeightedTransition(this,t.getSymb(), t.getToState(),transWeight);
				//right now we assume if there are other transitions to to.getToState() from this
				//then we will have duplicate transitions.
				addTransition(newT);
			}
			//for general weighted automata should also update the incoming edges,
			//but for acyclic no need to do so - for union we connect to the start
			//states that have no incoming edges and for the concat
			//we connect to the start state again.

			//update the weights if toState is final
			if(toState.isAccept()){
				if(accept){
					//adding 1 because of two copies to accept epsilon transition
					//w = w.add(1).multiply(toState.getWeight());
					//w = w.add(toState.getWeight());// still not working 2-13-19
					w = w.multiply(toState.getWeight());
				} else {
					accept = true;
					w = toState.getWeight();
				}
			}
		} else {
			//2. Scenario when there are incoming transition
			// Re-route them to toState and update the weight if this
			//state is the final state
			for(WeightedTransition in : incoming){
				Fraction newWeight = in.getWeight();
				if(accept){
					//multiple the the weight by the weight of the
					//accepting state
					newWeight = newWeight.multiply(w);
				}
				WeightedTransition newT = new WeightedTransition(in.getFromState(), in.getSymb(), toState, newWeight);
				//add the transition to the source of the incoming state
				in.getFromState().addTransition(newT);
			}
		}
	}

	/**
	 * Adding a new transition, which might already
	 * exists in the set
	 * @param t
	 */
	public void addTransition(WeightedTransition t){
		/*in regular automaton we would
		 *just add to the set (override if
		 *one already exists)
		 *However here we should be more careful
		 *If transition already exists
		 *then we need to add the weights
		 *together
		 */
		WeightedTransition update = null;
		for(WeightedTransition wt : transitions){
			//TODO - write equals method for transitions
			//I'd say the same char and to state but different 
			//weights - all different because we might
			//have a transition with a different weight
			if(wt.getSymb() == t.getSymb() && wt.getToState().equals(t.getToState())){
				update = wt;
			}
		}

		if(update == null){
			//did not find the transition to the same state on
			//the same symbol
			transitions.add(t);
		} else {
			//update the weight - only of one transition to the sum
			//and break after that
			Fraction oldWeight = update.getWeight();
			update.setWeight(oldWeight.add(t.getWeight()));
		}
	}

	public int getNumber(){
		return number;
	}

	public void setAccept(boolean accept){
		this.accept = accept;
		if(!accept){
			//reset the weight since
			//the weight only count in 
			//final states
			w = new Fraction(1,1);
		}
	}

	public boolean isAccept(){
		return accept;
	}

	/**
	 * Compares this object with the specified object for order.
	 * States are ordered by the time of construction.
	 */
	public int compareTo(WeightedState s) {
		return s.id - id;
	}

	/**
	 * See {@link java.lang.Object#equals(java.lang.Object)}.
	 */
	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	/**
	 * See {@link java.lang.Object#hashCode()}.
	 */
	@Override
	public int hashCode() {
		return super.hashCode();
	}

	/** 
	 * Returns string describing this state. Normally invoked via 
	 * {@link WeightedAutomaton#toString()}.
	 */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("state ").append(number).append(";").append(w);
		if (accept){
			b.append(" [accept]");
		} else {
			b.append(" [reject]");
		}
		b.append(":\n");
		for (WeightedTransition t : transitions)
			b.append("  ").append(t.toString()).append("\n");
		return b.toString();
	}

	public void removeTransition(WeightedTransition t) {
		this.transitions.remove(t);

	}



}
