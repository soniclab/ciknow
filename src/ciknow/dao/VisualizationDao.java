package ciknow.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import ciknow.domain.Node;
import ciknow.domain.Visualization;

public interface VisualizationDao {
	public void save(Visualization visualization);
	public void save(Collection<Visualization> visualizations);
	public void delete(Visualization visualization);
	public void delete(Collection<Visualization> visualizations);
	public void deleteAll();
		
	public List<Visualization> getAll();
	public int getCount();
	public Visualization findById(Long id);
	public List<Visualization> findByIds(Collection<Long> ids);
	public List<Visualization> findByCreator(Node creator);
	public List<Visualization> findByCreatorAndName(Node creator, String name);
	public Set<Long> getVisIdsByNodeId(Long nodeId);
}
