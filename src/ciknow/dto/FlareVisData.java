package ciknow.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ciknow.domain.Edge;
import ciknow.domain.Node;

@SuppressWarnings("unchecked")
public class FlareVisData {
	
	public List<Map> nodes; // combination of UserDTO and NodeDTO
	public List<EdgeDTO> edges;
	
	private static Log logger = LogFactory.getLog(FlareVisData.class);
	
	public FlareVisData(){
		nodes = new ArrayList<Map>();
		edges = new ArrayList<EdgeDTO>();
	}
	
	public FlareVisData(Collection<Node> nodeList, Collection<Edge> edgeList){
		
        nodes = new ArrayList<Map>();
        for (Node n : nodeList){
        	Map node = new HashMap();
        	node.put("nodeId", n.getId());
        	node.put("type", n.getType());
        	node.put("label", n.getLabel());        	
        	nodes.add(node);
        }
        
        edges = new ArrayList<EdgeDTO>();
        for (Edge edge : edgeList){
        	edges.add(new EdgeDTO(edge));
        }
	}
}
