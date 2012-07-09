package ciknow.io;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import ciknow.dao.EdgeDao;
import ciknow.dao.GroupDao;
import ciknow.dao.NodeDao;
import ciknow.dao.RoleDao;
import ciknow.domain.*;
import ciknow.util.Beans;
import ciknow.util.Constants;

/**
 * Write nodes/edges in graphml format
 * @author gyao
 *
 */
public class GraphmlWriter {
	public static final String NODE_PROP_PREFIX = "np";
	public static final String NODE_ATTR_PREFIX = "na";
	public static final String NODE_LONG_ATTR_PREFIX = "nla";
	
	public static final String EDGE_PROP_PREFIX = "ep";
	public static final String EDGE_ATTR_PREFIX = "ea";
	public static final String EDGE_LONG_ATTR_PREFIX = "ela";
	
	
	private static Log logger = LogFactory.getLog(GraphmlWriter.class);
	private NodeDao nodeDao;
	private EdgeDao edgeDao;
	private GroupDao groupDao;
	private RoleDao roleDao;
	
	private boolean importable = true;
	private Map<String, Question> shortNameToQuestionMap = null;
	
	private Map<String, String> npKeyMap = new HashMap<String, String>();
	private Map<String, String> naKeyMap = new HashMap<String, String>();
	private Map<String, String> nlaKeyMap = new HashMap<String, String>();
	private Map<String, String> epKeyMap = new HashMap<String, String>();
	private Map<String, String> eaKeyMap = new HashMap<String, String>();
	private Map<String, String> elaKeyMap = new HashMap<String, String>();
	
	private Map<Node, String> nodeToIdMap = new HashMap<Node, String>();
	private String xmlStr;
	private boolean forXMLStr;
	public static void main(String[] args) throws IOException{
		String dirname = args[0];
		String context = args[1];
		
		Beans.init();		
		GraphmlWriter writer = (GraphmlWriter) Beans.getBean("graphmlWriter");		
		writer.setImportable(false);
		writer.write(new FileOutputStream(dirname + context + "_graphml.xml"));		
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
	
	public boolean isImportable() {
		return importable;
	}

	public void setImportable(boolean importable) {
		this.importable = importable;
	}

	public Map<String, Question> getShortNameToQuestionMap() {
		return shortNameToQuestionMap;
	}

	public void setShortNameToQuestionMap(
			Map<String, Question> shortNameToQuestionMap) {
		this.shortNameToQuestionMap = shortNameToQuestionMap;
	}

	public void write(OutputStream os) throws IOException{
		List<Node> nodes = nodeDao.loadAll();
		List<Edge> edges = edgeDao.loadAll();
		write(nodes, edges, os);
	}
	
	public void write(Collection<Node> nodes, Collection<Edge> edges, OutputStream os) throws IOException{
		logger.info("exporting graphml...");
				
		OutputFormat format = OutputFormat.createPrettyPrint();
		XMLWriter writer = new XMLWriter(new OutputStreamWriter(os, "UTF-8"), format);
		writer.write(createDocument(nodes, edges));
		writer.flush();
		writer.close();
		
		logger.info("exported graphml.");		
	}
	
	public void writeStr(Collection<Node> nodes, Collection<Edge> edges) throws IOException{
		logger.info("generating graphml String ...");
			forXMLStr = true;
			//Document doc = createDocument(nodes, edges);
			String a= createDocument(nodes, edges).asXML();
			logger.info("*****xmlStr: " + a);
			xmlStr  = a.replace("\"", "~");
		}		
		
	public Document createDocument(Collection<Node> nodes, Collection<Edge> edges) {
		if (!importable) shortNameToQuestionMap = Question.getShortNameToQuestionMap();
		
		Document document = DocumentHelper.createDocument();
		Element graphml = document.addElement("graphml");
		
		addNodeKeys(graphml);
		addEdgeKeys(graphml);
		
		Element graph = graphml.addElement("graph");
		graph.addAttribute("edgedefault", "directed");	
		graph.addAttribute("parse.nodeids", "canonical");
		graph.addAttribute("parse.order", "nodesfirst");
		
		addNodes(nodes, graph);
		
		addEdges(edges, graph);
		
		return document;
	}

	public String getXMLStr(){
		return xmlStr; 
	}
	private void addNodeKeys(Element graphml){
		logger.info("declaring node keys...");
		String key; 			// key id	
		List<String> names; 	// names of attributes
		
		// node properties
		String[] nodeProps = {"username","password","firstName","lastName",
							"midName","addr1","addr2","city","state","country",
							"zipcode","email","phone","cell","fax","department",
							"organization","unit","enabled","label","type","uri"};
		for (int i = 0; i < nodeProps.length; i++){
			key = NODE_PROP_PREFIX + i;
			String np = nodeProps[i];
			npKeyMap.put(np, key);	
			if (np.equals("enabled")) addKey(graphml, key, "node", np, "boolean");
			else addKey(graphml, key, "node", np, "string");
		}
		logger.debug(nodeProps.length + " node properties declared.");
		
		// node attributes
		names = nodeDao.getAttributeNames(null);
		for (int i = 0; i < names.size(); i++){
			key = NODE_ATTR_PREFIX + i;
			String na = names.get(i);			
			String attributeLabel = na;
			if (!importable) attributeLabel = Question.getKeyLabel(shortNameToQuestionMap, na);
			if (attributeLabel == null) continue;
			naKeyMap.put(na, key);
			addKey(graphml, key, "node", attributeLabel, "string");
		}
		logger.debug(names.size() + " node attributes declared.");
		
		// node long attributes
		names = nodeDao.getLongAttributeNames();
		for (int i = 0; i < names.size(); i++){
			key = NODE_LONG_ATTR_PREFIX + i;
			String nla = names.get(i);
			
			String attributeLabel = nla;
			if (!importable) attributeLabel = Question.getKeyLabel(shortNameToQuestionMap, nla);
			if (attributeLabel == null) continue;
			nlaKeyMap.put(nla, key);
			addKey(graphml, key, "node", attributeLabel, "string");
		}
		logger.debug(names.size() + " node long attributes declared.");
		
		// group
		addKey(graphml, "group", "node", "group", "string");
		logger.debug("group key declared.");	
		
		// role
		addKey(graphml, "role", "node", "role", "string");
		logger.debug("role key declared.");
		
		logger.info("all node keys declared.");
	}
	
	private void addEdgeKeys(Element graphml){
		logger.info("declaring edge keys...");
		String key;
		List<String> names;
		int count = 0;
		
		// edge properties		
		key = EDGE_PROP_PREFIX + count++;
		epKeyMap.put("creator", key);
		addKey(graphml, key, "edge", "creator", "string");
		logger.debug("creator key declared.");
		
		key = EDGE_PROP_PREFIX + count++;
		epKeyMap.put("type", key);
		addKey(graphml, key, "edge", "type", "string");
		logger.debug("type key declared.");
		
		key = EDGE_PROP_PREFIX + count++;
		epKeyMap.put("weight", key);
		addKey(graphml, key, "edge", "weight", "float");
		logger.debug("weight key declared.");
		
		// edge attributes
		names = edgeDao.getAttributeNames();
		for (int i = 0; i < names.size(); i++){
			key = EDGE_ATTR_PREFIX + i;
			String ea = names.get(i);
			eaKeyMap.put(ea, key);
			addKey(graphml, key, "edge", ea, "string");
		}
		logger.debug(names.size() + " edge attributes declared.");
		
		// edge long attributes
		names = edgeDao.getLongAttributeNames();
		for (int i = 0; i < names.size(); i++){
			key = EDGE_LONG_ATTR_PREFIX + i;
			String ela = names.get(i);
			elaKeyMap.put(ela, key);
			addKey(graphml, key, "edge", ela, "string");
		}
		logger.debug(names.size() + " edge long attributes declared.");
		
		logger.info("all edge keys declared.");
	}
	
	private void addKey(Element graphml, String id, String domain, String attrName, String attrType){
		Element keyElement = graphml.addElement("key");
		keyElement.addAttribute("id", id);
		keyElement.addAttribute("for", domain);
		keyElement.addAttribute("attr.name", attrName);
		keyElement.addAttribute("attr.type", attrType);
	}
	
	
	private void addNodes(Collection<Node> nodes, Element graph) {
		logger.info("adding nodes...");
		int total = 0;
		for (Node node : nodes){
			Element nodeElement = graph.addElement("node");
			String id = "n" + total++;
			if(forXMLStr){
				id= node.getId()+ "";
			}
			nodeElement.addAttribute("id", id);
			nodeToIdMap.put(node, id);
			
			addData(nodeElement, npKeyMap.get("username"), node.getUsername());
			addData(nodeElement, npKeyMap.get("password"), node.getPassword());
			addData(nodeElement, npKeyMap.get("firstName"), node.getFirstName());
			addData(nodeElement, npKeyMap.get("lastName"), node.getLastName());
			addData(nodeElement, npKeyMap.get("midName"), node.getMidName());
			addData(nodeElement, npKeyMap.get("addr1"), node.getAddr1());
			addData(nodeElement, npKeyMap.get("addr2"), node.getAddr2());
			addData(nodeElement, npKeyMap.get("city"), node.getCity());
			addData(nodeElement, npKeyMap.get("state"), node.getState());
			addData(nodeElement, npKeyMap.get("country"), node.getCountry());
			addData(nodeElement, npKeyMap.get("zipcode"), node.getZipcode());
			addData(nodeElement, npKeyMap.get("email"), node.getEmail());
			addData(nodeElement, npKeyMap.get("phone"), node.getPhone());
			addData(nodeElement, npKeyMap.get("cell"), node.getCell());
			addData(nodeElement, npKeyMap.get("fax"), node.getFax());
			addData(nodeElement, npKeyMap.get("unit"), node.getUnit());
			addData(nodeElement, npKeyMap.get("organization"), node.getOrganization());
			addData(nodeElement, npKeyMap.get("department"), node.getDepartment());
			addData(nodeElement, npKeyMap.get("enabled"), node.getEnabled().toString());
			addData(nodeElement, npKeyMap.get("label"), node.getLabel());
			addData(nodeElement, npKeyMap.get("type"), node.getType());
			addData(nodeElement, npKeyMap.get("uri"), node.getUri());
			
			for (String attrKey : node.getAttributes().keySet()){
				if (naKeyMap.get(attrKey) == null) continue;
				String attributeValueLabel = node.getAttribute(attrKey);
				if (!importable) attributeValueLabel = Question.getValueLabel(shortNameToQuestionMap, attributeValueLabel);
				if (attributeValueLabel == null) continue;
				addData(nodeElement, naKeyMap.get(attrKey), attributeValueLabel);
			}
			
			for (String attrKey : node.getLongAttributes().keySet()){
				if (naKeyMap.get(attrKey) == null) continue;
				String attributeValueLabel = node.getLongAttribute(attrKey);
				if (!importable) attributeValueLabel = Question.getValueLabel(shortNameToQuestionMap, attributeValueLabel);
				if (attributeValueLabel == null) continue;
				addData(nodeElement, nlaKeyMap.get(attrKey), attributeValueLabel);
			}
			

			addData(nodeElement, "group", getGroupNames(node));

			addData(nodeElement, "role", getRoleNames(node));
		}
		
		logger.info(nodes.size() + " nodes added.");
	}

	private String getRoleNames(Node node) {
		String roleNames = "";
		int count = 0;
		for (Role role : node.getRoles()){
			if (count > 0) roleNames += Constants.SEPERATOR;
			roleNames += role.getName();
			count++;
		}
		return roleNames;
	}

	private String getGroupNames(Node node) {
		String groupNames = "";
		int count = 0;
		for (Group group : node.getGroups()){
			if (count > 0) groupNames += Constants.SEPERATOR;
			groupNames += group.getName();
			count++;
		}
		return groupNames;
	}
	
	private void addEdges(Collection<Edge> edges, Element graph) {
		logger.info("adding edges...");
		
		for (Edge edge : edges){
			Element edgeElement = graph.addElement("edge");
			if(forXMLStr){
			edgeElement.addAttribute("source", edge.getFromNode().getId()+"");
				edgeElement.addAttribute("target", edge.getToNode().getId()+"");
			}else{
				edgeElement.addAttribute("source", nodeToIdMap.get(edge.getFromNode()));
				edgeElement.addAttribute("target", nodeToIdMap.get(edge.getToNode()));
			}
			edgeElement.addAttribute("directed", edge.isDirected()?"true":"false");
			
			addData(edgeElement, epKeyMap.get("creator"), nodeToIdMap.get(edge.getCreator()));
			addData(edgeElement, epKeyMap.get("type"), edge.getType());
			addData(edgeElement, epKeyMap.get("weight"), edge.getWeight().toString());
			
			for (String attrKey : edge.getAttributes().keySet()){
				if (naKeyMap.get(attrKey) == null) continue;
				String attributeValueLabel = edge.getAttribute(attrKey);
				if (!importable) attributeValueLabel = Question.getValueLabel(shortNameToQuestionMap, attributeValueLabel);
				if (attributeValueLabel == null) continue;
				addData(edgeElement, eaKeyMap.get(attrKey), attributeValueLabel);
			}
			
			/*
			for (String attrKey : edge.getLongAttributes().keySet()){
				if (naKeyMap.get(attrKey) == null) continue;
				String attributeValueLabel = edge.getLongAttribute(attrKey);
				if (!importable) attributeValueLabel = Question.getValueLabel(shortNameToQuestionMap, attributeValueLabel);
				if (attributeValueLabel == null) continue;
				addData(edgeElement, elaKeyMap.get(attrKey), attributeValueLabel);
			}
			*/
		}
		
		logger.info(edges.size() + " edges added.");
	}
	
	
	private void addData(Element parent, String key, String attrValue){
		if (attrValue == null || attrValue.length() == 0) return;
		parent.addElement("data").addAttribute("key", key).addText(attrValue);
	}
}

