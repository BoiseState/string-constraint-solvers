/**
 * Used to allow abstract storage of representations of PCs (e.g., and automata representation). Can also be used to track taint.
 */
package edu.boisestate.cs.old;

import java.util.HashMap;


public class StoreToFile {

	//private static String name="automata";
	private HashMap<Integer, Object> objMap;
	protected HashMap<Integer, Boolean> taintMap;
	public StoreToFile(){
//	     File f = null;
//	      
//	      try{      
//	         // returns pathnames for files and directory
//	         f = new File("./"+name);
//	         f.mkdir();
//	         f.deleteOnExit();
//	         
//	      }catch(Exception e){
//	         // if any error occurs
//	         e.printStackTrace();
//	      }
	      objMap=new HashMap<Integer, Object>();
	      taintMap=new HashMap<Integer, Boolean>();
	}
	public void setTaint(int i, boolean value){
		taintMap.put(i,  value);
	}
	public boolean getTaint(int i){
		return taintMap.get(i);
	}
	
	public void put(int i, Object o){
		objMap.put(i, o);
		if(!taintMap.containsKey(i)){
			taintMap.put(i, false);
		}
//		 String fileName=name+"/"+name+i;
//		   try
//		      {
//			   	File f=new File(fileName);
//			   	f.delete();
//		        f.deleteOnExit();
//			   
//			   	RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
//			   	FileOutputStream fos = new FileOutputStream(raf.getFD());
//		         ObjectOutputStream out =
//		                            new ObjectOutputStream(fos);
//		         out.writeObject(o);
//		         out.close();
//		          fos.close();
//		          raf.close();
//		      }catch(IOException e)
//		      {
//		          e.printStackTrace();
//		      }
	}
	
	public Object get(int i){
		return objMap.get(i);
//		 String fileName=name+"/"+name+i;
//	      try
//	      {
//	    	  RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
//	    	  FileInputStream fin=new FileInputStream(raf.getFD());
//	         ObjectInputStream in = new ObjectInputStream(fin);
//	         Object o =in.readObject();
//	         in.close();
//	         fin.close();
//	         raf.close();
//	         return o;
//	      }catch(IOException e)
//	      {
//	         e.printStackTrace();
//	      }catch(ClassNotFoundException c)
//	      {
//	         System.out.println("Graph not found");
//	         c.printStackTrace();
//	      }
//	      return null;
	}

	public Object remove(int id) {
		return objMap.remove(id);
		//taintMap.remove(id);
	}

	public int size() {
		return this.objMap.size();
	}

}
