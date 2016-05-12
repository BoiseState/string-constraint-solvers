/**
 * An extended EJSA operation for a more precise prefix operation.
 */
package edu.boisestate.cs.stringOperations;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.StatePair;
import dk.brics.automaton.Transition;
import dk.brics.string.charset.CharSet;
import dk.brics.string.stringoperations.AssertHasLength;
import dk.brics.string.stringoperations.UnaryOperation;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class PreciseSuffix extends UnaryOperation{
	int end;
	public PreciseSuffix(int end){
		this.end=end;
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
		for(int i=0; i<end; i++){
			LinkedList<Transition> transitions=new LinkedList<Transition>();

			while(states.size()>0){
				transitions.addAll(states.removeFirst().getTransitions());
			}
			while(transitions.size()>0){
				states.add(transitions.removeFirst().getDest());
			}
		}
        Set<StatePair> epsilons = new HashSet<StatePair>();
        State finalState =new State();
        while(states.size()>0){
            epsilons.add(new StatePair(states.removeFirst(), finalState));

        }
        finalState.setAccept(true);
        for(State s: b.getAcceptStates()){
        	if(s!=finalState)
        	s.setAccept(false);
        }
        b.addEpsilons(epsilons);
        b.determinize();
        b.minimize();
        Automaton any=Automaton.makeAnyString();
        AssertHasLength l=new AssertHasLength(end, end);
        b=b.intersection(l.op(any));
        b.minimize();
		return b;
	}
	@Override
	public int getPriority() {
		return 4;
	}
	@Override
	public String toString() {
		return "PreciseSuffix";
	}

}
