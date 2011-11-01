package ca.pfv.spmf.sequentialpatterns.prefixspan_with_strings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * This class represents an itemset from a sequence from a sequence database.
 * @author Philippe Fournier-Viger 
 */
public class Itemset{

	private final List<String> items = new ArrayList<String>(); // ordered list.
	
	public Itemset(String item){
		addItem(item);
	}
	
	public Itemset(){
	}

	public void addItem(String value){
		if(!items.contains(value)){
			items.add(value);
		}
	}
	
	public List<String> getItems(){
		return items;
	}
	
	public String get(int index){
		return items.get(index);
	}
	
	public void print(){
		System.out.print(toString());
	}
	
	public String toString(){
		StringBuffer r = new StringBuffer ();
		for(String attribute : items){
			r.append(attribute.toString());
			r.append(' ');
		}
		return r.toString();
	}

	
	public int size(){
		return items.size();
	}
	
	public Itemset cloneItemSetMinusItems(Map<String, Set<Integer>> mapSequenceID, double minsuppRelatif) {
		Itemset itemset = new Itemset();
		for(String item : items){
			if(mapSequenceID.get(item).size() >= minsuppRelatif){
				itemset.addItem(item);
			}
		}

		return itemset;
	}
	
	public Itemset cloneItemSet(){
		Itemset itemset = new Itemset();
		itemset.getItems().addAll(items);
		return itemset;
	}
}
