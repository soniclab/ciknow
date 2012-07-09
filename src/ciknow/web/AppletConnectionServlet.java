package ciknow.web;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ciknow.domain.Edge;
import ciknow.domain.Visualization;

import org.apache.log4j.Logger;
import org.springframework.security.core.context.SecurityContextHolder;

import ciknow.dao.EdgeDao;
import ciknow.dao.NodeDao;
import ciknow.dao.VisualizationDao;
import ciknow.domain.Node;
import ciknow.io.GraphmlWriter;
import ciknow.mail.Mailer;
import ciknow.security.CIKNOWUserDetails;
import ciknow.util.Beans;
import ciknow.dao.GraphData;

@SuppressWarnings("unchecked")
public class AppletConnectionServlet extends BaseServlet {

	private static final long serialVersionUID = 1L;
	private Logger logger = Logger.getLogger(this.getClass());
	ObjectInputStream inputFromApplet = null;

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException {
		logger.info("Receiving graph data from applet...");

		try {

			// get an input stream from the applet
			inputFromApplet = new ObjectInputStream(request.getInputStream());
			// read the data from applet
			GraphData dataOb = (GraphData) inputFromApplet.readObject();
			inputFromApplet.close();

			if (dataOb.getLanguage() == null) {
				String emailAdd = dataOb.getEmailAd();
				String htmlStr = dataOb.getHTMLData();
				String subject = dataOb.getSub();
				String reply = dataOb.getReply();
				String msg = dataOb.getMsg();
				String fileName = dataOb.getFileName();
				
				if (emailAdd != null) {   
					char sent = 'i';
					if(htmlStr !=null){
					// create graphML file and insert it to html file
						List<Long> nodeIds = dataOb.getNodeIds();
						List<String> edgeFTTypes = dataOb.getEdgeFTTypes();
						String finalhtml = getFinalHtmlStr(nodeIds, edgeFTTypes, htmlStr);
						String[]  emails= {emailAdd};
						if(emailAdd.contains(","))							
						emails = emailAdd.split(",");
						else if(emailAdd.contains(";"))
							emails = emailAdd.split(";");
						
					//send email here
					Beans.init();
					Mailer mailer = (Mailer) Beans.getBean("mailService");
					mailer.sendHTML("no-reply@northwestern.edu", reply, emails, subject, msg, fileName, finalhtml);
					sent = mailer.getMailStatus();
					}
				
					responseToApplet(response, sent+ "");
					
				}else{
				// get current login node (creator of the visualization)
				Object p = SecurityContextHolder.getContext()
						.getAuthentication().getPrincipal();
				CIKNOWUserDetails node = (CIKNOWUserDetails) p;
				NodeDao nodeDao = (NodeDao) Beans.getBean("nodeDao");
				Node creator = nodeDao.findById(node.getId());

				VisualizationDao visDao = (VisualizationDao) Beans
						.getBean("visualizationDao");
				if (visDao.findByCreatorAndName(creator, dataOb.getName())
						.size() > 0) {
					logger.debug("Visualization with the same name='"
							+ dataOb.getName()
							+ "' has already been created by username='"
							+ creator.getUsername() + "'");
					responseToApplet(response, "y");
				} else {
					logger.debug("Creating new visualization name='"
							+ dataOb.getName() + "', creator='"
							+ creator.getUsername() + "'");
					Visualization vis = new Visualization();
					vis.setCreator(creator);
					vis.setData(dataOb.getData());
					vis.setName(dataOb.getName());
					vis.setLabel(dataOb.getLabel());
					String typeDesc = dataOb.getType().toLowerCase();
					String networkType;
					if (typeDesc.contains("local"))
						networkType = "local";
					else if (typeDesc.contains("custom"))
						networkType = "custom";
					else if (typeDesc.contains("rec"))
						networkType = "recommender";
					else
						networkType = "unrecognized type";
					vis.setNetworkType(networkType);
					vis.setType("layout");
					vis.setTimestamp(new Date());
					visDao.save(vis);
					responseToApplet(response, "n");
				}
				}
			} else {
				// write a file to the saver
				String path = getServletContext().getRealPath("/");
				String separator = System.getProperty("file.separator");

				FileWriter outputFileReader = new FileWriter(new File(path
						+ separator + "la.txt"));

				// Create Buffered/PrintWriter Objects
				PrintWriter outputStream = new PrintWriter(outputFileReader);

				outputStream.println(dataOb.getLanguage());

				outputStream.close();

			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException(e.getMessage());
		}
	}

	private void responseToApplet(HttpServletResponse response,
			String hasSameName) throws IOException {
		OutputStream outputToApplet = response.getOutputStream();
		StringReader read = new StringReader(hasSameName);
		int ch;
		while ((ch = read.read()) != -1) {
			outputToApplet.write((char) ch);
		}

		outputToApplet.flush();
		outputToApplet.close();
	}
		
	private String getFinalHtmlStr(List<Long> nodeIds, List<String> edgeFTTypes, String  htmlStr){
		Collection<Edge> edges = new HashSet<Edge>() ;
		String finalStr = null;	
		
		NodeDao nodeDao = (NodeDao) Beans.getBean("nodeDao");
		Collection<Node> nodes = nodeDao.loadByIds(nodeIds);
		
		EdgeDao	edgeDao = (EdgeDao) Beans.getBean("edgeDao");
		for(String eFTType:edgeFTTypes){
			String[] ftType = eFTType.split("&");
			List<Edge> ftEdges = edgeDao.loadByFromToNodeId(Long.parseLong(ftType[0]), Long.parseLong(ftType[1]));
			for(Edge e:ftEdges){
				if(e.getType().equals(ftType[2]))
					edges.add(e);
			}
		}
							
		try{	
		 GraphmlWriter gmw = (GraphmlWriter)Beans.getBean("graphmlWriter");
	        gmw.writeStr(nodes, edges);
	        String xml = gmw.getXMLStr();	        
	        int index = htmlStr.indexOf("</applet>");
	        finalStr = htmlStr.substring(0, index-1);	        
	        finalStr = finalStr + "\n<param name=graphML value=\"" +  xml + "\">\n</applet></html>";
		}catch(Exception e){
			
		}
		
	return finalStr;	
	}

}
