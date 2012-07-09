package ciknow.ro;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;

import ciknow.dao.*;
import ciknow.domain.*;
import ciknow.dto.*;
import ciknow.util.Beans;
import ciknow.util.Constants;
import ciknow.util.NodeUtil;

public class QuestionRO {
	private static Log logger = LogFactory.getLog(QuestionRO.class);
	private QuestionDao questionDao;
	private GroupDao groupDao;
	private SurveyDao surveyDao;
	private PageDao pageDao;
	private NodeDao nodeDao;
	private EdgeDao edgeDao;
	private RoleDao roleDao;
	
    public QuestionRO(){

	}

    public static void main(String[] args){
    	Beans.init();
    	QuestionRO ro = (QuestionRO)Beans.getBean("questionRO");
    	
    	ro.testTransaction();
    }
    
    @Transactional
    public void testTransaction(){
    	Node node;
    	Question question;
    	
    	question = questionDao.findById(1L);
    	questionDao.delete(question);
    	question = questionDao.findById(1L);
    	if (question == null) logger.info("question deleted!");
    	else logger.info("question still there.");
    	
    	String username = "x";
    	create(username);
    	
    	node = nodeDao.findByUsername(username);
    	logger.info("username: " + node.getUsername() + ", id: " + node.getId());
    	
    	question.setId(1000000L);
    	questionDao.delete(question);
    	
    	node = nodeDao.getProxy(11111111L);
    	nodeDao.delete(node);
    }
    
    private Node create(String username){
    	Node node = new Node();
    	node.setUsername(username);
    	nodeDao.save(node);
    	return node;
    }
    
    
    
    
    public QuestionDao getQuestionDao() {
        return questionDao;
    }

    public void setQuestionDao(QuestionDao questionDao) {
        this.questionDao = questionDao;
    }

    public GroupDao getGroupDao() {
        return groupDao;
    }

    public void setGroupDao(GroupDao groupDao) {
        this.groupDao = groupDao;
    }

    public SurveyDao getSurveyDao() {
		return surveyDao;
	}

	public void setSurveyDao(SurveyDao surveyDao) {
		this.surveyDao = surveyDao;
	}

	public PageDao getPageDao() {
		return pageDao;
	}

	public void setPageDao(PageDao pageDao) {
		this.pageDao = pageDao;
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
	
	public RoleDao getRoleDao() {
		return roleDao;
	}

	public void setRoleDao(RoleDao roleDao) {
		this.roleDao = roleDao;
	}

	/*
	@Transactional
	public QuestionDTO createQuestion(QuestionDTO dto) throws Exception{
		logger.info("create question ...");
		dto.sequenceNumber = questionDao.getMaxSequenceNumber() + 1;
		return saveOrUpdateQuestion(dto);
    }
	
	@Transactional
	@SuppressWarnings("unchecked")
	public QuestionDTO copyQuestion(Map data) throws Exception{
		logger.info("copy question ...");
    	String currentQuestionId = (String)data.get("currentQuestionId");
    	String shortName = (String)data.get("shortName");
    	String label = (String)data.get("label");
    	logger.debug("currentQuestionId: " + currentQuestionId);
    	
    	Question cq = questionDao.findById(Long.parseLong(currentQuestionId));        	
    	Question q = new Question(cq);
    	q.setId(null);
    	q.setVersion(null);
    	q.setShortName(shortName);
    	q.setLabel(label);
    	q.setSequenceNumber(questionDao.getMaxSequenceNumber() + 1);        	
    	questionDao.save(q);
    	
    	if (q.isPerceivedChoice() || q.isPerceivedRating()) updateQuestionTags(q);
    	
    	return new QuestionDTO(q);
    }
	
	@Transactional
    public QuestionDTO updateQuestion(QuestionDTO dto){
        return saveOrUpdateQuestion(dto);
    }

	@Transactional
    public List<QuestionDTO> updateQuestions(List<QuestionDTO> dtos){
        List<QuestionDTO> dtoList = new ArrayList<QuestionDTO>();
        for (QuestionDTO dto : dtos){
            dtoList.add(updateQuestion(dto));
        }
        return dtoList;
    }
    
	public QuestionDTO refreshQuestion(String idString){
		Long questionId = Long.parseLong(idString);
		Question question = questionDao.findById(questionId);
		if (question == null) return null;
		else return new QuestionDTO(question);
	}
	
	public List<QuestionDTO> refreshQuestions(){
		List<QuestionDTO> dtos = new LinkedList<QuestionDTO>();
		for (Question question : questionDao.getAll()){
			dtos.add(new QuestionDTO(question));
		}
		return dtos;
	}
	
    private QuestionDTO saveOrUpdateQuestion(QuestionDTO dto){
        Question q;
        if (dto.questionId == 0) {
            logger.debug("creating new question.");
             q = new Question();
        } else {
            logger.debug("updating question.");
             q = questionDao.findById(dto.questionId);
            if(q == null) return null;
             q.setVersion(dto.version);
        }

        q.setShortName(dto.shortName);
        q.setLabel(dto.label);
        q.setInstruction(dto.instruction);
        q.setHtmlInstruction(dto.htmlInstruction);
        q.setRowPerPage(dto.rowPerPage);
        q.setType(dto.type);
        q.setSequenceNumber(dto.sequenceNumber);
        q.setAttributes(dto.attributes);
        q.setLongAttributes(dto.longAttributes);
        
        q.setSurvey(surveyDao.findById(dto.surveyId));

        // fields
        Set<Field> fields = new HashSet<Field>();
        for (FieldDTO fdto : dto.fields){
            fields.add(fdto.toField());
        }
        q.setFields(fields);

        // scales
        Set<Scale> scales = new HashSet<Scale>();
        for (ScaleDTO sdto : dto.scales){
            scales.add(sdto.toScale());
        }
        q.setScales(scales);


        // text fields
        Set<TextField> tfields = new HashSet<TextField>();
        for (TextFieldDTO tfdto : dto.textFields){
            tfields.add(tfdto.toTextField());
        }
        q.setTextFields(tfields);

        // contactFields
        Set<ContactField> contactFields = new HashSet<ContactField>();
        for (ContactFieldDTO cfdto : dto.contactFields){
            contactFields.add(cfdto.toContactField());
        }
        q.setContactFields(contactFields);
        
        
        // visibleGroups
        Set<Group> visibleGroups = new HashSet<Group>();
        for (Object id : dto.visibleGroups){
            visibleGroups.add(groupDao.findById(Long.parseLong(id.toString())));
        }
        q.setVisibleGroups(visibleGroups);
        
        // availableGroups
        Set<Group> availableGroups = new HashSet<Group>();
        for (Object id : dto.availableGroups){
            availableGroups.add(groupDao.findById(Long.parseLong(id.toString())));
        }
        q.setAvailableGroups(availableGroups);

        // availableGroups2
        Set<Group> availableGroups2 = new HashSet<Group>();
        for (Object id : dto.availableGroups2){
            availableGroups2.add(groupDao.findById(Long.parseLong(id.toString())));
        }
        q.setAvailableGroups2(availableGroups2);

        questionDao.save(q);

        if (q.isPerceivedChoice() || q.isPerceivedRating()) updateQuestionTags(q);
        
        logger.debug("done.");
        
        return new QuestionDTO(q);
    }    
	
	
    public QuestionDTO getQuestionById(Long id){
        Question q = questionDao.findById(id);
        if (q == null) return null;
        else return new QuestionDTO(q);
    }
	*/
    
    public List<QuestionDTO> getQuestionsBySurveyId(Long surveyId){
		logger.debug("getQuestionsBySurveyId...");
		List<QuestionDTO> dtos = new ArrayList<QuestionDTO>();
		Survey survey = surveyDao.findById(1L);
		for (Page page : survey.getPages()){
			for (Question question : page.getQuestions()){
				dtos.add(new QuestionDTO(question));
			}
		}
		logger.debug("getQuestionsBySurveyId...done");
		return dtos;
	}
    
	@Transactional
	public void copyQuestion(Question question){
		// copy question
		
		// save/update tags
		if (question.isPerceivedChoice() || question.isPerceivedRating()) {
			updateQuestionTags(question);
		}
	}
	
	@Transactional
	public void saveQuestion(Question question){
		// save question
		questionDao.save(question);
		
		// save/update tags
		if (question.isPerceivedChoice() || question.isPerceivedRating()) {
			updateQuestionTags(question);
		}
	}
	
    @Transactional
	public void deleteQuestion(Long questionId){
    	logger.info("Deleting question(id=" + questionId + ").");
        Question q = questionDao.findById(questionId);
        if (q == null) {
        	logger.warn("Cannot find question(id=" + questionId + ").");
        	return;
        }

        // clear data collected for this question
        clearQuestion(questionId);
                	
    	// delete node attribute, e.g. MAX_ANSWERED_QUESTION
        // TODO: if do so, I'll lose track of the user progress; need to rewrite in the future
    	//nodeDao.deleteAttributeByValue(q.getShortName());
    	
    	// delete question attribute, e.g. CONTACT_CHOOSER, JUMP_QUESTION
        questionDao.deleteAttributeByValue(q.getShortName());
        
        // to avoid optimistic locking failure (question may have been updated when clear data)            
        q = questionDao.findById(questionId);
        Page page = q.getPage();
        page.getQuestions().remove(q);
        pageDao.save(page);  
        
        // delete tags associated with question
        if (q.isPerceivedChoice() || q.isPerceivedRating()) deleteQuestionTags(q);
    }

    @Transactional
    public void clearQuestion(Long questionId){
    	logger.info("Clearing data collected for question(id=" + questionId + ").");
    	Question q = questionDao.findById(questionId);
    	if (q == null) {
    		logger.warn("Cannot find question(id=" + questionId + ").");
    		return;
    	}
    	
    	String type = q.getType();
    	if (type.equals(Constants.CHOICE) || 
    		type.equals(Constants.RATING) ||
    		type.equals(Constants.CONTINUOUS) ||
    		type.equals(Constants.TEXT) || 
    		type.equals(Constants.TEXT_LONG) || 
    		type.equals(Constants.MULTIPLE_CHOICE) || 
    		type.equals(Constants.MULTIPLE_RATING) || 
    		type.equals(Constants.DURATION_CHOOSER)){
    		
        	List<String> keys = new ArrayList<String>();
        	if (type.equals(Constants.CHOICE) || 
        		type.equals(Constants.RATING) ||
        		type.equals(Constants.CONTINUOUS) ||
        		type.equals(Constants.TEXT) ||
        		type.equals(Constants.TEXT_LONG) ||
        		type.equals(Constants.DURATION_CHOOSER)){
        		for (Field field : q.getFields()){
        			keys.add(q.makeFieldKey(field));
        		}
        	} else if (type.equals(Constants.MULTIPLE_CHOICE) ||
        				type.equals(Constants.MULTIPLE_RATING)){
        		for (Field field : q.getFields()){
            		for (TextField tf : q.getTextFields()){
            			keys.add(q.makeFieldsKey(field, tf));
            		}
        		}
        	}
        	
        	if (type.equals(Constants.TEXT_LONG)){
            	for (String key : keys){
            		nodeDao.deleteLongAttributeByKey(key);
            	}
        	} else {
            	for (String key : keys){
            		nodeDao.deleteAttributeByKey(key);
            	}	
        	}

    	} else if (type.equals(Constants.RELATIONAL_CHOICE)
				|| type.equals(Constants.RELATIONAL_RATING)
				|| type.equals(Constants.RELATIONAL_CONTINUOUS)
				|| type.equals(Constants.PERCEIVED_RELATIONAL_CHOICE)
				|| type.equals(Constants.PERCEIVED_RELATIONAL_RATING)
				|| type.equals(Constants.PERCEIVED_CHOICE)
				|| type.equals(Constants.PERCEIVED_RATING)
				|| type.equals(Constants.RELATIONAL_CHOICE_MULTIPLE)
				|| type.equals(Constants.RELATIONAL_RATING_MULTIPLE)){
			edgeDao.delete(edgeDao.findByQuestion(q));
		}
    }

	
    
    /*
    public List<QuestionDTO> changeOrder(Map data){
    	logger.info("changing question order...");
    	List<QuestionDTO> dtos = new LinkedList<QuestionDTO>();
    	List<Question> questions = questionDao.getAll();
    	Collections.sort(questions, new QuestionSequenceComparator());
    	
    	String dragQuestionName = (String)data.get("dragQuestionName");
    	Question dragQuestion = questionDao.findByShortName(dragQuestionName); 
    	int dragIndex = questions.indexOf(dragQuestion);
    	
    	String dropQuestionName = (String)data.get("dropQuestionName");    	 
    	Question dropQuestion = questionDao.findByShortName(dropQuestionName);
    	int dropIndex = questions.indexOf(dropQuestion);
    	

    	if (dragIndex > dropIndex){
    		questions.remove(dragIndex);
    		questions.add(dropIndex, dragQuestion);
    	} else if (dragIndex < dropIndex){
    		questions.add(dropIndex, dragQuestion);
    		questions.remove(dragIndex);
    	} else {
    		// no change
    	}
    	
    	// reset sequenceNumber
    	int sn = 0;
    	for (Question q : questions){
    		q.setSequenceNumber(++sn);
    	}
    	questionDao.save(questions);
    	
    	// convert to dtos
    	for (Question q : questions){
    		dtos.add(new QuestionDTO(q));
    	}
    	
    	logger.info("question order changed: dragQuestion=" + dragQuestionName + ", dropQuestion=" + dropQuestionName);
    	return dtos;
    }
    */
    
    public Map getChartData(Map data) throws Exception{
    	logger.info("get charting data...");
    	Map result = new HashMap();
    	
    	List<Map> answers = new LinkedList<Map>();
    	String questionId = (String)data.get("questionId");
    	Question question = questionDao.findById(Long.parseLong(questionId));
    		
		if (question.getType().equals(Constants.CHOICE)){
	    	for (Field field : question.getFields()){
	    		Map m = new HashMap();
	    		m.put("fieldLabel", field.getLabel());
	    		m.put("fieldName", field.getName());
	    		m.put("sequenceNumber", field.getIndex());
	    		String attrKey = question.makeFieldKey(field);
	    		List<Long> nodeIds = nodeDao.getNodeIdsWithAttribute(attrKey);
	    		m.put("count", nodeIds.size());
	    		logger.debug("key: " + attrKey + ", count: " + nodeIds.size());
	    		answers.add(m);
	    	}
	    	result.put("list", answers);
		} else if (question.getType().equals(Constants.RATING)){
	    	for (Field field : question.getFields()){
	    		Map m = new HashMap();
	    		m.put("fieldLabel", field.getLabel());
	    		m.put("fieldName", field.getName());
	    		m.put("sequenceNumber", field.getIndex());
	    		String attrKey = question.makeFieldKey(field);
	    		List<Long> nodeIds = nodeDao.getNodeIdsWithAttribute(attrKey);
	    		List<Node> nodes = nodeDao.loadByIds(nodeIds);
    			for (Scale scale : question.getScales()){
    				String attrValue = question.makeScaleKey(scale);
    				int count = 0;
    				for (Node node : nodes){
    					if (node.getAttribute(attrKey).equals(attrValue)) count++;
    				}
    				
    				m.put(scale.getLabel(), count);
    				logger.debug("key: " + attrKey + ", value=" + attrValue + ", count: " + count);
    			}
    			answers.add(m);
	    	}
	    	
	    	List<String> scaleLabels = new LinkedList<String>();
	    	for (Scale scale : question.getScales()){
	    		scaleLabels.add(scale.getLabel());
	    	}
	    	
	    	
	    	result.put("list", answers);
	    	result.put("scaleLabels", scaleLabels);
		} else if (question.getType().equals(Constants.CONTINUOUS)){
	    	for (Field field : question.getFields()){
	    		Map m = new HashMap();
	    		m.put("fieldLabel", field.getLabel());
	    		m.put("fieldName", field.getName());
	    		m.put("sequenceNumber", field.getIndex());
	    		String attrKey = question.makeFieldKey(field);
	    		List<Long> nodeIds = nodeDao.getNodeIdsWithAttribute(attrKey);
	    		List<Node> nodes = nodeDao.loadByIds(nodeIds);
	    		double sum = 0;
	    		for (Node node : nodes){
	    			String attrValue = node.getAttribute(attrKey);
	    			double value = Double.parseDouble(attrValue);
	    			sum += value;
	    		}
	    		m.put("sum", sum);
	    		logger.debug("key: " + attrKey + ", sum: " + sum);
	    		answers.add(m);
	    	}
	    	result.put("list", answers);
		} else if (question.getType().equals(Constants.MULTIPLE_CHOICE)){ 
			List<Map> list = new LinkedList<Map>();
			for (TextField tf : question.getTextFields()){
				answers = new LinkedList<Map>();
    	    	for (Field field : question.getFields()){
    	    		Map m = new HashMap();
    	    		m.put("fieldLabel", field.getLabel());
    	    		m.put("fieldName", field.getName());
    	    		m.put("sequenceNumber", field.getIndex());
		    		String attrKey = question.makeFieldsKey(field, tf);
		    		List<Long> nodeIds = nodeDao.getNodeIdsWithAttribute(attrKey);
		    		m.put("count", nodeIds.size());
		    		logger.debug("key: " + attrKey + ", count: " + nodeIds.size());
		    		answers.add(m);
    	    	}
    	    	
    	    	Map tfm = new HashMap();
    	    	tfm.put("textFieldLabel", tf.getLabel());
    	    	tfm.put("list", answers);
    	    	list.add(tfm);
			}
			result.put("list", list);
		} else if (question.getType().equals(Constants.MULTIPLE_RATING)){
			List<Map> list = new LinkedList<Map>();
			for (TextField tf : question.getTextFields()){
				answers = new LinkedList<Map>();
    	    	for (Field field : question.getFields()){
    	    		Map m = new HashMap();
    	    		m.put("fieldLabel", field.getLabel());
    	    		m.put("fieldName", field.getName());
    	    		m.put("sequenceNumber", field.getIndex());
		    		String attrKey = question.makeFieldsKey(field, tf);
		    		List<Long> nodeIds = nodeDao.getNodeIdsWithAttribute(attrKey);
		    		List<Node> nodes = nodeDao.loadByIds(nodeIds);
	    			for (Scale scale : question.getScales()){
	    				String attrValue = question.makeScaleKey(scale);
	    				int count = 0;
	    				for (Node node : nodes){
	    					if (node.getAttribute(attrKey).equals(attrValue)) count++;
	    				}
	    				
	    				m.put(scale.getLabel(), count);
	    				logger.debug("key: " + attrKey + ", value=" + attrValue + ", count: " + count);
	    			}
	    			answers.add(m);
    	    	}
    	    	
    	    	Map tfm = new HashMap();
    	    	tfm.put("textFieldLabel", tf.getLabel());
    	    	List<String> scaleLabels = new LinkedList<String>();
    	    	for (Scale scale : question.getScales()){
    	    		scaleLabels.add(scale.getLabel());
    	    	}
    	    	tfm.put("scaleLabels", scaleLabels);
    	    	tfm.put("list", answers);
    	    	list.add(tfm);
			}
			result.put("list", list);
		} else throw new Exception("Not applicable question type: " + question.getType());

    	return result;
    }

    /*
    public Map getQuestionData(Map input) throws Exception{
    	Map result = new HashMap();
    	String qid = (String)input.get("qid");
    	String nid = (String)input.get("nid");
    	Question question = questionDao.findById(Long.parseLong(qid));
    	Node node = nodeDao.findById(Long.parseLong(nid));
    	
    	if (question.getType().equals(Constants.TEXT_QUICK)){
    		result = getTextQuickQuestionData(question, node);
    	} else {
	    	List<Edge> edges = edgeDao.loadByQuestionAndNode(question, node);
	    	List<EdgeDTO> dtos = new LinkedList<EdgeDTO>();
	    	for (Edge edge : edges){
	    		dtos.add(new EdgeDTO(edge));
	    	}
	    	result.put("edges", dtos);
	    	
	    	    	
	    	if (question.getType().equals(Constants.PERCEIVED_CHOICE)
	    		|| question.getType().equals(Constants.PERCEIVED_RATING)){
	    		List<Node> tags = nodeDao.findTagsByQuestion(question);
	    		List<NodeDTO> tagdtos = new LinkedList<NodeDTO>();
	    		for (Node tag : tags){
	    			tagdtos.add(new NodeDTO(tag));
	    		}
	    		result.put("tags", tagdtos);
	    	}
    	}
    	
    	result.put("qid", qid);
    	result.put("nid", nid);

    	return result;
    }
    */
    /*
    public Map getTextQuickQuestionData(Question question, Node loginNode) throws Exception{
    	logger.info("get data for question (shortName=" + question.getShortName() + "), loginNode=" + loginNode.getUsername());
    	Map result = new HashMap();
    	
    	Set<Long> availableNodeIds = Question.getCombinedAvailableNodeIds(question, loginNode, false);
    	List<Node> nodes = nodeDao.loadByIds(availableNodeIds);
    	List<NodeDTO> dtos = new ArrayList<NodeDTO>();
    	for (Node node : nodes){
    		if (node.isHidden()) {
    			logger.debug("node(username=" + node.getUsername() + ") is hidden.");
    			continue;
    		}
    			
    		dtos.add(new NodeDTO(node));
    	}
    	result.put("availableNodes", dtos);
    	
    	return result;
    }
    */
    /**
     * get available node ids for a given quesiton and login node
     * note: end user need to further filter out hidden node from the return node set
     * @param question
     * @param node
     * @param column
     * @return
     * @throws Exception
     */
    /*
    public Set<Long> getAvailableNodeIds(Question question, Node loginNode, boolean column) throws Exception{
    	logger.info("get available nodeIds...");
    	logger.debug("question: " + question.getShortName());
    	logger.debug("loginNode: " + loginNode.getUsername());
    	
    	Set<Long> availableNodeIds = new HashSet<Long>();    	
    	Set<Long> contactNodeIds = new HashSet<Long>();
    	Set<Long> mandatoryNodeIds = new HashSet<Long>();
    	Set<Long> nonMandatoryNodeIds = new HashSet<Long>();
    	
    	if (question.getType().equals(Constants.RELATIONAL_CHOICE)
    			|| question.getType().equals(Constants.RELATIONAL_CONTINUOUS)
    			|| question.getType().equals(Constants.RELATIONAL_RATING)
    			|| question.getType().equals(Constants.RELATIONAL_CHOICE_MULTIPLE)
    			|| question.getType().equals(Constants.RELATIONAL_RATING_MULTIPLE)
    			|| question.getType().equals(Constants.PERCEIVED_CHOICE)
    			|| question.getType().equals(Constants.PERCEIVED_RATING)
    			|| question.getType().equals(Constants.PERCEIVED_RELATIONAL_CHOICE)
    			|| question.getType().equals(Constants.PERCEIVED_RELATIONAL_RATING)
    			|| question.getType().equals(Constants.TEXT_QUICK)){
    		logger.debug("get mandatory and non-mandatory nodeIds...");
    		Set<Group> groups = null;
    		if (column) groups = question.getAvailableGroups2();
    		else groups = question.getAvailableGroups();    		
    		for (Group group : groups){
    			List<Long> nodeIds = groupDao.getNodeIdsByGroupId(group.getId());
    			if (group.isMandatory()) mandatoryNodeIds.addAll(nodeIds);
    			else nonMandatoryNodeIds.addAll(nodeIds);
    		}
    		    		
    		Question cc = question.getContactChooserQuestion();
    		if (cc != null && question.useContactsOnly()){
    			logger.debug("user selected contacts only");
    			boolean negate = question.getCCNegate();
    			logger.debug("negate: " + negate);
    			Set<Long> ccAvailableNodeIds = getAvailableNodeIds(cc, loginNode, false);
    			List<Long> selectedNodeIds = new ArrayList<Long>();
    			String groupName = Group.getPrivateGroupName(loginNode.getUsername(), cc.getShortName());
    			Group privateGroup = groupDao.findByName(groupName);
    			if (privateGroup != null){
    				selectedNodeIds = groupDao.getNodeIdsByGroupId(privateGroup.getId());
    				selectedNodeIds.retainAll(ccAvailableNodeIds);
    			}
    			
    			logger.debug("selectedNodeIds.size()=" + selectedNodeIds.size());
    			if (selectedNodeIds.isEmpty()){    				
    				String strategy = cc.getCCEmptyStrategy();
    				if (strategy.equals(Constants.CC_EMPTY_NONE)){
						if (negate) {
							contactNodeIds = ccAvailableNodeIds;
						} else {
							// empty
						}
    				} else {
						if (negate){
							// empty
						} else {
							contactNodeIds = ccAvailableNodeIds;
						}
    				}
    			} else {
    				if (negate) {
    					ccAvailableNodeIds.removeAll(selectedNodeIds);
    					contactNodeIds = ccAvailableNodeIds;
    				} else {
    					contactNodeIds.addAll(selectedNodeIds);
    				}
    			}
    			
    			contactNodeIds.retainAll(nonMandatoryNodeIds);
    			availableNodeIds = contactNodeIds;
    		} else {
    			availableNodeIds = nonMandatoryNodeIds;
    		}
    		
    		availableNodeIds.addAll(mandatoryNodeIds);
    	} else if (question.getType().equals(Constants.CONTACT_CHOOSER)){
    		Set<Group> groups = question.getAvailableGroups();
    		
    		for (Group group : groups){
    			List<Long> nodeIds = groupDao.getNodeIdsByGroupId(group.getId());
    			availableNodeIds.addAll(nodeIds);
    		}
    	}
    	
    	// show self?
    	Long nodeId = loginNode.getId();
    	if (question.showMyself()){
    		availableNodeIds.add(nodeId);
    	} else availableNodeIds.remove(nodeId);
    	logger.debug("showMySelf: " + question.showMyself());
    	
    	logger.info("availableNodeIds count: " + availableNodeIds.size());
    	return availableNodeIds;
    }
    */
    /*
    public Map saveQuestionTextQuick(Map data) throws Exception{
    	logger.info("save data for question type: " + Constants.TEXT_QUICK);
    	
    	Map result = new HashMap();
    	
    	Map changedNodeMap = (Map)data.get("changedNodeMap");
    	NodeDTO loginNodeDTO = (NodeDTO)data.get("loginNode");
    	
    	Beans.init();
    	NodeRO nodeRO = (NodeRO)Beans.getBean("nodeRO");    	
    	List<NodeDTO> dtos = new ArrayList<NodeDTO>();
    	for (NodeDTO nodeDTO : (Collection<NodeDTO>)changedNodeMap.values()){
    		dtos.add(nodeDTO);
    	}
    	nodeRO.saveNodes(dtos);
    	logger.info(dtos.size() + " node updated.");
    	
    	loginNodeDTO = nodeRO.saveNode(loginNodeDTO);
    	logger.info("loginNode (username=" + loginNodeDTO.username + ") is updated.");
    	
    	result.put("loginNode", loginNodeDTO);
    	
    	return result;
    }
    */
    
	private void updateQuestionTags(Question question){    
		if (!question.isPerceivedChoice() && !question.isPerceivedRating()){
			return;
		}
		logger.info("Updating tags based on question: " + question.getLabel());
		
    	List<Node> oldTags = nodeDao.findTagsByQuestion(question);
    	Map<String, Node> oldTagsMap = new HashMap<String, Node>();
    	for (Node oldTag : oldTags){
    		oldTagsMap.put(oldTag.getUsername(), oldTag);
    	}   	
    	
    	// Get new tag labels based on question, fields, and scales
    	List<Node> newTags = new LinkedList<Node>(); 
    	List<String> newTagNames = new ArrayList<String>();
    	if (question.isPerceivedChoice()){
    		newTagNames = question.getTagNames4PerceivedChoice();
    	} else if (question.isPerceivedRating()){
    		newTagNames = question.getTagNames4PerceivedRating();
    	}
    	
    	// Prepare for creating new tag
    	Group tagGroup = groupDao.findByName(Constants.GROUP_TAG, true);    	
    	for (String tagName : newTagNames){
			Node oldTag = oldTagsMap.get(tagName);
			if (oldTag == null){
				newTags.add(NodeUtil.createTag(tagName, tagGroup));
			} else {
				// evict from oldTagsMap
				oldTagsMap.remove(tagName);
			}
    	}
    	
    	// create new tags
    	nodeDao.save(newTags);
    	logger.debug("created " + newTags.size() + " new tags.");
    	
    	// remove obsolete tags
    	Collection<Node> removeTags = oldTagsMap.values();
    	deleteTags(removeTags);
    	
    	logger.info("Update completed.");
	}
	
	private void deleteQuestionTags(Question question){
		logger.info("Delete tags based on question: " + question.getLabel());
		List<Node> removeTags = nodeDao.findTagsByQuestion(question);
    	deleteTags(removeTags);
    	logger.info("Deletion completed.");
	}
	
	private void deleteTags(Collection<Node> removeTags){
		Collection<Long> removeTagIds = new ArrayList<Long>();
    	for (Node removeTag : removeTags){
    		removeTagIds.add(removeTag.getId());
    	}
    	List<Edge> removeEdges = edgeDao.findByToNodeIds(removeTagIds);
    	edgeDao.delete(removeEdges);
    	nodeDao.delete(removeTags);
    	logger.debug("deleted " + removeTags.size() + " tags and " + removeEdges.size() + " edges.");
	}
}
