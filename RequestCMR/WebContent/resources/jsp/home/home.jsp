<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<%@ taglib uri="/tags/cmr" prefix="cmr"%>
<script src="${resourcesPath}/js/system/chart.bundle.js?${cmrv}" type="text/javascript"></script>
<script>
addEventListener("DOMContentLoaded", function() {
    dojo.byId('quick_search_btn').style.display = 'none';
});
</script>
<style>
div.cmr-alert {
 border:1px Solid #FF7777; 
 padding:10px;
 padding-left: 20px;
 border-radius: 10px;
}
div.cmr-notice {
 border:2px Solid #4169E1; 
 padding:10px;
 padding-left: 20px;
 border-radius: 10px;
}

img.logo {
  width: 100px;
  height: 100px;
  border-radius: 10px;
  border: 1px Solid #AAA;
}
#ibm-content .ibm-columns {
  margin-top: 10px;
}
</style>
<script>
dojo.addOnLoad(function(){
  dojo.cookie('lastTab', null);
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
  div.logo {
   width: 100px;
   height: 100px;
   display: inline-block;
   padding-right: 10px;
  }
  div.logo-text {
   display: inline-block;
   width: 470px;
   vertical-align: top;
   padding-bottom: 20px;
  }
  
  div.quick-start {
    border: 1px Solid #666;
    border-radius: 10px;
    padding: 30px;
    margin-left: 50px;
    background: rgb(255,255,255); /* Old browsers */
    background: -moz-linear-gradient(top,  rgba(255,255,255,1) 0%, rgba(229,229,229,1) 100%); /* FF3.6-15 */
    background: -webkit-linear-gradient(top,  rgba(255,255,255,1) 0%,rgba(229,229,229,1) 100%); /* Chrome10-25,Safari5.1-6 */
    background: linear-gradient(to bottom,  rgba(255,255,255,1) 0%,rgba(229,229,229,1) 100%); /* W3C, IE10+, FF16+, Chrome26+, Opera12+, Safari7+ */
    filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#ffffff', endColorstr='#e5e5e5',GradientType=0 ); /* IE6-9 */
    padding-top: 20px;
  }
  div.home-btn {
    text-align : center;
    margin-auto;
    height: 80px;
    margin-top: 10px;
    font-size:13px;
  }
  div.home-btn input.ibm-btn-small {
    width: 190px !important;
    margin-top: 5px;
    font-size: 13px !important;
  }
  div.home-btn span {
    font-size: 13px;
    font-weight: bold;
  }
</style>
<div class="ibm-columns">

	<div class="ibm-col-1-1">
		<!-- start main content -->
			
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
						<div class="ibm-col-4-2" style="width:600px">
							<div id="welcome_note_id">
                 <div class="logo">
                   <img src="${resourcesPath}/images/CreateCMRLogo.png" class="logo">
                 </div>
                 <div class="logo-text">
                   <ul>
                     <li>
                     CreateCMR is part of the CMR Suite of Applications and supports the creation 
                     and update of Customer Master Records (CMRs) at IBM. 
                     </li>
                     <li>
                     The application also provides Web Service APIs for external systems to be able
                     to directly create requests without using the user interface. 
                     Specifications can be found <a href="https://ibm.box.com/s/6s88qa7f41ufagfeyupu86m08vwe5bp9" target="_blank">here</a>.
                     </li>
                   </ul>
                 </div>
							</div>

              <div id="welcome_note_id">
                 <div class="logo">
                   <img src="${resourcesPath}/images/cmde.png" class="logo">
                 </div>
                 <div class="logo-text">
                   <ul>
                     <li>
                     The Client  Master Data Execution (CMDE) is in charge of creation, maintenance, and overall management of customer master records (CMR) in 
                     IBM to support IBM business units and downstream partners with high data quality.
                     </li>
                     <li>
                     The world-wide team is settled in three locations - Bratislava (Slovakia), Dalian (China) and Kuala Lumpur (Malaysia). Centers manage operations 
                     for different geographical territories and handle data governance of client master records in accordance with IBM data standards.
                     </li>
                     <li>
                     To know more about the CMDE teams and processes, please visit the <a href="https://w3.ibm.com/w3publisher/cmde-cmr" target="_blank">CMDE Site</a>.
                     </li>
                   </ul>
                 </div>
              </div>
													
						</div>
						<div class="ibm-col-4-2 quick-start" style="width:300px">
              <div class="home-btn">
                <span>If you want to start a new request:</span>
                <cmr:button label="${ui.btn.createNewEntry}" onClick="cmr.chooseNewEntry()" highlight="true" pad="false"/>
              </div>
              <div class="home-btn">
                <span>If you know your request's ID <cmr:info text="You can also press Ctrl-q on any page within the application to open requests by ID"></cmr:info>:</span>
                <cmr:button label="${ui.btn.openByRequestID}" onClick="openRequestById()" highlight="false" pad="true" />
              </div>
              <div class="home-btn">
                <span>If you want to view your open requests:</span>
                <cmr:button label="View Pending Requests" onClick="goToUrl('${contextPath}/workflow/open')" highlight="false" pad="true" />
              </div>
              <div class="home-btn">
              <span>If you want to quickly search for an existing record in the IBM or D&B databases:</span>
              <input class="ibm-btn-small cmr-quick-search-btn-horizontal" type="button" value="Quick Search" onclick="openQuickSearch()" title="Quick Search">
               </div>
              <div class="home-btn">
                You can also check out some <strong><a href="https://w3.ibm.com/w3publisher/cmde-cmr/tutorials/createcmr-tutorials" target="_blank">tutorials</a></strong> from the 
                CMDE site for more information on how to create/update CMRs or watch a <strong><a href="https://video.ibm.com/embed/recorded/131988262" target="_blank">live demo</a></strong> of the tool.
              </div>
						</div>
					</div>
					
				</div>
			</div>
			
			<div class="ibm-columns">
				<div class="ibm-col-1-1">
					<div class="cmr-notice">
					For the privacy policy, please refer to the following link : <a href="https://w3.ibm.com/w3publisher/w3-privacy-notice" target="_blank">Privacy Notice</a>
					</div>
				</div>
			</div>
			
			
			<!-- stop main content -->
	</div>
</div>
