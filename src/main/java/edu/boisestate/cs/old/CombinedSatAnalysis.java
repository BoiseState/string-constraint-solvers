/**
 * Records all of the metrics presented for comparison of string constraint solvers
 * @author Scott Kausler
 */

package edu.boisestate.cs.old;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

//-Declipse.directory=/Users/scottkausler/Research/ECLiPSe/
public class CombinedSatAnalysis implements Solver{
	
	private ArrayList<SatSolver> solvers;
	private int timedOutBPs;
	private int totalUnsat;
	private int totalSingleton;
	private int totalSingletonUnsat;
	private int totalSingletonHS;
	private int totalIncomplete;
	private int totalComplete;
	private int totalSubset;
	private int totalAccPath;
	private int totalTop;
	private int disjoint;
	private int totalUnsatDisjoint;
	private int totalIncompleteDisjoint;
	private int totalSatTo;
	private int totalUnsatTo;
	private ArrayList<Integer> incompletes;
	private ArrayList<Integer> completes;
	private ArrayList<Integer> containsCompletes;
	private ArrayList<Integer> singletonUnsat;
	private String[] bpArray={"equalsIgnoreCase", "equals", "contains", "contentEquals", "endsWith", "startsWith", "matches", "isEmpty", "regionMatches"};
	private int[] bpToCount={0,0,0,0,0,0,0,0,0};
	private ArrayList<Integer> newTops;
	private int newTotalTop;

	StringBuilder fileWrite;
	
	public CombinedSatAnalysis(String properties, String tempFile) {
		fileWrite=new StringBuilder();
		timedOutBPs=0;
		solvers=new ArrayList<SatSolver>();
		//JSA should always go first. It constructs a static list of past constraints.
		solvers.add(new JSASolver(false,properties, "JSA"+tempFile));
		solvers.add(new StrangerSolver(false,properties, "Stranger"+tempFile));
		solvers.add(new Z3StrSolver(false,properties, "Z3Str"+tempFile));
//		solvers.add(new DPRLESolver(false, properties, "DPRLE"+tempFile));
		
		totalUnsat=totalSingleton=totalSingletonHS=totalIncomplete=totalComplete=totalSubset
				=totalAccPath=totalTop=disjoint=totalUnsatDisjoint=totalIncompleteDisjoint=totalSatTo=totalUnsatTo=newTotalTop=totalSingletonUnsat=0;
		SatSolver eclipse=null;
  	  try {
			eclipse=new ECLIPSESolver(false, properties, "ECLiPSe"+tempFile);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("ECLIPSE not initiated...exiting...");
			System.exit(1);
		}
  	  solvers.add(eclipse);
  	    	  
  	  incompletes=new ArrayList<Integer>(Collections.nCopies(solvers.size(), 0));
  	  completes=new ArrayList<Integer>(Collections.nCopies(solvers.size(), 0));
  	  containsCompletes=new ArrayList<Integer>(Collections.nCopies(solvers.size(), 0));
  	  singletonUnsat=new ArrayList<Integer>(Collections.nCopies(solvers.size(), 0));
  	  newTops=new ArrayList<Integer>(Collections.nCopies(solvers.size(), 0));
	}

	@Override
	public void addRoot(String value, String actualValue, int id) {
//		ArrayList<AddRoot> threads=new ArrayList<AddRoot>();
		for(int i=0; i<solvers.size(); i++){
//			threads.add(new AddRoot(solvers.get(i),value,actualValue, id));
//			threads.get(i).start();
			solvers.get(i).addRoot(value, actualValue, id);
		}
//		for(int i=0; i<threads.size();i++){
//			try {
//				threads.get(i).join();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
	}

	@Override
	public void addOperation(String value, String actualValue, int id,
							 HashMap<String, Integer> sourceMap) {
		boolean updateTop=false;
		boolean[]tops={false,false,false,false};
//		ArrayList<AddOperation> threads=new ArrayList<AddOperation>();
		for(int i=0; i<solvers.size(); i++){
//			threads.add(new AddOperation(solvers.get(i),value,actualValue, id, sourceMap));
//			threads.get(i).start();
			int tempTop=solvers.get(i).getNumSymbolicOps();
			solvers.get(i).addOperation(value, actualValue, id, sourceMap);
			if(tempTop!=solvers.get(i).getNumSymbolicOps()){
				updateTop=true;
				tops[i]=true;
			}
		}
		boolean updateNewTop=false;
		if(!(((JSASolver)solvers.get(0)).isAnyString(id)||((StrangerSolver)solvers.get(1)).isAnyString(id)))
			updateNewTop=true;
		
		if(updateNewTop){
			for(int i=0; i<tops.length; i++){
				if(tops[i]){
					newTops.set(i, newTops.get(i)+1);
				}
			}
		}
		if(updateTop){
			totalTop++;
			if(updateNewTop)
				newTotalTop++;
		}
//		for(int i=0; i<threads.size();i++){
//			try {
//				threads.get(i).join();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
	}

	@Override
	public void addEnd(String value, String actualValue, int id,
					   HashMap<String, Integer> sourceMap) {
		boolean updateSingleton=false;
		boolean updateSingletonHS=false;
		boolean updateSubset=false;
		boolean updateAccPath=false;
		boolean updateTotalSingletonUnsat=false;
		boolean timeout=false;

		
//		ArrayList<AddEnd> threads=new ArrayList<AddEnd>();
		for(int i=0; i<solvers.size(); i++){
//			threads.add(new AddEnd(solvers.get(i),value,actualValue, id, sourceMap));
//			threads.get(i).start();
			
			int tempSingleton=solvers.get(i).getCorrectConstraint();
			int tempSingletonHS=solvers.get(i).getCorrectHotspot();
			int tempSubset=solvers.get(i).getSubsets();
			int tempAccPath=solvers.get(i).getAccuratePaths();
			
			solvers.get(i).addEnd(value, actualValue, id, sourceMap);
			if(solvers.get(i).isTimeout(id))
				timeout=true;
			
			if(tempSingleton!=solvers.get(i).getCorrectConstraint()){
				updateSingleton=true;
				
				if(solvers.get(i).getResult().contains("UNSAT")){
					//first check if there was a timeout in calculating the singleton
					if(!solvers.get(i).toSingleton.contains(id)){
						singletonUnsat.set(i, singletonUnsat.get(i)+1);
						updateTotalSingletonUnsat=true;
					}
				}
//				else{
//					System.err.println("Singleton Not Unsat ("+id+")"+solvers.get(i).getName()+":"+value+" "+actualValue);
//				}
					
			}

			if(tempSingletonHS!=solvers.get(i).getCorrectHotspot())
				updateSingletonHS=true;
			if(tempSubset!=solvers.get(i).getSubsets())
				updateSubset=true;
			if(tempAccPath!=solvers.get(i).getAccuratePaths())
				updateAccPath=true;
		}
		
		if(timeout)
		{
			for(int i=0; i<bpArray.length; i++){
				if(SatSolver.checkPastListContains(id,bpArray[i])){
					bpToCount[i]++;
				}
			}
			timedOutBPs++;
			for(int i=0; i<solvers.size(); i++){
				solvers.get(i).revertBP(id);
				updateSingleton=true;
				
				if(updateTotalSingletonUnsat && solvers.get(i).getResult().contains("UNSAT") && !solvers.get(i).toSingleton.contains(id)){
					singletonUnsat.set(i, singletonUnsat.get(i)-1);
				}
			}
		}
		else
		{
			if(updateTotalSingletonUnsat)
				totalSingletonUnsat++;
			if( updateSingleton)
				totalSingleton++;
			
			if( updateSubset)
				totalSubset++;
			if( updateAccPath)
				totalAccPath++;
		}
		if( updateSingletonHS)
			totalSingletonHS++;
		
//		for(int i=0; i<threads.size();i++){
//			try {
//				threads.get(i).join();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		
		
		if(!timeout&&SatSolver.containsBoolFunction(value)){
			String fName=value.split("!!")[0];

			boolean mayBeIncomplete=false;
			boolean updateTotal=false;
			boolean updateSat=false;
			boolean isToTaint=false;
			for(int i=0; i<solvers.size(); i++){
				if(solvers.get(i).getToTaint(id))
					isToTaint=true;
				SatSolver currentSolver=solvers.get(i);
				
				String result=currentSolver.getResult();
				if(result.equals("UNSAT")){
					System.err.println("Error: UNSAT encountered: bug in " + currentSolver.getName());
					System.err.println(value + " " + actualValue + " " + id);
					System.exit(1);
				}
				if((result.equals("TRUEUNSAT")&&actualValue.equals("true"))||(result.equals("FALSEUNSAT")&&actualValue.equals("false"))){
					System.err.println("Error: bad report: " + currentSolver.getName());
					System.err.println(value + " " + actualValue + " " + id);
					System.exit(1);
				}
				if(result.equals("SAT"))
					updateSat=true;
				if(!(result.equals("UNKNOWN")||result.equals("TIMEOUT")||(result.equals("SAT")&&currentSolver.getTaint()))){
					if(result.equals("TRUEUNSAT")||result.equals("FALSEUNSAT")){
						
						//Make sure reports of incomplete are valid
//						if((currentSolver instanceof JSASolver || currentSolver instanceof StrangerSolver) && solvers.size()>3 && solvers.get(3) instanceof ECLIPSESolver && solvers.get(2) instanceof Z3StrSolver){
//							ECLIPSESolver eclipse=(ECLIPSESolver)solvers.get(3);
//							String eclResult=eclipse.getResult();
//							int errCode=0;
//							if(!(eclResult.equals("TIMEOUT")|| eclResult.equals("TRUEUNSAT")|| eclResult.equals("FALSEUNSAT"))){
//								errCode=1;
//							}
//							Z3StrSolver z3=(Z3StrSolver)solvers.get(2);
//							String z3Result=z3.getResult();
//							if(!(z3Result.equals("TIMEOUT")|| z3Result.equals("TRUEUNSAT")|| z3Result.equals("FALSEUNSAT")))
//								errCode+=2;
//							if(errCode>0){
//								System.err.print("\nError: ");
//								if(currentSolver instanceof JSASolver)
//									System.err.print("JSA");
//								else
//									System.err.print("STRANGER");
//								System.err.print(" Reports UNSAT result when ");
//								if(errCode==1)
//									System.err.print("ECLiPSe");
//								else if(errCode==2)
//									System.err.print("Z3-str");
//								else
//									System.err.print("ECLiPSe and Z3-str");
//								System.err.println(" Don't report unsat or timeout:");
//								System.err.println(actualValue);
//								//System.err.println(eclipse.getInfo(id));
//								System.err.println(currentSolver.getPastList(id));
//							}
//						}
						if(!mayBeIncomplete && currentSolver instanceof ECLIPSESolver){
							if(isToTaint)
								totalUnsatTo++;
							totalUnsat++;
//							if(isToTaint)
//								totalSatTo++;
//							sat++;
//							totalComplete++;
						}
						else
							mayBeIncomplete=true;
					}
					else{
				//	if(currentSolver.getTaint()){
						updateTotal=true;
						if(fName.equals("contains")||fName.equals("startsWith")||fName.equals("endsWith"))
							containsCompletes.set(i, containsCompletes.get(i)+1);
						else
							completes.set(i, completes.get(i)+1);
					}
				}
			}
			if(updateSat){
				if(isToTaint)
					totalSatTo++;
				disjoint++;
			}
			if(updateTotal){
				totalComplete++;
			}
			if(updateSat&&!updateTotal){
				totalIncompleteDisjoint++;
			}
			
			if(updateSat||mayBeIncomplete){
				totalUnsatDisjoint++;
			}
			if(mayBeIncomplete){
				if(isToTaint)
					totalUnsatTo++;
				totalUnsat++;
				boolean updateTotalIncom=false;

					for(int i=0; i<solvers.size(); i++){
						String result=solvers.get(i).getResult();
						if(!(result.equals("TRUEUNSAT")||result.equals("FALSEUNSAT"))){
							incompletes.set(i, incompletes.get(i)+1);
							updateTotalIncom=true;
						}	
					}
					if(updateTotalIncom)
						totalIncomplete++;
			}
		}
	}
	public void printHeader(String val){
		for(int i=0; i<solvers.size(); i++){
			StringBuilder name=new StringBuilder(solvers.get(i).getName());
			name.setLength(3);
			System.out.print("\t" + name + val);
		}
	}
	public void getStats(){
		System.out.print("#hotspots\t#ops\t#constraints\t#timedOutBPs");
		printHeader("ConTime");
		printHeader("HSTime");
		printHeader("MedConTime");
		printHeader("MedHSTime");
		printHeader("Timeouts");
		
		System.out.print("\tTUNSAT");
		printHeader("UNSAT");
		System.out.print("\tTUNSATTO");
		printHeader("UNSATTO");
		System.out.print("\tTSingleton");
		printHeader("Singleton");
		System.out.print("\tTSingletonUnsat");
		printHeader("SingletonUnsat");
		System.out.print("\tTSingletonHS");
		printHeader("SingletonHS");
		System.out.print("\tTIncomplete");
		printHeader("Incomplete");
		System.out.print("\tTIncompleteDisjoint");
		System.out.print("\tTUnsatDisjoint");
		System.out.print("\tTDisjoint");
		printHeader("Disjoint");
		System.out.print("\tTDisjointTo");
		printHeader("DisjointTo");
		System.out.print("\tTComplete");
		printHeader("Complete");
		System.out.print("\tTSubset");
		printHeader("Subset");
		System.out.print("\tTAccPath");
		printHeader("AccPath");
		System.out.print("\tTTop");
		printHeader("Top");
		System.out.print("\tTNewTop");
		printHeader("NewTop");
		printHeader("AddVal");
		printHeader("NumUnsound");
		for(int i=0; i<bpArray.length; i++){
			System.out.print("\t" + bpArray[i] + "Tos");
		}
		System.out.println();
		
		int numHotSpots=solvers.get(0).getNumHotSpots();
		int numOps=solvers.get(0).getNumOperations();
		int numConstraints=solvers.get(0).getNumConstraints();
		
		double floatConstraints=(numConstraints-timedOutBPs)/100.0;
		double floatOps=numOps/100.0;
		double floatHotSpots=numHotSpots/100.0;
		
		fileWrite.append(numHotSpots);
		fileWrite.append("\t"+numOps);
		fileWrite.append("\t"+numConstraints);
		fileWrite.append("\t"+timedOutBPs);


		for(int i=0; i<solvers.size(); i++){
			fileWrite.append("\t"+solvers.get(i).getTotalConstraintTime());
		}
		for(int i=0; i<solvers.size(); i++){
			fileWrite.append("\t"+solvers.get(i).getTotalHotSpotTime());
		}
		for(int i=0; i<solvers.size(); i++){
			fileWrite.append("\t"+solvers.get(i).getMediumConstraintTime());
		}
		for(int i=0; i<solvers.size(); i++){
			fileWrite.append("\t"+solvers.get(i).getMediumHotSpotTime());
		}
		for(int i=0; i<solvers.size(); i++){
			fileWrite.append("\t"+(solvers.get(i).getTimeouts()));
		}
		
		fileWrite.append("\t"+totalUnsat/floatConstraints);
		for(int i=0; i<solvers.size(); i++){
			fileWrite.append("\t"+(solvers.get(i).getTrueUnsat()+solvers.get(i).getFalseUnsat())/floatConstraints);
		}	
		fileWrite.append("\t"+totalUnsatTo/floatConstraints);
		for(int i=0; i<solvers.size(); i++){
			fileWrite.append("\t"+solvers.get(i).getUnsatTimeout()/floatConstraints);
		}
		fileWrite.append("\t"+totalSingleton/floatConstraints);
		for(int i=0; i<solvers.size(); i++){
			fileWrite.append("\t"+solvers.get(i).getCorrectConstraint()/floatConstraints);
		}
		fileWrite.append("\t"+totalSingletonUnsat/floatConstraints);
		for(int i=0; i<solvers.size(); i++){
			fileWrite.append("\t"+singletonUnsat.get(i)/floatConstraints);
		}
		fileWrite.append("\t"+totalSingletonHS/floatHotSpots);
		for(int i=0; i<solvers.size(); i++){
			fileWrite.append("\t"+solvers.get(i).getCorrectHotspot()/floatHotSpots);
		}
		fileWrite.append("\t"+totalIncomplete/floatConstraints);
		for(int i=0; i<solvers.size(); i++){
			fileWrite.append("\t"+incompletes.get(i)/floatConstraints);
		}
		fileWrite.append("\t"+totalIncompleteDisjoint/floatConstraints);
		fileWrite.append("\t"+totalUnsatDisjoint/floatConstraints);
		fileWrite.append("\t"+disjoint/floatConstraints);
		for(int i=0; i<solvers.size(); i++){
			fileWrite.append("\t"+solvers.get(i).getSat()/floatConstraints);
		}
		fileWrite.append("\t"+totalSatTo/floatConstraints);
		for(int i=0; i<solvers.size(); i++){
			fileWrite.append("\t"+solvers.get(i).getSatTimeout()/floatConstraints);
		}
		fileWrite.append("\t"+(totalComplete)/floatConstraints);
		for(int i=0; i<solvers.size(); i++){
			fileWrite.append("\t"+(completes.get(i)+containsCompletes.get(i))/floatConstraints);
		}		
		fileWrite.append("\t"+totalSubset/floatConstraints);
		for(int i=0; i<solvers.size(); i++){
			fileWrite.append("\t"+solvers.get(i).getSubsets()/floatConstraints);
		}
		fileWrite.append("\t"+totalAccPath/floatConstraints);
		for(int i=0; i<solvers.size(); i++){
			fileWrite.append("\t"+solvers.get(i).getAccuratePaths()/floatConstraints);
		}
		fileWrite.append("\t"+totalTop/floatOps);
		for(int i=0; i<solvers.size(); i++){
			fileWrite.append("\t"+solvers.get(i).getNumSymbolicOps()/floatOps);
		}
		fileWrite.append("\t"+newTotalTop/floatOps);
		for(int i=0; i<solvers.size(); i++){
			fileWrite.append("\t"+newTops.get(i)/floatOps);
		}
		
		for(int i=0; i<solvers.size(); i++){
			fileWrite.append("\t"+solvers.get(i).getNumOver());
		}
		
		for(int i=0; i<solvers.size(); i++){
			fileWrite.append("\t"+solvers.get(i).getNumUnsound());
		}
		for(int i=0; i<bpToCount.length; i++){
			fileWrite.append("\t"+bpToCount[i]);
		}
		fileWrite.append("\n");
		System.out.println(fileWrite);
	}

	@Override
	public void remove(int id) {
		for(int i=0; i<solvers.size(); i++){
			solvers.get(i).remove(id);
		}
	}
	
	@Override
	public void finishUp(){
		for(int i=0; i<solvers.size(); i++){
			solvers.get(i).finishUp();
		}
	}

	@Override
	public void writeToFile() {
		try {
	    PrintWriter
				out = new PrintWriter(new BufferedWriter(new FileWriter("stats.txt", true)));
	    out.print(fileWrite);
	    out.close();
	} catch (IOException e) {
		e.printStackTrace();
	}
	}
	
	private class AddRoot extends Thread {
		private SatSolver solver;
		private String value;
		private String actualValue;
		private int id;
		AddRoot(SatSolver solver, String value, String actualValue, int id){
			super();
			this.solver=solver;
			this.value=value;
			this.actualValue=actualValue;
			this.id=id;
		}

		@Override
		public void run() {
			solver.addRoot(value, actualValue, id);
		}	
	}
	
	private class AddOperation extends Thread {
		private SatSolver solver;
		private String value;
		private String actualValue;
		private int id;
		private HashMap<String, Integer> sourceMap;
		AddOperation(SatSolver solver, String value, String actualValue, int id, HashMap<String, Integer> sourceMap){
			super();
			this.solver=solver;
			this.value=value;
			this.actualValue=actualValue;
			this.id=id;
			this.sourceMap=new HashMap<String, Integer>(sourceMap);
		}

		@Override
		public void run() {
			solver.addOperation(value, actualValue, id, sourceMap);
		}	
	}
	private class AddEnd extends Thread {
		private SatSolver solver;
		private String value;
		private String actualValue;
		private int id;
		private HashMap<String, Integer> sourceMap;
		AddEnd(SatSolver solver, String value, String actualValue, int id, HashMap<String, Integer> sourceMap){
			super();
			this.solver=solver;
			this.value=value;
			this.actualValue=actualValue;
			this.id=id;
			this.sourceMap=new HashMap<String, Integer>(sourceMap);
		}

		@Override
		public void run() {
			solver.addEnd(value, actualValue, id, sourceMap);
		}	
	}
	@Override
	public void addHeight(int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean caresAboutHeight() {
		// TODO Auto-generated method stub
		return false;
	}
}
