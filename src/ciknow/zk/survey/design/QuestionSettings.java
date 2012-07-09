package ciknow.zk.survey.design;

import ciknow.dao.GroupDao;
import ciknow.dao.QuestionDao;
import ciknow.domain.*;
import ciknow.ro.QuestionRO;
import ciknow.util.GeneralUtil;
import ciknow.zk.survey.response.ISurveyQuestion;

import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zkforge.ckez.CKeditor;
import org.zkoss.zk.ui.Executions;
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
public class QuestionSettings extends Window {

    private static final long serialVersionUID = -3902957071713495737L;
    private static Log logger = LogFactory.getLog(QuestionSettings.class);
    @WireVariable
    private QuestionDao questionDao;
    @WireVariable
    private QuestionRO questionRO;
    @WireVariable
    private GroupDao groupDao;
    
    @Wire
    private Label idLabel;
    @Wire
    private Label typeLabel;
    @Wire
    private Label shortNameLabel;
    @Wire
    private Textbox labelBox;
    @Wire
    private Intbox pageSizeBox;
    
    //@Wire private Tab instructionTab;
    //@Wire private Tabpanel instructionPanel;
    @Wire
    private CKeditor editorBox;
    
    @Wire
    private Tab fieldsTab;
    @Wire
    private Tabpanel fieldsPanel;
    
    @Wire
    private Tab scalesTab;
    @Wire
    private Tabpanel scalesPanel;

    @Wire
    private Tab textFieldsTab;
    @Wire
    private Tabpanel textFieldsPanel;

    @Wire
    private Tab contactFieldsTab;
    @Wire
    private Tabpanel contactFieldsPanel;

    @Wire
    private Tab ccConfigTab;
    @Wire
    private Tabpanel ccConfigPanel;
    
    @Wire
    private Tab ccSortTab;
    @Wire
    private Tabpanel ccSortPanel;
    
    //@Wire private Tab respondentGroupsTab;
    //@Wire private Tabpanel respondentGroupsPanel;
    @Wire
    private Listbox respondentGroupBox;
    
    @Wire
    private Tab rowGroupsTab;
    @Wire
    private Tabpanel rowGroupsPanel;
    @Wire
    private Listbox rowGroupBox;
    
    @Wire
    private Tab columnGroupsTab;
    @Wire
    private Tabpanel columnGroupsPanel;
    @Wire
    private Listbox columnGroupBox;
    
    //@Wire private Tab optionTab;
    @Wire private Tabpanel optionPanel;   
    private QuestionSettingsOption option;
    
    private Question question;
    private Map<String, ciknow.domain.Group> groupMap;
    
    
    public QuestionSettings(ISurveyQuestion parent) {
    	logger.info("Creating question settings window...");
        this.setParent(parent);

        // create ui from template
        Executions.createComponents("/WEB-INF/zk/survey/design/QuestionSettings.zul", this, null);
        this.setClosable(true);
        this.setSclass("questionSettingsWindow");

        Selectors.wireVariables(this, this, Selectors.newVariableResolvers(getClass(), Div.class));
        Selectors.wireComponents(this, this, false);
        Selectors.wireEventListeners(this, this);
        
        // Create Additional UI
        question = parent.getQuestion();
        List<ciknow.domain.Group> groups = groupDao.getAll(); // TODO getAll() may contain lots of private groups and slow down performance
        groupMap = new HashMap<String, ciknow.domain.Group>();
        List<String> groupNameList = new ArrayList<String>();
        for (ciknow.domain.Group group : groups) {
            if (group.isPrivate() || group.isProvider()) continue;
            groupNameList.add(group.getName());
            groupMap.put(group.getName(), group);
        }
        Collections.sort(groupNameList);
        
        // basic info
        idLabel.setValue(question.getId().toString());
        typeLabel.setValue(question.getType());
        shortNameLabel.setValue(question.getShortName());
        labelBox.setValue(question.getLabel());
        if (question.isRelational()) {
            pageSizeBox.setValue(question.getRowPerPage());
        } else {
            pageSizeBox.setDisabled(true);
        }

        // instruction
        editorBox.setValue(question.getHtmlInstruction());

        // fields        
        if (question.isChoice()
                || question.isRating()
                || question.isContinuous()
                || question.isDuration()
                || question.isTextLong()
                || question.isMultipleChoice()
                || question.isMultipleRating()
                || question.isRelationalChoiceMultiple()
                || question.isRelationalRatingMultiple()
                || question.isPerceivedChoice()
                || question.isPerceivedRating()) {
            fieldsTab.setVisible(true);
            fieldsPanel.setVisible(true);

            // set tab name
            if (question.isChoice()) {
                fieldsTab.setLabel("Categories");
            } else if (question.isRelationalChoiceMultiple()
                    || question.isRelationalRatingMultiple()) {
                fieldsTab.setLabel("Relations");
            } else {
                fieldsTab.setLabel("Fields");
            }

            new QuestionSettingsField(fieldsPanel, question);
        }

        // ratings        
        if (question.isRating()
                || question.isMultipleRating()
                || question.isRelationalRating()
                || question.isRelationalRatingMultiple()
                || question.isPerceivedRating()
                || question.isPerceivedRelationalRating()) {
            scalesTab.setVisible(true);
            scalesPanel.setVisible(true);

            new QuestionSettingsScale(scalesPanel, question);
        }

        // text fields
        if (question.isText()
                || question.isMultipleChoice()
                || question.isMultipleRating()) {
            textFieldsTab.setVisible(true);
            textFieldsPanel.setVisible(true);

            // populate list
            new QuestionSettingsTextField(textFieldsPanel, question);
        }

        // contact fields
        if (question.isContactInfo()) {
            contactFieldsTab.setVisible(true);
            contactFieldsPanel.setVisible(true);

            new QuestionSettingsContactField(contactFieldsPanel, question);
        }

        // contact chooser
        if (question.isContactChooser()) {
            ccConfigTab.setVisible(true);
            ccConfigPanel.setVisible(true);
            new QuestionSettingsCCConfig(ccConfigPanel, question);
            
            ccSortTab.setVisible(true);
            ccSortPanel.setVisible(true);
            new QuestionSettingsCCSort(ccSortPanel, question);
        }

        // respondent groups                
        ListModelList<String> respondentGroupModel = new ListModelList<String>(groupNameList, true);        
        respondentGroupModel.setMultiple(true); // In ZK version 6, multiple has to be set on the model if model is used
        for (ciknow.domain.Group group : question.getVisibleGroups()){
            respondentGroupModel.addToSelection(group.getName());
        }
        respondentGroupBox.setModel(respondentGroupModel);
        
        // row groups
        if (question.isRelational() || question.isContactChooser()) {
            rowGroupsTab.setVisible(true);
            rowGroupsPanel.setVisible(true);
            
            ListModelList<String> rowGroupModel = new ListModelList<String>(groupNameList, true);
            rowGroupModel.setMultiple(true);
            for (ciknow.domain.Group group : question.getAvailableGroups()) {
                rowGroupModel.addToSelection(group.getName());
            }
            rowGroupBox.setModel(rowGroupModel);
        }

        // column groups
        if (question.isPerceivedRelationalChoice()
                || question.isPerceivedRelationalRating()) {
            columnGroupsTab.setVisible(true);
            columnGroupsPanel.setVisible(true);
            
            ListModelList<String> columnGroupModel = new ListModelList<String>(groupNameList, true);
            columnGroupModel.setMultiple(true);
            for (ciknow.domain.Group group : question.getAvailableGroups2()) {
                columnGroupModel.addToSelection(group.getName());
            }
            columnGroupBox.setModel(columnGroupModel);            
        }

        // options tab
        option = new QuestionSettingsOption(optionPanel, question);

        logger.info("Created question settings window.");
    }
    
    
    
    @SuppressWarnings("rawtypes")
	@Listen("onClick = #saveBtn")
    public void save() {
    	String label = labelBox.getValue().trim();
    	if (!GeneralUtil.isValidLabel(label)) return;
        question.setLabel(label);
        if (question.isRelational()) question.setRowPerPage(pageSizeBox.getValue());
        question.setHtmlInstruction(editorBox.getValue().trim());
        
        // respondent groups
        ListModelList listModel = (ListModelList)respondentGroupBox.getModel();
        Iterator itr = listModel.getSelection().iterator();
        question.getVisibleGroups().clear();        
        while (itr.hasNext()){
            String groupName = (String)itr.next();
            question.getVisibleGroups().add(groupMap.get(groupName));
        }
        
        // row groups
        if (question.isRelational() || question.isContactChooser()) {
            listModel = (ListModelList)rowGroupBox.getModel();
            itr = listModel.getSelection().iterator();
            question.getAvailableGroups().clear();        
            while (itr.hasNext()){
                String groupName = (String)itr.next();
                question.getAvailableGroups().add(groupMap.get(groupName));
            }
        }

        // column groups
        if (question.isPerceivedRelationalChoice()
                || question.isPerceivedRelationalRating()) {
            listModel = (ListModelList)columnGroupBox.getModel();
            itr = listModel.getSelection().iterator();
            question.getAvailableGroups2().clear();        
            while (itr.hasNext()){
                String groupName = (String)itr.next();
                question.getAvailableGroups2().add(groupMap.get(groupName));
            }           
        }
        
        // misc
        option.collectOptions();
        
        //questionDao.save(question);
        questionRO.saveQuestion(question);

        // update interface
        ISurveyQuestion parent = (ISurveyQuestion) getParent();
        parent.refresh();
        this.setParent(null);
    }

    @Listen("onClick = #cancelBtn")
    public void cancel() {
        question.update(questionDao.findById(question.getId()));

        // update interface
        ISurveyQuestion parent = (ISurveyQuestion) getParent();
        parent.refresh();
        this.setParent(null);
    }

}
