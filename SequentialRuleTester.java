import java.util.*; 
import java.io.*; 

public class SequentialRuleTester
{
	private LinkedList<SequentialRule> rules; 
	private LinkedList< LinkedList<Integer> > queries; 
	
	public static void main(String args[])
	{
		if(args.length != 2)
        {
            System.out.println("USAGE: java AssociationRuleTester <rule file> <log file>"); 
            System.exit(1); 
        }
		
        SequentialRuleTester tester = new SequentialRuleTester(); 
		
        tester.readRuleFile(args[0]); 
        tester.readLogFile(args[1]);
		
        tester.run(); 
	}
	
	public SequentialRuleTester()
    {
        try 
        {
            rules = new LinkedList<SequentialRule>(); 
            queries = new LinkedList< LinkedList<Integer> >();
        }
        catch (Exception e) 
        {
            System.out.println(e.getMessage()); 
        }
    }
	
	public void run()
	{
		LinkedList<Integer> current_query; 
		LinkedList<Integer> next_query; 
		LinkedList< LinkedList<Integer> > prediction;
		LinkedList<Double> prediction_support; 
		
		double prediction_accuracy; 
		int intersection_count; 
				
		for(int i = 1; i < queries.size(); i++)
		{
			prediction = new LinkedList< LinkedList<Integer> >(); 
			prediction_support = new LinkedList<Double>(); 
			
			current_query = queries.get(i-1); 
			next_query = queries.get(i); 
			
			//prediction = predictNextParititions(current_query); 
			
			predictNextParititions(current_query, prediction, prediction_support); 
			
			System.out.println("num predictions: " + prediction.size()); 
			
			intersection_count = 0; 
			prediction_accuracy = 0; 
			for(int j = 0; j < prediction.size(); j++)
			{
				if(next_query.contains(prediction.get(j)))
					intersection_count++; 
			}
			prediction_accuracy = intersection_count / (double)prediction.size(); 
			
			System.out.print("next query: "); 
			for(int j = 0; j < next_query.size(); j++)
			{
				System.out.print(next_query.get(j) + " "); 
			}
			System.out.println(); 
			
			System.out.print("predicted: "); 
			for(int j = 0; j < prediction.size(); j++)
			{
				System.out.println("  " + prediction.get(j) + ", support = " + prediction_support.get(j)); 
			}
			System.out.println("(" +  intersection_count + "/" + next_query.size() + " partitions predicted, " + 
							    (prediction_accuracy * 100) + "% accuracy)\n"); 
		}
	}
	
	public void predictNextParititions(LinkedList<Integer> current_partitions, 
									   LinkedList< LinkedList<Integer> > predicted_partitions, 
									   LinkedList<Double> supports)
	{
		//LinkedList<Integer> predicted_partitions = new LinkedList<Integer>(); 
		int highest_supports_rule = 0; 
		//double max_conf = 0;
		
		int num_predictions_added = 0; 
		boolean added;
		
		for(int i = 0; i < rules.size(); i++)
		{
			if(rules.get(i).lhs.containsAll(current_partitions))
			{
				added = false; 
				// insert predicted partitions and support into list by sorted by support value
				for(int j = 0; j < supports.size(); j++)
				{
					if(supports.get(j) < rules.get(i).support)
					{
						predicted_partitions.add(j, rules.get(i).rhs);			// add predicted partitions 
						supports.add(j, new Double(rules.get(i).support));	// add support 
						added = true; 
						break; 
					}
				}
				
				if(!added)  // add to the ends of the lists
				{
					predicted_partitions.add(rules.get(i).rhs); 
					supports.add(new Double(rules.get(i).support)); 
				}
			}
		}
	}
	
	public void readLogFile(String filename)
	{		
		BufferedReader log_file; 
		
		String line; 
        StringTokenizer tokenizer; 
        int lines_read = 0;
        int num_partitions = 0;
		int sum_partitions = 0;
				
		try 
        {
            log_file = new BufferedReader(new FileReader(filename)); 
			
			while((line = log_file.readLine()) != null)
            {
                tokenizer = new StringTokenizer(line, " "); 
                queries.add(new LinkedList<Integer>()); 
				
                while(tokenizer.hasMoreTokens())
                {
                    queries.get(lines_read).add(new Integer(Integer.parseInt(tokenizer.nextToken()))); 
                    num_partitions++;
                }
				
                lines_read++; 
            }
			
			log_file.close(); 
            System.out.println("\n...read " + lines_read + " queries\n");

        }
        catch (Exception e) 
        {
            System.out.println(e.getMessage()); 
        }
		
	}
	
	public void readRuleFile(String filename)
	{
		BufferedReader rule_file; 
		
		String line; 
		StringTokenizer tokenizer; 
		String token; 
		
		LinkedList<Integer> lhs; 
		LinkedList<Integer> rhs; 
		double support; 
		
		int num_rules_read = 0; 
		
		try 
        {
            rule_file = new BufferedReader(new FileReader(filename)); 
			
			while((line = rule_file.readLine()) != null)
			{
				tokenizer = new StringTokenizer(line, " "); 
				
				lhs = new LinkedList<Integer>(); 
				rhs = new LinkedList<Integer>(); 
				
				while(tokenizer.hasMoreTokens())
				{
					token = tokenizer.nextToken(); 
					
					if(token.equals("==>"))  // finished reading lhs
						break; 
					
					lhs.add(new Integer(Integer.parseInt(token))); 
				}
				
				while(tokenizer.hasMoreTokens())
				{
					token = tokenizer.nextToken(); 
					
					if(token.equals(","))  // finished reading rhs
						break; 
					
					rhs.add(new Integer(Integer.parseInt(token))); 
				}
				
				token = tokenizer.nextToken(); 
				support = Double.parseDouble(token); 
				
				rules.add(new SequentialRule(line, lhs, rhs, support));
				num_rules_read++;
				
			}	
			
			rule_file.close(); 
			System.out.println("\n...read " + num_rules_read + " rules");

        }
        catch (Exception e) 
        {
            System.out.println(e.getMessage()); 
        }
		
	}

}


class SequentialRule
{
	public String rule; 
	public LinkedList<Integer> lhs;
	public LinkedList<Integer> rhs; 
	
	public double support; 
	
	public SequentialRule(String rule_, LinkedList<Integer> lhs_, LinkedList<Integer> rhs_, double support_)
	{
		rule = rule_; 
		lhs = lhs_; 
		rhs = rhs_; 
		support = support_; 
	}
}






















