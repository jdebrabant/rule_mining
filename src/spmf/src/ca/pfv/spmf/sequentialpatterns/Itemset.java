package ca.pfv.spmf.sequentialpatterns;
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

	private final List<Item> items = new ArrayList<Item>(); // ordered list.
	private long timestamp = 0; // for PrefixspanItemIntervals
	
	public Itemset(Item item, long timestamp){
		addItem(item);
		setTimestamp(timestamp);
	}
	
	public Itemset(){
	}

	public void addItem(Item value){
			items.add(value);
	}
	
	public List<Item> getItems(){
		return items;
	}
	
	public Item get(int index){
		return items.get(index);
	}
	
	public void print(){
		System.out.print(toString());
	}
	
	public String toString(){
		StringBuffer r = new StringBuffer ();
		for(Item attribute : items){
			r.append(attribute.toString());
			r.append(' ');
		}
		return r.toString();
	}

	public Itemset cloneItemSetMinusItems(Map<Item, Set<Integer>> mapSequenceID, double minsuppRelatif) {
		Itemset itemset = new Itemset();
		itemset.timestamp = timestamp;
		for(Item item : items){
			if(mapSequenceID.get(item).size() >= minsuppRelatif){
				itemset.addItem(item);
			}
		}
		return itemset;
	}
	
	public Itemset cloneItemSet(){
		Itemset itemset = new Itemset();
		itemset.timestamp = timestamp;
		itemset.getItems().addAll(items);
		return itemset;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public int size(){
		return items.size();
	}
}
