#!/usr/bin/env python

import sys
import random
import copy

class Markov:

    def readFile(self):
        '''Reads the trainsition probabilities from file and returns the transition matrix
        the format of the file is as follows:
        state_i state_j trainsition_prob_from_i_to_j
        '''
        
        f=open(self.fileName, 'r')
        self.matrix=[]
        self.numStates=0
        
        for line in f:
            self.matrix.append( map(lambda x: float(x), line.strip().split(' ')) )
            self.numStates+=1
        
    
    def cumulativeSum(self):
        '''calculate the cumulative sum of the probabilities in the dictionary for each state'''
        
        self.cSum=copy.deepcopy(self.matrix)
        for i in range(self.numStates):
            for j in range(1, self.numStates):
                self.cSum[i][j]+=self.cSum[i][j-1]
        
        #print self.cSum
                
    
    def getNextTransition(self, curState):
        r=random.random()
        for i in range(len(self.cSum[curState])):
            if r<self.cSum[curState][i]:
                #print curState, r, i
                return i
        
        #in fact it shouldn't come here but it may due to floating point instabilities (probs don't sum to 1)
        return len(self.cSum[curState])-1
        
            
    def getParams(self):
        param=sys.argv
        self.fileName=param[1]        


    def generateMarkovChain(self):
        self.getParams()
        self.readFile()
        self.cumulativeSum()
        
        start=0             #start state
        iter=2            #number of sequences
        length=10000        #length of each sequence
        
        
        li=[]
        for i in range(iter):
            curState=start
            for j in range(length):
                    li.append(curState)
                    curState=self.getNextTransition(curState)
            print ','.join( map(lambda x: str(x), li) )
            li=[]
        

if __name__=='__main__':
    m=Markov();
    m.generateMarkovChain()

