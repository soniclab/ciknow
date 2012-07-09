package ciknow.io;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ciknow.dao.EdgeDao;
import ciknow.dao.GroupDao;
import ciknow.dao.NodeDao;
import ciknow.dao.RoleDao;
import ciknow.domain.Edge;
import ciknow.domain.Group;
import ciknow.domain.Node;
import ciknow.util.Beans;
import ciknow.util.Constants;

/**
 * Read UCINET DL file into CI-KNOW database
 * Currently only support 'edgelist1' and 'edgelist2' formats
 * Please use most conventional format, don not user exotic format even though 
 * they are accepted by UICNET
 * @author gyao
 *
 */
public class DLReader {
	private static Log logger = LogFactory.getLog(DLReader.class);
	
	public static final String NODE_TYPE_PREFIX = "dl_node_type_";
	public static final String EDGE_TYPE_PREFIX = "dl_edge_type_";
	
	private static final String LABEL_EMBEDDED = "labels embedded";
	private static final String LABELS_HEADER = "labels:";
	private static final String ROW_LABELS_HEADER = "row labels:";
	private static final String COL_LABELS_HEADER = "col labels:";
	private static final String COL_LABELS_HEADER_ALT = "column labels:";
	private static final String DATA_HEADER = "data:";
	
	private NodeDao nodeDao;
	private EdgeDao edgeDao;
	private GroupDao groupDao;
	private RoleDao roleDao;
	
	private int n = -1;
	private int nm = -1;
	private int nr = -1;
	private int nc = -1;
	private String format = null;
	private boolean labelEmbedded = false;
	//private List<Node> nodes;
	private Map<Long, Node> nodesMap = new HashMap<Long, Node>();	
	//private List<Node> colNodes;	// column nodes for 2-mode data (format=edgelist2)
	private Map<Long, Node> colNodesMap = new HashMap<Long, Node>();
	private List<Edge> edges = new LinkedList<Edge>();
	
	private String line; // current line
	private Group groupAll;
	
	public static void main(String[] args) throws Exception{
		Beans.init();
		DLReader dlr = (DLReader)Beans.getBean("dlReader");
		String filename;
		//filename = "data/edgelist2.txt";
		//filename = "data/edgelist2.embedded.txt";
		//filename = "data/mike/mixed.ucinet";
		filename = "data/dl/dl__Article__Citation__Article.embedded.txt";
		//filename = "data/dl/dl__Article__Article by Subject Category__Subject Category.embedded.txt";
		dlr.read(filename);
		
	}
	
	public DLReader(){
		
	}

	private void init(){
		n = -1;
		nm = -1;
		nr = -1;
		nc = -1;
		format = null;
		labelEmbedded = false;
		nodesMap.clear();
		colNodesMap.clear();
		edges.clear();
		line = null;
		groupAll = groupDao.findByName(Constants.GROUP_ALL);
	}
	
	public void read(String filename) throws Exception{
		read(new FileInputStream(filename));
	}
	
	public void read(InputStream is) throws Exception{	
		init();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		line = reader.readLine();
		while (line != null){
			line = line.trim();
			if (line.length() == 0 || line.startsWith("#")) {
				line = reader.readLine();
			} else if (line.startsWith("dl") || line.startsWith("DL")) {
				readHeader();
				line = reader.readLine();
			}
			else if (line.startsWith(DLReader.LABELS_HEADER)) readLabels(reader, false);
			else if (line.startsWith(DLReader.ROW_LABELS_HEADER)) readLabels(reader, false);
			else if (line.startsWith(DLReader.COL_LABELS_HEADER) 
					|| line.startsWith(DLReader.COL_LABELS_HEADER_ALT)) {
				readLabels(reader, true);
			}
			else if (line.equalsIgnoreCase(DLReader.LABEL_EMBEDDED)) {
				labelEmbedded = true;
				logger.info("labels embedded: " + labelEmbedded);
				line = reader.readLine();
			}
			else if (line.startsWith(DLReader.DATA_HEADER)) {
				if (!labelEmbedded) readData(reader);
				else readDataWithLabelEmbedded(reader);
			} else {
				throw new Exception("unrecoganized line: " + line);
//				logger.warn("unrecoganized line: " + line);
//				line = reader.readLine();				
			}
		}
	}
	
	/**
	 * Read DL meta data including n, nm, nc, nr, format
	 * All the DL meta data should be in a single line. 
	 * Please avoid exotic format:
	 * e.g, use 'n=4', DO NOT use 'n,4', 'n 4', 'n = 4', 'n= 4', 'n =4'
	 * @param line
	 * @param reader
	 */
	private void readHeader(){
		logger.info("reading headers...");
		String[] pairs = line.split(" ");
		for (String pair : pairs){
			if (pair.indexOf("=") < 0) continue;
			
			String[] parts = pair.trim().split("=");
			if (parts[0].trim().equalsIgnoreCase("n")){ 
				n = Integer.parseInt(parts[1].trim());
				logger.info("n=" + n);
			}
			else if (parts[0].trim().equalsIgnoreCase("nm")) {
				nm = Integer.parseInt(parts[1].trim()); 
				logger.info("nm=" + nm);
			}
			else if (parts[0].trim().equalsIgnoreCase("nr")) {
				nr = Integer.parseInt(parts[1].trim()); 
				logger.info("nr=" + nr);
			}
			else if (parts[0].trim().equalsIgnoreCase("nc")) {
				nc = Integer.parseInt(parts[1].trim()); 
				logger.info("nc=" + nc);
			}
			else if (parts[0].trim().equalsIgnoreCase("format")) {
				format = parts[1].trim(); 
				logger.info("format=" + format);
			}
		}
	}
	
	/**
	 * Read labels and create corresponding nodes
	 * Assumption: 
	 * labels can be in multiple lines following the 'labels:' line
	 * labels end when encountering line start with 'data:'
	 * @param reader
	 * @throws IOException 
	 */
	private void readLabels(BufferedReader reader, boolean isColLabels) throws IOException{
		logger.info("reading labels ...");
		
		line = reader.readLine();
		long index = 1;
		List<String> nodeTypes = nodeDao.getNodeTypes();
		String nodeType = "dl_type_" + (nodeTypes.size() + 1);
		while (line != null){
			line = line.trim();
			
			if (line.indexOf(DLReader.DATA_HEADER) >= 0 
					|| line.indexOf(DLReader.COL_LABELS_HEADER) >= 0
					|| line.indexOf(DLReader.COL_LABELS_HEADER_ALT) >= 0
					|| line.indexOf(DLReader.ROW_LABELS_HEADER) >= 0) break;
			
			if (line.length() == 0 || line.startsWith("#")) {
				line = reader.readLine();
				continue;
			}
			
			String[] labels = line.split(",", 0);			
			for (String label : labels){
				label = label.trim();
				Node node = new Node();
				node.setUsername(nodeType + "__" + index);
				node.setLabel(label.length() > 1024 ? label.substring(0, 1024) : label);
				node.setType(nodeType);
				// ADD TO DEFAULT GROUP
				node.getGroups().add(groupAll);
				
				if (isColLabels) colNodesMap.put(index++, node);
				else nodesMap.put(index++, node);				
			}
			
			line = reader.readLine();			
		}
		
		logger.info("saving nodes...");
		Collection<Node> nodes;
		if (isColLabels) nodes = colNodesMap.values();
		else nodes = nodesMap.values();
		nodeDao.save(nodes);
//		for (Node node : nodes){
//			logger.debug(node);
//		}		
		logger.info(nodes.size() + " nodes saved.");
	}
	
	/**
	 * read data and create edges for format=edgelist1, label not embedded
	 * @param reader
	 * @throws Exception 
	 */
	private void readData(BufferedReader reader) throws Exception{
		logger.info("reading data...");
		
		line = reader.readLine();
		List<String> edgeTypes = edgeDao.getEdgeTypes();
		int edgeTypeCount = edgeTypes.size() + 1;
		while (line != null){
			line = line.trim();
			if (line.equals("|")){
				edgeTypeCount++;
				line = reader.readLine();
				continue;
			}
			
			String[] parts = line.split(" ", 0);
			if (parts.length < 2 || parts.length > 3) {
				throw new Exception("edge: " + line + " is ignored because it is not confirmed with format=" + format);
//				logger.debug("edge: " + line + " is ignored because it is not confirmed with format=" + format);
//				line = reader.readLine();
//				continue;
			}
			
			Long fid = Long.parseLong(parts[0].trim());
			Node fnode = nodesMap.get(fid);
			if (fnode == null){
				throw new Exception("edge: " + line + " is ignored because node (index=" + fid + ") is missing.");
//				logger.debug("edge: " + line + " is ignored because node (index=" + fid + ") is missing.");
//				line = reader.readLine();
//				continue;
			}
			
			Long tid = Long.parseLong(parts[1].trim());
			Node tnode;
			if (format.equals("edgelist2")) tnode = colNodesMap.get(tid);
			else tnode = nodesMap.get(tid);
			if (tnode == null){
				throw new Exception("edge: " + line + " is ignored because node (index=" + tid + ") is missing.");
//				logger.debug("edge: " + line + " is ignored because node (index=" + tid + ") is missing.");
//				line = reader.readLine();
//				continue;
			}
			
			double weight = 1.0;
			if (parts.length == 3){
				String w = parts[2].trim();				
				try{
					weight = Float.parseFloat(w);
				} catch(Exception e){
					throw new Exception("edge: " + line + " is ignored because edge weight is not a number.");
//					logger.debug("edge: " + line + " is ignored because edge weight is not a number.");
//					line = reader.readLine();
//					continue;
				}
			}
			
			Edge edge = new Edge();
			edge.setFromNode(fnode);
			edge.setToNode(tnode);
			edge.setDirected(true);
			edge.setWeight(weight);
			edge.setType(EDGE_TYPE_PREFIX + edgeTypeCount);
			edges.add(edge);
			
			line = reader.readLine();
		}
		
		logger.info("saving edges...");
		edgeDao.save(edges);	
//		for (Edge edge : edges){
//			logger.debug(edge);
//		}		
		logger.info(edges.size() + " edges saved.");
	}
	
	/**
	 * read data to create nodes and edges for format=edgelist1, label embedded
	 * @param reader
	 * @throws Exception 
	 */
	private void readDataWithLabelEmbedded(BufferedReader reader) throws Exception{
		logger.info("reading data (label embedded)...");
		
		Map<String, Node> labelNodesMap = new HashMap<String, Node>();
		Map<String, Node> labelColNodesMap = new HashMap<String, Node>();
		
		List<String> nodeTypes = nodeDao.getNodeTypes();
		int nodeTypeCount = nodeTypes.size() + 1;

		List<String> edgeTypes = edgeDao.getEdgeTypes();
		int edgeTypeCount = edgeTypes.size() + 1;
		
		int index = 1;
		int colIndex = 1;
		
		line = reader.readLine();
		while (line != null){
			line = line.trim();
			if (line.equals("|")){
				edgeTypeCount++;
				line = reader.readLine();
				continue;
			}
			
			String[] parts = line.split(" ", 0);
			if (parts.length < 2 || parts.length > 3) {
				throw new Exception("edge: " + line + " is ignored because it is not confirmed with format=" + format);
//				logger.debug("edge: " + line + " is ignored because it is not confirmed with format=" + format);
//				line = reader.readLine();
//				continue;
			}
			
			String flabel = parts[0].trim();
			Node fnode = labelNodesMap.get(flabel);
			if (fnode == null){
				fnode = new Node();
				String nodeType = NODE_TYPE_PREFIX + nodeTypeCount;
				fnode.setUsername(nodeType + "__" + index++);
				fnode.setLabel(flabel.length() > 1024 ? flabel.substring(0, 1024) : flabel);											
				fnode.setType(nodeType);
				// ADD TO DEFAULT GROUP
				fnode.getGroups().add(groupAll);	
				
				labelNodesMap.put(flabel, fnode);				
			}
			
			String tlabel = parts[1].trim();
			Node tnode;
			if (format.equals("edgelist1")) {
				tnode = labelNodesMap.get(tlabel);
				if (tnode == null){
					tnode = new Node();
					String nodeType = NODE_TYPE_PREFIX + nodeTypeCount;
					tnode.setUsername(nodeType + "__" + index++);
					tnode.setLabel(tlabel.length() > 1024 ? tlabel.substring(0, 1024) : tlabel);
					tnode.setType(nodeType);
					// ADD TO DEFAULT GROUP
					tnode.getGroups().add(groupAll);	
					
					labelNodesMap.put(tlabel, tnode);
				}
			}			
			else if (format.equals("edgelist2")) {
				tnode = labelColNodesMap.get(tlabel);
				if (tnode == null){
					tnode = new Node();
					String nodeType = NODE_TYPE_PREFIX + (nodeTypeCount + 1);
					tnode.setUsername(nodeType + "__" + colIndex++);
					tnode.setLabel(tlabel.length() > 1024 ? tlabel.substring(0, 1024) : tlabel);
					tnode.setType(nodeType);
					// ADD TO DEFAULT GROUP
					tnode.getGroups().add(groupAll);	
					
					labelColNodesMap.put(tlabel, tnode);
				}
			}
			else {
				throw new Exception("unrecognized format: " + format);
//				logger.error("unrecognized format: " + format);
//				return;
			}

			
			double weight = 1.0;
			if (parts.length == 3){
				String w = parts[2].trim();				
				try{
					weight = Float.parseFloat(w);
				} catch(Exception e){
					throw new Exception("edge: " + line + " is ignored because edge weight is not a number.");
//					logger.debug("edge: " + line + " is ignored because edge weight is not a number.");
//					line = reader.readLine();
//					continue;
				}
			}
			
			Edge edge = new Edge();
			edge.setFromNode(fnode);
			edge.setToNode(tnode);
			edge.setDirected(true);
			edge.setWeight(weight);
			edge.setType(EDGE_TYPE_PREFIX + edgeTypeCount);
			edges.add(edge);
			
			line = reader.readLine();
		}
		
		logger.info("saving nodes...");
		Collection<Node> nodes;
		nodes = labelNodesMap.values();
		nodeDao.save(nodes);
//		for (Node node : nodes){
//			logger.debug(node);
//		}		
		logger.info(nodes.size() + " nodes saved.");
		
		if (format.equals("edgelist2")){
			nodes = labelColNodesMap.values();
			nodeDao.save(nodes);
//			for (Node node : nodes){
//				logger.debug(node);
//			}		
			logger.info(nodes.size() + " col nodes saved.");
		}
		
		logger.info("saving edges...");
		edgeDao.save(edges);	
//		for (Edge edge : edges){
//			logger.debug(edge);
//		}		
		logger.info(edges.size() + " edges saved.");
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

	public GroupDao getGroupDao() {
		return groupDao;
	}

	public void setGroupDao(GroupDao groupDao) {
		this.groupDao = groupDao;
	}

	public RoleDao getRoleDao() {
		return roleDao;
	}

	public void setRoleDao(RoleDao roleDao) {
		this.roleDao = roleDao;
	}			
}
