package ciknow.domain;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ciknow.dao.EdgeDao;
import ciknow.dao.NodeDao;
import ciknow.ro.GenericRO;
import ciknow.util.Beans;
import ciknow.util.Constants;
import ciknow.util.VisUtil;

public class Visualization implements java.io.Serializable {
	private static Log logger = LogFactory.getLog(Visualization.class);
	
	public static final String CRITERIA = "query";
	public static final String RESULT = "result";
	private static final long serialVersionUID = -5914261815716091004L;
	private Long id;
	private Long version;
	private Node creator;
	private String name;
	private String label;
	private String type; // query, result, layout
	private String networkType; // local, custom, recommender
	private String data;
	private boolean valid = true;
	private Date timestamp;
	private Set<Group> groups = new HashSet<Group>();
	private Set<Node> nodes = new HashSet<Node>();
	private Map<String, String> attributes = new HashMap<String, String>();
	
	public Visualization() {
		super();
	}

	public String validate(List<String> allEdgeTypes, List<String> allAttrNames, Map<String, Question> nameToQuestionMap, Map<Long, Question> idToQuestionMap){
		Beans.init();
		NodeDao nodeDao = (NodeDao)Beans.getBean("nodeDao");
		EdgeDao edgeDao = (EdgeDao)Beans.getBean("edgeDao");
		GenericRO genericRO = (GenericRO)Beans.getBean("genericRO");
		
		StringBuilder sb = new StringBuilder();
		if (type.equalsIgnoreCase("query")){
			logger.debug("validating query --- " + data);
			List<String> selectedEdgeTypes = getEdgeTypesFromQuery(data);
			selectedEdgeTypes.retainAll(allEdgeTypes);
			Collection<String> attrNames = nodeDao.getAttributeNames(edgeDao.getNodeTypesByEdgeTypes(selectedEdgeTypes));
			
			String[] parts = data.split("&", -1);
			String[] subparts;
			String key, value;
			
			for (String part : parts){
				subparts = part.split("=", -1);
				key = subparts[0];
				value = subparts[1];
				
				if (key.equals("node_id") && networkType.equals("local")){
					if (nodeDao.findById(Long.parseLong(value)) == null) {
						sb.append("Invalid: ").append(part).append("\n");
					}
				} else if (key.equals("edgeType")){
					if (!allEdgeTypes.contains(value)) {
						sb.append("Invalid: ").append(part).append("\n");
					}
				} else if (key.equals("sizeQuestion") || key.equals("sizeQuestion2")){
					if (idToQuestionMap.get(Long.parseLong(value)) == null) {
						sb.append("Invalid: ").append(part).append("\n");
					}
				} else if (key.equals("colorQuestion") || key.equals("groupQuestion") || key.equals("shapeQuestion")){
					if (!isValidColorableAttribute(value, attrNames, nameToQuestionMap, idToQuestionMap)) {
						sb.append("Invalid: ").append(part).append("\n");
					}
				} else if (key.equals("displayAttr")){
					if (!isValidDisplaybleAttribute(value, allAttrNames)) {
						sb.append("Invalid: ").append(part).append("\n");
					}
				} else if (key.equals("nodeFilter")){
					String[] conditions = value.split("-.-", -1);
					try{
						String attrName = URLDecoder.decode(conditions[0], "UTF-8");
						String attrValue = URLDecoder.decode(conditions[2], "UTF-8");
						if (!isValidFilterableAttribute(attrName, allAttrNames, nameToQuestionMap, idToQuestionMap)) {							
							sb.append("Invalid node filter attrName: ").append(attrName).append("\n");
							continue;
						}
						
						List<Map<String, String>> attrValues = genericRO.getAttributeValues(attrName);
						boolean valid = false;
						for (Map<String, String> item : attrValues){
							if (item.get("value").equals(attrValue)) {
								valid = true; 
								break;
							}
						}
						if (!valid) {							
							sb.append("Invalid node filter attrValue: ").append(attrValue).append("\n");
						}
					} catch(Exception e){
						sb.append(e.getMessage()).append("\n");
					}
				} else if (key.equals("edgeFilter")) {
					String[] conditions = value.split("-.-", -1);
					try {
						String attrName = URLDecoder.decode(conditions[0],"UTF-8");
						String attrValue = URLDecoder.decode(conditions[2],"UTF-8");
						if (!edgeDao.getAttributeNames().contains(attrName)) {
							sb.append("Invalid edge filter attrName: ").append(attrName).append("\n");
							continue;
						}

						if (!edgeDao.getAttributeValues(attrName).contains(attrValue)){
							sb.append("Invalid edge filter attrValue: ").append(attrValue).append("\n");
						}
					} catch (Exception e) {
						sb.append(e.getMessage()).append("\n");
					}
				}
			}
		}	
		
		return sb.toString();
	}
	
	private List<String> getEdgeTypesFromQuery(String query){
		String[] parts = query.split("&", -1);
		String[] subparts;
		String key, value;
		
		List<String> edgeTypes = new LinkedList<String>();
		for (String part : parts){
			subparts = part.split("=", -1);
			key = subparts[0];
			value = subparts[1];
			
			if (key.equals("edgeType")){
				edgeTypes.add(value);
			}
		}
		
		return edgeTypes;
	}
	
	private boolean isValidColorableAttribute(String value, Collection<String> attrNames, Map<String, Question> nameToQuestionMap, Map<Long, Question> idToQuestionMap){
		List<String> nodeProperties = Arrays.asList(Constants.COLORABLE_PROPERTIES);		
		if (nodeProperties.contains(value)) return true;
		else if (value.startsWith(VisUtil.ATTR_PREFIX)){
			value = value.substring(VisUtil.ATTR_PREFIX.length());
			
			if (!attrNames.contains(value)) return false;
			
			if (value.startsWith("F" + Constants.SEPERATOR)){
    			String shortName = Question.getShortNameFromKey(value);
    			Question question = nameToQuestionMap.get(shortName);
    			if (question == null || !question.isRating()) return false;

				String fieldName = Question.getFieldNameFromKey(value);
				Field field = question.getFieldByName(fieldName);
				if (field == null) return false;
			} else if (value.startsWith("FT" + Constants.SEPERATOR)){
    			String shortName = Question.getShortNameFromKey(value);
    			Question question = nameToQuestionMap.get(shortName);
    			if (question == null || !question.isMultipleRating()) return false;

				String fieldName = Question.getFieldNameFromKey(value);
				Field field = question.getFieldByName(fieldName);
				if (field == null) return false;
				
				String tfName = Question.getTextFieldNameFromFT(value);
				TextField tf = question.getTextFieldByName(tfName); 
				if (tf == null) return false;
			} else {
				// nothing
			}
		} else if (value.startsWith(VisUtil.QUESTION_PREFIX)){
			value = value.substring(VisUtil.QUESTION_PREFIX.length());
			
			Question q = idToQuestionMap.get(Long.parseLong(value));
			
			// this question has been deleted?
			if (q == null) return false; 
			Collection<String> questionAttrNames = q.getPossibleAttributeNames();
			questionAttrNames.retainAll(attrNames);
			
			// this question's possible attrNames are not eligible for color/shape/grouping?
			if (questionAttrNames.size() == 0) return false;
		} else return false;
		
		return true;
	}
	
	private boolean isValidDisplaybleAttribute(String value, Collection<String> attrNames){
		if (value.equals("NODE_LABEL")) return true;
		else if (attrNames.contains(value)) return true;
		else return false;
	}
	
	/*
	 * difference from "isValidColorableAttribute":
	 * 1, attrNames are all attribute names instead of restrained by nodeTypes
	 * 2, filterable attributes also include those Choice question type with multiple choices
	 */
	private boolean isValidFilterableAttribute(String value, Collection<String> attrNames, Map<String, Question> nameToQuestionMap, Map<Long, Question> idToQuestionMap){
		if (value.startsWith(VisUtil.ATTR_PREFIX)){
			value = value.substring(VisUtil.ATTR_PREFIX.length());
			
			if (!attrNames.contains(value)) return false;
			
			if (value.startsWith("F" + Constants.SEPERATOR)){
    			String shortName = Question.getShortNameFromKey(value);
    			Question question = nameToQuestionMap.get(shortName);
    			if (question == null || !question.isRating()) return false;

				String fieldName = Question.getFieldNameFromKey(value);
				Field field = question.getFieldByName(fieldName);
				if (field == null) return false;
			} else if (value.startsWith("FT" + Constants.SEPERATOR)){
    			String shortName = Question.getShortNameFromKey(value);
    			Question question = nameToQuestionMap.get(shortName);
    			if (question == null || !question.isMultipleRating()) return false;

				String fieldName = Question.getFieldNameFromKey(value);
				Field field = question.getFieldByName(fieldName);
				if (field == null) return false;
				
				String tfName = Question.getTextFieldNameFromFT(value);
				TextField tf = question.getTextFieldByName(tfName); 
				if (tf == null) return false;
			} else {
				// nothing
			}
		} else if (value.startsWith(VisUtil.QUESTION_PREFIX)){
			value = value.substring(VisUtil.QUESTION_PREFIX.length());
			
			Question q = idToQuestionMap.get(Long.parseLong(value));
			
			// this question has been deleted?
			if (q == null) return false; 
			Collection<String> questionAttrNames = q.getPossibleAttributeNames();
			questionAttrNames.retainAll(attrNames);
			
			// this question's possible attrNames are not eligible for color/shape/grouping?
			if (questionAttrNames.size() == 0) return false;
		} else return false;
		
		return true;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public Node getCreator() {
		return creator;
	}

	public void setCreator(Node creator) {
		this.creator = creator;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getNetworkType() {
		return networkType;
	}

	public void setNetworkType(String networkType) {
		this.networkType = networkType;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public Set<Group> getGroups() {
		return groups;
	}

	public void setGroups(Set<Group> groups) {
		this.groups = groups;
	}

	public Set<Node> getNodes() {
		return nodes;
	}

	public void setNodes(Set<Node> nodes) {
		this.nodes = nodes;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Visualization other = (Visualization) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
