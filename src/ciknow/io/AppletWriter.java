package ciknow.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ciknow.dao.NodeDao;
import ciknow.dao.QuestionDao;
import ciknow.domain.Edge;
import ciknow.domain.Field;
import ciknow.domain.Node;
import ciknow.domain.Question;
import ciknow.domain.Scale;
import ciknow.ro.GenericRO;
import ciknow.util.Beans;
import ciknow.util.Constants;
import ciknow.util.EdgeUtil;
import ciknow.util.GeneralUtil;
import ciknow.util.VisUtil;
import ciknow.vis.NetworkExtractor;
import ciknow.vis.RecommenderNetwork;
import edu.uci.ics.jung.graph.filters.impl.KNeighborhoodFilter;

import static ciknow.util.VisUtil.*;

public class AppletWriter {
	private static Log logger = LogFactory.getLog(AppletWriter.class);

	private QuestionDao questionDao;
	private GenericRO genericRO;

	private Map<Long, Question> idToQuestionMap = new HashMap<Long, Question>();
	private Map<String, Question> shortNameToQuestionMap = new HashMap<String, Question>();

	// for recommendation network only
	private Node sourceNode;
	private Set<Node> recNodes;
	private Node targetNode;

	// for local network only
	private Collection<Node> focalNodes;

	// /////////////////////////// intermediate data holder ///////////////////
	// colors preferences defined in ciknow.xml
	private Map<String, String> colorMap = null;
	// edge descriptions (type, label, verb)
	private List<Map<String, String>> eds;
	// node descriptions (type, label)
	private List<Map<String, String>> nds;

	// color by node attribute
	private Set<String> nodeColorAttrs = null;
	// node attribute (or node type) -> color
	private Map<String, String> nodeColorMap = new HashMap<String, String>();
	private Set<Long> colorQuestionVisibleNodeIds = null;

	// group by node attribute
	private Set<String> nodeGroupAttrs = null;
	// node attribute -> group label (e.g. Field label)
	private Map<String, String> nodeGroupMap = new HashMap<String, String>();
	private Map<String, String> fieldLabelMap = null;
	private Set<Long> groupQuestionVisibleNodeIds = null;

	// shape
	private Set<Long> shapeQuestionVisibleNodeIds = null;

	// vertical size by node attribute
	private String sizeAttrName = null;
	// horizontal size by node attribute
	private String sizeAttrName2 = null;

	// edge types based on given list of edges
	// private List<String> edgeTypes = null;
	// edge type -> color
	private Map<String, String> edgeColorMap = new HashMap<String, String>();

	// ///////////////////////////// string buffers ///////////////////////////
	// private Boolean directed = false;
	private String nodeURLPrefix;
	private String linkURLPrefix;

	private StringBuilder nodeIdStr = new StringBuilder();
	private StringBuilder nodeTypesStr = new StringBuilder();
	private StringBuilder nodeLabelStr = new StringBuilder();
	private StringBuilder hiddenNodesStr = new StringBuilder();
	private StringBuilder nodeColorStr = new StringBuilder();
	private StringBuilder nodeShapeStr = new StringBuilder();
	private StringBuilder nodeGroupStr = new StringBuilder();
	private StringBuilder nodeSizeStr = new StringBuilder();
	private StringBuilder nodeSizeStr2 = new StringBuilder();
	private StringBuilder nodeImageStr = new StringBuilder();
	private StringBuilder nodeUserStr = new StringBuilder();

	private StringBuilder edgeStr = new StringBuilder();
	private StringBuilder edgeTypesStr = new StringBuilder();
	private StringBuilder edgeDirectionStr = new StringBuilder();
	private StringBuilder edgeWeightStr = new StringBuilder();
	private StringBuilder edgeLabelStr = new StringBuilder();
	private StringBuilder hiddenEdgesStr = new StringBuilder();
	private StringBuilder edgeColorStr = new StringBuilder();

	private StringBuilder nodeLegendStr = new StringBuilder();
	private StringBuilder edgeLegendStr = new StringBuilder();
	private StringBuilder groupLegendStr = new StringBuilder();

	public AppletWriter() {
		Beans.init();
		questionDao = (QuestionDao) Beans.getBean("questionDao");
		genericRO = (GenericRO) Beans.getBean("genericRO");
		colorMap = GeneralUtil.getColors();
		eds = GeneralUtil.getEdgeDescriptions();
		nds = GeneralUtil.getNodeDescriptions();
	}

	/**
	 * 
	 * @param os
	 * @param title
	 * @param nodes
	 * @param edges
	 * @param hiddenNodes
	 *            - the hidden nodes are specific and only used for recommender
	 *            network. NOT the same meaning as the nodes with 'hidden' role,
	 *            which are already filtered out when extracting local/custom
	 *            network
	 * @param colorQuestionId
	 * @param groupQuestionId
	 * @param sizeQuestionId
	 * @param sizeQuestionId2
	 * @throws Exception
	 */
	public String write(OutputStream outputStream, String title, String path,String maxRecSize, String minRecSize,
			Collection<Node> nodes, Collection<Edge> edges,
			Collection<Node> hiddenNodes, Collection<Node> hiddenNodes1, Set<Long> hiddenEdges,
			Set<Long> hiddenEdges1, Set<Long> hiddenEdges2, String colorQuestionId,
			String shapeQuestionId, String groupQuestionId,
			String sizeQuestionId, String sizeQuestionId2, String displayAttr,
			String hideNodeLabel, String mutualAsUndirected, String os)
			throws Exception {
		
		logger.info("************************** generate applet for network...");
		logger.debug("number of nodes: " + nodes.size());
		logger.debug("number of edges: " + edges.size());
		if (colorQuestionId == null || colorQuestionId.length() == 0)
			colorQuestionId = "type";
		logger.debug("colorQuestionId: " + colorQuestionId);
		if (shapeQuestionId == null || shapeQuestionId.length() == 0)
			shapeQuestionId = "type";
		logger.debug("shapeQuestionId: " + shapeQuestionId);
		if (groupQuestionId == null || groupQuestionId.length() == 0)
			groupQuestionId = "type";
		logger.debug("groupQuestionId: " + groupQuestionId);
		logger.debug("sizeQuestionId: " + sizeQuestionId);
		logger.debug("sizeQuestionId2: " + sizeQuestionId2);
		logger.debug("displayAttr: " + displayAttr);
		logger.debug("hideNodeLabel: " + hideNodeLabel);
		if (mutualAsUndirected == null || mutualAsUndirected.equals("")) {
			mutualAsUndirected = "0";
		}
		logger.debug("mutualAsUndirected: " + mutualAsUndirected);
		logger.debug("client operating system: " + os);

		String baseUrl = genericRO.getBaseURL();
		nodeURLPrefix = baseUrl + "/vis_get_node_info.jsp?";
		linkURLPrefix = baseUrl + "/vis_get_link_info.jsp?";

		logger.debug("pre-process edges: merge if possible");
		// List<Edge> edgeList = new ArrayList<Edge>(edges);
		// Set<Edge> tempEdges = new HashSet<Edge>();
		// outer:
		// for (int i=0; i<edgeList.size(); i++){
		// Edge e1 = edgeList.get(i);
		// if (tempEdges.contains(e1)) continue;
		// for (int j=i+1; j<edgeList.size(); j++){
		// Edge e2 = edgeList.get(j);
		// if (tempEdges.contains(e2)) continue;
		// boolean merged = e1.merge(e2);
		// if (merged){
		// //logger.debug("merged edge: " + e3);
		// tempEdges.add(e2);
		// continue outer;
		// }
		// }
		// }
		// edges.removeAll(tempEdges);
		// logger.debug(tempEdges.size() +
		// " directed edges are merged into undirected edges");
		
		Edge.merge(edges);

		List<Question> questions = questionDao.getAll();
		for (Question question : questions) {
			idToQuestionMap.put(question.getId(), question);
			shortNameToQuestionMap.put(question.getShortName(), question);
		}
		preNodeLegend(nodes, colorQuestionId);
		preGroupLegend(nodes, groupQuestionId);
		preEdgeLegend(edges);
		Question sizeQuestion = preSizeQuestion(sizeQuestionId);
		Question sizeQuestion2 = preSizeQuestion2(sizeQuestionId2);

		logger.info("processing nodes...");
		long index = 0;
		Map<Long, Long> nodeIdIndexMap = new HashMap<Long, Long>();

		for (Node node : nodes) {
			index++;
			nodeIdIndexMap.put(node.getId(), index);

			nodeIdStr.append(node.getId()).append("||");

			if (sourceNode != null && sourceNode.equals(node)) {
				nodeTypesStr.append(VisUtil.LOGIN_LABEL).append("||");
			} else if (recNodes != null && recNodes.contains(node)) {
				nodeTypesStr.append(VisUtil.RECOMMENDATION_LABEL).append("||");
			} else if (targetNode != null && targetNode.equals(node)) {
				nodeTypesStr.append(VisUtil.TARGET_LABEL).append("||");
			} else if (focalNodes != null && focalNodes.contains(node)) {
				nodeTypesStr.append(Constants.NODE_TYPE_FOCAL).append("||");
			} else {
				nodeTypesStr.append(
						GeneralUtil.getNodeTypeLabel(nds, node.getType())).append(
						"||");
			}

			String label;
			String value;
			if (displayAttr != null && !displayAttr.equals("NODE_LABEL")) {
				value = node.getAttribute(displayAttr);
				if (value == null || value.length() == 0)
					value = node.getLabel();
			} else {
				value = node.getLabel();
			}
			label = value.replaceAll("\"", "");
			if (label.indexOf("||") >= 0) {
				label = label.replaceAll("\\|", "!");
			}
			if (label.length() == 0)
				label = "!EMPTY!";
			nodeLabelStr.append(label).append("||");
			try{
			if (hiddenNodes.contains(node))
				hiddenNodesStr.append("1||");
			else if (hiddenNodes1 != null && hiddenNodes1.contains(node))
				hiddenNodesStr.append("2||");
			else
				hiddenNodesStr.append("0||");
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("********* exception when processing hidden node: " + e);
			}
			if (hasImages(node.getUsername())) {
				nodeImageStr.append("1||");
				logger.warn("************ JInling ");
			} else {
				nodeImageStr.append("0||");

			}
			
			String userName = node.getUsername().replaceAll("\"", "");
			nodeUserStr.append(userName).append("||");

			String nodeColor = getNodeColor(node, colorQuestionId);
			nodeColorStr.append(nodeColor).append("||");

			String nodeShape = getNodeShape(node, shapeQuestionId);
			nodeShapeStr.append(nodeShape).append("||");

			String nodeGroup = getNodeGroup(node, groupQuestionId);
			nodeGroupStr.append(nodeGroup).append("||");

			if (sizeAttrName != null) {
				nodeSizeStr.append(getSize(node, sizeQuestion, sizeAttrName))
						.append("||");
			}

			if (sizeAttrName != null && sizeAttrName2 != null) {
				nodeSizeStr2
						.append(getSize(node, sizeQuestion2, sizeAttrName2))
						.append("||");
			}
		}
		logger.info("edges after merging: " + edges.size());
		logger.info("processing edges...");
		Map<String, Question> edgeTypeQuestionMap = EdgeUtil.getEdgeTypeQuestionMap(questions);

		Set<Edge> edgesAfterMerge = new HashSet<Edge>();
		edgesAfterMerge.addAll(edges);
		for (Edge edge : edgesAfterMerge) {
			Node fnode = edge.getFromNode();
			Node tnode = edge.getToNode();
			if (fnode.equals(tnode)) {
				logger.info("self link (edgeId=" + edge.getId() + ") ignored.");
				
				continue;
			}

			// if (edge.isDirected()) directed = true;
			try {
				String edgeType = edge.getType();
				String edgeLabel = GeneralUtil.getEdgeLabel(eds, edgeType);
				long findex = nodeIdIndexMap.get(fnode.getId());
				long tindex = nodeIdIndexMap.get(tnode.getId());
				edgeStr.append(findex).append("-").append(tindex).append("||");
				edgeTypesStr.append(edgeType).append("||");
				
				if (edge.isDirected())
					edgeDirectionStr.append("1");
				else {
					if (mutualAsUndirected.equals("0") && edge.getId() < 0)
						edgeDirectionStr.append("2");
					else
						edgeDirectionStr.append("0");
				}
				
				edgeDirectionStr.append("||");
				// edgeDirectionStr.append(edge.isDirected()?"1":"0").append("||");

				edgeLabelStr.append(edgeLabel).append("||");
				
				Double weight;
				Question question = edgeTypeQuestionMap.get(edge.getType());
				// tagging by game or node manager
				if (question == null)
					weight = edge.getWeight();
				// edge created by answering survey questions
				else
					weight = edge.getWeight();
				edgeWeightStr.append(weight).append("||");
				
				if (hiddenNodes.contains(fnode) || hiddenNodes.contains(tnode)) {
					
					if (hiddenEdges != null) {
						if (hiddenEdges.contains(Math.abs(edge.getId()))){
							hiddenEdgesStr.append("1||");	
						}else if (hiddenEdges2.contains(Math.abs(edge.getId()))){
							hiddenEdgesStr.append("3||");	
						}else {
							hiddenEdgesStr.append("0||");						
						}
					}
					
				} else {
					
					if (hiddenEdges1 != null && hiddenEdges1.contains(Math.abs(edge.getId()))){
						
						if (((sourceNode != null && sourceNode.equals(fnode)) 
							 &&(recNodes != null && recNodes.contains(tnode)))||
							 ((sourceNode != null && sourceNode.equals(tnode)) 
									 &&(recNodes != null && recNodes.contains(fnode)))){

						hiddenEdgesStr.append("2||");// direct relations, but not recommended edges
						}
					}else if (hiddenEdges2 != null && hiddenEdges2.contains(Math.abs(edge.getId()))){
											
							hiddenEdgesStr.append("3||"); //no directed not recommended edges
											
					}else if(hiddenEdges != null && hiddenEdges.contains(Math.abs(edge.getId()))){
								hiddenEdgesStr.append("1||");
								
					}else{
							hiddenEdgesStr.append("0||");
							
																	
					}
					
				}

				
				String edgeColor = edgeColorMap.get(edgeType);
				// if two edgeTypes have the same edgeLabel... see
				// preEdgeLegend()
				if (edgeColor == null) {
					edgeColor = COLOR2HEX(VisUtil.DEFAULT_EDGE_COLOR);
					logger
							.warn("There is no edge color assigned for edgeType: "
									+ edgeType + "!");
				}
				
				edgeColorStr.append(edgeColor).append("||");
				
			} catch (Exception e) {
				//e.printStackTrace();
				//System.out.println("********* exception when processing edges: " + e);
			}
		}

		String colorBy = getAttributeLabel(colorQuestionId);
		String shapeBy = getAttributeLabel(shapeQuestionId);
		String groupBy = getAttributeLabel(groupQuestionId);
		String html = composeHtml(title, path, maxRecSize, minRecSize, colorBy, groupBy, shapeBy,
				hideNodeLabel, os);
				
		logger.info("writing html...");
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(
				outputStream, "UTF8"));
		writer.append(html);
		writer.flush();

		logger.info("************************** applet generated.");

		return html;
	}

	private boolean hasImages(String userName) {

		String url = this.getClass().getProtectionDomain().getCodeSource()
				.getLocation().toString();
		String seperator = System.getProperty("file.separator");
		String fileName = url.split("WEB-INF")[0].replace("/", seperator)
				+ "images" + seperator + "photos" + seperator + userName
				+ ".jpg";
		
		int index = fileName.indexOf(seperator);
		String finalPath = "";
		if (isWindows())
		finalPath = fileName.substring(index + 1);
		else
		finalPath = fileName.substring(index);	
		
		File f = new File(finalPath);
		
		return f.exists();

	}

	private  boolean isWindows(){
		 
		String os = System.getProperty("os.name").toLowerCase();
		//windows
	    return (os.indexOf( "win" ) >= 0); 
 
	}
	
	// prepare node legends and color map
	private void preNodeLegend(Collection<Node> rawNodes, String colorQuestionId) {
		logger.info("prepare node legends...");
		String nodeColor;
		Collection<Node> nodes = new HashSet<Node>(rawNodes);
		String[] colors = colorMap.get("nodeColors").trim().split(",", 0);

		// for recommendation network only
		if (this.sourceNode != null) {
			nodeColor = COLOR2HEX(LOGIN_COLOR);
			nodeColorMap.put(LOGIN_LABEL, nodeColor);
			nodeLegendStr.append(LOGIN_LABEL).append("-").append(nodeColor)
					.append("||");

			if (this.recNodes != null) {
				nodeColor = COLOR2HEX(RECOMMENDATION_COLOR);
				nodeColorMap.put(RECOMMENDATION_LABEL, nodeColor);
				nodeLegendStr.append(RECOMMENDATION_LABEL).append("-").append(
						nodeColor).append("||");
			}
			if (this.targetNode != null) {

				nodeColor = COLOR2HEX(TARGET_COLOR);
				nodeColorMap.put(TARGET_LABEL, nodeColor);
				nodeLegendStr.append(TARGET_LABEL).append("-")
						.append(nodeColor).append("||");

			}
			nodes.remove(this.sourceNode);
			if (this.recNodes != null)
				nodes.removeAll(this.recNodes);

			nodes.remove(this.targetNode);
		}

		// for local network only
		if (this.focalNodes != null && !this.focalNodes.isEmpty()) {
			nodeColor = COLOR2HEX(FOCAL_COLOR);
			nodeColorMap.put(Constants.NODE_TYPE_FOCAL, nodeColor);
			nodeLegendStr.append(Constants.NODE_TYPE_FOCAL).append("-").append(
					nodeColor).append("||");
			nodes.removeAll(this.focalNodes);
		}

		nodeColorAttrs = getDistinctAttributes(nodes, colorQuestionId);
		int i = 0;
		
		for (String colorAttr : nodeColorAttrs) {
			nodeColor = COLOR2HEX(VisUtil.DEFAULT_NODE_COLOR);
			if (i < colors.length)
				nodeColor = colors[i].trim();
			else {
				logger
						.warn("Insufficient node colors, please contact admin to add more colors!");
			}

			String attrLabel;
			if (colorQuestionId.startsWith(VisUtil.ATTR_PREFIX)) {
				attrLabel = colorAttr;

				// for rating and multiple rating question's attribute
				if (colorAttr.startsWith("S" + Constants.SEPERATOR)) {
					String shortName = Question.getShortNameFromKey(colorAttr);
					Question question = shortNameToQuestionMap.get(shortName);
					String scaleName = Question.getScaleNameFromKey(colorAttr);
					Scale scale = question.getScaleByName(scaleName);
					if (scale != null)
						attrLabel = question.getLabel() + ":"
								+ scale.getLabel();
					else {
						logger.warn("invalid attribute value: " + colorAttr);
						nodeColor = COLOR2HEX(VisUtil.MISSING_VALUE_COLOR);
						nodeColorMap.put(colorAttr, nodeColor);
						continue;
					}
				}
			} else if (colorQuestionId.startsWith(VisUtil.QUESTION_PREFIX)) {
				attrLabel = this.fieldLabelMap.get(colorAttr);
			} else if (colorQuestionId.equals("type")){
				attrLabel = GeneralUtil.getNodeTypeLabel(nds, colorAttr);
			} else attrLabel = colorAttr;

			nodeColorMap.put(colorAttr, nodeColor);
			nodeLegendStr.append(attrLabel)
					.append("-").append(nodeColor).append("||");

			i++;
		}

		if (colorQuestionId.startsWith(VisUtil.ATTR_PREFIX)
				&& colorQuestionId.contains(Constants.SEPERATOR)) {
			String key = colorQuestionId
					.substring(VisUtil.ATTR_PREFIX.length());
			String shortName = Question.getShortNameFromKey(key);
			Question question = shortNameToQuestionMap.get(shortName);
			colorQuestionVisibleNodeIds = question.getVisibleNodeIds();
		} else if (colorQuestionId.startsWith(VisUtil.QUESTION_PREFIX)) {
			String qid = colorQuestionId.substring(VisUtil.QUESTION_PREFIX
					.length());
			Question q = idToQuestionMap.get(Long.parseLong(qid));
			colorQuestionVisibleNodeIds = q.getVisibleNodeIds();
		}


		// "not applicable"
		if (colorQuestionId.startsWith(VisUtil.QUESTION_PREFIX)
				|| colorQuestionId.contains(Constants.SEPERATOR)) {
			nodeColor = COLOR2HEX(VisUtil.NOT_APPLICABLE_COLOR);
			nodeLegendStr.append(VisUtil.NOT_APPLICABLE).append("-")
					.append(nodeColor).append("||");

		}

		// "missing value" for choice or (multiple) rating question
		nodeColor = COLOR2HEX(VisUtil.MISSING_VALUE_COLOR);
		nodeLegendStr.append(VisUtil.MISSING_VALUE).append("-").append(
				nodeColor).append("||");

	}

	// prepare group legends
	private void preGroupLegend(Collection<Node> nodes, String groupQuestionId) {
		logger.info("preparing group legends...");
		String[] colors = colorMap.get("groupColors").trim().split(",", 0);
		String groupColor;

		nodeGroupAttrs = getDistinctAttributes(nodes, groupQuestionId);
		int i = 0;
		if (nodeGroupAttrs.size() > colors.length) {
			logger
					.warn("Insufficient group colors, please contact admin to add more colors!");
		}
		for (String groupAttr : nodeGroupAttrs) {
			groupColor = COLOR2HEX(VisUtil.DEFAULT_GROUP_COLOR);
			if (i < colors.length)
				groupColor = colors[i].trim();

			String groupLabel = groupAttr;
			
			if (groupQuestionId.startsWith(VisUtil.ATTR_PREFIX)) {
				groupLabel = groupAttr;

				// for rating and multiple rating question's attribute
				if (groupAttr.startsWith("S" + Constants.SEPERATOR)) {
					String shortName = Question.getShortNameFromKey(groupAttr);
					Question question = shortNameToQuestionMap.get(shortName);
					String scaleName = Question.getScaleNameFromKey(groupAttr);
					Scale scale = question.getScaleByName(scaleName);
					if (scale != null)
						groupLabel = question.getLabel() + ":"
								+ scale.getLabel();
					else {
						logger.warn("invalid attribute value: " + groupAttr);
						nodeGroupMap.put(groupAttr, VisUtil.MISSING_VALUE);
						continue;
					}
				}
			} else if (groupQuestionId.startsWith(VisUtil.QUESTION_PREFIX)){
				groupLabel = this.fieldLabelMap.get(groupAttr);
			} else if (groupQuestionId.equals("type")){
				groupLabel = GeneralUtil.getNodeTypeLabel(nds, groupAttr);
			} else {
				groupLabel = groupAttr;
			}
			
			nodeGroupMap.put(groupAttr, groupLabel);
			groupLegendStr.append(groupLabel).append("-").append(groupColor)
					.append("||");

			i++;
		}

		if (groupQuestionId.startsWith(VisUtil.ATTR_PREFIX)
				&& groupQuestionId.contains(Constants.SEPERATOR)) {
			String key = groupQuestionId
					.substring(VisUtil.ATTR_PREFIX.length());
			String shortName = Question.getShortNameFromKey(key);
			Question question = shortNameToQuestionMap.get(shortName);
			groupQuestionVisibleNodeIds = question.getVisibleNodeIds();
		} else if (groupQuestionId.startsWith(VisUtil.QUESTION_PREFIX)) {
			String qid = groupQuestionId.substring(VisUtil.QUESTION_PREFIX
					.length());
			Question q = idToQuestionMap.get(Long.parseLong(qid));
			groupQuestionVisibleNodeIds = q.getVisibleNodeIds();
		}

		// "not applicable"
		if (groupQuestionId.startsWith(VisUtil.QUESTION_PREFIX)
				|| groupQuestionId.contains(Constants.SEPERATOR)) {
			groupColor = COLOR2HEX(VisUtil.NOT_APPLICABLE_COLOR);
			groupLegendStr.append(VisUtil.NOT_APPLICABLE).append("-")
					.append(groupColor).append("||");
		}

		// "missing value" for choice or (multiple) rating question
		groupColor = COLOR2HEX(VisUtil.MISSING_VALUE_COLOR);
		groupLegendStr.append(VisUtil.MISSING_VALUE).append("-").append(
				groupColor).append("||");
	}

	// prepare node legends and color map
	private void preEdgeLegend(Collection<Edge> edges) {
		logger.info("preparing edge legends...");
		String[] colors = colorMap.get("edgeColors").trim().split(",", 0);

		Set<String> edgeTypes = EdgeUtil.getDistinctEdgeTypes(edges);
		Map<String, String> edgeLabelTypeMap = new TreeMap<String, String>();
		for (String edgeType : edgeTypes) {
			String edgeLabel = GeneralUtil.getEdgeLabel(eds, edgeType);
			edgeLabelTypeMap.put(edgeLabel, edgeType);
		}
		int i = 0;
		for (String edgeLabel : edgeLabelTypeMap.keySet()) {
			String edgeType = edgeLabelTypeMap.get(edgeLabel);

			String edgeColor = COLOR2HEX(VisUtil.DEFAULT_EDGE_COLOR);
			if (i < colors.length)
				edgeColor = colors[i].trim();
			else {
				logger
						.warn("Insufficient edge colors, please contact admin to add more colors!");
			}

			edgeColorMap.put(edgeType, edgeColor);
			edgeLegendStr.append(edgeLabel).append("-").append(edgeColor)
					.append("||");
			i++;
		}
	}

	private String getAttributeLabel(String questionId) {
		if (questionId.startsWith(VisUtil.ATTR_PREFIX)) {
			String attrName = questionId
					.substring(VisUtil.ATTR_PREFIX.length());
			if (attrName.startsWith("F" + Constants.SEPERATOR)) {
				String shortName = Question.getShortNameFromKey(attrName);
				Question q = shortNameToQuestionMap.get(shortName);
				String fieldName = Question.getFieldNameFromKey(attrName);
				Field field = q.getFieldByName(fieldName);
				return q.getLabel() + ":" + field.getLabel();
			} else {
				return attrName;
			}
		} else if (questionId.startsWith(VisUtil.QUESTION_PREFIX)) {
			Long qid = Long.parseLong(questionId
					.substring(VisUtil.QUESTION_PREFIX.length()));
			Question q = idToQuestionMap.get(qid);
			return q.getLabel();
		} else
			return questionId;
	}

	// get distinct attribute names for classification based on given nodes and
	// color/group question
	private Set<String> getDistinctAttributes(Collection<Node> nodes,
			String questionId) {
		Set<String> distinctAttrs = new TreeSet<String>();
		if (questionId.startsWith(VisUtil.ATTR_PREFIX)) { // ATTR:gender
			String attrName = questionId
					.substring(VisUtil.ATTR_PREFIX.length());
			logger.debug("coloring by attribute: " + attrName);
			for (Node node : nodes) {
				String attrValue = node.getAttribute(attrName);
				if (attrValue != null)
					distinctAttrs.add(attrValue);
			}
		} else if (questionId.startsWith(VisUtil.QUESTION_PREFIX)) {
			String qid = questionId.substring(VisUtil.QUESTION_PREFIX.length());
			Question question = idToQuestionMap.get(Long.parseLong(qid));
			this.fieldLabelMap = getFieldLabelMap(question);
			Set<String> attrNames = this.fieldLabelMap.keySet();

			for (Node node : nodes) {
				Set<String> attrs = new HashSet<String>(attrNames);
				// should has one attribute only for single choice question
				attrs.retainAll(node.getAttributes().keySet());
				distinctAttrs.addAll(attrs);
			}
		} else {
			for (Node node : nodes) {
				String value = null;
				if (questionId.equals("type")) value = node.getType();
				else if (questionId.equals("city")) value = node.getCity();
				else if (questionId.equals("state")) value = node.getState();
				else if (questionId.equals("country")) value = node.getCountry();
				else if (questionId.equals("zipcode")) value = node.getZipcode();
				else if (questionId.equals("organization")) value = node.getOrganization();
				else if (questionId.equals("department")) value = node.getDepartment();
				else if (questionId.equals("unit")) value = node.getUnit();
				if (value != null && value.trim().length() > 0) distinctAttrs.add(value);
			}
		}
		return distinctAttrs;
	}

	private Map<String, String> getFieldLabelMap(Question question) {
		Map<String, String> fieldLabelMap = new HashMap<String, String>();
		for (Field field : question.getFields()) {
			fieldLabelMap.put(question.makeFieldKey(field), field.getLabel());
		}
		return fieldLabelMap;
	}

	private Question preSizeQuestion(String sizeQuestionId) {
		logger.info("preparing size question...");
		Question sizeQuestion = null;

		if (sizeQuestionId == null || sizeQuestionId.length() == 0)
			return null;

		sizeQuestion = idToQuestionMap.get(Long.parseLong(sizeQuestionId));
		if (sizeQuestion == null)
			return null;

		sizeAttrName = getSizeAttribute(sizeQuestion);
		if (sizeAttrName == null)
			return null;

		nodeSizeStr.append(sizeQuestion.getLabel()).append("||");

		return sizeQuestion;
	}

	private Question preSizeQuestion2(String sizeQuestionId2) {
		logger.info("preparing size question 2...");
		Question sizeQuestion2 = null;

		if (sizeQuestionId2 == null || sizeQuestionId2.length() == 0)
			return null;

		sizeQuestion2 = idToQuestionMap.get(Long.parseLong(sizeQuestionId2));
		if (sizeQuestion2 == null)
			return null;

		sizeAttrName2 = getSizeAttribute(sizeQuestion2);
		if (sizeAttrName2 == null)
			return null;

		nodeSizeStr2.append(sizeQuestion2.getLabel()).append("||");

		return sizeQuestion2;
	}

	private String getSizeAttribute(Question question) {
		List<Field> fields = question.getFields();
		if (fields.size() != 1) {
			logger.error("Sizing question must have one and only one field! "
					+ "Question(id=" + question.getId()
					+ ") is not. Sizing is ignored.");
			return null;
		}

		return question.makeFieldKey(fields.iterator().next());
	}

	private String getNodeColor(Node node, String colorQuestionId) {
		String nodeColor = null;
		if (sourceNode != null && node.equals(sourceNode)) {
			nodeColor = nodeColorMap.get(LOGIN_LABEL);
		} else if (recNodes != null && recNodes.contains(node)) {
			nodeColor = nodeColorMap.get(RECOMMENDATION_LABEL);
		} else if (targetNode != null && node.equals(targetNode)) {
			nodeColor = nodeColorMap.get(TARGET_LABEL);
		} else if (focalNodes != null && focalNodes.contains(node)) {
			nodeColor = nodeColorMap.get(Constants.NODE_TYPE_FOCAL); // COLOR2HEX(VisUtil.FOCAL_COLOR);
		} else if (colorQuestionId.startsWith(VisUtil.ATTR_PREFIX)) {
			String attrName = colorQuestionId.substring(VisUtil.ATTR_PREFIX
					.length());
			if (attrName.contains(Constants.SEPERATOR)) {
				if (!colorQuestionVisibleNodeIds.contains(node.getId())) {
					return VisUtil.COLOR2HEX(VisUtil.NOT_APPLICABLE_COLOR);
				}
			}
			String attrValue = node.getAttribute(attrName);
			if (attrValue == null)
				nodeColor = VisUtil.COLOR2HEX(VisUtil.MISSING_VALUE_COLOR);
			else
				nodeColor = nodeColorMap.get(attrValue);
		} else if (colorQuestionId.startsWith(VisUtil.QUESTION_PREFIX)) {
			if (!colorQuestionVisibleNodeIds.contains(node.getId())) {
				return VisUtil.COLOR2HEX(VisUtil.NOT_APPLICABLE_COLOR);
			}
			Set<String> colorAttrs = new HashSet<String>(nodeColorAttrs);
			colorAttrs.retainAll(node.getAttributes().keySet());
			List<String> colorAttrsList = new ArrayList<String>(colorAttrs);

			if (colorAttrsList.isEmpty()) {
				nodeColor = VisUtil.COLOR2HEX(VisUtil.MISSING_VALUE_COLOR);
			} else if (colorAttrsList.size() == 1) {
				nodeColor = nodeColorMap.get(colorAttrsList.get(0));
			} else {
				logger
						.warn("Data inconsistent: there are multiple selections "
								+ "(nodeId="
								+ node.getId()
								+ ") for single choice question "
								+ "(id="
								+ colorQuestionId
								+ "). Only first selection is used for coloring purpose.");
				nodeColor = nodeColorMap.get(colorAttrsList.get(0));
			}
		} else {
			if (colorQuestionId.equals("type")) {
				nodeColor = nodeColorMap.get(node.getType());
			} else if (colorQuestionId.equals("city")) {
				nodeColor = nodeColorMap.get(node.getCity());
			} else if (colorQuestionId.equals("state")) {
				nodeColor = nodeColorMap.get(node.getState());
			} else if (colorQuestionId.equals("country")) {
				nodeColor = nodeColorMap.get(node.getCountry());
			} else if (colorQuestionId.equals("zipcode")) {
				nodeColor = nodeColorMap.get(node.getZipcode());
			} else if (colorQuestionId.equals("organization")) {
				nodeColor = nodeColorMap.get(node.getOrganization());
			} else if (colorQuestionId.equals("department")) {
				nodeColor = nodeColorMap.get(node.getDepartment());
			} else if (colorQuestionId.equals("unit")) {
				nodeColor = nodeColorMap.get(node.getUnit());
			}
			
			if (nodeColor == null) nodeColor = VisUtil.COLOR2HEX(VisUtil.MISSING_VALUE_COLOR);
		}
		return nodeColor;
	}

	private String getNodeShape(Node node, String shapeQuestionId) {
		String nodeShape = null;
		if (shapeQuestionId.startsWith(VisUtil.ATTR_PREFIX)) {
			String attrName = shapeQuestionId.substring(VisUtil.ATTR_PREFIX
					.length());
			String attrValue = node.getAttribute(attrName);
			if (attrName.contains(Constants.SEPERATOR)) {
				String shortName = Question.getShortNameFromKey(attrName);
				Question question = shortNameToQuestionMap.get(shortName);
				if (shapeQuestionVisibleNodeIds == null) {
					shapeQuestionVisibleNodeIds = question.getVisibleNodeIds();
				}
				if (!shapeQuestionVisibleNodeIds.contains(node.getId())) {
					return VisUtil.NOT_APPLICABLE;
				}

				if (attrValue != null) {
					String scaleName = Question.getScaleNameFromKey(attrValue);
					Scale scale = question.getScaleByName(scaleName);
					nodeShape = scale.getLabel();
				} else
					nodeShape = VisUtil.MISSING_VALUE;
			} else {
				if (attrValue != null)
					nodeShape = attrValue;
				else
					nodeShape = VisUtil.MISSING_VALUE;
			}
		} else if (shapeQuestionId.startsWith(VisUtil.QUESTION_PREFIX)){
			String qid = shapeQuestionId.substring(VisUtil.QUESTION_PREFIX
					.length());
			Question question = idToQuestionMap.get(Long.parseLong(qid));
			if (shapeQuestionVisibleNodeIds == null) {
				shapeQuestionVisibleNodeIds = question.getVisibleNodeIds();
			}
			if (!shapeQuestionVisibleNodeIds.contains(node.getId())) {
				return VisUtil.NOT_APPLICABLE;
			}
			Set<String> names = new HashSet<String>(question
					.getPossibleAttributeNames());
			names.retainAll(node.getAttributes().keySet());
			if (!names.isEmpty()) {
				String key = names.iterator().next();
				String filedName = Question.getFieldNameFromKey(key);
				Field field = question.getFieldByName(filedName);
				nodeShape = field.getLabel();
			} else
				nodeShape = VisUtil.MISSING_VALUE;
		} else {
			if (shapeQuestionId.equals("type")) {
				nodeShape = GeneralUtil.getNodeTypeLabel(nds, node.getType());
			} else if (shapeQuestionId.equals("city")) {
				nodeShape = node.getCity();
			} else if (shapeQuestionId.equals("state")) {
				nodeShape = node.getState();
			} else if (shapeQuestionId.equals("country")) {
				nodeShape = node.getCountry();
			} else if (shapeQuestionId.equals("zipcode")) {
				nodeShape = node.getZipcode();
			} else if (shapeQuestionId.equals("organization")) {
				nodeShape = node.getOrganization();
			} else if (shapeQuestionId.equals("department")) {
				nodeShape = node.getDepartment();
			} else if (shapeQuestionId.equals("unit")) {
				nodeShape = node.getUnit();
			}
			
			if (nodeShape == null || nodeShape.trim().length() == 0) nodeShape = VisUtil.MISSING_VALUE;
		}
		return nodeShape;
	}

	private String getNodeGroup(Node node, String groupQuestionId) {
		String nodeGroup = null;
		if (groupQuestionId.startsWith(VisUtil.ATTR_PREFIX)) {
			String attrName = groupQuestionId.substring(VisUtil.ATTR_PREFIX
					.length());
			if (attrName.contains(Constants.SEPERATOR)) {
				if (!groupQuestionVisibleNodeIds.contains(node.getId())) {
					return VisUtil.NOT_APPLICABLE;
				}
			}
			String attrValue = node.getAttribute(attrName);
			if (attrValue == null) {
				nodeGroup = VisUtil.MISSING_VALUE;
			} else
				nodeGroup = nodeGroupMap.get(attrValue);
		} else if(groupQuestionId.startsWith(VisUtil.QUESTION_PREFIX)){
			if (!groupQuestionVisibleNodeIds.contains(node.getId())) {
				return VisUtil.NOT_APPLICABLE;
			}
			Set<String> groupAttrs = new HashSet<String>(nodeGroupAttrs);
			groupAttrs.retainAll(node.getAttributes().keySet());
			List<String> groupAttrsList = new ArrayList<String>(groupAttrs);

			if (groupAttrsList.isEmpty()) {
				nodeGroup = VisUtil.MISSING_VALUE;
			} else if (groupAttrsList.size() == 1) {
				nodeGroup = nodeGroupMap.get(groupAttrsList.get(0));
			} else {
				logger
						.warn("Data inconsistent: there are multiple selections "
								+ "(nodeId="
								+ node.getId()
								+ ") for single choice question "
								+ "(id="
								+ groupQuestionId
								+ "). Only first selection is used for grouping purpose.");
				nodeGroup = nodeGroupMap.get(groupAttrsList.get(0));
			}
		} else {
			if (groupQuestionId.equals("type")) {
				nodeGroup = nodeGroupMap.get(node.getType());
			} else if (groupQuestionId.equals("city")) {
				nodeGroup = nodeGroupMap.get(node.getCity());
			} else if (groupQuestionId.equals("state")) {
				nodeGroup = nodeGroupMap.get(node.getState());
			} else if (groupQuestionId.equals("country")) {
				nodeGroup = nodeGroupMap.get(node.getCountry());
			} else if (groupQuestionId.equals("zipcode")) {
				nodeGroup = nodeGroupMap.get(node.getZipcode());
			} else if (groupQuestionId.equals("organization")) {
				nodeGroup = nodeGroupMap.get(node.getOrganization());
			} else if (groupQuestionId.equals("department")) {
				nodeGroup = nodeGroupMap.get(node.getDepartment());
			} else if (groupQuestionId.equals("unit")) {
				nodeGroup = nodeGroupMap.get(node.getUnit());
			}
			
			if (nodeGroup == null) nodeGroup = VisUtil.MISSING_VALUE;
		}

		return nodeGroup;
	}

	private String getSize(Node node, Question question, String attr) {
		if (node == null || question == null || attr == null)
			return DEFAULT_SCALE_VALUE;

		String attrValue = node.getAttribute(attr);
		if (attrValue == null) {
			return DEFAULT_SCALE_VALUE;
		}

		if (question.isRating()) {
			// logger.debug("attrName: " + attr + ", attrValue: " + attrValue);
			String scaleName = Question.getScaleNameFromKey(attrValue);
			Scale scale = question.getScaleByName(scaleName);
			return scale.getValue().toString();
		} else if (question.isContinuous()) {
			return attrValue;
		} else
			return DEFAULT_SCALE_VALUE;
	}

	private String composeHtml(String title, String path, String maxRecSize, String minRecSize, String colorBy,
			String groupBy, String shapeBy, String hideNodeLabel, String os) {
		StringBuilder html = new StringBuilder();
		String debug = "0";
		if (nodeIdStr.length() > 0) {
			logger.info("composing applet code...");
			html.append("<html>").append("\n");

			html
					.append(
							"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>")
					.append("\n");

			if (os.equalsIgnoreCase("mac")) {
				html
						.append("<applet archive=\"./graphApplet5.jar, ./jh.jar, ./jhall.jar, ./jhbasic.jar, ./jsearch.jar, ./swinglayout.jar, ./prefuse5.jar\" code=\"admin.MainFrame.class\" width=\"100%\" height=\"100%\">\n\n");
			} else {
				html
						.append("<applet archive=\"./graphApplet.jar, ./jh.jar, ./jhall.jar, ./jhbasic.jar, ./jsearch.jar, ./swinglayout.jar, ./prefuse.jar\" code=\"admin.MainFrame.class\" width=\"100%\" height=\"100%\">\n\n");
			}

			// node related
			html.append("<param name=login value=\"" + nodeIdStr.toString()
					+ "\">\n");
			if(!hideNodeLabel.equalsIgnoreCase("2"))
			html.append("<param name=nodes value=\""
					+ nodeLabelStr.toString().replace("\"", "'") + "\">\n");
			html.append("<param name=nodeTypes value=\""
					+ nodeTypesStr.toString().replace("\"", "'") + "\">\n");
			html.append("<param name=colors value=\"" + nodeColorStr.toString()
					+ "\">\n");
			html.append("<param name=shapeAttri value=\""
					+ nodeShapeStr.toString().replace("\"", "'") + "\">\n");
			html.append("<param name=groupAttri value=\""
					+ nodeGroupStr.toString().replace("\"", "'") + "\">\n");
			if (sizeAttrName != null)
				html.append("<param name=nodeSize value=\""
						+ nodeSizeStr.toString() + "\">\n");
			if (sizeAttrName != null && sizeAttrName2 != null)
				html.append("<param name=nodeSize2 value=\""
						+ nodeSizeStr2.toString() + "\">\n");
			html.append("<param name=hiddenNodes value=\""
					+ hiddenNodesStr.toString() + "\">\n");
			html.append("<param name=images value=\"" + nodeImageStr.toString()
					+ "\">\n\n");
			html.append("<param name=username value=\""
					+ nodeUserStr.toString() + "\">\n\n");
			// edge related
			html.append("<param name=edges value=\"" + edgeStr.toString()
					+ "\">\n");
			html.append("<param name=edgeTypes value=\""
					+ edgeTypesStr.toString().replace("\"", "'") + "\">\n");
			html.append("<param name=edgeDirection value=\""
					+ edgeDirectionStr.toString() + "\">\n");
			html.append("<param name=edgeTypeDis value=\""
					+ edgeLabelStr.toString().replace("\"", "'") + "\">\n");
			html.append("<param name=strengths value=\""
					+ edgeWeightStr.toString() + "\">\n");
			html.append("<param name=edgeColors value=\""
					+ edgeColorStr.toString() + "\">\n");
			html.append("<param name=hiddenEdges value=\""
					+ hiddenEdgesStr.toString() + "\">\n\n");

			// legends
			html.append("<param name=group value=\""
					+ groupLegendStr.toString().replace("\"", "'") + "\">\n");
			html.append("<param name=thelegend value=\""
					+ nodeLegendStr.toString().replace("\"", "'") + "\">\n");
			html.append("<param name=linkLegend value=\""
					+ edgeLegendStr.toString().replace("\"", "'") + "\">\n\n");

			// general

			// html.append("<!-- Recommendation||ur_depth||isUr_hidden||rt_depth||isRt_hidden||utHasDirLink||RecScores||nodesInBothPath -->\n");
			if (maxRecSize != null)
				html.append("<param name=recMaxSize value=\"" + maxRecSize + "\">\n");
			if (minRecSize != null)
				html.append("<param name=recMinSize value=\"" + minRecSize + "\">\n");
			
			html.append("<param name=title value=\"" + title + "\">\n");
			if (path != null)
				html.append("<param name=path value=\"" + path + "\">\n");
			
			html.append("<param name=link_prefix value=\"" + linkURLPrefix
					+ "\">\n");
			html.append("<param name=node_prefix value=\"" + nodeURLPrefix
					+ "\">\n");
			html.append("<param name=cgi_url value=\"\">\n");
			// html.append("<param name=directional value=" + directed + ">\n");
			html.append("<param name=colorBy value=\"" + colorBy + "\">\n");
			html.append("<param name=groupBy value=\"" + groupBy + "\">\n");
			html.append("<param name=shapeBy value=\"" + shapeBy + "\">\n");
			html.append("<param name=os value=\"" + os + "\">\n");
			html.append("<param name=debug value=\"" + debug + "\">\n");
			html.append("<param name=hideNodeLabel value=\"" + hideNodeLabel
					+ "\">\n");
			html.append("<param name=default_width value=1200>\n");
			html.append("<param name=default_height value=800>\n\n");

			html.append("</applet>");
			html.append("</html>");
		} else {
			html
					.append("There is no valid node for visualization, please close this window and try again!");
		}

		return html.toString();
	}

	public QuestionDao getQuestionDao() {
		return questionDao;
	}

	public void setQuestionDao(QuestionDao questionDao) {
		this.questionDao = questionDao;
	}

	public GenericRO getGenericRO() {
		return genericRO;
	}

	public void setGenericRO(GenericRO genericRO) {
		this.genericRO = genericRO;
	}

	public Node getSourceNode() {
		return sourceNode;
	}

	public void setSourceNode(Node sourceNode) {
		this.sourceNode = sourceNode;
	}

	public Set<Node> getRecNodes() {
		return recNodes;
	}

	public void setRecNodes(Set<Node> recNodes) {
		this.recNodes = recNodes;
	}

	public Node getTargetNode() {
		return targetNode;
	}

	public void setTargetNode(Node targetNode) {
		this.targetNode = targetNode;
	}

	public Collection<Node> getFocalNodes() {
		return focalNodes;
	}

	public void setFocalNodes(Collection<Node> focalNodes) {
		this.focalNodes = focalNodes;
	}


	public static void main(String[] args) throws Exception {
		Beans.init();
		if (true)
			testCustomNetwork();
		if (false)
			testLocalNetwork();
		if (false)
			testRecommenderNetwork();

	}

	@SuppressWarnings("unchecked")
	private static void testCustomNetwork() throws Exception {
		String[] edgeTypes = { "Authorship" };
		String[] nodeAttributes = {};
		String attributeCombineMethod = "or";
		String questionCombineMethod = "and";
		String showIsolate = "2";
		String showRawRelation = "0";
		String colorQuestionId = "type";
		String shapeQuestionId = "type";
		String groupQuestionId = "type";
		String sizeQuestionId = null;
		String sizeQuestionId2 = null;

		// get nodes, edges
		NetworkExtractor extractor = (NetworkExtractor) Beans
				.getBean("networkExtractor");
		Map m = extractor.getCustomNetwork(Arrays.asList(edgeTypes), "or",
				null, "or", null, "or", Arrays.asList(nodeAttributes),
				attributeCombineMethod, questionCombineMethod, showIsolate,
				showRawRelation);
		Collection<Node> nodes = (Collection<Node>) m.get("nodes");
		Collection<Edge> edges = (Collection<Edge>) m.get("edges");

		if (nodes.size() == 0 || edges.size() == 0) {
			logger.warn("There is no valid data for Visualization.");
			return;
		}

		// write applet code
		AppletWriter writer = new AppletWriter();
		String title = "Custom Network";
		Set<Node> hiddenNodes = new HashSet<Node>();
		writer.write(new FileOutputStream("results/custom_network2.html"),
				title, null, null, null, nodes, edges, hiddenNodes, null, null, null, null,
				colorQuestionId, shapeQuestionId, groupQuestionId,
				sizeQuestionId, sizeQuestionId2, null, "0", "0", "Win");
	}

	@SuppressWarnings("unchecked")
	private static void testLocalNetwork() throws Exception {
		// String[] edgeTypes = {"Authorship"};
		String[] edgeTypes = {};
		String nodeId = "1";
		String depth = "1";
		String includeDerivedEdges = "1";

		String colorQuestionId = "type";
		String shapeQuestionId = "type";
		String groupQuestionId = "type";
		String sizeQuestionId = null;
		String sizeQuestionId2 = null;

		// get nodes, edges
		NetworkExtractor extractor = (NetworkExtractor) Beans
				.getBean("networkExtractor");
		NodeDao nodeDao = (NodeDao) Beans.getBean("nodeDao");
		List<Node> focalNodes = new LinkedList<Node>();
		List<Long> rootIDs = new ArrayList<Long>();
		Long nid = Long.parseLong(nodeId);
		rootIDs.add(nid);
		focalNodes.add(nodeDao.findById(nid));
		Map m = extractor.getLocalNetwork(rootIDs, Integer.parseInt(depth),
				includeDerivedEdges.equals("1"), false,
				KNeighborhoodFilter.IN_OUT, Arrays.asList(edgeTypes));
		Collection<Node> rawNodes = (Collection<Node>) m.get("nodes");
		Collection<Node> nodes = new LinkedList<Node>();

		for (Node node : rawNodes) {
			nodes.add(nodeDao.loadById(node.getId()));
		}
		Collection<Edge> edges = (Collection<Edge>) m.get("edges");

		if (nodes.size() == 0 || edges.size() == 0) {
			logger.warn("There is no valid data for Visualization.");
			return;
		}

		// write applet code
		AppletWriter writer = new AppletWriter();
		writer.setFocalNodes(focalNodes);
		String title = "Local Network";
		Set<Node> hiddenNodes = new HashSet<Node>();
		writer.write(new FileOutputStream("local_network.html"), title, null,null, null,
				nodes, edges, hiddenNodes, null, null, null,null, colorQuestionId,
				shapeQuestionId, groupQuestionId, sizeQuestionId,
				sizeQuestionId2, null, "0", "0", "Win");
	}


	private static void testRecommenderNetwork() throws Exception {
		String sourceId = "8";
		String[] recIds = new String[1];
		recIds[0] = "1911";
		String targetLabel = "media";
		String targetName = "media";
		String colorQuestionId = null;
		String shapeQuestionId = "type";
		String groupQuestionId = null;
		String sizeQuestionId = null;
		String sizeQuestionId2 = null;

		RecommenderNetwork rn = new RecommenderNetwork(
				Long.parseLong(sourceId), recIds, null, null, null, null, null,
				targetName, targetLabel, "50", "-1");

		Set<Node> nodeSet = new HashSet<Node>();
		nodeSet.addAll(rn.getUser_RecNodes());
		nodeSet.addAll(rn.getRec_targetNodes());
		nodeSet.add(rn.sourceNode);
		nodeSet.addAll(rn.recNodes);
		nodeSet.add(rn.targetNode);
		Collection<Node> nodes = new LinkedList<Node>();
		NodeDao nodeDao = (NodeDao) Beans.getBean("nodeDao");
		for (Node node : nodeSet) {
			nodes.add(nodeDao.loadById(node.getId()));
		}
		Collection<Edge> edges = rn.getEdges();
		List<Node> hiddenNodes = rn.getHiddenNodes();
		List<Node> hiddenNodes1 = rn.getHiddenNodes1();
		Set<Long> hiddenEdges = rn.getHiddenEdgeIds();
		Set<Long> hiddenEdges1 = rn.getHiddenEdge1Ids();
		Set<Long> hiddenEdges2 = rn.getHiddenEdge2Ids();
		String title = rn.getTitleStr();
		String path = rn.getPathStr();
		String maxRecSize = rn.getMaxRecSize();
		String minRecSize = rn.getMinRecSize();

		if (nodes.size() == 0 || edges.size() == 0) {
			logger.warn("There is no valid data for Visualization.");
			return;
		}

		// write applet code
		AppletWriter writer = new AppletWriter();
		writer.setSourceNode(rn.sourceNode);
		writer.setRecNodes(rn.recNodes);
		writer.setTargetNode(rn.targetNode);
		writer.write(new FileOutputStream("recommender_network.html"), title,
				path, maxRecSize, minRecSize, nodes, edges, hiddenNodes, hiddenNodes1, hiddenEdges, hiddenEdges1,hiddenEdges2,
				colorQuestionId, shapeQuestionId, groupQuestionId,
				sizeQuestionId, sizeQuestionId2, null, "0", "0", "Win");
	}
}
