package ca.pfv.spmf.clustering.hierarchical_clustering;

import java.util.ArrayList;
import java.util.List;

public class MainTestHierarchicalClustering {
	
	public static void main(String []args){
		// Array list for the input data
		List<Item> input = new ArrayList<Item>();
		input.add(new Item(2));  
		input.add(new Item(2));
		input.add(new Item(3));
		input.add(new Item(7));
		input.add(new Item(7));
		input.add(new Item(8));
		input.add(new Item(9));
		input.add(new Item(3));
		
		System.out.println("hierarchical with median distance, PARAM = max distance between clusters");
		// run the algorithm
		AlgoHierarchicalClustering algoH = new AlgoHierarchicalClustering(2.0);
		List<Cluster> clusters = algoH.runAlgorithm(input);
		// print the results
		for(Cluster cluster : clusters){
			System.out.println(cluster.toString());
		}

	}
	
	
}
