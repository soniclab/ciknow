package ciknow.dao.hibernate;

import ciknow.dao.ScaleDao;
import ciknow.domain.Scale;
import java.util.Collection;
import java.util.List;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 *
 * @author gyao
 */
public class ScaleHibernateDao extends HibernateDaoSupport implements ScaleDao {

    @Override
    public void save(Scale scale) {
        getHibernateTemplate().saveOrUpdate(scale);
    }

    @Override
    public void save(Collection<Scale> scales) {
        getHibernateTemplate().saveOrUpdateAll(scales);
    }

    @Override
    public Scale findById(Long id) {
        return (Scale) getHibernateTemplate().get(Scale.class, id);
    }

    @Override
    public List<Scale> getAll() {
        return getHibernateTemplate().loadAll(Scale.class);
    }
}
