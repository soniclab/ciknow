package ciknow.zk.survey.design;

import java.util.LinkedHashMap;
import java.util.List;

import ciknow.domain.Question;
import ciknow.util.Constants;
import ciknow.util.GeneralUtil;
import ciknow.util.SurveyUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Div;
import org.zkoss.zul.ListModelList;
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
public class QuestionSettingsCCConfig extends Div implements IdSpace{

    private static final long serialVersionUID = -3902957071716495757L;
    private static Log logger = LogFactory.getLog(QuestionSettingsCCConfig.class);
    private final String[] properties = 
    	{"label", "organization", "department", "unit", "type", 
    		"lastName", "firstName", "city", "state", "country", "zipcode"};
    
    @Wire
    private Listbox listBox;
    
    @Wire
    private Window propertyWindow;
    @Wire("#propertyWindow #nameBox")
    private Combobox propertyNameBox;    
    @Wire("#propertyWindow #labelBox")
    private Textbox propertyLabelBox;
    
    @Wire
    private Window attributeWindow;
    @Wire("#attributeWindow #nameBox")
    private Combobox attributeNameBox;    
    @Wire("#attributeWindow #labelBox")
    private Textbox attributeLabelBox;

    private Question question;
    private LinkedHashMap<String, String> columns;
    private DragDropListener listener;
    
    public QuestionSettingsCCConfig(Component parent, Question question) {
    	this.setParent(parent);
    	this.setWidth("100%");
    	this.setHeight("100%");
    	this.question = question;
    	
        // create ui from template
        Executions.createComponents("/WEB-INF/zk/survey/design/QuestionSettingsCCConfig.zul", this, null);

        Selectors.wireVariables(this, this, Selectors.newVariableResolvers(getClass(), Div.class));
        Selectors.wireComponents(this, this, false);
        Selectors.wireEventListeners(this, this);
        
        // hide popups
        propertyWindow.setParent(null);
        attributeWindow.setParent(null);
        
        columns = question.getCCColumns();
        listener = new DragDropListener();
        for (String name : columns.keySet()){
        	String label = columns.get(name);
        	Listitem li = new Listitem();
        	li.setParent(listBox);
        	li.appendChild(new Listcell(name));
        	li.appendChild(new Listcell(label));
        	li.setDroppable("property");
        	li.setDraggable("property");
        	li.setValue(name);
        	li.addEventListener("onDrop", listener);
        }
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Listen("onClick = #addPropertyBtn")
    public void showPropertyWindow(){
    	propertyWindow.setParent(this);
    	
    	ListModelList model = (ListModelList) propertyNameBox.getModel();
    	if (model == null){
    		model = new ListModelList(properties);
    		propertyNameBox.setModel(model);
    	}
    	model.clearSelection();  
    	
    	String msg = "To be displayed as column header";
    	propertyLabelBox.setValue(msg);
    	propertyLabelBox.setSelectionRange(0, msg.length());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Listen("onClick = #addAttributeBtn")
    public void showAttributeWindow(){
    	attributeWindow.setParent(this);
    	
    	ListModelList model = (ListModelList) attributeNameBox.getModel();
    	if (model == null){
    		model = new ListModelList();
    		List<Question> questions = SurveyUtil.getAllQuestions();
    		for (Question question : questions){
    			if (question.isSingleChoice()) model.add(question.getLabel());
    		}
    		attributeNameBox.setModel(model);
    	}
    	model.clearSelection();  
    	
    	String msg = "To be displayed as column header";
    	attributeLabelBox.setValue(msg);
    	attributeLabelBox.setSelectionRange(0, msg.length());
    }
    
    @Listen("onClick = #deleteBtn")
    public void delete(){
    	Listitem item = listBox.getSelectedItem();
    	if (item == null){
    		Messagebox.show("Please select the item to be removed.");
    		return;
    	}
    	
    	// update question
    	Listcell cell = (Listcell)item.getChildren().get(0);
    	String name = cell.getLabel();
    	columns.remove(name);
    	question.setCCColumns(columns);
    	
    	// update UI
    	item.setParent(null);
    }
    
    @Listen("onClick = #propertyWindow #saveBtn")
    public void addProperty(){
    	logger.info("Add property");
    	Comboitem item = propertyNameBox.getSelectedItem();
    	if (item == null) {
    		Messagebox.show("Please select a property.");
    		return;
    	}
    	
    	String name = item.getLabel();
    	if (columns.containsKey(name)){
    		Messagebox.show("Duplicated property");
    		return;
    	}
    	
    	String label = propertyLabelBox.getValue().trim();
    	if (!GeneralUtil.isValidLabel(label)) return;
    	
    	// update question
    	columns.put(name, label);
    	question.setCCColumns(columns);
    	
    	// update UI
    	Listitem li = new Listitem();
    	li.setParent(listBox);
    	li.appendChild(new Listcell(name));
    	li.appendChild(new Listcell(label));
    	li.setDroppable("property");
    	li.setDraggable("property");
    	li.setValue(name);
    	li.addEventListener("onDrop", listener);
    	listBox.setSelectedItem(li);    	
    	
    	propertyWindow.setParent(null);
    }
    
    @Listen("onClick = #propertyWindow #cancelBtn")
    public void cancelProperty(){
    	logger.info("Cancel property");
    	propertyWindow.setParent(null);
    }
    
    @Listen("onClick = #attributeWindow #saveBtn")
    public void addAttribute(){
    	logger.info("Add attribute");
    	Comboitem item = attributeNameBox.getSelectedItem();
    	if (item == null) {
    		Messagebox.show("Please select an attribute.");
    		return;
    	}
    	
    	String questionLabel = item.getLabel();
    	List<Question> questions = SurveyUtil.getAllQuestions();
    	Question selectedQuestion = SurveyUtil.getQuestionByLabel(questions, questionLabel);
    	String name = "Q" + Constants.SEPERATOR + selectedQuestion.getShortName();
    	if (columns.containsKey(name)){
    		Messagebox.show("Duplicated attribute");
    		return;
    	}
    	
    	String label = attributeLabelBox.getValue().trim();
    	if (!GeneralUtil.isValidLabel(label)) return;
    	
    	// update question
    	columns.put(name, label);
    	question.setCCColumns(columns);
    	
    	// update UI
    	Listitem li = new Listitem();
    	li.setParent(listBox);
    	li.appendChild(new Listcell(name));
    	li.appendChild(new Listcell(label));
    	li.setDraggable("attribute");
    	li.setDroppable("attribute");
    	li.setValue(name);
    	li.addEventListener("onDrop", listener);
    	listBox.setSelectedItem(li);

    	attributeWindow.setParent(null);  
    }
    
    @Listen("onClick = #attributeWindow #cancelBtn")
    public void cancelAttribute(){
    	logger.info("Cancel attribute");
    	attributeWindow.setParent(null);
    }

    private class DragDropListener implements EventListener<DropEvent>{

		@Override
		public void onEvent(DropEvent e) throws Exception {
	        Listitem dropItem = (Listitem) e.getTarget();
	        String dropName = dropItem.getValue();
	        String dropLabel = columns.get(dropName);
	        Listitem dragItem = (Listitem) e.getDragged();
	        String dragName = dragItem.getValue();
	        String dragLabel = columns.get(dragName);
	        
	        // update question
	        if (dragName == dropName) {
	            return;
	        } 
	        
	        LinkedHashMap<String, String> newColumns = new LinkedHashMap<String, String>();
	        for (String name : columns.keySet()){
	        	String label = columns.get(name);
	        	
	        	if (name.equals(dragName)) continue;
	        	if (name.equals(dropName)){
	        		newColumns.put(dragName, dragLabel);
	        	}
	        	newColumns.put(name, label);
	        }
	        columns = newColumns;
	        question.setCCColumns(newColumns);
	        
	        // update UI
	        listBox.insertBefore(dragItem, dropItem);
	        
	        logger.debug("dragged: " + dragLabel);
	        logger.debug("dropped: " + dropLabel);
		}
    	
    }
}
