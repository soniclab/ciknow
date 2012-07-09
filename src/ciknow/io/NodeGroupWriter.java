package ciknow.io;

import java.io.Writer;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ciknow.dao.GroupDao;
import ciknow.dao.NodeDao;
import ciknow.domain.Group;
import ciknow.domain.Node;

public class NodeGroupWriter{
	private static Log logger = LogFactory.getLog(NodeGroupWriter.class);
	private NodeDao nodeDao;
	private GroupDao groupDao;
	
	public NodeGroupWriter() {
		super();
	}

	public NodeDao getNodeDao() {
		return nodeDao;
	}

	public void setNodeDao(NodeDao nodeDao) {
		this.nodeDao = nodeDao;
	}

	public GroupDao getGroupDao() {
		return groupDao;
	}

	public void setGroupDao(GroupDao groupDao) {
		this.groupDao = groupDao;
	}

	public void write(Writer writer) throws Exception{
        logger.info("exporting groups...");
        StringBuffer sb = new StringBuffer();

        // find users 
        List<Node> nodes = nodeDao.loadAll();
        for (Node node : nodes) {
            sb.append(node.getUsername());
            Set<Group> groups = node.getGroups();
            for (Group group : groups){
            	sb.append("\t").append(group.getName());
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
