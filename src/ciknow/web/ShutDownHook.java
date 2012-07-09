package ciknow.web;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.ContextLoader;

/**
 * Application Lifecycle Listener implementation class ShutDownHook
 *
 * @author gyao
 */
public class ShutDownHook implements ServletContextListener {

    private static Log log = LogFactory.getLog(ShutDownHook.class);

    /**
     * Default constructor.
     */
    public ShutDownHook() {
    }

    /**
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.info("Initialize shutdownhook.");
    }

    /**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.info("Destroy shutdownhook.");
        BeanFactory bf = (BeanFactory) ContextLoader.getCurrentWebApplicationContext();
        if (bf instanceof ConfigurableApplicationContext) {
            ((ConfigurableApplicationContext) bf).close();
        }

        log.info("Wait for 2 seconds.");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        log.info("Supposely the spring context should have been shut down, resources are released.");
    }
}
