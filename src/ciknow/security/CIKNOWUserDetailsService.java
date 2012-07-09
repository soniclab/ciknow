package ciknow.security;

import ciknow.dao.NodeDao;
import ciknow.domain.Node;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * 
 * @author gyao
 */
public class CIKNOWUserDetailsService implements UserDetailsService {

    private NodeDao nodeDao;

    public NodeDao getNodeDao() {
        return nodeDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        this.nodeDao = nodeDao;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
        if (username == null || username.equals("")) {
            throw new UsernameNotFoundException("Node not found.");
        }
        Node node = nodeDao.loadByUsername(username);
        if (node == null || !node.getEnabled()) {
            throw new UsernameNotFoundException("Node " + username + " not found or disabled.");
        }

        /*
         * Set<Role> roles = node.getRoles(); if (roles == null ||
         * roles.isEmpty()) throw new UsernameNotFoundException("Node " +
         * username + " has no authorities.");
         */

        CIKNOWUserDetails userDetails = new CIKNOWUserDetails(node);
        return userDetails;
    }
}
