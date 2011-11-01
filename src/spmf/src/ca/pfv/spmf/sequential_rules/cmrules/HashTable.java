package ca.pfv.spmf.sequential_rules.cmrules;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is not currently used.
 * @author phil
 *
 */
public class HashTable {
	
	private int size;
	private List<Itemset>[] table;
	
	public HashTable(int size){
		this.size = size;
		table = new ArrayList[size];
	}
	public boolean containsSupersetOf(Itemset itemset) {
		int hashcode = hashCode(itemset);
		if(table[hashcode] ==  null){
			return false;
		}
		for(Object object : table[hashcode]){
			Itemset itemset2 = (Itemset)object;
			if(itemset2.getItems().contains(itemset.getItems())){
				return true;
			}
		}
		return false;
	}
	public void put(Itemset itemset) {
		int hashcode = hashCode(itemset);
		if(table[hashcode] ==  null){
			table[hashcode] = new ArrayList<Itemset>();
		}
		table[hashcode].add(itemset);
	}
	
	public int hashCode(Itemset itemset){
		int hashcode =0;
		for(Integer tid : itemset.getTransactionsIds()){
			hashcode += tid;
		}
		return (hashcode % size);
	}

}
