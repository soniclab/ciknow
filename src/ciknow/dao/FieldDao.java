package ciknow.dao;

import ciknow.domain.Field;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author gyao
 */
public interface FieldDao {

    public void save(Field field);

    public void save(Collection<Field> fields);

    public Field findById(Long id);

    public List<Field> getAll();
}
