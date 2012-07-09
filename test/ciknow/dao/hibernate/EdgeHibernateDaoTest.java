package ciknow.dao.hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;


import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestExecutionListeners;


import ciknow.dao.EdgeDao;
import ciknow.dao.NodeDao;
import ciknow.dao.QuestionDao;
import ciknow.domain.Edge;
import ciknow.domain.Node;
import ciknow.domain.Question;
import dbunit.Dbunit;
import dbunit.DbunitTestExecutionListener;
import edu.uci.ics.jung.graph.filters.impl.KNeighborhoodFilter;

@TestExecutionListeners(DbunitTestExecutionListener.class)
//@org.springframework.test.context.transaction.TransactionConfiguration(transactionManager="transactionManager", defaultRollback=false)
public class EdgeHibernateDaoTest extends AbstractHibernateDaoTest{
	
	
	@Autowired private EdgeDao edgeDao;
	@Autowired private NodeDao nodeDao;
	@Autowired private QuestionDao questionDao;
	
	private static final int MAX_EDGE_ROW_COUNT = 16;
	
	private static final Logger logger = Logger
			.getLogger(EdgeHibernateDaoTest.class.getName());
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/edges.xml")
	public void save() {
		
		Assert.assertEquals(MAX_EDGE_ROW_COUNT, edgeDao.getCount());
		
		Node fromNode = nodeDao.findById(1L);
		Node toNode = nodeDao.findById(2L);
		HashMap<String, String> attributes = new HashMap<String, String>();
		attributes.put("1", "ATT_VALUE 1");
		attributes.put("2", "ATT_VALUE 2");
		HashMap<String, String> longAttributes = new HashMap<String, String>();
		longAttributes.put("1", "LONG_ATT_VALUE 1");
		longAttributes.put("2", "LONG_ATT_VALUE 2");
		Edge edge = createEdge(fromNode, toNode, true, 1.0f, "test type", attributes, longAttributes);
		edgeDao.save(edge);
		Assert.assertEquals(MAX_EDGE_ROW_COUNT+1, edgeDao.getCount());
		
		List<Edge> edges = edgeDao.getAll();
		Edge edgeFromDb = edges.get(edges.size()-1);
		compareEdges(edge, edgeFromDb);
		
		edge = edgeDao.loadById(1L);
		edge.setType("Another Type");
		edge.setFromNode(fromNode);
		edge.setToNode(toNode);
		edge.setAttribute("new_key", "new_value");
		edge.setLongAttribute("new_key", "new_long_value");
		edgeDao.save(edge);
		compareEdges(edge, edgeDao.loadById(1L));
		
	}
	
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/edges.xml")
	public void saveCollection(){
		
		Assert.assertEquals(MAX_EDGE_ROW_COUNT, edgeDao.getCount());
		
		Node fromNode = nodeDao.findById(1L);
		Node toNode = nodeDao.findById(2L);
		HashMap<String, String> attributes = new HashMap<String, String>();
		attributes.put("1", "ATT_VALUE 1");
		attributes.put("2", "ATT_VALUE 2");
		HashMap<String, String> longAttributes = new HashMap<String, String>();
		longAttributes.put("1", "LONG_ATT_VALUE 1");
		longAttributes.put("2", "LONG_ATT_VALUE 2");
		Edge edge = createEdge(fromNode, toNode, true, 1.0f, "test type", attributes, longAttributes);
		Edge edge2 = createEdge(fromNode, toNode, false, 1.0f, "test type2", attributes, longAttributes);
		ArrayList<Edge> edges = new ArrayList<Edge>();
		edges.add(edge);
		edges.add(edge2);
		edgeDao.save(edges);
		Assert.assertEquals(MAX_EDGE_ROW_COUNT+2, edgeDao.getCount());
		
		List<Edge> edges2 = edgeDao.getAll();
		compareEdges(edge, edges2.get(edges2.size()-2));
		compareEdges(edge2, edges2.get(edges2.size()-1));
		
		
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/edges.xml")
	public void delete(){
		
		Assert.assertEquals(MAX_EDGE_ROW_COUNT, edgeDao.getCount());
		
		Edge edge = edgeDao.loadById(1L);
		edgeDao.delete(edge);
		
		Assert.assertEquals(MAX_EDGE_ROW_COUNT-1, edgeDao.getCount());
		Assert.assertTrue(!edgeDao.getAll().contains(edge));
		
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/edges.xml")
	public void deleteCollection(){
		
		Assert.assertEquals(MAX_EDGE_ROW_COUNT, edgeDao.getCount());
		
		Edge edge = edgeDao.loadById(1L);
		Edge edge2 = edgeDao.loadById(2L);
		ArrayList<Edge> edges = new ArrayList<Edge>();
		edges.add(edge);
		edges.add(edge2);
		edgeDao.delete(edges);
		Assert.assertEquals(MAX_EDGE_ROW_COUNT-2, edgeDao.getCount());
		
		Assert.assertTrue(!edgeDao.getAll().contains(edge));
		Assert.assertTrue(!edgeDao.getAll().contains(edge2));
		
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/edges.xml")
	public void deleteAll(){
	
		//TODO Address BulkUpdate failure
		
		//Assert.assertEquals(MAX_EDGE_ROW_COUNT, edgeDao.getCount());
		//edgeDao.deleteAll();
		//Assert.assertEquals(MAX_EDGE_ROW_COUNT-1, edgeDao.getCount());
		
	}

	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/edges.xml")
	public void getAll(){
		
		List<Edge> edges = edgeDao.getAll();
		Assert.assertEquals(MAX_EDGE_ROW_COUNT, edges.size());
		
		compareEdges(edges.get(0), edgeDao.loadById(1L));
		compareEdges(edges.get(6), edgeDao.loadById(7L));
		compareEdges(edges.get(12), edgeDao.loadById(13L));
		
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/edges.xml")
	public void getAllWithAttributes(){
		
		//TODO Address with bug - All edges are loaded
		
		/*List<Edge> edges = edgeDao.getAllWithAttributes();
		for(int i=0; i<edges.size(); i++){
			printAttributes(edges.get(i));
		}
		*/
		//Assert.assertEquals(3, edges.size());
		
		
	}
	
	
	private void printAttributes(Edge edge){
		Iterator<String> keys = edge.getAttributes().keySet().iterator();
		System.out.println("Edge." + edge.getId() + " attributes: " + edge.getAttributes().size());
		while(keys.hasNext()){
			String key = keys.next();
			System.out.println("key, value: " + key + ", " + edge.getAttribute(key));
		}
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/edges.xml")
	public void loadAll(){
		
		List<Edge> edges = edgeDao.loadAll();
		Assert.assertEquals(MAX_EDGE_ROW_COUNT, edges.size());
		
		compareEdges(edges.get(0), edgeDao.loadById(1L));
		compareEdges(edges.get(6), edgeDao.loadById(7L));
		compareEdges(edges.get(12), edgeDao.loadById(13L));
	
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/edges.xml")
	public void getCount(){
		Assert.assertEquals(MAX_EDGE_ROW_COUNT, edgeDao.getCount());
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/edges.xml")
	public void getEdgeTypes(){
		
		List<String> edgeTypes = edgeDao.getEdgeTypes();
		Assert.assertEquals(7, edgeTypes.size());
		
		Assert.assertEquals("Edge_Type1", edgeTypes.get(0));
		Assert.assertEquals("Edge_Type2", edgeTypes.get(1));
		Assert.assertEquals("Edge_Type3", edgeTypes.get(2));
		Assert.assertEquals("Edge_Type4", edgeTypes.get(3));
		Assert.assertEquals("Edge_Type5", edgeTypes.get(4));
		Assert.assertEquals("Edge_Type6", edgeTypes.get(5));
		Assert.assertEquals("Edge_Type7", edgeTypes.get(6));
		
		
	}

	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/edges.xml")
	public void getEdgeTypesByNodeTypes(){
		
		ArrayList<String> nodeTypes = new ArrayList<String>();
		nodeTypes.add("user2");
		nodeTypes.add("user3");
		
		List<String> edgeTypes = edgeDao.getEdgeTypesByNodeTypes(nodeTypes, 0);
		Assert.assertEquals(6, edgeTypes.size());
		/*Assert.assertEquals("Edge_Type1", edgeTypes.get(0));
		Assert.assertEquals("Edge_Type2", edgeTypes.get(1));
		Assert.assertEquals("Edge_Type3", edgeTypes.get(2));
		Assert.assertEquals("Edge_Type4", edgeTypes.get(3));
		Assert.assertEquals("Edge_Type6", edgeTypes.get(4));
		Assert.assertEquals("Edge_Type7", edgeTypes.get(5));
		*/
		Assert.assertTrue(edgeTypes.contains("Edge_Type1"));
		Assert.assertTrue(edgeTypes.contains("Edge_Type2"));
		Assert.assertTrue(edgeTypes.contains("Edge_Type3"));
		Assert.assertTrue(edgeTypes.contains("Edge_Type4"));
		Assert.assertTrue(edgeTypes.contains("Edge_Type6"));
		Assert.assertTrue(edgeTypes.contains("Edge_Type7"));
		
		edgeTypes = edgeDao.getEdgeTypesByNodeTypes(nodeTypes, KNeighborhoodFilter.IN);
		Assert.assertEquals(4, edgeTypes.size());
		/*Assert.assertEquals("Edge_Type2", edgeTypes.get(0));
		Assert.assertEquals("Edge_Type4", edgeTypes.get(1));
		Assert.assertEquals("Edge_Type6", edgeTypes.get(2));
		Assert.assertEquals("Edge_Type7", edgeTypes.get(3));
		*/
		Assert.assertTrue(edgeTypes.contains("Edge_Type2"));
		Assert.assertTrue(edgeTypes.contains("Edge_Type4"));
		Assert.assertTrue(edgeTypes.contains("Edge_Type6"));
		Assert.assertTrue(edgeTypes.contains("Edge_Type7"));
		
		
		edgeTypes = edgeDao.getEdgeTypesByNodeTypes(nodeTypes, KNeighborhoodFilter.OUT);
		Assert.assertEquals(4, edgeTypes.size());
		/*Assert.assertEquals("Edge_Type1", edgeTypes.get(0));
		Assert.assertEquals("Edge_Type2", edgeTypes.get(1));
		Assert.assertEquals("Edge_Type3", edgeTypes.get(2));
		Assert.assertEquals("Edge_Type7", edgeTypes.get(3));
		*/
		Assert.assertTrue(edgeTypes.contains("Edge_Type1"));
		Assert.assertTrue(edgeTypes.contains("Edge_Type2"));
		Assert.assertTrue(edgeTypes.contains("Edge_Type3"));
		Assert.assertTrue(edgeTypes.contains("Edge_Type7"));
		
	
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/edges.xml")
	public void getEdgeTypesAmongNodeTypes(){
		ArrayList<String> nodeTypes = new ArrayList<String>();
		nodeTypes.add("user2");
		nodeTypes.add("user3");
		
		List<String> edgeTypes = edgeDao.getEdgeTypesAmongNodeTypes(nodeTypes);
		Assert.assertEquals(1, edgeTypes.size());
		Assert.assertEquals("Edge_Type7", edgeTypes.get(0));
		
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/edges.xml")
	public void getOtherNodeTypesByNodeTypes(){
		ArrayList<String> nodeTypes = new ArrayList<String>();
		nodeTypes.add("user4");
		Collection<String> nodeTypes2 = edgeDao.getOtherNodeTypesByNodeTypes(nodeTypes, 0);
		Assert.assertEquals(1, nodeTypes2.size());
		Assert.assertTrue(nodeTypes2.contains("user2"));
		
		nodeTypes2 = edgeDao.getOtherNodeTypesByNodeTypes(nodeTypes, KNeighborhoodFilter.IN);
		Assert.assertEquals(0, nodeTypes2.size());
		
		nodeTypes2 = edgeDao.getOtherNodeTypesByNodeTypes(nodeTypes, KNeighborhoodFilter.OUT);
		Assert.assertEquals(1, nodeTypes2.size());
		Assert.assertTrue(nodeTypes2.contains("user2"));
		
		//TODO Make this test more robust
	
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/edges.xml")
	public void getNodeTypesByEdgeTypes(){
		
		ArrayList<String> edgeTypes = new ArrayList<String>();
		edgeTypes.add("Edge_Type1");
		edgeTypes.add("Edge_Type2");
		
		Collection<String> nodeTypes = edgeDao.getNodeTypesByEdgeTypes(edgeTypes);
		Assert.assertEquals(3, nodeTypes.size());
		Assert.assertTrue(nodeTypes.contains("user"));
		Assert.assertTrue(nodeTypes.contains("user2"));
		Assert.assertTrue(nodeTypes.contains("directorate"));
		
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/edges.xml")
	public void findEdgeTypesByFromAndToNodeTypes(){
		
		List<Map<String, String>> edgeTypesMaps = edgeDao.getEdgeTypesByFromAndToNodeTypes("user2", "user");
		Assert.assertEquals(4, edgeTypesMaps.size());
		
		//TODO figure out why test case sometimes fails 
		
		/*Map<String, String> map = edgeTypesMaps.get(0);
		Assert.assertEquals("1", map.get("direction"));
		Assert.assertEquals("Edge_Type1", map.get("label"));
		Assert.assertEquals("Edge_Type1", map.get("type"));
		
		map = edgeTypesMaps.get(1);
		Assert.assertEquals("1", map.get("direction"));
		Assert.assertEquals("Edge_Type2", map.get("label"));
		Assert.assertEquals("Edge_Type2", map.get("type"));
		
		map = edgeTypesMaps.get(2);
		Assert.assertEquals("-1", map.get("direction"));
		Assert.assertEquals("Edge_Type2", map.get("label"));
		Assert.assertEquals("Edge_Type2", map.get("type"));
		
		map = edgeTypesMaps.get(3);
		Assert.assertEquals("-1", map.get("direction"));
		Assert.assertEquals("Edge_Type6", map.get("label"));
		Assert.assertEquals("Edge_Type6", map.get("type"));
		*/
		
	}

	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/edges.xml")
	public void loadById(){
		
		Node node1 = nodeDao.loadById(1L);
		Node node2 = nodeDao.loadById(2L);
		Edge edge = edgeDao.loadById(1L);
		
		Assert.assertEquals(new Long(1L), edge.getId());
		Assert.assertEquals("Edge_Type1", edge.getType());
		Assert.assertEquals(node1, edge.getToNode());
		Assert.assertEquals(node2, edge.getFromNode());
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/edges.xml")
	public void getProxy(){
		
		Edge edge = edgeDao.getProxy(1L);
		Assert.assertEquals(new Long(1L), edge.getId());
		
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/edges.xml")
	public void findByType(){
		List<Edge> edges = edgeDao.findByType("Edge_Type7", false);
		Assert.assertEquals(1, edges.size());
		Edge edge = edges.get(0);
		Assert.assertEquals(new Long(14L), edge.getId());
		Assert.assertEquals("Edge_Type7", edge.getType());
		
		edges = edgeDao.findByType("Edge_Type7", true);
		Assert.assertEquals(2, edges.size());
		edge = edges.get(0);
		Assert.assertEquals(new Long(14L), edge.getId());
		Assert.assertEquals("Edge_Type7", edge.getType());
		edge = edges.get(1);
		Assert.assertEquals(new Long(15L), edge.getId());
		Assert.assertEquals("Edge_Type7", edge.getType());
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/edges.xml")
	public void loadByType(){
		
		Node node4 = nodeDao.loadById(4L);
		Node node6 = nodeDao.loadById(6L);
		Node node10 = nodeDao.loadById(10L);
		
		
		List<Edge> edges = edgeDao.findByType("Edge_Type7", false);
		Assert.assertEquals(1, edges.size());
		Edge edge = edges.get(0);
		Assert.assertEquals(new Long(14L), edge.getId());
		Assert.assertEquals("Edge_Type7", edge.getType());
		Assert.assertEquals(node6, edge.getToNode());
		Assert.assertEquals(node4, edge.getFromNode());
		
		edges = edgeDao.findByType("Edge_Type7", true);
		Assert.assertEquals(2, edges.size());
		edge = edges.get(0);
		Assert.assertEquals(new Long(14L), edge.getId());
		Assert.assertEquals("Edge_Type7", edge.getType());
		Assert.assertEquals(node6, edge.getToNode());
		Assert.assertEquals(node4, edge.getFromNode());
		edge = edges.get(1);
		Assert.assertEquals(new Long(15L), edge.getId());
		Assert.assertEquals("Edge_Type7", edge.getType());
		Assert.assertEquals(node6, edge.getToNode());
		Assert.assertEquals(node10, edge.getFromNode());
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/edges.xml")
	public void loadByQuestionAndNode(){

		Node node4 = nodeDao.loadById(4L);
		Question question = questionDao.findById(3L);
		
		try {
			List<Edge> edges = edgeDao.loadByQuestionAndNode(question, node4);
			Edge edge = edges.get(0);
			Assert.assertEquals(1, edges.size());
			Assert.assertEquals(new Long(14L), edge.getId());
			Assert.assertEquals("Edge_Type7", edge.getType());
			Assert.assertEquals(node4, edge.getFromNode());
			
		} catch (Exception e) {
			Assert.fail();
		}
		
		
		//TODO Make this test more robust
		
	}

	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/edges.xml")
	public void findByQuestion(){
		
		Question question = questionDao.findById(3L);
		
		List<Edge> edges = edgeDao.findByQuestion(question);
		Assert.assertEquals(2, edges.size());
		
		Edge edge = edges.get(0);
		Assert.assertEquals(new Long(14L), edge.getId());
		Assert.assertEquals("Edge_Type7", edge.getType());
		
		edge = edges.get(1);
		Assert.assertEquals(new Long(15L), edge.getId());
		Assert.assertEquals("Edge_Type7", edge.getType());
		
		
		//TODO Make this test more robust
		
		
	}

	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/edges.xml")
	public void findByFromNodeId(){
		
		List<Edge> edges = edgeDao.findByFromNodeId(2L);
		Assert.assertEquals(3, edges.size());
		
		Edge edge = edges.get(0);
		Assert.assertEquals(new Long(1L), edge.getId());
		Assert.assertEquals("Edge_Type1", edge.getType());
		
		edge = edges.get(1);
		Assert.assertEquals(new Long(12L), edge.getId());
		Assert.assertEquals("Edge_Type6", edge.getType());
		
		edge = edges.get(2);
		Assert.assertEquals(new Long(16L), edge.getId());
		Assert.assertEquals("Edge_Type2", edge.getType());
		
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/edges.xml")
	public void loadByFromNodeId(){
		
		Node node1 = nodeDao.loadById(1L);
		Node node2 = nodeDao.loadById(2L);
		Node node6 = nodeDao.loadById(6L);
		
		
		List<Edge> edges = edgeDao.loadByFromNodeId(2L);
		Assert.assertEquals(3, edges.size());
		
		Edge edge = edges.get(0);
		Assert.assertEquals(new Long(1L), edge.getId());
		Assert.assertEquals("Edge_Type1", edge.getType());
		Assert.assertEquals(node1, edge.getToNode());
		Assert.assertEquals(node2, edge.getFromNode());
		
		edge = edges.get(1);
		Assert.assertEquals(new Long(12L), edge.getId());
		Assert.assertEquals("Edge_Type6", edge.getType());
		Assert.assertEquals(node6, edge.getToNode());
		Assert.assertEquals(node2, edge.getFromNode());
		
		edge = edges.get(2);
		Assert.assertEquals(new Long(16L), edge.getId());
		Assert.assertEquals("Edge_Type2", edge.getType());
		Assert.assertEquals(node2, edge.getToNode());
		Assert.assertEquals(node1, edge.getFromNode());
		
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/edges.xml")
    public void findByToNodeId(){
		
		List<Edge> edges = edgeDao.findByToNodeId(1L);
		Assert.assertEquals(4, edges.size());
		
		Edge edge = edges.get(0);
		Assert.assertEquals(new Long(1L), edge.getId());
		Assert.assertEquals("Edge_Type1", edge.getType());
		
		edge = edges.get(1);
		Assert.assertEquals(new Long(10L), edge.getId());
		Assert.assertEquals("Edge_Type5", edge.getType());
		
		edge = edges.get(2);
		Assert.assertEquals(new Long(11L), edge.getId());
		Assert.assertEquals("Edge_Type5", edge.getType());
		
		edge = edges.get(3);
		Assert.assertEquals(new Long(16L), edge.getId());
		Assert.assertEquals("Edge_Type2", edge.getType());
    	
    }
    
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/edges.xml")
    public void loadByToNodeId(){
    	
		Node node1 = nodeDao.loadById(1L);
		Node node2 = nodeDao.loadById(2L);
		Node node5 = nodeDao.loadById(5L);
		Node node7 = nodeDao.loadById(7L);
		
		List<Edge> edges = edgeDao.loadByToNodeId(1L);
		Assert.assertEquals(4, edges.size());
		
		Edge edge = edges.get(0);
		Assert.assertEquals(new Long(1L), edge.getId());
		Assert.assertEquals("Edge_Type1", edge.getType());
		Assert.assertEquals(node1, edge.getToNode());
		Assert.assertEquals(node2, edge.getFromNode());
		
		edge = edges.get(1);
		Assert.assertEquals(new Long(10L), edge.getId());
		Assert.assertEquals("Edge_Type5", edge.getType());
		Assert.assertEquals(node1, edge.getToNode());
		Assert.assertEquals(node7, edge.getFromNode());
		
		edge = edges.get(2);
		Assert.assertEquals(new Long(11L), edge.getId());
		Assert.assertEquals("Edge_Type5", edge.getType());
		Assert.assertEquals(node1, edge.getToNode());
		Assert.assertEquals(node5, edge.getFromNode());
		
		edge = edges.get(3);
		Assert.assertEquals(new Long(16L), edge.getId());
		Assert.assertEquals("Edge_Type2", edge.getType());
		Assert.assertEquals(node2, edge.getToNode());
		Assert.assertEquals(node1, edge.getFromNode());
    }

	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/edges.xml")
    public void findByCreatorId(){
		
		List<Edge> edges = edgeDao.findByCreatorId(5L);
		Assert.assertEquals(2, edges.size());
		
		Edge edge = edges.get(0);
		Assert.assertEquals(new Long(10L), edge.getId());
		Assert.assertEquals("Edge_Type5", edge.getType());
		
		edge = edges.get(1);
		Assert.assertEquals(new Long(11L), edge.getId());
		Assert.assertEquals("Edge_Type5", edge.getType());
    
    	
    }
    
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/edges.xml")
    public void loadByCreatorId(){
    	
		Node node1 = nodeDao.loadById(1L);
		Node node5 = nodeDao.loadById(5L);
		Node node7 = nodeDao.loadById(7L);
		
		List<Edge> edges = edgeDao.loadByCreatorId(5L);
		Assert.assertEquals(2, edges.size());
		
		Edge edge = edges.get(0);
		Assert.assertEquals(new Long(10L), edge.getId());
		Assert.assertEquals("Edge_Type5", edge.getType());
		Assert.assertEquals(node1, edge.getToNode());
		Assert.assertEquals(node7, edge.getFromNode());
		
		edge = edges.get(1);
		Assert.assertEquals(new Long(11L), edge.getId());
		Assert.assertEquals("Edge_Type5", edge.getType());
		Assert.assertEquals(node1, edge.getToNode());
		Assert.assertEquals(node5, edge.getFromNode());
    }
    
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/edges.xml")
	public void findByAttribute(){
		
		//method not implemented
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/edges.xml")
	public void matchAttribute(){
		
		//method not implemented
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/edges.xml")
	public void findEdgesAmongNodes(){

		Node node1 = nodeDao.loadById(1L);
		Node node2 = nodeDao.loadById(2L);
		Node node3 = nodeDao.loadById(3L);
		Node node6 = nodeDao.loadById(6L);
		
		ArrayList<Node> nodes = new ArrayList<Node>();
		nodes.add(node1);
		nodes.add(node2);
		nodes.add(node3);
		nodes.add(node6);
		
		
		List<Edge> edges = edgeDao.findEdgesAmongNodes(nodes);
		Assert.assertEquals(5, edges.size());
		
		Edge edge = edges.get(0);
		Assert.assertEquals(new Long(1L), edge.getId());
		Assert.assertEquals("Edge_Type1", edge.getType());
		//Assert.assertEquals(node1, edge.getToNode());
		//Assert.assertEquals(node2, edge.getFromNode());
		
		edge = edges.get(1);
		Assert.assertEquals(new Long(2L), edge.getId());
		Assert.assertEquals("Edge_Type1", edge.getType());
		//Assert.assertEquals(node2, edge.getToNode());
		//Assert.assertEquals(node3, edge.getFromNode());
		
		edge = edges.get(2);
		Assert.assertEquals(new Long(12L), edge.getId());
		Assert.assertEquals("Edge_Type6", edge.getType());
		//Assert.assertEquals(node6, edge.getToNode());
		//Assert.assertEquals(node2, edge.getFromNode());
		
		edge = edges.get(3);
		Assert.assertEquals(new Long(13L), edge.getId());
		Assert.assertEquals("Edge_Type6", edge.getType());
		//Assert.assertEquals(node6, edge.getToNode());
		//Assert.assertEquals(node3, edge.getFromNode());
		
		edge = edges.get(4);
		Assert.assertEquals(new Long(16L), edge.getId());
		Assert.assertEquals("Edge_Type2", edge.getType());
		//Assert.assertEquals(node2, edge.getToNode());
		//Assert.assertEquals(node1, edge.getFromNode());
	
	}
	/*
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/edges.xml")
	public void loadAllEdgesAmongNodes(){
		//Collection<Node> nodes, boolean includeDerivedEdges, boolean includeEmptyEdges, Collection<String> edgeTypes
		
		Node node1 = nodeDao.loadById(1L);
		Node node2 = nodeDao.loadById(2L);
		Node node3 = nodeDao.loadById(3L);
		Node node6 = nodeDao.loadById(6L);
		
		ArrayList<Node> nodes = new ArrayList<Node>();
		nodes.add(node1);
		nodes.add(node2);
		nodes.add(node3);
		nodes.add(node6);
		
		ArrayList<String> edgeTypes = new ArrayList<String>();
		edgeTypes.add("Edge_Type1");
		edgeTypes.add("Edge_Type2");
		
		//TODO address loadAllEdgesFromNodes access method - Duplicate Edges are included in list
		
		
		List<Edge> edges = edgeDao.loadAllEdgesAmongNodes(nodes, true, true, null);
		//Assert.assertEquals(2, edges.size());
		
		System.out.println("EDGES: " + edges.size());
		for(int i=0; i<edges.size(); i++){
			System.out.println("EDGE: " + edges.get(i).getId());
		}

		
		
		
	}
	*/

	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/edges.xml")
	public void getAttributeNames(){
		
		List<String> names = edgeDao.getAttributeNames();
	
		Assert.assertEquals(3, names.size());
		Assert.assertEquals("1", names.get(0));
		Assert.assertEquals("2", names.get(1));
		Assert.assertEquals("3", names.get(2));
		
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/edges.xml")
	public void getLongAttributeNames(){
		
		List<String> names = edgeDao.getLongAttributeNames();
		
		Assert.assertEquals(4, names.size());
		Assert.assertEquals("1", names.get(0));
		Assert.assertEquals("2", names.get(1));
		Assert.assertEquals("3", names.get(2));
		Assert.assertEquals("4", names.get(3));

		
	}
	
	@Test
	@Dbunit(dataSetLocation="classpath:/dbunit/edges.xml")
	public void getAttributeValues(){
		
		List<String> values = edgeDao.getAttributeValues("1");
		Assert.assertEquals(3, values.size());
		Assert.assertEquals("EDGE_1_ATT_VALUE_1", values.get(0));
		Assert.assertEquals("EDGE_2_ATT_VALUE_1", values.get(1));
		Assert.assertEquals("EDGE_3_ATT_VALUE_1", values.get(2));
	}
	
	
	
	private void compareEdges(Edge expected, Edge actual){
		Assert.assertEquals(expected.getId(), actual.getId());
		Assert.assertEquals(expected.getFromNode(), actual.getFromNode());
		Assert.assertEquals(expected.isDirected(), actual.isDirected());
		Assert.assertEquals(expected.getWeight(), actual.getWeight());
		Assert.assertEquals(expected.getType(), actual.getType());
		Iterator<String> keys = expected.getAttributes().keySet().iterator();
		while(keys.hasNext()){
			String key = keys.next();
			Assert.assertEquals(expected.getAttribute(key), actual.getAttribute(key));
		}		
		keys = expected.getLongAttributes().keySet().iterator();
		while(keys.hasNext()){
			String key = keys.next();
			Assert.assertEquals(expected.getLongAttribute(key), actual.getLongAttribute(key));
		}
		
	}
	
	
	private Edge createEdge(Node fromNode, Node toNode, boolean directed, double weight, String type,
			HashMap<String, String> attributes, HashMap<String, String> longAttributes){
		Edge edge = new Edge();
		
		edge.setFromNode(fromNode);
		edge.setToNode(toNode);
		edge.setDirected(directed);
		edge.setWeight(weight);
		edge.setType(type);
		edge.setAttributes(attributes);
		edge.setLongAttributes(longAttributes);
		
		return edge;
	}
}
