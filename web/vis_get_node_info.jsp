<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="ciknow.vis.NodeDetails" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%
	String nodeId = request.getParameter("node");
	NodeDetails nd = new NodeDetails(Long.parseLong(nodeId));
%>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link rel="shortcut icon" href="favicon.ico" type="image/x-icon"/>
	<link rel="icon" href="favicon.ico" type="image/x-icon"/>
	<link href="ciknow.html.css" rel="stylesheet" type="text/css">
	<title>Node Information: </title>
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
	<table class='nodeTable'>
		<%=nd.toHtml() %>
	</table>
</body>
</html>