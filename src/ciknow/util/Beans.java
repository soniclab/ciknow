package ciknow.util;

import javax.servlet.ServletContext;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * This utility provides a way for one instance and global access.
 *
 * @author gyao
 */
public class Beans {

    private static Logger logger = Logger.getLogger(Beans.class);
    private static ApplicationContext context = null;
    private static ServletContext servletContext = null;

    private Beans() {
    }

    // for web applications
    public static synchronized void init(ServletContext sc) {
        if (context == null) {
            logger.info("initializing context from servletContext...");
            servletContext = sc;
            context = WebApplicationContextUtils.getWebApplicationContext(sc);
        }
    }

    // for standalone application
    public static synchronized void init() {
        if (context == null) {
            logger.info("initializing context from files: applicationContext-*.xml");
            String[] configs = {"applicationContext-datasource.xml",
                "applicationContext-dao.xml",
                "applicationContext-mail.xml",
                "applicationContext-ws.xml",
                "applicationContext-job.xml",
                "applicationContext-general.xml",
                "applicationContext-security.xml",
                "applicationContext-ro.xml"
            };
            context = new ClassPathXmlApplicationContext(configs);
        }
    }

    public static synchronized void init(String... filenames) {
        if (context == null) {
            logger.info("initializing context from file(" + filenames + ")...");
            context = new ClassPathXmlApplicationContext(filenames);
        }
    }

    public static synchronized void init(ApplicationContext ac) {
        if (context == null) {
            logger.info("initializing context from given context ...");
            context = ac;
        }
    }

    /**
     * Gets the named bean.
     *
     * @param beanName Name of the bean
     * @return The bean.
     */
    public static Object getBean(String beanName) {
        return context.getBean(beanName);
    }

    public static ServletContext getServletContext() {
        return servletContext;
    }
}
