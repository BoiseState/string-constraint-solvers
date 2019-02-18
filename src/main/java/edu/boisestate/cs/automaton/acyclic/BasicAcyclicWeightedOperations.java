package edu.boisestate.cs.automaton.acyclic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.math3.fraction.Fraction;
import org.apache.commons.math3.util.Pair;

import edu.boisestate.cs.util.DotToGraph;

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
		//a2 = a2.clone();
		//get accept states of the original machine sicne we 
		//will modify its accepting states
		Set<WeightedState> oldAcceptStates = a1.getAcceptStates();
		for(WeightedState s : oldAcceptStates){
			Set<WeightedTransition> incoming = a1.getIncoming(s);
//			if(incoming.isEmpty()){
//				s.addEpsilonTransition(incoming, a2.initial, s.getWeight());
//			} else {
				s.addEpsilonTransition(incoming, a2.initial);
//			}
			
			//1. Scenario
			//if a2.initial is final and no incoming edges for a1,
			//then don't change s to not being final,
			//otherwise Scenario 2 do
			if(!incoming.isEmpty() || !a2.initial.isAccept()){
				s.setAccept(false);
			}
		}
//		DotToGraph.outputDotFile(a1.toDot(), "concat");
//		System.exit(2);
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
		//remove dead states
		removeUnreachableStates(a);
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
			//System.out.println("labelSet: " + labelSet);
			//for each input label on the transition of states in pP pairs
			for(char label : labelSet){
				//calculate the new weight of that label
				//over all states in pP that have that label
				//new weight
				Fraction wP = new Fraction(0,1);
				//now compute the new state, which is a set of pair
				Set<Pair<WeightedState, Fraction>> qPPre = new HashSet<Pair<WeightedState, Fraction>>();
				//System.out.println("qPPre " + pP);
				for(Pair<WeightedState, Fraction> pv : pP){
					//System.out.println("PV: " + pv + "\n" + label);

					//get the state
					WeightedState p = pv.getFirst();
					//get the transitions
					Set<WeightedTransition> wt = p.getTransitions();
					//for all transitions
					for(WeightedTransition t : wt){
						//System.out.println("t: " + t + " " + wP);
						//determine if label between min and max
						if(t.getSymb() == label){
							//get the weight and multiple by the fraction of s
							Fraction vw = pv.getSecond().multiply(t.getWeight());
							wP = wP.add(vw);
							//pre-compute first pairs and weight and then mult by w^{-1}
							WeightedState q = t.getToState();
							//need to check whether qPPre already has a pair with that state
							for(Pair<WeightedState, Fraction> currP : qPPre){
								if(currP.getFirst().equals(q)){
									vw = vw.add(currP.getSecond());
									qPPre.remove(currP);
									break;
								}
							}
							Pair<WeightedState, Fraction> qvP = new Pair<WeightedState, Fraction>(q, vw);
							qPPre.add(qvP);
						}
					}//end for t:wt
					//System.out.println("weight " + wP);
				}// end pv : pP for new trans weight calc
				//System.out.println("new trans weight: " + wP);

				//the new state
				Set<Pair<WeightedState, Fraction>> qP = new HashSet<Pair<WeightedState, Fraction>>(); 
				//find all states that starts with one pP on label recal the weight
				for(Pair<WeightedState, Fraction> qvP : qPPre){
					Fraction fr = qvP.getSecond().divide(wP);
					Pair<WeightedState, Fraction> updatedPair = new Pair<WeightedState, Fraction>(qvP.getFirst(), fr);
					qP.add(updatedPair);
				}// end qvP : qP new state calc
				//System.out.println("new pair " + qP);
				//check if the state already exists
				//create a state for it
				WeightedState waS = null;
				//need to calculate the final weight of the state first before 
				//checking it existence, because the default is the weight 1
				boolean newState = false;
				if(waStates.containsKey(qP)){
					waS = waStates.get(qP);
					//System.out.println("found");
				} else {
					waS = new WeightedState();
					waStates.put(qP, waS);
					newState = true;
					//System.out.println("not found");
				}
				//System.out.println("New state: " + qP);
				//now create a transition for between pP and qP
				//make sure that the state is in the map
				if(waStates.containsKey(pP)){
					//get the state
					WeightedState from = waStates.get(pP);
					from.addTransition(new WeightedTransition(from,label,waS,wP));
					//System.out.println("New trans: " + from);
				} else{
					System.err.println("cannot find state in the map!");
				}

				//not in the queue and has not been found yet.
				//that is, a new state
				if(newState /*!queue.contains(qP)*/){
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
					//System.out.println("Final weight: " + finalW);
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
	
	public static void removeUnreachableStates(AcyclicWeightedAutomaton a){
		//find all states that are non-final but have no outgoing transitions
		boolean found = true;
		while(found){
			found = false;
			for(WeightedState s : a.getStates()){
				if(!s.isAccept() && s.getTransitions().isEmpty() && s != a.initial){
					found = true;//need to go more over all states.
					for(WeightedTransition wt : a.getIncoming(s)){
						//remove that transition for fromState to 
						wt.getFromState().getTransitions().remove(wt);
					}
				}
			}
		}
	}
	
	/**
	 * If performs minus operation on a1 and a2: a1 \cap \not a2
	 *  If a2 is weighted, then
	 * all weights are set to 1 when complement operations performed.
	 * @param a1
	 * @param a2
	 * @return
	 */
	public static AcyclicWeightedAutomaton minus(AcyclicWeightedAutomaton a1, AcyclicWeightedAutomaton a2){
		AcyclicWeightedAutomaton ret;
		if(a1.isEmpty() || a1 == a2){
			ret = BasicAcyclicWeightedAutomaton.makeEmpty();
		} else if (a2.isEmpty()){
			return a1.clone();
		} else {
//			System.out.println("a2 before");
//			System.out.println(a2);
//			System.out.println("Complement");
			a2 = a2.complement();
			//I can do the removal on the unreachable
			//states of a2 since it happens after taking
			//the complemented of completed automaton.
			a2.determinize();
			if(a2.isEmpty()){
				//non-complimented a2 had all strings of that size
				ret = BasicAcyclicWeightedAutomaton.makeEmpty();
			} else {
//			System.out.println("a2 compl");
//			System.out.println(a2);
//			DotToGraph.outputDotFile(a1.toDot(), "minusa1");
//			DotToGraph.outputDotFile(a2.toDot(), "minusa2");
//			System.out.println("Intersection");
			ret = a1.intersection(a2);
			}
		}
		return ret;
	}
	
	public static void normalize(AcyclicWeightedAutomaton a){
		//make sure remove all dead states first
		removeUnreachableStates(a);
		if(a.isEmpty()) return;
		
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
		//System.out.println(queue);

		while(!queue.isEmpty()){
			//process a state and add it predecessors into the queue
			WeightedState curr = queue.remove(queue.size()-1);
			//System.out.println("curr " + curr);
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
				//System.out.println("pw " + pushedWeights[i]);
				i++;
			}
			//find least common multiplier for all weights
			int lcm = lcmArr(pushedWeights);
			//System.out.println("lcm: " + lcm);
			
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
				//System.out.println("inPred: " + inPred);
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
//				ret.determinize();
//				ret.normalize();
//				ret.minimize();
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
					p.addEpsilonTransition(a.getIncoming(p),temp2.initial);
				}
//				temp1.determinize();
//				temp1.normalize();
//				temp1.minimize();
				temp2 = temp1;
				diff--;
			}
			
			//DotToGraph.outputDotFile(ret.toDot(), "ret");

			//DotToGraph.outputDotFile(temp2.toDot(), "temp2");
			//attach temp2 to ret
			for(WeightedState p : ret.getAcceptStates()){
				p.addEpsilonTransition(ret.getIncoming(p),temp2.initial);
			}
		}
		//DotToGraph.outputDotFile(ret.toDot(), "retFinal");
		
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
		removeUnreachableStates(a1);
		removeUnreachableStates(a2);
		AcyclicWeightedAutomaton ret;
		//System.out.println("a22 " + a2);
		if(a1.isEmpty() || a2.isEmpty()){
			//System.out.println(a1.isEmpty() + " " + a2.isEmpty());
			ret =  BasicAcyclicWeightedAutomaton.makeEmpty();
			//System.out.println("Empty " + ret);
			//System.exit(2);
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
				//System.out.println("Old " + sOld);
				//get the corresponding state of the ret automaton
				WeightedState curr = oldToNew.get(sOld);
				//for the pair of edges with the same symbol
				//System.out.println("Old " + sOld.getFirst());
				for(WeightedTransition t1 : sOld.getFirst().getTransitions()){
					//System.out.println("t1 " + t1 + "\n " + sOld.getSecond() + " T " + sOld.getSecond().getTransitions());
					for(WeightedTransition t2: sOld.getSecond().getTransitions()){
						//System.out.println("t2 " + t2);
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
							curr.addTransition(new WeightedTransition(curr, t1.getSymb(), 
									sNew, t1.getWeight().multiply(t2.getWeight())));
						}
					}
				}
		
				
			}
			
			//System.out.println("Normal " + ret);
		}
		
		return ret; 
	}
	
	/**
	 * Complement operation is not defined on Weighted Automata since
	 * "negation is not defined for all semirings" ref. Mohri 
	 * "Weighted Automata Algorithms" p. 19
	 * So the incoming automata is flattened (all weights set to 1), 
	 * completed (all alphabet symbols can be read from any state) and
	 * complimented (final -> nonfinal and vice versa).
	 * 
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
	 * @return 
	 */

	public static AcyclicWeightedAutomaton complement(AcyclicWeightedAutomaton a){
		AcyclicWeightedAutomaton ret  = a.flatten();
				//negate accept to non-accept and vice versa
				for(WeightedState s : ret.getStates()){
					s.setAccept(!s.isAccept());
				}
		return ret;
	}
	
	/**
	 * Creates a flatten copy of the parameter
	 * @param a
	 * @return
	 */
	public static AcyclicWeightedAutomaton flatten (AcyclicWeightedAutomaton a){
		AcyclicWeightedAutomaton ret = a.clone();
		for(WeightedState s : ret.getStates()){
			s.setWeight(new Fraction(1,1));
			for(WeightedTransition t : s.getTransitions()){
				t.setWeight(new Fraction(1,1));
			}
		}
		
		return ret;
	}

//	public static boolean isEmpty(AcyclicWeightedAutomaton a) {
//		//conditions works when we have minimized automaton
//		//since the minimization algorithm would make one 
//		//accepting state
//		//System.out.println("Before R " + a);
//		//a non empty if it can find at least one path from start to a final
//		
//		AcyclicWeightedAutomaton temp = a.clone();
//		removeUnreachableStates(temp);
//		//System.out.println("After R " + a);
//		return !temp.initial.isAccept() && temp.initial.getTransitions().isEmpty();
//	}

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

	/**
	 * Completes a by creating an equivalent automaton to a that all alphabet 
	 * symbols can be read from any state up to the bound.
	 * @param a - the automaton be competed
	 * @param bound - the bound for the string length
	 * @param symbols - the alphabet symbols.
	 * @return
	 */
	public static AcyclicWeightedAutomaton complete(AcyclicWeightedAutomaton a, int bound,
			String symbols) {
		
		AcyclicWeightedAutomaton ret = a.clone();
		//1. create and automaton that accepts
				//all strings up to the bound
				//create a single one
				AcyclicWeightedAutomaton compl = BasicAcyclicWeightedAutomaton.makeCharSet(symbols);
				//DotToGraph.outputDotFile(compl.toDot(), "compl1");
				//create set for abc
				Set<Character> abc = new HashSet<Character>();
				for(WeightedTransition t : compl.initial.getTransitions()){
					abc.add(t.getSymb());
				}
				//repeat it bound times.
				compl = compl.repeat(0, bound);
				//DotToGraph.outputDotFile(compl.toDot(), "compl2");
				//2. negate it
				compl = compl.complement();
				//DotToGraph.outputDotFile(compl.toDot(), "compl3");
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
				completeDFS(depthState, 1, abc, ret.initial, new HashSet<WeightedState>());
		return ret;
	}
	
	private static void completeDFS(Map<Integer, WeightedState> depthState, int depth, Set<Character> abc, WeightedState s, Set<WeightedState> visited){
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

	public static void minimize(AcyclicWeightedAutomaton acyclicWeightedAutomaton) {
//		if(true){
//		System.out.println("Starting min with " + acyclicWeightedAutomaton.getStates().size() + "\t" + acyclicWeightedAutomaton.getStringCount());
//		if(acyclicWeightedAutomaton.getStringCount().intValue() == 55944){
//		DotToGraph.outputDotFile(acyclicWeightedAutomaton.toDot(), "minBefore");
//		}
		//divide into non-final and final states with the same weight
		Set<Set<WeightedState>> groups = new HashSet<Set<WeightedState>>();
		//add an empty one in case no symbols going anywhere
//		Set<WeightedState> empty = new HashSet<WeightedState>();
//		groups.add(empty);
		Set<Pair<Character, Fraction>> symbols = new HashSet<Pair<Character, Fraction>>();
		Set<WeightedState> currStates = acyclicWeightedAutomaton.getStates();
		Map<Fraction, Set<WeightedState>> finalWeights = new HashMap<Fraction, Set<WeightedState>> ();
		Set<WeightedState> nonFinalSet = new HashSet<WeightedState>();
		for(WeightedState state : currStates){
			if(state.isAccept()){
				Fraction acceptW = state.getWeight();
				if(finalWeights.containsKey(acceptW)){
					finalWeights.get(acceptW).add(state);
				} else {
					//create a new set
					Set<WeightedState> acceptSet = new HashSet<WeightedState>();
					acceptSet.add(state);
					finalWeights.put(acceptW, acceptSet);
					//add to groups
					//groups.add(acceptSet); cannot add here - uses the current hash value that will be changed when the set is updated
				}
			} else {
				//System.out.println("nonfinal " + state);
				//add to nonFinal states
				nonFinalSet.add(state);
			}
			//track symbol, transition pair
			for(WeightedTransition t : state.getTransitions()){
				boolean found = false;
				for(Pair<Character,Fraction> tPair : symbols){
					if(tPair.getFirst() == t.getSymb() && tPair.getSecond().equals(t.getWeight())){
						found = true;
						break;
					}
				}
				if(!found){
					//add to the pair list
					//add it
					Pair<Character, Fraction> symb = new Pair<Character, Fraction>(t.getSymb(), t.getWeight());
					symbols.add(symb);
				}
			}
		}
		//add only non-empty sets
		if(!nonFinalSet.isEmpty()){
			groups.add(nonFinalSet); //add the set of non-final states
		}
		if(!finalWeights.values().isEmpty()){
			groups.addAll(finalWeights.values()); //add the set of final states
		}
		
		//System.out.println("symbols " + symbols);
		
		//System.out.println("Initial Groups " + groups);

		//---------- 2nd part finding transitions on each symbol to what group
		//int i = 0;
		Set<Set<WeightedState>> processedGroups = new HashSet<Set<WeightedState>>();
		while(!groups.equals(processedGroups)){
			//copy the old elements
			//prevGroups.addAll(groups);
		//while(!groups.isEmpty() && i < 10){
			//pick one that has not been processed
			Iterator<Set<WeightedState>> inter = groups.iterator();
			Set<WeightedState> group = groups.iterator().next();
			while(processedGroups.contains(group)){
				group = inter.next();
			}
			//System.out.println("Processing " + group);
			//calculate groups each state goes to on each symbol
			Map<Pair<WeightedState, Pair<Character,Fraction>>, Set<WeightedState>> transTable = new HashMap<Pair<WeightedState, Pair<Character,Fraction>>, Set<WeightedState>>();
			for(WeightedState fromState : group){
				for(WeightedTransition wt : fromState.getTransitions()){
					Pair<Character,Fraction> symb = findPair(symbols, wt);
					WeightedState toState = wt.getToState();
					//find the group in which this state in
					Set<WeightedState> toGroup = findGroup(groups, toState);
					//create a tuple for it
					Pair<WeightedState, Pair<Character,Fraction>> arg = new Pair<WeightedState, Pair<Character,Fraction>>(fromState,symb);
					//add it to the map
					transTable.put(arg, toGroup);	
					
				}
			}
			
			//the table is now computed, check whether on the same symbols each group goes to the set of states
			Set<Set<WeightedState>> newGroups = new HashSet<Set<WeightedState>> ();
			newGroups.add(group);
			//System.out.println("newGroups " + newGroups);
			for(Pair<Character, Fraction> symb : symbols){
				//System.out.println("on " + symb);
				Map<Set<WeightedState>, Set<WeightedState>> groupToStates = new HashMap<Set<WeightedState>, Set<WeightedState>>();
				//calculate all fromStates that go on the same symbol to the same group
				for(WeightedState fromS : group){
					Set<WeightedState> toGroup = getToGroup(symb, fromS, transTable);
					//System.out.println("toGroup " + toGroup);
					//check if it is in the key
					Set<WeightedState> addTo;
					if(groupToStates.containsKey(toGroup)){
						//add to the set
						addTo = groupToStates.get(toGroup);
					} else {
						addTo = new HashSet<WeightedState>();
						groupToStates.put(toGroup, addTo);
					}
					addTo.add(fromS);
					
				}
				
				//System.out.println("groupToStates " + groupToStates);
				
				// at this point we will have a set of states that go to the same group on the same symbol
				//as the values of groupsToStates
				//we need to check whether they are the same groups
				Set<Set<WeightedState>> updatedGroups = new HashSet<Set<WeightedState>>();
				for(Set<WeightedState> newGroup : newGroups){
					//System.out.println("oldGroup " + newGroup);
					for(Set<WeightedState> newG : groupToStates.values()){
						Set<WeightedState> splitGroup = new HashSet<WeightedState>();
						splitGroup.addAll(newG);
						splitGroup.retainAll(newGroup);
						updatedGroups.add(splitGroup);
					}
				}
				//at the end update oldGroups to the new partition
				//System.out.println("Updated groups " + updatedGroups);
				newGroups = updatedGroups;
			}
			
			//remove the groups that just has been processed
			//add the new group
			//now we need to add oldGroups and remove group
//			System.out.println("Before " + groups + " " + groups.getClass());
			//System.out.println("Adding " + newGroups);
			if(newGroups.size() == 1 && newGroups.contains(group)){
				//no split happened, then add to the processed
				processedGroups.add(group);
			} else {
				//so a group has been changed, so the previous
				//computations might be incorrect
				//thus clear processedGroup and
				//put them back into the queue
				groups.addAll(processedGroups);
				processedGroups.clear();
			}
			//remove the old group
			groups.remove(group);
			//add new groups
			groups.addAll(newGroups);

			//i++;
		}//while groups are not empty
		
		//System.out.println("iters: " + i);
		//let's test it at least
		//System.out.println("Final " + groups);
		
		//now we have groups, thus we need to establish the start state
		//final states and the transitions between them
		Map<Set<WeightedState>, WeightedState> groupToState = new HashMap<Set<WeightedState>, WeightedState>();
		WeightedState newStart = null;
		for(Set<WeightedState> group : groups){
			WeightedState newState = new WeightedState();
			WeightedState equivState = group.iterator().next();
			if(equivState.isAccept()){
				newState.setAccept(true);
				//set the same weight
				newState.setWeight(equivState.getWeight());
			}
			if(group.contains(acyclicWeightedAutomaton.initial)){
				newStart = newState;
			}
			//add to the map
			groupToState.put(group, newState);
		}
		acyclicWeightedAutomaton.setInitialState(newStart);
		//now add the transitions between them
		for( Entry<Set<WeightedState>, WeightedState> entry : groupToState.entrySet()){
			WeightedState equivState = entry.getKey().iterator().next(); // just get any state
			for(WeightedTransition wt : equivState.getTransitions()){
				WeightedState newToState = groupToState.get(findGroup(groups, wt.getToState()));
				//add a transition between two states
				entry.getValue().addTransition(new WeightedTransition(entry.getValue(), wt.getSymb(), newToState, wt.getWeight()));
			}
		}
		}
		//so we should done here
		
//		System.out.println("Ending min with " +  acyclicWeightedAutomaton.getStates().size() + "\t" + acyclicWeightedAutomaton.getStringCount());
//		if(acyclicWeightedAutomaton.getStringCount().intValue() == 54864){
//		DotToGraph.outputDotFile(acyclicWeightedAutomaton.toDot(), "minAfter");
//		}
	//}

	private static Set<WeightedState> getToGroup(Pair<Character, Fraction> symb, WeightedState fromS,
			Map<Pair<WeightedState, Pair<Character, Fraction>>, Set<WeightedState>> transTable) {
		Set<WeightedState> ret = new HashSet<WeightedState>();
		for(Entry<Pair<WeightedState, Pair<Character, Fraction>>, Set<WeightedState>> entry : transTable.entrySet()){
			if(entry.getKey().getFirst().equals(fromS) && entry.getKey().getSecond().equals(symb)){
				ret = entry.getValue();
			}
		}
		return ret;
	}

	private static Pair<Character, Fraction> findPair(Set<Pair<Character, Fraction>> symbols, WeightedTransition wt) {
		Pair<Character, Fraction> ret = null;
		for(Pair<Character, Fraction> symb : symbols){
			if(symb.getFirst() == wt.getSymb() && symb.getSecond().equals(wt.getWeight())){
				ret = symb;
				break;
			}
		}
			
			if(ret == null){
				System.out.println("Cannot find a group in minimization alg for  " + wt.getSymb() +"\t" + wt.getWeight());
				System.exit(2);
			}
		return ret;
	}

	private static Set<WeightedState> findGroup(Set<Set<WeightedState>> groups, WeightedState toState) {
		Set<WeightedState> ret = null;
		for(Set<WeightedState> group : groups){
			if(group.contains(toState)){
				ret = group;
				break;
			}
		}
		if(ret == null){
			System.out.println("Cannot find a group in minimization alg");
			System.exit(2);
		}
		return ret;
	}
}
