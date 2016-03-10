/**
 * Helper class to run ECLiPSe-str in a separate process.
 */
package edu.boisestate.cs.old;

import com.parctechnologies.eclipse.CompoundTerm;
import com.parctechnologies.eclipse.EclipseEngine;
import com.parctechnologies.eclipse.EclipseEngineOptions;
import com.parctechnologies.eclipse.OutOfProcessEclipse;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

class RunEclipse implements Callable<String> {
	CompoundTerm result;
	String term;
	EclipseEngine e;
	EclipseEngineOptions eclipseEngineOptions;
	  File eclipseProgram;
		  
	  
	RunEclipse(){
		eclipseEngineOptions = new EclipseEngineOptions();
	    eclipseEngineOptions.setUseQueues(false);
	    e=null;
	    
	    
	    String sep = System.getProperty("file.separator");
	    eclipseProgram = new File(System.getProperty("eclipse.directory") +
								  sep + "lib" + sep + "string_exp.ecl");
	    int i=0;
	    while(e==null&& i<5){
		    try {
				e = new OutOfProcessEclipse(eclipseEngineOptions);
				e.compile(eclipseProgram);
			}
		    catch (Exception e) {
		    	if(e==null)
		    		System.err.println("Warning: e not initialized...#" + i);
		    	else
		    		System.err.println("Warning: e is terminated...#" + i);
		    	e=null;
		    	i++;
			}
	    }
	    if(i==5){
	    	System.err.println("Error: Too many failed attempts at initializing eclipse");
	    	System.exit(1);
	    }
	}
//        Field f;
//		try {
//			f = e.getClass().getDeclaredField("pid");
//			f.setAccessible(true);
//			pid = (Integer) f.get(e);
//		}
//		catch(Exception e){
//			pid=-1;
//			e.printStackTrace();
//		}
//	}
//	
//	public int getPid(){
//		return pid;
//	}
	
	/**
	 * Sets the term
	 * @param newTerm The new term.
	 */
	public void setTerm(String newTerm){
		term=newTerm;
	}
	/**
	 * Closes the instance of ECLiPSe.
	 */
	public void destroy(){
		try {
			((OutOfProcessEclipse) e).destroy();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    @Override
    public String call() throws Exception {
    	result=e.rpc(term);

	    return null;
    }
    
    public CompoundTerm getResult(){
    	return result;
    }
}
