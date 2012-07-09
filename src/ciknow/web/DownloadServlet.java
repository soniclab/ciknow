package ciknow.web;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfWriter;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import ciknow.dao.EdgeDao;
import ciknow.dao.NodeDao;
import ciknow.dao.SurveyDao;
import ciknow.dao.QuestionDao;
import ciknow.dao.VisualizationDao;
import ciknow.ro.GenericRO;
import ciknow.teamassembly.Team;
import ciknow.teamassembly.TeamBuilder;
import ciknow.util.Beans;
import ciknow.util.Constants;
import ciknow.util.GeneralUtil;
import ciknow.domain.Edge;
import ciknow.domain.Node;
import ciknow.domain.Page;
import ciknow.domain.Survey;
import ciknow.domain.Question;
import ciknow.domain.Visualization;
import ciknow.io.CodeBookWriter;
import ciknow.io.ContactWriter;
import ciknow.io.DLWriter;
import ciknow.io.GraphmlWriter;
import ciknow.io.NodeDataWriter;
import ciknow.io.NodeGroupWriter;
import ciknow.io.NodeRoleWriter;
import ciknow.io.QuestionWriter;
import ciknow.io.ReportWriter;
import ciknow.io.TeamWriter;

/**
 * Created by IntelliJ IDEA.
 * User: gyao
 * Date: Dec 21, 2007
 * Time: 11:09:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class DownloadServlet extends BaseServlet {

    private static final long serialVersionUID = -4712750541068256505L;
    private Logger logger = Logger.getLogger(this.getClass());

    public void doGet(final HttpServletRequest request,
                      final HttpServletResponse response) throws ServletException{
    	String loginNodeId = null; // for error msg recording
        try {
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

            
        	loginNodeId = request.getParameter("loginNodeId");
            String action = request.getParameter("action");
            String filenameFormat = request.getParameter("filenameFormat");
            String outputFormat = request.getParameter(Constants.IO_OUTPUT_FORMAT);
            String keepEmptyPrivateGroup = request.getParameter(Constants.IO_KEEP_EMPTY_PRIVATE_GROUP);
            String removeNonRespondent = request.getParameter(Constants.IO_REMOVE_NON_RESPONDENT);
            String ignoreActivities = request.getParameter(Constants.IO_IGNORE_ACTIVITIES);
            String exportByColumn = request.getParameter(Constants.IO_EXPORT_BY_COLUMN);
            String visId = request.getParameter("visId");
            
            logger.info("action: " + action);
            logger.info("filenameFormat: " + filenameFormat);
            logger.info("outputFormat: " + outputFormat);
            logger.info("keepEmptyPrivateGroup: " + keepEmptyPrivateGroup);
            logger.info("removeNonRespondent: " + removeNonRespondent);
            logger.info("exportByColumn: " + exportByColumn);
            
            if (action.equals("getNodes")) {
                String tstring = request.getParameter("template");
                logger.info("template: " + tstring);
                boolean isTemplate = false;
                if (tstring != null && tstring.equals("1")) isTemplate = true;

                getContactData(response, isTemplate);
            } else if (action.equals("codebook")) {
            	getCodebook(response);
            } else if (action.equals("mahoutPreferences")) {
            	try{
	            	GenericRO ro = (GenericRO) Beans.getBean("genericRO");
	            	String filename = ro.getRealPath() + "WEB-INF/classes/ratings.txt";
	            	GeneralUtil.doDownload(request, response, filename, filename);
            	} catch (Exception ex){
            		ex.printStackTrace();
            		throw new ServletException(ex.getMessage());
            	}
            } else if (action.equals("graphml")) {
            	String importable = request.getParameter("importable");
            	getGraphml(response, importable.equals("1"));
            } else if (action.equals("systemReport")) {
            	String groupId = request.getParameter("groupId");
            	getSystemReport(response, Long.parseLong(groupId));
            } else if (action.equals("getImage")) {
            	String filename = request.getParameter("filename");
            	String imageData = request.getParameter("imageData");
            	getImage(response, filename, imageData);
            } else if (action.equals("getPdf")) {
            	String filename = request.getParameter("filename");
            	String imageData = request.getParameter("imageData");
            	getPdf(response, filename, imageData);
            } else if (action.equals("downloadVis")) {
            	getSavedVis(response, visId);
            } else if (action.equals("dl")) {
            	boolean isLabelEmbedded = request.getParameter("labelEmbedded").equals("1");
            	String showIsolate = request.getParameter("showIsolate");
            	if (showIsolate == null || showIsolate.length() == 0) showIsolate = "1";
            	
                getDL(response, showIsolate.equals("1"), isLabelEmbedded);
            } else if (action.equals("downloadNodeData")) {
            	String groupId = request.getParameter("groupId");
            	String questionIds = request.getParameter("questionIds");
            	Map<String, String> options = new HashMap<String, String>();
            	String prettyFormat = request.getParameter("prettyFormat");
            	if (prettyFormat == null || prettyFormat.isEmpty()) prettyFormat = "0";
            	options.put("prettyFormat", prettyFormat);
                getNodeData(response, groupId, questionIds.split(",", -1), options);
            } else if (action.equals("getNodeGroups")) {
                getUserGroupData(response);
            } else if (action.equals("getNodeRoles")) {
                getUserRoleData(response);
            } else if (action.equals("getQuestion")) {
                String questionId = request.getParameter("questionId");
                Long qid = Long.parseLong(questionId);
                Map<String, String> options = new HashMap<String, String>();
                options.put("filenameFormat", filenameFormat);
                options.put(Constants.IO_OUTPUT_FORMAT, outputFormat);
                options.put(Constants.IO_KEEP_EMPTY_PRIVATE_GROUP, keepEmptyPrivateGroup);
                options.put(Constants.IO_REMOVE_NON_RESPONDENT, removeNonRespondent);
                options.put(Constants.IO_IGNORE_ACTIVITIES, ignoreActivities);
                options.put(Constants.IO_EXPORT_BY_COLUMN, exportByColumn);
                getQuestionData(qid, options, response);
            } else if (action.equals("getSurvey")) {
                String surveyId = request.getParameter("surveyId");
                Long sid = Long.parseLong(surveyId);
                Map<String, String> options = new HashMap<String, String>();
                options.put("filenameFormat", filenameFormat);
                options.put(Constants.IO_OUTPUT_FORMAT, outputFormat);
                options.put(Constants.IO_KEEP_EMPTY_PRIVATE_GROUP, keepEmptyPrivateGroup);
                options.put(Constants.IO_REMOVE_NON_RESPONDENT, removeNonRespondent);
                options.put(Constants.IO_IGNORE_ACTIVITIES, ignoreActivities);
                options.put(Constants.IO_EXPORT_BY_COLUMN, exportByColumn);
                getSurveyData(sid, options, response);
            } else if (action.equals("getNetwork")) {
            	String row = request.getParameter("row");
            	String col = request.getParameter("col");
            	String type = request.getParameter("type");
            	String source = request.getParameter("source");
            	getNetwork(row, col, type, source, response);
            } else if (action.equals("invitationTemplate")) {
            	try{
	            	GenericRO ro = (GenericRO) Beans.getBean("genericRO");
	            	String filename = ro.getRealPath() + "WEB-INF/classes/template.vm";
	            	String displayFilename = filename.substring(0, filename.lastIndexOf(".")) + ".txt";
	            	GeneralUtil.doDownload(request, response, filename, displayFilename);
            	} catch (Exception ex){
            		ex.printStackTrace();
            		throw new ServletException(ex.getMessage());
            	}
            } else if (action.equals("getTeams")){
                response.setContentType("application/octet-stream");
                response.setHeader("Content-Disposition", "attachment; filename='teamassembly.zip'");
            	ZipOutputStream zout = new ZipOutputStream(response.getOutputStream());   
            	PrintWriter writer = new PrintWriter(new OutputStreamWriter(zout, "UTF-8"));
            	TeamWriter tw = new TeamWriter();
            	
            	int numTeams = Integer.parseInt(request.getParameter("numTeams"));
            	int minTeamSize = Integer.parseInt(request.getParameter("minTeamSize"));
            	int maxTeamSize = Integer.parseInt(request.getParameter("maxTeamSize"));
            	int iterations = Integer.parseInt(request.getParameter("iterations"));
            	String diversityQuestionShortName = request.getParameter("diversityQuestionShortName");
            	long similarityQuestionId = Long.parseLong(request.getParameter("similarityQuestionId"));
            	String[] groupIds = request.getParameterValues("groupId");
            	List<String> groupIdList = Arrays.asList(groupIds);
            	String[] edgeTypes = request.getParameterValues("edgeType");
            	List<String> edgeTypeList = Arrays.asList(edgeTypes);
            	
            	GenericRO genericRO = (GenericRO) Beans.getBean("genericRO");            	
            	TeamBuilder tb = genericRO.prepareTeamBuilder(numTeams, minTeamSize, maxTeamSize, 
            												iterations, diversityQuestionShortName, similarityQuestionId, 
            												groupIdList, edgeTypeList);
            	
            	ZipEntry entry = null;
            	String[] teams = request.getParameterValues("team");
            	List<Team> teamList = new LinkedList<Team>();
            	for (String team : teams){
            		String parts[] = team.split("-", -1);
                	Team t = new Team(tb, parts[0], parts[1].split(",", -1));
                	teamList.add(t);
                	
                	entry = new ZipEntry("team." + t.getId() + ".html");
                	zout.putNextEntry(entry);
                	tw.writeTeam(t, writer);
                	writer.flush();
            	}
            	
            	tb.setTeams(teamList);
            	entry = new ZipEntry("summary.html");
            	zout.putNextEntry(entry);	    	
            	tw.writeSummary(tb, writer);
            	writer.flush();
            	
            	writer.close();
            } else {
            	throw new Exception("Unrecognized action: " + action);
            }
        } catch (Exception e) {
        	logger.error(e.getMessage());
            e.printStackTrace();
            GenericRO genericRO = (GenericRO)Beans.getBean("genericRO");
            if (loginNodeId != null) genericRO.setErrorMsg(loginNodeId, e.getMessage());
            else {
            	logger.error("login nodeId should not be null.");
            }
            throw new ServletException(e.getMessage());
        }
    }


	/**
     * {@inheritDoc}
     * @throws IOException 
     */
    @Override
    public void doPost(final HttpServletRequest request,
                       final HttpServletResponse response) throws ServletException{
        doGet(request, response);
    }

    private void getCodebook(HttpServletResponse response) throws Exception {
        String filename = "ciknow.codebook.xml";
        response.setContentType("application/download");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        CodeBookWriter writer = (CodeBookWriter) Beans.getBean("codeBookWriter");
        writer.write(response.getOutputStream());
    }
    private void getSystemReport(HttpServletResponse response, Long groupId) throws Exception {
        String filename = "systemReport.txt";
        response.setContentType("application/download");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        ReportWriter writer = (ReportWriter)Beans.getBean("reportWriter");
        writer.writeSystemReport(response.getOutputStream(), groupId);
    }
    
    private void getGraphml(HttpServletResponse response, boolean importable) throws Exception {
        String filename = "ciknow.graphml.xml";
        response.setContentType("application/download");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        GraphmlWriter writer = (GraphmlWriter)Beans.getBean("graphmlWriter");
        writer.setImportable(importable);
        writer.write(response.getOutputStream());
    }

    private void getImage(HttpServletResponse response, String filename, String imageData) throws Exception {
    	byte[] imageBytes = Base64.decode(imageData);
        response.setContentType("application/octet-stream");
        response.setContentLength(imageBytes.length);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        OutputStream os = response.getOutputStream(); 
        os.write(imageBytes, 0, imageBytes.length);
        os.flush();
        os.close();
    }

    private void getPdf(HttpServletResponse response, String filename, String imageData) throws Exception {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
    	byte[] imageBytes = Base64.decode(imageData);
    	Image image = Image.getInstance(imageBytes);
    	float imgHeight = image.getPlainHeight() + 100;  
    	float imgWidth = image.getPlainWidth() + 100;     
    	Rectangle pageSize = new Rectangle(imgWidth, imgHeight);  
    	com.lowagie.text.Document document = new com.lowagie.text.Document(pageSize);             
    	PdfWriter.getInstance(document, response.getOutputStream());      	   
    	document.open();  
    	document.add(image);  
    	document.close();  
    }
    
    private void getSavedVis(HttpServletResponse response, String visId) throws Exception {
    	VisualizationDao visDao = (VisualizationDao)Beans.getBean("visualizationDao");
    	Visualization vis = visDao.findById(Long.parseLong(visId));
        String filename = vis.getName() + ".html";
        response.setContentType("application/download");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), "UTF8"));
		writer.append(vis.getData());
		writer.flush();
		writer.close();
    }
    
    private void getDL(HttpServletResponse response, boolean showIsolate, boolean isLabelEmbedded) throws Exception {
        String filename = "ciknow.dl.zip";
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        DLWriter writer = (DLWriter)Beans.getBean("dlWriter");
        EdgeDao edgeDao = (EdgeDao)Beans.getBean("edgeDao");
        NodeDao nodeDao = (NodeDao)Beans.getBean("nodeDao");
        writer.write(response.getOutputStream(), showIsolate?nodeDao.getAll():null, edgeDao.loadAll(), showIsolate, isLabelEmbedded);
    }
    
    private void getContactData(HttpServletResponse response, boolean isTemplate) throws Exception {
        String filename = "nodes.txt";
        response.setContentType("application/download");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8"));
        ContactWriter contactWriter = (ContactWriter) Beans.getBean("contactWriter");
        contactWriter.write(pw, isTemplate);
        pw.close();
    }

    private void getNodeData(HttpServletResponse response, String groupId, String[] questionIds, Map<String, String> options) throws Exception {
        String filename = "nodeData.txt";
        response.setContentType("application/download");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8"));
        NodeDataWriter nodeDataWriter = (NodeDataWriter) Beans.getBean("nodeDataWriter");
        nodeDataWriter.write(pw, groupId, questionIds, options);
        pw.close();
    }

    private void getUserGroupData(HttpServletResponse response) throws Exception {
        String filename = "nodeGroups.txt";
        response.setContentType("application/download");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8"));
        NodeGroupWriter nodeGroupWriter = (NodeGroupWriter) Beans.getBean("nodeGroupWriter");
        nodeGroupWriter.write(pw);
        pw.close();
    }
    
    private void getUserRoleData(HttpServletResponse response) throws Exception {
        String filename = "nodeRoles.txt";
        response.setContentType("application/download");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8"));
        NodeRoleWriter nodeRoleWriter = (NodeRoleWriter) Beans.getBean("nodeRoleWriter");
        nodeRoleWriter.write(pw);
        pw.close();
    }

    private void getQuestionData(Long qid, Map<String, String> options, HttpServletResponse response) throws Exception {    	
        QuestionDao qd = (QuestionDao) Beans.getBean("questionDao");
        Question question = qd.findById(qid);
        String filenameFormat = options.get("filenameFormat");
        logger.info("downloading data for question(shortName=" + question.getShortName() + ") ...");
        
        String filename = "unnamed.txt";
        if (filenameFormat.equals("name")) filename = question.getShortName() + ".txt";
        else if (filenameFormat.equals("id")) filename = "id_" + question.getId() + ".txt";
        else if (filenameFormat.equals("sequence")) filename = "sn_" + (question.getIndex() + 1) + ".txt";
        
        response.setContentType("application/download");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8"));

        if (question.isExportable()) {
            QuestionWriter questionWriter = (QuestionWriter) Beans.getBean("questionWriter");
            
            questionWriter.write(pw, question, options);
        } else {
            throw new Exception("question type (" + question.getType() + ") cannot be downloaded.");
        }
        pw.close();
    }

    private void getSurveyData(Long sid, Map<String, String> options, HttpServletResponse response) throws Exception {
        SurveyDao surveyDao = (SurveyDao) Beans.getBean("surveyDao");
        Survey survey = surveyDao.findById(sid);
        
        String filenameFormat = options.get("filenameFormat");
        logger.info("downloading data for survey (name=" + survey.getName() + ")...");
        
        String filename = "ciknow.zip";
        if (filenameFormat.equals("name")) filename = survey.getName() + ".zip";
        
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachement; filename=\"" + filename + "\"");
        ZipOutputStream out = new ZipOutputStream(response.getOutputStream());
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, "UTF-8"));
        QuestionWriter questionWriter = (QuestionWriter) Beans.getBean("questionWriter");
        for (Page page : survey.getPages()){
        	for (Question question : page.getQuestions()){
                if (!question.isExportable()) continue;
                
                String entryName = "unnamed.txt";
                if (filenameFormat.equals("name")) entryName = question.getShortName() + ".txt";
                else if (filenameFormat.equals("id")) entryName = "id_" + question.getId() + ".txt";
                else if (filenameFormat.equals("sequence")) entryName = "sn_" + (question.getIndex() + 1) + ".txt";
                
                ZipEntry entry = new ZipEntry(entryName);
                out.putNextEntry(entry);
                questionWriter.write(pw, question, options);
        	}
        }

        pw.flush();
        pw.close();
    }
    
    @SuppressWarnings("unchecked")
	private void getNetwork(String row, String col, String type, String source, HttpServletResponse response) throws Exception {
    	logger.info("get network(row=" + row + ", col=" + col + ")...");    	
    	
    	logger.debug("prepare data matrix");
        String configFilename = "recconfig." + type + "." + source + ".xml";
        URL url = Thread.currentThread().getContextClassLoader().getResource(configFilename);        
    	SAXReader reader = new SAXReader();
    	Document doc = reader.read(url.openStream());
    	Element config = doc.getRootElement();
        Beans.init();
        NodeDao nodeDao = (NodeDao) Beans.getBean("nodeDao");
        EdgeDao edgeDao = (EdgeDao) Beans.getBean("edgeDao");
        List<Node> rowNodes = nodeDao.findByType(row);
        List<Node> colNodes = nodeDao.findByType(col);
        DoubleMatrix2D matrix = new SparseDoubleMatrix2D(rowNodes.size(), colNodes.size());
        for (Element pair : (List<Element>) config.elements()){
        	String r = pair.attributeValue("row");
        	String c = pair.attributeValue("col");
        	if (!row.equals(r) || !col.equals(c)) continue;
        	for (Element e : (List<Element>) pair.elements("edge")){
        		String edgeType = e.attributeValue("type");
        		String direction = e.attributeValue("direction");
        		List<Edge> edges = edgeDao.loadByType(edgeType, false);
        		for (Edge edge : edges){
        			Node fnode = edge.getFromNode();
        			Node tnode = edge.getToNode();
        			if (direction.equals("-1")){
        				fnode = edge.getToNode();
        				tnode = edge.getToNode();
        			}
        			
        			int findex = rowNodes.indexOf(fnode);
        			int tindex = colNodes.indexOf(tnode);        			
					if (findex < 0 || tindex < 0) {
						logger.debug("edge (id=" + edge.getId() + ") is not eligible.");
						continue;
					}
					double oldWeight = matrix.getQuick(findex, tindex);
					double newWeight = oldWeight + edge.getWeight();
					matrix.setQuick(findex, tindex, newWeight);
        		}
        	}
        }
        
        logger.info("write to node");
        String filename = "matrix." + row + "." + col + ".txt";
        response.setContentType("application/download");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        // header        
        sb.append(row + " - " + col);
        for (Node node : colNodes){
        	sb.append("\t").append(node.getUsername());
        }
        sb.append("\n");
        // row by row
        for (int i = 0; i < rowNodes.size(); i++){
        	Node node = rowNodes.get(i);
        	sb.append(node.getUsername());
        	
        	for (int j = 0; j < colNodes.size(); j++){
        		sb.append("\t").append(matrix.getQuick(i, j));
        	}
        	
        	sb.append("\n");
        }
        pw.write(sb.toString());
        pw.close();
    }
}
