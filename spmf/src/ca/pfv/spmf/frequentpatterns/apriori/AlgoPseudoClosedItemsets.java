package ca.pfv.spmf.frequentpatterns.apriori;

/**
 * Finding frequent pseudo-closed itemsets from frequent closed itemsets 
 * and frequent itemsets based on 
 * Pasquier et al. 1999 "Efficient mining..."
 * @author Philippe Fournier-Viger 
 */
public class AlgoPseudoClosedItemsets {

	private Itemsets frequentPseudos = new Itemsets("FREQUENTS PSEUDO-CLOSED");

	public Itemsets runAlgorithm(Itemsets frequents, Itemsets frequentsClosed, int objectsCount) {
		System.out
		.println("====  EXECUTION - GENERATING PSEUDO-CLOSED ITEMSETS  ====");
		resetStats();
		
		System.out.println(" Step 1 : Calculating all Frequent pseudo-closed itemsets");
		// if FC0 is empty, then we add empty set to pseudo-closed
		if(frequentsClosed.getLevels().get(0).isEmpty()){
			ItemsetApriori emptyset = new ItemsetApriori(); 
			//			ensembleVide.setTransactioncount(context.getObjectsCount()); // non nécessaire je pense
			emptyset.setPseudoclose(true);    
			frequentPseudos.addItemset(emptyset, 0); 
			// calculate closure of the emptyset
			calculateClosureForPseudo(emptyset, frequentsClosed, 1); 
		}
		int k = frequents.getLevels().size();
		
		// For each level i=1 à k
		for(int i=1; i<k; i++){
			System.out.println(" Calculating pseudo-closed  for level " + i);
			for(ItemsetApriori itemset : frequents.getLevels().get(i)){
				// We only consider frequent non closed itemsets
				if(!itemset.isClose()){ 
					System.out.print("itemset :"); itemset.print();
					checkIfPseudoClosed(itemset, i);
					if(itemset.isPseudoclose()){
						System.out.println("is pseudo-closed");
						calculateClosureForPseudo(itemset, frequentsClosed, i); 
					}
					System.out.println();
				}
			}
		}

		return frequentPseudos;
	}



	private void calculateClosureForPseudo(ItemsetApriori itemset, Itemsets frequentsClosed, int i) {
		// we have to find the maximal closed in which this item
		// is included.
		for(int j=i+1; j < frequentsClosed.getLevels().size() && itemset.getClosure() == null; j++){
			for(ItemsetApriori fc : frequentsClosed.getLevels().get(j)){
				if(itemset.includedIn(fc)){
					itemset.setClosure(fc);
					break;
				}
			}
		}
	}

	private void checkIfPseudoClosed(ItemsetApriori l, int i) {
		l.setPseudoclose(true);
		for(int j=0; j< i && l.isPseudoclose(); j++){
			System.out.println("itemset : " + i + "   ");l.print();
			if(!(j>=frequentPseudos.getLevels().size())){
				for(ItemsetApriori p : frequentPseudos.getLevels().get(j)){
	//				System.out.println("SIZZZZZE --" + p.getItems().size());
					if(p.includedIn(l) && !p.getClosure().includedIn(l)){
						System.out.print("itemset : ");
						l.print();
						System.out.print(" is not closed because of  : ");
						p.print();
						System.out.print("   which has the closure : ");
						p.getClosure().print();
						System.out.println("");
						l.setPseudoclose(false);
						break;
					}
				}
			}
		}
		if(l.isPseudoclose()){
			frequentPseudos.addItemset(l, i);
		}
	}

	private void resetStats() {

	}

	public void printStatistiques(int nbObjects) {
		System.out
				.println("===== GENERATING PSEUDO-CLOSED =====");
		System.out.println(" Frequent pseudo-closed itemsets count : " + frequentPseudos.getItemsetsCount()); 
		System.out
				.println("===================================================");
	}

	public Itemsets getFrequentPseudos() {
		return frequentPseudos;
	}
}
