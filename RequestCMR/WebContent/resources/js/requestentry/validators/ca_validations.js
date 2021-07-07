/*

Validations file for Canada

 */

var regexNumeric = /^[0-9]+$/;
var regexAlphanumeric = /^[0-9a-zA-Z]+$/;
var _importedIndc = null;
/**
 * Adds the validator for the mandatory sold to and single address types
 * 
 * @returns
 */
function addAddressRecordTypeValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('cmrIssuingCntry') != SysLoc.CANADA) {
          return new ValidationResult(null, true);
        }
        var reqType = FormManager.getActualValue('reqType');

        if (reqType == 'C') {
          if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount < 1) {
            return new ValidationResult(null, false, 'Sold-To address is mandatory. Please add one Sold-To address.');
          }

          var record = null;
          var type = null;
          var zs01Count = 0; // Sold-to
          var zd01Count = 0; // Ship-to
          var zi01Count = 0; // Install-at
          var zp03Count = 0; // A/R Bill-to
          var zp07Count = 0; // TLA/Rentals Bill-to

          for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
            record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
            type = record.addrType;
            if (typeof (type) == 'object') {
              type = type[0];
            }
            if (type == 'ZS01') {
              zs01Count++;
            } else if (type == 'ZD01') {
              zd01Count++;
            } else if (type == 'ZI01') {
              zi01Count++;
            } else if (type == 'ZP03') {
              zp03Count++;
            } else if (type == 'ZP07') {
              zp07Count++;
            }
          }

          if (zs01Count == 0) {
            return new ValidationResult(null, false, 'Sold-To address is mandatory. Please add one Sold-To address.');
          } else if (zs01Count > 1) {
            return new ValidationResult(null, false, 'Only one Sold-To address is allowed. Please remove the additional Sold-to address.');
          } else if (zd01Count > 1) {
            return new ValidationResult(null, false, 'Only one Ship-To address is allowed. Please remove the additional Ship-To address.');
          } else if (zi01Count > 1) {
            return new ValidationResult(null, false, 'Only one Install-At address is allowed. Please remove the additional Install-At address.');
          } else if (zp03Count > 1) {
            return new ValidationResult(null, false, 'Only one (A/R) Bill-To address is allowed. Please remove the additional (A/R) Bill-To address.');
          } else if (zp07Count > 1) {
            return new ValidationResult(null, false, 'Only one TLA/Rentals Bill-To address is allowed. Please remove the additional TLA/Rentals Bill-To address.');
          } else {
            return new ValidationResult(null, true);
          }
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function addInacCdValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var inacCd = FormManager.getActualValue('inacCd');
        if (inacCd != null && inacCd != '' && !inacCd.match(regexAlphanumeric)) {
          return new ValidationResult(null, false, 'NAT/INAC contains special character');
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function addChangeNameAttachmentValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqType = FormManager.getActualValue('reqType');
        var mainCustNm1 = FormManager.getActualValue("mainCustNm1");
        var oldCustNm1 = FormManager.getActualValue("oldCustNm1");
        var mainCustNm2 = FormManager.getActualValue("mainCustNm2");
        var oldCustNm2 = FormManager.getActualValue("oldCustNm2");
        if (typeof (_pagemodel) != 'undefined') {
          if (reqType == 'U' && (mainCustNm1 != oldCustNm1 || mainCustNm2 != oldCustNm2)) {
            var id = FormManager.getActualValue('reqId');
            var ret = cmr.query('CHECK_NAME_CHANGE_ATTACHMENT', {
              ID : id
            });

            if (ret == null || ret.ret1 == null) {
              var ret2 = cmr.query('CHECK_TERRITORY_ATTACHMENT', {
                ID : id
              });
              if (ret2 == null || ret2.ret1 == null) {
                return new ValidationResult(null, false, 'Legal Name Change Letter OR Territory Manager Approval in Attachment tab is required.');
              } else {
                return new ValidationResult(null, true);
              }
            }
          } else {
            return new ValidationResult(null, true);
          }
        }
      }
    };
  })(), 'MAIN_ATTACH_TAB', 'frmCMR');
}

/**
 * Toggles the Install At, Invoice To, and Maintenance Billing choices depending
 * on the current address records
 * 
 * @param cntry
 * @param addressMode
 * @param details
 * @returns
 */
function toggleAddrTypesForCA(cntry, addressMode, details) {
  if (addressMode == 'newAddress' || addressMode == 'copyAddress') {
    var addressRowCount = CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount;
    if (addressRowCount > 0) {
      var arrAddrType = [];
      var i;
      for (i = 0; i < addressRowCount; i++) {
        var addrType = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i).addrType;
        arrAddrType.push("" + addrType);
      }

      cmr.showNode('radiocont_ZS01');
      cmr.showNode('radiocont_ZI01');
      cmr.showNode('radiocont_ZP01');

      if (arrAddrType.indexOf("ZS01") != -1) {
        cmr.hideNode('radiocont_ZS01');
      }

      if (arrAddrType.indexOf("ZI01") != -1) {
        cmr.hideNode('radiocont_ZI01');
        FormManager.setValue('addrType', 'ZP01');
      } else {
        FormManager.setValue('addrType', 'ZI01');
      }

    } else {
      FormManager.setValue('addrType', 'ZS01');
      cmr.showNode('radiocont_ZS01');
      cmr.hideNode('radiocont_ZI01');
      cmr.hideNode('radiocont_ZP01');
    }
  }
}

function hideObsoleteAddressOption(cntry, addressMode, details) {
  if (addressMode == 'newAddress' || addressMode == 'copyAddress') {
    cmr.hideNode('radiocont_ZD02');
    cmr.hideNode('radiocont_ZP08');
    cmr.hideNode('radiocont_ZP04');
    cmr.hideNode('radiocont_ZP05');
    cmr.hideNode('radiocont_ZE01');
    cmr.hideNode('radiocont_ZP06');
    cmr.hideNode('radiocont_ZP09');

  }
}

/**
 * Sets the default country to CA
 * 
 * @param cntry
 * @param addressMode
 * @param saving
 * @returns
 */
function addCAAddressHandler(cntry, addressMode, saving) {
  if (!saving) {
    if (addressMode == 'newAddress') {
      FilteringDropdown['val_landCntry'] = 'CA';
      FormManager.setValue('landCntry', 'CA');
    } else {
      FilteringDropdown['val_landCntry'] = null;
    }
  }
}

/**
 * Enable/Disable the PST Exempt, PST Exempt Lic No and QST based on the
 * selected Install-At Province/State
 * 
 * @returns
 */
function toggleCATaxFields() {
  var addrRowCount = CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount;
  if (addrRowCount > 0) {
    var record = null;
    var type = null;
    for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
      record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
      type = record.addrType;

      if (typeof (type) == 'object') {
        type = type[0];
      }

      if (type == 'ZS01') {
        toggleCATaxFieldsByProvCd(record.stateProv);
        return;
      }
    }
  } else {
    clearCATaxFields();
  }
}

function toggleCATaxFieldsByProvCd(provCd) {
  if (provCd == 'QC') {
    // Show QST
    FormManager.clearValue('PSTExempt');
    FormManager.getField('PSTExempt').set('checked', false);
    FormManager.clearValue('PSTExemptLicNum');
    FormManager.clearValue('AuthExemptType');
    FormManager.removeValidator('PSTExemptLicNum', Validators.REQUIRED);

    FormManager.readOnly('PSTExempt');
    FormManager.readOnly('PSTExemptLicNum');
    FormManager.readOnly('AuthExemptType');
    FormManager.enable('QST');
  } else if (provCd == 'BC' || provCd == 'MB' || provCd == 'SK') {
    FormManager.clearValue('QST');
    // PST Exempt Flag, PST Exemp Lic No
    FormManager.enable('PSTExempt');
    FormManager.enable('PSTExemptLicNum');
    FormManager.enable('AuthExemptType');
    FormManager.readOnly('QST');

  } else {
    clearCATaxFields();
  }
}

/**
 * Clear all the CA Tax Fields PST Exempt, PST Exempt Lic No, QST
 * 
 * @returns
 */
function clearCATaxFields() {

  FormManager.clearValue('PSTExempt');
  FormManager.getField('PSTExempt').set('checked', false);
  FormManager.clearValue('PSTExemptLicNum');
  FormManager.clearValue('AuthExemptType');
  FormManager.clearValue('QST');

  FormManager.readOnly('PSTExempt');
  FormManager.readOnly('PSTExemptLicNum');
  FormManager.readOnly('AuthExemptType');
  FormManager.readOnly('QST');
  FormManager.removeValidator('PSTExemptLicNum', Validators.REQUIRED);
}

function addPSTExemptValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var installAtAddr = getAddressByType('ZS01');
        if (installAtAddr != null && (installAtAddr.stateProv == 'BC' || installAtAddr.stateProv == 'MB' || installAtAddr.stateProv == 'KS')) {
          var pstExempt = FormManager.getActualValue('PSTExempt');
          // check if pstExempt value is X
          if (pstExempt != null && pstExempt == 'Y') {
            var pstExemptLicNum = FormManager.getActualValue('PSTExemptLicNum');
            if (pstExemptLicNum == '') {
              return new ValidationResult(null, false, 'PST Exemption License Number is required');
            } else {
              return new ValidationResult(null, true);
            }

          } else {
            return new ValidationResult(null, true);
          }
        }
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

/**
 * Checks if the address type is already present in the Address Grid
 * 
 * @param addrType
 * @returns
 */
function isAddressInGrid(addrType) {
  var record = null;
  var type = null;
  for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
    record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
    type = record.addrType;

    if (typeof (type) == 'object') {
      type = type[0];
    }

    if (type == addrType) {
      return true;
    }
  }
  return false;
}

function getAddressByType(addrType) {

  var record = null;
  var type = null;
  for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
    record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
    type = record.addrType;

    if (typeof (type) == 'object') {
      type = type[0];
    }

    if (type == addrType) {
      return record;
    }
  }
  return null;
}

/**
 * Toggles the COPY function on the address tab if there are already 2 addresses -
 * for initial release only
 * 
 * @param value
 * @param rowIndex
 * @param grid
 * @returns
 */
function canCopyAddress(value, rowIndex, grid) {
  return true;
}

/**
 * Toggles the REMOVE function on the address tab and prevents removing imported
 * address in update requests
 * 
 * @param value
 * @param rowIndex
 * @param grid
 * @returns
 */
function canRemoveAddress(value, rowIndex, grid) {
  var rowData = grid.getItem(rowIndex);
  var importInd = rowData.importInd[0];
  var reqType = FormManager.getActualValue('reqType');
  if ('U' == reqType && 'Y' == importInd) {
    return false;
  }
  return true;
}

function ADDRESS_GRID_showCheck(value, rowIndex, grid) {
  return canRemoveAddress(value, rowIndex, grid);
}

/**
 * After configuration for Canada Add the scripts here and not via
 * addAfterConfig calls to
 */
function afterConfigForCA() {
  if (role.toUpperCase() == 'VIEWER') {
    FormManager.readOnly('abbrevLocn');
    FormManager.readOnly('abbrevNm');
  }

  addFieldHandlers();
}

var _inacCodeHandler = null;
var _custSubGrpHandler = null;
function addFieldHandlers() {

  if (_inacCodeHandler == null) {
    _inacCodeHandler = dojo.connect(FormManager.getField('inacCd'), 'onChange', function(value) {
      if (value.match(regexNumeric) && value.length == 4) {
        FilteringDropdown['inacType'] = 'I';
        FormManager.setValue('inacType', 'I');
      } else if (value.match(regexNumeric) && value.length < 4) {
        FilteringDropdown['inacType'] = '';
        FormManager.setValue('inacType', '');
      } else if (value != null && value.match(regexAlphanumeric)) {
        FilteringDropdown['inacType'] = 'N';
        FormManager.setValue('inacType', 'N');
      } else {
        FilteringDropdown['inacType'] = '';
        FormManager.setValue('inacType', '');
      }
    });
  }

  if (_custSubGrpHandler == null) {
    _custSubGrpHandler = dojo.connect(FormManager.getField('custSubGrp'), 'onChange', function(value) {
      if (FormManager.getActualValue('reqType') == 'U') {
        return;
      }

      var custSubGrp = FormManager.getActualValue('custSubGrp');
      if (custSubGrp == 'OEM') {
        FormManager.enable('abbrevNm');
      }
    });
  }

}

var _pstExemptHandler = null;
function addPSTExemptHandler() {

  if (dojo.byId('PSTExempt').checked) {
    try {
      dojo.destroy('ast-PSTExemptLicNum');
    } catch (err) {
      console.log('Catch error destroying ast-PSTExemptLicNum element');
    }

    var label = dojo.query('label[for="PSTExemptLicNum"]');
    var mand = '<span style="color:red" class="cmr-ast" id="ast-PSTExemptLicNum">* </span>';
    if (label && label[0]) {
      var change = dojo.query(label[0]).query('img.cmr-delta-icon');
      if (change && change[0]) {
        dojo.place(mand, change[0], 'before');
      } else {
        var info = dojo.query(label[0]).query('img.cmr-info-bubble');
        if (info && info[0]) {
          dojo.place(mand, info[0], 'before');
        } else {
          dojo.place(mand, label[0], 'last');
        }
      }
    }
  } else {// PST Exempt NOT checked
    var reqMarker = dojo.query('ast-PSTExemptLicNum');
    if (reqMarker && reqMarker[0]) {
      dojo.byId('ast-PSTExemptLicNum').style.display = 'none';
    }
  }

  if (_pstExemptHandler == null) {
    _pstExemptHandler = dojo.connect(FormManager.getField('PSTExempt'), 'onClick', function(value) {
      if (dojo.byId('PSTExempt').checked) {
        FormManager.addValidator('PSTExemptLicNum', Validators.REQUIRED, [ 'PST Exemption License Number' ], 'MAIN_CUST_TAB');
        dojo.byId('ast-PSTExemptLicNum').style.display = 'inline-block';
      } else {
        FormManager.removeValidator('PSTExemptLicNum', Validators.REQUIRED);
        dojo.byId('ast-PSTExemptLicNum').style.display = 'none';
      }
    });
  }
}

function addLocationNoValidator() {
  FormManager.addFormValidator(
      (function() {
        return {
          validate : function() {
            var CND_LOC_CD = [ 'AG000', 'AI000', 'AW000', 'BS000', 'BB000', 'BM000', 'BQ000', 'BV000', 'CW000', 'DM000', 'DO000', 'GD000', 'GP000', 'GY000', 'HT000', 'KN000', 'KY000', 'JM000',
                'LC000', 'MQ000', 'MS000', 'PR000', 'SR000', 'SX000', 'TC000', 'TT000', 'VC000', 'VG000' ];
            var CND_CNTRY_CODE = [ 'AG', 'AI', 'AW', 'BS', 'BB', 'BM', 'BQ', 'BV', 'CW', 'DM', 'DO', 'GD', 'GP', 'GY', 'HT', 'KN', 'KY', 'JM', 'LC', 'MQ', 'MS', 'PR', 'SR', 'SX', 'TC', 'TT', 'VC',
                'VG' ];
            var custGrp = FormManager.getActualValue('custGrp');
            var custSubGrp = FormManager.getActualValue('custSubGrp');
            var locationNumber = FormManager.getActualValue('locationNumber');

            for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
              record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
              if (record == null && _allAddressData != null && _allAddressData[i] != null) {
                record = _allAddressData[i];
              }
              type = record.addrType;
              if (typeof (type) == 'object') {
                type = type[0];
              }
              if (type == 'ZS01') {
                var landCntry = record.landCntry[0];
                if ((CND_LOC_CD.indexOf(locationNumber) == -1) && (CND_CNTRY_CODE.indexOf(landCntry) > -1) && custGrp == 'CROSS') {
                  return new ValidationResult(null, false, 'Invalid Location Code for Caribbean Countries.');
                } else if (custGrp == 'LOCAL' && (CND_LOC_CD.indexOf(locationNumber) > -1) && landCntry == 'CA') {
                  return new ValidationResult(null, false, 'Invalid Location Code.');
                } else if (custSubGrp == 'USA' && locationNumber != '99999') {
                  return new ValidationResult(null, false, 'Invalid Location Code.');
                } else {
                  return new ValidationResult(null, true);
                }
              }
            }
          }
        };
      })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function removeValidatorForOptionalFields() {
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var reqType = FormManager.getActualValue('reqType');

  if (viewOnlyPage == 'true' || reqType != 'C') {
    return;
  }

  if (role == 'REQUESTER') {
    FormManager.removeValidator('taxCd2', Validators.REQUIRED);
    FormManager.removeValidator('salesBusOffCd', Validators.REQUIRED);
    FormManager.removeValidator('installBranchOff', Validators.REQUIRED);
    FormManager.removeValidator('salesTeamCd', Validators.REQUIRED);
    FormManager.removeValidator('repTeamMemberNo', Validators.REQUIRED);
    FormManager.removeValidator('invoiceDistCd', Validators.REQUIRED);
    FormManager.removeValidator('QST', Validators.REQUIRED);
    FormManager.removeValidator('creditCd', Validators.REQUIRED);
    FormManager.removeValidator('cusInvoiceCopies', Validators.REQUIRED);
    // FormManager.removeValidator('subIndustryCd', Validators.REQUIRED);
    // FormManager.removeValidator('isicCd', Validators.REQUIRED);
  }
}

function addPhoneNumberValidationCa() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var phoneNumber = FormManager.getActualValue('custPhone');
        var custGrp = FormManager.getActualValue('custGrp');

        if (custGrp != 'CROSS' && phoneNumber.length > 0 && !phoneNumber.match("([0-9]{3}-[0-9]{3}-[0-9]{4})$")) {
          return new ValidationResult({
            id : 'custPhone',
            type : 'text',
            name : 'custPhone'
          }, false, 'Invalid format of Phone Number. Format should be NNN-NNN-NNNN.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function retainImportValues(fromAddress, scenario, scenarioChanged) {
  var isCmrImported = getImportedIndc();
  var reqId = FormManager.getActualValue('reqId');

  if (FormManager.getActualValue('reqType') == 'C' && isCmrImported == 'Y' && scenarioChanged) {
    if (scenario == 'COMME' || scenario == 'GOVT') {
      var origSbo;
      var origRepTeam; // note: not sure if mapping is correct
      var origIsic;
      var origSubInd;
      var origInac;
      var origEfc;
      var origIbo;
      var origArFaar;
      var origCreditCode;

      var result = cmr.query("GET.CMRINFO.IMPORTED_CA", {
        REQ_ID : reqId
      });

      if (result != null && result != '') {
        origSbo = result.ret1;
        origRepTeam = result.ret2;
        origIsic = result.ret3;
        origSubInd = result.ret4;
        origInac = result.ret5;
        origEfc = result.ret6;
        origIbo = result.ret7;
        origArFaar = result.ret8;
        origCreditCode = result.ret9;

        FormManager.setValue('salesBusOffCd', origSbo);
        FormManager.setValue('repTeamMemberNo', origRepTeam);
        FormManager.setValue('isicCd', origIsic);
        FormManager.setValue('subIndustryCd', origSubInd);
        FormManager.setValue('inacCd', origInac);
        FormManager.setValue('taxCd1', origEfc);
        FormManager.setValue('installBranchOff', origIbo);
        FormManager.setValue('adminDeptCd', origArFaar);
        FormManager.setValue('creditCd', origCreditCode);
      }
    }
  }
}

function getImportedIndc() {
  if (_importedIndc) {
    return _importedIndc;
  }
  var results = cmr.query('VALIDATOR.IMPORTED', {
    REQID : FormManager.getActualValue('reqId')
  });
  if (results != null && results.ret1) {
    _importedIndc = results.ret1;
  } else {
    _importedIndc = 'N';
  }
  return _importedIndc;
}

function setDefaultInvoiceCopies(fromAddress, scenario, scenarioChanged) {
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  var reqType = FormManager.getActualValue('reqType');
  var invoiceCopies = FormManager.getActualValue('cusInvoiceCopies');
  if (viewOnlyPage == 'true' || reqType != 'C') {
    return;
  }
  if (invoiceCopies.length == 0) {
    FormManager.setValue('cusInvoiceCopies', '01');
  }
}

function addNumberOfInvoiceValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var invoiceCopies = FormManager.getActualValue('cusInvoiceCopies');
        if (invoiceCopies.length > 0 && !invoiceCopies.match("([0-9]{2})$")) {
          return new ValidationResult({
            id : 'cusInvoiceCopies',
            type : 'text',
            name : 'cusInvoiceCopies'
          }, false, invoiceCopies + ' is not a valid value for Number of Invoices.  Format should be NN.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function mappingAddressField(key) {
  var value = '';
  if (key == 'NL') {
    value = 'A';
  } else if (key == 'NS') {
    value = 'B';
  } else if (key == 'PE') {
    value = 'C';
  } else if (key == '99') {
    value = 'D';
  } else if (key == 'NB') {
    value = 'E';
  } else if (key == 'QC') {
    value = [ 'G', 'H', 'J' ];
  } else if (key == 'ON') {
    value = [ 'K', 'L', 'M', 'N', 'P', 'W' ];
  } else if (key == 'MB') {
    value = 'R';
  } else if (key == 'SK') {
    value = 'S';
  } else if (key == 'AB') {
    value = 'T';
  } else if (key == 'BC') {
    value = 'V';
  } else if (key == 'YT') {
    value = 'Y';
  } else if (key == 'NU') {
    value = [ 'X0A', 'X0B', 'X0C' ];
  } else if (key == 'NT') {
    value = [ 'X0E', 'X0G', 'X1A' ];
  }
  return value;
}

function addProvincePostalCdValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var NU_NT_PROV = [ 'NU', 'NT' ];
        var landCntry = FormManager.getActualValue('landCntry');
        var postCd = FormManager.getActualValue('postCd');
        var stateProv = FormManager.getActualValue('stateProv');

        if (stateProv != '' && landCntry == 'CA' && postCd != '') {
          if ((mappingAddressField(stateProv).indexOf(postCd.substring(0, 1)) == -1) && NU_NT_PROV.indexOf(stateProv) == -1) {
            return new ValidationResult(null, false, 'Invalid Postal Code, the first character should be ' + mappingAddressField(stateProv));
          } else if ((mappingAddressField(stateProv).indexOf(postCd.substring(0, 3)) == -1) && NU_NT_PROV.indexOf(stateProv) != -1) {
            return new ValidationResult(null, false, 'Invalid Postal Code, the first 3 characters should be ' + mappingAddressField(stateProv));
          }
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function addINACValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var inacType = FormManager.getActualValue('inacType');
        var inac = FormManager.getActualValue('inacCd');
        if (inac != '') {
          var message = '';
          var qParams = {
            FIELD_ID : "##INACCode",
            CMR_ISSUING_CNTRY : SysLoc.CANADA,
            CD : FormManager.getActualValue('inacCd'),
          };
          var result = cmr.query('CHECKLOV', qParams);
          if (inacType == 'I') {
            message = 'INAC value is invalid.';
          } else {
            message = 'NAT value is invalid.';
          }
          if (result.ret1 != 1) {
            return new ValidationResult(null, false, message);
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

/*
 * AbbrevName, AbbrevLocation fields locked for REQUESTER
 */
function lockAbbrevNmLocn() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (role == 'REQUESTER') {
    if (reqType != 'U') {
      FormManager.readOnly('abbrevNm');
      FormManager.readOnly('abbrevLocn');
    }
  } else {
    FormManager.enable('abbrevNm');
    FormManager.enable('abbrevLocn');
  }
}

/* Register CA Javascripts */
dojo.addOnLoad(function() {
  console.log('adding CA scripts...');

  // validators - register one each
  GEOHandler.registerValidator(addAddressRecordTypeValidator, [ SysLoc.CANADA ], null, true);
  GEOHandler.registerValidator(addInacCdValidator, [ SysLoc.CANADA ], null, true);
  GEOHandler.registerValidator(addChangeNameAttachmentValidator, [ SysLoc.CANADA ], null, true);
  GEOHandler.registerValidator(addDPLCheckValidator, [ SysLoc.CANADA ], GEOHandler.ROLE_REQUESTER, true);
  GEOHandler.registerValidator(addDPLAssessmentValidator, [ SysLoc.CANADA ], GEOHandler.ROLE_REQUESTER, true);
  GEOHandler.registerValidator(addLocationNoValidator, [ SysLoc.CANADA ], null, true);
  GEOHandler.registerValidator(addPhoneNumberValidationCa, [ SysLoc.CANADA ], null, true);
  // GEOHandler.registerValidator(addPSTExemptValidator, [ SysLoc.CANADA ],
  // null, true);
  GEOHandler.registerValidator(addNumberOfInvoiceValidator, [ SysLoc.CANADA ], null, true);
  GEOHandler.registerValidator(addProvincePostalCdValidator, [ SysLoc.CANADA ], null, true);
  // NOTE: do not add multiple addAfterConfig calls to avoid confusion, club the
  // functions on afterConfigForCA
  GEOHandler.addAfterConfig(afterConfigForCA, [ SysLoc.CANADA ]);
  GEOHandler.addAfterConfig(lockAbbrevNmLocn, [ SysLoc.CANADA ]);
  // GEOHandler.addToggleAddrTypeFunction(toggleAddrTypesForCA, [ SysLoc.CANADA
  // ]);
  GEOHandler.addAddrFunction(addCAAddressHandler, [ SysLoc.CANADA ]);
  GEOHandler.enableCopyAddress(SysLoc.CANADA);
  GEOHandler.addAfterTemplateLoad(removeValidatorForOptionalFields, SysLoc.CANADA);
  GEOHandler.addAfterTemplateLoad(retainImportValues, SysLoc.CANADA);
  GEOHandler.addAfterTemplateLoad(toggleCATaxFields, SysLoc.CANADA);
  GEOHandler.addAfterTemplateLoad(setDefaultInvoiceCopies, SysLoc.CANADA);
  GEOHandler.addAfterTemplateLoad(addPSTExemptHandler, SysLoc.CANADA);
  GEOHandler.addToggleAddrTypeFunction(hideObsoleteAddressOption, [ SysLoc.CANADA ]);
});
