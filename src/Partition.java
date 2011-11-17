/***************************************************************************************************
 * File: Partition.java
 * Authors: Justin A. DeBrabant (debrabant@cs.brown.edu)
 * Description: 

 ****************************************************************************************************/

import java.util.*; 

public class Partition
{
	public int x_min; 
	public int x_max; 
	public double y_min; 
	public double y_max; 
	
	public String partition_name; 
	public int partition_id; 
	
	public Partition()
	{
		x_min = x_max = partition_id = -1; 
		y_min = y_max = -1;  
		
		partition_name = ""; 
	}
	
	public Partition(int xmin, int xmax, double ymin, double ymax, String name, int id)
	{
		x_min = xmin; 
		x_max = xmax; 
		y_min = ymin; 
		y_max = ymax; 
		
		partition_name = name; 
		partition_id = id;  
	}
	
	public String toString()
	{
		return (new String(partition_id + " " + x_min + " " + x_max + " " + y_min + " " + y_max + "\n")); 
	}
	
	public String toSQL()
	{
		x_max = x_min + 2000; 
		y_max = y_min + 5; 
		return ("SELECT * FROM quote " + 
				"WHERE quote_time > " + x_min+ " AND quote_time < " + x_max + 
				" AND offer_price > " + y_min + " AND offer_price < " + y_max + "\n");
	}
}