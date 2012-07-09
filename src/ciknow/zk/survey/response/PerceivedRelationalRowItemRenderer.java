package ciknow.zk.survey.response;

import ciknow.domain.Edge;
import ciknow.domain.Node;
import ciknow.domain.Question;
import ciknow.domain.Scale;
import org.zkoss.zul.*;

/**
 *
 * @author gyao
 */
public class PerceivedRelationalRowItemRenderer implements RowRenderer<PerceivedRelationalRowItem> {

    @Override
    public void render(Row row, PerceivedRelationalRowItem data, int index) throws Exception {
        row.setValue(data.fromNode);

        // first column
        Label nodeLabel = new Label(data.fromNode.getLabel());
        nodeLabel.setParent(row);

        // one column per toNode
        for (Node toNode : data.toNodes) {
            Edge edge = null;
            if (data.toNodeEdgeMap != null) {
                edge = data.toNodeEdgeMap.get(toNode.getId());
            }

            Scale rating = null;
            if (edge != null) {
                String value = edge.getAttribute("scale");
                if (value != null) {
                    String scaleName = Question.getScaleNameFromKey(value);
                    rating = data.question.getScaleByName(scaleName);
                }
            }

            Listbox listbox = new Listbox();
            listbox.setParent(row);
            listbox.setMold("select");
            listbox.setRows(1);
            listbox.setWidth("90%");
            ListModelList<String> listModel = new ListModelList<String>(data.ratingLabels);
            if (rating != null) {
                listModel.addToSelection(rating.getLabel());
            }
            listbox.setModel(listModel);
        }
    }
}
