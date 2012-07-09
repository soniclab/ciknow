package ciknow.dao.hibernate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import ciknow.domain.Node;
import ciknow.domain.Metric;
import ciknow.dao.MetricDao;
import ciknow.dao.NodeDao;
import ciknow.util.Beans;
import ciknow.util.Constants;

/**
 * 
 * @author gyao
 *
 */
public class MetricHibernateDao extends HibernateDaoSupport implements MetricDao {
	private static Log logger = LogFactory.getLog(MetricHibernateDao.class);
	
	public static void main(String[] args){
		Beans.init();
		NodeDao nodeDao = (NodeDao)Beans.getBean("nodeDao");
		MetricDao metricDao = (MetricDao) Beans.getBean("metricDao");
		//testSave(nodeDao, metricDao);
		//testDelete(metricDao);
		testFind(nodeDao, metricDao);
	}
	
	private static void testSave(NodeDao nodeDao, MetricDao metricDao){
		Metric metric = new Metric();
		metric.setFromNode(nodeDao.getProxy(1L));
		metric.setToNode(nodeDao.getProxy(4L));
		metric.setType(Constants.ALG_PEARSON);
		metric.setSource("helllo:york");
		metric.setSymmetric(true);
		metric.setValue(0.5f);
		System.out.println(metric);
		metricDao.save(metric);
	}
	
	private static void testDelete(MetricDao metricDao){
		metricDao.delete(Constants.ALG_EUCLIDEAN, "user:user");
	}
	
	private static void testFind(NodeDao nodeDao, MetricDao metricDao){
		List<Node> nodes = new ArrayList<Node>();
		nodes.add(nodeDao.getProxy(119L));
		//nodes.add(nodeDao.getProxy(353L));
		//nodes.add(nodeDao.getProxy(354L));
		List<Metric> metrics = metricDao.findByNodesAndTypeAndSource(nodes, Constants.ALG_EUCLIDEAN, "user:user");
		for (Metric m : metrics){
			logger.info(m);
		}
	}
	
    public void save(Metric rec) {
        getHibernateTemplate().saveOrUpdate(rec);
    }

    public void save(Collection<Metric> recs) {
        getHibernateTemplate().saveOrUpdateAll(recs);
    }

    public void delete(Metric rec) {
        getHibernateTemplate().delete(rec);
    }

    public void delete(Collection<Metric> recs) {
        getHibernateTemplate().deleteAll(recs);
    }

    public void deleteAll() {
        getHibernateTemplate().bulkUpdate("delete Metric");
    }

	public Metric findById(Long id) {
		return (Metric)getHibernateTemplate().get(Metric.class, id);
	}

	@SuppressWarnings("unchecked")
	public List<Metric> getAll() {
		return getHibernateTemplate().loadAll(Metric.class);
	}

	public int getCount() {
		return (Integer) getHibernateTemplate().find("select count(*) from Metric").get(0);
	}

	public void delete(String metricType, String source) {
		logger.info("deleting metric (type=" + metricType + ", source=" + source + ")");
		String query = "delete Metric m where m.type = ? and m.source = ?";
		getHibernateTemplate().bulkUpdate(query, new Object[]{metricType, source});
	}

	@SuppressWarnings("unchecked")
	public List<Metric> findByNodesAndTypeAndSource(Collection<Node> nodes,
			String type, String source) {
		String query;
		List<Metric> metrics;
		if (type.equals(Constants.ALG_SIMILARITY)){
			query = "from Metric m where m.source = :source and m.type like :type and " +
			"(m.fromNode in (:nodes) or (m.symmetric = true and m.toNode in (:nodes)))";
			metrics = getHibernateTemplate().findByNamedParam(query, 
					new String[]{"nodes", "type", "source"}, 
					new Object[]{nodes, Constants.PREFIX_SIMILARITY + "%", source});
		} else {
			query = "from Metric m where m.source = :source and m.type = :type and " +
			"(m.fromNode in (:nodes) or (m.symmetric = true and m.toNode in (:nodes)))";
			metrics = getHibernateTemplate().findByNamedParam(query, 
					new String[]{"nodes", "type", "source"}, 
					new Object[]{nodes, type, source});
		}
		
		return metrics;
	}

	public void delete(final String rowType, String colType, String type,  String source) {
		logger.info("deleting metrics (row=" + rowType + ", col=" + colType 
									+ ", metricType=" + type + ", source=" + source + ")");
		
		// this doesn't work, syntax error in mysql
//		String query = "delete Metric m where ((m.fromNode.type = ? and m.toNode.type = ?) " +
//											"or (m.symmetric = true and m.fromNode.type = ? and m.toNode.type = ?)) " +
//											"and m.source = ?";
//		getHibernateTemplate().bulkUpdate(query, new Object[]{rowType, colType, colType, rowType, source});

		// this is a workaround, may not be efficient
		String query = "from Metric m where m.source = :source and m.type = :type and " +
											"((m.fromNode.type = :rowType and m.toNode.type = :colType) " +
											"or (m.symmetric = true and m.fromNode.type = :colType and m.toNode.type = :rowType)) ";
		List<Metric> metrics = getHibernateTemplate().findByNamedParam(query, new String[]{"rowType", "colType", "type", "source"}, new Object[]{rowType, colType, type, source});
		delete(metrics);
	}
	
	public void delete(final String rowType, String colType,  String source) {
		logger.info("deleting metrics (row=" + rowType + ", col=" + colType 
									 + ", source=" + source + ")");

		String query = "from Metric m where m.source = :source and " +
											"((m.fromNode.type = :rowType and m.toNode.type = :colType) " +
											"or (m.symmetric = true and m.fromNode.type = :colType and m.toNode.type = :rowType)) ";
		List<Metric> metrics = getHibernateTemplate().findByNamedParam(query, new String[]{"rowType", "colType", "source"}, new Object[]{rowType, colType, source});
		delete(metrics);
		logger.info(metrics.size() + " metrics deleted.");
	}

	public void delete(String source) {
		String query = "delete Metric m where m.source = ?";
		getHibernateTemplate().bulkUpdate(query, source);
	}

	@SuppressWarnings("unchecked")
	public List<Metric> findByNodeAndSource(Long nodeId, String source) {
		String query = "from Metric m where source = :source and (m.fromNode.id = :nodeId or (m.symmetric=true and m.toNode.id = :nodeId))";
		return getHibernateTemplate().findByNamedParam(query, new String[]{"nodeId", "source"}, new Object[]{nodeId, source});
	}

//	@SuppressWarnings("unchecked")
//	public List<Metric> findByNode(Node node) {
//		String query = "from Metric r where r.fromNode = :from or r.toNode= :to";
//		return getHibernateTemplate().findByNamedParam(query, new String[]{"from", "to"}, new Object[]{node, node});
//	}
//
//	@SuppressWarnings("unchecked")
//	public List<Metric> findByNodeAndSource(Node node, String source) {
//		String query = "from Metric r where (r.fromNode = :from or r.toNode = :to) and r.source = :source";
//		return getHibernateTemplate().findByNamedParam(query, new String[]{"from", "to", "source"} ,new Object[]{node, node, source});
//	}
//
//	@SuppressWarnings("unchecked")
//	public List<Metric> findByFromAndToNode(Node from, Node to) {
//		String query = "from Metric r where (r.fromNode = :from and r.toNode=:to) or (r.fromNode = :to and r.toNode= :from)";
//		return getHibernateTemplate().findByNamedParam(query, new String[]{"from", "to"}, new Object[]{from, to});
//	}
//
//	@SuppressWarnings("unchecked")
//	public Metric findByFromToNodeAndSource(Node from, Node to, String source) {
//		String query = "from Metric r where ((r.fromNode = :from and r.toNode=:to) or (r.fromNode=:to and r.toNode=:from)) and r.source=:source";
//		List<Metric> list = (List<Metric>) getHibernateTemplate().findByNamedParam(query, new String[]{"from", "to", "source"}, new Object[]{from, to, source});
//		if (list.isEmpty()) return null;
//		else return  list.get(0);
//	}
}
