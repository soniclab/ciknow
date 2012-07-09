package ciknow.web;

import ciknow.ro.GenericRO;
import ciknow.util.Beans;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * User: gyao
 * Date: Apr 8, 2008
 * Time: 9:59:13 PM
 */
public class DummyFilter implements Filter {
    private static Log log = LogFactory.getLog(DummyFilter.class);
    private boolean baseURLExtracted = false;
    private ServletContext context;

    public void init(FilterConfig filterConfig) throws ServletException {
        context = filterConfig.getServletContext();
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (!baseURLExtracted) {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            String baseURL = request.getScheme() + "://" +
                    request.getServerName() + ":" +
                    request.getServerPort() +
                    request.getContextPath();
            GenericRO genericRO = (GenericRO) Beans.getBean("genericRO");
            genericRO.setBaseURL(baseURL);
            log.info("baseURL: " + genericRO.getBaseURL());

            String realPath = context.getRealPath("/");
            genericRO.setRealPath(realPath);
            log.info("realPath: " + realPath);

            baseURLExtracted = true;
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    public void destroy() {

    }
}
