package ciknow.dao.hibernate;

import ciknow.dao.ContactFieldDao;
import ciknow.domain.ContactField;
import java.util.Collection;
import java.util.List;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 *
 * @author gyao
 */
public class ContactFieldHibernateDao extends HibernateDaoSupport implements ContactFieldDao {

    @Override
    public void save(ContactField contactField) {
        getHibernateTemplate().saveOrUpdate(contactField);
    }

    @Override
    public void save(Collection<ContactField> contactFields) {
        getHibernateTemplate().saveOrUpdateAll(contactFields);
    }

    @Override
    public ContactField findById(Long id) {
        return (ContactField) getHibernateTemplate().get(ContactField.class, id);
    }

    @Override
    public List<ContactField> getAll() {
        return getHibernateTemplate().loadAll(ContactField.class);
    }
}
