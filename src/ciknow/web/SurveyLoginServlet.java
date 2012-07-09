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
 * Verify Respondent Username/Password
 * @author gyao
 */
public class SurveyLoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1000L;
    private static final Log logger = LogFactory.getLog(SurveyLoginServlet.class);
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SurveyLoginServlet() {
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
		logger.info("Verifying username/password...");
		Beans.init();

		// check if this survey allow external (self-registered) respondent
        SurveyDao surveyDao = (SurveyDao) Beans.getBean("surveyDao");
        Survey survey = surveyDao.findById(1L);
        if (!GeneralUtil.verify(survey.getAttributes(), Constants.SURVEY_ALLOW_SELF_REGISTER)){
        	request.getRequestDispatcher("/surveyLogin.jsp").forward(request, response);
        	return;
        }
        
        // check username
		String username = request.getParameter("username");
		if (username.isEmpty()) {
			request.setAttribute("msg", "username is missing.");
			request.getRequestDispatcher("/surveyLogin.jsp").forward(request, response);
			return;
		}

		NodeDao nodeDao = (NodeDao) Beans.getBean("nodeDao");
		Node node = nodeDao.loadByUsername(username);
		if (node == null) {
			request.setAttribute("msg", "cannot identify login: " + username);
			request.getRequestDispatcher("/surveyLogin.jsp").forward(request, response);
			return;
		} else if (node.isAdmin()){
			request.setAttribute("msg", "survey administrator or creators should not login from here.");
			request.getRequestDispatcher("/surveyLogin.jsp").forward(request, response);
			return;
		}
		
		
		String password = request.getParameter("password");		
		if (!node.getPassword().equals(password)) {
			request.setAttribute("username", username);
			request.setAttribute("msg", "password is incorrect.");
			request.getRequestDispatcher("/surveyLogin.jsp").forward(request, response);
			return;
		}
        
        request.setAttribute("origin", "login");
		request.getRequestDispatcher("/s?r=" + username).forward(request, response);		
	}

}
