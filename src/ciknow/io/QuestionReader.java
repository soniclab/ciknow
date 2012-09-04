package ciknow.io;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ciknow.dao.*;
import ciknow.util.Beans;
import ciknow.util.Constants;
import ciknow.util.NodeUtil;
import ciknow.util.StringUtil;
import ciknow.domain.Edge;
import ciknow.domain.Field;
import ciknow.domain.Group;
import ciknow.domain.Node;
import ciknow.domain.Question;
import ciknow.domain.Scale;
import ciknow.domain.TextField;

/**
 * 
 * @author gyao
 * 
 */
public class QuestionReader{
	private static Log logger = LogFactory.getLog(QuestionReader.class);
    private NodeDao nodeDao;
    private EdgeDao edgeDao;
    private GroupDao groupDao;
    
	public static void main(String[] args) throws Exception{
		String filename = "results/textlong.txt";
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		
		Beans.init();
        QuestionReader questionReader = (QuestionReader) Beans.getBean("questionReader");
        questionReader.read(reader);
	}
	
    public QuestionReader(){

    }

    public void read(BufferedReader reader) throws Exception{
    	QuestionDao questionDao = (QuestionDao)Beans.getBean("questionDao");
    	String line = reader.readLine();
    	while (line.startsWith("#")){
    		line = reader.readLine();
    	}
    	Map<String, String> options = new HashMap<String, String>();
    	while (isOption(line)){
    		readOption(line, options);
    		line = reader.readLine();
    	}
    	String shortName = line.substring(line.indexOf(":") + 1).trim();
    	Question question = questionDao.findByShortName(shortName);
    	if (question == null) throw new Exception("Cannot find question with shortName = " + shortName);
    	
        logger.info("importing quesiton(name=" + question.getShortName() + ").");        
        if (question.isChoice() || question.isRating() || question.isDuration() || question.isContinuous()) readNode(reader, question, options);
        else if (question.isMultipleChoice() || question.isMultipleRating()) readNodeMultiple(reader, question, options);
        else if (question.isText()) readText(reader, question, options);
        //else if (question.getType().equals(Constants.TEXT_QUICK)) readTextQuick(reader, question);
        else if (question.isTextLong()) readTextLong(reader, question, options);
        else if (question.isRelationalChoice() || question.isRelationalRating() || question.isRelationalContinuous()) readRelational(reader, question, options);
        else if (question.isRelationalChoiceMultiple() || question.isRelationalRatingMultiple()) readRelationalMultiple(reader, question, options);
        else if (question.isPerceivedRelationalChoice() || question.isPerceivedRelationalRating()) readPerceivedRelational(reader, question, options);
        else if (question.isPerceivedChoice() || question.isPerceivedRating()) readPerceived(reader, question, options);
        else if (question.isContactChooser()) readContactChooser(reader, question, options);
        else if (question.isContactProvider()) readContactProvider(reader, question, options);
        else {
        	String msg = "question type: " + question.getType() + " is not supported for upload.";
        	throw new Exception(msg);
        }
        logger.info("done.");
    }
    
    private boolean isOption(String line){
    	return line.startsWith("option:");
    }
    private void readOption(String line, Map<String, String> options){
    	String option = line.substring(7);
    	String[] parts = option.split("=", -1);
    	options.put(parts[0], parts[1]);
    }
    
    /**
     * 
     * @param reader
     * @param question
     * @param options
     * @throws Exception
     */
	private void readPerceived(BufferedReader reader, Question question, Map<String, String> options) throws Exception{
		boolean exportByColumn = "1".equals(options.get(Constants.IO_EXPORT_BY_COLUMN));
		if (exportByColumn) {
			logger.info("importing by column ...");
			readPerceivedByColumn(reader, question, options);
			return;
		}
		
		Group tagGroup = groupDao.findByName(Constants.GROUP_TAG, true);
		
		// prepare a lookup map for tags
		List<Node> tags = nodeDao.findTagsByQuestion(question);
		Map<String, Node> tagMap = new HashMap<String, Node>();
		for (Node tag : tags){
			tagMap.put(tag.getUsername(), tag);
		}
		
		// get all fields
		String line = reader.readLine();
        String[] fieldNames = line.split("\t", -1);
        List<Field> fields = new ArrayList<Field>();
        for (int i=1; i<fieldNames.length; i++){
        	String fieldname = fieldNames[i];
            Field field = question.getFieldByName(fieldname);
            fields.add(field);
        }
        
        List<Edge> edges = new ArrayList<Edge>();
        int count = 0;
        while(line != null){
        	// ignore comments/seperators #####
        	if (line.startsWith("#")){
        		line = reader.readLine();
        		continue;
        	}
        	
        	// get perceiver
        	String username = line.split("\t", -1)[0];
            Node perceiver = nodeDao.loadByUsername(username);
            logger.debug("perceiver: " + perceiver.getUsername());
            
            // delete edges perceived by this node  
    		/* in order to import relational answers cumulatively, old edges won't be
    		 * removed. Use the "Clear Data" function in Question Design interface instead. 
            List<Edge> oldEdges = edgeDao.findTaggingByPerceiverAndQuestion(perceiver, question);
            edgeDao.delete(oldEdges);
			*/
            
            logger.debug("reading each row and create edges...");
            line = reader.readLine();
            while (line != null){
            	if (line.startsWith("#")) break;
                line = line.trim();                
                List<String> flags = StringUtil.splitAsList(line, "\t");
                Node from = nodeDao.findByUsername(flags.get(0));

                for (int i=1; i<flags.size(); i++){
                    String flag = flags.get(i);
                    if (flag.equals(Constants.NOT_ANSWERED)) continue;
                    if (question.isPerceivedRating() && flag.equals("-1")) continue;
                    if (question.isPerceivedChoice() && flag.equals("0")) continue;
                    
                    Edge edge = new Edge();
                    edge.setFromNode(from);
                    edge.setType(question.getEdgeType());
                    edge.setDirected(true);
                    edge.setWeight(1d);
                    edge.setCreator(perceiver); // indicate this IS perceived                                        
                    
                    Field field = fields.get(i-1);
                    String tagName = "undefined";
                    if (question.isPerceivedRating()){
                    	Scale scale = question.getScaleByName(flag);
                    	tagName = question.getTagName(field, scale);
                    } else if (question.isPerceivedChoice()){
                    	tagName = question.getTagName(field);
                    } else {
                    	throw new Exception("question type mismatched.");
                    }                                        
                    Node toNode = tagMap.get(tagName);
                    if (toNode == null) {
                    	toNode = NodeUtil.createTag(tagName, tagGroup);         			            		
                    	nodeDao.save(toNode);
                    	tagMap.put(tagName, toNode);
                    }
                    edge.setToNode(toNode);
                    
                    edges.add(edge);
                    count++;                    
                    if (count % Constants.HIBERNATE_BATCH_SIZE == 0){
                    	edgeDao.save(edges);
                    	edges = new ArrayList<Edge>();
                    	logger.info(count + " edges saved.");
                    }
                }
                line = reader.readLine();
            }

            // save to database
            edgeDao.save(edges);
            logger.info(count + " edges saved.");
            edges = new ArrayList<Edge>();
            count = 0;
            
            if (line == null) return;
            line = reader.readLine();
        }       
	}

	/**
	 * 
	 * @param reader
	 * @param question
	 * @param options
	 * @throws Exception
	 */
	private void readPerceivedByColumn(BufferedReader reader, Question question, Map<String, String> options) throws Exception{
		Group tagGroup = groupDao.findByName(Constants.GROUP_TAG, true);
		
		// prepare a lookup map for tags
		List<Node> tags = nodeDao.findTagsByQuestion(question);
		Map<String, Node> tagMap = new HashMap<String, Node>();
		for (Node tag : tags){
			tagMap.put(tag.getUsername(), tag);
		}
		
		// get available nodes
		String line = reader.readLine();
        String[] usernames = line.split("\t", -1);
        List<Node> nodes = new ArrayList<Node>();
        for (int i=1; i<usernames.length; i++){
        	String username = usernames[i];
            Node node = nodeDao.findByUsername(username);
            nodes.add(node);
        }
        
        List<Edge> edges = new ArrayList<Edge>();
        int count = 0;
        while(line != null){
        	// ignore comments/seperators #####
        	if (line.startsWith("#")){
        		line = reader.readLine();
        		continue;
        	}
        	
        	// get field
        	String fieldname = line.split("\t", -1)[0];
        	Field field = question.getFieldByName(fieldname);
            logger.debug("Field: " + fieldname);
            
            // delete edges perceived by this node  
    		/* in order to import relational answers cumulatively, old edges won't be
    		 * removed. Use the "Clear Data" function in Question Design interface instead. 
            List<Edge> oldEdges = edgeDao.findTaggingByPerceiverAndQuestion(perceiver, question);
            edgeDao.delete(oldEdges);
			*/
            
            logger.debug("reading each row and create edges...");
            line = reader.readLine();
            while (line != null){
            	if (line.startsWith("#")) break;
                line = line.trim();                
                List<String> flags = StringUtil.splitAsList(line, "\t");
                Node perceiver = nodeDao.findByUsername(flags.get(0));

                for (int i=1; i<flags.size(); i++){
                    String flag = flags.get(i);
                    if (flag.equals(Constants.NOT_ANSWERED)) continue;
                    if (question.isPerceivedRating() && flag.equals("-1")) continue;
                    if (question.isPerceivedChoice() && flag.equals("0")) continue;
                    
                    Edge edge = new Edge();
                    edge.setFromNode(nodes.get(i-1));
                    edge.setType(question.getEdgeType());
                    edge.setDirected(true);
                    edge.setWeight(1d);
                    edge.setCreator(perceiver); // indicate this IS perceived                                        
                    
                    String tagName = "undefined";
                    if (question.isPerceivedRating()){
                    	Scale scale = question.getScaleByName(flag);
                    	tagName = question.getTagName(field, scale);
                    } else if (question.isPerceivedChoice()){
                    	tagName = question.getTagName(field);
                    } else {
                    	throw new Exception("question type mismatched.");
                    }                                        
                    Node toNode = tagMap.get(tagName);
                    if (toNode == null) {
                    	toNode = NodeUtil.createTag(tagName, tagGroup);         			            		
                    	nodeDao.save(toNode);
                    	tagMap.put(tagName, toNode);
                    }
                    edge.setToNode(toNode);
                    
                    edges.add(edge);
                    count++;                    
                    if (count % Constants.HIBERNATE_BATCH_SIZE == 0){
                    	edgeDao.save(edges);
                    	edges = new ArrayList<Edge>();
                    	logger.info(count + " edges saved.");
                    }
                }
                line = reader.readLine();
            }

            // save to database
            edgeDao.save(edges);
            logger.info(count + " edges saved.");
            edges = new ArrayList<Edge>();
            count = 0;
            
            if (line == null) return;
            line = reader.readLine();
        }   
	}
	
	/**
	 * 
	 * @param reader
	 * @param question
	 * @param options
	 * @throws Exception
	 */
	private void readPerceivedRelational(BufferedReader reader, Question question, Map<String, String> options) throws Exception{
		boolean exportByColumn = "1".equals(options.get(Constants.IO_EXPORT_BY_COLUMN));
		if (exportByColumn) {
			logger.info("importing by column ...");
			readPerceivedRelationalByColumn(reader, question, options);
			return;
		}
		
		// get all nodes
		String line = reader.readLine();
        String[] usernames = line.split("\t", -1);
        List<Node> nodes = new ArrayList<Node>();
        for (int i=1; i<usernames.length; i++){
        	String username = usernames[i];
            Node node = nodeDao.findByUsername(username);
            nodes.add(node);
        }
        
        List<Edge> edges = new ArrayList<Edge>();
        int count = 0;
        while(line != null){
        	// ignore comments/seperators #####
        	if (line.startsWith("#")){
        		line = reader.readLine();
        		continue;
        	}
        	
        	// get perceiver
        	String username = line.split("\t", -1)[0];
            Node perceiver = nodeDao.findByUsername(username);
            logger.debug("perceiver: " + perceiver.getUsername());
            
            // delete edges perceived by this node    
    		/* in order to import relational answers cumulatively, old edges won't be
    		 * removed. Use the "Clear Data" function in Question Design interface instead. 
            List<Edge> oldEdges = edgeDao.findByTypeAndCreator(edgeType, perceiver);
            edgeDao.delete(oldEdges);
            */

            logger.debug("reading each row and create edges...");            
            line = reader.readLine();
            while (line != null){
            	if (line.startsWith("#")) break;
                line = line.trim();                
                List<String> flags = StringUtil.splitAsList(line, "\t");
                Node from = nodeDao.findByUsername(flags.get(0));

                for (int i=1; i<flags.size(); i++){
                    String flag = flags.get(i);
                    if (flag.equals(Constants.NOT_ANSWERED)) continue;
                    if (question.isPerceivedRelationalRating() && flag.equals("-1")) continue;
                    if (question.isPerceivedRelationalChoice() && flag.equals("0")) continue;
                    
                    Edge edge = new Edge();
                    edge.setFromNode(from);
                    edge.setToNode(nodes.get(i-1));
                    edge.setType(question.getEdgeType());
                    edge.setDirected(true);
                    edge.setWeight(1d);
                    edge.setCreator(perceiver); // indicate this IS perceived
                    
                    // "scale" attribute
                    if (question.isPerceivedRelationalRating()){
	                    Scale scale = question.getScaleByName(flag);
	                    String attrValue = question.makeScaleKey(scale);
	                    edge.setAttribute(Constants.SCALE_KEY, attrValue);
	                    edge.setWeight(scale.getValue());
                    }
                    
                    edges.add(edge);
                    count++;
                    if (count % Constants.HIBERNATE_BATCH_SIZE == 0){
                    	edgeDao.save(edges);
                    	edges = new ArrayList<Edge>();
                    	logger.info(count + " edges saved.");
                    }
                }
                line = reader.readLine();
            }

            // save to database
            edgeDao.save(edges);
            logger.info(count + " edges saved.");
            edges = new ArrayList<Edge>();
            count = 0;            
            
            if (line == null) return;
            line = reader.readLine();
        }       
	}
	
	/**
	 * 
	 * @param reader
	 * @param question
	 * @param options
	 * @throws Exception
	 */
	private void readPerceivedRelationalByColumn(BufferedReader reader, Question question, Map<String, String> options) throws Exception{
		// get all nodes
		String line = reader.readLine();
        String[] usernames = line.split("\t", -1);
        List<Node> nodes = new ArrayList<Node>();
        for (int i=1; i<usernames.length; i++){
        	String username = usernames[i];
            Node node = nodeDao.findByUsername(username);
            nodes.add(node);
        }
        
        List<Edge> edges = new ArrayList<Edge>();
        int count = 0;
        while(line != null){
        	// ignore comments/seperators #####
        	if (line.startsWith("#")){
        		line = reader.readLine();
        		continue;
        	}
        	
        	// get column
        	String username = line.split("\t", -1)[0];
            Node byNode = nodeDao.findByUsername(username);
            logger.debug("username: " + byNode.getUsername());
            
            // delete edges perceived by this node   
    		/* in order to import relational answers cumulatively, old edges won't be
    		 * removed. Use the "Clear Data" function in Question Design interface instead. 
            List<Edge> oldEdges = edgeDao.findByTypeAndToNode(edgeType, byNode);
            edgeDao.delete(oldEdges);
            */
            
            logger.debug("reading each row and create edges...");            
            line = reader.readLine();
            while (line != null){
            	if (line.startsWith("#")) break;
                line = line.trim();                
                List<String> flags = StringUtil.splitAsList(line, "\t");
                Node perceiver = nodeDao.findByUsername(flags.get(0));

                for (int i=1; i<flags.size(); i++){
                    String flag = flags.get(i);
                    if (flag.equals(Constants.NOT_ANSWERED)) continue;
                    if (question.isPerceivedRelationalRating() && flag.equals("-1")) continue;
                    if (question.isPerceivedRelationalChoice() && flag.equals("0")) continue;
                    
                    Edge edge = new Edge();
                    edge.setFromNode(nodes.get(i-1));
                    edge.setToNode(byNode);
                    edge.setType(question.getEdgeType());
                    edge.setDirected(true);
                    edge.setWeight(1d);
                    edge.setCreator(perceiver); // indicate this IS perceived
                    
                    // "scale" attribute
                    if (question.isPerceivedRelationalRating()){
	                    Scale scale = question.getScaleByName(flag);
	                    String attrValue = question.makeScaleKey(scale);
	                    edge.setAttribute(Constants.SCALE_KEY, attrValue);
	                    edge.setWeight(scale.getValue());
                    }
                    
                    edges.add(edge);
                    count++;                    
                    if (count % Constants.HIBERNATE_BATCH_SIZE == 0){
                    	edgeDao.save(edges);
                    	edges = new ArrayList<Edge>();
                    	logger.info(count + " edges saved.");
                    }
                }
                line = reader.readLine();
            }

            // save to database
            edgeDao.save(edges);
            logger.info(count + " edges saved.");
            edges = new ArrayList<Edge>();            
            count = 0;
            
            if (line == null) return;
            line = reader.readLine();
        }       
	}
	
	/**
	 * relational choice/rating/continuous
	 * @param reader
	 * @param question
	 * @param options
	 * @throws Exception
	 */
	private void readRelational(BufferedReader reader, Question question, Map<String, String> options) throws Exception{
		Question cp = question.getContactProviderQuestion();
		if (cp != null) {
			logger.warn("readRelationalByContactProvider is not implemented yet.");
			throw new Exception("To be implemented");
		}
		
        // remove old edges
		/* in order to import relational answers cumulatively, old edges won't be
		 * removed. Use the "Clear Data" function in Question Design interface instead. 
		logger.debug("removing old edges for this question.");
        String edgeType = question.getEdgeType();
        List<Edge> oldEdges = edgeDao.findByType(edgeType);       
        edgeDao.delete(oldEdges);
        */
		
        // prepare lookup map
        Map<String, Node> visibleNodeMap = getVisibleUsernameToNodeMap(question, Constants.PROXY);
        Map<String, Node> availableNodeMap = getAvailableUsernameToNodeMap(question, Constants.PROXY);
        
        logger.debug("reading headers...");
        String line = reader.readLine();
        String[] usernames = line.split("\t", -1);
        List<Node> nodes = new ArrayList<Node>();
        for (int i = 1; i < usernames.length; i++){
        	String username = usernames[i];
            Node node = availableNodeMap.get(username);
            if (node == null) {
            	node = nodeDao.findByUsername(username);
            	if (node == null) {
            		throw new Exception("cannot find node with username=" + username);
            	}
            	availableNodeMap.put(username, node);
            }
            nodes.add(node);
        }

        logger.debug("reading each row and create edges...");
        List<Edge> edges = new ArrayList<Edge>();
        int count = 0;
        
        line = reader.readLine();
        while (line != null){
        	line = line.trim();
            List<String> flags = StringUtil.splitAsList(line, "\t");
            String username = flags.get(0);
            Node from = visibleNodeMap.get(username);
            if (from == null) {
            	from = nodeDao.findByUsername(username);
            	if (from == null) {
            		throw new Exception("cannot find node with username=" + username);
            	}
            	visibleNodeMap.put(username, from);
            }
            

            for (int i=1; i<flags.size(); i++){
                String flag = flags.get(i);
                if (flag.equals(Constants.NOT_ANSWERED)) continue;
                if (question.isRelationalRating() && flag.equals("-1")) continue;
                if (question.isRelationalChoice() && flag.equals("0")) continue;
                if (question.isRelationalContinuous() && flag.equals("0")) continue;
                
                Edge edge = new Edge();
                edge.setFromNode(from);
                edge.setToNode(nodes.get(i-1));
                edge.setType(question.getEdgeType());
                edge.setDirected(true);
                edge.setCreator(null); // indicate this is NOT perceived
          
                if (question.isRelationalRating()){	                
	                Scale scale = question.getScaleByName(flag);
	                if (scale == null){
	                	throw new Exception("scaleName = " + flag + " is not available for question id=" + question.getId());
//	                	logger.warn("scaleName = " + flag + " is not available for question id=" + question.getId());
//	                	continue;
	                }
	                String attrValue = question.makeScaleKey(scale);
	                edge.setAttribute(Constants.SCALE_KEY, attrValue);
	                edge.setWeight(scale.getValue());
                } 
                else if (question.isRelationalChoice()) edge.setWeight(1d);
                else if (question.isRelationalContinuous()) edge.setWeight(Double.parseDouble(flag));
                else {
                	throw new Exception("question type mismatched.");
                }
                
                edges.add(edge);
                count++;
                if (count % Constants.HIBERNATE_BATCH_SIZE == 0){
                	edgeDao.save(edges);
                	edges = new ArrayList<Edge>();
                	logger.info(count + " edges saved.");
                }
            }
            line = reader.readLine();
        }

        // save to database
        edgeDao.save(edges);
        logger.debug(count + " edges saved.");
	}
	
	
	/**
	 * relational choice/rating multiple
	 * @param reader
	 * @param question
	 * @param options
	 * @throws Exception
	 */
	private void readRelationalMultiple(BufferedReader reader, Question question, Map<String, String> options) throws Exception{    
		Question cp = question.getContactProviderQuestion();
		if (cp != null) {
			logger.warn("readRelationalMultipleByContactProvider is not implemented yet.");
			throw new Exception("To be implemented");
		}
		
        // prepare lookup map
        Map<String, Node> visibleNodeMap = getVisibleUsernameToNodeMap(question, Constants.PROXY);
        Map<String, Node> availableNodeMap = getAvailableUsernameToNodeMap(question, Constants.PROXY);
        
        logger.debug("reading headers...");
        String line = reader.readLine();
        String[] usernames = line.split("\t", -1);
        List<Node> nodes = new ArrayList<Node>();
        for (int i = 1; i < usernames.length; i++){
        	String username = usernames[i];
            Node node = availableNodeMap.get(username);
            if (node == null) {
            	node = nodeDao.findByUsername(username);
            	if (node == null) {
            		throw new Exception("cannot find node with username=" + username);
            	}
            	availableNodeMap.put(username, node);
            }
            nodes.add(node);
        }

        logger.debug("reading main content...");
        List<Edge> edges = new ArrayList<Edge>();
        int count = 0;
        while (line != null){
        	String fieldName = line.split("\t", -1)[0];
        	Field field = question.getFieldByName(fieldName);
        	logger.debug("**** field: " + fieldName);
        	
            // remove old edges
    		/* in order to import relational answers cumulatively, old edges won't be
    		 * removed. Use the "Clear Data" function in Question Design interface instead. 
    		logger.debug("removing old edges ...");
            String edgeType = question.getEdgeTypeWithField(field);
            List<Edge> oldEdges = edgeDao.findByType(edgeType);       
            edgeDao.delete(oldEdges);
            */
        	
        	line = reader.readLine();
            while (line != null && !line.startsWith("####")){
            	line = line.trim();
                List<String> flags = StringUtil.splitAsList(line, "\t");
                String username = flags.get(0);
                Node from = visibleNodeMap.get(username);
                if (from == null) {
                	from = nodeDao.findByUsername(username);
                	if (from == null) {
                		throw new Exception("cannot find node with username=" + username);
                	}
                	visibleNodeMap.put(username, from);
                }
                

                for (int i=1; i<flags.size(); i++){
                    String flag = flags.get(i);
                    if (flag.equals(Constants.NOT_ANSWERED)) continue;
                    if (question.isRelationalRatingMultiple() && flag.equals("-1")) continue;
                    if (question.isRelationalChoiceMultiple() && flag.equals("0")) continue;
                    
                    Edge edge = new Edge();
                    edge.setFromNode(from);
                    edge.setToNode(nodes.get(i-1));
                    edge.setType(question.getEdgeTypeWithField(field));
                    edge.setDirected(true);
                    edge.setCreator(null); // indicate this is NOT perceived

                    if (question.isRelationalRatingMultiple()){	                
    	                Scale scale = question.getScaleByName(flag);
    	                if (scale == null){
    	                	throw new Exception("scaleName = " + flag + " is not available for question id=" + question.getId());
//    	                	logger.warn("scaleName = " + flag + " is not available for question id=" + question.getId());
//    	                	continue;
    	                }
    	                String attrValue = question.makeScaleKey(scale);
    	                edge.setAttribute(Constants.SCALE_KEY, attrValue);
    	                edge.setWeight(scale.getValue());
                    } else if (question.isRelationalChoiceMultiple()){
                    	edge.setWeight(1d);
                    } else {
                    	throw new Exception("question type mismatched.");
                    }
                    
                    edges.add(edge);
                    count++;                    
                    if (count % Constants.HIBERNATE_BATCH_SIZE == 0){
                    	edgeDao.save(edges);
                    	edges = new ArrayList<Edge>();
                    	logger.info(count + " edges saved.");
                    }
                }
                line = reader.readLine();
            }
            
            // save to database
            edgeDao.save(edges);
            logger.debug(count + " edges saved.");
            edges = new ArrayList<Edge>();
            count = 0;
            
            if (line == null) return;
            else line = reader.readLine();
        }               
	}

	/**
	 * 
	 * @param reader
	 * @param question
	 * @param options
	 * @throws Exception
	 */
	private void readText(BufferedReader reader, Question question, Map<String, String> options) throws Exception{        
        List<TextField> fields = new ArrayList<TextField>();
		List<Node> nodes = new ArrayList<Node>();
        
        // get fields from header
        logger.debug("reading headers...");
        String line = reader.readLine();
        String[] fieldNames = line.split("\t", -1);
        for (int i=1; i<fieldNames.length; i++){
        	String fieldName = fieldNames[i];
            TextField field = question.getTextFieldByName(fieldName);
            if (field == null) {
                throw new Exception("Syntax error: the field(" + fieldName + ") doesn't exist!");
            }
            fields.add(field);
        }

        // process each node's response (rating)
        logger.debug("reading each row (each node)");
        line = reader.readLine();
        int count = 0;
        while (line != null){
            //line = line.trim();
            List<String> texts = StringUtil.splitAsList(line, "\t");

            // the first item is username
            String username = texts.get(0);
            Node node = nodeDao.loadByUsername(username);
            if (node == null){
                throw new Exception("Syntax error: the node(" + username + ") doesn't exist!");
            }

            for (int i = 1; i < texts.size(); i++){
            	String text = texts.get(i);
            	if (text.equals(Constants.NOT_ANSWERED)) continue;
            	
                TextField field = fields.get(i-1);
                String attrKey = question.makeTextFieldKey(field);                
                if (text.equals("-1")) {
                    node.getAttributes().remove(attrKey);
                } else node.setAttribute(attrKey, text);                
            }
            
            nodes.add(node);            
            count++;
            if (count % Constants.HIBERNATE_BATCH_SIZE == 0){
            	nodeDao.save(nodes);
            	logger.debug(count + " nodes saved.");
            	
            	nodes = new ArrayList<Node>();
            }
            
            line = reader.readLine();
        }
		
		// persist the node and attributes
		nodeDao.save(nodes);
		logger.debug(count + " nodes saved.");
	}

	/*
	private void readTextQuick(BufferedReader reader, Question question, String firstLine) throws Exception{        
        List<Field> fields = new ArrayList<Field>();
		List<Node> nodes = new ArrayList<Node>();
        
        // get fields from header
        logger.debug("reading headers...");
        String[] fieldNames = firstLine.split("\t", -1);
        for (int i=1; i<fieldNames.length; i++){
        	String fieldName = fieldNames[i];
            Field field = question.getFieldByName(fieldName);
            if (field == null) {
                throw new Exception("Syntax error: the field(" + fieldName + ") doesn't exist!");
            }
            fields.add(field);
        }

        // process each node's response (rating)
        logger.debug("reading each row (each node)");
        String line = reader.readLine();
        int count = 0;
        while (line != null){
            //line = line.trim();
            List<String> texts = StringUtil.splitAsList(line, "\t");

            // the first item is username
            String username = texts.get(0);
            Node node = nodeDao.loadByUsername(username);
            if (node == null){
                throw new Exception("Syntax error: the node(" + username + ") doesn't exist!");
            }

            for (int i = 1; i < texts.size(); i++){
                Field field = fields.get(i-1);
                String attrKey = question.makeFieldKey(field);
                String text = texts.get(i);
                if (text.equals("-1")) {
                    node.getAttributes().remove(attrKey);
                } else node.setAttribute(attrKey, text);                
            }
            
            nodes.add(node);            
            count++;
            if (count % Constants.HIBERNATE_BATCH_SIZE == 0){
            	nodeDao.save(nodes);
            	logger.debug(count + " nodes saved.");
            	
            	nodes = new ArrayList<Node>();
            }
            
            line = reader.readLine();
        }
		
		// persist the node and attributes
		nodeDao.save(nodes);
		logger.debug(count + " nodes saved.");
	}
	*/
	
	/**
	 * 
	 * @param reader
	 * @param question
	 * @param options
	 * @throws Exception
	 */
	private void readTextLong(BufferedReader reader, Question question, Map<String, String> options) throws Exception{        
		List<Node> nodes = new ArrayList<Node>();

        // get fields from header
        logger.debug("reading headers...");
        List<Field> fields = new ArrayList<Field>();
        String line = reader.readLine();
        String[] fieldNames = line.split("\t", -1);
        for (int i=1; i<fieldNames.length; i++){
        	String fieldName = fieldNames[i];
            Field field = question.getFieldByName(fieldName);
            if (field == null) {
                throw new Exception("Syntax error: the field(" + fieldName + ") doesn't exist!");
            }
            fields.add(field);
        }

        // process each node's response (rating)
        logger.debug("reading each row (each node)");
        line = reader.readLine();
        int count = 0;
        while (line != null){
            line = line.trim();
            if (line.length() == 0) {
            	line = reader.readLine();
            	continue;
            }
            
            List<String> texts = StringUtil.splitAsList(line, "\t");
            if (texts.size() != fields.size() + 1){
            	throw new Exception("invalid line: " + line);
//            	logger.warn("invalid line: " + line);
//            	line = reader.readLine();
//            	continue;
            }
            
            // the first item is username
            String username = texts.get(0);
            Node node = nodeDao.loadByUsername(username);
            if (node == null){   
            	throw new Exception("Syntax error: the node(" + username + ") doesn't exist!\nLine: " + line + "\n");
//                logger.warn("Syntax error: the node(" + username + ") doesn't exist!");
//                logger.warn("line ignored: " + line);
//                line = reader.readLine();
//                continue;
            }

            for (int i = 1; i < texts.size(); i++){
            	String text = texts.get(i);
            	if (text.equals(Constants.NOT_ANSWERED)) continue;
            	
                Field field = fields.get(i-1);
                String attrKey = question.makeFieldKey(field);                
                if (text.equals("-1")) {
                    node.getLongAttributes().remove(attrKey);
                } else node.setLongAttribute(attrKey, text);                
            }
            
            nodes.add(node);            
            count++;
            if (count % Constants.HIBERNATE_BATCH_SIZE == 0){
            	nodeDao.save(nodes);
            	logger.debug(count + " nodes saved.");
            	
            	nodes = new ArrayList<Node>();
            }
            
            line = reader.readLine();
        }
		
		// persist the node and attributes
		nodeDao.save(nodes);
		logger.debug(count + " nodes saved.");
	}
	
	/**
	 * For question type of choice/rating/duration
	 * @param reader
	 * @param question
	 * @param options
	 * @throws Exception
	 */
	private void readNode(BufferedReader reader, Question question, Map<String, String> options) throws Exception {        
		// prepare node map for quick lookup
        Map<String, Node> usernameToNodeMap = getVisibleUsernameToNodeMap(question, Constants.FETCH);
		
        // get fields from header
        logger.debug("reading headers...");        
        List<Field> fields = new ArrayList<Field>();
        String line = reader.readLine();
        String[] fieldNames = line.split("\t", -1);
        for (int i=1; i<fieldNames.length; i++){
        	String fieldName = fieldNames[i];
            Field field = question.getFieldByName(fieldName);
            if (field == null) {
                throw new Exception("Syntax error: the field(" + fieldName + ") doesn't exist!");
            }
            fields.add(field);
        }

        // process node's response
        logger.debug("reading each row (each node)");
        line = reader.readLine();
        int count = 0;
        List<Node> nodes = new ArrayList<Node>();
        while (line != null){
            line = line.trim();
            List<String> scales = StringUtil.splitAsList(line, "\t");
            
    		// the first item is username
    		String username = scales.get(0);
    		Node node = usernameToNodeMap.get(username);
    		if (node == null){
    			node = nodeDao.loadByUsername(username);
            	if (node == null) {
            		throw new Exception("cannot find node with username=" + username);
            	}
    			usernameToNodeMap.put(username, node);
    		}
    		
            // node by node
    		for (int i = 1; i < scales.size(); i++){
    		    String value = scales.get(i);
    		    if (value.equals(Constants.NOT_ANSWERED)) continue;
    		    
    		    Field field = fields.get(i-1);
    		    String attrKey = question.makeFieldKey(field);    		   
    		    if (question.isRating()){
    			    if (value.equals("-1")) node.getAttributes().remove(attrKey);
    			    else {
    			    	Scale scale = question.getScaleByName(value); 
    			        if (scale == null) {
    			            throw new Exception("Syntax error: the scale(" + value + ") doesn't exist!");
    			        }
    			        String attrValue = question.makeScaleKey(scale);
    			        node.setAttribute(attrKey, attrValue);
    			    }
    		    } else if (question.isChoice() || question.isDuration()){
    			    if (value.equals("0")) node.getAttributes().remove(attrKey);
    			    else node.setAttribute(attrKey, value);
    		    } else if (question.isContinuous()){
    			    if (value.equals("-")) node.getAttributes().remove(attrKey);
    			    else node.setAttribute(attrKey, value);
    		    } else {
                	throw new Exception("question type mismatched.");
    		    }
    		}
            
    		nodes.add(node);
            count++;            
            if (count % Constants.HIBERNATE_BATCH_SIZE == 0){
            	nodeDao.save(nodes);
            	logger.debug(count + " node saved.");
            	nodes = new ArrayList<Node>();
            }
            
            line = reader.readLine();
        }
		
		// persist the node and attributes
        nodeDao.save(nodes);
		logger.debug(count + " node saved.");
	}
	
	/**
	 * For question type of multiple choice/rating
	 * @param reader
	 * @param question
	 * @param options
	 * @throws Exception
	 */
	private void readNodeMultiple(BufferedReader reader, Question question, Map<String, String> options) throws Exception {        
		// prepare node map for quick lookup
        Map<String, Node> usernameToNodeMap = getVisibleUsernameToNodeMap(question, Constants.FETCH);
		
        // get fields from header
        logger.debug("reading headers...");        
        List<Field> fields = new ArrayList<Field>();
        String line = reader.readLine();
        String[] fieldNames = line.split("\t", -1);
        for (int i=1; i<fieldNames.length; i++){
        	String fieldName = fieldNames[i];
            Field field = question.getFieldByName(fieldName);
            if (field == null) {
                throw new Exception("Syntax error: the field(" + fieldName + ") doesn't exist!");
            }
            fields.add(field);
        }

        // process node's response
        logger.debug("reading main content");
        List<Node> nodes = new ArrayList<Node>();
        int count = 0;
        while (line != null){        	
        	String tfname = line.split("\t", -1)[0];
        	TextField tf = question.getTextFieldByName(tfname);
        	logger.debug("***** text field: " + tfname);
        	
        	line = reader.readLine();
            while (line != null && !line.startsWith("####")){
                line = line.trim();
                List<String> scales = StringUtil.splitAsList(line, "\t");
                
        		// the first item is username
        		String username = scales.get(0);
        		Node node = usernameToNodeMap.get(username);
        		if (node == null){
        			node = nodeDao.loadByUsername(username);
                	if (node == null) {
                		throw new Exception("cannot find node with username=" + username);
                	}
        			usernameToNodeMap.put(username, node);
        		}
        		
                // node by node
        		for (int i = 1; i < scales.size(); i++){
        		    String value = scales.get(i);
        		    if (value.equals(Constants.NOT_ANSWERED)) continue;
        		    
        		    Field field = fields.get(i-1);
        		    String attrKey = question.makeFieldsKey(field, tf);       		    
        		    if (question.isMultipleRating()){
        			    if (value.equals("-1")) node.getAttributes().remove(attrKey);
        			    else {
        			    	Scale scale = question.getScaleByName(value); 
        			        if (scale == null) {
        			            throw new Exception("Syntax error: the scale(" + value + ") doesn't exist!");
        			        }
        			        String attrValue = question.makeScaleKey(scale);
        			        node.setAttribute(attrKey, attrValue);
        			    }
        		    } else if (question.isMultipleChoice()){
        			    if (value.equals("0")) node.getAttributes().remove(attrKey);
        			    else node.setAttribute(attrKey, value);
        		    } else {
                    	throw new Exception("question type mismatched.");
        		    }
        		}
                
        		nodes.add(node);
        		count++;
        		if (count % Constants.HIBERNATE_BATCH_SIZE == 0){
        			nodeDao.save(nodes);
        			nodes = new ArrayList<Node>();
        			logger.info(count + " nodes saved.");
        		}
        		
                line = reader.readLine();
            }
            
            nodeDao.save(nodes);
            logger.info(count + " nodes saved.");
            nodes = new ArrayList<Node>();
            count = 0;
            
            line = reader.readLine();
        }
	}	
	
	/**
	 * Reading contact chooser answers
	 * After updating the nodes, client side data may be out of sync and exception occur. Refresh needed.
	 * @param reader
	 * @param question
	 * @param options
	 * @throws Exception
	 */
	private void readContactChooser(BufferedReader reader, Question question, Map<String, String> options) throws Exception { 
		String format = options.get(Constants.IO_OUTPUT_FORMAT);
		logger.info("Reading contact chooser (name=" + question.getShortName() + ") with format=" + format);
		
		// prepare node/group map for quick lookup
		Map<String, Node> usernameToNodeMap = getAvailableUsernameToNodeMap(question, Constants.FETCH);		
		
        GroupDao groupDao = (GroupDao) Beans.getBean("groupDao");        
        List<Group> allGroups = groupDao.getAll();
        Map<String, Group> groupMap = new HashMap<String, Group>();
        for (Group group : allGroups){
        	groupMap.put(group.getName().trim(), group);
        }
                
        logger.debug("reading main content");
        if (format.equals("list")){
	        String line = reader.readLine();
	        while (line != null) {
				String[] parts = line.trim().split("\t", -1);
	
				// the first item is username for private group
				String username = parts[0].trim();				
				Node node = getNodeByUsername(username, usernameToNodeMap); // test whether node exists
				String privateGroupName = Group.getPrivateGroupName(username, question.getShortName());
				logger.debug("private group: " + privateGroupName);
				Group group = groupMap.get(privateGroupName);
				if (group == null){
					logger.warn("Private group for user: " + username + " does not exist.");
					group = new Group();
					group.setName(privateGroupName);
					groupDao.save(group);
					groupMap.put(privateGroupName, group);
					logger.warn("New group: " + privateGroupName + " is created.");
				}						
	
				// private group members
				Set<Node> availableNodes = new HashSet<Node>(usernameToNodeMap.values());
				logger.debug("members: " + Arrays.asList(parts));
				for (int i=1; i<parts.length; i++){
					username = parts[i].trim();
					node = getNodeByUsername(username, usernameToNodeMap);
					node.getGroups().add(group);
					availableNodes.remove(node);
				}
				
				// all those availableNodes who do not appear in file are considered NOT in the private group
				for (Node n : availableNodes){
					n.getGroups().remove(group);
				}
				
				line = reader.readLine();
			}
        } else {
	        String line = reader.readLine();
	        String[] usernames = line.split("\t", -1);
	        line = reader.readLine();
	        while (line != null) {
	        	String[] parts = line.trim().split("\t", -1);
	
				// the first item is username for private group
				String username =  parts[0].trim();
				Node node = getNodeByUsername(username, usernameToNodeMap); // test whether node exists
				String privateGroupName = Group.getPrivateGroupName(username, question.getShortName());
				logger.debug("private group: " + privateGroupName);
				Group group = groupMap.get(privateGroupName);
				if (group == null){
					logger.warn("Private group for user: " + username + " does not exist.");
					group = new Group();
					group.setName(privateGroupName);
					groupDao.save(group);
					groupMap.put(privateGroupName, group);
					logger.warn("New group: " + privateGroupName + " is created.");
				}						
	
				// private group members
				List<String> members = new LinkedList<String>();
				for (int i=1; i<parts.length; i++){
					username =  usernames[i].trim();
					node = getNodeByUsername(username, usernameToNodeMap);
					
					String value = parts[i].trim();	
					if (value.equals("0")) node.getGroups().remove(group);
					else {
						node.getGroups().add(group);
						members.add(username);
					}
				}
				
				logger.debug("members: " + members);
				
				line = reader.readLine();
			}
        }        
        
		// persist the node and attributes
        Collection<Node> nodes = usernameToNodeMap.values();
		nodeDao.save(nodes);
		logger.debug(nodes.size() + " nodes saved.");
	}		
	
	/**
	 * 
	 * @param reader
	 * @param question
	 * @param options
	 * @throws Exception
	 */
	private void readContactProvider(BufferedReader reader, Question question, Map<String, String> options) throws Exception { 
		// prepare node/group map for quick lookup
		Map<String, Node> usernameToNodeMap = getAvailableUsernameToNodeMap(question, Constants.FETCH);		
		
        GroupDao groupDao = (GroupDao) Beans.getBean("groupDao");        
        List<Group> allGroups = groupDao.getAll();
        Map<String, Group> groupMap = new HashMap<String, Group>();
        for (Group group : allGroups){
        	groupMap.put(group.getName().trim(), group);
        }
                
        logger.debug("reading main content");
        String line = reader.readLine();
        while (line != null) {
			String[] parts = line.trim().split("\t", -1);

			// the first item is username for provider group
			String username = parts[0].trim();				
			Node node = getNodeByUsername(username, usernameToNodeMap); // test whether node exists
			String providerGroupName = Group.getProviderGroupName(username, question.getShortName());
			logger.debug("provider group: " + providerGroupName);
			Group group = groupMap.get(providerGroupName);
			if (group == null){
				logger.warn("provider group for user: " + username + " does not exist.");
				group = new Group();
				group.setName(providerGroupName);
				groupDao.save(group);
				groupMap.put(providerGroupName, group);
				logger.warn("New group: " + providerGroupName + " is created.");
			}						

			// provider group members
			Set<Node> availableNodes = new HashSet<Node>(usernameToNodeMap.values());
			logger.debug("members: " + Arrays.asList(parts));
			for (int i=1; i<parts.length; i++){
				username = parts[i].trim();
				node = getNodeByUsername(username, usernameToNodeMap);
				node.getGroups().add(group);
				availableNodes.remove(node);
			}
			
			// all those availableNodes who do not appear in file are considered NOT in the provider group
			for (Node n : availableNodes){
				n.getGroups().remove(group);
			}
			
			line = reader.readLine();
		}
      
		// persist the node and attributes
        Collection<Node> nodes = usernameToNodeMap.values();
		nodeDao.save(nodes);
		logger.debug(nodes.size() + " nodes saved.");
	}
	
	
	
	
	private Node getNodeByUsername(String username, Map<String, Node> usernameToNodeMap) throws Exception{
		Node node = usernameToNodeMap.get(username);
		if (node == null){
			node = nodeDao.loadByUsername(username);
        	if (node == null) {
        		throw new Exception(">> missing node: " + username);
        	}                	
        	usernameToNodeMap.put(username, node);
		}
		return node;
	}
	
	private Map<String, Node> getVisibleUsernameToNodeMap(Question question, String depth){
		List<Node> nodes = nodeDao.findByIds(question.getVisibleNodeIds());
		return getUsernameToNodeMap(nodes, depth);
	}
	
	private Map<String, Node> getAvailableUsernameToNodeMap(Question question, String depth){
		List<Node> nodes = nodeDao.findByIds(question.getAvailableNodeIds(false));
		return getUsernameToNodeMap(nodes, depth);
	}
	
//	private Map<String, Node> getAvailableUsernameToNodeMap2(Question question, String depth){
//		Set<User> users = question.getAvailableUsers2();
//		return getUsernameToNodeMap(users, depth);
//	}
	
	private Map<String, Node> getUsernameToNodeMap(List<Node> nodes, String depth){
		Map<String, Node> nodeMap = new HashMap<String, Node>();
		
		for (Node node : nodes){
			if (depth.equals(Constants.PROXY)) nodeMap.put(node.getUsername().trim(), node);
			else if (depth.equals(Constants.NORMAL)) nodeMap.put(node.getUsername().trim(), nodeDao.findById(node.getId()));
			else if (depth.equals(Constants.FETCH)) nodeMap.put(node.getUsername().trim(), nodeDao.loadById(node.getId()));
		}
		
		return nodeMap;
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
	
}
