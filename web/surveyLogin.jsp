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
    boolean allowExternalUsers = GeneralUtil.verify(survey.getAttributes(), Constants.SURVEY_ALLOW_SELF_REGISTER);    
    String email = survey.getAttribute(Constants.SURVEY_ADMIN_EMAIL);    
    //boolean showLoginList = GeneralUtil.verify(survey.getAttributes(), Constants.SURVEY_SHOW_LOGIN_LIST);
    String username = request.getParameter("username");
    if (username == null || username.isEmpty()) {
    	username = (String)request.getAttribute("username");
    	if (username == null) username = "";
    }
    String msg = (String)request.getAttribute("msg");
    if (msg == null) msg = "";
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
        
        #forbid {
        	border: 1px solid red;
            text-align: left;
			position: absolute;
			top: 350px;
			left: 120px;
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
			height: 115px;
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
			<img src="images/logo_front.jpg" alt="C-IKNOW Logo" />
		</div>
        <div id="head">
            <h1><%=survey.getName()%></h1>
            <h2><%=survey.getDescription()%></h2>
            <h3>contact <%=email%> with any questions</h3>
        </div>
        <div class="error">
        	<p><%=msg %></p>
        </div>
        
            
        <div id="login">
        <% if (allowExternalUsers){ %>    
            <form id="loginForm" action="surveyLogin" method="POST">
                <div>
            		<span>Username</span>
            		<input tabindex="1" type='text' name='username' value='<%=username %>'/>                
                </div>
                <div>
            		<span>Password</span>
            		<input tabindex="2" type='password' name='password'>
                </div>
                <div>
                    <input tabindex="3" name="submit" type="submit" value="Login">
                </div>
                <div style="font-size: 10px; font-style: italic">
                    Don't have an account? Please <a href="surveyRegistration.jsp"><b>REGISTER</b></a>
                </div>
            </form>
            
            <!-- p class="forgot">Forgot <a href="#">username</a> or <a href="#">password</a>?</p-->
        <%} else { %>
	        <div>
	        	<p>This survey does not allow external respondents.</p>
	        </div>
        <%} %>            
        </div>

        
        <div id="footer">
            <p class="topleft">C-IKNOW</p>
            <p class="topright"><a href="http://ciknow.northwestern.edu/contact-us">more info</a></p>
            <p class="fullline">&copy; 2012 Noshir Contractor, Science of Networks in Communities (SONIC) Northwestern University, Evanston, Il</p>
        </div>
    </div>

</body>
</html>
