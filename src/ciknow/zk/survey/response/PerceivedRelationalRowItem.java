package ciknow.zk.survey.response;

import ciknow.domain.Edge;
import ciknow.domain.Node;
import ciknow.domain.Question;
import java.util.List;
import java.util.Map;

/**
 *
 * @author gyao
 */
public class PerceivedRelationalRowItem {

    public final Question question;
    public final List<String> ratingLabels;
    public final Node fromNode;
    public final List<Node> toNodes;
    public final Map<Long, Edge> toNodeEdgeMap;

    public PerceivedRelationalRowItem(Question question, List<String> ratingLabels, Node fromNode,
            List<Node> toNodes, Map<Long, Edge> toNodeEdgeMap) {
        super();
        this.question = question;
        this.ratingLabels = ratingLabels;
        this.fromNode = fromNode;
        this.toNodes = toNodes;
        this.toNodeEdgeMap = toNodeEdgeMap;
    }
}
