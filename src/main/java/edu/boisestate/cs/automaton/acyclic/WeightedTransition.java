package edu.boisestate.cs.automaton.acyclic;

import java.io.Serializable;

import org.apache.commons.math3.fraction.Fraction;

public class WeightedTransition implements Serializable, Cloneable{
	
	private char symb; //right now just have one symbol
	private WeightedState toState;
	private Fraction w;
	
	public WeightedTransition(char c, WeightedState toState){
		symb = c;
		this.toState = toState;
		w = new Fraction(1,1);
	}
	
	public WeightedTransition(char c, WeightedState toState, Fraction weight){
		symb = c;
		this.toState = toState;
		w = weight;
	}
	
	@Override
	public String toString(){
		StringBuilder b = new StringBuilder();
		b.append("(");
		b.append(symb);
		b.append(", ").append(w).append(")");
		b.append(" -> ").append(toState.getNumber());
		
		return b.toString();
		
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

}
