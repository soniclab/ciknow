package ciknow.io.temp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ciknow.dao.EdgeDao;
import ciknow.dao.NodeDao;
import ciknow.domain.*;
import ciknow.util.Beans;
import ciknow.util.Constants;

public class PerceivedChoice2MultipleRelationalChoice {
	private static Log logger = LogFactory.getLog(PerceivedChoice2MultipleRelationalChoice.class);
	
	public static void main(String[] args) throws IOException{
		Beans.init("applicationContext-datasource.xml", "applicationContext-dao.xml");
		NodeDao nodeDao = (NodeDao)Beans.getBean("nodeDao");
		EdgeDao edgeDao = (EdgeDao)Beans.getBean("edgeDao");
		
		String inFile = args[0];
		String shortName = args[1];
		BufferedReader reader = new BufferedReader(new FileReader(inFile));
		Map<String, Node> nodeMap = new HashMap<String, Node>();
		String fromUsername, toUsername;
		Node fromNode, toNode;
		List<Edge> edges = new LinkedList<Edge>();
		
		logger.info("****read perceivedChoice data file and create edges from multipleRelationalChoice question****");
		logger.info("process first line to get edgeTypes");
		String line = reader.readLine();
		String[] parts = line.split("\t", -1);
		fromUsername = parts[0].substring(parts[0].indexOf(Constants.SEPERATOR) + 1);
		logger.debug("perceiver: " + fromUsername);
		fromNode = getNodeByUsername(nodeMap, nodeDao, fromUsername);
		String[] edgeTypes = new String[parts.length-1];
		for (int i=0; i<edgeTypes.length; i++){
			edgeTypes[i] = shortName + Constants.SEPERATOR + parts[i+1];
		}
		
		logger.info("reading line by line...");
		line = reader.readLine();
		while (line != null){
			if (line.trim().isEmpty()) {
				line = reader.readLine();
				continue;
			}
			
			parts = line.split("\t", -1);
			toUsername = parts[0];
			toNode = getNodeByUsername(nodeMap, nodeDao, toUsername);
			
			for (int j=1; j<parts.length; j++){
				if (parts[j].equals("0")) continue;
				Edge edge = new Edge();
				edge.setType(edgeTypes[j-1]);
				edge.setFromNode(fromNode);
				edge.setToNode(toNode);				
				edges.add(edge);
			}
			
			line = reader.readLine();
			if (line != null && line.startsWith("########")){
				line = reader.readLine();
				parts = line.split("\t", -1);
				fromUsername = parts[0].substring(parts[0].indexOf(Constants.SEPERATOR) + 1);
				logger.debug("perceiver: " + fromUsername);
				fromNode = getNodeByUsername(nodeMap, nodeDao, fromUsername);
				line = reader.readLine();
			}
		}
		
		logger.info("saving " + edges.size() + " edges...");
		edgeDao.save(edges);
	}
	
	private static Node getNodeByUsername(Map<String, Node> nodeMap, NodeDao nodeDao, String username){
		Node node = nodeMap.get(username);
		if (node == null){
			node = nodeDao.findByUsername(username);
			if (node == null) {
				logger.error("Cannot find node with username=" + username +"!!!!!!");
			}
			nodeMap.put(username, node);
		}
		return node;
	}
}
