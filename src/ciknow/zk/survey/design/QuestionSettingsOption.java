package ciknow.zk.survey.design;

import java.util.List;
import java.util.Map;
import java.util.Set;

import ciknow.domain.Field;
import ciknow.domain.Page;
import ciknow.domain.Question;
import ciknow.domain.Survey;
import ciknow.ro.NodeRO;
import ciknow.util.Beans;
import ciknow.util.Constants;
import ciknow.util.GeneralUtil;
import ciknow.util.SurveyUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Div;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Spinner;
import org.zkoss.zul.Textbox;

/**
 *
 * @author gyao
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class QuestionSettingsOption extends Div implements IdSpace{

    private static final long serialVersionUID = -3902967071716495757L;
    private static Log logger = LogFactory.getLog(QuestionSettingsOption.class);

    /* choice */
    @Wire
    private Row isSingleChoiceItem;
    @Wire
    private Checkbox isSingleChoiceBox;
    @Wire
    private Row singleChoiceRenderItem;
    @Wire
    private Checkbox singleChoiceRenderBox;
    @Wire
    private Row maxChoiceItem;
    @Wire
    private Spinner maxChoiceSpinner;
    @Wire
    private Row jumpConditionItem;
    @Wire
    private Listbox jumpConditionBox;
    @Wire
    private Row jumpQuestionItem;
    @Wire
    private Listbox jumpQuestionBox; 
  
    /* rating */
    @Wire
    private Row ratingDisplayItem;
    @Wire
    private Checkbox ratingDisplayBox;
    /* choice, relational choice */
    @Wire
    private Row selectAllItem;
    @Wire
    private Checkbox selectAllBox;    
    /* is attribute */
    @Wire
    private Row allowCreateChoiceItem;
    @Wire
    private Checkbox allowCreateChoiceBox;    
    /* relational continuous */
    @Wire
    private Row recordDurationItem;
    @Wire
    private Checkbox recordDurationBox;    
    /* perceived choice, relational choice multiple */
    @Wire
    private Row singleChoicePerLineItem;
    @Wire
    private Checkbox singleChoicePerLineBox;    
    /* is relational */
    @Wire
    private Row contactProviderItem;
    @Wire
    private Listbox contactProviderBox;
    @Wire
    private Row contactChooserItem;
    @Wire
    private Listbox contactChooserBox;
    @Wire
    private Row negateItem;
    @Wire
    private Checkbox negateBox;
    @Wire
    private Row skipOnEmptyItem;
    @Wire
    private Checkbox skipOnEmptyBox;
    @Wire
    private Row hideContactChooserInstructionItem;
    @Wire
    private Checkbox hideContactChooserInstructionBox;
    /* contact chooser */
    @Wire
    private Row emptyStrategyItem;
    @Wire
    private Listbox emptyStrategyBox;
    @Wire
    private Row defaultContactTypeItem;
    @Wire
    private Listbox defaultContactTypeBox;
    @Wire
    private Row showImageItem;
    @Wire
    private Checkbox showImageBox;
    @Wire
    private Row selectedContactsHeaderItem;
    @Wire
    private Textbox selectedContactsHeaderTi;
    /* contact chooser, is relational */
    @Wire
    private Row showSelfItem;
    @Wire
    private Checkbox showSelfBox;
    @Wire
    private Row allowCreateNodeItem;
    @Wire
    private Checkbox allowCreateNodeBox;
    /* general */
    @Wire
    private Row hiddenFromVisItem;
    @Wire
    private Checkbox hiddenFromVisBox;
    
    
    private Question question;
    private List<Map<String, String>> nodeTypeDescriptions;
    
    public QuestionSettingsOption(Component parent, Question question) {
    	this.setParent(parent);
    	this.setWidth("100%");
    	this.setHeight("100%");
    	this.question = question;
    	
        // create ui from template
        Executions.createComponents("/WEB-INF/zk/survey/design/QuestionSettingsOption.zul", this, null);
        Selectors.wireVariables(this, this, Selectors.newVariableResolvers(getClass(), Div.class));
        Selectors.wireComponents(this, this, false);
        
        if (question.isChoice()){        	
        	isSingleChoiceItem.setVisible(true);   
        	if (question.isSingleChoice()){
        		singleChoiceRenderItem.setVisible(true);
        	} else {
        		maxChoiceItem.setVisible(true);   
        	}
        	jumpConditionItem.setVisible(true);
        	jumpQuestionItem.setVisible(true);
        	
        	isSingleChoiceBox.setChecked(question.isSingleChoice());
        	singleChoiceRenderBox.setChecked(question.showSingleChoiceAsList());

    		String constraint = "no empty, min 0";
    		int numFields = question.getFields().size();
    		if (numFields > 0) constraint += (" max " + numFields);
    		logger.info("Set constraint for maxChoiceSpinner: " + constraint);
    		maxChoiceSpinner.setConstraint(constraint);
        	int maxChoice = question.getMaxChoice();
        	if (maxChoice < 0 || maxChoice > numFields) maxChoice = numFields;
        	maxChoiceSpinner.setValue(maxChoice);

        	ListModelList<String> model = new ListModelList<String>();
        	model.add("");
        	for (Field field : question.getFields()){
        		model.add(field.getLabel());
        	}
        	Field jc = question.getJumpCondition();
        	if (jc != null) model.addToSelection(jc.getLabel());
        	jumpConditionBox.setModel(model);

        	model = new ListModelList<String>();
        	model.add("");
        	for (Question q : SurveyUtil.getAllQuestions()){
        		model.add(q.getLabel());
        	}
        	Question jq = question.getJumpQuestion();
        	if (jq != null) model.addToSelection(jq.getLabel());
        	jumpQuestionBox.setModel(model);
        }
        if (question.isRating()){
        	ratingDisplayItem.setVisible(true);
        	ratingDisplayBox.setChecked(question.displayRatingAsDropdownList());
        }
        if ((question.isChoice() && !question.isSingleChoice()) || question.isRelationalChoice()){
        	selectAllItem.setVisible(true);
        	selectAllBox.setChecked(question.showSelectAll());
        }
        if (question.isAttribute()){
        	allowCreateChoiceItem.setVisible(true);
        	allowCreateChoiceBox.setChecked(question.allowUserCreatedChoice());
        }
        if (question.isRelationalContinuous()){
        	recordDurationItem.setVisible(true);
        	recordDurationBox.setChecked(question.isRecordDuration());
        }
        if (question.isPerceivedChoice() || question.isRelationalChoiceMultiple()){
        	singleChoicePerLineItem.setVisible(true);
        	singleChoicePerLineBox.setChecked(question.isSingleChoicePerLine());
        }
        if (question.isRelational()){
        	contactProviderItem.setVisible(true);
        	contactChooserItem.setVisible(true);
        	negateItem.setVisible(true);
        	skipOnEmptyItem.setVisible(true);
        	hideContactChooserInstructionItem.setVisible(true);
        	
        	// contact provider
        	ListModelList<String> model = new ListModelList<String>();
        	Survey survey = SurveyUtil.getSurvey();
        	model.add("");
        	for (Page page : survey.getPages()){
        		for (Question q : page.getQuestions()){
        			if (q.isContactProvider()) model.add(q.getLabel());
        		}
        	}
        	Question cpQuestion = question.getContactProviderQuestion();
        	if (cpQuestion != null) model.addToSelection(cpQuestion.getLabel());        	
        	contactProviderBox.setModel(model);

        	// contact chooser
        	model = new ListModelList<String>();
        	model.add("");
        	for (Page page : survey.getPages()){
        		for (Question q : page.getQuestions()){
        			if (q.isContactChooser()) model.add(q.getLabel());
        		}
        	}
        	Question ccQuestion = question.getContactChooserQuestion();
        	if (ccQuestion != null) model.addToSelection(ccQuestion.getLabel());        	
        	contactChooserBox.setModel(model);
        	
        	negateBox.setChecked(question.getCCNegate());
        	skipOnEmptyBox.setChecked(question.skipOnEmpty());
        	hideContactChooserInstructionBox.setChecked(question.hideContactChooserInstruction());
        }
        if (question.isContactChooser()){
        	emptyStrategyItem.setVisible(true);
        	defaultContactTypeItem.setVisible(true);
        	showImageItem.setVisible(true);
        	selectedContactsHeaderItem.setVisible(true);
        	
        	ListModelList<String> model = new ListModelList<String>();
        	model.add("");
        	model.add(Constants.CC_EMPTY_ALL);
        	model.add(Constants.CC_EMPTY_NONE);
        	String strategy = question.getCCEmptyStrategy();
        	model.addToSelection(strategy);
        	emptyStrategyBox.setModel(model);
        	
        	NodeRO nodeRO = (NodeRO)Beans.getBean("nodeRO");
        	nodeTypeDescriptions = nodeRO.getNodeTypeDescriptions();
        	model = new ListModelList<String>();
        	model.add("");
        	for (Map<String,String> m : nodeTypeDescriptions){
        		model.add(m.get("label"));
        	}
        	String defaultNewContactType = question.getDefaultNewContactType();
        	if (defaultNewContactType != null) {
        		String nodeTypeLabel = GeneralUtil.getNodeTypeLabel(nodeTypeDescriptions, defaultNewContactType);
        		model.addToSelection(nodeTypeLabel);
        	}
        	defaultContactTypeBox.setModel(model);
        	
        	showImageBox.setChecked(question.showCCImage());
        	selectedContactsHeaderTi.setValue(question.getSelectedNodesHeader());
        }
        if (question.isContactProvider()){
        	defaultContactTypeItem.setVisible(true);
        	
        	NodeRO nodeRO = (NodeRO)Beans.getBean("nodeRO");
        	nodeTypeDescriptions = nodeRO.getNodeTypeDescriptions();
        	ListModelList<String> model = new ListModelList<String>();
        	model.add("");
        	for (Map<String,String> m : nodeTypeDescriptions){
        		model.add(m.get("label"));
        	}
        	String defaultNewContactType = question.getDefaultNewContactType();
        	if (defaultNewContactType != null) {
        		String nodeTypeLabel = GeneralUtil.getNodeTypeLabel(nodeTypeDescriptions, defaultNewContactType);
        		model.addToSelection(nodeTypeLabel);
        	}
        	defaultContactTypeBox.setModel(model);
        }
        if (question.isRelational() || question.isContactChooser()){
        	showSelfItem.setVisible(true);
        	allowCreateNodeItem.setVisible(true);
        	
        	showSelfBox.setChecked(question.showMyself());
        	allowCreateNodeBox.setChecked(question.allowUserCreatedNode());        	
        }        
        if (!question.isDisplayPage()){
        	hiddenFromVisItem.setVisible(true);
        	hiddenFromVisBox.setChecked(question.isHidden());
        }
        
        Selectors.wireEventListeners(this, this);
        
        logger.info("Created option tab.");
    }
    
    @Listen("onCheck = #isSingleChoiceBox")
    public void onSingleChoiceBoxChecked(){
    	if (isSingleChoiceBox.isChecked()){
    		maxChoiceItem.setVisible(false);
    		selectAllItem.setVisible(false);
    		singleChoiceRenderItem.setVisible(true);
    	} else {
    		maxChoiceItem.setVisible(true);
    		selectAllItem.setVisible(true);
    		singleChoiceRenderItem.setVisible(false);
    	}
    }
    
    /*
     * Collect options into question.attributes.
     */
    @SuppressWarnings("rawtypes")
	public void collectOptions(){
        if (isSingleChoiceItem.isVisible() && isSingleChoiceBox.isChecked()){
        	question.setAttribute(Constants.OPTION_SINGLE_CHOICE, "1");
        } else question.getAttributes().remove(Constants.OPTION_SINGLE_CHOICE);
       
        if (singleChoiceRenderItem.isVisible() && singleChoiceRenderBox.isChecked()){
        	question.setAttribute(Constants.SHOW_SINGLE_CHOICE_AS_LIST, "1");
        } else question.getAttributes().remove(Constants.SHOW_SINGLE_CHOICE_AS_LIST);
        
        if (maxChoiceItem.isVisible()){
        	Integer max = maxChoiceSpinner.getValue();
        	if (max == 0){
        		max = question.getFields().size();
        	}
        	question.setAttribute(Constants.CHOICE_LIMIT, max.toString());
        } else question.getAttributes().remove(Constants.CHOICE_LIMIT);
        
        if (jumpConditionItem.isVisible()){
        	ListModelList model = (ListModelList)jumpConditionBox.getModel();
        	String fieldLabel = "";
        	if (!model.getSelection().isEmpty()){
        		fieldLabel = (String) model.getSelection().iterator().next();
        	}
        	Field field = question.getFieldByLabel(fieldLabel);
        	if (field == null) question.getAttributes().remove(Constants.JUMP_CONDITION);
        	else question.setAttribute(Constants.JUMP_CONDITION, field.getName());
        }
        if (jumpQuestionItem.isVisible()){
        	ListModelList model = (ListModelList)jumpQuestionBox.getModel();
        	String questionLabel = "";
        	if (!model.getSelection().isEmpty()){
        		questionLabel = (String) model.getSelection().iterator().next();
        	}
        	Question q = SurveyUtil.getQuestionByLabel(SurveyUtil.getAllQuestions(), questionLabel);
        	if (q == null) question.getAttributes().remove(Constants.JUMP_QUESTION);
        	else question.setAttribute(Constants.JUMP_QUESTION, q.getShortName());
        }
        
        if (selectAllItem.isVisible() && selectAllBox.isChecked()){
        	question.setAttribute(Constants.SHOW_SELECT_ALL, "1");
        } else question.getAttributes().remove(Constants.SHOW_SELECT_ALL);
        
        if (ratingDisplayItem.isVisible() && ratingDisplayBox.isChecked()){
        	question.setAttribute(Constants.DISPLAY_RATING_AS_DROPDOWN_LIST, "1");
        } else question.getAttributes().remove(Constants.DISPLAY_RATING_AS_DROPDOWN_LIST);
        
        if (allowCreateChoiceItem.isVisible() && allowCreateChoiceBox.isChecked()){
        	question.setAttribute(Constants.OPTION_ALLOW_USER_CREATE_CHOICE, "1");
        } else question.getAttributes().remove(Constants.OPTION_ALLOW_USER_CREATE_CHOICE);
        
        if (recordDurationItem.isVisible() && recordDurationBox.isChecked()){
        	question.setAttribute(Constants.RECORD_DURATION, "1");
        } else question.getAttributes().remove(Constants.RECORD_DURATION);
        
        if (singleChoicePerLineItem.isVisible() && singleChoicePerLineBox.isChecked()){
        	question.setAttribute(Constants.SINGLE_CHOICE_PER_LINE, "1");
        } else question.getAttributes().remove(Constants.SINGLE_CHOICE_PER_LINE);
        
        if (contactProviderItem.isVisible()){
        	ListModelList model = (ListModelList)contactProviderBox.getModel();
        	Set selections = model.getSelection();
        	String selection = "";
        	if (!selections.isEmpty()){
        		selection = (String)selections.iterator().next();
        	}
    		if (!selection.isEmpty()){
    			Question cpQuestion = SurveyUtil.getQuestionByLabel(SurveyUtil.getAllQuestions(), selection);
    			if (cpQuestion != null) question.setAttribute(Constants.CP_SHORT_NAME, cpQuestion.getShortName());
    			else question.getAttributes().remove(Constants.CP_SHORT_NAME);
    		} else question.getAttributes().remove(Constants.CP_SHORT_NAME);
        }
        
        if (contactChooserItem.isVisible()){
        	ListModelList model = (ListModelList)contactChooserBox.getModel();
        	Set selections = model.getSelection();
        	String selection = "";
        	if (!selections.isEmpty()){
        		selection = (String)selections.iterator().next();
        	}
    		if (!selection.isEmpty()){
    			Question ccQuestion = SurveyUtil.getQuestionByLabel(SurveyUtil.getAllQuestions(), selection);
    			if (ccQuestion != null) question.setAttribute(Constants.CC_SHORT_NAME, ccQuestion.getShortName());
    			else question.getAttributes().remove(Constants.CC_SHORT_NAME);
    		} else question.getAttributes().remove(Constants.CC_SHORT_NAME);
        }
        
        if (negateItem.isVisible() && negateBox.isChecked()){
        	question.setAttribute(Constants.CC_NEGATE, "1");
        } else question.getAttributes().remove(Constants.CC_NEGATE);
        
        if (skipOnEmptyItem.isVisible() && skipOnEmptyBox.isChecked()){
        	question.setAttribute(Constants.SKIP_ON_EMPTY, "1");
        } else question.getAttributes().remove(Constants.SKIP_ON_EMPTY);
        
        if (hideContactChooserInstructionItem.isVisible() && hideContactChooserInstructionBox.isChecked()){
        	question.setAttribute(Constants.HIDE_CONTACT_CHOOSER_INSTRUCTION, "1");
        } else question.getAttributes().remove(Constants.HIDE_CONTACT_CHOOSER_INSTRUCTION);
        
        if (emptyStrategyItem.isVisible()){
        	ListModelList model = (ListModelList)emptyStrategyBox.getModel();
        	Set selections = model.getSelection();
        	String selection = "";
        	if (!selections.isEmpty()){
        		selection = (String)selections.iterator().next();
        	}
    		if (!selection.isEmpty()){
    			question.setAttribute(Constants.CC_EMPTY, selection);
    		} else question.getAttributes().remove(Constants.CC_EMPTY);
        }
        
        if (defaultContactTypeItem.isVisible()){
        	ListModelList model = (ListModelList)defaultContactTypeBox.getModel();
        	Set selections = model.getSelection();
        	String selection = "";
        	if (!selections.isEmpty()){
        		selection = (String)selections.iterator().next();
        	}
    		if (!selection.isEmpty()){
    			Map<String, String> ntd = GeneralUtil.getNodeDescriptionByLabel(nodeTypeDescriptions, selection);
    			String nodeType = ntd.get("type");
    			question.setAttribute(Constants.NEW_CONTACT_DEFAULT_TYPE, nodeType);
    		} else question.getAttributes().remove(Constants.NEW_CONTACT_DEFAULT_TYPE);
        }
        
        if (showImageItem.isVisible() && showImageBox.isChecked()){
        	question.setAttribute(Constants.SHOW_IMAGE, "1");
        } else question.getAttributes().remove(Constants.SHOW_IMAGE);
        
        if (selectedContactsHeaderItem.isVisible()){
        	String header = selectedContactsHeaderTi.getValue();
        	question.setAttribute(Constants.CC_SELECTED_NODES_HEADER, header);
        } else question.getAttributes().remove(Constants.CC_SELECTED_NODES_HEADER);
        
        if (showSelfItem.isVisible() && showSelfBox.isChecked()){
        	question.setAttribute(Constants.OPTION_SHOW_SELF, "1");
        } else question.getAttributes().remove(Constants.OPTION_SHOW_SELF);
        
        if (allowCreateNodeItem.isVisible() && allowCreateNodeBox.isChecked()){
        	question.setAttribute(Constants.ALLOW_USER_CREATED_NODE, "1");
        } else question.getAttributes().remove(Constants.ALLOW_USER_CREATED_NODE);
        
        if (hiddenFromVisItem.isVisible() && hiddenFromVisBox.isChecked()){
        	question.setAttribute(Constants.OPTION_HIDDEN, "1");
        } else question.getAttributes().remove(Constants.OPTION_HIDDEN);
    }
}
