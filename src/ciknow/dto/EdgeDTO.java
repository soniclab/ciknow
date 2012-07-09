package ciknow.dto;

import ciknow.domain.Edge;

import java.util.Map;
import java.util.HashMap;

/**
 * User: gyao
 * Date: Mar 8, 2008
 * Time: 12:01:51 PM
 */
public class EdgeDTO {
    public Long edgeId;
    public Long version;
    public Long creatorId;
    public Long fromNodeId;
    public Long toNodeId;
    public String type;
    public Double weight;
    public Boolean directed;
    public Map<String, String> attributes = new HashMap<String, String>();
    public Map<String, String> longAttributes = new HashMap<String, String>();
    
    public EdgeDTO() {
    }

    public EdgeDTO(Edge e){
        edgeId = e.getId();
        version = e.getVersion();
        creatorId = e.getCreator()!=null?e.getCreator().getId():null;
        fromNodeId = e.getFromNode().getId();
        toNodeId = e.getToNode().getId();
        type = e.getType();
        weight = e.getWeight();
        directed = e.isDirected();
        attributes.putAll(e.getAttributes());
        //longAttributes.putAll(e.getLongAttributes());
    }
}
