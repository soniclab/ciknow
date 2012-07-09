<%@ taglib prefix='c' uri='http://java.sun.com/jstl/core' %>
<%@ page import="ciknow.util.Beans" %>
<%@ page import="java.util.*" %>
<%@ page import="ciknow.ro.*" %>
<%@ page import="ciknow.teamassembly.*" %>
<%@ page import="ciknow.io.*" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page pageEncoding="UTF-8" %> 
<%
	String id = request.getParameter("id");
	String[] memberIds = request.getParameterValues("mid");
	
	int numTeams = Integer.parseInt(request.getParameter("numTeams"));
	int minTeamSize = Integer.parseInt(request.getParameter("minTeamSize"));
	int maxTeamSize = Integer.parseInt(request.getParameter("maxTeamSize"));
	int iterations = Integer.parseInt(request.getParameter("iterations"));
	String diversityQuestionShortName = request.getParameter("diversityQuestionShortName");
	long similarityQuestionId = Long.parseLong(request.getParameter("similarityQuestionId"));
	String[] groupIds = request.getParameterValues("groupId");
	List<String> groupIdList = Arrays.asList(groupIds);
	String[] edgeTypes = request.getParameterValues("edgeType");
	List<String> edgeTypeList = Arrays.asList(edgeTypes);
	GenericRO genericRO = (GenericRO) Beans.getBean("genericRO");
	TeamBuilder tb = genericRO.prepareTeamBuilder(numTeams, minTeamSize, maxTeamSize, iterations, diversityQuestionShortName, similarityQuestionId, groupIdList, edgeTypeList);
	
	Team team = new Team(tb, id, memberIds);
	
	TeamWriter tw = new TeamWriter();
	tw.writeTeam(team, out);
%>
