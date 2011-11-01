package ca.pfv.spmf.frequentpatterns.dci_closed_optimized;

import java.util.BitSet;

public class BitMatrix {
	
	private BitSet[] matrixItemTIDs;
	private int[] support1item;  // array to keep the support of each item of size 1.

	BitMatrix(int itemCount, int transactionCount){
		support1item = new int[itemCount];
		matrixItemTIDs = new BitSet[itemCount];
		for(int i=0; i< matrixItemTIDs.length; i++){
			matrixItemTIDs[i] = new BitSet(transactionCount);
		}
	}

	public void addTidForItem(Integer item, int bit) {
		matrixItemTIDs[item-1].set(bit, true);
	}

	public int getSupportOfItemFirstTime(int i) {
		support1item[i-1] = matrixItemTIDs[i-1].cardinality();
		return support1item[i-1];
	}
	
	public int getSupportOfItem(int i) {
		return support1item[i-1];
	}

	public BitSet getBitSetOf(Integer i) {
		return matrixItemTIDs[i-1];
	}
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		for(BitSet bitset : matrixItemTIDs){
			buffer.append(bitset.toString());
		}
		return buffer.toString();
    }
}
