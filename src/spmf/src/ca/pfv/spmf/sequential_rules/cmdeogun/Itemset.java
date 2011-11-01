package ca.pfv.spmf.sequential_rules.cmdeogun;
import java.text.DecimalFormat;
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
	public Set<Integer> transactionsIds = new HashSet<Integer>();
	
	public Itemset(){
	}

	public double getRelativeSupport(int nbObject) {
		return ((double)transactionsIds.size()) / ((double) nbObject);
	}
	
	public String getSupportRelatifFormatted(int nbObject) {
		double frequence = ((double)transactionsIds.size()) / ((double) nbObject);
		DecimalFormat format = new DecimalFormat();
		format.setMinimumFractionDigits(0); 
		format.setMaximumFractionDigits(2); 
		return format.format(frequence);
	}
	
	public int getAbsoluteSupport(){
		return transactionsIds.size();
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

	public boolean contains(Integer item) {
		for(Integer itemI : items){
			if(itemI.equals(item)){
				return true;
			}else if(itemI > item){
				return false;
			}
		}
		return false;
	}
	

	public void setTIDs(Set<Integer> listTransactionIds) {
		this.transactionsIds = listTransactionIds;
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
	public Integer allTheSameExceptLastItem(Itemset itemset2) {
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
			else if(!items.get(i).equals(itemset2.get(i))){ 
				return null; 
			}
		}
		return itemset2.get(itemset2.size()-1);
	}
	
	public boolean allTheSameExceptLastItemV2(Itemset itemset2) {
		if(itemset2.size() != items.size()){
			return false;
		}
		for(int i=0; i< items.size()-1; i++){
			// if they are not the last items, they  should be the same
			 if(!items.get(i).equals(itemset2.get(i))){ 
				return false; 
			}
		}
		return true;
	}
	
	public Integer getLastItem(){
		return items.get(size()-1);
	}
	
	// another version of the previous method but don't assume the lexicographical order!
	public Integer allTheSameExcept(Itemset itemset2) {
		if(itemset2.size() != items.size()){
			return null;
		}
		Integer missingItem = null;
		for(Integer item : itemset2.getItems()){
			if(!items.contains(item)){
				if(missingItem !=null){ // more than one item is different
					return null;
				}
				missingItem = item;
			}
		}
		return missingItem;
	}
	
	/**
	 * This method compare this itemset with another itemset to see if they are equal.
	 * The method assume that the two itemsets are lexically ordered.
	 * @return  true or false
	 */
	public boolean allTheSame(Itemset itemset2) {
		if(itemset2.size() != items.size()){
			return false;
		}
		for(int i=0; i< itemset2.size(); i++){
			if(!itemset2.getItems().get(i).equals(getItems().get(i))){
				return false;
			}
		}
		return true;
	}


	public Set<Integer> getTransactionsIds() {
		return transactionsIds;
	}
}
