package ciknow.graph.converter;

import edu.uci.ics.jung.graph.ArchetypeEdge;
import edu.uci.ics.jung.graph.decorators.NumberEdgeValue;

/**
 * A Jung {@link NumberEdgeValue} implementation for CIKNOW.
 * 
 * This is a just a dummy implementation that returns the same weight for all edge weights
 * Used in calculations where edge weights need to be ignored.
 * 
 * @author andydon
 *
 */

public class DummyNumberEdgeValue implements NumberEdgeValue {

	public Number getNumber(ArchetypeEdge arg0) {
		return 1.0;
	}

	public void setNumber(ArchetypeEdge arg0, Number arg1) {
	}

}
