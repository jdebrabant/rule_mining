package ca.pfv.spmf.sequential_rules.cmrules;
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
	private Set<Integer> transactionsIds = new HashSet<Integer>();
	
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

	// APRIORI
	public void increaseTransactionCount(int transactionId) { 
		transactionsIds.add(transactionId);
	}
	
	public boolean includedIn(Itemset itemset2) {
		return itemset2.items.containsAll(items);
	}
	
	public void addItem(Integer value){
			items.add(value);
	}
	
	public void addItemOrderedWithNoDuplicate(Integer value){
		for(int i=0; i< items.size(); i++){
			if(value.equals(items.get(i)) ){
				return; // already there!
			}
			if(value < items.get(i)){
				if(i ==0){
					items.add(0,value);
				}else{
					items.add(i,value);
				}
				return;
			}
		}
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
			
//			String s;
//			if(attribute == 1){
//				r.append("a");
//			}else if(attribute == 2){
//				r.append("b");
//			}else if(attribute == 3){
//				r.append("c");
//			}else if(attribute == 4){
//				r.append("d");
//			}else if(attribute == 5){
//				r.append("e");
//			}else if(attribute == 6){
//				r.append("f");
//			}else if(attribute == 7){
//				r.append("g");
//			}
//			
			
			r.append(attribute.toString());
			r.append(' ');
		}
//		r.append("[ <transactionIDs: " );
//		for(Integer id : transactionsIds){
//			r.append(" " + id );
//		}
//		r.append("]  ");
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

	
	public boolean isLexicallySmallerthan(Itemset itemset2){
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
	
	
	public boolean isEqualTo(Itemset itemset2){
		if(items.size() != itemset2.items.size()){
			return false;
		}
		return items.containsAll(itemset2.items);
	}


	public void setTransactioncount(Set<Integer> listTransactionIds) {
		this.transactionsIds = listTransactionIds;
	}

	// pour Apriori
	public Itemset cloneItemSetMinusOneItem(Integer itemsetToRemove){
		Itemset itemset = new Itemset();
		for(Integer item : items){
			if(!item.equals(itemsetToRemove)){
				itemset.addItem(item);
			}
		}
		return itemset;
	}
	
	public Itemset cloneItemSetMinusAnItemset(Itemset itemsetToNotKeep){
		Itemset itemset = new Itemset();
		for(Integer item : items){
			if(!itemsetToNotKeep.contains(item)){
				itemset.addItem(item);
			}
		}
		return itemset;
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
