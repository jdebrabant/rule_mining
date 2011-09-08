package ca.pfv.spmf.associationrules.agrawal_Apriori_version;

import ca.pfv.spmf.frequentpatterns.apriori.ItemsetApriori;

/**
 * This class is for representing an association rule that has been
 * found by IGB. An association rule has an antecedent, a consequent,
 * a support and a confidence.
 * 
 * @author Philippe Fournier-Viger, 2008
 */
public class RuleAgrawal {
	private ItemsetApriori itemset1; // antecedent
	private ItemsetApriori itemset2; // consequent
	private int transactionCount; // absolute support
	private double confidence;
	
	public RuleAgrawal(ItemsetApriori itemset1, ItemsetApriori itemset2, int transactionCount, double confidence){
		this.itemset1 = itemset1;
		this.itemset2 = itemset2;
		this.transactionCount =  transactionCount;
		this.confidence = confidence;
	}
	
	public double getRelativeSupport(int objectCount) {
		return ((double)transactionCount) / ((double) objectCount);
	}
	
	public int getSupportAbsolu(){
		return transactionCount;
	}

	public double getConfidence() {
		return confidence;
	}
	
	public void print(){
		System.out.println(toString());
	}
	
	public String toString(){
		return itemset1.toString() +  " ==> " + itemset2.toString();
	}

	public ItemsetApriori getItemset1() {
		return itemset1;
	}

	public ItemsetApriori getItemset2() {
		return itemset2;
	}


}
