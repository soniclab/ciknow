package ciknow.dao.hibernate;

import ciknow.dao.GroupDao;
import ciknow.domain.Group;
import ciknow.util.Beans;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 *
 * @author gyao
 */
public class GroupHibernateDao extends HibernateDaoSupport implements GroupDao {

    private static Log logger = LogFactory.getLog(GroupHibernateDao.class);

    public static void main(String[] args) {
        Beans.init();
        GroupDao groupDao = (GroupDao) Beans.getBean("groupDao");

        //testCreate(groupDao);
        testUpdate(groupDao);
        //testLoadById(groupDao);
        //testFindById(groupDao);
        //testGetUserIdsByGroupId(groupDao);		
    }

    private static void testCreate(GroupDao groupDao) {
        Group g = new Group();
        g.setName("test");
        groupDao.save(g);
    }

    private static void testUpdate(GroupDao groupDao) {
        Group g = groupDao.findByName("test");
        g.setName("test again");
        groupDao.save(g);
    }

    @Override
    public void delete(Group g) {
        getHibernateTemplate().delete(g);
    }

    @Override
    public void delete(Collection<Group> gs) {
        getHibernateTemplate().deleteAll(gs);
    }

    @Override
    public void deleteAll() {
        getHibernateTemplate().bulkUpdate("delete Group");
    }

    public Group getProxy(final Long id) {
        return (Group) getHibernateTemplate().load(Group.class, id);
    }

    public Group findById(final Long id) {
        return (Group) getHibernateTemplate().get(Group.class, id);
    }

    public Group loadById(Long id) {
        String query = "from Group g left join fetch g.nodes where g.id = :id";
        List<Group> groups = getHibernateTemplate().findByNamedParam(query, "id", id);
        if (groups != null && !groups.isEmpty()) {
            return (Group) groups.get(0);
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Group> getAll() {
        return getHibernateTemplate().loadAll(Group.class);
    }

    @Override
    public void save(Group g) {
        getHibernateTemplate().saveOrUpdate(g);
    }

    @Override
    public void save(Collection<Group> gs) {
        getHibernateTemplate().saveOrUpdateAll(gs);
    }

    @Override
    public Group findByName(String name) {
        return findByName(name, false);
    }

    @SuppressWarnings("rawtypes")
	@Override
    public Group findByName(String name, boolean autoCreate) {
        String query = "from Group g where g.name = :name";
        List groups = getHibernateTemplate().findByNamedParam(query, "name", name);
        if (groups != null && !groups.isEmpty()) {
            return (Group) groups.get(0);
        } else {
        	if (autoCreate){
        		Group group = new Group();
        		group.setName(name);
        		save(group);
        		return group;
        	} else return null;
        }
    }
    
    @Override
    public Group loadByName(String name) {
        String query = "from Group g left join fetch g.nodes where g.name = :name";
        List groups = getHibernateTemplate().findByNamedParam(query, "name", name);
        if (groups != null && !groups.isEmpty()) {
            return (Group) groups.get(0);
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Long> getNodeIdsByGroupId(final Long id) {
        HibernateTemplate ht = getHibernateTemplate();
        return (List<Long>) ht.execute(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session) {
                String sql = "SELECT node_id FROM node_group WHERE group_id=?";
                SQLQuery query = session.createSQLQuery(sql);
                query.setLong(0, id);
                query.addScalar("node_id", Hibernate.LONG);
                List list = query.list();
                return list;
            }
        });
    }

    @SuppressWarnings("unchecked")
    public void updateNodesInGroup(final Long groupId, final Collection<Long> nodeIds) {
        logger.info("updating nodes in group (id=" + groupId + ")");
        HibernateTemplate ht = getHibernateTemplate();
        ht.execute(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session) {
                Connection con = session.connection();
                PreparedStatement ps;

                try {

                    String sql = "DELETE FROM node_group WHERE group_id=?";
                    logger.debug("sql: " + sql);
                    ps = con.prepareStatement(sql);
                    ps.setLong(1, groupId);
                    ps.executeUpdate();


                    sql = "INSERT INTO node_group(node_id, group_id) VALUES(?, ?)";
                    logger.debug("sql: " + sql);
                    ps = con.prepareStatement(sql);
                    ps.setLong(2, groupId);
                    int count = 0;
                    int total = 0;
                    for (Long nodeId : nodeIds) {
                        ps.setLong(1, nodeId);
                        ps.executeUpdate();

                        count++;
                        total++;
                        if (count == 5000) {
                            logger.debug(total + " nodes inserted");
                            count = 0;
                        }
                    }
                    logger.debug(total + " nodes inserted");
                } catch (SQLException e) {
                    logger.error(e.getMessage());
                }

                return null;
            }
        });
        logger.info("updating nodes in group (id=" + groupId + "), done");
    }
}
