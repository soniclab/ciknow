package ciknow.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ciknow.dao.NodeDao;
import ciknow.dao.QuestionDao;
import ciknow.domain.ContactField;
import ciknow.domain.Field;
import ciknow.domain.Node;
import ciknow.domain.Question;
import ciknow.domain.Scale;
import ciknow.domain.TextField;
import ciknow.util.Beans;
import ciknow.util.Constants;

public class NodeDataReader {

	private static Log logger = LogFactory.getLog(NodeDataReader.class);
	private NodeDao nodeDao;
	private QuestionDao questionDao;
	
	public static void main(String[] args) throws Exception{
		String filename = "build/nodeData.txt";
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		Beans.init();
		NodeDataReader ndr = (NodeDataReader)Beans.getBean("nodeDataReader");
		ndr.read(reader);
	}
	
	public NodeDao getNodeDao() {
		return nodeDao;
	}
	public void setNodeDao(NodeDao nodeDao) {
		this.nodeDao = nodeDao;
	}
	public QuestionDao getQuestionDao() {
		return questionDao;
	}
	public void setQuestionDao(QuestionDao questionDao) {
		this.questionDao = questionDao;
	}	
	
	public void read(BufferedReader reader) throws Exception{
		logger.info("Import Node Data");
		
		logger.debug("Get all available questions and corresponding attributes set");
		Map<Question, List<String>> qMap = new LinkedHashMap<Question, List<String>>();
		Map<String, Question> shortNameQuestionMap = new HashMap<String, Question>();
		List<Question> questions = questionDao.getAll();
		List<String> allKeys = new ArrayList<String>();
		allKeys.add("Username");
		allKeys.add("Label");
		allKeys.add("FirstName");
		allKeys.add("LastName");
		allKeys.add("MidName");
		allKeys.add("Type");
		allKeys.addAll(Constants.PREDEFINED_CONTACT_FIELDS);
		for (Question question : questions){
			shortNameQuestionMap.put(question.getShortName(), question);
			
			List<String> keys = new ArrayList<String>();			
			if (question.isChoice() 
				|| question.isRating() 
				|| question.isDuration()
				|| question.isContinuous()
				|| question.isTextLong()){
				for (Field field : question.getFields()){
					keys.add(question.makeFieldKey(field));
				}
			} else if (question.isMultipleChoice()
				|| question.isMultipleRating()){
				for (TextField tf : question.getTextFields()){
					for (Field field : question.getFields()){
						keys.add(question.makeFieldsKey(field, tf));
					}
				}
			} else if (question.isText()){
				for (TextField tf : question.getTextFields()){
					keys.add(question.makeTextFieldKey(tf));
				}
			} else if (question.isContactInfo()){
				for (ContactField cf : question.getContactFields()){
					if (Constants.PREDEFINED_CONTACT_FIELDS.contains(cf.getName())){
						keys.add(cf.getName());
					} else {
						keys.add(question.makeContactFieldKey(cf));
					}
				}
			} else {
				logger.info("Unsupported question type:" + question.getType());
			}
			
			allKeys.addAll(keys);
			if (!keys.isEmpty()) qMap.put(question, keys);			
		}

		logger.debug("Inspect header...");
		StringBuffer sb = new StringBuffer();
		String line = reader.readLine();
		String[] columns = line.split("\t", -1);
		for (String column : columns){
			if (!allKeys.contains(column)){
				sb.append("Invalid column: " + column).append("\n");
			}
		}
		String errMsg = sb.toString();
		if (!errMsg.isEmpty()) throw new Exception(errMsg);
		
		logger.debug("Read node line by line...");
		line = reader.readLine();
		int count = 0;
		List<Node> nodes = new ArrayList<Node>();
		while (line != null){
			String[] parts = line.split("\t", -1);
			if (parts.length != columns.length) throw new Exception("Invalid line (columns mismatch): " + line);
			
			String username = parts[0];
			Node node = nodeDao.loadByUsername(username);
			if (node == null) throw new Exception("Node (username=" + username + ") does not exist.");
			
			for (int i=1; i<parts.length; i++){
				String key = columns[i];
				String value = parts[i];
				
				if (key.contains(Constants.SEPERATOR)){
					String shortName = Question.getShortNameFromKey(key);
					Question question = shortNameQuestionMap.get(shortName);
					if (question.isChoice() || question.isMultipleChoice() || question.isDuration()){
						if (value.equals("0")) node.getAttributes().remove(key);
						else node.setAttribute(key, value);
					} else if (question.isContinuous()){
						if (value.equals("-")) node.getAttributes().remove(key);
						else node.setAttribute(key, value);
					} else if (question.isText()){
						if (value.equals("-1")) node.getAttributes().remove(key);
						else node.setAttribute(key, value);
					} else if (question.isTextLong()){
						if (value.equals("-1")) node.getLongAttributes().remove(key);
						else node.setLongAttribute(key, value);
					} else if (question.isRating() || question.isMultipleRating()){
						if (value.equals("-1")) node.getAttributes().remove(key);
						else {
							Scale scale = question.getScaleByValue(Double.parseDouble(value));
							if (scale == null) throw new Exception("Cannot find scale (value=" + value + ") in question (shortName=" + shortName + ")");
							String v = question.makeScaleKey(scale);
							node.setAttribute(key, v);
						}
					} else if (question.isContactInfo()) {
						node.setAttribute(key, value);
					} else {
						logger.warn("Unsupported question type:" + question.getType());
					}
				} else {
					if (key.equals("Label")) node.setLabel(value);
					else if (key.equals("FirstName")) node.setFirstName(value);
					else if (key.equals("LastName")) node.setLastName(value);
					else if (key.equals("MidName")) node.setMidName(value);
					else if (key.equals(Constants.CONTACT_FIELD_ADDR1)) node.setAddr1(value);
					else if (key.equals(Constants.CONTACT_FIELD_ADDR2)) node.setAddr2(value);
					else if (key.equals(Constants.CONTACT_FIELD_CITY)) node.setCity(value);
					else if (key.equals(Constants.CONTACT_FIELD_STATE)) node.setState(value);
					else if (key.equals(Constants.CONTACT_FIELD_COUNTRY)) node.setCountry(value);
					else if (key.equals(Constants.CONTACT_FIELD_ZIP)) node.setZipcode(value);
					else if (key.equals(Constants.CONTACT_FIELD_PHONE)) node.setPhone(value);
					else if (key.equals(Constants.CONTACT_FIELD_CELL)) node.setCell(value);
					else if (key.equals(Constants.CONTACT_FIELD_FAX)) node.setFax(value);
					else if (key.equals(Constants.CONTACT_FIELD_EMAIL)) node.setEmail(value);
					else if (key.equals(Constants.CONTACT_FIELD_URL)) node.setUri(value);
					else if (key.equals(Constants.CONTACT_FIELD_DEPARTMENT)) node.setDepartment(value);
					else if (key.equals(Constants.CONTACT_FIELD_ORGANIZATION)) node.setOrganization(value);
					else if (key.equals(Constants.CONTACT_FIELD_UNIT)) node.setUnit(value);
					else if (key.equals("Type")) {
						// Node Type is immutable
					}
					else {
						logger.warn("Unsupported column:" + key);
					}
				}
			}
			
			// update nodes in batch
			count++;
			nodes.add(node);
			if (count >= 5000){
				nodeDao.save(nodes);
				logger.debug(nodes.size() + " nodes updated.");
				count = 0;
				nodes = new ArrayList<Node>();
			}
			
			line = reader.readLine();
		}
		
		// update last batch nodes
		nodeDao.save(nodes);
		logger.debug(nodes.size() + " nodes updated.");
		
		logger.info("Import finished.");
	}
}
