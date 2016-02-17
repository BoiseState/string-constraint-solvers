package extendedSolvers;

//Special class to hold the values
//of concrete strings together
//with the feasibility
public class ConcreteValue {
	
	String value;
	boolean feasible;
	
	//always feasible in the root nodes
	//of the graph
	public ConcreteValue(String val){
		value = val;
		feasible = true;
	}
	
	//creates an infeasible 
	//concrete value
	public ConcreteValue(){
		value = null;
		feasible = false;
	}
	
	public ConcreteValue(String val, boolean feas){
		value = val;
		feasible = feas;
	}
	
	public String getValue(){
		return value;
	}
	
	public boolean isFeasible(){
		return feasible;
	}
	
	public void setInfeasible(){
		feasible = false;
	}
	
	/**
	 * return@ the copy of itself
	 **/
	public ConcreteValue copy(){
		return new ConcreteValue(value, feasible);
	}
	
	
	@Override
	public boolean equals(Object o){
		boolean ret = false;
		if(o instanceof ConcreteValue){
			ConcreteValue other = (ConcreteValue) o;
			if(value.equals(o) && feasible==other.isFeasible()){
				ret = true;
			}
		}
		return ret;
	}
	
	@Override
	public String toString(){
		return value + " " + feasible;
	}
	

}
