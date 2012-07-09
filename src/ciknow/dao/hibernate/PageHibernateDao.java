package ciknow.dao.hibernate;

import ciknow.dao.PageDao;
import ciknow.domain.Page;
import java.util.Collection;
import java.util.List;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * 
 * @author gyao
 */
public class PageHibernateDao extends HibernateDaoSupport implements PageDao {

    @Override
    public void save(Page page) {
        getHibernateTemplate().saveOrUpdate(page);
    }

    @Override
    public void save(Collection<Page> pages) {
        getHibernateTemplate().saveOrUpdateAll(pages);
    }

    @Override
    public void delete(Page page) {
        getHibernateTemplate().delete(page);
    }

    @Override
    public void delete(Collection<Page> pages) {
        getHibernateTemplate().deleteAll(pages);
    }

    @Override
    public void deleteAll() {
        getHibernateTemplate().bulkUpdate("delete Page");
    }

    @Override
    public Page findById(Long id) {
        return (Page) getHibernateTemplate().get(Page.class, id);
    }

    @Override
    public List<Page> getAll() {
        return getHibernateTemplate().loadAll(Page.class);
    }

    @Override
    public int getCount() {
        return (Integer) getHibernateTemplate().find("select count(*) from Page").get(0);
    }
}
