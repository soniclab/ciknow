package ciknow.dao;

import java.util.*;

import ciknow.domain.Node;
import ciknow.domain.Question;


/**
 * Data access interface for Node
 * @author gyao
 *
 */
@SuppressWarnings("rawtypes")
public interface NodeDao{
	public void save(Node node);
	public void save(Collection<Node> nodes);
	public void delete(Node node);
	public void delete(Collection<Node> nodes);
	
	// QUERY
	public Node getProxy(Long id);
	public Node findById(Long id);
	public Node loadById(Long id);
	public Node findByUsername(String username);
	public Node loadByUsername(String username);
	public Node findByEmail(String email);
	
	public List<Node> findByLabel(String label);
	public List<Node> loadByLabel(String label);
	public List<Node> findByIds(Collection<Long> ids);
	public List<Node> loadByIds(Collection<Long> ids);
	public List<Node> findByUsernames(Collection<String> usernames);
    public List<Node> findByType(String type);
    public List<Node> loadByType(String type);
    public List<Node> findNodesByCriteria(Map request);
    public List<Node> findEnabledUser();
    
    public List<Node> matchByLabel(String pattern);
    
	// Finds all nodes the given node links to.
	public List<Node> findConnected(Collection<Node> nodes, boolean includeDerivedEdges);	
	// Finds all nodes connecting to the given node.
	public List<Node> findConnecting(Collection<Node> nodes, boolean includeDerivedEdges);	
	// Find neighbors, regardless direction
	public Set<Node> findNeighbors(Collection<Node> nodes, boolean includeDerivedEdges, boolean includeEmptyEdges, Collection<String> edgeTypes);
	
	// for Perceived Choice/Rating only
    public List<Node> findTagsByQuestion(Question question);
    
	public List<Node> getAll();
	public List<Node> loadAll();

	public List<Long> getNodeIdsWithAttribute(final String attrName);
    public List<Long> getNodeIdsByCriteria(Map request);	

	public int getCount();
	public int getNodesCountByCriteria(Map request);

	public List<String> getNodeTypes();
	
	public List<String> getAttributeNames(Collection<String> nodeTypes);
	public List<String> getLongAttributeNames();
	public List<String> getAttributeValues(String name);
	public List<String> getPropertyValues(String name);	
	
	// for clearing question data
	// remove all rows with attr_key=key from table node_attributes
	public void deleteAttributeByKey(String key);
	public void deleteLongAttributeByKey(String key);
	
	// for recommender
	public Map<Long, Float> getScoreMapByAttrName(final String attrName);
	
	// for flash client
	public Map<String, Map> getAllPlainNodes();
	public Map<String, Map> getPlainNodesByIds(List<String> attributes, List<Long> nodeIds);
}
