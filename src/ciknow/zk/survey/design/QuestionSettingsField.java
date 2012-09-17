package ciknow.zk.survey.design;

import java.util.List;

import ciknow.domain.Field;
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
public class QuestionSettingsField extends Div implements IdSpace{

    private static final long serialVersionUID = -3302957071716495757L;
    private static Log logger = LogFactory.getLog(QuestionSettingsField.class);
    
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
    
    private Question question;
    private DragDropListener listener;
    
    public QuestionSettingsField(Component parent, Question question) {
    	this.setParent(parent);
    	this.setWidth("100%");
    	this.setHeight("100%");
    	this.question = question;
    	
        // create ui from template
        Executions.createComponents("/WEB-INF/zk/survey/design/QuestionSettingsField.zul", this, null);

        Selectors.wireVariables(this, this, Selectors.newVariableResolvers(getClass(), Div.class));
        Selectors.wireComponents(this, this, false);
        Selectors.wireEventListeners(this, this);
        
        // hide popups
        win.setParent(null);        
        listener = new DragDropListener();
        for (Field field : question.getFields()){        	
        	createListitem(field);
        }
    }
    
    @Listen("onClick = #newBtn")
    public void showNewWinow() {
        caption.setLabel("Add Field");
        
        int count = question.getFields().size() + 1;
        nameBox.setValue("name" + count);
        nameBox.setDisabled(false);
        labelBox.setValue("label" + count);

        win.setParent(this);
    }

    @Listen("onClick = #editBtn")
    public void showEditWindow() {
        Listitem li = listBox.getSelectedItem();
        if (li == null) {
            Messagebox.show("Please select a field to edit.");
            return;
        }

        Field selection = (Field) li.getValue();
        caption.setLabel("Edit Field");
        nameBox.setValue(selection.getName());
        nameBox.setDisabled(true);
        labelBox.setValue(selection.getLabel());

        win.setParent(this);
    }

    /*
    @Listen("onChanging = #win #nameBox")
    public void onFieldNameChanging(InputEvent e) {
        String value = e.getValue().trim();
        labelBox.setValue(value);

        nameBox.setFocus(true);
        nameBox.setSelectionRange(value.length(), value.length());
    }
	*/
    
    @Listen("onClick = #win #saveBtn")
    public void saveField() {
        Field field;
        String name = nameBox.getValue().trim();
        if (!GeneralUtil.isValidName(name)) return; 
        String label = labelBox.getValue().trim();
        if (!GeneralUtil.isValidLabel(label)) return; 
        
        field = question.getFieldByName(name);
        if (nameBox.isDisabled()) {
            field.setLabel(label);
            
            Listitem li = listBox.getSelectedItem();
            Listcell cell = (Listcell)li.getChildren().get(1);
            cell.setLabel(label);
        } else {
            if (field != null) {
                Messagebox.show("Field with name=" + name + " is already exist!");
                return;
            }
            field = new Field();
            field.setName(name);
            field.setLabel(label);
            question.getFields().add(field);
            
            Listitem li = createListitem(field);
        	listBox.setSelectedItem(li);
        }

        win.setParent(null);
    }

    @Listen("onClick = #win #cancelBtn")
    public void cancelField() {
        win.setParent(null);
    }

    @Listen("onClick = #deleteBtn")
    public void deleteField() {
        final Listitem li = listBox.getSelectedItem();
        if (li == null) {
            Messagebox.show("Please select a field to delete.");
            return;
        }
        final Field selection = (Field) li.getValue();
        Messagebox.show("Are you sure to delete field: " + selection.getLabel(), "", 
        		Messagebox.YES | Messagebox.NO, Messagebox.QUESTION, 0, new EventListener<Event>() {

            @Override
            public void onEvent(Event e) throws Exception {
                if (e.getName().equals(Messagebox.ON_YES)) {
                    question.getFields().remove(selection);
                    li.setParent(null);
                }
            }
        });
    }

    private Listitem createListitem(Field field){
    	Listitem li = new Listitem();
    	li.setParent(listBox);
    	li.appendChild(new Listcell(field.getName()));
    	li.appendChild(new Listcell(field.getLabel()));     	
    	li.setDroppable("field");
    	li.setDraggable("field");
    	li.setValue(field);
    	li.addEventListener("onDrop", listener);
    	return li;
    }
    
    private class DragDropListener implements EventListener<DropEvent>{

		@Override
		public void onEvent(DropEvent e) throws Exception {
	        List<Field> fields = question.getFields();
	        Listitem dropItem = (Listitem) e.getTarget();
	        Field dropField = dropItem.getValue();
	        int dropIndex = fields.indexOf(dropField);
	        Listitem dragItem = (Listitem) e.getDragged();
	        Field dragField = dragItem.getValue();
	        int dragIndex = fields.indexOf(dragField);

	        // update question
	        if (dragIndex == dropIndex || dragIndex == (dropIndex - 1)) {
	            return;
	        } else if (dragIndex < dropIndex) {
	            fields.add(dropIndex, dragField);
	            fields.remove(dragIndex);
	        } else {
	            fields.remove(dragIndex);
	            fields.add(dropIndex, dragField);
	        }    
	        
	        // update UI
	        listBox.insertBefore(dragItem, dropItem);
	        listBox.setSelectedItem(dragItem);
	        
	        logger.debug("dragged: " + dragField.getLabel());
	        logger.debug("dropped: " + dropField.getLabel());
		}
    	
    }
}
