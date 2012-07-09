package ciknow.ro;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import ciknow.dao.EdgeDao;
import ciknow.dao.GroupDao;
import ciknow.dao.JobDao;
import ciknow.dao.NodeDao;
import ciknow.dao.PageDao;
import ciknow.dao.QuestionDao;
import ciknow.dao.SurveyDao;
import ciknow.domain.Edge;
import ciknow.domain.Field;
import ciknow.domain.Group;
import ciknow.domain.Job;
import ciknow.domain.Node;
import ciknow.domain.Page;
import ciknow.domain.Question;
import ciknow.domain.Survey;
import ciknow.dto.EdgeDTO;
import ciknow.dto.JobDTO;
import ciknow.dto.NodeDTO;
import ciknow.dto.Request;
import ciknow.dto.Response;
import ciknow.util.Beans;
import ciknow.util.Constants;
import ciknow.util.EdgeUtil;
import ciknow.util.GeneralUtil;
import ciknow.vis.NetworkExtractor;

/**
 * User: gyao
 * Date: Mar 6, 2008
 * Time: 11:30:35 AM
 */
public class EdgeRO {
    private static Log logger = LogFactory.getLog(EdgeRO.class);
    private EdgeDao edgeDao;
    private NodeDao nodeDao;
    private QuestionDao questionDao;
    private SurveyDao surveyDao;
    private PageDao pageDao;
    private GroupDao groupDao;
    private JobDao jobDao;
    private HibernateTemplate ht;
    
    public EdgeRO(){

    }

    public EdgeDao getEdgeDao() {
        return edgeDao;
    }

    public void setEdgeDao(EdgeDao edgeDao) {
        this.edgeDao = edgeDao;
    }

    public JobDao getJobDao() {
		return jobDao;
	}

	public void setJobDao(JobDao jobDao) {
		this.jobDao = jobDao;
	}

	public NodeDao getNodeDao() {
        return nodeDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        this.nodeDao = nodeDao;
    }

    public QuestionDao getQuestionDao() {
		return questionDao;
	}

	public void setQuestionDao(QuestionDao questionDao) {
		this.questionDao = questionDao;
	}

	public SurveyDao getSurveyDao() {
		return surveyDao;
	}

	public void setSurveyDao(SurveyDao surveyDao) {
		this.surveyDao = surveyDao;
	}

	public GroupDao getGroupDao() {
		return groupDao;
	}

	public void setGroupDao(GroupDao groupDao) {
		this.groupDao = groupDao;
	}

	public HibernateTemplate getHt() {
		return ht;
	}

	public void setHt(HibernateTemplate ht) {
		this.ht = ht;
	}

	/*
	public Map saveQuestionEdges(Map input){
		logger.info("saving/updating edges...");
		String qid = (String)input.get("qid");
		List<EdgeDTO> dtos = (List<EdgeDTO>)input.get("edges"); 
		dtos = saveEdges(dtos);
		
		Map output = new HashMap();
		output.put("edges", dtos);
		output.put("qid", qid);
		return output;
	}
	
	public Map deleteQuestionEdges(Map input){
		logger.info("deleting edges...");
		String qid = (String)input.get("qid");
		List<EdgeDTO> dtos = (List<EdgeDTO>)input.get("edges"); 
		List<Edge> edges = new LinkedList<Edge>();
		for (EdgeDTO dto : dtos){
			edges.add(edgeDao.getProxy(dto.edgeId));
		}
		edgeDao.delete(edges);
		
		Map output = new HashMap();
		output.put("edges", dtos);
		output.put("qid", qid);
		return output;
	}
	
	/////////////// save or update edges ////////////////////
	
    public EdgeDTO saveEdge(EdgeDTO dto){
    	Edge edge = dto2edge(dto);
    	edgeDao.save(edge);
    	return new EdgeDTO(edge);
    }
    
    public List<EdgeDTO> saveEdges(List<EdgeDTO> dtos){
    	List<Edge> edges = new LinkedList<Edge>();
    	List<EdgeDTO> edgeList = new LinkedList<EdgeDTO>();
    	
    	for (EdgeDTO dto : dtos){
    		edges.add(dto2edge(dto));
    	}
    	
    	edgeDao.save(edges);
    	
    	for (Edge edge : edges){
    		edgeList.add(new EdgeDTO(edge));
    	}
    	
    	return edgeList;
    }
    
    private Edge dto2edge(EdgeDTO dto){
        Edge edge;
        if (dto.edgeId == 0){
            logger.info("creating new edge...");
            edge = new Edge();
        } else {
            logger.info("updating edge id=" + dto.edgeId);
            edge = edgeDao.loadById(dto.edgeId);
            if (edge == null) return null;
            edge.setVersion(dto.version);
        }

        Long creatorId = dto.creatorId;
        if (creatorId != 0) edge.setCreator(nodeDao.getProxy(creatorId));
        else edge.setCreator(null);
        Long fromNodeId = dto.fromNodeId;
        edge.setFromNode(nodeDao.getProxy(fromNodeId));
        Long toNodeId = dto.toNodeId;
        edge.setToNode(nodeDao.getProxy(toNodeId));
        edge.setType(dto.type);
        edge.setWeight(dto.weight);
        edge.setDirected(dto.directed);
        //logger.debug("there are " + dto.attributes.keySet().size() + " attributes.");
        edge.getAttributes().clear();
        edge.getAttributes().putAll(dto.attributes);
        edge.getLongAttributes().clear();
        edge.getLongAttributes().putAll(dto.longAttributes);              
        
        return edge;
    }
    
    public Response createOrUpdateNodeTagging(Request req) throws Exception{
    	logger.info("createOrUpdateNodeTagging...");
    	Response res = new Response();
    	
    	// create node tag if necessary
    	NodeDTO node = null;
    	if (req.node != null){
    		Beans.init();
    		NodeRO nodeRO = (NodeRO)Beans.getBean("nodeRO");
    		node = nodeRO.saveNode(req.node);    		
    		req.edge.toNodeId = node.nodeId;
    	}
    	
    	res.node = node;
        res.edge = saveEdge(req.edge);
        
        logger.info("createOrUpdateNodeTagging done.");    	
    	return res;
    }   
      
    
    ////////////// delete edges ////////////////////////////////
    public Long deleteEdgeById(Long id){
    	logger.info("deleting edge id=" + id);
        Edge edge = edgeDao.findById(id);
        if (edge != null) edgeDao.delete(edge);
        return id;
    }
    
    @SuppressWarnings("unchecked")
	public List<Long> deleteEdgeByIds(List ids){
        List<Long> edgeIds = new ArrayList<Long>();
        for (Object id : ids){
            edgeIds.add(deleteEdgeById(Long.parseLong(id.toString())));
        }
        return edgeIds;
    }
    */
	
    public List<Long> deleteEdgesByType(String edgeType){
    	logger.info("deleting edges by type: " + edgeType);
    	List<Long> edgeIds = new ArrayList<Long>();
    	List<Edge> edges = edgeDao.findByType(edgeType, true);
    	edgeDao.delete(edges);
    	for (Edge edge : edges){
    		edgeIds.add(edge.getId());
    	}
    	logger.info(edges.size() + " edges are deleted.");
    	return edgeIds;
    }
    
    /*
    public List<Long> deleteNodeTaggingsByNodeAndTag(Long nodeId, Long tagId){
    	logger.info("deleteNodeTaggingsByNodeAndTag...");
    	List<Edge> edges = edgeDao.findByFromToNodeId(nodeId, tagId);
    	List<Edge> taggings = new ArrayList<Edge>();
    	List<Long> taggingIds = new ArrayList<Long>();
    	for (Edge edge : edges){
    		if (edge.getType().startsWith(Constants.TAGGING_PREFIX)) {
    			taggings.add(edge);
    			taggingIds.add(edge.getId());
    		}
    	}
    	
    	edgeDao.delete(taggings);
    	logger.info("deleteNodeTaggingsByNodeAndTag done");
    	return taggingIds;
    }
    
    
    ////////////////////////// query edges //////////////////////////
    public EdgeDTO getEdgeById(Long id){
        Edge edge = edgeDao.loadById(id);
        if (edge == null) return null;
        else return new EdgeDTO(edge);
    }
	*/
    
    // for relational questions
    public List<EdgeDTO> getOutgoingEdges(Long nodeId, boolean includePerceived){
    	logger.debug("getOutgoingEdges (nodeId=" + nodeId + ", includePerceived=" + includePerceived + ")...");
        List<Edge> edges = edgeDao.loadByFromNodeId(nodeId); // may include perceived edges
        List<EdgeDTO> outgoingEdges = new ArrayList<EdgeDTO>();
        for (Edge edge : edges){
            if (includePerceived || edge.getCreator() == null) outgoingEdges.add(new EdgeDTO(edge));
        }
        logger.debug("getOutgoingEdges(size=" + outgoingEdges.size() + ")...done");
        return outgoingEdges;
    }
    
    public List<EdgeDTO> getIncomingEdges(Long nodeId, boolean includePerceived){
        logger.debug("getIncomingEdges (nodeId=" + nodeId + ", includePerceived=" + includePerceived + ")...");
        List<Edge> edges = edgeDao.loadByToNodeId(nodeId); // may include perceived edges
        List<EdgeDTO> incomingEdges = new ArrayList<EdgeDTO>();
        for (Edge edge : edges){
            if (includePerceived || edge.getCreator() == null) incomingEdges.add(new EdgeDTO(edge));
        }
		logger.debug("getIncomingEdges(size=" + incomingEdges.size() + ")...done");
        return incomingEdges;
    }

    /*
    // for perceived relational questions
    public List<EdgeDTO> getCreatedEdges(Long creatorId, boolean includeTagging){
    	logger.debug("getCreatedEdges (creatorId=" + creatorId + ", includeTagging=" + includeTagging + ")...");
        List<Edge> edges = edgeDao.loadByCreatorId(creatorId);
        List<EdgeDTO> createdEdges = new ArrayList<EdgeDTO>();
        for (Edge edge : edges){
        	if (includeTagging || !edge.getType().startsWith(Constants.TAGGING_PREFIX)) createdEdges.add(new EdgeDTO(edge));
        }
        logger.debug("getCreatedEdges(size=" + createdEdges.size() + ")...done");
        return createdEdges;
    }
    
    // for tagging questions
    public List<EdgeDTO> getTaggingsByCreator(Long creatorId){
    	logger.debug("getNodeTaggings (creatorId=" + creatorId + ")...");
        List<Edge> edges = edgeDao.loadByCreatorId(creatorId);
        List<EdgeDTO> nodeTaggings = new ArrayList<EdgeDTO>();
        for (Edge edge : edges){
        	if (edge.getType().startsWith(Constants.TAGGING_PREFIX)) nodeTaggings.add(new EdgeDTO(edge));
        }
        logger.debug("getNodeTaggings(size=" + nodeTaggings.size() + ")...done");
        return nodeTaggings;
    }
    

    public List<EdgeDTO> getTaggingsByNode(Long nodeId){
        logger.debug("getTaggingsByNode (nodeId=" + nodeId + ")...");
        List<Edge> edges = edgeDao.loadByFromNodeId(nodeId);
        List<EdgeDTO> nodeTaggings = new ArrayList<EdgeDTO>();
        for (Edge edge : edges){
        	if (edge.getType().startsWith(Constants.TAGGING_PREFIX)) nodeTaggings.add(new EdgeDTO(edge));
        }
        logger.debug("getTaggingsByNode(size=" + nodeTaggings.size() + ")... done");
        return nodeTaggings;
    }
    
    public List<EdgeDTO> getTaggingsByTag(Long tagId){
        logger.debug("getTaggingsByTag (tagId=" + tagId + ")...");
        List<Edge> edges = edgeDao.loadByToNodeId(tagId);
        List<EdgeDTO> nodeTaggings = new ArrayList<EdgeDTO>();
        for (Edge edge : edges){
        	if (edge.getType().startsWith(Constants.TAGGING_PREFIX)) nodeTaggings.add(new EdgeDTO(edge));
        }
        logger.debug("getTaggingsByTag(size=" + nodeTaggings.size() + ")... done");
        return nodeTaggings;
    }
    */
    
    
    /////////////// edge types related ////////////////////////////
    public List<String> getEdgeTypes(){
    	logger.debug("getEdgeTypes...");
    	List<String> types = edgeDao.getEdgeTypes();
    	logger.debug("getEdgeTypes...done");
    	return types;
    }
    
    public List<Map<String, String>> getEdgeTypesByNodeTypes(Map input){
    	logger.info("getEdgeTypesByNodeTypes...");
    	List<String> nodeTypes = (List<String>) input.get("nodeTypes");
    	String direction = (String)input.get("direction");
    	Long loginNodeId = Long.parseLong((String)input.get("loginNodeId"));
    	Node loginNode = nodeDao.loadById(loginNodeId);
    	
    	List<String> edgeTypes = edgeDao.getEdgeTypesByNodeTypes(nodeTypes, Integer.parseInt(direction));
    	
    	if (!loginNode.isAdmin()) edgeTypes = removeHiddenEdgeTypes(edgeTypes);
    	
    	List<Map<String, String>> fullEds = getEdgeTypeDescriptions();
    	List<Map<String, String>> eds = new LinkedList<Map<String, String>>();
    	for (String edgeType : edgeTypes){
    		Map<String, String> ed = GeneralUtil.getEdgeDescription(fullEds, edgeType);
    		eds.add(ed);
    		logger.debug("edgeType: " + edgeType + ", edgeLabel: " + ed.get("label"));
    	}
    	logger.info("done.");
    	return eds;
    }
    
    public List<Map<String, String>> getEdgeTypesAmongNodeTypes(Map input){
    	logger.info("getEdgeTypesAmongNodeTypes...");
    	List<String> nodeTypes = (List<String>) input.get("nodeTypes");
    	Long loginNodeId = Long.parseLong((String)input.get("loginNodeId"));
    	Node loginNode = nodeDao.loadById(loginNodeId);
    	
    	List<String> edgeTypes = edgeDao.getEdgeTypesAmongNodeTypes(nodeTypes);
    	
    	if (!loginNode.isAdmin()) edgeTypes = removeHiddenEdgeTypes(edgeTypes);
    	
    	List<Map<String, String>> fullEds = getEdgeTypeDescriptions();
    	logger.info(fullEds);
    	List<Map<String, String>> eds = new LinkedList<Map<String, String>>();
    	for (String edgeType : edgeTypes){
    		logger.debug("edgeType: " + edgeType);
    		Map<String, String> ed = GeneralUtil.getEdgeDescription(fullEds, edgeType);
    		eds.add(ed);    		
    		logger.debug("edgeLabel: " + ed.get("label"));
    	}
    	logger.info("done.");
    	return eds;
    }
    
    private List<String> removeHiddenEdgeTypes(List<String> edgeTypes){
    	logger.info("remove edge types belong to hidden questions...");
    	List<Question> questions = questionDao.getAll();
    	
    	for (Question question : questions){
    		if (question.isHidden()){
    			String qtype = question.getType();
    			String edgeType;
    			if (qtype.equals(Constants.RELATIONAL_CHOICE_MULTIPLE)
    				|| qtype.equals(Constants.RELATIONAL_RATING_MULTIPLE)){
    				for (Field field : question.getFields()){
    					edgeType = question.getEdgeTypeWithField(field);
    					edgeTypes.remove(edgeType);
    					logger.debug("removed: " + edgeType);
    				}
    			} else {
    				edgeType = question.getEdgeType();
    				if (edgeType != null) {
    					edgeTypes.remove(edgeType);
    					logger.debug("removed: " + edgeType);
    				}
    			}
    		}
    	}
    	
    	return edgeTypes;
    }
    
    public List<Map<String, String>> getEdgeTypesByFromAndToNodeTypes(Map input){
    	String fromNodeType = (String)input.get("fromNodeType");
    	String toNodeType = (String)input.get("toNodeType");
    	return edgeDao.getEdgeTypesByFromAndToNodeTypes(fromNodeType, toNodeType);
    }
    
    @SuppressWarnings("unchecked")
	public List<Map<String, String>> getEdgeTypeDescriptions(){
    	logger.debug("getEdgeTypeDescriptions...");
    	
    	List<Map<String, String>> eds = GeneralUtil.getEdgeDescriptions();
    	List<Map<String, String>> fullEds = new LinkedList<Map<String, String>>();
    	List<String> edgeTypes = edgeDao.getEdgeTypes();
    	
    	for (String edgeType : edgeTypes){
    		Map<String, String> ed = GeneralUtil.getEdgeDescription(eds, edgeType);
    		if (ed == null){
    			//logger.debug("set default edge description for edgeType: " + edgeType);
	    		ed = new HashMap<String, String>();
	    		ed.put("type", edgeType);
	    		ed.put("label", edgeType);
	    		ed.put("verb", edgeType);
    		}
    		
    		fullEds.add(ed);
    	}
    	
		logger.debug("getEdgeTypeDescriptions...done");
    	return fullEds;
    }
    
    public void saveEdgeTypeDescriptions(List<Map<String, String>> eds){
    	logger.info("saveEdgeDescriptions...");
    	GeneralUtil.saveEdgeDescriptions(eds);
		logger.info("saveEdgeDescriptions... done");
    }
    
    
    
    
    ////////////////////// edge derivation //////////////////////////////
    @SuppressWarnings("unchecked")
    /**
     * Assumption: 
     * 1, There is only one fromNodeType and one toNodeType for each edgeType (relation)
     * 2, When doing element-wise operations, the fromNodeType and toNodeType of the resulted/derived
     * 		relation is the same as matrixA (with consideration of the direction/transposition)
     */
	public int deriveEdgesByRelation(Map data) throws Exception{
    	String msg = "";
    	String edgeTypeA = (String)data.get("edgeTypeA");
    	int directionA = Integer.parseInt((String)data.get("directionA"));
    	List<String> nodeFilterConditions_a_from = (List<String>)data.get("nodeFilterConditions_a_from");
    	String nodeCombiner_a_from = (String)data.get("nodeCombiner_a_from");
    	List<String> nodeFilterConditions_a_to = (List<String>)data.get("nodeFilterConditions_a_to");
    	String nodeCombiner_a_to = (String)data.get("nodeCombiner_a_to");
    	List<String> edgeFilterConditions_a = (List<String>)data.get("edgeFilterConditions_a");
    	String edgeCombiner_a = (String)data.get("edgeCombiner_a");
    	
    	String operator = (String)data.get("operator");
    	String keepDiagonal = (String) data.get("keepDiagonal");
    	
    	String edgeTypeB = (String)data.get("edgeTypeB");    	
    	int directionB = Integer.parseInt((String)data.get("directionB"));
    	List<String> nodeFilterConditions_b_from = (List<String>)data.get("nodeFilterConditions_b_from");
    	String nodeCombiner_b_from = (String)data.get("nodeCombiner_b_from");
    	List<String> nodeFilterConditions_b_to = (List<String>)data.get("nodeFilterConditions_b_to");
    	String nodeCombiner_b_to = (String)data.get("nodeCombiner_b_to");
    	List<String> edgeFilterConditions_b = (List<String>)data.get("edgeFilterConditions_b");
    	String edgeCombiner_b = (String)data.get("edgeCombiner_b");
    	
    	Long creator = Long.parseLong((String)data.get("creatorId"));
    	JobDTO job = (JobDTO)data.get("job");
    	
    	logger.info("deriving edges by relation ...");
    	logger.debug("edgeTypeA: " + edgeTypeA);
    	logger.debug("directionA: " + directionA);
    	logger.debug("nodeFilterConditions_a_from: " + nodeFilterConditions_a_from);
    	logger.debug("nodeCombiner_a_from: " + nodeCombiner_a_from);
    	logger.debug("nodeFilterConditions_a_to: " + nodeFilterConditions_a_to);
    	logger.debug("nodeCombiner_a_to: " + nodeCombiner_a_to);
    	logger.debug("edgeFilterConditions_a: " + edgeFilterConditions_a);
    	logger.debug("edgeCombiner_a: " + edgeCombiner_a);
    	logger.debug("operator: " + operator);
    	logger.debug("edgeTypeB: " + edgeTypeB);
    	logger.debug("directionB: " + directionB);
    	logger.debug("nodeFilterConditions_b_from: " + nodeFilterConditions_b_from);
    	logger.debug("nodeCombiner_b_from: " + nodeCombiner_b_from);
    	logger.debug("nodeFilterConditions_b_to: " + nodeFilterConditions_b_to);
    	logger.debug("nodeCombiner_b_to: " + nodeCombiner_b_to);
    	logger.debug("edgeFilterConditions_b: " + edgeFilterConditions_b);
    	logger.debug("edgeCombiner_b: " + edgeCombiner_b);
    	logger.debug("creatorId: " + creator);
    	
    	logger.info("loading questions...");
    	Map<String, Question> qmap = Question.getShortNameToQuestionMap();
    	Map<Long, Question> idToQuestionMap = Question.getIdToQuestionMap(qmap.values());

    	logger.info("loading edges...");
		List<Edge> edgesA = edgeDao.loadByType(edgeTypeA, false);
		List<Edge> edgesB = edgeDao.loadByType(edgeTypeB, false);
    	
		logger.info("loading nodes...");
		Map<Long, Node> fullNodeMap = new HashMap<Long, Node>();
		Map<String, List<Node>> typeToNodesMap = new HashMap<String, List<Node>>();
		// fromNodeType for edgesA
		String nodeType = edgesA.get(0).getFromNode().getType();
		List<Node> nodeList = nodeDao.loadByType(nodeType);
		typeToNodesMap.put(nodeType, nodeList);
		for (Node node : nodeList){
			fullNodeMap.put(node.getId(), node);
		}
		// toNodeType for edgesA
		nodeType = edgesA.get(0).getToNode().getType();
		if (!typeToNodesMap.containsKey(nodeType)){
			nodeList = nodeDao.loadByType(nodeType);
			typeToNodesMap.put(nodeType, nodeList);
			for (Node node : nodeList){
				fullNodeMap.put(node.getId(), node);
			}
		}
		// fromNodeType for edgesB
		nodeType = edgesB.get(0).getFromNode().getType();
		if (!typeToNodesMap.containsKey(nodeType)){
			nodeList = nodeDao.loadByType(nodeType);
			typeToNodesMap.put(nodeType, nodeList);
			for (Node node : nodeList){
				fullNodeMap.put(node.getId(), node);
			}
		}
		// toNodeType for edgesB
		nodeType = edgesB.get(0).getToNode().getType();
		if (!typeToNodesMap.containsKey(nodeType)){
			nodeList = nodeDao.loadByType(nodeType);
			typeToNodesMap.put(nodeType, nodeList);
			for (Node node : nodeList){
				fullNodeMap.put(node.getId(), node);
			}
		}
		
    	logger.info("filtering by node/edge filters...");
		Iterator<Edge> edgeIterator = edgesA.iterator();
		logger.debug("before filtering edgesA: " + edgesA.size());
		while(edgeIterator.hasNext()){
			Edge edge = edgeIterator.next();
			if (!NetworkExtractor.passEdgeFilter(edge, edgeFilterConditions_a, edgeCombiner_a, idToQuestionMap)){
				edgeIterator.remove();
				continue;
			}
			
			Long fid = edge.getFromNode().getId();
			Node fromNode = fullNodeMap.get(fid);
			if (!NetworkExtractor.passNodeFilter(fromNode, nodeFilterConditions_a_from, nodeCombiner_a_from, idToQuestionMap)){
				edgeIterator.remove();
				continue;
			}
			
			Long tid = edge.getToNode().getId();
			Node toNode = fullNodeMap.get(tid);
			if (!NetworkExtractor.passNodeFilter(toNode, nodeFilterConditions_a_to, nodeCombiner_a_to, idToQuestionMap)){
				edgeIterator.remove();
				continue;
			}
		}
		logger.debug("after filtering edgesA: " + edgesA.size());
		if (edgesA.size() == 0) {
			msg = "edgesA is empty.";
			logger.error(msg);
			throw new Exception(msg);
		}
		
		edgeIterator = edgesB.iterator();
		logger.debug("before filtering edgesB: " + edgesB.size());
		while(edgeIterator.hasNext()){
			Edge edge = edgeIterator.next();
			if (!NetworkExtractor.passEdgeFilter(edge, edgeFilterConditions_b, edgeCombiner_b, idToQuestionMap)){
				edgeIterator.remove();
				continue;
			}
			
			Long fid = edge.getFromNode().getId();
			Node fromNode = fullNodeMap.get(fid);
			if (!NetworkExtractor.passNodeFilter(fromNode, nodeFilterConditions_b_from, nodeCombiner_b_from, idToQuestionMap)){
				edgeIterator.remove();
				continue;
			}
			
			Long tid = edge.getToNode().getId();
			Node toNode = fullNodeMap.get(tid);
			if (!NetworkExtractor.passNodeFilter(toNode, nodeFilterConditions_b_to, nodeCombiner_b_to, idToQuestionMap)){
				edgeIterator.remove();
				continue;
			}
		}
		logger.debug("after filtering edgesB: " + edgesB.size());
		if (edgesB.size() == 0) {
			msg = "edgesB is empty.";
			logger.error(msg);
			throw new Exception(msg);
		}

		
		
		List<Node> fromNodeList_a = typeToNodesMap.get(edgesA.get(0).getFromNode().getType());
		List<Node> toNodeList_a = typeToNodesMap.get(edgesA.get(0).getToNode().getType());
		List<Node> fromNodeList_b = typeToNodesMap.get(edgesB.get(0).getFromNode().getType());
		List<Node> toNodeList_b = typeToNodesMap.get(edgesB.get(0).getToNode().getType());
		int rows_a = 0;
		int cols_a = 0;
		int rows_b = 0;
		int cols_b = 0;
		
		if (directionA > 0) {
			rows_a = fromNodeList_a.size();
			cols_a = toNodeList_a.size();
		} else {
			rows_a = toNodeList_a.size();
			cols_a = fromNodeList_a.size();
		}
		if (directionB > 0) {
			rows_b = fromNodeList_b.size();
			cols_b = toNodeList_b.size();
		} else {
			rows_b = toNodeList_b.size();
			cols_b = fromNodeList_b.size();
		}
		
    	// derivation
    	logger.debug("perform operation: " + operator);
    	DoubleMatrix2D derived = null;
    	List<Node> fromNodeList, toNodeList;
    	if(operator.equals("multiplication")){
    		if (cols_a != rows_b) {
    			msg = "matrixA: " + rows_a + "x" + cols_a + " cannot multiply with matrixB: " + rows_b + "x" + cols_b;
    			logger.error(msg);
    			throw new Exception(msg);
    		}
    		
    		derived = new SparseDoubleMatrix2D(rows_a, cols_b);
        	DoubleMatrix2D matrixA = createEdgeMatrix(qmap, edgesA, directionA, fromNodeList_a, toNodeList_a);    	
        	//logger.debug("matrixA: " + matrixA);
        	DoubleMatrix2D matrixB = createEdgeMatrix(qmap, edgesB, directionB, fromNodeList_b, toNodeList_b);    	
        	//logger.debug("matrixB: " + matrixB);
        	
	    	matrixA.zMult(matrixB, derived);
	    	if (keepDiagonal != null && keepDiagonal.equals("0")){
		    	for (int i=0; i<derived.rows(); i++){
		    		for (int j=0; j<derived.columns(); j++){
		    			if (i==j) derived.setQuick(i, j, 0);
		    		}
		    	}
	    	}
	    	
	    	fromNodeList = directionA > 0 ? fromNodeList_a:toNodeList_a;
	    	toNodeList = directionB > 0 ? toNodeList_b:fromNodeList_b;
    	} else{ // element-wise    		
    		if (rows_a != rows_b || cols_a != cols_b) {
    			msg = "matrixA: " + rows_a + "x" + cols_a + 
				" cannot perform element-wise " + operator + " with matrixB: " + rows_b + "x" + cols_b;
    			logger.error(msg);
    			throw new Exception(msg);
    		}
    			
    		derived = new SparseDoubleMatrix2D(rows_a, cols_a);
        	DoubleMatrix2D matrixA = createEdgeMatrix(qmap, edgesA, directionA, fromNodeList_a, toNodeList_a);    	
        	//logger.debug("matrixA: " + matrixA);
        	DoubleMatrix2D matrixB = createEdgeMatrix(qmap, edgesB, directionB, fromNodeList_b, toNodeList_b);    	
        	//logger.debug("matrixB: " + matrixB);
        	
    		for (int i=0; i<derived.rows(); i++){
    			for (int j=0; j<derived.columns(); j++){
    				double value = 0;
    				if (operator.equals("multiplication-e")) value = matrixA.getQuick(i, j) * matrixB.getQuick(i, j);
    				else if (operator.equals("addition")) value = matrixA.getQuick(i, j) + matrixB.getQuick(i, j);
    				else if (operator.equals("subtraction")) value = matrixA.getQuick(i, j) - matrixB.getQuick(i, j);
    				else if (operator.equals("division")) {
    					double denominator = matrixB.getQuick(i, j);
    					if (denominator != 0) value = matrixA.getQuick(i, j) / denominator;
    				}
    				else {
    					msg = "unrecognized operator: " + operator;
    					logger.error(msg);
    					throw new Exception(msg);
    				}
    				derived.setQuick(i, j, value);
    			}
    		} 
	    	fromNodeList = directionA > 0 ? fromNodeList_a:toNodeList_a;
	    	toNodeList = directionA > 0 ? toNodeList_a:fromNodeList_a;
    	}	
    	//logger.debug(derived);
    	logger.debug(operator + "... done");
    	
    	
    	String edgeType = "d." + (directionA > 0?"P":"N") + edgeTypeA + "." + (directionB > 0?"P":"N") + edgeTypeB + "." + operator;
    	createDerivedQuestion(edgeType);
    	int count = createDerivedEdges(fromNodeList, toNodeList, derived, edgeType);    	
    	
    	// schedule job?
    	if (job != null){
    		checkDuplicateJobName(job.name);
    		
    		logger.info("Scheduling a new job: " + job.name);
    		Job j = job.toJob();
    		
    		j.setClassName("ciknow.ro.EdgeRO");
    		j.setMethodName("deriveEdgesByRelation");
    		j.setBeanName("edgeRO");
    		Class[] pTypes = new Class[]{Map.class};
    		byte[] byteArray = GeneralUtil.objectToByteArray(pTypes);
    		logger.debug("job data type length (byte): " + byteArray.length);
    		j.setParameterTypes(byteArray);		
    		
    		data.remove("job");    		
    		Object[] pValues = new Object[]{data};
    		byteArray = GeneralUtil.objectToByteArray(pValues);
    		logger.debug("job data value length (byte): " + byteArray.length);
    		j.setParameterValues(byteArray);
    		jobDao.save(j);
    	}
    	
    	return count;
    }

    //public int deriveEdgesByAttribute(List<String> nodeTypes, String shortName, String fieldName) throws Exception{
    public int deriveEdgesByAttribute(Map data) throws Exception{
    	List<String> nodeTypes = (List<String>)data.get("nodeTypes");
    	String shortName = (String)data.get("shortName");
    	String fieldName = (String)data.get("fieldName");
    	JobDTO job = (JobDTO)data.get("job");
    	
    	logger.info("deriving edges by attribute (question shortName=" + shortName + ", fieldName=" + fieldName + ")");

    	List<Node> nodes = new ArrayList<Node>();
    	for (String type : nodeTypes){
    		List<Node> list = nodeDao.loadByType(type);
    		logger.info(list.size() + " nodes of type: " + list.size());
    		nodes.addAll(list);
    	}
    	int count = nodes.size();
    	DoubleMatrix2D matrix = new SparseDoubleMatrix2D(count, count);
    	
    	Question q = questionDao.findByShortName(shortName);
    	if (fieldName.equals("")){
	    	Set<String> attSet = new HashSet<String>();
	    	for (Field f : q.getFields()){
	    		String k = q.makeFieldKey(f);
	    		attSet.add(k);
	    		logger.debug("key: " + k);
	    	}
	    	for (int i=0; i<count; i++){
				Node n1 = nodes.get(i);
				Set<String> attSet1 = new HashSet<String>(n1.getAttributes().keySet());
				attSet1.retainAll(attSet);
	    		if (attSet1.isEmpty()) continue;
	    		
				for (int j=i+1; j < count; j++){    			
	    			Node n2 = nodes.get(j);
	    			Set<String> attSet2 = new HashSet<String>(n2.getAttributes().keySet());
	    			attSet2.retainAll(attSet);
	    			if (attSet2.isEmpty()) continue;
	    			
	    			// get all existing attributes
	    			Set<String> unionSet = new HashSet<String>();
	    			unionSet.addAll(attSet1);
	    			unionSet.addAll(attSet2);   
	    			if (unionSet.isEmpty()) continue;
	    			
	    			// get common attributes
	    			Set<String> intersectionSet = new HashSet<String>();
	    			intersectionSet.addAll(attSet1);
	    			intersectionSet.retainAll(attSet2);
	    			if (intersectionSet.isEmpty()) continue;
	    			
	    			float w = intersectionSet.size()/new Float(unionSet.size()).floatValue();
	    			matrix.setQuick(i, j, w);
	    			matrix.setQuick(j, i, w);
	    		}
	    	}
	    	//logger.debug(matrix);
    	} else {
    		Field field = q.getFieldByName(fieldName);
    		String key = q.makeFieldKey(field);
	    	for (int i=0; i<count; i++){
				Node n1 = nodes.get(i);
	    		if (n1.getAttribute(key) == null) continue;
	    		
				for (int j=i+1; j < count; j++){    			
	    			Node n2 = nodes.get(j);
	    			if (n2.getAttribute(key) == null) continue;
	    			
	    			float w = 1f;
	    			matrix.setQuick(i, j, w);
	    			matrix.setQuick(j, i, w);
	    		}
	    	}
    	}
    	
    	StringBuilder sb = new StringBuilder();
    	sb.append("d.");    	
    	for (int i=0; i<nodeTypes.size(); i++){
    		if (i > 0) sb.append("+");
    		sb.append(nodeTypes.get(i));
    	}
    	sb.append(".").append(shortName);
    	if (!fieldName.equals("")) sb.append(".").append(fieldName);
    	String edgeType = sb.toString();
    	createDerivedQuestion(edgeType);
    	int num = createDerivedEdges(nodes, nodes, matrix, edgeType);    	
    	
    	// schedule job?
    	if (job != null){
    		checkDuplicateJobName(job.name);
    		
    		logger.info("Scheduling a new job: " + job.name);
    		Job j = job.toJob();
    		
    		j.setClassName("ciknow.ro.EdgeRO");
    		j.setMethodName("deriveEdgesByAttribute");
    		j.setBeanName("edgeRO");
    		Class[] pTypes = new Class[]{Map.class};
    		j.setParameterTypes(GeneralUtil.objectToByteArray(pTypes));		
    		
    		data.remove("job");    		
    		Object[] pValues = new Object[]{data};
    		j.setParameterValues(GeneralUtil.objectToByteArray(pValues));
    		jobDao.save(j);
    	}
    	
    	return num;
    }

	private DoubleMatrix2D createEdgeMatrix(Map<String, Question> qmap,
											List<Edge> edges, int direction,
											List<Node> listA, 
											List<Node> listB) throws Exception {
		logger.info("creating edge matrix...");
		int rows = listA.size();
		int cols = listB.size();
		
		// init
		DoubleMatrix2D matrix = new SparseDoubleMatrix2D(rows, cols);
		
        // set weight
    	for (Edge edge:edges){
    		Node from = edge.getFromNode();    		
    		Node to = edge.getToNode();
    		
    		int row = listA.indexOf(from);
    		if (row < 0) {
    			throw new Exception("edge(id=" + edge.getId() + ") is ignored since " + "fromNode is not in the fromNode list");
    		}
    		int col = listB.indexOf(to);
    		if (col < 0) {
    			throw new Exception("edge(id=" + edge.getId() + ") is ignored since " + "toNode is not in the toNode list");
    		}
    		Question q = qmap.get(EdgeUtil.getShortNameFromEdgeType(edge.getType()));
    		double edgeWeight;
    		if (q == null) edgeWeight = edge.getWeight();
    		else edgeWeight = edge.getWeight();
    		double w = matrix.getQuick(row, col) + edgeWeight;
    		matrix.setQuick(row, col, w);    		
    	}
    	
    	logger.info("creating edge matrix...done");
    	
    	// transpose?
    	if (direction < 0) return matrix.viewDice();
    	else return matrix;
	}
	

	private Question createDerivedQuestion(String edgeType) throws Exception {
		Question q = questionDao.findByShortName(edgeType);
		if (q != null) {
			QuestionRO questionRO = (QuestionRO)Beans.getBean("questionRO");
			questionRO.deleteQuestion(q.getId());
		}
		
		logger.info("creating derived question...");
		Survey survey = surveyDao.findById(1L);
		
		Page page = new Page();
        page.setName("page: " + edgeType);
        page.setLabel(page.getName());
        page.setInstruction("this is page " + edgeType);
        survey.getPages().add(page);
        page.setSurvey(survey);

    	Question question = new Question();		
		question.setPage(page);
		page.getQuestions().add(question);
		question.setType(Constants.RELATIONAL_CONTINUOUS);
		question.setShortName(edgeType);
		question.setLabel("derived edges: " + edgeType);				
		question.setRowPerPage(20);		
		question.setHtmlInstruction("This is a auto generated question when creating derived relationship.");
		
		// attach default visible and available groups
		Set<Group> visibleGroups = new HashSet<Group>();		
		visibleGroups.add(groupDao.findByName(Constants.GROUP_USER));
		question.setVisibleGroups(visibleGroups);
		Set<Group> availableGroups = new HashSet<Group>();
		availableGroups.add(groupDao.findByName(Constants.GROUP_ALL));
		question.setAvailableGroups(availableGroups);

		questionDao.save(question);
		logger.info("created derived question(" + question.getShortName() + ")... done");
		return question;
	}

	private int createDerivedEdges(List<Node> fromNodeList,
			List<Node> toNodeList, DoubleMatrix2D derived, String edgeType) {
		logger.info("removing old edges...");
		List<Edge> delete_edges = edgeDao.findByType(edgeType, true);
		edgeDao.delete(delete_edges);
		
		logger.info("creating edges ...");		
		List<Edge> edges = new ArrayList<Edge>();
		int total = 0;		
		for (int row=0; row < derived.rows(); row++){
			for (int col=0; col < derived.columns(); col++){
				double v = derived.getQuick(row, col);
				if (Double.isInfinite(v) || Double.isNaN(v) || v <= 0) continue;

				Edge edge = new Edge();
				edge.setCreator(null);
				edge.setFromNode(fromNodeList.get(row));
				edge.setToNode(toNodeList.get(col));
				edge.setDirected(true);
				edge.setWeight(v);
				edge.setType(edgeType);
				edges.add(edge);
				
				total++;					
				if (total%Constants.HIBERNATE_BATCH_SIZE == 0){
					edgeDao.save(edges);
					edges = new ArrayList<Edge>();
					logger.debug(total + " edges saved.");
				}					
			}
		}
		edgeDao.save(edges);
		logger.info(total + " edges created.");
		return total;
	}
    

    
    //public int deriveEdgesByCC(String shortName){
	public int deriveEdgesByCC(Map data) throws Exception{
		String shortName = (String)data.get("shortName");
		JobDTO job = (JobDTO)data.get("job");
		
    	logger.info("derive edges by contact chooser question: " + shortName);
    	logger.debug("remove existing edges (type=" + shortName + ")");
    	List<Edge> delete_edges = edgeDao.findByType(shortName, true);
    	edgeDao.delete(delete_edges);
    	
    	int count = 0;
    	List<Group> groups = groupDao.getAll();
    	for (Group g : groups){
    		String groupName = g.getName().trim();
    		if (g.isPrivate() && groupName.endsWith("_" + shortName)){
    			logger.debug("group: " + groupName);
    			String username = groupName.substring(3, groupName.lastIndexOf("_" + shortName));
    			Node node = nodeDao.findByUsername(username);
    			if (node == null){
    				logger.warn("cannot not find user with username=" + username);
    				continue;
    			}
    			    			    			
    			List<Long> memberIds = groupDao.getNodeIdsByGroupId(g.getId());
    			List<Edge> edges = new LinkedList<Edge>();
    			for (Long mid : memberIds){
					Edge edge = new Edge();
					edge.setCreator(null);
					edge.setFromNode(node);
					edge.setToNode(nodeDao.getProxy(mid));
					edge.setDirected(true);
					edge.setWeight(1.0);
					edge.setType(shortName);
					edges.add(edge);
    			}
    			edgeDao.save(edges);
    			count += edges.size();
    		}
    	}
    	logger.info("edges derived from contact chooser.");
    	
    	// schedule job?
    	if (job != null){
    		checkDuplicateJobName(job.name);
    		logger.info("Scheduling a new job: " + job.name);
    		Job j = job.toJob();
    		
    		j.setClassName("ciknow.ro.EdgeRO");
    		j.setMethodName("deriveEdgesByCC");
    		j.setBeanName("edgeRO");
    		Class[] pTypes = new Class[]{Map.class};
    		j.setParameterTypes(GeneralUtil.objectToByteArray(pTypes));		
    		
    		data.remove("job");    		
    		Object[] pValues = new Object[]{data};
    		j.setParameterValues(GeneralUtil.objectToByteArray(pValues));
    		jobDao.save(j);
    	}
    	
    	return count;
    }    
    
    /**
     * @param shortName
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
	public Map deriveEdgesBySymmetrization(Map data) throws Exception{  
    	String edgeType = (String)data.get("edgeType");
    	String strategy = (String)data.get("strategy");  
    	JobDTO job = (JobDTO)data.get("job");
    	logger.info("deriving edges by symmetrization (edgeType=" + edgeType + ", strategy=" + strategy + ")");
    	
    	/* for strategy "Average", a floating number weight will break the semantic on relational choice/rating
    	// extract shortName
    	String shortName = edgeType;
    	if (edgeType.startsWith(Constants.TAGGING_PREFIX)) shortName = edgeType.substring(Constants.TAGGING_PREFIX.length());
    	else if (edgeType.contains(Constants.SEPERATOR)) shortName = edgeType.substring(0, edgeType.indexOf(Constants.SEPERATOR));    	
    	String newShortName = "s." + shortName;    	
    	
    	
    	logger.debug("copy relational question...");
    	Question question = questionDao.findByShortName(shortName);
    	if (question != null){
    		Question oldSymmetrizedQuestion = questionDao.findByShortName(newShortName);
    		if (oldSymmetrizedQuestion != null) questionDao.delete(oldSymmetrizedQuestion);
    		
	    	Question q = new Question(question);
	    	q.setId(null);
	    	q.setVersion(null);
	    	q.setShortName(newShortName);
	    	q.setLabel("Symmetrized From: " + question.getLabel());
	    	q.setSequenceNumber(questionDao.getMaxSequenceNumber() + 1);        	
	    	questionDao.save(q);
    	}
    	*/
    	
    	logger.debug("symmetrize edges...");
    	List<Edge> oldEdges = edgeDao.loadByType(edgeType, false);
    	Map<Node, List<Edge>> edgeMap = new HashMap<Node, List<Edge>>();
    	for (Edge edge : oldEdges){
    		List<Edge> edgeList = edgeMap.get(edge.getCreator());
    		if (edgeList == null){
    			edgeList = new ArrayList<Edge>();
    			edgeMap.put(edge.getCreator(), edgeList);
    		}
    		edgeList.add(edge);
    	}
    	
    	String newEdgeType = "symmetrized." + edgeType + "." + strategy;
    	List<Edge> deleteEdges = edgeDao.findByType(newEdgeType, true);
    	edgeDao.delete(deleteEdges);    	
    	int count = 0;
    	for (Node creator : edgeMap.keySet()){
    		Collection<Edge> edges = symmetrizeEdges(edgeMap.get(creator), newEdgeType, creator, strategy);
    		edgeDao.save(edges);
    		count += edges.size();
    		logger.debug(count + " edges saved.");
    	}
    	
    	Map output = new HashMap();
    	output.put("edgeType", newEdgeType);
    	output.put("edgeCount", count);
    	
    	// schedule job?
    	if (job != null){
    		checkDuplicateJobName(job.name);
    		logger.info("Scheduling a new job: " + job.name);
    		Job j = job.toJob();
    		
    		j.setClassName("ciknow.ro.EdgeRO");
    		j.setMethodName("deriveEdgesBySymmetrization");
    		j.setBeanName("edgeRO");
    		Class[] pTypes = new Class[]{Map.class};
    		j.setParameterTypes(GeneralUtil.objectToByteArray(pTypes));		
    		
    		data.remove("job");    		
    		Object[] pValues = new Object[]{data};
    		j.setParameterValues(GeneralUtil.objectToByteArray(pValues));
    		jobDao.save(j);
    	}
    	
    	return output;    	
    }
    
    private void checkDuplicateJobName(String name) throws Exception{
    	Job job = jobDao.getByName(name);
    	if (job != null){
    		throw new Exception("Duplicated Task Name: " + name);
    	}
    }
    
    private Collection<Edge> symmetrizeEdges(Collection<Edge> edges, String edgeType, Node creator, String strategy){
    	logger.info("creator: " + (creator!=null?creator.getUsername():"null"));
    	List<Edge> newEdges = new LinkedList<Edge>();
    	if (edges == null || edges.isEmpty()) return newEdges;
    	
    	Set<Node> nodeSet = new HashSet<Node>();
    	for (Edge edge : edges){
    		nodeSet.add(edge.getFromNode());
    		nodeSet.add(edge.getToNode());
    	}
    	List<Node> nodes = new ArrayList<Node>(nodeSet);
    	
    	SparseDoubleMatrix2D mat = new SparseDoubleMatrix2D(nodes.size(), nodes.size());
    	for (Edge edge : edges){   		
    		Node f = edge.getFromNode();
    		int findex = nodes.indexOf(f);
    		Node t = edge.getToNode();
    		int tindex = nodes.indexOf(t);
    		
    		mat.setQuick(findex, tindex, edge.getWeight());
    		if (!edge.isDirected()) mat.setQuick(tindex, findex, edge.getWeight());
    	}
    	//logger.debug("input: \n" + mat);
    	
    	for (int i=0; i<nodes.size(); i++){
    		for (int j=i+1; j<nodes.size(); j++){
    			double v1 = mat.getQuick(i, j);
    			double v2 = mat.getQuick(j, i);
    			double v = Math.max(v1, v2); // default strategy: s_max
    			if (strategy.equals(Constants.S_MIN)) v = Math.min(v1, v2);
    			else if (strategy.equals(Constants.S_AVR)) v = (v1+v2)/2;
    			
    			mat.setQuick(i, j, v);
    			mat.setQuick(j, i, v);
    		}
    	}
    	//logger.debug("output: \n" + mat);
    	
    	for (int i=0; i<nodes.size(); i++){
    		for (int j=0; j<nodes.size(); j++){
    			double weight = mat.getQuick(i, j);
    			if (weight > Double.MIN_VALUE){
	    			Edge edge = new Edge();
	    			edge.setCreator(creator);
	    			edge.setFromNode(nodes.get(i));
	    			edge.setToNode(nodes.get(j));
	    			edge.setDirected(true);
	    			edge.setType(edgeType);
	    			edge.setWeight(weight);
	    			newEdges.add(edge);
    			}
    		}
    	}
    	
    	return newEdges;
    }
    
    public void updateEdgeWeights() throws Exception{
    	logger.info("update edge weights");
    	try {
    		EdgeUtil.updateEdgeWeights();
    	} catch (Exception e) {
    		logger.error("Failed to update edge weights!");
    		e.printStackTrace();
    		throw new Exception("Failed to update edge weights!");
    	}	
    }
    
    public static void main(String[] args) throws Exception{    	
		if (args.length != 5) {
			logger.error("args: edgeTypeA directionA edgeTypeB directionB creatorId");
			logger.info("e.g. ant runEdgeRO author-article 1 keyword-article -1 123");
			return;
		}
		Map data = new HashMap();
		data.put("edgeTypeA", args[0]);
		data.put("directionA", args[1]);
		data.put("edgeTypeB", args[2]);
		data.put("directionB", args[3]);
		data.put("creatorId", args[4]);
				
    	Beans.init();    	
    	EdgeRO er = (EdgeRO) Beans.getBean("edgeRO");
    	er.deriveEdgesByRelation(data);
    }
}
