package ciknow.vis;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ciknow.dao.EdgeDao;
import ciknow.dao.NodeDao;
import ciknow.dao.QuestionDao;
import ciknow.domain.Edge;
import ciknow.domain.Field;
import ciknow.domain.Node;
import ciknow.domain.Question;
import ciknow.domain.Scale;
import ciknow.domain.TextField;
import ciknow.ro.GenericRO;
import ciknow.util.Beans;
import ciknow.util.GeneralUtil;

public class EdgeDetails {
	private static Log logger = LogFactory.getLog(EdgeDetails.class);
	
	private NodeDao nodeDao = null;
	private EdgeDao edgeDao = null;
	private QuestionDao questionDao = null;
	private GenericRO genericRO = null;
	
	List<Question> questions = null;
	private Node fnode = null;
	private Node tnode = null;
	private String edgeType = null;
	
	List<Map<String, String>> eds = null; // edge descriptions (type, label, verb)
	private int count = 0;
	private int toggleIndex = 0;
	//private Set<String> usedKeys = new HashSet<String>();
	
	public static void main(String[] args){
		EdgeDetails ed = new EdgeDetails(8L, 11L, "d.PAuthorship.PArticle by Subject Category");
		logger.info(ed.toHtml());
	}
	
	public EdgeDetails(Long fromNodeId, Long toNodeId, String edgeType){
		Beans.init();
		nodeDao = (NodeDao)Beans.getBean("nodeDao");
		edgeDao = (EdgeDao)Beans.getBean("edgeDao");
		questionDao = (QuestionDao)Beans.getBean("questionDao");
		genericRO = (GenericRO)Beans.getBean("genericRO");
		
		fnode = nodeDao.loadById(fromNodeId);
		tnode = nodeDao.loadById(toNodeId);
		questions = questionDao.getAll();
		
		eds = GeneralUtil.getEdgeDescriptions();
	}
	
	public String toHtml(){
    	StringBuilder sb = new StringBuilder();
    	
    	String html = getDirectRelations();
    	sb.append(getHeaderLine("Direct Relations"));
    	sb.append(html);
    	
    	html = getQuestionAnswers();
    	if (html.length() != 0) {
    		sb.append(getHeaderLine("Survey Question Answers"));
    		sb.append(html);
    	}
    	
    	html = getIndirectRelations();
    	if (html.length() != 0) {
    		sb.append(getHeaderLine("Indirect relation: They have the following nodes in common in the Knowledge Network"));
    		sb.append(html);
    	}	
    	
    	return sb.toString();	
	}
	
	private String getDirectRelations(){
		logger.info("get direct relations...");
		StringBuilder sb = new StringBuilder();
		
		// get all edges between fromNode and toNode
		List<Edge> edges = edgeDao.loadByFromToNodeId(fnode.getId(), tnode.getId());
		edges.addAll(edgeDao.loadByFromToNodeId(tnode.getId(), fnode.getId()));
			
		Set<Triple> triples = getTripleSet(edges);
		
		// the only use of instance variable 'edgeType': show it first
		for (Triple triple : triples){
			if (!triple.edge.getType().equals(edgeType)) continue;
			triple.setLinkFromNode(true);
			triple.setLinkToNode(true);
			
			sb.append("<tr><td colspan=2>");
			sb.append(triple.toString());
			sb.append("</td></tr>");
		}
		
		// show other type of edges
		for (Triple triple : triples){
			if (triple.edge.getType().equals(edgeType)) continue;
			triple.setLinkFromNode(true);
			triple.setLinkToNode(true);
			
			sb.append("<tr><td colspan=2>");
			sb.append(triple.toString());
			sb.append("</td></tr>");
		}
		
		return sb.toString();
	}
	
	private String getQuestionAnswers(){
    	logger.info("get question answers ...");
    	StringBuilder sb = new StringBuilder();
    	
    	// get eligible questions
    	List<Question> qs = new LinkedList<Question>();
    	for (Question q : questions){
    		if (q.isChoice()) qs.add(q);
    		if (q.isRating()) qs.add(q);
    		if (q.isDuration()) qs.add(q);
    		if (q.isText()) qs.add(q);
    		if (q.isTextLong()) qs.add(q);
    		if (q.isMultipleChoice()) qs.add(q);
    		if (q.isMultipleRating()) qs.add(q);
    	}
    	
    	// only process eligible questions which are visible to both fromNode and toNode
    	Map<Question, Set<Long>> map = Question.getQuestionVisibleNodeIdsMap(qs);    	
    	for (Question question : qs){
    		Set<Long> visibleNodeIds = map.get(question);

    		if (!visibleNodeIds.contains(fnode.getId())) {
    			logger.debug(fnode.getUsername() + " is not visible to question(shortName=" + question.getShortName() + "). Question is ignored.");
    			continue;
    		}
    		if (!visibleNodeIds.contains(tnode.getId())) {
    			logger.debug(tnode.getUsername() + " is not visible to question(shortName=" + question.getShortName() + "). Question is ignored.");
    			continue;
    		}
    		
    		if (question.isChoice()) sb.append(getChoice(question));
    		else if (question.isRating()) sb.append(getRating(question));
    		else if (question.isText()) sb.append(getText(question));
    		else if (question.isTextLong()) sb.append(getTextLong(question));
    		else if (question.isMultipleChoice()) sb.append(getMultipleChoice(question));
    		else if (question.isMultipleRating()) sb.append(getMultipleRating(question));
    		else if (question.isDuration()) sb.append(getDuration(question));
    	}
    	
    	return sb.toString();
	}
	
	private String getIndirectRelations(){
		logger.debug("get indirect relations...");
		StringBuilder sb = new StringBuilder();
		
		Set<Node> commonNodes = new HashSet<Node>();
		List<Node> nodes = new LinkedList<Node>();
		nodes.add(fnode);
		commonNodes.addAll(nodeDao.findNeighbors(nodes, true, false, new LinkedList<String>())); // includeDerivedEdges=true
		
		nodes.clear();
		nodes.add(tnode);
		commonNodes.retainAll(nodeDao.findNeighbors(nodes, true, false, new LinkedList<String>()));
		
		Map<String, List<Node>> typeToNodesMap = new TreeMap<String, List<Node>>();
		for (Node node : commonNodes){
			List<Node> list = typeToNodesMap.get(node.getType());
			if (list == null){
				list = new LinkedList<Node>();
				typeToNodesMap.put(node.getType(), list);
			}
			list.add(node);
		}
		
		int nodeCount = 0;
		for (String nodeType : typeToNodesMap.keySet()){
			StringBuilder edgesBuf = new StringBuilder();
			List<Node> list = typeToNodesMap.get(nodeType);
			edgesBuf.append("<ul id='").append(++toggleIndex).append("' style='display:none'>");
			for (Node node : list){
				nodeCount++;
				edgesBuf.append("<li>");
				
				// get all edges between fromNode and node
				List<Edge> edges = edgeDao.loadByFromToNodeId(fnode.getId(), node.getId());
				edges.addAll(edgeDao.loadByFromToNodeId(node.getId(), fnode.getId()));
				
				// get all edges between toNode and node
				edges.addAll(edgeDao.loadByFromToNodeId(tnode.getId(), node.getId()));
				edges.addAll(edgeDao.loadByFromToNodeId(node.getId(), tnode.getId()));
				
				for (Triple triple : getTripleSet(edges)){
					boolean linkFromNode = triple.fromNodeLabel.equals(node.getLabel());
					triple.setLinkFromNode(linkFromNode);
					
					boolean linkToNode = triple.toNodeLabel.equals(node.getLabel());
					triple.setLinkToNode(linkToNode);
					
					edgesBuf.append(triple.toString()).append("<br>");
				}
				edgesBuf.append("</li>");
			}
			edgesBuf.append("</ul>");
			String leftDisplay = nodeType + "(" + list.size() + ")";
			String leftHtml = NodeDetails.getToggleLink(leftDisplay, toggleIndex);
			String rightHtml = edgesBuf.toString();
			sb.append(getTableLine(leftHtml, rightHtml));
		}
		logger.debug("there are " + nodeCount + " common nodes");
		
		return sb.toString();
	}

	private Set<Triple> getTripleSet(Collection<Edge> edges) {
		Set<Triple> tripleSet = new TreeSet<Triple>(); 
		for (Edge edge : edges){
			Triple triple = new Triple(edge);
			tripleSet.add(triple);
		}
		return tripleSet;
	}
	
	
    private String getTableLine(String key, String value){
    	count++;
    	StringBuilder sb = new StringBuilder();
    	if (count%2 == 0) sb.append("<tr class='evenEdgeLine'>");
    	else sb.append("<tr class='oddEdgeLine'>");
    	sb.append("<td class='leftCell'>").append(key).append("</td>");    	
    	sb.append("<td class='rightCell'>").append(value).append("</td>");
    	sb.append("</tr>\n");
    	return sb.toString();
    }
    
    private String getHeaderLine(String header){
    	count = 0;
    	StringBuilder sb = new StringBuilder();
    	sb.append("<tr class='edgeInfoHeader'>");
    	sb.append("<td colspan=2>").append(header).append("</td>");    
    	sb.append("</tr>\n");
    	return sb.toString();
    }
    
    private class Triple implements Comparable<Triple>{
    	private Edge edge;
    	private boolean linkFromNode;
    	private boolean linkToNode;
    	
    	private String fromNodeLabel;
    	private String toNodeLabel;
    	private String edgeTypeVerb;

    	
    	public Triple(Edge edge){
    		this(edge, false, false);
    	}
    	
    	public Triple(Edge edge, boolean linkFromNode, boolean linkToNode){
    		this.edge = edge;
			this.linkFromNode = linkFromNode;
			this.linkToNode = linkToNode;
			
    		this.fromNodeLabel = edge.getFromNode().getLabel();
    		this.toNodeLabel = edge.getToNode().getLabel();
			String edgeType = edge.getType();
			String edgeVerb = GeneralUtil.getEdgeVerb(eds, edgeType);
			if (edgeVerb == null) edgeVerb = edgeType;
			this.edgeTypeVerb = edgeVerb;
		}
    	
    	
		public boolean isLinkFromNode() {
			return linkFromNode;
		}

		public void setLinkFromNode(boolean linkFromNode) {
			this.linkFromNode = linkFromNode;
		}

		public boolean isLinkToNode() {
			return linkToNode;
		}

		public void setLinkToNode(boolean linkToNode) {
			this.linkToNode = linkToNode;
		}

		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((edgeTypeVerb == null) ? 0 : edgeTypeVerb.hashCode());
			result = prime * result
					+ ((fromNodeLabel == null) ? 0 : fromNodeLabel.hashCode());
			result = prime * result
					+ ((toNodeLabel == null) ? 0 : toNodeLabel.hashCode());
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
			final Triple other = (Triple) obj;
			if (edgeTypeVerb == null) {
				if (other.edgeTypeVerb != null)
					return false;
			} else if (!edgeTypeVerb.equals(other.edgeTypeVerb))
				return false;
			if (fromNodeLabel == null) {
				if (other.fromNodeLabel != null)
					return false;
			} else if (!fromNodeLabel.equals(other.fromNodeLabel))
				return false;
			if (toNodeLabel == null) {
				if (other.toNodeLabel != null)
					return false;
			} else if (!toNodeLabel.equals(other.toNodeLabel))
				return false;
			return true;
		}

		public int compareTo(Triple o) {
			int result = edgeTypeVerb.compareTo(o.edgeTypeVerb);
			if (result == 0) {
				result = fromNodeLabel.compareTo(o.fromNodeLabel);
				if (result == 0){
					return toNodeLabel.compareTo(o.toNodeLabel);
				} else return result;
			} else return result;
		}
		
		public String toString(){
			StringBuilder sb = new StringBuilder();
			String urlPrefix = genericRO.getBaseURL() + "/vis_get_node_info.jsp?node=";
			if (linkFromNode) {
				sb.append("<a href='").append(urlPrefix).append(edge.getFromNode().getId()).append("'>");
				sb.append(fromNodeLabel);
				sb.append("</a>");
			} else sb.append(fromNodeLabel);
			
			sb.append(" ");
			
			sb.append("<b><i>").append(edgeTypeVerb).append("</i></b>");
			
			sb.append(" ");	
			
			if (linkToNode) {
				sb.append("<a href='").append(urlPrefix).append(edge.getToNode().getId()).append("'>");
				sb.append(toNodeLabel);
				sb.append("</a>");
			} else sb.append(toNodeLabel);
			
			return sb.toString();
		}
    }
    
    
    
    //////////// GET TABLE OF ATTRIBUTES FOR EACH QUESTION TYPE //////////
    // although quite verbose, these methods can be easily located and modified.
    
	private String getChoice(Question question) {
		StringBuilder sb = new StringBuilder();	
		boolean empty = true;		// totally ignore this question if true
		int common = 0;				// number of common attributes
		
		sb.append("<table id='").append(++toggleIndex).append("' class='edgeCompareTable' style='display:none'>");		
		for (Field field : question.getFields()){
			String key = question.makeFieldKey(field);; // attribute key
			String fvalue, tvalue; 						// attribute value;
			String flabel = "";							// from node attribute display
			String tlabel = "";							// to node attribute display
						
			fvalue = fnode.getAttribute(key);
			tvalue = tnode.getAttribute(key);
			
			if (fvalue != null) flabel = field.getLabel();
			if (tvalue != null) tlabel = field.getLabel();

			if (flabel.length() == 0 && tlabel.length() == 0) continue;
			else {
				sb.append("<tr>");
				if (flabel.equals(tlabel)){
					sb.append("<td class='edgeCompareCommonCell'>").append(flabel).append("</td>");
					sb.append("<td class='edgeCompareCommonCell'>").append(tlabel).append("</td>");					
				} else {
					sb.append("<td class='edgeCompareCell'>").append(flabel).append("</td>");
					sb.append("<td class='edgeCompareCell'>").append(tlabel).append("</td>");
				}
				sb.append("</tr>");
			}
			
			if (flabel.length() != 0 && tlabel.length() != 0) common++;
			empty = false;
		}		
		sb.append("</table>");

		if (empty) return "";		
		else {
			String leftDisplay = question.getLabel() + " (" + common + " in common)";			
			return getTableLine(NodeDetails.getToggleLink(leftDisplay, toggleIndex), sb.toString());
		}
	}
	
	private String getRating(Question question) {
		StringBuilder sb = new StringBuilder();	
		boolean empty = true;		// totally ignore this question if true
		int common = 0;				// number of common attributes
		
		sb.append("<table id='").append(++toggleIndex).append("' class='edgeCompareTable' style='display:none'>");		
		for (Field field : question.getFields()){
			String key = question.makeFieldKey(field);; // attribute key
			String fvalue, tvalue; 						// attribute value;
			String flabel = "";							// from node attribute display
			String tlabel = "";							// to node attribute display
			
			fvalue = fnode.getAttribute(key);
			tvalue = tnode.getAttribute(key);
			
			if (fvalue != null) {
				String scaleName = Question.getScaleNameFromKey(fvalue);
				Scale scale = question.getScaleByName(scaleName);
				flabel = field.getLabel() + ": " + scale.getLabel();
			}
			
			if (tvalue != null) {
				String scaleName = Question.getScaleNameFromKey(tvalue);
				Scale scale = question.getScaleByName(scaleName);
				tlabel = field.getLabel() + ": " + scale.getLabel();
			}

			if (flabel.length() == 0 && tlabel.length() == 0) continue;
			else {
				sb.append("<tr>");
				sb.append("<td class='edgeCompareCell'>").append(flabel).append("</td>");
				sb.append("<td class='edgeCompareCell'>").append(tlabel).append("</td>");
				sb.append("</tr>");
			}
			
			if (flabel.length() != 0 && tlabel.length() != 0) common++;
			
			empty = false;
		}		
		sb.append("</table>");
		
		if (empty) return "";	
		else {
			String leftDisplay = question.getLabel() + " (" + common + " in common)";			
			return getTableLine(NodeDetails.getToggleLink(leftDisplay, toggleIndex), sb.toString());
		}
	}
	
	private String getText(Question question) {
		StringBuilder sb = new StringBuilder();	
		boolean empty = true;		// totally ignore this question if true
		int common = 0;				// number of common attributes
		
		sb.append("<table id='").append(++toggleIndex).append("' class='edgeCompareTable' style='display:none'>");		
		for (TextField tf : question.getTextFields()){
			String key = question.makeTextFieldKey(tf); // attribute key
			String fvalue, tvalue; 						// attribute value;
			String flabel = "";							// from node attribute display
			String tlabel = "";							// to node attribute display
			
			fvalue = fnode.getAttribute(key);
			tvalue = tnode.getAttribute(key);
			
			if (fvalue != null) flabel = tf.getLabel() + ": " + fvalue;
			if (tvalue != null) tlabel = tf.getLabel() + ": " + tvalue;
			
			if (flabel.length() == 0 && tlabel.length() == 0) continue;
			else {
				sb.append("<tr>");
				sb.append("<td class='edgeCompareCell'>").append(flabel).append("</td>");
				sb.append("<td class='edgeCompareCell'>").append(tlabel).append("</td>");
				sb.append("</tr>");
			}
			
			if (flabel.length() != 0 && tlabel.length() != 0) common++;
			empty = false;
		}		
		sb.append("</table>");
		
		if (empty) return "";		
		else {
			String leftDisplay = question.getLabel() + " (" + common + " in common)";			
			return getTableLine(NodeDetails.getToggleLink(leftDisplay, toggleIndex), sb.toString());
		}
	}
	
	private String getTextLong(Question question) {
		StringBuilder sb = new StringBuilder();	
		boolean empty = true;		// totally ignore this question if true
		int common = 0;				// number of common attributes
		
		sb.append("<table id='").append(++toggleIndex).append("' class='edgeCompareTable' style='display:none'>");		
		for (Field field : question.getFields()){
			String key = question.makeFieldKey(field);; // attribute key
			String fvalue, tvalue; 						// attribute value;
			String flabel = "";							// from node attribute display
			String tlabel = "";							// to node attribute display
			
			fvalue = fnode.getLongAttribute(key);
			tvalue = tnode.getLongAttribute(key);
			
			if (fvalue != null) flabel = field.getLabel() + ": " + fvalue;
			if (tvalue != null) tlabel = field.getLabel() + ": " + tvalue;

			if (flabel.length() == 0 && tlabel.length() == 0) continue;
			else {
				sb.append("<tr>");
				sb.append("<td class='edgeCompareCell'>").append(flabel).append("</td>");
				sb.append("<td class='edgeCompareCell'>").append(tlabel).append("</td>");
				sb.append("</tr>");
			}
			
			if (flabel.length() != 0 && tlabel.length() != 0) common++;
			empty = false;
		}		
		sb.append("</table>");
		
		if (empty) return "";		
		else {
			String leftDisplay = question.getLabel() + " (" + common + " in common)";			
			return getTableLine(NodeDetails.getToggleLink(leftDisplay, toggleIndex), sb.toString());
		}
	}
	
	private String getDuration(Question question) {
		StringBuilder sb = new StringBuilder();	
		boolean empty = true;		// totally ignore this question if true
		int common = 0;				// number of common attributes
		
		sb.append("<table id='").append(++toggleIndex).append("' class='edgeCompareTable' style='display:none'>");		
		for (Field field : question.getFields()){
			String key = question.makeFieldKey(field);; // attribute key
			String fvalue, tvalue; 						// attribute value;
			String flabel = "";							// from node attribute display
			String tlabel = "";							// to node attribute display

			fvalue = fnode.getAttribute(key);
			tvalue = tnode.getAttribute(key);
			
			if (fvalue != null) flabel = field.getLabel() + ": " + fvalue;
			if (tvalue != null) tlabel = field.getLabel() + ": " + tvalue;

			if (flabel.length() == 0 && tlabel.length() == 0) continue;
			else {
				sb.append("<tr>");
				sb.append("<td class='edgeCompareCell'>").append(flabel).append("</td>");
				sb.append("<td class='edgeCompareCell'>").append(tlabel).append("</td>");
				sb.append("</tr>");
			}
			
			if (flabel.length() != 0 && tlabel.length() != 0) common++;
			empty = false;
		}
		sb.append("</table>");
		
		if (empty) return "";		
		else {
			String leftDisplay = question.getLabel() + " (" + common + " in common)";			
			return getTableLine(NodeDetails.getToggleLink(leftDisplay, toggleIndex), sb.toString());
		}
	}
    
	private String getMultipleChoice(Question question) {
		StringBuilder sb = new StringBuilder();		
		for (TextField tf : question.getTextFields()){			
			
			
			StringBuilder right = new StringBuilder();
			boolean empty = true;		// totally ignore this question if true
			int common = 0;				// number of common attributes
			
			right.append("<table id='").append(++toggleIndex).append("' class='edgeCompareTable' style='display:none'>");			
			for (Field field : question.getFields()){
				String key = question.makeFieldsKey(field, tf); // attribute key
				String fvalue, tvalue; 						// attribute value;
				String flabel = "";							// from node attribute display
				String tlabel = "";							// to node attribute display
							
				fvalue = fnode.getAttribute(key);
				tvalue = tnode.getAttribute(key);
				
				if (fvalue != null) flabel = field.getLabel();
				if (tvalue != null) tlabel = field.getLabel();

				if (flabel.length() == 0 && tlabel.length() == 0) continue;
				else {
					right.append("<tr>");
					if (flabel.equals(tlabel)){
						sb.append("<td class='edgeCompareCommonCell'>").append(flabel).append("</td>");
						sb.append("<td class='edgeCompareCommonCell'>").append(tlabel).append("</td>");					
					} else {
						sb.append("<td class='edgeCompareCell'>").append(flabel).append("</td>");
						sb.append("<td class='edgeCompareCell'>").append(tlabel).append("</td>");
					}
					right.append("</tr>");
				}
				
				if (flabel.length() != 0 && tlabel.length() != 0) common++;
				empty = false;
			}		
			right.append("</table>");
			
			if (!empty) {
				String leftDisplay = question.getLabel() + "::" + tf.getLabel() + " (" + common + " in common)";
				String content = getTableLine(NodeDetails.getToggleLink(leftDisplay, toggleIndex), right.toString());
				sb.append(content);
			}
		}
		
		return sb.toString();
	}
	
	private String getMultipleRating(Question question) {
		StringBuilder sb = new StringBuilder();		
		for (TextField tf : question.getTextFields()){			
			
			
			StringBuilder right = new StringBuilder();
			boolean empty = true;		// totally ignore this question if true
			int common = 0;				// number of common attributes
			
			right.append("<table id='").append(++toggleIndex).append("' class='edgeCompareTable' style='display:none'>");			
			for (Field field : question.getFields()){
				String key = question.makeFieldsKey(field, tf); // attribute key
				String fvalue, tvalue; 						// attribute value;
				String flabel = "";							// from node attribute display
				String tlabel = "";							// to node attribute display
							
				fvalue = fnode.getAttribute(key);
				tvalue = tnode.getAttribute(key);
				
				if (fvalue != null) {
					String scaleName = Question.getScaleNameFromKey(fvalue);
					Scale scale = question.getScaleByName(scaleName);
					flabel = field.getLabel() + ": " + scale.getLabel();
				}
				
				if (tvalue != null) {
					String scaleName = Question.getScaleNameFromKey(tvalue);
					Scale scale = question.getScaleByName(scaleName);
					tlabel = field.getLabel() + ": " + scale.getLabel();
				}

				if (flabel.length() == 0 && tlabel.length() == 0) continue;
				else {
					right.append("<tr>");
					right.append("<td class='edgeCompareCell'>").append(flabel).append("</td>");
					right.append("<td class='edgeCompareCell'>").append(tlabel).append("</td>");
					right.append("</tr>");
				}
				
				if (flabel.length() != 0 && tlabel.length() != 0) common++;
				empty = false;
			}		
			right.append("</table>");
			
			if (!empty) {
				String leftDisplay = question.getLabel() + "::" + tf.getLabel() + " (" + common + " in common)";
				String content = getTableLine(NodeDetails.getToggleLink(leftDisplay, toggleIndex), right.toString());
				sb.append(content);
			}
		}
		
		return sb.toString();
	}	
}
