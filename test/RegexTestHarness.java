import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class RegexTestHarness {

    public static void main(String[] args){
    	String pattenString = "[^/\\\\*\"<>:|?`, ]+";
    	String testString = "\\adgsa";
    	
        Pattern pattern = 
        Pattern.compile(pattenString);

        Matcher matcher = 
        pattern.matcher(testString);

        if (matcher.matches()) {
            System.out.printf("I found the text" +
                " \"%s\" starting at " +
                "index %d and ending at index %d.%n",
                matcher.group(),
                matcher.start(),
                matcher.end());
        }
    }
}