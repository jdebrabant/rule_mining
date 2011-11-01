package ca.pfv.spmf.frequentpatterns.zart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TCTableCandidate {

	public final List<List<ItemsetZart>> levels = new ArrayList<List<ItemsetZart>>();  // itemset classé par taille
	
	Map<ItemsetZart, Integer> mapPredSupp = new HashMap<ItemsetZart, Integer>();
	Map<ItemsetZart, Boolean> mapKey = new HashMap<ItemsetZart, Boolean>();
	Map<ItemsetZart, Integer> mapSupport = new HashMap<ItemsetZart, Integer>();
	
	public boolean thereisARowKeyValueIsTrue(int i) {
		for(ItemsetZart c : levels.get(i)){
			if(mapKey.get(c) == true){
				return true;
			}
		}
		return false;
	}

}
