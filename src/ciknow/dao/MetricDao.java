package ciknow.dao;

import java.util.List;
import java.util.Collection;

import ciknow.domain.Metric;
import ciknow.domain.Node;

/**
 * 
 * @author gyao
 *
 */
public interface MetricDao {
	public void save(Metric metric);
	public void save(Collection<Metric> metrics);
	public void delete(Metric metric);
	public void delete(Collection<Metric> metrics);
    public void deleteAll();
    public void delete(String metricType, String source);
    public void delete(String rowType, String colType, String metricType, String source);
    public void delete(String rowType, String colType,  String source);
	public void delete(String source);
	
	// query
	public Metric findById(Long id);
	public List<Metric> getAll();
	public int getCount();
	public List<Metric> findByNodesAndTypeAndSource(Collection<Node> nodes, String type, String source);
	public List<Metric> findByNodeAndSource(Long nodeId, String source);
//	public List<Metric> findByNode(Node node);
//	public List<Metric> findByNodeAndSource(Node node, String source);
//	public List<Metric> findByFromAndToNode(Node from, Node to);
//	public Metric findByFromToNodeAndSource(Node from, Node to, String source);

}
