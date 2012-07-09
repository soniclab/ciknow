package ciknow.dao;

import ciknow.domain.Role;
import java.util.Collection;
import java.util.List;

/**
 * 
 * @author gyao
 */
public interface RoleDao {

    public void save(Role r);

    public void save(Collection<Role> rs);

    public void delete(Role r);

    public void delete(Collection<Role> rs);

    public void deleteAll();

    // QUERY
    public Role getProxy(Long id);
    public Role findById(Long id);
    public Role loadById(Long id);
    public Role findByName(String name);
    public Role loadByName(String name);

    public List<Long> getNodeIdsByRoleId(Long id);
    public void updateNodesInRole(Long roleId, Collection<Long> nodeIds);

    public List<Role> getAll();
}
