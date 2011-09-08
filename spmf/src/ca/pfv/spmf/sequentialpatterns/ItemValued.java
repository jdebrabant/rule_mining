package ca.pfv.spmf.sequentialpatterns;

import ca.pfv.spmf.clustering.kmeans_for_seq_pattern_mining.Cluster;

/**
 * This class is an Item that can have an integer value.
 */
public class ItemValued extends Item{
	private double value;
	private int sequenceID =-1; // used for clustering to recognize items that are from sequences.
	
	private double min;
	private double max;

	private Cluster cluster = null; // used for clustering
	
	public ItemValued(int id){
		this(id, 0);
	}
	
	public ItemValued(int id, double value){
		super(id);
		this.value = value;
		this.min = value;
		this.max = value;
	}
	
	// pour clustering
	public ItemValued(int id, double value, double min, double max){
		super(id);
		this.value = value;
		this.min = min;
		this.max = max;
	}
	
	// Constructeur pour tests dans clustering
	public ItemValued(int id, double value, int sequenceID){
		super(id);
		this.value = value;
		min = value;
		max = value;
		this.sequenceID = sequenceID;
	}

	public double getValue() {
		return value;
	}

	public String toString(){
		StringBuffer temp = new StringBuffer();
		temp.append(getId());
		temp.append(" (");
		temp.append(getValue());
		if(min !=0 && max !=0){
			temp.append(", min=");
			temp.append(getMin());
			temp.append(" max=" );
			temp.append(getMax());
		}
		temp.append(')');
		if(getCluster() != null){
			temp.append('[');
			temp.append(getCluster().getMedian());
			temp.append(']');
		}
		return temp.toString();
	}
	
//	public int hashCode()
//	{
//		String string = getId() + " " + getValeur(); // ON POURRAIT AMÉLIORER!
//		return string.hashCode();
//	}

	
	//------------ Pour clustering ----//
	public int getSequenceID() {
		return sequenceID;
	}

	public void setSequenceID(int sequenceID) {
		this.sequenceID = sequenceID;
	}
	
	public void setValue(double value) {
		this.value = value;
	}

	public Cluster getCluster() {
		return cluster;
	}

	public void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}

	public double getMin() {
		return min;
	}

	public double getMax() {
		return max;
	}

	public void setMin(double min) {
		this.min = min;
	}

	public void setMax(double max) {
		this.max = max;
	}
	
}
