package edu.ucsb.cs.www.vlab.stranger;


// thrown for unsupported regexes
public class UnsupportedRegexException 
extends RuntimeException {
	
	public UnsupportedRegexException() { 
		super();
	}
	
	public UnsupportedRegexException(String message) {
		super(message);
	}

}
