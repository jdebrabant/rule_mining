package ca.pfv.spmf.frequentpatterns.fpgrowth;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents an itemset (a set of items)
 * @author Philippe Fournier-Viger 
 */
public class Itemset{
	private List<Integer> items = new ArrayList<Integer>(); // ordered
	private int transactioncount = 0;
	
	public Itemset(){
	}

	public double getRelativeSupport(int nbObject) {
		return ((double)transactioncount) / ((double) nbObject);
	}
	
	public String getSupportRelatifFormatted(int nbObject) {
		double frequence = ((double)transactioncount) / ((double) nbObject);
		DecimalFormat format = new DecimalFormat();
		format.setMinimumFractionDigits(0); 
		format.setMaximumFractionDigits(2); 
		return format.format(frequence);
	}
	
	public int getAbsoluteSupport(){
		return transactioncount;
	}

	// APRIORI
	public void increaseTransactionCount() { 
		transactioncount++;
	}
	
	public boolean includedIn(Itemset itemset2) {
		return itemset2.items.containsAll(items);
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
		return items.contains(item);
	}
	
//	public boolean containsAll(ItemsetApriori itemset) {
//		for(ItemApriori item : itemset.getItems()){
//			if(!contains(item)){
//				return false;
//			}
//		}
//		return true;
//	}
	
	public Itemset cloneItemset(){
		Itemset itemset = new Itemset();
		for(Integer item : items){
			itemset.addItem(item);
		}
		return itemset;
	}

	public void setTransactioncount(int transactioncount) {
		this.transactioncount = transactioncount;
	}

	public void addItem(Integer item) {
		items.add(item);
		
	}

	public int size() {
		return items.size();
	}

	//------------- The following methods are not necessary for FP-GROWTH. But
	//-------------- they are used for association rule mining. ---------------------
	//-------------------------------------------------------------------------------
	
	public Itemset cloneItemSetMinusAnItemset(Itemset itemsetToNotKeep){
		Itemset itemset = new Itemset();
		for(Integer item : items){
			if(!itemsetToNotKeep.contains(item)){
				itemset.addItem(item);
			}
		}
		return itemset;
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
			if(itemset2.getItems().get(i).equals(getItems().get(i)) == false){
				return false;
			}
		}
		return true;
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
			// if they are not the last items, they  should be different
			else if(items.get(i).equals(itemset2.get(i)) == false){ 
				return null; 
			}
		}
		return itemset2.get(itemset2.size()-1);
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
	
	public boolean isEqualTo(Itemset itemset2){
		if(items.size() != itemset2.items.size()){
			return false;
		}
		for(Integer val : items){
			if(!itemset2.contains(val)){
				return false;
			}
		}
		return true;
	}
	
	
}
