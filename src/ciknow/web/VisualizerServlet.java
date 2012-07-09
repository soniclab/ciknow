package ciknow.web;

import org.apache.log4j.Logger;
import org.dom4j.DocumentException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import ciknow.dao.NodeDao;
import ciknow.dao.VisualizationDao;
import ciknow.domain.*;
import ciknow.ro.GenericRO;
import ciknow.util.Beans;
import ciknow.util.GeneralUtil;
import ciknow.vis.NetworkExtractor;
import ciknow.vis.RecommenderNetwork;
import ciknow.io.AppletWriter;
import ciknow.io.DLWriter;
import ciknow.io.GraphmlWriter;
import edu.uci.ics.jung.graph.filters.impl.KNeighborhoodFilter;

import java.util.*;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: gyao
 * Date: Feb 4, 2008
 * Time: 8:32:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class VisualizerServlet extends BaseServlet {

	private static final long serialVersionUID = 4540159991051456788L;
	private static Logger logger = Logger.getLogger(VisualizerServlet.class);

	@SuppressWarnings({ "unchecked"})
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,IOException {		
		logger.info("************************** requesting for network visualization ...");			
		String loginNodeId = null; // for error msg recording
		try{
			loginNodeId = request.getParameter("loginNodeId");
			
			String visId = request.getParameter("visId");
			String viewType = request.getParameter("viewType");
			if (viewType == null || viewType.equals("")) viewType = "realtime";
			String snapShotName = request.getParameter("snapShotName");
			if (visId != null && visId.length() > 0){
				logger.info("Retrieving saved visualization (id=" + visId + ")");
				VisualizationDao visDao = (VisualizationDao) Beans.getBean("visualizationDao");
				Visualization vis = visDao.findById(Long.parseLong(visId));

				if (viewType.equals("snapshot")){					
					String filename = "vss_" + snapShotName + ".html";
					PrintWriter writer = new PrintWriter(request.getSession().getServletContext().getRealPath("/") + filename);
					writer.append(vis.getData());
					writer.flush();
					writer.close();
					response.sendRedirect(filename);
					return;
				} else {
					response.setContentType("text/html; charset=UTF-8");
					response.setCharacterEncoding("UTF-8");
					PrintWriter writer = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), "UTF8"));
					writer.append(vis.getData());
					writer.flush();
					return;
				}
			}
			
			String networkType = request.getParameter("networkType");
			String exportType = request.getParameter("exportType");
			if (exportType == null || exportType.length() == 0) exportType = "visual";
			String showIsolate = request.getParameter("isolate");
			String os = request.getParameter("os");
			String allowHugeNetwork = request.getParameter("allowHugeNetwork");
			
			// visual parameters
			String displayAttr = request.getParameter("displayAttr");
			String hideNodeLabel = request.getParameter("hideNodeLabel");
			if(request.getParameter("removeNodeLabel") != null){
				if(request.getParameter("removeNodeLabel").equals("1"))
				hideNodeLabel = "2";
			}
			if(request.getParameter("hideNodeLabel") == null){
				hideNodeLabel = "0";
			}
		    String mutualAsUndirected = request.getParameter("mutualAsUndirected");
			String groupQuestionId = request.getParameter("groupQuestion");
			String shapeQuestionId = request.getParameter("shapeQuestion");
			String colorQuestionId = request.getParameter("colorQuestion");
			String sizeQuestionId = request.getParameter("sizeQuestion");
			String sizeQuestionId2 = request.getParameter("sizeQuestion2");
	
			Map m = null;
			NetworkExtractor extractor = (NetworkExtractor) Beans.getBean("networkExtractor");
			Collection<Edge> edges = null;
			Collection<Node> nodes = null;
			String title = "Network";
			String pathStr = null;
			String maxRecSize = null;
			String minRecSize = null;
			Collection<Node> hiddenNodes = new HashSet<Node>();	
			Set<Long> hiddenEdges = new HashSet<Long>();
			List<Node> hiddenNodes1 = new ArrayList<Node>();
			Set<Long> hiddenEdges1 = new HashSet<Long>(); // only for attribute based recommnedation, edges 
			Set<Long> hiddenEdges2 = new HashSet<Long>();
			AppletWriter writer = new AppletWriter();
			NodeDao nodeDao = (NodeDao)Beans.getBean("nodeDao");
			
			if (networkType.equals("custom")){
				title = "Custom Network";
				String[] edgeTypes = request.getParameterValues("edgeType");
				if (edgeTypes == null) edgeTypes = new String[]{};
				String[] nodeFilters = request.getParameterValues("nodeFilter");
				String nfc = request.getParameter("nfc");
				String[] edgeFilters = request.getParameterValues("edgeFilter");
				String efc = request.getParameter("efc");
				String[] nodeAttributes = request.getParameterValues("nodeAttribute");
				if (nodeAttributes == null) nodeAttributes = new String[] {};		
				String attr_combineMethod = request.getParameter("nodeAttributeCombineMethod");
				String que_combineMethod = request.getParameter("questionCombineMethod");	
				String showRawRelation = (String) request.getParameter("showRawRelation");
				String operator = request.getParameter("operator");		
				
		        m = extractor.getCustomNetwork(Arrays.asList(edgeTypes), operator,
		        						nodeFilters==null?null:Arrays.asList(nodeFilters),
		        						nfc,
		        						edgeFilters==null?null:Arrays.asList(edgeFilters),
		        						efc,
		        						Arrays.asList(nodeAttributes), 
		        						attr_combineMethod, 
		        						que_combineMethod, 
		        						showIsolate, showRawRelation);
		        nodes = (Collection<Node>) m.get("nodes");
		        edges = (Collection<Edge>) m.get("edges");
			} else if (networkType.equals("local")){
				title = "Local Network";
			    String nodeId = request.getParameter("node_id");  	
			    String depth = request.getParameter("depth");
			    String includeDerivedEdges = request.getParameter("includeDerivedEdges");
				String[] edgeTypes = request.getParameterValues("edgeType");
				if (edgeTypes == null) edgeTypes = new String[]{};
				
				if (nodeId == null || nodeId.length() == 0) nodeId = "0";
				if (depth == null || depth.length() == 0) depth = "1";
				List<Long> rootIDs = new ArrayList<Long>();
				Long nid = Long.parseLong(nodeId);
				rootIDs.add(nid);
				m = extractor.getLocalNetwork(rootIDs, 
												Integer.parseInt(depth),
												includeDerivedEdges.equals("1"),
												false,
												KNeighborhoodFilter.IN_OUT, 
												Arrays.asList(edgeTypes));
		        Collection<Node> rawNodes = (Collection<Node>) m.get("nodes");	        
		        nodes = new LinkedList<Node>();	        
		    	for (Node node : rawNodes){
		    		nodes.add(nodeDao.loadById(node.getId()));
		    	}
		    	edges = (Collection<Edge>) m.get("edges");
		    	
		    	List<Node> focalNodes = new LinkedList<Node>();
		    	focalNodes.add(nodeDao.findById(nid));
		    	writer.setFocalNodes(focalNodes);    	
			} else if (networkType.equals("recommender")) {
				String sourceId = (String) request.getParameter("sourceId");
				String[] recIds = request.getParameterValues("recId");
				String[] seScores = request.getParameterValues("selectScore");
				String[] idScores = request.getParameterValues("identifyScore");
				String[] finalScores = request.getParameterValues("finalScore");
				String[] idMetricTypes = request.getParameterValues("idMetricType");
				String[] seMetricTypes = request.getParameterValues("seMetricType");
				String targetLabel = (String) request.getParameter("targetLabel");
				String targetName = (String) request.getParameter("targetName");
				String numRecs = request.getParameter("numRecs"); 
				String questionId = request.getParameter("questionId");
			
				RecommenderNetwork rn = new RecommenderNetwork(Long.parseLong(sourceId), recIds, seScores, idScores, finalScores, idMetricTypes, seMetricTypes,targetName, targetLabel, numRecs, questionId);
				
				nodes = new ArrayList<Node>();	
				nodes.addAll(rn.getNodeList());
					
				edges = rn.getEdges();
			
				hiddenEdges = rn.getHiddenEdgeIds();
				try{
				hiddenEdges1 = rn.getHiddenEdge1Ids();
				}catch(Exception e){
					
				}
				
				try{
					hiddenEdges2 = rn.getHiddenEdge2Ids();
					}catch(Exception e){
						
					}
					
				hiddenNodes = rn.getHiddenNodes();
				
				try{
					hiddenNodes1 = rn.getHiddenNodes1();
				}catch(Exception e){
						
				}
				
				title = rn.getTitleStr() + rn.getRecScores();
				pathStr = rn.getPathStr();
				maxRecSize = rn.getMaxRecSize();
				minRecSize = rn.getMinRecSize();
				writer.setSourceNode(rn.sourceNode);
				///if(questionId.equalsIgnoreCase("-1"))	
				writer.setRecNodes(rn.recNodes);
				
				writer.setTargetNode(rn.targetNode);
				
		}
			// present custom network to browser
			logger.info("number of nodes: " + nodes.size());
			logger.info("number of edges: " + edges.size());
			String numNodes = "5000";
			String numEdges = "5000";
			String hardlimit = "10000";
			Map limits = GeneralUtil.getLargeNetworkLimits();
			numNodes = (String)limits.get("nodes");
			numEdges = (String)limits.get("edges");
			hardlimit = (String)limits.get("hardlimit");
			
			
			if (nodes.size() == 0 || edges.size() == 0) {
				response.setContentType("text/html");
				error(response, "There is no valid data for Visualization. <br><br> Please try again!");
				return;
			} else if (exportType.equalsIgnoreCase("visual") && allowHugeNetwork != null && !allowHugeNetwork.equals("1") && 
					(nodes.size() > Integer.parseInt(numNodes) || edges.size() > Integer.parseInt(numEdges))){
				response.setContentType("text/html");
				error(response, "Selected network is (nodes=" + nodes.size() + ", edges=" + edges.size() + "). " +
						"<br>This visualization may take a considerable amount of time. " +
						"<br>Please limit the size of the network, or check the option of 'Allow large network'.");
				return;			
			} else if (exportType.equalsIgnoreCase("visual") && (nodes.size() + edges.size()) > Integer.parseInt(hardlimit)){
				response.setContentType("text/html");
				error(response, "Selected Network is too large to handle for visualization.");
				return;			
			}
			
			if (exportType.equalsIgnoreCase("graphml")){
				String filename = "graphml.xml";
		        response.setContentType("application/download");
		        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
		        
		        GraphmlWriter gmw = (GraphmlWriter)Beans.getBean("graphmlWriter");
		        gmw.write(nodes, edges, response.getOutputStream());
			} else if (exportType.equalsIgnoreCase("dl")){
				String labelEmbedded = request.getParameter("labelEmbedded");
				if (labelEmbedded == null || labelEmbedded.length() == 0) labelEmbedded = "1";
				if (showIsolate == null || showIsolate.length() == 0) showIsolate = "1";
				
				String filename = "dl.zip";
		        response.setContentType("application/octet-stream");
		        response.setHeader("Content-Disposition", "attachement; filename=\"" + filename + "\"");
		        DLWriter dlw = (DLWriter) Beans.getBean("dlWriter");
		        
		        // dl format always ignores isolate, e.g. only use nodes from edges
		        dlw.write(response.getOutputStream(), nodes, edges, showIsolate.equals("1"), labelEmbedded.equals("1"));
			} else {
				if (viewType.equals("snapshot")){
					String filename = "vss_" + snapShotName + ".html";
					FileOutputStream fos = new FileOutputStream(request.getSession().getServletContext().getRealPath("/") + filename);
					writer.write(fos , 
							title, pathStr, maxRecSize, minRecSize, nodes, edges, hiddenNodes, hiddenNodes1, hiddenEdges,hiddenEdges1,hiddenEdges2,
							colorQuestionId, shapeQuestionId, groupQuestionId, 
							sizeQuestionId, sizeQuestionId2, displayAttr, hideNodeLabel, mutualAsUndirected, os);
					fos.close();
					response.sendRedirect(filename);
				} else {
					response.setContentType("text/html; charset=UTF-8");
					response.setCharacterEncoding("UTF-8");
						
					writer.write(response.getOutputStream(), 
								title, pathStr, maxRecSize, minRecSize,nodes, edges, hiddenNodes, hiddenNodes1, hiddenEdges,hiddenEdges1,hiddenEdges2,
								colorQuestionId, shapeQuestionId, groupQuestionId, 
								sizeQuestionId, sizeQuestionId2, displayAttr, hideNodeLabel, mutualAsUndirected, os);
				}
			}
		} catch (Exception e){
        	logger.error(e.getMessage());
            e.printStackTrace();
            GenericRO genericRO = (GenericRO)Beans.getBean("genericRO");
            if (loginNodeId != null) genericRO.setErrorMsg(loginNodeId, e.getMessage());
            else {
            	logger.error("login nodeId should not be null.");
            }
            throw new ServletException(e.getMessage());
		}
	}

	public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	private void error(HttpServletResponse res, String msg) throws IOException{
		PrintWriter out = res.getWriter();
		logger.error(msg);
		out.println(msg);
		out.close();
	}
	
}
