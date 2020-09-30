<%@page import="com.ibm.cio.cmr.request.config.SystemConfiguration"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="resourcesPath" value="${contextPath}/resources" />
<script src="${resourcesPath}/js/system/chart.bundle.js?${cmrv}" type="text/javascript"></script>
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
  window.setTimeout('generateCharts()', 500);
});

function generateCharts(){
  var creates = cmr.query('VISUAL.DAILY_CREATES', {MANDT : cmr.MANDT, _qall : 'Y'});
  var ctx = dojo.byId("canvas").getContext("2d");
  
  var dataSet = [];
  var labelSet = [];
  var colorSet = [];
  var totals = 0;
  creates.forEach(function(item, i){
    dataSet.push(Number(item.ret2));
    labelSet.push(item.ret1.trim());
    colorSet.push(getRandomColor());
    totals += Number(item.ret2);
  });
  var ctx = dojo.byId("canvas").getContext("2d");
  
  var chart = new Chart(ctx, {
    type: 'pie',
    data: {
      labels: labelSet,
      datasets: [{
        backgroundColor: colorSet,
        data: dataSet
      }]
    },
    options : {
      title : {
        display : true,
        text : 'Total Requests Created Today (GMT): '+totals
      },
      tooltips : {
        mode : 'index',
        intersect : false
      },
      legend : {
        display : false
      },
      responsive : true
    }
  });

  var completes = cmr.query('VISUAL.DAILY_COMPLETES', {MANDT : cmr.MANDT, _qall : 'Y'});
  var ctx2 = dojo.byId("canvas2").getContext("2d");
  
  var dataSet2 = [];
  var labelSet2 = [];
  var colorSet2 = [];
  var totals2 = 0;
  completes.forEach(function(item, i){
    dataSet2.push(Number(item.ret2));
    labelSet2.push(item.ret1.trim());
    colorSet2.push(getRandomColor());
    totals2 += Number(item.ret2);
  });
  var ctx2 = dojo.byId("canvas2").getContext("2d");
  
  var chart2 = new Chart(ctx2, {
    type: 'pie',
    data: {
      labels: labelSet2,
      datasets: [{
        backgroundColor: colorSet2,
        data: dataSet2
      }]
    },
    options : {
      title : {
        display : true,
        text : 'Total Requests Completed Today (GMT): '+totals2
      },
      tooltips : {
        mode : 'index',
        intersect : false
      },
      legend : {
        display : false
      },
      responsive : true
    }
  });
  
}

function getRandomColor() {
  var r = Math.floor(Math.random() * 255);
  var g = Math.floor(Math.random() * 255);
  var b = Math.floor(Math.random() * 255);
  return 'rgb(' + r + ',' + g + ',' + b + ')';
}
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
						<div class="ibm-col-4-2">
							<div id="welcome_note_id">
								<%=home%>
							</div>
													
						</div>
						<div class="ibm-col-4-2">
              <canvas id="canvas" style="height:20px"></canvas>
              <canvas id="canvas2" style="height:20px"></canvas>
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
