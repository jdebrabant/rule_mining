/*
 *  mem_allocate.h
 *  
 *
 *  Created by Justin on 10/29/11.
 *  Copyright 2011 __MyCompanyName__. All rights reserved.
 *
 */

#include <iostream>
#include <cstdlib>
#include <stdio.h>
#include <string.h>
#include <sys/types.h>


int main(int argc, char* argv[])
{
	int num_children; 
	pid_t pid; 
	char* byte_array; 
	int bytes_in_gb; 
	
	num_children = atoi(argv[1]); 
	bytes_in_gb = 1024 * 1024 * 1024; 
	
	for(int i = 0; i < num_children; i++)
	{
		pid = fork(); 
		
		if(pid == 0) // we are in the child process, so allocate a 1GB array 
		{
			byte_array = new char[bytes_in_gb]; 
			memset(byte_array, '-', bytes_in_gb); // reset memory 
			
			while(1) // infinite loop to hold memory 
			{
			}
		}
	}
	
	while(1) // infinite loop to keep parent process running
	{
	}
	
	return 0; 
}