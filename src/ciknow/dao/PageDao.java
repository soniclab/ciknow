package ciknow.dao;

import ciknow.domain.Page;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author gyao
 */
public interface PageDao {

    public void save(Page page);

    public void save(Collection<Page> pages);

    public void delete(Page page);

    public void delete(Collection<Page> pages);

    public void deleteAll();

    public List<Page> getAll();

    public int getCount();

    public Page findById(Long id);
}
