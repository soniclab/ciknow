package ciknow.web;

import java.io.*;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.*;

//import org.apache.commons.fileupload.*;
//import org.apache.commons.fileupload.disk.DiskFileItemFactory;
//import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

//import ciknow.graph.Graph;
//import ciknow.graph.SavingGraph;
//import ciknow.graph.io.GraphMLReader;
import ciknow.util.Beans;

public class GraphMLUploadServlet extends BaseServlet {
    private Logger logger = Logger.getLogger(this.getClass());
    private static final long serialVersionUID = 1332513500816611096L;


    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
//		logger.info("received upload graphml request.");
//		if (!ServletFileUpload.isMultipartContent(request)) {
//			logger.info("request is not multipart content.");
//			return;
//		}
//		FileItemFactory factory = new DiskFileItemFactory();
//		ServletFileUpload upload = new ServletFileUpload(factory);
//
//		List items = null;
//		try {
//			logger.info("parsing multipart request...");
//			items = upload.parseRequest(request);
//		}
//		catch (FileUploadException e) {
//			logger.error(e.getMessage(), e);
//			e.printStackTrace();
//			return;
//		}
//
//		for (Iterator i = items.iterator(); i.hasNext();) {
//			FileItem item = (FileItem) i.next();
//			if (item.isFormField()) continue;
//			
//			// clear database
//			Graph graph = (Graph)Beans.getBean("savingGraph");
//			graph.clear();
//			
//			// importing data into database
//			logger.info("Received upload: " + item.getName());
//			GraphMLReader gmlReader = new GraphMLReader(graph);
//			gmlReader.read(new BufferedReader(new InputStreamReader(item.getInputStream())));
//			
//			// refresh memeory graph
//			WebGraph.refresh();
//		}
//		response.setContentType("text/plain");
//		response.getWriter().write("OK");

    }

} 
