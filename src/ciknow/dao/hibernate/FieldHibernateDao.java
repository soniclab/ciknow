package ciknow.dao.hibernate;

import ciknow.dao.FieldDao;
import ciknow.domain.Field;
import java.util.Collection;
import java.util.List;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * 
 * @author gyao
 */
public class FieldHibernateDao extends HibernateDaoSupport implements FieldDao {

    @Override
    public void save(Field field) {
        getHibernateTemplate().saveOrUpdate(field);
    }

    @Override
    public void save(Collection<Field> fields) {
        getHibernateTemplate().saveOrUpdateAll(fields);
    }

    @Override
    public Field findById(Long id) {
        return (Field) getHibernateTemplate().get(Field.class, id);
    }

    @Override
    public List<Field> getAll() {
        return getHibernateTemplate().loadAll(Field.class);
    }
}
