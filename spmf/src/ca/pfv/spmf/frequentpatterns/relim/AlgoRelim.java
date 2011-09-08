package ca.pfv.spmf.frequentpatterns.relim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.pfv.spmf.frequentpatterns.apriori.ContextApriori;
import ca.pfv.spmf.frequentpatterns.apriori.ItemApriori;
import ca.pfv.spmf.frequentpatterns.apriori.ItemsetApriori;
import ca.pfv.spmf.frequentpatterns.apriori.Itemsets;

/**
 * This is an implementation of the RELIM algorithm as described by :
 * 
 * Borgelt, C. (2005) Keeping Things Simple: Finding Frequent Item Sets by Recursive Elimination
 * Workshop Open Source Data Mining Software (OSDM'05, Chicago, IL), 66-70.
 * ACM Press, New York, NY, USA 2005
 * 
 * It is not a very efficient frequent itemset mining algorithm, but I decided to implement it
 * because it is simple.
 * 
 * Note that it might not be implemented in a very optimized way. One reason is that in the original
 * article there is no pseudo-code for the algorithm.
 * 
 * @author Philippe Fournier-Viger, 2009
 */
public class AlgoRelim {

	protected Itemsets frequentItemsets = new Itemsets("FREQUENT ITEMSETS");
	protected ContextApriori context; // initial context
	private long startTimestamp; // for stats
	private long endTimestamp; // for stats
	private int relativeMinsupp;
	
	private int items[];

	public AlgoRelim(ContextApriori context) {
		this.context = context;
	}

	public Itemsets runAlgorithm(double minsupp) {
		startTimestamp = System.currentTimeMillis();
		
		this.relativeMinsupp = (int) Math.ceil(minsupp * context.size());
		
		// (1) First database pass : calculate frequency of each item.
		final Map<ItemApriori, Integer> mapSupport = new HashMap<ItemApriori, Integer>();
		// for each transaction
		for(ItemsetApriori itemset : context.getObjects()){
			for(ItemApriori item : itemset.getItems()){
				if(mapSupport.get(item) == null){
					mapSupport.put(item, 1);
				}else{
					mapSupport.put(item, mapSupport.get(item) + 1);
				}
			}
		}
		
		// (2) remove items that are not frequent from database.
		// for each transaction
		for(ItemsetApriori itemset : context.getObjects()){
			// for each itemset
			for(int i=itemset.size() -1; i>=0; i--){
				if(mapSupport.get(itemset.get(i)) < relativeMinsupp){
					itemset.getItems().remove(i);
				}
			}
		}
		
		// (3) Sort database according to the frequency of items.
		// for each transaction
		for(ItemsetApriori itemset : context.getObjects()){
			// sort it
			Collections.sort(itemset.getItems(), new Comparator<ItemApriori>(){
				public int compare(ItemApriori item1, ItemApriori item2){
					int compare = mapSupport.get(item1) - mapSupport.get(item2);
					if(compare ==0){
						return (item1.getId() - item2.getId());
					}
					return compare;
				}
			});
		}
		
		// (4)Create ordered list of items by frequency and lexical ordering
		List<ItemApriori> listItems = new ArrayList<ItemApriori>();
		for(ItemApriori item : mapSupport.keySet()){
			if(mapSupport.get(item) > relativeMinsupp){
				listItems.add(item);
			}
		}
		Collections.sort(listItems, new Comparator<ItemApriori>(){
			public int compare(ItemApriori item1, ItemApriori item2){
				int compare = mapSupport.get(item1) - mapSupport.get(item2);
				if(compare ==0){
					return (item1.getId() - item2.getId());
				}
				return compare;
			}
		});
		
		context.printContext();

		//(5) Create initial database structure
		int supports[] = new int[listItems.size()];
		items = new int[listItems.size()];
		for(int i=0; i< listItems.size(); i++){
			items[i] = listItems.get(i).getId();
		}
		DatabaseStructure initialDatabase =  new DatabaseStructure(supports);
		initialDatabase.initializeTransactions();
		
		// insert transactions into initial database structure...
		for(ItemsetApriori itemset : context.getObjects()){
//			if(itemset.size() >= 2){  // IS THIS CORRECT ?
				// find index of where we should insert
				ItemApriori firstItem = itemset.get(0);
				int indexArray = listItems.indexOf(firstItem);
//				System.out.println(" f " + firstItem + " " + indexArray + " list " + listItems);
				supports[indexArray]++;
				
				// insert remaining items
				List<Integer> remainingItems = new ArrayList<Integer>();
				for(int i = 1; i<itemset.size(); i++){
					remainingItems.add(itemset.get(i).getId());
				}
				initialDatabase.transactions.get(indexArray).add(remainingItems);
//			}
		}
//		System.out.println(initialDatabase.toString());
		
		// (7) START RECURSION
		recursion(initialDatabase, new ArrayList<Integer>());

		endTimestamp = System.currentTimeMillis();
		return frequentItemsets; // Return all frequent itemsets found!
	}

	private void recursion(DatabaseStructure database, List<Integer> prefix) {
		for(int i=0; i< items.length; i++){
			if(database.supports[i] > 0 ){
				// Check if frequent
				if(database.supports[i]>= relativeMinsupp){
					// (1) add the frequent itemset to the set of frequent itemsets found!
					ItemsetApriori frequentItemset= new ItemsetApriori();
					frequentItemset.addItem(new ItemApriori(items[i]));
					for(Integer item : prefix){
						frequentItemset.addItem(new ItemApriori(item));
					}
					frequentItemset.setTransactioncount(database.supports[i]);
					frequentItemsets.addItemset(frequentItemset, frequentItemset.size());
				}
				// for each transaction for this item
				database.supports[i] = 0; // empty list for i
				
				int[] newSupportPrefix = new int[database.supports.length];
				
				DatabaseStructure databasePrefix =  new DatabaseStructure(newSupportPrefix);
				databasePrefix.initializeTransactions();
	
				for(List<Integer> transaction : database.transactions.get(i)){
						Integer firstItem = transaction.get(0);
						int index = getIndexOf(firstItem);
						database.supports[index]++;
						newSupportPrefix[index]++;
						if(transaction.size() >= 2){
							List<Integer> subList = transaction.subList(1, transaction.size());
							databasePrefix.transactions.get(index).add(subList);
							database.transactions.get(index).add(subList);
						}
				}
//				database.transactions.get(i).clear(); // not necessary
				
				// recursion
				List<Integer> newPrefix = new ArrayList<Integer>();
				newPrefix.addAll(prefix);
				newPrefix.add(items[i]);
	
				recursion(databasePrefix, newPrefix);
			}
		}
	}
	
	public int getIndexOf(int item){ // Could be optimized with a hashmap
		for(int i=0; i < items.length; i++){
			if(item == items[i]){
				return i;
			}
		}
		return -1;
	}

	public void printStats() {
		System.out
				.println("=============  RELIM - STATS =============");
		long temps = endTimestamp - startTimestamp;
		System.out.println(" Transactions count from database : "
				+ context.size());
		System.out.println(" Frequent itemsets count : " + frequentItemsets.getItemsetsCount()); 
		frequentItemsets.printItemsets(context.size());
		System.out.println(" Total time ~ " + temps + " ms");
		System.out
				.println("===================================================");
	}

	public Itemsets getItemsets() {
		return frequentItemsets;
	}
}
