package ciknow.zk.survey.design;

import java.util.List;

import ciknow.domain.Scale;
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
import org.zkoss.zul.Doublebox;
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
public class QuestionSettingsScale extends Div implements IdSpace{

    private static final long serialVersionUID = -3302957071716495757L;
    private static Log logger = LogFactory.getLog(QuestionSettingsScale.class);
    
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
    @Wire("#win #valueBox")
    private Doublebox valueBox;
    
    private Question question;
    private DragDropListener listener;
    
    public QuestionSettingsScale(Component parent, Question question) {
    	this.setParent(parent);
    	this.setWidth("100%");
    	this.setHeight("100%");
    	this.question = question;
    	
        // create ui from template
        Executions.createComponents("/WEB-INF/zk/survey/design/QuestionSettingsScale.zul", this, null);

        Selectors.wireVariables(this, this, Selectors.newVariableResolvers(getClass(), Div.class));
        Selectors.wireComponents(this, this, false);
        Selectors.wireEventListeners(this, this);
        
        // hide popups
        win.setParent(null);        
        listener = new DragDropListener();
        for (Scale scale : question.getScales()){        	
        	createListitem(scale);
        }
    }
    
    @Listen("onClick = #newBtn")
    public void showNewWinow() {
        caption.setLabel("Add Scale");
        
        int count = question.getScales().size() + 1;
        nameBox.setValue("name" + count);
        nameBox.setDisabled(false);
        labelBox.setValue("label" + count);
        valueBox.setValue(count);
        
        win.setParent(this);
    }

    @Listen("onClick = #editBtn")
    public void showEditWindow() {
        Listitem li = listBox.getSelectedItem();
        if (li == null) {
            Messagebox.show("Please select a scale to edit.");
            return;
        }

        Scale selection = (Scale) li.getValue();
        caption.setLabel("Edit Scale");
        nameBox.setValue(selection.getName());
        nameBox.setDisabled(true);
        labelBox.setValue(selection.getLabel());
        valueBox.setValue(selection.getValue());
        
        win.setParent(this);
    }

    /*
    @Listen("onChanging = #win #nameBox")
    public void onScaleNameChanging(InputEvent e) {
        String value = e.getValue().trim();
        labelBox.setValue(value);

        nameBox.setFocus(true);
        nameBox.setSelectionRange(value.length(), value.length());
    }
	*/
    
    @Listen("onClick = #win #saveBtn")
    public void saveScale() {
        Scale scale;
        String name = nameBox.getValue().trim();
        if (!GeneralUtil.isValidName(name)) return; 
        String label = labelBox.getValue().trim();
        if (!GeneralUtil.isValidLabel(label)) return; 
        Double value = valueBox.getValue();
        
        scale = question.getScaleByName(name);
        if (nameBox.isDisabled()) {
            scale.setLabel(label);
            scale.setValue(value);
            
            Listitem li = listBox.getSelectedItem();
            Listcell labelCell = (Listcell)li.getChildren().get(1);
            labelCell.setLabel(label);
            Listcell valueCell = (Listcell)li.getChildren().get(2);
            valueCell.setLabel(value.toString());
        } else {
            if (scale != null) {
                Messagebox.show("Scale with name=" + name + " is already exist!");
                return;
            }
            scale = new Scale();
            scale.setName(name);
            scale.setLabel(label);
            scale.setValue(value);
            question.getScales().add(scale);
            
            Listitem li = createListitem(scale);
        	listBox.setSelectedItem(li);
        }

        win.setParent(null);
    }

    @Listen("onClick = #win #cancelBtn")
    public void cancelScale() {
        win.setParent(null);
    }

    @Listen("onClick = #deleteBtn")
    public void deleteScale() {
        final Listitem li = listBox.getSelectedItem();
        if (li == null) {
            Messagebox.show("Please select a scale to delete.");
            return;
        }
        final Scale selection = (Scale) li.getValue();
        Messagebox.show("Are you sure to delete scale: " + selection.getLabel(), "", 
        		Messagebox.YES | Messagebox.NO, Messagebox.QUESTION, 0, new EventListener<Event>() {

            @Override
            public void onEvent(Event e) throws Exception {
                if (e.getName().equals(Messagebox.ON_YES)) {
                    question.getScales().remove(selection);
                    li.setParent(null);
                }
            }
        });
    }

    private Listitem createListitem(Scale scale){
    	Listitem li = new Listitem();
    	li.setParent(listBox);
    	li.appendChild(new Listcell(scale.getName()));
    	li.appendChild(new Listcell(scale.getLabel()));
    	li.appendChild(new Listcell(scale.getValue().toString()));
    	li.setDroppable("scale");
    	li.setDraggable("scale");
    	li.setValue(scale);
    	li.addEventListener("onDrop", listener);
    	return li;
    }
    
    private class DragDropListener implements EventListener<DropEvent>{

		@Override
		public void onEvent(DropEvent e) throws Exception {
	        List<Scale> scales = question.getScales();
	        Listitem dropItem = (Listitem) e.getTarget();
	        Scale dropScale = dropItem.getValue();
	        int dropIndex = scales.indexOf(dropScale);
	        Listitem dragItem = (Listitem) e.getDragged();
	        Scale dragScale = dragItem.getValue();
	        int dragIndex = scales.indexOf(dragScale);

	        // update question
	        if (dragIndex == dropIndex || dragIndex == (dropIndex - 1)) {
	            return;
	        } else if (dragIndex < dropIndex) {
	            scales.add(dropIndex, dragScale);
	            scales.remove(dragIndex);
	        } else {
	            scales.remove(dragIndex);
	            scales.add(dropIndex, dragScale);
	        }    
	        
	        // update UI
	        listBox.insertBefore(dragItem, dropItem);
	        listBox.setSelectedItem(dragItem);
	        
	        logger.debug("dragged: " + dragScale.getLabel());
	        logger.debug("dropped: " + dropScale.getLabel());
		}
    	
    }
}
