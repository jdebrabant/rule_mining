package ca.pfv.spmf.sequentialpatterns;


public class TestPseudoSequence {

	
	public static void main(String[] args) {
		
		//AB in ABBCA is  ABB. last instance
		PseudoSequence pseudoSequence = createABBCA();
		Sequence prefix = createAB();
		PseudoSequence prefix2 = createA_AB_C();
		PseudoSequence prefix3 = createA_ABA_CB();
//		PseudoSequence prefixCAABC = createCAABC();
//		PseudoSequence prefixCACAC = createCACAC();
//		Sequence prefixCAC = createCAC();
		
		System.out.println("STARTING");
		
		System.out.print("The first instance of  ");
		prefix.print();
		System.out.print("  in sequence  ");
		pseudoSequence.print();
		System.out.print("  is  ");
		pseudoSequence.getFirstInstanceOfPrefixSequence(prefix, prefix.getItemOccurencesTotalCount()).pseudoSequence.print();
		System.out.println();
		
		System.out.print("The last  instance of  ");
		prefix.print();
		System.out.print("  in sequence  ");
		pseudoSequence.print();
		System.out.print("  is  ");
		pseudoSequence.getLastInstanceOfPrefixSequence(prefix,prefix.getItemOccurencesTotalCount()).pseudoSequence.print();
		System.out.println();
		
		System.out.print("The first instance of  ");
		prefix.print();
		System.out.print("  in sequence  ");
		prefix2.print();
		System.out.print("  is  ");
		prefix2.getFirstInstanceOfPrefixSequence(prefix,prefix.getItemOccurencesTotalCount()).pseudoSequence.print();
		System.out.println();
		
		System.out.print("The last  instance of  ");
		prefix.print();
		System.out.print("  in sequence  ");
		prefix2.print();
		System.out.print("  is  ");
		prefix2.getLastInstanceOfPrefixSequence(prefix,prefix.getItemOccurencesTotalCount()).pseudoSequence.print();
		System.out.println();
		
		System.out.print("The first instance of  ");
		prefix.print();
		System.out.print("  in sequence  ");
		prefix3.print();
		System.out.print("  is  ");
		prefix3.getFirstInstanceOfPrefixSequence(prefix,prefix.getItemOccurencesTotalCount()).pseudoSequence.print();
		System.out.println();
		
		System.out.print("The last  instance of  ");
		prefix.print();
		System.out.print("  in sequence  ");
		prefix3.print();
		System.out.print("  is  ");
		prefix3.getLastInstanceOfPrefixSequence(prefix,prefix.getItemOccurencesTotalCount()).pseudoSequence.print();
		System.out.println();
		
		
////		If S= CAABC and SP  = AB   then  LL1 =  second A in CAABC
//		System.out.print("The 0th last-in-last of prefix ");
//		prefix.print();
//		System.out.print("  in sequence  ");
//		prefixCAABC.print();
//		System.out.print("  is  ");
//		prefixCAABC.getIthLastInLastApearanceWithRespectToPrefix(prefix,0).print();
//		System.out.println();
//		
////		 *           If S= CACAC and SP = CAC  then LL1 =  second C in S, 
////		 *                                          LL2 =  second A in S and 
////		 *                                          LL3 = third C in S
//		System.out.print("The 0th last-in-last of prefix ");
//		prefixCAC.print();
//		System.out.print("  in sequence  ");
//		prefixCACAC.print();
//		System.out.print("  is  ");
//		prefixCACAC.getIthLastInLastApearanceWithRespectToPrefix(prefixCAC,0).print();
//		System.out.println();
//		
//		System.out.print("The 1th last-in-last of prefix ");
//		prefixCAC.print();
//		System.out.print("  in sequence  ");
//		prefixCACAC.print();
//		System.out.print("  is  ");
//		prefixCACAC.getIthLastInLastApearanceWithRespectToPrefix(prefixCAC,1).print();
//		System.out.println();
//		
//		System.out.print("The 2th last-in-last of prefix ");
//		prefixCAC.print();
//		System.out.print("  in sequence  ");
//		prefixCACAC.print();
//		System.out.print("  is  ");
//		prefixCACAC.getIthLastInLastApearanceWithRespectToPrefix(prefixCAC,2).print();
//		System.out.println();
	}
	
	private static Sequence createCAC() {
		Itemset itemset1 = new Itemset(new Item(3), 0);
		Itemset itemset2 = new Itemset(new Item(1), 0);
		Itemset itemset3 = new Itemset(new Item(3), 0);
		
		Sequence sequence = new Sequence(1);   
		sequence.addItemset(itemset1);
		sequence.addItemset(itemset2);
		sequence.addItemset(itemset3);
		return sequence;
	}

	private static PseudoSequence createCACAC() {
		Itemset itemset1 = new Itemset(new Item(3), 0);
		Itemset itemset2 = new Itemset(new Item(1), 0);
		Itemset itemset3 = new Itemset(new Item(3), 0);
		Itemset itemset4 = new Itemset(new Item(1), 0);
		Itemset itemset5 = new Itemset(new Item(3), 0);
		
		Sequence sequence = new Sequence(1);   
		sequence.addItemset(itemset1);
		sequence.addItemset(itemset2);
		sequence.addItemset(itemset3);
		sequence.addItemset(itemset4);
		sequence.addItemset(itemset5);
		PseudoSequence pseudoSequence = new PseudoSequence(0, sequence, 0 , 0);
		return pseudoSequence;
	}
	
	private static PseudoSequence createCAABC() {
		Itemset itemset1 = new Itemset(new Item(3), 0);
		Itemset itemset2 = new Itemset(new Item(1), 0);
		Itemset itemset3 = new Itemset(new Item(1), 0);
		Itemset itemset4 = new Itemset(new Item(2), 0);
		Itemset itemset5 = new Itemset(new Item(3), 0);
		
		Sequence sequence = new Sequence(1);   
		sequence.addItemset(itemset1);
		sequence.addItemset(itemset2);
		sequence.addItemset(itemset3);
		sequence.addItemset(itemset4);
		sequence.addItemset(itemset5);
		PseudoSequence pseudoSequence = new PseudoSequence(0, sequence, 0 , 0);
		return pseudoSequence;
	}

	private static PseudoSequence createA_AB() {
		Itemset itemset1 = new Itemset(new Item(1), 0);
		Itemset itemset2 = new Itemset(new Item(1), 0);
		itemset2.addItem(new Item(2));
		
		Sequence sequence = new Sequence(1);  // AB
		sequence.addItemset(itemset1);
		sequence.addItemset(itemset2);
		PseudoSequence pseudoSequence = new PseudoSequence(0, sequence, 0 , 0);
		return pseudoSequence;
	}
	
	private static PseudoSequence createA_AB_C() {
		Itemset itemset1 = new Itemset(new Item(1), 0);
		Itemset itemset2 = new Itemset(new Item(1), 0);
		itemset2.addItem(new Item(2));
		Itemset itemset3 = new Itemset(new Item(3), 0);
		
		Sequence sequence = new Sequence(1);  // AB
		sequence.addItemset(itemset1);
		sequence.addItemset(itemset2);
		sequence.addItemset(itemset3);
		PseudoSequence pseudoSequence = new PseudoSequence(0, sequence, 0 , 0);
		return pseudoSequence;
	}
	
	private static PseudoSequence createA_ABA_CB() {
		Itemset itemset1 = new Itemset(new Item(1), 0);
		Itemset itemset2 = new Itemset(new Item(1), 0);
		itemset2.addItem(new Item(2));
		itemset2.addItem(new Item(1));
		Itemset itemset3 = new Itemset(new Item(3), 0);
		itemset3.addItem(new Item(2));
		
		Sequence sequence = new Sequence(1);  // AB
		sequence.addItemset(itemset1);
		sequence.addItemset(itemset2);
		sequence.addItemset(itemset3);
		PseudoSequence pseudoSequence = new PseudoSequence(0, sequence, 0 , 0);
		return pseudoSequence;
	}
	

	private static Sequence createAB() {
		Itemset itemset1 = new Itemset(new Item(1), 0);
		Itemset itemset2 = new Itemset(new Item(2), 0);
		
		Sequence sequence = new Sequence(1);  // AB
		sequence.addItemset(itemset1);
		sequence.addItemset(itemset2);
		return sequence;
	}

	private static PseudoSequence createABBCA() {
		Itemset itemset1 = new Itemset(new Item(1), 0);
		Itemset itemset2 = new Itemset(new Item(2), 0);
		Itemset itemset3 = new Itemset(new Item(2), 0);
		Itemset itemset4 = new Itemset(new Item(3), 0);
		Itemset itemset5 = new Itemset(new Item(1), 0);
		
		Sequence sequence = new Sequence(1);  // ABBCA 
		sequence.addItemset(itemset1);
		sequence.addItemset(itemset2);
		sequence.addItemset(itemset3);
		sequence.addItemset(itemset4);
		sequence.addItemset(itemset5);
		PseudoSequence pseudoSequence = new PseudoSequence(0, sequence, 0 , 0);
		return pseudoSequence;
	}
}
