package ciknow.util.compare;

import ciknow.domain.Node;
import java.util.Comparator;

/**
 *
 * @author gyao
 */
public class NodeIdComparator implements Comparator<Node> {

    @Override
    public int compare(Node n1, Node n2) {
        return n1.getId().compareTo(n2.getId());
    }
}
