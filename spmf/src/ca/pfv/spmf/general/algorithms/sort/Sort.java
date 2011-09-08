package ca.pfv.spmf.general.algorithms.sort;
/**
 * Implementation of a few sorting algorithms.
 * based on "Introduction to Algorithms" from MIT Press
 * @author Philippe Fournier-Viger 2009
 */
public class Sort {
	
	/**
	 * Implementation of Insertion sort for integers.
	 * This has an average performance of O(n log n)
	 * @param array of integers
	 */
	public static void insertionSort(int [] a){
		for(int j=1; j< a.length; j++){
			int key = a[j];
			int i = j - 1;
			for(; i>=0 && (a[i] > key); i--){
				a[i+1] = a[i];
			}
			a[i+1] = key;
		}
	}
	
	/**
	 * Implementation of Merge sort for integers
	 * @param array of integers
	 * @param p index of first element
	 * @param r index of last element
	 */
	public static void mergeSort(int [] a, int p, int r){
	      if (p < r)
	      {
	    	 int q = (p+r)/ 2;
	         mergeSort(a, p, q);      
	         mergeSort(a, q+1, r); 
	         merge(a, p, q, r);
	      }
	}
	
	private static void merge(int [] a, int p, int q, int r){
		int n1 = q-p+1;
		int n2 = r-q;
		
		int [] tabL = new int[n1+1];
		int [] tabR = new int[n2+1];
		
		for(int i=0; i<n1; i++){
			tabL[i] = a[p+i];  // -1
		}
		for(int j=0; j<n2; j++){
			tabR[j] = a[q+j+1];
		}
		tabL[n1]= Integer.MAX_VALUE;
		tabR[n2]= Integer.MAX_VALUE;
		
		int i =0;
		int j =0;
		for(int k=p; k<r+1;k++){
			if(tabL[i] <= tabR[j]){
				a[k] = tabL[i++];
			}else{
				a[k] = tabR[j++];
			}
		}
	}
	
	/**
	 * Implementation of Bubble sort for integers
	 * @param array of integers
	 */
	public static void bubbleSort(int [] a){
		for(int i=0; i < a.length; i++){
			for(int j= a.length -1; j>= i+1; j--){
				if(a[j] < a[j-1]){
					int temp = a[j];
					a[j] = a[j-1];
					a[j-1] = temp;
				}
			}
		}
	}
	
	
}
