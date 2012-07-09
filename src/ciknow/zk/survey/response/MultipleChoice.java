package ciknow.zk.survey.response;

import ciknow.domain.Field;
import ciknow.domain.Node;
import ciknow.domain.Question;
import ciknow.domain.TextField;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.zul.*;

/**
 *
 * @author gyao
 */
public class MultipleChoice extends AbstractQuestionAttribute {

    private static final long serialVersionUID = 432414436557941817L;
    private static Log logger = LogFactory.getLog(MultipleChoice.class);

    public MultipleChoice(Question currentQuestion) {
        super(currentQuestion);
    }

    public MultipleChoice(Node respondent, Question currentQuestion) {
        super(respondent, currentQuestion);
    }

    @Override
    public void afterCreationComplete() {
        super.afterCreationComplete();

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

                Checkbox checkbox = new Checkbox();
                if (value != null) {
                    checkbox.setChecked(true);
                }
                checkbox.setParent(row);
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
                Checkbox checkbox = (Checkbox) row.getChildren().get(index);

                String fieldKey = currentQuestion.makeFieldsKey(field, textField);
                if (checkbox.isChecked()) {
                    respondent.setAttribute(fieldKey, "1");
                } else {
                    respondent.getAttributes().remove(fieldKey);
                }

                index++;
            }
        }

        logger.debug("save to database.");
        nodeDao.save(respondent);
    }
}
