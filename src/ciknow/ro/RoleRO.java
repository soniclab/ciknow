package ciknow.ro;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ciknow.dao.NodeDao;
import ciknow.dao.RoleDao;
import ciknow.domain.Group;
import ciknow.domain.Node;
import ciknow.domain.Role;
import ciknow.dto.RoleDTO;

public class RoleRO {
	private static Log logger = LogFactory.getLog(RoleRO.class);
	RoleDao roleDao;
	NodeDao nodeDao;
	
	public RoleRO(){

	}

    public RoleDao getRoleDao() {
        return roleDao;
    }

    public void setRoleDao(RoleDao roleDao) {
        this.roleDao = roleDao;
    }

    public NodeDao getNodeDao() {
		return nodeDao;
	}

	public void setNodeDao(NodeDao nodeDao) {
		this.nodeDao = nodeDao;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List getPlainNodesByRoleId(String roleId){
		List plainNodes = new ArrayList();
		
		List<Long> nodeIds = roleDao.getNodeIdsByRoleId(Long.parseLong(roleId));
		if (nodeIds == null || nodeIds.isEmpty()) return plainNodes;
		
		List<String> attributes = new ArrayList<String>();
		attributes.add("label");
		Map<String, Map> nodeMap = nodeDao.getPlainNodesByIds(attributes, nodeIds);
		plainNodes.addAll(nodeMap.values());
		
		return plainNodes;
	}
	
	public RoleDTO createRole(RoleDTO dto){
    	logger.info("creating role: " + dto.name);
        Role role = new Role();
        role.setName(dto.name);
        roleDao.save(role);
        logger.info("done.");
        return new RoleDTO(role);
    }

    public RoleDTO updateRole(RoleDTO dto){
    	logger.info("updating role: " + dto.name);
        Role role = roleDao.findById(dto.roleId);
        role.setVersion(dto.version);
        role.setName(dto.name);
        roleDao.save(role);
        logger.info("done.");
        return new RoleDTO(role);
    }

    public Long deleteRole(Long id){
    	logger.info("deleting role id=" + id);
        Role role = roleDao.loadById(id);
        if (role != null) {        	
        	logger.debug("remove role from nodes.");
        	List<Long> nodeIds = new LinkedList<Long>();
        	for (Node node : role.getNodes()){
        		nodeIds.add(node.getId());
        	}
        	List<Node> nodes = nodeDao.loadByIds(nodeIds);
        	for (Node node : nodes){
        		node.getRoles().remove(role);
        	}
        	nodeDao.save(nodes);
        	
        	roleDao.delete(role);
        }
        
        logger.info("done.");
        return id;
    }

    public RoleDTO getRoleById(Long id){
        Role role = roleDao.findById(id);
        if (role == null) return null;
        else return new RoleDTO(role);
    }

    public List<RoleDTO> getAllRoles(){
		logger.debug("getAllRoles ...");
		List<RoleDTO> dtos = new ArrayList<RoleDTO>();
		List<Role> roles = roleDao.getAll();
		for (Role r : roles){
			dtos.add(new RoleDTO(r));
		}
		logger.debug("getAllRoles ...done");
		return dtos;
	}
    
    
    public Map addNodeToRole(Map input) throws Exception{
    	logger.info("adding nodes to role...");
    	Long roleId = Long.parseLong((String)input.get("roleId"));
    	Role role = roleDao.findById(roleId);
    	
    	List<Node> nodes = new LinkedList<Node>();
    	List<String> nodeIds = (List<String>) input.get("nodeIds");
    	for (String nodeId : nodeIds){
    		Node node = nodeDao.loadById(Long.parseLong(nodeId));
    		if (node == null) throw new Exception("Node(id=" + nodeId + ") does not exist.");
    		node.getRoles().add(role);
    		nodes.add(node);
    	}
    	nodeDao.save(nodes);
    	
    	return input;
    }
    
    public Map removeNodeFromRole(Map input) throws Exception{
    	logger.info("removing nodes from role.");
    	Long roleId = Long.parseLong((String)input.get("roleId"));
    	Role role = roleDao.findById(roleId);
    	
    	List<Node> nodes = new LinkedList<Node>();
    	List<String> nodeIds = (List<String>) input.get("nodeIds");
    	for (String nodeId : nodeIds){
    		Node node = nodeDao.loadById(Long.parseLong(nodeId));
    		if (node == null) throw new Exception("Node(id=" + nodeId + ") does not exist.");
    		node.getRoles().remove(role);
    		nodes.add(node);
    	}
    	nodeDao.save(nodes);
    	
    	return input;
    }
}
