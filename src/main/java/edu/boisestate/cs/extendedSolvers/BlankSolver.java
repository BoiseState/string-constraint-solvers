package edu.boisestate.cs.extendedSolvers;

/**
 * The most basic instantiation of an ExtendedSolver. 
 * @author Scott Kausler
 *
 */
public class BlankSolver extends ExtendedSolver<String>{

	@Override
	public void newSymbolicString(int id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void newConcreteString(int id, String string) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String replaceEscapes(String value) {
		return value;
	}

	@Override
	public void propagateSymbolicString(int id, int base) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void append(int id, int base, int arg, int start, int end) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void append(int id, int base, int arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void substring(int id, int base, int start) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void substring(int id, int base, int start, int end) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLength(int id, int base, int length) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void insert(int id, int base, int arg, int offset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void insert(int id, int base, int arg, int offset, int start, int end) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCharAt(int id, int base, int arg, int offset) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void trim(int id, int base) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(int id, int base, int start, int end) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteCharAt(int id, int base, int loc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reverse(int id, int base) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void toUpperCase(int id, int base) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void toLowerCase(int id, int base) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void replace(int id, int base, int argOne, int argTwo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void contains(boolean result, int base, int arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void endsWith(boolean result, int base, int arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startsWith(boolean result, int base, int arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void equals(boolean result, int base, int arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void equalsIgnoreCase(boolean result, int base, int arg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void isEmpty(boolean result, int base) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void revertLastPredicate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getSatisfiableResult(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSatisfiable(int id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSingleton(int id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSingleton(int id, String actualValue) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSound(int id, String actualValue) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void shutDown() {
		// TODO Auto-generated method stub
		
	}

}
