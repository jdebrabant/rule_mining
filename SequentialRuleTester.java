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
		
        //tester.run(); 
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






















