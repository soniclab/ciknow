package ciknow.tmp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ciknow.dao.GroupDao;
import ciknow.dao.PageDao;
import ciknow.dao.SurveyDao;
import ciknow.domain.Group;
import ciknow.domain.Page;
import ciknow.domain.Question;
import ciknow.domain.Survey;
import ciknow.util.Beans;
import ciknow.util.SurveyUtil;

public class PageCopyWithDynamicGroup {
	private static final Log logger = LogFactory.getLog(PageCopyWithDynamicGroup.class);
	
	public static void main(String[] args) throws IOException{
		Beans.init();
		PageDao pageDao = (PageDao)Beans.getBean("pageDao");
		SurveyDao surveyDao = (SurveyDao)Beans.getBean("surveyDao");
		GroupDao groupDao = (GroupDao)Beans.getBean("groupDao");
		
		String pageId = "1";
		String pageNamePrefix = "CTSA";
		String groupFilename = "C:/Users/gyao/git/ciknow/web/groups.txt";
		
		// read groups
		Survey survey = surveyDao.findById(1L);
		Page page = pageDao.findById(Long.parseLong(pageId));
		BufferedReader reader = new BufferedReader(new FileReader(groupFilename));
		String line = reader.readLine();
		int count = 1;
		while (line != null){
			String groupName = line.trim();
			if (groupName.isEmpty()){
				logger.info("Empty line ignored.");
				line = reader.readLine();
				continue;
			}
			Group group = groupDao.findByName(groupName);
			if (group == null){
				logger.error("Cannot find group: " + groupName);
				System.exit(0);
			}
			
			// check pageName
			String pageName = pageNamePrefix + count;
			logger.info("pageName: " + pageName);
			if (SurveyUtil.getPageByName(survey.getPages(), pageName) != null){
				logger.error("Page Name: " + pageName + " has been used. Try a different one.");
				System.exit(0);
			}
			
			Page newPage = new Page(page);			
			newPage.setName(pageName);
			newPage.setLabel(pageName);
			survey.getPages().add(newPage);
			
			for (Question question : newPage.getQuestions()){
				String questionName = pageName + "_" + question.getShortName();
				question.setShortName(questionName);
				question.getVisibleGroups().clear();
				question.getVisibleGroups().add(group);
				question.getAvailableGroups().clear();
				question.getAvailableGroups().add(group);
			}
			
			line = reader.readLine();
			count++;
		}
		
		surveyDao.save(survey);
		
		System.exit(0);
	}
}
