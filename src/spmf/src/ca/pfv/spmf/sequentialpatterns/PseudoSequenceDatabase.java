package ca.pfv.spmf.sequentialpatterns;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.pfv.spmf.clustering.kmeans_for_seq_pattern_mining.Cluster;

/**
 * This class represents a projected database.
 * A projected database is a list of pseudoSequences (projected sequences).
 */
public class PseudoSequenceDatabase {
	
	private List<PseudoSequence> pseudoSequences = new ArrayList<PseudoSequence>();
	
	// for clustering, the last item that was used to do the projection that results in this database.
	private Cluster cluster = null;

	
	public List<PseudoSequence> getPseudoSequences(){
		return pseudoSequences;
	}
	
	//----
	
	public void printContext(){
		System.out.println(toString());
	}
	
	public String toString(){
		StringBuffer r = new StringBuffer("============  CONTEXTE ==========");
		for(PseudoSequence sequence : pseudoSequences){ // pour chaque objet
			r.append(sequence.getId());
			r.append(":  ");
			r.append(sequence.toString());
			r.append('\n');
		}
		return r.toString();
	}
	
	public int size(){
		return pseudoSequences.size();
	}

	public Set<Integer> getSequenceIDs() {
		Set<Integer> ensemble = new HashSet<Integer>();
		for(PseudoSequence sequence : getPseudoSequences()){
			ensemble.add(sequence.getId());
		}
		return ensemble;
	}
	
	
	//--------------- For clustering
	public Cluster getCluster() {
		return cluster;
	}

	public void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}

	public void addSequence(PseudoSequence newSequence) {
		pseudoSequences.add(newSequence);
		
	}
}
