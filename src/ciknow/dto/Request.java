package ciknow.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request {
	public Map data = new HashMap();
	
	public NodeDTO node;
	public List<NodeDTO> nodes;
	
	public EdgeDTO edge;		
	public List<EdgeDTO> edges;
	public List<EdgeDTO> incomingEdges;
	public List<EdgeDTO> outgoingEdges;
	public List<EdgeDTO> createdEdges; // not include taggings
	public List<EdgeDTO> nodeTaggings;
	//public List<String> edgeTypes;
	//public Map edgeTypeDescriptions = new HashMap();
			
	public GroupDTO group;
	public List<GroupDTO> groups;

	public RoleDTO role;
	public List<RoleDTO> roles;
	
	public ContactFieldDTO contactField;
	public List<ContactFieldDTO> contactFields;
	
	public SurveyDTO survey;
	public List<SurveyDTO> surveys;
	
	public QuestionDTO question;
	public List<QuestionDTO> questions;
}
