package edu.boisestate.cs.automaton.acyclic;

import java.io.Serializable;

import org.apache.commons.math3.fraction.Fraction;

public class WeightedTransition implements Serializable, Cloneable{
	
	private char symb; //right now just have one symbol
	private WeightedState toState;
	private Fraction w;
	private WeightedState fromState;
	
	public WeightedTransition(WeightedState fromState, char c, WeightedState toState){
		symb = c;
		this.toState = toState;
		this.fromState = fromState;
		w = new Fraction(1,1);
	}
	
	public WeightedTransition(WeightedState fromState, char c, WeightedState toState, Fraction weight){
		symb = c;
		this.toState = toState;
		this.fromState = fromState;
		w = weight;
	}
	
	@Override
	public String toString(){
		StringBuilder b = new StringBuilder();
		b.append(fromState.getNumber()).append(" -> ");
		b.append("(");
		b.append(symb);
		b.append(", ").append(w).append(")");
		b.append(" -> ").append(toState.getNumber());
		
		return b.toString();
		
	}
	
	void appendDot(StringBuilder b) {
        b.append(" -> ").append(toState.getNumber()).append(" [label=\"");
        b.append(symb);
        b.append(", ").append(w);
        b.append("\"]\n");
	}
	
	public Fraction getWeight(){
		return w;
	}
	
	public void setWeight(Fraction newWeight){
		w = newWeight;
	}
	
	public char getSymb(){
		return symb;
	}
	
	public WeightedState getToState(){
		return toState;
	}
	
	public WeightedState getFromState(){
		return fromState;
	}

	public void setToState(WeightedState tempState) {
		toState = tempState;
		
	}

	public void setSybmol(char replace) {
		symb = replace;
		
	}

}
