package ca.pfv.spmf.sequential_rules.cmdeogun;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of a sequence.
 * A sequence is a list of itemsets.
 * @author Philippe Fournier-Viger 
 **/
public class Sequence{
	
	private final List<Itemset> itemsets = new ArrayList<Itemset>();
	private int id; // id de la sequence
		
	public Sequence(int id){
		this.id = id;
	}

	public void addItemset(Itemset itemset) {
		itemsets.add(itemset);
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
	
	
	
	public int getId() {
		return id;
	}

	public List<Itemset> getItemsets() {
		return itemsets;
	}
	
	public Itemset get(int index) {
		return itemsets.get(index);
	}
	
	public int size(){
		return itemsets.size();
	}
	
	public void setID(int id2) {
		id = id2;
	}
	
}
