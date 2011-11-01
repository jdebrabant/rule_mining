package ca.pfv.spmf.sequentialpatterns;


public class Item{
	
	private final int id;
	
	public Item(int id){
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public String toString(){
		return "" + getId();
	}
	
	public boolean equals(Object object){
		Item item = (Item) object;
		if((item.getId() == this.getId())){
			return true;
		}
		return false;
	}
	
	public int hashCode()
	{
		String string = ""+getId(); // This could be improved.
		return string.hashCode();
	}
	
}
