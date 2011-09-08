package ca.pfv.spmf.sequentialpatterns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * This is an implementation of the BIDE+ algorithm by Wang et al. 2007
 * This implementation is part of the SPMF framework.
 * 
 * @author Philippe Fournier-Viger
 **/

public class AlgoBIDEPlus extends AbstractAlgoPrefixSpan {
	
	// The sequential patterns that are found
	private Sequences patterns = null;
	
	// for statistics
	private long startTime;
	private long endTime;
	
	private final double minsup;
	
	// relative minimum support
	private int minsuppRelative;
	
	// For BIDE+, we have to keep a pointer to the original database
	private PseudoSequenceDatabase initialContext = null;
		
	public AlgoBIDEPlus(double minsup){
		this.minsup = minsup;
	}

	public double getMinSupp() {
		return minsup;
	}

	public Sequences runAlgorithm(SequenceDatabase database) {
		patterns = new Sequences("FREQUENT CLOSED SEQUENTIAL PATTERNS");
		this.minsuppRelative = (int) Math.ceil(minsup* database.size());
		if(this.minsuppRelative == 0){ // protection
			this.minsuppRelative = 1;
		}
		startTime = System.currentTimeMillis();
		bide(database);
		endTime = System.currentTimeMillis();
		return patterns;
	}
	
	/**
	 * @param contexte The initial context.
	 */
	private void bide(SequenceDatabase database){
		// We have to scan the database to find all frequent patterns of size 1.
		// We note the sequences in which these patterns appear.
		Map<Item, Set<Integer>> mapSequenceID = findSequencesContainingItems(database);
		
		// WE CONVERT THE DATABASE TO A PSEUDO-DATABASE, AND REMOVE
		// THE ITEMS OF SIZE 1 THAT ARE NOT FREQUENT, SO THAT THE ALGORITHM 
		// WILL NOT CONSIDER THEM ANYMORE. (OPTIMIZATION : OCTOBER-08 )
		initialContext = new PseudoSequenceDatabase();
		for(Sequence sequence : database.getSequences()){
			Sequence optimizedSequence = sequence.cloneSequenceMinusItems(mapSequenceID, minsuppRelative);
			if(optimizedSequence.size() != 0){
				initialContext.addSequence(new PseudoSequence(0, optimizedSequence, 0, 0));
			}
		}
		
		// For each item
		for(Entry<Item, Set<Integer>> entry : mapSequenceID.entrySet()){
			if(entry.getValue().size() >= minsuppRelative){ // if the item is frequent
				// build the projected context
				Item item = entry.getKey();
				PseudoSequenceDatabase projectedContext = buildProjectedContext(item, initialContext,  false);

				// Create the prefix for the projected context.
				Sequence prefix = new Sequence(0);  
				prefix.addItemset(new Itemset(item, 0));
				prefix.setSequencesID(entry.getValue());
				
				int frequenceSucesseurs =0;
				
				// We recursively call this method with the new prefix.
				if(!checkBackScanPruning(prefix)){
					frequenceSucesseurs = recursion(prefix, projectedContext); 
				}
				
				// We add the prefix to frequent sequential patterns found.
				if(prefix.getAbsoluteSupport() != frequenceSucesseurs){// no forward extension
					if(!checkBackwardExtension(prefix)){ // no backward extension
						patterns.addSequence(prefix, 1);  // we found a closed sequence.
					}
				}
			}
		}		
	}
	
	/**
	 * Return true if we should stop to explore this prefix.
	 * 
	 * Backscan-pruning is from the Bide article.
	 * 
	 * @param prefix
	 * @param projectedContext
	 * @return boolean
	 */
	private boolean checkBackScanPruning(Sequence prefix) {	
		for(int i=0; i< prefix.getItemOccurencesTotalCount(); i++){
			// (1) For each i, we construct the list of semi-maximum periods.
			List<PseudoSequence> semimaximumPeriods = new ArrayList<PseudoSequence>();
			for(PseudoSequence sequence : initialContext.getPseudoSequences()){
				if(prefix.getSequencesID().contains(sequence.getId())){
					PseudoSequence period = sequence.getIthSemiMaximumPeriodOfAPrefix(prefix, i, false);
					
					if(period !=null){
						semimaximumPeriods.add(period);
					}
				}
			}
			// (2) check if an element of the semi-max perdios as the same frequency as the prefix.
			Set<Pair> paires = findAllFrequentPairsForBackwardExtensionCheck(prefix, semimaximumPeriods, i);
			for(Pair pair : paires){				
				if(pair.getCount() == prefix.getAbsoluteSupport()){
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Return true if there is a backward-extension (see Bide+ article).
	 * This method do it a little bit differently than the BIDE+ article since
	 * we iterate with i on elements of the prefix instead of iterating with 
	 * a i on the itemsets of the prefix. But the idea is the same!
	 * @param prefix
	 * @param projectedContext
	 * @return boolean
	 */
	private boolean checkBackwardExtension(Sequence prefix) {	

		// CHECK FOR S-EXTENSION
		for(int i=0; i< prefix.getItemOccurencesTotalCount(); i++){
			// (1) For each i, we build the list of maximum periods
			List<PseudoSequence> maximumPeriods = new ArrayList<PseudoSequence>();
			for(PseudoSequence sequence : initialContext.getPseudoSequences()){
				if(prefix.getSequencesID().contains(sequence.getId())){

					PseudoSequence period = sequence.getIthMaximumPeriodOfAPrefix(prefix, i, false);
					
					if(period !=null){
						maximumPeriods.add(period);
					}
				}
			}
			// (2)check if an element from the maximum periods has the same support as the prefix.
			for(Pair pair : findAllFrequentPairsForBackwardExtensionCheck(prefix, maximumPeriods, i)){
				if(pair.getCount() == prefix.getAbsoluteSupport()){
					return true;
				}
			}
		}
		return false; // no backward extension
	} 
	
	/**
	 * Method to find all frequent items in a context (dabase).
	 * This is for k> 1.
	 * @param prefix
	 * @param contexte
	 * @return
	 */
	protected Set<Pair> findAllFrequentPairsForBackwardExtensionCheck(
			Sequence prefix, List<PseudoSequence> maximumPeriods, int iPeriod) {
		// On va traverser la BD et stocker les fréquences cumulatives de chaque paires dans une Map au fur et à mesure.
		Map<Pair, Pair> mapPaires = new HashMap<Pair, Pair>();
		// Important: We need to make sure that don't count two time the same element 
		PseudoSequence lastPeriod = null;
		Set<Pair> alreadyCountedForSequenceID = new HashSet<Pair>(); // il ne faut compter un item qu'une fois par séquence ID.

		// NEW CODE 2010-02-04
		Item itemI = prefix.getIthItem(iPeriod);  // iPeriod 
		Item itemIm1 = null;  // iPeriod -1
		if(iPeriod > 0){ 
			itemIm1 = prefix.getIthItem(iPeriod -1);	
		}
		// END NEW
		
		
		for(PseudoSequence period : maximumPeriods){
			if(period != lastPeriod){
				alreadyCountedForSequenceID.clear(); 
				lastPeriod = period;
			}

			for(int i=0; i< period.size(); i++){
				// NEW
				boolean sawI = false;  // sawI after current position
				boolean sawIm1 = false; // sawI-1 before current position
				// END NEW
				
				// NEW march 20 2010 : check if I is after current position in current itemset
				for(int j=0; j < period.getSizeOfItemsetAt(i); j++){
					Item item = period.getItemAtInItemsetAt(j, i);
					if(item.getId() == itemI.getId()){
						sawI = true; 
					}else if (item.getId() > itemI.getId()){
						break;
					}
				}
				// END NEW
				
				for(int j=0; j < period.getSizeOfItemsetAt(i); j++){
					Item item = period.getItemAtInItemsetAt(j, i);
					
					// NEW
//					if(item.getId() == itemI.getId()){
//						sawI = true;
//					}
					if(itemIm1 != null && item.getId() == itemIm1.getId()){
						sawIm1 = true;
					}
					
					boolean isPrefix = period.isCutAtRight(i);
					boolean isPostfix = period.isPostfix(i);
					// END NEW

					// normal case
					Pair paire = new Pair(isPrefix, isPostfix, item);  
					addPaire(mapPaires, alreadyCountedForSequenceID, period,
							paire);
					
					// NEW: special cases
					if(sawIm1){
						Pair paire2 = new Pair(isPrefix, !isPostfix, item);  
						addPaire(mapPaires, alreadyCountedForSequenceID, period,
								paire2);
					}

					if(sawI ){  
						Pair paire2 = new Pair(!isPrefix, isPostfix, item);  
						addPaire(mapPaires, alreadyCountedForSequenceID, period,
								paire2);
					}
					// END NEW
				}
			}
		}
		return mapPaires.keySet();
	}

	private void addPaire(Map<Pair, Pair> mapPaires,
			Set<Pair> alreadyCountedForSequenceID, PseudoSequence period,
			Pair paire) {
		Pair oldPaire = mapPaires.get(paire);
		if(!alreadyCountedForSequenceID.contains(paire)){
			if(oldPaire == null){
				mapPaires.put(paire, paire);
			}else{
				paire = oldPaire;
			}
			alreadyCountedForSequenceID.add(paire);
			// On conserve l'ID de la séquence
			paire.getSequencesID().add(period.getId());
		}
	}
	
	/**
	 * Find all sequences that contains an item.
	 * @param contexte Le contexte
	 * @return Map of items and Set of sequences that contains each of them.
	 */
	private Map<Item, Set<Integer>> findSequencesContainingItems(SequenceDatabase contexte) {
		Set<Integer> alreadyCounted = new HashSet<Integer>(); // il faut compter un item qu'une fois par séquence.
		Sequence lastSequence = null;
		Map<Item, Set<Integer>> mapSequenceID = new HashMap<Item, Set<Integer>>(); // pour conserver les ID des séquences: <Id Item, Set d'id de séquences>
		for(Sequence sequence : contexte.getSequences()){
			if(lastSequence == null || lastSequence.getId() != sequence.getId()){ // FIX
				alreadyCounted.clear(); 
				lastSequence = sequence;
			}
			for(Itemset itemset : sequence.getItemsets()){
				for(Item item : itemset.getItems()){
					if(!alreadyCounted.contains(item.getId())){
						Set<Integer> sequenceIDs = mapSequenceID.get(item);
						if(sequenceIDs == null){
							sequenceIDs = new HashSet<Integer>();
							mapSequenceID.put(item, sequenceIDs);
						}
						sequenceIDs.add(sequence.getId());
						alreadyCounted.add(item.getId()); 
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
	private PseudoSequenceDatabase buildProjectedContext(Item item, PseudoSequenceDatabase database, boolean inSuffix) {
		// The projected pseudo-database
		PseudoSequenceDatabase sequenceDatabase = new PseudoSequenceDatabase();

		for(PseudoSequence sequence : database.getPseudoSequences()){ // for each sequence
			for(int i =0; i< sequence.size(); i++){  // for each item of the sequence
				
				// if the itemset contains the item
				int index = sequence.indexOf(i, item.getId());
				if(index != -1 && sequence.isPostfix(i) == inSuffix){
					if(index != sequence.getSizeOfItemsetAt(i)-1){ // if this is not the last item of the itemset
						PseudoSequence newSequence = new PseudoSequence(sequence.getAbsoluteTimeStamp(i), 
								sequence, i, index+1);
						if(newSequence.size() >0){
							sequenceDatabase.addSequence(newSequence);
						} 
					}else if ((i != sequence.size()-1)){// if this is not the last itemset of the sequence			 
						PseudoSequence newSequence = new PseudoSequence(sequence.getAbsoluteTimeStamp(i), sequence, i+1, 0);
						if(newSequence.size() >0){
							sequenceDatabase.addSequence(newSequence);
						}	
					}	
				}
			}
		}
		return sequenceDatabase;
	}
	
	private int recursion(Sequence prefix, PseudoSequenceDatabase contexte) {	
		// find frequent items of size 1.
		Set<Pair> pairs = findAllFrequentPairs(prefix, contexte.getPseudoSequences());
		
		int frequenceMax = 0;
		
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
				newPrefix.setSequencesID(paire.getSequencesID()); 

				int frequenceSucesseur = 0;
				if(checkBackScanPruning(newPrefix) == false){
					 frequenceSucesseur = recursion(newPrefix, projectedContext); // récursion
				}		
				
				boolean noForwardSIExtension =  newPrefix.getAbsoluteSupport() != frequenceSucesseur;
				if(noForwardSIExtension){ 
					if(!checkBackwardExtension(newPrefix)){
						patterns.addSequence(newPrefix, newPrefix.size());  // it is a closed sequence
					}
				}
				if(newPrefix.getAbsoluteSupport() > frequenceMax){
					frequenceMax = newPrefix.getAbsoluteSupport();
				}
			}
		}
		return frequenceMax;
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
					Item item = sequence.getItemAtInItemsetAt(j, i);
					Pair paire = new Pair(sequence.isCutAtRight(i), sequence.isPostfix(i), item);  
					addPaire(mapPairs, alreadyCountedForSequenceID, sequence,
							paire);
				}
			}
		}
		return mapPairs.keySet();
	}

	// This method takes as parameters : a sequence, an item, and the item support.
	// It creates a copy of the sequence and add the item to the sequence. It sets the 
	// support of the sequence as the support of the item.
	private Sequence appendItemToSequence(Sequence prefix, Item item) {
		Sequence newPrefix = prefix.cloneSequence();  // isSuffix
		newPrefix.addItemset(new Itemset(item, 0));  // créé un nouvel itemset   + decalage
		return newPrefix;
	}
	
	// This method takes as parameters : a sequence, an item, and the item support.
	// It creates a copy of the sequence and add the item to the last itemset of the sequence. 
	// It sets the support of the sequence as the support of the item.
	private Sequence appendItemToPrefixOfSequence(Sequence prefix, Item item) {
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
		r.append(" Closed sequential patterns count : ");
		r.append(patterns.sequenceCount);
		r.append('\n');
		r.append(patterns.toString(size));
		r.append("===================================================\n");
		System.out.println(r.toString());
	}

}
