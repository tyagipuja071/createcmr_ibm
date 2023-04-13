/*
 * File: myapproval.js Description: Contains the functions for the Approvals tab
 * 
 */

function requestTypeFormatter(value, rowIndex) {
  var rowData = this.grid.getItem(rowIndex);
  var status = rowData.reqStatus;
  if (!value) {
    return '';
  }
  switch (value) {
  case 'C':
    return 'Create';
  case 'U':
    return 'Update';
  case 'M':
    return 'Mass Update';
  case 'N':
    return 'Mass Create';
  case 'D':
    return 'Delete';
  case 'R':
    return 'Reactivate';
  case 'E':
    return 'Update by Enterprise #';
  }
  return '';
}
function countryFormatter(value, rowIndex) {
  var rowData = this.grid.getItem(rowIndex);
  var desc = rowData.cntryDesc;

  if (value) {
    if (desc && '' != desc) {
      return '<span class="cmr-grid-tooltip" style="cursor:help" title="' + desc + '">' + value + '</span>';
    } else {
      return value;
    }
  } else {
    return '';
  }
}
function dplCheckFormatter(value, rowIndex) {
  if (value) {
    value = value.trim();
    if (value == 'AP') {
      return '<span style="color:green">All Passed</span>';
    }
    if (value == 'NR') {
      return 'Not Required';
    }
    if (value == 'AF') {
      return '<span style="color:red;font-weight:bold">All Failed</span>';
    }
    if (value == 'SF') {
      return '<span style="color:orange;font-weight:bold">Some Failed</span>';
    }
    return value;
  } else {
    return '';
  }
}

function approvalStatusFormatter(value, rowIndex) {
  if (!value) {
    return '';
  }
  switch (value) {
  case 'PAPR':
    return '<span style="font-weight:bold">Pending Approval</span>';
  case 'APR':
    return '<span style="color:green">Approved</span>';
  case 'CCAN':
    return '<em>Cond. Cancelled</em>';
  case 'CAPR':
    return '<span style="color:green;">Cond. Approved</span>';
  case 'REJ':
    return '<span style="color:red;">Rejected</span>';
  case 'PMAIL':
    return 'Pending Mail';
  case 'PREM':
    return 'Pending Reminder';
  case 'PCAN':
    return 'Pending Cancellation';
  case 'OVERP':
    return 'Override Pending';
  case 'OVERA':
    return 'Override Approved';
  case 'CAN':
    return '<em>Cancelled</em>';
  case 'DRA':
    return 'Draft';
  case 'OBSLT':
    return '<em>Obsolete</em>';
  }
  return '(cannot be determined)';
}

function requestIdFormatter(value, rowIndex) {
  var rowData = this.grid.getItem(rowIndex);
  var reqType = rowData.reqType;
  if (typeof (_wfgrid) != 'undefined' && typeof (_wfrec) != 'undefined' && dojo.cookie(_wfgrid + '_rec') != null) {
    if (dojo.cookie(_wfgrid + '_rec') == value) {
      _wfrec = rowIndex;
    }
  }
  return '<a class="reqIdLink" reqid="' + value + '" reqtype="' + reqType + '" title="Open Request Details for ' + value + '" href="' + cmr.CONTEXT_ROOT + '/request/' + value + '">' + value + '</a>'
      + '<br><img src="' + cmr.CONTEXT_ROOT + '/resources/images/pdf-icon.png"  class="approval-icon" style="vertical-align:bottom" onclick="exportToPdf(' + value
      + ')" title="Export Request Details to PDF">';
}

function nameFormatter(value, rowIndex) {
  var rowData = this.grid.getItem(rowIndex);
  var reqId = rowData.reqId[0];
  var reqType = rowData.reqType[0];
  var formatted = value ? value : '';
  formatted += '<br>';
  formatted += '<a class="appr-link" title="Open Summary Request ' + value + '" href="javascript: showSummaryScreen(' + reqId + ', \'' + reqType + '\')">Summary</a>';
  formatted += '<a class="appr-link" title="Open Workflow History Request ' + value + '" href="javascript: openWorkflowHistory(' + reqId + ')">Workflow</a>';
  formatted += '<a class="appr-link" title="Open Change Log Request ' + value + '" href="javascript: showChangeLog(' + reqId + ')">Change Log</a>';
  return formatted;
}

function openWorkflowHistory(requestId) {
  WindowMgr.open('WFHIST', requestId, 'wfhist?reqId=' + requestId, null, 550);
}

function showSummaryScreen(requestId, type) {
  WindowMgr.open('SUMMARY', requestId, 'summary?reqId=' + requestId + '&reqType=' + type, null, type == 'E' ? '420' : null);
}

function showChangeLog(requestId) {
  WindowMgr.open('CHANGELOG', requestId, 'reqchangelog?reqId=' + requestId, 1050, 570);
}

function exportToPdf(reqId) {
  cmr.showProgress('Exporting request details. Please wait...');
  var token = new Date().getTime();
  FormManager.setValue('pdfReqId', reqId);
  FormManager.setValue('pdfTokenId', token);
  document.forms['frmPDF'].submit();
  window.setTimeout('checkToken("' + token + '")', 1000);
}

function actionsFormatter(value, rowIndex) {
  var rowData = this.grid.getItem(rowIndex);
  var reqType = rowData.approvalStatus[0];
  var reqId = rowData.reqId[0];
  var approvalId = rowData.approvalId[0];
  var actions = '';
  if ('PAPR' == reqType) {
    actions += '<img src="' + cmr.CONTEXT_ROOT + '/resources/images/approve.png"  class="approval-icon" onclick="doApproval(\'A\', ' + reqId + ', ' + approvalId + ')" title="Approve">';
    actions += '<img src="' + cmr.CONTEXT_ROOT + '/resources/images/cond-approve.png"  class="approval-icon" onclick="doApproval(\'C\', ' + reqId + ', ' + approvalId
        + ')" title="Conditionally Approve">';
    actions += '<img src="' + cmr.CONTEXT_ROOT + '/resources/images/reject.png"  class="approval-icon" onclick="doApproval(\'R\', ' + reqId + ', ' + approvalId + ')" title="Reject">';
  }
  actions += '<img src="' + cmr.CONTEXT_ROOT + '/resources/images/info-bubble-icon.png"  class="approval-icon" onclick="viewComments(' + reqId + ', ' + approvalId + ')" title="View Comments">';
  return actions;
}

function doApproval(type, reqId, approvalId) {
  cmr.currentApproval = type;
  cmr.massApproval = false;
  cmr.approvalId = approvalId;
  cmr.reqId = reqId;
  FormManager.setValue('reqId', reqId);
  cmr.showModal('processApprovalModal');
}

function doMassApproval(type) {
  if (!CmrGrid.hasSelected()) {
    alert('No record(s) selected.');
    return;
  }
  cmr.currentApproval = type;
  cmr.massApproval = true;
  cmr.reqId = '0';
  cmr.showModal('processMassApprovalModal');
}

function executeApproval() {
  var type = cmr.currentApproval;
  var reason = FormManager.getActualValue('rejReason');
  if (type == 'R' && reason == '') {
    alert('Please input the rejection reason.');
    return;
  }
  var cmts = FormManager.getActualValue('comments');
  if (cmts == '') {
    alert('Please input your comments.');
    return;
  }
  var approvalId = cmr.approvalId;
  var reqId = cmr.reqId;

  FormManager.setValue('approval_comments', cmts);
  FormManager.setValue('approval_rejReason', reason);
  FormManager.setValue('mass', 'N');
  FormManager.setValue('approvalId', approvalId);
  FormManager.setValue('reqId', reqId);

  switch (type) {
  case 'A':
    if (confirm('Approve the request for ID ' + reqId + '?')) {
      FormManager.doHiddenAction('frmCMR', 'APPROVE', cmr.CONTEXT_ROOT + '/myappr/process.json', true, refreshAfterApprove, false);
    }
    return;
  case 'C':
    if (confirm('Conditionally Approve the request for ID ' + reqId + '?')) {
      FormManager.doHiddenAction('frmCMR', 'COND_APPROVE', cmr.CONTEXT_ROOT + '/myappr/process.json', true, refreshAfterApprove, false);
    }
    return;
  case 'R':
    if (confirm('Reject the request for ID ' + reqId + '?')) {
      FormManager.doHiddenAction('frmCMR', 'REJECT', cmr.CONTEXT_ROOT + '/myappr/process.json', true, refreshAfterApprove, false);
    }
    return;
  }
}

function executeMassApproval() {
  var type = cmr.currentApproval;
  var reason = FormManager.getActualValue('rejReasonMass');
  if (type == 'R' && reason == '') {
    alert('Please input the rejection reason.');
    return;
  }
  var cmts = FormManager.getActualValue('mass_comments');
  if (cmts == '') {
    alert('Please input your comments.');
    return;
  }
  var type = cmr.currentApproval;

  FormManager.setValue('approval_comments', cmts);
  FormManager.setValue('approval_rejReason', reason);
  FormManager.setValue('mass', 'Y');
  switch (type) {
  case 'A':
    FormManager.gridHiddenAction('frmCMR', 'APPROVE', cmr.CONTEXT_ROOT + '/myappr/process.json', true, refreshAfterApprove, false, 'Approve selected records?', 120000);
    return;
  case 'C':
    FormManager.gridHiddenAction('frmCMR', 'COND_APPROVE', cmr.CONTEXT_ROOT + '/myappr/process.json', true, refreshAfterApprove, false, 'Conditionally Approve selected records?', 120000);
    return;
  case 'R':
    FormManager.gridHiddenAction('frmCMR', 'REJECT', cmr.CONTEXT_ROOT + '/myappr/process.json', true, refreshAfterApprove, false, 'Reject selected records?', 120000);
    return;
  }
}

function refreshAfterApprove(result) {
  if (cmr.massApproval) {
    cmr.hideModal('processMassApprovalModal');
  } else {
    cmr.hideModal('processApprovalModal');
  }
  CmrGrid.refresh('myApprovalsGrid');
}

function viewComments(reqId, approvalId) {
  cmr.showModal('viewApprCommentsModal');
  CmrGrid.refresh('APPROVAL_CMT_GRID', cmr.CONTEXT_ROOT + '/approval/comments.json', 'reqId=' + reqId + '&approvalId=' + approvalId);
}

function viewApprCommentsModal_onLoad() {
  cmr.showProgress('Comments loading, pls wait..');
}

function APPROVAL_CMT_GRID_GRID_onLoad() {
  cmr.hideProgress();
}

function processApprovalModal_onLoad() {
  var title = cmr.currentApproval == 'A' ? 'Approval' : (cmr.currentApproval == 'C' ? 'Conditional Approval' : 'Rejection');
  cmr.setModalTitle('processApprovalModal', 'Process ' + title);
  dojo.byId('comments').value = '';
  var reqId = cmr.reqId;
  if (cmr.currentApproval == 'R') {
    cmr.showNode('rejectblock');
    cmr.showNode('rejectMainLine');
    cmr.hideNode('approveMainLine');
  } else {
    cmr.hideNode('rejectblock');
    cmr.hideNode('rejectMainLine');
    cmr.showNode('approveMainLine');
  }

  if (reqId != '0') {
    CmrGrid.refresh('ATTACHMENT_GRID', cmr.CONTEXT_ROOT + '/search/attachment.json', 'reqId=' + reqId);
  }
}

function processMassApprovalModal_onLoad() {
  var title = cmr.currentApproval == 'A' ? 'Approval' : (cmr.currentApproval == 'C' ? 'Conditional Approval' : 'Rejection');
  cmr.setModalTitle('processMassApprovalModal', 'Process Mass ' + title);
  dojo.byId('mass_comments').value = '';
  if (cmr.currentApproval == 'R') {
    cmr.showNode('rejectblock_mass');
    cmr.showNode('rejectMainLine_mass');
    cmr.hideNode('approveMainLine_mass');
  } else {
    cmr.hideNode('rejectblock_mass');
    cmr.hideNode('rejectMainLine_mass');
    cmr.showNode('approveMainLine_mass');
  }
}

function addAttachmentModal_onLoad() {
  dojo.byId('attach_reqId').value = cmr.reqId;
  if (_attachmode == 'S') {
    dojo.byId('addAttachmentModalTitle').innerHTML = 'Attach a Screenshot';
  } else {
    dojo.byId('addAttachmentModalTitle').innerHTML = 'Attach a File';
  }
}

function attchActFormatter(value, rowIndex) {
  var rowData = this.grid.getItem(0);
  if (rowData == null) {
    return ''; // not more than 1 record
  }
  rowData = this.grid.getItem(rowIndex);
  var docLink = rowData.docLink[0];
  var link = docLink;
  if (link.indexOf('.zip') > 0) {
    link = link.substring(0, link.lastIndexOf('.'));
  }
  var openBtn = '<input type="button" value="Open" class="cmr-grid-btn" onclick="doOpenFile(\'' + link + '\')">';
  var removeBtn = '';
  return openBtn + removeBtn;
}

function myApprovalsGrid_showCheck(value, rowIndex, grid) {
  var rowData = grid.getItem(rowIndex);
  var status = rowData.approvalStatus[0];
  return (status == 'PAPR');
}
