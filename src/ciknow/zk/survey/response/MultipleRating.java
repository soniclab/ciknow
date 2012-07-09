package ciknow.zk.survey.response;

import ciknow.domain.*;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.zul.*;

/**
 *
 * @author gyao
 */
public class MultipleRating extends AbstractQuestionAttribute {

    private static final long serialVersionUID = -7432166145166515730L;
    private static Log logger = LogFactory.getLog(MultipleRating.class);

    public MultipleRating(Question currentQuestion) {
        super(currentQuestion);
    }

    public MultipleRating(Node respondent, Question currentQuestion) {
        super(respondent, currentQuestion);
    }

    @Override
    public void afterCreationComplete() {
        super.afterCreationComplete();

        // get rating labels
        List<String> ratingLabels = new ArrayList<String>();
        ratingLabels.add("");
        for (Scale scale : currentQuestion.getScales()) {
            ratingLabels.add(scale.getLabel());
        }

        // header
        Columns columns = new Columns();
        columns.setParent(grid);

        Column column = new Column("");
        column.setWidth(getFirstColumnWidth() + "px");
        column.setParent(columns);

        for (TextField textField : currentQuestion.getTextFields()) {
            column = new Column(textField.getLabel());
            column.setParent(columns);
            column.setAlign("center");
        }

        // body
        Rows rows = new Rows();
        rows.setParent(grid);
        for (Field field : currentQuestion.getFields()) {
            Row row = new Row();
            row.setParent(rows);
            row.setValue(field);

            Label fieldLabel = new Label(field.getLabel());
            fieldLabel.setParent(row);

            for (TextField textField : currentQuestion.getTextFields()) {
                String fieldKey = currentQuestion.makeFieldsKey(field, textField);
                String value = respondent.getAttribute(fieldKey);
                Scale rating = null;
                if (value != null) {
                    String scaleName = Question.getScaleNameFromKey(value);
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
        for (Object child : grid.getRows().getChildren()) {
            Row row = (Row) child;
            Field field = (Field) row.getValue();

            int index = 1;
            for (TextField textField : currentQuestion.getTextFields()) {
                Listbox listbox = (Listbox) row.getChildren().get(index);

                String fieldKey = currentQuestion.makeFieldsKey(field, textField);
                Listitem selectedItem = listbox.getSelectedItem();
                String ratingLabel = null;
                if (selectedItem != null) {
                    ratingLabel = (String) selectedItem.getValue();
                }
                if (ratingLabel == null || ratingLabel.isEmpty()) {
                    respondent.getAttributes().remove(fieldKey);
                } else {
                    Scale rating = currentQuestion.getScaleByLabel(ratingLabel);
                    String scaleKey = currentQuestion.makeScaleKey(rating);
                    respondent.setAttribute(fieldKey, scaleKey);
                }

                index++;
            }
        }

        logger.debug("save to database.");
        nodeDao.save(respondent);
    }
}
