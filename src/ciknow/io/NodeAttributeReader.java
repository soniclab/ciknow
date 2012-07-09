package ciknow.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ciknow.dao.NodeDao;
import ciknow.domain.Node;

public class NodeAttributeReader{
	private static Log logger = LogFactory.getLog(NodeAttributeReader.class);
	private NodeDao nodeDao;
	
	public NodeAttributeReader() {
		super();
	}

	public void read(BufferedReader reader) throws Exception {
		logger.info("importing node attributes...");

    	logger.debug("get all existing users.");
        List<Node> existingNodes =  nodeDao.loadAll();
        Map<String, Node> nodeMap = new HashMap<String, Node>();
        for (Node n : existingNodes){
            nodeMap.put(n.getUsername().trim(), n);
        }
        List<Node> updatedNodes = new LinkedList<Node>();
        
        logger.debug("reading each row (each node)");
        String line = reader.readLine();
        String[] attrNames = line.split("\t", -1);
        line = reader.readLine();
        while (line != null){
            String[] texts = line.split("\t", -1);
            
            // get node
            String username = texts[0].trim();            
            Node node = nodeMap.get(username);
            if (node == null){
            	throw new Exception("node " + username + " doesn't exist.");
//            	logger.warn("node " + username + " doesn't exist.");
//            	line = reader.readLine();
//            	continue;
            }
            
            for (int i=1; i<texts.length; i++){
            	String value = texts[i].trim();
            	if (value.length() == 0) continue;
            	node.setAttribute(attrNames[i].trim(), value);            	
            }
            updatedNodes.add(node);
            
            // next
            line = reader.readLine();
        }

        // persist the users
        nodeDao.save(updatedNodes);

		logger.info(updatedNodes.size() + " nodes updated.");
	}
	


	public NodeDao getNodeDao() {
		return nodeDao;
	}

	public void setNodeDao(NodeDao nodeDao) {
		this.nodeDao = nodeDao;
	}
}
