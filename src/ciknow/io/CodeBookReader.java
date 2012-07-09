package ciknow.io;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import ciknow.dao.GroupDao;
import ciknow.dao.SurveyDao;
import ciknow.domain.ContactField;
import ciknow.domain.Field;
import ciknow.domain.Group;
import ciknow.domain.Page;
import ciknow.domain.Question;
import ciknow.domain.Scale;
import ciknow.domain.Survey;
import ciknow.domain.TextField;
import ciknow.util.Beans;
import ciknow.util.SurveyUtil;

/**
 * 
 * @author gyao
 * Notice:
 * 1, Group "ALL" and "USER" cannot be modified. Additional groups can be added.
 * 2, Code book can only be imported into empty survey.
 */

public class CodeBookReader {
	private static Log logger = LogFactory.getLog(CodeBookReader.class);
	private SurveyDao surveyDao;
	private GroupDao groupDao;
	
	
	public static void main(String[] args) throws Exception{
		Beans.init();
		CodeBookReader reader = (CodeBookReader)Beans.getBean("codeBookReader");
		reader.read(new FileInputStream("web/codebook.xml"), false);
	}


	@SuppressWarnings("rawtypes")
	public void read(InputStream is, boolean overwrite) throws Exception{
		logger.info("reading code book...");
		SAXReader reader = new SAXReader();
		Document doc = reader.read(new InputStreamReader(is, "UTF-8"));
		
		logger.debug("get root");
		Element root = doc.getRootElement();
		
		logger.info("update/create groups");
		List<Group> groups = new ArrayList<Group>();
		Map<String, Group> groupMap = new HashMap<String, Group>();
		for (Group g : groupDao.getAll()){
			groupMap.put(g.getName(), g);
		}
		for (Iterator groupItr = root.element("groups").elementIterator(); groupItr.hasNext(); ){
			Element groupElement = (Element) groupItr.next();
			String groupName = groupElement.attributeValue("name");
			Group group = groupMap.get(groupName);
			if (group == null) {
				group = new Group();
				group.setName(groupName);
				groups.add(group);
				groupMap.put(groupName, group);
			}			
		}
		
		logger.info("update survey");
		Element surveyElement = root.element("survey");
		Survey survey = surveyDao.findById(1L); // by default there is only one survey per ciknow instance
		if (!survey.getPages().isEmpty()){
			throw new Exception("Current survey is not empty!");
		}
		survey.setName(surveyElement.attributeValue("name"));
		survey.setDescription(surveyElement.elementText("description"));		
		survey.setTimestamp(new Date());
		// attributes
		if (surveyElement.element("attributes") != null){
			for (Iterator attrItr = surveyElement.element("attributes").elementIterator(); attrItr.hasNext(); ){
				Element attributeElement = (Element) attrItr.next();
				survey.setAttribute(attributeElement.attributeValue("key"), attributeElement.attributeValue("value"));
			}
		}
		// long attributes
		if (surveyElement.element("longAttributes") != null){
			for (Iterator attrItr = surveyElement.element("longAttributes").elementIterator(); attrItr.hasNext(); ){
				Element attributeElement = (Element) attrItr.next();
				survey.setLongAttribute(attributeElement.attributeValue("key"), attributeElement.attributeValue("value"));
			}
		}
		
		for (Iterator pageItr = surveyElement.element("pages").elementIterator(); pageItr.hasNext(); ){
			Element pageElement = (Element) pageItr.next();
			
			String name = pageElement.attributeValue("name");
			Page page = new Page();
			page.setName(name);
			page.setSurvey(survey);
			survey.getPages().add(page);
			String label = pageElement.attributeValue("label");
			page.setLabel(label);
			String instruction = pageElement.elementText("instruction");
			page.setInstruction(instruction);
			
			if (pageElement.element("attributes") != null){
				for (Iterator attrItr = pageElement.element("attributes").elementIterator(); attrItr.hasNext(); ){
					Element attributeElement = (Element) attrItr.next();
					page.getAttributes().put(attributeElement.attributeValue("key"), attributeElement.attributeValue("value"));
				}
			}
			
			for (Iterator itr = pageElement.element("questions").elementIterator(); itr.hasNext(); ){
				Element questionElement = (Element) itr.next();
				
				Question q = new Question();		
				q.setPage(page);
				page.getQuestions().add(q);
				q.setShortName(questionElement.attributeValue("shortName"));					
				q.setLabel(questionElement.attributeValue("label"));
				q.setType(questionElement.attributeValue("type"));
				
				if (questionElement.attribute("rowPerPage") != null) {
					Integer rowPerPage = Integer.parseInt(questionElement.attributeValue("rowPerPage"));			
					q.setRowPerPage(rowPerPage);	
				}
				q.setHtmlInstruction(questionElement.elementText("htmlInstruction"));
				
				// attributes
				if (questionElement.element("attributes") != null){
					for (Iterator attrItr = questionElement.element("attributes").elementIterator(); attrItr.hasNext(); ){
						Element attributeElement = (Element) attrItr.next();
						q.setAttribute(attributeElement.attributeValue("key"), attributeElement.attributeValue("value"));
					}
				}
				
				// long attributes
				if (questionElement.element("longAttributes") != null){
					for (Iterator attrItr = questionElement.element("longAttributes").elementIterator(); attrItr.hasNext(); ){
						Element attributeElement = (Element) attrItr.next();
						q.setLongAttribute(attributeElement.attributeValue("key"), attributeElement.attributeValue("value"));
					}
				}
				
				// fields
				Element fieldsElement = questionElement.element("fields");
				if (fieldsElement != null){
					for (Iterator fieldItr = fieldsElement.elementIterator(); fieldItr.hasNext(); ){
						Element fieldElement = (Element) fieldItr.next();
						Field field = new Field();
						field.setName(fieldElement.attributeValue("name"));
						field.setLabel(fieldElement.attributeValue("label"));
						q.getFields().add(field);
						field.setQuestion(q);
					}
				}
				
				// scales
				Element scalesElement = questionElement.element("scales");
				if (scalesElement != null){
					for (Iterator scaleItr = scalesElement.elementIterator(); scaleItr.hasNext(); ){
						Element scaleElement = (Element) scaleItr.next();
						Scale scale = new Scale();
						scale.setName(scaleElement.attributeValue("name"));
						scale.setLabel(scaleElement.attributeValue("label"));
						scale.setValue(Double.parseDouble(scaleElement.attributeValue("value")));
						q.getScales().add(scale);
						scale.setQuestion(q);
					}
				}
				
				// text fields
				Element textFieldsElement = questionElement.element("textFields");
				if (textFieldsElement != null){
					for (Iterator tfItr = textFieldsElement.elementIterator(); tfItr.hasNext(); ){
						Element textFieldElement = (Element) tfItr.next();
						TextField tf = new TextField();
						tf.setName(textFieldElement.attributeValue("name"));
						tf.setLabel(textFieldElement.attributeValue("label"));
						tf.setLarge(new Boolean(textFieldElement.attributeValue("large")));
						q.getTextFields().add(tf);
						tf.setQuestion(q);
					}
				}
				
				// contact fields
				Element contactFieldsElement = questionElement.element("contactFields");
				if (contactFieldsElement != null){
					for (Iterator cfItr = contactFieldsElement.elementIterator(); cfItr.hasNext(); ){
						Element cfElement = (Element) cfItr.next();
						ContactField cf = new ContactField();
						cf.setName(cfElement.attributeValue("name"));
						cf.setLabel(cfElement.attributeValue("label"));
						q.getContactFields().add(cf);
						cf.setQuestion(q);
					}
				}
				
				// available groups
				Element rowGroupsElement = questionElement.element("rowGroups");				
				if (rowGroupsElement != null){
					for (Iterator groupItr = rowGroupsElement.elementIterator(); groupItr.hasNext(); ){
						Element groupElement = (Element) groupItr.next();
						Group group = groupMap.get(groupElement.attributeValue("name"));
						q.getAvailableGroups().add(group);
					}	
				}
				
				// available groups2
				Element columnGroupsElement = questionElement.element("columnGroups");
				if (columnGroupsElement != null){
				for (Iterator groupItr = columnGroupsElement.elementIterator(); groupItr.hasNext(); ){
					Element groupElement = (Element) groupItr.next();
					Group group = groupMap.get(groupElement.attributeValue("name"));
					q.getAvailableGroups2().add(group);
				}	
				}
				
				// visible groups
				Element respondentGroupsElement = questionElement.element("respondentGroups");
				if (respondentGroupsElement != null){
					for (Iterator groupItr = respondentGroupsElement.elementIterator(); groupItr.hasNext(); ){
						Element groupElement = (Element) groupItr.next();
						Group group = groupMap.get(groupElement.attributeValue("name"));
						q.getVisibleGroups().add(group);
					}
				}
			}
		}
		
		groupDao.save(groups);	
		surveyDao.save(survey);
		
		SurveyUtil.setSurvey(survey);
		
		logger.info("codebook saved.");
	}
	

	
	public SurveyDao getSurveyDao() {
		return surveyDao;
	}


	public void setSurveyDao(SurveyDao surveyDao) {
		this.surveyDao = surveyDao;
	}

	public GroupDao getGroupDao() {
		return groupDao;
	}


	public void setGroupDao(GroupDao groupDao) {
		this.groupDao = groupDao;
	}
}
