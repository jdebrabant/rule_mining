package ca.pfv.spmf.tests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.frequentpatterns.zart.AlgoZart;
import ca.pfv.spmf.frequentpatterns.zart.ContextZart;
import ca.pfv.spmf.frequentpatterns.zart.ItemsetZart;
import ca.pfv.spmf.frequentpatterns.zart.TFTableFrequent;
import ca.pfv.spmf.frequentpatterns.zart.TZTableClosed;
/**
 * Class for testing the Zart Algorithm
 * @author Philippe Fournier-Viger, 2008
 *
 */
public class MainTestZart {

	public static void main(String[] args) throws IOException {

		long startTime = System.currentTimeMillis();
		
		// Load a binary context
		TZTableClosed results = null;
		TFTableFrequent frequents = null;
		try {
			ContextZart context = new ContextZart();
			context.loadFile(fileToPath("contextZart.txt"));

			long timeStamp = System.currentTimeMillis();
			// Apply the Zart algorithm
			AlgoZart zart = new AlgoZart(0.4);
			results = zart.runAlgorithm(context);
			frequents = zart.getTableFrequent();
			long timeStamp2 = System.currentTimeMillis();
			System.out.println("ZART EXECUTION TIME : " + (timeStamp2 - timeStamp) + " ms");
		} catch (Exception e) {
			e.printStackTrace();
		}
		long endTime = System.currentTimeMillis();
		// PRINTING RESULTS
		// FIRST WE PRINT CLOSED ITEMSETS AND THEIR GENERATORS:
		System.out.println("=================== RESULTS ====================");
		int countClosed=0;
		int countGenerators=0;
		System.out.println("======= List of closed itemsets and their generators ============");
		for(int i=0; i< results.levels.size(); i++){
			System.out.println("LEVEL (SIZE) : " + i);
			for(ItemsetZart closed : results.levels.get(i)){
				System.out.println(" CLOSED : " + closed.toString() + "  supp : " + closed.getAbsoluteSupport());
				countClosed++;
				System.out.println("   GENERATORS : ");
					for(ItemsetZart generator : results.mapGenerators.get(closed)){
						countGenerators++;
						System.out.println("     =" + generator.toString());
					}
			}
		}
		System.out.println(" NUMBER OF CLOSED : " + countClosed +  " NUMBER OF GENERATORS : " + countGenerators );
		
		// SECOND, WE PRINT THE LIST OF ALL FREQUENT ITEMSETS
		System.out.println("======= List of all frequent itemsets ============");
		int countFrequent =0;
		for(int i=0; i< frequents.levels.size(); i++){
			System.out.println("LEVEL (SIZE) : " + i);
			for(ItemsetZart itemset : frequents.levels.get(i)){
				countFrequent++;
				System.out.println(" ITEMSET : " + itemset.toString() + "  supp : " + itemset.getAbsoluteSupport());
			}
		}
		System.out.println("NB OF FREQUENT ITEMSETS : " + countFrequent);
		

		System.out.println("total Time : " + (endTime - startTime) + "ms");
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestZart.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
