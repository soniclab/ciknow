package ciknow.ro;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import ciknow.dao.ActivityDao;
import ciknow.dao.EdgeDao;
import ciknow.dao.GroupDao;
import ciknow.dao.JobDao;
import ciknow.dao.NodeDao;
import ciknow.dao.PageDao;
import ciknow.dao.QuestionDao;
import ciknow.dao.RoleDao;
import ciknow.dao.SurveyDao;
import ciknow.domain.Activity;
import ciknow.domain.Edge;
import ciknow.domain.Field;
import ciknow.domain.Group;
import ciknow.domain.Job;
import ciknow.domain.Node;
import ciknow.domain.Page;
import ciknow.domain.Question;
import ciknow.domain.Role;
import ciknow.domain.Scale;
import ciknow.domain.Survey;
import ciknow.dto.EdgeDTO;
import ciknow.dto.JobDTO;
import ciknow.dto.NodeDTO;
import ciknow.graph.metrics.IndividualMetric;
import ciknow.graph.metrics.NetworkAnalytics;
import ciknow.graph.metrics.NetworkMetrics;
import ciknow.security.CIKNOWUserDetails;
import ciknow.service.ActivityService;
import ciknow.util.Beans;
import ciknow.util.Constants;
import ciknow.util.GeneralUtil;
import ciknow.util.VisUtil;
import ciknow.vis.NetworkExtractor;
import ciknow.util.RandomString;
import edu.uci.ics.jung.algorithms.shortestpath.Distance;
import edu.uci.ics.jung.algorithms.shortestpath.UnweightedShortestPath;
import edu.uci.ics.jung.algorithms.transformation.DirectionTransformer;
import edu.uci.ics.jung.graph.Graph;
import flex.messaging.FlexContext;
import flex.messaging.FlexSession;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;


/**
 * User: gyao
 * Date: Mar 5, 2008
 * Time: 5:55:39 PM
 */
public class NodeRO {

    private Log logger = LogFactory.getLog(NodeRO.class);
    private NodeDao nodeDao;
	private GroupDao groupDao;
    private RoleDao roleDao;
    private EdgeDao edgeDao;
    private QuestionDao questionDao;
    private SurveyDao surveyDao;
    private JobDao jobDao;
    private EdgeRO edgeRO;
    private ActivityDao activityDao;
    private PageDao pageDao;
    private ActivityService activityService;
    
	public static void main(String[] args) throws Exception{
    	Beans.init();
    	NodeRO nodeRO = (NodeRO)Beans.getBean("nodeRO");
    	
    	Map m = new HashMap();
    	
    	// derive by analytics
    	/*
    	m.put("attrName", "x");
    	m.put("analyticName", "inDegree");
    	m.put("direction", "0");
    	List<String> edgeTypes = new LinkedList<String>();
    	edgeTypes.add("Authorship");
    	m.put("edgeTypes", edgeTypes);
    	nodeRO.deriveAttributeByAnalytics(m);
    	*/
    	
    	// derive by social influence
    	/*
    	m.put("attrName", "x");
    	m.put("varSocialInfluence", "QUESTION:7");
    	m.put("nodeType", "Author");
    	m.put("direction", "0");
    	nodeRO.deriveAttributeBySocialInfluence(m);
    	*/
    	nodeRO.deleteNodeById(2L);
    }
    public NodeRO(){

    }

    public PageDao getPageDao() {
		return pageDao;
	}
	public void setPageDao(PageDao pageDao) {
		this.pageDao = pageDao;
	}
	
	public ActivityService getActivityService() {
		return activityService;
	}
	public void setActivityService(ActivityService activityService) {
		this.activityService = activityService;
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

	public JobDao getJobDao() {
		return jobDao;
	}
	public void setJobDao(JobDao jobDao) {
		this.jobDao = jobDao;
	}
	public RoleDao getRoleDao() {
		return roleDao;
	}

	public void setRoleDao(RoleDao roleDao) {
		this.roleDao = roleDao;
	}

    public EdgeDao getEdgeDao() {
		return edgeDao;
	}

	public void setEdgeDao(EdgeDao edgeDao) {
		this.edgeDao = edgeDao;
	}	
	
    public QuestionDao getQuestionDao() {
		return questionDao;
	}

	public void setQuestionDao(QuestionDao questionDao) {
		this.questionDao = questionDao;
	}

	public SurveyDao getSurveyDao() {
		return surveyDao;
	}
	public void setSurveyDao(SurveyDao surveyDao) {
		this.surveyDao = surveyDao;
	}
		
    public ActivityDao getActivityDao() {
		return activityDao;
	}
	public void setActivityDao(ActivityDao activityDao) {
		this.activityDao = activityDao;
	}
	
	public EdgeRO getEdgeRO() {
		return edgeRO;
	}
	public void setEdgeRO(EdgeRO edgeRO) {
		this.edgeRO = edgeRO;
	}
	
	public NodeDTO getNodeById(Long id){
        Node node = nodeDao.loadById(id);
        if (node == null) return null;
        else return new NodeDTO(node);
    }
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map loadCurrentNode(String nodeIdString){
		Long nodeId = Long.parseLong(nodeIdString);
		logger.info("Loading current node (id=" + nodeId + ")");
		Map result = new HashMap();
		Node node = nodeDao.loadById(nodeId);		
		result.put("node", new NodeDTO(node));
		List<EdgeDTO> incomingEdges = edgeRO.getIncomingEdges(nodeId, true);
		List<EdgeDTO> outgoingEdges = edgeRO.getOutgoingEdges(nodeId, true);		
		result.put("incomingEdges", incomingEdges);
		result.put("outgoingEdges", outgoingEdges);	
		
		// some extra info needed to render the tree structure
		Set<Long> nodeIds = new HashSet<Long>();
		for (EdgeDTO dto : incomingEdges){
			nodeIds.add(dto.fromNodeId);
			nodeIds.add(dto.toNodeId);
			if (dto.creatorId != null) nodeIds.add(dto.creatorId);
		}
		for (EdgeDTO dto : outgoingEdges){
			nodeIds.add(dto.fromNodeId);
			nodeIds.add(dto.toNodeId);
			if (dto.creatorId != null) nodeIds.add(dto.creatorId);
		}
		List<Node> nodes = nodeDao.findByIds(nodeIds);
		List<NodeDTO> extraNodes = new ArrayList<NodeDTO>();
		for (Node n : nodes){
			NodeDTO d = new NodeDTO();
			d.shallowCopy(n);
			extraNodes.add(d);
		}
		result.put("extraNodes", extraNodes);
		
		logger.info("Loaded.");		
		return result;
	}
	
    public List<NodeDTO> getAllNodes(){ 
    	logger.debug("getAllNodes...");       
        List<NodeDTO> dtos = new ArrayList<NodeDTO>();
        
        List<Node> nodes = nodeDao.getAll();
        for (Node node : nodes){
        	NodeDTO dto = new NodeDTO();
        	dto.shallowCopy(node);
            dtos.add(dto);
        }

        logger.debug("getAllNodes(" + dtos.size() + ")...done");
        return dtos;
    }

    public Collection<Map> getPlainNodes(){
    	logger.info("Get plain nodes...");
    	return nodeDao.getAllPlainNodes().values();
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Map getNodesByCriteria(Map req){ 
    	logger.info("get nodes by criteria...");
		Map result = new HashMap();
		
    	String includeCount = (String)req.get("count");
    	if (includeCount != null && includeCount.equals("1")){
    		Integer count = nodeDao.getNodesCountByCriteria(req);
    		result.put("count", count.toString());
    	}
    	
    	List<NodeDTO> dtos = new ArrayList<NodeDTO>();
    	List<Node> nodes = nodeDao.findNodesByCriteria(req);
    	for (Node node : nodes){
    		NodeDTO dto = new NodeDTO();
    		dto.shallowCopy(node);
    		dtos.add(dto);
    	}
    	result.put("nodes", dtos);
    	
    	return result;
    }
    
    public NodeDTO getNodeByUsername(String username){
    	Node node = nodeDao.loadByUsername(username);
    	if (node != null) return new NodeDTO(node);
    	else return null;
    }
    
    public List<NodeDTO> getNodesByType(String type){
        List<Node> nodes = nodeDao.findByType(type);
        List<NodeDTO> dtos = new ArrayList<NodeDTO>();
        for (Node node : nodes){
        	NodeDTO dto;
        	dto = new NodeDTO(node);
        	
//        	dto = new NodeDTO();
//        	dto.shallowCopy(node);
        	
            dtos.add(dto);
        }
        return dtos;
    }
    
    @Transactional
    public NodeDTO saveNode(NodeDTO dto) throws Exception{
    	Node node = dto2node(dto);
        nodeDao.save(node);
        return new NodeDTO(node);
    }
    
    @Transactional
	public List<NodeDTO> saveNodes(List<NodeDTO> dtos) throws Exception{
    	List<Node> nodes = new LinkedList<Node>();
    	List<NodeDTO> dtoList = new ArrayList<NodeDTO>();
    	
    	// convert dtos to nodes
    	for (NodeDTO dto : dtos){
    		nodes.add(dto2node(dto));
    	}
    	
    	// save or update
    	nodeDao.save(nodes);
    	        
    	// convert nodes to dtos
        for (Node node : nodes){
            dtoList.add(new NodeDTO(node));
        }
        
        return dtoList;
    }

    private Node dto2node(NodeDTO dto) throws Exception{
    	Node node;
        if (dto.nodeId == 0) {     
        	if (dto.username.length() > 50) {
        		String msg = "Username is too long (> 50): " + dto.username;
        		logger.error(msg);
        		throw new Exception(msg);
        	}
        	if (nodeDao.findByUsername(dto.username) != null) {
        		String msg = "Duplicated Username: " + dto.username;
        		logger.error(msg);
        		throw new Exception(msg);
        	}
        	logger.info("create Node (username=" + dto.username + ")");
            node = new Node();

			Survey survey = surveyDao.findById(1L);
			String defaultPassword = survey.getAttribute(Constants.SURVEY_DEFAULT_PASSWORD);
			if (defaultPassword == null) defaultPassword = "sonic";
			if (defaultPassword.equals("rAnDoM")){
				RandomString rs = new RandomString(8);
				dto.password = rs.nextString();
			} else dto.password = defaultPassword;
        } else {
        	logger.info("update Node (username=" + dto.username + ")");
            node = nodeDao.findById(dto.nodeId);            
            if (node == null) return null;
            node.setVersion(dto.version);
        }

        node.setType(dto.type);
        if (dto.type.equals(Constants.NODE_TYPE_USER) && dto.label.trim().length() == 0){
        	node.setLabel(dto.lastName + ", " + dto.firstName);
        } else node.setLabel(dto.label);
        if (node.getLabel().length() == 0) node.setLabel("!EMPTY!");
        
        node.setUri(dto.uri);
        
        node.setUsername(dto.username);
        node.setPassword(dto.password);
        node.setFirstName(dto.firstName);
        node.setLastName(dto.lastName);
        node.setMidName(dto.midName);
        node.setAddr1(dto.addr1);
        node.setAddr2(dto.addr2);
        node.setCity(dto.city);
        node.setState(dto.state);
        node.setCountry(dto.country);
        node.setZipcode(dto.zipcode);
        node.setEmail(dto.email);
        node.setPhone(dto.phone);
        node.setCell(dto.cell);
        node.setFax(dto.fax);
        node.setDepartment(dto.department);
        node.setOrganization(dto.organization);
        node.setUnit(dto.unit);
        node.setEnabled(dto.enabled);
        
        node.setAttributes(dto.attributes);
        node.setLongAttributes(dto.longAttributes);
        
        // set roles
        Set<Role> roles = new HashSet<Role>();
        if (node.getType().equals(Constants.NODE_TYPE_USER)) {
        	roles.add(roleDao.findByName(Constants.ROLE_USER));
        }
        for (Object roleId : dto.roles){
            Role role = roleDao.findById(Long.parseLong(roleId.toString()));
            if (role != null) roles.add(role);
        }
        node.setRoles(roles);

        // set groups
        Set<Group> groups = new HashSet<Group>();
        groups.add(groupDao.findByName(Constants.GROUP_ALL));
        if (node.getType().equals(Constants.NODE_TYPE_USER)){
            groups.add(groupDao.findByName(Constants.GROUP_USER));        	
        } else {
        	String nodeType = node.getType();
        	String groupName = Constants.GROUP_NODE_TYPE_PREFIX + nodeType;
        	Group group = groupDao.findByName(groupName);
        	if (group == null){
        		group = new Group();
        		group.setName(groupName);
        		groupDao.save(group);
        		logger.debug("created new group: " + group.getName());
        	}
        	groups.add(group);        	
        }
        for (Object groupId : dto.groups){
            Group group = groupDao.findById(Long.parseLong(groupId.toString()));
            if (group != null) groups.add(group);
        }
        node.setGroups(groups);
        
        return node;
    }

    @Transactional
    public List<Long> deleteNodeByIds(List ids){
    	List<Long> deletedIds = new LinkedList<Long>();
    	for (Object nodeId : ids){
    		Long id = Long.parseLong(nodeId.toString());
    		deletedIds.add(deleteNodeById(id));
    	}
    	return deletedIds;
    }
    
    @Transactional
    public Long deleteNodeById(Long id){
    	logger.info("delete node id=" + id);
        Node node = nodeDao.findById(id);
        
        if (node != null) {
        	logger.debug("username: " + node.getUsername());
            clearDataById(id, Constants.CLEAR_DATA_ALL_BUT_CONTACTS);
            clearDataById(id, Constants.CLEAR_DATA_CONTACTS);
            
            // also need to remove all edges referencing the node
            edgeDao.delete(edgeDao.findByFromNodeId(id));
            edgeDao.delete(edgeDao.findByToNodeId(id));
            
            // remove the reference on Survey, well, it (remove admin?!) will never happen 
            
            // remove activities
            Collection<Activity> acts = activityDao.getActivitiesBySubject(node);
            acts.addAll(activityDao.getActivitiesByObject(node));
            activityDao.delete(acts);
            
            // to avoid optimistic locking failure (node has been updated when clear data)
            node = nodeDao.findById(id);
            nodeDao.delete(node);
        }
        
        logger.info("node deleted.");
        return id;
    }
   
    @Transactional
    public List<NodeDTO> clearDataByIds(List ids, String type){
    	List<NodeDTO> dtoList = new LinkedList<NodeDTO>();
    	
    	for (Object nodeId : ids){
    		Long id = Long.parseLong(nodeId.toString());
    		dtoList.add(clearDataById(id, type));
    	}
    	
    	return dtoList;
    }    
    

    private NodeDTO clearDataById(Long id, String type){
    	logger.info("clear data: " + type + " for node(id=" + id + ").");
    	
        Node node = nodeDao.loadById(id); 
        NodeDTO dto = null;
        if (type.equals(Constants.CLEAR_DATA_CONTACTS)) dto = clearContacts(node);
        else if (type.equals(Constants.CLEAR_DATA_ALL_BUT_CONTACTS)) dto = clearAllButContacts(node);
        else if (type.equals(Constants.CLEAR_DATA_TRACES)) dto = clearTraces(node);
        else {
        	logger.warn("unrecognized type of data.");
        }
        
        return dto;
    }
    
    private NodeDTO clearTraces(Node node){
    	List<Activity> acts = activityDao.getActivitiesBySubject(node);
    	activityDao.delete(acts);
    	return new NodeDTO(node);
    }
    
    private NodeDTO clearContacts(Node node){
    	List<Question> questions = questionDao.getAll();
    	for (Question question : questions){
    		if (!question.isContactChooser()) continue;
    		
    		String groupName = Group.getPrivateGroupName(node.getUsername(), question.getShortName());
	        Group privateGroup = groupDao.loadByName(groupName);	        
	    	if (privateGroup != null) {
	    		logger.debug("remove nodes from private group: " + privateGroup.getName());
	    		List<Node> nodes = new LinkedList<Node>();
	    		for (Node n : privateGroup.getNodes()){    	
	    			n = nodeDao.loadById(n.getId());
	    			n.getGroups().remove(privateGroup);  
	    			nodes.add(n);
	    		}
	        	nodeDao.save(nodes);
	        	
	    		logger.debug("remove private group: " + privateGroup.getName());  
	    		groupDao.delete(privateGroup);
	    	}
    	}
    		
    	return new NodeDTO(node);
    }
    
    private NodeDTO clearAllButContacts(Node node){
    	logger.debug("remove node attributes");
    	node.getAttributes().clear();
    	node.getLongAttributes().clear();
    	nodeDao.save(node);
    	
    	logger.debug("remove outgoing edges");
    	List<Edge> edges = edgeDao.findByFromNodeId(node.getId());
    	List<Edge> outgoingEdges = new ArrayList<Edge>();
    	for (Edge edge : edges){
    		if (edge.getCreator() == null) outgoingEdges.add(edge);
    	}
    	edgeDao.delete(outgoingEdges);
    	
    	logger.debug("remove created edges and taggings (may be need to reserve those non-question taggings?!)");
    	List<Edge> createdEdges = edgeDao.findByCreatorId(node.getId());
    	edgeDao.delete(createdEdges); 
    	
    	return new NodeDTO(node);
    }

    
    @Transactional
    @SuppressWarnings("unchecked")
	public Map updatePassword(Map data){    
    	Map result = new HashMap();
    	String nodeIdString = (String)data.get("nodeId");
    	String username = (String)data.get("username");
    	Node node = null;
    	if (nodeIdString != null){
    		Long nodeId = Long.parseLong(nodeIdString);
    		node = nodeDao.loadById(nodeId);
    		if (node == null) {
    			result.put("msg", "Node with id=" + nodeIdString + " cannot be found.");
    			return result;
    		}
    	} else if(username != null) {    		
    		node = nodeDao.loadByUsername(username);
    		if (node == null) {
    			result.put("msg", "Node with username=" + username + " cannot be found.");
    			return result;
    		}
    	} else {
    		result.put("msg", "What is the identity of the node you are trying to modify password?");
    		return result;
    	}
    	logger.info("updating password for node(username=" + node.getUsername() + ")");
    	
    	String password = data.get("password").toString();
    	 
    	node.setPassword(password);
    	node.setAttribute(Constants.NODE_FIRST_TIMER, "N");
    	nodeDao.save(node);
    	
    	logger.info("password updated.");
    	result.put("node", new NodeDTO(node));
    	return result;
    }
    
    @Transactional
    public String generatePassword(Map data){
    	logger.info("generate passwords...");
    	StringBuilder sb = new StringBuilder();
    	String groupId = (String)data.get("groupId");
    	String defaultPassword = (String)data.get("defaultPassword");
    	logger.debug("groupId: " + groupId + ", defaultPassword: " + defaultPassword);
    	
    	List<Long> nodeIds = groupDao.getNodeIdsByGroupId(Long.parseLong(groupId));
    	nodeIds.remove(1L); // admin password cannot be overwrite
    	List<Node> nodes = nodeDao.findByIds(nodeIds);
    	RandomString rs = new RandomString(8);
    	for (Node node : nodes){
    		String password;
    		if(!defaultPassword.equals("rAnDoM")){
    			password = defaultPassword;
    		} else password = rs.nextString();
    		
    		node.setPassword(password);
    		sb.append(node.getUsername()).append(",").append(password).append("\n");
    	}
    	nodeDao.save(nodes);
    	
    	return sb.toString();
    }
    
    public List<Map<String, String>> getNodeTypeDescriptions(){
    	logger.debug("getNodeTypeDescriptions...");
    	
    	List<Map<String, String>> ntds = GeneralUtil.getNodeDescriptions();
    	List<Map<String, String>> fullNtds = new LinkedList<Map<String, String>>();
    	List<String> nodeTypes = nodeDao.getNodeTypes();
    	
    	for (String nodeType : nodeTypes){
    		Map<String, String> ntd = GeneralUtil.getNodeDescription(ntds, nodeType);
    		if (ntd == null){
	    		ntd = new HashMap<String, String>();
	    		ntd.put("type", nodeType);
	    		ntd.put("label", nodeType);
    		}
    		
    		fullNtds.add(ntd);
    	}
    	
		logger.debug("getNodeTypeDescriptions...done");
    	return fullNtds;
    }
    
    public void saveNodeTypeDescriptions(List<Map<String, String>> ntds){
    	logger.info("saveNodeDescriptions...");
    	GeneralUtil.saveNodeDescriptions(ntds);
		logger.info("saveNodeDescriptions... done");
    }
    
    /**
     * 
     * @param data
     * @return >0: attributes created; =0: no attributes created; =-1: attrName not available
     * @throws IOException 
     */
    @Transactional
    public int deriveAttributeByProduct(Map data) throws Exception{
    	Question q1 = null;
    	Question q2 = null;
    	Collection<String> attrNames1 = null;
    	Collection<String> attrNames2 = null;
    	String attrName = (String)data.get("attrName");
    	List<String> nodeTypes = (List<String>)data.get("nodeTypes");
    	String attr1 = (String)data.get("attr1");
    	String attr2 = (String)data.get("attr2");
    	JobDTO job = (JobDTO)data.get("job");
    	String override = (String)data.get("override"); // override existing attribute if the parameter is not null
    	logger.info("derive attribute by product(attrName=" + attrName + ", nodeTypes=" + nodeTypes + ", attr1=" + attr1 + ", attr2=" + attr2 + ")...");
    	if (isAttrNameAvailable(attrName) == 0) {
    		if (override == null) return -1;
    		//else nodeDao.deleteAttributeByKey(attrName);
    	}
    	
    	
    	if (attr1.indexOf(VisUtil.ATTR_PREFIX) >=0) attr1 = attr1.substring(VisUtil.ATTR_PREFIX.length());
    	if (attr1.indexOf(VisUtil.QUESTION_PREFIX) >=0) {
    		String qid = attr1.substring(VisUtil.QUESTION_PREFIX.length());
    		q1 = questionDao.findById(Long.parseLong(qid));
    		if (q1 == null) {
    			logger.warn("question(id=" + qid + ") is not found.");
    			return 0;
    		}
    		attrNames1 = q1.getPossibleAttributeNames();
    	}
    	
    	
    	if (attr2.indexOf(VisUtil.ATTR_PREFIX) >=0) attr2 = attr2.substring(VisUtil.ATTR_PREFIX.length());
    	if (attr2.indexOf(VisUtil.QUESTION_PREFIX) >=0) {
    		String qid = attr2.substring(VisUtil.QUESTION_PREFIX.length());
    		q2 = questionDao.findById(Long.parseLong(qid));
    		if (q2 == null) {
    			logger.warn("question(id=" + qid + ") is not found.");
    			return 0;
    		}
    		attrNames2 = q2.getPossibleAttributeNames();
    	}
    	
    	List<Node> nodes = loadNodesByTypes(nodeTypes);
    	List<Node> updatedNodes = new LinkedList<Node>();
    	for (Node node : nodes){
    		
    		String v1 = null;
    		if (q1 == null) v1 = node.getAttribute(attr1);
    		else {
    			Set<String> attrNames = new HashSet<String>(attrNames1);
    			attrNames.retainAll(node.getAttributes().keySet());
    			if (attrNames.isEmpty()){
    				logger.warn("node(id=" + node.getId() + ") has not answer question: " + q1.getLabel());
    				continue;
    			} else {
    				String key = attrNames.iterator().next();
    				String fieldName = Question.getFieldNameFromKey(key);
    				Field field = q1.getFieldByName(fieldName);
    				if (field != null) v1 = field.getLabel();
    			}
    		}
    		if (v1 == null){
    			logger.warn("node(id=" + node.getId() + ") lack of categorical attribute: " + attr1);
    			continue;
    		}
    		
    		
    		String v2 = null;
    		if (q2 == null) v2 = node.getAttribute(attr2);
    		else {
    			Set<String> attrNames = new HashSet<String>(attrNames2);
    			attrNames.retainAll(node.getAttributes().keySet());
    			if (attrNames.isEmpty()){
    				logger.warn("node(id=" + node.getId() + ") has not answer question: " + q2.getLabel());
    				continue;
    			} else {
    				String key = attrNames.iterator().next();
    				String fieldName = Question.getFieldNameFromKey(key);
    				Field field = q2.getFieldByName(fieldName);
    				if (field != null) v2 = field.getLabel();
    			}
    		}
    		if (v2 == null){
    			logger.warn("node(id=" + node.getId() + ") lack of categorical attribute: " + attr2);
    			continue;
    		}
    		
    		node.setAttribute(attrName, v1 + ", " + v2);
    		updatedNodes.add(node);
    	}
    	
    	nodeDao.save(updatedNodes);
    	
    	// schedule job?
    	if (job != null){
    		checkDuplicateJobName(job.name);
    		
    		logger.info("Scheduling a new job: " + job.name);
    		Job j = job.toJob();
    		
    		j.setClassName("ciknow.ro.NodeRO");
    		j.setMethodName("deriveAttributeByProduct");
    		j.setBeanName("nodeRO");
    		Class[] pTypes = new Class[]{Map.class};
    		j.setParameterTypes(GeneralUtil.objectToByteArray(pTypes));		
    		
    		data.remove("job");    		
    		data.put("override", "1"); // the scheduled job will override existing attribute
    		Object[] pValues = new Object[]{data};
    		j.setParameterValues(GeneralUtil.objectToByteArray(pValues));
    		jobDao.save(j);
    	}
    	
    	int num = updatedNodes.size();
    	logger.info(num + " attributes derived.");
    	
    	return num;
    }

    private List<Node> loadNodesByTypes(List<String> nodeTypes){
    	List<Node> nodes = new LinkedList<Node>();
    	for (String nodeType : nodeTypes){
    		nodes.addAll(nodeDao.loadByType(nodeType));
    	}
    	return nodes;
    }
    
    /**
     * check whether a attribute name is still available
     * @param name new attribute name
     * @return 1: available 0: unavailable
     */
    public int isAttrNameAvailable(String name){
    	List<String> attrs = nodeDao.getAttributeNames(null);
    	if (attrs.contains(name)) return 0;;
    	return 1;
    }
    
    public void deleteAttributeByName(String name){
    	nodeDao.deleteAttributeByKey(name);
    }
    
	private Question createContinuousQuestion(String shortName, String fieldName) throws Exception {		
		Question q = questionDao.findByShortName(shortName);
		if (q != null) {
			QuestionRO questionRO = (QuestionRO)Beans.getBean("questionRO");
			questionRO.deleteQuestion(q.getId());
		}
		
		logger.info("creating derived Continuous question...");
		Survey survey = surveyDao.findById(1L);
		
		Page page = new Page();
        page.setName("page: " + shortName);
        page.setLabel(page.getName());
        page.setInstruction("this is page " + shortName);
        survey.getPages().add(page);
        page.setSurvey(survey);

    	Question question = new Question();		
		question.setPage(page);
		page.getQuestions().add(question);					
		question.setType(Constants.CONTINUOUS);
		question.setShortName(shortName);
		question.setLabel("derived attributes: " + shortName);				
		question.setRowPerPage(20);		
		question.setHtmlInstruction("This is a auto generated question when creating derived attribute.");
		
		// fields
		Field field = new Field();
		field.setName(fieldName);
		field.setLabel(fieldName);
		question.getFields().add(field);
		field.setQuestion(question);
		
		// attach default visible groups
		Set<Group> visibleGroups = new HashSet<Group>();		
		visibleGroups.add(groupDao.findByName(Constants.GROUP_USER));
		question.setVisibleGroups(visibleGroups);

		questionDao.save(question);
		logger.info("created derived question(" + question.getShortName() + ")... done");
		return question;
	}
	
	@Transactional
    public int deriveAttributeByAnalytics(Map data) throws Exception{
    	logger.info("deriving attribute by analytics ...");
    	String questionShortName = (String)data.get("questionShortName");    	
    	String fieldName = (String)data.get("fieldName");  
    	List<String> edgeTypes = (List<String>)data.get("edgeTypes");
    	String analyticName = (String)data.get("analyticName");
    	int direction = Integer.parseInt((String)data.get("direction"));
    	String undirectedOperator = (String)data.get("undirectedOperator");
    	String operator = (String) data.get("operator");
    	JobDTO job = (JobDTO)data.get("job");
    	
    	Question question = createContinuousQuestion(questionShortName, fieldName);
    	String attrName = question.makeFieldKey(question.getFieldByName(fieldName));   	    	
    	
    	logger.info("questionShortName: " + questionShortName 
    			+ ", fieldName=" + fieldName + ", edgeTypes=" + edgeTypes 
    			+ ", analyticName=" + analyticName + ", direction=" + direction + ")...");
    	
    	NetworkExtractor extractor = (NetworkExtractor)Beans.getBean("networkExtractor");
    	Map network = extractor.getCustomNetwork(edgeTypes, operator,
														null, null,
														null, null,
														null, 
														null, 
														null, 
														"0", "0");
    	
    	Collection<Node> nodes = (Collection<Node>)network.get("nodes");
    	Collection<Edge> edges = (Collection<Edge>)network.get("edges");
    	NetworkMetrics nm = new NetworkMetrics();
    	Graph graph = NetworkAnalytics.prepareGraph(nodes, edges, direction, undirectedOperator, nm);    	
    	if (graph == null) return 0;
    	
    	logger.info("networkMetrics is directed: " + nm.getDirected());
    	
    	if (analyticName.equals("inDegree") || analyticName.equals("outDegree")){
    		NetworkAnalytics.calculateDegree(graph, nm);  
    	} else if (analyticName.equals("inCloseness") || analyticName.equals("outCloseness")){
    		Distance d = new UnweightedShortestPath(graph);	
    		NetworkAnalytics.calculateCloseness(graph, d, nm);
    	} else if (analyticName.equals("betweenness")){
    		NetworkAnalytics.calculateBetweenness(graph, nm);
    	} else if (analyticName.equals("scanning")){
    		NetworkAnalytics.calculateScanning(graph, nm);
    	} else if (analyticName.equals("pageRank")){
    		NetworkAnalytics.calculatePageRank(DirectionTransformer.toDirected(graph, false), nm);
    	} else if (analyticName.equals("clusteringCoefficient")){
    		NetworkAnalytics.calculateClusteringCoefficient(graph, nm);
    	}
    	
		Map<Long, Node> nodeMap = GeneralUtil.getNodeMap(nodes);
    	List<Node> updatedNodes = new LinkedList<Node>();
    	NumberFormat nf = new DecimalFormat("0.00");
    	for (Long nodeId : nm.getIndividualMetricMap().keySet()){
    		IndividualMetric im = nm.getIndividualMetricMap().get(nodeId);
    		Node node = nodeMap.get(nodeId);
    		if (node == null) continue;
    		
        	if (analyticName.equals("inDegree")){
        		node.setAttribute(attrName, im.inDegree.toString());
        	} else if (analyticName.equals("outDegree")){
        		node.setAttribute(attrName, im.outDegree.toString());
        	} else if (analyticName.equals("inCloseness")){
        		if (im.inCloseness.isNaN()) continue;
        		node.setAttribute(attrName, nf.format(im.inCloseness.doubleValue()));
        	} else if (analyticName.equals("outCloseness")){
        		if (im.outCloseness.isNaN()) continue;
        		node.setAttribute(attrName, nf.format(im.outCloseness.doubleValue()));
        	} else if (analyticName.equals("betweenness")){
        		if (im.betweenness.isNaN()) continue;
        		node.setAttribute(attrName, nf.format(im.betweenness.doubleValue()));
        	} else if (analyticName.equals("scanning")){
        		if (im.scanning.isNaN()) continue;
        		node.setAttribute(attrName, nf.format(im.scanning.doubleValue()));
        	} else if (analyticName.equals("pageRank")){
        		if (im.pageRank.isNaN()) continue;
        		node.setAttribute(attrName, nf.format(im.pageRank.doubleValue()));
        	} else if (analyticName.equals("clusteringCoefficient")){
        		if (im.clusteringCoefficient.isNaN()) continue;
        		node.setAttribute(attrName, nf.format(im.clusteringCoefficient.doubleValue()));
        	}
        	
        	updatedNodes.add(node);
    	}
    	
    	nodeDao.save(updatedNodes);
    	
    	// schedule job?
    	if (job != null){
    		checkDuplicateJobName(job.name);
    		
    		logger.info("Scheduling a new job: " + job.name);
    		Job j = job.toJob();
    		
    		j.setClassName("ciknow.ro.NodeRO");
    		j.setMethodName("deriveAttributeByAnalytics");
    		j.setBeanName("nodeRO");
    		Class[] pTypes = new Class[]{Map.class};
    		j.setParameterTypes(GeneralUtil.objectToByteArray(pTypes));		
    		
    		data.remove("job");    		
    		Object[] pValues = new Object[]{data};
    		j.setParameterValues(GeneralUtil.objectToByteArray(pValues));
    		jobDao.save(j);
    	}
    	
    	int num = updatedNodes.size();
    	logger.info(num + " attributes derived.");
    	
    	return num;
    }
    
	@Transactional
    public int deriveAttributeByEquation(Map data) throws Exception{
    	logger.info("derive attribute by equation...");
    	Question q1 = null;
    	Question q2 = null;
    	Collection<String> attrNames1 = null;
    	Collection<String> attrNames2 = null;
    	String questionShortName = (String) data.get("questionShortName");
    	String fieldName = (String) data.get("fieldName");
    	String operator = (String) data.get("operator");
    	List<String> nodeTypes = (List<String>)data.get("nodeTypes");
    	JobDTO job = (JobDTO)data.get("job");
    	
    	Question question = createContinuousQuestion(questionShortName, fieldName);
    	String attrName = question.makeFieldKey(question.getFieldByName(fieldName));   	    	
    	
    	String varLeft = (String) data.get("varLeft");
    	List<Map> varLeftValues = (List<Map>) data.get("varLeftValues");
    	Map<String, Number> varLeftValueMap = new HashMap<String, Number>();
    	if (varLeftValues != null){
	    	for (Map m : varLeftValues){
	    		String key = (String)m.get("value");
	    		String value = (String)m.get("calcValue");
	    		if (value != null){
	    			varLeftValueMap.put(key, Double.parseDouble(value));
	    		}
	    	}
    	}
    	String varRight = (String) data.get("varRight");
    	List<Map> varRightValues = (List<Map>) data.get("varRightValues");
    	Map<String, Number> varRightValueMap = new HashMap<String, Number>();
    	if (varRightValues != null){
	    	for (Map m : varRightValues){
	    		String key = (String)m.get("value");
	    		String value = (String)m.get("calcValue");
	    		if (value != null){
	    			varRightValueMap.put(key, Double.parseDouble(value));
	    		}
	    	}
    	}    	
    	
    	logger.debug("questionShortName: " + questionShortName + 
    			", fieldName: " + fieldName + 
    			", nodeTypes: " + nodeTypes + 
    			", varLeft: " + varLeft + 
    			", varRight: " + varRight + 
    			", operator: " + operator);
    	
    	if (varLeft.indexOf(VisUtil.ATTR_PREFIX) >=0) varLeft = varLeft.substring(VisUtil.ATTR_PREFIX.length());
    	if (varLeft.indexOf(VisUtil.QUESTION_PREFIX) >=0) {
    		String qid = varLeft.substring(VisUtil.QUESTION_PREFIX.length());
    		q1 = questionDao.findById(Long.parseLong(qid));
    		if (q1 == null) {
    			logger.warn("question(id=" + qid + ") is not found.");
    			return 0;
    		}
    		attrNames1 = q1.getPossibleAttributeNames();
    	}
    	
    	
    	if (varRight.indexOf(VisUtil.ATTR_PREFIX) >=0) varRight = varRight.substring(VisUtil.ATTR_PREFIX.length());
    	if (varRight.indexOf(VisUtil.QUESTION_PREFIX) >=0) {
    		String qid = varRight.substring(VisUtil.QUESTION_PREFIX.length());
    		q2 = questionDao.findById(Long.parseLong(qid));
    		if (q2 == null) {
    			logger.warn("question(id=" + qid + ") is not found.");
    			return 0;
    		}
    		attrNames2 = q2.getPossibleAttributeNames();
    	}
    	

    	List<Node> nodes = loadNodesByTypes(nodeTypes);
    	NumberFormat nf = new DecimalFormat("0.00");
    	List<Question> questions = questionDao.getAll();
    	Map<String, Question> questionMap = new HashMap<String, Question>();
    	for (Question q : questions){
    		questionMap.put(q.getShortName(), q);
    	}
    	List<Node> updatedNodes = new LinkedList<Node>();
    	for (Node node : nodes){    		
    		Number v1 = null;
    		if (q1 == null) {
    			String s = node.getAttribute(varLeft);
    			if (s == null) continue;
    			else if (s.startsWith("S" + Constants.SEPERATOR)){
    				String shortName = Question.getShortNameFromKey(s);
    				String scaleName = Question.getScaleNameFromKey(s);
    				Question q = questionMap.get(shortName);
    				if (q == null) continue;    				
    				Scale scale = q.getScaleByName(scaleName);
    				if (scale == null) continue;
    				v1 = scale.getValue();
    			} else {
    				try{
    					v1 = Double.parseDouble(s);
    				}catch (NumberFormatException e){
    					logger.error("Attribute '" + varLeft + "' is not numeric!");
    					throw new Exception("Attribute '" + varLeft + "' is not numeric!");
    				}
    			}
    		}
    		else {
    			Set<String> attrNames = new HashSet<String>(attrNames1);
    			attrNames.retainAll(node.getAttributes().keySet());
    			if (attrNames.isEmpty()){
    				logger.warn("node(id=" + node.getId() + ") has not answer question: " + q1.getLabel());
    				continue;
    			} else {
    				String key = attrNames.iterator().next();
    				v1 = varLeftValueMap.get(key);
    				if (v1 == null){    	
	    				String fName = Question.getFieldNameFromKey(key);
	    				Field field = q1.getFieldByName(fName);
	    				if (field != null) v1 = field.getIndex();
    				}
    			}
    		}
    		if (v1 == null){
    			logger.warn("node(id=" + node.getId() + ") lack of numeric attribute: " + varLeft);
    			continue;
    		}
    		
    		
    		Number v2 = null;
    		if (q2 == null){
    			String s = node.getAttribute(varRight);
    			if (s == null) continue;
    			else if (s.startsWith("S" + Constants.SEPERATOR)){
    				String shortName = Question.getShortNameFromKey(s);
    				String scaleName = Question.getScaleNameFromKey(s);
    				Question q = questionMap.get(shortName);
    				if (q == null) continue;
    				Scale scale = q.getScaleByName(scaleName);
    				if (scale == null) continue;
    				v2 = scale.getValue();
    			} else {
    				try {
    					v2 = Double.parseDouble(s);
    				} catch (NumberFormatException e){
    					logger.error("Attribute '" + varRight + "' is not numeric!");
    					throw new Exception("Attribute '" + varRight + "' is not numeric!");
    				}
    			}
    		}
    		else {
    			Set<String> attrNames = new HashSet<String>(attrNames2);
    			attrNames.retainAll(node.getAttributes().keySet());
    			if (attrNames.isEmpty()){
    				logger.warn("node(id=" + node.getId() + ") has not answer question: " + q2.getLabel());
    				continue;
    			} else {
    				String key = attrNames.iterator().next();
    				v2 = varRightValueMap.get(key);
    				if (v2 == null){
	    				String fName = Question.getFieldNameFromKey(key);
	    				Field field = q2.getFieldByName(fName);
	    				if (field != null) v2 = field.getIndex();
    				}
    			}
    		}
    		if (v2 == null){
    			logger.warn("node(id=" + node.getId() + ") lack of numeric attribute: " + varRight);
    			continue;
    		}
    		
    		Number v = null;
    		if (operator.equals("+")) v = v1.doubleValue() + v2.doubleValue();
    		else if (operator.equals("-")) v = v1.doubleValue() - v2.doubleValue();
    		else if (operator.equals("x")) v = v1.doubleValue() * v2.doubleValue();
    		else if (operator.equals("/")) v = v1.doubleValue() / v2.doubleValue();
    		else {
    			logger.warn("unrecognized operator: " + operator);
    			throw new Exception("Unrecognized operator: " + operator);
    		}
    		node.setAttribute(attrName, nf.format(v.doubleValue()));
    		updatedNodes.add(node);
    	}
    	
    	nodeDao.save(updatedNodes);
    	
    	// schedule job?
    	if (job != null){
    		checkDuplicateJobName(job.name);
    		
    		logger.info("Scheduling a new job: " + job.name);
    		Job j = job.toJob();
    		
    		j.setClassName("ciknow.ro.NodeRO");
    		j.setMethodName("deriveAttributeByEquation");
    		j.setBeanName("nodeRO");
    		Class[] pTypes = new Class[]{Map.class};
    		j.setParameterTypes(GeneralUtil.objectToByteArray(pTypes));		
    		
    		data.remove("job");    		
    		Object[] pValues = new Object[]{data};
    		j.setParameterValues(GeneralUtil.objectToByteArray(pValues));
    		jobDao.save(j);
    	}
    	
    	int num = updatedNodes.size();
    	logger.info(num + " attributes derived.");
    	
    	return num;
    }
    
    /**
     * This method may be implemented by construct edge matrix at once, but it 
     * may consume too much memory (and failed due to out of memory) if network is large.
     * Current implementation query neighbors for each node, it is garanteed to 
     * be scalable, but can be very slow for large network.
     * @param data
     * @return
     * @throws Exception
     */
	@Transactional
    public int deriveAttributeBySocialInfluence(Map data) throws Exception{
    	logger.info("derive attribute by social influence...");
    	String questionShortName = (String) data.get("questionShortName");
    	String fieldName = (String) data.get("fieldName");
    	List<String> edgeTypes = (List<String>) data.get("edgeTypes");
    	String var = (String) data.get("varSocialInfluence");
    	List<Map> varValues = (List<Map>) data.get("varValues");
    	List<String> nodeTypes = (List<String>)data.get("nodeTypes");
    	String direction = (String) data.get("direction");
    	JobDTO job = (JobDTO)data.get("job");
    	
    	Question question = createContinuousQuestion(questionShortName, fieldName);
    	String attrName = question.makeFieldKey(question.getFieldByName(fieldName));
    	
    	logger.debug("questionShortName: " + questionShortName 
    			+ ", fieldName: " + fieldName 
    			+ ", nodeTypes: " + nodeTypes 
    			+ ", varSocialInfluence: " + var 
    			+ ", direction: " + direction 
    			+ ", edgeTypes: " + edgeTypes);
    	
    	Map<String, Number> varValueMap = new HashMap<String, Number>();
    	if (varValues != null){
	    	for (Map m : varValues){
	    		String key = (String)m.get("value");
	    		String value = (String)m.get("calcValue");
	    		if (value != null){
	    			varValueMap.put(key, Double.parseDouble(value));
	    		}
	    	}
    	}

    	question = null;
    	Collection<String> attrNames1 = null;
    	if (var.indexOf(VisUtil.ATTR_PREFIX) >=0) var = var.substring(VisUtil.ATTR_PREFIX.length());
    	if (var.indexOf(VisUtil.QUESTION_PREFIX) >=0) {
    		String qid = var.substring(VisUtil.QUESTION_PREFIX.length());
    		question = questionDao.findById(Long.parseLong(qid));
    		if (question == null) {
    			logger.warn("question(id=" + qid + ") is not found.");
    			return 0;
    		}
    		attrNames1 = question.getPossibleAttributeNames();
    	}
    	
    	logger.info("loading all possibly related nodes...");
    	Map<String, Map<Long, Node>> typeToNodesMap = new HashMap<String, Map<Long, Node>>();
    	if (edgeTypes == null || edgeTypes.isEmpty()) edgeTypes = edgeDao.getEdgeTypes();
    	Collection<String> relatedNodeTypes = edgeDao.getNodeTypesByEdgeTypes(edgeTypes);
    	for (String type : relatedNodeTypes){
    		List<Node> nodes = nodeDao.loadByType(type);
    		Map<Long, Node> nodesMap = new HashMap<Long, Node>();
    		for (Node node : nodes){
    			nodesMap.put(node.getId(), node);
    		}    
    		typeToNodesMap.put(type, nodesMap);
    	}
    	
    	logger.info("loading nodes we want to add new attributes...");
    	List<Node> nodes = loadNodesByTypes(nodeTypes);
    	NumberFormat nf = new DecimalFormat("0.00");
    	List<Question> questions = questionDao.getAll();
    	Map<String, Question> questionMap = new HashMap<String, Question>();
    	for (Question q : questions){
    		questionMap.put(q.getShortName(), q);
    	}

    	logger.info("processing each node...");
    	List<Node> updatedNodes = new LinkedList<Node>();
    	for (Node node : nodes){
    		Set<Edge> edges = new HashSet<Edge>();
    		if (direction.equals("0") || direction.equals("1")) edges.addAll(edgeDao.loadByFromNodeId(node.getId()));
    		if (direction.equals("0") || direction.equals("2")) edges.addAll(edgeDao.loadByToNodeId(node.getId()));
    		
    		if (edges.isEmpty()) continue;
    		
    		double up = 0.0;
    		double down = 0.0;
    		for (Edge edge : edges){
    			if (!edgeTypes.contains(edge.getType())) continue;
    			if (edge.getFromNode().getId().equals(edge.getToNode().getId())) continue;
    			Node neighbor = edge.getFromNode().getId().equals(node.getId())?edge.getToNode():edge.getFromNode();
    	    	neighbor = typeToNodesMap.get(neighbor.getType()).get(neighbor.getId());
    	    	
        		Number v1 = null;
        		if (question == null) {
        			String s = neighbor.getAttribute(var);
        			if (s == null) continue;
        			else if (s.startsWith("S" + Constants.SEPERATOR)){
        				String shortName = Question.getShortNameFromKey(s);
        				String scaleName = Question.getScaleNameFromKey(s);
        				Question q = questionMap.get(shortName);
        				if (q == null) continue;    				
        				Scale scale = q.getScaleByName(scaleName);
        				if (scale == null) continue;
        				v1 = scale.getValue();
        			} else {
        				try{
        					v1 = Double.parseDouble(s);
        				}catch (NumberFormatException e){
        					logger.error("Attribute '" + var + "' is not numeric!");
        					throw new Exception("Attribute '" + var + "' is not numeric!");
        				}
        			}
        		}
        		else {
        			Set<String> attrNames = new HashSet<String>(attrNames1);
        			attrNames.retainAll(neighbor.getAttributes().keySet());
        			if (attrNames.isEmpty()){
        				//logger.warn("node(id=" + node.getId() + ") has not answer question: " + question.getLabel());
        				continue;
        			} else {
        				String key = attrNames.iterator().next();
        				v1 = varValueMap.get(key);
        				if (v1 == null){
	        				String fName = Question.getFieldNameFromKey(key);
	        				Field field = question.getFieldByName(fName);
	        				if (field != null) v1 = field.getIndex();
        				}
        			}
        		}
        		if (v1 == null){
        			//logger.warn("node(id=" + node.getId() + ") lack of numeric attribute: " + var);
        			continue;
        		}
        		
        		up += edge.getWeight() * v1.doubleValue();
        		down += edge.getWeight();
    		}
    		
    		if (down < 0.001) continue;
    		
    		Double d = up/down;
    		if (d.isNaN()) continue;
    		node.setAttribute(attrName, nf.format(d));
    		updatedNodes.add(node);
    	}
    	
    	nodeDao.save(updatedNodes);
    	
    	// schedule job?
    	if (job != null){
    		checkDuplicateJobName(job.name);
    		logger.info("Scheduling a new job: " + job.name);    		
    		Job j = job.toJob();
    		
    		j.setClassName("ciknow.ro.NodeRO");
    		j.setMethodName("deriveAttributeBySocialInfluence");
    		j.setBeanName("nodeRO");
    		Class[] pTypes = new Class[]{Map.class};
    		j.setParameterTypes(GeneralUtil.objectToByteArray(pTypes));		
    		
    		data.remove("job");    		
    		Object[] pValues = new Object[]{data};
    		j.setParameterValues(GeneralUtil.objectToByteArray(pValues));
    		jobDao.save(j);
    	}
    	
    	int num = updatedNodes.size();
    	logger.info(num + " attributes derived.");
    	
    	return num;
    }
    
	@Transactional
    @SuppressWarnings({"rawtypes" })
	public int deriveAttributeByProgress(Map data) throws Exception{
    	logger.info("derive attribute by progress...");
    	JobDTO job = (JobDTO)data.get("job");
    	
    	// get status map
    	List<Node> nodes = nodeDao.loadAll(); // TODO: limit to respondents only
    	List<Page> pages = pageDao.getAll();
    	List<Map<String, String>> progressList = activityService.getProgress(nodes, pages);
    	Map<String, String> statusMap = new HashMap<String, String>();
    	for (Map<String, String> p : progressList){
    		String nodeId = p.get("id");
    		String status = p.get("status");
    		statusMap.put(nodeId, status);
    	}
    	
    	int count = 0;
    	int total = 0;
    	List<Node> updatedNodes = new LinkedList<Node>();    	
    	for (Node node : nodes){    
    		String status = statusMap.get(node.getId().toString());
    		node.setAttribute("surveyProgress", status);
    		
    		count++;
    		total++;
    		updatedNodes.add(node);
    		if (count >= 5000){
    			nodeDao.save(updatedNodes);
    			updatedNodes.clear();
    			count = 0;
    			logger.debug(total + " nodes updated.");
    		}    		
    	}
    	nodeDao.save(updatedNodes);
    	logger.debug(total + " nodes updated.");
    	
    	// schedule job?
    	if (job != null){
    		checkDuplicateJobName(job.name);
    		
    		logger.info("Scheduling a new job: " + job.name);
    		Job j = job.toJob();
    		
    		j.setClassName("ciknow.ro.NodeRO");
    		j.setMethodName("deriveAttributeByProgress");
    		j.setBeanName("nodeRO");
    		Class[] pTypes = new Class[]{Map.class};
    		j.setParameterTypes(GeneralUtil.objectToByteArray(pTypes));		
    		
    		data.remove("job");    		
    		Object[] pValues = new Object[]{data};
    		j.setParameterValues(GeneralUtil.objectToByteArray(pValues));
    		jobDao.save(j);
    	}
    	
    	logger.info(total + " attributes derived.");
    	
    	return total;
    }
    
    private void checkDuplicateJobName(String name) throws Exception{
    	Job job = jobDao.getByName(name);
    	if (job != null){
    		throw new Exception("Duplicated Task Name: " + name);
    	}
    }
    
    /*
    @SuppressWarnings("unchecked")
	public List<Map> getCCLevelAttributes(Map data) throws Exception{
    	logger.info("get contact chooser level attributes...");
    	List<Map> result = new LinkedList<Map>();
    	List levels = (List) data.get("levels");
    	List ids = (List)data.get("nodeIds");
    	logger.debug("nodeIds: " + ids);
    	logger.debug("levels: " + levels);
    	
    	List<Long> nodeIds = new LinkedList<Long>();
    	for (Object id : ids){
    		nodeIds.add(Long.parseLong((String)id));
    	}
    	List<Node> nodes = nodeDao.loadByIds(nodeIds);

    	Map<String, Question> questionMap = new HashMap<String, Question>();
    	for (Question question : questionDao.getAll()){
    		questionMap.put(question.getShortName(), question);
    	}
    	
    	for (Node node : nodes){
    		Map ccAttrs = new HashMap();
    		ccAttrs.put("nodeId", node.getId().toString());
    		for (Object o:levels){
    			String level = (String)o;
    			if (level.equals("organization")) ccAttrs.put(level, node.getOrganization());
    			else if (level.equals("department")) ccAttrs.put(level, node.getDepartment());
    			else if (level.equals("unit")) ccAttrs.put(level, node.getUnit());
    			else if (level.equals("type")) ccAttrs.put(level, node.getType());
    			else if (level.equals("lastName")) ccAttrs.put(level, node.getLastName());
    			else if (level.equals("firstName")) ccAttrs.put(level, node.getFirstName());
    			else if (level.equals("city")) ccAttrs.put(level, node.getCity());
    			else if (level.equals("state")) ccAttrs.put(level, node.getState());
    			else if (level.equals("country")) ccAttrs.put(level, node.getCountry());
    			else if (level.equals("zipcode")) ccAttrs.put(level, node.getZipcode());
    			else if (level.equals("enabled")) ccAttrs.put(level, node.getEnabled());
    			else if (level.startsWith("Q" + Constants.SEPERATOR)){ // choice (single) or multipleChoice question 
    				String shortName = level.substring(2);
    				Question question = questionMap.get(shortName);
    				if (question.isSingleChoice()){
    					String value = null;
    					for (Field field : question.getFields()){
    						String key = "F" + Constants.SEPERATOR + shortName + Constants.SEPERATOR + field.getName();
    						value = node.getAttribute(key);
    						if (value != null) {
    							if (value.equals("1")) value = field.getLabel();
    							else {
    								// this is for "Other" popup
    							}
    							break;
    						}
    					}
    					
    					if (value == null) value = ""; // if no selection, set it as blank
    					ccAttrs.put(level, value);
    				} else logger.warn("unsupported question: " + shortName);
    			}
    			else throw new Exception("Attribute '" + level + "' is not available.");
    		}
    		result.add(ccAttrs);
    	}
    	
    	return result;
    }
    */

    
    //////////////////////// login/logout///////////////////////

    public Long getLoginNodeId(){
        CIKNOWUserDetails node = (CIKNOWUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        logger.info("get login node: " + node.getUsername());
        return node.getId();
    }

    @Transactional
    public void applyLoginMode(String mode){
    	logger.info("applying login mode: " + mode);
    	
    	List<Node> nodes = nodeDao.loadAll();
    	for (Node node : nodes){
    		node.setAttribute(Constants.NODE_LOGIN_MODE, mode);
    	}
    	nodeDao.save(nodes);
    	
    	logger.info("all nodes's login modes are updated.");
    }
    
    /*
     * @deprecated
     */
    public void logoutUser(){        
        // invalidate session
    	logger.info("logout...");
        FlexSession fs = FlexContext.getFlexSession();
        if (fs != null) fs.invalidate();
        SecurityContextHolder.clearContext();                
    }
}
