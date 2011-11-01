package ca.pfv.spmf.associationrules.IGB;

import ca.pfv.spmf.frequentpatterns.zart.ItemsetZart;

/**
 * This class is for representing an association rule that has been
 * found by IGB. An association rule has an antecedent, a consequent,
 * a support and a confidence.
 * 
 * @author Philippe Fournier-Viger, 2008
 */
public class RuleIGB {
	private ItemsetZart itemset1; // antecedent
	private ItemsetZart itemset2; // consequent
	private int transactionCount; // absolute support
	private double confidence;
	
	public RuleIGB(ItemsetZart itemset1, ItemsetZart itemset2, int transactionCount, double confidence){
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

	public ItemsetZart getItemset1() {
		return itemset1;
	}

	public ItemsetZart getItemset2() {
		return itemset2;
	}


}
