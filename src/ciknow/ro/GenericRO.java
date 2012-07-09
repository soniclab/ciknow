package ciknow.ro;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.jms.JMSException;
import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.EmailValidator;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.DataModelBuilder;
import org.apache.mahout.cf.taste.eval.IRStatistics;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;
import org.apache.mahout.cf.taste.eval.RecommenderIRStatsEvaluator;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.eval.AverageAbsoluteDifferenceRecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.eval.GenericRecommenderIRStatsEvaluator;
import org.apache.mahout.cf.taste.impl.eval.LoadEvaluator;
import org.apache.mahout.cf.taste.impl.eval.RMSRecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.model.GenericBooleanPrefDataModel;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericBooleanPrefUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.slopeone.SlopeOneRecommender;
import org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.SpearmanCorrelationSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.TanimotoCoefficientSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.UncenteredCosineSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.dom4j.DocumentException;
import org.springframework.transaction.annotation.Transactional;

import ciknow.dao.EdgeDao;
import ciknow.dao.GroupDao;
import ciknow.dao.JobDao;
import ciknow.dao.NodeDao;
import ciknow.dao.PageDao;
import ciknow.dao.QuestionDao;
import ciknow.dao.RoleDao;
import ciknow.dao.SurveyDao;
import ciknow.dao.VisualizationDao;
import ciknow.domain.Edge;
import ciknow.domain.Field;
import ciknow.domain.Group;
import ciknow.domain.Job;
import ciknow.domain.Node;
import ciknow.domain.Page;
import ciknow.domain.Question;
import ciknow.domain.Scale;
import ciknow.domain.Survey;
import ciknow.domain.TextField;
import ciknow.domain.Visualization;
import ciknow.dto.JobDTO;
import ciknow.dto.NodeDTO;
import ciknow.dto.VisualizationDTO;
import ciknow.jms.producer.Messenger;
import ciknow.mahout.cf.data.CIKNOW2MAHOUT;
import ciknow.mail.Mailer;
import ciknow.service.ActivityService;
import ciknow.teamassembly.Team;
import ciknow.teamassembly.TeamBuilder;
import ciknow.util.Beans;
import ciknow.util.Constants;
import ciknow.util.GeneralUtil;
import ciknow.util.VisUtil;

/**
 * User: gyao
 * Date: Apr 8, 2008
 * Time: 9:28:29 PM
 */
public class GenericRO {
    private static Log logger = LogFactory.getLog(GenericRO.class);
    
    private String baseURL = "";
    private String realPath = "";
    private Map<Long, String> errorMsgMap = new HashMap<Long, String>();
    
    // generic data caching?
    @SuppressWarnings("unchecked")
	private Map map = new HashMap();
    
    private NodeDao nodeDao;
    private EdgeDao edgeDao;
    private GroupDao groupDao;
    private RoleDao roleDao;
    private SurveyDao surveyDao;
    private QuestionDao questionDao;
    private JobDao jobDao;
    private PageDao pageDao;
    
    private NodeRO nodeRO;
    private GroupRO groupRO;
    private RoleRO roleRO;
    private SurveyRO surveyRO;
    private QuestionRO questionRO;
    private EdgeRO edgeRO;
	
    private ActivityService activityService;
    
    public GenericRO(){
    	
    }

    
	@SuppressWarnings("unchecked")
	public Map init(Map req) throws Exception{
		logger.info("initialize ...");
		Map result = new HashMap();
		
		result.put("baseURL", baseURL);

		Node loginNode = null;
		String username = null;
		try{
			loginNode = nodeDao.loadById(nodeRO.getLoginNodeId());
			result.put("authenticated", "1");
		} catch (Exception e){
			logger.warn("Spring secuirity is turned off for public survey mode.");
			username = (String)req.get("username");
			logger.debug("username: " + username);
			if (username == null || username.trim().isEmpty()) throw  new Exception("Please specify username in URL");	
			username = username.trim();
			loginNode = nodeDao.loadByUsername(username);
			if (loginNode == null) throw new Exception("unrecoganized username: " + username);
			result.put("authenticated", "0");
		}
		result.put("loginNode", new NodeDTO(loginNode)); //TODO: mask the password?
		
		// whoever need nodes, load it by yourself in paginated manner. 
		// it is inefficient to load all nodes at once to client
		//result.put("nodes", nodeRO.getAllNodes());
		
		result.put("surveys", surveyRO.getAllSurveys());
		
		result.put("questions", questionRO.getQuestionsBySurveyId(1L));

		result.put("groups", groupRO.getAllGroups());
		
		result.put("roles", roleRO.getAllRoles());
		
		result.put("nodeTypeDescriptions", nodeRO.getNodeTypeDescriptions());
		result.put("edgeTypeDescriptions", edgeRO.getEdgeTypeDescriptions());
		
		return result;
	}
	
	/*
	@Transactional
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map initializeSurvey(Map req) throws Exception{
		logger.info("initialize survey...");
		Map result = new HashMap();
		
		// get all plain nodes
		Map<String, Map> allPlainNodes = nodeDao.getAllPlainNodes();		
		result.put("nodes", allPlainNodes.values());
		
		// attach properties/attributes to plain nodes for contact chooser question(s)
		List<Question> questions = questionDao.getAll();
		Set<String> fieldNames = populatePlainNodes(allPlainNodes, questions);
		
		// get distinct properties values 
		Map<String, List<String>> distinctFieldValuesMap = new HashMap<String, List<String>>();
		for (String fieldName : fieldNames){
			if (fieldName.startsWith("Q" + Constants.SEPERATOR)) continue;
			List<Map<String, String>> list = getPropertyValues(fieldName);
			List<String> fieldValues = new ArrayList<String>();
			for (Map<String, String> listItem : list){
				fieldValues.add(listItem.get("value"));
			}
			distinctFieldValuesMap.put(fieldName, fieldValues);
		}
		result.put("distinctFieldValuesMap", distinctFieldValuesMap);
		
		
		String nodeId = (String)req.get("nodeId");			
		Node loginNode = nodeDao.findById(Long.parseLong(nodeId));
		List<Edge> tempEdges;				
		// outgoing edges
		tempEdges = edgeDao.loadByFromNodeId(loginNode.getId());
		List<EdgeDTO> outgoingEdges = new ArrayList<EdgeDTO>();
		for (Edge edge : tempEdges){
			if (edge.getCreator() == null) outgoingEdges.add(new EdgeDTO(edge));
			else { logger.debug("Ignore edge id=" + edge.getId()); }
		}
		// created edges
		tempEdges = edgeDao.loadByCreatorId(loginNode.getId());
		List<EdgeDTO> perceivedEdges = new ArrayList<EdgeDTO>();
		for (Edge edge : tempEdges){
			perceivedEdges.add(new EdgeDTO(edge));
		}
		
		// tags
		List<Node> tags = nodeDao.findByType("tag");
		List<NodeDTO> tagDtos = new ArrayList<NodeDTO>();
		for (Node tag : tags){
			NodeDTO tagDto = new NodeDTO();
			tagDto.shallowCopy(tag);
			tagDtos.add(tagDto);
		}
		
		// create private groups if needed
		List<GroupDTO> newPrivateGroups = new ArrayList<GroupDTO>();
		for (Question question : questions){
			if (question.isContactChooser()){
				String groupName = Group.PRIVATE_PREFIX + loginNode.getUsername() + "_" + question.getShortName();
				Group pg = groupDao.findByName(groupName);
				if (pg == null){
					pg = new Group();
					pg.setName(groupName);
					groupDao.save(pg);
					newPrivateGroups.add(new GroupDTO(pg));
				}
			}
		}
		
		// groupMap
		List<Group> groups = groupDao.getAll();
		Map groupMap = new HashMap();
		for (Group group : groups){
			List<Long> nodeIds = groupDao.getNodeIdsByGroupId(group.getId());
			groupMap.put(group.getId().toString(), nodeIds);
		}
		// hidden nodes
		List<Long> hiddenNodeIds = roleDao.getNodeIdsByRoleId(2L);
		
		result.put("outgoingEdges", outgoingEdges);
		result.put("perceivedEdges", perceivedEdges);
		result.put("tags", tagDtos);
		result.put("newPrivateGroups", newPrivateGroups);
		result.put("groupMap", groupMap);
		result.put("hiddenNodeIds", hiddenNodeIds);
		
		return result;
	}
	*/
	/*
	private Set<String> populatePlainNodes(Map<String, Map> allPlainNodes, List<Question> questions){
		logger.info("Populating plain nodes...");
		
		Map<String, Question> questionMap = new HashMap<String, Question>();
		for (Question question : questions){
			questionMap.put(question.getShortName(), question);
		}
		
		// get all contact chooser questions' availableGroups and custom AdvancedDataGridColumns		
		Set<Group> availableGroupSet = new HashSet<Group>();
		Set<String> fieldNames = new HashSet<String>();
		for (Question question : questions){			
			if (question.isContactChooser()){
				availableGroupSet.addAll(question.getAvailableGroups());
				String ccLevel = question.getAttribute(Constants.CC_LEVEL);
				if (ccLevel != null && !ccLevel.isEmpty()){
					String[] parts = ccLevel.split(",");
					for (String part : parts){
						String[] subparts = part.split("=");
						String fieldName = subparts[0];
						
						// screening out ineligible fieldName
						if (fieldName.equals("label") || fieldName.equals("type")) continue;	// already available in plain nodes
						if (fieldName.startsWith("Q" + Constants.SEPERATOR)){ 					// choice (single) question 
							String shortName = fieldName.substring(2);
							Question q = questionMap.get(shortName);
							if (q == null || !q.isSingleChoice()) {
								logger.warn("Unsupported question: " + shortName);
								continue;
							}
						}
						
						fieldNames.add(fieldName);
					}
				}
			}
		}
		
		// If custom columns data is requested
		if (!fieldNames.isEmpty()){
			Set<Long> availableNodeIdSet = new HashSet<Long>();
			for (Group group : availableGroupSet){
				List<Long> nodeIds = groupDao.getNodeIdsByGroupId(group.getId());
				availableNodeIdSet.addAll(nodeIds);
			}
			List<Node> availableNodes = nodeDao.loadByIds(availableNodeIdSet);
						
			for (Node node : availableNodes){
				Map m = GeneralUtil.flattenNode(node, fieldNames, questionMap);
				Map plainNode = allPlainNodes.get(node.getId().toString());
				plainNode.putAll(m);
			}
		}
		
		return fieldNames;
	}
	*/
	
	/*
	public Map refreshSurvey(Map req) throws Exception{
		logger.debug("Refreshing data...");
		Map result = new HashMap();
		result.putAll(init(req));
		result.putAll(initializeSurvey(req));
		return result;
	}
	
	
	@Transactional
	public Map saveQuestionAnswers(Map input) throws Exception{
		QuestionDTO questiondto = (QuestionDTO)input.get("currentQuestion");
		logger.info("Saving question answers for: " + questiondto.label);
		
		Map output = new HashMap();
		
		// update loginNode
		NodeDTO nodedto = (NodeDTO)input.get("loginNode");
		nodedto = nodeRO.saveNode(nodedto);
		output.put("loginNode", nodedto);

		// create NEW edges
		List<EdgeDTO> edges = (List<EdgeDTO>) input.get("edges");
		if (edges != null && edges.size() > 0){
			if (edges != null && !edges.isEmpty()) {
				edges = edgeRO.saveEdges(edges);
				output.put("edges", edges);
			}		
			logger.debug("create " + edges.size() + " new edges.");
		}
		
		// delete OLD edges
		List<String> ids = (List<String>)input.get("oldEdgeIds");
		if (ids != null && ids.size() > 0){
			List<Long> oldEdgeIds = new ArrayList<Long>();
			for (String id : ids){
				oldEdgeIds.add(Long.parseLong(id));
			}
			edgeDao.delete(edgeDao.findByIds(oldEdgeIds));
			logger.debug("remove " + oldEdgeIds.size() + " old edges.");
		}		
		
		// for contact chooser question
		String privateGroupIdString = (String)input.get("privateGroupId");
		if (privateGroupIdString != null){
			Long privateGroupId = Long.parseLong(privateGroupIdString);
			Group group = groupDao.findById(privateGroupId);
			
			List<String> oldSelectedContactIds = (List<String>) input.get("oldSelectedContactIds");
			List<Long> oldIds = GeneralUtil.StringListToLongList(oldSelectedContactIds);
			List<String> newSelectedContactIds = (List<String>) input.get("newSelectedContactIds");
			List<Long> newIds = GeneralUtil.StringListToLongList(newSelectedContactIds);
			
			// determine nodes to add/remove from group
			List<Long> removeNodeIds;
			List<Long> addNodeIds = new ArrayList<Long>();
			for (Long id : newIds){
				if (oldIds.contains(id)) oldIds.remove(id);
				else addNodeIds.add(id);
			}
			removeNodeIds = oldIds;
			
			
			List<Node> nodes = new ArrayList<Node>();
			Node node;
			// remove nodes from group
			for (Long id : removeNodeIds){
				node = nodeDao.loadById(id);
	    		if (node == null) {    			
	    			logger.warn("Node(id=" + id + ") does not exist.");
	    			continue;
	    		}
	    		node.getGroups().remove(group);
	    		nodes.add(node);
	    		logger.debug(node.getLabel() + " removed.");
			}
			logger.debug(removeNodeIds.size() + " nodes removed from group: " + group.getName());
			
			// add nodes to group
			for (Long id : addNodeIds){
				node = nodeDao.loadById(id);
	    		if (node == null) {    			
	    			logger.warn("Node(id=" + id + ") does not exist.");
	    			continue;
	    		}
	    		node.getGroups().add(group);
	    		nodes.add(node);
	    		logger.debug(node.getLabel() + " added.");
			}
			logger.debug(addNodeIds.size() + " nodes added to group: " + group.getName());
			
			// save
			nodeDao.save(nodes);
		}
		
		return output;
	}	
	*/
	
	/*
    @SuppressWarnings("unchecked")
	public Map saveQuestionNode(Map input) throws Exception{
    	logger.info("saveQuestionNode");
    	
    	NodeDTO dto = (NodeDTO) input.get("node");
    	dto = nodeRO.saveNode(dto);
    	Map output = new HashMap();
    	output.put("node", dto);
    	
    	return output;
    }
    */
	
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public Map getUserStatusMap(Map data){
    	logger.info("get user status map...");
    	List nodeIds = (List)data.get("nodeIds");
    	List<Long> ids = new LinkedList<Long>();
    	for (Object o : nodeIds){
    		Long nodeId = Long.parseLong(o.toString());
    		ids.add(nodeId);
    	}
    	List<Node> nodes = nodeDao.findByIds(ids);
    	List<Page> pages = pageDao.getAll();
    	List<Map<String, String>> progressData = activityService.getProgress(nodes, pages);
    	
    	Map result = new HashMap();
    	List<Long> finishedIds = new LinkedList<Long>();
    	List<Long> completedIds = new LinkedList<Long>();
    	List<Long> notcompletedIds = new LinkedList<Long>();
    	List<Long> notStartedIds = new LinkedList<Long>();
    	result.put(Constants.NODE_PROGRESS_FINISHED, finishedIds);
    	result.put(Constants.NODE_PROGRESS_COMPLETED, completedIds);
    	result.put(Constants.NODE_PROGRESS_NOT_COMPLETED, notcompletedIds);;
    	result.put(Constants.NODE_PROGRESS_NOT_STARTED, notStartedIds);	
    	for (Map p : progressData){
    		Long nodeId = Long.parseLong((String)p.get("id")); 		
    		String status = (String) p.get("status");
    		if (status.equals(Constants.NODE_PROGRESS_FINISHED)){
    			finishedIds.add(nodeId);
    		} else if (status.equals(Constants.NODE_PROGRESS_COMPLETED)){
    			completedIds.add(nodeId);
    		} else if (status.equals(Constants.NODE_PROGRESS_NOT_COMPLETED)){
    			notcompletedIds.add(nodeId);
    		} else if (status.equals(Constants.NODE_PROGRESS_NOT_STARTED)){
    			notStartedIds.add(nodeId);
    		} else {
    			logger.warn("Node(id=" + nodeId + ") has illegal status: " + status);
    		}
    	}
    	
    	return result;
    }
    
    @SuppressWarnings({"rawtypes" })
	public List<Long> inviteUsers(Map data) throws Exception{
    	logger.info("inviting nodes...");
    	List nodeIds = (List)data.get("nodeIds");
    	String subject = (String)data.get("subject");
    	String fromEmail = (String)data.get("fromEmail");
    	logger.debug("nodeIds: " + nodeIds);
    	logger.debug("subject: " + subject);
    	logger.debug("fromEmail: " + fromEmail);
    	
    	List<Node> nodes = new LinkedList<Node>();
    	List<Long> invitedIds = new ArrayList<Long>();
    	Survey survey = surveyDao.findById(1L);
    	Mailer mailer = (Mailer) Beans.getBean("mailService");
    	int invalidCount = 0;
    	StringBuilder sb_invalidEmail = new StringBuilder();
    	int disabledCount = 0;
    	StringBuilder sb_disabled = new StringBuilder();    	
    	EmailValidator ev = EmailValidator.getInstance();
    	for (Object o : nodeIds){
    		Long nodeId = Long.parseLong(o.toString());
    		Node node = nodeDao.loadById(nodeId);    		
    		nodes.add(node);
    		
    		// check email
    		String email = node.getEmail();
    		if (email == null || email.equals("")) email = "N/A";
    		if (!ev.isValid(email)) {
    			sb_invalidEmail.append(">> ").append(node.getLabel()).append("(").append(email).append(")").append("\n");
    			invalidCount++;
    		}
    		
    		// check enable
    		if (!node.getEnabled()){
    			sb_disabled.append(">> ").append(node.getLabel()).append("\n");
    			disabledCount++;
    		}
    	}
    	
    	String msg = "";
    	if (invalidCount > 0){
    		msg += "\nThere are " + invalidCount + " invalid email(s):\n";
    		msg += sb_invalidEmail.toString();    		
    	}
    	if (disabledCount > 0){
    		msg += "\nThere are " + disabledCount + " disabled user(s):\n";
    		msg += sb_disabled.toString();
    	}
    	if (msg.length() > 0){
    		msg += "\nNO invitations have been sent. Please correct the problem(s) and try again.";
    		throw new Exception(msg);
    	}
    	
    	for (Node node : nodes){ 		
    		mailer.sendInvitations(node, survey, subject, fromEmail);
    		logger.debug("email to: " + node.getUsername());
    		invitedIds.add(node.getId());
    	}
    	
    	return invitedIds;
    }
    
	@SuppressWarnings("rawtypes")
	public List getProgress(Map data){
    	String groupId = (String)data.get("groupId");
    	List<Long> nodeIds = groupDao.getNodeIdsByGroupId(Long.parseLong(groupId));
    	List<Node> nodes = nodeDao.findByIds(nodeIds);
    	List<Page> pages = pageDao.getAll();
    	List<Map<String, String>> progressData = activityService.getProgress(nodes, pages);
    	return progressData;
    }
    
    /*
	public List<Map> getProgressByNodeIds(Collection<Long> nodeIds){
    	List<Node> nodes = nodeDao.loadByIds(nodeIds);
    	return getProgressByNodes(nodes);
    }  

	public List<Map> getProgressByNodes(Collection<Node> nodes){
    	List<Question> questions = questionDao.getAll();
    	Map<String, Question> questionMap = new HashMap<String, Question>();
    	for (Question question : questions){
    		questionMap.put(question.getShortName(), question);
    	}
    	int numQuestion = questions.size();
    	
    	List<Map> progressData = new LinkedList<Map>();
    	for (Node node : nodes){
    		Map np = new HashMap();
    		np.put("id", node.getId().toString());
    		np.put("username", node.getUsername());
    		np.put("label", node.getLabel());
    		np.put("firstName", node.getFirstName().isEmpty()?"-":node.getFirstName());
    		np.put("lastName", node.getLastName().isEmpty()?"-":node.getLastName());
    		np.put("phone", node.getPhone().isEmpty()?"-":node.getPhone());
    		np.put("email", node.getEmail().isEmpty()?"-":node.getEmail());
    		
    		String ts_start = node.getAttribute(Constants.NODE_TS_START_SURVEY);
    		String ts_finish = node.getAttribute(Constants.NODE_TS_FINISH_SURVEY);
    		String maxShortName = node.getAttribute(Constants.NODE_MAX_ANSWERED_QUESTION_NAME);
    		String lastShortName= node.getAttribute(Constants.NODE_LAST_ANSWERED_QUESTION_NAME);
    		String lastTime = node.getAttribute(Constants.NODE_LAST_ANSWERED_QUESTION_TIME);
    		Question maxQuestion = questionMap.get(maxShortName);
    		Question lastQuestion = questionMap.get(lastShortName);
    		
    		if (ts_finish != null){
    			np.put("status", Constants.NODE_PROGRESS_FINISHED);    			
    			np.put("progress", numQuestion + "/" + numQuestion);
    			np.put("completeTime", ts_finish);
    		} else if (ts_start != null){
    			if (maxQuestion == null) {
    				logger.warn("Question(shortName=" + maxShortName + ") has been deleted.");
    				np.put("status", Constants.NODE_PROGRESS_NOT_COMPLETED);
    			} else if (maxQuestion.getIndex() < numQuestion-1){
    				np.put("status", Constants.NODE_PROGRESS_NOT_COMPLETED);
    			} else {
    				np.put("status", Constants.NODE_PROGRESS_COMPLETED);
    			}
    			
    			np.put("progress", (maxQuestion == null?"-1":(maxQuestion.getIndex() + 1)) + "/" + numQuestion);
    			np.put("completeTime", "-"); 			
    		} else {
    			if (maxShortName == null){
	    			np.put("status", Constants.NODE_PROGRESS_NOT_STARTED);
	    			np.put("progress", "0/" + numQuestion);
	    			np.put("completeTime", "-"); 
    			} else { // this situation should not happen, just for handling legacy data
    				if (maxQuestion.getIndex() < numQuestion-1){
        				np.put("status", Constants.NODE_PROGRESS_NOT_COMPLETED);
        			} else {
        				np.put("status", Constants.NODE_PROGRESS_COMPLETED);
        			} 			
	    			np.put("progress", (maxQuestion == null?"-1":(maxQuestion.getIndex() + 1)) + "/" + numQuestion);
	    			np.put("completeTime", "-"); 
    			}
    		}
			np.put("maxQuestion", maxQuestion == null?"-":maxQuestion.getLabel());
			np.put("lastQuestion", lastQuestion == null?"-":lastQuestion.getLabel());
			np.put("lastTime", lastTime==null?"-":lastTime);
			
			progressData.add(np);
    	}
    	
    	return progressData;
    }  
	*/
	
    @SuppressWarnings("unchecked")
	public Map getVisColors(){
    	logger.info("get vis colors...");
    	return GeneralUtil.getColors();
    }
    
    public void saveVisColors(Map<String, String> colorMap){
    	logger.info("save vis colors...");
    	GeneralUtil.saveColors(colorMap);
    	logger.info("vis colors saved.");
    }
    
    /*
     * These attributes can be display in visualization (instead of node label only)
     */
    @SuppressWarnings("unchecked")
	public List<Map<String, String>> getDisplayableAttributes(Map input){
    	List<String> nodeTypes = (List<String>) input.get("nodeTypes");    	
    	List<Map<String, String>> attrs = new LinkedList<Map<String, String>>();
    	Map m = new HashMap<String, String>();
    	m.put("name", "NODE_LABEL");
    	m.put("label", "NODE_LABEL");
    	
    	attrs.add(m);
    	
    	for (String attr : nodeDao.getAttributeNames(nodeTypes)){
    		if (attr.indexOf(Constants.SEPERATOR) >= 0) continue;
    		if (attr.startsWith("NODE_")) continue;
    		
    		m = new HashMap<String, String>();
    		m.put("name", attr);
    		m.put("label", attr);
    		attrs.add(m);
    	}
    	return attrs;
    }
    
    @SuppressWarnings("unchecked")
	public List<Map<String, String>> getColorableAttributes(Map input){
    	logger.info("get colorable attributes by node types...");
    	List<String> nodeTypes = (List<String>) input.get("nodeTypes");
    	Long loginNodeId = Long.parseLong((String)input.get("loginNodeId"));
    	Node loginNode = nodeDao.loadById(loginNodeId);
    	
    	List<Question> questions = questionDao.getAll();
    	Map<String, Question> questionMap = new HashMap<String, Question>();
    	for (Question q : questions){
    		questionMap.put(q.getShortName(), q);
    	}
    	
    	List<Map<String, String>> attrs = new LinkedList<Map<String, String>>();
    	List<String> attrNames = nodeDao.getAttributeNames(nodeTypes);
    	String attrLabel;
    	Set<Long> singleChoiceQuestionIds = new HashSet<Long>();
    	for (String attrName : attrNames){
    		if (attrName.startsWith("T" + Constants.SEPERATOR)) continue;
    		if (attrName.startsWith("CF" + Constants.SEPERATOR)) continue;
    		else if (attrName.startsWith("NODE_")) continue;
    		
    		Map<String, String> m = new HashMap<String, String>();
    		if (attrName.startsWith("F" + Constants.SEPERATOR)){
    			String shortName = Question.getShortNameFromKey(attrName);
    			Question question = questionMap.get(shortName);
    			if (question == null || (question.isHidden() && !loginNode.isAdmin())) continue;
    			
    			if (question.isRating()){
    				String fieldName = Question.getFieldNameFromKey(attrName);
    				Field field = question.getFieldByName(fieldName);
    				if (field == null) continue;
    				//attrLabel = shortName + ":" + field.getLabel();
    				attrLabel = question.getLabel() + ":" + field.getLabel();
    				m.put("name", VisUtil.ATTR_PREFIX + attrName);
        			m.put("label", attrLabel);
        			attrs.add(m);
    			} else if (question.isChoice() && question.isSingleChoice()){
    				Long qid = question.getId();
    				if (singleChoiceQuestionIds.contains(qid)) continue;
    				singleChoiceQuestionIds.add(qid);
    				m.put("name", VisUtil.QUESTION_PREFIX + qid);
        			m.put("label", question.getLabel());
        			attrs.add(m);
    			}
    		} else if (attrName.startsWith("FT" + Constants.SEPERATOR)){
    			String shortName = Question.getShortNameFromKey(attrName);
    			Question question = questionMap.get(shortName);
    			if (question == null || (question.isHidden() && !loginNode.isAdmin())) continue;
    			
    			if (question.isMultipleRating()){    				
    				String fieldName = Question.getFieldNameFromKey(attrName);
    				Field field = question.getFieldByName(fieldName);
    				if (field == null) continue;
    				String tfName = Question.getTextFieldNameFromFT(attrName);
    				TextField tf = question.getTextFieldByName(tfName);  
    				if (tf == null) continue;
    				//attrLabel = shortName + ":" + field.getLabel() + ":" + tf.getLabel();
    				attrLabel = question.getLabel() + ":" + tf.getLabel() + ":" + field.getLabel();
    				m.put("name", VisUtil.ATTR_PREFIX + attrName);
        			m.put("label", attrLabel);
        			attrs.add(m);
    			}
    		} else {
				attrLabel = attrName;
				m.put("name", VisUtil.ATTR_PREFIX + attrName);
    			m.put("label", attrLabel);
    			attrs.add(m);
    		}
    	}
    	
    	logger.debug("got " + attrs.size() + " colorable attributes.");
    	return attrs;
    }
    
    @SuppressWarnings("unchecked")
	public List<Map<String, String>> getColorableAttributesByEdgeTypes(Map input){
    	logger.info("get colorable attributes by edge types...");
    	List<String> edgeTypes = (List<String>) input.get("edgeTypes");
    	String loginNodeId = (String)input.get("loginNodeId");
    	
    	List<String> nodeTypes = new ArrayList<String>(edgeDao.getNodeTypesByEdgeTypes(edgeTypes));
    	Map temp = new HashMap();
    	temp.put("nodeTypes", nodeTypes);
    	temp.put("loginNodeId", loginNodeId);
    	
    	return getColorableAttributes(temp);    	
    }
    
    /**
     * This is different from getColorableAttributes() by allow multiple choice question
     * @return
     */
    @SuppressWarnings("unchecked")
	public List<Map<String, String>> getFilterableAttributes(Map input){
    	logger.info("get filterable attributes...");
    	List<String> nodeTypes = (List<String>) input.get("nodeTypes");
    	Long loginNodeId = Long.parseLong((String)input.get("loginNodeId"));
    	Node loginNode = nodeDao.loadById(loginNodeId);
    	
    	List<Question> questions = questionDao.getAll();
    	Map<String, Question> questionMap = new HashMap<String, Question>();
    	for (Question q : questions){
    		questionMap.put(q.getShortName(), q);
    	}
    	
    	List<Map<String, String>> attrs = new LinkedList<Map<String, String>>();
    	List<String> attrNames = nodeDao.getAttributeNames(nodeTypes);
    	String attrLabel;
    	Set<Long> singleChoiceQuestionIds = new HashSet<Long>();
    	for (String attrName : attrNames){
    		if (attrName.startsWith("T" + Constants.SEPERATOR)) continue;
    		if (attrName.startsWith("CF" + Constants.SEPERATOR)) continue;
    		else if (attrName.startsWith("NODE_")) continue;
    		
    		Map<String, String> m = new HashMap<String, String>();
    		if (attrName.startsWith("F" + Constants.SEPERATOR)){
    			String shortName = Question.getShortNameFromKey(attrName);
    			Question question = questionMap.get(shortName);
    			if (question == null || (question.isHidden() && !loginNode.isAdmin())) continue;
    			
    			if (question.isRating() || question.isContinuous()){
    				String fieldName = Question.getFieldNameFromKey(attrName);
    				Field field = question.getFieldByName(fieldName);
    				if (field == null) continue;
    				//attrLabel = shortName + ":" + field.getLabel();
    				attrLabel = question.getLabel() + ":" + field.getLabel();
    				m.put("name", VisUtil.ATTR_PREFIX + attrName);
        			m.put("label", attrLabel);
        			attrs.add(m);
    			} else if (question.isChoice()){
    				Long qid = question.getId();
    				if (singleChoiceQuestionIds.contains(qid)) continue;
    				singleChoiceQuestionIds.add(qid);
    				m.put("name", VisUtil.QUESTION_PREFIX + qid);
        			m.put("label", question.getLabel());
        			attrs.add(m);
    			}
    		} else if (attrName.startsWith("FT" + Constants.SEPERATOR)){
    			String shortName = Question.getShortNameFromKey(attrName);
    			Question question = questionMap.get(shortName);
    			if (question == null || (question.isHidden() && !loginNode.isAdmin())) continue;
    			
    			if (question.isMultipleRating()){    				
    				String fieldName = Question.getFieldNameFromKey(attrName);
    				Field field = question.getFieldByName(fieldName);
    				if (field == null) continue;
    				String tfName = Question.getTextFieldNameFromFT(attrName);
    				TextField tf = question.getTextFieldByName(tfName);  
    				if (tf == null) continue;
    				//attrLabel = shortName + ":" + field.getLabel() + ":" + tf.getLabel();
    				attrLabel = question.getLabel() + ":" + tf.getLabel() + ":" + field.getLabel();
    				m.put("name", VisUtil.ATTR_PREFIX + attrName);
        			m.put("label", attrLabel);
        			attrs.add(m);
    			}
    		} else {
				attrLabel = attrName;
				m.put("name", VisUtil.ATTR_PREFIX + attrName);
    			m.put("label", attrLabel);
    			attrs.add(m);
    		}
    	}
    	return attrs;
    }
    
    /**
     * Find numeric attribute names for nodes of given types; Get all names if nodeTypes is null or empty
     * This is the same as colorable attributes (for now, but may change later)
     * @return
     */
    @SuppressWarnings("unchecked")
	public List<Map<String, String>> getNumericAttributes(Map input){
    	logger.info("get numeric attributes...");
    	List<String> nodeTypes = (List<String>) input.get("nodeTypes");
    	Long loginNodeId = Long.parseLong((String)input.get("loginNodeId"));
    	Node loginNode = nodeDao.loadById(loginNodeId);
    	
    	List<Question> questions = questionDao.getAll();
    	Map<String, Question> questionMap = new HashMap<String, Question>();
    	for (Question q : questions){
    		questionMap.put(q.getShortName(), q);
    	}
    	
    	List<Map<String, String>> attrs = new LinkedList<Map<String, String>>();
    	List<String> attrNames = nodeDao.getAttributeNames(nodeTypes);
    	String attrLabel;
    	Set<Long> singleChoiceQuestionIds = new HashSet<Long>();
    	for (String attrName : attrNames){
    		if (attrName.startsWith("T" + Constants.SEPERATOR)) continue;
    		if (attrName.startsWith("CF" + Constants.SEPERATOR)) continue;
    		else if (attrName.startsWith("NODE_")) continue;
    		
    		Map<String, String> m = new HashMap<String, String>();
    		if (attrName.startsWith("F" + Constants.SEPERATOR)){
    			String shortName = Question.getShortNameFromKey(attrName);
    			Question question = questionMap.get(shortName);
    			if (question == null || (question.isHidden() && !loginNode.isAdmin())) continue;
    			    			
    			if (question.isRating()){
    				String fieldName = Question.getFieldNameFromKey(attrName);
    				Field field = question.getFieldByName(fieldName);
    				if (field == null) continue;
    				//attrLabel = shortName + ":" + field.getLabel();
    				attrLabel = question.getLabel() + ":" + field.getLabel();
    				m.put("name", VisUtil.ATTR_PREFIX + attrName);
        			m.put("label", attrLabel);
        			attrs.add(m);
    			} else if (question.isChoice() && question.isSingleChoice()){
    				Long qid = question.getId();
    				if (singleChoiceQuestionIds.contains(qid)) continue;
    				singleChoiceQuestionIds.add(qid);
    				m.put("name", VisUtil.QUESTION_PREFIX + qid);
        			m.put("label", question.getLabel());
        			attrs.add(m);
    			}
    		} else if (attrName.startsWith("FT" + Constants.SEPERATOR)){
    			String shortName = Question.getShortNameFromKey(attrName);
    			Question question = questionMap.get(shortName);
    			if (question == null || (question.isHidden() && !loginNode.isAdmin())) continue;
    			
    			if (question.isMultipleRating()){    				
    				String fieldName = Question.getFieldNameFromKey(attrName);
    				Field field = question.getFieldByName(fieldName);
    				if (field == null) continue;
    				String tfName = Question.getTextFieldNameFromFT(attrName);
    				TextField tf = question.getTextFieldByName(tfName);  
    				if (tf == null) continue;
    				//attrLabel = shortName + ":" + field.getLabel() + ":" + tf.getLabel();
    				attrLabel = question.getLabel() + ":" + tf.getLabel() + ":" + field.getLabel();
    				m.put("name", VisUtil.ATTR_PREFIX + attrName);
        			m.put("label", attrLabel);
        			attrs.add(m);
    			}
    		} else {
				attrLabel = attrName;
				m.put("name", VisUtil.ATTR_PREFIX + attrName);
    			m.put("label", attrLabel);
    			attrs.add(m);
    		}
    	}
    	return attrs;
    }
    
    @SuppressWarnings("unchecked")
	public List<Map<String, String>> getNumericAttributesForSocialInference(Map input){
    	List<String> nodeTypes = (List<String>) input.get("nodeTypes");
    	int direction = Integer.parseInt((String)input.get("direction"));
    	List<String> edgeTypes = (List<String>) input.get("edgeTypes");
    	String loginNodeId = (String)input.get("loginNodeId");
    	
    	Collection<String> otherNodeTypes = edgeDao.getOtherNodeTypesByNodeTypes(nodeTypes, direction);
    	if (edgeTypes != null && !edgeTypes.isEmpty()){
    		Collection<String> nodeTypesAmongEdgeTypes = edgeDao.getNodeTypesByEdgeTypes(edgeTypes);
    		otherNodeTypes.retainAll(nodeTypesAmongEdgeTypes);
    	}
    	
    	Map temp = new HashMap();
    	temp.put("nodeTypes", new ArrayList<String>(otherNodeTypes));
    	temp.put("loginNodeId", loginNodeId);
    	return getNumericAttributes(temp);
    }
    
    /**
     * @param name attribute name, or "QUESTION:{id}", or "ATTR:{F`...}
     * @return
     */
    public List<Map<String, String>> getAttributeValues(String name){
    	logger.info("get attribute values for key=" + name);
    	List<Question> questions = questionDao.getAll();
    	Map<String, Question> questionMap = new HashMap<String, Question>();
    	for (Question q : questions){
    		questionMap.put(q.getShortName(), q);
    	}
    	
    	String prefix = null;
    	if (name.indexOf(VisUtil.ATTR_PREFIX) == 0) prefix = VisUtil.ATTR_PREFIX;
    	if (name.indexOf(VisUtil.QUESTION_PREFIX) == 0) prefix = VisUtil.QUESTION_PREFIX;
    	
    	
    	List<Map<String, String>> values = new LinkedList<Map<String, String>>();
    	
    	if (prefix.equals(VisUtil.QUESTION_PREFIX)){
    		String qid = name.substring(prefix.length());
    		Question q = questionDao.findById(Long.parseLong(qid));
    		for (Field f : q.getFields()){
    			Map<String, String> m = new HashMap<String, String>();
    			m.put("value", q.makeFieldKey(f));
    			m.put("label", f.getLabel());
    			values.add(m);
    		}
    	} else {
    		if (prefix.equals(VisUtil.ATTR_PREFIX)) name = name.substring(prefix.length());
    		
	    	List<String> attrValues = nodeDao.getAttributeValues(name);
	    	String attrLabel;
	    	for (String attrValue : attrValues){
	    		Map<String, String> m = new HashMap<String, String>();
	    		if (attrValue.startsWith("S" + Constants.SEPERATOR)){
	    			String shortName = Question.getShortNameFromKey(attrValue);
	    			Question question = questionMap.get(shortName);
	    			if (question == null) continue;
	    			
	    			if (question.getType().equals(Constants.RATING) || question.getType().equals(Constants.MULTIPLE_RATING)){
	    				String scaleName = Question.getScaleNameFromKey(attrValue);
	    				Scale scale = question.getScaleByName(scaleName);
	    				if (scale == null) continue;
	    				//attrLabel = shortName + ":" + scale.getLabel();
	    				attrLabel = question.getLabel() + ":" + scale.getLabel();
	    				m.put("value", attrValue);
	        			m.put("label", attrLabel);
	        			values.add(m);
	    			}
	    		} else {
					attrLabel = attrValue;
					m.put("value", attrValue);
	    			m.put("label", attrLabel);
	    			values.add(m);
	    		}
	    	}
    	}
    	return values;
    }
    
    public List<Map<String, String>> getPropertyValues(String name){
    	logger.info("get property values by " + name);
    	List<Map<String, String>> ntds = null;
    	if (name.equals("type")){
    		ntds = GeneralUtil.getNodeDescriptions();
    	}
    	List<Map<String, String>> values = new ArrayList<Map<String, String>>();
    	List<String> pValues = nodeDao.getPropertyValues(name);
    	for (String pValue : pValues){
    		Map<String, String> value = new HashMap<String, String>();
    		value.put("value", pValue);
    		if (name.equals("type")){
    			value.put("label", GeneralUtil.getNodeTypeLabel(ntds, pValue));
    		} else value.put("label", pValue);
    		values.add(value);
    	}
    	return values;
    }
    
    public List<Map<String, String>> getEdgeColorableAttributes(){
    	logger.info("get edge colorable attributes...");    	
    	List<Map<String, String>> attrs = new LinkedList<Map<String, String>>();
    	List<String> attrNames = edgeDao.getAttributeNames();
    	String attrLabel;
    	for (String attrName : attrNames){
			Map<String, String> m = new HashMap<String, String>();
			attrLabel = attrName;
			m.put("name", attrName);
			m.put("label", attrLabel);
			attrs.add(m);
    	}
    	return attrs;
    }
    
    public List<Map<String, String>> getEdgeAttributeValues(String name){
    	logger.info("get edge attribute values for key=" + name);
    	List<Question> questions = questionDao.getAll();
    	Map<String, Question> questionMap = new HashMap<String, Question>();
    	for (Question q : questions){
    		questionMap.put(q.getShortName(), q);
    	}
    	
    	List<Map<String, String>> values = new LinkedList<Map<String, String>>();
    	List<String> attrValues = edgeDao.getAttributeValues(name);
    	String attrLabel;
    	for (String attrValue : attrValues){
    		Map<String, String> m = new HashMap<String, String>();
    		if (attrValue.startsWith("S" + Constants.SEPERATOR)){
    			String shortName = Question.getShortNameFromKey(attrValue);
    			Question question = questionMap.get(shortName);
    			if (question == null) continue;
    			
    			if (question.getType().equals(Constants.RELATIONAL_RATING) 
    				|| question.getType().equals(Constants.RELATIONAL_RATING_MULTIPLE)){
    				String scaleName = Question.getScaleNameFromKey(attrValue);
    				Scale scale = question.getScaleByName(scaleName);
    				if (scale == null) continue;
    				//attrLabel = shortName + ":" + scale.getLabel();
    				attrLabel = question.getLabel() + ":" + scale.getLabel();
    				m.put("value", attrValue);
        			m.put("label", attrLabel);
        			values.add(m);
    			}
    		} else {
				attrLabel = attrValue;
				m.put("value", attrValue);
    			m.put("label", attrLabel);
    			values.add(m);
    		}
    	}
    	return values;
    }
    
    @SuppressWarnings("unchecked")
	public Map getLargeNetworkLimits() throws DocumentException, IOException{
    	return GeneralUtil.getLargeNetworkLimits();
    }
    
    @SuppressWarnings("unchecked")
	public void setLargeNetworkLimits(Map map) throws DocumentException, IOException{
    	GeneralUtil.setLargeNetworkLimits(map);
    }
    
    @SuppressWarnings("unchecked")
	public Map getTeamAssemblySuggestion(Map data) throws Exception{
    	logger.info("get team assembly suggestion...");
    	Map result = new HashMap();
    	List groupIds = (List)data.get("groupIds");
    	Integer numTeams = Integer.parseInt((String)data.get("numTeams"));
    	logger.debug("groupIds: " + groupIds);
    	logger.debug("numTeams: " + numTeams);
    	
    	List<Node> nodes = getNodes4TeamAssembly(groupIds);
    	
    	int total = nodes.size();
    	int min = new Double(Math.floor(total/numTeams.doubleValue())).intValue();
    	int max = new Double(Math.ceil(total/numTeams.doubleValue())).intValue();
    	result.put("total", total);
    	result.put("min", min);
    	result.put("max", max);
    	
    	logger.debug("total: " + total + ", min: " + min + ", max: " + max);
    	return result;
    }
    
    @SuppressWarnings("unchecked")
	public Map assembleTeam(Map data) throws Exception{
    	logger.info("assembling teams...");
    	int numTeams = Integer.parseInt((String)data.get("numTeams"));
    	int minTeamSize = Integer.parseInt((String)data.get("minTeamSize"));
    	int maxTeamSize = Integer.parseInt((String)data.get("maxTeamSize"));
    	int iterations = Integer.parseInt((String)data.get("iterations"));    	
    	String diversityQuestionShortName = (String)data.get("diversityQuestionShortName");    	
    	long similarityQuestionId = Long.parseLong((String)data.get("similarityQuestionId"));    	
    	List<String> edgeTypes = (List<String>) data.get("edgeTypes");    
    	List<String> groupIds = (List<String>) data.get("groupIds");
    	TeamBuilder tb = prepareTeamBuilder(numTeams, minTeamSize, maxTeamSize, iterations, diversityQuestionShortName, similarityQuestionId, groupIds, edgeTypes);
    	logger.debug(tb);
    	tb.build(iterations);    	    
    	
    	// return to client    	
    	NumberFormat nf = new DecimalFormat("0.00");
    	Map results = new HashMap();
    	results.put("teamsByMaxMinScore", getMapList(tb.getBestByMaxMinScore()));
    	results.put("maxMinScore", nf.format(tb.getMaxMinScore()));
    	results.put("bestItr4MaxMinScore", tb.getBestItr4MaxMinScore());
    	results.put("teamsByMinVariance", getMapList(tb.getBestByMinVariance()));
    	results.put("minVariance", nf.format(tb.getMinVariance()));
    	results.put("bestItr4MinVariance", tb.getBestItr4MinVariance());	    	

    	// extra for TeamWriter.java
    	results.put("numTeams", numTeams);
    	results.put("minTeamSize", minTeamSize);
    	results.put("maxTeamSize", maxTeamSize);
    	results.put("iterations", iterations);
    	results.put("diversityQuestionShortName", diversityQuestionShortName);
    	results.put("similarityQuestionId", similarityQuestionId);
    	results.put("edgeTypes", edgeTypes);
    	results.put("groupIds", groupIds);
    	
    	logger.debug("assembling teams done");
    	return results;
    }
    
    public TeamBuilder prepareTeamBuilder(int numTeams, int minTeamSize, int maxTeamSize, int iterations, String diversityQuestionShortName, long similarityQuestionId, List<String> groupIds, List<String> edgeTypes) throws Exception{    	    	
    	Question simQuestion = questionDao.findById(similarityQuestionId);
    	List<String> hobbies = (List<String>)simQuestion.getPossibleAttributeNames();
    	
    	List<Node> nodes = getNodes4TeamAssembly(groupIds);
    	Set<Long> nodeIds = new HashSet<Long>();
    	for (Node node : nodes){
    		nodeIds.add(node.getId());
    	}
    	
    	List<Edge> edges = new LinkedList<Edge>();
    	for (String edgeType : edgeTypes){
    		for (Edge edge : edgeDao.loadByType(edgeType, false)){
    			if (!nodeIds.contains(edge.getFromNode().getId()) || !nodeIds.contains(edge.getToNode().getId())){
    				continue;
    			}
    			edges.add(edge);
    		}
    	}
    	
    	TeamBuilder tb = new TeamBuilder(numTeams, minTeamSize, maxTeamSize, diversityQuestionShortName, hobbies, nodes, edges);
    	return tb;
    }
    
    private List<Node> getNodes4TeamAssembly(List<String> groupIds){
    	Set<Long> nodeIds = new HashSet<Long>();
    	for (String groupId : groupIds){
    		nodeIds.addAll(groupDao.getNodeIdsByGroupId(Long.parseLong(groupId)));
    	}
    	List<Node> nodes = nodeDao.loadByIds(nodeIds);
    	
    	logger.debug("removing hidden nodes...");
    	List<Node> hiddenNodes = new LinkedList<Node>();
    	for (Node node : nodes){
    		if (node.isHidden()) hiddenNodes.add(node); 
    	}
    	nodes.removeAll(hiddenNodes);
    	logger.debug("removed " + hiddenNodes.size() + " hidden nodes.");
    	return nodes;
    }
    
    @SuppressWarnings("unchecked")
	public Map getTeamAssemblyConfig(Map input) throws DocumentException, IOException{
    	String questionShortName = (String)input.get("questionShortName");
    	return GeneralUtil.getTeamAssemblyConfig(questionShortName);
    }
    
    @SuppressWarnings("unchecked")
	public void saveTeamAssemblyConfig(Map data) throws DocumentException, IOException{
    	GeneralUtil.saveTeamAssemblyConfig(data);
    }
    
    @Transactional
    @SuppressWarnings("unchecked")
	public void saveTeams4Vis(Map data) throws Exception{
    	List<Map> teams = (List<Map>)data.get("teams");
    	String name = (String)data.get("name");
    	
    	List<String> attrNames = nodeDao.getAttributeNames(null);
    	if (attrNames.contains(name)){
    		String msg = "attribute name '" + name + "' is already exist.";
    		logger.error(msg);
    		throw new Exception(msg);
    	}
    	
    	List<Node> nodes = new LinkedList<Node>();
    	for (Map m : teams){
    		Integer teamId = (Integer)m.get("id");
    		List<String> memberIds = (List<String>)m.get("members");
    		List<Long> ids = new LinkedList<Long>();
    		for (String memberId : memberIds){
    			ids.add(Long.parseLong(memberId));
    		}
    		for (Node node : nodeDao.loadByIds(ids)){
    			node.setAttribute(name, teamId.toString());
    			nodes.add(node);
    		}    		
    	}
    	nodeDao.save(nodes);
    }
    
    @SuppressWarnings("unchecked")
	private List<Map> getMapList(List<Team> teams){
    	List<Map> teamList = new LinkedList<Map>();
    	NumberFormat nf = new DecimalFormat("0.00");
    	for (Team team : teams){
    		Map map = new HashMap();
    		map.put("id", Integer.toString(team.getId()));
    		map.put("score", nf.format(team.getScore()));
    		map.put("diversity", nf.format(team.getDiversity()));
    		map.put("similarity", nf.format(team.getSimilarity()));
    		map.put("density", nf.format(team.getDensity()));
    		map.put("size", team.getSize());
    		List<String> memberIds = new LinkedList<String>();
    		for (Node node : team.getMembers()){
    			memberIds.add(Long.toString(node.getId()));
    		}
    		map.put("members", memberIds);
    		teamList.add(map);
    	}
    	return teamList;
    }
    
    public List<VisualizationDTO> getCreatedVisualization(String creatorId){
    	logger.info("get visualizations created by creatorId=" + creatorId);
    	
    	Beans.init();
    	VisualizationDao visDao = (VisualizationDao)Beans.getBean("visualizationDao");
    	Node creator = nodeDao.getProxy(Long.parseLong(creatorId));    	
    	List<Visualization> viss = visDao.findByCreator(creator);
    	
    	validateViss(viss);
    	
    	List<VisualizationDTO> dtos = new LinkedList<VisualizationDTO>();
    	for (Visualization vis : viss){
    		dtos.add(new VisualizationDTO(vis));
    	}
    	return dtos;
    }
    
    public List<VisualizationDTO> getVisibleVisualization(String nodeId){
    	logger.info("get visualizations visible to nodeId=" + nodeId);
    	List<VisualizationDTO> dtos = new LinkedList<VisualizationDTO>();
    	Beans.init();
    	VisualizationDao visDao = (VisualizationDao)Beans.getBean("visualizationDao");
    	Set<Long> visIds = visDao.getVisIdsByNodeId(Long.parseLong(nodeId));	
    	if (visIds == null || visIds.size() == 0) return dtos;
    	
    	List<Visualization> viss = visDao.findByIds(visIds);
    	
    	validateViss(viss);
    	
    	for (Visualization vis : viss){
    		if (vis.getCreator().getId().toString().equals(nodeId)) continue;
    		dtos.add(new VisualizationDTO(vis));
    	}
    	return dtos;
    }
    
    @SuppressWarnings("unchecked")
	public Map validateVis(Map input){
    	String visId = (String)input.get("visId");
		//String action = (String)input.get("action");
    	
    	VisualizationDao visDao = (VisualizationDao)Beans.getBean("visualizationDao");
    	Visualization vis = visDao.findById(Long.parseLong(visId));
    	
    	List<String> allAttrNames = nodeDao.getAttributeNames(null);
    	List<String> edgeTypes = edgeDao.getEdgeTypes(); 
    	List<Question> questions = questionDao.getAll();
    	Map<String, Question> nameToQuestionMap = new HashMap<String, Question>();
    	Map<Long, Question> idToQuestionMap = new HashMap<Long, Question>();    	
    	for (Question question : questions){
    		nameToQuestionMap.put(question.getShortName(), question);
    		idToQuestionMap.put(question.getId(), question);
    	}
    	
		String msg = vis.validate(edgeTypes, allAttrNames, nameToQuestionMap, idToQuestionMap);
		logger.debug(msg.length() == 0 ? "Valid" : msg);
		
		Map output = new HashMap();
		output.putAll(input);
		output.put("valid", msg.length() == 0?"1":"0");
		output.put("msg", msg);
		return output;
    }
    
    private void validateViss(List<Visualization> viss){
    	List<String> allAttrNames = nodeDao.getAttributeNames(null);
    	List<String> edgeTypes = edgeDao.getEdgeTypes(); 
    	List<Question> questions = questionDao.getAll();
    	Map<String, Question> nameToQuestionMap = new HashMap<String, Question>();
    	Map<Long, Question> idToQuestionMap = new HashMap<Long, Question>();    	
    	for (Question question : questions){
    		nameToQuestionMap.put(question.getShortName(), question);
    		idToQuestionMap.put(question.getId(), question);
    	}

    	for (Visualization vis : viss){
    		String msg = vis.validate(edgeTypes, allAttrNames, nameToQuestionMap, idToQuestionMap);
    		logger.debug(msg.length() == 0 ? "Valid" : msg);
    		vis.setValid(msg.length() == 0);
    	}
    }
    
    @Transactional
    public VisualizationDTO saveOrUpdateVisualization(VisualizationDTO dto) throws Exception{
    	Beans.init();
    	VisualizationDao visDao = (VisualizationDao)Beans.getBean("visualizationDao");   
    	List<Visualization> list = visDao.findByCreatorAndName(nodeDao.getProxy(dto.creatorId), dto.name);
    	Visualization vis;
    	if (dto.visId == 0) { 
    		logger.info("Saving new visualization.");
        	if (list.size() > 0){
        		throw new Exception("You have already saved a visualization named '" + dto.name + "'. " +
    			"Please assign a different name, or delete previously saved visualization first.");
        	}
        	vis = dto.toVisualization();
    		vis.setId(null);
    		vis.setTimestamp(new Date());    		
    	} else {
    		logger.info("Updating visualization:" + dto.name);
    		if (list == null || list.size()==0){
    			throw new Exception("Cannot find the visualization (name=" + dto.name + ") created by creatorId=" + dto.creatorId );
    		}
    		vis  = list.get(0);
    		vis.setLabel(dto.label);
    		if (vis.getType().equals("query")) vis.setData(dto.data); // only the "query" type of savedVis data can be updated
    		
    		Set<Group> groupSet = new HashSet<Group>();
    		for (Object groupId : dto.groups){
    			Group group = groupDao.findById(Long.parseLong(groupId.toString()));
    			groupSet.add(group);
    		}
    		vis.setGroups(groupSet);
    		
    		Set<Node> nodeSet = new HashSet<Node>();
    		for (Object nodeId : dto.nodes){
    			Node node = nodeDao.findById(Long.parseLong(nodeId.toString()));
    			nodeSet.add(node);
    		}
    		vis.setNodes(nodeSet);
    	}
    	visDao.save(vis);
    	return new VisualizationDTO(vis);
    }
    
    public void deleteVisualization(String visId){
    	logger.info("deleting visualization: visId=" + visId);
    	Beans.init();
    	VisualizationDao visDao = (VisualizationDao)Beans.getBean("visualizationDao");
    	Visualization vis = visDao.findById(Long.parseLong(visId));
    	visDao.delete(vis);
    }
    
    // only for testing of jms integration
    public void startMessaging() throws JMSException{
		Messenger m = (Messenger)Beans.getBean("messenger");
		m.send("it works!");
    }
    
    public String getErrorMsg(Long nodeId){
    	logger.debug("get error msg for nodeId=" + nodeId);
    	String msg = errorMsgMap.get(nodeId);
    	if (msg == null) return "Service temporarily unavailable";
    	else return msg;
    }
    
    public void setErrorMsg(String nodeId, String msg){
    	logger.debug("set error msg for nodeId: " + nodeId + ", msg: " + msg);
    	errorMsgMap.put(Long.parseLong(nodeId), msg);
    }
    
    public List<JobDTO> getScheduledJobs(){
    	List<Job> jobs = jobDao.getAll();
    	List<JobDTO> dtos = new LinkedList<JobDTO>();
    	for (Job job : jobs){
    		JobDTO dto = new JobDTO(job);
    		dtos.add(dto);
    	}
    	return dtos;
    }
    
    public void updateScheduledJob(JobDTO dto){
    	Job job = jobDao.getByName(dto.name);
    	job.setScheduledRuntime(dto.scheduledRuntime);
    	job.setDescription(dto.description);
    	job.setEnabled(dto.enabled);
    	jobDao.save(job);
    }
    
    public void deleteScheduledJob(String jobName){
    	Job job = jobDao.getByName(jobName);
    	jobDao.delete(job);
    }
    
    @SuppressWarnings("unchecked")
	public void generateMahoutPreferences(Map data) throws FileNotFoundException, TasteException{
    	String edgeType = (String)data.get("edgeType");
    	String direction = (String)data.get("direction");
    	CIKNOW2MAHOUT.export2file(edgeType, direction, getRealPath() + "WEB-INF/classes/ratings.txt");
    }
    
    public Map getMahoutRecommenderConfig() throws FileNotFoundException, IOException{
    	logger.info("get mahout recommender configuration...");	
    	
    	Properties props = new Properties();
    	props.load(new FileInputStream(new File(getRealPath() + "WEB-INF/classes/mahout.properties")));
    	
    	return props;
    }
    
    public void saveMahoutRecommenderConfig(Map data) throws FileNotFoundException, IOException{
    	logger.info("saving mahout recommender configuration...");
    	String recType = (String)data.get("recType");
    	String simType = (String)data.get("simType");
    	String neighborhoodType = (String)data.get("neighborhoodType");
    	String neighborhoodSize = (String)data.get("neighborhoodSize");
    	String neighborhoodThreshold = (String)data.get("neighborhoodThreshold");
    	logger.info("recType: " + recType);
    	logger.info("simType: " + simType);
    	logger.info("neighborhoodType: " + neighborhoodType);
    	logger.info("neighborhoodSize: " + neighborhoodSize);
    	logger.info("neighborhoodThreshold" + neighborhoodThreshold);    	
    	
    	Properties props = new Properties();
    	props.putAll(data);
    	props.store(new FileOutputStream(new File(getRealPath() + "WEB-INF/classes/mahout.properties")), "mahout recommender configuration");
    }
    
    public void updateMahoutRecommender() throws FileNotFoundException, IOException, TasteException{
    	logger.info("updating mahout recommender ...");
    	
    	logger.debug("reading configuration from mahout.properties ...");
    	Properties props = new Properties();
    	props.load(new FileInputStream(new File(getRealPath() + "WEB-INF/classes/mahout.properties")));
    	String recType = (String)props.get("recType");
    	String simType = (String)props.get("simType");
    	String neighborhoodType = (String)props.get("neighborhoodType");
    	String neighborhoodSize = (String)props.get("neighborhoodSize");
    	String neighborhoodThreshold = (String)props.get("neighborhoodThreshold");
    	logger.info("recType: " + recType);
    	logger.info("simType: " + simType);
    	logger.info("neighborhoodType: " + neighborhoodType);
    	logger.info("neighborhoodSize: " + neighborhoodSize);
    	logger.info("neighborhoodThreshold" + neighborhoodThreshold);  
    	
    	logger.debug("updating recommender in application context...");
    	DataModel dataModel = new FileDataModel(new File(getRealPath() + "WEB-INF/classes/ratings.txt"));
    	RecommenderBuilder builder = getRecommenderBuilder(recType, 
											    			simType,
															neighborhoodType, 
															neighborhoodSize, 
															neighborhoodThreshold);
    	Recommender recommender = builder.buildRecommender(dataModel);    	
    	ServletContext sc = Beans.getServletContext();
    	sc.setAttribute("mahoutRecommender", recommender);
    }
    
    @SuppressWarnings("unchecked")
	public List<Map> getMahoutRecommendations(Map data) throws FileNotFoundException, IOException, TasteException{
    	String userId = (String)data.get("userId");
    	String numRec = (String)data.get("numRec");
    	logger.info("get (" + numRec + ") recommendations from mahout recommender for userId: " + userId);
    	
    	ServletContext sc = Beans.getServletContext();
    	Recommender recommender = (Recommender)sc.getAttribute("mahoutRecommender");
    	if (recommender == null){
    		updateMahoutRecommender();
    	}
    	recommender = (Recommender)sc.getAttribute("mahoutRecommender");
    	
    	
    	List<RecommendedItem> items = recommender.recommend(Long.parseLong(userId), Integer.parseInt(numRec));
    	List<Long> ids = new ArrayList<Long>();
    	Map<Long, Float> valueMap = new HashMap<Long, Float>();
    	for (RecommendedItem item : items){
    		ids.add(item.getItemID());
    		valueMap.put(item.getItemID(), item.getValue());
    	}
    	List<Node> nodes = nodeDao.findByIds(ids);
    	List<Map> plainNodes = new ArrayList<Map>();
    	for (Node node : nodes){
    		Map m = new HashMap();
    		m.put("node_id", node.getId());
    		m.put("username", node.getUsername());
    		m.put("label", node.getLabel());
    		m.put("score", valueMap.get(node.getId()));
    		plainNodes.add(m);
    	}
    	
    	return plainNodes;
    }
    
    @SuppressWarnings("unchecked")
	public String evaluateMahoutRecommender(Map data) throws IOException, NumberFormatException, TasteException{
    	logger.info("evaluating mahout recommender...");
    	Map config = (Map)data.get("config");
    	String recType = (String)config.get("recType");
    	String simType = (String)config.get("simType");
    	String neighborhoodType = (String)config.get("neighborhoodType");
    	String neighborhoodSize = (String)config.get("neighborhoodSize");
    	String neighborhoodThreshold = (String)config.get("neighborhoodThreshold");
    	
		Map eval = (Map)data.get("evaluation");
    	final String evalType = (String)eval.get("evalType");
    	final String evalDiffType = (String)eval.get("evalDiffType");
    	final String trainingRatio = (String)eval.get("trainingRatio");
    	final String evaluationRatio = (String)eval.get("evaluationRatio");
    	final String at = (String)eval.get("at");
    	final String relevanceThreshold = (String)eval.get("relevanceThreshold");
    	final String irEvaluationRatio = (String)eval.get("irEvaluationRatio");
		
    	logger.info("Recommender Configuration +++++++++++++++++++++++");
    	logger.info("recType: " + recType);
    	logger.info("simType: " + simType);
    	logger.info("neighborhoodType: " + neighborhoodType);
    	logger.info("neighborhoodSize: " + neighborhoodSize);
    	logger.info("neighborhoodThreshold:" + neighborhoodThreshold);
    	
    	logger.info("Evaluation Configuration +++++++++++++++++++++++");
    	logger.info("evalType: " + evalType);
    	logger.info("evalDiffType: " + evalDiffType);
    	logger.info("trainingRatio: " + trainingRatio);
    	logger.info("evaluationRatio: " + evaluationRatio);
    	logger.info("at: " + at);
    	logger.info("relevanceThreshold: " + relevanceThreshold);
    	logger.info("irEvaluationRatio: " + irEvaluationRatio);
    	
    	if (recType.equals(Constants.MAHOUT_RECOMMENDER_TYPE_USER_BOOLEAN)){
    		if (!simType.equals(Constants.MAHOUT_SIMILARITY_LOG)) 
    			return Constants.MAHOUT_RECOMMENDER_TYPE_USER_BOOLEAN + " has to use " + Constants.MAHOUT_SIMILARITY_LOG;
    		if (evalType.equals(Constants.MAHOUT_EVALUATION_TYPE_DIFF)){
    			return Constants.MAHOUT_RECOMMENDER_TYPE_USER_BOOLEAN + " cannot use with " + Constants.MAHOUT_EVALUATION_TYPE_DIFF;
    		}
    	}
    	
    	DataModel dataModel = new FileDataModel(new File(getRealPath() + "WEB-INF/classes/ratings.txt"));
    	RecommenderBuilder builder = getRecommenderBuilder(recType, 
											    			simType,
															neighborhoodType, 
															neighborhoodSize, 
															neighborhoodThreshold);
    	
    	StringBuilder sb = new StringBuilder();
    	if (evalType.equals(Constants.MAHOUT_EVALUATION_TYPE_DIFF)){
    		RecommenderEvaluator evaluator = null;
    		if (evalDiffType.equals(Constants.MAHOUT_EVALUATOR_AVERAGE_ABSOLUTE_DIFF)){
    			evaluator = new AverageAbsoluteDifferenceRecommenderEvaluator();
    		} else if (evalDiffType.equals(Constants.MAHOUT_EVALUATOR_RMS)){
    			evaluator = new RMSRecommenderEvaluator();
    		} else {
    			logger.error("Evaluator not available: " + evalDiffType);
    			return "Evaluator not available: " + evalDiffType;
    		}
			double score = evaluator.evaluate(builder, null, dataModel, Double.parseDouble(trainingRatio), Double.parseDouble(evaluationRatio));
			sb.append("score: " + score);
    	} else if (evalType.equals(Constants.MAHOUT_EVALUATION_TYPE_IR)){
    		RecommenderIRStatsEvaluator evaluator = new GenericRecommenderIRStatsEvaluator();

    		IRStatistics stat = null;
    		if (recType.equals(Constants.MAHOUT_RECOMMENDER_TYPE_USER_BOOLEAN)){
    			DataModelBuilder modelBuilder = new DataModelBuilder() {
    				@Override
    				public DataModel buildDataModel(
    						FastByIDMap<PreferenceArray> trainingData) {
    					return new GenericBooleanPrefDataModel(GenericBooleanPrefDataModel.toDataMap(trainingData));
    				}
    			};
    			stat = evaluator.evaluate(builder, modelBuilder, dataModel, null, Integer.parseInt(at), Double.parseDouble(relevanceThreshold), Double.parseDouble(evaluationRatio));
    		} else {
    			stat = evaluator.evaluate(builder, null, dataModel, null, Integer.parseInt(at), Double.parseDouble(relevanceThreshold), Double.parseDouble(evaluationRatio));
    		}
    		sb.append("precision: " + stat.getPrecision()).append("\n");
    		sb.append("recall: " + stat.getRecall()).append("\n");
    		sb.append("fallout: " + stat.getFallOut()).append("\n");    		
    	} else if (evalType.equals(Constants.MAHOUT_EVALUATION_TYPE_PERFORMANCE)){
    		Recommender recommender = builder.buildRecommender(dataModel);
    		LoadEvaluator.runLoad(recommender);
    		sb.append("LoadEvaluator running in the server. Check server log to see performance metrics.");
    	} else {
    		logger.error("unrecognized evaluation type: " + evalType);
    		sb.append("unrecognized evaluation type: " + evalType);
    	}
    	
    	return sb.toString();
    }

	private RecommenderBuilder getRecommenderBuilder(
													final String recType,
													final String simType, 
													final String neighborhoodType,
													final String neighborhoodSize, 
													final String neighborhoodThreshold) 
	{
		RecommenderBuilder builder = new RecommenderBuilder(){

			@Override
			public Recommender buildRecommender(DataModel dataModel)
					throws TasteException {
				if (recType.equals(Constants.MAHOUT_RECOMMENDER_TYPE_USER)){
					UserSimilarity sim = null;
					if (simType.equals(Constants.MAHOUT_SIMILARITY_PEARSON)){
						sim = new PearsonCorrelationSimilarity(dataModel);
					} else if (simType.equals(Constants.MAHOUT_SIMILARITY_EUCLIDEAN)){
						sim = new EuclideanDistanceSimilarity(dataModel);
					} else if (simType.equals(Constants.MAHOUT_SIMILARITY_SPEARMAN)){
						sim = new SpearmanCorrelationSimilarity(dataModel);
					} else if (simType.equals(Constants.MAHOUT_SIMILARITY_COSINE)){
						sim = new UncenteredCosineSimilarity(dataModel);
					} else if (simType.equals(Constants.MAHOUT_SIMILARITY_TANIMOTO)){
						sim = new TanimotoCoefficientSimilarity(dataModel);
					} else if (simType.equals(Constants.MAHOUT_SIMILARITY_LOG)){
						sim = new LogLikelihoodSimilarity(dataModel);
					}
					UserNeighborhood neighborhood = null;
					if (neighborhoodType.equals(Constants.MAHOUT_NEIGHBORHOOD_NEARESTN)){						
						neighborhood = new NearestNUserNeighborhood(Integer.parseInt(neighborhoodSize), sim, dataModel);
					} else if (neighborhoodType.equals(Constants.MAHOUT_NEIGHBORHOOD_THRESHOLD)){						
						neighborhood = new ThresholdUserNeighborhood(Double.parseDouble(neighborhoodThreshold), sim, dataModel);
					}
					return new GenericUserBasedRecommender(dataModel, neighborhood, sim);
				} else if (recType.equals(Constants.MAHOUT_RECOMMENDER_TYPE_USER_BOOLEAN)){
					UserSimilarity sim = new LogLikelihoodSimilarity(dataModel);
					UserNeighborhood neighborhood = null;
					if (neighborhoodType.equals(Constants.MAHOUT_NEIGHBORHOOD_NEARESTN)){						
						neighborhood = new NearestNUserNeighborhood(Integer.parseInt(neighborhoodSize), sim, dataModel);
					} else if (neighborhoodType.equals(Constants.MAHOUT_NEIGHBORHOOD_THRESHOLD)){						
						neighborhood = new ThresholdUserNeighborhood(Double.parseDouble(neighborhoodThreshold), sim, dataModel);
					}
					return new GenericBooleanPrefUserBasedRecommender(dataModel, neighborhood, sim);
				} else if (recType.equals(Constants.MAHOUT_RECOMMENDER_TYPE_ITEM)){
					ItemSimilarity sim = null;
					if (simType.equals(Constants.MAHOUT_SIMILARITY_PEARSON)){
						sim = new PearsonCorrelationSimilarity(dataModel);
					} else if (simType.equals(Constants.MAHOUT_SIMILARITY_EUCLIDEAN)){
						sim = new EuclideanDistanceSimilarity(dataModel);
					} else if (simType.equals(Constants.MAHOUT_SIMILARITY_SPEARMAN)){
						logger.error("ItemSimilarity not available: " + simType);
						return null;
					} else if (simType.equals(Constants.MAHOUT_SIMILARITY_COSINE)){
						sim = new UncenteredCosineSimilarity(dataModel);
					} else if (simType.equals(Constants.MAHOUT_SIMILARITY_TANIMOTO)){
						sim = new TanimotoCoefficientSimilarity(dataModel);
					} else if (simType.equals(Constants.MAHOUT_SIMILARITY_LOG)){
						sim = new LogLikelihoodSimilarity(dataModel);
					}
					return new GenericItemBasedRecommender(dataModel, sim);
				} else if (recType.equals(Constants.MAHOUT_RECOMMENDER_TYPE_SLOPEONE)){
					return new SlopeOneRecommender(dataModel);
				} else {
					logger.error("Recommender not available: " + recType);
					return null;
				}
			}
    		
    	};
		return builder;
	}
    
    // obsolete, but the idea of using session is valuable
//    public void cacheImage(String nodeId, byte[] imageData){
//    	logger.info("Caching image for node: " + nodeId);
//    	HttpSession session = FlexContext.getHttpRequest().getSession();
//    	session.setAttribute("image", imageData);
//    }
    
    public double getQuote(String symbol){
    	return Math.random()*100;
    }
    
    public void updateQuote(String symbol, double value){
    	logger.debug("symbol: " + symbol + ", value: " + value);
    }
    
    public String getBaseURL() {
        return baseURL;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }
    
    public String getRealPath() {
		return realPath;
	}

	public void setRealPath(String realPath) {
		this.realPath = realPath;
	}

	@SuppressWarnings("unchecked")
	public Map getMap() {
		return map;
	}

	@SuppressWarnings("unchecked")
	public void setMap(Map map) {
		this.map = map;
	}

	public String getContextName(){
    	String[] parts = baseURL.split("/");
    	return parts[parts.length-1].trim();
    }

	public NodeDao getNodeDao() {
		return nodeDao;
	}

	public void setNodeDao(NodeDao nodeDao) {
		this.nodeDao = nodeDao;
	}

	public EdgeDao getEdgeDao() {
		return edgeDao;
	}

	public void setEdgeDao(EdgeDao edgeDao) {
		this.edgeDao = edgeDao;
	}

	public GroupDao getGroupDao() {
		return groupDao;
	}

	public void setGroupDao(GroupDao groupDao) {
		this.groupDao = groupDao;
	}

	public RoleDao getRoleDao() {
		return roleDao;
	}

	public void setRoleDao(RoleDao roleDao) {
		this.roleDao = roleDao;
	}

	public SurveyDao getSurveyDao() {
		return surveyDao;
	}

	public void setSurveyDao(SurveyDao surveyDao) {
		this.surveyDao = surveyDao;
	}

	public QuestionDao getQuestionDao() {
		return questionDao;
	}

	public void setQuestionDao(QuestionDao questionDao) {
		this.questionDao = questionDao;
	}

	public NodeRO getNodeRO() {
		return nodeRO;
	}

	public void setNodeRO(NodeRO nodeRO) {
		this.nodeRO = nodeRO;
	}

	public GroupRO getGroupRO() {
		return groupRO;
	}

	public void setGroupRO(GroupRO groupRO) {
		this.groupRO = groupRO;
	}

	public RoleRO getRoleRO() {
		return roleRO;
	}

	public void setRoleRO(RoleRO roleRO) {
		this.roleRO = roleRO;
	}

	public SurveyRO getSurveyRO() {
		return surveyRO;
	}

	public void setSurveyRO(SurveyRO surveyRO) {
		this.surveyRO = surveyRO;
	}

	public QuestionRO getQuestionRO() {
		return questionRO;
	}

	public void setQuestionRO(QuestionRO questionRO) {
		this.questionRO = questionRO;
	}

	public EdgeRO getEdgeRO() {
		return edgeRO;
	}

	public void setEdgeRO(EdgeRO edgeRO) {
		this.edgeRO = edgeRO;
	}

	public JobDao getJobDao() {
		return jobDao;
	}

	public void setJobDao(JobDao jobDao) {
		this.jobDao = jobDao;
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

	
}
