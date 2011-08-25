import java.util.*; 
import java.io.*; 


public class AssociationRuleTester
{
    BufferedReader rule_file; 
    BufferedReader data_file; 

    LinkedList<AssociationRule> rules;   // rules from the rule file
    LinkedList< LinkedList<Integer> > partitions;  // queries in the log file
    LinkedList<Integer> average_list; // for pattern analysis 

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
        //tester.patternSearch();

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
            average_list = new LinkedList<Integer>();
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
                    //System.out.print(lhs.get(i) + " "); 
                }

                // System.out.println("---> " + rhs + "\n" + line);

                token = tokenizer.nextToken();
                token = token.substring(1, token.length()-1); // chop off '(' in front and ',' in back

                confidence = Double.parseDouble(token); 

                token = tokenizer.nextToken(); 
                token = token.substring(0, token.length()-1); // chop off the ')' in back

                support = Double.parseDouble(token); 

                // for simplicity (for now) I am only adding rules with 1 conidtion on the lhs
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
        int num_partitions = 0;

        try 
        {
            while((line = data_file.readLine()) != null)
            {
                tokenizer = new StringTokenizer(line, " "); 
                partitions.add(new LinkedList<Integer>()); 
                int sum_partitions = 0;
                while(tokenizer.hasMoreTokens())
                {
                    Integer x = (Integer.parseInt(tokenizer.nextToken()));
                    sum_partitions += x;
                    partitions.get(lines_read).add(x); 
                    num_partitions++;
                }
                average_list.add(sum_partitions/num_partitions);
                num_partitions = 0;
                lines_read++; 
            }

            System.out.println("\n...read " + lines_read + " queries\n");
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage()); 
        }
    }

    public HashMap<Integer, Integer> predictNextPartitions(LinkedList<Integer> current_query)
    {
        //LinkedList<Integer> predicted_partitions = new LinkedList<Integer>();
        HashMap<Integer, Integer> predicted_partitions = new HashMap<Integer, Integer>();
        Integer value; 

        for(int i = 0; i < rules.size(); i++)
        {
            if(current_query.contains(rules.get(i).lhs.get(0)))
            {
                value = predicted_partitions.get(rules.get(i).rhs); 

                if(value == null) // this partition hasn't been predicted yet, so add it to hash table
                {
                    predicted_partitions.put(rules.get(i).rhs, new Integer(1)); 
                }
                else  // increment the count of this partition
                {
                    predicted_partitions.put(rules.get(i).rhs,++value); 
                }

                //System.out.println("...predicting partition " + rules.get(i).rhs); 
            }
        }
        return predicted_partitions; 
    }

    //Looks for blatant grouping in the data
    private int patternSearch() {
        final int DEPTH  = 12; //how deep to look
        int depth = java.lang.Math.max(DEPTH, average_list.size()); //make sure it's not more than the number of partitions
        int first = average_list.getFirst();       
        Iterator<Integer> it = average_list.iterator();
      
        it.next(); //skip the first element
        int which = 1;
        
        while(which<depth)
        {
            int x = it.next();
            if(isAbout(first, x))
            {
                List<Integer> l1 = average_list.subList(0, which);
                List<Integer> l2 = average_list.subList(which, average_list.size() - 1);
                Iterator<Integer> it1 = l1.iterator();
                Iterator<Integer> it2 = l2.iterator();
                while(it1.hasNext() && it2.hasNext())
                {
                    int head1 = it1.next();
                    int head2 = it2.next();
                    if(!isAbout(head1, head2))
                    {
                        return -1;
                    }
                }
                return which;
            }
            which++;
        }
        return -1;
    }

    public boolean isAbout(int first, int second)
    {
        double deviationTolerated = 0.25; //in percent

        if(second<= first + (first * deviationTolerated) && second>= first - (first * deviationTolerated))
            return true;
        else
            return false;
    }

    /*
    public boolean isSimilar(List<Integer> l1, List<Integer> l2) 
    {
        if(l1.size() == 0)
            return true;
        if(isAbout(l1.get(0), l2.get(0)))
        {
            //List<Integer> l1_ =  l1.
            //List<Integer> l2_ = (LinkedList<Integer>) l2.clone();
            synchronized (l1) {
            l1.remove(0); }
            synchronized (l2) {
            List<Integer>l3 = l2.subList(1, l2.size());
            isSimilar(l1, l3);}
        }
        return false;               
    }
    */

    public void run()
    {
        //double intersection; 
        LinkedList<Integer> current_query; 
        LinkedList<Integer> next_query; 
        HashMap<Integer, Integer> predicted_partitions; 
        //HashMap<Integer, Integer> actual_partitions = null;
        int pattern = patternSearch();
        if(pattern == -1)
        {
            System.out.println("No apparent patterns found");
            for(int i = 1; i < partitions.size(); i++) // iterate through each query
            {
                current_query = partitions.get(i-1); 
                next_query = partitions.get(i); 
                double predicted = 0;
                predicted_partitions = predictNextPartitions(current_query); 

                if(i == 1)
                {
                    System.out.print("first query: "); 
                    for(int j = 0; j < current_query.size(); j++)
                    {
                        int x = current_query.get(j);
                        System.out.print(x + " "); 
                    }
                }
                System.out.println();

                Iterator it = predicted_partitions.entrySet().iterator(); // iterate through the hashmap

                System.out.print("predicted partitions <partition_id, frequency>: "); 
                while(it.hasNext())
                {
                    Map.Entry entry = (Map.Entry)it.next(); 
                    System.out.print("<" + entry.getKey() + ", " + entry.getValue() + "> "); 
                }
                System.out.println(); 

                System.out.print("next query: "); 
                for(int j = 0; j < next_query.size(); j++)
                {
                    int x = next_query.get(j);
                    System.out.print(x + " "); 
                    if(predicted_partitions.containsKey(x))
                    {
                        predicted++;
                    }
                }
                System.out.println(); 
                System.out.println("Correctly predicted: " + predicted/next_query.size() * 100 + "%");
            }
        }
        else
        {
            int counter =0;
            boolean firstSeries = true;
            LinkedList<HashMap<Integer, Integer>>  pastPredictions = new LinkedList<HashMap<Integer, Integer>> ();
            List<Integer> percentPredicted = new ArrayList<Integer>();
            
            System.out.println("Found the following pattern: Series of " + pattern);
            
            for(int i = 1; i < partitions.size(); i++) // iterate through each query
            {
                if(counter%pattern == 0)
                {
                    System.out.println("Start of Series:");
                    //counter = 0;
                }
                    
                current_query = partitions.get(i-1); 
                int size = current_query.size();
                //next_query = partitions.get(i); 
                double predicted = 0;
                predicted_partitions = predictNextPartitions(current_query);
                String toReturn1= "query: ";
                
                for(int j = 0; j <size ; j++)
                {
                    int x = current_query.get(j);
                    toReturn1 += x + " ";
                    if(counter >=pattern   && pastPredictions.get((counter)%pattern).containsKey(x))
                    {
                        predicted++;
                    }
                }
                              
                Iterator it = predicted_partitions.entrySet().iterator(); // iterate through the hashmap
                String toReturn2 = "predicted partitions of this query in next iteration of the series <partition_id, frequency>: ";
                while(it.hasNext())
                {
                    Map.Entry entry = (Map.Entry)it.next(); 
                    toReturn2 += "<" + entry.getKey() + ", " + entry.getValue() + "> ";
                }
                if(counter>=pattern)
                {
				 //The formatting issue is here, adding \n breaks the columns 
                 System.out.printf("%-100s %100s %100f %s \n", toReturn1, toReturn2, predicted/current_query.size() * 100, " % Correctly predicted");
                 //System.out.println("");
                }
                else
                {
                	//Same issue here
                    System.out.printf("%-100s %100s \n", toReturn1, toReturn2);
                    //System.out.println("");
                }
                pastPredictions.addLast(predicted_partitions);
                counter++;
            }
        }
    }

    class AssociationRule
    {
        String rule; 
        List<Integer> lhs;
        Integer rhs; 

        double confidence; 
        double support; 

        public AssociationRule(String rule_, List<Integer> lhs_, Integer rhs_, double confidence_, double support_)
        {
            rule = rule_; 
            lhs = lhs_; 
            rhs = rhs_; 
            confidence = confidence_; 
            support = support_; 
        }

    }
}