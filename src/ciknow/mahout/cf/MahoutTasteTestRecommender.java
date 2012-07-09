package ciknow.mahout.cf;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.IRStatistics;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;
import org.apache.mahout.cf.taste.eval.RecommenderIRStatsEvaluator;
import org.apache.mahout.cf.taste.impl.eval.AverageAbsoluteDifferenceRecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.eval.GenericRecommenderIRStatsEvaluator;
import org.apache.mahout.cf.taste.impl.eval.LoadEvaluator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

public class MahoutTasteTestRecommender {
	private static Log logger = LogFactory.getLog(MahoutTasteTestRecommender.class);
	
	public static void main(String[] args) throws IOException, TasteException {
		String path = "data/grouplens/100k/ua.base";
		//path = "build/WEB-INF/classes/ratings.txt";
		DataModel dataModel = new FileDataModel(new File(path));
		
		RecommenderBuilder builder = new RecommenderBuilder(){

			@Override
			public Recommender buildRecommender(DataModel dataModel)
					throws TasteException {
				UserSimilarity sim = new PearsonCorrelationSimilarity(dataModel);
				UserNeighborhood neighborhood = new NearestNUserNeighborhood(5, sim, dataModel);
				Recommender recommender = new GenericUserBasedRecommender(dataModel, neighborhood, sim);
				return recommender;
			}
			
		};
		
		logger.info("evaluating ...");
		RecommenderEvaluator evaluator = new AverageAbsoluteDifferenceRecommenderEvaluator();
		double score = evaluator.evaluate(builder, null, dataModel, 0.7, 1);
		logger.info("score: " + score);
		
		
//		logger.info("evaluating IR stats...");
//		RecommenderIRStatsEvaluator IRevaluator = new GenericRecommenderIRStatsEvaluator();
//		IRStatistics stats = IRevaluator.evaluate(builder, null, dataModel, null, 10, GenericRecommenderIRStatsEvaluator.CHOOSE_THRESHOLD, 1);
//		logger.info("precision: " + stats.getPrecision());
//		logger.info("recall: " + stats.getRecall()); 
		
//		Recommender recommender = builder.buildRecommender(dataModel);
//		LoadEvaluator.runLoad(recommender);
	}
}
