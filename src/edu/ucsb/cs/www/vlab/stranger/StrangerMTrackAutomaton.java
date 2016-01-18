package edu.ucsb.cs.www.vlab.stranger;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

import edu.ucsb.cs.www.vlab.stranger.StrangerLibrary.StrangerDFA;


public class StrangerMTrackAutomaton extends StrangerAutomaton {
	
	public static IntByReference mindices_main;
	public static int m_NUM_ASCII_TRACKS; 
	public static int numOfInputs;
	
	/**
	 * Initialized with number of input variables in the program (used for multitrack number of tracks).
	 * @param numOfInputs
	 */
	public static void initializeIndices(int numOfInputs){
		debugToFile("int* mindices =allocateMultipleAscIIIndex("+(numOfInputs+1)+", NUM_ASCII_TRACKS);\nint mvar = "+(numOfInputs+1)+"*NUM_ASCII_TRACKS;\n");
		StrangerMTrackAutomaton.numOfInputs = numOfInputs;
		mindices_main = StrangerLibrary.INSTANCE.allocateMultipleAscIIIndex(numOfInputs+1, StrangerLibraryWrapper.NUM_ASCII_TRACKS);
		m_NUM_ASCII_TRACKS = (numOfInputs+1) * StrangerLibraryWrapper.NUM_ASCII_TRACKS;
	}

	public StrangerMTrackAutomaton(Pointer dfa) {
		super(dfa);
	}
	
	public StrangerAutomaton getTrack(int trackNum, int num_of_tracks, int id){
		debugToFile("M[" + traceID + "] = dfaGetTrack(M["+this.autoTraceID+"], "+trackNum+", "+num_of_tracks+",  NUM_ASCII_TRACKS, indices_main);");//" + id + " = clone(" + this.ID + ")");
		int tempTraceID = traceID;traceID++;
		debugToFile("char lambda = (char) 0xff; char* lambdaP = &lambda;");
		debugToFile("M[" + traceID + "] = dfaRemovePreLambda(M["+tempTraceID+"], /*getLambda(NUM_ASCII_TRACKS)*/0xff, NUM_ASCII_TRACKS, indices_main);");//" + id + " = clone(" + this.ID + ")");
		Pointer dfa = StrangerLibrary.INSTANCE.dfaGetTrack(this.dfa, trackNum, num_of_tracks, StrangerLibraryWrapper.NUM_ASCII_TRACKS, StrangerLibraryWrapper.indices_main);
		StrangerAutomaton retMe = new StrangerAutomaton(StrangerLibrary.INSTANCE.dfaRemovePreLambda(dfa, /*StrangerLibrary.INSTANCE.getLambda(StrangerLibraryWrapper.NUM_ASCII_TRACKS)*/Character.toString((char) 0xff), StrangerLibraryWrapper.NUM_ASCII_TRACKS, StrangerLibraryWrapper.indices_main));
		return retMe;
	}
	
	
	
	/**
	 * creates an automaton that accepts nothing, not even empty string id : id
	 * of node associated with this auto; used for debugging purposes only
	 * */
	public static StrangerMTrackAutomaton makePhi(int id) {
		debug(id + " = makePhi");

		StrangerMTrackAutomaton retMe = new StrangerMTrackAutomaton(null);
		retMe.str = null;
		{
			retMe.setID(id);
		}
		return retMe;
	}
	
	
	public StrangerMTrackAutomaton clone(int id) {
		debug(id + " = clone(" + this.ID + ")");
		if (dfa == null) {
			return makePhi(id);
		} else {
			
			debugToFile("M[" + traceID + "] = dfaCopy(M["+this.autoTraceID+"]);//" + id + " = clone(" + this.ID + ")");
			
			StrangerMTrackAutomaton retMe = new StrangerMTrackAutomaton(
					StrangerLibrary.INSTANCE.dfaCopy(this.dfa));

			retMe.setStr(this.getStr());
			{

				retMe.setID(id);
				retMe.debugAutomaton();
			}
			return retMe;
		}
	}

	
	
	/**
	 * 
	 * @param auto
	 * @param id
	 *            : id of node associated with this auto; used for debugging
	 *            purposes only
	 * @return
	 */
	public StrangerMTrackAutomaton union(StrangerAutomaton auto, int id) {
		debug(id + " = union(" + this.ID + ", " + auto.ID + ")");
		
		// if one is phi then result is a clone of the other as union always
		// returns a new automaton
		if (this.dfa == null)
			return (StrangerMTrackAutomaton) auto.clone(id);
		else if (auto.dfa == null)
			return this.clone(id);
		
		//debugToFile("M[" + traceID + "] = dfa_union_with_emptycheck(M["+ this.autoTraceID +"], M["+ auto.autoTraceID +"], NUM_ASCII_TRACKS, indices_main);//"+id + " = union(" + this.ID + ", " + auto.ID + ")");
		debugToFile("M[" + traceID + "] = dfa_union(M["+ this.autoTraceID +"], M["+ auto.autoTraceID +"]);//"+id + " = union(" + this.ID + ", " + auto.ID + ")");
		perfInfo.numOfUnion++;
		long start = System.currentTimeMillis();
		
//		StrangerAutomaton retMe = new StrangerAutomaton(
//				StrangerLibrary.INSTANCE.dfa_union_with_emptycheck(this.dfa,
//						auto.dfa, StrangerLibraryWrapper.NUM_ASCII_TRACKS,
//						StrangerLibraryWrapper.indices_main));
		StrangerMTrackAutomaton retMe = new StrangerMTrackAutomaton(
				StrangerLibrary.INSTANCE.dfa_union(this.dfa,auto.dfa));
		
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
	 * Concatenates current automaton with auto. New automaton will be
	 * this+auto. If both automatons' strings are not null, they will be
	 * concatenated too otherwise set null. id : id of node associated with this
	 * auto; used for debugging purposes only
	 * 
	 * @param auto
	 * @return
	 */
	public StrangerMTrackAutomaton concatenate(StrangerAutomaton auto, int id) {
		debug(id + " = concatenate(" + this.ID + ", " + auto.ID + ")");
		
		// if one is phi then result is phi
		if (this.dfa == null || auto.dfa == null)
			return makePhi(id);
		if (this.str != null && this.str.isEmpty()){
			StrangerMTrackAutomaton retMe =  (StrangerMTrackAutomaton) auto.clone(id);
			retMe.ID = id;
			return retMe;
		}
		else if (auto.str != null && auto.str.isEmpty()){
			StrangerMTrackAutomaton retMe =  this.clone(id);
			retMe.ID = id;
			return retMe;
		}
		
		debugToFile("M[" + traceID + "] = dfa_concat(M["+ this.autoTraceID +"], M["+ auto.autoTraceID +"], mvar, mindices);//"+id + " = concatenate(" + this.ID + ", " + auto.ID + ")");
		perfInfo.numOfConcat++;
		long start = System.currentTimeMillis();
		
		
		// dfa_concat_extrabit returns new dfa structure in memory so no need to
		// worry about the two dfas of this and auto
		StrangerMTrackAutomaton retMe = new StrangerMTrackAutomaton(
				StrangerLibrary.INSTANCE.dfa_concat(this.dfa,
						auto.dfa, m_NUM_ASCII_TRACKS,
						mindices_main));

		long finish = System.currentTimeMillis();
		perfInfo.concatTime += (finish - start);
		
		if (this.str != null && auto.getStr() != null)
			retMe.str = this.str + auto.getStr();
		else
			retMe.str = null;

		{
			retMe.setID(id);
			retMe.debugAutomaton();
		}
		return retMe;
	}
	
	
	/**
	 * id : id of node associated with this auto; used for debugging purposes
	 * only
	 */
	public StrangerMTrackAutomaton intersect(StrangerAutomaton auto, int id) {
		debug(id + " = intersect(" + this.ID + ", " + auto.ID + ")");

		// if one is phi then result is phi
		if (this.dfa == null || auto.dfa == null)
			return makePhi(id);

		debugToFile("M[" + traceID + "] = dfa_intersect(M["+ this.autoTraceID +"], M["+ auto.autoTraceID +"]);//"+id + " = intersect(" + this.ID + ", " + auto.ID + ")");
		perfInfo.numOfIntersect++;
		long start = System.currentTimeMillis();
		
		StrangerMTrackAutomaton retMe = new StrangerMTrackAutomaton(
				StrangerLibrary.INSTANCE.dfa_intersect(this.dfa, auto.dfa));
		
		long stop = System.currentTimeMillis();
		perfInfo.intersectTime += (stop - start);		

		{
			retMe.setID(id);
			retMe.debugAutomaton();
		}
		return retMe;
	}
	
	public static StrangerMTrackAutomaton makeInputMTAuto(int trackNum, int id){
		debugToFile("M[" + traceID + "] = mdfaSignatureInput(" + trackNum + ", " + (numOfInputs + 1) + ",  NUM_ASCII_TRACKS, indices_main);//" + id + " = makeInputMTAuto("+trackNum+","+id+")");
		
		StrangerMTrackAutomaton retMe = new StrangerMTrackAutomaton(StrangerLibrary.INSTANCE.mdfaSignatureInput(trackNum, numOfInputs + 1, StrangerLibraryWrapper.NUM_ASCII_TRACKS, StrangerLibraryWrapper.indices_main));
		retMe.setStr(null);
		
		{
			retMe.setID(id);
			retMe.debugAutomaton();
		}
		
		return retMe;
	}
	
	public static StrangerMTrackAutomaton makeConstMTAuto(StrangerAutomaton auto, int id){
		debugToFile("M[" + traceID + "] = mdfaSignatureConstant(M[" + auto.autoTraceID + "], " + (numOfInputs + 1) + ",  NUM_ASCII_TRACKS, indices_main);//" + id + " = makeConstMTAuto("+auto.autoTraceID+","+id+")");
		
		StrangerMTrackAutomaton retMe = new StrangerMTrackAutomaton(StrangerLibrary.INSTANCE.mdfaSignatureConstant(auto.dfa, numOfInputs + 1, StrangerLibraryWrapper.NUM_ASCII_TRACKS, StrangerLibraryWrapper.indices_main));
		retMe.setStr(auto.str);
		
		{
			retMe.setID(id);
			retMe.debugAutomaton();
		}
		
		return retMe;
	}
	
	
	public static StrangerMTrackAutomaton singleToMTAuto(StrangerAutomaton auto, int id){
		debugToFile("M[" + traceID + "] = mdfaOneToManyTrackNoLambda(M[" + auto.autoTraceID + "], "+ (numOfInputs + 1) +", "+ numOfInputs + ",  NUM_ASCII_TRACKS, indices_main);//" + id + " = singleToMTAuto("+auto.autoTraceID+","+id+")");
		
		StrangerMTrackAutomaton retMe = new StrangerMTrackAutomaton(StrangerLibrary.INSTANCE.mdfaOneToManyTrackNoLambda(auto.dfa, numOfInputs + 1, numOfInputs, StrangerLibraryWrapper.NUM_ASCII_TRACKS, StrangerLibraryWrapper.indices_main));
		retMe.setStr(auto.str);
		
		{
			retMe.setID(id);
			retMe.debugAutomaton();
		}
		
		return retMe;
	}
	
	
	public StrangerMTrackAutomaton removeLastTrack(int id){
		debugToFile("M[" + traceID + "] = dfaRemoveLastTrack(M[" + this.autoTraceID + "], "+ (numOfInputs + 1) + ",  NUM_ASCII_TRACKS, indices_main);//" + this.ID + " = singleToMTAuto("+id+")");
		
		StrangerMTrackAutomaton retMe = new StrangerMTrackAutomaton(StrangerLibrary.INSTANCE.dfaRemoveLastTrack(this.dfa, numOfInputs + 1, StrangerLibraryWrapper.NUM_ASCII_TRACKS, StrangerLibraryWrapper.indices_main));
		retMe.setStr(this.str);
		
		{
			retMe.setID(id);
			retMe.debugAutomaton();
		}
		
		initializeIndices(numOfInputs - 1);
		
		return retMe;
	}
	
	
	public void toDot(){
		System.out.flush();
		debugToFile("dfaPrintGraphviz(M["+ this.autoTraceID +"], mvar, mindices);//dfaPrintGraphviz(" + this.ID + ")");
		StrangerLibrary.INSTANCE.dfaPrintGraphviz(this.dfa, m_NUM_ASCII_TRACKS,
				mindices_main);
		debugToFile("flush_output();");
		StrangerLibrary.INSTANCE.flush_output();
	}


}
