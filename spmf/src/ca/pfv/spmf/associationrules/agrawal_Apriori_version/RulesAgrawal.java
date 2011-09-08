package ca.pfv.spmf.associationrules.agrawal_Apriori_version;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a set of association rules that has
 * been found by the IGB algorithm.
 * 
 * @author Philippe Fournier-Viger, 2008
 */
public class RulesAgrawal {
	public final List<RuleAgrawal> rules = new ArrayList<RuleAgrawal>();  // rules
	
	private final String name;
	
	public RulesAgrawal(String name){
		this.name = name;
	}
	
	public void printRules(int objectsCount){
		System.out.println(" ------- " + name + " -------");
		int i=0;
		for(RuleAgrawal rule : rules){
			System.out.print("  rule " + i + ":  " + rule.toString());
			System.out.print("support :  " + rule.getRelativeSupport(objectsCount) +
					" (" + rule.getSupportAbsolu() + "/" + objectsCount + ") ");
			System.out.print("confidence :  " + rule.getConfidence());
			System.out.println("");
			i++;
		}
		System.out.println(" --------------------------------");
	}
	
	public String toString(int nbObject){
		StringBuffer buffer = new StringBuffer(" ------- ");
		buffer.append(name);
		buffer.append(" -------\n");
		int i=0;
		for(RuleAgrawal rule : rules){
//			System.out.println("  L" + j + " ");
			buffer.append("   rule ");
			buffer.append(i);
			buffer.append(":  ");
			buffer.append(rule.toString());
			buffer.append("support :  ");
			buffer.append(rule.getRelativeSupport(nbObject));

			buffer.append(" (");
			buffer.append(rule.getSupportAbsolu());
			buffer.append("/");
			buffer.append(nbObject);
			buffer.append(") ");
			buffer.append("confidence :  " );
			buffer.append(rule.getConfidence());
			buffer.append("\n");
			i++;
		}
		return buffer.toString();
	}
	
	public void addRule(RuleAgrawal regle){
		rules.add(regle);
	}
	
	public int getRulesCount(){
		return rules.size();
	}

	public List<RuleAgrawal> getRules() {
		return rules;
	}
	
	
}
