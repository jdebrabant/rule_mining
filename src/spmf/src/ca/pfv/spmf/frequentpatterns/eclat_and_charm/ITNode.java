package ca.pfv.spmf.frequentpatterns.eclat_and_charm;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class ITNode {
	
	private Itemset itemset;
	private  Set<Integer> tidset;
	
	private ITNode parent = null;
	private List<ITNode> childNodes = new ArrayList<ITNode>();
	
	public ITNode(Itemset itemset){
		this.itemset = itemset;
	}

	public Itemset getItemset() {
		return itemset;
	}

	public void setItemset(Itemset itemset) {
		this.itemset = itemset;
	}

	public Set<Integer> getTidset() {
		return tidset;
	}

	public void setTidset(Set<Integer> tidset) {
		this.tidset = tidset;
	}

	public List<ITNode> getChildNodes() {
		return childNodes;
	}

	public void setChildNodes(List<ITNode> childNodes) {
		this.childNodes = childNodes;
	}

	public ITNode getParent() {
		return parent;
	}

	public void setParent(ITNode parent) {
		this.parent = parent;
	}
	
	// for charm
	public void replaceInChildren(Itemset replacement) {
		for(ITNode node : getChildNodes()){
			Itemset itemset  = node.getItemset();
			// could be optimized... not very efficient..
			// in particular, instead of using a list in itemset, we could use a set.
			for(Integer item : replacement.getItems()){
				if(!itemset.getItems().contains(item)){
					itemset.addItem(item);
				}
			}
			node.replaceInChildren(replacement);
		}
	}



}
