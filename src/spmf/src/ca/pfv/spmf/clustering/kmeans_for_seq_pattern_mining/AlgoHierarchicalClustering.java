package ca.pfv.spmf.clustering.kmeans_for_seq_pattern_mining;

import java.util.ArrayList;
import java.util.List;

import ca.pfv.spmf.sequentialpatterns.ItemValued;


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

public class AlgoHierarchicalClustering extends AbstractAlgoClustering{
	
	private final double threshold;

	public AlgoHierarchicalClustering(double threshold){
		this.threshold = threshold;
	}
	
	public List<Cluster> runAlgorithm(List<ItemValued> items){
		List<Cluster> clusters = new ArrayList<Cluster>();
		
		// (1) Initialize all items as clusters
		for(ItemValued item : items){
			Cluster cluster = new Cluster(item);
			clusters.add(cluster);
		}
		
		// (2) Loop to combine the two closest clusters into a bigger cluster
		//     until no clusters can be combined.
		boolean wasAMerge = false;
		do{
			wasAMerge = mergeTheClosestCluster(clusters, threshold);
		}while(wasAMerge);
		
		// Calculate min and max 
		for(Cluster cluster : clusters){
			cluster.computeHigherAndLower();
		}
		
		return clusters;
	}

	private boolean mergeTheClosestCluster(List<Cluster> clusters, double threshold) {
		Cluster clusterToMerge1 = null;
		Cluster clusterToMerge2 = null;
		double minClusterDistance = Integer.MAX_VALUE;
		
		// find the two closest cluster with distance > threshold
		for(int i=0; i< clusters.size(); i++){
			for(int j= i+1; j< clusters.size(); j++){
				double distance = medianDistance(clusters.get(i), clusters.get(j));
				if(distance < minClusterDistance && distance < threshold){
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
