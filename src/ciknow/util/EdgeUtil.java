package ciknow.util;

import static ciknow.util.Constants.*;
import ciknow.dao.EdgeDao;
import ciknow.dao.QuestionDao;
import ciknow.domain.Edge;
import ciknow.domain.Field;
import ciknow.domain.Node;
import ciknow.domain.Question;
import ciknow.domain.Scale;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author gyao
 */
public class EdgeUtil {
	private static final Log logger = LogFactory.getLog(EdgeUtil.class);
	
	/**
	 * Get question shortName from edgeType
	 * @param edgeType
	 * @return
	 */
    public static String getShortNameFromEdgeType(String edgeType){
    	// tagging, perceived choice/rating
    	if (edgeType.startsWith(Constants.TAGGING_PREFIX)){
    		return edgeType.substring(Constants.TAGGING_PREFIX.length());
    	}
    	
		int index = edgeType.lastIndexOf(Constants.SEPERATOR);
		if (index > 0) {	// relational choice/rating multiple
			return edgeType.substring(0,index);
		} else { 			// relational choice/rating/continuous, perceived relational choice/rating
			return edgeType;
		}
    }
    
    /**
     * Get question fieldName from edgeType (for relational choice/rating only)
     * @param edgeType
     * @return
     */
    public static String getFieldNameFromEdgeType(String edgeType) {
    	if (edgeType.startsWith(Constants.TAGGING_PREFIX)){
    		return null;
    	}
    	
    	int index = edgeType.lastIndexOf(Constants.SEPERATOR);
		if (index > 0) {	// relational choice/rating multiple
			return edgeType.substring(index+1);
		} else return null;
    }
    
    /**
     * Update edge weight based on "scale" attribute
     * The edge must belong to the specified question
     * @param e
     * @param question
     */
    public static void updateEdgeWeight(Edge e, Question question) {
    	if (question.isRelationalRating()
    		|| question.isRelationalRatingMultiple()
    		|| question.isPerceivedRelationalRating()){
            String scaleKey = e.getAttribute(Constants.SCALE_KEY);
            if (scaleKey == null){
            	logger.warn("Edge(id=" + e.getId() + ") does not has a 'scale' attribute");
            	return;
            }
            String scaleName = Question.getScaleNameFromKey(scaleKey);
            Scale scale = question.getScaleByName(scaleName);
            if (scale == null){
            	logger.warn("Question(id=" + question.getId() + ") does not has scale with name=" + scaleName);
            	return;
            }
            e.setWeight(scale.getValue());
    	}
    }
	
	public static void updateEdgeWeights() throws Exception{
		logger.info("updating edge weights for question with ratings ...");
		
		Beans.init();
		QuestionDao questionDao = (QuestionDao) Beans.getBean("questionDao");
		EdgeDao edgeDao = (EdgeDao) Beans.getBean("edgeDao");
		
		// create edge type to question map
		List<Question> questions = questionDao.getAll();
		Map<String, Question> questionMap = new HashMap<String, Question>(); 
		for (Question q : questions){
			String type = q.getType();
			if (type.equals(Constants.PERCEIVED_RELATIONAL_RATING) 
					|| type.equals(Constants.RELATIONAL_RATING)){
				
				questionMap.put(q.getEdgeType(), q);
			} else if (type.equals(Constants.RELATIONAL_RATING_MULTIPLE)){
				for (Field field : q.getFields()){
					questionMap.put(q.getEdgeTypeWithField(field), q);
				}
			}
		}
		
		// update edge weights
		for (String edgeType : questionMap.keySet()){
			Question question = questionMap.get(edgeType);
			List<Edge> edges = edgeDao.loadByType(edgeType, true);
						
			for (Edge edge : edges){
				updateEdgeWeight(edge, question);
			}
			
			logger.info("saving updated edges of type: " + edgeType);
			edgeDao.save(edges);
		}
		
		logger.info("edge weights updated.");
	}
	
	
	public static Set<String> getDistinctEdgeTypes(Collection<Edge> edges) {
		Set<String> edgeTypes = new TreeSet<String>();
		for (Edge e : edges) {
			String edgeType = e.getType();
			edgeTypes.add(edgeType);
		}
		return edgeTypes;
	}
	
	public static Map<String, Question> getEdgeTypeQuestionMap(List<Question> questions){
		logger.info("get edgeType to questions map...");
		
		Map<String, Question> questionMap = new HashMap<String, Question>(); 
		for (Question q : questions){
			String type = q.getType();
			if (type.equals(RELATIONAL_RATING_MULTIPLE) || type.equals(RELATIONAL_CHOICE_MULTIPLE)){				
				for (Field field : q.getFields()){
					questionMap.put(q.getEdgeTypeWithField(field), q);
				}
			} else if(type.equals(RELATIONAL_CHOICE)
					|| type.equals(RELATIONAL_RATING)
					|| type.equals(RELATIONAL_CONTINUOUS)
					|| type.equals(PERCEIVED_RELATIONAL_CHOICE)
					|| type.equals(PERCEIVED_RELATIONAL_RATING)
					|| type.equals(PERCEIVED_CHOICE)
					|| type.equals(PERCEIVED_RATING)) {
				questionMap.put(q.getEdgeType(), q);
			} else logger.debug("ignore question(shortName=" + q.getShortName() + ", type=" + type + ").");
		}
		
		logger.info("got edgeTypeQuestionMap.");
		
		return questionMap;
	}
	

	
	
	/*******************************************************************************
	 * QuestionWriter.java
	 ******************************************************************************/
    /**
     * getEdgesByFromNode
     * @param edges		- list of edges for a question
     * @param fromNode
     * @return
     */
    public static List<Edge> getEdgesByFromNode(List<Edge> edges, Node fromNode) {
        List<Edge> list = new ArrayList<Edge>();
        for (Edge edge : edges) {
            if (edge.getFromNode().equals(fromNode)) {
                list.add(edge);
            }
        }
        return list;
    }
    
    public static List<Edge> getEdgesByFromNodeAndToNode(List<Edge> edges, Node fromNode, Node toNode) {
        List<Edge> list = new ArrayList<Edge>();
        for (Edge edge : edges) {
            if (edge.getFromNode().equals(fromNode)) {
                if (edge.getToNode().equals(toNode)) {
                    list.add(edge);
                }
            }
        }
        return list;
    }
    
    /**
     * getEdgesByCreatorAndFromNode
     * @param edges		- list of edges for a question
     * @param creator
     * @param fromNode
     * @return
     */
    public static List<Edge> getEdgesByCreatorAndFromNode(List<Edge> edges, Node creator, Node fromNode) {
        List<Edge> list = new ArrayList<Edge>();
        for (Edge edge : edges) {
            if (edge.getCreator().equals(creator)) {
                if (edge.getFromNode().equals(fromNode)) {
                    list.add(edge);
                }
            }
        }
        return list;
    }
    
    /**
     * getEdgesByCreatorAndToNode
     * @param edges		- list of edges for a question
     * @param creator
     * @param fromNode
     * @return
     */
    public static List<Edge> getEdgesByCreatorAndToNode(List<Edge> edges, Node creator, Node toNode) {
        List<Edge> list = new ArrayList<Edge>();
        for (Edge edge : edges) {
            if (edge.getCreator().equals(creator)) {
                if (edge.getToNode().equals(toNode)) {
                    list.add(edge);
                }
            }
        }
        return list;
    }
    
	/**
	 * For Perceived Choice/Rating questions
	 * @param taggings	- list of edges/taggings for a question
	 * @param creator
	 * @param field
	 * @return
	 */
    public static List<Edge> getTaggingsByCreatorAndField(List<Edge> taggings, Node creator, Field field) {
        List<Edge> list = new ArrayList<Edge>();
        for (Edge tagging : taggings) {
            if (tagging.getCreator().equals(creator)) {
                if (tagging.getToNode().getUsername().indexOf(Constants.SEPERATOR + field.getName()) > 0) {
                    list.add(tagging);
                }
            }
        }
        return list;
    }
}
