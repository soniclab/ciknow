package ciknow.graph.metrics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.BronKerboschCliqueFinder;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import ciknow.domain.Edge;
import ciknow.domain.Node;
import ciknow.graph.converter.NumberEdgeValueImpl;
import ciknow.graph.converter.SparseGraphConverter;
import ciknow.ro.GenericRO;
import ciknow.security.CIKNOWUserDetails;
import ciknow.util.Beans;
import ciknow.util.Constants;
import ciknow.util.GeneralUtil;
import ciknow.util.PropsUtil;

import edu.uci.ics.jung.algorithms.importance.BetweennessCentrality;
import edu.uci.ics.jung.algorithms.importance.NodeRanking;
import edu.uci.ics.jung.algorithms.importance.PageRank;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraDistance;
import edu.uci.ics.jung.algorithms.shortestpath.Distance;
import edu.uci.ics.jung.algorithms.shortestpath.UnweightedShortestPath;
import edu.uci.ics.jung.algorithms.transformation.DirectionTransformer;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.impl.DirectedSparseGraph;
import edu.uci.ics.jung.statistics.DegreeDistributions;
import edu.uci.ics.jung.statistics.GraphStatistics;
import edu.uci.ics.jung.statistics.Histogram;

@SuppressWarnings({ "unchecked", "unused" })
public class NetworkAnalytics {	
	private static Log logger = LogFactory.getLog(NetworkAnalytics.class);
		
	public static void main(String[] args){		
		//Map m = NetworkGenerator.createStarGraph();
		//Map m = NetworkGenerator.createCircleGraph();
		Map m = NetworkGenerator.createLineGraph(true);
		NetworkMetrics nm = NetworkAnalytics.calculate((List<Node>)m.get("nodes"), (List<Edge>)m.get("edges"), null, 2, "or");
		logger.info(nm);
	}
	
	public static List<NetworkMetrics> getNetworkMetrics(Collection<Node> nodes, Collection<Edge> edges, Map<Long, Node> nodeMap, int type, String undirectedOperator) throws Exception{	
		logger.info("get network metrics...");
		List<Map<String, String>> eds = GeneralUtil.getEdgeDescriptions();
		List<NetworkMetrics> metrics = new ArrayList<NetworkMetrics>();
		NetworkMetrics nm;
		
		logger.debug("prepare edgeType to edges map");
		Map<String, List<Edge>> edgeMap = new HashMap<String, List<Edge>>();
		for (Edge edge : edges) {
			List<Edge> edgeList = edgeMap.get(edge.getType());
			if (edgeList == null) {
				edgeList = new ArrayList<Edge>();
				edgeMap.put(edge.getType(), edgeList);
			}
			edgeList.add(edge);
		}
		
		if (nodeMap == null || nodeMap.size() == 0) nodeMap = GeneralUtil.getNodeMap(nodes);	
		
		// overall network
		/*
		logger.info("#################################################");
		nm = NetworkAnalytics.calculate(nodes, edges, type);
		nm.setNetworkName("all");
		metrics.add(nm);
		logger.info("network [all] is calculated.");
		logger.info("#################################################");
		*/
		
		// network of certain edge type
		for (String edgeType : (Set<String>) edgeMap.keySet()) {
			List<Edge> edgeList = edgeMap.get(edgeType);
			Set<Node> nodeSet = new HashSet<Node>();
			for (Edge e : edgeList) {
				Long fid = e.getFromNode().getId();
				Node fromNode = nodeMap.get(fid);
				Long tid = e.getToNode().getId();
				Node toNode = nodeMap.get(tid);
				
				if (fromNode == null){
					logger.error(fid + " --> " + tid + ": from node is null");
					throw new Exception(fid + " --> " + tid + ": from node is null");
				}
				
				if (toNode == null){
					logger.error(fid + " --> " + tid + ": to node is null");
					throw new Exception(fid + " --> " + tid + ": to node is null");
				}
				
				nodeSet.add(fromNode);
				nodeSet.add(toNode);
			}

			logger.info("#################################################");
			nm = NetworkAnalytics.calculate(nodeSet, edgeList, nodeMap, type, undirectedOperator);
			
			String edgeLabel = GeneralUtil.getEdgeLabel(eds, edgeType);
			nm.setNetworkName(edgeLabel);
			metrics.add(nm);
			logger.info("network [type=" + edgeType + ", label=" + edgeLabel + "] is calculated.");
			logger.info("#################################################");
		}
		
		logger.info("get network metrics... done");
		return metrics;		
	}
	
	public NetworkAnalytics(){
		
	}
	
	/**
	 * 
	 * @param nodes list of nodes in the raw graph
	 * @param edges list of edges in the raw graph
	 * @param type 0=default, 1=directed, 2=undirected
	 * @return
	 */
	public static NetworkMetrics calculate(Collection<Node> nodes, Collection<Edge> edges, Map<Long, Node> nodeMap, int type, String undirectedOperator){
		logger.info("start calculating...");

		// prepare graph
		NetworkMetrics nm = new NetworkMetrics();
		Graph graph = prepareGraph(nodes, edges, type, undirectedOperator, nm);				
		if (graph == null) return nm;
		
		// prepare intermediate results
		if (nodeMap == null || nodeMap.size() == 0) nodeMap = GeneralUtil.getNodeMap(nodes);		
		Distance d = new UnweightedShortestPath(graph);	
		
		// calculate
		calculateTotalNodes(graph, nm);
		calculateTotalEdges(graph, nm);
		calculateNetworkDensity(graph, nm);
		calculateDegree(graph, nm);
		calculateDiameter(graph, d, nm); // this is used in closeness calculation
		calculateCloseness(graph, d, nm);
		calculateBetweenness(graph, nm);
		calculateClusteringCoefficient(graph, nm);		
		calculateCharacteristicsPathLength(graph, d, nm);		
		calculateScanning(graph, nm);		
		calculatePageRank(DirectionTransformer.toDirected(graph, false), nm);		
		calculatePowerLaw(graph, nm);
		
		SimpleGraph<Long, DefaultEdge> jg = SparseGraphConverter.CIKNOW2JGRAPHT_SIMPLE(nodes, edges);
		calculateMaximalCliques(jg, nm, nodeMap);
		calculateConnectedComponents(jg, nm, nodeMap);

		//logger.debug("there are " + nm.getIndividualMetrics().size() + " individual metrics.");
		logger.info("networkMetrics is directed: " + nm.getDirected());
		logger.info("end calculation.");
		return nm;
	}
	
	// assumption: the input network is NOT mixed network!
	public static Graph prepareGraph(Collection<Node> nodes, Collection<Edge> edges, int type, String undirectedOperator, NetworkMetrics nm){
		logger.info("preparing graph...");

		logger.debug("nodes: " + nodes.size() + " edges: " + edges.size() + " type: " + type);
		if (nodes == null || nodes.size() == 0 || edges == null || edges.size() == 0){
			logger.warn("empty network.");
			return null;
		}	
		
		boolean directed = edges.iterator().next().isDirected();
		Graph graph = null;
		if (directed && (type == 1 || type == 0)) {
			graph = SparseGraphConverter.CIKNOW2JUNG(nodes, edges, 1, false);
			nm.setDirected(true);
		}
		else if (!directed && (type == 2 || type == 0)) {
			graph = SparseGraphConverter.CIKNOW2JUNG(nodes, edges, 2, false);
			nm.setDirected(false);
		}
		else if (directed && type == 2) {
			logger.debug("undirectedOperator: " + undirectedOperator);
			if (undirectedOperator.equalsIgnoreCase("or")){
				graph = SparseGraphConverter.CIKNOW2JUNG(nodes, edges, 1, false);
				graph = DirectionTransformer.toUndirected(graph, false);
			} else {
//				List<Edge> edgeList = new ArrayList<Edge>(edges);
//				List<Edge> tempEdges = new LinkedList<Edge>();
//				List<Edge> undirectedEdges = new LinkedList<Edge>();
//				outer:
//				for (int i=0; i<edgeList.size(); i++){
//					Edge e1 = edgeList.get(i);
//					if (tempEdges.contains(e1)) continue;
//					for (int j=i+1; j<edgeList.size(); j++){				
//						Edge e2 = edgeList.get(j);
//						if (tempEdges.contains(e2)) continue;
//						Edge e3 = e1.merge(e2);
//						if (e3 != null){
//							//logger.debug("merged edge: " + e3);
//							undirectedEdges.add(e3);
//							tempEdges.add(e1);
//							tempEdges.add(e2);
//							continue outer;
//						}
//					}
//				}
//				
//				Set<Node> nodeSet = new HashSet<Node>();
//				for (Edge edge : undirectedEdges){
//					nodeSet.add(edge.getFromNode());
//					nodeSet.add(edge.getToNode());
//				}
				
				List<Edge> mergedEdges = Edge.merge(edges);
				Set<Node> nodeSet = new HashSet<Node>();
				for (Edge edge : mergedEdges){
					nodeSet.add(edge.getFromNode());
					nodeSet.add(edge.getToNode());
				}
				
				graph = SparseGraphConverter.CIKNOW2JUNG(nodeSet, mergedEdges, 2, false);
			}
			nm.setDirected(false);
		} else if (!directed && type == 1){
			graph = SparseGraphConverter.CIKNOW2JUNG(nodes, edges, 2, false);
			graph = DirectionTransformer.toDirected(graph, false);
			nm.setDirected(true);
		}
		
		/*
		nm.setDirected(directed);
		logger.info("original graph is considered directed: " + directed);
		Graph rawGraph = null;
		if (directed) rawGraph = SparseGraphConverter.CIKNOW2JUNG(nodes, edges, 1, false);
		else rawGraph = SparseGraphConverter.CIKNOW2JUNG(nodes, edges, 2, false);
		
		// customized by user
		
		if (type == 0) graph = rawGraph;
		else if (type == 1) {
			if (!directed){
				graph = DirectionTransformer.toDirected(rawGraph, false);
			} else graph = rawGraph;
			nm.setDirected(true);
		} else if (type == 2){
			if (directed){
				graph = DirectionTransformer.toUndirected(rawGraph, false);
			} else graph = rawGraph;
			nm.setDirected(false);
		}
		*/
		
		return graph;
	}
	
	public static void calculateTotalNodes(Graph graph, NetworkMetrics nm){
		logger.info("calculate total nodes...");
		nm.setTotalNodes(graph.numVertices());
	}
	
	public static void calculateTotalEdges(Graph graph, NetworkMetrics nm){
		logger.info("calculate total edges...");
		nm.setTotalEdges(graph.numEdges());
	}
	
	/**
	 * The proportion of ties in a network relative to the total number possible.
	 * 
	 * Note that we consider that a tie exists from A to B if there is at least one edge from A to B.
	 * We dont take into consideration that there can be more than one edges from A to B
	 * 
	 * (number of observed ties) / (numNodes*(numNodes-1))
	 * 
	 * O(n^2)
	 *
	 */	
	public static void calculateNetworkDensity(Graph graph, NetworkMetrics nm){
		logger.info("calculate network density...");
		if (graph == null) return;
		int numVert = graph.numVertices();
		// maximum number of possible ties in a directed graph
		int maxNumEdge = numVert*(numVert - 1);
		
		int numEdge = 0;
		Set<Vertex> vertices = graph.getVertices();
		for (Vertex from : vertices){
			for (Vertex to : vertices){
				if (from == to) continue;
				if (from.findEdge(to) != null) numEdge++;
			}			
		}
		nm.setNetworkDensity(numEdge/(double)maxNumEdge);
	}
	
	public static void calculateDegree(Graph graph, NetworkMetrics nm){
		logger.info("calculate degree...");
		boolean directed = nm.getDirected();
		int maxIn = 0;
		int sumIn = 0;
		int maxOut = 0;
		int sumOut = 0;
		int n = graph.numVertices();
		
		// individual metrics
		for (Vertex v : (Set<Vertex>)graph.getVertices()){
			Long nodeId = (Long)v.getUserDatum(Constants.NODE_ID);
			if (directed){
				int inDegree = v.inDegree();
				nm.setInDegree(nodeId, inDegree);
				if (maxIn < inDegree) maxIn = inDegree;
				
				int outDegree = v.outDegree();
				nm.setOutDegree(nodeId, outDegree);
				if (maxOut < outDegree) maxOut = outDegree;
			} else {
				int degree = v.degree();
				nm.setOutDegree(nodeId, degree);
				if (maxOut < degree) maxOut = degree;
			}
		}
		
		// network centralization
		for (Vertex v : (Set<Vertex>)graph.getVertices()){
			Long nodeId = (Long)v.getUserDatum(Constants.NODE_ID);
			if (directed){
				sumIn += (maxIn - nm.getInDegree(nodeId));
				sumOut += (maxOut - nm.getOutDegree(nodeId));
			} else {
				sumOut += (maxOut - nm.getOutDegree(nodeId));
			}
		}
		
		if (directed){
			nm.setNetworkInDegree(sumIn/((double)(n-1)*(n-1)));
			nm.setNetworkOutDegree(sumOut/((double)(n-1)*(n-1)));
		} else {
			nm.setNetworkOutDegree(sumOut/((double)(n-1)*(n-2)));
		}	
	}
	
	/**
	 * Calculate closeness based on the red book, Chapter 5.
	 * 
	 * Social Network Analysis: Methods and Applications
	 * Stanley Wasserman and Katherine Faust
	 * 
	 * Closeness is defined for undirected graph, but we also generalize
	 * to directed graph as described in the book above. The book
	 * doesn't define network centralization for directed graph, so 
	 * the same formula is used for directed graph as undirected graph.
	 * So closeness on directed graph is meaningless. (book, pg. 200)
	 * @param graph
	 * @param d
	 * @param nm
	 */
	public static void calculateCloseness(Graph graph, Distance d, NetworkMetrics nm){
		logger.info("calculate closeness...");
		boolean directed = nm.getDirected();
		double inCloseness = 0;
		double maxIn = 0;
		double sumIn = 0;
		double outCloseness = 0;
		double maxOut = 0;
		double sumOut = 0;
		int n = graph.numVertices();
		logger.debug("there are " + n + " vertices.");
		
		// individual metrics
		for (Vertex v : (Set<Vertex>)graph.getVertices()){			
			Long nodeId = (Long)v.getUserDatum(Constants.NODE_ID);
			logger.debug("------------- Vertice: " + nodeId);
			
			sumIn = 0;
			sumOut = 0;
			int numUnreachableOut = 0;
			int numUnreachableIn = 0;
			if (directed){
				for (Vertex endpoint : (Set<Vertex>)graph.getVertices()){
					Long endpointId = (Long)endpoint.getUserDatum(Constants.NODE_ID);
					if (endpoint == v) continue;
					
					// out
					Number dist = d.getDistance(v, endpoint);
					if (dist == null) {
						numUnreachableOut++;						
					} else {
						sumOut += dist.doubleValue();
					}
					
					// in
					dist = d.getDistance(endpoint, v);
					if (dist == null) {
						numUnreachableIn++;					
					} else {
						sumIn += dist.doubleValue();
					}
				}
				if (numUnreachableOut == n-1) sumOut = Double.POSITIVE_INFINITY;
				else sumOut += (numUnreachableOut * (nm.getDiameter() + 1));

				if (numUnreachableIn == n-1) sumIn = Double.POSITIVE_INFINITY;
				else sumIn += (numUnreachableIn * (nm.getDiameter() + 1));
				
				outCloseness = (n-1)/sumOut;
				//logger.debug("sumOut: " + sumOut + " outCloseness: " + outCloseness);				
				nm.setOutCloseness(nodeId, outCloseness);
				if (outCloseness > maxOut) maxOut = outCloseness;	
				
				inCloseness = (n-1)/sumIn;
				//logger.debug("sumIn: " + sumIn + " inCloseness: " + inCloseness);	
				nm.setInCloseness(nodeId, inCloseness);
				if (inCloseness > maxIn) maxIn = inCloseness;
			} else {
				for (Vertex endpoint : (Set<Vertex>)graph.getVertices()){
					Long endpointId = (Long)endpoint.getUserDatum(Constants.NODE_ID);
					if (endpoint == v) continue;
					
					Number dist = d.getDistance(v, endpoint);
					if (dist == null) {
						numUnreachableOut++;						
					} else {
						sumOut += dist.doubleValue();
					}
				}
				if (numUnreachableOut == n-1) sumOut = Double.POSITIVE_INFINITY;
				else sumOut += (numUnreachableOut * (nm.getDiameter() + 1));
				
				outCloseness = (n-1)/sumOut;
				//logger.debug("sumOut: " + sumOut + " outCloseness: " + outCloseness);	
				nm.setOutCloseness(nodeId, outCloseness);
				if (outCloseness > maxOut) maxOut = outCloseness;
			}			
		}
		
		// network centralization
		sumIn = 0;
		sumOut = 0;
		logger.debug("maxIn: " + maxIn);
		logger.debug("maxOut: " + maxOut);
		for (Vertex v : (Set<Vertex>)graph.getVertices()){
			Long nodeId = (Long)v.getUserDatum(Constants.NODE_ID);
			//logger.debug("nodeId: " + nodeId + " incloseness: " + nm.getInCloseness(nodeId));
			//logger.debug("nodeId: " + nodeId + " outcloseness: " + nm.getOutCloseness(nodeId));
			if (directed){
				sumIn += (maxIn - nm.getInCloseness(nodeId));
				sumOut += (maxOut - nm.getOutCloseness(nodeId));
			} else {
				sumOut += (maxOut - nm.getOutCloseness(nodeId));
			}
		}
		logger.debug("sumIn: " + sumIn);
		logger.debug("sumOut: " + sumOut);
		
		if (directed){
			double nic= sumIn/((double)(n-1)*(n-2)/(2*n-3));
			nm.setNetworkInCloseness(nic);
			logger.debug("network in closeness: " + nic);
			
			double noc = sumOut/((double)(n-1)*(n-2)/(2*n-3));
			nm.setNetworkOutCloseness(noc);
			logger.debug("network out closeness: " + noc);
		} else {
			double nc = sumOut/((double)(n-1)*(n-2)/(2*n-3));
			nm.setNetworkOutCloseness(nc);
			logger.debug("network closeness: " + nc);
		}	
	}
	
	public static void calculateBetweenness(Graph graph, NetworkMetrics nm){
		logger.info("calculate betweenness...");
		if (graph == null) return;
		
		BetweennessCentrality bc = new BetweennessCentrality(graph);
		bc.setRemoveRankScoresOnFinalize(false);
		bc.evaluate();
		
		// individual
		int numVert = graph.numVertices();
		int normalizer = ((numVert - 1) * (numVert - 2));
		double max = 0;
		Set<Vertex> vertices = graph.getVertices();
		for (Vertex v : vertices){
			Long nodeId = (Long) v.getUserDatum(Constants.NODE_ID);
			double rankScore = bc.getRankScore(v);
			//logger.debug("betweeness for " + nodeId + ": " + rankScore);
			double normalizedScore = rankScore/normalizer;
			//if (nm.getDirected()) normalizedScore = normalizedScore/2;
			nm.setBetweenness(nodeId, normalizedScore);
			
			if (normalizedScore > max) max = normalizedScore;
		}
		
		// network centralization
		double sum = 0;
/*		for (IndividualMetric im : nm.getIndividualMetrics()){
			sum += max - im.betweenness;
		}*/
		for (Long nodeId : nm.getIndividualMetricMap().keySet()){
			IndividualMetric im = nm.getIndividualMetricMap().get(nodeId);
			sum += max - im.betweenness;
		}
		
		nm.setNetworkBetweenness(sum / (numVert -1));
	}
	
	/*
	 * reference JUNG GraphStatistics.clusteringCoefficients()
	 */
	public static void calculateClusteringCoefficient(Graph graph, NetworkMetrics nm){
		logger.info("calculate clustering coefficient...");
		Map map = GraphStatistics.clusteringCoefficients(graph);
		double sum = 0;
		for (Vertex v : (Set<Vertex>) map.keySet()){
			double cc = (Double) map.get(v);
			Long nodeId = (Long)v.getUserDatum(Constants.NODE_ID);
			nm.setClusteringCoefficient(nodeId, cc);
			
			sum += cc;
		}
		nm.setNetworkClusteringCoefficient(sum/graph.numVertices());
	}
	
	/*
	 * reference JUNG GraphStatistics.diameter()
	 */
	public static void calculateDiameter(Graph graph, Distance dd, NetworkMetrics nm){
		logger.info("calculate diameter...");
		nm.setDiameter(GraphStatistics.diameter(graph, dd, true));
	}
	
	/*
	 * average shortest path
	 */
	public static void calculateCharacteristicsPathLength(Graph graph, Distance dd, NetworkMetrics nm){
		logger.info("calculate characteristicsPathLength...");
		double sum = 0;
		int count = 0;
		Set<Vertex> vertices = graph.getVertices();
		for (Vertex v : vertices){
			Map map = dd.getDistanceMap(v);
			for (Vertex vv : (Set<Vertex>)map.keySet()){
				if (v == vv) continue;
				Number n = (Number) map.get(vv);
				sum += n.doubleValue();
				count++;
			}
		}
		nm.setCharacteristicPathLength(sum/count);
	}
	
	public static void calculatePageRank(DirectedGraph graph, NetworkMetrics nm){
		logger.info("calculate page rank...");
		if (graph == null) return;
		
		PageRank pr = new PageRank(graph, 0.15);
		pr.evaluate();
				
		List<NodeRanking> rankings = pr.getRankings();
		double max = rankings.get(0).rankScore;
		for (NodeRanking ranking : rankings){
			Vertex v = ranking.vertex;
			Long nodeId = (Long)v.getUserDatum(Constants.NODE_ID);
			double rankScore = ranking.rankScore;
			nm.setPageRank(nodeId, rankScore/max);
		}	
	}
	
	/** 
	 * Measures how many nodes of different types one is connected to.
	 * Standardized to the individual with the maximum scanning.
	 *
	 */	
	public static void calculateScanning(Graph graph, NetworkMetrics nm){
		logger.info("calculate scanning...");
		if (graph == null) return;
		int max = 0;
		Set<Vertex> vertices = graph.getVertices();
		for (Vertex v : vertices){
			Long nodeId = (Long) v.getUserDatum(Constants.NODE_ID);
			String type = (String)v.getUserDatum(Constants.NODE_TYPE);
			Set<Vertex> neighbors = v.getNeighbors();
			
			int count = 0;
			for (Vertex neighbor : neighbors){
				String neighborType = (String)neighbor.getUserDatum(Constants.NODE_TYPE);
				if (!type.equals(neighborType)) count++;				
			}
			nm.setScanning(nodeId, new Double(count));
			if (count > max) max = count;
		}
		
		if (max == 0) return;
		
/*		for (IndividualMetric im : nm.getIndividualMetrics()){
			im.scanning = im.scanning/max;
		}*/
		
		for (Long nodeId : nm.getIndividualMetricMap().keySet()){
			IndividualMetric im = nm.getIndividualMetricMap().get(nodeId);
			im.scanning = im.scanning/max;
		}
	}

	
	public static void calculatePowerLaw(Graph graph, NetworkMetrics nm){
		logger.info("caculate power law...");
		Set<Vertex> vertices = graph.getVertices();

		// find max degree
		int maxIn = 0;
		int maxOut = 0;
		for (Vertex v : vertices){
			int d;
			if (nm.getDirected()){
				d = v.inDegree();
				if (maxIn < d) maxIn = d;
				d = v.outDegree();
				if (maxOut < d) maxOut = d;
			} else {
				d = v.degree();
				if (maxOut < d) maxOut = d ;
			}
		}
		
		// create histogram
		int[] inCounter = new int[maxIn + 1];
		int[] outCounter = new int[maxOut + 1];
		for (Vertex v : vertices){
			int d;
			if (nm.getDirected()){
				d = v.inDegree();
				inCounter[d] += 1;
				d = v.outDegree();
				outCounter[d] += 1;
			} else {
				d = v.degree();
				outCounter[d] += 1;
			}
		}
		
		//
		Map m;
		if (nm.getDirected()){
			m = calculatePowerLaw(inCounter);
			nm.setInAlpha((Double)m.get("alpha"));
			nm.setInBeta((Double)m.get("beta"));
			nm.setInCounts((List<Double>)m.get("counts"));
			
			m = calculatePowerLaw(outCounter);
			nm.setOutAlpha((Double)m.get("alpha"));
			nm.setOutBeta((Double)m.get("beta"));
			nm.setOutCounts((List<Double>)m.get("counts"));
		} else {
			m = calculatePowerLaw(outCounter);
			nm.setOutAlpha((Double)m.get("alpha"));
			nm.setOutBeta((Double)m.get("beta"));
			nm.setOutCounts((List<Double>)m.get("counts"));
		}
	}
	
	private static Map calculatePowerLaw(int[] counter){
		int n = counter.length;
		List<Double> counts = new ArrayList<Double>();
		double[] x = new double[n];
		double[] y = new double[n];
		double sumx = 0;
		double sumy = 0;
		for (int i=0; i<n; i++){
			double count = counter[i];
			//logger.debug("degree: " + i + " count: " + count);
			counts.add(count);
			if (i==0) continue;
			
			x[i] = Math.log(i);
			y[i] = Math.log(count == 0? 0.1:count);
			
			sumx += x[i];
			sumy += y[i];
		}
		
		double meanx = sumx/(n-1);
		double meany = sumy/(n-1);
		
		double cumulatorx = 0;
		double cumulatory = 0;
		for (int i=1; i<n; i++){
			cumulatorx += Math.pow((x[i] - meanx), 2);
			cumulatory += Math.pow((y[i] - meany), 2);
		}
		
		double beta = (Math.sqrt(cumulatory/cumulatorx));
		double alpha = meany - beta*meanx;
		
		Map m = new HashMap();
		m.put("counts", counts);
		m.put("alpha", alpha);
		m.put("beta", beta);
		return m;
	}
	
	public static void calculateMaximalCliques(SimpleGraph jg, NetworkMetrics nm, Map<Long, Node> nodeMap){
		logger.info("caculate maximal cliques...");		
		BronKerboschCliqueFinder<Long, DefaultEdge> cliqueFinder = new BronKerboschCliqueFinder<Long, DefaultEdge>(jg);
		Collection<Set<Long>> rawCliques = cliqueFinder.getAllMaximalCliques();
		List<Set<Long>> sortedCliques = sort(rawCliques);
		for (Set<Long> c : sortedCliques){
			Clique clique = new Clique();
			clique.nodes = new ArrayList<Long>(c);
			
			// prepare clique label
			StringBuffer sb = new StringBuffer();
			for (int i=0; i<clique.nodes.size(); i++){
				Long nodeId = clique.nodes.get(i);
				if (i!=0) sb.append(", ");
				sb.append(nodeMap.get(nodeId).getLabel());
			}			
			clique.label = sb.toString();			
			nm.getCliques().add(clique);
		}
	}
	
	public static void calculateConnectedComponents(SimpleGraph jg, NetworkMetrics nm, Map<Long, Node> nodeMap){
		logger.info("caculate connected components ...");		
		ConnectivityInspector<Long, DefaultEdge> componentFinder = new ConnectivityInspector<Long, DefaultEdge>(jg);
		Collection<Set<Long>> rawComponents = componentFinder.connectedSets();
		List<Set<Long>> sortedComponents = sort(rawComponents);
		for (Set<Long> c : sortedComponents){
			ConnectedComponent component = new ConnectedComponent();
			component.nodes = new ArrayList<Long>(c);
			
			// prepare component label
			StringBuffer sb = new StringBuffer();
			for (int i=0; i<component.nodes.size(); i++){
				if (i!=0) sb.append(", ");
				sb.append(nodeMap.get(component.nodes.get(i)).getLabel());
				//sb.append(component.nodes.get(i));
			}
			component.label = sb.toString();
			
			nm.getConnectedComponents().add(component);
		}
	}
	
	private static List<Set<Long>> sort(Collection<Set<Long>> raw){
		List<Set<Long>> sorted = new ArrayList<Set<Long>>();
		Map<Integer, List<Set<Long>>> map = new TreeMap<Integer, List<Set<Long>>>();
		
		for (Set<Long> set : raw){
			List<Set<Long>> list = map.get(set.size());
			if (list == null){
				list = new ArrayList<Set<Long>>();
				map.put(set.size(), list);
			}
			list.add(set);			
		}
		
		for (Integer size : map.keySet()){
			sorted.addAll(map.get(size));
		}
		
		return sorted;
	}
}
