package ca.pfv.spmf.multidimensionalpatterns;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Implémentation of an MD-Pattern.
 * Based on the article by Helen Pinto et al. (2001).
 * 
 * @author Philippe Fournier-Viger 
 **/
public class MDPattern {
	
	public List<Integer> values = new ArrayList<Integer>();
	public final static int WILDCARD = 9999;
	private int id; // sequence id
	
	// List of the IDs of all the patterns that contain this one.
	protected Set<Integer> patternsID = null;
	
	public MDPattern(int id){
		this.id = id;
	}

	public int getId() {
		return id;
	}
	
	public int size(){
		return values.size();
	}
	
	public int getValue(int index){
		return values.get(index);
	}
	
	public Integer get(int index){
		return values.get(index);
	}
	
	public void addInteger(int value){
		values.add(value);
	}
	
	public void addWildCard(){
		values.add(WILDCARD);
	}
	
	public void addIntegerFirstPosition(int value){
		values.add(0, value);
	}
	
	public void addWildCardFirstPosition(){
		values.add(0, WILDCARD);
	}
	
	public double getRelativeSupport(int nbObject) {
//		System.out.println("((( " + transactioncount);
		return ((double)patternsID.size()) / ((double) nbObject);
	}
	
	public int getAbsoluteSupport(){
		if(patternsID == null){
			return 0;
		}
		return patternsID.size();
	}
	
	public String getRelativeSupportFormatted(int nbObject) {
		double support = ((double)getAbsoluteSupport()) / ((double) nbObject);
		// pretty formating:
		DecimalFormat format = new DecimalFormat();
		format.setMinimumFractionDigits(0); 
		format.setMaximumFractionDigits(2); 
		return format.format(support);
	}
	
	public void print(){
		System.out.print(toString());
	}
	
	public String toString(){
		StringBuffer r = new StringBuffer("[ ");
		// Print the value for each dimension
		for(int i=0; i< values.size(); i++)
		{
			if(values.get(i).equals(WILDCARD)){
				r.append("* ");
			}else{
				r.append(values.get(i));
				r.append(' ');
			}
		}
		r.append(']');
		
		// Print the list of ids that contains this pattern:
		if(getPatternsID() != null){
			r.append("  Patterns ID: ");
			for(Integer id : getPatternsID()){
				r.append(id);
				r.append(' ');
			}
		}
		r.append("   ");
		return r.toString();
	}

	public Set<Integer> getPatternsID() {
		return patternsID;
	}

	public void setPatternsIDList(Set<Integer> patternsID) {
		this.patternsID = patternsID;
	}
	
	/**
	 * This method is only good for patterns having the same size.
	 * Return 1 if this mdpattern STRICTLY contains mdpattern2  *AND HAVE THE SAME SUPPORT*
	 * Return 2 if this mdpattern is exactly the same as mdpattern2  *AND HAVE THE SAME SUPPORT*
	 * Return 0 if this mdpattern does not contains mdpattern2.
	 * @param sequence2
	 * @return
	 */
	public int strictlyContains(MDPattern mdpattern2) {
		if(getAbsoluteSupport() != mdpattern2.getAbsoluteSupport()){
			return 0;
		}
		boolean allthesame = true;
		for(int i=0; i< values.size(); i++){
			if(values.get(i) != mdpattern2.getValue(i)){
				allthesame = false;
			}
			if(values.get(i) != WILDCARD && values.get(i) != mdpattern2.getValue(i)){
				return 0;
			}
		}
		if(allthesame){
			return 2;
		}
		return 1;
	}

	public void set(int indexDimension, int valeur) {
		values.remove(indexDimension);
		values.add(indexDimension, valeur);
	}

	public void setID(int id2) {
		id =id2;
	}

}
