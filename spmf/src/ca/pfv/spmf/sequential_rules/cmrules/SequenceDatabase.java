package ca.pfv.spmf.sequential_rules.cmrules;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import ca.pfv.spmf.clustering.kmeans_for_seq_pattern_mining.Cluster;
/**
 * Implementation of a sequence database. Each sequence should have a unique id.
 * See examples in /test/ directory for the format of input files.
 * 
 * @author Philippe Fournier-Viger
 **/
public class SequenceDatabase {

	// Contexte
	private final List<Sequence> sequences = new ArrayList<Sequence>();

	// for clustering, the last item that was used to do the projection that
	// results in this database.
	private Cluster cluster = null;

	public void loadFile(String path) throws IOException {
		String thisLine;
		BufferedReader myInput = null;
		try {
			FileInputStream fin = new FileInputStream(new File(path));
			myInput = new BufferedReader(new InputStreamReader(fin));
			while ((thisLine = myInput.readLine()) != null) {
				// si la ligne n'est pas un commentaire
				if (thisLine.charAt(0) != '#') {
					// ajoute une séquence
					addSequence(thisLine.split(" "));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				myInput.close();
			}
		}
	}

	public void addSequence(String[] entiers) { //
		Sequence sequence = new Sequence(sequences.size());
		Itemset itemset = new Itemset();
		for (String entier : entiers) {
			if (entier.codePointAt(0) == '<') { // Timestamp
				//String valeur = entier.substring(1, entier.length() - 1);
			} else if (entier.equals("-1")) { // séparateur d'itemsets
				sequence.addItemset(itemset);
				itemset = new Itemset();
			} else if (entier.equals("-2")) { // indicateur de fin de séquence
				sequences.add(sequence);
			} else { // un item au format : id(valeurentiere) ou format : id
				// si l'item à une valeur entière, extraire la valeur
				// extraire la valeur associée à un item
				itemset.addItem(Integer.parseInt(entier));
			}
		}
	}

	public void addSequence(Sequence sequence) {
		sequences.add(sequence);
	}

	public void printContext() {
		System.out.println("============  CONTEXTE ==========");
		for (Sequence sequence : sequences) { // pour chaque objet
			System.out.print(sequence.getId() + ":  ");
			sequence.print();
			System.out.println("");
		}
	}
	
	public void printDatabaseStats() {
		System.out.println("============  STATS ==========");
		System.out.println("Number of sequences : " + sequences.size());
		// average size of sequence
		List<Integer> sizes = new ArrayList<Integer>();
		List<Integer> itemsetsizes = new ArrayList<Integer>();
		List<Integer> differentitems = new ArrayList<Integer>();
		List<Integer> appearXtimesbySequence = new ArrayList<Integer>();
		for(Sequence sequence : sequences){
			sizes.add(sequence.size());
			HashMap<Integer, Integer> mapIntegers = new HashMap<Integer, Integer>();
			for(Itemset itemset : sequence.getItemsets()){
				itemsetsizes.add(itemset.size());
				for(Integer item : itemset.getItems()){
					Integer count = mapIntegers.get(item);
					if(count == null){
						count = 0;
					}
					count = count +1;
					mapIntegers.put(item, count);
				}
			}
			differentitems.add(mapIntegers.entrySet().size());
			
			for(Entry<Integer, Integer> entry: mapIntegers.entrySet()){
				appearXtimesbySequence.add(entry.getValue());
			}
		}
		System.out.println("SIZE mean : " + calculateMean(sizes) 
				+ " stdD: " + calculateStdDeviation(sizes)+
				" var: " + calculateVariance(sizes));
		System.out.println("DIFFERENT ITEMS COUNT BY SEQUENCE mean : " + calculateMean(differentitems) 
				+ " stdD: " + calculateStdDeviation(differentitems)+
				" var: " + calculateVariance(differentitems));
		System.out.println("EACH ITEM APPEAR HOW MANY TIMES BY SEQUENCE mean : " + calculateMean(appearXtimesbySequence) 
				+ " stdD: " + calculateStdDeviation(appearXtimesbySequence)+
				" var: " + calculateVariance(appearXtimesbySequence));
		System.out.println("ITEMSET SIZE mean : " + calculateMean(itemsetsizes) 
				+ " stdD: " + calculateStdDeviation(itemsetsizes)+
				" var: " + calculateVariance(itemsetsizes));
	}
	
	public static double calculateMean(List<Integer> list){
		double sum=0;
		for(Integer val : list){
			sum += val;
		}
		return sum / list.size();
	}
	
	public static double calculateStdDeviation(List<Integer> list){
		double deviation =0;
		double mean = calculateMean(list);
		for(Integer val : list){
			deviation += Math.pow(mean - val, 2);
		}
		return Math.sqrt(deviation / list.size());
	}
	
	public static double calculateVariance(List<Integer> list){
		double deviation =0;
		double mean = calculateMean(list);
		for(Integer val : list){
			deviation += Math.pow(mean - val, 2);
		}
		return Math.pow(Math.sqrt(deviation / list.size()), 2);
	}
	

	public String toString() {
		StringBuffer r = new StringBuffer();
		for (Sequence sequence : sequences) { // pour chaque objet
			r.append(sequence.getId());
			r.append(":  ");
			r.append(sequence.toString());
			r.append('\n');
		}
		return r.toString();
	}

	public int size() {
		return sequences.size();
	}

	public List<Sequence> getSequences() {
		return sequences;
	}

	public Set<Integer> getSequenceIDs() {
		Set<Integer> ensemble = new HashSet<Integer>();
		for (Sequence sequence : getSequences()) {
			ensemble.add(sequence.getId());
		}
		return ensemble;
	}

	// --------------- For clustering
	public Cluster getCluster() {
		return cluster;
	}

	public void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}

	
	public void loadFileSignLanguage(String fileToPath, int i) {
		String thisLine;
		BufferedReader myInput = null;
		try {
			FileInputStream fin = new FileInputStream(new File(fileToPath));
			myInput = new BufferedReader(new InputStreamReader(fin));
			String oldUtterance = "-1";
			Sequence sequence = null;
			int seqid =0;
			while ((thisLine = myInput.readLine()) != null) {
				if(thisLine.length() >= 1 && thisLine.charAt(0) != '#'){
					String []tokens = thisLine.split(" ");
					String currentUtterance = tokens[0];
					if(!currentUtterance.equals(oldUtterance)){
						if(sequence != null){
							sequences.add(sequence);
						}
						sequence = new Sequence(seqid++);
						oldUtterance = currentUtterance;
					}
					for(int j=1; j< tokens.length; j++){
						int character = Integer.parseInt(tokens[j]);
						if(character == -11 || character == -12){
							continue;
						}
						Itemset itemset = new Itemset();
						itemset.addItem(character);
						sequence.addItemset(itemset);
					}
				}
			}
			sequences.add(sequence);
			System.out.println(sequence.toString());
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	// --------------------------- For reading the CSlogs dataset format
	public void loadFileCSlogsFormat(String filepath, int nblinetoread)
			throws IOException {
		String thisLine;
		BufferedReader myInput = null;
		try {
			FileInputStream fin = new FileInputStream(new File(filepath));
			myInput = new BufferedReader(new InputStreamReader(fin));
			int i = 0;
			while ((thisLine = myInput.readLine()) != null) {
				// si la ligne n'est pas un commentaire
				if (thisLine.charAt(0) != '#') {
					// ajoute une séquence
					addSequenceCSLogsFormatV2(thisLine.split(" "));
					i++;
					if (nblinetoread == i) {
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				myInput.close();
			}
		}
	}

	// private void addSequenceCSLogsFormat(String[] integers) {
	// Sequence sequence = new Sequence(sequences.size()); // new sequence with
	// a new sequence id
	// Itemset itemset = new Itemset();
	// for(String integer: integers){
	// if(integer.equals("-1")){ // the -1 separator
	// if(itemset.size() >0){ // we will not add empty itemsets
	// Collections.sort(itemset.getItems()); // sort itemset
	// sequence.addItemset(itemset); // add itemset to sequence
	// itemset = new Itemset();
	// }
	// }else{ // we add the item
	// int item = Integer.parseInt(integer);
	// if(!itemset.contains(item)){ // we don't allow duplicate items in same
	// itemset
	// itemset.addItem(item);
	// }
	// }
	// }
	// sequences.add(sequence);
	// }

	private void addSequenceCSLogsFormatV2(String[] integers) {
		Sequence sequence = new Sequence(sequences.size()); // new sequence with
															// a new sequence id
		for (String integer : integers) {
			if (integer.equals("-1")) { // the -1 separator
			// if(itemset.size() >0){ // we will not add empty itemsets
			// itemset = new Itemset();
			// }
				break; // POUR GARDER UNE SEULE BRANCHE DE L'ARBRE
			} else { // we add the item
				Itemset itemset = new Itemset();
				int item = Integer.parseInt(integer);
				itemset.addItem(item);
				sequence.addItemset(itemset); // add itemset to sequence
			}
		}
		sequences.add(sequence);
	}

	// ---------------------- Pour le fomat généré par seq_data_generator
	public void loadFileBinaryFormat(String path, int maxcount) {
		// TODO Auto-generated method stub
		DataInputStream myInput = null;
		try {
			FileInputStream fin = new FileInputStream(new File(path));
			myInput = new DataInputStream(fin);

			Sequence sequence = new Sequence(sequences.size());
			Itemset itemset = new Itemset();
			while (true) {
				int value = INT_little_endian_TO_big_endian(myInput.readInt());

				// System.out.println(value);
				if (value == -1) {
					sequence.addItemset(itemset);
					itemset = new Itemset();
				} else if (value == -2) {
					sequences.add(sequence);
					if (sequences.size() == maxcount) {
						break;
					}
					sequence = new Sequence(sequences.size());
				} else {
					itemset.addItem(value);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 4-byte number this function was taken from the internet (by Anghel
	// Leonard)
	int INT_little_endian_TO_big_endian(int i) {
		return ((i & 0xff) << 24) + ((i & 0xff00) << 8) + ((i & 0xff0000) >> 8)
				+ ((i >> 24) & 0xff);
	}

	public void loadFileKosarakFormat(String filepath, int nblinetoread)
			throws IOException {
		String thisLine;
		BufferedReader myInput = null;
		try {
			FileInputStream fin = new FileInputStream(new File(filepath));
			myInput = new BufferedReader(new InputStreamReader(fin));
			int i = 0;
			while ((thisLine = myInput.readLine()) != null) {
				// ajoute une séquence
				String[] split = thisLine.split(" ");
				i++;
				if (nblinetoread == i) {
					break;
				}
				Sequence sequence = new Sequence(sequences.size());
				for (String value : split) {
					Itemset itemset = new Itemset();
					itemset.addItem(Integer.parseInt(value));
					sequence.addItemset(itemset);
				}
				sequences.add(sequence);
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				myInput.close();
			}
		}
	}
	
	public void loadFileKosarakFormatV2(String filepath, int nblinetoread)
	throws IOException {
		String thisLine;
		BufferedReader myInput = null;
		try {
			FileInputStream fin = new FileInputStream(new File(filepath));
			myInput = new BufferedReader(new InputStreamReader(fin));
			int i = 0;
			while ((thisLine = myInput.readLine()) != null) {
				// ajoute une séquence
				String[] split = thisLine.split(" ");
				i++;
//				if (nblinetoread == i) {
//					break;
//				}
				if(split.length < 25){
					continue;
				}
				Sequence sequence = new Sequence(sequences.size());
				int count=0;
				Itemset itemset = new Itemset();
				for (String value : split) {
					itemset.addItem(Integer.parseInt(value));
					count++;
					if(count == 3){
						sequence.addItemset(itemset);
						itemset = new Itemset();
						count =0;
					}
				}
				if(sequence.size() >10 ){
					sequences.add(sequence);
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				myInput.close();
			}
		}
	}
	
	public void loadFileWebViewFOrmat(String filepath) {
		String thisLine;
		BufferedReader myInput = null;
		try {
			FileInputStream fin = new FileInputStream(new File(filepath));
			myInput = new BufferedReader(new InputStreamReader(fin));
			int realID = 0;
			int lastId = 0;
			Sequence sequence = null;
			while ((thisLine = myInput.readLine()) != null) {
				// ajoute une séquence
				String[] split = thisLine.split(" ");
				int id = Integer.parseInt(split[0]);
				int val = Integer.parseInt(split[1]);
				
				if(lastId != id){
					if(lastId!=0 ){ //&& sequence.size() >=2
						sequences.add(sequence);
						realID++;
					}
					sequence = new Sequence(realID);
					lastId = id;
				}
				Itemset itemset = new Itemset();
				itemset.addItem(val);
				sequence.addItemset(itemset);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	
	public void loadFileWebViewFOrmatV2(String filepath, int nbLine) {
		String thisLine;
		BufferedReader myInput = null;
		try {
			FileInputStream fin = new FileInputStream(new File(filepath));
			myInput = new BufferedReader(new InputStreamReader(fin));
			int realID = 0;
			int lastId = 0;
			Sequence sequence = null;
			while ((thisLine = myInput.readLine()) != null) {
				// ajoute une séquence
				String[] split = thisLine.split(" ");
				int id = Integer.parseInt(split[0]);
				int val = Integer.parseInt(split[1]);
				
				if(lastId != id){
					if(lastId!=0  && sequence.size() >=5){
						sequences.add(sequence);
						realID++;
					}
					sequence = new Sequence(realID);
					lastId = id;
				}
				Itemset itemset = new Itemset();
				itemset.addItem(val);
				sequence.addItemset(itemset);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	

	public void loadSnakeDataset(String filepath) {
		String thisLine;
		BufferedReader myInput = null;
		try {
			FileInputStream fin = new FileInputStream(new File(filepath));
			myInput = new BufferedReader(new InputStreamReader(fin));
			int realID = 0;
			while ((thisLine = myInput.readLine()) != null) {
				if(thisLine.length() >= 50){
					Sequence sequence = new Sequence(realID++);
					for(int i=0; i< thisLine.length(); i++){
						Itemset itemset = new Itemset();
						int character = thisLine.toCharArray()[i] - 65;
						System.out.println(thisLine.toCharArray()[i] + " " + character);
						itemset.addItem(character);
						sequence.addItemset(itemset);
					}
					sequences.add(sequence);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
//	
//	public void loadFileMSNBCFormat(String filepath, int nblinetoread)
//	throws IOException {
//	String thisLine;
//	BufferedReader myInput = null;
//		try {
//			FileInputStream fin = new FileInputStream(new File(filepath));
//			myInput = new BufferedReader(new InputStreamReader(fin));
//			int i = 0;
//			while ((thisLine = myInput.readLine()) != null) {
//				// ajoute une séquence
//				String[] split = thisLine.split(" ");
//				i++;
//				if (nblinetoread == i) {
//					break;
//				}
//				Sequence sequence = new Sequence(sequences.size());
//				for (String value : split) {
//					Itemset itemset = new Itemset();
//					itemset.addItem(Integer.parseInt(value));
//					sequence.addItemset(itemset);
//				}
//				if(sequence.size() > 4){
//					sequences.add(sequence);
//				}
//				
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//				if (myInput != null) {
//					myInput.close();
//				}
//			}
//		}

}
