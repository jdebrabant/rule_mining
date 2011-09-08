package ca.pfv.spmf.multidimensionalsequentialpatterns;

import java.util.List;
import java.util.Set;

import ca.pfv.spmf.multidimensionalpatterns.AlgoDimApriori;
import ca.pfv.spmf.multidimensionalpatterns.MDPattern;
import ca.pfv.spmf.multidimensionalpatterns.MDPatterns;
import ca.pfv.spmf.multidimensionalpatterns.MDPatternsDatabase;
import ca.pfv.spmf.sequentialpatterns.AbstractAlgoPrefixSpan;
import ca.pfv.spmf.sequentialpatterns.Sequence;
import ca.pfv.spmf.sequentialpatterns.Sequences;

/**
 * Implementation of the SeqDim algorithm.
 * Uses AlgoPrefixspan and the AlgoDim algorithm.
 * Based on the article of Helen Pinto et al. (2001).
 * 
 * @author Philippe Fournier-Viger 
 **/
public class AlgoSeqDim {
	protected MDSequences sequences = new MDSequences("FREQUENT MD-SEQUENCES");
	private long startTime;
//	private int nbPassesBD;
	
	public MDSequences runAlgorithm(MDSequenceDatabase context, AbstractAlgoPrefixSpan algoPrefixSpan, AlgoDimApriori algoDim, boolean mineClosedPatterns) {
//		System.out
//		.println("=============  EXECUTION - DIM =============");
//		contexte.printContext();
//		System.out.println();

		resetStatistics();
		
		// (1) First mine sequential patterns.
		Sequences sequencesFound = algoPrefixSpan.runAlgorithm(context.getSequenceDatabase());   
//		nbPassesBD = algoPrefixSpan.getNbPassesBD();
		

		// (2) For each sequential pattern, form projected MD-Database 
		// and then find MD-patterns within projected databases
		for (int j = 0; j < sequencesFound.getLevelCount(); j++) { 		
			List<Sequence> sequencesList = sequencesFound.getLevel(j);
			for(Sequence sequence : sequencesList){  
				// we have to calculate the minsupp absolute and relative here.
				if(sequence.getSequencesID().size() ==0){
					System.out.println("TEST");
				}

				trySequence(sequence, context, algoPrefixSpan.getMinSupp(), algoDim);
			}
		}

		// (3) Eliminate non-closed multidimensional sequential patterns
		if(mineClosedPatterns){
//			System.out.println("STARTING ELIMINATING MULTI-DIMENSIONAL SEQ. PATTERNS REDUNDANCY");
			removeRedundancy();
//			System.out.println("FINISHED ELIMINATING MULTI-DIMENSIONAL SEQ. PATTERNS REDUNDANCY");
		}
		
		return sequences;
	}
	
	
	
	private void trySequence(Sequence sequence, MDSequenceDatabase contexte, double minsupp, AlgoDimApriori algoDim) {
		// (a) Project the database
		MDPatternsDatabase newContexte = createProjectedDatabase(sequence.getSequencesID(), contexte.getPatternDatabase());
		
//		if(newContexte.getMDPatterns().size() == 0){
//			System.out.println("TEST");
//		}
//		
		
		// (b) Run the DIM algorithm
//		newContexte.printContext();
//		double minsuppRelatif = (int) Math.ceil(algoPrefixSpan.getMinSupp() * contexte.size());
		double newMinSupp = minsupp * contexte.size() / newContexte.size();
		MDPatterns patterns = algoDim.runAlgorithm(newContexte, newMinSupp);
//		patterns.printSequencesFrequentes(4);
		
		// (c) Create MD-Sequences and add them
		for (int i = 0; i < patterns.getLevelCount(); i++) {
			for(MDPattern pattern : patterns.getLevel(i)){
				MDSequence mdsequence = new MDSequence(0, pattern, sequence);
				mdsequence.setTransactioncount(pattern.getAbsoluteSupport());  // TODO : pourrait faire autrement, peut-être!
				sequences.addSequence(mdsequence, sequence.size());
			}
		}
		
	}

	/**
	 * Create a projected database from a set of IDs of MDpatterns to keep
	 * @param patternsIds The list of of MDPatterns to keep
	 * @param context The initial context
	 * @return A new context containing only the MDPatterns to keep.
	 */
	private MDPatternsDatabase createProjectedDatabase(Set<Integer> patternsIds,
			MDPatternsDatabase context) {
		MDPatternsDatabase projectedDatabase = new MDPatternsDatabase();
		for(MDPattern pattern : context.getMDPatterns()){
			if(patternsIds.contains(pattern.getId())){
				projectedDatabase.addMDPattern(pattern);
			}
		}
		return projectedDatabase;
	}

	private void resetStatistics() {
		startTime = System.currentTimeMillis();
//		nbPassesBD = 0;
	}
	
	public void printStatistics(int objectsCount) {
		StringBuffer r = new StringBuffer(140);
		r.append("=============  SEQ-DIM - STATISTICS =============\n Total time ~ ");
		r.append(System.currentTimeMillis() - startTime);
		r.append(" ms\n");
		r.append(" Frequent sequences count : " );
		r.append(sequences.size());
		System.out.println(r.toString());
		sequences.printFrequentSequences(objectsCount);
		System.out.println("===================================================");
	}
	
	/**
	 * Eliminate non-closed multidimensional sequential patterns 
	 * by simply looping and eliminating redundant patterns.
	 * This is necessary if we want to mine closed multi-dim. seq. patterns, because:
	 * closed sequential patt. mining + closed itemset mining != closed multi-dim seq. patt. mining.
	 * REF : Panida Songram, Veera Boonjing and Sarun Intakosum (2006)
	 */
	private void removeRedundancy() {
		MDSequences closedSequences = new MDSequences("CLOSED MD-SEQUENCES");
		
		// Pour chaque MDSéquence
		for(int i=sequences.getLevels().size()-1; i > 0; i--){
			for(MDSequence sequence: sequences.getLevel(i)){
				// On vérifie si la séquence est incluse dans une séquence de taille supérieure ou égale
				boolean incluse = false;
				for(int j=i; j < sequences.getLevels().size() && !incluse; j++){		
					for(MDSequence sequence2: sequences.getLevel(j)){
//						System.out.println("TEST");
//						System.out.println("SEQUENCE : " + sequence.toString());
//						System.out.println("SEQUENCE2 : " + sequence2.toString());
						if(sequence != sequence2 && sequence2.getAbsoluteSupport() == sequence.getAbsoluteSupport() &&
								sequence2.contains(sequence)){
							incluse = true;
							break;
						}
					}
				}	
				if(!incluse){
					closedSequences.addSequence(sequence, i);
				}
			}
		}
		sequences = closedSequences;
		
	}
	
}
