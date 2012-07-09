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
 * Created by IntelliJ IDEA. User: jinling Date: Dec 3, 2008 Time: 11:32:17 AM
 * To change this template use File | Settings | File Templates.
 */

public class RecommenderNetwork {
	private static Log logger = LogFactory.getLog(RecommenderNetwork.class);

	private Set<Long> hiddenNodes;
	private Set<Long> nodes;
	private Set<Long> user_recNodes;
	private Set<Long> r_tHiddenNodes;
	private Set<Long> r_tNodes;
	private Set<Long> bioNodes;

	public Node targetNode;
	public Node sourceNode;
	public Set<Node> recNodes;

	public Long targetNodeId;
	public Long sourceNodeId;
	public List<Long> recNodeIds;

	// edges between source and rec nodes(from
	// or to) that contains edges whose node
	// is target.
	// or edges between rec and target nodes(from or to) that contains edges
	// whose node is source.
	private Map<Long, Edge> edgesMap;
	private Set<Long> edgeIds, hiddenEdgeIds;
	private Set<Long> initNode;
	private Set<Long> stNodes;
	private NodeDao nodeDao;
	private EdgeDao edgeDao;
	private boolean utHasDirLink;
	private Set<Long> visNodesIds; 
	private RecommenderNetwork2 rn2;
	private String rectitle;
	private boolean forNodeType;
	private List<String> idSeScores; 
	private String pathStr ="";
	private Set<String> rels = null;
	String[] recIds = null;
	private Long directLinkId;
	public static void main(String[] args) {

		String sourceId = "1";
		String[] recIds = new String[1];
		recIds[0] = "95";
		//recIds[1] = "6";
		String[] seScores = new String[1];
		seScores[0] = "304.56689453125";
		String[] idScores = new String[1];
		idScores[0] = "988.2388305664063";
		String[] finalScores = new String[1];
		finalScores[0] = "300984.84375";
		String[] idMetricTypes = new String[1];
		String[] seMetricTypes = new String[1];
		idMetricTypes[0] = "sm.euclidean";
		seMetricTypes[0] = "sm.euclidean";
		// String targetLabel = "(Alli's Journey) Shainhouse, Pamela";	
		//http://localhost:8400/_ciknow/vis?networkType=recommender&sourceId=1&recId=95&identifyScore=988.2388305664063&idMetricType=sm.euclidean&selectScore=304.56689453125&seMetricType=sm.euclidean&finalScore=300984.84375&questionId=-1&os=Win&targetName=Bernard%2C%20Russ&targetLabel=Bernard%2C%20Russ	
		 String targetLabel = "Bernard, Russ";
	
	RecommenderNetwork rn = new RecommenderNetwork( Long.parseLong(sourceId), recIds, seScores,  idScores, finalScores, idMetricTypes, seMetricTypes, targetLabel, targetLabel, null, "-1");

	
		System.out.println("rn.getNodeList(): " + rn.getNodeList().size());
		System.out.println("rn.getEdges(): " + rn.getEdges());
		System.out.println("rn.getHiddenEdgeIds(): " + rn.getHiddenEdgeIds());
		System.out.println("rn.getHiddenNodes(): " + rn.getHiddenNodes());
		System.out.println("rn.getTitleStr() + rn.getRecScores(): " + rn.getTitleStr() + rn.getRecScores());
		System.out.println("rn.getPathStr(): " + rn.getEdges());
		System.out.println("rn.sourceNode: " + rn.sourceNode);
	System.out.println("rn.recNodes: " + rn.recNodes);
		System.out.println("rn.targetNode: " + rn.targetNode);
		
	}

	public RecommenderNetwork(Long sourceId, String[] oriRecIds, String[] seScores, String[] idScores, String[]finalScores,String[]idMetricTypes,String[]seMetricTypes,
			String targetName, String targetLabel, String numRecs, String questionId) {
		
		Beans.init();
		
		Set<String> recTypes = new HashSet<String>();
		String sourceType;
		
		nodeDao = (NodeDao) Beans.getBean("nodeDao");
			idSeScores = new ArrayList<String>();
		
		sourceNode = nodeDao.findById(sourceId);
		sourceType = sourceNode.getType();
		
		if(questionId.equalsIgnoreCase("-1")){
		//	String finalScore = "finalScore:||";
			if (oriRecIds == null || oriRecIds.length >1) {
				//String[] score_id = null;
				Set<Double> totalScoreSet = new HashSet<Double>();
				List<Recommendation>   recs = null;;
				if (oriRecIds == null ){
					RecommenderService service = (RecommenderService) Beans.getBean("recommenderServiceImpl");
					List<String> keyWords = new ArrayList<String>();
					keyWords.add(targetLabel);
					 recs = service.getRecommendations(sourceId, keyWords, Integer.parseInt(numRecs), "and", questionId);
					 recIds = new String[recs.size()];
					
					 int m =0;
					 for(Recommendation rec: recs){
						totalScoreSet.add(rec.getFinalScore());	
						Node recNode = rec.getTarget();
						recTypes.add(recNode.getType());
						recIds[m]= (recNode.getId().toString());
						m++;
					}
				}else{
					
					recIds = new String[oriRecIds.length];
					recIds = oriRecIds;
					for(int j = 0; j < finalScores.length; j++){
						
						recTypes.add(nodeDao.loadById(Long.parseLong(recIds[j])).getType());
						totalScoreSet.add(Double.parseDouble(finalScores[j]));
					}
						
				}
					Double[] totalScoreArray = new Double[totalScoreSet.size()];
					int n = 0;
					for(double score: totalScoreSet){
						totalScoreArray[n]= score;
						n++;                
					}
				
					Arrays.sort(totalScoreArray);
					
					Map<String, Set<String>>  recRels = recRelType(sourceType, recTypes);
					
				int k = 0;
				for(int i = 0; i <totalScoreArray.length; i++){
					
					if(oriRecIds == null){	
						
						for(Recommendation rec: recs){
							Long recNodeId = rec.getTarget().getId();
							double totalScore = rec.getFinalScore();
							double idscore = rec.getIdentifyScore();
							 double sescore = rec.getSelectScore();
							 Node recNode = rec.getTarget();
							  rels = recRels.get(recNode.getType());
				
							if(totalScoreArray[totalScoreArray.length -1 -i] == totalScore){
								//recIds[k] = recNodeId+"";
								idSeScores.add(sescore  + "-" + idscore + "-" + totalScore);
							//	idSeScores.add(idscore  + "-" + sescore + "-" + totalScore);
							
								k++;
							}								
						}
				
					}else{
					
						for(int m = 0; m < finalScores.length; m++){
							
							if(totalScoreArray[totalScoreArray.length -1 -i] == Double.parseDouble(finalScores[m])){
								
								//recIds[k] = oriRecIds[m];
								String idscore = idScores[m];
								 String sescore = seScores[m];
								 String totalScore = finalScores[m];
							idSeScores.add(sescore  + "-" + idscore + "-" + totalScore);
							//	idSeScores.add(idscore  + "-" + sescore + "-" + totalScore);
								
								 rels = recRels.get(nodeDao.loadById(Long.parseLong(recIds[i])).getType());
									
								k++;
							}
						}
					
					}//if
				}//for				
			}else{
				
				recIds = new String[0];
				recIds = oriRecIds;
					String nodeType = nodeDao.loadById(Long.parseLong(recIds[0])).getType();
					recTypes.add(nodeType);
						
				Map<String, Set<String>>  recRels = recRelType(sourceType, recTypes);
							
				  rels = recRels.get(nodeType);
				 				
				String idscore = idScores[0];
				 String sescore = seScores[0];
				 String totalScore = finalScores[0];
				idSeScores.add(sescore  + "-" + idscore + "-" + totalScore);
				//idSeScores.add(idscore  + "-" + sescore + "-" + totalScore);
				
			}
			
			targetNode = nodeDao.findByLabel(targetLabel).get(0);	
			forNodeType = true;
			logger.info("recIds: " + Arrays.toString(recIds));
			recNodes = new HashSet<Node>();
			recNodeIds = new ArrayList<Long>();
			for(int i = 0; i< recIds.length; i++){
				Long recId = Long.parseLong(recIds[i]);
				recNodes.add(nodeDao.findById(recId));
				recNodeIds.add(recId);
				
			}
				
			logger.info("get recommender network ...");

			edgeDao = (EdgeDao) Beans.getBean("edgeDao");

		
			sourceNodeId = sourceId;
				
			targetNodeId = targetNode.getId();

			stNodes = new HashSet<Long>();
			stNodes.add(sourceId);
			stNodes.add(targetNodeId);
			
			
			logger.debug("sourceNode: " + sourceNode.getLabel());
		//	logger.debug("recNodes: " + recNodes);
			logger.debug("targetNode: " + targetLabel);

			nodes = new HashSet<Long>();
			hiddenNodes = new HashSet<Long>();
			visNodesIds = new HashSet<Long>();
			
			user_recNodes = new HashSet<Long>();
			r_tNodes = new HashSet<Long>();
			r_tHiddenNodes = new HashSet<Long>();

			edgesMap = new HashMap<Long, Edge>();
			edgeIds = new HashSet<Long>();
			hiddenEdgeIds = new HashSet<Long>();
			
			initNode = new HashSet<Long>();
		
				initNode.add(sourceNodeId);		
				initNode.add(targetNodeId);
		
			// all edges linked to "source" node
			List<Edge> sourceNodeEdges = edgeDao.findByFromNodeId(sourceId);
			List<Edge> edges1 = edgeDao.findByToNodeId(sourceId);

			Map<Long, Edge> sourceNodeEdgeMap = new HashMap<Long, Edge>();
			Set<Long> sourceNodeEdgeIds = new HashSet<Long>();

			for (Edge e : sourceNodeEdges) {
				
				if(rels.contains(e.getType())){
				sourceNodeEdgeIds.add(e.getId());
				sourceNodeEdgeMap.put(e.getId(), edgeDao.loadById(e.getId()));
				
				if (e.getToNode().getId().equals(targetNodeId)){
					utHasDirLink = true;
					directLinkId = e.getId();
				}
				}
			}

			for (Edge e : edges1) {
				if(rels.contains(e.getType())){
				sourceNodeEdgeIds.add(e.getId());
				sourceNodeEdgeMap.put(e.getId(), edgeDao.loadById(e.getId()));
				if (e.getToNode().getId().equals(targetNodeId)){
					utHasDirLink = true;
					directLinkId = e.getId();
				}
				}
			}

			// all edges linked to "target" node
			List<Edge> tarNodeEdges = edgeDao.findByFromNodeId(targetNodeId);
			List<Edge> tarNodeEdges1 = edgeDao.findByToNodeId(targetNodeId);

			Map<Long, Edge> tarNodeEdgeMap = new HashMap<Long, Edge>();
			Set<Long> tarNodeEdgeIds = new HashSet<Long>();

			for (Edge e : tarNodeEdges) {
				if(rels.contains(e.getType())){
				tarNodeEdgeIds.add(e.getId());
				tarNodeEdgeMap.put(e.getId(), edgeDao.loadById(e.getId()));
				}
			}

			for (Edge e : tarNodeEdges1) {
				if(rels.contains(e.getType())){
				tarNodeEdgeIds.add(e.getId());
				tarNodeEdgeMap.put(e.getId(), edgeDao.loadById(e.getId()));
				}
			}
			
			// all edges linked to "rec" node
		
			
			for(String recIdStr: recIds){
				List<Edge> recNodeEdges = new ArrayList<Edge>();
				List<Edge> recNodeEdges1 = new ArrayList<Edge>();
				Long recId = Long.parseLong(recIdStr);
				Node recNode = nodeDao.loadById(recId);
				 recNodeEdges.addAll(edgeDao.findByFromNodeId(recId));
				 recNodeEdges1.addAll(edgeDao.findByToNodeId(recId));
				 
			
				Map<Long, Edge> recNodeEdgeMap = new HashMap<Long, Edge>();
				Set<Long> recNodeEdgeIds = new HashSet<Long>();
			
			for (Edge e : recNodeEdges) {
				if(rels.contains(e.getType())){
				recNodeEdgeIds.add(e.getId());
				recNodeEdgeMap.put(e.getId(), edgeDao.loadById(e.getId()));
				}
			}

			for (Edge e : recNodeEdges1) {
				if(rels.contains(e.getType())){
				recNodeEdgeIds.add(e.getId());
				recNodeEdgeMap.put(e.getId(), edgeDao.loadById(e.getId()));
				}
			}
			
				String relation1_path = dealNodesSet(rels, sourceNodeEdgeMap, recNodeEdgeMap, sourceNode, recNode, 1);
				 
				String relation2_path =  dealNodesSet(rels, recNodeEdgeMap, tarNodeEdgeMap, recNode, targetNode,  2);
				
				pathStr += relation1_path + relation2_path + "||";
				
			}
			
			
			/*
			Integer[] ur_depthArray = ur_depthSet.toArray(new Integer[ur_depthSet.size()]);
			Integer[] rt_depthArray = rt_depthSet.toArray(new Integer[rt_depthSet.size()]);
			Arrays.sort(ur_depthArray);
			Arrays.sort(rt_depthArray);
			
			// for network with multiple recommendation, get the max depth
			ur_finaldepth = ur_depthArray[ur_depthSet.size()-1];
			rt_finaldepth = rt_depthArray[rt_depthSet.size()-1];
			
			*/
			// get nodes
			try {
				
				bioNodes = new HashSet<Long>();
				for (Long n : r_tNodes) {
					if (user_recNodes.contains(n)) {			
						bioNodes.add(n);			
					}
				}

				if(bioNodes.size() > 0){
					
					int urSize = user_recNodes.size();
					int rtSize = r_tNodes.size();
					
					for(Long n: bioNodes){
						if(hiddenNodes.contains(n)||r_tHiddenNodes.contains(n)){
							
							if(r_tHiddenNodes.contains(n)){
								r_tNodes.remove(n);
							}else{
								user_recNodes.remove(n);
							}
						}else{	
							
							if(urSize < rtSize){
								r_tNodes.removeAll(bioNodes);
								
							}else{	
								user_recNodes.removeAll(bioNodes);
								
							}
						}	
					}
				}

				Set<Long> remove1 = getNotHidden(hiddenNodes,  r_tNodes,
						r_tHiddenNodes);

				
				Set<Long> remove2 = getNotHidden(r_tHiddenNodes, user_recNodes,
						hiddenNodes);

				if (remove1 != null) {
					hiddenNodes.removeAll(remove1);					
				}

				if (remove2 != null) {
					r_tHiddenNodes.removeAll(remove2);					
				}
						
				hiddenNodes.addAll(r_tHiddenNodes);
				
				if(hiddenNodes !=null){
				if (hiddenNodes.contains(sourceNodeId)) {

					hiddenNodes.remove(sourceNodeId);

				}

				if (hiddenNodes.contains(recNodeIds)) {

					hiddenNodes.remove(recNodeIds);

				}

				if (hiddenNodes.contains(targetNodeId)) {

					hiddenNodes.remove(targetNodeId);

				}
				}
			
				//for multiple recommendation network, some nodes may be hidden for one recommendation but not hidden for other recommendations
			
				Set<Long> remove3  = notHiddenForMultiRec(hiddenNodes, visNodesIds);
				

				if (remove3 != null) 
				hiddenNodes.removeAll(remove3);
			
				/*nodes.addAll(r_tNodes);
				nodes.add(sourceNodeId);
				nodes.addAll(recNodeIds);
				nodes.add(targetNodeId);
			*/
				///// edges
			
			//	Set<Long> notHiddenEdges1 = getNotHiddenEdge(hiddenEdgeIds, edgeIds);
				
				if(hiddenEdgeIds!=null){
					
				//	if(notHiddenEdges1 != null)
				//hiddenEdgeIds.removeAll(notHiddenEdges1);
			
					Set<Long> notHiddenEdges = new HashSet<Long>();
					for(Long eid: hiddenEdgeIds){
						if(edgeIds.contains(eid))
							notHiddenEdges.add(eid);	
					}
					hiddenEdgeIds.removeAll(notHiddenEdges);	
				edgeIds.addAll(hiddenEdgeIds);
			
				}
				
				// remove all invalid edges
			/*	
				nodes.addAll(user_recNodes);
				nodes.addAll(r_tNodes);
				
				Set<Long> removedEdge = removeEdges(edgesMap, nodes);
			
			edgeIds.removeAll(removedEdge);
			*/
				
			} catch (Exception e) {
				//System.out.println(" Exception when processing eges: " + e);
			}
		}else{
			if(questionId.equalsIgnoreCase("-1"))
						targetNode = nodeDao.findByLabel(targetLabel).get(0);	
					
					rn2 = new RecommenderNetwork2(sourceId, oriRecIds, seScores, idScores, finalScores, idMetricTypes, seMetricTypes, targetName, targetLabel, numRecs,
							questionId);
					SetTitleString();
					recNodes = rn2.getRecNodeSet();
					
					
		}
	
	}

	private Map<String, Set<String>> recRelType(String userType, Set<String> recTypes) {
		Map<String, Set<String>> rec_rels = new HashMap<String, Set<String>>();
		
		 try{
		 Document doc = GeneralUtil.readXMLFromClasspath("recconfig.xml");
	    	Element root = doc.getRootElement();	    	
	    	 Element seElement = root.element("selection"); 	    	
			    for (Element pair : (List<Element>)seElement.elements()){
			    	
			    	if (pair.attributeValue("row").equals(userType)){			  		
			    		for(String recType: recTypes){			  			
			    			if (pair.attributeValue("col").equals(recType)){			    				
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
			    	}else if (pair.attributeValue("col").equals(userType)){
			    		for(String recType: recTypes){
			    			if (pair.attributeValue("row").equals(recType)){
			    				
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
	    	 
		 }catch(IOException e){
			 
		 }catch(DocumentException d){
			 
		 }
		 
		return rec_rels; 
	 }
	
	private Set<Long> notHiddenForMultiRec(Set<Long>hiddenNodes, Set<Long >visNodesIds){
		Set<Long> notHidden = new HashSet<Long>();
		for(Long hinode: hiddenNodes){
			if(visNodesIds.contains(hinode))
				notHidden.add(hinode);	
		}
		return notHidden;
	}
	
	private void SetTitleString() {
		
		StringBuilder titleStr = new StringBuilder();

		if (forNodeType) {
			titleStr.append("Recommendation" + "||");			
			//titleStr.append(ur_finaldepth + "||");			
			//titleStr.append(ur_hidden + "||");			
			//titleStr.append(rt_finaldepth + "||");		
		//titleStr.append(rt_hidden + "||");			
			titleStr.append(utHasDirLink + "||");
						
		} else {
			for (String idSeScore : rn2.getIdSeScores()) {
			
				titleStr.append(idSeScore + "||");
			}
		}		
		rectitle = titleStr.toString();

	}

	// hidden nodes from user and rec (rec and target) may used as not hidden
	// nodes between rec and target (user and rec).
	private Set<Long> getNotHidden(Set<Long> hiddenNodes, Set<Long> r_tNodes,
			Set<Long> r_tHiddenNodeIds) {
		Set<Long> notHidden = null;
		if (hiddenNodes.size() > 0) {
			notHidden = new HashSet<Long>();
			for (Long n : hiddenNodes) {
				if ((r_tNodes.contains(n) && !r_tHiddenNodeIds.contains(n))) {
					notHidden.add(n);
				}
			}
		}
	
		return notHidden;
	}

	
	private Set<Long> getNotHiddenEdge(Set<Long> hiddenEdges, Set<Long> visEdges
			) {
		Set<Long> notHidden = null;
		if (hiddenEdges.size() > 0) {
			notHidden = new HashSet<Long>();
			for (Long e : hiddenEdges) {
				if (visEdges.contains(e)) {
					notHidden.add(e);
				}
			}
		}
		
		return notHidden;
	}
	
	
	public List<Node> getNodeList() {
		List<Long> nodeIds = new ArrayList<Long>();
		List<Node> nodes = new ArrayList<Node>();
		if (forNodeType){
				
		if (user_recNodes.contains(sourceNodeId))
			user_recNodes.remove(sourceNodeId);
		for(Long recId: recNodeIds){
		if (user_recNodes.contains(recId)){
			user_recNodes.remove(recId);	
		}
		}
		if (user_recNodes.contains(targetNodeId))
			user_recNodes.remove(targetNodeId);

		for(Long recId: recNodeIds){
			if (r_tNodes.contains(recId)){
				r_tNodes.remove(recId);	
			}
			}
		if (r_tNodes.contains(targetNodeId))
			r_tNodes.remove(targetNodeId);
		if (r_tNodes.contains(sourceNodeId))
			r_tNodes.remove(sourceNodeId);
		
		
		nodeIds.add(sourceNodeId);
		
		nodeIds.addAll(user_recNodes);
		nodeIds.addAll(recNodeIds);
		nodeIds.addAll(r_tNodes);
		
		nodeIds.add(targetNodeId);
		
		SetTitleString();
		
		for (Long id : nodeIds) {
			nodes.add(nodeDao.loadById(id));
			
		}
		
		return nodes;
		}else{
			return rn2.getNodeList();
		}
		
		
	}

	public List<Node> getRec_targetNodes() {

		return getNodeFromIDs(r_tNodes);

	}

	public List<Node> getUser_RecNodes() {
		return getNodeFromIDs(user_recNodes);
	}

	public List<Node> getHiddenNodes() {
		if (forNodeType) 
		return getNodeFromIDs(hiddenNodes);
		else
		return rn2.getHiddenNodes();
	}

	public Set<Long> getHiddenEdgeIds() {
		if (forNodeType) 
			return hiddenEdgeIds;
		else
			return rn2.getHiddenEdgeIds();
	}
	
	public Set<Long> getHiddenEdge1Ids() {
		
			return rn2.getHiddenEdge1Ids();
	}
	
	public Set<Long> getHiddenEdge2Ids() {
		
		return rn2.getHiddenEdge2Ids();
}

	
	public List<Node> getHiddenNodes1() {
		
		return rn2.getHiddenNodes1();
}
	
	public String getMaxRecSize(){
		if (forNodeType) 
			return null;
		else
		return rn2.getMaxRecSize() + "";
		
	}
	public String getMinRecSize(){
		if (forNodeType) 
			return null;
		else
		return rn2.getMinRecSize()+ "";
	}
	
	public List<Edge> getEdges() {
		if(forNodeType){
		List<Edge> edges = new ArrayList<Edge>();
		
		for (Long eId : edgeIds) {
		
			edges.add(edgeDao.loadById(eId));

		}
		
		if(utHasDirLink)
			edges.add(edgeDao.loadById(directLinkId));
		
		return edges;
		
	}else{
			return rn2.getEdges();
		}
	}

	public String getTitleStr() {
		return rectitle;
	}

	public String getPathStr() {
		if(pathStr.length()> 0)
		return pathStr;
		else
			return null;
			
	}
	
	private List<Node> getNodeFromIDs(Set<Long> ids) {

		if (ids != null && ids.size() != 0)
			return nodeDao.loadByIds(ids);
		else
			return new ArrayList<Node>();
	}

	private String dealNodesSet(Set<String> rels, Map<Long, Edge> edges_1,
			Map<Long, Edge> edges_2, Node sourceNode, Node recNode, int relation) {

		int ur_depth = 1, rt_depth = 1;
		boolean is_ur_hidden = false;
		boolean is_rt_hidden = false;
		
		if (edges_1 != null && edges_2 != null) { // if 11111111
			boolean directRec = false;
			boolean twoSteps = false;
			// check if the two edgesset has common edges (there is direct path
			// between User and Rec).
			Set<Long> commonKeys = new HashSet<Long>();
			commonKeys.addAll(edges_1.keySet());
			commonKeys.retainAll(edges_2.keySet());
			
			if (commonKeys.size() > 0) {

				for (Long eId : edges_1.keySet()) {
					if (commonKeys.contains(eId)) {
						edgeIds.add(eId);
						edgesMap.put(eId, edges_1.get(eId));
						
					}
				}
				for (Long eId : edges_2.keySet()) {
					if (commonKeys.contains(eId)) {
						edgeIds.add(eId);
						edgesMap.put(eId, edges_2.get(eId));
						
					}
				}
				directRec= true;
			}// commonKeys.size() > 0

			
				// get first step path
				Set<Long> bridgeNodes = processNodeBridge(edges_1,
						edges_2, relation);
				if (bridgeNodes.size() > 0){
				
					processEdgeForBridgeNodes(edges_1, edges_2,
							bridgeNodes, directRec);
					Set<Long> removeNodeIds = new HashSet<Long>();
					Set<Long> newBridgeNodes = bridgeNodes;

					for (String recid : recIds) {
							Long id = Long.parseLong(recid);
						if (bridgeNodes.contains(id)) {

							removeNodeIds.add(id);
						}
					}
					newBridgeNodes.removeAll(removeNodeIds);
					
					if(relation == 1){
						
						user_recNodes.addAll(newBridgeNodes);
						if (directRec) {
							hiddenNodes.addAll(newBridgeNodes);
							is_ur_hidden = true;
						}else{
							visNodesIds.addAll(newBridgeNodes);
							twoSteps = true;
						}
					}else{
						
						r_tNodes.addAll(newBridgeNodes);
						if (directRec) {
							r_tHiddenNodes.addAll(newBridgeNodes);
							is_rt_hidden = true;
						}else{
							visNodesIds.addAll(newBridgeNodes);
							twoSteps = true;
						}
					}
					
					if(relation ==1){
						ur_depth++;
						
					}else{
						rt_depth++;	
						
					}
					
				} 
					// three steps
					
				else{
					
					Set<Node> source1step = new HashSet<Node>();
					Set<Node> rec1step = new HashSet<Node>();

					for (Long eId : edges_1.keySet()) {
						Edge e = edges_1.get(eId);
						
							if (e.getFromNode().getId() != sourceNode.getId())
								source1step.add(e.getFromNode());
							if (e.getToNode().getId() != sourceNode.getId())
								source1step.add(e.getToNode());

						

					}
					for (Long eId : edges_2.keySet()) {
						Edge e = edges_2.get(eId);
						
							if (e.getFromNode().getId() != recNode.getId())
								rec1step.add(e.getFromNode());
							if (e.getToNode().getId() != recNode.getId())
								rec1step.add(e.getToNode());

						

					}

					
					Map<Long, Edge> e_source1step = new HashMap<Long, Edge>();
					Map<Long, Edge> e_rec1step = new HashMap<Long, Edge>();
					
					initNode.add(sourceNode.getId());
					initNode.add(recNode.getId());
					for (Node n : source1step) {
						if(!initNode.contains(n.getId()))
						e_source1step.putAll(getNodeEdges(n.getId(), rels));

					}

					for (Node n : rec1step) {
						if(!initNode.contains(n.getId()))
						e_rec1step.putAll(getNodeEdges(n.getId(), rels));

					}

					Map<Long, Edge> finalcoEdgeMap = new HashMap<Long, Edge>();
					// Map<Long, Edge> finalcoEdgeMap = e_source1step;
					finalcoEdgeMap.putAll(e_source1step);

					Set<Long> finalcoEdges = finalcoEdgeMap.keySet();
					finalcoEdges.retainAll(e_rec1step.keySet());

					// find edges and node from the final common edges

					Set<Long> nodeIdSet = new HashSet<Long>();

					for (Long eId : finalcoEdges) {
						
						// get nodes for common edges
						Edge e = e_source1step.get(eId);

						Node from = e.getFromNode();
						Node to = e.getToNode();

						nodeIdSet.add(from.getId());
						nodeIdSet.add(to.getId());
						
						edgesMap.put(eId, e);
						if(twoSteps)
							hiddenEdgeIds.add(eId);
						else
							edgeIds.add(eId);
						if(relation == 1){
							user_recNodes.add(from.getId());
							user_recNodes.add(to.getId());
							if(twoSteps){
								hiddenNodes.add(from.getId());
								hiddenNodes.add(from.getId());
							}else{
								
								visNodesIds.add(from.getId());
								visNodesIds.add(to.getId());
							}
						}else{
							r_tNodes.add(from.getId());
							r_tNodes.add(to.getId());
							if(twoSteps){
								r_tHiddenNodes.add(from.getId());
								r_tHiddenNodes.add(to.getId());
							}else{
								
								visNodesIds.add(from.getId());
								visNodesIds.add(to.getId());
							}
						}
					}

					// edges from source to one intermedia or from rec to
					// another intermedia
					for (Long eId : e_source1step.keySet()) {
						// get nodes for common edges
						Edge e = e_source1step.get(eId);

						if (nodeIdSet.contains(e.getFromNode().getId())
								|| nodeIdSet.contains(e.getToNode().getId())) {
							if (edges_1.keySet().contains(eId)) {
								
								edgesMap.put(eId, e);
								if(twoSteps)
									hiddenEdgeIds.add(eId);
								else
									edgeIds.add(eId);
							}
						}
					}

					for (Long eId : e_rec1step.keySet()) {
						// get nodes for common edges
						Edge e = e_rec1step.get(eId);
						if (nodeIdSet.contains(e.getFromNode().getId())
								|| nodeIdSet.contains(e.getToNode().getId())) {
							if (edges_2.keySet().contains(eId)) {
								
								edgesMap.put(eId, e);
								if(twoSteps)
									hiddenEdgeIds.add(eId);
								else
									edgeIds.add(eId);
							}
						}
					}
					if(relation ==1){
						ur_depth=3;
						if(twoSteps)
						is_ur_hidden = true;
					}else{
						rt_depth=3;	
						if(twoSteps)
						is_rt_hidden = true;
					}
				}
			}

	
		
		if(relation ==1)
			return recNode.getId()+ "-" + ur_depth + "-" + is_ur_hidden;
			else
				return  "-" + rt_depth + "-" + is_rt_hidden; 
	}

	private Set<Long> processNodeBridge(
			Map<Long, Edge> edges_1, Map<Long, Edge> edges_2, int relation) {
		boolean oneBridgeRec = false;
		boolean oneBridgeRec1 = false;
		// get total nodes, hidden nodes

		Set<Long> nodes_fromEdges_1 = new HashSet<Long>();
		Set<Long> nodes_fromEdges_2 = new HashSet<Long>();

		// get nodes from Edges_1
		for (Long eId : edges_1.keySet()) {
			Edge e = edges_1.get(eId);
			
				oneBridgeRec1 = true;
			
				if(!stNodes.contains(e.getFromNode().getId()))
						
				nodes_fromEdges_1.add(e.getFromNode().getId());
				if( !stNodes.contains(e.getToNode().getId()))
					
				nodes_fromEdges_1.add(e.getToNode().getId());
				
		}// for
		// get nodes from Edges_2
		for (Long eId : edges_2.keySet()) {

			Edge e = edges_2.get(eId);
			if (oneBridgeRec1) {
				oneBridgeRec = true;
				nodes_fromEdges_2.add(e.getFromNode().getId());
				nodes_fromEdges_2.add(e.getToNode().getId());
			}
		} // for

		if (oneBridgeRec)
			nodes_fromEdges_1.retainAll(nodes_fromEdges_2);
			
		return nodes_fromEdges_1;
	}

	private void processEdgeForBridgeNodes(
			Map<Long, Edge> edges_1, Map<Long, Edge> edges_2,
			Set<Long> bridgeNodes, boolean directRec) {

		for (Long eId : edges_1.keySet()) {
			Edge e = edges_1.get(eId);
			Long fromNodeId = e.getFromNode().getId();
			Long toNodeId = e.getToNode().getId();

			if (bridgeNodes.contains(fromNodeId)
					|| bridgeNodes.contains(toNodeId)) {

					edgesMap.put(eId, e);
					if(directRec){
						hiddenEdgeIds.add(eId);
					}else{
						edgeIds.add(eId);
					}
			}

		}// for
		// get nodes from Edges_2
		for (Long eId : edges_2.keySet()) {
			Edge e = edges_2.get(eId);
			Long fromNodeId = e.getFromNode().getId();
			Long toNodeId = e.getToNode().getId();

			if (bridgeNodes.contains(fromNodeId)
					|| bridgeNodes.contains(toNodeId)) {
			
					edgesMap.put(eId, e);
					if(directRec){
						hiddenEdgeIds.add(eId);
					}else{
						edgeIds.add(eId);
					}
			}

		}// for

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
	public String getRecScores(){
		StringBuilder recStr = new StringBuilder();
		for (String idSeScore : idSeScores) {
			recStr.append(idSeScore + "||");
		}
		
		if (bioNodes != null) {
			
			for (Long nId : bioNodes) {
				recStr.append(nId + "||");
			}
		}
		//titleStr.append(finalScore + "||");
		return recStr.toString();
	}

	// remove ineligible egdes
	private Set<Long> removeEdges(Map<Long, Edge> edges, Set<Long> nodes) {
		Set<Long> removededges = new HashSet<Long>();
		// remove inelgible egdes
		for (Long eId : edges.keySet()) {

			Edge e = edges.get(eId);

			if (e.getFromNode().getId() != null) {
				if (!nodes.contains(e.getFromNode().getId())) {
					removededges.add(eId);
				}
			}

			if (e.getToNode().getId() != null && edges.keySet().contains(eId)) {
				if (!nodes.contains(e.getToNode().getId())) {
					removededges.add(eId);
				}
			}
		}
		return removededges;
	}
}