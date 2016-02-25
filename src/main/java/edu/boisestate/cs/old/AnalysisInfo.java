/**
 * Uses a properties file to collect hotspots for an analysis.
 * @author Scott Kausler
 */

package edu.boisestate.cs.old;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class AnalysisInfo {
	private static Set endMethods=null;
	
	/**
	 * Sets up and returns a reference to hotspots in the analysis.
	 * @param fileName a properties file name.
	 * @return A set of method names for hotspots.
	 */
	public static Set getEndMethods(String fileName){

		if(endMethods==null){
			//get properties file
			  Properties prop = new Properties();
			 try {
				prop.load(new FileInputStream(fileName));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			 
			 //get array split up by the semicolin
			 String[] a = prop.getProperty("endMethods").split(",");
			 endMethods=new HashSet<String>(Arrays.asList(a));
		}
		 return endMethods;
	}

}
