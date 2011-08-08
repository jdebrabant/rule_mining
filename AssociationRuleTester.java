import java.util.*; 
import java.io.*; 


public class AssociationRuleTester
{
	BufferedReader rule_file; 
	BufferedReader data_file; 
	
	LinkedList<AssociationRule> rules; 
	LinkedList< LinkedList<Integer> > partitions; 
	
	public static void main(String args[])
	{
		if(args.length != 2)
		{
			System.out.println("USAGE: java AssociationRuleTester <rule file> <log file>"); 
			System.exit(1); 
		}
		
		AssociationRuleTester tester = new AssociationRuleTester(args[0], args[1]); 
		
		tester.readRuleFile(); 
		tester.readLogFile(); 
		
		tester.run(); 
	}
	
	public AssociationRuleTester(String rule_file_name, String data_file_name)
	{
		try 
		{
			rule_file = new BufferedReader(new FileReader(rule_file_name)); 
			data_file = new BufferedReader(new FileReader(data_file_name)); 
			
			rules = new LinkedList<AssociationRule>(); 
			
			partitions = new LinkedList< LinkedList<Integer> >(); 
		}
		catch (Exception e) 
		{
			System.out.println(e.getMessage()); 
		}
	}
	
	/*
	public double intersectLHS(LinkedList<Integer> previous_query, AssociationRule rule)
	{
		int intersect_count = 0; 
		
		for(int i = 0; i < previous_query.size(); i++)
		{
			if(rule.lhs.contains(previous_query.get(i)))
			{
				intersect_count++; 
			}
		}
		
		return (((double)intersect_count)/previous_query.size()); 
	}
	 */
	
	public void readRuleFile()
	{
		String line; 
		StringTokenizer tokenizer; 
		
		int rhs; 
		LinkedList<Integer> lhs; 
		double confidence; 
		double support; 
		
		int num_tokens_lhs; 
		String token; 
		
		try 
		{
			while(rule_file.ready())
			{
				line = rule_file.readLine(); 
				tokenizer = new StringTokenizer(line, " "); 
				lhs = new LinkedList<Integer>(); 
				
				rhs = Integer.parseInt(tokenizer.nextToken()); 
				tokenizer.nextToken(); // skip the <-
				
				num_tokens_lhs = tokenizer.countTokens() - 2; // the confidence and support in () will be 2 tokens 
				
				// add tokens for left hand side
				for(int i = 0; i < num_tokens_lhs; i++)
				{
					lhs.add(new Integer(Integer.parseInt(tokenizer.nextToken()))); 
					System.out.print(lhs.get(i) + " "); 
				}
				
				System.out.println("---> " + rhs + "\n" + line);
				
				token = tokenizer.nextToken();
				token = token.substring(1, token.length()-1); // chop off '(' in front and ',' in back
				
				confidence = Double.parseDouble(token); 
				
				token = tokenizer.nextToken(); 
				token = token.substring(0, token.length()-1); // chop off the ')' in back
				
				support = Double.parseDouble(token); 
				
				if(lhs.size() == 1)
					rules.add(new AssociationRule(line, lhs, rhs, confidence, support)); 
				
			}
			rule_file.close();
		}
		catch(Exception e)
		{
		}
	}
	
	public void readLogFile()
	{
		String line; 
		StringTokenizer tokenizer; 
		int lines_read = 0; 
		
		try 
		{
			while((line = data_file.readLine()) != null)
			{
				tokenizer = new StringTokenizer(line, " "); 
				
				partitions.add(new LinkedList<Integer>()); 
				
				while(tokenizer.hasMoreTokens())
				{
					partitions.get(lines_read).add(new Integer(Integer.parseInt(tokenizer.nextToken()))); 
				}
				lines_read++; 
			}
			
			System.out.println("\n...read " + lines_read + " queries\n");
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage()); 
		}
	}
	
	public void run()
	{
		double intersection; 
		
		for(int i = 0; i < partitions.size(); i++) // iterate through each query
		{
			for(int j = 0; j < rules.size(); j++)
			{
				intersection = intersectLHS(partitions.get(i), rules.get(j)); 
				
				//if(intersection == 1)
				//	System.out.println("here"); 
				System.out.println("intersection = " + intersectLHS(partitions.get(i), rules.get(j))); 				
			}
			break; 
		}
	}
}

class AssociationRule
{
	String rule; 
	List<Integer> lhs;
	int rhs; 
	
	double confidence; 
	double support; 
	
	public AssociationRule(String rule_, List<Integer> lhs_, int rhs_, double confidence_, double support_)
	{
		rule = rule_; 
		lhs = lhs_; 
		rhs = rhs_; 
		confidence = confidence_; 
		support = support_; 
	}
	
}