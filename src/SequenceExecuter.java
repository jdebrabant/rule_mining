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
	private final int MAX_RECURSION_DEPTH = 3; 
	private final boolean PRINT_DEBUG_INFO = false; 
	
	private Connection conn; 
		
	
	private LinkedList<AssociationRule> rules; 
	private LinkedList< LinkedList<Integer> > query_partitions; 
	private LinkedList<String> sql_queries; 
	
	private HashMap<Integer, Partition> partition_info; 
	
	private int think_time_milli; 
	private boolean think_time_expired; 
	private int think_time_remaining; 
	
	
	public SequenceExecuter()
	{
		rules = new LinkedList<AssociationRule>(); 
		query_partitions = new LinkedList< LinkedList<Integer> >(); 
		sql_queries = new LinkedList<String>(); 
		
		partition_info = new HashMap<Integer, Partition>(); 
		
		think_time_milli = 30000; 
	}
	
	public static void main(String args[])
	{
		if(args.length != 5)
		{
			System.out.println("Usage: java SequenceExecuter <optimized, naive> <partition info file> <sql log file> <parition log file> <rule file>"); 
			System.exit(1); 
		}
		
		SequenceExecuter executer = new SequenceExecuter(); 
		
		executer.DBConnect(); 
		
		executer.readSQLFile(args[2]); 

		
		if(args[0].equals("optimized"))
		{
			executer.readPartitionInfoFile(args[1]); 
			executer.readPartitionFile(args[3]); 
			executer.readRuleFile(args[4]);
			
			executer.runSimulationOptimized(); 
		}
		else if(args[0].equals("naive"))
		{
			
			executer.runSimulationNaive(); 
		}
	}
	
	public void runSimulationNaive()
	{
		Statement stmt;
		ResultSet result; 
		
		long start_time, end_time;
		long query_start_time, query_end_time; 
		
		try 
		{
			stmt = conn.createStatement(); 
			
			start_time = System.currentTimeMillis(); 
			//for(int i = 0; i < sql_queries.size(); i++)
			for(int i = 0; i < 4; i++)
			{
				query_start_time = System.currentTimeMillis();
				result = stmt.executeQuery(sql_queries.get(i)); 
				result.close(); 
				query_end_time = System.currentTimeMillis();
				
				System.out.println("query " + i + " runtime: " + ((query_end_time - query_start_time)/1000.0) + " seconds"); 
				
				Thread.sleep(think_time_milli); 
			}
			end_time = System.currentTimeMillis(); 
			
			System.out.println("total execution time: " + ((end_time - start_time)/1000.0)); 
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
		LinkedList<Integer> frequencies; 
		
		LinkedList<Partition> ranked_partitions; 
		
		PrefetchThread thread; 
		
		long start_time, end_time;
		
		long query_start_time, query_end_time; 
		
		int row_count; 
		
		try 
		{
			stmt = conn.createStatement(); 
			
			start_time = System.currentTimeMillis(); 
			//for(int i = 0; i < sql_queries.size(); i++)
			for(int i = 0; i < 4; i++)
			{
				current_partitions = new LinkedList<Integer>(); 
				predicted_partitions = new LinkedList< LinkedList<Integer> >(); 
				supports = new LinkedList<Double>(); 
				frequencies = new LinkedList<Integer>(); 
				
				ranked_partitions = new LinkedList<Partition>(); 
				
				// predict next partitions based on current query
				predictNextParititions(query_partitions.get(i), predicted_partitions, supports, 1);				
				
				if(PRINT_DEBUG_INFO)
				{
					System.out.print("Predictions for current query " + query_partitions.get(i) + ": "); 
					for(int j = 0; j < predicted_partitions.size(); j++)
					{
						System.out.print(predicted_partitions.get(j) + ", " + supports.get(j)); 
					}
					System.out.println(); 
				}
								
				//rankPartitions(predicted_partitions); 
				
				query_start_time = System.currentTimeMillis();
				result = stmt.executeQuery(sql_queries.get(i)); // execute query 
				result.close();
				query_end_time = System.currentTimeMillis();
				
				System.out.println("query " + i + " runtime: " + ((query_end_time - query_start_time)/1000.0) + " seconds"); 
				
				// reset think time counters 
				think_time_expired = false; 
				think_time_remaining = think_time_milli; 
				
				// launch prefetch thread
				if(predicted_partitions.size() > 0)
				{
					thread = new PrefetchThread(predicted_partitions.get(0)); // prefetch the first predicted sequence only (for testing)
					thread.run(); 
				}
				
				while(think_time_remaining > 0)
				{
					//Thread.sleep(think_time_milli);
					Thread.sleep(1000); // sleep 1 second
					
					think_time_remaining -= 1000; // decrement by one second
				}
				
				think_time_expired = true; // stop prefetching
			}
			end_time = System.currentTimeMillis(); 
			
			System.out.println("total execution time: " + ((end_time - start_time)/1000.0)); 
		}
		catch(Exception e)
		{
			System.out.println("optimized simulation error: " + e.getMessage()); 
		}
	}
	
	public void predictNextParititions(LinkedList<Integer> current_partitions, 
												LinkedList< LinkedList<Integer> > predicted_partitions, 
												LinkedList<Double> supports, 
												//LinkedList<Integer> frequencies, 
												int depth)
	{
		int highest_supports_rule = 0; 
		
		int num_predictions_added = 0; 
		boolean added;
		
		LinkedList< LinkedList<Integer> > left_prediction; 
		LinkedList<Double> left_supports; 
		
		LinkedList< LinkedList<Integer> > right_prediction; 
		LinkedList<Double> right_supports; 		
		
		for(int i = 0; i < rules.size(); i++)  // iterate through all rules finding ones with LHS that intersect the current query
		{
			if(setIntersect(current_partitions, rules.get(i).lhs) > .9)
				//if(rules.get(i).lhs.containsAll(current_partitions))
			{
				added = false; 
				// insert predicted partitions and support into list sorted by support value
				for(int j = 0; j < supports.size(); j++)
				{
					if(supports.get(j) < rules.get(i).support)
					{
						num_predictions_added++; 
						predicted_partitions.add(j, rules.get(i).rhs);			// add predicted partitions 
						supports.add(j, new Double(rules.get(i).support));	// add support 
						added = true; 
						break; 
					}
					else if(supports.get(j) == rules.get(i).support)  // support is equal, choose the rule with higher intersection percent
					{
						if(setIntersect(current_partitions, rules.get(j).lhs) < setIntersect(current_partitions, rules.get(i).lhs))
						{
							num_predictions_added++; 
							predicted_partitions.add(j, rules.get(i).rhs);			// add predicted partitions 
							supports.add(j, new Double(rules.get(i).support));	// add support 
							added = true; 
							break; 
						}
					}
				}
				
				if(!added)  // add to the ends of the lists
				{
					num_predictions_added++; 
					
					predicted_partitions.add(rules.get(i).rhs); 
					supports.add(new Double(rules.get(i).support)); 
				}
				
				if(PRINT_DEBUG_INFO)
				{
					System.out.println("current partition: " + current_partitions + ", rule added: " 
									   + rules.get(i).lhs + " --> " + rules.get(i).rhs + ", " + rules.get(i).support); 
				}
			}
		}
		
		depth++; 
		if(depth <= MAX_RECURSION_DEPTH)  // recurse
		{
			depth++; 

			// recurse down the left branch of the prediction tree using the 1st ranked current prediction
			if(predicted_partitions.size() >= 1)
			{
				left_prediction = new LinkedList< LinkedList<Integer> >(); 
				left_supports = new LinkedList<Double>(); 

				predictNextParititions(predicted_partitions.get(0), left_prediction, left_supports, depth); 
			}
			
			// recurse down right branch of prediction tree using the 2nd ranked current prediction 
			if(predicted_partitions.size() >= 2)
			{
				right_prediction = new LinkedList< LinkedList<Integer> >(); 
				right_supports = new LinkedList<Double>(); 
				
				predictNextParititions(predicted_partitions.get(1), right_prediction, right_supports, depth); 
			}	
			
			
			// TODO: combine the predictions from the left and right subtrees
			
			
		}
		
		//System.out.println("predictions made for query " + current_partitions + ": " + num_predictions_added);
	}
	
	
	public double getQueryCost(String query)
	{
		Statement stmt;
		ResultSet result; 
		//ResultSetMetaData meta; 
		
		StringTokenizer tokenizer; 
		
		String cost_str; 
		double cost = 0; 
						
		query = "EXPLAIN " + query; 
		
		try 
		{
			stmt = conn.createStatement(); 
			
			result = stmt.executeQuery(query);
			//meta = result.getMetaData(); 
			
			result.next(); 
			
			tokenizer = new StringTokenizer(result.getString(1), ".");
			
			// skip the first 2 tokens
			tokenizer.nextToken(); 
			tokenizer.nextToken(); 
			
			// total cost is the concatenation of the 3rd and 4th tokens
			cost_str = tokenizer.nextToken() + tokenizer.nextToken(" "); 
			cost = Double.parseDouble(cost_str); 
			
			//System.out.println("query cost: " + cost); 
			//System.out.println("explain result: " + result.getString(1)); 	
			
			result.close(); 
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage()); 
		}
		
		return cost; 
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
	
	public double setIntersect(List<Integer> set1, List<Integer> set2)
	{
		int intersect_count = 0; 
		
		//System.out.println("intersecting set " + set1 + " and set " + set2); 
		
		for(int i = 0; i < set1.size(); i++)
		{
			if(set2.contains(set1.get(i)))
				intersect_count++; 
		}
		
		//System.out.println("count = " + intersect_count); 
		
		return (intersect_count/(double)set1.size());
		//return intersect_count; 
	}
	
	public void readPartitionInfoFile(String filename)
	{
		BufferedReader partition_file; 
		
		String line; 
		StringTokenizer tokenizer; 
		
		Partition p; 
		
		String name; 
		int id; 
		int x_min, x_max; 
		double y_min, y_max; 
		
		try 
		{
			partition_file = new BufferedReader(new FileReader(filename)); 
			
			System.out.println("here"); 
			
			while((line = partition_file.readLine()) != null)
			{
				tokenizer = new StringTokenizer(line, " "); 
				
				id = Integer.parseInt(tokenizer.nextToken()); 
				x_min = Integer.parseInt(tokenizer.nextToken()); 
				x_max = Integer.parseInt(tokenizer.nextToken()); 
				y_min = Double.parseDouble(tokenizer.nextToken()); 
				y_max = Double.parseDouble(tokenizer.nextToken()); 
				
				partition_info.put(new Integer(id), new Partition(x_min, x_max, y_min, y_max, "", id));
				
				//System.out.println("partition " + id); 
			}
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage()); 
		}
	}
	
	
	public void readSQLFile(String filename)
	{
		BufferedReader query_file; 
		
		String line; 
		StringTokenizer tokenizer; 
		
		int lines_read = 0; 
		
		try 
		{
			query_file = new BufferedReader(new FileReader(filename)); 
			
			while((line = query_file.readLine()) != null)
			{
				sql_queries.add(line); 
				
				lines_read++; 

			}
			
			System.out.println("...read " + lines_read + " sql queries\n");

		}
		catch(Exception e)
		{
			System.out.println(e.getMessage()); 
		}
	}
	
	public void readPartitionFile(String filename)
	{		
		BufferedReader log_file; 
		
		String line; 
        StringTokenizer tokenizer;
        int queries_read = 0;
        int num_partitions = 0;
		int sum_partitions = 0;
		
		String token; 
		
		try 
        {
            log_file = new BufferedReader(new FileReader(filename)); 
			
			while((line = log_file.readLine()) != null)
            {
				tokenizer = new StringTokenizer(line, " ");
				
				query_partitions.add(new LinkedList<Integer>()); 
				
				while(tokenizer.hasMoreTokens())
				{
					token = tokenizer.nextToken(); 

					if(token.equals("-1"))  // end of the current query in the sequence
					{
						queries_read++; 
						query_partitions.add(new LinkedList<Integer>()); 
						break; 
					}
					else if(token.equals("-2")) // end of the line
					{
						break; 
					}
					
					query_partitions.get(queries_read).add(new Integer(Integer.parseInt(token))); 
				}
            }
			
			log_file.close(); 
            System.out.println("...read " + queries_read + " partition queries\n");
			
			/*  
			for(int i = 0; i < query_partitions.size(); i++)   // print partition queries (for testing)
				System.out.print(query_partitions.get(i) + ", ");
			 */
			
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
			System.out.println("...read " + num_rules_read + " rules");
			
        }
        catch (Exception e) 
        {
            System.out.println(e.getMessage()); 
        }
	}
		
	public void DBConnect()
	{
		String user =  new String("justin"); 
		String password = new String(""); 
		String url = new String("jdbc:postgresql:nyse1");
		
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
	
	public void setStatementTimeout(int milliseconds)
	{
		Statement s = null;
		//ResultSet rs = null;
		try 
		{
			s = conn.createStatement(); 
			
			//rs = s.executeQuery("SELECT set_config('statement_timeout', 5000, false)"); 
			s.execute("SET statement_timeout TO " + milliseconds + ";"); 
			//rs.next(); 
			//rs.close(); 
			s = null; 
			
			
			if ( s != null ) 
				s.close();
			
		} 
		catch (SQLException e) 
		{
			System.out.println("ERROR setting STATEMENT_TIMEOUT: " + e.getMessage()); 
			//System.out.println("'statement_timeout' could not be set 2!");
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
			PreparedStatement s; 
			
			Partition next_partition; 
			
			long start_time, end_time; 
						
			try 
			{		
				//conn.setAutoCommit(false);
				
				//setStatementTimeout(think_time_remaining); 
				
				System.out.println("...in prefetch thread...prefetching " + partitions_to_prefetch.size() + " partitions"); 
				for(int i = 0; i < partitions_to_prefetch.size(); i++)
				{
					if(think_time_expired)   // stop prefetching
						return; 
					
					stmt = conn.createStatement();
					
					next_partition = partition_info.get(new Integer(partitions_to_prefetch.get(i))); 
					
					System.out.println("prefetching partition " + partitions_to_prefetch.get(i) + ": " + next_partition.toSQL()); 
										
					//getQueryCost(next_partition.toSQL()); 
										
					start_time = System.currentTimeMillis();
					
					try 
					{
						result = stmt.executeQuery(next_partition.toSQL());
						result.close();
					}
					catch(Exception e)
					{
						System.out.println("prefetch timed out"); 
					}
					end_time = System.currentTimeMillis(); 
					 
					
					System.out.println("prefetch took " + ((end_time-start_time)/1000.0) + " seconds");
 					
					break; 
				}
				
				System.out.println("...leaving prefetcher"); 
				//setStatementTimeout(0); 
				//conn.setAutoCommit(true);
				
			}
			catch(Exception e)
			{
				System.out.println("prefetch error: " + e.getMessage()); 
			}
		}
	}
}










