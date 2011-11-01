package ca.pfv.spmf.sequential_rules.cmdeogun;

/**
 * This class is for representing a sequential rule.
 * 
 * @author Philippe Fournier-Viger, 2009
 */
public class Rule {
	
	private Itemset itemset1; // antecedent
	private Itemset itemset2; // consequent
	private int transactioncount; // absolute support
	
	public Rule(Itemset itemset1, Itemset itemset2){
		this.itemset1 = itemset1;
		this.itemset2 = itemset2;
	}

	public Itemset getItemset1() {
		return itemset1;
	}

	public Itemset getItemset2() {
		return itemset2;
	}
	
	public double getRelativeSupport(int sequencecount) {
		return ((double)transactioncount) / ((double) sequencecount);
	}
	
	public int getSupportAbsolu(){
		return transactioncount;
	}

	public double getConfidence() {
		return ((double)transactioncount) / ((double) itemset1.getAbsoluteSupport());
	}
	
	public void print(){
		System.out.println(toString());
	}
	
	public String toString(){
		return itemset1.toString() +  " ==> " + itemset2.toString();
	}

	public void incrementTransactionCount() {
		this.transactioncount++;
	}

	public int getTransactionCount() {
		return transactioncount;
	}

	public void setTransactioncount(int transactioncount) {
		this.transactioncount = transactioncount;
	}
}
