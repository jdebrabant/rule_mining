package ca.pfv.spmf.clustering.kmeans_for_seq_pattern_mining;

import java.util.List;

import ca.pfv.spmf.sequentialpatterns.ItemValued;

public abstract class AbstractAlgoClustering {
	
	public abstract List<Cluster> runAlgorithm(List<ItemValued> items);

}
