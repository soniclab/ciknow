package ciknow.zk.survey.response;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;

import ciknow.domain.ContactField;
import ciknow.domain.Field;
import ciknow.domain.Node;
import ciknow.domain.Question;
import ciknow.domain.TextField;
import ciknow.util.GeneralUtil;
import ciknow.zk.survey.design.AddContactFieldWindow;
import ciknow.zk.survey.design.AddFieldWindow;
import ciknow.zk.survey.design.AddTextFieldWindow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zkoss.zk.ui.select.annotation.Listen;

/**
 *
 * @author gyao
 */
public class AbstractQuestionAttribute extends AbstractQuestion {

    private static final long serialVersionUID = 4444655066048606075L;
    private static final Log logger = LogFactory.getLog(AbstractQuestionAttribute.class);
    
    public AbstractQuestionAttribute(Question currentQuestion) {
        super(currentQuestion);
    }

    public AbstractQuestionAttribute(Node respondent, Question currentQuestion) {
        super(respondent, currentQuestion);
    }

    @Override
    public void afterCreationComplete() {
        super.afterCreationComplete();        
    }

    @Listen("onClick = #addContactFieldBtn")
    @Override
    public void addContactField() {
        // save any existing answers because current page will be refresh
        if (!validate()) {
            return;
        }
        respondent.update(nodeDao.loadById(respondent.getId()));
        save();

        AddContactFieldWindow addContactFieldWindow = new AddContactFieldWindow(this);
        addContactFieldWindow.doModal();
    }

    @Listen("onClick = #addTextFieldBtn")
    @Override
    public void addTextField() {
        // save any existing answers because current page will be refresh
        if (!validate()) {
            return;
        }
        respondent.update(nodeDao.loadById(respondent.getId()));
        save();

        AddTextFieldWindow addTextFieldWindow = new AddTextFieldWindow(this);
        addTextFieldWindow.doModal();
    }

    @Listen("onClick = #addFieldBtn")
    @Override
    public void addField() {
        // save any existing answers because current page will be refresh
        if (!validate()) {
            return;
        }
        respondent.update(nodeDao.loadById(respondent.getId()));
        save();

        AddFieldWindow addFieldWindow = new AddFieldWindow(this);
        addFieldWindow.doModal();
    }
    
    protected int getFirstColumnWidth(){  
    	int width = 150; // default and minimum width    	    	
    	
    	int sum = 0;
    	int average = 0;
    	FontRenderContext frc = new FontRenderContext(new AffineTransform(),true,true);
    	Font font = new Font("Arial", Font.PLAIN, 12);
    	if (currentQuestion.isText()){
    		if (currentQuestion.getTextFields().isEmpty()) return width;
    		for (TextField field : currentQuestion.getTextFields()){
    			sum += GeneralUtil.measureString(field.getLabel(), font, frc);
    		}
    		average = sum/currentQuestion.getTextFields().size();
    	} else if (currentQuestion.isContactInfo()){
    		if (currentQuestion.getContactFields().isEmpty()) return width;
    		for (ContactField field : currentQuestion.getContactFields()){
    			sum += GeneralUtil.measureString(field.getLabel(), font, frc);
    		}
    		average = sum/currentQuestion.getContactFields().size();
    	} else {
    		if (currentQuestion.getFields().isEmpty()) return width;
    		for (Field field : currentQuestion.getFields()){
    			sum += GeneralUtil.measureString(field.getLabel(), font, frc);
    		}
    		average = sum/currentQuestion.getFields().size();
    	}
    	
    	if (average > width) width = average;
    	logger.info("Get first column width: " + width + ", average: " + average);
    	
    	return width;
    }
}
