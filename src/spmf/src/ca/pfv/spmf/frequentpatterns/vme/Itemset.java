package ca.pfv.spmf.frequentpatterns.vme;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class represents an itemset (a set of items)
 * @author Philippe Fournier-Viger 
 */
public class Itemset{
	private final List<Integer> items = new ArrayList<Integer>(); // ordered
	private Set<Integer> transactionsIds = new HashSet<Integer>();
	
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

	public void setTransactioncount(Set<Integer> listTransactionIds) {
		this.transactionsIds = listTransactionIds;
	}
	
	public int size(){
		return items.size();
	}

	public Set<Integer> getTransactionsIds() {
		return transactionsIds;
	}
}
