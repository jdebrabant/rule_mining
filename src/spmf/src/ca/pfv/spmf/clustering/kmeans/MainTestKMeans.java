package ca.pfv.spmf.clustering.kmeans;

import java.util.ArrayList;
import java.util.List;



public class MainTestKMeans {
	
	public static void main(String []args){
		// We want to cluster a set of integers
		List<Item> input = new ArrayList<Item>();
		input.add(new Item(2));
		input.add(new Item(2));
		input.add(new Item(3));
		input.add(new Item(7));
		input.add(new Item(7));
		input.add(new Item(7));
		input.add(new Item(7));
		input.add(new Item(3));
		
		System.out.println("k-means, PARAM = number of clusters");
		// Apply the algorithm
		AlgoKMeans algoKMeans = new AlgoKMeans(3);  // we request 3 clusters
		List<Cluster> clusters2 = algoKMeans.runAlgorithm(input);
		// Print the results
		for(Cluster cluster : clusters2){
			System.out.println(cluster.toString());
		}

	}
	
	
}
