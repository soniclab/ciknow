package ciknow.upgrade;

import ciknow.dao.*;
import ciknow.domain.*;
import ciknow.util.Beans;
import ciknow.util.Constants;
import ciknow.util.GeneralUtil;

import java.sql.*;

/**
 *
 * @author gyao
 */
public class NameUpdater {

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        Beans.init();
        SurveyDao surveyDao = (SurveyDao) Beans.getBean("surveyDao");

        Survey survey = surveyDao.findById(1L);
        String name = survey.getName();
        name = update(name);
        survey.setName(name);
        
        for (Page page : survey.getPages()){
        	name = page.getName();
        	name = update(name);
        	page.setName(name);
        	
        	for (Question question : page.getQuestions()){
        		name = question.getShortName();
        		name = update(name);
        		question.setShortName(name);
        		
        		for (Field field : question.getFields()){
        			name = field.getName();
        			name = update(name);
        			field.setName(name);
        		}
        		
        		for (Scale scale : question.getScales()){
        			name = scale.getName();
        			name = update(name);
        			scale.setName(name);
        		}
        		
        		for (TextField field : question.getTextFields()){
        			name = field.getName();
        			name = update(name);
        			field.setName(name);
        		}
        		
        		for (ContactField field : question.getContactFields()){
        			name = field.getName();
        			name = update(name);
        			field.setName(name);
        		}
        	}
        }       

        surveyDao.save(survey);
        
        
        // update username as well?
        
        
        System.exit(0);
    }
    
    private static String update(String name){
    	name = GeneralUtil.replaceSpecialCharacter(name, "_");
    	name = name.replace(" ", "_");
    	name = name.replace(Constants.SEPERATOR, "_");
    	return name;
    }
}
