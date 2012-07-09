package ciknow.dao.hibernate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestExecutionListeners;

import ciknow.dao.NodeDao;
import ciknow.dao.SurveyDao;
import ciknow.domain.Node;
import ciknow.domain.Survey;

import dbunit.Dbunit;
import dbunit.DbunitTestExecutionListener;

@TestExecutionListeners(DbunitTestExecutionListener.class)
//@org.springframework.test.context.transaction.TransactionConfiguration(transactionManager="transactionManager", defaultRollback=false)
public class SurveyHibernateDaoTest extends AbstractHibernateDaoTest {
	
	@Autowired private SurveyDao surveyDao;
	@Autowired private NodeDao nodeDao;
	
	private static final Logger logger = Logger
			.getLogger(SurveyHibernateDaoTest.class.getName());
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/surveys.xml")
	public void save(){
		
		Assert.assertEquals(3, surveyDao.getAll().size());
		
		Node node1 = nodeDao.findById(1L);
		
		//create and save new survey
		Survey survey = new Survey();
		survey.setDescription("This is a different survey");
		survey.setDesigner(node1);
		survey.setName("A New Survey");
		survey.setVersion(1L);
		surveyDao.save(survey);
		Assert.assertEquals(4, surveyDao.getAll().size());
		
		List<Survey> list = surveyDao.getAll();
		Survey surveyFromDb = list.get(list.size()-1);
		compareSurveys(survey, surveyFromDb);
		
		//update and save existing survey
		Survey survey3 = surveyDao.findById(1L);
		survey3.setName("Change name");
		survey3.setAttribute("new_key", "new_value");
		surveyDao.save(survey3);
		compareSurveys(survey3, surveyDao.findById(1L));
		
		
	}
	
	
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/surveys.xml")
	public void saveCollection(){
		
		Assert.assertEquals(3, surveyDao.getAll().size());
		
		Node node1 = nodeDao.findById(1L);
		Node node2 = nodeDao.findById(2L);
		
		Survey survey = new Survey();
		survey.setDescription("This is a different survey");
		survey.setDesigner(node1);
		survey.setName("A New Survey");
		survey.setVersion(1L);
		
		Survey survey2 = new Survey();
		survey2.setDescription("This is a different survey again");
		survey2.setDesigner(node2);
		survey2.setName("A Newer Survey");
		survey2.setVersion(1L);
		
		ArrayList<Survey> list = new ArrayList<Survey>();
		list.add(survey);
		list.add(survey2);
		surveyDao.save(list);
		
		Assert.assertEquals(5, surveyDao.getAll().size());
		List<Survey> list2 = surveyDao.getAll();
		compareSurveys(survey, list2.get(list2.size()-2));
		compareSurveys(survey2, list2.get(list2.size()-1));
		
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/surveys.xml")
	public void delete(){
		
		List<Survey> list = surveyDao.getAll();
		Assert.assertEquals(3, list.size());
		
		Survey survey = list.get(0);
		surveyDao.delete(survey);
		
		Assert.assertEquals(2, surveyDao.getAll().size());
	
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/surveys.xml")
	public void deleteCollection(){
		
		List<Survey> list = surveyDao.getAll();
		Assert.assertEquals(3, list.size());
		
		surveyDao.delete(list);
		
		Assert.assertEquals(0, surveyDao.getAll().size());
		
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/surveys.xml")
	public void deleteAll(){
		
		//TODO Address BulkUpdate failure
		//Assert.assertEquals(3, surveyDao.getAll().size());	
		//surveyDao.deleteAll();
		//Assert.assertEquals(0, surveyDao.getAll().size());
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/surveys.xml")
	public void getAll(){
		
		List<Survey> list = surveyDao.getAll();
		Assert.assertEquals(3, list.size());
		
		Survey survey = surveyDao.findById(1L);
		Survey survey2 = surveyDao.findById(2L);
		compareSurveys(survey, list.get(0));
		compareSurveys(survey2, list.get(1));
		
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/surveys.xml")
	public void getCountTestMethod(){
		
		//List<Survey> list = surveyDao.getAll();
		Assert.assertEquals(3, surveyDao.getCount());
	}
	
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/surveys.xml")
	public void findById(){
		List<Survey> list = surveyDao.getAll();
		Assert.assertEquals(3, list.size());
		
		Survey survey = surveyDao.findById(1L);
		Assert.assertEquals(new Long(1L), survey.getId());
		Assert.assertEquals("This is a test survey", survey.getDescription());
	
		compareSurveys(survey, list.get(0));
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/surveys.xml")
	public void findByDesigner(){
		
		Node node2 = nodeDao.findById(2L);
		List<Survey> surveys = surveyDao.findByDesigner(node2);
		
		Assert.assertEquals(2, surveys.size());
		
		Survey survey1 = surveys.get(0);
		Assert.assertEquals(new Long(2L), survey1.getId());
		Assert.assertEquals("This is another test survey", survey1.getDescription());
		
		Survey survey2 = surveys.get(1);
		Assert.assertEquals(new Long(3L), survey2.getId());
		Assert.assertEquals("This is survey number 3", survey2.getDescription());
		
		List<Survey> list = surveyDao.getAll();
		compareSurveys(survey1, list.get(1));
		compareSurveys(survey2, list.get(2));
	
		
		
	}
	
	
	
	private void compareSurveys(Survey expected, Survey actual){
		Assert.assertEquals(expected.getDescription(), actual.getDescription());
		Assert.assertEquals(expected.getName(), actual.getName());	
		Assert.assertEquals(expected.getDesigner(), actual.getDesigner());
		Assert.assertEquals(expected.getId(), actual.getId());
		Assert.assertEquals(expected.getTimestamp(), actual.getTimestamp());
		//Assert.assertTrue(expected.getQuestions().containsAll(actual.getQuestions()));
		Iterator<String> keys = expected.getAttributes().keySet().iterator();
		while(keys.hasNext()){
			String key = keys.next();
			Assert.assertEquals(expected.getAttribute(key), actual.getAttribute(key));
		}		
		keys = expected.getLongAttributes().keySet().iterator();
		while(keys.hasNext()){
			String key = keys.next();
			Assert.assertEquals(expected.getLongAttribute(key), actual.getLongAttribute(key));
		}
		
	}
	

}
