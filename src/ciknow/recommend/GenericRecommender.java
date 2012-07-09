package ciknow.recommend;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import ciknow.UnsupportedOperationException;
import ciknow.domain.Metric;
import ciknow.domain.Node;
import ciknow.domain.Recommendation;
import ciknow.dao.MetricDao;
import ciknow.dao.NodeDao;
import ciknow.util.Beans;
import ciknow.util.Constants;
import ciknow.util.GeneralUtil;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class GenericRecommender extends AbstractRecommender implements Recommender {
	private static Logger logger = Logger.getLogger(GenericRecommender.class);
	

	public static void main(String[] args) throws UnsupportedOperationException {
		Beans.init();
		NodeDao nodeDao = (NodeDao)Beans.getBean("nodeDao");
		Node node = nodeDao.findById(2L);
		List<Node> targets = new ArrayList<Node>();
		targets.add(nodeDao.findById(4L));
		new GenericRecommender().getRecommendations(node, targets, 20);
	}

	public GenericRecommender() {
		super("generic");
	}

    public List<Recommendation> getRecommendations(Node node, Collection<Node> targets, int numRecs, int operator) throws UnsupportedOperationException{
        double[] args = {};  // default parameters
        return getRecommendations(node, targets, numRecs, operator, args);
    }

    public List<Recommendation> getRecommendations(
			Node node, Collection<Node> targets, int numRecs, int operator, double... args) throws UnsupportedOperationException{

    	return getRecommendations(node, targets.iterator().next(), numRecs, operator, args);
	}
    
    public List<Recommendation> getRecommendations(
			Node node, Node target, int numRecs, int operator, double... args) throws UnsupportedOperationException{
		logger.info("get Recommendations...");
		
		List<Recommendation> recs = new LinkedList<Recommendation>();
		if (node == null || target == null) return recs;
		
		Map<Long, Metric> identificationMap = getScoreMap(target, "identification");
		logger.debug("got identification score map: size=" + identificationMap.size());
				
		Map<Long, Metric> selectionMap = getScoreMap(node, "selection");
		logger.debug("got selection score map: size=" + selectionMap.size());
		
		logger.debug("run formula");
		recs = formulate(node, identificationMap, selectionMap);
		logger.info("there are " + recs.size() + " raw recommendations.");

		logger.debug("rescoring/filtering...");
		compact(recs, 0.0000000001f);

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
	
    public static Map<Long, Metric> getScoreMap(Node node, String source){
    	Beans.init();
    	MetricDao metricDao = (MetricDao) Beans.getBean("metricDao");
    	List<Metric> metrics = metricDao.findByNodeAndSource(node.getId(), source);
    	
    	Map<Long, Metric> scoreMap = new HashMap<Long, Metric>();
    	for (Metric m : metrics){
    		if (m.getValue() < Float.MIN_VALUE) continue;
    		
    		if (m.getFromNode().getId().equals(node.getId())) scoreMap.put(m.getToNode().getId(), m);
    		else scoreMap.put(m.getFromNode().getId(), m);
    	}
    	
    	return scoreMap;
    }
    
	private List<Recommendation> formulate(
			Node node, Map<Long, Metric> identificationMap, Map<Long, Metric> selectionMap) {
		
		float max_identification_distance = getMax(identificationMap);
		float max_selection_distance = getMax(selectionMap);
		logger.debug("maximum distance from recommended items to search terms: " + max_identification_distance);
		logger.debug("maximum distance from recommended items to user: " + max_selection_distance);

		Beans.init();
		NodeDao nodeDao = (NodeDao)Beans.getBean("nodeDao");
		List<Recommendation> recs = new ArrayList<Recommendation>();
		for (Long recommended : identificationMap.keySet()) {
			Metric seMetric = selectionMap.get(recommended);
			if (seMetric == null) continue;
			String seMetricType = seMetric.getType();			
			Float seScore = seMetric.getValue();
			if (seMetricType.equals(Constants.ALG_SP) || seMetricType.equals(Constants.ALG_EUCLIDEAN) || seMetricType.equals(Constants.ALG_SEUCLIDEAN)){
				seScore = 1/seScore;
			}
			
			Metric idMetric = identificationMap.get(recommended);
			String idMetricType = idMetric.getType();
			Float idScore = idMetric.getValue();
			if (idMetricType.equals(Constants.ALG_SP) || idMetricType.equals(Constants.ALG_EUCLIDEAN) || idMetricType.equals(Constants.ALG_SEUCLIDEAN)){
				idScore = 1/idScore;
			}

			double score = 0;
			
			//logger.debug("idScore: " + idScore + ", seScore: " + seScore);
			score = idScore * seScore;
			
			Recommendation rec = new Recommendation();
			rec.setUser(node);
			rec.setTarget(nodeDao.findById(recommended));
			rec.setIdentifyScore(idScore.doubleValue());
			rec.setIdMetricType(idMetricType);
			rec.setSelectScore(seScore.doubleValue());
			rec.setSeMetricType(seMetricType);
			rec.setFinalScore(score);
			recs.add(rec);
		}
		return recs;
	}


	/////////////////////// INTENTIONALLY UNSUPPORTED ////////////////////////
	public List<Recommendation> getRecommendations(Node node)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	public List<Recommendation> getRecommendations(Node node, int numRecs)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

}
