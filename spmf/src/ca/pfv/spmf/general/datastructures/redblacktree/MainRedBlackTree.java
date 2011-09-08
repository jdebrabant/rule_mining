package ca.pfv.spmf.general.datastructures.redblacktree;

public class MainRedBlackTree {

	/**
	 * @param args
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		RedBlackTree tree = new RedBlackTree(true);
		tree.add(1);
		tree.add(2);
		tree.add(5);
		tree.add(6);
		tree.add(7);
		tree.add(9);
		tree.add(3);
		tree.add(4);
		System.out.println("all elements : " + tree.toString() + "   Size of tree: " + tree.size());
		System.out.println("... will try to add 5 another time ...");
		tree.add(5); 
		System.out.println("all elements : " + tree.toString() + "   Size of tree: " + tree.size());
		tree.add(500);
		tree.add(501);
		tree.add(100);
		tree.add(101);
		System.out.println("all elements : " + tree.toString() + "   Size of tree: " + tree.size());
		System.out.println("minimum: " + tree.minimum());
		System.out.println("maximum: " + tree.maximum());
		System.out.println("... will remove 7 ");
		tree.remove(7);
		System.out.println("all elements : " + tree.toString() + "   Size of tree: " + tree.size());
		System.out.println("... will remove 2 ");
		tree.remove(2);
		System.out.println("all elements : " + tree.toString() + "   Size of tree: " + tree.size());
		System.out.println("... will remove 5 ");
		tree.remove(5);
		System.out.println("all elements : " + tree.toString() + "   Size of tree: " + tree.size());
		System.out.println("... will remove 5 ");
		tree.remove(5);
		System.out.println("all elements : " + tree.toString() + "   Size of tree: " + tree.size());
		System.out.println("... will add 2 ");
		tree.add(2);
		System.out.println("all elements : " + tree.toString() + "   Size of tree: " + tree.size());
		System.out.println("... will remove 999 ");
		tree.remove(999);
		
		System.out.println("all elements : " + tree.toString() + "   Size of tree: " + tree.size());
		System.out.println(" lower than 5  = " + tree.lower(5));
		System.out.println(" lower than 1 = " + tree.lower(1));
		System.out.println(" lower than 10 = " + tree.lower(10));
		System.out.println(" lower than 8 = " + tree.lower(8));
		System.out.println(" lower than 200 = " + tree.lower(200));
		
		System.out.println("all elements : " + tree.toString() + "   Size of tree: " + tree.size());
		System.out.println(" higher than 5  = " + tree.higher(5));
		System.out.println(" higher than 1 = " + tree.higher(1));
		System.out.println(" higher than 10 = " + tree.higher(10));
		System.out.println(" higher than 8 = " + tree.higher(8));
		System.out.println(" higher than 200 = " + tree.higher(200));

		System.out.println("all elements : " + tree.toString() + "   Size of tree: " + tree.size());
		System.out.println("... will pop maximum ...");
		System.out.println(" maximum " + tree.popMaximum());
		System.out.println("all elements : " + tree.toString() + "   Size of tree: " + tree.size());
		System.out.println("... will pop maximum ...");
		System.out.println(" maximum " + tree.popMaximum());
		System.out.println("all elements : " + tree.toString() + "   Size of tree: " + tree.size());
		System.out.println("... will pop minimum ...");
		System.out.println(" minimum " + tree.popMinimum());
		System.out.println("all elements : " + tree.toString() + "   Size of tree: " + tree.size());

	}

}
