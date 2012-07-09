package ciknow.zk.survey.response;

import ciknow.domain.Node;
import ciknow.domain.Question;
import ciknow.domain.TextField;
import ciknow.zk.survey.response.AbstractQuestionAttribute;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.zul.*;

/**
 *
 * @author gyao
 */
public class Text extends AbstractQuestionAttribute {

    private static final long serialVersionUID = 1384180725967069358L;
    private static Log logger = LogFactory.getLog(Text.class);

    public Text(Question currentQuestion) {
        super(currentQuestion);
    }

    public Text(Node respondent, Question currentQuestion) {
        super(respondent, currentQuestion);
    }

    @Override
    public void afterCreationComplete() {
        super.afterCreationComplete();

        // header
        Columns columns = new Columns();
        columns.setParent(grid);
        columns.setSizable(true);

        Column column = new Column();
        column.setParent(columns);
        column.setWidth(getFirstColumnWidth() + "px");

        // body
        Rows rows = new Rows();
        rows.setParent(grid);
        for (TextField field : currentQuestion.getTextFields()) {
            String fieldKey = currentQuestion.makeTextFieldKey(field);
            String value = respondent.getAttribute(fieldKey);
            if (value == null) {
                value = "";
            }

            Row row = new Row();
            row.setParent(rows);
            row.setValue(field);

            Label fieldLabel = new Label(field.getLabel());
            fieldLabel.setParent(row);

            Textbox textbox = new Textbox();
            textbox.setParent(row);
            textbox.setWidth("99%");
            if (field.getLarge()) {
                textbox.setRows(10);
            }
            textbox.setValue(value);
        }
    }

    @Transactional
    @Override
    public void save() {
        for (Object child : grid.getRows().getChildren()) {
            Row row = (Row) child;
            TextField field = (TextField) row.getValue();
            String fieldKey = currentQuestion.makeTextFieldKey(field);
            Textbox textbox = (Textbox) row.getChildren().get(1);
            String value = textbox.getValue().trim();

            if (value.isEmpty()) {
                respondent.getAttributes().remove(fieldKey);
            } else {
                respondent.setAttribute(fieldKey, value);
            }
        }

        logger.debug("save to database.");
        nodeDao.save(respondent);
    }
}
