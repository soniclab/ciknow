package ciknow.util.compare;

import ciknow.domain.Node;
import java.util.Comparator;

/**
 *
 * @author gyao
 */
public class NodeLabelComparator implements Comparator<Node> {

    @Override
    public int compare(Node o1, Node o2) {
        Node node1 = (Node) o1;
        Node node2 = (Node) o2;
        return node1.getLabel().compareTo(node2.getLabel());
    }
}
