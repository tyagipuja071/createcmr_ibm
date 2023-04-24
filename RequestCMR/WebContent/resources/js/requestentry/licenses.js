/**
 * Does the actual adding to the list
 */
function doAddLicense() {
  
  var licenseNumber = FormManager.getActualValue('licenseNumber');
  var licenseValidFrom = FormManager.getActualValue('licenseValidFrom');
  var licenseValidTo = FormManager.getActualValue('licenseValidTo');
  
  if (licenseNumber == '' || licenseValidFrom == '' || licenseValidTo == '') {
    cmr.showAlert('Please input License Number, Date Valid From and Valid To Date.');
    return;
  }
  
  var licenseDtls = `License Number: ${licenseNumber}, Date Valid From: ${licenseValidFrom} and Valid To Date: ${licenseValidTo}`;
  var addLicFunc = `actualAddLicense(${licenseNumber}, ${licenseValidFrom}, ${licenseValidTo})`;
  cmr.showConfirm(addLicFunc, 'Add <strong>' + licenseDtls  + '</strong> to the License List?');
}

/**
 * Called after the confirm
 */
function actualAddLicense(licenseNumber, licenseValidFrom, licenseValidTo) {
  var reqId = FormManager.getActualValue('reqId');
  var queryString = `?reqId=${reqId}&licenseNum=${licenseNumber}&validFrom=${licenseValidFrom}&validTo=${licenseValidTo}`
  FormManager.doHiddenAction('frmCMR_addressModal', 'ADD_LICENSE', cmr.CONTEXT_ROOT + '/request/license/process.json?' + queryString, true, refreshLicenseAfterResult, false);
}

function refreshLicenseAfterResult(result) {
  CmrGrid.refresh('LICENSES_GRID');
}

/**
 * Formatter for the Action column of the Delegate list grid
 * 
 * @param value
 * @param rowIndex
 * @returns {String}
 */
function removeLicenseFormatter(value, rowIndex) {
  var rowData = this.grid.getItem(0);
  if (rowData == null) {
    return ''; // not more than 1 record
  }
  rowData = this.grid.getItem(rowIndex);
  var currInd = rowData.currentIndc[0];
  if (currInd == 'Y') {
    return '';
  }

  var licenseNumber = rowData.licenseNum[0];

  var imgloc = cmr.CONTEXT_ROOT + '/resources/images/';
  return '<img src="' + imgloc + 'addr-remove-icon.png"  class="addr-icon" title = "Remove Entry" onclick = "doRemoveFromLicenseList(\'' + licenseNumber + '\')">';
}

function doRemoveFromLicenseList(licenseNumber) {
  cmr.showConfirm('actualRemoveFromLicenseList('+licenseNumber+')', 'Remove License <strong>' + licenseNumber + '</strong> from the License List?');
}

function actualRemoveFromLicenseList(licenseNumber) {
  var reqId = FormManager.getActualValue('reqId');
  var queryString = `?reqId=${reqId}&licenseNum=${licenseNumber}&currentIndc=N`
  FormManager.doHiddenAction('frmCMR_addressModal', 'REMOVE_LICENSE', cmr.CONTEXT_ROOT + '/request/license/process.json' + queryString, true,
      refreshLicenseAfterResult, false);
}

function licenseImportIndFormatter(value, rowIndex) {
  if (value == 'N') {
    return '<span style="color:red;font-weight:bold">New</span>';
  } else {
    return '';
  }
}
