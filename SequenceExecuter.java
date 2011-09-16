/***************************************************************************************************
 * File: SequenceExecuter.java
 * Authors: Justin A. DeBrabant (debrabant@cs.brown.edu)
 * Description: 
 ***************************************************************************************************/ 
 

import java.util.*; 
import java.lang.*; 
import java.io.*; 
import java.sql.*; 


public class SequenceExecuter
{
	private Connection conn; 
	
	private AssociationRuleTester tester;
	
	private int think_time_milli; 
	
	private LinkedList<AssociationRule> rules; 
	private LinkedList< LinkedList<Integer> > query_partitions; 
	private LinkedList<String> sql_queries; 
	
	private HashMap<Integer, Partition> partition_info; 
	
	private boolean think_time_expired; 
	
	
	public SequenceExecuter()
	{
		rules = new LinkedList<AssociationRule>(); 
		query_partitions = new LinkedList< LinkedList<Integer> >(); 
		sql_queries = new LinkedList<String>(); 
		
		think_time_milli = 2000; 
	}
	
	public static void main(String args[])
	{
		if(args.length != 1)
		{
			System.out.println("Usage: java SequenceExecuter <optimized, naive>"); 
			System.exit(1); 
		}
		
		SequenceExecuter executer = new SequenceExecuter(); 
		
		executer.DBConnect(); 
		
		if(args[0].equals("optimized"))
		{
			executer.readLogFile(""); 
			executer.readRuleFile("");
			
			executer.runSimulationOptimized(); 
		}
		else if(args[0].equals("naive"))
		{
			executer.readQueryFile(""); 
			
			executer.runSimulationNaive(); 
		}
	}
	
	public void runSimulationNaive()
	{
		Statement stmt;
		ResultSet result; 
		
		long start_time, end_time; 
		
		try 
		{
			stmt = conn.createStatement(); 
			
			start_time = System.currentTimeMillis(); 
			for(int i = 0; i < sql_queries.size(); i++)
			{
				result = stmt.executeQuery(sql_queries.get(i)); 
				
				Thread.sleep(think_time_milli); 
			}
			end_time = System.currentTimeMillis(); 
			
			System.out.println("total execution time: " + (end_time - start_time)); 
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage()); 
		}
	}
	
	public void runSimulationOptimized()
	{
		Statement stmt;
		ResultSet result; 
		
		LinkedList<Integer> current_partitions; 
		LinkedList< LinkedList<Integer> > predicted_partitions; 
		LinkedList<Double> supports; 
		
		LinkedList<Partition> ranked_partitions; 
		
		PrefetchThread thread; 
		
		long start_time, end_time; 
		
		try 
		{
			stmt = conn.createStatement(); 
			
			start_time = System.currentTimeMillis(); 
			for(int i = 0; i < sql_queries.size(); i++)
			{
				current_partitions = new LinkedList<Integer>(); 
				predicted_partitions = new LinkedList< LinkedList<Integer> >(); 
				supports = new LinkedList<Double>(); 
				
				ranked_partitions = new LinkedList<Partition>(); 
				
				// predict next partitions based on current query
				predictNextParititions(query_partitions.get(i), predicted_partitions, supports); 
				
				//rankPartitions(predicted_partitions); 
				
				result = stmt.executeQuery(sql_queries.get(i)); // execute query 
				
				think_time_expired = false; 
				
				thread = new PrefetchThread(predicted_partitions.get(0)); // prefetch the first predicted sequence only (for testing) 
				
				Thread.sleep(think_time_milli);
				
				think_time_expired = true; // stop prefetching
			}
			end_time = System.currentTimeMillis(); 
			
			System.out.println("total execution time: " + (end_time - start_time)); 
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage()); 
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
				// insert predicted partitions and support into list sorted by support value
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
	
	/*
	public LinkedList<Partition> rankPartitions(LinkedList< LinkedList<Integer> predicted_partitions)
	{
		//LinkedList<Partition> ranked_partitions = new LinkedList<Partition>(); 
		
		HashMap<Integer, Partition> ranked_partitions = new HashMap<int, Partition>(); 
		Integer key; 
		for(int i = 0; i < predicted_partitions.size(); i++)
		{
			for(int j = 0; j < predicted_partitions.get(i).size(); j++)
			{
				key = new Integer(predicted_partitions.get(i).get(j).partition_id); 
				ranked_partitions.add(key, partition_info.get(key); 
			}
		}
		
		return (LinkedList<Partition>)ranked_partitions.values; 
	}
	 */
	
	public void readPartitionFile(String filename)
	{
		BufferedReader partition_file; 
		
		String line; 
		StringTokenizer tokenizer; 
		
		Partition p; 
		
		String name; 
		int id; 
		int x_min, x_max, y_min, y_max; 
		
		try 
		{
			partition_file = new BufferedReader(new FileReader(filename)); 
			
			while((line = partition_file.readLine()) != null)
			{
				tokenizer = new StringTokenizer(line, " "); 
				
				id = Integer.parseInt(tokenizer.nextToken()); 
				x_min = Integer.parseInt(tokenizer.nextToken()); 
				x_max = Integer.parseInt(tokenizer.nextToken()); 
				y_min = Integer.parseInt(tokenizer.nextToken()); 
				y_max = Integer.parseInt(tokenizer.nextToken()); 
				
				partition_info.put(new Integer(id), new Partition(x_min, x_max, y_min, y_max, "", id));
			}
		}
		catch(Exception e)
		{
		}
	}
	
	
	public void readQueryFile(String filename)
	{
		BufferedReader query_file; 
		
		String line; 
		StringTokenizer tokenizer; 
		
		try 
		{
			query_file = new BufferedReader(new FileReader(filename)); 
			
			while((line = query_file.readLine()) != null)
			{
				
			}
			
		}
		catch(Exception e)
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
                query_partitions.add(new LinkedList<Integer>()); 
				
                while(tokenizer.hasMoreTokens())
                {
                    query_partitions.get(lines_read).add(new Integer(Integer.parseInt(tokenizer.nextToken()))); 
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
				
				rules.add(new AssociationRule(lhs, rhs, support));
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
		
	public void DBConnect()
	{
		String user =  new String(""); 
		String password = new String(""); 
		String url = new String("jdbc:postgresql:nyse");
		
		try 
		{
			Class.forName("org.postgresql.Driver");  // load the driver
			conn = DriverManager.getConnection(url, user, password);

		}
		catch(Exception e)
		{
			System.out.println(e.getMessage()); 
		}
		
		
	}
	
	class PrefetchThread extends Thread
	{
		LinkedList<Integer> partitions_to_prefetch;  
		
		public PrefetchThread(LinkedList<Integer> p)
		{
			partitions_to_prefetch = p; 
		}
		
		public void run()
		{
			Statement stmt;
			ResultSet result;
			
			Partition next_partition; 
			
			try 
			{				
				for(int i = 0; i < partitions_to_prefetch.size(); i++)
				{
					if(think_time_expired)   // stop prefetching
						return; 
					
					stmt = conn.createStatement(); 
					
					next_partition = partition_info.get(new Integer(partitions_to_prefetch.get(i))); 
					
					result = stmt.executeQuery(next_partition.toSQL());
					
					// prefetch next partition
				}
				
			}
			catch(Exception e)
			{
				System.out.println(e.getMessage()); 
			}
		}
	}
}




















