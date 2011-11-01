package ca.pfv.spmf.frequentpatterns.uapriori;

/**
 * This class represents an item from a binary context or itemset.
 * @author philippe
 */
public class ItemApriori{
	
	private final int id;
	private final double probability;
	
	public ItemApriori(int id, double probability){
		this.id = id;
		this.probability = probability;
	}

	public int getId() {
		return id;
	}


	public String toString(){
		return ""+getId() + " (" + probability + ")";
	}
	
	public boolean equals(Object object){
		ItemApriori item = (ItemApriori) object;
		if((item.getId() == this.getId())){
			return true;
		}
		return false;
	}
	
	public int hashCode()
	{
		String string = "" +getId();
		return string.hashCode();
	}

	public double getProbability() {
		return probability;
	}
}
