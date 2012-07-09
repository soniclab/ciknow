package ciknow.zk.survey.response;

import ciknow.domain.Node;
import ciknow.domain.Page;
import ciknow.domain.Question;
import ciknow.util.SurveyUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Vlayout;

/**
 *
 * @author gyao
 */
public class SurveyPage extends Vlayout implements IdSpace {

    private static final long serialVersionUID = -2333198966524673201L;
    private static final Log logger = LogFactory.getLog(SurveyPage.class);

    public SurveyPage(){

    }
    
    public void render(Page page) {
        // clear all children
        SurveyUtil.removeAllChildren(this);

        // populate questions
        Node respondent = SurveyUtil.getRespondent();
        for (Question question : page.getQuestions()) {
        	if (!question.isVisible(respondent)) { 
        		continue;
        	}
        	if (SurveyUtil.skipOnEmpty(question)) continue;
        	
            addQuestionToPage(question);
        }
        
        /*
        // test: scroll the last question into viewport
        Question lastQuestion = page.getQuestions().get(page.getQuestions().size() - 1);
        ISurveyQuestion c = findQuestionComponent(lastQuestion);
        //Clients.scrollIntoView(c);
        Clients.response(new AuInvoke((Component)c, "scrollIntoView"));
        logger.debug(new Date().toString());
        //Messagebox.show(lastQuestion.getLabel());
        */
    }

    public void addQuestionToPage(Question question) {
        ISurveyQuestion component = createQuestionComponent(question);
        component.setParent(this);
    }

    public void removeQuestionFromPage(Question question) {
        ISurveyQuestion component = findQuestionComponent(question);
        int index = getChildren().indexOf(component);
        if (component != null) {
            component.setParent(null);

            // update title for subsequent questions
            for (; index < getChildren().size(); index++) {
                component = (ISurveyQuestion) getChildren().get(index);
                component.setTitle();
            }
        } else {
            logger.warn("Cannot find question: " + question.getLabel() + " to be removed.");
        }
    }

    public void updateQuestionInPage(Question question) {
        ISurveyQuestion component = findQuestionComponent(question);
        ISurveyQuestion newComponent = createQuestionComponent(question);
        int currentIndex = getChildren().indexOf(component);
        removeChild(component);
        if (currentIndex < getChildren().size()) {
            insertBefore(newComponent, getChildren().get(currentIndex));
        } else {
            appendChild(newComponent);
        }
    }

    public boolean validate() {
        for (Component child : getChildren()) {
            ISurveyQuestion component = (ISurveyQuestion) child;
            if (!component.validate()) {
                return false;
            }
        }
        return true;
    }

    @Transactional
    public boolean save() {
        try {
            for (Component child : getChildren()) {
                ISurveyQuestion component = (ISurveyQuestion) child;
                component.save();
            }
        } catch (Exception e) {
            logger.warn(e.getMessage());
            e.printStackTrace();
            Messagebox.show(e.getMessage());
            return false;
        }

        return true;
    }

    public static ISurveyQuestion createQuestionComponent(Question question) {
        return createQuestionComponent(SurveyUtil.getRespondent(), question);
    }

    public static ISurveyQuestion createQuestionComponent(Node respondent, Question question) {
        ISurveyQuestion component;

        if (question.isChoice()) {
            component = new Choice(respondent, question);
        } else if (question.isRating()) {
            component = new Rating(respondent, question);
        } else if (question.isContinuous()) {
            component = new Continuous(respondent, question);
        } else if (question.isText()) {
            component = new Text(respondent, question);
        } else if (question.isTextLong()) {
            component = new TextLong(respondent, question);
        } else if (question.isDuration()) {
            component = new Duration(respondent, question);
        } else if (question.isContactInfo()) {
            component = new ContactInfo(respondent, question);
        } else if (question.isMultipleChoice()) {
            component = new MultipleChoice(respondent, question);
        } else if (question.isMultipleRating()) {
            component = new MultipleRating(respondent, question);
        } 
        
        else if (question.isContactChooser()) {
            component = new ContactChooser(respondent, question);
        } else if (question.isContactProvider()) {
            component = new ContactProvider(respondent, question);
        }
        
        else if (question.isRelationalChoice()) {
            component = new RelationalChoice(respondent, question);
        } else if (question.isRelationalRating()) {
            component = new RelationalRating(respondent, question);
        } else if (question.isRelationalContinuous()) {
            component = new RelationalContinuous(respondent, question);
        } else if (question.isPerceivedChoice()) {
            component = new PerceivedChoice(respondent, question);
        } else if (question.isPerceivedRating()) {
            component = new PerceivedRating(respondent, question);
        } else if (question.isPerceivedRelationalChoice()) {
            component = new PerceivedRelationalChoice(respondent, question);
        } else if (question.isPerceivedRelationalRating()) {
            component = new PerceivedRelationalRating(respondent, question);
        } else if (question.isRelationalChoiceMultiple()) {
            component = new RelationalChoiceMultiple(respondent, question);
        } else if (question.isRelationalRatingMultiple()) {
            component = new RelationalRatingMultiple(respondent, question);
        } 
        
        else {
            component = new DisplayPage(respondent, question);
        }

        component.setId("question" + question.getId());
        return component;
    }

    private ISurveyQuestion findQuestionComponent(Question question) {
        ISurveyQuestion component = (ISurveyQuestion) getFellow("question" + question.getId());
        return component;
    }
}
