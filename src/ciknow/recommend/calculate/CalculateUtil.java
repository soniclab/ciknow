package ciknow.recommend.calculate;

import org.apache.log4j.Logger;

import hep.aida.bin.StaticBin1D;
import cern.colt.list.DoubleArrayList;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.doublealgo.Statistic;
import cern.colt.matrix.doublealgo.Statistic.VectorVectorFunction;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.jet.math.Functions;

public class CalculateUtil {
	private static Logger logger = Logger.getLogger(CalculateUtil.class);
	
	public static final VectorVectorFunction COSINE = new VectorVectorFunction() {
		public final double apply(DoubleMatrix1D a, DoubleMatrix1D b) {
	        double nominator =  a.aggregate(b, Functions.plus, Functions.mult );
	        //handle case when a == 0 in a/b
	        if (nominator == 0)
	            return 0;

	        return nominator /
	                Math.sqrt(a.aggregate(Functions.plus,Functions.pow(2)) * b.aggregate(Functions.plus, Functions.pow(2)));
		}
	};
	
	/**
	 * Positive match distance function; <tt>Sqrt(Sum( (x[i] * y[i])^2 ))</tt>.
	 */ 
	public static final VectorVectorFunction PMATCH = new VectorVectorFunction() {
		public final double apply(DoubleMatrix1D a, DoubleMatrix1D b) {
			return Math.sqrt(a.aggregate(b, Functions.plus, Functions.chain(Functions.square,Functions.mult)));
		}
	};
	
	/**
	 * Calculate the cosine measure of given matrix
	 * @param matrix - input matrix
	 * @return 
	 */
	public static DoubleMatrix2D calculateCosineMeasure(DoubleMatrix2D matrix){
		return Statistic.distance(matrix, CalculateUtil.COSINE);
	}
	
	/**
	 * Calculate pearson correlation matrix
	 * @param raw - input matrix
	 * @return -  correlation matrix (symmetric)
	 */
	public static DoubleMatrix2D calculatePearsonCorrelations(DoubleMatrix2D raw){
		logger.debug("covariance ...");
		DoubleMatrix2D variances = Statistic.covariance(raw);
		logger.debug(variances);
		
		logger.debug("correlations ...");
		DoubleMatrix2D correlations = Statistic.correlation(variances);
		logger.debug(correlations);
		return correlations;
	}
	
	/**
	 * Calculate the Euclidean distance matrix
	 * @param matrix - input matrix
	 * @return - euclidean distance matrix (symmetric)
	 */
	public static DoubleMatrix2D calculateEuclideanDistance(DoubleMatrix2D matrix){
		return Statistic.distance(matrix, Statistic.EUCLID);
	}
	
	/**
	 * Constructs and returns the distance matrix of the given matrix.
	 * The distance matrix is a square, symmetric matrix consisting of nothing but distance coefficients. 
	 * The rows and the columns represent the variables, the cells represent distance coefficients. 
	 * The diagonal cells (i.e. the distance between a variable and itself) will be zero.
	 * Compares two column vectors at a time. Use dice views to compare two row vectors at a time.
	 * 
	 * @param matrix any matrix; a column holds the values of a given variable (vector).
	 * @param distanceFunction (EUCLID, CANBERRA, ..., or any user defined distance function operating on two vectors).
	 * @return the distance matrix (<tt>n x n, n=matrix.columns</tt>).
	 */
	public static DoubleMatrix2D calculateStandardizedEuclidean(DoubleMatrix2D matrix) {
		int columns = matrix.columns();
		DoubleMatrix2D distance = new cern.colt.matrix.impl.DenseDoubleMatrix2D(columns,columns);

		// calculate standard deviations
		DoubleMatrix1D sds = standardDeviation(matrix.viewDice());
		logger.debug("standard deviations: ");
		logger.debug(sds);
		
		// cache views
		DoubleMatrix1D[] cols = new DoubleMatrix1D[columns];
		for (int i=columns; --i >= 0; ) {
			cols[i] = matrix.viewColumn(i).copy().assign(sds, Functions.div);
			//cols[i] = matrix.viewColumn(i);
		}
		
		// work out all permutations
		for (int i=columns; --i >= 0; ) {
			for (int j=i; --j >= 0; ) {
				double d = Statistic.EUCLID.apply(cols[i], cols[j]);
				distance.setQuick(i,j,d);
				distance.setQuick(j,i,d); // symmetric
			}
		}
		
		logger.debug("standardized euclidean distances: ");
		logger.debug(distance);
		return distance;
	}



	public static DoubleMatrix2D calculatePositiveMatch(DoubleMatrix2D matrix){
		return Statistic.distance(matrix, PMATCH);
	}
	
	public static DoubleMatrix2D calculateStandardizedPositiveMatch(DoubleMatrix2D matrix) {
		int columns = matrix.columns();
		DoubleMatrix2D distance = new cern.colt.matrix.impl.DenseDoubleMatrix2D(columns,columns);

		// calculate standard deviations
		DoubleMatrix1D sds = standardDeviation(matrix.viewDice());
		logger.debug("standard deviations: ");
		logger.debug(sds);
		
		// cache views
		DoubleMatrix1D[] cols = new DoubleMatrix1D[columns];
		for (int i=columns; --i >= 0; ) {
			cols[i] = matrix.viewColumn(i).copy().assign(sds, Functions.div);
			//cols[i] = matrix.viewColumn(i);
		}
		
		// work out all permutations
		for (int i=columns; --i >= 0; ) {
			for (int j=i; --j >= 0; ) {
				double d = PMATCH.apply(cols[i], cols[j]);
				distance.setQuick(i,j,d);
				distance.setQuick(j,i,d); // symmetric
			}
		}
		
		logger.debug("standardized positve match: ");
		logger.debug(distance);
		return distance;
	}
	
	private static DoubleMatrix1D standardDeviation(DoubleMatrix2D matrix){		
		int colNum = matrix.columns();
		DoubleMatrix1D sds = new DenseDoubleMatrix1D(colNum);
		DoubleMatrix1D columnMatrix;
		StaticBin1D bin = new StaticBin1D();
		for (int col = 0; col < colNum; col++){
			columnMatrix = matrix.viewColumn(col);		
			bin.addAllOf(new DoubleArrayList(columnMatrix.toArray()));
			double sd = bin.standardDeviation();
			sds.setQuick(col, sd);
			bin.clear();
		}	
		return sds;
	}
}
