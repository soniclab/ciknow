package ciknow.zk.survey.response;

import ciknow.domain.Edge;
import ciknow.domain.Node;
import ciknow.domain.Question;
import ciknow.domain.Scale;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.*;

/**
 *
 * @author gyao
 */
public class RelationalRating extends AbstractQuestionRelation {

    private static final long serialVersionUID = -6628030462827877397L;
    private static Log logger = LogFactory.getLog(RelationalRating.class);
    // state
    private Map<Long, Edge> nodeEdgeMap;

    public RelationalRating(Question currentQuestion) {
        super(currentQuestion);
    }

    public RelationalRating(Node respondent, Question currentQuestion) {
        super(respondent, currentQuestion);
    }

    @Override
    public void afterCreationComplete() {
        super.afterCreationComplete();

        nodeEdgeMap = new HashMap<Long, Edge>();
        for (Edge edge : questionEdges) {
            nodeEdgeMap.put(edge.getToNode().getId(), edge);
        }
        ClearEventListener listener = new ClearEventListener();

        // header
        Columns columns = new Columns();
        columns.setParent(grid);
        columns.setSizable(true);

        Column column = new Column();
        column.setParent(columns);
        column.setWidth(getFirstColumnWidth() + "px");

        for (Scale scale : currentQuestion.getScales()) {
            column = new Column(scale.getLabel());
            column.setParent(columns);
            column.setAlign("center");
        }

        columns.appendChild(new Column());


        // body
        Rows rows = new Rows();
        rows.setParent(grid);
        for (Node node : rowNodes) {
            Edge edge = nodeEdgeMap.get(node.getId());
            Scale rating = null;
            if (edge != null) {
                String scaleKey = edge.getAttribute("scale");
                String scaleName = Question.getScaleNameFromKey(scaleKey);
                rating = currentQuestion.getScaleByName(scaleName);
            }

            Row row = new Row();
            row.setParent(rows);
            row.setValue(node);

            // Node label 
            Label nodeLabel = new Label(node.getLabel());
            //nodeLabel.setWidth(getFirstColumnWidth() + "px");
            nodeLabel.setParent(row);

            // Scales
            Radiogroup radiogroup = new Radiogroup();
            radiogroup.setId(node.getUsername()); // to be lookup by Component.getFellow()

            for (Scale scale : currentQuestion.getScales()) {
                Radio radio = new Radio();
                radio.setParent(row);
                radio.setRadiogroup(radiogroup);
                if (rating != null) {
                    radio.setChecked(scale.equals(rating));
                }
            }

            // Clear button
            Button button = new Button("Clear");
            button.setParent(row);
            button.addEventListener("onClick", listener);

            // Radio group
            radiogroup.setParent(row);
        }
    }

    @Transactional
    @Override
    public void save() {
        Rows rows = grid.getRows();
        for (Object child : rows.getChildren()) {
            Row row = (Row) child;
            Node toNode = (Node) row.getValue();
            Edge edge = nodeEdgeMap.get(toNode.getId());

            int index = 1;
            for (Scale scale : currentQuestion.getScales()) {
                Radio radio = (Radio) row.getChildren().get(index);
                if (radio.isChecked()) {
                    String scaleKey = currentQuestion.makeScaleKey(scale);
                    if (edge == null) {	// create edge
                        edge = new Edge();
                        edge.setFromNode(respondent);
                        edge.setToNode(toNode);
                        edge.setType(currentQuestion.getEdgeType());
                        edge.setWeight(scale.getValue());
                        edge.setAttribute("scale", scaleKey);
                        edgesToSave.add(edge);
                    } else {
                        String currentScaleKey = edge.getAttribute("scale");
                        if (!scaleKey.equals(currentScaleKey)) {	// update edge
                            edge.setWeight(scale.getValue());
                            edge.setAttribute("scale", scaleKey);
                            edgesToSave.add(edge);
                        }
                        nodeEdgeMap.remove(toNode.getId());
                    }
                    break;
                }
                index++;
            }
        }
        edgesToDelete.addAll(nodeEdgeMap.values());

        nodeDao.save(respondent);
        edgeDao.save(edgesToSave);
        logger.debug(edgesToSave.size() + " edges created or updated.");
        edgeDao.delete(edgesToDelete);
        logger.debug(edgesToDelete.size() + " edges deleted.");
    }

    private class ClearEventListener implements EventListener<Event> {

        @Override
        public void onEvent(Event event) {
            Button button = (Button) event.getTarget();
            Row row = (Row) button.getParent();
            Node toNode = (Node) row.getValue();
            logger.debug("Clearing selection for node: " + toNode.getLabel());

            Radiogroup radiogroup = (Radiogroup) row.getFellow(toNode.getUsername());
            radiogroup.setSelectedItem(null);
        }
    }
}
