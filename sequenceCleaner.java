/***************************************************************************************************
 * File: SequenceCleaner.java
 * Authors: Justin A. DeBrabant (debrabant@cs.brown.edu)
 * Description: 
***************************************************************************************************/ 


import java.util.*; 
import java.io.*; 

public class SequenceCleaner
{
	LinkedList<AssociationRule> rules;
	
	public SequenceCleaner()
	{
		rules = new LinkedList<AssociationRule>(); 
	}
	
	public void createRules(String sequence_file)
	{
		StringTokenizer tokenizer; 
		String token; 
		String line; 
		
		int num_rules_kept = 0; 
		int num_rules_read = 0; 
		int num_rules_after_cleaning; 
		
		LinkedList<Integer> lhs; 
		LinkedList<Integer> rhs; 
		double support; 
		
		try 
		{
			BufferedReader in = new BufferedReader(new FileReader("sequences.txt"));
			BufferedWriter out = new BufferedWriter(new FileWriter("sequences-clean.txt"));
			
			System.out.println("\n...generating rules\n"); 
			
			line = in.readLine(); 
			while(in.ready())    // read line by line from the sequence file
			{
				num_rules_read++;
				
				tokenizer = new StringTokenizer(line, " "); 
				lhs = new LinkedList<Integer>(); 
				rhs = new LinkedList<Integer>(); 
				
				int bracket_count = 0; 
				while(!((token = tokenizer.nextToken()).equals("-1")))  // read partitions in lhs of rule
				{
					lhs.add(new Integer(Integer.parseInt(token))); 
				}
				
				token = tokenizer.nextToken(); 
				if(token.equals("SID:"))  // there is no rhs, so discard this rule
				{
					lhs.clear(); 
					rhs.clear(); 
					
					lhs = null; 
					rhs = null; 
				}
				else 
				{
					num_rules_kept++; 
					
					rhs.add(new Integer(Integer.parseInt(token))); 
					while(!((token = tokenizer.nextToken()).equals("-1")))  // read partitions in rhs of rule
					{
						rhs.add(new Integer(Integer.parseInt(token))); 
					}
					
					while(!((token = tokenizer.nextToken()).equals("SUP:"))) // skip to support field
					{
					}
					
					token = tokenizer.nextToken(); 
					support = (Double.parseDouble(token)); 
					
					rules.add(new AssociationRule(lhs, rhs, support));   // add the newest rule to the list
					
				}
				
				line = in.readLine(); 
			}
			
			for(int i = 0; i < rules.size(); i++)
			{
				if(!rules.get(i).removed)
					out.write(rules.get(i).ruleToString()); 
			}
			
			in.close(); 
			out.close(); 
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage()); 
		}
	}

	public static void main(String [] args)
	{		
	}
	
	public static int cleanRules(LinkedList<AssociationRule> rules)
	{
		int num_rules_discarded = 0; 
		
		System.out.println("\n...cleaning rules\n"); 
		
		
		for(int i = 0; i < rules.size(); i++)
		{
			for(int j = 0; j < rules.size(); j++)
			{
				// rule i dominates rule j, so discard rule j 
				if(rules.get(i).lhs.containsAll(rules.get(j).lhs) &&
				   rules.get(j).lhs.containsAll(rules.get(i).lhs) &&
				   rules.get(i).rhs.containsAll(rules.get(j).rhs) &&
				   rules.get(i).support == rules.get(j).support && 
				   i != j &&
				   rules.get(j).removed == false &&
				   rules.get(i).removed == false
				   )
				{
					//System.out.println("removing rule: " + rules.get(i).ruleToString()); 
					
					rules.get(j).removed = true; 
					//rules.remove(j); 
					//j--; 
					num_rules_discarded++;
				}
			}
		}
		
		//System.out.println("rules discarded: " + num_rules_discarded); 
		return num_rules_discarded; 
	}
}











