/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package QueryProcessing;

import java.util.*;

/**
 * Class that represents query. Includes query ID (identifiers the particular
 * query template) and the parameters
 * This class should be created by some sort of factory (query parser). Currently
 * this is a factory method that is designed to be used with the given dataset
 * The parser that would handle all sorts of queries must be implemented
 * @author alex tarvo
 */
public class Query {

    /*
     * template ID of the query template for this query
     */
    int m_iType;

    /*
     * Parameter values for this query
     */
    ArrayList<ParamValue> m_arrParamValues;

    /*
     * Private constructor
     */
    Query(int iType)
    {
        m_arrParamValues = new ArrayList<ParamValue>();
        m_iType = iType;
    }

    /*
     * Adds a parameter to the query. Should be used by the query parser
     */
    void addParam(ParamValue param)
    {
        m_arrParamValues.add(param);
    }

    /*
     * Get the query ID
     */
    public int getType()
    {
        return m_iType;
    }

    /*
     * Get parameter values
     */
    public ArrayList<ParamValue> getParamValues()
    {
        return m_arrParamValues;
    }

    /*
     * Facory method that generates the query from its string representation.
     * This is a temporary solution that works only for a given dataset
     * A general-purpose scanner for parsing all kinds of SQL queries must be
     * implemented
     */
    public static Query createQuery(String strQuery)
    {
        int iQueryID = -1;
        if (strQuery.startsWith("begin"))
        {
            iQueryID = 1;
        }else if (strQuery.startsWith("select"))
        {
            iQueryID = 2;
        }else if (strQuery.startsWith("insert"))
        {
            iQueryID = 3;
        }else if (strQuery.startsWith("commit"))
        {
            iQueryID = 4;
        }
        if (iQueryID == -1)
        {
            System.err.println(String.format("Query of unknown type: %s", strQuery));
            return null;
        }

        Query queryRet = new Query(iQueryID);

        String[] strParams = strQuery.split("'");

        for (int i=1; i<strParams.length; i+=2)
        {
            ParamValue param = ParamValue.createParamValue(strParams[i]);
            queryRet.addParam(param);
        }

        return queryRet;
    }
}
