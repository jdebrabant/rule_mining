package ca.pfv.spmf.tests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import ca.pfv.spmf.sequentialpatterns.AlgoFournierViger08;
import ca.pfv.spmf.sequentialpatterns.SequenceDatabase;

/**
 * Test
 * @author Philippe Fournier-Viger
 */
public class MainTestSequentialPatternMining2 {

	public static void main(String [] arg) throws IOException{    
		// Load a sequence database
		SequenceDatabase sequenceDatabase = new SequenceDatabase(); 
		sequenceDatabase.loadFile(fileToPath("contextSequencesTimeExtended.txt"));
		sequenceDatabase.print();
		
		// Create an instance of the algorithm
		AlgoFournierViger08 algo 
		  = new AlgoFournierViger08(0.55,
				0, 2, 0, 100, null,  true, true);
		
		// execute the algorithm
		algo.runAlgorithm(sequenceDatabase);    
		algo.printStatistics(sequenceDatabase.size());
	}
	
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestSequentialPatternMining2.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}


