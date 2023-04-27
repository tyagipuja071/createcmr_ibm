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
  
  var licenseNumRegex = /^\d{2}\/\d{5}\/\d{6}$/;
  if (!licenseNumRegex.test(licenseNumber)) {
    cmr.showAlert('Invalid license number format. The license number format should be NN/NNNNN/NNNNNN.');
    return;
  }
  
  if(hasDuplicateLicenseNumber()) {
    cmr.showAlert('The license number entered is a duplicate. Please provide a unique license number.');
    return;
  }
  
  var licenseDtls = `License Number: ${licenseNumber}, Date Valid From: ${licenseValidFrom} and Valid To Date: ${licenseValidTo}`;
  var addLicFunc = `actualAddLicense(${licenseValidFrom}, ${licenseValidTo})`;
  cmr.showConfirm(addLicFunc, 'Add <strong>' + licenseDtls + '</strong> to the License List?');
}

function hasDuplicateLicenseNumber() {
  var licenseNumber = FormManager.getActualValue('licenseNumber');

  if(CmrGrid.GRIDS.LICENSES_GRID_GRID && CmrGrid.GRIDS.LICENSES_GRID_GRID .rowCount > 0) {
    for (var i = 0; i < CmrGrid.GRIDS.LICENSES_GRID_GRID.rowCount; i++) {
      var record = CmrGrid.GRIDS.LICENSES_GRID_GRID.getItem(i);
      if(licenseNumber == record.licenseNum[0]) {
        return true;
      }
    }
  }
  
  return false;
}

/**
 * Called after the confirm
 */
function actualAddLicense(licenseValidFrom, licenseValidTo) {
  var reqId = FormManager.getActualValue('reqId');
  var licenseNumber = FormManager.getActualValue('licenseNumber');

  var queryString = `?reqId=${reqId}&licenseNum=${encodeURIComponent(licenseNumber)}&validFrom=${licenseValidFrom}&validTo=${licenseValidTo}`
  FormManager.doHiddenAction('frmCMR_addressModal', 'ADD_LICENSE', cmr.CONTEXT_ROOT + '/request/license/process.json' + queryString, true, refreshLicenseAfterResult, false);
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
  if (rowData == null || FormManager.getActualValue('viewOnlyPage') == 'true') {
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

var licenseToRemove = '';
function doRemoveFromLicenseList(licenseNumber) {
  // note: Passing the licenseNumber as a parameter does not work as expected,
  // because its value gets evaluated when passed as a parameter in a function
  // As a workaround, we assign it to a variable and access the value within the
  // actual function
  licenseToRemove = licenseNumber;
  cmr.showConfirm('actualRemoveFromLicenseList()', 'Remove License <strong>' + licenseNumber + '</strong> from the License List?');
}

function actualRemoveFromLicenseList() {
  var reqId = FormManager.getActualValue('reqId');
  var queryString = `?reqId=${reqId}&licenseNum=${encodeURIComponent(licenseToRemove)}&currentIndc=N`
  FormManager.doHiddenAction('frmCMR_addressModal', 'REMOVE_LICENSE', cmr.CONTEXT_ROOT + '/request/license/process.json' + queryString, true,
      refreshLicenseAfterResult, false);
  
  licenseToRemove = '';
}

function licenseImportIndFormatter(value, rowIndex) {
  if (value == 'N') {
    return '<span style="color:red;font-weight:bold">New</span>';
  } else {
    return '';
  }
}
