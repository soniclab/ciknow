package ciknow.recommend.service;

import ciknow.domain.Node;
import ciknow.domain.Recommendation;

import javax.jws.WebService;
import java.util.List;
import java.util.Map;

/**
 * User: gyao
 * Date: Mar 4, 2008
 * Time: 12:07:35 AM
 */

@WebService
public interface RecommenderService {
    public List<Recommendation> getRecommendations(Long nodeId, List<String> keywords, int numRecs, String operator, String questionId);
    public List<Map> getRecommendationDTOs(Map input);
    public List<Recommendation> getRecommendationsByAttribute(Node node, String attrName, int numRecs);
}
