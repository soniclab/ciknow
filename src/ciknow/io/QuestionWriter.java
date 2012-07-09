package ciknow.io;

import java.io.File;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ciknow.dao.ActivityDao;
import ciknow.dao.EdgeDao;
import ciknow.dao.GroupDao;
import ciknow.dao.NodeDao;
import ciknow.dao.QuestionDao;
import ciknow.domain.Activity;
import ciknow.domain.Edge;
import ciknow.domain.Field;
import ciknow.domain.Group;
import ciknow.domain.Node;
import ciknow.domain.Page;
import ciknow.domain.Question;
import ciknow.domain.TextField;
import ciknow.util.EdgeUtil;
import ciknow.service.ActivityService;
import ciknow.util.Beans;
import ciknow.util.Constants;
import ciknow.zk.survey.response.NodeLabelComparator;

public class QuestionWriter {
	private static Log logger = LogFactory.getLog(QuestionWriter.class);
    private NodeDao nodeDao;
    private EdgeDao edgeDao;
    private GroupDao groupDao;
    private QuestionDao questionDao;
    private ActivityDao activityDao;
    private ActivityService activityService;
    
	public static void main(String[] args) throws Exception{
		String filename = "output/pc.txt";
		Long qid = 43L;
		PrintWriter pw = new PrintWriter(new File(filename));
		
		Beans.init();
        QuestionDao questionDao = (QuestionDao) Beans.getBean("questionDao");        
        Question question = questionDao.findById(qid);
        
        if (question == null){
        	logger.error("unrecognized question id: " + qid);
        	return;
        } else {
        	logger.debug("writing question(shortName=" + question.getShortName() + ") to file " + filename);
            QuestionWriter questionWriter = (QuestionWriter) Beans.getBean("questionWriter");
            Map<String, String> options = new HashMap<String, String>();
            options.put(Constants.IO_OUTPUT_FORMAT, "matrix");
            options.put(Constants.IO_KEEP_EMPTY_PRIVATE_GROUP, "1");
            options.put(Constants.IO_REMOVE_NON_RESPONDENT, "1");
            options.put(Constants.IO_EXPORT_BY_COLUMN, null);
        	questionWriter.write(pw, question, options);
        }
        
        System.exit(0);
	}
	
    public QuestionWriter(){
    	
    }

    public void write(Writer writer, Question question, Map<String, String> options) throws Exception{
    	logger.info("exporting quesiton...");
    	logger.debug("id: " + question.getId());
    	logger.debug("name: " + question.getShortName());
    	logger.debug("type: " + question.getType());
    	if (question.isChoice() || question.isRating() || question.isDuration() || question.isContinuous()) writeNode(writer, question, options);
    	else if (question.isMultipleRating() || question.isMultipleChoice()) writeNodeMultiple(writer, question, options);
    	else if (question.isText()) writeText(writer, question, options);
    	//else if (question.getType().equals(Constants.TEXT_QUICK)) writeTextQuick(writer, question);
    	else if (question.isTextLong()) writeTextLong(writer, question, options);
    	else if (question.isRelationalChoice() || question.isRelationalRating() || question.isRelationalContinuous()) writeRelational(writer, question, options);
    	else if (question.isRelationalRatingMultiple() || question.isRelationalChoiceMultiple()) writeRelationalMultiple(writer, question, options);
    	else if (question.isPerceivedRelationalChoice() || question.isPerceivedRelationalRating()) writePerceivedRelational(writer, question, options);
    	else if (question.isPerceivedChoice() || question.isPerceivedRating()) writePerceived(writer, question, options);
    	else if (question.isContactChooser()) writeContactChooser(writer, question, options);
    	else if (question.isContactProvider()) writeContactProvider(writer, question, options);
    	logger.info("done.");
    }
    
    
    
	private void writePerceived(Writer writer, Question question, Map<String, String> options) throws Exception{	
		boolean exportByColumn = "1".equals(options.get(Constants.IO_EXPORT_BY_COLUMN));
		if (exportByColumn) {
			logger.info("exporting by column...");
			writePerceivedByColumn(writer, question, options);
			return;
		}
		
		boolean removeNonRespondent = "1".equals(options.get(Constants.IO_REMOVE_NON_RESPONDENT));
		boolean ignoreActivities = "1".equals(options.get(Constants.IO_IGNORE_ACTIVITIES));
		StringBuilder sb = new StringBuilder();
		sb.append("# Question ID: " + question.getId()).append("\n");
		sb.append("# Question Type: " + question.getType()).append("\n");
		sb.append("# Question Label: " + question.getLabel()).append("\n");
		sb.append("option:").append(Constants.IO_EXPORT_BY_COLUMN).append("=0").append("\n");
		sb.append("option:").append(Constants.IO_REMOVE_NON_RESPONDENT).append("=").append(removeNonRespondent?"1":"0").append("\n");
		sb.append("option:").append(Constants.IO_IGNORE_ACTIVITIES).append("=").append(ignoreActivities?"1":"0").append("\n");
		sb.append("Question ShortName:").append(question.getShortName()).append("\n");
		
        List<Field> fields = question.getFields();
        StringBuilder lb = new StringBuilder();
        for (Field field : fields){
        	lb.append("\t").append(field.getName());
        }
        lb.append("\n");
        String fieldLine = lb.toString();

        List<Node> visibleNodes = nodeDao.loadByIds(question.getVisibleNodeIds());
        Collections.sort(visibleNodes, new NodeLabelComparator());
        List<Node> availableNodes = null;
        Question cp = question.getContactProviderQuestion();
    	if (cp == null){  
	        availableNodes = nodeDao.findByIds(question.getAvailableNodeIds(false));
	        Collections.sort(availableNodes, new NodeLabelComparator());
    	}
        List<Edge> questionEdges = edgeDao.loadByQuestion(question);   
        
        int i = 0;
        Page page = question.getPage();
        for (Node perceiver : visibleNodes){
        	if (cp != null){
        		availableNodes = getProvidedNodes(perceiver, cp);
        		if (availableNodes.isEmpty()) continue;
        	}
        	
        	// check whether the respondent answered the survey question
        	boolean answeredQuestion = ignoreActivities;        	        	        	
        	if (!answeredQuestion) {
        		List<Activity> acts = activityDao.getActivitiesBySubject(perceiver);
        		answeredQuestion = activityService.leavedPage(acts, page);
        	}
        	if (removeNonRespondent && !answeredQuestion) {
        		logger.warn("Ignored non-respondent: " + perceiver.getLabel());
        		continue;
        	}

        	
        	if (i > 0) sb.append("################## KEEP THIS SEPERATOR ##################\n");
        	sb.append(perceiver.getUsername()).append(fieldLine);
            
            for (Node node : availableNodes){
            	sb.append(node.getUsername());
                //List<Edge> edges = edgeDao.loadTaggingByPerceiverAndFromNodeAndQuestion(perceiver, node, question);
            	List<Edge> edges = EdgeUtil.getEdgesByCreatorAndFromNode(questionEdges, perceiver, node);
                Map<String, Edge> map = new HashMap<String, Edge>();
                for (Edge edge : edges) {
                	String tagName = edge.getToNode().getUsername();
                	String fieldName = Question.getFieldNameFromTagName(tagName);
                    map.put(fieldName, edge);
                }
                
                for (Field field : fields) {
                    sb.append("\t");
                    if (!answeredQuestion) {
                    	sb.append(Constants.NOT_ANSWERED);
                    	continue;
                    }
                    
                    Edge edge = map.get(field.getName());                    
                    if (question.isPerceivedChoice()){
                        if (edge == null)
                            sb.append("0");
                        else{
                            sb.append("1");
                        }
                    } else if (question.isPerceivedRating()){
	                    if (edge == null)
	                        sb.append("-1");
	                    else{
	                    	String tagName = edge.getToNode().getUsername();
	                        String scaleName = Question.getScaleNameFromTagName(tagName);
	                        sb.append(scaleName);
	                    }
                    } else {
                    	throw new Exception("question type mismatched.");
                    }
                }
                sb.append("\n");                
            }
            i++;
        }
		
		// write to file
		writer.write(sb.toString());
		writer.flush();
	}

	private void writePerceivedByColumn(Writer writer, Question question, Map<String, String> options) throws Exception{
		boolean removeNonRespondent = "1".equals(options.get(Constants.IO_REMOVE_NON_RESPONDENT));
		boolean ignoreActivities = "1".equals(options.get(Constants.IO_IGNORE_ACTIVITIES));
		StringBuilder sb = new StringBuilder();
		sb.append("# Question ID: " + question.getId()).append("\n");
		sb.append("# Question Type: " + question.getType()).append("\n");
		sb.append("# Question Label: " + question.getLabel()).append("\n");
		sb.append("option:").append(Constants.IO_EXPORT_BY_COLUMN).append("=1").append("\n");
		sb.append("option:").append(Constants.IO_REMOVE_NON_RESPONDENT).append("=").append(removeNonRespondent?"1":"0").append("\n");
		sb.append("option:").append(Constants.IO_IGNORE_ACTIVITIES).append("=").append(ignoreActivities?"1":"0").append("\n");
		sb.append("Question ShortName:").append(question.getShortName()).append("\n");
		
        List<Node> visibleNodes = nodeDao.loadByIds(question.getVisibleNodeIds());
        Collections.sort(visibleNodes, new NodeLabelComparator());
        List<Node> availableNodes = nodeDao.findByIds(question.getAvailableNodeIds(false));
        Collections.sort(availableNodes, new NodeLabelComparator());
        List<Edge> questionEdges = edgeDao.loadByQuestion(question);   
        List<Field> fields = question.getFields();
        
        StringBuilder lb = new StringBuilder();
        for (Node node : availableNodes){
        	lb.append("\t").append(node.getUsername());
        }
        lb.append("\n");
        String headerLine = lb.toString();

        int i = 0;
        Page page = question.getPage();
        Map<Long, Boolean> answerMap = new HashMap<Long, Boolean>();
        for (Field field : fields){        	
        	if (i > 0) sb.append("################## KEEP THIS SEPERATOR ##################\n");
        	sb.append(field.getName()).append(headerLine);
            
            for (Node perceiver : visibleNodes){
            	// check whether the respondent answered the survey question
            	Boolean answeredQuestion = answerMap.get(perceiver.getId());
            	if (answeredQuestion == null){
                	answeredQuestion = ignoreActivities;        	        	        	
                	if (!answeredQuestion) {
                		List<Activity> acts = activityDao.getActivitiesBySubject(perceiver);
                		answeredQuestion = activityService.leavedPage(acts, page);
                	}
            		answerMap.put(perceiver.getId(), answeredQuestion);
            	}
            	if (removeNonRespondent && !answeredQuestion) {
            		logger.warn("Ignored non-respondent: " + perceiver.getLabel());
            		continue;
            	}
            	
            	sb.append(perceiver.getUsername());
                List<Edge> edges = EdgeUtil.getTaggingsByCreatorAndField(questionEdges, perceiver, field);
                Map<Long, Edge> map = new HashMap<Long, Edge>();
                for (Edge edge : edges) {
                	map.put(edge.getFromNode().getId(), edge);
                }
                
                for (Node node : availableNodes) {
                    sb.append("\t");
                    if (!answeredQuestion) {
                    	sb.append(Constants.NOT_ANSWERED);
                    	continue;
                    }
                    
                    Edge edge = map.get(node.getId());                    
                    if (question.isPerceivedChoice()){
                        if (edge == null)
                            sb.append("0");
                        else{
                            sb.append("1");
                        }
                    } else if (question.isPerceivedRating()){
	                    if (edge == null)
	                        sb.append("-1");
	                    else{
	                    	String tagName = edge.getToNode().getUsername();
	                        String scaleName = Question.getScaleNameFromTagName(tagName);
	                        sb.append(scaleName);
	                    }
                    } else {
                    	throw new Exception("question type mismatched.");
                    }
                }
                sb.append("\n");                
            }
            i++;
        }
		
		// write to file
		writer.write(sb.toString());
		writer.flush();
	}

	private void writePerceivedRelational(Writer writer, Question question, Map<String, String> options) throws Exception{		
		boolean exportByColumn = "1".equals(options.get(Constants.IO_EXPORT_BY_COLUMN));
		if (exportByColumn) {
			logger.info("exporting by column...");
			writePerceivedRelationalByColumn(writer, question, options);
			return;
		}
		
		boolean removeNonRespondent = "1".equals(options.get(Constants.IO_REMOVE_NON_RESPONDENT));
		boolean ignoreActivities = "1".equals(options.get(Constants.IO_IGNORE_ACTIVITIES));
        StringBuilder sb = new StringBuilder();
		sb.append("# Question ID: " + question.getId()).append("\n");
		sb.append("# Question Type: " + question.getType()).append("\n");
		sb.append("# Question Label: " + question.getLabel()).append("\n");
		sb.append("option:").append(Constants.IO_EXPORT_BY_COLUMN).append("=0").append("\n");
		sb.append("option:").append(Constants.IO_REMOVE_NON_RESPONDENT).append("=").append(removeNonRespondent?"1":"0").append("\n");
		sb.append("option:").append(Constants.IO_IGNORE_ACTIVITIES).append("=").append(ignoreActivities?"1":"0").append("\n");
		sb.append("Question ShortName:").append(question.getShortName()).append("\n");
		
        List<Node> visibleNodes = nodeDao.loadByIds(question.getVisibleNodeIds());
        Collections.sort(visibleNodes, new NodeLabelComparator());
        List<Node> rowNodes = null;
        List<Node> colNodes = null;
        Question cp = question.getContactProviderQuestion();
    	if (cp == null){  
	        rowNodes = nodeDao.findByIds(question.getAvailableNodeIds(false));
	        Collections.sort(rowNodes, new NodeLabelComparator());
	        colNodes = nodeDao.findByIds(question.getAvailableNodeIds(true));
	        if (colNodes.isEmpty()) colNodes = rowNodes;
	        else {
	        	Collections.sort(colNodes, new NodeLabelComparator());
	        }
    	}
        List<Edge> questionEdges = edgeDao.loadByQuestion(question);  
        
        // writer header
        logger.debug("writing headers..."); 
        String nodeLine = null;
        if (cp == null) {
	        StringBuilder lb = new StringBuilder();
	        for (Node node : colNodes){
	        	lb.append("\t").append(node.getUsername());
	        }
	        lb.append("\n");
	        nodeLine = lb.toString();
        }
        
        // write matrix for each node  
        int i=0;
        Page page = question.getPage();
        for (Node perceiver: visibleNodes){
        	if (cp != null){
        		rowNodes = getProvidedNodes(perceiver, cp);
        		if (rowNodes.isEmpty()) continue;
        		colNodes = rowNodes;
    	        StringBuilder lb = new StringBuilder();
    	        for (Node node : colNodes){
    	        	lb.append("\t").append(node.getUsername());
    	        }
    	        lb.append("\n");
    	        nodeLine = lb.toString();
        	}
        	
        	// check whether the respondent answered the survey question
        	boolean answeredQuestion = ignoreActivities;        	        	        	
        	if (!answeredQuestion) {
        		List<Activity> acts = activityDao.getActivitiesBySubject(perceiver);
        		answeredQuestion = activityService.leavedPage(acts, page);
        	}
        	if (removeNonRespondent && !answeredQuestion) {
        		logger.warn("Ignored non-respondent: " + perceiver.getLabel());
        		continue;
        	}

        	
        	if (i > 0) sb.append("################## KEEP THIS SEPERATOR ##################\n");
        	sb.append(perceiver.getUsername()).append(nodeLine);

            for (Node node : rowNodes){
            	sb.append(node.getUsername());
                //List<Edge> edges = edgeDao.loadEdgesByTypeAndCreatorAndFromNode(question.getEdgeType(), perceiver, node, true);
                List<Edge> edges = EdgeUtil.getEdgesByCreatorAndFromNode(questionEdges, perceiver, node);
            	Map<Long, Edge> map = new HashMap<Long, Edge>();
                for (Edge edge : edges) {
                    map.put(edge.getToNode().getId(), edge);
                }
                
                for (Node n : colNodes) {
                    sb.append("\t");
                    if (!answeredQuestion) {
                    	sb.append(Constants.NOT_ANSWERED);
                    	continue;
                    }
                    
                    Edge edge = map.get(n.getId());                    
                    if (question.isPerceivedRelationalRating()){
	                    if (edge == null)
	                        sb.append("-1");
	                    else{
	                        String scaleKey = edge.getAttribute(Constants.SCALE_KEY);
	                        String scaleName = Question.getScaleNameFromKey(scaleKey);
	                        sb.append(scaleName);
	                    }
                    } else if (question.isPerceivedRelationalChoice()){
                        if (edge == null)
                            sb.append("0");
                        else{
                            sb.append("1");
                        }
                    } else{
                    	throw new Exception("question type mismatched.");
                    }
                }
                sb.append("\n");
            }
            i++;
        }

        // write to file
        writer.write(sb.toString());
        writer.flush();
	}

	private void writePerceivedRelationalByColumn(Writer writer, Question question, Map<String, String> options) throws Exception{
		boolean removeNonRespondent = "1".equals(options.get(Constants.IO_REMOVE_NON_RESPONDENT));
		boolean ignoreActivities = "1".equals(options.get(Constants.IO_IGNORE_ACTIVITIES));
        StringBuilder sb = new StringBuilder();
		sb.append("# Question ID: " + question.getId()).append("\n");
		sb.append("# Question Type: " + question.getType()).append("\n");
		sb.append("# Question Label: " + question.getLabel()).append("\n");
		sb.append("option:").append(Constants.IO_EXPORT_BY_COLUMN).append("=1").append("\n");
		sb.append("option:").append(Constants.IO_REMOVE_NON_RESPONDENT).append("=").append(removeNonRespondent?"1":"0").append("\n");
		sb.append("option:").append(Constants.IO_IGNORE_ACTIVITIES).append("=").append(ignoreActivities?"1":"0").append("\n");
		sb.append("Question ShortName:").append(question.getShortName()).append("\n");
		
        List<Node> visibleNodes = nodeDao.loadByIds(question.getVisibleNodeIds());
        Collections.sort(visibleNodes, new NodeLabelComparator());
        List<Node> availableNodes = nodeDao.findByIds(question.getAvailableNodeIds(false));
        Collections.sort(availableNodes, new NodeLabelComparator());
        List<Node> availableNodes2 = nodeDao.findByIds(question.getAvailableNodeIds(true));
        if (availableNodes2.isEmpty()) availableNodes2 = availableNodes;
        else {
        	Collections.sort(availableNodes2, new NodeLabelComparator());
        }
        List<Edge> questionEdges = edgeDao.loadByQuestion(question);  
        
        // writer header
        logger.debug("writing headers...");        
        StringBuilder lb = new StringBuilder();
        for (Node node : availableNodes){
        	lb.append("\t").append(node.getUsername());
        }
        lb.append("\n");
        String nodeLine = lb.toString();
        
        // write matrix for each node
        int i=0;
        Page page = question.getPage();
        Map<Long, Boolean> answerMap = new HashMap<Long, Boolean>();
        for (Node byNode: availableNodes2){
        	if (i > 0) sb.append("################## KEEP THIS SEPERATOR ##################\n");
        	sb.append(byNode.getUsername()).append(nodeLine);

            for (Node node : visibleNodes){
            	// check whether the respondent answered the survey question
            	Boolean answeredQuestion = answerMap.get(node.getId());
            	if (answeredQuestion == null){
                	answeredQuestion = ignoreActivities;        	        	        	
                	if (!answeredQuestion) {
                		List<Activity> acts = activityDao.getActivitiesBySubject(node);
                		answeredQuestion = activityService.leavedPage(acts, page);
                	}
            		answerMap.put(node.getId(), answeredQuestion);
            	}
            	if (removeNonRespondent && !answeredQuestion) {
            		logger.warn("Ignored non-respondent: " + node.getLabel());
            		continue;
            	}

            	
            	sb.append(node.getUsername());
                //List<Edge> edges = edgeDao.loadEdgesByTypeAndCreatorAndToNode(question.getEdgeType(), node, byNode, true);
            	List<Edge> edges = EdgeUtil.getEdgesByCreatorAndToNode(questionEdges, node, byNode);
                Map<Long, Edge> map = new HashMap<Long, Edge>();
                for (Edge edge : edges) {
                    map.put(edge.getFromNode().getId(), edge);
                }
                
                for (Node n : availableNodes) {
                    sb.append("\t");
                    if (!answeredQuestion) {
                    	sb.append(Constants.NOT_ANSWERED);
                    	continue;
                    }
                    
                    Edge edge = map.get(n.getId());                    
                    if (question.isPerceivedRelationalRating()){
	                    if (edge == null)
	                        sb.append("-1");
	                    else{
	                        String scaleKey = edge.getAttribute(Constants.SCALE_KEY);
	                        String scaleName = Question.getScaleNameFromKey(scaleKey);
	                        sb.append(scaleName);
	                    }
                    } else if (question.isPerceivedRelationalChoice()){
                        if (edge == null)
                            sb.append("0");
                        else{
                            sb.append("1");
                        }
                    } else{
                    	throw new Exception("question type mismatched.");
                    }
                }
                sb.append("\n");
            }
            i++;
        }

        // write to file
        writer.write(sb.toString());
        writer.flush();
	}
	
	private void writeRelational(Writer writer, Question question, Map<String, String> options) throws Exception{
		Question cp = question.getContactProviderQuestion();
		if (cp != null) {
			writeRelationalByContactProvider(writer, question, options);
			return;
		}
		
		boolean removeNonRespondent = "1".equals(options.get(Constants.IO_REMOVE_NON_RESPONDENT));
		boolean ignoreActivities = "1".equals(options.get(Constants.IO_IGNORE_ACTIVITIES));
		
        StringBuilder sb = new StringBuilder();
		sb.append("# Question ID: " + question.getId()).append("\n");
		sb.append("# Question Type: " + question.getType()).append("\n");
		sb.append("# Question Label: " + question.getLabel()).append("\n");
		sb.append("option:").append(Constants.IO_REMOVE_NON_RESPONDENT).append("=").append(removeNonRespondent?"1":"0").append("\n");
		sb.append("option:").append(Constants.IO_IGNORE_ACTIVITIES).append("=").append(ignoreActivities?"1":"0").append("\n");
		sb.append("Question ShortName:").append(question.getShortName()).append("\n");

        List<Node> visibleNodes = nodeDao.loadByIds(question.getVisibleNodeIds());
        Collections.sort(visibleNodes, new NodeLabelComparator());
        List<Node> availableNodes = new ArrayList<Node>();
	    availableNodes = nodeDao.findByIds(question.getAvailableNodeIds(false));
	    Collections.sort(availableNodes, new NodeLabelComparator());
        List<Edge> questionEdges = edgeDao.loadByQuestion(question);  
        
        // writer header
        logger.debug("writing headers...");
        for (Node node : availableNodes) {
            sb.append("\t");
            sb.append(node.getUsername());
        }
        sb.append("\n");

        // write row by row for each node
		logger.debug("there are " + visibleNodes.size() + " visible nodes..."); 
        int count=0;
        int total=0;
        Page page = question.getPage();
        for (Node node : visibleNodes) {
        	// check whether the respondent answered the survey question
        	boolean answeredQuestion = ignoreActivities;        	        	        	
        	if (!answeredQuestion) {
        		List<Activity> acts = activityDao.getActivitiesBySubject(node);
        		answeredQuestion = activityService.leavedPage(acts, page);
        	}
        	if (removeNonRespondent && !answeredQuestion) {
        		logger.warn("Ignored non-respondent: " + node.getLabel());
        		continue;
        	}
        	
            sb.append(node.getUsername());

            //String type = question.getEdgeType();
            //List<Edge> edges = edgeDao.loadDirectedOutgoingEdgeByType(node, type);
            List<Edge> edges = EdgeUtil.getEdgesByFromNode(questionEdges, node);
            
            Map<Long, Edge> map = new HashMap<Long, Edge>();
            for (Edge edge : edges) {
                map.put(edge.getToNode().getId(), edge);
            }

            for (Node n : availableNodes) {
                sb.append("\t");
                if (!answeredQuestion) {
                	sb.append(Constants.NOT_ANSWERED);
                	continue;
                }
                
                Edge edge = map.get(n.getId());                                
                if (question.isRelationalRating()){
	                if (edge == null)
	                    sb.append("-1");
	                else{
	                    String scaleKey = edge.getAttribute(Constants.SCALE_KEY);
                    	String scaleName = Question.getScaleNameFromKey(scaleKey);
                    	sb.append(scaleName);
	                }
                } else if (question.isRelationalChoice()){
    				if (edge == null)sb.append("0");
    				else sb.append("1");
                } else if (question.isRelationalContinuous()){
    				if (edge == null)sb.append("0");
    				else sb.append(edge.getWeight());
                } else {
                	throw new Exception("question type mismatched.");
                }
            }
            sb.append("\n");
            
            count++;
            total++;
            if (count==5000){
                writer.write(sb.toString());
                writer.flush();
                logger.debug(total + " nodes' relations written.");
                
                sb = new StringBuilder();
                count = 0;
            }
        }

        // write to file
        writer.write(sb.toString());
        writer.flush();
        logger.debug(total + " nodes' relations written.");
	}

	private void writeRelationalByContactProvider(Writer writer, Question question, Map<String, String> options) throws Exception{
		boolean removeNonRespondent = "1".equals(options.get(Constants.IO_REMOVE_NON_RESPONDENT));
		boolean ignoreActivities = "1".equals(options.get(Constants.IO_IGNORE_ACTIVITIES));
		
        StringBuilder sb = new StringBuilder();
		sb.append("# Question ID: " + question.getId()).append("\n");
		sb.append("# Question Type: " + question.getType()).append("\n");
		sb.append("# Question Label: " + question.getLabel()).append("\n");
		sb.append("option:").append(Constants.IO_REMOVE_NON_RESPONDENT).append("=").append(removeNonRespondent?"1":"0").append("\n");
		sb.append("option:").append(Constants.IO_IGNORE_ACTIVITIES).append("=").append(ignoreActivities?"1":"0").append("\n");
		sb.append("Question ShortName:").append(question.getShortName()).append("\n");

        List<Node> visibleNodes = nodeDao.loadByIds(question.getVisibleNodeIds());
        Collections.sort(visibleNodes, new NodeLabelComparator());
        List<Edge> questionEdges = edgeDao.loadByQuestion(question);   
        
        int i = 0;
        Page page = question.getPage();
        Question cp = question.getContactProviderQuestion();
        for (Node respondent : visibleNodes){
    		List<Node> availableNodes = getProvidedNodes(respondent, cp);
    		if (availableNodes.isEmpty()) continue;
        	
        	// check whether the respondent answered the survey question
        	boolean answeredQuestion = ignoreActivities;        	        	        	
        	if (!answeredQuestion) {
        		List<Activity> acts = activityDao.getActivitiesBySubject(respondent);
        		answeredQuestion = activityService.leavedPage(acts, page);
        	}
        	if (removeNonRespondent && !answeredQuestion) {
        		logger.warn("Ignored non-respondent: " + respondent.getLabel());
        		continue;
        	}

        	
        	if (i > 0) sb.append("################## KEEP THIS SEPERATOR ##################\n");
        	sb.append(respondent.getUsername()).append("\n");
            
            List<Edge> edges = EdgeUtil.getEdgesByFromNode(questionEdges, respondent);                
            Map<Long, Edge> map = new HashMap<Long, Edge>();
            for (Edge edge : edges) {
                map.put(edge.getToNode().getId(), edge);
            }
            
            for (Node node : availableNodes){
            	sb.append(node.getUsername()).append("\t");
                    
                Edge edge = map.get(node.getId());                    
                if (question.isRelationalRating()){
	                if (edge == null)
	                    sb.append("-1");
	                else{
	                    String scaleKey = edge.getAttribute(Constants.SCALE_KEY);
                    	String scaleName = Question.getScaleNameFromKey(scaleKey);
                    	sb.append(scaleName);
	                }
                } else if (question.isRelationalChoice()){
    				if (edge == null)sb.append("0");
    				else sb.append("1");
                } else if (question.isRelationalContinuous()){
    				if (edge == null)sb.append("0");
    				else sb.append(edge.getWeight());
                } else {
                	throw new Exception("question type mismatched.");
                }

                sb.append("\n");                
            }
            
            i++;
        }
		
		// write to file
		writer.write(sb.toString());
		writer.flush();
	}
	
	private void writeRelationalMultiple(Writer writer, Question question, Map<String, String> options) throws Exception{
		Question cp = question.getContactProviderQuestion();
		if (cp != null) {
			writeRelationalMultipleByContactProvider(writer, question, options);
			return;
		}
		
		boolean removeNonRespondent = "1".equals(options.get(Constants.IO_REMOVE_NON_RESPONDENT));
		boolean ignoreActivities = "1".equals(options.get(Constants.IO_IGNORE_ACTIVITIES));
        StringBuilder sb = new StringBuilder();
		sb.append("# Question ID: " + question.getId()).append("\n");
		sb.append("# Question Type: " + question.getType()).append("\n");
		sb.append("# Question Label: " + question.getLabel()).append("\n");
		sb.append("option:").append(Constants.IO_REMOVE_NON_RESPONDENT).append("=").append(removeNonRespondent?"1":"0").append("\n");
		sb.append("option:").append(Constants.IO_IGNORE_ACTIVITIES).append("=").append(ignoreActivities?"1":"0").append("\n");
		sb.append("Question ShortName:").append(question.getShortName()).append("\n");
		
        List<Node> visibleNodes = nodeDao.loadByIds(question.getVisibleNodeIds());
        Collections.sort(visibleNodes, new NodeLabelComparator());
        List<Node> availableNodes = nodeDao.findByIds(question.getAvailableNodeIds(false));
        Collections.sort(availableNodes, new NodeLabelComparator());
        
        // classify edges by types
        List<Edge> questionEdges = edgeDao.loadByQuestion(question); 
        Map<String, List<Edge>> edgeTypeMap = new HashMap<String, List<Edge>>();
        for (Edge edge : questionEdges){
        	String edgeType = edge.getType();
        	List<Edge> edgeList = edgeTypeMap.get(edgeType);
        	if (edgeList == null){
        		edgeList = new ArrayList<Edge>();
        		edgeTypeMap.put(edgeType, edgeList);
        	}
        	edgeList.add(edge);
        }

        
        // writer header
        logger.debug("writing headers...");
        StringBuilder nodeLineBuffer = new StringBuilder();
        for (Node node : availableNodes) {
            nodeLineBuffer.append("\t");
            nodeLineBuffer.append(node.getUsername());
        }
        nodeLineBuffer.append("\n");
        String nodeLine = nodeLineBuffer.toString();

        int i=0;
        Page page = question.getPage();
        Map<Long, Boolean> answerMap = new HashMap<Long, Boolean>();
        for (Field field : question.getFields()){
        	if (i > 0) sb.append("################## KEEP THIS SEPERATOR ##################\n");
        	sb.append(field.getName().trim()).append(nodeLine);
        	logger.debug("***** field: " + field.getName());
        	
            for (Node node : visibleNodes) {
            	// check whether the respondent answered the survey question
            	Boolean answeredQuestion = answerMap.get(node.getId());
            	if (answeredQuestion == null){
                	answeredQuestion = ignoreActivities;        	        	        	
                	if (!answeredQuestion) {
                		List<Activity> acts = activityDao.getActivitiesBySubject(node);
                		answeredQuestion = activityService.leavedPage(acts, page);
                	}
            		answerMap.put(node.getId(), answeredQuestion);
            	}
            	if (removeNonRespondent && !answeredQuestion) {
            		logger.warn("Ignored non-respondent: " + node.getLabel());
            		continue;
            	}
            	
                sb.append(node.getUsername());

                String type = question.getEdgeTypeWithField(field);
                //List<Edge> edges = edgeDao.loadDirectedOutgoingEdgeByType(node, type);
                List<Edge> edgesByType = edgeTypeMap.get(type);
                if (edgesByType == null) edgesByType = new ArrayList<Edge>();
                List<Edge> edges = EdgeUtil.getEdgesByFromNode(edgesByType, node);
                
                Map<Long, Edge> map = new HashMap<Long, Edge>();
                for (Edge edge : edges) {
                    map.put(edge.getToNode().getId(), edge);
                }

                for (Node n : availableNodes) {
                    sb.append("\t");
                    if (!answeredQuestion) {
                    	sb.append(Constants.NOT_ANSWERED);
                    	continue;
                    }
                    
                    Edge edge = map.get(n.getId());
                    if (question.isRelationalRatingMultiple()){
	                    if (edge == null)
	                        sb.append("-1");
	                    else{
	                        String scaleKey = edge.getAttribute(Constants.SCALE_KEY);
	                        String scaleName = Question.getScaleNameFromKey(scaleKey);
	                        sb.append(scaleName);
	                    }
                    } else if (question.isRelationalChoiceMultiple()){
                        if (edge == null) sb.append("0");
                        else {                        	
                        	if (field.getName().equalsIgnoreCase(Constants.OTHER)){
                        		String other = edge.getAttribute(Constants.OTHER);
                        		if (other != null && !other.trim().isEmpty()){
                        			sb.append(other);
                        		} else sb.append("1");
                        	} else sb.append("1");
                        }
                    } else {
                    	throw new Exception("question type mismatched.");
                    }
                    
                }
                sb.append("\n");                
            }
            
            writer.write(sb.toString());
            writer.flush();                    
            sb = new StringBuilder();
            
            i++;
        }
	}
	
	private void writeRelationalMultipleByContactProvider(Writer writer, Question question, Map<String, String> options) throws Exception{
		boolean removeNonRespondent = "1".equals(options.get(Constants.IO_REMOVE_NON_RESPONDENT));
		boolean ignoreActivities = "1".equals(options.get(Constants.IO_IGNORE_ACTIVITIES));
        StringBuilder sb = new StringBuilder();
		sb.append("# Question ID: " + question.getId()).append("\n");
		sb.append("# Question Type: " + question.getType()).append("\n");
		sb.append("# Question Label: " + question.getLabel()).append("\n");
		sb.append("option:").append(Constants.IO_REMOVE_NON_RESPONDENT).append("=").append(removeNonRespondent?"1":"0").append("\n");
		sb.append("option:").append(Constants.IO_IGNORE_ACTIVITIES).append("=").append(ignoreActivities?"1":"0").append("\n");
		sb.append("Question ShortName:").append(question.getShortName()).append("\n");

        List<Field> fields = question.getFields();
        StringBuilder lb = new StringBuilder();
        for (Field field : fields){
        	lb.append("\t").append(field.getName());
        }
        lb.append("\n");
        String fieldLine = lb.toString();

        List<Node> visibleNodes = nodeDao.loadByIds(question.getVisibleNodeIds());
        Collections.sort(visibleNodes, new NodeLabelComparator());
        List<Edge> questionEdges = edgeDao.loadByQuestion(question);   
        
        int i = 0;
        Page page = question.getPage();
        Question cp = question.getContactProviderQuestion();
        for (Node respondent : visibleNodes){
    		List<Node> availableNodes = getProvidedNodes(respondent, cp);
    		if (availableNodes.isEmpty()) continue;
        	
        	// check whether the respondent answered the survey question
        	boolean answeredQuestion = ignoreActivities;        	        	        	
        	if (!answeredQuestion) {
        		List<Activity> acts = activityDao.getActivitiesBySubject(respondent);
        		answeredQuestion = activityService.leavedPage(acts, page);
        	}
        	if (removeNonRespondent && !answeredQuestion) {
        		logger.warn("Ignored non-respondent: " + respondent.getLabel());
        		continue;
        	}

        	
        	if (i > 0) sb.append("################## KEEP THIS SEPERATOR ##################\n");
        	sb.append(respondent.getUsername()).append(fieldLine);
            
            for (Node node : availableNodes){
            	sb.append(node.getUsername());
                //List<Edge> edges = edgeDao.loadTaggingByPerceiverAndFromNodeAndQuestion(perceiver, node, question);
            	List<Edge> edges = EdgeUtil.getEdgesByFromNodeAndToNode(questionEdges, respondent, node);
                Map<String, Edge> map = new HashMap<String, Edge>();
                for (Edge edge : edges) {
                	String fieldName = Question.getFieldNameFromEdgeType(edge.getType());
                    map.put(fieldName, edge);
                }
                
                for (Field field : fields) {
                    sb.append("\t");
                    if (!answeredQuestion) {
                    	sb.append(Constants.NOT_ANSWERED);
                    	continue;
                    }
                    
                    Edge edge = map.get(field.getName());                    
                    if (question.isRelationalRatingMultiple()){
	                    if (edge == null)
	                        sb.append("-1");
	                    else{
	                        String scaleKey = edge.getAttribute(Constants.SCALE_KEY);
	                        String scaleName = Question.getScaleNameFromKey(scaleKey);
	                        sb.append(scaleName);
	                    }
                    } else if (question.isRelationalChoiceMultiple()){
                        if (edge == null) sb.append("0");
                        else {                        	
                        	if (field.getName().equalsIgnoreCase(Constants.OTHER)){
                        		String other = edge.getAttribute(Constants.OTHER);
                        		if (other != null && !other.trim().isEmpty()){
                        			sb.append(other);
                        		} else sb.append("1");
                        	} else sb.append("1");
                        }
                    } else {
                    	throw new Exception("question type mismatched.");
                    }
                }
                sb.append("\n");                
            }
            i++;
        }
		
		// write to file
		writer.write(sb.toString());
		writer.flush();
	}
	
	private void writeText(Writer writer, Question question, Map<String, String> options) throws Exception{
		boolean removeNonRespondent = "1".equals(options.get(Constants.IO_REMOVE_NON_RESPONDENT));
		boolean ignoreActivities = "1".equals(options.get(Constants.IO_IGNORE_ACTIVITIES));
        StringBuilder sb = new StringBuilder();
		sb.append("# Question ID: " + question.getId()).append("\n");
		sb.append("# Question Type: " + question.getType()).append("\n");
		sb.append("# Question Label: " + question.getLabel()).append("\n");
		sb.append("option:").append(Constants.IO_REMOVE_NON_RESPONDENT).append("=").append(removeNonRespondent?"1":"0").append("\n");
		sb.append("option:").append(Constants.IO_IGNORE_ACTIVITIES).append("=").append(ignoreActivities?"1":"0").append("\n");
		sb.append("Question ShortName:").append(question.getShortName()).append("\n");

        // write header
        logger.debug("writing headers...");
        List<TextField> fields = question.getTextFields();
        for (TextField field : fields) {
            sb.append("\t");
            sb.append(field.getName());
        }
        sb.append("\n");

        // find nodes 
        List<Node> visibleNodes = nodeDao.loadByIds(question.getVisibleNodeIds());
        Collections.sort(visibleNodes, new NodeLabelComparator());
        logger.debug("there are " + visibleNodes.size() + " visible nodes..."); 
        int count=0;
        int total=0;
        Page page = question.getPage();
        for (Node node : visibleNodes) {
        	// check whether the respondent answered the survey question
        	boolean answeredQuestion = ignoreActivities;        	        	        	
        	if (!answeredQuestion) {
        		List<Activity> acts = activityDao.getActivitiesBySubject(node);
        		answeredQuestion = activityService.leavedPage(acts, page);
        	}
        	if (removeNonRespondent && !answeredQuestion) {
        		logger.warn("Ignored non-respondent: " + node.getLabel());
        		continue;
        	}
        	
            sb.append(node.getUsername());
            for (TextField field : fields) {
                sb.append("\t");
                if (!answeredQuestion) {
                	sb.append(Constants.NOT_ANSWERED);
                	continue;
                }
                
                String key = question.makeTextFieldKey(field);
                String value = node.getAttribute(key);
                if (value != null && value.trim().length() > 0) sb.append(value);
                else sb.append("-1");
            }
            sb.append("\n");
            
            count++;
            total++;
            if (count==5000){
                writer.write(sb.toString());
                writer.flush();
                logger.debug(total + " nodes written.");
                
                sb = new StringBuilder();
                count = 0;
            }
        }

        // write to file
        //logger.debug(sb.toString());
        writer.write(sb.toString());
        writer.flush();
        logger.debug(total + " nodes written.");
	}

	/*
	private void writeTextQuick(Writer writer, Question question) throws Exception{
        StringBuilder sb = new StringBuilder();
        sb.append(question.getShortName());

        // write header
        logger.debug("writing headers...");
        List<Field> fields = question.getFields();
        for (Field field : fields) {
            sb.append("\t");
            sb.append(field.getName());
        }
        sb.append("\n");

        // find nodes 
        List<Node> availableNodes = nodeDao.findByIds(question.getAvailableNodeIds(false));
        logger.debug("there are " + availableNodes.size() + " available nodes..."); 
        int count=0;
        int total=0;
        for (Node n : availableNodes) {
            Node node = nodeDao.loadById(n.getId());
            sb.append(node.getUsername());
            for (Field field : fields) {
                sb.append("\t");
                String key = question.makeFieldKey(field);
                String value = node.getAttribute(key);
                if (value != null && value.trim().length() > 0) sb.append(value);
                else sb.append("-1");
            }
            sb.append("\n");
            
            count++;
            total++;
            if (count==5000){
                writer.write(sb.toString());
                writer.flush();
                logger.debug(total + " nodes' texts written.");
                
                sb = new StringBuilder();
                count = 0;
            }
        }

        // write to file
        //logger.debug(sb.toString());
        writer.write(sb.toString());
        writer.flush();
        logger.debug(total + " nodes' texts written.");
	}
	*/
	
	private void writeTextLong(Writer writer, Question question, Map<String, String> options) throws Exception{
		boolean removeNonRespondent = "1".equals(options.get(Constants.IO_REMOVE_NON_RESPONDENT));
		boolean ignoreActivities = "1".equals(options.get(Constants.IO_IGNORE_ACTIVITIES));
        StringBuilder sb = new StringBuilder();
		sb.append("# Question ID: " + question.getId()).append("\n");
		sb.append("# Question Type: " + question.getType()).append("\n");
		sb.append("# Question Label: " + question.getLabel()).append("\n");
		sb.append("option:").append(Constants.IO_REMOVE_NON_RESPONDENT).append("=").append(removeNonRespondent?"1":"0").append("\n");
		sb.append("option:").append(Constants.IO_IGNORE_ACTIVITIES).append("=").append(ignoreActivities?"1":"0").append("\n");
		sb.append("Question ShortName:").append(question.getShortName()).append("\n");

        // write header
        logger.debug("writing headers...");
        List<Field> fields = question.getFields();
        for (Field field : fields) {
            sb.append("\t");
            sb.append(field.getName());
        }
        sb.append("\n");

        // find nodes 
        List<Node> visibleNodes = nodeDao.loadByIds(question.getVisibleNodeIds());
        Collections.sort(visibleNodes, new NodeLabelComparator());
        logger.debug("there are " + visibleNodes.size() + " visible nodes..."); 
        int count=0;
        int total=0;
        Page page = question.getPage();
        for (Node node : visibleNodes) {
        	// check whether the respondent answered the survey question
        	boolean answeredQuestion = ignoreActivities;        	        	        	
        	if (!answeredQuestion) {
        		List<Activity> acts = activityDao.getActivitiesBySubject(node);
        		answeredQuestion = activityService.leavedPage(acts, page);
        	}
        	if (removeNonRespondent && !answeredQuestion) {
        		logger.warn("Ignored non-respondent: " + node.getLabel());
        		continue;
        	}
        	
            sb.append(node.getUsername());
            for (Field field : fields) {
                sb.append("\t");
                if (!answeredQuestion) {
                	sb.append(Constants.NOT_ANSWERED);
                	continue;
                }
                
                String key = question.makeFieldKey(field);
                String value = node.getLongAttribute(key);
                if (value != null && value.trim().length() > 0) {
                	
                	// Don't allow new line or tab in long text, otherwise
                	// the exported file will be corrupted (interfere with defined format)
                	if (value.indexOf("\n") >= 0){
                		logger.warn("new line in long text are removed.");
                		value = value.replaceAll("\n", " ");
                	}
                	if (value.indexOf("\t") >= 0){
                		logger.warn("tab in long text are removed.");
                		value = value.replaceAll("\t", " ");
                	}
                	
                	sb.append(value);
                }
                else sb.append("-1");
            }
            sb.append("\n");
            
            count++;
            total++;
            if (count==5000){
                writer.write(sb.toString());
                writer.flush();
                logger.debug(total + " nodes written.");
                
                sb = new StringBuilder();
                count = 0;
            }
        }

        // write to file
        writer.write(sb.toString());
        writer.flush();
        logger.debug(total + " nodes written.");
	}

	private void writeNode(Writer writer, Question question, Map<String, String> options) throws Exception{
		boolean removeNonRespondent = "1".equals(options.get(Constants.IO_REMOVE_NON_RESPONDENT));
		boolean ignoreActivities = "1".equals(options.get(Constants.IO_IGNORE_ACTIVITIES));
        StringBuilder sb = new StringBuilder();
		sb.append("# Question ID: " + question.getId()).append("\n");
		sb.append("# Question Type: " + question.getType()).append("\n");
		sb.append("# Question Label: " + question.getLabel()).append("\n");
		sb.append("option:").append(Constants.IO_REMOVE_NON_RESPONDENT).append("=").append(removeNonRespondent?"1":"0").append("\n");
		sb.append("option:").append(Constants.IO_IGNORE_ACTIVITIES).append("=").append(ignoreActivities?"1":"0").append("\n");
		sb.append("Question ShortName:").append(question.getShortName()).append("\n");

        // write header
        logger.debug("writing headers...");
        List<Field> fields = question.getFields();
        for (Field field : fields) {
            sb.append("\t");
            sb.append(field.getName());
        }
        sb.append("\n");

        // find nodes 
        List<Node> visibleNodes = nodeDao.loadByIds(question.getVisibleNodeIds());
        Collections.sort(visibleNodes, new NodeLabelComparator());
        logger.debug("there are " + visibleNodes.size() + " visible nodes..."); 
        int count=0;
        int total=0;
        Page page = question.getPage();
        for (Node node : visibleNodes) {
        	// check whether the respondent answered the survey question
        	boolean answeredQuestion = ignoreActivities;        	        	        	
        	if (!answeredQuestion) {
        		List<Activity> acts = activityDao.getActivitiesBySubject(node);
        		answeredQuestion = activityService.leavedPage(acts, page);
        	}
        	if (removeNonRespondent && !answeredQuestion) {
        		logger.warn("Ignored non-respondent: " + node.getLabel());
        		continue;
        	}
        	
            sb.append(node.getUsername());
            for (Field field : fields) {
            	sb.append("\t");
                if (!answeredQuestion) {
                	sb.append(Constants.NOT_ANSWERED);
                	continue;
                }
                
                String key = question.makeFieldKey(field);
                String value = node.getAttribute(key);                
                String toWrite = "";
                if (question.isRating()){
                	toWrite = "-1";
                	if (value != null) toWrite = Question.getScaleNameFromKey(value);
                } else if (question.isChoice()){
                	if (value == null) toWrite = "0";
                	//else toWrite = "1";
                	else toWrite = value;
                } else if (question.isDuration()){
                	if (value == null) toWrite = "0";
                	else toWrite = value;
                } else if (question.isContinuous()){
                	if (value == null) toWrite = "-";
                	else toWrite = value;
                } else{
                	throw new Exception("question type mismatched.");
                }                       
                sb.append(toWrite);
            }
            sb.append("\n");
            
            count++;
            total++;
            if (count==5000){
                writer.write(sb.toString());
                writer.flush();
                logger.debug(total + " nodes written.");
                
                sb = new StringBuilder();
                count = 0;
            }
        }

        // write to file    
        writer.write(sb.toString());
        writer.flush();
        logger.debug(total + " nodes written.");
	}

	
	private void writeNodeMultiple(Writer writer, Question question, Map<String, String> options) throws Exception{
		boolean removeNonRespondent = "1".equals(options.get(Constants.IO_REMOVE_NON_RESPONDENT));
		boolean ignoreActivities = "1".equals(options.get(Constants.IO_IGNORE_ACTIVITIES));
        StringBuilder sb = new StringBuilder();
		sb.append("# Question ID: " + question.getId()).append("\n");
		sb.append("# Question Type: " + question.getType()).append("\n");
		sb.append("# Question Label: " + question.getLabel()).append("\n");
		sb.append("option:").append(Constants.IO_REMOVE_NON_RESPONDENT).append("=").append(removeNonRespondent?"1":"0").append("\n");
		sb.append("option:").append(Constants.IO_IGNORE_ACTIVITIES).append("=").append(ignoreActivities?"1":"0").append("\n");
		sb.append("Question ShortName:").append(question.getShortName()).append("\n");
		
        // write header
        logger.debug("writing headers...");
        List<Field> fields = question.getFields();
        StringBuilder fieldLineBuffer = new StringBuilder();
        for (Field field : fields) {
        	fieldLineBuffer.append("\t");
        	fieldLineBuffer.append(field.getName());
        }
        fieldLineBuffer.append("\n");
        String fieldLine = fieldLineBuffer.toString();

        // find nodes
        List<Node> visibleNodes = nodeDao.loadByIds(question.getVisibleNodeIds());
        Collections.sort(visibleNodes, new NodeLabelComparator());
        
        int i=0;
        Page page = question.getPage();
        Map<Long, Boolean> answerMap = new HashMap<Long, Boolean>();
        for (TextField tf : question.getTextFields()){
        	if (i > 0) sb.append("############### KEEP THIS SEPERATOR ###############\n");
        	sb.append(tf.getName().trim()).append(fieldLine);
        	logger.debug("***** text field: " + tf.getName());
        	
            for (Node node : visibleNodes) {  
            	// check whether the respondent answered the survey question
            	Boolean answeredQuestion = answerMap.get(node.getId());
            	if (answeredQuestion == null){
                	answeredQuestion = ignoreActivities;        	        	        	
                	if (!answeredQuestion) {
                		List<Activity> acts = activityDao.getActivitiesBySubject(node);
                		answeredQuestion = activityService.leavedPage(acts, page);
                	}
            		answerMap.put(node.getId(), answeredQuestion);
            	}
            	if (removeNonRespondent && !answeredQuestion) {
            		logger.warn("Ignored non-respondent: " + node.getLabel());
            		continue;
            	}
            	
                sb.append(node.getUsername());                
                for (Field field : fields) {   
                	sb.append("\t");
                    if (!answeredQuestion) {
                    	sb.append(Constants.NOT_ANSWERED);
                    	continue;
                    }
                    
                    String key = question.makeFieldsKey(field, tf);
                    String value = node.getAttribute(key);                    
                    String toWrite = "";
                    if (question.isMultipleRating()){
                    	toWrite = "-1";
                    	if (value != null) toWrite = Question.getScaleNameFromKey(value);
                    } else if (question.isMultipleChoice()){
                    	if (value == null) toWrite = "0";
                    	//else toWrite = "1";
                    	else toWrite = value;
                    } else{
                    	throw new Exception("question type mismatched.");
                    }                    
                    sb.append(toWrite);
                }
                sb.append("\n");
                
            }
            writer.write(sb.toString());
            writer.flush();                    
            sb = new StringBuilder();
            
            i++;
        }
	}

	private void writeContactChooser(Writer writer, Question question, Map<String, String> options) throws Exception{
		boolean keepEmptyPrivateGroup = options.get(Constants.IO_KEEP_EMPTY_PRIVATE_GROUP).equals("1");
		String format = options.get(Constants.IO_OUTPUT_FORMAT);
        StringBuilder sb = new StringBuilder();
		sb.append("# Question ID: " + question.getId()).append("\n");
		sb.append("# Question Type: " + question.getType()).append("\n");
		sb.append("# Question Label: " + question.getLabel()).append("\n");
		sb.append("option:").append(Constants.IO_KEEP_EMPTY_PRIVATE_GROUP).append("=").append(keepEmptyPrivateGroup?"1":"0").append("\n");
		sb.append("option:").append(Constants.IO_OUTPUT_FORMAT).append("=").append(format).append("\n");
		sb.append("Question ShortName:").append(question.getShortName()).append("\n");
        
		List<Node> visibleNodes = nodeDao.findByIds(question.getVisibleNodeIds());
		Collections.sort(visibleNodes, new NodeLabelComparator());
		List<Node> availableNodes = nodeDao.findByIds(question.getAvailableNodeIds(false));
		Collections.sort(availableNodes, new NodeLabelComparator());
		
        if (format.equals("matrix")){
        	for (Node node : availableNodes){
        		sb.append("\t").append(node.getUsername().trim());
        	}  
        	sb.append("\n");
        }        
		
		for (Node node : visibleNodes){
			Group group = groupDao.loadByName(Group.getPrivateGroupName(node.getUsername(), question.getShortName()));
			if (!keepEmptyPrivateGroup && (group == null || group.getNodes().isEmpty())) continue;
			
			Set<Node> privateNodes = (group==null)?new HashSet<Node>():group.getNodes();
			privateNodes.retainAll(availableNodes);
			
			sb.append(node.getUsername().trim());
			
			if (format.equals("matrix")){
				for (Node availableNode : availableNodes){
					sb.append("\t").append(privateNodes.contains(availableNode)?"1":"0");
				}
			} else if (format.equals("list")){
				for (Node n : privateNodes){
					sb.append("\t").append(n.getUsername().trim());
				}
			} else throw new Exception("unrecognized export format: " + format);
			
			sb.append("\n");
		}
		writer.write(sb.toString());
		writer.flush();
	}

	
	private void writeContactProvider(Writer writer, Question question, Map<String, String> options) throws Exception{
		boolean keepEmptyPrivateGroup = "1".equals(options.get(Constants.IO_KEEP_EMPTY_PRIVATE_GROUP));
        StringBuilder sb = new StringBuilder();
		sb.append("# Question ID: " + question.getId()).append("\n");
		sb.append("# Question Type: " + question.getType()).append("\n");
		sb.append("# Question Label: " + question.getLabel()).append("\n");
		sb.append("option:").append(Constants.IO_KEEP_EMPTY_PRIVATE_GROUP).append("=").append(keepEmptyPrivateGroup?"1":"0").append("\n");
		sb.append("Question ShortName:").append(question.getShortName()).append("\n");
        
		List<Node> visibleNodes = nodeDao.findByIds(question.getVisibleNodeIds());
		Collections.sort(visibleNodes, new NodeLabelComparator());
		
		for (Node node : visibleNodes){
			Group group = groupDao.loadByName(Group.getProviderGroupName(node.getUsername(), question.getShortName()));
			if (!keepEmptyPrivateGroup && (group == null || group.getNodes().isEmpty())) continue;
			
			Set<Node> providerNodes = (group==null)?new HashSet<Node>():group.getNodes();
			
			sb.append(node.getUsername().trim());

			for (Node n : providerNodes){
				sb.append("\t").append(n.getUsername().trim());
			}
			
			sb.append("\n");
		}
		writer.write(sb.toString());
		writer.flush();
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

	public QuestionDao getQuestionDao() {
		return questionDao;
	}

	public void setQuestionDao(QuestionDao questionDao) {
		this.questionDao = questionDao;
	}

	public ActivityDao getActivityDao() {
		return activityDao;
	}

	public void setActivityDao(ActivityDao activityDao) {
		this.activityDao = activityDao;
	}

	public ActivityService getActivityService() {
		return activityService;
	}

	public void setActivityService(ActivityService activityService) {
		this.activityService = activityService;
	}
	
	private List<Node> getProvidedNodes(Node respondent, Question contactProvider){
		List<Node> selectedNodes = new ArrayList<Node>();
        String groupName = Group.getProviderGroupName(respondent.getUsername(), contactProvider.getShortName());
        Group providerGroup = groupDao.findByName(groupName);
        if (providerGroup != null) {
            List<Long> selectedNodeIds = groupDao.getNodeIdsByGroupId(providerGroup.getId());
            selectedNodes = nodeDao.findByIds(selectedNodeIds);
            Collections.sort(selectedNodes, new NodeLabelComparator());            
        }
        return selectedNodes;
	}
}
