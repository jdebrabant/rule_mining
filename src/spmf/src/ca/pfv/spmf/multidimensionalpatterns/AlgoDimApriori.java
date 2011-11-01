package ca.pfv.spmf.multidimensionalpatterns;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import ca.pfv.spmf.frequentpatterns.apriori.AlgoAprioriClose;
import ca.pfv.spmf.frequentpatterns.apriori.ContextApriori;
import ca.pfv.spmf.frequentpatterns.apriori.ItemApriori;
import ca.pfv.spmf.frequentpatterns.apriori.ItemsetApriori;
import ca.pfv.spmf.frequentpatterns.apriori.Itemsets;
import ca.pfv.spmf.frequentpatterns.clostream.AlgoCloSteam;
import ca.pfv.spmf.frequentpatterns.eclat_and_charm.AlgoCharm;
import ca.pfv.spmf.frequentpatterns.eclat_and_charm.Context;
import ca.pfv.spmf.frequentpatterns.eclat_and_charm.Itemset;

/**
 * Class to extract frequent MD-Patterns (multi-dimensional patterns) from a MD-Database.
 * To perform this, we use the AprioriClose or CloStream algorithm.
 * This allow to find all frequent MD-Patterns and those that are closed.
 * We can then keep only the closed one, if necessary.
 * 
 * To proceed,  we (1) convert MD-Patterns in itemsets, (2) mine frequent (closed) itemsets
 * and (3) convert itemsets back in MD-Patterns.
 * @author Philippe Fournier-Viger June 2008
 **/
public class AlgoDimApriori{
	private MDPatterns patterns = new MDPatterns("Frequent MD Patterns");
	private int dimensionsCount;
	
	private boolean findClosedPatterns;
	private boolean findClosedPatternsWithCloStream;
	private boolean findClosedPatternsWithCharm;
	
	/**
	 * Constructor.
	 * @param findClosedPatterns Indicates if this class uses Apriori or Apriori-Close to
	 * find respectively frequent itemsets or frequent closed itemsets.
	 */
	public AlgoDimApriori(boolean findClosedPatterns){
		this.findClosedPatterns = findClosedPatterns;
	}
	
	/**
	 * @param findClosedPatterns Indicates if this class has to find respectively frequent itemsets
	 *  or frequent closed itemsets.
	 * @param Indicates to use CloStreaminstead of AprioriClose to find freq. closed patterns.
	 */
	public AlgoDimApriori(boolean findClosedPatterns, boolean findClosedPatternsWithCloStream,
			boolean findClosedPatternsWithCharm){
		this.findClosedPatterns = findClosedPatterns;
		this.findClosedPatternsWithCloStream = findClosedPatternsWithCloStream;
		this.findClosedPatternsWithCharm = findClosedPatternsWithCharm;
	}
	
	public MDPatterns runAlgorithm(MDPatternsDatabase contexte, double minsupp) {
		patterns = new MDPatterns("FREQUENT MD Patterns");
		
		this.dimensionsCount = contexte.getMDPatterns().get(0).size();

		if(findClosedPatternsWithCloStream){ // CLOSTREAM
			AlgoCloSteam cloStream = new AlgoCloSteam();
			for(MDPattern pattern : contexte.getMDPatterns()){
				cloStream.processNewTransaction(convertPatternToItemset(pattern));
			}
			
			// Convert the patterns found in MDPatterns
			for(ItemsetApriori itemset : cloStream.getFrequentClosedItemsets()){
				// we have to check the support of each patterns found against minsup
				// because CloStream finds ALL closed itemsets without considering minsup.
				if(itemset.getRelativeSupport(contexte.size())>= minsupp){
					MDPattern pattern = convertItemsetToPattern(itemset);
					patterns.addPattern(pattern, pattern.size());
				}
			}
		}else if(findClosedPatternsWithCharm){ // CHARM
			Context contextCharm = new Context();
			for(MDPattern pattern : contexte.getMDPatterns()){
				contextCharm.addItemset( convertPatternToItemsetCharm(pattern));
			}
			AlgoCharm charm = new AlgoCharm(contextCharm, 100000);
			
//			contexte.printContext();
//			contextCharm.printContext();
			ca.pfv.spmf.frequentpatterns.eclat_and_charm.Itemsets frequentPatterns = charm.runAlgorithm(minsupp, true);
			
			// Convert patterns found by Charm into MDPatterns
			for(List<Itemset> itemsets : frequentPatterns.getLevels()){
				for(Itemset itemset : itemsets){
					MDPattern pattern = convertItemsetCharmToPattern(itemset);
					patterns.addPattern(pattern, pattern.size());
				}
			}
		}else{  // APRIORI / APRIORI-CLOSE
		
				// Appel de AprioriClose
			// (1) Create the context for AprioriClose
			ContextApriori context = new ContextApriori();
			for(MDPattern pattern : contexte.getMDPatterns()){
				context.addItemset(convertPatternToItemset(pattern));
			}
			AlgoAprioriClose apriori = new AlgoAprioriClose(context);
	//		context.printContext();
			Itemsets frequentPatterns = apriori.runAlgorithm(minsupp);
			
			if(findClosedPatterns){
				frequentPatterns = apriori.getFrequentClosed();
			}
			
			// Convert patterns found by AprioriClose into MDPatterns
			for(List<ItemsetApriori> itemsets : frequentPatterns.getLevels()){
				for(ItemsetApriori itemset : itemsets){
					MDPattern pattern = convertItemsetToPattern(itemset);
					patterns.addPattern(pattern, pattern.size());
				}
			}
		}
		return patterns;
	}
	


	public Map<Integer, String> mapItemIdIdentifier = new HashMap<Integer,String>();
	public Map<String, Integer> mapIdentifierItemId = new HashMap<String, Integer>();
	int lastUniqueItemIdGiven=0;
	
	public Integer getValueForItemId(ItemApriori value){
		return getValueForItemId(value.getId());
	}
	
	public Integer getValueForItemId(int value){
		String identifier = mapItemIdIdentifier.get(value);
		int index = identifier.indexOf("-");
		return Integer.valueOf(identifier.substring(0, index));
	}
	
	public Integer getDimensionForItemId(ItemApriori value){
		return getDimensionForItemId(value.getId());
	}
	
	public Integer getDimensionForItemId(int value){
		String identifier = mapItemIdIdentifier.get(value);
		int index = identifier.indexOf('-');
		return Integer.valueOf(identifier.substring(index+1, identifier.length()));
	}
	
	public int convertDimensionValueToItemId(int indexDimension, Integer value){
		Integer itemId = mapIdentifierItemId.get("" + value + "-" + indexDimension);
		if(itemId == null){
			itemId = lastUniqueItemIdGiven++;
			StringBuffer identifier = new StringBuffer();
			identifier.append(value);
			identifier.append('-');
			identifier.append(indexDimension);
			mapIdentifierItemId.put(identifier.toString(), itemId);
			mapItemIdIdentifier.put(itemId, identifier.toString());
		}
		return itemId;
	}
	
	private ItemsetApriori convertPatternToItemset(MDPattern pattern) {
		ItemsetApriori itemset = new ItemsetApriori();
		for(int i=0; i < pattern.values.size(); i++){
			itemset.addItem(new ItemApriori(convertDimensionValueToItemId(i, pattern.values.get(i))));
		}
		
		return itemset;
	}
	
	
	private Itemset convertPatternToItemsetCharm(MDPattern pattern) {
		Itemset itemsetCharm = new Itemset();
		for(int i=0; i < pattern.values.size(); i++){
			itemsetCharm.addItem(convertDimensionValueToItemId(i, pattern.values.get(i)));
		}
//		itemsetCharm.transactionId = pattern.getId(); // TODO :  IS THIS CORRECT ??? 2010-10-09  THERE WAS A BUG HERE.
		return itemsetCharm;
	}
	
	private MDPattern convertItemsetToPattern(ItemsetApriori itemset) {
		MDPattern mdpattern = new MDPattern(0);
		for(int i=0; i< dimensionsCount; i++){
			for(int j=0; j<itemset.size(); j++){
				int dimension = getDimensionForItemId(itemset.get(j));
				int value = getValueForItemId(itemset.get(j));
				if(dimension == i){
					mdpattern.addInteger(value);
				}
			}
			if(mdpattern.size() == i){
				mdpattern.addWildCard();
			}
		}
 
		// PATCH FOR SUPPORT...
		mdpattern.setPatternsIDList(new HashSet<Integer>());
		for(int i=0; i< itemset.getAbsoluteSupport(); i++){
			mdpattern.getPatternsID().add(9999+i); // because we don't have the id..
		}
		
		return mdpattern;
	}
	
	private MDPattern convertItemsetCharmToPattern(Itemset itemset) {
		MDPattern mdpattern = new MDPattern(0);
		for(int i=0; i< dimensionsCount; i++){
			for(int j=0; j<itemset.size(); j++){
				Object[] objects = itemset.getItems().toArray();
				int dimension = getDimensionForItemId((Integer)objects[j]);
				int value = getValueForItemId((Integer)objects[j]);
				if(dimension == i){
					mdpattern.addInteger(value);
				}
			}
			if(mdpattern.size() == i){
				mdpattern.addWildCard();
			}
		}
 
		mdpattern.setPatternsIDList(itemset.getTransactionsIds());
		
		return mdpattern;
	}

	public void printStats(int objectsCount) {
		System.out.println("=============  DIM - STATS =============");
		System.out.println(" Frequent patterns count : " + patterns.size()); 
		patterns.printFrequentPatterns(objectsCount);
		System.out.println("===================================================");
	}

}
