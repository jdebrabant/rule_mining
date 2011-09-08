package ca.pfv.spmf.highutility.two_phase_algorithm;
import java.util.List;

/**
 * This class represents an itemset (a set of items)
 * @author Philippe Fournier-Viger, 2010
 */
public class Transaction{
	private final List<Integer> items; // ordered
	private final List<Integer> itemsUtilities; // ordered
	private final int transactionUtility;
	
	public Transaction(List<Integer> items, List<Integer> itemsUtilities, int transactionUtility){
		this.items =  items;
		this.itemsUtilities = itemsUtilities;
		this.transactionUtility = transactionUtility;
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
		for(int i=0; i< items.size(); i++){
			r.append(items.get(i) + " ");
			if(i == items.size() -1){
				r.append(":");
			}
		}
		r.append(transactionUtility + ": ");
		for(int i=0; i< itemsUtilities.size(); i++){
			r.append(itemsUtilities.get(i) + " ");
		}

		return r.toString();
	}

	public boolean contains(Integer item) {
		for(Integer itemI : items){
			if(itemI == item){
				return true;
			}else if(itemI > item){
				return false;
			}
		}
		return false;
	}
	
	public boolean contains(int item) {
		for(int i=0; i<items.size(); i++){
			if(items.get(i) == item){
				return true;
			}else if(items.get(i) > item){
				return false;
			}
		}
		return false;
	}

	
	public boolean isLexicallySmallerthan(Transaction itemset2){
		for(int i=0; i< items.size(); i++){
			if(items.get(i) > itemset2.items.get(i)){
				return false;
			}
			else if(items.get(i) < itemset2.items.get(i)){
				return true;
			}
		}
		return true;
	}
	
	
	public boolean isEqualTo(Transaction itemset2){
		if(items.size() != itemset2.items.size()){
			return false;
		}
		return items.containsAll(itemset2.items);
	}

	
	public int size(){
		return items.size();
	}

	/** 
	* check if the item from this itemset are all the same as those of itemset2 
	* except the last item 
	* and that itemset2 is lexically smaller than this itemset. If all these conditions are satisfied,
	* this method return the last item of itemset2. Otherwise it returns null.
	* @return the last item of itemset2, or null.
	* */
	public Integer allTheSameExceptLastItem(Transaction itemset2) {
		if(itemset2.size() != items.size()){
			return null;
		}
		for(int i=0; i< items.size(); i++){
			// if they are the last items
			if(i == items.size()-1){ 
				// the one from items should be smaller (lexical order) and different than the one of itemset2
				if(items.get(i) >= itemset2.get(i)){  
					return null;
				}
			}
			// if they are not the last items, they  should be the same
			else if(items.get(i) != itemset2.get(i)){ 
				return null; 
			}
		}
		return itemset2.get(itemset2.size()-1);
	}
	
	/**
	 * This method compare this itemset with another itemset to see if they are equal.
	 * The method assume that the two itemsets are lexically ordered.
	 * @return  true or false
	 */
	public boolean allTheSame(Transaction itemset2) {
		if(itemset2.size() != items.size()){
			return false;
		}
		for(int i=0; i< itemset2.size(); i++){
			if(itemset2.getItems().get(i) != getItems().get(i)){
				return false;
			}
		}
		return true;
	}

	public List<Integer> getItemsUtilities() {
		return itemsUtilities;
	}

	public int getTransactionUtility() {
		return transactionUtility;
	}

}
