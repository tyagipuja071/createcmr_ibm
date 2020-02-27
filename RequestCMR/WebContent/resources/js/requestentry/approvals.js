/*
 * File: approval.js
 * Description: 
 * Contains the functions for the approvals tab
 * 
 */

var _approvals = {
  warningShown : false,
  actionMode : 'A',
  currId : '',
  approver : '',
  status : '',
  hasDraft : false,
  overallStatus : '',
  latestStatus : '',
  latestTs : '',
  requiredIndc : ''
};

/**
 * Formatter for NotesID
 * 
 */
function notesIdFormatter(value, rowIndex) {
  rowData = this.grid.getItem(rowIndex);
  var intranetId = rowData.approverId[0];
  return '<span class="cmr-grid-tooltip" title="' + intranetId + '">' + value + '</span>';
}

/**
 * Formatter for the Action column of the Approval list grid
 * 
 */
function actionsFormatter(value, rowIndex) {
  rowData = this.grid.getItem(rowIndex);
  var status = rowData.status;
  var requiredIndc = rowData.requiredIndc.toString();
  var actions = '';
  var approvalId = rowData.approvalId[0];
  var approverNm = rowData.approverNm[0];
  if (status == 'DRA') {
    _approvals.hasDraft = true;
  }
  if ('PAPR' == status) {
    actions = actions + '<input type="button" value="Send Reminder" class="cmr-grid-btn" onclick="sendApprovalReminder(\'' + approvalId + '\', \'' + approverNm + '\', \'' + status + '\')">';
  }
  if ('DRA' == status || 'PMAIL' == status || 'PAPR' == status || 'OVERP' == status || 'PREM' == status) {
    if (!('Y' == requiredIndc.toUpperCase())) {
      actions = actions + '<input type="button" value="Cancel" class="cmr-grid-btn" onclick="cancelApproval(\'' + approvalId + '\', \'' + status + '\')">';
    } else if ('PROCESSOR' == role.toUpperCase()) {
      actions = actions + '<input type="button" value="Cancel" class="cmr-grid-btn" onclick="cancelApproval(\'' + approvalId + '\', \'' + status + '\')">';
    }
  }
  if ('REJ' == status) {
    actions = actions + '<input type="button" value="Override" class="cmr-grid-btn" onclick="overrideApproval(\'' + approvalId + '\', \'' + status + '\', \'' + requiredIndc + '\')">'
        + '<input type="button" value="Re-submit" class="cmr-grid-btn" onclick="resubmitApproval(\'' + approvalId + '\', \'' + status + '\')">';
  }
  if ('DRA' == status) {
    // actions = actions + '<input type="button" value="Send Request"
    // class="cmr-grid-btn" onclick="sendApprovalRequest(\'' + approvalId + '\',
    // \'' + approverNm + '\', \'' + status + '\')">';
  }

  return actions;
}
function actionsFormatterBlank(value, rowIndex) {
  var rowData = this.grid.getItem(rowIndex);
  var status = rowData.status[0];
  var approvalId = rowData.approvalId[0];
  var approverNm = rowData.approverNm[0];
  var actions = '';
  var viewer = true;
  if ((typeof (_pagemodel) != 'undefined') && _pagemodel.userRole != 'Viewer') {
    viewer = false;
  }
  if (!viewer && status == 'PAPR') {
    actions = actions + '<input type="button" value="Send Reminder" class="cmr-grid-btn" onclick="sendApprovalReminder(\'' + approvalId + '\', \'' + approverNm + '\', \'' + status + '\')">';
  }
  return actions;
}

/**
 * Formatter for required
 * 
 */
function requiredFormatter(value, rowIndex) {
  rowData = this.grid.getItem(rowIndex);
  var requiredIndc = rowData.requiredIndc.toString();
  var req = '';

  if ('Y' == requiredIndc.toUpperCase()) {
    req = 'Yes';
  }

  return req;
}

/**
 * Formatter for comments
 * 
 */
function commentsFormatter(value, rowIndex) {
  rowData = this.grid.getItem(rowIndex);
  var approvalId = rowData.approvalId[0];

  return '<input type="button" value="View" class="cmr-grid-btn" onclick="viewComments(\'' + approvalId + '\')">';
}

/**
 * Action column: Send Reminder
 */
function sendApprovalReminder(approvalId, approver, status) {
  _approvals.status = status;
  _approvals.approver = approver;
  _approvals.actionMode = 'SEND_REMINDER';
  _approvals.currId = approvalId;
  var approver = _approvals.approver;
  cmr.showConfirm('processApprovalAction()', 'Send reminder email to <b>' + approver + '</b>?', null);
}

/**
 * Action column: Cancel
 */
function cancelApproval(approvalId, status) {
  _approvals.status = status;
  _approvals.actionMode = 'CANCEL';
  _approvals.currId = approvalId;
  cmr.showModal('addApprovalModal');
}

/**
 * Action column: Override
 */
function overrideApproval(approvalId, status, requiredIndc) {
  _approvals.status = status;
  _approvals.actionMode = 'OVERRIDE';
  _approvals.currId = approvalId;
  _approvals.requiredIndc = requiredIndc;

  cmr.showModal('addApprovalModal');
}

/**
 * Action column: Re-submit
 */
function resubmitApproval(approvalId, status) {
  _approvals.status = status;
  _approvals.actionMode = 'RESUBMIT';
  _approvals.currId = approvalId;
  cmr.showModal('addApprovalModal');
}

/**
 * Action column: Send request
 */
function sendApprovalRequest() {
  if (!_approvals.hasDraft) {
    cmr.showAlert('There are no Draft approvals to send. ', 'Error');
    return;
  }
  _approvals.currId = 0;
  _approvals.actionMode = 'SEND_REQUEST';
  if (!FormManager.validate('frmCMR', null, true)) {
    cmr.showAlert('There are errors on the page. Approvals cannot be sent until all errors are fixed.' + '<br>Select Check Request to view the errors.', 'Error');
    return;
  }
  if (!_approvals.warningShown) {
    var msg = 'Once approval requests have been sent, the request cannot be edited. ';
    msg += 'Please ensure all other approvals required are ready for sending.';
    cmr.showAlert(msg, 'Warning', 'doSendApprovalRequest()');
    _approvals.warningShown = true;
  } else {
    doSendApprovalRequest();
  }
}

function doSendApprovalRequest() {
  cmr.showConfirm('processApprovalAction()', 'All Draft approvals will be sent to the corresponding approvers. Proceed?', null);
}

/**
 * Comments column: view
 */
function viewComments(approvalId) {
  var reqId = FormManager.getActualValue('reqId');
  cmr.showModal('viewApprCommentsModal');
  CmrGrid.refresh('APPROVAL_CMT_GRID', cmr.CONTEXT_ROOT + '/approval/comments.json', 'reqId=' + reqId + '&approvalId=' + approvalId);
}
/**
 * Shows the add approval modal
 */
function doAddApproval() {
  if (FormManager.validate('frmCMR', null, true)) {

    _approvals.actionMode = 'ADD_APPROVER';
    cmr.showModal('addApprovalModal');

  } else {
    cmr.showAlert('There are errors on the page. Approvals cannot be added until all errors are fixed.' + '<br>Select Check Request to view the errors.');
  }
}

/**
 * Adds the approval to the request
 */
function addApproval() {
  if (FormManager.validate('frmCMR_addApprovalModal', true)) {
    var reqId = FormManager.getActualValue('reqId');
    var typId = FormManager.getActualValue('typId');
    var intranetId = FormManager.getActualValue('intranetId');

    var check = cmr.query('CHECKAPPROVAL', {
      REQ_ID : reqId,
      TYP_ID : typId,
      INTRANET_ID : intranetId
    });
    if (check && check.ret1 == '1') {
      cmr.showAlert('This Approval already exists for the current request.');
      return;
    }

    cmr.modalmode = true;
    FormManager.doHiddenAction('frmCMR_addApprovalModal', 'ADD_APPROVAL', cmr.CONTEXT_ROOT + '/approval/process.json', true, refreshApprovalGrid, true);
  }
}

/**
 * Refresh the approval grid after a save
 */
function refreshApprovalGrid(result) {
  if (result.success) {
    try {
      cmr.hideModal('addApprovalModal');
    } catch (e) {
      // safety for non-modal functions that used the method
    }
    if (_approvals.actionMode == 'SEND_REQUEST') {
      FormManager.setValue('dplMessage', result.message);
      FormManager.doAction('frmCMR', 'DPL', true);
    } else {
      checkOverallApprovalStatus(false);
      refreshAprovalsList();
    }
  }
}

function refreshAprovalsList() {
  _approvals.hasDraft = false;
  if (_approvals.overallStatus != _approvals.latestStatus) {
    var msg = 'The overall approval status of this request has changed. The status is now ' + _approvals.latestStatus + '.<br>';
    msg += 'The request needs to be reloaded.';
    cmr.showAlert(msg, 'Warning', 'reloadRequestForApproval()');
  } else {
    CmrGrid.refresh('APPROVALS_GRID');
  }
}

function reloadRequestForApproval() {
  cmr.showProgress('Reloading request..');
  document.forms['frmCMR'].submit();
}

function processApprovalAction() {
  FormManager.setValue('approvalId', _approvals.currId);
  FormManager.setValue('approvalstatus', _approvals.status);
  var approvalAction = '';
  FormManager.setValue('typId', '1');
  if (_approvals.actionMode == 'CANCEL' || _approvals.actionMode == 'RESUBMIT') {
    approvalAction = _approvals.actionMode;
    FormManager.setValue('displayName', 'xxx');
    FormManager.setValue('intranetId', 'xxx');
    FormManager.setValue('displayName_bpcont', 'xxx:xxx');
  } else if (_approvals.actionMode == 'SEND_REQUEST' || _approvals.actionMode == 'SEND_REMINDER') {
    approvalAction = _approvals.actionMode;
    FormManager.setValue('displayName', 'xxx');
    FormManager.setValue('approval_cmt', 'xxx');
    FormManager.setValue('intranetId', 'xxx');
    FormManager.setValue('displayName_bpcont', 'xxx:xxx');
  } else if (_approvals.actionMode == 'OVERRIDE') {
    FormManager.setValue('requiredIndc', _approvals.requiredIndc);
    approvalAction = _approvals.actionMode;
  }

  if (FormManager.validate('frmCMR_addApprovalModal', true)) {
    cmr.modalmode = true;
    FormManager.doHiddenAction('frmCMR_addApprovalModal', approvalAction, cmr.CONTEXT_ROOT + '/approval/action.json', true, refreshApprovalGrid, true);
  }

}
// onLoad / onClose modals

function viewApprCommentsModal_onLoad() {
}

function viewApprCommentsModal_onClose() {
  CmrGrid.refresh('APPROVAL_CMT_GRID', cmr.CONTEXT_ROOT + '/approval/comments.json', 'reqId=' + reqId);
}

function addApprovalModal_onLoad() {
  var reqType = FormManager.getActualValue('reqType');
  cmr.currentModalId = 'addApprovalModal';
  // clear messages and values
  MessageMgr.clearMessages(true);
  if (reqType == 'C' || reqType == 'U') {
    FormManager.setValue('typId', '');
  }
  FormManager.setValue('displayName', '');
  FormManager.setValue('approval_cmt', '');
  FormManager.setValue('intranetId', '');
  FormManager.setValue('displayName_bpcont', '');
  FormManager.setValue('displayName_readonly', '(none selected)');

  // show default for add approval
  cmr.showNode('approvalTypeCont');
  cmr.showNode('approverCont');
  cmr.showNode('approvalBtnAdd');
  cmr.hideNode('approvalBtnSubmit');
  dojo.byId('displayName-lbl').innerHTML = 'Approver: <span style="color:red" class="cmr-ast" id="ast-approval_cmt">* </span>';
  cmr.setModalTitle('addApprovalModal', 'Add Approval');

  if (_approvals.actionMode == 'ADD_APPROVER') {
    // add approval, defaults
  } else if (_approvals.actionMode == 'CANCEL') {
    // cancel
    cmr.setModalTitle('addApprovalModal', 'Cancel Approval');
    cmr.hideNode('approvalTypeCont');
    cmr.hideNode('approverCont');
    cmr.hideNode('approvalBtnAdd');
    cmr.showNode('approvalBtnSubmit');
  } else if (_approvals.actionMode == 'RESUBMIT') {
    // cancel
    cmr.setModalTitle('addApprovalModal', 'Re-submit Approval');
    cmr.hideNode('approvalTypeCont');
    cmr.hideNode('approverCont');
    cmr.hideNode('approvalBtnAdd');
    cmr.showNode('approvalBtnSubmit');
  } else if (_approvals.actionMode == 'OVERRIDE') {
    // cancel
    FormManager.enable('displayName');
    cmr.setModalTitle('addApprovalModal', 'Override Approval');
    cmr.hideNode('approvalTypeCont');
    dojo.byId('displayName-lbl').innerHTML = 'New Approver: <span style="color:red" class="cmr-ast" id="ast-approval_cmt">* </span>';
    cmr.showNode('approverCont');
    cmr.hideNode('approvalBtnAdd');
    cmr.showNode('approvalBtnSubmit');
  }
}

function checkOverallApprovalStatus(showNotif) {
  var status = getOverallApprovalStatus();
  _approvals.latestStatus = status.status;
  if (!status.maxTs) {
    status.maxTs = '';
  }
  if (status.maxTs != null && _approvals.latestTs != status.maxTs && showNotif) {
    cmr.showNode('approval-notif');
  }
  _approvals.latestTs = status.maxTs;
}
function getOverallApprovalStatus() {
  var result = {};
  dojo.xhrGet({
    url : cmr.CONTEXT_ROOT + '/approval/status.json',
    handleAs : 'json',
    method : 'GET',
    content : {
      reqId : FormManager.getActualValue('reqId')
    },
    timeout : 50000,
    sync : true,
    load : function(data, ioargs) {
      if (data && data.status) {
        result = data;
      }
    },
    error : function(error, ioargs) {
      result = {
        status : 'None',
        maxTs : ''
      };
    }
  });
  return result;
}

function closeApprovalNotif() {
  cmr.hideNode('approval-notif');
}
// add the status check
dojo.addOnLoad(function() {
  if (_pagemodel.userRole != 'Viewer') {
    if (FormManager.getActualValue('reqId') != '0') {
      _approvals.overallStatus = _pagemodel.approvalResult;
      _approvals.latestStatus = _pagemodel.approvalResult;
      _approvals.latestTs = _pagemodel.approvalMaxTs;
      if (!_approvals.latestTs) {
        _approvals.latestTs = '';
      }
      if (CmrGrid.GRIDS.APPROVALS_GRID_GRID && CmrGrid.GRIDS.APPROVALS_GRID_GRID.rowCount > 0) {
        window.setInterval('checkOverallApprovalStatus(true)', 1000 * 60);
      }
    }
  }
});