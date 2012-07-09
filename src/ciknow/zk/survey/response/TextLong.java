package ciknow.zk.survey.response;

import ciknow.domain.Field;
import ciknow.domain.Node;
import ciknow.domain.Question;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.zul.*;

/**
 *
 * @author gyao
 */
public class TextLong extends AbstractQuestionAttribute {

    private static final long serialVersionUID = 493401595847823025L;
    private static Log logger = LogFactory.getLog(TextLong.class);

    public TextLong(Question currentQuestion) {
        super(currentQuestion);
    }

    public TextLong(Node respondent, Question currentQuestion) {
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
        for (Field field : currentQuestion.getFields()) {
            String fieldKey = currentQuestion.makeFieldKey(field);
            String value = respondent.getLongAttribute(fieldKey);
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
            textbox.setRows(10);
            textbox.setValue(value);
        }
    }

    @Transactional
    @Override
    public void save() {
        for (Object child : grid.getRows().getChildren()) {
            Row row = (Row) child;
            Field field = (Field) row.getValue();
            String fieldKey = currentQuestion.makeFieldKey(field);
            Textbox textbox = (Textbox) row.getChildren().get(1);
            String value = textbox.getValue().trim();

            if (value.isEmpty()) {
                respondent.getLongAttributes().remove(fieldKey);
            } else {
                respondent.setLongAttribute(fieldKey, value);
            }
        }

        logger.debug("save to database.");
        nodeDao.save(respondent);
    }
}
