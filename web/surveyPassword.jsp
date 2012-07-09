﻿<%@ taglib prefix='c' uri='http://java.sun.com/jstl/core' %>
<%@ page import="org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter" %>
<%@ page import="org.springframework.security.core.AuthenticationException" %>
<%@ page import="ciknow.dao.*" %>
<%@ page import="ciknow.domain.*" %>
<%@ page import="java.util.*" %>
<%@ page import="ciknow.util.*" %>
<%@ page import="java.io.*" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page pageEncoding="UTF-8" %> 


<%
	String username = (String) request.getAttribute("username");
	if (username == null || username.isEmpty()) {
		out.println("You are not here from the right place. We cannot identify you.");
		return;
	}	
	
	NodeDao nodeDao = (NodeDao) Beans.getBean("nodeDao");
	Node node = nodeDao.findByUsername(username);
	if (node == null) {
		out.println("Cannot identify respondent (username=" + username + ") in the system.");
		return;
	}
	
    SurveyDao surveyDao = (SurveyDao) Beans.getBean("surveyDao");
    Survey survey = surveyDao.findById(1L);
    if (!GeneralUtil.verify(survey.getAttributes(), Constants.SURVEY_REQUIRE_PASSWORD)){
    	out.println("You are not here from the right place. This survey don't require password at all.");
    	return;
    }
    
    String msg = (String) request.getAttribute("msg");
%>

<html>
<head>
    <title>SURVEY PASSWORD REQUIRED</title>
    
    <!-- added by york for favicon feature
    	the icon is in the context root directory. To make sure, also put the 
    	icon in the web root directory, e.g. tomcat/webapps/ROOT
    -->	
    <link rel="shortcut icon" href="favicon.ico" type="image/x-icon"/>
    <link rel="icon" href="favicon.ico" type="image/x-icon"/>
    
    <link rel=stylesheet type="text/css" href="ciknow.html.css">

    <style type="text/css">
        
        #container {
            width: 500px;
            margin: 0 auto;
			position: relative;
        }
        
        #form table{   
        	border: none; 
        	width: 400px;
        	margin: 0 auto;
        }
        
        #form input[type=text], #form input[type=password] {
            border-radius: 5px;
            width: 160px;
            font-size: 18px;
            color: #5D5E6B;
        }
        
        #form input[type=button], #form input[type=submit] {
            background: url('images/login-button.png');
            border: none;
            width: 95px;
            height: 32px;
            font-size: 18px;
            color: #5D5E6B;
            cursor: pointer;
        }
          
        .error {
			width: 500px;
			color: red;
        }
        
    </style>
</head>

<body onload="document.forms[0].elements[0].focus()">
    <div id="container">
        <div class="error">
        	<c:if test="${not empty msg}">
                    <p><%=msg %></p>
            </c:if>
        </div>        
        <form id="form" action="surveyPassword" method="POST">
        	<table>
        		<tr>
        			<td colspan="2"><input type="hidden" name="username" value="<%=username %>"/></td>
        		</tr>
        		<tr>
        			<td>Password</td>
        			<td><input tabindex="1" type='password' name='password'/></td>
        			<td><input tabindex="2" name="submit" type="submit" value="Login"/></td>
        		</tr>
        	</table>
        </form>
	</div>

</body>
</html>
