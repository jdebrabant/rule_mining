package ca.pfv.spmf.associationrules.pasquier;

import ca.pfv.spmf.frequentpatterns.apriori.ItemsetApriori;
import ca.pfv.spmf.frequentpatterns.apriori.Itemsets;

/**
 * Algorithm 4 from Pasquier et al. 1999 "Efficient mining..."
 * @author Philippe Fournier-Viger 
 */
public class AlgoProperBasisForApproxRulesFromFC {

	private Rules basis = new Rules("Proper Basis for approximative rules");

	public Rules runAlgorithm(Itemsets frequentsClosed, double minconf) {
		System.out
		.println("===  EXECUTION - GENERATING PROPER BASIS FOR APPROX A.RULES  ===");
		
		int k = frequentsClosed.getLevels().size()-1;
		
		for(int i=2; i<= k; i++){
			System.out.println("k =" + i);
			for(ItemsetApriori l : frequentsClosed.getLevels().get(i)){
				for(int j = i-1; j>0; j--){
					for(ItemsetApriori lprime : frequentsClosed.getLevels().get(j)){
						System.out.print(" l = ");
						l.print();
						System.out.print("    l' = ");
						lprime.print();
						if(lprime.includedIn(l)){
							System.out.println("");
							double confidence = ((double)l.getAbsoluteSupport()) 
								/ ((double)lprime.getAbsoluteSupport());
							if(confidence >= minconf){
								System.out.print(" rule : ");
								Rule rule = new Rule(lprime, 
										l.cloneItemSetMinusAnItemset(lprime),
										l.getAbsoluteSupport(), confidence);
								rule.print();
								System.out.println(" found");
								basis.addRule(rule);
							}else{
								System.out.println(" rejected because confidence " + confidence + " is lower than minconf " + minconf);
							}
						}else{
							System.out.println(" rejected, because l' is not included in l");
						}
					}
				}
			}
		}

		return basis;
	}


	public void printStats(int transactionsCount) {
		System.out
				.println("===== GENERATING PB FOR APPROX A.RULES STATISTIQUES =====");
		System.out.println(" Prober basis association rules count : " + basis.getRulesCount()); 
		basis.printRules(transactionsCount);
		System.out
				.println("===================================================");
	}

	public Rules getProperBasisForApproximativeRules() {
		return basis;
	}
}
