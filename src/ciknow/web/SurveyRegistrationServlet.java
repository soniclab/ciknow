package ciknow.web;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ciknow.domain.Group;
import ciknow.domain.Role;
import net.tanesha.recaptcha.ReCaptchaImpl;
import net.tanesha.recaptcha.ReCaptchaResponse;

import org.apache.log4j.Logger;

import ciknow.dao.GroupDao;
import ciknow.dao.NodeDao;
import ciknow.dao.RoleDao;
import ciknow.dao.SurveyDao;
import ciknow.domain.Node;
import ciknow.domain.Survey;
import ciknow.util.Beans;
import ciknow.util.Constants;
import ciknow.util.GeneralUtil;
import ciknow.util.PropsUtil;

/**
 * @author gyao
 */
public class SurveyRegistrationServlet extends BaseServlet {
    private static final long serialVersionUID = -2470876179879040968L;
    private Logger logger = Logger.getLogger(this.getClass());


    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        try {
            SurveyDao surveyDao = (SurveyDao) Beans.getBean("surveyDao");
            Survey survey = surveyDao.findById(1L);
            String msg = "";
            ServletContext sc = getServletContext();
            if (!GeneralUtil.verify(survey.getAttributes(), Constants.SURVEY_ALLOW_SELF_REGISTER)){
            	logger.warn("user registration is disabled.");
            	msg = "This CIKNOW instance doesn't allow external users, please contact administrator if you are not happy about this.";
            	request.setAttribute("msg", msg);
            	sc.getRequestDispatcher("/surveyRegistration.jsp").forward(request, response);
            	return;
            } else {
            	logger.info("registering new user.");
            	NodeDao nodeDao = (NodeDao)Beans.getBean("nodeDao");
            	GroupDao groupDao = (GroupDao)Beans.getBean("groupDao");
            	RoleDao roleDao = (RoleDao)Beans.getBean("roleDao");
            	
            	String username = request.getParameter("username").trim();  
            	logger.debug("username: " + username);
            	String firstname = request.getParameter("firstname").trim();
            	String lastname = request.getParameter("lastname").trim();
            	String password = request.getParameter("password").trim();
            	String email = request.getParameter("email").trim();
            	
            	// validate username
            	if (username.length() == 0 || password.length() == 0){
            		msg = "Username and password cannot be empty: " + username;
                	request.setAttribute("msg", msg);
                	sc.getRequestDispatcher("/surveyRegistration.jsp").forward(request, response);
                	return;
            	} else if (username.length() > 80){
            		msg = "Username is too long (> 80): " + username;
            		logger.warn(msg);
            		request.setAttribute("msg", msg);
            		request.getRequestDispatcher("surveyRegistration.jsp").forward(request, response);
            		return;
            	} else if (username.contains(" ")
                		|| username.contains(",")
                		|| username.contains("`")
                		|| username.contains("/")
                		|| username.contains("\\")
                		|| username.contains("*")
                		|| username.contains("\"")
                		|| username.contains(">")
                		|| username.contains("<")
                		|| username.contains(":")
                		|| username.contains("|")
                		|| username.contains("?")){
                		msg = "Username cannot contain special characters or spaces: " + username;
                    	request.setAttribute("msg", msg);
                    	sc.getRequestDispatcher("/surveyRegistration.jsp").forward(request, response);
                    	return;
                } else if (nodeDao.findByUsername(username) != null){
            		msg = "Duplicated username: " + username;
            		logger.warn(msg);
            		request.setAttribute("msg", msg);
            		request.getRequestDispatcher("surveyRegistration.jsp").forward(request, response);
            		return;
            	}
            	
            	// validate registered email address
            	if (!ciknow.util.Validation.isEmailValid(email)){
            		msg = "Invalid Email Address";
                	request.setAttribute("msg", msg);
                	sc.getRequestDispatcher("/surveyRegistration.jsp").forward(request, response);
                	return;
            	}
            	if (nodeDao.findByEmail(email) != null){
            		msg = "Email is already exist.";
                	request.setAttribute("msg", msg);
                	sc.getRequestDispatcher("/surveyRegistration.jsp").forward(request, response);
                	return;	
            	}
            	
            	// protect by reCaptcha
            	PropsUtil props = new PropsUtil("ciknow");
                String remoteAddr = request.getRemoteAddr();
                ReCaptchaImpl reCaptcha = new ReCaptchaImpl();
                reCaptcha.setPrivateKey(props.get("recaptcha_private_key"));

                String challenge = request.getParameter("recaptcha_challenge_field");
                String uresponse = request.getParameter("recaptcha_response_field");
                ReCaptchaResponse reCaptchaResponse = reCaptcha.checkAnswer(remoteAddr, challenge, uresponse);

                if (!reCaptchaResponse.isValid()) {
            		msg = "Show me you are not a robot by passing the reCaptcha:)";
                	request.setAttribute("msg", msg);
                	sc.getRequestDispatcher("/surveyRegistration.jsp").forward(request, response);
                	return;
                }
                
                // create new respondent
            	Node node = new Node();
                node.setType(Constants.NODE_TYPE_USER);
                node.setLabel(lastname + ", " + firstname);
                node.setUri("-");                
                node.setUsername(username);
                node.setFirstName(firstname);
                node.setLastName(lastname);
                node.setPassword(password);
                node.setEmail(email);
                node.setEnabled(true);
                node.setAttribute(Constants.NODE_LOGIN_MODE, survey.getAttribute(Constants.SURVEY_DEFAULT_LOGIN_MODE));
                Set<Group> groups = new HashSet<Group>();
                groups.add(groupDao.findByName(Constants.GROUP_ALL));
                groups.add(groupDao.findByName(Constants.GROUP_USER));
                
                String selfRegisterGroup = survey.getAttribute(Constants.SURVEY_SELF_REGISTER_GROUPS);
                logger.debug("self registered user default group: " + selfRegisterGroup);
                if (selfRegisterGroup != null){
                	groups.add(groupDao.findByName(selfRegisterGroup));
                }
                
                node.setGroups(groups);
                Set<Role> roles = new HashSet<Role>();
                roles.add(roleDao.findByName(Constants.ROLE_USER));
                node.setRoles(roles);
                nodeDao.save(node);
                logger.debug("new node (username=" + node.getUsername() + ") is created.");
                
                // forward
                request.setAttribute("origin", "register");
                sc.getRequestDispatcher("/s?r=" + node.getUsername()).forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(e.getMessage());
        }
    }
}
