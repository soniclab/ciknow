<%@ page import="ciknow.util.Beans" %>
<%@ page import="ciknow.io.CodeBookWriter" %>
<%@ page import="ciknow.ro.GenericRO" %>
<%@ page import="org.dom4j.Document" %>   
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page pageEncoding="UTF-8" %>  
<%
  	Beans.init();
	GenericRO genericRO = (GenericRO) Beans.getBean("genericRO");
  	String realPath = genericRO.getRealPath();
  	String filename = realPath + "/codebook.xml";
  	
  	CodeBookWriter writer = (CodeBookWriter) Beans.getBean("codeBookWriter");
  	Document doc = writer.createDocument();
  	writer.serializeToXML(doc, filename);
	writer.replaceBrackets(filename);
	
  	request.getRequestDispatcher("/codebook.xml").forward(request, response);
  %>    