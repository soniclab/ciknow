package ciknow.graph.metrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ciknow.domain.Edge;
import ciknow.domain.Node;
import ciknow.util.Constants;

public class NetworkGenerator {
	
	public static Map createStarGraph(boolean directed){
		Map m = new HashMap();
		List<Node> nodes = new ArrayList<Node>();
		List<Edge> edges = new ArrayList<Edge>();
		Node node;
		Edge edge;
		
		for (Long i=1L; i<=7; i++){
			nodes.add(createNode(i, Integer.toString(i.intValue()), Constants.NODE_TYPE_USER));
		}
		
		for (Long i=2L; i<=7; i++){
			edges.add(createEdge(i, nodes.get(0), nodes.get(i.intValue() -1), "test", 1f, directed));
		}
				
		m.put("nodes", nodes);
		m.put("edges", edges);
		return m;
	}
	
	public static Map createCircleGraph(boolean directed){
		Map m = new HashMap();
		List<Node> nodes = new ArrayList<Node>();
		List<Edge> edges = new ArrayList<Edge>();
		Node node;
		Edge edge;
		
		for (Long i=1L; i<=7; i++){
			nodes.add(createNode(i, Integer.toString(i.intValue()), Constants.NODE_TYPE_USER));
		}
		
		for (Long i=1L; i<=7; i++){
			edges.add(createEdge(i, nodes.get(i.intValue()-1), nodes.get(i.intValue()%7), "test", 1f, directed));
		}
				
		m.put("nodes", nodes);
		m.put("edges", edges);
		return m;
	}
	
	public static Map createLineGraph(boolean directed){
		Map m = new HashMap();
		List<Node> nodes = new ArrayList<Node>();
		List<Edge> edges = new ArrayList<Edge>();
		Node node;
		Edge edge;
		
		for (Long i=1L; i<=7; i++){
			nodes.add(createNode(i, Integer.toString(i.intValue()), Constants.NODE_TYPE_USER));
		}
		
		for (Long i=1L; i<7; i++){
			edges.add(createEdge(i, nodes.get(i.intValue()-1), nodes.get(i.intValue()), "test", 1f, directed));
		}
				
		m.put("nodes", nodes);
		m.put("edges", edges);
		return m;
	}
	
	private static Node createNode(Long id, String label, String type){
		Node node = new Node();
		node.setId(id);
		node.setType(type);
		node.setLabel(label);
		return node;
	}
	
	private static Edge createEdge(Long id, Node from, Node to, String type, double weight, boolean directed){
		Edge edge = new Edge();
		edge.setId(id);
		edge.setFromNode(from);
		edge.setToNode(to);
		edge.setType(type);
		edge.setWeight(weight);
		edge.setDirected(directed);
		return edge;
	}
	
	public static Map createTestGraph(){
		List<Node> nodes = new ArrayList<Node>();
		List<Edge> edges = new ArrayList<Edge>();
		Edge edge;
		
		// A
		Node nodeA = new Node();
		nodeA.setId(100000L);
		nodeA.setLabel("A");
		nodeA.setType(Constants.NODE_TYPE_USER);
		nodes.add(nodeA);
		
		// B
		Node nodeB = new Node();
		nodeB.setId(100001L);
		nodeB.setLabel("B");
		nodeB.setType(Constants.NODE_TYPE_USER);
		nodes.add(nodeB);	
		
		// C
		Node nodeC = new Node();
		nodeC.setId(100002L);
		nodeC.setLabel("C");
		nodeC.setType("keyword");
		nodes.add(nodeC);	
		
		// D
		Node nodeD = new Node();
		nodeD.setId(100003L);
		nodeD.setLabel("D");
		nodeD.setType("doc");
		nodes.add(nodeD);	
		
		// E
		Node nodeE = new Node();
		nodeE.setId(100004L);
		nodeE.setLabel("E");
		nodeE.setType("keyword");
		nodes.add(nodeE);	
		
		// F
		Node nodeF = new Node();
		nodeF.setId(100005L);
		nodeF.setLabel("F");
		nodeF.setType("doc");
		nodes.add(nodeF);	
		
		// e1
		edge = new Edge();
		edge.setId(1L);
		edge.setFromNode(nodeA);
		edge.setToNode(nodeB);
		edge.setType("t1");
		edge.setWeight(2d);
		edge.setDirected(false);
		edges.add(edge);
		
		// e2
		edge = new Edge();
		edge.setId(2L);
		edge.setFromNode(nodeA);
		edge.setToNode(nodeC);
		edge.setType("t1");
		edge.setWeight(3d);
		edge.setDirected(false);
		edges.add(edge);
		
		// e3
		edge = new Edge();
		edge.setId(3L);
		edge.setFromNode(nodeB);
		edge.setToNode(nodeC);
		edge.setType("t2");
		edge.setWeight(3d);
		edge.setDirected(false);
		edges.add(edge);
		
		// e4
		edge = new Edge();
		edge.setId(4L);
		edge.setFromNode(nodeB);
		edge.setToNode(nodeD);
		edge.setType("t3");
		edge.setWeight(6d);
		edge.setDirected(false);
		edges.add(edge);
		
		// e5
		edge = new Edge();
		edge.setId(5L);
		edge.setFromNode(nodeC);
		edge.setToNode(nodeD);
		edge.setType("t4");
		edge.setWeight(3d);
		edge.setDirected(false);
		edges.add(edge);
		
		// e6
		edge = new Edge();
		edge.setId(6L);
		edge.setFromNode(nodeD);
		edge.setToNode(nodeE);
		edge.setType("t4");
		edge.setWeight(1d);
		edge.setDirected(false);
		edges.add(edge);
		
		// e7
		edge = new Edge();
		edge.setId(7L);
		edge.setFromNode(nodeD);
		edge.setToNode(nodeF);
		edge.setType("t4");
		edge.setWeight(3d);
		edge.setDirected(false);
		edges.add(edge);
		
		// e8
		edge = new Edge();
		edge.setId(8L);
		edge.setFromNode(nodeE);
		edge.setToNode(nodeF);
		edge.setType("t5");
		edge.setWeight(1d);
		edge.setDirected(false);
		edges.add(edge);
		
		Map m = new HashMap();
		m.put("nodes", nodes);
		m.put("edges", edges);
		return m;
	}
}
