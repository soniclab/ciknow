package ciknow.ro;

import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ciknow.dao.NodeDao;
import ciknow.dto.VisualizationDTO;

public class VisualizationRO {
	private static Log logger = LogFactory.getLog(VisualizationRO.class);

	NodeDao NodeDao;

    public VisualizationRO(){

	}

    public NodeDao getNodeDao() {
        return NodeDao;
    }

    public void setNodeDao(NodeDao NodeDao) {
        this.NodeDao = NodeDao;
    }

    public List<VisualizationDTO> getVisualizationsByNode(Long NodeId){
    	List<VisualizationDTO> list = new ArrayList<VisualizationDTO>();
    	return list;
    }
}
