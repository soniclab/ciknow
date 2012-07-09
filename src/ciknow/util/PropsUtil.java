package ciknow.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

public class PropsUtil {
	private Properties props;
	
	public PropsUtil(String propsFileName){
		props = new Properties();
		try {
			InputStream is = Thread.currentThread().getContextClassLoader()
									.getResourceAsStream(propsFileName + ".properties");
			props.load(is);
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String get(String key){
		return props.getProperty(key);
	}
	
	public int getInt(String key){
		return Integer.parseInt(get(key));
	}
	
	public float getFloat(String key){
		return Float.parseFloat(get(key));
	}	
	
	public double getDouble(String key){
		return Double.parseDouble(get(key));
	}	
	
	public List<String> getStringList(String key, String seperator){
		String raw = props.getProperty(key);
		
		return StringUtil.splitAsList(raw, seperator);
	}
}
