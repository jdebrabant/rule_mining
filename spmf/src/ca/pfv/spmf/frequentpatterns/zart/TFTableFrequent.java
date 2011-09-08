package ca.pfv.spmf.frequentpatterns.zart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TFTableFrequent {
	
	public final List<List<ItemsetZart>> levels = new ArrayList<List<ItemsetZart>>();  // itemset classé par taille
	
	Map<ItemsetZart, Integer> mapPredSupp = new HashMap<ItemsetZart, Integer>();
	Map<ItemsetZart, Boolean> mapKey = new HashMap<ItemsetZart, Boolean>();
	Map<ItemsetZart, Boolean> mapClosed = new HashMap<ItemsetZart, Boolean>();
	
	public boolean emptySetIsClosed = false;
	
	public void addFrequentItemset(ItemsetZart itemset){
		while(levels.size() <= itemset.size()){
			levels.add(new ArrayList<ItemsetZart>());
		}
		levels.get(itemset.size()).add(itemset);
	}
	
	public List<ItemsetZart> getLevelForZart(int i){
		if(i+1 == levels.size()){
			System.out.println("ERREUR FATALE 111 !!!!!");
			List<ItemsetZart> newList = new ArrayList<ItemsetZart>();
			levels.add(newList);
			return newList;
		}
		return levels.get(i+1);
	}
}
