package edu.boisestate.cs.automaton.acyclic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.fraction.Fraction;
import org.apache.commons.math3.util.Pair;

public class BasicAcyclicWeightedOperations {


	/**
	 * Returns an automaton that accepts the concatenation of the
	 * languages of the given automata.
	 * @param a1
	 * @param a2
	 * @return
	 */
	static public AcyclicWeightedAutomaton concantenate(AcyclicWeightedAutomaton a1,
			AcyclicWeightedAutomaton a2){
		//create copies of a1 and a2
		a1 = a1.clone();
		a2 = a2.clone();
		//get accept states of a1
		for(WeightedState s : a1.getAcceptStates()){
			s.addEpsilonTransition(a2.initial);
			//need to do it second since 
			//if s is final then its new weight
			//is calculated differently when a2.initial
			//is also final.
			//only going to set to false if a2.initial is
			//not final, otherwise it will undo epsilon transition
			//marking of s as final when a2.initial is final 
			//i.e., final of a2 is pushed up to a1
			if(!a2.initial.isAccept()){
				s.setAccept(false);
			}
		}
		return a1;
	}
	
	/**
	 * Returns a weighted automaton that is the union of two automata
	 * @param a1
	 * @param a2
	 * @return
	 */
	public static AcyclicWeightedAutomaton union(AcyclicWeightedAutomaton a1, AcyclicWeightedAutomaton a2){
		a1 = a1.clone();
		a2 = a2.clone();
		
		WeightedState newStart = new WeightedState();
		newStart.addEpsilonTransitions(a1.initial, a2.initial);
		a1.initial = newStart;
		
		return a1;
	}

	/**
	 * Removes epsilon transitions from an acyclic weighted automaton
	 * Should be done before the determinization algorithm since 
	 * it assumes  that the machine has no epsilon transitions
	 * @param a an automaton with epsilon transition
	 * @return the equivalent version of a without epsilon transitions.
	 */
	static public AcyclicWeightedAutomaton epsilonRemoval(AcyclicWeightedAutomaton a){
		a = a.clone();
		//Algorithm on p.22 Mehryar Mohri - not sure 
		//if we need it, especially its general case with e-closures
		//we will try to make each operation without e transitions
		//plus we don't have a special symbol for it anyway
		return a;
	}

	/**
	 * a should be e-transitions free 
	 * @param a
	 */
	public static void determinize(AcyclicWeightedAutomaton a) {

		WeightedState oldInit = a.getInitialState();

		Map<Set<Pair<WeightedState, Fraction>>, WeightedState> waStates = 
				new HashMap<Set<Pair<WeightedState, Fraction>>, WeightedState>();
		//unlike the alg, there is only one initial state
		Pair<WeightedState, Fraction> pH = new Pair<WeightedState, Fraction>(oldInit, 
				new Fraction(1));
		Set<Pair<WeightedState, Fraction>> iP = new HashSet<Pair<WeightedState, Fraction>>();
		iP.add(pH);
		//add as the initial states and add to the map
		WeightedState waInit = new WeightedState();
		a.setInitialState(waInit);
		//set the weight, and what if it is also a final state with some weight
		//for epsilon transitions -- look more into it.
		if(oldInit.isAccept()){
			waInit.setAccept(true);
			waInit.setWeight(oldInit.getWeight());
		}
		waStates.put(iP, waInit);

		List<Set<Pair<WeightedState, Fraction>>> queue = new ArrayList<Set<Pair<WeightedState, Fraction>>>();
		queue.add(iP);
		while(queue.size() > 0){
			Set<Pair<WeightedState, Fraction>> pP = queue.remove(0);//remove first element
			//get all the labels outgoing those transition
			Set<Character> labelSet = new HashSet<Character>();
			for(Pair<WeightedState, Fraction> s : pP){
				//get the transition
				Set<WeightedTransition> wt = s.getFirst().getTransitions();
				for(WeightedTransition t : wt){
					//					for(char label = t.getMin(); label <= t.getMax();label++){
					//						labelSet.add(label);
					//					}
					labelSet.add(t.getSymb());
				}
			}
			System.out.println("labelSet: " + labelSet);
			//for each input label on the transition of states in pP pairs
			for(char label : labelSet){
				//calculate the new weight of that label
				//over all states in pP that have that label
				//new weight
				Fraction wP = new Fraction(0,1);
				//now compute the new state, which is a set of pair
				Set<Pair<WeightedState, Fraction>> qPPre = new HashSet<Pair<WeightedState, Fraction>>();
				for(Pair<WeightedState, Fraction> pv : pP){
					System.out.println("PV: " + pv);

					//get the state
					WeightedState p = pv.getFirst();
					//get the transitions
					Set<WeightedTransition> wt = p.getTransitions();
					//for all transitions
					for(WeightedTransition t : wt){
						System.out.println("t: " + t + " " + wP);
						//determine if label between min and max
						if(t.getSymb() == label){
							//get the weight and multiple by the fraction of s
							Fraction vw = pv.getSecond().multiply(t.getWeight());
							wP = wP.add(vw);
							//pre-compute first pairs and weight and then mult by w^{-1}
							WeightedState q = t.getToState();
							Pair<WeightedState, Fraction> qvP = new Pair<WeightedState, Fraction>(q, vw);
							qPPre.add(qvP);
						}
					}//end for t:wt
				}// end pv : pP for new trans weight calc
				System.out.println("new trans weight: " + wP);

				//the new state
				Set<Pair<WeightedState, Fraction>> qP = new HashSet<Pair<WeightedState, Fraction>>(); 
				//find all states that starts with one pP on label recal the weight
				for(Pair<WeightedState, Fraction> qvP : qPPre){
					Fraction fr = qvP.getSecond().divide(wP);
					Pair<WeightedState, Fraction> updatedPair = new Pair<WeightedState, Fraction>(qvP.getFirst(), fr);
					qP.add(updatedPair);
				}// end qvP : qP new state calc

				//check if the state already exists
				//create a state for it
				WeightedState waS = null;
				if(waStates.containsKey(qP)){
					waS = waStates.get(qP);
				} else {
					waS = new WeightedState();
					waStates.put(qP, waS);
				}
				System.out.println("New state: " + qP);
				//now create a transition for between pP and qP
				//make sure that the state is in the map
				if(waStates.containsKey(pP)){
					//get the state
					WeightedState from = waStates.get(pP);
					from.addTransition(new WeightedTransition(label,waS,wP));
					System.out.println("New trans: " + from);
				} else{
					System.err.println("cannot find state in the map!");
				}

				if(!queue.contains(qP)){
					//check if qP contains p that is a final state
					//and compute its weight in case the string exists there
					Fraction finalW = Fraction.ZERO;
					for(Pair<WeightedState, Fraction> qvP : qP){
						if(qvP.getFirst().isAccept()){
							//then qP's state is also accept
							//ro is a weight function for the final states only
							finalW = finalW.add(qvP.getSecond().multiply(qvP.getFirst().getWeight()));
						}
					}
					System.out.println("Final weight: " + finalW);
					if(finalW != Fraction.ZERO){
						//means found at least one non-final state
						waS.setAccept(true);
						//and set its weight
						waS.setWeight(finalW);
					}
					//break;
					queue.add(qP);
				}
			}
		}//end of while loop for queue
	}
	
	public static void normalize(AcyclicWeightedAutomaton a){
		//BFS until reached a final state with no more transitions
		//staring from the start state
		List<WeightedState> queue = new ArrayList<WeightedState>();
		
		//do BFS, add to the queue and retrieve it in reverse later
		List<WeightedState> revQueue = new ArrayList<WeightedState>();
		revQueue.add(a.getInitialState());
		while(!revQueue.isEmpty()){
			WeightedState curr = revQueue.remove(0);
			if(queue.contains(curr)){
				queue.remove(curr);
			}
			queue.add(curr);
			for(WeightedTransition  wT : curr.getTransitions()){
				//get its children and add them to the queue
				WeightedState next = wT.getToState();
				if(!revQueue.contains(next)){
					revQueue.add(wT.getToState());
				}
			}
		}
		System.out.println(queue);

		while(!queue.isEmpty()){
			//process a state and add it predecessors into the queue
			WeightedState curr = queue.remove(queue.size()-1);
			System.out.println("curr " + curr);
			//compute the factor
			//call on the successors
			Set<WeightedTransition> outs = curr.getTransitions();
			int numOuts = outs.size();
			int[] pushedWeights;
			int i=0;
			//if it is an accepting state, then add the weight
			//of this state to the computation, i.e.,
			//treat it as an outgoing edge.
			if(curr.isAccept()){
				pushedWeights = new int[numOuts+1];
				pushedWeights[i] = curr.getWeight().getDenominator();
				i++;
			} else {
				pushedWeights = new int[numOuts];
			}
			
			//collect fraction's denominators from outgoing edges
			for(WeightedTransition tSucc : curr.getTransitions()){;
				pushedWeights[i] = tSucc.getWeight().getDenominator();
				System.out.println("pw " + pushedWeights[i]);
				i++;
			}
			//find least common multiplier for all weights
			int lcm = lcmArr(pushedWeights);
			System.out.println("lcm: " + lcm);
			
			//update the weight of the incoming and the current weight
			//if the state is final or the start state
			if(curr.isAccept() || a.getInitialState().equals(curr)){
				curr.setWeight(curr.getWeight().multiply(lcm));
			}
			for(WeightedTransition tSucc : curr.getTransitions()){;
			tSucc.setWeight(tSucc.getWeight().multiply(lcm));
		}
			
			//get the incoming transitions and divide them by lcm
			Set<WeightedTransition> ins = a.getIncoming(curr);
			for(WeightedTransition inPred : ins){
				System.out.println("inPred: " + inPred);
				inPred.setWeight(inPred.getWeight().divide(lcm));
			}
		}
		
	}
	
	public static int lcm(int a, int b){
		return Math.abs(a*b)/gcd(a,b);
	}
	
	public static int gcd(int a, int b){
		if(b==0) return a;
		return gcd(b, a%b);
	}
	
	public static int gcdArr(int[] arr){
		int result = arr[0];
		for(int i=1; i < arr.length; i++){
			result = gcd(arr[i], result);
			System.out.println(result);
		}
		return result;
	}
	
	public static int lcmArr(int[] arr){
		int result = arr[0];
		for(int i=1; i < arr.length; i++){
			result = lcm(result, arr[i]);
		}
		
		return result;
	}

	/**
	 * Returns an automaton that accepts between min and max (inclusive)
	 * concatenated repetitions of the language of the given automaton.
	 * @param a
	 * @param min
	 * @param max
	 * @return
	 */
	public static AcyclicWeightedAutomaton repeat(AcyclicWeightedAutomaton a, int min, int max) {
		int diff = max - min;
		
		AcyclicWeightedAutomaton ret;
		if(min < 0){
			ret = BasicAcyclicWeightedAutomaton.makeEmpty();
		} else if(min == 0){
			//create an automaton that accepts the empty string.
			ret = BasicAcyclicWeightedAutomaton.makeEmptyString();
		} else { //assume min > 0
			//create a clone
			ret = a.clone();
			min--;
			//if there are more, then append them in sequence
			while(min > 0){
				ret = concantenate(ret,a);
				min--;
			}
		}
		
		if(diff > 0){
			//need to do repetitions, almost
			//line a concat only without setting
			//the final states to non-final
			AcyclicWeightedAutomaton temp2 = a.clone();
			diff--;
			while(diff > 0){
				AcyclicWeightedAutomaton temp1 = a.clone();
				for(WeightedState p : temp1.getAcceptStates()){
					p.addEpsilonTransition(temp2.initial);
				}
				temp2 = temp1;
				diff--;
			}
			//attach temp2 to ret
			for(WeightedState p : ret.getAcceptStates()){
				p.addEpsilonTransition(temp2.initial);
			}
		}
		
		return ret;
	}

	/**
	 * Returns an automaton that accepts the intersection of
	 * the language of the given automata.
	 * (A1 \cap A2) (s) = A1(s) x A2(s)
	 * That is the count of number of times
	 * s accepted by A1 should be multiplied
	 * the number of times A2 accepted by s.
	 * @param a1
	 * @param a2
	 * @return
	 */
	public static AcyclicWeightedAutomaton intersection(AcyclicWeightedAutomaton a1,
			AcyclicWeightedAutomaton a2) {
		AcyclicWeightedAutomaton ret;
		if(a1.isEmpty() || a2.isEmpty()){
			ret =  BasicAcyclicWeightedAutomaton.makeEmpty();
		} else {
			//the algorithm is the same as the traditional
			//automaton, only the weights of the
			//transitions and the weight of the final
			//states should be multiplied.
			
			ret = new AcyclicWeightedAutomaton();
			Map<Pair<WeightedState,WeightedState>, WeightedState> oldToNew = 
					new HashMap<Pair<WeightedState,WeightedState>,WeightedState>();
			Pair<WeightedState,WeightedState> sOld = 
					new Pair<WeightedState,WeightedState>(a1.initial,a2.initial);
			WeightedState sNew = new WeightedState();//newly discovered state
			if(a1.initial.isAccept() && a2.initial.isAccept()){
				sNew.setAccept(true);
				sNew.setWeight(a1.initial.getWeight().multiply(a2.initial.getWeight()));
			}
			ret.initial = sNew;
			//add to the queue
			List<Pair<WeightedState,WeightedState>> queue = new ArrayList<Pair<WeightedState,WeightedState>>();
			queue.add(sOld);
			oldToNew.put(sOld, sNew);
			while(!queue.isEmpty()){
				//remove the elements
				sOld = queue.remove(0);
				WeightedState curr = sNew;
				//for the pair of edges with the same symbol
				for(WeightedTransition t1 : sOld.getFirst().getTransitions()){
					for(WeightedTransition t2: sOld.getSecond().getTransitions()){
						if(t1.getSymb() == t2.getSymb()){
							//the same symbols
							//find toState for each
							WeightedState t1ToState = t1.getToState();
							WeightedState t2ToState = t2.getToState();
							//create a pair and see if it is in the map
							Pair<WeightedState,WeightedState> oldToState = 
									new Pair<WeightedState,WeightedState>(t1ToState, t2ToState);
							if(oldToNew.containsKey(oldToState)){
								//this state already been seen
								 sNew = oldToNew.get(oldToState);
							} else {
								//did not see it, add to the queue
								//and to the map
								sNew = new WeightedState();
								//check if it should be an accept state
								//and update its weight
								if(t1ToState.isAccept() && t2ToState.isAccept()){
									sNew.setAccept(true);
									sNew.setWeight(t1ToState.getWeight().multiply(t2ToState.getWeight()));
								}
								//add to the map and to the queue
								queue.add(oldToState);
								oldToNew.put(oldToState, sNew);
							}
							
							//add a weighted edge to curr on that symbol to the
							//discovered state sNew
							//where weights are multiplied
							curr.addTransition(new WeightedTransition(t1.getSymb(), 
									sNew, t1.getWeight().multiply(t2.getWeight())));
						}
					}
				}
		
				
			}
		}
		return ret; 
	}

	public static boolean isEmpty(AcyclicWeightedAutomaton a) {
		//conditions works when we have minimized automaton
		//since the minimization algorithm would make one 
		//accepting state
		return !a.initial.isAccept() && a.initial.getTransitions().isEmpty();
	}

	public static boolean run(AcyclicWeightedAutomaton a, String s) {
		//we assume that our automaton is deterministic
		WeightedState p = a.initial;
		for(int i = 0; i < s.length() && p != null; i++){
			for(WeightedTransition t : p.getTransitions()){
				if(t.getSymb() == s.charAt(i)){
					//assign the next state
					p = t.getToState();
					break;
				}
			}
		}
		boolean ret = p == null? false : p.isAccept();
		return ret;
	}

}
