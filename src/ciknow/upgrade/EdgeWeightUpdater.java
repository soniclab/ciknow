package ciknow.upgrade;

import ciknow.util.EdgeUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is to run in command line or ant
 * Can also run from web interface via EdgeRO.updateEdgeWeights()
 * @author gyao
 */
public class EdgeWeightUpdater {

    private static Log logger = LogFactory.getLog(EdgeWeightUpdater.class);

    public static void main(String[] args) {
        try {
            EdgeUtil.updateEdgeWeights();
        } catch (Exception e) {
        	logger.error(e.getMessage());
            e.printStackTrace();
        }
    }
}
