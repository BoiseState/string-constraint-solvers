package edu.ucsb.cs.www.vlab.stranger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import dotparser.ParseException;



import edu.ucsb.cs.www.vlab.stranger.RegExp;
import edu.ucsb.cs.www.vlab.stranger.StrangerLibrary.StrangerDFA;
import edu.ucsb.cs.www.vlab.stranger.StrangerLibrary._DFAFiniteLengths;
import edu.ucsb.cs.www.vlab.stranger.StrangerLibrary.transition;
import edu.ucsb.cs.www.vlab.stranger.util.AutoDotFileParser;



/**
 * <p>This is a wrapper for Stranger Automaton Library written in c by Fang Yu&lt;yuf@cs.ucsb.edu&gt;.<br>
 * The actual dfa resides in c library memory an pointed to by dfa field here.</p>
 * <b>Usage:</b><br>
 * <b>---------</b><br>
 * <ul>
 * <li>First you have to initialize the c automata library by setting up the system property:
 * {@code System.setProperty("jna.library.path",} <i>path to libstranger.so</i>{@code )} or 
 * passing the path as a VM argument with -Djna.library.path="...". Then
 * call {@code initialize()}.</li>
 * <li>Then prepare performance collector:<br>
 * {@code perfInfo = new PerfInfo();}<br>{@code StrangerAutomaton.perfInfo = perfInfo;}
 * </li>
 * <li>Then set traceID to 0:<br>
 * {@code StrangerAutomaton.traceID = 0;}
 * </li>  
 * <li>Then you have to provide a c trace file name to get a c trace of the automata operation:<br>
 * {@code String fileName = ...}<br>
 * {@code StrangerAutomaton.openCtraceFile(fileName);}<br>
 * This is important for debugging as Java debugger can not interact with the c library.</li>
 * <li> Finally, to construct a new automaton from a string you have to use one of the factory methods 
 * that have "make" prefix such as {@code makeString()}.</li>
 * </ul> 
 * <b>Warning:</b><br>
 * <b>-----------</b><br>
 * <p>Make sure that no two StrangerAutomata point to the same dfa.
 * If one of the two is garbage collected then its dfa will
 * be free in c library and the other one will have a dfa that
 * is not synchronized anymore and upon its release by the 
 * garbage collector, freeing its dfa may cause problems as
 * it will try to fee an empty memory.</p> 
 * 
 * @author Muath Alkhalaf &lt;muath@cs.ucsb.edu&gt;
 */

public class StrangerAutomaton {
	static int count=0;
	protected Pointer dfa;

	// only for debugging purposes. It should be the id of the node which
	// this auto is associated with
	protected int ID;

	public int getID() {
		return ID;
	}

	public void setID(int id) {
		ID = id;
	}
	
	/**
	 * This is true if the automaton is the top element of the lattice. top > Sigma*.
	 * This is used for variables that have non string type.
	 * Do not use if the variable does not have any type (uninitialized).
	 */
	protected boolean top = false;
	
	/**
	 * This is true if the automaton is the bottom element of the lattice. bottom = phi.
	 * This is used for variables that have no type.
	 * Use in dynamically typed languages when a variable does not have any type (uninitialized).
	 */
	protected boolean bottom = false;
	
	/**
	 * true if L(auto) contains empty string
	 * just a workaround
	 */
	protected boolean empty = false;
	
	/**
     * used to collect performance info
     */
	public static PerfInfo perfInfo;

	static char slash = '/';

	// if this automaton represents one string then we can assign it here it we
	// have it
	protected String str;

	
	protected static Map<Character, Character> encoding; 
	/**
	 * this is set to true when automaton library is initialized. This is to
	 * avoid initializing more than once.
	 */
	private static boolean initialized = false;

	private static boolean coarseWidening = false;

	/**
	 * initializes StrangerAutomaton native shared library before using it. If
	 * you need JNA to protect JVM stack from native code failures then set protected
	 * to true.
	 * 
	 * @param _protected
	 *            : weather JVM is protected by JNA from native code failures or
	 *            not.
	 * @param libraryPath
	 *            : the path to the native library. DO NOT include library name
	 *            here.
	 */
	public static void initialize(boolean _protected) {
		// TODO:
		// there is may be a better place to initialize the library
		// This is called now from within StrangerSanitAnalysis
		try {
			if (System.getProperty("jna.library.path") == null) {
				System.out
						.println("************************************************************************");
				System.out
						.println("jna.library.path variables is not set so default value will be used.");
				System.out
						.println("************************************************************************");
				System.setProperty("jna.library.path",
						System.getenv("STRANGER_LIBRARY_PATH"));
			}
			StrangerLibraryWrapper.initialize(_protected);
			initialized = true;
		} catch (Exception e) {
			System.err.println("Can not initialize c library.");
			e.printStackTrace();
//			throw new RuntimeException(
//					"Pixy: Could not load native library with JNA. "
//							+ e.getMessage());
		}
	}

	protected StrangerAutomaton(Pointer dfa) {
		this.dfa = dfa;
		this.autoTraceID = traceID++;
	}

	public Pointer getDfa() {
		return this.dfa;
	}

	/**
	 * if this automaton represents one string then we can assign it here if we
	 * have it.
	 * 
	 * @param s
	 */
	public void setStr(String s) {
		this.str = s;
	}

	/**
	 * if this automaton represents one string then return this string if known.
	 * 
	 * @param s
	 */
	public String getStr() {
		return this.str;
	}
	
	@Override
	/**
	 * @return "Top" if isTop, "Bottom" if isBottom, str.toString if str != null 
	 * (str is internal string value) else super.toString()
	 */
	public String toString(){
		if (isTop()) return "Top";
		else if (isBottom()) return "Bottom";
		else if (this.str != null)
			return str;
		else 
			return super.toString() + " --points-to-> " + this.dfa.toString();
	}

	public StrangerAutomaton clone(int id) {
		debug(id + " = clone(" + this.ID + ")");
		if (isBottom()) 
			return makeBottom(id);
		else if (isTop())
				return makeTop(id);
		else {
			
			debugToFile("M[" + traceID + "] = dfaCopy(M["+this.autoTraceID+"]);//" + id + " = clone(" + this.ID + ")");
			
			StrangerAutomaton retMe = new StrangerAutomaton(
					StrangerLibrary.INSTANCE.dfaCopy(this.dfa));

			retMe.setStr(this.getStr());
			{

				retMe.setID(id);
				retMe.debugAutomaton();
			}
			return retMe;
		}
	}
	
	public StrangerAutomaton clone(){
		return this.clone(-1);
	}

	/**
	 * This is needed to free the dfa in the shared library that is wrapped by
	 * this class before it is garbage collected
	 */
	public void finalize() {
		Free();
	}
	
	public void Free() {
		try {
			// free the c library memory
			if (this.dfa != null){
				debugToFile("dfaFree(M["+this.autoTraceID+"]);\nM["+this.autoTraceID+"] = 0;");
				StrangerLibraryWrapper.dfaFree(this.dfa);
				this.dfa = null;
			}
		} catch (Throwable e) {
			System.err.println("Could not free a dfa. " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	//***************************************************************************************
	//*                                  Factory Operations                                 *
	//*									--------------------								*
	//*	These operations construct automata from strings not from other automata			*
	//***************************************************************************************

	
	/**
	 * Creates a new automaton that holds the bottom value of the String Analysis lattice. 
	 * (bottom < phi). This artificial bottom is used to make things faster as we do not 
	 * need to create a native c library DFA which holds language phi - the empty language.
	 * If you need to actually make the language phi then refer to {@link makeAnyString} 
	 * and {@link complement}.
	 * This method should be used for:
	 * 1- Variables of type string if they are uninitialized and the 
	 * semantics of the language consider them to hold an unknown or null value. 
	 * 2- Variables with unknown type in dynamically typed languages. In this case as soon 
	 * as type is detected then we should use another factory method.
	 * @param id
	 *            : id of node associated with this auto; used for debugging
	 *            purposes only
	 */
	public static StrangerAutomaton makeBottom(int id){
		debug(id + " = makeBottom()");
		StrangerAutomaton retMe =  new StrangerAutomaton(null);
		retMe.str = null;
		{
			retMe.setID(id);
		}
		retMe.bottom = true;
		return retMe;
	}
	
	/**
	 * Creates a new automaton that holds the bottom value of the String Analysis lattice. 
	 * (bottom < phi). This artificial bottom is used to make things faster as we do not 
	 * need to create a native c library DFA which holds language phi - the empty language.
	 * If you need to actually make the language phi then refer to {@link makeAnyString} 
	 * and {@link complement}.
	 * This method should be used for:
	 * 1- Variables of type string if they are uninitialized and the 
	 * semantics of the language consider them to hold an unknown or null value. 
	 * 2- Variables with unknown type in dynamically typed languages. In this case as soon 
	 * as type is detected then we should use another factory method.
	 */
	public static StrangerAutomaton makeBottom(){
		return makeBottom(traceID);
	}
	
	/**
	 * Creates a new automaton that holds the top value of the String Analysis lattice 
	 * which is undefined (undefined > Sigma*).
	 * It is used for variables in dynamically typed languages which may change their type from 
	 * a string type to another nonstring type. 
	 * If you need to create Sigma* (the actual top) then refer to {@link makeAnyString}.
	 * Joining a variable x of type string with itself should yield top
	 * only if the other copy of the variable has a different type (hold top
	 * value).
	 * If a variable is declared but with no value then it should be bottom.
	 * @param id
	 *            : id of node associated with this auto; used for debugging
	 *            purposes only
	 */
	public static StrangerAutomaton makeTop(int id){
		debug(id + " = makeTop()");
		StrangerAutomaton retMe =  new StrangerAutomaton(null);
		retMe.top = true;
		retMe.str = null;
		{
			retMe.setID(id);
		}
		return retMe;
	}
	
	/**
	 * Creates a new automaton that holds the top value of the String Analysis lattice 
	 * which is undefined (undefined > Sigma*).
	 * It is used for variables in dynamically typed languages which may change their type from 
	 * a string type to another nonstring type. 
	 * If you need to create Sigma* (the actual top) then refer to {@link makeAnyString}.
	 * Joining a variable x of type string with itself should yield top
	 * only if the other copy of the variable has a different type (hold top
	 * value).
	 * If a variable is declared but with no value then it should be bottom.
	 */
	public static StrangerAutomaton makeTop(){
		return makeTop(traceID);
	}
	
	/**
	 * Creates an automaton that accepts exactly the given string. It also
	 * assigns this string to StrangerAutomaton.str. If parameter s is empty
	 * string then it will call StrangerAutomaton.makeEmptyString
	 * @param id
	 *            : id of node associated with this auto; used for debugging
	 *            purposes only
	 * */
	public static StrangerAutomaton makeString(String s, int id) {
		debug(id + " = makeString(" + s + ")");

		StrangerAutomaton retMe;
		// We need to set the string explicitly because the current way we deal
		// with replace is with a string parameter for replace instead of an
		// automaton. Until we have a replace function with the replace
		// parameter
		// as a string we will keep this.

		// if the string is empty then make sure you generate an empty string
		// automaton
		// cause empty string needs special treatment
		if (s.isEmpty()) {
			return StrangerAutomaton.makeAnyStringL1ToL2(0, 0);
		} else {
			
			debugToFile("M[" + traceID + "] = dfa_construct_string(\"" + escapeSpecialChars(s) + "\", NUM_ASCII_TRACKS, indices_main);//" + id + " = makeString(" + escapeSpecialChars(s) + ")");
			
			
			//String encodedString = getEncodedString(s);
			retMe = new StrangerAutomaton(StrangerLibrary.INSTANCE
					.dfa_construct_string(s, StrangerLibraryWrapper.NUM_ASCII_TRACKS,
							StrangerLibraryWrapper.indices_main));
			retMe.setStr(s);
			
			perfInfo.stringLength += s.length();	
						
			{
				retMe.setID(id);
				retMe.debugAutomaton();
			}
			return retMe;
		}

	}
	
	/**
	 * Creates an automaton that accepts exactly the given string. It also
	 * assigns this string to StrangerAutomaton.str. If parameter s is empty
	 * string then it will call StrangerAutomaton.makeEmptyString 
	 * */
	public static StrangerAutomaton makeString(String s) {
		return makeString(s, traceID);
	}

	//TODO: encodedString is used for abstraction and it is not implemented yet.
	public static String getEncodedString(String origStr) {
		StringBuilder retMe = new StringBuilder();
		for (int i = 0; i < origStr.length(); i++){
			char c = origStr.charAt(i);
			char result;
			if (encoding.containsKey(c))
				result = encoding.get(c).charValue();
			else
				result = (char) 0;
			retMe.append(result);
		}
		return retMe.toString();
	}

	/**
	 * Creates an automaton that accepts exactly the given character. It also
	 * assigns this character to StrangerAutomaton.str
	 * @param id
	 *            : id of node associated with this auto; used for debugging
	 *            purposes only
	 * */
	public static StrangerAutomaton makeChar(char c, int id) {
		debug(id + " = makeChar(" + c + ") -- start");

		StrangerAutomaton retMe = StrangerAutomaton.makeString(Character
				.toString(c), id);

		debug(id + " = makeChar(" + c + ") -- end");
		{
			retMe.setID(id);
			retMe.debugAutomaton();
		}
		return retMe;
	}
	
	/**
	 * Creates an automaton that accepts exactly the given character. It also
	 * assigns this character to StrangerAutomaton.str
	 * */
	public static StrangerAutomaton makeChar(char c) {
		return makeChar(c, traceID);
	}

	/**
	 * returns an automaton that accepts a single character in the range between
	 * from and to
	 * @param id
	 *            : id of node associated with this auto; used for debugging
	 *            purposes only
	 */
	public static StrangerAutomaton makeCharRange(char from, char to, int id) {
		debug(id + " = makeCharRange(" + from + ", " + to + ")");

		debugToFile("M[" + traceID + "] = dfa_construct_range('" + from + "', '" + to + "', NUM_ASCII_TRACKS, indices_main);//" + id + " = makeCharRange(" + from + ", " + to + ")");
		
		StrangerAutomaton retMe = new StrangerAutomaton(
				StrangerLibrary.INSTANCE.dfa_construct_range(from, to,
						StrangerLibraryWrapper.NUM_ASCII_TRACKS,
						StrangerLibraryWrapper.indices_main));
		
		{
			retMe.setID(id);
			retMe.debugAutomaton();
		}
		return retMe;
	}

	/**
	 * returns an automaton that accepts a single character in the range between
	 * from and to
	 */
	public static StrangerAutomaton makeCharRange(char from, char to) {
		return makeCharRange(from, to, traceID);
	}
	
	/**
	 * Creates an automaton that accepts any string including empty string (.*)
	 * @param id
	 *            : id of node associated with this auto; used for debugging
	 *            purposes only
	 * */
	public static StrangerAutomaton makeAnyString(int id) {
		debug(id + " = makeAnyString()");

		debugToFile("M[" + traceID + "] = dfaAllStringASCIIExceptReserveWords(NUM_ASCII_TRACKS, indices_main);//" + id + " = makeAnyString()");
		
//		StrangerAutomaton retMe = new StrangerAutomaton(
//				StrangerLibrary.INSTANCE.dfaAllStringASCIIExceptReserveWords(
//						StrangerLibraryWrapper.NUM_ASCII_TRACKS,
//						StrangerLibraryWrapper.indices_main));
		StrangerAutomaton retMe = new StrangerAutomaton(
				StrangerLibrary.INSTANCE.dfaASCIIOnlyNullString(
						StrangerLibraryWrapper.NUM_ASCII_TRACKS,
						StrangerLibraryWrapper.indices_main));
		retMe = retMe.complement().union(StrangerAutomaton.makeAnyStringL1ToL2(0, 0));
		retMe.setStr(null);
		retMe.empty = true;
				
		{
			retMe.setID(id);
			retMe.debugAutomaton();
		}
		return retMe;
	}
	
	/**
	 * Creates an automaton that accepts any string including empty string (.*)
	 * */
	public static StrangerAutomaton makeAnyString() {
		return makeAnyString(traceID);
	}

	/**
	 * Creates an automaton that accepts everything (.*) within the length from
	 * l1 to l2 l2 = -1, indicates unbounded upperbound l1 = -1, indicates
	 * unbounded lowerbound StrangerAutomaton.str will be assigned null.
	 * @param id
	 *            : id of node associated with this auto; used for debugging
	 *            purposes only
	 */
	// TODO: check to see if l1 is allowed to be 0
	public static StrangerAutomaton makeAnyStringL1ToL2(int l1, int l2, int id) {
		debug("makeAnyStringL1ToL2(" + l1 + "," + l2 + ")");
		debugToFile("M[" + traceID + "] = dfaSigmaC1toC2("+l1+","+l2+",NUM_ASCII_TRACKS, indices_main);//" + id + " = dfaSigmaC1toC2()");

		StrangerAutomaton retMe = new StrangerAutomaton(
				StrangerLibrary.INSTANCE.dfaSigmaC1toC2(l1, l2,
						StrangerLibraryWrapper.NUM_ASCII_TRACKS,
						StrangerLibraryWrapper.indices_main));
		retMe.setStr(null);
		if (l1 == 0 || l1 == -1)
			retMe.empty = true;
		{
			retMe.debugAutomaton();
		}
		return retMe;
	}
	
	/**
	 * Creates an automaton that accepts everything (.*) within the length from
	 * l1 to l2 l2 = -1, indicates unbounded upperbound l1 = -1, indicates
	 * unbounded lowerbound StrangerAutomaton.str will be assigned null.
	 */
	public static StrangerAutomaton makeAnyStringL1ToL2(int l1, int l2) {
		return makeAnyStringL1ToL2(l1, l2, traceID);
	}

	/**
	 * Creates an automaton that accepts only the empty string "epsilon". It
	 * also assigns empty string ("") to StrangerAutomaton.str to be used later
	 * with autoToString method.
	 * @param id
	 *            : id of node associated with this auto; used for debugging
	 *            purposes only
	 * @return
	 */
	public static StrangerAutomaton makeEmptyString(int id) {
		debug(id + " = makeEmptyString()");

		debugToFile("M[" + traceID + "] = dfaASCIIOnlyNullString(NUM_ASCII_TRACKS, indices_main);//" + id + " = makeEmptyString()");
		
//		StrangerAutomaton retMe = new StrangerAutomaton(
//				StrangerLibrary.INSTANCE.dfaASCIIOnlyNullString(
//						StrangerLibraryWrapper.NUM_ASCII_TRACKS,
//						StrangerLibraryWrapper.indices_main));
		// constructs language phi (empty lang)
		StrangerAutomaton retMe = new StrangerAutomaton(
				StrangerLibrary.INSTANCE.dfaASCIINonString(
				StrangerLibraryWrapper.NUM_ASCII_TRACKS,
				StrangerLibraryWrapper.indices_main));
		retMe.empty = true;
		// We need to set the string explicitly because the current way we deal
		// with replace is with a string parameter for replace instead of an
		// automaton. Until we have a replace function with the replace
		// parameter
		// as a string we will keep this.
		retMe.setStr("");
				
		{
			retMe.setID(id);
			retMe.debugAutomaton();
		}
		return retMe;
	}
	
	/**
	 * Creates an automaton that accepts only the empty string "epsilon". It
	 * also assigns empty string ("") to StrangerAutomaton.str to be used later
	 * with autoToString method.
	 * @return
	 */
	public static StrangerAutomaton makeEmptyString() {
		return makeEmptyString(traceID);
	}

	/**
	 * creates an automaton that represents a dot (.) in a regular expressions.
	 * Dot means single character of any value in alphabet.
	 * @param id
	 *            : id of node associated with this auto; used for debugging
	 *            purposes only
	 * @return
	 */
	public static StrangerAutomaton makeDot(int id) {
		debug(id + " = makeDot()");
		
		debugToFile("M[" + traceID + "] = dfaDot(NUM_ASCII_TRACKS, indices_main);//"+ id + " = makeDot()");

		StrangerAutomaton retMe = new StrangerAutomaton(
				StrangerLibrary.INSTANCE.dfaDot(
						StrangerLibraryWrapper.NUM_ASCII_TRACKS,
						StrangerLibraryWrapper.indices_main));
				
		{
			retMe.setID(id);
			retMe.debugAutomaton();
		}
		return retMe;
	}

	/**
	 * creates an automaton that represents a dot (.) in a regular expressions.
	 * Dot means single character of any value in alphabet.
	 * @return
	 */
	public static StrangerAutomaton makeDot() {
		return makeDot(traceID);
	}
	
	/**
	 * creates an automaton that accepts nothing, not even empty string
	 * @param id
	 *            : id of node associated with this auto; used for debugging
	 *            purposes only
	 * */
	public static StrangerAutomaton makePhi(int id) {
		debug(id + " = makePhi");

		StrangerAutomaton retMe = makeBottom(id);
		return retMe;
	}
	
	/**
	 * creates an automaton that accepts nothing, not even empty string 
	 * */
	public static StrangerAutomaton makePhi() {
		return makePhi(traceID);
	}
	/**
	 * Represent a state in an automaton with an id and either an accept or reject status
	 * This class is only used when constructing a new stranger automaton from an existing 
	 * one by calling {@code makeFromAnotherAuto()}.
	 * @author muath
	 *
	 */
	public static class State implements Comparable<State>{
		public int id;
		boolean accept;
		
		/**
		 * Construct a new state object
		 * @param id: id of the state.
		 * @param accept: if the state is an accepting state (true) or rejecting state (false)
		 */
		public State(int id, boolean accept){
			this.id = id;
			this.accept = accept;
		}
		
		@Override
		/**
		 * Two states are the same if they have the same id
		 */
		public boolean equals(Object s){
			if (s == null)
				return false;
			return (((State)s).id == this.id);
		}
		
		@Override
		public int compareTo(State s){
			if (s == null)
				return 0;
			return (new Integer(this.id)).compareTo(new Integer(s.id));
		}
	}
	
	/**
	 * Represents a transition (an edge) between two states along with its alphabet label 
	 * represented as a range between two characters {@code start} and {@code finish}.
	 * A transition object must confirm to the invariant {@code start<=finish}.
	 * This class is only used when constructing a new stranger automaton from an existing 
	 * non-stranger one by calling {@code makeFromAnotherAuto()}.
	 * From a given state, there must not be two equal transitions out of it (does not support
	 * NFA).
	 * Two transitions are equal if:
	 * 1- They have the same start state.
	 * 2- Their alphabet ranges are not disjoint
	 * So the following transitions are equal: 
	 * (0,1,a,n) == (0,1,a,n) == (0,1,c,d) == (0,3,n,m) == (0,0,a,n)
	 * @author muath
	 */
	public static class Transition implements Comparable<Transition>{
		public int src;
		public int dest;
		public char start;
		public char finish;
		
		/**
		 * Construct a new Transition that must fulfill the invariant (start<=finish)
		 * @param src: id of source state
		 * @param dest: id of destination state
		 * @param start: first character in the alphabet range
		 * @param finish: last character in the alphabet range
		 */
		public Transition(int src, int dest, char start, char finish){
			this.src = src;
			this.dest = dest;
			if (start>finish){
				throw new StrangerAutomatonException("Trying to construct a transition with a range where the start of the range is grater than the finish");
			}
			this.start = start;
			this.finish = finish;
		}
		
		@Override
		/**
		 * Two transitions are equal if:
		 * 1- They have the same start state.
		 * 2- Their alphabet ranges are not disjoint
		 * So the following transitions are equal: 
		 * (0,1,a,n) == (0,1,a,n) == (0,1,c,d) == (0,3,n,m) == (0,0,a,n)
		 */
		public boolean equals(Object t){
			if (t == null)
				return false;
			Transition tr = (Transition)t;
			if (tr.src != this.src)
				return false;
			if (this.start > tr.finish || this.finish < tr.start)
				return false;
			return true;
		}
		
		@Override
		/**
		 * Two transitions are equal if:
		 * 1- They have the same start state.
		 * 2- Their alphabet ranges are not disjoint
		 * So the following transitions are equal: 
		 * (0,1,a,n) == (0,1,a,n) == (0,1,c,d) == (0,3,n,m) == (0,0,a,n)
		 * For two transitions t1, t2 we return t1 < t2 is the range of t1 is less than 
		 * the range of t2 in ASCII table.
		 */
		public int compareTo(Transition tr){
			if (tr == null)
				return -1;
			if (tr.src != this.src)
				return (new Integer(this.src)).compareTo(new Integer(tr.src));
			
			// if source states are the same then make sure that we do not have equivalent transitions
			
			// although two have the same state they are not equivalent for the set 
			//for sorting it does not matter if one of them is before the other
			else if ((this.start > tr.finish) || (this.finish < tr.start))
				return (new Character(this.start)).compareTo(new Character(tr.finish));
			else 
				return 0;
		}
		
	}
	
	/**
	 * returns the c trace equivalent to the exceution of function <code>makeFromAnotherAuto</code>.
	 * @param states
	 * @param transitions
	 * @param id
	 * @return
	 */
	private static String makeFromAnotherAutoToCTrace(TreeSet<State> states, TreeSet<Transition> transitions, int id){
		StringBuilder sb = new StringBuilder();
		sb.append("int num_of_trans"+ (tempTraceID++) +" = " + transitions.size() +";"+"\n");
		sb.append("transition* t"+ (tempTraceID++) +" = (transition*) malloc(num_of_trans" + (tempTraceID - 2) + " * sizeof(transition));" + "\n");
		sb.append("int i"+(tempTraceID++) + " = 0;" + "\n");
//		sb.append("for (i"+(tempTraceID-1)+" = 0; i"+(tempTraceID -1)+" < num_of_trans"+(tempTraceID-3)+"; i"+(tempTraceID -1)+"++){");
		for (Transition tr: transitions){
		sb.append("t"+(tempTraceID - 2)+"[i"+(tempTraceID - 1)+"].source = "+tr.src+";");
		sb.append("t"+(tempTraceID - 2)+"[i"+(tempTraceID - 1)+"].dest = "+tr.dest+";");
		sb.append("t"+(tempTraceID - 2)+"[i"+(tempTraceID - 1)+"].first = '"+tr.start+"';");
		sb.append("t"+(tempTraceID - 2)+"[i"+(tempTraceID - 1)+"++].last = '"+tr.finish+"';" + "\n");
//		sb.append("}");
		}
		sb.append("assert(i"+(tempTraceID-1)+" == num_of_trans"+(tempTraceID-3) + ");"+"\n");
		sb.append("M[" + traceID + "] = dfa_construct_from_automaton("+states.size()+", num_of_trans"+(tempTraceID-3)+", t"+(tempTraceID - 2)+", \"");
		for (State state: states){
			if (state.accept)
				sb.append("+");
			else
				sb.append("-");
		}
		sb.append("\", NUM_ASCII_TRACKS, indices_main);//"+ id + " = makeFromAnotherAuto()" + "\n");
		return sb.toString();
	}
	
	/**
	 * Creates a stranger automaton from anther non-stranger automaton.
	 * @param states: a sorted set (TreeSet) the states of the input non-stranger automaton where each state is an object of type StrangerAutomaton.State
	 * @param transitions: a sorted set (TreeSet) of the non-stranger automaton where each transition is an object of type StrangerAutomaton.Transitions
	 * @param id
	 *            : id of node associated with this auto; used for debugging
	 *            purposes only	 
	 * @return
	 */
	public static StrangerAutomaton makeFromAnotherAuto(TreeSet<State> states, TreeSet<Transition> transitions, boolean empty, int id){
		
		debug(id + " = makeFromAnotherAuto()");
		
		debugToFile(makeFromAnotherAutoToCTrace(states, transitions, id));;

		int n_states = states.size();
		String accepting_pattern = "";
		for (State s: states)
			if (s.accept)
				accepting_pattern += "+";
			else
				accepting_pattern += "-";
		int n_trans = transitions.size();
		
		transition.ByReference[] nativeTransitions =  (transition.ByReference[]) new transition.ByReference().toArray(transitions.size());
		int i = 0;
		for (Transition t: transitions){
			((StrangerLibrary.transition)nativeTransitions[i]).source = t.src;
			((StrangerLibrary.transition)nativeTransitions[i]).dest = t.dest;
			((StrangerLibrary.transition)nativeTransitions[i]).first = (byte)t.start;
			((StrangerLibrary.transition)nativeTransitions[i++]).last = (byte)t.finish;
		}

		StrangerAutomaton retMe =  new StrangerAutomaton(StrangerLibrary.INSTANCE.dfa_construct_from_automaton(n_states, n_trans, nativeTransitions[0], accepting_pattern, StrangerLibraryWrapper.NUM_ASCII_TRACKS, StrangerLibraryWrapper.indices_main));	

		retMe.empty = empty;
		{
			retMe.setID(id);
			retMe.debugAutomaton();
		}
		return retMe;
	}
	
	/**
	 * Creates a stranger automaton from anther non-stranger automaton.
	 * @param states: a sorted set (TreeSet) the states of the input non-stranger automaton where each state is an object of type StrangerAutomaton.State
	 * @param transitions: a sorted set (TreeSet) of the non-stranger automaton where each transition is an object of type StrangerAutomaton.Transitions
	 * @return
	 */
	public static StrangerAutomaton makeFromAnotherAuto(TreeSet<State> states, TreeSet<Transition> transitions, boolean empty){
		return makeFromAnotherAuto(states, transitions, empty, traceID);
	}
	
	private static String getHexString(byte[] b) throws Exception {
		  String result = "";
		  for (int i=0; i < b.length; i++) {
		    result +=
		          Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
		  }
		  return result;
	}
	
	public String generateSatisfyingExample(){
		debug("running: generateSatisfyingExample(" + this.ID + ")");

		if (isTop() || isBottom()) return null;
		
		debugToFile("char * example" + autoTraceID + " = dfaGenerateExample(M["+ this.autoTraceID +"], NUM_ASCII_TRACKS, indices_main);// example = generateSatisfyingExample(" + this.ID + ")");
		perfInfo.numOfGenerateExample++;
		long start = System.currentTimeMillis();
		
		String retMe = 
				StrangerLibrary.INSTANCE.dfaGenerateExample(this.dfa,
						StrangerLibraryWrapper.NUM_ASCII_TRACKS,
						StrangerLibraryWrapper.indices_main);
		
		long stop = System.currentTimeMillis();
		perfInfo.generateExampleTime += (stop - start);
		
		try {
			debug("result of: generateSatisfyingExample(" + this.ID + ") is " + retMe + "  in hex: " + getHexString(retMe.getBytes()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}

		return retMe;
	}
	
	/**
	 * Given a dot file that contains output of StrangerAutomaton.toDot(), this method
	 * will parse the content and construct the automaton again into memory.
	 * @param fileName : dot file for the automaton
	 * @param numberOfTracks : number of tracks for the automaton. If unsure use 1.
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	public static StrangerAutomaton fromDotFile(String fileName , int numberOfTracks) throws ParseException, IOException {
		AutoDotFileParser parser = new AutoDotFileParser();
		StrangerAutomaton retMe = parser.parseDotFile(fileName, numberOfTracks);
		return retMe;
	}
	
	//***************************************************************************************
	//*                                  Unary Operations                                   *
	//*									-------------------									*
	//* These operations are given one automata and result in a newly created one.			*
	//***************************************************************************************
	
	/**
	 * Returns a new automaton that accepts (empty_string) union L(this auto)).
	 * @param id: id of node associated with this auto; used for debugging
	 *            purposes only
	 * @return
	 */
	public StrangerAutomaton optional(int id) {
		debug(id + " = makeOptional(" + this.ID + ") -- start");

		StrangerAutomaton retMe = this.unionWithEmptyString(id);

		debug(id + " = makeOptional(" + this.ID + ") -- end");

		{
			retMe.setID(id);
			retMe.debugAutomaton();
		}
		return retMe;
	}

	/**
	 * Returns a new automaton that accepts (empty_string) union L(this auto)).
	 * @return
	 */
	public StrangerAutomaton optional() {
		return this.optional(traceID);
	}

	

	/**
	 * Returns a new automaton that accepts (empty_string) union closure(this auto)).
	 * @param id
	 *            : id of node associated with this auto; used for debugging
	 *            purposes only
	 * @return
	 */
	public StrangerAutomaton kleensStar(int id) {
		debug(id + " = kleensStar(" + this.ID + ") -- start");

		StrangerAutomaton retMe = this.closure(this.ID);
		retMe = retMe.unionWithEmptyString(id);
		debug(id + " = kleensStar(" + this.ID + ") -- end");
		{
			retMe.setID(id);
			retMe.debugAutomaton();
		}
		return retMe;
	}
	
	/**
	 * Returns a new automaton that accepts (empty_string) union closure(this auto)).
	 * @return
	 */
	public StrangerAutomaton kleensStar() {
		return this.kleensStar(traceID);
	}
	
	/**
	 * Returns a new automaton that accepts (empty_string) union closure(auto)).
	 * @param auto: input auto to do kleens star for
	 * @param id
	 *            : id of node associated with this auto; used for debugging
	 *            purposes only
	 * @return
	 */
	public static StrangerAutomaton kleensStar(StrangerAutomaton auto, int id) {
		debug(id + " = kleensStar(" + auto.ID + ")");

		StrangerAutomaton retMe = auto.kleensStar(id);
		{
			retMe.setID(id);
			retMe.debugAutomaton();
		}
		return retMe;
	}
	
	/**
	 * Returns a new automaton that accepts (empty_string) union closure(auto)).
	 * @param auto: input auto to do kleens star for
	 * @return
	 */
	public static StrangerAutomaton kleensStar(StrangerAutomaton auto) {
		return kleensStar(auto, traceID);
	}
	
	
	/**
	 * returns a new automaton with language L = closure(L(this auto))
	 * @param id
	 *            : id of node associated with this auto; used for debugging
	 *            purposes only
	 * @return
	 */
	public StrangerAutomaton closure(int id) {
		debug(id + " = closure(" + this.ID + ")");

		if (isTop() || isBottom()) return this.clone(id);
		
		debugToFile("M[" + traceID + "] = dfa_closure_extrabit(M["+ this.autoTraceID +"], NUM_ASCII_TRACKS, indices_main);//"+id + " = closure(" + this.ID + ")");
		perfInfo.numOfClosure++;
		long start = System.currentTimeMillis();
		
		StrangerAutomaton retMe = new StrangerAutomaton(
				StrangerLibrary.INSTANCE.dfa_closure_extrabit(this.dfa,
						StrangerLibraryWrapper.NUM_ASCII_TRACKS,
						StrangerLibraryWrapper.indices_main));
		retMe.str = null;
		
		long stop = System.currentTimeMillis();
		perfInfo.closureTime += (stop - start);
		
		{
			retMe.setID(id);
			retMe.debugAutomaton();
		}
		return retMe;
	}
	
	/**
	 * returns a new automaton with language L = closure(L(this auto))
	 * @return
	 */
	public StrangerAutomaton closure() {
		return this.closure(traceID);
	}

	/**
	 * returns a new automaton with language L = closure(L(auto))
	 * @param auto
	 * @param id
	 *            : id of node associated with this auto; used for debugging
	 *            purposes only
	 * @return
	 */
	public static StrangerAutomaton closure(StrangerAutomaton auto, int id) {
		debug(id + " = closure(" + auto.ID + ")");

		StrangerAutomaton retMe = auto.closure(id);
		{
			retMe.setID(id);
			retMe.debugAutomaton();
		}
		return retMe;
	}
	
	/**
	 * returns a new automaton with language L = closure(L(auto))
	 * @param auto
	 * @return
	 */
	public static StrangerAutomaton closure(StrangerAutomaton auto) {
		return closure(auto, traceID);
	}

	/**
	 * Returns new automaton that accepts <code>min</code> or more concatenated
	 * repetitions of the language of this automaton.
	 * 
	 * @param min: minimum number of concatenations
	 * @param id: id of node associated with this auto; used for debugging
	 *            purposes only
	 * @return
	 */
	public StrangerAutomaton repeat(int min, int id) {
		debug(id + " = repeate(" + min + "," + this.ID + ") -- start");

		StrangerAutomaton retMe = null;
		if (min == 0)
			retMe = this.kleensStar(id);
		else if (min == 1)
			retMe = this.closure(id);
		else {
			StrangerAutomaton temp = this.closure(id);
			StrangerAutomaton unionAuto = null;
			// repeat from 0 to min - 1
			for (int i = 1; i < min; i++){
//				if (i == 0)
//					retMe = makeEmptyString(id);
				int j = i;
				StrangerAutomaton orig = this;
				retMe = this;
				while (--j > 0)
					retMe = retMe.concatenate(orig, this.ID);
				if (i == 1)
					unionAuto = retMe.clone(id);
				else
					unionAuto = unionAuto.union(retMe, id);
			}
			// add emtpy string for min
			unionAuto.empty = true;
			// if min grater than one then it is closure minus lower concats
			retMe = temp.intersect(unionAuto.complement());
		}
		debug(id + " = repeate(" + min + "," + this.ID + ") -- end");
		{
			retMe.setID(id);
			retMe.debugAutomaton();
		}
		return retMe;
	}
	
	/**
	 * Returns new automaton that accepts <code>min</code> or more concatenated
	 * repetitions of the language of this automaton.
	 * 
	 * @param min: minimum number of concatenations
	 * @return
	 */
	public StrangerAutomaton repeat(int min) {
		return this.repeat(min, traceID);
	}
	
	
	/**
	 * Returns new automaton that accepts between <code>min</code> and
	 * <code>max</code> (including both) concatenated repetitions of the
	 * language of this automaton.
	 * 
	 * @param min: minimum number of concatenations
	 * @param max: maximum number of concatenations
	 * 
	 * @param id: id of node associated with this auto; used for debugging
	 *            purposes only
	 * @return
	 */
	public StrangerAutomaton repeat(int min, int max, int id) {
		debug(id + " = repeate(" + min + ", " + max + ", " + this.ID
				+ ") -- start");

		StrangerAutomaton retMe = null;
		if (min > max)
			retMe = makePhi(id);
		else {
//			max -= min;
//			else {
				// TODO: I think we should clone here
				StrangerAutomaton unionAuto = null;
				for (int i = min; i <= max; i++){
					if (i == 0)
//						retMe = makeEmptyString(id);
						continue;
					int j = i;
					StrangerAutomaton orig = this;
					retMe = this;
					while (--j > 0)
						retMe = retMe.concatenate(orig, this.ID);
					if (i == min || (min == 0 && i == 1))
						unionAuto = retMe.clone(id);
					else
						unionAuto = unionAuto.union(retMe, id);
				}
//			}
			retMe = unionAuto.clone();
			if (min == 0)
				retMe.empty = true;
			
//			while (max-- > 0) {
//				roundAuto = roundAuto.concatenate(roundAuto, this.ID);
//				retMe = retMe.union(roundAuto, id);
//			}
		}

		debug(id + " = repeate(" + min + ", " + max + ", " + this.ID
				+ ") -- end");
		{
			retMe.setID(id);
			retMe.debugAutomaton();
		}
		return retMe;
	}
	
	/**
	 * Returns new automaton that accepts between <code>min</code> and
	 * <code>max</code> (including both) concatenated repetitions of the
	 * language of this automaton.
	 * @param min: minimum number of concatenations
	 * @param max: maximum number of concatenations
	 * @return
	 */
	public StrangerAutomaton repeat1(int min, int max) {
		return this.repeat(min, max, traceID);
	}

	/**
	 * Returns a new automaton auto with L(auto)= the complement of the language of current automaton
	 * @param id: id of node associated with this auto; used for debugging
	 *            purposes only
	 * @return
	 */
	public StrangerAutomaton complement(int id) {
		debug(id + " = complement(" + this.ID + ")");
		if (isTop())
			// top is an unknown type so can not be complemented
			return makeTop(id);
		else if (isBottom())
			// bottom is efficient phi so complement is Sigma*
			return makeAnyString(id);
		
		debugToFile("M[" + traceID + "] = dfa_negate(M["+ this.autoTraceID +"], NUM_ASCII_TRACKS, indices_main);//"+id + " = complement(" + this.ID + ")");
		perfInfo.numOfComplement++;
		long start = System.currentTimeMillis();
		
		StrangerAutomaton retMe = new StrangerAutomaton(
				StrangerLibrary.INSTANCE.dfa_negate(this.dfa,
						StrangerLibraryWrapper.NUM_ASCII_TRACKS,
						StrangerLibraryWrapper.indices_main));
		
		retMe.empty = !this.empty;
		
		long stop = System.currentTimeMillis();
		perfInfo.complementTime += (stop - start);
		
		
		
		{
			retMe.setID(id);
			retMe.debugAutomaton();
		}
				
		return retMe;
	}
	
	/**
	 * Returns a new automaton auto with L(auto)= the complement of the language of current automaton
	 * @return
	 */
	public StrangerAutomaton complement() {
		return this.complement(traceID);
	}
	
	//***************************************************************************************
	//*                                  Binary Operations                                  *
	//*									-------------------									*
	//* These operations are given two automata and result in a newly created one.			*
	//***************************************************************************************

	
	/**
	 * Returns a new automaton auto with L(auto)= L(this) union L(auto)
	 * @param auto
	 * @param id
	 *            : id of node associated with this auto; used for debugging
	 *            purposes only
	 * @return
	 */
	public StrangerAutomaton union(StrangerAutomaton auto, int id) {
		debug(id + " = union(" + this.ID + ", " + auto.ID + ")");
		
		// if top or bottom then do not use the c library as dfa == null
		if (this.isTop() || auto.isTop())
			return makeTop(id);
		else if (this.isBottom())
			return auto.clone(id);
		else if (auto.isBottom())
			return this.clone(id);
		
		
		//debugToFile("M[" + traceID + "] = dfa_union_with_emptycheck(M["+ this.autoTraceID +"], M["+ auto.autoTraceID +"], NUM_ASCII_TRACKS, indices_main);//"+id + " = union(" + this.ID + ", " + auto.ID + ")");
		debugToFile("M[" + traceID + "] = dfa_union(M["+ this.autoTraceID +"], M["+ auto.autoTraceID +"]);//"+id + " = union(" + this.ID + ", " + auto.ID + ")");
		perfInfo.numOfUnion++;
		long start = System.currentTimeMillis();
		
//		StrangerAutomaton retMe = new StrangerAutomaton(
//				StrangerLibrary.INSTANCE.dfa_union_with_emptycheck(this.dfa,
//						auto.dfa, StrangerLibraryWrapper.NUM_ASCII_TRACKS,
//						StrangerLibraryWrapper.indices_main));
		StrangerAutomaton retMe = new StrangerAutomaton(
				StrangerLibrary.INSTANCE.dfa_union(this.dfa,auto.dfa));
		
		retMe.empty = this.empty || auto.empty;
		
		long finish = System.currentTimeMillis();
		perfInfo.unionTime += (finish - start);
		
		retMe.str = null;
		
		
		{
			retMe.setID(id);
			retMe.debugAutomaton();
		}
		return retMe;
	}
	
	/**
	 * Returns a new automaton auto with L(auto)= L(this) union L(auto)
	 * @param auto
	 * @return
	 */
	public StrangerAutomaton union(StrangerAutomaton auto) {
		return this.union(auto, traceID);
	}

	/**
	 * Returns a new automaton auto with L(auto)= L(this) union L2 where L2 contains only empty string (epsilon)
	 * @param id
	 *            : id of node associated with this auto; used for debugging
	 *            purposes only
	 * @return
	 */
	public StrangerAutomaton unionWithEmptyString(int id) {
		debug(id + " = unionWithEmptyString(" + this.ID + ") -- start");

//		StrangerAutomaton empty = StrangerAutomaton.makeEmptyString(-100);
//		StrangerAutomaton retMe = this.union(empty, id);
//		StrangerAutomaton retMe = new StrangerAutomaton(StrangerLibrary.INSTANCE.dfa_union_with_emptycheck(this.dfa, empty.dfa, StrangerLibraryWrapper.NUM_ASCII_TRACKS, StrangerLibraryWrapper.indices_main));
		StrangerAutomaton retMe = this;
		retMe.empty = true;
		debug(id + " = unionWithEmptyString(" + this.ID + ") -- end");
		// if you union me with empty string then I represent now at least two
		// different strings
		// one of them is the empty string
		retMe.str = null;
		{
			retMe.setID(id);
			retMe.debugAutomaton();
		}
		return retMe;
	}
	
	/**
	 * Returns a new automaton auto with L(auto)= L(this) union L2 where L2 contains only empty string (epsilon)
	 * @return
	 */
	public StrangerAutomaton unionWithEmptyString() {
		return this.unionWithEmptyString(traceID);
	}
	

	/**
 	 * Returns a new automaton auto with L(auto)= L(this) intersect L(auto)
 	 * @param id
	 *            : id of node associated with this auto; used for debugging
	 *            purposes only
	 */
	public StrangerAutomaton intersect(StrangerAutomaton auto, int id) {
		debug(id + " = intersect(" + this.ID + ", " + auto.ID + ")");

		// if top or bottom then do not use the c library as dfa == null
		if (this.isBottom() || auto.isBottom())
			return makeBottom(id);
		else if (this.isTop())
			return auto.clone(id);
		else if (auto.isTop())
			return this.clone(id);

		debugToFile("M[" + traceID + "] = dfa_intersect(M["+ this.autoTraceID +"], M["+ auto.autoTraceID +"]);//"+id + " = intersect(" + this.ID + ", " + auto.ID + ")");
		perfInfo.numOfIntersect++;
		long start = System.currentTimeMillis();
		
		StrangerAutomaton retMe = new StrangerAutomaton(
				StrangerLibrary.INSTANCE.dfa_intersect(this.dfa, auto.dfa));
		
		retMe.empty = this.empty && auto.empty;
		
		long stop = System.currentTimeMillis();
		perfInfo.intersectTime += (stop - start);		

		{
			retMe.setID(id);
			retMe.debugAutomaton();
		}
		return retMe;
	}
	
	/**
 	 * Returns a new automaton auto with L(auto)= L(this) intersect L(auto)
	 */
	public StrangerAutomaton intersect(StrangerAutomaton auto) {
		return this.intersect(auto, traceID);
	}

	
	//***************************************************************************************
	//*                                  Widening operations                                *
	//***************************************************************************************

	/**
	 * This will do widen(this, auto). L(this) should_be_subset_of L(auto)
	 * We first apply union, then this widening for a while then the 
	 * coarse one.
	 * @param auto
	 * @param id
	 *            : id of node associated with this auto; used for debugging
	 *            purposes only
	 * @return
	 */
	public StrangerAutomaton preciseWiden(StrangerAutomaton auto, int id) {
		debug(id + " = precise_widen(" + this.ID + ", " + auto.ID + ")");

		// if top or bottom then do not use the c library as dfa == null
		if (this.isTop() || auto.isTop())
			return makeTop(id);
		else if (this.isBottom())
			return auto.clone(id);
		else if (auto.isBottom())
			return this.clone(id);
		
		if (coarseWidening) {
			debugToFile("setPreciseWiden();");			
			StrangerLibrary.INSTANCE.setPreciseWiden();			
			coarseWidening = false;
		}
		
		debugToFile("M[" + traceID + "] = dfaWiden(M["+ this.autoTraceID +"], M["+ auto.autoTraceID +"]);//"+id + " = precise_widen(" + this.ID + ", " + auto.ID + ")");	
		perfInfo.numOfPreciseWiden++;
		long start = System.currentTimeMillis();
		
		StrangerAutomaton retMe = new StrangerAutomaton(
				StrangerLibrary.INSTANCE.dfaWiden(this.dfa, auto.dfa));
		
		long stop = System.currentTimeMillis();
		perfInfo.preciseWidenTime += (stop - start);
		
		{
			retMe.setID(id);
			retMe.debugAutomaton();
		}
		return retMe;
	}
	
	/**
	 * This will do widen(this, auto). L(this) should_be_subset_of L(auto)
	 * We first apply union, then this widening for a while then the 
	 * coarse one.
	 * @param auto
	 * @return
	 */
	public StrangerAutomaton preciseWiden(StrangerAutomaton auto) {
		return this.preciseWiden(auto, traceID);
	}

	/**
	 * This will do widen(this, auto). L(this) should_be_subset_of L(auto)
	 * We first apply union, then precise widening for a while then this 
	 * coarse one.
	 * @param auto
	 * @param id
	 *            : id of node associated with this auto; used for debugging
	 *            purposes only
	 * @return
	 */
	public StrangerAutomaton coarseWiden(StrangerAutomaton auto, int id) {
		debug(id + " = coarse_widen(" + this.ID + ", " + auto.ID + ")");

		// if top or bottom then do not use the c library as dfa == null
		if (this.isTop() || auto.isTop())
			return makeTop(id);
		else if (this.isBottom())
			return auto.clone(id);
		else if (auto.isBottom())
			return this.clone(id);

		if (!coarseWidening) {
			debugToFile("setCoarseWiden();");
			StrangerLibrary.INSTANCE.setCoarseWiden();
			coarseWidening = true;
		}

		debugToFile("M[" + traceID + "] = dfaWiden(M["+ this.autoTraceID +"], M["+ auto.autoTraceID +"]);//"+id + " = coarse_widen(" + this.ID + ", " + auto.ID + ")");		
		perfInfo.numOfCoarseWiden++;
		long start = System.currentTimeMillis();
		
		StrangerAutomaton retMe = new StrangerAutomaton(
				StrangerLibrary.INSTANCE.dfaWiden(this.dfa, auto.dfa));
		
		long stop = System.currentTimeMillis();
		perfInfo.coarseWidenTime += (stop - start);
		
		{
			retMe.setID(id);
			retMe.debugAutomaton();
		}
		return retMe;
	}
	
	/**
	 * This will do widen(this, auto). L(this) should_be_subset_of L(auto)
	 * We first apply union, then precise widening for a while then this 
	 * coarse one. 
	 * @param auto
	 * @return
	 */
	public StrangerAutomaton coarseWiden(StrangerAutomaton auto) {
		return this.coarseWiden(auto, traceID);
	}
	
	//***************************************************************************************
	//*                                  Forwards Concatenation                             *
	//***************************************************************************************
	
	/**
	 * Concatenates current automaton with auto. New automaton will be
	 * this+auto. If both automatons' strings are not null, they will be
	 * concatenated too otherwise set null. id : id of node associated with this
	 * auto; used for debugging purposes only
	 * 
	 * @param auto
	 * @param id
	 *            : id of node associated with this auto; used for debugging
	 *            purposes only
	 * @return
	 */
	public StrangerAutomaton concatenate(StrangerAutomaton auto, int id) {
		debug(id + " = concatenate(" + this.ID + ", " + auto.ID + ")");
		// TODO: this is different than javascrit semantics. check http://www.quirksmode.org/js/strings.html
		// if top or bottom then do not use the c library as dfa == null
		if (this.isTop() || auto.isTop())
			return makeTop(id);
		else if (this.isBottom() || auto.isBottom())
			return makeBottom(id);

		StrangerAutomaton temp1 = makeBottom() , temp2 = makeBottom();
		// if one of the two is phi then we need to check if it has empty string
		// if it has empty string then it is actually not phi
//		if (this.empty && this.checkEmptiness())
//			return auto.clone();
//		else if (auto.empty && auto.checkEmptiness())
//			return this.clone();
//		else {
			if (this.empty)
				temp1 = auto.clone();
			if (auto.empty)
				temp2 = this.clone();
//		}

		debugToFile("M[" + traceID + "] = dfa_concat(M["+ this.autoTraceID +"], M["+ auto.autoTraceID +"], NUM_ASCII_TRACKS, indices_main);//"+id + " = concatenate(" + this.ID + ", " + auto.ID + ")");
		perfInfo.numOfConcat++;
		long start = System.currentTimeMillis();

		// dfa_concat_extrabit returns new dfa structure in memory so no need to
		// worry about the two dfas of this and auto
		StrangerAutomaton retMe = null;
//			retMe = new StrangerAutomaton(
//				StrangerLibrary.INSTANCE.dfa_concat(auto.dfa,
//						this.dfa, StrangerLibraryWrapper.NUM_ASCII_TRACKS,
//						StrangerLibraryWrapper.indices_main));
//		else

			retMe = new StrangerAutomaton(
					StrangerLibrary.INSTANCE.dfa_concat(this.dfa,
							auto.dfa, StrangerLibraryWrapper.NUM_ASCII_TRACKS,
							StrangerLibraryWrapper.indices_main));

		long finish = System.currentTimeMillis();
		perfInfo.concatTime += (finish - start);

		if (this.str != null && auto.getStr() != null)
			retMe.str = this.str + auto.getStr();
		else
			retMe.str = null;

		retMe = retMe.union(temp1).union(temp2);

		{
			retMe.setID(id);
			retMe.debugAutomaton();
		}
		return retMe;
	}

	/**
	 * Concatenates current automaton with auto. New automaton will be
	 * this+auto. If both automatons' strings are not null, they will be
	 * concatenated too otherwise set null. id : id of node associated with this
	 * auto; used for debugging purposes only
	 *
	 * @param auto
	 * @param id
	 *            : id of node associated with this auto; used for debugging
	 *            purposes only
	 * @return
	 */
	public StrangerAutomaton concatenate(StrangerAutomaton auto) {
		return this.concatenate(auto, traceID);
	}
	
	//***************************************************************************************
	//*                                  Backwards Concatenation                            *
	//***************************************************************************************
	/**
	 * For current automaton concatAuto (this) , method returns an automaton
	 * retMe such that L(retME) = all possible strings to be the left side of a
	 * concat operation that results in the concatAuto.
	 * 
	 * @param rightSiblingAuto
	 * @param concatAuto
	 * @param id
	 *            : id of node associated with this auto; used for debugging
	 *            purposes only
	 * @return
	 */
	public StrangerAutomaton leftPreConcat(StrangerAutomaton rightSiblingAuto,
			int id) {
		debug(id + " = leftPreConcat(" + this.ID + ", " + rightSiblingAuto.ID
				+ ")");
		// if top or bottom then do not use the c library as dfa == null
		if (this.isBottom() || rightSiblingAuto.isBottom())
			return makeBottom(id);
		else if (this.isTop() || rightSiblingAuto.isTop())
			return makeTop(id);
		
		debugToFile("M[" + traceID + "] = dfa_pre_concat(M["+ this.autoTraceID +"], M[" + rightSiblingAuto.autoTraceID + "], 1, NUM_ASCII_TRACKS, indices_main);//" + id + " = leftPreConcat(" + this.ID + ", " + rightSiblingAuto.ID
				+ ")");
		perfInfo.numOfPreConcat++;
		long start = System.currentTimeMillis();
		
		StrangerAutomaton retMe = new StrangerAutomaton(
				StrangerLibrary.INSTANCE.dfa_pre_concat(this.dfa,
						rightSiblingAuto.dfa, 1,
						StrangerLibraryWrapper.NUM_ASCII_TRACKS,
						StrangerLibraryWrapper.indices_main));
		
		long stop = System.currentTimeMillis();
		perfInfo.preconcatTime += (stop - start);
		
		{
			retMe.setID(id);
			retMe.debugAutomaton();
		}
		return retMe;
	}
	
	/**
	 * For current automaton concatAuto (this) , method returns an automaton
	 * retMe such that L(retME) = all possible strings to be the left side of a
	 * concat operation that results in the concatAuto.
	 * 
	 * @param rightSiblingAuto
	 * @param concatAuto
	 * @return
	 */
	public StrangerAutomaton leftPreConcat(StrangerAutomaton rightSiblingAuto) {
		return this.leftPreConcat(rightSiblingAuto, traceID);
	}

	
	/**
	 * For current automaton concatAuto (this) , method returns an automaton
	 * retMe such that L(retME) = all possible strings to be the left side of a
	 * concat operation that results in the concatAuto.
	 * 
	 * @param rightSiblingAuto
	 * @param concatAuto
	 * @param id
	 *            : id of node associated with this auto; used for debugging
	 *            purposes only
	 * @return
	 */
	public StrangerAutomaton leftPreConcatConst(String rightSiblingString,
			int id) {
		debug(id + " = rightPreConcatConst(" + this.ID + ", "
				+ rightSiblingString + ")");

		// if top or bottom then do not use the c library as dfa == null
		if (this.isBottom())
			return makeBottom(id);
		else if (this.isTop())
			return makeTop(id);
		
		debugToFile("M[" + traceID + "] = dfa_pre_concat_const(M["+ this.autoTraceID +"], \"" + escapeSpecialChars(rightSiblingString) + "\", 1, NUM_ASCII_TRACKS, indices_main);//" +id + " = rightPreConcatConst(" + this.ID + ", "
				+ escapeSpecialChars(rightSiblingString) + ")");
		perfInfo.numOfConstPreConcat++;
		long start = System.currentTimeMillis();
		
		StrangerAutomaton retMe = new StrangerAutomaton(
				StrangerLibrary.INSTANCE.dfa_pre_concat_const(this.dfa,
						rightSiblingString, 1,
						StrangerLibraryWrapper.NUM_ASCII_TRACKS,
						StrangerLibraryWrapper.indices_main));
		
//		StrangerAutomaton retMe = new StrangerAutomaton(
//				StrangerLibrary.INSTANCE.dfa_pre_concat(this.dfa,
//						makeString(rightSiblingString, -1).dfa, 1,
//						StrangerLibraryWrapper.NUM_ASCII_TRACKS,
//						StrangerLibraryWrapper.indices_main));
		
		long stop = System.currentTimeMillis();
		perfInfo.constpreconcatTime += (stop - start);

		
		{
			retMe.setID(id);
			retMe.debugAutomaton();
		}
		return retMe;
	}
	
	/**
	 * For current automaton concatAuto (this) , method returns an automaton
	 * retMe such that L(retME) = all possible strings to be the left side of a
	 * concat operation that results in the concatAuto.
	 * 
	 * @param rightSiblingAuto
	 * @param concatAuto
	 * @return
	 */
	public StrangerAutomaton leftPreConcatConst(String rightSiblingString) {
		return this.leftPreConcatConst(rightSiblingString, traceID);
	}

	/**
	 * For current automaton concatAuto (this) , method returns an automaton
	 * retMe such that L(retME) = all possible strings to be the right side of a
	 * concat operation that results in the concatAuto.
	 * 
	 * @param leftSiblingAuto
	 * @param concatAuto
	 * @param id
	 *            : id of node associated with this auto; used for debugging
	 *            purposes only
	 * @return
	 */
	public StrangerAutomaton rightPreConcat(StrangerAutomaton leftSiblingAuto,
			int id) {
		debug(id + " = rightPreConcat(" + this.ID + ", " + leftSiblingAuto.ID
				+ ")");
		
		// if top or bottom then do not use the c library as dfa == null
		if (this.isBottom() || leftSiblingAuto.isBottom())
			return makeBottom(id);
		else if (this.isTop() || leftSiblingAuto.isTop())
			return makeTop(id);

		debugToFile("M[" + (traceID) + "] = dfa_pre_concat(M["+ this.autoTraceID +"], M[" + leftSiblingAuto.autoTraceID + "], 2, NUM_ASCII_TRACKS, indices_main);//"+id + " = rightPreConcat(" + this.ID + ", " + leftSiblingAuto.ID
				+ ")");
		perfInfo.numOfPreConcat++;
		long start = System.currentTimeMillis();
				
		StrangerAutomaton retMe = new StrangerAutomaton(
				StrangerLibrary.INSTANCE.dfa_pre_concat(this.dfa,
						leftSiblingAuto.dfa, 2,
						StrangerLibraryWrapper.NUM_ASCII_TRACKS,
						StrangerLibraryWrapper.indices_main));
		
		long stop = System.currentTimeMillis();
		perfInfo.preconcatTime += (stop - start);
		
		{
			retMe.setID(id);
			retMe.debugAutomaton();
		}
		return retMe;
	}

	/**
	 * For current automaton concatAuto (this) , method returns an automaton
	 * retMe such that L(retME) = all possible strings to be the right side of a
	 * concat operation that results in the concatAuto.
	 * 
	 * @param leftSiblingAuto
	 * @param concatAuto
	 * @return
	 */
	public StrangerAutomaton rightPreConcat(StrangerAutomaton leftSiblingAuto) {
		return this.rightPreConcat(leftSiblingAuto, traceID);
	}
	
	/**
	 * For current automaton concatAuto (this) , method returns an automaton
	 * retMe such that L(retME) = all possible strings to be the right side of a
	 * concat operation that results in the concatAuto.
	 * 
	 * @param leftSiblingString
	 * @param concatAuto
	 * @param id
	 *            : id of node associated with this auto; used for debugging
	 *            purposes only
	 * @return
	 */
	public StrangerAutomaton rightPreConcatConst(String leftSiblingString,
			int id) {
		debug(id + " = rightPreConcatConst(" + this.ID + ", "
				+ leftSiblingString + ")");
		
		// if top or bottom then do not use the c library as dfa == null
		if (this.isBottom())
			return makeBottom(id);
		else if (this.isTop())
			return makeTop(id);

		debugToFile("M[" + traceID + "] = dfa_pre_concat_const(M["+ this.autoTraceID +"], \"" + escapeSpecialChars(leftSiblingString) + "\", 2, NUM_ASCII_TRACKS, indices_main);//" + id + " = rightPreConcatConst(" + this.ID + ", "
				+ escapeSpecialChars(leftSiblingString) + ")");
		perfInfo.numOfConstPreConcat++;
		long start = System.currentTimeMillis();		
		
		StrangerAutomaton retMe = new StrangerAutomaton(
				StrangerLibrary.INSTANCE.dfa_pre_concat_const(this.dfa,
						leftSiblingString, 2, StrangerLibraryWrapper.NUM_ASCII_TRACKS,
						StrangerLibraryWrapper.indices_main));
		
//		StrangerAutomaton retMe = new StrangerAutomaton(
//				StrangerLibrary.INSTANCE.dfa_pre_concat(this.dfa,
//						makeString(leftSiblingString, -1).dfa, 2, StrangerLibraryWrapper.NUM_ASCII_TRACKS,
//						StrangerLibraryWrapper.indices_main));
		
		long stop = System.currentTimeMillis();
		perfInfo.constpreconcatTime += (stop - start);
		
		
		
		{
			retMe.setID(id);
			retMe.debugAutomaton();
		}
		return retMe;
	}

	/**
	 * For current automaton concatAuto (this) , method returns an automaton
	 * retMe such that L(retME) = all possible strings to be the right side of a
	 * concat operation that results in the concatAuto.
	 * 
	 * @param leftSiblingString
	 * @param concatAuto
	 * @return
	 */
	public StrangerAutomaton rightPreConcatConst(String leftSiblingString) {
		return this.rightPreConcatConst(leftSiblingString, traceID);
	}
	
	//***************************************************************************************
	//*                                  Forward Replacement                                *
	//***************************************************************************************

	/**
	 * Parses a PHP regular expression and converts it into stranger automaton.
	 * For allowed regular expressions check {@link RegExp}.
	 * @param phpRegexOrig: the string literal representing the regular expression
	 * @param preg: if following preg or ereg (now only supports preg)
	 * @param id
	 *            : id of node associated with this auto; used for debugging
	 *            purposes only
	 * @return
	 */
	public static StrangerAutomaton regExToAuto(String phpRegexOrig,
			boolean preg, int id) {
		debug("============");
		debug(id + " = regExToAuto(" + phpRegexOrig + ") -- start");

		StrangerAutomaton retMe = null;

		if (phpRegexOrig == null) {
			System.out.println("regular expression is null so overapproximatin to makeAnyString");
			retMe = StrangerAutomaton.makeAnyString(id);
		} else if (phpRegexOrig.isEmpty()) {
			System.out.println("regular expression is empty so using makeEmptyString");
			retMe = StrangerAutomaton.makeAnyStringL1ToL2(0, 0);
		} else {
			StringBuilder phpRegex = new StringBuilder(phpRegexOrig);
			if (preg) {
				// if the preg regex is not delimited...
				Character first = new Character(phpRegex.charAt(0));
				Character last = new Character(phpRegex.charAt(phpRegex
						.lastIndexOf("/")));
				if (!first.equals(StrangerAutomaton.slash)
						|| (!last.equals(StrangerAutomaton.slash))) {
					throw new UnsupportedRegexException(
							"Undelimited preg regexp: \"" + phpRegexOrig + "\"");
				}
				// peel off delimiter
				phpRegexOrig = phpRegex.substring(1, phpRegex.lastIndexOf("/"));
				debug(id + ": regular expression after removing delimeters = "
						+ phpRegexOrig);
			}
			RegExp.restID();// for debugging purposes only
			// following loop is to convert a special character from its representation 
			// to actual character.
			// Example: convert \x41 to A
			while(phpRegexOrig.matches(".*(\\\\x\\w\\w).*")){
				Pattern p = Pattern.compile(".*(\\\\x\\w\\w).*");
				String tempRegExp = phpRegexOrig;
				Matcher m = p.matcher(tempRegExp);		
				m.find();
				String hexChar = m.group(1);
				char c = (char)Integer.valueOf(hexChar.substring(2,4), 16).intValue();
				phpRegexOrig = phpRegexOrig.replace(hexChar, "\\" + c);
			}
//			phpRegexOrig.rep
			RegExp regExp = new RegExp(phpRegexOrig);
			debug(id + ": regExToString = "
					+ regExp.toStringBuilder(new StringBuilder()));
			retMe = regExp.toAutomaton();
		}

		debug(id + " = regExToAuto(" + phpRegexOrig + ") -- end");
		debug("============");
		{
			retMe.setID(id);
			retMe.debugAutomaton();
		}
		return retMe;
	}
	
	/**
	 * Parses a PHP regular expression and converts it into stranger automaton.
	 * Regualr expression should follow preg syntax.
	 * For allowed regular expressions check {@link RegExp}.
	 * @param phpRegexOrig: the string literal representing the regular expression
	 * @return
	 */
	public static StrangerAutomaton regExToAuto(String phpRegexOrig) {
		return regExToAuto(phpRegexOrig, true, traceID);
	}

	/**
	 * constructs a StrangerAutomaton that accepts the result of replacing every
	 * occurrence of a string of patternAuto language in subjectAuto language
	 * with replaceStr. var and indices is the depth of the BDD (number of
	 * variables in the BDD) and ordering of them
	 * If patternAuto or subjectAuto are bottom then it will throw StrangerAutomatonException
	 * IF patternAuto is top then it will throw StrangerAutomatonException
	 * If subjectAuto is top then it will return top as replacing something in top which is not
	 * guaranteed to be a string variable may cause errors
	 * 
	 * @param patternAuto
	 *            : search auto (of type StrangerAutomaton) , replace substrings
	 *            which match elements in L(searchAuto)
	 * @param replaceStr
	 *            : the replace string (of type String) , replace with this
	 *            string
	 * @param subjectAuto
	 *            : target auto (of type StrangerAutomaton) , replace substrings
	 *            in L(subjectAuto)
	 * @param id
	 *            : id of node associated with this auto; used for debugging
	 *            purposes only
	 */
	//TODO: merge this with str_replace as we no longer need preg
	public static StrangerAutomaton reg_replace(StrangerAutomaton patternAuto,
			String replaceStr, StrangerAutomaton subjectAuto, boolean preg,
			int id) {

		debug(id + " = reg_replace(" + patternAuto.ID + ", " + replaceStr
				+ ", " + subjectAuto.ID + ")");
		// Note: the replaceAuto parameter should be of type
		// Automaton not String. We changed it
		// to use the replace function from StrangerLibrary.
		// TODO: Otherwise we need a method to accept all three parameters as
		// automaton in Stranger Library
		debug("calling reg_replace with the following order (" + subjectAuto.ID
				+ ", " + patternAuto.ID + ", " + replaceStr + ")");
		if (replaceStr == null)
			throw new StrangerAutomatonException(
					"SNH: In StrangerAutoatmon.reg_replace: replace string is null.");
		if (patternAuto.isBottom() || subjectAuto.isBottom())
			throw new StrangerAutomatonException(
			"SNH: In StrangerAutoatmon.reg_replace: either patternAuto or subjectAuto is bottom element (phi) which can not be used in replace.");
		else if (patternAuto.isTop())
			throw new StrangerAutomatonException(
			"SNH: In StrangerAutoatmon.reg_replace: patternAuto is top (indicating that the variable may no longer be of type string) and can not be used in replacement");
		else if (subjectAuto.isTop())
			throw new StrangerAutomatonException(
			"SNH: In StrangerAutoatmon.reg_replace: subjectAuto is top (indicating that the variable may no longer be of type string) and can not be used in replacement");
	
		debugToFile("M[" + (traceID) + "] = dfa_replace_extrabit(M["+ subjectAuto.autoTraceID +"], M[" + patternAuto.autoTraceID + "], \"" + replaceStr + "\" , NUM_ASCII_TRACKS, indices_main);//"+id + " = reg_replace(" + patternAuto.ID + ", " + replaceStr
				+ ", " + subjectAuto.ID + ")");
		perfInfo.numOfReplace++;
		long start = System.currentTimeMillis();
		
		StrangerAutomaton retMe = new StrangerAutomaton(
				StrangerLibrary.INSTANCE.dfa_replace_extrabit(subjectAuto.dfa,
						patternAuto.dfa, replaceStr,
						StrangerLibraryWrapper.NUM_ASCII_TRACKS,
						StrangerLibraryWrapper.indices_main));
		
		long stop = System.currentTimeMillis();
		perfInfo.replaceTime += (stop - start);
		
		{
			retMe.ID = id;
			retMe.debugAutomaton();
		}
		return retMe;
	}
	
	/**
	 * constructs a StrangerAutomaton that accepts the result of replacing every
	 * occurrence of a string of patternAuto language in subjectAuto language
	 * with replaceStr. var and indices is the depth of the BDD (number of
	 * variables in the BDD) and ordering of them
	 * 
	 * @param patternAuto
	 *            : search auto (of type StrangerAutomaton) , replace substrings
	 *            which match elements in L(searchAuto)
	 * @param replaceStr
	 *            : the replace string (of type String) , replace with this
	 *            string
	 * @param subjectAuto
	 *            : target auto (of type StrangerAutomaton) , replace substrings
	 *            in L(subjectAuto)
	 */
	//TODO: merge this with str_replace as we no longer need preg
	public static StrangerAutomaton reg_replace(StrangerAutomaton patternAuto,
			String replaceStr, StrangerAutomaton subjectAuto) {
		return reg_replace(patternAuto, replaceStr, subjectAuto, true, traceID);
	}

	/**
	 * constructs a StrangerAutomaton that accepts the result of replacing every
	 * occurrence of a string of searchAuto language in subjectAuto language
	 * with replaceStr. var and indices is the depth of the BDD (number of
	 * variables in the BDD) and ordering of them
	 * 
	 * @param searchAuto
	 *            : search auto (of type StrangerAutomaton) , replace substrings
	 *            which match elements in L(searchAuto)
	 * @param replaceStr
	 *            : the replace string (of type String) , replace with this
	 *            string
	 * @param subjectAuto
	 *            : target auto (of type StrangerAutomaton) , replace substrings
	 *            in L(subjectAuto)
	 * @param id
	 *            : id of node associated with this auto; used for debugging
	 *            purposes only
	 */
	public static StrangerAutomaton str_replace(StrangerAutomaton searchAuto,
			String replaceStr, StrangerAutomaton subjectAuto, int id) {

		debug(id + " = str_replace(" + searchAuto.ID + ", " + replaceStr + ", "
				+ subjectAuto.ID + ")");
		// Note: the original replaceAuto parameter in FSAAutomaton is of type
		// Automaton not String. We changed it
		// to use the replace function from StrangerLibrary which only accepts a
		// string literal.
		debug("calling str_replace with the following order (" + subjectAuto.ID
				+ ", " + searchAuto.ID + ", " + replaceStr + ")");
		if (replaceStr == null)
			throw new StrangerAutomatonException(
					"SNH: In StrangerAutoatmon.str_replace: replace string is null.");
		else if (searchAuto.isBottom() || subjectAuto.isBottom())
			throw new StrangerAutomatonException(
			"SNH: In StrangerAutoatmon.str_replace: either searchAuto or subjectAuto is bottom element (phi) which can not be used in replace.");
		else if (searchAuto.isTop())
			throw new StrangerAutomatonException(
			"SNH: In StrangerAutoatmon.str_replace: searchAuto is top (indicating that the variable may no longer be of type string) and can not be used in replacement");
		else if (subjectAuto.isTop())
			throw new StrangerAutomatonException(
			"SNH: In StrangerAutoatmon.str_replace: subjectAuto is top (indicating that the variable may no longer be of type string) and can not be used in replacement");
	
		
		
		debugToFile("M[" + (traceID) + "] = dfa_replace_extrabit(M["+ subjectAuto.autoTraceID +"], M[" + searchAuto.autoTraceID + "], \"" + replaceStr + "\" , NUM_ASCII_TRACKS, indices_main);//"+id + " = str_replace(" + searchAuto.ID + ", " + replaceStr + ", "
				+ subjectAuto.ID + ")");
		perfInfo.numOfReplace++;
		long start = System.currentTimeMillis();
		
		StrangerAutomaton retMe = new StrangerAutomaton(
				StrangerLibrary.INSTANCE.dfa_replace_extrabit(subjectAuto.dfa,
						searchAuto.dfa, replaceStr,
						StrangerLibraryWrapper.NUM_ASCII_TRACKS,
						StrangerLibraryWrapper.indices_main));
		
		long stop = System.currentTimeMillis();
		perfInfo.replaceTime += (stop - start);
		
		{
			retMe.ID = id;
			retMe.debugAutomaton();
		}
		return retMe;
	}
	
	/**
	 * constructs a StrangerAutomaton that accepts the result of replacing every
	 * occurrence of a string of searchAuto language in subjectAuto language
	 * with replaceStr. var and indices is the depth of the BDD (number of
	 * variables in the BDD) and ordering of them
	 * 
	 * @param searchAuto
	 *            : search auto (of type StrangerAutomaton) , replace substrings
	 *            which match elements in L(searchAuto)
	 * @param replaceStr
	 *            : the replace string (of type String) , replace with this
	 *            string
	 * @param subjectAuto
	 *            : target auto (of type StrangerAutomaton) , replace substrings
	 *            in L(subjectAuto)
	 */
	public static StrangerAutomaton str_replace(StrangerAutomaton searchAuto,
			String replaceStr, StrangerAutomaton subjectAuto) {
		return str_replace(searchAuto, replaceStr, subjectAuto, traceID);
	}
	
	//***************************************************************************************
	//*                                  Backward Replacement                               *
	//***************************************************************************************

	/**
	 * This is for backward analysis to compute the preimage of replace
	  * @param searchAuto
	 *            : search auto (of type StrangerAutomaton) , replace substrings
	 *            which match elements in L(searchAuto)
	 * @param replaceStr
	 *            : the replace string (of type String) , replace with this
	 *            string
	 * @param id
	 *            : id of node associated with this auto; used for debugging
	 *            purposes only
	 * @return
	 */
	public StrangerAutomaton preReplace(StrangerAutomaton searchAuto,
			String replaceString, int id) {
		debug(id + " = preReplace(" + this.ID + ", " + searchAuto.ID + ")");
		if (replaceString == null)
			throw new StrangerAutomatonException(
					"SNH: In StrangerAutoatmon.preReplace: replace string is null.");
		if (searchAuto.isBottom() || this.isBottom())
			throw new StrangerAutomatonException(
			"SNH: In StrangerAutoatmon.preReplace: either searchAuto or subjectAuto is bottom element (phi) which can not be used in replace.");
		else if (searchAuto.isTop())
			throw new StrangerAutomatonException(
			"SNH: In StrangerAutoatmon.preReplace: searchAuto is top (indicating that the variable may no longer be of type string) and can not be used in replacement");
		else if (this.isTop())
			throw new StrangerAutomatonException(
			"SNH: In StrangerAutoatmon.preReplace: subjectAuto is top (indicating that the variable may no longer be of type string) and can not be used in replacement");

		
		debugToFile("M[" + (traceID) + "] = dfa_pre_replace_str(M["+ this.autoTraceID +"], M[" + searchAuto.autoTraceID + "], \"" + replaceString + "\" , NUM_ASCII_TRACKS, indices_main);//"+id + " = preReplace(" + this.ID + ", " + searchAuto.ID + ")");
		perfInfo.numOfPreReplace++;
		long start = System.currentTimeMillis();

		StrangerAutomaton retMe = new StrangerAutomaton(
				StrangerLibrary.INSTANCE.dfa_pre_replace_str(this.dfa,
						searchAuto.dfa, replaceString,
						StrangerLibraryWrapper.NUM_ASCII_TRACKS,
						StrangerLibraryWrapper.indices_main));
		
		long stop = System.currentTimeMillis();
		perfInfo.prereplaceTime += (stop - start);
		
		{
			retMe.setID(id);
			retMe.debugAutomaton();
		}
		return retMe;
	}
	
	/**
	 * This is for backward analysis to compute the preimage of replace
	  * @param searchAuto
	 *            : search auto (of type StrangerAutomaton) , replace substrings
	 *            which match elements in L(searchAuto)
	 * @param replaceStr
	 *            : the replace string (of type String) , replace with this
	 *            string
	 * @return
	 */
	public StrangerAutomaton preReplace(StrangerAutomaton searchAuto,
			String replaceString) {
		return this.preReplace(searchAuto, replaceString, traceID);
	}
	
	//***************************************************************************************
	//*                                  Automata checks                                    *
	//*									-------------------									*
	//* These operations only check current automaton without creating a new one.			*
	//***************************************************************************************
	/**
	 * returns true if L(this auto) intersect L(auto) != phi (empty language)
	 * @param auto
	 * @param id1
	 *            : id of node associated with this auto; used for debugging
	 *            purposes only
	 * @param id2
	 *            : id of node associated with this auto; used for debugging
	 *            purposes only
	 * @return
	 */
	public boolean checkIntersection(StrangerAutomaton auto, int id1, int id2) {
		String debugStr = "checkIntersection(" + this.ID + ", " + auto.ID + ") = ";
		
		if (this.isTop() || auto.isTop()){
			debug(debugStr + "true");
			return true;
		} else if (this.isBottom() || auto.isBottom()){
			debug(debugStr + "false");
			return false;
		}
				
		
		debugToFile("check_intersection(M["+ this.autoTraceID +"],M["+ auto.autoTraceID +"], NUM_ASCII_TRACKS, indices_main);//check_intersection(" + this.ID + ", " + auto.ID + ")");
		int result = StrangerLibrary.INSTANCE.check_intersection(this.dfa,
				auto.dfa, StrangerLibraryWrapper.NUM_ASCII_TRACKS,
				StrangerLibraryWrapper.indices_main);

		{
			debug(debugStr +  (result == 0 ? false : true));
		}

		if (result == 0)
			return this.empty && auto.empty;
		else if (result == 1)
			return true;
		else
			// TODO: we should have our own exception for StrangerAutomaton
			throw new RuntimeException(
					"Error in checkIntersection result for StrangerAutomaton.");
	}
	
	/**
	 * returns true if L(this auto) intersect L(auto) != phi (empty language)
	 * @param auto
	 * @return
	 */
	public boolean checkIntersection(StrangerAutomaton auto) {
		return this.checkIntersection(auto, -1, -1);
	}

	/**
	 * return true if parameter auto includes this auto. i.e. returns true if L(this auto)
	 * is_subset_of L(parameter auto)
	 * 
	 * @param auto
	 * @param id1
	 *            : id of node associated with this auto; used for debugging
	 *            purposes only
	 * @param id2
	 *            : id of node associated with this auto; used for debugging
	 *            purposes only * @return
	 */
	public boolean checkInclusion(StrangerAutomaton auto, int id1, int id2) {
		String debugStr = "checkInclusion(" + this.ID + ", " + auto.ID + ") = ";
		// phi is always a subset of any other set, top is always superset of anything
		if (this.isBottom() || auto.isTop()){
			debug(debugStr + "true");
			return true;
		} else if (auto.isBottom() || this.isTop()){
			debug(debugStr + "false");
			return false;
		} 
		
		debugToFile("check_inclusion(M["+ this.autoTraceID +"],M["+ auto.autoTraceID +"], NUM_ASCII_TRACKS, indices_main);//check_inclusion(" + this.ID + ", " + auto.ID + ")");
		int result = StrangerLibrary.INSTANCE.check_inclusion(this.dfa,
				auto.dfa, StrangerLibraryWrapper.NUM_ASCII_TRACKS,
				StrangerLibraryWrapper.indices_main);

		{
			debug(debugStr +  (result == 0 ? false : true));
		}

		if (result == 0)
			return false;
		else if (result == 1){
			// if this contains empty string then auto must contain empty string
			if (this.empty)
				return auto.empty;
			else
				return true;
		}
		
		else
			// TODO: we should have our own exception for StrangerAutomaton
			throw new RuntimeException(
					"Error in checkInclusion result for StrangerAutomaton.");
	}

	/**
	 * return true if parameter auto includes this auto. i.e. returns true if L(this auto)
	 * is_subset_of L(parameter auto)
	 * 
	 * @param auto
	 * @return
	 */
	public boolean checkInclusion(StrangerAutomaton auto) {
		return this.checkInclusion(auto, -1, -1);
	}
	
	/**
	 * returns true if this auto is equivalent to parameter auto. i.e. returns true if
	 * L(parameter auto) == L(this auto)
	 * 
	 * @param auto
	 * @param id1
	 *            : id of node associated with this auto; used for debugging
	 *            purposes only
	 * @param id2
	 *            : id of node associated with this auto; used for debugging
	 *            purposes only * @return
	 */
	public boolean checkEquivalence(StrangerAutomaton auto, int id1, int id2) {
		String debugStr = "checkEquivalence(" + this.ID + ", " + auto.ID + ") = ";
		
		if ((this.isTop() && auto.isTop()) || (this.isBottom() && auto.isBottom())){
			debug(debugStr + "true");
			return true;
		}
		else if (this.isTop() || this.isBottom() || auto.isTop() || auto.isBottom()){
			debug(debugStr + "false");
			return false;
		}
		
		debugToFile("check_equivalence(M["+ this.autoTraceID +"],M["+ auto.autoTraceID +"], NUM_ASCII_TRACKS, indices_main);//check_equivalence(" + this.ID + ", " + auto.ID + ")");
		int result = StrangerLibrary.INSTANCE.check_equivalence(this.dfa,
				auto.dfa, StrangerLibraryWrapper.NUM_ASCII_TRACKS,
				StrangerLibraryWrapper.indices_main);

		{
			debug( debugStr + (result == 0 ? false : true));
		}

		if (result == 0)
			return false;
		else if (result == 1)
			// They should both either include or exclude empty string
			return (this.empty == auto.empty);
		else
			// TODO: we should have our own exception for StrangerAutomaton
			throw new RuntimeException(
					"Error in checkInclusion result for StrangerAutomaton.");
	}
	
	/**
	 * returns true if this auto is equivalent to parameter auto. i.e. returns true if
	 * L(parameter auto) == L(this auto)
	 * 
	 * @param auto
	 * @return
	 */
	public boolean checkEquivalence(StrangerAutomaton auto) {
		return this.checkEquivalence(auto, -1, -1);
	}
	
	@Override
	/**
	 * returns the result of this.checkEquivalence(other)
	 */
	public boolean equals(Object other) {
		return (other instanceof StrangerAutomaton) &&
			this.checkEquivalence((StrangerAutomaton)other);
	}
	
	/**
	 * returns true if this auto is empty. i.e. returns true if
	 * L(this auto) == phi (empty set)
	 * 
	 * @param id1
	 *            : id of node associated with this auto; used for debugging
	 *            purposes only
	 * 
	 */
	public boolean checkEmptiness() {
		String debugStr = "checkEmptiness(" + this.ID + ") = ";
		if (this.isBottom()){
			debug(debugStr + "true");
			return true;
		}
		else if (this.isTop()){
			debug(debugStr + "false");
			return false;
		}
		if(StrangerAutomaton.makeAnyStringL1ToL2(0, 0).checkEquivalence(this))
			return false;
		debugToFile("check_emptiness(M["+ this.autoTraceID +"], NUM_ASCII_TRACKS, indices_main);//check_emptiness(" + this.ID + ")");
		int result = StrangerLibrary.INSTANCE.check_emptiness(this.dfa, StrangerLibraryWrapper.NUM_ASCII_TRACKS,
				StrangerLibraryWrapper.indices_main);
		{
			debug(debugStr + (result == 0 ? false : true));
		}

		if (result == 0)
			return false;
		else if (result == 1)
			// if it contains empty string then it is not Phi
			return !this.empty;
		else
			// TODO: we should have our own exception for StrangerAutomaton
			throw new RuntimeException(
					"Error in checkEmptiness result for StrangerAutomaton.");
	}
	
	
	/**
	 * returns true if this L(automaton) == bottom
	 * if you need to check if the language is actual phi use {@link checkEmptiness}
	 */
	public boolean isEmpty() {
		return isBottom();
	}


	/**
	 * check if this automaton only accepts empty string i.e. string of length
	 * 0.
	 * 
	 * @return
	 */
	public boolean checkEmptyString() {
		if (this.isBottom() || this.isTop())
			return false;
		debugToFile("checkEmptyString(M["+ this.autoTraceID +"]);//checkEmptyString(" + this.ID + ")");
		return this.empty;
//		if (StrangerLibrary.INSTANCE.checkEmptyString(this.dfa) == 1)
//			return true;
//		else
//			return false;
	}
	
	/**
	 * check if this automaton represents the bottom of the lattice.
	 * @return
	 */
	public boolean isBottom() {
		//TODO: checkEmptiness causes lots of crashes so be careful here
		return (this.bottom == true);
	}
	
	/**
	 * check if this automaton represents the top of the lattice.
	 * @return
	 */
	public boolean isTop() {
		return (this.top == true);
	}
	
	
	//***************************************************************************************
	//*                                  PHP Function models                                *
	//***************************************************************************************
	//TODO: there must be a better place for these function models outside the StrangerAutomaton class.
	// One way of doing this is to have an anonymous object for each model in a separate class called
	// FunctionModels

	public static StrangerAutomaton addslashes(StrangerAutomaton subjectAuto,
			int id) {
		debug(id + " = addSlashes(" + subjectAuto.ID + ") -- start");

		// the easy way: addslashes is the same as applying str_replace
		// several times:
		// Note that we need to escape the backslash in Java string. So to
		// add one slash such as \ we have the write this in makeString
		// as \\ to let Java escape the second slash.

		// escape backslash \
		// \ -> \\
		StrangerAutomaton searchAuto = StrangerAutomaton.makeString("\\", 1);
		String replaceStr = "\\\\";
		StrangerAutomaton retMe = str_replace(searchAuto, replaceStr,
				subjectAuto, 2);

		// escape single quota '
		// ' -> \'
		searchAuto = StrangerAutomaton.makeString("'", 3);
		replaceStr = "\\'";
		retMe = str_replace(searchAuto, replaceStr,
				retMe, 4);

		// escape double quota "
		// " -> \"
		searchAuto = StrangerAutomaton.makeString("\"", 5);
		replaceStr = "\\\"";
		retMe = str_replace(searchAuto, replaceStr,
				retMe, 6);

		debug(id + " = addSlashes(" + subjectAuto.ID + ") -- end");

		{
			subjectAuto.ID = id;
			subjectAuto.debugAutomaton();
		}

		return retMe;
	}
	
	public static StrangerAutomaton pre_addslashes(StrangerAutomaton subjectAuto,
			int id) {
		debug(id + " = pre_addslashes(" + subjectAuto.ID + ") -- start");

		
		// Note that we need to escape the backslash in Java string. So to
		// add one slash such as \ we have the write this in makeString
		// as \\ to let Java escape the second slash.

		// pre escape backslash \
		// \ -> \\
		StrangerAutomaton searchAuto = StrangerAutomaton.makeString("\\", 1);
		String replaceStr = "\\\\";
		StrangerAutomaton retMe = subjectAuto.preReplace(
				searchAuto, replaceStr, 2);
		
		// pre escape single quota '
		// ' -> \'
		searchAuto = StrangerAutomaton.makeString("'", 5);
		replaceStr = "\\'";
		retMe = retMe.preReplace(
				searchAuto, replaceStr, 2);
		
		// pre escape double quota "
		// " -> \"
		searchAuto = StrangerAutomaton.makeString("\"", 5);
		replaceStr = "\\\"";
		retMe = retMe.preReplace(
				searchAuto, replaceStr, 2);
		
		
		debug(id + " = pre_addslashes(" + subjectAuto.ID + ") -- end");

		{
			subjectAuto.ID = id;
			subjectAuto.debugAutomaton();
		}

		return retMe;
	}
	
	public static StrangerAutomaton stripslashes(StrangerAutomaton subjectAuto,
			int id) {
		debug(id + " = stripSlashes(" + subjectAuto.ID + ") -- start");

		// stripslashes does the oppisite of addslashes.
		// Note that we need to escape the backslash in Java string. So to
		// add one slash such as \ we have the write this in makeString
		// as \\ to let Java escape the second slash.

		

		// unescape single quota '
		// ' -> \'
		StrangerAutomaton searchAuto = StrangerAutomaton.makeString("\\'", 1);
		String replaceStr = "'";
		subjectAuto = str_replace(searchAuto, replaceStr,
				subjectAuto, 2);

		// unescape double quota "
		// " -> \"
		searchAuto = StrangerAutomaton.makeString("\\\"", 3);
		replaceStr = "\"";
		subjectAuto = str_replace(searchAuto, replaceStr,
				subjectAuto, 4);

		// unescape backslash \
		// \ -> \\
		searchAuto = StrangerAutomaton.makeString("\\\\", 5);
		replaceStr = "\\";
		subjectAuto = str_replace(searchAuto, replaceStr,
				subjectAuto, 6);
		
		debug(id + " = stripSlashes(" + subjectAuto.ID + ") -- end");

		{
			subjectAuto.ID = id;
			subjectAuto.debugAutomaton();
		}

		return subjectAuto;
	}
	
	public static StrangerAutomaton pre_stripslashes(StrangerAutomaton subjectAuto,
			int id) {
		debug(id + " = pre_stripslashes(" + subjectAuto.ID + ") -- start");

		
		// Note that we need to escape the backslash in Java string. So to
		// add one slash such as \ we have the write this in makeString
		// as \\ to let Java escape the second slash.

		// pre unescape single quota '
		// ' -> \'
		StrangerAutomaton searchAuto = StrangerAutomaton.makeString("\\'", 1);
		String replaceStr = "'";
		StrangerAutomaton retMe = subjectAuto.preReplace(
				searchAuto, replaceStr, 2);
		
		// pre unescape double quota "
		// " -> \"
		searchAuto = StrangerAutomaton.makeString("\\\"", 3);
		replaceStr = "\"";
		retMe = retMe.preReplace(
				searchAuto, replaceStr, 2);
		
		// pre unescape backslash \
		// \ -> \\
		searchAuto = StrangerAutomaton.makeString("\\\\", 5);
		replaceStr = "\\";
		retMe = retMe.preReplace(
				searchAuto, replaceStr, 2);
		
		
		debug(id + " = pre_stripslashes(" + subjectAuto.ID + ") -- end");

		{
			subjectAuto.ID = id;
			subjectAuto.debugAutomaton();
		}

		return retMe;
	}
	
	public static StrangerAutomaton mysql_real_escape_string(StrangerAutomaton subjectAuto,
			int id) {
		debug(id + " = mysql_real_escape_string(" + subjectAuto.ID + ") -- start");

		// the easy way: mysql_real_escape_string is very similar to addslashes
		// Note that we need to escape the backslash in Java string. So to
		// add one slash such as \ we have the write this in makeString
		// as \\ to let Java escape the second slash.

		// escape backslash \
		// \ -> \\
		StrangerAutomaton searchAuto = StrangerAutomaton.makeString("\\", 1);
		String replaceStr = "\\\\";
		StrangerAutomaton retMe = str_replace(searchAuto, replaceStr,
				subjectAuto, 2);

		// escape single quota '
		// ' -> \'
		searchAuto = StrangerAutomaton.makeString("'", 3);
		replaceStr = "\\'";
		retMe = str_replace(searchAuto, replaceStr,
				retMe, 4);

		// escape double quota "
		// " -> \"
		searchAuto = StrangerAutomaton.makeString("\"", 5);
		replaceStr = "\\\"";
		retMe = str_replace(searchAuto, replaceStr,
				retMe, 6);

		debug(id + " = mysql_real_escape_string(" + subjectAuto.ID + ") -- end");

		{
			subjectAuto.ID = id;
			subjectAuto.debugAutomaton();
		}

		return retMe;
	}
	
	public static StrangerAutomaton pre_mysql_real_escape_string(StrangerAutomaton subjectAuto,
			int id) {
		debug(id + " = pre_mysql_real_escape_string(" + subjectAuto.ID + ") -- start");

		
		// Note that we need to escape the backslash in Java string. So to
		// add one slash such as \ we have the write this in makeString
		// as \\ to let Java escape the second slash.

		// pre escape backslash \
		// \ -> \\
		StrangerAutomaton searchAuto = StrangerAutomaton.makeString("\\", 1);
		String replaceStr = "\\\\";
		StrangerAutomaton retMe = subjectAuto.preReplace(
				searchAuto, replaceStr, 2);
		
		// pre escape single quota '
		// ' -> \'
		searchAuto = StrangerAutomaton.makeString("'", 5);
		replaceStr = "\\'";
		retMe = retMe.preReplace(
				searchAuto, replaceStr, 2);
		
		// pre escape double quota "
		// " -> \"
		searchAuto = StrangerAutomaton.makeString("\"", 5);
		replaceStr = "\\\"";
		retMe = retMe.preReplace(
				searchAuto, replaceStr, 2);
		
		
		debug(id + " = pre_mysql_real_escape_string(" + subjectAuto.ID + ") -- end");

		{
			subjectAuto.ID = id;
			subjectAuto.debugAutomaton();
		}

		return retMe;
	}

	public static StrangerAutomaton nl2br(StrangerAutomaton subjectAuto,
			int id) {
		debug(id + " = nl2br(" + subjectAuto.ID + ") -- start");

		
		// Note that we need to escape the backslash in Java string. So to
		// add one slash such as \ we have the write this in makeString
		// as \\ to let Java escape the second slash.

		StrangerAutomaton searchAuto = StrangerAutomaton.makeString("\\n", 1);
		String replaceStr = "<br/>";
		StrangerAutomaton retMe = StrangerAutomaton.str_replace(
				searchAuto, replaceStr, subjectAuto, 2);
		
		
		debug(id + " = nl2br(" + subjectAuto.ID + ") -- end");

		{
			subjectAuto.ID = id;
			subjectAuto.debugAutomaton();
		}

		return retMe;
	}
	
	public static StrangerAutomaton pre_nl2br(StrangerAutomaton subjectAuto,
			int id) {
		debug(id + " = pre_nl2br(" + subjectAuto.ID + ") -- start");

		
		// Note that we need to escape the backslash in Java string. So to
		// add one slash such as \ we have the write this in makeString
		// as \\ to let Java escape the second slash.

		StrangerAutomaton searchAuto = StrangerAutomaton.makeString("\\n", 1);
		String replaceStr = "<br/>";
		StrangerAutomaton retMe = subjectAuto.preReplace(
				searchAuto, replaceStr, 2);
		
		
		debug(id + " = pre_nl2br(" + subjectAuto.ID + ") -- end");

		{
			subjectAuto.ID = id;
			subjectAuto.debugAutomaton();
		}

		return retMe;
	}

	
	//***************************************************************************************
	//*                                  Old stuff                                          *
	//***************************************************************************************
	
	//TODO: should be removed at sometime
	
	/**
	 * @deprecated
	 */
	// returns an automaton for the language of undesired strings for sql
	// analysis (test);
	// MISSING HERE: double quotes and other evil stuff (see PHP's addslashes())
	public static StrangerAutomaton getUndesiredSQLTest() {

		debug("Building automaton for SQL vulnerabilities");
		debug("----------------------------------------");
		
		//initializeAlphabit("UPDATE `pblguestbook_config`SET=,';DROPDATABASE");

		
//		StrangerAutomaton autoSingleQuota = StrangerAutomaton
//				.makeString("`", 1);
//		StrangerAutomaton autoComplementSingleQuotaStar = autoSingleQuota
//				.complement(-1).kleensStar(2);
//		StrangerAutomaton autoEquals = StrangerAutomaton.makeString("=", 3);
//		StrangerAutomaton autoSingleQuote = StrangerAutomaton
//				.makeString("'", 4);
//		StrangerAutomaton autoComplementSingleQuoteStar = autoSingleQuote
//				.complement(-1).kleensStar(5);
//		StrangerAutomaton autoComma = StrangerAutomaton.makeString(",", 6);
//		StrangerAutomaton autoAnyString = StrangerAutomaton.makeAnyString(7);// change me back to UPDATE...
//		StrangerAutomaton update = StrangerAutomaton.makeString("UPDATE `pblguestbook_config` SET ", 7);
//		// Building attack pattern : Sigma* ` (Sigma-`)* ` = ' (Sigma-')* '; DROP DATABASE
//		// Sigma*
//		// TODO: This is a specific attack pattern only for sql test test41.php
//		StrangerAutomaton autoAttackPattern = update.concatenate(
//				autoSingleQuota, 8);
//		autoAttackPattern = autoAttackPattern.concatenate(
//				autoComplementSingleQuotaStar, 8);
//		autoAttackPattern = autoAttackPattern.concatenate(autoSingleQuota, 9);
//		autoAttackPattern = autoAttackPattern.concatenate(autoEquals, 10);
//		autoAttackPattern = autoAttackPattern.concatenate(autoSingleQuote, 11);
//		autoAttackPattern = autoAttackPattern.concatenate(
//				autoComplementSingleQuoteStar, 12);
//		autoAttackPattern = autoAttackPattern.concatenate(autoSingleQuote, 13);
//		autoAttackPattern = autoAttackPattern.concatenate(autoComma, 14);
//		autoAttackPattern = autoAttackPattern.concatenate(makeString("; DROP DATABASE", 15),16).concatenate(autoAnyString, 17);
		StrangerAutomaton autoAttackPattern = regExToAuto("/.*'or 1=1'.*/", true, 0);


		debug("----------------------------------------");

		return autoAttackPattern;
	}

	/**
	 * @deprecated
	 */
	// returns an automaton for the language of undesired strings for xss
	// analysis (test);
	public static StrangerAutomaton getUndesiredXSSTest() {
		//debug("Building automaton for XSS vulnerabilities");
		//debug("------------------------------------------");
		//initializeAlphabit("<SCRIPT");
		
		
		StrangerAutomaton autoKleensStar = StrangerAutomaton.makeAnyString(1);
		//StrangerAutomaton retMe = autoKleensStar.concatenate(makeString("<", -1), -1).concatenate(autoKleensStar, -1);
//		StrangerAutomaton autoPointyleftCapital = StrangerAutomaton.makeString(
//				"<SCRIPT ", 3);
//		StrangerAutomaton autoPointyleftSmall = StrangerAutomaton.makeString(
//				"< ", 4);
//		StrangerAutomaton small = autoKleensStar.concatenate(autoPointyleftSmall, 6)
//		.concatenate(autoKleensStar, 7);
////		StrangerAutomaton capital = autoKleensStar.concatenate(autoPointyleftCapital, 10)
////		.concatenate(autoKleensStar, 11);
//		StrangerAutomaton retMe = small;//.union(capital, 14);
		 StrangerAutomaton retMe = regExToAuto("/.*\\<SCRIPT .*\\>.*/", true, 0);
		//debug("------------------------------------------");

		return retMe;
	}
	
	/**
	 * @deprecated
	 */
	public static StrangerAutomaton getUndesiredMFETest() {
		StrangerAutomaton retMe = regExToAuto("//evil/",true,0);
		return retMe;
	}

	/**
	 *  This is used for abstraction which is not implemented yet.
	 * @param attackStringAlphabit
	 */
	public static void initializeAlphabit(String attackStringAlphabit) {
		StringBuilder alphabit = new StringBuilder(attackStringAlphabit);
		// we encode each letter of the attack pattern with an integer
		// when we finish we take the log to decide how many bits do we
		// need for actual encoding within the automaton.
		encoding = new HashMap<Character,Character>();
		char cEncode = (char) 1;
		for (int i = 0; i < alphabit.length(); i++){
			char c = alphabit.charAt(i);
			if (!encoding.containsKey(c)){
				encoding.put(c, cEncode);			
				cEncode = (char) (cEncode + 1);
			}
		}
		StrangerLibraryWrapper.NUM_ASCII_TRACKS = (int) Math.ceil((Math.log(alphabit.length() + 3)/Math.log(2)));
	}

	//***************************************************************************************
	//*                                  Debugging                                          *
	//***************************************************************************************
	public void printAutomaton() {

		System.out.flush();
		debugToFile("dfaPrintVerbose(M[" + this.autoTraceID + "]);");
		StrangerLibrary.INSTANCE.dfaPrintVerbose(this.dfa);
		debugToFile("flush_output();");
		StrangerLibrary.INSTANCE.flush_output();

	}
	
	/**
	 * prints some statistics about the automaton
	 */
	public void printAutomatonVitals() {

		System.out.flush();
		debugToFile("dfaPrintVitals(M[" + this.autoTraceID + "]);");
		StrangerLibrary.INSTANCE.dfaPrintVitals(this.dfa);
		debugToFile("flush_output();");
		StrangerLibrary.INSTANCE.flush_output();

	}
	
	/**
	 * Prints the current automaton to the out stream in a dot format (see Graphviz).
	 * Unfortunately until now there is no interface to provide a file to the C library
	 * to print the output into yet :-( 
	 */
	public void toDot(){
		System.out.flush();
		debugToFile("dfaPrintGraphviz(M["+ this.autoTraceID +"], NUM_ASCII_TRACKS, indices_main);//dfaPrintGraphviz(" + this.ID + ")");
		StrangerLibrary.INSTANCE.dfaPrintGraphviz(this.dfa, StrangerLibraryWrapper.NUM_ASCII_TRACKS, StrangerLibraryWrapper.indices_main);
		debugToFile("flush_output();");
		StrangerLibrary.INSTANCE.flush_output();
		System.out.println("EmpytString:" + empty);
		System.out.flush();
	}

	/**
	 * Debug levels:
	 * 0- print nothing at all.
	 * 1- print automata operations
	 * 3- print the automata
	 */
	public static int debugLevel = 0;

	/**
	 * For verbose output (debugging).
	 * prints each automaton operation (concat, replace, ...etc) along with IDs of automata operands
	 * @param s
	 */
	public static void debug(String s) {
		if (debugLevel >= 1)
			System.out.println(s);
	}

	/** 
	 * prints the internal c representation for the automaton for debugging purposes
	 */
	protected void debugAutomaton() {
		if (debugLevel >= 3) {
//			this.printAutomaton();
			this.toDot();
		}

	}
	
	/**
	 * ask the c library for the amount of the allocated memory.
	 * @return
	 */
	public static com.sun.jna.NativeLong getAllocatedMemory(){
		debugToFile("mem_allocated()");
		return StrangerLibrary.INSTANCE.mem_allocated();
	}
	
	/**
	 * ask the c library for the amount of the allocated memory.
	 * @return
	 */
	public static double getAllocatedMemoryDouble(){
		debugToFile("mem_allocated()");
		return StrangerLibrary.INSTANCE.mem_allocated().doubleValue();
	}
	
//	public void dumpStrangerDfaMemory(){
//		int[] a = this.dfa.getPointer().getIntArray(0, 5);
//		for (int i = 0; i < 5; i++){
//			System.out.print("0x" +Integer.toHexString(a[i])+",");
//		}
//		debug("");
//	}
	
	public void dumpStrangerDfaPointerMemory(){
		int[] a = this.dfa.getIntArray(0, 5);
		for (int i = 0; i < 5; i++){
			System.out.print("0x" +Integer.toHexString(a[i])+",");
		}
		debug("");
	}
	
	
	//***************************************************************************************
	//*                               C execution trace                                     *
	//*                               -----------------                                     *
	//* this will output c code fortrace files. One file per each analysis run.             *
	//* This is important so that we can run a trace in a c program directly for debugging. *
	//***************************************************************************************
	
	/**
	 * used for the verbose output that shows operations names and automaton
	 * they operate on.
	 */
	protected int autoTraceID;
	/**
	 * used for array index in the output c trace.
	 */
	public static int traceID = 0;
	// this is used to reset the traceID after processing each sink. 
	// this is because we need for each sink ctrace some space in the
	// array of ctrace for the attack pattern
	// So we will set this to the index in array after the index of the 
	// dfa of the attack pattern (this is set in XSSStrangerSanitAnalysis constructor)
	public static int baseTraceID = 0;
	
	/**
	 * used to avoid duplicate variable defs in the output c trace.
	 */
	public static int tempTraceID = 0;
	public static int baseTempTraceID = 0;
	
	private static FileWriter fstream;
    private static BufferedWriter out;
	
    private static void resetTraceID(){
    	traceID = baseTraceID;
    	tempTraceID = baseTempTraceID;
    }
    
    /**
     * creates a new file that will hold the c trace 
     * @param name
     */
	public static void openCtraceFile(String name){
		try{
			resetTraceID();
			fstream = new FileWriter(name);
			out = new BufferedWriter(fstream);
			out.write("int* indices_main = (int *) allocateAscIIIndexWithExtraBit(NUM_ASCII_TRACKS);\nint i;\nDFA* M[1000];\nfor (i = 0; i < 1000; i++)\n\t M[i] = 0;\n");
		} catch (Exception e) {
			throw new StrangerAutomatonException("exec_trace.c file can not be opened.");
		}
	}
	
	/**
     * appends the c trace to a previous file
     * @param name
     */
	public static void appendCtraceFile(String name){
		try{
			resetTraceID();
			fstream = new FileWriter(name, true);
			out = new BufferedWriter(fstream);
		} catch (Exception e) {
			throw new StrangerAutomatonException("exec_trace.c file can not be opened." + e.getMessage());
		}
	}
	
//	private static void readTraceID(String name) {
//		FileReader frstream;
//		try {
//			frstream = new FileReader(name);
//			BufferedReader tempIn = new BufferedReader(frstream);
//			String temp = null, trID = null, tempTrID = null;
//			temp = tempIn.readLine();
//			while (temp != null){
//				tempTrID = trID;
//				trID = temp;
//				temp = tempIn.readLine();
//			}
//			traceID = Integer.parseInt(trID.substring(2));
//			tempTraceID = Integer.parseInt(tempTrID.substring(2));
//			frstream.close();
//		} catch (Exception e) {
//			resetTraceID();
//		} 
//		
//	}

	/**
	 * closes the c trace file
	 */
	public static void closeCtraceFile(){
		if (fstream != null)
			try {
				out.write("for (i = 0; i < 1000; i++)\n\tif (M[i] != 0){\n\t\tdfaFree(M[i]);\n\t\tM[i] = 0;\n}\nprintf(\"Finished execution.\");\n");
				out.flush();
				fstream.close();
			} catch (IOException e) {
				throw new StrangerAutomatonException("Can not close file exec_trace.c");
			}
	}
	
//	/**
//	 * closes the c trace file without writing any cleanup code so that user can append to this c trace file
//	 */
//	public static void tempCloseCtraceFile(){
//		if (fstream != null)
//			try {
//				out.write("//" + traceID + "\n" + "//" + tempTraceID);
//				fstream.close();
//			} catch (IOException e) {
//				throw new StrangerAutomatonException("Can not close file exec_trace.c");
//			}
//	}
	
	/**
	 * writes the c code equivalent to the current automaton operation into the c trace file.
	 * If the file is not manually created then it will call 
	 * <code>StrangerAutomaton.openCtraceFile<\code> and pass it the following file name:
	 * <i>tempDir</i>/stranger_automaton_exec_trace.c where <i>tempDir</i> is the value
	 * of <code>java.io.tempdir</code>.
	 * @param str: value to be written to c trace file
	 */
	public static void debugToFile(String str) {
		//if (debugLevel >= 2) {
			if (fstream == null){
				String property = "java.io.tmpdir"; 
				// Get the temporary directory and print it. 
				String tempDir = System.getProperty(property);
				openCtraceFile(tempDir + "/stranger_automaton_exec_trace.c");
			}
			try {
				out.write(str + "\n");
				// always flush cause we do not know when a crash may occur
				out.flush();
			} catch (IOException e) {
				throw new StrangerAutomatonException("Can not write to exec_trace.c file");
			}
		//}
	}
	
	/**
	 * escape special characters when printing the c trace
	 * @param s
	 * @return
	 */
	private static String escapeSpecialChars(String s) {
		StringBuilder b = new StringBuilder();
		boolean skip  = false;
		for (int i = 0; i < s.length(); i++){
			char c = s.charAt(i);
			if (c == '\n' ){
				if (!skip)
					b.append("\\n");
				else
					skip = false;
			}
			else if (c == '\r'){
				b.append("\\n");skip = true;
			}
			else if (c == '"')
				b.append("\\\"" );
			else if (c == '\\')
				b.append("\\\\" );
			else
				b.append(c);
		}
		return b.toString();
	}
	//***************************************************************************************
	
	//***************Interace added by Scott Kausler 12-5-13

    /**
     * check if dfa accepts only empty string
     */
	public boolean checkOnlyEmptyString() {
		String debugStr = "checkOnlyEmptyString(" + this.ID + ") = ";
		
		if (this.isTop()|| this.isBottom() || !this.empty){
			debug(debugStr + "false");
			return false;
		}
		
		debugToFile("checkOnlyEmptyString(M["+ this.autoTraceID +"]);//checkOnlyEmptyString(" + this.ID + ")");
		int result = StrangerLibrary.INSTANCE.checkOnlyEmptyString(this.dfa,
				StrangerLibraryWrapper.NUM_ASCII_TRACKS,
				StrangerLibraryWrapper.indices_main);

		{
			debug( debugStr + (result == 0 ? false : true));
		}

		if (result == 0)
			return false;
		else if (result == 1)
			return true;
		else
			// TODO: we should have our own exception for StrangerAutomaton
			throw new RuntimeException(
					"Error in checkOnlyEmptyString result for StrangerAutomaton.");
	}
	
    /**
    if L(M) is a singleton set, it will return the string element
    in this set otherwise it will return NULL.
    NULL means not singleton (either empty language or accepts more
    than one string
    */
	public String isSingleton(){
		String debugStr = "isSingleton(" + this.ID + ") = ";
		
//		if (this.isTop()|| this.isBottom()){
//			debug(debugStr + "false");
//			return false;
//		}

		debugToFile("isSingleton(M["+ this.autoTraceID +"]);//isSingleton(" + this.ID + ")");
		String result = StrangerLibrary.INSTANCE.isSingleton(this.dfa,
				StrangerLibraryWrapper.NUM_ASCII_TRACKS,
				StrangerLibraryWrapper.indices_main);
		{
			debug( debugStr + result);
		}
//		if(result!=null && StrangerAutomaton.makeAnyStringL1ToL2(0, 0).checkInclusion(this)){
//			//Sometimes a "singleton" accepts both the empty string and an additional string.
//			StrangerAutomaton temp=this.intersect(StrangerAutomaton.makeAnyStringL1ToL2(0, 0).complement());
//			debugToFile("isSingleton(M["+ this.autoTraceID +"]);//isSingleton(" + this.ID + ")");
//			String newresult = StrangerLibrary.INSTANCE.isSingleton(temp.dfa,
//					StrangerLibraryWrapper.NUM_ASCII_TRACKS,
//					StrangerLibraryWrapper.indices_main);
//			{
//				debug( debugStr + newresult);
//			}
//			if(newresult !=null)
//				return null;
//		}
		return result;
	}
	
    /**
     * returns true (1) if {|w| < n: w elementOf L(M) && n elementOf Integers}
     * In other words length of all strings in the language is bounded by a value n
     */
	public boolean isLengthFinite() {
		String debugStr = "isLengthFinite(" + this.ID + ") = ";
		
//		if (this.isTop()|| this.isBottom() || !this.empty){
//			debug(debugStr + "false");
//			return false;
//		}
		
		debugToFile("isLengthFinite(M["+ this.autoTraceID +"]);//isLengthFinite(" + this.ID + ")");
		int result = StrangerLibrary.INSTANCE.isLengthFiniteTarjan(this.dfa,
				StrangerLibraryWrapper.NUM_ASCII_TRACKS,
				StrangerLibraryWrapper.indices_main);

		{
			debug( debugStr + (result == 0 ? false : true));
		}

		if (result == 0)
			return false;
		else if (result == 1)
			return true;
		else
			// TODO: we should have our own exception for StrangerAutomaton
			throw new RuntimeException(
					"Error in checkOnlyEmptyString result for StrangerAutomaton.");
	}
	
	public boolean checkMembership(String input) {
		String debugStr = "checkMembership(" + this.ID + ","+input+") = ";
		
//		if (this.isTop()|| this.isBottom() || !this.empty){
//			debug(debugStr + "false");
//			return false;
//		}
		
		debugToFile("checkMembership(M["+ this.autoTraceID +","+input+"]);//checkMembership(" + this.ID + ","+input+")");
		int result = StrangerLibrary.INSTANCE.checkMembership(this.dfa, input,
				StrangerLibraryWrapper.NUM_ASCII_TRACKS,
				StrangerLibraryWrapper.indices_main);

		{
			debug( debugStr + (result == 0 ? false : true));
		}

		if (result == 0)
			return false;
		else if (result == 1)
			return true;
		else
			// TODO: we should have our own exception for StrangerAutomaton
			throw new RuntimeException(
					"Error in checkMembership result for StrangerAutomaton.");
	}
	
    /**Output M' so that L(M')={w| w'\in \Sigma*, ww' \in L(M), c_1 = |w| }
     */
	public StrangerAutomaton prefix(int c1) {
		return this.prefix(c1,c1);
	}
	
    /**Output M' so that L(M')={w| w'\in \Sigma*, ww' \in L(M), c_1 <= |w|<=c_2 }
     */
	public StrangerAutomaton prefix(int c1, int c2) {
		debug(traceID + " = prefix(" + this.ID + ")");

		// if top or bottom then do not use the c library as dfa == null
		if (this.isBottom())
			return makeBottom(this.ID);

		debugToFile("M[" + traceID + "] = prefix(M["+ this.autoTraceID +"]);//"+traceID + " = prefix(" + this.ID + ")");
		perfInfo.numOfPrefix++;
		long start = System.currentTimeMillis();
		
		StrangerAutomaton retMe = new StrangerAutomaton(
				StrangerLibrary.INSTANCE.dfa_Suffix(this.dfa, c1,c2, 
						StrangerLibraryWrapper.NUM_ASCII_TRACKS,
						StrangerLibraryWrapper.indices_main		
						));
		
		
		long stop = System.currentTimeMillis();
		perfInfo.suffixTime += (stop - start);		

		{
			retMe.setID(traceID);
			retMe.debugAutomaton();
		}
		return retMe;
	}
	
    /**Output M' so that L(M')={w| w'\in \Sigma*, w'w \in L(M), c_1 = |w| }
     */
	public StrangerAutomaton suffix(int c1) {
		return this.suffix(c1,c1);
	}
	
    /**Output M' so that L(M')={w| w'\in \Sigma*, w'w \in L(M), c_1 <= |w|<=c_2 }
     */
	public StrangerAutomaton suffix(int c1, int c2) {
		debug(traceID + " = suffix(" + this.ID + ")");

		// if top or bottom then do not use the c library as dfa == null
		if (this.isBottom())
			return makeBottom(this.ID);

		debugToFile("M[" + traceID + "] = suffix(M["+ this.autoTraceID +"]);//"+traceID + " = suffix(" + this.ID + ")");
		perfInfo.numOfPrefix++;
		long start = System.currentTimeMillis();
		
		StrangerAutomaton retMe = new StrangerAutomaton(
				StrangerLibrary.INSTANCE.dfa_Prefix(this.dfa, c1,c2, 
						StrangerLibraryWrapper.NUM_ASCII_TRACKS,
						StrangerLibraryWrapper.indices_main		
						));
		
		
		long stop = System.currentTimeMillis();
		perfInfo.suffixTime += (stop - start);		

		{
			retMe.setID(traceID);
			retMe.debugAutomaton();
		}
		return retMe;
	}
	
	public StrangerAutomaton toUpperCase() {
		debug(traceID + " = toUpperCase(" + this.ID + ")");

		// if top or bottom then do not use the c library as dfa == null
		if (this.isBottom())
			return makeBottom(this.ID);
		if (this.empty)
			return makeEmptyString(this.ID);

		debugToFile("M[" + traceID + "] = toUpperCase(M["+ this.autoTraceID +"]);//"+traceID + " = toUpperCase(" + this.ID + ")");
		perfInfo.numOfPrefix++;
		
		StrangerAutomaton retMe = new StrangerAutomaton(
				StrangerLibrary.INSTANCE.dfaToUpperCase(this.dfa, 
						StrangerLibraryWrapper.NUM_ASCII_TRACKS,
						StrangerLibraryWrapper.indices_main		
						));		

		{
			retMe.setID(traceID);
			retMe.debugAutomaton();
		}
		return retMe;
	}
	
	public StrangerAutomaton toLowerCase() {
		debug(traceID + " = toLowerCase(" + this.ID + ")");

		// if top or bottom then do not use the c library as dfa == null
//		if (this.isBottom())
//			return makeBottom(this.ID);
//		if (this.empty)
//			return makeEmptyString(this.ID);

		debugToFile("M[" + traceID + "] = toLowerCase(M["+ this.autoTraceID +"]);//"+traceID + " = toLowerCase(" + this.ID + ")");
		perfInfo.numOfPrefix++;
		StrangerAutomaton retMe = new StrangerAutomaton(
				StrangerLibrary.INSTANCE.dfaToLowerCase(this.dfa, 
						StrangerLibraryWrapper.NUM_ASCII_TRACKS,
						StrangerLibraryWrapper.indices_main		
						));		

		{
			retMe.setID(traceID);
			retMe.debugAutomaton();
		}
		return retMe;
	}
	
	public StrangerAutomaton trim(){
		return this.trim(' ');
	}
	
	public StrangerAutomaton trim(char c) {
		debug(traceID + " = trim(" + this.ID + ")");

		// if top or bottom then do not use the c library as dfa == null
		if (this.isBottom())
			return makeBottom(this.ID);
		if (this.empty)
			return makeEmptyString(this.ID);

		debugToFile("M[" + traceID + "] = dfaTrim(M["+ this.autoTraceID +"],'"+c+"',NUM_ASCII_TRACKS, indices_main);//"+traceID + " = dfaTrim(" + this.ID + ")");
		perfInfo.numOfPrefix++;
		
		StrangerAutomaton retMe = new StrangerAutomaton(
				StrangerLibrary.INSTANCE.dfaTrim(this.dfa, c,
						StrangerLibraryWrapper.NUM_ASCII_TRACKS,
						StrangerLibraryWrapper.indices_main		
						));		

		{
			retMe.setID(traceID);
			retMe.debugAutomaton();
		}
		return retMe;
	}
	
	public StrangerAutomaton substring(int start, int end){
		StrangerAutomaton returnVal;
		if(start==end){
			return StrangerAutomaton.makeAnyStringL1ToL2(0, 0);
		}
		else if(start==0){
			returnVal=this.suffix(end);
//			returnVal=returnVal.intersect(StrangerAutomaton.makeAnyStringL1ToL2(end, end));
			return returnVal;
		}
		else if(end==0){
			return this.prefix(start);
		}
		StrangerAutomaton temp;
		temp=this.prefix(start);
		int length=end-start;
		returnVal=temp.suffix(length);
//		returnVal=returnVal.intersect(StrangerAutomaton.makeAnyStringL1ToL2(length, length));
		return returnVal;
	}
	
	public StrangerAutomaton replace(StrangerAutomaton patternAuto,
			StrangerAutomaton replaceStr) {
		debug(traceID + " = replace(" + patternAuto.ID + ", " + replaceStr.ID
				+ ", " + this.ID + ")");

		if (patternAuto.isBottom() || this.isBottom()||replaceStr.isBottom())
			throw new StrangerAutomatonException(
			"SNH: In StrangerAutoatmon.reg_replace: either patternAuto or subjectAuto is bottom element (phi) which can not be used in replace.");
		else if (patternAuto.isTop())
			throw new StrangerAutomatonException(
			"SNH: In StrangerAutoatmon.reg_replace: patternAuto is top (indicating that the variable may no longer be of type string) and can not be used in replacement");
		else if (this.isTop())
			throw new StrangerAutomatonException(
			"SNH: In StrangerAutoatmon.reg_replace: subjectAuto is top (indicating that the variable may no longer be of type string) and can not be used in replacement");
	
		debugToFile("M[" + (traceID) + "] = dfa_general_replace_extrabit(M["+ this.autoTraceID +"], M[" + patternAuto.autoTraceID + "], M[" + replaceStr.autoTraceID + "] , NUM_ASCII_TRACKS, indices_main);//"+traceID + " = replace(" + patternAuto.ID + ", " + replaceStr.ID
				+ ", " + this.ID + ")");
		perfInfo.numOfReplace++;
		long start = System.currentTimeMillis();

		StrangerAutomaton retMe = new StrangerAutomaton(
				StrangerLibrary.INSTANCE.dfa_general_replace_extrabit(this.dfa,
						patternAuto.dfa, replaceStr.dfa,
						StrangerLibraryWrapper.NUM_ASCII_TRACKS,
						StrangerLibraryWrapper.indices_main));

		long stop = System.currentTimeMillis();
		perfInfo.replaceTime += (stop - start);
		
		{
			retMe.ID = traceID;
			retMe.debugAutomaton();
		}
		return retMe;
				
	}
	public int getNumStates(){
		StrangerDFA.ByReference structDFA=new StrangerDFA.ByReference(this.dfa);
		return structDFA.ns;
	}
	public int getNumTransitions(){
		StrangerDFA.ByReference structDFA=new StrangerDFA.ByReference(this.dfa);
		int result=StrangerLibrary.INSTANCE.bdd_size(structDFA.bddm);
		return result;
	}
	
	public int [] getFiniteLengths(){
		if(!this.isLengthFinite())
			return new int [0];
		_DFAFiniteLengths.ByReference lengths=new _DFAFiniteLengths.ByReference(
				StrangerLibrary.INSTANCE.dfaGetLengthsFiniteLang(this.dfa, StrangerLibraryWrapper.NUM_ASCII_TRACKS,
						StrangerLibraryWrapper.indices_main));
		if(lengths.size==0)
			return new int[0];
		int [] array=lengths.lengths.getIntArray(0, lengths.size);
		return array;
	}
	
	public StrangerAutomaton minus(StrangerAutomaton a) {
		if (isEmpty() || a == this)
			return StrangerAutomaton.makePhi();
		if (a.isEmpty())
			return clone();

		String s=isSingleton();
		if (s!=null) {
			if (a.checkMembership(s))
				return makePhi();
			else
				return clone();
		}
		return intersect(a.complement());
	}
	
	public String isTrueSingleton(){
		String debugStr = "isSingleton(" + this.ID + ") = ";
		
//		if (this.isTop()|| this.isBottom()){
//			debug(debugStr + "false");
//			return false;
//		}

		debugToFile("isSingleton(M["+ this.autoTraceID +"]);//isSingleton(" + this.ID + ")");
		String result = StrangerLibrary.INSTANCE.isSingleton(this.dfa,
				StrangerLibraryWrapper.NUM_ASCII_TRACKS,
				StrangerLibraryWrapper.indices_main);
		{
			debug( debugStr + result);
		}
		if(result!=null && StrangerAutomaton.makeAnyStringL1ToL2(0, 0).checkInclusion(this)){
			//Sometimes a "singleton" accepts both the empty string and an additional string.
			StrangerAutomaton temp=this.intersect(StrangerAutomaton.makeAnyStringL1ToL2(0, 0).complement());
			debugToFile("isSingleton(M["+ this.autoTraceID +"]);//isSingleton(" + this.ID + ")");
			String newresult = StrangerLibrary.INSTANCE.isSingleton(temp.dfa,
					StrangerLibraryWrapper.NUM_ASCII_TRACKS,
					StrangerLibraryWrapper.indices_main);
			{
				debug( debugStr + newresult);
			}
			if(newresult !=null)
				return null;
		}
		return result;
	}
	
	public StrangerAutomaton equalsIgnoreCase(StrangerAutomaton a, boolean isTrue){
		String concrete=a.isSingleton();
		boolean hasUnicode=false;
		if(concrete!=null){
			StrangerAutomaton result=StrangerAutomaton.makeAnyStringL1ToL2(0, 0);
			for(int i=0; i<concrete.length();i++){
				int c=concrete.codePointAt(i);
				StrangerAutomaton newAuto;
				if((c>=(int)'a'&&c<=(int)'z')||(c>=(int)'A'&&c<=(int)'Z')){
					newAuto=StrangerAutomaton.makeChar(Character.toUpperCase((char)c)).union(StrangerAutomaton.makeChar(Character.toLowerCase((char)c)));
				}
				else if(Character.isLowerCase(c)||Character.isUpperCase(c)){
					newAuto=StrangerAutomaton.makeAnyStringL1ToL2(1, 1);
					hasUnicode=true;
					if(!isTrue){
						return this.clone();
					}
				}
				else
					newAuto=StrangerAutomaton.makeChar((char)c);
				result=result.concatenate(newAuto);
			}
			if(isTrue){
				return this.intersect(result);
			}
			else if(!hasUnicode){
				return this.minus(result);
			}
			else{
				return this;
			}
		}
		else{
			return this;
		}
	}
}
