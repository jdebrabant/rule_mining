package ca.pfv.spmf.frequentpatterns.zart;

// An item
public class ItemZart{
	private final int id;
	
	public ItemZart(int id){
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public String toString(){
		return ""+getId();
	}
	
	public boolean equals(Object object){
		ItemZart item = (ItemZart) object;
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
