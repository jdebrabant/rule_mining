package ca.pfv.spmf.frequentpatterns.zart;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Classe contexte.
 * Prend en entrée un contexte au format PrefixSpan : 
 * 1 -1 2 -1 -2
 * 1 -1 2 3 4 -1 -2
 * où chaque ligne représente une transaction et chaque chiffre positif représente un item.
 * -1 est un séparateur entre les itemsets. -2 indique la fin d'une ligne.
 * @author Philippe Fournier-Viger 
 */
public class ContextZart {

	// Contexte
	private final Set<ItemZart> attributes = new HashSet<ItemZart>();
	private final List<ItemsetZart> objects = new ArrayList<ItemsetZart>();
	
	public void addItemset(ItemsetZart itemset){
		objects.add(itemset);
		attributes.addAll(itemset.getItems());
	}

	public void loadFile(String path) throws IOException {
		String thisLine;
		BufferedReader myInput = null;
		try {
			FileInputStream fin = new FileInputStream(new File(path));
			myInput = new BufferedReader(new InputStreamReader(fin));
			while ((thisLine = myInput.readLine()) != null) {
				if(thisLine.charAt(0) != '#'){ 
					System.out.println(thisLine);
					addObject(thisLine.split(" "));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(myInput != null){
				myInput.close();
			}
	    }
	}
	
	public void addObject(String attributs[]){
		// on suppose qu'il n'y a pas de ligne vide
		ItemsetZart itemset = new ItemsetZart();
		for(String attribute:  attributs){
			ItemZart item = new  ItemZart(Integer.parseInt(attribute));
			itemset.addItem(item);
			attributes.add(item);
		}
		objects.add(itemset);
	}
	
	public void printContext(){
		System.out
		.println("===================  CONTEXTE SÉQUENCES ===================");
		int count = 0;
		for(ItemsetZart itemset : objects){ // pour chaque objet
			System.out.print("0" + count + ":  ");
			itemset.print();
			System.out.println("");
			count++;
		}
	}
	
	public String toString(){
		StringBuffer buffer = new StringBuffer("===================  CONTEXTE SÉQUENCES ===================\n");
		int count = 0;
		for(ItemsetZart itemset : objects){ // pour chaque objet
			buffer.append(count);
			buffer.append(":  ");
			buffer.append(itemset.toString());
			buffer.append("\n");
			count++;
		}
		return buffer.toString();
	}
	
	public int size(){
		return objects.size();
	}

	public List<ItemsetZart> getObjects() {
		return objects;
	}

	public Set<ItemZart> getAttributes() {
		return attributes;
	}

}
