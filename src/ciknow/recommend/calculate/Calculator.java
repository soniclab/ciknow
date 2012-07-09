package ciknow.recommend.calculate;
/**
 * Implementing classes will provide the functionality needed to pre-calculate
 * whatever is needed for their corresponding Recommender implementation.
 * @author gyao
 */
public interface Calculator {
	/**
	 * Run the pre-calculations.
	 */
	public void calculate() throws Exception;
}
