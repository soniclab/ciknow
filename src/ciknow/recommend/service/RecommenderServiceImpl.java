package ciknow.recommend.service;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import ciknow.dao.EdgeDao;
import ciknow.dao.MetricDao;
import ciknow.dao.NodeDao;
import ciknow.dao.QuestionDao;
import ciknow.util.Beans;
import ciknow.util.Constants;
import ciknow.util.GeneralUtil;
import ciknow.domain.Edge;
import ciknow.domain.Field;
import ciknow.domain.Metric;
import ciknow.domain.Question;
import ciknow.domain.Recommendation;
import ciknow.domain.Node;
import ciknow.graph.converter.SparseGraphConverter;
import ciknow.recommend.AbstractRecommender;
import ciknow.recommend.GenericRecommender;
import ciknow.recommend.RecommendationComparator;
import ciknow.recommend.Recommender;
import ciknow.recommend.calculate.CalculateUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraDistance;
import edu.uci.ics.jung.algorithms.transformation.DirectionTransformer;
import edu.uci.ics.jung.graph.ArchetypeEdge;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.decorators.NumberEdgeValue;
import edu.uci.ics.jung.utils.UserData;

import javax.jws.WebService;

/**
 * User: gyao  
 * Date: March 27, 2008
 * Time: 11:04:31 PM
 */

@WebService(endpointInterface="ciknow.recommend.service.RecommenderService")
public class RecommenderServiceImpl implements RecommenderService{
    private static Log logger = LogFactory.getLog(RecommenderServiceImpl.class);
    private NodeDao nodeDao;
    private EdgeDao edgeDao;
    private MetricDao metricDao;
    private QuestionDao questionDao;

    public static void main(String[] args) throws Exception {   
    	Map<String, String> map = new HashMap<String, String>();
    	map.put("source", "identification");
    	//map.put("row", "a");
    	//map.put("col", "b");
    	new RecommenderServiceImpl().computeRec(map);
    	if (true) return;
    	
        Beans.init();
        RecommenderService service = (RecommenderService) Beans.getBean("recommenderServiceImpl");
        List<String> targets = new ArrayList<String>();
        targets.add("James  Bussel");
        Map input = new HashMap();
        input.put("nodeId", "2");
        input.put("keywords", targets);
        input.put("numRecs", "50");
        input.put("operator", "and");
        input.put("questionId", "-1");
        service.getRecommendationDTOs(input);
    }

    public RecommenderServiceImpl(){
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

	public MetricDao getMetricDao() {
		return metricDao;
	}

	public void setMetricDao(MetricDao metricDao) {
		this.metricDao = metricDao;
	}

	public QuestionDao getQuestionDao() {
		return questionDao;
	}

	public void setQuestionDao(QuestionDao questionDao) {
		this.questionDao = questionDao;
	}

	public List<Recommendation> getRecommendations(Long nodeId,
			List<String> keywords, int numRecs, String operator, String questionId) {
		List<Recommendation> recs = new ArrayList<Recommendation>();		
		try {
			logger.debug("get user");
			Node node = nodeDao.loadById(nodeId);
			if (node == null) {
				logger.error("node with id " + nodeId + " is not found.");
				return recs;
			}
			
			logger.debug("user: " + node.getLabel());
			logger.debug("keywords: " + keywords);
			logger.debug("operator: " + operator);
			logger.debug("numRecs: " + numRecs);
			logger.debug("questionId: " + questionId);
			
			if (questionId.equals("-1")){
				logger.info("search by node label__________________");
				logger.debug("get targets");
				List<Node> targets = new ArrayList<Node>();
				for (String keyword : keywords) {
					keyword = keyword.trim();
					if (keyword == null || keyword.length() == 0) {
						logger.info("keyword (" + keyword
								+ ") is illegal and ignored.");
						continue;
					}

					List<Node> nodes = nodeDao.findByLabel(keyword);
					if (nodes == null || nodes.size() == 0) {
						logger.warn("keyword " + keyword + " is not found.");
						continue;
					}
					targets.add(nodes.get(0));
				}
				if (targets.size() == 0) {
					logger.error("no targets, not recommendations for u.");
					return recs;
				}
				
				logger.debug("get recommender implementation");
				Recommender recommender = null;
				String filename = "recconfig.xml";
				Element config = null;
				Document doc = GeneralUtil.readXMLFromClasspath(filename);
				config = doc.getRootElement();
				String algorithm = config.element("alg").attributeValue("name");
				if (algorithm == null) algorithm = "ciknow.recommend.GenericRecommender";
				logger.info("Creating recommender: " + algorithm);
				Class clazz = Class.forName(algorithm);
				recommender = (Recommender) clazz.newInstance();
	
				logger.debug("make recommendations");
				// the CXF webservices cannot map NodeAttribute, so don't use any of them
				node.setAttributes(null);
				node.setLongAttributes(null);
				int op;
				if (operator.equals("or"))
					op = Constants.OR;
				else
					op = Constants.AND;
				recs = recommender.getRecommendations(node, targets, numRecs, op);
			} else {
				logger.info("search by field label__________________");
				Long qid = Long.parseLong(questionId);
				Question q = questionDao.findById(qid);
				if (q == null) return recs;
//				Field f = q.getFieldByLabel(keywords.get(0));
//				if (f == null) return recs;
//				String attrName = q.makeFieldKey(f);
				String attrName = keywords.get(0);
				recs = getRecommendationsByAttribute(node, attrName, numRecs);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		return recs;
	}

    public List<Map> getRecommendationDTOs(Map input) {
    	logger.info("getRecommendationDTOs...");
    	Long nodeId = Long.parseLong((String)input.get("nodeId"));
    	List<String> keywords = (List<String>) input.get("keywords");
    	int numRecs = Integer.parseInt((String) input.get("numRecs"));
    	String operator = (String) input.get("operator");
    	String questionId = (String)input.get("questionId");

        List<Recommendation> recs = getRecommendations(nodeId, keywords, numRecs, operator, questionId);
        List<Map> dtos = new ArrayList<Map>();
        
        for (Recommendation rec : recs){
        	//logger.debug(rec);
        	
        	Map m = new HashMap();
        	m.put("nodeId", rec.getUser().getId().toString());
        	m.put("recId", rec.getTarget().getId().toString());
        	m.put("type", rec.getTarget().getType());
        	m.put("label", rec.getTarget().getLabel());
        	//m.put("identifyScore", new Formatter().format("%1.4f", rec.getIdentifyScore().doubleValue()));
        	m.put("identifyScore", rec.getIdentifyScore().doubleValue());
        	m.put("idMetricType", rec.getIdMetricType());
        	m.put("selectScore", rec.getSelectScore().doubleValue());
        	m.put("seMetricType", rec.getSeMetricType());
        	m.put("finalScore", rec.getFinalScore().doubleValue());
        	m.put("questionId", questionId);
        	
            dtos.add(m);
            //logger.debug(m);      
        }
        
        logger.info("got " + dtos.size() + " DTOs");
        return dtos;
    }

	public List<Recommendation> getRecommendationsByAttribute(Node node, String attrName, int numRecs) {
		logger.info("get recommendations by attribute: " + attrName);
		Map<Long, Metric> seScoreMap = GenericRecommender.getScoreMap(node, "selection");
		Map<Long, Float> idScoreMap = nodeDao.getScoreMapByAttrName(attrName);
		logger.debug("candidate for id: " + idScoreMap.size() + ", se: " + seScoreMap.size());
		List<Recommendation> recs = new LinkedList<Recommendation>();
		for (Long nodeId : idScoreMap.keySet()){
			Metric seMetric = seScoreMap.get(nodeId);
			if (seMetric == null) continue;
			Float idScore = idScoreMap.get(nodeId);
			String seMetricType = seMetric.getType();
			Float seScore = seMetric.getValue();
			if (seMetricType.equals(Constants.ALG_SP) || seMetricType.equals(Constants.ALG_EUCLIDEAN) || seMetricType.equals(Constants.ALG_SEUCLIDEAN)){
				seScore = 1/seScore;
			}
			
			Recommendation rec = new Recommendation();
			rec.setUser(node);
			rec.setTarget(nodeDao.findById(nodeId));
			rec.setIdentifyScore(idScore.doubleValue());
			rec.setIdMetricType(attrName);
			rec.setSelectScore(seScore.doubleValue());
			rec.setSeMetricType(seMetricType);
			rec.setFinalScore(idScore.doubleValue()*seScore.doubleValue());
			recs.add(rec);
		}		
		logger.info("there are " + recs.size() + " raw recommendations.");

		logger.debug("rescoring/filtering...");
		AbstractRecommender.compact(recs, 0.0000000001f);

		logger.debug("sort, truncate, normalize...");
		Map<String, List<Recommendation>> recMap = new HashMap<String, List<Recommendation>>();
		for (Recommendation rec : recs){
			String type = rec.getTarget().getType();
			List<Recommendation> list = recMap.get(type);
			if (list == null){
				list = new ArrayList<Recommendation>();				
				recMap.put(type, list);
			}
			list.add(rec);
		}		
		recs.clear();
		for (String type:recMap.keySet()){
			List<Recommendation> list = recMap.get(type); 
			Collections.sort(list, new RecommendationComparator());
			recs.addAll(list.size()>=numRecs?list.subList(0, numRecs):list);
		}

		logger.debug("got " + recs.size() + " recommendations.");
		return recs;
	}
	
    public String getRecConfig(Map map) throws IOException{
    	String filename = "recconfig.xml";    	
    	URL url = Thread.currentThread().getContextClassLoader().getResource(filename);
    	File file = new File(url.getFile());    	
    	BufferedReader reader = new BufferedReader(new FileReader(file));
    	StringBuilder sb = new StringBuilder();
    	String line = reader.readLine();
    	while(line != null) {
    		sb.append(line).append("\n");
    		line = reader.readLine();
    	}
    	reader.close();
    	return sb.toString();
    }
    
    public void updateRecConfig(Map config) throws IOException{
    	logger.info("update recommender configuration...");
    	String filename = "recconfig.xml";    	
    	URL url = Thread.currentThread().getContextClassLoader().getResource(filename);
    	File file = new File(url.getFile());    	
    	PrintWriter writer = new PrintWriter(file);
    	String xml = (String) config.get("config");
    	writer.print(xml);
    	writer.close();
    	logger.info("update recommender configuration... done");
    }
    
    public String computeRec(Map map) throws Exception{
    	String xml = (String) map.get("config");
    	if (xml != null){
    		updateRecConfig(map);
    	}
    	
    	logger.info("computing recommendations...");
    	String row = (String) map.get("row");
    	String col = (String) map.get("col");
    	String source = (String) map.get("source");
    	String dirtyonly = (String) map.get("dirtyonly");
    	String msg;
    	
    	Document doc = GeneralUtil.readXMLFromClasspath("recconfig.xml");
    	Element root = doc.getRootElement();
    	Element sourceElement = root.element(source);    
    	Integer numMetrics = 0;
    	if (row != null && col != null) {
    		for (Element pair : (List<Element>)sourceElement.elements()){
    			if (pair.attributeValue("row").equals(row) && pair.attributeValue("col").equals(col)){
    				numMetrics = calculatePair(source, pair);
    				break;
    			}
    		}
    		msg = "pair: (" + row + ", " + col + ") is calculated for " + source;
    	} else {    		
    		numMetrics = calculateAll(source, sourceElement, dirtyonly);
    		msg = "all pairs are calculated for " + source;
    	}
    	logger.info(msg);
    	
    	GeneralUtil.writeXMLToClasspath(doc, "recconfig.xml");
    	sourceElement.addAttribute("lastSavedMetrics", numMetrics.toString());    	
    	
    	logger.info("computing recommendations done.");
    	return doc.asXML();
    }
    
	private int calculateAll(String source, Element e, String dirtyonly) throws DocumentException, IOException {
		// for similarity
    	Map sdata = null;
    	
    	// for sp
    	Map udata = null;
    	
    	int numMetrics = 0;
		for (Element pair : (List<Element>)e.elements()){
			String row = pair.attributeValue("row");
			String col = pair.attributeValue("col");
			String metricType = pair.attributeValue("metric");
			String dirty = pair.attributeValue("dirty");
			if (dirtyonly.equals("1") && (dirty != null && !dirty.equals("1"))) continue;
			
			logger.info("row=" + row + ", col=" + col + ", metricType=" + metricType);
			
			if (metricType.equals(Constants.ALG_SP)){				
				String universal = pair.attributeValue("universal");
				Map data = null;
				if (universal.equals("1")){
					logger.info("universal=true");
					if (udata == null) udata = getSPData(pair);
					data = udata;
				} else {
					data = getSPData(pair);
				}
								
				numMetrics += calculatePair_sp(row, col, metricType, source, data);
			} else if (metricType.equals(Constants.ALG_ERGM)){
				logger.warn("ergm is not implemented!");
			} else {
		    	if (sdata == null) sdata = getSimilarityData(pair);
		    	numMetrics += calculatePair_similarity(source, pair, sdata);
			}
			
			pair.addAttribute("lastCalcTime", new Date().toString());
			pair.addAttribute("dirty", "0");
		}
		
		return numMetrics;
	}
	

    private int calculatePair(String source, Element pair) throws DocumentException, IOException{
		String row = pair.attributeValue("row");
		String col = pair.attributeValue("col");
		String metricType = pair.attributeValue("metric");
		logger.info("row=" + row + ", col=" + col + ", metricType=" + metricType);
		
		int numMetrics = 0;
		if (metricType.equals(Constants.ALG_SP)){	    		
			numMetrics = calculatePair_sp(row, col, metricType, source, getSPData(pair));
		} else if (metricType.equals(Constants.ALG_ERGM)){
			logger.warn("ergm is not implemented!");
		} else {
			numMetrics = calculatePair_similarity(source, pair, getSimilarityData(pair));
		}
		
		pair.addAttribute("lastCalcTime", new Date().toString());
		pair.addAttribute("dirty", "0");
		return numMetrics;
    }
    
	@SuppressWarnings("unchecked")
	private Map getSimilarityData(Element pair){
		Map<String, List<Node>> nodeMap = new HashMap<String, List<Node>>();
		Map<String, Integer> nodeSizeMap = new HashMap<String, Integer>();
    	List<String> nodeTypes = nodeDao.getNodeTypes();
    	for (String nodeType : nodeTypes){
    		List<Node> nodeList = nodeDao.findByType(nodeType);
    		nodeMap.put(nodeType, nodeList);
    		nodeSizeMap.put(nodeType, nodeList.size());
    	}
    	
    	Map data = new HashMap();
    	data.put("nodeSizeMap", nodeSizeMap);
    	data.put("nodeMap", nodeMap);
    	
    	return data; 
	}
	
	@SuppressWarnings("unchecked")
	private Map getSPData(Element pair){
		Map data = null;
		logger.info("preparing network...");
    	List<Edge> edges = new LinkedList<Edge>();
    	for (Element edge : (List<Element>)pair.elements()){
    		String edgeType = edge.attributeValue("type");
    		List<Edge> edgesByType = edgeDao.loadByType(edgeType, false);
    		edges.addAll(edgesByType);
    	}
    	Set<Node> nodes = new HashSet<Node>();
    	for (Edge ue : edges){
    		nodes.add(ue.getFromNode());
    		nodes.add(ue.getToNode());
    	}
    	
		if (nodes == null || nodes.size() == 0 || edges == null || edges.size() == 0) {
			logger.warn("nodes or edges are empty.");
			return data;
		}
		
		logger.info("converting to undirected graph...");
        UndirectedGraph jungGraph = DirectionTransformer.toUndirected(SparseGraphConverter.CIKNOW2JUNG(nodes, edges, 3, false), false);
        
        logger.info("calculating dijkstra distance...");
		DijkstraDistance dd = new DijkstraDistance(jungGraph, NEV);

		data = new HashMap();
		data.put("jungGraph", jungGraph);
		data.put("dd", dd);
		
		return data;
	}
	
    @SuppressWarnings("unchecked")
	private int calculatePair_similarity(String source, Element pair, Map sdata) {
    	if (sdata == null){
    		logger.warn("data not available!");
    		return 0;
    	}
    	Map<String, List<Node>> nodeMap = (Map<String, List<Node>>) sdata.get("nodeMap");
    	Map<String, Integer> nodeSizeMap = (Map<String, Integer>) sdata.get("nodeSizeMap");
    	
		String row = pair.attributeValue("row");
		String col = pair.attributeValue("col");
		String metricType = pair.attributeValue("metric");
    	
    	metricDao.delete(row, col, source);
    	
		logger.info("get from node types...");
		List<String> fromNodeTypes = new ArrayList<String>();
		fromNodeTypes.add(row);
		if (!row.equals(col)) fromNodeTypes.add(col);
		
		logger.info("get to node types...");
		List<String> toNodeTypes = new ArrayList<String>();
		for (Element entry : (List<Element>)pair.elements()){
			String t = entry.attributeValue("t");
			if (toNodeTypes.contains(t)) continue;
			toNodeTypes.add(t);
		}
		if (toNodeTypes.isEmpty()) {
			logger.info("this pair is not configured.");
			return 0;
		}
		
		// init matrix
		logger.info("init matrix...");
		int rowSize = 0;
		int colSize = 0;
		for (String nodeType : fromNodeTypes){
			rowSize += nodeSizeMap.get(nodeType);
		}
		for (String nodeType : toNodeTypes){
			colSize += nodeSizeMap.get(nodeType);
		}
		DoubleMatrix2D matrix = new SparseDoubleMatrix2D(rowSize, colSize);    		
		
		// fill matrix
		logger.info("fill matrix...");
		for (Element entry : (List<Element>)pair.elements()){
			for (Element edge : (List<Element>) entry.elements()){
				String edgeType = edge.attributeValue("type");
				String direction = edge.attributeValue("direction");
				List<Edge> edges = edgeDao.loadByType(edgeType, false);
				for (Edge e : edges){    					
					int findex, tindex;
					Node fnode, tnode;
					if (direction.equals("1")){
						fnode = e.getFromNode();
						tnode = e.getToNode();
					} else {
						tnode = e.getFromNode();
						fnode = e.getToNode();
					}
					
					findex = getIndexByNode(nodeMap, nodeSizeMap, fromNodeTypes, fnode);
					tindex = getIndexByNode(nodeMap, nodeSizeMap, toNodeTypes, tnode);   
					if (findex < 0 || tindex < 0) {
						logger.debug("edge (id=" + e.getId() + ") is not eligible.");
						continue;
					}
					double oldWeight = matrix.getQuick(findex, tindex);
					double newWeight = oldWeight + e.getWeight();
					matrix.setQuick(findex, tindex, newWeight);
				}
			}
		}
		
		// calculate
		logger.info("calculate...");
		DoubleMatrix2D rawMatrix = matrix.viewDice();		
		//logger.debug("raw matrix:" + rawMatrix);
		
		DoubleMatrix2D metricsMatrix = null;
		if (metricType.equals(Constants.ALG_PEARSON))
			metricsMatrix = CalculateUtil.calculatePearsonCorrelations(rawMatrix); 
		else if (metricType.equals(Constants.ALG_COSINE))
			metricsMatrix = CalculateUtil.calculateCosineMeasure(rawMatrix);
		else if (metricType.equals(Constants.ALG_EUCLIDEAN))
			metricsMatrix = CalculateUtil.calculateEuclideanDistance(rawMatrix);
		else if (metricType.equals(Constants.ALG_SEUCLIDEAN))
			metricsMatrix = CalculateUtil.calculateStandardizedEuclidean(rawMatrix);
		else if (metricType.equals(Constants.ALG_PMATCH))
			metricsMatrix = CalculateUtil.calculatePositiveMatch(rawMatrix);
		else if (metricType.equals(Constants.ALG_SPMATCH))
			metricsMatrix = CalculateUtil.calculateStandardizedPositiveMatch(rawMatrix);
		else{
			logger.warn("Unrecognized metric type!");
			return 0;
		}
		
		//logger.debug("distance(similarity) matrix:");
		//logger.debug(metricsMatrix);    		
		
		logger.info("save new metrics...");
		int count = 0;
		ArrayList<Metric> metrics = new ArrayList<Metric>();
		metrics.ensureCapacity(Constants.HIBERNATE_BATCH_SIZE);
		if (row.equals(col)){
			int rowBound = metricsMatrix.rows();
			int colBound = metricsMatrix.columns();
			for (int i=0; i<rowBound; i++){
				for (int j=i+1; j<colBound; j++){
					double v = metricsMatrix.getQuick(i, j);
					
					if (Double.isNaN(v)) continue;
					if ((metricType.equals(Constants.ALG_PEARSON) || metricType.equals(Constants.ALG_COSINE)) && v <= 0.0001) continue;
					if ((metricType.equals(Constants.ALG_EUCLIDEAN) || metricType.equals(Constants.ALG_SEUCLIDEAN)) && v <= 0.0001) continue;
					
					Node fromNode = getNodeByIndex(nodeMap, nodeSizeMap, fromNodeTypes, i);
					Node toNode = getNodeByIndex(nodeMap, nodeSizeMap, fromNodeTypes, j);
					
					Metric metric = new Metric();
					metric.setFromNode(fromNode);
					metric.setToNode(toNode);
					metric.setType(Constants.PREFIX_SIMILARITY + metricType);
					metric.setSource(source);
					metric.setSymmetric(true);
					metric.setValue(new Float(v).floatValue());
					metrics.add(metric);
					
					count++;
					if (count % Constants.HIBERNATE_BATCH_SIZE == 0){
						metricDao.save(metrics);
						metrics = new ArrayList<Metric>();	
						metrics.ensureCapacity(Constants.HIBERNATE_BATCH_SIZE);
						logger.info(count + " metrics created.");
					}
				}
			} 
			
			metricDao.save(metrics);
			logger.info(count + " metrics created.");
		} else {
			int rowBound = nodeSizeMap.get(row);
			int colBound = metricsMatrix.columns();
			for (int i=0; i < rowBound; i++){
				for (int j=rowBound; j < colBound; j++){
					double v = metricsMatrix.getQuick(i, j);
					
					if (Double.isNaN(v)) continue;
					if ((metricType.equals(Constants.ALG_PEARSON) || metricType.equals(Constants.ALG_COSINE)) && v <= 0.0001) continue;
					if ((metricType.equals(Constants.ALG_EUCLIDEAN) || metricType.equals(Constants.ALG_SEUCLIDEAN)) && v <= 0.0001) continue;
					
					Node fromNode = getNodeByIndex(nodeMap, nodeSizeMap, fromNodeTypes, i);
					Node toNode = getNodeByIndex(nodeMap, nodeSizeMap, fromNodeTypes, j);
					
					Metric metric = new Metric();
					metric.setFromNode(fromNode);
					metric.setToNode(toNode);
					metric.setType(Constants.PREFIX_SIMILARITY + metricType);
					metric.setSource(source);
					metric.setSymmetric(true);
					metric.setValue(new Float(v).floatValue());
					metrics.add(metric);
					
					count++;
					if (count % Constants.HIBERNATE_BATCH_SIZE == 0){
						metricDao.save(metrics);
						metrics = new ArrayList<Metric>();
						metrics.ensureCapacity(Constants.HIBERNATE_BATCH_SIZE);
						logger.info(count + " metrics created.");
					}
				}
			}
			
			metricDao.save(metrics);
			logger.info(count + " metrics created.");
		}
		
		return count;
	}

	
    private int getIndexByNode(Map<String, List<Node>> nodeMap, Map<String, Integer> nodeSizeMap, List<String> nodeTypes, Node node){
    	int index = 0;
    	
    	String type = node.getType();
    	for (String nodeType : nodeTypes){
    		if (nodeType.equals(type)){
    			List<Node> nodes = nodeMap.get(nodeType);
    			index += nodes.indexOf(node);
    			return index;
    		} else {
    			index += nodeSizeMap.get(nodeType);
    		}
    	}
    	
    	return -1;
    }
    
    private Node getNodeByIndex(Map<String, List<Node>> nodeMap, Map<String, Integer> nodeSizeMap, List<String> nodeTypes, int index){
    	for (String nodeType : nodeTypes){
    		int nodeTypeSize = nodeSizeMap.get(nodeType);
    		if (index >= nodeTypeSize) index -= nodeTypeSize;
    		else {
    			List<Node> nodes = nodeMap.get(nodeType);
    			return nodes.get(index);
    		}
    	}
    	return null;
    }
    
	private int calculatePair_sp(String row, String col, String metricType, String source, Map data) throws DocumentException, IOException {		
		if (data == null) {
			logger.warn("data not available!");
			return 0;
		}
		UndirectedGraph jungGraph = (UndirectedGraph) data.get("jungGraph");
		DijkstraDistance dd = (DijkstraDistance) data.get("dd");
		
		metricDao.delete(row, col, source);				
		
		ArrayList<Metric> list = new ArrayList<Metric>();
		list.ensureCapacity(Constants.HIBERNATE_BATCH_SIZE);
		int count = 0;
		List<Node> fromNodes = nodeDao.findByType(row);
		List<Node> toNodes = null;
		if (!row.equals(col)) toNodes = nodeDao.findByType(col);
		else toNodes = fromNodes;		
		Set<Vertex> vertices = (Set<Vertex>)jungGraph.getVertices();
		//logger.debug("number of vertex: " + vertices.size());
		for (Vertex vsource : vertices){
			//logger.debug("Distance from vertex: " + vsource.getUserDatum(Constants.NODE_LABEL));
			Node from = SparseGraphConverter.JUNG2CIKNOW(vsource);
			if (!fromNodes.contains(from)) continue;
			
			Map distanceMap = dd.getDistanceMap(vsource);
			Set<Vertex> targets = distanceMap.keySet();
			for (Vertex vtarget : targets){
				Node to = SparseGraphConverter.JUNG2CIKNOW(vtarget);
				if (!toNodes.contains(to)) continue;
				Number distance = (Number)distanceMap.get(vtarget);
				//logger.debug("to vertex: " + vtarget.getUserDatum(Constants.NODE_LABEL) + ": " + distance);
				if (distance.doubleValue() == Double.NaN) continue;				
				if (from.getId().equals(to.getId())) continue;
				
				Metric metric = new Metric();
				metric.setFromNode(from);
				metric.setToNode(to);
				metric.setType(metricType);
				metric.setSource(source);
				if (!row.equals(col)) metric.setSymmetric(true);
				else metric.setSymmetric(false);
				metric.setValue(distance.floatValue());
				list.add(metric);
				count++;
				
				if (count % Constants.HIBERNATE_BATCH_SIZE == 0){
					metricDao.save(list);
					list = new ArrayList<Metric>();
					list.ensureCapacity(Constants.HIBERNATE_BATCH_SIZE);
					logger.info(count + " metrics created.");
				}
			}
		}
		
		metricDao.save(list);
		logger.info(count + " metrics created.");	
		
		return count;
	}
	

	private static final NumberEdgeValue NEV = new NumberEdgeValue(){

		public Number getNumber(ArchetypeEdge edge) {
			return (Double)edge.getUserDatum(Constants.EDGE_WEIGHT); 
		}

		public void setNumber(ArchetypeEdge edge, Number weight) {
			edge.setUserDatum(Constants.EDGE_WEIGHT, weight.doubleValue(), UserData.CLONE);
			
		}
	};
}
