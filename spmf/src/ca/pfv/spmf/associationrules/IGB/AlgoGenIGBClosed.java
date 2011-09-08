package ca.pfv.spmf.associationrules.IGB;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.pfv.spmf.frequentpatterns.zart.ItemZart;
import ca.pfv.spmf.frequentpatterns.zart.ItemsetZart;
import ca.pfv.spmf.frequentpatterns.zart.TZTableClosed;
/**
 * This is an implementation of the GEN-IGB-FERMES algorithm as described in the article : 
 * "IGB : une nouvelle base générique informative des règles d’association" 
 * dans Information-Interaction-Intelligence (Revue I3), vol. 6, n° 1, Cépaduès-éditions, pp. 31-67, octobre 2006 
 * 
 * This algorithm generates the IGB basis of association rules from the set of frequent closed itemsets,
 * their support and their associated minimal generators. 
 * 
 * @author Philippe Fournier-Viger, 2008
 */

public class AlgoGenIGBClosed {
	private TZTableClosed closedPatternsAndGenerators;
	private RulesIGB rules;
	
	private double minconf;
	private int objectsCount;
	
	public AlgoGenIGBClosed(double minconf){
		this.minconf = minconf;
	}

	public RulesIGB runAlgorithm(TZTableClosed closedPatternsAndGenerators, int nbobjects) {
		this.closedPatternsAndGenerators = closedPatternsAndGenerators;
		rules = new RulesIGB("IGB Basis of association rules");
		this.objectsCount = nbobjects;
		
		//3 For each closed frequent itemset t.
		for(List<ItemsetZart> level : closedPatternsAndGenerators.levels){
			for(ItemsetZart itemset : level){
				if(itemset.size() != 0){
					processItemset(itemset);
				}
			}
		}
		
		return rules;
	}

	private void processItemset(ItemsetZart i) {
		if(i.getRelativeSupport(objectsCount) >= minconf){  // 3
			RuleIGB rule = new RuleIGB(new ItemsetZart(), i, i.getAbsoluteSupport(), i.getRelativeSupport(objectsCount)); // 4,5,6
			rules.addRule(rule); // 7
			return;
		}
		// else
		Set<ItemsetZart> lSmallestPremise = new HashSet<ItemsetZart>(); // 9
		// 10
		for(int j=0; j < i.size(); j++){
			for(ItemsetZart i1 : closedPatternsAndGenerators.levels.get(j)){
				if(((double)i.getAbsoluteSupport() / (double)i1.getAbsoluteSupport()) >= minconf 
						&& i.getItems().containsAll(i1.getItems())){ 
					// 11
					for(ItemsetZart genI1 : closedPatternsAndGenerators.mapGenerators.get(i1)){
						// 12
						boolean thereIsSmaller = false;
						for(ItemsetZart l : lSmallestPremise){
							if(genI1.getItems().containsAll(l.getItems()) && genI1.size() != l.size()){ 
								thereIsSmaller = true; 
								break;
							}
						}
						if(thereIsSmaller ==  false){
							lSmallestPremise.add(genI1);//13
						}
					}
				}
			}
		}
		// 14
		for(ItemsetZart gs : lSmallestPremise){
			// 15, 16, 17
			ItemsetZart i_gs = new ItemsetZart();
			for(ItemZart item : i.getItems()){
				if(!gs.contains(item)){
					i_gs.getItems().add(item);
				}
			}
			RuleIGB rule = new RuleIGB(gs, i_gs, i.getAbsoluteSupport(), (double)i.getAbsoluteSupport() / (double)gs.getAbsoluteSupport());
			rules.addRule(rule); // 18
		}
		
	}
	
}
