package ca.pfv.spmf.associationrules.pasquier;

import ca.pfv.spmf.frequentpatterns.apriori.ItemsetApriori;

/**
 * Association rule
 * @author Philippe Fournier-Viger
 */
public class Rule {
	private ItemsetApriori itemset1;
	private ItemsetApriori itemset2;
	private int transactioncount;
	private double confidence;
	
	public Rule(ItemsetApriori itemset1, ItemsetApriori itemset2, int transactionCount, double confidence){
		this.itemset1 = itemset1;
		this.itemset2 = itemset2;
		this.transactioncount =  transactionCount;
		this.confidence = confidence;
	}
	
	public void print(){
		itemset1.print();
		System.out.print(" ==> ");
		itemset2.print();
	}

	public double getRelativeSupport(int objectsCount) {
		return ((double)transactioncount) / ((double) objectsCount);
	}
	
	public int getAbsoluteSupport(){
		return transactioncount;
	}

	public double getConfidence() {
		return confidence;
	}

	public boolean isLexicallySmallerthan(Rule regle) {
		return itemset1.isLexicallySmallerthan(regle.itemset1);
	}
}
