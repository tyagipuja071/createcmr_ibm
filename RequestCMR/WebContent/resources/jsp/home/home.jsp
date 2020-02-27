<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<style>
div.cmr-alert {
 border:1px Solid #FF7777; 
 padding:10px;
 padding-left: 20px;
 border-radius: 10px;
}
</style>
<script>
dojo.addOnLoad(function(){
  dojo.cookie('lastTab', null);
  if (!BrowserCheck.isFirefox() && !BrowserCheck.isChrome()){
    var msg = 'You are using an unsupported browser. The functions on the pages may not work properly.<br>';
    msg += 'Please use either Firefox or Chrome to access the tool to ensure all functions work correctly.';
    cmr.showAlert(msg);
  }
  if (BrowserCheck.isFirefox() && BrowserCheck.getFirefoxVersion() < 52){
    var msg = 'You are using an older version of Firefox.  It is recommended to use at least Firefox v52.<br>';
    msg += 'Some functions of the application may not work correctly until you update to the required version.';
    cmr.showAlert(msg);
  }
});
</script>
<%
  String alert = SystemConfiguration.getValue("ALERT",null); 
  String info = SystemConfiguration.getValue("INFO",null); 
  String home = SystemConfiguration.getValue("HOME",null); 
%>
<style>
  div.news-header {
    font-weight: 300;
    font-family : "HelveticaNeue-Bold","HelvBoldIBM",Arial,sans-serif;
  }
  div.news-content {
    width: 200px !important;
    height: 230px;
    overflow-x : hidden;
    overflow-y : auto;
    font-size: 14px;
    border: 1px Solid #AAAAAA;
    border-radius : 5px;
    padding: 3px;
  }
  div.news-content ul, div.news-content li {
    padding : 0;
    width: 185px !important;
    word-wrap : break-word;
  }
  div.news-content ul, div.news-content li::before {
    content : none;
  }
  div.news-content ul {
    list-style: inside;
  }
</style>
<div class="ibm-columns">

	<div class="ibm-col-1-1">
		<!-- start main content -->
		<div id="wwq-content">
			
			<%if (alert != null && !"".equals(alert.trim())){%>		    
			<div class="ibm-columns">
				<div class="ibm-col-1-1">
					<div class="cmr-alert">
					<%=alert%>
					</div>
				</div>
			</div>
			<div class="ibm-columns">
				<div class="ibm-col-1-1">
				&nbsp;
				</div>
			</div>
			<%} %>
			<div class="ibm-columns">
				<div class="ibm-col-1-1">
							
					<div class="ibm-columns">
						<div class="ibm-col-4-3">
							<div id="welcome_note_id">
								<%=home%>
							</div>
													
						</div>
						<div class="ibm-col-4-1">
             <div class="news-header">News</div>
             <div class="news-content">
               <ul>
                      <%=info%>
               </ul>
             </div>
						</div>
					</div>
					
<!-- 					<div class="ibm-columns">
						<div class="ibm-col-1-1">
							<div class="ibm-container ibm-show-hide ibm-alternate">
								<h2>
									<a class="" href="#" style="">Sales Leaders</a>
								</h2>
								<div class="ibm-container-body" style="overflow: hidden; height: 1px; display: none;">
									<div class="ibm-columns">
										<div class="ibm-col-4-3" id="sales_leader_id">
										</div>
										<div class="ibm-col-4-1" id="sales_leader_bookmark_id">
											<h2 style="border-top: 0px !important;">Userful bookmarks for Sales Leaders</h2>
										</div>
									</div>	
								</div>
							</div>
						</div>
					</div>
					
					<div class="ibm-columns">
						<div class="ibm-col-1-1">
							<div class="ibm-container ibm-show-hide ibm-alternate">
								<h2>
									<a class="" href="#">IMT/GMT Quota Teams</a>
								</h2>
								<div class="ibm-container-body" style="overflow: hidden; height: 1px; display: none;">
									<div class="ibm-columns">
										<div class="ibm-col-4-3" id="quota_team_id">
										</div>
										<div class="ibm-col-4-1" id="quota_team_bookmark_id">
											<h2 style="border-top: 0px !important;">Userful bookmarks for Quota Teams</h2>
										</div>
									</div>
								</div>
							</div>
						</div>
					</div>	
					
					<div class="ibm-columns">	
						<div class="ibm-col-1-1">
							<div class="ibm-container ibm-show-hide">
								<h2>
									<a class="ibm-show-active" href="#show-hide">Quota calendar and support</a>
								</h2>
								<div class="ibm-container-body" style="overflow: hidden; height: auto; display: block;">
									<div class="ibm-columns">
										<div class="ibm-col-4-3" id="calendar_id">
										</div>
										<div class="ibm-col-4-1" id="support_id">
											<h2 style="border-top: 0px !important;">Support</h2>
										</div>
									</div>
								</div>
							</div>
						</div>					
					</div>	 -->									
				</div>
			</div>
			
			<!-- stop main content -->
		</div>
	</div>
</div>
