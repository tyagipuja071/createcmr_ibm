<%@page import="com.ibm.cio.cmr.request.config.ConfigItem"%>
<%@page import="java.util.Collections"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Collection"%>
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
    String error = null;
	ArrayList allitems = new ArrayList();
	ConfigItem item = null;
	if (map != null) {
		Collection items = (Collection) map.get("items");
		if (items != null){
			allitems.addAll(items);
		}
	} 
	if (map != null && map.get("ERROR") != null) {
		error = (String) map.get("ERROR");
	}
	Collections.sort(allitems);
%>
<style>
a.back { 
  color: blue;
  cursor: pointer;
}
a.back:hover {
  color: blue;
  text-decoration:underline;
} 

</style>
<style>
  table.ibm-data-table th, table.ibm-data-table td, table.ibm-data-table a, .ibm-type table caption em {
    letter-spacing: 1px;
  }
  table.ibm-data-table td:NTH-CHILD(4) {
    font-size: 12px;
  }
</style>
<script>
  function editParameter(id){
	  window.location='systemParameterEdit?configItem='+id;
  }
</script>
<%if (error != null){%>
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
		<!-- Main Content -->
		<div class="ibm-col-1-1">
			<div id="wwq-content">
	
					<div class="ibm-columns">
						<div class="ibm-col-1-1" style="width:915px">
				            <table cellspacing="0" cellpadding="0" border="0" summary="System Parameters" class="ibm-data-table ibm-sortable-table ibm-alternating">
				                <caption>
				                <em>System Parameters</em>
				                </caption>
				                <thead>
				                  <tr>
				                    <th scope="col" width="25%">Name</th>
				                    <th scope="col" width="*">Description</th>
				                    <th scope="col" width="10%">Required</th>
				                    <th scope="col" width="25%">Value</th>
				                  </tr>
				                </thead>
				                <tbody>
								<%for (int i = 0; i < allitems.size(); i++){%>
								<%item = (ConfigItem) allitems.get(i);%>
				                  <tr>
				                    <%if (item.isEditable()){%>
					                    <td><a href="javascript: editParameter('<%=item.getId()%>')"><%=item.getName()%></a></td>
				                    <%} else {%>
					                    <td><%=item.getName()%></td>
				                    <%}%>
				                    
				                    <td><%=item.getDescription()%></td>
				                    
				                    <td><%=item.isRequired()? "Yes" : "No"%>
				                    <%if ("textarea".equals(item.getEditType())){%>
					                    <%if ("".equals(item.getValue().trim())){ %>
						                    <td>&nbsp;</td>
						                <%} else { %>
						                    <td>(some long text)</td>
						                <%} %>
				                    <%} else {%>
					                    <td><%=item.getValue()%></td>
				                    <%}%>
				                    
				                  </tr>
								<%}%>
				                </tbody>
	  		              	</table>					
	              		</div>
					</div>
			</div>
		</div>
	</div>
<%}%>