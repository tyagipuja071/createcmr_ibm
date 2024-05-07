/**
 * Does the actual adding to the list
 */
function doAddLicense() {
  
  var licenseNumber = FormManager.getActualValue('licenseNum');
  var licenseValidFrom = FormManager.getActualValue('validFrom');
  var licenseValidTo = FormManager.getActualValue('validTo');
  
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
  
  var fromDateIsValidFormat = isValidDate(licenseValidFrom);
  var toDateIsValidFormat = isValidDate(licenseValidTo);
  if(!fromDateIsValidFormat || !toDateIsValidFormat) {
    var invalidDateStr = '';
    if(!fromDateIsValidFormat && !toDateIsValidFormat) {
      invalidDateStr = `Invalid dates for 'Date Valid From' and 'Valid To Date'`
    } else if(!fromDateIsValidFormat) {
      invalidDateStr = `Invalid date for 'Date Valid From'`
    } else if (!toDateIsValidFormat) {
      invalidDateStr = `Invalid date for 'Valid To Date'`
    }
    cmr.showAlert(`${invalidDateStr}. Please enter a valid date in the format 'YYYYMMDD'.`);
    return;
  }
  
  if(licenseValidFrom > licenseValidTo) {
    cmr.showAlert(`'Date Valid From' must be on or before 'Valid To Date'.`);
    return;
  }
  
  var licenseDtls = `License Number: ${licenseNumber}, Date Valid From: ${licenseValidFrom} and Valid To Date: ${licenseValidTo}`;
  var addLicFunc = `actualAddLicense(${licenseValidFrom}, ${licenseValidTo})`;
  cmr.showConfirm(addLicFunc, 'Add <strong>' + licenseDtls + '</strong> to the License List?');

}

function doUpdateLicense() {
  
  var licenseNumber = FormManager.getActualValue('licenseNum');
  var licenseValidFrom = FormManager.getActualValue('validFrom');
  var licenseValidTo = FormManager.getActualValue('validTo');
  
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
  
  var fromDateIsValidFormat = isValidDate(licenseValidFrom);
  var toDateIsValidFormat = isValidDate(licenseValidTo);
  if(!fromDateIsValidFormat || !toDateIsValidFormat) {
    var invalidDateStr = '';
    if(!fromDateIsValidFormat && !toDateIsValidFormat) {
      invalidDateStr = `Invalid dates for 'Date Valid From' and 'Valid To Date'`
    } else if(!fromDateIsValidFormat) {
      invalidDateStr = `Invalid date for 'Date Valid From'`
    } else if (!toDateIsValidFormat) {
      invalidDateStr = `Invalid date for 'Valid To Date'`
    }
    cmr.showAlert(`${invalidDateStr}. Please enter a valid date in the format 'YYYYMMDD'.`);
    return;
  }
  
  if(licenseValidFrom > licenseValidTo) {
    cmr.showAlert(`'Date Valid From' must be on or before 'Valid To Date'.`);
    return;
  }
  
  var licenseDtls = `License Number: ${licenseNumber}, Date Valid From: ${licenseValidFrom} and Valid To Date: ${licenseValidTo}`;
  var addLicFunc = `actualUpdateLicense(${licenseValidFrom}, ${licenseValidTo})`;
  cmr.showConfirm(addLicFunc, 'The following are the new license details: ' + licenseDtls + '</strong>. Proceed?');

}

function showModalForAdd() {
  cmr.showModal('addUpdateLicenseModal');
  FormManager.setValue('licenseNum', '');
  FormManager.setValue('validTo', '');
  FormManager.setValue('validFrom', '');  
}

function showModalForUpdate(licenseNumber, validFrom, validTo) {
  cmr.showModal('addUpdateLicenseModal');
  FormManager.setValue('oldLicenseNumber', licenseNumber);
  FormManager.setValue('licenseNum', licenseNumber);
  FormManager.setValue('validFrom', validFrom);
  FormManager.setValue('validTo', validTo);
}

function performAddUpdate() {
  var oldLicense = FormManager.getActualValue('oldLicenseNumber');
  if (oldLicense != '' && oldLicense != undefined) {
    doUpdateLicense();
  } else {
    doAddLicense();
  }
}

function showAddLicenseModal() {
  cmr.showModal('addUpdateLicenseModal');
  FormManager.setValue('licenseNum', '');
  FormManager.setValue('validTo', '');
  FormManager.setValue('validFrom', '');  
}

function hasDuplicateLicenseNumber() {
  var licenseNumber = FormManager.getActualValue('licenseNum');

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
  var licenseNumber = FormManager.getActualValue('licenseNum');

  var queryString = `?reqId=${reqId}&licenseNum=${encodeURIComponent(licenseNumber)}&validFrom=${licenseValidFrom}&validTo=${licenseValidTo}`;
  FormManager.doHiddenAction('frmCMR_addressModal', 'ADD_LICENSE', cmr.CONTEXT_ROOT + '/request/license/process.json' + queryString, true, refreshLicenseAfterResult, false);
  closeLicenseModal();
}

function refreshLicenseAfterResult(result) {
  CmrGrid.refresh('LICENSES_GRID');
}

/**
 * Called after the confirm
 */
function actualUpdateLicense(licenseValidFrom, licenseValidTo) {
  var reqId = FormManager.getActualValue('reqId');
  var licenseNumber = FormManager.getActualValue('licenseNum');
  var oldLicenseNumber = FormManager.getActualValue('oldLicenseNumber');

  var queryString = `?reqId=${reqId}&licenseNum=${encodeURIComponent(licenseNumber)}&validFrom=${licenseValidFrom}&validTo=${licenseValidTo}&oldLicenseNumber=${oldLicenseNumber}&&currentIndc=N`;
  FormManager.doHiddenAction('frmCMR_addressModal', 'UPDATE_LICENSE', cmr.CONTEXT_ROOT + '/request/license/process.json' + queryString, true, refreshLicenseAfterResult, false);
  closeLicenseModal();
}

/**
 * Formatter for the Action column of the Delegate list grid
 * 
 * @param value
 * @param rowIndex
 * @returns {String}
 */
function removeUpdateLicenseFormatter(value, rowIndex) {
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
  var validFrom = rowData.validFrom[0];
  var validTo = rowData.validTo[0];
  
  var imgloc = cmr.CONTEXT_ROOT + '/resources/images/';
  var action = '';
  action += '<img src="' + imgloc + 'addr-remove-icon.png"  class="addr-icon" title = "Remove Entry" onclick = "doRemoveFromLicenseList(\'' + licenseNumber + '\')">' + 
  '<img src="' + imgloc + 'addr-edit-icon.png"  class="addr-icon" title = "Update Entry" onclick = "showModalForUpdate(\'' + licenseNumber + '\', \'' + validFrom + '\', \'' + validTo + '\')">';

  return action;
}

function doUpdateLicenseRecord(licenseNumber, validFrom, validTo) {
  FormManager.setValue('oldLicenseNumber', licenseNumber);
  FormManager.setValue('licenseNum', licenseNumber);
  FormManager.setValue('validFrom', validFrom);
  FormManager.setValue('validTo', validTo);
  licenseToUpdate = licenseNumber;
  cmr.showModal('addUpdateLicenseModal');
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

function closeLicenseModal() {
  FormManager.setValue('oldLicenseNumber', '');
  cmr.hideModal('addUpdateLicenseModal');
}

function licenseImportIndFormatter(value, rowIndex) {
  if (value == 'N') {
    return '<span style="color:red;font-weight:bold">New</span>';
  } else {
    return '';
  }
}

function isValidDate(dateString) {
  var regex = /^(19|20)\d\d(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01])$/;
  if(!regex.test(dateString)) return false;

  var year = Number(dateString.slice(0,4));
  var month = Number(dateString.slice(4,6)) - 1; // months are 0-based in
                                                  // JavaScript
  var day = Number(dateString.slice(6,8));

  var dateObject = new Date(year, month, day);

  // check if the dates match (JS will correct invalid dates like February 30 )
  return dateObject.getFullYear() === year && 
         dateObject.getMonth() === month && 
         dateObject.getDate() === day;
}
