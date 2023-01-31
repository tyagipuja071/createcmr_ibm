/* Register Cyprus Javascripts */

var addrTypeHandler = [];
var _custSubTypeHandler = null;
var crossborderScenariosInterval = null;
var _importedIndc = null;
var _isuCdHandler = null;

function getImportedIndcForCyprus() {
  console.log(">>>> getImportedIndcForCyprus");
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

function addISUHandler() {
  console.log(">>>> addISUHandler");
  var _CTCHandler = null;
  _isuCdHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
    setValuesWRTIsuCtc();
  });
  _CTCHandler = dojo.connect(FormManager.getField('clientTier'), 'onChange', function(value) {
    setValuesWRTIsuCtc(value);
  });
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if(role == "VIEWER") {
    FormManager.readOnly('clientTier');
  }
}

function addEMEALandedCountryHandler(cntry, addressMode, saving, finalSave) {
  console.log(">>>> addEMEALandedCountryHandler");
  if (!saving) {
    if (addressMode == 'newAddress') {
      FilteringDropdown['val_landCntry'] = FormManager.getActualValue('defaultLandedCountry');
      FormManager.setValue('landCntry', FormManager.getActualValue('defaultLandedCountry'));
    } else {
      FilteringDropdown['val_landCntry'] = null;
    }
  }
  
  var landCntry = FormManager.getActualValue('landCntry');
  if(landCntry == 'CY') {
    if (_allAddressData == null || _allAddressData.length == 0) {
          GEOHandler.disableCopyAddress();
    }
  }
}

/*
 * EmbargoCode field locked for REQUESTER
 */
function lockEmbargo() {
  console.log(">>>> lockEmbargo");
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var issu_cntry = FormManager.getActualValue('cmrIssuingCntry');
  if (role == 'REQUESTER') {
    if (FormManager.getActualValue('reqType') == 'U' || FormManager.getActualValue('reqType') == 'X') {
      FormManager.enable('embargoCd');
    } else {
      FormManager.readOnly('embargoCd');
    }
  } else {
    FormManager.enable('embargoCd');
  }
}

function addOccupationPOBoxAttnPersonValidatorForCY() {
  console.log(">>>> addOccupationPOBoxAttnPersonValidatorForCY");
    
    FormManager.addFormValidator((function() {
      return {
        validate : function() {
        var filledCount = 0; 
          
          if (FormManager.getActualValue('cmrIssuingCntry') != SysLoc.CYPRUS) {
            return new ValidationResult(null, true);
          }
          
          if(FormManager.getActualValue('addrTxt') != '') {
            filledCount++;
          }
          
          if(FormManager.getActualValue('addrTxt2') != '') {
            filledCount++;
          }
          
          if(FormManager.getActualValue('poBox') != '') {
            filledCount++;
          }
          
          if(FormManager.getActualValue('custNm4') != '') {
            filledCount++;
          }
          
          if(filledCount > 2) {
             return new ValidationResult(null, false, 'Street Address,  Address Con\'t/Occupation, PO BOX, and Att. Person only 2 can be filled at the same time');
          }
          
          return new ValidationResult(null, true);
        }
      };
    })(), null, 'frmCMR_addressModal');
  }

function clearPOBoxFromGrid() {
  console.log(">>>> clearPOBoxFromGrid");
  for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
    recordList = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
    if (_allAddressData != null && _allAddressData[i] != null) {
    if(!(_allAddressData[i].addrType[0] == 'ZS01' || _allAddressData[i].addrType[0] == 'ZP01')) {
      _allAddressData[i].poBox[0] = ''; 
    }
  }
  }
}

/*
 * Disable VAT ID when user role is requester and request type is update
 */

function addVATDisabler() {
  console.log(">>>> addVATDisabler");
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
    var vat = FormManager.getActualValue('vat');

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
  console.log(">>>> addLatinCharValidator");
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
    if (cntry == SysLoc.ISRAEL) {
      checkAndAddValidator('custNm2', Validators.LATIN, [ 'Customer Name Con\'t' ]);
      checkAndAddValidator('addrTxt2', Validators.LATIN, [ 'Address Con\'t' ]);
      checkAndAddValidator('dept', Validators.LATIN, [ 'Attention Person' ]);
    } else if (cntry == SysLoc.GREECE) {
      checkAndAddValidator('custNm2', Validators.LATIN, [ 'Customer Name Con\'t' ]);
      checkAndAddValidator('addrTxt2', Validators.LATIN, [ ' Address Con\'t/Occupation' ]);
      checkAndAddValidator('dept', Validators.LATIN, [ 'District' ]);
    }
    checkAndAddValidator('custNm1', Validators.LATIN, [ 'Customer Name' ]);
    checkAndAddValidator('addrTxt', Validators.LATIN, [ 'Street Address' ]);
    checkAndAddValidator('city1', Validators.LATIN, [ 'City' ]);
    checkAndAddValidator('postCd', Validators.LATIN, [ 'Postal Code' ]);
    checkAndAddValidator('poBox', Validators.LATIN, [ 'PO Box' ]);
    // checkAndAddValidator('custPhone', Validators.LATIN, [ 'Phone #' ]);
  } else {
    FormManager.removeValidator('custNm1', Validators.LATIN);
    FormManager.removeValidator('custNm2', Validators.LATIN);
    FormManager.removeValidator('addrTxt', Validators.LATIN);
    FormManager.removeValidator('addrTxt2', Validators.LATIN);
    FormManager.removeValidator('city1', Validators.LATIN);
    FormManager.removeValidator('postCd', Validators.LATIN);
    FormManager.removeValidator('dept', Validators.LATIN);
    FormManager.removeValidator('poBox', Validators.LATIN);
    // FormManager.removeValidator('custPhone', Validators.LATIN);
  }
}

/**
 * Add Non-Latin character validation for address fields
 */
function addNonLatinCharValidator() {
  console.log(">>>> addNonLatinCharValidator");
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

  if (validateLatin) {
    checkAndAddValidator('addrTxt', Validators.NON_LATIN, [ 'Street Address' ]);
    checkAndAddValidator('city1', Validators.NON_LATIN, [ 'City' ]);
    checkAndAddValidator('postCd', Validators.NON_LATIN, [ 'Postal Code' ]);
    checkAndAddValidator('poBox', Validators.NON_LATIN, [ 'PO Box' ]);
  } else {
    FormManager.removeValidator('custNm1', Validators.NON_LATIN);
    FormManager.removeValidator('custNm2', Validators.NON_LATIN);
    FormManager.removeValidator('addrTxt', Validators.NON_LATIN);
    FormManager.removeValidator('addrTxt2', Validators.NON_LATIN);
    FormManager.removeValidator('city1', Validators.NON_LATIN);
    FormManager.removeValidator('postCd', Validators.NON_LATIN);
    FormManager.removeValidator('dept', Validators.NON_LATIN);
    FormManager.removeValidator('poBox', Validators.NON_LATIN);
    }
}

function removeValidationInacNac() {
  console.log(">>>> removeValidationInacNac");
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.ISRAEL) {
    FormManager.removeValidator('inacCd', Validators.REQUIRED);
    FormManager.removeValidator('inacCd', Validators.NUMBER);
  }
}

function defaultCapIndicatorUKI() {
  console.log(">>>> defaultCapIndicatorUKI");
  if ((FormManager.getActualValue('cmrIssuingCntry') == SysLoc.UK || FormManager.getActualValue('cmrIssuingCntry') == SysLoc.IRELAND) && FormManager.getActualValue('reqType') == 'C') {
    if (FormManager.getField('capInd').set) {
      FormManager.getField('capInd').set('checked', true);
    } else if (FormManager.getField('capInd')) {
      FormManager.getField('capInd').checked = true;
    }
    FormManager.readOnly('capInd');
  }
}

function defaultCapIndicator() {
  console.log(">>>> defaultCapIndicator");
  if ((FormManager.getActualValue('cmrIssuingCntry') == SysLoc.ISRAEL || FormManager.getActualValue('cmrIssuingCntry') == SysLoc.GREECE
      || FormManager.getActualValue('cmrIssuingCntry') == SysLoc.CYPRUS || FormManager.getActualValue('cmrIssuingCntry') == SysLoc.TURKEY)
      && FormManager.getActualValue('reqType') == 'C') {
    FormManager.getField('capInd').set('checked', true);
    FormManager.readOnly('capInd');
  }
}

function postalCodeNumericOnlyForDomestic() {
  console.log(">>>> postalCodeNumericOnlyForDomestic");
  dojo.connect(FormManager.getField('postCd'), 'onChange', function(value) {

    var custGroup = FormManager.getActualValue('custGrp');
    var postalCode = FormManager.getActualValue('postCd');
    var postCd = FormManager.getActualValue('postCd');

    if (custGroup == "LOCAL") {
      var postalCodeCheck = /^\d+$/.test(postalCode);
      if (!postalCodeCheck) {
        cmr.showAlert('Postal code should only allow numeric characters for domestic scenario.');
        FormManager.setValue(postCd, '');
      }
    }
  });
}

function validateEMEACopy(addrType, arrayOfTargetTypes) {
  console.log(">>>> validateEMEACopy");
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

function setAbbrvLocCrossBorderScenario() {
  console.log(">>>> setAbbrvLocCrossBorderScenario");
  var interval = new Object();

  interval = setInterval(function() {
    var custGroup = FormManager.getActualValue('custGrp');

    if (custGroup != null && custGroup.length > 0) {
      if (custGroup == "CROSS") {
        var reqId = FormManager.getActualValue('reqId');
        if (reqId != null) {
          reqParam = {
            REQ_ID : reqId,
          };
        }
        var results = cmr.query('ADDR.GET.CTYALANDCNTRY.BY_REQID', reqParam);
        var abbrevLocn = results.ret1;
        if (abbrevLocn != null) {
          if (abbrevLocn && abbrevLocn.length > 12) {
            abbrevLocn = abbrevLocn.substring(0, 12);
          }
          FormManager.setValue('abbrevLocn', abbrevLocn);
        } else {
          FormManager.setValue('abbrevLocn', '');
        }
        clearInterval(interval);
      } else {
        clearInterval(interval);
      }

    }
  }, 1000);
}

function setAbbrvLocCrossBorderScenarioOnChange() {
  console.log(">>>> setAbbrvLocCrossBorderScenarioOnChange");
  dojo.connect(FormManager.getField('custGrp'), 'onChange', function(value) {

    var custGroup = FormManager.getActualValue('custGrp');

    if (custGroup == "CROSS") {
      var reqId = FormManager.getActualValue('reqId');
      if (reqId != null) {
        reqParam = {
          REQ_ID : reqId,
        };
      }
      var results = cmr.query('ADDR.GET.CTYALANDCNTRY.BY_REQID', reqParam);
      var abbrevLocn = results.ret1;
      if (abbrevLocn != null) {
        if (abbrevLocn && abbrevLocn.length > 12) {
          abbrevLocn = abbrevLocn.substring(0, 12);
        }
        FormManager.setValue('abbrevLocn', abbrevLocn);
      } else {
        FormManager.setValue('abbrevLocn', '');
      }
    }
  });
}

function isTranslationAddrFieldsMatchForGR(zs01Data, zp01Data) {
  console.log(">>>> isTranslationAddrFieldsMatchForGR");
  
  if(zs01Data.custNm1[0]  == zp01Data.custNm1[0] 
  && zs01Data.custNm2[0]  == zp01Data.custNm2[0]
  && zs01Data.custNm4[0]  == zp01Data.custNm4[0]
  && zs01Data.addrTxt[0]  == zp01Data.addrTxt[0] 
  && zs01Data.addrTxt2[0] == zp01Data.addrTxt2[0] 
  && zs01Data.poBox[0]    == zp01Data.poBox[0]
  && zs01Data.postCd[0]   == zp01Data.postCd[0]
  && zs01Data.city1[0]    == zp01Data.city1[0]) {
    return true;
  }
  
  return false;
}

function getMismatchFields(zs01Data, zp01Data, isCrossborder) {
  console.log(">>>> getMismatchFields");

  var mismatchFields = '';
  
  if(zs01Data == null || zp01Data == null) {
    return mismatchFields;
  }
    if(!hasMatchingFieldsFilled(zs01Data.addrTxt[0], zp01Data.addrTxt[0], isCrossborder)) {
    mismatchFields += 'Street Address';
      }
      if(!hasMatchingFieldsFilled(zs01Data.custNm2[0], zp01Data.custNm2[0], isCrossborder)) {
        mismatchFields += mismatchFields != '' ? ', ' : '';
        mismatchFields += 'Customer Name Con\'t';
      }
      if(!hasMatchingFieldsFilled(zs01Data.addrTxt2[0], zp01Data.addrTxt2[0], isCrossborder)) {
        mismatchFields += mismatchFields != '' ? ', ' : '';
        mismatchFields += 'Address Con\'t/Occupation';
      }
      if(!hasMatchingFieldsFilled(zs01Data.poBox[0], zp01Data.poBox[0], isCrossborder)) {
        mismatchFields += mismatchFields != '' ? ', ' : '';
        mismatchFields += 'PO Box';
      }
      if(!hasMatchingFieldsFilled(zs01Data.custNm4[0], zp01Data.custNm4[0], isCrossborder)) {
        mismatchFields += mismatchFields != '' ? ', ' : '';
        mismatchFields += 'Att. Person';
      }
      
      if(isCrossborder) {
        if(!hasMatchingFieldsFilled(zs01Data.custNm1[0], zp01Data.custNm1[0], isCrossborder)) {
          mismatchFields += mismatchFields != '' ? ', ' : '';
          mismatchFields += 'Customer Name';
         }
        if(!hasMatchingFieldsFilled(zs01Data.postCd[0], zp01Data.postCd[0], isCrossborder)) {
          mismatchFields += mismatchFields != '' ? ', ' : '';
          mismatchFields += 'Postal Code';
        }        
        if(!hasMatchingFieldsFilled(zs01Data.city1[0], zp01Data.city1[0], isCrossborder)) {
          mismatchFields += mismatchFields != '' ? ', ' : '';
          mismatchFields += 'City';
        }
      }
      
     return mismatchFields;
}

function hasMatchingFieldsFilled(zs01Field, zp01Field, isCrossborder) {
  console.log(">>>> hasMatchingFieldsFilled");
  if(!isCrossborder) {
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
   if(zs01Field != zp01Field) {
     return false;
   } 
 }
  return true;
}

function isLandedCntryMatch(zs01Data, zp01Data) {
  console.log(">>>> isLandedCntryMatch");
  if(zs01Data.landCntry[0]  == zp01Data.landCntry[0] ) {
    return true;
  }
  
  return false;
}

function populateTranslationAddrWithSoldToData() {
  console.log(">>>> populateTranslationAddrWithSoldToData");
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
  if(zs01Data != null ) {
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
  console.log(">>>> populateTranslationAddrWithSoldToDataTR");
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
  if(zs01Data != null ) {
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

function clearAddrFieldsForGR() { 
  console.log(">>>> clearAddrFieldsForGR");
  FormManager.clearValue('custNm1');
  FormManager.clearValue('custNm2');
  FormManager.clearValue('addrTxt');
  FormManager.clearValue('addrTxt2');
  FormManager.clearValue('poBox');
  FormManager.clearValue('postCd');
  FormManager.clearValue('city1');
}

function clearAddrFieldsForTR() { 
  console.log(">>>> clearAddrFieldsForTR");
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
  console.log(">>>> preFillTranslationAddrWithSoldToForGR");
  if(FormManager.getActualValue('cmrIssuingCntry') == SysLoc.GREECE) {
    var custType = FormManager.getActualValue('custGrp');

    // for local don't proceed
  if (custType == 'LOCAL' || cmr.addressMode == 'copyAddress') {
    return;
  }
  if(!saving) {
    if (FormManager.getActualValue('addrType') == 'ZP01') {
      populateTranslationAddrWithSoldToData();  
    } else if (FormManager.getActualValue('addrType') != 'ZP01' && addressMode != 'updateAddress' && _addrSelectionHistGR == 'ZP01'){
      // clear address fields when switching
      clearAddrFieldsForGR();
    }  
  }
  _addrSelectionHistGR = FormManager.getActualValue('addrType');
  }
}

var _addrSelectionHistTR = '';
function preFillTranslationAddrWithSoldToForTR(cntry, addressMode, saving) {
  console.log(">>>> preFillTranslationAddrWithSoldToForTR");
  if(FormManager.getActualValue('cmrIssuingCntry') == SysLoc.TURKEY) {
    var custType = FormManager.getActualValue('custGrp');
  // for local don't proceed
  if (custType == 'LOCAL') {
    return;
  }
  if(!saving) {
    if (FormManager.getActualValue('addrType') == 'ZP01') {
      populateTranslationAddrWithSoldToDataTR();  
    } else if (FormManager.getActualValue('addrType') != 'ZP01' && addressMode != 'updateAddress' && _addrSelectionHistTR == 'ZP01'){
      // clear address fields when switching
      clearAddrFieldsForTR();
    }  
  }
  _addrSelectionHistTR = FormManager.getActualValue('addrType');
  }
}

function addTRAddressTypeValidator() {
  console.log(">>>> addTRAddressTypeValidator");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('cmrIssuingCntry') != SysLoc.TURKEY) {
          return new ValidationResult(null, true);
        }
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount == 0) {
          return new ValidationResult(null, false, 'Installing/Shipping/EPL and Mailing/Billing addresses are both required.');
        }
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          var record = null;
          var type = null;
          var zs01Cnt = 0;
          var zp01Cnt = 0;
          var zd01Cnt = 0;

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
              zs01Cnt++;
            } else if (type == 'ZP01') {
              zp01Cnt++;
            } else if (type == 'ZD01') {
              zd01Cnt++;
            }
          }

          // if (zs01Cnt == 0 || zd01Cnt == 0) {
          if (zs01Cnt == 0 || zp01Cnt == 0) {
            // return new ValidationResult(null, false, 'Installing and shipping
            // addresses are both required.');
            return new ValidationResult(null, false, 'Installing/Shipping/EPL and Mailing/Billing addresses are both required.');
          } else if (zs01Cnt > 1) {
            return new ValidationResult(null, false, 'Only one Installing/Shipping/EPL is allowed.');
          } else if (zp01Cnt > 1) {
            return new ValidationResult(null, false, 'Only one Mailing/Billing is allowed.');
          }
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function addCYAddressTypeValidator() {
  console.log(">>>> addCYAddressTypeValidator");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('cmrIssuingCntry') != SysLoc.CYPRUS) {
          return new ValidationResult(null, true);
        }
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount == 0) {
          return new ValidationResult(null, false, 'Mailing/Billing/Installing/Shipping/EPL address is required.');
        }
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          var record = null;
          var type = null;
          var zs01Cnt = 0;
          var zd01Cnt = 0;
          var zp01Cnt = 0;
          var zi01Cnt = 0;
          var zs02Cnt = 0;
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
            } else if (type == 'ZD01') {
              zd01Cnt++;
            } else if (type == 'ZP01') {
              zp01Data = record;  
              zp01Cnt++;
            } else if (type == 'ZI01') {
              zi01Cnt++;
            } else if (type == 'ZS02') {
              zs02Cnt++;
            }
          }
          
          if(zs01Data!= null && zp01Data!= null && !isLandedCntryMatch(zs01Data, zp01Data)) {
            return new ValidationResult(null, false, 'Mailing and Billing should have the same landed country.');
          }
          
          if (zs01Cnt == 0 || zd01Cnt == 0 || zp01Cnt == 0 || zi01Cnt == 0 || zs02Cnt == 0) {
            return new ValidationResult(null, false, 'Mailing/Billing/Installing/Shipping/EPL address is required.');
          } else if (zs01Cnt > 1) {
            return new ValidationResult(null, false, 'Only one Mailing address is allowed.');
          } else if (zp01Cnt > 1) {
            return new ValidationResult(null, false, 'Only one Billing address is allowed.');
          } else if (zs02Cnt > 1) {
            return new ValidationResult(null, false, 'Only one EPL address is allowed.');
          } else {
            return new ValidationResult(null, true);
          }
        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

var _gtcISUHandler = null;
var _CTCHandler = null;
var _gtcISRHandler = null;
var _gtcAddrTypes = [ 'ZS01', 'ZP01', 'ZD01', 'ZI01', 'ZS02' ];
var _gtcAddrTypeHandler = [];
var _gtcVatExemptHandler = null;

function addHandlersForCY() {
  console.log(">>>> addHandlersForCY");
var custType = FormManager.getActualValue('custGrp'); 
  if (_gtcISUHandler == null) {
      _gtcISUHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
        setClientTierAndISR(value);
        setISRValues(value);
        setValuesWRTIsuCtc();
      });
  }
  if (_CTCHandler == null) {
    _CTCHandler = dojo.connect(FormManager.getField('clientTier'), 'onChange', function(value) {
      setEnterprise(value);
      setValuesWRTIsuCtc(value);
    });
  }

  for (var i = 0; i < _gtcAddrTypes.length; i++) {
    _gtcAddrTypeHandler[i] = null;
    if (_gtcAddrTypeHandler[i] == null) {
      _gtcAddrTypeHandler[i] = dojo.connect(FormManager.getField('addrType_' + _gtcAddrTypes[i]), 'onClick', function(value){
        disableAddrFieldsGRCYTR();
        preFillTranslationAddrWithSoldToForTR();
        if(FormManager.getActualValue('cmrIssuingCntry') == SysLoc.CYPRUS){
          disableAddrFieldsCY();
        }
        
      });
    }
  }
  
  if (_gtcVatExemptHandler == null) {
    _gtcVatExemptHandler = dojo.connect(FormManager.getField('vatExempt'), 'onClick', function(value) {
      setVatValidator();
    });
  }
}

function disableAddrFieldsCY(){
  console.log(">>>> disableAddrFieldsCY");
  if ((FormManager.getActualValue('addrType') == 'ZS01' || FormManager
      .getActualValue('addrType') == 'ZD01')) {
    FormManager.enable('custPhone');
  } else {
    FormManager.setValue('custPhone', '');
    FormManager.disable('custPhone');
  }
  
  if (FormManager.getActualValue('addrType') == 'ZP01'
    || FormManager.getActualValue('addrType') == 'ZS01') {
  FormManager.enable('poBox');
  } else {
    FormManager.setValue('poBox', '');
    FormManager.disable('poBox');
  }
  
  var landCntry = FormManager.getActualValue('landCntry');
  if(!(FormManager.getActualValue('custGrp') == 'CROSS' || isUpdateReqCrossborder()) && landCntry == 'CY' && 
      (FormManager.getActualValue('addrType') == 'ZP01'
    || FormManager.getActualValue('addrType') == 'ZS01') ) {
    FormManager.readOnly('landCntry');
  } else {
    FormManager.enable('landCntry');
  }
}

function setVatValidator() {
  console.log(">>>> setVatValidator");
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');

  if (viewOnlyPage != 'true' && FormManager.getActualValue('reqType') == 'C') {
    FormManager.resetValidations('vat');
    if (FormManager.getActualValue('custSubGrp') == 'IBMEM') {
      FormManager.readOnly('vat');
    }
    if (dijit.byId('vatExempt').get('checked')) {
      FormManager.clearValue('vat');
    }
    if (undefined != dijit.byId('vatExempt') && !dijit.byId('vatExempt').get('checked')) {
      checkAndAddValidator('vat', Validators.REQUIRED, [ 'VAT' ]);
      FormManager.enable('vat');
    }
  }
}

function addPOBoxValidatorGR() {
  console.log(">>>> addPOBoxValidatorGR");
  FormManager.removeValidator('poBox', Validators.LATIN);
  FormManager.removeValidator('poBox', Validators.NON_LATIN);
  FormManager.addValidator('poBox', Validators.DIGIT, [ 'PO Box' ]);
}

function setClientTierAndISR(isu) {
  console.log(">>>> setClientTierAndISR");
  var reqType = FormManager.getActualValue('reqType');
  var clientTier = FormManager.getActualValue('clientTier');
  var custSubGroupSet = new Set(['CRINT','CRBUS','BUSPR','INTER','IBMEM']);
  
  if (!isu) {
    isu = FormManager.getActualValue('isuCd');
  }
  
  if (reqType != 'C') {
    return;
  }


  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.CYPRUS) {
    if (isu == '34') {
      FormManager.setValue('clientTier', 'Q');
      FormManager.setValue('enterprise', '822830');
      FormManager.enable('enterprise');
      FormManager.readOnly('salesTeamCd')
      FormManager.readOnly('repTeamMemberNo');
      FormManager.readOnly('salesBusOffCd');
    } else if (isu == '36') {
      FormManager.setValue('clientTier', 'Y');
      FormManager.setValue('enterprise', '822840');
      FormManager.enable('enterprise');
      FormManager.readOnly('salesTeamCd')
      FormManager.readOnly('repTeamMemberNo');
      FormManager.readOnly('salesBusOffCd');
    } else if (isu == '32' ) {
      FormManager.setValue('clientTier', 'T');
      FormManager.setValue('enterprise', '985985');
      FormManager.enable('enterprise');
      FormManager.readOnly('salesTeamCd')
      FormManager.readOnly('repTeamMemberNo');
      FormManager.readOnly('salesBusOffCd');
    } else if (isu == '5K') {
      FormManager.setValue('clientTier', '');
      FormManager.setValue('enterprise', '985999');
      FormManager.enable('enterprise');
      FormManager.readOnly('salesTeamCd')
      FormManager.readOnly('repTeamMemberNo');
      FormManager.readOnly('salesBusOffCd');
    } else {
      FormManager.setValue('clientTier', '');
      FormManager.setValue('enterprise', '');
      FormManager.readOnly('enterprise');
      FormManager.readOnly('salesTeamCd')
      FormManager.readOnly('repTeamMemberNo');
      FormManager.readOnly('salesBusOffCd');
    }
  }
  
  FormManager.setValue('salesTeamCd', '000000');
  FormManager.setValue('repTeamMemberNo', '000000');
  FormManager.setValue('salesBusOffCd', '000');
}

function retainImportValues(fromAddress, scenario, scenarioChanged) {
  console.log(">>>> retainImportValues");
  var isCmrImported = getImportedIndcForCyprus();
  var reqId = FormManager.getActualValue('reqId');
  
  if(FormManager.getActualValue('reqType') == 'C' && isCmrImported == 'Y' 
   && scenarioChanged && (scenario == 'COMME' || scenario == 'GOVRN' || scenario == 'CROSS')) {
      
   var origISU;
   var origClientTier;
   var origRepTeam;
   var origSbo;
   var origInac;
   var origEnterprise;
   
   var result = cmr.query("GET.CMRINFO.IMPORTED_GR", {
     REQ_ID : reqId
   });
      
   if(result != null && result != '') {
     origISU = result.ret1;
     origClientTier = result.ret2;
     origInac = result.ret5;
     origEnterprise = result.ret6;
     
     FormManager.setValue('isuCd',origISU);
     FormManager.setValue('clientTier', origClientTier);
     FormManager.setValue('inacCd',origInac);
     FormManager.setValue('enterprise', origEnterprise);
   }
 } else if (FormManager.getActualValue('reqType') == 'C' && isCmrImported == 'Y' && scenarioChanged) {
   FormManager.setValue('inacCd', '');
 }
}

function setFieldsBehaviourCY() {
  console.log(">>>> setFieldsBehaviourCY");
  var role = FormManager.getActualValue('userRole').toUpperCase();
  
  if(FormManager.getActualValue('reqType') == 'U'){
    if(role == 'PROCESSOR') {
      FormManager.addValidator('repTeamMemberNo', Validators.REQUIRED, [ 'ISR' ], 'MAIN_IBM_TAB');
      FormManager.addValidator('salesTeamCd', Validators.REQUIRED, [ 'Sales Rep' ], 'MAIN_IBM_TAB');
    } else if(role == 'REQUESTER') {
      FormManager.resetValidations('repTeamMemberNo');
      FormManager.resetValidations('salesTeamCd');
    }
  }
}

function setISRValues(isuCd) {
  console.log(">>>> setISRValues");
  var isuCd = FormManager.getActualValue('isuCd');
  var clientTier = FormManager.getActualValue('clientTier');
  if (!isuCd) {
    isuCd = FormManager.getActualValue('isuCd');
  }
  if (isuCd == '5K') {
    FormManager.removeValidator('clientTier', Validators.REQUIRED);
    return;
  } else if (custSubGrp == 'BUSPR' || custSubGrp == 'INTER' || custSubGrp == 'CRINT' || custSubGrp == 'CRBUS') {
    FormManager.readOnly('clientTier');
  }
  if (reqType != 'C') {
    return;
  }
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
        setEnterprise(false);
        FormManager.setValue('repTeamMemberNo', isrs[0]);
      }
      setSalesBoSboIbo();
    }
  }
}

function setEnterprise(ctc) {
  console.log(">>>> setEnterprise");
  if(FormManager.getActualValue('reqType') == 'U'){
    return;
  }
  var isu = FormManager.getActualValue('isuCd');
  if(ctc == null){
    var ctc = FormManager.getActualValue('clientTier');
  }
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  
  var isuCtc = isu + ctc;
  var enterprises = [];
  if (isu != '' && ctc != '') {
    var qParams = {
      _qall : 'Y',
      ISSUING_CNTRY : cntry,
      ISU : '%' + isuCtc + '%'
    };
    var results = cmr.query('GET.ENTLIST.BYISU', qParams);
    if (results != null) {
      FormManager.resetDropdownValues(FormManager.getField('enterprise'));
      for (var i = 0; i < results.length; i++) {
        enterprises.push(results[i].ret1);
      }
      if (enterprises != null) {
        if (enterprises.length == 1) {
          FormManager.setValue('enterprise', enterprises[0]);
        }
      }
    }
  }
}

function hideCollectionCd() {
  console.log(">>>> hideCollectionCd");
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.TURKEY && FormManager.getActualValue('reqType') == 'U') {
    FormManager.show('CollectionCd', 'collectionCd');
  } else {
    FormManager.resetValidations('collectionCd');
    FormManager.hide('CollectionCd', 'collectionCd');
  }
}

function setSalesBoSboIbo() {
  console.log(">>>> setSalesBoSboIbo");
  var salesRep = FormManager.getActualValue('salesTeamCd');
  
  if (salesRep != '') {
    var qParams = {
      ISSUING_CNTRY : FormManager.getActualValue('cmrIssuingCntry'),
      REP_TEAM_CD : salesRep
    };
    var result = cmr.query('DATA.GET.SALES_BO_CD', qParams);
    var salesBoCd = result.ret1;
    var selsr = result.ret2;
    FormManager.setValue('salesBusOffCd', salesBoCd);
    FormManager.setValue('repTeamMemberNo', selsr);
  } else {
    FormManager.setValue('salesBusOffCd', '000');
  }
}

function addShippingAddrTypeValidator() {
  console.log(">>>> addShippingAddrTypeValidator");
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
  console.log(">>>> convertToUpperCaseGR");
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.CYPRUS) {
    return;
  }
    var custType = FormManager.getActualValue('custGrp');

    // for cross border
    if (custType == 'CROSS') {
      return;
    }

    // Greek address - block lowercase
    var addrFields = [ 'custNm1', 'custNm2', 'addrTxt', 'addrTxt2', 'city1', 'postCd', 'custPhone', 'sapNo', 'custNm4' ];
    if (FormManager.getActualValue('addrType') == 'ZP01') {
      for (var i = 0; i < addrFields.length; i++) {
        dojo.byId(addrFields[i]).style.textTransform = 'uppercase';
        if (saving) {
          dojo.byId(addrFields[i]).value = dojo.byId(addrFields[i]).value.toUpperCase();
        }
      }
    } else {
      for (var i = 0; i < addrFields.length; i++) {
        dojo.byId(addrFields[i]).style.textTransform = 'none';
      }
    }
}

function updateAddrTypeList(cntry, addressMode, saving) {
  console.log(">>>> updateAddrTypeList");
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

function setFieldsToReadOnlyGRCYTR() {
  console.log(">>>> setFieldsToReadOnlyGRCYTR");
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var reqType = FormManager.getActualValue('reqType');
  if (role == 'REQUESTER') {
    FormManager.readOnly('abbrevNm');
    FormManager.readOnly('abbrevLocn');
  }
 
  FormManager.readOnly('subIndustryCd');
  if(reqType != 'U'){
    FormManager.readOnly('repTeamMemberNo');
    FormManager.readOnly('salesTeamCd');
  }
  // CREATCMR-788
  addressQuotationValidatorCY();
}

function updateAbbrevNmLocnGRCYTR(cntry, addressMode, saving, finalSave, force) {
  console.log(">>>> updateAbbrevNmLocnGRCYTR");
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

function addrFunctionForGRCYTR(cntry, addressMode, saving) {
  console.log(">>>> addrFunctionForGRCYTR");
  if (!saving) {
    var cntryCd = FormManager.getActualValue('cmrIssuingCntry');
    var custType = FormManager.getActualValue('custGrp');

    if ((addressMode == 'updateAddress' || addressMode == 'copyAddress') && FormManager.getActualValue('landCntry') == '') {
      FormManager.setValue('landCntry', cmr.oldlandcntry);
    }
    
    // for Turkey - cross border
    if (cntryCd == SysLoc.TURKEY && custType == 'CROSS') {
      FormManager.removeValidator('dept', Validators.REQUIRED);
    } else if (cntryCd == SysLoc.TURKEY) {
      checkAndAddValidator('dept', Validators.REQUIRED, [ 'District' ]);
    }
    checkAndAddValidator('landCntry', Validators.REQUIRED, [ 'Country (Landed)' ]);
  }
}

function retainLandCntryValuesOnCopy() {  
  console.log(">>>> retainLandCntryValuesOnCopy");
  if ((cmr.addressMode == 'copyAddress') && FormManager.getActualValue('landCntry') == '') {
    FormManager.setValue('landCntry', cmr.oldlandcntry);
  }
}

function disableAddrFieldsGRCYTR() {
  console.log(">>>> disableAddrFieldsGRCYTR");
  var cntryCd = FormManager.getActualValue('cmrIssuingCntry');

  // Phone - for mailing/billing address only
  if ((cntryCd == SysLoc.GREECE || cntryCd == SysLoc.TURKEY) && FormManager.getActualValue('addrType') != 'ZP01') {
    FormManager.setValue('custPhone', '');
    FormManager.disable('custPhone');
  } else {
    FormManager.enable('custPhone');
  }

  // CMR-1616:PostBox only for cross-border scenrio
  var custGrp = FormManager.getActualValue('custGrp');
  if (cntryCd == SysLoc.TURKEY && custGrp != 'CROSS') {
    FormManager.setValue('poBox', '');
    FormManager.disable('poBox');
  } else {
    FormManager.enable('poBox');
  }
}

function disableAddrFieldsGR() {
  console.log(">>>> disableAddrFieldsGR");
  // GR - Phone - for Sold-to and Ship-to
  if ((FormManager.getActualValue('addrType') == 'ZS01' || FormManager
      .getActualValue('addrType') == 'ZD01')) {
    FormManager.enable('custPhone');
  } else {
    FormManager.setValue('custPhone', '');
    FormManager.disable('custPhone');
  }

  if (FormManager.getActualValue('addrType') == 'ZP01'
      || FormManager.getActualValue('addrType') == 'ZS01') {
    FormManager.enable('poBox');
  } else {
    FormManager.setValue('poBox', '');
    FormManager.disable('poBox');

  }
  
  var landCntry = FormManager.getActualValue('landCntry');
  if(!(FormManager.getActualValue('custGrp') == 'CROSS' || isUpdateReqCrossborder()) && landCntry == 'GR' && 
      (FormManager.getActualValue('addrType') == 'ZP01'
    || FormManager.getActualValue('addrType') == 'ZS01') ) {
    FormManager.readOnly('landCntry');
  } else {
    FormManager.enable('landCntry');
  }
}

function isUpdateReqCrossborder() {
  console.log(">>>> isUpdateReqCrossborder");
  if(!(FormManager.getActualValue('custGrp') == 'LOCAL')) {
    for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
      recordList = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
      if (_allAddressData != null && _allAddressData[i] != null) {
        if(_allAddressData[i].addrType[0] == 'ZS01') {
          return _allAddressData[i].landCntry[0] != 'CY'; 
        }
      }
    }  
  } 
  return false;
}

function hideMOPAFieldForGR() {
  console.log(">>>> hideMOPAFieldForGR");
  if (FormManager.getActualValue('reqType') == 'C') {
    FormManager.hide('ModeOfPayment', 'modeOfPayment');
  } else if(FormManager.getActualValue('reqType') == 'U') {
    FormManager.show('ModeOfPayment', 'modeOfPayment');
  }
}

function setTypeOfCustomerBehaviorForCY() {
  console.log(">>>> setTypeOfCustomerBehaviorForCY");
  // Customer Type behaviour for CY
  if (FormManager.getActualValue('reqType') == 'C') {
    FormManager.hide('CrosSubTyp', 'crosSubTyp');
  } else if(FormManager.getActualValue('reqType') == 'U') {
    FormManager.show('CrosSubTyp', 'crosSubTyp');
    var role = FormManager.getActualValue('userRole').toUpperCase();
    if(role == 'REQUESTER') {
      FormManager.readOnly('crosSubTyp');
    } else if (role == 'PROCESSOR') {
      FormManager.enable('crosSubTyp');
    }
  }
}

function addPostalCodeLenForTurGreCypValidator() {
  console.log(">>>> addPostalCodeLenForTurGreCypValidator");
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
  console.log(">>>> setPostalCodeTurGreCypValidator");
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
  console.log(">>>> abbrvLocMandatory");
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
  console.log(">>>> abbrvLocMandatoryOnChange");
  var role = FormManager.getActualValue('userRole').toUpperCase();
  FormManager.addValidator('abbrevNm', Validators.NO_QUOTATION, [ 'Abbreviated Name (TELX1)' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('abbrevLocn', Validators.NO_QUOTATION, [ 'Abbreviated Location' ], 'MAIN_CUST_TAB');
  dojo.connect(FormManager.getField('abbrevLocn'), 'onChange', function(value) {
    if (role != 'REQUESTER') {
      FormManager.addValidator('abbrevLocn', Validators.REQUIRED, [ 'AbbrevLocation' ], 'MAIN_CUST_TAB');
    }
  });
}

function setCommonCollectionCd() {
  console.log(">>>> setCommonCollectionCd");
  if (FormManager.getActualValue('reqType') == 'C') {
    FormManager.readOnly('collectionCd2');
    FormManager.setValue('collectionCd2', '');
  }
}

function hideCustPhoneonSummary() {
  console.log(">>>> hideCustPhoneonSummary");
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
  console.log(">>>> addHandlerForCustSubTypeBpGRTRCY");
  if (_custSubTypeHandler == null) {
    _custSubTypeHandler = dojo.connect(FormManager.getField('custSubGrp'), 'onChange', function(value) {
      setCustSubTypeBpGRTRCY();
    });
  }
}

function setCustSubTypeBpGRTRCY(fromAddress, scenario, scenarioChanged) {
  console.log(">>>> setCustSubTypeBpGRTRCY");
  var custType = FormManager.getActualValue('custSubGrp');
  var _reqId = FormManager.getActualValue('reqId');
  var isuCd = FormManager.getActualValue('isuCd')
  
  if (isuCd == '5K') {
    FormManager.removeValidator('clientTier', Validators.REQUIRED);
    return;
  } else if (custSubGrp == 'BUSPR' || custSubGrp == 'INTER' || custSubGrp == 'CRINT' || custSubGrp == 'CRBUS') {
    FormManager.readOnly('clientTier');
  }
  
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.CYPRUS) {
    if( FormManager.getActualValue('custGrp') != 'CROSS'){
      var city1Params = {
          REQ_ID : _reqId,
          ADDR_TYPE : "ZS01",
        };
      var city1Result = cmr.query('ADDR.GET.CITY.BY_REQID_ADDRTYP', city1Params);
      var city1 = city1Result.ret1;
      if (city1 != '' && custType != 'SAASP') {
        if (city1 && city1.length > 12) {
          city1 = city1.substring(0, 12);
        }
        FormManager.setValue('abbrevLocn', city1);
      } else if (custType == 'SAASP') {
        FormManager.setValue('abbrevLocn', 'SAAS');
      }
    }
      
    if (FormManager.getActualValue('vatExempt') != null && FormManager.getActualValue('vatExempt') != 'Y'
      && (custType != 'PRICU' || custType != 'SAASP')) {
      checkAndAddValidator('vat', Validators.REQUIRED, [ 'VAT' ],'MAIN_CUST_TAB');
    }
    
    if (custType == 'BUSPR' || custType == 'CRBUS' || custType == 'INTER' || custType == 'CRINT') {
      FormManager.readOnly('clientTier');
      // CREATCMR-4293
      // FormManager.setValue('clientTier', '7');
      FormManager.readOnly('isuCd');
      FormManager.setValue('isuCd', '21');
      if(scenarioChanged){
        checkAndAddValidator('vat', Validators.REQUIRED, [ 'VAT' ],'MAIN_CUST_TAB');
        FormManager.setValue('vatExempt', false);
      }
    } else if (custType == 'IBMEM') {
      FormManager.setValue('clientTier', '');
      FormManager.readOnly('clientTier');
      FormManager.readOnly('isuCd');
      if (FormManager.getActualValue('userRole').toUpperCase() == 'PROCESSOR') {
        FormManager.enable('clientTier');
        FormManager.enable('isuCd');
      }
    } else if (custType == 'SAASP') {
      FormManager.readOnly('clientTier');
      FormManager.setValue('clientTier', 'Q');
      FormManager.readOnly('isuCd');
      FormManager.setValue('isuCd', '34');
      if(scenarioChanged) {
        FormManager.resetValidations('vat');
        FormManager.setValue('vatExempt', 'Y');
      }
    } else if (custType == 'PRICU') {
       FormManager.enable('clientTier');
       FormManager.enable('isuCd');
       if(scenarioChanged){
         FormManager.resetValidations('vat');
         FormManager.setValue('vatExempt', 'Y');
       }
    } else {
      FormManager.enable('clientTier');
      FormManager.enable('isuCd');
      if(scenarioChanged){
        checkAndAddValidator('vat', Validators.REQUIRED, [ 'VAT' ],'MAIN_CUST_TAB');
        FormManager.setValue('vatExempt', false);
      }
    }
    
    if (undefined != dijit.byId('vatExempt') && !dijit.byId('vatExempt').get('checked')) {
      checkAndAddValidator('vat', Validators.REQUIRED, [ 'VAT' ],'MAIN_CUST_TAB');
      FormManager.setValue('vatExempt', false);
    }
  } 
}

function disableINACEnterpriseOnViewOnly() {
  console.log(">>>> disableINACEnterpriseOnViewOnly");
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');

  if (viewOnlyPage == 'true') {
    FormManager.readOnly('inacCd');
    FormManager.readOnly('enterprise');
  }
}

function viewOnlyAddressDetails() {
  console.log(">>>> viewOnlyAddressDetails");
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');

  if (viewOnlyPage == 'true' && (cmrIssuingCntry == SysLoc.GREECE || cmrIssuingCntry == SysLoc.CYPRUS)) {
    $('label[for="addrTxt_view"]').text('Street Address:');
    $('label[for="addrTxt2_view"]').text('Occupation:');
  }
  
  if(viewOnlyPage == 'true' && cmrIssuingCntry == SysLoc.GREECE) {
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

function salesSRforUpdate() {
  console.log(">>>> salesSRforUpdate");
  if (FormManager.getActualValue('reqType') == 'U') {
    FormManager.setValue('salesBusOffCd', '000');
  }
}

function modifyCharForTurk(field) {
  console.log(">>>> modifyCharForTurk");

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
  console.log(">>>> addTaxCodeForProcessorValidator");
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

function getDescription(fieldId) {
  console.log(">>>> getDescription");
  var field = dijit.byId(fieldId);
  return field.displayedValue;
}

function addPhoneValidatorEMEA() {
  console.log(">>>> addPhoneValidatorEMEA");
  FormManager.addValidator('custPhone', Validators.DIGIT, [ 'Phone #' ]);
}
function addPOBOXValidatorEMEA() {
  console.log(">>>> addPOBOXValidatorEMEA");
  FormManager.addValidator('poBox', Validators.DIGIT, [ 'PO Box' ]);
}
function addCompanyAddrValidator() {
  console.log(">>>> addCompanyAddrValidator");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var zi01ReqId = FormManager.getActualValue('reqId');
        qParams = {
          REQ_ID : zi01ReqId,
        };
        var record = cmr.query('GETZI01VALRECORDS', qParams);
        var zi01Reccount = record.ret1;
        if (Number(zi01Reccount) > 1) {
          return new ValidationResult(null, false, 'Only one Company Address can be defined.');
        } else if (Number(zi01Reccount == 0)) {
          return new ValidationResult(null, false, 'At least one Company Address must be defined.');
        } else {
          return new ValidationResult(null, true);
        }

      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function addBillingAddrValidator() {
  console.log(">>>> addBillingAddrValidator");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var zp01ReqId = FormManager.getActualValue('reqId');
        qParams = {
          REQ_ID : zp01ReqId,
        };
        var record = cmr.query('GETZP01VALRECORDS', qParams);
        var zp01Reccount = record.ret1;
        if (Number(zp01Reccount) > 1) {
          return new ValidationResult(null, false, 'Only one Billing Address can be defined.');
        } else if (Number(zp01Reccount == 0)) {
          return new ValidationResult(null, false, 'At least one Billing Address must be defined.');
        } else {
          return new ValidationResult(null, true);
        }

      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function canRemoveAddress(value, rowIndex, grid) {
  console.log(">>>> canRemoveAddress");
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
  console.log(">>>> canUpdateAddress");
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
  console.log(">>>> canCopyAddress");
  var cntry = FormManager.getActualValue('cmrIssuingCntry');

  if(cntry == '726') {
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
  console.log(">>>> shouldShowCopyAddressInGrid");
  if(grid != null && rowIndex != null && grid.getItem(rowIndex) != null) {
    if(grid.getItem(rowIndex).addrType[0] == 'ZP01' && grid.getItem(rowIndex).landCntry[0] == 'GR') {
      return false;
    }
  }
  return true;
}

function ADDRESS_GRID_showCheck(value, rowIndex, grid) {
  console.log(">>>> ADDRESS_GRID_showCheck");
  return canRemoveAddress(value, rowIndex, grid);
}

function disableHideFieldsOnAddrIT() {
  console.log(">>>> disableHideFieldsOnAddrIT");
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

/**
 * Override to not format for greece and cyprus
 */
function streetValueFormatter(value, rowIndex) {
  console.log(">>>> streetValueFormatter");
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
  console.log(">>>> addSMAndVAPostalCDValidator");
  var landCntry = FormManager.getActualValue('landCntry');
  if (landCntry != 'undefined' && 'SM' == landCntry) {
    FormManager.addValidator('postCd', Validators.REQUIRED, [ 'Postal Code for San Marino - SM' ], 'MAIN_NAME_TAB');
  }
  if (landCntry != 'undefined' && 'VA' == landCntry) {
    FormManager.addValidator('postCd', Validators.REQUIRED, [ 'Postal Code for Holy See (Vatican City State) - VA' ], 'MAIN_NAME_TAB');
  }
}

function checkIfVATFiscalUpdatedIT() {
  console.log(">>>> checkIfVATFiscalUpdatedIT");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var requestType = FormManager.getActualValue('reqType');
        if (requestType != 'U') {
          return new ValidationResult(null, true);
        }
        var currentFiscalCode = FormManager.getActualValue('taxCd1');
        var currentVAT = FormManager.getActualValue('vat');
        var fiscalStatus = FormManager.getActualValue('fiscalDataStatus');
        qParams = {
          REQ_ID : FormManager.getActualValue('reqId'),
        };
        var result = cmr.query('GET.OLD_FISCAL_DATA', qParams);
        var oldVAT = result.ret1;
        var oldFiscalCode = result.ret2;
        var oldCompany = result.ret3;
        if (result != null && oldVAT != null && oldFiscalCode != null && (oldFiscalCode != currentFiscalCode || oldVAT != currentVAT)) {
          if (fiscalStatus == null || fiscalStatus == '') {
            return new ValidationResult(null, false, 'Fiscal Data has been modified, please validate fiscal data');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function doValidateFiscalDataModal() {
  console.log(">>>> doValidateFiscalDataModal");
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

function validateVATFiscalLengthOnIdentIT() {
  console.log(">>>> validateVATFiscalLengthOnIdentIT");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var requestType = FormManager.getActualValue('reqType');
        var role = FormManager.getActualValue('userRole').toUpperCase();
        if (requestType != 'C') {
          return new ValidationResult(null, true);
        }
        var ident = FormManager.getActualValue('identClient');
        var fiscal = FormManager.getActualValue('taxCd1');
        var lbl1 = FormManager.getLabel('LocalTax1');
        var custSubGrp = FormManager.getActualValue('custSubGrp');

        if (ident == 'A' && fiscal != undefined && fiscal != '' && (fiscal.length != 11 || !fiscal.match("^[0-9]*$")) && (role == 'PROCESSOR' || (role == 'REQUESTER' && custSubGrp == 'BUSPR'))) {
          return new ValidationResult({
            id : 'taxCd1',
            type : 'text',
            name : 'taxCd1'
          }, false, 'For Ident Client A ' + lbl1 + ' must be of 11 digits only');
        }
        if (ident == 'B' && fiscal != undefined && fiscal != '' && (fiscal.length != 11 || !fiscal.match("^[0-9]*$"))) {
          return new ValidationResult({
            id : 'taxCd1',
            type : 'text',
            name : 'taxCd1'
          }, false, 'For Ident Client B ' + lbl1 + ' must be of 11 digits only');
        }
        if (ident == 'C' && fiscal != undefined && fiscal != '' && (fiscal.length != 11 || !fiscal.match("^[0-9]*$"))) {
          return new ValidationResult({
            id : 'taxCd1',
            type : 'text',
            name : 'taxCd1'
          }, false, 'For Ident Client C ' + lbl1 + ' must be of 11 digits only');
        }
        if (ident == 'D' && fiscal != undefined && fiscal != '' && (fiscal.length != 16 || !fiscal.match("^[0-9a-zA-Z]*$")) && role == 'PROCESSOR') {
          return new ValidationResult({
            id : 'taxCd1',
            type : 'text',
            name : 'taxCd1'
          }, false, 'For Ident Client D ' + lbl1 + ' must be of 16 alphanumerics only');
        }
        if (ident == 'X' && fiscal != undefined && fiscal != '' && fiscal.length != 16) {
          return new ValidationResult({
            id : 'taxCd1',
            type : 'text',
            name : 'taxCd1'
          }, false, 'For Ident Client X ' + lbl1 + ' must be of 16 chars');
        }
        if (ident == 'X' && fiscal != undefined && fiscal != '' && !fiscal.match("^(?=.*[a-zA-Z])(?=.*[0-9])[0-9a-zA-Z]*$")) {
          return new ValidationResult({
            id : 'taxCd1',
            type : 'text',
            name : 'taxCd1'
          }, false, 'For Ident Client X ' + lbl1 + ' must contain alphanumeric characters.');
        }
        if (ident == 'N' && fiscal != undefined && fiscal != '' && !fiscal.match("^[0-9a-zA-Z]*$") && role == 'PROCESSOR') {
          return new ValidationResult({
            id : 'taxCd1',
            type : 'text',
            name : 'taxCd1'
          }, false, 'For Ident Client N ' + lbl1 + ' must contain digits and alphabets only');
        }
        // Defect 1593720
        var cntryRegion = FormManager.getActualValue('countryUse');
        var tempCntryRegion = '';
        if (cntryRegion != '' && cntryRegion.length > 3) {
          tempCntryRegion = cntryRegion.substring(3, 5);
        }
        console.log("cntryRegion is>>" + cntryRegion);
        if (tempCntryRegion != 'VA') {
          console.log("For Not VA CntryRegion>>" + tempCntryRegion);
          if (ident == 'N' && fiscal != undefined && (fiscal == '' || fiscal.length > 16) && role == 'PROCESSOR') {
            return new ValidationResult({
              id : 'taxCd1',
              type : 'text',
              name : 'taxCd1'
            }, false, 'For Ident Client N ' + lbl1 + ' must be of 1-16 chars');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function enableCMRNUMForPROCESSOR() {
  console.log(">>>> enableCMRNUMForPROCESSOR");
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
  } else if(role == "PROCESSOR") {
    FormManager.enable('cmrNo');
  } else {
    FormManager.readOnly('cmrNo');
  }
}

function addStreetPoBoxValidator() {
  console.log(">>>> addStreetPoBoxValidator");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var addrTxt = FormManager.getActualValue('addrTxt');
        var poBox = FormManager.getActualValue('poBox');
        var addrType = FormManager.getActualValue('addrType');
        if ((addrType != undefined && addrType != '') && (addrType == 'ZS01' || addrType == 'ZP01')) {
          if (addrTxt == '' && poBox == '') {
            return new ValidationResult(null, false, 'Please fill-out Street Address or PO Box or both.');
          }
          return new ValidationResult(null, true);
        }
        if ((addrType != undefined && addrType != '') && (addrType == 'ZI01' || addrType == 'ZD01' || addrType == 'ZS02')) {
          if (addrTxt == '') {
            return new ValidationResult({
              id : 'addrTxt',
              type : 'text',
              name : 'addrTxt'
            }, false, 'Street Address is required.');
          }
          return new ValidationResult(null, true);
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function addStreetAddressValidator() {
  console.log(">>>> addStreetAddressValidator");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var addrTxt = null;
        var type = null;
        var record = null;
        var reqType = FormManager.getActualValue('reqType').toUpperCase();
        var changedInd = null;
        
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
            record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
            if (record == null && _allAddressData != null && _allAddressData[i] != null) {
              record = _allAddressData[i];
            }
            type = record.addrType;
            addrTxt =  record.addrTxt;
            changedInd = record.updateInd;
            if (typeof (type) == 'object') {
              type = type[0];
            }
            
            if ((type != undefined && type != '') && (type == 'ZI01' || type == 'ZD01' || type == 'ZS02')) {
              if (addrTxt == '' && reqType != 'U') {
                return new ValidationResult(null, false, 'Street Address is required for Shipping, Installing and EPL address.');
              }else if(addrTxt == '' && reqType == 'U' && (changedInd == 'U' || changedInd == 'N')){
                return new ValidationResult(null, false, 'Street Address is required for Shipping, Installing and EPL address.');
              }
            }
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(),'MAIN_NAME_TAB', 'frmCMR');
}

function addSBOSRLogicIE() {
  console.log(">>>> addSBOSRLogicIE");
  var reqType = FormManager.getActualValue('reqType');
  if (reqType != 'C') {
    return;
  }
  var _isuCd = FormManager.getActualValue('isuCd');
  var _clientTier = FormManager.getActualValue('clientTier');
  var salesRepValue = [];
  if (_isuCd != '' && _clientTier != '') {
    var qParams = {
      _qall : 'Y',
      ISU_CD : _isuCd,
      CLIENT_TIER : '%' + _clientTier + '%',
    };
    var results = cmr.query('GET.SALESREP.IRELAND', qParams);
    if (results != null) {
      for (var i = 0; i < results.length; i++) {
        salesRepValue.push(results[i].ret1);
      }

      if (_isuCd == '21' && _clientTier == '7') {
        salesRepValue.push('MMIR11');
      } else if (_isuCd == '4F' && _clientTier == '7') {
        salesRepValue.push('I72089');
      } else if (_isuCd == '31' && _clientTier == '7') {
        salesRepValue.push('I72089');
      } else if (_isuCd == '18' && _clientTier == '7') {
        salesRepValue.push('MMIRE2');
      } else if (_isuCd == '15' && _clientTier == '7') {
        salesRepValue.push('MMIRE2');
      } else if (_isuCd == '34' && _clientTier == '6') {
        salesRepValue.push('MMSN11');
      } else if (_isuCd == '32' && _clientTier == 'M') {
        salesRepValue.push('MMIR12');
        salesRepValue.push('MMSN11');
      }

      FormManager.limitDropdownValues(FormManager.getField('repTeamMemberNo'), salesRepValue);
      if (salesRepValue.length == 1) {
        FormManager.setValue('repTeamMemberNo', salesRepValue[0]);
      }
    }
  }
}
function addEmbargoCodeValidatorUKI() {
  console.log(">>>> addEmbargoCodeValidatorUKI");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var embargoCd = FormManager.getActualValue('embargoCd');
        var reqType = FormManager.getActualValue('reqType');

        if (reqType != 'U' || reqType != 'X') {
          return new ValidationResult(null, true);
        }
        if (embargoCd != '' && embargoCd.length > 0) {
          embargoCd = embargoCd.trim();
          if ((embargoCd != '' && embargoCd.length == 1) && (embargoCd == 'E' || embargoCd == 'C' || embargoCd == 'J')) {
            return new ValidationResult(null, true);
          } else {
            return new ValidationResult({
              id : 'embargoCd',
              type : 'text',
              name : 'embargoCd'
            }, false, 'Embargo Code value should be only E or C or J.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function autoPopulateISUClientTierUK() {
  console.log(">>>> autoPopulateISUClientTierUK");
  var reqType = FormManager.getActualValue('reqType');
  if (cmr.currentRequestType != 'C') {
    return;
  }
  var custSubGroup = FormManager.getActualValue('custSubGrp');
  var noScenario = new Set([ 'INTER', 'XINTR', 'BUSPR', 'XBSPR' ]);

  if (custSubGroup != undefined && custSubGroup != '' && !noScenario.has(custSubGroup)) {

    var addrType = FormManager.getActualValue('addrType');
    var postCd = FormManager.getActualValue('postCd');
    if (postCd != null && postCd.length > 2) {
      postCd = postCd.substring(0, 2);
    }
    if (addrType != '' && addrType == 'ZS01') {
      if (postCd != '' && (isNorthernIrelandPostCd(postCd) || isScotlandPostCd(postCd))) {
        FormManager.setValue('isuCd', "32");
        FormManager.setValue('clientTier', "C");
      } else {
        FormManager.setValue('clientTier', "S");
      }
    }
  } else {
    FormManager.setValue('isuCd', "21");
    FormManager.setValue('clientTier', "7");
  }
}

function autoSetISUClientTierUK() {
  console.log(">>>> autoSetISUClientTierUK");
  var custSubGroup = FormManager.getActualValue('custSubGrp');
  var reqType = FormManager.getActualValue('reqType');
  var noScenario = new Set([ 'INTER', 'XINTR', 'BUSPR', 'XBSPR' ]);

  if (reqType != 'C') {
    return;
  }

  if ('866' == FormManager.getActualValue('cmrIssuingCntry')) {
    if (custSubGroup != undefined && custSubGroup != '' && !noScenario.has(custSubGroup)) {

      var _reqId = FormManager.getActualValue('reqId');
      var postCdParams = {
        REQ_ID : _reqId,
        ADDR_TYPE : "ZS01",
      };

      var postCdResult = cmr.query('ADDR.GET.POSTCD.BY_REQID_ADDRTYP', postCdParams);
      var postCd = postCdResult.ret1;
      if (postCd != null && postCd.length > 2) {
        postCd = postCd.substring(0, 2);
      }

      if (postCd != '' && (isNorthernIrelandPostCd(postCd) || isScotlandPostCd(postCd))) {
        FormManager.setValue('isuCd', "32");
        FormManager.setValue('clientTier', "C");
      } else {
        FormManager.setValue('clientTier', "S");
      }
    } else {
      FormManager.setValue('isuCd', '21');
      // CREATCMR-4293
      // FormManager.setValue('clientTier', '7');
    }
  }
}
function disableAddrFieldsUKI() {
  console.log(">>>> disableAddrFieldsUKI");
  var addrType = FormManager.getActualValue('addrType');
  if (addrType != 'ZS01' && addrType != 'ZD01') {
    FormManager.setValue('custPhone', '');
    FormManager.disable('custPhone');
  } else {
    FormManager.enable('custPhone');
  }
}

function autoSetAbbrevLocUKI() {
  console.log(">>>> autoSetAbbrevLocUKI");
  var _custType = FormManager.getActualValue('custSubGrp');
  var _custGrp = FormManager.getActualValue('custGrp');
  var _zs01ReqId = FormManager.getActualValue('reqId');
  var _abbrevLocn = null;
  var _addrType = null;
  var _result = null;
  if (_custGrp == 'CROSS') {
    if (_custType == 'XINTR') {
      _addrType = 'ZI01';
    } else if (_custType == 'XBSPR' || _custType == 'XGOVR' || _custType == 'XPRIC' || _custType == 'CROSS') {
      _addrType = 'ZS01';
    }
    var qParams = {
      REQ_ID : _zs01ReqId,
      ADDR_TYPE : _addrType,
    };

    if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.IRELAND) {
      _result = cmr.query('ADDR.GET.CITY1.BY_REQID_ADDRTYP', qParams);
    } else {
      _result = cmr.query('ADDR.GET.LANDCNTRY.BY_REQID_ADDRTYP', qParams);
    }

    // _result = cmr.query('ADDR.GET.LANDCNTRY.BY_REQID_ADDRTYP', qParams);
    _abbrevLocn = _result.ret1;
  } else {
    if (_custType == 'SOFTL') {
      _abbrevLocn = "SOFTLAYER";
    } else {
      var _zs01ReqId = FormManager.getActualValue('reqId');
      var _addrType = null;
      if (_custType == 'INTER' || _custType == 'INFSL' || (_custType == 'THDPT' && FormManager.getActualValue('cmrIssuingCntry') == SysLoc.IRELAND)) {
        _addrType = 'ZI01';
      } else {
        _addrType = 'ZS01';
      }
      var qParams = {
        REQ_ID : _zs01ReqId,
        ADDR_TYPE : _addrType,
      };

      if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.UK) {
        _result = cmr.query('ADDR.GET.POSTCD.BY_REQID_ADDRTYP', qParams);
      } else {
        _result = cmr.query('ADDR.GET.CITY1.BY_REQID_ADDRTYP', qParams);
      }
      _abbrevLocn = _result.ret1;
    }
  }

  if (_abbrevLocn != null && _abbrevLocn.length > 12) {
    _abbrevLocn = _abbrevLocn.substr(0, 12);
  }
  FormManager.setValue('abbrevLocn', _abbrevLocn);
  if (_custType == 'SOFTL') {
    FormManager.readOnly('abbrevLocn');
  } else {
    FormManager.enable('abbrevLocn');
  }
}
function islandedCountry(landCntry) {
  console.log(">>>> islandedCountry");
  var islandedCountry = false;
  for (var i = 0; i < UK_LANDED_CNTRY.length; i++) {
    if (landCntry == UK_LANDED_CNTRY[i]) {
      islandedCountry = true;
      break;
    }
  }
  if (islandedCountry == true) {
    return true;
  } else {
    return false;
  }
}

function optionalRulePostalCodeUK() {
  console.log(">>>> optionalRulePostalCodeUK");
  var landCntry = FormManager.getActualValue('landCntry');
  var reqType = FormManager.getActualValue('reqType');

  if (reqType != 'C') {
    return;
  }

  if (landCntry != 'undefined' && islandedCountry(landCntry)) {
    FormManager.resetValidations('postCd');
  } else {
    FormManager.addValidator('postCd', Validators.REQUIRED, [ 'Postal Code' ], 'MAIN_NAME_TAB');
  }
}

function optionalRuleForVatUK() {
  console.log(">>>> optionalRuleForVatUK");
  if (reqType != 'C') {
    return;
  }
  var issuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  var custGrp = FormManager.getActualValue('custGrp');
  var landCntry = FormManager.getActualValue('landCntry');

  if (issuingCntry == SysLoc.UK && (custGrp != undefined && custGrp != '') && custGrp == 'CROSS') {
    var _reqId = FormManager.getActualValue('reqId');
    var params = {
      REQ_ID : _reqId,
      ADDR_TYPE : "ZS01"
    };

    var landCntryResult = cmr.query('ADDR.GET.LAND_CNTRY.BY_REQID', params);
    landCntry = landCntryResult.ret1;

    if (landCntry != 'undefined' && islandedCountry(landCntry)) {
      FormManager.resetValidations('vat');
      FormManager.enable('vat');
    } else {
      FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
    }
  }
}
function setTaxCdBasedOnlandCntryUK() {
  console.log(">>>> setTaxCdBasedOnlandCntryUK");
  var custGrp = FormManager.getActualValue('custGrp');
  var issuingCntry = FormManager.getActualValue('cmrIssuingCntry');

  if (issuingCntry == SysLoc.UK && (custGrp != undefined && custGrp != '') && custGrp == 'CROSS') {
    var landCntry = FormManager.getActualValue('landCntry');
    var addrType = FormManager.getActualValue('addrType');
    var custSubGrp = FormManager.getActualValue('custSubGrp');

    if (addrType != null && addrType == 'ZS01') {
      if (landCntry != 'undefined' && landCntry == 'IM') {
        FormManager.setValue('specialTaxCd', 'Bl');
        FormManager.enable('specialTaxCd');
      } else {
        if (custSubGrp != null && custSubGrp == 'XINTR') {
          FormManager.setValue('specialTaxCd', 'XX');
          FormManager.readOnly('specialTaxCd');
        } else {
          FormManager.setValue('specialTaxCd', '32');
          FormManager.enable('specialTaxCd');
        }
      }
    }
  }
}

function autoSetABLocnAddr(_addrType) {
  console.log(">>>> autoSetABLocnAddr");
  var _custType = FormManager.getActualValue('custSubGrp');
  var _custGrp = FormManager.getActualValue('custGrp');
  var _abbrevLocn = null;
  var _result = null;
  if (_custGrp == 'CROSS') {
    if (_addrType == 'ZS01' && (_custType == 'XBSPR' || _custType == 'XGOVR' || _custType == 'XPRIC' || _custType == 'CROSS')) {
      _abbrevLocn = FormManager.getActualValue('landCntry');
    }
  } else {
    if (_custType == 'SOFTL') {
      _abbrevLocn = "SOFTLAYER";
    } else {
      if (_custType == 'INTER' || _custType == 'INFSL') {
        return;
      } else if (_addrType == 'ZS01') {
        if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.UK) {
          _abbrevLocn = FormManager.getActualValue('postCd');
        } else {
          _abbrevLocn = FormManager.getActualValue('city1');
        }
      }
    }
  }

  if (_abbrevLocn != null && _abbrevLocn.length > 12) {
    _abbrevLocn = _abbrevLocn.substr(0, 12);
  }
  FormManager.setValue('abbrevLocn', _abbrevLocn);
  if (_custType == 'SOFTL') {
    FormManager.readOnly('abbrevLocn');
  } else {
    FormManager.enable('abbrevLocn');
  }
}

function autoPopulateABLocnUK(cntry, addressMode, saving, finalSave, force) {
  console.log(">>>> autoPopulateABLocnUK");
  var reqType = null;
  var role = null;
  var _zs01ReqId = FormManager.getActualValue('reqId');
  if (typeof (_pagemodel) != 'undefined') {
    reqType = FormManager.getActualValue('reqType');
    role = _pagemodel.userRole;
  }
  if (reqType != 'C') {
    return;
  }
  if (role != 'Requester') {
    return;
  }
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  if (saving || finalSave) {
    return;
  }

  var addressTyp = FormManager.getActualValue('addrType');
  var oldVal = null;
  var currAbbrevLocn = FormManager.getActualValue('abbrevLocn');

  if (addressTyp == 'ZS01') {
    var qParams = {
      REQ_ID : _zs01ReqId,
      ADDR_TYPE : addressTyp,
    };

    if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.UK) {
      var res = cmr.query('GET_OLD_POST_CD', qParams);
      oldVal = res.ret1;
    }
  }

  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.IRELAND) {
    var res = cmr.query('GET_OLD_CITY', qParams);
    oldVal = res.ret1;
  }

  if (oldVal != null && oldVal != undefined && currAbbrevLocn == oldVal) {
    if (addressTyp == 'ZS01') {
      autoSetABLocnAddr(addressTyp);
    }
  }
}

function disableProcpectCmrCY() {
  console.log(">>>> disableProcpectCmrCY");
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  var ifProspect = FormManager.getActualValue('prospLegalInd');
  if (dijit.byId('prospLegalInd')) {
    ifProspect = dijit.byId('prospLegalInd').get('checked') ? 'Y' : 'N';
  }
  console.log("disable prospect CMR");
  if ('Y' == ifProspect) {
    FormManager.readOnly('cmrNo');
  }
}

function mandatoryForBusinessPartnerCY() {
  console.log(">>>> mandatoryForBusinessPartnerCY");
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var reqType = FormManager.getActualValue('reqType');
  if (reqType == 'C') {
    var _custType = FormManager.getActualValue('custSubGrp');
    if (_custType == 'BUSPR' || _custType == 'CRBUS') {
      FormManager.show('PPSCEID', 'ppsceid');
      FormManager.enable('ppsceid');
      FormManager.addValidator('ppsceid', Validators.REQUIRED, [ 'PPS CEID' ], 'MAIN_IBM_TAB');
    } else {
      FormManager.setValue('ppsceid', '');
      FormManager.readOnly('ppsceid');
      FormManager.resetValidations('ppsceid');
      FormManager.removeValidator('ppsceid', Validators.REQUIRED);
    }
  }
}

function showOnlyMailingOnFirstAddrAdd() {
  console.log(">>>> showOnlyMailingOnFirstAddrAdd");
  if(_allAddressData == null || _allAddressData.length == 0) {
    cmr.hideNode('radiocont_ZP01');
    cmr.hideNode('radiocont_ZD01');
    cmr.hideNode('radiocont_ZI01');
    cmr.hideNode('radiocont_ZS02');
  } 
}

function validateCollectionCodeforCyprus(){
  console.log(">>>> validateCollectionCodeforCyprus");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        console.log('checking collection code for update request..');
        if(FormManager.getActualValue('reqType') != 'U' || FormManager.getActualValue('collectionCd') == ''){
          return;
        }
        var reqId = FormManager.getActualValue('reqId');
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        var origCollectionCd = null;
        var lovCollectionCd = [];
        var result = cmr.query("GET.CMRINFO.IMPORTED", {
          REQ_ID : reqId
        });
        lovCollectionCd = cmr.query("GET.COLLECTIONCD", {
          CMR_ISSUING_CNTRY : cntry,
          _qall : 'Y'
        });
        
        if (result != null && result != '') {
          origCollectionCd = result.ret5;
        }
      
        var check = lovCollectionCd.map(obj => obj.ret1);
        if(origCollectionCd != '' && !check.includes(origCollectionCd)){
            return new ValidationResult({
              id : 'collectionCd',
              type : 'text',
              name : 'collectionCd'}, false, 'Please select correct Collection Code.');
        }else{
          return new ValidationResult(null,true);
        }
     }
   };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function setAbbrvCyprus(){
  console.log(">>>> setAbbrvCyprus");
  var reqType = FormManager.getActualValue('reqType');
  var reqId = FormManager.getActualValue('reqId');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (reqType == 'C' && role == 'REQUESTER') {
    if (reqId != null) {
      reqParam = {
        REQ_ID : reqId
      };
    }
    var custNm = cmr.query('ADDR.GET.CUSTNM1.BY_REQID', reqParam);
    var abbrvNm = custNm.ret1;
    if (abbrvNm != null) {
      if (abbrvNm.length > 22) {
        abbrvNm = abbrvNm.substring(0, 22);
      }
      FormManager.setValue('abbrevNm', abbrvNm);      
    }
  }else if(role == 'PROCESSOR'){
    var abbrevNm = FormManager.getActualValue('abbrevNm');
    if (abbrevNm.length > 22) {
      abbrevNm = abbrevNm.substring(0, 22);
    }
    FormManager.setValue('abbrevNm', abbrevNm);  
  }
}

function addInacCodeValidator() {
  console.log(">>>> addInacCodeValidator");
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

function modeOfPaymentValidator() {
  console.log(">>>> modeOfPaymentValidator");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var modeOfPayment = FormManager.getActualValue('modeOfPayment');
        var role = FormManager.getActualValue('userRole');
        if (FormManager.getActualValue('reqType') == 'U') {
          if (modeOfPayment != "" && modeOfPayment != '5' && modeOfPayment != null) {
            return new ValidationResult({
              id : 'modeOfPayment',
              type : 'text',
              name : 'modeOfPayment'
            }, false, 'Mode of Payment can either be 5 or blank.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function crossborderScenariosAbbrvLoc() {
  console.log(">>>> crossborderScenariosAbbrvLoc");
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  if (role != 'REQUESTER') {
    return;
  }
  if (crossborderScenariosInterval != null) {
    clearInterval(crossborderScenarios);
  }
  crossborderScenariosInterval = setInterval(function() {
    var custGroup = FormManager.getActualValue('custGrp');

    if (custGroup != null && custGroup.length > 0) {
      if (custGroup == "CROSS") {
        var reqId = FormManager.getActualValue('reqId');
        if (reqId != null) {
          reqParam = {
            REQ_ID : reqId,
            ADDR_TYPE : "ZS01",
          };
        }
        var results = cmr.query('ADDR.GET.LANDCNTRY.BY_REQID_ADDRTYP', reqParam);
        var abbrevLocn = results.ret1;
        if (abbrevLocn != null && abbrevLocn.length > 12) {
          abbrevLocn = abbrevLocn.substring(0, 12);
        }
        if (abbrevLocn != null) {
          FormManager.setValue('abbrevLocn', abbrevLocn);
        } else {
          FormManager.setValue('abbrevLocn', '');
        }
        clearInterval(crossborderScenariosInterval);
      } else {
        clearInterval(crossborderScenariosInterval);
      }
    }
  }, 1000);
}

function crossborderScenariosAbbrvLocOnChange() {
  console.log(">>>> crossborderScenariosAbbrvLocOnChange");
  dojo.connect(FormManager.getField('custGrp'), 'onChange', function(value) {
    var role = FormManager.getActualValue('userRole').toUpperCase();
    if (FormManager.getActualValue('reqType') != 'C') {
      return;
    }
    if (role != 'REQUESTER') {
      return;
    }
    var custGroup = FormManager.getActualValue('custGrp');

    if (custGroup == "CROSS") {
      var reqId = FormManager.getActualValue('reqId');
      if (reqId != null) {
        reqParam = {
          REQ_ID : reqId,
          ADDR_TYPE : "ZS01",
        };
      }
      var results = cmr.query('ADDR.GET.LANDCNTRY.BY_REQID_ADDRTYP', reqParam);
      var abbrevLocn = results.ret1;
      if (abbrevLocn != null && abbrevLocn.length > 12) {
        abbrevLocn = abbrevLocn.substring(0, 12);
      }
      if (abbrevLocn != null) {
        FormManager.setValue('abbrevLocn', abbrevLocn);
      } else {
        FormManager.setValue('abbrevLocn', '');
      }
    }
  });
}

function validateSalesRepISR() {
  console.log(">>>> validateSalesRepISR");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var salRep = FormManager.getActualValue('salesTeamCd');
        if (salRep.length >= 1 && salRep.length != 6) {
            return new ValidationResult(null, false, 'Sales Rep should be 6 digit long.');
        }
        return new ValidationResult(null, true);
       }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
} 

function validateISR() {
  console.log(">>>> validateISR");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var isr = FormManager.getActualValue('repTeamMemberNo');
        if(isr.length >= 1 && isr.length != 6){
            return new ValidationResult(null, false, 'ISR should be 6 digit long.');
        }
        return new ValidationResult(null, true);
       }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function addEnterpriseValidator() {
  console.log(">>>> addEnterpriseValidator");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var entNo = FormManager.getActualValue('enterprise');
        if (entNo != '' && entNo.length != 6) {
          return new ValidationResult(null, false, 'Enterprise Number should have exactly 6 digits.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

// CREATCMR-4293
function setCTCValues() {
  console.log(">>>> setCTCValues");
  FormManager.removeValidator('clientTier', Validators.REQUIRED);
  
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  
  // Business Partner OR Internal
  if(custSubGrp == 'CRBUS' || custSubGrp == 'CRINT'|| custSubGrp == 'BUSPR' || custSubGrp == 'INTER') {
    var isuCd = FormManager.getActualValue('isuCd');
    if(isuCd =='21'){
      // FormManager.setValue('clientTier', _pagemodel.clientTier == null ? '' :
      // _pagemodel.clientTier);
      // FormManager.enable('clientTier');
      FormManager.setValue('clientTier', '');
      FormManager.readOnly('clientTier');
    }
  }
}

function iSUCTCEnterpriseCombinedCodeValidatorForCYPRUS() {
  console.log(">>>> iSUCTCEnterpriseCombinedCodeValidatorForCYPRUS");
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var custSubGroup = FormManager.getActualValue('custSubGrp');
  var isuCd = FormManager.getActualValue('isuCd');
  var clientTier = FormManager.getActualValue('clientTier');
  var enterprise = FormManager.getActualValue('enterprise');

  var isuCdSet = new Set([ '34', '36', '5K', '32' ]);
  var isuCdSet1 = new Set([ '21', '5K' ]);
  var custSubGroupSet = new Set(['CRINT','CRBUS','BUSPR','INTER','IBMEM']);

  var isuCtc = isuCd + clientTier;
  var isuCtcEnterprise = isuCtc + enterprise;

  FormManager.removeValidator('isuCd', Validators.REQUIRED);
  FormManager.removeValidator('clientTier', Validators.REQUIRED);
  FormManager.removeValidator('enterprise', Validators.REQUIRED);

  if (custSubGroup == '') {
    return new ValidationResult(null, true);
  } else if (!isuCdSet.has(isuCd) && !custSubGroupSet.has(custSubGroup)) {
    return new ValidationResult({
      id : 'isuCd',
      type : 'text',
      name : 'isuCd'
    }, false, 'ISU can only accept \'34\', \'36\', \'5K\', \'32\'.');
  } else if (isuCdSet1.has(isuCd) && clientTier != '') {
    return new ValidationResult({
      id : 'clientTier',
      type : 'text',
      name : 'clientTier'
    }, false, 'Client Tier can only accept blank.');
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
  } else if (isuCd == '32' && clientTier != 'T') {
    return new ValidationResult({
      id : 'clientTier',
      type : 'text',
      name : 'clientTier'
    }, false, 'Client Tier can only accept \'T\'.');
  } else if (isuCdSet1.has(isuCtc) && enterprise != '985999') {
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
  } else if (isuCd == '36' && enterprise != '822840') {
    return new ValidationResult({
      id : 'enterprise',
      type : 'text',
      name : 'enterprise'
    }, false, 'Enterprise can only accept \'822840\'.');
  } else {
    return new ValidationResult(null, true);
  }
}

function clientTierCodeValidator() {
  console.log(">>>> clientTierCodeValidator");
  var isuCode = FormManager.getActualValue('isuCd');
  var clientTierCode = FormManager.getActualValue('clientTier');
  var reqType = FormManager.getActualValue('reqType');
  
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var reqType = FormManager.getActualValue('reqType'); 
  if (cntry == SysLoc.CYPRUS && reqType == 'C') {
    return iSUCTCEnterpriseCombinedCodeValidatorForCYPRUS();
  }

  if (((isuCode == '21' || isuCode == '8B' || isuCode == '5K') && reqType == 'C') || (isuCode != '34' && reqType == 'U')) {
    if (clientTierCode == '') {
      $("#clientTierSpan").html('');

      return new ValidationResult(null, true);
    } else {
      $("#clientTierSpan").html('');

      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier can only accept blank.');
    }
  } else if (isuCode == '34') {
    if (clientTierCode == '') { 
      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier code is Mandatory.');
    } else if (clientTierCode == 'Q' || clientTierCode == 'Y') {
      return new ValidationResult(null, true);
    } else {
      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier can only accept \'Q\' or \'Y\'.');
    }
  } else {
    if (clientTierCode == 'Q' || clientTierCode == 'Y' || clientTierCode == '') {
      $("#clientTierSpan").html('');

      return new ValidationResult(null, true);
    } else {
      $("#clientTierSpan").html('');
      $("#clientTierSpan").append('<span style="color:red" class="cmr-ast" id="ast-clientTier">* </span>');

      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier can only accept \'Q\', \'Y\' or blank.');
    }
  }
}
// CREATCMR-4293

function clientTierValidator() {
  console.log(">>>> clientTierValidator");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var clientTier = FormManager.getActualValue('clientTier');
        var isuCd = FormManager.getActualValue('isuCd');
        var reqType = FormManager.getActualValue('reqType');
        var valResult = null;
        
        var oldClientTier = null;
        var oldISU = null;
        var requestId = FormManager.getActualValue('reqId');
        
        if (reqType == 'C') {
          valResult = clientTierCodeValidator();
        } else {
          qParams = {
              REQ_ID : requestId,
          };
          var result = cmr.query('GET.CLIENT_TIER_EMBARGO_CD_OLD_BY_REQID', qParams);
          
          if (result != null && result != '') {
            oldClientTier = result.ret1 != null ? result.ret1 : '';
            oldISU =  result.ret3 != null ? result.ret3 : '';
            
            if (clientTier != oldClientTier || isuCd != oldISU) {
              valResult = clientTierCodeValidator();
            }
          }
        }
        return valResult;
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}


function setValuesWRTIsuCtc(ctc){
  console.log(">>>> setValuesWRTIsuCtc");
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var isu = FormManager.getActualValue('isuCd');
  if(ctc==null){
    var ctc = FormManager.getActualValue('clientTier');
  }
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  if (isu == '34' && ctc == 'Q') {
    FormManager.setValue('enterprise', '822830');
    FormManager.setValue('salesTeamCd', '000000');
    FormManager.setValue('repTeamMemberNo', '000000');
    FormManager.setValue('salesBusOffCd', '000');
    FormManager.enable('enterprise');
    FormManager.readOnly('salesTeamCd')
    FormManager.readOnly('repTeamMemberNo');
    FormManager.readOnly('salesBusOffCd');
  } else if (isu == '36' && ctc == 'Y') {
    FormManager.setValue('enterprise', '822840');
    FormManager.setValue('salesTeamCd', '000000');
    FormManager.setValue('repTeamMemberNo', '000000');
    FormManager.setValue('salesBusOffCd', '000');
    FormManager.enable('enterprise');
    FormManager.readOnly('salesTeamCd')
    FormManager.readOnly('repTeamMemberNo');
    FormManager.readOnly('salesBusOffCd');
  } else if (isu == '32' && ctc == 'T') {
    FormManager.setValue('enterprise', '985985');
    FormManager.setValue('salesTeamCd', '000000');
    FormManager.setValue('repTeamMemberNo', '000000');
    FormManager.setValue('salesBusOffCd', '000');
    FormManager.enable('enterprise');
    FormManager.readOnly('salesTeamCd')
    FormManager.readOnly('repTeamMemberNo');
    FormManager.readOnly('salesBusOffCd');
  } else if ((isu == '5K' || isu == '21') && ctc == '') {
    FormManager.setValue('enterprise', '985999');
    FormManager.setValue('salesTeamCd', '000000');
    FormManager.setValue('repTeamMemberNo', '000000');
    FormManager.setValue('salesBusOffCd', '000');
    FormManager.enable('enterprise');
    FormManager.readOnly('salesTeamCd')
    FormManager.readOnly('repTeamMemberNo');
    FormManager.readOnly('salesBusOffCd');
  }
  if(role == 'REQUESTER') {
    FormManager.removeValidator('enterprise', Validators.REQUIRED);
  } else {
    FormManager.addValidator('enterprise', Validators.REQUIRED, [ 'Enterprise' ]);
  }
}
function addressQuotationValidatorCY() {
  console.log(">>>> addressQuotationValidatorCY");
  // CREATCMR-788
  FormManager.addValidator('abbrevNm', Validators.NO_QUOTATION, [ 'Abbreviated Name (TELX1)' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('abbrevLocn', Validators.NO_QUOTATION, [ 'Abbreviated Location' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('custNm1', Validators.NO_QUOTATION, [ 'Customer Name' ]);
  FormManager.addValidator('custNm2', Validators.NO_QUOTATION, [ 'Customer Name Con\'t' ]);
  FormManager.addValidator('custNm4', Validators.NO_QUOTATION, [ 'Att Person' ]);
  FormManager.addValidator('addrTxt', Validators.NO_QUOTATION, [ 'Street Address' ]);
  FormManager.addValidator('addrTxt2', Validators.NO_QUOTATION, [ 'Address Con\'t/Occupation' ]);
  FormManager.addValidator('city1', Validators.NO_QUOTATION, [ 'City' ]);
  FormManager.addValidator('postCd', Validators.NO_QUOTATION, [ 'Postal Code' ]);
  FormManager.addValidator('poBox', Validators.NO_QUOTATION, [ 'PO Box' ]);
  FormManager.addValidator('custPhone', Validators.NO_QUOTATION, [ 'Phone #' ]);
}

function checkCmrUpdateBeforeImport() {
  console.log(">>>> checkCmrUpdateBeforeImport");
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

dojo.addOnLoad(function() {
  GEOHandler.EMEA = [ SysLoc.UK, SysLoc.IRELAND, SysLoc.ISRAEL, SysLoc.TURKEY, SysLoc.GREECE, SysLoc.CYPRUS, SysLoc.ITALY ];
  console.log('adding EMEA functions...');
  GEOHandler.addAddrFunction(addEMEALandedCountryHandler, GEOHandler.EMEA);
  GEOHandler.enableCopyAddress(GEOHandler.EMEA, validateEMEACopy, [ 'ZD01', 'ZI01' ]);
  GEOHandler.enableCustomerNamesOnAddress(GEOHandler.EMEA);
  GEOHandler.addAddrFunction(updateMainCustomerNames, GEOHandler.EMEA);
  GEOHandler.setRevertIsicBehavior(false);
  GEOHandler.addAddrFunction(addPhoneValidatorEMEA, [ SysLoc.ISRAEL, SysLoc.GREECE, SysLoc.CYPRUS, SysLoc.TURKEY ]);
  
  // Turkey
  GEOHandler.addAfterConfig(setFieldsToReadOnlyGRCYTR, [ SysLoc.GREECE, SysLoc.CYPRUS, SysLoc.TURKEY ]);
  GEOHandler.addAfterTemplateLoad(setFieldsToReadOnlyGRCYTR, [ SysLoc.GREECE, SysLoc.CYPRUS, SysLoc.TURKEY ]);
  GEOHandler.addAddrFunction(addrFunctionForGRCYTR, [ SysLoc.GREECE, SysLoc.CYPRUS, SysLoc.TURKEY ]);
  GEOHandler.addAddrFunction(disableAddrFieldsGRCYTR, [ SysLoc.CYPRUS, SysLoc.TURKEY ]);
  GEOHandler.addAddrFunction(preFillTranslationAddrWithSoldToForTR, [ SysLoc.GREECE ]);
  GEOHandler.registerValidator(addTRAddressTypeValidator, [ SysLoc.TURKEY ], null, true);
  GEOHandler.registerValidator(addGenericVATValidator(SysLoc.TURKEY, 'MAIN_CUST_TAB', 'frmCMR'), [ SysLoc.TURKEY ], null, true);
  GEOHandler.addAfterConfig(salesSRforUpdate, [ SysLoc.CYPRUS ]);

  // Greece
  GEOHandler.addAfterConfig(addHandlersForCY, [ SysLoc.CYPRUS ]);
  
  // Customer Type behaviour for CY
  GEOHandler.addAfterConfig(setClientTierAndISR, [ SysLoc.CYPRUS ]);
  GEOHandler.addAfterConfig(setTypeOfCustomerBehaviorForCY, [ SysLoc.CYPRUS ]);
  GEOHandler.addAddrFunction(disableAddrFieldsGR, [ SysLoc.GREECE ]);
  GEOHandler.addAddrFunction(addLatinCharValidator, [ SysLoc.GREECE ]);
  GEOHandler.addAddrFunction(addPOBoxValidatorGR, [ SysLoc.GREECE ]);
  GEOHandler.addAddrFunction(updateAddrTypeList, [ SysLoc.TURKEY ]);
  GEOHandler.addAddrFunction(convertToUpperCaseGR, [ SysLoc.GREECE ]);
  GEOHandler.addAddrFunction(preFillTranslationAddrWithSoldToForGR, [ SysLoc.GREECE ]);
  GEOHandler.addAddrFunction(updateAbbrevNmLocnGRCYTR, [ SysLoc.GREECE, SysLoc.CYPRUS, SysLoc.TURKEY ]);
  GEOHandler.registerValidator(addOccupationPOBoxAttnPersonValidatorForCY, [ SysLoc.CYPRUS ], null, true);
  GEOHandler.addAfterConfig(clearPOBoxFromGrid, [ SysLoc.CYPRUS ]);
  GEOHandler.addAfterTemplateLoad(retainLandCntryValuesOnCopy, [ SysLoc.GREECE ]);
  GEOHandler.addAfterConfig(setEnterprise, [ SysLoc.CYPRUS ]);

  GEOHandler.addAddrFunction(setPostalCodeTurGreCypValidator, [ SysLoc.GREECE, SysLoc.CYPRUS, SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(abbrvLocMandatory, [ SysLoc.GREECE, SysLoc.CYPRUS, SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(abbrvLocMandatoryOnChange, [ SysLoc.GREECE, SysLoc.CYPRUS, SysLoc.TURKEY ]);
  GEOHandler.registerValidator(addGenericVATValidator(SysLoc.GREECE, 'MAIN_CUST_TAB', 'frmCMR'), [ SysLoc.GREECE ], null, true);
  GEOHandler.addAfterConfig(hideCustPhoneonSummary, [ SysLoc.GREECE, SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(addHandlerForCustSubTypeBpGRTRCY, [ SysLoc.GREECE, SysLoc.CYPRUS, SysLoc.TURKEY ]);
  GEOHandler.addAfterTemplateLoad(setCustSubTypeBpGRTRCY, [ SysLoc.GREECE, SysLoc.CYPRUS, SysLoc.TURKEY ]);

  // Cyprus
  GEOHandler.addAddrFunction(addLatinCharValidator, [ SysLoc.CYPRUS ]);
  GEOHandler.addAddrFunction(addNonLatinCharValidator, [ SysLoc.CYPRUS ]);
  GEOHandler.registerValidator(addCYAddressTypeValidator, [ SysLoc.CYPRUS ], null, true);
  GEOHandler.registerValidator(addGenericVATValidator(SysLoc.CYPRUS, 'MAIN_CUST_TAB', 'frmCMR'), [ SysLoc.CYPRUS ], null, true);

  // common greece/cyprus/turkey
  GEOHandler.addAfterConfig(setCommonCollectionCd, [ SysLoc.GREECE, SysLoc.CYPRUS, SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(disableINACEnterpriseOnViewOnly, [ SysLoc.GREECE, SysLoc.CYPRUS, SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(viewOnlyAddressDetails, [ SysLoc.GREECE, SysLoc.CYPRUS, SysLoc.TURKEY ]);

  // common israel/greece/cyprus/turkey
  GEOHandler.addAfterConfig(defaultCapIndicator, [ SysLoc.ISRAEL, SysLoc.GREECE, SysLoc.CYPRUS, SysLoc.TURKEY ]);

  /* 1438717 - add DPL match validation for failed dpl checks */
  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.EMEA, GEOHandler.ROLE_PROCESSOR, true);

  // For EmbargoCode
  GEOHandler.addAfterConfig(lockEmbargo, SysLoc.CYPRUS);

  // For Legacy Direct
  GEOHandler.addAfterConfig(setFieldsBehaviourCY, [ SysLoc.CYPRUS ]);
  GEOHandler.addAfterTemplateLoad(setFieldsBehaviourCY, [ SysLoc.CYPRUS ]);

  // CYPRUS Legacy
  GEOHandler.registerValidator(validateCollectionCodeforCyprus, [ SysLoc.CYPRUS ],null, true);
  GEOHandler.addAfterConfig(setAbbrvCyprus, [ SysLoc.CYPRUS ]);
  GEOHandler.addAfterConfig(enableCMRNUMForPROCESSOR, [ SysLoc.CYPRUS ]);
  GEOHandler.addAddrFunction(disableAddrFieldsCY, [ SysLoc.CYPRUS ]);
  GEOHandler.addAddrFunction(mandatoryForBusinessPartnerCY, [ SysLoc.CYPRUS ]);
  GEOHandler.addAddrFunction(showOnlyMailingOnFirstAddrAdd, [ SysLoc.CYPRUS ]);
  GEOHandler.addAfterTemplateLoad(mandatoryForBusinessPartnerCY, [ SysLoc.CYPRUS ]);
  GEOHandler.addAfterTemplateLoad(setVatValidator, [ SysLoc.CYPRUS ]);
  GEOHandler.registerValidator(addInacCodeValidator, [ SysLoc.CYPRUS ], null, true);
  GEOHandler.registerValidator(modeOfPaymentValidator, [ SysLoc.CYPRUS ], null, true);
  GEOHandler.addAfterConfig(disableProcpectCmrCY, [ SysLoc.CYPRUS ]);
  GEOHandler.addAfterTemplateLoad(disableProcpectCmrCY, [ SysLoc.CYPRUS ]);
  GEOHandler.registerValidator(addStreetAddressValidator, [ SysLoc.CYPRUS ], null, true);
  GEOHandler.addAfterConfig(crossborderScenariosAbbrvLoc, [ SysLoc.CYPRUS ]);
  GEOHandler.addAfterConfig(crossborderScenariosAbbrvLocOnChange, [ SysLoc.CYPRUS ]);
  GEOHandler.registerValidator(validateSalesRepISR, [ SysLoc.CYPRUS ], null, true);
  GEOHandler.addAfterTemplateLoad(retainImportValues, [ SysLoc.CYPRUS ]);
  GEOHandler.registerValidator(validateISR, [ SysLoc.CYPRUS ], null, true);
  GEOHandler.registerValidator(addEnterpriseValidator,  [ SysLoc.CYPRUS ], null, true);
  GEOHandler.registerValidator(addStreetPoBoxValidator, [ SysLoc.CYPRUS ], null, true);
  GEOHandler.addAfterTemplateLoad(setClientTierAndISR, [ SysLoc.CYPRUS ]);
  
  // CREATCMR-4293
  GEOHandler.addAfterTemplateLoad(setCTCValues, [ SysLoc.CYPRUS ]);
  GEOHandler.registerValidator(clientTierValidator, [ SysLoc.CYPRUS ], null, true);
  
  GEOHandler.addAfterTemplateLoad(addISUHandler, [ SysLoc.CYPRUS ]);
  GEOHandler.addAfterConfig(addISUHandler, [ SysLoc.CYPRUS ]);
  GEOHandler.registerValidator(checkCmrUpdateBeforeImport, [ SysLoc.CYPRUS ], null, true);

});
