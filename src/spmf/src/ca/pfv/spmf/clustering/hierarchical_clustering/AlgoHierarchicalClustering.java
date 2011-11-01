package ca.pfv.spmf.clustering.hierarchical_clustering;

import java.util.ArrayList;
import java.util.List;


/**
 * Based on the description of Hierarchical Clustering Algorithms from
 * http://home.dei.polimi.it/matteucc/Clustering/tutorial_html/hierarchical.html
 * 
 * This is a Hierarchical Clustering with a constant "threshold" that indicate
 * the maximal distance between two clusters to group them.
 * 
 * The distance between two clusters is calculated as the distance between the medians
 * of the two clusters. 
 * 
 * @author Philippe Fournier-Viger 
 */

public class AlgoHierarchicalClustering{
	
	private final double maxDistance;

	public AlgoHierarchicalClustering(double maxDistance){
		this.maxDistance = maxDistance;
	}
	
	public List<Cluster> runAlgorithm(List<Item> items){
		List<Cluster> clusters = new ArrayList<Cluster>();
		
		// (1) Initialize all items as clusters
		for(Item item : items){
			Cluster cluster = new Cluster(item);
			clusters.add(cluster);
		}
		
		// (2) Loop to combine the two closest clusters into a bigger cluster
		//     until no clusters can be combined.
		boolean wasAMerge = false;
		do{
			wasAMerge = mergeTheClosestCluster(clusters);
		}while(wasAMerge);
		
		// Calculate min and max 
		for(Cluster cluster : clusters){
			cluster.computeHigherAndLower();
		}
		
		return clusters;
	}

	private boolean mergeTheClosestCluster(List<Cluster> clusters) {
		Cluster clusterToMerge1 = null;
		Cluster clusterToMerge2 = null;
		double minClusterDistance = Integer.MAX_VALUE;
		
		// find the two closest cluster with distance > threshold
		for(int i=0; i< clusters.size(); i++){
			for(int j= i+1; j< clusters.size(); j++){
				double distance = medianDistance(clusters.get(i), clusters.get(j));
				if(distance < minClusterDistance && distance < maxDistance){
					minClusterDistance = distance;
					clusterToMerge1 = clusters.get(i);
					clusterToMerge2 = clusters.get(j);
				}
			}
		}	
		
		// if no close clusters were found, return false
		if(clusterToMerge1 == null){
			return false;
		}
		
		// else, merge the two closest clusters
		clusterToMerge1.addItemsFromCluster(clusterToMerge2);
		clusterToMerge1.recomputeClusterMedian();
		clusters.remove(clusterToMerge2);
		
		return true;
	}
	
	private double medianDistance(Cluster cluster1, Cluster cluster2){
		return Math.abs(cluster1.getMedian() - cluster2.getMedian());
	}
	
}
