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
<%
	ModelMap map = (ModelMap) request.getAttribute("map");
    String error = null;
	ConfigItem item = null;
	if (map != null) {
		item = (ConfigItem) map.get("items");
	} 
	if (map != null && map.get("ERROR") != null) {
		error = (String) map.get("ERROR");
	}
	if (item == null){
		error = "Invalid Configuration Item.";
	}
	String validationError = (String) map.get("VALIDATION");
	String info = (String) map.get("INFO");
	String oldValue = (String) map.get("OLDVALUE");
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
<script>
  function saveParameter(id){
	  var id = document.getElementById('configId').value;
	  var field = document.getElementById('itemvalue');
	  var value = "";
	  if (field.tagName == 'INPUT'){
		value = field.value;  
	  } else {
		value = field.value;  
	  }
	  var url = 'systemParameterEdit?action=save&configItem='+id+'&itemvalue='+encodeURIComponent(value);
	  window.location = url;
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
	<input type="hidden" name="configId" value="<%=item.getId()%>" id="configId">
		<!-- Main Content -->
		<div class="ibm-col-1-1">
			<div id="wwq-content">
<%	
String itemValue = oldValue != null ? oldValue : item.getValue();
%>
					<%if (validationError != null){%>	
						<div class="ibm-columns">
							<div class="ibm-col-1-1">
							  <h3 style="color:red"><%=validationError%></h3>
		              		</div>
						</div>
					<%} else if (info != null){%>
						<div class="ibm-columns">
							<div class="ibm-col-1-1">
							  <h3 style="color:green"><%=info%></h3>
		              		</div>
						</div>
					<%}%>
					<div class="ibm-columns">
						<div class="ibm-col-1-1">
						  <h3><%=item.getName()%></h3>
	              		</div>
					</div>
					<div class="ibm-columns">
						<div class="ibm-col-1-1">
						  <p>
						  <%=item.getDescription()%>&nbsp;<span class="ibm-item-note">[Expected Value: <%=item.getType()%>]</span>
						  </p>
	              		</div>
					</div>
					<div class="ibm-columns">
						<div class="ibm-col-1-1">
						<%if ("text".equals(item.getEditType())){%>
						  <input id="itemvalue" type="text" name="itemvalue" value="<%=itemValue%>" style="width:500px">
						<%} else if ("textarea".equals(item.getEditType())) {%>
						  <textarea id="itemvalue" name="itemvalue" rows="15" cols="80"><%=itemValue%></textarea>
						<%}%>
	              		</div>
					</div>
					<%if (item.getHint() != null){%>
					<div class="ibm-columns">
						<div class="ibm-col-1-1">
						  <span class="ibm-item-note"><span class="ibm-required">*</span>Hint:&nbsp;<%=item.getHint()%></span>
						</div>
				    </div>
				    <%}%>
					<br>
					<div class="ibm-columns">
						<div class="ibm-col-1-1">
							<input class="ibm-btn-cancel-pri ibm-btn-small" type="button"
								name="ibm-cancel" value="Save" onClick="saveParameter()">
							<span class="ibm-sep">&nbsp;</span> 
							<input class="ibm-btn-cancel-sec ibm-btn-small" type="button"
								name="ibm-cancel" value="Cancel" onClick="window.location='systemParameters'">
						</div>
					</div>
			</div>
		</div>
	</div>
<%}%>