package ca.pfv.spmf.frequentpatterns.relim;

import java.util.ArrayList;
import java.util.List;

public class DatabaseStructure {
	 
	int[] supports;  
	
	List<List<List<Integer>>> transactions = new ArrayList<List<List<Integer>>>();
	
	public DatabaseStructure(int[] supports){
		this.supports = supports;
	}

	public void initializeTransactions(){
		// Initialize "transactions"
		for(int i=0; i< supports.length; i++){
			transactions.add(new ArrayList<List<Integer>>());
		}
	}
	
	public String toString(){
		StringBuffer temp = new StringBuffer();
		temp.append("\n supports : ");
		for(Integer integer: supports){
			temp.append(integer);
			temp.append(" ");
		}
		temp.append("\nLISTS\n");
		for(int i=0;  i< supports.length; i++){
			temp.append("sup: " + supports[i] + " " +  transactions.get(i).toString() + "\n");
		}
		
		return temp.toString();
	}
	
	

}
