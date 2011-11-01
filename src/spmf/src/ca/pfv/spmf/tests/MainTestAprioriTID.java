package ca.pfv.spmf.tests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.frequentpatterns.aprioriTID_saveToMemory.AlgoAprioriTID;
import ca.pfv.spmf.frequentpatterns.aprioriTID_saveToMemory.Context;
import ca.pfv.spmf.frequentpatterns.aprioriTID_saveToMemory.Itemsets;

/**
 * Class to test the AprioriTID algorithm .
 * @author Philippe Fournier-Viger 
 */
public class MainTestAprioriTID {

	public static void main(String [] arg){
		// Loading the binary context
		Context context = new ca.pfv.spmf.frequentpatterns.aprioriTID_saveToMemory.Context();
		try {
			context.loadFile(fileToPath("contextPasquier99.txt"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		context.printContext();
		
		// Applying the AprioriTID algorithm
		AlgoAprioriTID apriori = new AlgoAprioriTID(context);
		Itemsets patterns = apriori.runAlgorithm(0.4);
		patterns.printItemsets(context.size());
		apriori.printStats();
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestAprioriTID.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
