package ciknow.graph.converter;

import ciknow.util.Constants;
import edu.uci.ics.jung.graph.ArchetypeEdge;
import edu.uci.ics.jung.graph.decorators.NumberEdgeValue;
import edu.uci.ics.jung.utils.UserData;

/**
 * The main Jung {@link NumberEdgeValue} implementation for CIKNOW.
 * @author andydon
 *
 */
public class NumberEdgeValueImpl implements NumberEdgeValue {

	public Number getNumber(ArchetypeEdge e) {
		return (Float) e.getUserDatum(Constants.EDGE_WEIGHT);
	}

	public void setNumber(ArchetypeEdge e, Number num) {
		e.setUserDatum(Constants.EDGE_WEIGHT, num.toString(), UserData.SHARED);
	}

}
