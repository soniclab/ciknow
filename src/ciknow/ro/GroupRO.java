package ciknow.ro;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ciknow.dao.GroupDao;
import ciknow.dao.NodeDao;
import ciknow.dao.QuestionDao;
import ciknow.dao.VisualizationDao;
import ciknow.domain.Group;
import ciknow.domain.Node;
import ciknow.domain.Question;
import ciknow.domain.Visualization;
import ciknow.dto.GroupDTO;

public class GroupRO {
	private static Log logger = LogFactory.getLog(GroupRO.class);
	GroupDao groupDao;
	QuestionDao questionDao;
	NodeDao nodeDao;
	VisualizationDao visDao;
	
	public GroupRO(){

	}

    public GroupDao getGroupDao() {
        return groupDao;
    }

    public void setGroupDao(GroupDao groupDao) {
        this.groupDao = groupDao;
    }

    public QuestionDao getQuestionDao() {
		return questionDao;
	}

	public void setQuestionDao(QuestionDao questionDao) {
		this.questionDao = questionDao;
	}

	public NodeDao getNodeDao() {
		return nodeDao;
	}

	public void setNodeDao(NodeDao nodeDao) {
		this.nodeDao = nodeDao;
	}

	public VisualizationDao getVisDao() {
		return visDao;
	}

	public void setVisDao(VisualizationDao visDao) {
		this.visDao = visDao;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List getPlainNodesByGroupId(String groupId){
		List plainNodes = new ArrayList();
		
		List<Long> nodeIds = groupDao.getNodeIdsByGroupId(Long.parseLong(groupId));
		if (nodeIds == null || nodeIds.isEmpty()) return plainNodes;
		
		List<String> attributes = new ArrayList<String>();
		attributes.add("label");
		Map<String, Map> nodeMap = nodeDao.getPlainNodesByIds(attributes, nodeIds);
		plainNodes.addAll(nodeMap.values());
		
		return plainNodes;
	}
	
	public GroupDTO createGroup(GroupDTO dto){
		logger.debug("createGroup: " + dto.name);
        Group group = new Group();
        group.setName(dto.name);
        groupDao.save(group);
        logger.debug("createGroup, done");
        return new GroupDTO(group);
    }

    public GroupDTO updateGroup(GroupDTO dto){
    	logger.info("update group: " + dto.name);
        Group group = groupDao.findById(Long.parseLong(dto.groupId.toString()));
        group.setVersion(dto.version);
        group.setName(dto.name);  
        groupDao.save(group);        
        logger.info("update group: " + dto.name + ", done");
        return new GroupDTO(group);
    }

    public Long deleteGroupById(Long id){
    	logger.info("deleting group (id=" + id + ")");
        Group group = groupDao.loadById(id);
        if (group != null)  {        	
        	logger.debug("remove group from questions.");
        	List<Question> questions = questionDao.getAll();
        	for (Question question : questions){
        		question.getVisibleGroups().remove(group);
        		question.getAvailableGroups().remove(group);
        		question.getAvailableGroups2().remove(group);
        	}
        	questionDao.save(questions);
        	
        	logger.debug("remove group from nodes.");
        	List<Long> nodeIds = new LinkedList<Long>();
        	for (Node node : group.getNodes()){
        		nodeIds.add(node.getId());
        	}
        	List<Node> nodes = nodeDao.loadByIds(nodeIds);
        	for (Node node : nodes){
        		node.getGroups().remove(group);
        	}
        	nodeDao.save(nodes);
        	
        	logger.debug("remove group from visualizations.");
        	List<Visualization> viss = visDao.getAll();
        	for (Visualization vis : viss){
        		vis.getGroups().remove(group);
        	}
        	visDao.save(viss);
        	
        	groupDao.delete(group);
        }
        logger.info("deleting group (id=" + id + "), done");
        return id;
    }

    public GroupDTO getGroupById(Long id){
        Group group = groupDao.findById(id);
        if (group == null) return null;
        return new GroupDTO(group);
    }

    public List<GroupDTO> getAllGroups(){
		logger.debug("getAllGroups...");
		List<GroupDTO> dtos = new ArrayList<GroupDTO>();
		List<Group> groups = groupDao.getAll();
		for (Group g : groups){
			dtos.add(new GroupDTO(g));
		}
		logger.debug("getAllGroups...done");
		return dtos;
	}
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public Map addNodeToGroup(Map input) throws Exception{
    	logger.info("adding nodes to group...");
    	Long groupId = Long.parseLong((String)input.get("groupId"));
    	Group group = groupDao.findById(groupId);
    	
    	List<Node> nodes = new LinkedList<Node>();
    	List<String> nodeIds = (List<String>) input.get("nodeIds");
    	for (String nodeId : nodeIds){
    		Node node = nodeDao.loadById(Long.parseLong(nodeId));
    		if (node == null) {
    			logger.warn("Node(id=" + nodeId + ") does not exist.");
    			continue;
    		}
    		node.getGroups().add(group);
    		nodes.add(node);
    	}
    	nodeDao.save(nodes);
    	
    	return input;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public Map removeNodeFromGroup(Map input) throws Exception{
    	logger.info("removing nodes from group.");
    	Long groupId = Long.parseLong((String)input.get("groupId"));
    	Group group = groupDao.findById(groupId);
    	
    	List<Node> nodes = new LinkedList<Node>();
    	List<String> nodeIds = (List<String>) input.get("nodeIds");
    	for (String nodeId : nodeIds){
    		Node node = nodeDao.loadById(Long.parseLong(nodeId));
    		if (node == null) {    			
    			logger.warn("Node(id=" + nodeId + ") does not exist.");
    			continue;
    		}
    		node.getGroups().remove(group);
    		nodes.add(node);
    	}
    	nodeDao.save(nodes);
    	
    	return input;
    }
    
}
