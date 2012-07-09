package ciknow.recommend;

import java.util.*;
import ciknow.UnsupportedOperationException;
import ciknow.domain.Node;
import ciknow.domain.Recommendation;
import ciknow.util.Constants;

public class MockRecommender extends AbstractRecommender implements Recommender {

    public List<Recommendation> getRecommendations(Node node, Collection<Node> targets, int numRecs, int operator) throws UnsupportedOperationException{
        double[] args = {};  // default parameters
        return getRecommendations(node, targets, numRecs, operator, args);
    }

    public List<Recommendation> getRecommendations(
			Node node, Collection<Node> targets, int numRecs, int operator, double... args){

		List<Recommendation> recs = new LinkedList<Recommendation>();
        double random;
        String[] types = {Constants.NODE_TYPE_USER, "Online Presentations", "tags", "docs"};

        for (long i=0; i<numRecs; i++){
            Recommendation rec = new Recommendation();
            rec.setFinalScore(new Double(100 - i));
            rec.setUser(node);


            Node recommended = new Node();
            recommended.setId(i+10);
            recommended.setLabel("label" + i);
            random = Math.random();
            if (random < 0.25) recommended.setType(types[0]);
            else if (random < 0.5) recommended.setType(types[1]);
            else if (random < 0.75) recommended.setType(types[2]);
            else recommended.setType(types[3]);
            recommended.setVersion(1L);

            rec.setTarget(recommended);

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