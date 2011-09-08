package ca.pfv.spmf.sequentialpatterns;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.pfv.spmf.clustering.kmeans_for_seq_pattern_mining.Cluster;

/**
 * Implementation of a sequence database.
 * Each sequence should have a unique id.
 * See examples in /test/ directory for the format of input files.
 * @author Philippe Fournier-Viger 
 **/
public class SequenceDatabase{

	// List of sequences
	private final List<Sequence> sequences = new ArrayList<Sequence>();
	
	// for clustering, the last item that was used to do the projection that results in this database.
	private Cluster cluster = null;
	
	public void loadFile(String path) throws IOException {
		String thisLine;
		BufferedReader myInput = null;
		try {
			FileInputStream fin = new FileInputStream(new File(path));
			myInput = new BufferedReader(new InputStreamReader(fin));
			while ((thisLine = myInput.readLine()) != null) {
				// si la ligne n'est pas un commentaire
				if(thisLine.charAt(0) != '#'){ 
					// ajoute une séquence
					addSequence(thisLine.split(" "));	
				}		
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(myInput != null){
				myInput.close();
			}
	    }
	}
	
	public void addSequence(String[] integers) {	//
		Sequence sequence = new Sequence(sequences.size());
		Itemset itemset = new Itemset();
		for(String integer:  integers){
			if(integer.codePointAt(0) == '<'){  // Timestamp
				String value = integer.substring(1, integer.length()-1);
				itemset.setTimestamp(Long.parseLong(value));
			}else if(integer.equals("-1")){ // indicate the end of an itemset
				sequence.addItemset(itemset);
				itemset = new Itemset();
			}else if(integer.equals("-2")){ // indicate the end of a sequence
				// check if the last "-1" was not included
				if(itemset.size() >0){
					sequence.addItemset(itemset);
					itemset = new Itemset();
				}
				sequences.add(sequence);
			}else{ // an item with the format : id(value)  ou:  id
				int indexLeftParenthesis = integer.indexOf("(");
				if(indexLeftParenthesis != -1){
					int indexRightParenthesis = integer.indexOf(")");
					int value = Integer.parseInt(integer.substring(indexLeftParenthesis+1, indexRightParenthesis));
					integer = integer.substring(0, indexLeftParenthesis);
					ItemValued item = new ItemValued(Integer.parseInt(integer), value);
					itemset.addItem(item);
				}else{
					// extract the value for an item
					Item item = new Item(Integer.parseInt(integer));
					if(!itemset.getItems().contains(item)){
						itemset.addItem(item);
					}
				}
				
			}
		}
	}
	
	public void addSequence(Sequence sequence){
		sequences.add(sequence);
	}
	
	public void print(){
		System.out.println("============  Context ==========");
		for(Sequence sequence : sequences){ // pour chaque objet
			System.out.print(sequence.getId() + ":  ");
			sequence.print();
			System.out.println("");
		}
	}
	
	public String toString(){
		StringBuffer r = new StringBuffer();
		for(Sequence sequence : sequences){ // for each transaction
			r.append(sequence.getId());
			r.append(":  ");
			r.append(sequence.toString());
			r.append('\n');
		}
		return r.toString();
	}
	
	public int size(){
		return sequences.size();
	}

	public List<Sequence> getSequences() {
		return sequences;
	}

	public Set<Integer> getSequenceIDs() {
		Set<Integer> set = new HashSet<Integer>();
		for(Sequence sequence : getSequences()){
			set.add(sequence.getId());
		}
		return set;
	}

	//--------------- For clustering
	public Cluster getCluster() {
		return cluster;
	}

	public void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}
}
