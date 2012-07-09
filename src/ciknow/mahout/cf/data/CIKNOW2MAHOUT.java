package ciknow.mahout.cf.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.common.IOUtils;

import ciknow.util.Beans;

public class CIKNOW2MAHOUT {
	private static Log log = LogFactory.getLog(CIKNOW2MAHOUT.class);
	
	public static void main(String[] args) throws FileNotFoundException, TasteException {
		String path = "C:\\Users\\gyao\\workspace.galileo\\ciknow\\data\\ciknow2mahout\\citation.txt";
		export2file("Citation", "1", path);
	}
	
	public static void export2file(String edgeType, String direction, String path) throws TasteException, FileNotFoundException{
		log.info("exporting ciknow relations to mahout preference file....");
		log.info("edgeType: " + edgeType);
		log.info("direction: " + direction);
		log.info("path: " + path);
		
		Beans.init();
		DataSource ds = (DataSource)Beans.getBean("dataSource");
		boolean hasPrefValues = true;
		
		PrintWriter writer = new PrintWriter(new File(path));
		writer.append("# CIKNOW data exported as Mahout preferences").append("\n");
		writer.append("# Relation: " + edgeType).append("\n");
		writer.append("# Direction: " + direction).append("\n");
		
	    Connection conn = null;
	    PreparedStatement stmt = null;
	    ResultSet rs = null;
	    String query = "SELECT from_node_id, to_node_id, weight FROM edges WHERE type=?";
	    try {
	      conn = ds.getConnection();
	      
	      stmt = conn.prepareStatement("SELECT COUNT(DISTINCT weight) FROM edges WHERE type=?");
	      stmt.setString(1, edgeType);
	      rs = stmt.executeQuery();
	      if (rs.next()) {
	    	  int count = rs.getInt(1);
	    	  if (count <= 1) hasPrefValues = false;
	    	  log.info("There are " + count + " different preference values.");
	      }
	      
	      stmt = conn.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
	      stmt.setFetchDirection(ResultSet.FETCH_FORWARD);
	      stmt.setFetchSize(1000);
	      stmt.setString(1, edgeType);

	      log.info("Executing SQL query: " + query);
	      rs = stmt.executeQuery();

	      int count = 0;
	      log.info("examing result set...");
	      while (rs.next()){
	    	  Long fromNodeId = rs.getLong(1);
	    	  Long toNodeId = rs.getLong(2);
	    	  Float weight = rs.getFloat(3);
	    	  if (direction.equals("1")) {
	    		  writer.append(fromNodeId.toString()).append(",").append(toNodeId.toString());
	    	  }
	    	  else {
	    		  writer.append(toNodeId.toString()).append(",").append(fromNodeId.toString());
	    	  }
    		  if (hasPrefValues) writer.append(",").append(weight.toString());
    		  writer.append("\n");
    		  
	    	  count++;
	    	  if (count%1000000 == 0){
	    		  writer.flush();
	    	  }
	      }
	      writer.close();
	      
	      log.info(count + " preferences written.");
	      
	    } catch (SQLException sqle) {
	      log.warn("Exception while exporting preferences", sqle);
	      throw new TasteException(sqle);
	    } finally {
	      IOUtils.quietClose(rs, stmt, conn);
	    }
	}
}
