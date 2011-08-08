/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mc;

import java.io.*;
import java.util.*;

/**
 *
 * @author iscander
 */
public class FileUtil {


    //Read the sequence of the numbers from the .csv file
    public static void ReadFromFile(String strFileName, ArrayList<Integer> output)
    {
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(strFileName));
            String strText;

            while  ((strText = reader.readLine()) != null)
            {
                String strNumbers[] = strText.split(",");
                for (String strNumber:strNumbers)
                {
                    int iNum = Integer.parseInt(strNumber);
                    output.add(iNum);
                }
            }
        }catch(FileNotFoundException e)
        {
            System.out.println("File "+strFileName + " could not be found");
        }
        catch(IOException e)
        {
            System.out.println("Failed reading data from the file "+strFileName + ": "+e.getMessage());
        }
    }

    //Save an array to the .csv file
    public static void WriteToFile(String strFullFileName, ArrayList objToSave)
    {
        BufferedWriter writer = null;
        try
        {
            // Create file
            FileWriter fstream = new FileWriter(strFullFileName);
            writer = new BufferedWriter(fstream);
        }catch (Exception e)
        {
            System.out.println("Failed to open file "+strFullFileName +" for writing: " + e.getMessage());
        }

        try
        {
            //if (!objToSave.isEmpty())
            //    writer.write(objToSave.get(0).toString());
            for (int i=0; i<objToSave.size(); i++)
            {
                writer.write(objToSave.get(i).toString());
            }
            writer.write("\n");
        }catch (Exception e)
        {
            System.out.println("Failed to write data to the output file "+strFullFileName +": " + e.getMessage());
        }

        try
        {
            writer.close();
        }catch(Exception e)
        {
            System.out.println("Failed to close the output file "+strFullFileName +": " + e.getMessage());
        }
    }
}
