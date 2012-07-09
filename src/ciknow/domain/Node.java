package ciknow.domain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Node implements java.io.Serializable {
	public static final String DEFAULT_NODE_LABEL = "Default Label";
	public static final String DEFAULT_NODE_TYPE = "defaultNodeType";
	private static final long serialVersionUID = 8095476466063370888L;
	
	private Long id;
	private Long version;
	private String label = DEFAULT_NODE_LABEL;		// required
	private String type = DEFAULT_NODE_TYPE;	// required
	private String uri = "-";
	private String username;	// required
	private String password = "";
	private String firstName = "";
	private String lastName = "";
	private String midName = "";
	private String addr1 = "";
	private String addr2 = "";
	private String city = "";
	private String state = "";
	private String country = "";
	private String zipcode = "";
	private String email = "";
	private String phone = "";
	private String cell = "";
	private String fax = "";
    private String department = "";
    private String organization = "";
    private String unit = "";
    private Boolean enabled = true;	//required
    private Map<String, String> attributes = new HashMap<String, String>();
    private Map<String, String> longAttributes = new HashMap<String, String>();
    private Set<Role> roles = new HashSet<Role>();
	private Set<Group> groups = new HashSet<Group>();
	
    public Node() {
	}

    public Node(Node n){
        update(n);
    }
    
    public void update(Node n){
        id = n.id;
        version = n.version;
        label = n.label;
        type = n.type;
        uri = n.uri;
        username = n.username;
        password = n.password;
        firstName = n.firstName;
        lastName = n.lastName;
        midName = n.midName;
        addr1 = n.addr1;
        addr2 = n.addr2;
        city = n.city;
        state = n.state;
        country = n.country;
        zipcode = n.zipcode;
        email = n.email;
        phone = n.phone;
        cell = n.cell;
        fax = n.fax;
        department = n.department;
        organization = n.organization;
        unit = n.unit;
        enabled = n.enabled;
        attributes = new HashMap<String, String>(n.getAttributes());
        longAttributes = new HashMap<String, String>(n.getLongAttributes());
        roles = new HashSet<Role>(n.getRoles());
        groups = new HashSet<Group>(n.getGroups());
    }
    
    /*
     * Used in GraphmlReader.java
     * 
     */
    public void overrideBy(Node n){
        label = n.label;
        type = n.type;
        uri = n.uri;
        username = n.username;
        password = n.password;
        firstName = n.firstName;
        lastName = n.lastName;
        midName = n.midName;
        addr1 = n.addr1;
        addr2 = n.addr2;
        city = n.city;
        state = n.state;
        country = n.country;
        zipcode = n.zipcode;
        email = n.email;
        phone = n.phone;
        cell = n.cell;
        fax = n.fax;
        department = n.department;
        organization = n.organization;
        unit = n.unit;
        enabled = n.enabled;
        attributes.putAll(n.getAttributes());
        longAttributes.putAll(n.getLongAttributes());
        roles.addAll(n.getRoles());
        groups.addAll(n.getGroups());
    }
    
    public boolean isAdmin(){
    	if (username.equalsIgnoreCase("admin")) return true;
    	
    	for (Role role : roles){
    		if (role.getId().equals(1L)) return true;
    	}
    	return false;
    }
    
    public boolean isHidden(){
    	for (Role role : roles){
    		if (role.getId().equals(2L)) return true;
    	}
    	return false;
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

	public String getLabel() {
		return this.label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUri() {
		return this.uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	
    public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getMidName() {
		return midName;
	}

	public void setMidName(String midName) {
		this.midName = midName;
	}

	public String getAddr1() {
		return addr1;
	}

	public void setAddr1(String addr1) {
		this.addr1 = addr1;
	}

	public String getAddr2() {
		return addr2;
	}

	public void setAddr2(String addr2) {
		this.addr2 = addr2;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getZipcode() {
		return zipcode;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getCell() {
		return cell;
	}

	public void setCell(String cell) {
		this.cell = cell;
	}

	public String getFax() {
		return fax;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
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
    
    public Set<Role> getRoles() {
		return this.roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	public Set<Group> getGroups() {
		return this.groups;
	}

	public void setGroups(Set<Group> groups) {
		this.groups = groups;
	}
	
    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("Node(id=").append(id).append(", ");
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		Node other = (Node) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		result = prime * result
//				+ ((username == null) ? 0 : username.hashCode());
//		return result;
//	}
//
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (obj == null)
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//		final Node other = (Node) obj;
//		if (username == null) {
//			if (other.username != null)
//				return false;
//		} else if (!username.equals(other.username))
//			return false;
//		return true;
//	}


}
