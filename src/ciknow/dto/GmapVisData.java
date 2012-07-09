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

public class GmapVisData {
	public List<Map> nodes; // combination of UserDTO and NodeDTO
	public List<EdgeDTO> edges;
	
	private static Log logger = LogFactory.getLog(GmapVisData.class);
	
	public GmapVisData(){
		nodes = new ArrayList<Map>();
		edges = new ArrayList<EdgeDTO>();
	}
	
	public GmapVisData(Collection<Node> nodeList, Collection<Edge> edgeList){
		
        nodes = new ArrayList<Map>();
        for (Node n : nodeList){
        	Map node = new HashMap();
        	node.put("nodeId", n.getId());
        	node.put("firstName", n.getFirstName());
        	node.put("lastName", n.getLastName());
        	node.put("username", n.getUsername());
        	node.put("addr1", n.getAddr1());
        	node.put("addr2", n.getAddr2());
        	node.put("city", n.getCity());
        	node.put("state", n.getState());
        	node.put("zipcode", n.getZipcode());
        	node.put("country", n.getCountry());
        	node.put("type", n.getType());
        	
        	nodes.add(node);
        }
        
        edges = new ArrayList<EdgeDTO>();
        for (Edge edge : edgeList){
        	edges.add(new EdgeDTO(edge));
        }
	}
}
