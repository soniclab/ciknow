package ciknow.zk.survey.response;

import ciknow.domain.Node;
import java.util.Comparator;

/**
 *
 * @author gyao
 */
public class NodeLabelComparator implements Comparator<Node> {

    @Override
    public int compare(Node o1, Node o2) {
        return o1.getLabel().compareTo(o2.getLabel());
    }
}
