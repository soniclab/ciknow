package ciknow.zk.survey.response;

import ciknow.domain.Edge;
import ciknow.domain.Node;
import ciknow.domain.Question;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;

/**
 *
 * @author gyao
 */
public class RelationalChoice extends AbstractQuestionRelation {

    private static final long serialVersionUID = 7599581190860201991L;
    private static Log logger = LogFactory.getLog(RelationalChoice.class);
    // state
    private Map<Long, Edge> nodeEdgeMap;

    public RelationalChoice(Question currentQuestion) {
        super(currentQuestion);
    }

    public RelationalChoice(Node respondent, Question currentQuestion) {
        super(respondent, currentQuestion);
    }

    @Override
    public void afterCreationComplete() {
        super.afterCreationComplete();

        if (currentQuestion.showSelectAll()) {
            selectAllBox.setVisible(true);
        }

        nodeEdgeMap = new HashMap<Long, Edge>();
        for (Edge edge : questionEdges) {
            nodeEdgeMap.put(edge.getToNode().getId(), edge);
        }

        Rows rows = new Rows();
        rows.setParent(grid);
        for (Node node : rowNodes) {
            Row row = new Row();
            row.setParent(rows);
            row.setValue(node);

            Checkbox checkbox = new Checkbox(node.getLabel());
            checkbox.setParent(row);
            if (nodeEdgeMap.containsKey(node.getId())) {
                checkbox.setChecked(true);
            }
        }
    }

    @Listen("onClick = #selectAllBox")
    public void selectAll() {
        Rows rows = grid.getRows();
        for (Object child : rows.getChildren()) {
            Row row = (Row) child;
            Checkbox checkbox = (Checkbox) row.getChildren().get(0);
            checkbox.setChecked(selectAllBox.isChecked());
        }
    }

    @Transactional
    @Override
    public void save() {
        Rows rows = grid.getRows();
        for (Object child : rows.getChildren()) {
            Row row = (Row) child;
            Node toNode = (Node) row.getValue();
            Checkbox checkbox = (Checkbox) row.getChildren().get(0);
            if (checkbox.isChecked()) {
                if (nodeEdgeMap.containsKey(toNode.getId())) {
                    nodeEdgeMap.remove(toNode.getId());
                } else {
                    Edge edge = new Edge();
                    edge.setFromNode(respondent);
                    edge.setToNode(toNode);
                    edge.setType(currentQuestion.getEdgeType());
                    edgesToSave.add(edge);
                }
            }
        }
        edgesToDelete.addAll(nodeEdgeMap.values());

        nodeDao.save(respondent);
        edgeDao.save(edgesToSave);
        logger.debug(edgesToSave.size() + " edges created.");
        edgeDao.delete(edgesToDelete);
        logger.debug(edgesToDelete.size() + " edges deleted.");
    }
}
