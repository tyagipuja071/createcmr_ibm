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
  if (!BrowserCheck.isFirefox() && !BrowserCheck.isChrome()){
    var msg = 'You are using an unsupported browser. The functions on the pages may not work properly.<br>';
    msg += 'Please use either Firefox or Chrome to access the tool to ensure all functions work correctly.';
    //cmr.showAlert(msg);
  }
  if (BrowserCheck.isFirefox() && BrowserCheck.getFirefoxVersion() < 52){
    var msg = 'You are using an older version of Firefox.  It is recommended to use at least Firefox v52.<br>';
    msg += 'Some functions of the application may not work correctly until you update to the required version.';
    //cmr.showAlert(msg);
  }
  window.setTimeout('generateCharts()', 150);
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
                     The Client Master Data Execution (CMDE) teams support the processing of requests created 
                     within CreateCMR.  There are 3 major centers for CMDE: Kuala Lumpur, Bratislava, and Dalian.
                     Specific teams handle requests from different countries.
                     </li>
                     <li>
                     To know more about the CMDE teams and processes, please visit the <a href="https://w3.ibm.com/w3publisher/cmde-cmr" target="_blank">CMDE Site</a>.
                     </li>
                   </ul>
                 </div>
              </div>
													
						</div>
						<div class="ibm-col-4-2" style="width:400px">
              <canvas id="canvas" style="height:20px"></canvas>
              <canvas id="canvas2" style="height:20px"></canvas>
						</div>
					</div>
					
				</div>
			</div>
			
			<!-- stop main content -->
	</div>
</div>
