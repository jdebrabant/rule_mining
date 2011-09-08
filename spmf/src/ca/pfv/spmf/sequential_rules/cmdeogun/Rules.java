package ca.pfv.spmf.sequential_rules.cmdeogun;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a set of sequential rules.
 * 
 * @author Philippe Fournier-Viger, 2009
 */
public class Rules {
	public final List<Rule> rules = new ArrayList<Rule>();  // rules
	
	private final String name;
	
	public Rules(String name){
		this.name = name;
	}
	
	public void printRules(int objectsCount){
		System.out.println(" ------- " + name + " -------");
		int i=0;
		for(Rule rule : rules){
			System.out.print("  rule " + i + ":  ");
			rule.print();
			System.out.print("support: " + rule.getRelativeSupport(objectsCount) +
					" (" + rule.getSupportAbsolu() + "/" + objectsCount + ")  ");
			System.out.print("confidence: " + rule.getConfidence());

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

	public List<Rule> getRules() {
		return rules;
	}
}
