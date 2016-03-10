package edu.boisestate.cs.stringRandom;

import java.util.Random;

/**
 * Code taken from 
 * http://stackoverflow.com/questions/41107/how-to-generate-a-random-alpha-numeric-string
 * @author elena sherman
 *
 */
public class RandomString {
	
	static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	static Random rnd = new Random();
	static int length = 3;

	public static String randomString(){
	   StringBuilder sb = new StringBuilder(length );
	   for( int i = 0; i < length; i++ ) 
	      sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
	   return sb.toString();
	}

}
