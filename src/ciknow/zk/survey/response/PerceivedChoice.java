package ciknow.zk.survey.response;

import ciknow.domain.Edge;
import ciknow.domain.Field;
import ciknow.domain.Node;
import ciknow.domain.Question;

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
public class PerceivedChoice extends AbstractQuestionRelation {

    private static final long serialVersionUID = -6746989954087500896L;
    private static Log logger = LogFactory.getLog(PerceivedChoice.class);
    // state
    private Map<Long, Map<String, Edge>> nodeFieldEdgeMap;
    private Map<String, Node> tagMap; // fieldName --> Node/Tag
    private boolean singleChoicePerLine;

    public PerceivedChoice(Question currentQuestion) {
        super(currentQuestion);
    }

    public PerceivedChoice(Node respondent, Question currentQuestion) {
        super(respondent, currentQuestion);
    }

    @Override
    public void afterCreationComplete() {
        super.afterCreationComplete();

        nodeFieldEdgeMap = new HashMap<Long, Map<String, Edge>>();
        for (Edge edge : questionEdges) {
            addEdgeToMap(edge);
        }

        tagMap = new HashMap<String, Node>();
        for (Node tag : questionTags) {
            String fieldName = Question.getFieldNameFromTagName(tag.getUsername());
            tagMap.put(fieldName, tag);
        }

        singleChoicePerLine = currentQuestion.isSingleChoicePerLine();

        ClearEventListener listener = new ClearEventListener();

        // header
        Columns columns = new Columns();
        columns.setParent(grid);
        columns.setSizable(true);

        Column column = new Column();
        column.setParent(columns);
        column.setWidth(getFirstColumnWidth() + "px");

        for (Field field : currentQuestion.getFields()) {
            column = new Column(field.getLabel());
            column.setParent(columns);
            column.setAlign("center");
        }

        if (singleChoicePerLine) {
            column = new Column();
            column.setParent(columns);
        }


        // body
        Rows rows = new Rows();
        rows.setParent(grid);
        for (Node node : rowNodes) {
            Row row = new Row();
            row.setParent(rows);
            row.setValue(node);

            Label nodeLabel = new Label(node.getLabel());
            nodeLabel.setParent(row);

            Radiogroup radiogroup = new Radiogroup();
            for (Field field : currentQuestion.getFields()) {
                Edge edge = getEdgeFromMap(node.getId(), field.getName());

                if (singleChoicePerLine) {
                    Radio radio = new Radio();
                    radio.setParent(row);
                    radio.setRadiogroup(radiogroup);
                    if (edge != null) {
                        radio.setChecked(true);
                    }
                } else {
                    Checkbox checkbox = new Checkbox();
                    checkbox.setParent(row);
                    if (edge != null) {
                        checkbox.setChecked(true);
                    }
                }
            }

            if (singleChoicePerLine) {
                // Clear button
                Button button = new Button("Clear");
                button.setParent(row);
                button.addEventListener("onClick", listener);

                radiogroup.setId(node.getUsername());
                radiogroup.setParent(row);
            }
        }
    }

    @Transactional
    @Override
    public void save() {
        Rows rows = grid.getRows();
        for (Object child : rows.getChildren()) {
            Row row = (Row) child;
            Node fromNode = (Node) row.getValue();

            int index = 1;
            for (Field field : currentQuestion.getFields()) {
                Edge edge = getEdgeFromMap(fromNode.getId(), field.getName());

                if (singleChoicePerLine) {
                    Radio radio = (Radio) row.getChildren().get(index);
                    if (radio.isChecked()) {
                        if (edge != null) {
                            removeEdgeFromMap(edge);
                        } else {
                            edge = new Edge();
                            edge.setCreator(respondent);
                            edge.setFromNode(fromNode);
                            edge.setToNode(tagMap.get(field.getName()));
                            edge.setType(currentQuestion.getEdgeType());
                            edgesToSave.add(edge);
                        }
                        break;
                    }
                } else {
                    Checkbox checkbox = (Checkbox) row.getChildren().get(index);
                    if (checkbox.isChecked()) {
                        if (edge != null) {
                            removeEdgeFromMap(edge);
                        } else {
                            edge = new Edge();
                            edge.setCreator(respondent);
                            edge.setFromNode(fromNode);
                            edge.setToNode(tagMap.get(field.getName()));
                            edge.setType(currentQuestion.getEdgeType());
                            edgesToSave.add(edge);
                        }
                    }
                }

                index++;
            }
        }

        for (Map<String, Edge> fieldEdgeMap : nodeFieldEdgeMap.values()) {
            edgesToDelete.addAll(fieldEdgeMap.values());
        }


        nodeDao.save(respondent);
        edgeDao.save(edgesToSave);
        logger.debug(edgesToSave.size() + " edges created.");
        edgeDao.delete(edgesToDelete);
        logger.debug(edgesToDelete.size() + " edges deleted.");
    }

    private void addEdgeToMap(Edge edge) {
        Long fromNodeId = edge.getFromNode().getId();
        Map<String, Edge> fieldEdgeMap = nodeFieldEdgeMap.get(fromNodeId);
        if (fieldEdgeMap == null) {
            fieldEdgeMap = new HashMap<String, Edge>();
            nodeFieldEdgeMap.put(fromNodeId, fieldEdgeMap);
        }

        String fieldName = Question.getFieldNameFromTagName(edge.getToNode().getUsername());
        fieldEdgeMap.put(fieldName, edge);
    }

    private void removeEdgeFromMap(Edge edge) {
        Long fromNodeId = edge.getFromNode().getId();
        Map<String, Edge> fieldEdgeMap = nodeFieldEdgeMap.get(fromNodeId);
        if (fieldEdgeMap != null) {
            String fieldName = Question.getFieldNameFromTagName(edge.getToNode().getUsername());
            fieldEdgeMap.remove(fieldName);
        }
    }

    private Edge getEdgeFromMap(Long fromNodeId, String fieldName) {
        Map<String, Edge> fieldEdgeMap = nodeFieldEdgeMap.get(fromNodeId);
        if (fieldEdgeMap == null) {
            return null;
        }
        return fieldEdgeMap.get(fieldName);
    }

    private class ClearEventListener implements EventListener<Event> {

        @Override
        public void onEvent(Event event) {
            Button button = (Button) event.getTarget();
            Row row = (Row) button.getParent();
            Node node = (Node) row.getValue();
            logger.debug("Clearing selection for node: " + node.getLabel());

            Radiogroup radiogroup = (Radiogroup) row.getFellow(node.getUsername());
            radiogroup.setSelectedItem(null);
        }
    }
}
