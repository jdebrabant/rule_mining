package ca.pfv.spmf.sequential_rules.cmrules;

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
 * This class represents a binary context.
 * It can read the context directly from a file.
 * See the ca.pfv.spmf.test folder for some examples of files
 * containing binary contexts.
 * 
 * @author Philippe Fournier-Viger 
 */
public class Context {

	// Contexte
	private final Set<Integer> attributes = new HashSet<Integer>();
	private final List<Itemset> objects = new ArrayList<Itemset>();
	
	public void addItemset(Itemset itemset){
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
		//We assume that there is no empty line
		Itemset itemset = new Itemset();
		for(String attribute:  attributs){
			int item = Integer.parseInt(attribute);
			itemset.addItem(item);
			attributes.add(item);
		}
		objects.add(itemset);
	}
	
	public void printContext(){
		System.out
		.println("===================  BINARY CONTEXT ===================");
		int count = 0;
		for(Itemset itemset : objects){ // pour chaque objet
			System.out.print("0" + count + ":  ");
			itemset.print();
			System.out.println("");
			count++;
		}
	}
	
	public int size(){
		return objects.size();
	}

	public List<Itemset> getObjects() {
		return objects;
	}

	public Set<Integer> getAttributes() {
		return attributes;
	}

}
