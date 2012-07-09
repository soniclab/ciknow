package ciknow.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Edge implements java.io.Serializable {
	public static final String DEFAULT_EDGE_TYPE = "defaultEdgeType";
	private static final long serialVersionUID = -1253402722571658529L;
	private static final Log logger = LogFactory.getLog(Edge.class);
	
	private Long id;
	private Long version;
	private Node creator;
	private Node toNode;						// required
	private Node fromNode;						// required
	private String type = DEFAULT_EDGE_TYPE;	// required
	private Double weight = 1.0;				// required
	private Boolean directed = true;			// required
    private Map<String, String> attributes = new HashMap<String, String>();
    private Map<String, String> longAttributes = new HashMap<String, String>();
    
    public Edge() {
	}

//    public Edge(Edge edge){
//        id = edge.id;
//        version = edge.version;
//        creator = new Node(edge.creator);
//        toNode = new Node(edge.toNode);
//        fromNode = new Node(edge.fromNode);
//        type = edge.type;
//        weight = edge.weight;
//        directed = edge.directed;
//        attributes.putAll(edge.getAttributes());
//    }

    public Edge(Node creator, Node toNode, Node fromNode, String type,
			Double weight, boolean directed) {
		this.creator = creator;
		this.toNode = toNode;
		this.fromNode = fromNode;
		this.type = type;
		this.weight = weight;
		this.directed = directed;
	}

    /**
     * Merge current edge with specified edge as an undirected edge if possible
     * @param e
     * @return an artificial undirected edge for display and visualization only, not persistent
     */
    public boolean merge(Edge e){
    	if (!this.type.equals(e.getType())) return false;
    	if (!this.isDirected() || !e.isDirected()) return false;
		if ((this.creator == null && e.getCreator() != null) 
			|| (this.creator != null && e.getCreator() == null)
			|| (this.creator != null && e.getCreator() != null && !this.creator.getId().equals(e.getCreator().getId()))) return false;
    	if (!this.fromNode.getId().equals(e.getToNode().getId()) || !this.toNode.getId().equals(e.getFromNode().getId())) return false;

    	setId(-1*id);
    	setVersion(-1L);
    	setDirected(false);
    	
    	return true;
    }
    
    /*
     * merge mutual links in given edges collection
     * return merged edges
     */
    public static List<Edge> merge(Collection<Edge> edges){
		List<Edge> edgeList = new ArrayList<Edge>(edges);
		List<Edge> mergedEdges = new LinkedList<Edge>();
		Set<Edge> tempEdges = new HashSet<Edge>();
		outer:
		for (int i=0; i<edgeList.size(); i++){
			Edge e1 = edgeList.get(i);
			if (tempEdges.contains(e1)) continue;
			for (int j=i+1; j<edgeList.size(); j++){				
				Edge e2 = edgeList.get(j);
				if (tempEdges.contains(e2)) continue;
				boolean merged = e1.merge(e2);
				if (merged){
					mergedEdges.add(e1);
					tempEdges.add(e2);
					continue outer;
				}
			}
		}
		edges.removeAll(tempEdges);
		
		logger.debug(tempEdges.size() + " directed edges are merged into undirected edges");
		
		return mergedEdges;
    }    
    
    public boolean isEmpty(){
    	return (weight < Double.MIN_VALUE);    	
    }
    
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getVersion() {
		return this.version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public Node getCreator() {
		return this.creator;
	}

	public void setCreator(Node creator) {
		this.creator = creator;
	}

	public Node getToNode() {
		return this.toNode;
	}

	public void setToNode(Node toNode) {
		this.toNode = toNode;
	}

	public Node getFromNode() {
		return this.fromNode;
	}

	public void setFromNode(Node fromNode) {
		this.fromNode = fromNode;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Double getWeight() {
		return this.weight;
	}

	public void setWeight(Double weight) {
		this.weight = weight;
	}

	public boolean isDirected() {
		return this.directed;
	}

	public void setDirected(boolean directed) {
		this.directed = directed;
	}

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public String getAttribute(String key){
        return attributes.get(key);
    }

    public void setAttribute(String key, String value){
        attributes.put(key, value);
    }
    
    public Map<String, String> getLongAttributes() {
        return longAttributes;
    }

    public void setLongAttributes(Map<String, String> attributes) {
        this.longAttributes = attributes;
    }

    public String getLongAttribute(String key){
        return longAttributes.get(key);
    }

    public void setLongAttribute(String key, String value){
        longAttributes.put(key, value);
    }
    
    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("Edge(id=").append(id).append(", ");
        sb.append("version=").append(version).append(", ");
        sb.append("creator=").append(creator).append(", ");
        sb.append("fromNodeId=").append(fromNode.getId()).append(", ");
        sb.append("toNodeId=").append(toNode.getId()).append(", ");
        sb.append("type=").append(type).append(", ");
        sb.append("weight=").append(weight).append(", ");
        sb.append("directed=").append(directed).append(", ");
        sb.append("attributes[");
        for (String k : attributes.keySet()){
            sb.append(k).append(":").append(attributes.get(k)).append(", ");
        }
        sb.append("])");
        return sb.toString();
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((creator == null) ? 0 : creator.getId().hashCode());
		result = prime * result
				+ ((directed == null) ? 0 : directed.hashCode());
		result = prime * result
				+ ((fromNode == null) ? 0 : fromNode.getId().hashCode());
		result = prime * result + ((toNode == null) ? 0 : toNode.getId().hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Edge other = (Edge) obj;
		
		if (creator == null) {
			if (other.creator != null)
				return false;
		} else if(other.creator == null){
			if (creator != null)
				return false;
		} else if (!creator.getId().equals(other.creator.getId()))
			return false;
		
		if (directed == null) {
			if (other.directed != null)
				return false;
		} else if (!directed.equals(other.directed))
			return false;
		
		if (fromNode == null) {
			if (other.fromNode != null)
				return false;
		} else if(other.fromNode == null){
			if (fromNode != null)
				return false;
		} else if (!fromNode.getId().equals(other.fromNode.getId()))
			return false;
		
		if (toNode == null) {
			if (other.toNode != null)
				return false;
		} else if(other.toNode == null){
			if (toNode != null)
				return false;
		} else if (!toNode.getId().equals(other.toNode.getId()))
			return false;
		
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		
		return true;
	}       
}
