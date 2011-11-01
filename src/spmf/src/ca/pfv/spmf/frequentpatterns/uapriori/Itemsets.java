package ca.pfv.spmf.frequentpatterns.uapriori;
import java.util.ArrayList;
import java.util.List;


/**
 * This class represents a set of itemsets.
 * They are ordered by size. For example, level 1 means itemsets of size 1 (that contains 1 item).
 * @author Philippe Fournier-Viger 
 */
public class Itemsets {
	private final List<List<ItemsetApriori>> levels = new ArrayList<List<ItemsetApriori>>();  // itemset class� par taille
	private int itemsetsCount=0;
	private final String name;
	
	public Itemsets(String name){
		this.name = name;
		levels.add(new ArrayList<ItemsetApriori>()); // We create an empty level 0 by default.
	}
	
	public void printItemsets(){
		System.out.println(" ------- " + name + " -------");
		int patternCount=0;
		int levelCount=0;
		for(List<ItemsetApriori> level : levels){
			System.out.println("  L" + levelCount + " ");
			for(ItemsetApriori itemset : level){
				System.out.print("  pattern " + patternCount + ":  ");
				itemset.printWithoutSupport();
				System.out.print("support :  " + itemset.getSupport());
				patternCount++;
				System.out.println("");
			}
			levelCount++;
		}
		System.out.println(" --------------------------------");
	}
	
	
	
	public void addItemset(ItemsetApriori itemset, int k){
		while(levels.size() <= k){
			levels.add(new ArrayList<ItemsetApriori>());
		}
		levels.get(k).add(itemset);
		itemsetsCount++;
	}

	public List<List<ItemsetApriori>> getLevels() {
		return levels;
	}

	public int getItemsetsCount() {
		return itemsetsCount;
	}
	
}
