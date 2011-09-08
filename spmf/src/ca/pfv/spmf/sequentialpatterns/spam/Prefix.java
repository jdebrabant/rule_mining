package ca.pfv.spmf.sequentialpatterns.spam;

import java.util.ArrayList;
import java.util.List;


/**
 * Implementation of a sequence.
 * A sequence is a list of itemsets.
 * @author Philippe Fournier-Viger 
 **/
public class Prefix{
	
	private final List<Itemset> itemsets = new ArrayList<Itemset>();
	
	public Prefix(){
	}
	

	public void addItemset(Itemset itemset) {
		itemsets.add(itemset);
	}
	
	public Prefix cloneSequence(){
		Prefix sequence = new Prefix();
		for(Itemset itemset : itemsets){
			sequence.addItemset(itemset.cloneItemSet());
		}
		return sequence;
	}

	public void print() {
		System.out.print(toString());
	}
	
	public String toString() {
		StringBuffer r = new StringBuffer("");
		for(Itemset itemset : itemsets){
			r.append('(');
			for(Integer item : itemset.getItems()){
				String string = item.toString();
				r.append(string);
				r.append(' ');
			}
			r.append(')');
		}
		return r.append("    ").toString();
	}
	
	public String itemsetsToString() {
		StringBuffer r = new StringBuffer("");
		for(Itemset itemset : itemsets){
			for(Integer item : itemset.getItems()){
				String string = item.toString();
				r.append(string);
				r.append(' ');
			}
			r.append('}');
		}
		return r.append("    ").toString();
	}

	public List<Itemset> getItemsets() {
		return itemsets;
	}
	
	public Itemset get(int index) {
		return itemsets.get(index);
	}
	
	// new : nov. 2009.
	public Integer getIthItem(int i) { 
		for(int j=0; j< itemsets.size(); j++){
			if(i < itemsets.get(j).size()){
				return itemsets.get(j).get(i);
			}
			i = i- itemsets.get(j).size();
		}
		return null;
	}
	
	public int size(){
		return itemsets.size();
	}
	
	/**
	 * Return the sum of the size of all itemsets of this sequence.
	 */
	public int getItemOccurencesTotalCount(){
		int count =0;
		for(Itemset itemset : itemsets){
			count += itemset.size();
		}
		return count;
	}

}
