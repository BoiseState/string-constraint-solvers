/*
 * dk.brics.automaton
 * 
 * Copyright (c) 2001-2011 Anders Moeller
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.boisestate.cs.automaton;

import java.io.Serializable;
import java.util.*;
import org.apache.commons.math3.fraction.Fraction;

/** 
 * <tt>Automaton</tt> state.
 * @author Andrew Harris
 * @author Anders M&oslash;ller &lt;<a href="mailto:amoeller@cs.au.dk">amoeller@cs.au.dk</a>&gt;
 */
public class WeightedState
        implements Serializable, Comparable<WeightedState> {
	
	private boolean accept;
	private Set<WeightedTransition> transitions;
	
	private int number;
	
	private Fraction w;

	public int getNumber() {
		return number;
	}

	private int id;
	private static int next_id;

	public void setNumber(int number) {
		this.number = number;
	}
	
	public void setWeight(Fraction newWeight){
		w = newWeight;
	}
	
	public Fraction getWeight(){
		return w;
	}

	public void setTransitions(Collection<WeightedTransition> transitions) {
		this.transitions.clear();
		this.transitions.addAll(transitions);
	}

	/** 
	 * Constructs a new state. Initially, the new state is a reject state. 
	 */
	public WeightedState() {
		resetTransitions();
		id = next_id++;
		w = new Fraction(1,1);
	}
	
	/** 
	 * Resets transition set. 
	 */
	final void resetTransitions() {
		transitions = new HashSet<WeightedTransition>();
	}
	
	/** 
	 * Returns the set of outgoing transitions. 
	 * Subsequent changes are reflected in the automaton.
	 * @return transition set
	 */
	public Set<WeightedTransition> getTransitions()	{
		return transitions;
	}
	
	/**
	 * Adds an outgoing transition.
	 * @param t transition
	 */
	public void addTransition(WeightedTransition t)	{
		// if transition for characters with equal weight already exists
		while(transitions.contains(t)){
			// remove existing duplicate transition
			transitions.remove(t);
			// double weight
			t.setWeightInt(t.getWeightInt() * 2);
		}
		transitions.add(t);
	}
	
	/** 
	 * Sets acceptance for this state.
	 * @param accept if true, this state is an accept state
	 */
	public void setAccept(boolean accept) {
		this.accept = accept;
	}
	
	/**
	 * Returns acceptance status.
	 * @return true is this is an accept state
	 */
	public boolean isAccept() {
		return accept;
	}
	
	/** 
	 * Performs lookup in transitions, assuming determinism. 
	 * @param c character to look up
	 * @return destination state, null if no matching outgoing transition
	 * @see #step(char, Collection)
	 */
	public StateWeight step(char c) {
		for (WeightedTransition t : transitions)
			if (t.getMin() <= c && c <= t.getMax())
				return new StateWeight(t.getDest(), t.getWeightInt());
		return null;
	}

	/** 
	 * Performs lookup in transitions, allowing nondeterminism.
	 * @param c character to look up
	 * @param dest collection where destination states are stored
	 * @see #step(char)
	 */
	public void step(char c, Collection<StateWeight> dest) {
		for (WeightedTransition t : transitions)
			if (t.getMin() <= c && c <= t.getMax())
				dest.add(new StateWeight(t.getDest(), t.getWeightInt()));
	}

	void addEpsilon(WeightedState to) {
		if (to.accept) {
			accept = true;
		}
		for (WeightedTransition t : to.transitions) {
			if (transitions.contains(t)) {
				transitions.remove(t);
				int newWeight = t.getWeightInt() * 2;
				transitions.add(new WeightedTransition(t.getMin(), t.getMax(), t.getDest(), newWeight));
			} else {
				transitions.add(t);
			}
		}
	}

	void addEpsilon(WeightedState to, int weight) {
		if (to.accept)
			accept = true;
		for (WeightedTransition t : to.transitions) {
			int newWeight = t.getWeightInt() * weight;
			transitions.add(new WeightedTransition(t.getMin(), t.getMax(), t.getDest(), newWeight));
		}
	}
	
	/** Returns transitions sorted by (min, reverse max, to, weight) or
	 *  (to, min, max, weight) */
	WeightedTransition[] getSortedTransitionArray(boolean to_first) {
		WeightedTransition[] e = transitions.toArray(new WeightedTransition[transitions.size()]);
		Arrays.sort(e, new WeightedTransitionComparator(to_first));
		return e;
	}
	
	/**
	 * Returns sorted list of outgoing transitions.
	 * @param to_first if true, order by (to, min, max, weight); otherwise (min, reverse max, to, weight)
	 * @return transition list
	 */
	public List<WeightedTransition> getSortedTransitions(boolean to_first)	{
		return Arrays.asList(getSortedTransitionArray(to_first));
	}
	
	/** 
	 * Returns string describing this state. Normally invoked via 
	 * {@link WeightedAutomaton#toString()}.
	 */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("state ").append(number).append(";").append(w);
		if (accept)
			b.append(" [accept]");
		else
			b.append(" [reject]");
		b.append(":\n");
		for (WeightedTransition t : transitions)
			b.append("  ").append(t.toString()).append("\n");
		return b.toString();
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
}
