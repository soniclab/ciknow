package ciknow.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ciknow.dao.GroupDao;
import ciknow.dao.NodeDao;
import ciknow.domain.Group;
import ciknow.domain.Node;

public class NodeGroupReader{
	private static Log logger = LogFactory.getLog(NodeGroupReader.class);
	private NodeDao nodeDao;
	private GroupDao groupDao;
	
	public NodeGroupReader() {
		super();
	}

	public void read(BufferedReader reader) throws Exception {
		logger.info("importing groups...");
		StringBuilder sb = new StringBuilder();
		
    	logger.debug("get all existing users.");
        List<Node> existingNodes =  nodeDao.loadAll();
        Map<String, Node> nodeMap = new HashMap<String, Node>();
        for (Node n : existingNodes){
            nodeMap.put(n.getUsername().trim(), n);
        }

        logger.debug("get all existing groups.");
        List<Group> existingGroups = groupDao.getAll();
        Map<String, Group> groupMap = new HashMap<String, Group>();
        for (Group group : existingGroups){
        	groupMap.put(group.getName().trim(), group);
        }
        
        logger.debug("reading each row (each node)");
        String line = reader.readLine();
        while (line != null){
            logger.debug(line);
            String[] texts = line.split("\t", -1);
            
            // get node
            String username = texts[0].trim();            
            Node node = nodeMap.get(username);
            if (node == null){
            	sb.append(">> node ").append(username).append(" doesn't exist.\n");
            	line = reader.readLine();
            	continue;
            }
            
            // add each group to node if appropriate
            for (int i=1; i<texts.length; i++){
            	String name = texts[i].trim();
            	
                if (name.length() == 0) {
                	sb.append(">> groupName cannot be empty: " + line + "\n");
                	continue;
                }
                
                if (name.length() > 250) {
                	sb.append(">> groupName is too long (> 250): " + name + "\n");
                	continue;
                }
                
                if (name.contains(" ")
                		|| name.contains(",")
                		|| name.contains("`")
                		|| name.contains("/")
                		|| name.contains("\\")
                		|| name.contains("*")
                		|| name.contains("\"")
                		|| name.contains(">")
                		|| name.contains("<")
                		|| name.contains(":")
                		|| name.contains("|")
                		|| name.contains("?")){
                	sb.append(">> groupName cannot contains special characters or spaces: " + name + "\n");
                	continue;
                }
            	
            	Group g = groupMap.get(name);
            	if (g == null){
            		g = new Group();
            		g.setName(name);
            		groupDao.save(g);
            		groupMap.put(name, g);
            	}
            	

            	node.getGroups().add(g);
            }
            
            // next
            line = reader.readLine();
        }

        if (sb.length() > 0) throw new Exception(sb.toString());
        
        // persist the users
        nodeDao.save(existingNodes);

		logger.info("done.");
	}
	


	public NodeDao getNodeDao() {
		return nodeDao;
	}

	public void setNodeDao(NodeDao nodeDao) {
		this.nodeDao = nodeDao;
	}

	public GroupDao getGroupDao() {
		return groupDao;
	}

	public void setGroupDao(GroupDao groupDao) {
		this.groupDao = groupDao;
	}
}
