package ca.pfv.spmf.clustering.kmeans;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Modified version of the K-means algorithm
 * From Wikipedia : 
 * The K-means algorithm steps are (J. MacQueen, 1967):
 *  * Choose the number of clusters, k.
 *  * Randomly generate k clusters and determine the cluster centers, 
 *    or directly generate k random points as cluster centers.
 *  * Assign each point to the nearest cluster center.
 *  * Recompute the new cluster centers.
 *  * Repeat the two previous steps until some convergence criterion 
 *    is met (usually that the assignment hasn't changed).
 * 
 * @author Philippe Fournier-Viger, 2008
 */

public class AlgoKMeans{

	private int k;
	private final static Random random = new Random(System.currentTimeMillis());
	
	public AlgoKMeans(int k){
		this.k = k;
	}
	
	/**
	 * 
	 * @param input
	 * @param k :desired number of cluster
	 * @return
	 */
	public List<Cluster> runAlgorithm(List<Item> input){
		List<Cluster> clusters = new ArrayList<Cluster>();
		
		// Special case : only one item
		if(input.size() == 1){
			Item item = input.get(0);
			Cluster cluster = new Cluster(item);
			cluster.addItem(item);
			clusters.add(cluster);
			return clusters;
		}
		
		 //(1) Randomly generate k empty clusters with a random median (cluster center)
		
		// (1.1) Choose the higher and lower values for generating a median
		double higher = input.get(0).getValue();
		double lower = input.get(0).getValue();
		for(Item item : input){
			if(item.getValue() > higher){
				higher = item.getValue();
			}
			if(item.getValue() < lower){
				lower = item.getValue();
			}
		}
		
		// Special case : all items have the same values, so we return only one cluster.
		if(higher == lower){
			Cluster cluster = new Cluster(input);
			clusters.add(cluster);
			return clusters;
		}
		
		// (1.2) Generate the k empty clusters with random median
		for(int i=0; i< k; i++){
			// generate random median
			
			double median = random.nextInt((int)(higher-lower))+lower;
			// create the cluster
			Cluster cluster = new Cluster(median);
			clusters.add(cluster);
		}
		
		
		// (2) Repeat the two next steps until the assignment hasn't changed
		boolean changed;

		
		do{
			changed = false;
			 // (2.1) Assign each point to the nearest cluster center.
	
			/// for each item
			for(Item item : input){
				// find the nearest cluster and the cluster containing the item
				Cluster nearestCluster = null;
				Cluster containingCluster = null;
				double distanceToNearestCluster = Double.MAX_VALUE;
				
				for(Cluster cluster : clusters){
					double distance = medianDistance(cluster, item);
					if(distance < distanceToNearestCluster){
						nearestCluster = cluster;
						distanceToNearestCluster = distance;
					}
					if(cluster.containsItem(item)){
						containingCluster = cluster;
					}
				}
				
				if(containingCluster != nearestCluster){
					if(containingCluster != null){
						removeItem(containingCluster.getItems(), item);  
					}
					nearestCluster.addItem(item);
					changed = true;
				}
			}
			
			 // (2.2) Recompute the new cluster medians
			for(Cluster cluster : clusters){
				cluster.recomputeClusterMedian();
			}

		}while(changed);
		
		// Computer min and max for all clusters
		for(Cluster cluster : clusters){
			cluster.computeHigherAndLower();
		}
		
		
		
	
		return clusters;
	}
	
	private void removeItem(List<Item> items, Item item) {
		for(int i=0; i< items.size(); i++){
			if(items.get(i) ==  item){
				items.remove(i);
			}
		}
	}

	private double medianDistance(Cluster cluster1, Item item){
		return Math.abs(cluster1.getMedian() - item.getValue());
	}

	public void setK(int k) {
		this.k = k;
	}
}
