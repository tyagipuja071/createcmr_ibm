/*
 * File: processing.js
 * Description: 
 * Contains the functions for the processing tab
 * 
 */

/**
 * Downloads the mass template file
 */
function downloadMassTemplate() {
  var token = new Date().getTime();
  FormManager.setValue('dlDocType', 'TEMPLATE');
  FormManager.setValue('dlTokenId', token);
  FormManager.setValue('dlReqId', FormManager.getActualValue('procReqId'));
  cmr.showProgress('Downloading file. Please wait...');
  document.forms['fileDlForm'].submit();
  window.setTimeout('checkToken("' + token + '")', 1000);

}

/**
 * Downloads the latest mass request file
 */
function downloadMassFile() {
  var token = new Date().getTime();
  FormManager.setValue('dlDocType', 'FILE');
  FormManager.setValue('dlTokenId', token);
  FormManager.setValue('dlIterId', '');
  FormManager.setValue('dlReqId', FormManager.getActualValue('procReqId'));
  cmr.showProgress('Downloading file. Please wait...');
  document.forms['fileDlForm'].submit();
  window.setTimeout('checkToken("' + token + '")', 1000);

}

/**
 * Downloads the latest mass request file
 */
function downloadMassFileIter(iteration) {
  var token = new Date().getTime();
  FormManager.setValue('dlDocType', 'FILE');
  FormManager.setValue('dlTokenId', token);
  FormManager.setValue('dlIterId', iteration);
  FormManager.setValue('dlReqId', FormManager.getActualValue('procReqId'));
  cmr.showProgress('Downloading file. Please wait...');
  document.forms['fileDlForm'].submit();
  window.setTimeout('checkToken("' + token + '")', 1000);

}
/**
 * Downloads the error log file
 */
function downloadErrorLog() {
  var token = new Date().getTime();
  FormManager.setValue('dlDocType', 'ERROR_LOG');
  FormManager.setValue('dlTokenId', token);
  FormManager.setValue('dlReqId', FormManager.getActualValue('procReqId'));
  cmr.showProgress('Downloading file. Please wait...');
  document.forms['fileDlForm'].submit();
  window.setTimeout('checkToken("' + token + '")', 1000);

}

/**
 * Submits the mass file selected
 */
function submitMassFile() {
  if (FormManager.getActualValue('massFile') == '') {
    cmr.showAlert('No file selected to upload...');
    return;
  }
  if ((FormManager.getActualValue('massFile').endsWith(".xlsx") != true && FormManager.getActualValue('reqType') == 'M')) {
    cmr.showAlert('Invalid mass file type selected. Please select \".xlsx\" file only.');
    return;
  }
  if ((FormManager.getActualValue('massFile').endsWith(".xlsm") != true && FormManager.getActualValue('reqType') == 'N')) {
    cmr.showAlert('Invalid mass file type selected. Please select \".xlsm\" file only.');
    return;
  }
  cmr.aftertoken = refreshMassProcess;
  FormManager.doHiddenFileAction('frmCMRProcess', 'SUBMIT_FILE', cmr.CONTEXT_ROOT + '/massrequest/process.json', true, massTokenId, false);

}

/**
 * Refreshes UI fields after mass file upload
 */
function refreshMassProcess() {
  var params = {
    REQ_ID : FormManager.getActualValue('procReqId')
  };
  var record = cmr.getRecord('ADMIN', 'Admin', params);
  FormManager.setValue('iterId', record.iterationId);
  dojo.byId('iterationId').innerHTML = record.iterationId;
  dojo.byId('massFileDl').innerHTML = '<a href="javascript:downloadMassFile()">Download Current Version</a>';
  dojo.byId('massFile').value = '';
}

/**
 * Marks the request as completed
 */
function markAsCompleted() {
  cmr.showConfirm('markComplete()', 'Mark the request as completed?', null, null, {
    OK : 'Yes',
    CANCEL : 'Cancel'
  });
}

function markComplete() {
  var completeAction = YourActions.Mark_as_Completed;
  FormManager.doAction('frmCMR', completeAction, true);
}

/**
 * Does the actual addition of cmrs to the CMR list
 */
function actualAddToCMRList() {
  cmr.parReqId = FormManager.getActualValue('reqId');
  cmr.parReqType = FormManager.getActualValue('reqType');
  cmr.cmrList = document.getElementById("cmrList").value;
  var textAreaString = document.getElementById("cmrList").value;
  textAreaString = textAreaString.replace(/\n\r/g, ",");
  textAreaString = textAreaString.replace(/\n/g, ",");
  var cmrCntry = FormManager.getActualValue('cmrIssuingCntry');

  cmr.cmrList = textAreaString;
  // validation added to not import invalid CMR number for Swiss
  if (cmrCntry == SysLoc.SWITZERLAND || cmrCntry == SysLoc.SPAIN || cmrCntry == SysLoc.AUSTRIA || cmrCntry == SysLoc.GREECE || cmrCntry == SysLoc.CYPRUS) {
    var cmrsArr = cmr.cmrList.split(',');
    var reqtype = FormManager.getActualValue('reqType');
    var invalidCount = 0;
    var invalidCmr = false;
    var invalidCountRnD = 0;
    var invalidCmrRnD = false;
    var result = null;
    var resultRnD = null;
    var landCntryToCheck = "";
    var countryUse = FormManager.getActualValue('countryUse');
    if (countryUse == '848LI') {
      landCntryToCheck = 'CH';
    } else {
      landCntryToCheck = 'LI';
    }
    if (cmrCntry == '618') {
      landCntryToCheck = 'AT';
    }

    for (var i = 0; i < cmrsArr.length; i++) {
      var qParams = {
        KATR6 : cmrCntry,
        MANDT : cmr.MANDT,
        ZZKV_CUSNO : cmrsArr[i],
      };
      var qParamsRnD = {
        KATR6 : cmrCntry,
        MANDT : cmr.MANDT,
        ZZKV_CUSNO : cmrsArr[i],
        BEGRU : landCntryToCheck
      };
      console.log("KATR6 = " + cmrCntry + "MANDT" + cmr.MANDT + "ZZKV_CUSNO" + cmrsArr[i]);

      if (reqtype == 'R') {
        result = cmr.query('CHECK_VALID_CMRS_R', qParams);
      } else {
        result = cmr.query('CHECK_VALID_CMRS_D', qParams);
      }
      if (result != null && reqtype == 'D') {
        invalidCmr = result.ret1 > 0 ? false : true;
        if (invalidCmr == true)
          invalidCount++;
      }
      // even if records have one zs01 that is active the cmr should not be a
      // candidate for reactivation.
      if (result != null && reqtype == 'R') {
        invalidCmr = result.ret1 > 0 ? true : false;
        if (invalidCmr == true)
          invalidCount++;
      }
      // to check the land cntry of imported cmrs (applicable for only
      // sub-regions cntry)
      if ((reqtype == 'R' || reqtype == 'D') && cmrCntry == SysLoc.SWITZERLAND) {
        resultRnD = cmr.query('CHECK_VALID_R_D', qParamsRnD);
        invalidCmrRnD = resultRnD.ret1 > 0 ? true : false;
        if (invalidCmrRnD == true)
          invalidCountRnD++;
      }

      if ((reqtype == 'R' || reqtype == 'D') && cmrCntry == SysLoc.AUSTRIA) {
        resultRnD = cmr.query('CHECK_VALID_R_D_AT', qParams);
        invalidCmrRnD = resultRnD.ret1 > 0 ? true : false;
        if (invalidCmrRnD == true)
          invalidCountRnD++;
      }

    }
    if (invalidCount > 0 && reqtype == 'R') {
      cmr.showAlert('Addresses can only be imported from inactive CMRs. The chosen CMRs has an invalid CMR.');
      return;
    } else if (invalidCount > 0 && reqtype == 'D') {
      cmr.showAlert('Addresses can only be imported from active CMRs. The chosen CMR is an invalid record.');
      return;
    }

    if (invalidCountRnD > 0 && landCntryToCheck == 'AT') {
      cmr.showAlert('Addresses can only be imported from active CMRs. The chosen CMR is an invalid record.');
      return;
    }

    if (invalidCountRnD > 0 && landCntryToCheck == 'LI') {
      cmr.showAlert('Addresses cannot be imported from Liechtenstein(landed country) CMRs for issuing country Siwtzerland.');
      return;
    } else if (invalidCountRnD > 0 && landCntryToCheck == 'CH') {
      cmr.showAlert('Addresses cannot be imported from Switzerland(landed country) CMRs for issuing country Liechtenstein.');
      return;
    }
  }
  FormManager.doHiddenCMRAction('frmReactivate', 'ADD_CMR', cmr.CONTEXT_ROOT + '/reactivaterequest/process.json?reqId=' + cmr.parReqId + '&&cmrList=' + cmr.cmrList + '&&reqType=' + cmr.parReqType,
      true, refreshCMRAfterAdd);
}

/**
 * Refreshes CMR list after add
 */
function refreshCMRAfterAdd(result) {
  if (result.success) {
    CmrGrid.refresh('CMR_LIST_GRID');
  }
  cmr.hideModal('addCMRsModal');
}

/**
 * Refreshes CMR list after remove
 */
function refreshCMRAfterRemove(result) {

  if (result.success) {
    CmrGrid.refresh('CMR_LIST_GRID');
  }
}

function addInputCMRs() {
  cmr.showProgress('Processing. Please wait...');
  document.forms['frmReactivate'].submit();
}

function addCMRsModal_onLoad() {
  // MessageMgr.clearMessages(true);

  FormManager.setValue('cmrList', '');
}

/**
 * Removes an entry from the CMR list
 */
function doRemoveFromCMRList(value, rowIndex) {
  var textAreaString = document.getElementById("cmrList").value;
  textAreaString = textAreaString.replace(/\n\r/g, "<br />");
  textAreaString = textAreaString.replace(/\n/g, "<br />");
  cmr.cmrList = textAreaString;
  cmr.parReqId = FormManager.getActualValue('reqId');
  cmr.parReqType = FormManager.getActualValue('reqType');
  MessageMgr.clearMessages();
  cmr.showConfirm('actualRemoveFromCMRList()', 'Remove selected CMR(s) from the list?');
}

/**
 * Does the actual removal from the CMR list
 */
function actualRemoveFromCMRList() {
  cmr.parReqId = FormManager.getActualValue('reqId');
  cmr.parReqType = FormManager.getActualValue('reqType');
  /*
   * cmr.cmrList = document.getElementById("cmrList").value; var textAreaString =
   * document.getElementById("cmrList").value; textAreaString =
   * textAreaString.replace(/\n\r/g, ","); textAreaString =
   * textAreaString.replace(/\n/g, ",");
   */

  // cmr.cmrList = textAreaString;
  if (!CmrGrid.hasSelected()) {
    alert('No record(s) selected.');
    return;
  }

  var chkBoxes = document.getElementsByName('gridchk');
  var val = '';
  if (chkBoxes) {
    for (var i = 0; i < chkBoxes.length; i++) {
      if (chkBoxes[i].checked) {
        console.log('value' + i + "=" + chkBoxes[i].value);
        val = chkBoxes[i].value.slice(6);
        console.log(val);
        if (cmr.cmrList != '') {
          cmr.cmrList = cmr.cmrList + "," + val;
        } else {
          cmr.cmrList = val;
        }
      }
    }
  }

  console.log(cmr.cmrList);
  FormManager.doHiddenCMRAction('frmReactivate', 'REMOVE_CMR', cmr.CONTEXT_ROOT + '/reactivaterequest/process.json?reqId=' + cmr.parReqId + '&&cmrList=' + cmr.cmrList + '&&reqType=' + cmr.parReqType,
      true, refreshCMRAfterRemove);
}

function addCmrs() {
  cmr.currentModalId = 'addCMRsModal';
  MessageMgr.clearMessages();
  cmr.showModal('addCMRsModal');

}

function doImportAddToCMRList(cmrNo) {

  console.log('Imorted CMR=' + cmrNo);
  cmr.cmrList = cmrNo;
  cmr.parReqId = FormManager.getActualValue('reqId');
  cmr.parReqType = FormManager.getActualValue('reqType');
  FormManager.doHiddenCMRAction('frmReactivate', 'ADD_CMR', cmr.CONTEXT_ROOT + '/reactivaterequest/process.json?reqId=' + cmr.parReqId + '&&cmrList=' + cmr.cmrList + '&&reqType=' + cmr.parReqType,
      true, refreshCMRAfterAdd);
}

/**
 * Add cmrs to the CMR list
 */
function doAddToCMRList(id, name) {
  // cmr.cmrList = document.getElementById("cmrList").value;
  var textAreaString = document.getElementById("cmrList").value;
  textAreaString = textAreaString.replace(/\n\r/g, "<br />");
  textAreaString = textAreaString.replace(/\n/g, "<br />");

  cmr.cmrList = textAreaString;
  cmr.parReqId = FormManager.getActualValue('reqId');
  cmr.parReqType = FormManager.getActualValue('reqType');
  cmr.showConfirm('actualAddToCMRList()', 'Add CMR(s) to the Request ID ' + cmr.parReqId + '?');
}

function downloadMassDplResult() {
  if (FormManager.getActualValue('dplChkResult') == 'Not Done') {
    cmr.showConfirm('doRunMassDplChecking()', 'DPL Check is not yet performed. Run DPL checking now?');
    return;
  }
  var token = new Date().getTime();
  FormManager.setValue('dlDocType', 'MASSDPL_RESULT_FILE');
  FormManager.setValue('dlTokenId', token);
  FormManager.setValue('dlIterId', '');
  FormManager.setValue('dlReqId', FormManager.getActualValue('procReqId'));
  cmr.showProgress('Downloading file. Please wait...');
  document.forms['fileDlForm'].submit();
  window.setTimeout('checkToken("' + token + '")', 1000);
}

function addDplCheckValidator() {
  console.log('>>> mass request addDPLCheckValidator start processing. . .');
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var result = FormManager.getActualValue('dplChkResult');
        if (result == '' || result.toUpperCase() == 'NOT DONE') {
          return new ValidationResult(null, false, 'DPL Check has not been performed yet.');
        } else if (result == '' || result.toUpperCase() == 'ALL FAILED') {
          return new ValidationResult(null, false, 'DPL Check has failed. This record cannot be processed.');
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), null, 'frmCMR');
}

/**
 * DTN: 1897817: ITALY - DPL check for mass update when customer name and/or
 * customer name con't are changed
 */
function doDplCheck() {
  cmr.showConfirm('doRunDpl()', 'DPL Check will be performed on the addresses on the mass update template where customer names (Customer Name and Customer Name Con\'t) are provided. Proceed?');
}
/**
 * DTN: 1897817: ITALY - DPL check for mass update when customer name and/or
 * customer name con't are changed
 */
function doRunDpl() {
  cmr.aftertoken = showDplResult;
  FormManager.doHiddenCMRAction('frmCMRProcess', 'DPL_CHECK', cmr.CONTEXT_ROOT + '/massrequest/ld_dpl.json', true, showDplResult);
}
/**
 * DTN: 1897817: ITALY - DPL check for mass update when customer name and/or
 * customer name con't are changed
 */
function showDplResult(result) {
  console.log('>> Executing showDplResult()');
  var qParams = {
    REQ_ID : FormManager.getActualValue('procReqId'),
    ITER_ID : FormManager.getActualValue('iterId'),
  };

  var result = cmr.query('COUNT.LD_MASS_UPDT_FOR_DPL_CHECK', qParams);

  if (result != null && result.ret1 > 0) {
    // do show the show DPL check summary
    FormManager.show('btnDplSum');
  }
}

/**
 * Imports the CMR to the request
 * 
 * @param cmrNo
 * @param cntry
 * @param cntryDesc
 */
function importCMRs(cmrNo, cntry, cntryDesc, addressOnly) {
  cmr.importcmr = cmrNo;
  cmr.importcntry = cntry;
  if (addressOnly) {
    var addrType = cmr.currentCmrResult != null ? cmr.currentCmrResult.data.addressTypeDesc + ' address ' : ' address ';
    var addrSeq = cmr.currentCmrResult != null ? ' with sequence ' + cmr.currentCmrResult.data.addressSeq : ' ';
    cmr.showConfirm('doImportCmrs(true)', 'The ' + addrType + addrSeq + ' from CMR Number ' + cmrNo + ' issued by ' + cntryDesc
        + ' will be imported by the system. The address will be added to the current list. Continue?', null, null, {
      OK : 'Yes',
      CANCEL : 'Cancel'
    });
  } else {
    if (cntryDesc != null) {
      cmr.showConfirm('doImportCmrs()', 'Records with CMR Number ' + cmrNo + ' issued by ' + cntryDesc
          + ' will be imported by the system. The data will replace all the current data on the request. Continue?', null, null, {
        OK : 'Yes',
        CANCEL : 'Cancel'
      });
    } else {
      cmr.showConfirm('doImportCmrs()', 'Records with CMR Number ' + cmrNo + ' will be imported by the system. The data will replace all the current data on the request. Continue?', null, null, {
        OK : 'Yes',
        CANCEL : 'Cancel'
      });
    }
  }
}

/**
 * Overriding findcmrsearch method to Continue importing the record
 */
function continueUpdateCMR() {
  var result = cmr.currentCmrResult;
  FormManager.setValue('enterCMRNo', result.data.cmrNum);
  importCMRs(result.data.cmrNum, result.data.issuedBy, result.data.issuedByDesc);
}

/**
 * overriding find cmr search method to Actual call to import CMRs
 */
function doImportCmrs(addressOnly) {
  var cmrNo = cmr.importcmr;
  if (cmr.importcntry != null) {
    cntry = cmr.importcntry;
    FormManager.setValue('cmrIssuingCntry', cmr.importcntry == '756' ? '755' : cmr.importcntry);
    cmr.importcntr = null;
  }
  cmr.showProgress('Importing records with CMR Number ' + cmrNo + '.  This process might take a while. Please wait..');
  doImportAddToCMRList(cmrNo);
}
