package ciknow.web;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ciknow.dao.NodeDao;
import ciknow.dao.SurveyDao;
import ciknow.domain.Node;
import ciknow.domain.Survey;
import ciknow.util.Beans;
import ciknow.util.Constants;
import ciknow.util.GeneralUtil;

/**
 * Verify Survey Password
 * @author gyao
 */
public class SurveyPasswordServlet extends HttpServlet {
	private static final long serialVersionUID = 1000L;
    private static final Log logger = LogFactory.getLog(SurveyPasswordServlet.class);
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SurveyPasswordServlet() {
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
		logger.info("Verifying survey password...");
		Beans.init();

		String username = request.getParameter("username");
		if (username.isEmpty()) {
			request.setAttribute("msg", "username is missing.");
			request.getRequestDispatcher("/surveyPassword.jsp").forward(request, response);
			return;
		}

		NodeDao nodeDao = (NodeDao) Beans.getBean("nodeDao");
		Node node = nodeDao.findByUsername(username);
		if (node == null) {
			request.setAttribute("username", username);
			request.getRequestDispatcher("/surveyPassword.jsp").forward(request, response);
			return;
		}
		
		String password = request.getParameter("password");		
		if (password.isEmpty()) {
			request.setAttribute("username", username);
			request.setAttribute("msg", "password is missing.");
			request.getRequestDispatcher("/surveyPassword.jsp").forward(request, response);
			return;
		}
		
        SurveyDao surveyDao = (SurveyDao) Beans.getBean("surveyDao");
        Survey survey = surveyDao.findById(1L);
        if (!GeneralUtil.verify(survey.getAttributes(), Constants.SURVEY_REQUIRE_PASSWORD)){
        	request.setAttribute("username", username);
        	request.getRequestDispatcher("/surveyPassword.jsp").forward(request, response);
        	return;
        }
        
        String surveyPassword = survey.getAttribute(Constants.SURVEY_PASSWORD);
        logger.info("surveyPassword: " + surveyPassword + ", respondent entered password: " + password);
        if (!password.equals(surveyPassword)) {
			request.setAttribute("username", username);
			request.setAttribute("msg", "password is incorrect.");
			request.getRequestDispatcher("/surveyPassword.jsp").forward(request, response);
			return;
        }
        
        request.setAttribute("origin", "password");
		request.getRequestDispatcher("/s?r=" + username).forward(request, response);		
	}

}
