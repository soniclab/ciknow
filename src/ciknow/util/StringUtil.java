package ciknow.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;

public class StringUtil {
	private static Logger logger = Logger.getLogger("ncsa.sonic.ciknow.util.StringUtil");
	
	/**
	 * split the input string by given expression
	 * @param line
	 * @param exp
	 * @return
	 */
	public static List<String> splitAsList(String line, String exp) {
		logger.debug("readline: " + line);
		String[] values = line.split(exp, -1);
		List<String> valueList = new ArrayList<String>();
		for (String value : values){
			//value = value.trim();
/*			if (value.length() > 0) {
				valueList.add(value);
			}*/
			valueList.add(value);
		}
		
		return valueList;
	}
	
	
	public static Comparator<String> CASE_INSENSITIVE_COMPARATOR = new Comparator<String>(){

		public int compare(String arg0, String arg1) {
			if (arg0 == null)
				return 1;
			else if (arg1 == null)
				return -1;
			
			return String.CASE_INSENSITIVE_ORDER.compare(arg0, arg1);
		}
		
	};
	
	//		STRING MATCHING
	
	public interface StringMatchMethod {
		public boolean matches(String s0, String s1);
	}
	
	public static final StringMatchMethod EXACT = new StringMatchMethod () {
		public boolean matches(String s0, String s1){
			if (s0.equals(s1))
				return true;
			else
				return false;
		}
	};
	
	public static final StringMatchMethod CASE_INSENSITIVE = new StringMatchMethod () {
		public boolean matches(String s0, String s1){
			if (s0.equalsIgnoreCase(s1))
				return true;
			else
				return false;
		}
	};
	
	public static final StringMatchMethod CONTAINS_CASE_SENSITIVE = new StringMatchMethod () {
		public boolean matches(String s0, String s1){
			if (s0.contains(s1))
				return true;
			else
				return false;
		}
	};
	
	
	
	public static final StringMatchMethod CONTAINS_CASE_INSENSITIVE = new StringMatchMethod () {
		public boolean matches(String s0, String s1){
			if (s0.toLowerCase().contains(s1.toLowerCase()))
				return true;
			else
				return false;
		}
	};
	
	public static boolean matches(String s0, String s1, StringMatchMethod method){
		return method.matches(s0, s1);
	}
}
