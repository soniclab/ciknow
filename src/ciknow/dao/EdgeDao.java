package ciknow.dao;

import java.util.*;

import ciknow.domain.Edge;
import ciknow.domain.Node;
import ciknow.domain.Question;


/**
 * Data access interface for Node
 * @author gyao
 *
 */

public interface EdgeDao{
	public void save(Edge edge);
	public void save(Collection<Edge> edges);
	public void delete(Edge edge);
	public void delete(Collection<Edge> edges);

    // QUERY
	public Edge findById(Long id);
	public Edge loadById(Long id);
	public Edge getProxy(Long id);
	
    public List<Edge> findByCreatorId(Long id);
    public List<Edge> loadByCreatorId(Long id);
    
	public List<Edge> findByFromNodeId(Long id);
	public List<Edge> loadByFromNodeId(Long id);
	
    public List<Edge> findByToNodeId(Long id);
    public List<Edge> findByToNodeIds(Collection<Long> ids);
    public List<Edge> loadByToNodeId(Long id);
    
    public List<Edge> findByFromToNodeId(Long fromId, Long toId);
    public List<Edge> loadByFromToNodeId(Long fromId, Long toId);
    
	public List<Edge> findByType(String type, boolean includeEmptyEdges);
	public List<Edge> loadByType(String type, boolean includeEmptyEdges);
	
	public List<Edge> findByQuestion(Question question);
	public List<Edge> loadByQuestion(Question question);
	// node can be fromNode or creator, depending on question type
	public List<Edge> loadByQuestionAndNode(Question question, Node node);
	
	public List<Edge> findEdgesAmongNodes(Collection<Node> nodes);
	public List<Edge> loadEdgesAmongNodes(Collection<Node> nodes, boolean includeDerivedEdges, boolean includeEmptyEdges, Collection<String> edgeTypes);

	public List<Edge> getAll();
	public List<Edge> loadAll();

	public int getCount();
	
	public List<String> getEdgeTypes();
	public List<String> getEdgeTypesByNodeType(String nodetType);
	public List<String> getEdgeTypesByNodeTypes(List<String> nodeTypes, int direction);
	public List<String> getEdgeTypesAmongNodeTypes(List<String> nodeTypes);
	public Collection<String> getOtherNodeTypesByNodeTypes(List<String> nodeTypes, int direction);
	public Collection<String> getNodeTypesByEdgeTypes(Collection<String> edgeTypes);
	public List<Map<String, String>> getEdgeTypesByFromAndToNodeTypes(String fromNodeType, String toNodeType);

	public List<String> getAttributeNames();
	public List<String> getLongAttributeNames();	
	public List<String> getAttributeValues(String name);
}
