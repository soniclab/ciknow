package ciknow.io;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ciknow.dao.GroupDao;
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

public class NodeDataWriter {

	private static Log logger = LogFactory.getLog(NodeDataWriter.class);
	private NodeDao nodeDao;
	private QuestionDao questionDao;
	private GroupDao groupDao;
	
	public static void main(String[] args) throws IOException{
		Beans.init();
		NodeDataWriter ndw = (NodeDataWriter)Beans.getBean("nodeDataWriter");
		QuestionDao questionDao = (QuestionDao)Beans.getBean("questionDao");
		
		String nodeGroupId = null;
		String[] questionIds;
		if (args.length <= 1) {
			logger.warn("You did not provide node group Id and attribute question IDs. By default, node group USER will be exported with all attribute questions data.");
			nodeGroupId = "2";
			List<Question> questions = questionDao.getAll();
			List<Question> attributeQuestions = new ArrayList<Question>();
			for (Question question : questions){
				if (question.isAttribute()) attributeQuestions.add(question);
			}
			questionIds = new String[attributeQuestions.size()];
			int index = 0;
			for (Question question : attributeQuestions){
				questionIds[index] = question.getId().toString();
				index += 1;
			}
		} else {
			nodeGroupId = args[0];
			questionIds = new String[args.length-1];
			for (int i = 1; i < args.length; i++) {
				questionIds[i-1] = args[i];
			}
		}

		String filename = "nodeData.txt";
		File file = new File(filename);
		PrintWriter writer = new PrintWriter(file);
		Map<String, String> options = new HashMap<String, String>();
		options.put("prettyFormat", "0");
		ndw.write(writer, nodeGroupId, questionIds, options);
		writer.flush();
		writer.close();
		System.out.println("Results: " + file.getAbsolutePath());
		System.exit(0);
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
	public GroupDao getGroupDao() {
		return groupDao;
	}
	public void setGroupDao(GroupDao groupDao) {
		this.groupDao = groupDao;
	}
	
	public void write(Writer writer, String groupId, String[] questionIds, Map<String, String> options) throws IOException{
		logger.info("Export Node Data");
		boolean prettyFormat = options.get("prettyFormat").equals("1");
		
		logger.debug("Determine property/attributes to export...");
		Map<Question, List<String>> qMap = new LinkedHashMap<Question, List<String>>();
		for (String qid : questionIds){
			Question question = questionDao.findById(Long.parseLong(qid));
			if (question == null){
				logger.warn("Cannot find question with id=" + qid);
				continue;
			}
			
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
				logger.warn("Unsupported question type:" + question.getType());
			}
			
			if (!keys.isEmpty()) qMap.put(question, keys);
		}
		
		logger.debug("Determine nodes to export");
		List<Long> nodeIds = groupDao.getNodeIdsByGroupId(Long.parseLong(groupId));
		
		logger.debug("Write header");
		if (prettyFormat){
			writer.append(getQuestionLine(qMap.keySet()));
			writer.append(getFieldLine(qMap.keySet()));
		} else {
			writer.append("Username");
			for (Question question : qMap.keySet()){
				List<String> keys = qMap.get(question);
				for (String key : keys){
					writer.append("\t").append(key);
				}
			}
			writer.append("\t").append("Label");
			writer.append("\t").append("FirstName");
			writer.append("\t").append("LastName");
			writer.append("\t").append("MidName");
			writer.append("\t").append("Type");
			writer.append("\n");
		}
		
		logger.debug("Export batch of nodes (5000)...");
		int nodeSize = nodeIds.size();
		int pageSize = 5000;		
		int totalPage = nodeSize/pageSize;
		if (nodeSize%pageSize > 0) totalPage += 1;
		int pageStartIndex = 0;		
		for (int page = 1; page <= totalPage; page++){
			logger.debug("page: " + page + ", totalPage: " + totalPage);
			StringBuilder sb = new StringBuilder();
			int start = pageStartIndex;
			int end = Math.min(pageSize*page, nodeSize);
			List<Long> nodeIdsByPage = nodeIds.subList(start, end);
			logger.debug("Exporting node " + start + " to " + end);
			List<Node> nodes = nodeDao.loadByIds(nodeIdsByPage);
			
			for (Node node : nodes){
				sb.append(node.getUsername());
				for (Question question : qMap.keySet()){
					List<String> keys = qMap.get(question);
					if (keys == null || keys.isEmpty()){
						logger.warn("There is no attribute for question: " + question.getShortName());
						continue;
					}
					
					String value = "";					
					if (question.isChoice() 
						|| question.isMultipleChoice()
						|| question.isDuration()){
						// default value is "0"
						for (String key : keys){
							value = node.getAttribute(key);
							if (value == null || value.trim().isEmpty()) value = "0";
							sb.append("\t").append(value);
						}
					} else if (question.isContinuous()){
						// default value is "-"
						for (String key : keys){
							value = node.getAttribute(key);
							if (value == null || value.trim().isEmpty()) value = "-";
							sb.append("\t").append(value);
						}
					} else if (question.isText()){
						// default value is "-1"
						for (String key : keys){
							value = node.getAttribute(key);
							if (value == null || value.trim().isEmpty()) value = "-1";
							
		                	// Don't allow new line or tab in long text, otherwise
		                	// the exported file will be corrupted (interfere with defined format)
		                	if (value.indexOf("\n") >= 0){
		                		logger.warn("new line in long text are removed.");
		                		value = value.replaceAll("\n", " ");
		                	}
		                	if (value.indexOf("\r") >= 0){
		                		logger.warn("carriage-return in long text are removed.");
		                		value = value.replaceAll("\r", " ");
		                	}
		                	if (value.indexOf("\f") >= 0){
		                		logger.warn("form feed in long text are removed.");
		                		value = value.replaceAll("\f", " ");
		                	}
		                	if (value.indexOf("\t") >= 0){
		                		logger.warn("tab in long text are removed.");
		                		value = value.replaceAll("\t", " ");
		                	}
		                	
							sb.append("\t").append(value);
						}
					} else if (question.isTextLong()){
						// get long attributes, default value is "-1"
						for (String key : keys){
							value = node.getLongAttribute(key);
							if (value == null || value.trim().isEmpty()) value = "-1";
							
		                	// Don't allow new line or tab in long text, otherwise
		                	// the exported file will be corrupted (interfere with defined format)
		                	if (value.indexOf("\n") >= 0){
		                		logger.warn("new line in long text are removed.");
		                		value = value.replaceAll("\n", " ");
		                	}
		                	if (value.indexOf("\r") >= 0){
		                		logger.warn("carriage-return in long text are removed.");
		                		value = value.replaceAll("\r", " ");
		                	}
		                	if (value.indexOf("\f") >= 0){
		                		logger.warn("form feed in long text are removed.");
		                		value = value.replaceAll("\f", " ");
		                	}
		                	if (value.indexOf("\t") >= 0){
		                		logger.warn("tab in long text are removed.");
		                		value = value.replaceAll("\t", " ");
		                	}
		                	
							sb.append("\t").append(value);
						}
					} else if (question.isRating() || question.isMultipleRating()){
						// default value is "-1"
						for (String key : keys){
							value = node.getAttribute(key);
							if (value == null || value.trim().isEmpty()) value = "-1";
							else {
								Scale scale = question.getScaleByName(Question.getScaleNameFromKey(value));
								value = (scale==null?"-1":scale.getValue().toString());
							}
							sb.append("\t").append(value);
						}
					} else if (question.isContactInfo()){
						// mixture of attributes and properties
						for (String key : keys){
							if (key.equals(Constants.CONTACT_FIELD_ADDR1)) value = node.getAddr1();
							else if (key.equals(Constants.CONTACT_FIELD_ADDR2)) value = node.getAddr2();
							else if (key.equals(Constants.CONTACT_FIELD_CITY)) value = node.getCity();
							else if (key.equals(Constants.CONTACT_FIELD_STATE)) value = node.getState();
							else if (key.equals(Constants.CONTACT_FIELD_COUNTRY)) value = node.getCountry();
							else if (key.equals(Constants.CONTACT_FIELD_ZIP)) value = node.getZipcode();
							else if (key.equals(Constants.CONTACT_FIELD_PHONE)) value = node.getPhone();
							else if (key.equals(Constants.CONTACT_FIELD_CELL)) value = node.getCell();
							else if (key.equals(Constants.CONTACT_FIELD_FAX)) value = node.getFax();
							else if (key.equals(Constants.CONTACT_FIELD_EMAIL)) value = node.getEmail();
							else if (key.equals(Constants.CONTACT_FIELD_URL)) value = node.getUri();
							else if (key.equals(Constants.CONTACT_FIELD_DEPARTMENT)) value = node.getDepartment();
							else if (key.equals(Constants.CONTACT_FIELD_ORGANIZATION)) value = node.getOrganization();
							else if (key.equals(Constants.CONTACT_FIELD_UNIT)) value = node.getUnit();
							else {
								value = node.getAttribute(key);
							}
							sb.append("\t").append(value==null?"":value);
						}
					} else {
						// ignored
					}
				}
				
				// extra
				sb.append("\t").append(node.getLabel());
				sb.append("\t").append(node.getFirstName());
				sb.append("\t").append(node.getLastName());
				sb.append("\t").append(node.getMidName());
				sb.append("\t").append(node.getType());
				
				// end of line
				sb.append("\n");
			}
			
			// write a batch of nodes to file
			writer.append(sb.toString());
			writer.flush();
			
			pageStartIndex += pageSize;
		}
		
		logger.info("Export finished.");
	}
	
	@SuppressWarnings("unused")
	private String getQuestionLine(Collection<Question> questions){
		StringBuilder sb = new StringBuilder();
		
		sb.append("Username");
		
		for (Question question : questions){
			if (question.isChoice() 
					|| question.isRating() 
					|| question.isDuration()
					|| question.isContinuous()
					|| question.isTextLong()){
					for (Field field : question.getFields()){
						sb.append("\t").append(question.getLabel());
					}
				} else if (question.isMultipleChoice()
					|| question.isMultipleRating()){
					for (TextField tf : question.getTextFields()){
						for (Field field : question.getFields()){
							sb.append("\t").append(question.getLabel());
						}
					}
				} else if (question.isText()){
					for (TextField tf : question.getTextFields()){
						sb.append("\t").append(question.getLabel());
					}
				} else if (question.isContactInfo()){
					for (ContactField cf : question.getContactFields()){
						sb.append("\t").append(question.getLabel());
					}
				} else {
					logger.warn("Unsupported question type:" + question.getType());
				}
		}
		
		sb.append("\t").append("Label");
		sb.append("\t").append("FirstName");
		sb.append("\t").append("LastName");
		sb.append("\t").append("MidName");
		sb.append("\t").append("Type");
		sb.append("\n");
		
		return sb.toString();
	}
	
	private String getFieldLine(Collection<Question> questions){
		StringBuilder sb = new StringBuilder();
		
		sb.append("Username");
		
		for (Question question : questions){
			if (question.isChoice() 
					|| question.isRating() 
					|| question.isDuration()
					|| question.isContinuous()
					|| question.isTextLong()){
					for (Field field : question.getFields()){
						sb.append("\t").append(field.getLabel());
					}
				} else if (question.isMultipleChoice()
					|| question.isMultipleRating()){
					for (TextField tf : question.getTextFields()){
						for (Field field : question.getFields()){
							sb.append("\t").append(field.getLabel() + "::" + tf.getLabel());
						}
					}
				} else if (question.isText()){
					for (TextField tf : question.getTextFields()){
						sb.append("\t").append(tf.getLabel());
					}
				} else if (question.isContactInfo()){
					for (ContactField cf : question.getContactFields()){
						sb.append("\t").append(cf.getLabel());
					}
				} else {
					logger.warn("Unsupported question type:" + question.getType());
				}
		}
		
		sb.append("\t").append("Label");
		sb.append("\t").append("FirstName");
		sb.append("\t").append("LastName");
		sb.append("\t").append("MidName");
		sb.append("\t").append("Type");
		sb.append("\n");
		
		return sb.toString();
	}
}
