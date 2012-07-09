package ciknow.vis;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ciknow.dao.EdgeDao;
import ciknow.dao.NodeDao;
import ciknow.dao.QuestionDao;
import ciknow.dao.RoleDao;
import ciknow.domain.*;
import ciknow.util.Beans;
import ciknow.util.Constants;
import ciknow.util.VisUtil;
import edu.uci.ics.jung.graph.filters.impl.KNeighborhoodFilter;


@SuppressWarnings("unchecked")
public class NetworkExtractor {
    private static Log logger = LogFactory.getLog(NetworkExtractor.class);
    private EdgeDao edgeDao;
    private NodeDao nodeDao;
    private RoleDao roleDao;
    private QuestionDao questionDao;
    
    public static void main(String[] args){
//    	String filter = "F`a`b#eq#S`a`b";
//    	String[] parts = filter.split("#");
//    	logger.debug(parts[0]);
//    	logger.debug(parts[1]);
//    	logger.debug(parts[2]);
    	
		Beans.init();
		
		String[] edgeTypes = {"Authorship"};
		String[] nodeAttributes = {"F`PI_type`awarded", "F`PI_type`unawarded"};
		String attributeCombineMethod = "or";
		String questionCombineMethod = "and";
		String showIsolate = "0";
		String showRawRelation = "0";
		
        NetworkExtractor extractor = (NetworkExtractor) Beans.getBean("networkExtractor");
        Map m = extractor.getCustomNetwork(Arrays.asList(edgeTypes), "or", null, "or", null, "or", 
        							Arrays.asList(nodeAttributes), 
        							attributeCombineMethod, 
        							questionCombineMethod, 
        							showIsolate, showRawRelation);
        Collection<Node> nodes = (Collection<Node>) m.get("nodes");
        Collection<Edge> edges = (Collection<Edge>) m.get("edges");
        
        logger.info("got " + nodes.size() + " nodes and " + edges.size() + " edges.");
    }
    
    public NetworkExtractor() {

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

	public QuestionDao getQuestionDao() {
		return questionDao;
	}

	public void setQuestionDao(QuestionDao questionDao) {
		this.questionDao = questionDao;
	}

	/**
	 * get local network
	 * @param rootIDs
	 * @param radius
	 * @param includeDerivedEdges - not used in this implementation
	 * @param direction
	 * @param edgeTypes
	 * @return
	 */
	public Map getLocalNetwork(Collection<Long> rootIDs, int radius, boolean includeDerivedEdges, boolean includeEmptyEdges, int direction, Collection<String> edgeTypes){
    	logger.info("get local network...");
    	logger.debug("rootIDs: " + rootIDs);
    	logger.debug("depth: " + radius);
    	logger.debug("direction: " + direction);
    	logger.debug("edgeTypes: " + edgeTypes);
    	
    	Map map = new HashMap();
    	Set<Long> hiddenNodeIds = new HashSet<Long>(roleDao.getNodeIdsByRoleId(2L));
    	
    	logger.debug("preparing root nodes...");
    	Set<Node> sources = new HashSet<Node>();
    	for (Long nodeId : rootIDs){
    		Node node = nodeDao.findById(nodeId);
    		if (node != null) sources.add(node);
    	}

    	logger.debug("get nodes...");
    	Set<Node> nodeSet = new HashSet<Node>();
    	if (sources != null && !sources.isEmpty()) {
    		getNeighbors(nodeSet, sources, radius, includeDerivedEdges, includeEmptyEdges, edgeTypes, direction, hiddenNodeIds);
    	}

    	logger.debug("get edges...");
    	Set<Edge> edgeSet = new HashSet<Edge>();
    	if (!nodeSet.isEmpty()) edgeSet = new HashSet<Edge>(edgeDao.loadEdgesAmongNodes(nodeSet, includeDerivedEdges, includeEmptyEdges, edgeTypes));
    	
    	
        map.put("nodes", nodeSet);
    	map.put("edges", edgeSet);
    	
        logger.info("gotHibernateLocalNetwork: nodes=" + nodeSet.size() + ", edges=" + edgeSet.size());
    	return map;
    }
	
    private void getNeighbors(Set<Node> nodeSet, Collection<Node> sources, int depth, boolean includeDerivedEdges, boolean includeEmptyEdges, Collection<String> edgeTypes, int direction, Set<Long> hiddenNodeIds){
    	// add non-hidden nodes to nodeSet
    	Set<Node> validSources = new HashSet<Node>();
    	for (Node node : sources){
    		if (hiddenNodeIds.contains(node.getId())) continue;
    		validSources.add(node);
    		nodeSet.add(node);
    	}
    	
    	logger.debug("size: " + nodeSet.size() + ", depth to go: " + depth);
    	if (!validSources.isEmpty() && depth > 0){
    		Collection<Node> nodes;
    		if (direction == KNeighborhoodFilter.IN_OUT){
    			nodes = nodeDao.findNeighbors(validSources, includeDerivedEdges, includeEmptyEdges, edgeTypes);
    		} else if (direction == KNeighborhoodFilter.IN){
    			nodes = nodeDao.findConnecting(validSources, includeDerivedEdges);
    		} else if (direction == KNeighborhoodFilter.OUT){
    			nodes = nodeDao.findConnected(validSources, includeDerivedEdges);
    		} else {
    			nodes = new HashSet<Node>();
    		}

    		if (nodes.isEmpty()) return;
    		
    		depth--;
    		getNeighbors(nodeSet, nodes, depth, includeDerivedEdges, includeEmptyEdges, edgeTypes, direction, hiddenNodeIds);
    	}
    }
   
    /**
     * 
     * @param edgeTypes
     * @param operator
     * @param nodeFilters
     * @param nfc
     * @param edgeFilters
     * @param efc
     * @param nodeAttributes
     * @param attr_combineMethod
     * @param que_combineMethod
     * @param showIsolate - if 1, then show all non-hidden nodes of types bound to given edge types
     * @param showRawRelation - if 0, then survey question metadata is used to filter edges
     * @return
     */
   public Map getCustomNetwork(Collection<String> edgeTypes, String operator,
			Collection<String> nodeFilters, String nfc,
			Collection<String> edgeFilters, String efc,
			Collection<String> nodeAttributes, String attr_combineMethod,
			String que_combineMethod, String showIsolate, String showRawRelation) {
		logger.info("get custom network ...");
		logger.debug("edgeTypes: " + edgeTypes);
		logger.debug("edge operator: " + operator);
		logger.debug("nodeFilters: " + nodeFilters);
		logger.debug("nfc: " + nfc);
		logger.debug("edgeFilters: " + edgeFilters);
		logger.debug("efc: " + efc);
		logger.debug("nodeAttributes: " + nodeAttributes);
		logger.debug("attributeCombineMethod: " + attr_combineMethod);
		logger.debug("questionCombineMethod: " + que_combineMethod);
		logger.debug("showIsolate: " + showIsolate);
		logger.debug("showRawRelation: " + showRawRelation);
		
		if (operator.equals("and")) return getCustomNetwork_and(edgeTypes, 
															nodeFilters, nfc, 
															edgeFilters, efc, 
															nodeAttributes, 
															attr_combineMethod, 
															que_combineMethod, 
															showIsolate, showRawRelation);
		else return getCustomNetwork_or(edgeTypes, 
								nodeFilters, nfc, 
								edgeFilters, efc, 
								nodeAttributes, 
								attr_combineMethod, 
								que_combineMethod, 
								showIsolate, showRawRelation);
	}

   private Map getCustomNetwork_or(Collection<String> edgeTypes,
			Collection<String> nodeFilters, String nfc,
			Collection<String> edgeFilters, String efc,
			Collection<String> nodeAttributes, String attr_combineMethod,
			String que_combineMethod, String showIsolate, String showRawRelation){
	   
		Map<Long, Question> questionMap = new HashMap<Long, Question>();
		Map<String, Question> shortNameToQuestionMap = new HashMap<String, Question>();
		List<Question> questions = questionDao.getAll();
		for (Question q : questions) {
			questionMap.put(q.getId(), q);
			shortNameToQuestionMap.put(q.getShortName(), q);
		}

		Set<Long> hiddenNodeIds = new HashSet<Long>(roleDao.getNodeIdsByRoleId(2L));
		Map<Long, Node> fullNodeMap = new HashMap<Long, Node>();

		logger.debug("get edges from database...");
		Set<Edge> eligibleEdges = new HashSet<Edge>();
		Set<Node> eligibleNodes = new HashSet<Node>();
		for (String edgeType : edgeTypes) {
			logger.debug("edgeType: " + edgeType);
			List<Edge> edges = edgeDao.loadByType(edgeType, false);
			
			Set<Long> visibleNodeSet = new HashSet<Long>();
			Set<Long> availableNodeIds = null;
			Set<Long> availableNodeIds2 = null;
			List<String> tagNames = null;
			Question q = null;
			if (showRawRelation.equals("0")){
				if (edgeType.startsWith(Constants.TAGGING_PREFIX)){
					q = shortNameToQuestionMap.get(edgeType.substring(Constants.TAGGING_PREFIX.length()));
				} else if (edgeType.contains(Constants.SEPERATOR)){
					q = shortNameToQuestionMap.get(edgeType.substring(0, edgeType.indexOf(Constants.SEPERATOR)));
				} else q = shortNameToQuestionMap.get(edgeType);
				
				if (q != null) {
					List<Node> visibleNodes = nodeDao.findByIds(q.getVisibleNodeIds());
					for (Node node : visibleNodes){
						visibleNodeSet.add(node.getId());
					}
					
					availableNodeIds = q.getAvailableNodeIds(false);
					if (q.isPerceivedRelationalChoice() || q.isPerceivedRelationalRating()){
						availableNodeIds2 = q.getAvailableNodeIds(true);
					} else if (q.isPerceivedChoice()){
						tagNames = q.getTagNames4PerceivedChoice();
					} else if (q.isPerceivedRating()){
						tagNames = q.getTagNames4PerceivedRating();
					}
				}				
			}
			
			logger.debug("filtering...");
			for (Edge edge : edges) {
				if (edge.isEmpty()) continue;
				
				Node fnode = edge.getFromNode();
				Node tnode = edge.getToNode();
				Node creator = edge.getCreator();
				// logger.debug("edge(id=" + edge.getId() + ")
				// --------------------");
				if (!passEdgeFilter(edge, edgeFilters,
						efc, questionMap)) {
					// logger.debug("edge(id=" + edge.getId() + ") cannot pass
					// filter.");
					continue;
				}

				Long fid = edge.getFromNode().getId();
				Long tid = edge.getToNode().getId();
				if (hiddenNodeIds.contains(fid)) {
					// logger.debug("hidden node(s), ignored.");
					continue;
				}
				if (hiddenNodeIds.contains(tid)) {
					// logger.debug("hidden node(s), ignored.");
					continue;
				}
				if (q != null) {
					if (q.isPerceivedChoice() || q.isPerceivedRating()){
						if (!visibleNodeSet.contains(creator.getId())) continue;
						if (!availableNodeIds.contains(fnode.getId())) continue;
						if (!tagNames.contains(tnode.getUsername())) continue;
					} else if (q.isPerceivedRelationalChoice() || q.isPerceivedRelationalRating()){
						if (!visibleNodeSet.contains(creator.getId())) continue;
						if (!availableNodeIds.contains(fnode.getId())) continue;
						if (!availableNodeIds2.contains(tnode.getId())) continue;
					} else {
						if (!visibleNodeSet.contains(fnode.getId())) continue;
						if (!availableNodeIds.contains(tnode.getId())) continue;					
					}
				}

				Node fromNode = fullNodeMap.get(fid);
				if (fromNode == null) {
					fromNode = nodeDao.loadById(fid);
					fullNodeMap.put(fid, fromNode);
				}
				if (!passNodeFilter(fromNode, nodeFilters, nfc, questionMap)) {
					// logger.debug("fromNode(id=" + fromNode.getId() + ")
					// cannot pass filter.");
					continue;
				}

				Node toNode = fullNodeMap.get(tid);
				if (toNode == null) {
					toNode = nodeDao.loadById(tid);
					fullNodeMap.put(tid, toNode);
				}
				if (!passNodeFilter(toNode, nodeFilters, nfc, questionMap)) {
					// logger.debug("toNode(id=" + toNode.getId() + " cannot
					// pass filter.");
					continue;
				}
				
				eligibleEdges.add(edge);
				eligibleNodes.add(fromNode);
				eligibleNodes.add(toNode);
			}
		}
		
		if (!eligibleEdges.isEmpty() && showIsolate.equals("1")) {
			logger.info("adding isolates...");
			Collection<String> nodeTypes = edgeDao.getNodeTypesByEdgeTypes(edgeTypes);
			for (String nodeType : nodeTypes) {
				List<Node> nodes = nodeDao.loadByType(nodeType);
				for (Node node : nodes){
					if (hiddenNodeIds.contains(node.getId())) continue;
					if (!passNodeFilter(node, nodeFilters, nfc, questionMap)) continue;
					eligibleNodes.add(node);
				}				
			}
		}

		Map m = new HashMap();
		m.put("nodes", eligibleNodes);
		m.put("edges", eligibleEdges);

		logger.info("get custom network(operator=or) done: nodes=" + eligibleNodes.size() + ", edges=" + eligibleEdges.size());
		return m;
   }
   
   private Map getCustomNetwork_and(Collection<String> edgeTypes,
			Collection<String> nodeFilters, String nfc,
			Collection<String> edgeFilters, String efc,
			Collection<String> nodeAttributes, String attr_combineMethod,
			String que_combineMethod, String showIsolate, String showRawRelation){
	   
		Map<Long, Question> questionMap = new HashMap<Long, Question>();
		Map<String, Question> shortNameToQuestionMap = new HashMap<String, Question>();
		List<Question> questions = questionDao.getAll();
		for (Question q : questions) {
			questionMap.put(q.getId(), q);
			shortNameToQuestionMap.put(q.getShortName(), q);
		}

		Set<Long> hiddenNodeIds = new HashSet<Long>(roleDao.getNodeIdsByRoleId(2L));
		Map<Long, Node> fullNodeMap = new HashMap<Long, Node>();

		logger.debug("get edges from database...");
		// node -> connected edges
		Map<Node, Map<Node, Set<Edge>>> nodeToNodeMap = new HashMap<Node, Map<Node, Set<Edge>>>();

		boolean firstEdgeType = true;
		for (String edgeType : edgeTypes) {
			logger.debug("edgeType: " + edgeType);
			List<Edge> edges = edgeDao.loadByType(edgeType, false);
			
			Set<Long> visibleNodeSet = new HashSet<Long>();
			Question q = null;
			if (showRawRelation.equals("0")){				
				if (edgeType.startsWith(Constants.TAGGING_PREFIX)){
					q = shortNameToQuestionMap.get(edgeType.substring(Constants.TAGGING_PREFIX.length()));
				} else if (edgeType.startsWith(Constants.SEPERATOR)){
					q = shortNameToQuestionMap.get(edgeType.substring(0, edgeType.indexOf(Constants.SEPERATOR)));
				} else q = shortNameToQuestionMap.get(edgeType);
				List<Node> visibleNodes = new ArrayList<Node>();
				if (q != null) visibleNodes = nodeDao.findByIds(q.getVisibleNodeIds());			
				for (Node node : visibleNodes){
					visibleNodeSet.add(node.getId());
				}			
			}
			
			for (Edge edge : edges) {
				if (edge.isEmpty()) continue;
				
				// logger.debug("edge(id=" + edge.getId() + ")
				// --------------------");
				if (!passEdgeFilter(edge, edgeFilters, efc, questionMap)) {
					// logger.debug("edge(id=" + edge.getId() + ") cannot pass
					// filter.");
					continue;
				}

				Long fid = edge.getFromNode().getId();
				Long tid = edge.getToNode().getId();
				if (hiddenNodeIds.contains(fid) || hiddenNodeIds.contains(tid)) {
					// logger.debug("hidden node(s), ignored.");
					continue;
				}
				if (q != null && !visibleNodeSet.contains(fid)) {
					logger.debug("node not visible to the question, ignored.");
					continue;
				}
				
				Node fromNode = fullNodeMap.get(fid);
				if (fromNode == null) {
					fromNode = nodeDao.loadById(fid);
					fullNodeMap.put(fid, fromNode);
				}
				if (!passNodeFilter(fromNode, nodeFilters, nfc, questionMap)) {
					// logger.debug("fromNode(id=" + fromNode.getId() + ")
					// cannot pass filter.");
					continue;
				}

				Node toNode = fullNodeMap.get(tid);
				if (toNode == null) {
					toNode = nodeDao.loadById(tid);
					fullNodeMap.put(tid, toNode);
				}
				if (!passNodeFilter(toNode, nodeFilters, nfc, questionMap)) {
					// logger.debug("toNode(id=" + toNode.getId() + " cannot
					// pass filter.");
					continue;
				}

				//logger.debug("firstEdgeType: " + firstEdgeType);
				if (firstEdgeType){
					Map<Node, Set<Edge>> nodeToEdgeMap = nodeToNodeMap.get(fromNode);
					if (nodeToEdgeMap == null) {
						nodeToEdgeMap = new HashMap<Node, Set<Edge>>();
						nodeToNodeMap.put(fromNode, nodeToEdgeMap);
					}
					Set<Edge> edgeSet = nodeToEdgeMap.get(toNode);
					if (edgeSet == null){
						edgeSet = new HashSet<Edge>();
						nodeToEdgeMap.put(toNode, edgeSet);
					}
					edgeSet.add(edge);
				} else {
					Map<Node, Set<Edge>> nodeToEdgeMap = nodeToNodeMap.get(fromNode);
					if (nodeToEdgeMap == null) continue;
					Set<Edge> edgeSet = nodeToEdgeMap.get(toNode);
					if (edgeSet == null) continue;
					edgeSet.add(edge);
				}
			}
			
			firstEdgeType = false;
		}

		logger.debug("removing isolates if specified ...");
		Set<Edge> eligibleEdges = new HashSet<Edge>();
		Set<Node> eligibleNodes = new HashSet<Node>();
		for (Node fromNode : nodeToNodeMap.keySet()){
			Map<Node, Set<Edge>> nodeToEdgeMap = nodeToNodeMap.get(fromNode);			
			for (Node toNode : nodeToEdgeMap.keySet()){
				Set<Edge> edgeSet = nodeToEdgeMap.get(toNode);
				Set<String> types = new HashSet<String>();
				for (Edge e : edgeSet){
					types.add(e.getType());
				}
				if (types.size() == edgeTypes.size()){
					eligibleEdges.addAll(edgeSet);
					eligibleNodes.add(fromNode);
					eligibleNodes.add(toNode);
				}
			}				
		}
		
		if (!eligibleEdges.isEmpty() && showIsolate.equals("1")){
			logger.info("adding isolates...");
			Collection<String> nodeTypes = edgeDao.getNodeTypesByEdgeTypes(edgeTypes);
			for (String nodeType : nodeTypes) {
				eligibleNodes.addAll(nodeDao.loadByType(nodeType));
			}
		}
		
		Map m = new HashMap();
		m.put("nodes", eligibleNodes);
		m.put("edges", eligibleEdges);

		logger.info("get custom network(operator=and) done: nodes=" + eligibleNodes.size() + ", edges=" + eligibleEdges.size());
		return m;
  }
   
    /*
    public Map getCustomNetwork(Collection<String> edgeTypes, 
    							Collection<String> nodeFilters, String nfc,
    							Collection<String> edgeFilters, String efc,
    							Collection<String> nodeAttributes,
                                String attr_combineMethod, 
                                String que_combineMethod, 
                                String showIsolate) {
    	logger.info("get custom network ...");
    	logger.debug("edgeTypes: " + edgeTypes);
    	logger.debug("nodeFilters: " + nodeFilters);
    	logger.debug("nfc: " + nfc);
    	logger.debug("edgeFilters: " + edgeFilters);
    	logger.debug("efc: " + efc);
    	logger.debug("nodeAttributes: " + nodeAttributes);
    	logger.debug("attributeCombineMethod: " + attr_combineMethod);
    	logger.debug("questionCombineMethod: " + que_combineMethod);
    	logger.debug("showIsolate: " + showIsolate);
    	
    	Map<Long, Question> questionMap = new HashMap<Long, Question>();
    	List<Question> questions = questionDao.getAll();
    	for (Question q : questions){
    		questionMap.put(q.getId(), q);
    	}
    	
        Set<Long> hiddenNodeIds = new HashSet<Long>(roleDao.getNodeIdsByRoleId(2L));
        Map<Long, Node> fullNodeMap = new HashMap<Long, Node>();
        
        logger.debug("get edges from database...");  
        // node -> connected edges
        Map<Node, Set<Edge>> nodeToEdgesMap = new HashMap<Node, Set<Edge>>();
        Set<Edge> edgeSet;
        for (String edgeType : edgeTypes) {
        	logger.debug("edgeType: " + edgeType);
        	List<Edge> edges = edgeDao.loadByType(edgeType);    
        	for (Edge edge : edges){
        		//logger.debug("edge(id=" + edge.getId() + ") --------------------");
        		if (!passAttributesFilter(edge.getAttributes(), edgeFilters, efc, questionMap)) {
        			//logger.debug("edge(id=" + edge.getId() + ") cannot pass filter.");
        			continue;
        		}
        		
                Long fid = edge.getFromNode().getId();
                Long tid = edge.getToNode().getId();
                if (hiddenNodeIds.contains(fid) || hiddenNodeIds.contains(tid)) {
                	//logger.debug("hidden node(s), ignored.");
                	continue;
                }
                
                Node fromNode = fullNodeMap.get(fid);
                if (fromNode == null) {
                	fromNode = nodeDao.loadById(fid);
                	fullNodeMap.put(fid, fromNode);
                }
                if (!passAttributesFilter(fromNode.getAttributes(), nodeFilters, nfc, questionMap)) {
                	//logger.debug("fromNode(id=" + fromNode.getId() + ") cannot pass filter.");
                	continue;
                }
                
                Node toNode = fullNodeMap.get(tid);
                if (toNode == null){
                	toNode = nodeDao.loadById(tid);
                	fullNodeMap.put(tid, toNode);
                }
                if (!passAttributesFilter(toNode.getAttributes(), nodeFilters, nfc, questionMap)) {
                	//logger.debug("toNode(id=" + toNode.getId() + " cannot pass filter.");
                	continue;
                }

                edgeSet = nodeToEdgesMap.get(fromNode);
                if (edgeSet == null) {
                    edgeSet = new HashSet<Edge>();
                    nodeToEdgesMap.put(fromNode, edgeSet);
                }
                edgeSet.add(edge);

                edgeSet = nodeToEdgesMap.get(toNode);
                if (edgeSet == null) {
                    edgeSet = new HashSet<Edge>();
                    nodeToEdgesMap.put(toNode, edgeSet);
                }
                edgeSet.add(edge);
        	}
        }        
        
        // this section is obsolete. corresponding interface has been removed from front end
        // but leaving it here won't hurt
        if (nodeAttributes != null && nodeAttributes.size() > 0) {
        	logger.debug("filtering by questions ...");
        	Map<Question, Set<String>> questionAttrMap = getQuestionAttrMap(nodeAttributes);
        	Map<Question, Set<Long>> visibleNodeIdMap = Question.getQuestionVisibleNodeIdsMap(questionAttrMap.keySet());
        	List<Node> badNodes = new LinkedList<Node>();
            for (Node node : nodeToEdgesMap.keySet()) {
                if (isEligible(node, questionAttrMap, visibleNodeIdMap, attr_combineMethod, que_combineMethod)) continue;
                
                edgeSet = nodeToEdgesMap.get(node);
                for (Edge edge : edgeSet){
                    Node fromNode = edge.getFromNode();
                    Node toNode = edge.getToNode();
                	Node n = node.equals(fromNode)?toNode:fromNode;
                	nodeToEdgesMap.get(n).remove(edge);
                }
                edgeSet.clear();
                badNodes.add(node);
            }
            for (Node node : badNodes) nodeToEdgesMap.remove(node);
            logger.debug(badNodes.size() + " nodes are filtered.");
        }

        logger.debug("removing isolates if specified ...");
        Set<Edge> eligibleEdges = new HashSet<Edge>();
        Set<Node> eligibleNodes = new HashSet<Node>();
        int i = 0;
        for (Node node : nodeToEdgesMap.keySet()){
        	edgeSet = nodeToEdgesMap.get(node);
        	eligibleEdges.addAll(edgeSet);
        	if (showIsolate.equals("0") && edgeSet.isEmpty()){
        		i++;
        	} else eligibleNodes.add(node);
        }
        
        if (showIsolate.equals("0")){
        	logger.debug(i + " isolates are removed.");
        } else {
        	Collection<String> nodeTypes = edgeDao.getNodeTypesByEdgeTypes(edgeTypes);
        	for (String nodeType : nodeTypes){
        		eligibleNodes.addAll(nodeDao.loadByType(nodeType));
        	}        	
        }
        
        Map m = new HashMap();
        m.put("nodes", eligibleNodes);
        m.put("edges", eligibleEdges);
        
        logger.info("get custom network done.");
        return m;
    }
    */


    private Map<Question, Set<String>> getQuestionAttrMap(Collection<String> attributes){
    	logger.info("get question -> required node attributes map...");
		Map<Question, Set<String>> questionAttrMap = new HashMap<Question, Set<String>>();
		for (String attribute : attributes){
			String shortName = Question.getShortNameFromKey(attribute);
			Question question = questionDao.findByShortName(shortName);
			Set<String> attrs = questionAttrMap.get(question);
			if (attrs == null){
				attrs = new HashSet<String>();
				questionAttrMap.put(question, attrs);
			}
			attrs.add(attribute);
		}
		return questionAttrMap;
    }
    
    /**
     * Filter node by attributes (defined by survey questions)
     * @param node					current examing node
     * @param questionAttrMap 		question -> required attributes map
     * @param visibleNodeIdMap		question -> visible nodeIds map
     * @param attr_combineMethod	operator among attributes
     * @param que_combineMethod		operator among questions
     * @return true is node is eligible
     */
    /*
    private boolean isEligible(Node node, 
    							Map<Question, Set<String>> questionAttrMap, 
    							Map<Question, Set<Long>> visibleNodeIdMap, 
    							String attr_combineMethod, 
    							String que_combineMethod) {

		// only consider questions which are visible to current node
    	List<Question> questions = new ArrayList<Question>();
    	Set<String> attributes = new HashSet<String>(); 
		for (Question question : questionAttrMap.keySet()){
			Set<Long> visibleIds = visibleNodeIdMap.get(question);
			if (visibleIds.contains(node.getId())) {
				questions.add(question);
				attributes.addAll(questionAttrMap.get(question));
				//logger.debug("filtering question(shortName): " + question.getShortName());
			}
		}
		
        if (attributes.size() == 0) return true;
        else if (node.getAttributes().isEmpty()) return false;

        // set default combineMethods
		if (attr_combineMethod == null) attr_combineMethod = "or";
		if (que_combineMethod == null) que_combineMethod = "and";

		// operator logic, read/modify carefully
		// note that "retainAll()" will change the data collection, so make copy
		Set<String> requiredAttributes; // for each question
		Set<String> existingAttributes;
		if (que_combineMethod.equalsIgnoreCase("and")){
			if (attr_combineMethod.equalsIgnoreCase("and")){
				existingAttributes = node.getAttributes().keySet();
				return existingAttributes.containsAll(attributes);
			} 
			else{
				for (Question question : questions){					
					requiredAttributes = questionAttrMap.get(question);
					existingAttributes = new HashSet<String>(node.getAttributes().keySet());
					existingAttributes.retainAll(requiredAttributes);
					if (existingAttributes.isEmpty()) return false;
				}
				return true;
			}
		} else {
			if (attr_combineMethod.equalsIgnoreCase("and")){
				for (Question question : questions){					
					requiredAttributes = questionAttrMap.get(question);
					existingAttributes = node.getAttributes().keySet();
					if (existingAttributes.containsAll(requiredAttributes)) return true;
				}
				return false;					
			} else{
				for (Question question : questions){
					requiredAttributes = questionAttrMap.get(question);
					existingAttributes = new HashSet<String>(node.getAttributes().keySet());
					existingAttributes.retainAll(requiredAttributes);
					if (!existingAttributes.isEmpty()) return true;
				}
				return false;
			}
		}
    }
	*/
    
    public static boolean passNodeFilter(Node node, Collection<String> filters, String combination, Map<Long, Question> qmap){
    	if (filters == null || filters.isEmpty()) {
    		return true;
    	}
    	
    	Map<String, String> attributes = node.getAttributes();
    	if (attributes == null || attributes.isEmpty()) {
    		return true;
    	}
    	
    	if (combination.equals("and")){
	    	for (String filter : filters){
	    		//logger.debug("filter: " + filter);
	    		if (filter.indexOf(VisUtil.ATTR_PREFIX) == 0) {
		    		String[] parts = filter.split("-.-", -1);
		    		String attrName = parts[0].substring(VisUtil.ATTR_PREFIX.length());
		    		String operator = parts[1];
		    		String attrValue = parts[2];
		    		String realValue = attributes.get(attrName);
					//logger.debug("realValue: " + realValue);
					
		    		// keep node with missing value
					//if (realValue == null) continue;
		    		// discard node with missing value
		    		if (realValue == null) return false;
		    		
					else if (operator.equals(Constants.EQUAL)){
		    			if (!realValue.equals(attrValue)) {
		    				return false;
		    			}
		    		} else if (operator.equals(Constants.NOT_EQUAL)){
		    			if (realValue.equals(attrValue)) {
		    				return false;
		    			}
		    		} else if (operator.equals(Constants.LESS_THAN)){
		    		} else if (operator.equals(Constants.GREATER_THAN)){
		    		} else {
		    			return false;
		    		}
	    		} else if (filter.indexOf(VisUtil.QUESTION_PREFIX) == 0){
		    		String[] parts = filter.split("-.-", -1);
		    		String qid = parts[0].substring(VisUtil.QUESTION_PREFIX.length());
		    		Question q = qmap.get(Long.parseLong(qid));
		    		Collection<String> possibleAttrNames = q.getPossibleAttributeNames();
		    		Set<String> attrs = new HashSet<String>(attributes.keySet());
		    		attrs.retainAll(possibleAttrNames);
		    		String operator = parts[1];
		    		String attr = parts[2];
		    		
		    		if (attrs.isEmpty()) { 
		    			//continue; 	// user never answer this question, ignored
		    			return false; 	// discard
		    		}
					else if (operator.equals(Constants.EQUAL)){
		    			if (!attrs.contains(attr)) {
		    				return false;
		    			}
		    		} else if (operator.equals(Constants.NOT_EQUAL)){
		    			if (attrs.contains(attr)) {
		    				return false;
		    			}
		    		} else if (operator.equals(Constants.LESS_THAN)){
		    		} else if (operator.equals(Constants.GREATER_THAN)){
		    		} else {}
	    		} else {
		    		String[] parts = filter.split("-.-", -1);
		    		String attrName = parts[0];
		    		String operator = parts[1];
		    		String attrValue = parts[2];
		    		String realValue = null;
					if (attrName.equals("type")) realValue = node.getType();
					else if (attrName.equals("city")) realValue = node.getCity();
					else if (attrName.equals("state")) realValue = node.getState();
					else if (attrName.equals("country")) realValue = node.getCountry();
					else if (attrName.equals("zipcode")) realValue = node.getZipcode();
					else if (attrName.equals("organization")) realValue = node.getOrganization();
					else if (attrName.equals("department")) realValue = node.getDepartment();
					else if (attrName.equals("unit")) realValue = node.getUnit();
					
					//logger.debug("realValue: " + realValue);
					
					if (realValue == null || realValue.trim().length() == 0) {
						//continue; 	// keep
						return false;	// discard
					}
					else if (operator.equals(Constants.EQUAL)){
		    			if (!realValue.equals(attrValue)) {
		    				return false;
		    			}
		    		} else if (operator.equals(Constants.NOT_EQUAL)){
		    			if (realValue.equals(attrValue)) {
		    				return false;
		    			}
		    		} else if (operator.equals(Constants.LESS_THAN)){
		    		} else if (operator.equals(Constants.GREATER_THAN)){
		    		} else {
		    			return false;
		    		}
	    		}
	    	}
	    	return true;
    	} else { // or
    		int count = 0;
	    	for (String filter : filters){
	    		//logger.debug("filter: " + filter);
	    		if (filter.indexOf(VisUtil.ATTR_PREFIX) == 0){
		    		String[] parts = filter.split("-.-", -1);
		    		String attrName = parts[0].substring(VisUtil.ATTR_PREFIX.length());
		    		String operator = parts[1];
		    		String attrValue = parts[2];
		    		String realValue = attributes.get(attrName);
					//logger.debug("realValue: " + realValue);
					
					if (realValue == null) {
						// count++;	// keep node with missing value						
						continue;
					}
					else if (operator.equals(Constants.EQUAL)){
		    			if (realValue.equals(attrValue)) {
		    				return true;
		    			}
		    		} else if (operator.equals(Constants.NOT_EQUAL)){
		    			if (!realValue.equals(attrValue)) {
		    				return true;
		    			}
		    		} else if (operator.equals(Constants.LESS_THAN)){
		    		} else if (operator.equals(Constants.GREATER_THAN)){
		    		} else {}
	    		} else if (filter.indexOf(VisUtil.QUESTION_PREFIX) == 0){
		    		String[] parts = filter.split("-.-", -1);
		    		String qid = parts[0].substring(VisUtil.QUESTION_PREFIX.length());
		    		Question q = qmap.get(Long.parseLong(qid));
		    		Collection<String> possibleAttrNames = q.getPossibleAttributeNames();
		    		Set<String> attrs = new HashSet<String>(attributes.keySet());
		    		attrs.retainAll(possibleAttrNames);
		    		String operator = parts[1];
		    		String attr = parts[2];
		    		
		    		if (attrs.isEmpty()) { 
		    			// count++;	// user never answer this question, ignored
		    			continue;
		    		}
					else if (operator.equals(Constants.EQUAL)){
		    			if (attrs.contains(attr)) {
		    				return true;
		    			}
		    		} else if (operator.equals(Constants.NOT_EQUAL)){
		    			if (!attrs.contains(attr)) {
		    				return true;
		    			}
		    		} else if (operator.equals(Constants.LESS_THAN)){
		    		} else if (operator.equals(Constants.GREATER_THAN)){
		    		} else {}
	    		} else {
		    		String[] parts = filter.split("-.-", -1);
		    		String attrName = parts[0];
		    		String operator = parts[1];
		    		String attrValue = parts[2];
		    		String realValue = null;
					if (attrName.equals("type")) realValue = node.getType();
					else if (attrName.equals("city")) realValue = node.getCity();
					else if (attrName.equals("state")) realValue = node.getState();
					else if (attrName.equals("country")) realValue = node.getCountry();
					else if (attrName.equals("zipcode")) realValue = node.getZipcode();
					else if (attrName.equals("organization")) realValue = node.getOrganization();
					else if (attrName.equals("department")) realValue = node.getDepartment();
					else if (attrName.equals("unit")) realValue = node.getUnit();
					
					//logger.debug("realValue: " + realValue);
					
					if (realValue == null || realValue.trim().length() == 0) {
						// count++;	// keep node with missing value
						continue;
					}
					else if (operator.equals(Constants.EQUAL)){
		    			if (realValue.equals(attrValue)) {
		    				return true;
		    			}
		    		} else if (operator.equals(Constants.NOT_EQUAL)){
		    			if (!realValue.equals(attrValue)) {
		    				return true;
		    			}
		    		} else if (operator.equals(Constants.LESS_THAN)){
		    		} else if (operator.equals(Constants.GREATER_THAN)){
		    		} else {}
	    		}
	    	}
	    	
	    	if (count == filters.size()) return true;
	    	else return false;
    	}
    	
    }
    
    public static boolean passEdgeFilter(Edge edge, Collection<String> filters, String combination, Map<Long, Question> qmap){
    	if (filters == null || filters.isEmpty()) {
    		return true;
    	}
    	Map<String, String> attributes = edge.getAttributes();
    	if (attributes == null || attributes.isEmpty()) {
    		return true;
    	}
    	
    	if (combination.equals("and")){
	    	for (String filter : filters){
	    		//logger.debug("filter: " + filter);
	    		if (filter.indexOf(VisUtil.ATTR_PREFIX) == 0) {
		    		String[] parts = filter.split("-.-", -1);
		    		String attrName = parts[0].substring(VisUtil.ATTR_PREFIX.length());
		    		String operator = parts[1];
		    		String attrValue = parts[2];
		    		String realValue = attributes.get(attrName);
					//logger.debug("realValue: " + realValue);
					
					if (realValue == null) continue;
					else if (operator.equals(Constants.EQUAL)){
		    			if (!realValue.equals(attrValue)) {
		    				return false;
		    			}
		    		} else if (operator.equals(Constants.NOT_EQUAL)){
		    			if (realValue.equals(attrValue)) {
		    				return false;
		    			}
		    		} else if (operator.equals(Constants.LESS_THAN)){
		    		} else if (operator.equals(Constants.GREATER_THAN)){
		    		} else {
		    			return false;
		    		}
	    		} else if (filter.indexOf(VisUtil.QUESTION_PREFIX) == 0){
		    		String[] parts = filter.split("-.-", -1);
		    		String qid = parts[0].substring(VisUtil.QUESTION_PREFIX.length());
		    		Question q = qmap.get(Long.parseLong(qid));
		    		Collection<String> possibleAttrNames = q.getPossibleAttributeNames();
		    		Set<String> attrs = new HashSet<String>(attributes.keySet());
		    		attrs.retainAll(possibleAttrNames);
		    		String operator = parts[1];
		    		String attr = parts[2];
		    		
		    		if (attrs.isEmpty()) { // user never answer this question, ignored
		    			continue;
		    		}
					else if (operator.equals(Constants.EQUAL)){
		    			if (!attrs.contains(attr)) {
		    				return false;
		    			}
		    		} else if (operator.equals(Constants.NOT_EQUAL)){
		    			if (attrs.contains(attr)) {
		    				return false;
		    			}
		    		} else if (operator.equals(Constants.LESS_THAN)){
		    		} else if (operator.equals(Constants.GREATER_THAN)){
		    		} else {}
	    		}
	    	}
	    	return true;
    	} else { // or
    		int count = 0;
	    	for (String filter : filters){
	    		//logger.debug("filter: " + filter);
	    		if (filter.indexOf(VisUtil.ATTR_PREFIX) == 0){
		    		String[] parts = filter.split("-.-", -1);
		    		String attrName = parts[0].substring(VisUtil.ATTR_PREFIX.length());
		    		String operator = parts[1];
		    		String attrValue = parts[2];
		    		String realValue = attributes.get(attrName);
					//logger.debug("realValue: " + realValue);
					
					if (realValue == null) {
						count++;
						continue;
					}
					else if (operator.equals(Constants.EQUAL)){
		    			if (realValue.equals(attrValue)) {
		    				return true;
		    			}
		    		} else if (operator.equals(Constants.NOT_EQUAL)){
		    			if (!realValue.equals(attrValue)) {
		    				return true;
		    			}
		    		} else if (operator.equals(Constants.LESS_THAN)){
		    		} else if (operator.equals(Constants.GREATER_THAN)){
		    		} else {}
	    		} else if (filter.indexOf(VisUtil.QUESTION_PREFIX) == 0){
		    		String[] parts = filter.split("-.-", -1);
		    		String qid = parts[0].substring(VisUtil.QUESTION_PREFIX.length());
		    		Question q = qmap.get(Long.parseLong(qid));
		    		Collection<String> possibleAttrNames = q.getPossibleAttributeNames();
		    		Set<String> attrs = new HashSet<String>(attributes.keySet());
		    		attrs.retainAll(possibleAttrNames);
		    		String operator = parts[1];
		    		String attr = parts[2];
		    		
		    		if (attrs.isEmpty()) { // user never answer this question, ignored
		    			count++;
		    			continue;
		    		}
					else if (operator.equals(Constants.EQUAL)){
		    			if (attrs.contains(attr)) {
		    				return true;
		    			}
		    		} else if (operator.equals(Constants.NOT_EQUAL)){
		    			if (!attrs.contains(attr)) {
		    				return true;
		    			}
		    		} else if (operator.equals(Constants.LESS_THAN)){
		    		} else if (operator.equals(Constants.GREATER_THAN)){
		    		} else {}
	    		}
	    	}
	    	
	    	if (count == filters.size()) return true;
	    	else return false;
    	}
    	
    }
}
