package ciknow.io;

import java.io.BufferedReader;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ciknow.dao.NodeDao;
import ciknow.dao.RoleDao;
import ciknow.domain.Role;
import ciknow.domain.Node;

public class NodeRoleReader{
	private static Log logger = LogFactory.getLog(NodeRoleReader.class);
	private NodeDao nodeDao;
	private RoleDao roleDao;
	
	public NodeRoleReader() {
		super();
	}

	public void read(BufferedReader reader) throws Exception {
		logger.info("importing roles...");
		StringBuilder sb = new StringBuilder();
		
    	logger.debug("get all existing users.");
        List<Node> existingNodes =  nodeDao.loadAll();
        Map<String, Node> nodeMap = new HashMap<String, Node>();
        for (Node n : existingNodes){
            nodeMap.put(n.getUsername().trim(), n);
        }

        logger.debug("get all existing roles.");
        List<Role> existingRoles = roleDao.getAll();
        Map<String, Role> roleMap = new HashMap<String, Role>();
        for (Role role : existingRoles){
        	roleMap.put(role.getName().trim(), role);
        }
        
        logger.debug("reading each row (each node)");
        String line = reader.readLine();
        while (line != null){
        	line = line.trim();
        	if (line.isEmpty()) {
        		line = reader.readLine();
        		continue;
        	}
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
            
            // add each role to node if appropriate
            for (int i=1; i<texts.length; i++){
            	String name = texts[i].trim();
            	
                if (name.length() == 0) {
                	sb.append(">> roleName cannot be empty: " + line + "\n");
                	continue;
                }
                
                if (name.length() > 250) {
                	sb.append(">> roleName is too long (> 250): " + name + "\n");
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
                	sb.append(">> roleName cannot contains special characters or spaces: " + name + "\n");
                	continue;
                }
                
            	Role g = roleMap.get(name);
            	if (g == null){
            		g = new Role();
            		g.setName(name);
            		roleDao.save(g);
            		roleMap.put(name, g);
            	}
            	
            	node.getRoles().add(g);
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

	public RoleDao getRoleDao() {
		return roleDao;
	}

	public void setRoleDao(RoleDao roleDao) {
		this.roleDao = roleDao;
	}
}
