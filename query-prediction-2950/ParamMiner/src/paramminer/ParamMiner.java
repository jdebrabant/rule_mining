/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package paramminer;

import Utils.*;
import java.util.*;
import java.io.*;
import QueryProcessing.*;

/**
 *
 * @author alexta
 */
public class ParamMiner {

    CmdLineParser parser;

    //Command line option that denotes action
    CmdLineParser.Option actionFlag;
    CmdLineParser.Option fileInFlag;
    CmdLineParser.Option fileOutFlag;
    CmdLineParser.Option depthFlag;

    String m_strFilesIn;
    String m_strFileOut;
    String m_strAction;
    int m_iDepth;

    ArrayList<String> m_arrResult;

    ArrayList<Rule> m_arrRules;

    /*
     * Constructor
     */
    public ParamMiner()
    {
        m_arrResult = new ArrayList<String>();
        m_arrRules = new ArrayList<Rule>();

        parser = new CmdLineParser();

        //
        //Options for parsing both Logger and STap logs
        //
        //Action to be taken:
        //mineAssoc - prepare training data for the weka
        //predict - predict data using the set of hardcoded rules
        actionFlag = parser.addStringOption('a', "action");
        //Path to the input log file
        fileInFlag = parser.addStringOption('i', "fileIn");
        //Path to the output file (either training dataset for Weka or prediction)
        fileOutFlag = parser.addStringOption('o', "fileOut");
        //How many previous queries will be considered
        depthFlag = parser.addIntegerOption('d', "depth");
    }

    /*
     * Save list of entries to the file, every entry in the separate line
     */
    public void SaveListToFile(String strFullFileName, List<?> lstToSave) throws Exception
    {
        BufferedWriter writer = null;
        try
        {
            // Create file
            FileWriter fstream = new FileWriter(strFullFileName);
            writer = new BufferedWriter(fstream);
        }catch (Exception e)
        {
            throw new Exception("Failed to open file "+strFullFileName +" for writing: " + e.getMessage(), e);
        }

        try
        {
            for(Object lstEntry: lstToSave)
            {
                writer.write(lstEntry.toString());
                writer.write("\n");
            }
        }catch (Exception e)
        {
            throw new Exception("Failed to write data to the output file "+strFullFileName +": " + e.getMessage(), e);
        }

        try
        {
            writer.close();
        }catch(Exception e)
        {
            throw new Exception("Failed to close the output file "+strFullFileName +": " + e.getMessage(), e);
        }
    }

    /*
     * Get a sequence from the source array and write it into the different array (destination)
     * The sequence starts from the (iStartIdx - iDepth) index and spans
     * until the iStartIdx index of the source array
     *
     * @param arrSource: source array
     * @param iStartIdx: starting index for the sequence in the source array. The
     * sequence will be
     * @param iDepth: length of the sequence
     * @param arrDestination: destination array
     */
    private void getArraySubset(ArrayList arrSource, int iStartIdx, int iDepth, ArrayList arrDestination) throws Exception
    {
        arrDestination.clear();
        if (iStartIdx - iDepth < 0)
        {
            throw new Exception("Start index cannot be less than depth");
        }
        if (iStartIdx >= arrSource.size())
            throw new Exception("Start index is beyond the array boundary");

        for (int i=iStartIdx-iDepth; i<iStartIdx; i++)
        {
            arrDestination.add(arrSource.get(i));
        }
    }

    public void PredictAndVerify()
    {
        
    }

    /*
     * Form rules out of current query and previous "depth" queries
     *
     * We are forming rules in form
     * <List of prev. query IDs> <current query ID> <current param index> <consequent>
     * Here consequent is the actual pattern, e.g. one that captures the fact that
     * current parameter is same as the 3-rd parameter of the previous query
     *
     * Patterns are represented as strings and saved into the text file
     */
    public void formPattern(ArrayList<Query> arrPrevQueries, Query currentQuery)
    {
        ArrayList<ParamValue> arrParamValuesCurrent = currentQuery.getParamValues();
        ArrayList<Integer> arrQueryIDs = new ArrayList<Integer>();

        for (int i=0;i<arrPrevQueries.size();i++)
        {
            arrQueryIDs.add(arrPrevQueries.get(i).getType());
        }

        //Scan all the parameters of the current query
        for (int currParamIdx=0; currParamIdx<arrParamValuesCurrent.size(); currParamIdx++)
        {
            boolean bAssocFound = false;
            //i-th parameter for the current query
            ParamValue paramValCurrent = arrParamValuesCurrent.get(currParamIdx);

            //Scan all the previous queries
            for (int prevQueryIdx = 0; prevQueryIdx<arrPrevQueries.size(); prevQueryIdx++)
            {
                int j=arrPrevQueries.size() - prevQueryIdx;
                //j-th query
                Query prevQuery = arrPrevQueries.get(prevQueryIdx);
                ArrayList<ParamValue> arrParamValuesPrev = prevQuery.getParamValues();

                //Scan all the parameters for the j-th query
                for (int prevParamIdx=0; prevParamIdx<arrParamValuesPrev.size(); prevParamIdx++)
                {
                    //k-th param of the j-th query
                    ParamValue paramValPrev = arrParamValuesPrev.get(prevParamIdx);

                    //Current param type match k-th param of the j-th query
                    if (paramValCurrent.getType() == paramValPrev.getType())
                    {
                        switch(paramValCurrent.getType())
                        {
                            case ParamValue.PT_STRING:
                                String strValCurrent = ((StringParamValue)paramValCurrent).getValue();
                                String strValPrev = ((StringParamValue)paramValPrev).getValue();
                                if (strValPrev.equals(strValCurrent))
                                {
                                    String strConsequent = String.format("P(t-%d;%d)", j, prevParamIdx);
                                    String strRule = Pattern.patternToString(arrQueryIDs, currentQuery.getType(), currParamIdx, strConsequent);
                                    m_arrResult.add(strRule);
                                    bAssocFound = true;
                                }
                                break;
                            case ParamValue.PT_FLOAT:
                                float fValCurrent = ((FloatParamValue)paramValCurrent).getValue();
                                float fValPrev = ((FloatParamValue)paramValPrev).getValue();
                                if (Math.abs(fValPrev - fValCurrent) < 0.000001 )
                                {
                                    String strConsequent = String.format("P(t-%d;%d)", j, prevParamIdx);
                                    String strRule = Pattern.patternToString(arrQueryIDs, currentQuery.getType(), currParamIdx, strConsequent);
                                    m_arrResult.add(strRule);
                                    bAssocFound = true;
                                }
                                break;
                            case ParamValue.PT_INT:
                                int iValCurrent = ((IntParamValue)paramValCurrent).getValue();
                                int iValPrev = ((IntParamValue)paramValPrev).getValue();
                                if (iValCurrent == iValPrev)
                                {
                                    String strConsequent = String.format("P(t-%d;%d)", j, prevParamIdx);
                                    String strRule = Pattern.patternToString(arrQueryIDs, currentQuery.getType(), currParamIdx, strConsequent);
                                    m_arrResult.add(strRule);
                                    bAssocFound = true;
                                }else if (iValCurrent == iValPrev+1)
                                {
                                    String strConsequent = String.format("P(t-%d;%d)+1", j, prevParamIdx);
                                    String strRule = Pattern.patternToString(arrQueryIDs, currentQuery.getType(), currParamIdx, strConsequent);
                                    m_arrResult.add(strRule);
                                    bAssocFound = true;
                                }else if (iValCurrent == iValPrev-1)
                                {
                                    String strConsequent = String.format("P(t-%d;%d)-1", j, prevParamIdx);
                                    String strRule = Pattern.patternToString(arrQueryIDs, currentQuery.getType(), currParamIdx, strConsequent);
                                    m_arrResult.add(strRule);
                                    bAssocFound = true;
                                }
                                break;
                        }
                    }
                }
            }

            if (!bAssocFound)
            {
                String strRule = Pattern.patternToString(arrQueryIDs, currentQuery.getType(), currParamIdx, "NA");
                m_arrResult.add(strRule);
            }

        }
        
    }

    /*
     * Create the training set. It will be used to generate association rules
     */
    public void doTrain()
    {
        ArrayList<Query> arrSubset = new ArrayList();
        LogReader logReader = new LogReader();
        try
        {
            //Read input data
            String arrFiles[] = m_strFilesIn.split(",");
            for(String strFile:arrFiles)
            {
                logReader.ReadFromFile(strFile);
            }
            ArrayList<Query> arrQueries = logReader.getQueries();

            StringBuilder header = new StringBuilder();
            for (int i=m_iDepth; i>=1; i--)
            {
                header.append(String.format("Q(t-%d)",i));
                header.append(",");
            }
            header.append("Q(t)");
            header.append(",");
            header.append("paramIdx");
            header.append(",");
            header.append("antecedent");
            m_arrResult.add(header.toString());

            //For each query, scan previous "depth" queries and form rule, if possible
            for (int i=m_iDepth; i< arrQueries.size(); i++)
            {
                getArraySubset(arrQueries,i, m_iDepth,arrSubset);
                formPattern(arrSubset, arrQueries.get(i));
            }
            //Save data to the output file
            SaveListToFile(m_strFileOut, m_arrResult);

        }catch (Exception e)
        {
            System.err.println("Exception while obtaining training data:");
            System.err.println(e.getMessage());
            for(StackTraceElement element:e.getStackTrace())
                System.err.println(element.toString());
            System.exit(1);
        }
        
    }

    /*
     * Do prediction using the statically defined set of rules
     */
    public void doPredict()
    {
        int iTotalParams=0;
        int iTotalPredictions=0;
        int iPredictedCorrect=0;
        //
        // Here we are creating a set of rules
        // BUGBUG: these rules are created statically for the depth=5
        //
        try
        {
            Rule rule1 = new Rule();
            rule1.Initialize(new int[]{2, -1, -1, -1}, 2, 0, 0, 0, Rule.RA_Assign);
            m_arrRules.add(rule1);

            Rule rule2 = new Rule();
            rule2.Initialize(new int[]{3, -1, -1, -1}, 3, 1, 0, 1, Rule.RA_Inc);
            m_arrRules.add(rule2);

            Rule rule3 = new Rule();
            rule3.Initialize(new int[]{3, -1, -1, -1}, 3, 0, 0, 0, Rule.RA_Inc);
            m_arrRules.add(rule3);
        }catch(Exception e)
        {
            System.err.println("Exception while creating rules:");
            System.err.println(e.getMessage());
            for(StackTraceElement element:e.getStackTrace())
                System.err.println(element.toString());
            System.exit(1);
        }

        //
        // Here we are doing actual prediction
        //
        m_arrResult.add("QueryNo,QueryID,ParamIdx,RuleNo,ActualValue,PredictedValue,IsCorrect");
        ArrayList<Query> arrPrevQuerySubset = new ArrayList();
        LogReader logReader = new LogReader();
        try
        {
            //Read input data
            logReader.ReadFromFile(m_strFilesIn);
            ArrayList<Query> arrQueries = logReader.getQueries();

            //For each query, scan previous "depth" queries 
            for (int i=m_iDepth; i< arrQueries.size(); i++)
            {
                getArraySubset(arrQueries,i, m_iDepth,arrPrevQuerySubset);
                Query currentQuery = arrQueries.get(i);
                
                for (int iCurrentParamIdx=0; iCurrentParamIdx<currentQuery.getParamValues().size();iCurrentParamIdx++)
                {   //Scan all the query params

                    iTotalParams++;

                    for (int iRuleIdx = 0; iRuleIdx<m_arrRules.size(); iRuleIdx++)
                    {   //Scan all the rules and try to find a match for the combination of
                        //previous queries IDs, current query ID, and parameter index

                        Rule rule = m_arrRules.get(iRuleIdx);
                        ParamValue predicted = rule.MatchAndPredict(arrPrevQuerySubset, 
                                currentQuery.getType(),
                                iCurrentParamIdx);
                        if (predicted != null)
                        {   //Got a match!
                            String strRecord = String.format("%d,%d,%d,%d,", i, currentQuery.getType(), iCurrentParamIdx, iRuleIdx);
                            iTotalPredictions++;

                            String strCorrect=",FAIL";
                            //Now assess the accuracy
                            if (predicted.compareTo(currentQuery.getParamValues().get(iCurrentParamIdx)) == 0)
                            {
                                iPredictedCorrect++;
                                strCorrect=",OK";
                            }

                            m_arrResult.add(strRecord+
                                    currentQuery.getParamValues().get(iCurrentParamIdx).toString()+
                                    ","+
                                    predicted.toString()+
                                    strCorrect);

                            //Don't look for other rules
                            break;
                        }
                    }
                }
            }

            //Save data to the output file
            SaveListToFile(m_strFileOut, m_arrResult);

            double dblMatchRate = (double)iTotalPredictions/iTotalParams;
            double dblCorrectRate = (double)iPredictedCorrect/iTotalParams;
            System.out.println(String.format("Predicted %d out of %d parameter values. Match rate=%f",
                    iTotalPredictions, iTotalParams, dblMatchRate));
            System.out.println(String.format("Correctly predicted %d out of %d parameter values. Accuracy=%f",
                    iPredictedCorrect, iTotalParams, dblCorrectRate));
            
        }catch (Exception e)
        {
            System.err.println("Exception while doing prediction:");
            System.err.println(e.getMessage());
            for(StackTraceElement element:e.getStackTrace())
                System.err.println(element.toString());
            System.exit(1);
        }
    }

    public void run(String[] args)
    {
        //Parse command line
        try
        {
            parser.parse(args);
        } catch (CmdLineParser.OptionException e)
        {
            System.err.println("Error parsing command line:");
            System.err.println(e.getMessage());
            for(StackTraceElement element:e.getStackTrace())
                System.err.println(element.toString());
            System.exit(1);
        }

        m_strFilesIn = (String)parser.getOptionValue(fileInFlag);
        if (m_strFilesIn == null)
        {
            System.err.println("Path to the input file must be specified");
            System.exit(1);
        }

        m_strFileOut = (String)parser.getOptionValue(fileOutFlag);
        if (m_strFileOut == null)
        {
            System.err.println("Path to the output file must be specified");
            System.exit(1);
        }

        m_strAction = (String)parser.getOptionValue(actionFlag);
        if (m_strAction == null)
        {
            System.err.println("Action must be specified");
            System.exit(1);
        }

        m_iDepth = ((Integer)parser.getOptionValue(depthFlag)).intValue();
        if (m_iDepth <=0)
        {
            System.err.println("Depth must be > 0");
            System.exit(1);
        }

        if (m_strAction.equals("mineAssoc"))
        {   //perform training
            doTrain();
        }else if (m_strAction.equals("predict"))
        {   //perform prediction
            doPredict();
        }else
        {
            System.err.println("Action must be either \"mineAssoc\" or \"predict\"");
            System.exit(1);
        }

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        ParamMiner miner = new ParamMiner();
        miner.run(args);
    }

}
