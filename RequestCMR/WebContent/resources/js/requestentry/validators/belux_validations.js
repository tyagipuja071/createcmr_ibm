/* Register BELUX Javascripts */

var reqType = null;
var role = null;
var _reqReasonHandler = null;
function afterConfigForBELUX() {
  var cntryUse = FormManager.getActualValue('countryUse');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var reqType = FormManager.getActualValue('reqType');
  var custLang = FormManager.getActualValue('custPrefLang');
  FormManager.readOnly('cmrOwner');
  FormManager.readOnly('capInd');
  FormManager.readOnly('salesBusOffCd');
  FormManager.setValue('capInd', true);
  FormManager.resetValidations('enterprise');

  if ((custSubGrp.substring(2, 5) == 'LUINT' || custSubGrp == 'CBBUS' || custSubGrp.substring(2, 5) == 'PRI' || custSubGrp.substring(2, 5) == 'ISO' || custSubGrp.substring(2, 5) == 'BUS')) {
    FormManager.addValidator('inacCd', Validators.REQUIRED, [ 'INAC/NAC Code' ], 'MAIN_IBM_TAB');

  } else {
    FormManager.removeValidator('inacCd', Validators.REQUIRED);
  }

  if ((custSubGrp.substring(2, 5) == 'INT' || custSubGrp == 'CBBUS' || custSubGrp.substring(2, 5) == 'PRI' || custSubGrp.substring(2, 5) == 'ISO' || custSubGrp == 'BECOM' || custSubGrp == 'LUCOM')){
    FormManager.addValidator('isicCd', Validators.REQUIRED, [ 'ISIC' ], 'MAIN_CUST_TAB');
  } else {
    FormManager.removeValidator('isicCd', Validators.REQUIRED);
  }
  
  if ((custSubGrp.substring(2, 5) == 'INT' || custSubGrp == 'CBBUS' || custSubGrp.substring(2, 5) == 'PRI' || custSubGrp.substring(2, 5) == 'ISO')) {
    // FormManager.addValidator('inacCd', Validators.REQUIRED, [ 'INAC/NAC Code'
    // ], 'MAIN_IBM_TAB');
    FormManager.addValidator('isuCd', Validators.REQUIRED, [ 'ISU Code' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('clientTier', Validators.REQUIRED, [ 'Client Tier' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('collectionCd', Validators.REQUIRED, [ 'Collection Code' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('subIndustryCd', Validators.REQUIRED, [ 'Subindustry' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('economicCd', Validators.REQUIRED, [ 'Economic Code' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('searchTerm', Validators.REQUIRED, [ 'Account Team Number' ], 'MAIN_IBM_TAB');
  } else {
    // FormManager.removeValidator('inacCd', Validators.REQUIRED);
    FormManager.removeValidator('isuCd', Validators.REQUIRED);
    FormManager.removeValidator('clientTier', Validators.REQUIRED);
    FormManager.removeValidator('collectionCd', Validators.REQUIRED);
    FormManager.removeValidator('subIndustryCd', Validators.REQUIRED);
    FormManager.removeValidator('economicCd', Validators.REQUIRED);
    FormManager.removeValidator('searchTerm', Validators.REQUIRED);
  }

  // Creating Manadatory to Account Team Number

  /*
   * if(custSubGrp.substring(2,5) == 'INT' || custSubGrp == 'CBBUS' ||
   * custSubGrp.substring(2,5) == 'PRI' || custSubGrp.substring(2,5) == 'ISO' ||
   * custSubGrp.substring(2,5) == 'BUS'){ FormManager.addValidator('searchTerm',
   * Validators.REQUIRED, ['Account Team Number'], 'MAIN_IBM_TAB'); }else{
   * FormManager.removeValidator('searchTerm', Validators.REQUIRED); }
   */

  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }
  if ((custLang == null || custLang == '') && reqType == 'U') {
    FormManager.setValue('custPrefLang', 'V');
  }

  if (role == 'Processor') {
    FormManager.enable('abbrevNm');
    FormManager.enable('abbrevLocn');
    FormManager.addValidator('salesBusOffCd', Validators.REQUIRED, [ 'SBO' ], 'MAIN_IBM_TAB');

    // if (reqType != 'U')
    // FormManager.addValidator('searchTerm', Validators.REQUIRED,
    // [ 'Account Team Number' ], 'MAIN_IBM_TAB');
  } else {
    if (role == 'Requester' && reqType == 'C'){
    FormManager.readOnly('abbrevNm');
    FormManager.readOnly('abbrevLocn');
    }
    // FormManager.removeValidator('searchTerm', Validators.REQUIRED);
    FormManager.removeValidator('salesBusOffCd', Validators.REQUIRED);

  }
  if (reqType == 'C' && role == 'Processor') {
    FormManager.addValidator('abbrevNm', Validators.REQUIRED, [ 'Abbreviated Name' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('abbrevLocn', Validators.REQUIRED, [ 'Abbreviated Location' ], 'MAIN_CUST_TAB');
  }
  if (reqType == 'U') {
    FormManager.resetValidations('taxCd1');
  }
  if (reqType == 'C' && role != 'Processor') {
    FormManager.readOnly('collectionCd');
  }

  var landCntry = '';
  var cntryRegion = FormManager.getActualValue('countryUse');
  if (cntryRegion && cntryRegion != '' && cntryRegion.length > 3) {
    landCntry = cntryRegion.substring(3, 5);
  }
  if (landCntry != '') {
    FormManager.setValue('defaultLandedCountry', landCntry);
  }

  if ((custSubGrp == 'BEISO' || custSubGrp == 'LUISO') && role == 'Requester') {
    FormManager.addValidator('ibmDeptCostCenter', Validators.REQUIRED, [ 'Department Number' ], 'MAIN_CUST_TAB');
  } else {
    FormManager.removeValidator('ibmDeptCostCenter', Validators.REQUIRED);
  }
  if ((custSubGrp == 'CBCOM' || custSubGrp == 'CBBUS') && cntryUse == '624LU') {
    FormManager.setValue('taxCd1', '15');
  }

  setVatValidatorBELUX();
  addHandlerForReqRsn();
}

function disableLandCntry() {
  var custType = FormManager.getActualValue('custGrp');
  if ((custType == 'LOCAL' || custType.substring(2, 5) == 'LOC') && FormManager.getActualValue('addrType') == 'ZS01') {
    FormManager.readOnly('landCntry');
  } else {
    FormManager.enable('landCntry');
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

function addCrossBorderValidatorBELUX() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqType = FormManager.getActualValue('reqType');
        // only Create type will be validated
        if (reqType != 'C') {
          return new ValidationResult(null, true);
        }
        var reqId = FormManager.getActualValue('reqId');
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        var cntryRegion = FormManager.getActualValue('countryUse');
        var scenario = FormManager.getActualValue('custSubGrp');
        var mscenario = FormManager.getActualValue('custGrp');

        if (mscenario == 'CROSS') {
          scenario = 'CROSS';
        } else if (mscenario == ((cntryRegion.substring(3, 5) + "CRO"))) {
          scenario = 'CRO';
        }

        if (cntryRegion.length > cntry.length) {
          var defaultcntry = cntryRegion.substring(3, 5);
          var result = cmr.query('VALIDATOR.CROSSBORDER', {
            REQID : reqId
          });
          if (result != null && result.ret1 != '' && result.ret1 != undefined && defaultcntry != '' && result.ret1 != defaultcntry && scenario != 'CRO') {
            return new ValidationResult(null, false, 'Landed Country value of the Sold-to (Main) Address should be \'' + defaultcntry + '\' for Non Cross-Border customers.');
          } else if (result != null && result.ret1 != '' && result.ret1 != undefined && defaultcntry != '' && result.ret1 == defaultcntry && scenario == 'CRO') {
            return new ValidationResult(null, false, 'Landed Country value of the Sold-to (Main) Address should not be \'' + defaultcntry + '\' for Cross-Border customers.');
          }

        } else {
          var defaultLandCntry = FormManager.getActualValue('defaultLandedCountry');
          var result = cmr.query('VALIDATOR.CROSSBORDER', {
            REQID : reqId
          });
          if (result != null && result.ret1 != '' && result.ret1 != undefined && defaultLandCntry != '' && result.ret1 != defaultLandCntry && scenario != 'CROSS') {
            return new ValidationResult(null, false, 'Landed Country value of the Sold-to (Main) Address should be \'' + defaultLandCntry + '\' for Non Cross-Border customers.');
          } else if (result != null && result.ret1 != '' && result.ret1 != undefined && defaultLandCntry != '' && result.ret1 == defaultLandCntry && scenario == 'CROSS') {
            return new ValidationResult(null, false, 'Landed Country value of the Sold-to (Main) Address should not be \'' + defaultLandCntry + '\' for Cross-Border customers.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function setAbbrevNameLocn(cntry, addressMode, saving, finalSave, force) {
  if (typeof (_pagemodel) != 'undefined') {
    reqType = FormManager.getActualValue('reqType');
    role = _pagemodel.userRole;
  }
  if (reqType == 'U') {
    return;
  }
  if (role != 'Requester') {
    console.log("Processor, return");
    return;
  }
  if (finalSave || force || addressMode == 'ZS01') {
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
    var cmpnyName = FormManager.getActualValue('custNm1');
    var city1 = FormManager.getActualValue('city1');
    if (addrType == 'ZS01' || copyingToA) {
      FormManager.setValue('abbrevNm', cmpnyName);
      FormManager.setValue('abbrevLocn', city1);
    }
    FormManager.removeValidator('abbrevNm', Validators.REQUIRED);
    FormManager.removeValidator('abbrevLocn', Validators.REQUIRED);
  }
}

/**
 * Lock Embargo Code field
 */
function lockEmbargo() {
  reqType = FormManager.getActualValue('reqType');
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (role == 'REQUESTER' && reqType != 'U') {
    FormManager.readOnly('embargoCd');
  } else {
    FormManager.enable('embargoCd');
  }
}

/**
 * After config handlers
 */
var _ISUHandler = null;
var _ISICHandler = null;
var _CTCHandler = null;
var _IMSHandler = null;
var _vatExemptHandler = null;
var _ExpediteHandler = null;
var _AccountTeamHandler = null;

function addHandlersForBELUX() {

  if (_ISUHandler == null) {
    _ISUHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
      setClientTierValues(value);
    });
  }

  if (_CTCHandler == null) {
    _CTCHandler = dojo.connect(FormManager.getField('clientTier'), 'onChange', function(value) {
      setAccountTeamNumberValues(value);
    });
  }

  if (_AccountTeamHandler == null) {
    _AccountTeamHandler = dojo.connect(FormManager.getField('searchTerm'), 'onChange', function(value) {
      // setINACValues(value);
      setEconomicCodeValues(value);
      setSBO();

    });
  }

  if (_ISICHandler == null) {
    _ISICHandler = dojo.connect(FormManager.getField('isicCd'), 'onChange', function(value) {
      setCollectionCodeValues(value);
    });
  }

  if (_vatExemptHandler == null) {
    _vatExemptHandler = dojo.connect(FormManager.getField('vatExempt'), 'onClick', function(value) {
      setVatValidatorBELUX();
    });
  }
  if (_IMSHandler == null && FormManager.getActualValue('cmrIssuingCntry')) {
    _IMSHandler = dojo.connect(FormManager.getField('subIndustryCd'), 'onChange', function(value) {
      setAccountTeamNumberValues();
    });
  }
  if (_ExpediteHandler == null) {
    _ExpediteHandler = dojo.connect(FormManager.getField('expediteInd'), 'onChange', function(value) {
      setExpediteReason();
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

/* Vat Handler */
function setVatValidatorBELUX() {
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  if (viewOnlyPage != 'true' && FormManager.getActualValue('reqType') == 'C') {
    FormManager.resetValidations('vat');
    if (!dijit.byId('vatExempt').get('checked')) {
      checkAndAddValidator('vat', Validators.REQUIRED, [ 'VAT' ]);
    }
  }
}

/**
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
  var clientTiers = [];
  if (isuCd != '') {
    var qParams = {
      _qall : 'Y',
      ISSUING_CNTRY : cntry,
      ISU : '%' + isuCd + '%'
    };
    var results = cmr.query('GET.CTCLIST.BYISU', qParams);
    if (results != null) {
      for ( var i = 0; i < results.length; i++) {
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

/*
 * Set Account Team Number Values based on isuCTC
 */
function setAccountTeamNumberValues(clientTier) {
  
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }

  var clientTier = FormManager.getActualValue('clientTier');
  var isuCd = FormManager.getActualValue('isuCd');
  var ims = FormManager.getActualValue('subIndustryCd');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var geoCd = FormManager.getActualValue('countryUse').substring(3, 5);

  var accountTeamNumber = [];
  if (isuCd != '') {
    var isuCtc = isuCd + clientTier;
    var qParams = null;
    var results = null;

    // Account Team Number will be based on IMS for 32S
    if (ims != '' && ims.length > 1 && (isuCtc == '32S')) {
      qParams = {
        _qall : 'Y',
        ISSUING_CNTRY : cntry + geoCd,
        ISU : '%' + isuCd + clientTier + '%',
        CLIENT_TIER : '%' + ims.substring(0, 1) + '%'
      };
      results = cmr.query('GET.SRLIST.BYISUCTC', qParams);
    } else {
      qParams = {
        _qall : 'Y',
        ISSUING_CNTRY : cntry + geoCd,
        ISU : '%' + isuCd + clientTier + '%'
      };
      results = cmr.query('GET.SRLIST.BYISU', qParams);
    }

    if (results != null) {
      for ( var i = 0; i < results.length; i++) {
        accountTeamNumber.push(results[i].ret1);
      }
      FormManager.limitDropdownValues(FormManager.getField('searchTerm'), accountTeamNumber);
      if (accountTeamNumber.length == 1) {
        FormManager.setValue('searchTerm', accountTeamNumber[0]);
      }
    }

    var custGrp = FormManager.getActualValue('custGrp');
    var custSubGrp = FormManager.getActualValue('custSubGrp');
    var role = FormManager.getActualValue('userRole').toUpperCase();
    
    if (role == 'PROCESSOR'){
      FormManager.enable('searchTerm');
    } else if ((geoCd == 'LU') && (custSubGrp == 'CBBUS')) {
      FormManager.setValue('searchTerm', 'LP0000');
      FormManager.readOnly('searchTerm');
    } else if ((custGrp == 'CROSS') && (custSubGrp == 'CBBUS')) {
      FormManager.readOnly('searchTerm');
    } else if ((custSubGrp == 'BEBUS') || (custSubGrp == 'LUBUS')) {
      FormManager.readOnly('searchTerm');
    } else if ((custSubGrp == 'LUBUS')){
      FormManager.readOnly('searchTerm');
    } else if ((custSubGrp == 'BEINT') || (custSubGrp == 'BEISO') || (custSubGrp == 'BEPRI') || 
        (custSubGrp == 'LUINT') || (custSubGrp == 'LUPRI') || (custSubGrp == 'LUISO')) {
      FormManager.readOnly('searchTerm');
    } else {
      FormManager.enable('searchTerm');
    }
  }
}

/*
 * NL - INAC Code based on AccountTeamNumber Values
 */
function setINACValues(searchTerm) {

  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }

  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  searchTerm = FormManager.getActualValue('searchTerm');

  var inacCode = [];
  if (searchTerm != '') {
    var qParams = {
      _qall : 'Y',
      ISSUING_CNTRY : cntry,
      SEARCHTERM : '%' + searchTerm + '%'
    };
    var results = cmr.query('GET.INACLIST.BYST', qParams);
    if (results != null) {
      for ( var i = 0; i < results.length; i++) {
        inacCode.push(results[i].ret1);
      }
      if (inacCode != null) {
        FormManager.limitDropdownValues(FormManager.getField('inacCd'), inacCode);
        if (inacCode.length == 1) {
          FormManager.setValue('inacCd', inacCode[0]);
        }
       /* if (inacCode.length == 0) {
          FormManager.setValue('inacCd', '');
        }*/
      }
    }
  }
}

/*
 * NL - EconomicCode based on AccountTeamNumber Values
 */
function setEconomicCodeValues(searchTerm) {
  
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }

  var role = _pagemodel.userRole;
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  searchTerm = FormManager.getActualValue('searchTerm');
  var geoCd = FormManager.getActualValue('countryUse').substring(3, 5);
  var custSubGrp = FormManager.getActualValue('custSubGrp').substring(2, 5);
  var custSGrp = FormManager.getActualValue('custSubGrp');

  var economicCode = [];
  if (searchTerm != '' && custSubGrp != 'ISO' && custSGrp != 'CBBUS') {
    var qParams = {
      _qall : 'Y',
      ISSUING_CNTRY : cntry + geoCd,
      REP_TEAM_CD : '%' + searchTerm + '%'
    };
    var results = cmr.query('GET.ECONOMICLIST.BYST', qParams);
    if (results != null) {
      for ( var i = 0; i < results.length; i++) {
        economicCode.push(results[i].ret1);
      }
      if (economicCode != null) {
        FormManager.limitDropdownValues(FormManager.getField('economicCd'), economicCode);
        if (economicCode.length == 1) {
          FormManager.setValue('economicCd', economicCode[0]);
        }
        if (economicCode.length == 0) {
          FormManager.setValue('economicCd', '');
        }
      }
    }
  }
  if (custSubGrp == 'PUB') {
    FormManager.setValue('economicCd', 'K11');
  } else if (custSGrp == 'CBBUS') {
    FormManager.setValue('economicCd', 'K49');
    if (role == 'Requester')
      FormManager.readOnly('economicCd');
  }
}

/*
 * Set Collection Code Values based On ISIC
 */
function setCollectionCodeValues(isicCd) {

  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var geoCd = FormManager.getActualValue('countryUse').substring(3, 5);
  var isicCd = FormManager.getActualValue('isicCd');

  var collectionCode = [];

  if (isicCd != '') {
    var qParams = {
      _qall : 'Y',
      ISSUING_CNTRY : cntry + geoCd,
      REP_TEAM_CD : '%' + isicCd + '%'
    };
    var results = cmr.query('GET.CCLIST.BYISIC', qParams);
    if (results != null) {
      for ( var i = 0; i < results.length; i++) {
        collectionCode.push(results[i].ret1);
      }
      if (collectionCode != null) {
        FormManager.limitDropdownValues(FormManager.getField('collectionCd'), collectionCode);
        if (collectionCode.length == 1) {
          FormManager.setValue('collectionCd', collectionCode[0]);
        }
      }
    }
  }
}

/*
 * Set Preferred Language for 624LU
 */
function setPreferredLanguage() {

  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  FormManager.enable('custPrefLang');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var geoCd = FormManager.getActualValue('countryUse').substring(3, 5);

  if (cntry == '624' && geoCd == 'LU') {
    FormManager.limitDropdownValues(FormManager.getField('custPrefLang'), [ 'F', 'V' ]);
  }
}

function addAbbrevLocnLengthValidator() {
  reqType = FormManager.getActualValue('reqType');
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var _abbrevLocn = FormManager.getActualValue('abbrevLocn');
        if (reqType != 'U' && role == 'Requester' && _abbrevLocn.length != 12) {
          return new ValidationResult({
            id : 'abbrevLocn',
            type : 'text',
            name : 'abbrevLocn'
          }, false, 'The length of Abbreviated Location should be 12 characters.');
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function addAbbrevNameLengthValidator() {
  reqType = FormManager.getActualValue('reqType');
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var _abbrevName = FormManager.getActualValue('abbrevNm');
        if (reqType != 'U' && role == 'Requester' && _abbrevName.length != 22) {
          return new ValidationResult({
            id : 'abbrevNm',
            type : 'text',
            name : 'abbrevNm'
          }, false, 'The length of Abbreviated Name should be 22 characters.');
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
    FormManager.setValue('collectionCd', 'TCBE99');
  }
}

function addHandlerForReqRsn() {
  if (_reqReasonHandler == null) {
    _reqReasonHandler = dojo.connect(FormManager.getField('reqReason'), 'onChange', function(value) {
      chngesAfterBnkrptcy();
    });
  }
}

function setINACfrLux() {
  var custSubScnrio = FormManager.getActualValue('custSubGrp');
  var geoCd = FormManager.getActualValue('countryUse').substring(3, 5);
  if (custSubScnrio == 'CBBUS' && geoCd == 'LU') {
    FormManager.setValue('inacCd', 'LP98');
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

/*
 * Mandatory addresses ZS01/ZP01/ZI01 *Billing (Sold-to) *Installing (Install
 * at) *Mailing - not flowing into RDC!!
 */
function addBELUXAddressTypeValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount == 0) {
          return new ValidationResult(null, false, 'Mailing, Installing address are mandatory. Only one address for each address type should be defined when sending for processing.');
        }
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          var record = null;
          var type = null;
          var installingCnt = 0;
          var billingCnt = 0;
          var mailingCnt = 0;
          var shippingCnt = 0;
          var eplCnt = 0;

          for ( var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
            record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
            if (record == null && _allAddressData != null && _allAddressData[i] != null) {
              record = _allAddressData[i];
            }
            type = record.addrType;
            if (typeof (type) == 'object') {
              type = type[0];
            }
            if (type == 'ZI01') {
              mailingCnt++;
            } else if (type == 'ZP01') {
              billingCnt++;
            } else if (type == 'ZS01') {
              installingCnt++;
            } else if (type == 'ZD01') {
              shippingCnt++;
            } else if (type == 'ZS02') {
              eplCnt++;
            }
          }
          if (installingCnt == 0 || mailingCnt == 0) {
            return new ValidationResult(null, false, 'Mailing, installing address are mandatory.');
          } else if (installingCnt > 1) {
            return new ValidationResult(null, false, 'Only one Installing address can be defined. Please remove the additional Installing address.');
          } else if (billingCnt > 1) {
            return new ValidationResult(null, false, 'Only one Billing address can be defined. Please remove the additional Billing address.');
          } else if (mailingCnt > 1) {
            return new ValidationResult(null, false, 'Only one Mailing address can be defined. Please remove the additional Mailing address.');
          } else if (eplCnt > 1) {
            return new ValidationResult(null, false, 'Only one Software address can be defined. Please remove the additional Software address.');
          } else {
            return new ValidationResult(null, true);
          }
        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function addAddressFieldValidators() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        var addrTxt = FormManager.getActualValue('addrTxt');
        var addrTxt2 = FormManager.getActualValue('addrTxt2');
        var poBox = FormManager.getActualValue('poBox');

        var addrFldCnt = 0;
        var computedLength = addrTxt;
        if (FormManager.getActualValue('poBox') != '') {
          addrFldCnt++;
          computedLength += poBox;
        }
        if (FormManager.getActualValue('addrTxt') != '') {
          addrFldCnt++;
        }
        
        if (FormManager.getActualValue('addrTxt2') != '') {
          addrFldCnt++;
          computedLength += addrTxt2;
        }
        
        if(addrFldCnt ==1){
          if( poBox != null && poBox != '' && poBox.length > 23){
              return new ValidationResult(null, false, 'PostBOX should not exceed 23 characters.');
          }else if( addrTxt != null && addrTxt != '' && addrTxt.length > 30){
            return new ValidationResult(null, false, 'Street should not exceed 30 characters.');
          }else if( addrTxt2 != null && addrTxt2 != '' && addrTxt2.length > 30){
            return new ValidationResult(null, false, 'Street no. should not exceed 30 characters.');
          }
        }else if(addrFldCnt ==2){
          if( poBox != null && poBox != ''){
            if (addrTxt != null && addrTxt != '' && computedLength.length > 22) {
              return new ValidationResult(null, false, 'Total computed length of Street and PostBOX should not exceed 22 characters.');
            }else if(addrTxt2 != null && addrTxt2 != '' && computedLength.length > 22){
              return new ValidationResult(null, false, 'Total computed length of Street no. and PostBOX should not exceed 22 characters.');
            }
          }else{
            if(computedLength.length > 29)
              return new ValidationResult(null, false, 'Total computed length of Street and Street no. should not exceed 29 characters.');
          }
        }else if(addrFldCnt ==3){
          if (computedLength.length > 21) {
            return new ValidationResult(null, false, 'Total computed length of Street, Street no. and PostBOX should not exceed 21 characters.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');

  // Street and PO BOX
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        // if (FormManager.getActualValue('reqType') != 'C') {
        // return;
        // }
        var addrFldCnt1 = 0;
        if (FormManager.getActualValue('poBox') != '') {
          addrFldCnt1++;
        }
        if (FormManager.getActualValue('addrTxt') != '') {
          addrFldCnt1++;
        }
        if (addrFldCnt1 < 1) {
          return new ValidationResult(null, false, 'For Street and PostBox, atleast one should be filled.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');

  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var title = FormManager.getActualValue('dept');
        var firstName = FormManager.getActualValue('custNm3');
        var lastName = FormManager.getActualValue('custNm4');

        var val = title;
        if (firstName != '') {
          val += firstName;
          if (lastName != '') {
            val += lastName;

            if (val.length > 30) {
              return new ValidationResult(null, false, 'Total computed length of Title, First Name and Last Name should not exceed 30 characters.');
            }
          } else {
            if (val.length > 30) {
              return new ValidationResult(null, false, 'Total computed length of Title and First Name should not exceed 30 characters.');
            }
          }
        } else {
          if (val.length > 30) {
            return new ValidationResult(null, false, 'Title should not exceed 30 characters.');
          }
        }

        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');

  // only 3 out of 4 can be filled
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var street = FormManager.getActualValue('addrTxt');
        var nameCont = FormManager.getActualValue('custNm2');
        var poBox = FormManager.getActualValue('poBox');
        var title = FormManager.getActualValue('dept');
        var firstName = FormManager.getActualValue('custNm3');
        var lastName = FormManager.getActualValue('custNm4');
        var addrFldCnt = 0;
        if (street != '') {
          addrFldCnt++;
        }
        if (nameCont != '') {
          addrFldCnt++;
        }
        if (poBox != '') {
          addrFldCnt++;
        }
        if (title != '' || firstName != '' || lastName != '') {
          addrFldCnt++;
        }
        if (addrFldCnt > 3) {
          return new ValidationResult(null, false, ' Street, PostBox, Attention person (Title, First name, Last name) and Name Con\'t - only 3 out of 4 can be filled.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');

  // ALL BELUX POBOX
  FormManager.addFormValidator((function() {
    return {
      validate : function() {

        var cntry = FormManager.getActualValue('landCntry');
        if (cntry != '') {
          var POBox = FormManager.getActualValue('poBox');
          if (isNaN(POBox)) {
            return new ValidationResult(null, false, 'PostBox should be Numeric.');
          }

        }

        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');

  // Street mandatory for Mailing & Billing
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var addrType = FormManager.getActualValue('addrType');
        var cntry = FormManager.getActualValue('landCntry');
        if (cntry != '' && (addrType == 'ZI01' || addrType == 'ZP01')) {
          var street = FormManager.getActualValue('addrTxt');
          if (street == null || street == '') {
            return new ValidationResult(null, false, 'Street is mandatory for Mailing & Billing even when PO BOX is filled');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function setVatInfoBubble() {
  var reqId = FormManager.getActualValue('reqId');
  var vatCntry = null;
  var infoText = "";
  if (reqId != null) {
    reqParam = {
      REQ_ID : reqId,
    };
    var landCntry = null;
    landCntry = FormManager.getActualValue('countryUse');
    if (landCntry != null) {
      vatCntry = landCntry;
      switch (vatCntry) {
      case "624LU":
        infoText = "For Local customers, insert country prefix in front of the VAT number (LU12345678)";
        break;
      default:
        infoText = "For Local customers, insert country prefix in front of the VAT number (BE0123456789)";
      }

    }

    document.getElementById('vatInfoBubble').getElementsByClassName('cmr-info-bubble')[0].setAttribute('title', infoText);

  }
  return;
}

function setAbbrvNmLoc() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var reqParam = null;

  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  // if (role != 'REQUESTER') {
  // return;
  // }
  var reqId = FormManager.getActualValue('reqId');
  if (reqId != null) {
    reqParam = {
      REQ_ID : reqId
    };
  }
  var custNm = cmr.query('ADDR.GET.CUSTNM1.BY_REQID', reqParam);
  var city;
  var abbrevLocn = null;
  var abbrvNm = custNm.ret1;
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var custGrp = FormManager.getActualValue('custGrp');

  if (custSubGrp == 'CBCOM' || custGrp == 'LOCAL' || custGrp.substring(2,5) == 'LOC') {
    city = cmr.query('ADDR.GET.CITY1.BY_REQID', reqParam);
    abbrevLocn = city.ret1;
  } else {
    city = cmr.query('ADDR.GET.LANDCNTRY.BY_REQID', reqParam);
    abbrevLocn = getLandCntryDesc(city.ret1);
  }

  if (abbrvNm && abbrvNm.length > 22) {
    abbrvNm = abbrvNm.substring(0, 22);
  }
  if (abbrevLocn && abbrevLocn.length > 12) {
    abbrevLocn = abbrevLocn.substring(0, 12);
  }

  if (abbrevLocn != null && custSubGrp.substring(2, 5) != 'SOF') {
    FormManager.setValue('abbrevLocn', abbrevLocn);
  }
  if (abbrvNm != null) {
    FormManager.setValue('abbrevNm', abbrvNm);
  }
}

function addBELUXVATValidator(cntry, tabName, formName, aType) {
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
          console.log('VAT Country: ' + zs01Cntry);

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
function addPhoneValidatorBELUX() {
  FormManager.addValidator('custPhone', Validators.DIGIT, [ 'Phone #' ]);
}

function validateBELUXCopy(addrType, arrayOfTargetTypes) {
  return null;
}

function setSBO(searchTerm) {
  accTeam = document.getElementById('searchTerm').value;
  if (!accTeam || accTeam == '') {
    return;
  }
  if (FormManager.getActualValue('countryUse') == '624') {
    FormManager.setValue('salesBusOffCd', '0'.concat(accTeam.substring(0, 2) + '0000'));
  } else if (FormManager.getActualValue('countryUse') == '624LU') {
    FormManager.setValue('salesBusOffCd', '0'.concat(accTeam.substring(0, 2) + '0001'));
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

function setAddressDetailsForView() {
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');

  if (viewOnlyPage == 'true') {
    $('label[for="custNm1_view"]').text('Customer Name:');
    $('label[for="dept_view"]').text('Title:');
    $('label[for="custNm3_view"]').text('First Name');
    $('label[for="custNm4_view"]').text('Last Name');
    $('label[for="landCntry_view"]').text('Country (Landed):');
    $('label[for="addrTxt_view"]').text('Street:');
    $('label[for="custNm2_view"]').text('Name Con' + '\'' + 't:');
    $('label[for="addrTxt2_view"]').text('Street No.:');
    $('label[for="poBox_view"]').text('PostBox:');
  }
}

function streetValueFormatterBELUX(value, rowIndex) {
  var display = value ? value : '';
  var rowData = this.grid.getItem(rowIndex);
  var streetCont = rowData.addrTxt2;
  if (streetCont && streetCont[0]) {
    display += ' ' + streetCont;
  }
  var poBox = rowData.poBox;
  if (poBox && poBox[0]) {
    display += (display ? '<br>' : '') + 'PO BOX ' + poBox;
  }
  return display;
}

function attnFormatterBELUX(value, rowIndex) {
  var rowData = this.grid.getItem(rowIndex);
  var title = rowData.dept[0] ? rowData.dept[0] : '';
  var firstName = rowData.custNm3[0] ? rowData.custNm3[0] : '';
  var lastName = rowData.custNm4[0] ? rowData.custNm4[0] : '';
  return title + (firstName ? ' ' + firstName : '') + (lastName ? ' ' + lastName : '');
}

dojo.addOnLoad(function() {
  GEOHandler.BELUX = [ '624' ];

  console.log('adding BELUX functions...');
  GEOHandler.setRevertIsicBehavior(false);
  GEOHandler.enableCustomerNamesOnAddress(GEOHandler.BELUX);
  GEOHandler.addAddrFunction(updateMainCustomerNames, GEOHandler.BELUX);
  GEOHandler.addAfterConfig(addHandlersForBELUX, GEOHandler.BELUX);
  GEOHandler.addAfterTemplateLoad(setAccountTeamNumberValues, GEOHandler.BELUX);
  GEOHandler.addAfterTemplateLoad(addHandlersForBELUX, GEOHandler.BELUX);
  GEOHandler.addAfterConfig(setCollectionCodeValues, GEOHandler.BELUX);
  GEOHandler.addAfterTemplateLoad(setCollectionCodeValues, GEOHandler.BELUX);
  GEOHandler.addAfterConfig(afterConfigForBELUX, GEOHandler.BELUX);
  GEOHandler.addAfterTemplateLoad(afterConfigForBELUX, GEOHandler.BELUX);
  // GEOHandler.addAfterConfig(setAbbrvNmLoc, GEOHandler.BELUX);
  GEOHandler.addAfterTemplateLoad(setAbbrvNmLoc, GEOHandler.BELUX);
  // GEOHandler.registerValidator(addAbbrevLocnLengthValidator,GEOHandler.BELUX,
  // null, true);
  // GEOHandler.registerValidator(addAbbrevNameLengthValidator,GEOHandler.BELUX,
  // null, true);
  GEOHandler.registerValidator(checkForBnkruptcy, GEOHandler.BELUX, null, true);
  // GEOHandler.addAfterConfig(addHandlerForReqRsn, GEOHandler.BELUX);
  GEOHandler.addAfterTemplateLoad(setINACfrLux, GEOHandler.BELUX);
  GEOHandler.addAfterConfig(setPreferredLanguage, GEOHandler.BELUX);
  GEOHandler.addAfterTemplateLoad(setPreferredLanguage, GEOHandler.BELUX);
  // GEOHandler.addAfterTemplateLoad(setINACValues, GEOHandler.BELUX);
  GEOHandler.addAfterTemplateLoad(setEconomicCodeValues, GEOHandler.BELUX);
  GEOHandler.addAfterConfig(lockEmbargo, GEOHandler.BELUX);
  GEOHandler.addAfterConfig(setClientTierValues, GEOHandler.BELUX);
  GEOHandler.addAfterTemplateLoad(setClientTierValues, GEOHandler.BELUX);
  GEOHandler.registerValidator(addBELUXVATValidator('', 'MAIN_CUST_TAB', 'frmCMR', 'ZP01'), GEOHandler.BELUX, null, true);
  GEOHandler.registerValidator(addAddressFieldValidators, GEOHandler.BELUX, null, true);
  GEOHandler.enableCopyAddress(GEOHandler.BELUX, validateBELUXCopy, [ 'ZD01' ]);
  GEOHandler.registerValidator(addCrossBorderValidatorBELUX, GEOHandler.BELUX, null, true);
  GEOHandler.registerValidator(addBELUXAddressTypeValidator, GEOHandler.BELUX, null, true);
  GEOHandler.addAfterConfig(setVatInfoBubble, GEOHandler.BELUX);
  GEOHandler.addAddrFunction(disableLandCntry, GEOHandler.BELUX);
  GEOHandler.addAddrFunction(addLandedCountryHandler, GEOHandler.BELUX);
  GEOHandler.addAddrFunction(addPhoneValidatorBELUX, GEOHandler.BELUX);
  GEOHandler.addAfterConfig(setAddressDetailsForView, GEOHandler.BELUX);
  /* 1596058: All BeLux - DPL check failed */
  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.BELUX, GEOHandler.ROLE_PROCESSOR, true);

});