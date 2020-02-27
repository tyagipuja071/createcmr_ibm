<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%
String siteID = "IBMREQUESTCMRTOOL";
if ("TEST".equals(SystemConfiguration.getValue("SYSTEM_TYPE","TEST"))){
	siteID = "IBMREQUESTCMRTOOLTEST";
}
%>
<!-- IDA Stats -->
		<script type="text/javascript">
			digitalData = {
				page: {
					pageInfo: {
						pageName: '<tiles:getAsString name="title" />',
						language: 'en-US',					
						publisher: 'IBM Corporation',		
						version: 'v17e',					
						ibm: {
							country: 'US',					
							industry: 'IBM Industry',		
							owner: 'Rajesh Singi/Atlanta/IBM@IBMUS',	
							siteID: '<%=siteID%>',				
							description: "CMR Search Tool",
							keywords: 'ww, cmrsearch'
						}
					},
					category: {
						primaryCategory: 'CMR Tools'		
					}
				}
			}
	</script>
<script src="https://www.ibm.com/common/stats/ida_stats.js" type="text/javascript">//</script>
<!-- End IDA Stats -->