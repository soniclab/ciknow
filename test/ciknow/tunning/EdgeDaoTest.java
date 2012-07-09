package ciknow.tunning;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import ciknow.dao.EdgeDao;
import ciknow.dao.NodeDao;
import ciknow.domain.Edge;
import ciknow.domain.Node;
import ciknow.util.Beans;
import ciknow.util.Constants;

/**
 * Measure performance of data access
 * @author gyao
 * After extensive trials, batch size of 2500 ~ 50000 will give similar results:
 * insert 100000 records in 32~34 seconds
 * Smaller batch size will open/close more hibernate sessions, but save memeory
 * Larger batch size use less sessions but use more memory
 * So suggested batch size for edge creation is 5000 ~ 10000
 */
public class EdgeDaoTest {
	private static final int SIZE = 100000;
	
	private static final Logger logger = Logger.getLogger(EdgeDaoTest.class
			.getName());
	
	public static void main(String[] args) throws Exception {
		Beans.init("applicationContext-datasource.xml", "classpath:/applicationContext-dao.xml");
		NodeDao nodeDao = (NodeDao)Beans.getBean("nodeDao");
		EdgeDao edgeDao = (EdgeDao)Beans.getBean("edgeDao");
		DataSource ds = (DataSource)Beans.getBean("dataSource");
		
		saveWithBatch(nodeDao, edgeDao);
		
		//saveWithoutBatch(nodeDao, edgeDao);
		
		//saveWithJDBC(ds);
		//saveWithJDBCBATCH(ds);
	}

	private static void saveWithBatch(NodeDao nodeDao, EdgeDao edgeDao) {
		List<Edge> edges = new ArrayList<Edge>();
		Node fromNode = nodeDao.findById(1L);
		Node toNode = nodeDao.findById(2L);
		
		logger.info("creating sample edges...");
		int count = 0;
		int total = 0;
		for (int i=0; i<SIZE; i++){
			Edge edge = new Edge();
			edge.setFromNode(fromNode);
			edge.setToNode(toNode);
			edge.setDirected(true);
			edge.setWeight(1.0);
			edge.setType("t" + i);
			
			edges.add(edge);
			count++;
			total++;
			if (count == Constants.HIBERNATE_BATCH_SIZE) {
				edgeDao.save(edges);
				count = 0;
				edges = new ArrayList<Edge>();
				//logger.info(total);						
			}
		}
		
		logger.info(total);
		edgeDao.save(edges);
		logger.info("done");
	}
	
	private static void saveWithoutBatch(NodeDao nodeDao, EdgeDao edgeDao) {
		List<Edge> edges = new ArrayList<Edge>();
		Node fromNode = nodeDao.findById(1L);
		Node toNode = nodeDao.findById(2L);
		
		logger.info("creating sample edges...");
		int count = 0;
		int total = 0;
		for (int i=0; i<SIZE; i++){
			Edge edge = new Edge();
			edge.setFromNode(fromNode);
			edge.setToNode(toNode);
			edge.setDirected(true);
			edge.setWeight(1.0);
			edge.setType("t" + i);
			
			edges.add(edge);
			count++;
			total++;
			if (count == Constants.HIBERNATE_BATCH_SIZE) {
				logger.info(total);	
				count=0;
			}
		}
		
		logger.info(total);
		edgeDao.save(edges);
		logger.info("done");
	}
	
	private static void saveWithJDBC(DataSource ds) throws Exception{
		Connection con = ds.getConnection();
		PreparedStatement ps = con.prepareStatement("insert into edges values(?, ?, ?, ?, ?, ?, ?, ?)");
		
		logger.info("creating sample edges...");
		int count = 0;
		int total = 0;
		for (int i=0; i<SIZE; i++){
			ps.setLong(1, 1000000+i);
			ps.setLong(2, 0L);
			ps.setNull(3, Types.BIGINT);
			ps.setLong(4, 2L);
			ps.setLong(5, 1L);
			ps.setString(6, "t"+i);
			ps.setFloat(7, 1.0f);
			ps.setBoolean(8, true);
			
			ps.executeUpdate();

			count++; 
			total++;
			if (count == Constants.HIBERNATE_BATCH_SIZE){
				count = 0;
				logger.info(total);
			}
		}
		
		logger.info(total);
		logger.info("done");
	}
	
	private static void saveWithJDBCBATCH(DataSource ds) throws Exception{
		Connection con = ds.getConnection();
		PreparedStatement ps = con.prepareStatement("insert into edges values(?, ?, ?, ?, ?, ?, ?, ?)");
		
		logger.info("creating sample edges...");
		int count = 0;
		int total = 0;
		for (int i=0; i<SIZE; i++){
			ps.setLong(1, 1000000+i);
			ps.setLong(2, 0L);
			ps.setNull(3, Types.BIGINT);
			ps.setLong(4, 2L);
			ps.setLong(5, 1L);
			ps.setString(6, "t"+i);
			ps.setFloat(7, 1.0f);
			ps.setBoolean(8, true);
			
			ps.addBatch();

			count++; 
			total++;
			if (count == Constants.HIBERNATE_BATCH_SIZE){
				logger.info("executing batch...");
				ps.executeBatch();
				ps.clearBatch();
				count = 0;
				logger.info(total);
			}
		}
		
		logger.info(total);
		ps.executeBatch();
		logger.info("done");
	}
}
