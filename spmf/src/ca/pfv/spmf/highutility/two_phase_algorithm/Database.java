package ca.pfv.spmf.highutility.two_phase_algorithm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Philippe Fournier-Viger, 2010
 */
public class Database {

	// Contexte
	private final Set<Integer> attributes = new HashSet<Integer>();
	private final List<Transaction> transactions = new ArrayList<Transaction>();
	
	public void addTransaction(Transaction t){
		transactions.add(t);
		attributes.addAll(t.getItems());
	}

	public void loadFile(String path) throws IOException {
		String thisLine;
		BufferedReader myInput = null;
		try {
			FileInputStream fin = new FileInputStream(new File(path));
			myInput = new BufferedReader(new InputStreamReader(fin));
			while ((thisLine = myInput.readLine()) != null) {
				if(thisLine.charAt(0) != '#'){ 
					addObject(thisLine.split(":"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(myInput != null){
				myInput.close();
			}
	    }
	}
	
	public void addObject(String line[]){
		int transactionUtility = Integer.parseInt(line[1]);
		
		List<Integer> items = new ArrayList<Integer>();
		for(String item:  line[0].split(" ")){
			items.add(Integer.parseInt(item));
		}
		
		List<Integer> itemsUtilities = new ArrayList<Integer>();
		for(String utility:  line[2].split(" ")){
			itemsUtilities.add(Integer.parseInt(utility));
		}
		
		bubbleSort(items, itemsUtilities);
		
		transactions.add(new Transaction(items, itemsUtilities, transactionUtility));
	}
	
	// bubble sort
	private void bubbleSort(List<Integer> items, List<Integer> itemsUtilities) {
			for(int i=0; i < items.size(); i++){
				for(int j= items.size() -1; j>= i+1; j--){
					if(items.get(j) < items.get(j-1)){
						int temp = items.get(j);
						items.set(j, items.get(j-1));
						items.set(j-1, temp);
						int tempUtilities = itemsUtilities.get(j);
						itemsUtilities.set(j, itemsUtilities.get(j-1));
						itemsUtilities.set(j-1, tempUtilities);
					}
				}
			}
		}

	public void printContext(){
		System.out
		.println("===================  Database ===================");
		int count = 0;
		for(Transaction itemset : transactions){ // pour chaque objet
			System.out.print("0" + count + ":  ");
			itemset.print();
			System.out.println("");
			count++;
		}
	}
	
	public int size(){
		return transactions.size();
	}

	public List<Transaction> getTransactions() {
		return transactions;
	}

	public Set<Integer> getAttributes() {
		return attributes;
	}

}
