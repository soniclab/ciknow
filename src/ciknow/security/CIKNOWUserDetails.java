package ciknow.security;

import ciknow.domain.Node;
import ciknow.domain.Role;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.UserDetails;

/**
 *
 * @author gyao
 */
public class CIKNOWUserDetails implements UserDetails {

    private static final long serialVersionUID = 5376329304842140187L;
    private Node node;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;

    public CIKNOWUserDetails(Node node) {
        this(node, true, true, true);
    }

    public CIKNOWUserDetails(Node node, boolean accountNonExpired, boolean accountNonLocked, boolean credentialsNonExpired) {
        if (node == null || node.getUsername() == null
                || node.getUsername().equals("") || node.getPassword() == null) {
            throw new IllegalArgumentException("username or password cannot be empty.");
        }
        this.node = node;
        this.accountNonExpired = accountNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.credentialsNonExpired = credentialsNonExpired;
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new LinkedList<GrantedAuthority>();
        Set<Role> roles = node.getRoles();
        if (roles == null) {
            return authorities;
        }

        for (Role role : roles) {
            authorities.add(new GrantedAuthorityImpl(role.getName()));
        }

        return authorities;
    }

    @Override
    public String getPassword() {
        return node.getPassword();
    }

    @Override
    public String getUsername() {
        return node.getUsername();
    }

    @Override
    public boolean isEnabled() {
        return node.getEnabled();
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

//	public Long[] getOwnedNodes(){
//		Set<Node> ownedNodes = node.getOwnedNodes();
//		if (ownedNodes == null) return null;
//		
//		Long[] ownedNodeIds = new Long[ownedNodes.size()];
//		int i=0; 
//		for (Node node : ownedNodes){
//			ownedNodeIds[i] = node.getId();
//			i++;
//		}
//		return ownedNodeIds;
//	}
    public String[] getRoles() {
        Set<Role> roles = node.getRoles();
        if (roles == null) {
            return null;
        }
        String[] roleArray = new String[roles.size()];
        int i = 0;
        for (Role role : roles) {
            roleArray[i] = role.getName();
            i++;
        }
        return roleArray;
    }

    public boolean hasRole(String role) {
        Set<Role> roles = node.getRoles();
        for (Role r : roles) {
            if (r.getName().equals(role)) {
                return true;
            }
        }
        return false;
    }

    public Long getId() {
        return node.getId();
    }

    public Node getNode() {
        return node;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CIKNOWUserDetails[nodeId=").append(node.getId()).append(",");
        sb.append("username=").append(node.getUsername()).append(",");
        sb.append("]");
        return sb.toString();
    }
}
