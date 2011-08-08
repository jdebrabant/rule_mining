/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package QueryProcessing;

/**
 * Class representing the integer query parameter
 * @author alexta
 */
public class IntParamValue extends ParamValue implements Comparable<ParamValue>
{
    int m_iValue;

    public IntParamValue(int iValue)
    {
        super(ParamValue.PT_INT);
        m_iValue = iValue;
    }

    public int getValue()
    {
        return m_iValue;
    }

    @Override
    public IntParamValue clone()
    {
        IntParamValue newValue = new IntParamValue(this.m_iValue);
        return newValue;
    }

    @Override
    public String toString()
    {
        return Integer.toString(m_iValue);
    }

    @Override
    public int compareTo(ParamValue objToCompare)
    {
        IntParamValue intParamToCompare = (IntParamValue)objToCompare;
        if (intParamToCompare == null)
            return -1;
        return m_iValue-intParamToCompare.getValue();
    }
}
