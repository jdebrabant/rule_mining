import java.util.*; 
import java.io.*; 

public class sequenceCleaner
{
	public sequenceCleaner()
	{
		//rules = new LinkedList<Rule>(); 
	}

	public static void main(String [] args)
	{
		StringTokenizer tokenizer; 
		String token; 
		String line; 
		
		int num_rules_kept = 0; 
		int num_rules_read = 0; 
		int num_rules_after_cleaning; 
		
		LinkedList<Rule> rules = new LinkedList<Rule>(); 
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
					
					rules.add(new Rule(lhs, rhs, support));   // add the newest rule to the list
					
				}
				
				line = in.readLine(); 
			}
						
			num_rules_after_cleaning = num_rules_kept - cleanRules(rules); 
			
			System.out.println("rules read: " + num_rules_read); 
			System.out.println("rules kept: " + num_rules_after_cleaning); 

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
	
	public static int cleanRules(LinkedList<Rule> rules)
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

class Rule
{
	public List<Integer> lhs; 
	public List<Integer> rhs; 
	public double support; 
	public boolean removed; 
	
	public Rule(List<Integer> l, List<Integer> r, double supp)
	{
		lhs = l; 
		rhs = r; 
		support = supp; 
		removed = false; 
	}
	
	public String ruleToString()
	{
		String rule = ""; 
		
		for(int i = 0; i < lhs.size(); i++)
		{
			rule += lhs.get(i).intValue() + " "; 
		}
		
		rule += " ==> "; 
		
		for(int i = 0; i < rhs.size(); i++)
		{
			rule += rhs.get(i).intValue() + " "; 
		}
		
		rule += ", " + support + "\n";
		
		return rule; 
	}
}









