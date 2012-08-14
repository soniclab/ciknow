package ciknow.io;

import java.io.BufferedReader;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ciknow.dao.GroupDao;
import ciknow.domain.Group;

public class GroupAttributeReader{
	private static Log logger = LogFactory.getLog(GroupAttributeReader.class);
	private GroupDao groupDao;
	
	public GroupAttributeReader() {
		super();
	}

	public void read(BufferedReader reader) throws Exception {
		logger.info("importing group attributes...");

    	logger.debug("get all existing groups.");
        List<Group> existingGroups =  groupDao.loadAll();
        Map<String, Group> groupMap = new HashMap<String, Group>();
        for (Group group : existingGroups) {
            groupMap.put(group.getName().trim(), group);
        }
        List<Group> updatedGroups = new LinkedList<Group>();
        
        logger.debug("reading each row (each group)");
        String line = reader.readLine();
        String[] attrNames = line.split("\t", -1);
        line = reader.readLine();
        while (line != null){
            String[] texts = line.split("\t", -1);
            
            // get group
            String groupname = texts[0].trim();            
            Group group = groupMap.get(groupname);
            if (group == null){
            	throw new Exception("group " + groupname + " doesn't exist.");
//            	logger.warn("group " + groupname + " doesn't exist.");
//            	line = reader.readLine();
//            	continue;
            }
            
            for (int i=1; i<texts.length; i++){
            	String value = texts[i].trim();
            	if (value.length() == 0) continue;
            	group.getAttributes().put(attrNames[i].trim(), value);            	
            }
            updatedGroups.add(group);
            
            // next
            line = reader.readLine();
        }

        // persist the users
        groupDao.save(updatedGroups);

		logger.info(updatedGroups.size() + " groups updated.");
	}
	


	public GroupDao getGroupDao() {
		return groupDao;
	}

	public void setGroupDao(GroupDao groupDao) {
		this.groupDao = groupDao;
	}
}
