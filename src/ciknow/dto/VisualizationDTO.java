package ciknow.dto;

import java.util.*;

import ciknow.dao.GroupDao;
import ciknow.dao.NodeDao;
import ciknow.domain.*;
import ciknow.util.Beans;

public class VisualizationDTO {
	public Long visId;
	public Long version;
	public Long creatorId;
	public String name;
	public String label;
	public String type;
	public String networkType;
	public String data; // This DTO only transfer data for type of "query, not "layout". 
	public Boolean valid = true;
	public Date timestamp;	
	public Set groups = new HashSet();
	public Set nodes = new HashSet();
    public Map<String, String> attributes = new HashMap<String, String>();
	
	public VisualizationDTO(){
		
	}
	
	public VisualizationDTO(Visualization s){
		visId = s.getId();
		version = s.getVersion();
		creatorId = s.getCreator().getId();
		name = s.getName();
		label = s.getLabel();
		type = s.getType();
		networkType = s.getNetworkType();
		if (type.equals("query")) data = s.getData();
		valid = s.isValid();
		timestamp =s.getTimestamp();
		for (Group group : s.getGroups()){
			groups.add(group.getId());
		}
		for (Node node : s.getNodes()){
			nodes.add(node.getId());
		}
        attributes.putAll(s.getAttributes());
	}
	
	public Visualization toVisualization(){
		Beans.init();
		NodeDao nodeDao = (NodeDao)Beans.getBean("nodeDao");
		GroupDao groupDao = (GroupDao)Beans.getBean("groupDao");
		Visualization vis = new Visualization();
		vis.setId(visId);
		vis.setVersion(version);
		vis.setName(name);
		vis.setLabel(label);
		vis.setCreator(nodeDao.findById(creatorId));
		vis.setNetworkType(networkType);
		vis.setType(type);
		vis.setData(data);
		vis.setValid(valid);
		vis.setTimestamp(timestamp);
		Set<Group> groupSet = new HashSet<Group>();
		for (Object groupId : groups){
			Group group = groupDao.findById(Long.parseLong(groupId.toString()));
			groupSet.add(group);
		}
		vis.setGroups(groupSet);
		Set<Node> nodeSet = new HashSet<Node>();
		for (Object nodeId : nodes){
			Node node = nodeDao.findById(Long.parseLong(nodeId.toString()));
			nodeSet.add(node);
		}
		vis.setNodes(nodeSet);
		vis.setAttributes(attributes);
		return vis;
	}

    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("VisualizationDTO[visId=").append(visId).append(",");
        sb.append("version=").append(version).append(",");
        sb.append("creatorId=").append(creatorId).append(",");
        sb.append("name=").append(name).append(",");
        sb.append("label=").append(label).append(",");
        sb.append("type=").append(type).append(",");
        sb.append("networkType=").append(networkType).append(",");
        sb.append("valid=").append(valid).append(",");
        sb.append("timestamp=").append(timestamp).append(",");
        sb.append("attributes=").append(attributes).append("]");
        return sb.toString();
    }
}
