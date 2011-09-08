package ca.pfv.spmf.associationrules.MNRRules;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.pfv.spmf.frequentpatterns.zart.ItemsetZart;
import ca.pfv.spmf.frequentpatterns.zart.TZTableClosed;
/**
 * This is an implementation of an algorithm for finding the set of Minimum Non Redundant rules (MNR)
 * from a binary context (see Kryszkiewicz, 98, for details about MNR association rules).
 * 
 * M. Kryszkiewicz. Representative Association Rules. In PAKDD '98: Proceedings
 * of the Second Pacic-Asia Conference on Research and Development in Knowledge 
 * Discovery and Data Mining, pages 198209, London, UK, 1998. Springer-Verlag.
 * 
 * Here, the implementation is based on the description in  Szathmary's thesis (2006). 
 * The algorithm proceed by exploiting the generators and closed itemset found 
 * by the Zart algorithm.
 * @author Philippe Fournier-Viger, 2008
 */

public class AlgoMNRRules {
	private TZTableClosed closedPatternsAndGenerators;
	private RulesMNR rules;
	
	private double minconf;
	
	public AlgoMNRRules(double minconf){
		this.minconf = minconf;
	}

	public RulesMNR runAlgorithm(TZTableClosed closedPatternsAndGenerators) {
		this.closedPatternsAndGenerators = closedPatternsAndGenerators;
		rules = new RulesMNR("MNR association rules");
		
		// 1 - for each equivalence class
		for(Map.Entry<ItemsetZart,List<ItemsetZart>> entryEquivalenceClass : closedPatternsAndGenerators.mapGenerators.entrySet()){
			// get the list of generators
			List<ItemsetZart> listGenerators = entryEquivalenceClass.getValue();
			// if the equivalence class has no generator, then its closed itemset is a generator...
			if(listGenerators.size() == 0  && entryEquivalenceClass.getKey().size() !=0){
				listGenerators.add(entryEquivalenceClass.getKey());
			}
			
			// loop over the generators g of the equivalence class
			for(ItemsetZart generatorG : listGenerators){
				// 3 - find proper supersets of G among the frequent closed itemsets
				Set<ItemsetZart> supersets = new HashSet<ItemsetZart>();
				for(ItemsetZart closedItemset : closedPatternsAndGenerators.mapGenerators.keySet()){
					if(generatorG.size() < closedItemset.size() 
							&& generatorG.includedIn(closedItemset)){
						supersets.add(closedItemset);
					}
				}
				
				// 6 - loop over the supersets found
				for(ItemsetZart closedItemset : supersets){
					ItemsetZart leftSide = generatorG;
					ItemsetZart rightSide = closedItemset.cloneItemSetMinusAnItemset(generatorG);
					calculateSupport(rightSide);
					// left.support = g.support;
					
					double conf = ((double)closedItemset.getAbsoluteSupport()) / ((double)generatorG.getAbsoluteSupport());
//										
					if(conf >= minconf){
						RuleMNR rule = new RuleMNR(leftSide, rightSide, closedItemset.getAbsoluteSupport(), conf);
						rules.addRule(rule);
					}
				}
			}
		}
		
		return rules;
	}

	// This method could be performed more efficiently....
	// I made it very simple.
	private void calculateSupport(ItemsetZart itemsetToTest) {  // THIS WAS CHANGED
		// check if closed
		for(List<ItemsetZart> list : closedPatternsAndGenerators.levels){
			if(list.size() == 0  || list.get(0).size() < itemsetToTest.size()){
				continue; // it is not useful to consider itemsets that are smaller  
				          // than itemsetToTest.size
			}
			for(ItemsetZart itemset : list){
				if(itemsetToTest.includedIn(itemset)){
					itemsetToTest.setTransactioncount(itemset.getAbsoluteSupport());
					return;
				}
			}
		}
	}

}
