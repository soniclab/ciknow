package ciknow.dao;

import ciknow.domain.Scale;
import java.util.Collection;
import java.util.List;

/**
 * 
 * @author gyao
 */
public interface ScaleDao {

    public void save(Scale scale);

    public void save(Collection<Scale> scales);

    public Scale findById(Long id);

    public List<Scale> getAll();
}
