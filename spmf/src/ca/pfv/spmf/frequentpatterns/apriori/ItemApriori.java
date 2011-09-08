package ca.pfv.spmf.frequentpatterns.apriori;

/**
 * This class represents an item from a binary context or itemset.
 * @author philippe
 */
public class ItemApriori{
	
	private final int id;
	
	public ItemApriori(int id){
		this.id = id;
	}

	public int getId() {
		return id;
	}


	public String toString(){
		return ""+getId();
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
}
