package ciknow.zk.survey.response;

import ciknow.domain.Edge;
import ciknow.domain.Node;
import ciknow.domain.Question;
import ciknow.domain.Scale;

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
public class PerceivedRelationalRating_simple extends AbstractQuestionRelation {

    private static final long serialVersionUID = 1171167239281897595L;
    private static Log logger = LogFactory.getLog(PerceivedRelationalRating_simple.class);
    private Map<Long, Map<Long, Edge>> fromNodeToNodeEdgeMap;

    public PerceivedRelationalRating_simple(Question currentQuestion) {
        super(currentQuestion);
    }

    public PerceivedRelationalRating_simple(Node respondent, Question currentQuestion) {
        super(respondent, currentQuestion);
    }

    @Override
    public void afterCreationComplete() {
        super.afterCreationComplete();

        fromNodeToNodeEdgeMap = new HashMap<Long, Map<Long, Edge>>();
        for (Edge edge : questionEdges) {
            addEdgeToMap(edge);
        }

        List<String> ratingLabels = new ArrayList<String>();
        ratingLabels.add("");
        for (Scale scale : currentQuestion.getScales()) {
            String scaleLabel = scale.getLabel();
            ratingLabels.add(scaleLabel);
        }

        // frozen first column
        Frozen frozen = new Frozen();
        frozen.setColumns(1);
        frozen.setParent(grid);

        // header
        Columns columns = new Columns();
        columns.setParent(grid);
        columns.setSizable(true);

        Column column = new Column();
        column.setParent(columns);
        column.setWidth("150px");

        for (Node toNode : colNodes) {
            column = new Column(toNode.getLabel());
            column.setParent(columns);
            column.setWidth("100px");
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

            for (Node toNode : colNodes) {
                Edge edge = getEdgeFromMap(node.getId(), toNode.getId());

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
            Node fromNode = (Node) row.getValue();

            int index = 1;
            for (Node toNode : colNodes) {
                Listbox listbox = (Listbox) row.getChildren().get(index);
                Listitem selectedItem = listbox.getSelectedItem();
                String scaleLabel = null;
                if (selectedItem != null) {
                    scaleLabel = (String) selectedItem.getValue();
                }
                Scale selection = currentQuestion.getScaleByLabel(scaleLabel);

                if (selection != null) {
                    Edge edge = getEdgeFromMap(fromNode.getId(), toNode.getId());
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
                        edge.setCreator(respondent);
                        edge.setFromNode(fromNode);
                        edge.setToNode(toNode);
                        edge.setType(currentQuestion.getEdgeType());
                        edge.setWeight(selection.getValue());
                        edge.setAttribute("scale", currentQuestion.makeScaleKey(selection));
                        edgesToSave.add(edge);
                    }
                }

                index++;
            }
        }

        for (Map<Long, Edge> toNodeEdgeMap : fromNodeToNodeEdgeMap.values()) {
            edgesToDelete.addAll(toNodeEdgeMap.values());
        }


        nodeDao.save(respondent);
        edgeDao.save(edgesToSave);
        logger.debug(edgesToSave.size() + " edges created or updated.");
        edgeDao.delete(edgesToDelete);
        logger.debug(edgesToDelete.size() + " edges deleted.");
    }

    private void addEdgeToMap(Edge edge) {
        Long fromNodeId = edge.getFromNode().getId();
        Map<Long, Edge> toNodeEdgeMap = fromNodeToNodeEdgeMap.get(fromNodeId);
        if (toNodeEdgeMap == null) {
            toNodeEdgeMap = new HashMap<Long, Edge>();
            fromNodeToNodeEdgeMap.put(fromNodeId, toNodeEdgeMap);
        }

        toNodeEdgeMap.put(edge.getToNode().getId(), edge);
    }

    private void removeEdgeFromMap(Edge edge) {
        Long fromNodeId = edge.getFromNode().getId();
        Map<Long, Edge> toNodeEdgeMap = fromNodeToNodeEdgeMap.get(fromNodeId);
        if (toNodeEdgeMap != null) {
            toNodeEdgeMap.remove(edge.getToNode().getId());
        }
    }

    private Edge getEdgeFromMap(Long fromNodeId, Long toNodeId) {
        Map<Long, Edge> toNodeEdgeMap = fromNodeToNodeEdgeMap.get(fromNodeId);
        if (toNodeEdgeMap == null) {
            return null;
        }
        return toNodeEdgeMap.get(toNodeId);
    }
}
