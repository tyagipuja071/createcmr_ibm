/* Register EMEA Javascripts */
var _customerTypeHandler = null;
var _vatExemptHandler = null;
var _isuCdHandler = null;
var _isuCdHandlerIE = null;
var _isicCdHandler = null;
var _requestingLOBHandler = null;
var _economicCdHandler = null;
var _custSubTypeHandler = null;
var _custSubTypeHandlerGr = null;
var _custSalesRepHandlerGr = null;
var _landCntryHandler = null;
var _stateProvITHandler = null;
var _internalDeptHandler = null;
var _isicHandler = null;
var addrTypeHandler = [];
var _hwMstrInstallFlagHandler = null;
var _isicCdCRNHandler = null;
var _crnExemptHandler = null;
var _scenarioSubTypeHandler = null;
var _addrTypeOnChangeHandler = [];
var _addrTypesForOnChange = [ 'ZS01', 'ZP01', 'ZD01', 'ZI01' ];

var SCOTLAND_POST_CD = [ 'AB', 'KA', 'DD', 'KW', 'DG', 'KY', 'EH', 'ML', 'FK', 'PA', 'G1', 'G2', 'G3', 'G4', 'G5', 'G6', 'G7', 'G8', 'G9', 'PH', 'TD', 'IV' ];
var NORTHERN_IRELAND_POST_CD = [ 'BT' ];
var UK_LANDED_CNTRY = [ 'AI', 'BM', 'IO', 'VG', 'KY', 'FK', 'GI', 'MS', 'PN', 'SH', 'TC', 'GS', 'GG', 'JE', 'IM', 'AO' ];
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

function getImportedIndcForItaly() {
  if (_importedIndc) {
    console.log('Returning imported indc = ' + _importedIndc);
    return _importedIndc;
  }
  var results = cmr.query('VALIDATOR.IMPORTED_IT', {
    REQID : FormManager.getActualValue('reqId')
  });
  if (results != null && results.ret1) {
    _importedIndc = results.ret1;
  } else {
    _importedIndc = 'N';
  }
  console.log('saving imported ind as ' + _importedIndc);
  return _importedIndc;

}

function addEMEALandedCountryHandler(cntry, addressMode, saving, finalSave) {
  if (!saving) {
    if (addressMode == 'newAddress') {
      FilteringDropdown['val_landCntry'] = FormManager.getActualValue('defaultLandedCountry');
      FormManager.setValue('landCntry', FormManager.getActualValue('defaultLandedCountry'));
    } else {
      FilteringDropdown['val_landCntry'] = null;
    }
  }
}

/**
 * Type Of Customer field
 */
function typeOfCustomer() {
  var reqType = FormManager.getActualValue('reqType');
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (reqType != 'C') {
    return;
  }
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var role = FormManager.getActualValue('userRole').toUpperCase();

  if (reqType == 'C') {
    if (FormManager.getActualValue('crosSubTyp') == ''
        && (custSubGrp == 'INTER' || custSubGrp == 'INTSM' || custSubGrp == 'INTVA' || custSubGrp == 'BUSPR' || custSubGrp == 'BUSSM' || custSubGrp == 'BUSVA' || custSubGrp == 'CROIN' || custSubGrp == 'CROBP')) {
      FormManager.enable('crosSubTyp');
      FormManager.addValidator('crosSubTyp', Validators.REQUIRED, [ 'Type Of Customer' ], 'MAIN_CUST_TAB');
    } else {
      FormManager.resetValidations('crosSubTyp');
    }
  }

  if ((custSubGrp == 'INTER') || (custSubGrp == 'INTSM') || (custSubGrp == 'INTVA') || (custSubGrp == 'CROIN')) {
    FormManager.enable('crosSubTyp');
    FormManager.limitDropdownValues(FormManager.getField('crosSubTyp'), [ '91', '92' ]);
  } else if (custSubGrp == 'LOCEN' || custSubGrp == 'LOCSM' || custSubGrp == 'LOCVA' || custSubGrp == 'CROLC') {
    FormManager.setValue('crosSubTyp', 'E');
    FormManager.readOnly('crosSubTyp');
  } else if (custSubGrp == 'GOVST' || custSubGrp == 'GOVSM' || custSubGrp == 'GOVVA' || custSubGrp == 'CROGO') {
    FormManager.setValue('crosSubTyp', 'G');
    FormManager.readOnly('crosSubTyp');
  } else if (custSubGrp == 'UNIVE' || custSubGrp == 'UNISM' || custSubGrp == 'UNIVA' || custSubGrp == 'CROUN') {
    FormManager.setValue('crosSubTyp', 'U');
    FormManager.readOnly('crosSubTyp');
  } else if (custSubGrp == '3PAIT' || custSubGrp == '3PASM' || custSubGrp == '3PAVA' || custSubGrp == 'CRO3P') {
    FormManager.setValue('crosSubTyp', 'IL');
    FormManager.readOnly('crosSubTyp');
  } else if (custSubGrp == 'CROPR' || custSubGrp == 'PRISM' || custSubGrp == 'PRIVA' || custSubGrp == 'PRICU' || custSubGrp == 'COMME' || custSubGrp == 'CROCM' || custSubGrp == 'COMSM'
      || custSubGrp == 'COMVA' || custSubGrp == 'NGOIT' || custSubGrp == 'NGOSM' || custSubGrp == 'NGOVA') {
    FormManager.readOnly('crosSubTyp');
    FormManager.setValue('crosSubTyp', '');
  }

  if (((custSubGrp == 'BUSPR') || (custSubGrp == 'BUSSM') || (custSubGrp == 'BUSVA') || (custSubGrp == 'CROBP')) && role != 'REQUESTER') {
    FormManager.enable('crosSubTyp');
    FormManager.limitDropdownValues(FormManager.getField('crosSubTyp'), [ '51', '52', '53' ]);
  } else if (((custSubGrp == 'BUSPR') || (custSubGrp == 'BUSSM') || (custSubGrp == 'BUSVA') || (custSubGrp == 'CROBP')) && role == 'REQUESTER') {
    FormManager.readOnly('crosSubTyp');
    FormManager.setValue('crosSubTyp', '52');
  }
}

/*
 * EmbargoCode field locked for REQUESTER
 */
function lockEmbargo() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (role == 'REQUESTER') {
    if ((FormManager.getActualValue('reqType') == 'U' || FormManager.getActualValue('reqType') == 'X')) {
      FormManager.enable('embargoCd');
    } else {
      FormManager.readOnly('embargoCd');
    }
  } else {
    FormManager.enable('embargoCd');
  }
}

function autoSetAbbrevNmFrmDept() {
  console.log('>>> Running autoSetAbbrevNmFrmDept');
  var reqType;
  if (typeof (_pagemodel) != 'undefined') {
    reqType = FormManager.getActualValue('reqType');
    role = _pagemodel.userRole;
  }
  if (reqType != 'C') {
    return;
  }
  var custType = FormManager.getActualValue('custSubGrp');
  var lob = FormManager.getActualValue('requestingLob');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var abbrevNm = '';
  var tmInstallName1 = '';

  var qParams = {
    REQ_ID : FormManager.getActualValue('reqId'),
  };

  var result = cmr.query('UKI.GET_TOP_INSTALL_1', qParams);
  tmInstallName1 = result.ret4;

  if (('XINTR' == custType || 'INTER' == custType) && (SysLoc.IRELAND == cntry || SysLoc.UK == cntry)) {
    var dept = FormManager.getActualValue('ibmDeptCostCenter');

    if (tmInstallName1.length > 11) {
      tmInstallName1 = tmInstallName1.substring(0, 11);
    }
    if (!abbrevNm.includes('IBM/')) {
      abbrevNm = 'IBM/' + dept + '/' + tmInstallName1;
      FormManager.setValue('abbrevNm', abbrevNm);
      FormManager.readOnly('abbrevNm');
    }
  }
}

function getZS01LandCntry() {
  var reqId = FormManager.getActualValue('reqId');
  if (reqId != null) {
    reqParam = {
      REQ_ID : reqId,
    };
  }
  var results = cmr.query('ADDR.GET.ZS01LANDCNTRY.BY_REQID', reqParam);
  var landCntry = results.ret2 != undefined ? results.ret2 : '';
  return landCntry;
}

function isScotlandPostCd(postCd) {
  var isScotlandPostCdInd = false;
  for (var i = 0; i < SCOTLAND_POST_CD.length; i++) {
    if (postCd == SCOTLAND_POST_CD[i]) {
      isScotlandPostCdInd = true;
      break;
    }
  }
  if (isScotlandPostCdInd == true) {
    return true;
  } else {
    return false;
  }
}

function isNorthernIrelandPostCd(postCd) {
  var isNorthernIrelandPostCd = false;
  for (var i = 0; i < NORTHERN_IRELAND_POST_CD.length; i++) {
    if (postCd == NORTHERN_IRELAND_POST_CD[i]) {
      isNorthernIrelandPostCd = true;
      break;
    }
  }
  if (isNorthernIrelandPostCd == true) {
    return true;
  } else {
    return false;
  }
}

function searchISICArryAndSetSBO(isicArry, isicCdValue, sBOValue, salesRepValue) {
  for (var i = 0; i < isicArry.length; i++) {
    if (isicArry[i] == isicCdValue) {
      FormManager.setValue('salesBusOffCd', sBOValue);
      FormManager.setValue('repTeamMemberNo', salesRepValue);
    }
  }
}

function fieldsReadOnlyItaly(fromAddress, scenario, scenarioChanged) {
  var custSubType = FormManager.getActualValue('custSubGrp');
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var reqId = FormManager.getActualValue('reqId');
  if (reqType != 'C') {
    return;
  }
  var checkImportIndc = getImportedIndcForItaly();

  if (reqType == 'C' && checkImportIndc == 'N' && scenarioChanged) {
    if (custSubType == 'BUSPR' || custSubType == 'INTER' || custSubType == 'CROBP' || custSubType == 'CROIN' || custSubType == 'BUSSM' || custSubType == 'INTSM' || custSubType == 'BUSVA'
        || custSubType == 'INTVA') {
      FormManager.setValue('isuCd', '21');
      FormManager.setValue('clientTier', '');
      FormManager.readOnly('isuCd');
      FormManager.readOnly('clientTier');
      FormManager.limitDropdownValues(FormManager.getField('repTeamMemberNo'), [ '0B14A0', '0B1BA0', '0B1GA0', '0B1EA0', '0B11A0', '0B12A0', '0B13A0' ]);
    } else if (custSubType != 'BUSPR' || custSubType != 'INTER' || custSubType != 'CROBP' || custSubType != 'CROIN' || custSubType != 'BUSSM' || custSubType != 'INTSM' || custSubType != 'BUSVA'
        || custSubType != 'INTVA') {
      /*
       * FormManager.setValue('isuCd', '32'); FormManager.setValue('clientTier',
       * 'S'); FormManager.enable('isuCd'); FormManager.enable('clientTier');
       */
    }
  }
  if (reqType == 'C' && role == 'REQUESTER') {
    console.log("fieldsReadOnlyItaly for REQUESTER..");

    // Defect: 1461349 - For Lock and unlocked
    FormManager.readOnly('abbrevNm');
    FormManager.readOnly('abbrevLocn');
    FormManager.resetValidations('abbrevNm');

    if (checkImportIndc == "Y") {
      // fields for Legacy consolidation
      FormManager.readOnly('taxCd1');
      FormManager.readOnly('vat');
      FormManager.readOnly('enterprise');
      FormManager.resetValidations('taxCd1');
      FormManager.resetValidations('vat');
      FormManager.resetValidations('enterprise');
      FormManager.readOnly('identClient');
      FormManager.resetValidations('identClient');
      FormManager.enable('collectionCd');
    }

  } else if (reqType == 'C' && role == 'PROCESSOR') {
    if (custSubType == '3PAIT' || custSubType == '3PASM' || custSubType == '3PAVA' || custSubType == 'CROCM' || custSubType == 'CROBP' || custSubType == 'CROGO' || custSubType == 'CROLC'
        || custSubType == 'CROUN') {
      FormManager.enable('enterprise');
      FormManager.enable('affiliate');
    }
    if (custSubType == 'PRICU' || custSubType == 'PRISM' || custSubType == 'PRIVA') {
      FormManager.enable('enterprise');
      FormManager.enable('affiliate');
    }
    FormManager.enable('abbrevNm');
    FormManager.enable('abbrevLocn');
    FormManager.addValidator('abbrevNm', Validators.REQUIRED, [ 'Abbreviated Name (TELX1)' ], 'MAIN_CUST_TAB');
    if (custSubType == 'GOVST' || custSubType == 'LOCEN' || custSubType == 'UNIVE') {
      FormManager.enable('specialTaxCd');
    }
    if (checkImportIndc == "Y") {
      // fields for Legacy consolidation
      FormManager.readOnly('taxCd1');
      FormManager.readOnly('vat');
      FormManager.readOnly('enterprise');
      FormManager.resetValidations('taxCd1');
      FormManager.resetValidations('vat');
      FormManager.resetValidations('enterprise');
      FormManager.readOnly('identClient');
      FormManager.resetValidations('identClient');
      FormManager.enable('collectionCd');
    }
  }
}

/**
 * Add Non-Latin character validation for address fields
 */
function addNonLatinCharValidator() {
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
      // commenting custNm2 for defect Fix 1359086
      // checkAndAddValidator('custNm2', Validators.NON_LATIN, [ 'Nickname' ]);
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
    FormManager.removeValidator('addrTxt', Validators.NON_LATIN);
    FormManager.removeValidator('addrTxt2', Validators.NON_LATIN);
    FormManager.removeValidator('city1', Validators.NON_LATIN);
    FormManager.removeValidator('postCd', Validators.NON_LATIN);
    FormManager.removeValidator('dept', Validators.NON_LATIN);
    FormManager.removeValidator('poBox', Validators.NON_LATIN);
    // FormManager.removeValidator('custPhone', Validators.NON_LATIN);
    FormManager.removeValidator('taxOffice', Validators.NON_LATIN);
  }
}

// Defect 1468006: Local languages accepted by the tool :Mukesh
function addLatinCharValidatorITALY() {
  console.log("Allow Latin character only..");
  FormManager.addValidator('custNm1', Validators.LATIN, [ 'Customer Name' ]);
}

var _addrTypesIL = [ 'ZS01', 'ZP01', 'ZI01', 'ZD01', 'ZS02', 'CTYA', 'CTYB', 'CTYC' ];
var _addrTypeHandler = [];

/**
 * 1310266 - 'newAddress' enable custPhone for mailing address
 */
function setCustPhone(value) {
  if (FormManager.getField('addrType_ZS01').checked) {
    FormManager.enable('custPhone');
  } else {
    FormManager.setValue('custPhone', '');
    FormManager.disable('custPhone');
  }
}

function validateEMEACopy(addrType, arrayOfTargetTypes) {
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

function setSpecialTaxCodeOnScenarioIT() {
  // for legacy consolidation CMR-492
  var landCntryZS01 = null;
  var reqId = FormManager.getActualValue('reqId');
  var custGroup = FormManager.getActualValue('custGrp');

  if (FormManager.getActualValue('reqType') == 'C' && custGroup == 'CROSS') {
    landCntryZS01 = cmr.query('ADDR.GET.LAND_CNTRY.BY_REQID', {
      REQ_ID : reqId,
      ADDR_TYPE : 'ZS01'
    });

    FormManager.setValue('specialTaxCd', '');
    if (landCntryZS01 != null && landCntryZS01.ret1 == 'IT') {
      FormManager.setValue('specialTaxCd', 'A');
    } else {
      FormManager.setValue('specialTaxCd', 'N');
    }
  }

}

function setSpecialTaxCodeOnAddressIT() {

  var custGroup = FormManager.getActualValue('custGrp');
  var landCntry = FormManager.getActualValue('landCntry');
  var addrType = FormManager.getActualValue('addrType');
  console.log("landCntry:" + landCntry + " addrType:" + addrType);
  if (FormManager.getActualValue('reqType') == 'C' && custGroup == 'CROSS') {
    if (landCntry != null && addrType != null && landCntry == 'IT' && addrType == 'ZS01') {
      FormManager.setValue('specialTaxCd', 'A');
    } else {
      FormManager.setValue('specialTaxCd', 'N');
    }
  }
}
// CMR-1500 Billing level fields are enabled for Create imports when Installing
// CMR is imported
function enableDisableTaxCodeCollectionCdIT() {

  var reqId = FormManager.getActualValue('reqId');
  var reqType = FormManager.getActualValue('reqType');
  if (reqType == 'C') {
    FormManager.enable('specialTaxCd');
    FormManager.enable('collectionCd');
  }
  if (reqType == 'U' || reqType == 'X') {
    var qParams = {
      ADDR_TYPE : 'ZP01',
      REQ_ID : reqId
    };

    var billingResult = cmr.query('CHECK_BILLING_IMPORTED_IT', qParams);

    var billingCMR = '';
    if (billingResult != null && billingResult.ret1 != undefined) {
      billingCMR = billingResult.ret1;
    }
    console.log("billingCMR :>" + billingCMR);
    if (billingCMR == '') {
      FormManager.readOnly('specialTaxCd');
      FormManager.readOnly('collectionCd');
    } else {
      FormManager.enable('specialTaxCd');
      FormManager.enable('collectionCd');
    }
  }
}

function getMismatchFields(zs01Data, zp01Data, isCrossborder) {
  console.log('isCrossborder: ' + isCrossborder);

  var mismatchFields = '';
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

  var mismatchFields = '';
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
  console.log(zs01Field + ' : ' + zp01Field);
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
  if (zs01Data.landCntry[0] == zp01Data.landCntry[0]) {
    return true;
  }

  return false;
}

function mappingAddressField(key) {
  var value = '';
  if (key == 'custNm1') {
    value = 'Customer Name';
  } else if (key == 'custNm2') {
    value = 'Customer Name Con\'t';
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

function setSalesBoSboIbo() {
  var repTeamMemberNo = FormManager.getActualValue('repTeamMemberNo');
  if (repTeamMemberNo != '') {
    var qParams = {
      ISSUING_CNTRY : FormManager.getActualValue('cmrIssuingCntry'),
      REP_TEAM_CD : repTeamMemberNo
    };
    var result = cmr.query('DATA.GET.SALES_BO_CD', qParams);
    var salesBoCd = result.ret1;
    var selsr = result.ret2;
    FormManager.setValue('salesBusOffCd', salesBoCd);
    FormManager.setValue('salesTeamCd', selsr);
  } else {
    FormManager.setValue('salesBusOffCd', '');
  }
}

function isUpdateReqCrossborder() {
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

function modifyCharForTurk(field) {

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
function addFieldValidationForProcessorItaly() {
  var requestType = null;
  requestType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole');
  var custSubType = FormManager.getActualValue('custSubGrp');
  var reqId = FormManager.getActualValue('reqId');

  if (requestType == 'C' && role == GEOHandler.ROLE_PROCESSOR) {
    var checkImportIndc = getImportedIndcForItaly();

    FormManager.addValidator('abbrevNm', Validators.REQUIRED, [ 'Abbreviated Name (TELX1)' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('subIndustryCd', Validators.REQUIRED, [ 'Subindustry' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('specialTaxCd', Validators.REQUIRED, [ 'Tax Code/ Code IVA' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('salesBusOffCd', Validators.REQUIRED, [ 'SBO (SORTL)' ], 'MAIN_IBM_TAB');
    FormManager.resetValidations('taxCd1');

    FormManager.addValidator('identClient', Validators.REQUIRED, [ 'Ident Client' ], 'MAIN_CUST_TAB');
    // FormManager.addValidator('collectionCd', Validators.REQUIRED, [
    // 'Collection Code/ SSV Code' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('repTeamMemberNo', Validators.REQUIRED, [ 'Sales Rep' ], 'MAIN_IBM_TAB');

    if (checkImportIndc != 'Y'
        && (custSubType == '3PAIT' || custSubType == '3PASM' || custSubType == '3PAVA' || custSubType == 'PRICU' || custSubType == 'PRISM' || custSubType == 'PRIVA' || custSubType == 'GOVST'
            || custSubType == 'GOVSM' || custSubType == 'GOVVA' || custSubType == 'CROGO' || custSubType == 'UNIVE' || custSubType == 'UNISM' || custSubType == 'UNIVA' || custSubType == 'CROUN'
            || custSubType == 'LOCEN' || custSubType == 'LOCSM' || custSubType == 'LOCVA' || custSubType == 'CROLC' || custSubType == 'COMME' || custSubType == 'COMSM' || custSubType == 'COMVA'
            || custSubType == 'CROCM' || custSubType == 'NGOIT' || custSubType == 'NGOSM' || custSubType == 'NGOVA' || custSubType == 'BUSPR' || custSubType == 'BUSSM' || custSubType == 'BUSVA' || custSubType == 'CROBP')) {
      FormManager.resetValidations('enterprise');
      FormManager.resetValidations('affiliate');
      if ("" != FormManager.getActualValue('isuCd') && ("32" != FormManager.getActualValue('isuCd') || "21" != FormManager.getActualValue('isuCd'))) {
        FormManager.addValidator('enterprise', Validators.REQUIRED, [ 'Enterprise' ], 'MAIN_IBM_TAB');
        FormManager.addValidator('affiliate', Validators.REQUIRED, [ 'Affiliate' ], 'MAIN_IBM_TAB');
        FormManager.enable('enterprise');
        FormManager.enable('affiliate');
      }
      if ("32" == FormManager.getActualValue('isuCd') || "21" == FormManager.getActualValue('isuCd')) {
        FormManager.resetValidations('enterprise');
        FormManager.resetValidations('affiliate');
        FormManager.enable('enterprise');
        FormManager.enable('affiliate');
      }
    }

    if (checkImportIndc != 'Y' && "32" == FormManager.getActualValue('isuCd')) {
      console.log("company not exit..");
      FormManager.addValidator('collectionCd', Validators.REQUIRED, [ 'Collection Code' ], 'MAIN_IBM_TAB');
      FormManager.enable('collectionCd');
    }

    if (custSubType == 'GOVSM' || custSubType == 'GOVVA' || custSubType == 'CROGO' || custSubType == 'UNISM' || custSubType == 'UNIVA' || custSubType == 'CROUN' || custSubType == 'LOCSM'
        || custSubType == 'LOCVA' || custSubType == 'CROLC' || custSubType == 'COMSM' || custSubType == 'COMVA' || custSubType == 'CROCM' || custSubType == 'BUSSM' || custSubType == 'BUSVA'
        || custSubType == 'CROBP' || custSubType == '3PASM' || custSubType == '3PAVA' || custSubType == 'NGOSM' || custSubType == 'NGOVA' || custSubType == 'PRISM' || custSubType == 'PRIVA') {
      FormManager.enable('collectionCd');
      console.log("addFieldValidationForProcessorItaly>>processor labal enable collection code");
    }
  }
  if (requestType == 'C' && role == GEOHandler.ROLE_PROCESSOR && (custSubType == 'UNISM' || custSubType == 'LOCSM' || custSubType == 'GOVSM')) {
    FormManager.enable('repTeamMemberNo');
  }
}

function addAddrValidationForProcItaly() {
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

function afterConfigForIT() {

  if (_landCntryHandler == null) {
    _landCntryHandler = dojo.connect(FormManager.getField('landCntry'), 'onChange', function(value) {
      showHideCityStateProvIT();
    });
  }

  if (_stateProvITHandler == null) {
    _stateProvITHandler = dojo.connect(FormManager.getField('stateProvItaly'), 'onChange', function(value) {
      showHideOtherStateProvIT();
    });
  }

  if (_fiscalCodeUpdateHandlerIT == null) {
    _fiscalCodeUpdateHandlerIT = dojo.connect(FormManager.getField('taxCd1'), 'onChange', function(value) {
      autoResetFiscalDataStatus();
    });
  }

  if (_CTCHandlerIT == null) {
    _CTCHandlerIT = dojo.connect(FormManager.getField('clientTier'), 'onChange', function(value) {
      setSalesRepValuesIT(value);
      // autoSetSBOSROnPostalCode(value, false);
      blankedOutCollectionCD();
    });
  }

  if (_ISUHandlerIT == null) {
    _ISUHandlerIT = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
      setClientTierValuesIT(value);
      setSalesRepValuesIT(value);
      setAffiliateEnterpriseRequired();
      blankedOutCollectionCD();

    });
  }

  if (_salesRepHandlerIT == null) {
    _salesRepHandlerIT = dojo.connect(FormManager.getField('repTeamMemberNo'), 'onChange', function(value) {
      autoSetSBOOnSRValueIT();
    });
  }

  if (_identClientHandlerIT == null) {
    _identClientHandlerIT = dojo.connect(FormManager.getField('identClient'), 'onChange', function(value) {
      setVATOnIdentClientChangeIT();
      setFiscalCodeOnIdentClientCB_IT();
    });
  }
  if (_vatUpdateHandlerIT == null) {
    _vatUpdateHandlerIT = dojo.connect(FormManager.getField('vat'), 'onChange', function(value) {
      autoResetFiscalDataStatus();
    });
  }

  if (_custGrpIT == null) {
    _custGrpIT = dojo.connect(FormManager.getField('custGrp'), 'onChange', function(value) {
      setSpecialTaxCodeOnScenarioIT();
    });
  }

  var reqType = FormManager.getActualValue('reqType');
  var role = null;
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }
  FormManager.readOnly('cmrOwner');

  if (role.toUpperCase() == 'PROCESSOR' && reqType == 'C') {
    FormManager.addValidator('collectionCd', Validators.REQUIRED, [ 'Collection Code' ], 'MAIN_IBM_TAB');
  } else {
    FormManager.removeValidator('collectionCd', Validators.REQUIRED);
  }

  var cntryRegion = FormManager.getActualValue('countryUse');
  var landCntry = 'IT'; // default to Italy
  if (cntryRegion != '' && cntryRegion.length > 3) {
    landCntry = cntryRegion.substring(3, 5);
  }
  // set default landed country
  FormManager.setValue('defaultLandedCountry', landCntry);
  FormManager.setValue('landCntry', FormManager.getActualValue('defaultLandedCountry'));
  // Story 1594125
  FormManager.readOnly('company');

  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubType = FormManager.getActualValue('custSubGrp');

  if ((reqType == 'U' || reqType == 'X') && role == 'REQUESTER') {
    FormManager.readOnly('sensitiveFlag');
  } else if ((reqType == 'U' || reqType == 'X') && role == 'PROCESSOR') {
    FormManager.enable('sensitiveFlag');
  }
  if (reqType == 'C' && role == 'REQUESTER' && FormManager.getActualValue('isuCd') == '32') {
    FormManager.readOnly('collectionCd');
  } else if (reqType == 'C' && role == 'PROCESSOR' && FormManager.getActualValue('isuCd') == '32') {
    FormManager.enable('collectionCd');
  }

  if (FormManager.getActualValue('isuCd') != '32') {
    FormManager.enable('collectionCd');
  }

  if (reqType == 'U' || reqType == 'X') {
    var reqId = FormManager.getActualValue('reqId');
    var result = cmr.query('VALIDATOR.BILLING.CROSSBORDERIT', {
      REQID : reqId
    });

    if (result != null && result.ret1 != '' && result.ret1 != undefined && result.ret1 != 'IT') {
      FormManager.setValue('vat', '');
      FormManager.readOnly('vat');
    }
  }

  // Defect 1461349: Abbreviated name, Abbreviated location :Mukesh
  autoSetAbbrevNmOnChanageIT();
  autoSetAbbrevLocnOnChangeIT();

  if (_vatUpdateHandlerIT && _vatUpdateHandlerIT[0]) {
    // _vatUpdateHandlerIT[0].onChange();
  }

  if (_identClientHandlerIT && _identClientHandlerIT[0]) {
    _identClientHandlerIT[0].onChange();
  }

  if (_landCntryHandler && _landCntryHandler[0]) {
    _landCntryHandler[0].onChange();
  }

  if (_stateProvITHandler && _stateProvITHandler[0]) {
    _stateProvITHandler[0].onChange();
  }

  if (_salesRepHandlerIT && _salesRepHandlerIT[0]) {
    _salesRepHandlerIT[0].onChange();
  }

  if (_fiscalCodeUpdateHandlerIT && _fiscalCodeUpdateHandlerIT[0]) {
    // _fiscalCodeUpdateHandlerIT[0].onChange();
  }

  if (_custGrpIT && _custGrpIT[0]) {
    _custGrpIT[0].onChange();
  }

  var reqId = FormManager.getActualValue('reqId');
  if (reqType == 'C' && role == 'REQUESTER') {
    var checkImportIndc = getImportedIndcForItaly();
    // Story 1544496: Imported codes should not get overwritten
    if (checkImportIndc == 'Y') {
      console.log(">>importIndc is Y -Non editable fields- are ('clientTier', 'isuCd', 'repTeamMemberNo', 'salesBusOffCd','affiliate', 'specialTaxCd' , 'collectionCd','vat', 'taxCd1')");
      if (custSubType != undefined && custSubType != '' && (custSubType != '3PAIT' && custSubType != '3PASM' && custSubType != '3PAVA')) {
        FormManager.readOnly('isuCd');
        FormManager.readOnly('clientTier');
        // FormManager.readOnly('repTeamMemberNo');
        // FormManager.readOnly('salesBusOffCd');
        FormManager.readOnly('affiliate');
        FormManager.resetValidations('clientTier');
        FormManager.readOnly('inacCd');
      }
      if (custSubType == '3PAIT' || custSubType == '3PASM' || custSubType == '3PAVA') {
        FormManager.enable('inacCd');
      }
      FormManager.readOnly('identClient');

    } else {
      var custSubType = FormManager.getActualValue('custSubGrp');
      if (FormManager.getActualValue('isuCd') != null && "32" == FormManager.getActualValue('isuCd')) {
        if (custSubType == 'CROGO' || custSubType == 'CROUN' || custSubType == 'CROLC' || custSubType == 'CROCM' || custSubType == 'CROBP') {
          FormManager.setValue('collectionCd', 'CIT16');
          FormManager.readOnly('collectionCd');
        }
        if (custSubType == 'GOVSM' || custSubType == 'GOVVA' || custSubType == 'UNISM' || custSubType == 'UNIVA' || custSubType == 'LOCSM' || custSubType == 'LOCVA' || custSubType == 'COMSM'
            || custSubType == 'COMVA' || custSubType == 'BUSSM' || custSubType == 'BUSVA' || custSubType == '3PASM' || custSubType == '3PAVA' || custSubType == 'NGOSM' || custSubType == 'NGOVA'
            || custSubType == 'PRISM' || custSubType == 'PRIVA') {
          console.log("For SM/VA after config set default value of collection code CIT14");
          FormManager.setValue('collectionCd', 'CIT14');
          FormManager.readOnly('collectionCd');
        }
      }
    }
  }
  // CMR-1257
  if ('C' == FormManager.getActualValue('reqType')) {
    var _countryUse = '';
    var cntryRegion = FormManager.getActualValue('countryUse');
    var _landCntry = FormManager.getActualValue('landCntry');
    if (cntryRegion != '' && cntryRegion.length > 3) {
      _countryUse = cntryRegion.substring(3, 5);
    }
    if (_countryUse != '' && _landCntry != '' && (_landCntry == 'VA' || _landCntry == 'SM')) {
      FormManager.readOnly('landCntry');
    }
    if (_countryUse != '' && _landCntry != '' && _landCntry == 'VA') {
      FormManager.setValue('identClient', 'Y');
    }
  }

  if (role.toUpperCase() == 'REQUESTER' && (reqType == 'U' || reqType == 'X')) {
    var _countryUse = '';
    var cntryRegion = FormManager.getActualValue('countryUse');
    if (cntryRegion != '' && cntryRegion.length > 3) {
      _countryUse = cntryRegion.substring(3, 5);
    }
    if (_countryUse != '' && _countryUse == 'VA') {
      FormManager.readOnly('taxCd1');
      FormManager.readOnly('vat');
    }
  }
  addAfterTemplateLoadIT();
  // CMR-1363
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    FormManager.readOnly('cmrNo');
  }
}

/*
 * ITALY - sets Sales rep based on isuCTC
 */
var _importedSalesRep = '';
function setSalesRepValuesIT(isuCd, clientTier) {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  var checkImportIndc = getImportedIndcForItaly();

  if (checkImportIndc == 'Y' && _importedSalesRep == '') {
    _importedSalesRep = _pagemodel.repTeamMemberNo;
  }
  var custSubType = FormManager.getActualValue('custSubGrp');
  var isuCd = FormManager.getActualValue('isuCd');
  var clientTier = FormManager.getActualValue('clientTier');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var salesRep = FormManager.getActualValue('repTeamMemberNo');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var salesReps = [];
  if (clientTier != '') {
    qParams = {
      _qall : 'Y',
      ISSUING_CNTRY : cntry,
      ISU : '%' + isuCd + clientTier + '%',
    };
    results = cmr.query('GET.SRLIST.BYCTC', qParams);
    if (results != null && Object.keys(results).length > 0) {
      for (var i = 0; i < results.length; i++) {
        salesReps.push(results[i].ret1);
      }
      FormManager.enable('repTeamMemberNo');
      FormManager.limitDropdownValues(FormManager.getField('repTeamMemberNo'), salesReps);
      if (salesReps.length == 1) {
        FormManager.setValue('repTeamMemberNo', salesReps[0]);
      } else if (salesRep != null && salesRep != '') {
        FormManager.setValue('repTeamMemberNo', salesRep);
      }
    }
    if (custSubType == 'INTER' || custSubType == 'INTSM' || custSubType == 'INTVA' || custSubType == 'CROIN') {
      FormManager.setValue('isuCd', '21');
      FormManager.readOnly('clientTier', '');
      FormManager.limitDropdownValues(FormManager.getField('repTeamMemberNo'), [ '0B14A0', '0B1BA0', '0B1GA0', '0B1EA0', '0B11A0', '0B12A0', '0B13A0' ]);
    }
    if (custSubType == 'BUSPR' || custSubType == 'BUSSM' || custSubType == 'BUSVA' || custSubType == 'CROBP') {
      FormManager.setValue('isuCd', '21');
      FormManager.readOnly('clientTier', '');
      FormManager.resetDropdownValues(FormManager.getField('repTeamMemberNo'));
      FormManager.enable('repTeamMemberNo');
      FormManager.setValue('repTeamMemberNo', '09ZPA0');
      FormManager.limitDropdownValues(FormManager.getField('repTeamMemberNo'), [ '09ZPA0', '09ZPB0' ]);
    }
    if (isuCd != '32' && clientTier != 'S'
        && !(custSubType == 'BUSPR' || custSubType == 'BUSSM' || custSubType == 'BUSVA' || custSubType == 'CROBP' || (custSubType == 'PRISM' && role == "REQUESTER"))) {
      FormManager.enable('repTeamMemberNo');
    } else if ((isuCd == '32' && clientTier == 'S') && (role == "REQUESTER") && (checkImportIndc == 'N')) {
      FormManager.resetDropdownValues(FormManager.getField('repTeamMemberNo'));
      if (checkImportIndc == 'N') {
        autoSetSBOSROnPostalCode(false, false);
      } else {
        FormManager.setValue('repTeamMemberNo', _importedSalesRep);
        console.log("Kept imported sales rep value >>>" + _importedSalesRep);
      }
      FormManager.readOnly('repTeamMemberNo');
    } else if (isuCd != '32' && clientTier != 'S' && custSubType == 'PRISM' && role == "REQUESTER") {
      FormManager.readOnly('repTeamMemberNo');
    } else {
      FormManager.enable('repTeamMemberNo');
    }
  } else if (isuCd == '32') {
    FormManager.clearValue('repTeamMemberNo');
    FormManager.resetDropdownValues(FormManager.getField('repTeamMemberNo'));
  }
  setAffiliateEnterpriseRequired();
}

function showHideCityStateProvIT() {
  console.log("--->>> showHideCityStateProvIT for IT ---<<<");
  var landCntryVal = FormManager.getActualValue('landCntry');
  var addrType = FormManager.getActualValue('addrType');

  if (landCntryVal != 'IT') {
    FormManager.show('City1', 'city1');
    FormManager.hide('StateProv', 'stateProv');
    FormManager.resetValidations('stateProv');
    FormManager.show('StateProvItaly', 'stateProvItaly');
    FormManager.readOnly('stateProvItaly');

    FormManager.addValidator('city1', Validators.REQUIRED, [ 'City' ], null);
    FormManager.setValue('crossbCntryStateProvMapIT', '');
    FormManager.setValue('crossbCntryStateProvMapIT', landCntryVal);
    FormManager.setValue('stateProvItaly', getDescription('crossbCntryStateProvMapIT'));

    var stateProvIT = FormManager.getActualValue('stateProvItaly');
    FormManager.setValue('crossbStateProvPostalMapIT', stateProvIT);
    FormManager.setValue('poBoxPostCd', getDescription('crossbStateProvPostalMapIT'));
  } else {
    FormManager.addValidator('dropDownCity', Validators.REQUIRED, [ 'City' ], null);
    FormManager.hide('StateProvItaly', 'stateProvItaly');
    FormManager.hide('StateProvItalyOth', 'stateProvItalyOth');
    FormManager.resetValidations('stateProvItaly');
    FormManager.resetValidations('stateProvItalyOth');
  }
  if (addrType == 'ZS01' || FormManager.getField('addrType_ZS01').checked) {
    FormManager.show('StateProv', 'stateProv');
  }
  if (landCntryVal == 'IT') {
    FormManager.show('StateProv', 'stateProv');
  } else {
    FormManager.hide('StateProv', 'stateProv');
  }
}

/*
 * Author: Dennis T Natad Purpose: Function for displayedValue retrieval in the
 * case of select objects and similar objects.
 */
function getDescription(fieldId) {
  var field = dijit.byId(fieldId);
  return field.displayedValue;
}

function showHideOtherStateProvIT() {
  console.log(">>> showHideOtherStateProvIT for IT");
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
  console.log("--->>> autoSetAbbrevNmOnChanageIT >> running");
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
  console.log(">>> autoSetAbbrevLocnOnChangeIT >> running");
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

function autoSetSBOSROnPostalCode(clientTier, currPostCd) {
  var requestType = FormManager.getActualValue('reqType');
  var custGrp = FormManager.getActualValue('custGrp');
  var custSubType = FormManager.getActualValue('custSubGrp');
  var isuCode = FormManager.getActualValue('isuCd');
  var ctc = FormManager.getActualValue('clientTier');
  if (requestType != 'C') {
    return;
  }
  var reqId = FormManager.getActualValue('reqId');
  var result = cmr.query('VALIDATOR.POSTCODEIT', {
    REQID : reqId
  });

  var postCodeOrg = '';

  if (result != null && result.ret1 != undefined) {
    postCodeOrg = result.ret1;
  } else {
    postCodeOrg = '';
  }

  var postCode = parseInt(postCodeOrg.substring(0, 2));

  if (currPostCd && currPostCd != undefined && currPostCd != '' && currPostCd != postCodeOrg) {
    postCode = currPostCd.substring(0, 2);
  }

  // set collection code based on postalcode logic
  var checkImportIndc = getImportedIndcForItaly();
  if (checkImportIndc != 'Y') {
    if ((custGrp != 'CROSS') && postCodeOrg != '' && isuCode != '' && isuCode == '32' && ctc == 'S') {
      if (postCode >= 00 && postCode <= 04) {
        FormManager.setValue('collectionCd', 'CIT14');
      } else if (postCode == 10 || postCode == 11 || postCode == 28) {
        FormManager.setValue('collectionCd', 'CIT04');
      } else if (postCode > 11 && postCode <= 19) {
        FormManager.setValue('collectionCd', 'CIT04');
      } else if ((postCode >= 21 && postCode <= 24) || postCode == 27) {
        FormManager.setValue('collectionCd', 'CIT02');
      } else if (postCode == 23 || postCode == 25 || postCode == 26) {
        FormManager.setValue('collectionCd', 'CIT02');
      } else if (postCode == 20) {
        FormManager.setValue('collectionCd', 'CIT16');
      } else if ((postCode >= 30 && postCode <= 35) || postCode == 45) {
        FormManager.setValue('collectionCd', 'CIT19');
      } else if ((postCode >= 36 && postCode <= 39) || postCode == 46) {
        FormManager.setValue('collectionCd', 'CIT19');
      } else if ((postCode >= 40 && postCode <= 44) || postCode == 29 || postCode == 47 || postCode == 48) {
        FormManager.setValue('collectionCd', 'CIT03');
      } else if (postCode >= 50 && postCode <= 59) {
        FormManager.setValue('collectionCd', 'CIT03');
      } else if (postCode == 60 || postCode == 61) {
        FormManager.setValue('collectionCd', 'CIT03');
      } else if (postCode == 05 || postCode == 06) {
        FormManager.setValue('collectionCd', 'CIT14');
      } else if ((postCode >= 62 && postCode <= 67) || (postCode >= 70 && postCode <= 76) || (postCode >= 85 && postCode <= 89) || (postCode >= 7 && postCode <= 9)) {
        FormManager.setValue('collectionCd', 'CIT14');
      } else if (postCode >= 80 && postCode <= 84) {
        FormManager.setValue('collectionCd', 'CIT14');
      } else if (postCode >= 90 && postCode <= 98) {
        FormManager.setValue('collectionCd', 'CIT14');
      }
    } else if (isuCode != '32') {
      FormManager.clearValue('collectionCd');
    }

    if (postCodeOrg != '' && isuCode != '' && isuCode == '32' && ctc == 'S'
        && (custSubType == 'COMME' || custSubType == '3PAIT' || custSubType == 'NGOIT' || custSubType == 'GOVST' || custSubType == 'LOCEN' || custSubType == 'PRICU' || custSubType == 'UNIVE')) {
      if (postCode >= 00 && postCode <= 04) {
        FormManager.setValue('repTeamMemberNo', '09NCMM');
        FormManager.setValue('salesBusOffCd', 'NC');
      } else if (postCode == 10 || postCode == 11 || postCode == 28) {
        FormManager.setValue('repTeamMemberNo', '09NBMM');
        FormManager.setValue('salesBusOffCd', 'NB');
      } else if (postCode > 11 && postCode <= 19) {
        FormManager.setValue('repTeamMemberNo', '09GEMM');
        FormManager.setValue('salesBusOffCd', 'GE');
      } else if ((postCode >= 21 && postCode <= 24) || postCode == 27) {
        FormManager.setValue('repTeamMemberNo', '09NLMM');
        FormManager.setValue('salesBusOffCd', 'NL');
      } else if (postCode == 23 || postCode == 25 || postCode == 26) {
        FormManager.setValue('repTeamMemberNo', '09GJMM');
        FormManager.setValue('salesBusOffCd', 'GJ');
      } else if (postCode == 20) {
        FormManager.setValue('repTeamMemberNo', '09GHMM');
        FormManager.setValue('salesBusOffCd', 'GH');
      } else if ((postCode >= 30 && postCode <= 35) || postCode == 45) {
        FormManager.setValue('repTeamMemberNo', '09NFMM');
        FormManager.setValue('salesBusOffCd', 'NF');
      } else if ((postCode >= 36 && postCode <= 39) || postCode == 46) {
        FormManager.setValue('repTeamMemberNo', '09GKMM');
        FormManager.setValue('salesBusOffCd', 'GK');
      } else if ((postCode >= 40 && postCode <= 44) || postCode == 29 || postCode == 47 || postCode == 48) {
        FormManager.setValue('repTeamMemberNo', '09NIMM');
        FormManager.setValue('salesBusOffCd', 'NI');
      } else if ((postCode >= 50 && postCode <= 59) || (postCode == 61)) {
        FormManager.setValue('repTeamMemberNo', '09RPMM');
        FormManager.setValue('salesBusOffCd', 'RP');
      } else if (postCode == 60) {
        FormManager.setValue('repTeamMemberNo', '09CPMM');
        FormManager.setValue('salesBusOffCd', 'CP');
      } else if (postCode == 05 || postCode == 06) {
        FormManager.setValue('repTeamMemberNo', '09NAMM');
        FormManager.setValue('salesBusOffCd', 'NA');
      } else if ((postCode >= 62 && postCode <= 67) || (postCode >= 70 && postCode <= 76) || (postCode >= 85 && postCode <= 89) || (postCode >= 7 && postCode <= 9)) {
        FormManager.setValue('repTeamMemberNo', '09CPMM');
        FormManager.setValue('salesBusOffCd', 'CP');
      } else if (postCode >= 80 && postCode <= 84) {
        FormManager.setValue('repTeamMemberNo', '09NGMM');
        FormManager.setValue('salesBusOffCd', 'NG');
      } else if (postCode >= 90 && postCode <= 98) {
        FormManager.setValue('repTeamMemberNo', '09NMMM');
        FormManager.setValue('salesBusOffCd', 'NM');
      }
    }

    // CMR-1496 - set specific SR/SBO for VA/SM fresh creates
    var countryUse = FormManager.getActualValue('countryUse');
    if (countryUse && isuCode == '32' && ctc == 'S') {
      if (countryUse == '758VA') {
        FormManager.setValue('repTeamMemberNo', '09NCMM');
        FormManager.setValue('salesBusOffCd', 'NC');
      }
      if (countryUse == '758SM') {
        FormManager.setValue('repTeamMemberNo', '09NIMM');
        FormManager.setValue('salesBusOffCd', 'NI');
      }
    }
  }
}

function addCrossBorderValidatorForIT() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqType = FormManager.getActualValue('reqType');
        // only Create type will be validated
        if (reqType != 'C') {
          return new ValidationResult(null, true);
        }

        var scenario = FormManager.getActualValue('custGrp');
        if (scenario != null && scenario.includes('CRO')) {
          scenario = 'CROSS';
        }

        var cntryRegion = FormManager.getActualValue('countryUse');
        var landCntry = 'IT'; // default to Italy
        if (cntryRegion != '' && cntryRegion.length > 3) {
          landCntry = cntryRegion.substring(3, 5);
        }

        var reqId = FormManager.getActualValue('reqId');
        var defaultLandCntry = landCntry;
        var result = cmr.query('VALIDATOR.CROSSBORDERIT', {
          REQID : reqId
        });
        if (result != null && result.ret1 != '' && result.ret1 != undefined && defaultLandCntry != '' && result.ret1 != defaultLandCntry && scenario != 'CROSS') {
          return new ValidationResult(null, false, 'Landed Country value of the Bill-to (Billing) Address should be \'' + defaultLandCntry + '\' for Non Cross-Border customers.');
        } else if (result != null && result.ret1 != '' && result.ret1 != undefined && defaultLandCntry != '' && result.ret1 == defaultLandCntry && scenario == 'CROSS'
            && (cntryRegion.substring(3, 5) != 'SM' || cntryRegion.substring(3, 5) != 'VA')) {
          return new ValidationResult(null, false, 'Landed Country value of the Bill-to (Billing) Address should not be \'' + defaultLandCntry + '\' for Cross-Border customers.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function autoPopulateIdentClientIT() {
  var reqType = FormManager.getActualValue('reqType');
  var custSubType = FormManager.getActualValue('custSubGrp');
  var reqId = FormManager.getActualValue('reqId');
  var identClientValuesCross = [ 'N', 'Y' ];
  var identClientValuesLocal = [ 'A', 'B', 'C', 'D', 'X' ];
  // only Create type will be validated
  if (reqType != 'C') {
    return new ValidationResult(null, true);
  }
  var scenario = FormManager.getActualValue('custGrp');
  if (scenario != null && scenario.includes('CRO')) {
    scenario = 'CROSS';
  }

  var checkImportIndc = getImportedIndcForItaly();

  var cntryRegion = FormManager.getActualValue('countryUse');
  var landCntry = 'IT'; // default to Italy
  if (cntryRegion != '' && cntryRegion.length > 3) {
    landCntry = cntryRegion.substring(3, 5);
  }
  if (landCntry == 'SM' || landCntry == 'VA') {
    FormManager.setValue('identClient', 'N');
    FormManager.readOnly('identClient');
    FormManager.resetValidations('vat');
    FormManager.setValue('vat', '');
    FormManager.readOnly('vat');
    // FormManager.resetValidations('taxCd1');
    if (landCntry == 'VA') {
      FormManager.resetValidations('taxCd1');
      FormManager.setValue('taxCd1', '');
      FormManager.readOnly('taxCd1');
      FormManager.setValue('identClient', 'Y');
    }
    return;
  }

  if (scenario == 'CROSS') {
    FormManager.resetValidations('vat');
    FormManager.setValue('vat', '');
    FormManager.readOnly('vat');
    FormManager.resetValidations('taxCd1');
    FormManager.limitDropdownValues(FormManager.getField('identClient'), identClientValuesCross);
  } else if (scenario == 'LOCAL' && checkImportIndc != 'Y') {
    if (custSubType == 'COMME') {
      FormManager.limitDropdownValues(FormManager.getField('identClient'), [ 'A', 'D' ]);
    } else if (custSubType == 'BUSPR') {
      FormManager.limitDropdownValues(FormManager.getField('identClient'), [ 'A' ]);
    } else if (custSubType == 'PRICU') {
      FormManager.limitDropdownValues(FormManager.getField('identClient'), [ 'X' ]);
    } else if (custSubType == 'LOCEN' || custSubType == 'UNIVE' || custSubType == 'GOVST') {
      FormManager.limitDropdownValues(FormManager.getField('identClient'), [ 'B', 'C' ]);
    } else if (custSubType == 'NGOIT') {
      FormManager.limitDropdownValues(FormManager.getField('identClient'), [ 'B' ]);
    } else {
      FormManager.limitDropdownValues(FormManager.getField('identClient'), identClientValuesLocal);
    }
  } else if (scenario == 'LOCAL' && checkImportIndc == 'Y') {
    FormManager.limitDropdownValues(FormManager.getField('identClient'), identClientValuesLocal);
  } else {
    FormManager.resetDropdownValues(FormManager.getField('identClient'));
  }

  var result = cmr.query('VALIDATOR.CROSSBORDERIT', {
    REQID : reqId
  });

  /*
   * if (result != null && result.ret1 != '' && result.ret1 != undefined &&
   * cntryWhereVATIsMandatory.includes(result.ret1) && scenario == 'CROSS') {
   * FormManager.setValue('identClient', 'N'); } else if (result != null &&
   * result.ret1 != '' && result.ret1 != undefined &&
   * !cntryWhereVATIsMandatory.includes(result.ret1) && scenario == 'CROSS') {
   * FormManager.setValue('identClient', 'Y'); }
   */
  // Defect 1462395: Ident Client :Mukesh
  var custSubType = FormManager.getActualValue('custSubGrp');
  var fiscalCode = FormManager.getActualValue('taxCd1');
  var vat = FormManager.getActualValue('vat');
  if (custSubType != undefined && custSubType == 'LOCEN' && checkImportIndc != 'Y') {
    if ((fiscalCode != '' && fiscalCode.length == 11) && vat == '') {
      FormManager.setValue('identClient', 'B');
    }
    if ((fiscalCode != '' && fiscalCode.length == 11) && (vat != '' && vat.length == 13)) {
      FormManager.setValue('identClient', 'C');
    }
  }

  if (custSubType != undefined && custSubType == 'COMME' && checkImportIndc != 'Y') {
    if ((fiscalCode != '' && fiscalCode.length == 11) && (vat != '' && vat.length == 13)) {
      FormManager.setValue('identClient', 'A');
    }
    if ((fiscalCode != '' && fiscalCode.length == 16) && (vat != '' && vat.length == 13)) {
      FormManager.setValue('identClient', 'D');
    }
  }
}

function addCompanyAddrValidator() {
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

/*
 * function addCMRValidator(){ var role =
 * FormManager.getActualValue('userRole').toUpperCase(); var custSubType =
 * FormManager.getActualValue('custSubGrp'); if
 * (FormManager.getActualValue('reqType') == 'C'){
 * if(FormManager.getActualValue('findCmrResult') == 'NOT DONE' ||
 * FormManager.getActualValue('findCmrResult') == 'REJECTED')) { if (role ==
 * "REQUESTER" && (custSubType == '3PAIT' || custSubType == '3PASM' ||
 * custSubType == '3PAVA' || custSubType == 'CRO3P')) { return new
 * ValidationResult(null, false,'For 3rd party scenario please import a CMR via
 * CMR search'); } } } }
 */

function setVATForItaly() {
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  // only Create type will be validated
  if (reqType != 'C') {
    return new ValidationResult(null, true);

  }
  var ident = FormManager.getActualValue('identClient');
  var reqId = FormManager.getActualValue('reqId');
  /*
   * var euCntryList = [ 'AT', 'IT', 'BE', 'LV', 'BG', 'LT', 'HR', 'LU', 'CY',
   * 'MT', 'CZ', 'NL', 'DK', 'PL', 'EE', 'PT', 'FI', 'RO', 'FR', 'DE', 'SK',
   * 'SI', 'GR', 'ES', 'HU', 'SE', 'IE', 'GB' ]; var reqId =
   * FormManager.getActualValue('reqId'); var result =
   * cmr.query('VALIDATOR.CROSSBORDERIT', { REQID : reqId });
   * 
   * if (result != null && result != '' && result != undefined && result.ret1 !=
   * undefined && euCntryList.includes(result.ret1) && role == 'REQUESTER' &&
   * ident!='' && (ident!='N' || ident!='Y')) { FormManager.addValidator('vat',
   * Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB'); } else if (result != null &&
   * result != undefined && result.ret1 != undefined && result != '' &&
   * !euCntryList.includes(result.ret1) && role == 'REQUESTER') {
   * FormManager.resetValidations('vat'); FormManager.setValue('vat', '');
   * FormManager.readOnly('vat'); } else { FormManager.enable('vat'); }
   */
  // Defect 1475328: Scenarios: Local Enterprise, University, Government : VAT
  // is mandatory :Mukesh
  var checkImportIndc = getImportedIndcForItaly();

  var custSubType = FormManager.getActualValue('custSubGrp');
  if ((custSubType == 'LOCEN' || custSubType == 'UNIVE' || custSubType == 'GOVST') && ident != 'C' && checkImportIndc == 'N') {
    FormManager.resetValidations('vat');
    FormManager.enable('vat');
  }
  // Added new changs in defect :1475328
  if ((role == 'REQUESTER') && (custSubType == 'COMME' || custSubType == 'BUSPR') && checkImportIndc == 'N') {
    FormManager.enable('vat');
    FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
  }
  if ((role == 'PROCESSOR') && (custSubType == 'COMME' || custSubType == 'BUSPR') && checkImportIndc == 'N') {
    FormManager.resetValidations('vat');
    FormManager.enable('vat');
  }
  if ((custSubType == 'PRICU' || custSubType == 'PRISM' || custSubType == 'PRIVA') && role == 'REQUESTER' && checkImportIndc == 'N') { // Defect
    // 1496242
    FormManager.resetValidations('vat');
    FormManager.setValue('vat', '');
    FormManager.readOnly('vat');
  }
}

function canRemoveAddress(value, rowIndex, grid) {
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
  if (grid != null && rowIndex != null && grid.getItem(rowIndex) != null) {
    if (grid.getItem(rowIndex).addrType[0] == 'ZP01' && grid.getItem(rowIndex).landCntry[0] == 'GR') {
      return false;
    }
  }
  return true;
}

function ADDRESS_GRID_showCheck(value, rowIndex, grid) {
  return canRemoveAddress(value, rowIndex, grid);
}

function autoSetSBOSRForCompany() {
  var reqType = FormManager.getActualValue('reqType');
  if (reqType != 'C') {
    return new ValidationResult(null, true);
  }
  var addrType = FormManager.getActualValue('addrType');
  if (addrType != null && addrType == 'ZS01') {
    var currPostCd = FormManager.getActualValue('postCd');
    autoSetSBOSROnPostalCode(false, currPostCd);
  }
}

function disableHideFieldsOnAddrIT() {
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

function autoSetAddrFieldsForIT() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (cmr.addressMode == 'newAddress' || cmr.addressMode == 'copyAddress') {
    FormManager.hide('StreetAbbrev', 'streetAbbrev');
    FormManager.hide('AddrAbbrevName', 'addrAbbrevName');
    FormManager.hide('AddrAbbrevLocn', 'addrAbbrevLocn');
    FormManager.resetValidations('streetAbbrev');
    FormManager.resetValidations('addrAbbrevName');
    FormManager.resetValidations('addrAbbrevLocn');
    for (var i = 0; i < _addrTypesForIT.length; i++) {
      _addrTypeITHandler[i] = null;
      if (_addrTypeITHandler[i] == null) {
        _addrTypeITHandler[i] = dojo.connect(FormManager.getField('addrType_' + _addrTypesForIT[i]), 'onClick', function(value) {
          disableHideFieldsOnAddrIT();
        });
      }
    }

  }

  if (cmr.addressMode == 'updateAddress' && FormManager.getActualValue('addrType') == 'ZP01' && role == 'REQUESTER') {
    FormManager.show('StreetAbbrev', 'streetAbbrev');
    FormManager.show('AddrAbbrevName', 'addrAbbrevName');
    FormManager.show('AddrAbbrevLocn', 'addrAbbrevLocn');
    FormManager.readOnly('streetAbbrev');
    FormManager.readOnly('addrAbbrevName');
    FormManager.readOnly('addrAbbrevLocn');
  }

  if (cmr.addressMode == 'updateAddress' && FormManager.getActualValue('addrType') == 'ZP01' && role == 'PROCESSOR') {
    FormManager.show('StreetAbbrev', 'streetAbbrev');
    FormManager.show('AddrAbbrevName', 'addrAbbrevName');
    FormManager.show('AddrAbbrevLocn', 'addrAbbrevLocn');
    FormManager.enable('streetAbbrev');
    FormManager.enable('addrAbbrevName');
    FormManager.enable('addrAbbrevLocn');
  }

  if (cmr.addressMode == 'updateAddress' && FormManager.getActualValue('addrType') != 'ZP01') {
    FormManager.hide('StreetAbbrev', 'streetAbbrev');
    FormManager.hide('AddrAbbrevName', 'addrAbbrevName');
    FormManager.hide('AddrAbbrevLocn', 'addrAbbrevLocn');
    FormManager.resetValidations('streetAbbrev');
    FormManager.resetValidations('addrAbbrevName');
    FormManager.resetValidations('addrAbbrevLocn');
  }
}

function setStreetAbbrevIT() {
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
  var reqType = FormManager.getActualValue('reqType');
  /*
   * if (reqType != 'C') { return new ValidationResult(null, true); }
   */
  var addrType = FormManager.getActualValue('addrType');
  if (addrType != null && (addrType == 'ZP01' || FormManager.getField('addrType_ZP01').checked)) {
    FormManager.setValue('addrAbbrevLocn', FormManager.getActualValue('city1').substring(0, 12));
  }
}

function addressLoadHandlerIT(cntry, addressMode, saving, afterValidate) {
  if (saving) {
    return;
  }
  var reqType = FormManager.getActualValue('reqType');
  /*
   * if (reqType != 'C') { return new ValidationResult(null, true); }
   */

  if (_custNameITHandler == null) {
    _custNameITHandler = dojo.connect(FormManager.getField('custNm1'), 'onChange', function(value) {
      setAddrAbbrevNameIT();
    });
  }

  if (_custNameITHandler && _custNameITHandler[0]) {
    // _custNameITHandler[0].onChange();
  }

  if (_cityITHandler == null) {
    _cityITHandler = dojo.connect(FormManager.getField('city1'), 'onChange', function(value) {
      setAddrAbbrevLocnIT();
    });
  }

  if (_cityITHandler && _cityITHandler[0]) {
    // _cityITHandler[0].onChange();
  }

  if (_streetITHandler == null) {
    _streetITHandler = dojo.connect(FormManager.getField('addrTxt'), 'onChange', function(value) {
      setStreetAbbrevIT();
    });
  }

  if (_streetITHandler && _streetITHandler[0]) {
    // _streetITHandler[0].onChange();
  }
  /*
   * if (_dropDownCityHandler == null) { _dropDownCityHandler =
   * dojo.connect(FormManager.getField('dropDownCity'), 'onChange',
   * function(value) { setAddrAbbrevLocnIT(); }); }
   * 
   * if (_dropDownCityHandler && _dropDownCityHandler[0]) {
   * _dropDownCityHandler[0].onChange(); }
   */
}
/* End 1430539 */

/**
 * Override to not format for greece and cyprus
 */
function streetValueFormatter(value, rowIndex) {
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
// Defect 1459920: Enterprise number, Affiliate number :Mukesh
function setAffiliateEnterpriseRequired() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubType = FormManager.getActualValue('custSubGrp');
  var reqId = FormManager.getActualValue('reqId');
  if (FormManager.getActualValue('reqType') == 'C') {
    var checkImportIndc = getImportedIndcForItaly();
    if (checkImportIndc != 'Y') {
      var isu = FormManager.getActualValue('isuCd');
      FormManager.resetValidations('enterprise');
      FormManager.resetValidations('affiliate');
      FormManager.resetValidations('inacCd');
      if (custSubType == 'INTER' || custSubType == 'INTSM' || custSubType == 'INTVA' || custSubType == 'CROIN' || custSubType == 'PRISM' || custSubType == 'PRICU' || custSubType == 'PRIVA'
          || custSubType == 'CROPR') {
        FormManager.clearValue('enterprise');
        FormManager.clearValue('affiliate');
        FormManager.clearValue('inacCd');
        FormManager.readOnly('enterprise');
        FormManager.readOnly('affiliate');
      } else {
        if (isu == '21') {
          FormManager.clearValue('affiliate');
          FormManager.clearValue('inacCd');
          FormManager.enable('enterprise');
          FormManager.readOnly('affiliate');
          FormManager.readOnly('inacCd');
        } else if (isu == '32') {
          FormManager.enable('enterprise');
          FormManager.enable('affiliate');
          FormManager.enable('inacCd');
        } else {
          FormManager.addValidator('enterprise', Validators.REQUIRED, [ 'Enterprise Number/ CODICE ENTERPRISE' ], 'MAIN_IBM_TAB');
          FormManager.addValidator('affiliate', Validators.REQUIRED, [ 'Affiliate Number' ], 'MAIN_IBM_TAB');
          FormManager.enable('enterprise');
          FormManager.enable('affiliate');
          FormManager.enable('inacCd');
        }
      }
    } else if ('C' == FormManager.getActualValue('reqType') && checkImportIndc == 'Y') {
      if (custSubType == 'INTER' || custSubType == 'INTSM' || custSubType == 'INTVA' || custSubType == 'CROIN' || custSubType == 'BUSPR' || custSubType == 'BUSSM' || custSubType == 'BUSVA'
          || custSubType == 'CROBP') {
        FormManager.clearValue('inacCd');
        FormManager.clearValue('affiliate');
        FormManager.readOnly('inacCd');
        FormManager.readOnly('affiliate');
      } else if (role == "REQUESTER" && (custSubType == '3PAIT' || custSubType == '3PASM' || custSubType == '3PAVA' || custSubType == 'CRO3P')) {
        FormManager.enable('inacCd');
        FormManager.enable('affiliate');
        FormManager.readOnly('repTeamMemberNo');
      } else if (role == "REQUESTER" && !(custSubType == '3PAIT' || custSubType == '3PASM' || custSubType == '3PAVA' || custSubType == 'CRO3P')) {
        FormManager.readOnly('inacCd');
        // FormManager.readOnly('affiliate');
      } else {
        FormManager.enable('inacCd');
        FormManager.enable('affiliate');
      }
      if ((role != "REQUESTER") && (custSubType == 'PRISM' || custSubType == 'PRICU' || custSubType == 'PRIVA' || custSubType == 'CROPR')) {
        FormManager.enable('isuCd');
        FormManager.enable('clientTier');
      }
      if (role == "REQUESTER" && !(custSubType == 'INTER' || custSubType == 'INTSM' || custSubType == 'INTVA' || custSubType == 'CROIN')) {
        FormManager.readOnly('repTeamMemberNo');
      }
    }
  }
}

function getOldValuesIT(fromAddress, scenario, scenarioChanged) {
  if (scenarioChanged) {
    var custSubType = scenario;
    var reqId = FormManager.getActualValue('reqId');
    var checkImportIndc = getImportedIndcForItaly();
    if (_oldISUIT == "" && _oldCTCIT == "") {
      var result = cmr.query("GET.CMRINFO.IMPORTED", {
        REQ_ID : reqId
      });
      if (result != null && result.ret1 != '' && result.ret1 != null) {
        _oldISUIT = result.ret1;
        _oldCTCIT = result.ret2;
        _oldSalesRepIT = result.ret3;
        _oldSBOIT = result.ret4;
        _oldINACIT = result.ret6;
        _oldSpecialTaxCdIT = result.ret7;
        _oldAffiliateIT = result.ret8;
      }
    }

    // Story 1594125: Company number field should be locked
    FormManager.readOnly('company');
    // Defect 1553451
    var role = FormManager.getActualValue('userRole').toUpperCase();
    if (FormManager.getActualValue('reqType') == 'C' && role == 'REQUESTER') {
      if ((custSubType != undefined && FormManager.getActualValue('isuCd') != undefined)
          && (custSubType == 'COMME' || custSubType == 'COMSM' || custSubType == 'COMVA' || custSubType == 'CROCM' || custSubType == 'NGOIT' || custSubType == 'NGOSM' || custSubType == 'NGOVA'
              || custSubType == 'PRICU' || custSubType == 'PRISM' || custSubType == 'PRIVA') && (FormManager.getActualValue('isuCd') == '32') && FormManager.getField('clientTier')) {
        FormManager.readOnly('repTeamMemberNo');
        FormManager.readOnly('salesBusOffCd');
      }
      // Defect 1556300 (Old Defect 1526869)
      if (custSubType == 'GOVST' || custSubType == 'GOVSM' || custSubType == 'GOVVA' || custSubType == 'CROGO' || custSubType == 'LOCEN' || custSubType == 'LOCSM' || custSubType == 'LOCVA'
          || custSubType == 'CROLC' || custSubType == 'UNIVE' || custSubType == 'UNISM' || custSubType == 'UNIVA' || custSubType == 'CROUN') {
        FormManager.addValidator('repTeamMemberNo', Validators.REQUIRED, [ 'Sales Rep' ], 'MAIN_IBM_TAB');
      } else {
        FormManager.resetValidations('repTeamMemberNo');
      }

      if (FormManager.getActualValue('isuCd') != null
          && "32" != FormManager.getActualValue('isuCd')
          && (custSubType == 'GOVSM' || custSubType == 'GOVVA' || custSubType == 'CROGO' || custSubType == 'UNISM' || custSubType == 'UNIVA' || custSubType == 'CROUN' || custSubType == 'LOCSM'
              || custSubType == 'LOCVA' || custSubType == 'CROLC' || custSubType == 'COMSM' || custSubType == 'COMVA' || custSubType == 'CROCM' || custSubType == 'BUSSM' || custSubType == 'BUSVA'
              || custSubType == 'CROBP' || custSubType == '3PASM' || custSubType == '3PAVA' || custSubType == 'NGOSM' || custSubType == 'NGOVA' || custSubType == 'PRISM' || custSubType == 'PRIVA')) {
        FormManager.setValue('collectionCd', '');
        FormManager.enable('collectionCd');
        console.log("isu!=32 , collection code -blank and locked for the requester for CB/SM or VA ");
      }

      // Story 1544496: Imported codes should not get overwritten
      if (checkImportIndc == 'Y') {
        console
            .log(">getOldValuesIT> importIndc is Y -Non editable fields- are ('clientTier', 'isuCd', 'repTeamMemberNo', 'salesBusOffCd', 'inacCd','affiliate', 'specialTaxCd' , 'collectionCd','vat', 'taxCd1')");
        if (custSubType != undefined && custSubType != '' && (custSubType != '3PAIT' && custSubType != '3PASM' && custSubType != '3PAVA')) {
          FormManager.readOnly('isuCd');
          FormManager.readOnly('clientTier');
          FormManager.readOnly('affiliate');
          FormManager.resetValidations('clientTier');
          FormManager.readOnly('inacCd');
        }
        if (FormManager.getActualValue('isuCd') != null && "32" == FormManager.getActualValue('isuCd')) {
          if (custSubType == 'CROGO' || custSubType == 'CROUN' || custSubType == 'CROLC' || custSubType == 'CROCM' || custSubType == 'CROBP') {
            // console.log("For CB getOld set default value of collection code
            // CIT16");
            FormManager.setValue('collectionCd', 'CIT16');
            FormManager.readOnly('collectionCd');
          }
          if (custSubType == 'GOVSM' || custSubType == 'GOVVA' || custSubType == 'UNISM' || custSubType == 'UNIVA' || custSubType == 'LOCSM' || custSubType == 'LOCVA' || custSubType == 'COMSM'
              || custSubType == 'COMVA' || custSubType == 'BUSSM' || custSubType == 'BUSVA' || custSubType == '3PASM' || custSubType == '3PAVA' || custSubType == 'NGOSM' || custSubType == 'NGOVA'
              || custSubType == 'PRISM' || custSubType == 'PRIVA') {
            console.log("For SM/VA getOld set default value of collection code CIT14");
            FormManager.setValue('collectionCd', 'CIT14');
            FormManager.readOnly('collectionCd');
          }
        }
      }
    }
    if (checkImportIndc == 'Y') {
      if (custSubType == '3PAIT' || custSubType == '3PASM' || custSubType == '3PAVA' || custSubType == 'CRO3P') {
        if (custSubType != 'CRO3P') {
          FormManager.enable('inacCd');
        }
        FormManager.enable('isuCd');
        FormManager.enable('clientTier');
        FormManager.enable('specialTaxCd');
      }
      FormManager.readOnly('identClient');
      // if imported and scenario is 3rd person
      if (!(custSubType == 'INTER' || custSubType == 'INTSM' || custSubType == 'INTVA' || custSubType == 'CROIN' || custSubType == 'BUSPR' || custSubType == 'BUSSM' || custSubType == 'BUSVA' || custSubType == 'CROBP')) {
        FormManager.setValue('isuCd', _oldISUIT);
        FormManager.setValue('clientTier', _oldCTCIT);
        FormManager.setValue('repTeamMemberNo', _oldSalesRepIT);
        FormManager.setValue('salesBusOffCd', _oldSBOIT);
        FormManager.setValue('inacCd', _oldINACIT);
        FormManager.setValue('affiliate', _oldAffiliateIT);
      } else {
        FormManager.clearValue('affiliate');
      }
      FormManager.setValue('specialTaxCd', _oldSpecialTaxCdIT);
    }
  }
}

// Blanking Out Collection Code Based On ISU and CTC and Scenario
function blankedOutCollectionCD() {
  console.log("---->> blankedOutCollectionCD. <<----");
  // var custSubType = FormManager.getActualValue('custSubGrp');
  // var role = FormManager.getActualValue('userRole').toUpperCase();
  var checkImportIndc = getImportedIndcForItaly();
  var reqType = FormManager.getActualValue('reqType');
  var isuCd = FormManager.getActualValue('isuCd');
  var ctc = FormManager.getActualValue('clientTier');
  if (checkImportIndc != 'Y' && reqType == 'C') {
    FormManager.resetValidations('collectionCd');
    if (isuCd == '32' && ctc == 'S') {
      FormManager.readOnly('collectionCd');
      autoSetSBOSROnPostalCode();
    } else {
      FormManager.enable('collectionCd');
      FormManager.clearValue('collectionCd');
    }
  }
}

function setMRCOnScenariosIT() {
  var requestType = FormManager.getActualValue('reqType');
  if (requestType != 'C') {
    return new ValidationResult(null, true);
  }
  var custSubType = FormManager.getActualValue('custSubGrp');
  var isuCode = FormManager.getActualValue('isuCd');
  if (custSubType == 'BUSPR' || custSubType == 'BUSSM' || custSubType == 'BUSVA' || custSubType == 'CROBP') {
    FormManager.setValue('mrcCd', '5');
  } else if (custSubType != null && isuCode != null && isuCode == '34' && (custSubType == 'COMME' || custSubType == 'COMSM' || custSubType == 'COMVA' || custSubType == 'CROCM')) {
    FormManager.setValue('mrcCd', 'M');
  } else {
    FormManager.setValue('mrcCd', '2');
  }
}

function checkIfVATFiscalUpdatedIT() {
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

/*
 * function doValidateFiscalData() { var currentFiscalCode =
 * FormManager.getActualValue('taxCd1'); var currentVAT =
 * FormManager.getActualValue('vat'); var currentCMRNo =
 * FormManager.getActualValue('cmrNo'); var currentCompanyNo =
 * FormManager.getActualValue('company'); if (currentCMRNo != '') { qParams = {
 * _qall : 'Y', VAT : currentVAT, FISCAL_CD : currentFiscalCode, MANDT :
 * FormManager.getActualValue('mandt'), }; var result =
 * cmr.query('GET.RDC_FISCAL_DATA', qParams); if (result != null &&
 * result.length > 1) { if (dijit.byId('disableAutoProc')) {
 * FormManager.getField('disableAutoProc').set('checked', true); }
 * FormManager.disable('disableAutoProc');
 * FormManager.setValue('fiscalDataStatus', 'A'); cmr.showAlert("More than 1 CMR
 * exists in RDC with the current VAT and fiscal code.", "Warning"); return; }
 * if (result != null && result.length == 1) { var rdcCmrNo = result[0].ret1;
 * var rdcVATValue = result[0].ret2; var rdcFiscalCode = result[0].ret3; } if
 * (result != null && rdcCmrNo > currentCMRNo) { console.log('Request has old
 * data'); FormManager.setValue('fiscalDataStatus', 'O');
 * FormManager.setValue('fiscalDataCompanyNo', rdcCmrNo);
 * FormManager.setValue('taxCd1', rdcFiscalCode); FormManager.setValue('vat',
 * rdcVATValue); FormManager.setValue('company', rdcCmrNo);
 * 
 * 
 * if (dijit.byId('disableAutoProc')) {
 * FormManager.getField('disableAutoProc').set('checked', true); }
 * FormManager.disable('disableAutoProc');
 * 
 * 
 * if (dijit.byId('disableAutoProc')) {
 * FormManager.getField('disableAutoProc').set('checked', false); }
 * FormManager.enable('disableAutoProc');
 * 
 * cmr.showAlert("Request has Old Data. Fiscal Data fields values has been
 * updated to newer values on Request.", "Warning"); } else if (result != null &&
 * rdcCmrNo < currentCMRNo) { console.log('Request has New data');
 * FormManager.setValue('fiscalDataStatus', 'N');
 * FormManager.setValue('fiscalDataCompanyNo', rdcCmrNo);
 * 
 * 
 * if (dijit.byId('disableAutoProc')) {
 * FormManager.getField('disableAutoProc').set('checked', true); }
 * FormManager.disable('disableAutoProc');
 * 
 * 
 * if (dijit.byId('disableAutoProc')) {
 * FormManager.getField('disableAutoProc').set('checked', false); }
 * FormManager.enable('disableAutoProc');
 * 
 * cmr.showAlert("Request has Newer Data. Old CMR=" + rdcCmrNo, "Warning"); }
 * else { console.log("No action needed as data on request and RDC is same or no
 * record found on KNA1 with current vat and fiscal code.");
 * FormManager.setValue('fiscalDataStatus', 'U'); if
 * (dijit.byId('disableAutoProc')) {
 * FormManager.getField('disableAutoProc').set('checked', false); }
 * FormManager.enable('disableAutoProc'); cmr.showAlert("No action needed as
 * data on request and RDC is same or no record found on KNA1 with current vat
 * and fiscal code.", "Warning"); } } else { cmr.showAlert("Fiscal Data cannot
 * be validated as CMR number is blank", "Warning"); } }
 */

// Defect 1470471: Expedite Reason not blanked out when Expedite flag changed
// from YES to NO :Mukesh
function setExpediteReasonOnChange() {
  dojo.connect(FormManager.getField('expediteInd'), 'onChange', function(value) {
    if (FormManager.getActualValue('viewOnlyPage') == 'true') {
      return;
    }
    if (FormManager.getActualValue('expediteInd') == 'N') {
      FormManager.clearValue('expediteReason');
    }
  });
}

function validateVATFiscalLengthOnIdentIT() {
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

function setVATOnIdentClientChangeIT() {
  var requestType = FormManager.getActualValue('reqType');
  if (requestType != 'C') {
    return new ValidationResult(null, true);
  }

  var role = FormManager.getActualValue('userRole').toUpperCase();

  var ident = FormManager.getActualValue('identClient');
  if (ident != '' && (ident == 'A' || ident == 'D') && role == 'REQUESTER') {
    FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
  }
  if (ident != '' && (ident == 'A' || ident == 'D') && role == 'PROCESSOR') {
    FormManager.resetValidations('vat');
  }
  if (ident != '' && ident == 'B') {
    FormManager.resetValidations('vat');
  }
  if (ident != '' && ident == 'C' && role == 'PROCESSOR') {
    FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
  }
  if (ident != '' && ident == 'C' && role == 'REQUESTER') {
    FormManager.resetValidations('vat');
  }

  if (ident != '' && (ident == 'X' || ident == 'N' || ident == 'Y')) {
    FormManager.resetValidations('vat');
    FormManager.setValue('vat', '');
    FormManager.readOnly('vat');
  }

  var checkImportIndc = getImportedIndcForItaly();
  if (checkImportIndc != 'Y' && ident != '' && (ident == 'A' || ident == 'C')) {
    FormManager.addValidator('taxCd1', Validators.REQUIRED, [ 'Fiscal Code' ], 'MAIN_CUST_TAB');
    FormManager.enable('taxCd1');
    FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
    FormManager.enable('vat');
  }

}

function setFiscalCodeOnIdentClientCB_IT() {
  var ident = FormManager.getActualValue('identClient');
  if (FormManager.getActualValue('reqType') == 'C' && 'CROSS' == FormManager.getActualValue('custGrp')) {
    var reqId = FormManager.getActualValue('reqId');
    var checkImportIndc = getImportedIndcForItaly();

    if (checkImportIndc != 'Y') {
      FormManager.enable('identClient');
      if (ident == 'N') {
        FormManager.addValidator('taxCd1', Validators.REQUIRED, [ 'Fiscal Code' ], 'MAIN_CUST_TAB');
        FormManager.enable('taxCd1');
      } else if (ident == 'Y') {
        FormManager.resetValidations('taxCd1');
        FormManager.setValue('taxCd1', '');
        FormManager.readOnly('taxCd1');
      }
    }
  }
}
// Defect 1532076 : Mukesh
function validateEnterpriseNumForIT() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var enterprise = FormManager.getActualValue('enterprise');
        var letterNumber = /^[0-9a-zA-Z]+$/;
        // Story 1592370
        if (enterprise != '') {
          if (enterprise.length == 1 && enterprise != '#') {
            return new ValidationResult(null, false, 'Enterprise Number/ CODICE ENTERPRISE should be # only.');
          }
          if (enterprise.length > 1 && enterprise.length != 6) {
            return new ValidationResult(null, false, 'Enterprise Number/ CODICE ENTERPRISE should be 6 characters long.');
          }

          if (enterprise.length > 1 && !enterprise.match(letterNumber)) {
            return new ValidationResult({
              id : 'enterprise',
              type : 'text',
              name : 'enterprise'
            }, false, 'Enterprise Number/ CODICE ENTERPRISE should be alpha numeric.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function useCountryScenarioRules(scenario, name, value) {
  var cty = FormManager.getActualValue('cmrIssuingCntry');
  if (cty != '758') {
    return false;
  }
  // Defect 1579935
  // italy do not override values for ISU, CTC, SBO, SR
  var reqId = FormManager.getActualValue('reqId');

  var importIndc = getImportedIndcForItaly();
  var custSubType = FormManager.getActualValue('custSubGrp');

  if (importIndc == 'N') {
    return false;
  }
  // ISU, CTC, INAC, SBO, Sales Rep, Affiliate
  var noOverrides = [ 'clientTier', 'isuCd', 'repTeamMemberNo', 'salesBusOffCd', 'inacCd', 'affiliate', 'identClient' ];
  if (custSubType != undefined && custSubType != '' && (custSubType == '3PAIT' || custSubType == '3PASM' || custSubType == '3PAVA')) {
    noOverrides = [ 'identClient' ];
  }
  if (noOverrides.indexOf(name) >= 0) {
    var currVal = FormManager.getActualValue(name);
    if (currVal != '') {
      return true;
    }
  }
  return false;
}

function setStateProvReqdPostalCodeIT() {
  var postCode = FormManager.getActualValue('postCd');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var landCnty = FormManager.getActualValue('landCntry');
  var addrType = FormManager.getActualValue('addrType');
  if (postCode != '' && postCode.length >= 2 && landCnty == 'IT') {
    postCode = postCode.substring(0, 2);
    postCode = postCode.trim() + '%';
    qParams = {
      _qall : 'Y',
      TXT : postCode,
    };
    var results = cmr.query('GETSPVALFORPOSTCDIT', qParams);
    if (results != null) {
      if (role == 'REQUESTER' && results.length > 1 && (addrType != 'ZS01' || !FormManager.getField('addrType_ZS01').checked)) {
        FormManager.addValidator('stateProv', Validators.REQUIRED, [ 'State/Province' ], null);
      }
      if (results.length == 1 && role == 'REQUESTER' && (addrType != 'ZS01' || !FormManager.getField('addrType_ZS01').checked)) {
        FormManager.resetValidations('stateProv');
      }
    }
  }
}

/*
 * ITALY - sets SBO/IBO based on Sales rep
 */
function autoSetSBOOnSRValueIT() {
  var reqType = FormManager.getActualValue('reqType');
  var scenario = FormManager.getActualValue('custSubGrp');
  if (reqType != 'C' || scenario == 'CROCM' || scenario == 'CROGO' || scenario == 'CROPR') {
    return;
  }
  var sbo = FormManager.getActualValue('salesBusOffCd');
  var salesRep = FormManager.getActualValue('repTeamMemberNo');
  if (salesRep != undefined && salesRep != '' && salesRep.length >= 4) {
    sbo = salesRep.substring(2, 4);
  }
  if (sbo != '' && sbo.length == 2 && !(FormManager.getActualValue('salesBusOffCd') == sbo || FormManager.getActualValue('salesBusOffCd') == '0' + sbo + 'B000')) {
    FormManager.setValue('salesBusOffCd', sbo);
    FormManager.readOnly('salesBusOffCd');
    console.log('setting SBO=' + sbo);
  }
  if (salesRep == '') {
    FormManager.clearValue('salesBusOffCd');
  }
}

function addInstallingAddrValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var zs01ReqId = FormManager.getActualValue('reqId');
        qParams = {
          REQ_ID : zs01ReqId,
        };
        var record = cmr.query('GETZS01VALRECORDS', qParams);
        var zs01Reccount = record.ret1;
        if (Number(zs01Reccount) > 1) {
          return new ValidationResult(null, false, 'Only one Installing Address can be defined.');
        } else if (Number(zs01Reccount == 0)) {
          return new ValidationResult(null, false, 'At least one Installing Address must be defined.');
        } else {
          return new ValidationResult(null, true);
        }

      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function checkIfStateProvBlankForProcIT() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var requestType = FormManager.getActualValue('reqType');
        var landCountry = FormManager.getActualValue('landCountry');
        var role = FormManager.getActualValue('userRole').toUpperCase();
        if (requestType != 'C') {
          return new ValidationResult(null, true);
        }
        if (landCountry != 'IT') {
          return new ValidationResult(null, true);
        }
        if (role == 'PROCESSOR') {
          var qParams = {
            REQ_ID : FormManager.getActualValue('reqId'),
          };
          var results = cmr.query('GETZP01STATEPROV', qParams);
          if (results != null && results.ret1 != undefined) {
            if (results.ret1 == '') {
              return new ValidationResult(null, false, 'State Prov cannot be blank for the Billing address');
            }

            var results = cmr.query('GETZI01STATEPROV', qParams);
            if (results != null && results.ret1 != undefined) {
              if (results.ret1 == '') {
                return new ValidationResult(null, false, 'State Prov cannot be blank for the Company address');
              }
            }
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function autoResetFiscalDataStatus() {
  var requestType = FormManager.getActualValue('reqType');
  if (requestType != 'U') {
    return new ValidationResult(null, true);
  }
  var currentFiscalCode = FormManager.getActualValue('taxCd1');
  var currentVAT = FormManager.getActualValue('vat');
  qParams = {
    REQ_ID : FormManager.getActualValue('reqId'),
  };
  var result = cmr.query('GET.OLD_FISCAL_DATA', qParams);
  var oldVAT = result.ret1;
  var oldFiscalCode = result.ret2;
  if (oldFiscalCode != currentFiscalCode || oldVAT != currentVAT) {
    console.log('Setting fiscalDataStatus and fiscalDataCompanyNo to blank');
    FormManager.setValue('fiscalDataStatus', '');
    FormManager.setValue('fiscalDataCompanyNo', '');
  } else {
    console.log("Retaining old values for company, enterprise and identclient: " + _pagemodel.company + ", " + _pagemodel.enterprise + ", " + _pagemodel.identClient);
    FormManager.setValue('company', _pagemodel.company);
    FormManager.setValue('enterprise', _pagemodel.enterprise);
    FormManager.setValue('identClient', _pagemodel.identClient);
  }
}

function disableAutoProcessingOnFiscalUpdate() {
  var requestType = FormManager.getActualValue('reqType');
  if (requestType != 'U' || requestType != 'X') {
    return new ValidationResult(null, true);
  }
  console.log('Setting disable auto processing Y and readonly');
  if (FormManager.getActualValue('fiscalDataStatus') != '' && FormManager.getActualValue('fiscalDataStatus') == 'A') {
    if (dijit.byId('disableAutoProc')) {
      FormManager.getField('disableAutoProc').set('checked', true);
    }
    FormManager.disable('disableAutoProc');
  } else {
    if (dijit.byId('disableAutoProc')) {
      FormManager.getField('disableAutoProc').set('checked', false);
    }
    FormManager.enable('disableAutoProc');
  }

}

function allowCopyOfNonExistingAddressOnly(cntry, addressMode, saving, afterValidate) {
  if (saving) {
    return;
  }

  if (cmr.addressMode == 'copyAddress') {
    var requestId = FormManager.getActualValue('reqId');
    var requestType = FormManager.getActualValue('reqType');
    if (requestType != 'C') {
      return new ValidationResult(null, true);
    }
    qParams = {
      REQ_ID : requestId,
    };
    var record1 = cmr.query('GETZS01VALRECORDS', qParams);
    var zs01Reccount = record1.ret1;
    if (Number(zs01Reccount) == 1) {
      cmr.hideNode('radiocont_ZS01');
    } else {
      cmr.showNode('radiocont_ZS01');
    }
    var record2 = cmr.query('GETZI01VALRECORDS', qParams);
    var zi01Reccount = record2.ret1;
    if (Number(zi01Reccount) == 1) {
      cmr.hideNode('radiocont_ZI01');
    } else {
      cmr.showNode('radiocont_ZI01');
    }
    var record3 = cmr.query('GETZP01VALRECORDS', qParams);
    var zp01Reccount = record3.ret1;
    if (Number(zp01Reccount) == 1) {
      cmr.hideNode('radiocont_ZP01');
    } else {
      cmr.showNode('radiocont_ZP01');
    }
  }
}

function stateProvValidatorCBforIT(cntry, addressMode, saving, afterValidate) {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var landCntryVal = FormManager.getActualValue('landCntry');
        var addrType = FormManager.getActualValue('addrType');
        if (landCntryVal != 'IT' && addrType != 'ZS01') {
          var expectedStateProv = getDescription('crossbCntryStateProvMapIT');
          var currentStateProv = FormManager.getActualValue('stateProvItaly');
          if (expectedStateProv != undefined && currentStateProv != undefined && expectedStateProv != '' && currentStateProv != '' && expectedStateProv != currentStateProv) {
            return new ValidationResult(null, false, 'Invalid value for State/Prov. Please select ' + expectedStateProv);
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function setCodFlagVal() {
  FormManager.setValue('codFlag', '3');
  FormManager.readOnly('codFlag');
}

function addSBOSRLogicIE() {
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
      ISU_CD : '%' + _isuCd + '%',
      CLIENT_TIER : '%' + _clientTier + '%',
    };
    var results = cmr.query('GET.SALESREP.IRELAND', qParams);
    if (results != null) {
      for (var i = 0; i < results.length; i++) {
        salesRepValue.push(results[i].ret1);
      }

      FormManager.limitDropdownValues(FormManager.getField('repTeamMemberNo'), salesRepValue);
      if (salesRepValue.length == 1) {
        FormManager.setValue('repTeamMemberNo', salesRepValue[0]);
      }
    }
  }
}

function autoSetISUClientTierUK() {
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
      FormManager.setValue('clientTier', '');
    }
  }
}

function autoSetAbbrevLocUKI() {
  var _custType = FormManager.getActualValue('custSubGrp');
  var _custGrp = FormManager.getActualValue('custGrp');
  var _zs01ReqId = FormManager.getActualValue('reqId');
  var _abbrevLocn = null;
  var _addrType = null;
  var _result = null;
  if (_custGrp == 'CROSS') {
    if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.IRELAND) {
      _addrType = 'ZS01';
    } else if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.UK) {
      _addrType = 'ZI01';
    }
    var qParams = {
      REQ_ID : _zs01ReqId,
      ADDR_TYPE : _addrType,
    };

    _result = cmr.query('ADDR.GET.LANDCNTRY.BY_REQID_ADDRTYP', qParams);
    _abbrevLocn = _result.ret1;
  } else {
    if (_custType == 'SOFTL') {
      _abbrevLocn = "SOFTLAYER";
    } else {
      var _zs01ReqId = FormManager.getActualValue('reqId');
      var _addrType = 'ZI01';
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
}
function islandedCountry(landCntry) {
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

function optionalRuleForVatUK() {

  var reqType = FormManager.getActualValue('reqType');
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

function autoSetABLocnAddr(_addrType) {
  var _custType = FormManager.getActualValue('custSubGrp');
  var _custGrp = FormManager.getActualValue('custGrp');
  var _abbrevLocn = null;
  var _result = null;
  if (_custGrp == 'CROSS') {
    if (_addrType == 'ZS01' && (_custType == 'XBSPR' || _custType == 'XGOVR' || _custType == 'XPRIC' || _custType == 'CROSS' || _custType == 'XIGF')) {
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

function toggleBPRelMemType() {
  var reqType = null;
  var role = null;
  if (typeof (_pagemodel) != 'undefined') {
    reqType = FormManager.getActualValue('reqType');
    role = _pagemodel.userRole.toUpperCase();
  }
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (reqType == 'C') {
    var _custType = FormManager.getActualValue('custSubGrp');
    if (_custType == 'BUSPR' || _custType == 'BUSSM' || _custType == 'BUSVA' || _custType == 'CROBP') {
      FormManager.show('PPSCEID', 'ppsceid');
      FormManager.addValidator('ppsceid', Validators.REQUIRED, [ 'PPS CEID' ], 'MAIN_IBM_TAB');
      FormManager.show('MembLevel', 'memLvl');
      FormManager.show('BPRelationType', 'bpRelType');
      FormManager.resetValidations('bpRelType');
      FormManager.resetValidations('memLvl');
      FormManager.readOnly('bpRelType');
      FormManager.readOnly('memLvl');
      FormManager.addValidator('memLvl', Validators.REQUIRED, [ 'Membership Level' ], 'MAIN_IBM_TAB');
      FormManager.addValidator('bpRelType', Validators.REQUIRED, [ 'BP Relation Type' ], 'MAIN_IBM_TAB');
    } else {
      FormManager.resetValidations('ppsceid');
      FormManager.hide('PPSCEID', 'ppsceid');
      FormManager.hide('MembLevel', 'memLvl');
      FormManager.hide('BPRelationType', 'bpRelType');
      FormManager.removeValidator('ppsceid', Validators.REQUIRED);
      FormManager.removeValidator('memLvl', Validators.REQUIRED);
      FormManager.removeValidator('bpRelType', Validators.REQUIRED);
    }
  } else if ((reqType == 'U' || reqType == 'X') && role == 'REQUESTER') {
    FormManager.readOnly('ppsceid');
    FormManager.resetValidations('ppsceid');
    FormManager.readOnly('memLvl');
    FormManager.resetValidations('memLvl');
    FormManager.readOnly('bpRelType');
    FormManager.resetValidations('bpRelType');
  } else if ((reqType == 'U' || reqType == 'X') && role == 'PROCESSOR') {
    FormManager.enable('ppsceid');
    if (FormManager.getActualValue('ppsceid') != '') {
      FormManager.addValidator('ppsceid', Validators.REQUIRED, [ 'PPS CEID' ], 'MAIN_IBM_TAB');
    } else {
      FormManager.removeValidator('ppsceid', Validators.REQUIRED);
    }
  }
}

var _addrTypesForEMEA = [ 'ZD01', 'ZP01', 'ZI01', 'ZS01', 'ZS02' ];

function validateFiscalCodeForCreatesIT() {
  console.log("register FiscalCode validator for Creates IT . . .");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var role = FormManager.getActualValue('userRole');
        var taxCd1 = FormManager.getActualValue('taxCd1');
        var reqId = FormManager.getActualValue('reqId');
        var checkImportIndc = getImportedIndcForItaly();
        if (FormManager.getActualValue('reqType') == 'C' && checkImportIndc == 'N') {
          if ((taxCd1 != null && taxCd1.length != 0)) {
            qParams = {
              FISCAL_CODE : FormManager.getActualValue('taxCd1'),
              MANDT : cmr.MANDT
            }
            var result = cmr.query('IT.CHECK.DUPLICATE_FISCAL_ADDRESS', qParams);
            if (result.ret1 != null && result.ret1 != '') {
              return new ValidationResult({
                id : 'taxCd1',
                type : 'text',
                name : 'taxCd1'
              }, false, 'Fiscal Code on the request is already available on CMR No. ' + result.ret1 + '. Please mention another new fiscal code or import the CMR into the request.');
            }
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function validateLandCntryForCBCreatesIT() {
  console.log("register Landed country validator with new requirement for IT . . .");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('reqType') == 'C') {
          var scenario = FormManager.getActualValue('custGrp');
          var reqId = FormManager.getActualValue('reqId');
          // Billing Address
          var landCntryZP01 = cmr.query('ADDR.GET.LAND_CNTRY.BY_REQID', {
            REQ_ID : reqId,
            ADDR_TYPE : 'ZP01'
          });
          // Company Address
          var landCntryZI01 = cmr.query('ADDR.GET.LAND_CNTRY.BY_REQID', {
            REQ_ID : reqId,
            ADDR_TYPE : 'ZI01'
          });

          if (scenario == 'CROSS') {
            console.log("Italy Cross boarder Scenario......");

            if (landCntryZP01.ret1 != null && landCntryZI01.ret1 != null && landCntryZP01.ret1 != landCntryZI01.ret1) {
              return new ValidationResult({
                id : 'landCntry',
                type : 'text',
                name : 'Landed Country'
              }, false, 'Please make sure landed country of billing and company addresses is the same.');
            }
            return new ValidationResult(null, true);
          } else {
            console.log("Italy Local Scenario......");
            var cntryRegion = FormManager.getActualValue('countryUse');
            var landCntry = 'IT'; // default to Italy
            if (cntryRegion != '' && cntryRegion.length > 3) {
              landCntry = cntryRegion.substring(3, 5);
            }
            var defaultLandCntry = landCntry;

            if (landCntryZP01.ret1 != null && defaultLandCntry != '' && landCntryZP01.ret1 != defaultLandCntry) {
              return new ValidationResult(null, false, 'Landed Country value of the Bill-to (Billing) Address should be \'' + defaultLandCntry + '\' for Local.');
            } else if (landCntryZP01.ret1 != null && landCntryZI01.ret1 != null && landCntryZP01.ret1 != landCntryZI01.ret1) {
              return new ValidationResult({
                id : 'landCntry',
                type : 'text',
                name : 'Landed Country'
              }, false, 'Please make sure landed country of billing and company addresses is the same.');
            }
            return new ValidationResult(null, true);
          }
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function setPostCdItalyVA(cntry, addressMode, saving, finalSave) {
  if (addressMode == 'newAddress' || addressMode == 'updateAddress' || addressMode == 'copyAddress' && cmr.currentRequestType == 'C') {
    var _landCntryHandler = dojo.connect(FormManager.getField('landCntry'), 'onChange', function(value) {
      var landCntry = FormManager.getActualValue('landCntry');
      if (landCntry == 'VA') {
        if (_pagemodel.userRole.toUpperCase() == 'REQUESTER') {
          FormManager.setValue('postCd', '00120');
          FormManager.readOnly('postCd');
        } else {
          if (FormManager.getActualValue('postCd') == '') {
            FormManager.setValue('postCd', '00120');
          }
          FormManager.enable('postCd');
        }
      } else {
        FormManager.enable('postCd');
      }
    });
    if (_landCntryHandler && _landCntryHandler[0]) {
      _landCntryHandler[0].onChange();
    }
  }
}

function validateSBOForIT() {
  FormManager.addFormValidator(
      (function() {
        return {
          validate : function() {
            var isu = FormManager.getActualValue('isuCd');
            var sbo = FormManager.getActualValue('salesBusOffCd').toUpperCase();
            var lbl1 = FormManager.getLabel('SalesBusOff');
            var _custType = FormManager.getActualValue('custSubGrp');
            if (isu != undefined && isu == 21 && _custType != undefined && (_custType == 'INTER' || _custType == 'CROIN' || _custType == 'INTSM' || _custType == 'INTVA') && sbo != undefined
                && sbo != '') {
              if (sbo != '1B' && sbo != '1G' && sbo != '1E' && sbo != '11' && sbo != '12' && sbo != '13' && sbo != '14') {
                return new ValidationResult({
                  id : 'salesBusOffCd',
                  type : 'text',
                  name : 'salesBusOffCd'
                }, false, 'The ' + lbl1 + ' value for Internal scenario is invalid.');
              }
            }
            return new ValidationResult(null, true);
          }
        };
      })(), 'MAIN_IBM_TAB', 'frmCMR');

}

function validateCodiceDesIT() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var codicDes = FormManager.getActualValue('hwSvcsRepTeamNo');
        var alphanumeric = /^[0-9a-zA-Z]*$/;

        if (codicDes == '') {
          return new ValidationResult(null, true);
        } else {
          if (codicDes.length <= 5) {
            return new ValidationResult(null, false, 'CODICE DESTINATARIO/UFFICIO should be 6 or 7 characters.');
          }
          if (!codicDes.match(alphanumeric)) {
            return new ValidationResult({
              id : 'hwSvcsRepTeamNo',
              type : 'text',
              name : 'hwSvcsRepTeamNo'
            }, false, 'CODICE DESTINATARIO/UFFICIO should be alphanumeric only.');
          }
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function validateCMRNumberForIT() {

  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var cmrNo = FormManager.getActualValue('cmrNo');
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
          console.log("validateCMRNumberForIT ifProspect:" + ifProspect);
          if ('Y' == ifProspect) {
            return new ValidationResult(null, true);
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

function saveFiscalValues() {
  var choice = FormManager.getActualValue('cmrNewOld');
  if (choice == '') {
    cmr.showAlert('Please select either import or fiscal before save.');
    return;
  }

  if (document.getElementById('W').checked) {
    FormManager.setValue('company', dojo.byId('oldCmrNo').innerText);
    FormManager.setValue('fiscalDataStatus', 'W');
    FormManager.setValue('nationalCusId', 'W');
    FormManager.setValue('fiscalDataCompanyNo', dojo.byId('newCmrNo').innerText);
    FormManager.setValue('taxCd1', dojo.byId('oldTaxCd1').innerText);
    FormManager.setValue('vat', dojo.byId('oldVat').innerText);
    FormManager.setValue('enterprise', dojo.byId('oldEnterprise').innerText);
    FormManager.setValue('identClient', dojo.byId('oldIdentClient').innerText);
    FormManager.enable('taxCd1');
    FormManager.enable('vat');
    FormManager.enable('enterprise');
    FormManager.enable('identClient');
    cmr.hideModal('validateFiscalDataModal');
  } else if (document.getElementById('L').checked) {
    FormManager.setValue('company', dojo.byId('newCmrNo').innerText);
    FormManager.setValue('fiscalDataStatus', 'L');
    FormManager.setValue('nationalCusId', 'L');
    FormManager.setValue('fiscalDataCompanyNo', dojo.byId('newCmrNo').innerText);
    FormManager.setValue('taxCd1', dojo.byId('newTaxCd1').innerText);
    FormManager.setValue('vat', dojo.byId('newVat').innerText);
    FormManager.setValue('enterprise', dojo.byId('newEnterprise').innerText);
    FormManager.setValue('identClient', dojo.byId('newIdentClient').innerText);
    FormManager.readOnly('company');
    FormManager.readOnly('taxCd1');
    FormManager.readOnly('vat');
    FormManager.readOnly('enterprise');
    FormManager.readOnly('identClient');
    cmr.hideModal('validateFiscalDataModal');
  }
}

function disableCompanyLevelFieldsIT() {

  console.log("Disabling company level fields");
  if (FormManager.getActualValue('reqType') == 'U') {
    var _cmrNo = FormManager.getActualValue('cmrNo');
    var _company = FormManager.getActualValue('company');
    if (_cmrNo != null && _company != null && _cmrNo != _company) {
      FormManager.readOnly('cmrNo');
      FormManager.readOnly('company');
      FormManager.readOnly('taxCd1');
      FormManager.readOnly('vat');
      FormManager.readOnly('enterprise');
      FormManager.readOnly('identClient');
    }
  }
}

function addEmbargoCodeValidatorIT() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var embargoCd = FormManager.getActualValue('embargoCd');
        var reqType = FormManager.getActualValue('reqType');

        /*
         * if (reqType != 'U') { return new ValidationResult(null, true); }
         */
        if (embargoCd != '' && embargoCd.length > 0) {
          embargoCd = embargoCd.trim();
          if ((embargoCd != '' && embargoCd.length == 1) && (embargoCd == 'Y' || embargoCd == 'J')) {
            return new ValidationResult(null, true);
          } else {
            return new ValidationResult({
              id : 'embargoCd',
              type : 'text',
              name : 'embargoCd'
            }, false, 'Please use valid value for Embargo code field.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

/**
 * Set Client Tier Value
 */
function setClientTierValuesIT(isuCd) {

  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }

  isuCd = FormManager.getActualValue('isuCd');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var clientTiers = [];
  if (isuCd != '') {
    var qParams = {
      _qall : 'Y',
      ISSUING_CNTRY : cntry,
      ISU : '%' + isuCd + '%'
    };
    var results = cmr.query('GET.CTCLIST.BYISU', qParams);
    if (results != null) {
      for (var i = 0; i < results.length; i++) {
        clientTiers.push(results[i].ret1);
      }
      if (clientTiers != null) {
        FormManager.limitDropdownValues(FormManager.getField('clientTier'), clientTiers);
        if (clientTiers.length == 1) {
          FormManager.setValue('clientTier', clientTiers[0]);
        }
      }
    }
  }
}

function validateExistingCMRNo() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        console.log('checking requested cmr number...');
        var reqType = FormManager.getActualValue('reqType');
        var cmrNo = FormManager.getActualValue('cmrNo');
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        if (reqType == 'C' && cmrNo) {
          var exists = cmr.query('LD.CHECK_EXISTING_CMR_NO', {
            COUNTRY : cntry,
            CMR_NO : cmrNo,
            MANDT : cmr.MANDT
          });
          if (exists && exists.ret1) {
            return new ValidationResult({
              id : 'cmrNo',
              type : 'text',
              name : 'cmrNo'
            }, false, 'The requested CMR Number ' + cmrNo + ' already exists in the system.');
          } else {
            exists = cmr.query('LD.CHECK_EXISTING_CMR_NO_RESERVED', {
              COUNTRY : cntry,
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

function addAfterTemplateLoadIT() {
  var _custSubGrp = FormManager.getActualValue('custSubGrp');
  var role = FormManager.getActualValue('userRole').toUpperCase();

  if ('C' == FormManager.getActualValue('reqType')) {
    if (role == "REQUESTER" && _custSubGrp == 'INTER' || _custSubGrp == 'CROIN' || _custSubGrp == 'INTSM' || _custSubGrp == 'INTVA') {
      FormManager.enable('cmrNo');
    } else {
      FormManager.readOnly('cmrNo');
    }
    if (role == "PROCESSOR") {
      FormManager.enable('cmrNo');
    }
  }
  autoSetSBOSROnPostalCode(false, false);

  var ident = FormManager.getActualValue('identClient');
  if (FormManager.getActualValue('reqType') == 'C' && 'CROSS' == FormManager.getActualValue('custGrp')) {

    var reqId = FormManager.getActualValue('reqId');
    var checkImportIndc = getImportedIndcForItaly();
    if (checkImportIndc != 'Y') {
      FormManager.enable('identClient');
      if (ident == 'N') {
        FormManager.addValidator('taxCd1', Validators.REQUIRED, [ 'Fiscal Code' ], 'MAIN_CUST_TAB');
        FormManager.enable('taxCd1');
      } else if (ident == 'Y') {
        FormManager.resetValidations('taxCd1');
        FormManager.setValue('taxCd1', '');
        FormManager.readOnly('taxCd1');
      }
    }
  }
  if ('C' == FormManager.getActualValue('reqType')) {
    FormManager.readOnly('salesBusOffCd');
  } else {
    FormManager.enable('salesBusOffCd');
  }

  if ('C' == FormManager.getActualValue('reqType') && role == "PROCESSOR" && custSubGrp == 'PRISM') {
    FormManager.enable('salesBusOffCd');
  }
}

function checkIsicCodeValidationIT() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var requestType = FormManager.getActualValue('reqType');
        if (requestType != 'C') {
          return new ValidationResult(null, true);
        }
        var isicCode = FormManager.getActualValue('isicCd');
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        if (isicCode == '9500') {
          if (custSubGrp == 'PRISM' || custSubGrp == 'PRICU' || custSubGrp == 'PRIVA' || custSubGrp == 'CROPR') {
            console.log('no validation required');
          } else {
            return new ValidationResult(null, false, 'ISIC code value should be other than 9500 for Non-Private customer.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function addAfterConfigItaly() {
  lockEmbargo();
  typeOfCustomer();
  afterConfigForIT();
  toggleBPRelMemType();
  fieldsReadOnlyItaly();
  disableProcpectCmrIT();
  setExpediteReasonOnChange();
  disableCompanyLevelFieldsIT();
  disableAutoProcessingOnFiscalUpdate();
  enableDisableTaxCodeCollectionCdIT();
  addFieldValidationForRequestorItaly();

}

function addAfterTemplateLoadItaly(fromAddress, scenario, scenarioChanged) {
  addFieldValidationForProcessorItaly();
  getOldValuesIT(fromAddress, scenario, scenarioChanged);
  autoPopulateIdentClientIT();
  setVATForItaly();
  fieldsReadOnlyItaly(fromAddress, scenario, scenarioChanged);
  setMRCOnScenariosIT();
  toggleBPRelMemType();
  typeOfCustomer();
  setSpecialTaxCodeOnScenarioIT();
  enableDisableTaxCodeCollectionCdIT();
  setClientTierValuesIT();
  addAfterTemplateLoadIT();
  setSalesRepValuesIT();
  blankedOutCollectionCD();
  setAffiliateEnterpriseRequired();
  addFieldValidationForRequestorItaly();
  disableProcpectCmrIT();
}

function addAddrFunctionItaly(cntry, addressMode, saving, finalSave) {
  addNonLatinCharValidator(cntry, addressMode, saving, finalSave);
  autoSetSBOSRForCompany(cntry, addressMode, saving, finalSave);
  autoSetAddrFieldsForIT(cntry, addressMode, saving, finalSave);
  addressLoadHandlerIT(cntry, addressMode, saving, finalSave);
  addLatinCharValidatorITALY(cntry, addressMode, saving, finalSave);
  toggleBPRelMemType(cntry, addressMode, saving, finalSave);
  setStateProvReqdPostalCodeIT(cntry, addressMode, saving, finalSave);
  allowCopyOfNonExistingAddressOnly(cntry, addressMode, saving, finalSave);
  setSpecialTaxCodeOnAddressIT(cntry, addressMode, saving, finalSave);
  setPostCdItalyVA(cntry, addressMode, saving, finalSave);

}
// CMR-1524 ITALY - Ident Client for CBs
function addFieldValidationForRequestorItaly() {
  var requestType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var scenario = FormManager.getActualValue('custGrp');
  if (requestType != 'C') {
    return;
  }
  if (scenario.includes('CRO')) {
    var scenarioType = 'CROSS';

    if (requestType == 'C' && scenarioType != undefined && scenarioType == 'CROSS') {
      FormManager.addValidator('identClient', Validators.REQUIRED, [ 'Ident Client' ], 'MAIN_CUST_TAB');
    } else {
      FormManager.resetValidations('identClient');
    }
  }
}
function disableProcpectCmrIT() {
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

function setClassificationCodeTR() {
  var field = FormManager.getField('custClass');
  if (FormManager.getActualValue('reqType') == 'C') {
    FormManager.limitDropdownValues(field, [ '45', '46' ]);
  } else if (FormManager.getActualValue('reqType') == 'U') {
    FormManager.limitDropdownValues(field, [ '11', '13', '33', '35', '45', '46', '60', '81' ]);
  }
}

function setCOFClassificationCodeTR() {
  var cofVal = FormManager.getActualValue('commercialFinanced');
  var field = FormManager.getField('custClass');
  if (cofVal == 'R' || cofVal == 'S' || cofVal == 'T') {
    FormManager.limitDropdownValues(field, [ '11' ]);
  } else {
    setClassificationCodeTR();
  }
}

function setTypeOfCustomerClassificationCodeTR() {
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

function setIsicClassificationCodeTR(value) {
  var field = FormManager.getField('custClass');
  if (!value) {
    value = FormManager.getActualValue('isicCd');
  }

  if (value == '9500') {
    FormManager.limitDropdownValues(field, [ '60' ]);
  } else {
    setClassificationCodeTR();
  }
}

function validateSingleReactParentCMR() {

  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        console.log('checking Inactive Parents of Single Reactivation CMR..');
        var reqType = FormManager.getActualValue('reqType');
        var cmrNo = FormManager.getActualValue('cmrNo');
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        console.log('ReqType:' + reqType + ' <>cmrNo:' + cmrNo + ' <>cntry:' + cntry);
        if (reqType == 'X' && cmrNo) {
          var qParams = {
            COUNTRY : cntry,
            CMR_NO : cmrNo
          };
          var resultComp = cmr.query('LD.SINGLE_REACT_CHECK_ACTIVE_PARENT_COMPANY', qParams);

          var resultBill = cmr.query('LD.SINGLE_REACT_CHECK_ACTIVE_PARENT_BILLING', qParams);

          if (resultComp.ret1 != null && 'C' == resultComp.ret1) {
            return new ValidationResult({
              id : 'cmrNo',
              type : 'text',
              name : 'cmrNo'
            }, false, 'Parents CMR#' + resultComp.ret2 + ' is inactive So, first reactivate this.');
          } else if (resultBill.ret1 != null && ('C' == resultBill.ret1 && cmrNo != resultBill.ret2)) {
            return new ValidationResult({
              id : 'cmrNo',
              type : 'text',
              name : 'cmrNo'
            }, false, 'Parents CMR#' + resultBill.ret2 + ' is inactive So, first reactivate this.');
          } else {
            return new ValidationResult(null, true);
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function StcOrderBlockValidation() {
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
          return new ValidationResult(null, false, 'Please fill either STC order block code or Order Block field');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

dojo.addOnLoad(function() {
  GEOHandler.EMEA = [ SysLoc.UK, SysLoc.IRELAND, SysLoc.ISRAEL, SysLoc.TURKEY, SysLoc.GREECE, SysLoc.CYPRUS, SysLoc.ITALY ];
  console.log('adding EMEA functions...');
  GEOHandler.setRevertIsicBehavior(false);
  GEOHandler.enableCustomerNamesOnAddress(GEOHandler.EMEA);
  GEOHandler.addAddrFunction(updateMainCustomerNames, GEOHandler.EMEA);
  GEOHandler.addAddrFunction(addEMEALandedCountryHandler, GEOHandler.EMEA);
  GEOHandler.enableCopyAddress(GEOHandler.EMEA, validateEMEACopy, [ 'ZD01', 'CTYC' ]);

  /* 1438717 - add DPL match validation for failed dpl checks */
  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.EMEA, GEOHandler.ROLE_PROCESSOR, true);

  // ITALY
  GEOHandler.registerValidator(checkIfVATFiscalUpdatedIT, [ SysLoc.ITALY ]);
  GEOHandler.registerValidator(stateProvValidatorCBforIT, [ SysLoc.ITALY ]);
  GEOHandler.registerValidator(validateVATFiscalLengthOnIdentIT, [ SysLoc.ITALY ]);
  GEOHandler.registerValidator(addCompanyAddrValidator, [ SysLoc.ITALY ], null, true);
  GEOHandler.registerValidator(addBillingAddrValidator, [ SysLoc.ITALY ], null, true);
  GEOHandler.registerValidator(addInstallingAddrValidator, [ SysLoc.ITALY ], null, true);
  GEOHandler.registerValidator(validateEnterpriseNumForIT, [ SysLoc.ITALY ], null, true);
  GEOHandler.registerValidator(addCrossBorderValidatorForIT, [ SysLoc.ITALY ], null, true);
  GEOHandler.registerValidator(validateSingleReactParentCMR, [ SysLoc.ITALY ], null, true);
  GEOHandler.registerValidator(validateFiscalCodeForCreatesIT, [ SysLoc.ITALY ], null, true);
  GEOHandler.registerValidator(validateLandCntryForCBCreatesIT, [ SysLoc.ITALY ], null, true);
  GEOHandler.registerValidator(checkIfStateProvBlankForProcIT, [ SysLoc.ITALY ], GEOHandler.ROLE_PROCESSOR, true);
  GEOHandler.checkRoleBeforeAddAddrFunction(addAddrValidationForProcItaly, [ SysLoc.ITALY ], null, GEOHandler.ROLE_PROCESSOR);
  GEOHandler.registerValidator(addGenericVATValidator(SysLoc.ITALY, 'MAIN_CUST_TAB', 'frmCMR', 'ZP01'), [ SysLoc.ITALY ], null, true);

  // For Legacy Direct
  GEOHandler.addAfterConfig(addAfterConfigItaly, [ SysLoc.ITALY ]);
  GEOHandler.addAddrFunction(addAddrFunctionItaly, [ SysLoc.ITALY ]);
  GEOHandler.addAfterTemplateLoad(addAfterTemplateLoadItaly, [ SysLoc.ITALY ]);
  // GEOHandler.registerValidator(addCMRValidator,[ SysLoc.ITALY ], null, true);
  GEOHandler.registerValidator(validateSBOForIT, [ SysLoc.ITALY ]);
  GEOHandler.registerValidator(checkIsicCodeValidationIT, [ SysLoc.ITALY ]);
  GEOHandler.registerValidator(validateCodiceDesIT, [ SysLoc.ITALY ], null, true);
  GEOHandler.registerValidator(validateExistingCMRNo, [ SysLoc.ITALY ], null, true);
  GEOHandler.registerValidator(validateCMRNumberForIT, [ SysLoc.ITALY ], null, true);
  GEOHandler.registerValidator(addEmbargoCodeValidatorIT, [ SysLoc.ITALY ], null, true);
  GEOHandler.registerValidator(StcOrderBlockValidation, [ SysLoc.ITALY ], null, true);

});
