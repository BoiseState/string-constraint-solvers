/**
 * A vertex in the collector/flow graph representing some string value/constraint.
 * @author Scott Kausler
 */
package analysis;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class PrintConstraint implements Serializable, Comparable<PrintConstraint>{

	private static final long serialVersionUID = 1L;
	private static int globalNum=0;
	private String value;
	private String actualVal;
	private LinkedList<PrintConstraint> sourceConstraints;
	private int type;
	private int num;
	private int id;
	private long timeStamp;
	private HashMap<String, Integer> sourceMap;

	/**
	 * 
	 * @param value The name of the vertex (e.g., the symbolic value or sting method name)
	 * @param actualVal The actual value gathered from the program execution.
	 */
	public PrintConstraint(String value, String actualVal) {
		this.value=value;
		sourceConstraints=new LinkedList<PrintConstraint>();
		sourceConstraints.add(this);
		type=0;
		num=0;
		id=globalNum++;
		this.actualVal=actualVal;
		timeStamp=System.nanoTime();
	}
	/**
	 * Used to make a unique copy of a previous constraint, i.e., when a symbolic value is reused.
	 * @param oldConstraint The constraint to make a copy of
	 * @param actualVal The actual value gathered from the program execution.
	 */
	public PrintConstraint(PrintConstraint oldConstraint, String actualVal){
		this.value=oldConstraint.value;

		this.type=oldConstraint.type;
		this.num=oldConstraint.num+1;
		this.actualVal=actualVal;
			timeStamp=System.nanoTime();
			id=globalNum++;
			sourceConstraints=new LinkedList<PrintConstraint>();
			//sourceConstraints.removeFirst();
			//sourceConstraints.add(this);
			sourceConstraints.addFirst(this);
//			if(oldConstraint.type==0&&oldConstraint.sourceConstraints.size()>1){
//				sourceConstraints.add(oldConstraint.getSource(id));
//			}
	}
	
	/**
	 * Sets the source of this vertex in the flow graph.
	 * @param source The new source
	 */
	public void setSource(PrintConstraint source){
		sourceConstraints.add(source);
	}

	/**
	 * Sets the type of the constraint, e.g., is it a parameter?
	 * @param type An integer value representing the new source
	 */
	public void setType(int type){
		this.type=type;
	}
	
	/**
	 * At vertex may have a different source at different points in execution. Thus this method looks for the last source that
	 * existed depending on the argument.
	 * @param num1 Denotes the unique id of the point with which we are looking for the source.
	 * @return The source depending on the argument.
	 */
	public PrintConstraint getSource(int num1){
		Iterator<PrintConstraint> it=sourceConstraints.descendingIterator();
		while(it.hasNext()){
			PrintConstraint source=it.next();
			if(num1>source.id){
				return source;
			}
		}
		return sourceConstraints.getFirst();
		}
	public String toString(){
		return value+"("+num+")"+"-"+id;
	}
	/**
	 * @return The vertex's id.
	 */
	public int getId(){
		return id;
	}
	/**
	 * @return The numeric type (such as static string).
	 */
	public int getType(){
		return type;
	}
	/**
	 * @return Which copy of the original vertex this is.
	 */
	public int getNum(){
		return num;
	}
	/**
	 * @param newValue The new value (name) of this vetex.
	 */
	public void setValue(String newValue){
		this.value=newValue;
	}
	
	/**
	 * The value, (or name) of the vertex is a sting with lots of information. Thus, this method separates out the first part.
	 * @return The first part of the vertex's value
	 */
	public String getSplitValue(){
		return value.split("!:!")[0];
	}
	/**
	 * 
	 * @return The value(name of this vertex)
	 */
	public String getValue(){
		return value;
	}
	/**
	 * @param actualVal The new actual string value for this vertex gatered using DSE.
	 */
	public void setActualVal(String actualVal){
		this.actualVal=actualVal;
	}
	/**
	 * 
	 * @return The actual value of the vertex.
	 */
	public String getActualVal(){
		return actualVal;
	}
	/**
	 * @return The last source of this vertex.
	 */
	public PrintConstraint getSource() {
		Iterator<PrintConstraint> it=sourceConstraints.descendingIterator();
		while(it.hasNext()){
			PrintConstraint source=it.next();
			if(globalNum>source.id){
				return source;
			}
		}
		return sourceConstraints.getFirst();
	}
	
	@Override
	public boolean equals(Object arg) {
		if(arg instanceof PrintConstraint){
			if(id==((PrintConstraint)arg).id)
				return true;
		}
		return false;
	}
	
	//compares timestamps
	@Override
	public int compareTo(PrintConstraint o) {
		if(this.id<o.id)
			return -1;
		if(this.id==o.id)
			return 0;
		return 1;
//		Long longStamp=new Long(timeStamp);
//		return longStamp.compareTo(o.timeStamp);
	}
	
	/**Creates a PrintConstraint representing toString(for use with dual constraints in one method). Represents a special case of string method.
	 * @param source The source of the constraint
	 * @return A new PrintConstriant representing toString.
	 */
	public static PrintConstraint createToString(PrintConstraint source) {
		PrintConstraint returnConstraint=new PrintConstraint("toString!!", source.actualVal);

		returnConstraint.sourceConstraints.add(source);
		returnConstraint.type=source.type;
		returnConstraint.num=source.num;
		returnConstraint.id=source.id;
		returnConstraint.timeStamp=source.timeStamp;
		
		return returnConstraint;
	}
	
	/**
	 * Used in processing to set the source map
	 * @param sourceMap The new sourceMap for this constraint.
	 */
	public void setSourceMap(HashMap<String, Integer> sourceMap) {
		this.sourceMap=sourceMap;
	}
	/**
	 * Used in processing to get the sourceMap.
	 * @return The soruceMap
	 */
	public HashMap<String, Integer> getSourceMap() {
		return sourceMap;
	}
}
