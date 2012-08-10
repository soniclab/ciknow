package ciknow.zk.survey.response;

import ciknow.dao.*;
import ciknow.domain.Node;
import ciknow.domain.Page;
import ciknow.domain.Question;
import ciknow.util.SurveyUtil;
import ciknow.zk.survey.design.QuestionSettings;

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stringtemplate.v4.ST;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.event.DropEvent;
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
public class SurveyQuestionBase extends Div implements IdSpace, ISurveyQuestion {

    private static final long serialVersionUID = -2333198966524673201L;
    private static final Log logger = LogFactory.getLog(SurveyQuestionBase.class);
    @WireVariable
    protected NodeDao nodeDao;
    @WireVariable
    protected GroupDao groupDao;
    @WireVariable
    protected RoleDao roleDao;
    @WireVariable
    protected SurveyDao surveyDao;
    @WireVariable
    protected PageDao pageDao;
    @WireVariable
    protected QuestionDao questionDao;
    @WireVariable
    protected EdgeDao edgeDao;
    
    @Wire
    protected Hbox questionCaption;
    @Wire
    protected Button questionNumber;
    @Wire
    protected Label questionLabel;
    @Wire
    protected Hbox toolbar;
    @Wire
    protected Html instruction;
    @Wire
    protected Label msg;
    
    @Wire
    protected Button helpBtn;
    @Wire
    protected Window helpWin;
    @Wire("#helpWin #helpFrame")
    protected Iframe helpFrame; 
    
    
    protected Node respondent;
    protected Question currentQuestion;

    public SurveyQuestionBase(Question currentQuestion) {
        this(SurveyUtil.getRespondent(), currentQuestion);
    }

    public SurveyQuestionBase(Node respondent, Question currentQuestion) {
        this.respondent = respondent;
        this.currentQuestion = currentQuestion;

        // create UI from template
        createUI();
        
        // wiring variables and components
        Selectors.wireVariables(this, this, Selectors.newVariableResolvers(getClass(), Div.class));
        Selectors.wireComponents(this, this, false);
        
        // create additional UI before wiring listeners
        boolean isAdmin = respondent.isAdmin();
        if (isAdmin){
        	Button deleteBtn = new Button();
        	deleteBtn.setId("deleteBtn");
        	deleteBtn.setLabel("Delete");
        	toolbar.insertBefore(deleteBtn, helpBtn); 
        	
        	Button settingsBtn = new Button();
        	settingsBtn.setId("settingsBtn");
        	settingsBtn.setLabel("Settings");
        	toolbar.insertBefore(settingsBtn, deleteBtn);
        	        	
        	Button addBtn = new Button();
        	if (currentQuestion.isContactChooser()){              		
        		addBtn.setId("addNodeBtn");
        		addBtn.setLabel("Add Node");
        		addBtn.setWidth("80px");
        		toolbar.insertBefore(addBtn, settingsBtn);
        	} else if (currentQuestion.isRelational()){
        		addBtn.setId("addNodeBtn");
        		addBtn.setLabel("Add Node");
        		addBtn.setWidth("80px");
        		toolbar.insertBefore(addBtn, settingsBtn);
        	}
        } else {
        	if (currentQuestion.isDisplayPage()){
        		// do nothing
        	} else if (currentQuestion.isContactChooser()){
        		boolean showAddBtn = currentQuestion.allowUserCreatedNode();
        		if (showAddBtn){
                	Button addBtn = new Button();
            		addBtn.setId("addNodeBtn");
            		addBtn.setLabel("Add Node");
            		addBtn.setWidth("80px");
            		toolbar.insertBefore(addBtn, helpBtn);
        		}
        	} else if (currentQuestion.isContactProvider()){
        		// do nothing
        	} else if (currentQuestion.isRelational()){	// relational based questions
                boolean showAddBtn = currentQuestion.allowUserCreatedNode();
                if (showAddBtn) {
                	Button addBtn = new Button();
            		addBtn.setId("addNodeBtn");
            		addBtn.setLabel("Add Node");
            		addBtn.setWidth("80px");
            		toolbar.insertBefore(addBtn, helpBtn);
                }
        	} else {	// attribute based questions   	
	            boolean showAddBtn = currentQuestion.allowUserCreatedChoice();
	            if (showAddBtn){            	
	            	Button addBtn = new Button();
	    	        if (currentQuestion.isContactInfo()) {	                	
	            		addBtn.setId("addContactFieldBtn");
	            		//addBtn.setLabel("Add Contact Field");
	            		//addBtn.setWidth("120px");
	    	        } else if (currentQuestion.isText()) {
	            		addBtn.setId("addTextFieldBtn");
	            		//addBtn.setLabel("Add Text Field");
	            		//addBtn.setWidth("120px");
	    	        } else {
	            		addBtn.setId("addFieldBtn");
	            		//addBtn.setLabel("Add Field");	            		
	            		//addBtn.setWidth("80px");
	    	        }
	    	        addBtn.setLabel("Other");
	    	        addBtn.setWidth("50px");
	    	        
	    	        //toolbar.insertBefore(addBtn, helpBtn);
	            	Component questionArea = questionCaption.getParent();
	            	Space bottomSpacer = (Space) questionArea.getFellow("bottomSpacer");
	    	        if (bottomSpacer != null) questionArea.insertBefore(addBtn, bottomSpacer);
	    	        else questionArea.appendChild(addBtn);
	            } 
        	}
        }
        
        // wiring listeners
        Selectors.wireEventListeners(this, this);
        
        // create additional UI after wiring listeners
        afterCreationComplete();
        String anchor = SurveyUtil.getSlideInAnchor();
        if (anchor == null || anchor.isEmpty()) anchor = "r";
        this.setAction("show: slideIn({anchor:'" + anchor + "'})");
        helpWin.setParent(null);
    }

    protected void createUI() {
    	
    }

    /**
     * Perform operations after UI creation, wiring and forwarding
     */
    protected void afterCreationComplete() {
        logger.info("Question: " + currentQuestion.getLabel());

        if (respondent.isAdmin()){
	        questionCaption.setDraggable("question");
	        questionCaption.setDroppable("question");
        }
        setTitle();
        setInstruction();
    }

    @Override
    public Node getRespondent() {
        return respondent;
    }

    @Override
    public Question getQuestion() {
        return currentQuestion;
    }

    @Override
    public void setTitle() {
        questionNumber.setLabel("" + (currentQuestion.getIndex() + 1));
        questionLabel.setValue(currentQuestion.getLabel());
    }

    @Override
    public void setInstruction() {
        String content = currentQuestion.getHtmlInstruction();
        if (content != null && !content.isEmpty()) {
            if (content.startsWith("<TEXTFORMAT ")) {
                content = content.replaceAll("SIZE=\"[0-9]+\"", "");
                logger.debug("htmlInstruction from flash version of C-IKNOW");
            }
            
            // processing metadata...
            ST template = new ST(content, '{', '}');
            template.add("user", respondent);
            template.add("question", currentQuestion);
            template.add("page", currentQuestion.getPage());
            template.add("survey", currentQuestion.getPage().getSurvey());
            content = template.render();
            
            // wrap the content
            StringBuilder sb = new StringBuilder();
            sb.append("<div "
                    + "style='"
                    + "-moz-border-radius: 10px;"
                    + "-webkit-border-radius: 10px;"
                    + "border-radius: 10px;"
                    + "border: 5px solid #fff5ee;"
                    + "background:#fff5ee'>");
            sb.append(content);
            sb.append("</div>");
            instruction.setContent(sb.toString());
        } else {
            instruction.setParent(null);
        }
    }

    @Override
    public void addField() {
        logger.error("This method must be implemented");
    }

    @Override
    public void addTextField() {
        logger.error("This method must be implemented");
    }

    @Override
    public void addContactField() {
        logger.error("This method must be implemented");
    }

    @Override
    public void addScale() {
        logger.error("This method must be implemented");
    }

    @Override
    public void addNode() {
        logger.error("This method must be implemented");
    }

    @Listen("onClick = #settingsBtn")
    @Override
    public void updateQuestionSettings(){
        // save any existing answers because current page will be refresh
        if (!validate()) return;
        respondent.update(nodeDao.loadById(respondent.getId()));		
        save();
        
        QuestionSettings win = new QuestionSettings(this);
        win.doModal();
    }
    
    @Listen("onClick = #deleteBtn")
    @Override
    public void deleteQuestion() {

        Messagebox.show("Are you sure to remove this question?", "Confirmation Needed",
                Messagebox.YES | Messagebox.NO, Messagebox.QUESTION, new EventListener<Event>() {

            @Override
            public void onEvent(Event event) throws Exception {
                if (event.getName().equals(Messagebox.ON_YES)) {
                    Page page = currentQuestion.getPage();
                    page.getQuestions().remove(currentQuestion);
                    currentQuestion.setPage(null);
                    try {
                        pageDao.save(page);
                    } catch (Exception e) {
                        logger.warn(e.getMessage());
                        Messagebox.show("Failed to add new question. Press OK to refersh and try again.", "",
                                Messagebox.OK, Messagebox.ERROR, new EventListener<Event>() {

                            @Override
                            public void onEvent(Event event) throws Exception {
                                SurveyUtil.refresh();
                            }
                        });
                        return;
                    }

                    SurveyPage surveyPage = (SurveyPage) getParent();
                    surveyPage.removeQuestionFromPage(currentQuestion);
                }
            }
        });
    }

    @Listen("onClick = #helpBtn")
    public void getHelp(){
    	helpWin.setParent(this);
    	helpWin.doModal();
    	helpWin.setTitle("Question Type - " + currentQuestion.getType());
    	String url = "http://ciknow.northwestern.edu/documentation";
    	helpFrame.setSrc(url);
    	helpFrame.invalidate();
    }
    
    @Listen("onDrop = #questionCaption")
    @Override
    public void moveQuestion(DropEvent event) {
        Component draggedComponent = (Component) event.getDragged();
        SurveyQuestionBase dragged = (SurveyQuestionBase) draggedComponent.getParent().getParent().getParent().getParent();

        // change order
        Page page = SurveyUtil.getCurrentPage();
        List<Question> questions = page.getQuestions();
        Question draggedQuestion = dragged.currentQuestion;
        int dragIndex = questions.indexOf(draggedQuestion);
        int dropIndex = questions.indexOf(currentQuestion);
        if (dragIndex < dropIndex) {
            questions.add(dropIndex, draggedQuestion);
            questions.remove(dragIndex);
        } else if (dragIndex > dropIndex) {
            questions.remove(dragIndex);
            questions.add(dropIndex, draggedQuestion);
        } else {
            // do nothing
        }
        logger.debug("dragged: " + draggedQuestion.getLabel());
        logger.debug("dropped: " + currentQuestion.getLabel());

        // save to database
        try {
            pageDao.save(page);
        } catch (Exception e) {
            logger.warn(e.getMessage());
            Messagebox.show("Failed to drag nad drop. Press OK to refersh and try again.", "",
                    Messagebox.OK, Messagebox.ERROR, new EventListener<Event>() {

                @Override
                public void onEvent(Event event) throws Exception {
                    SurveyUtil.refresh();
                }
            });
            return;
        }

        // update UI
        SurveyPage surveyPage = (SurveyPage) getParent();
        surveyPage.insertBefore(dragged, this);
        for (Object o : surveyPage.getChildren()) {
            SurveyQuestionBase child = (SurveyQuestionBase) o;
            child.setTitle();
        }
    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public void save() {
        logger.error("This method must be implemented");
    }

    @Override
    public void setErrorMsg(String msg) {
        this.msg.setValue(msg);
        this.msg.setStyle("color:red");
        this.msg.setVisible(true);
    }

    /**
     * update current question only
     */
    @Override
    public void refresh() {
        SurveyPage surveyPage = (SurveyPage) getParent();
        surveyPage.updateQuestionInPage(currentQuestion);	
    }
    
    /**
     * update the whole page
     */
    @Override
    public void refreshWholePage() {
        SurveyPage surveyPage = (SurveyPage) getParent();
        surveyPage.render(currentQuestion.getPage());		
    }
}
