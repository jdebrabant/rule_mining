package ca.pfv.spmf.sequentialpatterns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import ca.pfv.spmf.clustering.kmeans_for_seq_pattern_mining.AbstractAlgoClustering;
import ca.pfv.spmf.clustering.kmeans_for_seq_pattern_mining.Cluster;

/**
 * This is the Fournier-Viger algorithm (2008) that is based on Hirate et Yamana (2006).
 * I have added some new stuff to the algorithm : 
 *  - clustering of valued items.
 *  - closed sequences, based on Bide+
 *  - ...
 * @author Philippe Fournier-Viger
 **/
public class AlgoFournierViger08 extends AbstractAlgoPrefixSpan{
	// The sequential patterns that are found
	private Sequences patterns = null;
	
	// for statistics
	private long startTime;
	
	// parameters of runAlgorithm
	private final double minInterval;
	private final double maxInterval;
	private final double minWholeInterval;
	private final double maxWholeInterval;
	private final double minsupp;
	private final boolean findClosedPatterns;
	private int minsuppRelative;
	private boolean enableBackscanPruning;
	
	// For clustering
	private final AbstractAlgoClustering algoClustering;
	
	// For BIDE+, we have to keep a pointer to the original database
	private PseudoSequenceDatabase initialContext = null;

	
	
	/**
	 * @param minsupp minimum support
	 * @param minInterval minimum item interval between two adjacent items. (C1)
	 * @param maxInterval maximum item interval between two adjacent items. (C2)
	 * @param minWholeInterval minimum item interval between the head and tail of a sequence. (C3)
	 * @param maxWholeInterval maximum item interval between the head and tail of a sequence (C4)
	 * @param algoClustering  algorithm for clustering 
	 * @param findClosedPatterns to mine only closed sequences
	 */
	public AlgoFournierViger08(double minsupp, 
			double minInterval, double maxInterval, 
			double minWholeInterval, double maxWholeInterval, 
			AbstractAlgoClustering algoClustering, 
			boolean findClosedPatterns, boolean enableBackscanPruning){
		// Checking if the parameters are correct.
		if((minInterval > maxInterval) ||
			(minWholeInterval > maxWholeInterval) ||
			(minInterval > maxWholeInterval) ||
			(maxInterval > maxWholeInterval)){
			throw new RuntimeException("Parameters are not valid!!!");
		}	
		
		// Save the parameters in some class variables
		this.minInterval = minInterval;
		this.maxInterval = maxInterval;
		this.minWholeInterval = minWholeInterval;
		this.maxWholeInterval = maxWholeInterval;
		this.algoClustering = algoClustering;
		this.minsupp = minsupp;
		this.findClosedPatterns = findClosedPatterns;
		this.enableBackscanPruning = enableBackscanPruning;
	}
	
	public Sequences runAlgorithm(SequenceDatabase contexte) {
		patterns = new Sequences("FREQUENT SEQUENCES WITH TIME + CLUSTERING");
		this.minsuppRelative = (int) Math.ceil(minsupp * contexte.size());
		if(this.minsuppRelative == 0){ // protection
			this.minsuppRelative = 1;
		}
		

		
//		System.out.println("=============  EXECUTION PrefixSpanItemIntervalsClustering =============");
//		System.out.println("Support absolu : " + minsuppRelatif);
//		contexte.printContext();
//		System.out.println("-----");
		startTime = System.currentTimeMillis();
		isdb(contexte);
		return patterns;
	}

	
	/**
	 * Method ISDB based on the description in the article of Hirate & Yumana.
	 * @param contexte The initial context.
	 */
	private void isdb(SequenceDatabase contexteInitial){
		// We have to scan the database to find all frequent patterns of size 1.
		// We note the sequences in which these patterns appear.
		Map<Item, Set<Integer>> mapSequenceID = findSequencesContainingItems(contexteInitial);
		
		// WE CONVERT THE DATABASE IN A PSEUDO-DATABASE, AND REMOVE
		// THE ITEMS OF SIZE 1 THAT ARE NOT FREQUENT, SO THAT THE ALGORITHM 
		// WILL NOT CONSIDER THEM ANYMORE. (OPTIMIZATION : OCTOBER-08 )
		initialContext = new PseudoSequenceDatabase();
		for(Sequence sequence : contexteInitial.getSequences()){
			Sequence optimizedSequence = sequence.cloneSequenceMinusItems(mapSequenceID, minsuppRelative);
			if(optimizedSequence.size() != 0){
				initialContext.addSequence(new PseudoSequence(0, optimizedSequence, 0, 0));
			}
		}
		
		// For each item
		for(Entry<Item, Set<Integer>> entry : mapSequenceID.entrySet()){
			if(entry.getValue().size() >= minsuppRelative){ // s'il est fréquent
				Item item = entry.getKey();
				PseudoSequenceDatabase[] projectedContexts = null;
				if(item instanceof ItemValued){
					projectedContexts = buildProjectedContextItemValued((ItemValued)item, initialContext,  false, -1);
				}else{
					projectedContexts = buildProjectedContext(item, initialContext,  false, -1);
				}
				
				// For each projected database (because of clustering, there could be many)
				for(PseudoSequenceDatabase projectedContext : projectedContexts){
					// we create a prefix
					Sequence prefix = new Sequence(0);  
					if(projectedContext.getCluster() ==  null){  // there was no clustering
						prefix.addItemset(new Itemset(item, 0));
						prefix.setSequencesID(entry.getValue());
					}
					else{ // There was valued items (clustering or not)
						ItemValued item2 = new ItemValued(entry.getKey().getId(),
								projectedContext.getCluster().getMedian(),
								projectedContext.getCluster().getLower(),
								projectedContext.getCluster().getHigher());         
						prefix.addItemset(new Itemset(item2, 0));
						// Sequence IDs
						prefix.setSequencesID(projectedContext.getCluster().getSequenceIDs());
					}
					int frequenceSucesseurs =0;
					
					// We recursively call this method with the new prefix.
					if(!findClosedPatterns  || !checkBackScanPruning(prefix)){
						frequenceSucesseurs = projection(prefix, 2, projectedContext); 
					}
					
					if(isMinWholeIntervalRespected(prefix)){ 
						// We add the prxi to frequent sequential patterns found.
						boolean noForwardSIExtension = !findClosedPatterns || !(prefix.getAbsoluteSupport() == frequenceSucesseurs);
						boolean noBackwardExtension = !findClosedPatterns  || !checkBackwardExtension(prefix);
//						if(noForwardSIExtension && !noBackwardExtension){
//							System.out.println("BACCK");
//						}
						if(noForwardSIExtension && noBackwardExtension){ // IF CLOSED
							patterns.addSequence(prefix, 1);  // we found a sequence.
						}
					}
				}
			}
		}		
	}

	/**
	 * Return true if there is a backward-extension (see Bide+ article).
	 * This method do it a little bit differently than the BIDE+ article since
	 * we iterate with i on elements of the prefix instead of iterating with 
	 * a i on the itemsets of the prefix. Also because we use timestamps it is a little
	 * bit different.
	 * @param prefix
	 * @param projectedContext
	 * @return boolean
	 */
	private boolean checkBackwardExtension(Sequence prefix) {	

		for(int i=0; i< prefix.getItemOccurencesTotalCount(); i++){
			// (1)For each i, create the list of maximum periods
			List<PseudoSequence> maximumPeriods = new ArrayList<PseudoSequence>();
			for(PseudoSequence sequence : initialContext.getPseudoSequences()){
				if(prefix.getSequencesID().contains(sequence.getId())){
					
					// nov 2009 : FIXED BUG HERE, so that maxgap works
					// with timestamp we need to do it differently than bide..
					List<PseudoSequence> periods = sequence.getAllIthMaxPeriodOfAPrefix(prefix, i, true);
					
					for(PseudoSequence period : periods){
						if(period !=null){
							maximumPeriods.add(period);
						}
					}
				}
			}
			
			// (2)check if an element from the maximum periods has the same support as the prefix.
			for(Pair pair : findAllFrequentPairsSatisfyingC1andC2ForBackwardExtensionCheck(prefix, maximumPeriods, i)){
				if(pair.getCount() == prefix.getAbsoluteSupport()){
					return true;
				}
			}
		}
//		System.out.println("NO BACKWARD");
		return false;
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
		//*********************************************************************
		// VERY IMPORTANT : The backscan pruning cannot work correctly if
		// the maximum whole interval constraint (constraint C4 in Hirate & Yamana)
		// is not equal to infinity.
		// *********************************************************************
		
		if(enableBackscanPruning == false){
			return false;
		}
		
		for(int i=0; i< prefix.getItemOccurencesTotalCount(); i++){
			// (1) For each i, we construct the list of semi-maximum periods.
			List<PseudoSequence> semimaximumPeriods = new ArrayList<PseudoSequence>();
			for(PseudoSequence sequence : initialContext.getPseudoSequences()){
				if(prefix.getSequencesID().contains(sequence.getId())){

					PseudoSequence period = sequence.getIthSemiMaximumPeriodOfAPrefix(prefix, i, true);
					
					if(period !=null){
						semimaximumPeriods.add(period);
					}
				}
			}
			// (2) check if an element of the semi-max perdios as the same frequency as the prefix.
			Set<Pair> paires = findAllFrequentPairsSatisfyingC1andC2ForBackwardExtensionCheck(prefix, semimaximumPeriods, i);
			for(Pair pair : paires){
//				System.out.println(" Entry : " + pair.getItem().getId() + " post " +  pair.isPostfix() +  "  freq " + pair.getCount());
				if(pair.getCount() == prefix.getAbsoluteSupport()){
//					System.out.println("PRUNING SHOULD BE DONE");
					return true;
				}
			}
		}
//		System.out.println("NO PRUNING SHOULD BE DONE");
		return false;
	}
	
	
	/**
	 * Method to find all frequent items in a context (dabase) thas satisfy the C1 and C2 constraints.
	 * This is for k> 1.
	 * @param prefixe
	 * @param contexte
	 * @return
	 */
	protected Set<Pair> findAllFrequentPairsSatisfyingC1andC2ForBackwardExtensionCheck(
			Sequence prefixe, List<PseudoSequence> maximumPeriods, int iPeriod) {
		// On va traverser la BD et stocker les fréquences cumulatives de chaque paires dans une Map au fur et à mesure.
		Map<Pair, Pair> mapPaires = new HashMap<Pair, Pair>();
		// Important: Il faut s'assurer qu'on ne compte pas deux fois le même élément
		// pour des séquences ayant le même ID (contrairement à prefixspan, il est possible
		// que plusieurs séquences aient le même ID à cause de l'algorithme de projection.
		PseudoSequence lastPeriod = null;
		Set<Pair> alreadyCountedForSequenceID = new HashSet<Pair>(); // il ne faut compter un item qu'une fois par séquence ID.

		for(PseudoSequence period : maximumPeriods){
			// Si la séquence n'a plus le même ID, on vide la map.
			if(period != lastPeriod){
				alreadyCountedForSequenceID.clear(); 
				lastPeriod = period;
			}

			for(int i=0; i< period.size(); i++){
				for(int j=0; j < period.getSizeOfItemsetAt(i); j++){
					Item item = period.getItemAtInItemsetAt(j, i);
					
					// Rappel : un maximum period est un sous-ensemble d'une séquence
					
//					 intervalleSucesseur: l'intervalle en temps entre (1) l'itemset du "maximum period" contenant "item" 
					// "item"et (2) l'itemset de la séquence immédiatement après ce "maximum period". 
					// Dans le cas où l'itemset sucesseur est coupé en deux, 
					// c'est entre (1) le temps entre l'itemset contenant "item" 
					// et (2) le temps du dernier itemset de la "maximum period".
					long intervalleSucesseur = period.getTimeSucessor() - period.getAbsoluteTimeStamp(i); 
					// totalTime: temps total du préfixe obtenu si on ajoutait item au préfixe courant.
					long totalTime = prefixe.getTimeLength() + intervalleSucesseur; 
					// intervallePredecesseur: l'intervalle en temps entre (1) l'itemset du "maximum period" contenant "item" 
					// et (2) l'itemset de la séquence immédiatement avant ce "maximum period". Dans le cas où l'itemset 
					// prédécesseur est coupé en deux, c'est entre (1) le temps entre l'itemset contenant "item" 
					// et (2) le temps du premier itemset de la "maximum period".

					long intervallePredecesseur = period.getAbsoluteTimeStamp(i) - period.getTimePredecessor();
					
					// Vérification que l'intervalle avec le sucesseur respecte les contraintes C1 C2
					boolean checkGapSucesseur
						= intervalleSucesseur >= minInterval && intervalleSucesseur <= maxInterval || intervalleSucesseur == 0;
					
					// Vérification que l'intervalle avec le prédécesseur respecte les contraintes C1 C2
					// Si le "i" de cette "ith max period" est 0 (la variable "iPeriod"), c'est inutile de faire la vérification, car
					// on est dans le cas d'une backward extension où l'item serait ajouté AVANT le préfixe.
					boolean checkGapPredecesseur 
						= intervallePredecesseur >= minInterval && intervallePredecesseur <= maxInterval 
							|| iPeriod ==0 || intervallePredecesseur == 0;
					
					// Vérification que le temps total respecterait C3 et C4.
					// Si le "i" de cette "ith max period" est différent de 0 (la variable "iPeriod"), 
					// c'est inutile de faire la vérification, car on est dans le cas d'une backward extension 
					// où l'item serait ajouté À L'INTÉRIEUR du préfixe.
					boolean checkWholeInterval 
						= totalTime <= maxWholeInterval && totalTime >= minWholeInterval || iPeriod !=0;
					
					if(checkGapSucesseur  && checkGapPredecesseur && checkWholeInterval){ // C1 C2, C3, C4 check    
						Pair paire = new Pair(intervalleSucesseur, period.isCutAtRight(i), period.isPostfix(i), item);  // INTERVALLE ?
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
				}
			}
		}
		return mapPaires.keySet();
	}



	/**
	 * Trouve l'ensemble des séquences contenant chaque item d'un contexte (sans tenir compte des valeurs entières).
	 * @param contexte Le contexte
	 * @return Map de d'items et de liste de séquences contenant chaque item.
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
	 * Méthode Projection calquée sur la description dans l'article Hirate et Yumana.
	 * SCAN ISDB|is to find all pairs of item and its itemized interval, denoted as (delta-t, i).
	 * Retourne la plus haute fréquence parmi les séquences trouvées.
	 */
	private int projection(Sequence prefixe, int k, PseudoSequenceDatabase contexte) {	
//		System.out.println(" ----- PROJECTION ----");
//		prefixe.print();
//		contexte.printContext();
//		System.out.println(" ****** PROJECTION ****");
		int frequenceMax = 0;
		// Pour chaque paire trouvée, si la fréquence est suffisante et si les contraintes C4 est restpectée,
		// on  construit le préfixe, on construit la BD projetée, on appelle récursivement la méthode "projection",
		// et on ajoute la séquence aux séquences trouvées si C3 est respectée, 
		for(Pair paire : findAllFrequentPairsSatisfyingC1andC2(prefixe, contexte.getPseudoSequences())){
			// Si l'item fait partie d'un postfixe et est fréquent.
			if(paire.getCount() >= minsuppRelative){
				Sequence newPrefix;
				if(paire.isPostfix()){
					newPrefix = appendItemToPrefixOfSequence(prefixe, paire.getItem()); // is =<is, (deltaT,i)>
				}else{
					newPrefix = appendItemToSequence(prefixe, paire.getItem(), paire.getTimestamp());
				}
				if(isMaxWholeIntervalRespected(newPrefix)){ // C4 check
					int frequenceSucesseur = projectionPair(newPrefix, paire, prefixe, contexte, k);
					if(frequenceSucesseur > frequenceMax){
						frequenceMax = frequenceSucesseur;
					}
				}
			}
		}
		return frequenceMax;
	}

	private int projectionPair(Sequence newPrefix, Pair paire, Sequence prefixe, PseudoSequenceDatabase contexte, int k) {
//		System.out.println(" PREFIXE");
//		newPrefix.print();
//		System.out.println();
		int frequenceMax = 0;
		// Pour chaque contexte projeté (à cause du clustering, il peut y en avoir plusieurs)
		PseudoSequenceDatabase[] projectedContexts = null;
		if(paire.getItem() instanceof ItemValued){
			projectedContexts = buildProjectedContextItemValued((ItemValued)paire.getItem(), contexte, paire.isPostfix(), paire.getTimestamp());
		}else{
			projectedContexts = buildProjectedContext(paire.getItem(), contexte, paire.isPostfix(), paire.getTimestamp());
		}
		
		for(PseudoSequenceDatabase projectedContext : projectedContexts){
			Sequence prefix;
			if(projectedContext.getCluster() == null){ // Il n'y a pas eu d'actions à valeur entieres.
				prefix = newPrefix.cloneSequence();
				prefix.setSequencesID(paire.getSequencesID()); 
			}
			else{ // Il y a eu un ou plusieurs clusters
				ItemValued item2 = new ItemValued(projectedContext.getCluster().getItemId(),
					projectedContext.getCluster().getMedian(),
					projectedContext.getCluster().getLower(),
					projectedContext.getCluster().getHigher()); 
				Set<Integer> sequenceIDs = projectedContext.getCluster().getSequenceIDs();
				if(paire.isPostfix()){
					prefix = appendItemToPrefixOfSequence(prefixe, item2); 
				}else{
					prefix = appendItemToSequence(prefixe, item2, paire.getTimestamp());  
				}
				prefix.setSequencesID(sequenceIDs); 
			}
			int frequenceSucesseurs =0;
			
			// On fait une récursion en appelant projection avec le prefixe.
			if(!findClosedPatterns  || !checkBackScanPruning(prefix)){
				frequenceSucesseurs = projection(prefix, k+1, projectedContext); // récursion
			}		
			
			if(isMinWholeIntervalRespected(prefix)){ 
				boolean noForwardSIExtension = !findClosedPatterns || !(prefix.getAbsoluteSupport() == frequenceSucesseurs);
				boolean noBackwardExtension = !findClosedPatterns  || !checkBackwardExtension(prefix);
				if(noForwardSIExtension && noBackwardExtension){ // IF CLOSED
					patterns.addSequence(prefix, prefix.size());  // on a trouvé une séquence!
				}
				if(prefix.getAbsoluteSupport() > frequenceMax){
					frequenceMax = prefix.getAbsoluteSupport();
				}
			}
		}
		return frequenceMax;
	}

	/**
	 * Méthode pour trouver tout les items fréquents dans un contexte satisfaisant C1 et C2. Cette
	 * méthode est valable pour k>1.
	 * @param prefixe
	 * @param contexte
	 * @return
	 */
	protected Set<Pair> findAllFrequentPairsSatisfyingC1andC2(Sequence prefixe, List<PseudoSequence> sequences) {
		// On va traverser la BD et stocker les fréquences cumulatives de chaque paires dans une Map au fur et à mesure.
 		Map<Pair, Pair> mapPaires = new HashMap<Pair, Pair>();
		// Important: Il faut s'assurer qu'on ne compte pas deux fois le même élément
		// pour des séquences ayant le même ID (contrairement à prefixspan, il est possible
		// que plusieurs séquences aient le même ID à cause de l'algorithme de projection.
		PseudoSequence lastSequence = null;
		Set<Pair> alreadyCountedForSequenceID = new HashSet<Pair>(); // il ne faut compter un item qu'une fois par séquence ID.

		for(PseudoSequence sequence : sequences){
			// Si la séquence n'a plus le même ID, on vide la map.
			if(lastSequence == null || sequence.getId() != lastSequence.getId()){  //  NEW PHILIPPE OCT-08
				alreadyCountedForSequenceID.clear(); 
				lastSequence = sequence;
			}
			
			for(int i=0; i< sequence.size(); i++){
				for(int j=0; j < sequence.getSizeOfItemsetAt(i); j++){
					Item item = sequence.getItemAtInItemsetAt(j, i);
					if(isTheMinAndMaxIntervalRespected(sequence.getTimeStamp(i)) // C1 C2 check
						|| sequence.isPostfix(i)){ // seulement si ce n'est pas un postfix
						Pair paire = new Pair(sequence.getTimeStamp(i), sequence.isCutAtRight(i),sequence.isPostfix(i), item);
						Pair oldPaire = mapPaires.get(paire);
						if(!alreadyCountedForSequenceID.contains(paire)){
							if(oldPaire == null){
								mapPaires.put(paire, paire);
							}else{
								paire = oldPaire;
							}
							alreadyCountedForSequenceID.add(paire);
							// On conserve l'ID de la séquence
							paire.getSequencesID().add(sequence.getId());
						}
					}
				}
			}
		}
		return mapPaires.keySet();
	}


	// C1 et C2
	public boolean isTheMinAndMaxIntervalRespected(long itemsetTimestamp){
		return (itemsetTimestamp >= minInterval) && (itemsetTimestamp <= maxInterval);
	}
	
	// C3
	public boolean isMaxWholeIntervalRespected(Sequence sequence){  // OK ????
		return (sequence.get(sequence.size()-1).getTimestamp() <= maxWholeInterval);
	}
	
	// C4
	public boolean isMinWholeIntervalRespected(Sequence sequence){    // OK ???
		return (sequence.get(sequence.size()-1).getTimestamp() >= minWholeInterval);
	}
	
	private PseudoSequenceDatabase[] buildProjectedContext(Item item, 
			PseudoSequenceDatabase contexte, boolean inSuffix, long timestamp) {
		
//		contexte.printContext();	
		
		// Structure pour contenir toutes les séquences projetées.
		PseudoSequenceDatabase sequenceDatabase = new PseudoSequenceDatabase();

		// Contrairement à prefixspan il faut créer toutes les sous-séquences possibles, dans la base projetée
		// à cause des timestamps. Pour cette raison on va scanner toutes les séquences du contexte, et  on
		// va comparer les séquences en partant du dernier itemset jusqu'au premier. À chaque fois que 
		// l'on rencontrera l'itemset utilisé pour la projection et son timestamp, on ajoutera la séquence au
		// contexte projeté (si elle est non vide bien entendu!). Il faut aussi ajuster les timestamps de tout les itemsets
		// des nouvelles séquences qui sont créées.
		for(PseudoSequence sequence : contexte.getPseudoSequences()){
			for(int i =0; i< sequence.size(); i++){ 
				// Si le timestamp correspond pas, on passe...
				if(timestamp != -1 && timestamp != sequence.getTimeStamp(i)){
					continue;
				}
				
				// Si l'itemset contient l'item
				int index = sequence.indexOf(i, item.getId());
				if(index != -1 && sequence.isPostfix(i) == inSuffix){
					if(index != sequence.getSizeOfItemsetAt(i)-1){ //ce n'est pas le dernier item de l'itemset
						PseudoSequence newSequence = new PseudoSequence(sequence.getAbsoluteTimeStamp(i), 
								sequence, i, index+1);
						if(newSequence.size() >0){
							sequenceDatabase.addSequence(newSequence);
						} 
					}else if ((i != sequence.size()-1)){// si ce n'est pas le dernier itemset de la séquence			 
						PseudoSequence newSequence = new PseudoSequence(sequence.getAbsoluteTimeStamp(i), sequence, i+1, 0);
						if(newSequence.size() >0){
							sequenceDatabase.addSequence(newSequence);
						}	
					}	
				}
			}
		}

		return new PseudoSequenceDatabase[]{sequenceDatabase};
	}
	
	private PseudoSequenceDatabase[] buildProjectedContextItemValued(ItemValued item, 
			PseudoSequenceDatabase contexte, boolean inSuffix, long timestamp) {
		
//		contexte.printContext();	
		
		// Structure pour contenir toutes les séquences projetées.
		PseudoSequenceDatabase sequenceDatabase = new PseudoSequenceDatabase();
		List<ItemValued> removedItems = new ArrayList<ItemValued>(); // Pour clustering, on conserve les items enlevés de chaque séquence projetée. On conserve
		// cette information dans une liste ordonnée en terme des séquences trouvées.

		List<ItemValued> removedItemsDestroyed = new ArrayList<ItemValued>(); // on conserve les items enlevés qui ne sont pas dans la liste removedItems.
		
		// Contrairement à prefixspan il faut créer toutes les sous-séquences possibles, dans la base projetée
		// à cause des timestamps. Pour cette raison on va scanner toutes les séquences du contexte, et  on
		// va comparer les séquences en partant du dernier itemset jusqu'au premier. À chaque fois que 
		// l'on rencontrera l'itemset utilisé pour la projection et son timestamp, on ajoutera la séquence au
		// contexte projeté (si elle est non vide bien entendu!). Il faut aussi ajuster les timestamps de tout les itemsets
		// des nouvelles séquences qui sont créées.
//		System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
//		System.out.println("ITEM : " + item  + " " + inSuffix);
//		contexte.printContext();
//		System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
		for(PseudoSequence sequence : contexte.getPseudoSequences()){
			for(int i =0; i< sequence.size(); i++){ 
				// Si le timestamp correspond pas, on passe...
				if(timestamp != -1 && timestamp != sequence.getTimeStamp(i)){
					continue;
				}
				
				// Si l'itemset contient l'item
				int index = sequence.indexOf(i, item.getId());
				if(index != -1 && sequence.isPostfix(i) == inSuffix){
					if(index != sequence.getSizeOfItemsetAt(i)-1){ //ce n'est pas le dernier item de l'itemset
						PseudoSequence newSequence = new PseudoSequence(sequence.getAbsoluteTimeStamp(i), 
								sequence, i, index+1);
						if(newSequence.size() >0){
							sequenceDatabase.addSequence(newSequence);
						}
						removedItems.add((ItemValued)sequence.getItemAtInItemsetAt(index, i)); 	 
					}else if(i == sequence.size()-1){ // si c'est le dernier itemset de la séquence 
						// et dernier item de l'itemset, cela projete une séquence vide.
							removedItemsDestroyed.add((ItemValued)sequence.getItemAtInItemsetAt(index, i));
//							removedItems.add(sequence.getItemAtInItemsetAt(index, i));  // AJOUT PHILIPPE 2 OCT
					}else{// si ce n'est pas le dernier itemset de la séquence			 
						PseudoSequence newSequence = new PseudoSequence(sequence.getAbsoluteTimeStamp(i), sequence, i+1, 0);
						if(newSequence.size() >0){
							sequenceDatabase.addSequence(newSequence);
						}
						removedItems.add((ItemValued)sequence.getItemAtInItemsetAt(index, i)); 	
					}	
				}
			}
		}

		return breakInClusters(item, contexte, sequenceDatabase, removedItems, removedItemsDestroyed);
	}
	

	
	private PseudoSequenceDatabase[] breakInClusters(ItemValued item,
			PseudoSequenceDatabase contexte, PseudoSequenceDatabase sequenceDatabase,
			List<ItemValued> removedItems, List<ItemValued> removedItemsDestroyed) {
		// Si l'item pour faire la projection est une action à valeur entière et que le nombre de
		// séquences projetées est supérieure à zéro, on brise le contexte en 1 ou plusieurs contexte
		// en fonction des clusters.
		if(removedItems.size() == 0 &&
			removedItemsDestroyed.size() ==0) { // no clustering
			return new PseudoSequenceDatabase[]{sequenceDatabase};
		}

		PseudoSequenceDatabase[] sequenceDatabases;
		if(sequenceDatabase.getSequenceIDs().size() >= (minsuppRelative *2)){
			sequenceDatabases = createSequenceDatabasesByClusters(sequenceDatabase, removedItems);
		}else{ // Sinon on retourne un seul contexte.
			sequenceDatabases = new PseudoSequenceDatabase[]{sequenceDatabase};
			Cluster cluster = new Cluster(removedItems, removedItemsDestroyed);
			cluster.addItems(removedItemsDestroyed);
			cluster.computeHigherAndLower();
			sequenceDatabase.setCluster(cluster);
		}
//		------------------------------------------------------
		// Extra step: Compute support for each cluster from sequences of the initial database taken
		// as parameter (instread of the projected context, which would be wrong). This could
		// probably be optimized by combining the first loop with this method.
		findSequencesContainingClusters(contexte, sequenceDatabases, item); // peut être optimisé
		return sequenceDatabases;
	}
	
	private void findSequencesContainingClusters(PseudoSequenceDatabase contexte, PseudoSequenceDatabase[] sequenceDatabases, ItemValued item) {
		// Recouvre la liste des clusters
		Cluster[] clusters = new Cluster[sequenceDatabases.length];
		for(int i=0; i< sequenceDatabases.length; i++){

			clusters[i] = sequenceDatabases[i].getCluster();
			clusters[i].setSequenceIDs(new HashSet<Integer>());
		}
		
		Set<Cluster> alreadyCounted = new HashSet<Cluster>(); // il faut compter un item qu'une fois par séquence.
		PseudoSequence lastSequence = null;

		for(PseudoSequence sequence : contexte.getPseudoSequences()){
			if(lastSequence == null || lastSequence.getId() != sequence.getId()){ //  NEW PHILIPPE OCT-08
				alreadyCounted.clear(); 
				lastSequence = sequence;
			}
			for(int i=0; i< sequence.size(); i++){
				for(int j=0; j< sequence.getSizeOfItemsetAt(i); j++){
					Item item2 = sequence.getItemAtInItemsetAt(j, i);
					if(item2.getId() == item.getId()){  
						Cluster cluster = findClusterContainingItem(clusters, (ItemValued)item2);
						if(cluster != null && !alreadyCounted.contains(cluster)){
							cluster.getSequenceIDs().add(sequence.getId());
							alreadyCounted.add(cluster); 
						}
					}
				}
			}
		}
	}
	
	private Cluster findClusterContainingItem(Cluster[] clusters, ItemValued item2) {
		for(Cluster cluster : clusters){
			if(cluster.containsItem(item2)){
				return cluster;
			}
		}
		return null;
	}

	/**
	 *  Prend les séquences projetées et les sépare en différents contextes en fonction de clusters. 
	 *  Cette méthode suppose que les items sont des actions à valeur entières.
	 * @param newContexte Un contexte
	 * @param removedItems Les items enlevés à chaque séquence.
	 * @return Un ou plusieurs contexte
	 */
	private PseudoSequenceDatabase[] createSequenceDatabasesByClusters(
			PseudoSequenceDatabase database, List<ItemValued> removedItems) {

		// On associe des numéro de séquence à chaque item pour s'assurer que les clusters fréquents
		// retournés ne comptent pas plusieurs fois des items provenant de séquences avec le même ID.
		for(int i=0; i< removedItems.size(); i++){
			removedItems.get(i).setSequenceID(database.getPseudoSequences().get(i).getId());
		}
		
		List<Cluster> clusters = algoClustering.runAlgorithm(removedItems);
		
		// create a sequenceDatabase for each cluster
		PseudoSequenceDatabase[] sequenceDatabases = new PseudoSequenceDatabase[clusters.size()];
		// For each sequence, assign it to a sequenceDatabase based on the clusters found.
		for(int i=0; i< database.size(); i++){
//			System.out.println("i " + i);
			ItemValued item = removedItems.get(i);
//			if(item.getCluster() != null){ // this check may be unnecessary
				int clusterIndex = clusters.indexOf(item.getCluster());
				if(clusterIndex == -1){  // NOT SURE IF THIS IS CORRECT -- 2010  ADDED THIS TO FIX A PROBLEM
					continue;
				}
				if(sequenceDatabases[clusterIndex] == null){
					sequenceDatabases[clusterIndex] = new PseudoSequenceDatabase();
					sequenceDatabases[clusterIndex].setCluster(clusters.get(clusterIndex));
				}
				sequenceDatabases[clusterIndex].addSequence(database.getPseudoSequences().get(i));
//			}
		}
		
//		for(PseudoSequenceDatabase databasex : sequenceDatabases){
//			if(databasex == null){
//				System.currentTimeMillis(); // DEBUG
//			}
//		}
		return sequenceDatabases;
	}

	// This method takes as parameters : a sequence, an item, and the item support.
	// It creates a copy of the sequence and add the item to the sequence. It sets the 
	// support of the sequence as the support of the item.
	private Sequence appendItemToSequence(Sequence prefix, Item item, long timestamp) {
		Sequence newPrefix = prefix.cloneSequence();  // isSuffix
		long decalage = newPrefix.get(newPrefix.size()-1).getTimestamp();
		newPrefix.addItemset(new Itemset(item, timestamp + decalage));  // créé un nouvel itemset   + decalage
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
	
	public void printStatistics(int objectsCount) {
		StringBuffer r = new StringBuffer(200);
		r.append("=============  Algorithm - STATISTICS =============\n Total time ~ ");
		r.append(System.currentTimeMillis() - startTime);
		r.append(" ms\n");
		r.append(" Frequent sequences count : ");
		r.append(patterns.sequenceCount);
		r.append('\n');
		r.append(patterns.toString(objectsCount));
		r.append("===================================================\n");
		System.out.println(r.toString());
	}
	
	@Override
	public double getMinSupp() {
		return minsupp;
	}

	public int getMinsuppRelative() {
		return minsuppRelative;
	}

	
}
