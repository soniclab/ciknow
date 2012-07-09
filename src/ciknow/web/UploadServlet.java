package ciknow.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import ciknow.dao.MetricDao;
import ciknow.dao.NodeDao;
import ciknow.dao.QuestionDao;
import ciknow.domain.Metric;
import ciknow.domain.Node;
import ciknow.domain.Question;
import ciknow.io.CodeBookReader;
import ciknow.io.ContactReader;
import ciknow.io.DLQuestionReader;
import ciknow.io.DLReader;
import ciknow.io.GraphmlReader;
import ciknow.io.NodeAttributeReader;
import ciknow.io.NodeDataReader;
import ciknow.io.NodeGroupReader;
import ciknow.io.NodeRoleReader;
import ciknow.io.QuestionReader;
import ciknow.ro.GenericRO;
import ciknow.util.Beans;
import ciknow.util.Constants;
import ciknow.util.GeneralUtil;

/**
 * @author gyao
 */
public class UploadServlet extends BaseServlet {
    private static final long serialVersionUID = -2470876179879040968L;
    private Logger logger = Logger.getLogger(this.getClass());


    @SuppressWarnings("rawtypes")
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException{
    	String loginNodeId = null; // for error msg recording
    	GenericRO ro = (GenericRO) Beans.getBean("genericRO");
        try {        	
            String action = null;
            String subAction = null;
            String overwrite = "0";
            
            // for upload recommendation metrics
            String metricType = null;
            String source = null;
            
            if (!ServletFileUpload.isMultipartContent(request)) return;

            FileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            List items = upload.parseRequest(request);
            for (Iterator i = items.iterator(); i.hasNext();) {
                FileItem item = (FileItem) i.next();
                if (item.isFormField()) {
                	if (item.getFieldName().equals("loginNodeId")) loginNodeId = item.getString(); 
                    if (item.getFieldName().equals("action")) action = item.getString();
                    if (item.getFieldName().equals("subAction")) subAction = item.getString();
                    if (item.getFieldName().equals("overwrite")) overwrite = item.getString();
                    if (item.getFieldName().equals("metricType")) metricType = item.getString();
                    if (item.getFieldName().equals("source")) source = item.getString();
                    continue;
                }

                String filename = item.getName();
                InputStream is = item.getInputStream();
                logger.info("Received upload: " + filename);
                logger.info("action: " + action);
                if (action.equals("survey")) {
                	if (!filename.endsWith(".zip")) throw new Exception("A .zip file is expected.");
                    handleZipFile(is);
                } else if (action.equals("codebook")) {
                	if (!filename.endsWith(".xml")) throw new Exception("A .xml file is expected.");
                    CodeBookReader reader = (CodeBookReader) Beans.getBean("codeBookReader");
                    reader.read(is, overwrite.equals("1"));
                } else if (action.equals("question")) {
                	if (!filename.endsWith(".txt")) throw new Exception("A .txt file is expected.");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    readQuestion(reader);
                    reader.close();
                } else if (action.equals("dlQuestion")) {
                	if (!filename.endsWith(".txt")) throw new Exception("A .txt file is expected.");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    readQuestionInDLFormat(reader);
                    reader.close();
                } else if (action.equals("nodes")) {
                	if (!filename.endsWith(".txt")) throw new Exception("A .txt file is expected.");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    ContactReader contactReader = (ContactReader) Beans.getBean("contactReader");
                    if (subAction.equals("normal")){
                    	contactReader.read(reader, overwrite);
                	} else if (subAction.equals("createOnly")){
                		contactReader.createOnly(reader);
                	} else if (subAction.equals("updateOnly")){
                		contactReader.updateOnly(reader);
                	} else{ // default is just to validate input file
                		contactReader.validate(reader);
                	}
                } else if (action.equals("nodeAttributes")) {
                	if (!filename.endsWith(".txt")) throw new Exception("A .txt file is expected.");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    NodeAttributeReader aReader = (NodeAttributeReader) Beans.getBean("nodeAttributeReader");
                    aReader.read(reader);
                } else if (action.equals("uploadNodeData")) {
                	if (!filename.endsWith(".txt")) throw new Exception("A .txt file is expected.");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    NodeDataReader nodeDatareader = (NodeDataReader) Beans.getBean("nodeDataReader");
                    nodeDatareader.read(reader);
                    reader.close();
                } else if (action.equals("nodeGroups")) {
                	if (!filename.endsWith(".txt")) throw new Exception("A .txt file is expected.");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    NodeGroupReader nodeGroupreader = (NodeGroupReader) Beans.getBean("nodeGroupReader");
                    nodeGroupreader.read(reader);
                } else if (action.equals("nodeRoles")) {
                	if (!filename.endsWith(".txt")) throw new Exception("A .txt file is expected.");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    NodeRoleReader nodeRoleReader = (NodeRoleReader) Beans.getBean("nodeRoleReader");
                    nodeRoleReader.read(reader);
                } else if (action.indexOf("logo_") == 0) {
                	if (!filename.endsWith(".jpg")) throw new Exception("A .jpg file is expected.");                    
                    File logoFile = new File(ro.getRealPath() + "images/" + action + ".jpg");                    
                    item.write(logoFile);
                    logger.info("Uploaded logo written to: " + logoFile.getAbsolutePath());
                } else if (action.indexOf("photo" + Constants.SEPERATOR) == 0) {
                	if (!filename.endsWith(".jpg")) throw new Exception("A .jpg file is expected.");
                    String parts[] = action.split(Constants.SEPERATOR);
                    File file = new File(ro.getRealPath() + "images/photos/" + parts[1] + ".jpg");
                    item.write(file);
                } else if (action.equals("photos")) {
                	if (!filename.endsWith(".zip")) throw new Exception("A .zip file is expected.");
                    File file = new File(ro.getRealPath() + "images/" + action + ".zip");
                    item.write(file);
                    GeneralUtil.unzip(file, new File(ro.getRealPath() + "images/" + action));
                } else if (action.equals("uploadMetrics")) {                	
                	BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                	uploadMetrics(metricType, source, reader);
                } else if (action.equals("graphml")) {
                	if (!filename.endsWith(".xml")) throw new Exception("A .xml file is expected.");
                	GraphmlReader reader = (GraphmlReader)Beans.getBean("graphmlReader");
                	reader.read(is, overwrite);
                } else if (action.equals("dl")) {
                	if (!filename.endsWith(".txt")) throw new Exception("A .txt file is expected.");
                	DLReader reader = (DLReader)Beans.getBean("dlReader");
                	reader.read(is);
                } else if (action.equals("invitationTemplate")) {
                	if (!filename.endsWith(".txt") && !filename.endsWith(".vm")) throw new Exception("A .txt or .vm file is expected.");
                	item.write(new File(ro.getRealPath() + "WEB-INF/classes/template.vm"));
                } else if (action.equals("mahoutPreferences")) {
                	if (!filename.endsWith(".txt")) throw new Exception("A .txt file is expected.");
                	item.write(new File(ro.getRealPath() + "WEB-INF/classes/ratings.txt"));
                } else {
                    throw new ServletException("Operation not supported.");
                }
            }

            response.setContentType("text/plain");

        	// Set Cache-Control.
        	response.addHeader("Cache-Control", "no-cache");
        	//response.addHeader("Cache-Control", "no-store");
        	//response.addHeader("Cache-Control", "must-revalidate");
        	//response.addHeader("Cache-Control", "proxy-revalidate");
        	// Prevent proxy caching.
        	response.setHeader("Pragma", "no-cache");
        	// Set expiration date to a date in the past.
        	response.setDateHeader("Expires", 0L); 
        	// Force always modified.
        	response.setDateHeader("Last-Modified", System.currentTimeMillis());

            response.getWriter().write("OK");
        } catch (Exception e) {
        	logger.error(e.getMessage());
            e.printStackTrace();
            GenericRO genericRO = (GenericRO)Beans.getBean("genericRO");
            if (loginNodeId != null) genericRO.setErrorMsg(loginNodeId, e.getMessage());
            else {
            	logger.error("login nodeId should not be null.");
            }
            throw new ServletException(e.getMessage());
            
            // below does not work for Flash
//            request.setAttribute("msg", e.getMessage());
//            try {
//				request.getRequestDispatcher("error.jsp").include(request, response);
//			} catch (IOException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
        }
    }


    /**
     * 
     * @param metricType 
     * @param source
     * @param reader
     * @throws IOException
     */
    private void uploadMetrics(String metricType, String source, BufferedReader reader) throws IOException {
    	logger.info("importing ergm metrics...");
		String line = reader.readLine();
		String[] cols = line.split("\t", -1);
		String[] types = cols[0].split(" - ");
		String row = types[0];
		String col = types[1];
		
		Beans.init();
		NodeDao nodeDao = (NodeDao)Beans.getBean("nodeDao");
		MetricDao metricDao = (MetricDao)Beans.getBean("metricDao");
		
		logger.debug("removing old metrics...");
		metricDao.delete(row, col, metricType, source);
		
		// get row/col node map
		logger.debug("prepare username to node map");
		List<Node> rowNodes = nodeDao.findByType(row);
		Map<String, Node> rowNodeMap = new HashMap<String, Node>();
		for (Node node : rowNodes){
			rowNodeMap.put(node.getUsername(), node);
		}
		List<Node> colNodes = nodeDao.findByType(col);
		Map<String, Node> colNodeMap = new HashMap<String, Node>();
		for (Node node : colNodes){
			colNodeMap.put(node.getUsername(), node);
		}
		
		logger.debug("read line by line and create metrics...");
		line = reader.readLine();
		List<Metric> metrics = new LinkedList<Metric>();
		while (line != null){
			if (line.trim().length() == 0) {
				line = reader.readLine();
				continue;
			}
			
			String parts[] = line.split("\t", -1);
			String fname = parts[0];
			Node fnode = rowNodeMap.get(fname);
			for (int i=1; i<parts.length; i++){
				float v = Float.parseFloat(parts[i]);
				if (Float.isNaN(v) || v < 0.01) continue;
				
				String tname = cols[i];
				Node tnode = colNodeMap.get(tname);
				
				Metric metric = new Metric();
				metric.setFromNode(fnode);
				metric.setToNode(tnode);
				metric.setType(metricType);
				metric.setSource(source);
				metric.setSymmetric(true);
				if (metricType.equals(Constants.ALG_ERGM) || metricType.equals(Constants.ALG_SP)){
					metric.setSymmetric(false);
				}
				metric.setValue(v);
				metrics.add(metric);
			}
			
			line = reader.readLine();
		}
		
		metricDao.save(metrics);
		logger.info(metrics.size() + " metrics saved.");
	}
    
    private void handleZipFile(InputStream is) throws Exception {
        ZipInputStream in = new ZipInputStream(is);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        ZipEntry currentEntry = in.getNextEntry();
        while (currentEntry != null) {
        	logger.info("reading file: " + currentEntry.getName());
            readQuestion(reader);
            currentEntry = in.getNextEntry();
        }
        reader.close();
    }

    private void readQuestion(BufferedReader reader) throws Exception {
        QuestionReader questionReader = (QuestionReader) Beans.getBean("questionReader");
        questionReader.read(reader);
    }
    
    private void readQuestionInDLFormat(BufferedReader reader) throws Exception {
        QuestionDao questionDao = (QuestionDao) Beans.getBean("questionDao");
        DLQuestionReader questionReader = (DLQuestionReader) Beans.getBean("dlQuestionReader");
        String line = reader.readLine().trim();
        logger.debug("finding question by shortname: " + line);
        Question question = questionDao.findByShortName(line);
        if (question == null){
        	String msg = "unrecognized question shortName: " + line;
        	logger.warn(msg);
        	throw new Exception(msg);
        }
        questionReader.read(reader, question);
    }
}
