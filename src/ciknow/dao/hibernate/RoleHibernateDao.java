package ciknow.dao.hibernate;

import ciknow.dao.RoleDao;
import ciknow.domain.Role;
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
public class RoleHibernateDao extends HibernateDaoSupport implements RoleDao {

    private static Log logger = LogFactory.getLog(RoleHibernateDao.class);

    public static void main(String[] args) {
        Beans.init();
        RoleDao roleDao = (RoleDao) Beans.getBean("roleDao");
        Long id = 3L;

        List<Long> nodeIds = roleDao.getNodeIdsByRoleId(id);
        for (Long nodeId : nodeIds) {
            logger.debug("nodeId: " + nodeId);
        }
        logger.info("there are " + nodeIds.size() + " nodes with role (id=" + id + ")");
    }

    @Override
    public void delete(Role r) {
        getHibernateTemplate().delete(r);
    }

    @Override
    public void delete(Collection<Role> rs) {
        getHibernateTemplate().deleteAll(rs);
    }

    @Override
    public void deleteAll() {
        getHibernateTemplate().bulkUpdate("delete Role");
    }

    public Role getProxy(Long id) {
        return (Role) getHibernateTemplate().load(Role.class, id);
    }

    public Role findById(Long id) {
        return (Role) getHibernateTemplate().get(Role.class, id);
    }

    public Role loadById(Long id) {
        String query = "from Role r left join fetch r.nodes where r.id = :id";
        List<Role> roles = getHibernateTemplate().findByNamedParam(query, "id", id);
        if (roles != null && !roles.isEmpty()) {
            return (Role) roles.get(0);
        } else {
            return null;
        }
    }

    @Override
    public Role findByName(String name) {
        String query = "from Role r where r.name = :name";
        List<Role> roles = getHibernateTemplate().findByNamedParam(query, "name", name);
        if (roles != null && !roles.isEmpty()) {
            return (Role) roles.get(0);
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public Role loadByName(String name) {
        String query = "from Role r left join fetch r.nodes where r.name = :name";
        List<Role> roles = getHibernateTemplate().findByNamedParam(query, "name", name);
        if (roles != null && !roles.isEmpty()) {
            return (Role) roles.get(0);
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Long> getNodeIdsByRoleId(final Long id) {
        HibernateTemplate ht = getHibernateTemplate();
        return (List<Long>) ht.execute(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session) {
                String sql = "SELECT node_id FROM node_role WHERE role_id=?";
                SQLQuery query = session.createSQLQuery(sql);
                query.setLong(0, id);
                query.addScalar("node_id", Hibernate.LONG);
                List list = query.list();
                return list;
            }
        });
    }

    public void updateNodesInRole(final Long roleId, final Collection<Long> nodeIds) {
        logger.info("updating nodes in role (id=" + roleId + ")");
        HibernateTemplate ht = getHibernateTemplate();
        ht.execute(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session) {
                Connection con = session.connection();
                PreparedStatement ps;

                try {

                    String sql = "DELETE FROM node_role WHERE role_id=?";
                    logger.debug("sql: " + sql);
                    ps = con.prepareStatement(sql);
                    ps.setLong(1, roleId);
                    ps.executeUpdate();


                    sql = "INSERT INTO node_role(node_id, role_id) VALUES(?, ?)";
                    logger.debug("sql: " + sql);
                    ps = con.prepareStatement(sql);
                    ps.setLong(2, roleId);
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
        logger.info("updating nodes in role (id=" + roleId + "), done");
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Role> getAll() {
        return getHibernateTemplate().loadAll(Role.class);
    }

    @Override
    public void save(Role r) {
        getHibernateTemplate().saveOrUpdate(r);
    }

    @Override
    public void save(Collection<Role> rs) {
        getHibernateTemplate().saveOrUpdateAll(rs);
    }
}
