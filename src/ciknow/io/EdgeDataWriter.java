package ciknow.io;

import java.io.File;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ciknow.dao.EdgeDao;
import ciknow.dao.NodeDao;
import ciknow.dao.QuestionDao;
import ciknow.domain.Edge;
import ciknow.domain.Field;
import ciknow.domain.Question;
import ciknow.util.Beans;

public class EdgeDataWriter {

	private static Log logger = LogFactory.getLog(EdgeDataWriter.class);
	private NodeDao nodeDao;
	private EdgeDao edgeDao;
	private QuestionDao questionDao;
	
	public static void main(String[] args) throws Exception{
		Beans.init();
		EdgeDataWriter ndw = (EdgeDataWriter)Beans.getBean("edgeDataWriter");
		QuestionDao questionDao = (QuestionDao)Beans.getBean("questionDao");
		
		String[] questionIds;
		if (args.length == 0) {
			logger.warn("You did not provid relational question Ids. All relational questions data will be exported by default.");
			List<Question> questions = questionDao.getAll();
			List<Question> relationalQuestions = new ArrayList<Question>();
			for (Question question : questions){
				if (question.isRelational()) relationalQuestions.add(question);
			}
			questionIds = new String[relationalQuestions.size()];
			int index = 0;
			for (Question question : relationalQuestions){
				questionIds[index] = question.getId().toString();
				index += 1;
			}
		} else questionIds = args;
		
		String filename = "edgeData.txt";
		File file = new File(filename);
		PrintWriter writer = new PrintWriter(file);
		Map<String, String> options = new HashMap<String, String>();
		ndw.write(writer, questionIds, options);
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
	public EdgeDao getEdgeDao() {
		return edgeDao;
	}
	public void setEdgeDao(EdgeDao edgeDao) {
		this.edgeDao = edgeDao;
	}
	public QuestionDao getQuestionDao() {
		return questionDao;
	}
	public void setQuestionDao(QuestionDao questionDao) {
		this.questionDao = questionDao;
	}	
	
	public void write(Writer writer, String[] questionIds, Map<String, String> options) throws Exception{
		logger.info("Export Edge Data");
		
		logger.info("Determine edges to export...");		
		Map<String, Question> edgeTypeToQuestionMap = new LinkedHashMap<String, Question>();	// keep the edgeType insertion order
		Map<Long, Map<Long, Map<String, Edge>>> edgeMap = new TreeMap<Long, Map<Long, Map<String, Edge>>>(); // sorted map by fromNodeId
		for (String qid : questionIds){
			Question question = questionDao.findById(Long.parseLong(qid));
			if (question == null){
				logger.warn("Cannot find question with id=" + qid);
				continue;
			}
			if (!question.isRelational()) {
				logger.warn("Question: " + question.getShortName() + " is not relational.");
				continue;
			}

			String edgeType = null;
			List<Edge> edges;
			if (question.isRelationalChoiceMultiple() 
				|| question.isRelationalRatingMultiple()){
				for (Field field : question.getFields()){
					edgeType = question.getEdgeTypeWithField(field);
					edgeTypeToQuestionMap.put(edgeType, question);
					edges = edgeDao.loadByType(edgeType, false);
					populateEdgeMap(edgeMap, edges);
				}
			} else {
				edgeType = question.getEdgeType();
				if (edgeType != null) {
					edgeTypeToQuestionMap.put(edgeType, question);
					edges = edgeDao.loadByType(edgeType, false);
					populateEdgeMap(edgeMap, edges);
				}
			}
		}
		if (edgeMap.isEmpty()) {
			throw new Exception("There is not relations to export.");
		}
		
		logger.info("Write header");
		writer.append("fromNodeId");
		writer.append("\t").append("toNodeId");
		for (String edgeType : edgeTypeToQuestionMap.keySet()){
			writer.append("\t").append(edgeType);
		}
		writer.append("\n");
		
		logger.info("Write content...");
		for (Long fromNodeId : edgeMap.keySet()){
			Map<Long, Map<String, Edge>> toNodeEdgeMap = edgeMap.get(fromNodeId);
			
			StringBuilder sb = new StringBuilder();
			for (Long toNodeId : toNodeEdgeMap.keySet()){
				Map<String, Edge> edgeTypeMap = toNodeEdgeMap.get(toNodeId);
								
				sb.append(fromNodeId);
				sb.append("\t").append(toNodeId);
				for (String edgeType : edgeTypeToQuestionMap.keySet()){
					Edge edge = edgeTypeMap.get(edgeType);
					sb.append("\t").append(edge==null?"0":edge.getWeight());
				}
				sb.append("\n");
			}			
			writer.append(sb.toString());
		}

		logger.info("Export finished.");
	}
	
	private void populateEdgeMap(Map<Long, Map<Long, Map<String, Edge>>> edgeMap, List<Edge> edges){
		for (Edge edge : edges){
			Long fromNodeId = edge.getFromNode().getId();
			Long toNodeId = edge.getToNode().getId();
			
			Map<Long, Map<String, Edge>> toNodeEdgeMap = edgeMap.get(fromNodeId);
			if (toNodeEdgeMap == null) {
				toNodeEdgeMap = new TreeMap<Long, Map<String, Edge>>(); // sorted map by toNodeId
				edgeMap.put(fromNodeId, toNodeEdgeMap);
			}
			
			Map<String, Edge> edgeTypeMap = toNodeEdgeMap.get(toNodeId);
			if (edgeTypeMap == null){
				edgeTypeMap = new HashMap<String, Edge>();
				toNodeEdgeMap.put(toNodeId, edgeTypeMap);
			}
			
			edgeTypeMap.put(edge.getType(), edge);
		}
	}
}
