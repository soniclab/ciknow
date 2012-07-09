package ciknow.vis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;

import ciknow.dao.EdgeDao;
import ciknow.dao.NodeDao;
import ciknow.domain.Edge;
import ciknow.domain.Node;
import ciknow.domain.Recommendation;
import ciknow.recommend.service.RecommenderService;
import ciknow.util.Beans;
import ciknow.util.GeneralUtil;

/**
 * Created by IntelliJ IDEA. User: jinling Date: Oct.27, 2009 Time: 11:32:17 AM
 * To change this template use File | Settings | File Templates.
 */

public class RecommenderNetwork2 {
	private static Log logger = LogFactory.getLog(RecommenderNetwork.class);
	private NodeDao nodeDao;
	public Node targetNode;
	public Node sourceNode;
	private Set<Node> hiddenNodes;
	private Set<Long> hiddenNodeIds;
	private Set<Long> hiddenEdges; // edges for indirect relations
	private Set<Long> hiddenEdges1,hiddenEdges2, hiddenNodeIds1; // real hidden edge
	public Long sourceNodeId;
	private Map<Long, Edge> edgesMap;
	private EdgeDao edgeDao;
	private List<Long> nodeIds;
	private Set<Long> directEdgeIds;
	private Set<Long> edgeIds;
	private String labelFormat;
	private List<String> idSeScores;
	private double maxRecSize = 0.00;
	private double minRecSize = 9999999.99;
	private Set<Long> recIdSet;
	private Set<Long> noRecRelationNode;
	private String questionId;

	public static void main(String[] args) {
		/*
		 * String sourceId = "4"; String[] recIds = new String[1]; recIds[0] =
		 * "103"; String[] seScores = new String[1]; seScores[0] = "2"; String[]
		 * idScores = new String[1]; idScores[0] = "1"; String[] finalScores =
		 * new String[1]; finalScores[0] = "0.5";
		 * 
		 * String targetLabel = "\"Physician\""; RecommenderNetwork2 rn = new
		 * RecommenderNetwork2(Long.parseLong(sourceId), recIds, seScores,
		 * idScores, finalScores, targetLabel, "1", "-1");
		 * 
		 * for(Node n: rn.getNodeList()){ System.out.println("node: " +
		 * n.getId()); }
		 * 
		 * for(Edge e: rn.getEdges()){ System.out.println("edge: " + e.getId());
		 * }
		 */

		// System.out.println("getNodeList(): " + rn.getNodeList());
		// System.out.println("getEdges(): " + rn.getEdges());
		// System.out.println("getIdSeScores(): " + rn.getIdSeScores());
	}

	public RecommenderNetwork2(Long sourceId, String[] recIds,
			String[] seScores, String[] idScores, String[] finalScores,
			String[] idMetricTypes, String[] seMetricTypes, String targetName,
			String targetLabel, String numRecs, String questionId) {
		if(numRecs == null)
			numRecs = "100";
		Set<String> recTypes = new HashSet<String>();
		String sourceType;
		logger.info("get recommender network ...");
		Beans.init();
		edgeDao = (EdgeDao) Beans.getBean("edgeDao");
		nodeDao = (NodeDao) Beans.getBean("nodeDao");
		this.questionId = questionId;
		sourceNodeId = sourceId;
		sourceNode = nodeDao.loadById(sourceId);
		sourceType = sourceNode.getType();

		if (questionId.equalsIgnoreCase("-1"))
			targetNode = nodeDao.findByLabel(targetLabel).get(0);

		List<String> keyWords = new ArrayList<String>();
		keyWords.add(targetName);

		nodeIds = new ArrayList<Long>();
		nodeIds.add(sourceNodeId);

		idSeScores = new ArrayList<String>();

		idSeScores.add("RecScores"); // artificial score for user

		edgesMap = new HashMap<Long, Edge>();
		edgeIds = new HashSet<Long>();

		for (Edge e : edgeDao.findByFromNodeId(sourceId)) {
			edgeIds.add(e.getId());
			edgesMap.put(e.getId(), e);

		}

		for (Edge e : edgeDao.findByToNodeId(sourceId)) {
			edgeIds.add(e.getId());
			edgesMap.put(e.getId(), e);
		}
		List<String> reclist = null;
		if (recIds != null)
			reclist = Arrays.asList(recIds);

		hiddenNodes = new HashSet<Node>();
		hiddenNodeIds1 = new HashSet<Long>();
		hiddenEdges = new HashSet<Long>();
		hiddenEdges1 = new HashSet<Long>();
		hiddenEdges2 = new HashSet<Long>();
		
		hiddenNodeIds = new HashSet<Long>();
		recIdSet = new HashSet<Long>();
		directEdgeIds = new HashSet<Long>();

		Map<Long, Edge> sourceEdges = getNodeEdges(sourceNodeId, null);
		String idMetricType = null;
		String seMetricType = null;
		
		RecommenderService service = (RecommenderService) Beans.getBean("recommenderServiceImpl");
		
		List<Recommendation> recs = service.getRecommendations(
		sourceNodeId, keyWords, Integer.parseInt(numRecs), "and",
		questionId);
	
		for (Recommendation rec : recs) {
			double idscore = rec.getIdentifyScore();
			maxRecSize= Math.max(maxRecSize, idscore);
			minRecSize= Math.min(minRecSize, idscore);
		}
	
		if (recIds == null) {			

			for (Recommendation rec : recs) {
				Node recNode = rec.getTarget();
				recTypes.add(recNode.getType());
				recIdSet.add(recNode.getId());
				nodeIds.add(recNode.getId());
			}

			Map<String, Set<String>> recRels = recRelType(sourceType, recTypes);

			for (Recommendation rec : recs) {

				Node recNode = rec.getTarget();
				
				Set<String> rels = recRels.get(recNode.getType());
				
				double idscore = rec.getIdentifyScore();
				double sescore = rec.getSelectScore();
				double totalScore = rec.getFinalScore();

				seMetricType = rec.getSeMetricType();
				idMetricType = rec.getIdMetricType();
				idSeScores.add(totalScore + "-" + idscore + "-" + sescore);
				dealNodesSet(rels, sourceEdges, getNodeEdges(recNode.getId(),
						null), sourceNode, recNode);
				Set<Long> removedEdge = removeEdges(edgesMap, rels);

				edgeIds.removeAll(removedEdge);
			}
			
			//rec nodes can not be a hidden node
			for (Recommendation rec : recs) {

				Node recNode = rec.getTarget();	
				
				hiddenNodeIds.remove(recNode.getId());
				
			}

		} else {
			for (int i = 0; i < recIds.length; i++) {

				recIdSet.add(Long.parseLong(recIds[i]));
				recTypes.add(nodeDao.loadById(Long.parseLong(recIds[i]))
						.getType());
				nodeIds.add(Long.parseLong(recIds[i]));
			}

			Map<String, Set<String>> recRels = recRelType(sourceType, recTypes);

			for (int i = 0; i < recIds.length; i++) {
				
				String idscore = idScores[i];
				String sescore = seScores[i];
				String totalScore = finalScores[i];
				idMetricType = idMetricTypes[i];
				seMetricType = seMetricTypes[i];
				idSeScores.add(totalScore + "-" + idscore + "-" + sescore);
				Set<String> rels = recRels.get(nodeDao.loadById(
						Long.parseLong(recIds[i])).getType());
				
				dealNodesSet(rels, sourceEdges, getNodeEdges(Long
						.parseLong(recIds[i]), null), sourceNode, nodeDao
						.loadById(Long.parseLong(recIds[i])));
				Set<Long> removedEdge = removeEdges(edgesMap, rels);

				edgeIds.removeAll(removedEdge);
				
				edgeIds.addAll(hiddenEdges1);
				edgeIds.addAll(hiddenEdges2);
			}
			
			//rec nodes can not be a hidden node
			for (int i = 0; i < recIds.length; i++) {				
				hiddenNodeIds.remove(Long.parseLong(recIds[i]));
				
			}

		}

		idSeScores.add(idMetricType + "||" + seMetricType + "||" + targetLabel); // artificial
																					// scores
																					// for
																					// target

		// idSeScores.add(targetLabel);

		/*
		 * System.out.println("****nodeid size: " + nodeIds);
		 * System.out.println("****edgeid size: " + edgeIds.size());
		 * System.out.println("****hidden edgeid size: " + hiddenEdges.size());
		 * System.out.println("****hidden nodeid size: " +
		 * hiddenNodeIds.size()); System.out.println("****hidden edgeid1 size: "
		 * + hiddenEdges1.size());
		 */
	}

	private Map<String, Set<String>> recRelType(String userType,
			Set<String> recTypes) {
		Map<String, Set<String>> rec_rels = new HashMap<String, Set<String>>();

		try {
			Document doc = GeneralUtil.readXMLFromClasspath("recconfig.xml");
			Element root = doc.getRootElement();
			Element seElement = root.element("selection");
			for (Element pair : (List<Element>) seElement.elements()) {
				if (pair.attributeValue("row").equals(userType)) {
					for (String recType : recTypes) {
						if (pair.attributeValue("col").equals(recType)) {

							Set<String> rels = new HashSet<String>();
							
							if(pair.element("entry")!= null){
		    					Element enElement = pair.element("entry"); 		 				
		    					for (Element edge: (List<Element>)enElement.elements()){				    	
			    					rels.add(edge.attributeValue("type"));
			    				}
		    				}else{
		    					
		    					for (Element edge: (List<Element>)pair.elements()){	
		    						rels.add(edge.attributeValue("type"));
		    					}
		    				}
							rec_rels.put(recType, rels);
						}
					}
				} else if (pair.attributeValue("col").equals(userType)) {
					for (String recType : recTypes) {
						if (pair.attributeValue("row").equals(recType)) {

							Set<String> rels = new HashSet<String>();
							
							if(pair.element("entry")!= null){
		    					Element enElement = pair.element("entry"); 		 				
		    					for (Element edge: (List<Element>)enElement.elements()){				    	
			    					rels.add(edge.attributeValue("type"));
			    				}
		    				}else{
		    					
		    					for (Element edge: (List<Element>)pair.elements()){	
		    						rels.add(edge.attributeValue("type"));
		    					}
		    				}
							rec_rels.put(recType, rels);
						}
					}
				}
			}

		} catch (IOException e) {

		} catch (DocumentException d) {

		}

		return rec_rels;
	}

	private Map<Long, Edge> getNodeEdges(Long nodeId, Set<String> rels) {
		List<Edge> sourceNodeEdges = edgeDao.loadByFromNodeId(nodeId);
		List<Edge> edges1 = edgeDao.loadByToNodeId(nodeId);

		Map<Long, Edge> sourceNodeEdgeMap = new HashMap<Long, Edge>();

		for (Edge e : sourceNodeEdges) {
			if (rels != null) {
				if (rels.contains(e.getType()))
					sourceNodeEdgeMap.put(e.getId(), e);
			} else {
				sourceNodeEdgeMap.put(e.getId(), e);
			}
		}

		for (Edge e : edges1) {
			if (rels != null) {
				if (rels.contains(e.getType()))
					sourceNodeEdgeMap.put(e.getId(), e);
			} else {
				sourceNodeEdgeMap.put(e.getId(), e);
			}
		}

		return sourceNodeEdgeMap;
	}

	private void dealNodesSet(Set<String> rels, Map<Long, Edge> edges_1,
			Map<Long, Edge> edges_2, Node sourceNode, Node recNode) {

		if (edges_1 != null && edges_2 != null) { // if 11111111
			boolean directRec = false;
			boolean twoSteps = false;
			// check if the two edgesset has common edges (there is direct path
			// between User and Rec).
			Set<Long> commonKeys = new HashSet<Long>();
			commonKeys.addAll(edges_1.keySet());
			commonKeys.retainAll(edges_2.keySet());

			if (commonKeys.size() > 0) {
				
				for (Long eId : commonKeys) {
						edgeIds.add(eId);
						edgesMap.put(eId, edges_1.get(eId));
						if (rels.contains(edges_1.get(eId).getType())) {
							directRec = true;
							directEdgeIds.add(eId);
						}
				}
				
				if(!directRec){
					
					for (Long eId1 : commonKeys) {
						edgeIds.add(eId1);
						edgesMap.put(eId1, edges_1.get(eId1));					
						hiddenEdges1.add(eId1);
						
					}
				}
				
			}// commonKeys.size() > 0
	
			
		
			if (!directRec) {
				// get first step path
				Set<Long> bridgeNodes = processNodeBridge(rels, edges_1,
						edges_2);
				if (bridgeNodes.size() > 0)
					twoSteps = true;
				if (twoSteps) {
					processEdgeForBridgeNodes(rels, edges_1, edges_2,
							bridgeNodes);
					Set<Long> removeNodeIds = new HashSet<Long>();
					Set<Long> newBridgeNodes = bridgeNodes;

					for (Long recid : recIdSet) {

						if (bridgeNodes.contains(recid)) {

							removeNodeIds.add(recid);
						}
					}
					newBridgeNodes.removeAll(removeNodeIds);
					hiddenNodeIds.addAll(newBridgeNodes);
			
					nodeIds.addAll(newBridgeNodes);
					
				} else {
					// for real hidden nodes and edges
					processEdgeForBridgeNodes(null, edges_1, edges_2,
							noRecRelationNode);
					Set<Long> removeNodeIds = new HashSet<Long>();
					Set<Long> newBridgeNodes = noRecRelationNode;

					for (Long recid : recIdSet) {

						if (noRecRelationNode.contains(recid)) {

							removeNodeIds.add(recid);
						}
					}
					
					newBridgeNodes.removeAll(removeNodeIds);
					hiddenNodeIds1.addAll(newBridgeNodes);
					nodeIds.addAll(newBridgeNodes);
					
					
					Set<Node> source1step = new HashSet<Node>();
					Set<Node> rec1step = new HashSet<Node>();

					for (Long eId : edges_1.keySet()) {
						Edge e = edges_1.get(eId);
						if (rels.contains(e.getType())) {
							if (e.getFromNode().getId() != sourceNode.getId())
								source1step.add(e.getFromNode());
							if (e.getToNode().getId() != sourceNode.getId())
								source1step.add(e.getToNode());

						}

					}
					for (Long eId : edges_2.keySet()) {
						Edge e = edges_2.get(eId);
						if (rels.contains(e.getType())) {
							if (e.getFromNode().getId() != recNode.getId())
								rec1step.add(e.getFromNode());
							if (e.getToNode().getId() != recNode.getId())
								rec1step.add(e.getToNode());

						}

					}

					Map<Long, Edge> e_source1step = new HashMap<Long, Edge>();
					Map<Long, Edge> e_rec1step = new HashMap<Long, Edge>();

					for (Node n : source1step) {
						e_source1step.putAll(getNodeEdges(n.getId(), rels));

					}

					for (Node n : rec1step) {
						e_rec1step.putAll(getNodeEdges(n.getId(), rels));

					}

					Map<Long, Edge> finalcoEdgeMap = new HashMap<Long, Edge>();
					// Map<Long, Edge> finalcoEdgeMap = e_source1step;
					finalcoEdgeMap.putAll(e_source1step);

					Set<Long> finalcoEdges = finalcoEdgeMap.keySet();
					finalcoEdges.retainAll(e_rec1step.keySet());

					boolean haspath = false;
					// find edges and node from the final common edges

					Set<Long> nodeIdSet = new HashSet<Long>();

					for (Long eId : finalcoEdges) {
						
						// get nodes for common edges
						Edge e = e_source1step.get(eId);

						Node from = e.getFromNode();
						Node to = e.getToNode();

						nodeIdSet.add(from.getId());
						nodeIdSet.add(to.getId());

						hiddenEdges.add(eId);
						edgeIds.add(eId);

						hiddenNodeIds.add(from.getId());
						hiddenNodeIds.add(to.getId());
						edgesMap.put(eId, e);
						
					}
					
					// edges from source to one intermedia or from rec to
					// another intermedia
					for (Long eId : e_source1step.keySet()) {
						// get nodes for common edges
						Edge e = e_source1step.get(eId);

						if (nodeIdSet.contains(e.getFromNode().getId())
								|| nodeIdSet.contains(e.getToNode().getId())) {
							if (edges_1.keySet().contains(eId)) {
								hiddenEdges.add(eId);
								edgeIds.add(eId);
								edgesMap.put(eId, e);
							}
						}
					}

					for (Long eId : e_rec1step.keySet()) {
						// get nodes for common edges
						Edge e = e_rec1step.get(eId);
						if (nodeIdSet.contains(e.getFromNode().getId())
								|| nodeIdSet.contains(e.getToNode().getId())) {
							if (edges_2.keySet().contains(eId)) {
								hiddenEdges.add(eId);
								edgeIds.add(eId);
								edgesMap.put(eId, e);
							}
						}
					}

					nodeIds.addAll(nodeIdSet);

					hiddenNodeIds.remove(sourceNode.getId());
					hiddenNodeIds.remove(recNode.getId());
					hiddenNodeIds1.remove(sourceNode.getId());
					hiddenNodeIds1.remove(recNode.getId());
					
					
				}
			}

		} // if
	}

	private Set<Long> processNodeBridge(Set<String> rels,
			Map<Long, Edge> edges_1, Map<Long, Edge> edges_2) {
		boolean oneBridgeRec = false;
		boolean oneBridgeRec1 = false;
		// get total nodes, hidden nodes

		Set<Long> nodes_fromEdges_1 = new HashSet<Long>();
		Set<Long> nodes_fromEdges_2 = new HashSet<Long>();

		Set<Long> nodes_fromEdges_11 = new HashSet<Long>();
		Set<Long> nodes_fromEdges_21 = new HashSet<Long>();
		
		// get nodes from Edges_1
		for (Long eId : edges_1.keySet()) {
			Edge e = edges_1.get(eId);
			if (rels.contains(e.getType())) {
				oneBridgeRec1 = true;
				nodes_fromEdges_1.add(e.getFromNode().getId());
				nodes_fromEdges_1.add(e.getToNode().getId());
			}else{
				nodes_fromEdges_11.add(e.getFromNode().getId());
				nodes_fromEdges_11.add(e.getToNode().getId());
			}
		}// for
		// get nodes from Edges_2
		for (Long eId : edges_2.keySet()) {

			Edge e = edges_2.get(eId);
			if (rels.contains(e.getType()) && oneBridgeRec1) {
				oneBridgeRec = true;
				nodes_fromEdges_2.add(e.getFromNode().getId());
				nodes_fromEdges_2.add(e.getToNode().getId());
			}else{
				nodes_fromEdges_21.add(e.getFromNode().getId());
				nodes_fromEdges_21.add(e.getToNode().getId());
			}
		} // for

		if (oneBridgeRec)
			nodes_fromEdges_1.retainAll(nodes_fromEdges_2);
		
			nodes_fromEdges_11.retainAll(nodes_fromEdges_21);
			noRecRelationNode = new HashSet<Long>();
			noRecRelationNode.addAll(nodes_fromEdges_11);
		return nodes_fromEdges_1;
	}

	private void processEdgeForBridgeNodes(Set<String> rels,
			Map<Long, Edge> edges_1, Map<Long, Edge> edges_2,
			Set<Long> bridgeNodes) {

		for (Long eId : edges_1.keySet()) {
			Edge e = edges_1.get(eId);
			Long fromNodeId = e.getFromNode().getId();
			Long toNodeId = e.getToNode().getId();

			if (bridgeNodes.contains(fromNodeId)
					|| bridgeNodes.contains(toNodeId)) {
				if(rels == null){
					hiddenEdges2.add(eId);
				
				}else if (rels.contains(edges_1.get(eId).getType())) {
					hiddenEdges.add(eId);
				}
				
				edgeIds.add(eId);
				edgesMap.put(eId, e);
				
			}

		}// for
		// get nodes from Edges_2
		for (Long eId : edges_2.keySet()) {
			Edge e = edges_2.get(eId);
			Long fromNodeId = e.getFromNode().getId();
			Long toNodeId = e.getToNode().getId();

			if (bridgeNodes.contains(fromNodeId)
					|| bridgeNodes.contains(toNodeId)) {

				if(rels == null){
					hiddenEdges2.add(eId);
				
				}else if (rels.contains(edges_2.get(eId).getType())) {
					hiddenEdges.add(eId);
				}
				
				edgeIds.add(eId);
				edgesMap.put(eId, e);
			}

		}// for

	}

	private Set<Long> removeEdges(Map<Long, Edge> edges, Set<String> rels) {
		Set<Long> removededges = new HashSet<Long>();
		// remove inelgible egdes
		for (Long eId : edges.keySet()) {

			Edge e = edges.get(eId);

			if (!rels.contains(e.getType())) {

				removededges.add(eId);
			}

		}
		return removededges;
	}

	/*
	 * private Set<Long> removeEdges(Map<Long, Edge> edges, List<Long> nodeIds)
	 * { Set<Long> removededges = new HashSet<Long>(); // remove inelgible egdes
	 * for (Long eId : edges.keySet()) {
	 * 
	 * Edge e = edges.get(eId);
	 * 
	 * if((!nodeIds.contains(e.getFromNode().getId())) ||
	 * (!nodeIds.contains(e.getToNode().getId()))){
	 * 
	 * removededges.add(eId); }
	 * 
	 * } return removededges; }
	 */
	public List<Node> getHiddenNodes() {
		
		if (hiddenNodeIds != null && hiddenNodeIds.size() != 0) {
			
			return nodeDao.loadByIds(hiddenNodeIds);
		} else
			return new ArrayList<Node>();
	}

	public Set<Long> getHiddenEdgeIds() {
		Set<Long> notHiddenEdges = new HashSet<Long>();
		if (directEdgeIds != null) {
			
			for (Long edgeId : directEdgeIds) {

				if (hiddenEdges.contains(edgeId))
					notHiddenEdges.add(edgeId);
			}
			hiddenEdges.removeAll(notHiddenEdges);
		}
		return hiddenEdges;
	}

	public Set<Long> getHiddenEdge1Ids() {
		
		Set<Long> notHiddenEdges1 = new HashSet<Long>();
		Set<Long> newhiddenEdges = getHiddenEdgeIds();
		if (newhiddenEdges != null) {

			for (Long edgeId : newhiddenEdges) {

				if (hiddenEdges1.contains(edgeId))
					notHiddenEdges1.add(edgeId);
			}
		}
		
		if (directEdgeIds != null) {
			
			for (Long edgeId : directEdgeIds) {

				if (hiddenEdges1.contains(edgeId))
					notHiddenEdges1.add(edgeId);
			}
		}	

		hiddenEdges1.removeAll(notHiddenEdges1);

		return hiddenEdges1;
	}

public Set<Long> getHiddenEdge2Ids() {
		
		Set<Long> notHiddenEdges2 = new HashSet<Long>();
		Set<Long> newhiddenEdges = getHiddenEdgeIds();
		if (newhiddenEdges != null) {

			for (Long edgeId : newhiddenEdges) {

				if (hiddenEdges2.contains(edgeId))
					notHiddenEdges2.add(edgeId);
			}
		}
		
		if (directEdgeIds != null) {
			
			for (Long edgeId : directEdgeIds) {

				if (hiddenEdges2.contains(edgeId))
					notHiddenEdges2.add(edgeId);
			}
		}	

		hiddenEdges2.removeAll(notHiddenEdges2);

		return hiddenEdges2;
	}

	public List<Node> getHiddenNodes1() {
		Set<Long> notHiddenNodes1 = new HashSet<Long>();
	
		if (hiddenNodeIds != null) {

			for (Long nodeId : hiddenNodeIds) {
				
				if (hiddenNodeIds1.contains(nodeId))
					notHiddenNodes1.add(nodeId);
			}
		}

			hiddenNodeIds1.removeAll(notHiddenNodes1);
			
			if (hiddenNodeIds1 != null && hiddenNodeIds1.size() != 0) {
				
				return nodeDao.loadByIds(hiddenNodeIds1);
			} else
				return new ArrayList<Node>();
	}
	
	
	public List<String> getIdSeScores() {

		return idSeScores;
	}

	public List<Node> getNodeList() {

		List<Node> nodes = new ArrayList<Node>();
		Set<Long> nodeids = new HashSet<Long>();
		for (Long id : nodeIds) {
			if (!nodeids.contains(id))
				nodes.add(nodeDao.loadById(id));
			nodeids.add(id);
		}
		if (questionId.equalsIgnoreCase("-1"))
			nodes.add(targetNode);
		return nodes;

	}

	public Set<Node> getRecNodeSet() {

		Set<Node> nodes = new HashSet<Node>();
		for (Long id : recIdSet) {

			nodes.add(nodeDao.loadById(id));
		}

		return nodes;

	}

	public List<Edge> getEdges() {
		List<Edge> edges = new ArrayList<Edge>();
		for (Long eId : edgeIds) {
			Edge e = edgeDao.loadById(eId);

			edges.add(e);
			// System.out.println("***** edge: " + e);
		}

		return edges;
	}
	
	public double getMaxRecSize(){
		return maxRecSize;
	}
	
	public double getMinRecSize(){
		return minRecSize;
	}
	

}