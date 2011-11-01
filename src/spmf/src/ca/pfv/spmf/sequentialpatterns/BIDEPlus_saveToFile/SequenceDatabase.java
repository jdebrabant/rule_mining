package ca.pfv.spmf.sequentialpatterns.BIDEPlus_saveToFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation of a sequence database.
 * Each sequence should have a unique id.
 * See examples in /test/ directory for the format of input files.
 * @author Philippe Fournier-Viger 
 **/
public class SequenceDatabase{

	// List of sequences
	private final List<Sequence> sequences = new ArrayList<Sequence>();
	
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
			if(integer.equals("-1")){ // indicate the end of an itemset
				sequence.addItemset(itemset);
				itemset = new Itemset();
			}else if(integer.equals("-2")){ // indicate the end of a sequence
				// check if the last "-1" was not included
				if(itemset.size() >0){
					sequence.addItemset(itemset);
					itemset = new Itemset();
				}
				sequences.add(sequence);
			}else{ // an item with the format :  id
				// extract the value for an item
				int item = Integer.parseInt(integer);
				if(!itemset.getItems().contains(item)){
					itemset.addItem(item);
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
}
