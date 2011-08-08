/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mc;
import java.util.*;

/**
 *
 * @author iscander
 */
public class MarkovChainSimple {
    double m_arrTransMat[][];
    int m_iAlphabetSize;

    public MarkovChainSimple(int iAlphabetSize)
    {
        m_iAlphabetSize = iAlphabetSize;
        m_arrTransMat = new double[m_iAlphabetSize][m_iAlphabetSize];
        for (int i=0; i<m_iAlphabetSize; i++)
            for (int j=0; j<m_iAlphabetSize; j++)
                m_arrTransMat[i][j] = 0.0;
    }

    public void build(ArrayList<Integer> trainSeq)
    {
        int iNext = -1;
        int iCurrent = -1;
        
        for (int i=0; i<trainSeq.size()-1; i++)
        {

            iCurrent = trainSeq.get(i);
            iNext = trainSeq.get(i+1);

            m_arrTransMat[iCurrent][iNext] += 1;
        }

        for (int i=0; i<m_iAlphabetSize; i++)
        {
            double sum = 0;
            for (int j=0; j<m_iAlphabetSize; j++)
                sum+=m_arrTransMat[i][j];

            for (int j=0; j<m_iAlphabetSize; j++)
                m_arrTransMat[i][j] /= sum;
        }

    }

    public void print()
    {
        for (int i=0; i<m_iAlphabetSize; i++)
        {
            for (int j=0; j<m_iAlphabetSize; j++)
            {
                System.out.format("%3f\t", m_arrTransMat[i][j]);
            }
            System.out.println();
        }
    }
}
