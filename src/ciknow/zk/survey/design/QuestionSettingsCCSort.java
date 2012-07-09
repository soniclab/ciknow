package ciknow.zk.survey.design;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ciknow.domain.Question;
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
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Div;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

/**
 *
 * @author gyao
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class QuestionSettingsCCSort extends Div implements IdSpace{

    private static final long serialVersionUID = -3902957071716495757L;
    private static Log logger = LogFactory.getLog(QuestionSettingsCCSort.class);
    private final String[] orderTypes = {"ascend", "descend", "alternate"};
    
    @Wire
    private Listbox listBox;
    
    @Wire
    private Window sortWindow;
    @Wire("#sortWindow #nameBox")
    private Combobox nameBox;    
    @Wire("#sortWindow #caseBox")
    private Checkbox caseBox;
    @Wire("#sortWindow #numericBox")
    private Checkbox numericBox;
    @Wire("#sortWindow #orderBox")
    private Combobox orderBox;
    
    private Question question;
    private LinkedHashMap<String, String> columns;
    private List<Map<String, String>> sortColumns;
    private DragDropListener listener;
    
    public QuestionSettingsCCSort(Component parent, Question question) {
    	this.setParent(parent);
    	this.setWidth("100%");
    	this.setHeight("100%");
    	this.question = question;
    	
        // create ui from template
        Executions.createComponents("/WEB-INF/zk/survey/design/QuestionSettingsCCSort.zul", this, null);

        Selectors.wireVariables(this, this, Selectors.newVariableResolvers(getClass(), Div.class));
        Selectors.wireComponents(this, this, false);
        Selectors.wireEventListeners(this, this);
        
        // hide popups
        sortWindow.setParent(null);
        
        columns = question.getCCColumns();
        sortColumns = question.getCCSortColumns();
        listener = new DragDropListener();
        for (Map<String, String> m : sortColumns){
        	String name = m.get("name");
        	Boolean casesensitive = m.get("casesensitive").equals("1");
        	Boolean numeric = m.get("numeric").equals("1");
        	String order = m.get("order");
        	
        	Listitem li = new Listitem();
        	li.setParent(listBox);
        	li.appendChild(new Listcell(name));
        	li.appendChild(new Listcell(casesensitive.toString()));
        	li.appendChild(new Listcell(numeric.toString()));
        	li.appendChild(new Listcell(order));        	
        	li.setDroppable("sort");
        	li.setDraggable("sort");
        	li.setValue(name);
        	li.addEventListener("onDrop", listener);
        }
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Listen("onClick = #addBtn")
    public void showSortWindow(){
    	sortWindow.setParent(this);
    	
    	ListModelList model = (ListModelList) nameBox.getModel();
    	if (model == null) {    		
    		model = new ListModelList(columns.keySet());
    		nameBox.setModel(model);
    	}
    	model.clearSelection();  
    	
    	caseBox.setChecked(true);
    	numericBox.setChecked(false);
    	
    	model = (ListModelList) orderBox.getModel();
    	if (model == null){
    		model = new ListModelList(orderTypes);
    		orderBox.setModel(model);
    	}
    	model.clearSelection();
    }
    
    @Listen("onClick = #deleteBtn")
    public void delete(){
    	Listitem item = listBox.getSelectedItem();
    	if (item == null){
    		Messagebox.show("Please select the item to be removed.");
    		return;
    	}
    	
    	// update question
    	String name = item.getValue();
    	Map<String, String> m = getSortColumnByName(name);
    	sortColumns.remove(m);
    	question.setCCSortColumns(sortColumns);
    	
    	// update UI
    	item.setParent(null);
    }
    
    @Listen("onClick = #sortWindow #saveBtn")
    public void addSort(){
    	logger.info("Add sort");
    	Comboitem item = nameBox.getSelectedItem();
    	if (item == null) {
    		Messagebox.show("Please select a column name.");
    		return;
    	}
    	
    	String name = item.getLabel();
    	Map<String, String> m = getSortColumnByName(name);
    	if (m != null){
    		Messagebox.show("Duplicated sort column");
    		return;
    	}
    	
    	item = orderBox.getSelectedItem();
    	if (item == null){
    		Messagebox.show("Please select a sort order.");
    		return;
    	}
    	String order = item.getLabel();
    	Boolean casesensitive = caseBox.isChecked();
    	Boolean numeric = numericBox.isChecked();
    	
    	// update question
    	Map<String, String> sortColumn = new HashMap<String, String>();
    	sortColumn.put("name", name);
    	sortColumn.put("label", columns.get(name));
    	sortColumn.put("casesensitive", casesensitive?"1":"0");
    	sortColumn.put("numeric", numeric?"1":"0");
    	sortColumn.put("order", order);
    	sortColumns.add(sortColumn);
    	question.setCCSortColumns(sortColumns);
    	
    	// update UI
    	Listitem li = new Listitem();
    	li.setParent(listBox);
    	li.appendChild(new Listcell(name));
    	li.appendChild(new Listcell(casesensitive.toString()));
    	li.appendChild(new Listcell(numeric.toString()));
    	li.appendChild(new Listcell(order));        	
    	li.setDroppable("sort");
    	li.setDraggable("sort");
    	li.setValue(name);
    	li.addEventListener("onDrop", listener);
    	listBox.setSelectedItem(li);    	
    	
    	sortWindow.setParent(null);
    }
    
    @Listen("onClick = #sortWindow #cancelBtn")
    public void cancelProperty(){
    	sortWindow.setParent(null);
    }

    
    
    private Map<String, String> getSortColumnByName(String name){
    	for (Map<String, String> m : sortColumns){
    		if (m.get("name").equals(name)) return m;
    	}
    	return null;
    }
    
    private class DragDropListener implements EventListener<DropEvent>{

		@Override
		public void onEvent(DropEvent e) throws Exception {
	        Listitem dropItem = (Listitem) e.getTarget();
	        String dropName = dropItem.getValue();
	        Map<String, String> dropColumn = getSortColumnByName(dropName);
	        int dropIndex = sortColumns.indexOf(dropColumn);
	        
	        Listitem dragItem = (Listitem) e.getDragged();
	        String dragName = dragItem.getValue();
	        Map<String, String> dragColumn = getSortColumnByName(dragName);
	        int dragIndex = sortColumns.indexOf(dragColumn);
	        
	        // update question
	        if (dragIndex == dropIndex || dragIndex == (dropIndex - 1)) {
	            return;
	        } else if (dragIndex < dropIndex) {
	        	sortColumns.add(dropIndex, dragColumn);
	        	sortColumns.remove(dragIndex);
	        } else {
	        	sortColumns.remove(dragIndex);
	        	sortColumns.add(dropIndex, dragColumn);
	        } 	        
	        question.setCCSortColumns(sortColumns);
	        
	        // update UI
	        listBox.insertBefore(dragItem, dropItem);
	        
	        logger.debug("dragged: " + dragName);
	        logger.debug("dropped: " + dropName);
		}
    	
    }
}
