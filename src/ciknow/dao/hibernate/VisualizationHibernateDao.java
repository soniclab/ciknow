package ciknow.dao.hibernate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ciknow.dao.VisualizationDao;
import ciknow.domain.Node;
import ciknow.domain.Visualization;
import ciknow.util.Beans;

public class VisualizationHibernateDao extends HibernateDaoSupport implements VisualizationDao{
	private static Log logger = LogFactory.getLog(VisualizationHibernateDao.class);
	
	public static void main(String[] args){
		Beans.init();
		VisualizationDao visDao = (VisualizationDao)Beans.getBean("visualizationDao");
		testFindByIds(visDao);
	}
	
	private static void testFindByIds(VisualizationDao visDao){
		List<Long> ids = new LinkedList<Long>();
		ids.add(11L);
		List<Visualization> viss = visDao.findByIds(ids);
		logger.debug("found " + viss.size() + " visualizations.");
	}
	public void save(Visualization visualization) {
		getHibernateTemplate().saveOrUpdate(visualization);
	}

	public void save(Collection<Visualization> visualizations) {
		getHibernateTemplate().saveOrUpdateAll(visualizations);
	}
	
	public void delete(Visualization visualization) {
		getHibernateTemplate().delete(visualization);
	}

	public void delete(Collection<Visualization> visualizations) {
		getHibernateTemplate().deleteAll(visualizations);
	}

	public void deleteAll() {
		getHibernateTemplate().bulkUpdate("delete Visualization");
	}

	public Visualization findById(Long id) {
		return (Visualization)getHibernateTemplate().get(Visualization.class, id);
	}
	
	@SuppressWarnings("unchecked")
	public List<Visualization> findByIds(Collection<Long> ids){		
		List<Visualization> viss = new LinkedList<Visualization>();
		if (ids == null || ids.size() == 0) return viss;
		viss = getHibernateTemplate().findByNamedParam("from Visualization v where v.id in (:ids)", "ids", ids);
		return viss;
	}

	@SuppressWarnings("unchecked")
	public List<Visualization> getAll() {
		return getHibernateTemplate().loadAll(Visualization.class);
	}

	@SuppressWarnings("unchecked")
	public List<Visualization> findByCreator(Node creator) {
		String query = "from Visualization v where v.creator = ?";		
		return getHibernateTemplate().find(query, creator);
	}
	
	@SuppressWarnings("unchecked")
	public List<Visualization> findByCreatorAndName(Node creator, String name) {
		String query = "from Visualization v where v.creator = :creator and v.name = :name";		
		return getHibernateTemplate().findByNamedParam(query, 
														new String[]{"creator", "name"}, 
														new Object[]{creator, name});
	}
	
	public int getCount() {
		return (Integer)getHibernateTemplate().find("select count(*) from Visualization").get(0);
	}

	@SuppressWarnings("unchecked")
	public Set<Long> getVisIdsByNodeId(final Long nodeId) {
		logger.info("get visIds for nodeId=" + nodeId);
		
		HibernateTemplate ht = getHibernateTemplate();
		return (Set<Long>)ht.execute(new HibernateCallback(){

			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Connection con = session.connection();
				PreparedStatement ps;
				Set<Long> visIds = new HashSet<Long>();
				
				try {
					
					String sql = "SELECT group_id FROM node_group WHERE node_id=?";
					logger.debug("sql: " + sql);
					ps = con.prepareStatement(sql);
					ps.setLong(1, nodeId);
					ResultSet rs = ps.executeQuery();
					StringBuilder groupIds = new StringBuilder();
					int count = 0;
					while (rs.next()){
						Long groupId = rs.getLong(1);
						if (count == 0) groupIds.append("'").append(groupId).append("'");
						else groupIds.append(",'").append(groupId).append("'");
						count++;
					}
					ps.close();
					rs.close();
					
					sql = "SELECT vis_id FROM visualization_group WHERE group_id in (" + groupIds + ")";
					logger.debug("sql: " + sql);
					ps = con.prepareStatement(sql);
					rs = ps.executeQuery();
					while (rs.next()){
						Long visId = rs.getLong(1);
						visIds.add(visId);
					}
					
					sql = "SELECT vis_id FROM visualization_node WHERE node_id = ?";
					logger.debug("sql: " + sql);
					ps = con.prepareStatement(sql);
					ps.setLong(1, nodeId);
					rs = ps.executeQuery();
					while (rs.next()){
						Long visId = rs.getLong(1);
						visIds.add(visId);
					}
					
					logger.debug(visIds.size() + " visIds retrieved.");
				} catch (SQLException e) {					
					e.printStackTrace();
				}

				return visIds;
			}
			
		});
	}
}
