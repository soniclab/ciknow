package ciknow.io;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import ciknow.dao.*;
import ciknow.util.Beans;
import ciknow.util.Constants;
import ciknow.util.StringUtil;
import ciknow.domain.Edge;
import ciknow.domain.Node;
import ciknow.domain.Question;
import ciknow.domain.Scale;

/**
 * 
 * @author gyao
 * 
 */
public class DLQuestionReader{
	private static Log logger = LogFactory.getLog(DLQuestionReader.class);
    private NodeDao nodeDao;
    private EdgeDao edgeDao;
    
	public static void main(String[] args) throws Exception{
		if (args.length < 1) {
			logger.error("filename (e.g. c:/path/to/file) is required.");
			return;
		}
		String filename = args[0];
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		
		Beans.init();
        QuestionDao questionDao = (QuestionDao) Beans.getBean("questionDao");
        DLQuestionReader questionReader = (DLQuestionReader) Beans.getBean("dlQuestionReader");
        String line = reader.readLine().trim();
        logger.debug("finding question by shortname: " + line);
        Question question = questionDao.findByShortName(line);
        if (question == null){
        	logger.error("unrecognized shortName: " + line);
        	return;
        } else {
        	logger.debug("question found");
        	questionReader.read(reader, question);
        }
	}
	
    public DLQuestionReader(){

    }

	public void read(BufferedReader reader, Question question) throws Exception {
        logger.info("importing quesiton(name=" + question.getShortName() + ").");        
        if (question.isRelationalChoice()) readRelationalChoice(reader, question);
        else if (question.isRelationalRating()) readRelationalRating(reader, question);
        else {
        	String msg = "question type: " + question.getType() + " is not supported for upload.";
        	throw new Exception(msg);
        }
        logger.info("done.");
    }

	
	private void readRelationalRating(BufferedReader reader, Question question) throws Exception{
		logger.info("reading relational rating question(" + question.getShortName() + ") data...");
        
		// remove old edges
		logger.debug("removing old edges for this question.");
        String edgeType = question.getEdgeType();
        List<Edge> oldEdges = edgeDao.findByType(edgeType, true);        
        edgeDao.delete(oldEdges);        

        logger.debug("reading each row and create edges...");
        int count = 0;
        int total = 0;
        List<Edge> edges = new ArrayList<Edge>();        
        String line = reader.readLine();
        while (line != null){
            List<String> flags = StringUtil.splitAsList(line, "\t");
            if (flags.size() < 3){
            	logger.warn("invalid line: " + line);
            	line = reader.readLine();
            	continue;
            }
            
            // get from node
            Node fromNode = nodeDao.getProxy(new Long(flags.get(0).trim()));
            
            // get to node
            Node toNode = nodeDao.getProxy(new Long(flags.get(1).trim()));
            
            // get scale 
            Scale scale = question.getScaleByName(flags.get(2));
            String attrValue = question.makeScaleKey(scale);
            
            // create edge
            Edge edge = new Edge();
            edge.setCreator(null);
            edge.setFromNode(fromNode);
            edge.setToNode(toNode);
            edge.setType(question.getEdgeType());
            edge.setDirected(true);
            edge.setWeight(1.0);
            edge.setAttribute(Constants.SCALE_KEY, attrValue);
            
            edges.add(edge);
            count++;
            total++;
            
            if (count == 5000){
            	edgeDao.save(edges);
            	edges.clear();
            	count = 0;
            	logger.debug(total + " edges saved.");
            }

            line = reader.readLine();
        }

        edgeDao.save(edges);
        logger.debug(total + " edges saved.");
        logger.info("reading relational rating question(" + question.getShortName() + ") data... done");
	}


	private void readRelationalChoice(BufferedReader reader, Question question) throws Exception{
		logger.info("reading relational choice question(" + question.getShortName() + ") data...");
        // remove old edges
		logger.debug("removing old edges for this question.");
        String edgeType = question.getEdgeType();
        List<Edge> oldEdges = edgeDao.findByType(edgeType, true);        
        edgeDao.delete(oldEdges);        

        logger.debug("reading each row and create edges...");
        int count = 0;
        int total = 0;
        List<Edge> edges = new ArrayList<Edge>();        
        String line = reader.readLine();
        while (line != null){
            List<String> flags = StringUtil.splitAsList(line, "\t");
            if (flags.size() < 2){
            	logger.warn("invalid line: " + line);
            	line = reader.readLine();
            	continue;
            }
            
            // get from node
            Node fromNode = nodeDao.getProxy(new Long(flags.get(0).trim()));
            
            // get to node
            Node toNode = nodeDao.getProxy(new Long(flags.get(1).trim()));
            
            // create edge
            Edge edge = new Edge();
            edge.setCreator(null);
            edge.setFromNode(fromNode);
            edge.setToNode(toNode);
            edge.setType(question.getEdgeType());
            edge.setDirected(true);
            edge.setWeight(1.0);
            
            edges.add(edge);
            count++;
            total++;
            
            if (count == 5000){
            	edgeDao.save(edges);
            	edges.clear();
            	count = 0;
            	logger.debug(total + " edges saved.");
            }

            line = reader.readLine();
        }

        edgeDao.save(edges);
        logger.debug(total + " edges saved.");
        logger.info("reading relational choice question(" + question.getShortName() + ") data... done");
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
}
