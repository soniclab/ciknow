package ciknow.graph.converter;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import edu.uci.ics.jung.exceptions.ConstraintViolationException;
import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.graph.impl.*;
import edu.uci.ics.jung.io.GraphMLFile;
import edu.uci.ics.jung.utils.PredicateUtils;
import edu.uci.ics.jung.utils.UserData;

import org.apache.log4j.Logger;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleGraph;

import ciknow.domain.Edge;
import ciknow.domain.Node;
import ciknow.util.Constants;
import java.util.List;

/**
 * A class containing conversion functions between CIKNOW objects and Jung objects.
 * It also contains functions to import from GraphML
 * 
 * @author gyao
 * @author andydon
 *
 */
public class SparseGraphConverter {
	private static Logger logger = Logger.getLogger(SparseGraphConverter.class);
	
	

	//////////////////////// CIKNOW -> JUNG /////////////////////////////////////
	/**
	 * Converts a CIKNOW graph to Jung graph
	 * 
	 * @param g - the CIKNOW graph to be converted
	 * @param type - 1 for directed, 2 for undirected, 3 for mixed
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Graph CIKNOW2JUNG(Collection<Node> nodes, Collection<Edge> edges, int type, boolean includeAttrs){
		if (nodes == null || nodes.isEmpty()) return null;
		
		// create jung graph
		Graph jg = null;
		switch (type){
			case 1: jg = new DirectedSparseGraph();
			case 2: jg = new UndirectedSparseGraph();
			case 3: jg = new SparseGraph();
		}
		Map<Long, Vertex> nodesMap = new HashMap<Long, Vertex>();
		
		// convert nodes to vertices
		for (Node node : nodes){
			Vertex v = jg.addVertex(CIKNOW2JUNG(node, type, includeAttrs));
			if (v == null) {
				logger.warn("node(id=" + node.getId() + ") has been converted to NULL vertex!!");
				continue;
			}
			nodesMap.put(node.getId(), v);
		}
		logger.info("Converted " + nodes.size() + " Nodes to JUNG Vertex.");

		// convert edges to jung edges
		for (Edge edge : edges){			
			Node from = edge.getFromNode();
			Node to = edge.getToNode();
			
			Vertex vf = nodesMap.get(from.getId());
			Vertex vt = nodesMap.get(to.getId());	
//			logger.debug(from.getId() + ": " + vf.getUserDatum(Constants.NODE_LABEL));
//			logger.debug(to.getId() + ": " + vt.getUserDatum(Constants.NODE_LABEL));
			if (vf == null) logger.warn("node(id=" + from.getId() + ") has been converted to NULL vertex!!");
			if (vt == null) logger.warn("node(id=" + to.getId() + ") has been converted to NULL vertex!!");
			edu.uci.ics.jung.graph.Edge e = CIKNOW2JUNG(edge, vf, vt, includeAttrs);
			
			if (e != null){
				try{
					jg.addEdge(e);
				} catch(ConstraintViolationException cve){
					logger.error(PredicateUtils.evaluateNestedPredicates(cve.getViolatedConstraint(), e));
				}
			} else{
				logger.warn("null edge!?");
			}
				
		}		
		logger.info("Converted " + edges.size() + " edges to JUNG edges.");
		
		return jg;
	}
	
	
	/**
	 * Converts a CIKNOW node to a jung vertex.
	 * 
	 * @param node	the node to be converted
	 * @param type	the type of jung vertex to convert to, 1 for {@link DirectedSparseVertex}, 2 for {@link UndirectedSparseVertex}, 3 for {@link SparseVertex}
	 * 
	 * @return
	 */
	public static Vertex CIKNOW2JUNG(Node node, int type, boolean includeAttrs){
		Vertex v;
		switch(type){
            case 1: v = new DirectedSparseVertex(); break;
            case 2: v = new UndirectedSparseVertex(); break;
            case 3: v = new SparseVertex(); break;
            default: v = new SparseVertex();
        }
			
		//logger.debug("set permanent node attributes...");
		v.addUserDatum(Constants.NODE_ID, node.getId(), UserData.SHARED);
		
		Long version = node.getVersion();
		if (version != null) v.addUserDatum(Constants.NODE_VERSION, version, UserData.SHARED);
		String nodeType = node.getType();
		if (nodeType != null && nodeType.length() > 0) v.addUserDatum(Constants.NODE_TYPE, nodeType, UserData.SHARED);
		String label = node.getLabel();
		if (label != null && label.length() > 0) v.addUserDatum(Constants.NODE_LABEL, label, UserData.SHARED);
		String uri = node.getUri();
		if (uri != null && uri.length() > 0) v.addUserDatum(Constants.NODE_URI, uri, UserData.SHARED);

		//logger.debug("set temp node attributes...");
		if (!includeAttrs) return v;		
		Map<String, String> attributes = node.getAttributes();
		for (String key : attributes.keySet()){
			v.addUserDatum(key, attributes.get(key), UserData.SHARED);
		}	
				
		return v;
	}
	
	
	/**
	 * Converts a CIKNOW edge to a Jung edge
	 * 
	 * @param edge	the Edge to be converted
	 * @param from	the Jung vertex to be set as the source/endpoint of the new Jung edge
	 * @param to	the Jung vertex to be set as the destination/endpoint of the new Jung edge
	 * @return
	 */
	public static edu.uci.ics.jung.graph.Edge CIKNOW2JUNG(Edge edge, Vertex from, Vertex to, boolean includeAttrs){
		edu.uci.ics.jung.graph.Edge e = null;
		if (from == null || to == null){
			logger.error("invalid input data");
			return e;
		}
		if (edge.isDirected()) e = new DirectedSparseEdge(from, to);
		else e = new UndirectedSparseEdge(from, to);
		
		e.addUserDatum(Constants.EDGE_ID, edge.getId(), UserData.SHARED);	
		if (edge.getVersion() != null) e.addUserDatum(Constants.EDGE_VERSION, edge.getVersion(), UserData.SHARED);
		if (edge.getType() != null) e.addUserDatum(Constants.EDGE_TYPE, edge.getType(), UserData.SHARED);
		if (edge.getWeight() != null) e.addUserDatum(Constants.EDGE_WEIGHT, edge.getWeight(), UserData.SHARED);
		
		if (!includeAttrs) return e;
		Map<String, String> attributes = edge.getAttributes();
		for (String key : attributes.keySet()){
			e.addUserDatum(key, attributes.get(key), UserData.SHARED);
		}

		return e;	
	}
	

	

	
	

	

	//////////////////////// JUNG -> CIKNOW /////////////////////////////////////

	/**
	 * Converts the nodes and edges in a Jung graph into CIKNOW nodes and edges
	 * @param jg	the input Jung graph
	 * @return a hashmap containing CIKNOW "nodes" and "edges"
	 */
	@SuppressWarnings("unchecked")
	public static Map JUNG2CIKNOW(edu.uci.ics.jung.graph.Graph jg){
		if (jg == null) return null;
		Map ciknowG = new HashMap();
		Map vertexNodeMap = new HashMap();
		
		Set vertices = jg.getVertices();
		List<Node> nodes = new ArrayList<Node>();
		for (Object o : vertices){
			Vertex vertex = (Vertex) o;
			Node node = JUNG2CIKNOW(vertex);
			nodes.add(node);
			vertexNodeMap.put(vertex, node);
		}
		ciknowG.put("nodes", nodes);
		logger.debug("Converted " + jg.numVertices() + " jung vertices to " + nodes.size() + " ciknow nodes.");
		
		Set jungEdges = jg.getEdges();
		List<Edge> edges = new ArrayList<Edge>();
		for (Object o : jungEdges){
			edu.uci.ics.jung.graph.Edge jungEdge = (edu.uci.ics.jung.graph.Edge) o;
			
			Vertex from = null;
			Vertex to = null;
			if (jungEdge instanceof DirectedEdge){
				DirectedEdge de = (DirectedEdge) jungEdge;
				from = de.getSource();
				to = de.getDest();
			} else {
				from = (Vertex) jungEdge.getEndpoints().getFirst();
				to = (Vertex) jungEdge.getEndpoints().getSecond();
			}
			
			Node nf = (Node) vertexNodeMap.get(from);
			Node nt = (Node) vertexNodeMap.get(to);
			
			Edge edge = JUNG2CIKNOW(jungEdge, nf, nt);			
			
			// make use of vertexNodeMap to save memory, so comment out this
			//Edge edge = JUNG2CIKNOW(jungEdge);
			
			edges.add(edge);
		}
		ciknowG.put("edges", edges);
		logger.debug("Converted " + jg.numEdges() + " jung edges to " + edges.size() + " ciknow edges.");
		
		return ciknowG;
	}
	
	/**
	 * Converts a Jung edge to a CIKNOW edge
	 * @param jungEdge	the edge to be converted
	 * @return
	 */
	/*
	@SuppressWarnings("unused")
	public static Edge JUNG2CIKNOW(edu.uci.ics.jung.graph.Edge jungEdge){
		Vertex from = null;
		Vertex to = null;
		if (jungEdge instanceof DirectedEdge){
			DirectedEdge de = (DirectedEdge) jungEdge;
			from = de.getSource();
			to = de.getDest();
		} else {
			from = (Vertex) jungEdge.getEndpoints().getFirst();
			to = (Vertex) jungEdge.getEndpoints().getSecond();
		}
		return JUNG2CIKNOW(jungEdge, JUNG2CIKNOW(from), JUNG2CIKNOW(to));		
	}*/
	
	/**
	 * Converts a Jung edge to a CIKNOW edge
	 * 
	 * @param jungEdge	the Jung edge to be converted
	 * @param from		the CIKNOW node to be set as the source/endpoint of the newly created edge
	 * @param to		the CIKNOW node to be set as the dest/endpoint of the newly created edge
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static Edge JUNG2CIKNOW(edu.uci.ics.jung.graph.Edge jungEdge, Node from, Node to){
		Edge edge = new Edge();
		
		if (jungEdge instanceof DirectedEdge) edge.setDirected(true);
		else edge.setDirected(false);
		
		edge.setFromNode(from);
		edge.setToNode(to);
		
		Iterator itr = jungEdge.getUserDatumKeyIterator();
		while(itr.hasNext()){
			Object key = itr.next();
			String keyString = (String)key;
			Object value = jungEdge.getUserDatum(key);
			if (keyString.equalsIgnoreCase(Constants.EDGE_WEIGHT)) {
				edge.setWeight((Double)value);
			}else if (keyString.equalsIgnoreCase(Constants.EDGE_ID)) {
				edge.setId((Long)value);
			}else if (keyString.equalsIgnoreCase(Constants.EDGE_VERSION)) {
				edge.setVersion((Long)value);
			}else if (keyString.equalsIgnoreCase(Constants.EDGE_TYPE)) {
				edge.setType((String) value);
			}else {
				edge.setAttribute(keyString, (String)value);
            }
		}	
		
		return edge;
	}
	
	
	/**
	 * Converts a Jung vertex to a CIKNOW node
	 * @param vertex	the Jung vertex to be converted
	 * @return Node
	 */
	@SuppressWarnings("unchecked")
	public static Node JUNG2CIKNOW(Vertex vertex){
		Node node = new Node();
		
		Iterator itr = vertex.getUserDatumKeyIterator();
		while(itr.hasNext()){
			Object key = itr.next();
			String keyString = (String)key;
			if (keyString.equalsIgnoreCase(Constants.NODE_ID)) {
				node.setId((Long) vertex.getUserDatum(keyString));
			}else if (keyString.equalsIgnoreCase(Constants.NODE_VERSION)) {
				node.setVersion((Long) vertex.getUserDatum(keyString));
			}else if (keyString.equalsIgnoreCase(Constants.NODE_TYPE)) {
				node.setType((String) vertex.getUserDatum(keyString));
			}else if (keyString.equalsIgnoreCase(Constants.NODE_LABEL)) {
				node.setLabel((String) vertex.getUserDatum(keyString));
			}else if (keyString.equalsIgnoreCase(Constants.NODE_URI)) {
				node.setUri((String) vertex.getUserDatum(keyString));
			}else {
                node.getAttributes().put(keyString, (String)vertex.getUserDatum(key));
            }
		}
		
		return node;
	}
	
	
	//////////////////////// GRAPHML <-> JUNG /////////////////////////////////////
	/**
	 * Reads a GraphML file into a Jung graph
	 * @param filename
	 * @return
	 */
	public static edu.uci.ics.jung.graph.Graph GraphML2JUNG(String filename){
		GraphMLFile gml = new GraphMLFile();
		edu.uci.ics.jung.graph.Graph jg = gml.load(filename);

		// temperoary, will be removed after andydon remove "nodeId" from graphml
		//posProcessVertices(jg);
		
		return jg;
	}
	
	/**
	 * Writes a Jung graph to GraphML
	 * @param g
	 * @param out
	 */
	public static void JUNG2GraphML(edu.uci.ics.jung.graph.Graph g, OutputStream out){		
		GraphMLFile gml = new GraphMLFile();
		gml.save(g, new PrintStream(out));
	}
	
	/*
	@SuppressWarnings({ "unchecked", "unused" })
	private static void posProcessVertices(edu.uci.ics.jung.graph.Graph jg) {
		int count = 1;
		Set<Vertex> vertices = (Set<Vertex>)jg.getVertices();		
		for (Vertex v : vertices){
			v.setUserDatum(Constants.NODE_ID, count++, UserData.CLONE);		
		}
	}
	*/
	
	
	
	//////////////////////////////////////////////////////////////////////////////
	////////////////////////// CIKNOW -> JGRAPHT ///////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////
	public static SimpleGraph<Long, DefaultEdge> CIKNOW2JGRAPHT_SIMPLE(Collection<Node> nodes, Collection<Edge> edges){
		SimpleGraph<Long, DefaultEdge> jg = new SimpleGraph<Long, DefaultEdge>(DefaultEdge.class);
		
		for (Node node : nodes){
			jg.addVertex(node.getId());
		}
		
		for (Edge e : edges){
			Long fromNodeId = e.getFromNode().getId();
			Long toNodeId = e.getToNode().getId();
			try{
				org.jgrapht.graph.DefaultEdge jedge= jg.addEdge(fromNodeId, toNodeId);
				if (jedge == null) logger.info("Ignore duplicate tie between " + fromNodeId + " and " + toNodeId);
			} catch(IllegalArgumentException ex){
				logger.info("Ignore self loop for node " + fromNodeId);
			}
		}
		
		return jg;
	}
	
	public static SimpleDirectedGraph<Long, DefaultEdge> CIKNOW2JGRAPHT_SIMPLE_DIRECTED(Collection<Node> nodes, Collection<Edge> edges){
		SimpleDirectedGraph<Long, DefaultEdge> jg = new SimpleDirectedGraph<Long, DefaultEdge>(DefaultEdge.class);
		
		for (Node node : nodes){
			jg.addVertex(node.getId());
		}
		
		for (Edge e : edges){
			Long fromNodeId = e.getFromNode().getId();
			Long toNodeId = e.getToNode().getId();
			try{
				org.jgrapht.graph.DefaultEdge jedge= jg.addEdge(fromNodeId, toNodeId);
				if (jedge == null) logger.info("Ignore duplicate tie between " + fromNodeId + " and " + toNodeId);
			} catch(IllegalArgumentException ex){
				logger.info("Ignore self loop for node " + fromNodeId);
			}
		}
		
		return jg;
	}
	
	public static void main(String[] args){
		//testSimpleGraph();
		testSimpleDirectedGraph();
/*		List<Node> nodes = new ArrayList<Node>();
		List<Edge> edges = new ArrayList<Edge>();
		Node node;
		Edge edge;
		
		node = new Node();
		node.setId(1L);
		nodes.add(node);
		
		node = new Node();
		node.setId(2L);
		nodes.add(node);
		
		edge = new Edge();
		edge.setFromNode(nodes.get(0));
		edge.setToNode(nodes.get(1));*/
		
	}

	private static void testSimpleGraph() {
		SimpleGraph<Long, DefaultEdge> g = new SimpleGraph<Long, DefaultEdge>(DefaultEdge.class);
		g.addVertex(1L);
		g.addVertex(2L);
		
		DefaultEdge edge;
		edge = g.addEdge(1L, 2L);
		edge = g.addEdge(2L, 1L);		
		logger.info(edge==null);
		edge = g.addEdge(1L, 2L);
		logger.info(edge==null);
	}
	
	private static void testSimpleDirectedGraph() {
		SimpleDirectedGraph<Long, DefaultEdge> g = new SimpleDirectedGraph<Long, DefaultEdge>(DefaultEdge.class);
		g.addVertex(1L);
		g.addVertex(2L);
		
		DefaultEdge edge;
		edge = g.addEdge(1L, 2L);
		edge = g.addEdge(2L, 1L);		
		logger.info(edge==null);
		edge = g.addEdge(1L, 2L);
		logger.info(edge==null);
	}	
}