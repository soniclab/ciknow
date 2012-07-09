package ciknow.dto;

import ciknow.domain.Role;

public class RoleDTO {
	public Long roleId;
	public Long version;
	public String name;
	
	public RoleDTO(){
		
	}
	
	public RoleDTO(Role role){
		roleId = role.getId();
		version = role.getVersion();
		name = role.getName();
	}
}


