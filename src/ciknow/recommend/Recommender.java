package ciknow.recommend;

import java.util.*;

import ciknow.UnsupportedOperationException;
import ciknow.domain.Node;
import ciknow.domain.Recommendation;

/**
 * Makes recommendations based upon the given graph.
 * @author gyao
 */
public interface Recommender {
	// Default number of recommendations to supply when none is specified.
	public static final int DEFAULT_NUM_RECS = 1;
	
	// recommender name
	public String getName();
	
	/**
	 * Returns a list of recommendations, in order
	 * from highest to lowest confidence.
	 */
	public List<Recommendation> getRecommendations(Node node) throws UnsupportedOperationException;
	
	/**
	 * Returns a list of recommendations, with a maximum
	 * of <code>numRecs</code> elements.  The list is sorted from
	 * highest to lowest confidence.
	 */
	public List<Recommendation> getRecommendations(Node node, int numRecs) throws UnsupportedOperationException;
	
	/**
	 * Returns a list of recommendations, taking into
	 * consideration only the link between <code>node</code> and
	 * <code>target</code> as the basis.
	 */
	public List<Recommendation> getRecommendations(Node node, Node target) throws UnsupportedOperationException;
	
	/**
	 * Identical to <code>getRecommendations(Node,Node)</code> but 
	 * returns at most <code>numRecs</code> recommendations.
	 */
	public List<Recommendation> getRecommendations(Node node, Node target, int numRecs) throws UnsupportedOperationException;
	
	/**
	 * Returns a list of recommendations, taking into
	 * consideration only the links between <code>node</code> and
	 * <code>targets</code> as the basis.
	 */
	public List<Recommendation> getRecommendations(Node node, Collection<Node> targets) throws UnsupportedOperationException;
	
	/**
	 * Identical to <code>getRecommendations(Node,Collection)</code> but 
	 * returns at most <code>numRecs</code> recommendations.
	 */
	public List<Recommendation> getRecommendations(Node node, Collection<Node> targets, int numRecs) throws UnsupportedOperationException;
	
	public List<Recommendation> getRecommendations(Node node, Collection<Node> targets, int numRecs, int operator) throws UnsupportedOperationException;

    public List<Recommendation> getRecommendations(Node node, Collection<Node> targets, int numRecs, int operator, double... args) throws UnsupportedOperationException;
}
