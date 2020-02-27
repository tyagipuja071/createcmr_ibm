<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="org.springframework.ui.ModelMap"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<link href="//1.www.s81c.com/common/v17e/css/data.css" rel="stylesheet" title="www" type="text/css" />
<%
	ModelMap map = (ModelMap) request.getAttribute("map");
	Map status = null;
	String error = null;
	if (map != null && map.get("items") != null) {
		status = (Map) map.get("items");
	}
	if (map != null && map.get("ERROR") != null) {
		error = (String) map.get("ERROR");
	}
	if (status == null){
		status = new HashMap();
	}
%>
<script>
	function refresh(id) {
		window.location = 'systemRefresh?rid=' + id;
	}
</script>
<style>
div.cmr-sub {
	border-top: 1px Solid #eaeaea;
}
a.back { 
  color: blue;
  cursor: pointer;
}
a.back:hover {
  color: blue;
  text-decoration:underline;
} 
span.refresh-y {
  padding-left:10px;
  color:green;
}
span.refresh-n {
  padding-left:10px;
  color:red;
}
</style>
<div class="ibm-columns">
	<!-- Main Content -->
	<div class="ibm-col-1-1">
		<div id="wwq-content">

			<%if (error != null) {%>
				<div class="ibm-columns">
					<div class="ibm-col-1-1">
						<h3 style="color:red"><%=error%></h3>
					</div>
				</div>
				<div class="ibm-columns">
					<div class="ibm-col-1-1">
						<h3>
   							Click <a class="back" onclick="history.go(-1);">here</a> to go back to last known stable state. <br/>
						</h3>
					</div>
				</div>
				
			<%} else {%>
					<div class="ibm-columns">
						<div class="ibm-col-1-1" style="width:915px">
				            <table cellspacing="0" cellpadding="0" border="0" summary="System Refresh" class="ibm-data-table ibm-sortable-table ibm-alternating">
				                <caption>
				                <em>System Refresh</em>
				                </caption>
				                <tbody>
				                  <tr>
				                    <td width="30%">System Parameters</td>
				                    <td width="70%">
					                    <input class="ibm-btn-cancel-sec ibm-btn-small" type="button"
											name="ibm-cancel" value="Refresh" onClick="refresh('syspar')">
										<%if(status.get("syspar") != null){ %>
										    <%if ("Y".equals(status.get("syspar"))){%>
										      <span class="refresh-y">Refreshed successfully</span>
										    <%}else{%>
										      <span class="refresh-n">Refresh unsuccessful</span>
										    <%}%> 
										<%}%>
									</td>
				                  </tr>
				                  <tr>
				                    <td width="30%">Queries</td>
				                    <td width="70%">
					                    <input class="ibm-btn-cancel-sec ibm-btn-small" type="button"
											name="ibm-cancel" value="Refresh" onClick="refresh('query')">
										<%if(status.get("query") != null){ %>
										    <%if ("Y".equals(status.get("query"))){%>
										      <span class="refresh-y">Refreshed successfully</span>
										    <%}else{%>
										      <span class="refresh-n">Refresh unsuccessful</span>
										    <%}%> 
										<%}%>
									</td>
				                  </tr>
				                  <tr>
				                    <td width="30%">Messages</td>
				                    <td width="70%">
					                    <input class="ibm-btn-cancel-sec ibm-btn-small" type="button"
											name="ibm-cancel" value="Refresh" onClick="refresh('msg')">
										<%if(status.get("msg") != null){ %>
										    <%if ("Y".equals(status.get("msg"))){%>
										      <span class="refresh-y">Refreshed successfully</span>
										    <%}else{%>
										      <span class="refresh-n">Refresh unsuccessful</span>
										    <%}%> 
										<%}%>
									</td>
				                  </tr>
				                  <tr>
				                    <td width="30%">Cache</td>
				                    <td width="70%">
					                    <input class="ibm-btn-cancel-sec ibm-btn-small" type="button"
											name="ibm-cancel" value="Refresh" onClick="refresh('cache')">
										<%if(status.get("cache") != null){ %>
										    <%if ("Y".equals(status.get("cache"))){%>
										      <span class="refresh-y">Refreshed successfully</span>
										    <%}else{%>
										      <span class="refresh-n">Refresh unsuccessful</span>
										    <%}%> 
										<%}%>
									</td>
				                  </tr>
				                  <tr>
				                    <td width="30%">UI Labels</td>
				                    <td width="70%">
					                    <input class="ibm-btn-cancel-sec ibm-btn-small" type="button"
											name="ibm-cancel" value="Refresh" onClick="refresh('ui')">
										<%if(status.get("ui") != null){ %>
										    <%if ("Y".equals(status.get("ui"))){%>
										      <span class="refresh-y">Refreshed successfully</span>
										    <%}else{%>
										      <span class="refresh-n">Refresh unsuccessful</span>
										    <%}%> 
										<%}%>
									</td>
				                  </tr>
				                </tbody>
				            </table>
				        </div>
				    </div>
					<div class="ibm-columns">
						<div class="ibm-col-6-1">
							<input class="ibm-btn-cancel-pri ibm-btn-small" type="button"
								name="ibm-cancel" value="Refresh All" onClick="refresh('all')">
						</div>
					</div>
				</div>
			<%}%>
		</div>
	</div>
</div>
