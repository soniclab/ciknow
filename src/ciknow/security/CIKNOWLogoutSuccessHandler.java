package ciknow.security;

import ciknow.dao.ActivityDao;
import ciknow.domain.Activity;
import ciknow.util.Constants;
import java.io.IOException;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;

/**
 *
 * @author gyao
 */
public class CIKNOWLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {

    private static final Log logger = LogFactory.getLog(CIKNOWLogoutSuccessHandler.class);
    private ActivityDao activityDao;

    public ActivityDao getActivityDao() {
        return activityDao;
    }

    public void setActivityDao(ActivityDao activityDao) {
        this.activityDao = activityDao;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request,
            HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        CIKNOWUserDetails user = (CIKNOWUserDetails) authentication.getPrincipal();
        logger.info(user.getUsername() + " logout successfully.");
        Activity act = new Activity();
        act.setSubject(user.getNode());
        act.setPredicate(Constants.ACTIVITY_LOGOUT);
        act.setTimestamp(new Date());
        activityDao.save(act);

        //setDefaultTargetUrl("/login.jsp?logout=1");
        setDefaultTargetUrl("/logout-complete.html");
        super.onLogoutSuccess(request, response, authentication);

        Session session = Sessions.getCurrent();
        if (session != null) {
            session.invalidate();
        }
    }
}
