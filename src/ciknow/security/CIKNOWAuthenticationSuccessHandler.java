package ciknow.security;

import ciknow.dao.ActivityDao;
import ciknow.domain.Activity;
import ciknow.util.Constants;
import java.io.IOException;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

/**
 * Log the login activity into database before forwarding successful login user
 *
 * @author gyao
 *
 */
public class CIKNOWAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private ActivityDao activityDao;

    public ActivityDao getActivityDao() {
        return activityDao;
    }

    public void setActivityDao(ActivityDao activityDao) {
        this.activityDao = activityDao;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws ServletException, IOException {

        // log activity
        CIKNOWUserDetails user = (CIKNOWUserDetails) authentication.getPrincipal();
        logger.info(user.getUsername() + " login successfully.");
        Activity act = new Activity();
        act.setSubject(user.getNode());
        act.setPredicate(Constants.ACTIVITY_LOGIN);
        act.setTimestamp(new Date());
        activityDao.save(act);

        // call superclass
        this.setDefaultTargetUrl("/survey.zul");
        this.setAlwaysUseDefaultTargetUrl(false);
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
