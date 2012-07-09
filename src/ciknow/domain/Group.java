package ciknow.domain;

import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author gyao
 */
public class Group implements java.io.Serializable {

    private static final long serialVersionUID = -6631209837206026482L;
	public static final String PRIVATE_PREFIX = "UG_";
	public static final String MANDATORY_PREFIX = "MG_";
	public static final String PROVIDER_PREFIX = "CP_";
	
    private Long id;
    private Long version;
    private String name;
    private Set<Node> nodes = new HashSet<Node>(0);

    public Group() {
    }

    // for contact chooser
    public boolean isPrivate() {
        if (name.startsWith(PRIVATE_PREFIX)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isMandatory() {
        if (name.startsWith(MANDATORY_PREFIX)) {
            return true;
        } else {
            return false;
        }
    }

    // for contact provider
    public boolean isProvider() {
        if (name.startsWith(PROVIDER_PREFIX)) {
            return true;
        } else {
            return false;
        }
    }
    
    public static String getPrivateGroupName(String username, String questionShortName) {
        return PRIVATE_PREFIX + username.trim() + "_" + questionShortName.trim();
    }

    public static String getProviderGroupName(String username, String questionShortName) {
        return PROVIDER_PREFIX + username.trim() + "_" + questionShortName.trim();
    }
    
    public Group(String name) {
        this.name = name;
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

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Node> getNodes() {
        return nodes;
    }

    public void setNodes(Set<Node> nodes) {
        this.nodes = nodes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Group group = (Group) o;

        if (name != null ? !name.equals(group.name) : group.name != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return (name != null ? name.hashCode() : 0);
    }
}
