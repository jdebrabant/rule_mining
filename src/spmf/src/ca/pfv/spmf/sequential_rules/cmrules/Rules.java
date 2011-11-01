package ca.pfv.spmf.sequential_rules.cmrules;
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
//			System.out.print("supp: " + rule.getRelativeSupport(objectsCount) +
//					" (" + rule.getAbsoluteSupport() + "/" + objectsCount + ")  ");
//			System.out.print("conf: " + rule.getConfidence());
			System.out.print(" seqSupp: " + rule.getSequentialSupport(objectsCount) +
					" (" + rule.getAbsoluteSeqSupport() + "/" + objectsCount + ") ");
			System.out.print(" seqConf: " + rule.getSequentialConfidence() +
					" (" + rule.getAbsoluteSupport() + "/" + rule.getItemset1().getAbsoluteSupport() + ") ");
			System.out.println("");
			i++;
		}
		System.out.println(" --------------------------------");
	}
	
	public String toString(int objectsCount){
		StringBuffer buffer = new StringBuffer(" ------- " + name + " -------\n");
		int i=0;
		for(Rule rule : rules){
			buffer.append("  rule ");
			buffer.append(i);
			buffer.append(":  ");
			buffer.append(rule.toString());
//			buffer.append("supp: ");
//			buffer.append(rule.getRelativeSupport(objectsCount));
//			buffer.append(" (");
//			buffer.append(rule.getAbsoluteSupport());
//			buffer.append("/");
//			buffer.append(objectsCount);
//			buffer.append(")   ");
//			buffer.append("conf: ");
//			buffer.append(rule.getConfidence());
			buffer.append("  seqSupp: ");
			buffer.append(rule.getSequentialSupport(objectsCount));
			buffer.append(" (");
			buffer.append(rule.getAbsoluteSeqSupport());
			buffer.append("/");
			buffer.append(objectsCount);
			buffer.append("  seqConf: " );
			buffer.append(rule.getSequentialConfidence());
			buffer.append(" (");
			buffer.append(rule.getAbsoluteSupport());
			buffer.append("/");
			buffer.append(rule.getItemset1().getAbsoluteSupport());
			buffer.append("\n");
			i++;
		}
		buffer.append("--------------------------------\n");
		return buffer.toString();
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
