package ciknow.zk.survey.design;

import ciknow.dao.QuestionDao;
import ciknow.domain.Question;
import ciknow.domain.Scale;
import ciknow.ro.QuestionRO;
import ciknow.util.GeneralUtil;
import ciknow.zk.survey.response.ISurveyQuestion;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zkoss.zk.ui.Executions;
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
public class AddScaleWindow extends Window {

    private static final long serialVersionUID = -3902957071713495757L;
    private static Log logger = LogFactory.getLog(AddScaleWindow.class);
    @WireVariable
    private QuestionDao questionDao;
    @WireVariable
    private QuestionRO questionRO;    
    @Wire
    private Textbox labelBox;
    @Wire
    private Textbox nameBox;

    public AddScaleWindow(ISurveyQuestion parent) {
        this.setParent(parent);

        // create UI from template
        Executions.createComponents("/WEB-INF/zk/survey/design/AddScaleWindow.zul", this, null);
        this.setClosable(true);

        Selectors.wireVariables(this, this, Selectors.newVariableResolvers(getClass(), Div.class));
        Selectors.wireComponents(this, this, false);
        Selectors.wireEventListeners(this, this);
    }

    /*
    @Listen("onChanging = #nameBox")
    public void onChanging$nameBox(InputEvent e) {
        String value = e.getValue();
        labelBox.setValue(value);
    }
	*/
    
    @Listen("onClick = #createBtn")
    public void create() {
        ISurveyQuestion parent = (ISurveyQuestion) getParent();
        Question question = parent.getQuestion();
        question.update(questionDao.findById(question.getId()));

        // validate
        String name = nameBox.getValue().trim();
        String label = labelBox.getValue().trim();
        if (!GeneralUtil.isValidName(name)) return;
        if (!GeneralUtil.isValidLabel(label)) return;   
        
        // create new field		
        Scale scale = new Scale();
        scale.setName(name);
        scale.setLabel(label);
        if (question.getScales().contains(scale)) {
            Messagebox.show("The scale name (" + name + ") has been used.");
            return;
        } else {
            question.getScales().add(scale);
            scale.setQuestion(question);
            scale.setValue(new Double(scale.getIndex()));
        }

        // add new scale to question and save to database
        //questionDao.save(question);
        questionRO.saveQuestion(question);
        logger.debug("Scale created: " + name + "=" + label);

        // update interface
        parent.refresh();
        this.setParent(null);
    }

    @Listen("onClick = #cancelBtn")
    public void cancel() {
        this.setParent(null);
    }
}
