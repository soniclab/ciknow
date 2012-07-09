package ciknow.zk.survey.design;

import java.util.List;

import ciknow.domain.ContactField;
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
import org.zkoss.zul.Combobox;
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
public class QuestionSettingsContactField extends Div implements IdSpace{

    private static final long serialVersionUID = -3302957071716495757L;
    private static Log logger = LogFactory.getLog(QuestionSettingsContactField.class);
    
    @Wire
    private Listbox listBox;
    @Wire
    private Window win;
    @Wire("#win #caption")
    private Caption caption;
    @Wire("#win #nameBox")
    private Combobox nameBox;
    @Wire("#win #labelBox")
    private Textbox labelBox;

    
    private Question question;
    private DragDropListener listener;
    
    public QuestionSettingsContactField(Component parent, Question question) {
    	this.setParent(parent);
    	this.setWidth("100%");
    	this.setHeight("100%");
    	this.question = question;
    	
        // create ui from template
        Executions.createComponents("/WEB-INF/zk/survey/design/QuestionSettingsContactField.zul", this, null);

        Selectors.wireVariables(this, this, Selectors.newVariableResolvers(getClass(), Div.class));
        Selectors.wireComponents(this, this, false);
        Selectors.wireEventListeners(this, this);
        
        // hide popups
        win.setParent(null);        
        listener = new DragDropListener();
        for (ContactField contactField : question.getContactFields()){        	
        	createListitem(contactField);
        }
    }
    
    @Listen("onClick = #newBtn")
    public void showNewWinow() {
        caption.setLabel("Add ContactField");
        
        int count = question.getContactFields().size() + 1;
        nameBox.setValue("name" + count);
        nameBox.setDisabled(false);
        labelBox.setValue("label" + count);

        win.setParent(this);
    }

    @Listen("onClick = #editBtn")
    public void showEditWindow() {
        Listitem li = listBox.getSelectedItem();
        if (li == null) {
            Messagebox.show("Please select a contactField to edit.");
            return;
        }

        ContactField selection = (ContactField) li.getValue();
        caption.setLabel("Edit ContactField");
        nameBox.setValue(selection.getName());
        nameBox.setDisabled(true);
        labelBox.setValue(selection.getLabel());

        win.setParent(this);
    }

    /* This does not work well because of the autocompletion of the Combobox
    @Listen("onChanging = #win #nameBox")
    public void onContactFieldNameChanging(InputEvent e) {
        String value = e.getValue().trim();
        labelBox.setValue(value);

        nameBox.setFocus(true);
        nameBox.setSelectionRange(value.length(), value.length());
    }
	*/
    
    @Listen("onClick = #win #saveBtn")
    public void saveContactField() {
        ContactField contactField;
        String name = nameBox.getValue().trim();
        if (!GeneralUtil.isValidName(name)) return; 
        String label = labelBox.getValue().trim();
        if (!GeneralUtil.isValidLabel(label)) return; 
        
        contactField = question.getContactFieldByName(name);
        if (nameBox.isDisabled()) {
            contactField.setLabel(label);
            
            Listitem li = listBox.getSelectedItem();
            Listcell cell = (Listcell)li.getChildren().get(1);
            cell.setLabel(label);
        } else {
            if (contactField != null) {
                Messagebox.show("ContactField with name=" + name + " is already exist!");
                return;
            }
            contactField = new ContactField();
            contactField.setName(name);
            contactField.setLabel(label);
            question.getContactFields().add(contactField);
            
            Listitem li = createListitem(contactField);
        	listBox.setSelectedItem(li);
        }

        win.setParent(null);
    }

    @Listen("onClick = #win #cancelBtn")
    public void cancelContactField() {
        win.setParent(null);
    }

    @Listen("onClick = #deleteBtn")
    public void deleteContactField() {
        final Listitem li = listBox.getSelectedItem();
        if (li == null) {
            Messagebox.show("Please select a contactField to delete.");
            return;
        }
        final ContactField selection = (ContactField) li.getValue();
        Messagebox.show("Are you sure to delete contactField: " + selection.getLabel(), "", 
        		Messagebox.YES | Messagebox.NO, Messagebox.QUESTION, 0, new EventListener<Event>() {

            @Override
            public void onEvent(Event e) throws Exception {
                if (e.getName().equals(Messagebox.ON_YES)) {
                    question.getContactFields().remove(selection);
                    li.setParent(null);
                }
            }
        });
    }

    private Listitem createListitem(ContactField contactField){
    	Listitem li = new Listitem();
    	li.setParent(listBox);
    	li.appendChild(new Listcell(contactField.getName()));
    	li.appendChild(new Listcell(contactField.getLabel()));     	
    	li.setDroppable("contactField");
    	li.setDraggable("contactField");
    	li.setValue(contactField);
    	li.addEventListener("onDrop", listener);
    	return li;
    }
    
    private class DragDropListener implements EventListener<DropEvent>{

		@Override
		public void onEvent(DropEvent e) throws Exception {
	        List<ContactField> contactFields = question.getContactFields();
	        Listitem dropItem = (Listitem) e.getTarget();
	        ContactField dropContactField = dropItem.getValue();
	        int dropIndex = contactFields.indexOf(dropContactField);
	        Listitem dragItem = (Listitem) e.getDragged();
	        ContactField dragContactField = dragItem.getValue();
	        int dragIndex = contactFields.indexOf(dragContactField);

	        // update question
	        if (dragIndex == dropIndex || dragIndex == (dropIndex - 1)) {
	            return;
	        } else if (dragIndex < dropIndex) {
	            contactFields.add(dropIndex, dragContactField);
	            contactFields.remove(dragIndex);
	        } else {
	            contactFields.remove(dragIndex);
	            contactFields.add(dropIndex, dragContactField);
	        }    
	        
	        // update UI
	        listBox.insertBefore(dragItem, dropItem);
	        listBox.setSelectedItem(dragItem);
	        
	        logger.debug("dragged: " + dragContactField.getLabel());
	        logger.debug("dropped: " + dropContactField.getLabel());
		}
    	
    }
}
