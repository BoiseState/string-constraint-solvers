/**
 * An extended EJSA operation for a more precise prefix operation.
 */
package edu.boisestate.cs.stringOperations;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.StatePair;
import dk.brics.automaton.Transition;
import dk.brics.string.charset.CharSet;
import dk.brics.string.stringoperations.UnaryOperation;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class PrecisePrefix extends UnaryOperation{

		int start;
		public PrecisePrefix(int start){
			this.start=start;
		}
		@Override
		public CharSet charsetTransfer(CharSet arg0) {
			return arg0;
		}
		@Override
		public Automaton op(Automaton a) {
			Automaton b=a.clone();
			LinkedList<State> states=new LinkedList<State>();
			states.add(b.getInitialState());
			for(int i=0; i<start; i++){
				LinkedList<Transition> transitions=new LinkedList<Transition>();

				while(states.size()>0){
					transitions.addAll(states.removeFirst().getTransitions());
				}
				while(transitions.size()>0){
					states.add(transitions.removeFirst().getDest());
				}
			}
	        Set<StatePair> epsilons = new HashSet<StatePair>();
	        State initial =new State();
	        while(states.size()>0){
	            epsilons.add(new StatePair(initial, states.removeFirst()));

	        }
	        b.setInitialState(initial);
	        b.addEpsilons(epsilons);
	        b.minimize();
			return b;
		}
		@Override
		public int getPriority() {
			// TODO Auto-generated method stub
			return 4;
		}
		@Override
		public String toString() {
			return "PrecisePrefix";
		}

}
