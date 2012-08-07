package ciknow.zk.survey.response;

import ciknow.domain.Field;
import ciknow.domain.Node;
import ciknow.domain.Question;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zul.*;

/**
 *
 * @author gyao
 */
public class Choice extends AbstractQuestionAttribute {

    private static final long serialVersionUID = 3158405627505482164L;
    private static Log logger = LogFactory.getLog(Choice.class);
    private int choiceLimit;
    private List<Field> choices;
    private Listbox listbox;
    private Radiogroup radioGroup;

    public Choice(Question currentQuestion) {
        super(currentQuestion);
    }

    public Choice(Node respondent, Question currentQuestion) {
        super(respondent, currentQuestion);
    }

    @Override
    public void afterCreationComplete() {
        super.afterCreationComplete();
        Component questionArea = grid.getParent();

        choices = getChoices();
        if (currentQuestion.isSingleChoice()) {
            if (currentQuestion.showSingleChoiceAsList()) {
                grid.setParent(null);

                listbox = new Listbox();
                listbox.setParent(questionArea);
                listbox.setId("listbox");
                listbox.setMold("select");
                listbox.setRows(1);

                ListModelList<String> listModel = new ListModelList<String>();
                listModel.add("");
                for (Field field : currentQuestion.getFields()) {
                    listModel.add(field.getLabel());
                }
                if (!choices.isEmpty()) {
                    listModel.addToSelection(choices.get(0).getLabel());
                }
                listbox.setModel(listModel);
            } else {
                radioGroup = new Radiogroup();
                radioGroup.setId("radioGroup");

                Rows rows = new Rows();
                rows.setParent(grid);
                for (Field field : currentQuestion.getFields()) {
                    Row row = new Row();
                    row.setParent(rows);
                    Radio radio = new Radio();
                    radio.setParent(row);
                    radio.setLabel(field.getLabel());
                    radio.setValue(field.getName());
                    radio.setRadiogroup(radioGroup);
                    if (choices.contains(field)) {
                        radio.setChecked(true);
                    }
                }

                Hlayout hbox = new Hlayout();
                questionArea.insertBefore(hbox, bottomSpacer);
                Button button = new Button("Clear");
                button.addEventListener("onClick", new ClearEventListener());
                button.setParent(hbox);
                radioGroup.setParent(hbox);
            }
        } else {
            choiceLimit = currentQuestion.getMaxChoice();
            if (currentQuestion.showSelectAll()) {
                if (choiceLimit == currentQuestion.getFields().size()) {
                    selectAllBox.setVisible(true);
                }
            }

            Rows rows = new Rows();
            rows.setParent(grid);
            for (Field field : currentQuestion.getFields()) {
                Row row = new Row();
                row.setParent(rows);
                Checkbox checkbox = new Checkbox();
                checkbox.setParent(row);
                checkbox.setLabel(field.getLabel());
                checkbox.setValue(field.getName());
                if (choices.contains(field)) {
                    checkbox.setChecked(true);
                }
            }
        }
    }

    @Listen("onClick = #selectAllBox")
    public void selectAll() {
        Rows rows = grid.getRows();
        for (Object child : rows.getChildren()) {
            Row row = (Row) child;
            Checkbox checkbox = (Checkbox) row.getChildren().get(0);
            checkbox.setChecked(selectAllBox.isChecked());
        }
    }

    @Override
    public boolean validate() { 
    	String msg = "This question is mandatory (answer required)";
    	boolean mandatory = currentQuestion.isMandatory();
    	if (currentQuestion.isSingleChoice()) {
    		if (!mandatory) return true;
    		
    		Field selection = null;
    		if (currentQuestion.showSingleChoiceAsList()){
    			ListModelList<Object> listModel = (ListModelList<Object>) listbox.getModel();
                Iterator<Object> itr = listModel.getSelection().iterator();                
                if (itr.hasNext()) {
                    String fieldLabel = (String) itr.next();
                    selection = currentQuestion.getFieldByLabel(fieldLabel);
                }
    		} else {
                Radio radio = radioGroup.getSelectedItem();
                if (radio != null) {
                    String fieldName = radio.getValue();
                    selection = currentQuestion.getFieldByName(fieldName);
                }
    		}
    		
			if (selection == null) {
				Messagebox.show(msg);  
				return false;
			} else return true;
    	}
    	
        Rows rows = grid.getRows();
        int count = 0;
        for (Object child : rows.getChildren()) {
            Row row = (Row) child;
            Checkbox checkbox = (Checkbox) row.getChildren().get(0);
            if (checkbox.isChecked()) {
                count++;
            }
        }

        if (mandatory && count == 0) {
        	Messagebox.show(msg);
        	return false;
        }
        
        if (choiceLimit >= 0 && choiceLimit < count) {
        	logger.info("choiceLimit: " + choiceLimit + " , count: " + count);
            Messagebox.show("You have made more selections than allowed.");
            return false;
        }
        
        return true;
    }
    
    @Transactional
    @Override
    public void save() {

        // remove old answers (if any)
        for (Field field : choices) {
            String fieldKey = currentQuestion.makeFieldKey(field);
            respondent.getAttributes().remove(fieldKey);
        }

        // collect new answers
        if (currentQuestion.isSingleChoice()) {
            Field selection = null;
            if (currentQuestion.showSingleChoiceAsList()) {
                ListModelList<Object> listModel = (ListModelList<Object>) listbox.getModel();
                Iterator<Object> itr = listModel.getSelection().iterator();
                if (itr.hasNext()) {
                    String fieldLabel = (String) itr.next();
                    selection = currentQuestion.getFieldByLabel(fieldLabel);
                }
            } else {
                Radio radio = radioGroup.getSelectedItem();
                if (radio != null) {
                    String fieldName = radio.getValue();
                    selection = currentQuestion.getFieldByName(fieldName);
                }
            }

            if (selection != null) {
                String fieldKey = currentQuestion.makeFieldKey(selection);
                respondent.setAttribute(fieldKey, "1");
                logger.debug("selection: " + selection.getLabel());
            } else {
                logger.warn("Respondent(username=" + respondent.getUsername()
                        + ") does not make selection on question(shortName=" + currentQuestion.getShortName() + ")");
            }
        } else {
            Rows rows = grid.getRows();
            int count = 0;
            for (Object child : rows.getChildren()) {
                Row row = (Row) child;
                Checkbox checkbox = (Checkbox) row.getChildren().get(0);
                if (checkbox.isChecked()) {
                    String fieldName = checkbox.getValue();
                    Field selection = currentQuestion.getFieldByName(fieldName);
                    if (selection != null) {
                        String fieldKey = currentQuestion.makeFieldKey(selection);
                        respondent.setAttribute(fieldKey, "1");
                        count++;
                        logger.debug("selection: " + selection.getLabel());
                    }
                }
            }

            if (choiceLimit >= 0 && choiceLimit < count) {
            	logger.info("choiceLimit: " + choiceLimit + " , count: " + count);
                Messagebox.show("You have made more selections than allowed.");
                return;
            }

            if (count == 0) {
                logger.warn("Respondent(username=" + respondent.getUsername()
                        + ") does not make selection on question(shortName=" + currentQuestion.getShortName() + ")");
            }
        }

        // save into database
        nodeDao.save(respondent);
    }

    private List<Field> getChoices() {
        logger.debug("get existing selections...");
        List<Field> _choices = new ArrayList<Field>();
        //Field field = null;
        for (Field field : currentQuestion.getFields()) {
            String fieldKey = currentQuestion.makeFieldKey(field);
            if (respondent.getAttribute(fieldKey) != null) {
                _choices.add(field);
            }
        }

        /*
        if (_choices.isEmpty()) {
            String fieldName = currentQuestion.getAttribute(Constants.DEFAULT_FIELD);
            if (fieldName != null) {
                Field field = currentQuestion.getFieldByName(fieldName);
                if (field != null) {
                    _choices.add(field);
                } else {
                    logger.warn("Field (name=" + fieldName + ") does not exist for question (shortName=" + currentQuestion.getShortName() + ")");
                }
            }
        }
        */
        
        logger.debug("got " + _choices.size() + " existing selections.");
        return _choices;
    }

    private class ClearEventListener implements EventListener<Event> {

        @Override
        public void onEvent(Event event) {
            radioGroup.setSelectedItem(null);
        }
    }
}
