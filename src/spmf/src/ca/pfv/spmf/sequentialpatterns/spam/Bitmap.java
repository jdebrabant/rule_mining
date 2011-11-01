package ca.pfv.spmf.sequentialpatterns.spam;

import java.util.BitSet;
/**
 * Implementation of a bitmap for SPAM.
 * 
 * The variable BIT_PER_SECTION indicates the number of bits to used to represent each sequence.
 * By default, this is 32. That means that it assumes that a sequence will not contain more than 32 itemsets.
 * 
 * @author Philippe Fournier-Viger, 2011
 **/
public class Bitmap {
	
	final int BIT_PER_SECTION = 32;  // the number of bit that we use for each sequence
	
	private BitSet bitmap = new BitSet(BIT_PER_SECTION);  // the bitmap
	
	// for calculating the support
	private int lastSID = -1;  // the sid of the last sequence inserted that contains a bit set to true
	private int support = 0;  // the number of bits that are currently set to 1
	
	Bitmap(){
		bitmap = new BitSet(BIT_PER_SECTION); 
	}
	
	private Bitmap(BitSet bitmap){
		this.bitmap = bitmap; 
	}

	public void registerBit(int sid, int tid) {
		if(tid >= BIT_PER_SECTION){
			throw new RuntimeException("This implementation of SPAM does not accept sequence with more than 32 itemsets. See the class Bitmap to change the number of bits and accept more itemsets.");
		}
		int bitIndex = BIT_PER_SECTION*sid+tid;
		bitmap.set(bitIndex, true);
		
		// to update the bit count
		if(sid != lastSID){
			support++;
		}
		lastSID = sid;
	}

	public int getSupport() {
		return support;
	}
	
	public String toString(){
		StringBuffer buffer = new StringBuffer();
		for (int bit = bitmap.nextSetBit(0); bit >= 0; bit = bitmap.nextSetBit(bit+1)) {
			int tid = bit % BIT_PER_SECTION;
			int sid = (bit-tid)/BIT_PER_SECTION;
			buffer.append("[sid=");
			buffer.append(sid);
			buffer.append(" tid=");
			buffer.append(tid);
			buffer.append("]");
		}
		return buffer.toString();
	}

	Bitmap createNewBitmapSStep(Bitmap bitmapItem) {
		BitSet newBitset = new BitSet(lastSID * BIT_PER_SECTION);
		Bitmap newBitmap = new Bitmap(newBitset);
		
		
		// We do an AND with the bitmap of the item
		for (int bitK = bitmap.nextSetBit(0); bitK >= 0; bitK = bitmap.nextSetBit(bitK+1)) {
			
			int sid = bitK/BIT_PER_SECTION;
			int lastBitOfSID = ((sid+1) *BIT_PER_SECTION)-1;
			boolean match = false;
			for (int bit = bitmapItem.bitmap.nextSetBit(bitK+1); bit >= 0 && bit <= lastBitOfSID; bit = bitmapItem.bitmap.nextSetBit(bit+1)) {
				newBitmap.bitmap.set(bit);
				match = true;
			}
			if(match){
				// update the support
				if(sid != newBitmap.lastSID){
					newBitmap.support++;
				}
				newBitmap.lastSID = sid;
			}
			bitK = lastBitOfSID; // to skip the bit from the same sequence
		}
		
//		System.out.println("A =" + toString());
//		System.out.println("B =" + bitmapItem.toString());
//		System.out.println("C =" + newBitmap.toString());
		// We return the resulting bitmap
		return newBitmap;
	}

	Bitmap createNewBitmapIStep(Bitmap bitmapItem) {
		// We create the new bitmap
		BitSet newBitset = new BitSet(lastSID * BIT_PER_SECTION);
		Bitmap newBitmap = new Bitmap(newBitset);
		
		
		// We do an AND with the bitmap of the item
		for (int bit = bitmap.nextSetBit(0); bit >= 0; bit = bitmap.nextSetBit(bit+1)) {
			if(bitmapItem.bitmap.get(bit)){ // if both bits are TRUE
				
				
				// set the bit
				newBitmap.bitmap.set(bit);
				// update the support
				int sid = bit/BIT_PER_SECTION;
				if(sid != newBitmap.lastSID){
					newBitmap.support++;
				}
				newBitmap.lastSID = sid;
			}
		}
		// Then 
		newBitset.and(bitmapItem.bitmap);
		
		// We return the resulting bitmap
		return newBitmap;
	}
	
	

	// 4, 8, 16, 32, 64
//	enum bitmapSize {b4, b8, b16, b32, b64};
	
//	private bitmapSize determineSection(int itemsetCount) {
//		if(itemsetCount <= 4){
//			return bitmapSize.b4;
//		}else if (itemsetCount <= 8){
//			return bitmapSize.b8;
//		}else if (itemsetCount <= 16){
//			return bitmapSize.b16;
//		}else if (itemsetCount <= 32){
//			return bitmapSize.b32;
//		}else if (itemsetCount <= 64){
//			return bitmapSize.b64;
//		}
//		throw new RuntimeException("This implementation does not handle sequences longer than 64 itemsets");
//	}
//	
}
