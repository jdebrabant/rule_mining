/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package paramminer;

import java.util.*;
import QueryProcessing.*;

/**
 * Class representing the association rule mined by the Weka
 * @author iscander
 */
public class Rule
{
    /*
     * List of previous query Ids to be matched against the sequence
     */
    int[] m_lstPrevQueryIDs;
    /*
     * Current query ID
     */
    int m_iCurrentQueryID;
    /*
     * Parameter index (param no.)
     */
    int m_iCurrentParamIdx;
    /*
     * Index of the source query from which the value of the attribute
     * should be used to calculate the value of the given param for the
     * current query
     */
    int m_iSourceQueryIdx;
    /*
     * Index of the parameter from the sources query whose attribute
     * should be used to calculate the value of the given param for the
     * current query
     */
    int m_iSourceParamIdx;

    /*
     * Action for the source parameter. Should it be left unchanged, incremented
     * or decremented. See a set of RA_XXX constants
     */
    int m_iAction;

    static final int RA_Assign=0;
    static final int RA_Inc=1;
    static final int RA_Dec=2;

    public Rule()
    {
        
    }

    /*
     * Initialize the rule
     */
    public void Initialize(int[] lstQueryIDs,
            int iCurrentQueryID,
            int iCurrentParamIdx,
            int iSourceQueryIdx,
            int iSourceParamIdx,
            int iAction) throws Exception
    {
        m_lstPrevQueryIDs = lstQueryIDs;

        m_iCurrentParamIdx = iCurrentParamIdx;
        
        if (iCurrentQueryID < 0)
            throw new Exception("Invalid current query ID "+iCurrentQueryID);
        m_iCurrentQueryID = iCurrentQueryID;

        if ((iSourceQueryIdx < 0) && (iSourceQueryIdx >= lstQueryIDs.length))
            throw new Exception("Invalid source query index "+iSourceQueryIdx);
        m_iSourceQueryIdx = iSourceQueryIdx;

        if (iSourceParamIdx < 0)
            throw new Exception("Invalid source parameter index "+iSourceParamIdx);
        m_iSourceParamIdx = iSourceParamIdx;

        if ((iAction < RA_Assign)||(iAction > RA_Dec))
            throw new Exception("Invalid action code "+iAction);
        m_iAction = iAction;
    }

    /*
     * Match the rule against the sequence of previous queries and current query
     * If match is successful, return the predicted parameter value
     */
    public ParamValue MatchAndPredict(ArrayList<Query> lstPrevQuerys, 
            int iCurrentQueryID,
            int iCurrentParamIdx) throws Exception
    {
        if (m_lstPrevQueryIDs.length != lstPrevQuerys.size())
            return null;
        if (iCurrentQueryID != m_iCurrentQueryID)
            return null;
        if (m_iCurrentParamIdx != iCurrentParamIdx)
            return null;
        
        Query sourceQuery = lstPrevQuerys.get(m_iSourceQueryIdx);
        ParamValue sourceParamValue = sourceQuery.getParamValues().get(m_iSourceParamIdx);

        for (int i=0; i<lstPrevQuerys.size(); i++)
        {
            if ((m_lstPrevQueryIDs[i] != -1) && (lstPrevQuerys.get(i).getType() != m_lstPrevQueryIDs[i]))
                return null;
        }
        if (m_iAction == RA_Assign)
            return sourceParamValue.clone();
        else if ((m_iAction == RA_Inc) || (m_iAction == RA_Dec))
        {
            if (sourceParamValue.getType() == ParamValue.PT_STRING)
                throw new Exception("Incorrect combination of parameter type (string) and action (increment/decrement)");
            if (sourceParamValue.getType() == ParamValue.PT_INT)
            {
                if (m_iAction == RA_Inc)
                    return new IntParamValue(((IntParamValue)sourceParamValue).getValue()+1);
                else if (m_iAction == RA_Dec)
                    return new IntParamValue(((IntParamValue)sourceParamValue).getValue()-1);
                else
                    throw new Exception("Unknown action "+m_iAction);
            }else if (sourceParamValue.getType() == ParamValue.PT_FLOAT)
            {
                if (m_iAction == RA_Inc)
                    return new FloatParamValue(((FloatParamValue)sourceParamValue).getValue()+1);
                else if (m_iAction == RA_Dec)
                    return new FloatParamValue(((FloatParamValue)sourceParamValue).getValue()-1);
                else
                    throw new Exception("Unknown action "+m_iAction);
            }
        }
        throw new Exception("Failed to take an action for the rule");
    }
}
