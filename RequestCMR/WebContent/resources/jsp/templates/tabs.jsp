<%@page import="com.ibm.cio.cmr.request.user.AppUser"%>
<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@page import="com.ibm.cio.cmr.request.util.BluePagesHelper" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<%
AppUser user = AppUser.getUser(request);
boolean hasPref = user != null && user.isPreferencesSet();
boolean approver = user != null && user.isApprover();

//CREATCMR-6522
boolean isTaxTeam = BluePagesHelper.isUserInUSTAXBlueGroup(user.getIntranetId());
%>

			<div id="ibm-primary-tabs">
				<ul class="ibm-tabs" role="toolbar">
          
          <!--  Approver Only View -->
          <% if (user.isApprover()){%>
            <li id="APPROVALS_TAB">
              <a href="javascript: goToUrl('${contextPath}/myappr')">My Approvals</a>
            </li>
            <li id="HELP_TAB">
              <a href="javascript: openCISupportal()" title="Report any issue, requests, or concerns to the support team.">
                Help
              </a>
            </li>
          <%} else {%>
        
            <!--  Regular user view -->
  					<%if (hasPref){%>
  					<li id="HOME_TAB">
  					  <a href="javascript: goToUrl('${contextPath}/home')">Home</a>
  					</li>
  					<li id="WORKFLOW_TAB">
  					  <a href="javascript: goToUrl('${contextPath}/workflow/open')">Workflow</a>
    				</li>
  					<li id="REQUEST_TAB">
  					  <a href="javascript: cmr.chooseNewEntry()">Request Entry</a>
     				</li>
            <li id="CHANGELOG_TAB">
              <a href="javascript: goToUrl('${contextPath}/changelog')">Change Log</a>
              </li>                				
      				<%}%>
  					<li id="PREFERENCES_TAB">
  					  <a href="javascript: goToUrl('${contextPath}/preferences')">Preferences</a>
    				</li>
            <%if (user.isHasApprovals()){%>
              <li id="APPROVALS_TAB">
                <a href="javascript: goToUrl('${contextPath}/myappr')">Approvals</a>
              </li>
            <%} %>
              <li id="SEARCH_HOME_TAB">
                <a href="javascript: goToUrl('${contextPath}/searchhome')">Search</a>
              </li>
  
  					<%if (SystemConfiguration.isAdmin(request) || (user != null && user.isCmde())){%>
                <li id="ADMIN_TAB">
                  <%if (SystemConfiguration.isAdmin(request)){%>
                    <a href="javascript: goToUrl('${contextPath}/systemParameters')">Admin</a>
                  <%} else  {%>
                    <a href="javascript: goToUrl('${contextPath}/code')">Admin</a>
                  <%} %>
                </li>
            <%}%>
            <%if (user != null && (user.isAdmin() || user.isCmde() || user.isProcessor()) ){%>
                <li id="METRICS_TAB">
                    <a href="javascript: goToUrl('${contextPath}/metrics/stats')">Metrics</a>
                </li>
            <%}%>  
            <li id="HELP_TAB">
              <a href="javascript: openCISupportal()" title="Report any issue, requests, or concerns to the support team.">
                Help
              </a>
            </li>
            <%if (user != null && (user.isAdmin() || user.isCmde() || user.isProcessor()) ){%>            
            <li id="REV_TAB">
            	<a href="javascript: goToUrl('${contextPath}/revivedcmrs')">Revive</a>
            </li>
            <%}%>
          <%}%>
				</ul>
			</div>
			<div id="ibm-secondary-tabs">
				<ul class="ibm-tabs">

				<c:if test="${primaryTabId ==  'HOME'}">
				  <!-- Home Secondary Tabs -->
				  <li id="OVERVIEW_TAB"><a href="javascript: goToUrl('${contextPath}/home')">Overview</a></li>
          <!-- <li id="LOG"><a href="javascript: showAppLog()">Release Notes</a></li>-->
          <%if (user != null && (user.isAdmin() || user.isCmde()) ){%>            
            <li id="DASHBOARD_TAB">
              <a href="javascript: goToUrl('${contextPath}/dashboard')">Dashboard</a>
            </li>
          <%}%>
				</c:if>

        <c:if test="${primaryTabId ==  'APPROVALS'}">
          <!-- Home Secondary Tabs -->
          <li id="APPROVALS_PENDING_TAB"><a href="javascript: goToUrl('${contextPath}/myappr')">Pending Approvals</a></li>
          <li id="APPROVALS_ALL_TAB"><a href="javascript: goToUrl('${contextPath}/myappr?all=Y')">All Approvals</a></li>
        </c:if>

				<c:if test="${primaryTabId ==  'ADMIN'}">
          <%if (SystemConfiguration.isAdmin(request)){ %> 
				    <li id="SYS_CONFIG_TAB"><a href="javascript: goToUrl('${contextPath}/systemParameters')">Configuration</a></li>
				    <li id="SYS_REFRESH_TAB"><a href="javascript: goToUrl('${contextPath}/systemRefresh')">Refresh</a></li>
          <%} %>
          <li id="FORCE_CHANGE_TAB"><a href="javascript: goToUrl('${contextPath}/statuschange')">Forced Status Change</a></li>
          <li id="APPROVALS_ADMIN_TAB"><a href="javascript: goToUrl('${contextPath}/approvalsadminlist')">Approvals Override</a></li>
          <%if (user != null && (SystemConfiguration.isAdmin(request) || user.isCmde())){ %> 
            <li id="USER_ADMIN_TAB"><a href="javascript: goToUrl('${contextPath}/users')">User Maintenance</a></li>
          <%} %>
          <li id="CODE_ADMIN_TAB"><a href="javascript: goToUrl('${contextPath}/code')">Code Maintenance</a></li>
          <%if (user.isCmde()){ %>
           <li id="CORRECTIONS_TAB"><a href="javascript: goToUrl('${contextPath}/corrections')">Corrections</a></li>
          <%} %>
				</c:if>
        <c:if test="${primaryTabId ==  'METRICS'}">
            <li id="METRICS_STATS_TAB"><a href="javascript: goToUrl('${contextPath}/metrics/stats')">Request Statistics</a></li>
            <li id="METRICS_ASTATS_TAB"><a href="javascript: goToUrl('${contextPath}/metrics/autostats')">Automation Statistics</a></li>
            <li id="METRICS_DAILY_TAB"><a href="javascript: goToUrl('${contextPath}/metrics/daily')">Daily Totals</a></li>
            <%if (SystemConfiguration.isAdmin(request)){ %> 
            <li id="METRICS_USAGE_TAB"><a href="javascript: goToUrl('${contextPath}/metrics/usage')">Web Service Usage</a></li>
            <%} %>
        </c:if>

        <c:if test="${primaryTabId ==  'SEARCH_HOME'}">
            <li id="DPLSEARCH_TAB"><a href="javascript: goToUrl('${contextPath}/dplsearch')">DPL Search</a></li>
            <li id="LSEARCH_TAB"><a href="javascript: goToUrl('${contextPath}/legacysearch')">Legacy DB2</a></li>
            <li id="MQSEARCH_TAB"><a href="javascript: goToUrl('${contextPath}/mqsearch')">SOF/WTAAS</a></li>
            <%if (user != null && (user.isAdmin() || user.isCmde() || user.isProcessor()) ){%>
            <li id="FILEATTACH_TAB"><a href="javascript: goToUrl('${contextPath}/attachlist')">File Attachments</a></li>
            <%}%>
            <%if (isTaxTeam || (user != null && (user.isAdmin()))){%>
            <li id="USSCC_TAB"><a href="javascript: goToUrl('${contextPath}/code/scclist?taxTeamFlag=Y')">US SCC</a></li>
            <%}%>
            <%if (user !=null && (user.isAdmin() || user.isCmde() || user.isProcessor()) ){%>
              <li id="CRISREPORT_TAB"><a href="javascript: goToUrl('${contextPath}/crisreport')">CRIS Report</a></li>
            <%}%>
        </c:if>
        

				  
    			<c:if test="${primaryTabId ==  'WORKFLOW'}">
				  <!-- Home Secondary Tabs -->
				  <li id="OPEN_REQ_TAB"><a href="javascript: goToUrl('${contextPath}/workflow/open')">Open Requests</a></li>
				  <li id="COMPLETED_REQ_TAB"><a href="javascript: goToUrl('${contextPath}/workflow/completed')">Completed Requests</a></li>
				  <li id="REJECTED_REQ_TAB"><a href="javascript: goToUrl('${contextPath}/workflow/rejected')">Rejected Requests</a></li>
				  <li id="ALL_REQ_TAB"><a href="javascript: goToUrl('${contextPath}/workflow/all')">All Requests</a></li>
				  <li id="SEARCH_REQUESTS_TAB"><a href="javascript: goToUrl('${contextPath}/workflow/search')">Search Requests</a></li>
				</c:if>
        
  				  <div class="ibm-confidential-note">IBM Confidential</div>
				</ul>
			</div>
<script>
  // higlight current menus, and remove the links
  dojo.addOnLoad(function(){
	var topMenu = document.getElementById('<%=request.getAttribute("primaryTabId")%>_TAB');
	if (topMenu) {
	  topMenu.setAttribute("class", "ibm-active");
	  var menuLink = dojo.query(topMenu).query("a");
	  if (menuLink && menuLink[0]){
	    menuLink[0].href = '#';
	  }
	}
	var subMenu = document.getElementById('<%=request.getAttribute("secondaryTabId")%>_TAB');
	if (subMenu) {
	  subMenu.setAttribute("class", "ibm-active");
	  var menuLink = dojo.query(subMenu).query("a");
	  if (menuLink && menuLink[0] && '<%=request.getAttribute("primaryTabId")%>' != 'ADMIN'){
	    menuLink[0].href = '#';
	  }
	}
  });
</script>
