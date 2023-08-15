/* Register NETHERLANDS Javascripts */
var _addrTypesForNL = [ 'ZS01', 'ZP01', 'ZD01', 'ZI01', 'ZP02' ];
var _poBOXHandler = [];
var _reqReasonHandler = null;

function afterConfigForNL() {
  var reqType = FormManager.getActualValue('reqType');
  var role = null;
  FormManager.readOnly('capInd');
  FormManager.setValue('capInd', true);
  FormManager.readOnly('cmrOwner');
  FormManager.resetValidations('enterprise');
  FormManager.readOnly('sensitiveFlag');

  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }

  // var custSubGrp = FormManager.getActualValue('custSubGrp');
  // if (custSubGrp == 'PRICU') {
  // FormManager.removeValidator('inacCd', Validators.REQUIRED);
  // FormManager.readOnly('inacCd');
  // FormManager.setValue('inacCd', '');
  // }

  // Set abbrevLocn for Softlayer Scenario
  var custSubType = FormManager.getActualValue('custSubGrp');
  if (custSubType == 'SOFTL') {
    FormManager.setValue('abbrevLocn', 'Softlayer');
  }
  if (reqType == 'C' && role != 'Processor') {
    FormManager.readOnly('inacCd');
    FormManager.readOnly('abbrevNm');
    FormManager.readOnly('subIndustryCd');
    // FormManager.resetValidations('engineeringBo');
  } else {
    FormManager.enable('inacCd');
  }
  if (custSubType == 'PRICU') {
    FormManager.clearValue('enterprise');
  }

  if (role == 'Processor' && reqType == 'C') {
    FormManager.enable('abbrevNm');
    FormManager.removeValidator('clientTier', Validators.REQUIRED);
    FormManager.addValidator('abbrevNm', Validators.REQUIRED, [ 'Abbreviated Name' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('abbrevLocn', Validators.REQUIRED, [ 'Abbreviated Location' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('isuCd', Validators.REQUIRED, [ 'ISU Code' ], 'MAIN_IBM_TAB');
    /*
     * if (FormManager.getActualValue('custSubGrp') != 'IBMEM') {
     * FormManager.addValidator('clientTier', Validators.REQUIRED, [ 'Client
     * Tier' ], 'MAIN_IBM_TAB'); }
     */
    // FormManager.addValidator('engineeringBo', Validators.REQUIRED, [ 'BO
    // Team' ], 'MAIN_IBM_TAB');
  } else {
    FormManager.removeValidator('isuCd', Validators.REQUIRED);
    FormManager.removeValidator('clientTier', Validators.REQUIRED);
    FormManager.removeValidator('engineeringBo', Validators.REQUIRED);
  }
  if (reqType == 'U') {
    FormManager.resetValidations('engineeringBo');
    FormManager.resetValidations('collectionCd');
  }
  if (reqType == 'C') {
    FormManager.clearValue('collectionCd');
    FormManager.readOnly('collectionCd');
  }
  if (role == 'Processor' || reqType == 'U' && FormManager.getActualValue('viewOnlyPage') != 'true') {
    FormManager.enable('abbrevLocn');
  } else {
    FormManager.readOnly('abbrevLocn');
  }
  var landCntry = '';
  var cntryRegion = FormManager.getActualValue('countryUse');
  if (cntryRegion && cntryRegion != '' && cntryRegion.length > 3) {
    landCntry = cntryRegion.substring(3, 5);
  }
  if (landCntry != '') {
    FormManager.setValue('defaultLandedCountry', landCntry);
  }
  setVatValidatorNL();

  var custSubScnrio = FormManager.getActualValue('custSubGrp');
  var vatInd = FormManager.getActualValue('vatInd');
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }
  var sourceSystId = FormManager.getActualValue('sourceSystId');
  // PayGo_check
  var qParams = {
    SYST_ID : sourceSystId
  };
  var paygoUser = cmr.query('PAYGO.CHECK.CRN', qParams);
  var countpaygo = paygoUser.ret1;
  if ((custSubScnrio == 'PUBCU' && role == 'Processor') || custSubScnrio == 'PRICU' || custSubScnrio == 'CBCOM' || custSubScnrio == 'CBBUS' || custSubScnrio == 'IBMEM'
      || (custSubScnrio == 'INTER' && dojo.string.trim(vatInd) == 'N') || viewOnlyPage == 'true' || (Number(countpaygo) == 1 && role == 'Processor')) {
    FormManager.removeValidator('taxCd2', Validators.REQUIRED);
  } else if (reqType != 'U') {
    FormManager.addValidator('taxCd2', Validators.REQUIRED, [ 'KVK' ], 'MAIN_CUST_TAB');
  }

  var custGrp = FormManager.getActualValue('custGrp');
  if (custGrp == 'CROSS') {
    FormManager.setValue('custPrefLang', 'E');
  }

  setDeptartmentNumber();
  disableCmrNo();
  var clientTier = FormManager.getField('clientTier');
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }
  if (role == 'Processor' && clientTier != null) {
    // setBOTeamValues(clientTier);
  }
  lockDunsNo();
  disableIBMTab();
  // CREATCMR-788
  addressQuotationValidatorNL();
}

function lockDunsNo() {
  var reqType = FormManager.getActualValue('reqType');
  if (reqType != 'C') {
    return;
  }
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (role == 'REQUESTER') {
    FormManager.readOnly('dunsNo');
  }
}

function disableIBMTab() {
  var reqType = FormManager.getActualValue('reqType');
  var cntryUse = FormManager.getActualValue('countryUse');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }
  if (reqType == 'C' && role == 'Requester') {
    FormManager.readOnly('cmrNo');
    FormManager.readOnly('cmrOwner');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('inacCd');
    FormManager.readOnly('searchTerm');
    FormManager.readOnly('enterprise');

    FormManager.readOnly('bgId');
    FormManager.readOnly('gbgId');
    FormManager.readOnly('bgRuleId');
    FormManager.readOnly('covId');
    FormManager.readOnly('geoLocationCd');
    FormManager.readOnly('dunsNo');
    FormManager.readOnly('salesBusOffCd');
    FormManager.readOnly('economicCd');
  } else if (reqType == 'C' && role == 'Processor') {
    FormManager.enable('cmrNo');
  }
  if (custSubGrp.includes('IBM')) {
    FormManager.readOnly('enterprise');
    FormManager.readOnly('inacCd');
    FormManager.readOnly('searchTerm');
    FormManager.readOnly('dunsNo');
    FormManager.readOnly('cmrNo');
  } else {
    FormManager.enable('enterprise');
    FormManager.enable('inacCd');
    FormManager.enable('searchTerm');
    if (role == 'Processor') {
      FormManager.enable('cmrNo');
      FormManager.enable('dunsNo');
    }
  }
}

function checkCmrUpdateBeforeImport() {
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

function disableCmrNo() {
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }
  var reqType = FormManager.getActualValue('reqType');
  if (reqType == 'U') {
    // do nothing for Update request
  } else if (reqType == 'C') {
    if (role == 'Requester') {
      FormManager.readOnly('cmrNo');
    } else if (role == 'Processor') {
      FormManager.enable('cmrNo');
    }
  }
}

/* Vat Handler */
function setVatValidatorNL() {
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (custSubGrp == 'PUBCU') {
    FormManager.removeValidator('vat', Validators.REQUIRED);
    return;
  }
  if (viewOnlyPage != 'true' && FormManager.getActualValue('reqType') == 'C') {
    if (custSubGrp == 'IBMEM' || custSubGrp == 'PRICU') {
      FormManager.removeValidator('vat', Validators.REQUIRED);
      FormManager.clearValue('vat');
      FormManager.readOnly('vat');
      return;
    }
    if (custSubGrp == 'IBMEM' && dojo.byId('vatExempt').checked) {
      FormManager.removeValidator('vat', Validators.REQUIRED);
      FormManager.clearValue('vat');
      FormManager.readOnly('vat');
    }

    // FormManager.resetValidations('vat');
    // if (!dojo.byId('vatExempt').checked) {
    // checkAndAddValidator('vat', Validators.REQUIRED, [ 'VAT' ]);
    // FormManager.enable('vat');
    // }
  }
}

function setKVKValidatorNL() {
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  if (viewOnlyPage == 'true') {
    return;
  }
  var reqType = FormManager.getActualValue('reqType');
  if (reqType != 'C') {
    return;
  }
  var custSubScnrio = FormManager.getActualValue('custSubGrp');
  var vatInd = FormManager.getActualValue('vatInd');
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }
  if (custSubScnrio == 'PUBCU' && role == 'Processor') {
    FormManager.removeValidator('taxCd2', Validators.REQUIRED);
    return;
  }
  if (custSubScnrio == 'INTER') {
    if (vatInd == 'N') {
      FormManager.removeValidator('taxCd2', Validators.REQUIRED);
    } else {
      FormManager.addValidator('taxCd2', Validators.REQUIRED, [ 'KVK' ], 'MAIN_CUST_TAB');
    }
  }
}

function setDeptartmentNumber() {

  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  if (viewOnlyPage == 'true') {
    return;
  }
  var reqType = FormManager.getActualValue('reqType');
  var custSubScnrio = FormManager.getActualValue('custSubGrp');
  var cmrNo = FormManager.getActualValue('cmrNo');
  var intCmrRegEx = /(^99)/;
  var deptNum = FormManager.getActualValue('ibmDeptCostCenter');

  if (reqType == 'C') {
    if (deptNum && deptNum != '') {
      FormManager.setValue('ibmDeptCostCenter', deptNum);
    }
    if (custSubScnrio != '' && custSubScnrio) {
      FormManager.readOnly('ibmDeptCostCenter');
      FormManager.setValue('ibmDeptCostCenter', '');
    } else {
      FormManager.enable('ibmDeptCostCenter');
    }
  } else {
    if (intCmrRegEx.test(cmrNo)) {
      FormManager.enable('ibmDeptCostCenter');
    } else {
      FormManager.readOnly('ibmDeptCostCenter');
      FormManager.setValue('ibmDeptCostCenter', '');
    }
  }
}

function disableLandCntry() {
  var custType = FormManager.getActualValue('custGrp');
  var reqType = FormManager.getActualValue('reqType');
  if (custType == 'LOCAL' && FormManager.getActualValue('addrType') == 'ZS01') {
    FormManager.readOnly('landCntry');
  } else {
    FormManager.enable('landCntry');
  }
  if (reqType == 'U') {
    if (FormManager.getActualValue('addrType') == 'ZS01' || FormManager.getActualValue('addrType') == 'ZP01') {
      FormManager.readOnly('landCntry');
    } else {
      FormManager.enable('landCntry');
    }
  }
}

function addLandedCountryHandler(cntry, addressMode, saving, finalSave) {
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
 * lock Embargo Code field
 */
function lockEmbargo() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (role == 'REQUESTER') {

    FormManager.readOnly('embargoCd');
  } else {
    FormManager.enable('embargoCd');
  }
}

/**
 * After config handlers
 */
var _ISUHandler = null;
var _CTCHandler = null;
var _BOTeamHandler = null;
var _IMSHandler = null;
var _vatExemptHandler = null;
var _ExpediteHandler = null;
var _SubSceanrioHandler = null;

function addHandlersForNL() {

  if (_ISUHandler == null) {
    _ISUHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
      setClientTierValues(value);
      // setClientTierValuesForUpdate();
      setSORTLBasedOnIsuCtc();
      // setBOTeamValues();
    });
  }

  if (_CTCHandler == null) {
    _CTCHandler = dojo.connect(FormManager.getField('clientTier'), 'onChange', function(value) {
      // setBOTeamValues(value);
      setSORTLBasedOnIsuCtc();
    });
  }

  if (_IMSHandler == null && FormManager.getActualValue('cmrIssuingCntry')) {
    _IMSHandler = dojo.connect(FormManager.getField('subIndustryCd'), 'onChange', function(value) {
      // setBOTeamValues(value);
    });
  }

  if (_BOTeamHandler == null) {
    _BOTeamHandler = dojo.connect(FormManager.getField('engineeringBo'), 'onChange', function(value) {
      // setINACValues(value);
      // setEconomicCodeValues(value);
      setSORTL();
    });
  }
  if (_vatExemptHandler == null) {
    _vatExemptHandler = dojo.connect(FormManager.getField('vatExempt'), 'onClick', function(value) {
      setVatValidatorNL();
      setKVKValidatorNL();
    });
  }

  if (_ExpediteHandler == null) {
    _ExpediteHandler = dojo.connect(FormManager.getField('expediteInd'), 'onChange', function(value) {
      setExpediteReason();
    });
  }

  if (_SubSceanrioHandler == null) {
    _SubSceanrioHandler = dojo.connect(FormManager.getField('custSubGrp'), 'onChange', function(value) {
      setEcoCodeBasedOnSubScenario();
    });
  }
}

function setExpediteReason() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (FormManager.getActualValue('expediteInd') != 'Y') {
    FormManager.clearValue('expediteReason');
    // FormManager.readOnly('expediteReason');
  }
}

/*
 * Set Client Tier Value
 */
function setClientTierValues(isuCd) {

  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }

  isuCd = FormManager.getActualValue('isuCd');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var clientTierUI = FormManager.getActualValue('clientTier');
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
        if ('7' == clientTierUI && ('5K' == isuCd || '21' == isuCd || '28' == isuCd)) {
          FormManager.setValue('clientTier', '');
        }
        FormManager.limitDropdownValues(FormManager.getField('clientTier'), clientTiers);
        if (clientTiers.length == 1) {
          FormManager.setValue('clientTier', clientTiers[0]);
        }
      }
    }
  }
}

/**
 * NL - sets BO Team based on isuCtc
 */
/*
 * function setBOTeamValues(clientTier) { if
 * (FormManager.getActualValue('viewOnlyPage') == 'true') { return; }
 * 
 * if (FormManager.getActualValue('reqType') != 'C') { return; }
 * 
 * var clientTier = FormManager.getActualValue('clientTier'); var cntry =
 * FormManager.getActualValue('cmrIssuingCntry'); var isuCd =
 * FormManager.getActualValue('isuCd'); var ims =
 * FormManager.getActualValue('subIndustryCd'); var role =
 * FormManager.getActualValue('userRole').toUpperCase();
 * 
 * var boTeam = []; var selectedBoTeam = []; if (isuCd != '') { var isuCtc =
 * isuCd + clientTier; var qParams = null; var results = null; var
 * selectedResults = null;
 *  // 32S changed to 34Q on 2021 2H coverage // BO Team will be based on IMS
 * for 32S // if (ims != '' && ims.length > 1 && (isuCtc == '32S')) { if (ims != '' &&
 * ims.length > 1 && (isuCtc == '34Q')) { qParams = { _qall : 'Y', ISSUING_CNTRY :
 * cntry, ISU : '%' + isuCd + clientTier + '%' // CLIENT_TIER : '%' +
 * ims.substring(0, 1) + '%' }; results = cmr.query('GET.BOTEAMLIST.BYISU',
 * qParams); }
 *  // if (ims != '' && ims.length > 1 && (isuCtc == '32S')) { if (ims != '' &&
 * ims.length > 1 && (isuCtc == '34Q')) { qParams = { _qall : 'Y', ISSUING_CNTRY :
 * cntry, ISU : '%' + isuCd + clientTier + '%', CLIENT_TIER : '%' +
 * ims.substring(0, 1) + '%' }; selectedResults =
 * cmr.query('GET.BOTEAMLIST.BYISUCTC', qParams); }
 * 
 * if (results != null || selectedResults != null) {
 * 
 * if (results != null) { for (var i = 0; i < results.length; i++) {
 * boTeam.push(results[i].ret1); } }
 * 
 * if (selectedResults != null) { for (var i = 0; i < selectedResults.length;
 * i++) { selectedBoTeam.push(selectedResults[i].ret1);
 * FormManager.setValue('engineeringBo', selectedBoTeam[0]); } }
 * 
 * FormManager.limitDropdownValues(FormManager.getField('engineeringBo'),
 * boTeam); if (boTeam.length == 1) { FormManager.setValue('engineeringBo',
 * boTeam[0]); } // 2020-09-18 CMR-5004 console.log('custSubGrp==' +
 * FormManager.getActualValue('custSubGrp')); console.log('pagemodel
 * custSubGrp==' + _pagemodel.custSubGrp);
 * 
 * if (FormManager.getActualValue('custSubGrp') != null &&
 * FormManager.getActualValue('custSubGrp') != '' &&
 * FormManager.getActualValue('custSubGrp') != _pagemodel.custSubGrp ||
 * (FormManager.getActualValue('custSubGrp') == _pagemodel.custSubGrp &&
 * FormManager.getActualValue('subIndustryCd') != _pagemodel.subIndustryCd)) {
 * FormManager.setValue('engineeringBo', selectedBoTeam[0]);
 * 
 * var custSubScnrio = FormManager.getActualValue('custSubGrp'); if
 * (custSubScnrio == 'BUSPR' || custSubScnrio == 'CBBUS') {
 * FormManager.setValue('engineeringBo', '33P01');
 * FormManager.readOnly('engineeringBo'); FormManager.readOnly('economicCd'); }
 * else if (custSubScnrio == 'INTER' || custSubScnrio == 'PRICU') {
 * FormManager.setValue('engineeringBo', '33U00');
 * FormManager.readOnly('engineeringBo'); FormManager.readOnly('economicCd'); }
 * else if (custSubScnrio == 'PUBCU' && role != 'PROCESSOR') {
 * FormManager.enable('economicCd'); } else {
 * FormManager.enable('engineeringBo'); if (custSubScnrio != 'CBBUS' && role !=
 * 'REQUESTER') FormManager.enable('economicCd'); } } } lockEngineeringBo(); } }
 */

function lockEngineeringBo() {
  var role = null;
  var reqType = FormManager.getActualValue('reqType');
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }
  if (role == 'Requester' && reqType == 'C') {
    FormManager.readOnly('engineeringBo');
  }
}

/*
 * NL - INAC Code based on BOTEAM Values
 */
function setINACValues(engineeringBo) {

  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }

  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var engineeringBo = FormManager.getActualValue('engineeringBo');

  var inacCode = [];
  if (engineeringBo != '') {
    var qParams = {
      _qall : 'Y',
      ISSUING_CNTRY : cntry,
      EngineeringBo : '%' + engineeringBo + '%'
    };
    var results = cmr.query('GET.INACLIST.BYBO', qParams);
    if (results != null) {
      for (var i = 0; i < results.length; i++) {
        inacCode.push(results[i].ret1);
      }
      if (inacCode != null) {
        FormManager.limitDropdownValues(FormManager.getField('inacCd'), inacCode);
        if (inacCode.length == 1) {
          FormManager.setValue('inacCd', inacCode[0]);
        }
        /*
         * if (inacCode.length == 0) { FormManager.setValue('inacCd', ''); }
         */
      }
    }
  }
}

/*
 * NL - Economic Code based on BOTEAM Values
 */
function setEconomicCodeValues(engineeringBo) {

  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }

  var role = FormManager.getActualValue('userRole').toUpperCase();
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var engineeringBo = FormManager.getActualValue('engineeringBo');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var isuCd = FormManager.getActualValue('isuCd');
  var clientTier = FormManager.getActualValue('clientTier');

  var economicCode = [];
  if (engineeringBo != '') {
    var qParams = {
      _qall : 'Y',
      ISSUING_CNTRY : cntry,
      REP_TEAM_MEMBER_NO : '%' + engineeringBo + '%'
    };
    var results = cmr.query('GET.ECONOMICLIST.BYST.NL', qParams);
    if (results != null) {
      for (var i = 0; i < results.length; i++) {
        economicCode.push(results[i].ret1);
      }
      if (economicCode != null) {
        FormManager.limitDropdownValues(FormManager.getField('economicCd'), economicCode);
        if (economicCode.length == 1) {
          FormManager.setValue('economicCd', economicCode[0]);
        }
        if ('PRICU' == custSubGrp && '33U00' == engineeringBo) {
          FormManager.setValue('economicCd', 'K60');
        }
        if ('INTER' == custSubGrp && '33U00' == engineeringBo) {
          FormManager.setValue('economicCd', 'K81');
        }
        if ('33P01' == engineeringBo && ('BUSPR' == custSubGrp || 'CBBUS' == custSubGrp)) {
          FormManager.setValue('economicCd', 'K49');
        }
        if ('5B' == isuCd && '7' == clientTier && ('2NY77' == engineeringBo || '33P01' == engineeringBo)) {
          FormManager.setValue('economicCd', 'K44');
        }
        if ('32' == isuCd && 'N' == clientTier && '2NY77' == engineeringBo) {
          FormManager.setValue('economicCd', 'K43');
        }
        if (role == 'REQUESTER') {
          FormManager.readOnly('economicCd');
        }
      }
    }
  }
}

function setSORTL() {

  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  var boTeam = FormManager.getActualValue('engineeringBo');
  var custSubType = FormManager.getActualValue('custSubGrp');
  var reqType = FormManager.getActualValue('reqType');

  if (reqType != 'C') {
    return;
  }
  switch (custSubType) {
  case 'INTER':
  case 'PRICU':
  case 'BUSPR':
    /*
     * if (boTeam != _pagemodel.engineeringBo) {
     * FormManager.setValue('commercialFinanced', boTeam); } break;
     */
  case 'COMME':
  case 'PUBCU':
  case 'THDPT':
  case 'NLDAT':
    // do nothing
    break;
  case 'CBBUS':
  case 'CBCOM':
    // do nothing
    break;
  default:
    break;
  }
}

function addAbbrevLocnLengthValidator() {
  var reqType = FormManager.getActualValue('reqType');
  var role = _pagemodel.userRole;
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var _abbrevLocn = FormManager.getActualValue('abbrevLocn');
        if (reqType != 'U' && role == 'Requester' && _abbrevLocn.length != 12) {
          return new ValidationResult({
            id : 'abbrevLocn',
            type : 'text',
            name : 'abbrevLocn'
          }, false, 'The length for Abbreviated Location should be 12 characters.');
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function addAbrrevNameLengthValidator() {

  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var _abbrevName = FormManager.getActualValue('abbrevNm');
        if (reqType != 'U' && role == 'Requester' && _abbrevName.length != 22) {
          return new ValidationResult({
            id : 'abbrevNm',
            type : 'text',
            name : 'abbrevNm'
          }, false, 'The length for Abbreviated Name should be 22 characters.');
        } else {
          return new ValidationResult(null, true);
        }
      }

    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');

}

function addKVKLengthValidator() {
  var reqType = FormManager.getActualValue('reqType');
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var _kvkLen = FormManager.getActualValue('taxCd2');
        if (reqType != 'U' && _kvkLen && _kvkLen.length > 0 && _kvkLen.length != 8) {
          return new ValidationResult({
            id : 'taxCd2',
            type : 'text',
            name : 'taxCd2'
          }, false, 'The length for KVK should be 8 characters.');
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function checkForBnkruptcy() {
  reqType = FormManager.getActualValue('reqType');
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var _reqreason = FormManager.getActualValue('reqReason');
        if (reqType != 'U' && role == 'Requester' && _reqreason == 'BKSC') {
          return new ValidationResult(null, false, 'The Request Reason cannot be chosen as "Bankruptcy" for Create requests.');
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_GENERAL_TAB', 'frmCMR');
}

function chngesAfterBnkrptcy() {
  reqType = FormManager.getActualValue('reqType');
  var reqRsn = FormManager.getActualValue('reqReason');
  if (reqRsn == 'BKSC' && reqType == 'U') {
    FormManager.setValue('embargoCd', '5');
    FormManager.setValue('collectionCd', 'TCNL99');
  }
}

/* 1430539 - do not allow delete of imported addresses on update requests */

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

function addHandlerForReqRsn() {
  if (_reqReasonHandler == null) {
    _reqReasonHandler = dojo.connect(FormManager.getField('reqReason'), 'onChange', function(value) {
      chngesAfterBnkrptcy();
    });
  }
}
function setAbbrvNmLoc() {
  var role = FormManager.getActualValue('userRole').toUpperCase();

  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  if (role != 'REQUESTER') {
    return;
  }
  var reqId = FormManager.getActualValue('reqId');
  if (reqId != null) {
    reqParam = {
      REQ_ID : reqId,
    };
  }
  var custNm = cmr.query('ADDR.GET.CUSTNM1.BY_REQID', reqParam);
  var city;
  var abbrevLocn = null;
  var abbrvNm = custNm.ret1;
  var cntryRegion = FormManager.getActualValue('countryUse');
  var mscenario = FormManager.getActualValue('custGrp');
  var scenario = null;
  if (mscenario == 'CROSS') {
    scenario = 'CROSS';
  } else if (mscenario == ((cntryRegion.substring(3, 5) + "CRO"))) {
    scenario = 'CROSS';
  }
  if (scenario == 'CROSS') {
    city = cmr.query('ADDR.GET.LANDCNTRY.BY_REQID', reqParam);
    abbrevLocn = getLandCntryDesc(city.ret1);
  } else {
    city = cmr.query('ADDR.GET.CITY1.BY_REQID', reqParam);
    abbrevLocn = city.ret1;
  }

  if (abbrvNm && abbrvNm.length > 22) {
    abbrvNm = abbrvNm.substring(0, 22);
  }

  if (abbrevLocn && abbrevLocn.length > 12) {
    abbrevLocn = abbrevLocn.substring(0, 12);
  }
  if (abbrevLocn != null) {
    FormManager.setValue('abbrevLocn', abbrevLocn);
  }
  if (abbrvNm != null) {
    FormManager.setValue('abbrevNm', abbrvNm);
  }
}

// CMR-1282
function setAbbrvNmLocFrProc() {
  var role = FormManager.getActualValue('userRole').toUpperCase();

  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  if (role != 'PROCESSOR') {
    return;
  }
  var reqId = FormManager.getActualValue('reqId');
  if (reqId != null) {
    reqParam = {
      REQ_ID : reqId,
    };
  }
  var custNm = cmr.query('DATA.GET.ABBREV_NM.BY_REQID', reqParam);
  var city;
  var abbrevLocn = null;
  var abbrvNm = custNm.ret1;
  var cntryRegion = FormManager.getActualValue('countryUse');
  var mscenario = FormManager.getActualValue('custGrp');
  var scenario = null;
  if (mscenario == 'CROSS') {
    scenario = 'CROSS';
  } else if (mscenario == ((cntryRegion.substring(3, 5) + "CRO"))) {
    scenario = 'CROSS';
  }
  if (scenario == 'CROSS') {
    city = cmr.query('ADDR.GET.LANDCNTRY.BY_REQID', reqParam);
    abbrevLocn = getLandCntryDesc(city.ret1);
  } else {
    city = cmr.query('ADDR.GET.CITY1.BY_REQID', reqParam);
    abbrevLocn = city.ret1;
  }

  if (abbrvNm && abbrvNm.length > 22) {
    abbrvNm = abbrvNm.substring(0, 22);
  }
  if (abbrevLocn && abbrevLocn.length > 12) {
    abbrevLocn = abbrevLocn.substring(0, 12);
  }

  if (abbrevLocn != null) {
    FormManager.setValue('abbrevLocn', abbrevLocn);
  }
  if (abbrvNm != null) {
    FormManager.setValue('abbrevNm', abbrvNm);
  }
}

function getLandCntryDesc(cntryCd) {
  if (cntryCd != null) {
    reqParam = {
      COUNTRY : cntryCd,
    };
  }
  var results = cmr.query('GET.CNTRY_DESC', reqParam);
  _cntryDesc = results.ret1;
  return _cntryDesc;
}

function validateNLCopy() {
  return null;
}

function updateAddrTypeList(cntry, addressMode, saving) {
  // hide 'KVK/VAT' selection for copy
  if ((addressMode == 'newAddress' || addressMode == 'copyAddress') && cmr.currentRequestType == 'C' || cmr.currentRequestType == 'U') {
    cmr.hideNode('radiocont_ZKVK');
    cmr.hideNode('radiocont_ZVAT');
  }
  if ((addressMode == 'newAddress' || addressMode == 'copyAddress') && (cmr.currentRequestType == 'C' || cmr.currentRequestType == 'U')) {
    var requestingLob = FormManager.getActualValue('requestingLob');
    var reqReason = FormManager.getActualValue('reqReason');
    if (requestingLob != 'IGF' || reqReason != 'IGF') {
      cmr.hideNode('radiocont_ZP02');
    } else {
      cmr.showNode('radiocont_ZP02');
    }

  }
}

/*
 * Mandatory addresses ZS01/ZP01/ZI01 *Billing (Sold-to) *Installing (Install
 * at) *Mailing - not flowing into RDC!!
 */
function addNLAddressTypeValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount == 0) {
          return new ValidationResult(null, false, 'Sold-to address is mandatory. Only one address for each address type should be defined when sending for processing.');
        }
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          var reqType = FormManager.getActualValue('reqType');
          var requestingLob = FormManager.getActualValue('requestingLob');
          var reqReason = FormManager.getActualValue('reqReason');
          var record = null;
          var type = null;
          var billToCnt = 0;
          var soldToCnt = 0;
          var shipToCnt = 0;
          var installAtCnt = 0;
          var igfCnt = 0;

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
              soldToCnt++;
            } else if (type == 'ZP01') {
              billToCnt++;
            } else if (type == 'ZD01') {
              shipToCnt++;
            } else if (type == 'ZI01') {
              installAtCnt++;
            } else if (type == 'ZP02') {
              igfCnt++;
            }
          }
          if (soldToCnt == 0) {
            return new ValidationResult(null, false, 'Sold-to Address is mandatory.');
          } else if (reqType == 'C' && billToCnt == 0) {
            return new ValidationResult(null, false, 'Bill-to Address is mandatory.');
          } else if (billToCnt > 1) {
            return new ValidationResult(null, false, 'Only one Bill-to address can be defined. Please remove the additional Bill-to address.');
          } else if (soldToCnt > 1) {
            return new ValidationResult(null, false, 'Only one Sold-to address can be defined. Please remove the additional Sold-to address.');
          } else if (igfCnt > 1) {
            return new ValidationResult(null, false, 'Only one IGF Billing address can be defined. Please remove the additional Sold-to address.');
          } else if (igfCnt == 0 && requestingLob == 'IGF' && reqReason == 'IGF') {
            return new ValidationResult(null, false, 'IGF Bill-to Address is mandatory when LOB is IGF and request reason is IGF.');
          } else {
            return new ValidationResult(null, true);
          }
        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function addNLVATValidator(cntry, tabName, formName, aType) {
  return function() {
    FormManager.addFormValidator((function() {
      var landCntry = cntry;
      var addrType = aType;
      return {
        validate : function() {
          var reqType = FormManager.getActualValue('reqType');
          var vat = FormManager.getActualValue('vat');

          if (!vat || vat == '' || vat.trim() == '') {
            return new ValidationResult(null, true);
          } else if (reqType == 'U' && vat == '@') {
            // vat deletion for updates
            return new ValidationResult(null, true);
          }

          var zs01Cntry = landCntry;

          var ret = cmr.query('VAT.GET_ZS01_CNTRY', {
            REQID : FormManager.getActualValue('reqId'),
            TYPE : addrType ? addrType : 'ZS01'
          });
          if (ret && ret.ret1 && ret.ret1 != '') {
            zs01Cntry = ret.ret1;
          } else {
            ret = cmr.query('VAT.GET_ZS01_CNTRY', {
              REQID : FormManager.getActualValue('reqId'),
              TYPE : 'ZS01'
            });
            if (ret && ret.ret1 && ret.ret1 != '') {
              zs01Cntry = ret.ret1;
            }
          }
          console.log('ZS01 VAT Country: ' + zs01Cntry);

          var result = cmr.validateVAT(zs01Cntry, vat);
          if (result && !result.success) {
            if (result.errorPattern == null) {
              return new ValidationResult({
                id : 'vat',
                type : 'text',
                name : 'vat'
              }, false, result.errorMessage + '.');
            } else {
              var msg = result.errorMessage + '. Format should be ' + result.errorPattern.formatReadable;
              return new ValidationResult({
                id : 'vat',
                type : 'text',
                name : 'vat'
              }, false, msg);
            }
          } else {
            return new ValidationResult(null, true);
          }
        }
      };
    })(), tabName, formName);
  };
}

function addAddressFieldValidators() {
  // Street and PO BOX for General Address
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var addrType = FormManager.getActualValue('addrType');
        var addrFldCnt = 0;
        var streetCont = 0;
        if (FormManager.getActualValue('poBox') != '') {
          addrFldCnt++;
        }
        if (FormManager.getActualValue('addrTxt') != '') {
          addrFldCnt++;
          streetCont++;
        }
        if (addrFldCnt < 2 && streetCont < 1 && addrType == 'ZS01') {
          return new ValidationResult(null, false, 'In General Address, Street is mandatory at all times. It can be filled along with PO BOX but PO BOX cannot be filled without Street.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');

  // Street and PO BOX
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var addrType = FormManager.getActualValue('addrType');
        var addrFldCnt = 0;
        if (FormManager.getActualValue('poBox') != '') {
          addrFldCnt++;
        }
        if (FormManager.getActualValue('addrTxt') != '') {
          addrFldCnt++;
        }
        if (addrFldCnt < 1 && addrType != 'ZS01') {
          return new ValidationResult(null, false, 'For Street Address and PO Box, atleast one should be filled.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');

  // only 1 out of 3 can be filled
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var custNm3 = FormManager.getActualValue('custNm3');
        var poBox = FormManager.getActualValue('poBox');
        var attPerson = FormManager.getActualValue('custNm4');
        var addrFldCnt1 = 0;
        if (custNm3 != '') {
          addrFldCnt1++;
        }
        if (poBox != '') {
          addrFldCnt1++;
        }
        if (attPerson != '') {
          addrFldCnt1++;
        }
        if (addrFldCnt1 > 1) {
          return new ValidationResult(null, false, 'Customer Name 3, Attention person and PO Box - only 1 out of 3 can be filled.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');

  // ALL NL POBOX
  FormManager.addFormValidator((function() {
    return {
      validate : function() {

        var cntry = FormManager.getActualValue('landCntry');
        if (cntry != '') {
          var POBox = FormManager.getActualValue('poBox');
          if (isNaN(POBox)) {
            return new ValidationResult(null, false, 'PO Box should be Numeric.');
          }

        }

        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');

  // post code + city
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var city = FormManager.getActualValue('city1');
        var postCd = FormManager.getActualValue('postCd');
        var val = postCd + ' ' + city;
        if (val.length > 30) {
          return new ValidationResult(null, false, 'Total computed length of City and Postal Code should not exceed 30 characters.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');

}

function getLandCntryDesc(cntryCd) {
  if (cntryCd != null) {
    reqParam = {
      COUNTRY : cntryCd,
    };
  }
  var results = cmr.query('GET.CNTRY_DESC', reqParam);
  _cntryDesc = results.ret1;
  return _cntryDesc;
}

function addCrossBorderValidatorNL() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqType = FormManager.getActualValue('reqType');
        var addrType = 'ZS01';
        var seqNo = '99901';
        var record = null;
        var type = null;
        var billingCnt = 0;
        // only Create type will be validated
        if (reqType != 'C') {
          return new ValidationResult(null, true);
        }

        var scenario = FormManager.getActualValue('custGrp');
        if (scenario != null && scenario.includes('CRO')) {
          scenario = 'CROSS';
        }

        var cntryRegion = FormManager.getActualValue('countryUse');
        var landCntry = 'NL'; // default to Netherlands
        if (cntryRegion != '' && cntryRegion.length > 3) {
          landCntry = cntryRegion.substring(3, 5);
        }

        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
            record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
            if (record == null && _allAddressData != null && _allAddressData[i] != null) {
              record = _allAddressData[i];
            }
            type = record.addrType;
            if (typeof (type) == 'object') {
              type = type[0];
            }
            if (type == 'ZP01') {
              billingCnt++;
            }
          }

          if (billingCnt > 0) {
            addrType = 'ZP01';
            seqNo = '29901';
          }
        }
        var reqId = FormManager.getActualValue('reqId');
        var defaultLandCntry = landCntry;
        var result = cmr.query('VALIDATOR.CROSSBORDERNL', {
          REQID : reqId,
          ADDRTYPE : addrType,
          SEQNO : seqNo
        });
        if (result != null && result.ret1 != '' && result.ret1 != undefined && defaultLandCntry != '' && result.ret1 != defaultLandCntry && scenario != 'CROSS' && billingCnt == 0) {
          return new ValidationResult(null, false, 'Landed Country value of the Sold-to (Main) Address should be \'' + defaultLandCntry + '\' for Non Cross-Border customers.');
        } else if (result != null && result.ret1 != '' && result.ret1 != undefined && defaultLandCntry != '' && result.ret1 == defaultLandCntry && scenario == 'CROSS' && billingCnt == 0) {
          return new ValidationResult(null, false, 'Landed Country value of the Sold-to (Main) Address should not be \'' + defaultLandCntry + '\' for Cross-Border customers.');
        } else if (result != null && result.ret1 != '' && result.ret1 != undefined && defaultLandCntry != '' && result.ret1 != defaultLandCntry && scenario != 'CROSS' && billingCnt > 0) {
          return new ValidationResult(null, false, 'Landed Country value of the Billing  Address should be \'' + defaultLandCntry + '\' for Non Cross-Border customers.');
        } else if (result != null && result.ret1 != '' && result.ret1 != undefined && defaultLandCntry != '' && result.ret1 == defaultLandCntry && scenario == 'CROSS' && billingCnt > 0) {
          return new ValidationResult(null, false, 'Landed Country value of the Billing  Address should not be \'' + defaultLandCntry + '\' for Cross-Border customers.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function hideCustPhone() {
  if (cmr.addressMode == 'newAddress' || cmr.addressMode == 'copyAddress') {
    for (var i = 0; i < _addrTypesForNL.length; i++) {
      _poBOXHandler[i] = null;
      if (_poBOXHandler[i] == null) {
        _poBOXHandler[i] = dojo.connect(FormManager.getField('addrType_' + _addrTypesForNL[i]), 'onClick', function(value) {
          setPhone();
          setPoBox();
        });
      }
      setPoBox();
    }
  }
  if (cmr.addressMode == 'updateAddress') {
    if (FormManager.getActualValue('addrType') == 'ZS01') {
      FormManager.enable('custPhone');
    } else {
      FormManager.disable('custPhone');
      FormManager.setValue('custPhone', '');

    }
    setPoBox();
  }
}
function setPhone() {
  if (FormManager.getField('addrType_ZS01').checked) {
    FormManager.enable('custPhone');
  } else {
    FormManager.disable('custPhone');
    FormManager.setValue('custPhone', '');
  }
}
function setPoBox() {
  if (FormManager.getField('addrType_ZS01').checked) {
    FormManager.disable('poBox');
    FormManager.setValue('poBox', '');
  } else {
    FormManager.enable('poBox');
  }
}

function setAddressDetailsForView() {
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');

  if (viewOnlyPage == 'true') {
    $('label[for="custNm1_view"]').text('Customer Name 1:');
    $('label[for="custNm2_view"]').text('Customer Name 2');
    $('label[for="custNm3_view"]').text('Customer Name 3');
    $('label[for="custNm4_view"]').text('Attention Person');
    $('label[for="landCntry_view"]').text('Country (Landed):');
    $('label[for="addrTxt_view"]').text('Street Address:');
    $('label[for="poBox_view"]').text('PO Box:');
    $('label[for="stateProv_view"]').text('State/Prov');
    $('label[for="postCd_view"]').text('Postal Code:');
  }
}

function addPhoneValidatorNL() {
  FormManager.addValidator('custPhone', Validators.DIGIT, [ 'Phone #' ]);
}

function setFieldsMandtOnSc() {
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (custSubGrp == 'COMME' || custSubGrp == 'NLDAT') {
    FormManager.addValidator('taxCd1', Validators.REQUIRED, [ 'Tax Code' ]);
  }
}

/**
 * Override WW validator
 */
function addCMRSearchValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqType = FormManager.getActualValue('reqType');
        var result = FormManager.getActualValue('findCmrResult');
        if (reqType == 'U') {
          if (result == '' || result.toUpperCase() == 'NOT DONE') {
            return new ValidationResult(null, false, 'CMR Search has not been performed yet.');
          }
        }
        if (reqType == 'U' && result.toUpperCase() != 'ACCEPTED') {
          return new ValidationResult(null, false, 'An existing CMR for update must be searched for and imported properly to the request.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_GENERAL_TAB', 'frmCMR');
}

function disbleCreateByModel() {
  var reqType = FormManager.getActualValue('reqType');
  if (reqType == 'C') {
    FormManager.disable('cmrSearchBtn');
    dojo.removeClass(dojo.byId('cmrSearchBtn'), 'ibm-btn-cancel-pri');
    dojo.addClass(dojo.byId('cmrSearchBtn'), 'ibm-btn-cancel-disabled');
    FormManager.disable('dnbSearchBtn');
    dojo.removeClass(dojo.byId('dnbSearchBtn'), 'ibm-btn-cancel-pri');
    dojo.addClass(dojo.byId('dnbSearchBtn'), 'ibm-btn-cancel-disabled');
  }
}

/**
 * Validator to check whether D&B search has been performed
 */
function addDnBSearchValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var ifProspect = FormManager.getActualValue('prospLegalInd');
        if (dijit.byId('prospLegalInd')) {
          ifProspect = dijit.byId('prospLegalInd').get('checked') ? 'Y' : 'N';
        }
        var reqType = FormManager.getActualValue('reqType');
        var result = FormManager.getActualValue('findDnbResult');
        var reqStatus = FormManager.getActualValue('reqStatus');
        if ((result == '' || result.toUpperCase() == 'NOT DONE') && reqType == 'C' && reqStatus == 'DRA' && ifProspect == 'Y') {
          return new ValidationResult(null, false, 'D&B Search has not been performed yet.');
        }
        if (FormManager.getActualValue('dnbPrimary') != 'Y') {
          return new ValidationResult(null, true);
        }
        if (reqType == 'U') {
          return new ValidationResult(null, true);
        }
        if (reqType == 'C') {
          return new ValidationResult(null, true);
        }
        if (reqStatus != 'DRA') {
          return new ValidationResult(null, true);
        }
        if (isSkipDnbMatching()) {
          return new ValidationResult(null, true);
        }
        if (result == '' || result.toUpperCase() == 'NOT DONE') {
          return new ValidationResult(null, false, 'D&B Search has not been performed yet.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_GENERAL_TAB', 'frmCMR');
}

function addCmrNoValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        var custSubType = FormManager.getActualValue('custSubGrp');
        var cmrNo = FormManager.getActualValue('cmrNo');
        if (FormManager.getActualValue('reqType') == 'U') {
          return new ValidationResult(null, true);
        }
        if (cmrNo != '' && cmrNo != null) {

          var isProspect = FormManager.getActualValue('prospLegalInd');
          if ('Y' == isProspect && cmrNo.startsWith('P') && cmrNo.length == 6) {
            return new ValidationResult(null, true);
          }

          if (cmrNo.length != 6) {
            return new ValidationResult(null, false, 'CMR Number should be exactly 6 digits long.');
          } else if (isNaN(cmrNo)) {
            return new ValidationResult(null, false, 'CMR Number should be only numbers.');
          } else if (cmrNo == "000000") {
            return new ValidationResult(null, false, 'CMR Number should not be 000000.');
          } else if (custSubType != '' && (custSubType.includes('INT') && (cmrNo < '990000' || cmrNo > '996999'))) {
            return new ValidationResult(null, false, 'for internal scenario the scope of CMR Number is 990000 ~ 996999');
          } else if (custSubType != '' && (custSubType.includes('ISO') && ((!cmrNo.startsWith('997')) && (!cmrNo.startsWith('998'))))) {
            return new ValidationResult(null, false, 'CMR Number should be in 997XXX or 998XXX format for internal SO scenario');
          } else if (custSubType != '' && !custSubType.includes('IN') && cmrNo.startsWith('99')) {
            return new ValidationResult(null, false, 'Non Internal CMR Number should not be in 99XXXX for scenarios');
          } else {
            var qParams = {
              CMRNO : cmrNo,
              CNTRY : cntry,
              MANDT : cmr.MANDT
            };
            var results = cmr.query('GET.CMR.ME', qParams);
            if (results.ret1 != null) {
              return new ValidationResult(null, false, 'The CMR Number already exists.');
            } else {
              results = cmr.query('LD.CHECK_EXISTING_CMR_NO_RESERVED', {
                COUNTRY : cntry,
                CMR_NO : cmrNo,
                MANDT : cmr.MANDT
              });
              if (results && results.ret1) {
                return new ValidationResult({
                  id : 'cmrNo',
                  type : 'text',
                  name : 'cmrNo'
                }, false, 'The requested CMR Number ' + cmrNo + ' already exists in the system.');
              }
            }
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function restrictDuplicateAddr(cntry, addressMode, saving, finalSave, force) {
  FormManager.addFormValidator(
      (function() {
        return {
          validate : function() {
            var reqReason = FormManager.getActualValue('reqReason');
            var addressType = FormManager.getActualValue('addrType');
            if (addressType == 'ZP02') {
              if (reqReason != 'IGF') {
                return new ValidationResult(null, false, 'Request Reason should be IGF.');
              }
            }
            var requestId = FormManager.getActualValue('reqId');
            var addressSeq = FormManager.getActualValue('addrSeq');
            var dummyseq = "xx";
            var showDuplicateIGFBillToError = false;
            var qParams;
            if (cmr.addressMode == 'updateAddress') {
              qParams = {
                REQ_ID : requestId,
                ADDR_SEQ : addressSeq,
                ADDR_TYPE : addressType
              };
            } else {
              qParams = {
                REQ_ID : requestId,
                ADDR_SEQ : dummyseq,
                ADDR_TYPE : addressType
              };
            }
            var result = cmr.query('GETADDRECORDSBYTYPE', qParams);
            var addCount = result.ret1;
            if (addressType != undefined && addressType != '' && addressType == 'ZP02' && cmr.addressMode != 'updateAddress') {
              showDuplicateIGFBillToError = Number(addCount) >= 1 && addressType == 'ZP02';
              if (showDuplicateIGFBillToError) {
                return new ValidationResult(null, false,
                    'Only one IGF Bill-To address is allowed. If you still want to create new address , please delete the existing one and then create a new address.');
              }
            }

            return new ValidationResult(null, true);
          }
        };
      })(), null, 'frmCMR_addressModal');
}

function streetValueFormatterBELUX(value, rowIndex) {
  var display = '';
  var rowData = this.grid.getItem(rowIndex);
  var custNm3 = rowData.custNm3;
  if (custNm3 && custNm3[0]) {
    display += ' ' + custNm3;
  }
  var custNm4 = rowData.custNm4;
  if (custNm4 && custNm4[0]) {
    display += (display ? '<br>' : '') + 'ATT ' + custNm4;
  }
  var poBox = rowData.poBox;
  if (poBox && poBox[0]) {
    display += (display ? '<br>' : '') + 'PO BOX ' + poBox;
  }
  return display;
}

function rdcDupZP01Check(cntry, addressMode, saving, finalSave, force) {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqType = FormManager.getActualValue('reqType');
        var addressType = FormManager.getActualValue('addrType');
        var addressSeq = FormManager.getActualValue('addrSeq');
        var cmrNo = FormManager.getActualValue('cmrNo');
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        if (cmr.currentRequestType == 'U' && addressType == 'ZP01' && (cmr.addressMode == 'newAddress' || cmr.addressMode == 'copyAddress')) {
          var qParams = {
            KATR6 : cntry,
            MANDT : cmr.MANDT,
            ZZKV_SEQNO : "29901",
            ZZKV_CUSNO : cmrNo,
            KTOKD : "ZP01"
          };
          var result = cmr.query('BENELUX.CHECK_RDC_ZP01', qParams);
          if (result != null && result.ret1 != null) {
            return new ValidationResult(null, false, 'Existing Bill-to address was identified in RDc and new one cannot be created, please contact CMR team for further assistence.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function rdcDupZP01CheckValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          var reqType = FormManager.getActualValue('reqType');
          var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
          var cmrNo = FormManager.getActualValue('cmrNo');
          var record = null;
          var type = null;
          var billToCnt = 0;
          var importInd = null;

          for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
            record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
            if (record == null && _allAddressData != null && _allAddressData[i] != null) {
              record = _allAddressData[i];
            }
            type = record.addrType;
            if (typeof (type) == 'object') {
              type = type[0];
            }
            if (type == 'ZP01') {
              billToCnt++;
              importInd = record.importInd;
              if (typeof (importInd) == 'object') {
                importInd = importInd[0];
              }
            }
          }
          if (billToCnt > 0 && importInd != 'Y' && reqType == 'U') {
            var qParams = {
              KATR6 : '788',
              MANDT : cmr.MANDT,
              ZZKV_SEQNO : "29901",
              ZZKV_CUSNO : cmrNo,
              KTOKD : "ZP01"
            };
            var result = cmr.query('BENELUX.CHECK_RDC_ZP01', qParams);
            if (result != null && result.ret1 != null) {
              return new ValidationResult(null, false, 'Existing Bill-to address was identified in RDc and new one cannot be created, please contact CMR team for further assistence.');
            }
          } else {
            return new ValidationResult(null, true);
          }
        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function addIGFZP02Validator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          var requestingLob = FormManager.getActualValue('requestingLob');
          var reqReason = FormManager.getActualValue('reqReason');
          var record = null;
          var type = null;
          var igfBillToCnt = 0;

          for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
            record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
            if (record == null && _allAddressData != null && _allAddressData[i] != null) {
              record = _allAddressData[i];
            }
            type = record.addrType;
            if (typeof (type) == 'object') {
              type = type[0];
            }
            if (type == 'ZP02') {
              igfBillToCnt++;
            }
          }
          if (igfBillToCnt > 0 && (requestingLob != 'IGF' || reqReason != 'IGF')) {
            return new ValidationResult(null, false, 'IGF Bill-to address is only available when Requesting LOB and Request Reason are IGF');
          } else {
            return new ValidationResult(null, true);
          }
        }
      }
    };
  })(), 'MAIN_GENERAL_TAB', 'frmCMR');
}

function addNlIsuHandler() {
  _ISUHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
    if (!value) {
      value = FormManager.getActualValue('isuCd');
    }
    reqType = FormManager.getActualValue('reqType');
    if (value == '32') {
      FormManager.setValue('clientTier', 'T');
    } else if (value == '34') {
      FormManager.setValue('clientTier', 'Q');
    } else if (value == '36') {
      FormManager.setValue('clientTier', 'Y');
    } else {
      FormManager.setValue('clientTier', '');
    }

    if (reqType == 'Processor') {
      FormManager.enable('commercialFinanced');
    }
  });
}

function setSORTLBasedOnIsuCtc() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true' || FormManager.getActualValue('reqType') == 'U') {
    return;
  }
  var isuCd = FormManager.getActualValue('isuCd');
  var clientTier = FormManager.getActualValue('clientTier');
  var reqType = FormManager.getActualValue('reqType');
  var subGrp = FormManager.getActualValue('custSubGrp');
  var nlSubList = [ 'INTER', 'IBMEM', 'PRICU' ];
  // before coverage
  /*
   * var isuList = [ '34', '5K', '15', '4A', '04', '28' ]; if (isuCd == '34' &&
   * clientTier == 'Y') { FormManager.setValue('commercialFinanced',
   * 'T0007969'); } else if (isuCd == '34' && clientTier == 'Q') {
   * FormManager.setValue('commercialFinanced', 'T0003611'); } else if (isuCd ==
   * '5K' && clientTier == '') { FormManager.setValue('commercialFinanced',
   * 'T0009067'); } else if (isuCd == '28' && clientTier == '') {
   * FormManager.setValue('commercialFinanced', 'I0000270'); } else if (isuCd ==
   * '15' && clientTier == '') { FormManager.setValue('commercialFinanced',
   * 'A0005229'); } else if (isuCd == '4A' && clientTier == '') {
   * FormManager.setValue('commercialFinanced', 'A0005236'); } else if (isuCd ==
   * '04' && clientTier == '') { FormManager.setValue('commercialFinanced',
   * 'I0000002'); } else { FormManager.setValue('commercialFinanced', ''); }
   */
  //
  var isuList = [ '21', '34', '36', '5K', '32', '28' ];
  if (isuCd == '34' && clientTier == 'Q') {
    FormManager.setValue('commercialFinanced', 'T0003611');
    if (role == 'Requester') {
      FormManager.readOnly('commercialFinanced');
      FormManager.readOnly('clientTier');
    }
    if (role == 'Processor') {
      FormManager.enable('isuCd');
      FormManager.enable('commercialFinanced');
      FormManager.enable('clientTier');
    }
  } else if (nlSubList.includes(subGrp)) {
    FormManager.setValue('commercialFinanced', '33U00');
  } else if (subGrp == 'BUSPR' || subGrp == 'CBBUS') {
    FormManager.setValue('commercialFinanced', 'P0000004');
  }
  /*
   * if (isuList.slice(2, 6).includes(isuCd)) {
   * FormManager.resetValidations('clientTier');
   * FormManager.setValue('clientTier', ''); FormManager.readOnly('clientTier'); }
   * else { FormManager.enable('clientTier'); }
   */
}

/*
 * function setClientTierValuesForUpdate() { var reqType =
 * FormManager.getActualValue('reqType'); if
 * (FormManager.getActualValue('viewOnlyPage') == 'true' || reqType != 'C' ||
 * FormManager.getActualValue('custSubGrp') == 'IBMEM') { return; } var isuList = [
 * '21', '5K', '28' ]; var isuCd = FormManager.getActualValue('isuCd'); if
 * (isuList.includes(isuCd)) { FormManager.setValue('clientTier', '');
 * FormManager.readOnly('clientTier'); } else {
 * FormManager.enable('clientTier'); } }
 */

function clientTierCodeValidator() {
  var isuCode = FormManager.getActualValue('isuCd');
  var clientTierCode = FormManager.getActualValue('clientTier');
  var reqType = FormManager.getActualValue('reqType');
  var reqType = FormManager.getActualValue('reqType');
  FormManager.removeValidator('clientTier', Validators.REQUIRED);
  var activeIsuCd = [ '32', '34', '36' ];
  var activeCtc = [ 'Q', 'Y', 'T' ];

  // if (((isuCode == '21' || isuCode == '8B' || isuCode == '5K') && reqType ==
  // 'C') || (isuCode != '34' && reqType == 'U')) {
  var activeIsuCd = [ '32', '34', '36', '21' ];
  var activeCtc = [ 'Q', 'Y', 'T' ];

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

      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier can only accept blank.');
    }
  } else if (isuCode == '34') {
    if (clientTierCode == 'Q') {
      return new ValidationResult(null, true);
    } else {
      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier can only accept \'Q\'.');
    }
  } else if (isuCode == '36') {
    if (clientTierCode == 'Y') {
      return new ValidationResult(null, true);
    } else {
      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier can only accept \'Y\'.');
    }
  } else if (isuCode == '32') {
    if (clientTierCode == 'T') {
      return new ValidationResult(null, true);
    } else {
      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier can only accept \'T\'.');
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
      }, false, 'Client Tier can only accept \'Q\', \'Y\' , \'T\' or blank.');
    }
  }
}
// CREATCMR-4293

function lockFields() {
  var reqTyp = FormManager.getActualValue('reqType');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var lockSubGrpList = [ 'CBBUS', 'BUSPR', 'INTER', 'IBMEM', 'PRICU' ];

  if (lockSubGrpList.includes(custSubGrp)) {
    FormManager.readOnly('commercialFinanced');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('clientTier');
  }
}

function sortlValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var commercialFinanced = FormManager.getActualValue('commercialFinanced');
        var isuCd = FormManager.getActualValue('isuCd');
        var clientTier = FormManager.getActualValue('clientTier');
        var reqType = FormManager.getActualValue('reqType');
        var valResult = null;
        var oldSearchTerm = null;
        var oldClientTier = null;
        var oldISU = null;
        var requestId = FormManager.getActualValue('reqId');

        if (reqType == 'C') {
          valResult = sortlCheckValidator();
        }
        return valResult;
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function sortlCheckValidator() {
  var reqTyp = FormManager.getActualValue('reqType');
  var isuCode = FormManager.getActualValue('isuCd');
  var clientTierCode = FormManager.getActualValue('clientTier');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var commercialFinanced = FormManager.getActualValue('commercialFinanced');
  var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  var countryUse = FormManager.getActualValue('countryUse');
  var isuCtc = isuCode.concat(clientTierCode);
  var subIndustry = FormManager.getActualValue('subIndustryCd');
  var ind = subIndustry.substring(0, 1);

  var scenariosToBlock = [ 'CBBUS', 'BUSPR', 'INTER', 'IBMEM', 'PRICU' ];

  var accSeq_788 = {
    '34Q' : [ 'T0003611' ],
    '36Y' : [ 'T0007969', 'T0010029', 'T0010540' ],
    '32T' : [ 'T0010435', 'T0010402', 'T0010437', 'T0010480', 'T0010401', 'T0010434' ],
    '5K' : [ 'T0009067' ],
    '21' : [ '33U00', 'P0000004' ],
    '28' : [ 'I0000270' ]
  };

  if (!scenariosToBlock.includes(custSubGrp) && isuCtc != '' && isuCtc != undefined && isuCtc != null) {
    if (cmrIssuingCntry == '788') {
      if (accSeq_788.hasOwnProperty(isuCtc) && !accSeq_788[isuCtc].includes(commercialFinanced)) {
        return new ValidationResult({
          id : 'commercialFinanced',
          type : 'text',
          name : 'commercialFinanced'
        }, false, 'SORTL can only accept ' + accSeq_788[isuCtc]);
      } else {
        return new ValidationResult(null, true);
      }
    }
  }
}

function addAfterConfigNl() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
    if (isuCd == '32') {
      FormManager.setValue('clientTier', 'T');
    } else if (isuCd == '34') {
      FormManager.setValue('clientTier', 'Q');
    } else if (isuCd == '36') {
      FormManager.setValue('clientTier', 'Y');
    } else {
      FormManager.setValue('clientTier', '');
    }
  });
}

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

function setPPSCEIDRequired() {
  var reqType = FormManager.getActualValue('reqType');
  var subGrp = FormManager.getActualValue('custSubGrp');
  if (reqType == 'U' || FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
 // if (subGrp.includes('BP') || subGrp.includes('BUS')) {
  if (subGrp=='BUSPR' || subGrp=='CBBUS') {  
 FormManager.enable('ppsceid');
    FormManager.addValidator('ppsceid', Validators.REQUIRED, [ 'PPS CEID' ], 'MAIN_IBM_TAB');
  } else {
    FormManager.clearValue('ppsceid');
    FormManager.readOnly('ppsceid');
    FormManager.removeValidator('ppsceid', Validators.REQUIRED);
  }
}
/*
 * CREATECMR-6379 NL - Economic Code based on Customer Sub Scenario Values
 */
function setEcoCodeBasedOnSubScenario() {
  var reqType = FormManager.getActualValue('reqType');
  var custSubScnrio = FormManager.getActualValue('custSubGrp');
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  if (custSubScnrio == '') {
    FormManager.setValue('economicCd', '');
  } else if (custSubScnrio == 'BUSPR' || custSubScnrio == 'CBBUS') {
    FormManager.setValue('economicCd', 'K49');
  } else if (custSubScnrio == 'PRICU' || custSubScnrio == 'CBPRI') {
    FormManager.setValue('economicCd', 'K60');
  } else if (custSubScnrio == 'INTER') {
    FormManager.setValue('economicCd', 'K81');
  } else if (custSubScnrio == 'IBMEM') {
    FormManager.setValue('economicCd', 'K71');
  } else {
    FormManager.setValue('economicCd', 'K11');
  }
}

function addVatIndValidator() {
  var _vatHandler = null;
  var _vatIndHandler = null;
  var vat = FormManager.getActualValue('vat');
  var vatInd = FormManager.getActualValue('vatInd');
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');

  if (viewOnlyPage == 'true') {
    FormManager.resetValidations('vat');
    FormManager.readOnly('vat');
  } else {

    var cntry = FormManager.getActualValue('cmrIssuingCntry');
    var results = cmr.query('GET_COUNTRY_VAT_SETTINGS', {
      ISSUING_CNTRY : cntry
    });

    if ((results != null || results != undefined || results.ret1 != '') && results.ret1 == 'O' && vat == '' && vatInd == '') {
      FormManager.removeValidator('vat', Validators.REQUIRED);
      FormManager.setValue('vatInd', 'N');
    } else if ((results != null || results != undefined || results.ret1 != '') && vat != '' && vatInd != 'E' && vatInd != 'N' && vatInd != '') {
      FormManager.setValue('vatInd', 'T');
      FormManager.enable('vatInd');
    // FormManager.readOnly('vatInd');
    } else if ((results != null || results != undefined || results.ret1 != '') && results.ret1 == 'R' && vat == '' && vatInd != 'E' && vatInd != 'N' && vatInd != 'T' && vatInd != '') {
      FormManager.setValue('vat', '');
      FormManager.setValue('vatInd', '');
    } else if (vat && dojo.string.trim(vat) != '' && vatInd != 'E' && vatInd != 'N' && vatInd != '') {
      FormManager.setValue('vatInd', 'T');
      FormManager.enable('vatInd');
      // FormManager.readOnly('vatInd');
    } else if (vat && dojo.string.trim(vat) == '' && vatInd != 'E' && vatInd != 'T' && vatInd != '') {
      FormManager.removeValidator('vat', Validators.REQUIRED);
      FormManager.setValue('vatInd', 'N');
    }

    if ((vat && dojo.string.trim(vat) == '') || (vat && dojo.string.trim(vat) == null) && vatInd == 'N') {
      FormManager.resetValidations('vat');
    }
  }
}

// CREATCMR-788
function addressQuotationValidatorNL() {
  FormManager.addValidator('abbrevNm', Validators.NO_QUOTATION, [ 'Abbreviated Name' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('abbrevLocn', Validators.NO_QUOTATION, [ 'Abbreviated Location' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('custNm1', Validators.NO_QUOTATION, [ 'Customer Name 1' ]);
  FormManager.addValidator('custNm2', Validators.NO_QUOTATION, [ 'Customer Name 2' ]);
  FormManager.addValidator('custNm3', Validators.NO_QUOTATION, [ 'Customer Name 3' ]);
  FormManager.addValidator('custNm4', Validators.NO_QUOTATION, [ 'Attention Person' ]);
  FormManager.addValidator('city1', Validators.NO_QUOTATION, [ 'City' ]);
  FormManager.addValidator('addrTxt', Validators.NO_QUOTATION, [ 'Street' ]);
  FormManager.addValidator('postCd', Validators.NO_QUOTATION, [ 'Postal Code' ]);
  FormManager.addValidator('poBox', Validators.NO_QUOTATION, [ 'PO Box' ]);
  FormManager.addValidator('custPhone', Validators.NO_QUOTATION, [ 'Phone #' ]);

}
dojo.addOnLoad(function() {
  GEOHandler.NL = [ '788' ];
  console.log('adding NETHERLANDS functions...');
  GEOHandler.enableCustomerNamesOnAddress(GEOHandler.NL);
  GEOHandler.enableCopyAddress(GEOHandler.NL, validateNLCopy, [ 'ZD01' ]);

  GEOHandler.addAfterConfig(lockEmbargo, GEOHandler.NL);
  GEOHandler.addAfterConfig(afterConfigForNL, GEOHandler.NL);
  GEOHandler.addAfterConfig(addHandlersForNL, GEOHandler.NL);
  GEOHandler.addAfterConfig(addHandlerForReqRsn, GEOHandler.NL);
  GEOHandler.addAfterConfig(setClientTierValues, GEOHandler.NL);
  GEOHandler.addAfterConfig(setAddressDetailsForView, GEOHandler.NL);
  // GEOHandler.addAfterConfig(disbleCreateByModel, GEOHandler.NL);
  // GEOHandler.addAfterConfig(setClientTierValuesForUpdate, GEOHandler.NL);
  GEOHandler.addAfterTemplateLoad(setPPSCEIDRequired, GEOHandler.NL);

  // GEOHandler.addAfterTemplateLoad(setClientTierValuesForUpdate,
  // GEOHandler.NL);
  GEOHandler.addAfterTemplateLoad(setAbbrvNmLoc, GEOHandler.NL);
  GEOHandler.addAfterTemplateLoad(setAbbrvNmLocFrProc, GEOHandler.NL);

  GEOHandler.addAfterTemplateLoad(afterConfigForNL, GEOHandler.NL);
  GEOHandler.addAfterTemplateLoad(setClientTierValues, GEOHandler.NL);
  // GEOHandler.addAfterTemplateLoad(setBOTeamValues, GEOHandler.NL);
  // GEOHandler.addAfterTemplateLoad(setINACValues, GEOHandler.NL);
  // GEOHandler.addAfterTemplateLoad(setEconomicCodeValues, GEOHandler.NL);
  GEOHandler.addAfterTemplateLoad(setFieldsMandtOnSc, GEOHandler.NL);

  GEOHandler.addAddrFunction(updateMainCustomerNames, GEOHandler.NL);
  GEOHandler.addAddrFunction(updateAddrTypeList, GEOHandler.NL);
  GEOHandler.addAddrFunction(addLandedCountryHandler, GEOHandler.NL);
  GEOHandler.addAddrFunction(hideCustPhone, GEOHandler.NL);
  GEOHandler.addAddrFunction(addPhoneValidatorNL, GEOHandler.NL);
  GEOHandler.addAddrFunction(disableLandCntry, GEOHandler.NL);

  GEOHandler.registerValidator(addKVKLengthValidator, GEOHandler.NL, null, true);
  GEOHandler.registerValidator(addCrossBorderValidatorNL, GEOHandler.NL, null, true);
  // GEOHandler.registerValidator(addAbrrevNameLengthValidator, GEOHandler.NL,
  // null, true);
  GEOHandler.registerValidator(checkForBnkruptcy, GEOHandler.NL, null, true);
  GEOHandler.registerValidator(addNLVATValidator('', 'MAIN_CUST_TAB', 'frmCMR', 'ZP01'), GEOHandler.NL, null, true);
  GEOHandler.registerValidator(addNLAddressTypeValidator, GEOHandler.NL, null, true);
  GEOHandler.registerValidator(addAddressFieldValidators, GEOHandler.NL, null, true);
  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.NL, GEOHandler.ROLE_PROCESSOR, true);
  GEOHandler.registerValidator(addCMRSearchValidator, GEOHandler.NL, null, true);
  GEOHandler.registerValidator(addCmrNoValidator, GEOHandler.NL, null, true);
  GEOHandler.registerValidator(restrictDuplicateAddr, GEOHandler.NL, null, true);
  GEOHandler.registerValidator(rdcDupZP01Check, GEOHandler.NL, null, true);
  GEOHandler.registerValidator(rdcDupZP01CheckValidator, GEOHandler.NL, null, true);
  GEOHandler.addAfterConfig(resetVATValidationsForPayGo, GEOHandler.NL);
  GEOHandler.addAfterTemplateLoad(resetVATValidationsForPayGo, GEOHandler.NL);
  // GEOHandler.addAfterConfig(resetVATValidationsForPayGo, GEOHandler.NL);
  // GEOHandler.addAfterTemplateLoad(resetVATValidationsForPayGo,
  // GEOHandler.NL);
  GEOHandler.registerValidator(addIGFZP02Validator, GEOHandler.NL, null, true);
  GEOHandler.registerValidator(checkCmrUpdateBeforeImport, GEOHandler.NL, null, true);
  GEOHandler.registerValidator(clientTierValidator, GEOHandler.NL, null, true);

  GEOHandler.registerValidator(addVatIndValidator, GEOHandler.NL, null, true);
  GEOHandler.addAfterConfig(setVatIndFieldsForGrp1AndNordx, GEOHandler.NL);
  GEOHandler.addAfterTemplateLoad(setVatIndFieldsForGrp1AndNordx, GEOHandler.NL);
  GEOHandler.registerValidator(sortlValidator, GEOHandler.NL, null, true);
  GEOHandler.addAfterConfig(addAfterConfigNl, GEOHandler.NL);
  GEOHandler.addAfterTemplateLoad(setSORTLBasedOnIsuCtc, GEOHandler.NL);
  GEOHandler.addAfterConfig(setSORTLBasedOnIsuCtc, GEOHandler.NL);
  GEOHandler.addAfterConfig(addNlIsuHandler, GEOHandler.NL);
  GEOHandler.addAfterTemplateLoad(lockFields, GEOHandler.NL);
  GEOHandler.addAfterConfig(lockFields, GEOHandler.NL);
});