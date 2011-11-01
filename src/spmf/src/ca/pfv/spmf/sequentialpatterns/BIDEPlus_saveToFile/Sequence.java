package ca.pfv.spmf.sequentialpatterns.BIDEPlus_saveToFile;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Implementation of a sequence.
 * A sequence is a list of itemsets.
 * @author Philippe Fournier-Viger 
 **/
public class Sequence{
	
	private final List<Itemset> itemsets = new ArrayList<Itemset>();
	private int id; // id de la sequence
	
	// List of IDS of all patterns that contains this one.
	private Set<Integer> sequencesID = null;
	
	public Sequence(int id){
		this.id = id;
	}
	
	public String getRelativeSupportFormated(int transactionCount) {
		double frequence = ((double)sequencesID.size()) / ((double) transactionCount);
		// pretty formating :
		DecimalFormat format = new DecimalFormat();
		format.setMinimumFractionDigits(0); 
		format.setMaximumFractionDigits(2); 
		return format.format(frequence);
	}
	
	public int getAbsoluteSupport(){
		return sequencesID.size();
	}

	public void addItemset(Itemset itemset) {
		itemsets.add(itemset);
	}
	
	public Sequence cloneSequence(){
		Sequence sequence = new Sequence(getId());
		for(Itemset itemset : itemsets){
			sequence.addItemset(itemset.cloneItemSet());
		}
		return sequence;
	}

	public void print() {
		System.out.print(toString());
	}
	
	public String toString() {
		StringBuffer r = new StringBuffer("");
		for(Itemset itemset : itemsets){
			r.append("{");
			for(Integer item : itemset.getItems()){
				String string = item.toString();
				r.append(string);
				r.append(' ');
			}
			r.append('}');
		}

		//  print the list of Pattern IDs that contains this pattern.
		if(getSequencesID() != null){
			r.append("  Sequence ID: ");
			for(Integer id : getSequencesID()){
				r.append(id);
				r.append(' ');
			}
		}
		return r.append("    ").toString();
	}
	
	public String itemsetsToString() {
		StringBuffer r = new StringBuffer("");
		for(Itemset itemset : itemsets){
			for(Integer item : itemset.getItems()){
				String string = item.toString();
				r.append(string);
				r.append(' ');
			}
			r.append('}');
		}
		return r.append("    ").toString();
	}
	
	public int getId() {
		return id;
	}

	public List<Itemset> getItemsets() {
		return itemsets;
	}
	
	public Itemset get(int index) {
		return itemsets.get(index);
	}
	
	// new : nov. 2009.
	public Integer getIthItem(int i) { 
		for(int j=0; j< itemsets.size(); j++){
			if(i < itemsets.get(j).size()){
				return itemsets.get(j).get(i);
			}
			i = i- itemsets.get(j).size();
		}
		return null;
	}
	
	public int size(){
		return itemsets.size();
	}

	public Set<Integer> getSequencesID() {
		return sequencesID;
	}

	public void setSequencesID(Set<Integer> sequencesID) {
		this.sequencesID = sequencesID;
	}
	
	/**
	 * Return the sum of the size of all itemsets of this sequence.
	 */
	public int getItemOccurencesTotalCount(){
		int count =0;
		for(Itemset itemset : itemsets){
			count += itemset.size();
		}
		return count;
	}
	
	
	/**
	 * Return 1 if this sequence STRICTLY contains sequence  
	 * Return 2 if this sequence is exactly the same as sequence2  
	 * Return 0 if this sequence does not contains sequence2.
	 * @param sequence2
	 * @return
	 */
	public int strictlyContains(Sequence sequence2) {
		int retour = strictlyContainsHelper(sequence2, 0, 0);
		if(retour ==2){
			return (size() == sequence2.size()) ? 2 : 1;
		}
		return retour;
	}
	
	public int strictlyContainsHelper(Sequence sequence2, int index, int index2) {
		if(index == size()){
			return 0;
		}
		
		if(size() - index < sequence2.size() - index2){
			return 0;
		}
		
		int retour = 0;
		for(int i=index; i <size(); i++){
			
			if(get(i).getItems().containsAll(sequence2.get(index2).getItems())){
				boolean sameSize = get(i).getItems().size() == sequence2.get(index2).size();

				// We have found the last one 
				if(sequence2.size()-1 == index2){ 
					if(sameSize){ // Strictly contains
						return 2;
					}
					retour = 1;
				}
				else{
					int resultat = strictlyContainsHelper(sequence2, i+1, index2+1);
					if(resultat == 2 && sameSize){
						return 2;
					}else if (resultat != 0){
						retour = 1;
					}
				}
			}
		} 
		return retour;
	}

	public Sequence cloneSequenceMinusItems(Map<Integer, Set<Integer>> mapSequenceID, double relativeMinSup) {
		Sequence sequence = new Sequence(getId());
		for(Itemset itemset : itemsets){
			Itemset newItemset = itemset.cloneItemSetMinusItems(mapSequenceID, relativeMinSup);
			if(newItemset.size() !=0){ 
				sequence.addItemset(newItemset);
			}
		}
		return sequence;
	}

	public void setID(int id2) {
		id = id2;
	}

}
