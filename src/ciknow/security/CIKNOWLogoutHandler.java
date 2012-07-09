package ciknow.security;

import ciknow.dao.ActivityDao;
import ciknow.domain.Activity;
import ciknow.util.Constants;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.util.Assert;

/**
 *
 * @author gyao
 */
public class CIKNOWLogoutHandler implements LogoutHandler {

    private static final Log logger = LogFactory.getLog(CIKNOWLogoutHandler.class);
    private ActivityDao activityDao;

    public ActivityDao getActivityDao() {
        return activityDao;
    }

    public void setActivityDao(ActivityDao activityDao) {
        this.activityDao = activityDao;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) {
        Assert.notNull(request, "HttpServletRequest required");

        CIKNOWUserDetails user = (CIKNOWUserDetails) authentication.getPrincipal();
        logger.info(user.getUsername() + " logout successfully.");
        Activity act = new Activity();
        act.setSubject(user.getNode());
        act.setPredicate(Constants.ACTIVITY_LOGOUT);
        act.setTimestamp(new Date());
        activityDao.save(act);
    }
}
