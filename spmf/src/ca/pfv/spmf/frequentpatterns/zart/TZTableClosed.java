package ca.pfv.spmf.frequentpatterns.zart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author philippe
 *
 */
public class TZTableClosed {
	
	// TODO : remplacé la variable suivante par Frequent Patterns
	public final List<List<ItemsetZart>> levels = new ArrayList<List<ItemsetZart>>();  // itemset classé par taille
	
	// each entry of the following map is : key : a closed itemset  values : the corresponding generator(s) 
	public Map<ItemsetZart, List<ItemsetZart>> mapGenerators = new HashMap<ItemsetZart, List<ItemsetZart>>();

	public void addClosedItemset(ItemsetZart itemset){
		while(levels.size() <= itemset.size()){
			levels.add(new ArrayList<ItemsetZart>());
		}
		levels.get(itemset.size()).add(itemset);
	}
	
	public List<ItemsetZart> getLevelForZart(int i){
		if(i+1 == levels.size()){
			System.out.println("ERREUR FATALE 2222 !!!!!");
			List<ItemsetZart> newList = new ArrayList<ItemsetZart>();
			levels.add(newList);
			return newList;
		}
		return levels.get(i+1);
	}
}
