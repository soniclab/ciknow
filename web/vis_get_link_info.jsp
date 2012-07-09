<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="ciknow.vis.EdgeDetails" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%
	String fid = request.getParameter("from");
	String tid = request.getParameter("to");
	String edgeType = request.getParameter("type");
	
	EdgeDetails ed = new EdgeDetails(Long.parseLong(fid), Long.parseLong(tid), edgeType);
%>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<link rel="shortcut icon" href="favicon.ico" type="image/x-icon"/>
	<link rel="icon" href="favicon.ico" type="image/x-icon"/>
	<link href="ciknow.html.css" rel="stylesheet" type="text/css">
	<title>Edge Information:</title>
	<script type="text/javascript">
	    function toggle(id) {
	        var node = document.getElementById(id);
	        var icon = document.getElementById(id + "_icon");
	        if (node.style.display == 'none') {
	            icon.src = 'images/minus.gif'
	            node.style.display = 'block';
	        } else {
	            icon.src = 'images/plus.gif'
	            node.style.display = 'none';
	        }
	    }
	</script>
</head>

<body>
	<table class='edgeTable'>
		<%=ed.toHtml() %>
	</table>
</body>

</html>