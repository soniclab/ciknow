package ciknow.zk.survey.response;

import ciknow.domain.*;
import ciknow.util.EdgeUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.zul.*;

/**
 *
 * @author gyao
 */
public class RelationalRatingMultiple extends AbstractQuestionRelation {

    private static final long serialVersionUID = -8866718361894458348L;
    private static Log logger = LogFactory.getLog(RelationalRatingMultiple.class);
    // state
    private Map<Long, Map<String, Edge>> nodeFieldEdgeMap;

    public RelationalRatingMultiple(Node respondent, Question currentQuestion) {
        super(respondent, currentQuestion);
    }

    public RelationalRatingMultiple(Question currentQuestion) {
        super(currentQuestion);
    }

    @Override
    public void afterCreationComplete() {
        super.afterCreationComplete();

        nodeFieldEdgeMap = new HashMap<Long, Map<String, Edge>>();
        for (Edge edge : questionEdges) {
            addEdgeToMap(edge);
        }

        List<String> ratingLabels = new ArrayList<String>();
        ratingLabels.add("");
        for (Scale scale : currentQuestion.getScales()) {
            String scaleLabel = scale.getLabel();
            ratingLabels.add(scaleLabel);
        }

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


        // body
        Rows rows = new Rows();
        rows.setParent(grid);
        for (Node node : rowNodes) {
            Row row = new Row();
            row.setParent(rows);
            row.setValue(node);

            Label nodeLabel = new Label(node.getLabel());
            nodeLabel.setParent(row);

            for (Field field : currentQuestion.getFields()) {
                Edge edge = getEdgeFromMap(node.getId(), field.getName());
                Scale rating = null;
                if (edge != null) {
                    String value = edge.getAttribute("scale");
                    if (value != null) {
                        String scaleName = Question.getScaleNameFromKey(value);
                        rating = currentQuestion.getScaleByName(scaleName);
                    }
                }

                Listbox listbox = new Listbox();
                listbox.setParent(row);
                listbox.setMold("select");
                listbox.setRows(1);
                listbox.setWidth("90%");
                ListModelList<String> listModel = new ListModelList<String>(ratingLabels);
                if (rating != null) {
                    listModel.addToSelection(rating.getLabel());
                }
                listbox.setModel(listModel);
            }
        }
    }

    @Transactional
    @Override
    public void save() {
        Rows rows = grid.getRows();
        for (Object child : rows.getChildren()) {
            Row row = (Row) child;
            Node toNode = (Node) row.getValue();

            int index = 1;
            for (Field field : currentQuestion.getFields()) {
                Listbox listbox = (Listbox) row.getChildren().get(index);
                Listitem selectedItem = listbox.getSelectedItem();
                String scaleLabel = null;
                if (selectedItem != null) {
                    scaleLabel = (String) selectedItem.getValue();
                }
                Scale selection = currentQuestion.getScaleByLabel(scaleLabel);

                if (selection != null) {
                    Edge edge = getEdgeFromMap(toNode.getId(), field.getName());
                    if (edge != null) {
                        Scale rating = null;
                        String value = edge.getAttribute("scale");
                        if (value != null) {
                            String scaleName = Question.getScaleNameFromKey(value);
                            rating = currentQuestion.getScaleByName(scaleName);
                        }

                        if (!selection.equals(rating)) {
                            edge.setWeight(selection.getValue());
                            edge.setAttribute("scale", currentQuestion.makeScaleKey(selection));
                            edgesToSave.add(edge);
                        }

                        removeEdgeFromMap(edge);
                    } else {
                        edge = new Edge();
                        edge.setFromNode(respondent);
                        edge.setToNode(toNode);
                        edge.setType(currentQuestion.getEdgeTypeWithField(field));
                        edge.setWeight(selection.getValue());
                        edge.setAttribute("scale", currentQuestion.makeScaleKey(selection));
                        edgesToSave.add(edge);
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
        logger.debug(edgesToSave.size() + " edges created or updated.");
        edgeDao.delete(edgesToDelete);
        logger.debug(edgesToDelete.size() + " edges deleted.");
    }

    private void addEdgeToMap(Edge edge) {
        Long toNodeId = edge.getToNode().getId();
        Map<String, Edge> fieldEdgeMap = nodeFieldEdgeMap.get(toNodeId);
        if (fieldEdgeMap == null) {
            fieldEdgeMap = new HashMap<String, Edge>();
            nodeFieldEdgeMap.put(toNodeId, fieldEdgeMap);
        }

        String fieldName = EdgeUtil.getFieldNameFromEdgeType(edge.getType());
        fieldEdgeMap.put(fieldName, edge);
    }

    private void removeEdgeFromMap(Edge edge) {
        Long toNodeId = edge.getToNode().getId();
        Map<String, Edge> fieldEdgeMap = nodeFieldEdgeMap.get(toNodeId);
        if (fieldEdgeMap != null) {
            String fieldName = EdgeUtil.getFieldNameFromEdgeType(edge.getType());
            fieldEdgeMap.remove(fieldName);
        }
    }

    private Edge getEdgeFromMap(Long toNodeId, String fieldName) {
        Map<String, Edge> fieldEdgeMap = nodeFieldEdgeMap.get(toNodeId);
        if (fieldEdgeMap == null) {
            return null;
        }
        return fieldEdgeMap.get(fieldName);
    }
}
