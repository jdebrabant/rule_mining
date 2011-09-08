package ca.pfv.spmf.associationrules.pasquier;
import java.util.ArrayList;
import java.util.List;

/**
 * A set of Association Rules
 * @author Philippe Fournier-Viger
 */
public class Rules {
	private final List<Rule> rules = new ArrayList<Rule>();  // itemset classé par taille
	
	private final String name;
	
	public Rules(String name){
		this.name = name;
	}
	
	public void printRules(int objectsCount){
		System.out.println(" ------- " + name + " -------");
		int i=0;
		for(Rule regle : rules){
			System.out.print("  rule " + i + ":  ");
			regle.print();
			System.out.print("support :  " + regle.getRelativeSupport(objectsCount) +
					" (" + regle.getAbsoluteSupport() + "/" + objectsCount + ") ");
			System.out.print("confidence :  " + regle.getConfidence());
			System.out.println("");
			i++;
		}
		System.out.println(" --------------------------------");
	}
	
	public void addRule(Rule rule){
		rules.add(rule);
	}
	
	public int getRulesCount(){
		return rules.size();
	}
}
