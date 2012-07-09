package ciknow.recommend;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import ciknow.UnsupportedOperationException;
import ciknow.dao.MetricDao;
import ciknow.domain.Metric;
import ciknow.domain.Node;
import ciknow.domain.Recommendation;
import ciknow.recommend.rescore.Rescorer;
import ciknow.util.Beans;
import ciknow.util.Constants;

import org.apache.log4j.Logger;

/**
 * @author gyao
 */
public abstract class AbstractRecommender implements Recommender {
	private static Logger logger = Logger.getLogger(AbstractRecommender.class);
	private String name;
	private List<Rescorer> rescorers;	
	private MetricDao metricDao;
	
	public AbstractRecommender(){
		this("AbstractRecommender");
	}
	
	public AbstractRecommender(String name) {
		super();
		this.name = name;
		rescorers = new LinkedList<Rescorer>();
		this.metricDao = (MetricDao)Beans.getBean("metricDao");
	}


	// NAME
	public String getName() {
		return name;
	}
	protected void setName(String name){
		this.name = name;
	}

	// GET RECOMMENDATIONS
	public List<Recommendation> getRecommendations(Node node)  throws UnsupportedOperationException{
		return getRecommendations(node, DEFAULT_NUM_RECS);
	}

	public List<Recommendation> getRecommendations(Node node, int numRecs)  throws UnsupportedOperationException{		
		return getRecommendations(node, new HashSet<Node>(), numRecs);
	}
	
	public List<Recommendation> getRecommendations(Node node, Node target)  throws UnsupportedOperationException{
		List<Node> targets = new ArrayList<Node>(1);
		
		if(target != null)
			targets.add(target);
		
		return getRecommendations(node, targets, DEFAULT_NUM_RECS);
	}

	public List<Recommendation> getRecommendations(Node node,
													Node target, 
													int numRecs)  throws UnsupportedOperationException{
		List<Node> targets = new ArrayList<Node>(1);
		
		if(target != null)
			targets.add(target);
		
		return getRecommendations(node, targets, numRecs);
	}
	
	public List<Recommendation> getRecommendations(Node node, Collection<Node> targets)  throws UnsupportedOperationException{
		return getRecommendations(node, targets, DEFAULT_NUM_RECS);
	}
	
	public List<Recommendation> getRecommendations(Node node, Collection<Node> targets, int numRecs) throws UnsupportedOperationException{
		return getRecommendations(node, targets, numRecs, Constants.AND);
	}
    

    ///////////////////////////// UTILITY ////////////////////////////////
	
	// rescore the recommendations
	protected void addRescorer(Rescorer rescorer){
		this.rescorers.add(rescorer);
	}
	protected void addRescorers(Collection<Rescorer> rescorers){
		this.rescorers.addAll(rescorers);
	}
	protected void clearRescorers(){
		this.rescorers.clear();
	}
	protected void rescore(Collection<Recommendation> recommendations){
		for (Rescorer rescorer : this.rescorers){
			logger.info("applying rescorer: " + rescorer.getClass());
			for (Recommendation rec : recommendations){
				rescorer.rescore(rec);
			}
		}
	}
	
	// remove recommendations with score less than a threshold
	public static void compact(Collection<Recommendation> recs, float thredshold){
		for (Iterator<Recommendation> itr = recs.iterator(); itr.hasNext(); ){
			Recommendation rec = itr.next();
			if (rec.getFinalScore() < thredshold) {
				itr.remove();
				Node target = rec.getTarget();
				logger.debug("compaction: remove node (id=" + target.getId() + ")");
			}
		}
	}
	
	// normalize score to be between 0 and 1
	protected void normalizeScores(Collection<Recommendation> recommendations){
		double max = 0.0;

		// Find the maximum
		for (Recommendation r : recommendations)
			if (r.getFinalScore() > max)
				max = r.getFinalScore();
		
		for (Recommendation r : recommendations){
			double score = r.getFinalScore();
			r.setFinalScore(score / max);
			Long targetId = r.getTarget().getId();
			String targetLabel = r.getTarget().getLabel();
			String targetType = r.getTarget().getType();
			logger.debug(targetId + "(" + targetLabel + "," + targetType + "): " + score + " -> " + r.getFinalScore());
		}
	}
	
	
	
	protected float getMax(Map<Long, Metric> map){
		float max = 0;
		Collection<Metric> metrics = map.values();
		for (Metric m : metrics){
			if (m.getValue() > max) max = m.getValue(); 
		}
		return max;
	}
	
	/*
	protected Map<Long, Float> getScoreMap(Node node, 
			String metricType, 
			String source, 
			int operator){
		List<Node> nodes = new ArrayList<Node>();
		nodes.add(node);
		return getScoreMap(nodes, metricType, source, operator);
	}
	*/
	
	/*
	protected Map<Long, Float> getScoreMap(Collection<Node> nodes, 
											String metricType, 
											String source, 
											int operator){
		logger.info("get score map...");
		// prepare metric map
		Map<Long, Map<Long, Float>> targetMap = new HashMap<Long, Map<Long, Float>>();
		List<Metric> metrics = metricDao.findByNodesAndTypeAndSource(nodes, metricType, source);
		Set<Long> nodeIds = new HashSet<Long>();
		for (Node node : nodes){
			nodeIds.add(node.getId());
			logger.debug("target: " + node.getLabel());
		}
		logger.debug("metricType: " + metricType);
		logger.debug("datasource: " + source);
		logger.debug("operator: " + operator);
		
		for (Metric metric : metrics){
			Long fid = metric.getFromNode().getId();
			Long tid = metric.getToNode().getId();

			String type = metric.getType();
			float v = metric.getValue();			
			if (type.equals(Constants.ALG_COSINE) || type.equals(Constants.ALG_PEARSON)){
				v = 1/v;
			}
			
			if (nodeIds.contains(fid)){
				Map<Long, Float> map = targetMap.get(fid);
				if (map == null) {
					map = new HashMap<Long, Float>();
					targetMap.put(fid, map);
				}			
				map.put(tid, v);
			}
			
			if (nodeIds.contains(tid)){
				Map<Long, Float> map = targetMap.get(tid);
				if (map == null) {
					map = new HashMap<Long, Float>();
					targetMap.put(tid, map);
				}			
				map.put(fid, v);
			}
		}
		
		// calculate scores
		Map<Long, Float> scoreMap = new HashMap<Long, Float>();
		if (operator == Constants.AND) scoreMap = getScoreMapByAnd(targetMap);
		if (operator == Constants.OR) scoreMap = getScoreMapByOr(targetMap);
		
		// remove target nodes
		for (Long nodeId : nodeIds){
			scoreMap.remove(nodeId);
		}
		
		logger.info("obtained " + scoreMap.size() + " entries in the score map.");
		return scoreMap;
	}
	*/
		
	protected Map<Long, Float> getScoreMapByAnd(Map<Long, Map<Long, Float>> targetMap){
		Map<Long, Float> scoreMap = new HashMap<Long, Float>();
		
		// determine recommended nodes 	
		Set<Long> container = new HashSet<Long>(); 
		for (Long target : targetMap.keySet()) {
			Map<Long, Float> entryMap = targetMap.get(target);
			Set<Long> entrySet = entryMap.keySet();
			if (container.size() == 0) container.addAll(entrySet);
			else container.retainAll(entrySet);
		}

		// calculate raw score for each recommended items
		for (Long node : container) {
			for (Long target : targetMap.keySet()) {
				Map<Long, Float> entryMap = targetMap.get(target);
				Float newScore = entryMap.get(node);
				if (scoreMap.get(node) == null)
					scoreMap.put(node, newScore);
				else {
					float oldScore = scoreMap.get(node);
					scoreMap.put(node, oldScore * newScore);
				}				
			}
		}
		return scoreMap;
	}
	
	protected Map<Long, Float> getScoreMapByOr(Map<Long, Map<Long, Float>> targetMap){
		
		Map<Long, Float> scoreMap = new HashMap<Long, Float>();
		
		// determine recommended nodes 	
		Set<Long> container = new HashSet<Long>(); 
		for (Long target : targetMap.keySet()) {
			Map<Long, Float> entryMap = targetMap.get(target);
			Set<Long> entrySet = entryMap.keySet();
			container.addAll(entrySet);
		}

		// calculate raw score for each recommended items
		for (Long node : container) {
			for (Long target : targetMap.keySet()) {
				Map<Long, Float> entryMap = targetMap.get(target);
				Float newScore = entryMap.get(node);
				if (newScore==null) continue;
				
				if (scoreMap.get(node) == null)
					scoreMap.put(node, newScore);
				else {
					float oldScore = scoreMap.get(node);
					scoreMap.put(node, oldScore + newScore);
				}				
			}
		}
		return scoreMap;
	}
	
}
