package ca.pfv.spmf.frequentpatterns.zart;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * An itemset (a set of items).
 * @author Philippe Fournier-Viger 
 */
public class ItemsetZart{
	private final List<ItemZart> items = new ArrayList<ItemZart>(); // ordonnée
	private int transactioncount = 0;
	
	public ItemsetZart(ItemZart item, long timestamp){
		addItem(item);
	}
	
	public ItemsetZart(){
	}
	
	public double getRelativeSupport(int nbObject) {
		return ((double)transactioncount) / ((double) nbObject);
	}
	
	public ItemsetZart cloneItemSetMinusAnItemset(ItemsetZart itemsetToNotKeep){
		ItemsetZart itemset = new ItemsetZart();
		for(ItemZart item : items){
			if(!itemsetToNotKeep.contains(item)){
				itemset.addItem(item);
			}
		}
		return itemset;
	}
	
	public String getRelativeSupportAsString(int nbObject) {
		double frequence = ((double)transactioncount) / ((double) nbObject);
		// We create a DecimalFormat to format double value:
		DecimalFormat format = new DecimalFormat();
		format.setMinimumFractionDigits(0); 
		format.setMaximumFractionDigits(2); 

		// We return double as a String value
		return format.format(frequence);
	}
	
	public int getAbsoluteSupport(){
		return transactioncount;
	}

	public void increaseTransactionCount() { 
		transactioncount++;
	}
	
	public boolean includedIn(ItemsetZart itemset2) {
		return itemset2.items.containsAll(items);
	}
	
	public void addItem(ItemZart value){
			items.add(value);
	}
	
	public List<ItemZart> getItems(){
		return items;
	}
	
	public ItemZart get(int index){
		return items.get(index);
	}
	
	
	public void print(){
		System.out.print(toString());
	}
	
	public String toString(){
		StringBuffer r = new StringBuffer ();
		for(ItemZart attribute : items){
			r.append(attribute.toString());
			r.append(' ');
		}
		return r.toString();
	}

	public boolean contains(ItemZart item) {
		return items.contains(item);
	}

	
	// Pour l'algorithme Apriori
	public ItemZart shareFirstIItemsOnly(ItemsetZart itemset2){
		// Pourrait être optimisé... en sachant que les listes sont ordonnées.
		boolean shareIminus1= true;
		// ils doivent avoir les premiers i-1 éléments en commun
		for(int j=0; j< items.size()-1; j++){ 
			if(!itemset2.get(j).equals(get(j))){
				shareIminus1 = false;
				break;
			}
		}
		if(!shareIminus1){
			return null;
		}
		if( itemset2.items.get(items.size()-1).equals(items.get(items.size()-1))){ // le dernier élément doit être différent!
			return null;
		}
		if( items.get(items.size()-1).getId() > itemset2.items.get(items.size()-1).getId()){
			return null;
		}
		return itemset2.get(items.size()-1);
	}
	
	public boolean isEqualTo(ItemsetZart itemset2){
		if(items.size() != itemset2.items.size()){
			return false;
		}
		for(ItemZart val : items){
			if(!itemset2.contains(val)){
				return false;
			}
		}
		return true;
	}
	
	// pour Apriori
	public ItemsetZart cloneItemSetMinusOneItem(ItemZart itemsetToRemove){
		ItemsetZart itemset = new ItemsetZart();
		for(ItemZart item : items){
			if(!item.equals(itemsetToRemove)){
				itemset.addItem(item);
			}
		}
		return itemset;
	}
	
	public boolean contains(ItemsetZart item) {
		return items.contains(item);
	}

	public void setTransactioncount(int transactioncount) {
		this.transactioncount = transactioncount;
	}
	
	public int size(){
		return items.size();
	}
}
