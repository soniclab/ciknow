package ciknow.io;

import java.io.Writer;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ciknow.dao.NodeDao;
import ciknow.dao.RoleDao;
import ciknow.domain.Node;
import ciknow.domain.Role;

public class NodeRoleWriter{
	private static Log logger = LogFactory.getLog(NodeRoleWriter.class);
	private NodeDao nodeDao;
	private RoleDao roleDao;
	
	public NodeRoleWriter() {
		super();
	}


	public NodeDao getNodeDao() {
		return nodeDao;
	}

	public void setNodeDao(NodeDao nodeDao) {
		this.nodeDao = nodeDao;
	}

	public RoleDao getRoleDao() {
		return roleDao;
	}

	public void setRoleDao(RoleDao roleDao) {
		this.roleDao = roleDao;
	}

	public void write(Writer writer) throws Exception{
        logger.info("exporting roles...");
        StringBuffer sb = new StringBuffer();

        // find users 
        List<Node> users = nodeDao.loadAll();
        for (Node node : users) {
            sb.append(node.getUsername());
            Set<Role> roles = node.getRoles();
            for (Role role : roles){
            	sb.append("\t").append(role.getName());
            }
            sb.append("\n");
        }

        // write to file
        logger.debug(sb.toString());
        writer.write(sb.toString());
        writer.flush();

        logger.info("done.");
	}

}
