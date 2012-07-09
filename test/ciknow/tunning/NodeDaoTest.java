package ciknow.tunning;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ciknow.dao.NodeDao;
import ciknow.domain.Node;
import ciknow.util.Beans;
import ciknow.util.Constants;

/**
 * Measure performance of data access
 * @author gyao
 * got similar results as EdgeDaoTest
 */
public class NodeDaoTest {
	private static final int SIZE = 1000000;
	
	private static final Logger logger = Logger.getLogger(NodeDaoTest.class
			.getName());
	
	public static void main(String[] args) {
		Beans.init("applicationContext-datasource.xml", "classpath:/applicationContext-dao.xml");
		NodeDao nodeDao = (NodeDao)Beans.getBean("nodeDao");
		
		saveWithBatch(nodeDao);
	}
	
	private static void saveWithBatch(NodeDao nodeDao) {
		List<Node> nodes = new ArrayList<Node>();
		
		logger.info("creating sample edges...");
		int count = 0;
		for (int i=0; i<SIZE; i++){
			Node node = new Node();
			node.setUsername("tunninguser" + i);
			
			nodes.add(node);
			
			count++;
			if (count % Constants.HIBERNATE_BATCH_SIZE == 0) {
				nodeDao.save(nodes);
				nodes = new ArrayList<Node>();
				//logger.info(total);						
			}
		}
		
		logger.info(count);
		nodeDao.save(nodes);
		logger.info("done");
	}
}
