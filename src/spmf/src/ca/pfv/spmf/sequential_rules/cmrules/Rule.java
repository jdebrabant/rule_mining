package ca.pfv.spmf.sequential_rules.cmrules;

/**
 * This class is for representing a sequential rule.
 * 
 * @author Philippe Fournier-Viger, 2009
 */
public class Rule {
	private Itemset itemset1; // antecedent

	private Itemset itemset2; // consequent
	private int transactionCount; // absolute support
	private double confidence;
	int sequentialTransactionCount;
	
	public Rule(Itemset itemset1, Itemset itemset2, int transactionCount, double confidence){
		this.itemset1 = itemset1;
		this.itemset2 = itemset2;
		this.transactionCount =  transactionCount;
		this.confidence = confidence;
	}
	
	public Rule(Rule rule	) {
		itemset1 = rule.getItemset1();
		itemset2 = rule.getItemset2();
		confidence = rule.getConfidence();
		this.transactionCount =  rule.getAbsoluteSupport();
	}

	public Itemset getItemset1() {
		return itemset1;
	}

	public Itemset getItemset2() {
		return itemset2;
	}
	
	public double getCausality() {
		return ((double)sequentialTransactionCount) / ((double) transactionCount);
	}
	
	public double getRelativeSupport(int objectCount) {
		return ((double)transactionCount) / ((double) objectCount);
	}
	
	public int getAbsoluteSupport(){
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
	
	public int getAbsoluteSeqSupport() {
		return sequentialTransactionCount;
	}

	public double getSequentialSupport(int objectCount) {
		return ((double)sequentialTransactionCount) / ((double) objectCount);
	}

	public double getSequentialConfidence() {
		return ((double)sequentialTransactionCount) / ((double) itemset1.getAbsoluteSupport());
	}

	public void incrementTransactionCount() {
		transactionCount++;
	}

}
