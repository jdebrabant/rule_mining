package ca.pfv.spmf.frequentpatterns.apriori;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents an itemset (a set of items)
 * @author Philippe Fournier-Viger 
 */
public class ItemsetApriori{
	private List<ItemApriori> items = new ArrayList<ItemApriori>(); // ordered
	private int transactioncount = 0;
	
	private boolean close = false;
	private boolean pseudoclose = false;
	private ItemsetApriori closure = null;
	
	public ItemsetApriori(){
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
	
	public boolean includedIn(ItemsetApriori itemset2) {
		return itemset2.items.containsAll(items);
	}
	
	public void addItem(ItemApriori value){
			items.add(value);
	}
	
	public void addItemOrderedWithNoDuplicate(ItemApriori value){
		for(int i=0; i< items.size(); i++){
			if(value.getId() == items.get(i).getId() ){
				return; // already there!
			}
			if(value.getId() < items.get(i).getId()){
				if(i ==0){
					items.add(0,value);
				}else{
					items.add(i,value);
				}
				return;
			}
		}
		addItem(value);
	}
	
	public List<ItemApriori> getItems(){
		return items;
	}
	
	public ItemApriori get(int index){
		return items.get(index);
	}
	
	public void print(){
		System.out.print(toString());
	}
	
	public String toString(){
		StringBuffer r = new StringBuffer ();
		for(ItemApriori attribute : items){
			r.append(attribute.toString());
			r.append(' ');
		}
		return r.toString();
	}

	public boolean contains(ItemApriori item) {
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
	
	public boolean isLexicallySmallerthan(ItemsetApriori itemset2){
		for(int i=0; i< items.size(); i++){
			if(items.get(i).getId() > itemset2.items.get(i).getId()){
				return false;
			}
			else if(items.get(i).getId() < itemset2.items.get(i).getId()){
				return true;
			}
		}
		return true;
	}
	
//	// Pour l'algorithme Apriori
//	public ItemApriori shareFirstIItemsOnly(ItemsetApriori itemset2){
//		// Pourrait être optimisé... en sachant que les listes sont ordonnées.
//		boolean shareIminus1= true;
//		// ils doivent avoir les premiers i-1 éléments en commun
//		for(int j=0; j< items.size()-1; j++){ 
//			if(!itemset2.get(j).equals(get(j))){
//				shareIminus1 = false;
//				break;
//			}
//		}
//		if(!shareIminus1){
//			return null;
//		}
//		if(itemset2.items.get(items.size()-1).equals(items.get(items.size()-1))){ // le dernier élément doit être différent!
//			return null;
//		}
//		if(items.get(items.size()-1).getId() > itemset2.items.get(items.size()-1).getId()){
//			return null;
//		}
//		return itemset2.get(items.size()-1);
//	}
	
//	// For Apriori
//	public ItemApriori haveOneItemDifferent(ItemsetApriori itemset2){
//		ItemApriori missingFromItemset2 = null;
//		for(ItemApriori item : items){
//			if(!itemset2.contains(item)){
//				if(missingFromItemset2 != null){
//					return null;  // more than one item is different
//				}else{
//					missingFromItemset2 = item;
//				}
//			}
//		}
//		return missingFromItemset2;
//	}
	
	public boolean isEqualTo(ItemsetApriori itemset2){
		if(items.size() != itemset2.items.size()){
			return false;
		}
		for(ItemApriori val : items){
			if(!itemset2.contains(val)){
				return false;
			}
		}
		return true;
	}


	public void setTransactioncount(int transactioncount) {
		this.transactioncount = transactioncount;
	}

	public boolean isClose() {
		return close;
	}

	public void setClose(boolean close) {
		this.close = close;
		this.closure = this;
	}

	public boolean isPseudoclose() {
		return pseudoclose;
	}

	public void setPseudoclose(boolean pseudoclose) {
		this.pseudoclose = pseudoclose;
	}

	public ItemsetApriori getClosure() {
		return closure;
	}

	public void setClosure(ItemsetApriori closure) {
		this.closure = closure;
	}

	// pour Apriori
	public ItemsetApriori cloneItemSetMinusOneItem(ItemApriori itemsetToRemove){
		ItemsetApriori itemset = new ItemsetApriori();
		for(ItemApriori item : items){
			if(!item.equals(itemsetToRemove)){
				itemset.addItem(item);
			}
		}
		return itemset;
	}
	
	public ItemsetApriori cloneItemSetMinusAnItemset(ItemsetApriori itemsetToNotKeep){
		ItemsetApriori itemset = new ItemsetApriori();
		for(ItemApriori item : items){
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
	public ItemApriori allTheSameExceptLastItem(ItemsetApriori itemset2) {
		if(itemset2.size() != items.size()){
			return null;
		}
		for(int i=0; i< items.size(); i++){
			// if they are the last items
			if(i == items.size()-1){ 
				// the one from items should be smaller (lexical order) and different than the one of itemset2
				if(items.get(i).getId() >= itemset2.get(i).getId()){  
					return null;
				}
			}
			// if they are not the last items, they  should be different
			else if(items.get(i).getId() != itemset2.get(i).getId()){ 
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
	public boolean allTheSame(ItemsetApriori itemset2) {
		if(itemset2.size() != items.size()){
			return false;
		}
		for(int i=0; i< itemset2.size(); i++){
			if(itemset2.getItems().get(i).getId() != getItems().get(i).getId()){
				return false;
			}
		}
		return true;
	}
	
	// used by the CloStream algorithm
	public ItemsetApriori intersection(ItemsetApriori itemset2){
		ItemsetApriori intersection = new ItemsetApriori();
		for(ItemApriori item : items){
			if(itemset2.contains(item)){
				intersection.addItem(item);
			}
		}
		return intersection;
	}
	
	/**
	 * for Eclat/charm
	 * Could be optimized if we used set instead of a list of items.
	 */
	public ItemsetApriori union(ItemsetApriori itemset) {
//		ItemsetApriori union = new ItemsetApriori(); 
//		for(ItemApriori item : items){
//			union.addItem(item);
//		}
//		union.addItem(itemset.get(itemset.size()-1));
//		return union;
		
		ItemsetApriori union = new ItemsetApriori(); 
		union.getItems().addAll(items);
		for(ItemApriori item : itemset.getItems()){
			if(items.contains(item) == false){
				union.addItem(item);
			}
		}
		
		return union;
	}

	public void setItems(List<ItemApriori> items) {
		this.items = items;
	}
	



}
