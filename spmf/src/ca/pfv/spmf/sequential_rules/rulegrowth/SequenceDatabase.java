package ca.pfv.spmf.sequential_rules.rulegrowth;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

//import ca.pfv.spmf2.sequentialrules.TopKSeqRulesB_testBinaryTree.Sequence;

/**
 * Implementation of a sequence database. Each sequence should have a unique id.
 * See examples in /test/ directory for the format of input files.
 * 
 * @author Philippe Fournier-Viger
 **/
public class SequenceDatabase {

	// Contexte
	private final List<Sequence> sequences = new ArrayList<Sequence>();


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
		Sequence sequence = new Sequence();
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
		for (int i=0 ; i < sequences.size(); i++) { // pour chaque objet
			System.out.print(i + ":  ");
			sequences.get(i).print();
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
		for (int i=0 ; i < sequences.size(); i++) { // pour chaque objet
			r.append(i);
			r.append(":  ");
			r.append(sequences.get(i).toString());
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
		Sequence sequence = new Sequence(); // new sequence with
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

			Sequence sequence = new Sequence();
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
					sequence = new Sequence();
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
		Sequence sequence = new Sequence();
		for (String value : split) {
			Itemset itemset = new Itemset();
			Integer item = Integer.parseInt(value);
			itemset.addItem(item);
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
				Sequence sequence = new Sequence();
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
					sequence = new Sequence();
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
					sequence = new Sequence();
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
			while ((thisLine = myInput.readLine()) != null) {
				if(thisLine.length() >= 50){
					Sequence sequence = new Sequence();
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
	
	public void loadFileSignLanguage(String fileToPath, int i) {
		String thisLine;
		BufferedReader myInput = null;
		try {
			FileInputStream fin = new FileInputStream(new File(fileToPath));
			myInput = new BufferedReader(new InputStreamReader(fin));
			String oldUtterance = "-1";
			Sequence sequence = null;
			while ((thisLine = myInput.readLine()) != null) {
				if(thisLine.length() >= 1 && thisLine.charAt(0) != '#'){
					String []tokens = thisLine.split(" ");
					String currentUtterance = tokens[0];
					if(!currentUtterance.equals(oldUtterance)){
						if(sequence != null){
							sequences.add(sequence);
						}
						sequence = new Sequence();
						oldUtterance = currentUtterance;
					}
					for(int j=1; j< tokens.length; j++){
						int character = Integer.parseInt(tokens[j]);
						if(character == -11 || character == -12){
							continue;
						}
//						if(character >= maxItem){
//							maxItem = character;
//						}
//						if(character < minItem){
//							minItem = character;
//						}
						sequence.addItemset(new Itemset(character));
					}
				}
			}
			sequences.add(sequence);
			System.out.println(sequence.toString());
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
