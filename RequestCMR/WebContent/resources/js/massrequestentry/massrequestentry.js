/*
 * File: massrequestentry.js
 * Description: 
 * Contains the main functions in handling mass-request processing.
 * The sub functions used by each tab are separated from this file. This will
 * contain only the more general functions related to action executions general
 * UI handling
 * 
 */

dojo.require("dojo.io.iframe");

/**
 * Function to switch tabs
 */
function switchTabs(showId) {
  var existing = dojo.byId("reqId").value != '0';

  var cmrCntry = FormManager.getActualValue('cmrIssuingCntry');
  var massreqType = FormManager.getActualValue('reqType');
  if ((cmrCntry == '' || massreqType == '') && showId != 'GENERAL_REQ_TAB') {
    cmr.showAlert('CMR Issuing Country and Request Type must be specified to navigate to the other tabs.');
    cmr.selectTab(dojo.byId('MAIN_GENERAL_TAB'), 'switchTabs(\'GENERAL_REQ_TAB\')');
    return;
  }

  if (!existing && showId != 'GENERAL_REQ_TAB') {
    triggerSave();
  }

  if (typeof (openTabDetails) != 'undefined') {
    openTabDetails(showId);
  }

  cmr.currentTab = showId;
  dojo.cookie('lastTab', showId);
}

/**
 * Goes back to the workflow screens that accessed the page
 */
function goBackToWorkflow() {
  var url = dojo.byId('fromUrl').value;
  goToUrl(cmr.CONTEXT_ROOT + url);
}

/**
 * Processes the 'Your Actions' dropdown commands
 */
function processRequestAction() {
  var cmrCntry = FormManager.getActualValue('cmrIssuingCntry');
  var reqType = FormManager.getActualValue('reqType');
  var approvalResult = FormManager.getActualValue('approvalResult');

  if ((cmrCntry == '' || reqType == '')) {
    cmr.showAlert('CMR Issuing Country and Request Type need to be specified to execute actions.');
    return;
  }

  var action = FormManager.getActualValue('yourAction');
  if (action == '') {
    cmr.showAlert('Please select an action from the choices.');
  }

  // prevent from overwriting the DB REQ_STATUS
  // if another tab is open with different UI REQ_STATUS
  if (!isReqStatusEqualBetweenUIandDB()) {
    cmr.showAlert("Unable to save the request. Request Status mismatch from database." +
      "<br><br>Please reload the page.");

    return;
  }

  /* Your Actions processing here */

  if (action == YourActions.Save) {
    var hasError = !FormManager.validate('frmCMR', null, true);

    var uiReqStatus = FormManager.getActualValue('reqStatus');
    var reqId = FormManager.getActualValue('reqId');
    var dbReqStatus = "";

    var result = cmr.query("WW.GET_REQ_STATUS", {
      REQ_ID: reqId
    });
    if (result != null && result.ret1 != '' && result.ret1 != null) {
      dbReqStatus = result.ret1;
    }

    // prevent from overwriting the DB REQ_STATUS
    // if another tab is open with different UI REQ_STATUS
    if (uiReqStatus == dbReqStatus) {
      if (hasError) {
        FormManager.setValue('hasError', 'Y');
      }
      doSaveRequest();
    } else {
      cmr.showAlert("Unable to save the request. Request Status mismatch from database." +
      "<br><br>Please reload the page.");
    }

  } else if (action == YourActions.Validate) {
    doValidateRequest();

  } else if (action == YourActions.Claim) {
    if (approvalResult == 'Cond. Approved') {
      cmr.showConfirm('doYourAction()', 'The request was conditionally approved by ERO.', 'Warning', null, {
        OK : 'Ok',
        CANCEL : 'Cancel'
      });
    } else {
      doYourAction();
    }

  } else if (action == YourActions.All_Processing_Complete) {
    if (FormManager.validate('frmCMR')) {
      doYourAction();
    } else {
      cmr.showAlert('The request contains errors. Please check the list of errors on the page.');
    }

  } else if (action == YourActions.Cancel_Processing) {
    doYourAction();

  } else if (action == YourActions.Cancel_Request) {
    doYourAction();

  } else if (action == YourActions.Processing_Validation_Complete || action == YourActions.Processing_Validation_Complete2) {
    if (FormManager.validate('frmCMR')) {
      doYourAction();
    } else {
      cmr.showAlert('The request contains errors. Please check the list of errors on the page.');
    }

  } else if (action == YourActions.Reject) {
    doYourAction();

  } else if (action == YourActions.Send_for_Processing) {
    if (_pagemodel.approvalResult == 'Rejected') {
      cmr.showAlert('The request\'s approvals have been rejected. Please re-submit or override the rejected approvals. ');
    } else if (FormManager.validate('frmCMR')) {
      doYourAction();
    } else {
      cmr.showAlert('The request contains errors. Please check the list of errors on the page.');
    }

  } else if (action == YourActions.Processing_Create_Up_Complete) {
    if (FormManager.validate('frmCMR')) {
      doYourAction();
    } else {
      cmr.showAlert('The request contains errors. Please check the list of errors on the page.');
    }

  } else if (action == YourActions.Create_Update_Approved) {
    if (FormManager.validate('frmCMR')) {
      doYourAction();
    } else {
      cmr.showAlert('The request contains errors. Please check the list of errors on the page.');
    }

  } else if (action == YourActions.Create_Update_CMR) {
    if (_pagemodel.approvalResult == 'Rejected') {
      cmr.showAlert('The request\'s approvals have been rejected. Please re-submit or override the rejected approvals. ');
    } else if (FormManager.validate('frmCMR')) {
      if (isNewMassTemplateUsed() && isDPLCheckNeeded()) {
        MessageMgr.showErrorMessage('DPL Check is needed for uploaded template.');
      } else if (isNewMassTemplateUsed() && isAttachmentNeeded()) {
        MessageMgr.showErrorMessage('DPL Matching results has not been attached to the request. This is required since DPL checks failed for one or more addresses.');
      } else {
        doYourAction();
      }
    } else {
      cmr.showAlert('The request contains errors. Please check the list of errors on the page.');
    }

  } else if (action == YourActions.Unlock) {
    doYourAction();

  } else if (action == YourActions.Exit) {
    exitWithoutSaving();
  } else {
    cmr.showAlert('Invalid action.');
  }
}

/**
 * Go back to the workflow without saving the request
 */
function exitWithoutSaving() {
  window.location = cmr.CONTEXT_ROOT + '/workflow/open';
}

/**
 * Validates and unlocks a request
 */
function doValidateAndUnlock() {
  if (!FormManager.validate('frmCMR')) {
    cmr.showConfirm('doYourAction()', 'There are errors in the current request. Continue unlocking this request?', null, null, {
      OK : 'Yes',
      CANCEL : 'Cancel'
    });
  } else {
    promptUnlockNoValidationError();
  }
}

/**
 * Generic action processing for those that skip validation.
 */
function doYourAction() {
  MessageMgr.clearMessages();
  var action = FormManager.getActualValue('yourAction');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var status = FormManager.getActualValue('reqStatus');
  var reqType = FormManager.getActualValue('reqType');
  var lockind = FormManager.getActualValue('lockInd') == 'Y' ? 'Y' : 'N';
  var qParams = {
    action : action,
    cntry : cntry,
    status : status,
    lockind : lockind,
    reqtype : reqType
  };
  var result = cmr.query('transition', qParams);
  if (result.ret1 != status && action != YourActions.Claim) {
    cmr.statuschange = result;
    cmr.showModal('statusChangeModal');
  } else {
    dijit.byId('rejectReason').set('value', '');
    dojo.byId('statusChgCmt').value = '';
    dojo.byId('statusChgCmt_main').name = 'dummy';
    dojo.byId('statusChgCmt_main').value = '';
    dojo.byId('statusChgCmt_charind').innerHTML = 1000;
    FormManager.doAction('frmCMR', action, true);
  }
}

/**
 * Prompts the confirmation to save the request
 */
function promptSave() {
  cmr.showConfirm('doSaveRequest()', 'Save this request?', null, null, {
    OK : 'Yes',
    CANCEL : 'Cancel'
  });
}

/**
 * Prompts the save with no errors
 */
function promptSaveNoValidationError() {
  cmr.showConfirm('doSaveRequest()', 'No errors exist. Save this request?', null, null, {
    OK : 'Yes',
    CANCEL : 'Cancel'
  });
}

/**
 * Prompt for unlock
 */
function promptUnlock() {
  cmr.showConfirm('doSaveRequest()', 'Unlock this request?', null, null, {
    OK : 'Yes',
    CANCEL : 'Cancel'
  });
}

/**
 * Prompt for unlock without validation errors
 */
function promptUnlockNoValidationError() {
  cmr.showConfirm('doSaveRequest()', 'No errors exist. Unlock this request?', null, null, {
    OK : 'Yes',
    CANCEL : 'Cancel'
  });
}

/**
 * Save function
 */
function doSaveRequest(progressMsg) {
  // enable all checkboxes
  var cb = dojo.query('[type=checkbox]');
  for (var i = 0; i < cb.length; i++) {
    if (cb[i].id.indexOf('dijit') < 0 && cb[i].disabled) {
      cb[i].disabled = false;
      cb[i].removeAttribute('disabled');
    }
  }
  var action = FormManager.getActualValue('yourAction');
  FormManager.doAction('frmCMR', action, true, progressMsg);
}

/**
 * Validate and saves
 */
function doValidateAndSave() {
  if (!FormManager.validate('frmCMR')) {
    cmr.showConfirm('doSaveRequest()', 'There are errors in the current request. Continue saving this request?', null, null, {
      OK : 'Yes',
      CANCEL : 'Cancel'
    });
  } else {
    promptSaveNoValidationError();
  }
}

function isAttachmentNeeded() {
  console.log('>> Executing isAttachmentNeeded()');
  var qParams = {
    REQ_ID : FormManager.getActualValue('procReqId'),
    ITER_ID : FormManager.getActualValue('iterId'),
  };

  var result = cmr.query('COUNT.LD_MASS_UPDT_ATT_DPL_CHECK', qParams);
  var id = FormManager.getActualValue('reqId');
  var dplAtt = false;
  var ret = cmr.query('CHECK_DPL_ATTACHMENT', {
    ID : id
  });

  if (ret == null || ret.ret1 == null) {
    dplAtt = true;
  } else {
    dplAtt = false;
  }

  if (result != null && result.ret1 > 0 && dplAtt) {
    return true;
  } else {
    return false;
  }

}

function isDPLCheckNeeded() {
  console.log('>> Executing isDPLCheckNeeded()');
  var qParams = {
    REQ_ID : FormManager.getActualValue('procReqId'),
    ITER_ID : FormManager.getActualValue('iterId'),
  };

  var result = cmr.query('COUNT.LD_MASS_UPDT_INC_DPL_CHECK', qParams);

  if (result != null && result.ret1 > 0) {
    return true;
  } else {
    return false;
  }
}

function isNewMassTemplateUsed() {
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  qParams = {
    CNTRY : cntry,
  };
  var recordLD = cmr.query('CHECK_LD_MASS_NEW_TEMP', qParams);
  var countrecordLD = recordLD.ret1;

  var LA_COUNTRIES = [ SysLoc.BRAZIL, SysLoc.MEXICO, SysLoc.ARGENTINA, SysLoc.BOLIVIA, SysLoc.CHILE, SysLoc.COLOMBIA, SysLoc.COSTA_RICA, SysLoc.DOMINICAN_REPUBLIC, SysLoc.ECUADOR, SysLoc.GUATEMALA,
      SysLoc.HONDURAS, SysLoc.NICARAGUA, SysLoc.PANAMA, SysLoc.PARAGUAY, SysLoc.PERU, SysLoc.EL_SALVADOR, SysLoc.URUGUAY, SysLoc.VENEZUELA ];

  if (LA_COUNTRIES.indexOf(cntry) > -1) {
    return true;
  }

  if (Number(countrecordLD) > 0) {
    return true;
  } else {
    return false;
  }

}

function isCompanyProofNeeded() {
  console.log('>> Executing isCompanyProofNeeded()');

  var MCOAFRICA = [ '373', '382', '383', '610', '635', '636', '637', '645', '656', '662', '667', '669', '670', '691', '692', '698', '700', '717', '718', '725', '745', '753', '764', '769', '770',
      '782', '804', '810', '825', '827', '831', '833', '835', '840', '841', '842', '851', '857', '876', '879', '880', '881', '883' ];
  var cmrCntry = FormManager.getActualValue('cmrIssuingCntry');
  var reqId = FormManager.getActualValue('procReqId');
  var iterId = FormManager.getActualValue('iterId');

  if (MCOAFRICA.includes(cmrCntry)) {
    var qParams = {
      ID : reqId,
      ITER_ID : iterId,
    };

    var result = cmr.query('COUNT.LD_MASS_UPDT_ADDR_ZS01_ZP01', qParams);

    if (result != null && result.ret1 > 0) {

      var resultAttachment = cmr.query('CHECK_DNB_MATCH_ATTACHMENT', qParams);
      var attachmentCount = resultAttachment.ret1;

      if (attachmentCount > 0) {
        return false;
      }
      return true;
    } else {
      return false;
    }
  }
  return false;
}

/**
 * Validate function
 */
function doValidateRequest() {
  if (isDPLCheckNeeded() && _pagemodel.userRole.toUpperCase() == "PROCESSOR" && isNewMassTemplateUsed()) {
    MessageMgr.showErrorMessage('DPL Check is needed for uploaded template.');
  } else if (isAttachmentNeeded() && _pagemodel.userRole.toUpperCase() == "PROCESSOR" && isNewMassTemplateUsed()) {
    MessageMgr.showErrorMessage('DPL Matching results has not been attached to the request. This is required since DPL checks failed for one or more addresses.');
  } else if (isCompanyProofNeeded()) {
    MessageMgr.showErrorMessage('Proof of address is mandatory. Please attach Company Proof.');
  } else if (FormManager.validate('frmCMR')) {
    MessageMgr.showInfoMessage('The request has no errors.', null, true);
  } else {
    cmr.showAlert('The request contains errors. Please check the list of errors on the page.');
  }
}

/**
 * Executed when the status change modal is opened
 */
function statusChangeModal_onLoad() {
  var details = cmr.statuschange;
  var action = FormManager.getActualValue('yourAction');
  dijit.byId('rejectReason').set('value', '');
  cmr.hideNode('sendToBlock');
  cmr.hideNode('rejectReasonBlock');
  dojo.byId('newReqStatus').innerHTML = details.ret2;
  dojo.byId('statusChgCmt').value = '';
  dojo.byId('statusChgCmt_charind').innerHTML = 1000;
  if (action == YourActions.Reject) {
    cmr.showNode('rejectReasonBlock');
  } else if (action == YourActions.Send_for_Processing) {
    dojo.byId('sendToProcessingCenter').innerHTML = details.ret3;
    cmr.showNode('sendToBlock');
  }
}

/**
 * Executed when the status change modal is closed
 */
function statusChangeModal_onClose() {

}

function lengthInPageEncoding(s) {
  var a = document.createElement('A');
  a.href = '#' + s;
  var sEncoded = a.href;
  sEncoded = sEncoded.substring(sEncoded.indexOf('#') + 1);
  var m = sEncoded.match(/%[0-9a-f]{2}/g);
  return sEncoded.length - (m ? m.length * 2 : 0);
}

/**
 * Saves the comments for the status change and executes the action
 */
function doSaveChangeComments() {
  var action = FormManager.getActualValue('yourAction');
  var cmt = FormManager.getActualValue('statusChgCmt');
  if (cmt && cmt.length > 1000) {
    cmr.showAlert('Please limit comment length to 1000 characters.');
    return;
  }
  if (cmt && lengthInPageEncoding(cmt) > 1000) {
    cmr.showAlert('Please limit comment length to 1000 characters/bytes. If your comment has non-Latin1 characters or line breaks, '
        + 'these are larger than Latin-1 characters. The number of bytes of your comment is: ' + lengthInPageEncoding(cmt));
    return;
  }
  var rejReason = FormManager.getActualValue('rejectReason');
  if (action == YourActions.Reject && rejReason == '') {
    cmr.showAlert('Please specify the Reject Reason');
    return;
  } else if (action == YourActions.Reject) {
    var rej = FormManager.getActualValue('rejectReason');
    var rejField = '<input type="hidden" name="rejectReason" value="' + rej + '">';
    dojo.place(rejField, document.forms['frmCMR'], 'last');
  }
  var cmt = FormManager.getActualValue('statusChgCmt');
  cmt = cmt.replace(/"/g, '\"');
  dojo.byId('statusChgCmt_main').value = cmt;
  /* 824560 fixed to be able to handle strings with quotes */
  // var cmtField = '<input type="hidden" name="statusChgCmt" value="'+cmt+'">';
  // dojo.place(cmtField, document.forms['frmCMR'], 'last');
  try {
    var cb = dojo.query('[type=checkbox]');
    for (var i = 0; i < cb.length; i++) {
      if (cb[i].id.indexOf('dijit') < 0 && cb[i].disabled) {
        cb[i].disabled = false;
        cb[i].removeAttribute('disabled');
      }
    }
  } catch (e) {

  }
  FormManager.doAction('frmCMR', action, true);

}

/**
 * Forced saving triggered by some action
 */
function triggerSave() {
  var cmrCntry = FormManager.getActualValue('cmrIssuingCntry');
  var massreqType = FormManager.getActualValue('reqType');
  if ((cmrCntry == '' || massreqType == '')) {
    cmr.showAlert('CMR Issuing Country and Request Type must be specified to navigate to the other tabs.');
    cmr.selectTab(dojo.byId('MAIN_GENERAL_TAB'), 'switchTabs(\'GENERAL_REQ_TAB\')');
    return;
  }
  cmr.showConfirm('forceSaveRequest()', 'The request needs to be saved first before the tab can be accessed. Save the request?', null, 'goBackToLastTab()', {
    OK : 'Yes',
    CANCEL : 'Cancel'
  });
}

/**
 * Executes the forced save
 */
function forceSaveRequest() {
  FormManager.doAction('frmCMR', 'CREATE_NEW', true);
}

/**
 * Goes back to the last tab accessed
 */
function goBackToLastTab() {
  cmr.selectTab(dojo.byId('MAIN_GENERAL_TAB'), 'switchTabs(\'GENERAL_REQ_TAB\')');
}

/**
 * Formatter for Comment to warp the word
 * 
 * @param value
 * @param rowIndex
 * @returns {String}
 */
function commentFormatter(value, rowIndex) {
  rowData = this.grid.getItem(rowIndex);
  var cmtdata = rowData.cmt;
  if (cmtdata != null && cmtdata[0] != null) {
    cmtdata = cmtdata[0].replace(/\n/g, '<br>');
  }
  return '<span style="word-wrap: break-word">' + cmtdata + '</span>';
}

function commentImgFormatter(value, rowIndex) {
  if (!value) {
    return '&nbsp';
  }
  if (value.indexOf('@') > 0) {
    if (value.indexOf('ibm.com') > 0) {
      return '<img title="' + value + '" src="https://unified-profile-api.us-south-k8s.intranet.ibm.com/v3/image/' + value + '" class="cmt-img" onerror="this.onerror=null; this.src=\''
          + cmr.CONTEXT_ROOT + '/resources/images/person.jpg\'">';
    } else {
      return '<img title="' + value + '" src="' + cmr.CONTEXT_ROOT + '/resources/images/person.jpg" class="cmt-img">';
    }
  } else {
    return '<img title="' + value + '" src="' + cmr.CONTEXT_ROOT + '/resources/images/CreateCMRLogo.png" class="cmt-img">';
  }
}

function afterConfigChange() {
  // if the request is not new, disable cmr issuing country and request type
  FormManager.readOnly('cmrIssuingCntry');
  // var disableAutoProc = FormManager.getField('disableAutoProc');

  // if (disableAutoProc != null) {
  // disableAutoProc.checked = true;
  // FormManager.readOnly('disableAutoProc');
  // }

  FormManager.readOnly('reqType');
  if (dojo.byId("reqId").value != '0') {
  }

  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  if (cntry == '760') {
    if (typeof (GEOHandler) != 'undefined') {
      GEOHandler.executeAfterConfigs();
    }
  }
}

/**
 * Opens the workflow history window
 * 
 * @param requestId
 */
function openWorkflowHistory(requestId) {
  WindowMgr.open('WFHIST', requestId, 'wfhist?reqId=' + requestId, null, 550);
}

/* 835170, 835169 Add Summary Screens */
/**
 * Shows the request summary screens
 */
function showSummaryScreen(requestId, requestType) {
  var type = FormManager.getActualValue('reqType');
  if ('N' == type || 'M' == type || 'R' == type || 'D' == type || 'E' == type) {
    WindowMgr.open('SUMMARY', requestId, 'summary?reqId=' + requestId + '&reqType=' + type, null, type == 'E' ? '420' : null);
  } else {
    cmr.showAlert('Request Type has not been specified yet.');
  }
}

/**
 * Trigger Undo current changes on the main request
 */
function undoCurrentChanges() {
  cmr.showConfirm('doUndoChanges()', 'This will undo all current changes on this request. Changes to Processing, Attachments, and Notify List data cannot be undone. Proceed?', 'Warning', null, null);
}

/**
 * Undo current changes on the main request
 */
function doUndoChanges() {
  document.forms['frmCMR'].submit();
}

/**
 * Ensures mass file is uploaded before submit
 */
function addMassFileValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var result = FormManager.getActualValue('iterId');
        if (!result || result == '' || result <= 0) {
          return new ValidationResult(null, false, 'No file has been uploaded yet.');
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), null, 'frmCMR');
}

/**
 * Ensures CMR should be deleted to delete before submit
 * 
 */
function cmrsListGridValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (CmrGrid.GRIDS.CMR_LIST_GRID_GRID && CmrGrid.GRIDS.CMR_LIST_GRID_GRID.rowCount == 0) {
          return new ValidationResult(null, false, 'No CMR added, please add CMR and try again.');
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_PROC_TAB', 'frmCMR');
}

var _moveOk = false;

function addMoveHandler() {
  window.addEventListener('scroll', moveYourActions);
  moveYourActions();
  return;
}
/**
 * 
 * @param e
 */
function moveYourActions(e) {
  var elem = $('#cmr-your-actions');
  var scrollTop = window.pageYOffset || document.documentElement.scrollTop;
  if ((scrollTop) > 157) {
    elem.css({
      'top' : 0
    });
  } else {
    var pos = $('#ibm-secondary-tabs').offset();
    elem.css({
      'top' : (pos.top - scrollTop + 35)
    });
    if (scrollTop < 5) {
      window.setTimeout('moveYourActions()', 50);
      _moveOk = false;
    }
    if (scrollTop > 90 && !_moveOk) {
      window.setTimeout('moveYourActions()', 50);
      _modeOk = true;
    }
  }
}

var UpdateByEntSrvc = (function() {
  return {
    clickRadio : function(chk) {
      if (FormManager.getActualValue('cmrIssuingCntry') == '760') {
        if (chk.value == "A") {
          FormManager.enable('comp');
          FormManager.enable('cname1');
          FormManager.disable('comp1');
          FormManager.disable('newEntp');
          FormManager.disable('newEntpName');
          FormManager.disable('newEntpNameCont');
          FormManager.clearValue('comp1');
          FormManager.clearValue('newEntp');
          FormManager.clearValue('newEntpName');
          FormManager.clearValue('newEntpNameCont');
          FormManager.removeValidator('newEntpNameCont', Validators.REQUIRED);
          FormManager.removeValidator('newEntpName', Validators.REQUIRED);
          FormManager.removeValidator('comp1', Validators.REQUIRED);
          FormManager.removeValidator('newEntp', Validators.REQUIRED);
          FormManager.addValidator('comp', Validators.REQUIRED, [ 'Company Number' ]);
          FormManager.addValidator('cname1', Validators.REQUIRED, [ 'New Enterprise Name' ]);
          FormManager.removeValidator('comp1', Validators.NUMBER);
          FormManager.removeValidator('newEntp', Validators.NUMBER);
        } else if (chk.value == "B") {
          FormManager.enable('comp1');
          FormManager.enable('newEntp');
          FormManager.disable('comp');
          FormManager.disable('cname1');
          FormManager.disable('newEntpName');
          FormManager.disable('newEntpNameCont');
          FormManager.clearValue('comp');
          FormManager.clearValue('cname1');
          FormManager.clearValue('newEntpName');
          FormManager.clearValue('newEntpNameCont');
          FormManager.removeValidator('newEntpNameCont', Validators.REQUIRED);
          FormManager.removeValidator('newEntpName', Validators.REQUIRED);
          FormManager.removeValidator('comp', Validators.REQUIRED);
          FormManager.removeValidator('cname1', Validators.REQUIRED);
          FormManager.addValidator('comp1', Validators.REQUIRED, [ 'Establishment Number' ]);
          FormManager.addValidator('newEntp', Validators.REQUIRED, [ 'New Company Number' ]);
          FormManager.removeValidator('comp', Validators.NUMBER);
        } else if (chk.value == "C") {
          FormManager.enable('newEntpName');
          FormManager.enable('newEntpNameCont');
          FormManager.disable('comp');
          FormManager.disable('cname1');
          FormManager.disable('comp1');
          FormManager.disable('newEntp');
          FormManager.clearValue('comp');
          FormManager.clearValue('cname1');
          FormManager.clearValue('comp1');
          FormManager.clearValue('newEntp');
          FormManager.removeValidator('comp', Validators.REQUIRED);
          FormManager.removeValidator('cname1', Validators.REQUIRED);
          FormManager.removeValidator('comp1', Validators.REQUIRED);
          FormManager.removeValidator('newEntp', Validators.REQUIRED);
          FormManager.addValidator('newEntpName', Validators.REQUIRED, [ 'Establishment Number' ]);
          FormManager.addValidator('newEntpNameCont', Validators.REQUIRED, [ 'New Account Number' ]);
        }
      } else {
        if (chk.value == "A") {
          FormManager.enable('comp');
          FormManager.enable('cname1');
          FormManager.disable('comp1');
          FormManager.disable('newEntp');
          FormManager.disable('newEntpName');
          FormManager.disable('newEntpNameCont');
          FormManager.removeValidator('newEntpNameCont', Validators.REQUIRED);
          FormManager.removeValidator('newEntpName', Validators.REQUIRED);
          FormManager.removeValidator('comp1', Validators.REQUIRED);
          FormManager.removeValidator('newEntp', Validators.REQUIRED);
          FormManager.addValidator('comp', Validators.REQUIRED, [ 'Company Number' ]);
          FormManager.addValidator('cname1', Validators.REQUIRED, [ 'New Company Name' ]);
          FormManager.addValidator('comp', Validators.NUMBER, [ 'Company Number' ]);
          FormManager.removeValidator('comp1', Validators.NUMBER);
          FormManager.removeValidator('newEntp', Validators.NUMBER);
        } else if (chk.value == "B") {
          FormManager.enable('comp1');
          FormManager.enable('newEntp');
          FormManager.disable('comp');
          FormManager.disable('cname1');
          FormManager.disable('newEntpName');
          FormManager.disable('newEntpNameCont');
          FormManager.removeValidator('newEntpNameCont', Validators.REQUIRED);
          FormManager.removeValidator('newEntpName', Validators.REQUIRED);
          FormManager.removeValidator('comp', Validators.REQUIRED);
          FormManager.removeValidator('cname1', Validators.REQUIRED);
          FormManager.addValidator('comp1', Validators.REQUIRED, [ 'Company Number' ]);
          FormManager.addValidator('newEntp', Validators.REQUIRED, [ 'New Enterprise No.' ]);
          FormManager.addValidator('comp1', Validators.NUMBER, [ 'Company Number' ]);
          FormManager.addValidator('newEntp', Validators.NUMBER, [ 'New Enterprise No.' ]);
          FormManager.removeValidator('comp', Validators.NUMBER);
        } else if (chk.value == "C") {
          FormManager.enable('newEntpName');
          FormManager.enable('newEntpNameCont');
          FormManager.disable('comp');
          FormManager.disable('cname1');
          FormManager.disable('comp1');
          FormManager.disable('newEntp');
          FormManager.removeValidator('comp', Validators.REQUIRED);
          FormManager.removeValidator('cname1', Validators.REQUIRED);
          FormManager.removeValidator('comp1', Validators.REQUIRED);
          FormManager.removeValidator('newEntp', Validators.REQUIRED);
          FormManager.addValidator('newEntpName', Validators.REQUIRED, [ 'New Enterprise Name' ]);
          // FormManager.addValidator('newEntpNameCont', Validators.REQUIRED, [
          // 'New Enterprise Name Cont.' ]);
        }
      }
    },
    delayedConnectRadios : function() {
      if (!dijit.byId('entUpdTyp_A')) {
        // wait for dijit to register the widgets
        window.setTimeout('UpdateByEntSrvc.delayedConnectRadios()', 500);
      } else {
        dojo.connect(dijit.byId('entUpdTyp_A'), 'onClick', function(evt) {
          UpdateByEntSrvc.clickRadio(evt.target);
        });
        dojo.connect(dijit.byId('entUpdTyp_B'), 'onClick', function(evt) {
          UpdateByEntSrvc.clickRadio(evt.target);
        });
        dojo.connect(dijit.byId('entUpdTyp_C'), 'onClick', function(evt) {
          UpdateByEntSrvc.clickRadio(evt.target);
        });
      }
    },
    disableEntFields : function() {
      FormManager.readOnly('entNo');
      FormManager.readOnly('comp');
      FormManager.readOnly('cname1');
      FormManager.readOnly('comp1');
      FormManager.readOnly('newEntp');
      FormManager.readOnly('newEntpName');
      FormManager.readOnly('newEntpNameCont');
      FormManager.readOnly('entUpdTyp');
    }
  };
})();

function addEnterpriseValidator() {
  var entUpdTyp = document.getElementById("entUpdType").value;
  if (entUpdTyp == 'A') {
    document.getElementById("entUpdTyp_A").checked = true;
    FormManager.enable('comp');
    FormManager.enable('cname1');
    FormManager.disable('comp1');
    FormManager.disable('newEntp');
    FormManager.disable('newEntpName');
    FormManager.disable('newEntpNameCont');
  } else if (entUpdTyp == 'B') {
    document.getElementById("entUpdTyp_B").checked = true;
    FormManager.enable('comp1');
    FormManager.enable('newEntp');
    FormManager.disable('comp');
    FormManager.disable('cname1');
    FormManager.disable('newEntpName');
    FormManager.disable('newEntpNameCont');
  } else if (entUpdTyp == 'C') {
    document.getElementById("entUpdTyp_C").checked = true;
    FormManager.enable('newEntpName');
    FormManager.enable('newEntpNameCont');
    FormManager.disable('comp');
    FormManager.disable('cname1');
    FormManager.disable('comp1');
    FormManager.disable('newEntp');
  }
  if (!document.getElementById("entUpdTyp_A").checked && !document.getElementById("entUpdTyp_B").checked && !document.getElementById("entUpdTyp_C").checked) {
    document.getElementById("entUpdTyp_A").checked = true;
    FormManager.enable('comp');
    FormManager.enable('cname1');
    FormManager.disable('comp1');
    FormManager.disable('newEntp');
    FormManager.disable('newEntpName');
    FormManager.disable('newEntpNameCont');
  }
}

function showChangeLog(requestId) {
  WindowMgr.open('CHANGELOG', requestId, 'reqchangelog?reqId=' + requestId, 1050, 570);
}

function doMassDplChecking() {
  cmr.showConfirm('doRunMassDplChecking()', 'DPL Check will be performed in every row on the current uploaded mass file. Proceed?');
}

function doRunMassDplChecking() {
  var url = cmr.CONTEXT_ROOT + '/massrequest/dpl.json';
  FormManager.doHiddenAction('frmCMRProcess', 'MASS_DPL', url, true, refreshAfterMassDplCheck, false);
}

function refreshAfterMassDplCheck(result) {
  if (result.success) {
    FormManager.setValue('dplMessage', result.message);
    /*
     * var cb = dojo.query('[type=checkbox]'); for ( var i = 0; i < cb.length;
     * i++) { if (cb[i].id.indexOf('dijit') < 0 && cb[i].disabled) {
     * cb[i].disabled = false; cb[i].removeAttribute('disabled'); } }
     */
    FormManager.setValue('dplMessage', result.message);
    FormManager.doAction('frmCMR', 'DPL_REFRESH', true);
  }
}

/**
 * Generic method to open CI Supportal with most values
 */
function openCISupportal() {
  var params = {};
  var error = dojo.byId('cmr-error-box-msg') ? dojo.byId('cmr-error-box-msg').innerHTML : null;
  if (error) {
    params.issueType = 'A';
    error = error.toLowerCase();
    if (error.includes('an unexpected error has occurred')) {
      params.appIssue = 'S';
    } else if (error.includes('dpl check cannot be done')) {
      params.appIssue = 'D';
    } else if (error.includes('error retrieving current values')) {
      params.appIssue = 'I';
    } else {
      params.appIssue = 'O';
    }
  }
  if (typeof (_pagemodel) != 'undefined') {
    params.requestId = _pagemodel.reqId;
    params.cmrNo = 'N/A';
    params.country = _pagemodel.cmrIssuingCntry;
  }
  CISupportal.open(params);
  cmr.hideNode('supportal');
}

function openNPSFeedback(url) {
  var reqId = FormManager.getActualValue('reqId');
  WindowMgr.open('FEEDBACK', reqId, url, 900, 700, true);
}

function showDPLSummaryScreen() {
  var type = FormManager.getActualValue('reqType');
  var requestId = FormManager.getActualValue('reqId');
  WindowMgr.open('SUMMARY', requestId, 'dplsummary?reqId=' + requestId + '&reqType=' + type, null, type == 'E' ? '420' : null);
  // if ('N' == type || 'M' == type || 'R' == type || 'D' == type || 'E' ==
  // type) {
  // WindowMgr.open('SUMMARY', requestId, 'summary?reqId=' + requestId +
  // '&reqType=' + type, null, type == 'E' ? '420' : null);
  // } else {
  // cmr.showAlert('Request Type has not been specified yet.');
  // }
}

// dojo.addOnLoad(function() {
// GEOHandler.
// GEOHandler.registerValidator(isDPLCheckNeeded, GEOHandler.CN,
// GEOHandler.ROLE_REQUESTER, false, false);
// GEOHandler.registerValidator(isDPLCheckNeeded, [ SysLoc.SPAIN, SysLoc. ]);
// });

function isReqStatusEqualBetweenUIandDB() {

  var uiReqStatus = FormManager.getActualValue('reqStatus');
  var reqId = FormManager.getActualValue('reqId');
  var dbReqStatus = "";

  var result = cmr.query("WW.GET_REQ_STATUS", {
    REQ_ID: reqId
  });
  if (result != null && result.ret1 != '' && result.ret1 != null) {
    dbReqStatus = result.ret1;
  }

  return uiReqStatus == dbReqStatus;
}
