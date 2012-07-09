package ciknow.zk.survey.response;

import ciknow.domain.Field;
import ciknow.domain.Node;
import ciknow.domain.Question;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.zul.Column;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Rows;

/**
 *
 * @author gyao
 */
public class Duration extends AbstractQuestionAttribute {

    private static final long serialVersionUID = -8337843296044217390L;
    private static Log logger = LogFactory.getLog(Duration.class);

    public Duration(Question currentQuestion) {
        super(currentQuestion);
    }

    public Duration(Node respondent, Question currentQuestion) {
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
        column.setWidth(getFirstColumnWidth() + "px");
        column.setParent(columns);

        String[] headers = {"Year", "Month", "Day", "Hour", "Minute", "Second"};
        for (String header : headers) {
            column = new Column(header);
            column.setWidth("65px");
            column.setAlign("center");
            column.setParent(columns);
        }

        // body
        Rows rows = new Rows();
        rows.setParent(grid);
        for (Field field : currentQuestion.getFields()) {
            String fieldKey = currentQuestion.makeFieldKey(field);
            String value = respondent.getAttribute(fieldKey);
            Long longValue = null;
            if (value != null) {
                try {
                    longValue = Long.parseLong(value);
                } catch (NumberFormatException nfe) {
                    logger.error("respondent: " + respondent.getLabel());
                    logger.error("currentQuestion: " + currentQuestion.getLabel());
                    logger.error("field: " + field.getLabel() + ", value: " + value);
                    logger.error("ErrorMessage: " + nfe.getMessage());
                    nfe.printStackTrace();
                    Messagebox.show("Invalid data for field: " + field.getLabel() + ", value=" + value);
                }
            }

            if (longValue == null) {
                longValue = 0L;
            }
            DurationRow row = new DurationRow(field, longValue);
            row.setParent(rows);
        }
    }

    @Transactional
    @Override
    public void save() {
        for (Object child : grid.getRows().getChildren()) {
            DurationRow row = (DurationRow) child;
            Field field = row.field;
            String fieldKey = currentQuestion.makeFieldKey(field);
            Long longValue = row.getDurationValue();
            if (longValue != 0L) {
                respondent.setAttribute(fieldKey, longValue.toString());
            } else {
                respondent.getAttributes().remove(fieldKey);
            }
        }

        logger.debug("save to database.");
        nodeDao.save(respondent);
    }
}
