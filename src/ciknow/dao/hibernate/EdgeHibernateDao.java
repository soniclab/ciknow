package ciknow.dao.hibernate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Transactional;

import ciknow.dao.EdgeDao;
import ciknow.dao.NodeDao;
import ciknow.dao.QuestionDao;
import ciknow.domain.Edge;
import ciknow.domain.Field;
import ciknow.domain.Node;
import ciknow.domain.Question;
import ciknow.util.Beans;
import ciknow.util.Constants;
import ciknow.util.GeneralUtil;
import edu.uci.ics.jung.graph.filters.impl.KNeighborhoodFilter;

@Transactional
public class EdgeHibernateDao extends HibernateDaoSupport implements EdgeDao{
	private static Log logger = LogFactory.getLog(EdgeHibernateDao.class);
	
	public static void main(String[] args) throws Exception{
		Beans.init();
		NodeDao nodeDao = (NodeDao)Beans.getBean("nodeDao");
		EdgeDao edgeDao = (EdgeDao)Beans.getBean("edgeDao");
		QuestionDao questionDao = (QuestionDao)Beans.getBean("questionDao");
		
		//testSave(nodeDao, edgeDao);
		//testGetEdgeTypesAmongNodeTypes(edgeDao);
		//testFindByFromNodeId(edgeDao);
		//testGetNodeTypesByEdgeTypes(edgeDao);
		//testGetEdgeTypesByNodeType(nodeDao, edgeDao);
		//testGetEdgeTypesByNodeTypes(nodeDao, edgeDao);
		//testGetOtherNodeTypesByNodeTypes(nodeDao, edgeDao);
		//testFindEdgeTypesByFromAndToNodeTypes(edgeDao);
		//testFindByType(edgeDao);
		//testLoadByType(edgeDao);
		//testGetAttributeNames(edgeDao);
		//testDeleteByType(edgeDao);
		//testDeleteByIds(edgeDao);
		//testLoadAll(edgeDao);
		//testGetAll(edgeDao);
		//testGetPage(edgeDao);
		//testLoadPage(edgeDao);
		//testLoadEdgesAmongNodes(nodeDao, edgeDao);
		testLoadByQuestionAndNode(edgeDao, nodeDao, questionDao);
	}
	
	// this method will failed, thus prove the save() method is indeed within a transaction
	// - either succeed or rollback all
	@SuppressWarnings("unused")
	private static void testSave(NodeDao nodeDao, EdgeDao edgeDao){
		List<Edge> edges = new ArrayList<Edge>();
		Node fromNode = nodeDao.getProxy(1L);
		Node toNode = nodeDao.getProxy(2000L);
		HashMap<String, String> attributes = new HashMap<String, String>();
		attributes.put("1", "ATT_VALUE 1");
		attributes.put("2", "ATT_VALUE 2");
		HashMap<String, String> longAttributes = new HashMap<String, String>();
		longAttributes.put("1", "LONG_ATT_VALUE 1");
		longAttributes.put("2", "LONG_ATT_VALUE 2");
		
		Edge edge;
		
		logger.debug("edge 2");
		edge = createEdge(fromNode, fromNode, true, 1.0f, "test type", attributes, longAttributes);
		edges.add(edge);
		
		logger.debug("edge 4");
		edge = createEdge(toNode, toNode, true, 1.0f, "test type", attributes, longAttributes);
		edges.add(edge);
		
		logger.debug("edge 1");
		edge = createEdge(fromNode, toNode, true, 1.0f, "test type", attributes, longAttributes);
		edges.add(edge);

		logger.debug("edge 3");
		edge = createEdge(toNode, fromNode, true, 1.0f, "test type", attributes, longAttributes);
		edges.add(edge);

		
		edgeDao.save(edges);
	}
	
	private static Edge createEdge(Node fromNode, Node toNode, boolean directed, double weight, String type,
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
	
	/*
	private static void testGetEdgeTypesAmongNodeTypes(EdgeDao edgeDao){
		List<String> nodeTypes = new LinkedList<String>();
		nodeTypes.add("user");
		
		List<String> edgeTypes = edgeDao.getEdgeTypes();
		logger.info("All edge types: " + edgeTypes);
		
		edgeTypes = edgeDao.getEdgeTypesAmongNodeTypes(nodeTypes);
	}
	*/
	private static void testLoadByQuestionAndNode(EdgeDao edgeDao, NodeDao nodeDao, QuestionDao questionDao) throws Exception{
		Node node = nodeDao.getProxy(1L);
		Question question = questionDao.findById(34L);
		edgeDao.loadByQuestionAndNode(question, node);
	}
	/*
	private static void testFindByFromNodeId(EdgeDao edgeDao){
		Set<Edge> edgeSet = new HashSet<Edge>();
		List<Edge> edges = edgeDao.findByFromNodeId(2L);
		edgeSet.addAll(edges);
		logger.debug("there are " + edgeSet.size() + " outgoing edges.");
	}
	
	private static void testGetNodeTypesByEdgeTypes(EdgeDao edgeDao){
		List<String> edgeTypes = new LinkedList<String>();
		//edgeTypes.add("Authorship");
		//edgeTypes.add("Citation");
		edgeTypes.add("Dep (relation)");
		Collection<String> nodeTypes = edgeDao.getNodeTypesByEdgeTypes(edgeTypes);
		logger.info(nodeTypes);
	}
	
	private static void testLoadEdgesAmongNodes(NodeDao nodeDao, EdgeDao edgeDao) {
		List<Node> nodes = nodeDao.getPage(0, 100);
		List<Edge> edges = edgeDao.loadEdgesAmongNodes(nodes, false, false, null);
		for (Edge edge : edges){
			logger.debug(edge);
		}
	}
	
	private static void testLoadAll(EdgeDao edgeDao) {
		List<Edge> edges = edgeDao.loadAll();
		Edge edge = edges.get(0);
		logger.debug("first edge: " + edge);
	}
	
	private static void testGetAll(EdgeDao edgeDao) {
		List<Edge> edges = edgeDao.getAll();
		Edge edge = edges.get(0);
		logger.debug("first edge: " + edge.getId());
	}

	private static void testGetAttributeNames(EdgeDao edgeDao){
		List<String> attrNames = edgeDao.getAttributeNames();
		for (String name : attrNames){
			logger.info(name);
		}
	}
	
	private static void testFindByType(EdgeDao edgeDao){
		String type = "Citation";
		List<Edge> edges = edgeDao.findByType(type, true);
		logger.info("there are " + edges.size() + " edges for type: " + type);
	}
	private static void testLoadByType(EdgeDao edgeDao){
		String type = "q _creativity2";
		List<Edge> edges = edgeDao.loadByType(type, false);
		logger.info("there are " + edges.size() + " edges for type: " + type);
	}
	
	private static void testDeleteByType(EdgeDao edgeDao){
		String type = "Citation";
		edgeDao.deleteByType(type);
	}
	
	private static void testFindEdgeTypesByFromAndToNodeTypes(EdgeDao edgeDao){
		List<Map<String, String>> edgeTypes = edgeDao.getEdgeTypesByFromAndToNodeTypes(Constants.NODE_TYPE_USER, Constants.NODE_TYPE_USER);
		logger.info("edgeTypes: \n" + edgeTypes);
	}
	

	private static void testGetEdgeTypesByNodeType(NodeDao nodeDao, EdgeDao edgeDao){
		List<String> types = nodeDao.getNodeTypes();
		String nodeType = types.get(0);
		List<String> edgeTypes = edgeDao.getEdgeTypesByNodeType(nodeType);
	}

	
	private static void testGetEdgeTypesByNodeTypes(NodeDao nodeDao, EdgeDao edgeDao){
		List<String> types = nodeDao.getNodeTypes();
		List<String> nodeTypes = new LinkedList<String>();
		nodeTypes.add(types.get(0));
		nodeTypes.add(types.get(1));
		List<String> edgeTypes = edgeDao.getEdgeTypesByNodeTypes(nodeTypes, KNeighborhoodFilter.IN_OUT);		
	}
	
	private static void testGetOtherNodeTypesByNodeTypes(NodeDao nodeDao, EdgeDao edgeDao){
		List<String> types = nodeDao.getNodeTypes();
		int direction = KNeighborhoodFilter.OUT;
		List<String> nodeTypes = new LinkedList<String>();
		nodeTypes.add(types.get(0));
		nodeTypes.add(types.get(1));
		Collection<String> otherNodeTypes = edgeDao.getOtherNodeTypesByNodeTypes(nodeTypes, direction);
		logger.info("nodeTypes: " + nodeTypes + ", direction=" + direction + " ==> otherNodeTypes=" + otherNodeTypes);
	}
	*/
	
	
	
	public void save(Edge edge) {
		getHibernateTemplate().saveOrUpdate(edge);		
	}

	public void save(Collection<Edge> edges) {
		getHibernateTemplate().saveOrUpdateAll(edges);
	}

    public void delete(Edge edge) {
		getHibernateTemplate().delete(edge);
	}

	public void delete(Collection<Edge> edges) {
		getHibernateTemplate().deleteAll(edges);
	}
	
	
    public Edge findById(Long id) {
		return (Edge)getHibernateTemplate().get(Edge.class, id);
	}
    
	@SuppressWarnings("unchecked")
	public Edge loadById(Long id){
    	String query = "from Edge e left join fetch e.attributes " +
    								"left join fetch e.longAttributes " +
    								"left join fetch e.creator " + 
    								"left join fetch e.fromNode " +
    								"left join fetch e.toNode " + 
    								"where e.id = ?";
    	List<Edge> list = getHibernateTemplate().find(query, id);
    	if (list.size() == 0) return null;
    	else return list.get(0);
    }

    public Edge getProxy(Long id){
    	return getHibernateTemplate().load(Edge.class, id);
    }
    
    
    @SuppressWarnings("unchecked")
	public List<Edge> findByCreatorId(Long id) {
		String query = "from Edge e where e.creator.id = :creatorId";
		return getHibernateTemplate().findByNamedParam(query, "creatorId", id);
	}
    
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Edge> loadByCreatorId(final Long id) {
		logger.info("load by creatorId...");
    	return getHibernateTemplate().executeFind(new HibernateCallback(){

			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
		    	String query = "from Edge e " +
									"left join fetch e.creator " + 
									"left join fetch e.fromNode " +
									"left join fetch e.toNode " +
									"where e.creator.id = :creatorId";
				List<Edge> edges = session.createQuery(query)
									.setLong("creatorId", id)
									.list();
				init(edges);
				logger.debug(edges.size() + " edges loaded.");
				
				return edges;
			}    		
    	});
	}
	
	@SuppressWarnings("unchecked")
	public List<Edge> findByFromNodeId(Long id) {
        if (id == null) return new LinkedList<Edge>();
		String query = "from Edge e " +
				"where e.fromNode.id = :fromId " +
				"or (e.directed = false and e.toNode.id = :fromId)";
		return getHibernateTemplate().findByNamedParam(query, "fromId", id);
    }
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Edge> loadByFromNodeId(final Long id) {
		logger.info("load by fromNodeId...");
        if (id == null) return new LinkedList<Edge>();        
    	return getHibernateTemplate().executeFind(new HibernateCallback(){

			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
		    	String query = "from Edge e " +
									"left join fetch e.creator " + 
									"left join fetch e.fromNode " +
									"left join fetch e.toNode " +
									"where e.fromNode.id = :fromId " +
									"or (e.directed = false " +
										"and e.toNode.id = :fromId)";
				List<Edge> edges = session.createQuery(query)
									.setLong("fromId", id)
									.list();
				init(edges);
				logger.debug(edges.size() + " edges loaded.");
				
				return edges;
			}    		
    	});
    }
    
	@SuppressWarnings("unchecked")
	public List<Edge> findByToNodeId(Long id) {
        if (id == null) return new LinkedList<Edge>();
		String query = "from Edge e " +
				"where e.toNode.id = :toId " +
				"or (e.directed = false and e.fromNode.id = :toId)";
		List<Edge> list = getHibernateTemplate().findByNamedParam(query, "toId", id);

		return list;
    }
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Edge> findByToNodeIds(Collection<Long> ids) {
		if (ids == null || ids.isEmpty()) return  new ArrayList<Edge>();
		String query = "from Edge e where e.toNode.id in (:ids) or (e.directed = false and e.fromNode.id in (:ids))";
		return (List<Edge>)getHibernateTemplate().findByNamedParam(query, "ids", ids);
	}
	
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Edge> loadByToNodeId(final Long id) {
    	logger.info("load by toNodeId...");
        if (id == null) return new LinkedList<Edge>();
    	return getHibernateTemplate().executeFind(new HibernateCallback(){

			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
		    	String query = "from Edge e " +
									"left join fetch e.creator " + 
									"left join fetch e.fromNode " +
									"left join fetch e.toNode " +
									"where e.toNode.id = :toId " +
									"or (e.directed = false " +
										"and e.fromNode.id = :toId)";
				List<Edge> edges = session.createQuery(query)
									.setLong("toId", id)
									.list();
				init(edges);
				logger.debug(edges.size() + " edges loaded.");
				
				return edges;
			}
    		
    	});

    }


    @SuppressWarnings("unchecked")
	public List<Edge> findByFromToNodeId(Long fromId, Long toId) {
		if (fromId == null || toId == null) return new LinkedList<Edge>();

		String query = "from Edge e " +
				"where (e.fromNode.id = :fromId " +
					"and e.toNode.id = :toId) " +
				"or (e.directed = false " +
					"and e.toNode.id = :fromId " +
					"and e.fromNode.id = :toId)";
		List<Edge> list = getHibernateTemplate().findByNamedParam(query,
											new String[]{"fromId", "toId"},
											new Object[]{fromId, toId});
		return list;        
    }
    
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Edge> loadByFromToNodeId(final Long fromId, final Long toId) {
		logger.info("load by fromNodeId and toNodeId...");
		if (fromId == null || toId == null) return new LinkedList<Edge>();

    	return getHibernateTemplate().executeFind(new HibernateCallback(){

			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
		    	String query = "from Edge e " +
									"left join fetch e.creator " + 
									"left join fetch e.fromNode " +
									"left join fetch e.toNode " +
									"where (e.fromNode.id = :fromId " +
											"and e.toNode.id = :toId) " +
									"or (e.directed = false " +
										"and e.toNode.id = :fromId " +
										"and e.fromNode.id = :toId)";
				List<Edge> edges = session.createQuery(query)
									.setLong("fromId", fromId)
									.setLong("toId", toId)
									.list();
				init(edges);
				logger.debug(edges.size() + " edges loaded.");
				
				return edges;
			}
    		
    	});
      
    }
	
	
	@SuppressWarnings("unchecked")
	public List<Edge> findByType(String type, boolean includeEmptyEdges) {
		String query = "from Edge e where e.type = :type";
		if (!includeEmptyEdges) query += " and e.weight > " + Float.MIN_VALUE;
		return getHibernateTemplate().findByNamedParam(query, "type", type);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Edge> loadByType(final String type, final boolean includeEmptyEdges) {
		logger.info("load by type: " + type);

    	return getHibernateTemplate().executeFind(new HibernateCallback(){

			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
		    	String query = "from Edge e " +
									"left join fetch e.creator " + 
									"left join fetch e.fromNode " +
									"left join fetch e.toNode " + 
									"where e.type = :type";
		    	if (!includeEmptyEdges) query += " and e.weight > " + Float.MIN_VALUE;
		    	
				List<Edge> edges = session.createQuery(query)
									.setString("type", type)
									.list();
				init(edges);
				logger.debug(edges.size() + " edges loaded.");
				
				return edges;
			}
    		
    	});    	
	}

	
	
	@SuppressWarnings("unchecked")
	public List<Edge> findByQuestion(Question question){		
		List<Edge> edges = new ArrayList<Edge>();
		String qType = question.getType();
		if (qType.equals(Constants.RELATIONAL_CHOICE)
			|| qType.equals(Constants.RELATIONAL_RATING)
			|| qType.equals(Constants.RELATIONAL_CONTINUOUS)
			|| qType.equals(Constants.PERCEIVED_RELATIONAL_CHOICE)
			|| qType.equals(Constants.PERCEIVED_RELATIONAL_RATING)
			|| qType.equals(Constants.PERCEIVED_CHOICE)
			|| qType.equals(Constants.PERCEIVED_RATING)
			|| qType.equals(Constants.RELATIONAL_CHOICE_MULTIPLE)
			|| qType.equals(Constants.RELATIONAL_RATING_MULTIPLE)){
			
			if (qType.equals(Constants.RELATIONAL_CHOICE_MULTIPLE)
				|| qType.equals(Constants.RELATIONAL_RATING_MULTIPLE)){
				String query = "from Edge e where e.type like :edgeType";
				edges = getHibernateTemplate().findByNamedParam(query, "edgeType", question.getShortName() + Constants.SEPERATOR + "%");
			} else {
				String query = "from Edge e where e.type = :edgeType";
				edges = getHibernateTemplate().findByNamedParam(query, "edgeType", question.getEdgeType());
			}
		}
		
		return edges;
	}
	
	@SuppressWarnings("unchecked")
	public List<Edge> loadByQuestion(Question question){		
		List<Edge> edges = new ArrayList<Edge>();
		String qType = question.getType();
		if (qType.equals(Constants.RELATIONAL_CHOICE)
			|| qType.equals(Constants.RELATIONAL_RATING)
			|| qType.equals(Constants.RELATIONAL_CONTINUOUS)
			|| qType.equals(Constants.PERCEIVED_RELATIONAL_CHOICE)
			|| qType.equals(Constants.PERCEIVED_RELATIONAL_RATING)
			|| qType.equals(Constants.PERCEIVED_CHOICE)
			|| qType.equals(Constants.PERCEIVED_RATING)
			|| qType.equals(Constants.RELATIONAL_CHOICE_MULTIPLE)
			|| qType.equals(Constants.RELATIONAL_RATING_MULTIPLE)){
			
			if (qType.equals(Constants.RELATIONAL_CHOICE_MULTIPLE)
				|| qType.equals(Constants.RELATIONAL_RATING_MULTIPLE)){
				String query = "from Edge e " +
						"left join fetch e.creator " + 
						"left join fetch e.fromNode " +
						"left join fetch e.toNode " +
						"left join fetch e.attributes " +
						"where e.type like :edgeType";
				edges = getHibernateTemplate().findByNamedParam(query, "edgeType", question.getShortName() + Constants.SEPERATOR + "%");
			} else {
				String query = "from Edge e " +
						"left join fetch e.creator " + 
						"left join fetch e.fromNode " +
						"left join fetch e.toNode " +
						"left join fetch e.attributes " +
						"where e.type = :edgeType";
				edges = getHibernateTemplate().findByNamedParam(query, "edgeType", question.getEdgeType());
			}
		}
		
		return edges;
	}
	
    @SuppressWarnings("unchecked")
	@Override
    public List<Edge> loadByQuestionAndNode(Question question, Node node) {
        logger.info("loading edges by question (" + question.getLabel() + ") and node (" + node.getId() + ")...");
        String qtype = question.getType();
        List<Edge> edges = new ArrayList<Edge>();
        if (qtype.equals(Constants.RELATIONAL_CHOICE_MULTIPLE)
                || qtype.equals(Constants.RELATIONAL_RATING_MULTIPLE)) {
            List<String> edgeTypes = new LinkedList<String>();
            for (Field field : question.getFields()) {
                edgeTypes.add(question.getEdgeTypeWithField(field));
            }
            if (edgeTypes.isEmpty()) return edges;
            
            String query = "from Edge e "
                    + "left join fetch e.creator "
                    + "left join fetch e.fromNode "
                    + "left join fetch e.toNode "
                    + "left join fetch e.attributes "
                    //+ "left join fetch e.longAttributes "
                    + "where e.fromNode = :fromNode and e.type in (:edgeTypes)";
            edges = getHibernateTemplate().findByNamedParam(query, new String[]{"fromNode", "edgeTypes"}, new Object[]{node, edgeTypes});
        } else if (qtype.equals(Constants.RELATIONAL_CHOICE)
                || qtype.equals(Constants.RELATIONAL_RATING)
                || qtype.equals(Constants.RELATIONAL_CONTINUOUS)) {
            String query = "from Edge e "
                    + "left join fetch e.creator "
                    + "left join fetch e.fromNode "
                    + "left join fetch e.toNode "
                    + "left join fetch e.attributes "
                    //+ "left join fetch e.longAttributes "
                    + "where e.fromNode = :fromNode and e.type = :edgeType";
            edges = getHibernateTemplate().findByNamedParam(query, new String[]{"fromNode", "edgeType"}, new Object[]{node, question.getEdgeType()});

        } else if (qtype.equals(Constants.PERCEIVED_RELATIONAL_CHOICE)
                || qtype.equals(Constants.PERCEIVED_RELATIONAL_RATING)
                || qtype.equals(Constants.PERCEIVED_CHOICE)
                || qtype.equals(Constants.PERCEIVED_RATING)) {
            String query = "from Edge e "
                    + "left join fetch e.creator "
                    + "left join fetch e.fromNode "
                    + "left join fetch e.toNode "
                    + "left join fetch e.attributes "
                    //+ "left join fetch e.longAttributes "
                    + "where e.creator = :creator and e.type = :edgeType";
            edges = getHibernateTemplate().findByNamedParam(query, new String[]{"creator", "edgeType"}, new Object[]{node, question.getEdgeType()});
        } else {
            logger.warn("Question type='" + qtype + "' is not allowed in this operation.");
        }

        logger.debug(edges.size() + " edges loaded.");

        return edges;
    }
    
	@SuppressWarnings("unchecked")
	public List<Edge> findEdgesAmongNodes(Collection<Node> nodes) {
		logger.info("find edges among nodes");
		String query = "from Edge e " +
				"where e.fromNode in (:nodes) and e.toNode in (:nodes)";		
		List<Edge> edges = getHibernateTemplate().findByNamedParam(query, 
																	"nodes", 
																	nodes);
		logger.info("find edges among nodes. done");
		return edges;
	}
	
	@SuppressWarnings("unchecked")
	public List<Edge> loadEdgesAmongNodes(Collection<Node> nodes, boolean includeDerivedEdges, boolean includeEmptyEdges, Collection<String> edgeTypes) {
		logger.info("load edges among nodes");
		
			String query = "from Edge e " +
					"left join fetch e.creator " +
					"left join fetch e.fromNode " +
					"left join fetch e.toNode " +
					"left join fetch e.attributes " +
					//"left join fetch e.longAttributes " +
					"where e.fromNode in (:nodes) and e.toNode in (:nodes)";
			if (!includeDerivedEdges) query += " and e.type not like 'd.%'";
			if (!includeEmptyEdges) query += " and e.weight > " + Float.MIN_VALUE;
			if (edgeTypes != null && !edgeTypes.isEmpty()) query += " and e.type in (:edgeTypes)";
			
			List<Edge> edges = null;
			if (edgeTypes != null && !edgeTypes.isEmpty()){
				edges = getHibernateTemplate().findByNamedParam(query, new String[]{"nodes", "edgeTypes"}, new Object[]{nodes, edgeTypes});		
			} else {
				edges = getHibernateTemplate().findByNamedParam(query, "nodes", nodes);
			}
			
			logger.debug(edges.size() + " edges loaded.");
			return edges;
	}	
	

	public List<Edge> getAll() {
		logger.info("get all edges...");
		List<Edge> edges = getHibernateTemplate().loadAll(Edge.class);
		logger.info("got " + edges.size() + " edges.");
		
		return edges;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Edge> loadAll() {
		logger.info("loading all edges...");
    	return getHibernateTemplate().executeFind(new HibernateCallback(){

			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
		    	String query = "from Edge e " +
									"left join fetch e.creator " + 
									"left join fetch e.fromNode " +
									"left join fetch e.toNode";

				List<Edge> edges = session.createQuery(query).list();
				init(edges);
				
				logger.debug(edges.size() + " edges loaded.");
				
				return new ArrayList<Edge>(edges);
			}
    		
    	});

	}
	
	public int getCount() {
		return (Integer) getHibernateTemplate().find("select count(*) from Edge").get(0);
	}

	@SuppressWarnings("unchecked")
	public List<String> getEdgeTypes() {	
		logger.info("get all edge types...");
		List<String> edgeTypes;
		edgeTypes = getHibernateTemplate().find("select distinct e.type from Edge e");
		
		logger.info("get " + edgeTypes.size() + " edgeTypes.");
		return edgeTypes;
	}


	@SuppressWarnings("unchecked")
	public List<String> getEdgeTypesByNodeType(String nodeType) {
		String query = "select distinct e.type from Edge e where " +
						"e.fromNode.type = :nodeType " +
						"or e.toNode.type = :nodeType";		
		logger.info("getEdgeTypesByNodeType: " + query);
		List<String> edgeTypes = getHibernateTemplate().findByNamedParam(
																	query, 
																	"nodeType", 
																	nodeType);
		logger.info("nodeType: " + nodeType + ", edgeTypes: " + edgeTypes);
		return edgeTypes;
	}

	
	/**
	 * direction: KNeighborhoodFilter.IN_OUT, IN, OUT
	 */
	@SuppressWarnings("unchecked")
	public List<String> getEdgeTypesByNodeTypes(List<String> nodeTypes, int direction) {
		String query = "select distinct e.type from Edge e where " +
								"e.fromNode.type in (:nodeTypes) " +
								"or e.toNode.type in (:nodeTypes)";
		if (direction == KNeighborhoodFilter.IN) 
			query = "select distinct e.type from Edge e where " + "e.toNode.type in (:nodeTypes) " +
					"or (e.directed = false and e.fromNode.type in (:nodeTypes))";
		
		else if (direction == KNeighborhoodFilter.OUT) 
			query = "select distinct e.type from Edge e where " + "e.fromNode.type in (:nodeTypes) " +
					"or (e.directed = false and e.toNode.type in (:nodeTypes))";
		
		logger.info("getEdgeTypesByNodeTypes: " + query);
		List<String> edgeTypes = getHibernateTemplate().findByNamedParam(query, "nodeTypes", nodeTypes);
		logger.info("nodeTypes: " + nodeTypes + ", edgeTypes: " + edgeTypes);
		return edgeTypes;
	}	
	
	@SuppressWarnings("unchecked")
	public List<String> getEdgeTypesAmongNodeTypes(List<String> nodeTypes) {
		String query = "select distinct e.type from Edge e where " +
								"e.fromNode.type in (:nodeTypes) " +
								"and e.toNode.type in (:nodeTypes)";
		
		logger.info("getEdgeTypesAmongNodeTypes: " + query);
		List<String> edgeTypes = getHibernateTemplate().findByNamedParam(query, "nodeTypes", nodeTypes);
		logger.info("nodeTypes: " + nodeTypes + ", edgeTypes: " + edgeTypes);
		return edgeTypes;
	}
	
	@SuppressWarnings("unchecked")
	public Collection<String> getOtherNodeTypesByNodeTypes(List<String> nodeTypes, int direction) {
		logger.info("getOtherNodeTypesByNodeTypes: nodeTypes=" + nodeTypes + ", direction=" + direction);
		HibernateTemplate ht = getHibernateTemplate();
		Set<String> otherNodeTypes = new TreeSet<String>();
		String query = null;
		if (direction == KNeighborhoodFilter.IN) {
			query = "select distinct e.fromNode.type from Edge e where e.toNode.type in (:nodeTypes)";
			logger.info("query: " + query);
			otherNodeTypes.addAll(ht.findByNamedParam(query, "nodeTypes", nodeTypes));
			query = "select distinct e.toNode.type from Edge e where e.directed = false and e.fromNode.type in (:nodeTypes)";
			logger.info("query: " + query);
			otherNodeTypes.addAll(ht.findByNamedParam(query, "nodeTypes", nodeTypes));
		} else if (direction == KNeighborhoodFilter.OUT) {
			query = "select distinct e.toNode.type from Edge e where e.fromNode.type in (:nodeTypes)";
			logger.info("query: " + query);
			otherNodeTypes.addAll(ht.findByNamedParam(query, "nodeTypes", nodeTypes));
			query = "select distinct e.fromNode.type from Edge e where e.directed = false and e.toNode.type in (:nodeTypes)";
			logger.info("query: " + query);
			otherNodeTypes.addAll(ht.findByNamedParam(query, "nodeTypes", nodeTypes));
		} else {
			query = "select distinct e.toNode.type from Edge e where e.fromNode.type in (:nodeTypes)";
			logger.info("query: " + query);
			otherNodeTypes.addAll(ht.findByNamedParam(query, "nodeTypes", nodeTypes));
			query = "select distinct e.fromNode.type from Edge e where e.toNode.type in (:nodeTypes)";
			logger.info("query: " + query);
			otherNodeTypes.addAll(ht.findByNamedParam(query, "nodeTypes", nodeTypes));
		}
		
		logger.info("Got other Node Types: " + otherNodeTypes);
		return otherNodeTypes;
	}
	
	@SuppressWarnings("unchecked")
	public Collection<String> getNodeTypesByEdgeTypes(Collection<String> edgeTypes) {
		Set<String> nodeTypes = new HashSet<String>();
		
		String query = "select distinct e.fromNode.type from Edge e where e.type in (:edgeTypes)";		
		List<String> fromNodeTypes = getHibernateTemplate().findByNamedParam(query, "edgeTypes", edgeTypes);
		query = "select distinct e.toNode.type from Edge e where e.type in (:edgeTypes)";
		List<String> toNodeTypes = getHibernateTemplate().findByNamedParam(query, "edgeTypes", edgeTypes);
		nodeTypes.addAll(fromNodeTypes);
		nodeTypes.addAll(toNodeTypes);
		
		return nodeTypes;
	}
	
	@SuppressWarnings("unchecked")
	public List<Map<String, String>> getEdgeTypesByFromAndToNodeTypes(
														String fromNodeType, 
														String toNodeType) {
		logger.info("find edge types by from and to node types...");
		
		String query = "select distinct e.type from Edge e " +
				"where (e.fromNode.type = :fromNodeType " +
					"and e.toNode.type = :toNodeType) " +
				"or (e.directed = false " +
					"and e.fromNode.type = :toNodeType " +
					"and e.toNode.type = :fromNodeType)";
		List<String> edgeTypes = getHibernateTemplate().findByNamedParam(query, 
				new String[]{"fromNodeType", "toNodeType"}, 
				new Object[]{fromNodeType, toNodeType});		
		
		List<Map<String, String>> list = new LinkedList<Map<String, String>>();
		List<Map<String, String>> eds = GeneralUtil.getEdgeDescriptions();
		for (String edgeType:edgeTypes){
			Map<String, String> m = new HashMap<String, String>();
			m.put("type", edgeType);
			m.put("label", GeneralUtil.getEdgeLabel(eds, edgeType));
			m.put("direction", "1");
			list.add(m);
		}
		
		if (!fromNodeType.equals(toNodeType)){
			query = "select distinct e.type from Edge e " +
					"where (e.directed = true and e.fromNode.type = :toNodeType " +
					"and e.toNode.type = :fromNodeType)";
			List<String> reversedEdgeTypes = getHibernateTemplate().findByNamedParam(query, 
			new String[]{"fromNodeType", "toNodeType"}, 
			new Object[]{fromNodeType, toNodeType});
			for (String edgeType:reversedEdgeTypes){
				Map<String, String> m = new HashMap<String, String>();
				m.put("type", edgeType);
				m.put("label", GeneralUtil.getEdgeLabel(eds, edgeType));
				m.put("direction", "-1");
				list.add(m);
			}
		}
		
		return list;
	}
	
	
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<String> getAttributeNames() {
		HibernateTemplate ht = getHibernateTemplate();
		List<String> attrNames = (List<String>)ht.execute(new HibernateCallback(){

			public Object doInHibernate(Session session){
				Connection con = session.connection();
				PreparedStatement ps;
				List<String> attrNames = new LinkedList<String>();
				try {
					
					String sql = "SELECT DISTINCT attr_key FROM edge_attributes";
					ps = con.prepareStatement(sql);
					ResultSet rs = ps.executeQuery();
					while (rs.next()){
						attrNames.add(rs.getString(1));
					}
					
				} catch (SQLException e) {					
					e.printStackTrace();
				}

				return attrNames;
			}
			
		});	
		
		return attrNames;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<String> getLongAttributeNames() {
		HibernateTemplate ht = getHibernateTemplate();
		List<String> attrNames = (List<String>)ht.execute(new HibernateCallback(){

			public Object doInHibernate(Session session){
				Connection con = session.connection();
				PreparedStatement ps;
				List<String> attrNames = new LinkedList<String>();
				try {
					
					String sql = "SELECT DISTINCT attr_key FROM edge_long_attributes";
					ps = con.prepareStatement(sql);
					ResultSet rs = ps.executeQuery();
					while (rs.next()){
						attrNames.add(rs.getString(1));
					}
					
				} catch (SQLException e) {					
					e.printStackTrace();
				}

				return attrNames;
			}
			
		});	
		
		return attrNames;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<String> getAttributeValues(final String name) {
		HibernateTemplate ht = getHibernateTemplate();
		List<String> attrValues = (List<String>)ht.execute(new HibernateCallback(){

			public Object doInHibernate(Session session){
				Connection con = session.connection();
				PreparedStatement ps;
				List<String> attrValues = new LinkedList<String>();
				try {
					
					String sql = "SELECT DISTINCT attr_value FROM edge_attributes where attr_key = ?";
					ps = con.prepareStatement(sql);
					ps.setString(1, name);
					ResultSet rs = ps.executeQuery();
					while (rs.next()){
						attrValues.add(rs.getString(1));
					}
					
				} catch (SQLException e) {					
					e.printStackTrace();
				}

				return attrValues;
			}
			
		});	
		
		return attrValues;
	}
	
	
	// initialize (fetch=subselect, lazy=true)	
	private void init(List<Edge> edges){
		if (!edges.isEmpty()){			
			Edge edge = edges.get(0);			
			
			edge.getAttributes().isEmpty();
			
			// edge.longAttribute is never used
			//edge.getLongAttributes().isEmpty();
		}
	}
}
