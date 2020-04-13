/*
 * File: requestentry.js
 * Description: 
 * Contains the main functions in handling single-request processing.
 * The sub functions used by each tab are separated from this file. This will
 * contain only the more general functions related to action executions general
 * UI handling
 * 
 */
var CNTRY_LIST_FOR_INVALID_CUSTOMERS = [ '838', '866', '754' ];
dojo.require("dojo.io.iframe");

/**
 * Function to switch tabs
 */
function switchTabs(showId, noCheck) {
  var existing = dojo.byId("reqId").value != '0';

  // for new requests, CMR Issuing Country and Request Type must be specified
  var cmrCntry = FormManager.getActualValue('cmrIssuingCntry');
  var reqType = FormManager.getActualValue('reqType');
  if (!existing && !noCheck && (cmrCntry == '' || reqType == '') && showId != 'GENERAL_REQ_TAB') {
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

  // show the address tab for some cases
  // where the last action was on the address tab
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

  // CMRIssuing Country and Request Type must be specified before processing the
  // action
  if ((cmrCntry == '' || reqType == '')) {
    cmr.showAlert('CMR Issuing Country and Request Type need to be specified to execute actions.');
    return;
  }

  var action = FormManager.getActualValue('yourAction');
  if (action == '') {
    cmr.showAlert('Please select an action from the choices.');
  }

  /* Your Actions processing here */

  if (action == YourActions.Save) {
    var hasError = !FormManager.validate('frmCMR', null, true);
    if (hasError) {
      FormManager.setValue('hasError', 'Y');
    }
    doSaveRequest();

  } else if (action == YourActions.Validate) {
    cmr.showProgress('Checking request data..');
    window.setTimeout('doValidateRequest()', 500);

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
    // for any request type, CMR No is needed to complete the workflow
    // 1790834 - except JP Update in BC/BF scenarios
    var cmrNo = FormManager.getActualValue('cmrNo');
    var cntry = FormManager.getActualValue('cmrIssuingCntry');
    if (cmrNo == '' && cntry != '760') {
      cmr.showAlert('This action cannot be completed until a CMR# is entered.  Please enter the CMR# and try the action again.');
      return;
    } else if (cmrNo == '' && cntry == '760') {
      var reqType = FormManager.getActualValue('reqType');
      var custSubGrp = FormManager.getActualValue('custSubGrp');
      if (cmrNo == '' && !((reqType == 'U' || reqType == 'C') && (custSubGrp == 'BFKSC' || custSubGrp == 'BCEXA'))) {
        cmr.showAlert('This action cannot be completed until a CMR# is entered.  Please enter the CMR# and try the action again.');
        return;
      }
    }
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
      if (cmrCntry == '821') {
        executeBeforeSubmit();
      } else {
        // if there are no errors, show the Address Verification modal window
        cmr.showModal('addressVerificationModal');
      }
    } else {
      cmr.showAlert('The request contains errors. Please check the list of errors on the page.');
    }

  } else if (action == YourActions.Processing_Create_Up_Complete) {
    var cmrNo = FormManager.getActualValue('cmrNo');
    var disAutoProc = FormManager.getActualValue('disableAutoProc');
    // for any request type, CMR No is needed to complete the workflow
    // 1790834 - JP Update use company number instead in BF and BC scenario.
    var cntry = FormManager.getActualValue('cmrIssuingCntry');
    if (cmrNo == '' && disAutoProc == 'Y' && cntry != '760') {
      cmr.showAlert('This action cannot be completed until a CMR# is entered.  Please enter the CMR# and try the action again.');
      return;
    } else if (cmrNo == '' && disAutoProc == 'Y' && cntry == '760') {
      var reqType = FormManager.getActualValue('reqType');
      var custSubGrp = FormManager.getActualValue('custSubGrp');
      if (cmrNo == '' && !((reqType == 'U' || reqType == 'C') && (custSubGrp == 'BFKSC' || custSubGrp == 'BCEXA'))) {
        cmr.showAlert('This action cannot be completed until a CMR# is entered.  Please enter the CMR# and try the action again.');
        return;
      }
    }
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
      var result = FormManager.getActualValue('dplChkResult');
      // if DPL check status is not 'All Passed', ask for confirmation to
      // proceed
      if (result == '' || (result.toUpperCase() != 'AP' && result.toUpperCase() != 'NR')) {
        cmr.showConfirm('doYourAction()', 'This request has not passed all DPL checks. Are you sure you want to process this request?', 'Warning', null, {
          OK : 'Yes',
          CANCEL : 'No'
        });
      } else {
        // if the customer names have changed since last save and it is being
        // asked for creation,
        // force a save to reset DPL check
        var nm1 = _pagemodel.mainCustNm1 == null ? '' : _pagemodel.mainCustNm1;
        var nm2 = _pagemodel.mainCustNm2 == null ? '' : _pagemodel.mainCustNm2;
        if (nm1 != FormManager.getActualValue('mainCustNm1') || nm2 != FormManager.getActualValue('mainCustNm2')) {
          cmr.showAlert("The Customer Name/s have changed. The record has to be saved first. Please select Save from the actions. A DPL check will also be necessary after saving. ");
        } else {
          doYourAction();
        }
      }
    } else {
      cmr.showAlert('The request contains errors. Please check the list of errors on the page.');
    }

  } else if (action == YourActions.Unlock) {
    doYourAction();

  } else if (action == YourActions.Exit) {
    exitWithoutSaving();
  } else if (action == YourActions.Reprocess_Checks) {
    var ifReprocessAllowed = FormManager.getActualValue('autoEngineIndc');
    if (ifReprocessAllowed == 'R' || ifReprocessAllowed == 'P' || ifReprocessAllowed == 'B') {
      doYourAction();
    } else {
      cmr.showAlert('The request\'s country is not enabled for automated checks.\n Action cannot be completed');
    }
  } else {
    cmr.showAlert('Invalid action.');
  }
}

/**
 * Shows the prompt for the Address Verification disclaimer
 */
function showAddressVerificationModal() {
  cmr.showModal('addressVerificationModal');
}

/**
 * Accept the address verification disclaimer
 */
function doAcceptAddressVerification() {
  if (!dojo.byId('addrVerAgree').checked) {
    cmr.showAlert('You must agree to the Address Verification Rules to proceed.');
    return;
  }
  cmr.hideModal('addressVerificationModal');
  doYourAction();
}

/**
 * Hides the address verification modal
 */
function doCancelAddressVerification() {
  cmr.hideModal('addressVerificationModal');
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
    var cb = dojo.query('[type=checkbox]');
    for (var i = 0; i < cb.length; i++) {
      if (cb[i].id.indexOf('dijit') < 0 && cb[i].disabled) {
        cb[i].disabled = false;
        cb[i].removeAttribute('disabled');
      }
    }
    var rd = dojo.query('[type=radio]');
    for (var i = 0; i < rd.length; i++) {
      if (rd[i].id.indexOf('dijit') < 0 && rd[i].disabled) {
        rd[i].disabled = false;
        rd[i].removeAttribute('disabled');
      }
    }
    if (typeof (_doBeforeAction) != 'undefined') {
      _doBeforeAction(action);
    }
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
  if (typeof (_doBeforeAction) != 'undefined') {
    _doBeforeAction(action);
  }
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

/**
 * Validate function
 */
function doValidateRequest() {
  if (FormManager.validate('frmCMR')) {
    cmr.hideProgress();
    MessageMgr.showInfoMessage('The request has no errors.', null, true);
  } else {
    cmr.hideProgress();
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
  // noop
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
 * 
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
  } else if (action == YourActions.Reject && (rejReason == 'DUPC' || rejReason == 'MAPP') && (FormManager.getActualValue('rejSupplInfo1') == '' || FormManager.getActualValue('rejSupplInfo2') == '')) {
    cmr.showAlert('Please specify ' + dojo.byId('rejInfo1Label').innerText + " and " + dojo.byId('rejInfo2Label').innerText + ".");
    return;
  } else if (action == YourActions.Reject && (rejReason == 'MDOC' || rejReason == 'DUPR' || rejReason == 'TYPR') && FormManager.getActualValue('rejSupplInfo1') == '') {
    cmr.showAlert('Please specify ' + dojo.byId('rejInfo1Label').innerText + ".");
    return;
  } else if (action == YourActions.Reject) {
    var rej = FormManager.getActualValue('rejectReason');
    var rejInfo1 = FormManager.getActualValue('rejSupplInfo1');
    var rejInfo2 = FormManager.getActualValue('rejSupplInfo2');
    var rejField = '<input type="hidden" name="rejectReason" value="' + rej + '">';
    rejField += '<input type="hidden" name="rejSupplInfo1" value="' + rejInfo1 + '">';
    rejField += '<input type="hidden" name="rejSupplInfo2" value="' + rejInfo2 + '">';
    dojo.place(rejField, document.forms['frmCMR'], 'last');
  }

  /* 824560 fixed to be able to handle strings with quotes */
  var cmt = FormManager.getActualValue('statusChgCmt');
  cmt = cmt.replace(/"/g, '\"');
  dojo.byId('statusChgCmt_main').value = cmt;

  // enable all checkboxes for saving properly
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
 * Shows the address tab. This method is called when the last action that
 * triggered an overall save was under the Address tab like DPL check or adding
 * new address records
 */
function showAddressTab() {
  if (FormManager.getActualValue('cmrIssuingCntry') != '') {
    cmr.selectTab(dojo.byId('MAIN_NAME_TAB'), 'switchTabs(\'NAME_REQ_TAB\')');
  }
}

/**
 * Shows the taxinfo tab. This method is called when the last action that
 * triggered an overall save was under the Tax Info tab like adding new tax info
 * records
 */
function showTaxInfoTab() {
  if (FormManager.getActualValue('cmrIssuingCntry') != '' && PageManager.fromGeo("LA", reqentry.getCmrIssuingCntry())) {
    cmr.selectTab(dojo.byId('MAIN_TAXINFO_TAB'), 'switchTabs(\'TAXINFO_REQ_TAB\')');
  }
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
    cmtdata = cmtdata[0].replace(/</g, '[');
    cmtdata = cmtdata.replace(/>/g, ']');
    cmtdata = cmtdata.replace(/\n/g, '<br>');
  }
  return '<span style="word-wrap: break-word">' + cmtdata + '</span>';
}

/**
 * Disables the CMR Search and D&B Search buttons if page is read-only
 * 
 * @param readOnly
 */
function addCMRSearchHandler(readOnly) {
  if (readOnly) {
    FormManager.disable('cmrSearchBtn');
    dojo.removeClass(dojo.byId('cmrSearchBtn'), 'ibm-btn-cancel-pri');
    dojo.addClass(dojo.byId('cmrSearchBtn'), 'ibm-btn-cancel-disabled');
    FormManager.disable('dnbSearchBtn');
    dojo.removeClass(dojo.byId('dnbSearchBtn'), 'ibm-btn-cancel-pri');
    dojo.addClass(dojo.byId('dnbSearchBtn'), 'ibm-btn-cancel-disabled');
    FormManager.disable('dplCheckBtn');
    dojo.removeClass(dojo.byId('dplCheckBtn'), 'ibm-btn-cancel-pri');
    dojo.addClass(dojo.byId('dplCheckBtn'), 'ibm-btn-cancel-disabled');
  }
}

var _inacHandler = null;
var _typeHandler = null;
var _countyHandler = null;
var _enterCMRHandler = null;
var _templateHandler = null;
var defaultLandCntry = null;
var _rejSupplInfoHandler = null;

/**
 * Executed after PageManager loads all the scripts. Place here code that needs
 * to be executed to override the PageManager configurable fields' settings
 */
function afterConfigChange() {
  // add special INAC value validator
  // if INAC Type = I, the code should be a number
  var cmrCntry = FormManager.getActualValue('cmrIssuingCntry');
  if (_inacHandler == null) {
    _inacHandler = dojo.connect(FormManager.getField('inacType'), 'onChange', function(value) {
      var value = FormManager.getActualValue('inacType');
      if (cmrCntry != '616' && (value && dojo.string.trim(value) == 'I')) {
        FormManager.addValidator('inacCd', Validators.NUMBER, [ 'INAC Code' ], 'MAIN_IBM_TAB');
      } else {
        FormManager.removeValidator('inacCd', Validators.NUMBER);
      }
    });
  }
  if (_inacHandler && _inacHandler[0]) {
    _inacHandler[0].onChange();
  }

  // rejectReason
  if (_rejSupplInfoHandler == null) {
    _rejSupplInfoHandler = dojo.connect(FormManager.getField('rejectReason'), 'onChange', function(value) {
      setRejSupplInfoFields(value);
    });
  }

  // ensure CMR No value on the main tab
  if ((_pagemodel.reqType == 'U' || _pagemodel.reqType == 'X') && FormManager.getActualValue('cmrNo') != '') {
    FormManager.setValue('enterCMRNo', FormManager.getActualValue('cmrNo'));
  }

  // CMR No is always read only for update types
  if ((_pagemodel.reqType == 'U' || _pagemodel.reqType == 'X') && FormManager.getActualValue('cmrNo') != '') {
    FormManager.readOnly('cmrNo');
  }

  // delayed method to ensure the CMR no is loaded on the Enter CMR Number field
  // used window timeouts to check for some time the need to assign it
  if (_enterCMRHandler == null) {
    _enterCMRHandler = dojo.connect(FormManager.getField('enterCMRNo'), 'onChange', function(value) {
      var cmr = FormManager.getActualValue('enterCMRNo');
      if (cmr == '' && FormManager.getActualValue('cmrNo') != '') {
        FormManager.setValue('enterCMRNo', FormManager.getActualValue('cmrNo'));
      }
    });
  }

  if (_typeHandler == null) {
    _typeHandler = dojo.connect(FormManager.getField('reqType'), 'onChange', function(value) {
      var type = FormManager.getActualValue('reqType');
      if (FormManager.getActualValue('custSubGrp') != '') {
        TemplateService.fill('reqentry');
      }
      if (type == 'C' && _pagemodel && _pagemodel.reqType == 'U' && cmr.noCreatePop == 'N') {
        var confMsg = 'The CMR Number and SAP Numbers will be removed from the request when the type is changed to Create.  Do you want to proceed?';
        cmr.showConfirm('changeToCreateType()', confMsg, null, 'keepUpdateType()', {
          OK : 'Yes',
          CANCEL : 'No'
        });
      }
    });

  }

  // show the address tab if it was the last tab accessed prior to opening the
  // details
  if (dojo.cookie('lastTab') == 'NAME_REQ_TAB' && dojo.byId("reqId").value != '0') {
    // showAddressTab();
  }
  // show the tax info tab if it was the last tab accessed prior to opening the
  // details
  if (dojo.cookie('lastTab') == 'TAXINFO_REQ_TAB' && dojo.byId("reqId").value != '0') {
    // showTaxInfoTab();
  }

  FormManager.readOnly('cmrIssuingCntry');
  FormManager.readOnly('reqType');

  // if the request is not new, disable cmr issuing country and request type
  if (dojo.byId("reqId").value != '0') {

    // if this is a Prospect to legal conversion, show the checkbox for prosp to
    // legal
    // and disable it
    if (FormManager.getActualValue('ordBlk') == '75') {
      FormManager.show('ProspectToLegalCMR', 'prospLegalInd');
      if (dijit.byId('prospLegalInd')) {
        FormManager.getField('prospLegalInd').set('checked', true);
      } else {
        dojo.byId('prospLegalInd').checked = true;
      }
      FormManager.disable('prospLegalInd');
      FormManager.readOnly('cmrNo');
    }
    var cntry = FormManager.getActualValue('cmrIssuingCntry');

    if (FormManager.getActualValue('ordBlk') == '93' && !CNTRY_LIST_FOR_INVALID_CUSTOMERS.includes(cntry)) {
      FormManager.show('DeactivateToActivateCMR', 'func');
      if (dijit.byId('func')) {
        FormManager.getField('func').set('checked', true);
      } else {
        dojo.byId('func').checked = true;
      }
      FormManager.disable('func');
      FormManager.readOnly('cmrNo');
    }
  }

  // populate the country name field when the county code is chosen
  if (_countyHandler == null && FormManager.getField('county')) {
    _countyHandler = dojo.connect(FormManager.getField('county'), 'onChange', function(value) {
      var field = FormManager.getField('county');
      if (field && field.displayedValue) {
        FormManager.setValue('countyName', field.displayedValue);
      } else {
        FormManager.setValue('countyName', '');
      }
    });
  }
  if (_countyHandler && _countyHandler[0]) {
    _countyHandler[0].onChange();
  }

  // add special handler for Client Tier
  // TODO this should be changed to dynamic after 1.2
  // requester specific logic

  if (typeof (_pagemodel) != 'undefined' && _pagemodel.userRole == 'Requester') {
    FormManager.hide('DisableAutoProcessing', 'disableAutoProc');
    if (FormManager.getActualValue('reqType') == 'C') {
      FormManager.readOnly('cmrNo');
    }
  } else {
    if (dijit.byId('disableAutoProc')) {
      dojo.connect(FormManager.getField('disableAutoProc'), 'onClick', function(value) {
        var val = value.target.checked;
        cmr.showConfirm('saveDisableProcSetting()', 'Changing the setting for Automatic Processing will trigger an automatic save. Proceed?', 'Confirm', 'setDisableAutoProcVal(' + val + ')');
      });
    } else {
      dojo.byId('disableAutoProc').onClick = function(e) {
        var val = e.target.checked;
        cmr.showConfirm('saveDisableProcSetting()', 'Changing the setting for Automatic Processing will trigger an automatic save. Proceed?', 'Confirm', 'setDisableAutoProcVal(' + val + ')');
      };
    }

  }

  // processor specific logic
  if (typeof (_pagemodel) != 'undefined' && _pagemodel.userRole == 'Processor') {
    FormManager.readOnly('cmrOwner');

    if (dijit.byId('disableAutoProc') && !dijit.byId('disableAutoProc').get('checked')) {
      FormManager.readOnly('cmrNo');
    } else if (dojo.byId('disableAutoProc') && dojo.byId('disableAutoProc').checked) {
      if (FormManager.getActualValue('reqType') == 'C') {
        FormManager.enable('cmrNo');
      } else {
        FormManager.readOnly('cmrNo');
      }
    }

  }
  FormManager.setValue('dept_int', _pagemodel.dept);

  window.setTimeout('ensureCMRNoValue()', 1000);

  if (typeof (GEOHandler) != 'undefined') {
    GEOHandler.executeAfterConfigs();
  }
  dnbAutoChk();

  FormManager.ready();
}

/**
 * Sets the value for Disable Auto Processing (checked, unchecked)
 * 
 * @param disable
 */
function setDisableAutoProcVal(disable) {
  FormManager.getField('disableAutoProc').set('checked', !disable);
}

/**
 * When Disable Auto Processing has been changed (processor only), save the
 * request to trigger load new Your Actions set
 */
function saveDisableProcSetting() {
  FormManager.setValue('yourAction', 'SAV');
  doSaveRequest();
}

/**
 * Executed a few times until the correct value is placed on the Enter CMR No
 * field
 */
function ensureCMRNoValue() {
  if (FormManager.getActualValue('reqId') == '0') {
    return;
  }
  console.log('Ensuring CMR No. value.. ' + FormManager.getActualValue('enterCMRNo'));
  // also retry assignment of this value
  FormManager.setValue('dept_int', _pagemodel.dept);
  if (FormManager.getActualValue('reqType') == '') {
    window.setTimeout('ensureCMRNoValue()', 1000);
    return;
  }
  if (dojo.byId("reqId").value != '0') {
    FormManager.readOnly('reqType');
  }
  if ((FormManager.getActualValue('reqType') == 'U' || FormManager.getActualValue('reqType') == 'X') && FormManager.getActualValue('enterCMRNo') == '') {
    if ((FormManager.getActualValue('reqType') == 'U' || FormManager.getActualValue('reqType') == 'X') && FormManager.getActualValue('cmrNo') != '') {
      FormManager.setValue('enterCMRNo', FormManager.getActualValue('cmrNo'));
    }
    if ((FormManager.getActualValue('reqType') == 'U' || FormManager.getActualValue('reqType') == 'X') && FormManager.getActualValue('cmrNo') != '') {
      FormManager.readOnly('cmrNo');
    }
    window.setTimeout('ensureCMRNoValue()', 1000);
  }
}

/**
 * Changes the type to Create
 */
function changeToCreateType() {
  FormManager.setValue('reqType', 'C');
}

/**
 * Keeps the type as Update
 */
function keepUpdateType() {
  FormManager.setValue('reqType', 'U');
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
  if ('C' == type || 'U' == type || 'X' == type) {
    WindowMgr.open('SUMMARY', requestId, 'summary?reqId=' + requestId + '&reqType=' + type);
  } else {
    cmr.showAlert('Request Type has not been specified yet.');
  }
}

/**
 * Trigger to connect to the CMR Services and retrieves the values for: <br>
 * <ul>
 * <li>Coverage</li>
 * <li>Buying</li>
 * <li>Group DUNS GLC</li>
 * </ul>
 */
function retrieveInterfaceValues() {
  var count = CmrGrid.GRIDS && CmrGrid.GRIDS.ADDRESS_GRID_GRID ? CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount : 0;
  if (count < 1) {
    cmr.showAlert('Please add at least one address to the request to perform this function.');
    return;
  }
  cmr.showProgress('Retrieving values. Please wait...');
  window.setTimeout('connectToCmrServices()', 500);
}

/**
 * Connects to the CMR Services and retrieves the values for: <br>
 * <ul>
 * <li>Coverage</li>
 * <li>Buying</li>
 * <li>Group DUNS GLC</li>
 * </ul>
 */
function connectToCmrServices() {
  var data = CmrServices.getAll('reqentry');
  cmr.hideProgress();
  if (data) {
    console.log(data);
    if (data.error && data.error == 'Y') {
      cmr.showAlert('An error was encountered when retrieving the values.\nPlease contact your system administrator.', 'Create CMR');
    } else {
      var showError = false;
      var covError = false;
      var errorMsg = 'The following values cannot be retrieved at the moment: ';
      if (data.coverageError || data.buyingGroupError || data.glcError) {
        covError = true;
      }
      if (data.coverageError) {
        errorMsg += 'Coverage Type/ID';
        showError = true;
      } else if (!covError) {
        FormManager.setValue('covId', data.coverageType + data.coverageID);
        FormManager.setValue('covDesc', data.coverageDesc);
        dojo.byId('covDescCont').innerHTML = data.coverageDesc != null ? data.coverageDesc : '(no description available)';
        if (data.clientTier == null || data.clientTier.trim() == '') {
          /*
           * 1490262: Client Tier Code is set to Unassigned after Retrieving
           * values
           */
          /* jz: do not set anything */
          // FormManager.setValue('clientTier', '');
        } else {
          FormManager.setValue('clientTier', data.clientTier);
        }
      }
      if (data.buyingGroupError) {
        errorMsg += (showError ? ', ' : '') + 'Buying Group ID';
        showError = true;
      } else {
        FormManager.setValue('bgId', data.buyingGroupID);
        FormManager.setValue('bgDesc', data.buyingGroupDesc);
        dojo.byId('bgDescCont').innerHTML = data.buyingGroupDesc != null ? data.buyingGroupDesc : '(no description available)';

        FormManager.setValue('gbgId', data.globalBuyingGroupID);
        FormManager.setValue('gbgDesc', data.globalBuyingGroupDesc);
        FormManager.setValue('bgRuleId', data.odmRuleID);
        dojo.byId('gbgDescCont').innerHTML = data.globalBuyingGroupDesc != null ? data.globalBuyingGroupDesc : '(no description available)';

      }
      if (data.glcError) {
        errorMsg += (showError ? ', ' : '') + 'GEO Location Code';
        showError = true;
      } else {
        FormManager.setValue('geoLocationCd', data.glcCode);
        FormManager.setValue('geoLocDesc', data.glcDesc);
        dojo.byId('geoLocDescCont').innerHTML = data.glcDesc != null ? data.glcDesc : '(no description available)';
      }
      if (data.dunsError) {
        errorMsg += (showError ? ', ' : '') + 'DUNS No.';
        showError = true;
      } else {
        var sysLocCd = FormManager.getActualValue('cmrIssuingCntry');
        var COUNTRIES = [ SysLoc.BRAZIL, SysLoc.MEXICO, SysLoc.ARGENTINA, SysLoc.BOLIVIA, SysLoc.CHILE, SysLoc.COLOMBIA, SysLoc.COSTA_RICA, SysLoc.DOMINICAN_REPUBLIC, SysLoc.ECUADOR,
            SysLoc.GUATEMALA, SysLoc.HONDURAS, SysLoc.NICARAGUA, SysLoc.PANAMA, SysLoc.PARAGUAY, SysLoc.PERU, SysLoc.EL_SALVADOR, SysLoc.URUGUAY, SysLoc.VENEZUELA, SysLoc.JAPAN ];
        if (COUNTRIES.indexOf(sysLocCd) == -1) {
          FormManager.setValue('dunsNo', data.dunsNo);
        } else {
          var dunsNo = FormManager.getActualValue('dunsNo');

          if (dunsNo == '' && data.dunsNo != '') {
            FormManager.setValue('dunsNo', data.dunsNo);
          }

        }
      }
      if (showError) {
        if (covError) {
          errorMsg += '<br>Coverage data cannot be determined at this point.';
        }
        errorMsg += '<br>You can try to retrieve the values again later.';
        cmr.showAlert(errorMsg, 'Warning');
      }
    }
  } else {
    FormManager.setValue('covId', '');
    FormManager.setValue('bgId', '');
    FormManager.setValue('geoLocationCd', '');
    FormManager.setValue('dunsNo', '');
  }
  FormManager.setValue('covBgRetrievedInd', 'Y');
}

/**
 * Trigger Undo current changes on the main request
 */
function undoCurrentChanges() {
  cmr.showConfirm('doUndoChanges()', 'This will undo all current changes on this request. Changes to Address, Attachments, and Notify List data cannot be undone. Proceed?', 'Warning', null, null);
}

/**
 * Undo current changes on the main request
 */
function doUndoChanges() {
  document.forms['frmCMR'].submit();
}

function showValidationUrl(url, seqNo) {
  var reqId = FormManager.getActualValue('reqId');
  WindowMgr.open('VALIDATION', reqId + '-' + seqNo, url, null, 550, true);
}

var _moveOk = false;

function addMoveHandler() {
  window.addEventListener('scroll', moveYourActions);
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
      'top' : 33
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

function showChangeLog(requestId) {
  WindowMgr.open('CHANGELOG', requestId, 'reqchangelog?reqId=' + requestId, 1050, 570);

}

function checkDUNSWithDnB() {
  var issuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  var dunsNo = FormManager.getActualValue('dunsNo');
  if (dunsNo == '') {
    cmr.showAlert('DUNS No. on the request must be specified to perform the check.');
    return;
  }
  WindowMgr.open('COMPDET', 'DNB' + dunsNo, 'company_details?viewOnly=Y&issuingCountry=' + issuingCntry + '&dunsNo=' + dunsNo, null, 550);
}

function doCheckDUNSWithDnB() {
  var dunsNo = FormManager.getActualValue('dunsNo');
  var result = CmrServices.checkDnB(dunsNo);
  if (!result.success || !result.data || !result.data.results) {
    cmr.hideProgress();
    cmr.showAlert('D&B check cannot performed at this time. Please try again in a few minutes.');
    return;
  }
  if (result.data.returnedMatches == 0) {
    cmr.hideProgress();
    cmr.showAlert('D&B did not find a match for any record with DUNS No. ' + dunsNo);
    return;
  }
  var record = result.data.results[0];
  var elem = null;
  for ( var prop in record) {
    if (record.hasOwnProperty(prop)) {
      elem = dojo.byId('dnb_' + prop);
      if (elem) {
        elem.innerHTML = record[prop] == null ? '&nbsp;' : record[prop];
      }
    }
  }
  var orgIds = record.organizationIds;
  if (orgIds && orgIds.length) {
    var ids = '';
    for (var i = 0; i < orgIds.length; i++) {
      ids += ids != '' ? '<br>' : '';
      ids += orgIds[i];
    }
    dojo.byId('dnb_organizationId').innerHTML = ids.trim();
  }
  console.log(record);
  cmr.hideProgress();
  cmr.showModal('dnbCheckModal');
}
function dnbCheckModal_onLoad() {
  console.log('execute on load..');
}

/**
 * Generic method to open CI Supportal with most values
 */
function openCISupportal() {
  var params = {};
  var error = dojo.byId('cmr-error-box-msg') ? dojo.byId('cmr-error-box-msg').innerHTML : null;
  if (error) {
    params.issueType = 'A';
    params.errorMessage = error;
    error = error.toLowerCase();
    if (error.includes('an unexpected error has occurred')) {
      params.appIssue = 'S';
    } else if (error.includes('dpl check cannot be done')) {
      params.appIssue = 'D';
    } else if (error.includes('error retrieving current values')) {
      params.appIssue = 'I';
    } else if (error.includes('error occurred while trying to import the current values')) {
      params.appIssue = 'I';
    } else {
      params.appIssue = 'O';
    }
  }
  if (typeof (_pagemodel) != 'undefined') {
    params.requestId = _pagemodel.reqId;
    params.cmrNo = _pagemodel.cmrNo;
    params.country = _pagemodel.cmrIssuingCntry;
  }

  try {
    console.log('checking current error');
    dojo.xhrGet({
      url : cmr.CONTEXT_ROOT + '/currenterror.json',
      handleAs : 'json',
      method : 'GET',
      content : params,
      timeout : 50000,
      sync : true,
      load : function(data, ioargs) {
        if (data) {
          params.stackTrace = data.errors;
        }
      },
      error : function(error, ioargs) {
      }
    });
  } catch (e) {
    console.log('error in getting current exception');
  }
  CISupportal.open(params);
  cmr.hideNode('supportal');
}

function openNPSFeedback(url) {
  var reqId = FormManager.getActualValue('reqId');
  WindowMgr.open('FEEDBACK', reqId, url, 900, 700, true);
}

/**
 * Verify company
 */
function verifyCompany() {
  cmr.showConfirm('doVerifyCompany()', 'Company information will be set as verified. Proceed?', 'Warning', null, null);
}

/**
 * set the company as verified
 */
function doVerifyCompany() {
  FormManager.setValue('compVerifiedIndc', 'Y');
  FormManager.setValue('compInfoSrc', 'Manually Verified');
  FormManager.doAction('frmCMR', 'VERIFY_COMPANY', true);
}
/**
 * Verify Scenario
 */
function verifyScenario() {
  cmr.showConfirm('doVerifyScenario()', 'Scenario information will be set as verified. Proceed?', 'Warning', null, null);
}

/**
 * set the Scenario as verified
 */
function doVerifyScenario() {
  FormManager.setValue('scenarioVerifiedIndc', 'Y');
  FormManager.doAction('frmCMR', 'VERIFY_SCENARIO', true);
}

/**
 * Shows dnb matches for automation
 */
function dnbAutoChk() {
  var requestId = _pagemodel.reqId;
  var reqType = _pagemodel.reqType;
  var reqStatus = FormManager.getActualValue('reqStatus');
  var matchIndc = FormManager.getActualValue('matchIndc');
  var matchOverrideIndc = FormManager.getActualValue('matchOverrideIndc');
  var findDnbResult = FormManager.getActualValue('findDnbResult');
  var userRole = FormManager.getActualValue('userRole');
  if (requestId > 0 && reqType == 'C' && reqStatus == 'DRA' && matchIndc == 'D' && !matchOverrideIndc && findDnbResult != 'Rejected' && findDnbResult != 'Accepted' && userRole != 'Viewer') {
    cmr.showModal('DnbAutoCheckModal');
  }
}

/**
 * Onload function for the dnb matches modal
 */
function DnbAutoCheckModal_onLoad() {
}

function DnbAutoCheckModal_onClose() {
}

/**
 * Override Dnb Matches
 */
function overrideDnBMatch() {
  cmr.showConfirm('doOverrideDnBMatch()', 'Dnb Matches will be overriden. Proceed?', 'Warning', null, null);
}

/**
 * set the match override indc
 */
function doOverrideDnBMatch() {
  FormManager.setValue('matchOverrideIndc', 'Y');
  FormManager.setValue('findDnbResult', 'Rejected');
  FormManager.doAction('frmCMR', 'OVERRIDE_DNB', true);
}

function autoDnbImportActFormatter(value, rowIndex) {
  var rowData = this.grid.getItem(rowIndex);
  if (rowData) {
    return '<input type="button" class="cmr-grid-btn-h" style="font-size:11px" value="Import" onClick="autoDnbImportMatch(\'' + rowData.autoDnbDunsNo + '\', \'' + rowData.itemNo + '\')">';
  } else {
    return '';
  }
}

function autoDnbImportConfidenceFormatter(value, rowIndex) {
  var rowData = this.grid.getItem(rowIndex);
  if (!value || value.indexOf('=') < 0) {
    return '';
  }
  var confidence = value.substring(value.indexOf('=') + 1).trim();
  if (confidence == 10) {
    return '<span style="color:green; font-weight:bold; font-size:11px;" title="Confidence Code = 10">VERY HIGH</span>';
  } else if (confidence == 9) {
    return '<span style="color:green; font-size:11px;" title="Confidence Code = 9">HIGH</span>';
  } else {
    return '<span style="color:black; font-size:11px;" title="Confidence Code = ' + confidence + '">GOOD</span>';
  }
}
function autoDnbImportMatch(autoDnbDunsNo, itemNo) {
  cmr.showProgress('Importing record into request..');
  dojo.xhrGet({
    url : cmr.CONTEXT_ROOT + '/request/dnb/matchlist/importrecord.json',
    handleAs : 'json',
    method : 'GET',
    content : {
      'autoDnbDunsNo' : autoDnbDunsNo,
      'itemNo' : itemNo
    },
    timeout : 50000,
    sync : false,
    load : function(data, ioargs) {
      cmr.hideProgress();
      console.log(data);
      if (!data.success) {
        cmr.showAlert(data.error, 'Error');
      } else {
        importDnb(data);
        CmrGrid.refresh('dnbMachesGrid', cmr.CONTEXT_ROOT + '/request/dnb/matchlist.json');
      }
    },
    error : function(error, ioargs) {
      console.log(error);
      cmr.hideProgress();
      cmr.showAlert('An error occurred while importing the record. Please contact your system administrator', 'Error');
    }
  });
}
function hideModaldnb_Window() {
  cmr.hideModal('DnbAutoCheckModal');
}

function matchDetailsFormatterUI(value, rowIndex) {
  var str = value.replace(/\n/gim, '<br>');
  if (str.indexOf('ISIC = ') >= 0) {
    str = str.substring(str.indexOf('=') + 1).trim();
  }
  return str;
}

function setRejSupplInfoFields(value) {
  switch (value) {
  case "DUPC":
    cmr.showNode('rejInfo1Div');
    cmr.showNode('rejInfo2Div');
    dojo.byId('rejInfo1Label').innerText = "CMR No.";
    dojo.byId('rejInfo2Label').innerText = "Sold-to KUNNR";
    break;
  case "MDOC":
    cmr.showNode('rejInfo1Div');
    cmr.hideNode('rejInfo2Div');
    dojo.byId('rejInfo1Label').innerText = "Missing Document";
    break;
  case "MAPP":
    cmr.showNode('rejInfo1Div');
    cmr.showNode('rejInfo2Div');
    dojo.byId('rejInfo1Label').innerText = "Approval Type";
    dojo.byId('rejInfo2Label').innerText = "Approver";
    break;
  case "DUPR":
    cmr.showNode('rejInfo1Div');
    cmr.hideNode('rejInfo2Div');
    dojo.byId('rejInfo1Label').innerText = "Other Request Id";
    break;
  case "TYPR":
    cmr.showNode('rejInfo1Div');
    cmr.hideNode('rejInfo2Div');
    dojo.byId('rejInfo1Label').innerText = "Correct Type";
    break;
  default:
    cmr.hideNode('rejInfo1Div');
    cmr.hideNode('rejInfo2Div');
    break;
  }
}
