/*
 * File: us_validations.js
 * Description:
 * Contains the specific validations and configuration adjustments for US (897)
 */

var _usSicmenHandler = null;
/**
 * Adds the validator for Invoice-to that only 3 address lines can be specified
 */
function addInvoiceAddressLinesValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        // // no validation for non invoice to
        if (FormManager.getActualValue('addrType') != 'ZI01') {
          return new ValidationResult(null, true);
        }

        var addrLines = 0;
        if (FormManager.getActualValue('addrTxt') != '') {
          addrLines++;
        }
        if (FormManager.getActualValue('addrTxt2') != '') {
          addrLines++;
        }
        if (FormManager.getActualValue('divn') != '') {
          addrLines++;
        }
        if (FormManager.getActualValue('dept') != '') {
          addrLines++;
        }
        if (addrLines > 3) {
          return new ValidationResult(null, false, 'Please fill-out only 3 of the following fields: Address, Address Con\'t, Division, Department.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

/**
 * Adds the validator for Restrict To
 */
function addInvoiceAddressLinesValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getField('restrictInd').get('checked') && FormManager.getActualValue('restrictTo') == '') {
          return new ValidationResult({
            id : 'restrictTo',
            type : 'text',
            name : 'restrictTo'
          }, false, 'Restrict To is required for a Restricted customer.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

/**
 * Validates that the request only has exactly 1 Install-to and at most 1
 * Invoice-to
 */
function addAddressRecordTypeValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('cmrIssuingCntry') != SysLoc.USA) {
          return new ValidationResult(null, true);
        }
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount == 0) {
          return new ValidationResult(null, false, 'Please add an Install At and optionally an Invoice To address to this request.');
        }
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          var record = null;
          var type = null;
          var invoiceToCnt = 0;
          var installAtCnt = 0;
          for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
            record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
            type = record.addrType;
            if (typeof (type) == 'object') {
              type = type[0];
            }
            if (type == 'ZS01') {
              installAtCnt++;
            } else if (type == 'ZI01') {
              invoiceToCnt++;
            }
          }
          // if (installAtCnt != 1 || CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount >
          // 2) {
          // return new ValidationResult(null, false, 'The request should
          // contain exactly one Install At address and one optional Invoice To
          // address.');
          // }
          if (installAtCnt != 1 || invoiceToCnt > 1) {
            return new ValidationResult(null, false, 'The request should contain exactly one Install At address and one optional Invoice To address.');
          } else {
            return new ValidationResult(null, true);
          }
        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');

}

/**
 * After configuration for US
 */
function afterConfigForUS() {
  // PPS CEID field as mandatory for BP scenario
  var custTypeHandler = null;
  if (custTypeHandler == null) {
    var custTypeHandler = dojo.connect(FormManager.getField('custType'), 'onChange', function(value) {
      if (FormManager.getActualValue('userRole').toUpperCase() == 'REQUESTER' && FormManager.getActualValue('reqType') == 'C' && FormManager.getActualValue('custType') == '7') {
        FormManager.addValidator('ppsceid', Validators.REQUIRED, [ 'PPSCEID' ], 'MAIN_IBM_TAB');
      } else {
        FormManager.removeValidator('ppsceid', Validators.REQUIRED);
      }
    });
  }

  var usCntryHandler = null;
  if (usCntryHandler == null) {
    // set the postal code to 0000 for non-US landed countries and disable
    usCntryHandler = dojo.connect(FormManager.getField('landCntry'), 'onChange', function(value) {
      if (FormManager.getActualValue('landCntry') != '' && FormManager.getActualValue('landCntry') != 'US') {
        FormManager.setValue('postCd', '00000');
        FormManager.readOnly('postCd');
      } else {
        var readOnly = false;
        try {
          if (TemplateService.isHandled('postCd')) {
            if ((typeof TemplateService != "undefined") && (typeof PageManager != "undefined")) {
              readOnly = TemplateService.isFieldReadOnly('postCd');
              if ((readOnly || PageManager.isReadOnly())) {
                FormManager.readOnly('postCd');
              } else {
                FormManager.enable('postCd');
              }
            }
          }
        } catch (e) {
          FormManager.enable('postCd');
        }
      }
    });
  }

  if (_usSicmenHandler == null) {
    _usSicmenHandler = dojo.connect(FormManager.getField('usSicmen'), 'onChange', function(value) {
      var sicmen = FormManager.getActualValue('usSicmen');
      var _custType = FormManager.getActualValue('custSubGrp');
      if (_custType == 'OEMHW' || _custType == 'OEM-SW' || _custType == 'TPD' || _custType == 'SSD' || _custType == 'DB4') {
        FormManager.setValue('isicCd', '357X');
      } else {
        var currIsic = FormManager.getActualValue('isicCd');
        if (currIsic != '357X') {
          FormManager.setValue('isicCd', sicmen);
        }
      }
    });
  }
  _usSicmenHandler[0].onChange();
}

function initUSTemplateHandler() {
  // templates/scenarios initialization. connect onchange of the subgroup
  // to load the template
  if (_templateHandler == null && FormManager.getField('custSubGrp')) {
    _templateHandler = dojo.connect(FormManager.getField('custSubGrp'), 'onChange', function(value) {
      if (value && value != '') {
        TemplateService.fill('reqentry');
      }
    });
  }
  if (_templateHandler && _templateHandler[0]) {
    _templateHandler[0].onChange();
  }
}

function addUSAddressHandler(cntry, addressMode, saving) {
  if (!saving) {
    if (addressMode == 'newAddress') {
      FilteringDropdown['val_landCntry'] = 'US';
      FormManager.setValue('landCntry', 'US');
    } else {
      FilteringDropdown['val_landCntry'] = null;
    }
  }
}

function toggleAddrTypesForUS(cntry, addressMode, details) {
  var reqType = FormManager.getActualValue('reqType');
  if (addressMode == 'newAddress' || addressMode == 'copyAddress') {
    if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
      var firstRecord = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(0);
      var type = firstRecord.addrType;
      if (typeof (type) == 'object') {
        type = type[0];
      }
      if (type == 'ZS01') {
        FormManager.setValue('addrType', 'ZI01');
        cmr.showNode('radiocont_ZI01');
        cmr.hideNode('radiocont_ZS01');
      } else if (type == 'ZI01') {
        FormManager.setValue('addrType', 'ZS01');
        cmr.showNode('radiocont_ZS01');
        cmr.hideNode('radiocont_ZI01');
      }
    } else {
      FormManager.setValue('addrType', 'ZS01');
      cmr.showNode('radiocont_ZS01');
      cmr.hideNode('radiocont_ZI01');
    }
  }
  // CMR - 6072
  if (reqType == 'U' && (cmr.addressMode == 'updateAddress' || cmr.addressMode == 'copyAddress')) {
    cmr.showNode('radiocont_ZP01');
  } else {
    cmr.hideNode('radiocont_ZP01');
  }
}

function setCSPValues(fromAddress, scenario, scenarioChanged) {
  if (scenario == 'CSP') {
    FormManager.setValue('isuCd', '32');
    FormManager.setValue('clientTier', 'N');
    FormManager.readOnly('isuCd');
  } else {
    FormManager.enable('isuCd');
  }
}

function enableUSSicMenForScenarios(fromAddress, scenario, scenarioChanged) {
  var reqType = FormManager.getActualValue('reqType');
  var viewOnly = FormManager.getActualValue('viewOnlyPage');
  if (viewOnly != '' && viewOnly == 'true') {
    return;
  }
  var role = null;
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }
  if (reqType == 'C') {
    FormManager.readOnly('isicCd');
  } else if (reqType == 'U') {
    if (role == 'Processor') {
      FormManager.enable('isicCd');
    } else {
      FormManager.readOnly('isicCd');
    }
  } else {
    FormManager.readOnly('isicCd');
  }
}

function canUpdateAddress(value, rowIndex, grid) {
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  console.log(value + ' - ' + rowIndex);
  var rowData = grid.getItem(0);
  if (rowData == null) {
    return '';
  }
  rowData = grid.getItem(rowIndex);
  var addrType = rowData.addrType;
  if (addrType == 'ZP01' && role == 'REQUESTER') {
    return false;
  } else {
    return true;
  }
}

function canCopyAddress(value, rowIndex, grid) {
  var reqType = FormManager.getActualValue('reqType');
  console.log(value + ' - ' + rowIndex);
  var rowData = grid.getItem(0);
  if (rowData == null) {
    return '';
  }
  rowData = grid.getItem(rowIndex);
  var addrType = rowData.addrType;
  if (addrType == 'ZP01') {
    return false;
  } else {
    return true;
  }
}

function canRemoveAddress(value, rowIndex, grid) {
  var reqType = FormManager.getActualValue('reqType');
  console.log(value + ' - ' + rowIndex);
  var rowData = grid.getItem(0);
  if (rowData == null) {
    return '';
  }
  rowData = grid.getItem(rowIndex);
  var addrType = rowData.addrType;
  if (addrType == 'ZP01') {
    return false;
  } else {
    return true;
  }
}

/* Register US Javascripts */
dojo.addOnLoad(function() {
  console.log('adding US scripts...');
  GEOHandler.registerValidator(addInvoiceAddressLinesValidator, [ SysLoc.USA ], null, true);
  GEOHandler.registerValidator(addAddressRecordTypeValidator, [ SysLoc.USA ], null, true);
  GEOHandler.addAfterConfig(afterConfigForUS, [ SysLoc.USA ]);
  GEOHandler.addAfterConfig(initUSTemplateHandler, [ SysLoc.USA ]);
  GEOHandler.addAddrFunction(addUSAddressHandler, [ SysLoc.USA ]);
  GEOHandler.addToggleAddrTypeFunction(toggleAddrTypesForUS, [ SysLoc.USA ]);
  GEOHandler.addAfterTemplateLoad(setCSPValues, [ SysLoc.USA ]);
  GEOHandler.addAfterTemplateLoad(enableUSSicMenForScenarios, [ SysLoc.USA ]);
  GEOHandler.addAfterConfig(enableUSSicMenForScenarios, [ SysLoc.USA ]);
});