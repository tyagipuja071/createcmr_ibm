/*

Validations file for Canada

 */

var regexNumeric = /^[0-9]+$/;
var regexAlphanumeric = /^[0-9a-zA-Z]+$/;
/**
 * Adds the validator for the Install At and optional Invoice To
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
          if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount < 2) {
            return new ValidationResult(null, false, 'Please add Install At and Invoice To address to this request.');
          }

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
          if (installAtCnt != 1 || invoiceToCnt != 1) {
            return new ValidationResult(null, false, 'The request should contain both Install At address and Invoice To address.');
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
 * Toggles the Install At and Invoice To choices depending on the current
 * address records
 * 
 * @param cntry
 * @param addressMode
 * @param details
 * @returns
 */
function toggleAddrTypesForCA(cntry, addressMode, details) {
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
    var reqType = FormManager.getActualValue('reqType');
    if (reqType == 'C') {
      cmr.hideNode('radiocont_ZP01');
    } else {
      cmr.showNode('radiocont_ZP01');
    }
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

  if (cmr.currentRequestType == 'U') {
    cmr.hideNode('radiocont_ZS01');

    // Some cmr's only has ZS01 so we will show invoice if not present
    if (isAddressInGrid('ZI01')) {
      cmr.hideNode('radiocont_ZI01');
    } else {
      cmr.showNode('radiocont_ZI01');
    }
  }
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
  if (role.toUpperCase() == 'REQUESTER' || role.toUpperCase() == 'VIEWER') {
    FormManager.readOnly('abbrevLocn');
    FormManager.readOnly('abbrevNm');
  } else {
    FormManager.enable('abbrevLocn');
    FormManager.enable('abbrevNm');
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

/* Register CA Javascripts */
dojo.addOnLoad(function() {
  console.log('adding CA scripts...');

  // validators - register one each
  GEOHandler.registerValidator(addAddressRecordTypeValidator, [ SysLoc.CANADA ], null, true);
  GEOHandler.registerValidator(addInacCdValidator, [ SysLoc.CANADA ], null, true);
  GEOHandler.registerValidator(addChangeNameAttachmentValidator, [ SysLoc.CANADA ], null, true);
  GEOHandler.registerValidator(addDPLCheckValidator, [ SysLoc.CANADA ], GEOHandler.ROLE_REQUESTER, true);
  GEOHandler.registerValidator(addDPLAssessmentValidator, [ SysLoc.CANADA ], GEOHandler.ROLE_REQUESTER, true);

  // NOTE: do not add multiple addAfterConfig calls to avoid confusion, club the
  // functions on afterConfigForCA
  GEOHandler.addAfterConfig(afterConfigForCA, [ SysLoc.CANADA ]);

  GEOHandler.addToggleAddrTypeFunction(toggleAddrTypesForCA, [ SysLoc.CANADA ]);
  GEOHandler.addAddrFunction(addCAAddressHandler, [ SysLoc.CANADA ]);
  GEOHandler.enableCopyAddress(SysLoc.CANADA);
});
