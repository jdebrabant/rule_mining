/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mc;

import vmm.*;
import java.util.*;

import Utils.*;
import QueryProcessing.*;
/**
 * Class trains and predicts data using the vMM.
 * @author Alex Tarvo
 */
public class MCCheck {

    /*
     * Class that generates a multinomial distribution. Used to verify the
     * prediction done by the vMM
     */
    class Multinom
    {
        Random rnd;
        public ArrayList<Double> arrCumProbabilities;

        public Multinom()
        {
            rnd = new Random();
            arrCumProbabilities = new ArrayList<Double>();
        }

        //Set array of probabilities for each class
        public void setProbabilities(ArrayList<Double> arrProbabilities)
        {
            arrCumProbabilities.clear();
            arrCumProbabilities.add(arrProbabilities.get(0));
            for (int i=1; i<arrProbabilities.size(); i++)
            {
                arrCumProbabilities.add(arrCumProbabilities.get(i-1)+arrProbabilities.get(i));
            }
        }

        //Generate a random number from the multinomial distribution
        //according to the probabilities being set earlier
        public int generate()
        {
            double rndNext = rnd.nextDouble();
            for (int i=0; i<arrCumProbabilities.size(); i++)
            {
                if (rndNext < arrCumProbabilities.get(i))
                    return i;
            }
            return -1;
        }
    }


    //
    //Datasets used for training/testing/verification
    //
    /*
     * The training set. Each query is represented as a number; different
     * numbers correspond to different query templates
     */
    public ArrayList<Integer> trainSeq;
    /*
     * Original test set. Contains actual query IDs
     */
    public ArrayList<Integer> testSeq;
    /*
     * Prediction for the given test set. Contains predicted query IDs
     */
    public ArrayList<Integer> predictSeq;

    /*
     * File to contain the output table
     */
    ArrayList<String> m_arrResult;

    //
    //PST prediction data
    //
    /*
     * PST tree object (variable-order Markov network)
     */
    PSTPredictor predictor;

    //
    //Parameters of the PST tree
    //

    /*
     * Alphabet size for the input sequence (number of unique query templates)
     */
    int m_iAlphabetSize=5;

    /*
     * Minimum frequency of the subsequence in the training set. Subsequences
     * with frequency less than this one won't be remembered
     */
    double m_dblSubsequenceMinFreq = 0.001;

    /*
     * minimal pprobability of the next template given the previous sequence, when the
     * sequence will be presented as a distinct node in the PST tree (min "meanningfullness" of the sequence)
     */
    double alpha = 0.01;

    /*
     * probability for the unseen template - for normalization
     */
    double gamma = 0.001;
    
    /*
     * minimum information gain for the symbol s': P(next sym|sequence,s')/P(next sym|sequence)
     * when the symbol and the sequence will be presented as a distinct node
     */
    double r = 1.05;

    /*
     * maximum order of the Variable Markov Model (vmm)
     */
    int m_iVMMOrder = 5;

    //
    // Prediction parameters
    //
    /*
     * generate the predicted value according to the MAP principle. That's what
     * should be used during the actual run
     */
    boolean m_bPredictMAP;
    /*
     * generate the predicted value randomly from the distribution given by the
     * PST tree. Used to test the vMM using the random Markov sequence
     */
    boolean m_bPredictRandom;

    //
    //Verification parameters
    //
    Multinom multinom;
    //Transition matrix obtained from the training set
    public double transmat_train[][];
    //Transition matrix obtained from the verification set
    public double transmat_verify[][];


    /*
     * Constructor. Allocate all the arrays
     */
    MCCheck()
    {

        trainSeq = new ArrayList<Integer>();
        testSeq = new ArrayList<Integer>();
        predictSeq = new ArrayList<Integer>();

        multinom = new Multinom();

        m_arrResult = new ArrayList<String>();
    }

    /*
     * Initialize the PST with the specified values.
     */
    void initialize()
    {
        predictor = new PSTPredictor();
        predictor.init(m_iAlphabetSize, m_dblSubsequenceMinFreq, alpha, gamma, r, m_iVMMOrder);
    }

    /*
     * Train the PST using the input sequence
     */
    void train()
    {
        predictor.learn(trainSeq);
        System.out.println("Training complete");
    }

    /*
     * Predict data for the given test sequence testSeq. Prediction results will 
     * be placed to the predictSeq array
     */
    void predict()
    {
        ArrayList<Double> arrProbs = new ArrayList<Double>();
        for (int i=1; i<testSeq.size(); i++)
        {   //Loop through the input sequence and get the last m_iVMMOrder numbers from it
            List<Integer> context;
            if (i<m_iVMMOrder)
            {   //We are at the beginning of the sequence, so get first i<m_iVMMOrder elements
                context = testSeq.subList(0, i);
            }else
            {
                context = testSeq.subList(i-m_iVMMOrder, i);
            }

            if (m_bPredictMAP)
            {   //Do the MAP prediction: generate that state that has the
                //highest probability according to the PST
                double dblMax = 0;
                int chrToAppend = 0;
                //Calculate the probability of j-th output using the tree
                for (int j=0; j<m_iAlphabetSize; j++)
                {
                    double p = predictor.predict(j, context);
                    if (p > dblMax)
                    {
                        chrToAppend = j;
                        dblMax = p;
                    }
                }
                predictSeq.add(chrToAppend);
            }

            if (m_bPredictRandom)
            {   //Generate predicted values randomly according to the
                //distribution given by the PST
                arrProbs.clear();
                for (int j=0; j<m_iAlphabetSize; j++)
                {
                    double p = predictor.predict(j, context);
                    arrProbs.add(p);
                }
                multinom.setProbabilities(arrProbs);
                int iNewNum = multinom.generate();
                predictSeq.add(iNewNum);
            }
        }
    }


    /*
     * Entry point for the program. This method reads the input data, initializes
     * the vMM, runs the vMM for the given dataset and stores the results into the
     * table. Finally, it prints out the accuracy on the string.
     */
    public void run(String[] args)
    {
        String m_strFilesTrain;
        String m_strFileTest;
        String m_strFileOut;

        CmdLineParser parser = new CmdLineParser();
        //Path to the input log file with the training dataset. This one will
        //be used to train the vMM
        CmdLineParser.Option filesTrainFlag = parser.addStringOption("fileTrain");
        //Path to the input log file with test set. This one will be used to assess
        //the accuracy of prediction
        CmdLineParser.Option fileTestFlag = parser.addStringOption("fileTest");
        //Output file to store the prediction results
        CmdLineParser.Option fileOutFlag = parser.addStringOption("fileOut");

        //Parse command line arguments.
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

        m_strFilesTrain = (String)parser.getOptionValue(filesTrainFlag);
        if (m_strFilesTrain == null)
        {
            System.err.println("Path to the training set file must be specified");
            System.exit(1);
        }

        m_strFileTest = (String)parser.getOptionValue(fileTestFlag);
        if (m_strFileTest == null)
        {
            System.err.println("Path to the test set file must be specified");
            System.exit(1);
        }

        m_strFileOut = (String)parser.getOptionValue(fileOutFlag);
        if (m_strFileOut == null)
        {
            System.err.println("Path to the output file must be specified");
            System.exit(1);
        }

        //Read the training set, which can include multiple files.
        //Use the LogReader helper class to read and parse queries.
        LogReader logReader = new LogReader();
        String arrFiles[] = m_strFilesTrain.split(",");
        for(String strFile:arrFiles)
        {
            try
            {
                    logReader.ReadFromFile(strFile);
            } catch (Exception e)
            {
                System.err.println("Error reading input query file "+strFile+":");
                System.err.println(e.getMessage());
                for(StackTraceElement element:e.getStackTrace())
                    System.err.println(element.toString());
                System.exit(1);
            }
        }
        ArrayList<Query> arrQueries = logReader.getQueries();
        trainSeq.clear();
        for (Query query:arrQueries)
        {
            trainSeq.add(query.getType());
        }

        //Read the test set
        logReader = new LogReader();
        try
        {
                logReader.ReadFromFile(m_strFileTest);
        } catch (Exception e)
        {
            System.err.println("Error reading input query file "+m_strFileTest+":");
            System.err.println(e.getMessage());
            for(StackTraceElement element:e.getStackTrace())
                System.err.println(element.toString());
            System.exit(1);
        }
        arrQueries = logReader.getQueries();
        testSeq.clear();
        for (Query query:arrQueries)
        {
            testSeq.add(query.getType());
        }

        m_bPredictMAP = true;
        m_bPredictRandom = false;

        //Initialize the vMM
        initialize();
        //Train it using the training data
        train();
        //Do prediction using the trained vMM
        predict();

        if (testSeq.size() != predictSeq.size()+1)
        {
            System.err.println("Size of the training and predicted sequences are different: "+testSeq.size()+" vs. "+predictSeq.size());
            System.exit(1);
        }
        //Dump the predicted data to the disk in a table format
        m_arrResult.add("QueryNo,ActualQueryID,PredictedQueryID,IsCorrect\n");
        int iCorrect = 0;
        for (int i=0; i<predictSeq.size(); i++)
        {
            String strResult = i+","+testSeq.get(i+1)+","+predictSeq.get(i);
            if (testSeq.get(i+1) == predictSeq.get(i))
            {
                iCorrect++;
                strResult+=",OK\n";
            }else
            {
                strResult+=",FAIL\n";
            }
            m_arrResult.add(strResult);
        }
        FileUtil.WriteToFile(m_strFileOut, m_arrResult);
        double dblAccuracy = (double)iCorrect/predictSeq.size();
        System.out.println("Accuracy: "+dblAccuracy);
    }


    /**
     * Entry point for the program.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        MCCheck vmmPredictor = new MCCheck();
        vmmPredictor.run(args);
/*
        if (args.length < 3)
        {
            System.out.println("Input arguments: <training sequence> <test sequence> <predicted sequence> [MAP|prob] [verify]");
            System.out.println("<training sequence>: name of the .csv file with the training sequence");
            System.out.println("<test sequence>: name of the .csv file with the testing sequence");
            System.out.println("<predicted sequence>: name of the .csv file where predicted sequence will be saved");
            System.out.println("MAP: MAP will be used for prediction. Namely, the resulting state will be one with the highest probability predicted by the Markov model (default)");
            System.out.println("prob: the resulting state will be generated probabilistically, taking into account all the probabilities predicted by the Markov model (used for verification)");
            System.out.println("verify: if specified, the program will print out transition matrices for 1-order MM learned both from training and predicted data, for comparison");
            System.exit(-1);
        }
        //Create and initialize the Markov network
        MCCheck vmmPredictor = new MCCheck();
        vmmPredictor.initialize();

        System.out.println("Reading the training set");
        FileUtil.ReadFromFile(args[0], vmmPredictor.trainSeq);
        System.out.println("Reading the test set");
        FileUtil.ReadFromFile(args[1], vmmPredictor.testSeq);

        System.out.println("Training the model");
        vmmPredictor.train();

        vmmPredictor.m_bPredictMAP = true;
        if ((args.length >= 4)&&(args[3].equals("prob")))
        {
            vmmPredictor.m_bPredictMAP = false;
            vmmPredictor.m_bPredictRandom = true;
        }
        System.out.println("Doing the prediction");
        vmmPredictor.predict();
        FileUtil.WriteToFile(args[2], vmmPredictor.predictSeq);

        if ((args.length == 5)&&(args[4].equals("verify")))
        {   //Do verification
            vmmPredictor.transmat_train = new double[vmmPredictor.m_iAlphabetSize][vmmPredictor.m_iAlphabetSize];
            vmmPredictor.transmat_verify = new double[vmmPredictor.m_iAlphabetSize][vmmPredictor.m_iAlphabetSize];

            System.out.println("Transition matrix calculated from training sequence");
            vmmPredictor.build1MM(vmmPredictor.transmat_train, true);
            System.out.println();
            System.out.println("Transition matrix, calculated from our prediction");
            vmmPredictor.build1MM(vmmPredictor.transmat_verify, false);

            System.out.println();
            System.out.println("Comparing matrices");
            double maxDt = 0;
            double avgDt = 0;
            
            for (int i=0; i<vmmPredictor.m_iAlphabetSize; i++)
            {
                for (int j=0; j<vmmPredictor.m_iAlphabetSize; j++)
                {
                    double dt = Math.abs(vmmPredictor.transmat_train[i][j] - vmmPredictor.transmat_verify[i][j]);
                    if (dt > maxDt)
                        maxDt = dt;
                    avgDt += dt;
                }
            }

            System.out.format("Maximum error: %f, average error:%f\n", maxDt, avgDt/(vmmPredictor.m_iAlphabetSize*vmmPredictor.m_iAlphabetSize));
        }
*/
    }
}
