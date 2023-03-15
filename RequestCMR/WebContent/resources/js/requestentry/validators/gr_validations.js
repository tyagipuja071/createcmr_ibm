/* Register Greece Javascripts */

var addrTypeHandler = [];
var _importedIndc = null;
var _isuCdHandler = null;
var _custSubTypeHandler = null;
var _custSubTypeHandlerGr = null;
var _subindustryCdHandler = null;
var _custSalesRepHandlerGr = null;
var _isuCdHandler = null;
var _ISUHandler = null;
var _CTCHandler = null;
var _oldIsu = null;
var _oldClientTier = null;
var _oldIsuCd = null;
var _oldCtc = null;
var _oldEnt = null;

function addAfterConfigGR() {
  console.log(">>>> addAfterConfigGR ");
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  if (viewOnlyPage == 'true') {
    FormManager.readOnly('inacCd');
    FormManager.readOnly('enterprise');
    FormManager.readOnly('salesTeamCd');
    FormManager.readOnly('repTeamMemberNo');
    FormManager.readOnly('salesBusOffCd');
    FormManager.readOnly('clientTier');
  }

  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (role == 'REQUESTER') {
    FormManager.readOnly('abbrevNm');
    FormManager.readOnly('abbrevLocn');
  }

  if (FormManager.getActualValue('reqType') == 'C') {
    FormManager.getField('capInd').set('checked', true);
    FormManager.readOnly('capInd');
    FormManager.readOnly('collectionCd2');
    FormManager.setValue('collectionCd2', '');
  }

  if (FormManager.getActualValue('reqType') == 'C') {
    FormManager.hide('ModeOfPayment', 'modeOfPayment');
  } else if (FormManager.getActualValue('reqType') == 'U') {
    FormManager.show('ModeOfPayment', 'modeOfPayment');
  }

  FormManager.readOnly('salesTeamCd');
  FormManager.readOnly('subIndustryCd');
  // CREATCMR-788
  addressQuotationValidatorGR();
  lockUnlockField();
}

function addISUHandler() {
  console.log(">>>> addISUHandler");
  _oldIsu = FormManager.getActualValue('isuCd');
  _oldClientTier = FormManager.getActualValue('clientTier');
  getExitingValueOfCTCAndIsuCD();
  addRemoveValidator();
  lockUnlockField();
  if (_ISUHandler == null) {
    _ISUHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
      if (_oldIsu != FormManager.getActualValue('isuCd') || (typeof (_pagemodel) != 'undefined' && _pagemodel['custSubGrp'] != FormManager.getActualValue('custSubGrp'))) {
        setClientTierValuesGR();
        _oldIsu = FormManager.getActualValue('isuCd');
      }
    });
  }
  if (_CTCHandler == null) {
    _CTCHandler = dojo.connect(FormManager.getField('clientTier'), 'onChange', function(value) {
      if (_oldClientTier != FormManager.getActualValue('clientTier') || (typeof (_pagemodel) != 'undefined' && _pagemodel['custSubGrp'] != FormManager.getActualValue('custSubGrp'))) {
        setEnterpriseValues();
        _oldClientTier = FormManager.getActualValue('clientTier');
      }
    });
  }

  // if (_custSubTypeHandlerGr == null) {
  // _custSubTypeHandlerGr = dojo.connect(FormManager.getField('custSubGrp'),
  // 'onChange', function(value) {
  // setClientTierValuesGR();
  // });
  // }

}

function getImportedIndcForGreece() {
  console.log(">>>> getImportedIndcForGreece ");
  if (_importedIndc) {
    return _importedIndc;
  }
  var results = cmr.query('VALIDATOR.IMPORTED_GR', {
    REQID : FormManager.getActualValue('reqId')
  });
  if (results != null && results.ret1) {
    _importedIndc = results.ret1;
  } else {
    _importedIndc = 'N';
  }
  return _importedIndc;
}

function addEMEALandedCountryHandler(cntry, addressMode, saving, finalSave) {
  console.log(">>>> addEMEALandedCountryHandler ");
  if (!saving) {
    if (addressMode == 'newAddress') {
      FilteringDropdown['val_landCntry'] = FormManager.getActualValue('defaultLandedCountry');
      FormManager.setValue('landCntry', FormManager.getActualValue('defaultLandedCountry'));
    } else {
      FilteringDropdown['val_landCntry'] = null;
    }
  }

  var landCntry = FormManager.getActualValue('landCntry');
  if (landCntry == 'GR') {
    var scenario = FormManager.getActualValue('custGrp');
    if ((scenario == 'LOCAL' || FormManager.getActualValue('reqType') == 'U') && FormManager.getActualValue('addrType') == 'ZP01') {
      GEOHandler.disableCopyAddress();
    } else {
      GEOHandler.enableCopyAddress([ SysLoc.GREECE ], validateEMEACopy, [ 'ZD01', 'ZI01' ]);
    }
  }
}

/*
 * EmbargoCode field locked for REQUESTER
 */
function lockEmbargo() {
  console.log(">>>> lockEmbargo ");
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var issu_cntry = FormManager.getActualValue('cmrIssuingCntry');
  if (role == 'REQUESTER') {
    if ((issu_cntry == '866' || issu_cntry == '754' || issu_cntry == '758') && (FormManager.getActualValue('reqType') == 'U' || FormManager.getActualValue('reqType') == 'X')) {
      FormManager.enable('embargoCd');
    } else {
      FormManager.readOnly('embargoCd');
    }
  } else {
    FormManager.enable('embargoCd');
  }
}

function addOccupationPOBoxAttnPersonValidatorForGR() {
  console.log(">>>> addOccupationPOBoxAttnPersonValidatorForGR ");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var filledCount = 0;

        if (FormManager.getActualValue('cmrIssuingCntry') != SysLoc.GREECE) {
          return new ValidationResult(null, true);
        }

        if (FormManager.getActualValue('addrTxt') != '') {
          filledCount++;
        }

        if (FormManager.getActualValue('addrTxt2') != '') {
          filledCount++;
        }

        if (FormManager.getActualValue('poBox') != '') {
          filledCount++;
        }

        if (FormManager.getActualValue('custNm4') != '') {
          filledCount++;
        }

        if (filledCount > 2) {
          return new ValidationResult(null, false, 'Street Address,  Address Con\'t/Occupation, PO BOX, and Att. Person only 2 can be filled at the same time');
        }

        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function addStreetAddressFormValidatorGR() {
  console.log(">>>> addStreetAddressFormValidatorGR ");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('cmrIssuingCntry') != SysLoc.GREECE) {
          return new ValidationResult(null, true);
        }
        if (FormManager.getActualValue('addrTxt') == '' && FormManager.getActualValue('poBox') == '') {
          return new ValidationResult(null, false, 'Please fill-out either Street Address or PO Box.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function addCrossLandedCntryFormValidatorGR() {
  console.log(">>>> addCrossLandedCntryFormValidatorGR ");

  FormManager.addFormValidator((function() {
    return {
      validate : function() {

        var isCrossborder = cmr.oldlandcntry != 'GR' && FormManager.getActualValue('reqType') == 'U';
        if ((FormManager.getActualValue('custGrp') == 'CROSS' || isCrossborder) && (FormManager.getActualValue('addrType') == 'ZP01' || FormManager.getActualValue('addrType') == 'ZS01')
            && FormManager.getActualValue('landCntry') == 'GR') {
          return new ValidationResult(null, false, 'Landed Country value should not be \'Greece - GR\' for Cross-border customers.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function clearPhoneNoFromGrid() {
  console.log(">>>> clearPhoneNoFromGrid ");
  for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
    recordList = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
    if (_allAddressData != null && _allAddressData[i] != null) {
      if (!(_allAddressData[i].addrType[0] == 'ZS01' || _allAddressData[i].addrType[0] == 'ZD01')) {
        _allAddressData[i].custPhone[0] = '';
      }
    }
  }
}

function clearPOBoxFromGrid() {
  console.log(">>>> clearPOBoxFromGrid ");
  for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
    recordList = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
    if (_allAddressData != null && _allAddressData[i] != null) {
      if (!(_allAddressData[i].addrType[0] == 'ZS01' || _allAddressData[i].addrType[0] == 'ZP01')) {
        _allAddressData[i].poBox[0] = '';
      }
    }
  }
}

/*
 * Disable VAT ID when user role is requester and request type is update
 */
function addVATDisabler() {
  console.log(">>>> addVATDisabler ");
  var interval = new Object();
  var roleCheck = false;
  var reqCheck = false;

  interval = setInterval(function() {
    var role = FormManager.getActualValue('userRole').toUpperCase();
    var req = FormManager.getActualValue('reqType').toUpperCase();
    var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');

    var vat = FormManager.getActualValue('vat');
    if (req == 'C') {
      return;
    }

    FormManager.enable('vat');

    if (role != null && role.length > 0) {
      roleCheck = true;
    }

    if (req != null && req.length > 0) {
      reqCheck = true;
    }

    if (roleCheck && reqCheck) {
      if (role == 'REQUESTER' && vat != "") {
        FormManager.readOnly('vat');
      }
      clearInterval(interval);
    }
      
    if (viewOnlyPage == 'true') {
      FormManager.readOnly('vat');
    }
  }, 1000);
}


/**
 * Add Latin character validation for address fields
 */
function addLatinCharValidator() {
  console.log(">>>> addVATDisabler ");
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var addrType = FormManager.getActualValue('addrType');
  var custType = FormManager.getActualValue('custGrp');
  var validateNonLatin = false;

  // latin addresses
  var addrToChkForIL = new Set([ 'ZI01', 'ZS02', 'CTYA', 'CTYB', 'CTYC' ]);
  var addrToChkForGR = new Set([ 'ZS01', 'ZD01', 'ZI01' ]);

  // for cross border
  if (custType == 'CROSS') {
    addrToChkForGR = new Set([ 'ZP01', 'ZS01', 'ZD01', 'ZI01' ]);
  }

  if (cntry == SysLoc.ISRAEL && addrToChkForIL.has(addrType)) {
    validateNonLatin = true;
  } else if (cntry == SysLoc.GREECE && addrToChkForGR.has(addrType)) {
    validateNonLatin = true;
  }

  if (validateNonLatin) {
    if (cntry == SysLoc.GREECE) {
      checkAndAddValidator('custNm4', Validators.LATIN, [ 'Att. Person' ]);
      checkAndAddValidator('custNm1', Validators.LATIN, [ 'Customer Name' ]);
      checkAndAddValidator('custNm2', Validators.LATIN, [ 'Customer Name Con\'t' ]);
      checkAndAddValidator('addrTxt2', Validators.LATIN, [ ' Address Con\'t/Occupation' ]);
      checkAndAddValidator('dept', Validators.LATIN, [ 'District' ]);
    }
    checkAndAddValidator('custNm1', Validators.LATIN, [ 'Customer Name' ]);
    checkAndAddValidator('addrTxt', Validators.LATIN, [ 'Street Address' ]);
    checkAndAddValidator('city1', Validators.LATIN, [ 'City' ]);
    checkAndAddValidator('postCd', Validators.LATIN, [ 'Postal Code' ]);
    checkAndAddValidator('poBox', Validators.LATIN, [ 'PO Box' ]);
  } else {
    FormManager.removeValidator('custNm1', Validators.LATIN);
    FormManager.removeValidator('custNm2', Validators.LATIN);
    FormManager.removeValidator('custNm4', Validators.LATIN);
    FormManager.removeValidator('addrTxt', Validators.LATIN);
    FormManager.removeValidator('addrTxt2', Validators.LATIN);
    FormManager.removeValidator('city1', Validators.LATIN);
    FormManager.removeValidator('postCd', Validators.LATIN);
    FormManager.removeValidator('dept', Validators.LATIN);
    FormManager.removeValidator('poBox', Validators.LATIN);
  }
}

/**
 * Add Non-Latin character validation for address fields
 */
function addNonLatinCharValidator() {
  console.log(">>>> addNonLatinCharValidator ");
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var addrType = FormManager.getActualValue('addrType');
  var custType = FormManager.getActualValue('custGrp');
  var landCntry = FormManager.getActualValue('landCntry');

  // local addresses
  var addrToChkForIL = new Set([ 'ZS01', 'ZP01', 'ZD01' ]);
  var addrToChkForGR = new Set([ 'ZP01' ]);
  var validateLatin = false;

  // for cross border
  if (custType == 'CROSS' || landCntry != 'GR') {
    addrToChkForGR = new Set();
  }

  if (cntry == SysLoc.ISRAEL && addrToChkForIL.has(addrType)) {
    validateLatin = true;
  } else if (cntry == SysLoc.GREECE && addrToChkForGR.has(addrType)) {
    validateLatin = true;
  }

  if (validateLatin) {
    if (cntry == SysLoc.ISRAEL) {
      checkAndAddValidator('custNm2', Validators.NON_LATIN, [ 'Customer Name Con\'t' ]);
      checkAndAddValidator('addrTxt2', Validators.NON_LATIN, [ 'Address Con\'t' ]);
      checkAndAddValidator('dept', Validators.NON_LATIN, [ 'Attention Person' ]);
      checkAndAddValidator('custNm1', Validators.NON_LATIN, [ 'Customer Name' ]);
    } else if (cntry == SysLoc.GREECE) {
      checkAndAddValidator('addrTxt2', Validators.NON_LATIN, [ 'Address Con\'t/Occupation' ]);
      checkAndAddValidator('dept', Validators.NON_LATIN, [ 'District' ]);
      checkAndAddValidator('taxOffice', Validators.NON_LATIN, [ 'Tax Office' ]);
      checkAndAddValidator('custNm4', Validators.NON_LATIN, [ 'Att. Person' ]);
    }
    checkAndAddValidator('addrTxt', Validators.NON_LATIN, [ 'Street Address' ]);
    checkAndAddValidator('city1', Validators.NON_LATIN, [ 'City' ]);
    checkAndAddValidator('postCd', Validators.NON_LATIN, [ 'Postal Code' ]);
    checkAndAddValidator('poBox', Validators.NON_LATIN, [ 'PO Box' ]);
    // checkAndAddValidator('custPhone', Validators.NON_LATIN, [ 'Phone #' ]);

    if (cntry == SysLoc.ISRAEL && custType == 'CROSS') {
      FormManager.removeValidator('postCd', Validators.NON_LATIN);
    }
  } else {
    FormManager.removeValidator('custNm1', Validators.NON_LATIN);
    FormManager.removeValidator('custNm2', Validators.NON_LATIN);
    FormManager.removeValidator('custNm4', Validators.NON_LATIN);
    FormManager.removeValidator('addrTxt', Validators.NON_LATIN);
    FormManager.removeValidator('addrTxt2', Validators.NON_LATIN);
    FormManager.removeValidator('city1', Validators.NON_LATIN);
    FormManager.removeValidator('postCd', Validators.NON_LATIN);
    FormManager.removeValidator('dept', Validators.NON_LATIN);
    FormManager.removeValidator('poBox', Validators.NON_LATIN);
    FormManager.removeValidator('taxOffice', Validators.NON_LATIN);
  }
}

/**
 * postal code plus city should not exceed 28 char for UKI
 */
function addPostCdCityValidator() {
  console.log(">>>> addPostCdCityValidator ");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var postal_cd = FormManager.getActualValue('postCd');
        var city = FormManager.getActualValue('city1');
        var total_length = postal_cd.length + city.length;
        var land_cntry = FormManager.getActualValue('landCntry');
        var issu_cntry = FormManager.getActualValue('cmrIssuingCntry');
        if ((issu_cntry == '866' && land_cntry != 'GB') || (issu_cntry == '754' && land_cntry != 'IE')) {
          if (total_length > 28) {
            return new ValidationResult(null, false, 'Limit for Postal code together with City exceeded max length 28 characters.');
          }
        } else {
          return new ValidationResult(null, true);
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function addAbbrevNmValidator() {
  console.log(">>>> addAbbrevNmValidator ");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var _abbrevNm = FormManager.getActualValue('abbrevNm');
        var reg = /[^\u0020-\u007e]/;
        if (_abbrevNm != '' && (_abbrevNm.length > 0 && !reg.test(_abbrevNm))) {
          return new ValidationResult({
            id : 'abbrevNm',
            type : 'text',
            name : 'abbrevNm'
          }, false, 'The value for Abbreviated name is invalid. Only ALPHANUMERIC characters are allowed.');
        } else {
          return new ValidationResult(null, true);
        }

      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function validateEMEACopy(addrType, arrayOfTargetTypes) {
  console.log(">>>> validateEMEACopy ");
  console.log('Addr Type: ' + addrType + " Targets: " + arrayOfTargetTypes);
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var custType = FormManager.getActualValue('custGrp');
  var reqType = FormManager.getActualValue('reqType');
  if (cntry == SysLoc.ISRAEL) {
    var hebrewSource = addrType == 'ZS01' || addrType == 'ZP01' || addrType == 'ZD01';
    if (hebrewSource
        && (arrayOfTargetTypes.indexOf('ZI01') >= 0 || arrayOfTargetTypes.indexOf('ZS02') >= 0 || arrayOfTargetTypes.indexOf('CTYA') >= 0 || arrayOfTargetTypes.indexOf('CTYB') >= 0 || arrayOfTargetTypes
            .indexOf('CTYC') >= 0)) {
      return 'Cannot copy Hebrew address to non-Hebrew addresses. Please select only Hebrew target addresses.';
    }
    if (!hebrewSource && (arrayOfTargetTypes.indexOf('ZS01') >= 0 || arrayOfTargetTypes.indexOf('ZP01') >= 0 || arrayOfTargetTypes.indexOf('ZD01') >= 0)) {
      return 'Cannot copy non-Hebrew address to Hebrew addresses. Please select only non-Hebrew target addresses.';
    }
  } else if (cntry == SysLoc.GREECE) {
    var greekSource = addrType == 'ZP01';
    if (greekSource && custType != 'CROSS' && (arrayOfTargetTypes.indexOf('ZS01') >= 0 || arrayOfTargetTypes.indexOf('ZD01') >= 0)) {
      return 'Cannot copy Greek address to non-Greek addresses. Please select only Greek target addresses.';
    }
    if (!greekSource && custType != 'CROSS' && arrayOfTargetTypes.indexOf('ZP01') >= 0) {
      return 'Cannot copy non-Greek address to Greek addresses. Please select only non-Greek target addresses.';
    }
  } else if (cntry == SysLoc.ITALY) {
    if (typeof (_allAddressData) == 'undefined' || !_allAddressData || _allAddressData.length == 0) {
      return null;
    }
    if (reqType == 'C') {
      var trg = null;
      for (var i = 0; i < arrayOfTargetTypes.length; i++) {
        trg = arrayOfTargetTypes[i];
        for (var j = 0; j < _allAddressData.length; j++) {
          console.log('Type: ' + _allAddressData[j].addrType[0] + ' Import: ' + _allAddressData[j].importInd[0]);
          if (trg == _allAddressData[j].addrType[0] && _allAddressData[j].importInd[0] == 'Y') {
            return 'Cannot copy to an address that cannot be modified.';
          }
        }
      }
    } else if (reqType == 'U' || reqType == 'X') {
      var cmrNo = FormManager.getActualValue('cmrNo');
      var trg = null;
      for (var i = 0; i < arrayOfTargetTypes.length; i++) {
        trg = arrayOfTargetTypes[i];
        for (var j = 0; j < _allAddressData.length; j++) {
          console.log('Type: ' + _allAddressData[j].addrType[0] + ' Par CMR: ' + _allAddressData[j].parCmrNo[0]);
          if (trg == _allAddressData[j].addrType[0] && _allAddressData[j].parCmrNo[0] != cmrNo) {
            return 'Cannot copy to an address under a different CMR No.';
          }
        }
      }
    }
  }
  return null;
}

function addGRAddressTypeValidator() {
  console.log(">>>> addGRAddressTypeValidator ");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('cmrIssuingCntry') != SysLoc.GREECE) {
          return new ValidationResult(null, true);
        }
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount == 0) {
          return new ValidationResult(null, false, 'Local Language translation of Sold-to is required');
        }
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          var record = null;
          var type = null;
          var zs01Cnt = 0;
          var zp01Cnt = 0;
          var zd01Cnt = 0;
          var zi01Cnt = 0;
          var zs01Data = null;
          var zp01Data = null;

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
              zs01Data = record;
              zs01Cnt++;
            } else if (type == 'ZP01') {
              zp01Data = record;
              zp01Cnt++;
            } else if (type == 'ZD01') {
              zd01Cnt++;
            } else if (type == 'ZI01') {
              zi01Cnt++;
            }
          }

          if (FormManager.getActualValue('custGrp') == 'LOCAL') {
            mismatchFields = getMismatchFields(zs01Data, zp01Data, false);
            if (mismatchFields != '') {
              return new ValidationResult(null, false, 'Sold-to mismatch, please update Local Language translation of Sold-to: ' + mismatchFields);
            }
          } else if (FormManager.getActualValue('custGrp') == 'CROSS' && zs01Data != null && zp01Data != null && !isTranslationAddrFieldsMatchForGR(zs01Data, zp01Data)) {
            return new ValidationResult(null, false, 'Local language not applicable for Cross-border, address must match sold to data.');
          } else if (FormManager.getActualValue('reqType') == 'U' && !isLandedCntryMatch(zs01Data, zp01Data)) {
            return new ValidationResult(null, false, '\'Country (Landed)\' of Local Language translation of Sold-to should match Sold-to.');
          } else if ((FormManager.getActualValue('reqType') == 'U')) {
            var mismatchFields = '';
            // GR then not crossborder
            if (zs01Data.landCntry[0] == 'GR') {
              mismatchFields = getMismatchFields(zs01Data, zp01Data, false);
            } else if (zs01Data.landCntry[0] != 'GR') {
              mismatchFields = getMismatchFields(zs01Data, zp01Data, true);
            }
            if (mismatchFields != '') {
              return new ValidationResult(null, false, 'Sold-to and Local Translation, mismatched fields: ' + mismatchFields);
            }
          }

          if (zs01Cnt == 0 || zp01Cnt == 0 || zd01Cnt == 0 || zi01Cnt == 0) {
            return new ValidationResult(null, false, 'Local Language translation of Sold-to is required');
          } else if (zs01Cnt > 1) {
            return new ValidationResult(null, false, 'Only one Sold To address is allowed.');
          } else if (zp01Cnt > 1) {
            return new ValidationResult(null, false, 'Only one Local Language translation of Sold-to address is allowed.');
          }
          return new ValidationResult(null, true);

        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function addGRAddressGridValidatorStreetPOBox() {
  console.log(">>>> addGRAddressGridValidatorStreetPOBox ");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('cmrIssuingCntry') != SysLoc.GREECE) {
          return new ValidationResult(null, true);
        }
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          var record = null;
          var type = null;

          var missingPOBoxStreetAddrs = '';
          for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
            record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
            if (record == null && _allAddressData != null && _allAddressData[i] != null) {
              record = _allAddressData[i];
            }
            type = record.addrType;
            if (typeof (type) == 'object') {
              type = type[0];
            }

            var isPOBoxOrStreetFilled = (record.poBox[0] != null && record.poBox[0] != '') || (record.addrTxt[0] != null && record.addrTxt[0] != '');
            if (!isPOBoxOrStreetFilled) {
              if (missingPOBoxStreetAddrs != '') {
                missingPOBoxStreetAddrs += ', ' + record.addrTypeText[0];
              } else {
                missingPOBoxStreetAddrs += record.addrTypeText[0];
              }
            }
          }

          if (missingPOBoxStreetAddrs != '') {
            return new ValidationResult(null, false, 'Please fill-out Street for the following address: ' + missingPOBoxStreetAddrs);
          }

          return new ValidationResult(null, true);

        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function isTranslationAddrFieldsMatchForGR(zs01Data, zp01Data) {
	console.log(">>>> isTranslationAddrFieldsMatchForGR ");
  if (zs01Data.custNm1[0] == zp01Data.custNm1[0] && zs01Data.custNm2[0] == zp01Data.custNm2[0] && zs01Data.custNm4[0] == zp01Data.custNm4[0] && zs01Data.addrTxt[0] == zp01Data.addrTxt[0]
      && zs01Data.addrTxt2[0] == zp01Data.addrTxt2[0] && zs01Data.poBox[0] == zp01Data.poBox[0] && zs01Data.postCd[0] == zp01Data.postCd[0] && zs01Data.city1[0] == zp01Data.city1[0]) {
    return true;
  }
  return false;
}

function getMismatchFields(zs01Data, zp01Data, isCrossborder) {
  console.log(">>>> getMismatchFields ");
  var mismatchFields = '';
  if (zs01Data == null || zp01Data == null) {
    return mismatchFields;
  }
  if (!hasMatchingFieldsFilled(zs01Data.addrTxt[0], zp01Data.addrTxt[0], isCrossborder)) {
    mismatchFields += 'Street Address';
  }
  if (!hasMatchingFieldsFilled(zs01Data.custNm2[0], zp01Data.custNm2[0], isCrossborder)) {
    mismatchFields += mismatchFields != '' ? ', ' : '';
    mismatchFields += 'Customer Name Con\'t';
  }
  if (!hasMatchingFieldsFilled(zs01Data.addrTxt2[0], zp01Data.addrTxt2[0], isCrossborder)) {
    mismatchFields += mismatchFields != '' ? ', ' : '';
    mismatchFields += 'Address Con\'t/Occupation';
  }
  if (!hasMatchingFieldsFilled(zs01Data.poBox[0], zp01Data.poBox[0], isCrossborder)) {
    mismatchFields += mismatchFields != '' ? ', ' : '';
    mismatchFields += 'PO Box';
  }
  if (!hasMatchingFieldsFilled(zs01Data.custNm4[0], zp01Data.custNm4[0], isCrossborder)) {
    mismatchFields += mismatchFields != '' ? ', ' : '';
    mismatchFields += 'Att. Person';
  }

  if (isCrossborder) {
    if (!hasMatchingFieldsFilled(zs01Data.custNm1[0], zp01Data.custNm1[0], isCrossborder)) {
      mismatchFields += mismatchFields != '' ? ', ' : '';
      mismatchFields += 'Customer Name';
    }
    if (!hasMatchingFieldsFilled(zs01Data.postCd[0], zp01Data.postCd[0], isCrossborder)) {
      mismatchFields += mismatchFields != '' ? ', ' : '';
      mismatchFields += 'Postal Code';
    }
    if (!hasMatchingFieldsFilled(zs01Data.city1[0], zp01Data.city1[0], isCrossborder)) {
      mismatchFields += mismatchFields != '' ? ', ' : '';
      mismatchFields += 'City';
    }
  }

  return mismatchFields;
}

function hasMatchingFieldsFilled(zs01Field, zp01Field, isCrossborder) {
  console.log(">>>> hasMatchingFieldsFilled ");
  if (!isCrossborder) {
    // local just check if empty or not
    if (zs01Field != '' && zs01Field != null) {
      if (zp01Field == '' || zp01Field == null) {
        return false;
      }
    }
    if (zp01Field != '' && zp01Field != null) {
      if (zs01Field == '' || zs01Field == null) {
        return false;
      }
    }
  } else {
    // check if it is matching
    if (zs01Field != zp01Field) {
      return false;
    }
  }
  return true;
}

function isLandedCntryMatch(zs01Data, zp01Data) {
  console.log(">>>> isLandedCntryMatch ");
  if (zs01Data.landCntry[0] == zp01Data.landCntry[0]) {
    return true;
  }
  return false;
}

function populateTranslationAddrWithSoldToData() {
    console.log(">>>> populateTranslationAddrWithSoldToData ");
  if (FormManager.getActualValue('custGrp') == 'CROSS' && CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0 && FormManager.getActualValue('addrType') == 'ZP01') {
    var record = null;
    var type = null;
    var zs01Data = null; // Sold-to

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
        zs01Data = record;
        break;
      }
    }

    // Populate Local language translation of sold to with Sold to data
    if (zs01Data != null) {
      FormManager.setValue('custNm1', zs01Data.custNm1);
      FormManager.setValue('custNm2', zs01Data.custNm2);
      FormManager.setValue('addrTxt', zs01Data.addrTxt);
      FormManager.setValue('addrTxt2', zs01Data.addrTxt2);
      FormManager.setValue('poBox', zs01Data.poBox);
      FormManager.setValue('postCd', zs01Data.postCd);
      FormManager.setValue('city1', zs01Data.city1);
    }
  }
}

// Add individual function to prevent different requirement in future
function populateTranslationAddrWithSoldToDataTR() {
  console.log(">>>> populateTranslationAddrWithSoldToDataTR ");
  if (FormManager.getActualValue('custGrp') == 'CROSS' && CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0 && FormManager.getActualValue('addrType') == 'ZP01') {
    var record = null;
    var type = null;
    var zs01Data = null; // Sold-to

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
        zs01Data = record;
        break;
      }
    }

    // Populate Local language translation of sold to with Sold to data
    if (zs01Data != null) {
      FormManager.setValue('custNm1', zs01Data.custNm1);
      FormManager.setValue('custNm2', zs01Data.custNm2);
      FormManager.setValue('addrTxt', zs01Data.addrTxt);
      FormManager.setValue('addrTxt2', zs01Data.addrTxt2);
      FormManager.setValue('poBox', zs01Data.poBox);
      FormManager.setValue('postCd', zs01Data.postCd);
      FormManager.setValue('city1', zs01Data.city1);
      FormManager.setValue('dept', zs01Data.city1);
    }
  }
}

var _addrSelectionHistTR = '';
function preFillTranslationAddrWithSoldToForTR(cntry, addressMode, saving) {
  console.log(">>>> preFillTranslationAddrWithSoldToForTR ");
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.TURKEY) {
    var custType = FormManager.getActualValue('custGrp');
    // for local don't proceed
    if (custType == 'LOCAL') {
      return;
    }
    if (!saving) {
      if (FormManager.getActualValue('addrType') == 'ZP01') {
        populateTranslationAddrWithSoldToDataTR();
      } else if (FormManager.getActualValue('addrType') != 'ZP01' && addressMode != 'updateAddress' && _addrSelectionHistTR == 'ZP01') {
        // clear address fields when switching
        clearAddrFieldsForTR();
      }
    }
    _addrSelectionHistTR = FormManager.getActualValue('addrType');
  }
}

function clearAddrFieldsForGR() {
  console.log(">>>> clearAddrFieldsForGR ");
  FormManager.clearValue('custNm1');
  FormManager.clearValue('custNm2');
  FormManager.clearValue('addrTxt');
  FormManager.clearValue('addrTxt2');
  FormManager.clearValue('poBox');
  FormManager.clearValue('postCd');
  FormManager.clearValue('city1');
}

function clearAddrFieldsForTR() {
  console.log(">>>> clearAddrFieldsForTR ");
  FormManager.clearValue('custNm1');
  FormManager.clearValue('custNm2');
  FormManager.clearValue('addrTxt');
  FormManager.clearValue('addrTxt2');
  FormManager.clearValue('poBox');
  FormManager.clearValue('postCd');
  FormManager.clearValue('city1');
  FormManager.clearValue('dept');
}

var _addrSelectionHistGR = '';
function preFillTranslationAddrWithSoldToForGR(cntry, addressMode, saving) {
  console.log(">>>> preFillTranslationAddrWithSoldToForGR ");
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.GREECE) {
    var custType = FormManager.getActualValue('custGrp');

    // for local don't proceed
    if (custType == 'LOCAL' || cmr.addressMode == 'copyAddress') {
      return;
    }
    if (!saving) {
      if (FormManager.getActualValue('addrType') == 'ZP01') {
        populateTranslationAddrWithSoldToData();
      } else if (FormManager.getActualValue('addrType') != 'ZP01' && addressMode != 'updateAddress' && _addrSelectionHistGR == 'ZP01') {
        // clear address fields when switching
        clearAddrFieldsForGR();
      }
    }
    _addrSelectionHistGR = FormManager.getActualValue('addrType');
  }
}

var _gtcISUHandler = null;
var _CTCHandler = null;
var _gtcISRHandler = null;
var _gtcAddrTypes = [ 'ZS01', 'ZP01', 'ZD01', 'ZI01', 'ZS02' ];
var _gtcAddrTypeHandler = [];
var _gtcVatExemptHandler = null;

function addHandlersForGR() {
  console.log(">>>> addHandlersForGR ");

  var custType = FormManager.getActualValue('custGrp');
  addRemoveValidator();
  lockUnlockField();
  if (_custSubTypeHandlerGr == null && FormManager.getActualValue('cmrIssuingCntry') == SysLoc.GREECE) {
    _custSubTypeHandlerGr = dojo.connect(FormManager.getField('custSubGrp'), 'onChange', function(value) {
      FormManager.setValue('salesTeamCd', '');
      resetSubIndustryCdGR();
    });
  }

  for (var i = 0; i < _gtcAddrTypes.length; i++) {
    _gtcAddrTypeHandler[i] = null;
    if (_gtcAddrTypeHandler[i] == null) {
      _gtcAddrTypeHandler[i] = dojo.connect(FormManager.getField('addrType_' + _gtcAddrTypes[i]), 'onClick', function(value) {
        preFillTranslationAddrWithSoldToForTR();
        if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.GREECE) {
          convertToUpperCaseGR();
          disableAddrFieldsGR();
          preFillTranslationAddrWithSoldToForGR();
        }
      });
    }
  }

  if (_gtcVatExemptHandler == null) {
    _gtcVatExemptHandler = dojo.connect(FormManager.getField('vatExempt'), 'onClick', function(value) {
      setVatValidatorGRCYTR();
    });
  }

  // if (_gtcISUHandler == null) {
  // _gtcISUHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange',
  // function(value) {
  // setClientTierForCreates(value);
  // });
  // }
  //
  // if (_CTCHandler == null) {
  // _CTCHandler = dojo.connect(FormManager.getField('clientTier'), 'onChange',
  // function(value) {
  // setISRValuesGR();
  // setClientTierForCreates();
  // });
  // }

}

// function setClientTierValuesForGREECE(isuCd) {
// console.log(">>>> setClientTierValuesForGREECE ");
// var reqType = FormManager.getActualValue('reqType');
// if (isuCd == null || !isuCd) {
// isuCd = FormManager.getActualValue('isuCd');
// }
// var custSubGrp = FormManager.getActualValue('custSubGrp');
// var clientTier = FormManager.getActualValue('clientTier');
// var isuCtc = isuCd + clientTier;
// var isuCtcEnterprise = isuCtc + enterprise;
//
// var custSubGrpSet1 = new Set([ 'BUSPR', 'INTER', 'IBMEM', 'XBP', 'XBP' ]);
// var custSubGrpSet2 = new Set([ 'PRICU', 'SPAS' ]);
// var custSubGrpSet3 = new Set([ 'COMME', 'GOVRN' ]);
//
// if (reqType != 'C') {
// return;
// }
//
// if (isuCd == '21' && custSubGrpSet1.has(custSubGrp)) {
// FormManager.setValue('clientTier', '');
// FormManager.setValue('enterprise', '985999');
// } else if (isuCd == '34' && custSubGrpSet2.has(custSubGrp)) {
// FormManager.setValue('clientTier', 'Q');
// FormManager.setValue('enterprise', '822830');
// } else if (isuCd == '34' && custSubGrpSet3.has(custSubGrp)) {
// FormManager.setValue('clientTier', 'Q');
// FormManager.setValue('enterprise', '822830');
// } else if (isuCd == '36' && custSubGrpSet3.has(custSubGrp)) {
// FormManager.setValue('clientTier', 'Y');
// FormManager.setValue('enterprise', '');
// } else if (isuCd == '32' && custSubGrpSet3.has(custSubGrp)) {
// FormManager.setValue('clientTier', 'T');
// FormManager.setValue('enterprise', '985985');
// } else if (isuCd == '5K' && custSubGrpSet3.has(custSubGrp)) {
// FormManager.setValue('clientTier', '');
// FormManager.setValue('enterprise', '985999');
// } else if (custSubGrpSet3.has(custSubGrp)) {
// FormManager.setValue('clientTier', '');
// FormManager.setValue('enterprise', '');
// }
// lockUnlockField();
//
// }

// function setClientTierForCreates(isuCd) {
// console.log(">>>> setClientTierForCreates ");
// var cntry = FormManager.getActualValue('cmrIssuingCntry');
// if (cntry == SysLoc.GREECE) {
// setClientTierValuesForGREECE(isuCd);
// lockUnlockField();
// return;
// }
// var reqType = null;
// reqType = FormManager.getActualValue('reqType');
// if (isuCd == null || !isuCd) {
// var isuCd = FormManager.getActualValue('isuCd');
// }
// var custSubGrp = FormManager.getActualValue('custSubGrp');
// var clientTiers = FormManager.getActualValue('clientTier');
// if (isuCd == '5K') {
// FormManager.removeValidator('clientTier', Validators.REQUIRED);
// return;
// } else if ((custSubGrp == 'PRICU' || custSubGrp == 'SPAS') && isuCd == '34')
// {
// FormManager.setValue('clientTier', 'Q');
// } else if (custSubGrp == 'BUSPR' || custSubGrp == 'INTER' || custSubGrp ==
// 'XBP' || custSubGrp == 'XINTR') {
// FormManager.readOnly('clientTier');
// } else if (custSubGrp == 'IBMEM') {
// FormManager.setValue('clientTier', '');
// }
// lockUnlockField();
//
// if (reqType != 'C') {
// return;
// }
// tierValues = null;
// enterpriseLov = null;
// if (reqType == 'C') {
// enterpriseLov = [];
// if (isuCd == '34') {
// tierValues = [ 'V', '6', 'A', 'Q', 'Y', 'Z' ];
// if (clientTiers == '6') {
// enterpriseLov = [ '822835' ];
// }
// if (clientTiers == 'Q') {
// enterpriseLov = [ '822830' ];
// }
// if (clientTiers == 'Y') {
// enterpriseLov = [ '822840' ];
// }
// } else if (isuCd == '32') {
// tierValues = [ 'N', 'S' ];
// }
// }
// if (tierValues != null) {
// FormManager.limitDropdownValues(FormManager.getField('clientTier'),
// tierValues);
// if (tierValues.length == 1) {
// FormManager.setValue('clientTier', tierValues[0]);
// }
// } else {
// FormManager.resetDropdownValues(FormManager.getField('clientTier'));
// }
//
// if (enterpriseLov != null) {
// FormManager.limitDropdownValues(FormManager.getField('enterprise'),
// enterpriseLov);
// if (enterpriseLov.length == 1) {
// FormManager.setValue('enterprise', enterpriseLov[0]);
// }
// } else {
// FormManager.resetDropdownValues(FormManager.getField('enterprise'));
// }
// lockUnlockField();
// }

function addPOBoxValidatorGR() {
  console.log(">>>> addPOBoxValidatorGR ");
  FormManager.removeValidator('poBox', Validators.LATIN);
  FormManager.removeValidator('poBox', Validators.NON_LATIN);
  FormManager.addValidator('poBox', Validators.DIGIT, [ 'PO Box' ]);
}

function setVatValidatorGRCYTR() {
  console.log(">>>> setVatValidatorGRCYTR ");
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var vatInd = FormManager.getActualValue('vatInd');

  if (viewOnlyPage != 'true' && FormManager.getActualValue('reqType') == 'C') {
    FormManager.resetValidations('vat');
    if (FormManager.getActualValue('custSubGrp') == 'IBMEM') {
      FormManager.readOnly('vat');
    }
    if (vatInd == 'N') {
      FormManager.clearValue('vat');
    }
    if (undefined != dijit.byId('vatExempt') && !dijit.byId('vatExempt').get('checked') && cntry == SysLoc.GREECE) {
      checkAndAddValidator('vat', Validators.REQUIRED, [ 'VAT' ]);
      FormManager.enable('vat');
    }
  }
}

function addPaymentModeValidator() {
  console.log(">>>> addPaymentModeValidator ");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var modeOfPayment = FormManager.getActualValue('modeOfPayment');

        var result = false;
        if (modeOfPayment != '' && modeOfPayment != '5' && modeOfPayment != null) {
          result = false;
        } else {
          result = true;
        }
        if (modeOfPayment.length == 0 || modeOfPayment == '') {
          result = true;
        }
        if (!result) {
          return new ValidationResult(null, false, 'Invalid value for Mode of Payment.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

// function setISRValuesGR() {
// console.log(">>>> setISRValuesGR ");
// var custSubGrp = FormManager.getActualValue('custSubGrp');
// if (custSubGrp == 'SPAS') {
// FormManager.setValue('abbrevLocn', 'SAAS');
// } else if (custSubGrp == 'INTER' || custSubGrp == 'XINTR') {
// FormManager.setValue('repTeamMemberNo', '000000');
// } else if ((custSubGrp == 'BUSPR' || custSubGrp == 'XBP') &&
// _isScenarioChanged && FormManager.getActualValue('repTeamMemberNo') == '') {
// FormManager.setValue('repTeamMemberNo', '200005');
// }
// var repTeamMemberNo = FormManager.getActualValue('repTeamMemberNo');
// FormManager.readOnly('subIndustryCd');
// if (custSubGrp == 'COMME' || custSubGrp == 'CROSS' || custSubGrp == 'PRICU'
// || custSubGrp == 'GOVRN' || custSubGrp == '' || custSubGrp == 'BUSPR' ||
// custSubGrp == 'XBP' || custSubGrp == 'SPAS') {
// setISRValues();
// }
// if (repTeamMemberNo == '') {
// FormManager.setValue('salesSR', '');
// FormManager.setValue('salesTeamCd', '');
// FormManager.setValue('salesBusOffCd', '');
// }
// }
//
// function setISRValuesGROnUpdate() {
// console.log(">>>> setISRValuesGROnUpdate ");
// if (FormManager.getActualValue('reqType') == 'U') {
// setISRValuesGR();
// }
// }

var _isScenarioChanged = false;
function checkScenarioChanged(fromAddress, scenario, scenarioChanged) {
  console.log(">>>> checkScenarioChanged ");
  _isScenarioChanged = scenarioChanged;
}

function retainImportValues(fromAddress, scenario, scenarioChanged) {
  console.log(">>>> retainImportValues ");
  var isCmrImported = getImportedIndcForGreece();
  var reqId = FormManager.getActualValue('reqId');

  if (FormManager.getActualValue('reqType') == 'C' && isCmrImported == 'Y' && scenarioChanged) {
    if (scenario == 'COMME' || scenario == 'GOVRN' || scenario == 'CROSS') {

      var origISU;
      var origClientTier;
      var origRepTeam;
      var origSbo;
      var origInac;
      var origEnterprise;

      var result = cmr.query("GET.CMRINFO.IMPORTED_GR", {
        REQ_ID : reqId
      });

      if (result != null && result != '') {
        origISU = result.ret1;
        origClientTier = result.ret2;
        origRepTeam = result.ret3;
        origSbo = result.ret4;
        origInac = result.ret5;
        origEnterprise = result.ret6;

        FormManager.setValue('isuCd', origISU);
        FormManager.setValue('clientTier', origClientTier);
        FormManager.setValue('repTeamMemberNo', origRepTeam);
        FormManager.setValue('salesBusOffCd', origSbo);
        FormManager.setValue('inacCd', origInac);
        FormManager.setValue('enterprise', origEnterprise);
      }
    } else {
      FormManager.setValue('inacCd', '');
    }
  }
}

function setFieldsBehaviourGR() {
  console.log(">>>> setFieldsBehaviourGR ");
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var isuCd = FormManager.getActualValue('isuCd');
  var clientTier = FormManager.getActualValue('clientTier');
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var repTeamMemberNo = FormManager.getActualValue('repTeamMemberNo');
  if (custSubGrp == 'CROSS' || custSubGrp == 'COMME') {
    lockUnlockField();
  }
  if (viewOnlyPage != 'true') {
    if (role == 'PROCESSOR') {
      FormManager.enable('abbrevLocn');
      FormManager.addValidator('abbrevLocn', Validators.REQUIRED, [ 'Abbreviated Location' ], 'MAIN_CUST_TAB');
      FormManager.addValidator('isuCd', Validators.REQUIRED, [ 'ISU Code' ], 'MAIN_IBM_TAB');
      FormManager.addValidator('clientTier', Validators.REQUIRED, [ 'Client Tier' ], 'MAIN_IBM_TAB');
    }
    if (role == 'REQUESTER') {
      FormManager.resetValidations('isuCd');
      FormManager.resetValidations('clientTier');
      FormManager.addValidator('abbrevLocn', Validators.REQUIRED, [ 'Abbreviated Location' ], 'MAIN_CUST_TAB');
    }
    if (FormManager.getActualValue('reqType') == 'U') {
      FormManager.enable('embargoCd');
    }
  }
  FormManager.addValidator('custPrefLang', Validators.REQUIRED, [ 'Preferred Language' ], 'MAIN_IBM_TAB');
  FormManager.addValidator('cmrOwner', Validators.REQUIRED, [ 'CMR Owner' ], 'MAIN_IBM_TAB');
  if (FormManager.getActualValue('reqType') == 'C') {
    FormManager.addValidator('subIndustryCd', Validators.REQUIRED, [ 'Subindustry' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('isicCd', Validators.REQUIRED, [ 'ISIC' ], 'MAIN_CUST_TAB');

    if (role == 'PROCESSOR') {
      FormManager.addValidator('repTeamMemberNo', Validators.REQUIRED, [ 'Sales Rep' ], 'MAIN_IBM_TAB');
    } else if (role == 'REQUESTER') {
      FormManager.resetValidations('repTeamMemberNo');
      FormManager.resetValidations('salesTeamCd');
    }

  } else if (FormManager.getActualValue('reqType') == 'U') {
    FormManager.resetValidations('subIndustryCd');
    FormManager.resetValidations('isicCd');
    FormManager.resetValidations('repTeamMemberNo');
    FormManager.resetValidations('isuCd');
    FormManager.resetValidations('clientTier');
    FormManager.readOnly('custPrefLang');
    FormManager.resetValidations('salesTeamCd');

  }
  FormManager.resetValidations('sitePartyId');
  FormManager.readOnly('sitePartyId');
  FormManager.readOnly('subIndustryCd');
  if (viewOnlyPage) {
    FormManager.readOnly('modeOfPayment');
  }
  addRemoveValidator();
  lockUnlockField();
}

function resetSubIndustryCdGR() {
  console.log(">>>> resetSubIndustryCdGR ");
  if (PageManager.isReadOnly()) {
    return;
  }
  var interval = new Object();
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  interval = setInterval(function() {
    var isicValue = FormManager.getActualValue('isicCd');
    if (isicValue == '') {
      if (typeof (_pagemodel) != 'undefined' && _pagemodel.isicCd != null && _pagemodel.isicCd != '') {
        isicValue = _pagemodel.isicCd;
      }
    }
    if (isicValue != null && isicValue.length > 0 && viewOnlyPage != 'true') {
      clearInterval(interval);
      FormManager.readOnly('subIndustryCd');
    } else {
      FormManager.readOnly('subIndustryCd');
    }
  }, 1000);
}

function addInacCodeValidator() {
  console.log(">>>> addInacCodeValidator ");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var value = FormManager.getActualValue('inacCd');
        var inacCd1 = value.substring(0, 2);
        var inacCd2 = value.substring(2, 4);
        var result = false;
        if (value && value.length == 4) {
          if (value && value.length > 0 && isNaN(value)) {
            result = false;
            if (inacCd1 && inacCd1.length > 0 && inacCd1.match("^[a-zA-Z]+$")) {
              result = true;
              if (isNaN(inacCd2)) {
                result = false;
              }
            } else {
              result = false;
            }
          } else {
            result = true;
          }
        } else {
          result = false;
        }
        if (value.length == 0 || value == '') {
          result = true;
        }
        if (!result) {
          return new ValidationResult(null, false, 'Invalid value for INAC Code.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function setISRValues() {
  console.log(">>>> setISRValues ");
  if (FormManager.getActualValue('reqType') == 'C') {
    var isuCd = FormManager.getActualValue('isuCd');
    var clientTier = FormManager.getActualValue('clientTier');
    var isrs = [];
    if (isuCd != '') {
      var qParams = {
        _qall : 'Y',
        ISSUING_CNTRY : FormManager.getActualValue('cmrIssuingCntry'),
        ISU : '%' + isuCd + clientTier + '%'
      };
      var results = cmr.query('GET.ISRLIST.BYISU', qParams);
      if (results != null) {
        for (var i = 0; i < results.length; i++) {
          isrs.push(results[i].ret1);
        }
        FormManager.limitDropdownValues(FormManager.getField('repTeamMemberNo'), isrs);
        if (isrs.length == 1) {
          FormManager.setValue('repTeamMemberNo', isrs[0]);
        }
        setSalesBoSboIbo();
      }
    }
  }
}

var _subindustryChanged = false;
function addHandlersForSubindustryCd() {
  console.log(">>>> addHandlersForSubindustryCd ");
  if (_subindustryCdHandler == null && FormManager.getField('subIndustryCd')) {
    _subindustryCdHandler = dojo.connect(FormManager.getField('subIndustryCd'), 'onChange', function(value) {
      if (cmr.currentTab == "CUST_REQ_TAB") {
        _subindustryChanged = true;
      }
      var repTeamMemberNo = FormManager.getActualValue('repTeamMemberNo');
    });
  }
  if (_subindustryCdHandler && _subindustryCdHandler[0]) {
    _subindustryCdHandler[0].onChange();
  }
}

function hideCollectionCd() {
  console.log(">>>> hideCollectionCd ");
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.TURKEY && FormManager.getActualValue('reqType') == 'U') {
    FormManager.show('CollectionCd', 'collectionCd');
  } else {
    FormManager.resetValidations('collectionCd');
    FormManager.hide('CollectionCd', 'collectionCd');
  }
}

// function setSalesBoSboIbo() {
// console.log(">>>> setSalesBoSboIbo ");
// var repTeamMemberNo = FormManager.getActualValue('repTeamMemberNo');
//
// if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.GREECE &&
// repTeamMemberNo.length > 6) {
// repTeamMemberNo = repTeamMemberNo.substring(0, 6);
// }
//
// if (repTeamMemberNo != '') {
// var qParams = {
// ISSUING_CNTRY : FormManager.getActualValue('cmrIssuingCntry'),
// REP_TEAM_CD : repTeamMemberNo
// };
// var result = cmr.query('DATA.GET.SALES_BO_CD', qParams);
// var salesBoCd = result.ret1;
// var selsr = result.ret2;
// FormManager.setValue('salesBusOffCd', salesBoCd);
// FormManager.setValue('salesTeamCd', selsr);
// } else {
// FormManager.setValue('salesBusOffCd', '');
// }
//
// // setSalesBoSboIboForGREECE();
//
// }

// function setSalesBoSboIboForGREECE() {
// console.log(">>>> setSalesBoSboIboForGREECE ");
// if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.GREECE) {
// FormManager.setValue('salesBusOffCd', '000000');
// FormManager.setValue('salesTeamCd', '000000');
// lockUnlockField();
// }
// }

function addShippingAddrTypeValidator() {
  console.log(">>>> addShippingAddrTypeValidator ");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('cmrIssuingCntry') != '755') {
          return new ValidationResult(null, true);
        }

        if (FormManager.getActualValue('reqType') == 'U') {
          if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
            var recordList = null;
            var addrType = null;
            var updateIndicator = null;
            var addrSequence = null;
            var recordLists = null;
            var updateIndicators = null;
            var pairedSequence = null;
            var pairedSequences = null;
            var addrSequences = null;
            for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
              recordList = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
              if (recordList == null && _allAddressData != null && _allAddressData[i] != null) {
                recordList = _allAddressData[i];
              }
              addrType = recordList.addrType;
              updateIndicator = recordList.updateInd;
              addrSequence = recordList.addrSeq;
              pairedSequences = recordList.pairedSeq;

              if (typeof (updateIndicator) == 'object') {
                updateIndicator = updateIndicator[0];
              }
              if (typeof (addrSequence) == 'object') {
                addrSequence = addrSequence[0];
              }
              if (typeof (pairedSequences) == 'object') {
                pairedSequences = pairedSequences[0];
              }

              if (addrType == 'ZD01' && updateIndicator == 'U') {
                for (var j = 0; j < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; j++) {
                  recordLists = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(j);
                  updateIndicators = recordLists.updateInd;
                  pairedSequence = recordLists.pairedSeq;
                  if (typeof (pairedSequence) == 'object') {
                    pairedSequence = pairedSequence[0];
                  }
                  if (typeof (updateIndicators) == 'object') {
                    updateIndicators = updateIndicators[0];
                  }

                  if (recordLists.addrType == 'CTYC' && addrSequence == pairedSequence && updateIndicators != 'U') {
                    return new ValidationResult(null, false, 'The translated Country Use C address ' + addrSequence + ' should also be updated.');
                  }
                }
              }

              if (addrType == 'CTYC' && updateIndicator == 'U') {
                for (var j = 0; j < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; j++) {
                  recordLists = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(j);
                  updateIndicators = recordLists.updateInd;
                  pairedSequence = recordLists.pairedSeq;
                  addrSequences = recordLists.addrSeq;
                  if (typeof (addrSequences) == 'object') {
                    addrSequences = addrSequences[0];
                  }
                  if (typeof (pairedSequence) == 'object') {
                    pairedSequence = pairedSequence[0];
                  }
                  if (typeof (updateIndicators) == 'object') {
                    updateIndicators = updateIndicators[0];
                  }
                  if (recordLists.addrType == 'ZD01' && addrSequences == pairedSequences && updateIndicators != 'U') {
                    return new ValidationResult(null, false, 'The Shipping address ' + addrSequences + ' should also be updated.');
                  }
                }
              }
            }
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function convertToUpperCaseGR(cntry, addressMode, saving) {
  console.log(">>>> convertToUpperCaseGR ");
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.CYPRUS) {
    return;
  }
  var custType = FormManager.getActualValue('custGrp');

  // for cross border
  if (custType == 'CROSS') {
    return;
  }

  // Greek address - block lowercase
  var addrFields = [ 'custNm1', 'custNm2', 'addrTxt', 'addrTxt2', 'city1', 'postCd', 'custPhone', 'sapNo', 'taxOffice', 'custNm4' ];
  if (FormManager.getActualValue('addrType') == 'ZP01') {
    for (var i = 0; i < addrFields.length; i++) {
      if (dojo.byId(addrFields[i]) != null) {
        dojo.byId(addrFields[i]).style.textTransform = 'uppercase';
        if (saving) {
          dojo.byId(addrFields[i]).value = dojo.byId(addrFields[i]).value.toUpperCase();
        }
      }
    }
  } else {
    for (var i = 0; i < addrFields.length; i++) {
      if (dojo.byId(addrFields[i]) != null) {
        dojo.byId(addrFields[i]).style.textTransform = 'none';
      }
    }
  }
}

function updateAddrTypeList(cntry, addressMode, saving) {
  console.log(">>>> updateAddrTypeList ");
  if (!saving || FormManager.getActualValue('cmrIssuingCntry') != "862") {
    // hide 'additional shipping' selection for creates
    if ((addressMode == 'newAddress' || addressMode == 'copyAddress') && cmr.currentRequestType == 'C') {
      cmr.hideNode('radiocont_ZD01');
    }
    // if (FormManager.getActualValue('cmrIssuingCntry') == "862") {
    // cmr.showNode('radiocont_ZD01');
    // }
  }
}

function updateAbbrevNmLocnGRCYTR(cntry, addressMode, saving, finalSave, force) {
  console.log(">>>> updateAbbrevNmLocnGRCYTR ");
  var role = null;
  var reqType = null;
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
    reqType = FormManager.getActualValue('reqType');
  }
  if (reqType != 'C') {
    return;
  }
  if (role != 'Requester') {
    // do not update for non-requesters
    return;
  } else {
    if (finalSave || force || addressMode == 'COPY') {
      var copyTypes = document.getElementsByName('copyTypes');
      var copyingToA = false;
      if (copyTypes != null && copyTypes.length > 0) {
        copyTypes.forEach(function(input, i) {
          if (input.value == 'ZS01' && input.checked) {
            copyingToA = true;
          }
        });
      }
      var addrType = FormManager.getActualValue('addrType');
      if (addrType == 'ZS01' || copyingToA) {
        // generate Abbreviated Name/Location
        var abbrevNm = FormManager.getActualValue('custNm1');
        var abbrevLocn = FormManager.getActualValue('city1');
        if (abbrevNm && abbrevNm.length > 22) {
          abbrevNm = abbrevNm.substring(0, 22);
        }
        if (abbrevLocn && abbrevLocn.length > 12) {
          abbrevLocn = abbrevLocn.substring(0, 12);
        }

        if (cntry !== null && cntry == SysLoc.TURKEY) {
          abbrevNm = modifyCharForTurk(abbrevNm);
          abbrevLocn = modifyCharForTurk(abbrevLocn);
        }

        FormManager.setValue('abbrevNm', abbrevNm);
        FormManager.setValue('abbrevLocn', abbrevLocn);
      }
    }
  }
}

function addrFunctionForGR(cntry, addressMode, saving) {
  console.log(">>>> addrFunctionForGR ");
  if (!saving) {
    var cntryCd = FormManager.getActualValue('cmrIssuingCntry');
    var custType = FormManager.getActualValue('custGrp');

    if ((addressMode == 'updateAddress' || addressMode == 'copyAddress') && FormManager.getActualValue('landCntry') == '') {
      FormManager.setValue('landCntry', cmr.oldlandcntry);
    }
    // for cross border
    if ((custType == 'CROSS' || FormManager.getActualValue('addrType') == 'ZS01') && cmr.currentRequestType == 'U') {
      FormManager.readOnly('landCntry');
    }
    checkAndAddValidator('landCntry', Validators.REQUIRED, [ 'Country (Landed)' ]);
  }
}

function retainLandCntryValuesOnCopy() {
  console.log(">>>> retainLandCntryValuesOnCopy ");
  if ((cmr.addressMode == 'copyAddress') && FormManager.getActualValue('landCntry') == '') {
    FormManager.setValue('landCntry', cmr.oldlandcntry);
  }
}

function disableAddrFieldsGR() {
  console.log(">>>> disableAddrFieldsGR ");
  if (FormManager.getActualValue('addrType') != 'ZP01') {
    FormManager.setValue('taxOffice', '');
    FormManager.disable('taxOffice');
  } else {
    FormManager.enable('taxOffice');
  }

  // GR - Phone - for Sold-to and Ship-to
  if ((FormManager.getActualValue('addrType') == 'ZS01' || FormManager.getActualValue('addrType') == 'ZD01')) {
    FormManager.enable('custPhone');
  } else {
    FormManager.setValue('custPhone', '');
    FormManager.disable('custPhone');
  }

  if (FormManager.getActualValue('addrType') == 'ZP01' || FormManager.getActualValue('addrType') == 'ZS01') {
    FormManager.enable('poBox');
  } else {
    FormManager.setValue('poBox', '');
    FormManager.disable('poBox');

  }

  var landCntry = FormManager.getActualValue('landCntry');
  if (!(FormManager.getActualValue('custGrp') == 'CROSS' || isUpdateReqCrossborder()) && landCntry == 'GR'
      && (FormManager.getActualValue('addrType') == 'ZP01' || FormManager.getActualValue('addrType') == 'ZS01')) {
    FormManager.readOnly('landCntry');
  } else {
    FormManager.enable('landCntry');
  }
}

function isUpdateReqCrossborder() {
  console.log(">>>> isUpdateReqCrossborder ");
  if (!(FormManager.getActualValue('custGrp') == 'LOCAL')) {
    for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
      recordList = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
      if (_allAddressData != null && _allAddressData[i] != null) {
        if (_allAddressData[i].addrType[0] == 'ZS01') {
          return _allAddressData[i].landCntry[0] != 'GR';
        }
      }
    }
  }
  return false;
}

function setTypeOfCustomerBehaviorForGR() {
  console.log(">>>> setTypeOfCustomerBehaviorForGR ");

  if (FormManager.getActualValue('reqType') == 'C') {
    FormManager.hide('CrosSubTyp', 'crosSubTyp');
  } else if (FormManager.getActualValue('reqType') == 'U') {
    FormManager.show('CrosSubTyp', 'crosSubTyp');
    var role = FormManager.getActualValue('userRole').toUpperCase();
    if (role == 'REQUESTER') {
      FormManager.readOnly('crosSubTyp');
    } else if (role == 'PROCESSOR') {
      FormManager.enable('crosSubTyp');
    }
  }
}

function addPostalCodeLenForTurGreCypValidator() {
  console.log(">>>> addPostalCodeLenForTurGreCypValidator ");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var postal_cd = FormManager.getActualValue('postCd');
        var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
        var custType = FormManager.getActualValue('custGrp');
        if (custType != 'CROSS') {
          if (cmrIssuingCntry == SysLoc.GREECE || cmrIssuingCntry == SysLoc.TURKEY) {
            if (postal_cd.length != 5 && postal_cd.length != '') {
              return new ValidationResult(null, false, 'Postal Code should be 5 characters long.');
            }
          } else if (cmrIssuingCntry == SysLoc.CYPRUS) {
            if (postal_cd.length != 4 && postal_cd.length != '') {
              return new ValidationResult(null, false, 'Postal Code should be 4 characters long.');
            }
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function setPostalCodeTurGreCypValidator() {
  console.log(">>>> setPostalCodeTurGreCypValidator ");
  var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  var postal_cd = FormManager.getActualValue('postCd');
  var custType = FormManager.getActualValue('custGrp');
  if (custType == 'CROSS') {
    FormManager.removeValidator('postCd', Validators.NUMBER);
  } else {
    if (cmrIssuingCntry == SysLoc.GREECE || cmrIssuingCntry == SysLoc.TURKEY) {
      if (postal_cd.length == 5) {
        FormManager.addValidator('postCd', Validators.NUMBER, [ 'Postal Code' ]);
      }
    } else if (cmrIssuingCntry == SysLoc.CYPRUS) {
      if (postal_cd.length == 4) {
        FormManager.addValidator('postCd', Validators.NUMBER, [ 'Postal Code' ]);
      }
    }
  }
}

function abbrvLocMandatory() {
  console.log(">>>> abbrvLocMandatory ");
  var interval = new Object();
  interval = setInterval(function() {
    var role = FormManager.getActualValue('userRole').toUpperCase();
    var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
    FormManager.addValidator('abbrevNm', Validators.NO_QUOTATION, [ 'Abbreviated Name (TELX1)' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('abbrevLocn', Validators.NO_QUOTATION, [ 'Abbreviated Location' ], 'MAIN_CUST_TAB');

    if (viewOnlyPage != 'true') {
      if (role != 'REQUESTER') {
        FormManager.addValidator('abbrevLocn', Validators.REQUIRED, [ 'AbbrevLocation' ], 'MAIN_CUST_TAB');
      }
    } else {
      clearInterval(interval);
    }

  }, 1000);
}

function abbrvLocMandatoryOnChange() {
  console.log(">>>> abbrvLocMandatoryOnChange ");
  var role = FormManager.getActualValue('userRole').toUpperCase();
  FormManager.addValidator('abbrevNm', Validators.NO_QUOTATION, [ 'Abbreviated Name (TELX1)' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('abbrevLocn', Validators.NO_QUOTATION, [ 'Abbreviated Location' ], 'MAIN_CUST_TAB');
  dojo.connect(FormManager.getField('abbrevLocn'), 'onChange', function(value) {
    if (role != 'REQUESTER') {
      FormManager.addValidator('abbrevLocn', Validators.REQUIRED, [ 'AbbrevLocation' ], 'MAIN_CUST_TAB');
    }
  });
}

function hideCustPhoneonSummary() {
  console.log(">>>> hideCustPhoneonSummary ");
  setInterval(function() {
    if (openAddressDetails.addrType == 'ZS01') {
      cmr.hideNode('custPhone_view');
      $('label[for="custPhone_view"]').hide();
    } else {
      cmr.showNode('custPhone_view');
      $('label[for="custPhone_view"]').show();
    }
  }, 1000);

}

function addHandlerForCustSubTypeBpGRTRCY() {
  console.log(">>>> addHandlerForCustSubTypeBpGRTRCY ");
  if (_custSubTypeHandler == null) {
    _custSubTypeHandler = dojo.connect(FormManager.getField('custSubGrp'), 'onChange', function(value) {
      setCustSubTypeBpGRTRCY();
    });
  }
}

function setCustSubTypeBpGRTRCY() {
  console.log(">>>> setCustSubTypeBpGRTRCY ");
  var custType = FormManager.getActualValue('custSubGrp');
  if (FormManager.getActualValue('cmrIssuingCntry') != SysLoc.TURKEY) {
    return;
  }
  if (custType == 'BUSPR') {
    FormManager.setValue('clientTier', '');
    FormManager.setValue('isuCd', '8B');
  } else if (custType == 'INTER') {
    FormManager.setValue('clientTier', '');
    FormManager.setValue('isuCd', '21');
  } else if (custType != 'XINT' && custType != 'XBP') {
    FormManager.enable('clientTier');
    FormManager.enable('isuCd');
  }

  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.GREECE
      && (FormManager.getActualValue('custSubGrp') != 'COMME' || FormManager.getActualValue('custSubGrp') != 'CROSS' || FormManager.getActualValue('custSubGrp') != 'GOVRN' || FormManager
          .getActualValue('custSubGrp') != 'PRICU')) {
    setISRValuesGR();
  } else if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.TURKEY) {
    console.log("skip set ISR.");
  } else {
    setISRValues();
  }
}

function viewOnlyAddressDetails() {
  console.log(">>>> viewOnlyAddressDetails ");
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');

  if (viewOnlyPage == 'true' && (cmrIssuingCntry == SysLoc.GREECE || cmrIssuingCntry == SysLoc.CYPRUS)) {
    $('label[for="addrTxt_view"]').text('Street Address:');
    $('label[for="addrTxt2_view"]').text('Occupation:');
  }

  if (viewOnlyPage == 'true' && cmrIssuingCntry == SysLoc.GREECE) {
    $('label[for="addrTxt_view"]').text('Street Address:');
    $('label[for="custNm2_view"]').text('Customer Name Con\'t');
    $('label[for="custNm4_view"]').text('Att. Person');
    $('label[for="addrTxt2_view"]').text(' Address Con\'t/Occupation');
  }

  if (viewOnlyPage == 'true' && cmrIssuingCntry == SysLoc.TURKEY) {
    $('label[for="addrTxt_view"]').text('Street Address:');
    $('label[for="addrTxt2_view"]').text('Street Con' + '\'' + 't:');
    $('label[for="dept_view"]').text('District:');
  }
}

function modifyCharForTurk(field) {
  console.log(">>>> modifyCharForTurk ");

  if (field != null && field.length > 0) {
    var modifiedVal = field;

    modifiedVal = modifiedVal.replace(/Ç/g, 'C');
    modifiedVal = modifiedVal.replace(/ç/g, 'c');
    modifiedVal = modifiedVal.replace(/Ğ/g, 'G');
    modifiedVal = modifiedVal.replace(/ğ/g, 'g');
    modifiedVal = modifiedVal.replace(/İ/g, 'I');
    modifiedVal = modifiedVal.replace(/ı/g, 'i');

    modifiedVal = modifiedVal.replace(/Ö/g, 'O');
    modifiedVal = modifiedVal.replace(/ö/g, 'o');
    modifiedVal = modifiedVal.replace(/Ş/g, 'S');
    modifiedVal = modifiedVal.replace(/ş/g, 's');
    modifiedVal = modifiedVal.replace(/Ü/g, 'U');
    modifiedVal = modifiedVal.replace(/ü/g, 'u');

    return modifiedVal;
  }
}
// Not used
function addTaxCodeForProcessorValidator() {
  console.log(">>>> addTaxCodeForProcessorValidator ");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var specialTaxCd = FormManager.getActualValue('specialTaxCd');
        var role = FormManager.getActualValue('userRole');
        if (FormManager.getActualValue('reqType') == 'C') {
          if (role == GEOHandler.ROLE_PROCESSOR && (specialTaxCd == null || specialTaxCd.length == 0)) {
            return new ValidationResult({
              id : 'specialTaxCd',
              type : 'text',
              name : 'specialTaxCd'
            }, false, 'Tax Code/ Code IVA should not be blank.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function addAddrValidationForProcItaly() {
  console.log(">>>> addAddrValidationForProcItaly ");
  var requestType = null;
  requestType = FormManager.getActualValue('reqType');
  var addrType = FormManager.getActualValue('addrType');
  var landCntryVal = FormManager.getActualValue('landCntry');
  if (requestType == 'C' && addrType != 'ZS01') {
    if (landCntryVal == 'IT') {
      FormManager.addValidator('stateProv', Validators.REQUIRED, [ 'State/Province' ], null);
    } /*
       * else { FormManager.addValidator('stateProvItaly', Validators.REQUIRED, [
       * 'State/Province' ], null); }
       */
  }
}

/*
 * Author: Dennis T Natad Purpose: Function for displayedValue retrieval in the
 * case of select objects and similar objects.
 */
function getDescription(fieldId) {
  console.log(">>>> getDescription ");
  var field = dijit.byId(fieldId);
  return field.displayedValue;
}

function showHideOtherStateProvIT() {
  console.log(">>>> showHideOtherStateProvIT ");
  var stateProvIT = FormManager.getActualValue('stateProvItaly');
  var reqType = FormManager.getActualValue('reqType');
  var addrType = FormManager.getActualValue('addrType');
  var landCntryVal = FormManager.getActualValue('landCntry');
  // Defect :1474362, 1534200
  if (reqType == 'U' || reqType == 'X') {
    if (addrType == 'ZS01') {
      console.log(">>Hide stateProvItalyOth when installing address " + addrType);
      FormManager.hide('StateProvItalyOth', 'stateProvItalyOth');
    }
  }

  if (stateProvIT != 'OTH') {
    // hide the OTHER field
    FormManager.hide('StateProvItalyOth', 'stateProvItalyOth');
    FormManager.resetValidations('stateProvItalyOth');
    FormManager.setValue('crossbStateProvPostalMapIT', stateProvIT);
    // Defect 1452033 :Cross Boarder
    if (addrType == 'ZI01') {
      FormManager.setValue('poBoxPostCd', getDescription('crossbStateProvPostalMapIT'));
    }

  } else if (landCntryVal != 'IT' && addrType != 'ZS01') {
    console.log("show stateProvItalyOth for non ITand non installing address.");
    FormManager.show('StateProvItalyOth', 'stateProvItalyOth');
    FormManager.addValidator('stateProvItalyOth', Validators.REQUIRED, [ 'Other State/Province' ], null);
  } else {
    FormManager.resetValidations('stateProvItalyOth');
    FormManager.hide('StateProvItalyOth', 'stateProvItalyOth');
  }
}

function autoSetAbbrevNmOnChanageIT() {
  console.log(">>>> autoSetAbbrevNmOnChanageIT ");
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (reqType != 'C') {
    return;
  }
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (role != 'REQUESTER') {
    return;
  }
  var zs01ReqId = FormManager.getActualValue('reqId');
  var qParams = {
    REQ_ID : zs01ReqId,
    ADDR_TYPE : "ZS01",
  };
  var _abbrevNmValue = null;
  var result = cmr.query('ADDR.GET.CUSTNM1.BY_REQID_ADDRTYP', qParams);
  _abbrevNmValue = result.ret1;

  if (_abbrevNmValue != null && _abbrevNmValue.length > 22) {
    _abbrevNmValue = _abbrevNmValue.substr(0, 22);
  }
  if (_abbrevNmValue == undefined) {
    FormManager.setValue('abbrevNm', '');
  } else {
    FormManager.setValue('abbrevNm', _abbrevNmValue);
  }
  console.log("AbbrevNM>>" + FormManager.getActualValue('abbrevNm'));
}

function autoSetAbbrevLocnOnChangeIT() {
  console.log(">>>> autoSetAbbrevLocnOnChangeIT ");
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (reqType != 'C') {
    return;
  }
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (role != 'REQUESTER') {
    return;
  }
  var _abbrevLocn = null;
  var _zs01ReqId = FormManager.getActualValue('reqId');
  var qParams = {
    REQ_ID : _zs01ReqId,
    ADDR_TYPE : "ZS01",
  };
  var _result = cmr.query('ADDR.GET.CITY1.BY_REQID_ADDRTYP', qParams);
  _abbrevLocn = _result.ret1;

  if (_abbrevLocn != null && _abbrevLocn.length > 12) {
    _abbrevLocn = _abbrevLocn.substr(0, 12);
  }
  if (_abbrevLocn == undefined) {
    FormManager.setValue('abbrevLocn', '');
  } else {
    FormManager.setValue('abbrevLocn', _abbrevLocn);
  }
  console.log("abbrevLocn>>" + FormManager.getActualValue('abbrevLocn'));
}

function addPhoneValidatorEMEA() {
  console.log(">>>> addPhoneValidatorEMEA ");
  FormManager.addValidator('custPhone', Validators.DIGIT, [ 'Phone #' ]);
}

function canRemoveAddress(value, rowIndex, grid) {
  console.log(">>>> canRemoveAddress ");
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  if (cntry != '758') {
    var rowData = grid.getItem(rowIndex);
    var importInd = rowData.importInd[0];
    var reqType = FormManager.getActualValue('reqType');
    if ('U' == reqType && 'Y' == importInd) {
      return false;
    }
    return true;
  } else {
    var type = FormManager.getActualValue('reqType');
    if ('C' == type) {
      var rowData = grid.getItem(rowIndex);
      // var importInd = rowData.importInd[0];
      var addrType = rowData.addrType[0];
      var ifProspect = FormManager.getActualValue('prospLegalInd');
      if (dijit.byId('prospLegalInd')) {
        ifProspect = dijit.byId('prospLegalInd').get('checked') ? 'Y' : 'N';
      }
      if (ifProspect == 'Y') {
        return (addrType != 'ZI01');
      } else {
        return ((addrType != 'ZI01') && (ifProspect != 'Y'));
      }
    } else {
      // no allowing to remove for updates
      return false;
    }
  }
}

function canUpdateAddress(value, rowIndex, grid) {
  console.log(">>>> canUpdateAddress ");
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  if (cntry == '758') {
    var rowData = grid.getItem(rowIndex);
    var importInd = rowData.importInd[0];
    var type = FormManager.getActualValue('reqType');
    var ifProspect = FormManager.getActualValue('prospLegalInd');
    if (dijit.byId('prospLegalInd')) {
      ifProspect = dijit.byId('prospLegalInd').get('checked') ? 'Y' : 'N';
    }
    if ('C' == type) {
      if (ifProspect == 'Y') {
        return true;
      } else {
        return ((importInd != 'Y') && (ifProspect != 'Y'));
      }
    } else {
      var cmrNo = FormManager.getActualValue('cmrNo');
      var company = FormManager.getActualValue('company');
      if (cmrNo == company) {
        return true;
      } else {
        var addrType = rowData.addrType[0];
        var currParCMR = rowData.parCmrNo[0];
        if (currParCMR == cmrNo) {
          return true;
        } else {
          return false;
        }
      }
    }
  } else {
    return true;
  }
}
// Defect 1509289 :Mukesh
function canCopyAddress(value, rowIndex, grid) {
  console.log(">>>> canCopyAddress ");
  var cntry = FormManager.getActualValue('cmrIssuingCntry');

  if (cntry == '726') {
    return shouldShowCopyAddressInGrid(rowIndex, grid);
  }

  if (cntry != '758') {
    return true;
  }
  if (CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 2) {
    return false;
  }
  return true;
}

function shouldShowCopyAddressInGrid(rowIndex, grid) {
  console.log(">>>> shouldShowCopyAddressInGrid ");
  if (grid != null && rowIndex != null && grid.getItem(rowIndex) != null) {
    if (grid.getItem(rowIndex).addrType[0] == 'ZP01' && grid.getItem(rowIndex).landCntry[0] == 'GR') {
      return false;
    }
  }
  return true;
}

function ADDRESS_GRID_showCheck(value, rowIndex, grid) {
  console.log(">>>> ADDRESS_GRID_showCheck ");
  return canRemoveAddress(value, rowIndex, grid);
}

function disableHideFieldsOnAddrIT() {
  console.log(">>>> disableHideFieldsOnAddrIT ");
  var addrType = FormManager.getActualValue('addrType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var landCntryVal = FormManager.getActualValue('landCntry');

  // Story 1377871 -added Postal Address in case of Billing address :Mukesh
  /*
   * if (addrType != '' && addrType == 'ZP01') {
   * FormManager.show('BillingPstlAddr', 'billingPstlAddr'); } else {
   * FormManager.clearValue('billingPstlAddr');
   * FormManager.hide('BillingPstlAddr', 'billingPstlAddr'); }
   */

  FormManager.setValue('poBoxPostCd', getDescription('crossbStateProvPostalMapIT'));

  /*
   * if (addrType != 'ZI01' && !FormManager.getField('addrType_ZI01').checked &&
   * cmr.addressMode == 'newAddress') { console.log("AddrType other than ZI01");
   * FormManager.setValue('postCd', ''); // for addressMode=newAddress only }
   */

  /*
   * if (addrType != '' && (FormManager.getField('addrType_ZS01').checked ||
   * addrType == 'ZS01')) { // FormManager.hide('StateProv', 'stateProv');
   * FormManager.resetValidations('stateProv');
   * FormManager.hide('StateProvItaly', 'stateProvItaly');
   * FormManager.resetValidations('stateProvItaly'); } else if (addrType != '' &&
   * !FormManager.getField('addrType_ZS01').checked && addrType != 'ZS01') {
   */
  if (landCntryVal == 'IT') {
    FormManager.show('StateProv', 'stateProv');
  }
  /*
   * if (landCntryVal != 'IT') { FormManager.show('StateProvItaly',
   * 'stateProvItaly'); }
   */
  // }
  if (addrType != '' && FormManager.getField('addrType_ZP01').checked && role == 'REQUESTER') {
    FormManager.show('StreetAbbrev', 'streetAbbrev');
    FormManager.show('AddrAbbrevName', 'addrAbbrevName');
    FormManager.show('AddrAbbrevLocn', 'addrAbbrevLocn');
    if (cmr.addressMode == 'newAddress') {
      FormManager.clearValue('addrAbbrevName');
      FormManager.clearValue('streetAbbrev');
    }
    FormManager.readOnly('streetAbbrev');
    FormManager.readOnly('addrAbbrevName');
    FormManager.readOnly('addrAbbrevLocn');

  } else if (addrType != '' && FormManager.getField('addrType_ZP01').checked && role == 'PROCESSOR') {
    FormManager.show('StreetAbbrev', 'streetAbbrev');
    FormManager.show('AddrAbbrevName', 'addrAbbrevName');
    FormManager.show('AddrAbbrevLocn', 'addrAbbrevLocn');
    FormManager.enable('streetAbbrev');
    FormManager.enable('addrAbbrevName');
    FormManager.enable('addrAbbrevLocn');
  } else if (addrType != '' && !FormManager.getField('addrType_ZP01').checked) {
    FormManager.hide('StreetAbbrev', 'streetAbbrev');
    FormManager.hide('AddrAbbrevName', 'addrAbbrevName');
    FormManager.hide('AddrAbbrevLocn', 'addrAbbrevLocn');
    FormManager.resetValidations('streetAbbrev');
    FormManager.resetValidations('addrAbbrevName');
    FormManager.resetValidations('addrAbbrevLocn');
  } else {
    FormManager.hide('StreetAbbrev', 'streetAbbrev');
    FormManager.hide('AddrAbbrevName', 'addrAbbrevName');
    FormManager.hide('AddrAbbrevLocn', 'addrAbbrevLocn');
    FormManager.resetValidations('streetAbbrev');
    FormManager.resetValidations('addrAbbrevName');
    FormManager.resetValidations('addrAbbrevLocn');
  }
}

function setStreetAbbrevIT() {
  console.log(">>>> setStreetAbbrevIT ");
  var reqType = FormManager.getActualValue('reqType');
  /*
   * if (reqType != 'C') { return new ValidationResult(null, true); }
   */
  var addrType = FormManager.getActualValue('addrType');
  if (addrType != null && (addrType == 'ZP01' || FormManager.getField('addrType_ZP01').checked)) {
    FormManager.setValue('streetAbbrev', FormManager.getActualValue('addrTxt').substring(0, 18));
  }
}

function setAddrAbbrevNameIT() {
  console.log(">>>> setAddrAbbrevNameIT ");
  var reqType = FormManager.getActualValue('reqType');
  /*
   * if (reqType != 'C') { return new ValidationResult(null, true); }
   */
  var addrType = FormManager.getActualValue('addrType');
  if (addrType != null && (addrType == 'ZP01' || FormManager.getField('addrType_ZP01').checked)) {
    FormManager.setValue('addrAbbrevName', FormManager.getActualValue('custNm1').substring(0, 22));
  }
}

function setAddrAbbrevLocnIT() {
  console.log(">>>> setAddrAbbrevLocnIT ");
  var reqType = FormManager.getActualValue('reqType');
  /*
   * if (reqType != 'C') { return new ValidationResult(null, true); }
   */
  var addrType = FormManager.getActualValue('addrType');
  if (addrType != null && (addrType == 'ZP01' || FormManager.getField('addrType_ZP01').checked)) {
    FormManager.setValue('addrAbbrevLocn', FormManager.getActualValue('city1').substring(0, 12));
  }
}

/**
 * Override to not format for greece and cyprus
 */
function streetValueFormatter(value, rowIndex) {
  console.log(">>>> streetValueFormatter ");
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  if (cntry == '726' || cntry == '666') {
    return value;
  }
  var rowData = this.grid.getItem(rowIndex);
  var streetCont = rowData.addrTxt2;
  if (streetCont && streetCont[0]) {
    return value + '<br>' + streetCont;
  }
  return value;
}

// Defect 1494371: Postal Code for San Marino optional : Mukesh
function addSMAndVAPostalCDValidator() {
  console.log(">>>> addSMAndVAPostalCDValidator ");
  var landCntry = FormManager.getActualValue('landCntry');
  if (landCntry != 'undefined' && 'SM' == landCntry) {
    FormManager.addValidator('postCd', Validators.REQUIRED, [ 'Postal Code for San Marino - SM' ], 'MAIN_NAME_TAB');
  }
  if (landCntry != 'undefined' && 'VA' == landCntry) {
    FormManager.addValidator('postCd', Validators.REQUIRED, [ 'Postal Code for Holy See (Vatican City State) - VA' ], 'MAIN_NAME_TAB');
  }
}

function doValidateFiscalDataModal() {
  console.log(">>>> doValidateFiscalDataModal ");
  cmr.setModalTitle('validateFiscalDataModal', 'Process Fiscal Data');
  var vat = FormManager.getActualValue('vat');
  var fiscalCd = FormManager.getActualValue('taxCd1');
  if (!vat && !fiscalCd) {
    FormManager.setValue('fiscalDataStatus', 'F');
    cmr.showAlert("No company addresses with this fiscal data found.", null, null, null);
  } else {
    var qParams = {
      VAT : vat,
      FISCAL_CD : fiscalCd,
      ZZKV_CUSNO : FormManager.getActualValue('cmrNo'),
      MANDT : cmr.MANDT
    };
    var result = cmr.query('GET.RDC_FISCAL_DATA_NEW', qParams);
    if (result.ret1 != null && result.ret1 != '') {
      cmr.showModal('validateFiscalDataModal');
      loadFiscalDataModal(result);
    } else {
      FormManager.setValue('fiscalDataStatus', 'F');
      cmr.showAlert("No company addresses with this fiscal data found.", null, null, null);
    }
  }
}

function enableCMRNUMForPROCESSOR() {
  console.log(">>>> enableCMRNUMForPROCESSOR ");
  var isProspect = FormManager.getActualValue('prospLegalInd');
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (reqType != 'C') {
    return;
  }
  if (dijit.byId('prospLegalInd')) {
    isProspect = dijit.byId('prospLegalInd').get('checked') ? 'Y' : 'N';
  }
  console.log("validateCMRNumberForLegacy ifProspect:" + isProspect);
  if ('Y' == isProspect) {
    FormManager.readOnly('cmrNo');
  } else if (role == "PROCESSOR") {
    FormManager.enable('cmrNo');
  } else {
    FormManager.readOnly('cmrNo');
  }
}

function addEnterpriseValidator() {
  console.log(">>>> addEnterpriseValidator ");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var enterpriseNo = FormManager.getActualValue('enterprise');

        if (enterpriseNo != null && enterpriseNo != undefined && enterpriseNo != '') {
          if (!enterpriseNo.match("^[0-9]*$")) {
            return new ValidationResult(null, false, 'Enterprise Number should be numeric only.');
          } else if ((enterpriseNo.length != 6)) {
            return new ValidationResult(null, false, 'Enterprise Number should be 6 digit long.');
          }
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function getExitingValueOfCTCAndIsuCD() {
  console.log(">>>> getExitingValueOfCTCAndIsuCD");
  var reqType = FormManager.getActualValue('reqType');
  var requestId = FormManager.getActualValue('reqId');

  if (reqType != 'U' || _oldIsuCd != null || _oldCtc != null) {
    return;
  }

  var result = cmr.query('GET.CLIENT_TIER_EMBARGO_CD_OLD_BY_REQID', {
    REQ_ID : requestId,
  });

  if (result != null && result != '') {
    _oldCtc = result.ret1;
    _oldIsuCd = result.ret3;
  }

}

// CREATCMR-4293

function clientTierValidator() {
  console.log(">>>> clientTierValidator ");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        return validatorISUCTCEntGR();
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function setValuesWRTIsuCtc(ctc) {
  console.log(">>>> setValuesWRTIsuCtc ");
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var isu = FormManager.getActualValue('isuCd');
  if (ctc == null) {
    var ctc = FormManager.getActualValue('clientTier');
  }
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  if (isu == '34' && ctc == 'Q') {
    FormManager.setValue('enterprise', '822830');
    FormManager.setValue('salesTeamCd', '000000');
    FormManager.setValue('repTeamMemberNo', '000000');
    FormManager.setValue('salesBusOffCd', '000');
  } else if (isu == '36' && ctc == 'Y') {
    FormManager.setValue('enterprise', '');
    FormManager.setValue('salesTeamCd', '000000');
    FormManager.setValue('repTeamMemberNo', '000000');
    FormManager.setValue('salesBusOffCd', '000');
  } else if (isu == '32' && ctc == 'T') {
    FormManager.setValue('enterprise', '985985');
    FormManager.setValue('salesTeamCd', '000000');
    FormManager.setValue('repTeamMemberNo', '000000');
    FormManager.setValue('salesBusOffCd', '000');
  } else if ((isu == '5K' || isu == '21') && ctc == '') {
    FormManager.setValue('enterprise', '985999');
    FormManager.setValue('salesTeamCd', '000000');
    FormManager.setValue('repTeamMemberNo', '000000');
    FormManager.setValue('salesBusOffCd', '000');
  }
  if (role == 'REQUESTER') {
    FormManager.removeValidator('enterprise', Validators.REQUIRED);
  } else {
    FormManager.addValidator('enterprise', Validators.REQUIRED, [ 'Enterprise' ]);
  }
  lockUnlockField();
}

function addPpsCeidValidator() {
  console.log(">>>> addPpsCeidValidator ");
  var _custType = FormManager.getActualValue('custSubGrp');
  var reqType = FormManager.getActualValue('reqType');

  if (reqType == 'C') {
    if (_custType == 'BUSPR' || _custType == 'XBP') {
      FormManager.show('PPSCEID', 'ppsceid');
      FormManager.enable('ppsceid');
      FormManager.addValidator('ppsceid', Validators.REQUIRED, [ 'PPS CEID' ], 'MAIN_IBM_TAB');
    } else {
      FormManager.setValue('ppsceid', '');
      FormManager.readOnly('ppsceid');
      FormManager.removeValidator('ppsceid', Validators.REQUIRED);
    }
  }
}
function addressQuotationValidatorGR() {
  console.log(">>>> addressQuotationValidatorGR ");
  // CREATCMR-788
  FormManager.addValidator('abbrevNm', Validators.NO_QUOTATION, [ 'Abbreviated Name (TELX1)' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('abbrevLocn', Validators.NO_QUOTATION, [ 'Abbreviated Location' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('custNm1', Validators.NO_QUOTATION, [ 'Customer Name' ]);
  FormManager.addValidator('custNm2', Validators.NO_QUOTATION, [ 'Customer Name Con\'t' ]);
  FormManager.addValidator('addrTxt', Validators.NO_QUOTATION, [ 'Street Address' ]);
  FormManager.addValidator('addrTxt2', Validators.NO_QUOTATION, [ 'Address Con\'t/Occupation' ]);
  FormManager.addValidator('city1', Validators.NO_QUOTATION, [ 'City' ]);
  FormManager.addValidator('postCd', Validators.NO_QUOTATION, [ 'Postal Code' ]);
  FormManager.addValidator('custNm4', Validators.NO_QUOTATION, [ 'Att. Person' ]);
  FormManager.addValidator('poBox', Validators.NO_QUOTATION, [ 'PO Box' ]);
  FormManager.addValidator('custPhone', Validators.NO_QUOTATION, [ 'Phone #' ]);
}

function checkCmrUpdateBeforeImport() {
  console.log(">>>> checkCmrUpdateBeforeImport ");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {

        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        var cmrNo = FormManager.getActualValue('cmrNo');
        var reqId = FormManager.getActualValue('reqId');
        var reqType = FormManager.getActualValue('reqType');
        var uptsrdc = '';
        var lastupts = '';

        if (reqType == 'C') {
          // console.log('reqType = ' + reqType);
          return new ValidationResult(null, true);
        }

        var resultsCC = cmr.query('GETUPTSRDC', {
          COUNTRY : cntry,
          CMRNO : cmrNo,
          MANDT : cmr.MANDT
        });

        if (resultsCC != null && resultsCC != undefined && resultsCC.ret1 != '') {
          uptsrdc = resultsCC.ret1;
          // console.log('lastupdatets in RDC = ' + uptsrdc);
        }

        var results11 = cmr.query('GETUPTSADDR', {
          REQ_ID : reqId
        });
        if (results11 != null && results11 != undefined && results11.ret1 != '') {
          lastupts = results11.ret1;
          // console.log('lastupdatets in CreateCMR = ' + lastupts);
        }

        if (lastupts != '' && uptsrdc != '') {
          if (uptsrdc > lastupts) {
            return new ValidationResult(null, false, 'This CMR has a new update , please re-import this CMR.');
          } else {
            return new ValidationResult(null, true);
          }
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_GENERAL_TAB', 'frmCMR');
}

function lockUnlockField() {
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  if (cntry == SysLoc.GREECE) {
    lockUnlockFieldForGR();
  }
}

function lockUnlockFieldForGR() {
  console.log(">>>> lockUnlockFieldForGR");
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var _custGrpSet = new Set([ 'COMME', 'GOVRN' ]);

  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    FormManager.readOnly('isuCd');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('enterprise');
    FormManager.readOnly('repTeamMemberNo');
    FormManager.readOnly('salesTeamCd');
    FormManager.readOnly('salesBusOffCd');
    FormManager.readOnly('ppsceid');

  } else if (!_custGrpSet.has(custSubGrp) && custSubGrp != '') {
    FormManager.readOnly('isuCd');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('enterprise');
    FormManager.readOnly('repTeamMemberNo');
    FormManager.readOnly('salesTeamCd');
    FormManager.readOnly('salesBusOffCd');

  } else if (_custGrpSet.has(custSubGrp)) {
    FormManager.enable('isuCd');
    FormManager.enable('clientTier');
    FormManager.enable('enterprise');
    FormManager.readOnly('repTeamMemberNo');
    FormManager.readOnly('salesTeamCd');
    FormManager.readOnly('salesBusOffCd');
  }
}

function setClientTierValuesGR() {
  console.log(">>>> setClientTierValuesGR");
  var reqType = FormManager.getActualValue('reqType');
  var isuCd = FormManager.getActualValue('isuCd');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');

  if (isuCd == '34') {
    FormManager.setValue('clientTier', 'Q');
  } else if (isuCd == '36') {
    FormManager.setValue('clientTier', 'Y');
  } else if (isuCd == '32') {
    FormManager.setValue('clientTier', 'T');
  } else {
    FormManager.setValue('clientTier', '');
  }
  addRemoveValidator();
  setEnterpriseValues();
  lockUnlockField();
}

function setEnterpriseValues() {
  console.log(">>>> setEnterpriseValues");
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  if (cntry == SysLoc.GREECE) {
    setEntepriseGR();
  }
}

function setEntepriseGR() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }

  console.log(">>>> setEntepriseCY");
  var isuCd = FormManager.getActualValue('isuCd');
  var clientTier = FormManager.getActualValue('clientTier');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var custSubGrpSet21 = new Set([ 'BUSPR', 'CRBUS', 'CRINT', 'IBMEM', 'INTER' ]);
  var custSubGrpSet34 = new Set([ 'COMME', 'GOVRN', 'PRICU', 'SAASP' ]);
  var custSubGrpSet = new Set([ 'COMME', 'GOVRN' ]);

  var isuCtc = isuCd + clientTier;

  if (custSubGrpSet21.has(custSubGrp) && isuCtc == '21') {
    FormManager.setValue('enterprise', '985999');
  } else if (custSubGrpSet34.has(custSubGrp) && isuCtc == '34Q') {
    FormManager.setValue('enterprise', '822830');
  } else if (custSubGrpSet.has(custSubGrp) && isuCtc == '36Y') {
    FormManager.setValue('enterprise', '');
  } else if (custSubGrpSet34.has(custSubGrp) && isuCtc == '5K') {
    FormManager.setValue('enterprise', '985999');
  } else if (custSubGrpSet34.has(custSubGrp) && isuCtc == '32T') {
    FormManager.setValue('enterprise', '985985');
  } else {
    FormManager.setValue('enterprise', '');
  }

  FormManager.setValue('salesTeamCd', '000000');
  FormManager.setValue('repTeamMemberNo', '000000');
  FormManager.setValue('salesBusOffCd', '000');
  lockUnlockField();
}

function getExitingValueOfCTCAndIsuCD() {
  console.log(">>>> getExitingValueOfCTCAndIsuCD");
  var reqType = FormManager.getActualValue('reqType');
  var requestId = FormManager.getActualValue('reqId');

  if (reqType != 'U' || _oldIsuCd != null || _oldCtc != null) {
    return;
  }

  var result = cmr.query('GET.CLIENT_TIER_EMBARGO_CD_OLD_BY_REQID', {
    REQ_ID : requestId,
  });

  if (result != null && result != '') {
    _oldCtc = result.ret1;
    _oldIsuCd = result.ret3;
    _oldEnt = result.ret4;

  }

}

function validatorISUCTCEntGR() {
  console.log(">>>> validatorISUCTCEntGR");
  var reqType = FormManager.getActualValue('reqType');
  var isuCd = FormManager.getActualValue('isuCd');
  var clientTier = FormManager.getActualValue('clientTier');
  var isuCD323436Set = new Set([ '32', '34', '36' ])

  getExitingValueOfCTCAndIsuCD();
  if (reqType == 'U' && _oldIsuCd == isuCd && _oldCtc == clientTier) {
    return new ValidationResult(null, true);
  } else if (isuCD323436Set.has(isuCd) && clientTier == '') {
    return new ValidationResult(null, true);
  } else if (!isuCD323436Set.has(isuCd) && clientTier != '') {
    return new ValidationResult({
      id : 'clientTier',
      type : 'text',
      name : 'clientTier'
    }, false, 'Client Tier can be blank only.');
  } else if (isuCd == '32' && clientTier != 'T') {
    return new ValidationResult({
      id : 'clientTier',
      type : 'text',
      name : 'clientTier'
    }, false, 'Client Tier can only accept \'T\'.');
  } else if (isuCd == '34' && clientTier != 'Q') {
    return new ValidationResult({
      id : 'clientTier',
      type : 'text',
      name : 'clientTier'
    }, false, 'Client Tier can only accept \'Q\'.');
  } else if (isuCd == '36' && clientTier != 'Y') {
    return new ValidationResult({
      id : 'clientTier',
      type : 'text',
      name : 'clientTier'
    }, false, 'Client Tier can only accept \'Y\'.');
  } else if (reqType == 'C' && FormManager.getActualValue('enterprise') != '') {
    return validatorEnterpriseGR();
  } else {
    return new ValidationResult(null, true);
  }
}

function validatorEnterpriseGR() {
  var isuCd = FormManager.getActualValue('isuCd');
  var clientTier = FormManager.getActualValue('clientTier');
  var enterprise = FormManager.getActualValue('enterprise');
  var isuCdSet1 = new Set([ '21', '5K' ]);

  if (isuCdSet1.has(isuCd) && enterprise != '985999') {
    return new ValidationResult({
      id : 'enterprise',
      type : 'text',
      name : 'enterprise'
    }, false, 'Enterprise can only accept \'985999\'.');
  } else if (isuCd == '34' && enterprise != '822830') {
    return new ValidationResult({
      id : 'enterprise',
      type : 'text',
      name : 'enterprise'
    }, false, 'Enterprise can only accept \'822830\'.');
  } else if (isuCd == '32' && enterprise != '985985') {
    return new ValidationResult({
      id : 'enterprise',
      type : 'text',
      name : 'enterprise'
    }, false, 'Enterprise can only accept \'985985\'.');
  } else if (isuCd == '36' && enterprise != '822840' && enterprise != '822850') {
    return new ValidationResult({
      id : 'enterprise',
      type : 'text',
      name : 'enterprise'
    }, false, 'Enterprise can only accept \'822840\',\'822850\'.');
  } else {
    return new ValidationResult(null, true);
  }
}

function addRemoveClientTierValidator() {
  console.log(">>>> addRemoveClientTierValidator");
  var isuCd = FormManager.getActualValue('isuCd');
  FormManager.resetValidations('clientTier');
  if (isuCd != '32' && isuCd != '34' && isuCd != '36') {
    FormManager.removeValidator('clientTier', Validators.REQUIRED);
  } else {
    FormManager.addValidator('clientTier', Validators.REQUIRED, [ 'Client Tier' ], 'MAIN_IBM_TAB');
  }
}

function addRemoveEnterperiseValidator() {
  var reqType = FormManager.getActualValue('reqType');
  FormManager.resetValidations('enterprise');
  if (reqType == 'C' || (reqType == 'U' && _oldEnt != null && _oldEnt != '')) {
    FormManager.addValidator('enterprise', Validators.REQUIRED, [ 'Enterprise Number' ], 'MAIN_IBM_TAB');
  }

}

function addRemoveValidator() {
  addRemoveClientTierValidator();
  addRemoveEnterperiseValidator();

}

dojo.addOnLoad(function() {
  GEOHandler.EMEA = [ SysLoc.UK, SysLoc.IRELAND, SysLoc.ISRAEL, SysLoc.TURKEY, SysLoc.GREECE, SysLoc.CYPRUS, SysLoc.ITALY ];
  console.log('adding EMEA functions...');
  GEOHandler.addAddrFunction(addEMEALandedCountryHandler, GEOHandler.EMEA);
  GEOHandler.enableCopyAddress(GEOHandler.EMEA, validateEMEACopy, [ 'ZD01', 'ZI01' ]);
  GEOHandler.enableCustomerNamesOnAddress(GEOHandler.EMEA);
  GEOHandler.addAddrFunction(updateMainCustomerNames, GEOHandler.EMEA);
  GEOHandler.setRevertIsicBehavior(false);
  GEOHandler.addAddrFunction(addPhoneValidatorEMEA, [ SysLoc.ISRAEL, SysLoc.GREECE, SysLoc.CYPRUS, SysLoc.TURKEY ]);

  // Greece
  GEOHandler.addAfterConfig(addAfterConfigGR, [ SysLoc.GREECE ]);
  GEOHandler.addAfterConfig(addHandlersForGR, [ SysLoc.GREECE ]);
  GEOHandler.addAfterConfig(addVATDisabler, [ SysLoc.GREECE, SysLoc.CYPRUS ]);
  GEOHandler.addAfterConfig(setTypeOfCustomerBehaviorForGR, [ SysLoc.GREECE ]);
  GEOHandler.addAddrFunction(disableAddrFieldsGR, [ SysLoc.GREECE ]);
  GEOHandler.addAddrFunction(addrFunctionForGR, [ SysLoc.GREECE ]);
  GEOHandler.addAddrFunction(addLatinCharValidator, [ SysLoc.GREECE ]);
  GEOHandler.addAddrFunction(addNonLatinCharValidator, [ SysLoc.GREECE ]);
  GEOHandler.addAddrFunction(addPOBoxValidatorGR, [ SysLoc.GREECE ]);
  GEOHandler.addAddrFunction(updateAddrTypeList, [ SysLoc.TURKEY ]);
  GEOHandler.addAddrFunction(convertToUpperCaseGR, [ SysLoc.GREECE ]);
  GEOHandler.addAddrFunction(preFillTranslationAddrWithSoldToForGR, [ SysLoc.GREECE ]);
  GEOHandler.addAddrFunction(updateAbbrevNmLocnGRCYTR, [ SysLoc.GREECE, SysLoc.CYPRUS, SysLoc.TURKEY ]);
  GEOHandler.registerValidator(addGRAddressTypeValidator, [ SysLoc.GREECE ], null, true);
  GEOHandler.registerValidator(addGRAddressGridValidatorStreetPOBox, [ SysLoc.GREECE ], null, true);
  GEOHandler.registerValidator(addOccupationPOBoxAttnPersonValidatorForGR, [ SysLoc.GREECE ], null, true);
  GEOHandler.registerValidator(addStreetAddressFormValidatorGR, [ SysLoc.GREECE ], null, true);
  GEOHandler.registerValidator(addCrossLandedCntryFormValidatorGR, [ SysLoc.GREECE ], null, true);
  GEOHandler.addAfterConfig(clearPhoneNoFromGrid, [ SysLoc.GREECE ]);
  GEOHandler.addAfterConfig(clearPOBoxFromGrid, [ SysLoc.GREECE ]);
  GEOHandler.addAfterTemplateLoad(clearPOBoxFromGrid, [ SysLoc.GREECE ]);
  GEOHandler.addAfterTemplateLoad(retainLandCntryValuesOnCopy, [ SysLoc.GREECE ]);
  GEOHandler.addAfterTemplateLoad(checkScenarioChanged, [ SysLoc.GREECE ]);
  GEOHandler.addAfterTemplateLoad(retainImportValues, [ SysLoc.GREECE ]);
  GEOHandler.addAfterTemplateLoad(addPpsCeidValidator, [ SysLoc.GREECE ]);

  GEOHandler.addAddrFunction(setPostalCodeTurGreCypValidator, [ SysLoc.GREECE, SysLoc.CYPRUS, SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(abbrvLocMandatory, [ SysLoc.GREECE, SysLoc.CYPRUS, SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(abbrvLocMandatoryOnChange, [ SysLoc.GREECE, SysLoc.CYPRUS, SysLoc.TURKEY ]);
  GEOHandler.registerValidator(addGenericVATValidator(SysLoc.GREECE, 'MAIN_CUST_TAB', 'frmCMR'), [ SysLoc.GREECE ], null, true);
  GEOHandler.addAfterConfig(hideCustPhoneonSummary, [ SysLoc.GREECE, SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(addHandlerForCustSubTypeBpGRTRCY, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterTemplateLoad(setCustSubTypeBpGRTRCY, [ SysLoc.TURKEY ]);
  // GEOHandler.addAfterConfig(setISRValuesGROnUpdate, [ SysLoc.GREECE ]);

  // common greece/cyprus/turkey
  GEOHandler.addAfterConfig(viewOnlyAddressDetails, [ SysLoc.GREECE, SysLoc.CYPRUS, SysLoc.TURKEY ]);
  GEOHandler.addAfterTemplateLoad(setVatValidatorGRCYTR, [ SysLoc.GREECE, SysLoc.TURKEY, ]);

  /* 1438717 - add DPL match validation for failed dpl checks */
  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.EMEA, GEOHandler.ROLE_PROCESSOR, true);

  // For EmbargoCode
  GEOHandler.addAfterConfig(lockEmbargo, GEOHandler.EMEA);

  // For Legacy Direct
  GEOHandler.addAfterConfig(setFieldsBehaviourGR, [ SysLoc.GREECE ]);
  GEOHandler.addAfterConfig(resetSubIndustryCdGR, [ SysLoc.GREECE ]);
  GEOHandler.addAfterConfig(enableCMRNUMForPROCESSOR, [ SysLoc.GREECE ]);
  GEOHandler.addAfterTemplateLoad(setFieldsBehaviourGR, [ SysLoc.GREECE ]);
  GEOHandler.addAfterConfig(addHandlersForSubindustryCd, [ SysLoc.GREECE ]);
  // GEOHandler.addAfterTemplateLoad(setClientTierForCreates, [ SysLoc.GREECE
  // ]);
  GEOHandler.registerValidator(addInacCodeValidator, [ SysLoc.GREECE ], null, true);
  GEOHandler.registerValidator(addPaymentModeValidator, [ SysLoc.GREECE ], null, true);
  GEOHandler.registerValidator(addEnterpriseValidator, [ SysLoc.GREECE ], null, true);
  // GEOHandler.addAfterConfig(setClientTierForCreates, [ SysLoc.GREECE ]);
  GEOHandler.addAddrFunction(preFillTranslationAddrWithSoldToForTR, [ SysLoc.GREECE ]);

  // CREATCMR-4293
  // GEOHandler.addAfterTemplateLoad(setCTCValues, [ SysLoc.GREECE ]);
  GEOHandler.registerValidator(clientTierValidator, [ SysLoc.GREECE ], null, true);
  // GEOHandler.registerValidator(validatorISUCTCEntGR, [ SysLoc.GREECE ], null,
  // true);
  GEOHandler.addAfterConfig(addISUHandler, [ SysLoc.GREECE ]);
  GEOHandler.registerValidator(checkCmrUpdateBeforeImport, [ SysLoc.GREECE ], null, true);
  GEOHandler.addAfterTemplateLoad(addVATDisabler, [ SysLoc.GREECE ]);
  GEOHandler.addAfterConfig(addVATDisabler, [ SysLoc.GREECE ]);

});
