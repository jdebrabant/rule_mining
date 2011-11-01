package ca.pfv.spmf.sequentialpatterns.BIDEPlus_saveToFile;

import java.util.HashSet;
import java.util.Set;

/**
 * This class is used by PrefixSpanItemIntervals. It represents, based on Hirate & Yamana
 * a pair of an (1) Item  and (2) a time interval. It is used for calculating the support
 * of item in a database. It contains some other information too.
 * @author Philippe Fournier-Viger 
 */
class Pair{
	private final boolean postfix; // is cut at left
	private final boolean prefix; // is cut at right
	private final Integer item;
	
	// List of the its of all the patterns that contains this one.
	private Set<Integer> sequencesID = new HashSet<Integer>();
	
	Pair(long timestamp, boolean prefix, boolean postfix, Integer item){
		this.postfix = postfix;
		this.prefix  = prefix;
		this.item = item;
	}
	
	// for prefixspan
	Pair( boolean prefix, boolean postfix, Integer item){
		this.postfix = postfix;
		this.prefix = prefix;
		this.item = item;
	}
	
	public boolean equals(Object object){
		Pair paire = (Pair) object;
		if((paire.postfix == this.postfix) 
				&& (paire.prefix == this.prefix)
				&& (paire.item.equals(this.item))){
			return true;
		}
		return false;
	}
	
	public int hashCode()
	{// Ex: 127333,P,X,1  127333,N,Z,2
		StringBuffer r = new StringBuffer();
		r.append((postfix ? 'P' : 'N')); // the letters here have no meanings. they are just used for the hashcode
		r.append((prefix ? 'X' : 'Z')); // the letters here have no meanings. they are just used for the hashcode
		r.append(item);
		return r.toString().hashCode();
	}

	public boolean isPostfix() {
		return postfix;
	}

	public Integer getItem() {
		return item;
	}

	public int getCount() {
		return sequencesID.size();
	}		

	public Set<Integer> getSequencesID() {
		return sequencesID;
	}

	public void setSequencesID(Set<Integer> sequencesID) {
		this.sequencesID = sequencesID;
	}

	public boolean isPrefix() {
		return prefix;
	}
}