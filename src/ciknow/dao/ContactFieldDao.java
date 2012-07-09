package ciknow.dao;

import ciknow.domain.ContactField;
import java.util.Collection;
import java.util.List;

/**
 * 
 * @author gyao
 */
public interface ContactFieldDao {

    public void save(ContactField contactField);

    public void save(Collection<ContactField> contactFields);

    public ContactField findById(Long id);

    public List<ContactField> getAll();
}
