package ciknow.dao.hibernate;

import ciknow.dao.TextFieldDao;
import ciknow.domain.TextField;
import java.util.Collection;
import java.util.List;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 *
 * @author gyao
 */
public class TextFieldHibernateDao extends HibernateDaoSupport implements TextFieldDao {

    @Override
    public void save(TextField textField) {
        getHibernateTemplate().saveOrUpdate(textField);
    }

    @Override
    public void save(Collection<TextField> textFields) {
        getHibernateTemplate().saveOrUpdateAll(textFields);
    }

    @Override
    public TextField findById(Long id) {
        return (TextField) getHibernateTemplate().get(TextField.class, id);
    }

    @Override
    public List<TextField> getAll() {
        return getHibernateTemplate().loadAll(TextField.class);
    }
}
