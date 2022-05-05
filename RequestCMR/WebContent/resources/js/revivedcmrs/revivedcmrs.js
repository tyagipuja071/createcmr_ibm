/*
 * File: revivedcmrs.js
 * Description: 
 * Contains the main functions in handling single-request processing.
 * The sub functions used by each tab are separated from this file. This will
 * contain only the more general functions related to action executions general
 * UI handling
 * 
 */

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
  cmr.showAlert('Processing started. Please wait for the processing result email.', null, null, true);
  document.forms['frmCMRRevived'].submit();
  // window.setTimeout('checkToken("' + token + '")', 1000);
}
