
public class SplitTest {
	public static void main(String[] args){
		String input = "a|b|c|d";
		String[] parts = input.split("\\|");
		for (String part : parts){
			System.out.println(part);
		}
		
		System.exit(0);
	}
}
