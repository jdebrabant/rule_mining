package ca.pfv.spmf.sequentialpatterns;

import java.util.ArrayList;
import java.util.List;


/**
 * This class represents a sequence from a projected database (as based in PrefixSpan).
 * Since it is a projected sequence, it makes reference to the original sequence.
 * 
 * This class also include several methods for calculating the maximum periods, 
 * semi-maximum perdiods, etc. as required by the BIDE+ algorithm.
 * @author Philippe
 */

public class PseudoSequence {

	private long timeShift; //decalageTemps
	private Sequence sequence;
	private int firstItemset;
	private int firstItem;
	private int lastItemset;
	private int lastItem;
	
	public PseudoSequence(long timeShift, PseudoSequence sequence, int indexItemset, int indexItem){
		this.timeShift = timeShift;
		this.sequence = sequence.sequence;
		this.firstItemset = indexItemset + sequence.firstItemset;
		if(this.firstItemset == sequence.firstItemset){
			this.firstItem = indexItem + sequence.firstItem;
		}else{
			this.firstItem = indexItem; // ?????????? NÉCESSAIRE??
		}
		// Last itemset and item (par défaut c'est le dernier item et itemset de la séquence)
		this.lastItemset = sequence.lastItemset;
		this.lastItem = sequence.lastItem;
	}
	
	public PseudoSequence(long timeShift, PseudoSequence sequence, int indexItemset, int indexItem, int lastItemset, int lastItem){
		this.timeShift = timeShift;
		this.sequence = sequence.sequence;
		this.firstItemset = indexItemset + sequence.firstItemset;
		if(this.firstItemset == sequence.firstItemset){
			this.firstItem = indexItem + sequence.firstItem;
		}else{
			this.firstItem = indexItem; // ?????????? necessary??
		}
		// Last itemset and item
		this.lastItemset = lastItemset;
		this.lastItem = lastItem;
	}
	
	public  PseudoSequence(long decalageTemps, Sequence sequence, int indexItemset, int indexItem){
		this.timeShift = decalageTemps;
		this.sequence = sequence;
		this.firstItemset = indexItemset;
		this.firstItem = indexItem;
		// Last itemset and item  (by default, this is the last item & itemset of the sequence.
		this.lastItemset = sequence.size()-1;
		this.lastItem = sequence.getItemsets().get(lastItemset).size()-1;
	}
	
	public int size(){
		int size = sequence.size() - firstItemset - ((sequence.size()-1) - lastItemset);
		if(size == 1 && sequence.getItemsets().get(firstItemset).size() == 0){
			return 0;
		}
		return size;
	}
	
	public int getSizeOfItemsetAt(int index){
		int size = sequence.getItemsets().get(index + firstItemset).size();
		if(isLastItemset(index)){
			size -= ((size -1) - lastItem);
		}
		if(isFirstItemset(index)){
			size -=  firstItem;
		}
		return size;
	}
	
	//return true if this itemset is cut at its right.
	public boolean isCutAtRight(int index){
		if(!isLastItemset(index)){
			return false;
		}
		return (sequence.getItemsets().get(index + firstItemset).size() -1) != lastItem;
	}
	
	//	return true if this itemset is cut at its left.
	public boolean isPostfix(int indexItemset){
		return indexItemset == 0  && firstItem !=0;
	}
	
	public boolean isFirstItemset(int index){
		return index == 0;
	}
	
	public boolean isLastItemset(int index){
		return (index + firstItemset) == lastItemset;
	}
	
	public Item getItemAtInItemsetAt(int indexItem, int indexItemset){
//		if((firstItemset + indexItemset) > lastItemset){// Protection
//			throw new RuntimeException("Out of bound itemset!");
//		}
//		if(isLastItemset(indexItemset)){// Protection
//			if(isFirstItemset(indexItemset) && (firstItem + indexItem) > lastItem){
//				throw new RuntimeException("Out of bound item!");
//			}else if (indexItem > lastItem){
//				throw new RuntimeException("Out of bound item!");
//			}
//		}
		if(isFirstItemset(indexItemset)){
			return getItemset(indexItemset).get(indexItem + firstItem);
		}else{
			return getItemset(indexItemset).get(indexItem);
		}
	}
	
	public long getTimeStamp(int indexItemset){
		return getItemset(indexItemset).getTimestamp() - timeShift;
	}
	
	public long getAbsoluteTimeStamp(int indexItemset){
		return getItemset(indexItemset).getTimestamp();
	}
	
	private Itemset getItemset(int index){
		return sequence.get(index+firstItemset);
	}

	public int getId() {
		return sequence.getId();
	}

	public void print() {
		System.out.print(toString());
	}
	
	public String toString(){
		StringBuffer r = new StringBuffer();
		for(int i=0; i < size(); i++){
			r.append("{t=");
			r.append(getTimeStamp(i));
			r.append(", ");
			for(int j=0; j < getSizeOfItemsetAt(i); j++){
				if(!isLastItemset(i) || (j <= lastItem)){
					r.append(getItemAtInItemsetAt(j, i).toString());
					if(isPostfix(i)){
						r.append('*');
					}
					r.append(' ');
				}
			}
			r.append("}");
		}
		r.append("  ");
		return r.toString();
	}

	public int indexOf(int indexItemset, int idItem) {
		for(int i=0; i < getSizeOfItemsetAt(indexItemset); i++){
			if(getItemAtInItemsetAt(i, indexItemset).getId() == idItem){
				return i;
			}
		}
		return -1;
	}
	
	//-------------------------------- Methods for BIDE+
	// A sequence is not closed if there exist a forward-extension or a backward-extension.
	
	/**
	 * Structure to contains a sequence and list of positions to elements in the sequence.
	 */
	class PseudoSequencePair{
		final PseudoSequence pseudoSequence;
		final List<Position> list;
		public PseudoSequencePair(PseudoSequence pseudoSequence, List<Position> list){
			this.pseudoSequence = pseudoSequence;
			this.list = list;
		}
	}
	
	/**
	 * Internal class representing a position in a pseudo-sequence.
	 * (position of an itemset + position of an item).
	 */
	class Position{
		final int itemset;
		final int item;
		public Position(int itemset, int item){
			this.itemset = itemset;
			this.item = item;
		}
	}
	
	/**
	 * Method that find all instances of a prefix in a sequence S.
	 * The meaning of instance is the one from the BIDE+ article when they tak about
	 * "first instance", "last instance".  Here instead of finding only 
	 * the "first instance" or "last instance", we find all instances and
	 * they also respect the timestamps!
	 */
	public  List<PseudoSequencePair> getAllInstancesOfPrefix(Sequence prefix, int i){
		List<List<Position>> listInstances  
		  = getAllInstancesOfPrefixHelper(prefix, 0, new ArrayList<List<Position>>(), new ArrayList<Position>(), 0, 0);
		// On coupe les instances trouvés selon la taille maximale du préfix cherché.
		List<PseudoSequencePair> allPairs = new ArrayList<PseudoSequencePair>();
		for(List<Position> listPositions : listInstances){
			PseudoSequence newSequence = new PseudoSequence(0, this, this.firstItemset, this.firstItem, 
					listPositions.get(i-1).itemset, listPositions.get(i-1).item);
			allPairs.add(new PseudoSequencePair(newSequence,listPositions));
		}
		return allPairs;
	}
	
	private  List<List<Position>> getAllInstancesOfPrefixHelper(Sequence prefix, int indexItemset, 
			 List<List<Position>> allInstances, List<Position> listPositionsTotal, 
			long decalageTemps, int decalageItemset){

		for(int i=decalageItemset; i< size(); i++){
			boolean firstTime = indexItemset ==0;
			if(!firstTime && getTimeStamp(i)-decalageTemps != prefix.get(indexItemset).getTimestamp()){  // VÉRIFIER DÉCALAGE TEMPS
				continue;
			}
			int indexItem =0;
			List<Position> listPositions = new ArrayList<Position>();
			int iDCourant = prefix.get(indexItemset).get(indexItem).getId();
			
			for(int j=0; j < getSizeOfItemsetAt(i); j++){
				int id = getItemAtInItemsetAt(j, i).getId();
				if(id == iDCourant){// l'item match
					listPositions.add(new Position(i,j));
					if(listPositions.size()+ listPositionsTotal.size()	== prefix.getItemOccurencesTotalCount())  // si on a trouvé tout le préfix
					{
						List<Position> newList = new ArrayList<Position>(listPositionsTotal);
						newList.addAll(listPositions);
						allInstances.add(newList);
					}else if(indexItem+1 >= prefix.get(indexItemset).size()){ // si on a trouvé tout l'itemset
						long decalage = firstTime ? getTimeStamp(i) : decalageTemps;
						
						List<Position> newList = new ArrayList<Position>(listPositionsTotal);
						newList.addAll(listPositions);
						
						if(indexItemset+1 < prefix.size()){ // NOUVEAU POUR RÉGLER UNE EXCEPTION 2008-09  C'EST CORRECT ???
							getAllInstancesOfPrefixHelper(prefix, indexItemset+1, allInstances, newList, decalage, i+1);
						}
					}else{
						indexItem++;
						iDCourant = prefix.get(indexItemset).get(indexItem).getId();
					}
				}
			}
		}
		return allInstances;
	}

	/**
	 * Last instance of a prefix sequence X in a sequence S.
	 * For example, the last instance of AB in ABBCA is  ABB.
	 * Additionnal difficulty : this sequence must respect timestamps!
	 */
	public  PseudoSequencePair getLastInstanceOfPrefixSequence(Sequence prefix, int i){
		List<PseudoSequencePair> list = getAllInstancesOfPrefix(prefix, i);
		// Return the last one
		PseudoSequencePair sequenceRetourPair = list.get(0);
		for(PseudoSequencePair sequencePair : list){
			PseudoSequence sequence = sequencePair.pseudoSequence;
			PseudoSequence sequenceRetour = sequenceRetourPair.pseudoSequence;
			if((sequence.lastItemset > sequenceRetour.lastItemset) ||
					(sequenceRetour.lastItemset == sequence.lastItemset  && sequence.lastItem > sequenceRetour.lastItem)
					){
				sequenceRetourPair = sequencePair;
			}
		}
		return sequenceRetourPair;
	}

	/**
	 * First Instance of the prefix X in a sequence S.
	 * Method to find the first Instance of the prefix sequence X= e1, e2... ei+1 in a sequence S.
	 * Exemple: first instance of AB in the sequence CAABC = CAAB.
	 * Additionnal difficulty : this sequence must respect timestamps!
	 */
	public  PseudoSequencePair getFirstInstanceOfPrefixSequence(Sequence prefix, int i){
		List<PseudoSequencePair> list = getAllInstancesOfPrefix(prefix, i);
		// Return the first one
		PseudoSequencePair sequenceRetourPair = list.get(0);
		for(PseudoSequencePair sequencePair : list){
			PseudoSequence sequence = sequencePair.pseudoSequence;
			PseudoSequence sequenceRetour = sequenceRetourPair.pseudoSequence;
			if((sequence.lastItemset < sequenceRetour.lastItemset) ||
					(sequenceRetour.lastItemset == sequence.lastItemset  && sequence.lastItem < sequenceRetour.lastItem)){
				sequenceRetourPair = sequencePair;
			}
		}
		return sequenceRetourPair;
	}
	
	/**
	 * Get the ith last-in-last appearance with respect to a prefix sequence Sp.
	 * n = size of S
	 * If i == n, it is the last appearance of ei in the last instance of Sp.
	 * If 0 <= i < n, it is the last appearance of ei in the last instance of Sp, and LLi must appear
	 *   before LLi+1.
	 * Example : If S= CAABC and SP  = AB   then  LL0 =  second A in CAABC
	 *           If S= CACAC and SP = CAC  then LL0 =  second C in S, 
	 *                                          LL1 =  second A in S and 
	 *                                          LL2 = third C in S
	 * @param prefix : le prefix
	 * @param i : le ième élément du préfixe.
	 * @return
	 */
	public Position getIthLastInLastApearanceWithRespectToPrefix(Sequence prefix, int i, boolean withTimeStamps){
		// we obtain the last instance:
		// The last instance is a PseudoSequencePair object.
		// It consists of 
		// - the pseudo sequence that is the last instance
		// - the list of positions for each element of prefix in that last instance.
		PseudoSequencePair lastInstancePair = getLastInstanceOfPrefixSequence(prefix, prefix.getItemOccurencesTotalCount());
		
		// IF WE DON'T USE TIMESTAMP THE "ITH LAST IN LAST" IS A LITTLE BIT COMPLICATED TO GET : 
		if(withTimeStamps == false){
			// ith item of prefix id is :
			int iditem = prefix.getIthItem(i).getId();
			
			if(i == prefix.getItemOccurencesTotalCount()-1){
				// return the last occurence of that item:
				for(int j=lastInstancePair.pseudoSequence.size()-1; j>=0; j--){
					for(int k=lastInstancePair.pseudoSequence.getItemset(j).size()-1; k>=0; k--){
						if(lastInstancePair.pseudoSequence.getItemAtInItemsetAt(k, j).getId() == iditem){
							return new Position(j, k);
						}
					}
				}
			}else{
				// return the last before LLi+1
				Position LLiplus1 = getIthLastInLastApearanceWithRespectToPrefix(prefix, i+1, false);
				for(int j=LLiplus1.itemset; j>=0; j--){
					for(int k=lastInstancePair.pseudoSequence.getItemset(j).size()-1; k>=0; k--){
						if(j == LLiplus1.itemset && k >= LLiplus1.item){
							continue;
						}
						if(lastInstancePair.pseudoSequence.getItemAtInItemsetAt(k, j).getId() == iditem){
							return new Position(j, k);
						}
					}
				}
			}
			return null; // should not happen!
		}
		else{
			// IF WE USE TIMESTAMPS, THE "ITH LAST IN LAST" IS SIMPLE TO GET :
			return lastInstancePair.list.get(i);
		}
	}
	


	/**
	 * Get the ith maximum period of a prefix sequence for this sequence S.
	 * The ith maximum period of the prefix Sp in S is : 
	 *  * if 0 < i <= n, it is the piece of sequence between the end of the first instance of prefix e1... ei-1 in S 
	 *    and the ith last-in-last appearance with respect to prefix Sp
	 *  * if i = 0, it is the piece of sequence in S located before the first last-in-last appearance with respect to prefix Sp.
	 *   Example1:  if S = ABCB  and Sp = AB
	 *   	the 1th semi-period of Sp in S is empty
	 *      the 2th semi-period of Sp in S is BC
	 *   Example2:  if S = ABBB  and Sp = BB
	 *   	the 1th semi-period of Sp in S is AB
	 *      the 2th semi-period of Sp in S is B
	 */
	public PseudoSequence getIthMaximumPeriodOfAPrefix(Sequence prefix, int i, boolean withTimeStamps){
		if(i == 0){ //it is the piece of sequence in S located before the first last-in-last appearance with respect to prefix Sp.
			Position ithlastlast = getIthLastInLastApearanceWithRespectToPrefix(prefix, 0, withTimeStamps);
			return trimBeginingAndEnd(null, ithlastlast);
		}
		
		// ELSE it is the piece of sequence between the end of the first instance of prefix e1... ei-1 in S 
		// and the ith last-in-last appearance with respect to prefix Sp
		// Important : We thus have to cut the prefix at ei-1  (short prefix = e1 ... ei-1). 
		//      It is because the parameter i is used by getLastInstanceOfPrefixSequence(...). (???)
		PseudoSequencePair firstInstance = this.getFirstInstanceOfPrefixSequence(prefix, i); 
		Position lastOfFirstInstance = firstInstance.list.get(i-1); 
		
		Position ithlastlast = this.getIthLastInLastApearanceWithRespectToPrefix(prefix, i, withTimeStamps);
		return trimBeginingAndEnd(lastOfFirstInstance, ithlastlast);
	}
	
	// NEW09 ---------- for the algorithm with timestamps
	public List<PseudoSequence> getAllIthMaxPeriodOfAPrefix(Sequence prefix,
			int i, boolean b) {
		if(i == 0){ 
			List<PseudoSequence> periods = new ArrayList<PseudoSequence>();
			for(PseudoSequencePair instance: getAllInstancesOfPrefix(prefix, prefix.getItemOccurencesTotalCount())){
				PseudoSequence period = trimBeginingAndEnd(null, instance.list.get(0));
				periods.add(period);
			}
			return periods;
		}
		
		List<PseudoSequence> periods = new ArrayList<PseudoSequence>();
		for(PseudoSequencePair instance: getAllInstancesOfPrefix(prefix, i)){
			PseudoSequence period = trimBeginingAndEnd(instance.list.get(i-1), instance.list.get(i));
			periods.add(period);
		}
		return periods;
	}

	
	
	
	
	
	
	
	/**
	 * Method that is used by the one above.
	 * This method cut a sequence by removing some part at the begining and at the end.
	 * IMPORTANT : this method assumes that the sequence has never been cut.
	 * This simplify what this method has to do to handle time.
	 * 
	 * @return null If the result is an empty sequence.!
	 */
	public PseudoSequence trimBeginingAndEnd(Position positionStart, Position positionEnd){
		int itemsetStart = 0;
		int itemStart =0;
		int itemsetEnd=lastItemset;
		int itemEnd=lastItem;
		long newTimeStamp = 0;
		
		if(positionStart != null){  // Coupe du debut
			itemsetStart = positionStart.itemset;
			itemStart =  positionStart.item + 1;
			if(itemStart == getSizeOfItemsetAt(itemsetStart)){
				itemsetStart++;
				itemStart =0;
			}
			if(itemsetStart == size()){// the resulting sequence is empty!
				return null;
			}
			newTimeStamp = getTimeStamp(itemsetStart);
		}
		
		if(positionEnd != null){ // We cut the right part
			itemsetEnd = positionEnd.itemset;
			itemEnd = positionEnd.item -1;
			if(itemEnd<0){
				itemsetEnd--;
				if(itemsetEnd < itemsetStart){
					return null;
				}
				itemEnd = getSizeOfItemsetAt(itemsetEnd)-1;
			}
		}
		// Check if the end is not before the beginning of the sequence!
		if(itemsetEnd == itemsetStart && itemEnd < itemStart){ 
			return null;
		}
		return new PseudoSequence(newTimeStamp, this, itemsetStart, itemStart, itemsetEnd, itemEnd);
	}

	public long getTimeShift() {
		return timeShift;
	}

	public long getTimeSucessor() {
		
		int positionLastElement = size()-1;
		int absolutePositionLastElement = size()-1 + firstItemset;
		//firstItem ==0
//		lastIteml
		if(isCutAtRight(positionLastElement)  // NEW PHIL 2008-09
		){ //if the last itemset is cut at the right
			return getAbsoluteTimeStamp(positionLastElement);
		}else if(absolutePositionLastElement < sequence.size()-1){ // if the last itemset is not the last itemset of the original sequence.
			return sequence.get(absolutePositionLastElement+1).getTimestamp();
		}
		return 0; // if it is the last itemset of the original sequence and it was not cut.
	}
	
	public long getTimePredecessor() {  // NOUVEAU 2008-09-11
		if(firstItemset ==0){
			return 0;
		}
		if(firstItem ==0){
			return getAbsoluteTimeStamp(-1);
		}else{
			return getAbsoluteTimeStamp(0);
		}
	}

	//----------------------- Backscan search space pruning  sept. 2009 updated
	
	/**
	 * Get the ith first-in-last appearance with respect to a prefix sequence Sp.
	 * n = size of S
	 * If i == n, it is the last appearance of ei in the first instance of Sp.
	 * If 0 <= i < n, it is the last appearance of ei in the first instance of Sp, and LFi must appear
	 *   before LFi+1.
	 * @param prefix : the prefix
	 * @param i : the ième element of the prefix.
	 * @return
	 */
	public Position getIthLastInFirstApearanceWithRespectToPrefix(Sequence prefix, int i, boolean withTimestamps){
		// First, we get the first instance.
		// The first instance is a PseudoSequencePair object.
		// It consists of 
		// - the pseudosequence that is the first instance
		// - the list of positions for each element of prefix in that first instance.
		PseudoSequencePair firstInstancePair = getFirstInstanceOfPrefixSequence(prefix, prefix.getItemOccurencesTotalCount());
		
		// IF WE DON'T USE TIMESTAMP THE "ITH LAST IN LAST" IS A LITTLE BIT COMPLICATED TO GET : 
		if(withTimestamps == false){
			// ith item of prefix id is :
			int iditem = prefix.getIthItem(i).getId();
			
			if(i == prefix.getItemOccurencesTotalCount()-1){
				// return the last occurence of that item:
				for(int j=firstInstancePair.pseudoSequence.size()-1; j>=0; j--){
					for(int k=firstInstancePair.pseudoSequence.getItemset(j).size()-1; k>=0; k--){
						if(firstInstancePair.pseudoSequence.getItemAtInItemsetAt(k, j).getId() == iditem){
							return new Position(j, k);
						}
					}
				}
			}else{
				// return the last before LLi+1
				Position LLiplus1 = getIthLastInFirstApearanceWithRespectToPrefix(prefix, i+1, false);
				if(LLiplus1 == null){
					System.out.println("DEBUG");
				}
				for(int j= LLiplus1.itemset; j>=0; j--){
					for(int k=firstInstancePair.pseudoSequence.getItemset(j).size()-1; k>=0; k--){
						if(j == LLiplus1.itemset && k >= LLiplus1.item){
							continue;
						}
						if(firstInstancePair.pseudoSequence.getItemAtInItemsetAt(k, j).getId() == iditem){
							return new Position(j, k);
						}
					}
				}
			}
			return null; // should not happen!
		}
		else{
			return firstInstancePair.list.get(i);
		}
	}
	
	/**
	 * Get the ith semi-maximum period of a prefix sequence for this sequence S.
	 * The ith semi-maximum period of the prefix Sp in S is : 
	 *  * if 0 < i <= n, it is the piece of sequence between the end of the first instance of prefix e1... ei-1 in S 
	 *    and the ith last-in-first appearance with respect to prefix Sp
	 *  * if i = 0, it is the piece of sequence in S located before the first last-in-first appearance with respect to prefix Sp.
	 */
	public PseudoSequence getIthSemiMaximumPeriodOfAPrefix(Sequence prefix, int i, boolean withTimestamps){
				
		if(i == 0){ //it is the piece of sequence in S located before the first last-in-first appearance with respect to prefix Sp.
			Position ithlastfirst = getIthLastInFirstApearanceWithRespectToPrefix(prefix, 0, withTimestamps);
			return trimBeginingAndEnd(null, ithlastfirst);
		}
		
		// ELSE it is the piece of sequence between the end of the first instance of prefix e1... ei-1 in S 
		// and the ith last-in-first appearance with respect to prefix Sp
		/// Important: we have to cut the prefix at ei-1  (short prefix = e1 ... ei-1). 
		//  since the parameter i is used by getLastInstanceOfPrefixSequence(...) and.....(???)
		
		// THIS IS DONE AS FOLLOWS:
		// We get the first instance of prefix e1... ei-1
		PseudoSequencePair firstInstance = this.getFirstInstanceOfPrefixSequence(prefix, i);  // e1... ei-1
		// we get the position of the last item of that first instance
		Position endOfFirstInstance = firstInstance.list.get(i-1); 
		
		// we get the ith last-in-first appearance with respect to prefix Sp
		Position ithlastfirst = this.getIthLastInFirstApearanceWithRespectToPrefix(prefix, i, withTimestamps);
		//we return the piece of sequence between the end of the first instance of prefix e1... ei-1 in S 
		// and the ith last-in-first appearance with respect to prefix Sp
		return trimBeginingAndEnd(endOfFirstInstance, ithlastfirst);
	}

	
}