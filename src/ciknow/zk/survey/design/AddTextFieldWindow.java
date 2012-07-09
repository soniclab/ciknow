package ciknow.zk.survey.design;

import ciknow.dao.QuestionDao;
import ciknow.domain.Question;
import ciknow.domain.TextField;
import ciknow.util.GeneralUtil;
import ciknow.zk.survey.response.ISurveyQuestion;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Div;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 *
 * @author gyao
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class AddTextFieldWindow extends Window {

    private static final long serialVersionUID = -3902957071713495757L;
    private static Log logger = LogFactory.getLog(AddTextFieldWindow.class);
    @WireVariable
    private QuestionDao questionDao;
    @Wire
    private Textbox labelBox;
    @Wire
    private Textbox nameBox;

    public AddTextFieldWindow(ISurveyQuestion parent) {
        this.setParent(parent);

        // create UI from template
        Executions.createComponents("/WEB-INF/zk/survey/design/AddTextFieldWindow.zul", this, null);
        this.setClosable(true);

        Selectors.wireVariables(this, this, Selectors.newVariableResolvers(getClass(), Div.class));
        Selectors.wireComponents(this, this, false);
        Selectors.wireEventListeners(this, this);
    }

    @Listen("onChanging = #nameBox")
    public void onChanging$nameBox(InputEvent e) {
        String value = e.getValue();
        labelBox.setValue(value);
    }

    @Listen("onClick = #createBtn")
    public void create() throws Exception {
        ISurveyQuestion parent = (ISurveyQuestion) getParent();
        Question question = parent.getQuestion();
        question.update(questionDao.findById(question.getId()));

        // validate
        String name = nameBox.getValue().trim();
        String label = labelBox.getValue().trim();
        if (!GeneralUtil.isValidName(name)) return;
        if (!GeneralUtil.isValidLabel(label)) return;  
        
        // create new field
        TextField field = new TextField();
        field.setName(name);
        field.setLabel(label);
        field.setLarge(false);	// use TextLong question type for inputing long essay
        if (question.getTextFields().contains(field)) {
            Messagebox.show("The text field name (" + name + ") has been used.");
            return;
        } else {
            question.getTextFields().add(field);
            field.setQuestion(question);
        }

        // save to database
        questionDao.save(question);
        logger.debug("TextField created: " + name + "=" + label);

        // update interface
        parent.refresh();
        this.setParent(null);
    }

    @Listen("onClick = #cancelBtn")
    public void cancel() {
        this.setParent(null);
    }
}
