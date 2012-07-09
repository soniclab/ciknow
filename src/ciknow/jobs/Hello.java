package ciknow.jobs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Hello{
	private static Log logger = LogFactory.getLog(Hello.class);
	
	private int count = 0;
	private String msg = "Hello C-IKNOW!";

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	public void sayHello(){
		logger.info(++count + " : " + msg);
	}
}
