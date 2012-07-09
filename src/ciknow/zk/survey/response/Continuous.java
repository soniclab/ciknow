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
public class Continuous extends AbstractQuestionAttribute {

    private static final long serialVersionUID = 6350405922562172893L;
    private static Log logger = LogFactory.getLog(Continuous.class);

    public Continuous(Node respondent, Question currentQuestion) {
        super(respondent, currentQuestion);
    }

    public Continuous(Question currentQuestion) {
        super(currentQuestion);
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
            String value = respondent.getAttribute(fieldKey);
            Double doubleValue = null;
            if (value != null) {
                try {
                    doubleValue = Double.parseDouble(value);
                } catch (NumberFormatException nfe) {
                    logger.error("respondent: " + respondent.getLabel());
                    logger.error("currentQuestion: " + currentQuestion.getLabel());
                    logger.error("field: " + field.getLabel() + ", value: " + value);
                    logger.error("ErrorMessage: " + nfe.getMessage());
                    nfe.printStackTrace();
                    Messagebox.show("Invalid data for field: " + field.getLabel() + ", value=" + value);
                }
            }

            Row row = new Row();
            row.setParent(rows);
            row.setValue(field);

            Label fieldLabel = new Label(field.getLabel());
            fieldLabel.setParent(row);

            Doublebox doublebox = new Doublebox();
            doublebox.setParent(row);
            doublebox.setWidth("99%");
            doublebox.setValue(doubleValue);
        }
    }

    @Transactional
    @Override
    public void save() {
        for (Object child : grid.getRows().getChildren()) {
            Row row = (Row) child;
            Field field = (Field) row.getValue();
            String fieldKey = currentQuestion.makeFieldKey(field);
            Doublebox doublebox = (Doublebox) row.getChildren().get(1);
            Double doubleValue = doublebox.getValue();

            if (doubleValue == null) {
                respondent.getAttributes().remove(fieldKey);
            } else {
                respondent.setAttribute(fieldKey, doubleValue.toString());
            }
        }

        logger.debug("save to database.");
        nodeDao.save(respondent);
    }
}
