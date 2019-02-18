package edu.boisestate.cs.reporting;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class MCDifference {

	public static void main(String[] args) {
		//takes in two file names
		String fName = "delete_concat_l3.txt";
		if(args.length != 0){
			fName = args[0];
		}
		
		String f1Path = "./data/correctness/concrete/" + fName;
		String f2Path = "./data/correctness/weightedAcyclic/" + fName;
		
		try {
			BufferedReader brF1 = new BufferedReader(new FileReader(f1Path));
			BufferedReader brF2 = new BufferedReader(new FileReader(f2Path));
			
			brF1.readLine();
			brF2.readLine();
			//the first line is the header, so read the next one
			String line1 = brF1.readLine();
			String line2 = brF2.readLine();
			while(line1 != null && line2 != null){
				String[] line1Val = line1.split("\t");
				String[] line2Val = line2.split("\t");
				//make sure that nodes are the same
				if(!line1Val[0].equals(line2Val[0])){
					System.out.println("Lines do not match: " + line1Val[0] + "\t" + line1Val[0]);
					System.exit(2);
				}
				String report = "";
				//check if in count match:
				if(!line1Val[8].equals(line2Val[8])){
					report = "InCount does not match " + line1Val[8] + "\t" + line2Val[8];
				}
				//check if tCount match
				if(!line1Val[10].equals(line2Val[10])){
					report += "\nTCount does not match " + line1Val[10] + "\t" + line2Val[10];
				}
				//check if fCount match
				if(!line1Val[13].equals(line2Val[13])){
					report += "\nFCount does not match " + line1Val[13] + "\t" + line2Val[13];
				}
				if(!report.isEmpty()){
					System.out.println(line1Val[0]+"\t" + line1Val[17]);
					System.out.println(report);
					break;
				}
				
				line1 = brF1.readLine();
				line2 = brF2.readLine();
			}
			brF1.close();
			brF2.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
