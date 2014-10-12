package edu.ucsb.cs.www.vlab.stranger.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

//import Debugger.Debugger;

import dotparser.*;
import edu.ucsb.cs.www.vlab.stranger.PerfInfo;
import edu.ucsb.cs.www.vlab.stranger.StrangerAutomaton;

//import edu.ucsb.cs.www.vlab.stranger.StrangerAutomaton.Transition;

public class AutoDotFileParser {
	static int id = 0;

	static int debugLevel = 0;

	public static int numOfTracks = 2;
	public static int charLength = 8; // 8 bits for each character
	
	
	static class Debugger {
		public static Writer outputFile = null;

		public static void debug(String output, int dlevel, int actualDLevel){
			if (actualDLevel >= dlevel)
				try {
					if (outputFile != null)
						outputFile.write(output);
					System.out.print(output);
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
	}


	public class Visitor implements DOTParserVisitor {
		int id;
		Map<String, State> string2State;
		Set<String> acceptStates;
		/**
		 * accept is turned on when we parse the dot grammar non-terminal a_list
		 * in a dot grammar non-terminal node_stmt that has "shape=doublecircle"
		 * and turned off when parsing any other shape value. This allows us to
		 * know that the next states to parse are all accept states.
		 */
		boolean accept = false;

		public Visitor(int myId) {
			this.id = myId;
			string2State = new HashMap<String, State>();
			acceptStates = new HashSet<String>();
		}

		/**
		 * converts a MONA binary (ASCII) character with 0s, 1s, and Xs to a
		 * ascendingly sorted list of actual ASCII characters
		 * 
		 * @param MONAChar
		 *            an array of eight 0's, 1's and/or Xs that represents a set
		 *            of ASCII characters i.e. [0, 1, 0, 1, 0, 1, 0, X] = {U,V}
		 * @param index
		 *            : as this is a recursive call, index tells us which
		 *            position in current input MONAchar to process (first call
		 *            use 0)
		 * @return an array of ASCII characters sorted according to ASCII
		 *         sorting. Example param would give [U, V]
		 */
		public char[] MONAMultiTrackCharToCharSet(char[] MONAChar, int index) {
			char[] result = null;
			// Base case (reading last character in oneChar which is the least
			// significant bit)
			if (index == 7) {
				if (MONAChar[index] == '0' || MONAChar[index] == '1') {
					result = new char[1];
					if (MONAChar[index] == '0')
						result[0] = 0;
					else
						result[0] = 1;
				} else {// oneChar[index] == 'x'
					result = new char[2];
					result[0] = 0;
					result[1] = 1;
				}
			} else {
				// get all possible char values from suffix subarray then add
				// current values to it
				char[] prevResult = MONAMultiTrackCharToCharSet(MONAChar,
						(index + 1));
				if (MONAChar[index] == '0' || MONAChar[index] == '1') {
					result = prevResult;
					for (int i = 0; i < prevResult.length; i++)
						result[i] = (char) (result[i] + (((int) MONAChar[index]) - 48)
								* Math.pow(2, (7 - index)));
				} else {// oneChar[index] == 'x'
					result = new char[prevResult.length * 2];
					// first multiply by 0
					for (int i = 0; i < prevResult.length; i++)
						result[i] = prevResult[i];
					// then multiply by 1
					for (int i = 0; i < prevResult.length; i++)
						result[i + prevResult.length] = (char) (prevResult[i] + Math
								.pow(2, (7 - index)));
				}
			}
			return result;
		}

		public char[] MONAMultiTrackCharToCharSet2(char[] MONAChar, int index) {
			char[] result = null;
			// Base case (reading last character in oneChar which is the least
			// significant bit)
			if (index == 15) {
				if (MONAChar[index] == '0' || MONAChar[index] == '1') {
					result = new char[1];
					if (MONAChar[index] == '0')
						result[0] = 0;
					else
						result[0] = 1;
				} else {// oneChar[index] == 'x'
					result = new char[2];
					result[0] = 0;
					result[1] = 1;
				}
			} else {
				// get all possible char values from suffix subarray then add
				// current values to it
				char[] prevResult = MONAMultiTrackCharToCharSet2(MONAChar,
						(index + 1));
				if (MONAChar[index] == '0' || MONAChar[index] == '1') {
					result = prevResult;
					for (int i = 0; i < prevResult.length; i++)
						result[i] = (char) (result[i] + (((int) MONAChar[index]) - 48)
								* Math.pow(2, (15 - index)));
				} else {// oneChar[index] == 'x'
					result = new char[prevResult.length * 2];
					// first multiply by 0
					for (int i = 0; i < prevResult.length; i++)
						result[i] = prevResult[i];
					// then multiply by 1
					for (int i = 0; i < prevResult.length; i++)
						result[i + prevResult.length] = (char) (prevResult[i] + Math
								.pow(2, (15 - index)));
				}
			}
			return result;
		}

		/**
		 * Parses a MONA automaton transition label and returns a list of
		 * automaton transitions corresponding to every range of chars in this
		 * MONA transition label Note that if a transition is labeled with
		 * NON-ASCII chars or ASCII chars that are non-printable then it will
		 * not be added.
		 * 
		 * @param MONA
		 *            transition label that is a matrix or 0s, 1s, and/or Xs
		 * @param the
		 *            automaton state that represents the destination of these
		 *            transitions
		 * @return a list of Transitions (could be empty)
		 */
		public ArrayList<Transition> parseMonaMultiTrackEdge(String edgeLabel,
				State to) {

			int numOfLines = charLength * numOfTracks; // number of lines in a
														// character matrix
			edgeLabel = edgeLabel.substring(1);// remove first " in MONA label
			edgeLabel = edgeLabel.substring(0, edgeLabel.length() - 1);// remove
			// last
			// " in
			// MONA
			// label
			StringTokenizer edgeTokenizer = new StringTokenizer(edgeLabel,
					"\\n");
			// each row in MONALabel is a multi track character (8bits, 16bits,
			// ...etc). Number of rows (height) equals width of label
			char[][] MONALabel = null;
			// for every line in
			// label matrix (num of lines is length of char by num of tracks)
			// fill in the MONAlabel (width and height are for MONALabel)
			for (int width = 0; width < numOfLines; width++) {
				String nextToken = edgeTokenizer.nextToken();
				StringTokenizer lineTokenizer = new StringTokenizer(nextToken,
						" ,");
				if (MONALabel == null)
					MONALabel = new char[lineTokenizer.countTokens()][numOfLines];
				int height = 0;// height of label matrix
				while (lineTokenizer.hasMoreTokens()) {
					String character = lineTokenizer.nextToken();
					MONALabel[height++][width] = character.charAt(0);
				}
			}
			/** debugging **/
			// for (int i = 0; i < MONALabel.length; i++){
			// System.out.print("[" + MONALabel[i][0]);
			// for (int j = 1; j < MONALabel[i].length; j++)
			// System.out.print(", " + MONALabel[i][j]);
			// System.out.println("]\n");
			// }
			// System.out.println("\n");
			/** end debugging **/
			if (edgeTokenizer.hasMoreTokens())
				throw new RuntimeException(
						"There are more than"
								+ numOfLines
								+ " bit in a MONA ASCII char. You need to change character length of numoftracks.");

			// Convert every MONA binary character into a set of actual
			// characters

			// tracks will hold an array for each track "trackSet". this array
			// contains the characters for this track on current edge
			boolean tracks[][] = new boolean[numOfTracks][256];

			for (int t = 0; t < numOfTracks; t++) {
				// every row in MONALabel is a char
				for (int i = 0; i < MONALabel.length; i++) {
					// for each column in mona label matrix (row in MONALabel)
					// break it into multiple tracks (2 or more) then for each
					// character in a track convert into a set of characters
					/** add a track */
					// convert a mona char of one track (8 bits) with 0s, 1s,
					// and/or Xs to a set of characters
					char[] charSet = MONAMultiTrackCharToCharSet(MONALabel[i],
							0);

					/** for debugging **/
					// System.out.print("track"+(t+1)+": ");
					// for (int k = 0; k < charSet.length; k++){
					// System.out.print(Transition.returnCharString(charSet[k])
					// + " ");
					// }
					// System.out.println();
					/** end for debugging **/
					// ignore lambda
					// if (charSet.length == 1 && ((int)charSet[0]) == 255){
					// System.out.print("\n --> lambda ignored");
					// }
					// else
					{
						// ArrayList<Character> charArray = new
						// ArrayList<Character>();
						for (int j = 0; j < charSet.length; j++)
							// only add printable characters
							// if (charSet[j] > 31 && charSet[j] < 127)
							tracks[t][(int) charSet[j]] = true;
						// add the previous set of characters to the whole set
						// of
						// characters
						// trackSet.addAll(charArray);
					}
					// System.out.println();
					// if we read last track no need to shift left
					if ((t + 1) < numOfTracks)
						shiftLeft(MONALabel[i]);

					// /** add second track */
					//
					// // convert a char with 0s, 1s, and/or Xs to a set of
					// characters
					// char[] charSet2 =
					// MONAMultiTrackCharToCharSet(MONALabel[i], 0);
					// System.out.print("track2: ");
					// for (int k = 0; k < charSet2.length; k++){
					// //charSet2[k] = (char)(Math.pow(2, 8) +
					// ((int)charSet2[k]));
					// System.out.print(Transition.returnCharString(charSet2[k])
					// + " ");
					// }
					// // if it is lambda in this track and the corresponding
					// character in the first track is not lambda then ignore
					// lambda
					// // if (charSet2.length == 1 && ((int)charSet2[0]) == 255
					// && !(charSet.length == 1 && ((int)charSet[0]) == 255)){
					// // System.out.print("\n --> lambda ignored");
					// // }
					// // else
					// {
					// ArrayList<Character> charArray = new
					// ArrayList<Character>();
					// for (int j = 0; j < charSet2.length; j++)
					// // only add printable characters
					// //if (charSet[j] > 31 && charSet[j] < 127)
					// charArray.add(charSet2[j]);
					// // add the previous set of characters to the whole set of
					// // characters
					// charSets2.addAll(charArray);
					// }
					// System.out.println("\n");
				}
				/** debugging **/
				// for (int k = 0; k < MONALabel.length; k++){
				// System.out.print("[" + MONALabel[k][0]);
				// for (int j = 1; j < MONALabel[k].length; j++)
				// System.out.print(", " + MONALabel[k][j]);
				// System.out.println("]\n");
				// }
				// System.out.println("\n");
				/** end debugging **/
			}
			/**
			 * The following only works if we have a total ascending order (<
			 * with no equality )according to ASCII on charSets, This is the
			 * case here due to the way we parse the MONA label and construct
			 * the char sets. It will construct transitions and label them with
			 * either range of characters of one character
			 */
			ArrayList<Transition> returnMe = new ArrayList<Transition>();
			int trackNum = 0;
			for (int tr = 0; tr < numOfTracks; tr++) {
				trackNum++;
				boolean trackArray[] = tracks[tr];
				// for (int k = 0;k < trackArray.length; k++)
				// System.out.println(trackArray[k]);
				char first = (char) -1;
				for (int i = 0; i < 256; i++) {
					if (trackArray[i]) {
						if (first == (char) -1) {
							first = (char) i;
						}
					} else if (!(first == (char) -1)) {
						Transition t = new Transition(first, (char) (i - 1),
								trackNum, to);
						// System.out.println("transition with label ["
						// +Transition.returnCharString(first) + "-" +
						// Transition.returnCharString((char)(i-1)) +
						// "] added to track "+trackNum );
						returnMe.add(t);
						// start a new range
						first = (char) -1;
					}
				}
				if (!(first == (char) -1)) {
					// process the last char in the array
					Transition t = new Transition(first, (char) 255, trackNum,
							to);
					// System.out.println("transition with label ["
					// +Transition.returnCharString(first) + "-" +
					// Transition.returnCharString((char)255) +
					// "] added to track "+trackNum );
					returnMe.add(t);
				}
			}

			// if (!charSets2.isEmpty()) { // if all the transitions has only
			// NON-ASCII chars then do not create any transition
			// // first char in an range
			// char first = charSets2.remove(0).charValue();
			// // previously read char (to check if we still have an range)
			// char prev = first;
			// char current = first;
			// for (Character c : charSets2) {
			// current = c.charValue();
			// // this to check that we have the total order
			// assert (current != prev);
			// // if we are still in a range then keep going
			// if (current == (prev + 1)) {
			// prev = current;
			// continue;
			// }
			// // if we finished one range then construct a transition with
			// // this range (may have only one char)
			// else {
			// Transition t = new Transition(first, prev, 2, to);
			// System.out.println("transition with label ["
			// +Transition.returnCharString(first) + "-" +
			// Transition.returnCharString(prev) + "] added to track 2" );
			// returnMe.add(t);
			// // start a new range
			// first = prev = current;
			// }
			// }
			// // process the last char in the array
			// Transition t = new Transition(first, current, 2, to);
			// System.out.println("transition with label ["
			// +Transition.returnCharString(first) + "-" +
			// Transition.returnCharString(current) + "] added to track 2" );
			// returnMe.add(t);
			// }
			// else {
			// System.out.println("second track has no chars");
			// }
			return returnMe;
		}

		/**
		 * removes the current track to process the next one
		 * 
		 * @param cs
		 */
		private void shiftLeft(char[] cs) {
			for (int i = 0; i < cs.length - charLength; i++) {
				cs[i] = cs[i + charLength];
			}
			// for debugging
			// System.out.print("after shift: [");
			// for (int i = 0; i < 8; i++){
			//				
			// System.out.print(cs[i]+", ");
			// }
			// System.out.println("]");

		}

		/**
		 * converts a MONA binary (ASCII) character with 0s, 1s, and Xs to a
		 * ascendingly sorted list of actual ASCII characters
		 * 
		 * @param MONAChar
		 *            an array of eight 0's, 1's and/or Xs that represents a set
		 *            of ASCII characters i.e. [0, 1, 0, 1, 0, 1, 0, X] = {U,V}
		 * @param index
		 *            : as this is a recursive call, index tells us which
		 *            position in current input MONAchar to process (first call
		 *            use 0)
		 * @return an array of ASCII characters sorted according to ASCII
		 *         sorting. Example param would give [U, V]
		 */
		public char[] MONACharToCharSet(char[] MONAChar, int index) {
			char[] result = null;
			// Base case (reading last character in oneChar which is the least
			// significant bit)
			if (index == 7) {
				if (MONAChar[index] == '0' || MONAChar[index] == '1') {
					result = new char[1];
					if (MONAChar[index] == '0')
						result[0] = 0;
					else
						result[0] = 1;
				} else {// oneChar[index] == 'x'
					result = new char[2];
					result[0] = 0;
					result[1] = 1;
				}
			} else {
				// get all possible char values from suffix subarray then add
				// current values to it
				char[] prevResult = MONACharToCharSet(MONAChar, (index + 1));
				if (MONAChar[index] == '0' || MONAChar[index] == '1') {
					result = prevResult;
					for (int i = 0; i < prevResult.length; i++)
						result[i] = (char) (result[i] + (((int) MONAChar[index]) - 48)
								* Math.pow(2, (7 - index)));
				} else {// oneChar[index] == 'x'
					result = new char[prevResult.length * 2];
					// first multiply by 0
					for (int i = 0; i < prevResult.length; i++)
						result[i] = prevResult[i];
					// then multiply by 1
					for (int i = 0; i < prevResult.length; i++)
						result[i + prevResult.length] = (char) (prevResult[i] + Math
								.pow(2, (7 - index)));
				}
			}
			return result;
		}

		private void commonProcessing(SimpleNode node, Visitor visitor) {
			if (node.jjtGetParent() instanceof SimpleNode) {
				int parentId = node.jjtGetParent().hashCode();
				int myId = node.hashCode();
				// Debugger.debug(myId + " [shape=circle,label=\"" +
				// visitor.id++
				// + ":" + node.toString() + "\"];");
				// Debugger.debug(parentId + " -> " + myId);
			} else {
				int myId = node.hashCode();
				// Debugger.debug(myId + " [shape=box,label=\"" + visitor.id++
				// + ":" + node.toString() + "no parent" + "\"];");
			}
		}

		@Override
		public Object visit(SimpleNode node, Object data) {
			commonProcessing(node, this);
			node.childrenAccept(this, data);
			return null;
		}

		@Override
		public Object visit(ASTparse node, Object data) {
			commonProcessing(node, this);
			node.childrenAccept(this, data);
			return null;
		}

		@Override
		public Object visit(ASTgraph node, Object data) {
			commonProcessing(node, this);
			node.childrenAccept(this, data);
			return null;
		}

		@Override
		public Object visit(ASTstmt_list node, Object data) {
			commonProcessing(node, this);
			node.childrenAccept(this, data);
			return null;
		}

		@Override
		public Object visit(ASTStatement node, Object data) {
			commonProcessing(node, this);
			node.childrenAccept(this, data);
			return null;
		}

		@Override
		public Object visit(ASTideq_stmt node, Object data) {
			commonProcessing(node, this);
			node.childrenAccept(this, data);
			return null;
		}

		@Override
		public Object visit(ASTattr_stmt node, Object data) {
			commonProcessing(node, this);
			if (node.jjtGetValue().equals("node")) {
				for (int i = 0; i < node.jjtGetNumChildren(); i++) {
					Node child = node.jjtGetChild(i);
					if (child instanceof ASTattr_list) {
						String childNodeShape = (String) child.jjtAccept(this,
								data);
						if (childNodeShape.equals("doublecircle") || childNodeShape.equals("box"))
							this.accept = true;
						else
							this.accept = false;
					} else {
						// not interested in its data (may be in the subtree
						// data so
						// run accept to visit its subtree)
						child.jjtAccept(this, data);
					}
				}
			} else
				// not interested in specific child data (may be in the subtree
				// data
				// so run accept to visit its subtree)
				node.childrenAccept(this, data);

			return null;
		}

		@Override
		public Object visit(ASTnode_stmt node, Object data) {
			commonProcessing(node, this);
			Automaton auto = (Automaton) data;
			for (int i = 0; i < node.jjtGetNumChildren(); i++) {
				Node child = node.jjtGetChild(i);
				if (child instanceof ASTnode_id) {
					String value = (String) child.jjtAccept(this, data);
					State s = string2State.get(value);
					if (s == null) {
						s = new State();
						string2State.put(value, s);
						if (accept) // an accept state (with a doublecircle
							// shape in MONA dot file)
							s.accept = true;
						try {
							// keep 0 for inits
							s.number = s.id = Integer.parseInt(value);
							if (s.id == 0)
								auto.setInitialState(s);
						} catch (NumberFormatException e) {
							// all states from mona dot files are numbered
							// except a dump initial state called "init"
							if (!value.equals("init"))
								throw new RuntimeException(
										"Parsing Error: A state has a noninteger value but not init");
						}
						Debugger.debug("Node " + value + " has been read\n", 2,
								debugLevel);
					} else {
						throw new RuntimeException("Parsing Error: State "
								+ value
								+ " has been parsed before (duplicate state)");
					}

				} else {
					// not interested in its data (may be in the subtree data so
					// run accept to visit its subtree)
					child.jjtAccept(this, data);
				}
			}

			return null;
		}

		/**
		 * node_id : ID An ID is one of the following: # Any string of
		 * alphabetic ([a-zA-Z\200-\377]) characters, underscores ('_') or
		 * digits ([0-9]), not beginning with a digit; # a numeral [-]?(.[0-9]+
		 * | [0-9]+(.[0-9]*)? ); # any double-quoted string ("...") possibly
		 * containing escaped quotes (\")1; # an HTML string (<...>).
		 */
		@Override
		public Object visit(ASTnode_id node, Object data) {
			commonProcessing(node, this);
			return node.jjtGetValue();
		}

		/**
		 * This is a subset of dot grammar
		 * (http://www.graphviz.org/doc/info/lang.html) that only shows what we
		 * deal with in MONA dot files. Example : 0 -> 0
		 * [label="0\n0\n1\n0\n0\n0\n0\n1"] edge_stmt : node_id edgeRHS [
		 * attr_list ] edgeRHS : edgeop node_id attr_list : '[' a_list ']'
		 * a_list : ID '=' ID node_id : ID edgeop : '->' in directed graphs
		 */
		@Override
		public Object visit(ASTedge_stmt node, Object data) {
			commonProcessing(node, this);
			Automaton auto = (Automaton) data;
			int nodesCount = 0;
			State from = null, to = null;
			Transition t = null;
			String edgeLabel = null;
			for (int i = 0; i < node.jjtGetNumChildren(); i++) {
				Node child = node.jjtGetChild(i);

				// read from state
				if (child instanceof ASTnode_id) {
					String value = (String) child.jjtAccept(this, data);
					State s = string2State.get(value);
					if (s != null) {
						from = s;
						Debugger.debug("from: " + s.id + "\n", 2, debugLevel);
					} else {
						// TODO: not pretty sure about this
						throw new RuntimeException(
								"SNH: a node in an edge is not defined before (MONA always defines nodes before using them)");
					}

					// read to state
				} else if (child instanceof ASTedgeRHS) {
					String value = (String) child.jjtAccept(this, data);
					State s = string2State.get(value);
					if (s != null) {
						to = s;
						Debugger.debug("to: " + s.id + "\n", 2, debugLevel);
					} else {
						// TODO: not pretty sure about this
						throw new RuntimeException(
								"SNH: a node in an edge is not defined before (MONA always defines nodes before using them)");
					}

					// read edge label
				} else if (child instanceof ASTattr_list) {
					edgeLabel = (String) child.jjtAccept(this, data);
					Debugger.debug("Edge: " + edgeLabel + "\n", 2, debugLevel);

					// not interesting information from children in AST
				} else {
					// not interested in current childs data but may be in it
					// subtree in AST so
					// run accept to visit its subtree
					child.jjtAccept(this, data);
				}
			}
			assert (from != null);
			assert (to != null);
			// only one edge: (init -> 0) has no label (null). 'init' is a dummy
			// initial state in MONA automaton
			if (edgeLabel != null) {
				ArrayList<Transition> transitions = null;
				if (numOfTracks == 1)
					transitions = this.parseMonaEdge(edgeLabel, to);
				else
					transitions = this.parseMonaMultiTrackEdge(edgeLabel, to);
				for (Transition tr : transitions)
					from.addTransition(tr);
			} else {
				// there is always an edge from init state to some state with no
				// label
				// t = new Transition(' ', to);
				// from.addTransition(t);
			}
			Debugger.debug(from.id + " -> " + to.id + "\n\n\n\n\n\n", 2,
					debugLevel);
			return null;
		}

		/**
		 * Parses a MONA automaton transition label and returns a list of
		 * automaton transitions corresponding to every range of chars in this
		 * MONA transition label Note that if a transition is labeled with
		 * NON-ASCII chars or ASCII chars that are non-printable then it will
		 * not be added.
		 * 
		 * @param MONA
		 *            transition label that is a matrix or 0s, 1s, and/or Xs
		 * @param the
		 *            automaton state that represents the destination of these
		 *            transitions
		 * @return a list of Transitions (could be empty)
		 */
		public ArrayList<Transition> parseMonaEdge(String edgeLabel, State to) {
			edgeLabel = edgeLabel.substring(1);// remove first " in MONA label
			edgeLabel = edgeLabel.substring(0, edgeLabel.length() - 1);// remove
			// last
			// " in
			// MONA
			// label
			StringTokenizer edgeTokenizer = new StringTokenizer(edgeLabel,
					"\\n");
			char[][] MONALabel = null;
			for (int width = 0; width < 8; width++) {// for every column in
				// label matrix (actual
				// ASCII char)
				String nextToken = edgeTokenizer.nextToken();
				StringTokenizer lineTokenizer = new StringTokenizer(nextToken,
						" ,");
				if (MONALabel == null)
					MONALabel = new char[lineTokenizer.countTokens()][8];
				int height = 0;// height of label matrix
				while (lineTokenizer.hasMoreTokens()) {
					String character = lineTokenizer.nextToken();
					MONALabel[height++][width] = character.charAt(0);
				}
			}
			/** debugging **/
			// for (int i = 0; i < label.length; i++){
			// System.out.print("[" + label[i][0]);
			// for (int j = 1; j < label[i].length; j++)
			// System.out.print(", " + label[i][j]);
			// System.out.println("]");
			// }
			/** end debugging **/
			if (edgeTokenizer.hasMoreTokens())
				throw new RuntimeException(
						"There are more than 8 bit in a MONA ASCII char");

			// Convert every MONA binary character into a set of actual
			// characters
			ArrayList<Character> charSets = new ArrayList<Character>();
			for (int i = 0; i < MONALabel.length; i++) {
				// convert a char with 0s, 1s, and/or Xs to a set of characters
				char[] charSet = MONACharToCharSet(MONALabel[i], 0);
				ArrayList<Character> charArray = new ArrayList<Character>();
				for (int j = 0; j < charSet.length; j++)
					// only add printable characters
					// if (charSet[j] > 31 && charSet[j] < 127)
					charArray.add(charSet[j]);
				// add the previous set of characters to the whole set of
				// characters
				charSets.addAll(charArray);
			}

			/**
			 * The following only works if we have a total ascending order (<
			 * with no equality )according to ASCII on charSets, This is the
			 * case here due to the way we parse the MONA label and construct
			 * the char sets. It will construct transitions and label them with
			 * either range of characters of one character
			 */
			ArrayList<Transition> returnMe = new ArrayList<Transition>();
			if (!charSets.isEmpty()) { // if all the transitions has only
										// NON-ASCII chars then do not create
										// any transition
				// first char in an range
				char first = charSets.remove(0).charValue();
				// previously read char (to check if we still have an range)
				char prev = first;
				char current = first;
				for (Character c : charSets) {
					current = c.charValue();
					// this to check that we have the total order
					assert (current != prev);
					// if we are still in a range then keep going
					if (current == (prev + 1)) {
						prev = current;
						continue;
					}
					// if we finished one range then construct a transition with
					// this range (may have only one char)
					else {
						Transition t = new Transition(first, prev, to);
						returnMe.add(t);
						// start a new range
						first = prev = current;
					}
				}
				// process the last char in the array
				Transition t = new Transition(first, current, to);
				returnMe.add(t);
			}
			return returnMe;
		}

		@Override
		public Object visit(ASTsubgraph node, Object data) {
			commonProcessing(node, this);
			node.childrenAccept(this, data);
			return null;
		}

		@Override
		public Object visit(ASTedgeRHS node, Object data) {
			commonProcessing(node, this);
			String value = null;
			for (int i = 0; i < node.jjtGetNumChildren(); i++) {
				Node child = node.jjtGetChild(i);
				if (child instanceof ASTnode_id) {
					value = (String) child.jjtAccept(this, data);

				} else {
					child.jjtAccept(this, data);
				}
			}

			return value;
		}

		@Override
		public Object visit(ASTedgeop node, Object data) {
			commonProcessing(node, this);
			node.childrenAccept(this, data);
			return null;
		}

		@Override
		public Object visit(ASTattr_list node, Object data) {
			commonProcessing(node, this);
			String attrValue = null;
			for (int i = 0; i < node.jjtGetNumChildren(); i++) {
				Node child = node.jjtGetChild(i);
				if (child instanceof ASTa_list) {
					attrValue = (String) child.jjtAccept(this, data);
				} else {
					// not interested in its data (may be in the subtree data so
					// run accept to visit its subtree)
					child.jjtAccept(this, data);
				}
			}
			return attrValue;
		}

		@Override
		public Object visit(ASTa_list node, Object data) {
			commonProcessing(node, this);
			node.childrenAccept(this, data);
			// StringTokenizer tokenizer = new
			// StringTokenizer((String)node.jjtGetValue(), " ");
			// String[] attr = new String[2];
			// attr[0] = tokenizer.nextToken();// attribute name
			// attr[1] = tokenizer.nextToken();// attribute value

			return node.jjtGetValue();
		}

	}

	public static Automaton testAuto3tracks() {
		Automaton auto = new Automaton();
		State s = new State();
		s.id = s.number = 0;
		auto.setInitialState(s);

		State s1 = new State();
		s1.id = s1.number = 1;
		Transition tr = new Transition('<', '<', 1, s1);
		s.addTransition(tr);
		tr = new Transition((char) 255, (char) 255, 2, s1);
		s.addTransition(tr);
		tr = new Transition((char) 255, (char) 255, 3, s1);
		s.addTransition(tr);

		State s2 = new State();
		s2.id = s2.number = 2;
		tr = new Transition((char) 0, (char) 255, 2, s2);
		s.addTransition(tr);
		tr = new Transition((char) 0, (char) 255, 1, s2);
		s.addTransition(tr);
		tr = new Transition((char) 0, (char) 255, 3, s2);
		s.addTransition(tr);

		State s3 = new State();
		s3.id = s3.number = 3; // s3.accept = true;
		tr = new Transition((char) 0, (char) 255, 1, s3);
		s1.addTransition(tr);
		tr = new Transition((char) 0, (char) 255, 2, s3);
		s1.addTransition(tr);
		tr = new Transition((char) 0, (char) 255, 3, s3);
		s1.addTransition(tr);
		tr = new Transition((char) 0, (char) 255, 2, s3);
		s2.addTransition(tr);
		tr = new Transition((char) 0, (char) 255, 3, s3);
		s2.addTransition(tr);
		tr = new Transition((char) 0, (char) 255, 1, s3);
		s2.addTransition(tr);

		State s4 = new State();
		s4.id = s4.number = 4;
		s4.accept = true;
		tr = new Transition((char) 255, (char) 255, 1, s4);
		// s3.addTransition(tr);
		tr = new Transition((char) 255, (char) 255, 2, s4);
		// s3.addTransition(tr);
		tr = new Transition((char) 255, (char) 255, 3, s4);
		// s3.addTransition(tr);

		// State s5 = new State();
		// s5.id = s5.number = 3; //s3.accept = true;
		// tr = new Transition((char)255, (char)255, 1, s5);
		// s1.addTransition(tr);
		// tr = new Transition('<', '<', 2, s5);
		// s1.addTransition(tr);

		return auto;

	}

	public static Automaton testAuto() {
		Automaton auto = new Automaton();
		State s = new State();
		s.id = s.number = 0;
		auto.setInitialState(s);

		State s1 = new State();
		s1.id = s1.number = 1;
		Transition tr = new Transition((char) 255, (char) 255, 1, s1);
		s.addTransition(tr);
		tr = new Transition('S', 'S', 2, s1);
		s.addTransition(tr);

		State s2 = new State();
		s2.id = s2.number = 2;
		tr = new Transition((char) 255, (char) 255, 2, s2);
		s.addTransition(tr);
		tr = new Transition('<', '<', 1, s2);
		s.addTransition(tr);

		State s3 = new State();
		s3.id = s3.number = 3; // s3.accept = true;
		tr = new Transition((char) 255, (char) 255, 1, s3);
		s1.addTransition(tr);
		tr = new Transition('<', '<', 2, s3);
		s1.addTransition(tr);
		tr = new Transition((char) 255, (char) 255, 2, s3);
		s2.addTransition(tr);
		tr = new Transition('<', '<', 1, s3);
		s2.addTransition(tr);

		State s4 = new State();
		s4.id = s4.number = 4;
		s4.accept = true;
		tr = new Transition((char) 255, (char) 255, 1, s4);
		s3.addTransition(tr);
		tr = new Transition((char) 255, (char) 255, 2, s4);
		s3.addTransition(tr);

		// State s5 = new State();
		// s5.id = s5.number = 3; //s3.accept = true;
		// tr = new Transition((char)255, (char)255, 1, s5);
		// s1.addTransition(tr);
		// tr = new Transition('<', '<', 2, s5);
		// s1.addTransition(tr);

		return auto;

	}

	public static void setUpStrangerLibrary() throws Exception {
		StrangerAutomaton.initialize(true);
		PerfInfo perfInfo = new PerfInfo();
		StrangerAutomaton.perfInfo = perfInfo;
		StrangerAutomaton
				.appendCtraceFile("/tmp/stranger_automaton_exec_trace.c");
	}

	public static void tearDownStrangerLibrary() throws Exception {
		StrangerAutomaton.closeCtraceFile();
	}

	private boolean empty;

	/**
	 * @param args
	 *            : args[1]: the dot file for MONA automaton (better to be in
	 *            test folder in the project), args[2]: the file to write result
	 *            of mincut to args[3]: number of tracks Note that the file to
	 *            dump automaton to for debugging will automatically be inferred
	 * @throws ParseException
	 * @throws IOException
	 */
	public StrangerAutomaton parseDotFile(String fileName /*args[1]*/, int numberOfTracks /*args[3]*/) throws ParseException, IOException {
			//setUpStrangerLibrary();
			/**
			 * Construct parse tree
			 */
			DOTParser parser;
			fileName = removeEmptyStringFromDotFile(fileName);
			parser = new DOTParser(new FileReader(fileName));
			SimpleNode root = parser.parse();
			
			/** set number of tracks **/
			numOfTracks = numberOfTracks;
			
			/**
			 * create a visitor and use it to iterate over AST and build the
			 * automaton graph
			 **/
			Visitor graphVisitor = new AutoDotFileParser().new Visitor(0);
			Automaton auto = new Automaton();
			root.childrenAccept(graphVisitor, auto);
			// now garbage collect all states not reachable from initial state
			graphVisitor.string2State = null;
			graphVisitor = null;
			parser = null;
			root = null;
			
			
			/**
			 * Convert Automaton into StrangerAutomaton
			 */
			TreeSet<StrangerAutomaton.State> states = new TreeSet<StrangerAutomaton.State>();
			TreeSet<StrangerAutomaton.Transition> transitions = new TreeSet<StrangerAutomaton.Transition>();
			for (State s : auto.getStates()) {
				StrangerAutomaton.State state = new StrangerAutomaton.State(
						s.id, s.accept);
				states.add(state);
				for (Transition t : s.getSortedTransitionArray(false)) {
					if (t.min > t.max)
						throw new RuntimeException();
					StrangerAutomaton.Transition trans = new StrangerAutomaton.Transition(
							s.id, t.to.id, t.min, t.max);
					transitions.add(trans);
				}
			}
			//dumpAuto(fileName.substring(0, fileName.length() - 4) + ".AUTO.dot",
			//		auto);
			auto = null;
			for (StrangerAutomaton.Transition t : transitions) {
				StringBuilder b = new StringBuilder();
				Transition.appendCharString(t.start, b);
				String start = b.toString();
				b = new StringBuilder();
				Transition.appendCharString(t.finish, b);
				String finish = b.toString();
//				System.out.println(t.src + " --> " + t.dest + " : " + start
//						+ "-" + finish);
			}
			StrangerAutomaton sAuto1 = null;

			sAuto1 = StrangerAutomaton.makeFromAnotherAuto(states, transitions, empty );
			
			return sAuto1;

			
//			if (sAuto1.checkIntersection(sAuto2))
//				System.out.println("\n\nIntersection is NOT empty.");
//			else
//				System.out.println("\n\nIntersection is empty.");
//			StrangerAutomaton sAutoIntersect = sAuto1.intersect(sAuto2);
//			if (!sAutoIntersect.checkEmptiness())
//				System.out.println("\n\n\nExample: "
//						+ sAutoIntersect.generateSatisfyingExample());
//			
			
//			Debugger.outputFile.close();
	}
	
	private String removeEmptyStringFromDotFile(String fileName) {
		String newFileName = fileName.substring(0, fileName.length()-4).concat("-AUTO.dot");
		InputStream in = null;
		OutputStreamWriter out = null;
		 try {
			 in = new FileInputStream(fileName);
		   InputStreamReader inR = new InputStreamReader( in );
		   BufferedReader buf = new BufferedReader( inR );
		   out = new OutputStreamWriter(new FileOutputStream(newFileName));
		   String line;
		   String script = "";
		   while ( ( line = buf.readLine() ) != null ) {
			   script += (line + "\n");
		   }
		   String result = script.substring(script.lastIndexOf('}')+2);
		   empty = Boolean.parseBoolean(result.substring(result.indexOf(':') + 1, result.lastIndexOf('\n')));
		   script = script.substring(0, script.lastIndexOf('}')+1);
//		   if (result != null)
//			   out.write(result);
//		   else
			   out.write(script);
		   out.flush();
		 } catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			 if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			 if (out != null)
				try {
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		 }
		return newFileName;
		
	}

	@SuppressWarnings("unused")
	private static void dumpAuto(String fileName, Automaton auto)
			throws IOException {
		Writer output;
		File autoFile = new File(fileName);
		output = new BufferedWriter(new FileWriter(autoFile));
		output.write(auto.toDot());
		output.close();
	}

}
