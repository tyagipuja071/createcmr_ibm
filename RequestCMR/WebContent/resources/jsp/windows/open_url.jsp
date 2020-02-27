<%@ page import="com.ibm.cio.cmr.request.ui.UIMgr"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page language="java" contentType="text/html"%>
<%@page import="com.ibm.cio.cmr.request.model.requestentry.ValidationUrlModel"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>

<%
  ValidationUrlModel val = (ValidationUrlModel) request.getAttribute("val");
%>
<c:import url="<%=val.getUrl()%>" />