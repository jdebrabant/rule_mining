/***************************************************************************************************
 * File: SequenceGenerator.java
 * Authors: Justin A. DeBrabant (debrabant@cs.brown.edu)
			Ivan Yanev (ivan_yanev@brown.edu)
 * Description: 
	Used for artificial dataset generation. Generates sequences of timebox queries and 
	partitions the queries into uniform partitions. 
****************************************************************************************************/

import java.util.*;
import java.io.*; 

public class SequenceGenerator
{
    private Random rand;

	private FileWriter partition_info_out; 
	
    private FileWriter partition_out_1; 
	private FileWriter query_out_1;
	private FileWriter rule_miner_input_1; 
	
    private FileWriter partition_out_2; 
	private FileWriter query_out_2; 
	
    private FileWriter partition_out_3; 
	private FileWriter query_out_3; 
	
	//private FileWriter apriori_out; 

    // max amount of random shift 
    public final int max_delta_x = 540; //5 % of box width
    public final double max_delta_y = 12.25; //roughly 5% of box height

    public final int min_x;
    public final int max_x;
    public final int min_y;
    public final int max_y;
    public final int num_partitions_x;
    public final int num_partitions_y;
    public final int partition_size_x;
    public final double partition_size_y;
    
    public LinkedList<Partition> grid;

    private class QueryBox
	{
        //public final int box_num;
        private int x;
        private double y;
        private int delta_x;
        private double delta_y;
        private int x_new;
        private double y_new;
        private int delta_x_new;
        private double delta_y_new;
		
		private int transaction_id; 
		private int customer_id; 
		private int customer_transaction_count; 

         public QueryBox(int x_, int deltax,  double y_,double deltay)
        {
            x = x_;           
            y = y_;          
            delta_x = deltax;         
            delta_y = deltay; 
			transaction_id = 0; 
			customer_id = 0; 
			customer_transaction_count = 0; 
        }

        public void scatter()
        {
			/*
            //set to original values
            x_new = x;
            delta_x_new = delta_x;
            y_new = y;
            delta_y_new = delta_y;
            //get new values
            x_new = x + rand.nextInt(max_delta_x);
            delta_x_new = delta_x + rand.nextInt(max_delta_x);
            y_new = y + rand.nextDouble() * max_delta_y; 
            delta_y_new = delta_y + rand.nextDouble() * max_delta_y; 
			 
			 
			 */
			
			delta_x_new = 0; 
			delta_y_new = 0; 
			
			//set to original values
            x_new = x;
            delta_x_new = delta_x;
            y_new = y;
            delta_y_new = delta_y;
            //get new values
            x_new = x; //+ rand.nextInt(max_delta_x);
            delta_x_new = delta_x; // + rand.nextInt(max_delta_x);
            y_new = y; // + rand.nextDouble() * max_delta_y; 
            delta_y_new = delta_y; //  + rand.nextDouble() * max_delta_y; 
        }
			
		public String convertToSQL()
		{			
			return ("SELECT * FROM quote " + 
					"WHERE quote_time > " + x_new + " AND quote_time < " + (x_new + delta_x_new) + 
					" AND offer_price > " + y_new + " AND offer_price < " + (y_new + delta_y_new) + "\n"); 
		}

        public String checkPartitions()
        {			
			String toReturn = ""; 
			int true_count = 0; 
			
			transaction_id++; 
			customer_transaction_count++; 
			
			if(customer_transaction_count == 50)
			{
				customer_transaction_count = 0; 
				customer_id++;
			}
			
			try 
			{
				for(Partition current: grid)
				{
					if(current.x_max >= x_new && current.x_min< (x_new + delta_x_new) &&
							current.y_max >= y_new  && current.y_min < (y_new + delta_y_new))
					{						
						toReturn += current.partition_id + " "; 
						true_count++; 
					}

				}
				
				if(true_count > 0)
				{
					toReturn += "-1 "; 
				}
				else
				{
					toReturn = ""; 
				}
				
			}
			catch(Exception e)
			{
				System.out.println(e.getMessage()); 
			}
			return toReturn; 
        }
    }

    //takes  minimum_x,  maximum_x,  minimum_y,  maximum_y, number_of_partitions_x, number_of_partitions_y as arguments
    public static void main(String args[])
    {
		/*
		if(args.length != 6)
		{
			System.out.println("usage: java QueryGenerator <min x> <max x> <min y> <max y> <num x partitions> <num y partitions>\n"); 
		}
		 */
		
        try 
		{
            //QueryGenerator task_simulator = new QueryGenerator(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]),
            //        Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]));
			
			SequenceGenerator task_simulator = new SequenceGenerator(10000, 90000, 0, 150, 10, 10);
			
            // run each task 1000 times
            task_simulator.runTask1(); 
            //task_simulator.runTask2(); 
            //task_simulator.runTask3(); 

            task_simulator.finishTasks();

        }

        catch(NumberFormatException e)
        {
            System.out.println("Invalid Input");  
            e.printStackTrace();
        }

    }

    public SequenceGenerator(int minimum_x, int maximum_x, int minimum_y, int maximum_y, int number_of_partitions_x, int number_of_partitions_y)
    {
        rand = new Random();
        min_x = minimum_x;
        max_x = maximum_x;
        min_y = minimum_y;
        max_y = maximum_y;
        num_partitions_x = number_of_partitions_x;
        num_partitions_y = number_of_partitions_y;
        partition_size_x= (max_x - min_x)/num_partitions_x;
        partition_size_y = (max_y - min_y)/num_partitions_y;
        
        grid = new LinkedList<Partition>(); 
        String attribute; 
        
        try
        {
			partition_info_out = new FileWriter("../data/partition_info.txt"); 
			
			partition_out_1 = new FileWriter("../data/partition1.txt"); 
			//partition_out_2 = new FileWriter("data/partition2.txt"); 
			//partition_out_3 = new FileWriter("data/partition3.txt"); 
			
			rule_miner_input_1 = new FileWriter("../data/miner1.txt"); 
			
			query_out_1 = new FileWriter("../data/sql1.txt");
			//query_out_2 = new FileWriter("data/sql2.txt");
			//query_out_3 = new FileWriter("data/sql3.txt");
			
			createPartitions(); 

        }
        catch(Exception e)
        {
            System.out.println(e.getMessage()); 
        }

    }
    
    private void createPartitions()
    {        
        int curr_x = min_x; 
        double curr_y = min_y; 
        int partition_id = 0;  
		
		Partition p; 
		
		try 
		{
			for(int i = 0; i < num_partitions_x; i++)
			{
				for(int j = 0; j < num_partitions_y; j++)
				{
					p = new Partition(curr_x, curr_x+partition_size_x, 
									  curr_y, curr_y+partition_size_y, 
									  "partition_"+i+"_"+j, partition_id); 
					
					grid.add(p); 
					
					//partition_info_out.write(p.toString()); 
					
					curr_y += partition_size_y; 
					
					if(curr_y > max_y)
						curr_y = max_y; 
					
					partition_id++; 
				}
				curr_x += partition_size_x; 
				curr_y = min_y;
				
				if(curr_x > max_x)
					curr_x = max_x; 
			}
			
			writePartitions(); 
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage()); 
		}
    }

    public void writer(List<QueryBox> boxList, int task)
    {
		try
		{

			for(QueryBox box: boxList)
			{
				box.scatter();
			   
					switch(task)
					{
					case 1:                       
						partition_out_1.write(box.checkPartitions() + "\n");
						query_out_1.write(box.convertToSQL()); 
						break;
					case 2:
						partition_out_2.write(box.checkPartitions());
						query_out_2.write(box.convertToSQL()); 
						break;
					case 3:
						partition_out_3.write(box.checkPartitions());
						query_out_3.write(box.convertToSQL()); 
						break;
					default:
						System.out.println("Error!!!!");
					}
			}
			
			/*
			switch(task)
			{
				case 1:                       
					partition_out_1.write("-2\n"); 
					break;
				case 2:
					partition_out_2.write("-2\n"); 
					break;
				case 3:
					partition_out_3.write("-2\n"); 
					break;
				default:
					System.out.println("Error!!!!");
			}
			 */
			
			for(int i = 0; i < boxList.size()-1; i++)
			{
				rule_miner_input_1.write(boxList.get(i).checkPartitions()); 
				rule_miner_input_1.write(boxList.get(i+1).checkPartitions());
				rule_miner_input_1.write("-2\n"); 
			}
			
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage()); 
		}
    }
	
	public void writePartitions()
	{		
		//System.out.println("writing partition info for " + grid.size() + " partitions"); 
		
		try 
		{			
			for(int i = 0; i < grid.size(); i++)
			{
				grid.get(i).toString(); 
				partition_info_out.write(grid.get(i).toString()); 
			}
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage()); 
		}
	}

    public void runTask1() //3 non-contiguous boxes, upward trend
    {
        LinkedList<QueryBox> boxList = new LinkedList <QueryBox>();
		
		for(int i = 0; i < 25; i++)
		{
			QueryBox box1 = new QueryBox(35000, 5000, 30, 10); // approximately 5% selectivity 
			QueryBox box2 = new QueryBox(40000, 5000, 40, 10); // approximately 5% selectivity 
			QueryBox box3 = new QueryBox(45000, 5000, 50, 10); // approximately 5% selectivity 
			QueryBox box4 = new QueryBox(50000, 5000, 60, 10); // approximately 5% selectivity 
			
			boxList.add(box1);
			boxList.add(box2);
			boxList.add(box3);
			boxList.add(box4);
		}
		
        writer(boxList, 1);
    }

    public void runTask2() // 6 contigous boxes, linearly arranged
    {
        LinkedList<QueryBox> boxList = new LinkedList <QueryBox>();
		
		/*
        QueryBox box1 = new QueryBox(18000, 9000, 100, 100); //5  AM
        QueryBox box2 = new QueryBox(27000, 9000, 100, 100); //7:30 AM
        QueryBox box3 = new QueryBox(36000, 9000, 100, 100);  ///10 AM
        QueryBox box4 = new QueryBox(45000, 9000, 100, 100); //12:30 PM
        QueryBox box5 = new QueryBox(54000, 9000, 100, 100); //3 PM
        QueryBox box6 = new QueryBox(63000, 9000, 100, 100); //5:30 PM to 8PM
		 */
		
		QueryBox box1 = new QueryBox(18000, 9000, 100, 100); 
        QueryBox box2 = new QueryBox(19000, 9000, 100, 100); 
        QueryBox box3 = new QueryBox(20000, 9000, 100, 100); 
        QueryBox box4 = new QueryBox(21000, 9000, 100, 100); 
        QueryBox box5 = new QueryBox(22000, 9000, 100, 100); 
        QueryBox box6 = new QueryBox(23000, 9000, 100, 100); 
		
        boxList.add(box1);
        boxList.add(box2);
        boxList.add(box3);
        boxList.add(box4);
        boxList.add(box5);
        boxList.add(box6);

        writer(boxList, 2);

    }

    public void runTask3() // 4 boxes, split into 2 pairs of upward trends (1 before noon, 1 after)
    {
        LinkedList<QueryBox> boxList = new LinkedList <QueryBox>();
        QueryBox box1 = new QueryBox(18000, 7200, 25, 50); //5  to 7 AM
        QueryBox box2 = new QueryBox(32400, 7200, 100, 50); // to 11AM
        QueryBox box3 = new QueryBox(46800, 7200, 25, 50); // 1 to 3 PM
        QueryBox box4 = new QueryBox(61200, 7200, 100, 50); //5 to 7 PM
        boxList.add(box1);
        boxList.add(box2);
        boxList.add(box3);
        boxList.add(box4);

        writer(boxList, 3);

    }
	
	// deprecated, not used in current implementation 
	public void trim(String in, String out)
	{
		BufferedReader in_file;
		FileWriter out_file; 
		
		String data_line; 
		String column; 
		StringTokenizer tokenizer; 
		int num_columns = 0; 
		int column_num; 
		boolean first_column; 
		
		int[] frequency_list = null;	
		String[] name_list = null; 
		
		try 
		{
			in_file = new BufferedReader(new FileReader(in)); 
			
			// scan file for @data section
			while((data_line = in_file.readLine()) != null)
			{
				if(data_line.equals("@data"))
					break; 
			}
			
			// read data, line by line
			while((data_line = in_file.readLine()) != null)
			{
				tokenizer = new StringTokenizer(data_line, ","); 
				column_num = 0; 
				
				if(frequency_list == null) // initialize variables
				{
					num_columns = tokenizer.countTokens(); 
					frequency_list = new int[num_columns];  
					name_list = new String[num_columns]; 
					
					// initialize frequency counts to 0
					for(int i = 0; i < num_columns; i++)
						frequency_list[i] = 0; 
				}
				
				// scan all the columns in the current row
				for(int i = 0; i < num_columns; i++)
				{
					column = tokenizer.nextToken(); 
					
					if(column.equals("TRUE"))
					{
						frequency_list[i]++; 
					}
				}
			}
			
			in_file.close(); 
			
			// reopen input file and scan to data section, reading attribute names along way
			in_file = new BufferedReader(new FileReader(in)); 
			column_num = 0; 
			String token; 
			while((data_line = in_file.readLine()) != null)
			{
				tokenizer = new StringTokenizer(data_line, " "); 
				
				if(tokenizer.hasMoreTokens())
				{
					token = tokenizer.nextToken(); 
					
					if(token.equals("@data"))
						break; 
					
					// save attribute name
					if(token.equals("@attribute"))
					{
						name_list[column_num] = tokenizer.nextToken(); 
						column_num++; 
					}
				}
			}
			
			out_file = new FileWriter(out);

			// set up relation section of arff file
			out_file.write("@relation nyse-trim\n\n");
			
			// print out attribute section
			for(int i = 0; i < num_columns; i++)
			{
				// only print this attribute name if it has a true in at least one row
				if(frequency_list[i] > 0)
				{
					out_file.write("@attribute " + name_list[i] + " { TRUE, FALSE }\n"); 
					
				}
			}
			
			out_file.write("\n\n@data\n"); 
			
			// read data, line by line
			while((data_line = in_file.readLine()) != null)
			{
				tokenizer = new StringTokenizer(data_line, ","); 
				first_column = true; 
				
				for(int i = 0; i < num_columns; i++)
				{
					column = tokenizer.nextToken(); 
					
					// only print this column if it has a true in at least 1 row
					if(frequency_list[i] > 0)
					{
						if(!first_column)
						{
							out_file.write(","); 
						}
						else 
						{
							first_column = false; 
						}
						out_file.write(column); 
					}
				}
				out_file.write("\n"); 
			}
			
			in_file.close(); 
			out_file.close(); 
			
		}
		catch(Exception e)
		{
			System.out.println("Exception: " + e.getMessage()); 
		}
	}

    public void finishTasks()
    {
        try
        {
             
            //partition_out_2.close(); 
            //partition_out_3.close();
			
			partition_out_1.close();
			query_out_1.close(); 
			rule_miner_input_1.close(); 
			partition_info_out.close(); 
		}
        catch(Exception e)
        {
            System.out.println(e.getMessage()); 
        }
    }
    
}
