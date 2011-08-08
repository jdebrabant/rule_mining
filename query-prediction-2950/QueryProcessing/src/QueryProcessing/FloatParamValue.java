/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package QueryProcessing;

/**
 * Class representing the float query parameter
 * @author alexta
 */
public class FloatParamValue extends ParamValue implements Comparable<ParamValue>
{
    float m_fValue;

    public FloatParamValue(float fValue)
    {
        super(ParamValue.PT_FLOAT);
        m_fValue = fValue;
    }

    public float getValue()
    {
        return m_fValue;
    }

    @Override
    public FloatParamValue clone()
    {
        FloatParamValue newValue = new FloatParamValue(this.m_fValue);
        return newValue;
    }

    @Override
    public String toString()
    {
        return Float.toString(m_fValue);
    }

    @Override
    public int compareTo(ParamValue objToCompare)
    {
        FloatParamValue floatParamToCompare = (FloatParamValue)objToCompare;
        if (floatParamToCompare == null)
            return -1;
        double res = m_fValue - floatParamToCompare.getValue();
        if (res < -0.00000001)
            return -1;
        if (res > 0.00000001)
            return 1;
        return 0;
    }
}
