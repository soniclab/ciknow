package ciknow.zk.survey.response;

import ciknow.domain.*;

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
public class PerceivedRating extends AbstractQuestionRelation {

    private static final long serialVersionUID = -6224835688605400431L;
    private static Log logger = LogFactory.getLog(PerceivedRating.class);
    // state
    private Map<Long, Map<String, Edge>> nodeFieldEdgeMap;
    private Map<String, Map<String, Node>> tagMap; // fieldName --> scaleName --> Node/Tag

    public PerceivedRating(Question currentQuestion) {
        super(currentQuestion);
    }

    public PerceivedRating(Node respondent, Question currentQuestion) {
        super(respondent, currentQuestion);
    }

    @Override
    public void afterCreationComplete() {
        super.afterCreationComplete();

        nodeFieldEdgeMap = new HashMap<Long, Map<String, Edge>>();
        for (Edge edge : questionEdges) {
            addEdgeToMap(edge);
        }

        tagMap = new HashMap<String, Map<String, Node>>();
        for (Node tag : questionTags) {
            String name = tag.getUsername();
            String fieldName = Question.getFieldNameFromTagName(name);
            String scaleName = Question.getScaleNameFromTagName(name);
            Map<String, Node> m = tagMap.get(fieldName);
            if (m == null) {
                m = new HashMap<String, Node>();
                tagMap.put(fieldName, m);
            }
            m.put(scaleName, tag);
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
                    String name = edge.getToNode().getUsername();
                    String scaleName = Question.getScaleNameFromTagName(name);
                    rating = currentQuestion.getScaleByName(scaleName);
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
            for (Field field : currentQuestion.getFields()) {
                Edge edge = getEdgeFromMap(fromNode.getId(), field.getName());

                Listbox listbox = (Listbox) row.getChildren().get(index);
                Listitem item = listbox.getSelectedItem();
                String scaleLabel = null;
                if (item != null) {
                    scaleLabel = (String) item.getValue();
                }
                Scale selection = currentQuestion.getScaleByLabel(scaleLabel);

                if (selection != null) {
                    if (edge != null) {
                        String scaleName = Question.getScaleNameFromTagName(edge.getToNode().getUsername());
                        Scale rating = currentQuestion.getScaleByName(scaleName);
                        if (!selection.equals(rating)) {
                            edge.setToNode(tagMap.get(field.getName()).get(selection.getName()));
                            edgesToSave.add(edge);
                        }
                        removeEdgeFromMap(edge);
                    } else {
                        edge = new Edge();
                        edge.setCreator(respondent);
                        edge.setFromNode(fromNode);
                        edge.setToNode(tagMap.get(field.getName()).get(selection.getName()));
                        edge.setType(currentQuestion.getEdgeType());
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
}
