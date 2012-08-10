package ciknow.zk.survey.response;

import ciknow.domain.Node;
import ciknow.domain.Question;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.DropEvent;

/**
 *
 * @author gyao
 *
 */
public interface ISurveyQuestion extends Component {

    public Node getRespondent();

    public Question getQuestion();

    // set question header
    public void setTitle();

    // set question instruction content
    public void setInstruction();

    public void addField();

    public void addTextField();

    public void addContactField();

    public void addScale();

    public void addNode();

    public void updateQuestionSettings();

    public void deleteQuestion();

    public void moveQuestion(DropEvent event);

    /**
     * Validate question answers
     */
    public boolean validate();

    /**
     * Save question answers
     */
    @Transactional
    public void save();

    /**
     * Set Error Message
     */
    public void setErrorMsg(String msg);

    /**
     * Refresh current question interface
     */
    public void refresh();

    /**
     * Refresh whole page interface
     */
	public void refreshWholePage();
}
