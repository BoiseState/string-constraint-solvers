package edu.ucsb.cs.www.vlab.stranger;
import java.util.Arrays;
import java.util.List;

import com.sun.jna.*;
import com.sun.jna.ptr.IntByReference;

public interface StrangerLibrary extends Library {
	
	//Fixes a bug where the garbage collector caused invalid memory access.
	StrangerLibrary INSTANCE =(StrangerLibrary) Native.synchronizedLibrary( (StrangerLibrary) Native.loadLibrary("stranger",
			StrangerLibrary.class));
	
	/******************************** bdd_internal.h ***********************************/

	public static class bdd_record extends Structure { // bdd.h
		public static class ByReference extends bdd_record implements
				Structure.ByReference {
		}

		public int[] lri = new int[2];
		public int next;
		public int mark;
		@Override
		protected List getFieldOrder() {
			 return Arrays.asList(new String[] { "lri", "next", "mark"});
		}
	}
	
	/******************************** bdd.h ***********************************/

	public static class cache_record extends Structure { // bdd_internal.h
		public static class ByReference extends cache_record implements
				Structure.ByReference {
		}

		public int p0, q0, res0, p1, q1, res1;
		public int next;
		public int align; /* to make record fill 32 bytes */
		@Override
		protected List getFieldOrder() {
			 return Arrays.asList(new String[] { "p0", "q0", "res0", "p1", "q1", "res1", "next", "align"});
		}
	}
	

	public static class bdd_manager extends Structure {
		public static class ByReference extends bdd_manager implements
				Structure.ByReference {
		}

		/* table */

		public int table_log_size;
		public int table_size; /* = 2^table_log_size */
		public int table_total_size; /* including overflow area */
		public int table_mask; /*
								 * table_log_size - the number of bits in
								 * table_mask is the logarithm of the number of
								 * bins per hash address
								 */
		public int table_overflow_increment; /*
											 * the number of new nodes added
											 * when overflow area is full; also
											 * initial size of overflow area
											 */
		public int table_elements; /*
									 * number of elements inserted in hashed
									 * mode
									 */

		public int table_next; /*
								 * next available position when nodes are
								 * inserted sequentially
								 */
		public int table_overflow; /*
									 * next free node in overflow area when
									 * node_table used in hashed mode
									 */
		public int table_double_trigger; /* when to trigger doubting of the table */
		public bdd_record.ByReference node_table; /*
												 * node_table is the beginning
												 * of array of BDD nodes
												 */

		// DECLARE_SEQUENTIAL_LIST(roots, unsigned) /*results of applys and
		// projects*/
		public IntByReference roots_array;
		public int roots_length;
		public int roots_index;

		/* cache */

		public cache_record.ByReference cache; /*
												 * cache is the beginning of
												 * cache table
												 */
		public int cache_total_size; /* size of hashed area + overflow area */
		public int cache_size; /*
								 * size of hashed area (which is initialized to
								 * unused)
								 */
		public int cache_mask; /*
								 * table_log_size - the number of bits in
								 * cache_mask is the logarithm of the number of
								 * bins per hash address
								 */
		public int cache_overflow_increment; /*
											 * initial size of overflow area and
											 * increment when full
											 */
		public int cache_overflow; /*
									 * points to next available position in
									 * overflow area
									 */
		public int cache_erase_on_doubling; /*
											 * if not set, cache is rehashed
											 * when table is doubled in hashed
											 * access mode; default is true
											 */

		/* statistics */

		public int number_double;
		public int number_cache_collissions;
		public int number_cache_link_followed;
		public int number_node_collissions;
		public int number_node_link_followed;
		public int number_lookup_cache;
		public int number_insert_cache;
		public int apply1_steps;
		public int call_steps;
		public int apply2_steps;
		@Override
		protected List getFieldOrder() {
			
			 return Arrays.asList(new String[] { "table_log_size", "table_size", "table_total_size", "table_mask", "table_overflow_increment"
					 , "table_elements", "table_next", "table_overflow", "table_double_trigger", "node_table"
					,"roots_array", "roots_length", "roots_index", "cache", "cache_total_size", "cache_size", "cache_mask"
					, "cache_overflow_increment", "cache_overflow", "cache_erase_on_doubling"
					, "number_double", "number_cache_collissions", "number_cache_link_followed"
					, "number_node_collissions", "number_node_link_followed", "number_lookup_cache", "number_insert_cache", "apply1_steps", "call_steps", "apply2_steps"});
		}
	}

	
	/******************************** dfa.h ***********************************/
	
	//typedef struct { ...bdd_manager } DFA;
	public static class StrangerDFA extends Structure {
		public StrangerDFA(Pointer p) {
			super(p);
		}
		public static class ByReference extends StrangerDFA implements
			Structure.ByReference {
			public ByReference(Pointer p){
				super(p);
				read();
			}
		}
		//bdd_manager *bddm; /* manager of BDD nodes */
		public bdd_manager.ByReference bddm;
		//int ns;            /* number of states */
		public int ns;
		//  bdd_ptr *q;        /* transition array */
		public IntByReference q;
		//int s;             /* start state */
		public int s;
		//int *f;            /* state statuses; -1:reject, 0:don't care, +1:accept */
		public IntByReference f;
		@Override
		protected List getFieldOrder() {
			 return Arrays.asList(new String[] { "bddm", "ns", "q", "s", "f" });
		}
	}
	
//	typedef enum {
//	  dfaIMPL = 11,
//	  dfaBIIMPL = 9,
//	  dfaAND = 8,
//	  dfaOR = 14
//	} dfaProductType;
	public static int dfaIMPL = 11;
	public static int dfaBIIMPL = 9;
	public static int dfaAND = 8;
	public static int dfaOR = 14;
	
	//DFA *dfaCopy(DFA *a);
	StrangerDFA dfaCopy(StrangerDFA a);	
	Pointer dfaCopy(Pointer a);	

	
	//DFA *dfaProduct(DFA *a1, DFA *a2, dfaProductType mode);
	StrangerDFA dfaProduct(StrangerDFA a1, StrangerDFA a2, int mode);
	Pointer dfaProduct(Pointer a1, Pointer a2, int mode);

	
	//DFA *dfaMinimize(DFA *a);
	StrangerDFA dfaMinimize(StrangerDFA a);
	Pointer dfaMinimize(Pointer a);

	
	//void dfaSetup(int s, int len, int indices[]);
	void dfaSetup(int s, int len, IntByReference indices);
	
	
	//void dfaAllocExceptions(int n);
	void dfaAllocExceptions(int n);
	
	//void dfaStoreException(int s, char *path);
	void dfaStoreException(int s, String path);
	
	//void dfaStoreState(int s);
	void dfaStoreState(int s);
	
	//DFA *dfaBuild(char statuses[]);
//	StrangerDFA dfaBuild(String statuses);
	Pointer dfaBuild(String statuses);

	
	//void dfaPrintVerbose(DFA *a);
	void dfaPrintVerbose(StrangerDFA a);
	void dfaPrintVerbose(Pointer a);

	
	//void dfaPrintVitals(DFA *a);
	void dfaPrintVitals(StrangerDFA a);
	void dfaPrintVitals(Pointer a);

	
	//void dfaPrintGraphviz(DFA *a, int num, unsigned indices[]);
	void dfaPrintGraphviz(StrangerDFA a, int num, IntByReference indices);
	void dfaPrintGraphviz(Pointer a, int num, IntByReference indices);

	
	//void dfaFree(DFA *a);
	void dfaFree(StrangerDFA a);
	void dfaFree(Pointer a);

	
	
	/******************************** mem.h ***********************************/
	
	//unsigned int mem_allocated();
	com.sun.jna.NativeLong mem_allocated();

	/******************************** stranger.h ***********************************/
	
	public static class semilinear_type extends Structure{
		int R;
		int C;
		//int* r;
		IntByReference r;
		//int *c;
		IntByReference c;
		int nc;
		int nr;
		@Override
		protected List getFieldOrder() {
			 return Arrays.asList(new String[] { "R", "C", "r", "c", "nc", "nr"});
		}
	}
	
	
	public static class _DFAFiniteLengths extends Structure{
		public _DFAFiniteLengths(Pointer p) {
			super(p);
		}
		public static class ByReference extends _DFAFiniteLengths implements
		Structure.ByReference {
			public ByReference(Pointer p){
				super(p);
				read();
			}
		}
		public Pointer lengths;
		public int size;
		protected List getFieldOrder() {
			 return Arrays.asList(new String[] { "lengths", "size"});
		}
	}
    
	public static class transition extends Structure{
		public static class ByReference extends transition implements
		Structure.ByReference {
		}
		public int source;
		public int dest;
		public byte first;
		public byte last;
		@Override
		protected List getFieldOrder() {
			 return Arrays.asList(new String[] { "source", "dest", "first", "last"});
		}
	}
	
	public static class P_transition extends Structure {
	    public static class ByReference extends P_transition implements Structure.ByReference { }
	    public transition.ByReference p_transition;
	    public P_transition() {p_transition = new transition.ByReference();}
	    public transition.ByReference[] toArray(int size) {
	        return (transition.ByReference[]) p_transition.toArray(size);
	    }
		@Override
		protected List getFieldOrder() {
			 return Arrays.asList(new String[] { "p_transition"});
		}
	}
	
	
	/** 
	 * @param M: automaton to generate example for.
	 * @param var: StrangerLibraryWrapper.NUM_ASCII_TRACKS
	 * @param indices: StrangerLibraryWrapper.indices_main
	 * @return An example string s where s elementOf L(M). s could be null
	 * in case there is no solution or the solution is the empty string.
	 */
	//char *dfaGenerateExample(DFA* M, int var, unsigned indices[])
//	String dfaGenerateExample(StrangerDFA M, int var, IntByReference indices);
	String dfaGenerateExample(Pointer M, int var, IntByReference indices);

	
	//DFA *dfaGetTrack(DFA *M, int i_track, int m, int var, int* indices)
	StrangerDFA dfaGetTrack(StrangerDFA M, int i_track, int m, int var, IntByReference indices);
	Pointer dfaGetTrack(Pointer M, int i_track, int m, int var, IntByReference indices);

	//DFA *dfaRemovePreLambda(DFA *M, char* lambda, int var, int *oldindices)
	StrangerDFA dfaRemovePreLambda(StrangerDFA M, String lambda, int var, IntByReference oldindices);
	Pointer dfaRemovePreLambda(Pointer M, String lambda, int var, IntByReference oldindices);

	//char *getLambda(int var)
	String getLambda(int var);
	/** generate a multitrack automaton for an input 
	 * For each input we call this function to initialize the input automaton. 
	 * For one dependency graph:
	 * 		- we need to be consistent with the track number (i_track) (always give same track number to same input) 
	 * 		- m, var , indices are always the same for the same sink
	 * This function is only used to build multitrack automaton for input. For constants use mdfaSignatureConstant
	 * @param i_track: track number (input index or order in tracks of automaton) starting from 0. 
	 * Track i is associated with input i. track m-1 is the output track
	 * @param m: total number of tracks = number of input tracks + output track (number of inputs + 1)
	 * @param var: the original ones used to single track
	 * @param indices: the original ones used to single track
	 * @return signature multitrack automaton for given input
	 */
	//DFA *mdfaSignatureInput(int i_track, int m, int var, int* indices)
//	StrangerDFA mdfaSignatureInput(int i_track, int m, int var, IntByReference indices);
	Pointer mdfaSignatureInput(int i_track, int m, int var, IntByReference indices);

	
	
	/**
	 * Extend M to m track DFA where the m-1 th track is equal to M, and the rest is set to \lambda
	 * @param M: represent the automaton for the constant which is a regular single track automaton
	 * @param m: m-1 is the track number of the constant string
	 * @param var: the original ones used to single track
	 * @param indices: the original ones used to single track
	 * @return
	 */
	//DFA *mdfaSignatureConstant( DFA* M, int m, int var, int* indices)
	StrangerDFA mdfaSignatureConstant( StrangerDFA M, int m, int var, IntByReference indices);
	Pointer mdfaSignatureConstant( Pointer M, int m, int var, IntByReference indices);

	
	/**
	 * Allocates indices for multitrack signature automaton.
	 * The result should be used with concatenation when applied to multitrack 
	 * automaton instead of the original indices number.
	 * @param m: total number of tracks = number of input tracks + output track (number of inputs + 1)
	 * @param length: the same as var parameter
	 * @return
	 */
	//int* allocateMultipleAscIIIndex(int m, int length)
	IntByReference allocateMultipleAscIIIndex(int m, int length);
	
	
	/**
	 * We here extend the single track automaton M into multitrack one (usually used with attack pattern). 
	 * Do not use for constants automaton (automaton that represent a singlton) instead use mdfaSignatureConstant
	 * @param M: the single track automaton to be extended into multitrack automaton
	 * @param m: total number of tracks = number of input tracks + output track (number of inputs + 1)
	 * @param i_track: the track that should have the original value of M. 
	 * For attack pattern we use i_track = m-1 to intersect the output track with the attack pattern
	 * @param var: the original ones used to single track
	 * @param indices: the original ones used to single track
	 * @return
	 */
	//DFA *mdfaOneToManyTrackNoLambda( DFA* M, int m, int i_track, int var, int* indices)
	StrangerDFA mdfaOneToManyTrackNoLambda( StrangerDFA M, int m, int i_track, int var, IntByReference indices);
	Pointer mdfaOneToManyTrackNoLambda( Pointer M, int m, int i_track, int var, IntByReference indices);

	
	/**
	 * Removes the last track (output track) from automaton. Should be applied on the result of 
	 * intersection of attack pattern and analysis result automaton before printing with graphviz
	 * @param M: M is the multitrack dfa (result of intersection with attack pattern)
	 * @param m: m is number of tracks
	 * @param var: the original ones used to single track
	 * @param indices: the original ones used to single track
	 * @return
	 */
	//DFA *dfaRemoveLastTrack(DFA *M, int m, int var, int* indices)
	StrangerDFA dfaRemoveLastTrack(StrangerDFA M, int m, int var, IntByReference indices);
	Pointer dfaRemoveLastTrack(Pointer M, int m, int var, IntByReference indices);

	
	/** generates a DFA that accepts nothin. i.e. phi.
	 * @param var and indices is the depth of the BDD (number of variables in the BDD) and ordering of them */
	Pointer dfaASCIINonString(int var, IntByReference indices);
	
	/** generates a DFA that accepts arbitrary strings including empty string (.*)
	 * Note that two reserved words are used internally and will not be accepted 
	 * by a dfa generated by this function.
	 * @param var and indices is the depth of the BDD (number of variables in the BDD) and ordering of them */
	//DFA *dfaAllStringASCIIExceptReserveWords(int var, int *indices);
//	StrangerDFA dfaAllStringASCIIExceptReserveWords(int var, IntByReference indices);
	Pointer dfaAllStringASCIIExceptReserveWords(int var, IntByReference indices);


	/** generates a DFA that accepts empty string - var and indices is the depth of the BDD (number of variables in the BDD) and ordering of them */
	//DFA *dfaASCIIOnlyNullString(int var, int *indices);
//	StrangerDFA dfaASCIIOnlyNullString(int var, IntByReference indices);
	Pointer dfaASCIIOnlyNullString(int var, IntByReference indices);


	/** Constructs a DFA that accepts exactly the given str String - var and indices is the depth of the BDD (number of variables in the BDD) and ordering of them */
	//DFA *dfa_construct_string(char *reg, int var, int *indices);
//	StrangerDFA dfa_construct_string(String str, int var, IntByReference indices); // take a look at char *
	Pointer dfa_construct_string(String str, int var, IntByReference indices); // take a look at char *

    /**
     * outputs a DFA M that accepts the set of string values in array set
     * outputs DFA M where L(M) = {s: s elementOf set}
     */
	Pointer dfa_construct_set_of_strings(String []set, int size, int var, IntByReference indices);

//	StrangerDFA dfa_construct_encoded_string(String s, int numAsciiTracks,
//			IntByReference indices_main);
	Pointer dfa_construct_encoded_string(String s, int numAsciiTracks,
			IntByReference indices_main);
	
	/** Constructs a DFA that accepts the Kleene-+ closure of the given str String (str+) - var and indices is the depth of the BDD (number of variables in the BDD) and ordering of them */
	//DFA *dfa_construct_string_closure(char *reg, int var, int *indices); // take a look at char *
//	StrangerDFA dfa_construct_string_closure(String reg, int var, IntByReference indices);
	Pointer dfa_construct_string_closure(String reg, int var, IntByReference indices);

	
	//Construct DFA accepts one character within [a-b]
	//DFA *dfa_construct_range(char a, char b, int var, int *indices);
//	StrangerDFA dfa_construct_range(char a, char b, int var, IntByReference indices);
	Pointer dfa_construct_range(char a, char b, int var, IntByReference indices);

	
	//DFA *dfa_construct_from_automaton(int n_states, char* accept_states, int n_trans, transition* transitions, int var, unsigned *indices);
//	StrangerDFA dfa_construct_from_automaton(int n_states,  int n_trans, transition.ByReference  transitions, String accept_states, int var, IntByReference indices);
	Pointer dfa_construct_from_automaton(int n_states,  int n_trans, transition.ByReference  transitions, String accept_states, int var, IntByReference indices);


	/** constructs a DFA that accepts the union of the languages of the two DFAs M1 and M2 
	 * It checks if one of the two is empty_string and uses the appropriate union method. 
	 */
	//DFA *dfa_union_with_emptycheck(DFA* M1, DFA* M2, int var, int* indices);
	StrangerDFA dfa_union_with_emptycheck(StrangerDFA M1, StrangerDFA M2, int var, IntByReference indices);
	Pointer dfa_union_with_emptycheck(Pointer M1, Pointer M2, int var, IntByReference indices);


	/** 
	 * do not use it unless you want to test union and ignore empty string
	 * @param M1
	 * @param M2
	 * @return
	 */
	//DFA *dfa_union(DFA *M1, DFA *M2);
	StrangerDFA dfa_union(StrangerDFA M1, StrangerDFA M2);
	Pointer dfa_union(Pointer M1, Pointer M2);

	
	/** constructs a DFA that accepts the union of the languages of the two DFAs M1 and M2 
	 * Deprecated. use dfa_unoin_with_emptycheck instead.
	 **/
	//DFA *dfa_union(DFA *M1, DFA *M2);
	//StrangerDFA dfa_union(StrangerDFA M1, StrangerDFA M2);
	
	/** Given M, creates a dfa accepting (L(M) union {empty string}) 
	 * Deprecated. use dfa_unoin_with_emptycheck instead.
	 * */
	//DFA *dfa_union_add_empty_M(DFA *M, int var, int *indices);
	//StrangerDFA dfa_union_add_empty_M(StrangerDFA M, int var, IntByReference indices);

	/** constructs a DFA that accepts the intersection of the languages of the two DFAs M1 and M2 */
	//DFA *dfa_intersect(DFA *M1, DFA *M2);
	StrangerDFA dfa_intersect(StrangerDFA M1, StrangerDFA M2);
	Pointer dfa_intersect(Pointer M1, Pointer M2);

	
	/** constructs a DFA that accepts the complement of the language of the DFA M1 - var and indices is the depth of the BDD (number of variables in the BDD) and ordering of them */
	//DFA *dfa_negate(DFA *M1, int var, int *indices);
	StrangerDFA dfa_negate(StrangerDFA M1, int var, IntByReference indices);
	Pointer dfa_negate(Pointer M1, int var, IntByReference indices);

	
	/** constructs a DFA that accepts the concatenation of the languages of the two DFAs M1 and M2 - var and indices is the depth of the BDD (number of variables in the BDD) and ordering of them */
	//DFA *dfa_concat_extrabit(DFA *M1, DFA *M2, int var, int *indices);
	StrangerDFA dfa_concat_extrabit(StrangerDFA M1, StrangerDFA M2, int var, IntByReference indices);
	Pointer dfa_concat_extrabit(Pointer M1, Pointer M2, int var, IntByReference indices);

	
	StrangerDFA dfa_concat(StrangerDFA M1, StrangerDFA M2, int var, IntByReference indices);
	Pointer dfa_concat(Pointer M1, Pointer M2, int var, IntByReference indices);

	
	
	// DFA *dfa_pre_concat(DFA* ML, DFA* MR, int pos, int var, int* indices)
	StrangerDFA dfa_pre_concat(StrangerDFA ML, StrangerDFA MR, int pos, int var, IntByReference indices);
	Pointer dfa_pre_concat(Pointer ML, Pointer MR, int pos, int var, IntByReference indices);

	
	// DFA *dfa_pre_concat_const(DFA* ML, char* const, int pos, int var, int* indices)
	StrangerDFA dfa_pre_concat_const(StrangerDFA ML, String str, int pos, int var, IntByReference indices);
	Pointer dfa_pre_concat_const(Pointer ML, String str, int pos, int var, IntByReference indices);

	
	/** constructs a DFA that accepts the result of replacing every occurrence of a string of M2 language in M1 language with str - 
	 * var and indices is the depth of the BDD (number of variables in the BDD) and ordering of them
	 * @param M1: target auto (of type StrangerDFA), replace substrings in L(M1)
	 * @param M2: search auto (of type StrangerDFA), replace substrings which match elements in L(M2)
	 * @param str: replace string (of type String) , replace with this string
	 */
	//DFA *dfa_replace_extrabit(DFA *M1, DFA *M2, char *str, int var, int *indices);
	StrangerDFA dfa_replace_extrabit(StrangerDFA M1, StrangerDFA M2, String str, int var, IntByReference indices);
	Pointer dfa_replace_extrabit(Pointer M1, Pointer M2, String str, int var, IntByReference indices);

	
	//DFA *dfa_general_replace_extrabit(DFA *M1, DFA *M2, DFA *M3, int var, int *indices);
	StrangerDFA dfa_general_replace_extrabit(StrangerDFA M1, StrangerDFA M2, StrangerDFA M3, int var, IntByReference indices);
	Pointer dfa_general_replace_extrabit(Pointer M1, Pointer M2, Pointer M3, int var, IntByReference indices);

	
	// DFA *dfa_pre_replace(DFA* M1, DFA* M2, DFA* M3, int var, int* indices)
	StrangerDFA dfa_pre_replace(StrangerDFA M1, StrangerDFA M2, StrangerDFA M3, int var, IntByReference indices);
	Pointer dfa_pre_replace(Pointer M1, Pointer M2, Pointer M3, int var, IntByReference indices);

	
	// DFA *dfa_pre_replace(DFA* M1, DFA* M2, char* str, int var, int* indices)
	StrangerDFA dfa_pre_replace_str(StrangerDFA M1, StrangerDFA M2, String str, int var, IntByReference indices);
	Pointer dfa_pre_replace_str(Pointer M1, Pointer M2, String str, int var, IntByReference indices);

	
	//DFA *dfa_closure_extrabit(DFA *M1,int var,int *indices);
	StrangerDFA dfa_closure_extrabit(StrangerDFA M1,int var,IntByReference indices);
	Pointer dfa_closure_extrabit(Pointer M1,int var,IntByReference indices);

	
	//DFA *dfaWiden(DFA *a, DFA *d);
	StrangerDFA dfaWiden(StrangerDFA a, StrangerDFA d);
	Pointer dfaWiden(Pointer a, Pointer d);

	
	void setCoarseWiden();
	
	void setPreciseWiden();


	/** Given M, output a DFA accepting S*.w.S* where w \in M */
	//DFA *dfa_star_M_star(DFA *M, int var, int *indices);
	StrangerDFA dfa_star_M_star(StrangerDFA M, int var, IntByReference indices);
	Pointer dfa_star_M_star(Pointer M, int var, IntByReference indices);

	
	// A DFA that accepts only one arbitrary character
	//DFA *dfaDot(int var, int *indices);
//	StrangerDFA dfaDot(int var, IntByReference indices);
	Pointer dfaDot(int var, IntByReference indices);

	
	/** A DFA that accepts only emty or one arbitrary character */
	//DFA *dfaQuestionMark(int var, int *indices);
//	StrangerDFA dfaQuestionMark(int var, IntByReference indices);
	Pointer dfaQuestionMark(int var, IntByReference indices);

	
	/** A DFA that accepts everything within the length from c1 to c2
	  * c2 = -1, indicates unbounded upperbound
	  * c1 = -1, indicates unbounded lowerbound
	  */
	//DFA *dfaSigmaC1toC2(int c1, int c2, int var, int* indices);
//	StrangerDFA dfaSigmaC1toC2(int c1, int c2, int var, IntByReference indices);
	Pointer dfaSigmaC1toC2(int c1, int c2, int var, IntByReference indices);

	
	/** Output M' so that L(M')={w| w'\in \Sigma*, ww' \in L(M), c_1 <= |w|<=c_2 } */
	//DFA *dfa_Prefix(DFA *M, int c1, int c2, int var, int* indices);
	StrangerDFA dfa_Prefix(StrangerDFA M, int c1, int c2, int var, IntByReference indices);
	Pointer dfa_Prefix(Pointer M, int c1, int c2, int var, IntByReference indices);

	
	//DFA *dfa_Suffix(DFA *M, int c1, int c2, int var, int *indices);
	StrangerDFA dfa_Suffix(StrangerDFA M, int c1, int c2, int var, IntByReference indices);
	Pointer dfa_Suffix(Pointer M, int c1, int c2, int var, IntByReference indices);

	
	//Checking function
	//int check_emptiness(DFA *M1, int var, int *indices);
	int check_emptiness(StrangerDFA M1, int var, IntByReference indices);
	int check_emptiness(Pointer M1, int var, IntByReference indices);

	
	/** checks wether L(M1) = L(M2) - var and indices is the depth of the BDD (number of variables in the BDD) and ordering of them */
	//int check_equivalence(DFA *M1, DFA *M2, int var, int *indices);
	int check_equivalence(StrangerDFA M1, StrangerDFA M2, int var, IntByReference indices);
	int check_equivalence(Pointer M1, Pointer M2, int var, IntByReference indices);

	
	//int check_intersection(DFA *M1,DFA *M2,int var,int *indices);
	int check_intersection(StrangerDFA M1, StrangerDFA M2, int var, IntByReference indices);
	int check_intersection(Pointer M1, Pointer M2, int var, IntByReference indices);

	
	//int check_inclusion(DFA *M1,DFA *M2,int var,int *indices);
	int check_inclusion(StrangerDFA M1, StrangerDFA M2, int var, IntByReference indices);
	int check_inclusion(Pointer M1, Pointer M2, int var, IntByReference indices);

    /**
    if L(M) is a singleton set, it will return the string element
    in this set otherwise it will return NULL.
    NULL means not singleton (either empty language or accepts more
    than one string
    */
	String isSingleton(StrangerDFA M, int var, IntByReference indices);
	String isSingleton(Pointer M, int var, IntByReference indices);
	
	int checkMembership(StrangerDFA M, String string, int var, IntByReference indices);
	int checkMembership(Pointer M, String string, int var, IntByReference indices);
	
    /**
     * returns true (1) if {|w| < n: w elementOf L(M) && n elementOf Integers}
     * In other words length of all strings in the language is bounded by a value n
     */
	int isLengthFiniteTarjan(StrangerDFA M, int var, IntByReference indices);
	int isLengthFiniteTarjan(Pointer M, int var, IntByReference indices);
	
	//int checkEmptyString(DFA *M);
	int checkEmptyString(StrangerDFA M);
	int checkEmptyString(Pointer M);
	
	StrangerDFA dfaRemoveSpace(StrangerDFA M, int var, IntByReference indices);
	Pointer dfaRemoveSpace(Pointer M, int var, IntByReference indices);
	
	int dfaPrintBDD(StrangerDFA a, String filename, int var);
	int dfaPrintBDD(Pointer a, String filename, int var);
	
    /**
     * check if dfa accepts only empty string
     */	
	int checkOnlyEmptyString(StrangerDFA M, int var, IntByReference indices);
	int checkOnlyEmptyString(Pointer M, int var, IntByReference indices);

	StrangerDFA dfaToUpperCase(StrangerDFA M, int var, IntByReference indices);
	Pointer dfaToUpperCase(Pointer M, int var, IntByReference indices);
	
	StrangerDFA dfaToLowerCase(StrangerDFA M, int var, IntByReference indices);
	Pointer dfaToLowerCase(Pointer M, int var, IntByReference indices);
	
	StrangerDFA dfaTrim(StrangerDFA M, char c, int var, IntByReference indices);
	Pointer dfaTrim(Pointer M, char c,int var, IntByReference indices);
	
	int bdd_size(bdd_manager.ByReference bddm);
	int bdd_size(Pointer bddm);
	
	//int* allocateAscIIIndexWithExtraBit(int length);
	IntByReference allocateAscIIIndexWithExtraBit(int length);
	
	//void flush_output();
	void flush_output();
	
//	_DFAFiniteLengths dfaGetLengthsFiniteLang(StrangerDFA M, int var, IntByReference indices);
	Pointer dfaGetLengthsFiniteLang(Pointer M, int var, IntByReference indices);

	
	//int main_test (int argc, char *argv[]);
	int main_test(int argc, String[] argv);
	
	/******************************** stranger_lib.c ***********************************/
	//char *bintostr(unsigned long n, int k)
	String bintostr(NativeLong n, int k);
	
	//char *getSharp0(int k)
	String getSharp0(int k);
	
	//char *getSharp1(int k) 
	String getSharp1(int k);

	
	
	

	
}
