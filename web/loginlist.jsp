<%@ taglib prefix='c' uri='http://java.sun.com/jstl/core' %>
<%@ page import="ciknow.util.Beans" %>
<%@ page import="java.util.List" %>
<%@ page import="ciknow.dao.NodeDao" %>
<%@ page import="ciknow.domain.Node" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page pageEncoding="UTF-8" %> 
<%
    NodeDao nodeDao = (NodeDao) Beans.getBean("nodeDao");
    List<Node> nodes = nodeDao.findEnabledUser();

%>
<html>
<head>
    <title>User Logins</title>
    <link rel="shortcut icon" href="favicon.ico" type="image/x-icon"/>
    <link rel="icon" href="favicon.ico" type="image/x-icon"/>    
    <link rel=stylesheet type="text/css" href="ciknow.html.css">
</head>

<body>
<h2> List of active login accounts </h2>
<table align="center">
    <tr>
        <th width="200">Name</th>
        <th width="100">Login</th>
        <!-- th width="100">Active</th-->
    </tr>
    <% for (Node node : nodes){
    %>
        <tr>
            <td width="200"><%=node.getLastName()%> ,&nbsp; <%=node.getFirstName()%></td>
            <td width="100"><a href="login.jsp?username=<%=node.getUsername()%>"><%=node.getUsername()%></a></td>
            <!--td align="center"><%=node.getEnabled()%></td-->
        </tr>
    <%}%>
</table>
</body>
</html>