package ciknow.dao.hibernate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mahout.common.IOUtils;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Transactional;

import ciknow.domain.Node;
import ciknow.domain.Question;
import ciknow.domain.Scale;
import ciknow.dao.NodeDao;
import ciknow.dao.QuestionDao;
import ciknow.util.Beans;
import ciknow.util.Constants;

/**
 * Implementation of NodeDao using Hibernate and Spring support
 * @author gyao
 *
 */
@Transactional
public class NodeHibernateDao extends HibernateDaoSupport implements NodeDao {
	private static Log logger = LogFactory.getLog(NodeHibernateDao.class);
	
	public static void main(String[] args){
		Beans.init();
		NodeDao nodeDao = (NodeDao)Beans.getBean("nodeDao");

		//testFindNeighbors(nodeDao);
		//testGetAll(nodeDao);
		//testGetAllPlainNodes(nodeDao);
		//testLoadAll(nodeDao);
		//testGetPage(nodeDao);
		//testLoadPage(nodeDao);		
		//testLoadById(nodeDao);		
		//testFindById(nodeDao);
		//testFindByType(nodeDao);
		//testLoadByType(nodeDao);
		testGetAttributeNames(nodeDao);
		//testFindByIds(nodeDao);
		//testLoadByIds(nodeDao);
	}


	/*
	private static void testFindByIds(NodeDao nodeDao) {
		Collection<Long> ids = new LinkedList<Long>();
		ids.add(1L);
		ids.add(2L);
		List<Node> nodes = nodeDao.findByIds(ids);
		for (Node node : nodes){
			logger.info(node.getUsername());
		}
	}
	
	private static void testLoadByIds(NodeDao nodeDao) {
		Collection<Long> ids = new LinkedList<Long>();
		ids.add(1L);
		ids.add(2L);
		List<Node> nodes = nodeDao.loadByIds(ids);
		for (Node node : nodes){
			logger.info(node.getUsername() + ":" + node.getAttribute("F`c`a"));
		}
	}

	private static void testFindNeighbors(NodeDao nodeDao){
		List<Node> nodes = new LinkedList<Node>();
		nodes.add(nodeDao.findById(59L));
		Collection<String> edgeTypes = new LinkedList<String>();
		edgeTypes.add("PIsAndCo-PIs");
		Set<Node> neighbors = nodeDao.findNeighbors(nodes, false, false, edgeTypes);
		logger.info(neighbors.size() + " neighbors found");
	}
	
	private static void testGetAll(NodeDao nodeDao) {
		List<Node> nodes = nodeDao.getAll();
	}	
	*/
	
	private static void testGetAttributeNames(NodeDao nodeDao){
		List<String> attrNames = nodeDao.getAttributeNames(null);
		logger.info("attrNames: " + attrNames);
		
		List<String> nodeTypes = new LinkedList<String>();
		nodeTypes.add("Author");
		attrNames = nodeDao.getAttributeNames(nodeTypes);
		logger.info("nodeTypes: " + nodeTypes + ", attrNames: " + attrNames);
	}
	
	/*
	private static void testFindByType(NodeDao nodeDao){
		List<Node> nodes = nodeDao.findByType(Constants.NODE_TYPE_USER);
		for (Node node : nodes){
			logger.debug(node.getUsername());
		}
	}
	
	private static void testLoadByType(NodeDao nodeDao){
		List<Node> nodes = nodeDao.loadByType(Constants.NODE_TYPE_USER);
		for (Node node : nodes){
			logger.debug(node);
		}
	}
	
	private static void testFindById(NodeDao nodeDao){
		Node node = nodeDao.findById(1L);		
		logger.debug("nodeLabel: " + node.getLabel());
		node.setLabel("ciknow admin");
		nodeDao.save(node);
		
		node = nodeDao.loadById(1L);
		logger.debug("nodeLabel: " + node.getLabel());
		logger.debug("attributes: " + node.getAttributes().size());
		node.setLabel("CIKNOW ADMIN");
		nodeDao.save(node);		
	}
	
	private static void testLoadById(NodeDao nodeDao){
		Node node = nodeDao.loadById(1L);
		logger.debug("nodeLabel=" + node.getLabel() + " number of attributes: " + node.getAttributes().size());
	}
	
	private static void testLoadAll(NodeDao nodeDao){
		List<Node> nodes = nodeDao.loadAll();
		Node node = nodes.get(100);
		logger.debug("node: " + node);
		logger.debug("long attributes: " + node.getLongAttributes());
		logger.debug("groups: " + node.getGroups());
		logger.debug("roles: " + node.getRoles());
	}
	*/
	
	
	public void save(Node node) {
		getHibernateTemplate().saveOrUpdate(node);
	}

	public void save(Collection<Node> nodes) {
		getHibernateTemplate().saveOrUpdateAll(nodes);
	}
	
	public void delete(Node node) {
		getHibernateTemplate().delete(node);		
	}

	public void delete(Collection<Node> nodes) {
		getHibernateTemplate().deleteAll(nodes);
	}
	
	
	public Node getProxy(Long id) {
		return (Node)getHibernateTemplate().load(Node.class, id);
	}

	public Node findById(Long id) {
		return (Node)getHibernateTemplate().get(Node.class, id);
	}
	
	
	@SuppressWarnings({ "rawtypes" })
	public Node loadById(Long id) {
		String query = "from Node n " +
						"left join fetch n.attributes " +
						"left join fetch n.longAttributes " +
						"left join fetch n.roles " +
						"left join fetch n.groups " +
						"where n.id=?";

		List list = getHibernateTemplate().find(query, id);
		if (list != null && list.size() > 0) return (Node)list.get(0);
		else return null;	
	}
	
	@SuppressWarnings({ "rawtypes" })
	public Node findByUsername(String username) {
		String query = "from Node n " +
						"where n.username=?";
		List list = getHibernateTemplate().find(query, username);
		if (list != null && list.size() > 0) return (Node)list.get(0);
		else return null;
	}


	
	@SuppressWarnings({ "rawtypes" })
	public Node loadByUsername(String username) {
		String query = "from Node n " +
						"left join fetch n.attributes " +
						"left join fetch n.longAttributes " +
						"left join fetch n.roles " +
						"left join fetch n.groups " +
						"where n.username=?";
		
		List list = getHibernateTemplate().find(query, username);
		if (list != null && list.size() > 0) return (Node)list.get(0);
		else return null;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Node findByEmail(String email) {
		String query = "from Node n " +
						"where n.email=?";
		List list = getHibernateTemplate().find(query, email);
		if (list != null && list.size() > 0) return (Node)list.get(0);
		else return null;
	}
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Node> findByLabel(String label){
		String query = "from Node n where n.label = :label";
		List list = getHibernateTemplate().findByNamedParam(query, "label", label);
		return (List<Node>) list;		
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Node> loadByLabel(final String label) {
		logger.info("load nodes by label...");
		
		return getHibernateTemplate().executeFind(new HibernateCallback(){

			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query query = session.createQuery("from Node n where n.label = :label");
				query.setString("label", label);
				List<Node> nodes = query.list();
				init(nodes);
				
				return nodes;
			}
			
		});
	}
	
	@SuppressWarnings("unchecked")
	public List<Node> findByIds(Collection<Long> ids) {
		if (ids == null || ids.isEmpty()) return new LinkedList<Node>();
		String query = "from Node n where n.id in (:ids)";
		return getHibernateTemplate().findByNamedParam(query, "ids", ids);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Node> loadByIds(final Collection<Long> ids) {
		logger.info("Load nodes by collection of ids...");
		List<Node> nodes = new ArrayList<Node>();
		if (ids == null || ids.size() == 0) return nodes;
		
		nodes = getHibernateTemplate().executeFind(new HibernateCallback(){

			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Criteria c = session.createCriteria(Node.class);
				c.add(Restrictions.in("id", ids));
				c.setFetchMode("attributes", FetchMode.JOIN);
				c.setFetchMode("longAttributes", FetchMode.JOIN);
				c.setFetchMode("roles", FetchMode.JOIN);
				c.setFetchMode("groups", FetchMode.JOIN);
				c.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);				
				List<Node> nodes = c.list();											
				return nodes;
			}
			
		});
		
		/* the init() method is very costy
		if (ids == null || ids.size() == 0) return new LinkedList<Node>();
		nodes = getHibernateTemplate().executeFind(new HibernateCallback(){

			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				
				Query query = session.createQuery("from Node n where n.id in (:ids)");
				query.setParameterList("ids", ids);
				List<Node> nodes = query.list();								
				init(nodes);				
				return nodes;
			}
			
		});
		*/
		
		/* this "distinct" keyword does not work, at least in this version of hibernate
		String query = "select distinct n from Node n " +
						"left join fetch n.attributes " +
						"left join fetch n.longAttributes " +
						"left join fetch n.roles " +
						"left join fetch n.groups " + 
						"where n.id in (:ids)";
		nodes = getHibernateTemplate().findByNamedParam(query, "ids", ids);
		*/
		
		logger.debug(nodes.size() + " nodes loaded.");
		return nodes;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<Node> findByUsernames(Collection<String> usernames) {
		String query = "from Node n where n.username in (:usernames)";		
		List<Node> nodes = (List<Node>) getHibernateTemplate().findByNamedParam(query, "usernames", usernames);
		return nodes;
	}
	
    @SuppressWarnings("unchecked")
	public List<Node> findByType(String type){
    	logger.info("find nodes by type: " + type);
    	
    	String query = "from Node n WHERE n.type = :type";
		List<Node> list = getHibernateTemplate().findByNamedParam(query, "type", type);
		
		logger.info("found " + list.size() + " nodes.");
		return list;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Node> loadByType(final String type) {
		logger.info("load nodes by type...");
		
		return getHibernateTemplate().executeFind(new HibernateCallback(){

			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query query = session.createQuery("from Node n where n.type = :type");
				query.setString("type", type);
				List<Node> nodes = query.list();
				init(nodes);
				
				return nodes;
			}
			
		});
	}
	
	/**
	 * Query nodes by criteria
	 * Parameters in request map:
	 * type				- node type
	 * username			- node username
	 * label			- node label
	 * 
	 * order			- sort order (asc/desc)
	 * orderProperty	- sort property
	 * 
	 * firstResult		- the first result to return
	 * maxResult		- the max results to return
	 * 
	 */
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List<Node> findNodesByCriteria( final Map request){
		logger.info("Get nodes by criteria...");
		HibernateTemplate ht = getHibernateTemplate();
		
		List<Node> nodes = (List<Node>)ht.execute(new HibernateCallback(){

			public Object doInHibernate(Session session){
				Criteria c = getSearchCriteria(session, request);								
				c = getComplexCriteria(c, request);				
				return c.list();
			}
			
		});
		
		logger.debug(nodes.size() + " nodes retrieved.");
		return nodes;
	}
	
	@SuppressWarnings("unchecked")
	public List<Node> findEnabledUser(){
		return (List<Node>)getHibernateTemplate().find("from Node n where n.enabled=true and n.type = 'user'");
	}
	
	@SuppressWarnings("unchecked")
	public List<Node> findConnected(final Collection<Node> nodes, boolean includeDerivedEdges) {
		if (nodes == null || nodes.size() == 0) return new LinkedList<Node>();
		logger.info("find outgoing nodes");
		
		String directQuery = "select e.toNode from Edge e where e.fromNode in (:nodes)";
		if (!includeDerivedEdges) directQuery += " and e.type not like 'd.%'";
		String undirectQuery = "select e.fromNode from Edge e where e.directed = :directed and e.toNode in (:nodes)";
		if (!includeDerivedEdges) undirectQuery += " and e.type not like 'd.%'";
		
		HibernateTemplate ht = getHibernateTemplate();
		List<Node> directList = (List<Node>) ht.findByNamedParam(directQuery, "nodes", nodes);
		logger.debug("completed: " + directQuery);
		List<Node> undirectList = (List<Node>) ht.findByNamedParam(undirectQuery, 
												new String[]{"directed", "nodes"}, 
												new Object[]{Boolean.FALSE, nodes});
		logger.debug("completed: " + undirectQuery);
		for (Node n : undirectList){
			if (!directList.contains(n)) directList.add(n);
		}
		
		logger.info("find outgoing nodes, done");
		return directList;
	}


	@SuppressWarnings("unchecked")
	public List<Node> findConnecting(final Collection<Node> nodes, boolean includeDerivedEdges) {
		if (nodes == null || nodes.size() == 0)
			return new LinkedList<Node>();
		logger.info("find incoming nodes");
		
		String directQuery = "select e.fromNode from Edge e where e.toNode in (:nodes)";
		if (!includeDerivedEdges) directQuery += " and e.type not like 'd.%'";
		String undirectQuery = "select e.toNode from Edge e where e.directed = :directed and e.fromNode in (:nodes)";
		if (!includeDerivedEdges) undirectQuery += " and e.type not like 'd.%'";
		
		HibernateTemplate ht = getHibernateTemplate();
		List<Node> directList = (List<Node>) ht.findByNamedParam(directQuery, "nodes", nodes);
		logger.debug("completed: " + directQuery);
		List<Node> undirectList = (List<Node>) ht.findByNamedParam(undirectQuery, 
												new String[]{"directed", "nodes"}, 
												new Object[]{Boolean.FALSE, nodes});
		logger.debug("completed: " + undirectQuery);
		for (Node n : undirectList){
			if (!directList.contains(n)) directList.add(n);
		}
		logger.info("find incoming nodes. done");
		return directList;
	}
	
	@SuppressWarnings("unchecked")
	public Set<Node> findNeighbors(Collection<Node> nodes, boolean includeDerivedEdges, boolean includeEmptyEdges, Collection<String> edgeTypes) {
		logger.info("find neighbor nodes...");
		Set<Node> neighbors = new HashSet<Node>();
		
		if (nodes != null && !nodes.isEmpty()) {		
			String fromQuery = "select e.toNode from Edge e where e.fromNode in (:nodes)";
			if (!includeDerivedEdges) fromQuery += " and e.type not like 'd.%'";
			if (!includeEmptyEdges) fromQuery += " and e.weight > " + Float.MIN_VALUE;
			if (edgeTypes != null && !edgeTypes.isEmpty()) fromQuery += " and e.type in (:edgeTypes)";
			
			String toQuery = "select e.fromNode from Edge e where e.toNode in (:nodes)";
			if (!includeDerivedEdges) toQuery += " and e.type not like 'd.%'";
			if (!includeEmptyEdges) toQuery += " and e.weight > " + Float.MIN_VALUE;
			if (edgeTypes != null && !edgeTypes.isEmpty()) toQuery += " and e.type in (:edgeTypes)";
			
			HibernateTemplate ht = getHibernateTemplate();
			List<Node> fromList;
			if (edgeTypes == null || edgeTypes.isEmpty()) fromList = (List<Node>) ht.findByNamedParam(fromQuery, "nodes", nodes);
			else fromList = (List<Node>) ht.findByNamedParam(fromQuery, new String[]{"nodes", "edgeTypes"}, new Object[]{nodes, edgeTypes});
			neighbors.addAll(fromList);
			logger.debug("completed: " + fromQuery);
			List<Node> toList;
			if (edgeTypes == null || edgeTypes.isEmpty()) toList = (List<Node>) ht.findByNamedParam(toQuery, "nodes", nodes);
			else toList = (List<Node>) ht.findByNamedParam(toQuery, new String[]{"nodes", "edgeTypes"}, new Object[]{nodes, edgeTypes});
			neighbors.addAll(toList);
			logger.debug("completed: " + toQuery);
			
			// remove self
			neighbors.removeAll(nodes);
		}
		
		logger.info("found " + neighbors.size() + " neighbors.");
		return neighbors;
	}
	
	@SuppressWarnings("unchecked")
	public List<Node> findTagsByQuestion(Question question) {
		String query = "from Node n where n.type = 'tag' and n.username like :shortName";
		List<Node> nodes = getHibernateTemplate().findByNamedParam(query, "shortName", question.getShortName() + Constants.SEPERATOR + "%");		
		return nodes;
	}
	
	@SuppressWarnings("unchecked")
	public List<Node> getAll() {
		logger.info("get all nodes...");
		String query = "from Node n order by n.id asc";
		List<Node> nodes = getHibernateTemplate().find(query);
		logger.info("got " + nodes.size() + " nodes.");
		
		return nodes;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Node> loadAll() {
		logger.info("loading all nodes");
		
		return getHibernateTemplate().executeFind(new HibernateCallback(){

			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				
				List<Node> nodes = session.createQuery("from Node n order by n.id asc").list();										
				init(nodes);
				
				logger.info(nodes.size() + " nodes loaded.");
				return nodes;
			}
			
		});
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Long> getNodeIdsWithAttribute(final String attrName) {		
		return getHibernateTemplate().executeFind(new HibernateCallback(){

			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Connection con = session.connection();
				PreparedStatement ps;
				List<Long> nodeIds = new LinkedList<Long>();
				
				String query = "select distinct node_id from node_attributes where attr_key=?";				
				ps = con.prepareStatement(query);
				ps.setString(1, attrName);
				ResultSet rs = ps.executeQuery();
				while (rs.next()){
					nodeIds.add(rs.getLong(1));
				}
				
				return nodeIds;
			}
			
		});
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public List<Long> getNodeIdsByCriteria( final Map request){
		logger.info("Get nodeIds by criteria...");
		HibernateTemplate ht = getHibernateTemplate();
		
		List<Long> nodeIds = (List<Long>)ht.execute(new HibernateCallback(){

			public Object doInHibernate(Session session){
				Criteria c = getSearchCriteria(session, request);				
				c = getComplexCriteria(c, request);
				
				// only need the nodeId
				c.setProjection(Projections.id());
				
				return c.list();
			}
			
		});
		
		logger.debug(nodeIds.size() + " nodeIds retrieved.");
		return nodeIds;
	}
	
	public int getCount() {
		return (Integer) getHibernateTemplate().find("select count(*) from Node").get(0);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public int getNodesCountByCriteria(final Map request) {
		logger.info("Get nodes count by criteria...");
		HibernateTemplate ht = getHibernateTemplate();
		
		Integer count = (Integer)ht.execute(new HibernateCallback(){

			public Object doInHibernate(Session session){
				Criteria c = getSearchCriteria(session, request);
				c.setProjection(Projections.rowCount());
				Integer count = (Integer) c.list().get(0);
				return count;
			}
			
		});	
		logger.debug("Count=" + count);
		return count;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<String> getNodeTypes() {
		logger.info("get node types...");
		List list = getHibernateTemplate().find("select distinct n.type from Node n");
		logger.info("got " + list.size() + " nodeTypes.");
		return (List<String>) list;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<String> getAttributeNames(final Collection<String> nodeTypes) {
		HibernateTemplate ht = getHibernateTemplate();
		
		final StringBuilder sb = new StringBuilder();
		if (nodeTypes != null && !nodeTypes.isEmpty()){
			String query = "SELECT n.id FROM Node n WHERE n.type IN (:nodeTypes)";
			List<Long> nodeIds = ht.findByNamedParam(query, "nodeTypes", nodeTypes);
			
			int i = 0;
			for (Long nodeId : nodeIds){
				if (i > 0) sb.append(",");
				sb.append(nodeId);
				i++;
			}
			logger.info(i + " nodes found for nodeTypes: " + nodeTypes);
		}
		
		List<String> attrNames = (List<String>)ht.execute(new HibernateCallback(){

			public Object doInHibernate(Session session){
				Connection con = session.connection();
				PreparedStatement ps;
				List<String> attrNames = new LinkedList<String>();
				try {
					
					String sql = "SELECT DISTINCT attr_key FROM node_attributes";	
					if (nodeTypes != null && !nodeTypes.isEmpty()){
						sql += " WHERE node_id in (" + sb.toString() + ")";
					}
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
					
					String sql = "SELECT DISTINCT attr_key FROM node_long_attributes";
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
					
					String sql = "SELECT DISTINCT attr_value FROM node_attributes where attr_key = ?";
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
	
	@SuppressWarnings("unchecked")
	public List<String> getPropertyValues(String name) {
		String query = "SELECT DISTINCT " + name + " FROM Node ORDER BY " + name + " ASC";
		List<String> values = (List<String>)getHibernateTemplate().find(query);
		List<String> validValues = new ArrayList<String>();
		for (String value : values){
			if (value != null && value.trim().length() > 0){
				validValues.add(value);
			}
		}
		return validValues;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void deleteAttributeByKey(final String key) {

		HibernateTemplate ht = getHibernateTemplate();
		ht.execute(new HibernateCallback(){

			public Object doInHibernate(Session session){
				Connection con = session.connection();
				PreparedStatement ps;
				
				try {
					
					String sql = "DELETE FROM node_attributes WHERE attr_key=?";
					logger.debug("deleting attributes: " + sql);
					ps = con.prepareStatement(sql);
					ps.setString(1, key);
					ps.executeUpdate();

					logger.debug("node attributes with key=" + key + " are deleted.");
				} catch (SQLException e) {					
					e.printStackTrace();
				}

				return null;
			}
			
		});	
	}

	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void deleteLongAttributeByKey(final String key) {

		HibernateTemplate ht = getHibernateTemplate();
		ht.execute(new HibernateCallback(){

			public Object doInHibernate(Session session){
				Connection con = session.connection();
				PreparedStatement ps;
				
				try {
					
					String sql = "DELETE FROM node_long_attributes WHERE attr_key=?";
					logger.debug("deleting long attributes: " + sql);
					ps = con.prepareStatement(sql);
					ps.setString(1, key);
					ps.executeUpdate();

					logger.debug("node long attributes with key=" + key + " are deleted.");
				} catch (SQLException e) {					
					e.printStackTrace();
				}

				return null;
			}
			
		});	
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<Long, Float> getScoreMapByAttrName(final String attrName) {	
		logger.info("get score map by attrName: " + attrName);
		String questionShortName = Question.getShortNameFromKey(attrName);
		Beans.init();
		QuestionDao questionDao = (QuestionDao)Beans.getBean("questionDao");
		final Question q = questionDao.findByShortName(questionShortName);
		
		List list =  (List) getHibernateTemplate().executeFind(new HibernateCallback(){

			public Object doInHibernate(Session session) throws SQLException{
				List<Map<Long, Float>> list = new ArrayList<Map<Long, Float>>();
				Map<Long, Float> scoreMap = new HashMap<Long, Float>();
				Connection con = session.connection();
				PreparedStatement ps;								
				String query = "select node_id, attr_value from node_attributes where attr_key=?";	
				ps = con.prepareStatement(query);
				ps.setString(1, attrName);
				ResultSet rs = ps.executeQuery();
				float max = Float.MIN_VALUE;
				while (rs.next()){
					Long nodeId = rs.getLong(1);
					String value = rs.getString(2);
					Float score = 1.0f;
					if (q.isChoice() || q.isMultipleChoice()){}
					else if (q.isRating() || q.isMultipleRating()){
						String scaleName = Question.getScaleNameFromKey(value);
						Scale scale = q.getScaleByName(scaleName);
						score = scale.getValue().floatValue();
					} else if (q.isContinuous()){
						score = Float.parseFloat(value);
					} else if (q.isDuration()){
						String[] parts = value.split(":", -1);
						score = Float.parseFloat(parts[0]);
					} else {
						logger.warn("Unsupported question type: " + q.getType());
						continue;
					}
					
					if (score < Float.MIN_VALUE) continue;
					
					if (max < score) max = score;
					
					scoreMap.put(nodeId, score);
				}
				
				// for rating and multiple rating question, max is the max scale value
				if (q.isRating() || q.isMultipleRating()){
					for (Scale s : q.getScales()){
						Float score = s.getValue().floatValue();
						if (max < score) max = score;
					}
				}
				
				// normalization
				for (Long nodeId : scoreMap.keySet()){
					scoreMap.put(nodeId, scoreMap.get(nodeId)/max);
				}
				
				list.add(scoreMap);
				
				return list;
			}
			
		});
		
		return (Map<Long, Float>)list.get(0);
	}
	

	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, Map> getAllPlainNodes() {
		
		HibernateTemplate ht = getHibernateTemplate();
		return (Map<String, Map>) ht.execute(new HibernateCallback(){
			public Object doInHibernate(Session session){
				logger.info("get all plain nodes...");				
				
				//String sql = "SELECT node_id, label, type, username, organization, department, unit FROM nodes";				
				String sql = "SELECT node_id, label, type, username FROM nodes";
				logger.debug("sql: " + sql);
				SQLQuery query = session.createSQLQuery(sql);
				query.addScalar("node_id", Hibernate.LONG);
				query.addScalar("label", Hibernate.STRING);
				query.addScalar("type", Hibernate.STRING);				
				query.addScalar("username", Hibernate.STRING);
				/* retrieve at runtime for contact chooser
				query.addScalar("organization", Hibernate.STRING);
				query.addScalar("department", Hibernate.STRING);
				query.addScalar("unit", Hibernate.STRING);
				*/
				logger.debug("querying ...");
				List list = query.list();
				
				logger.debug("converting to nodes map.");
				Map<String, Map> nodeMap = new HashMap<String, Map>();
				for (Object row : list){
					Object[] items = (Object[]) row;
					Map node = new HashMap();
					Long nodeId = (Long)items[0];
					node.put("nodeId", nodeId);
					node.put("label", items[1]==null?"":items[1].toString());
					node.put("type", items[2]==null?"":items[2].toString());
					node.put("username", items[3].toString());
					/*
					node.put("organization", items[4]==null?"":items[4].toString());
					node.put("department", items[5]==null?"":items[5].toString());
					node.put("unit", items[6]==null?"":items[6].toString());
					*/
					nodeMap.put(nodeId.toString(), node);
				}
				
				logger.info(nodeMap.size() + " nodes retrived.");
				return nodeMap;
			}
			
		});	
		
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, Map> getPlainNodesByIds(List<String> attributes, List<Long> nodeIds){
		logger.info("get plain nodes by node ids...");
		Map<String, Map> nodeMap = new HashMap<String, Map>();
		if (attributes.isEmpty() || nodeIds.isEmpty()){
			logger.warn("attributes and nodeIds cannot be empty.");
			return nodeMap;
		}
		if (!attributes.contains("node_id")) attributes.add("node_id");
		
		Beans.init();
		DataSource ds = (DataSource)Beans.getBean("dataSource");
		
		
	    Connection conn = null;
	    PreparedStatement stmt = null;
	    ResultSet rs = null;

	    StringBuilder sb = new StringBuilder();
	    sb.append("SELECT ");
	    for (int i=0; i<attributes.size(); i++){
	    	String attribute = attributes.get(i);
	    	if (i>0){
	    		sb.append(",");
	    	}
	    	sb.append(attribute);
	    }
	    sb.append(" FROM nodes WHERE node_id in (");
	    for (int j=0; j<nodeIds.size(); j++){
	    	if (j>0) sb.append(",");
	    	sb.append(nodeIds.get(j));
	    }
	    sb.append(") ORDER BY node_id");
	    String query = sb.toString();
	    
	    try {
	      conn = ds.getConnection();
	      stmt = conn.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	      stmt.setFetchDirection(ResultSet.FETCH_FORWARD);
	      stmt.setFetchSize(1000);

	      logger.info("Executing SQL query: " + query);
	      rs = stmt.executeQuery();

	      logger.info("examing result set...");
	      while (rs.next()){
	    	  Map m = new HashMap();
	    	  String node_id = null;
	    	  for (int k=0; k<attributes.size(); k++){
	    		  String attribute = attributes.get(k);
	    		  String value = rs.getObject(k+1).toString();
	    		  if (attribute.equals("node_id")) node_id = value; 
	    		  m.put(attribute, value);
	    	  }
	    	  
	    	  nodeMap.put(node_id, m);
	    	  //logger.debug(m);
	      }
	      
	      
	    } catch (SQLException sqle) {
	      logger.warn(sqle.getMessage());
	      sqle.printStackTrace();
	    } finally {
	      IOUtils.quietClose(rs, stmt, conn);
	    }
	    
	    return nodeMap;
	}
	


	
	// initialize (fetch=subselect, lazy=true)	
	private void init(List<Node> nodes){
		if (!nodes.isEmpty()){			
			Node node = nodes.get(0);
			node.getAttributes().isEmpty();
			node.getLongAttributes().isEmpty();
			node.getGroups().isEmpty();
			node.getRoles().isEmpty();
		}
	}

	
	@SuppressWarnings("rawtypes")
	private Criteria getSearchCriteria(Session session, Map request){
		Criteria c = session.createCriteria(Node.class);
		String nodeType = (String)request.get("type");
		String username = (String)request.get("username");
		String label = (String)request.get("label");
		if (nodeType != null) c.add(Restrictions.eq("type", nodeType));
		if (username != null) c.add(Restrictions.ilike("username", username, MatchMode.ANYWHERE));
		if (label != null) c.add(Restrictions.ilike("label", label, MatchMode.ANYWHERE));
		
		return c;
	}
	
	@SuppressWarnings("rawtypes")
	private Criteria getComplexCriteria(Criteria c, Map request){		
		// sorting
		String orderString = (String)request.get("order");
		if (orderString != null){
			String orderProperty = (String) request.get("orderProperty");
			if (orderProperty == null) orderProperty = "label";
			
			if (orderString.equals("asc")) c.addOrder(Order.asc(orderProperty));
			else c.addOrder(Order.desc(orderProperty));
		}
		
		// paging
		String firstResultString = (String)request.get("firstResult");
		if (firstResultString != null){
			c.setFirstResult(Integer.parseInt(firstResultString));
		}		
		String maxResultString = (String)request.get("maxResult");
		if (maxResultString != null){
			c.setMaxResults(Integer.parseInt(maxResultString));
		}
		
		// more criteria?
		
		return c;
	}

}
