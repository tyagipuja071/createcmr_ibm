/* Register Turkey Javascripts */
var _customerTypeHandler = null;
var _vatExemptHandler = null;
var _isuCdHandler = null;
var _isicCdHandler = null;
var _requestingLOBHandler = null;
var _subIndustryCdHandler = null;
var _economicCdHandler = null;
var _custSubTypeHandler = null;
var _custSubTypeHandlerGr = null;
var _custSalesRepHandlerGr = null;
var _landCntryHandler = null;
var _stateProvITHandler = null;
var _internalDeptHandler = null;
var addrTypeHandler = [];
var _hwMstrInstallFlagHandler = null;
var _scenarioSubTypeHandler = null;
var _addrTypeOnChangeHandler = [];
var _addrTypesForOnChange = [ 'ZS01', 'ZP01', 'ZD01', 'ZI01' ];

var SCOTLAND_POST_CD = [ 'AB', 'KA', 'DD', 'KW', 'DG', 'KY', 'EH', 'ML', 'FK', 'PA', 'G1', 'G2', 'G3', 'G4', 'G5', 'G6', 'G7', 'G8', 'G9', 'PH', 'TD', 'IV' ];
var NORTHERN_IRELAND_POST_CD = [ 'BT' ];
var UK_LANDED_CNTRY = [ 'AI', 'BM', 'IO', 'VG', 'KY', 'FK', 'GI', 'MS', 'PN', 'SH', 'TC', 'GS', 'GG', 'JE', 'IM' ];
var _addrTypeITHandler = [];
var _streetITHandler = null;
var _custNameITHandler = null;
var _cityITHandler = null;
var _dropDownCityHandler = null;
var _addrTypesForIT = [ 'ZS01', 'ZP01', 'ZI01', 'ZS02' ];
var _identClientHandlerIT = null;
var _salesRepHandlerIT = null;
var _oldSalesRepIT = null;
var _oldSBOIT = null;
var _oldCollectionCdIT = null;
var _vatUpdateHandlerIT = null;
var _fiscalCodeUpdateHandlerIT = null;
var _CTCHandlerIT = null;
var _ISUHandlerIT = null;
var _oldISUIT = "";
var _oldCTCIT = "";
var _oldINACIT = "";
var _oldSpecialTaxCdIT = "";
var _oldAffiliateIT = "";
// DTN: Defect 1858294 : UKI: Internal FSL sub-scenario rules for abbreviated
// name
var _lobHandler = null;
var _landCntryHandlerUK = null;
var _postalCdUKHandler = null;
var _cityUKHandler = null;
var _custGrpIT = null;

var _importedIndc = null;

function addISUHandler() {
  console.log('addISUHandler=====');
  var _CTCHandler = null;
  _isuCdHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
    var value = FormManager.getField('isuCd');
    setClientTierAndISR(value);
    setValuesForTurkey(value);

  });
  _CTCHandler = dojo.connect(FormManager.getField('clientTier'), 'onChange', function(value) {
    var value = FormManager.getField('clientTier');
    setClientTierAndISR(value);
    setValuesForTurkey();
  });
}

// CREATCMR-2657 for TURKEY
function setSORTL() {
  console.log('setSORTL=====');
  if (FormManager.getActualValue('cmrIssuingCntry') != '862') {
    return;
  }
  _clientTierHandler = dojo.connect(FormManager.getField('clientTier'), 'onChange', function(value) {
    var clientTier = FormManager.getActualValue('clientTier');
    var reqType = FormManager.getActualValue('reqType');
    var isu = FormManager.getActualValue('isuCd');
    lockSORTL();
  });
}

// CREATCMR-2657 lock SORTL field for turkey
function lockSORTL() {
  console.log('lockSORTL=====');
  if (FormManager.getActualValue('cmrIssuingCntry') != '862') {
    return;
  }
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var reqType = FormManager.getActualValue('reqType');
  var clientTier = FormManager.getActualValue('clientTier');
  if (reqType == 'U' || reqType == 'C') {
    if (role == 'REQUESTER') {
    } else if (role == 'PROCESSOR') {
    }
  }
}

function addEMEALandedCountryHandler(cntry, addressMode, saving, finalSave) {
  console.log('addEMEALandedCountryHandler=====');
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
    }
  }
}

// CMR-2688 Turkey - Default Preferred Language to T for all Create Scenarios
function setDefaultValueForPreferredLanguage() {
  console.log("setDefaultValueForPreferredLanguage=======");
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.TURKEY) {
    var reqType = FormManager.getActualValue('reqType');
    console.log("reqType value is :" + reqType);
    if (reqType == 'C') {
      FormManager.setValue('custPrefLang', 'T');
    }
  }
}

function setISUDefaultValueOnSubTypeChange() {
  console.log('setISUDefaultValueOnSubTypeChange=======');
  if (_scenarioSubTypeHandler == null && FormManager.getField('custSubGrp')) {
    _scenarioSubTypeHandler = dojo.connect(FormManager.getField('custSubGrp'), 'onChange', function(value) {
      if (typeof (_pagemodel) != 'undefined' && _pagemodel['custSubGrp'] != FormManager.getActualValue('custSubGrp')) {
        controlFieldsBySubScenarioTR(value);
        setIsuCtcCBMEA();
        setDefaultEntCBMEA();
      }
    });
  }
}

function disableTaxOfficeTR() {
  console.log('disableTaxOfficeTR=====');
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.TURKEY) {
    console.log("disableTaxOfficeTR..............");
    var addressTypeValue = FormManager.getActualValue('addrType');
    if (addressTypeValue == 'ZP01') {
      FormManager.show('TaxOffice', 'taxOffice');
      var custType = FormManager.getActualValue('custGrp');
      if (custType == 'CROSS' || cmr.currentRequestType == 'U') {
        FormManager.removeValidator('taxOffice', Validators.REQUIRED);
      } else {
        checkAndAddValidator('taxOffice', Validators.REQUIRED, [ 'Tax Office' ]);
      }
    } else {
      FormManager.hide('TaxOffice', 'taxOffice');
      FormManager.removeValidator('taxOffice', Validators.REQUIRED);
    }
  }
}

function addDistrictPostCodeCityValidator() {
  console.log('addDistrictPostCodeCityValidator=====');
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('cmrIssuingCntry') != SysLoc.TURKEY) {
          return new ValidationResult(null, true);
        }

        var city = FormManager.getActualValue('city1');
        var dept = FormManager.getActualValue('dept');
        var post = FormManager.getActualValue('postCd');

        var val = city;
        if (dept != '') {
          val += (val.length > 0 ? ' ' : '') + dept;
        }
        if (post != '') {
          val += (val.length > 0 ? ' ' : '') + post;
        }
        if (val.length > 30) {
          return new ValidationResult(null, false, 'Total computed length of District, Postal Code, and City should be less than 30 characters.');
        }

        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

/**
 * Add Latin character validation for address fields
 */
function addLatinCharValidator() {
  console.log('addLatinCharValidator=====');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var addrType = FormManager.getActualValue('addrType');
  var custType = FormManager.getActualValue('custGrp');
  var validateNonLatin = false;

  // latin addresses
  var addrToChkForIL = new Set([ 'ZI01', 'ZS02', 'CTYA', 'CTYB', 'CTYC' ]);
  var addrToChkForGR = new Set([ 'ZS01', 'ZD01', 'ZI01' ]);
  var addrToChkForTR = new Set([ 'ZS01', 'ZD01', 'ZI01' ]);

  // for cross border
  if (custType == 'CROSS') {
    addrToChkForGR = new Set([ 'ZP01', 'ZS01', 'ZD01', 'ZI01' ]);
    addrToChkForTR = new Set([ 'ZP01', 'ZS01', 'ZD01', 'ZI01' ]);
  }

  if (cntry == SysLoc.ISRAEL && addrToChkForIL.has(addrType)) {
    validateNonLatin = true;
  } else if (cntry == SysLoc.GREECE && addrToChkForGR.has(addrType)) {
    validateNonLatin = true;
  } else if (cntry == SysLoc.TURKEY && addrToChkForTR.has(addrType)) {
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
    } else if (cntry == SysLoc.TURKEY) {
      checkAndAddValidator('custNm2', Validators.LATIN, [ 'Customer Name Con\'t' ]);
      checkAndAddValidator('addrTxt2', Validators.LATIN, [ 'Street Con\'t' ]);
      checkAndAddValidator('dept', Validators.LATIN, [ 'District' ]);
    }
    checkAndAddValidator('custNm1', Validators.LATIN, [ 'Customer Name' ]);
    checkAndAddValidator('addrTxt', Validators.LATIN, [ 'Street Address' ]);
    checkAndAddValidator('city1', Validators.LATIN, [ 'City' ]);
    // checkAndAddValidator('postCd', Validators.LATIN, [ 'Postal Code' ]);
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
 * Add Turkish character validation for address fields
 */
function addTurkishCharValidator() {
  console.log('addTurkishCharValidator=====');
  var addrType = FormManager.getActualValue('addrType');
  var custType = FormManager.getActualValue('custGrp');
  var validateTurkish = false;

  // turkish addresses
  var addrToChkForTR = new Set([]);
  var land_cntry = FormManager.getActualValue('landCntry');
  // for LOCAL
  if (custType == 'LOCAL' || land_cntry == 'TR') {
    addrToChkForTR = new Set([ 'ZP01' ]);
  }

  if (addrToChkForTR.has(addrType)) {
    validateTurkish = true;
  }

  if (validateTurkish) {
    checkAndAddValidator('custNm1', turkish, [ 'Customer Name' ]);
    checkAndAddValidator('custNm2', turkish, [ 'Customer Name Con\'t' ]);
    checkAndAddValidator('custNm4', turkish, [ 'Name 4' ]);
    checkAndAddValidator('addrTxt', turkish, [ 'Street Address' ]);
    checkAndAddValidator('addrTxt2', turkish, [ 'Street Con\'t' ]);
    checkAndAddValidator('city1', turkish, [ 'City' ]);
    checkAndAddValidator('dept', turkish, [ 'District' ]);
    // checkAndAddValidator('postCd', turkish, [ 'Postal Code' ]);
    checkAndAddValidator('poBox', turkish, [ 'PO Box' ]);
    checkAndAddValidator('taxOffice', turkish, [ 'Tax Office' ]);
  } else {
    FormManager.removeValidator('custNm1', turkish);
    FormManager.removeValidator('custNm2', turkish);
    FormManager.removeValidator('custNm4', turkish);
    FormManager.removeValidator('addrTxt', turkish);
    FormManager.removeValidator('addrTxt2', turkish);
    FormManager.removeValidator('city1', turkish);
    FormManager.removeValidator('postCd', turkish);
    FormManager.removeValidator('dept', turkish);
    FormManager.removeValidator('poBox', turkish);
    FormManager.removeValidator('taxOffice', turkish);
  }
}

function setEconomicCode() {
  console.log('setEconomicCode=====');
  var requestId = FormManager.getActualValue('reqId');
  qParams = {
    REQ_ID : requestId,
  };
  var result = cmr.query('GET.ECONOMIC_CD_BY_REQID', qParams);
  var economicCdResult = result.ret1;
  var economicCd = FormManager.getActualValue('economicCd');
  if (economicCdResult == '') {
    FormManager.setValue('economicCd', '');
  } else {
    FormManager.getActualValue('economicCd');
  }
}

function defaultCapIndicator() {
  console.log('defaultCapIndicator=====');
  if ((FormManager.getActualValue('cmrIssuingCntry') == SysLoc.ISRAEL || FormManager.getActualValue('cmrIssuingCntry') == SysLoc.GREECE
      || FormManager.getActualValue('cmrIssuingCntry') == SysLoc.CYPRUS || FormManager.getActualValue('cmrIssuingCntry') == SysLoc.TURKEY)
      && FormManager.getActualValue('reqType') == 'C') {
    if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.TURKEY && FormManager.getField('capInd') == undefined) {
      return;
    }
    FormManager.getField('capInd').set('checked', true);
    FormManager.readOnly('capInd');
  }
}

function validateEMEACopy(addrType, arrayOfTargetTypes) {
  console.log('validateEMEACopy=====');
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

// Add individual function to prevent different requirement in future
function populateTranslationAddrWithSoldToDataTR() {
  console.log('populateTranslationAddrWithSoldToDataTR=====');
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

function clearAddrFieldsForTR() {
  console.log('clearAddrFieldsForTR=====');
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

function preFillTranslationAddrWithSoldToForTR(cntry, addressMode, saving) {
  console.log('preFillTranslationAddrWithSoldToForTR=====');
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

function addTRAddressTypeValidator() {
  console.log("addTRAddressTypeValidator=======");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('cmrIssuingCntry') != SysLoc.TURKEY) {
          return new ValidationResult(null, true);
        }
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount == 0) {
          return new ValidationResult(null, false, 'Sold-To/Ship-To/Install-At/Local Language Translation of Sold-To are mandatory.');
        }
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          var record = null;
          var type = null;
          var updateInd = null;
          var zs01Cnt = 0;
          var zp01Cnt = 0;
          var zd01Cnt = 0;
          var zi01Cnt = 0;
          var zs01Copy;
          var zp01Copy;
          var zs01Upd = false;
          var custType = FormManager.getActualValue('custGrp');
          var compareFields = [ 'custNm1', 'custNm2', 'addrTxt', 'addrTxt2', 'city1', 'stateProv', 'postCd', 'dept', 'poBox', 'landCntry' ];
          var compareFieldsLocal = [ 'custNm1', 'custNm2', 'addrTxt', 'addrTxt2', 'city1', 'stateProv', 'postCd', 'dept', 'poBox', 'taxOffice' ];
          var compareFieldsNonLocal = [ 'custNm1', 'custNm2', 'addrTxt', 'addrTxt2', 'city1', 'stateProv', 'postCd', 'dept', 'poBox' ];
          var compareFieldsValue = null;
          var enErrMsg = '';
          var turkishErrMsg = '';
          var reqType = cmr.currentRequestType;
          if (reqType == undefined) {
            reqType = FormManager.getActualValue('reqType');
          }
          if (reqType == 'U') {
            compareFields.push('custNm4');
          }
          for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
            record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
            if (record == null && _allAddressData != null && _allAddressData[i] != null) {
              record = _allAddressData[i];
            }
            type = record.addrType;
            updateInd = record.updateInd;
            if (typeof (type) == 'object') {
              type = type[0];
            }
            var addrTypeText = record.addrTypeText;
            if (typeof (addrTypeText) == 'object') {
              addrTypeText = addrTypeText[0];
            }
            if (typeof (updateInd) == 'object') {
              updateInd = updateInd[0];
            }
            // Valid english for all address types when
            // 1: All address types except ZP01
            // 2: For ZP01, create request => scenario is CROSS,
            // update request=> land country is not TR
            if (type != 'ZP01' || (type == 'ZP01' && (custType == 'CROSS' || (reqType == 'U' && record['landCntry'][0] != 'TR')))) {
              if (type == 'ZP01') {
                compareFieldsValue = compareFieldsLocal;
              } else {
                compareFieldsValue = compareFieldsNonLocal;
              }
              for (var j = 0; j < compareFieldsValue.length; j++) {
                var value = record[compareFieldsValue[j]];
                if (typeof (value) == 'object') {
                  value = value[0];
                }
                if ((value != null && value != undefined && value != '' && typeof (value) == 'string') && updateInd == 'U') {
                  var reg = /[^\u0000-\u007f]/;
                  if (reg.test(value)) {
                    enErrMsg += addrTypeText + ', ';
                    break;
                  }
                }
              }
            }

            if (type == 'ZS01') {
              zs01Cnt++;
              zs01Copy = record;
            } else if (type == 'ZP01') {
              zp01Cnt++;
              zp01Copy = record;
            } else if (type == 'ZD01') {
              zd01Cnt++;
            } else if (type == 'ZI01') {
              zi01Cnt++;
            }
          }

          if (enErrMsg != '') {
            enErrMsg = enErrMsg.substring(0, enErrMsg.lastIndexOf(','));
            enErrMsg += ' must be in English.';
            return new ValidationResult(null, false, enErrMsg);
          }

          if (zs01Cnt == 0 || zp01Cnt == 0 || zd01Cnt == 0 || zi01Cnt == 0) {
            return new ValidationResult(null, false, 'Sold-To/Ship-To/Install-At/Local Language Translation of Sold-To are mandatory.');
          } else if (zs01Cnt > 1) {
            return new ValidationResult(null, false, 'Only one Sold-To is allowed.');
          } else if (zp01Cnt > 1) {
            return new ValidationResult(null, false, 'Only one Local Language Translation of Sold-To is allowed.');
          }

          var mismatchErrMsg = '';
          for (var i = 0; i < compareFields.length; i++) {
            if (custType == 'CROSS' || (reqType == 'U' && zp01Copy['landCntry'][0] != 'TR')) {
              if (zs01Copy[compareFields[i]][0] != zp01Copy[compareFields[i]][0]) {
                mismatchErrMsg += mappingAddressField(compareFields[i]) + ', ';
              }
            } else if (custType == 'LOCAL' || (reqType == 'U' && zp01Copy['landCntry'][0] == 'TR')) {
              if ((zs01Copy[compareFields[i]] == '' || zs01Copy[compareFields[i]] == null || zs01Copy[compareFields[i]] == undefined)
                  && (zp01Copy[compareFields[i]] != '' && zp01Copy[compareFields[i]] != null && zp01Copy[compareFields[i]] != undefined)) {
                mismatchErrMsg += mappingAddressField(compareFields[i]) + ', ';
              }
              if ((zp01Copy[compareFields[i]] == '' || zp01Copy[compareFields[i]] == null || zp01Copy[compareFields[i]] == undefined)
                  && (zs01Copy[compareFields[i]] != '' && zs01Copy[compareFields[i]] != null && zs01Copy[compareFields[i]] != undefined)) {
                mismatchErrMsg += mappingAddressField(compareFields[i]) + ', ';
              }
            }
          }

          if (mismatchErrMsg != '') {
            mismatchErrMsg = mismatchErrMsg.substring(0, mismatchErrMsg.lastIndexOf(','));
            return new ValidationResult(null, false, 'Sold-to and Local Translation, mismatched fields: ' + mismatchErrMsg);
          }

          var zs01data = [];
          var zp01data = [];
          var zp01converted = [];
          for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
            record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
            if (record == null && _allAddressData != null && _allAddressData[i] != null) {
              record = _allAddressData[i];
            }
            type = record.addrType;
            updateInd = record.updateInd;
            if (typeof (type) == 'object') {
              type = type[0];
            }
            if (typeof (updateInd) == 'object') {
              updateInd = updateInd[0];
            }

            if (type == 'ZS01') {
              if (updateInd == 'U') {
                zs01Upd = true;
              }
              zs01data.push(_allAddressData[i].custNm1[0]);
              zs01data.push(_allAddressData[i].custNm4[0]);
              zs01data.push(_allAddressData[i].addrTxt[0]);
              zs01data.push(_allAddressData[i].city1[0]);
              zs01data.push(_allAddressData[i].dept[0]);
            }
            if (type == 'ZP01') {
              zp01data.push(_allAddressData[i].custNm1[0]);
              zp01data.push(_allAddressData[i].custNm4[0]);
              zp01data.push(_allAddressData[i].addrTxt[0]);
              zp01data.push(_allAddressData[i].city1[0]);
              zp01data.push(_allAddressData[i].dept[0]);
            }
          }

          for (var i = 0; i < zp01data.length; i++) {
            zp01converted.push(modifyCharForTurk(zp01data[i]));
          }

          if (zs01Upd == true) {
            var errorMessage = '';
            if (zs01data[0] != zp01converted[0]) {
              errorMessage = errorMessage + 'Customer Name';
            }
            if (zs01data[1] != zp01converted[1]) {
              errorMessage = errorMessage + (errorMessage.length > 0 ? ', Name 4' : 'Name 4');
            }
            if (zs01data[2] != zp01converted[2]) {
              errorMessage = errorMessage + (errorMessage.length > 0 ? ', Street' : 'Street');
            }
            if (zs01data[3] != zp01converted[3]) {
              errorMessage = errorMessage + (errorMessage.length > 0 ? ', City' : 'City');
            }
            if (zs01data[4] != zp01converted[4]) {
              errorMessage = errorMessage + (errorMessage.length > 0 ? ', District' : 'District');
            }
            if (errorMessage.length > 0) {
              return new ValidationResult(null, false, "Field value mismatch for Sold-To and Local Language Translation of Sold To: " + errorMessage);
            }
          }
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function mappingAddressField(key) {
  console.log("mappingAddressField=======");
  var value = '';
  if (key == 'custNm1') {
    value = 'Customer Name';
  } else if (key == 'custNm2') {
    value = 'Customer Name Con\'t';
  } else if (key == 'custNm4') {
    value = 'Name 4';
  } else if (key == 'addrTxt') {
    value = 'Street Address';
  } else if (key == 'addrTxt2') {
    value = 'Street Con\'t';
  } else if (key == 'city1') {
    value = 'City';
  } else if (key == 'stateProv') {
    value = 'State Province';
  } else if (key == 'postCd') {
    value = 'Postal Code';
  } else if (key == 'dept') {
    value = 'District';
  } else if (key == 'poBox') {
    value = 'PO Box';
  } else if (key == 'landCntry') {
    value = 'Country (Landed)';
  }
  return value;
}

var _gtcISUHandler = null;
var _CTCHandler = null;
var _gtcISRHandler = null;
var _gtcAddrTypes = [ 'ZS01', 'ZP01', 'ZD01', 'ZI01' ];
var _gtcAddrTypeHandler = [];
var _gtcVatExemptHandler = null;

function setVatValidatorGRCYTR(fromAddress, scenario, scenarioChanged) {
  console.log("setVatValidatorGRCYTR=======");
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');

  if (viewOnlyPage != 'true' && FormManager.getActualValue('reqType') == 'C') {
    FormManager.resetValidations('vat');
    if (scenarioChanged) {
      if (FormManager.getActualValue('custSubGrp') == 'PRICU' || FormManager.getActualValue('custSubGrp') == 'INTER' || FormManager.getActualValue('custSubGrp') == 'XPC'
          || FormManager.getActualValue('custSubGrp') == 'XINT' || FormManager.getActualValue('custSubGrp') == 'IBMEM') {
        FormManager.resetValidations('vat');
        FormManager.setValue('vatExempt', 'Y');
      } else {
        checkAndAddValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
        FormManager.setValue('vatExempt', false);
      }
    }

    if (undefined != dijit.byId('vatExempt') && !dijit.byId('vatExempt').get('checked')) {
      checkAndAddValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
      FormManager.setValue('vatExempt', false);
    }

    if (dijit.byId('vatExempt') != undefined && dijit.byId('vatExempt').get('checked')) {
      FormManager.clearValue('vat');
      FormManager.readOnly('vat');
    }
    if (undefined != dijit.byId('vatExempt') && !dijit.byId('vatExempt').get('checked')) {
      checkAndAddValidator('vat', Validators.REQUIRED, [ 'VAT' ]);
      FormManager.enable('vat');
    }
  }
}

function setClientTierAndISR(value) {
  console.log("setClientTierAndISR=======");
  var reqType = null;
  reqType = FormManager.getActualValue('reqType');
  if (reqType != 'C') {
    return;
  }
  /*
   * if (!PageManager.isReadOnly()) { FormManager.enable('clientTier'); }
   */
  if (!value) {
    value = FormManager.getActualValue('isuCd');
  }
  if (_pagemodel.isuCd != value) {
    // FormManager.setValue('clientTier', '');
  }
  tierValues = null;
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.TURKEY) {
    if (value == '34') {
      tierValues = [ 'Q' ];
    } else if (value == '27') {
      tierValues = [ 'E' ];
    } else if (value == '36') {
      tierValues = [ 'Y' ];
    } else if (value == '5K' || value == '21' || value == '8B' || value == '28' || value == '04') {
      tierValues = [ '' ];
    }
  }
  if (tierValues != null) {
    FormManager.limitDropdownValues(FormManager.getField('clientTier'), tierValues);
    if (tierValues.length == 1) {
      FormManager.setValue('clientTier', tierValues[0]);
    }
  } else {
    FormManager.resetDropdownValues(FormManager.getField('clientTier'));
  }
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.TURKEY) {
    console.log("skip set ISR.");
  } else {
    setISRValues();
  }

}

function setISRValues() {
  console.log("setISRValues=======");
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
        gy
        setEnterprise(isrs[0]);
        FormManager.setValue('repTeamMemberNo', isrs[0]);
      }
      // setSalesBoSboIbo();
    }
  }
}

function updateAddrTypeList(cntry, addressMode, saving) {
  console.log("updateAddrTypeList=======");
  if (!saving && FormManager.getActualValue('cmrIssuingCntry') != '862') {
    // hide 'additional shipping' selection for creates .
    if ((addressMode == 'newAddress' || addressMode == 'copyAddress') && cmr.currentRequestType == 'C') {
      cmr.hideNode('radiocont_ZD01');
    }
    // if (FormManager.getActualValue('cmrIssuingCntry') == "862") {
    // cmr.showNode('radiocont_ZD01');
    // }
  }
}

function setFieldsToReadOnlyGRCYTR() {
  console.log("setFieldsToReadOnlyGRCYTR=======");
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (role == 'REQUESTER') {
    FormManager.readOnly('abbrevNm');
    FormManager.readOnly('abbrevLocn');
  }
  FormManager.readOnly('salesTeamCd');
  FormManager.readOnly('subIndustryCd');
}

function updateAbbrevNmLocnGRCYTR(cntry, addressMode, saving, finalSave, force) {
  console.log("updateAbbrevNmLocnGRCYTR=======");
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
        if ("TR" != FormManager.getActualValue("landCntry")) {
          if (document.getElementById('landCntry') != undefined) {
            abbrevLocn = document.getElementById('landCntry').value;
          }
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
  console.log("addrFunctionForGRCYTR=======");
  if (!saving) {
    var cntryCd = FormManager.getActualValue('cmrIssuingCntry');
    var custType = FormManager.getActualValue('custGrp');

    if ((addressMode == 'updateAddress' || addressMode == 'copyAddress') && FormManager.getActualValue('landCntry') == '') {
      FormManager.setValue('landCntry', cmr.oldlandcntry);
    }
    // for cross border
    if (custType == 'CROSS' && cmr.currentRequestType == 'U') {
      FormManager.readOnly('landCntry');
    }
    // for Turkey - cross border
    setDistrictMandatoryTR();
    checkAndAddValidator('landCntry', Validators.REQUIRED, [ 'Country (Landed)' ]);
  }
}

function setDistrictMandatoryTR() {
  console.log("setDistrictMandatoryTR=======");
  var cntryCd = FormManager.getActualValue('cmrIssuingCntry');
  if (cntryCd == SysLoc.TURKEY) {
    var landCntry = FormManager.getActualValue('landCntry');
    if (landCntry == 'TR') {
      checkAndAddValidator('dept', Validators.REQUIRED, [ 'District' ]);
    } else {
      FormManager.removeValidator('dept', Validators.REQUIRED);
    }
  }
}

function isUpdateReqCrossborder() {
  console.log("isUpdateReqCrossborder=======");
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

function addPostalCodeLenForTurGreCypValidator() {
  console.log("addPostalCodeLenForTurGreCypValidator=======");
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

function abbrvLocMandatory() {
  console.log("abbrvLocMandatory=======");
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
  console.log("abbrvLocMandatoryOnChange=======");
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
  console.log("setCommonCollectionCd=======");
  if (FormManager.getActualValue('reqType') == 'C') {
    FormManager.readOnly('collectionCd2');
    FormManager.setValue('collectionCd2', '');
  }
}

function showClassificationForTRUpd() {
  console.log("showClassificationForTRUpd=======");
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.TURKEY) {
    if (FormManager.getActualValue('reqType') == 'U') {
      FormManager.show('CustClass', 'custClass');
      var role = FormManager.getActualValue('userRole').toUpperCase();
      if (role != 'REQUESTER') {
        FormManager.enable('CustClass');
        FormManager.addValidator('custClass', Validators.REQUIRED, [ 'Classification Code' ], 'MAIN_CUST_TAB');
      }
    }
  }
}

function viewOnlyAddressDetails() {
  console.log("viewOnlyAddressDetails=======");
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');

  if (viewOnlyPage == 'true' && (cmrIssuingCntry == SysLoc.CYPRUS)) {
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

function salesSRforUpdate() {
  console.log("salesSRforUpdate=======");
  if (FormManager.getActualValue('reqType') == 'U') {
    // setSalesBoSboIbo();
  }
}

function salesSRforUpdateOnChange() {
  console.log("salesSRforUpdateOnChange=======");
  if (FormManager.getActualValue('reqType') == 'U') {
    dojo.connect(FormManager.getField('repTeamMemberNo'), 'onChange', function(value) {
      // setSalesBoSboIbo();
    });
  }

  setClassificationCodeTR();
  dojo.connect(FormManager.getField('commercialFinanced'), 'onChange', function(value) {
    setCOFClassificationCodeTR();
  });

  dojo.connect(FormManager.getField('crosSubTyp'), 'onChange', function(value) {
    setTypeOfCustomerClassificationCodeTR();
  });

  dojo.connect(FormManager.getField('isicCd'), 'onChange', function(value) {
    setIsicClassificationCodeTR(value);
    setValuesForTurkey();
  });
}

function modifyCharForTurk(field) {
  console.log("modifyCharForTurk=======");

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

// CMR-2205
function autoSetAbbrevNmOnChanageTR() {
  console.log("--->>> autoSetAbbrevNmOnChanageTR >> running");
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (role != 'REQUESTER') {
    return;
  }
  var abbrName = FormManager.getActualValue("abbrevNm");
  if (abbrName != null && abbrName.length > 22) {
    abbrName = abbrName.substr(0, 22);
  }
  if (abbrName == undefined) {
    FormManager.setValue('abbrevNm', '');
  } else {
    FormManager.setValue('abbrevNm', abbrName);
  }
  console.log("AbbrevNM>>" + FormManager.getActualValue('abbrevNm'));
}

function autoSetAbbrevLocnOnChangeTR() {
  console.log("autoSetAbbrevLocnOnChangeTR=======");
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (role != 'REQUESTER') {
    return;
  }

  var _abbrevLocn = FormManager.getActualValue('abbrevLocn');
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

function updateAbbrLocWithZS01TR() {
  console.log("updateAbbrLocWithZS01TR=======");
  var _abbrevLocn = null;
  var addrType = FormManager.getActualValue('addrType');
  var reqType = FormManager.getActualValue('reqType');
  var isCross = true;
  if ("TR" == FormManager.getActualValue("landCntry")) {
    isCross = false;
  }
  if (("ZS01" != addrType && "ZP01" != addrType) || (!isCross && "ZP01" == addrType)) {
    return;
  }
  var _zs01ReqId = FormManager.getActualValue('reqId');
  var newAddrCity = FormManager.getActualValue("city1");
  var newAddrLand = FormManager.getActualValue("landCntry");
  var qParams = {
    REQ_ID : _zs01ReqId,
    ADDR_TYPE : "ZS01",
  };
  var _result = cmr.query('ADDR.GET.CITY1.BY_REQID_ADDRTYP', qParams);
  var oldAddrCity = _result.ret1;

  if (isCross) {
    _abbrevLocn = document.getElementById('landCntry').value;
    if (_abbrevLocn != null && _abbrevLocn.length > 12) {
      _abbrevLocn = _abbrevLocn.substr(0, 12);
    }
    FormManager.setValue('abbrevLocn', _abbrevLocn);
  } else {
    if (newAddrCity != oldAddrCity) {
      if (newAddrCity != null && newAddrCity.length > 12) {
        newAddrCity = newAddrCity.substr(0, 12);
      }
      if (newAddrCity == undefined) {
        FormManager.setValue('abbrevLocn', '');
      } else {
        FormManager.setValue('abbrevLocn', newAddrCity);
      }
    }
  }
}

function updateAbbrNameWithZS01TR() {
  console.log("updateAbbrNameWithZS01TR=======");
  var reqType = FormManager.getActualValue('reqType');
  var addrType = FormManager.getActualValue('addrType');
  var abbrName = FormManager.getActualValue("abbrevNm");
  var newAddrName1 = FormManager.getActualValue("custNm1");
  if ("ZS01" != addrType) {
    return;
  }
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  if ('PROCESSOR' == role) {
    var zs01ReqId = FormManager.getActualValue('reqId');
    var qParams = {
      REQ_ID : zs01ReqId,
    };
    var result = cmr.query('DATA.GET.ABBREV_NM.BY_REQID', qParams);
    var oldAbbrName = result.ret1;

    if (abbrName != oldAbbrName) {
      return;
    }
  }

  var zs01ReqId = FormManager.getActualValue('reqId');
  var qParams = {
    REQ_ID : zs01ReqId,
    ADDR_TYPE : "ZS01",
  };
  var oldAddrName = null;
  var result = cmr.query('ADDR.GET.CUSTNM1.BY_REQID_ADDRTYP', qParams);
  oldAddrName = result.ret1;

  if (oldAddrName != newAddrName1) {
    if (newAddrName1 != null && newAddrName1.length > 22) {
      newAddrName1 = newAddrName1.substr(0, 22);
    }
    if (newAddrName1 == undefined) {
      FormManager.setValue('abbrevNm', '');
    } else {
      FormManager.setValue('abbrevNm', newAddrName1);
    }
  }

}

/**
 * CMR-2093:Turkey - show CoF field for Update request only
 */
function showCommercialFinanced() {
  console.log("showCommercialFinanced=======");
  if (FormManager.getActualValue('reqType') != 'U' || FormManager.getActualValue('cmrIssuingCntry') != SysLoc.TURKEY) {
    FormManager.clearValue('commercialFinanced');
    FormManager.hide('CommercialFinanced', 'commercialFinanced');
  } else {
    FormManager.show('CommercialFinanced', 'commercialFinanced');
    var role = FormManager.getActualValue('userRole').toUpperCase();
    if (role == 'REQUESTER') {
    } else {
      FormManager.enable('custClass');
    }
  }
}

function lockCmrOwner() {
  console.log("lockCmrOwner=======");
  showCommercialFinanced
  FormManager.setValue('cmrOwner', 'IBM');
  FormManager.readOnly('cmrOwner');
}

function addPhoneValidatorEMEA() {
  console.log("addPhoneValidatorEMEA=======");
  if (FormManager.getActualValue('addrType') == 'ZS01') {
    FormManager.addValidator('custPhone', Validators.DIGIT, [ 'Phone #' ]);
  }
}

function validateCMRNumExistForTR() {
  console.log("validateCMRNumExistForTR=======");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        console.log('checking requested cmr number...');
        var reqType = FormManager.getActualValue('reqType');
        var cmrNo = FormManager.getActualValue('cmrNo');
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        if (reqType == 'C' && cmrNo) {
          var exists = cmr.query('GETCMRNUMFORPROCESSOR', {
            CNTRY : cntry,
            CMR_NO : cmrNo,
            MANDT : cmr.MANDT
          });
          if (exists && exists.ret1) {
            return new ValidationResult({
              id : 'cmrNo',
              type : 'text',
              name : 'cmrNo'
            }, false, 'The requested CMR Number ' + cmrNo + ' already exists in the system.');
          }
        }
        return new ValidationResult({
          id : 'cmrNo',
          type : 'text',
          name : 'cmrNo'
        }, true);
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function enableCMRNUMForPROCESSOR() {
  console.log("enableCMRNUMForPROCESSOR=======");
  var role = FormManager.getActualValue('userRole').toUpperCase();

  if ('C' == FormManager.getActualValue('reqType')) {
    if (role == "PROCESSOR") {
      FormManager.enable('cmrNo');
    } else {
      FormManager.readOnly('cmrNo');
    }
  }
}

function autoPopulateAbbNameCopySoldToTR() {
  console.log("autoPopulateAbbNameCopySoldToTR=======");
  var reqType = FormManager.getActualValue('reqType');
  if (cmr.currentRequestType != 'C') {
    return;
  }
  var custGroup = FormManager.getActualValue('custGrp');

  if (custGroup != undefined && (custGroup == 'CROSS' || "TR" != FormManager.getActualValue("landCntry"))) {

    var addrType = FormManager.getActualValue('addrType');
    var abbName = FormManager.getActualValue('custNm1');

    console.log('addrType = ' + addrType);
    console.log('abbName = ' + abbName);
    if (addrType != '' && addrType == 'ZP01') {

      FormManager.setValue('abbrevNm', abbName);
    }
  }
}

function toggleBPRelMemTypeForTurkey() {
  console.log("toggleBPRelMemTypeForTurkey=======");
  var reqType = null;
  if (typeof (_pagemodel) != 'undefined') {
    reqType = FormManager.getActualValue('reqType');
  }
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (reqType == 'U') {
  } else {
    var _custType = FormManager.getActualValue('custSubGrp');
    if (_custType == 'BUSPR' || _custType == 'XBP') {
      FormManager.show('PPSCEID', 'ppsceid');
      FormManager.resetValidations('ppsceid');
      FormManager.addValidator('ppsceid', Validators.REQUIRED, [ 'PPS CEID' ], 'MAIN_IBM_TAB');
    } else {
      FormManager.hide('PPSCEID', 'ppsceid');
      FormManager.removeValidator('ppsceid', Validators.REQUIRED);
    }
  }
}

function toggleTypeOfCustomerForTR() {
  console.log("toggleTypeOfCustomerForTR=======");
  var reqType = null;
  if (typeof (_pagemodel) != 'undefined') {
    reqType = FormManager.getActualValue('reqType');
  }
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (reqType == 'U') {
    FormManager.show('TypeOfCustomer', 'crosSubTyp');
  } else {
    FormManager.hide('TypeOfCustomer', 'crosSubTyp');
  }
  // Lock type of customer and classification code when update requester
  if (reqType == 'U' && role == 'REQUESTER') {
    FormManager.readOnly('crosSubTyp');
    FormManager.readOnly('custClass');
  }
}

function setIsuCtcCBCewaSa() {
  var landCntry = '';
  var reqId = FormManager.getActualValue('reqId');
  // GET LANDCNTRY in case of CB
  var result = cmr.query('LANDCNTRY.IT', {
    REQID : reqId
  });
  if (result != null && result.ret1 != undefined) {
    landCntry = result.ret1;
  }
  var reqId = FormManager.getActualValue('reqId');
  var custSubType = FormManager.getActualValue('custSubGrp');
  var landCntry = '';
  var result = cmr.query('LANDCNTRY.IT', {
    REQID : reqId
  });
  if (result != null && result.ret1 != undefined) {
    landCntry = result.ret1;
  }
  var crossSubTypes = [ 'XINTS', 'XGOV', 'XIGF', 'XPC', 'XTP' ];
  var SA_COUNTRIES_CB = [ 'ZA', 'NA', 'LS', 'SZ' ];
  if (reqType == 'U' || !SA_COUNTRIES_CB.includes(landCntry) || !crossSubTypes.includes(custSubType)) {
    return;
  }
  var subIndustryCd = FormManager.getActualValue('subIndustryCd');
  if (subIndustryCd == '') {
    subIndustryCd = _pagemodel.subIndustryCd;
  }
  var SA34Q = [ 'PK', 'AF', 'MA', 'TN', 'LY', 'QA', 'EG' ];
  var SaSubIndustry34Q = [ 'E', 'G', 'V', 'Y', 'H', 'X' ];
  if (MEA34Q.includes(landCntry) || (landCntry == 'SA' && SaSubIndustry34Q.includes(subIndustryCd))) {
    // CEWA MEA REGION
    FormManager.setValue('isuCd', '34');
    FormManager.setValue('clientTier', 'Q');
  } else {
    FormManager.setValue('isuCd', '27');
    FormManager.setValue('clientTier', 'E');
  }
}

function setIsuCtcCBMEA() {
  var landCntry = '';
  var reqId = FormManager.getActualValue('reqId');
  // GET LANDCNTRY in case of CB
  var result = cmr.query('LANDCNTRY.IT', {
    REQID : reqId
  });
  if (result != null && result.ret1 != undefined) {
    landCntry = result.ret1;
  }
  var reqId = FormManager.getActualValue('reqId');
  var custSubType = FormManager.getActualValue('custSubGrp');
  var landCntry = '';
  var result = cmr.query('LANDCNTRY.IT', {
    REQID : reqId
  });
  if (result != null && result.ret1 != undefined) {
    landCntry = result.ret1;
  }
  var crossSubTypes = [ 'XINTS', 'XGOV', 'XIGF', 'XPC', 'XTP' ];
  var MEA_COUNTRIES_CB = [ 'TR', 'DZ', 'TN', 'LY', 'AO', 'BW', 'CV', 'CD', 'MG', 'MW', 'MU', 'MZ', 'ST', 'SC', 'ZM', 'ZW', 'GH', 'LR', 'NG', 'SL', 'BI', 'ER', 'ET', 'DJ', 'KE', 'RW', 'SO', 'SD',
      'TZ', 'UG', 'BJ', 'BF', 'CM', 'CF', 'TD', 'CG', 'GQ', 'GA', 'GM', 'GN', 'GW', 'CI', 'ML', 'MR', 'NE', 'SN', 'TG', 'LY', 'TN', 'MA', 'PK', 'AF', 'EG', 'BH', 'AE', 'AE', 'IQ', 'JO', 'PS', 'KW',
      'LB', 'OM', 'QA', 'SA', 'YE', 'SY' ];
  if (reqType == 'U' || !MEA_COUNTRIES_CB.includes(landCntry) || !crossSubTypes.includes(custSubType)) {
    return;
  }
  var subIndustryCd = FormManager.getActualValue('subIndustryCd');
  if (subIndustryCd == '') {
    subIndustryCd = _pagemodel.subIndustryCd;
  }
  var MEA34Q = [ 'PK', 'AF', 'MA', 'TN', 'LY', 'QA', 'EG' ];
  var MEAsubIndustry34Q = [ 'E', 'G', 'V', 'Y', 'H', 'X' ];
  if (MEA34Q.includes(landCntry) || (landCntry == 'SA' && MEAsubIndustry34Q.includes(subIndustryCd))) {
    // CEWA MEA REGION
    FormManager.setValue('isuCd', '34');
    FormManager.setValue('clientTier', 'Q');
  } else {
    FormManager.setValue('isuCd', '27');
    FormManager.setValue('clientTier', 'E');
  }
}

function setDefaultEntCBMEA() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var reqType = FormManager.getActualValue('reqType');
  var custGrp = FormManager.getActualValue('custGrp');
  var custSubType = FormManager.getActualValue('custSubGrp');
  var reqId = FormManager.getActualValue('reqId');
  var subRegion = FormManager.getActualValue('countryUse');
  var subIndustryCd = FormManager.getActualValue('subIndustryCd');
  if (subIndustryCd == '') {
    subIndustryCd = _pagemodel.subIndustryCd;
  }
  if (subIndustryCd != null && subIndustryCd.length > 1) {
    subIndustryCd = subIndustryCd.substring(0, 1);
  }
  var crossSubTypes = [ 'XINTS', 'XGOV', 'XIGF', 'XPC', 'XTP' ];
  var privScenarios = [ 'ZAXPC', 'SZXPC', 'NAXPC', 'LSXPC' ];
  var ME_LC = [ 'LY', 'TN', 'MA', 'PK', 'AF', 'EG', 'BH', 'AE', 'AE', 'IQ', 'JO', 'PS', 'KW', 'LB', 'OM', 'QA', 'SA', 'YE', 'SY' ];

  var landCntry = '';
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  // GET LANDCNTRY in case of CB
  var result = cmr.query('LANDCNTRY.IT', {
    REQID : reqId
  });
  if (result != null && result.ret1 != undefined) {
    landCntry = result.ret1;
  }

  if (reqType != 'C' || FormManager.getActualValue('viewOnlyPage') == 'true' || !crossSubTypes.includes(custSubType) || landCntry == '' || !(ME_LC.includes(landCntry))) {
    return;
  }

  var isu = FormManager.getActualValue('isuCd');
  var ctc = FormManager.getActualValue('clientTier');
  var isuCtc = isu.concat(ctc);

  if (crossSubTypes.includes(custSubType)) {
    if (ME_LC.includes(landCntry)) {
      var noSubCountriesME = [ 'KW', 'OM', 'IQ', 'SY', 'YE', 'JO', 'PS', 'LB', 'BH', 'LY', 'TN', 'MA', 'PK', 'AF' ];
      var enterprise = '';
      if (noSubCountriesME.includes(landCntry)) {
        var qParams = {
          ISSUING_CNTRY : cntry,
          SALES_BO_DESC : '%' + landCntry + '%',
          ISU_CD : '%' + isuCtc + '%'
        };
        var results = cmr.query('GET.ENTERPRISEVALUE.BYLANDCNTRY', qParams);
        enterprise = results.ret1;
      } else {
        // ME MEA REGION
        var qParams = {
          ISSUING_CNTRY : cntry,
          SALES_BO_CD : '%' + subIndustryCd + '%',
          SALES_BO_DESC : '%' + landCntry + '%',
          ISU_CD : '%' + isuCtc + '%'
        };
        var results = cmr.query('GET.ENTERPRISEVALUE.BYSUBINDUSTRY', qParams);
        enterprise = results.ret1;
      }
      FormManager.setValue('enterprise', enterprise);
    }
  }
}

function enterpriseValidatorMea() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqType = FormManager.getActualValue('reqType');
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        var enterprise = FormManager.getActualValue('enterprise');
        var countryUse = FormManager.getActualValue('countryUse');
        var isuCode = FormManager.getActualValue('isuCd');
        var clientTierCode = FormManager.getActualValue('clientTier');
        var reqId = FormManager.getActualValue('reqId');
        var isuCtc = isuCode + clientTierCode;
        // var isuCtcWithBlank = ['04', '12', '28', '4F', '5K'];
        var subIndustryCd = FormManager.getActualValue('subIndustryCd');
        if (subIndustryCd == '') {
          subIndustryCd = _pagemodel.subIndustryCd;
        }
        if (subIndustryCd != null && subIndustryCd.length > 1) {
          subIndustryCd = subIndustryCd.substring(0, 1);
        }
        var crossSubTypes = [ 'XINTS', 'XGOV', 'XIGF', 'XPC', 'XTP' ];
        var privScenarios = [ 'ZAXPC', 'SZXPC', 'NAXPC', 'LSXPC' ];
        var ME_LC = [ 'LY', 'TN', 'MA', 'PK', 'AF', 'EG', 'BH', 'AE', 'AE', 'IQ', 'JO', 'PS', 'KW', 'LB', 'OM', 'QA', 'SA', 'YE', 'SY' ];

        var landCntry = '';
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        // GET LANDCNTRY in case of CB
        var result = cmr.query('LANDCNTRY.IT', {
          REQID : reqId
        });
        if (result != null && result.ret1 != undefined) {
          landCntry = result.ret1;
        }
        if (reqType != 'C' || FormManager.getActualValue('viewOnlyPage') == 'true' || !crossSubTypes.includes(custSubGrp) || landCntry == '' || enterprise == '' || !(ME_LC.includes(landCntry))) {
          return;
        }
        if (ME_LC.includes(landCntry)) {
          return enterpriseMEValidator(landCntry, isuCtc, subIndustryCd, enterprise);
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function enterpriseMEValidator(landCntry, isuCtc, subIndustryCd, enterprise) {
  // ME MEA REGION
  if (landCntry == 'AE') {
    if (isuCtc == '27E') {
      if ([ 'E', 'G', 'H', 'X', 'Y' ].includes(subIndustryCd) && enterprise != '911812') {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : 911812');
      } else if ([ 'F', 'N', 'S' ].includes(subIndustryCd) && enterprise != '911813') {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : 911813');
      } else if ([ 'A', 'D', 'K', 'R', 'T', 'U', 'W' ].includes(subIndustryCd) && enterprise != '911814') {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : 911814');
      } else if ([ 'J', 'L', 'M', 'V', 'P' ].includes(subIndustryCd) && enterprise != '911815') {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : 911815');
      } else if ([ 'B', 'C' ].includes(subIndustryCd) && enterprise != '911816') {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : 911816');
      }
    } else if (isuCtc == '34Q') {
      var enterprise34Q = [ '911811', '911810' ];
      if (!enterprise34Q.includes(enterprise)) {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : \'911811\'\ \'911810\'\ ');
      }
    } else if (isuCtc == '36Y') {
      var enterprise34Q = [ '912073', '912075', '912074' ];
      if (!enterprise34Q.includes(enterprise)) {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : \'912073\'\ \'912075\'\ \'912074\'\ ');
      }
    }
  } else if (landCntry == 'ZA') {
    if (isuCtc == '27E') {
      if ([ 'A', 'K' ].includes(subIndustryCd) && enterprise != '911693') {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : 911693');
      } else if ([ 'B', 'C' ].includes(subIndustryCd) && enterprise != '911700') {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : 911700');
      } else if ([ 'D', 'L', 'R', 'W' ].includes(subIndustryCd) && enterprise != '911699') {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : 911699');
      } else if ([ 'F', 'S' ].includes(subIndustryCd) && enterprise != '911688') {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : 911688');
      } else if ([ 'J', 'T' ].includes(subIndustryCd) && enterprise != '911701') {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : 911701');
      } else if ([ 'M', 'P', 'U' ].includes(subIndustryCd) && enterprise != '901469') {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : 901469');
      } else if ([ 'N' ].includes(subIndustryCd) && enterprise != '911698') {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : 911698');
      }
    } else if (isuCtc == '34Q') {

      var enterprise34Q = [ '911690', '911685', '911691', '911702', '911687', '911697' ];
      if ([ 'E' ].includes(subIndustryCd) && !(enterprise == '911695' || enterprise34Q.includes(enterprise))) {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : \'911695\'\ \'911690\'\ \'911685\'\ \'911691\'\ \'911702\'\ \'911687\'\ \'911797\'\ ');
      }
      if ([ 'G', 'V', 'Y' ].includes(subIndustryCd) && !(enterprise == '911692' || enterprise34Q.includes(enterprise))) {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : \'911692\'\ \'911690\'\ \'911685\'\ \'911691\'\ \'911702\'\ \'911687\'\ \'911797\'\ ');
      } else if ([ 'H', 'X' ].includes(subIndustryCd) && !(enterprise == '911696' || enterprise34Q.includes(enterprise))) {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : \'911696\'\ \'911690\'\ \'911685\'\ \'911691\'\ \'911702\'\ \'911687\'\ \'911797\'\ ');
      } else if (!enterprise34Q.includes(enterprise) && !([ 'E', 'G', 'V', 'Y', 'H', 'X' ].includes(subIndustryCd))) {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : \'911690\'\ \'911685\'\ \'911691\'\ \'911702\'\ \'911687\'\ \'911797\'\ ');
      }
    } else if (isuCtc == '36Y') {
      var enterprise36Y = [ '908025', '912094', '912093' ];
      if (!enterprise36Y.includes(enterprise)) {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : \'908025\'\ \'912094\'\ \'912093\'\ ');
      }
    }
  } else if (landCntry == 'KW') {
    if (isuCtc == '27E') {
      if (!(enterprise == '907695' || enterprise == '911297')) {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : \'907695\'\ \'911297\'\ ');
      }
    } else if (isuCtc == '36Y') {
      var enterprise36Y = [ '912087', '912085', '912086' ];
      if (!enterprise36Y.includes(enterprise)) {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : \'912085\'\ \'912086\'\ \'912087\'\ ');
      }
    }
  } else if (landCntry == 'OM') {
    if (isuCtc == '27E') {
      if (!(enterprise == '911824' || enterprise == '911823')) {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : \'911824\'\ \'911823\'\ ');
      }
    } else if (isuCtc == '36Y') {
      var enterprise36Y = [ '912080', '912079', '912081' ];
      if (!enterprise36Y.includes(enterprise)) {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : \'912080\'\ \'912079\'\ \'912081\'\ ');
      }
    }
  } else if (landCntry == 'IQ' || landCntry == 'SY' || landCntry == 'YE' || landCntry == 'JO' || landCntry == 'PS' || landCntry == 'LB') {
    if (isuCtc == '27E') {
      if (!(enterprise == '911827' || enterprise == '911826')) {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : \'911827\'\ \'911826\'\ ');
      }
    } else if (isuCtc == '36Y') {
      var enterprise36Y = [ '908024', '912072', '912071' ];
      if (!enterprise36Y.includes(enterprise)) {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : \'908024\'\ \'912072\'\ \'912071\'\ ');
      }
    }
  } else if (landCntry == 'BH') {
    if (isuCtc == '27E') {
      if (!(enterprise == '911825')) {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : \'911825\'\  ');
      }
    } else if (isuCtc == '36Y') {
      var enterprise36Y = [ '912084', '912082', '912083' ];
      if (!enterprise36Y.includes(enterprise)) {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : \'912084\'\ \'912082\'\ \'912083\'\ ');
      }
    }
  } else if (landCntry == 'LY' || landCntry == 'TN' || landCntry == 'MA') {
    if (isuCtc == '34Q') {
      if (enterprise != '911756' && landCntry == 'LY') {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : \'911756\'\  ');
      } else if (enterprise != '901728' && landCntry == 'TN') {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : \'901728\'\  ');
      }
      if (enterprise != '901462' && landCntry == 'MA') {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : \'901462\'\  ');
      }
    } else if (isuCtc == '36Y') {
      var enterprise36Y = [ 'BUILD1', 'DISTR1', 'SRVCE1' ];
      if (!enterprise36Y.includes(enterprise)) {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : \'BUILD1\'\ \'DISTR1\'\ \'SRVCE1\'\ ');
      }
    }
  } else if (landCntry == 'PK' || landCntry == 'AF') {
    if (isuCtc == '34Q') {
      if (enterprise != '901459') {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : \'901459\'\  ');
      }
    } else if (isuCtc == '36Y') {
      var enterprise36Y = [ '908027', '912092', '912091' ];
      if (!enterprise36Y.includes(enterprise)) {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : \'908027\'\ \'912092\'\ \'912091\'\ ');
      }
    }
  } else if (landCntry == 'QA') {
    if (isuCtc == '34Q') {
      if ([ 'A', 'D', 'K', 'R', 'T', 'U', 'W' ].includes(subIndustryCd) && !(enterprise == '911818' || enterprise == '911817')) {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : \'911818\'\ \'911817\'\ ');
      } else if ([ 'J', 'L', 'M', 'P', 'V' ].includes(subIndustryCd) && !(enterprise == '911819' || enterprise == '911817')) {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : \'911819\'\ \'911817\'\ ');
      } else if ([ 'F', 'N', 'S' ].includes(subIndustryCd) && !(enterprise == '911820' || enterprise == '911817')) {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : \'911820\'\ \'911817\'\ ');
      } else if ([ 'E', 'G', 'H', 'X', 'Y' ].includes(subIndustryCd) && !(enterprise == '911821' || enterprise == '911817')) {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept :  \'911821\'\ \'911817\'\ ');
      } else if ([ 'B', 'C' ].includes(subIndustryCd) && !(enterprise == '911822' || enterprise == '911817')) {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : \'911822\'\ \'911817\'\ ');
      } else {
        var imsList = [ 'A', 'D', 'K', 'R', 'T', 'U', 'W', 'J', 'L', 'M', 'P', 'V', 'F', 'N', 'S', 'E', 'G', 'H', 'X', 'Y', 'B', 'C' ]
        if (enterprise != '911817' && !imsList.includes(subIndustryCd)) {
          return new ValidationResult({
            id : 'enterprise',
            type : 'text',
            name : 'enterprise'
          }, false, 'Enterprise can only accept : \'911817\'\ ');
        }
      }
    } else if (isuCtc == '36Y') {
      var enterprise34Q = [ '912088', '912090', '912089' ];
      if (!enterprise34Q.includes(enterprise)) {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : \'912088\'\ \'912090\'\ \'912089\'\ ');
      }
    }
  }
  if (landCntry == 'EG') {
    if (isuCtc == '34Q') {
      var enterprise34Q = [ '912088', '911831', '911835', '911275', '911833' ];
      if ([ 'D', 'J', 'K', 'L', 'R', 'T', 'W' ].includes(subIndustryCd) && !(enterprise == '911836' || enterprise34Q.includes(enterprise))) {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : \'911836\'\ \'912088\'\ \'911831\'\ \'911835\'\ \'911275\'\ \'911833\'\ ');
      } else if ([ 'A' ].includes(subIndustryCd) && !(enterprise == '911771' || enterprise34Q.includes(enterprise))) {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : \'911771\'\ \'912088\'\ \'911831\'\ \'911835\'\ \'911275\'\ \'911833\'\ ');
      } else if ([ 'B', 'C' ].includes(subIndustryCd) && !(enterprise == '911772' || enterprise34Q.includes(enterprise))) {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : \'911820\'\ \'912088\'\ \'911831\'\ \'911835\'\ \'911275\'\ \'911833\'\ ');
      } else if ([ 'E' ].includes(subIndustryCd) && !(enterprise == '911773' || enterprise34Q.includes(enterprise))) {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : \'911773\'\ \'912088\'\ \'911831\'\ \'911835\'\ \'911275\'\ \'911833\'\ ');
      } else if ([ 'F' ].includes(subIndustryCd) && !(enterprise == '911830' || enterprise34Q.includes(enterprise))) {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : \'911830\'\ \'912088\'\ \'911831\'\ \'911835\'\ \'911275\'\ \'911833\'\ ');
      } else if ([ 'G', 'V', 'Y' ].includes(subIndustryCd) && !(enterprise == '911832' || enterprise34Q.includes(enterprise))) {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : \'911832\'\ \'912088\'\ \'911831\'\ \'911835\'\ \'911275\'\ \'911833\'\ ');
      } else if ([ 'H', 'X' ].includes(subIndustryCd) && !(enterprise == '911768' || enterprise34Q.includes(enterprise))) {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : \'911768\'\ \'912088\'\ \'911831\'\ \'911835\'\ \'911275\'\ \'911833\'\ ');
      } else if ([ 'M', 'U' ].includes(subIndustryCd) && !(enterprise == '901456' || enterprise34Q.includes(enterprise))) {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : \'901456\'\ \'912088\'\ \'911831\'\ \'911835\'\ \'911275\'\ \'911833\'\ ');
      } else if (subIndustryCd == 'N' && !(enterprise == '911770' || enterprise34Q.includes(enterprise))) {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : \'911770\'\ \'912088\'\ \'911831\'\ \'911835\'\ \'911275\'\ \'911833\'\ ');
      } else if (subIndustryCd == 'P' && !(enterprise == '911834' || enterprise34Q.includes(enterprise))) {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : \'911834\'\  \'912088\'\ \'911831\'\ \'911835\'\ \'911275\'\ \'911833\'\ ');
      } else if (subIndustryCd == 'S' && !(enterprise == '911769' || enterprise34Q.includes(enterprise))) {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : \'911769\'\ \'912088\'\ \'911831\'\ \'911835\'\ \'911275\'\ \'911833\'\ ');
      } else {
        // var enterprise34Q = ['912088', '911831', '911835', '911275',
        // '911833'];
        if (!enterprise34Q.includes(enterprise) && !([ 'A', 'B', 'C', 'D', 'J', 'K', 'L', 'R', 'T', 'W', 'E', 'F', 'G', 'V', 'Y', 'H', 'X', 'M', 'U', 'N', 'P', 'S' ].includes(subIndustryCd))) {
          return new ValidationResult({
            id : 'enterprise',
            type : 'text',
            name : 'enterprise'
          }, false, 'Enterprise can only accept : \'912088\'\ \'911831\'\ \'911835\'\ \'911275\'\ \'911833\'\ ');
        }
      }
    } else if (isuCtc == '36Y') {
      var enterprise34Q = [ '908023', '912070', '912069' ];
      if (!enterprise34Q.includes(enterprise)) {
        return new ValidationResult({
          id : 'enterprise',
          type : 'text',
          name : 'enterprise'
        }, false, 'Enterprise can only accept : \'908023\'\ \'912070\'\ \'912069\'\ ');
      }
    }
  }
  if (isuCtc == '5k') {
    if (enterprise != '') {
      return new ValidationResult({
        id : 'enterprise',
        type : 'text',
        name : 'enterprise'
      }, false, 'Enterprise can only accept Blank ');
    }
  }
}

function validateCMRNumberForTR() {
  console.log("validateCMRNumberForTR=======");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var cmrNo = FormManager.getActualValue('cmrNo');
        var _custSubGrp = FormManager.getActualValue('custSubGrp');

        var numPattern = /^[0-9]+$/;
        if (FormManager.getActualValue('reqType') != 'C') {
          return new ValidationResult(null, true);
        }
        if (cmrNo == '') {
          return new ValidationResult(null, true);
        } else {
          // prod issue: skip validation for prospect request
          var ifProspect = FormManager.getActualValue('prospLegalInd');
          if (dijit.byId('prospLegalInd')) {
            ifProspect = dijit.byId('prospLegalInd').get('checked') ? 'Y' : 'N';
          }
          console.log("validateCMRNumberForTR ifProspect:" + ifProspect);
          if ('Y' == ifProspect) {
            return new ValidationResult(null, true);
          }
          if (_custSubGrp == 'INTER' || _custSubGrp == 'INTSO' || _custSubGrp == 'XINT' || _custSubGrp == 'XISO') {
            if (!cmrNo.startsWith("99")) {
              return new ValidationResult(null, false, 'Internal CMR should begin with 99.');
            }
          }
          if (cmrNo.length >= 1 && cmrNo.length != 6) {
            return new ValidationResult(null, false, 'CMR Number should be 6 digit long.');
          }
          if (cmrNo.length > 1 && !cmrNo.match(numPattern)) {
            return new ValidationResult({
              id : 'cmrNo',
              type : 'text',
              name : 'cmrNo'
            }, false, 'CMR Number should be number only.');
          }
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function addEmbargoCdValidatorForTR() {
  console.log("addEmbargoCdValidatorForTR=======");
  var role = FormManager.getActualValue('userRole');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var embargoCd = FormManager.getActualValue('embargoCd');
        if (embargoCd && !(embargoCd == 'Y' || embargoCd == 'C' || embargoCd == 'J' || embargoCd == '')) {
          return new ValidationResult(null, false, 'Embargo Code should be only Y, C, J, Blank.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function setSBOValuesForIsuCtc(value) {
  console.log("setSBOValuesForIsuCtc=======");
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var clientTier = FormManager.getActualValue('clientTier');
  var isuCd = FormManager.getActualValue('isuCd');
  var isuCtc = ((value != undefined ? value : isuCd) + clientTier);

  var reqType = FormManager.getActualValue('reqType');
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (reqType != 'C') {
    return;
  }
  var qParams = null;
  console.log("begin setSBO:" + '%' + isuCtc + '%');
  if (isuCd != '') {
    var results = null;
    qParams = {
      _qall : 'Y',
      ISSUING_CNTRY : cntry,
      ISU : '%' + isuCtc + '%'
    };
    results = cmr.query('GET.SBOLIST.BYISU', qParams);
    console.log("there are " + results.length + " SBO returned.");
    if (results.length > 0) {
      var sboList = new Array();
      for (var i = 0; i < results.length; i++) {
        sboList[i] = results[i].ret1;
      }
      if (results.length != 0) {
        FormManager.limitDropdownValues(FormManager.getField('salesBusOffCd'), sboList);
      }
      if (sboList.length == 1 || (sboList.length > 1 && _isScenarioChanged)) {
        FormManager.setValue('salesBusOffCd', sboList[0]);
        if (isuCtc == '27E') {
          FormManager.setValue('salesBusOffCd', 'A20');
          FormManager.readOnly('salesBusOffCd');
        } else if (isuCtc == '34Q' || isuCtc == '36Y') {
          FormManager.setValue('salesBusOffCd', 'A20');
          FormManager.enable('salesBusOffCd');
        }
      } else if (!_isScenarioChanged) {
        var oldSbo = null;
        qParams = {
          REQ_ID : FormManager.getActualValue('reqId')
        };
        oldSbo = cmr.query('GET.SBO.BYREQ', qParams);
        if (oldSbo != null && oldSbo != "") {
          if (sboList.indexOf(oldSbo.ret1) >= 0) {
            FormManager.setValue('salesBusOffCd', oldSbo.ret1);
          }
        }
      }
    } else {
      console.log("setting sbo=====" + isuCtc);
      if (isuCtc == '04' || isuCtc == '28' || isuCtc == '5K') {
        FormManager.setValue('salesBusOffCd', 'A00');
        FormManager.enable('salesBusOffCd');
      } else if (isuCtc == '34Q' || isuCtc == '36Y') {
        FormManager.setValue('salesBusOffCd', 'A20');
        FormManager.enable('salesBusOffCd');
      } else
        FormManager.resetDropdownValues(FormManager.getField('salesBusOffCd'));
    }
  } else {
    FormManager.resetDropdownValues(FormManager.getField('salesBusOffCd'));
    console.log("reset SBO start.");

    var oldSbo = null;
    qParams = {
      REQ_ID : FormManager.getActualValue('reqId')
    };
    oldSbo = cmr.query('GET.SBO.BYREQ', qParams);
    if (oldSbo != null && oldSbo != "") {
      FormManager.setValue('salesBusOffCd', oldSbo.ret1);
      console.log("reseting SBO ." + oldSbo);
    }
  }
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    FormManager.readOnly('salesBusOffCd');
  }
}

function setSBOLogicOnISUChange() {
  console.log("setSBOLogicOnISUChange=======");
  if (_isuCdHandler == null && FormManager.getField('isuCd')) {
    _isuCdHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
      setSBOValuesForIsuCtc(value);
    });
  }
  if (_clientTierHandler == null && FormManager.getField('clientTier')) {
    _clientTierHandler = dojo.connect(FormManager.getField('clientTier'), 'onChange', function(value) {
      setSBOValuesForIsuCtc(FormManager.getActualValue('isuCd'));
    });
  }
}

var _isuHandler = null;
var _oldSubInd = null;
function onIsuChangeHandler() {
  console.log("onIsuChangeHandler=======");
  _oldSubInd = FormManager.getActualValue('subIndustryCd');
  if (_isuHandler == null) {
    _isuHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
      // setISUCTCBasedScenarios();
      setClientTierAndISR(value);
      setDefaultEntCBMEA();
    });
  }

  if (_subIndustryCdHandler == null) {
    _subIndustryCdHandler = dojo.connect(FormManager.getField('subIndustryCd'), 'onChange', function(value) {
      if (_oldSubInd != FormManager.getActualValue('subIndustryCd') || typeof (_pagemodel) != 'undefined' && _pagemodel['custSubGrp'] != FormManager.getActualValue('custSubGrp')) {
        console.log("On subIndustryCdHandler=======");
        setDefaultEntCBMEA();
      }
    });
  }
}

function setISUCTCBasedScenarios() {
  console.log("setISUCTCBasedScenarios=======");
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var role = FormManager.getActualValue('userRole').toUpperCase();

  var clientTier = FormManager.getActualValue('clientTier');
  var isuCd = FormManager.getActualValue('isuCd');
  var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  var reqType = FormManager.getActualValue('reqType');

  if (reqType == 'C' && cmrIssuingCntry == '862') {
    if (custSubGrp == 'BUSPR' || custSubGrp == 'XBP') {
      if (role == 'REQUESTER') {
        FormManager.readOnly('clientTier');
        FormManager.readOnly('salesBusOffCd');
        FormManager.readOnly('enterprise');
      } else if (role == 'PROCESSOR') {
        FormManager.readOnly('clientTier');
        FormManager.readOnly('isuCd');
        FormManager.enable('salesBusOffCd');
        FormManager.readOnly('enterprise');
      }
    } else if ((custSubGrp == 'IBMEM' || custSubGrp == 'XINT' || custSubGrp == 'INTER' || custSubGrp == 'XPC') && (role == 'REQUESTER' || role == 'PROCESSOR')) {
      FormManager.readOnly('clientTier');
      FormManager.readOnly('isuCd');
      FormManager.readOnly('salesBusOffCd');
      FormManager.readOnly('enterprise');
    } else if (custSubGrp == 'PRICU') {
      FormManager.setValue('clientTier', 'E');
      FormManager.setValue('salesBusOffCd', 'A20');
      FormManager.setValue('isuCd', '27');
      FormManager.readOnly('clientTier');
      FormManager.readOnly('isuCd');
      FormManager.readOnly('salesBusOffCd');
      FormManager.readOnly('enterprise');
    } else if ((role == 'PROCESSOR' || role == 'REQUESTER') && custSubGrp == 'COMME' || custSubGrp == 'XINTS' || custSubGrp == 'GOVRN' || custSubGrp == 'THDPT' || custSubGrp == 'XGOV'
        || custSubGrp == 'XIGF' || custSubGrp == 'XTP' || custSubGrp == 'IGF' || custSubGrp == 'GOVERN') {
      FormManager.setValue('isuCd', '27');
      FormManager.setValue('clientTier', 'E');
      FormManager.setValue('salesBusOffCd', 'A20');
      FormManager.readOnly('salesBusOffCd');
      FormManager.enable('isuCd');
      FormManager.enable('clientTier');
      FormManager.readOnly('enterprise');
      if (isuCd == '34' && clientTier == 'Q') {
        FormManager.enable('salesBusOffCd');
        FormManager.enable('clientTier');
        FormManager.enable('enterprise');
      } else if (isuCd == '36' && clientTier == 'Y') {
        FormManager.enable('salesBusOffCd');
        FormManager.enable('clientTier');
        FormManager.enable('enterprise');
        // FormManager.setValue('salesBusOffCd', 'A20');
      } else if (isuCd == '34') {
        FormManager.setValue('clientTier', 'Q');
        FormManager.enable('salesBusOffCd');
        FormManager.setValue('salesBusOffCd', 'A20');
      } else if (isuCd == '36') {
        FormManager.setValue('clientTier', 'Y');
        FormManager.enable('salesBusOffCd');
        FormManager.setValue('salesBusOffCd', 'A20');
      } else if (isuCd == '27') {
        FormManager.setValue('clientTier', 'E');
        FormManager.setValue('salesBusOffCd', 'A20');
        FormManager.enable('salesBusOffCd');
      } else if (isuCd == '04') {
        FormManager.setValue('clientTier', '');
        FormManager.setValue('salesBusOffCd', 'A00');
        FormManager.enable('salesBusOffCd');
      } else if (isuCd == '28') {
        FormManager.setValue('clientTier', '');
        FormManager.setValue('salesBusOffCd', 'A00');
        FormManager.enable('salesBusOffCd');
      } else if (isuCd == '5K') {
        FormManager.setValue('clientTier', '');
        FormManager.setValue('salesBusOffCd', 'A00');
        FormManager.enable('salesBusOffCd');
      } else {
        FormManager.setValue('clientTier', '');
      }
    }
  }
}

function addALPHANUMValidatorForEnterpriseNumber() {
  console.log("addALPHANUMValidatorForEnterpriseNumber=======");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var _number = FormManager.getActualValue('enterprise');
        if (_number && _number.length > 0 && !_number.match("^[0-9a-zA-Z]*$")) {
          return new ValidationResult(FormManager.getField('enterprise'), false, 'Enterprise Number should be alphanumeric.');
        }
        return new ValidationResult(null, true, null);
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function addALPHANUMValidatorForTypeOfCustomer() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var _value = FormManager.getActualValue('crosSubTyp');
        if (_value && _value.length > 0 && !_value.match("^[0-9a-zA-Z]*$")) {
          return new ValidationResult(FormManager.getField('crosSubTyp'), false, 'Type Of Customer should be alphanumeric.');
        }
        return new ValidationResult(null, true, null);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function sboCodeValidator() {
  console.log('sboCodeValidator======');
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var isuCd = FormManager.getActualValue('isuCd');
        var clientTier = FormManager.getActualValue('clientTier');
        var isuCtc = isuCd + clientTier;
        var sbo = FormManager.getActualValue('salesBusOffCd');
        if (isuCtc == '8B') {
          if (sbo != '140') {
            return new ValidationResult(FormManager.getField('salesBusOffCd'), false, 'SBO can only accept \'140\'\  for ISU CTC 8B.');
          } else
            return new ValidationResult(null, true, null);
        } else if (isuCtc == '21') {
          if (sbo != 'A10') {
            return new ValidationResult(FormManager.getField('salesBusOffCd'), false, 'SBO can only accept \'A10\'\ for ISU CTC 21.');
          } else
            return new ValidationResult(null, true, null);
        } else if (isuCtc == '27E' || isuCtc == '34Q' || isuCtc == '36Y') {
          if (sbo != 'A20') {
            return new ValidationResult(FormManager.getField('salesBusOffCd'), false, 'SBO can only accept \'A20\'\ for ISU CTC ' + isuCtc);
          } else
            return new ValidationResult(null, true, null);
        } else if (isuCtc == '04' || isuCtc == '28' || isuCtc == '5K') {
          if (sbo != 'A00') {
            return new ValidationResult(FormManager.getField('salesBusOffCd'), false, 'SBO can only accept \'A00\'\ for ISU ' + isuCtc);
          } else
            return new ValidationResult(null, true, null);
        } else
          return new ValidationResult(null, true, null);
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function entValidator() {
  console.log('entValidator======');
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var isuCd = FormManager.getActualValue('isuCd');
        var clientTier = FormManager.getActualValue('clientTier');
        var isuCtc = isuCd + clientTier;
        var enterprise = FormManager.getActualValue('enterprise');
        var custType = FormManager.getActualValue('custGrp');
        var countryUse = FormManager.getActualValue('countryUse');
        validEnt27E = [ '011675', '011677', '011676', '011672', '011673', '011674', '909813', '909813', '910510', '910509' ];
        var SOUTH_AFRICA_LC = [ 'ZA', 'NA', 'LS', 'ZS' ];
        var ME_LC = [ 'LY', 'TN', 'MA', 'PK', 'AF', 'EG', 'BH', 'AE', 'AE', 'IQ', 'JO', 'PS', 'KW', 'LB', 'OM', 'QA', 'SA', 'YE', 'SY' ];
        var TURKEY_LC = [ 'TR' ];
        var CEWA_LC = [ 'DZ', 'TN', 'LY', 'AO', 'BW', 'CV', 'CD', 'MG', 'MW', 'MU', 'MZ', 'ST', 'SC', 'ZM', 'ZW', 'GH', 'LR', 'NG', 'SL', 'BI', 'ER', 'ET', 'DJ', 'KE', 'RW', 'SO', 'SD', 'TZ', 'UG',
            'BJ', 'BF', 'CM', 'CF', 'TD', 'CG', 'GQ', 'GA', 'GM', 'GN', 'GW', 'CI', 'ML', 'MR', 'NE', 'SN', 'TG' ];
        var custSubType = FormManager.getActualValue('custSubGrp');
        var landCntry = '';
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        // GET LANDCNTRY in case of CB
        var result = cmr.query('LANDCNTRY.IT', {
          REQID : reqId
        });
        if (result != null && result.ret1 != undefined) {
          landCntry = result.ret1;
        }
        var crossSubTypes = [ 'ZAXCO', 'ZAXGO', 'ZAXPC', 'ZAXTP', 'SZXCO', 'SZXGO', 'SZXPC', 'SZXTP', 'NAXCO', 'NAXGO', 'NAXPC', 'NAXTP', 'LSXCO', 'LSXGO', 'LSXPC', 'LSXTP' ];
        if (crossSubTypes.includes(custSubType) && ME_LC.includes(landCntry)) {
          return;
        }
        if (custType == 'LOCAL') {
          if (isuCtc == '34Q') {
            if (!(enterprise == '911703' || enterprise == '911716' || enterprise == '911704')) {
              return new ValidationResult(FormManager.getField('enterprise'), false, 'Enterprise can only accept \'911703\'\ \'911716\'\ \'911704\'\ for ISU CTC 34Q.');
            } else
              return new ValidationResult(null, true, null);
          } else if (isuCtc == '36Y') {
            if (!(enterprise == '908030' || enterprise == '912103' || enterprise == '912104')) {
              return new ValidationResult(FormManager.getField('enterprise'), false, 'Enterprise can only accept \'908030\'\ \'912103\'\ \'912104\'\ for ISU CTC 36Y.');
            } else
              return new ValidationResult(null, true, null);
          } else if (isuCtc == '04' || isuCtc == '28' || isuCtc == '5K') {
            if (!(enterprise == '')) {
              return new ValidationResult(FormManager.getField('enterprise'), false, 'Enterprise can only accept blank for ISU CTC ' + isuCtc);
            } else
              return new ValidationResult(null, true, null);
          } else
            return new ValidationResult(null, true, null);
        } else {
          if (SOUTH_AFRICA_LC.includes(landCntry)) {
            if (isuCtc == '27E') {
              if (!((enterprise == '011680' || enterprise == '011684' || enterprise == '011679' || enterprise == '011681' || enterprise == '011682' || enterprise == '011683') || (validEnt27E
                  .includes(enterprise)))) {
                return new ValidationResult(FormManager.getField('enterprise'), false, 'Enterprise cannot accept ' + enterprise + ' for ISU CTC 27E.');
                // 'Enterprise can only accept \'011680\'\ \'911716\'\
                // \'011684\'\ \'011681\'\ \'011682\'\ \'011683\'\ for
                // ISU CTC 27E.');
              } else
                return new ValidationResult(null, true, null);
            } else if (isuCtc == '34Q') {
              if (!(enterprise == '004179' || enterprise == '011678')) {
                return new ValidationResult(FormManager.getField('enterprise'), false, 'Enterprise cannot accept ' + enterprise + '  for ISU CTC 34Q.');
                // 'Enterprise can only accept \'004179\'\ \'011678\'\
                // for ISU CTC 34Q.');
              } else
                return new ValidationResult(null, true, null);
            } else if (isuCtc == '36Y') {
              if (!(enterprise == '008028' || enterprise == '010032' || enterprise == '012430' || enterprise == '009814' || enterprise == '012095' || enterprise == '012096' || enterprise == '012097'
                  || enterprise == '012098' || enterprise == '012100' || enterprise == '012101' || enterprise == '012102')) {
                return new ValidationResult(FormManager.getField('enterprise'), false, 'Enterprise cannot accept ' + enterprise + 'for ISU CTC 36Y.');
                // 'Enterprise can only accept \'008028\'\ \'010032\'\
                // \'012430\'\ \'009814\'\ \'010032\'\ \'009814\'\ \'012095\'\
                // \'012096\'\ \'012097\'\ \'012098\'\ \'012099\'\ \'012100\'\
                // \'012101\'\ \'012102\'\ for ISU CTC 36Y.');
              } else
                return new ValidationResult(null, true, null);
            } else if (isuCtc == '04' || isuCtc == '28' || isuCtc == '12' || isuCtc == '4K' || isuCtc == '5K') {
              if (!(enterprise == '')) {
                return new ValidationResult(FormManager.getField('enterprise'), false, 'Enterprise can only accept blank for ISU CTC ' + isuCtc);
              } else
                return new ValidationResult(null, true, null);
            } else
              return new ValidationResult(null, true, null);
          } else if (CEWA_LC.includes(landCntry)) {
            if (isuCtc == '36Y') {
              if (!(enterprise == 'BUILD1' || enterprise == 'DISTR1' || enterprise == 'SRVCE1')) {
                return new ValidationResult(FormManager.getField('enterprise'), false, 'Enterprise cannot accept ' + enterprise + '  for ISU CTC 36Y.');
              } else
                return new ValidationResult(null, true, null);
            } else if (isuCtc == '5K') {
              if (!(enterprise == '')) {
                return new ValidationResult(FormManager.getField('enterprise'), false, 'Enterprise can only accept blank value for ISU CTC 5K.');
              } else
                return new ValidationResult(null, true, null);
            }

          }
        }
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function entValidatorSA() {
  if (isuCtc == '27E') {
    if ([ 'A', 'K', 'U' ].includes(subIndustryCd) && enterprise != '011675') {
      return new ValidationResult({
        id : 'enterprise',
        type : 'text',
        name : 'enterprise'
      }, false, 'Enterprise can only accept : 011675');
    } else if ([ 'B', 'C' ].includes(subIndustryCd) && enterprise != '011677') {
      return new ValidationResult({
        id : 'enterprise',
        type : 'text',
        name : 'enterprise'
      }, false, 'Enterprise can only accept : 011677');
    } else if ([ 'D', 'R', 'T', 'W' ].includes(subIndustryCd) && enterprise != '011676') {
      return new ValidationResult({
        id : 'enterprise',
        type : 'text',
        name : 'enterprise'
      }, false, 'Enterprise can only accept : 011676');
    } else if ([ 'E', 'G', 'H', 'X', 'Y' ].includes(subIndustryCd) && enterprise != '911688') {
      return new ValidationResult({
        id : 'enterprise',
        type : 'text',
        name : 'enterprise'
      }, false, 'Enterprise can only accept : 911688');
    } else if ([ 'J', 'T' ].includes(subIndustryCd) && enterprise != '911701') {
      return new ValidationResult({
        id : 'enterprise',
        type : 'text',
        name : 'enterprise'
      }, false, 'Enterprise can only accept : 911701');
    } else if ([ 'M', 'P', 'U' ].includes(subIndustryCd) && enterprise != '901469') {
      return new ValidationResult({
        id : 'enterprise',
        type : 'text',
        name : 'enterprise'
      }, false, 'Enterprise can only accept : 901469');
    } else if ([ 'N' ].includes(subIndustryCd) && enterprise != '911698') {
      return new ValidationResult({
        id : 'enterprise',
        type : 'text',
        name : 'enterprise'
      }, false, 'Enterprise can only accept : 911698');
    }
  } else if (isuCtc == '34Q') {

    var enterprise34Q = [ '911690', '911685', '911691', '911702', '911687', '911697' ];
    if ([ 'E' ].includes(subIndustryCd) && !(enterprise == '911695' || enterprise34Q.includes(enterprise))) {
      return new ValidationResult({
        id : 'enterprise',
        type : 'text',
        name : 'enterprise'
      }, false, 'Enterprise can only accept : \'911695\'\ \'911690\'\ \'911685\'\ \'911691\'\ \'911702\'\ \'911687\'\ \'911797\'\ ');
    }
    if ([ 'G', 'V', 'Y' ].includes(subIndustryCd) && !(enterprise == '911692' || enterprise34Q.includes(enterprise))) {
      return new ValidationResult({
        id : 'enterprise',
        type : 'text',
        name : 'enterprise'
      }, false, 'Enterprise can only accept : \'911692\'\ \'911690\'\ \'911685\'\ \'911691\'\ \'911702\'\ \'911687\'\ \'911797\'\ ');
    } else if ([ 'H', 'X' ].includes(subIndustryCd) && !(enterprise == '911696' || enterprise34Q.includes(enterprise))) {
      return new ValidationResult({
        id : 'enterprise',
        type : 'text',
        name : 'enterprise'
      }, false, 'Enterprise can only accept : \'911696\'\ \'911690\'\ \'911685\'\ \'911691\'\ \'911702\'\ \'911687\'\ \'911797\'\ ');
    } else if (!enterprise34Q.includes(enterprise) && !([ 'E', 'G', 'V', 'Y', 'H', 'X' ].includes(subIndustryCd))) {
      return new ValidationResult({
        id : 'enterprise',
        type : 'text',
        name : 'enterprise'
      }, false, 'Enterprise can only accept : \'911690\'\ \'911685\'\ \'911691\'\ \'911702\'\ \'911687\'\ \'911797\'\ ');
    }
  } else if (isuCtc == '36Y') {
    var enterprise36Y = [ '908025', '912094', '912093' ];
    if (!enterprise36Y.includes(enterprise)) {
      return new ValidationResult({
        id : 'enterprise',
        type : 'text',
        name : 'enterprise'
      }, false, 'Enterprise can only accept : \'908025\'\ \'912094\'\ \'912093\'\ ');
    }
  }
}
function addTRLandedCountryValidtor() {
  console.log("addTRLandedCountryValidtor=======");
  FormManager
      .addFormValidator(
          (function() {
            return {
              validate : function() {
                var reqType = FormManager.getActualValue('reqType');
                if (typeof (CmrGrid.GRIDS.ADDRESS_GRID_GRID) != 'undefined' && CmrGrid.GRIDS.ADDRESS_GRID_GRID != null) {
                  var addressStore = CmrGrid.GRIDS.ADDRESS_GRID_GRID.store, addressItems = addressStore._arrayOfAllItems, addrGridRow = 0, genericMsg = 'Landed Country value of the Sold-to (Main) Address should not be "TR" for Cross-Border customers.';
                  if (addressItems != null && addressItems.length != 0) {
                    for ( var key in addressItems) {
                      addrGridRow++;
                      var currentAddr = addressItems[key], landCtry = '', taxOffice = '', scenario = FormManager.getActualValue('custGrp');
                      var addrType = currentAddr.addrType[0];
                      landCtry = currentAddr.landCntry[0];
                      taxOffice = currentAddr.taxOffice[0];
                      console.log('addrType >> ' + addrType);
                      console.log('landCntry >> ' + landCtry);
                      if (addrType == 'ZS01' && landCtry == 'TR' && reqType == 'C' && scenario == 'CROSS') {
                        return new ValidationResult(null, false, genericMsg);
                      }
                      if (addrType == 'ZS01' && landCtry != 'TR' && reqType == 'C' && scenario == 'LOCAL') {
                        genericMsg = 'Landed Country value of the Sold-to (Main) Address should be "TR" for Local customers.';
                        return new ValidationResult(null, false, genericMsg);
                      }
                      if (addrType == 'ZP01' && reqType == 'C' && scenario == 'LOCAL' && (taxOffice == '' || taxOffice == null || taxOffice == undefined)) {
                        genericMsg = 'Tax office of the Local Language translation of Sold-to Address is required.';
                        return new ValidationResult(null, false, genericMsg);
                      }
                    } // for
                  }
                } else {
                  console.log("CmrGrid.GRIDS.ADDRESS_GRID_GRID undefined/null");
                }
                return new ValidationResult(null, true);
              } // validate
            }; // return
          })(), 'MAIN_NAME_TAB', 'frmCMR'); // validator body
}

function setClassificationCodeTR() {
  console.log("setClassificationCodeTR=======");
  var field = FormManager.getField('custClass');
  if (FormManager.getActualValue('reqType') == 'C') {
    FormManager.limitDropdownValues(field, [ '45', '46', '71' ]);
  } else if (FormManager.getActualValue('reqType') == 'U') {
    FormManager.limitDropdownValues(field, [ '11', '13', '33', '35', '45', '46', '60', '71', '81' ]);
  }
}

function setCOFClassificationCodeTR() {
  console.log("setCOFClassificationCodeTR=======");
  var cofVal = FormManager.getActualValue('commercialFinanced');
  var field = FormManager.getField('custClass');
  if (cofVal == 'R' || cofVal == 'S' || cofVal == 'T') {
    FormManager.limitDropdownValues(field, [ '11' ]);
  } else {
    setClassificationCodeTR();
  }
}

function setTypeOfCustomerClassificationCodeTR() {
  console.log("setTypeOfCustomerClassificationCodeTR=======");
  var typeOfCustomer = FormManager.getActualValue('crosSubTyp');
  var field = FormManager.getField('custClass');
  if (typeOfCustomer == 'G') {
    FormManager.limitDropdownValues(field, [ '13' ]);
  } else if (typeOfCustomer == 'BP') {
    FormManager.limitDropdownValues(field, [ '45', '46' ]);
  } else if (typeOfCustomer == '91') {
    FormManager.limitDropdownValues(field, [ '81' ]);
  } else {
    setClassificationCodeTR();
  }
}

function controlFieldsBySubScenarioTR(value) {
  console.log("controlFieldsBySubScenarioTR=======");
  var reqType = FormManager.getActualValue('reqType');
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (reqType != 'C') {
    return;
  }
  if (reqType == 'C') {
    if (!value) {
      value = FormManager.getActualValue('custSubGrp');
    }
    FormManager.removeValidator('crosSubTyp', Validators.REQUIRED);
    // Control Type Of Customer
    if (value == 'BUSPR' || value == 'XBP') {
      FormManager.setValue('crosSubTyp', 'BP');
    } else if (value == 'XGOV' || value == 'GOVRN') {
      FormManager.setValue('crosSubTyp', 'G');
    } else if (value == 'INTER' || value == 'XINT') {
      FormManager.setValue('crosSubTyp', '91');
    } else if (value == 'COMME' || value == 'IGF' || value == 'OEM' || value == 'PRICU' || value == 'THDPT' || value == 'XINTS' || value == 'XPC' || value == 'XIBME' || value == 'XTP'
        || value == 'XIGF') {
      FormManager.setValue('crosSubTyp', '');
    }
  }
}

function lockFieldsIBMEm() {
  console.log("lockFieldsIBMEm=======");
  if (!(FormManager.getActualValue('custSubGrp') == 'IBMEM' && FormManager.getActualValue('userRole') == 'Requester')) {
    return;
  }
  FormManager.readOnly('isuCd');
  FormManager.readOnly('clientTier');
  FormManager.readOnly('custClass');
}

function setIsicClassificationCodeTR(value) {
  console.log("setIsicClassificationCodeTR=======");
  var field = FormManager.getField('custClass');
  if (!value) {
    value = FormManager.getActualValue('isicCd');
  }

  if (value == '9500') {
    FormManager.limitDropdownValues(field, [ '60', '71' ]);
  } else {
    setClassificationCodeTR();
  }
}

function turkish(input) {
  console.log("turkish=======");
  var value = FormManager.getActualValue(input);
  if (!value || value == '' || value.length == 0) {
    return true;
  }
  // var reg =
  // /^[0-9ABDEFHJ-NPQRTV-Zabdefhj-npqrtv-zÇçĞğİıÖöŞşÜü\'\"\,\.\!\-\$\(\)\?\:\s|“|�?|‘|’|�?|＂|．|？|：|。|，]+/;
  var reg = /[a-zA-Z0-9ğüşöçİĞÜŞÖÇ]+/;
  if (!value.match(reg)) {
    return new ValidationResult(input, false, '{1} is not a valid value for {0}. Please enter turkish characters only.');
  } else {
    return new ValidationResult(input, true);
  }
}

function filterCmrnoForTR() {
  console.log("filterCmrnoForTR=======");
  var cmrNo = FormManager.getActualValue('cmrNo');
  if (cmrNo.length > 0 && cmrNo.substr(0, 1).toUpperCase() == 'P') {
    FormManager.setValue('cmrNo', '');
  }

  dojo.connect(FormManager.getField('cmrNo'), 'onChange', function(value) {
    if (value.length > 0 && value.substr(0, 1).toUpperCase() == 'P') {
      FormManager.setValue('cmrNo', '');
    }
  });
}

function addCustNm4ValidatorForTR() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var custNm4 = FormManager.getActualValue('custNm4');
        var streetCont = FormManager.getActualValue('addrTxt2');

        // var reqId = FormManager.getActualValue('reqId');
        // var addressType = FormManager.getActualValue('addrType');
        // var addrSeq = FormManager.getActualValue('addrSeq');
        // var reqType = FormManager.getActualValue('reqType');
        // var qParams = {
        // ADDR_TYPE : addressType,
        // ADDR_SEQ : addrSeq,
        // REQ_ID : reqId
        // };
        // var results = cmr.query('GET.NAME4STR.TR', qParams);
        // if ('U' == reqType) {
        // if (results.ret2 != null && results.ret1 != null && results.ret2 ==
        // streetCont && results.ret1 == custNm4) {
        // return new ValidationResult(null, true);
        // }
        // }

        if ((custNm4.length != undefined && custNm4.length > 0) && (streetCont.length != undefined && streetCont.length > 0)) {
          return new ValidationResult(null, false, 'Only \'Street Cont\' or \'Name 4\' can be filled.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function afterConfigForTR() {
  console.log("afterConfigForTR=======");
  if (_landCntryHandler == null) {
    _landCntryHandler = dojo.connect(FormManager.getField('landCntry'), 'onChange', function(value) {
      setDistrictMandatoryTR();
      addTurkishCharValidator();
    });
  }
  // CREATCMR-788
  addressQuotationValidatorTR();
}

var _isScenarioChanged = false;

function checkScenarioChanged(fromAddress, scenario, scenarioChanged) {
  console.log("checkScenarioChanged=======");
  _isScenarioChanged = scenarioChanged;
  setSBOValuesForIsuCtc(FormManager.getActualValue('isuCd'));
  setValuesForTurkey();
  // if (_isScenarioChanged) {
  // setCustSubTypeBpGRTRCY();
  // }
}

function handleClassCode() {
  console.log("handleClassCode=======");
  var custType = FormManager.getActualValue('custSubGrp');
  var reqType = FormManager.getActualValue('reqType');
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (reqType != 'C') {
    return;
  }
  if (custType == 'BUSPR' || custType == 'XBP' || custType == 'IBMEM') {
    FormManager.show('CustClass', 'custClass');
    FormManager.addValidator('custClass', Validators.REQUIRED, [ 'Classification Code' ], 'MAIN_CUST_TAB');
  } else {
    FormManager.hide('CustClass', 'custClass');
    FormManager.setValue('custClass', '');
    FormManager.resetValidations('custClass');
  }
}

function hideCollectionCode() {
  console.log("hideCollectionCode=======");
  var viewOnly = FormManager.getActualValue('viewOnlyPage');
  if (viewOnly != '' && viewOnly == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') == 'C') {
    FormManager.hide('CollectionCd2', 'collectionCd2');
  }
}

function checkCmrUpdateBeforeImport() {
  console.log("checkCmrUpdateBeforeImport=======");
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

// function setValuesWRTIsuCtc(ctc) {
// console.log("setValuesWRTIsuCtc=======");
// var role = FormManager.getActualValue('userRole').toUpperCase();
// var isu = FormManager.getActualValue('isuCd');
// if (ctc == null) {
// var ctc = FormManager.getActualValue('clientTier');
// }
// var cntry = FormManager.getActualValue('cmrIssuingCntry');
// if (isu == '34' && ctc == 'Q') {
// FormManager.setValue('salesBusOffCd', 'A20');
// if (role == 'REQUESTER' || role == 'PROCESSOR') {
// FormManager.enable('salesBusOffCd');
// }
// } else if (isu == '5K') {
// // FormManager.removeValidator('clientTier', Validators.REQUIRED);
// FormManager.setValue('salesBusOffCd', 'A00');
// if (role == 'REQUESTER' || role == 'PROCESSOR') {
// FormManager.enable('salesBusOffCd');
// }
// }
// }

// CREATCMR-4293
function setCTCValues() {
  console.log("setCTCValues=======");
  FormManager.removeValidator('clientTier', Validators.REQUIRED);

  var custSubGrp = FormManager.getActualValue('custSubGrp');

  // Business Partner
  if (custSubGrp == 'XBP') {
    var isuCd = FormManager.getActualValue('isuCd');
    if (isuCd == '8B') {
      FormManager.setValue('clientTier', _pagemodel.clientTier == null ? '' : _pagemodel.clientTier);
      FormManager.readOnly('clientTier');
    }

  }

  // Internal
  if (custSubGrp == 'XINT') {
    var isuCd = FormManager.getActualValue('isuCd');
    if (isuCd == '21') {
      FormManager.setValue('clientTier', _pagemodel.clientTier == null ? '' : _pagemodel.clientTier);
      FormManager.enable('clientTier');
    }
  }

}

// creatcmr-6032
function vatValidatorTR() {
  console.log('vatValidatorTR======');
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqType = FormManager.getActualValue('reqType');
        var custGrp = FormManager.getActualValue('custGrp');
        var vat = FormManager.getActualValue('vat');
        var reqId = FormManager.getActualValue('reqId');
        var cmrResult = FormManager.getActualValue('findCmrResult');
        var dnbResult = FormManager.getActualValue('findDnbResult');
        var districts = [ 'GAZIMAGUSA', 'GIRNE', 'GUZELYURT', 'YENI ISKELE', 'LEFKE', 'LEFKOSA' ];
        var soldToDistrict = null;
        var vatPattern = null;
        var vatDistrictChkUpdate = true;
        if (_allAddressData.length > 0) {
          for (var i = 0; i < _allAddressData.length; i++) {
            if (_allAddressData[i].addrType[0] == 'ZS01') {
              soldToDistrict = _allAddressData[i].dept[0];
            }
          }
        }
        var requestId = FormManager.getActualValue('reqId');
        qParams = {
          REQ_ID : requestId,
        };
        var result = cmr.query('GET.VAT_OLD_BY_REQID', qParams);
        var oldVAT = result.ret1;
        qParams = {
          REQ_ID : requestId,
        };
        var result = cmr.query('GET.OLD_ZS01_DEPT_BY_REQID', qParams);
        var oldZS01DEPT = result.ret1;

        if (result.ret1 == '' && result.ret1 != null) {
          vatDistrictChkUpdate = true;
        }
        if ((cmrResult != '' && cmrResult == 'Accepted') || (dnbResult != '' && cmrResult == 'Accepted')) {
          if (oldVAT == FormManager.getActualValue('vat') && oldZS01DEPT == soldToDistrict) {
            return new ValidationResult(null, true);
          }
        }
        if (cmrResult == 'No Results' || cmrResult == 'Rejected' || dnbResult == 'No Results' || dnbResult == 'Rejected') {
          if (vat == '') {
            return new ValidationResult(null, true);
          }
        }
        if (soldToDistrict != null && districts.includes(soldToDistrict.toUpperCase()) && (reqType == 'C' || (reqType == 'U' && vatDistrictChkUpdate))) {
          vatPattern = /^[0-9]{9}$/;
          if (vat.match(vatPattern) != null && vat.match(vatPattern).length > 0) {
            return new ValidationResult(null, true);
          } else {
            if (vat != '' || oldVAT != '' || (oldVAT != '' && oldZS01DEPT != soldToDistrict)) {
              return new ValidationResult({
                id : 'vat',
                type : 'text',
                name : 'vat'
              }, false, 'Format for VAT is incorrect. Correct format is 999999999.');
            }
          }
        } else {
          if (vat.match(/^[0-9]{10}$/) || vat.match(/^[0-9]{11}$/)) {
            return new ValidationResult(null, true);
          }
          if ((vat != '') && reqType == 'U') {
            return new ValidationResult({
              id : 'vat',
              type : 'text',
              name : 'vat'
            }, false, 'Invalid VAT for TR. Length should be 10, or 11 characters long.');
          }
          if ((vat != '' || oldVAT != '' || (oldVAT != '' && oldZS01DEPT != soldToDistrict)) && reqType == 'C') {
            return new ValidationResult({
              id : 'vat',
              type : 'text',
              name : 'vat'
            }, false, 'Invalid VAT for TR. Length should be 10, or 11 characters long.');
          }
        }
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function StcOrderBlockValidation() {
  console.log('StcOrderBlockValidation======');
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        // var role = FormManager.getActualValue('userRole').toUpperCase();
        var ordBlk = FormManager.getActualValue('embargoCd');
        var stcOrdBlk = FormManager.getActualValue('taxExemptStatus3');
        if (ordBlk == null || ordBlk == '') {
          if (stcOrdBlk == 'ST' || stcOrdBlk == '') {
          } else {
            return new ValidationResult(null, false, 'Only ST and blank STC order block code allowed.');
          }
        } else if (ordBlk != '' && stcOrdBlk != '') {
          return new ValidationResult(null, false, 'Please fill either STC order block code or Embargo Code field');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function clientTierValidator() {
  console.log('clientTierValidator======');
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
            oldISU = result.ret3 != null ? result.ret3 : '';

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

function clientTierCodeValidator() {
  var isuCode = FormManager.getActualValue('isuCd');
  var clientTierCode = FormManager.getActualValue('clientTier');
  var reqType = FormManager.getActualValue('reqType');

  if (isuCode == '34') {
    if (clientTierCode == '') {
      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier code is Mandatory.');
    } else if (clientTierCode == 'Q') {
      return new ValidationResult(null, true);
    } else {
      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier can only accept value Q.');
    }
  } else if (isuCode == '27') {
    if (clientTierCode == '') {
      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier code is Mandatory.');
    } else if (clientTierCode == 'E') {
      return new ValidationResult(null, true);
    } else {
      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier can only accept value E.');
    }
  } else if (isuCode == '36') {
    if (clientTierCode == '') {
      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier code is Mandatory.');
    } else if (clientTierCode == 'Y') {
      return new ValidationResult(null, true);
    } else {
      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier can only accept value Y.');
    }
  } else if (isuCode == '8B' || isuCode == '21' || isuCode == '28' || isuCode == '04' || isuCode == '5K') {
    if (clientTierCode == '') {
      $("#clientTierSpan").html('');

      return new ValidationResult(null, true);
    } else {
      $("#clientTierSpan").html('');
      $("#clientTierSpan").append('<span style="color:red" class="cmr-ast" id="ast-clientTier">* </span>');

      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier can only accept blank.');
    }
  }
}

function addressQuotationValidatorTR() {
  console.log('addressQuotationValidatorTR======');
  // CREATCMR-788
  FormManager.addValidator('abbrevNm', Validators.NO_QUOTATION, [ 'Abbreviated Name (TELX1)' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('abbrevLocn', Validators.NO_QUOTATION, [ 'Abbreviated Location' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('custNm1', Validators.NO_QUOTATION, [ 'Customer Name' ]);
  FormManager.addValidator('custNm2', Validators.NO_QUOTATION, [ 'Customer Name Con\'t' ]);
  FormManager.addValidator('addrTxt', Validators.NO_QUOTATION, [ 'Street Address' ]);
  FormManager.addValidator('addrTxt2', Validators.NO_QUOTATION, [ 'Street Con\'t' ]);
  FormManager.addValidator('city1', Validators.NO_QUOTATION, [ 'City' ]);
  // FormManager.addValidator('postCd', Validators.NO_QUOTATION, [ 'Postal Code'
  // ]);
  FormManager.addValidator('dept', Validators.NO_QUOTATION, [ 'District' ]);
  FormManager.addValidator('custPhone', Validators.NO_QUOTATION, [ 'Phone #' ]);
  FormManager.addValidator('taxOffice', Validators.NO_QUOTATION, [ 'Tax Office' ]);
}
function setValuesForTurkey() {

  console.log('setValuesForTurkey=====');
  var reqType = FormManager.getActualValue('reqType');
  var issuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  var currentLandedCountry = getLandedCountry();
  var isuCd = FormManager.getActualValue('isuCd');
  var clientTier = FormManager.getActualValue('clientTier');
  var countryUse = FormManager.getActualValue('countryUse');
  var subIndustry = FormManager.getActualValue('subIndustryCd');
  var custType = FormManager.getActualValue('custGrp');
  var custSubType = FormManager.getActualValue('custSubGrp');
  var enterprise = FormManager.getActualValue('enterprise');
  var reqType = FormManager.getActualValue('reqType');
  var role = null;
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (custSubType == null) {
    return;
  }
  if (reqType != 'C') {
    return;
  }

  if (reqType == 'C') {
    if (isuCd != '') {

      var isuCtc = isuCd + clientTier;
      var ind = subIndustry.substring(0, 1);
      var indG = subIndustry.substring(0, 2);
    }

    if (reqType == 'C') {
      FormManager.setValue('repTeamMemberNo', 'NOREP0');
    }

    if (issuingCntry == '862' && custType == 'LOCAL') {
      if (custSubType == 'PRICU') {
        FormManager.setValue('enterprise', '911720');
        FormManager.readOnly('enterprise');
      } else if (custSubType == 'BUSPR' || custSubType == 'INTER' || custSubType == "IBMEM") {
        FormManager.setValue('enterprise', '');
        FormManager.readOnly('enterprise');
      } else if (custSubType == 'COMME' || custSubType == 'GOVRN' || custSubType == 'THDPT' || custSubType == 'IGF') {
        if (isuCtc == '27E') {
          var T911727 = 'A';
          var T911710 = 'B';
          var T911725 = 'C';
          var T911711 = 'D';
          var T911712 = 'E';
          var T911708 = 'F';
          var T911715 = [ 'GB', 'GF', 'GJ', 'GN' ];
          var T911717 = [ 'GA', 'GC', 'GD', 'GE', 'GG', 'GH', 'GK', 'GL', 'GM', 'GP', 'GR', 'GW' ];
          var T911718 = [ 'GA', 'GC', 'GD', 'GE', 'GG', 'GH', 'GK', 'GL', 'GM', 'GP', 'GR', 'GW' ];
          var T911719 = 'H';
          var T911706 = 'J';
          var T911724 = 'K';
          var T911713 = 'L';
          var T911721 = 'M';
          var T911722 = 'N';
          var T911709 = 'P';
          var T911726 = 'R';
          var T911707 = 'S';
          var T911728 = 'T';
          var T911714 = 'U';
          var T911705 = 'V';
          var T911720 = 'W';
          var T911723 = 'X';
          var T911716 = 'Y';

          if (ind != '') {
            if (T911727.includes(ind)) {
              FormManager.setValue('enterprise', '911727');
            } else if (T911710.includes(ind)) {
              FormManager.setValue('enterprise', '911710');
            } else if (T911725.includes(ind)) {
              FormManager.setValue('enterprise', '911725');
            } else if (T911711.includes(ind)) {
              FormManager.setValue('enterprise', '911711');
            } else if (T911712.includes(ind)) {
              FormManager.setValue('enterprise', '911712');
            } else if (T911708.includes(ind)) {
              FormManager.setValue('enterprise', '911708');
            } else if (T911715.includes(indG)) {
              // landed country- TR 06- first fetch address then fetch code
              FormManager.setValue('enterprise', '911715');
            } else if (T911717.includes(indG)) {
              FormManager.setValue('enterprise', '911717');
            } else if (T911718.includes(indG)) {
              FormManager.setValue('enterprise', '911718');
            } else if (T911719.includes(ind)) {
              FormManager.setValue('enterprise', '911719');
            } else if (T911706.includes(ind)) {
              FormManager.setValue('enterprise', '911706');
            } else if (T911724.includes(ind)) {
              FormManager.setValue('enterprise', '911724');
            } else if (T911713.includes(ind)) {
              FormManager.setValue('enterprise', '911713');
            } else if (T911721.includes(indG)) {
              FormManager.setValue('enterprise', '911721');
            } else if (T911718.includes(indG)) {
              FormManager.setValue('enterprise', '911722');
            } else if (T911709.includes(ind)) {
              FormManager.setValue('enterprise', '911709');
            } else if (T911726.includes(ind)) {
              FormManager.setValue('enterprise', '911726');
            } else if (T911707.includes(ind)) {
              FormManager.setValue('enterprise', '911707');
            } else if (T911728.includes(ind)) {
              FormManager.setValue('enterprise', '911728');
            } else if (T911714.includes(indG)) {
              FormManager.setValue('enterprise', '911714');
            } else if (T911705.includes(ind)) {
              FormManager.setValue('enterprise', '911705');
            } else if (T911720.includes(ind)) {
              FormManager.setValue('enterprise', '911720');
            } else if (T911723.includes(ind)) {
              FormManager.setValue('enterprise', '911723');
            } else if (T911716.includes(ind)) {
              FormManager.setValue('enterprise', '911716');
            } else {
              FormManager.setValue('enterprise', '911727');
            }
          } else {
            FormManager.setValue('enterprise', '911727');
            FormManager.readOnly('enterprise');
          }
        } else {
          FormManager.setValue('enterprise', '');
          FormManager.enable('enterprise');
        }
      }
    }

    if (custSubType == 'XINTS' || custSubType == 'XGOV' || custSubType == 'XIGF' || custSubType == 'XPC' || custSubType == 'XTP') {
      FormManager.enable('enterprise');
      setCBEnterprise();
    }
  }
}

function setCBEnterprise() {
  if (issuingCntry == '862' && currentLandedCountry == 'SA' && custType == 'CROSS') {
    if (isuCtc == '27E') {
      var T011675 = [ 'A', 'K', 'U' ];
      var T011677 = [ 'B', 'C' ];
      var T011676 = [ 'D', 'R', 'T', 'W' ];
      var T011672 = [ 'E', 'G', 'H', 'X', 'Y' ];
      var T011673 = [ 'F', 'N', 'S' ];
      var T011674 = [ 'J', 'L', 'M', 'P', 'V' ];
      if (ind != '') {
        if (T011675.includes(ind)) {
          FormManager.setValue('enterprise', '011675');
        } else if (T011677.includes(ind)) {
          FormManager.setValue('enterprise', '011677');
        } else if (T011676.includes(ind)) {
          FormManager.setValue('enterprise', '011676');
        } else if (T011672.includes(ind)) {
          FormManager.setValue('enterprise', '011672');
        } else if (T011673.includes(ind)) {
          FormManager.setValue('enterprise', '011673');
        } else if (T011674.includes(ind)) {
          FormManager.setValue('enterprise', '011674');
        } else
          FormManager.setValue('enterprise', '911713');
      }
    }

    if (currentLandedCountry == 'NM' && issuingCntry == SysLoc.TURKEY && custGrp == 'CROSS') {
      if (isuCtc == '27E') {
        FormManager.setValue('enterprise', '909813');
      }
    } else if (currentLandedCountry == 'LS' && issuingCntry == SysLoc.TURKEY && custGrp == 'CROSS') {
      FormManager.setValue('enterprise', '910510');
    } else if (currentLandedCountry == 'EW' && issuingCntry == SysLoc.TURKEY && custGrp == 'CROSS') {
      FormManager.setValue('enterprise', '910509');
    } else {
      FormManager.setValue('enterprise', '');
      FormManager.enable('enterprise');
    }
  }
}

function getLandedCountry() {
  var landCntry = '';
  var reqId = FormManager.getActualValue('reqId');
  // GET LANDCNTRY in case of CB
  var result = cmr.query('LANDCNTRY.IT', {
    REQID : reqId
  });
  if (result != null && result.ret1 != undefined) {
    landCntry = result.ret1;
  }
  var reqId = FormManager.getActualValue('reqId');
  var custSubType = FormManager.getActualValue('custSubGrp');
  var landCntry = '';
  var result = cmr.query('LANDCNTRY.IT', {
    REQID : reqId
  });
  if (result != null && result.ret1 != undefined) {
    landCntry = result.ret1;
  }
  return landCntry;
}

dojo.addOnLoad(function() {

  console.log('adding EMEA functions...');
  GEOHandler.EMEA = [ SysLoc.TURKEY ];
  GEOHandler.addAddrFunction(addEMEALandedCountryHandler, GEOHandler.EMEA);
  GEOHandler.enableCopyAddress(GEOHandler.EMEA, validateEMEACopy, [ 'ZD01', 'CTYC' ]);
  GEOHandler.enableCustomerNamesOnAddress(GEOHandler.EMEA);
  GEOHandler.addAddrFunction(updateMainCustomerNames, GEOHandler.EMEA);
  GEOHandler.setRevertIsicBehavior(false);
  GEOHandler.addAddrFunction(addPhoneValidatorEMEA, [ SysLoc.TURKEY ]);

  GEOHandler.addAfterTemplateLoad(lockCmrOwner, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterTemplateLoad(setFieldsToReadOnlyGRCYTR, [ SysLoc.TURKEY ]);
  // GEOHandler.addAfterTemplateLoad(setClientTierValuesTR, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterTemplateLoad(setSBOLogicOnISUChange, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterTemplateLoad(checkScenarioChanged, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterTemplateLoad(setVatValidatorGRCYTR, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterTemplateLoad(toggleBPRelMemTypeForTurkey, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterTemplateLoad(controlFieldsBySubScenarioTR, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterTemplateLoad(filterCmrnoForTR, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterTemplateLoad(afterConfigForTR, [ SysLoc.TURKEY ]);
  // GEOHandler.addAfterTemplateLoad(setValuesForTurkey, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterTemplateLoad(lockSORTL, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterTemplateLoad(setISUDefaultValueOnSubTypeChange, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterTemplateLoad(addISUHandler, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterTemplateLoad(lockFieldsIBMEm, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterTemplateLoad(handleClassCode, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterTemplateLoad(setCTCValues, [ SysLoc.TURKEY ]);
  // GEOHandler.addAfterTemplateLoad(setISUCTCBasedScenarios, [ SysLoc.TURKEY
  // ]);

  GEOHandler.addAddrFunction(addrFunctionForGRCYTR, [ SysLoc.TURKEY ]);
  GEOHandler.addAddrFunction(preFillTranslationAddrWithSoldToForTR, [ SysLoc.TURKEY ]);
  GEOHandler.addAddrFunction(addTurkishCharValidator, [ SysLoc.TURKEY ]);
  GEOHandler.addAddrFunction(updateAddrTypeList, [ SysLoc.TURKEY ]);
  GEOHandler.addAddrFunction(updateAbbrevNmLocnGRCYTR, [ SysLoc.TURKEY ]);
  GEOHandler.addAddrFunction(addLatinCharValidator, [ SysLoc.TURKEY ]);
  GEOHandler.addAddrFunction(updateAbbrNameWithZS01TR, [ SysLoc.TURKEY ]);
  GEOHandler.addAddrFunction(updateAbbrLocWithZS01TR, [ SysLoc.TURKEY ]);
  GEOHandler.addAddrFunction(disableTaxOfficeTR, [ SysLoc.TURKEY ]);
  GEOHandler.addAddrFunction(autoPopulateAbbNameCopySoldToTR, [ SysLoc.TURKEY ]);

  GEOHandler.addAfterConfig(lockCmrOwner, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(setFieldsToReadOnlyGRCYTR, [ SysLoc.TURKEY ]);
  // GEOHandler.addAfterConfig(setClientTierValuesTR, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(salesSRforUpdate, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(salesSRforUpdateOnChange, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(afterConfigForTR, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(toggleTypeOfCustomerForTR, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(controlFieldsBySubScenarioTR, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(filterCmrnoForTR, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(showClassificationForTRUpd, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(setSBOValuesForIsuCtc, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(setSBOLogicOnISUChange, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(setClientTierAndISR, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(abbrvLocMandatory, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(abbrvLocMandatoryOnChange, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(setCommonCollectionCd, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(viewOnlyAddressDetails, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(setVatValidatorGRCYTR, [ SysLoc.TURKEY, ]);
  GEOHandler.addAfterConfig(defaultCapIndicator, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(enableCMRNUMForPROCESSOR, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(autoSetAbbrevNmOnChanageTR, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(autoSetAbbrevLocnOnChangeTR, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(setDefaultValueForPreferredLanguage, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(setSORTL, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(lockSORTL, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(setISUDefaultValueOnSubTypeChange, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(showCommercialFinanced, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(hideCollectionCode, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(addISUHandler, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(setEconomicCode, [ SysLoc.TURKEY ]);
  GEOHandler.addAfterConfig(onIsuChangeHandler, [ SysLoc.TURKEY ]);

  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.EMEA, GEOHandler.ROLE_PROCESSOR, true);
  GEOHandler.registerValidator(entValidator, [ SysLoc.TURKEY ], null, true);
  GEOHandler.registerValidator(sboCodeValidator, [ SysLoc.TURKEY ], null, true);
  GEOHandler.registerValidator(validateCMRNumExistForTR, [ SysLoc.TURKEY ], null, true);
  GEOHandler.registerValidator(validateCMRNumberForTR, [ SysLoc.TURKEY ], null, true);
  GEOHandler.registerValidator(addTRAddressTypeValidator, [ SysLoc.TURKEY ], null, true);
  GEOHandler.registerValidator(vatValidatorTR, [ SysLoc.TURKEY ], null, true);
  GEOHandler.registerValidator(addDistrictPostCodeCityValidator, [ SysLoc.TURKEY ], null, true);
  GEOHandler.registerValidator(addALPHANUMValidatorForEnterpriseNumber, [ SysLoc.TURKEY ], null, true);
  GEOHandler.registerValidator(addALPHANUMValidatorForTypeOfCustomer, [ SysLoc.TURKEY ], null, true);
  GEOHandler.registerValidator(addCustNm4ValidatorForTR, [ SysLoc.TURKEY ], null, true);
  GEOHandler.registerValidator(addEmbargoCdValidatorForTR, [ SysLoc.TURKEY ], null, true);
  GEOHandler.registerValidator(checkCmrUpdateBeforeImport, [ SysLoc.TURKEY ], null, true);
  GEOHandler.registerValidator(clientTierValidator, [ SysLoc.TURKEY ], null, true);
  GEOHandler.registerValidator(StcOrderBlockValidation, [ SysLoc.TURKEY ], null, true);
  GEOHandler.registerValidator(addTRLandedCountryValidtor, [ SysLoc.TURKEY ], null, true);
  GEOHandler.registerValidator(enterpriseValidatorMea, [ SysLoc.TURKEY ], null, true);

});