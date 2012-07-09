
import java.awt.Font;

import ciknow.util.GeneralUtil;


public class StringMeasureTest {
	public static void main(String[] args){
		String input = "WWWW WWWW WWWW";  
		Font f = new Font("Arial", Font.PLAIN, 12);
		
		GeneralUtil.measureString(input, f, null);
	}
}
