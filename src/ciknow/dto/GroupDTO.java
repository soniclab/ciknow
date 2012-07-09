package ciknow.dto;


import ciknow.domain.Group;

public class GroupDTO {
	public Long groupId;
	public Long version;
	public String name;
	
	public GroupDTO(){
		
	}
	
	public GroupDTO(Group group){
		groupId = group.getId();
		version = group.getVersion();
		name = group.getName();
	}
}
