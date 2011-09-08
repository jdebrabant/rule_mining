package ca.pfv.spmf.sequentialpatterns.prefixspan;



/**
 * This class represents a sequence from a projected database (as based in PrefixSpan).
 * Since it is a projected sequence, it makes reference to the original sequence.
 * 
 * This class also include several methods for calculating the maximum periods, 
 * semi-maximum perdiods, etc. as required by the BIDE+ algorithm.
 * @author Philippe
 */

public class PseudoSequence {

	private Sequence sequence;
	private int firstItemset;
	private int firstItem;
	private int lastItemset;
	private int lastItem;
	
	public PseudoSequence(PseudoSequence sequence, int indexItemset, int indexItem){

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
	
	public PseudoSequence(PseudoSequence sequence, int indexItemset, int indexItem, int lastItemset, int lastItem){
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
	
	public Integer getItemAtInItemsetAt(int indexItem, int indexItemset){
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
			if(getItemAtInItemsetAt(i, indexItemset) == idItem){
				return i;
			}
		}
		return -1;
	}
	

	
}