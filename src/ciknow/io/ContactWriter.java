package ciknow.io;

import java.io.File;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ciknow.dao.NodeDao;
import ciknow.domain.Node;
import ciknow.util.Beans;
import ciknow.util.Constants;

public class ContactWriter{
	private static Log logger = LogFactory.getLog(ContactWriter.class);
	private NodeDao nodeDao;
	
	public static void main(String[] args) throws Exception{
		if (args.length < 1) {
			logger.error("filename (e.g. c:/path/to/file) is required.");
			return;
		}
		String filename = args[0];		
		PrintWriter pw = new PrintWriter(new File(filename));
		
		Beans.init();
        ContactWriter contactWriter = (ContactWriter) Beans.getBean("contactWriter");

        contactWriter.write(pw, false);
	}
	
	public ContactWriter() {
		super();
	}

	public NodeDao getNodeDao() {
		return nodeDao;
	}

	public void setNodeDao(NodeDao nodeDao) {
		this.nodeDao = nodeDao;
	}

	public void write(Writer writer, boolean isTemplate) throws Exception{
        logger.info("exporting contacts...");
        
        StringBuilder sb = new StringBuilder();
        
        // write instruction
        // this is abandoned because Excel always add extra quotes ("") on comments.
//        sb.append("##0, Comments start with '##'\n");
//        sb.append("##1, Username is required and must be unique for each entity.\n");
//        sb.append("##2, Enabled = 1 means this node can login, otherwise not.\n");
//        sb.append("##3, NodeType can be user (reserved), keyword, document, etc. \n");
//        sb.append("##4, if NodeType = user, firstname and lastname is required. " +
//        		"They will be concatenated as nodeLabel; Otherwise, nodeLabel is " +
//        		"required and cannot exceed 1024 characters.\n");
//        sb.append("##5, NodeURI can be a url to web resource about current row entity. " +
//        		"Place a dash '-' if not available. Leaving it blank may cause parser error.\n");
        
        // write header        
        logger.info("writing headers...");        
        List<String> defaultFieldNames = Constants.getDefaultContactFields();
        int i=0;
        for (String f : defaultFieldNames){
            if (i>0) sb.append("\t");
            sb.append(f);
            i++;
        }
        sb.append("\n");
        
        if (!isTemplate){        
	        List<Node> nodes = nodeDao.getAll();
	        logger.info("there are " + nodes.size() + " nodes.");
	        logger.debug("writing each row (for each node)...");
	        int count = 0;
	        int total = 0;
	        for (Node node : nodes) {
	            sb.append(node.getUsername());
	            sb.append("\t" + (node.getFirstName() == null?"":node.getFirstName()));
	            sb.append("\t" + (node.getLastName() == null?"":node.getLastName()));
	            sb.append("\t" + (node.getMidName()== null?"":node.getMidName()));
	            sb.append("\t" + (node.getAddr1()==null?"":node.getAddr1()));
	            sb.append("\t" + (node.getAddr2()==null?"":node.getAddr2()));
	            sb.append("\t" + (node.getCity()==null?"":node.getCity()));
	            sb.append("\t" + (node.getState()==null?"":node.getState()));
	            sb.append("\t" + (node.getCountry()==null?"":node.getCountry()));
	            sb.append("\t" + (node.getZipcode()==null?"":node.getZipcode()));
	            sb.append("\t" + (node.getEmail()==null?"":node.getEmail()));
	            sb.append("\t" + (node.getPhone()==null?"":node.getPhone()));
	            sb.append("\t" + (node.getCell()==null?"":node.getCell()));
	            sb.append("\t" + (node.getFax()==null?"":node.getFax()));
	            sb.append("\t" + (node.getDepartment()==null?"":node.getDepartment()));
	            sb.append("\t" + (node.getOrganization()==null?"":node.getOrganization()));
	            sb.append("\t" + (node.getUnit()==null?"":node.getUnit()));
	            sb.append("\t" + (node.getEnabled()?"1":"0"));	            
	            sb.append("\t" + (node.getLabel()==null?"":node.getLabel()));
	            sb.append("\t" + (node.getUri()==null?"":node.getUri()));   
	            sb.append("\t" + node.getType()); 
	            sb.append("\n");
	            
	            count++;
	            total++;
	            if (count == 5000){
	                writer.write(sb.toString());
	                writer.flush();
	            	logger.debug(total + " nodes written.");
	            	
	            	sb = new StringBuilder();
	            	count = 0;
	            }
	        }
	        logger.debug(total + " nodes written.");
        }
        // write to file
        writer.write(sb.toString());
        writer.flush();

        logger.info("done.");
	}

}
