package edu.boisestate.cs.automatonModel;

import edu.boisestate.cs.Alphabet;
import edu.boisestate.cs.automaton.acyclic.AcyclicWeightedAutomaton;
import edu.boisestate.cs.automaton.acyclic.BasicAcyclicWeightedAutomaton;

public class AcyclicWeightedAutomatonModelManager extends AutomatonModelManager {
	private final int boundLenght;
	
	public AcyclicWeightedAutomatonModelManager(Alphabet alphabet, int initialBoundLenght){
		this.alphabet = alphabet;
		this.boundLenght = initialBoundLenght;
	}
	
	static void setInstance(Alphabet alphabet, int initialBoundLength){
		instance = new AcyclicWeightedAutomatonModelManager(alphabet, initialBoundLength);
	}
	
	@Override
	public AutomatonModel createString(String string) {
		AcyclicWeightedAutomaton a = BasicAcyclicWeightedAutomaton.makeString(string);
		
		return  new AcyclicWeightedAutomatonModel(a, alphabet, boundLenght);
	}

	@Override
	public AutomatonModel createAnyString(int initialBound) {
		// TODO Auto-generated method stub
		return createAnyString(0, initialBound);
	}

	@Override
	public AutomatonModel createAnyString() {

		return createAnyString(0, boundLenght);
	}

	@Override
	public AutomatonModel createAnyString(int min, int max) {
		//create a symbolic string from min to max
		String charSet = alphabet.getCharSet();
		AcyclicWeightedAutomaton a  = BasicAcyclicWeightedAutomaton.makeCharSet(charSet);
		//repeat a from min to max
		a = a.repeat(min,max);
		return new AcyclicWeightedAutomatonModel(a, alphabet, max);
	}

}
