package ca.pfv.spmf.sequentialpatterns.prefixspan_with_strings;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * This is an implementation of the PrefixSpan algorithm (Pei et al. 2001)
 * 
 * This is the third version. It can take strings instead of integers as input.
 * I have created a separated version for this because many people have requested this feature.
 * 
 * This implementation is part of the SPMF framework.
 * 
 * This implemtation uses pseudo-projection as suggested by Pei et al. 2001
 * @author Philippe Fournier-Viger
 **/

public class AlgoPrefixSpanStrings{
	
	// The sequential patterns that are found
	private Sequences patterns = null;
	
	// for statistics
	private long startTime;
	private long endTime;
	
	private final double minsup;
	
	// relative minimum support
	private int minsuppRelative;
		
	public AlgoPrefixSpanStrings(double minsup){
		this.minsup = minsup;
	}

	public double getMinSupp() {
		return minsup;
	}

	public Sequences runAlgorithm(SequenceDatabase database) {
		patterns = new Sequences("FREQUENT SEQUENTIAL PATTERNS");
		this.minsuppRelative = (int) Math.ceil(minsup* database.size());
		if(this.minsuppRelative == 0){ // protection
			this.minsuppRelative = 1;
		}
		startTime = System.currentTimeMillis();
		prefixSpan(database);
		endTime = System.currentTimeMillis();
		return patterns;
	}
	
	/**
	 * @param contexte The initial context.
	 */
	private void prefixSpan(SequenceDatabase database){
		// We have to scan the database to find all frequent patterns of size 1.
		// We note the sequences in which these patterns appear.
		Map<String, Set<Integer>> mapSequenceID = findSequencesContainingItems(database);
		
		// WE CONVERT THE DATABASE IN A PSEUDO-DATABASE, AND REMOVE
		// THE ITEMS OF SIZE 1 THAT ARE NOT FREQUENT, SO THAT THE ALGORITHM 
		// WILL NOT CONSIDER THEM ANYMORE. (OPTIMIZATION : OCTOBER-08 )
		PseudoSequenceDatabase initialContext = new PseudoSequenceDatabase();
		for(Sequence sequence : database.getSequences()){
			Sequence optimizedSequence = sequence.cloneSequenceMinusItems(mapSequenceID, minsuppRelative);
			if(optimizedSequence.size() != 0){
				initialContext.addSequence(new PseudoSequence(0, optimizedSequence, 0, 0));
			}
		}
		
		// For each item
		for(Entry<String, Set<Integer>> entry : mapSequenceID.entrySet()){
			if(entry.getValue().size() >= minsuppRelative){ // if the item is frequent
				// build the projected context
				String item = entry.getKey();
				PseudoSequenceDatabase projectedContext = buildProjectedContext(item, initialContext,  false);

				// Create the prefix for the projected context.
				Sequence prefix = new Sequence(0);  
				prefix.addItemset(new Itemset(item));
				prefix.setSequencesID(entry.getValue());

				patterns.addSequence(prefix, 1);  // we found a sequence.
				
				// Recursive call !
				recursion(prefix, 2, projectedContext); 
				
			}
		}		
	}
	
	/**
	 * Find all sequences that contains an item.
	 * @param contexte Le contexte
	 * @return Map of items and Set of sequences that contains each of them.
	 */
	private Map<String, Set<Integer>> findSequencesContainingItems(SequenceDatabase contexte) {
		Set<String> alreadyCounted = new HashSet<String>(); // il faut compter un item qu'une fois par séquence.
		Sequence lastSequence = null;
		Map<String, Set<Integer>> mapSequenceID = new HashMap<String, Set<Integer>>(); // pour conserver les ID des séquences: <Id Item, Set d'id de séquences>
		for(Sequence sequence : contexte.getSequences()){
			if(lastSequence == null || lastSequence.getId() != sequence.getId()){ // FIX
				alreadyCounted.clear(); 
				lastSequence = sequence;
			}
			for(Itemset itemset : sequence.getItemsets()){
				for(String item : itemset.getItems()){
					if(!alreadyCounted.contains(item)){
						Set<Integer> sequenceIDs = mapSequenceID.get(item);
						if(sequenceIDs == null){
							sequenceIDs = new HashSet<Integer>();
							mapSequenceID.put(item, sequenceIDs);
						}
						sequenceIDs.add(sequence.getId());
						alreadyCounted.add(item); 
					}
				}
			}
		}
		return mapSequenceID;
	}
	

	/**
	 * Create a projected database by pseudo-projection
	 * @param item The item to use to make the pseudo-projection
	 * @param context The current database.
	 * @param inSuffix This boolean indicates if the item "item" is part of a suffix or not.
	 * @return the projected database.
	 */
	private PseudoSequenceDatabase buildProjectedContext(String item, PseudoSequenceDatabase database, boolean inSuffix) {
		// The projected pseudo-database
		PseudoSequenceDatabase sequenceDatabase = new PseudoSequenceDatabase();

		for(PseudoSequence sequence : database.getPseudoSequences()){ // for each sequence
			for(int i =0; i< sequence.size(); i++){  // for each item of the sequence
				
				// if the itemset contains the item
				int index = sequence.indexOf(i, item);
				if(index != -1 && sequence.isPostfix(i) == inSuffix){
					if(index != sequence.getSizeOfItemsetAt(i)-1){ // if this is not the last item of the itemset
						PseudoSequence newSequence = new PseudoSequence( 
								sequence, i, index+1);
						if(newSequence.size() >0){
							sequenceDatabase.addSequence(newSequence);
						} 
					}else if ((i != sequence.size()-1)){// if this is not the last itemset of the sequence			 
						PseudoSequence newSequence = new PseudoSequence( sequence, i+1, 0);
						if(newSequence.size() >0){
							sequenceDatabase.addSequence(newSequence);
						}	
					}	
				}
			}
		}
		return sequenceDatabase;
	}
	
	private void recursion(Sequence prefix, int k, PseudoSequenceDatabase contexte) {	
		// find frequent items of size 1.
		Set<Pair> pairs = findAllFrequentPairs(prefix, contexte.getPseudoSequences());
		
		// For each pair found, 
		for(Pair paire : pairs){
			// if the item is freuqent.
			if(paire.getCount() >= minsuppRelative){
				// create the new postfix
				Sequence newPrefix;
				if(paire.isPostfix()){ // if the item is part of a postfix
					newPrefix = appendItemToPrefixOfSequence(prefix, paire.getItem()); // is =<is, (deltaT,i)>
				}else{ // else
					newPrefix = appendItemToSequence(prefix, paire.getItem());
				}
				// build the projected database
				PseudoSequenceDatabase projectedContext = buildProjectedContext(paire.getItem(), contexte, paire.isPostfix());

				// create new prefix
				Sequence prefix2 = newPrefix.cloneSequence();
				prefix2.setSequencesID(paire.getSequencesID()); 
				
				// On fait une récursion en appelant projection avec le prefixe.
				patterns.addSequence(prefix2, prefix2.size());  
				recursion(prefix2, k+1, projectedContext); // récursion
			}
		}
	}
	
	/**
	 * Method to find all frequent items in a context (database).
	 * This is for k> 1.
	 * @param prefix
	 * @param sequences
	 * @return
	 */
	protected Set<Pair> findAllFrequentPairs(Sequence prefix, List<PseudoSequence> sequences){
		// we will scan the database and store the cumulative support of each pair
		// in a map.
		Map<Pair, Pair> mapPairs = new HashMap<Pair, Pair>();
		
		PseudoSequence lastSequence = null;
		Set<Pair> alreadyCountedForSequenceID = new HashSet<Pair>(); // to count each item only one time for each sequence ID

		for(PseudoSequence sequence : sequences){
			// if the sequence does not have the same id, we clear the map.
			if(sequence != lastSequence){
				alreadyCountedForSequenceID.clear(); 
				lastSequence = sequence;
			}

			for(int i=0; i< sequence.size(); i++){
				for(int j=0; j < sequence.getSizeOfItemsetAt(i); j++){
					String item = sequence.getItemAtInItemsetAt(j, i);
					Pair paire = new Pair(false, sequence.isPostfix(i), item);   // false is ok?
					Pair oldPaire = mapPairs.get(paire);
					if(!alreadyCountedForSequenceID.contains(paire)){
						if(oldPaire == null){
							mapPairs.put(paire, paire);
						}else{
							paire = oldPaire;
						}
						alreadyCountedForSequenceID.add(paire);
						// we keep the sequence id
						paire.getSequencesID().add(sequence.getId());
					}
				}
			}
		}
		return mapPairs.keySet();
	}

	// This method takes as parameters : a sequence, an item, and the item support.
	// It creates a copy of the sequence and add the item to the sequence. It sets the 
	// support of the sequence as the support of the item.
	private Sequence appendItemToSequence(Sequence prefix, String item) {
		Sequence newPrefix = prefix.cloneSequence();  // isSuffix
		newPrefix.addItemset(new Itemset(item));  // créé un nouvel itemset   + decalage
		return newPrefix;
	}
	
	// This method takes as parameters : a sequence, an item, and the item support.
	// It creates a copy of the sequence and add the item to the last itemset of the sequence. 
	// It sets the support of the sequence as the support of the item.
	private Sequence appendItemToPrefixOfSequence(Sequence prefix, String item) {
		Sequence newPrefix = prefix.cloneSequence();
		Itemset itemset = newPrefix.get(newPrefix.size()-1);  // ajoute au dernier itemset
		itemset.addItem(item);   
		return newPrefix;
	}

	public void printStatistics(int size) {
		StringBuffer r = new StringBuffer(200);
		r.append("=============  Algorithm - STATISTICS =============\n Total time ~ ");
		r.append(endTime - startTime);
		r.append(" ms\n");
		r.append(" Frequent sequences count : ");
		r.append(patterns.sequenceCount);
		r.append('\n');
		r.append(patterns.toString(size));
		r.append("===================================================\n");
		System.out.println(r.toString());
	}

}
