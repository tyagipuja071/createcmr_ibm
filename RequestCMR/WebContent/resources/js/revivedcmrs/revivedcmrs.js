/*
 * File: revivedcmrs.js
 * Description: 
 * Contains the main functions in handling single-request processing.
 * The sub functions used by each tab are separated from this file. This will
 * contain only the more general functions related to action executions general
 * UI handling
 * 
 */
var CNTRY_LIST_FOR_INVALID_CUSTOMERS = [ '838', '866', '754' ];
var comp_proof_IN = false;
var flag = false;
dojo.require("dojo.io.iframe");

/**
 * Generic Validator to execute UpdateChecks Automation
 */
function processRevivedCMRS() {
  if (FormManager.getActualValue('revivedcmrsFile') == '') {
    cmr.showAlert('No file selected to upload...');
    return;
  }
  if (FormManager.getActualValue('revivedcmrsFile').endsWith(".xlsx") != true) {
    cmr.showAlert('Invalid file type selected. Please select \".xlsx\" file only.');
    return;
  }
  console.log("processRevivedCMRS..............");
  var reqType = FormManager.getActualValue('reqType');
  var elementResData = "";

  cmr.showProgress('Processing revived cmrs...');
  dojo.xhrPost({
    url : cmr.CONTEXT_ROOT + '/revivedcmrs/process.json',
    handleAs : 'json',
    method : 'POST',
    form : 'frmCMRRevived',
    timeout : 500000,
    sync : true,
    load : function(data, ioargs) {
      cmr.hideProgress();
      if (data != '' && data != undefined) {
        if (data.rejectionMsg != null && data.rejectionMsg != '') {
          console.log('Processing Revived CMRS done.');
          cmr.showAlert('Request cannot be submitted for update because of the following reasons.<br/><strong>' + data.rejectionMsg + '</strong>');
        }
        // else if (data.negativeChksMsg != '' && data.negativeChksMsg != null)
        // {
        // cmr.showConfirm('showAddrVerificationModal()', '<strong>' +
        // data.negativeChksMsg + '</strong> <br/> The request will require CMDE
        // review. Do you want to proceed ?', 'Warning', null, {
        // OK : 'Ok',
        // CANCEL : 'Cancel'
        // });
        // }
        else {
          cmr.showModal('addressVerificationModal');
        }
      } else {
        cmr.showModal('addressVerificationModal');
      }
    },
    error : function(error, ioargs) {
      success = false;
      console.log('An error occurred while running processRevivedCMRS. Please contact your system administrator');
      reject('Error occurred in Processing Revived CMRs.');
    }
  });
}

function downloadRevCMRTemplate() {
  var token = new Date().getTime();
  FormManager.setValue('dlDocType', 'TEMPLATE');
  FormManager.setValue('dlTokenId', token);
  cmr.showProgress('Downloading file. Please wait...');
  document.forms['fileTemplateDownloadForm'].submit();
  window.setTimeout('checkToken("' + token + '")', 1000);

}

function submitRevCMRSFile() {
  if (FormManager.getActualValue('revivedcmrsFile') == '') {
    cmr.showAlert('No file selected to upload...');
    return;
  }
  if (FormManager.getActualValue('revivedcmrsFile').endsWith(".xlsx") != true) {
    cmr.showAlert('Invalid file type selected. Please select \".xlsx\" file only.');
    return;
  }
  var token = new Date().getTime();
  FormManager.setValue('processTokenId', token);
  // cmr.showProgress('Processing file. Please wait...');
  cmr.showAlert('Processing stated. Please wait for the processing result email.', null, null, true);
  document.forms['frmCMRRevived'].submit();
  // window.setTimeout('checkToken("' + token + '")', 1000);
}
