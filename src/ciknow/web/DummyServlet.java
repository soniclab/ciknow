package ciknow.web;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author gyao
 */
public class DummyServlet extends BaseServlet {

    private static final long serialVersionUID = -677579408836859548L;
    private static Log log = LogFactory.getLog(DummyServlet.class);

    @Override
    public void doGet(final HttpServletRequest request,
            final HttpServletResponse response) throws ServletException {
        log.debug("contextPath: " + request.getContextPath());
        log.debug("realPath: " + getServletContext().getRealPath(""));
        log.debug("localHost: " + request.getLocalName());
        log.debug("localAddr: " + request.getLocalAddr());
        log.debug("localPort: " + request.getLocalPort());
        log.debug("remoteHost: " + request.getRemoteHost());
        log.debug("remoteAddr: " + request.getRemoteAddr());
        log.debug("remotePort: " + request.getRemotePort());
        log.debug("serverName: " + request.getServerName());
        log.debug("serverPort: " + request.getServerPort());
        log.debug("scheme: " + request.getScheme());

        String baseURL = request.getScheme() + "://"
                + request.getServerName() + ":"
                + request.getServerPort()
                + request.getContextPath();

        log.debug("baseURL: " + baseURL);
    }
}
