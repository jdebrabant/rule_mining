package ca.pfv.spmf.general.datastructures.triangularmatrix;

/**
 * This class is for creating a triangular matrix of integers.
 * All the elements in the matrix are initialized to zero.
 * For example: 
 * 0: [0, 0, 0, 0]
 * 1: [0, 0, 0]
 * 2: [0, 0]
 * 3: [0]
 * @author Philippe Fournier-Viger
 */
public class TriangularMatrix {
	
	private int[][] matrix;
	private int elementCount;

	public TriangularMatrix(int elementCount){
		this.elementCount = elementCount;
		matrix = new int[elementCount-1][]; // -1 cause we want it shorter of 1 element
		for(int i=0; i< elementCount-1; i++){ // -1 cause we want it shorter of 1 element
		   // allocate an array for each row
			matrix[i] = new int[elementCount - i -1];
		}
	}
	
	public int get(int i, int j){
		return matrix[i][j];
	}
	
	/**
	 * for testing!
	 */
	public static void main(String[] args) {
		TriangularMatrix a = new TriangularMatrix(5);

		System.out.println(a.toString());
		// AB, AD, AE, BD, BE, DE
		a.incrementCount(1, 0);
		System.out.println(a.toString());
		a.incrementCount(1, 4);
		a.incrementCount(1, 3);
		a.incrementCount(2, 4);
		a.incrementCount(2, 4);
		a.incrementCount(4, 3);
		System.out.println(a.toString());
		a.incrementCount(0, 2);
		a.incrementCount(0, 3);
		a.incrementCount(0, 4);
		System.out.println(a.toString());
	}
	
	/**
	 * for testing!
	 */
	public String toString() {
		System.out.println("Element count = " + elementCount);
		StringBuffer temp = new StringBuffer();
		for (int i = 0; i < matrix.length; i++) {
			temp.append(i);
			temp.append(": ");
			for (int j = 0; j < matrix[i].length; j++) {
				temp.append(matrix[i][j]);
				temp.append(" ");
			}
			temp.append("\n");
		}
		return temp.toString();
	}

	public void incrementCount(int id, int id2) {
		if(id2 < id){
			incrementCount(id2, id);  // so that id is always smaller than id2
		}else{
			matrix[elementCount - id2 -1][id]++;
		}
		
		
		
//		if(id == 1){
//			matrix[0][elementCount - id2]++;
//		}else if(id2 ==1){
//			matrix[0][elementCount - id]++;
//		}else{
//			if(id < id2){
//				matrix[id-1][elementCount - id2]++;
//			}else{
//				matrix[id2-1][elementCount - id]++;
//			}
//		}
	}
	
	public int getSupportForItems(int id, int id2){
		if(id2 < id){
			return getSupportForItems(id2, id);  // so that id is always smaller than id2
		}else{
			return matrix[elementCount - id2 -1][id];
		}
//		if(id ==1){
//			return matrix[0][elementCount - id2];
//		}else if(id2 ==1){
//			return matrix[0][elementCount - id];
//		}else{
//			if(id < id2){
//				return matrix[id-1][elementCount - id2];
//			}else{
//				return matrix[id2-1][elementCount - id];
//			}
//		}
	}
}
