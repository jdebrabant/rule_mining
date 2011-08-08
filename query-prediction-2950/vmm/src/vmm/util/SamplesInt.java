/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vmm.util;

import java.util.*;

/**
 *
 * @author iscander
 */
public class SamplesInt
{
    int m_iNumSamples;
    int m_iTotalSize;

    ArrayList<ArrayList<Integer>> m_lstSamples;

    public SamplesInt()
    {
        m_lstSamples = new ArrayList<ArrayList<Integer>>();
        m_iNumSamples = 0;
        m_iTotalSize = 0;
    }

    public void AddSample(Collection<Integer> sample)
    {
        ArrayList<Integer> sampleLst = new ArrayList<Integer>();
        sampleLst.addAll(sample);
        m_lstSamples.add(sampleLst);

        m_iNumSamples++;
        m_iTotalSize += sample.size();
    }

    public int get(int sampleIndex, int index)
    {
        ArrayList<Integer> sample = m_lstSamples.get(sampleIndex);
        return sample.get(index);
    }

    public int size(int sampleIndex)
    {
        return m_lstSamples.get(sampleIndex).size();
    }

    public int size()
    {
        return m_lstSamples.size();
    }

    public int allLength()
    {
        return m_iTotalSize;
    }
}
