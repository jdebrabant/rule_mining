package ca.pfv.spmf.decisiontree.id3;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * This is an implementation of the ID3 algorithm for creating a decision tree
 * @author Philippe Fournier-Viger, 2011
 */
public class AlgoID3 {

	private String []allAttributes;  // the list of attributes
	private int indexTargetAttribute = -1;  // the position of the target attribute in the list of attributes
	private Set<String> targetAttributeValues = new HashSet<String>(); // the set of values for the target attribute
	private long startTime;
	private long endTime;
	
	public DecisionTree runAlgorithm(String input, String targetAttribute, String separator) throws IOException {
		startTime = System.currentTimeMillis();
		// create an empty decision tree
		DecisionTree tree = new DecisionTree();

		// (1) read input file 
		BufferedReader reader = new BufferedReader(new FileReader(input));
		String line = reader.readLine();
		
		// Read the first line and note the name of the attributes.
		// At the same time identify the position of the target attribute and other attributes.
		allAttributes = line.split(separator);
		int[] remainingAttributes = new int[allAttributes.length-1];
		int pos=0;
		for(int i=0; i< allAttributes.length; i++){
			if(allAttributes[i].equals(targetAttribute)){
				// save the position of the target attribute. It will be useful later.
				indexTargetAttribute = i;
			}else{
				remainingAttributes[pos++] = i;
			}
		}
		
		// Read instances into memory
		List<String[]> instances = new ArrayList<String[]>();
		while( ((line = reader.readLine())!= null)){ // for each transaction
			String [] lineSplit = line.split(separator);
			instances.add(lineSplit);
			targetAttributeValues.add(lineSplit[indexTargetAttribute]);
		}
		reader.close();
		
		// (2) Start the recusive process
		tree.root = id3(remainingAttributes, instances);
		tree.allAttributes = allAttributes;
		endTime = System.currentTimeMillis();
		return tree;
	}

	private Node id3(int[] remainingAttributes,	List<String[]> instances) {
		// if only one remaining attribute, 
		// return a class node with the most common value in the instances
		if(remainingAttributes.length ==0){
			Map<String, Integer> targetValuesFrequency = calculateFrequencyOfAttributeValues(instances, indexTargetAttribute);
			int highestCount = 0;
			String highestName = "";
			for(Entry<String, Integer> entry: targetValuesFrequency.entrySet()){
				if(entry.getValue() > highestCount){
					highestCount = entry.getValue();
					highestName = entry.getKey();
				}
			}
			ClassNode classNode = new ClassNode();
			classNode.className = highestName;
			return classNode;
		}
		
		// Calculate the frequency of each target attribute value and 
		// at the same time check if there is a single class.
		Map<String, Integer> targetValuesFrequency = calculateFrequencyOfAttributeValues(instances, indexTargetAttribute);
		
		// if all instances are from the same class
		if(targetValuesFrequency.entrySet().size() ==1){
			ClassNode classNode = new ClassNode();
			classNode.className = (String)targetValuesFrequency.keySet().toArray()[0];
			return classNode;
		}
		
		// Calculate global entropy
		double globalEntropy = 0d;
		for(String value : targetAttributeValues){
			double frequency = targetValuesFrequency.get(value) / (double)instances.size();
			globalEntropy -= frequency * Math.log(frequency)/Math.log(2);
		}
//		System.out.println("Global entropy = " + globalEntropy);
		
		// Select the attribute from remaining attributes such that if we split the dataset on this 
		// attribute, we will get the higher information gain
		int attributeWithHighestGain = 0;
		double highestGain = -99999;
		for(int attribute : remainingAttributes){
			double gain = calculateGain(attribute, instances, globalEntropy);
//			System.out.println("Process " + allAttributes[attribute] + " gain = " + gain);
			if(gain >= highestGain){
				highestGain = gain;
				attributeWithHighestGain = attribute;
			}
		}
		
		// Create a decision node for the attribute
//		System.out.println("Attribute with highest gain = " + allAttributes[attributeWithHighestGain] + " " + highestGain);
		DecisionNode decisionNode = new DecisionNode();
		decisionNode.attribute = attributeWithHighestGain;
		
		// calculate the list of remaining attribute after we remove the attribute
		int[] newRemainingAttribute = new int[remainingAttributes.length-1];
		int pos =0;
		for(int i=0; i< remainingAttributes.length; i++){
			if(remainingAttributes[i]!= attributeWithHighestGain){
				newRemainingAttribute[pos++] = remainingAttributes[i];
			}
		}
		
		// Split the dataset into partitions according to the selected attribute
		Map<String, List<String[]>> partitions = new HashMap<String, List<String[]>>();
		for(String [] instance : instances){
			String value = instance[attributeWithHighestGain];
			List<String[]> listInstances = partitions.get(value);
			if(listInstances == null){
				listInstances = new ArrayList<String[]>();
				partitions.put(value, listInstances);
			}
			listInstances.add(instance);
		}
		

		// create the values for the subnodes
		decisionNode.nodes = new Node[partitions.size()];
		decisionNode.attributeValues = new String[partitions.size()];
		
		// for each partition, make a recursive call to create the corresponding branches in the tree.
		int index =0;
		for(Entry<String, List<String[]>> partition : partitions.entrySet()){
			decisionNode.attributeValues[index] = partition.getKey();
			decisionNode.nodes[index] = id3(newRemainingAttribute, partition.getValue());  // recursive call
			index++;
		}
		
		return decisionNode;
	}

	private double calculateGain(int attributePos, List<String[]> instances, double globalEntropy) {
		// count the frequency of each value for the attribute
		Map<String, Integer> valuesFrequency = calculateFrequencyOfAttributeValues(instances, attributePos);

		// calculate the gain
		double sum = 0;
		for(Entry<String, Integer> entry: valuesFrequency.entrySet()){
			sum += entry.getValue()/ ((double)instances.size()) * calculateEntropyIfValue(instances, attributePos, entry.getKey());
		}
		return globalEntropy - sum;
	}
 
	/**
	 * Calculate the entropy for the target attribute, if a given attribute has a given value.
	 * @param instances  : list of instances
	 * @param attributeIF : the given attribute
	 * @param valueIF : the given value
	 * @return entropy
	 */
	private double calculateEntropyIfValue(List<String[]> instances,
			int attributeIF, String valueIF) {
		int instancesCount = 0;
		Map<String, Integer> valuesFrequency = new HashMap<String, Integer>();
		for(String [] instance : instances){
			if(instance[attributeIF].equals(valueIF)){
				String targetValue = instance[indexTargetAttribute];
				if(valuesFrequency.get(targetValue)== null){
					valuesFrequency.put(targetValue, 1);
				}else{
					valuesFrequency.put(targetValue, valuesFrequency.get(targetValue)+1);
				}
				instancesCount++;
			}
		}
		double entropy = 0;
		for(String value : targetAttributeValues){
			Integer count = valuesFrequency.get(value);
			if(count != null){
				double frequency = count / (double) instancesCount;
				entropy -= frequency * Math.log(frequency)/Math.log(2);
			}
		}
		return entropy;
	}

	/**
	 * This method calculates the frequency of each value for an attribute in a given set of instances
	 * @param instances  A set of instances
	 * @param indexAttribute The attribute.
	 * @return A map where the keys are attributes and values are the number of times 
	 *      that the value appeared in the set of instances.
	 */
	private Map<String, Integer> calculateFrequencyOfAttributeValues(
			List<String[]> instances, int indexAttribute) {
		Map<String, Integer> targetValuesFrequency = new HashMap<String, Integer>();
		for(String [] instance : instances){
			String targetValue = instance[indexAttribute];
			if(targetValuesFrequency.get(targetValue)== null){
				targetValuesFrequency.put(targetValue, 1);
			}else{
				targetValuesFrequency.put(targetValue, targetValuesFrequency.get(targetValue)+1);
			}
		}
		return targetValuesFrequency;
	}

	public void printStatistics() {
		System.out.println("Time to construct decision tree = " + (endTime - startTime) +  " ms");
		System.out.println("Target attribute = " + allAttributes[indexTargetAttribute]);
		System.out.print("Other attributes = ");
		for(String attribute : allAttributes){
			if(!attribute.equals(allAttributes[indexTargetAttribute])){
				System.out.print(attribute + " ");
			}
		}
		System.out.println();
	}
}
