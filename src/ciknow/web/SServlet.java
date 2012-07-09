package ciknow.web;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import ciknow.dao.SurveyDao;
import ciknow.domain.Survey;
import ciknow.security.CIKNOWUserDetails;
import ciknow.security.CIKNOWUserDetailsService;
import ciknow.util.Beans;
import ciknow.util.Constants;
import ciknow.util.GeneralUtil;

/**
 * Servlet implementation class SServlet
 * @author gyao
 */
public class SServlet extends HttpServlet {
	private static final long serialVersionUID = 1000L;
    private static final Log logger = LogFactory.getLog(SServlet.class);
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Beans.init();
        SurveyDao surveyDao = (SurveyDao) Beans.getBean("surveyDao");
		CIKNOWUserDetailsService userService = (CIKNOWUserDetailsService) Beans.getBean("userDetailsService");
		String username = request.getParameter("r");
        
		// identify respondent
		CIKNOWUserDetails userDetails;
		try {
			userDetails = (CIKNOWUserDetails) userService.loadUserByUsername(username);
			if (userDetails.getNode().isAdmin()) throw new Exception("Project owner or admin should not go this way.");
		} catch (Exception e){
			logger.error(e.getMessage());
			request.setAttribute("msg", e.getMessage());
			request.getRequestDispatcher("/noway.jsp").forward(request, response);
			return;
		}
		
		// determine whether respondent can pass through to survey
        List<Survey> surveys = surveyDao.getAll();
        Survey survey = surveys.get(0);
		boolean pass = false;
		String origin = (String) request.getAttribute("origin");
		if (origin == null) origin = "";
		if (origin.equals("password") || origin.equals("register") || origin.equals("login")) pass = true;
		else if (GeneralUtil.verify(survey.getAttributes(), Constants.SURVEY_REQUIRE_PASSWORD)) pass = false;
		else pass = true;
		
		// forward to the right page
		if (pass){
			Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
			SecurityContextHolder.getContext().setAuthentication(auth);
			request.getRequestDispatcher("/survey.zul").forward(request, response);
		} else { // verify survey level password
			request.setAttribute("username", username);
			request.getRequestDispatcher("/surveyPassword.jsp").forward(request, response);
		}
	}

}
