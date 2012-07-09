<%@ taglib prefix='c' uri='http://java.sun.com/jstl/core'%>
<%@ page import="ciknow.dao.SurveyDao"%>
<%@ page import="ciknow.util.*"%>
<%@ page import="ciknow.domain.Survey"%>
<%@ page import="java.util.*"%>
<%@ page import="net.tanesha.recaptcha.ReCaptcha" %>
<%@ page import="net.tanesha.recaptcha.ReCaptchaFactory" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page pageEncoding="UTF-8" %> 
<%
    SurveyDao surveyDao = (SurveyDao) Beans.getBean("surveyDao");
    Survey survey = surveyDao.findById(1L);

    String msg = (String) request.getAttribute("msg");
    boolean showForm = true;
    if (!GeneralUtil.verify(survey.getAttributes(), Constants.SURVEY_ALLOW_SELF_REGISTER)){
    	msg = "This CIKNOW instance doesn't allow external users, please contact administrator if you are not happy about this.";
    	showForm = false;
    }
    if (msg == null || msg.length() == 0) msg = "";
    
	String username = request.getParameter("username");	
	String firstname = request.getParameter("firstname");
	String lastname = request.getParameter("lastname");
	String password = request.getParameter("password");   
	String email = request.getParameter("email");
	
	if (username==null) username = "";
	if (firstname==null) firstname = "";
	if (lastname==null) lastname = "";
	if (password==null) password = "";
	if (email == null) email = "";
	
	PropsUtil props = new PropsUtil("ciknow");
    ReCaptcha c = ReCaptchaFactory.newReCaptcha(
    		props.get("recaptcha_public_key"), 
		    props.get("recaptcha_private_key"), false);
%>
<html>
<head>
	<title>Registration</title>
    <link rel="shortcut icon" href="favicon.ico" type="image/x-icon"/>
    <link rel="icon" href="favicon.ico" type="image/x-icon"/>
	<link rel=stylesheet type="text/css" href="ciknow.html.css">

	<script language="JavaScript">	
		function validate(form) {
			var e = form.elements, m = '';
			if(!e['username'].value) {m += '- Username is required.\n';}
			if(!e['firstname'].value) {m += '- First name is required.\n';}
			if(!e['lastname'].value) {m += '- Last name is required.\n';}		
			if(!e['password'].value) {m += '- Password is required.\n';}
			if(e['password'].value != e['confirm'].value) {
				m += '- Your password and confirmation password do not match.\n';
			}
			if(!/.+@[^.]+(\.[^.]+)+/.test(e['email'].value)) {
				m += '- E-mail requires a valid e-mail address.\n';
			}		
			if(m) {
				alert('The following error(s) occurred:\n\n' + m);
				return false;
			}
			return true;
		}	
	</script>
	
	<style type="text/css">
		#container {
			width: 500px;
			margin: 0 auto;
		}
		
		.login {
			border: 1px dotted #666666;
			margin: 0 auto;
		}		
	</style>
</head>

<body onload="document.forms[0].elements[0].focus()">
<p class="error"><%=msg %></p>
<%if (showForm){%>
	<div id="container">
	<form id="form" action="surveyRegistration" method="POST" onsubmit="return validate(this)">
		<table class="login">
			<tr>
				<td>Username:</td>
				<td>
					<input tabindex="1" type='text' name="username" value="<%=username %>"/>
				</td>
			</tr>

			<tr>
				<td>First Name:</td>
				<td>
					<input tabindex="2" type='text' name="firstname" value="<%=firstname %>"/>
				</td>
			</tr>
			
			<tr>
				<td>Last Name:</td>
				<td>
					<input tabindex="3" type='text' name="lastname" value="<%=lastname %>"/>
				</td>
			</tr>
									
			<tr>
				<td>Password:</td>
				<td>
					<input tabindex="3" type='password' name="password" value="<%=password %>">
				</td>
			</tr>
			
			<tr>
				<td>Confirm:</td>
				<td>
					<input tabindex="4" type='password' name="confirm">
				</td>
			</tr>	
			
			<tr>
				<td>Email:</td>
				<td>
					<input tabindex="5" type='text' name="email" value="<%=email %>" />
				</td>
			</tr>
			
			<tr>
              	<td>&nbsp;</td>
                <td><% out.print(c.createRecaptchaHtml(null, "white", 5)); %></td>
            </tr>		
			<tr>
				<td colspan='2'>
					<input tabindex="6" name="submit" type="submit" value="Register">
				</td>
			</tr>
			<tr><td></td></tr>
			<tr>
				<td colspan='2' style="font-size: 10px; font-style: italic">
					Already registered? Please <a href="surveyLogin.jsp"><b>LOGIN</b></a>.
				</td>
			</tr>			
		</table>
	</form>
	</div>
<%} %>
</body>
</html>
