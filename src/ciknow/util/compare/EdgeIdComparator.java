package ciknow.util.compare;

import ciknow.domain.Edge;
import java.util.Comparator;

/**
 *
 * @author gyao
 */
public class EdgeIdComparator implements Comparator<Edge> {

    @Override
    public int compare(Edge e1, Edge e2) {
        return e1.getId().compareTo(e2.getId());
    }
}
