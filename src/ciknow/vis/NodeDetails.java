package ciknow.vis;

import ciknow.domain.*;
import ciknow.dao.EdgeDao;
import ciknow.dao.NodeDao;
import ciknow.dao.QuestionDao;
import ciknow.dao.SurveyDao;
import ciknow.ro.GenericRO;
import ciknow.util.Beans;
import ciknow.util.Constants;
import ciknow.util.GeneralUtil;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Generate HTML for displaying node details
 * @author gyao
 *
 */
public class NodeDetails {
	private static Log logger = LogFactory.getLog(NodeDetails.class);
	private NodeDao nodeDao;
	private EdgeDao edgeDao;
	private QuestionDao questionDao;
	private GenericRO genericRO;
	private SurveyDao surveyDao;

    private Node node = null;
    private List<Question> questions;
    private int count = 0;
    private int toggleIndex = 0;
    private Set<String> usedKeys = new HashSet<String>();
    private Set<String> usedLongKeys = new HashSet<String>();
    private List<Map<String, String>> eds;
    private Survey survey;
    
    public static void main(String[] args) throws FileNotFoundException{
    	NodeDetails nd = new NodeDetails(26L);
    	PrintWriter writer = new PrintWriter("build/testNode.html");
    	writer.write(nd.toHtml());
    	writer.close();
    }
    
    public NodeDetails(Long nodeId) {
        Beans.init();
        nodeDao = (NodeDao)Beans.getBean("nodeDao");
        edgeDao = (EdgeDao)Beans.getBean("edgeDao");
        questionDao = (QuestionDao)Beans.getBean("questionDao");
        genericRO = (GenericRO) Beans.getBean("genericRO");
        surveyDao = (SurveyDao) Beans.getBean("surveyDao");
        
        node = nodeDao.loadById(nodeId);
        questions = questionDao.getAll();
        
        eds = GeneralUtil.getEdgeDescriptions();
        survey = surveyDao.findById(1L);
    }

    public String toHtml(){
    	StringBuilder sb = new StringBuilder();
    	
    	String html = getBasicInfo();
    	sb.append(getHeaderLine("Basic Info"));
    	sb.append(html);
    	
    	html = getContactInfo();
    	if (html.length() != 0) {
    		sb.append(getHeaderLine("Contact Info"));
    		sb.append(html);
    	}
    	
    	html = getQuestionAnswers();
    	if (html.length() != 0) {
    		//sb.append(getHeaderLine("Survey Question Answers (Questions Must Be Visible To This Node)"));
    		sb.append(getHeaderLine("Attributes"));
    		sb.append(html);
    	}
    	
    	String showRawAttrs = survey.getAttribute(Constants.SURVEY_SHOW_RAW_ATTRIBUTES_IN_REPORT); // defined in ModelLocator.as
    	if (showRawAttrs != null && showRawAttrs.equals("Y")){
	    	html = getOtherAttributes();
	    	if (html.length() != 0) {
	    		sb.append(getHeaderLine("Raw Attributes"));
	    		sb.append(html);
	    	}
    	
    	
	    	html = getOtherLongAttributes();
	    	if (html.length() != 0) {
	    		sb.append(getHeaderLine("Raw Long Attributes"));
	    		sb.append(html);
	    	}
    	}
    	
    	html = getEdgesInfo();
    	if (html.length() != 0) {
    		sb.append(html);
    	}
    	
    	/*
    	html = getOutgoingEdgesInfo();
    	if (!html.isEmpty()) {
    		sb.append(getHeaderLine("Relations (outgoing)"));   
    		sb.append(html);
    	}
    	
    	html = getIncomingEdgesInfo();
    	if (!html.isEmpty()) {
    		sb.append(getHeaderLine("Relations (incoming)"));   
    		sb.append(html);
    	}
    	*/
    	
    	/*
    	html = getGroupInfo();
    	if (!html.isEmpty()) {
    		sb.append(getHeaderLine("Groups"));   
    		sb.append(html);
    	}
    	    	
    	html = getRoleInfo();
    	if (!html.isEmpty()) {
    		sb.append(getHeaderLine("Roles"));  
    		sb.append(html);
    	}   
    	*/ 	
    	
    	return sb.toString();
    }
    
    private String getBasicInfo(){
    	logger.info("preparing basic info...");
    	StringBuilder sb = new StringBuilder();
    	
    	sb.append(getTableLine("Name", node.getLabel()));
    	sb.append(getTableLine("Type", node.getType())); 
    	if (node.getUri() != null && node.getUri().length() != 0){
    		String url = node.getUri();
    		if (url.startsWith("http")){
    			url = "<a href='" + url + "'>" + url + "</a>";
    		}
    		sb.append(getTableLine("URI", url));
    	}
    	
    	/*
    	sb.append(getTableLine("Username", node.getUsername()));
    	sb.append(getTableLine("Enabled", node.getEnabled().toString()));
    	if (node.getFirstName() != null && !node.getFirstName().isEmpty()){
    		sb.append(getTableLine("FirstName", node.getFirstName()));
    	}
    	if (node.getLastName() != null && !node.getLastName().isEmpty()){
    		sb.append(getTableLine("LastName", node.getLastName()));
    	}
    	if (node.getMidName() != null && !node.getMidName().isEmpty()){
    		sb.append(getTableLine("MidName", node.getMidName()));
    	}    	   	
		*/

    	return sb.toString();
    }
    
    private String getContactInfo(){
    	logger.info("preparing contact info...");
    	StringBuilder sb = new StringBuilder();
    	
    	if (node.getAddr1() != null && node.getAddr1().length() != 0){
    		sb.append(getTableLine("Address Line 1", node.getAddr1()));
    	}
    	
    	if (node.getAddr2() != null && node.getAddr2().length() != 0){
    		sb.append(getTableLine("Address Line 2", node.getAddr2()));
    	}
    	
    	if (node.getCity() != null && node.getCity().length() != 0){
    		sb.append(getTableLine("City", node.getCity()));
    	}
    	
    	if (node.getState() != null && node.getState().length() != 0){
    		sb.append(getTableLine("State", node.getState()));
    	}
    	
    	if (node.getCountry() != null && node.getCountry().length() != 0){
    		sb.append(getTableLine("Country", node.getCountry()));
    	}
    	
    	if (node.getZipcode() != null && node.getZipcode().length() != 0){
    		sb.append(getTableLine("Zip", node.getZipcode()));
    	}
    	
    	if (node.getEmail() != null && node.getEmail().length() != 0){
    		sb.append(getTableLine("Email", node.getEmail()));
    	}
    	
    	if (node.getPhone() != null && node.getPhone().length() != 0){
    		sb.append(getTableLine("Phone", node.getPhone()));
    	}
    	
    	if (node.getCell() != null && node.getCell().length() != 0){
    		sb.append(getTableLine("Cell", node.getCell()));
    	}
    	
    	if (node.getFax() != null && node.getFax().length() != 0){
    		sb.append(getTableLine("Fax", node.getFax()));
    	}
    	
    	if (node.getDepartment() != null && node.getDepartment().length() != 0){
    		sb.append(getTableLine("Department", node.getDepartment()));
    	}
    	
    	if (node.getOrganization() != null && node.getOrganization().length() != 0){
    		sb.append(getTableLine("Organization", node.getOrganization()));
    	}
    	
    	if (node.getUnit() != null && node.getUnit().length() != 0){
    		sb.append(getTableLine("Unit", node.getUnit()));
    	}    	
    	
    	// custom contact fileds
    	logger.info("extracting custom (user defined) contact info from questions ...");
    	for (Question question : questions){
    		if (question.isContactInfo()){
    			logger.debug("question shortName: " + question.getShortName());
    			for (ContactField cf : question.getContactFields()){
    				String key = question.makeContactFieldKey(cf);
    				String value = node.getAttribute(key);
    				if (value != null){
    					sb.append(getTableLine(cf.getLabel(), value));
    					usedKeys.add(key);
    				}
    			}
    		}
    	}
    	
    	return sb.toString();
    }
    
    private String getQuestionAnswers(){
    	logger.info("preparing user's answers to survey questions...");
    	StringBuilder sb = new StringBuilder();
    	
    	// get eligible questions
    	List<Question> qs = new LinkedList<Question>();
    	for (Question q : questions){
    		if (q.isChoice()) qs.add(q);
    		if (q.isRating()) qs.add(q);
    		if (q.isContinuous()) qs.add(q);
    		if (q.isDuration()) qs.add(q);
    		if (q.isText()) qs.add(q);
    		if (q.isTextLong()) qs.add(q);
    		if (q.isMultipleChoice()) qs.add(q);
    		if (q.isMultipleRating()) qs.add(q);
    	}
    	
    	// only process eligible questions which are visible to specified node
    	Map<Question, Set<Long>> map = Question.getQuestionVisibleNodeIdsMap(qs);    	
    	for (Question question : qs){
    		Set<Long> visibleNodeIds = map.get(question);
    		if (!visibleNodeIds.contains(node.getId())) {
    			logger.debug(node.getUsername() + " is not visible to question(shortName=" + question.getShortName() + "). Question is ignored.");
    			continue;
    		}
    		
    		if (question.isChoice()) sb.append(getChoice(question));
    		else if (question.isRating()) sb.append(getRating(question));
    		else if (question.isContinuous()) sb.append(getContinuous(question));
    		else if (question.isText()) sb.append(getText(question));
    		else if (question.isTextLong()) sb.append(getTextLong(question));
    		else if (question.isMultipleChoice()) sb.append(getMultipleChoice(question));
    		else if (question.isMultipleRating()) sb.append(getMultipleRating(question));
    		else if (question.isDuration()) sb.append(getDuration(question));
    	}
    	
    	return sb.toString();
    }

    private String getOtherAttributes(){
    	logger.info("get attributes...");
    	StringBuilder sb = new StringBuilder();
    	Set<String> keys = new TreeSet<String>();
    	keys.addAll(node.getAttributes().keySet());
    	keys.removeAll(usedKeys);
    	for (String key : keys){
    		if (key.contains("`")) continue;
    		String value = node.getAttribute(key);
    		value = value.replaceAll(Constants.SEPERATOR, "::");
    		sb.append(getTableLine(key, value));
    	}
    	return sb.toString();
    }
    
    private String getOtherLongAttributes(){
    	logger.info("get long attributes...");
    	StringBuilder sb = new StringBuilder();
    	Set<String> keys = new TreeSet<String>();
    	keys.addAll(node.getLongAttributes().keySet());
    	keys.removeAll(usedLongKeys);
    	for (String key : keys){
    		if (key.contains("`")) continue;
    		String value = node.getLongAttribute(key);
    		value = value.replaceAll(Constants.SEPERATOR, "::");
    		sb.append(getTableLine(key, value));
    	}
    	return sb.toString();
    }
    
    private String getGroupInfo(){
    	logger.info("preparing group info...");
    	StringBuilder sb = new StringBuilder();
    	
    	for (Group group : node.getGroups()){
    		sb.append(group.getName()).append("<br>");
    	}
    	
		String content = sb.toString();
		if (content.length() == 0) return "";
    	return getTableLine("", content);  	
    }
    
    private String getRoleInfo(){
    	logger.info("preparing role info...");
    	StringBuilder sb = new StringBuilder();
    	
    	for (Role role : node.getRoles()){
    		sb.append(role.getName()).append("<br>");
    	}
    	
		String content = sb.toString();
		if (content.length() == 0) return "";
    	return getTableLine("", content);  	
    }
    
    private String getEdgesInfo(){
    	logger.info("preparing edges info...");
    	StringBuilder sb = new StringBuilder();
    	
    	List<Edge> edges = edgeDao.loadByFromNodeId(node.getId());
    	List<Edge> undirectedEdges = new LinkedList<Edge>();
    	List<Edge> directedOutgoingEdges = new LinkedList<Edge>();
    	for (Edge edge : edges){
    		if (edge.isDirected()) directedOutgoingEdges.add(edge);
    		else undirectedEdges.add(edge);
    	}
    	
    	edges = edgeDao.loadByToNodeId(node.getId());
    	List<Edge> directedIncomingEdges = new LinkedList<Edge>();
    	for (Edge edge : edges){
    		if (edge.isDirected()) directedIncomingEdges.add(edge);
    	}
    	
    	// merge directed edges if possible    	
    	List<Edge> mergedEdges = new LinkedList<Edge>();
    	Set<Edge> tempEdges = new HashSet<Edge>();
    	for (Edge e1 : directedOutgoingEdges){
    		for (Edge e2 : directedIncomingEdges){
    			if (tempEdges.contains(e2)) continue;
    			boolean merged = e1.merge(e2);
    			if (merged){
    				mergedEdges.add(e1);
    				tempEdges.add(e2);
    				break;
    			}
    		}
    	}
    	
    	// undirected edges  
    	if (!undirectedEdges.isEmpty()){
	    	sb.append(getHeaderLine("Relations (undirected)"));
	    	Map<String, List<Edge>> edgeMap = classifyEdges(undirectedEdges);
	    	for (String edgeType : edgeMap.keySet()){  
	    		toggleIndex++;
	    		List<Edge> edgeList = edgeMap.get(edgeType);
	    		String edgeTypeLabel = GeneralUtil.getEdgeLabel(eds, edgeType) + "(" + edgeList.size() + ")";
	    		sb.append(getTableLine(getToggleLink(edgeTypeLabel, toggleIndex), getEdgesTable(edgeList)));
	    	}
    	}
    	
    	// outgoing edges    
    	if (!directedOutgoingEdges.isEmpty()){
	    	sb.append(getHeaderLine("Relations (outgoing)"));
	    	Map<String, List<Edge>> edgeMap = classifyEdges(directedOutgoingEdges);
	    	for (String edgeType : edgeMap.keySet()){  
	    		toggleIndex++;
	    		List<Edge> edgeList = edgeMap.get(edgeType);
	    		String edgeTypeLabel = GeneralUtil.getEdgeLabel(eds, edgeType) + "(" + edgeList.size() + ")";
	    		sb.append(getTableLine(getToggleLink(edgeTypeLabel, toggleIndex), getEdgesTable(edgeList)));
	    	}
    	}
    	
    	// incoming edges
    	if (!directedIncomingEdges.isEmpty()){
	    	sb.append(getHeaderLine("Relations (incoming)"));
	    	Map<String, List<Edge>> edgeMap = classifyEdges(directedIncomingEdges);
	    	for (String edgeType : edgeMap.keySet()){  
	    		toggleIndex++;
	    		List<Edge> edgeList = edgeMap.get(edgeType);
	    		String edgeTypeLabel = GeneralUtil.getEdgeLabel(eds, edgeType) + "(" + edgeList.size() + ")";
	    		sb.append(getTableLine(getToggleLink(edgeTypeLabel, toggleIndex), getEdgesTable(edgeList)));
	    	}
    	}
    	
    	// merged edges
    	if (!mergedEdges.isEmpty()){
	    	sb.append(getHeaderLine("Relations (merged)"));
	    	Map<String, List<Edge>> edgeMap = classifyEdges(mergedEdges);
	    	for (String edgeType : edgeMap.keySet()){  
	    		toggleIndex++;
	    		List<Edge> edgeList = edgeMap.get(edgeType);
	    		String edgeTypeLabel = GeneralUtil.getEdgeLabel(eds, edgeType) + "(" + edgeList.size() + ")";
	    		sb.append(getTableLine(getToggleLink(edgeTypeLabel, toggleIndex), getEdgesTable(edgeList)));
	    	}
    	}
    	
    	return sb.toString();
    }
    
    private String getOutgoingEdgesInfo(){
    	logger.info("preparing outgoing edges info...");
    	StringBuilder sb = new StringBuilder();
    	
    	List<Edge> edges = edgeDao.loadByFromNodeId(node.getId());
    	List<Edge> undirectedEdges = new LinkedList<Edge>();
    	List<Edge> directedOutgoingEdges = new LinkedList<Edge>();
    	for (Edge edge : edges){
    		if (edge.isDirected()) directedOutgoingEdges.add(edge);
    		else undirectedEdges.add(edge);
    	}
    	
    	// undirected edges    	
    	Map<String, List<Edge>> edgeMap = classifyEdges(undirectedEdges);
    	for (String edgeType : edgeMap.keySet()){  
    		toggleIndex++;
    		List<Edge> edgeList = edgeMap.get(edgeType);
    		String edgeTypeLabel = GeneralUtil.getEdgeLabel(eds, edgeType) + "(" + edgeList.size() + ")";
    		sb.append(getTableLine(getToggleLink(edgeTypeLabel, toggleIndex), getEdgesTable(edgeList)));
    	}
    	
    	// outgoing edges    	
    	edgeMap = classifyEdges(directedOutgoingEdges);
    	for (String edgeType : edgeMap.keySet()){  
    		toggleIndex++;
    		List<Edge> edgeList = edgeMap.get(edgeType);
    		String edgeTypeLabel = GeneralUtil.getEdgeLabel(eds, edgeType) + "(" + edgeList.size() + ")";
    		sb.append(getTableLine(getToggleLink(edgeTypeLabel, toggleIndex), getEdgesTable(edgeList)));
    	}
    	
    	return sb.toString();
    }
    
    private String getIncomingEdgesInfo(){
    	logger.info("preparing incoming edges info...");
    	StringBuilder sb = new StringBuilder();
    	    	
    	List<Edge> edges = edgeDao.loadByToNodeId(node.getId());
    	List<Edge> directedIncomingEdges = new LinkedList<Edge>();
    	for (Edge edge : edges){
    		if (edge.isDirected()) directedIncomingEdges.add(edge);
    	}
    	
    	// incoming edges
    	Map<String, List<Edge>> edgeMap = classifyEdges(directedIncomingEdges);
    	for (String edgeType : edgeMap.keySet()){  
    		toggleIndex++;
    		List<Edge> edgeList = edgeMap.get(edgeType);
    		String edgeTypeLabel = GeneralUtil.getEdgeLabel(eds, edgeType) + "(" + edgeList.size() + ")";
    		sb.append(getTableLine(getToggleLink(edgeTypeLabel, toggleIndex), getEdgesTable(edgeList)));
    	}
    	
    	return sb.toString();
    }
    
    private Map<String, List<Edge>> classifyEdges(Collection<Edge> edges){
    	Map<String, List<Edge>> edgeMap = new TreeMap<String, List<Edge>>();
    	
    	for (Edge edge : edges){
    		String type = edge.getType();
    		List<Edge> edgeList = edgeMap.get(type);
    		if (edgeList == null) {
    			edgeList = new LinkedList<Edge>();
    			edgeMap.put(type, edgeList);
    		}
    		edgeList.add(edge);
    	}
    	
    	return edgeMap;
    }
    
    private String getEdgesTable(List<Edge> edgeList){
    	StringBuilder sb = new StringBuilder();
    	
    	boolean showCreator = false;
    	for (Edge edge : edgeList){
    		if (edge.getCreator() != null) {
    			showCreator = true;
    			break;
    		}
    	}
    	
    	if (showCreator) sb.append("<table id='" + toggleIndex + "' style='display:none'><tr><th>Node</th><th>Weight</th><th>Creator</th></tr>");
    	else sb.append("<table id='" + toggleIndex + "' style='display:none'><tr><th>Node</th><th>Weight</th></tr>");
    	
    	int edgeLineCount = 0;
    	for (Edge edge : edgeList){
    		if (edgeLineCount%2 == 0) sb.append("<tr class='evenEdgeLine'>");
    		else sb.append("<tr class='oddEdgeLine'>");
    		Node otherNode = edge.getFromNode();
    		if (node.equals(otherNode)) otherNode = edge.getToNode();
    		sb.append("<td width='275'>").append(getNodeLink(otherNode)).append("</td>");
    		sb.append("<td width='50'>").append(edge.getWeight()).append("</td>");
    		
    		if (showCreator){
	    		Node creator = edge.getCreator();
	    		sb.append("<td width='275'>").append(creator == null? "---":getNodeLink(creator)).append("</td>");
    		}
    		
    		sb.append("</tr>");
    		edgeLineCount++;
    	}
    	sb.append("</table>");
    	
    	return sb.toString();
    }
 
	
	private String getChoice(Question question) {
		if (!question.isChoice()) return "";
		
		StringBuilder sb = new StringBuilder();
		for (Field field : question.getFields()){
			String key = question.makeFieldKey(field);
			if (node.getAttribute(key) != null) {
				sb.append(field.getLabel()).append("<br>");
				usedKeys.add(key);
			}
		}
		
		String content = sb.toString();
		if (content.length() == 0) return "";
		else return getTableLine(question.getLabel(), content);
	}
	
	private String getRating(Question question) {
		if (!question.isRating()) return null;
		
		StringBuilder sb = new StringBuilder();
		for (Field field : question.getFields()){
			String key = question.makeFieldKey(field);
			String value = node.getAttribute(key);
			if (value != null) {
				String scaleName = Question.getScaleNameFromKey(value);
				Scale scale = question.getScaleByName(scaleName);
				sb.append("<b>" + field.getLabel() + "</b>").append(": ").append(scale.getLabel()).append("<br>");
				usedKeys.add(key);
			}
		}
		
		String content = sb.toString();
		if (content.length() == 0) return "";
		else return getTableLine(question.getLabel(), content);
	}
	
	private String getContinuous(Question question) {
		if (!question.isContinuous()) return null;
		
		StringBuilder sb = new StringBuilder();
		for (Field field : question.getFields()){
			String key = question.makeFieldKey(field);
			String value = node.getAttribute(key);
			if (value != null) {
				sb.append("<b>" + field.getLabel() + "</b>").append(": ").append(value).append("<br>");
				usedKeys.add(key);
			}
		}
		
		String content = sb.toString();
		if (content.length() == 0) return "";
		else return getTableLine(question.getLabel(), content);
	}	
	
	private String getText(Question question) {
		if (!question.isText()) return null;
		
		StringBuilder sb = new StringBuilder();
		for (TextField field : question.getTextFields()){
			String key = question.makeTextFieldKey(field);
			String value = node.getAttribute(key);
			if (value != null) {
				sb.append("<b>" + field.getLabel() + "</b>").append(": ").append(value).append("<br>");
				usedKeys.add(key);
			}
		}
		
		String content = sb.toString();
		if (content.length() == 0) return "";
		else return getTableLine(question.getLabel(), content);
	}
	
	private String getTextLong(Question question) {
		if (!question.isTextLong()) return null;
		
		StringBuilder sb = new StringBuilder();
		for (Field field : question.getFields()){
			String key = question.makeFieldKey(field);
			String value = node.getLongAttribute(key);
			if (value != null) {
				sb.append("<b>" + field.getLabel() + "</b>").append(": ").append(value).append("<br>");
				usedLongKeys.add(key);
			}
		}
		
		String content = sb.toString();
		if (content.length() == 0) return "";
		else return getTableLine(question.getLabel(), content);
	}
	
	private String getDuration(Question question) {
		if (!question.isDuration()) return null;
		
		StringBuilder sb = new StringBuilder();
		for (Field field : question.getFields()){
			String key = question.makeFieldKey(field);
			String value = node.getAttribute(key);
			if (value != null) {
				sb.append("<b>" + field.getLabel() + "</b>").append(": ").append(value).append("<br>");
				usedKeys.add(key);
			}
		}
		
		String content = sb.toString();
		if (content.length() == 0) return "";
		else return getTableLine(question.getLabel(), content);
	}
    
	private String getMultipleChoice(Question question) {
		if (!question.isMultipleChoice()) return null;
		
		StringBuilder sb = new StringBuilder();		
		for (TextField tf : question.getTextFields()){			
			String left = question.getLabel() + "(" + tf.getLabel() + ")";
			StringBuilder right = new StringBuilder();
			for (Field field : question.getFields()){
				String key = question.makeFieldsKey(field, tf);
				if (node.getAttribute(key) != null) {
					right.append(field.getLabel()).append("<br>");
					usedKeys.add(key);
				}
			}
			
			String content = right.toString();
			if (content.length() == 0) continue;
			sb.append(getTableLine(left, content));
		}
		
		return sb.toString();
	}
	
	private String getMultipleRating(Question question) {
		if (!question.isMultipleRating()) return null;
		
		StringBuilder sb = new StringBuilder();		
		for (TextField tf : question.getTextFields()){			
			String left = question.getLabel() + "(" + tf.getLabel() + ")";
			StringBuilder right = new StringBuilder();
			for (Field field : question.getFields()){
				String key = question.makeFieldsKey(field, tf);
				String value = node.getAttribute(key);
				if (value != null) {
					String scaleName = Question.getScaleNameFromKey(value);
					Scale scale = question.getScaleByName(scaleName);
					right.append("<b>" + field.getLabel() + "</b>").append(": ").append(scale.getLabel()).append("<br>");
					usedKeys.add(key);
				}
			}
			String content = right.toString();
			if (content.length() == 0) continue;
			sb.append(getTableLine(left, content));
		}
		
		return sb.toString();
	}
	
    private String getTableLine(String key, String value){
    	count++;
    	StringBuilder sb = new StringBuilder();
    	if (count%2 == 0) sb.append("<tr class='evenNodeLine'>");
    	else sb.append("<tr class='oddNodeLine'>");
    	sb.append("<td class='leftCell'>").append(key).append("</td>");    	
    	sb.append("<td class='rightCell'>").append(value).append("</td>");
    	sb.append("</tr>\n");
    	return sb.toString();
    }
    
    private String getHeaderLine(String header){
    	count = 0;
    	StringBuilder sb = new StringBuilder();
    	sb.append("<tr class='nodeInfoHeader'>");
    	sb.append("<td colspan=2>").append(header).append("</td>");    
    	sb.append("</tr>\n");
    	return sb.toString();
    }
    
    
    
    
    private String getNodeLink(Node node){
    	StringBuilder sb = new StringBuilder();
    	
    	String urlPrefix = genericRO.getBaseURL() + "/vis_get_node_info.jsp?node=";
		sb.append("<a href='").append(urlPrefix).append(node.getId()).append("'>");
		sb.append(node.getLabel());
		sb.append("</a>");
    	
    	return sb.toString();
    }
    
	
	public static String getToggleLink(String display, int toggleIndex){
		StringBuilder sb = new StringBuilder();

		sb.append("<a onClick=\"toggle('").append(toggleIndex).append("')\">");
		sb.append("<img id='").append(toggleIndex + "_icon").append("' src='images/plus.gif'/>&nbsp;");
		sb.append(display);
		sb.append("</a>");

		return sb.toString();
	}
}