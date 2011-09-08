package ca.pfv.spmf.frequentpatterns.eclat_and_charm;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;

/**
 * This class represents an itemset (a set of items)
 * 
 * @author Philippe Fournier-Viger, 2008-2009
 */
public class Itemset {
	private final Set<Integer> items = new HashSet<Integer>(); // ordered
	private Set<Integer> tidset = new HashSet<Integer>();
	boolean maximal = true;

	public Itemset() {
	}

	public double getRelativeSupport(int nbObject) {
		return ((double) tidset.size()) / ((double) nbObject);
	}

	public String getSupportRelatifFormatted(int nbObject) {
		double frequence = getRelativeSupport(nbObject);
		DecimalFormat format = new DecimalFormat();
		format.setMinimumFractionDigits(0);
		format.setMaximumFractionDigits(2);
		return format.format(frequence);
	}

	public int getAbsoluteSupport() {
		return tidset.size();
	}

	// APRIORI
	public void increaseTransactionCount(int transactionId) {
		tidset.add(transactionId);
	}

	public void addItem(Integer value) {
		items.add(value);
	}

	public Set<Integer> getItems() {
		return items;
	}

	public void print() {
		System.out.print(toString());
	}

	public String toString() {
		StringBuffer r = new StringBuffer();
		for (Integer attribute : items) {

			r.append(attribute.toString());
			
			r.append(' ');
		}
		if (maximal) {
			r.append(" M ");
		}
		// r.append("[ <transactionIDs: " );
		// for(Integer id : transactionsIds){
		// r.append(" " + id );
		// }
		// r.append("]  ");
		return r.toString();
	}

	public boolean isEqualTo(Itemset itemset2) {
		if (items.size() != itemset2.items.size()) {
			return false;
		}
		return items.containsAll(itemset2.items);
	}

	public void setTransactioncount(Set<Integer> listTransactionIds) {
		this.tidset = listTransactionIds;
	}

	public int size() {
		return items.size();
	}

	public Itemset union(Itemset itemset) {

		Itemset union = new Itemset();
		union.getItems().addAll(items);
		union.getItems().addAll(itemset.getItems());

		return union;
	}

	public Set<Integer> getTransactionsIds() {
		return tidset;
	}
}
