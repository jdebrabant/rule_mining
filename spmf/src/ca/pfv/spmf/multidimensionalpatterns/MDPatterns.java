package ca.pfv.spmf.multidimensionalpatterns;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of a set of MD-Patterns, sorted by size.
 * Level 0 = MDPatterns of size 0 
 * and so on...
 * 
 * @author Philippe Fournier-Viger 
 **/
public class MDPatterns {

	private final List<List<MDPattern>> levels = new ArrayList<List<MDPattern>>();  // sorted by size
	private int sequencesCount=0;
	
	private final String name;
	
	public MDPatterns(String name){
		this.name = name;
		levels.add(new ArrayList<MDPattern>()); // level 0 is empty.
	}
	
	public void printFrequentPatterns(int objectCount){
		StringBuffer r = new StringBuffer(150);
		r.append(" ------- ");
		r.append(name);
		r.append(" -------\n");
		int levelCount=0;
		for(List<MDPattern> level : levels){
			r.append("  L");
			r.append(levelCount);
			r.append(" \n");
			for(MDPattern pattern : level){
				StringBuffer s = new StringBuffer(100);
				s.append("  pattern ");
				s.append(pattern.getId());
				s.append(":  ");
				s.append(pattern.toString());
				s.append("support :  ");
				s.append(pattern.getRelativeSupportFormatted(objectCount));
				s.append(" (");
				s.append(pattern.getAbsoluteSupport());
				s.append("/");
				s.append(objectCount);
				s.append(") ");
				s.append("\n");
				r.append(s);
			}
			levelCount++;
		}
		r.append(" --------------------------------\n");
		System.out.print(r);
	}
	
	public void addPattern(MDPattern pattern, int k){
		while(levels.size() <= k){
			levels.add(new ArrayList<MDPattern>());
		}
		levels.get(k).add(pattern);
		sequencesCount++;
	}
	
	public void removePattern(MDPattern pattern, int k){
		levels.get(k).remove(pattern);
		sequencesCount--;
	}
	
	public List<MDPattern> getLevel(int k){
		return levels.get(k);
	}

	public int size() {
		return sequencesCount;
	}

	public int getLevelCount() {
		return levels.size();
	}

}
