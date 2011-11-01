package ca.pfv.spmf.tests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.frequentpatterns.apriori.AlgoAprioriRare;
import ca.pfv.spmf.frequentpatterns.apriori.ContextApriori;
import ca.pfv.spmf.frequentpatterns.apriori.Itemsets;

/**
 * Class to test the AprioriRare algorithm
 * @author Philippe Fournier-Viger (Copyright 2008)
 */
public class MainTestAprioriRare {

	public static void main(String [] arg){
		// Loading a binary context
		ContextApriori context = new ContextApriori();
		try {
			context.loadFile(fileToPath("contextZart.txt"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		context.printContext();
		
		// Applying the APRIORI-RARE algorithm
		AlgoAprioriRare apriori2 = new AlgoAprioriRare(context);
		Itemsets frequents = apriori2.runAlgorithm(0.6);
		frequents.printItemsets(context.size());
		apriori2.getMinimalRareItemsets().printItemsets(context.size());

	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestAprioriRare.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
