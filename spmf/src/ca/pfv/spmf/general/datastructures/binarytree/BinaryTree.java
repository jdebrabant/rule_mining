package ca.pfv.spmf.general.datastructures.binarytree;

/**
 * This is an implementation of a "binary tree" based on the chapter 12 of the
 * book: "Introductions to algorithms" by Cormen et al. (2001).
 * Most of the code is based on the pseudo-code from this book.
 * 
 * Elements inserted in the tree have to extend the Comparable class.
 * 
 * The tree provides these operations: add, remove, size, contains, minimum,
 * maximum, popMinimum, popMaximum, higher, lower.
 * 
 * The implementation is copyright 2011 by Philippe Fournier-Viger.
 * 
 * @param <Comparable>
 */
public class BinaryTree<T extends Comparable> {

	private int size = 0; // number of elements currently in the tree
	private Node root = null; // the tree root
	boolean allowSameElementMultipleTimes = true;

	/**
	 * Constructor
	 * 
	 * @param allowSameElementMultipleTimes
	 *            if set to true, this allows the tree to contains the same
	 *            element multiple times. To check if an element is the same,
	 *            this class use the compareTo method
	 */
	public BinaryTree(boolean allowSameElementMultipleTimes) {
		this.allowSameElementMultipleTimes = allowSameElementMultipleTimes;
	}

	public BinaryTree() {
		// TODO Auto-generated constructor stub
	}

	public int size() {
		return size;
	}

	public void add(T element) {
		// create element
		Node z = new Node();
		z.key = element;

		Node y = null;
		Node x = root;

		while (x != null) {
			y = x;
			int compare = z.key.compareTo(x.key);
			if (compare < 0) {
				x = x.left;
			} else {
				if (compare == 0 && !allowSameElementMultipleTimes) {
					return;
				}
				x = x.right;
			}
		}
		z.parent = y;
		if (y == null) { // case of an empty tree
			root = z;
		} else if (z.key.compareTo(y.key) < 0) {
			y.left = z;
		} else {
			y.right = z;
		}

		size++;
	}
	
	public boolean isEmpty(){
		return root == null;
	}

	/**
	 * Remove an element from the tree
	 * 
	 * @param element
	 *            the element to be removed
	 */
	public void remove(T element) {
		// First find the node containing the element.
		Node z = search(root, element);
		if (z == null) { // if the element is not in the tree
			return;
		}

		performDelete(z);
	}

	
//	/**
//	 * Remove all the node from a tree that are lower than a given value
//	 * IMPORTANT!!!!!!! : this method does not update the size of the tree, so
//	 * after calling this method, the size may be wrong!!!
//	 * @param element the element to be removed
//	 */
//	public void removeLowerThan(T element) {
//		// First find the node containing the element.
//		Node z = lowerNode(element);
//		if (z == null) { // if no smaller element in the tree
//			return;
//		}
//		// If the tree has only one node
//		if (z == root) {
//			root = z.right; 
//			return;
//		}
//		// Otherwise, there is two cases
//		if (z == z.parent.left) {	
//			// This is a recursive process.
//			// We will climb the links recursively and remove nodes by changing the
//			// pointers.
//			z.parent.left = z.right;
//			z = z.parent;
//			while (true) {
//				// we go up the right links until we cannot go up anymore
//				Node current = z;
//				while (current.parent != null
//						&& current.parent.right == current) {
//					current = current.parent;
//				}
//				if (current.parent == null) {
//					root = z;
//					break;
//				} else { 
//					current.parent.left = z;
//					z = current.parent;
//					while (z.parent != null && z.parent.left == z) {
//						z = z.parent;
//					}
//				}
//			}
//
//		} else {
//			// This is a recursive process.
//			// We will climb the links recursively and remove nodes by changing the
//			// pointers.
//			boolean first = true;
//			while (true) {
//				// we go up the right links until we cannot go up anymore
//				Node current = z;
//				while (current.parent != null
//						&& current.parent.right == current) {
//					current = current.parent;
//				}
//				// if there is no more parents, the subtree of z become the root
//				if (current.parent == null) {
//					if (first) {
//						root = z.right;
//					} else {
//						root = z;
//					}
//					break;
//				} else { // otherwise, the subtree of z become the left child of
//							// the parent
//					if (first) {
//						current.parent.left = z.right;
//					} else {
//						current.parent.left = z;
//					}
//					z = current.parent;
//					while (z.parent != null && z.parent.left == z) {
//						z = z.parent;
//					}
//				}
//				first = false;
//			}
//		}
//	}

	private void performDelete(Node z) {
		Node y;
		if (z.left == null || z.right == null) {
			y = z;
		} else {
			y = successor(z);
		}

		Node x;
		if (y.left != null) {
			x = y.left;
		} else {
			x = y.right;
		}
		if (x != null) {
			x.parent = y.parent;
		}
		if (y.parent == null) {
			root = x;
		} else if (y.equals(y.parent.left)) {
			y.parent.left = x;
		} else {
			y.parent.right = x;
		}

		if (y != z) {
			z.key = y.key;
		}

		size--;
	}

	private Node successor(Node x) {
		if (x.right != null) {
			return minimum(x.right);
		}
		Node y = x.parent;
		while (y != null && x.equals(y.right)) {
			x = y;
			y = y.parent;
		}
		return y;
	}

	private Node predecessor(Node x) {
		if (x.left != null) {
			return maximum(x.left);
		}
		Node y = x.parent;
		while (y != null && x.equals(y.left)) {
			x = y;
			y = y.parent;
		}
		return y;
	}

	/**
	 * Get the minimum element in the tree and remove it from the tree
	 * 
	 * @return the minimum element in the tree
	 */
	public T popMinimum() {
		if (root == null) {
			return null;
		}
		Node x = root;
		while (x.left != null) {
			x = x.left;
		}
		T value = x.key;
		performDelete(x);
		return value;
	}

	/**
	 * Return the largest element having a value lower than a given element k.
	 */
	public T lower(T k) {
		Node result = lowerNode(k);
		if (result == null) {
			return null;
		} else {
			return result.key;
		}
	}

	/**
	 * Return the node having the largest element having a value lower than a
	 * given element k.
	 */
	private Node lowerNode(T k) {
		Node x = root;
		while (x != null) {
			if (k.compareTo(x.key) > 0) {
				if (x.right != null) {
					x = x.right;
				} else {
					return x;
				}
			} else {
				if (x.left != null) {
					x = x.left;
				} else {
					Node current = x;
					while (current.parent != null
							&& current.parent.left == current) {
						current = current.parent;
					}
					return current.parent;
				}
			}
		}
		return null;
	}

	/**
	 * Return the largest element having a value lower than a given element k.
	 */
	public T higher(T k) {
		Node result = higherNode(k);
		if (result == null) {
			return null;
		} else {
			return result.key;
		}
	}

	/**
	 * Return the node having the largest element having a value higher than a
	 * given element k.
	 */
	private Node higherNode(T k) {
		Node x = root;
		while (x != null) {
			if (k.compareTo(x.key) < 0) {
				if (x.left != null) {
					x = x.left;
				} else {
					return x;
				}
			} else {
				if (x.right != null) {
					x = x.right;
				} else {
					Node current = x;
					while (current.parent != null
							&& current.parent.right == current) {
						current = current.parent;
					}
					return current.parent;
				}
			}
		}
		return null;
	}

	/**
	 * Get the minimum element in the tree
	 * 
	 * @return the minimum element in the tree
	 */
	public T minimum() {
		if (root == null) {
			return null;
		}
		return minimum(root).key;
	}

	private Node minimum(Node x) {
		while (x.left != null) {
			x = x.left;
		}
		return x;
	}

	/**
	 * Get the maximum element in the tree and remove it from the tree
	 * 
	 * @return the maximum element in the tree
	 */
	public T popMaximum() {
		if (root == null) {
			return null;
		}
		Node x = root;
		while (x.right != null) {
			x = x.right;
		}
		T value = x.key;
		performDelete(x);
		return value;
	}

	/**
	 * Get the maximum element in the tree
	 * 
	 * @return the maximum element in the tree
	 */
	public T maximum() {
		if (root == null) {
			return null;
		}
		return maximum(root).key;
	}

	private Node maximum(Node x) {
		while (x.right != null) {
			x = x.right;
		}
		return x;
	}

	/**
	 * Check if an element is contained in the tree
	 * 
	 * @param k
	 *            the element.
	 * @return true if the element is in the tree. Otherwise, false.
	 */
	public boolean contains(T k) {
		return search(root, k) != null;
	}

	/**
	 * Method that search for an element and return the node that contains this
	 * element.
	 * 
	 * @param x
	 *            The node where the search will start.
	 * @param k
	 *            The element to search
	 * @return The node containing the element or null if the element is not in
	 *         the tree.
	 */
	private Node search(Node x, T k) {
		while (x != null && !k.equals(x.key)) {
			if (k.compareTo(x.key) < 0) {
				x = x.left;
			} else {
				x = x.right;
			}
		}
		return x;
	}

	/**
	 * Method toString that returns a string with all the elements in the tree
	 * according to the ascending order. NOTE : could be transformed into a non
	 * recursive algorithm.
	 */
	public String toString() {
		if (root == null) {
			return "";
		}
		return print(root, new StringBuffer()).toString();
	}

	private StringBuffer print(Node x, StringBuffer buffer) {
		if (x != null && x.key != null) {
			print(x.left, buffer);
			buffer.append(x.key + " ");
			print(x.right, buffer);
		}
		return buffer;
	}

	public class Node {
		T key = null;
		Node left = null;
		Node right = null;
		Node parent = null;

		public String toString() {
			StringBuffer buffer = new StringBuffer();
			buffer.append(key.toString());
			if (left != null) {
				buffer.append(" L= " + left.key);
			}
			if (right != null) {
				buffer.append(" R= " + right.key);
			}
			return buffer.toString();
		}
	}

}
