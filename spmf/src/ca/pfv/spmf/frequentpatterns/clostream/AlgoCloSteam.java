package ca.pfv.spmf.frequentpatterns.clostream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.pfv.spmf.frequentpatterns.apriori.ItemApriori;
import ca.pfv.spmf.frequentpatterns.apriori.ItemsetApriori;

/**
 * This is an implementation of the CloStream algorithm as proposed by S.J Yen et al.(2009)
 * in the proceedings of IEA-AIE 2009, pp.773.
 * 
 * @author Philippe Fournier-Viger, 2008
 */
public class AlgoCloSteam {
	
	List<ItemsetApriori> tableClosed = new ArrayList<ItemsetApriori>();
	Map<ItemApriori, List<Integer>> cidListMap = new HashMap<ItemApriori, List<Integer>>();

	public AlgoCloSteam() {
		ItemsetApriori emptySet = new ItemsetApriori();
		emptySet.setTransactioncount(0);
		tableClosed.add(emptySet); // add the empty set in the list of closed sets
	}
	
	public void processNewTransaction(ItemsetApriori transaction){
		Map<ItemsetApriori, Integer> tableTemp = new HashMap<ItemsetApriori, Integer>();
		
		// 02
		tableTemp.put(transaction, 0); 
		
		// 03
		Set<Integer> cidset = new HashSet<Integer>();
		for(ItemApriori item : transaction.getItems()){
			List<Integer> cidlist = cidListMap.get(item);
			if(cidlist != null){
				cidset.addAll(cidlist);
			}
		}
		
		// 04
		for(Integer cid : cidset){
			// 05, 06 create intersection
			ItemsetApriori cti = tableClosed.get(cid);
			ItemsetApriori intersectionS = transaction.intersection(cti);

			// 07
			// check if intersection is in Temp
			boolean found = false;
			for(Map.Entry<ItemsetApriori, Integer> entry : tableTemp.entrySet()){
				if(entry.getKey().isEqualTo(intersectionS)){
					found = true;
					// 07
					ItemsetApriori ctt = tableClosed.get(entry.getValue());
					if(cti.getAbsoluteSupport() > ctt.getAbsoluteSupport()){  // &&&&&&&&&
						//08
						entry.setValue(cid);
					}
					break;
				}
			}
			if(found == false){ // 09
				// 10
				tableTemp.put(intersectionS, cid);
			}
		}
		
		// 13
		for(Map.Entry<ItemsetApriori, Integer> xc : tableTemp.entrySet()){
			ItemsetApriori x = xc.getKey();
			Integer c = xc.getValue();
			ItemsetApriori ctc = tableClosed.get(c);
			
			if(x.isEqualTo(ctc)){
				ctc.increaseTransactionCount();
			}else{ // 15
				tableClosed.add(x);
				x.setTransactioncount(ctc.getAbsoluteSupport()+1);
				// 18
				for(ItemApriori item : transaction.getItems()){
					List<Integer> cidlist = cidListMap.get(item);
					if(cidlist == null){
						cidlist = new ArrayList<Integer>();
						cidListMap.put(item, cidlist);
					}
					cidlist.add(tableClosed.size()-1);
				}
			}
		
		}
	}

	public List<ItemsetApriori> getFrequentClosedItemsets() {
		if(tableClosed.get(0).size() ==0){
			tableClosed.remove(0);  // FIX FOR THE EMPTY SET
		}
		return tableClosed;
	}
}
