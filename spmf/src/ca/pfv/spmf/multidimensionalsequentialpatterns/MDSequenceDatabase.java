package ca.pfv.spmf.multidimensionalsequentialpatterns;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.pfv.spmf.multidimensionalpatterns.MDPattern;
import ca.pfv.spmf.multidimensionalpatterns.MDPatternsDatabase;
import ca.pfv.spmf.sequentialpatterns.Item;
import ca.pfv.spmf.sequentialpatterns.ItemValued;
import ca.pfv.spmf.sequentialpatterns.Itemset;
import ca.pfv.spmf.sequentialpatterns.Sequence;
import ca.pfv.spmf.sequentialpatterns.SequenceDatabase;

/**
 * Implementation of a "Multi-Dimensional Sequence Database"
 * based on Helen Pinto et al (2001).
 * 
 * A MD-Sequences database contains a list of md-Sequences, where
 * each MD-Sequence is composed of an MD-Pattern and a Sequence.
 * 
 * @author Philippe Fournier-Viger 
 **/
public class MDSequenceDatabase {
	// List of md-sequences
	private final List<MDSequence> sequences = new ArrayList<MDSequence>();
	// We also keep the sequences and patterns in some separate databases.
	private final SequenceDatabase sequenceDatabase = new SequenceDatabase();
	private final MDPatternsDatabase patternDatabase = new MDPatternsDatabase();
	
	private  final Set<Item> attributes = new HashSet<Item>();
	
	private int sequenceNumber =0;
	
	public int getItemCount(){
		return attributes.size();
	}

	public void loadFile(String path) throws IOException {
		String thisLine;
		BufferedReader myInput = null;
		try {
			FileInputStream fin = new FileInputStream(new File(path));
			myInput = new BufferedReader(new InputStreamReader(fin));
			while ((thisLine = myInput.readLine()) != null) {
				// if this line is not a comment
				if(thisLine.charAt(0) != '#'){ 
					// we add the sequence
					addMDSequence(thisLine.split(" "));	
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
	
	public void addMDSequence(String[] entiers) {	
		// (1) Read the pattern
		MDPattern mdpattern = new MDPattern(sequenceNumber);
		int i= 0;
		for(; i< entiers.length; i++){
			if(entiers[i].equals("-3")){ // this is the end of values for dimensions
				break;
			}else if(entiers[i].equals("*")){
				mdpattern.addInteger(MDPattern.WILDCARD);
			}else{ // if it is a value
				mdpattern.addInteger(Integer.valueOf(entiers[i]));
			}
		}
		// (2) Read the sequence
		Sequence sequence = new Sequence(sequenceNumber);
		Itemset itemset = new Itemset();
		for(i++ ;i< entiers.length; i++){
			if(entiers[i].codePointAt(0) == '<'){
				String valeur = entiers[i].substring(1, entiers[i].length()-1);
				itemset.setTimestamp(Long.parseLong(valeur));
			}else if(entiers[i].equals("-1")){
				sequence.addItemset(itemset);
				itemset = new Itemset();
			}else if(entiers[i].equals("-2")){
				MDSequence mdsequence = new MDSequence(sequenceNumber, mdpattern, sequence);
				sequences.add(mdsequence);
				sequenceDatabase.addSequence(sequence);
				patternDatabase.addMDPattern(mdpattern);
				sequenceNumber++;
			}else{ // un item  au format : id(valeurentiere)  ou format : id
				// si l'item à une valeur entière, extraire la valeur
				int indexParentheseGauche = entiers[i].indexOf("(");
				int valeur =0;
				if(indexParentheseGauche != -1){
					int indexParentheseDroite = entiers[i].indexOf(")");
					valeur = Integer.parseInt(entiers[i].substring(indexParentheseGauche+1, indexParentheseDroite));
					entiers[i] = entiers[i].substring(0, indexParentheseGauche);
					ItemValued item = new ItemValued(Integer.parseInt(entiers[i]), valeur);
					itemset.addItem(item);
				}else{
					// extract the value for a valued item
					Item item = new Item(Integer.parseInt(entiers[i]));
					itemset.addItem(item);
				}
			}
		}
	}
	
	public void addSequence(MDSequence sequence){
		sequences.add(sequence);
		sequenceDatabase.addSequence(sequence.getSequence());
		patternDatabase.addMDPattern(sequence.getMdpattern());
	}

	
	public void printContext(){
		System.out.println(toString());
	}
	
	public String toString(){
		StringBuffer out = new StringBuffer("============  MD Sequence Database ==========\n");
		int sequencesCount = 0;
		for(MDSequence sequence : sequences){ //for each transaction
			out.append(sequence.toString() + "\n");
			sequencesCount++;
		}
		return out.toString();
	}
	
	public int size(){
		return sequences.size();
	}

	public List<MDSequence> getSequences() {
		return sequences;
	}
	
	public MDSequence get(int index) {
		return sequences.get(index);
	}

	public Set<Item> getAttributes() {
		return attributes;
	}

	public SequenceDatabase getSequenceDatabase() {
		return sequenceDatabase;
	}

	public MDPatternsDatabase getPatternDatabase() {
		return patternDatabase;
	}
}
