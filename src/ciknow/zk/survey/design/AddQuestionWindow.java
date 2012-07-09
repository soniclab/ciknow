package ciknow.zk.survey.design;

import ciknow.dao.GroupDao;
import ciknow.dao.PageDao;
import ciknow.dao.QuestionDao;
import ciknow.domain.Group;
import ciknow.domain.Page;
import ciknow.domain.Question;
import ciknow.util.Constants;
import ciknow.util.GeneralUtil;
import ciknow.util.SurveyUtil;
import ciknow.zk.survey.response.SurveyPage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.InputEvent;
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
public class AddQuestionWindow extends Window {

    private static final long serialVersionUID = -3902957071713445754L;
    private static Log logger = LogFactory.getLog(AddQuestionWindow.class);
    @WireVariable
    private GroupDao groupDao;
    @WireVariable
    private QuestionDao questionDao;
    @WireVariable
    private PageDao pageDao;
    @Wire
    private Textbox labelBox;
    @Wire
    private Textbox nameBox;
    @Wire
    private Listbox typesBox;
    @Wire
    private Listbox groupsBox;
    private Map<String, Group> groupMap;

    public AddQuestionWindow(Component parent) {
        this.setParent(parent);
        this.setClosable(true);
        this.setSclass("addQuestionWindow");

        // create UI from template
        Executions.createComponents("/WEB-INF/zk/survey/design/AddQuestionWindow.zul", this, null);

        Selectors.wireVariables(this, this, Selectors.newVariableResolvers(getClass(), Div.class));
        Selectors.wireComponents(this, this, false);
        Selectors.wireEventListeners(this, this);
        
        // populate type box
        ListModelList<String> typesModel = new ListModelList<String>(Constants.questionTypes);
        typesBox.setModel(typesModel);

        // populate group box
        groupMap = new HashMap<String, Group>();
        List<Group> groups = groupDao.getAll();
        ListModelList<String> groupsModel = new ListModelList<String>();
        for (Group group : groups) {
        	if (group.isPrivate() || group.isProvider()) continue;
            groupMap.put(group.getName(), group);
            groupsModel.add(group.getName());
        }
        groupsModel.setMultiple(true); // In ZK version 6, multiple has to be set on the model if model is used
        groupsBox.setModel(groupsModel);
    }

    @Listen("onChanging = #nameBox")
    public void onChanging$nameBox(InputEvent e) {
        String value = e.getValue();
        labelBox.setValue(value);
    }

    @Listen("onClick = #createBtn")
    public void create() throws Exception {
        logger.debug("on create");

        // validate
        Question question;
        String shortName = nameBox.getValue().trim();
        if (!GeneralUtil.isValidName(shortName)) return;                
        question = questionDao.findByShortName(shortName);
        if (question != null) {
            Messagebox.show("Question Name: " + shortName + " has been used. Try a different one.");
            return;
        }
        String label = labelBox.getValue().trim();
        if (!GeneralUtil.isValidLabel(label)) return; 
        
        Listitem selectedTypeItem = typesBox.getSelectedItem();
        if (selectedTypeItem == null) {
            Messagebox.show("Question Type is required.");
            return;
        }
        String type = (String) selectedTypeItem.getValue();

        Set<Object> selectedGroups = ((ListModelList<Object>) groupsBox.getModel()).getSelection();
        if (selectedGroups.isEmpty()) {
            Messagebox.show("Respondent Group(s) are required.");
            return;
        }

        // create new question
        question = new Question();
        question.setShortName(shortName);
        question.setLabel(label);
        question.setType(type);
        question.setRowPerPage(20);
        for (Object o : selectedGroups) {
            String groupName = (String) o;
            Group group = groupMap.get(groupName);
            question.getVisibleGroups().add(group);
        }
        question.getAvailableGroups().add(groupMap.get(Constants.GROUP_ALL));

        // save to database
        Page currentPage = SurveyUtil.getCurrentPage();
        currentPage.getQuestions().add(question);
        question.setPage(currentPage);
        try {
            pageDao.save(currentPage);
        } catch (Exception e) {
            logger.warn(e.getMessage());
            e.printStackTrace();
            Messagebox.show("Failed to add new question. Press OK to refersh and try again.", "",
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
        surveyPage.addQuestionToPage(question);

        this.setParent(null);
    }

    @Listen("onClick = #cancelBtn")
    public void cancel() {
        logger.debug("on cancel");
        this.setParent(null);
    }
}
