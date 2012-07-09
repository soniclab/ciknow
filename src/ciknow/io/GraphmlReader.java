package ciknow.io;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import ciknow.dao.EdgeDao;
import ciknow.dao.GroupDao;
import ciknow.dao.NodeDao;
import ciknow.dao.RoleDao;
import ciknow.domain.*;
import ciknow.util.Beans;
import ciknow.util.Constants;

public class GraphmlReader {
	private static Log logger = LogFactory.getLog(GraphmlReader.class);
	public static final String NODE_TYPE_PREFIX = "graphml_node_type_";
	public static final String EDGE_TYPE_PREFIX = "graphml_edge_type_";
	
	private NodeDao nodeDao;
	private EdgeDao edgeDao;
	private GroupDao groupDao;
	private RoleDao roleDao;
	
	private String overwrite = "0";
	private Map<String, GraphmlKey> keyMap = new HashMap<String, GraphmlKey>();
	private Map<String, Group> groupMap = new HashMap<String, Group>();
	private Map<String, Role> roleMap = new HashMap<String, Role>();
	private Map<String, Node> nodeMap = new HashMap<String, Node>();
	
	private Map<String, Node> usernameToNodeMap = new HashMap<String, Node>();
	private Set<Edge> edgesByDuplicatedNodes = new HashSet<Edge>();
	private StringBuilder sb = new StringBuilder();
	
	public static void main(String[] args) throws Exception{
		String dirname = args[0];
		String context = args[1];
		
		Beans.init();	
		
		GraphmlReader graphmlReader = (GraphmlReader) Beans.getBean("graphmlReader");
		graphmlReader.read(new FileInputStream(dirname + context + "_graphml.xml"), "0");
	}
	
	/**
	 * Read data from graphml
	 * For duplicated nodes:
	 * 	- non-existing attribute is added
	 * 	- existing attribute is override
	 * 	- group/roles of the nodes are unioned
	 * For duplicated edges (among duplicated nodes): ignored
	 * 
	 * @param is
	 * @param overwrite
	 * @throws Exception
	 */
	public void read(InputStream is, String overwrite) throws Exception{		
		logger.info("importing graphml...");
		keyMap.clear();
		groupMap.clear();
		roleMap.clear();
		nodeMap.clear();
		usernameToNodeMap.clear();
		edgesByDuplicatedNodes.clear();
		sb = new StringBuilder();
		this.overwrite = overwrite;
		
		logger.debug("preparing group map...");
		List<Group> groups = groupDao.getAll();		
		for (Group g : groups){
			groupMap.put(g.getName().trim(), g);
		}
		
		logger.debug("preparing role map...");
		List<Role> roles = roleDao.getAll();		
		for (Role r : roles){
			roleMap.put(r.getName().trim(), r);
		}
		
		logger.debug("preparing username to node map...");
		List<Node> allNodes = nodeDao.loadAll();
		for (Node n : allNodes){
			usernameToNodeMap.put(n.getUsername().trim(), n);
		}
		
		logger.debug("reading xml document...");
		SAXReader reader = new SAXReader();
		Document doc = reader.read(new InputStreamReader(is, "UTF-8"));
		Element graphml = doc.getRootElement();
		
		logger.debug("preparing key map...");
		readKeys(graphml);
		
		Element graph = graphml.element("graph");
		Boolean defaultDirected = graph.attributeValue("edgedefault").equals("directed")?true:false;
		
		logger.debug("reading nodes...");
		List<Node> nodes = readNodes(graph);	
		if (sb.toString().length() > 0){
			throw new Exception(sb.toString());
		}
		
		logger.debug("processing duplicated nodes in the system...");
		Set<Node> duplicatedNodes = new HashSet<Node>();
		for (Node n : nodes){
			String username = n.getUsername();
			Node oldNode = usernameToNodeMap.get(username);
			if (oldNode != null){
				if (overwrite.equals("0")){
					sb.append(">> Node already exists: " + username + "\n");
				} else {
					n.setId(oldNode.getId());
					n.setVersion(oldNode.getVersion());
					
					Map<String, String> attrs = oldNode.getAttributes();
					attrs.putAll(n.getAttributes());
					n.setAttributes(attrs);
					
					attrs = oldNode.getLongAttributes();
					attrs.putAll(n.getLongAttributes());
					n.setLongAttributes(attrs);
					
					n.getGroups().addAll(oldNode.getGroups());
					n.getRoles().addAll(oldNode.getRoles());
					
					duplicatedNodes.add(n);
				}
			}
		}
		
		if (sb.toString().length() > 0){
			sb.append("You can overwrite duplicated nodes by checking the 'Overwrite' box.\n");
			sb.append("If you do decided to overwrite the nodes, notice that duplicated edges among these duplicated nodes are ignored, for good.\n");
			throw new Exception(sb.toString());
		}
		
		saveNodes(nodes);
		
		if (duplicatedNodes.size() > 0) {
			logger.debug("There are " + duplicatedNodes.size() + " nodes overwritten.");			
			edgesByDuplicatedNodes.addAll(edgeDao.findEdgesAmongNodes(duplicatedNodes));
			logger.debug("There are " + edgesByDuplicatedNodes.size() + " edges among these overwritten/duplicated nodes.");
		}
		
		logger.debug("reading edges...");
		List<Edge> edges = readEdges(graph, defaultDirected);				
		saveEdges(edges);
		
		logger.info("graphml imported.");
	}

	private void saveNodes(List<Node> nodes) {
		logger.debug("saving " + nodes.size() + " nodes...");
		//nodeDao.save(nodes);
		List<Node> batchNodes = new LinkedList<Node>();
		int i = 0;
		int total = 0;
		for (Node node : nodes){
			batchNodes.add(node);
			i++;
			total++;
			
			if (i == 5000){
				nodeDao.save(batchNodes);
				batchNodes.clear();
				i = 0;
				logger.debug(total + " nodes saved.");
			}
		}
		
		nodeDao.save(batchNodes);
		logger.debug(total + " nodes saved.");
	}

	private void saveEdges(List<Edge> edges) {
		logger.debug("saving " + edges.size() + " edges...");
		//edgeDao.save(edges);
		List<Edge> batchEdges = new LinkedList<Edge>();
		int i = 0;
		int total = 0;
		for (Edge edge : edges){
			batchEdges.add(edge);
			i++;
			total++;
			
			if (i == 5000) {
				edgeDao.save(batchEdges);
				batchEdges.clear();
				i = 0;
				logger.debug(total + " edges saved.");
			}
		}
		
		edgeDao.save(batchEdges);
		logger.debug(total + " edges saved.");
	}



	@SuppressWarnings("unchecked")
	private void readKeys(Element graphml) {
		for (Iterator kItr = graphml.elementIterator("key"); kItr.hasNext();){
			Element keyElement = (Element) kItr.next();
			String key = keyElement.attributeValue("id");
			String domain = keyElement.attributeValue("for");
			String attrName = keyElement.attributeValue("attr.name");
			String attrType = keyElement.attributeValue("attr.type");
			keyMap.put(key, new GraphmlKey(key, domain, attrName, attrType));
		}
	}

	@SuppressWarnings("unchecked")
	private List<Node> readNodes(Element graph) {
		Set<String> usernameInFile = new HashSet<String>();
		Group groupAll = groupMap.get(Constants.GROUP_ALL);
		Group groupUser = groupMap.get(Constants.GROUP_USER);
		List<Node> nodes = new LinkedList<Node>();
		List<String> nodeTypes = nodeDao.getNodeTypes();
		for (Iterator nodeItr = graph.elementIterator("node"); nodeItr.hasNext(); ){
			Element nodeElement = (Element) nodeItr.next();
			String id = nodeElement.attributeValue("id");
			
			Node node = new Node();			
			for (Iterator dataItr = nodeElement.elementIterator(); dataItr.hasNext();){
				Element dataElement = (Element) dataItr.next();
				String key = dataElement.attributeValue("key");
				String value = dataElement.getTextTrim();
				
				GraphmlKey gk = keyMap.get(key);
				String name = gk.getAttrName();
				//String type = gk.getAttrType();
				
				// update admin node
				/*
				if (name.equals("username") && value.equals("admin")){
					Node admin = nodeDao.findById(1L);
					node.setId(1L);
					node.setVersion(admin.getVersion());
					node.setUsername(value);
					continue;
				}
				*/
				
				// node attributes
				if (key.startsWith(GraphmlWriter.NODE_ATTR_PREFIX)) {
					if (value.length() > 1024) {
						logger.warn("attribute (" + key + ") value truncated to be 1024 characters.");
						value = value.substring(0, 1024);
					}
					node.setAttribute(name, value);
				}
				// node long attributes
				else if (key.startsWith(GraphmlWriter.NODE_LONG_ATTR_PREFIX)) {
					node.setLongAttribute(name, value);
				}
				// node groups
				else if (key.equals("group")) setGroups(node, value);
				// node roles
				else if (key.equals("role")) setRoles(node, value);
				// node properties
				else if (name.equals("username")) node.setUsername(value.length() == 0?id:value);
				else if (name.equals("password")) node.setPassword(value);
				else if (name.equals("firstName")) node.setFirstName(value);
				else if (name.equals("lastName")) node.setLastName(value);
				else if (name.equals("midName")) node.setMidName(value);
				else if (name.equals("addr1")) node.setAddr1(value);
				else if (name.equals("addr2")) node.setAddr2(value);
				else if (name.equals("city")) node.setCity(value);
				else if (name.equals("state")) node.setState(value);
				else if (name.equals("country")) node.setCountry(value);
				else if (name.equals("zipcode")) node.setZipcode(value);
				else if (name.equals("email")) node.setEmail(value);
				else if (name.equals("phone")) node.setPhone(value);
				else if (name.equals("cell")) node.setCell(value);
				else if (name.equals("fax")) node.setFax(value);
				else if (name.equals("department")) node.setDepartment(value);
				else if (name.equals("organization")) node.setOrganization(value);
				else if (name.equals("unit")) node.setUnit(value);
				else if (name.equals("label")) node.setLabel(value.length() == 0?id:value);
				else if (name.equals("type")) {
					String nodeType = NODE_TYPE_PREFIX + (nodeTypes.size() + 1);
					if (value.length() != 0) nodeType = value;
					node.setType(nodeType);
				}
				else if (name.equals("uri")) node.setUri(value);
				else if (name.equals("enabled")) node.setEnabled(Boolean.valueOf(value));
			}
			
			if (node.getType().equals(Node.DEFAULT_NODE_TYPE)) {
				String nodeType = NODE_TYPE_PREFIX + (nodeTypes.size() + 1);
				node.setType(nodeType);
			}
			if (node.getUsername() == null) node.setUsername(node.getType() + "__" + id);
			if (node.getLabel().equals(Node.DEFAULT_NODE_LABEL)) node.setLabel(node.getUsername());			
			
			// ADD TO DEFAULT GROUP
			node.getGroups().add(groupAll);
			if (node.getType().equals(Constants.NODE_TYPE_USER)) node.getGroups().add(groupUser);
			
			// Duplicate checking
			String username = node.getUsername();
			if (usernameInFile.contains(username)){
				sb.append(">> Duplicated username in file: " + username + "\n");
			} else {
            	usernameInFile.add(username);
            }
			
			nodes.add(node);
			nodeMap.put(id, node);
		}
		return nodes;
	}
	
	@SuppressWarnings("unchecked")
	private List<Edge> readEdges(Element graph, Boolean defaultDirected) throws Exception {
		List<String> edgeTypes = edgeDao.getEdgeTypes();
		List<Edge> edges = new LinkedList<Edge>();	
		int duplicateCount = 0;
		edgeloop:
		for (Iterator eItr = graph.elementIterator("edge"); eItr.hasNext();){
			Element edgeElement = (Element) eItr.next();
			String source = edgeElement.attributeValue("source");
			Node fnode = nodeMap.get(source);
			if (fnode == null){
				throw new Exception("fromNode is missing for edge: \n" + edgeElement.toString());
//				logger.warn("fromNode is missing for edge: \n" + edgeElement.toString());
//				continue edgeloop;
			}
			String target = edgeElement.attributeValue("target");
			Node tnode = nodeMap.get(target);
			if (tnode == null){
				throw new Exception("toNode is missing for edge: \n" + edgeElement.toString());
//				logger.warn("toNode is missing for edge: \n" + edgeElement.toString());
//				continue edgeloop;
			}			
			
			Edge edge = new Edge();
			edge.setFromNode(fnode);
			edge.setToNode(tnode);
			edge.setDirected(defaultDirected);
			String directed = edgeElement.attributeValue("directed");
			if (directed != null) edge.setDirected(new Boolean(directed));
			
			for (Iterator dataItr = edgeElement.elementIterator(); dataItr.hasNext();){
				Element dataElement = (Element) dataItr.next();
				String key = dataElement.attributeValue("key");
				String value = dataElement.getTextTrim();
				
				GraphmlKey gk = keyMap.get(key);
				String name = gk.getAttrName();
				//String type = gk.getAttrType();
				
				// edge attributes
				if (key.startsWith(GraphmlWriter.EDGE_ATTR_PREFIX)) {					
					if (value.length() > 255) {
						logger.warn("attribute (" + key + ") value truncated to be 255 characters.");
						value = value.substring(0, 255);
					}
					edge.setAttribute(name, value);
				}
				// edge long attributes
				else if (key.startsWith(GraphmlWriter.EDGE_LONG_ATTR_PREFIX)) edge.setLongAttribute(name, value);
				// edge properties
				else if (name.equals("creator")) {
					Node creator = nodeMap.get(value);
					if (creator == null){
						throw new Exception("creator is missing for edge: \n" + edgeElement.toString());
//						logger.warn("creator is missing for edge: \n" + edgeElement.toString());
//						continue edgeloop;
					}
					edge.setCreator(creator);
				} else if (name.equals("type")){
					String edgeType = EDGE_TYPE_PREFIX + (edgeTypes.size() + 1);
					if (value.length() != 0) edgeType = value;
					edge.setType(edgeType);
				} else if (name.equals("weight")){
					edge.setWeight(Double.parseDouble(value));
				}
			}
			
			if (edge.getType().equals(Edge.DEFAULT_EDGE_TYPE)){
				String edgeType = EDGE_TYPE_PREFIX + (edgeTypes.size() + 1);
				edge.setType(edgeType);
			}
			
			if (!edgesByDuplicatedNodes.contains(edge)) edges.add(edge);	
			else {
				logger.debug("duplicated edge: " + edge);
				duplicateCount++;
			}
		}
		
		logger.debug("There are " + duplicateCount + " duplicated edges among overwritten/duplicated nodes ignored.");
		return edges;
	}
	
	private void setGroups(Node node, String names){
		String[] groupNames = names.split(Constants.SEPERATOR);
		for (String groupName : groupNames){
			groupName = groupName.trim();
			if (groupName.length() == 0) continue;
			
			Group g = groupMap.get(groupName);
			if (g == null){
				g = new Group();
				g.setName(groupName);
				groupDao.save(g);
				groupMap.put(groupName, g);
				logger.info("new group (name=" + groupName + ") is created.");
			}
			node.getGroups().add(g);
		}
	}
	
	private void setRoles(Node node, String names){
		String[] roleNames = names.split(Constants.SEPERATOR);
		for (String roleName : roleNames){
			roleName = roleName.trim();
			if (roleName.length() == 0) continue;
			
			Role r = roleMap.get(roleName);
			if (r == null){
				r = new Role();
				r.setName(roleName);
				roleDao.save(r);
				roleMap.put(roleName, r);
				logger.info("new role (name=" + roleName + ") is created.");
			}
			node.getRoles().add(r);
		}
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
