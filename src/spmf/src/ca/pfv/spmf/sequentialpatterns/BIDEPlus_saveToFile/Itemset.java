package ca.pfv.spmf.sequentialpatterns.BIDEPlus_saveToFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class represents an itemset from a sequence from a sequence database.
 * The itemset can thus have a timestamp.
 * @author Philippe Fournier-Viger 
 */
public class Itemset{

	private final List<Integer> items = new ArrayList<Integer>(); // ordered list.
	
	public Itemset(Integer item, long timestamp){
		addItem(item);
	}
	
	public Itemset(){
	}

	public void addItem(Integer value){
			items.add(value);
	}
	
	public List<Integer> getItems(){
		return items;
	}
	
	public Integer get(int index){
		return items.get(index);
	}
	
	public void print(){
		System.out.print(toString());
	}
	
	public String toString(){
		StringBuffer r = new StringBuffer ();
		for(Integer attribute : items){
			r.append(attribute.toString());
			r.append(' ');
		}
		return r.toString();
	}

	public Itemset cloneItemSetMinusItems(Map<Integer, Set<Integer>> mapSequenceID, double minsuppRelatif) {
		Itemset itemset = new Itemset();
		for(Integer item : items){
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
	
	public int size(){
		return items.size();
	}
}
