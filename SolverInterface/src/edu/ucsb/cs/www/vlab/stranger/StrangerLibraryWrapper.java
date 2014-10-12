package edu.ucsb.cs.www.vlab.stranger;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

import edu.ucsb.cs.www.vlab.stranger.StrangerLibrary.StrangerDFA;


public class StrangerLibraryWrapper {
	/** initializes Stranger shared library before using it. If you need 
	 * JNA to protect JVM from native code failures then set protected to true.
	 * @param _protected: weather JVM is protected by JNA from native code failures or not.
	 */
	public static void initialize(boolean _protected) {
		Native.setProtected(_protected);
		indices_main = StrangerLibrary.INSTANCE
		.allocateAscIIIndexWithExtraBit(StrangerLibraryWrapper.NUM_ASCII_TRACKS);
	}
	/** stops synchronization between the StragnerDFA object and the native c structure
	 * it represents before freeing the structure.
	 * @param a: the StrangerDFA object which synchronization with will be stopped.
	 */
	public static void dfaFree(StrangerDFA a){
		a.setAutoSynch(false);
		StrangerLibrary.INSTANCE.dfaFree(a);
	}
	public static void dfaFree(Pointer a){
		StrangerLibrary.INSTANCE.dfaFree(a);
	}
	public static IntByReference indices_main;
	public static int NUM_ASCII_TRACKS = 8;
}
