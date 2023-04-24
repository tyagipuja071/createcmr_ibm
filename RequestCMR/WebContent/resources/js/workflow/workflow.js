/*
 * Workflow Javascript. All workflow javascript should be here
 */

dojo.addOnLoad(function() {
  FormManager.ready();
  CmrGrid.showProgressInd();
  dojo.cookie('lastTab', 'x');
});

/**
 * Formatter for the expedite column in the grid
 * 
 * @param value
 * @param rowIndex
 * @returns {String}
 */
function expediteFormatter(value, rowIndex) {
  var rowData = this.grid.getItem(rowIndex);
  var expediteInd = rowData.expediteInd;
  var source = rowData.sourceSystId[0];
  if (source) {
    return '<span class="source-syst">' + source + '</span>';
//  } else if (expediteInd == 'Y') {
//    return '<img src="' + cmr.CONTEXT_ROOT + '/resources/images/rush.png" class="cmr-grid-check">';
  } else {
    return '';
  }
}

/**
 * Formatter for the CMRIssuing Country field
 * 
 * @param value
 * @param rowIndex
 */
function countryFormatter(value, rowIndex) {
  var rowData = this.grid.getItem(rowIndex);
  var desc = rowData.cmrIssuingCntryDesc;

  if (value) {
    if (desc && '' != desc) {
      return '<span class="cmr-grid-tooltip" title="' + desc + '">' + value + '</span>';
    } else {
      return value;
    }
  } else {
    return '';
  }
}

/**
 * Formats the request ID
 * 
 * @param value
 * @param rowIndex
 * @returns {String}
 */
function requestIdFormatter(value, rowIndex) {
  var rowData = this.grid.getItem(rowIndex);
  var reqType = rowData.reqType;
  var expedite = rowData.expediteInd[0];
  if (typeof (_wfgrid) != 'undefined' && typeof (_wfrec) != 'undefined' && dojo.cookie(_wfgrid + '_rec') != null) {
    if (dojo.cookie(_wfgrid + '_rec') == value) {
      _wfrec = rowIndex;
    }
  }
  var exp = expedite == 'Y' ? '<img title="Expedite Requested" src="' + cmr.CONTEXT_ROOT + '/resources/images/rush.png" class="cmr-grid-check" style="vertical-align:sub;padding-right:5px;cursor:help;width:16px;height:16px;">' : '';

  return exp+'<a class="reqIdLink" reqid="' + value + '" reqtype="' + reqType + '" title="Open Request Details for ' + value + '" href="' + cmr.CONTEXT_ROOT + '/request/' + value + '">' + value + '</a>';
}

/**
 * Formatter for the CMR No.
 * 
 * @param value
 * @param rowIndex
 * @returns {String}
 */
function cmrNoFormatter(value, rowIndex) {
  var rowData = this.grid.getItem(rowIndex);
  var reqId = rowData.reqId;
  var reqType = rowData.reqType;
  var iterId = parseInt(rowData.iterationId);

  if (reqType == 'C' || reqType == 'U' || reqType == 'X') {
    return rowData.cmrNo;
  } else if ((reqType == 'M' || reqType == 'N') && iterId > 0) {
    return '<a href="javascript: downloadMassFile(\'' + reqId + '\')" title="Download the mass change file for the current iteration.">' + 'Download File' + '</a>';
  } else if ((reqType == 'M' || reqType == 'N') && iterId <= 0) {
    return 'No File Uploaded';
  } else if (reqType == 'R' || reqType == 'D') {
    return '<a href="javascript: openCmrList(\'' + reqId + '\', \'' + reqType + '\')">' + 'Show CMR List' + '</a>';
  }
  return '';
}

/**
 * Opens the history for the given request
 * 
 * @param requestId
 */
function openCmrList(requestId, requestType) {
  WindowMgr.open('CMRLIST', requestId, 'showcmrList?reqId=' + requestId + '&&reqType=' + requestType, null, 550);

}

function downloadMassFile(reqId) {
  var token = new Date().getTime();
  FormManager.setValue('dlDocType', 'FILE');
  FormManager.setValue('dlTokenId', token);
  FormManager.setValue('dlReqId', reqId);
  cmr.showProgress('Downloading file. Please wait...');
  document.forms['fileDlForm'].submit();
  window.setTimeout('checkToken("' + token + '")', 1000);

}

/**
 * Formatter for the Status
 * 
 * @param value
 * @param rowIndex
 * @returns {String}
 */
function overallStatusFormatter(value, rowIndex) {
  var rowData = this.grid.getItem(rowIndex);
  var reqId = rowData.reqId;
  var expediteInd = rowData.expediteInd;
  var reqType = rowData.reqTypeText;
  var pendingAppr = rowData.pendingAppr[0];
  var custName = rowData.custName;
  if (custName && custName[0]) {
    custName = custName[0].replace(/'/gi, '~');
  }
  var imgloc = cmr.CONTEXT_ROOT + '/resources/images/';
  var img = '';
  if (pendingAppr == 'Y') {
    img = '<img src="' + imgloc + 'pending.png"  class="pending-icon" title = "Pending Approvals">';
  }
  if (pendingAppr == 'Y1') {
    img = '<img src="' + imgloc + 'pending-red.png"  class="pending-icon" title = "Pending Approvals (more than 1 day)">';
  }
  var html =  '<a title="Open Workflow History for ' + reqId + '" href="javascript: openWorkflowHistory(\'' + reqId + '\', \'' + reqType + '\',\'' + custName + '\',\'' + expediteInd + '\')">' + value
      + '</a>' + img;
  
  var childReqId = rowData.childRequestId;
  console.log('child: '+childReqId);
  var childReqStatus = rowData.childRequestStatus;
  var childHtml = '';
  if (childReqId && childReqId != '0'){
    childHtml = '<br><br><span style="font-size:9px;font-weight:bold">CHILD REQUEST</span>:<br><a class="reqIdLink" reqid="' + childReqId + '" title="Open Request Details for Child Request ' + childReqId + '" href="' + cmr.CONTEXT_ROOT + '/request/' + childReqId + '">' + childReqId +' ('+childReqStatus+')'+ '</a>';
  }

  return html + childHtml;
}

/**
 * Formatter for the Claim field
 * 
 * @param value
 * @param rowIndex
 */
function claimFormatter(value, rowIndex) {
  var rowData = this.grid.getItem(rowIndex);
  var reqId = rowData.reqId;
  var reqStatus = rowData.reqStatus[0];
  var status = rowData.overallStatus;
  var canClaim = rowData.canClaim;
  var canClaimAll = rowData.canClaimAll;

  if (status == 'Completed') {
    canClaim = 'N';
    canClaimAll = 'N';
  }

  if ('[R]' == value || '[P]' == value) {
    var action = '';
    if (canClaim == 'Y' || canClaimAll == 'Y') {
      action += '<input type="button" title="Claim this request" value="Claim" class="cmr-grid-btn" onclick="doClaimRequest(\'' + reqId + '\', \'' + status + '\')">';
    }
    if ('AWA' == reqStatus) {
      action += '<br><input type="button" title="Requeue for automated checks" value="Redo Checks" class="cmr-grid-btn-h" onclick="doReprocessRequest(\'' + reqId + '\', \'' + status + '\')">';
    }

    return action;
  }
  return value;
}

/**
 * Formatter for the Pending Approvals column
 * 
 * @param value
 * @param rowIndex
 * @returns {String}
 */
function pendingApprFormatter(value, rowIndex) {
  if (value == 'Y') {
    return 'Yes';
  }
  if (value == 'Y1') {
    return '<span style="font-weight:bold;cursor:help;" title="Pending for more than 1 day">Yes</span>';
  }
  return '';
}

/**
 * Shows the prompt to claim the request
 * 
 * @param reqId
 */
function doClaimRequest(reqId, status) {
  cmr.tempReqId = reqId;
  cmr.showConfirm('actualClaimRequest()', 'Claim the <strong>' + status + '</strong> request with ID <strong>' + reqId + '</strong>?');
}

/**
 * Shows the prompt to claim the request
 * 
 * @param reqId
 */
function doReprocessRequest(reqId, status) {
  cmr.tempReqId = reqId;
  cmr.showConfirm('actualReprocessRequest()', 'Requeue the <strong>' + status + '</strong> request with ID <strong>' + reqId + '</strong> for automated checks?');
}

/**
 * Does the actual claim request
 */
function actualClaimRequest(reqId) {
  dojo.byId('mainReqId').value = cmr.tempReqId;
  var act = document.forms['frmCMR'].getAttribute('action');
  document.forms['frmCMR'].setAttribute('action', act + '/claim');

  var filter = null;
  if (typeof (_wfgrid) != 'undefined' && CmrGrid.GRIDS && CmrGrid.GRIDS[_wfgrid]) {
    filter = JSON.stringify(CmrGrid.GRIDS[_wfgrid].getFilter());
    dojo.cookie(_wfgrid + '_filter', filter);
    dojo.cookie(_wfgrid + '_rec', cmr.tempReqId);
  }

  FormManager.doAction('frmCMR', 'CLAIM', true);
}

/**
 * Does the actual claim request
 */
function actualReprocessRequest(reqId) {
  dojo.byId('mainReqId').value = cmr.tempReqId;
  var act = document.forms['frmCMR'].getAttribute('action');
  document.forms['frmCMR'].setAttribute('action', act + '/claim');

  var filter = null;
  if (typeof (_wfgrid) != 'undefined' && CmrGrid.GRIDS && CmrGrid.GRIDS[_wfgrid]) {
    filter = JSON.stringify(CmrGrid.GRIDS[_wfgrid].getFilter());
    dojo.cookie(_wfgrid + '_filter', filter);
    dojo.cookie(_wfgrid + '_rec', cmr.tempReqId);
  }

  FormManager.doAction('frmCMR', 'REPROCESS', true);
}
/**
 * Does the search from the criteria page
 */
function doSearchRequests() {
  if (FormManager.validate('frmCMR')) {
    cmr.showProgress('Processing. Please wait...');
    document.forms['frmCMR'].submit();
  }
}

/**
 * Opens the history for the given request
 * 
 * @param requestId
 */
function openWorkflowHistory(requestId, requestType, custName, expediteInd) {
  WindowMgr.open('WFHIST', requestId, 'wfhist?reqId=' + requestId, null, 550);
}

/**
 * Onload function for the workflow history modal
 */
function workflowHistoryModal_onLoad() {
  var url = cmr.CONTEXT_ROOT + '/workflow/history/list.json';
  var params = 'reqId=' + FormManager.getActualValue('workflowRequestId');
  // Request Id
  var reqId = FormManager.getActualValue('workflowRequestId');
  dojo.byId('wfhist_reqId').innerHTML = '<a href="javascript: openRequestDetails(\'' + reqId + '\')">' + reqId + '</a>';

  // Request Type
  dojo.byId('wfhist_mainReqType').innerHTML = FormManager.getActualValue('mainReqType');

  // Customer Name
  dojo.byId('wfhist_mainCustName').innerHTML = FormManager.getActualValue('mainCustName');

  if ('Y' == dojo.byId('mainExpedite').value) {
    dojo.byId('wfhist_mainExpedite').innerHTML = '<img src="' + cmr.CONTEXT_ROOT + '/resources/images/check-icon.png" class="cmr-grid-check">';
  } else {
    dojo.byId('wfhist_mainExpedite').innerHTML = '-';
  }
  CmrGrid.refresh('workflowHistoryGrid', url, params);
}

/**
 * Validates the workflow inputs
 * 
 * @param formName
 * @returns ValidationResult
 */
function validateWorkflow(input) {
  if (document.getElementById("wfProcCentre").value == "" && document.getElementById("wfReqName").value == "" && document.getElementById("cmrIssuingCountry").value == ""
      && document.getElementById("wfOrgName").value == "" && document.getElementById("cmrOwnerCriteria").value == "" && document.getElementById("wfClaimByName").value == ""
      && document.getElementById("cmrNoCriteria").value == "" && document.getElementById("requestType").value == "" && document.getElementById("customerName").value == ""
      && document.getElementById("requestId").value == "" && document.getElementById("requestStatus").value == "" && document.getElementById("createDateFrom").value == ""
      && document.getElementById("lastActDateFrom").value == "" && document.getElementById("createDateTo").value == "" && document.getElementById("lastActDateTo").value == ""
      && document.getElementById("procStatus").value == "") {
    return new ValidationResult(input, false, MessageMgr.MESSAGES.SEARCH_CRIT_AT_LEAST_ONE);
  } else {
    return new ValidationResult(input, true);
  }
}

/**
 * Validates the cmr number inputs
 * 
 * @param formName
 * @returns ValidationResult
 */
function validateCMRNum(input) {
  if (FormManager.getActualValue('cmrNoCriteria') != '') {
    if (FormManager.getActualValue('cmrIssuingCountry') != '') {
      return new ValidationResult(input, true);
    } else {
      return new ValidationResult(input, false, MessageMgr.MESSAGES.SEARCH_CMR_NUM_CNTRY);
    }
  } else {
    return new ValidationResult(input, true);
  }
}

/**
 * Opens the Request Details screen
 * 
 * @param reqId
 */
function openRequestDetails(reqId, reqType) {
  // var from = '<input type="hidden" name="fromUrl" value="' +
  // cmr.extractUrl(true) + '">';
  // dojo.place(from, document.forms['frmCMR'], 'last');
  dojo.byId('fromURL').value = cmr.extractUrl(true);
  if (reqType == 'C' || reqType == 'U' || reqType == 'X' || reqType == '') {
    document.forms['frmCMR'].setAttribute('action', cmr.CONTEXT_ROOT + '/request/' + reqId);
  } else {
    document.forms['frmCMR'].setAttribute('action', cmr.CONTEXT_ROOT + '/massrequest/' + reqId);
  }

  var filter = null;
  if (typeof (_wfgrid) != 'undefined' && CmrGrid.GRIDS && CmrGrid.GRIDS[_wfgrid]) {
    filter = JSON.stringify(CmrGrid.GRIDS[_wfgrid].getFilter());
    dojo.cookie(_wfgrid + '_filter', filter);
    dojo.cookie(_wfgrid + '_rec', reqId);
  }

  cmr.showProgress('Opening the request, please wait...');
  document.forms['frmCMR'].submit();
  // window.location.href = cmr.CONTEXT_ROOT+'/request/'+reqId +
  // '?fromUrl='+from;
}

function sentToIdFormatter(value, rowIndex) {
  var rowData = this.grid.getItem(rowIndex);
  var nm = rowData.sendToNm;
  if (nm && dojo.strig.trim(nm) != '') {
    return nm;
  } else {
    return value;
  }
}

function histCmtFormatter(value, rowIndex) {
  rowData = this.grid.getItem(rowIndex);
  var cmtdata = rowData.cmt;
  if (cmtdata != null && cmtdata[0] != null) {
    cmtdata = cmtdata[0].replace(/\n/g, '<br>');
  }
  return '<span style="word-wrap: break-word">' + cmtdata + '</span>';
}

/**
 * Opens a new Request Entry page for creating new requests For
 */
function createNewEntry() {
  if (cmr.validateNewEntry()) {
    var type = FormManager.getActualValue('newReqType');
    var cntry = FormManager.getActualValue('newReqCntry');
    if (type == 'C' || type == 'U' || type == 'X') {
      var from = '<input type="hidden" name="fromUrl" value="' + cmr.extractUrl(true) + '">';
      dojo.place(from, document.forms['frmCMR'], 'last');
      dojo.byId('newReqType_h').value = type;
      dojo.byId('newReqCntry_h').value = cntry;
      document.forms['frmCMR'].setAttribute('action', cmr.CONTEXT_ROOT + '/request?create=Y');
      document.forms['frmCMR'].submit();
    } else {
      var from = '<input type="hidden" name="fromUrl" value="' + cmr.extractUrl(true) + '">';
      dojo.byId('newReqType_h').value = type;
      dojo.byId('newReqCntry_h').value = cntry;
      dojo.place(from, document.forms['frmCMR'], 'last');
      document.forms['frmCMR'].setAttribute('action', cmr.CONTEXT_ROOT + '/massrequest?create=Y');
      document.forms['frmCMR'].submit();
    }
  }
}

/**
 * Opens a new Mass Request Entry page for creating new mass requests
 */
function createNewMassEntry() {
}

// create cmr
/**
 * Formatter for the expedite column in the grid
 * 
 * @param value
 * @param rowIndex
 * @returns {String}
 */
function reqTypeFormatter(value, rowIndex) {
  if (value == null) {
    value = '';
  }
  var rowData = this.grid.getItem(rowIndex);
  var typeDesc = rowData.typeDescription;
  var p = rowData.prospect;
  var p1 = '';
  if (p == 'Y') {
    p1 = '<img title="Prospect to Legal CMR request" src="' + cmr.CONTEXT_ROOT + '/resources/images/prospect.png" style="width:15px; height:15px;vertical-align:sub">';
  }
  if (typeDesc && typeDesc != '') {
    return '<span class="cmr-grid-tooltip" title="' + typeDesc + '">' + value + p1 + '</span>';
  } else {
    return value + p1;
  }
}

function openRequestById() {
  cmr.showModal('openRequestByIdModal');
}

function doOpenRequestById() {
  var reqId = FormManager.getActualValue('requestById');
  if (reqId == '') {
    cmr.showAlert('Please enter the Request ID.');
    return;
  }
  var ret = cmr.query('CHECKREQUESTBYID', {
    REQID : reqId
  });
  if (ret && ret.ret1 == 'Y') {
    openRequestDetails(reqId, ret.ret2);
  } else {
    cmr.showAlert('Request ' + reqId + ' does not exist.');
    return;
  }
}

function wfNameFormatter(value, rowIndex) {
  var rowData = this.grid.getItem(rowIndex);
  var reqId = rowData.reqId;
  var reqType = rowData.reqType;
  var shownVal = value ? value : '?';
  if (reqType && reqType[0] && (reqType[0] == 'M' || reqType[0] == 'N' || reqType[0] == 'R' || reqType[0] == 'D' || reqType[0] == 'E')) {
    shownVal = '- n/a -';
  }
  return '<a title="Open Summary for ' + reqId + '" href="javascript: showSummaryScreen(\'' + reqId + '\', \'' + reqType + '\')">' + shownVal + '</a>';
}

function showSummaryScreen(requestId, type) {
  WindowMgr.open('SUMMARY', requestId, 'summary?reqId=' + requestId + '&reqType=' + type, null, type == 'E' ? '420' : null);
}

function wfGridContext(event) {
  var trg = event.target;
  if (trg && trg.localName == 'a' && trg.className == 'reqIdLink') {
    var reqId = trg.getAttribute('reqid');
    var reqType = trg.getAttribute('reqtype');

    if (event.ctrlKey) {
      return;
    } else if (event.button == 2) {
      alert('Please use Ctrl + Left Click to open the link on a new tab.');
      return;
      var context = dojo.byId('wf-context');
      if (context) {
        context.setAttribute('reqid', reqId);
        context.setAttribute('reqtype', reqType);
        console.log(' show context: ' + event.pageX + ' - ' + event.pageY);
        context.style.left = (event.screenX + 5) + 'px';
        context.style.top = (event.screenY + 10) + 'px';
        cmr.showNode('wf-context');
      }
    } else {
      console.log('simple click');
      event.preventDefault();
      // event.stopPropagation();
      openRequestDetails(reqId, reqType);
    }
    return;
  }
  if (this.pagination == null) {
    return;
  }
  if (!trg) {
    return;
  }
  console.log(trg);
  if (!(trg.localName == 'span' && trg.className == 'dojoxGridActivedSwitch')) {
    return;
  }
  if (this.pagination.plugin.pageSize == 100) {
    this.rowsPerPage = 100;
    this._refresh();
  }
  if (this.pagination.plugin.pageSize == 75) {
    this.rowsPerPage = 75;
    this._refresh();
  }
  if (this.pagination.plugin.pageSize == 50) {
    this.rowsPerPage = 50;
    this._refresh();
  }
  if (this.pagination.plugin.pageSize == 25) {
    this.rowsPerPage = 25;
    this._refresh();
  }
}

function getPosition(el) {
  var xPosition = 0;
  var yPosition = 0;

  while (el) {
    if (el.tagName == "BODY") {
      // deal with browser quirks with body/window/document and page scroll
      var xScrollPos = el.scrollLeft || document.documentElement.scrollLeft;
      var yScrollPos = el.scrollTop || document.documentElement.scrollTop;

      xPosition += (el.offsetLeft - xScrollPos + el.clientLeft);
      yPosition += (el.offsetTop - yScrollPos + el.clientTop);
    } else {
      xPosition += (el.offsetLeft - el.scrollLeft + el.clientLeft);
      yPosition += (el.offsetTop - el.scrollTop + el.clientTop);
    }

    el = el.offsetParent;
  }
  return {
    x : xPosition,
    y : yPosition
  };
}