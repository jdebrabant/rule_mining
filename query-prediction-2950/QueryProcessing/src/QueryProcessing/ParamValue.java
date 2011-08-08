/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package QueryProcessing;

/**
 * Abstract class that represents query parameter. There can be
 * different types of parameters; currently we support integer and string
 * Derived classes can handle parameter cloning, comparison etc
 * @author alexta
 */
public abstract class ParamValue implements Comparable<ParamValue>
{
    /*
     * Type of the param (int/float/string)
     */
    int m_iType;

    public final static int PT_INT = 0;
    public final static int PT_FLOAT = 1;
    public final static int PT_STRING = 2;

    /*
     * Constructor. Should be called by the factory
     */
    ParamValue(int iType)
    {
        m_iType = iType;
    }

    public int getType()
    {
        return m_iType;
    }

    /*
     * Factory for creating parameters
     */
    public static ParamValue createParamValue(String strValue)
    {

        try
        {
            int val = Integer.parseInt(strValue);
            return new IntParamValue(val);
        }catch(NumberFormatException e)
        {
        }

        try
        {
            float val = Float.parseFloat(strValue);
            return new FloatParamValue(val);
        }catch(NumberFormatException e)
        {
        }

        return new StringParamValue(strValue);
    }

    @Override
    public ParamValue clone() throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException("Abstract class ParamValue does not support clone(); only its children do");
    }

    @Override
    public String toString()
    {
        return "";
    }

    public int compareTo(ParamValue objToCompare)
    {
        return 0;
    }

}
