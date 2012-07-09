package ciknow.zk.survey.response;

import ciknow.domain.Node;
import ciknow.domain.Question;
import ciknow.zk.survey.design.AddScaleWindow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Space;

/**
 *
 * @author gyao
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class AbstractQuestion extends SurveyQuestionBase {

    private static final long serialVersionUID = -2333198966524673201L;
    private static final Log logger = LogFactory.getLog(AbstractQuestion.class);

    @Wire
    protected Checkbox selectAllBox;
    @Wire
    protected Grid grid;
    @Wire
    protected Space bottomSpacer;

    public AbstractQuestion(Question currentQuestion) {
        super(currentQuestion);
    }

    public AbstractQuestion(Node respondent, Question currentQuestion) {
        super(respondent, currentQuestion);
    }

    @Override
    protected void createUI() {
        Executions.createComponents("/WEB-INF/zk/survey/response/AbstractQuestion.zul", this, null);
    }

    /**
     * Perform operations after UI creation, wiring and forwarding
     */
    @Override
    protected void afterCreationComplete() {
        super.afterCreationComplete();
    }

    @Listen("onClick = #addScaleBtn")
    @Override
    public void addScale() {
        logger.debug("Adding new scale...");

        // save any existing answers because current page will be refresh
        if (!validate()) {
            return;
        }
        respondent.update(nodeDao.loadById(respondent.getId()));
        save();

        AddScaleWindow win = new AddScaleWindow(this);
        win.doModal();
    }
    /*
     * In this new system, "other" is defined by respondent on specified
     * question and save as node attribute: question_short_name`other
     *
     * protected void getOtherInfo(AbstractQuestion qc) throws Exception {
     * GetOtherWindow win = new GetOtherWindow(this); this.appendChild(win);
     * win.doModal(); }
     *
     * public void gotOtherInfo(String other) throws Exception { // to be
     * implemented by Choice, MultipleChoice, RelationalChoiceMultiple throw new
     * Exception("not implemented."); }
     */
}
