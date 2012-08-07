package ciknow.zk.survey.design;

import ciknow.domain.Question;
import ciknow.util.SurveyUtil;
import ciknow.zk.survey.response.ContactChooser;
import ciknow.zk.survey.response.ISurveyQuestion;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Div;
import org.zkoss.zul.Window;

/**
 *
 * @author gyao
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class AddNodeViaContactChooserWindow extends Window {

    private static final long serialVersionUID = 507011502637726908L;
    private static Log logger = LogFactory.getLog(AddNodeViaContactChooserWindow.class);
    @Wire
    private Div questionArea;
    private ContactChooser cc;

    public AddNodeViaContactChooserWindow(ISurveyQuestion parent) {
        this.setParent(parent);

        // create UI from template
        Executions.createComponents("/WEB-INF/zk/survey/design/AddNodeViaContactChooserWindow.zul", this, null);
        Selectors.wireVariables(this, this, Selectors.newVariableResolvers(getClass(), Div.class));
        Selectors.wireComponents(this, this, false);
        Selectors.wireEventListeners(this, this);

        // 
        Question ccQuestion = parent.getQuestion().getContactChooserQuestion();
        cc = new ContactChooser(SurveyUtil.getRespondent(), ccQuestion);
        questionArea.appendChild(cc);
        if (parent.getQuestion().hideContactChooserInstruction()) {
            cc.removeInstruction();
        }
        this.setClosable(true);
    }

    @Listen("onClick = #saveBtn")
    public void onClick$saveBtn() throws Exception {
        logger.debug("on create");
        cc.save();

        // update interface
        ISurveyQuestion parent = (ISurveyQuestion) getParent();
        parent.refresh();
        this.setParent(null);
    }

    @Listen("onClick = #cancelBtn")
    public void onClick$cancelBtn() throws Exception {
        logger.debug("on cancel");
        this.setParent(null);
    }
}
