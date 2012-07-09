package ciknow.dao.hibernate;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestExecutionListeners;

import ciknow.dao.QuestionDao;
import ciknow.dao.SurveyDao;
import ciknow.domain.Question;
import ciknow.domain.Scale;
import ciknow.domain.Survey;
import ciknow.domain.Group;

import dbunit.Dbunit;
import dbunit.DbunitTestExecutionListener;

@TestExecutionListeners(DbunitTestExecutionListener.class)
//@org.springframework.test.context.transaction.TransactionConfiguration(transactionManager="transactionManager", defaultRollback=false)
public class QuestionHibernateDaoTest extends AbstractHibernateDaoTest {
	
	@Autowired private QuestionDao questionDao;
	@Autowired private SurveyDao surveyDao;
	
	private static final Logger logger = Logger
			.getLogger(QuestionHibernateDaoTest.class.getName());
	
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/questions.xml")
	public void persist(){
		
		Assert.assertEquals(3, questionDao.getCount());
		Survey survey = surveyDao.findById(1L);
		Set<Scale> scales = new HashSet<Scale>();
		scales.add(createScale(1, "Scale 1", "S 1", 1.5));
		scales.add(createScale(1, "Scale 2", "S 2", 2.5));
		HashMap<String, String> attributes = new HashMap<String, String>();
		attributes.put("1", "ATT_VALUE 1");
		attributes.put("2", "ATT_VALUE 2");
		HashMap<String, String> longAttributes = new HashMap<String, String>();
		longAttributes.put("1", "LONG_ATT_VALUE 1");
		longAttributes.put("2", "LONG_ATT_VALUE 2");
		Question question = createQuestion(1L, survey, 10, "Test Question", "TQ", 
				"Type 1", "Question instructions", "<p>Instructions</p>", 2, scales, attributes, longAttributes);
		
		questionDao.persist(question);
		Assert.assertEquals(4, questionDao.getCount());
		
		List<Question> list = questionDao.getAll();
		Question fromDbQuestion = list.get(list.size()-1);
		
		compareQuestions(question, fromDbQuestion);
		
	}
	
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/questions.xml")
	public void save(){
		
		Assert.assertEquals(3, questionDao.getCount());
		Survey survey = surveyDao.findById(1L);
		Set<Scale> scales = new HashSet<Scale>();
		scales.add(createScale(1, "Scale 1", "S 1", 1.5));
		scales.add(createScale(1, "Scale 2", "S 2", 2.5));
		HashMap<String, String> attributes = new HashMap<String, String>();
		attributes.put("1", "ATT_VALUE 1");
		attributes.put("2", "ATT_VALUE 2");
		HashMap<String, String> longAttributes = new HashMap<String, String>();
		longAttributes.put("1", "LONG_ATT_VALUE 1");
		longAttributes.put("2", "LONG_ATT_VALUE 2");
		Question question = createQuestion(1L, survey, 10, "Test Question", "TQ", 
				"Type 1", "Question instructions", "<p>Instructions</p>", 2, scales, attributes, longAttributes);
		
		questionDao.save(question);
		Assert.assertEquals(4, questionDao.getCount());
		
		List<Question> list = questionDao.getAll();
		Question fromDbQuestion = list.get(list.size()-1);
		
		compareQuestions(question, fromDbQuestion);
		
		
		fromDbQuestion.setAttribute("1", "Different Value");
		fromDbQuestion.setLongAttribute("1", "Different Long Value");
		fromDbQuestion.setLabel("Different Label");
		questionDao.save(fromDbQuestion);
		fromDbQuestion = questionDao.findById(fromDbQuestion.getId());
		Assert.assertEquals("Different Value", fromDbQuestion.getAttribute("1"));
		Assert.assertEquals("Different Long Value", fromDbQuestion.getLongAttribute("1"));
		Assert.assertEquals("Different Label", fromDbQuestion.getLabel());
		
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/questions.xml")
	public void saveCollection(){
		
		Assert.assertEquals(3, questionDao.getCount());
		Survey survey = surveyDao.findById(1L);
		Set<Scale> scales = new HashSet<Scale>();
		scales.add(createScale(1, "Scale 1", "S 1", 1.5));
		scales.add(createScale(1, "Scale 2", "S 2", 2.5));
		HashMap<String, String> attributes = new HashMap<String, String>();
		attributes.put("1", "ATT_VALUE 1");
		attributes.put("2", "ATT_VALUE 2");
		HashMap<String, String> longAttributes = new HashMap<String, String>();
		longAttributes.put("1", "LONG_ATT_VALUE 1");
		longAttributes.put("2", "LONG_ATT_VALUE 2");
		Question question = createQuestion(1L, survey, 10, "Test Question", "TQ", 
				"Type 1", "Question instructions", "<p>Instructions</p>", 2, scales, attributes, longAttributes);
		
		Assert.assertEquals(3, questionDao.getCount());
		Survey survey2 = surveyDao.findById(1L);
		Set<Scale> scales2 = new HashSet<Scale>();
		scales.add(createScale(1, "Scale 1", "S 1", 1.5));
		scales.add(createScale(1, "Scale 2", "S 2", 2.5));
		HashMap<String, String> attributes2 = new HashMap<String, String>();
		attributes.put("1", "ATT_VALUE 1");
		attributes.put("2", "ATT_VALUE 2");
		HashMap<String, String> longAttributes2 = new HashMap<String, String>();
		longAttributes.put("1", "LONG_ATT_VALUE 1");
		longAttributes.put("2", "LONG_ATT_VALUE 2");
		Question question2 = createQuestion(1L, survey2, 10, "Test Question", "TQ", 
				"Type 1", "Question instructions", "<p>Instructions</p>", 2, scales2, attributes2, longAttributes2);
		
		ArrayList<Question> list = new ArrayList<Question>();
		list.add(question);
		list.add(question2);
		
		questionDao.save(list);
		
		Assert.assertEquals(5, questionDao.getCount());
		
		List<Question> list2 = questionDao.getAll();
		compareQuestions(question, list2.get(list2.size()-2));
		compareQuestions(question2, list2.get(list2.size()-1));
		
		
	}
	

	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/questions.xml")
	public void deleteTestFunction(){
		
		Assert.assertEquals(3, questionDao.getCount());
		Question question = questionDao.findById(1L);
		questionDao.delete(question);
		Assert.assertEquals(2, questionDao.getCount());
		Assert.assertTrue(!questionDao.getAll().contains(question));
		
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/questions.xml")
	public void deleteCollection(){
		
		Assert.assertEquals(3, questionDao.getCount());
		Question question = questionDao.findById(1L);
		Question question2 = questionDao.findById(2L);
		ArrayList<Question> list = new ArrayList<Question>();
		list.add(question);
		list.add(question2);
		questionDao.delete(list);
		Assert.assertEquals(1, questionDao.getCount());
		Assert.assertTrue(!questionDao.getAll().contains(question));
		Assert.assertTrue(!questionDao.getAll().contains(question2));
		
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/questions.xml")
	public void deleteAll(){
		
		//TODO Address inefficient SQL delete - related to BulkUpdate-Cascading issues
		Assert.assertEquals(3, questionDao.getCount());
		questionDao.deleteAll();
		Assert.assertEquals(0, questionDao.getCount());
		
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/questions.xml")
	public void findById(){
		
		checkQuestion_1_Params(questionDao.findById(1L));
	
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/questions.xml")
	public void findByShortName(){
		checkQuestion_1_Params(questionDao.findByShortName("Q_1"));
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/questions.xml")
	public void getAll(){
		
		List<Question> list = questionDao.getAll();
		Assert.assertEquals(3, list.size());
		
		Assert.assertTrue(list.contains(questionDao.findById(1L)));
		Assert.assertTrue(list.contains(questionDao.findById(2L)));
		Assert.assertTrue(list.contains(questionDao.findById(3L)));
		
		
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/questions.xml")
	public void getCount(){
		
		Assert.assertEquals(3, questionDao.getCount());
		
	}
	
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/questions.xml")
	public void deleteAttributeByValue(){
		
		Question question = questionDao.findById(1L);
		Assert.assertEquals(2, question.getAttributes().size());
		questionDao.deleteAttributeByValue("QUESTION_1_ATT_VALUE_1");
		
		//Currently unable to verify results due to hibernate caching issues
		//Need to build in some SQL fetching code
		
		/*List<Question> list = questionDao.getAll();
		question = list.get(0);
		System.out.println("question.attribute.size: " + question.getAttributes().size());
		
		Iterator<String> keys = question.getAttributes().keySet().iterator();
		while(keys.hasNext()){
			String key = (String)keys.next();
			//Assert.assertEquals(question.getAttribute(key), actual.getAttribute(key));
			System.out.println("key, value: " + key + ", " + question.getAttribute(key));
		}
		*/
		
		//Assert.assertEquals(1, question.getAttributes().size());
		
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/questions.xml")
	public void deleteLongAttributeByValue(){
		
		Question question = questionDao.findById(1L);
		Assert.assertEquals(2, question.getLongAttributes().size());
		questionDao.deleteAttributeByValue("LONG_QUESTION_1_ATT_VALUE_1");
		
		//Currently unable to verify results due to hibernate caching issues
		//Need to build in some SQL fetching code
		
	}
	
    private void compareQuestions(Question expected, Question actual){
		
		
		Assert.assertEquals(expected.getVersion(), actual.getVersion());
		Assert.assertEquals(expected.getLabel(), actual.getLabel());
		Assert.assertEquals(expected.getShortName(), actual.getShortName());
		Assert.assertEquals(expected.getType(), actual.getType());
		Assert.assertEquals(expected.getInstruction(), actual.getInstruction());
		Assert.assertEquals(expected.getHtmlInstruction(), actual.getHtmlInstruction());
		Assert.assertEquals(expected.getRowPerPage(), actual.getRowPerPage());
		Assert.assertTrue(expected.getScales().containsAll(actual.getScales()));
		
		Iterator<String> keys = expected.getAttributes().keySet().iterator();
		while(keys.hasNext()){
			String key = (String)keys.next();
			Assert.assertEquals(expected.getAttribute(key), actual.getAttribute(key));
		}
		
		keys = expected.getLongAttributes().keySet().iterator();
		while(keys.hasNext()){
			String key = (String)keys.next();
			Assert.assertEquals(expected.getLongAttribute(key), actual.getLongAttribute(key));
		}
		
		//ADD MORE COMPARSIONS - i.e fields, available, visible ....
		
		
	}
	
	
	private Scale createScale(Integer seqNumber, String name, String label, Double value){
		Scale scale = new Scale();
		scale.setName(name);
		scale.setLabel(label);
		scale.setValue(value);
		
		return scale;
	}
	
	
	private Question createQuestion(Long version, Survey survey, int sequence, String label, String shortName, 
				String type, String instruction, String htmlInstruction, int rowPerPage, 
						Set<Scale> scales, HashMap<String, String> attributes, HashMap<String, String> longAttributes){
		
		Question question = new Question();
		question.setVersion(version);
		question.setLabel(label);
		question.setShortName(shortName);
		question.setType(type);
		question.setInstruction(instruction);
		question.setHtmlInstruction(htmlInstruction);
		question.setRowPerPage(rowPerPage);
		question.setAttributes(attributes);
		question.setLongAttributes(longAttributes);
		
	
		return question;
		
	}
	
	
	private void checkQuestion_1_Params(Question question){
		
		Assert.assertEquals(new Long(1), question.getId());
		Assert.assertEquals("Question 1", question.getLabel());
		Assert.assertEquals(2, question.getTextFields().size());
		Assert.assertEquals("Q1 Text field 1", question.getTextFieldByName("Question_1_Text_field_1").getLabel());
		Assert.assertEquals(2, question.getFields().size());
		Assert.assertEquals("Q1 Field 1", question.getFieldByName("Question_1_Field_1").getLabel());
		Assert.assertEquals(2, question.getContactFields().size());
		Assert.assertEquals("Q1 Contact Field 1", question.getContactFieldByName("Question_1_Contact_Field_1").getLabel());
		Assert.assertEquals(2, question.getScales().size());
		Scale scale = question.getScales().iterator().next();
		Assert.assertEquals("Scale 1", scale.getLabel());
		Assert.assertEquals(2, question.getAttributes().size());
		Assert.assertEquals("QUESTION_1_ATT_VALUE_1", question.getAttribute("1"));
		Assert.assertEquals(2, question.getLongAttributes().size());
		Assert.assertEquals("LONG_QUESTION_1_ATT_VALUE_1", question.getLongAttribute("1"));
		Assert.assertEquals(2, question.getAvailableGroups().size());
		Group group = question.getAvailableGroups().iterator().next();
		Assert.assertEquals(new Long(1), group.getId());
		Assert.assertEquals(2, question.getAvailableGroups2().size());
		group = question.getAvailableGroups2().iterator().next();
		Assert.assertEquals(new Long(1), group.getId());
		Assert.assertEquals(2, question.getVisibleGroups().size());
		group = question.getVisibleGroups().iterator().next();
		Assert.assertEquals(new Long(1), group.getId());
	}

}
