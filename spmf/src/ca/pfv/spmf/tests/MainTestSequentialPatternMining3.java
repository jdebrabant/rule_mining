package ca.pfv.spmf.tests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.clustering.kmeans_for_seq_pattern_mining.AlgoKMeans;
import ca.pfv.spmf.clustering.kmeans_for_seq_pattern_mining.AlgoKMeansWithSupport;
import ca.pfv.spmf.sequentialpatterns.AlgoFournierViger08;
import ca.pfv.spmf.sequentialpatterns.SequenceDatabase;

/**
 * Test
 * @author Philippe Fournier-Viger
 */
public class MainTestSequentialPatternMining3 {

	public static void main(String [] arg) throws IOException{    
		// Load a sequence database
		SequenceDatabase sequenceDatabase = new SequenceDatabase(); 
		sequenceDatabase.loadFile(fileToPath("contextSequencesTimeExtended_ValuedItems.txt"));
		sequenceDatabase.print();
		
		// we create the clustering algorithm to be used.
		// we create the clustering algorithm to be used.
		AlgoKMeansWithSupport algoKMeansWithSupport =
			new AlgoKMeansWithSupport(5, 0.50, sequenceDatabase.size(), new AlgoKMeans(5), 1);
		
		// Create an instance of the algorithm
		AlgoFournierViger08 algo 
		  = new AlgoFournierViger08(0.50,
				0, Double.MAX_VALUE, 0, Double.MAX_VALUE, algoKMeansWithSupport,  false, false);
		
		// execute the algorithm
		algo.runAlgorithm(sequenceDatabase);    
		algo.printStatistics(sequenceDatabase.size());
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestSequentialPatternMining3.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}


