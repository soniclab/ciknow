package ciknow.dao;

import ciknow.domain.Group;
import java.util.Collection;
import java.util.List;

/**
 * 
 * @author gyao
 */
public interface GroupDao {

    public void save(Group g);

    public void save(Collection<Group> gs);

    public void delete(Group g);

    public void delete(Collection<Group> gs);

    public void deleteAll();

    // QUERY
    public Group getProxy(Long id);	// proxy
    public Group findById(Long id);	// lazy
    public Group loadById(Long id);	// eager
    public Group findByName(String name);
    public Group findByName(String name, boolean autoCreate);
    public Group loadByName(String name);

    public List<Long> getNodeIdsByGroupId(Long id);
    public void updateNodesInGroup(Long groupId, Collection<Long> nodeIds);

    public List<Group> getAll();
    public List<Group> loadAll();
}
