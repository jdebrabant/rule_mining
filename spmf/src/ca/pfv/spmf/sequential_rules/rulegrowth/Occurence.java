package ca.pfv.spmf.sequential_rules.rulegrowth;


public class Occurence {
	public short firstItemset;
	public short lastItemset;
	
	public Occurence(short firstItemset, short lastItemset){
		this.firstItemset = firstItemset;
		this.lastItemset = lastItemset;
	}
}
