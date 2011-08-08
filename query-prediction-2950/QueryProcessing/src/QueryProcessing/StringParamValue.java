/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package QueryProcessing;

/**
 * Class representing the string query parameter
 * @author alexta
 */
public class StringParamValue extends ParamValue implements Comparable<ParamValue>
{
    String m_strValue;

    public StringParamValue(String strValue)
    {
        super(ParamValue.PT_STRING);
        m_strValue = strValue;
    }

    public String getValue()
    {
        return m_strValue;
    }


    @Override
    public StringParamValue clone()
    {
        StringParamValue newValue = new StringParamValue(this.m_strValue);
        return newValue;
    }

    @Override
    public String toString()
    {
        return m_strValue;
    }

    @Override
    public int compareTo(ParamValue objToCompare)
    {
        StringParamValue strParamToCompare = (StringParamValue)objToCompare;
        if (strParamToCompare == null)
            return -1;
        return m_strValue.compareTo(strParamToCompare.getValue());
    }
}
