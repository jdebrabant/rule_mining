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
		return (partition_id + " " + x_min + " " + x_max + " " + y_min + " " + y_max + "\n"); 
	}
	
	public String toSQL()
	{
		return ("SELECT * FROM quote " + 
				"WHERE time > " + x_min+ " AND time < " + x_max + 
				" AND price > " + y_min + " AND price < " + y_max + "\n");
	}
}