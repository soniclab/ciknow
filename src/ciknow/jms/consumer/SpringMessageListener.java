package ciknow.jms.consumer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jms.support.JmsUtils;

public class SpringMessageListener implements MessageListener{
	private static Log logger = LogFactory.getLog(SpringMessageListener.class);
	
	public void onMessage(Message msg) {
		TextMessage tm = (TextMessage) msg;
		try {
			logger.info("received: " + tm.getText());
		} catch (JMSException e) {
			//e.printStackTrace();
			throw JmsUtils.convertJmsAccessException(e);
		}
	}

}
