package ciknow.zk.survey.design;

import java.util.List;

import ciknow.domain.TextField;
import ciknow.domain.Question;
import ciknow.util.GeneralUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 *
 * @author gyao
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class QuestionSettingsTextField extends Div implements IdSpace{

    private static final long serialVersionUID = -5302957071716495757L;
    private static Log logger = LogFactory.getLog(QuestionSettingsTextField.class);
    
    @Wire
    private Listbox listBox;
    @Wire
    private Window win;
    @Wire("#win #caption")
    private Caption caption;
    @Wire("#win #nameBox")
    private Textbox nameBox;
    @Wire("#win #labelBox")
    private Textbox labelBox;
    @Wire("#win #largeBox")
    private Checkbox largeBox;
    
    private Question question;
    private DragDropListener listener;
    
    public QuestionSettingsTextField(Component parent, Question question) {
    	this.setParent(parent);
    	this.setWidth("100%");
    	this.setHeight("100%");
    	this.question = question;
    	
        // create ui from template
        Executions.createComponents("/WEB-INF/zk/survey/design/QuestionSettingsTextField.zul", this, null);

        Selectors.wireVariables(this, this, Selectors.newVariableResolvers(getClass(), Div.class));
        Selectors.wireComponents(this, this, false);
        Selectors.wireEventListeners(this, this);
        
        // hide popups
        win.setParent(null);        
        listener = new DragDropListener();
        for (TextField textField : question.getTextFields()){        	
        	createListitem(textField);
        }
    }
    
    @Listen("onClick = #newBtn")
    public void showNewWinow() {
        caption.setLabel("Add TextField");
        
        int count = question.getTextFields().size() + 1;
        nameBox.setValue("name" + count);
        nameBox.setDisabled(false);
        labelBox.setValue("label" + count);

        win.setParent(this);
    }

    @Listen("onClick = #editBtn")
    public void showEditWindow() {
        Listitem li = listBox.getSelectedItem();
        if (li == null) {
            Messagebox.show("Please select a textField to edit.");
            return;
        }

        TextField selection = (TextField) li.getValue();
        caption.setLabel("Edit TextField");
        nameBox.setValue(selection.getName());
        nameBox.setDisabled(true);
        labelBox.setValue(selection.getLabel());
        largeBox.setChecked(selection.getLarge());
        
        win.setParent(this);
    }

    /*
    @Listen("onChanging = #win #nameBox")
    public void onTextFieldNameChanging(InputEvent e) {
        String value = e.getValue().trim();
        labelBox.setValue(value);

        nameBox.setFocus(true);
        nameBox.setSelectionRange(value.length(), value.length());
    }
	*/
    
    @Listen("onClick = #win #saveBtn")
    public void saveTextField() {
        TextField textField;
        String name = nameBox.getValue().trim();
        if (!GeneralUtil.isValidName(name)) return; 
        String label = labelBox.getValue().trim();
        if (!GeneralUtil.isValidLabel(label)) return; 
        Boolean large = largeBox.isChecked();
        
        textField = question.getTextFieldByName(name);
        if (nameBox.isDisabled()) {
            textField.setLabel(label);
            textField.setLarge(large);
            
            Listitem li = listBox.getSelectedItem();
            Listcell labelCell = (Listcell)li.getChildren().get(1);
            labelCell.setLabel(label);
            Listcell largeCell = (Listcell)li.getChildren().get(2);
            largeCell.setLabel(large.toString());
        } else {
            if (textField != null) {
                Messagebox.show("TextField with name=" + name + " is already exist!");
                return;
            }
            textField = new TextField();
            textField.setName(name);
            textField.setLabel(label);
            textField.setLarge(large);
            question.getTextFields().add(textField);
            
            Listitem li = createListitem(textField);
        	listBox.setSelectedItem(li);
        }

        win.setParent(null);
    }

    @Listen("onClick = #win #cancelBtn")
    public void cancelTextField() {
        win.setParent(null);
    }

    @Listen("onClick = #deleteBtn")
    public void deleteTextField() {
        final Listitem li = listBox.getSelectedItem();
        if (li == null) {
            Messagebox.show("Please select a textField to delete.");
            return;
        }
        final TextField selection = (TextField) li.getValue();
        Messagebox.show("Are you sure to delete textField: " + selection.getLabel(), "", 
        		Messagebox.YES | Messagebox.NO, Messagebox.QUESTION, 0, new EventListener<Event>() {

            @Override
            public void onEvent(Event e) throws Exception {
                if (e.getName().equals(Messagebox.ON_YES)) {
                    question.getTextFields().remove(selection);
                    li.setParent(null);
                }
            }
        });
    }

    private Listitem createListitem(TextField textField){
    	Listitem li = new Listitem();
    	li.setParent(listBox);
    	li.appendChild(new Listcell(textField.getName()));
    	li.appendChild(new Listcell(textField.getLabel()));  
    	li.appendChild(new Listcell(textField.getLarge().toString()));
    	li.setDroppable("textField");
    	li.setDraggable("textField");
    	li.setValue(textField);
    	li.addEventListener("onDrop", listener);
    	return li;
    }
    
    private class DragDropListener implements EventListener<DropEvent>{

		@Override
		public void onEvent(DropEvent e) throws Exception {
	        List<TextField> textFields = question.getTextFields();
	        Listitem dropItem = (Listitem) e.getTarget();
	        TextField dropTextField = dropItem.getValue();
	        int dropIndex = textFields.indexOf(dropTextField);
	        Listitem dragItem = (Listitem) e.getDragged();
	        TextField dragTextField = dragItem.getValue();
	        int dragIndex = textFields.indexOf(dragTextField);

	        // update question
	        if (dragIndex == dropIndex || dragIndex == (dropIndex - 1)) {
	            return;
	        } else if (dragIndex < dropIndex) {
	            textFields.add(dropIndex, dragTextField);
	            textFields.remove(dragIndex);
	        } else {
	            textFields.remove(dragIndex);
	            textFields.add(dropIndex, dragTextField);
	        }    
	        
	        // update UI
	        listBox.insertBefore(dragItem, dropItem);
	        listBox.setSelectedItem(dragItem);
	        
	        logger.debug("dragged: " + dragTextField.getLabel());
	        logger.debug("dropped: " + dropTextField.getLabel());
		}
    	
    }
}
