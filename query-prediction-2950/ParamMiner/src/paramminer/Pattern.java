/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package paramminer;

import java.util.*;

/**
 * Class that should represent a sequence of queries. Currently used to convert
 * the sequence of queries, current query ID and the parameter index to the
 * actual pattern that will be fed to the Weka
 * @author iscander
 */
public class Pattern {

    /*
     * Convert the query pattern to the string
     * @param iQueryIDs: list of previous query IDs
     * @param iCurrentQueryID: current query ID
     * @param iCurrentParamIdx: index of the parameter for which we are creating a pattern
     * @param strConsequent: consequent (the right hand of the rule), which contains actual pattern
     */
    public static String patternToString(ArrayList<Integer> iQueryIDs, int iCurrentQueryID, int iCurrentParamIdx, String strConsequent)
    {
        StringBuilder strRet = new StringBuilder();
        if (iQueryIDs.size() >0)
        {

            //strRet.append(String.format("Q(t-%d)=%d", iQueryIDs.size(), iQueryIDs.get(0).intValue()));
            for (int i=0; i<iQueryIDs.size(); i++)
            {
                //strRet.append(String.format("\"Q(t-%d)=%d\"", iQueryIDs.size()-i, iQueryIDs.get(i).intValue()));
                strRet.append(String.format("\"Q%d\"", iQueryIDs.get(i).intValue()));
                strRet.append(",");
            }

            //strRet.append(String.format("\"Q(t)=%d\"", iCurrentQueryID));
            strRet.append(String.format("\"Q%d\"", iCurrentQueryID));
            strRet.append(",");
            strRet.append(String.format("\"P%d\"", iCurrentParamIdx));
            if (!strConsequent.equals(""))
            {
                strRet.append(",");
                strRet.append("\"");
                strRet.append(strConsequent);
                strRet.append("\"");
            }
        }
        return strRet.toString();
        
    }
/*
    @Override
    public String toString()
    {
        return patternToString(m_iPrevQueryIDs, m_iCurrentQueryID, m_strConsequent);
    }
*/
}
