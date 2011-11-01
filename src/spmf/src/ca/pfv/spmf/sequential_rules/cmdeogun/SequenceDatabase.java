package ca.pfv.spmf.sequential_rules.cmdeogun;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
//				String valeur = entier.substring(1, entier.length() - 1);
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
		long size = 0;
		for(Sequence sequence : sequences){
			size += sequence.size();
		}
		double meansize = ((float)size) / ((float)sequences.size());
		System.out.println("mean size" + meansize);
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

	public void loadFileWebViewFOrmat(String filepath, int nbLine) {
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
	
	public void loadSnakeDataset(String filepath, int nbLine) {
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
