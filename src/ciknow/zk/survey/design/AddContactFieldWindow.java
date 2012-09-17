package ciknow.zk.survey.design;

import ciknow.dao.QuestionDao;
import ciknow.domain.ContactField;
import ciknow.domain.Question;
import ciknow.util.Constants;
import ciknow.util.GeneralUtil;
import ciknow.zk.survey.response.ISurveyQuestion;

import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
public class AddContactFieldWindow extends Window {

    private static final long serialVersionUID = -3902957071713495757L;
    private static Log logger = LogFactory.getLog(AddContactFieldWindow.class);
    @WireVariable
    QuestionDao questionDao;
    @Wire
    private Radio predefinedBtn;
    @Wire
    private Combobox predefinedBox;
    @Wire
    private Radio customBtn;
    @Wire
    private Textbox labelBox;
    @Wire
    private Textbox nameBox;
    private ListModelList<String> predefinedModel;

    public AddContactFieldWindow(ISurveyQuestion parent) {
        this.setParent(parent);

        // create UI from template
        Executions.createComponents("/WEB-INF/zk/survey/design/AddContactFieldWindow.zul", this, null);
        this.setClosable(true);

        Selectors.wireVariables(this, this, Selectors.newVariableResolvers(getClass(), Div.class));
        Selectors.wireComponents(this, this, false);
        Selectors.wireEventListeners(this, this);

        // post creation
        predefinedModel = new ListModelList<String>(Constants.PREDEFINED_CONTACT_FIELDS);
        predefinedModel.addToSelection(Constants.PREDEFINED_CONTACT_FIELDS.get(0));
        predefinedBox.setModel(predefinedModel);
    }

    /*
    @Listen("onChanging = #nameBox")
    public void onChanging$nameBox(InputEvent e) {
        String value = e.getValue();
        labelBox.setValue(value);
    }
	*/
    
    @Listen("onFocus = #predefinedBox")
    public void onFocus$predefinedBox() {
        predefinedBtn.setChecked(true);

        Constraint cons = null;
        labelBox.setConstraint(cons);
        nameBox.setConstraint(cons);
    }

    @Listen("onFocus = #labelBox")
    public void onFocus$labelBox() {
        customBtn.setChecked(true);

        String cons = "no empty";
        labelBox.setConstraint(cons);
        nameBox.setConstraint(cons);
    }

    @Listen("onFocus = #nameBox")
    public void onFocus$nameBox() {
        customBtn.setChecked(true);

        String cons = "no empty";
        labelBox.setConstraint(cons);
        nameBox.setConstraint(cons);
    }

    @Listen("onClick = #createBtn")
    public void create() throws Exception {
        ISurveyQuestion parent = (ISurveyQuestion) getParent();
        Question question = parent.getQuestion();
        question.update(questionDao.findById(question.getId()));

        // validate
        String name;
        String label;
        if (predefinedBtn.isChecked()) {
            Set<String> selections = predefinedModel.getSelection();
            if (selections.isEmpty()) {
                Messagebox.show("Please select a predefined contact field.");
                return;
            }
            name = selections.iterator().next();
            label = name;
        } else {
            name = nameBox.getValue().trim();
            label = labelBox.getValue().trim();
        }
        if (!GeneralUtil.isValidName(name)) return;
        if (!GeneralUtil.isValidLabel(label)) return;        
        
        // create new contact field
        ContactField field = new ContactField();
        field.setName(name);
        field.setLabel(label);
        if (question.getContactFields().contains(field)) {
            Messagebox.show("The contact field name (" + name + ") has been used.");
            return;
        } else {
            question.getContactFields().add(field);
            field.setQuestion(question);
        }

        // save to database
        questionDao.save(question);
        logger.debug("ContactField created: " + name + "=" + label);

        // update interface
        parent.refresh();
        this.setParent(null);
    }

    @Listen("onClick = #cancelBtn")
    public void cancel() {
        this.setParent(null);
    }
}
