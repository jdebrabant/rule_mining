package ca.pfv.spmf.clustering.hierarchical_clustering;

import java.util.ArrayList;
import java.util.List;

public class Cluster {
	private List<Item> items;
	private double median;
	private double higher =0;
	private double lower = Double.MAX_VALUE;
	
	private double sum = 0; // FOR MEAN
	
	//Constructor of empty cluster with a specified median (for  K-means)
	public Cluster(double median){
		this.items = new ArrayList<Item>();
		this.median = median;
	}
	
	public Cluster(List<Item> newItems){
		this.items = new ArrayList<Item>(newItems);
		calculateMedian();
	}
	
	public Cluster(List<Item> newItems, List<Item> newItems2){
		this.items = new ArrayList<Item>(newItems);
		items.addAll(newItems2);
		calculateMedian();
	}
	
	public Cluster(Item item){
		this.items = new ArrayList<Item>();
		this.items.add(item);
		// calculate median
		this.median = item.getValue();
	}
	
	
	// To perform merges
	public void addItemsFromCluster(Cluster cluster2){
		for(Item item : cluster2.getItems()){
			getItems().add(item);
			sum+= item.getValue();
		}
//		.addAll();
	}
	
	public void addItem(Item item) {
		getItems().add(item);
		sum += item.getValue();
	}
	
	public void addItems(List<Item> newItems) {
		for(Item item : newItems){
			this.getItems().add(item);
			sum += item.getValue();
		}
	}

	public List<Item> getItems() {
		return items;
	}
	
	public int size(){
		return getItems().size();
	}

	public double getMedian() {
		return median;
	}
	
	public String toString(){
		String retour = "(";
		for(Item item : getItems()){
			retour = retour + item.getValue() + " ";
		}
		retour = retour + ")      <" + median + ", min=" + getLower() + " max=" + getHigher() + ">";
		return retour;
	}
	
	
	private void calculateMedian() {
		if(getItems().isEmpty()){
			return;
		}
		
		if(getItems().size() ==1){
			median = getItems().get(0).getValue();
			return;
		}
		// VERSION USING THE AVERAGE
		median = sum /((double)items.size());

		
		// VERSION USING THE MEDIAN VALUE 
//		median = 0;
//		// sort the list
//		Collections.sort(getItems(), new Comparator<Item>(){
//			public int compare(Item obj1, Item obj2)
//            {
//                    return (int) (obj1.getValue() - obj2.getValue());
//            }
//		});
//		
//		// calculate median 
//		if((getItems().size() % 2) == 0){ // if even
//			int index1 = (getItems().size() / 2); // upper
//			int index2 = (getItems().size() / 2) - 1; // lower
//			double somme = getItems().get(index1).getValue() + getItems().get(index2).getValue();
//			median = somme /2;
//		}
//		else{ // if odd
//			int index1 = (int)Math.floor(items.size() / 2);
//			median = items.get(index1).getValue(); // middle
//		}
	}

	public void recomputeClusterMedian() {
		calculateMedian();
	}
	
	public void computeHigherAndLower(){
		for(Item item : getItems()){
			if(item.getValue() > higher){
				higher = item.getValue();
			}
			if(item.getValue() < lower){
				lower = item.getValue();
			}
		}
	}

	public boolean containsItem(Item item2) {
		for(Item item : getItems()){
			if(item == item2){
				return true;
			}
		}
		return false;
	}

	public double getHigher() {
		return higher;
	}

	public double getLower() {
		return lower;
	}


}
