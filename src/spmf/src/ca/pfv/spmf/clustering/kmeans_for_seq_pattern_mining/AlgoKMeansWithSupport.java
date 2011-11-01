package ca.pfv.spmf.clustering.kmeans_for_seq_pattern_mining;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.pfv.spmf.sequentialpatterns.ItemValued;

/**
 * Second implementation of the K-Means algorithm
 * This algorithm adds to K-Means the notion of minimum support.
 * 
 * We  specify a maximum K.
 * The algorithmm execute K-Means from K=1 to K=Kmax and try to find
 * the higher number of clusters with a size that is higher than the minimum support.
 * The algorithm returns this set of clusters.
 * The algorithm stops at k=k+1 or when the number of clusters does not increase for
 * to succesives K.
 * 
 * @author Philippe Fournier-Viger 
 */

public class AlgoKMeansWithSupport extends AbstractAlgoClustering{
	
	private int maxK;
	private int minsuppRelative;
	private final int numberOfTriesForEachK;
	private final AlgoKMeans algoKMeans;

	// constructor with  minimum absolute support
	public AlgoKMeansWithSupport(int maxK, int minsuppRelatif, AlgoKMeans algoKMeans, int numberOfTriesForEachK){
		this.maxK = maxK;
		this.minsuppRelative = minsuppRelatif;
		this.algoKMeans = algoKMeans;
		this.numberOfTriesForEachK = numberOfTriesForEachK;
		
		if(minsuppRelative <= 0){
			minsuppRelative = 1;
		}
	}
	
	// constructor with  minimum relative support
	public AlgoKMeansWithSupport(int maxK, double minsupp, int transactioncount,  AlgoKMeans algoKMeans, int numberOfTriesForEachK){
		this.maxK = maxK;
		this.minsuppRelative = (int) Math.ceil(minsupp * transactioncount);
		this.algoKMeans = algoKMeans;
		this.numberOfTriesForEachK = numberOfTriesForEachK;
		
		if(minsuppRelative <= 0){
			minsuppRelative = 1;
		}
	}
	
	/**
	 * 
	 * @param items  the values to be clustered
	 * @param minsupp: the minimum absolute support for a cluster
	 * @return a list of clusters found.
	 */
	public List<Cluster> runAlgorithm(List<ItemValued> items){
		if(maxK > items.size()){ // protection against bad parameters.
			maxK = items.size();
		}
		
		
		int nbClustersFound = -1; 
		List<Cluster> clustersFound = null;
		
		// For each K.
		for(int k=1; k <= maxK; k++){
			// we try numberOfTriesForEachK times.
			for(int j=0; j<numberOfTriesForEachK; j++){
				
				// We execute K-Means
				algoKMeans.setK(k);
				List<Cluster> clusters = algoKMeans.runAlgorithm(items);
				
				// We count the numbers of clusters with size >= minsupp
				// and we remove clusters with size < minsupp
				int nbclusterFrequents = 0;
				for(int i=0; i< clusters.size();){
					if(isAFrequentCluster(clusters.get(i))){
						nbclusterFrequents++;
						i++;
					}else{
						clusters.remove(i);  
					}
				}
				// If the number of clusters is higher than
				// the one that has been found up to now, we keep the clusters.
				if(nbclusterFrequents > nbClustersFound){
					nbClustersFound = nbclusterFrequents;
					clustersFound = clusters;
				}
			}
		}
		
		// New step.
		// We associate the items to their respective clusters because we called
		// K-Means many times with different K and it is possible
		// that items are not associated to the last set of clusters that was found.
		for(ItemValued item : items){
			for(Cluster cluster : clustersFound){
				for(ItemValued item2 : cluster.getItems()){
					if(item == item2){
						item.setCluster(cluster);
					}
				}
			}
		}
	
		// We return the result.
		return clustersFound;
	}

	/**
	 * Check if the support of a cluster is higher than minsupp.
	 * To do this, we should not count two times items that have the same SequenceID.
	 * @param cluster
	 * @return
	 */
	private boolean isAFrequentCluster(Cluster cluster) {
		Set<Integer> sequenceIds = new HashSet<Integer>();
		for(ItemValued item : cluster.getItems()){
			sequenceIds.add(item.getSequenceID());
		}
		return sequenceIds.size() >= minsuppRelative;
	}

	public int getMaxK() {
		return maxK;
	}

	public int getMinsuppRelative() {
		return minsuppRelative;
	}

	public int getNumberOfTriesForEachK() {
		return numberOfTriesForEachK;
	}
}
