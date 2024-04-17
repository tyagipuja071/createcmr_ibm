/* Register EMEA Javascripts */
var _customerTypeHandler = null;
var _vatExemptHandler = null;
var _isuCdHandler = null;
var _postCdHandler = null
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

var _gtcISUHandler = null;
var _CTCHandler = null;
var _gtcISRHandler = null;
var _gtcAddrTypes = [ 'ZS01', 'ZP01', 'ZD01', 'ZI01' ];
var _gtcAddrTypesIL = [ 'ZS01', 'ZP01', 'ZD01', 'ZI01', 'CTYA', 'CTYB', 'CTYC' ];
var _gtcAddrTypeHandler = [];
var _gtcAddrTypeHandlerIL = [];
var _gtcVatExemptHandler = null;
var _postCdHandlerUK = null;
var _oldIsicCd = null;

var _CTCHandlerIL = null;
var _isuCdHandlerIL = null;
var _CTCHandlerIE = null;

var _specialTaxCdHandlerIE = null;

function countryScenarioProcessorRules() {
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.ISRAEL) {
    return true;
  } else {
    return false;
  }
}

var NORTHERN_IRELAND_POST_CD = [ 'BT' ];
var UK_LANDED_CNTRY = [ 'AI', 'BM', 'IO', 'VG', 'KY', 'FK', 'GI', 'MS', 'PN', 'SH', 'TC', 'GS', 'GG', 'JE', 'IM', 'AO', 'IE' ];
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
var _oldCollectionIT = "";
var _oldIdentClientIT = "";
var _oldVatIT = "";
var _lobHandler = null;
var _landCntryHandlerUK = null;
var _landCntryHandlerIE = null;
var _postalCdUKHandler = null;
var _cityUKHandler = null;
var _custGrpIT = null;
var _importedIndc = null;
var _landedIT = null;
var _sboHandlerIT = null;
var _importedIndcBilling = null;
var _ILentConnect = null;

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

function getImportedIndcForItalyBillingAddr() {
  if (_importedIndcBilling) {
    console.log('Returning imported indc for Billing Address = ' + _importedIndcBilling);
    return _importedIndcBilling;
  }
  var results = cmr.query('IMPORTED_ADDR_ZP01', {
    REQID : FormManager.getActualValue('reqId')
  });
  if (results != null && results.ret1) {
    _importedIndcBilling = results.ret1;
  } else {
    _importedIndcBilling = 'N';
  }
  console.log('saving imported ind as for Billing Address' + _importedIndc);
  return _importedIndcBilling;
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

  var landCntry = FormManager.getActualValue('landCntry');
  if (landCntry == 'GR') {
    var scenario = FormManager.getActualValue('custGrp');
    if ((scenario == 'LOCAL' || FormManager.getActualValue('reqType') == 'U') && FormManager.getActualValue('addrType') == 'ZP01') {
      GEOHandler.disableCopyAddress();
    }
  }
  var issu_cntry = FormManager.getActualValue('cmrIssuingCntry');
  var reqType = FormManager.getActualValue('reqType');
  if (reqType == 'U' && FormManager.getActualValue('addrType') == 'ZS01' && (issu_cntry == SysLoc.IRELAND || issu_cntry == SysLoc.UK)) {
    FormManager.readOnly('landCntry');
  } else {
    FormManager.enable('landCntry');
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
  } else if ((custSubGrp == 'IBMIT') || (custSubGrp == 'XIBM')) {
    FormManager.setValue('crosSubTyp', '98');
    FormManager.readOnly('crosSubTyp');
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

/**
 * Customer Name Con't or Attention Person should be required (Israel)
 */
function addNameContAttnDeptValidatorUKI() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('cmrIssuingCntry') != SysLoc.UK && FormManager.getActualValue('cmrIssuingCntry') != SysLoc.IRELAND) {
          return new ValidationResult(null, true);
        }

        var nm2 = FormManager.getActualValue('custNm2');
        var dept = FormManager.getActualValue('dept');
        var phone = FormManager.getActualValue('custPhone');

        var val = nm2;
        if (dept != '') {
          val += (val.length > 0 ? ', ' : '') + dept;
        }
        if (phone != '') {
          val += (val.length > 0 ? ', ' : '') + phone;
        }
        if (val.length > 30) {
          return new ValidationResult(null, false, 'Total computed length of Customer Name Con\'t, Department/Attn, and Customer Phone should be less than 30 characters.');
        }

        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function fieldsReadOnlyItaly(fromAddress, scenario, scenarioChanged) {
  var custSubType = FormManager.getActualValue('custSubGrp');
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var reqId = FormManager.getActualValue('reqId');

  if (custSubType == 'INTER' || custSubType == 'CROIN' || custSubType == 'INTSM' || custSubType == 'INTVA') {
    cmr.showNode('sboInfo');
  } else {
    cmr.hideNode('sboInfo');
  }
  if (reqType == 'U') {
    var landcntry = getZS01LandCntry();
    if (landcntry != 'IT' && landcntry != null && landcntry != undefined && landcntry != '') {
      FormManager.readOnly('taxCd1');
      FormManager.setValue('taxCd1', '');
    }
  }
  if (reqType != 'C') {
    return;
  }
  var checkImportIndc = getImportedIndcForItaly();
  if (reqType == 'C' && role == 'REQUESTER') {
    console.log("fieldsReadOnlyItaly for REQUESTER..");
    // Defect: 1461349 - For Lock and unlocked
    FormManager.readOnly('abbrevNm');
    FormManager.readOnly('abbrevLocn');
    // CREATCMR-622
    FormManager.readOnly('icmsInd');
    FormManager.readOnly('hwSvcsRepTeamNo');
    FormManager.readOnly('email2');
    FormManager.readOnly('email3');
    FormManager.resetValidations('abbrevNm');
    FormManager.resetValidations('salesBusOffCd');

    if (checkImportIndc == "Y") {
      // fields for Legacy consolidation
      FormManager.readOnly('taxCd1');
      FormManager.enable('collectionCd');
      FormManager.readOnly('enterprise');
      // FormManager.resetValidations('vat');
      // FormManager.readOnly('identClient');
      FormManager.resetValidations('taxCd1');
      FormManager.resetValidations('enterprise');
      // FormManager.resetValidations('identClient');
      // FormManager.readOnly('vat');
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
      FormManager.readOnly('enterprise');
      FormManager.resetValidations('taxCd1');
      FormManager.resetValidations('vat');
      FormManager.resetValidations('enterprise');
      FormManager.readOnly('identClient');
      FormManager.resetValidations('identClient');
      FormManager.enable('collectionCd');
    }
    // CREATCMR-622
    FormManager.enable('icmsInd');
    FormManager.enable('hwSvcsRepTeamNo');
    FormManager.enable('email2');
    FormManager.enable('email3');
  }
}

/**
 * Add Latin character validation for address fields
 */
function addLatinCharValidator() {
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
      checkAndAddValidator('addrTxt2', Validators.LATIN, [ ' Address Con\'t' ]);
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
      // checkAndAddValidator('custNm2', Validators.NON_LATIN, [
      // 'Nickname' ]);
      checkAndAddValidator('addrTxt2', Validators.NON_LATIN, [ 'Address Con\'t/Occupation' ]);
      checkAndAddValidator('dept', Validators.NON_LATIN, [ 'District' ]);
      checkAndAddValidator('taxOffice', Validators.NON_LATIN, [ 'Tax Office' ]);
      checkAndAddValidator('custNm4', Validators.NON_LATIN, [ 'Att. Person' ]);
    }
    checkAndAddValidator('addrTxt', Validators.NON_LATIN, [ 'Street Address' ]);
    checkAndAddValidator('city1', Validators.NON_LATIN, [ 'City' ]);
    // checkAndAddValidator('postCd', Validators.NON_LATIN, [ 'Postal Code' ]);
    checkAndAddValidator('poBox', Validators.NON_LATIN, [ 'PO Box' ]);
    // checkAndAddValidator('custPhone', Validators.NON_LATIN, [ 'Phone #'
    // ]);

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

function setEconomicCode() {
  if (_economicCdHandler == null) {
    _economicCdHandler = dojo.connect(FormManager.getField('subIndustryCd'), 'onChange', function(value) {
      FormManager.setValue('economicCd', '0' + FormManager.getActualValue('subIndustryCd'));
    });
  }
  if (_economicCdHandler && _economicCdHandler[0]) {
    _economicCdHandler[0].onChange();
  }

  FormManager.readOnly('economicCd');
  if (FormManager.getActualValue('subIndustryCd') != '') {
    FormManager.setValue('economicCd', '0' + FormManager.getActualValue('subIndustryCd'));
  }
}

function addPostalCodeValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var postal_cd = FormManager.getActualValue('postCd');
        /*
         * DENNIS Defect 1830120: PP: Getting 'Cannot get postal code error' for
         * postal codes for UK
         */
        // if (FormManager.getActualValue('landCntry')
        // != 'GB') {
        // return new ValidationResult(null, true);
        // } else {
        var postcodeRegEx = /^[a-zA-Z]{1,2}([0-9]{1,2}|[0-9][a-zA-Z])\s*[0-9][a-zA-Z]{2}$/;
        if (postcodeRegEx.test(postal_cd)) {
          return new ValidationResult(null, true);
        } else {
          return new ValidationResult(null, false, 'Postal Code format error. Please refer to info bubble for details. ');
        }
        // }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function addAbbrevNmValidator() {
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

function addEMEAClientTierISULogic() {
  var reqType = null;
  reqType = FormManager.getActualValue('reqType');
  if (reqType != 'C') {
    return;
  }
  if (!PageManager.isReadOnly()) {
    FormManager.enable('clientTier');
  }
  _ISUHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
    if (!value) {
      value = FormManager.getActualValue('isuCd');
    }
    if (!value) {
      FormManager.setValue('clientTier', '');
    }
    var tierValues = null;
    if (value == '32') {
      tierValues = [ 'B', 'M', 'S' ];
    } else if (value == '34') {
      tierValues = [ 'A', 'E', 'V', '4', '6' ];
    } else if (value != '') {
      tierValues = [ 'Z' ];
    }

    if (tierValues != null) {
      FormManager.limitDropdownValues(FormManager.getField('clientTier'), tierValues);
    } else {
      FormManager.resetDropdownValues(FormManager.getField('clientTier'));
    }
  });
  if (_ISUHandler && _ISUHandler[0]) {
    _ISUHandler[0].onChange();
  }
}

function addISUHandlerIT() {
  var _isuHandler = null;
  _isuHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
    if (!value) {
      value = FormManager.getActualValue('isuCd');
    }
    setClientTierValuesIT(value);
  });

  if (_custSubTypeHandler == null) {
    _custSubTypeHandler = dojo.connect(FormManager.getField('custSubGrp'), 'onChange', function(value) {
      if (typeof (_pagemodel) != 'undefined' && _pagemodel['custSubGrp'] != FormManager.getActualValue('custSubGrp')) {
        setDeafultISUCtcChange();
        setDeafultSBOLogicComm();
      }
    });
  }
}

function addIsicCdHandler() {
  if (_isicHandler == null) {
    _isicHandler = dojo.connect(FormManager.getField('isicCd'), 'onChange', function(value) {
      setDeafultSBOLogicComm();
    });
  }
}

function setClientTierValuesIT(isuCd) {
  var reqType = FormManager.getActualValue('reqType');
  if (FormManager.getActualValue('viewOnlyPage') == 'true' || reqType != 'C') {
    return;
  }
  isuCd = FormManager.getActualValue('isuCd');
  isuCdList = [ '36', '34', '27' ];
  if (!isuCdList.includes(isuCd)) {
    FormManager.removeValidator('clientTier', Validators.REQUIRED);
    FormManager.setValue('clientTier', '');
  }
  // setting default combos that are allowed for IsuCTC
  if (isuCd == '27') {
    FormManager.setValue('clientTier', 'E');
  } else if (isuCd == '34') {
    FormManager.setValue('clientTier', 'Q');
  } else if (isuCd == '36') {
    FormManager.setValue('clientTier', 'Y');
  } else {
    FormManager.setValue('clientTier', '');
  }
  // setDeafultSBOLogicComm();
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

function postalCodeNumericOnlyForDomestic() {
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

  if (FormManager.getActualValue('reqType') == 'C' && (custGroup == 'CROSS')) {
    landCntryZS01 = cmr.query('ADDR.GET.LAND_CNTRY.BY_REQID', {
      REQ_ID : reqId,
      ADDR_TYPE : 'ZS01'
    });
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
  if (FormManager.getActualValue('reqType') == 'C' && (custGroup == 'CROSS')) {
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

function isTranslationAddrFieldsMatchForGR(zs01Data, zp01Data) {

  if (zs01Data.custNm1[0] == zp01Data.custNm1[0] && zs01Data.custNm2[0] == zp01Data.custNm2[0] && zs01Data.custNm4[0] == zp01Data.custNm4[0] && zs01Data.addrTxt[0] == zp01Data.addrTxt[0]
      && zs01Data.addrTxt2[0] == zp01Data.addrTxt2[0] && zs01Data.poBox[0] == zp01Data.poBox[0] && zs01Data.postCd[0] == zp01Data.postCd[0] && zs01Data.city1[0] == zp01Data.city1[0]) {
    return true;
  }

  return false;
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
    if (!hasMatchingFieldsFilled(zs01Data.stateProv[0], zp01Data.stateProv[0], isCrossborder)) {
      mismatchFields += mismatchFields != '' ? ', ' : '';
      mismatchFields += 'State/Province';
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

function populateTranslationAddrWithSoldToData() {
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

function clearAddrFieldsForGR() {
  FormManager.clearValue('custNm1');
  FormManager.clearValue('custNm2');
  FormManager.clearValue('addrTxt');
  FormManager.clearValue('addrTxt2');
  FormManager.clearValue('poBox');
  FormManager.clearValue('postCd');
  FormManager.clearValue('city1');
}

function clearAddrFieldsForTR() {
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

var _addrSelectionHistTR = '';

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

function setISRValuesGR() {
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (custSubGrp == 'SPAS') {
    FormManager.setValue('repTeamMemberNo', '000000');
    FormManager.setValue('abbrevLocn', 'SAAS');
    FormManager.readOnly('repTeamMemberNo');
  } else if (custSubGrp == 'BUSPR' || custSubGrp == 'XBP') {
    FormManager.setValue('repTeamMemberNo', '200005');
    FormManager.readOnly('repTeamMemberNo');
  } else if (custSubGrp == 'INTER' || custSubGrp == 'XINTR') {
    FormManager.setValue('repTeamMemberNo', '000000');
  }
  var repTeamMemberNo = FormManager.getActualValue('repTeamMemberNo');
  setEnterprise(repTeamMemberNo);
  FormManager.readOnly('subIndustryCd');
  if (custSubGrp == 'COMME' || custSubGrp == 'CROSS' || custSubGrp == 'PRICU' || custSubGrp == 'GOVRN' || custSubGrp == '') {
    setISRValues();
  }
  if (repTeamMemberNo == '') {
    FormManager.setValue('salesSR', '');
    FormManager.setValue('salesTeamCd', '');
    FormManager.setValue('salesBusOffCd', '');
  }
}

function setFieldsBehaviourGR() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var isuCd = FormManager.getActualValue('isuCd');
  var clientTier = FormManager.getActualValue('clientTier');
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var repTeamMemberNo = FormManager.getActualValue('repTeamMemberNo');
  setEnterprise(repTeamMemberNo);
  if (custSubGrp == 'CROSS' || custSubGrp == 'COMME') {
    FormManager.enable('clientTier');
    FormManager.enable('repTeamMemberNo');
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
  }
  FormManager.addValidator('custPrefLang', Validators.REQUIRED, [ 'Preferred Language' ], 'MAIN_IBM_TAB');
  FormManager.addValidator('cmrOwner', Validators.REQUIRED, [ 'CMR Owner' ], 'MAIN_IBM_TAB');
  FormManager.addValidator('subIndustryCd', Validators.REQUIRED, [ 'Subindustry' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('isicCd', Validators.REQUIRED, [ 'ISIC' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('repTeamMemberNo', Validators.REQUIRED, [ 'Sales Rep' ], 'MAIN_IBM_TAB');
  if (FormManager.getActualValue('reqType') == 'C') {
    FormManager.addValidator('subIndustryCd', Validators.REQUIRED, [ 'Subindustry' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('isicCd', Validators.REQUIRED, [ 'ISIC' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('repTeamMemberNo', Validators.REQUIRED, [ 'Sales Rep' ], 'MAIN_IBM_TAB');
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
}

function resetSubIndustryCdGR() {
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

function setISRValues() {
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
        setEnterprise(isrs[0]);
        FormManager.setValue('repTeamMemberNo', isrs[0]);
      }
      setSalesBoSboIbo();
    }
  }
}

function setEnterprise(value) {
  var cmrCntry = FormManager.getActualValue('cmrIssuingCntry');
  var isu = FormManager.getActualValue('isuCd');
  var ctc = FormManager.getActualValue('clientTier');
  if (cmrCntry == SysLoc.GREECE && value == 'R21180' && isu == '32' && ctc == 'M') {
    FormManager.setValue('enterprise', '822835');
  } else if (cmrCntry == SysLoc.GREECE && value == 'R21180' && isu == '32' && ctc == 'S') {
    FormManager.setValue('enterprise', '822806');
  } else if (cmrCntry == SysLoc.GREECE && value == '000000' && isu == '34' && ctc == '6') {
    FormManager.setValue('enterprise', '822836');
  } else if (cmrCntry == SysLoc.GREECE && value == '000000' && isu == '32' && ctc == '6') {
    FormManager.setValue('enterprise', '822835');
  } else if (cmrCntry == SysLoc.GREECE && value == '000000' && isu == '32' && ctc == 'S') {
    FormManager.setValue('enterprise', '822806');
  } else if (cmrCntry == SysLoc.CYPRUS && value == 'E33290' && isu == '32' && ctc == 'M') {
    FormManager.setValue('enterprise', '822835');
  } else {
    FormManager.setValue('enterprise', '');
  }
}

function hideCollectionCd() {
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.TURKEY && FormManager.getActualValue('reqType') == 'U') {
    FormManager.show('CollectionCd', 'collectionCd');
  } else {
    FormManager.resetValidations('collectionCd');
    FormManager.hide('CollectionCd', 'collectionCd');
  }
}

function setSalesBoSboIbo() {
  var repTeamMemberNo = FormManager.getActualValue('repTeamMemberNo');
  if (repTeamMemberNo != '') {
    var qParams = {
      ISSUING_CNTRY : FormManager.getActualValue('cmrIssuingCntry'),
      REP_TEAM_CD : repTeamMemberNo
    };
    var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
    var custSubType = FormManager.getActualValue('custSubGrp');
    if (cmrIssuingCntry == '758'
        && (custSubType == 'COMME' || custSubType == 'GOVST' || custSubType == 'LOCEN' || custSubType == 'NGOIT' || custSubType == 'PRICU' || custSubType == '3PAIT' || custSubType == 'UNIVE')) {
      return;
    }
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

function addPostalCodeLenForTurGreCypValidator() {
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
// Not used
function addTaxCodeForProcessorValidator() {
  console.log("register addTaxCodeForProcessorValidator . . .");
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

function addFieldValidationForProcessorItaly() {
  var requestType = null;
  requestType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole');
  var custSubType = FormManager.getActualValue('custSubGrp');
  var reqId = FormManager.getActualValue('reqId');

  if (requestType == 'C' && role == GEOHandler.ROLE_PROCESSOR) {
    var checkImportIndc = getImportedIndcForItaly();
    FormManager.resetValidations('taxCd1');
    FormManager.addValidator('salesBusOffCd', Validators.REQUIRED, [ 'SBO (SORTL)' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('identClient', Validators.REQUIRED, [ 'Ident Client' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('repTeamMemberNo', Validators.REQUIRED, [ 'Sales Rep' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('subIndustryCd', Validators.REQUIRED, [ 'Subindustry' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('specialTaxCd', Validators.REQUIRED, [ 'Tax Code/ Code IVA' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('abbrevNm', Validators.REQUIRED, [ 'Abbreviated Name (TELX1)' ], 'MAIN_CUST_TAB');

    if (checkImportIndc != 'Y'
        && (custSubType == '3PAIT' || custSubType == '3PASM' || custSubType == '3PAVA' || custSubType == 'PRICU' || custSubType == 'PRISM' || custSubType == 'PRIVA' || custSubType == 'GOVST'
            || custSubType == 'GOVSM' || custSubType == 'GOVVA' || custSubType == 'CROGO' || custSubType == 'UNIVE' || custSubType == 'UNISM' || custSubType == 'UNIVA' || custSubType == 'CROUN'
            || custSubType == 'LOCEN' || custSubType == 'LOCSM' || custSubType == 'LOCVA' || custSubType == 'CROLC' || custSubType == 'COMME' || custSubType == 'COMSM' || custSubType == 'COMVA'
            || custSubType == 'CROCM' || custSubType == 'NGOIT' || custSubType == 'NGOSM' || custSubType == 'NGOVA' || custSubType == 'BUSPR' || custSubType == 'BUSSM' || custSubType == 'BUSVA' || custSubType == 'CROBP')) {
      FormManager.resetValidations('enterprise');
      FormManager.resetValidations('affiliate');
      if ("" != FormManager.getActualValue('isuCd') && ("34" != FormManager.getActualValue('isuCd') || "21" != FormManager.getActualValue('isuCd') || "27" != FormManager.getActualValue('isuCd'))) {
        FormManager.addValidator('enterprise', Validators.REQUIRED, [ 'Enterprise' ], 'MAIN_IBM_TAB');
        FormManager.addValidator('affiliate', Validators.REQUIRED, [ 'Affiliate' ], 'MAIN_IBM_TAB');
        FormManager.enable('enterprise');
        FormManager.enable('affiliate');
      }
      if ("34" == FormManager.getActualValue('isuCd') || "21" == FormManager.getActualValue('isuCd')) {
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
}

function addAddrValidationForProcItaly() {
  console.log("Inside addAddrValidationForProcItaly")
  var requestType = null;
  requestType = FormManager.getActualValue('reqType');
  var addrType = FormManager.getActualValue('addrType');
  var landCntryVal = FormManager.getActualValue('landCntry');
  if (requestType == 'C' && addrType != 'ZS01') {
    if (landCntryVal == 'IT') {
      FormManager.addValidator('stateProv', Validators.REQUIRED, [ 'State/Province' ], null);
    }
  }
}

function landedCntryLockedIT() {
  var addrType = FormManager.getActualValue('addrType');
  var issuingCntryVal = FormManager.getActualValue('cmrIssuingCntry');
  var landCntry = FormManager.getActualValue('landCntry');

  if (issuingCntryVal == '758' && cmr.currentRequestType == 'U') {
    if (addrType == 'ZS01') {
      FormManager.readOnly('landCntry');
    } else if (addrType == 'ZI01' || addrType == 'ZP01') {
      FormManager.enable('landCntry');
    }
  }
}

function afterConfigForIT() {

  if (_landCntryHandler == null) {
    _landCntryHandler = dojo.connect(FormManager.getField('landCntry'), 'onChange', function(value) {
      showHideCityStateProvIT();
      if (FormManager.getActualValue('addrType') == 'ZS01') {
        setDeafultSBOLogicComm(FormManager.getActualValue('postCd'));
      }
    });
  }

  if (_stateProvITHandler == null) {
    _stateProvITHandler = dojo.connect(FormManager.getField('stateProvItaly'), 'onChange', function(value) {
      showHideOtherStateProvIT();
    });
  }

  if (_postCdHandler == null && FormManager.getField('postCd')) {
    _postCdHandler = dojo.connect(FormManager.getField('postCd'), 'onChange', function(value) {
      if (value != '' && FormManager.getActualValue('addrType') == 'ZS01') {
        setDeafultSBOLogicComm(value);
      }
    });
  }

  if (_fiscalCodeUpdateHandlerIT == null) {
    _fiscalCodeUpdateHandlerIT = dojo.connect(FormManager.getField('taxCd1'), 'onChange', function(value) {
      autoResetFiscalDataStatus();
      autoPopulateIdentClientIT();
    });
  }

  if (_ISUHandlerIT == null) {
    _ISUHandlerIT = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
      setAffiliateEnterpriseRequired();
      ibmFieldsBehaviourInCreateByScratchIT();
      lockCollectionCode();
      // setDeafultISUCtcChange();
    });
  }

  if (_isuCdHandler == null && FormManager.getField('isuCd')) {
    _isuCdHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
      setDeafultSBOLogicComm();
    });
  }
  if (_clientTierHandler == null && FormManager.getField('clientTier')) {
    _clientTierHandler = dojo.connect(FormManager.getField('clientTier'), 'onChange', function(value) {
      setDeafultSBOLogicComm();
    });
  }

  if (_CTCHandlerIT == null) {
    _CTCHandlerIT = dojo.connect(FormManager.getField('clientTier'), 'onChange', function(value) {
      ibmFieldsBehaviourInCreateByScratchIT();
      lockCollectionCode();
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
      autoPopulateIdentClientIT();
    });
  }

  if (_custGrpIT == null) {
    _custGrpIT = dojo.connect(FormManager.getField('custGrp'), 'onChange', function(value) {
      setSpecialTaxCodeOnScenarioIT();
    });
  }

  if (_sboHandlerIT == null) {
    _sboHandlerIT = dojo.connect(FormManager.getField('salesBusOffCd'), 'onChange', function(value) {
      var isuCode = FormManager.getActualValue('isuCd');
      var ctc = FormManager.getActualValue('clientTier');
      if (isuCode == '' || ctc == '') {
        setCollCdOnSBOIT(value);
      }
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
  if (reqType == 'U' || reqType == 'X') {
    var reqId = FormManager.getActualValue('reqId');
    var result = cmr.query('VALIDATOR.BILLING.CROSSBORDERIT', {
      REQID : reqId
    });

    if (result != null && result.ret1 != '' && result.ret1 != undefined && result.ret1 != 'IT') {
      FormManager.setValue('taxCd1', '');
      FormManager.readOnly('taxCd1');
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
        FormManager.readOnly('repTeamMemberNo');
        FormManager.readOnly('salesBusOffCd');
        FormManager.readOnly('affiliate');
        FormManager.resetValidations('clientTier');
        // FormManager.readOnly('inacCd');
      }
      if (custSubType == '3PAIT' || custSubType == '3PASM' || custSubType == '3PAVA') {
        FormManager.enable('inacCd');
      }
      FormManager.readOnly('identClient');
      if (custSubType == 'INTER' || custSubType == 'CROIN' || custSubType == 'INTSM' || custSubType == 'INTVA') {
        FormManager.readOnly('salesBusOffCd');
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
    if (_countryUse != '' && _landCntry != '' && _landCntry == 'IT') {
      FormManager.setValue('taxCd1', '');
    }
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
        FormManager.readOnly('repTeamMemberNo');
        FormManager.readOnly('salesBusOffCd');
        FormManager.readOnly('affiliate');
        FormManager.resetValidations('clientTier');
        // FormManager.readOnly('inacCd');
      }
      if (custSubType == '3PAIT' || custSubType == '3PASM' || custSubType == '3PAVA') {
        FormManager.enable('inacCd');
      }
      FormManager.readOnly('identClient');
      if (custSubType == 'INTER' || custSubType == 'CROIN' || custSubType == 'INTSM' || custSubType == 'INTVA') {
        FormManager.readOnly('salesBusOffCd');
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
    if (_countryUse != '' && _landCntry != '' && _landCntry == 'IT') {
      FormManager.setValue('taxCd1', '');
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
  addAfterTemplateLoadIT(false, false, false);
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    FormManager.readOnly('cmrNo');
  }
}

// CREATCMR-5089 ITALY - coverage 2H22 updates sbo should not be set based on
// isu/ctc
/**
 * function setSboValueBasedOnIsuCtcIT() { if
 * (FormManager.getActualValue('viewOnlyPage') == 'true') { return; } var isuCd =
 * FormManager.getActualValue('isuCd'); var clientTier =
 * FormManager.getActualValue('clientTier'); var reqType =
 * FormManager.getActualValue('reqType'); var isuList = [ '34', '5K', '14',
 * '19', '3T', '4A' ]; if (!isuList.includes(isuCd)) { //
 * FormManager.setValue('salesBusOffCd', ''); return; } if (isuCd == '34' &&
 * clientTier == 'Y') { FormManager.setValue('salesBusOffCd', 'FL'); } else if
 * (isuCd == '5K' && clientTier == '') { FormManager.setValue('salesBusOffCd',
 * '99'); } else if (isuCd == '14' && clientTier == '') {
 * FormManager.setValue('salesBusOffCd', 'TX'); } else if (isuCd == '19' &&
 * clientTier == '') { FormManager.setValue('salesBusOffCd', 'EB'); } else if
 * (isuCd == '3T' && clientTier == '') { FormManager.setValue('salesBusOffCd',
 * 'TF'); } else if (isuCd == '4A' && clientTier == '') {
 * FormManager.setValue('salesBusOffCd', 'XD'); } if (isuList.slice(2,
 * 6).includes(isuCd) && reqType == 'C') {
 * FormManager.resetValidations('clientTier');
 * FormManager.setValue('clientTier', ''); FormManager.readOnly('clientTier'); } }
 */
var oldIsuCdValueUK = null;
function setSboValueBasedOnIsuCtcUK(value) {
  var isuCd = FormManager.getActualValue('isuCd');
  var clientTier = FormManager.getActualValue('clientTier');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var reqType = FormManager.getActualValue('reqType');
  var isuList = [ '34', '5K', '14', '19', '3T', '4A' ];
  if (!isuList.includes(isuCd)) {
    // FormManager.setValue('salesBusOffCd', '');
    return;
  }
  if (isuCd == '34' && clientTier == 'Y') {
    FormManager.setValue('salesBusOffCd', 'FL');
  } else if (isuCd == '5K' && clientTier == '') {
    FormManager.setValue('salesBusOffCd', '99');
  } else if (isuCd == '14' && clientTier == '') {
    FormManager.setValue('salesBusOffCd', 'TX');
  } else if (isuCd == '19' && clientTier == '') {
    FormManager.setValue('salesBusOffCd', 'EB');
  } else if (isuCd == '3T' && clientTier == '') {
    FormManager.setValue('salesBusOffCd', 'TF');
  } else if (isuCd == '4A' && clientTier == '') {
    FormManager.setValue('salesBusOffCd', 'XD');
  }
  if (isuList.slice(2, 6).includes(isuCd) && reqType == 'C') {
    FormManager.resetValidations('clientTier');
    FormManager.setValue('clientTier', '');
    FormManager.readOnly('clientTier');
  }
}

var oldIsuCdValueUK = null;
var oldIsuCdValueIE = null;
function setSboValueBasedOnIsuCtcIE(value) {
  var reqType = FormManager.getActualValue('reqType');
  var isuCd = FormManager.getActualValue('isuCd');
  var clientTier = FormManager.getActualValue('clientTier');
  var reqType = FormManager.getActualValue('reqType');
  var scenario = FormManager.getActualValue('custGrp');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var isuList = [ '34', '36', '32' ];

  if (reqType == 'U') {
    return;
  }
  if ((value != false || value == undefined || value == null) && clientTier != '' && isuCd != '' && oldIsuCdValueIE == null) {
    oldIsuCdValueIE = isuCd + clientTier;
  }

  if (oldIsuCdValueIE == null) {
    return;
  }

  if (isuCd == '5K' && clientTier == '') {
    FormManager.setValue('salesBusOffCd', '000');
    FormManager.setValue('repTeamMemberNo', 'SPA000');
  } else if (isuCd == '32' && clientTier == 'T') {
    FormManager.setValue('salesBusOffCd', '123');
    FormManager.setValue('repTeamMemberNo', 'SPA123');
  } else if (isuCd == '34' && clientTier == 'Q' && getZS01LandCntry() == 'GB' && scenario == 'CROSS') {
    FormManager.setValue('salesBusOffCd', '057');
    FormManager.setValue('repTeamMemberNo', 'SPA057');
  } else if (isuCd == '34' && clientTier == 'Q') {
    FormManager.setValue('salesBusOffCd', '090');
    FormManager.setValue('repTeamMemberNo', 'MMIR11');
  } else if (isuCd == '21' && clientTier == '') {
    if (custSubGrp == 'BUSPR' || custSubGrp == 'INTER' || custSubGrp == 'IBMEM') {
      FormManager.setValue('salesBusOffCd', '000');
      FormManager.setValue('repTeamMemberNo', 'SPA000');
    } else if (custSubGrp == 'PRICU') {
      FormManager.setValue('salesBusOffCd', '090');
      FormManager.setValue('repTeamMemberNo', 'MMIR11');
    }
  } else {
    FormManager.setValue('salesBusOffCd', '');
    FormManager.setValue('repTeamMemberNo', '');
  }
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
    // FormManager.readOnly('stateProvItaly');

    FormManager.addValidator('city1', Validators.REQUIRED, [ 'City' ], null);
    FormManager.setValue('crossbCntryStateProvMapIT', '');
    FormManager.setValue('crossbCntryStateProvMapIT', landCntryVal);
    // FormManager.setValue('stateProvItaly',
    // getDescription('crossbCntryStateProvMapIT'));

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
    FormManager.addValidator('stateProv', Validators.REQUIRED, [ 'State/Province' ], null);
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

/**
 * CMR-2093:Turkey - show CoF field for Update request only
 */
function showCommercialFinanced() {
  if (FormManager.getActualValue('reqType') != 'U' || FormManager.getActualValue('cmrIssuingCntry') != SysLoc.TURKEY) {
    FormManager.clearValue('commercialFinanced');
    FormManager.hide('CommercialFinanced', 'commercialFinanced');
  } else {
    FormManager.show('CommercialFinanced', 'commercialFinanced');
    FormManager.limitDropdownValues(FormManager.getField('commercialFinanced'), [ 'R', 'S', 'T' ]);
    FormManager.show('CustClass', 'custClass');
    var role = FormManager.getActualValue('userRole').toUpperCase();
    if (role == 'REQUESTER') {
      // FormManager.readOnly('custClass');
    } else {
      FormManager.enable('custClass');
    }
  }
}

function setDeafultISUCtcChange() {
  var checkImportIndc = getImportedIndcForItaly();
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var reqType = FormManager.getActualValue('reqType');
  if (reqType == 'C') {
    var custSubType = FormManager.getActualValue('custSubGrp');
    var commSubTypes = [ 'COMME', 'COMSM', 'COMVA', 'CROCM', 'GOVST', 'LOCEN', 'GOVSM', 'LOCSM', 'GOVVA', 'LOCVA', 'CROGO', 'NGOIT', 'NGOVA', 'NGOSM', '3PAIT', 'UNIVA', 'UNIVE', 'UNISM', '3PASM',
        '3PAVA', 'CRO3P', 'CROUN', 'CROLC' ];
    var ibmEmpCustSubTypes = [ 'IBMIT', 'XIBM' ];
    var bpCustTypes = [ 'BUSPR', 'BUSSM', 'BUSVA', 'CROBP' ];
    var internalCustSubTypes = [ 'INTER', 'INTVA', 'INTSM', 'CROIN' ];
    var pripeCustSubTypes = [ 'CROPR', 'PRICU', 'PRISM', 'PRIVA' ];
    var validIsuCTCs = [ '27E', '34Q', '36Y', '04', '12', '15', '19', '28', '31', '1R', '3T', '4A', '4F', '5B', '5E', '5K' ];
    var isu = FormManager.getActualValue('isuCd');
    var ctc = FormManager.getActualValue('clientTier');
    if (internalCustSubTypes.includes(custSubType)) {
      FormManager.clearValue('collectionCd');
    } else if (commSubTypes.includes(custSubType)) {
      FormManager.enable('isuCd');
      // FormManager.limitDropdownValues(FormManager.getField('isuCd'), [ '32',
      // '34', '36', '04', '19', '28', '14', '4A', '3T', '5K' ]);
      FormManager.enable('clientTier');
      FormManager.readOnly('repTeamMemberNo');
      FormManager.setValue('repTeamMemberNo', '012345');
      FormManager.enable('salesBusOffCd');
      if (!validIsuCTCs.includes(isu.concat(ctc))) {
        FormManager.setValue('isuCd', '27');
        FormManager.setValue('clientTier', 'E');
      }
    } else if (ibmEmpCustSubTypes.includes(custSubType)) {
      FormManager.clearValue('collectionCd');
    } else if (bpCustTypes.includes(custSubType)) {
      FormManager.readOnly('isuCd');
      FormManager.setValue('isuCd', '8B');
      FormManager.readOnly('clientTier');
      FormManager.setValue('clientTier', '');
      if (role == 'REQUESTER') {
        FormManager.readOnly('repTeamMemberNo');
        FormManager.readOnly('salesBusOffCd');
      } else if (role == 'PROCESSOR') {
        FormManager.enable('repTeamMemberNo');
        FormManager.enable('salesBusOffCd');

      }
      FormManager.setValue('repTeamMemberNo', '09ZPB0');
      FormManager.setValue('salesBusOffCd', 'ZP');

    } else if (pripeCustSubTypes.includes(custSubType)) {
      FormManager.readOnly('isuCd');
      FormManager.setValue('isuCd', '27');
      FormManager.readOnly('clientTier');
      FormManager.setValue('clientTier', 'E');
      FormManager.readOnly('repTeamMemberNo');
      FormManager.setValue('repTeamMemberNo', '012345');
    }
    setDeafultSBOLogicComm();
  }

  // if (_isuCdHandler == null && FormManager.getField('isuCd')) {
  // _isuCdHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange',
  // function(value) {
  // setDeafultSBOLogicComm();
  // });
  // }
  // if (_clientTierHandler == null && FormManager.getField('clientTier')) {
  // _clientTierHandler = dojo.connect(FormManager.getField('clientTier'),
  // 'onChange', function(value) {
  // setDeafultSBOLogicComm();
  // });
  // }
}

function setDeafultSBOLogicComm(postalCd) {

  var role = FormManager.getActualValue('userRole').toUpperCase();
  var reqType = FormManager.getActualValue('reqType');
  var custGrp = FormManager.getActualValue('custGrp');
  var custSubType = FormManager.getActualValue('custSubGrp');
  var reqId = FormManager.getActualValue('reqId');
  var subRegion = FormManager.getActualValue('countryUse');
  var isu = FormManager.getActualValue('isuCd');
  var ctc = FormManager.getActualValue('clientTier');
  var subIndustryCd = FormManager.getActualValue('subIndustryCd');
  if (subIndustryCd == '') {
    subIndustryCd = _pagemodel.subIndustryCd;
  }
  if (subIndustryCd != null && subIndustryCd.length > 1) {
    subIndustryCd = subIndustryCd.substring(0, 1);
  }
  var commSubTypes = [ 'COMME', 'COMSM', 'COMVA', 'CROCM', 'GOVST', 'LOCEN', 'GOVSM', 'LOCSM', 'GOVVA', 'LOCVA', 'CROGO', 'NGOIT', 'NGOVA', 'NGOSM', '3PAIT', 'UNIVA', 'UNIVE', 'UNISM', '3PASM',
      '3PAVA', 'CRO3P', 'CROUN', 'CROLC' ];
  var ibmEmpCustSubTypes = [ 'IBMIT', 'XIBM' ];
  var bpCustTypes = [ 'BUSPR', 'BUSSM', 'BUSVA', 'CROBP' ];
  var internalCustSubTypes = [ 'INTER', 'INTVA', 'INTSM', 'CROIN' ]
  var pripeCustSubTypes = [ 'CROPR', 'PRICU', 'PRISM', 'PRIVA' ];
  var landCntry = '';
  var isuCTC = isu.concat(ctc);

  var result = cmr.query('VALIDATOR.POSTCODEIT', {
    REQID : reqId
  });
  var postCd = '';
  var postCd3 = '';
  if (postalCd == undefined || postalCd == '') {
    postalCd = FormManager.getActualValue('postCd')
  }
  if (postalCd != null && postalCd != undefined && postalCd != '') {
    postCd = postalCd;
  } else if (result != null && result.ret1 != undefined) {
    postCd = result.ret1;
  } else {
    postCd = '';
  }
  if (postCd != null && postCd.length > 2) {
    postCd3 = postCd.substring(0, 3);
    postCd = postCd.substring(0, 2);

  }

  // GET LANDCNTRY in case of CB
  var result1 = cmr.query('LANDCNTRY.IT', {
    REQID : reqId
  });
  if (result1 != null && result1.ret1 != undefined) {
    landCntry = result1.ret1;
  }

  if (reqType == 'U' || FormManager.getActualValue('viewOnlyPage') == 'true' || postCd == '' || postCd == undefined || postCd == null) {
    return;
  }

  if ((commSubTypes.includes(custSubType) || pripeCustSubTypes.includes(custSubType)) && isuCTC == '27E' && subRegion == '758VA') {
    FormManager.setValue('salesBusOffCd', 'NC');
    return;
  } else if ((commSubTypes.includes(custSubType) || pripeCustSubTypes.includes(custSubType)) && isuCTC == '27E' && subRegion == '758SM') {
    FormManager.setValue('salesBusOffCd', 'DU');
    return;
  } else if ((commSubTypes.includes(custSubType) || pripeCustSubTypes.includes(custSubType)) && custGrp == 'CROSS' && isuCTC == '27E') {
    FormManager.setValue('salesBusOffCd', 'NB');
    return;
  }

  if (commSubTypes.includes(custSubType) || pripeCustSubTypes.includes(custSubType)) {
    if (isuCTC == '27E') {
      FormManager.setValue('repTeamMemberNo', '012345');

      // postal Cd logic for SBO assignment
      var PostCdList1 = [ '05', '06', '50', '51', '52', '53', '54', '55', '56', '57', '58', '59' ];
      var PostCdList2 = [ '07', '08', '09', '60', '61', '62', '63', '64', '65', '66', '67', '70', '71', '72', '73', '74', '75', '76', '85', '86', '87', '88', '89', '90', '91', '92', '93', '94', '95',
          '96', '97', '98' ];
      var PostCdList3 = [ '20', '21', '22', '23', '24', '25', '26', '27' ];
      var PostCdList4 = [ '33', '34', '38', '39', '46' ];
      var PostCdList5 = [ '80', '81', '82', '83', '84' ];
      var PostCdList6 = [ '00', '01', '02', '03', '04' ];
      var PostCdList7 = [ '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', '28' ];
      var PostCdList8 = [ '200', '201' ];
      var PostCdList9 = [ '29', '40', '41', '42', '43', '44', '47', '48' ];
      var PostCdList10 = [ '30', '31', '32', '35', '36', '37', '45' ];

      if (PostCdList1.includes(postCd) && custGrp != 'CROSS') {
        FormManager.setValue('salesBusOffCd', 'NM');
      } else if (PostCdList2.includes(postCd) && custGrp != 'CROSS') {
        FormManager.setValue('salesBusOffCd', 'RP');
      } else if (PostCdList3.includes(postCd) && !(postCd3 == '200' || postCd3 == '201') && custGrp != 'CROSS') {
        FormManager.setValue('salesBusOffCd', 'GJ');
      } else if (PostCdList4.includes(postCd) && custGrp != 'CROSS') {
        FormManager.setValue('salesBusOffCd', 'GK');
      } else if (PostCdList5.includes(postCd) && custGrp != 'CROSS') {
        FormManager.setValue('salesBusOffCd', 'NG');
      } else if ((PostCdList6.includes(postCd) || subRegion == '758VA') && custGrp != 'CROSS') {

        if ([ 'D', 'R', 'G', 'T', 'W' ].includes(subIndustryCd) || subRegion == '758VA') {
          FormManager.setValue('salesBusOffCd', 'NC');
        } else if ([ 'A', 'B', 'C', 'E', 'F', 'H', 'J', 'K', 'L', 'M', 'N', 'P', 'S', 'U', 'V', 'X', 'Y' ].includes(subIndustryCd)) {
          FormManager.setValue('salesBusOffCd', 'KA');
        }
      } else if ((PostCdList7.includes(postCd) || custGrp == 'CROSS') && !(subRegion == '758VA' || subRegion == '758SM')) {
        if ([ 'D', 'R', 'G', 'T', 'W' ].includes(subIndustryCd) || custGrp != 'CROSS') {
          FormManager.setValue('salesBusOffCd', 'KF');
        } else if ([ 'A', 'B', 'C', 'E', 'F', 'H', 'J', 'K', 'L', 'M', 'N', 'P', 'S', 'U', 'V', 'X', 'Y' ].includes(subIndustryCd) || custGrp == 'CROSS') {
          FormManager.setValue('salesBusOffCd', 'NB');
        }
      } else if (PostCdList8.includes(postCd3) && custGrp != 'CROSS') {
        if ([ 'D', 'R', 'G', 'T', 'W' ].includes(subIndustryCd)) {
          FormManager.setValue('salesBusOffCd', 'GH');
        } else if ([ 'F', 'N', 'S' ].includes(subIndustryCd)) {
          FormManager.setValue('salesBusOffCd', 'KC');
        } else if ([ 'A', 'B', 'C', 'E', 'H', 'J', 'K', 'L', 'M', 'P', 'U', 'V', 'X', 'Y' ].includes(subIndustryCd)) {
          FormManager.setValue('salesBusOffCd', 'KB');
        }
      } else if (PostCdList9.includes(postCd) && custGrp != 'CROSS') {
        if ([ 'D', 'R', 'G', 'T', 'W' ].includes(subIndustryCd)) {
          FormManager.setValue('salesBusOffCd', 'DU');
        } else if ([ 'A', 'B', 'C', 'E', 'F', 'H', 'J', 'K', 'L', 'M', 'N', 'P', 'S', 'U', 'V', 'X', 'Y' ].includes(subIndustryCd)) {
          FormManager.setValue('salesBusOffCd', 'KE');
        }
      } else if (PostCdList10.includes(postCd) && custGrp != 'CROSS') {
        if ([ 'D', 'R', 'G', 'T', 'W' ].includes(subIndustryCd)) {
          FormManager.setValue('salesBusOffCd', 'KD');
        } else if ([ 'A', 'B', 'C', 'E', 'F', 'H', 'J', 'K', 'L', 'M', 'N', 'P', 'S', 'U', 'V', 'X', 'Y' ].includes(subIndustryCd)) {
          FormManager.setValue('salesBusOffCd', 'NI');
        }
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
  /*
   * var cntryWhereVATIsMandatory = [ 'AT', 'BE', 'BG', 'HR', 'CY', 'CZ', 'EG',
   * 'FR', 'DE', 'GR', 'HU', 'IE', 'IL', 'IT', 'LU', 'MT', 'NL', 'PK', 'PL',
   * 'PT', 'RO', 'RU', 'RS', 'SK', 'SI', 'ZA', 'ES', 'UA', 'GB', 'TR', 'LV',
   * 'LT', 'DK', 'EE', 'FI', 'SE' ];
   */
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
    // FormManager.resetValidations('vat');
    // FormManager.enable('vat');
    FormManager.resetValidations('taxCd1');
    FormManager.limitDropdownValues(FormManager.getField('identClient'), identClientValuesCross);

  }
  // else if (scenario == 'LOCAL' && checkImportIndc != 'Y') {
  // if (custSubType == 'COMME') {
  // FormManager.limitDropdownValues(FormManager.getField('identClient'), [ 'A',
  // 'D' ]);
  // } else if (custSubType == 'BUSPR') {
  // FormManager.limitDropdownValues(FormManager.getField('identClient'), [ 'A',
  // 'D' ]);
  // } else if (custSubType == 'PRICU') {
  // FormManager.limitDropdownValues(FormManager.getField('identClient'), [ 'X'
  // ]);
  // } else if (custSubType == 'LOCEN' || custSubType == 'UNIVE' || custSubType
  // == 'GOVST') {
  // FormManager.limitDropdownValues(FormManager.getField('identClient'), [ 'B',
  // 'C' ]);
  // } else if (custSubType == 'NGOIT') {
  // FormManager.limitDropdownValues(FormManager.getField('identClient'), [ 'B'
  // ]);
  // } else {
  // FormManager.limitDropdownValues(FormManager.getField('identClient'),
  // identClientValuesLocal);
  // }
  // } else if (scenario == 'LOCAL' && (custSubType != 'IBMIT') &&
  // checkImportIndc == 'Y') {
  // FormManager.limitDropdownValues(FormManager.getField('identClient'),
  // identClientValuesLocal);
  // }
  else {
    FormManager.limitDropdownValues(FormManager.getField('identClient'), identClientValuesLocal);
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

  if (custSubType != undefined && checkImportIndc != 'Y' && (custSubType == 'COMME' || custSubType == 'BUSPR' || custSubType == '3PAIT')) {
    if ((fiscalCode != '' && fiscalCode.length == 11) && (vat != '' && vat.length == 13)) {
      FormManager.setValue('identClient', 'A');
    }
    if ((fiscalCode != '' && fiscalCode.length == 16) && (vat != '' && vat.length == 13)) {
      FormManager.setValue('identClient', 'D');
    }
  }
  if (checkImportIndc == 'Y') {
    getOldValuesIT();
  }
}

/*
 * function autoSetIdentClientForBillingIT() { var reqType =
 * FormManager.getActualValue('reqType'); if (reqType != 'C') { return new
 * ValidationResult(null, true); } var addrType =
 * FormManager.getActualValue('addrType'); var scenario =
 * FormManager.getActualValue('custGrp'); if (scenario != null &&
 * scenario.includes('CRO')) { scenario = 'CROSS'; } if (addrType != null &&
 * addrType == 'ZP01' && scenario != null && scenario != '' && scenario ==
 * 'CROSS') { autoPopulateIdentClientIT(); } }
 */

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

function addBillingValidator() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var zp01ReqId = FormManager.getActualValue('reqId');
        qParams = {
          REQ_ID : zp01ReqId,
        };
        var record = cmr.query('GETZP01VALRECORDS', qParams);
        var custSubType = FormManager.getActualValue('custSubGrp');
        var zp01Reccount = record.ret1;
        if (Number(zp01Reccount == 1)) {
          if (FormManager.getActualValue('reqType') == 'C' && role == "REQUESTER") {
            if (custSubType == '3PAIT' || custSubType == '3PASM' || custSubType == '3PAVA' || custSubType == 'CRO3P') {
              var checkImportIndc = getImportedIndcForItalyBillingAddr();
              if (checkImportIndc == 'Y') {
                return new ValidationResult(null, false, 'Please remove the imported billing address and create a new one for the 3rd party customer.');
              }
            }
          }
        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function addCMRValidator() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('reqType') == 'C') {
          if (FormManager.getActualValue('findCmrResult') == 'Not Done' || FormManager.getActualValue('findCmrResult') == 'Rejected' || FormManager.getActualValue('findCmrResult') == 'No Results') {
            var custSubType = FormManager.getActualValue('custSubGrp');
            if (role == "REQUESTER" && (custSubType == '3PAIT' || custSubType == '3PASM' || custSubType == '3PAVA' || custSubType == 'CRO3P')) {
              return new ValidationResult(null, false, 'For 3rd party scenario please import a CMR via CMR search');
            }
          }
        }
      }
    };
  })(), 'MAIN_GENERAL_TAB', 'frmCMR');
}

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

/*
 * function autoSetVATForBilling() { var reqType =
 * FormManager.getActualValue('reqType'); if (reqType != 'C') { return new
 * ValidationResult(null, true); } var addrType =
 * FormManager.getActualValue('addrType'); if (addrType != null && addrType ==
 * 'ZP01') { setVATForItaly(); } }
 */
/* 1430539 - do not allow delete of imported addresses on update requests */

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
  // autoSetValuesOnPostalCodeIT(addressMode);
}
var _isScenarioChanged = false;
function checkScenarioChanged(fromAddress, scenario, scenarioChanged) {
  _isScenarioChanged = scenarioChanged;
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

function setAffiliateEnterpriseRequired() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custType = FormManager.getActualValue('custGrp');
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
        } else if (isu == '34' || isu == '27') {
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
    }
  }
}

// IBM Tab Fields Behaviour In CreateByModel
function ibmFieldsBehaviourInCreateByModelIT() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubType = FormManager.getActualValue('custSubGrp');
  if (FormManager.getActualValue('reqType') == 'C') {
    var checkImportIndc = getImportedIndcForItaly();
    if (FormManager.getActualValue('viewOnlyPage') == 'true') {
      return;
    }
    if ('C' == FormManager.getActualValue('reqType') && checkImportIndc == 'Y') {

      if (role == "REQUESTER" && !(custSubType == '3PAIT' || custSubType == '3PASM' || custSubType == '3PAVA' || custSubType == 'CRO3P')) {
        // FormManager.enable('isuCd');
        // FormManager.readOnly('inacCd');
        // FormManager.enable('clientTier');
        FormManager.enable('collectionCd');
        // FormManager.enable('repTeamMemberNo');
        // FormManager.enable('salesBusOffCd');
        FormManager.enable('affiliate');
        FormManager.removeValidator('isuCd', Validators.REQUIRED);
        FormManager.removeValidator('clientTier', Validators.REQUIRED);
        FormManager.removeValidator('salesBusOffCd', Validators.REQUIRED);
        FormManager.removeValidator('repTeamMemberNo', Validators.REQUIRED);
      } else {
        // FormManager.enable('isuCd');
        FormManager.enable('inacCd');
        FormManager.enable('affiliate');
        // FormManager.enable('clientTier');
        // FormManager.enable('salesBusOffCd');
        // FormManager.enable('repTeamMemberNo');
        FormManager.addValidator('isuCd', Validators.REQUIRED, [ 'ISU' ], 'MAIN_IBM_TAB');
        FormManager.addValidator('salesBusOffCd', Validators.REQUIRED, [ 'SBO' ], 'MAIN_IBM_TAB');
        // FormManager.addValidator('clientTier', Validators.REQUIRED, [ 'Client
        // Tier' ], 'MAIN_IBM_TAB');
        FormManager.addValidator('repTeamMemberNo', Validators.REQUIRED, [ 'Sales Rep' ], 'MAIN_IBM_TAB');
      }
      if (custSubType == 'INTER' || custSubType == 'INTSM' || custSubType == 'INTVA' || custSubType == 'CROIN' || custSubType == 'BUSPR' || custSubType == 'BUSSM' || custSubType == 'BUSVA'
          || custSubType == 'CROBP') {
        FormManager.readOnly('inacCd');
        FormManager.readOnly('affiliate');
        FormManager.clearValue('inacCd');
        FormManager.clearValue('affiliate');
      }
      if (role == "REQUESTER" && (custSubType == 'INTER' || custSubType == 'INTSM' || custSubType == 'INTVA' || custSubType == 'CROIN')) {
        FormManager.enable('collectionCd');
        FormManager.readOnly('salesBusOffCd');
        // FormManager.clearValue('salesBusOffCd');
        if (FormManager.getActualValue('salesBusOffCd') == '') {
          FormManager.clearValue('collectionCd');
        }
        FormManager.removeValidator('salesBusOffCd', Validators.REQUIRED);
        FormManager.removeValidator('repTeamMemberNo', Validators.REQUIRED);
      }
      if (role == 'REQUESTER' && (custSubType == 'CROPR' || custSubType == 'PRISM' || custSubType == 'PRIVA')) {
        FormManager.clearValue('collectionCd');
        FormManager.enable('collectionCd');
      }
      if (role == 'REQUESTER' && custSubType == 'PRICU') {
        FormManager.enable('collectionCd');
      }

      if (custSubType == 'IBMIT' || custSubType == 'XIBM') {
        FormManager.readOnly('identClient');
        FormManager.enable('collectionCd');
        getOldValuesIT();
      }
      FormManager.removeValidator('collectionCd', Validators.REQUIRED);
      var custType = FormManager.getActualValue('custGrp');
      if (custType == 'CROSS') {
        FormManager.enable('vat');
      }

      var isuCd = FormManager.getActualValue('isuCd');
      setClientTierValuesIT(isuCd);
    }
  }
}

function getOldValuesIT(fromAddress, scenario, scenarioChanged) {
  if (scenarioChanged) {
    var custSubType = scenario;
    var reqId = FormManager.getActualValue('reqId');
    var checkImportIndc = getImportedIndcForItaly();
    var addrType = FormManager.getActualValue('addrType');
    if (_oldISUIT == "" && _oldCTCIT == "") {
      var result = cmr.query("GET.CMRINFO.IMPORTED", {
        REQ_ID : reqId
      });
      if (result != null && result.ret1 != '' && result.ret1 != null) {
        _oldISUIT = result.ret1;
        _oldCTCIT = result.ret2;
        _oldSalesRepIT = result.ret3;
        _oldSBOIT = result.ret4;
        _oldCollectionIT = result.ret5;
        _oldINACIT = result.ret6;
        _oldSpecialTaxCdIT = result.ret7;
        _oldAffiliateIT = result.ret8;
        _oldIdentClientIT = result.ret9;
        _oldVatIT = result.ret10;
      }
    }

    // Story 1594125: Company number field should be locked
    FormManager.readOnly('company');
    // Defect 1553451
    var role = FormManager.getActualValue('userRole').toUpperCase();
    if (FormManager.getActualValue('reqType') == 'C' && role == 'REQUESTER') {
      // if ((custSubType != undefined &&
      // FormManager.getActualValue('isuCd') !=
      // undefined)
      // && (custSubType == 'COMME' || custSubType == 'COMSM' ||
      // custSubType ==
      // 'COMVA' || custSubType == 'CROCM' || custSubType == 'NGOIT' ||
      // custSubType == 'NGOSM' || custSubType == 'NGOVA'
      // || custSubType == 'PRICU' || custSubType == 'PRISM' ||
      // custSubType ==
      // 'PRIVA') && (FormManager.getActualValue('isuCd') == '32') &&
      // FormManager.getField('clientTier')) {
      // FormManager.readOnly('repTeamMemberNo');
      // FormManager.readOnly('salesBusOffCd');
      // }
      // Defect 1556300 (Old Defect 1526869)
      if (custSubType == 'GOVST' || custSubType == 'GOVSM' || custSubType == 'GOVVA' || custSubType == 'CROGO' || custSubType == 'LOCEN' || custSubType == 'LOCSM' || custSubType == 'LOCVA'
          || custSubType == 'CROLC' || custSubType == 'UNIVE' || custSubType == 'UNISM' || custSubType == 'UNIVA' || custSubType == 'CROUN') {
        FormManager.addValidator('repTeamMemberNo', Validators.REQUIRED, [ 'Sales Rep' ], 'MAIN_IBM_TAB');
      } else {
        FormManager.resetValidations('repTeamMemberNo');
      }

      // Story 1544496: Imported codes should not get overwritten
      if (checkImportIndc == 'Y') {
        console
            .log(">getOldValuesIT> importIndc is Y -Non editable fields- are ('clientTier', 'isuCd', 'repTeamMemberNo', 'salesBusOffCd', 'inacCd','affiliate', 'specialTaxCd' , 'collectionCd','vat', 'taxCd1')");
        // FormManager.readOnly('identClient');
        if (custSubType != undefined && custSubType != '' && (custSubType != '3PAIT' && custSubType != '3PASM' && custSubType != '3PAVA')) {
          // FormManager.readOnly('isuCd');
          // FormManager.readOnly('inacCd');
          // FormManager.readOnly('affiliate');
          // FormManager.readOnly('clientTier');
          // FormManager.resetValidations('clientTier');
        }

        if (custSubType == '3PAIT' || custSubType == '3PASM' || custSubType == '3PAVA' || custSubType == 'CRO3P') {
          if (custSubType != 'CRO3P') {
            FormManager.enable('inacCd');
          }
          FormManager.enable('isuCd');
          FormManager.enable('clientTier');
          FormManager.enable('specialTaxCd');
        }

        // if imported and scenario is 3rd person
        if (!(custSubType == 'INTER' || custSubType == 'INTSM' || custSubType == 'INTVA' || custSubType == 'CROIN' || custSubType == 'BUSPR' || custSubType == 'BUSSM' || custSubType == 'BUSVA'
            || custSubType == 'CROBP' || custSubType == 'XIBM')) {
          FormManager.setValue('isuCd', _oldISUIT);
          FormManager.setValue('inacCd', _oldINACIT);
          FormManager.setValue('clientTier', _oldCTCIT);
          FormManager.setValue('salesBusOffCd', _oldSBOIT);
          FormManager.setValue('affiliate', _oldAffiliateIT);
          FormManager.setValue('collectionCd', _oldCollectionIT);
          FormManager.setValue('repTeamMemberNo', _oldSalesRepIT);
        } else if (custSubType == 'BUSPR' || custSubType == 'BUSSM' || custSubType == 'BUSVA' || custSubType == 'CROBP') {
          FormManager.setValue('repTeamMemberNo', '09ZPB0');
          FormManager.setValue('salesBusOffCd', 'ZP');
          FormManager.clearValue('collectionCd');
        } else if (custSubType == 'INTER' || custSubType == 'INTSM' || custSubType == 'INTVA' || custSubType == 'CROIN') {
          FormManager.setValue('isuCd', '21');
          FormManager.setValue('clientTier', '');
          FormManager.setValue('repTeamMemberNo', '012345');
          FormManager.setValue('salesBusOffCd', '99');
          FormManager.clearValue('collectionCd');
        } else {
          FormManager.clearValue('affiliate');
        }

        if ((custSubType == 'COMME' || custSubType == 'GOVST' || custSubType == 'LOCEN' || custSubType == 'NGOIT' || custSubType == 'PRICU' || custSubType == '3PAIT' || custSubType == 'UNIVE')
            && addrType != null && addrType == 'ZS01') {
          FormManager.setValue('isuCd', '27');
          FormManager.setValue('clientTier', 'E');
        }
        if (custSubType == 'IBMIT' || custSubType == 'XIBM') {
          FormManager.setValue('isuCd', '21');
          FormManager.setValue('clientTier', '');
          FormManager.setValue('repTeamMemberNo', '0B1TA0');
          FormManager.setValue('salesBusOffCd', '1T');
          FormManager.clearValue('collectionCd');
        }

        if (custSubType == 'IBMIT' || custSubType == 'XIBM') {
          FormManager.setValue('identClient', _oldIdentClientIT);
          FormManager.setValue('vat', _oldVatIT);
        }
        FormManager.setValue('specialTaxCd', _oldSpecialTaxCdIT);
      }
    }
  }
}

// Collection Code Behaviour in create by scratch and model !!
function collectionCDBehaviour() {
  console.log("---->> collectionCDBehaviour. <<----");
  var checkImportIndc = getImportedIndcForItaly();
  var reqType = FormManager.getActualValue('reqType');
  var custSubType = FormManager.getActualValue('custSubGrp');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (reqType == 'C') {
    if (checkImportIndc == 'Y') {
      if (custSubType == 'CROPR' || custSubType == 'PRISM' || custSubType == 'PRIVA') {
        FormManager.clearValue('collectionCd');
        FormManager.enable('collectionCd');
      }
      if (custSubType == 'PRICU') {
        FormManager.enable('collectionCd');
      }
      getOldValuesIT();
    }

    if (checkImportIndc != 'Y') {
      if (custSubType == 'INTER' || custSubType == 'CRINT' || custSubType == 'INTSM' || custSubType == 'INTVA' || custSubType == 'IBMIT' || custSubType == 'XIBM') {
        // FormManager.clearValue('collectionCd');
        FormManager.readOnly('collectionCd');
      }
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
// Defect 1494371: Postal Code for San Marino optional : Mukesh
function addSMAndVAPostalCDValidator() {
  var landCntry = FormManager.getActualValue('landCntry');
  if (landCntry != 'undefined' && 'SM' == landCntry) {
    FormManager.addValidator('postCd', Validators.REQUIRED, [ 'Postal Code for San Marino - SM' ], 'MAIN_NAME_TAB');
  }
  if (landCntry != 'undefined' && 'VA' == landCntry) {
    FormManager.addValidator('postCd', Validators.REQUIRED, [ 'Postal Code for Holy See (Vatican City State) - VA' ], 'MAIN_NAME_TAB');
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
        if (currentFiscalCode == '' && currentVAT == '') {
          return new ValidationResult(null, true);
        } else if (result != null && oldVAT != null && oldFiscalCode != null && (oldFiscalCode != currentFiscalCode || oldVAT != currentVAT)) {
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
    cmr.showAlert("No company addresses with this fiscal data found.", null, null, true);
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
      cmr.showAlert("No company addresses with this fiscal data found.", null, null, true);
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

function validateFiscalLengthOnIdentIT() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var requestType = FormManager.getActualValue('reqType');
        var role = FormManager.getActualValue('userRole').toUpperCase();
        var reqId = FormManager.getActualValue('reqId');
        var ident = FormManager.getActualValue('identClient');
        var fiscal = FormManager.getActualValue('taxCd1');
        var isFiscalIdentChanged = false;

        var result = cmr.query('GETDATARDCVALUESIT', {
          REQ_ID : reqId
        });

        if (result != null && result != undefined && (result['ret4'] != fiscal || result['ret3'] != ident)) {
          isFiscalIdentChanged = true;
        }

        if (requestType == 'U' && !isFiscalIdentChanged) {
          return new ValidationResult(null, true);
        }

        var lbl1 = FormManager.getLabel('LocalTax1');
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        var custGrp = FormManager.getActualValue('custGrp');

        if (requestType == 'U' || custGrp == 'LOCAL') {

          if (ident == 'A' && (fiscal == undefined || fiscal == '' || fiscal.length != 11 || !fiscal.match("^[0-9]*$"))) {
            return new ValidationResult({
              id : 'taxCd1',
              type : 'text',
              name : 'taxCd1'
            }, false, 'For Ident Client A ' + lbl1 + ' must be of 11 digits.');
          }
          if (ident == 'B' && (fiscal == undefined || fiscal == '' || fiscal.length != 11 || !fiscal.match("^[0-9]*$"))) {
            return new ValidationResult({
              id : 'taxCd1',
              type : 'text',
              name : 'taxCd1'
            }, false, 'For Ident Client B ' + lbl1 + ' must be of 11 digits.');
          }
          if (ident == 'C' && (fiscal == undefined || fiscal == '' || fiscal.length != 11 || !fiscal.match("^[0-9]*$"))) {
            return new ValidationResult({
              id : 'taxCd1',
              type : 'text',
              name : 'taxCd1'
            }, false, 'For Ident Client C ' + lbl1 + ' must be of 11 digits.');
          }
          if (ident == 'D' && (fiscal == undefined || fiscal == '' || fiscal.length != 16 || !fiscal.match("^[0-9a-zA-Z]*$"))) {
            return new ValidationResult({
              id : 'taxCd1',
              type : 'text',
              name : 'taxCd1'
            }, false, 'For Ident Client D ' + lbl1 + ' must be of 16 alphanumerics.');
          }
          if (ident == 'X' && (fiscal == undefined || fiscal == '' || fiscal.length != 16)) {
            return new ValidationResult({
              id : 'taxCd1',
              type : 'text',
              name : 'taxCd1'
            }, false, 'For Ident Client X ' + lbl1 + ' must be of 16 chars');
          }
          if (ident == 'X' && (fiscal == undefined || fiscal == '' || !fiscal.match("^(?=.*[a-zA-Z])(?=.*[0-9])[0-9a-zA-Z]*$"))) {
            return new ValidationResult({
              id : 'taxCd1',
              type : 'text',
              name : 'taxCd1'
            }, false, 'For Ident Client X ' + lbl1 + ' must contain alphanumeric characters.');
          }
          if (fiscal != undefined && (ident == 'N' || ident == 'Y') && fiscal != '') {
            FormManager.removeValidator('taxCd1', Validators.REQUIRED);
            return new ValidationResult({
              id : 'taxCd1',
              type : 'text',
              name : 'taxCd1'
            }, false, 'For Ident Client ' + ident + ' ' + lbl1 + ' must be blank');
          }

        }
        // Defect 1593720
        var cntryRegion = FormManager.getActualValue('countryUse');
        var tempCntryRegion = '';
        if (cntryRegion != '' && cntryRegion.length > 3) {
          tempCntryRegion = cntryRegion.substring(3, 5);
        }
        console.log("cntryRegion is>>" + cntryRegion);
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function validateVATOnIdentIT() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var requestType = FormManager.getActualValue('reqType');
        var role = FormManager.getActualValue('userRole').toUpperCase();
        if (requestType != 'C') {
          return new ValidationResult(null, true);
        }
        var ident = FormManager.getActualValue('identClient');
        var lbl1 = FormManager.getLabel('vat');
        var vat = FormManager.getActualValue('vat');

        if ((ident == 'A' || ident == 'C' || ident == 'D') && vat != undefined && vat != '' && !vat.match("^IT[0-9]{11}$")) {
          return new ValidationResult({
            id : 'vat',
            type : 'text',
            name : 'vat'
          }, false, 'For Ident Client ' + ident + ', ' + lbl1 + ' must be IT99999999999.');
        } else if ((ident == 'B' || ident == 'X' || ident == 'Y') && vat != '') {
          return new ValidationResult({
            id : 'taxCd1',
            type : 'text',
            name : 'taxCd1'
          }, false, 'For Ident Client ' + ident + ', ' + lbl1 + ' must be Blank.');
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

  var custGrp = FormManager.getActualValue('custGrp');
  var ident = FormManager.getActualValue('identClient');
  var landCntry = FormManager.getActualValue('landCntry');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var checkImportIndc = getImportedIndcForItaly();
  var _zs01ReqId = FormManager.getActualValue('reqId');
  var cntryCdParams = {
    REQ_ID : _zs01ReqId,
    ADDR_TYPE : 'ZI01',
  };
  var cntryCdResult = cmr.query('ADDR.GET.LANDEDCNTRY.BY_REQID_ADDRTYPE', cntryCdParams);
  if (cntryCdResult.ret1 != undefined) {
    landCntry = cntryCdResult.ret1;
  }
  var isCrossBorder = false;
  if (landCntry != 'IT') {
    isCrossBorder = true;
  }
  if (ident != '') {
    if (ident == 'B') {
      FormManager.resetValidations('vat');
    }
    if (role == 'REQUESTER') {
      if (ident == 'A' || ident == 'D') {
        FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
      } else if (ident == 'C') {
        FormManager.resetValidations('vat');
      }
    }
    if (role == 'PROCESSOR') {
      if (ident == 'A' || ident == 'D') {
        FormManager.resetValidations('vat');
      } else if (ident == 'C') {
        FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
      }
    }
    if (isCrossBorder) {
      if (ident == 'N') {
        FormManager.resetValidations('taxCd1');
        FormManager.readOnly('taxCd1');
        FormManager.enable('vat');
      } else if (ident == 'Y') {
        FormManager.resetValidations('taxCd1');
        FormManager.setValue('taxCd1', '');
        FormManager.readOnly('taxCd1');
      }
    } else if (ident == 'X' || ident == 'Y') {
      FormManager.readOnly('vat');
      FormManager.setValue('vat', '');
      FormManager.resetValidations('vat');
    } else if (ident == 'N') {
      FormManager.enable('vat');
      FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
    } else if ((ident == 'A' || ident == 'D' || ident == 'C') && checkImportIndc == 'Y') {
      FormManager.readOnly('vat');
    }
    if (ident == 'N') {
      FormManager.addValidator('vat', Validators.REQUIRED, [ 'vat' ], 'MAIN_CUST_TAB');
      FormManager.enable('vat');
      FormManager.removeValidator('taxCd1', Validators.REQUIRED);
      FormManager.setValue('taxCd1', '');
      FormManager.readOnly('taxCd1');
    } else if (ident == 'Y') {
      FormManager.removeValidator('vat', Validators.REQUIRED);
      FormManager.setValue('vat', '');
      FormManager.readOnly('vat');
      FormManager.removeValidator('taxCd1', Validators.REQUIRED);
      FormManager.setValue('taxCd1', '');
      FormManager.readOnly('taxCd1');
    }

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
        FormManager.resetValidations('taxCd1');
        FormManager.readOnly('taxCd1');
        FormManager.enable('vat');
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

/*
 * function validateSRForIT() { FormManager.addFormValidator((function() {
 * return { validate : function() { var isu =
 * FormManager.getActualValue('isuCd'); var salesRep =
 * FormManager.getActualValue('repTeamMemberNo'); var lbl1 =
 * FormManager.getLabel('SalRepNameNo'); if (isu != undefined && isu != 32) { if
 * (salesRep == '09NIMM' || salesRep == '09GEMM' || salesRep == '09NCMM' ||
 * salesRep == '09NAMM' || salesRep == '09NBMM' || salesRep == '09NLMM' ||
 * salesRep == '09NPMM' || salesRep == '09NFMM' || salesRep == '09RPMM' ||
 * salesRep == '09KAMM' || salesRep == '09CPMM' || salesRep == '09NGMM' ||
 * salesRep == '09NMMM' || salesRep == '09GKMM' || salesRep == '09GJMM' ||
 * salesRep == '09GHMM' ) { return new ValidationResult({ id :
 * 'repTeamMemberNo', type : 'text', name : 'repTeamMemberNo' }, false, 'The ' +
 * lbl1 + ' cannot be default when ISU differs from 32.'); } } return new
 * ValidationResult(null, true); } }; })(), 'MAIN_IBM_TAB', 'frmCMR'); }
 */

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
      if (role == 'REQUESTER' && results.length >= 1) {
        FormManager.addValidator('stateProv', Validators.REQUIRED, [ 'State/Province' ], null);
      }
      /*
       * if (results.length == 1 && role == 'REQUESTER' && (addrType != 'ZS01' ||
       * !FormManager.getField('addrType_ZS01').checked)) {
       * FormManager.resetValidations('stateProv'); }
       */
    }
  }
}

// CREATCMR-2658 ITALY
function autoSetSROnSBOValueIT() {
  var reqType = FormManager.getActualValue('reqType');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var cntryRegion = FormManager.getActualValue('countryUse');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  if (reqType != 'C' || cntry != '758') {
    return;
  }
  var sbo = FormManager.getActualValue('salesBusOffCd');
  var salesRep = FormManager.getActualValue('repTeamMemberNo');
  var checkImportIndc = getImportedIndcForItaly();
  if (custSubGrp == 'INTER' || custSubGrp == 'CROIN' || custSubGrp == 'INTSM' || custSubGrp == 'INTVA') {
    checkScenarioChanged();
    if (sbo == '' || _isScenarioChanged) {
      FormManager.clearValue('CollectionCd');
    }
    cmr.showNode('SRInfo');
    cmr.showNode('sboInfo');
    if (sbo != undefined && sbo != '' && sbo.length == 2) {
      if (sbo == '99') {
        FormManager.setValue('repTeamMemberNo', '012345');
      }
      if (sbo == '1B' || sbo == '1b') {
        FormManager.setValue('repTeamMemberNo', '0B1BA0');
      }
      if (sbo == '1G' || sbo == '1g') {
        FormManager.setValue('repTeamMemberNo', '0B1GA0');
      }
      if (sbo == '1E' || sbo == '1e') {
        FormManager.setValue('repTeamMemberNo', '0B1EA0');
      }
      if (sbo == '11') {
        FormManager.setValue('repTeamMemberNo', '0B11A0');
      }
      if (sbo == '12') {
        FormManager.setValue('repTeamMemberNo', '0B12A0');
      }
      if (sbo == '13') {
        FormManager.setValue('repTeamMemberNo', '0B13A0');
      }
      if (sbo == '14') {
        FormManager.setValue('repTeamMemberNo', '0B14A0');
      }
      FormManager.enable('salesBusOffCd');
      FormManager.readOnly('repTeamMemberNo');
      console.log('setting SalesRep=' + repTeamMemberNo);
    }
    if (sbo == '') {
      FormManager.setValue('salesBusOffCd', '99');
      FormManager.setValue('repTeamMemberNo', '012345');
    }
  } else {
    cmr.hideNode('SRInfo');
    cmr.hideNode('sboInfo');
  }
  // if (salesRep == '') {
  // FormManager.clearValue('salesBusOffCd');
  // }
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
          if ((results != null || results != undefined) && results.ret1 != undefined) {
            if (results.ret1 == '') {
              return new ValidationResult(null, false, 'State Prov cannot be blank for the Billing address');
            }

            var results = cmr.query('GETZI01STATEPROV', qParams);
            if ((results != null || results != undefined) && results.ret1 != undefined) {
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

function addSBOSRLogicIE(clientTier) {
  var reqType = FormManager.getActualValue('reqType');
  if (reqType != 'C') {
    return;
  }
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var _isuCd = FormManager.getActualValue('isuCd');
  var _clientTier = FormManager.getActualValue('clientTier');

  var salesRepValue = [];
  var sboValues = [];
  if (_isuCd != '') {
    var isuCtc = _isuCd + _clientTier;
    var qParams = null;
    var results = null;

    qParams = {
      _qall : 'Y',
      ISSUING_CNTRY : cntry,
      ISU_CD : '%' + _isuCd + _clientTier + '%',
    };
    var results = cmr.query('GET.SALESREP.IRELAND', qParams);
    if (results != null) {
      for (var i = 0; i < results.length; i++) {
        salesRepValue.push(results[i].ret1);
        sboValues.push(results[i].ret2);
      }
      FormManager.limitDropdownValues(FormManager.getField('repTeamMemberNo'), salesRepValue);
      FormManager.limitDropdownValues(FormManager.getField('salesBusOffCd'), sboValues);
      if (salesRepValue != null) {
        if (salesRepValue.length == 1) {
          FormManager.setValue('repTeamMemberNo', salesRepValue[0]);
        }
      }
      if (sboValues != null) {
        if (sboValues.length >= 1) {
          FormManager.setValue('salesBusOffCd', sboValues[0]);
        }
      }
    }
  }
}

// function autoSetISUClientTierUK() {
// console.log("autoSetISUClientTierUK........")
// var custSubGroup = FormManager.getActualValue('custSubGrp');
// var reqType = FormManager.getActualValue('reqType');
// var noScenario = new Set([ 'INTER', 'XINTR', 'BUSPR', 'XBSPR' ]);
// var isuCd = FormManager.getActualValue('isuCd');
// var clientTier = FormManager.getActualValue('clientTier');
// var isuCdList = [ '34', '36', '32' ];
//
// if (reqType != 'C') {
// return;
// }
// if (FormManager.getActualValue('viewOnlyPage') == 'true') {
// return;
// }
//
// if ('866' == FormManager.getActualValue('cmrIssuingCntry') &&
// isuCdList.includes(isuCd) && clientTier == '') {
// return;
// }
//
// if ('866' == FormManager.getActualValue('cmrIssuingCntry')) {
// if (custSubGroup != undefined && custSubGroup != '' &&
// !noScenario.has(custSubGroup)) {
//
// var _reqId = FormManager.getActualValue('reqId');
// var postCdParams = {
// REQ_ID : _reqId,
// ADDR_TYPE : "ZS01",
// };
//
// var postCdResult = cmr.query('ADDR.GET.POSTCD.BY_REQID_ADDRTYP',
// postCdParams);
// var postCd = postCdResult.ret1;
// if (postCd != null && postCd.length > 2) {
// postCd = postCd.substring(0, 2);
// }
//
// if (postCd != '' && (isNorthernIrelandPostCd(postCd))) {
// FormManager.setValue('isuCd', "34");
// FormManager.setValue('clientTier', "Q");
// } else {
// FormManager.setValue('clientTier', "Q");
// }
// } else {
// FormManager.setValue('isuCd', '21');
//
// // CREATCMR-4293
// var custSubGrp = FormManager.getActualValue('custSubGrp');
// // Business Partner OR Internal
// if (custSubGrp == 'BUSPR' || custSubGrp == 'INTER') {
// FormManager.setValue('clientTier', '');
// }
// }
// }
// }

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

function optionalRulePostalCodeUK() {
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
      // FormManager.readOnly('bpRelType');
      // FormManager.readOnly('memLvl');
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
        var vat = FormManager.getActualValue('vat');
        var reqId = FormManager.getActualValue('reqId');
        var checkImportIndc = getImportedIndcForItaly();
        if (FormManager.getActualValue('reqType') == 'C' && checkImportIndc == 'N') {
          if ((taxCd1 != null && taxCd1.length != 0) || (vat != null && vat.length != 0)) {
            qParams = {
              FISCAL_CODE : FormManager.getActualValue('taxCd1'),
              MANDT : cmr.MANDT
            }
            var result = cmr.query('IT.CHECK.DUPLICATE_FISCAL_ADDRESS', qParams);

            qParams = {
              VAT : FormManager.getActualValue('vat'),
              MANDT : cmr.MANDT
            }
            var result1 = cmr.query('IT.CHECK.DUPLICATE_VAT', qParams);

            if (result.ret1 != null && result.ret1 != '' && vat == '' && vat.length == 0) {
              return new ValidationResult({
                id : 'taxCd1',
                type : 'text',
                name : 'taxCd1'
              }, false, 'Fiscal Code on the request is already available on CMR No. ' + result.ret1 + '. Please mention another new fiscal code or import the CMR into the request.');
            }

            if (result1.ret1 != null && result1.ret1 != '' && taxCd1 == '' && taxCd1.length == 0) {
              return new ValidationResult({
                id : 'vat',
                type : 'text',
                name : 'vat'
              }, false, 'Vat on the request is already available on CMR No. ' + result.ret1 + '. Please mention another new VAT or import the CMR into the request.');
            }

            if (result.ret1 != null && result.ret1 != '' && result1.ret1 != null && result1.ret1 != '' && result.ret1 == result1.ret1) {
              return new ValidationResult({
                id : 'taxCd1',
                type : 'text',
                name : 'taxCd1'
              }, false, 'Fiscal Code and VAT on the request is already available on CMR No. ' + result.ret1 + '. Please mention another new fiscal code and VAT or import the CMR into the request.');
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
            var landCntry = 'IT'; // default to
            // Italy
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
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var isu = FormManager.getActualValue('isuCd');
        var ctc = FormManager.getActualValue('clientTier');
        var isuCTC = isu.concat(ctc);
        var sbo = FormManager.getActualValue('salesBusOffCd').toUpperCase();
        var salRep = FormManager.getActualValue('repTeamMemberNo');
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        var reqType = FormManager.getActualValue('reqType');
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        if (custSubGrp == null || custSubGrp == '' || custSubGrp == undefined || reqType != 'C') {
          return new ValidationResult(null, true);

        }

        var qParams = {
          _qall : 'Y',
          CNTRY : cntry,
          SBO : sbo,
          SALES_REP : '%' + salRep + '%',
          ISU : '%' + isuCTC + '%'
        };
        var results = cmr.query('GET.SR.SBO.BYISUCTC', qParams);
        var displayInvalidMsg = true;

        if (results != null && results.length > 0) {
          displayInvalidMsg = false;
        }

        if (displayInvalidMsg) {
          return new ValidationResult({
            id : 'salesBusOffCd',
            type : 'text',
            name : 'salesBusOffCd'
          }, false, 'Please enter the valid combination of ISU , ClientTier ,SBO and Sales Rep.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');

  // validate Sbo length for ITALY
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var sbo = FormManager.getActualValue('salesBusOffCd').toUpperCase();
        var lbl1 = FormManager.getLabel('SalesBusOff');
        if (sbo.length != 2 && sbo != undefined && sbo != '') {
          return new ValidationResult({
            id : 'salesBusOffCd',
            type : 'text',
            name : 'salesBusOffCd'
          }, false, 'The ' + lbl1 + ' should be two characters long');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');

  // validate Sbo Characters and numbers only
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var sbo = FormManager.getActualValue('salesBusOffCd').toUpperCase();
        var lbl1 = FormManager.getLabel('SalesBusOff');
        if (sbo != undefined && sbo != '' && !sbo.match("^[0-9a-zA-Z]*$")) {
          return new ValidationResult({
            id : 'salesBusOffCd',
            type : 'text',
            name : 'salesBusOffCd'
          }, false, 'The ' + lbl1 + ' should consist of digits and alphabets only');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');

}

// CREATCMR - 985 validate Sales Rep
function validateSalesRepForIT() {
  // validate SalesRep for ITALY
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var salesRep = FormManager.getActualValue('repTeamMemberNo');
        if (salesRep.length != 6 && salesRep != undefined && salesRep != '') {
          return new ValidationResult({
            id : 'repTeamMemberNo',
            type : 'text',
            name : 'repTeamMemberNo'
          }, false, 'The Sales Rep should be six characters long');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function validateCollectionCdIT() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var collCd = FormManager.getActualValue('collectionCd');
        var alphanumeric = /^[0-9A-Z]*$/;

        if (collCd == '') {
          return new ValidationResult(null, true);
        } else {
          if (collCd.length < 5) {
            return new ValidationResult(null, false, 'Collection Code should be exactly 5 characters.');
          }
          if (!collCd.match(alphanumeric)) {
            return new ValidationResult({
              id : 'collectionCd',
              type : 'text',
              name : 'collectionCd'
            }, false, 'Collection Code should contain only upper-case latin and numeric characters.');
          }
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function fetchSboSrForIsuCtcIE(cntry, sbo, salRep, isuCTC) {
  console.log(">>>> fetchSboSrForIsuCtcIE");
  var results = null;
  var qParams = {
    _qall : 'Y',
    CNTRY : cntry,
    SBO : sbo,
    SALES_REP : salRep,
    ISU : '%' + isuCTC + '%'
  };
  results = cmr.query('IE.GET.SBOSR_FOR_ISU_CTC', qParams);
  return results;
}

function fetchSboSrForIsuCtcUK(cntry, sbo, salRep, isu, ctc) {
  console.log(">>>> fetchSboSrForIsuCtcUK");
  var results = null;
  var qParams = {
    _qall : 'Y',
    CNTRY : cntry,
    SBO : sbo,
    SALES_REP : salRep,
    ISU : isu,
    CTC : ctc
  };
  results = cmr.query('UK.GET.SBOSR_FOR_ISU_CTC', qParams);
  return results;
}

function fetchSboSrForIsuCtcIE(cntry, sbo, salRep, isuCTC) {
  console.log(">>>> fetchSboSrForIsuCtcIE");
  var results = null;
  var qParams = {
    _qall : 'Y',
    CNTRY : cntry,
    SBO : sbo,
    SALES_REP : salRep,
    ISU : '%' + isuCTC + '%'
  };
  results = cmr.query('IE.GET.SBOSR_FOR_ISU_CTC', qParams);
  return results;
}

function fetchSboSrForIsuCtcUK(cntry, sbo, salRep, isu, ctc) {
  console.log(">>>> fetchSboSrForIsuCtcUK");
  var results = null;
  var qParams = {
    _qall : 'Y',
    CNTRY : cntry,
    SBO : sbo,
    SALES_REP : salRep,
    ISU : isu,
    CTC : ctc
  };
  results = cmr.query('UK.GET.SBOSR_FOR_ISU_CTC', qParams);
  return results;
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

function setCtcFieldMandtUKI() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var isuCd = FormManager.getActualValue('isuCd');
  var reqType = FormManager.getActualValue('reqType');
  if (role != 'PROCESSOR' || reqType != 'C' || FormManager.getActualValue('custSubGrp') == 'IBMEM') {
    return;
  }
  var isuList = [ '34', '36', '27' ];
  if (!isuList.includes(isuCd)) {
    FormManager.removeValidator('clientTier', Validators.REQUIRED);
    return;
  }
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
 * Set Collection Code on SBO
 */
function setCollCdOnSBOIT(salesBusOffCd) {
  var checkImportIndc = getImportedIndcForItaly();
  var custSubType = FormManager.getActualValue('custSubGrp');
  if (FormManager.getActualValue('reqType') != 'C' || FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  // if (checkImportIndc == 'Y') {
  // return;
  // }

  sbo = FormManager.getActualValue('salesBusOffCd');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');

  if (salesBusOffCd == sbo && custSubType != undefined && custSubType != '' && custSubType == 'PRICU') {
    return;
  }

  var collectionCds = [];
  if (sbo != '') {
    var qParams = {
      _qall : 'Y',
      ISSUING_CNTRY : cntry,
      SALES_BO_CD : '%' + salesBusOffCd + '%'
    };
    var results = cmr.query('GET.CCVALUE.BYSBO', qParams);
    if (results != null) {
      for (var i = 0; i < results.length; i++) {
        collectionCds.push(results[i].ret1);
      }
      if (collectionCds != null) {
        if (collectionCds.length == 1) {
          FormManager.setValue('collectionCd', collectionCds[0]);
        }
        if (collectionCds.length == 0) {
          FormManager.setValue('collectionCd', '');
        }
      }
    }
  }
}

function validateExistingCMRNo() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        console.log('checking if prospect cmr...');
        var reqId = FormManager.getActualValue('reqId');
        var cmrNo = FormManager.getActualValue('cmrNo');
        if (cmrNo.match('^P[0-9]{5}')) {
          return;
        }
        console.log('checking requested cmr number...');
        var reqType = FormManager.getActualValue('reqType');
        var cmrNo = FormManager.getActualValue('cmrNo');
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        var reqId = FormManager.getActualValue('reqId');
        var action = FormManager.getActualValue('yourAction');
        if (reqType == 'C' && cmrNo) {
          var exists = cmr.query('LD.CHECK_EXISTING_CMR_NO', {
            COUNTRY : cntry,
            CMR_NO : cmrNo,
            MANDT : cmr.MANDT
          });
          var processed = cmr.query('LD.CHECK_RDC_PROCESSING_STATUS', {
            REQ_ID : reqId
          });

          if (exists && exists.ret1 && !processed.ret1) {
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
          var exists1 = cmr.query('LD.CHECK_CMR_EXIST_IN_RDC', {
            COUNTRY : cntry,
            CMR_NO : cmrNo,
            MANDT : cmr.MANDT
          });
          if (exists1 && exists1.ret1 && action != 'PCM') {
            return new ValidationResult({
              id : 'cmrNo',
              type : 'text',
              name : 'cmrNo'
            }, false, 'The requested CMR: ' + cmrNo + ' already exists in the system for country : ' + cntry);
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

function addAfterTemplateLoadIT(fromAddress, scenario, scenarioChanged) {
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
    if ((_custSubGrp == 'INTER' || _custSubGrp == 'CROIN' || _custSubGrp == 'INTSM' || _custSubGrp == 'INTVA') && scenarioChanged) {
      FormManager.addValidator('repTeamMemberNo', Validators.REQUIRED, [ 'SalRepNameNo' ], 'MAIN_IBM_TAB');
    } else {
      FormManager.removeValidator('repTeamMemberNo', Validators.REQUIRED);
    }
  }

  if (checkImportIndc != 'Y') {
    var ident = FormManager.getActualValue('identClient');
    // FormManager.enable('identClient');
    if (FormManager.getActualValue('reqType') == 'C' && 'CROSS' == FormManager.getActualValue('custGrp')) {
      var checkImportIndc = getImportedIndcForItaly();
      if (ident == 'N') {
        FormManager.addValidator('vat', Validators.REQUIRED, [ 'vat' ], 'MAIN_CUST_TAB');
        FormManager.enable('vat');
        FormManager.removeValidator('taxCd1', Validators.REQUIRED);
        FormManager.setValue('taxCd1', '');
        FormManager.readOnly('taxCd1');
      } else if (ident == 'Y') {
        FormManager.removeValidator('vat', Validators.REQUIRED);
        FormManager.setValue('vat', '');
        FormManager.readOnly('vat');
        FormManager.removeValidator('taxCd1', Validators.REQUIRED);
        FormManager.setValue('taxCd1', '');
        FormManager.readOnly('taxCd1');
      }
    }
  }
  autoSetSROnSBOValueIT();

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
          if (custSubGrp == 'PRISM' || custSubGrp == 'PRICU' || custSubGrp == 'PRIVA' || custSubGrp == 'CROPR' || custSubGrp == 'IBMIT' || custSubGrp == 'XIBM') {
            console.log('no validation required');
          } else {
            return new ValidationResult(null, false, 'ISIC code value should be other than 9500 for Non-IBM/Private customer.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function addAfterConfigItaly() {
  afterConfigForIT();
  fieldsReadOnlyItaly();
  setExpediteReasonOnChange();
  disableAutoProcessingOnFiscalUpdate();
  toggleBPRelMemType();
  typeOfCustomer();
  enableDisableTaxCodeCollectionCdIT();
  disableCompanyLevelFieldsIT();
  ibmFieldsBehaviourInCreateByModelIT();
  ibmFieldsBehaviourInCreateByScratchIT();
  disableProcpectCmrIT();
  getOldValuesIT();
  // CREATCMR-788
  addressQuotationValidatorIT();
}

function addAfterTemplateLoadItaly(fromAddress, scenario, scenarioChanged) {
  addFieldValidationForProcessorItaly();
  getOldValuesIT(fromAddress, scenario, scenarioChanged);
  autoPopulateIdentClientIT();
  setVATForItaly();
  fieldsReadOnlyItaly(fromAddress, scenario, scenarioChanged);
  checkScenarioChanged(fromAddress, scenario, scenarioChanged);
  setMRCOnScenariosIT();
  toggleBPRelMemType();
  typeOfCustomer();
  setSpecialTaxCodeOnScenarioIT();
  enableDisableTaxCodeCollectionCdIT();
  addAfterTemplateLoadIT(fromAddress, scenario, scenarioChanged);
  collectionCDBehaviour();
  setAffiliateEnterpriseRequired();
  ibmFieldsBehaviourInCreateByModelIT();
  ibmFieldsBehaviourInCreateByScratchIT();
  disableProcpectCmrIT();
  autoSetSROnSBOValueIT();
  // setDeafultISUCtcChange();
  setVATOnIdentClientChangeIT();
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
  landedCntryLockedIT(cntry, addressMode, saving, finalSave);
  // setDeafultSBOLogicComm(cntry, addressMode, saving, finalSave);
  // autoSetValuesOnPostalCodeIT(addressMode);
}

function ibmFieldsBehaviourInCreateByScratchIT() {
  var requestType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var scenario = FormManager.getActualValue('custGrp');
  var isuCd = FormManager.getActualValue('isuCd');
  var clientTier = FormManager.getActualValue('clientTier');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var countryUse = FormManager.getActualValue('countryUse');
  if (requestType != 'C') {
    return;
  }
  if (scenario.includes('CRO')) {
    var scenarioType = 'CROSS';

    if (requestType == 'C' && scenarioType != undefined && scenarioType == 'CROSS') {
      FormManager.addValidator('identClient', Validators.REQUIRED, [ 'Ident Client' ], 'MAIN_CUST_TAB');
      FormManager.enable('vat');
    } else {
      FormManager.resetValidations('identClient');
    }
  }
  var checkImportIndc = getImportedIndcForItaly();
  if (checkImportIndc != 'Y') {
    // commented as implemented for CREATCMR-7861
    // if (role == 'REQUESTER') {
    // FormManager.addValidator('salesBusOffCd', Validators.REQUIRED, [ 'SBO' ],
    // 'MAIN_IBM_TAB');
    // if (countryUse == '758SM' || countryUse == '758VA') {
    // if ((isuCd == '34' && clientTier == 'Q') || custSubGrp == 'BUSSM' ||
    // custSubGrp == 'BUSVA') {
    // FormManager.readOnly('salesBusOffCd');
    // FormManager.readOnly('repTeamMemberNo');
    // } else {
    // FormManager.enable('salesBusOffCd');
    // FormManager.enable('repTeamMemberNo');
    // }
    // }
    // }
    // if (custSubGrp == 'BUSPR' || custSubGrp == 'BUSSM' || custSubGrp ==
    // 'BUSVA' || custSubGrp == 'CROBP' || custSubGrp == 'PRICU' || custSubGrp
    // == 'CROPR' || custSubGrp == 'PRISM'
    // || custSubGrp == 'PRIVA') {
    // FormManager.readOnly('salesBusOffCd');
    // FormManager.readOnly('repTeamMemberNo');
    // FormManager.removeValidator('salesBusOffCd', Validators.REQUIRED);
    // }

    if ((isuCd == '34' && clientTier == 'Q')
        || (isuCd == '27' && clientTier == 'E')
        || (custSubGrp == 'BUSPR' || custSubGrp == 'BUSSM' || custSubGrp == 'BUSVA' || custSubGrp == 'CROBP' || custSubGrp == 'INTER' || custSubGrp == 'INTSM' || custSubGrp == 'INTVA'
            || custSubGrp == 'CROIN' || custSubGrp == 'IBMIT' || custSubGrp == 'XIBM')) {
      FormManager.removeValidator('affiliate', Validators.REQUIRED);
      FormManager.removeValidator('enterprise', Validators.REQUIRED);
    } else {
      FormManager.addValidator('affiliate', Validators.REQUIRED, [ 'Affiliate' ], 'MAIN_IBM_TAB');
      FormManager.addValidator('enterprise', Validators.REQUIRED, [ 'Enterprise' ], 'MAIN_IBM_TAB');
    }

    if ((checkImportIndc == 'N') && (custSubGrp == 'IBMIT' || custSubGrp == 'XIBM')) {
      FormManager.readOnly('vat');
      FormManager.readOnly('collectionCd');
      FormManager.setValue('specialTaxCd', 'A');
    }

    FormManager.removeValidator('collectionCd', Validators.REQUIRED);
  }
}

function disableProcpectCmrIT() {
  var cmrNo = FormManager.getActualValue('cmrNo');
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  var ifProspect = FormManager.getActualValue('prospLegalInd');
  if (dijit.byId('prospLegalInd')) {
    ifProspect = dijit.byId('prospLegalInd').get('checked') ? 'Y' : 'N';
  }
  if (ifProspect == 'N') {
    FormManager.setValue('prospLegalInd', 'N');
  }
  console.log("disable prospect CMR");
  if (cmrNo.match('^P[0-9]{5}')) {
    FormManager.readOnly('cmrNo');
  } else {
    FormManager.enable('cmrNo');
  }
}

function autoSetAbbrNameUKI() {
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var billingCustNm = '';
  var installingCustNm = '';
  var reqId = FormManager.getActualValue('reqId');
  var cmrCntry = FormManager.getActualValue('cmrIssuingCntry');
  var abbName = FormManager.getActualValue('abbrevNm');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  // CMR - 5063
  if (custSubGrp == 'THDPT') {
    var result = cmr.query('GET.CUSTNM1_ADDR_UKI', {
      REQ_ID : reqId,
      ADDR_TYPE : 'ZI01'
    });
    if (result.ret1 != undefined) {
      installingCustNm = result.ret1 + (result.ret2 != undefined ? result.ret2 : '');
      if (installingCustNm.match('^VR[0-9]{3}')) {
        FormManager.setValue('abbrevNm', installingCustNm.substring(0, 22));
      } else {
        var result2 = cmr.query('GET.CUSTNM1_ADDR_UKI', {
          REQ_ID : reqId,
          ADDR_TYPE : 'ZS01'
        });
        if (result2.ret1 != undefined) {
          billingCustNm = result2.ret1 + (result2.ret2 != undefined ? result2.ret2 : '');
        }
        billingCustNm = result2.ret1 != undefined ? result2.ret1 : '';
        if (billingCustNm != '') {
          var offset = 0;
          if (installingCustNm.length < 11) {
            offset = 11 - installingCustNm.length;
          }
          var installingCustNmTrim = installingCustNm.length > 11 ? installingCustNm.substring(0, 11) : installingCustNm;
          var billingCustNmTrim = billingCustNm.length > (offset + 8) ? billingCustNm.substring(0, offset + 8) : billingCustNm;
          FormManager.setValue('abbrevNm', (installingCustNmTrim + ' / ' + billingCustNmTrim));
        }
      }
    }
  } else if (('INFSL' == custSubGrp) && (SysLoc.IRELAND == cmrCntry || SysLoc.UK == cmrCntry)) {
    var qParams = {
      REQ_ID : FormManager.getActualValue('reqId'),
    };
    var result = cmr.query('UKI.GET_ABBNAME_DATA', qParams);
    if (result.ret1 != undefined && result.ret1 != '') {
      FormManager.setValue('abbrevNm', result.ret1);
    }
  } else if (('INTER' == custSubGrp)) {
    // Defect-6793
    autoSetAbbrevNmFrmDept();
  } else {
    var result = cmr.query('GET.CUSTNM1_ADDR_UKI', {
      REQ_ID : reqId,
      ADDR_TYPE : 'ZS01'
    });

    if ((result != null || result != undefined) && result.ret1 != undefined) {
      {
        var _abbrevNmValue = result.ret1 + (result.ret2 != undefined ? result.ret2 : '');
        if (_abbrevNmValue != null && _abbrevNmValue.length > 22) {
          _abbrevNmValue = _abbrevNmValue.substr(0, 22);
        }
        FormManager.setValue('abbrevNm', _abbrevNmValue);
        FormManager.readOnly('abbrevNm');
      }
      // CREATCMR-8135
      if (custSubGrp == 'DC' && _abbrevNmValue != null && _abbrevNmValue.length > 0) {
        _abbrevNmValue = _abbrevNmValue.substr(0, 17) + " DC";
        FormManager.setValue('abbrevNm', _abbrevNmValue);
        FormManager.readOnly('abbrevNm');
      }
    }
  }
}
function autoSetUIFieldsOnScnrioUKI() {
  var reqType = FormManager.getActualValue('reqType');
  var custGrp = FormManager.getActualValue('custGrp');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var issuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  if (custSubGrp == 'INTER') {
    FormManager.setValue('company', '');
    FormManager.readOnly('company');
    FormManager.setValue('collectionCd', '88');
    FormManager.readOnly('collectionCd');
  } else if (custSubGrp == 'INFSL') {
    FormManager.readOnly('collectionCd');
    FormManager.setValue('collectionCd', '69');
    FormManager.readOnly('custClass');
    FormManager.setValue('custClass', '33');
  }

  if (issuingCntry == '754') {
    if (custSubGrp == 'BUSPR' || custSubGrp == 'INTER') {
      FormManager.setValue('clientTier', '');
    }
  }

  // if (reqType == 'C' && !(custGrp == 'CROSS' || custSubGrp == 'PRICU') &&
  // !dijit.byId('restrictInd').get('checked')) {
  // FormManager.getField('restrictInd').checked = false;
  // FormManager.enable('restrictInd');
  // }

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

function turkish(input) {
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

function disableSitePartyIdTR() {
  var reqType = FormManager.getActualValue('reqType');
  if (reqType == 'C') {
    FormManager.setValue('sitePartyId', '');
  }
  FormManager.resetValidations('sitePartyId');
  FormManager.readOnly('sitePartyId');
}

function getLandedCntryForItaly() {
  if (_landedIT) {
    console.log('Returning landed cntry = ' + _landedIT);
    return _landedIT;
  }
  var results = cmr.query('VALIDATOR.LANDED_IT', {
    REQID : FormManager.getActualValue('reqId')
  });
  if ((results != null || results != undefined) && results.ret1) {
    _landedIT = results.ret1;
  } else {
    _landedIT = '';
  }
  console.log('saving landed cntry as ' + _landedIT);
  return _landedIT;

}

function lockCollectionCode() {
  var isuCd = FormManager.getActualValue('isuCd');
  var clientTier = FormManager.getActualValue('clientTier');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubType = FormManager.getActualValue('custSubGrp');
  var checkImportIndc = getImportedIndcForItaly();
  if (FormManager.getActualValue('reqType') != 'C' || FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (checkImportIndc == 'Y') {
    return;
  }
  if (role != 'REQUESTER') {
    return;
  }
  if (custSubType != undefined
      && custSubType != ''
      && (custSubType == 'BUSPR' || custSubType == 'BUSSM' || custSubType == 'BUSVA' || custSubType == 'INTER' || custSubType == 'INTSM' || custSubType == 'INTVA' || custSubType == 'CROBP' || custSubType == 'CROIN')) {
    return;
  }

  if (isuCd == '34' && clientTier == 'Q') {
    FormManager.enable('collectionCd');
  } else {
    // FormManager.setValue('collectionCd', '');
    FormManager.readOnly('collectionCd');
  }
}

function emeaPpsCeidValidator() {
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

function clientTierCodeValidator() {
  console.log("Inside clientTierCodeValidator......")
  var isuCode = FormManager.getActualValue('isuCd');
  var clientTierCode = FormManager.getActualValue('clientTier');
  var reqType = FormManager.getActualValue('reqType');
  var activeIsuCd = [ '27', '34', '36' ];
  var activeCtc = [ 'Q', 'Y', 'E' ];

  if (!activeIsuCd.includes(isuCode)) {
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
    } else if (clientTierCode == 'Q') {
      return new ValidationResult(null, true);
    } else {
      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier can only accept \'Q\'.');
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
      }, false, 'Client Tier can only accept \'E\'.');
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
      }, false, 'Client Tier can only accept \'Y\'.');
    }
  } else {
    if (activeCtc.includes(clientTierCode) || clientTierCode == '') {
      $("#clientTierSpan").html('');
      return new ValidationResult(null, true);
    } else {
      $("#clientTierSpan").html('');
      $("#clientTierSpan").append('<span style="color:red" class="cmr-ast" id="ast-clientTier">* </span>');
      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier can only accept \'Q\', \'Y\', \'E\' or blank.');
    }
  }
}

// CREATCMR-4293

function clientTierValidator() {
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

// CREATCMR-1727
function cmrNoEnableForUKI() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var reqType = FormManager.getActualValue('reqType');
  var cmrNo = FormManager.getActualValue('cmrNo');

  if (role != "PROCESSOR" || FormManager.getActualValue('viewOnlyPage') == 'true' || reqType == 'U') {
    FormManager.readOnly('cmrNo');
  } else {
    if (!cmrNo.startsWith('P')) {
      FormManager.enable('cmrNo');
    }
  }
}

// CREATCMR-1727
function addressQuotationValidatorIT() {
  // CREATCMR-788
  FormManager.addValidator('abbrevNm', Validators.NO_QUOTATION, [ 'Abbreviated Name' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('abbrevLocn', Validators.NO_QUOTATION, [ 'Abbreviated Location' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('custNm1', Validators.NO_QUOTATION, [ 'Customer Name' ]);
  FormManager.addValidator('custNm2', Validators.NO_QUOTATION, [ 'Customer Name Continuation' ]);
  FormManager.addValidator('addrTxt', Validators.NO_QUOTATION, [ 'Street' ]);
  FormManager.addValidator('city1', Validators.NO_QUOTATION, [ 'City' ]);

  // FormManager.addValidator('postCd', Validators.NO_QUOTATION, [ 'Postal Code'
  // ]);
  FormManager.addValidator('addrAbbrevName', Validators.NO_QUOTATION, [ 'Abbreviated Name' ]);
  FormManager.addValidator('streetAbbrev', Validators.NO_QUOTATION, [ 'Street Abbreviation' ]);
  FormManager.addValidator('addrAbbrevLocn', Validators.NO_QUOTATION, [ 'Abbreviated Location' ]);

}

function addressQuotationValidatorUKI() {
  // CREATCMR-788
  FormManager.addValidator('abbrevNm', Validators.NO_QUOTATION, [ 'Abbreviated Name (TELX1)' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('abbrevLocn', Validators.NO_QUOTATION, [ 'Abbreviated Location' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('custNm1', Validators.NO_QUOTATION, [ 'Customer Name' ]);
  FormManager.addValidator('custNm2', Validators.NO_QUOTATION, [ 'Customer Name Con\'t' ]);
  FormManager.addValidator('addrTxt', Validators.NO_QUOTATION, [ 'Street' ]);
  FormManager.addValidator('addrTxt2', Validators.NO_QUOTATION, [ 'Street Cont' ]);
  FormManager.addValidator('city1', Validators.NO_QUOTATION, [ 'City' ]);
  // FormManager.addValidator('postCd', Validators.NO_QUOTATION, [ 'Postal Code'
  // ]);
  FormManager.addValidator('dept', Validators.NO_QUOTATION, [ 'Attn' ]);
  FormManager.addValidator('poBox', Validators.NO_QUOTATION, [ 'PO Box' ]);
  FormManager.addValidator('custPhone', Validators.NO_QUOTATION, [ 'Phone #' ]);
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

function hasValidLicenseDate() {
  var today = new Date().toISOString().slice(0, 10).replace(/-/g, '');
  if (CmrGrid.GRIDS.LICENSES_GRID_GRID && CmrGrid.GRIDS.LICENSES_GRID_GRID.rowCount > 0) {
    for (var i = 0; i < CmrGrid.GRIDS.LICENSES_GRID_GRID.rowCount; i++) {
      var record = CmrGrid.GRIDS.LICENSES_GRID_GRID.getItem(i);
      var validTo = record.validTo[0]
      if (today < validTo) {
        return true;
      }
    }
  }
  return false;
}

function sboSalesRepValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var sbo = FormManager.getActualValue('salesBusOffCd');
        var isuCd = FormManager.getActualValue('isuCd');
        var clientTier = FormManager.getActualValue('clientTier');
        var reqType = FormManager.getActualValue('reqType');
        var valResult = null;
        var oldSbo = null;
        var oldClientTier = null;
        var oldISU = null;
        var requestId = FormManager.getActualValue('reqId');

        if (reqType == 'C') {
          valResult = sboSalesRepCodeValidator();
        } else {
          qParams = {
            REQ_ID : requestId,
          };
          var result = cmr.query('GET.CLIENT_TIER_ISU_SBO_CD_OLD_BY_REQID', qParams);

          if (result != null && result != '') {
            oldClientTier = result.ret1 != null ? result.ret1 : '';
            oldSbo = result.ret3 != null ? result.ret3 : '';
            oldISU = result.ret2 != null ? result.ret2 : '';

            if (sbo != oldSbo) {
              valResult = sboSalesRepCodeValidator();
            }
          }
        }
        return valResult;
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function sboSalesRepCodeValidator() {
  var isuCd = FormManager.getActualValue('isuCd');
  var clientTier = FormManager.getActualValue('clientTier');
  var isuCtc = isuCd + clientTier;
  var sbo = FormManager.getActualValue('salesBusOffCd');
  var subRegion = FormManager.getActualValue('countryUse');
  var reqType = FormManager.getActualValue('reqType');
  var salesRep = FormManager.getActualValue('repTeamMemberNo');
  var custSubGrp = FormManager.getActualValue('custSubGrp');

  var role = null;
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }
  var landedCountry = getSoldToLanded();
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (reqType != 'C') {
    return new ValidationResult(null, true);
  }
  if (isuCtc == '8B') {
    if (!(sbo == 'ZP' || sbo == 'VQ' || sbo == 'ZS' || sbo == 'QQ')) {
      return new ValidationResult({
        id : 'salesBusOffCd',
        type : 'text',
        name : 'salesBusOffCd'
      }, false, 'SORTL can only accept \'ZP\'\ \'VQ\'\ \'ZS\'\ \'QQ\'\ for ISU CTC 8B.');
    }
    if (!(salesRep == '09ZPB0' || salesRep == '09VQB0' || salesRep == '09ZSB0' || salesRep == '09QQB0')) {
      return new ValidationResult({
        id : 'repTeamMemberNo',
        type : 'text',
        name : 'repTeamMemberNo'
      }, false, 'salesRep can only accept \'09ZPB0\'\ \'09VQB0\'\ \'09ZSB0\'\ \'09QQB0\'\ for ISU 8B.');
    }
    if (sbo != 'ZP' && salesRep == '09ZPB0') {
      return new ValidationResult({
        id : 'salesBusOffCd',
        type : 'text',
        name : 'salesBusOffCd'
      }, false, 'SORTL can only accept \'ZP\'\  for ISU CTC 8B and salesRep 09ZPB0.');
    } else if (sbo != 'VQ' && salesRep == '09VQB0') {
      return new ValidationResult({
        id : 'salesBusOffCd',
        type : 'text',
        name : 'salesBusOffCd'
      }, false, 'SORTL can only accept \'VQ\'\  for ISU CTC 8B and salesRep 09VQB0.');
    } else if (sbo != 'ZS' && salesRep == '09ZSB0') {
      return new ValidationResult({
        id : 'salesBusOffCd',
        type : 'text',
        name : 'salesBusOffCd'
      }, false, 'SORTL can only accept \'ZS\'\  for ISU CTC 8B and salesRep 09ZSB0.');
    } else if (sbo != 'QQ' && salesRep == '09QQB0') {
      return new ValidationResult({
        id : 'salesBusOffCd',
        type : 'text',
        name : 'salesBusOffCd'
      }, false, 'SORTL can only accept \'QQ\'\  for ISU CTC 8B and salesRep 09QQB0.');
    }
  } else if (isuCtc == '21' && (custSubGrp == 'XIBM' || custSubGrp == 'IBMIT')) {
    if (!(sbo == '1T')) {
      return new ValidationResult({
        id : 'salesBusOffCd',
        type : 'text',
        name : 'salesBusOffCd'
      }, false, 'SORTL can only accept \'1T\'\ for ISU CTC 8B.');
    }
  } else if (isuCtc == '21') {
    if (!(sbo == '99' || sbo == 'ZZ' || sbo == '1B' || sbo == '1G' || sbo == '1E' || sbo == '11' || sbo == '12' || sbo == '13' || sbo == '14')) {
      return new ValidationResult({
        id : 'salesBusOffCd',
        type : 'text',
        name : 'salesBusOffCd'
      }, false, 'SORTL can only accept \'99\'\ \'ZZ\'\ \'1B\'\ \'1G\'\ \'1E\'\ \'11\'\  \'12\'\ \'13\'\ \'14\'\ for ISU CTC 8B.');
    }
  } else if (isuCtc == '34Q') {
    if (!(sbo == 'PA' || sbo == 'PB' || sbo == 'PC' || sbo == 'PD' || sbo == 'PE' || sbo == 'PF' || sbo == 'PG' || sbo == 'PH' || sbo == 'PI' || sbo == 'PJ' || sbo == 'PK' || sbo == 'PL'
        || sbo == 'PM' || sbo == 'PN' || sbo == 'PO' || sbo == 'PP')) {
      return new ValidationResult({
        id : 'salesBusOffCd',
        type : 'text',
        name : 'salesBusOffCd'
      }, false, 'SORTL can only accept \'PA\'\ \'PB\'\ \'PC\'\ \'PD\'\ \'PE\'\ \'PF\'\  \'PG\'\ \'PH\'\ \'PI\'\ \'PJ\'\ \'PK\'\ \'PL\'\ \'PM\'\ \'PN\'\ \'PO\'\ \'PP\'\ for ISU CTC 34Q.');
    }
  } else if (isuCtc == '27E') {
    if (!(sbo == 'NM' || sbo == 'RP' || sbo == 'GJ' || sbo == 'GK' || sbo == 'NG' || sbo == 'NC' || sbo == 'KA' || sbo == 'KF' || sbo == 'NB' || sbo == 'GH' || sbo == 'KC' || sbo == 'KB'
        || sbo == 'DU' || sbo == 'KE' || sbo == 'KD' || sbo == 'NI')) {
      return new ValidationResult({
        id : 'salesBusOffCd',
        type : 'text',
        name : 'salesBusOffCd'
      }, false, 'SORTL can only accept \'NM\'\ \'RP\'\ \'GJ\'\ \'GK\'\ \'NG\'\ \'NC\'\ \'KA\'\ \'KF\'\ \'NB\'\ \'GH\'\ \'KC\'\ \'KB\'\ \'DU\'\ \'KE\'\ \'KD\'\ \'NI\'\ for ISU CTC 27E.');
    }
  } else if (isuCtc == '36Y') {
    if (!(sbo == 'FL' || sbo == 'FM' || sbo == 'FP' || sbo == 'FQ' || sbo == 'FR' || sbo == 'FS' || sbo == 'FT' || sbo == 'FV' || sbo == 'FW' || sbo == 'FX' || sbo == 'FY' || sbo == 'FZ')) {
      return new ValidationResult({
        id : 'salesBusOffCd',
        type : 'text',
        name : 'salesBusOffCd'
      }, false, 'SORTL can only accept \'FL\'\ \'FM\'\ \'FP\'\ \'FQ\'\ \'FR\'\ \'FS\'\  \'FT\'\ \'FV\'\ \'FW\'\ \'FX\'\ \'FY\'\ \'FZ\'\  for ISU CTC 36Y.');
    }
  } else if (isuCtc == '04') {
    if (!(sbo == 'GG' || sbo == 'HG' || sbo == 'SD')) {
      return new ValidationResult({
        id : 'salesBusOffCd',
        type : 'text',
        name : 'salesBusOffCd'
      }, false, 'SORTL can only accept \'GG\'\ \'HG\'\ \'SD\'\  for ISU 04.');
    }
  } else if (isuCtc == '19') {
    if (!(sbo == 'EB' || sbo == 'SD')) {
      return new ValidationResult({
        id : 'salesBusOffCd',
        type : 'text',
        name : 'salesBusOffCd'
      }, false, 'SORTL can only accept \'EB\'\ \'SD\'\ for ISU 19.');
    }
  } else if (isuCtc == '28') {
    if (!(sbo == 'SD' || sbo == 'TQ' || sbo == 'TX')) {
      return new ValidationResult({
        id : 'salesBusOffCd',
        type : 'text',
        name : 'salesBusOffCd'
      }, false, 'SORTL can only accept \'SD\'\ \'TQ\'\ \'TX\'\ for ISU 28.');
    }
  } else if (isuCtc == '4A') {
    if (sbo != 'XD') {
      return new ValidationResult({
        id : 'salesBusOffCd',
        type : 'text',
        name : 'salesBusOffCd'
      }, false, 'SORTL can only accept \'XD\'\ for ISU 4A.');
    }
  } else if (isuCtc == '12' || isuCtc == '15' || isuCtc == '31' || isuCtc == '1R' || isuCtc == '3T' || isuCtc == '4F' || isuCtc == '5B' || isuCtc == '5E' || isuCtc == '5K') {
    if (sbo != 'SD') {
      return new ValidationResult({
        id : 'salesBusOffCd',
        type : 'text',
        name : 'salesBusOffCd'
      }, false, 'SORTL can only accept  \'SD\'\ for ISU' + isuCtc + '.');
    }
  }
  // else if(isuCtc == '34Q' || isuCtc == '36Y' || isuCtc == '04' || isuCtc ==
  // '12' || isuCtc == '15' || isuCtc == '19' || isuCtc == '28' || isuCtc ==
  // '31' || isuCtc == '1R'
  // || isuCtc == '3T' || isuCtc == '4A' || isuCtc == '5B' || isuCtc == '4F' ||
  // isuCtc == '5E' || isuCtc == '5K' ) {
  // if (!(sbo == '99' || sbo == 'ZZ' || sbo == '1B' || sbo == '1G' || sbo ==
  // '1E' || sbo == '11' || sbo == '12' || sbo == '13' || sbo == '14')) {
  // return new ValidationResult({
  // id : 'salesBusOffCd',
  // type : 'text',
  // name : 'salesBusOffCd'
  // }, false, 'SORTL can only accept \'99\'\ \'ZZ\'\ \'1B\'\ \'1G\'\ \'1E\'\
  // \'11\'\ \'12\'\ \'13\'\ \'14\'\ for ISU CTC 8B.');
  // }
  // }
}

function getSoldToLanded() {
  var countryCd = FormManager.getActualValue('landCntry');
  var _zs01ReqId = FormManager.getActualValue('reqId');
  var cntryCdParams = {
    REQ_ID : _zs01ReqId,
    ADDR_TYPE : 'ZS01',
  };
  var cntryCdResult = cmr.query('ADDR.GET.LANDEDCNTRY.BY_REQID_ADDRTYPE', cntryCdParams);

  if (cntryCdResult.ret1 != undefined) {
    countryCd = cntryCdResult.ret1;
  }

  return countryCd;
}

function isNewLicenseAdded() {
  if (CmrGrid.GRIDS.LICENSES_GRID_GRID && CmrGrid.GRIDS.LICENSES_GRID_GRID.rowCount > 0) {
    for (var i = 0; i < CmrGrid.GRIDS.LICENSES_GRID_GRID.rowCount; i++) {
      var record = CmrGrid.GRIDS.LICENSES_GRID_GRID.getItem(i);
      var changeIndc = record.currentIndc[0];
      if ('N' == changeIndc) {
        return true;
      }
    }
  }
  return false;
}
dojo.addOnLoad(function() {
  GEOHandler.EMEA = [ SysLoc.UK, SysLoc.IRELAND, SysLoc.ISRAEL, SysLoc.TURKEY, SysLoc.GREECE, SysLoc.CYPRUS, SysLoc.ITALY ];
  console.log('adding EMEA functions...');
  GEOHandler.addAddrFunction(addEMEALandedCountryHandler, GEOHandler.EMEA);
  GEOHandler.enableCopyAddress(GEOHandler.EMEA, validateEMEACopy, [ 'ZD01', 'CTYC' ]);
  GEOHandler.enableCustomerNamesOnAddress(GEOHandler.EMEA);
  GEOHandler.addAddrFunction(updateMainCustomerNames, GEOHandler.EMEA);
  GEOHandler.setRevertIsicBehavior(false);
  GEOHandler.addAfterTemplateLoad(emeaPpsCeidValidator, GEOHandler.EMEA);

  // Italy
  GEOHandler.registerValidator(addCrossBorderValidatorForIT, [ SysLoc.ITALY ], null, true);
  GEOHandler.registerValidator(addCompanyAddrValidator, [ SysLoc.ITALY ], null, true);
  GEOHandler.registerValidator(addBillingAddrValidator, [ SysLoc.ITALY ], null, true);
  GEOHandler.registerValidator(addGenericVATValidator(SysLoc.ITALY, 'MAIN_CUST_TAB', 'frmCMR', 'ZP01'), [ SysLoc.ITALY ], null, true);
  GEOHandler.registerValidator(validateFiscalCodeForCreatesIT, [ SysLoc.ITALY ], null, true);
  GEOHandler.registerValidator(validateLandCntryForCBCreatesIT, [ SysLoc.ITALY ], null, true);
  /* 1438717 - add DPL match validation for failed dpl checks */
  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.EMEA, GEOHandler.ROLE_PROCESSOR, true);

  GEOHandler.registerValidator(checkIfVATFiscalUpdatedIT, [ SysLoc.ITALY ]);
  GEOHandler.registerValidator(validateFiscalLengthOnIdentIT, [ SysLoc.ITALY ]);
  GEOHandler.registerValidator(validateVATOnIdentIT, [ SysLoc.ITALY ]);
  GEOHandler.registerValidator(addInstallingAddrValidator, [ SysLoc.ITALY ], null, true);
  GEOHandler.registerValidator(validateEnterpriseNumForIT, [ SysLoc.ITALY ], null, true);
  GEOHandler.registerValidator(checkIfStateProvBlankForProcIT, [ SysLoc.ITALY ], GEOHandler.ROLE_PROCESSOR, true);
  GEOHandler.registerValidator(stateProvValidatorCBforIT, [ SysLoc.ITALY ]);
  GEOHandler.registerValidator(validateSingleReactParentCMR, [ SysLoc.ITALY ], null, true);
  GEOHandler.registerValidator(validateCodiceDesIT, [ SysLoc.ITALY ], null, true);
  GEOHandler.registerValidator(validateCollectionCdIT, [ SysLoc.ITALY ], null, true);
  GEOHandler.registerValidator(validateCMRNumberForIT, [ SysLoc.ITALY ], null, true);
  GEOHandler.registerValidator(addEmbargoCodeValidatorIT, [ SysLoc.ITALY ], null, true);
  GEOHandler.registerValidator(validateExistingCMRNo, [ SysLoc.ITALY ], null, true);
  GEOHandler.registerValidator(addCMRValidator, [ SysLoc.ITALY ], null, true);
  GEOHandler.registerValidator(addBillingValidator, [ SysLoc.ITALY ], null, true);

  GEOHandler.addAddrFunction(addAddrFunctionItaly, [ SysLoc.ITALY ]);
  // GEOHandler.addAddrFunction(setDeafultSBOLogicComm, [ SysLoc.ITALY ]);

  GEOHandler.checkRoleBeforeAddAddrFunction(addAddrValidationForProcItaly, [ SysLoc.ITALY ], null, GEOHandler.ROLE_PROCESSOR);
  // Coverage Functions- control ISU CTC SORTL SalesRep
  GEOHandler.registerValidator(validateSBOForIT, [ SysLoc.ITALY ]);
  GEOHandler.registerValidator(validateSalesRepForIT, [ SysLoc.ITALY ]);
  GEOHandler.registerValidator(checkIsicCodeValidationIT, [ SysLoc.ITALY ]);
  GEOHandler.addAfterTemplateLoad(setClientTierValuesIT, [ SysLoc.ITALY ]);
  // GEOHandler.addAfterTemplateLoad(setDeafultISUCtcChange, [ SysLoc.ITALY ]);
  GEOHandler.addAfterConfig(addISUHandlerIT, [ SysLoc.ITALY ]);
  GEOHandler.addAfterConfig(addIsicCdHandler, [ SysLoc.ITALY ]);
  // GEOHandler.addAfterConfig(setClientTierValuesIT, [ SysLoc.ITALY ]);
  // GEOHandler.addAfterConfig(setDeafultSBOLogicComm, [ SysLoc.ITALY ]);
  GEOHandler.registerValidator(clientTierValidator, [ SysLoc.ITALY ], null, true);

  GEOHandler.addAfterTemplateLoad(addAfterTemplateLoadItaly, [ SysLoc.ITALY ]);
  GEOHandler.addAfterConfig(addAfterConfigItaly, [ SysLoc.ITALY ]);

  // For EmbargoCode

  GEOHandler.addAfterConfig(lockEmbargo, GEOHandler.EMEA);
  GEOHandler.registerValidator(StcOrderBlockValidation, GEOHandler.EMEA, null, true);
  GEOHandler.registerValidator(sboSalesRepValidator, [ SysLoc.ITALY ], null, true);

});
