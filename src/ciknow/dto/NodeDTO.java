package ciknow.dto;

import ciknow.domain.Group;
import ciknow.domain.Node;
import ciknow.domain.Role;

import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

/**
 * User: gyao
 * Date: Mar 9, 2008
 * Time: 10:02:00 AM
 */
public class NodeDTO {
	public Long nodeId;
	public Long version;
	public String label;
	public String type;
	public String uri;
	
	public String username;
	public String password;
	public String firstName;
	public String lastName;
	public String midName;
	public String addr1;
	public String addr2;
	public String city;
	public String state;
	public String country;
	public String zipcode;
	public String email;
	public String phone;
	public String cell;
	public String fax;
    public String department;
    public String organization;
    public String unit;
    public Boolean enabled;
    
	public Set roles = new HashSet();
	public Set groups = new HashSet();
	
    public Map<String, String> attributes = new HashMap<String, String>();
    public Map<String, String> longAttributes = new HashMap<String, String>();
    
    public NodeDTO() {

    }

    public NodeDTO(Node n){
        nodeId = n.getId();
        version = n.getVersion();
        label = n.getLabel();
        type = n.getType();
        uri = n.getUri();
        
		username = n.getUsername();
		password = n.getPassword();
		firstName = n.getFirstName();
		lastName = n.getLastName();
		midName = n.getMidName();
		addr1 = n.getAddr1();
		addr2 = n.getAddr2();
		city = n.getCity();
		state = n.getState();
		country = n.getCountry();
		zipcode = n.getZipcode();
		email = n.getEmail();
		phone = n.getPhone();
		cell = n.getCell();
		fax = n.getFax();
        department = n.getDepartment();
        organization = n.getOrganization();
        unit = n.getUnit();
        enabled = n.getEnabled();

		for (Role role : n.getRoles()){
			roles.add(role.getId());
		}
		for (Group group : n.getGroups()){
			groups.add(group.getId());
		}
        attributes.putAll(n.getAttributes());
        longAttributes.putAll(n.getLongAttributes());
    }
    
    public void shallowCopy(Node n){
        nodeId = n.getId();
        version = n.getVersion();
        label = n.getLabel();
        type = n.getType();
        uri = n.getUri();
        
		username = n.getUsername();
		password = n.getPassword();
		firstName = n.getFirstName();
		lastName = n.getLastName();
		midName = n.getMidName();
		addr1 = n.getAddr1();
		addr2 = n.getAddr2();
		city = n.getCity();
		state = n.getState();
		country = n.getCountry();
		zipcode = n.getZipcode();
		email = n.getEmail();
		phone = n.getPhone();
		cell = n.getCell();
		fax = n.getFax();
        department = n.getDepartment();
        organization = n.getOrganization();
        unit = n.getUnit();
        enabled = n.getEnabled();        
    }
    
    public String toString(){
    	StringBuilder sb = new StringBuilder();
        sb.append("NodeDTO(id=").append(nodeId).append(", ");
        sb.append("version=").append(version).append(", ");
        sb.append("username=").append(username).append(", ");
        sb.append("label=").append(label).append(", ");
        sb.append("type=").append(type).append(", ");  
        sb.append("enabled=").append(enabled).append(", ");
        sb.append("attributes=[");
        for (String key : attributes.keySet()){
            String value = attributes.get(key);
            sb.append(key).append(":").append(value).append(",");
        }
        sb.append("])");
    	return sb.toString();
    }
}
