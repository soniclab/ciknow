package ciknow.web;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GraphMLDownloadServlet extends BaseServlet {
    private static final long serialVersionUID = 1332513500816611096L;

    public void doGet(final HttpServletRequest request,
                      final HttpServletResponse response) throws ServletException {
//		response.setContentType( "text/plain" );
//		response.setHeader( "Content-Disposition", "attachment; filename=\"graphml_output\"" );
//		GraphMLWriter writer = new GraphMLWriter(WebGraph.getInstance());
//		try {
//			writer.write(response.getWriter());
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
    }

    public void doPost(final HttpServletRequest request,
                       final HttpServletResponse response) throws ServletException {
        doGet(request, response);
    }

}
