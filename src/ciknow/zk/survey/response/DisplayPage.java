package ciknow.zk.survey.response;

import ciknow.domain.Node;
import ciknow.domain.Question;

/**
 *
 * @author gyao
 */
public class DisplayPage extends AbstractQuestion {

    private static final long serialVersionUID = 6742208596839377328L;

    public DisplayPage(Question currentQuestion) {
        super(currentQuestion);
    }

    public DisplayPage(Node respondent, Question currentQuestion) {
        super(respondent, currentQuestion);
    }

    @Override
    protected void afterCreationComplete() {
        super.afterCreationComplete();
        msg.setVisible(false);
        grid.setVisible(false);
        helpBtn.setVisible(false);
    }

    @Override
    public void save() {
        // do nothing
    }
}
