package ciknow.zk.survey.response;

import ciknow.domain.Edge;
import ciknow.domain.Node;
import ciknow.domain.Question;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.zul.*;

/**
 *
 * @author gyao
 */
public class RelationalContinuous extends AbstractQuestionRelation {

    private static final long serialVersionUID = 4896481036027046485L;
    private static Log logger = LogFactory.getLog(RelationalContinuous.class);
    // state
    private Map<Long, Edge> nodeEdgeMap;

    public RelationalContinuous(Question currentQuestion) {
        super(currentQuestion);
    }

    public RelationalContinuous(Node respondent, Question currentQuestion) {
        super(respondent, currentQuestion);
    }

    @Override
    public void afterCreationComplete() {
        super.afterCreationComplete();

        nodeEdgeMap = new HashMap<Long, Edge>();
        for (Edge edge : questionEdges) {
            nodeEdgeMap.put(edge.getToNode().getId(), edge);
        }

        boolean recordDuration = currentQuestion.isRecordDuration();
        if (recordDuration) {
            // header
            Columns columns = new Columns();
            columns.setParent(grid);
            columns.setSizable(true);

            Column column = new Column();
            column.setWidth(getFirstColumnWidth() + "px");
            column.setParent(columns);

            String[] headers = {"Year", "Month", "Day", "Hour", "Minute", "Second"};
            for (String header : headers) {
                column = new Column(header);
                column.setWidth("65px");
                column.setAlign("center");
                column.setParent(columns);
            }
        }

        // body
        Rows rows = new Rows();
        rows.setParent(grid);
        for (Node node : rowNodes) {
            Edge edge = nodeEdgeMap.get(node.getId());

            if (recordDuration) {
                Double weight = null;
                if (edge != null) {
                    weight = edge.getWeight();
                }
                if (weight == null) {
                    weight = 0d;
                }

                RelationalDurationRow row = new RelationalDurationRow(node, weight.longValue());
                row.setParent(rows);
                row.setValue(node);
            } else {
                Row row = new Row();
                row.setParent(rows);
                row.setValue(node);

                Label nodeLabel = new Label(node.getLabel());
                nodeLabel.setParent(row);
                nodeLabel.setWidth(getFirstColumnWidth() + "px");

                Doublebox doublebox = new Doublebox();
                doublebox.setParent(row);
                if (edge != null) {
                    doublebox.setValue(edge.getWeight());
                }
            }
        }
    }

    @Transactional
    @Override
    public void save() {
        boolean recordDuration = currentQuestion.isRecordDuration();
        Rows rows = grid.getRows();
        for (Object child : rows.getChildren()) {
            Row row = (Row) child;
            Node toNode = (Node) row.getValue();
            Double weight;

            if (recordDuration) {
                weight = ((RelationalDurationRow) row).getDurationValue().doubleValue();
            } else {
                Doublebox doublebox = (Doublebox) row.getChildren().get(1);
                weight = doublebox.getValue();
            }

            if (weight != null && (weight - 0d) > Double.MIN_VALUE) {
                Edge edge = nodeEdgeMap.get(toNode.getId());
                if (edge != null) {
                    if (!edge.getWeight().equals(weight)) {
                        edge.setWeight(weight);
                        edgesToSave.add(edge);
                    }
                    nodeEdgeMap.remove(toNode.getId());
                } else {
                    edge = new Edge();
                    edge.setFromNode(respondent);
                    edge.setToNode(toNode);
                    edge.setType(currentQuestion.getEdgeType());
                    edge.setWeight(weight);
                    edgesToSave.add(edge);
                }
            }
        }
        edgesToDelete.addAll(nodeEdgeMap.values());

        nodeDao.save(respondent);
        edgeDao.save(edgesToSave);
        logger.debug(edgesToSave.size() + " edges saved or updated.");
        edgeDao.delete(edgesToDelete);
        logger.debug(edgesToDelete.size() + " edges deleted.");
    }
}
