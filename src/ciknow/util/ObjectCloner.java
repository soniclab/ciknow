package ciknow.util;

import java.io.*;

import org.apache.log4j.Logger;


/**
 * Simple utility to allow for deep copying of objects.
 *
 */
public class ObjectCloner {
	private static Logger logger = Logger.getLogger("ncsa.sonic.ciknow.util.ObjectCloner");
	
	/**
	 * Deep copy the object supplied.
	 */
	static public Object deepCopy(Object oldObj) throws Exception {
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bos);
			
			oos.writeObject(oldObj);
			oos.flush();
			
			ByteArrayInputStream bin = new ByteArrayInputStream(bos
					.toByteArray());
			ois = new ObjectInputStream(bin);
			
			return ois.readObject();
		} catch (Exception e) {
			logger.error(e.toString());
			throw (e);
		} finally {
			oos.close();
			ois.close();
		}
	}

}