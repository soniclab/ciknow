package ciknow.recommend;

import ciknow.domain.Recommendation;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: gyao
 * Date: Feb 19, 2008
 * Time: 10:19:33 AM
 * To change this template use File | Settings | File Templates.
 */
public class RecommendationComparator implements Comparator<Recommendation> {
	public int compare(Recommendation a, Recommendation b) {
		return (int)Math.signum(b.getFinalScore() - a.getFinalScore());
	}
}
