package ciknow.zk.survey.design;

import java.util.List;
import java.util.Set;

import ciknow.dao.PageDao;
import ciknow.dao.QuestionDao;
import ciknow.dao.SurveyDao;
import ciknow.domain.Page;
import ciknow.domain.Question;
import ciknow.domain.Survey;
import ciknow.util.GeneralUtil;
import ciknow.util.SurveyUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.*;

/**
 *
 * @author gyao
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class SurveyOverview extends Window {

    private static final long serialVersionUID = -3982957071713495757L;
    private static Log logger = LogFactory.getLog(SurveyOverview.class);
    @WireVariable
    SurveyDao surveyDao;
    @WireVariable
    PageDao pageDao;
    @WireVariable
    QuestionDao questionDao;
    
    @Wire
    private Tree surveyTree;
    
    @Wire
    private Window updateWindow;
    @Wire("#updateWindow #oldLabel")
    private Label oldLabel;
    @Wire("#updateWindow #newLabelBox")
    private Textbox newLabelBox;
    @Wire
    private Window copyWindow;
    @Wire("#copyWindow #nameBox")
    private Textbox nameBox;;
    @Wire("#copyWindow #labelBox")
    private Textbox labelBox;
    
    @SuppressWarnings("rawtypes")
	private DefaultTreeModel treeModel;
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public SurveyOverview(Component parent) {
        this.setParent(parent);
        this.setSclass("surveyOverviewWindow");
        
        // create UI from template
        Executions.createComponents("/WEB-INF/zk/survey/design/SurveyOverview.zul", this, null);

        Selectors.wireVariables(this, this, Selectors.newVariableResolvers(getClass(), Div.class));
        Selectors.wireComponents(this, this, false);
        Selectors.wireEventListeners(this, this);
        
        // populate the tree
        DefaultTreeNode root = new DefaultTreeNode(null, new DefaultTreeNode[]{});
        treeModel = new DefaultTreeModel(root);
        Survey survey = SurveyUtil.getSurvey();
        for (Page page : survey.getPages()){
        	DefaultTreeNode pageNode = new DefaultTreeNode(page, new DefaultTreeNode[]{});
        	for (Question question : page.getQuestions()){
        		DefaultTreeNode questionNode = new DefaultTreeNode(question);
        		pageNode.add(questionNode);
        	}
        	
        	root.add(pageNode);
        }
        surveyTree.setModel(treeModel);
        surveyTree.setItemRenderer(new SurveyOverviewItemRenderer());
        
        updateWindow.setParent(null);
        copyWindow.setParent(null);
    }

    
	@SuppressWarnings("rawtypes")
	@Listen("onClick = #updateBtn")
    public void showUpdateWindow(){
    	Set selections = treeModel.getSelection();
    	if (selections.isEmpty()){
    		Messagebox.show("Please select a page or question first.");
    		return;
    	}
    	DefaultTreeNode selection = (DefaultTreeNode)selections.iterator().next();
    	String label = null;
    	if (selection.isLeaf()){    		
    		Question question = (Question)selection.getData();
    		label = question.getLabel();
    	} else {
    		Page page = (Page)selection.getData();
    		label = page.getLabel();		
    	}
		oldLabel.setValue(label);
		newLabelBox.setValue(label);
		
		// Highlight text, but it does not work as expected
		newLabelBox.setSelectionRange(0, label.length());	
    	newLabelBox.setFocus(true);
    	
    	// show popup window
    	updateWindow.setParent(this);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Listen("onClick = #updateWindow #saveBtn")
    public void updateLabel(){
    	String newLabel = newLabelBox.getValue().trim();
    	if (!GeneralUtil.isValidLabel(newLabel)){
    		Messagebox.show("Invalid label");
    		return;
    	}
    	
    	Set selections = treeModel.getSelection();
    	DefaultTreeNode selection = (DefaultTreeNode)selections.iterator().next();
    	if (selection.isLeaf()){
    		Question question = (Question)selection.getData();
    		question.setLabel(newLabel);
    		selection.setData(question);
    	} else {
    		Page page = (Page)selection.getData();
    		page.setLabel(newLabel);
    		selection.setData(page);
    	}
    	
    	updateWindow.setParent(null);
    }
    
    @Listen("onClick = #updateWindow #cancelBtn")
    public void cancelUpdateWindow(){
    	updateWindow.setParent(null);
    }

    
    @SuppressWarnings("rawtypes")
	@Listen("onClick = #copyBtn")
    public void showCopyWindow(){
    	Set selections = treeModel.getSelection();
    	if (selections.isEmpty()){
    		Messagebox.show("Please select a page or question first.");
    		return;
    	}
    	DefaultTreeNode selection = (DefaultTreeNode)selections.iterator().next();
    	if (selection.isLeaf()){    		
    		Question question = (Question)selection.getData();
    		nameBox.setValue(question.getShortName() + "_copy");
    		labelBox.setValue(question.getLabel() + "_copy");
    	} else {
    		Page page = (Page)selection.getData();
    		nameBox.setValue(page.getName() + "_copy");
    		labelBox.setValue(page.getLabel() + "_copy");
    	}
    	
    	// show popup window
    	copyWindow.setParent(this);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Listen("onClick = #copyWindow #saveBtn")
    public void copy(){
        String name = nameBox.getValue().trim();
        if (!GeneralUtil.isValidName(name)) return;                

        String label = labelBox.getValue().trim();
        if (!GeneralUtil.isValidLabel(label)) return; 
    	
    	Set selections = treeModel.getSelection();
    	DefaultTreeNode selection = (DefaultTreeNode)selections.iterator().next();
    	DefaultTreeNode parent = (DefaultTreeNode)selection.getParent();
    	Survey survey = SurveyUtil.getSurvey();
    	if (selection.isLeaf()){
    		Question question = (Question)selection.getData();
    		Page parentPage = question.getPage();
    		Question newQuestion = SurveyUtil.getQuestionByShortName(SurveyUtil.getAllQuestions(), name);            
            if (newQuestion != null) {
                Messagebox.show("Question Name: " + name + " has been used. Try a different one.");
                return;
            }    		
    		newQuestion = new Question(question);
    		newQuestion.setShortName(name);
    		newQuestion.setLabel(label);    		
    		parentPage.getQuestions().add(newQuestion);
    		
    		DefaultTreeNode questionNode = new DefaultTreeNode(newQuestion);
    		parent.add(questionNode);
    	} else {
    		Page page = (Page)selection.getData();    		    		
    		Page newPage = SurveyUtil.getPageByName(survey.getPages(), name); 
    		if (newPage != null){
    			Messagebox.show("Page Name: " + name + " has been used. Try a different one.");
    			return;
    		}
    		newPage = new Page(page);
    		newPage.setName(name);
    		newPage.setLabel(label);    		
    		survey.getPages().add(newPage);
    		
        	DefaultTreeNode pageNode = new DefaultTreeNode(newPage, new DefaultTreeNode[]{});
        	for (Question question : newPage.getQuestions()){
        		DefaultTreeNode questionNode = new DefaultTreeNode(question);
        		pageNode.add(questionNode);
        	}        	
        	parent.add(pageNode);
    	}
    	
    	copyWindow.setParent(null);
    }
    
    @Listen("onClick = #copyWindow #cancelBtn")
    public void cancelCopyWindow(){
    	copyWindow.setParent(null);
    }
    
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Listen("onClick = #deleteBtn")
    public void delete(){
    	Set selections = treeModel.getSelection();
    	if (selections.isEmpty()){
    		Messagebox.show("Please select a page or question to delete.");
    		return;
    	}
    	DefaultTreeNode selection = (DefaultTreeNode)selections.iterator().next();
    	selection.getParent().remove(selection);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Listen("onClick = #saveBtn")
    public void save() {    	    	
    	TreeNode root = (DefaultTreeNode)treeModel.getRoot();
    	Survey survey = SurveyUtil.getSurvey();
    	survey.getPages().clear();
    	
    	// Iterate treeModel to update survey. Possible changes:
    	// re-ordering, page/question label, page/question removal
    	List<DefaultTreeNode> pageNodes = root.getChildren();
    	for (DefaultTreeNode pageNode : pageNodes){
    		Page page = (Page) pageNode.getData();
    		page.getQuestions().clear();
    		
    		List<DefaultTreeNode> questionNodes = pageNode.getChildren();
    		for (DefaultTreeNode questionNode : questionNodes){
    			Question question = (Question) questionNode.getData();
    			
    			if (!question.getPage().getName().equals(page.getName())){
    				logger.debug("question(label=" + question.getLabel() + ") " +
    						"is moved to different page: " + page.getLabel());
        			    				    				
    				Question newQuestion = new Question(question);
        			newQuestion.setPage(page);        			
        			page.getQuestions().add(newQuestion);
        			question.setPage(null);
    			} else {
    				page.getQuestions().add(question);
    			}
    		}
    		
    		survey.getPages().add(page);
    	}
    	try {
    		surveyDao.save(survey);
    	} catch (Exception e){
    		logger.warn(e.getMessage());
    		e.printStackTrace();
    		Messagebox.show("Error occurs, please click OK to refresh browser and try again.", "", 
    				Messagebox.OK, Messagebox.ERROR, new EventListener(){

						@Override
						public void onEvent(Event event) throws Exception {
							cancel();
						}
    			
    		});
    		return;
    	}
    	
    	// update current page if it is removed
    	/* Refresh the browser will take care of this
    	Page currentPage = SurveyUtil.getCurrentPage();
    	boolean deleted = true;
    	for (Page page : survey.getPages()){
    		if (page.getName().equals(currentPage.getName())){
    			deleted = false;
    			break;
    		}
    	}
    	if (deleted){
    		SurveyUtil.setCurrentPage(null);
    	}
    	*/
    	
        // update interface
        Executions.sendRedirect(null);
    }

    @Listen("onClick = #cancelBtn")
    public void cancel() {
    	SurveyUtil.setSurvey(null);
    	SurveyUtil.setCurrentPage(null);
    	Executions.sendRedirect(null);
    }
}
