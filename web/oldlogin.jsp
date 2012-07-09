﻿<%@ taglib prefix='c' uri='http://java.sun.com/jstl/core' %>
<%@ page import="org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter" %>
<%@ page import="org.springframework.security.core.AuthenticationException" %>
<%@ page import="ciknow.dao.SurveyDao" %>
<%@ page import="ciknow.domain.Survey" %>
<%@ page import="java.util.*" %>
<%@ page import="ciknow.util.*" %>
<%@ page import="java.io.*" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page pageEncoding="UTF-8" %> 


<%
    SurveyDao surveyDao = (SurveyDao) Beans.getBean("surveyDao");
    Survey survey = surveyDao.findById(1L);

    boolean allowExternalUsers = true;
    if (survey.getAttribute(Constants.SURVEY_ALLOW_SELF_REGISTER) == null ||
    	!survey.getAttribute(Constants.SURVEY_ALLOW_SELF_REGISTER).equals("Y")){
    	allowExternalUsers = false;
    }
    
    String email = null;
    Boolean showLoginList = false;
    if (survey != null) {
        Map<String, String> attributes = survey.getAttributes();
        String emailKey = Constants.SURVEY_ADMIN_EMAIL;
        email = attributes.get(emailKey);
        String loginListKey = Constants.SURVEY_SHOW_LOGIN_LIST;
        showLoginList = (attributes.get(loginListKey) != null && attributes.get(loginListKey).equals("Y"));
    }

    String username = request.getParameter("username");
    if (username == null) username = "";
    
    // check last update
    String obsolete = "";
    try{
	    ServletContext sc = session.getServletContext();
	    String realPath = (String)sc.getAttribute(Constants.APP_REAL_PATH);
	    String prefix = realPath.substring(0, realPath.lastIndexOf("webapps") + 7);
	    String ciknowmgrFile = prefix + "/ciknowmgr/WEB-INF/classes/ciknowmgr.properties";
	    Date ciknowmgrLastUpdate = GeneralUtil.getLastUpdateTime(ciknowmgrFile);
	    String ciknowFile = realPath + "WEB-INF/classes/ciknow.properties";
	    Date ciknowLastUpdate = GeneralUtil.getLastUpdateTime(ciknowFile);    
	    if (ciknowLastUpdate.before(ciknowmgrLastUpdate)){
	    	String baseUrl = (String)sc.getAttribute(Constants.APP_BASE_URL);
	    	String urlPrefix = baseUrl.substring(0, baseUrl.lastIndexOf("/"));
	    	obsolete = "There is a newer C-IKNOW version for <a href='" + urlPrefix + "/ciknowmgr'>UPDATE</a>."; 
	    	System.out.println(obsolete);		
	    }
    } catch (Exception e){
    	System.out.println(e.getMessage());
    }
%>

<html>
<head>
    <title>CIKNOW Login</title>
    
    <!-- added by york for favicon feature
    	the icon is in the context root directory. To make sure, also put the 
    	icon in the web root directory, e.g. tomcat/webapps/ROOT
    -->	
    <link rel="shortcut icon" href="favicon.ico" type="image/x-icon"/>
    <link rel="icon" href="favicon.ico" type="image/x-icon"/>
    
    <link rel=stylesheet type="text/css" href="ciknow.html.css">

    <script type="text/javascript">
        function get_help(addr)
        {
          top.location = "mailto:"+addr;
          return false;
        }    
    </script>
    <style type="text/css">
        
        #container {
            background: url('images/login.png');
            width: 500px;
            height: 557px;
            margin: 0 auto;
			position: relative;
        }
        
        #head {
            width: 500px;
			position: absolute;
			top: 143px;
        }
        
        #head h1 {
            color: #000444;
            font-weight: normal;
            font-size: 40px;
            margin-bottom: 0;
        }
        
        #head h2 {
            color: #9299f9;
            font-weight: normal;
            font-size: 18px;
            font-style: italic;
            margin-top: 5px;
        }
        
        #head h3 {
            color: #9299F9;
            font-weight: normal;
            margin-top: 25px;
            font-size: 12px;
        }
        
        #login {
            text-align: left;
			position: absolute;
			top: 350px;
			left: 120px;
        }
        
        #login span {
            color: #5D5E6B;
            font-size: 20px;
            width: 105px;
            display: inline-block;
            padding-bottom: 11px;
            text-align: left;
        }
        
        #login input[type=text], #login input[type=password] {
            border: 1px solid #5D5E6B;
            border-radius: 5px;
            width: 160px;
            font-size: 18px;
            color: #5D5E6B;
        }
        
        #login input[type=button], #login input[type=submit] {
            background: url('images/login-button.png');
            border: none;
            width: 95px;
            height: 32px;
            font-size: 17px;
            color: #5D5E6B;
            cursor: pointer;
        }
        
        .forgot {
			color: #5D5E6B;
			position: absolute;
			top: 130px;
        }
        
        #footer {
			color: #5F5F6C;
			font-size: 12px;
			width: 480px;
			top: 507px;
			position: absolute;
			left: 11px;
        }
        
        #footer .topleft {
            float: left;
            margin-bottom: 0;
        }
        
        #footer .topright {
            float: right;
            margin-bottom: 0;
        }
        
        #footer .fullline {
            clear: both;
            font-size: 9px;
            padding-top: 5px;
        }
                
        .error {
        	position: absolute;
			width: 500px;
			top: 300px;
        }
        
        .error p {
        	margin: 0;
        }

		#logo img {
			width: 350px;
		}
		
		#logo {
			position: absolute;
			top: 31px;
			width: 500px;
		}
        
    </style>
</head>

<body onload="document.forms[0].elements[0].focus()">
    <div id="container">
		<div id="logo">
			<img src="images/logo.png" alt="C-IKNOW Logo" />
		</div>
        <div id="head">
            <h1><%=survey.getName()%></h1>
            <h2><%=survey.getDescription()%></h2>
            <h3>contact <%=email%> with any questions</h3>
        </div>
        <div class="error">
        	<c:if test="${not empty param.login_error}">
                    <p>Your login attempt was not successful, try again.</p>
                    <p>Reason: <%= ((AuthenticationException) session.getAttribute(AbstractAuthenticationProcessingFilter.SPRING_SECURITY_LAST_EXCEPTION_KEY)).getMessage() %></p>
            </c:if>
            <c:if test="${not empty param.logout}">
                    You are logged out.
            </c:if>
        </div>        
        <div id="login">
            <form id="loginForm" action="<c:url value='j_spring_security_check'/>" method="POST">
                <div>
            <span>Username</span><input id="username" tabindex="1" type='text' name='j_username'                  
                                  <c:choose>
                                      <c:when test="${not empty param.login_error}">value='<c:out value="${SPRING_SECURITY_LAST_USERNAME}"/>'</c:when>
                                      <c:otherwise>
                                          value='<%=username%>'
                                      </c:otherwise>
                                  </c:choose>
                                  /> 
                </div>
                <div>
            <span>Password</span><input tabindex="2" type='password' name='j_password'>
                </div>
                <div>
                    <input tabindex="3" name="submit" type="submit" value="Login">
                    <input tabindex="4" type="button" value="Help" onClick="top.location='http://ciknow.northwestern.edu/documentation'">
                </div>
            </form>
                <p class="forgot">Forgot <a href="#">username</a> or <a href="#">password</a>?</p>
        </div>
                <div id="footer">
                    <p class="topleft">C-IKNOW</p>
                    <p class="topright"><a href="#">more info</a></p>
                    <p class="fullline">&copy; 2012 Noshir Contractor, Science of Networks in Communities (SONIC) Northwestern University, Evanston, Il</p>
                </div>
    </div>
</table>

</body>
</html>
