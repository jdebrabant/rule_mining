package ca.pfv.spmf.tests;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;

import ca.pfv.spmf.frequentpatterns.apriori.ItemApriori;
import ca.pfv.spmf.frequentpatterns.apriori.ItemsetApriori;
import ca.pfv.spmf.frequentpatterns.clostream.AlgoCloSteam;

/**
 * Class to test the CloStream algorithm.
 * @author Philippe Fournier-Viger (Copyright 2008)
 */
public class MainTestCloStream {  

	public static void main(String [] arg){
		
		// Creating an instance of the CloStream algorithm
		AlgoCloSteam cloStream = new AlgoCloSteam();
		
		// Now we add 5 transactions
		long startTime = System.currentTimeMillis();
		ItemsetApriori transaction0 = new ItemsetApriori();
		transaction0.addItem(new ItemApriori(1));
		transaction0.addItem(new ItemApriori(3));
		transaction0.addItem(new ItemApriori(4));
		cloStream.processNewTransaction(transaction0);
		
		ItemsetApriori transaction1 = new ItemsetApriori();
		transaction1.addItem(new ItemApriori(2));
		transaction1.addItem(new ItemApriori(3));
		transaction1.addItem(new ItemApriori(5));
		cloStream.processNewTransaction(transaction1);
		
		ItemsetApriori transaction2 = new ItemsetApriori();
		transaction2.addItem(new ItemApriori(1));
		transaction2.addItem(new ItemApriori(2));
		transaction2.addItem(new ItemApriori(3));
		transaction2.addItem(new ItemApriori(5));
		cloStream.processNewTransaction(transaction2);
		
		ItemsetApriori transaction3 = new ItemsetApriori();
		transaction3.addItem(new ItemApriori(2));
		transaction3.addItem(new ItemApriori(5));
		cloStream.processNewTransaction(transaction3);

		ItemsetApriori transaction4 = new ItemsetApriori();
		transaction4.addItem(new ItemApriori(1));
		transaction4.addItem(new ItemApriori(2));
		transaction4.addItem(new ItemApriori(3));
		transaction4.addItem(new ItemApriori(5));
		cloStream.processNewTransaction(transaction4);
		
		// We print the patterns found
		List<ItemsetApriori> list = cloStream.getFrequentClosedItemsets();
		System.out.println("Closed itemsets count : " + list.size());
		for(ItemsetApriori itemset : list){
			System.out.println("  " + itemset.toString() + " absolute support : " + itemset.getAbsoluteSupport());
		}

		long endTime = System.currentTimeMillis();
		System.out.println("total Time : " + (endTime - startTime) + "ms");
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestCloStream.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
