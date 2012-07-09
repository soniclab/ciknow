package ciknow.dao.hibernate;

import ciknow.dao.ActivityDao;
import ciknow.dao.NodeDao;
import ciknow.domain.Activity;
import ciknow.domain.Node;
import ciknow.util.Beans;
import java.sql.SQLException;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 *
 * @author gyao
 */
public class ActivityHibernateDao extends HibernateDaoSupport implements ActivityDao {

    private static Log logger = LogFactory.getLog(ActivityHibernateDao.class);

    public static void main(String[] args) {
        testSave();
    }

    private static void testSave() {
        Beans.init();
        NodeDao nodeDao = (NodeDao) Beans.getBean("nodeDao");
        ActivityDao activityDao = (ActivityDao) Beans.getBean("activityDao");
        Node fromNode = nodeDao.findById(1L);
        Node toNode = nodeDao.findById(2L);
        int size = 100;
        Random r = new Random();

        for (int i = 0; i < size; i++) {
            Activity a = new Activity();
            a.setSubject(fromNode);
            a.setPredicate(r.nextBoolean() ? "login" : "logout");
            a.setObject(r.nextBoolean() ? toNode : null);

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, Math.round(-100 * r.nextFloat()));
            a.setTimestamp(cal.getTime());

            activityDao.save(a);
        }

        logger.info(activityDao.getAll().size() + " activities inserted.");
    }

    @Override
    public void save(Activity activity) {
        getHibernateTemplate().saveOrUpdate(activity);
    }

	@Override
	public void delete(Collection<Activity> acts) {
		getHibernateTemplate().deleteAll(acts);
	}
	
    @SuppressWarnings("rawtypes")
    @Override
    public Activity getLatestActivity() {
        HibernateTemplate ht = getHibernateTemplate();
        List list = ht.executeFind(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session)
                    throws HibernateException, SQLException {
                String hql = "from Activity order by timestamp desc";
                Query query = session.createQuery(hql);
                query.setMaxResults(1);
                List list = query.list();
                return list;
            }
        });

        if (list == null || list.isEmpty()) {
            return null;
        } else {
            return (Activity) list.get(0);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public List<Activity> getActivitiesBefore(final Date date) {
        HibernateTemplate ht = getHibernateTemplate();
        List<Activity> activities = (List<Activity>) ht.executeFind(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session)
                    throws HibernateException, SQLException {
                String hql = "from Activity where timestamp < :t";
                Query query = session.createQuery(hql);
                query.setTimestamp("t", date);
                return query.list();
            }
        });

        return activities;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public List<Activity> getActivitiesAfter(final Date date) {
        HibernateTemplate ht = getHibernateTemplate();
        List<Activity> activities = (List<Activity>) ht.executeFind(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session)
                    throws HibernateException, SQLException {
                String hql = "from Activity where timestamp >= :t";
                Query query = session.createQuery(hql);
                query.setTimestamp("t", date);
                return query.list();
            }
        });

        return activities;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public List<Activity> getActivitiesBetween(final Date begin, final Date end) {
        HibernateTemplate ht = getHibernateTemplate();
        List<Activity> activities = (List<Activity>) ht.executeFind(new HibernateCallback() {

            @Override
            public Object doInHibernate(Session session)
                    throws HibernateException, SQLException {
                String hql = "from Activity where timestamp >= :begin and timestamp < :end";
                Query query = session.createQuery(hql);
                query.setTimestamp("begin", begin);
                query.setTimestamp("end", end);
                return query.list();
            }
        });

        return activities;
    }

    @Override
    public List<Activity> getAll() {
        return getHibernateTemplate().loadAll(Activity.class);
    }

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Activity> getActivitiesBySubject(Node subject) {
		String query = "from Activity a where a.subject = :subject";
		List list = getHibernateTemplate().findByNamedParam(query, "subject", subject);
		return list;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List<Activity> getActivitiesByObject(Node object) {
		String query = "from Activity a where a.object = :object";
		List list = getHibernateTemplate().findByNamedParam(query, "object", object);
		return list;
	}
}
