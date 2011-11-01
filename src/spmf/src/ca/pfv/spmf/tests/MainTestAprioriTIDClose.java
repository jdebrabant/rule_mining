package ca.pfv.spmf.tests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.frequentpatterns.aprioriTIDClose.AlgoAprioriTIDClose;
import ca.pfv.spmf.frequentpatterns.aprioriTIDClose.Context;
import ca.pfv.spmf.frequentpatterns.aprioriTIDClose.Itemsets;


/**
 * Class to test the AprioriClose algorithm.
 * @author Philippe Fournier-Viger (Copyright 2008)
 */
public class MainTestAprioriTIDClose {

	public static void main(String [] arg){

		long startTime = System.currentTimeMillis();
		
		// Loading the binary context
		Context context = new Context();
		try {
			context.loadFile(fileToPath("contextPasquier99.txt"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		context.printContext();
		
		// Applying the APRIORI-CLOSE algorithm
		AlgoAprioriTIDClose apriori2 = new AlgoAprioriTIDClose(context);
		Itemsets frequents = apriori2.runAlgorithm(0.4);
		
		long endTime = System.currentTimeMillis();
		
		// print the frequent itemsets found
		frequents.printItemsets(context.size());
		
		// print the frequent closed itemsets found
		apriori2.getFrequentClosed().printItemsets(context.size());

		System.out.println("total Time : " + (endTime - startTime) + "ms");
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestAprioriTIDClose.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
