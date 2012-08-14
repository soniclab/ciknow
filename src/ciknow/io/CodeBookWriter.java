package ciknow.io;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.ProcessingInstruction;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import ciknow.dao.GroupDao;
import ciknow.dao.SurveyDao;
import ciknow.domain.*;
import ciknow.ro.GenericRO;
import ciknow.util.Beans;

public class CodeBookWriter {
	private static Log logger = LogFactory.getLog(CodeBookWriter.class);

	private GenericRO genericRO;
	private SurveyDao surveyDao;
	private GroupDao groupDao;
	
	public static void main(String[] args) throws IOException{
		Beans.init();
		CodeBookWriter codebook = (CodeBookWriter) Beans.getBean("codeBookWriter");
		codebook.write(new FileOutputStream("web/codebook.xml"));
	}
	
	public CodeBookWriter(){
		
	}

	public void write(OutputStream os) throws IOException{
		logger.info("exporting codebook...");
		Document doc = createDocument();
		OutputFormat format = OutputFormat.createPrettyPrint();
		XMLWriter writer = new XMLWriter(new OutputStreamWriter(os, "UTF-8"), format);
		writer.write(doc);
		writer.close();
		logger.info("codebook exported.");
	}
	
	public void serializeToXML(Document doc) throws IOException{
		String realPath = genericRO.getRealPath();
		serializeToXML(doc, realPath + "codebook.xml");
	}
	
	public void serializeToXML(Document doc, String filename) throws IOException{
		logger.info("write codebook to " + filename);
		OutputStream out = new FileOutputStream(filename);
		serializeToXML(doc, out);
		out.close();
	}
	
	public void serializeToXML(Document doc, OutputStream out) throws IOException{
		OutputFormat format = OutputFormat.createPrettyPrint();
		XMLWriter writer = new XMLWriter(new OutputStreamWriter(out, "UTF-8"), format);
		writer.write(doc);
		writer.flush();
	}
	
	@SuppressWarnings("unchecked")
	public Document createDocument() {
		logger.info("creating document");
		
		Survey survey = surveyDao.findById(1L);
		List<Group> groups = groupDao.getAll();
		
		Document document = DocumentHelper.createDocument();
		
		Map<String, String> args = new HashMap<String, String>();
		args.put("type", "text/xsl");
		args.put("href", "codebook.xsl");
		ProcessingInstruction pi = DocumentHelper.createProcessingInstruction("xml-stylesheet", args);		
		document.content().add(0, pi);

		document.addComment("Group 'ALL' and 'USER' cannot be modified. Additional groups can be added.");
		document.addComment("Code book can only be imported into empty survey.");
		
		Element root = document.addElement("ciknow");

		// survey
		Element surveyElement = root.addElement("survey");
		surveyElement.addAttribute("name", survey.getName());
		
		Element surveyDescription = surveyElement.addElement("description");
		surveyDescription.addText(survey.getDescription());
				
		Map<String, String> attributes = survey.getAttributes();
		if (!attributes.isEmpty()){
			Element surveyAttributes = surveyElement.addElement("attributes");
			for (String key : attributes.keySet()){
				String value = attributes.get(key);
				surveyAttributes.addElement("attribute").addAttribute("key", key).addAttribute("value", value);
			}		
		}
		
		attributes = survey.getLongAttributes();
		if (!attributes.isEmpty()){
			Element surveyLongAttributes = surveyElement.addElement("longAttributes");		
			for (String key : attributes.keySet()){
				String value = attributes.get(key);
				surveyLongAttributes.addElement("attribute").addAttribute("key", key).addAttribute("value", value);
			}
		}
		
		// page
		List<Page> pages = survey.getPages();
		Element pagesElement = surveyElement.addElement("pages");		
		for (Page page : pages){
			Element pageElement = pagesElement.addElement("page");
			
			pageElement.addAttribute("name", page.getName());
			pageElement.addAttribute("label", page.getLabel());
			String pageInstruction = page.getInstruction();
			if (pageInstruction != null && !pageInstruction.isEmpty()){
				pageElement.addElement("instruction").addText(pageInstruction);
			}			
			
			Map<String, String> pageAttributes = page.getAttributes();
			if (!pageAttributes.isEmpty()){
				Element pageAttributesElement = pageElement.addElement("attributes");			
				for (String key : pageAttributes.keySet()){
					String value = pageAttributes.get(key);
					pageAttributesElement.addElement("attribute").addAttribute("key", key).addAttribute("value", value);
				}
			}
			
			// questions
			List<Question> questions = page.getQuestions();
			Element questionsElement = pageElement.addElement("questions");
			for (Question question : questions){
				Element questionElement = questionsElement.addElement("question");
				questionElement.addAttribute("shortName", question.getShortName());
				questionElement.addAttribute("label", question.getLabel());
				questionElement.addAttribute("type", question.getType());
				Integer rowPerPage = question.getRowPerPage();
				if (rowPerPage != null) questionElement.addAttribute("rowPerPage", rowPerPage.toString());
				
				Element htmlInstructionElement = questionElement.addElement("htmlInstruction");
				String htmlInstruction = question.getHtmlInstruction();
				if (htmlInstruction != null && htmlInstruction.length() > 0){
					htmlInstructionElement.addText(htmlInstruction);
				}			
				
				Map<String, String> questionAttributes = question.getAttributes();
				if (!questionAttributes.isEmpty()){
					Element attrs = questionElement.addElement("attributes");
					for (String key : questionAttributes.keySet()){
						attrs.addElement("attribute").addAttribute("key", key).addAttribute("value", question.getAttribute(key));				
					}
				}
				
				Map<String, String> questionLongAttributes = question.getLongAttributes();
				if (!questionLongAttributes.isEmpty()){
					Element attrs = questionElement.addElement("longAttributes");
					for (String key : questionLongAttributes.keySet()){
						attrs.addElement("attribute").addAttribute("key", key).addAttribute("value", question.getLongAttribute(key));				
					}
				}
						
				if (!question.getFields().isEmpty()){
					Element fields = questionElement.addElement("fields");
					for (Field field : question.getFields()){
						fields.addElement("field")
							.addAttribute("name", field.getName())
							.addAttribute("label", field.getLabel());
					}
				}
				
				if (!question.getTextFields().isEmpty()){
					Element textFields = questionElement.addElement("textFields");
					for (TextField tf : question.getTextFields()){
						textFields.addElement("textField")
							.addAttribute("name", tf.getName())
							.addAttribute("label", tf.getLabel())
							.addAttribute("large", tf.getLarge().toString());
					}
				}
				
				if (!question.getScales().isEmpty()){
					Element scales = questionElement.addElement("scales");
					for (Scale scale : question.getScales()){
						scales.addElement("scale")
							.addAttribute("name", scale.getName())
							.addAttribute("label", scale.getLabel())
							.addAttribute("value", scale.getValue().toString());
					}			
				}
				
				if (!question.getContactFields().isEmpty()){
					Element contactFields = questionElement.addElement("contactFields");
					for (ContactField cf : question.getContactFields()){
						contactFields.addElement("contactField")
							.addAttribute("name", cf.getName())
							.addAttribute("label", cf.getLabel());
					}
				}
								
				if (!question.getVisibleGroups().isEmpty()){
					Element questionVisibleGroups = questionElement.addElement("respondentGroups");
					for (Group group : question.getVisibleGroups()){
						Long groupId = group.getId();
						Group g = getGroupById(groups, groupId);
						if (g == null){
							logger.warn("question: " + question.getShortName() + 
										", visible groupId: " + groupId + " doesn't exist.");
							continue;
						}
						questionVisibleGroups.addElement("group")
							.addAttribute("name", g.getName());
					}
				}
				
				if (!question.getAvailableGroups().isEmpty()){
					Element questionAvailableGroups = questionElement.addElement("rowGroups");
					for (Group group : question.getAvailableGroups()){
						Long groupId = group.getId();
						Group g = getGroupById(groups, groupId);
						if (g == null){
							logger.warn("question: " + question.getShortName() + 
										", available groupId: " + groupId + " doesn't exist.");
							continue;
						}
						questionAvailableGroups.addElement("group")
							.addAttribute("name", g.getName());
					}
				}
				
				if (!question.getAvailableGroups2().isEmpty()){
					Element questionAvailableGroups2 = questionElement.addElement("columnGroups");
					for (Group group : question.getAvailableGroups2()){
						Long groupId = group.getId();
						Group g = getGroupById(groups, groupId);
						if (g == null){
							logger.warn("question: " + question.getShortName() + 
										", available groupId: " + groupId + " doesn't exist.");
							continue;
						}
						questionAvailableGroups2.addElement("group")
							.addAttribute("name", g.getName());
					}
				}
			}
		}
		
		// all groups
		Element groupsElement = root.addElement("groups");
		for (Group group : groups){
			if (group.isPrivate() || group.isProvider()) continue;
			
			String pg = "0"; // default is non-private group
			if (group.isPrivate()) pg = "1";
			groupsElement.addElement("group")
				.addAttribute("name", group.getName())
				.addAttribute("private", pg);
		}
		
		
		logger.info("document created.");
		return document;
	}
	
	private Group getGroupById(List<Group> groups, Long id){
		for (Group group : groups){
			if (group.getId().equals(id)) return group;
		}
		return null;
	}

	
	public void replaceBrackets(String filename) throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(filename));		
		StringBuilder sb = new StringBuilder();
		String line = reader.readLine();
		while (line != null){
			line = line.replaceAll("&lt;", "<");
			line = line.replaceAll("&gt;", ">");
			sb.append(line).append("\n");
			line = reader.readLine();
		}
		reader.close();
		
		PrintWriter writer = new PrintWriter(filename);
		writer.print(sb.toString());
		writer.close();
	}
	
	
	public GenericRO getGenericRO() {
		return genericRO;
	}

	public void setGenericRO(GenericRO genericRO) {
		this.genericRO = genericRO;
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
