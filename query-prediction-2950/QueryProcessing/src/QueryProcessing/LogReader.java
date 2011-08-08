/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package QueryProcessing;

import java.io.*;
import java.util.*;

/**
 * Class that reads the set of queries and uses the parser (now a factory method
 * of the Query class) to parse the query
 *
 * @author Alex Tarvo
 */
public class LogReader {
    /*
     * Line of the log file currently being processed
     */
    protected int m_iCurrentLine;

    /*
     * Resulting set of queries
     */
    ArrayList<Query> m_arrQueries;

    public LogReader()
    {
        m_arrQueries = new ArrayList<Query>();
    }

    /*
     * Read the set of queries from the file
     */
    public void ReadFromFile(String strFileName) throws Exception
    {
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(strFileName));
            String strText;
            m_iCurrentLine=1;

            while  ((strText = reader.readLine()) != null)
            {
                Query query = Query.createQuery(strText);
                m_arrQueries.add(query);
                m_iCurrentLine++;
            }
        }catch(FileNotFoundException e)
        {
            throw new Exception("File "+strFileName + " could not be found", e);
        }
        catch(IOException e)
        {
            throw new Exception("Failed reading data from the file "+strFileName + ": "+e.getMessage(), e);
        }
    }

    public ArrayList<Query> getQueries()
    {
        return m_arrQueries;
    }


}
