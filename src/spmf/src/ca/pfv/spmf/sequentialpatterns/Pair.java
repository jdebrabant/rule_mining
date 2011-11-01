package ca.pfv.spmf.sequentialpatterns;

import java.util.HashSet;
import java.util.Set;


/**
 * This class is used by PrefixSpanItemIntervals. It represents, based on Hirate & Yamana
 * a pair of an (1) Item  and (2) a time interval. It is used for calculating the support
 * of item in a database. It contains some other information too.
 * @author Philippe Fournier-Viger 
 */
public class Pair{
	private final long timestamp; // not used by PrefixSpan
	private final boolean postfix; // is cut at left
	private final boolean prefix; // is cut at right
	private final Item item;
	
	// List of the its of all the patterns that contains this one.
	private Set<Integer> sequencesID = new HashSet<Integer>();
	
	Pair(long timestamp, boolean prefix, boolean postfix, Item item){
		this.timestamp = timestamp;
		this.postfix = postfix;
		this.prefix  = prefix;
		this.item = item;
	}
	
	// for prefixspan
	public Pair( boolean prefix, boolean postfix, Item item){
		this.timestamp = 0; // not used by prefixspan
		this.postfix = postfix;
		this.prefix = prefix;
		this.item = item;
	}
	
	public boolean equals(Object object){
		Pair paire = (Pair) object;
		if((paire.timestamp == this.timestamp) && (paire.postfix == this.postfix) 
				&& (paire.prefix == this.prefix)
				&& (paire.item.equals(this.item))){
			return true;
		}
		return false;
	}
	
	public int hashCode()
	{// Ex: 127333,P,X,1  127333,N,Z,2
		StringBuffer r = new StringBuffer();
		r.append(timestamp);
		r.append((postfix ? 'P' : 'N')); // the letters here have no meanings. they are just used for the hashcode
		r.append((prefix ? 'X' : 'Z')); // the letters here have no meanings. they are just used for the hashcode
		r.append(item.getId());
		return r.toString().hashCode();
	}

	public long getTimestamp() {
		return timestamp;
	}

	public boolean isPostfix() {
		return postfix;
	}

	public Item getItem() {
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