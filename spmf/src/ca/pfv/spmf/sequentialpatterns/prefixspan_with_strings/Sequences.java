package ca.pfv.spmf.sequentialpatterns.prefixspan_with_strings;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of a set of sequences, grouped by their size (how many items they have).
 * @author Philippe Fournier-Viger 
 */
public class Sequences {
	public final List<List<Sequence>> levels = new ArrayList<List<Sequence>>();  // itemset classé par taille
	public int sequenceCount=0;
	
	private final String name;
	
	public Sequences(String name){
		this.name = name;
		levels.add(new ArrayList<Sequence>()); // on créé le niveau zéro vide par défaut.
	}
	
	public void printSequencesFrequentes(int nbObject){
		System.out.println(toString(nbObject));
	}
	
	public String toString(int nbObject){
		StringBuffer r = new StringBuffer(200);
		r.append(" ----------");
		r.append(name);
		r.append(" -------\n");
		int levelCount=0;
		for(List<Sequence> level : levels){
			r.append("  L");
			r.append(levelCount);
			r.append(" \n");
			for(Sequence sequence : level){
				r.append("  pattern ");
				r.append(sequence.getId());
				r.append(":  ");
				r.append(sequence.toString());
				r.append("support :  ");
				r.append(sequence.getRelativeSupportFormated(nbObject));
				r.append(" (" );
				r.append(sequence.getAbsoluteSupport());
				r.append('/');
				r.append(nbObject);
				r.append(") \n");
			}
			levelCount++;
		}
		r.append(" -------------------------------- Patterns count : ");
		r.append(sequenceCount);
		return r.toString();
	}
	
	public void addSequence(Sequence sequence, int k){
		while(levels.size() <= k){
			levels.add(new ArrayList<Sequence>());
		}
		levels.get(k).add(sequence);
		sequenceCount++;
	}
	
	public List<Sequence> getLevel(int index){
		return levels.get(index);
	}
	
	public int getLevelCount(){
		return levels.size();
	}

	public List<List<Sequence>> getLevels() {
		return levels;
	}
}
