package ciknow.util;

import java.awt.Color;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Various utilities related to visualizations.
 *
 */
public class VisUtil {
	private static Log logger = LogFactory.getLog(VisUtil.class);
	
	//public static final String NODE_LABEL_ATTRIBUTE_KEY = "label";
	//public static final String NODE_TYPE_ATTRIBUTE_KEY = "type";
	public static final String DEFAULT_NODE_ID = "nodeId";
	public static final String DEFAULT_NODE_NAME = "Unnamed Node";
	public static final String DEFAULT_SCALE_VALUE = "1.0";
	
	public static final boolean DEFAULT_NODE_BOLD = false;
	public static final boolean DEFAULT_NODE_ITALIC = false;
	public static final boolean DEFAULT_NODE_UNDERLINE = false;
	public static final boolean DEFAULT_NODE_HIDDEN = false;
	public static final boolean DEFAULT_EDGE_HIDDEN = false;
	
	//public static final String DEFAULT_LEGEND_LABEL = "Other";
	//public static final String DEFAULT_COLOR_KEY = "defaultColorKey";
	//public static final String DEFAULT_GROUP_KEY = "defaultGroupKey";
	
	public static final String MISSING_VALUE = "MISSING VALUE";
	public static final Color MISSING_VALUE_COLOR = Color.LIGHT_GRAY;
	public static final String NOT_APPLICABLE = "NOT APPLICABLE";
	public static final Color NOT_APPLICABLE_COLOR = Color.WHITE;
	
	public static final String LOGIN_LABEL = "Requester";
	public static final String RECOMMENDATION_LABEL = "Recommendation";
	public static final String TARGET_LABEL = "Target";
	
	public static final Color DEFAULT_NODE_COLOR = Color.black;
	public static final Color DEFAULT_GROUP_COLOR = Color.cyan;
	public static final Color DEFAULT_EDGE_COLOR = Color.black;
	public static final Color LOGIN_COLOR = new Color(Integer.parseInt("E77471", 16));
	public static final Color RECOMMENDATION_COLOR = Color.green;
	public static final Color TARGET_COLOR = Color.yellow;
	public static final Color FOCAL_COLOR = Color.red;
	
	public static final String ATTR_PREFIX = "ATTR:";
	public static final String QUESTION_PREFIX = "QUESTION:";
	
//	public static Color[] COLORS = { VisUtil.HEX2COLOR("00FFFF"),
//		VisUtil.HEX2COLOR("a4d3ee"), VisUtil.HEX2COLOR("ffff66"),
//		VisUtil.HEX2COLOR("DAC4E5"), VisUtil.HEX2COLOR("B272A6"),
//		VisUtil.HEX2COLOR("8FBC8F"), VisUtil.HEX2COLOR("EE9A49"),
//		VisUtil.HEX2COLOR("FFBBFF"), VisUtil.HEX2COLOR("BEBEBE"),
//		VisUtil.HEX2COLOR("5F9EA0"), VisUtil.HEX2COLOR("0000FF"),
//		VisUtil.HEX2COLOR("C71585"), VisUtil.HEX2COLOR("FFFF00"),
//		VisUtil.HEX2COLOR("ADFF2F"), VisUtil.HEX2COLOR("00BFFF"),
//		VisUtil.HEX2COLOR("9932CC"), VisUtil.HEX2COLOR("98FB98"),
//		VisUtil.HEX2COLOR("FFE4E1"), VisUtil.HEX2COLOR("9370D8"),
//		VisUtil.HEX2COLOR("B0C4DE"), VisUtil.HEX2COLOR("C1FFC1"),
//		VisUtil.HEX2COLOR("FAFAD2"), VisUtil.HEX2COLOR("BA55D3"),
//		VisUtil.HEX2COLOR("7B68EE"), VisUtil.HEX2COLOR("6B8E23"),
//		VisUtil.HEX2COLOR("483D8B"), VisUtil.HEX2COLOR("00FA9A"),
//		VisUtil.HEX2COLOR("90EE90"), VisUtil.HEX2COLOR("FFD700"),
//		VisUtil.HEX2COLOR("87CEFA"), VisUtil.HEX2COLOR("FFDAB9"),
//		VisUtil.HEX2COLOR("DDA0DD"), VisUtil.HEX2COLOR("D8BFD8"),
//		VisUtil.HEX2COLOR("F5F5F5"), VisUtil.HEX2COLOR("4682B4"),
//		VisUtil.HEX2COLOR("20B2AA"), VisUtil.HEX2COLOR("FFA07A"),
//		VisUtil.HEX2COLOR("E0FFFF"), VisUtil.HEX2COLOR("E6E6FA"),
//		VisUtil.HEX2COLOR("4B0082"), VisUtil.HEX2COLOR("E9967A"),
//		VisUtil.HEX2COLOR("556B2F"), VisUtil.HEX2COLOR("BDB76B"),
//		VisUtil.HEX2COLOR("00008B"), VisUtil.HEX2COLOR("5F9EA0"),
//		VisUtil.HEX2COLOR("DEB887"), VisUtil.HEX2COLOR("8A2BE2"),
//		VisUtil.HEX2COLOR("E6E6FA"), VisUtil.HEX2COLOR("000080"),
//		VisUtil.HEX2COLOR("FFA500"), VisUtil.HEX2COLOR("808000"),
//		VisUtil.HEX2COLOR("D87093"), VisUtil.HEX2COLOR("800080"),
//		VisUtil.HEX2COLOR("A0522D"), VisUtil.HEX2COLOR("C0C0C0"),
//		VisUtil.HEX2COLOR("87CEEB"), VisUtil.HEX2COLOR("708090"),
//		VisUtil.HEX2COLOR("EE9A49"), VisUtil.HEX2COLOR("008080"),
//		VisUtil.HEX2COLOR("FF6347"), VisUtil.HEX2COLOR("40E0D0"),
//		VisUtil.HEX2COLOR("EE82EE"), VisUtil.HEX2COLOR("F5DEB3"),
//		VisUtil.HEX2COLOR("9ACD32"), VisUtil.HEX2COLOR("00FFFF"),
//		VisUtil.HEX2COLOR("a4d3ee"), VisUtil.HEX2COLOR("ffff66"),
//		VisUtil.HEX2COLOR("DAC4E5"), VisUtil.HEX2COLOR("B272A6"),
//		VisUtil.HEX2COLOR("8FBC8F"), VisUtil.HEX2COLOR("EE9A49"),
//		VisUtil.HEX2COLOR("C1FFC1"), VisUtil.HEX2COLOR("FFBBFF"),
//		VisUtil.HEX2COLOR("BEBEBE"), VisUtil.HEX2COLOR("5F9EA0"),
//		VisUtil.HEX2COLOR("0000FF"), VisUtil.HEX2COLOR("C71585"),
//		VisUtil.HEX2COLOR("FFFF00"), VisUtil.HEX2COLOR("ADFF2F"),
//		VisUtil.HEX2COLOR("00BFFF"), VisUtil.HEX2COLOR("9932CC") };
	
	public static String COLOR2HEX(Color c){
		String hexNumber = Integer.toHexString( c.getRGB() & 0x00ffffff );
		hexNumber = hexNumber.toUpperCase();
		
		StringBuffer ret = new StringBuffer();
		ret.append("0x");
		for (int i = 0; i < 6 - hexNumber.length(); i++){
			ret.append("0");
		}
		ret.append(hexNumber);
		return ret.toString();
	}	
	
	public static void main(String[] args){
		String s = "0xff0000";
		logger.info("initial string: " + s);
		
		Color color = Color.decode(s);
		logger.info(color);
		logger.info(Integer.toHexString((color.getRGB())));
	}
}
