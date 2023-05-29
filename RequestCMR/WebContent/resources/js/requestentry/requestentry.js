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
var NORDX = [ '846', '806', '702', '678' ];
var ROW = [ '706' ];
var comp_proof_INAUSG = false;
var flag = false;
var addrMatchResultForNZCreate;

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

  // prevent from overwriting the DB REQ_STATUS
  // if another tab is open with different UI REQ_STATUS
  if (!isReqStatusEqualBetweenUIandDB()) {
    cmr.showAlert("Unable to execute the action. Request Status mismatch from database." +
      "<br><br>Please reload the page.");

    return;
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
        OK: 'Ok',
        CANCEL: 'Cancel'
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
    var findDnbResult = FormManager.getActualValue('findDnbResult');
    var reqType = FormManager.getActualValue('reqType');
    var vatInd = FormManager.getActualValue('vatInd');
    var custGrp = FormManager.getActualValue('custGrp');
    var reqId = FormManager.getActualValue('reqId');
    var crossScenTyp = [ 'CROSS', 'LUCRO', 'EECRO', 'LTCRO', 'LVCRO', 'FOCRO', 'GLCRO', 'ISCRO' ];
    if (custGrp == null || custGrp == '') {
      custGrp = getCustGrp();
    }
    var oldVat = cmr.query('GET.OLD.VAT.VALUE', {
      REQ_ID: reqId
    });
    var oldVatValue = oldVat.ret1 != undefined ? oldVat.ret1 : '';
    var personalInfoPrivacyNoticeCntryList = [ '858', '834', '818', '856', '778', '749', '643', '852', '744', '615', '652', '616', '796', '641', '738', '736', '766', '760' ];
    if (_pagemodel.approvalResult == 'Rejected') {
      cmr.showAlert('The request\'s approvals have been rejected. Please re-submit or override the rejected approvals. ');
    } else if (FormManager.validate('frmCMR') && checkIfDataOrAddressFieldsUpdated(frmCMR)) {
      cmr.showAlert('Request cannot be submitted for update because No data/address changes made on request. ');
    } else if (FormManager.validate('frmCMR') && !comp_proof_INAUSG) {
      if ((GEOHandler.GROUP1.includes(FormManager.getActualValue('cmrIssuingCntry')) || NORDX.includes(FormManager.getActualValue('cmrIssuingCntry')) || ROW.includes(FormManager
          .getActualValue('cmrIssuingCntry')))
          && (vatInd == 'N') && (!crossScenTyp.includes(custGrp)) && ((oldVatValue == '' && reqType == 'U') || (reqType == 'C'))) {
        findVatInd();
      } else if (checkForConfirmationAttachments()) {
        showDocTypeConfirmDialog();
      } else if (cmrCntry == SysLoc.INDIA) {
        // Cmr-2340- For India Dnb import
        if (findDnbResult == 'Accepted') {
          if (checkIfDnBCheckReqForIndia()) {
            matchDnBForIndia();
          } else {
            // cmr.showModal('addressVerificationModal');
            verifyGlcChangeIN();
          }
        } else if (checkIfFinalDnBCheckRequired() && reqType == 'C') {
          matchDnBForAutomationCountries();
        } else {
          executeBeforeSubmit();
        }
      } else if (cmrCntry == SysLoc.AUSTRALIA && reqType == 'U') {
        // Cmr-3176- Dnb match
        var checkCompProof = checkForCompanyProofAttachment();
        if (checkIfFinalDnBCheckRequired() && reqType == 'U' && checkCompProof) {
          // CustNm check and Addresses Dnb Check
          matchCustNmAUUpdate();
        } else {
          // cmr.showModal('addressVerificationModal');
          showAddressVerificationModal();
        }
      } else if (cmrCntry == SysLoc.SINGAPORE || cmrCntry == SysLoc.AUSTRALIA) {
        // Cmr-1701 for isic Dnb match acc to scenario
        if (findDnbResult == 'Accepted') {
          if (checkIfDnBCheckReqForAUSG()) {
            matchDnbForAUSG();
          } else {
            // cmr.showModal('addressVerificationModal');
            showAddressVerificationModal();
          }
        } else if (checkIfFinalDnBCheckRequired() && reqType == 'C') {
          matchDnBForAutomationCountries();
        } else {
          if (cmrCntry == SysLoc.SINGAPORE) {
            executeBeforeSubmit();
          } else {
            // cmr.showModal('addressVerificationModal');
            showAddressVerificationModal();
          }
        }
      } else if (cmrCntry == SysLoc.NEW_ZEALAND) {
        // CREATCMR-8430: do DNB check for NZ update
        var checkCompProof = checkForCompanyProofAttachment();
        if (checkIfFinalDnBCheckRequired() && checkCompProof) {
          if (reqType == 'C') {
            matchDnBForNZ();
          } else {
            matchDnBForNZUpdate();
          }
        } else {
          if (reqType == 'C') {
            var custSubGrp = FormManager.getActualValue('custSubGrp');
            if (custSubGrp == 'NRMLC' || custSubGrp == 'AQSTN') {
              cmr.showProgress('Checking request data..');
              checkRetrievedForNZ();
            } else {
              showAddressVerificationModal();
            }
          } else {
            showAddressVerificationModal();
          }
        }
      } else if (checkIfFinalDnBCheckRequired()) {
        matchDnBForAutomationCountries();
      } else if (cmrCntry == '897' || cmrCntry == '649') {
        // CREATCMR-6074
        // addUpdateChecksExecution(frmCMR);
        if (cmrCntry == '897') {
          if (FormManager.getActualValue('isTaxTeamFlg') != 'true') {
            addUpdateChecksExecution(frmCMR);
          } else {
            // cmr.showModal('addressVerificationModal');
            showAddressVerificationModal();
          }
        } else {
          addUpdateChecksExecution(frmCMR);
        }
      } else {
        if (cmrCntry == '821' || cmrCntry == '755') {
          executeBeforeSubmit();
        } else {
          // cmr.hideNode('personalInformationDiv');
          // if there are no errors, show the Address Verification modal window
          // cmr.showModal('addressVerificationModal');
          showAddressVerificationModal();
        }
      }
    } else if (comp_proof_INAUSG && cmrCntry == '744') {
      if (checkIfDnBCheckReqForIndia() && findDnbResult == 'Accepted') {
        matchDnBForIndia();
      } else {
        // if there are no errors, show the Address Verification modal window
        // cmr.showModal('addressVerificationModal');
        verifyGlcChangeIN();
      }
    } else if (comp_proof_INAUSG && cmrCntry == SysLoc.AUSTRALIA && reqType == 'U') {
      // Cmr-3176- Dnb match
      var checkCompProof = checkForCompanyProofAttachment();
      if (checkIfFinalDnBCheckRequired() && reqType == 'U' && checkCompProof) {
        matchCustNmAUUpdate();
      } else {
        // cmr.showModal('addressVerificationModal');
        showAddressVerificationModal();
      }
    } else if (comp_proof_INAUSG && (cmrCntry == SysLoc.SINGAPORE || cmrCntry == SysLoc.AUSTRALIA)) {
      if (checkIfDnBCheckReqForAUSG() && findDnbResult == 'Accepted') {
        matchDnbForAUSG();
      } else {
        // if there are no errors, show the Address Verification modal window
        // cmr.showModal('addressVerificationModal');
        showAddressVerificationModal();
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
          OK: 'Yes',
          CANCEL: 'No'
        });
      } else {
        // if the customer names have changed since last save and it is being
        // asked for creation,
        // force a save to reset DPL check
        var nm1 = _pagemodel.mainCustNm1 == null ? '' : _pagemodel.mainCustNm1;
        var nm2 = _pagemodel.mainCustNm2 == null ? '' : _pagemodel.mainCustNm2;
        if (nm1 != FormManager.getActualValue('mainCustNm1') || nm2 != FormManager.getActualValue('mainCustNm2')) {
          cmr.showAlert("The Customer Name/s have changed. The record has to be saved first. Please select Save from the actions. A DPL check will also be necessary after saving. ");
        } else if (FormManager.getActualValue('reqType') == 'U' && cmrCntry == '755') {
          executeBeforeSubmit(action);
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

function verifyGlcChangeIN() {
  var cmrCntry = FormManager.getActualValue('cmrIssuingCntry');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (cmrCntry == SysLoc.INDIA && (custSubGrp == 'NRMLC' || custSubGrp == 'AQSTN')) {
    var hasRetrievedValue = FormManager.getActualValue('covBgRetrievedInd') == 'Y';
    if (!hasRetrievedValue) {
      cmr.showAlert('Request cannot be submitted because retrieve value is required action . ');
    } else {
      var oldGlc = FormManager.getActualValue('geoLocationCd');
      var oldCluster = FormManager.getActualValue('apCustClusterId');
      console.log("Checking the GLC match... retrieve value again...")
      var data = CmrServices.getAll('reqentry');

      // cmr.hideProgress();
      if (data) {
        console.log(data);
        if (data.error && data.error == 'Y') {
          cmr.showAlert('An error was encountered when retrieving the values.\nPlease contact your system administrator.', 'Create CMR');
        } else {
          if (data.glcError) {
            // errorMsg += (showError ? ', ' : '') + 'GEO Location Code';
          } else {
            var newGlc = data.glcCode
            var qParams = {
              TXT : newGlc,
            };
            var newCluster = cmr.query('GET_CLUSTER_BY_GLC', qParams);
            if (newCluster != null && (oldGlc != newGlc || oldCluster != newCluster.ret1)) {
              retrieveInterfaceValues();
              FormManager.setValue('apCustClusterId', newCluster.ret1);
              FormManager.readOnly('apCustClusterId');
              cmr.showAlert('The GLC and Cluster has been overwritten to ' + newGlc + ' and ' + newCluster.ret1 + ' respectively' + '. Do you want to proceed with this request?',
                  ' GLC and Cluster value overwritten', 'showAddressVerificationModal()');
            } else {
              showAddressVerificationModal();
            }
          }
        }
      } else {
        showAddressVerificationModal();
      }
    }
  } else {
    showAddressVerificationModal();
  }
}

function getCustGrp() {
  var custGrp = null;
  var issueCntry = getIssuingCntry();
  var zs01LandCntry = getZS01LandCntry();

  if (issueCntry == zs01LandCntry) {
    custGrp = 'LOCAL'
  } else {
    custGrp = 'CROSS'
  }
  return custGrp;
}

function getZS01LandCntry() {
  var reqId = FormManager.getActualValue('reqId');
  if (reqId != null) {
    reqParam = {
      REQ_ID : reqId,
    };
  }
  var results = cmr.query('ADDR.GET.ZS01LANDCNTRY.BY_REQID', reqParam);
  var landCntry = results.ret1 != undefined ? results.ret1 : '';
  return landCntry;
}

function getIssuingCntry() {
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  reqParam1 = {
    SYS_LOC_CD : cntry,
  };
  var results1 = cmr.query('GET.ISSUING.CNTRY.NAME', reqParam1);
  var issueCntry = results1.ret1 != undefined ? results1.ret1 : '';
  return issueCntry;
}

function findVatInd() {
  var issuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  var reqId = FormManager.getActualValue('reqId');
  var vatInd = FormManager.getActualValue('vatInd');
  var custGrp = FormManager.getActualValue('custGrp');
  if (vatInd == 'N' && custGrp != 'CROSS') {
    console.log("Test");
    cmr
        .showConfirm(
            'showAddressVerificationModal()',
            '<div align="center"><strong><i><u><b><p style="font-size:25px"> Warning Message</p></u><br><br><p style="font-size:15px">Please note, if you choose not to provide the company’s VAT ID, IBM will not be able to include VAT ID in the customer address section. As a consequence the IBM invoice may not be eligible to recover the VAT charged to the client which can cause a delay on payment in countries that is required. However, at any moment business can submit VAT ID update whenever VAT ID is collected/needed.</p><br><br> <p style="font-size:17px">Would you like  to proceed?</p></i></strong></div>',
            'Warning', null, {
              OK : 'YES',
              CANCEL : 'NO'
            });
  }
}

/**
 * Shows the prompt for the Address Verification disclaimer
 */
// CREATCMR-7477
function showAddressVerificationModal() {
  var personalInfoPrivacyNoticeCntryList = [ '858', '834', '818', '856', '778', '749', '643', '852', '744', '615', '652', '616', '796', '641', '738', '736', '766', '760' ];
  var cmrCntry = FormManager.getActualValue('cmrIssuingCntry');
  if (personalInfoPrivacyNoticeCntryList.includes(cmrCntry)) {
    cmr.showNode('personalInformationDiv');
    cmr.showModal('addressVerificationModal');
  } else {
    cmr.hideNode('personalInformationDiv');
    cmr.showModal('addressVerificationModal');
  }
}

/**
 * Accept the address verification disclaimer
 */
function doAcceptAddressVerification() {

  var personalInfoPrivacyNoticeCntryList = [ '858', '834', '818', '856', '778', '749', '643', '852', '744', '615', '652', '616', '796', '641', '738', '736', '766', '760' ];
  var cmrCntry = FormManager.getActualValue('cmrIssuingCntry');
  if (!dojo.byId('addrVerAgree').checked && personalInfoPrivacyNoticeCntryList.includes(cmrCntry)) {
    cmr.showAlert('You must agree to the Address Verification and PI - Business Contact Rules to proceed.');
    return;
  } else if (!dojo.byId('addrVerAgree').checked) {
    cmr.showAlert('You must agree to the Address Verification Rules to proceed.');
    return;
  }

  if (duplicateCMRMatchesCheck()) {
    var dupCmrReason = dojo.byId('dupCmrRsn').value;
    if (dupCmrReason && dupCmrReason != null && dupCmrReason.trim().length > 0) {
      FormManager.setValue('dupCmrReason', dupCmrReason.trim());
    } else {
      cmr.showAlert('You must provide a Duplicate CMR Override Reason to proceed.');
      return;
    }
  }

  cmr.hideModal('addressVerificationModal');
  doYourAction();
}

/**
 * Hides the address verification modal
 */
function doCancelAddressVerification() {
  cmr.hideModal('addressVerificationModal');
  // CREATCMR-8430: for NZ DNB overriding, refresh the page if use cancel the address confirm
  var loc = FormManager.getActualValue('cmrIssuingCntry');
  var usSicmen = FormManager.getActualValue('usSicmen');
  if (loc == SysLoc.NEW_ZEALAND && usSicmen && usSicmen=='DNBO') {
	  console.log("refresh this page...")
      window.location.reload();
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
  cmr.hideNode('bpIBMDirectCMR');
  cmr.hideNode('bpIBMDirectCMRWR');
  dojo.byId('newReqStatus').innerHTML = details.ret2;
  dojo.byId('statusChgCmt').value = '';
  dojo.byId('statusChgCmt_charind').innerHTML = 1000;
  if (action == YourActions.Reject) {
    cmr.showNode('rejectReasonBlock');
  } else if (action == YourActions.Send_for_Processing) {
    dojo.byId('sendToProcessingCenter').innerHTML = details.ret3;
    cmr.showNode('sendToBlock');
  } else {
    var isscntry = FormManager.getActualValue('cmrIssuingCntry');
    if (isscntry == '897') {
      var reqId = FormManager.getActualValue('reqId');
      var qParams = {
        REQ_ID : reqId
      };
      var resultSource = cmr.query('bpsourceflag', qParams);
      if (resultSource.ret1 == 'CreateCMR-BP' && resultSource.ret2 == 'E') {
        var result = cmr.query('bpibmdirectcmr', qParams);
        if (result.ret1 == null || result.ret1 == '') {
          cmr.showNode('bpIBMDirectCMRWR');
        } else {
          cmr.showNode('bpIBMDirectCMR');
          dojo.byId('childIbmDrCMR').innerHTML = result.ret1;
        }
      }
    }
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
  } else if (action == YourActions.Reject && rejReason == 'DUPC') {
    if (dojo.byId('rejInfo1Label').innerText == "CMR No.") {
      var rej = FormManager.getActualValue('rejectReason');
      var isscntry = FormManager.getActualValue('cmrIssuingCntry');
      mandt = FormManager.getActualValue('mandt');
      var ktokd = "ZS01";
      var rejInfo1 = FormManager.getActualValue('rejSupplInfo1');
      if (rejInfo1 != '') {
        var qParams = {
          ZZKV_CUSNO : rejInfo1,
          KATR6 : isscntry,
          MANDT : mandt
        };
        var result = cmr.query('VALIDATECMR', qParams);
        var cmrExist = result.ret1;
        if (cmrExist != undefined) {
          var qParams1 = {
            ZZKV_CUSNO : rejInfo1,
            MANDT : mandt,
            KATR6 : isscntry
          };
          var kunnr = cmr.query('GET.KUNNR.SOLDTO', qParams1);
          var kunnr1 = kunnr.ret1;
          if (kunnr1 != null || kunnr1 != undefined) {
            dojo.byId('rejSupplInfo2').value = kunnr1;
            console.log(dojo.byId('rejSupplInfo2').value);
          } else {
            cmr.showAlert('No Sold-To Kunnr found for ' + rejInfo1 + ".");
            return;
          }
          var rejInfo2 = FormManager.getActualValue('rejSupplInfo2');
          var rejField = '<input type="hidden" name="rejectReason" value="' + rej + '">';
          rejField += '<input type="hidden" name="rejSupplInfo1" value="' + rejInfo1 + '">';
          rejField += '<input type="hidden" name="rejSupplInfo2" value="' + rejInfo2 + '">';

          if (SysLoc.ISRAEL == FormManager.getActualValue('cmrIssuingCntry')) {
            rejField += '<input type="hidden" name="statusChgCmt" value="' + cmt + '">';
          }
          dojo.place(rejField, document.forms['frmCMR'], 'last');
        } else {
          cmr.showAlert('The CMR Number is not correct');
          return;
        }
      } else {
        cmr.showAlert('Please specify ' + dojo.byId('rejInfo1Label').innerText + ".");
        return;
      }
    }
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

    if (SysLoc.ISRAEL == FormManager.getActualValue('cmrIssuingCntry')) {
      rejField += '<input type="hidden" name="statusChgCmt" value="' + cmt + '">';
    }
    dojo.place(rejField, document.forms['frmCMR'], 'last');
  } else {
    var isscntry = FormManager.getActualValue('cmrIssuingCntry');
    if (isscntry == '897') {
      var reqId = FormManager.getActualValue('reqId');
      var qParams = {
        REQ_ID : reqId
      };
      var resultSource = cmr.query('bpsourceflag', qParams);
      if (resultSource.ret1 == 'CreateCMR-BP' && resultSource.ret2 == 'E') {
        var result = cmr.query('bpibmdirectcmr', qParams);
        if (result.ret1 == null || result.ret1 == '') {
          var ibmDrCMR = FormManager.getActualValue('cmrNo2');
          if (ibmDrCMR == '' || ibmDrCMR.length < 7 || ibmDrCMR.length > 7) {
            cmr.showAlert('Please input correct IBM Direct CMR.');
            return;
          }
          var cmt = FormManager.getActualValue('statusChgCmt');
          FormManager.setValue('statusChgCmt', 'IBMDIRECT_CMR' + ibmDrCMR + cmt);
        }
      }
    }
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

function commentImgFormatter(value, rowIndex) {
  if (!value) {
    return '&nbsp';
  }
  if (value.indexOf('@') > 0) {
    if (value.indexOf('ibm.com') > 0) {
      return '<img title="' + value + '" src="https://w3-unifiedprofile-api.dal1a.cirrus.ibm.com/v3/image/' + value + '" class="cmt-img" onerror="this.onerror=null; this.src=\'' + cmr.CONTEXT_ROOT
          + '/resources/images/person.jpg\'">';
    } else {
      return '<img title="' + value + '" src="' + cmr.CONTEXT_ROOT + '/resources/images/person.jpg" class="cmt-img">';
    }
  } else {
    return '<img title="' + value + '" src="' + cmr.CONTEXT_ROOT + '/resources/images/CreateCMRLogo.png" class="cmt-img">';
  }
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
    // FormManager.disable('dplCheckBtn');
    // dojo.removeClass(dojo.byId('dplCheckBtn'), 'ibm-btn-cancel-pri');
    // dojo.addClass(dojo.byId('dplCheckBtn'), 'ibm-btn-cancel-disabled');
  }
}

var _inacHandler = null;
var _typeHandler = null;
var _countyHandler = null;
var _enterCMRHandler = null;
var _templateHandler = null;
var defaultLandCntry = null;
var _rejSupplInfoHandler = null;
var _dnbSearchHandler = null;
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
      if ((cmrCntry != '616' && cmrCntry != '641') && (value && dojo.string.trim(value) == 'I')) {
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
      } else if (dojo.byId('func')) {
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
  if (duplicateCMRMatchesCheck()) {
    cmr.showNode('dupCMRReasonDiv');
    var reason = FormManager.getActualValue('dupCmrReason');
    if (reason != null && reason != '') {
      FormManager.setValue('dupCmrRsn', reason);
    }
    duplicateCMRMatchesNotif();
  } else {
    cmr.hideNode('dupCMRReasonDiv');
  }
  // check if dnbManadatory
  handleRequiredDnBSearch();

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
  if (FormManager.getField('MAIN_GENERAL_TAB')) {
    cmr.hideProgress();
  }

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

        // CREATCMR-7884:reset cluster after retrieve action for NZ
        var cmrCntry = FormManager.getActualValue('cmrIssuingCntry');
        var reqType = FormManager.getActualValue('reqType');
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        if (cmrCntry == SysLoc.NEW_ZEALAND && reqType == 'C' && (custSubGrp == 'NRMLC' || custSubGrp == 'AQSTN')) {
          setClusterIDAfterRetrieveAction(data.glcCode);
        }
        if (cmrCntry == SysLoc.CHINA && reqType == 'C' && (custSubGrp == 'NRMLC' || custSubGrp == 'AQSTN' || custSubGrp == 'ECOSY' || custSubGrp == 'EMBSA')) {
          setClusterIDAfterRetrieveAction4CN(custSubGrp, data.glcCode);
        }
      }
      if (data.dunsError) {
        // errorMsg += (showError ? ', ' : '') + 'DUNS No.';
        // showError = true;
      } else {
        // var sysLocCd = FormManager.getActualValue('cmrIssuingCntry');
        // var COUNTRIES = [ SysLoc.BRAZIL, SysLoc.MEXICO, SysLoc.ARGENTINA,
        // SysLoc.BOLIVIA, SysLoc.CHILE, SysLoc.COLOMBIA, SysLoc.COSTA_RICA,
        // SysLoc.DOMINICAN_REPUBLIC, SysLoc.ECUADOR,
        // SysLoc.GUATEMALA, SysLoc.HONDURAS, SysLoc.NICARAGUA, SysLoc.PANAMA,
        // SysLoc.PARAGUAY, SysLoc.PERU, SysLoc.EL_SALVADOR, SysLoc.URUGUAY,
        // SysLoc.VENEZUELA, SysLoc.JAPAN ];
        // if (COUNTRIES.indexOf(sysLocCd) == -1) {
        // FormManager.setValue('dunsNo', data.dunsNo);
        // } else {
        var dunsNo = FormManager.getActualValue('dunsNo');

        if (dunsNo == '' && data.dunsNo != '') {
          FormManager.setValue('dunsNo', data.dunsNo);
        } else {
          if (dunsNo && data.dunsNo) {
            cmr.showAlert('DUNS ' + data.dunsNo + ' was retrieved but an existing DUNS ' + dunsNo + ' was found. The value was not overwritten.');
          }
        }

        // }
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
    // FormManager.setValue('dunsNo', '');
  }
  FormManager.setValue('covBgRetrievedInd', 'Y');
  var cmrCntry = FormManager.getActualValue('cmrIssuingCntry');
  if (cmrCntry == '744') {
    setClusterGlcCovIdMapNrmlc();
  }
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
    showDnBMatchModal();
  }
}

function duplicateCMRMatchesNotif() {
  if (duplicateCMRMatchesCheck()) {
    cmr
        .showAlert(
            'Existing CMR(s) were found with the similar information as the current request. Please check the CMR(s) provided in the request comments.<br><br>To proceed with duplicate CMR creation, please provide the override reason while sending the request for processing.<br><b>Note: </b>Duplicate CMR creation will trigger approvals',
            'Duplicate CMR(s) Found');
  }
}

function duplicateCMRMatchesCheck() {
  var requestId = _pagemodel.reqId;
  var reqType = _pagemodel.reqType;
  var reqStatus = FormManager.getActualValue('reqStatus');
  var matchIndc = FormManager.getActualValue('matchIndc');
  var findDnbResult = FormManager.getActualValue('findDnbResult');
  var userRole = FormManager.getActualValue('userRole');
  if (requestId > 0 && reqType == 'C' && reqStatus == 'DRA' && matchIndc == 'C' && userRole != 'Viewer') {
    return true;
  } else {
    return false;
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
  // FOR CN
  var cntry = FormManager.getActualValue('landCntry');
  var loc = FormManager.getActualValue('cmrIssuingCntry');
  var reqType = FormManager.getActualValue('reqType');
  if (cntry == 'CN' || loc == '641') {
    var reqType = FormManager.getActualValue('reqType');
    var custSubGroup = FormManager.getActualValue('custSubGrp');

    if (loc == '641' && (custSubGroup == 'CROSS' && reqType == 'C' || reqType == 'U' && cntry != 'CN')) {
      cmr
          .showConfirm(
              'doOverrideDnBMatch()',
              'This action will override the D&B Matching Process.<br> By overriding the D&B matching, you\'re obliged to provide either one of the following documentation as backup - client\'s official website, Secretary of State business registration proof, client\'s confirmation email and signed PO, attach it under the file content of <strong>Company Proof</strong>. Please note that the sources from Wikipedia, Linked In and social medias are not acceptable.<br>Proceed?',
              'Warning', null, null);
    } else {
      cmr
          .showConfirm(
              'doOverrideDnBMatch()',
              'This action will override the D&B Matching Process.<br> By overriding the D&B matching, you\'re obliged to provide either one of the following documentation as backup - client\'s official website, Secretary of State business registration proof, client\'s confirmation email and signed PO, attach it under the file content of <strong>Name and Address Change(China Specific)</strong>. Please note that the sources from Wikipedia, Linked In and social medias are not acceptable.<br>Proceed?',
              'Warning', null, null);
    }
  // CREATCMR-8430: do NZBN API check after override dnb
  } else if (loc == SysLoc.NEW_ZEALAND && reqType == 'C') {
    console.log(">>> for NZ, do NZBN API check after doOverrideDnBMatch >>>")
    cmr
        .showConfirm(
            'doNZBNAPIMatch()',
            'This action will override the D&B Matching Process.<br>Proceed?',
            'Warning', null, null);
  } else {
    cmr
        .showConfirm(
            'doOverrideDnBMatch()',
            'This action will override the D&B Matching Process.<br> By overriding the D&B matching, you\'re obliged to provide either one of the following documentation as backup - client\'s official website, Secretary of State business registration proof, client\'s confirmation email and signed PO, attach it under the file content of <strong>Company Proof</strong>. Please note that the sources from Wikipedia, Linked In and social medias are not acceptable.<br>Proceed?',
            'Warning', null, null);
  }
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
    var formattedString = '<input type="button" class="cmr-grid-btn-h" style="font-size:11px" value="Import" onClick="autoDnbImportMatch(\'' + rowData.autoDnbDunsNo + '\', \'' + rowData.itemNo
        + '\')">';
    var dunsNo = rowData.autoDnbDunsNo[0];
    if (dunsNo) {
      formattedString += '<input type="button" class="cmr-grid-btn" style="font-size:11px" value="View Details" onClick="openDNBDetailsPage(\'' + dunsNo.trim() + '\')">';
    }
    return formattedString;
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
    FormManager.readOnly('rejSupplInfo2');
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

/**
 * Method to check whether for a scenario dnb matching is allowed or not
 */
function isSkipDnbMatching() {
  var custGrp = FormManager.getActualValue('custGrp');
  var custSubGroup = FormManager.getActualValue('custSubGrp');
  var dnbPrimary = FormManager.getActualValue("dnbPrimary");
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var countryUse = FormManager.getActualValue("countryUse");
  var subRegionCd = countryUse != null && countryUse.length > 0 ? countryUse : cntry;
  if (SysLoc.INDIA == FormManager.getActualValue('cmrIssuingCntry')) {
    return false;
  }
  if (dnbPrimary == 'Y') {
    if (custGrp != null && custGrp != '' && custSubGrp != null && custSubGrp != '') {
      var qParams = {
        CNTRY : cntry,
        CUST_TYP : custGrp,
        CUST_SUB_TYP : custSubGroup,
        SUBREGION_CD : subRegionCd
      };
      var result = cmr.query("AUTO.SKIP_VERIFICATION_INDC", qParams);
      if (result.ret1 != null && result.ret1 != "") {
        if (result.ret1 == 'Y') {
          return true;
        } else if (result.ret1 == 'N') {
          return false;
        }
      } else {
        qParams.CUST_SUB_TYP = "*";
        result = cmr.query("AUTO.SKIP_VERIFICATION_INDC", qParams);
        if (result.ret1 != null && result.ret1 != '') {
          if (result.ret1 == 'Y') {
            return true;
          } else if (result.ret1 == 'N') {
            return false;
          }
        } else {
          qParams.CUST_TYP = "*";
          result = cmr.query("AUTO.SKIP_VERIFICATION_INDC", qParams);
          if (result.ret1 != null && result.ret1 != '') {
            if (result.ret1 == 'Y') {
              return true;
            } else if (result.ret1 == 'N') {
              return false;
            }
          }
        }
      }
      return false;
    } else
      return true;
  } else {
    return true;
  }
}

/**
 * universal handler to make dnb search mandatory or optional on scenario change
 */
function handleRequiredDnBSearch() {
  var reqId = FormManager.getActualValue('reqId');
  var reqType = FormManager.getActualValue('reqType');
  var reqStatus = FormManager.getActualValue('reqStatus');
  if (reqId != null && reqId != '' && reqType == 'C' && reqStatus == 'DRA' && _dnbSearchHandler == null) {
    _dnbSearchHandler = dojo.connect(FormManager.getField('custSubGrp'), 'onChange', function(value) {
      if (!isSkipDnbMatching() && FormManager.getActualValue('viewOnlyMode') != 'true') {
        cmr.showNode('dnbRequired');
        cmr.showNode('dnbRequiredIndc');
      } else {
        cmr.hideNode('dnbRequired');
        cmr.hideNode('dnbRequiredIndc');
      }
    });
  }

  if (_dnbSearchHandler && _dnbSearchHandler[0]) {
    _dnbSearchHandler[0].onChange();
  }
}

function checkIfFinalDnBCheckRequired() {
  var cmrCntry = FormManager.getActualValue('cmrIssuingCntry');
  var reqId = FormManager.getActualValue('reqId');
  var reqType = FormManager.getActualValue('reqType');
  var reqStatus = FormManager.getActualValue('reqStatus');
  var matchOverrideIndc = FormManager.getActualValue('matchOverrideIndc');
  var findDnbResult = FormManager.getActualValue('findDnbResult');
  var userRole = FormManager.getActualValue('userRole');
  var ifReprocessAllowed = FormManager.getActualValue('autoEngineIndc');
  if (cmrCntry == '834') {
    var ret = cmr.query('CHECK_DNB_MATCH_ATTACHMENT', {
      ID : reqId
    });
    if (ret && ret.ret1 && ret.ret1 != '') {
      return false;
    }
  }
  if (reqId > 0 && (reqType == 'C' || reqType == 'U') && reqStatus == 'DRA' && userRole == 'Requester' && (ifReprocessAllowed == 'R' || ifReprocessAllowed == 'P' || ifReprocessAllowed == 'B')
      && !isSkipDnbMatching() && matchOverrideIndc != 'Y') {
    // currently Enabled Only For US
    return true;
  }
  if (cmrCntry == '641') {
    if (reqId > 0 && reqType == 'U' && reqStatus == 'DRA' && userRole == 'Requester' && (ifReprocessAllowed == 'R' || ifReprocessAllowed == 'P' || ifReprocessAllowed == 'B')
        && matchOverrideIndc != 'Y') {
      // currently Enabled Only For CN
      return true;
    }
  }
  if (cmrCntry == '616') {
    if (reqId > 0 && reqType == 'U' && reqStatus == 'DRA' && userRole == 'Requester' && (ifReprocessAllowed == 'R' || ifReprocessAllowed == 'P' || ifReprocessAllowed == 'B')) {
      // currently Enabled Only For AU
      return true;
    }
  }
  return false;
}
function checkIfDnBCheckReqForIndia() {
  var reqId = FormManager.getActualValue('reqId');
  var reqType = FormManager.getActualValue('reqType');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var result = cmr.query('CHECK_DNB_MATCH_ATTACHMENT', {
    ID : reqId
  });
  if (reqType == 'C'
      && (custSubGrp == 'BLUMX' || custSubGrp == 'MKTPC' || custSubGrp == 'IGF' || custSubGrp == 'AQSTN' || custSubGrp == 'NRML' || custSubGrp == 'ESOSW' || custSubGrp == 'CROSS'
          || custSubGrp == 'NRMLC' || custSubGrp == 'KYNDR' || custSubGrp == 'ECOSY')) {
    if (result && result.ret1) {
      return false;
    } else {
      return true;
    }
  }
  return false;
}

function checkIfDnBCheckReqForAUSG() {
  var reqId = FormManager.getActualValue('reqId');
  var reqType = FormManager.getActualValue('reqType');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var result = cmr.query('CHECK_DNB_MATCH_ATTACHMENT', {
    ID : reqId
  });
  if (reqType == 'C'
      && cntry == '616'
      && (custSubGrp == 'AQSTN' || custSubGrp == 'BLUMX' || custSubGrp == 'ESOSW' || custSubGrp == 'IGF' || custSubGrp == 'MKTPC' || custSubGrp == 'NRML' || custSubGrp == 'SOFT'
          || custSubGrp == 'XAQST' || custSubGrp == 'CROSS' || custSubGrp == 'XIGF')) {
    if (result && result.ret1) {
      return false;
    } else {
      return true;
    }
  } else if (reqType == 'C'
      && cntry == '834'
      && (custSubGrp == 'ASLOM' || custSubGrp == 'AQSTN' || custSubGrp == 'BLUMX' || custSubGrp == 'BUSPR' || custSubGrp == 'MKTPC' || custSubGrp == 'NRML' || custSubGrp == 'SOFT'
          || custSubGrp == 'XAQST' || custSubGrp == 'XBLUM' || custSubGrp == 'XBUSP' || custSubGrp == 'XMKTP' || custSubGrp == 'CROSS' || custSubGrp == 'SPOFF')) {
    if (result && result.ret1) {
      return false;
    } else {
      return true;
    }
  }
  return false;
}

function matchDnBForAutomationCountries() {
  var reqId = FormManager.getActualValue('reqId');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var reqType = FormManager.getActualValue('reqType');
  console.log("Checking if the request matches D&B...");
  var nm1 = _pagemodel.mainCustNm1 == null ? '' : _pagemodel.mainCustNm1;
  var nm2 = _pagemodel.mainCustNm2 == null ? '' : _pagemodel.mainCustNm2;
  if (nm1 != FormManager.getActualValue('mainCustNm1') || nm2 != FormManager.getActualValue('mainCustNm2')) {
    cmr.showAlert("The Customer Name/s have changed. The record has to be saved first. Please select Save from the actions.");
    return;
  }
  cmr.showProgress('Checking request data with D&B...');
  dojo
      .xhrGet({
        url : cmr.CONTEXT_ROOT + '/request/dnb/checkMatch.json',
        handleAs : 'json',
        method : 'GET',
        content : {
          'reqId' : reqId
        },
        timeout : 50000,
        sync : false,
        load : function(data, ioargs) {
          cmr.hideProgress();
          console.log(data);
          if (data && data.success) {
            if (data.match) {
              // cmr.showModal('addressVerificationModal');
              showAddressVerificationModal();
            } else if (data.tradeStyleMatch) {
              cmr
                  .showConfirm(
                      'autoDnbImportMatch("' + data.dunsNo + '","0")',
                      'The customer name on the request is a tradestyle name. For CMR creation, legal name should be used. <strong>Tradestyle name can be placed on the address’s division line.</strong> Do you want to override the customer name on the request with <strong><u>'
                          + data.legalName + '</u></strong>?' + '?', 'Warning', 'doOverrideDnBMatch()', {
                        OK : 'Yes',
                        CANCEL : 'No'
                      });
            } else if (data.confidenceCd) {
              showDnBMatchModal();
            } else {
              if (cntry != SysLoc.INDIA && cntry != '641' && cntry != SysLoc.CANADA && cntry != '649') {
                cmr.showAlert('No matches found in dnb : Data Overidden.\nPlease attach company proof');
                FormManager.setValue('matchOverrideIndc', 'Y');
              }
              // Cmr-2755_India_no_match_found
              else if (cntry == SysLoc.INDIA
                  && (custSubGrp == 'BLUMX' || custSubGrp == 'MKTPC' || custSubGrp == 'IGF' || custSubGrp == 'AQSTN' || custSubGrp == 'NRML' || custSubGrp == 'ESOSW' || custSubGrp == 'CROSS')
                  && !flag) {
                cmr.showAlert('Please attach company proof as no matches found in dnb.');
                checkNoMatchingAttachmentValidator();
              } else if (cntry == SysLoc.CANADA) {
                cmr
                    .showAlert('This action will override the D&B Matching Process. By overriding the D&B matching, you\'re obliged to provide either one of the following documentation '
                        + 'as backup-client\'s official website, business registration proof, government issued documents, client\'s confirmation email and signed PO, attach it under the file content of Company Proof. '
                        + 'Please note that the sources from Wikipedia, Linked In and social medias are not acceptable.');
                FormManager.setValue('matchOverrideIndc', 'Y');
              } else if (data.cnCrossFlag) {
                cmr.showAlert('No matches found in dnb : Data Overidden.\nPlease attach company proof');
                FormManager.setValue('matchOverrideIndc', 'Y');
              } else {
                // cmr.showModal('addressVerificationModal');
                showAddressVerificationModal();
              }
            }
          } else {
            // continue
            console.log("An error occurred while matching dnb.");
            if (cntry == '641') {
              cmr.showConfirm('showAddressVerificationModal()', 'An error occurred while matching dnb. Do you want to proceed with this request?', 'Warning', null, {
                OK : 'Yes',
                CANCEL : 'No'
              });
            } else {
              cmr.showConfirm('showAddressVerificationModal()', 'An error occurred while matching dnb. Do you want to proceed with this request?', 'Warning', null, {
                OK : 'Yes',
                CANCEL : 'No'
              });
            }
          }
        },
        error : function(error, ioargs) {
        }
      });

}
function matchDnBForIndia() {
  var reqId = FormManager.getActualValue('reqId');
  var isicCd = FormManager.getActualValue('isicCd');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  console.log("Checking if the request matches D&B...");
  var nm1 = _pagemodel.mainCustNm1 == null ? '' : _pagemodel.mainCustNm1;
  var nm2 = _pagemodel.mainCustNm2 == null ? '' : _pagemodel.mainCustNm2;
  if (nm1 != FormManager.getActualValue('mainCustNm1') || nm2 != FormManager.getActualValue('mainCustNm2')) {
    cmr.showAlert("The Customer Name/s have changed. The record has to be saved first. Please select Save from the actions.");
    return;
  }
  cmr.showProgress('Checking request data with D&B...');
  dojo.xhrGet({
    url : cmr.CONTEXT_ROOT + '/request/dnb/checkMatch.json',
    handleAs : 'json',
    method : 'GET',
    content : {
      'reqId' : reqId,
      'isicCd' : isicCd
    },
    timeout : 50000,
    sync : true,
    load : function(data, ioargs) {
      cmr.hideProgress();
      console.log(data);
      console.log(data.success);
      console.log(data.match);
      console.log(data.isicMatch);
      console.log(data.validate);
      if (data && data.success) {
        if (data.match && data.isicMatch) {
          comp_proof_INAUSG = true;
          if (!data.validate) {
            checkDnBMatchingAttachmentValidator();
          }
          if (FormManager.validate('frmCMR')) {
            MessageMgr.clearMessages();
            doValidateRequest();
            // cmr.showModal('addressVerificationModal');
            verifyGlcChangeIN();
          } else {
            cmr.showAlert('The request contains errors. Please check the list of errors on the page.');
          }
        } else {

          if (data.match && !data.isicMatch && custSubGrp != 'IGF') {
            comp_proof_INAUSG = false;
            console.log("ISIC validation failed by Dnb.");
            cmr.showAlert("Please attach company proof as ISIC validation failed by Dnb.");
          } else if (data.match && !data.isicMatch && custSubGrp == 'IGF') {
            comp_proof_INAUSG = true;
            // cmr.showModal('addressVerificationModal');
            verifyGlcChangeIN();
          } else {
            comp_proof_INAUSG = false;
            console.log("Name/Address validation failed by dnb");
            cmr.showAlert("Please attach company proof as Name/Address validation failed by Dnb.");
          }
          if (!data.validate) {
            checkDnBMatchingAttachmentValidator();
          }
        }
      } else {
        // continue
        console.log("An error occurred while matching dnb.");
        cmr.showConfirm('verifyGlcChangeIN()', 'An error occurred while matching dnb. Do you want to proceed with this request?', 'Warning', null, {
          OK : 'Yes',
          CANCEL : 'No'
        });
      }
    },
    error : function(error, ioargs) {
    }
  });

}

function matchDnbForAUSG() {
  var reqId = FormManager.getActualValue('reqId');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var reqType = FormManager.getActualValue('reqType');
  console.log("Checking if the request matches D&B...");
  var nm1 = _pagemodel.mainCustNm1 == null ? '' : _pagemodel.mainCustNm1;
  var nm2 = _pagemodel.mainCustNm2 == null ? '' : _pagemodel.mainCustNm2;
  var isicCd = FormManager.getActualValue('isicCd');
  if (nm1 != FormManager.getActualValue('mainCustNm1') || nm2 != FormManager.getActualValue('mainCustNm2')) {
    cmr.showAlert("The Customer Name/s have changed. The record has to be saved first. Please select Save from the actions.");
    return;
  }
  cmr.showProgress('Checking request data with D&B...');
  dojo
      .xhrGet({
        url : cmr.CONTEXT_ROOT + '/request/dnb/checkMatch.json',
        handleAs : 'json',
        method : 'GET',
        content : {
          'reqId' : reqId,
          'isicCd' : isicCd
        },
        timeout : 50000,
        sync : false,
        load : function(data, ioargs) {
          cmr.hideProgress();
          console.log(data);
          console.log(data.success);
          console.log(data.match);
          console.log(data.isicMatch);
          console.log(data.orgIdMatch);
          console.log(data.validate);
          if (data && data.success) {
            if (data.match && data.isicMatch && data.orgIdMatch) {
              comp_proof_INAUSG = true;
              if (!data.validate) {
                checkDnBMatchingAttachmentValidator();
              }
              if (FormManager.validate('frmCMR')) {
                MessageMgr.clearMessages();
                doValidateRequest();
                // cmr.showModal('addressVerificationModal');
                showAddressVerificationModal();
              } else {
                cmr.showAlert('The request contains errors. Please check the list of errors on the page.');
              }
            } else if (data.match && !data.isicMatch && !(reqType == 'U' && cntry == SysLoc.AUSTRALIA)) {
              comp_proof_INAUSG = false;
              console.log("ISIC validation failed by Dnb.");
              cmr.showAlert("Please attach company proof as ISIC validation failed by Dnb.");
            } else if (data.match && !data.orgIdMatch && cntry == '834') {
              comp_proof_INAUSG = false;
              console.log("UEN validation failed by Dnb.");
              cmr.showAlert("Please attach company proof as UEN validation failed by Dnb.");
            } else if (data.tradeStyleMatch && data.isicMatch) {
              cmr
                  .showConfirm(
                      'autoDnbImportMatch("' + data.dunsNo + '","0")',
                      'The customer name on the request is a tradestyle name. For CMR creation, legal name should be used. <strong>Tradestyle name can be placed on the address’s division line.</strong> Do you want to override the customer name on the request with <strong><u>'
                          + data.legalName + '</u></strong>?' + '?', 'Warning', 'doOverrideDnBMatch()', {
                        OK : 'Yes',
                        CANCEL : 'No'
                      });
            } else if (data.confidenceCd) {
              showDnBMatchModal();
            } else {
              comp_proof_INAUSG = false;
              console.log("Name/Address validation failed by dnb");
              cmr.showAlert("Please attach company proof as Name/Address validation failed by Dnb.");
            }
            if (!data.validate) {
              checkDnBMatchingAttachmentValidator();
            }
          } else {
            // continue
            console.log("An error occurred while matching dnb.");
            cmr.showConfirm('showAddressVerificationModal()', 'An error occurred while matching dnb. Do you want to proceed with this request?', 'Warning', null, {
              OK : 'Yes',
              CANCEL : 'No'
            });
          }
        },
        error : function(error, ioargs) {
        }
      });
}

function matchCustNmAUUpdate() {
  // Get Fields
  var reqId = FormManager.getActualValue('reqId');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var reqType = FormManager.getActualValue('reqType');
  var formerCustNm = getFormerCustNameAU(reqId);
  formerCustNm = formerCustNm.trim();
  var vat = FormManager.getActualValue('vat');
  var custNm = '';
  var dataAPI = {};
  // dataAPI.success = false;
  var contractCustNm = cmr.query('GET.CUSTNM_ADDR', {
    REQ_ID : reqId,
    ADDR_TYPE : 'ZS01'
  });
  if (contractCustNm != undefined) {
    custNm = contractCustNm.ret1.toUpperCase() + " " + contractCustNm.ret2.toUpperCase();
  }
  custNm = custNm.trim();
  console.log("Checking if the request matches D&B...");
  var nm1 = _pagemodel.mainCustNm1 == null ? '' : _pagemodel.mainCustNm1;
  var nm2 = _pagemodel.mainCustNm2 == null ? '' : _pagemodel.mainCustNm2;
  var isicCd = FormManager.getActualValue('isicCd');
  if (nm1 != FormManager.getActualValue('mainCustNm1') || nm2 != FormManager.getActualValue('mainCustNm2')) {
    cmr.showAlert("The Customer Name/s have changed. The record has to be saved first. Please select Save from the actions.");
    return;
  }
  if (custNm == formerCustNm) {
    comp_proof_INAUSG = false;
    matchDnbForAUUpdate();
    return;
  }
  // match with API
  if (vat != '' && reqId != '' && formerCustNm != '' && custNm != '') {
    dataAPI = cmr.validateCustNmFromVat(vat, reqId, formerCustNm, custNm);
  } else {
    dataAPI.success = false;
  }

  if (dataAPI.success && dataAPI.custNmMatch) {
    if (dataAPI.formerCustNmMatch) {
      comp_proof_INAUSG = true;
      checkDnBMatchingAttachmentValidator();
      if (FormManager.validate('frmCMR')) {
        MessageMgr.clearMessages();
        doValidateRequest();
        matchDnbForAUUpdate();
        return;
      } else {
        cmr.showAlert('The request contains errors. Please check the list of errors on the page.');
      }

    } else {
      console.log("Name matches found in API but former Name doesn't match with Historical/trading/business name in API");
      comp_proof_INAUSG = false;
      cmr.showAlert("Please attach company proof as Name validation failed by API.");
    }
  } else if (dataAPI.success && dataAPI.formerCustNmMatch && !dataAPI.custNmMatch) {
    comp_proof_INAUSG = false;
    console.log("Former Name matched with Historical/trading/business name in API but name match failed in API");
    cmr.showAlert("Please attach company proof as Name validation failed by API.");
  } else {
    // match it with AU customerNm API
    if (reqId != '' && formerCustNm != '' && custNm != '') {
      dataAPI = cmr.validateCustNmFromAPI(reqId, formerCustNm, custNm);
    } else {
      dataAPI.success = false;
    }

    if (dataAPI.success && dataAPI.custNmMatch) {
      if (dataAPI.formerCustNmMatch) {
        comp_proof_INAUSG = true;
        checkDnBMatchingAttachmentValidator();
        if (FormManager.validate('frmCMR')) {
          matchDnbForAUUpdate();
          return;
        } else {
          cmr.showAlert('The request contains errors. Please check the list of errors on the page.');
        }

      } else {
        console.log("Name matches found in API but former Name doesn't match with Historical/trading/business name in API");
        comp_proof_INAUSG = false;
        cmr.showAlert("Please attach company proof as Name validation failed by API.");
      }
    } else if (dataAPI.success && dataAPI.formerCustNmMatch && !dataAPI.custNmMatch) {
      comp_proof_INAUSG = false;
      console.log("Former Name matched with Historical/trading/business name in API but name match failed in API");
      cmr.showAlert("Please attach company proof as Name validation failed by API.");
    } else {
      cmr.showProgress('Customer Name match with API failed . Now Checking Customer Name with Dnb...');
      // API match failed for CustomerName now checking for Dnb Match Start
      dojo.xhrGet({
        url : cmr.CONTEXT_ROOT + '/request//dnb/custNmUpdate.json',
        handleAs : 'json',
        method : 'GET',
        content : {
          'reqId' : reqId,
          'custNm' : custNm,
          'formerCustNm' : formerCustNm
        },
        timeout : 50000,
        sync : true,
        load : function(data, ioargs) {
          cmr.hideProgress();
          console.log(data);
          console.log(data.success);
          console.log(data.match);
          console.log(data.custNmMatch);
          console.log(data.formerCustNmMatch);
          if (data && data.success) {
            if (data.custNmMatch && data.formerCustNmMatch) {
              comp_proof_INAUSG = true;
              checkDnBMatchingAttachmentValidator();
              if (FormManager.validate('frmCMR')) {
                matchDnbForAUUpdate();
                return;
              } else {
                cmr.showAlert('The request contains errors. Please check the list of errors on the page.');
              }
            } else if (data.custNmMatch && !data.formerCustNmMatch) {
              console.log("Customer name matched with Dnb But Former Customer name match failed by Tradestyle Name");
              comp_proof_INAUSG = false;
              cmr.showAlert("Please attach company proof as Former Customer Name match failed by Dnb.");
            } else if (!data.custNmMatch && data.formerCustNmMatch) {
              console.log("Former Customer name matched with Dnb Tradestyle name But Customer name match failed by Dnb");
              comp_proof_INAUSG = false;
              cmr.showAlert("Please attach company proof as Former Customer Name match failed by Dnb.");
            } else {
              comp_proof_INAUSG = false;
              console.log("Name validation failed by dnb and API ");
              cmr.showAlert("Please attach company proof as Name validation failed by Dnb.");
            }
            checkDnBMatchingAttachmentValidator();
            var checkCompProof = checkForCompanyProofAttachment();
            if (!checkCompProof) {
              matchDnbForAUUpdate
            }
          } else {
            // continue
            console.log("An error occurred while matching dnb.");
            cmr.showConfirm("return;", 'An error occurred while matching Customer Name. Do you want to proceed with this request?', 'Warning', null, {
              OK : 'Yes',
              CANCEL : 'No'
            });
          }
        },
        error : function(error, ioargs) {
        }
      });
      // Dnb Match end
    }
  }

}

function matchDnbForAUUpdate() {
  var reqId = FormManager.getActualValue('reqId');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var reqType = FormManager.getActualValue('reqType');
  console.log("Checking if the request matches D&B...");
  var nm1 = _pagemodel.mainCustNm1 == null ? '' : _pagemodel.mainCustNm1;
  var nm2 = _pagemodel.mainCustNm2 == null ? '' : _pagemodel.mainCustNm2;
  var isicCd = FormManager.getActualValue('isicCd');
  if (nm1 != FormManager.getActualValue('mainCustNm1') || nm2 != FormManager.getActualValue('mainCustNm2')) {
    cmr.showAlert("The Customer Name/s have changed. The record has to be saved first. Please select Save from the actions.");
    return;
  }
  cmr.showProgress('Checking request data with D&B...');
  dojo.xhrGet({
    url : cmr.CONTEXT_ROOT + '/request/dnb/checkMatchUpdate.json',
    handleAs : 'json',
    method : 'GET',
    content : {
      'reqId' : reqId,
      'isicCd' : isicCd
    },
    timeout : 50000,
    sync : false,
    load : function(data, ioargs) {
      cmr.hideProgress();
      console.log(data);
      console.log(data.success);
      console.log(data.match);
      console.log(data.isicMatch);
      console.log(data.validate);
      if (data && data.success) {
        if (data.match) {
          comp_proof_INAUSG = true;
          checkDnBMatchingAttachmentValidator();
          if (FormManager.validate('frmCMR')) {
            MessageMgr.clearMessages();
            doValidateRequest();
            // cmr.showModal('addressVerificationModal');
            showAddressVerificationModal();
          } else {
            cmr.showAlert('The request contains errors. Please check the list of errors on the page.');
          }
        } else {
          comp_proof_INAUSG = false;
          console.log("Name/Address validation failed by dnb");
          cmr.showAlert("Please attach company proof as Address validation failed by Dnb.");
        }
        checkDnBMatchingAttachmentValidator();
      } else {
        // continue
        console.log("An error occurred while matching dnb.");
        cmr.showConfirm('showAddressVerificationModal()', 'An error occurred while matching dnb. Do you want to proceed with this request?', 'Warning', null, {
          OK : 'Yes',
          CANCEL : 'No'
        });
      }
    },
    error : function(error, ioargs) {
    }
  });
}

function checkIfUpfrontUpdateChecksRequired() {
  var reqId = FormManager.getActualValue('reqId');
  var reqType = FormManager.getActualValue('reqType');
  var reqStatus = FormManager.getActualValue('reqStatus');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var result = cmr.query('SYS_PARAM_VALUE_VERIFY', {
    CD : 'UPD_UI_CNTRY_LIST',
    VALUE : '%' + cntry + '%'
  });

  if (reqId > 0 && reqType == 'U' && reqStatus == 'DRA' && result && result.ret1) {
    return true;
  } else {
    return false;
  }
}

/**
 * Generic Validator to execute UpdateChecks Automation
 */
function addUpdateChecksExecution(frmCMR) {
  console.log("addUpdateChecksExecution..............");
  var reqType = FormManager.getActualValue('reqType');
  var elementResData = "";

  if (reqType != 'U') {
    // cmr.showModal('addressVerificationModal');
    showAddressVerificationModal();
    return;
  }
  console.log('Running Update Checks Element...');
  cmr.showProgress('Validating requested updates...');
  dojo.xhrPost({
    url : cmr.CONTEXT_ROOT + '/auto/element/updateCheck.json',
    handleAs : 'json',
    method : 'POST',
    content : dojo.formToObject(frmCMR),
    timeout : 500000,
    sync : true,
    load : function(data, ioargs) {
      cmr.hideProgress();
      if (data != '' && data != undefined) {
        if (data.rejectionMsg != null && data.rejectionMsg != '') {
          console.log('UpdateChecks Element Executed Successfully.');
          cmr.showAlert('Request cannot be submitted for update because of the following reasons.<br/><strong>' + data.rejectionMsg + '</strong>');
        }
        // else if (data.negativeChksMsg != '' && data.negativeChksMsg != null)
        // {
        // cmr.showConfirm('showAddressVerificationModal()', '<strong>' +
        // data.negativeChksMsg + '</strong> <br/> The request will require CMDE
        // review. Do you want to proceed ?', 'Warning', null, {
        // OK : 'Ok',
        // CANCEL : 'Cancel'
        // });
        // }
        else {
          // cmr.showModal('addressVerificationModal');
          showAddressVerificationModal();
        }
      } else {
        // cmr.showModal('addressVerificationModal');
        showAddressVerificationModal();
      }
    },
    error : function(error, ioargs) {
      success = false;
      console.log('An error occurred while running UpdateSwitchElement. Please contact your system administrator');
      reject('Error occurred in Update Checks.');
    }
  });
}

function checkForConfirmationAttachments() {
  var id = FormManager.getActualValue('reqId');
  var ret = cmr.query('CHECK_CONFIRMATION_ATTACHMENTS', {
    ID : id
  });
  if (ret && ret.ret1) {
    return true;
  } else {
    return false;
  }
}

function checkDnBMatchingAttachmentValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        // FOR Temporary India , AU and SG
        var id = FormManager.getActualValue('reqId');
        var ret = cmr.query('CHECK_DNB_MATCH_ATTACHMENT', {
          ID : id,
        });
        // FOR Temporary India
        if ((ret == null || ret.ret1 == null) && !comp_proof_INAUSG) {
          comp_proof_INAUSG = true;
          return new ValidationResult(null, false, "You\'re obliged to provide either one of the following documentation as backup - "
              + "client\'s official website, Secretary of State business registration proof, client\'s confirmation email and signed PO, attach it under the file content "
              + "of <strong>Company Proof</strong>. Please note that the sources from Wikipedia, Linked In and social medias are not acceptable.");
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), null, 'frmCMR');
}

// CREATCMR-2755
function checkNoMatchingAttachmentValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var id = FormManager.getActualValue('reqId');
        var ret = cmr.query('CHECK_DNB_MATCH_ATTACHMENT', {
          ID : id
        });
        if (ret == null || ret.ret1 == null) {
          return new ValidationResult(null, false, "You\'re obliged to provide either one of the following documentation as backup - "
              + "client\'s official website, Secretary of State business registration proof, client\'s confirmation email and signed PO, attach it under the file content "
              + "of <strong>Company Proof</strong>. Please note that the sources from Wikipedia, Linked In and social medias are not acceptable.");
        } else {
          flag = true;
          return new ValidationResult(null, true);
        }
      }
    };
  })(), null, 'frmCMR');
}

function showDocTypeConfirmDialog() {
  cmr.showConfirm('doChangeDocType()', 'There are Legal Name Confirmation and/or Address Confirmation attachment(s) attached with the request. '
      + 'These attachment types are not supported anymore and will be sunset. Do you want to use <strong>Company Proof</strong> instead?', 'Warning', null, null);
}

function doChangeDocType() {
  FormManager.doAction('frmCMR', 'CONFIRM_DOC_UPD', true, "Updating attachment types to 'Company Proof'...");
}

/**
 * Save function
 */
function autoSaveRequest() {
  // enable all checkboxes
  var cb = dojo.query('[type=checkbox]');
  for (var i = 0; i < cb.length; i++) {
    if (cb[i].id.indexOf('dijit') < 0 && cb[i].disabled) {
      cb[i].disabled = false;
      cb[i].removeAttribute('disabled');
    }
  }
  FormManager.doAction('frmCMR', 'SAV', true, 'Saving the request...');
}

function recreateCMR() {
  var msg = '<strong>WARNING: Use this function with caution!</strong><br><br>This will recreate a <strong>NEW CMR</strong> for this request. Please ensure that any invalid records in RDC have been marked as logically deleted and there is an actual need to create a new CMR for this request. Proceed?';
  cmr.showConfirm('executeRecreateCMR()', msg, 'Warning', null);
}

function executeRecreateCMR() {
  FormManager.doAction('frmCMR', 'RECREATE', true, 'Setting up request for recreation of CMR...');
}

function checkIfDataOrAddressFieldsUpdated(frmCMR) {
  console.log("checkIfDataOrAddressFieldsUpdated..............");
  var reqType = FormManager.getActualValue('reqType');
  var isNoDataUpdated = false;
  if (checkIfUpdateChecksRequiredOnUI()) {
    console.log('Running Update Checks Element...');
    cmr.showProgress('Validating requested updates...');
    var formData = dojo.formToObject(frmCMR);
    formData.capInd = FormManager.getActualValue('capInd');
    dojo.xhrPost({
      url : cmr.CONTEXT_ROOT + '/auto/element/updateCheck.json',
      handleAs : 'json',
      method : 'POST',
      content : formData,
      timeout : 500000,
      sync : true,
      load : function(data, ioargs) {
        cmr.hideProgress();
        if (data != '' && data != undefined) {
          if ((data.rejectionMsg != null && data.rejectionMsg.includes('No data/address changes made on request.'))
              || (data.negativeChksMsg != null && data.negativeChksMsg.includes('No data/address changes made on request.'))) {
            console.log('UpdateChecks Element Executed Successfully.');
            isNoDataUpdated = true;
          }
        }
      },
      error : function(error, ioargs) {
        success = false;
        console.log('An error occurred while running UpdateSwitchElement. Please contact your system administrator');
        reject('Error occurred in Update Checks.');
      }
    });
  }
  return isNoDataUpdated;
}

function checkIfUpdateChecksRequiredOnUI() {
  console.log('checkIfUpdateChecksRequiredOnUI..');
  var CNTRY_LIST_FOR_UPDT_CHECKS_ON_UI = [ '897', '724', '618', '848', '631', '866', '754', '649', '641', '624', '788', '678', '702', '806', '846', '706', '744', '838', '616', '834' ];
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var reqId = FormManager.getActualValue('reqId');
  var reqType = FormManager.getActualValue('reqType');
  var reqStatus = FormManager.getActualValue('reqStatus');
  // var requesterId = FormManager.getActualValue('requesterId');

  if (reqId > 0 && reqType == 'U' && reqStatus == 'DRA') {
    /*
     * var result = cmr.query('IS_AUTOMATED_PROCESSING', { CNTRY : cntry });
     * 
     * var isCmde = cmr.query('CHECK_CMDE', { CNTRY : cntry, REQUESTER_ID :
     * requesterId });
     * 
     * if (result && (result.ret1 == 'P' || result.ret1 == 'R' || result.ret1 ==
     * 'B')) {
     */
    if (CNTRY_LIST_FOR_UPDT_CHECKS_ON_UI.includes(cntry)) {
      console.log("checkIfUpdateChecksRequiredOnUI.. update checks are required");
      return true;
    } else {
      console.log("checkIfUpdateChecksRequiredOnUI.. update checks are not required");
      return false;
    }
  }

}

// CREATCMR-7874: NZ 2.0 - API check in Creation(D&B match, ISIC match, NZAPI
// match)
// CREATCMR-8430: do address matching for all addresses for create request
function matchDnBForNZ() {
  console.log('>>> matchDnBForNZ >>>');
  var reqId = FormManager.getActualValue('reqId');
  var isicCd = FormManager.getActualValue('isicCd');
  var businessNumber = FormManager.getActualValue('vat');
  console.log("Checking if the request matches D&B...");
  var nm1 = _pagemodel.mainCustNm1 == null ? '' : _pagemodel.mainCustNm1;
  var nm2 = _pagemodel.mainCustNm2 == null ? '' : _pagemodel.mainCustNm2;
  if (nm1 != FormManager.getActualValue('mainCustNm1') || nm2 != FormManager.getActualValue('mainCustNm2')) {
    cmr.showAlert("The Customer Name/s have changed. The record has to be saved first. Please select Save from the actions.");
    return;
  }
  cmr.showProgress('Checking request data with D&B...');
  // CREATCMR-8430: reset usSicmen when start DNB matching
  FormManager.setValue('usSicmen', '');  

  dojo
      .xhrGet({
        url : cmr.CONTEXT_ROOT + '/request/dnb/checkDNBAPIMatchForNZ.json',
        handleAs : 'json',
        method : 'GET',
        content : {
          'reqId' : reqId,
          'isicCd' : isicCd,
          'businessNumber' : businessNumber
        },
        timeout : 50000,
        sync : false,
        load : function(data, ioargs) {
          cmr.hideProgress();
          console.log(data);
          addrMatchResultForNZCreate = data;
          if (data && data.success) {
            if (data.dnbNmMatch && data.dnbAddrMatch) {
              console.log("DNB name match and DNB address match.");
              if (data.isicMatch) {
                console.log("ISIC match.");
                matchOtherAddressesforNZCreate(data);
              } else {
                console.log("ISIC mismatch.");
                cmr.showAlert('DNB name and address match success. ISIC match fail.\nPlease attach company proof');
                FormManager.setValue('matchOverrideIndc', 'Y');
              }
            } else if (data.tradeStyleMatch) {
              cmr
                  .showConfirm(
                      'autoDnbImportMatch("' + data.dunsNo + '","0")',
                      'The customer name on the request is a tradestyle name. For CMR creation, legal name should be used. <strong>Tradestyle name can be placed on the address’s division line.</strong> Do you want to override the customer name on the request with <strong><u>'
                          + data.legalName + '</u></strong>?' + '?', 'Warning', 'doOverrideDnBMatch()', {
                        OK : 'Yes',
                        CANCEL : 'No'
                      });
            } else if (data.confidenceCd) {
              showDnBMatchModal();
            } else {
              if (!data.dnbNmMatch) {
                console.log("DNB name mismatch and go to NZAPI check...");
                if (!data.apiSuccess || !data.apiCustNmMatch || !data.apiAddressMatch) {
                  console.log('Customer name or address match fail in NZ API: ' + data.message);
                  cmr.showAlert('DNB name match fail. Name or address match fail in NZAPI.\nPlease attach company proof');
                  FormManager.setValue('matchOverrideIndc', 'Y');
                } else {
                  matchOtherAddressesforNZCreate(data);
                }
              } else if (!data.dnbAddrMatch && data.isicMatch) {
                console.log('DNB name match, DNB address mismatch, ISIC match, go to NZAPI check...');
                if (!data.apiSuccess || !data.apiCustNmMatch || !data.apiAddressMatch) {
                  console.log('Customer name mismatch or address mismatch in NZAPI: ' + data.message);
                  cmr.showAlert('DNB name match success, DNB address match fail.\nISIC match success.\nName or address match fail in NZAPI.\nPlease attach company proof');
                  FormManager.setValue('matchOverrideIndc', 'Y');
                } else {
                  console.log('Customer name and address mismatch in NZAPI');
                  matchOtherAddressesforNZCreate(data);
                }
              } else {
                cmr.showAlert('DNB name and address match fail. ISIC match fail.\nPlease attach company proof');
                FormManager.setValue('matchOverrideIndc', 'Y');
              }
            }
          } else {
            // continue
            console.log("An error occurred while matching dnb.");
            cmr.showConfirm('showAddressVerificationModal()', 'An error occurred while matching dnb. Do you want to proceed with this request?', 'Warning', null, {
              OK : 'Yes',
              CANCEL : 'No'
            });
          }
        },
        error : function(error, ioargs) {
        }
      });

}

// check all address types for create request for Newzealand
function matchOtherAddressesforNZCreate (data) {
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var usSicmen = FormManager.getActualValue('usSicmen');
  if (!data.otherAddrDNBMatch) {
    if (!data.otherAddrAPIMatch) {
	  FormManager.setValue('matchOverrideIndc', 'Y');
	  if(usSicmen && usSicmen=='DNBO') {
		cmr.showAlert(data.message + '\nPlease attach company proof', 'Warning', 'doOverrideDnBMatch()');
	  } else {
		cmr.showAlert(data.message + '\nPlease attach company proof');
	  }
    } else {
      console.log("DNB address match fail. NZAPI address match success.")
      if (custSubGrp == 'NRMLC' || custSubGrp == 'AQSTN') {
        checkRetrievedForNZ();
      } else {
        showAddressVerificationModal();
      }
    }
  } else {
    if (custSubGrp == 'NRMLC' || custSubGrp == 'AQSTN') {
      checkRetrievedForNZ();
    } else {
      showAddressVerificationModal();
    }
  }
}

// CREATCMR-7884
function setClusterIDAfterRetrieveAction(glcCode) {
  console.log('>>> setClusterIDAfterRetrieveAction >>>');
  var glcClusterMap = {};
  glcClusterMap['NZL0005'] = '10662';
  glcClusterMap['NZL0020'] = '10662';
  glcClusterMap['NZL0010'] = '10663';
  glcClusterMap['NZL9999'] = '01147';

  FormManager.setValue('apCustClusterId', glcClusterMap[glcCode]);
  FormManager.setValue('clientTier', 'Q');
  FormManager.setValue('isuCd', '34');
}

// CREATCMR-7879
function setClusterIDAfterRetrieveAction4CN(custSubGrp, glcCode) {
  console.log('>>> setClusterIDAfterRetrieveAction4CN >>>');
  var indc = 'C';
  if (custSubGrp == 'EMBSA') {
    var _GBGId = FormManager.getActualValue('gbgId');
    if (FormManager.getActualValue('gbgId') != undefined && FormManager.getActualValue('gbgId') != '') {
      var ret = cmr.query('CHECK_CN_S1_GBG_ID_LIST', {
        ID : _GBGId
      });
      if (ret && ret.ret1 && ret.ret1 != 0) {
        indc = '';
      }
    }
    if (indc == 'C') {
      var result1 = cmr.query('GLC.CN.SEARCHTERM', {
        GLC_CD : '%' + glcCode + '%',
        DEFAULT_INDC : indc
      });
      indc = 'E';
      var result2 = cmr.query('GLC.CN.SEARCHTERM', {
        GLC_CD : '%' + glcCode + '%',
        DEFAULT_INDC : indc
      });
      if (result1 != null && result1.ret1 != undefined && result1.ret1 != '' || result2 != null && result2.ret1 != undefined && result2.ret1 != '') {
        var searchTerm1 = result1 != null ? result1.ret1 : '';
        var searchTerm2 = result2 != null ? result2.ret1 : '';
        var clientTier = result1.ret2;
        var isuCd = result1.ret3;
        FormManager.limitDropdownValues(FormManager.getField('searchTerm'), [ searchTerm1, searchTerm2 ]);
        FormManager.setValue('searchTerm', searchTerm1);
        // FormManager.readOnly('searchTerm');
        FormManager.limitDropdownValues(FormManager.getField('clientTier'), [ clientTier ]);
        FormManager.setValue('clientTier', clientTier);
        FormManager.readOnly('clientTier');
        FormManager.limitDropdownValues(FormManager.getField('isuCd'), [ isuCd ]);
        FormManager.setValue('isuCd', isuCd);
        FormManager.readOnly('isuCd');
      }
    }
  } else {
    if (custSubGrp == 'ECOSY') {
      indc = 'E';
    }
    var result = cmr.query('GLC.CN.SEARCHTERM', {
      GLC_CD : '%' + glcCode + '%',
      DEFAULT_INDC : indc
    });
    if (result != null && result.ret1 != undefined && result.ret1 != '') {
      var searchTerm = result.ret1;
      var clientTier = result.ret2;
      var isuCd = result.ret3;
      FormManager.limitDropdownValues(FormManager.getField('searchTerm'), [ searchTerm ]);
      FormManager.setValue('searchTerm', searchTerm);
      FormManager.readOnly('searchTerm');
      FormManager.limitDropdownValues(FormManager.getField('clientTier'), [ clientTier ]);
      FormManager.setValue('clientTier', clientTier);
      FormManager.readOnly('clientTier');
      FormManager.limitDropdownValues(FormManager.getField('isuCd'), [ isuCd ]);
      FormManager.setValue('isuCd', isuCd);
      FormManager.readOnly('isuCd');
      if (clientTier == '00000' && (custSubGrp == 'NRMLC' || custSubGrp == 'AQSTN')) {
        FormManager.setValue('clientTier', 'Q');
        FormManager.setValue('isuCd', '34');
      }
    } else if (custSubGrp == 'ECOSY' && glcCode != undefined && glcCode != '') {
      FormManager.limitDropdownValues(FormManager.getField('searchTerm'), [ '08036' ]);
      FormManager.setValue('searchTerm', '08036');
      FormManager.readOnly('searchTerm');
      FormManager.limitDropdownValues(FormManager.getField('clientTier'), [ 'Y' ]);
      FormManager.setValue('clientTier', 'Y');
      FormManager.readOnly('clientTier');
      FormManager.limitDropdownValues(FormManager.getField('isuCd'), [ '36' ]);
      FormManager.setValue('isuCd', '36');
      FormManager.readOnly('isuCd');
    }
  }
}

// CREATCMR-7884
function checkRetrievedForNZ() {
  console.log('>>> checkRetrievedForNZ >>>');
  var glcClusterMap = {};
  glcClusterMap['NZL0005'] = '10662';
  glcClusterMap['NZL0020'] = '10662';
  glcClusterMap['NZL0010'] = '10663';
  glcClusterMap['NZL9999'] = '01147';
  var hasRetrievedValue = FormManager.getActualValue('covBgRetrievedInd') == 'Y';
  var oldGlcCode = FormManager.getActualValue('geoLocationCd');
  var oldClusterId = FormManager.getActualValue('apCustClusterId');
  console.log("hasRetrievedValue is ", hasRetrievedValue, "old GLC code is ", oldGlcCode);

  if (!hasRetrievedValue) {
    cmr.showAlert('Request cannot be submitted because retrieve value is required action . ');
  } else {
    console.log("Checking the GLC match... retrieve value again...")
    var data = CmrServices.getAll('reqentry');
    cmr.hideProgress();
    if (data) {
      console.log(data);
      if (data.error && data.error == 'Y') {
        cmr.showAlert('An error was encountered when retrieving the values.\nPlease contact your system administrator.', 'Create CMR');
      } else {
        if (data.glcError) {
          // errorMsg += (showError ? ', ' : '') + 'GEO Location Code';
        } else {
          if (glcClusterMap[data.glcCode] != oldClusterId) {
            console.log("The cluster id are different, then overwrite the GLC code and cluster id.")
            FormManager.setValue('geoLocationCd', data.glcCode);
            FormManager.setValue('geoLocDesc', data.glcDesc);
            FormManager.setValue('apCustClusterId', glcClusterMap[data.glcCode]);
            FormManager.setValue('clientTier', 'Q');
            FormManager.setValue('isuCd', '34');
            // cmr.showAlert('The GLC and Cluster has been overwritten to ' +
            // data.glcCode + '-' + glcClusterMap[data.glcCode] + ', please
            // continue the process.\nPlease contact your system
            // administrator.', 'Create CMR');
            cmr.showConfirm('showAddressVerificationModal()', 'The GLC and Cluster has been overwritten to ' + data.glcCode + '-' + glcClusterMap[data.glcCode]
                + '. Do you want to proceed with this request?', 'Warning', null, {
              OK : 'Yes',
              CANCEL : 'No'
            });
          } else {
            if (data.glcCode != oldGlcCode) {
              console.log("The GLC code are different, the cluster id are same, then overwrite the GLC code only.")
              FormManager.setValue('geoLocationCd', data.glcCode);
              FormManager.setValue('geoLocDesc', data.glcDesc);
            }
            showAddressVerificationModal();
          }
        }
      }
    }
  }
}
// CREATCMR-8430: do DNB check for NZ update
function matchDnBForNZUpdate() {
  console.log('>>> matchDnBForNZUpdate >>>');
  var reqId = FormManager.getActualValue('reqId');
  var businessNumber = FormManager.getActualValue('vat');
  console.log("Checking if the request matches D&B...");
  var nm1 = _pagemodel.mainCustNm1 == null ? '' : _pagemodel.mainCustNm1;
  var nm2 = _pagemodel.mainCustNm2 == null ? '' : _pagemodel.mainCustNm2;
  if (nm1 != FormManager.getActualValue('mainCustNm1') || nm2 != FormManager.getActualValue('mainCustNm2')) {
    cmr.showAlert("The Customer Name/s have changed. The record has to be saved first. Please select Save from the actions.");
    return;
  }
  cmr.showProgress('Checking request data with D&B...');
  dojo.xhrGet({
    url : cmr.CONTEXT_ROOT + '/request/dnb/checkDNBAPIMatchUpdateForNZ.json',
    handleAs : 'json',
    method : 'GET',
    content : {
      'reqId' : reqId,
      'businessNumber' : businessNumber
    },
    timeout : 50000,
    sync : false,
    load : function(data, ioargs) {
      cmr.hideProgress();
      console.log(data);
      if (data && data.success) {
        if (!data.custNmMatch) {
          cmr.showAlert('Customer name match fail.\nPlease attach company proof');
          FormManager.setValue('matchOverrideIndc', 'Y');
        } else if (!data.formerCustNmMatch) {
          cmr.showAlert('Customer former name match fail.\nPlease attach company proof');
          FormManager.setValue('matchOverrideIndc', 'Y');
        } else if (!data.matchesAddrDnb) {
          if (data.addressType == "ZS01") {
            if (!data.matchesAddrAPI) {
              cmr.showAlert('DNB address match fail. NZAPI address match fail.\nPlease attach company proof');
              FormManager.setValue('matchOverrideIndc', 'Y');
            } else {
              console.log("DNB address match fail. NZAPI address match success.")
              showAddressVerificationModal();
            }
          } else {
            cmr.showAlert(data.message + '\nPlease attach company proof');
            FormManager.setValue('matchOverrideIndc', 'Y');
          }
        } else {
          showAddressVerificationModal();
        }
      } else {
        // continue
        console.log("An error occurred while matching dnb.");
        cmr.showConfirm('showAddressVerificationModal()', 'An error occurred while matching dnb. Do you want to proceed with this request?', 'Warning', null, {
          OK : 'Yes',
          CANCEL : 'No'
        });
      }
    },
    error : function(error, ioargs) {
    }
  });

}

// CREATCMR-8430: do NZBN API check,
function doNZBNAPIMatch() {
  console.log('>>> doNZBNAPIMacht >>>');
  FormManager.setValue('findDnbResult', 'Rejected');
  // CREATCMR-8430: use usSicmen to save the dnboverride flag for NZ, automation will use this flag to skip DNB matching
  FormManager.setValue('usSicmen', 'DNBO');
  
  hideModaldnb_Window();
  
  console.log("Checking if the request matches NZBN API...");
  var nm1 = _pagemodel.mainCustNm1 == null ? '' : _pagemodel.mainCustNm1;
  var nm2 = _pagemodel.mainCustNm2 == null ? '' : _pagemodel.mainCustNm2;
  if (nm1 != FormManager.getActualValue('mainCustNm1') || nm2 != FormManager.getActualValue('mainCustNm2')) {
    cmr.showAlert("The Customer Name/s have changed. The record has to be saved first. Please select Save from the actions.");
    return;
  }

  console.log(addrMatchResultForNZCreate);
  if (addrMatchResultForNZCreate && addrMatchResultForNZCreate.success) {
    if (!addrMatchResultForNZCreate.apiCustNmMatch || !addrMatchResultForNZCreate.apiAddressMatch) {
      console.log('Customer name or address match fail in NZ API: ' + addrMatchResultForNZCreate.message);
      cmr.showAlert('Name or address match fail in NZBN API.\nPlease attach company proof', 'Warning', 'doOverrideDnBMatch()');
    } else {
      console.log('ZS01 Customer name and address matched in NZBN API');
      matchOtherAddressesforNZCreate(addrMatchResultForNZCreate);
    }
  } else {
    // continue
    console.log("An error occurred while matching dnb.");
    cmr.showConfirm('showAddressVerificationModal()', 'An error occurred while matching dnb. Do you want to proceed with this request?', 'Warning', null, {
      OK : 'Yes',
      CANCEL : 'No'
    });
  }
}

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
