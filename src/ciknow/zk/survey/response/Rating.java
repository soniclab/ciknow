package ciknow.zk.survey.response;

import ciknow.domain.Field;
import ciknow.domain.Node;
import ciknow.domain.Question;
import ciknow.domain.Scale;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.*;

/**
 *
 * @author gyao
 */
public class Rating extends AbstractQuestionAttribute {

    private static final long serialVersionUID = 580187432656051477L;
    private static Log logger = LogFactory.getLog(Rating.class);

    public Rating(Node respondent, Question currentQuestion) {
        super(respondent, currentQuestion);
    }

    public Rating(Question currentQuestion) {
        super(currentQuestion);
    }

    @Override
    public void afterCreationComplete() {
        super.afterCreationComplete();

        ClearEventListener listener = new ClearEventListener();

        // header
        Columns columns = new Columns();
        columns.setParent(grid);
        columns.setSizable(true);

        Column column = new Column();
        column.setParent(columns);
        column.setWidth(getFirstColumnWidth() + "px");

        if (currentQuestion.displayRatingAsDropdownList()) {
            List<String> ratingLabels = new ArrayList<String>();
            ratingLabels.add("");
            for (Scale scale : currentQuestion.getScales()) {
                String scaleLabel = scale.getLabel();
                ratingLabels.add(scaleLabel);
            }

            Rows rows = new Rows();
            rows.setParent(grid);
            for (Field field : currentQuestion.getFields()) {
                Row row = new Row();
                row.setParent(rows);
                row.setValue(field);

                Label fieldLabel = new Label(field.getLabel());
                fieldLabel.setParent(row);

                Listbox listbox = new Listbox();
                listbox.setParent(row);
                listbox.setMold("select");
                listbox.setRows(1);
                ListModelList<String> listModel = new ListModelList<String>(ratingLabels);
                Scale rating = getRating(field);
                if (rating != null) {
                    listModel.addToSelection(rating.getLabel());
                }
                listbox.setModel(listModel);
            }
        } else {
            // more header		
            for (Scale scale : currentQuestion.getScales()) {
                column = new Column(scale.getLabel());
                column.setParent(columns);
                column.setAlign("center");
            }
            column = new Column("");
            column.setParent(columns);

            // body			
            Rows rows = new Rows();
            rows.setParent(grid);
            for (Field field : currentQuestion.getFields()) {
                Row row = new Row();
                row.setParent(rows);
                row.setValue(field);

                Label fieldLabel = new Label(field.getLabel());
                fieldLabel.setParent(row);

                Scale rating = getRating(field);
                Radiogroup radiogroup = new Radiogroup();
                radiogroup.setId(field.getName());
                for (Scale scale : currentQuestion.getScales()) {
                    Radio radio = new Radio();
                    radio.setParent(row);
                    radio.setRadiogroup(radiogroup);
                    radio.setValue(scale.getName());
                    if (rating != null && rating.equals(scale)) {
                        radio.setSelected(true);
                    }
                }

                // last column
                Hlayout hbox = new Hlayout();
                hbox.setParent(row);
                Button button = new Button("Clear");
                button.setParent(hbox);
                button.addEventListener("onClick", listener);
                radiogroup.setParent(hbox);
            }
        }
    }

    @Transactional
    @Override
    public void save() {
        Rows rows = grid.getRows();
        for (Object child : rows.getChildren()) {
            Row row = (Row) child;
            Field field = (Field) row.getValue();

            // remove old answers (if any)
            String fieldKey = currentQuestion.makeFieldKey(field);
            respondent.getAttributes().remove(fieldKey);

            Scale scale = null;
            if (currentQuestion.displayRatingAsDropdownList()) {
                Listbox listbox = (Listbox) row.getChildren().get(1);
                ListModelList<Object> listModel = (ListModelList<Object>) listbox.getModel();
                Iterator<Object> itr = listModel.getSelection().iterator();
                if (itr.hasNext()) {
                    String scaleLabel = (String) itr.next();
                    scale = currentQuestion.getScaleByLabel(scaleLabel);
                }
            } else {
                Radiogroup radiogroup = (Radiogroup) row.getFellow(field.getName());
                Radio radio = radiogroup.getSelectedItem();
                if (radio != null) {
                    scale = currentQuestion.getScaleByName(radio.getValue());
                }
            }

            if (scale != null) {
                String scaleKey = currentQuestion.makeScaleKey(scale);
                respondent.setAttribute(fieldKey, scaleKey);
            }
        }

        // save to database
        nodeDao.save(respondent);
    }

    private Scale getRating(Field field) {
        Scale scale = null;
        String fieldKey = currentQuestion.makeFieldKey(field);
        String scaleKey = respondent.getAttribute(fieldKey);
        if (scaleKey != null) {
            String scaleName = Question.getScaleNameFromKey(scaleKey);
            scale = currentQuestion.getScaleByName(scaleName);
        }
        /*
        else {
            scale = currentQuestion.getDefaultRating();
        }
        */
        
        return scale;
    }

    private class ClearEventListener implements EventListener<Event> {

        @Override
        public void onEvent(Event event) {
            Button button = (Button) event.getTarget();
            Row row = (Row) button.getParent().getParent();
            Field field = (Field) row.getValue();
            logger.debug("Clearing selection for field: " + field.getLabel());

            Radiogroup radiogroup = (Radiogroup) row.getFellow(field.getName());
            radiogroup.setSelectedItem(null);
        }
    }
}
