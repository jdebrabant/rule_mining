
import java.util.*; 

public class AssociationRule
{
	public LinkedList<Integer> lhs; 
	public LinkedList<Integer> rhs; 
	public double support; 
	public boolean removed; 
	
	public AssociationRule(LinkedList<Integer> l, LinkedList<Integer> r, double supp)
	{
		lhs = l; 
		rhs = r; 
		support = supp; 
		removed = false; 
	}
	
	public String ruleToString()
	{
		String rule = ""; 
		
		for(int i = 0; i < lhs.size(); i++)
		{
			rule += lhs.get(i).intValue() + " "; 
		}
		
		rule += " ==> "; 
		
		for(int i = 0; i < rhs.size(); i++)
		{
			rule += rhs.get(i).intValue() + " "; 
		}
		
		rule += ", " + support + "\n";
		
		return rule; 
	}
}