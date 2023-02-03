/**
 * 
 */
/* Register FR Javascripts */

// var _addrTypesForFR = [ 'ZP01', 'ZS01', 'ZI01', 'ZD01', 'ZS02' ];
var _addrTypesForFR = [ 'ZP01', 'ZS01', 'ZI01', 'ZD01' ];
var _poBOXHandler = [];
var EU_COUNTRIES = [ "AT", "BE", "BG", "HR", "CY", "CZ", "DE", "DK", "EE", "GR", "FI", "FR", "GB", "HU", "IE", "IT", "LT", "LU", "LV", "MT", "NL", "PL", "PT", "RO", "SE", "SI", "SK" ];
var VAT_EU_COUNTRY = [ "AT", "BE", "BG", "HR", "CY", "CZ", "EG", "FR", "DE", "GR", "HU", "IE", "IL", "IT", "LU", "MT", "NL", "PK", "PL", "PT", "RO", "RU", "RS", "SK", "SI", "ZA", "ES", "TR",
    "UA" ];
var SUB_REGIONS = new Set([ 'MQ', 'GP', 'GF', 'PM', 'RE', 'TF', 'KM', 'YT', 'NC', 'VU', 'WF', 'PF', 'MC', 'AD', 'DZ' ]);

function afterConfigForFR() {
  FormManager.hide('CountrySubRegion', 'countryUse');
  var role = null;
  var reqType = null;

  reqType = FormManager.getActualValue('reqType');
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }

  if (reqType == 'C') {
    if (FormManager.getField('capInd').set) {
      FormManager.getField('capInd').set('checked', true);
    } else if (FormManager.getField('capInd')) {
      FormManager.getField('capInd').checked = true;
    }
  }
  FormManager.readOnly('capInd');

  FormManager.readOnly('subIndustryCd');

  affacturageLogic();
  setFieldsRequiredForCreateRequester();
  setIERPSitePartyIDForFR()

  if (role == 'Requester') {
    FormManager.readOnly('abbrevNm');
    FormManager.readOnly('abbrevLocn');
    // FormManager.readOnly('taxCd2');
    FormManager.readOnly('poBox');
    // FormManager.readOnly('embargoCd');
    // FormManager.readOnly('currencyCd');

    if (reqType == 'C') {
      FormManager.resetDropdownValues(FormManager.getField('taxCd2'));
      FormManager.limitDropdownValues(FormManager.getField('taxCd2'), [ '1', '0' ]);
      FormManager.setValue('taxCd2', '1');

      FormManager.readOnly('ordBlk');

      internalDeptValidate();
      iSICAndSubScenarioTypeCreate();
    }

    if (reqType == 'U') {
      FormManager.enable('taxCd2');
      FormManager.enable('currencyCd');
      FormManager.enable('abbrevNm');
      FormManager.addValidator('abbrevNm', Validators.REQUIRED, [ 'Abbreviated Name (TELX1)' ], 'MAIN_CUST_TAB');
      FormManager.enable('abbrevLocn');
      FormManager.addValidator('abbrevLocn', Validators.REQUIRED, [ 'Abbreviated Location' ], 'MAIN_CUST_TAB');
      FormManager.enable('ordBlk');
      orderBlockCodeValidator();
      setInternalDept();

      internalDeptValidate();
      iSICAndSubScenarioTypeUpdate();
    }
  } else if (role == 'Processor') {
    FormManager.enable('abbrevNm');
    FormManager.enable('abbrevLocn');
    FormManager.enable('taxCd2');
    FormManager.enable('embargoCd');
    FormManager.enable('currencyCd');
    // FormManager.addValidator('taxCd2', Validators.REQUIRED, [ 'Tax Code' ],
    // 'MAIN_CUST_TAB');
    FormManager.addValidator('abbrevNm', Validators.REQUIRED, [ 'Abbreviated Name (TELX1)' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('abbrevLocn', Validators.REQUIRED, [ 'Abbreviated Location' ], 'MAIN_CUST_TAB');
    orderBlockCodeValidator();
    internalDeptValidate();
    iSICAndSubScenarioTypeUpdate();

    FormManager.addFormValidator((function() {
      return {
        validate : function() {
          var orderBlockCd = FormManager.getActualValue('taxCd2');
          if (orderBlockCd != '') {
            return new ValidationResult(null, true);
          } else {
            return new ValidationResult({
              id : 'taxCd2',
              type : 'text',
              name : 'taxCd2'
            }, false, 'Tax Code is required.');
          }

          return new ValidationResult(null, true);
        }
      };
    })(), 'MAIN_CUST_TAB', 'frmCMR');
  }

  if (role == 'Processor' && reqType == 'C') {
    FormManager.addValidator('isuCd', Validators.REQUIRED, [ 'ISU Code' ], 'MAIN_IBM_TAB');
    // CREATCMR-4293
    // FormManager.addValidator('clientTier', Validators.REQUIRED, [ 'Client
    // Tier' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('salesBusOffCd', Validators.REQUIRED, [ 'Search Term (SORTL)' ], 'MAIN_IBM_TAB');
    // FormManager.addValidator('installBranchOff', Validators.REQUIRED, [
    // 'Installing BO' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('repTeamMemberNo', Validators.REQUIRED, [ 'Sales Rep No' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('subIndustryCd', Validators.REQUIRED, [ 'Subindustry' ], 'MAIN_CUST_TAB');
    // FormManager.readOnly('ordBlk');
    // FormManager.addValidator('abbrevNm', Validators.REQUIRED, [ 'Abbreviated
    // Name (TELX1)' ], 'MAIN_CUST_TAB');
    // FormManager.addValidator('abbrevLocn', Validators.REQUIRED, [
    // 'Abbreviated Location' ], 'MAIN_CUST_TAB');

    if (_isuHandler == null) {
      _isuHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
        if (value == undefined) {
          return;
        }
        setCtcByIsu(value);
        setCoverageSBOBasedOnIsuCtc();
      });
    }

    if (_isuHandler && _isuHandler[0]) {
      _isuHandler[0].onChange();
    }
  }

  var _custSubGrpHandler = dojo.connect(FormManager.getField('custSubGrp'), 'onChange', function(value) {
    // setAbbrevNmOnCustSubGrpChange();
    setAbbrevLocnOnCustSubGrpChange();
    setDummySIRETOnCustSubGrpChange(value);
    setHideFieldForFR();
    setINACOnScenario();
    setISICAndSubindustryOnScenario();
    setVATOnScenario();
    // setSBOOnScenarioLD();
    setSalesRepLogic();
    setTaxCdOnScnrio();
    affacturageLogic();
    // setISUClientTierOnScenario();
    setFieldsRequiredForCreateRequester();
    setClassificationCode();
    setTaxCd();
    if (FormManager.getActualValue('custSubGrp') == 'HOSTC' || FormManager.getActualValue('custSubGrp') == 'CBSTC') {
      updateAbbrNameWithZS01_ZI01();
    } else if (FormManager.getActualValue('custSubGrp') == 'THDPT' || FormManager.getActualValue('custSubGrp') == 'CBDPT') {
      updateAbbrNameWithTPZS01_ZI01();
    } else {
      updateAbbrNameWithZS01();
    }
    setPPSCEIDRequired();
  });
  if (_custSubGrpHandler && _custSubGrpHandler[0]) {
    _custSubGrpHandler[0].onChange();
  }

  // CMR-(228) France new tax code logic don't need this.
  // just remove this START.
  /*
   * dojo.connect(FormManager.getField('isicCd'), 'onChange', function(value) {
   * var isicCd = FormManager.getActualValue('isicCd'); var countyCd =
   * FormManager.getActualValue('countryUse'); if (countyCd == '706NC') { if
   * (isicCd == '1010' || isicCd == '1020' || isicCd == '1200' || isicCd ==
   * '1310' || isicCd == '1320' || isicCd == '1421' || isicCd == '1429') {
   * FormManager.resetDropdownValues(FormManager.getField('taxCd2'));
   * FormManager.setValue('taxCd2', '42'); FormManager.readOnly('taxCd2'); }
   * else { FormManager.resetDropdownValues(FormManager.getField('taxCd2'));
   * FormManager.setValue('taxCd2', '41'); FormManager.readOnly('taxCd2'); } }
   * });
   */
  // just remove this END.
  // set defaultLandedCountry
  var landCntry = '';
  var cntryRegion = FormManager.getActualValue('countryUse');
  if (cntryRegion && cntryRegion != '' && cntryRegion.length > 3) {
    landCntry = cntryRegion.substring(3, 5);
  }
  if (landCntry != '') {
    FormManager.setValue('defaultLandedCountry', landCntry);
  }

  reqReasonOnChange();
  filterCmrnoP();
  cmrNoEnable();
  FormManager.addValidator('taxCd1', Validators.DIGIT, [ 'SIRET' ], 'MAIN_CUST_TAB');
  if (reqType == 'U' && FormManager.getActualValue('viewOnlyPage') != 'true') {
    FormManager.enable('taxCd1');
  } else {
    FormManager.readOnly('taxCd1');
  }
  addVatExemptHandler();
  dojo.connect(FormManager.getField('custSubGrp'), 'onChange', function(value) {
    if (value != '' || value != 'undefined') {
      var landedCountry = getSoldToLanded();
      var custGrp = FormManager.getActualValue('custGrp');
      var custSubGrp = FormManager.getActualValue('custSubGrp');
      var custSubGrpArrayCbComm = [ 'CBMME', 'CBFIN', 'CBVRN', 'CBSTC', 'CBDPT', 'XBLUM' ];
      var custSubGrpArrayLocalComm = [ 'COMME', 'LCFIN', 'GOVRN', 'HOSTC', 'PRICU', 'THDPT' ];
      var LandedCntryArray = [ 'TN', 'LY', 'DZ', 'RE', 'KM', 'MQ', 'GP', 'GF', 'PM', 'YT', 'NC', 'WF', 'VU', 'PF', 'AD', 'MC' ];
      if (custSubGrpArrayCbComm.includes(custSubGrp) && !isCoverage2H22Subregion(landedCountry) && !isCoverage2H22MEACountry(landedCountry) && !LandedCntryArray.includes(landedCountry)) {
        FormManager.setValue('isuCd', '21');
        FormManager.setValue('clientTier', '');
      } else if (custSubGrpArrayCbComm.includes(custSubGrp) && isCoverage2H22MEACountry(landedCountry) && !LandedCntryArray.includes(landedCountry)) {
        FormManager.setValue('isuCd', '34');
        FormManager.setValue('clientTier', 'Q');
      } else if (custSubGrpArrayCbComm.includes(custSubGrp) && LandedCntryArray.includes(landedCountry)) {
        FormManager.setValue('isuCd', '34');
        FormManager.setValue('clientTier', 'Q');
      }
    }
  });
}

function setINACOnScenario() {
  var role = null;
  var reqType = FormManager.getActualValue('reqType');
  var subGrp = FormManager.getActualValue('custSubGrp');
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }
  if (reqType == 'U') {
    return;
  }

  var countyCd = null;
  var countryUse = FormManager.getActualValue('countryUse');
  if (countryUse.length > 3) {
    countyCd = countryUse.substring(3, 5);
  } else {
    countyCd = "FR";
  }
  if (countyCd == 'MC') {
    if (role == 'Requester') {
      FormManager.setValue('inacCd', '');
      FormManager.readOnly('inacCd');

    } else if (role == 'Processor') {
      FormManager.enable('inacCd');
    }
  }
  if (subGrp == 'INTER') {
    FormManager.setValue('inacCd', '');
    FormManager.readOnly('inacCd');
  }
  if (countyCd == 'FR' && subGrp == 'IBMEM' || subGrp == 'CBIEM' || subGrp == 'PRICU' || subGrp == 'CBICU') {
    FormManager.setValue('inacCd', '');
    FormManager.readOnly('inacCd');
  }
}
function setISICAndSubindustryOnScenario() {
  var role = null;
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }

  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  var custSubGrp = FormManager.getActualValue('custSubGrp');

  if (role == 'Requester' || role == 'Processor') {
    if (custSubGrp == 'PRICU' || custSubGrp == 'XBLUM' || custSubGrp == 'IBMEM' || custSubGrp == 'CBIEM') {
      FormManager.setValue('subIndustryCd', 'WQ');
      FormManager.readOnly('subIndustryCd');

      FormManager.setValue('isicCd', '9500');
      FormManager.readOnly('isicCd');
    } else {
      if (role == 'Processor') {
        FormManager.enable('subIndustryCd');
      }
      FormManager.enable('isicCd');
    }
  }
}
// function setVATOnScenario() {
// var role = null;
// var reqType = FormManager.getActualValue('reqType');
// if (typeof (_pagemodel) != 'undefined') {
// role = _pagemodel.userRole;
// }
// if (reqType == 'U') {
// return;
// }
// if (FormManager.getActualValue('viewOnlyPage') == 'true') {
// return;
// }
// if (role == 'Processor') {
// return;
// }
//
// var countyCd = null;
// var countryUse = FormManager.getActualValue('countryUse');
// if (countryUse.length > 3) {
// countyCd = countryUse.substring(3, 5);
// } else {
// countyCd = "FR";
// }
//
// var custSubGrp = FormManager.getActualValue('custSubGrp');
// if (custSubGrp == '') {
// return;
// } else {
// if (custSubGrp == 'PRICU' || custSubGrp == 'CBICU' || custSubGrp == 'IBMEM'
// || custSubGrp == 'CBIEM') {
// FormManager.readOnly('vat');
// FormManager.removeValidator('vat', Validators.REQUIRED);
//
// } else if (custSubGrp == 'GOVRN' || custSubGrp == 'CBVRN') {
// // RC: Temporary fix for story CMR-5134, until TAX exempt is implemented
// FormManager.removeValidator('vat', Validators.REQUIRED);
// FormManager.enable('vat');
// } else if (custSubGrp == 'COMME' || custSubGrp == 'FIBAB' || custSubGrp ==
// 'BPIEU' || custSubGrp == 'BPUEU' || custSubGrp == 'GOVRN'
// || custSubGrp == 'INTER' || custSubGrp == 'INTSO' || custSubGrp == 'LCIFF' ||
// custSubGrp == 'LCIFL' || custSubGrp == 'OTFIN'
// || custSubGrp == 'LEASE' || custSubGrp == 'LCOEM' || custSubGrp == 'HOSTC' ||
// custSubGrp == 'THDPT') {
// if (countyCd == 'FR' || countyCd == 'AD') {
// FormManager.enable('vat');
// FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ],
// 'MAIN_CUST_TAB');
// } else {
// FormManager.removeValidator('vat', Validators.REQUIRED);
// FormManager.enable('vat');
// }
//
// } else if (custSubGrp == 'CBMME' || custSubGrp == 'CBBAB' || custSubGrp ==
// 'CBIEU' || custSubGrp == 'CBUEU' || custSubGrp == 'CBVRN'
// || custSubGrp == 'CBTER' || custSubGrp == 'CBTSO' || custSubGrp == 'CBIFF' ||
// custSubGrp == 'CBIFL' || custSubGrp == 'CBFIN'
// || custSubGrp == 'CBASE' || custSubGrp == 'CBOEM' || custSubGrp == 'CBSTC' ||
// custSubGrp == 'CBDPT') {
// var isVATEUCntry = false;
// var reqId = FormManager.getActualValue('reqId');
// var qParams = {
// REQ_ID : reqId,
// ADDR_TYPE : 'ZS01',
// };
// var _result = cmr.query('ADDR.GET.LANDCNTRY.BY_REQID_ADDRTYP', qParams);
// var landCntry = _result.ret2;
// for (var i = 0; i < VAT_EU_COUNTRY.length; i++) {
// if (VAT_EU_COUNTRY[i] == landCntry) {
// isVATEUCntry = true;
// break;
// }
// }
// if (isVATEUCntry == true) {
// FormManager.enable('vat');
// FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ],
// 'MAIN_CUST_TAB');
// } else {
// FormManager.removeValidator('vat', Validators.REQUIRED);
// FormManager.enable('vat');
// }
// }
// }
// }

function setVATOnScenario(vatExemptOnchange) {
  var role = null;
  var reqType = FormManager.getActualValue('reqType');
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }
  if (reqType == 'U') {
    return;
  }
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (!vatExemptOnchange) {
    FormManager.enable('vatExempt');
    if (dijit.byId('vatExempt') != undefined && dijit.byId('vatExempt') != null) {
      FormManager.getField('vatExempt').set('checked', false);
    }
    var dbCustSubGrp = _pagemodel.custSubGrp;
    var dbVatExempt = _pagemodel.vatExempt;
    if (custSubGrp == dbCustSubGrp && dbVatExempt == 'Y' && dijit.byId('vatExempt') != undefined && dijit.byId('vatExempt') != null) {
      FormManager.getField('vatExempt').set('checked', true);
      FormManager.readOnly('vat');
      FormManager.removeValidator('vat', Validators.REQUIRED);
      return;
    }
  }
  if (custSubGrp == '') {
    return;
  } else {
    if (custSubGrp == 'PRICU' || custSubGrp == 'XBLUM' || custSubGrp == 'IBMEM' || custSubGrp == 'CBIEM') {
      FormManager.readOnly('vat');
      FormManager.removeValidator('vat', Validators.REQUIRED);
      FormManager.readOnly('vatExempt');
      if (dijit.byId('vatExempt') != undefined && dijit.byId('vatExempt') != null) {
        FormManager.getField('vatExempt').set('checked', true);
      }
    } else if (custSubGrp == 'GOVRN' || custSubGrp == 'COMME' || custSubGrp == 'HOSTC' || custSubGrp == 'THDPT') {
      FormManager.enable('vat');
      FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
    } else if (custSubGrp == 'INTER' || custSubGrp == 'INTSO' || custSubGrp == 'BUSPR' || custSubGrp == 'LCFIN') {
      FormManager.removeValidator('vat', Validators.REQUIRED);
      FormManager.enable('vat');
    } else if (custSubGrp == 'CBMME' || custSubGrp == 'XBUSP' || custSubGrp == 'CBTER' || custSubGrp == 'CBSTC' || custSubGrp == 'CBDPT' || custSubGrp == 'CBTSO' || custSubGrp == 'CBFIN'
        || custSubGrp == 'CBVRN') {
      var isVATEUCntry = false;
      var reqId = FormManager.getActualValue('reqId');
      var qParams = {
        REQ_ID : reqId,
        ADDR_TYPE : 'ZS01',
      };
      var _result = cmr.query('ADDR.GET.LANDCNTRY.BY_REQID_ADDRTYP', qParams);
      var landCntry = _result.ret2;
      for (var i = 0; i < VAT_EU_COUNTRY.length; i++) {
        if (VAT_EU_COUNTRY[i] == landCntry) {
          isVATEUCntry = true;
          break;
        }
      }
      if (isVATEUCntry == true) {
        FormManager.enable('vat');
        FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
      } else {
        FormManager.removeValidator('vat', Validators.REQUIRED);
        FormManager.enable('vat');
      }
    }
  }
}

function setSBOOnScenarioLD() {
  var role = null;
  var reqType = FormManager.getActualValue('reqType');
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }
  var isu = FormManager.getActualValue('isuCd');
  var ctc = FormManager.getActualValue('clientTier');
  isuCtc = isu + ctc;
  if (reqType == 'U' || FormManager.getActualValue('viewOnlyPage') == 'true' || isuCtc != "34Q") {
    return;
  }
  var countyCd = "FR";
  var custLandCntry = FormManager.getActualValue('landCntry');
  if (custLandCntry != '') {
    countyCd = custLandCntry;
  } else {
    var _zs01ReqId = FormManager.getActualValue('reqId');
    var cntryCdParams = {
      REQ_ID : _zs01ReqId,
      ADDR_TYPE : 'ZS01',
    };
    var cntryCdResult = cmr.query('ADDR.GET.LANDEDCNTRY.BY_REQID_ADDRTYPE', cntryCdParams);

    if (cntryCdResult.ret1 != undefined) {
      countyCd = cntryCdResult.ret1;
    }
  }
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var custSubGrpArrayCbComm = [ 'CBMME', 'CBFIN', 'CBVRN', 'CBSTC', 'CBDPT', 'XBLUM' ];
  var LandedCntryArray = [ 'TN', 'LY', 'DZ', 'RE', 'KM', 'MQ', 'GP', 'GF', 'PM', 'YT', 'NC', 'WF', 'VU', 'PF', 'AD', 'MC' ];
  if (custSubGrp == '' || !custSubGrpArrayCbComm.includes(custSubGrp)) {
    return;
  } else {
    FormManager.readOnly('installBranchOff');
    if (countyCd == "LY" || countyCd == "DZ") {
      FormManager.setValue('salesBusOffCd', '710710');
    } else if (countyCd == "RE" || countyCd == "KM") {
      FormManager.setValue('salesBusOffCd', '860860');
    } else if (countyCd == "MQ") {
      FormManager.setValue('salesBusOffCd', '851851');
    } else if (countyCd == "GP") {
      FormManager.setValue('salesBusOffCd', '852852');
    } else if (countyCd == "GF" || countyCd == "PM") {
      FormManager.setValue('salesBusOffCd', '853853');
    } else if (countyCd == "YT") {
      FormManager.setValue('salesBusOffCd', '864864');
    } else if (countyCd == "NC" || countyCd == "WF" || countyCd == "VU") {
      FormManager.setValue('salesBusOffCd', '872872');
    } else if (countyCd == "PF") {
      FormManager.setValue('salesBusOffCd', '873873');
    } else if (countyCd == "AD") {
      FormManager.setValue('salesBusOffCd', 'IBOIBO');
    } else if (countyCd == "MC") {
      FormManager.setValue('salesBusOffCd', '18D18D');
    }
    var sbo = FormManager.getActualValue('salesBusOffCd');
    if ((sbo == null || sbo == '' || sbo == undefined) && role != 'Requester' && custSubGrp != 'XBLUM') {
      FormManager.enable('salesBusOffCd');
    }
  }
}

function setSalesRepLogic() {
  var role = null;
  var reqType = null;
  var countyCd = null;
  var countryUse = FormManager.getActualValue('countryUse');
  var custGrp = FormManager.getActualValue('custGrp');

  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
    reqType = FormManager.getActualValue('reqType');
  } else {
    return;
  }
  if (role != 'Requester' && role != 'Processor') {
    return;
  }
  if (reqType == 'U') {
    return;
  }

  if (countryUse.length > 3) {
    countyCd = countryUse.substring(3, 5);
  } else {
    countyCd = "FR";
  }
  if (countyCd == "DZ" && custGrp == "LOCAL")
    FormManager.setValue('repTeamMemberNo', 'A99999');
  else
    FormManager.setValue('repTeamMemberNo', 'D99999');
}

var _isuHandler = null;
var _postCdHandler = null;
var _isicHandler = null;
var _postBoxHandler = null;
function addISUHandler() {
  if (_isuHandler == null) {
    _isuHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
      if (!value) {
        value = FormManager.getActualValue('isuCd');
      }
      setCoverageSBOBasedOnIsuCtc();
    });
  }
}

function addPostBoxHandler() {
  if (_postBoxHandler == null) {
    _postBoxHandler = dojo.connect(FormManager.getField('poBox'), 'onChange', function() {
      var poBox = FormManager.getActualValue('poBox');
      var addrType = FormManager.getActualValue('addrType');
      var importInd = FormManager.getActualValue('importInd');

      if (addrType == 'ZP01' && importInd == 'N' && poBox != '') {
        FormManager.removeValidator('addrTxt', Validators.REQUIRED);
      } else {
        FormManager.addValidator('addrTxt', Validators.REQUIRED, [ 'Street name and number' ]);
      }
    });
  }
}

function addPostCdHandler() {
  var addrType = FormManager.getActualValue('addrType');
  if (addrType != null && addrType == 'ZS01') {
    var city1 = FormManager.getActualValue('city1');
    var currPostCd = FormManager.getActualValue('postCd');
    currPostCd = currPostCd.substring(0, 2);
    // CREATCMR-4530 For Local scenarios except Internal BP
    autoSetValuesOnPostalCode(currPostCd, city1);
  }
}

function addIsicCdHandler() {
  if (_isicHandler == null) {
    _isicHandler = dojo.connect(FormManager.getField('isicCd'), 'onChange', function(value) {
      var custGrp = FormManager.getActualValue('custGrp');
      if (custGrp == 'LOCAL') {
        add34QPostalCdLogic();
      }
    });
  }
}
// CREATCMR - 4530 Postal code logic
function add34QPostalCdLogic() {
  var _zs01ReqId = FormManager.getActualValue('reqId');
  var postCdParams = {
    REQ_ID : _zs01ReqId,
    ADDR_TYPE : "ZS01",
  };
  var postCdResult = cmr.query('ADDR.GET.POSTCD.BY_REQID_ADDRTYP', postCdParams);
  postCd = postCdResult.ret1;
  if (postCd != null && postCd.length > 2) {
    postCd = postCd.substring(0, 2);
  }
  var cityResult = cmr.query('ADDR.GET.CITY1.BY_REQID_ADDRTYP', postCdParams);
  var city1 = cityResult.ret1;
  // autoSetSboOnPostalCode(false, currPostCd);
  // CREATCMR-4530 For Local scenarios except Internal BP
  autoSetValuesOnPostalCode(postCd, city1);
  // }
}

function autoSetValuesOnPostalCode(postCd, city1) {
  var isuCd = FormManager.getActualValue('isuCd');
  var clientTier = FormManager.getActualValue('clientTier');
  var isuCtc = isuCd + clientTier;
  var isicCd = FormManager.getActualValue('isicCd');
  var reqType = FormManager.getActualValue('reqType');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var isViewPage = FormManager.getActualValue('viewOnlyPage');
  var custType = FormManager.getActualValue('custGrp');

  // add scenario check
  if (isuCtc != "34Q" || isViewPage == 'true' || reqType == 'U' || custSubGrp == '' || custType == 'CROSS') {
    return;
  }
  var PostCdList1 = [ '02', '14', '27', '50', '59', '60', '61', '62', '76', '80' ];
  var PostCdList2 = [ '18', '22', '28', '29', '35', '36', '37', '41', '44', '45', '49', '53', '56', '72', '79', '85', '86' ];
  var PostCdList3 = [ '01', '03', '07', '15', '26', '38', '42', '43', '63', '69', '73', '74' ];
  var PostCdList4 = [ '08', ' 10', ' 21', ' 25', ' 39', ' 51', ' 52', ' 54', ' 55', ' 57', ' 58', ' 67', ' 68', ' 70', ' 71', ' 88', ' 89', ' 90' ];
  var PostCdList5 = [ '09', ' 12', ' 16', ' 17', ' 19', ' 23', ' 24', ' 31', ' 32', ' 33', ' 40', ' 46', ' 47', ' 64', ' 65', ' 81', ' 82', ' 87' ];
  var PostCdList6 = [ '04', ' 05', ' 06', ' 11', ' 13', ' 20', ' 30', ' 34', ' 48', ' 66', ' 83', ' 84' ];
  city1 = city1.toUpperCase();
  var sboSetbyPostalCdLogic = false;
  if (city1.includes('NIORT')) {
    FormManager.setValue('salesBusOffCd', "8CB8CB");
    sboSetbyPostalCdLogic = true;
  } else if (PostCdList1.includes(postCd) && city1.includes('LILLE')) {
    FormManager.setValue('salesBusOffCd', "FLIFLI");
    sboSetbyPostalCdLogic = true;
  } else if (PostCdList2.includes(postCd) && city1.includes('NANTES')) {
    FormManager.setValue('salesBusOffCd', "INAINA");
    sboSetbyPostalCdLogic = true;
  } else if (PostCdList3.includes(postCd) && city1.includes('LYON')) {
    FormManager.setValue('salesBusOffCd', "4DD4DD");
    sboSetbyPostalCdLogic = true;
  } else if (PostCdList4.includes(postCd) && city1.includes('STRASBOURG')) {
    FormManager.setValue('salesBusOffCd', "FSTFST");
    sboSetbyPostalCdLogic = true;
  } else if (PostCdList5.includes(postCd) && (city1.includes('BORDEAUX') || city1.includes('TOULOUSE'))) {
    FormManager.setValue('salesBusOffCd', "IBOIBO");
    sboSetbyPostalCdLogic = true;
  } else if (PostCdList6.includes(postCd) && (city1.includes('MARSEILLE') || city1.includes('MONTPELLIER') || city1.includes('NICE'))) {
    FormManager.setValue('salesBusOffCd', "18D18D");
    sboSetbyPostalCdLogic = true;
  }
  // If Sbo not set by PostalCd logic then ISIC logic
  if (!sboSetbyPostalCdLogic) {

    var IsicCdList1 = [ '9806', '6519', '6591', '6592', '7020', '7705', '9819', '6511', '6599', '6711', '6712', '6719', '7715' ];
    var IsicCdList2 = [ '7511', '7523', '9900', '7706', '9807', '7513', '7514', '7521', '7522', '7720', '9818', '8030', '8090', '8010', '8021', '8022', '7704', '9231', '7512', '7320' ];
    var IsicCdList3 = [ '7310', '2423', '7721' ];
    var IsicCdList4 = [ '9820', '6210', '6220', '6010', '6021', '6022', '6023', '6110', '6120', '6301', '6302', '6303', '6304', '5510', '7111', '6309', '6411', '6412', '7716' ];
    var IsicCdList5 = [ '9808', '8512', '8511', '8519', '7707' ];
    var IsicCdList6 = [ '8532', '7530' ];
    var IsicCdList7 = [ '7414', '7411', '7412' ];
    var IsicCdList8 = [ '9813', '2930', '3410', '3420', '3591', '2511', '3430', '2911', '2921', '3520', '3599', '5010', '5040', '7708', '2811', '2812', '2813', '2892', '2893', '2899', '2912', '2913',
        '2914', '2915', '2919', '2922', '2923', '2925', '2926', '2929', '3320', '3512', '3592', '3610', '3691', '3692', '3693', '3694', '3699', '4510', '4530', '4540', '7010', '2010', '2021', '2022',
        '2023', '2029', '2101', '2102', '2109', '1711', '1712', '1722', '1723', '1729', '2310', '2710', '2720', '2731', '2732', '2891', '3710', '1010', '1020', '1030', '1200', '1310', '1320', '1410',
        '1421', '1422', '1429', '2519', '2520', '2610', '2691', '2692', '2693', '2694', '2695', '2696', '2699', '3720', '7711', '9817', '3110', '3120', '3130', '3140', '3150', '3190', '3210', '3220',
        '3230', '3311', '3312', '3313', '3330', '3000', '7710' ];
    var IsicCdList9 = [ '9816', '3530', '2927', '3511', '7718' ];
    var IsicCdList10 = [ '0140', '0150', '0200', '0501', '0502', '7495', '9812', '1513', '1514', '1520', '1541', '1542', '1543', '1544', '1549', '1551', '1552', '1553', '1554', '1600', '0111',
        '0112', '0113', '0121', '0122', '0130', '1511', '1512', '1531', '1532', '1533', '8520', '7703', '5121', '5122', '5139', '5142', '5149', '5151', '5152', '5159', '5190', '5020', '5110', '5260',
        '7112', '7113', '7121', '7129', '7130', '7422', '7491', '7492', '7493', '7494', '7499', '8531', '9301', '9302', '9303', '9309', '9500', '7719', '9804', '5219', '5251', '5030', '5240', '5211',
        '5220', '5252', '5259', '5520', '5233', '5234', '7714' ];
    var IsicCdList11 = [ '2424', '5131', '1721', '1730', '1810', '1820', '1911', '1912', '1920', '5232', '5239', '5231' ];
    var IsicCdList12 = [ '9814', '6601', '6603', '6602', '6720', '7712' ];
    var IsicCdList13 = [ '9811', '4888', '9213', '2230', '9211', '9212', '9214', '9219', '9249', '2211', '2212', '2213', '2219', '2221', '2222', '7413', '9220', '9241', '7430', '7709', '9233',
        '9801', '6420', '7700', '9111', '9112', '9120', '9191', '9192', '9199', '9232' ];
    var IsicCdList14 = [ '9802', '7230', '7240', '7290', '7210', '7221', '7229', '7250', '7123', '7701', '7702' ];
    var IsicCdList15 = [ '2924', '4520', '7421', '5143', '4550', '7122' ];
    var IsicCdList16 = [ '9821', '4010', '4020', '4030', '4100', '9000', '7717', '9822', '2330', '2411', '2412', '2413', '2421', '2422', '2429', '2430', '1110', '1120', '2320', '5050', '5141',
        '6030', '7713' ];

    if (IsicCdList1.includes(isicCd)) {
      FormManager.setValue('salesBusOffCd', "14A14A");
    } else if (IsicCdList2.includes(isicCd)) {
      FormManager.setValue('salesBusOffCd', "14B14B");
    } else if (IsicCdList3.includes(isicCd)) {
      FormManager.setValue('salesBusOffCd', "14C14C");
    } else if (IsicCdList4.includes(isicCd)) {
      FormManager.setValue('salesBusOffCd', "31F31F");
    } else if (IsicCdList5.includes(isicCd)) {
      FormManager.setValue('salesBusOffCd', "15C15C");
    } else if (IsicCdList6.includes(isicCd)) {
      FormManager.setValue('salesBusOffCd', "3TS3TS");
    } else if (IsicCdList7.includes(isicCd)) {
      FormManager.setValue('salesBusOffCd', "8CS8CS");
    } else if (IsicCdList8.includes(isicCd)) {
      FormManager.setValue('salesBusOffCd', "3TG3TG");
    } else if (IsicCdList9.includes(isicCd)) {
      FormManager.setValue('salesBusOffCd', "5BA5BA");
    } else if (IsicCdList10.includes(isicCd)) {
      FormManager.setValue('salesBusOffCd', "12W12W");
    } else if (IsicCdList11.includes(isicCd)) {
      FormManager.setValue('salesBusOffCd', "11W11W");
    } else if (IsicCdList12.includes(isicCd)) {
      FormManager.setValue('salesBusOffCd', "19W19W");
    } else if (IsicCdList13.includes(isicCd)) {
      FormManager.setValue('salesBusOffCd', "4FF4FF");
    } else if (IsicCdList14.includes(isicCd)) {
      FormManager.setValue('salesBusOffCd', "IF1IF1");
    } else if (IsicCdList15.includes(isicCd)) {
      FormManager.setValue('salesBusOffCd', "05G05G");
    } else if (IsicCdList16.includes(isicCd)) {
      FormManager.setValue('salesBusOffCd', "MC5MC5");
    }
  }
}

function add32PostCdCntrySBOlogicOnISUChange() {
  _isuHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
    // add32PostCdCntrySBOlogic();
  });
  if (_isuHandler && _isuHandler[0]) {
    _isuHandler[0].onChange();
  }
  var _subIndustryHandler = dojo.connect(FormManager.getField('subIndustryCd'), 'onChange', function(value) {
    // add32PostCdCntrySBOlogic();
  });
  if (_subIndustryHandler && _subIndustryHandler[0]) {
    _subIndustryHandler[0].onChange();
  }
}

function addIBOlogic() {
  var reqType = FormManager.getActualValue('reqType');
  // var role = null;
  // if (typeof (_pagemodel) != 'undefined') {
  // role = _pagemodel.userRole;
  // }
  //
  // if (role == 'Processor') {
  // FormManager.enable('installBranchOffCd');
  // return;
  // }
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  if (reqType == 'C') {
    addSboIboLogic();
  }
}

function addSboIboLogic() {
  var sboCd = '';
  var iboCd = '';
  var _sboHandler = dojo.connect(FormManager.getField('salesBusOffCd'), 'onChange', function(value) {
    if (FormManager.getActualValue('salesBusOffCd') == _pagemodel.salesBusOffCd) {
      return;
    } else if (value != _pagemodel.salesBusOffCd) {
      sboCd = FormManager.getActualValue('salesBusOffCd');
      iboCd = sboCd;
      if (iboCd != '' && iboCd.length > 3) {
        iboCd = iboCd.substring(iboCd.length - 3, iboCd.length);
      }
      FormManager.setValue('installBranchOff', iboCd);
    }
  });
  if (_sboHandler && _sboHandler[0]) {
    _sboHandler[0].onChange();
  }
}

/*
 * Mandatory address ZS01 Sold-to
 */
function addFRAddressTypeValidator() {
  console.log("addFRAddressTypeValidator..............");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('cmrIssuingCntry') != '706') {
          return new ValidationResult(null, true);
        }
        var reqType = FormManager.getActualValue('reqType');
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount == 0 && reqType == 'C') {
          return new ValidationResult(null, false, 'Sold-to address is mandatory.');
        } else if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount == 0 && reqType == 'U') {
          return new ValidationResult(null, false, 'Sold-to address is mandatory.');
        }
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          var record = null;
          var type = null;
          var installingCnt = 0;

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
              installingCnt++;
            }
          }

          if (installingCnt == 0) {
            return new ValidationResult(null, false, 'Sold-to address is mandatory.');
          } else if (installingCnt > 1) {
            return new ValidationResult(null, false, 'Only one Sold-to address can be defined. Please remove the additional Sold-to address.');
          } else {
            return new ValidationResult(null, true);
          }
        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}
/**
 * No non-Latin characters for FR
 */
function addLatinCharValidatorFR() {
  FormManager.addValidator('custNm1', Validators.LATIN1, [ 'Customer legal name' ]);
  FormManager.addValidator('custNm2', Validators.LATIN1, [ 'Legal name continued' ]);
  FormManager.addValidator('custNm3', Validators.LATIN1, [ 'Division/Department' ]);
  FormManager.addValidator('custNm4', Validators.LATIN1, [ 'Attention to/Building/Floor/Office' ]);
  FormManager.addValidator('addrTxt', Validators.LATIN1, [ 'Street name and number' ]);
  // FormManager.addValidator('addrTxt2', Validators.LATIN1, [ 'Street
  // Continuation' ]);
  FormManager.addValidator('city1', Validators.LATIN1, [ 'City' ]);
}

// function addALPHANUMSPACEValidatorFR() {
// var role = null;
// if (typeof (_pagemodel) != 'undefined') {
// role = _pagemodel.userRole;
// }
// if (role == 'Processor' || (role == 'Requester' &&
// FormManager.getActualValue('reqType') == 'U')) {
// FormManager.addValidator('abbrevLocn', Validators.CHECKALPHANUMDASH, [
// 'Abbreviated Location' ], 'MAIN_CUST_TAB');
// FormManager.addValidator('abbrevNm', Validators.CHECKALPHANUMDASH, [
// 'Abbreviated Name' ], 'MAIN_CUST_TAB');
// FormManager.addValidator('abbrevLocn', Validators.CHECKSPACEPLACE4DASH, [
// 'Abbreviated Location' ], 'MAIN_CUST_TAB');
// FormManager.addValidator('abbrevNm', Validators.CHECKSPACEPLACE4DASH, [
// 'Abbreviated Name' ], 'MAIN_CUST_TAB');
// FormManager.addValidator('abbrevLocn', Validators.CHECKDASHSPACE, [
// 'Abbreviated Location' ], 'MAIN_CUST_TAB');
// FormManager.addValidator('abbrevNm', Validators.CHECKDASHSPACE, [
// 'Abbreviated Name' ], 'MAIN_CUST_TAB');
// }
//
// FormManager.addValidator('custNm1', Validators.CHECKALPHANUMDASH, [ 'Customer
// Name' ]);
// FormManager.addValidator('custNm2', Validators.CHECKALPHANUMDASHDOT, [
// 'Customer Name Continuation' ]);
// FormManager.addValidator('custNm3', Validators.CHECKALPHANUMDASH, [ 'Customer
// Name/ Additional Address Information' ]);
// FormManager.addValidator('addrTxt', Validators.CHECKALPHANUMDASH, [ 'Street'
// ]);
// FormManager.addValidator('addrTxt2', Validators.CHECKALPHANUMDASH, [ 'Street
// Continuation' ]);
// FormManager.addValidator('city1', Validators.CHECKALPHANUMDASH, [ 'City' ]);
// FormManager.addValidator('custNm1', Validators.CHECKSPACEPLACE4DASH, [
// 'Customer Name' ]);
// FormManager.addValidator('custNm2', Validators.CHECKSPACEPLACE4DASH, [
// 'Customer Name Continuation' ]);
// FormManager.addValidator('custNm3', Validators.CHECKSPACEPLACE4DASH, [
// 'Customer Name/ Additional Address Information' ]);
// FormManager.addValidator('addrTxt', Validators.CHECKSPACEPLACE4DASH, [
// 'Street' ]);
// FormManager.addValidator('addrTxt2', Validators.CHECKSPACEPLACE4DASH, [
// 'Street Continuation' ]);
// FormManager.addValidator('city1', Validators.CHECKSPACEPLACE4DASH, [ 'City'
// ]);
// FormManager.addValidator('custNm1', Validators.CHECKDASHSPACE, [ 'Customer
// Name' ]);
// FormManager.addValidator('custNm2', Validators.CHECKDASHSPACE, [ 'Customer
// Name Continuation' ]);
// FormManager.addValidator('custNm3', Validators.CHECKDASHSPACE, [ 'Customer
// Name/ Additional Address Information' ]);
// FormManager.addValidator('addrTxt', Validators.CHECKDASHSPACE, [ 'Street' ]);
// FormManager.addValidator('addrTxt2', Validators.CHECKDASHSPACE, [ 'Street
// Continuation' ]);
// FormManager.addValidator('city1', Validators.CHECKDASHSPACE, [ 'City' ]);
// }

function addALPHANUMSPACEValidatorFR() {
  var role = null;
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }
  if (role == 'Processor' || (role == 'Requester' && FormManager.getActualValue('reqType') == 'U')) {
    FormManager.addValidator('abbrevLocn', Validators.LATIN1, [ 'Abbreviated Location' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('abbrevNm', Validators.LATIN1, [ 'Abbreviated Name' ], 'MAIN_CUST_TAB');
  }

  FormManager.addValidator('custNm1', Validators.LATIN1, [ 'Customer Name' ]);
  FormManager.addValidator('custNm2', Validators.LATIN1, [ 'Customer Name Continuation' ]);
  FormManager.addValidator('custNm3', Validators.LATIN1, [ 'Customer Name/ Additional Address Information' ]);
  FormManager.addValidator('addrTxt', Validators.LATIN1, [ 'Street' ]);
  FormManager.addValidator('addrTxt2', Validators.LATIN1, [ 'Street Continuation' ]);
  FormManager.addValidator('city1', Validators.LATIN1, [ 'City' ]);
}

/**
 * story 1567051 - Address H available only for IGF LOB
 */
// function toggleAddrTypesForFR(cntry, addressMode) {
// var requestingLob = FormManager.getActualValue('requestingLob');
// if (requestingLob == 'IGF') {
// if (addressMode == 'newAddress' || addressMode == 'copyAddress') {
// cmr.showNode('radiocont_ZD02');
// }
// } else {
// cmr.hideNode('radiocont_ZD02');
// }
// dojo.connect(FormManager.getField('requestingLob'), 'onChange',
// function(value) {
// var _requestingLob = FormManager.getActualValue('requestingLob');
// if (_requestingLob == 'IGF') {
// if (addressMode == 'newAddress' || addressMode == 'copyAddress') {
// cmr.showNode('radiocont_ZD02');
// }
// } else {
// cmr.hideNode('radiocont_ZD02');
// }
// });
// }
function included(cntryCd) {
  var includedCntryCd = [ 'AD', 'MC', 'MQ', 'GP', 'GF', 'PM', 'RE', 'TF', 'KM', 'YT', 'NC', 'VU', 'WF', 'PF' ];
  var includedInd = false;
  for (var i = 0; i < includedCntryCd.length; i++) {
    if (cntryCd == includedCntryCd[i])
      includedInd = true;
  }
  if (includedInd == true) {
    return true;
  } else {
    return false;
  }
}

function setCtcForIsu5K() {
  isuCd = FormManager.getActualValue('isuCd');
  var ctc = FormManager.getActualValue('clientTier');
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var custSubGrpArray = [ 'INTER', 'INTSO', 'IBMEM', 'BUSPR', 'XBUSP', 'CBTER', 'CBTSO', 'CBIEM', 'PRICU', 'XBLUM' ];
  if (isuCd == '5K' || isuCd == '14' || isuCd == '18' || isuCd == '19' || isuCd == '1R' || isuCd == '31' || isuCd == '3T' || isuCd == '4A') {
    FormManager.removeValidator('clientTier', Validators.REQUIRED);
  } else {
    if (reqType == 'U' || (reqType != 'U' && role == 'PROCESSOR' && !custSubGrpArray.includes(custSubGrp))) {
      FormManager.enable('clientTier');
    }
  }
}

function add32PostCdCntrySBOlogic() {
  var role = null;
  var reqType = null;
  var postCd = null;
  var cntryCd = null;
  var sboValue = null;

  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
    reqType = FormManager.getActualValue('reqType');
  } else {
    return;
  }
  if (role != 'Requester' && role != 'Processor') {
    return;
  }
  if (reqType == 'U') {
    return;
  }

  var countyCd = null;
  // var countryUse = FormManager.getActualValue('countryUse');
  // if (countryUse.length > 3) {
  // countyCd = countryUse.substring(3, 5);
  if (FormManager.getActualValue('landCntry') != undefined && FormManager.getActualValue('landCntry') != '') {
    countyCd = FormManager.getActualValue('landCntry');
  } else {
    countyCd = "FR";
  }
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (!((countyCd == "FR" || countyCd == "KM" || countyCd == "WF") && (custSubGrp == 'COMME' || custSubGrp == 'PRICU' || custSubGrp == 'GOVRN' || custSubGrp == 'INTER' || custSubGrp == 'INTSO'
      || custSubGrp == 'IBMEM' || custSubGrp == 'LCOEM' || custSubGrp == 'HOSTC' || custSubGrp == 'THDPT' || custSubGrp == 'CBTER' || custSubGrp == 'LCFIN'))) {
    return;
  }

  var isuCd = FormManager.getActualValue('isuCd');
  if (isuCd != '32') {
    return;
  }

  FormManager.setValue('salesBusOffCd', '');

  var _zs01ReqId = FormManager.getActualValue('reqId');
  var postCdParams = {
    REQ_ID : _zs01ReqId,
    ADDR_TYPE : "ZS01",
  };
  var postCdResult = cmr.query('ADDR.GET.POSTCD.BY_REQID_ADDRTYP', postCdParams);
  postCd = postCdResult.ret1;
  if (postCd != null && postCd.length > 2) {
    postCd = postCd.substring(0, 2);
  }

  var cntryCdParams = {
    REQ_ID : _zs01ReqId,
    ADDR_TYPE : 'ZS01',
  };
  var cntryCdResult = cmr.query('ADDR.GET.LANDEDCNTRY.BY_REQID_ADDRTYPE', cntryCdParams);
  cntryCd = cntryCdResult.ret1;

  if (postCd == null || cntryCd == null) {
    return;
  }

  var cntryUpdateByParams = null;
  if (included(cntryCd)) {
    cntryUpdateByParams = {
      ISSUING_CNTRY : '706',
      UPDATE_BY_ID : '%' + cntryCd + '%',
      ISU_CD : '32',
    };
    var sboResult = cmr.query('GET.SBO.BY_CNTRY_UPDATED_BY', cntryUpdateByParams);
    sboValue = sboResult.ret1;
    sboValue = sboValue + sboValue;// CMR-221
    FormManager.setValue('salesBusOffCd', sboValue);
  } else if (!included(cntryCd)) {
    if (postCd != '75' && postCd != '77' && postCd != '78' && postCd != '91' && postCd != '92' && postCd != '93' && postCd != '94' && postCd != '95') {
      cntryUpdateByParams = {
        ISSUING_CNTRY : '706',
        UPDATE_BY_ID : '%' + postCd + '%',
        ISU_CD : '32',
      };
      var sboResult = cmr.query('GET.SBO.BY_CNTRY_UPDATED_BY', cntryUpdateByParams);
      sboValue = sboResult.ret1;
      sboValue = sboValue + sboValue;// CMR-221
      FormManager.setValue('salesBusOffCd', sboValue);
    } else if (postCd == '75' || postCd == '77' || postCd == '78' || postCd == '91' || postCd == '92' || postCd == '93' || postCd == '94' || postCd == '95') {
      var insustry = FormManager.getActualValue('subIndustryCd').substring(0, 1);
      if (insustry == 'A' || insustry == 'B' || insustry == 'C' || insustry == 'D' || insustry == 'F' || insustry == 'H' || insustry == 'K' || insustry == 'N' || insustry == 'R' || insustry == 'S'
          || insustry == 'T' || insustry == 'W') {
        FormManager.setValue('salesBusOffCd', '01D01D');// CMR-221
      } else if (insustry == 'E' || insustry == 'G' || insustry == 'J' || insustry == 'L' || insustry == 'M' || insustry == 'P' || insustry == 'U' || insustry == 'V' || insustry == 'X') {
        FormManager.setValue('salesBusOffCd', '01M01M');// CMR-221
      }
    }
  }

  // SBO scenario logic should over write the 32 postal code logic
  setSBOOnScenarioLD();
}

function addPhoneValidatorFR() {
  FormManager.addValidator('custPhone', Validators.LATIN, [ 'Phone #' ]);
}

// function setAbbrevNmOnCustSubGrpChange() {
// var reqType = null;
// var role = null;
// if (typeof (_pagemodel) != 'undefined') {
// reqType = FormManager.getActualValue('reqType');
// role = _pagemodel.userRole;
// }
// if (reqType == 'U') {
// return;
// }
// if (role != 'Requester') {
// return;
// }
//
// var abbrevNmValue = null;
// var cntryCd = null;
// var singleIndValue = null;
// var departmentNumValue = null;
//
// var reqId = FormManager.getActualValue('reqId');
// var qParams = {
// REQ_ID : reqId,
// ADDR_TYPE : "ZS01",
// };
// var abbrevNmResult = cmr.query('ADDR.GET.CUSTNM1.BY_REQID_ADDRTYP', qParams);
// abbrevNmValue = abbrevNmResult.ret1;
//
// var landCntryResult = cmr.query('ADDR.GET.LANDEDCNTRY.BY_REQID_ADDRTYPE',
// qParams);
// cntryCd = landCntryResult.ret1;
//
// var custSubGrp = FormManager.getActualValue('custSubGrp');
// if (custSubGrp == "INTER" || custSubGrp == "CBTER") {
// if (cntryCd == "DZ") {
// departmentNumValue = "0371";
// } else if (cntryCd == "TN") {
// departmentNumValue = "0382";
// } else if (cntryCd == "YT" || cntryCd == "RE" || cntryCd == "VU") {
// departmentNumValue = "0381";
// } else if (cntryCd == "MQ") {
// departmentNumValue = "0385";
// } else if (cntryCd == "GP") {
// departmentNumValue = "0392";
// } else if (cntryCd == "GF" || cntryCd == "PM") {
// departmentNumValue = "0388";
// } else if (cntryCd == "NC" || cntryCd == "PF") {
// departmentNumValue = "0386";
// } else {
// departmentNumValue = FormManager.getActualValue('ibmDeptCostCenter');
// }
// singleIndValue = departmentNumValue;
// } else if (custSubGrp == "BPIEU" || custSubGrp == "BPUEU" || custSubGrp ==
// "CBIEU" || custSubGrp == "CBUEU") {
// singleIndValue = "R5";
// } else if (custSubGrp == "CBTSO" || custSubGrp == "INTSO") {
// singleIndValue = "FM";
// } else if (custSubGrp == "CBIFF" || custSubGrp == "CBIFL" || custSubGrp ==
// "CBFIN" || custSubGrp == "LCIFF" || custSubGrp == "LCIFL"
// || custSubGrp == "OTFIN") {
// singleIndValue = "F3";
// } else if (custSubGrp == "LEASE" || custSubGrp == "CBASE") {
// singleIndValue = "L3";
// } else {
// singleIndValue = "D3";
// }
// // if (abbrevNmValue != null && abbrevNmValue.length > 18 && (custSubGrp !=
// // "INTER" && custSubGrp != "CBTER")) {
// // abbrevNmValue = abbrevNmValue.substring(0, 18);
// // } else if (abbrevNmValue != null && abbrevNmValue.length < 18 &&
// // (custSubGrp != "INTER" && custSubGrp != "CBTER")) {
// // for ( var i = abbrevNmValue.length; i < 18; i++) {
// // abbrevNmValue += ' ';
// // }
// // } else if (abbrevNmValue != null && abbrevNmValue.length > 16 &&
// // (custSubGrp == "INTER" || custSubGrp == "CBTER")) {
// // abbrevNmValue = abbrevNmValue.substring(0, 16);
// // } else if (abbrevNmValue != null && abbrevNmValue.length < 16 &&
// // (custSubGrp == "INTER" || custSubGrp == "CBTER")) {
// // for ( var i = abbrevNmValue.length; i < 16; i++) {
// // abbrevNmValue += ' ';
// // }
// // }
//
// // story 1507805 - set single identificateur 'DF' if having address H
// var checkZD02QParams = {
// REQ_ID : reqId,
// ADDR_TYPE : "ZD02",
// };
// var checkZD02Result = cmr.query('GET.ADDR_BY_REQID_TYPE', checkZD02QParams);
// var checkZD02Value = checkZD02Result.ret1;
// if (checkZD02Value) {
// singleIndValue = 'DF';
// }
//
// if (singleIndValue != null && abbrevNmValue != null) {
// var _abbrevNmValue = abbrevNmValue + ' ' + singleIndValue;
// if (_abbrevNmValue.length > 22) {
// _abbrevNmValue = abbrevNmValue.substring(0, (21 - singleIndValue.length)) + '
// ' + singleIndValue;
// } else if (_abbrevNmValue.length < 22) {
// for (var i = _abbrevNmValue.length; i < (22 - singleIndValue.length); i++) {
// abbrevNmValue += ' ';
// }
// _abbrevNmValue = abbrevNmValue + ' ' + singleIndValue;
// }
// FormManager.setValue('abbrevNm', _abbrevNmValue);
// }
// }

function setAbbrevLocnOnCustSubGrpChange() {
  var reqType = null;
  var role = null;
  if (typeof (_pagemodel) != 'undefined') {
    reqType = FormManager.getActualValue('reqType');
    role = _pagemodel.userRole;
  }
  if (reqType == 'U') {
    return;
  }
  if (role != 'Requester') {
    return;
  }

  var abbrevLocnValue = null;
  var countryUse = null;
  var countyCd = null;
  countryUse = FormManager.getActualValue('countryUse');
  if (countryUse.length > 3) {
    countyCd = countryUse.substring(3, 5);
  } else {
    countyCd = "FR";
  }

  var custGrp = FormManager.getActualValue('custGrp');
  if (custGrp == null) {
    return;
  } else if (custGrp == "LOCAL") {
    if (countyCd != "VU" && countyCd != "PF" && countyCd != "YT" && countyCd != "NC" && countyCd != "WF" && countyCd != "AD" && countyCd != "DZ" && countyCd != "TN") {
      var reqId = FormManager.getActualValue('reqId');
      var qParams = {
        REQ_ID : reqId,
        ADDR_TYPE : "ZS01",
      };
      var _result = cmr.query('ADDR.GET.CITY1.BY_REQID_ADDRTYP', qParams);
      abbrevLocnValue = _result.ret1;
    } else if (countyCd == "VU") {
      abbrevLocnValue = "Vanuatu";
    } else if (countyCd == "PF") {
      abbrevLocnValue = "Polynesie Francaise";
    } else if (countyCd == "YT") {
      abbrevLocnValue = "Mayotte";
    } else if (countyCd == "NC") {
      abbrevLocnValue = "Noumea";
    } else if (countyCd == "WF") {
      abbrevLocnValue = "Wallis & Futuna";
    } else if (countyCd == "AD") {
      abbrevLocnValue = "Andorra";
    } else if (countyCd == "DZ") {
      abbrevLocnValue = "Algeria";
    } else if (countyCd == "TN") {
      abbrevLocnValue = "Tunisia";
    }
  } else if (custGrp == "CROSS") {
    var reqId = FormManager.getActualValue('reqId');
    var qParams = {
      REQ_ID : reqId,
      ADDR_TYPE : 'ZS01',
    };
    var _result = cmr.query('ADDR.GET.LANDCNTRY.BY_REQID_ADDRTYP', qParams);
    abbrevLocnValue = _result.ret1;
  }

  if (abbrevLocnValue != null && abbrevLocnValue.length > 12) {
    abbrevLocnValue = abbrevLocnValue.substring(0, 12);
  }
  FormManager.setValue('abbrevLocn', abbrevLocnValue);

}
// function setDummySIRETOnCustSubGrpChange() {
// var reqType = null;
// var role = null;
// if (typeof (_pagemodel) != 'undefined') {
// reqType = FormManager.getActualValue('reqType');
// role = _pagemodel.userRole;
// }
// if (reqType == 'U') {
// return;
// }
//
// var dummySIRETValue = null;
// var countyCd = "";
// var city = "";
// var postCd = "";
// var custLocNumValue = "";
// var countryUse = FormManager.getActualValue('countryUse');
// var custGrp = FormManager.getActualValue('custGrp');
// var custSubGrp = FormManager.getActualValue('custSubGrp');
//
// // set siret mandatory for processor, sub region 'France', all scenarios
// // except government, private person and IBM employee.
// // if (role == 'Processor') {
// // if (custSubGrp != "GOVRN"&&custSubGrp != "PRICU"&&custSubGrp !=
// // "IBMEM"&&custSubGrp != "CBVRN"&&custSubGrp != "CBICU"&&custSubGrp !=
// // "CBMEM")
// // if (countryUse == "706")
// // FormManager.addValidator('taxCd1', Validators.REQUIRED, [ 'SIRET' ],
// // 'MAIN_CUST_TAB');
// // return;
// // }
//
// var reqId = FormManager.getActualValue('reqId');
// var qParams = {
// REQ_ID : reqId,
// ADDR_TYPE : "ZS01",
// };
// var landCntryResult = cmr.query('ADDR.GET.CNTRY_CITY_PSCD.BY_REQID_ADDRTYPE',
// qParams);
// countyCd = landCntryResult.ret1;
// city = landCntryResult.ret2;
// postCd = landCntryResult.ret3;
//
// if (typeof (countyCd) != 'undefined' && countyCd != null && countyCd != 'FR')
// {
// var cntyQParams = {
// LANDED_CNTRY : countyCd,
// };
// var custLocByCntryResultCnty = cmr.query('CUSLOC.GET.LOCN_BY_CNTRY',
// cntyQParams);
// custLocNumValue = custLocByCntryResultCnty.ret1;
// }
// if ((custLocNumValue == '' || custLocNumValue == 'undefined' ||
// custLocNumValue == null) && city != null && postCd != null) {
// var cityQParams = {
// CITY : city,
// POST_CD : postCd,
// };
// var custLocByCityResultCity = cmr.query('CUSLOC.GET.LOCN_BY_CITY_PSCD',
// cityQParams);
// custLocNumValue = custLocByCityResultCity.ret1;
// }
// if (typeof (custLocNumValue) == 'undefined' || custLocNumValue == null)
// custLocNumValue = "";
//
// FormManager.resetValidations('taxCd1');
// if (custSubGrp == "COMME" || custSubGrp == "FIBAB") {
// if (countryUse == "706MC" || countryUse == "706VU" || countryUse == "706PF"
// || countryUse == "706YT" || countryUse == "706NC"
// || countryUse == "706AD" || countryUse == "706DZ" || countryUse == "706TN") {
// if (custLocNumValue != '') {
// dummySIRETValue = "SCxxxxxx0" + custLocNumValue;
// FormManager.setValue('taxCd1', dummySIRETValue);
// }
// }
// }
//
// // else if (custSubGrp == "PRICU") {
// // if(custLocNumValue!=''){
// // dummySIRETValue = "SCxxxxxx0" + custLocNumValue;
// // FormManager.setValue('taxCd1', dummySIRETValue);
// // }
//
// else if (custSubGrp == 'PRICU' || custSubGrp == 'CBICU' || custSubGrp ==
// 'IBMEM' || custSubGrp == 'CBIEM') {
// FormManager.setValue('taxCd1', '');
// FormManager.readOnly('taxCd1');
// }
//
// // else{
// // FormManager.setValue('taxCd1', "");
// // }
// if (custGrp == "CROSS") {
// if (custLocNumValue != '') {
// dummySIRETValue = "SCxxxxxx0" + custLocNumValue;
// FormManager.setValue('taxCd1', dummySIRETValue);
// }
// }
// // if (custSubGrp != "GOVRN"&&custSubGrp != "PRICU"&&custSubGrp !=
// // "IBMEM"&&custSubGrp != "CBVRN"&&custSubGrp != "CBICU"&&custSubGrp !=
// // "CBMEM")
// // if (countryUse == "706")
// // FormManager.addValidator('taxCd1', Validators.REQUIRED, [ 'SIRET' ],
// // 'MAIN_CUST_TAB');
// if (role != 'Requester') {
// return;
// } else {
// if (countryUse == "706") {
// if (custGrp == "LOCAL") {
// if (custSubGrp == "PRICU" || custSubGrp == "IBMEM") {
// FormManager.removeValidator('taxCd1', Validators.REQUIRED);
// } else {
// FormManager.addValidator('taxCd1', Validators.REQUIRED, [ 'SIRET' ],
// 'MAIN_CUST_TAB');
// }
// } else if (custGrp == "CROSS") {
// FormManager.removeValidator('taxCd1', Validators.REQUIRED);
// }
// } else {
// FormManager.removeValidator('taxCd1', Validators.REQUIRED);
// }
// }
// }
function setDummySIRETOnCustSubGrpChange(value) {
  var reqType = null;
  if (typeof (_pagemodel) != 'undefined') {
    reqType = FormManager.getActualValue('reqType');
  }
  if (reqType == 'U' || FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var sourceSystId = FormManager.getActualValue('sourceSystId');
  // PayGo_check
  var qParams = {
    SYST_ID : sourceSystId
  };
  var paygoUser = cmr.query('PAYGO.CHECK.CRN', qParams);
  var countpaygo = paygoUser.ret1;
  if ((Number(countpaygo) == 1 && role == 'Processor')) {
    FormManager.removeValidator('taxCd1', Validators.REQUIRED);
  } else if (value == "COMME" || value == "HOSTC" || value == "THDPT" || value == "BUSPR" || value == "GOVRN" || value == "INTER" || value == "INTSO" || value == "LCFIN") {
    FormManager.enable('taxCd1');
    FormManager.addValidator('taxCd1', Validators.REQUIRED, [ 'SIRET' ], 'MAIN_CUST_TAB');
  } else if (value == 'PRICU' || value == 'XBLUM' || value == 'IBMEM' || value == 'CBIEM') {
    FormManager.removeValidator('taxCd1', Validators.REQUIRED);
    FormManager.setValue('taxCd1', '');
    FormManager.readOnly('taxCd1');
  } else {
    FormManager.removeValidator('taxCd1', Validators.REQUIRED);
    var reqId = FormManager.getActualValue('reqId');
    var qParams = {
      REQ_ID : reqId,
      ADDR_TYPE : "ZS01",
    };
    var landCntryResult = cmr.query('ADDR.GET.CNTRY_CITY_PSCD.BY_REQID_ADDRTYPE', qParams);
    var landCntry = landCntryResult.ret1;
    var siretEnabledCntryList = [ 'GP', 'GF', 'MQ', 'RE', 'PM', 'KM', 'FR' ];
    if (siretEnabledCntryList.indexOf(landCntry) > -1) {
      FormManager.enable('taxCd1');
    } else {
      if (!(value != _pagemodel.custSubGrp && value == undefined)) {
        FormManager.setValue('taxCd1', '');
        FormManager.readOnly('taxCd1');
      }
    }
  }
}
// function setAbbrevNmLocnOnAddressSave(cntry, addressMode, saving, finalSave,
// force) {
// var reqType = null;
// var role = null;
// if (typeof (_pagemodel) != 'undefined') {
// reqType = FormManager.getActualValue('reqType');
// role = _pagemodel.userRole;
// }
// if (cmr.currentRequestType == 'U') {
// return;
// }
// if (role != 'Requester') {
// console.log("Processor, return");
// return;
// }
// if (finalSave || force || addressMode == 'ZS01' || addressMode == 'ZD02') {
// var copyTypes = document.getElementsByName('copyTypes');
// var copyingToA = false;
// var copyToAddrH = false;
// var abbName = FormManager.getActualValue("custNm1");
// abbName = abbName.length > 22 ? abbName.substring(0, 21) : abbName;
// FormManager.setValue('abbrevNm', abbName);
// if (copyTypes != null && copyTypes.length > 0) {
// copyTypes.forEach(function(input, i) {
// if (input.value == 'ZS01' && input.checked) {
// copyingToA = true;
// }
// if (input.value == 'ZD02' && input.checked) {
// copyToAddrH = true;
// }
// });
// }
// var addrType = FormManager.getActualValue('addrType');
// if (addrType == 'ZS01' || copyingToA) {
// addAbbrevLocnlogic();
// add32SBODependcyOnPostCdOnAddrSave();
// addVATScenarioOnAddrSave();
// setTaxCdOnAddrSave();
// }
// if (addrType == 'ZS01' || addrType == 'ZS01' || copyingToA || copyToAddrH) {
// addAbbrevNmlogic();
// }
// }
// }
function setAbbrevNmLocnOnAddressSave(cntry, addressMode, saving, finalSave, force) {
  var reqType = FormManager.getActualValue('reqType');
  if (reqType == 'U') {
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
    if (addrType == 'ZS01' || copyingToA) {
      addAbbrevLocnlogic();
      // add32SBODependcyOnPostCdOnAddrSave();
      addVATScenarioOnAddrSave();
      setTaxCdOnAddrSave();
    }
  }
}

function updateAbbrNameWithZS01() {
  var reqType = FormManager.getActualValue('reqType');
  if (reqType == 'U') {
    return;
  }
  var abbrName = FormManager.getActualValue("abbrevNm");
  var newAddrName1 = FormManager.getActualValue("custNm1");
  var role = FormManager.getActualValue('userRole').toUpperCase();

  if ('PROCESSOR' == role) {
    var zs01ReqId = FormManager.getActualValue('reqId');
    var qParams = {
      REQ_ID : zs01ReqId,
    };
    var result = cmr.query('DATA.GET.ABBREV_NM.BY_REQID', qParams);
    var oldAbbrName = result.ret1;

    if (oldAbbrName != '' || oldAbbrName != null) {
      FormManager.setValue('abbrevNm', oldAbbrName);
      return;
    }

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
    if (newAddrName1 != undefined && newAddrName1 != null && newAddrName1 != '') {
      FormManager.setValue('abbrevNm', newAddrName1);
    } else if (oldAddrName != null && oldAddrName != undefined && oldAddrName != '') {
      FormManager.setValue('abbrevNm', oldAddrName.substr(0, 22));
    }
  }
}

function updateAbbrNameWithZS01_ZI01() {
  var reqType = FormManager.getActualValue('reqType');
  if (reqType == 'U') {
    return;
  }
  var abbrName = FormManager.getActualValue("abbrevNm");
  var newAddrName1 = FormManager.getActualValue("custNm1");
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var reqId = FormManager.getActualValue('reqId');

  if ('PROCESSOR' == role) {
    var qParams = {
      REQ_ID : reqId,
    };
    var result = cmr.query('DATA.GET.ABBREV_NM.BY_REQID', qParams);
    var oldAbbrName = result.ret1;

    if (oldAbbrName != '' || oldAbbrName != null) {
      FormManager.setValue('abbrevNm', oldAbbrName);
      return;
    }

    if (abbrName != oldAbbrName) {
      return;
    }
  }

  var qPZs01 = {
    REQ_ID : reqId,
    ADDR_TYPE : "ZS01",
  };
  var qPZi01 = {
    REQ_ID : reqId,
    ADDR_TYPE : "ZI01",
  };
  var oldAddrName = null;
  var resultzs01 = cmr.query('ADDR.GET.CUSTNM1.BY_REQID_ADDRTYP', qPZs01);
  var resultzi01 = cmr.query('ADDR.GET.CUSTNM1.BY_REQID_ADDRTYP', qPZi01);

  var zs01Nm = (resultzs01.ret1 != null && resultzs01.ret1 != undefined) ? resultzs01.ret1 : "";
  var zi01Nm = (resultzi01.ret1 != null && resultzi01.ret1 != undefined) ? resultzi01.ret1 : "";

  if (zi01Nm != "") {
    var offset = 0;
    if (zs01Nm.length < 11) {
      offset = 11 - zs01Nm.length;
    }
    var zs01NmTrim = zs01Nm.length > 11 ? zs01Nm.substring(0, 11) : zs01Nm;
    var zi01NmTrim = zi01Nm.length > (offset + 10) ? zi01Nm.substring(0, offset + 10) : zi01Nm;
    oldAddrName = zs01NmTrim + '/' + zi01NmTrim;
  } else {
    oldAddrName = zs01Nm;
  }

  if (oldAddrName != newAddrName1) {
    if (newAddrName1 != null && newAddrName1.length > 22) {
      newAddrName1 = newAddrName1.substr(0, 22);
    }
    if (newAddrName1 != undefined && newAddrName1 != "" && newAddrName1 != null) {
      FormManager.setValue('abbrevNm', newAddrName1);
    } else if (oldAddrName != null && oldAddrName != undefined && oldAddrName != '') {
      FormManager.setValue('abbrevNm', oldAddrName.substr(0, 22));
    }
  }
}

function updateAbbrNameWithTPZS01_ZI01() {
  var reqType = FormManager.getActualValue('reqType');
  if (reqType == 'U') {
    return;
  }
  var abbrName = FormManager.getActualValue("abbrevNm");
  var newAddrName1 = FormManager.getActualValue("custNm1");
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var reqId = FormManager.getActualValue('reqId');

  if ('PROCESSOR' == role) {
    var qParams = {
      REQ_ID : reqId,
    };
    var result = cmr.query('DATA.GET.ABBREV_NM.BY_REQID', qParams);
    var oldAbbrName = result.ret1;

    if (oldAbbrName != '' || oldAbbrName != null) {
      FormManager.setValue('abbrevNm', oldAbbrName);
      return;
    }

    if (abbrName != oldAbbrName) {
      return;
    }
  }

  var qPZs01 = {
    REQ_ID : reqId,
    ADDR_TYPE : "ZS01",
  };
  var qPZi01 = {
    REQ_ID : reqId,
    ADDR_TYPE : "ZI01",
  };
  var oldAddrName = null;
  var resultzs01 = cmr.query('ADDR.GET.CUSTNM1.BY_REQID_ADDRTYP', qPZs01);
  var resultzi01 = cmr.query('ADDR.GET.CUSTNM1.BY_REQID_ADDRTYP', qPZi01);

  var zs01Nm = (resultzs01.ret1 != null && resultzs01.ret1 != undefined) ? resultzs01.ret1 : "";
  var zi01Nm = (resultzi01.ret1 != null && resultzi01.ret1 != undefined) ? resultzi01.ret1 : "";

  if (zi01Nm != "") {
    var offset = 0;
    if (zs01Nm.length < 11) {
      offset = 11 - zs01Nm.length;
    }
    var zs01NmTrim = zs01Nm.length > 10 ? zs01Nm.substring(0, 10) : zs01Nm;
    var zi01NmTrim = zi01Nm.length > (offset + 11) ? zi01Nm.substring(0, offset + 11) : zi01Nm;
    oldAddrName = zs01NmTrim + '/' + zi01NmTrim;
  } else {
    oldAddrName = zs01Nm;
  }

  if (oldAddrName != newAddrName1) {
    if (newAddrName1 != null && newAddrName1.length > 22) {
      newAddrName1 = newAddrName1.substr(0, 22);
    }
    if (newAddrName1 != undefined && newAddrName1 != "" && newAddrName1 != null) {
      FormManager.setValue('abbrevNm', newAddrName1);
    } else if (oldAddrName != null && oldAddrName != undefined && oldAddrName != '') {
      FormManager.setValue('abbrevNm', oldAddrName.substr(0, 22));
    }
  }
}

// function addAbbrevNmlogic() {
// var abbrevNmValue = null;
// var cntryCd = null;
// var singleIndValue = null;
// var departmentNumValue = null;
//
// var reqType = FormManager.getActualValue('reqType');
// var addrType = FormManager.getActualValue('addrType');
// var custSubGrp = FormManager.getActualValue('custSubGrp');
//
// var havingZS01 = false;
// var havingZD02 = false;
// if (CmrGrid.GRIDS.ADDRESS_GRID_GRID &&
// CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
// var record = null;
// var type = null;
// for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
// record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
// if (record == null && _allAddressData != null && _allAddressData[i] != null)
// {
// record = _allAddressData[i];
// }
// type = record.addrType;
// if (typeof (type) == 'object') {
// type = type[0];
// }
// if (type == 'ZS01') {
// havingZS01 = true;
// }
// if (type == 'ZD02') {
// havingZD02 = true;
// }
// }
// }
// console.log("havingZS01 = " + havingZS01);
// console.log("havingZD02 = " + havingZD02);
//
// var copyTypes = document.getElementsByName('copyTypes');
// var copyToZS01 = false;
// var copyToZD02 = false;
// if (copyTypes != null && copyTypes.length > 0) {
// copyTypes.forEach(function(input, i) {
// if (input.value == 'ZS01' && input.checked) {
// copyToZS01 = true;
// }
// if (input.value == 'ZD02' && input.checked) {
// copyToZD02 = true;
// }
// });
// }
// console.log("copyToZS01 = " + copyToZS01);
// console.log("copyToZD02 = " + copyToZD02);
//
// if (reqType == "C" && addrType == "ZS01") {
// abbrevNmValue = FormManager.getActualValue('custNm1');
// cntryCd = FormManager.getActualValue('landCntry');
//
// if (custSubGrp == "INTER" || custSubGrp == "CBTER") {
// if (cntryCd == "DZ") {
// departmentNumValue = "0371";
// } else if (cntryCd == "TN") {
// departmentNumValue = "0382";
// } else if (cntryCd == "YT" || cntryCd == "RE" || cntryCd == "VU") {
// departmentNumValue = "0381";
// } else if (cntryCd == "MQ") {
// departmentNumValue = "0385";
// } else if (cntryCd == "GP") {
// departmentNumValue = "0392";
// } else if (cntryCd == "GF" || cntryCd == "PM") {
// departmentNumValue = "0388";
// } else if (cntryCd == "NC" || cntryCd == "PF") {
// departmentNumValue = "0386";
// } else {
// departmentNumValue = FormManager.getActualValue('ibmDeptCostCenter');
// }
// singleIndValue = departmentNumValue;
// } else if (custSubGrp == "BPIEU" || custSubGrp == "BPUEU" || custSubGrp ==
// "CBIEU" || custSubGrp == "CBUEU") {
// singleIndValue = "R5";
// } else if (custSubGrp == "CBTSO" || custSubGrp == "INTSO") {
// singleIndValue = "FM";
// } else if (custSubGrp == "CBIFF" || custSubGrp == "CBIFL" || custSubGrp ==
// "CBFIN" || custSubGrp == "LCIFF" || custSubGrp == "LCIFL"
// || custSubGrp == "OTFIN") {
// singleIndValue = "F3";
// } else if (custSubGrp == "LEASE" || custSubGrp == "CBASE") {
// singleIndValue = "L3";
// } else {
// singleIndValue = "D3";
// }
// if (abbrevNmValue != null && abbrevNmValue.length > 19 && (custSubGrp !=
// "INTER" && custSubGrp != "CBTER")) {
// abbrevNmValue = abbrevNmValue.substring(0, 19);
// } else if (abbrevNmValue != null && abbrevNmValue.length < 19 && (custSubGrp
// != "INTER" && custSubGrp != "CBTER")) {
// for (var i = abbrevNmValue.length; i < 19; i++) {
// abbrevNmValue += ' ';
// }
// } else if (abbrevNmValue != null && abbrevNmValue.length > 17 && (custSubGrp
// == "INTER" || custSubGrp == "CBTER")) {
// abbrevNmValue = abbrevNmValue.substring(0, 17);
// } else if (abbrevNmValue != null && abbrevNmValue.length < 17 && (custSubGrp
// == "INTER" || custSubGrp == "CBTER")) {
// for (var i = abbrevNmValue.length; i < 17; i++) {
// abbrevNmValue += ' ';
// }
// }
//
// // story 1507805 - set single identificateur 'DF' if having address H or
// // copy to address H
// if (havingZD02 || copyToZD02) {
// singleIndValue = "DF";
// }
// console.log("singleIndValue = " + singleIndValue);
//
// if (singleIndValue != null && abbrevNmValue != null) {
// abbrevNmValue = abbrevNmValue + ' ' + singleIndValue;
// FormManager.setValue('abbrevNm', abbrevNmValue);
// }
// } else if (reqType == "C" && addrType == "ZD02") {
// if (havingZS01) {
// var reqId = FormManager.getActualValue('reqId');
// var qParams = {
// REQ_ID : reqId,
// ADDR_TYPE : "ZS01",
// };
// var abbrevNmResult = cmr.query('ADDR.GET.CUSTNM1.BY_REQID_ADDRTYP', qParams);
// abbrevNmValue = abbrevNmResult.ret1;
// } else if (copyToZS01) {
// abbrevNmValue = FormManager.getActualValue('custNm1');
// }
//
// if (abbrevNmValue != null && abbrevNmValue.length > 19 && (custSubGrp !=
// "INTER" && custSubGrp != "CBTER")) {
// abbrevNmValue = abbrevNmValue.substring(0, 19);
// } else if (abbrevNmValue != null && abbrevNmValue.length < 19 && (custSubGrp
// != "INTER" && custSubGrp != "CBTER")) {
// for (var i = abbrevNmValue.length; i < 19; i++) {
// abbrevNmValue += ' ';
// }
// } else if (abbrevNmValue != null && abbrevNmValue.length > 17 && (custSubGrp
// == "INTER" || custSubGrp == "CBTER")) {
// abbrevNmValue = abbrevNmValue.substring(0, 17);
// } else if (abbrevNmValue != null && abbrevNmValue.length < 17 && (custSubGrp
// == "INTER" || custSubGrp == "CBTER")) {
// for (var i = abbrevNmValue.length; i < 17; i++) {
// abbrevNmValue += ' ';
// }
// }
// singleIndValue = "DF";
// if (singleIndValue != null && abbrevNmValue != null) {
// abbrevNmValue = abbrevNmValue + ' ' + singleIndValue;
// FormManager.setValue('abbrevNm', abbrevNmValue);
// }
// }
// }

function addAbbrevLocnlogic() {
  var abbrevLocnValue = null;
  var countryUse = null;
  var countyCd = null;
  var cntryDesc = null;

  var role = null;
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }
  if (role != 'Requester') {
    return;
  }

  var reqType = FormManager.getActualValue('reqType');
  var addrType = FormManager.getActualValue('addrType');
  if (reqType == "C" && addrType == "ZS01") {

    countryUse = FormManager.getActualValue('countryUse');
    if (countryUse.length > 3) {
      countyCd = countryUse.substring(3, 5);
    } else {
      countyCd = "FR";
    }

    var custGrp = FormManager.getActualValue('custGrp');
    if (custGrp == null) {
      return;
    } else if (custGrp == "LOCAL") {
      if (countyCd != "VU" && countyCd != "PF" && countyCd != "YT" && countyCd != "NC" && countyCd != "WF" && countyCd != "AD" && countyCd != "DZ" && countyCd != "TN") {
        abbrevLocnValue = FormManager.getActualValue('city1');
      } else if (countyCd == "VU") {
        abbrevLocnValue = "Vanuatu";
      } else if (countyCd == "PF") {
        abbrevLocnValue = "Polynesie Francaise";
      } else if (countyCd == "YT") {
        abbrevLocnValue = "Mayotte";
      } else if (countyCd == "NC") {
        abbrevLocnValue = "Noumea";
      } else if (countyCd == "WF") {
        abbrevLocnValue = "Wallis & Futuna";
      } else if (countyCd == "AD") {
        abbrevLocnValue = "Andorra";
      } else if (countyCd == "DZ") {
        abbrevLocnValue = "Algeria";
      } else if (countyCd == "TN") {
        abbrevLocnValue = "Tunisia";
      }
    } else if (custGrp == "CROSS") {
      cntryDesc = getLandCntryDesc(FormManager.getActualValue('landCntry'));
      abbrevLocnValue = cntryDesc;
    }

    if (abbrevLocnValue != null && abbrevLocnValue.length > 12) {
      abbrevLocnValue = abbrevLocnValue.substring(0, 12);
    }
    FormManager.setValue('abbrevLocn', abbrevLocnValue);
  }
}

function getLandCntryDesc(cntryCd) {
  if (cntryCd == null) {
    return;
  }
  var reqParam = {
    COUNTRY : cntryCd,
  };
  var results = cmr.query('GET.CNTRY_DESC', reqParam);
  var _cntryDesc = results.ret1;
  return _cntryDesc;
}

function add32SBODependcyOnPostCdOnAddrSave() {
  var role = null;
  var reqType = null;
  var postCd = null;
  var cntryCd = null;
  var sboValue = null;

  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
    reqType = FormManager.getActualValue('reqType');
  } else {
    return;
  }
  if (role != 'Requester' && role != 'Processor') {// CMR-221
    return;
  }
  if (reqType == 'U') {
    return;
  }

  var countyCd = null;
  var countryUse = FormManager.getActualValue('landCntry');
  if (countryUse.length > 1) {
    countyCd = countryUse;
  } else {
    countyCd = "FR";
  }
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (!((countyCd == "FR" || countyCd == "KM" || countyCd == "WF") && (custSubGrp == 'COMME' || custSubGrp == 'PRICU' || custSubGrp == 'GOVRN' || custSubGrp == 'INTER' || custSubGrp == 'LCIFF'
      || custSubGrp == 'LCIFL' || custSubGrp == 'OTFIN' || custSubGrp == 'IBMEM' || custSubGrp == 'LCOEM' || custSubGrp == 'INTSO' || custSubGrp == 'HOSTC' || custSubGrp == 'THDPT'
      || custSubGrp == 'CBTER' || custSubGrp == 'LCFIN'))) {
    return;
  }

  var isuCd = FormManager.getActualValue('isuCd');
  if (isuCd != '32') {
    return;
  }

  FormManager.readOnly('salesBusOffCd');
  FormManager.setValue('salesBusOffCd', '');

  cntryCd = FormManager.getActualValue('landCntry');
  postCd = FormManager.getActualValue('postCd');
  if (postCd != null && postCd.length > 2) {
    postCd = postCd.substring(0, 2);
  }

  var cntryUpdateByParams = null;
  if (included(cntryCd)) {
    cntryUpdateByParams = {
      ISSUING_CNTRY : '706',
      UPDATE_BY_ID : '%' + cntryCd + '%',
      ISU_CD : '32',
    };
    var sboResult = cmr.query('GET.SBO.BY_CNTRY_UPDATED_BY', cntryUpdateByParams);
    sboValue = sboResult.ret1;
    sboValue = sboValue + sboValue;// CMR-221
    FormManager.setValue('salesBusOffCd', sboValue);
  } else if (!included(cntryCd)) {
    if (postCd != '75' && postCd != '77' && postCd != '78' && postCd != '91' && postCd != '92' && postCd != '93' && postCd != '94' && postCd != '95') {
      cntryUpdateByParams = {
        ISSUING_CNTRY : '706',
        UPDATE_BY_ID : '%' + postCd + '%',
        ISU_CD : '32',
      };
      var sboResult = cmr.query('GET.SBO.BY_CNTRY_UPDATED_BY', cntryUpdateByParams);
      sboValue = sboResult.ret1;
      sboValue = sboValue + sboValue;// CMR-221
      FormManager.setValue('salesBusOffCd', sboValue);
    } else if (postCd == '75' || postCd == '77' || postCd == '78' || postCd == '91' || postCd == '92' || postCd == '93' || postCd == '94' || postCd == '95') {
      var insustry = FormManager.getActualValue('subIndustryCd').substring(0, 1);
      if (insustry == 'A' || insustry == 'B' || insustry == 'C' || insustry == 'D' || insustry == 'F' || insustry == 'H' || insustry == 'K' || insustry == 'N' || insustry == 'R' || insustry == 'S'
          || insustry == 'T' || insustry == 'W') {
        FormManager.setValue('salesBusOffCd', '01D01D');// CMR-221
      } else if (insustry == 'E' || insustry == 'G' || insustry == 'J' || insustry == 'L' || insustry == 'M' || insustry == 'P' || insustry == 'U' || insustry == 'V' || insustry == 'X') {
        FormManager.setValue('salesBusOffCd', '01M01M');// CMR-221
      }
    }
  }

  // SBO scenario logic should over write the 32 postal code logic
  setSBOOnScenarioLD();
}
// function addVATScenarioOnAddrSave() {
// var role = null;
// if (typeof (_pagemodel) != 'undefined') {
// role = _pagemodel.userRole;
// } else {
// return;
// }
// if (cmr.currentRequestType == 'U') {
// return;
// }
// if (role == 'Processor') {
// return;
// }
// var countyCd = null;
// var countryUse = FormManager.getActualValue('countryUse');
// if (countryUse.length > 3) {
// countyCd = countryUse.substring(3, 5);
// } else {
// countyCd = "FR";
// }
// var custSubGrp = FormManager.getActualValue('custSubGrp');
// if (custSubGrp == '') {
// return;
// } else {
// if (custSubGrp == 'PRICU' || custSubGrp == 'CBICU') {
// FormManager.enable('vat');
// FormManager.removeValidator('vat', Validators.REQUIRED);
//
// } else if (custSubGrp == 'COMME' || custSubGrp == 'FIBAB' || custSubGrp ==
// 'BPIEU' || custSubGrp == 'BPUEU' || custSubGrp == 'GOVRN'
// || custSubGrp == 'INTER' || custSubGrp == 'INTSO' || custSubGrp == 'LCIFF' ||
// custSubGrp == 'LCIFL' || custSubGrp == 'OTFIN'
// || custSubGrp == 'LEASE' || custSubGrp == 'LCOEM' || custSubGrp == 'HOSTC' ||
// custSubGrp == 'THDPT') {
// if (countyCd == 'FR' || countyCd == 'AD') {
// FormManager.enable('vat');
// FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ],
// 'MAIN_CUST_TAB');
// } else {
// FormManager.removeValidator('vat', Validators.REQUIRED);
// FormManager.enable('vat');
// }
//
// } else if (custSubGrp == 'CBMME' || custSubGrp == 'CBBAB' || custSubGrp ==
// 'CBIEU' || custSubGrp == 'CBUEU' || custSubGrp == 'CBVRN'
// || custSubGrp == 'CBTER' || custSubGrp == 'CBTSO' || custSubGrp == 'CBIFF' ||
// custSubGrp == 'CBIFL' || custSubGrp == 'CBFIN'
// || custSubGrp == 'CBASE' || custSubGrp == 'CBOEM' || custSubGrp == 'CBSTC' ||
// custSubGrp == 'CBDPT') {
// var isVATEUCntry = false;
// var landCntry = FormManager.getActualValue('landCntry');
// for (var i = 0; i < VAT_EU_COUNTRY.length; i++) {
// if (VAT_EU_COUNTRY[i] == landCntry) {
// isVATEUCntry = true;
// break;
// }
// }
// if (isVATEUCntry == true) {
// FormManager.enable('vat');
// FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ],
// 'MAIN_CUST_TAB');
// } else {
// FormManager.removeValidator('vat', Validators.REQUIRED);
// FormManager.enable('vat');
// }
// }
// }
// }
function addVATScenarioOnAddrSave() {
  var role = null;
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  } else {
    return;
  }
  if (cmr.currentRequestType == 'U') {
    return;
  }
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  FormManager.enable('vatExempt');
  if (dijit.byId('vatExempt') != undefined && dijit.byId('vatExempt') != null) {
    FormManager.getField('vatExempt').set('checked', false);
  }
  var dbCustSubGrp = _pagemodel.custSubGrp;
  var dbVatExempt = _pagemodel.vatExempt;
  if (custSubGrp == dbCustSubGrp && dbVatExempt == 'Y' && dijit.byId('vatExempt') != undefined && dijit.byId('vatExempt') != null) {
    FormManager.getField('vatExempt').set('checked', true);
    FormManager.readOnly('vat');
    FormManager.removeValidator('vat', Validators.REQUIRED);
    return;
  }
  if (custSubGrp == '') {
    return;
  } else {
    if (custSubGrp == 'PRICU' || custSubGrp == 'XBLUM' || custSubGrp == 'IBMEM' || custSubGrp == 'CBIEM') {
      FormManager.readOnly('vat');
      FormManager.removeValidator('vat', Validators.REQUIRED);
      FormManager.readOnly('vatExempt');
      if (dijit.byId('vatExempt') != undefined && dijit.byId('vatExempt') != null) {
        FormManager.getField('vatExempt').set('checked', true);
      }
    } else if (custSubGrp == 'GOVRN' || custSubGrp == 'CBVRN' || custSubGrp == 'COMME' || custSubGrp == 'HOSTC' || custSubGrp == 'THDPT') {
      FormManager.enable('vat');
      FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
    } else if (custSubGrp == 'INTER' || custSubGrp == 'INTSO' || custSubGrp == 'BUSPR' || custSubGrp == 'LCFIN') {
      FormManager.removeValidator('vat', Validators.REQUIRED);
      FormManager.enable('vat');
    } else if (custSubGrp == 'CBMME' || custSubGrp == 'XBUSP' || custSubGrp == 'CBTER' || custSubGrp == 'CBSTC' || custSubGrp == 'CBDPT' || custSubGrp == 'CBTSO' || custSubGrp == 'CBFIN') {
      var isVATEUCntry = false;
      var landCntry = FormManager.getActualValue('landCntry');
      for (var i = 0; i < VAT_EU_COUNTRY.length; i++) {
        if (VAT_EU_COUNTRY[i] == landCntry) {
          isVATEUCntry = true;
          break;
        }
      }
      if (isVATEUCntry == true) {
        FormManager.enable('vat');
        FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
      } else {
        FormManager.removeValidator('vat', Validators.REQUIRED);
        FormManager.enable('vat');
      }
    }
  }
}
function hidePOBox() {
  if (cmr.addressMode == 'newAddress' || cmr.addressMode == 'copyAddress') {
    for (var i = 0; i < _addrTypesForFR.length; i++) {
      _poBOXHandler[i] = null;
      if (_poBOXHandler[i] == null) {
        _poBOXHandler[i] = dojo.connect(FormManager.getField('addrType_' + _addrTypesForFR[i]), 'onClick', function(value) {
          setPOBOX(value);
        });
      }
    }
  }
  if (cmr.addressMode == 'updateAddress') {
    if (FormManager.getActualValue('addrType') == 'ZP01') {
      FormManager.enable('poBox');
    } else {
      FormManager.disable('poBox');
    }
  }
}

function setPOBOX(value) {
  if (FormManager.getField('addrType_ZP01').checked) {
    FormManager.enable('poBox');
  } else {
    FormManager.disable('poBox');
  }
}

function setHideFieldForFR() {
  var countryUse = FormManager.getActualValue('countryUse');
  if (countryUse.length > 3 && countryUse.substring(3, 5) == "DZ") {
    FormManager.show('DoubleCreate', 'DoubleCreate');
  } else {
    FormManager.hide('DoubleCreate', 'DoubleCreate');
  }
  var reqType = FormManager.getActualValue('custSubGrp');
  var requestType = FormManager.getActualValue('reqType');
  if (requestType == 'C') {
    if (reqType == 'INTER' || reqType == 'CBTER' || reqType == 'INTSO' || reqType == 'CBTSO') {
      FormManager.addValidator('ibmDeptCostCenter', Validators.REQUIRED, [ 'Internal Department Number' ], 'MAIN_IBM_TAB');
      FormManager.show('InternalDept', 'InternalDept');
    } else {
      FormManager.resetValidations('ibmDeptCostCenter');
      FormManager.hide('InternalDept', 'InternalDept');
    }
  } else if (requestType == 'U') {
    FormManager.resetValidations('ibmDeptCostCenter');
    FormManager.show('InternalDept', 'InternalDept');
  }

  if ('GOVRN' == reqType || 'CBVRN' == reqType) {
    FormManager.resetValidations('privIndc');
    FormManager.show('PrivIndc', 'PrivIndc');
    FormManager.readOnly('privIndc');
  } else {
    FormManager.hide('PrivIndc', 'PrivIndc');
  }
}

function addCrossBorderValidatorForFR() {
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

        // var cntryRegion = FormManager.getActualValue('countryUse');
        var landCntry = 'FR'; // default to France
        // if (cntryRegion != '' && cntryRegion.length > 3) {
        // landCntry = cntryRegion.substring(3, 5);
        // }

        var reqId = FormManager.getActualValue('reqId');
        var defaultLandCntry = landCntry;
        var result = cmr.query('VALIDATOR.CROSSBORDER', {
          REQID : reqId
        });

        if (result != null && result.ret1 != '' && result.ret1 != undefined && defaultLandCntry != '' && result.ret1 != defaultLandCntry && scenario != 'CROSS') {
          return new ValidationResult(null, false, 'Landed Country value of the Sold-to (Main) Address should be \'' + defaultLandCntry + '\' for Non Cross-Border customers.');
        } else if (result != null && result.ret1 != '' && result.ret1 != undefined && defaultLandCntry != '' && result.ret1 == defaultLandCntry && scenario == 'CROSS') {
          return new ValidationResult(null, false, 'Landed Country value of the Sold-to (Main) Address should not be \'' + defaultLandCntry + '\' for Cross-Border customers.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
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

function canUpdateAddress(value, rowIndex, grid) {
  var rowData = grid.getItem(rowIndex);
  var parCmrNo = rowData.parCmrNo[0];
  if (parCmrNo && parCmrNo.trim() != '') {
    return false;
  }
  return true;
}

function ADDRESS_GRID_showCheck(value, rowIndex, grid) {
  return canRemoveAddress(value, rowIndex, grid);
}

/* End 1430539 */
function setTaxCdOnScnrio() {
  var role = null;
  var reqType = FormManager.getActualValue('reqType');
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }

  if (role == 'Requester' && reqType == 'C') {
    if (FormManager.getActualValue('custGrp') == 'LOCAL') {
      setTaxCdDropDownValuesLOCAL();
    } else if (FormManager.getActualValue('custGrp') == 'CROSS') {
      setTaxCdDropDownValuesCROSS();
    }
  }

}

function setTaxCdDropDownValuesLOCAL() {
  var countryUse = null;
  var countyCd = null;
  countryUse = FormManager.getActualValue('countryUse');
  if (countryUse.length > 3) {
    countyCd = countryUse.substring(3, 5);
  } else {
    countyCd = "FR";
  }
  if (countyCd == "GF") {
    FormManager.enable('taxCd2');
    // FormManager.limitDropdownValues(FormManager.getField('taxCd2'), [ '11',
    // '69' ]);
    if (FormManager.getActualValue('taxCd2') == '') {
      // FormManager.setValue('taxCd2', '69');
    }
  } else if (countyCd == "PM" || countyCd == "YT" || countyCd == "AD" || countyCd == "DZ" || countyCd == "TN") {
    FormManager.enable('taxCd2');
    // FormManager.limitDropdownValues(FormManager.getField('taxCd2'), [ '11',
    // '37' ]);
    if (FormManager.getActualValue('taxCd2') == '') {
      // FormManager.setValue('taxCd2', '37');
    }
  } else if (countyCd == "VU" || countyCd == "WF") {
    FormManager.resetDropdownValues(FormManager.getField('taxCd2'));
    // FormManager.setValue('taxCd2', '36');
    // FormManager.readOnly('taxCd2');
  } else if (countyCd == "GP" || countyCd == "MQ" || countyCd == "RE" || countyCd == "KM") {
    FormManager.resetDropdownValues(FormManager.getField('taxCd2'));
    // FormManager.setValue('taxCd2', '02');
    // FormManager.readOnly('taxCd2');
  } else if (countyCd == "FR" || countyCd == "MC") {
    FormManager.resetDropdownValues(FormManager.getField('taxCd2'));
    // FormManager.setValue('taxCd2', '01');
    // FormManager.readOnly('taxCd2');
  } else if (countyCd == "PF") {
    FormManager.resetDropdownValues(FormManager.getField('taxCd2'));
    // FormManager.setValue('taxCd2', '34');
    // FormManager.readOnly('taxCd2');
  } else if (countyCd == "NC") {
    FormManager.resetDropdownValues(FormManager.getField('taxCd2'));
    // FormManager.setValue('taxCd2', '41');
    // FormManager.readOnly('taxCd2');
  }
}

function setTaxCdDropDownValuesCROSS() {
  var isEUCntry = false;
  var reqId = FormManager.getActualValue('reqId');
  var qParams = {
    REQ_ID : reqId,
    ADDR_TYPE : 'ZS01',
  };
  var _result = cmr.query('ADDR.GET.LANDCNTRY.BY_REQID_ADDRTYP', qParams);
  var landCntry = _result.ret2;
  if (landCntry != null) {
    for (var i = 0; i < EU_COUNTRIES.length; i++) {
      if (EU_COUNTRIES[i] == landCntry) {
        isEUCntry = true;
        break;
      }
    }
    if (isEUCntry == true) {
      FormManager.enable('taxCd2');
      // FormManager.limitDropdownValues(FormManager.getField('taxCd2'), [ '12',
      // '15' ]);
      if (FormManager.getActualValue('taxCd2') == '') {
        // FormManager.setValue('taxCd2', '15');
      }
    } else {
      FormManager.enable('taxCd2');
      // FormManager.limitDropdownValues(FormManager.getField('taxCd2'), [ '11',
      // '37' ]);
      if (FormManager.getActualValue('taxCd2') == '') {
        // FormManager.setValue('taxCd2', '37');
      }
    }
  }
}

function setTaxCdOnAddrSave() {
  var role = null;
  var reqType = FormManager.getActualValue('reqType');
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }
  if (role == 'Requester' && reqType == 'C') {
    if (FormManager.getActualValue('custGrp') == 'LOCAL') {
      setTaxCdDropDownValuesLOCAL();
    } else if (FormManager.getActualValue('custGrp') == 'CROSS') {
      setTaxCdOnAddrSaveCROSS();
    }
  }
}

function setTaxCdOnAddrSaveCROSS() {
  var isEUCntry = false;
  var landCntry = FormManager.getActualValue('landCntry');
  var addrType = FormManager.getActualValue('addrType');
  if (landCntry != null && addrType == 'ZS01') {
    for (var i = 0; i < EU_COUNTRIES.length; i++) {
      if (EU_COUNTRIES[i] == landCntry) {
        isEUCntry = true;
        break;
      }
    }
    if (isEUCntry == true) {
      FormManager.enable('taxCd2');
      // FormManager.limitDropdownValues(FormManager.getField('taxCd2'), [ '12',
      // '15' ]);
      if (FormManager.getActualValue('taxCd2') == '') {
        // FormManager.setValue('taxCd2', '15');
      }
    } else {
      FormManager.enable('taxCd2');
      // FormManager.limitDropdownValues(FormManager.getField('taxCd2'), [ '11',
      // '37' ]);
      if (FormManager.getActualValue('taxCd2') == '') {
        // FormManager.setValue('taxCd2', '37');
      }
    }
  }
}

// function addAbbrevLocnValidator() {
// FormManager.addFormValidator((function() {
// return {
// validate : function() {
// var reqType = FormManager.getActualValue('reqType');
// var role = _pagemodel.userRole;
// var _abbrevLocn = FormManager.getActualValue('abbrevLocn');
// var reg = /[^\u0000-\u007f]/;
// if (reqType != 'U' && role == 'Requester' && reg.test(_abbrevLocn)) {
// return new ValidationResult({
// id : 'abbrevLocn',
// type : 'text',
// name : 'abbrevLocn'
// }, false, 'The value for Abbreviated location is invalid. Only Latin
// characters are allowed.');
// } else {
// return new ValidationResult(null, true);
// }
// }
// };
// })(), 'MAIN_CUST_TAB', 'frmCMR');
// }

// 1916044 - Hosting Case Installing CustNm1 and CustNm2 validate
// for cust name con't in all cases:
// 1) only 1 stand along 'chez'.
// 2) 'sanchez' and 'chez.' are not stand along 'chez'. 'chez.' is an
// abbreviation ending with dot.
// 3) stand along 'chez' should not be in the end of cust name.
// 4) stand along 'chez' might be in the beginning of cust name or cust name
// cont.
// 5) one stand along 'chez' in the beginning and there is other stand along
// 'chez' - incorrect, should get error.
// 6) 'xyz chez chez 123' - incorrect.
// function addHostingInstallCustNm1Validator() {
// FormManager.addFormValidator((function() {
// return {
// validate : function() {
// var custSubGrp = FormManager.getActualValue('custSubGrp');
// var addrType = FormManager.getActualValue('addrType');
// var custNm1 = FormManager.getActualValue('custNm1');
// var custNm2 = FormManager.getActualValue('custNm2');
// var custName = null;
// if (custNm2 != '' && custNm2.length >= 1) {
// custName = custNm1.trim() + ' ' + custNm2.trim();
// } else {
// custName = custNm1.trim();
// }
// if ((custSubGrp == 'HOSTC' || custSubGrp == 'CBSTC') && addrType == 'ZS01') {
// var validateResult = true;
// if (custName != '' && custName.length > 1) {
// if (patchCount(" chez ", custName) != 1) {
// validateResult = false; // 1)
// if (patchCount(" chez ", custName) == 0 &&
// custName.toLowerCase().indexOf("chez ") == 0) {
// validateResult = true; // 4)
// }
// } else {
// if (custName.toLowerCase().indexOf("chez ") == 0) {
// validateResult = false; // 5)
// }
// }
// if (endWith(custName.toLowerCase(), " chez")) {
// validateResult = false; // 3)
// }
// if (patchCount(" chez chez ", custName) > 0 ||
// custName.toLowerCase().indexOf("chez chez ") == 0) {
// validateResult = false; // 6)
// }
// }
// if (validateResult == false) {
// return new ValidationResult({
// id : 'custNm1',
// type : 'text',
// name : 'custNm1'
// }, false, 'Correct format of the Customer Name should be: Name of end-user
// "chez" Name of host + Address of host.');
// }
// }
// return new ValidationResult(null, true);
// }
// };
// })(), null, 'frmCMR_addressModal');
// }

// function addPpsceidValidator() {
// FormManager.addFormValidator((function() {
// return {
// validate : function() {
// var custSubGrp = FormManager.getActualValue('custSubGrp');
// var ppsceid = FormManager.getActualValue('ppsceid');
//
// if (custSubGrp == 'XBUSP' || custSubGrp == 'BUSPR') {
// var validateResult = true;
// if (ppsceid == '') {
// validateResult = false;
// }
// if (validateResult == false) {
// return new ValidationResult({
// id : 'ppsceid',
// type : 'text',
// name : 'ppsceid'
// }, false, 'PPS CEID is mandatory for Business Partner Scenario');
// }
// }
// return new ValidationResult(null, true);
// }
// };
// })(), 'MAIN_IBM_TAB', 'frmCMR');
//
// }

function endWith(string, endString) {
  var result = false;
  if (string != null && endString != null) {
    var d = string.length - endString.length;
    if (d >= 0 && string.lastIndexOf(endString) == d) {
      result = true;
    }
    ;
  }
  return result;
}
function patchCount(re, str) {
  re = eval("/" + re + "/ig");
  if (str.match(re)) {
    return str.match(re).length;
  } else {
    return 0;
  }
  ;
}
// function addSIRETValidator() {
// FormManager.addFormValidator((function() {
// return {
// validate : function() {
// var custSubGrp = FormManager.getActualValue('custSubGrp');
// var _siret = FormManager.getActualValue('taxCd1');
// if ("GOVRN" == custSubGrp) {
// if (_siret && _siret.length > 0 && (_siret.substring(0, 1) == "1" ||
// _siret.substring(0, 1) == "2")) {
//
// } else {
// return new ValidationResult(null, false, 'The value for SIRET should begins
// with 1 or 2.');
// }
// }
// return new ValidationResult(null, true);
// }
// };
// })(), 'MAIN_CUST_TAB', 'frmCMR');
// }

function addLengthValidatorForSIRET() {
  console.log("register SIRET length validator . . .");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var _siret = FormManager.getActualValue('taxCd1');
        if (_siret && _siret.length > 0 && _siret.length != 14) {
          return new ValidationResult(FormManager.getField('taxCd1'), false, 'The correct Siret format-length is exactly 14 characters.');
        }
        return new ValidationResult(null, true, null);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}
// function addHostingInstallCustNmValidatorOnCheckReq() {
// FormManager
// .addFormValidator(
// (function() {
// return {
// validate : function() {
// var reqId = FormManager.getActualValue('reqId');
// var qParams = {
// REQ_ID : reqId,
// ADDR_TYPE : 'ZS01',
// };
// var _result = cmr.query('ADDR.GET.CUSTNM1.BY_REQID_ADDRTYP', qParams);
// var custNm1 = _result.ret2;
//
// var custSubGrp = FormManager.getActualValue('custSubGrp');
//
// if (custNm1 != null && (custSubGrp == 'HOSTC' || custSubGrp == 'CBSTC')) {
// if (custNm1.toLowerCase().indexOf("chez") == -1) {
// return new ValidationResult(
// null,
// false,
// 'When scenario is Hosting case, address type is Installing, correct format of
// the Customer Name should be: Name of end-user "chez" Name of host + Address
// of host.');
// } else {
// return new ValidationResult(null, true);
// }
// } else {
// return new ValidationResult(null, true);
// }
// }
// };
// })(), 'MAIN_NAME_TAB', 'frmCMR');
// }

var _addrTypeHandler = [];

function disableLandCntry() {
  var reqType = FormManager.getActualValue('reqType');

  var custType = FormManager.getActualValue('custGrp');

  for (var i = 0; i < _addrTypesForFR.length; i++) {
    _addrTypeHandler[i] = null;
    if (_addrTypeHandler[i] == null) {
      _addrTypeHandler[i] = dojo.connect(FormManager.getField('addrType_' + _addrTypesForFR[i]), 'onClick', function(value) {
        var addrTypeVal = FormManager.getActualValue('addrType');
        if (custType == 'LOCAL' && (addrTypeVal == 'ZS01')) {
          FormManager.readOnly('landCntry');
        } else {
          FormManager.enable('landCntry');
        }
      });
    }
  }

  if (custType == 'LOCAL' && FormManager.getActualValue('addrType') == 'ZS01') {
    FormManager.readOnly('landCntry');
  } else {
    FormManager.enable('landCntry');
  }
  if (reqType == 'U' && FormManager.getActualValue('addrType') == 'ZS01') {
    FormManager.readOnly('landCntry');
  } else {
    FormManager.enable('landCntry');
  }
}

function addFRLandedCountryHandler(cntry, addressMode, saving, finalSave) {
  GEOHandler.disableCopyAddress();
  if (!saving) {
    if (addressMode == 'newAddress') {
      FilteringDropdown['val_landCntry'] = FormManager.getActualValue('defaultLandedCountry');
      FormManager.setValue('landCntry', FormManager.getActualValue('defaultLandedCountry'));
    } else {
      FilteringDropdown['val_landCntry'] = null;
    }
  }
}

function validateFRCopy(addrType, arrayOfTargetTypes) {
  return null;
}

function addIBOUpdate453Validator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqType = FormManager.getActualValue('reqType');
        var role = _pagemodel.userRole;
        var IBOActual = FormManager.getActualValue('installBranchOff');
        var reqId = FormManager.getActualValue('reqId');
        var qParams = {
          REQ_ID : reqId,
        };
        var rdcIboResult = cmr.query('GET.IBO_OLD_BY_REQID', qParams);
        iboOld = rdcIboResult.ret1;
        if (reqType == 'U' && role != 'Processor' && IBOActual == '453' && iboOld != '453') {
          return new ValidationResult({
            id : 'installBranchOff',
            type : 'text',
            name : 'Installing BO'
          }, false, 'Can not change Installing BO to 453 via Update request');
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function addPostalCodeLengthValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var postal_cd = FormManager.getActualValue('postCd');
        var countryUse = FormManager.getActualValue('countryUse');
        var land_cntry = FormManager.getActualValue('landCntry');
        var countyCd = null;
        var isLOCAL = false;
        if (countryUse.length > 3) {
          countyCd = countryUse.substring(3, 5);
        } else {
          countyCd = "FR";
        }
        if (countyCd == land_cntry) {
          isLOCAL = true;
        }
        if (isLOCAL == true) {
          if (postal_cd.length > 5) {
            return new ValidationResult(null, false, 'Postal Code max length is 5 characters.');
          } else {
            return new ValidationResult(null, true);
          }
        } else if (isLOCAL == false) {
          if (postal_cd.length > 10) {
            return new ValidationResult(null, false, 'Postal Code max length is 10 characters.');
          } else {
            return new ValidationResult(null, true);
          }
        }
      }
    };
  })(), null, 'frmCMR_addressModal');
}

var _clientTierHandler = null;
function addFRClientTierLogic() {
  if (_clientTierHandler == null) {
    _clientTierHandler = dojo.connect(FormManager.getField('clientTier'), 'onChange', function(value) {
      var countyCd = null;
      var countryUse = FormManager.getActualValue('countryUse');
      var custSubGrp = FormManager.getActualValue('custSubGrp');

      if (FormManager.getActualValue('viewOnlyPage') == 'true') {
        return;
      }

      // Unlock ISU for Update and Processor
      if (FormManager.getActualValue('reqType') == 'U') {
        FormManager.enable('isuCd');
        return;
      }

      setCoverageSBOBasedOnIsuCtc();
    });
  }
  if (_clientTierHandler && _clientTierHandler[0]) {
    _clientTierHandler[0].onChange();
  }
}

function addLengthValidatorForSR() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var _sr = FormManager.getActualValue('repTeamMemberNo');
        if (_sr && _sr.length > 0 && _sr.length != 6) {
          return new ValidationResult(FormManager.getField('repTeamMemberNo'), false, 'The correct Sales Rep length is exactly 6 characters.');
        }
        return new ValidationResult(null, true, null);
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function addALPHANUMValidatorForSR() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var _sr = FormManager.getActualValue('repTeamMemberNo');
        if (_sr && _sr.length > 0 && !_sr.match("^[0-9a-zA-Z]*$")) {
          return new ValidationResult(FormManager.getField('repTeamMemberNo'), false, 'Sales Rep should be alphanumeric.');
        }
        return new ValidationResult(null, true, null);
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function showAffacturageOnReqReason() {
  var _reqReasonHandler = dojo.connect(FormManager.getField('reqReason'), 'onChange', function(value) {
    var reqType = FormManager.getActualValue('reqType');
    var reqReason = FormManager.getActualValue('reqReason');
    if (reqType == 'U') {
      if (reqReason == 'COPT') {
        FormManager.show('dupCmrIndc', 'dupCmrIndc');
      } else {
        FormManager.setValue('bpSalesRepNo', '');
        FormManager.hide('dupCmrIndc', 'dupCmrIndc');
      }
    }
    checkOrderBlk();
  });
  if (_reqReasonHandler && _reqReasonHandler[0]) {
    _reqReasonHandler[0].onChange();
  }
}

function checkOrderBlk() {
  var value = FormManager.getActualValue('reqReason');
  var role = _pagemodel.userRole;
  if (role == 'Viewer') {
    return;
  }
  if (value != 'TREC')
    return;
  var reqId = FormManager.getActualValue('reqId');
  var ordBlk = FormManager.getActualValue('ordBlk');
  var qParams = {
    REQ_ID : reqId
  };

  var result = cmr.query('GET.DATA_RDC.EMBARGO_BY_REQID_SWISS', qParams);
  if (result.ret1 == '88' && !ordBlk) {
    // correct, no alert
  } else {
    FormManager.clearValue('reqReason');
    cmr
        .showAlert('This request reason can be chosen only if the CMR\'s Order Block is 88 and the new value on the request is blank.<br><br>Please set the value of Order Block to blank then choose the request reason again.');
    return;
  }
}

// function setAbbrevNameFrDSW() {
// var _abbrevNmHandler = dojo.connect(FormManager.getField('abbrevNm'),
// 'onChange', function(value) {
// var abbrNm = FormManager.getActualValue('abbrevNm').trim();
// var requestingLob = FormManager.getActualValue('requestingLob');
// if (requestingLob == 'DSW') {
// if (abbrNm.endsWith("D3")) {
// abbrNm.substring(0, abbrNm.length - 2).trim();
// }
// if (!abbrNm.includes(" DSW D3")) {
// abbrNm = abbrNm.length > 15 ? abbrNm.substring(0, 15) : abbrNm;
// FormManager.setValue('abbrevNm', abbrNm.concat(" DSW D3"));
// }
// }
// });
// if (_abbrevNmHandler && _abbrevNmHandler[0]) {
// _abbrevNmHandler[0].onChange();
// }
// }

function affacturageLogic() {
  var reqType = FormManager.getActualValue('reqType');
  var reqReason = FormManager.getActualValue('reqReason');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (reqType == 'C') {
    if (custSubGrp == 'FIBAB' || custSubGrp == 'CBBAB') {
      FormManager.resetValidations('dupCmrIndc');
      FormManager.show('dupCmrIndc', 'dupCmrIndc');
      FormManager.readOnly('dupCmrIndc');
    } else {
      FormManager.setValue('bpSalesRepNo', '');
      FormManager.hide('dupCmrIndc', 'dupCmrIndc');
    }
  } else if (reqType == 'U') {
    if (reqReason == 'COPT') {
      FormManager.show('dupCmrIndc', 'dupCmrIndc');
    } else {
      FormManager.setValue('bpSalesRepNo', '');
      FormManager.hide('dupCmrIndc', 'dupCmrIndc');
    }
  }
}
function setISUClientTierOnScenario() {
  var role = null;
  var reqType = null;
  var countyCd = null;
  var countryUse = FormManager.getActualValue('countryUse');
  var custSubGrp = FormManager.getActualValue('custSubGrp');

  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
    reqType = FormManager.getActualValue('reqType');
  } else {
    return;
  }
  if (role != 'Requester' && role != 'Processor') {// CMR-234
    return;
  }
  if (reqType == 'U') {
    return;
  }

  // CMR-234 change start
  var countyCd = "FR";
  var custLandCntry = FormManager.getActualValue('landCntry');
  if (custLandCntry != '') {
    countyCd = custLandCntry;
  } else {
    var _zs01ReqId = FormManager.getActualValue('reqId');
    var cntryCdParams = {
      REQ_ID : _zs01ReqId,
      ADDR_TYPE : 'ZS01',
    };
    var cntryCdResult = cmr.query('ADDR.GET.LANDEDCNTRY.BY_REQID_ADDRTYPE', cntryCdParams);

    if (cntryCdResult.ret1 != undefined) {
      countyCd = cntryCdResult.ret1;
    }
  }
  // CMR-234 change end
  if (countyCd == "TN" && (custSubGrp == 'BPIEU' || custSubGrp == 'BPUEU' || custSubGrp == 'CBIEU' || custSubGrp == 'CBUEU')) {
    FormManager.setValue('isuCd', '8B');
    FormManager.setValue('clientTier', '');
  } else if (countyCd == "DZ" && (custSubGrp == 'BPIEU' || custSubGrp == 'BPUEU' || custSubGrp == 'CBIEU' || custSubGrp == 'CBUEU')) {
    FormManager.setValue('isuCd', '8B');
    FormManager.setValue('clientTier', '');
  } else if (SUB_REGIONS.has(countyCd)// CMR-234
      && (custSubGrp == 'CBMME' || custSubGrp == 'CBVRN' || custSubGrp == 'XBLUM' || custSubGrp == 'CBIEM' || custSubGrp == 'CBFIN' || custSubGrp == 'CBTSO' || custSubGrp == 'CBDPT' || custSubGrp == 'CBSTC')) {
    FormManager.setValue('isuCd', '32');
    FormManager.setValue('clientTier', 'S');
  } else if (custSubGrp == 'XBLUM' || custSubGrp == 'BUSPR' || custSubGrp == 'XBUSP' || custSubGrp == 'INTER' || custSubGrp == 'CBTER') {
    FormManager.setValue('isuCd', '21');
    FormManager.setValue('clientTier', '');
  } else {
    return;
  }
}

function setFieldsRequiredForCreateRequester() {
  var role = null;
  var reqType = FormManager.getActualValue('reqType');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }
  if (reqType == 'C' && role == 'Requester') {
    if (custSubGrp == null) {
      return;
    } else {
      if (custSubGrp == 'INTER' || custSubGrp == 'CBTER') {
        FormManager.addValidator('salesBusOffCd', Validators.REQUIRED, [ 'Search Term (SORTL)' ], 'MAIN_IBM_TAB');
        // FormManager.addValidator('installBranchOff', Validators.REQUIRED, [
        // 'Installing BO' ], 'MAIN_IBM_TAB');
      } else {
        FormManager.removeValidator('salesBusOffCd', Validators.REQUIRED);
        // FormManager.removeValidator('installBranchOff', Validators.REQUIRED);
      }
    }
  }
}

// story 1640384 - Error message for processor in case 'IBM' is part of
// Abbreviated name for some scenarios
// function addIBMAbbrevNmValidator() {
// FormManager.addFormValidator((function() {
// return {
// validate : function() {
// var role = _pagemodel.userRole;
// var abbrevNm = FormManager.getActualValue('abbrevNm');
// var custSubGrp = FormManager.getActualValue('custSubGrp');
// if (abbrevNm.toUpperCase().indexOf("IBM") != -1
// && !(custSubGrp == 'INTER' || custSubGrp == 'INTSO' || custSubGrp == 'CBTER'
// || custSubGrp == 'CBTSO')) {
// var ibmIndex = abbrevNm.toUpperCase().indexOf("IBM");
// var ibm = abbrevNm.substring(ibmIndex, ibmIndex + 3);
// FormManager.enable('abbrevNm');
// FormManager.addValidator('abbrevNm', Validators.REQUIRED, [ 'Abbreviated Name
// (TELX1)' ], 'MAIN_CUST_TAB');
// return new ValidationResult({
// id : 'abbrevNm',
// type : 'text',
// name : 'Abbreviated Name (TELX1)'
// }, false, 'Abbreviated Name (TELX1) should not contain "' + ibm + '" when
// scenario sub-type is other than Internal or Internal SO/FM');
// } else if (abbrevNm.toUpperCase().indexOf("IBM") == -1
// && (custSubGrp == 'INTER' || custSubGrp == 'INTSO' || custSubGrp == 'CBTER'
// || custSubGrp == 'CBTSO')) {
// FormManager.enable('abbrevNm');
// FormManager.addValidator('abbrevNm', Validators.REQUIRED, [ 'Abbreviated Name
// (TELX1)' ], 'MAIN_CUST_TAB');
// return new ValidationResult({
// id : 'abbrevNm',
// type : 'text',
// name : 'Abbreviated Name (TELX1)'
// }, false, 'Abbreviated Name (TELX1) should contain "IBM" when scenario
// sub-type is Internal or Internal SO/FM');
// } else {
// return new ValidationResult(null, true);
// }
// }
// };
// })(), 'MAIN_CUST_TAB', 'frmCMR');
// }

// function unlockAbbrevNmForInternalScenario() {
// var reqType = FormManager.getActualValue('reqType');
// var custSubGrp = FormManager.getActualValue('custSubGrp');
// if (FormManager.getActualValue('viewOnlyPage') == 'true') {
// return;
// }
// if (reqType == 'C') {
// if (custSubGrp == 'INTER' || custSubGrp == 'INTSO' || custSubGrp == 'CBTER'
// || custSubGrp == 'CBTSO') {
// FormManager.enable('abbrevNm');
// FormManager.addValidator('abbrevNm', Validators.REQUIRED, [ 'Abbreviated Name
// (TELX1)' ], 'MAIN_CUST_TAB');
// } else {
// FormManager.readOnly('abbrevNm');
// FormManager.removeValidator('abbrevNm', Validators.REQUIRED);
// }
// }
// }

// function setAbbrNmOnIbmDeptChng() {
// var _ibmDeptCostHandler =
// dojo.connect(FormManager.getField('ibmDeptCostCenter'), 'onChange',
// function(value) {
// setAbbrevNmOnCustSubGrpChange();
// });
// if (_ibmDeptCostHandler && _ibmDeptCostHandler[0]) {
// _ibmDeptCostHandler[0].onChange();
// }
// }

function addSoldToAddressValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (cmr.currentRequestType != 'U') {
          return new ValidationResult(null, true);
        }
        var zs01ReqId = FormManager.getActualValue('reqId');
        var addrType = FormManager.getActualValue('addrType');
        qParams = {
          REQ_ID : zs01ReqId,
        };
        var record = cmr.query('GETZS01VALRECORDS', qParams);
        var zs01Reccount = record.ret1;
        if (addrType == 'ZS01' && Number(zs01Reccount) == 1 && cmr.addressMode != 'updateAddress') {
          return new ValidationResult(null, false, 'Only one Sold-To Address can be defined.');
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function lockIBMTabForFR() {
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubType = FormManager.getActualValue('custSubGrp');

  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    // FormManager.readOnly('isuCd');
    return;
  }

  if (reqType == 'C' && (role == 'REQUESTER')) {// CMR-234
    // start
    FormManager.readOnly('isuCd');
    FormManager.readOnly('clientTier');
  }// CMR-234 -end

  var custSubGrpArray = [ 'INTER', 'INTSO', 'IBMEM', 'BUSPR', 'XBUSP', 'CBTER', 'CBTSO', 'CBIEM', 'PRICU', 'XBLUM' ];
  if (role == 'PROCESSOR' && ((reqType == 'C' && !custSubGrpArray.includes(custSubType)) || reqType == 'U')) {
    FormManager.enable('isuCd');
    FormManager.enable('clientTier');
  }

  if (reqType == 'C' && role == 'REQUESTER') {
    FormManager.readOnly('cmrNo');
    FormManager.readOnly('cmrOwner');
    FormManager.readOnly('inacCd');
    FormManager.readOnly('buyingGroupId');
    FormManager.readOnly('globalBuyingGroupId');
    FormManager.readOnly('covId');
    FormManager.readOnly('geoLocationCode');
    FormManager.readOnly('dunsNo');
    // if (custSubType != 'BPIEU' && custSubType != 'BPUEU' && custSubType !=
    // 'CBIEU' && custSubType != 'CBUEU') {
    // FormManager.readOnly('ppsceid');
    // } else {
    // FormManager.enable('ppsceid');
    // }
    FormManager.readOnly('soeReqNo');
    FormManager.readOnly('repTeamMemberNo');
    FormManager.readOnly('salesBusOffCd');
    FormManager.readOnly('installBranchOff');
    if (custSubType != 'INTER' && custSubType != 'INTSO' && custSubType != 'CBTER' && custSubType != 'CBTSO') {
      FormManager.readOnly('ibmDeptCostCenter');
    } else {
      FormManager.enable('ibmDeptCostCenter');
    }
    FormManager.readOnly('dupCmrIndc');
    FormManager.readOnly('privIndc');
  }
}

function reqReasonOnChange() {
  var reqReason = FormManager.getActualValue('reqReason');
  var addressListIGF = [ 'ZP02', 'ZD02' ];
  for (var i = 0; i < addressListIGF.length; i++) {
    var addressType = addressListIGF[i];
    if (reqReason == 'IGF' && isZD01OrZP01ExistOnCMR(addressType)) {
      dojo.byId('radiocont_' + addressType).style.display = 'inline-block';
    } else {
      dojo.byId('radiocont_' + addressType).style.display = 'none';
    }
  }

  dojo.connect(FormManager.getField('reqReason'), 'onChange', function(value) {
    for (var i = 0; i < addressListIGF.length; i++) {
      var addressType = addressListIGF[i];
      if (value == 'IGF' && isZD01OrZP01ExistOnCMR(addressType)) {
        dojo.byId('radiocont_' + addressType).style.display = 'inline-block';
      } else {
        dojo.byId('radiocont_' + addressType).style.display = 'none';
      }
    }
  });
}

function showIGFOnOpen(cntry, addressMode, saving, afterValidate) {
  if (!saving) {
    var reqReason = FormManager.getActualValue('reqReason');
    var addressListIGF = [ 'ZP02', 'ZD02' ];
    for (var i = 0; i < addressListIGF.length; i++) {
      var addressType = addressListIGF[i];
      if (reqReason == 'IGF' && isZD01OrZP01ExistOnCMR(addressType)) {
        dojo.byId('radiocont_' + addressType).style.display = 'inline-block';
      } else {
        dojo.byId('radiocont_' + addressType).style.display = 'none';
      }
    }
  }
}

function isZD01OrZP01ExistOnCMR(addressType) {
  if (addressType == 'ZP02') {
    addressType = 'ZP01';
  } else if (addressType == 'ZD02') {
    addressType = 'ZD01';
  }
  for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
    record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
    if (record == null && _allAddressData != null && _allAddressData[i] != null) {
      record = _allAddressData[i];
    }
    var type = record.addrType;
    if (typeof (type) == 'object') {
      type = type[0];
    }
    var importInd = record.importInd[0];
    if ('U' == cmr.currentRequestType && 'Y' == importInd && type == addressType) {
      return true;
    }
  }
  return false;
}

function restrictDuplicateAddr(cntry, addressMode, saving, finalSave, force) {
  FormManager.addFormValidator(
      (function() {
        return {
          validate : function() {
            var reqReason = FormManager.getActualValue('reqReason');
            var addressType = FormManager.getActualValue('addrType');
            if (addressType == 'ZP02' || addressType == 'ZD02') {
              if (reqReason != 'IGF') {
                return new ValidationResult(null, false, 'Request Reason should be IGF.');
              }
            }
            var requestId = FormManager.getActualValue('reqId');
            var addressSeq = FormManager.getActualValue('addrSeq');
            var dummyseq = "xx";
            var showDuplicateIGFBillToError = false;
            var showDuplicateIGFInstallAtToError = false;
            var qParams;
            if (addressMode == 'updateAddress') {
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

            if (addressType != undefined && addressType != '' && addressType == 'ZD02' && cmr.addressMode != 'updateAddress') {
              showDuplicateIGFInstallAtToError = Number(addCount) >= 1 && addressType == 'ZD02';
              if (showDuplicateIGFInstallAtToError) {
                return new ValidationResult(null, false,
                    'Only one IGF Ship-To address is allowed. If you still want to create new address , please delete the existing one and then create a new address.');
              }
            }

            return new ValidationResult(null, true);
          }
        };
      })(), null, 'frmCMR_addressModal');
}

function filterCmrnoP() {
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
          if (cmrNo.length != 6) {
            return new ValidationResult(null, false, 'CMR Number should be exactly 6 digits long.');
          } else if (isNaN(cmrNo)) {
            return new ValidationResult(null, false, 'CMR Number should be only numbers.');
          } else if (cmrNo == "000000") {
            return new ValidationResult(null, false, 'CMR Number should not be 000000.');
          } else if (cmrNo != '' && custSubType != '' && (custSubType == 'CBTER' || custSubType == 'CBTSO' || custSubType == 'INTER' || custSubType == 'INTSO') && !cmrNo.startsWith('99')) {
            return new ValidationResult(null, false, 'CMR Number should be in 99XXXX format for internal scenarios');
          } else if (cmrNo != '' && custSubType != '' && custSubType != 'CBTER' && custSubType != 'CBTSO' && custSubType != 'INTER' && custSubType != 'INTSO' && cmrNo.startsWith('99')) {
            return new ValidationResult(null, false, 'Non Internal CMR Number should not be in 99XXXX for scenarios');
          } else {
            var qParams = {
              CMRNO : cmrNo,
              CNTRY : cntry,
              MANDT : cmr.MANDT
            };
            var results = cmr.query('GET.CMR_BY_CNTRY_CUSNO_SAPR3', qParams);
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

function cmrNoEnable() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var reqType = FormManager.getActualValue('reqType');
  var cmrNo = FormManager.getActualValue('cmrNo');

  if (role != "PROCESSOR" || FormManager.getActualValue('viewOnlyPage') == 'true' || reqType == 'U') {
    FormManager.readOnly('cmrNo');
  } else {
    FormManager.enable('cmrNo');
  }
}

function canCopyAddress(value, rowIndex, grid) {
  return false;
}
var pageModelFlag = 'N';
// Control Customer Classification Code
function setClassificationCode() {
  var role = null;
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }

  var reqType = FormManager.getActualValue('reqType');
  var field = FormManager.getField('custClass');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var custClass = '';

  FormManager.show('CustClass', 'custClass');
  FormManager.resetDropdownValues(FormManager.getField('custClass'));

  if (role == 'Requester') {
    if (reqType == 'C') {
      if (custSubGrp != '') {
        if (custSubGrp == 'COMME' || custSubGrp == 'CBMME' || custSubGrp == 'HOSTC' || custSubGrp == 'CBSTC' || custSubGrp == 'THDPT' || custSubGrp == 'CBDPT') {
          FormManager.setValue(field, '11');
          FormManager.readOnly('custClass');
        } else if (custSubGrp == 'BUSPR' || custSubGrp == 'XBUSP') {
          var custSubGrpCodeArray = [ '41', '42', '43', '44', '45', '46', '47', '48' ];
          FormManager.limitDropdownValues(field, custSubGrpCodeArray);
          FormManager.enable(field);
          if (pageModelFlag == 'Y') {
            FormManager.setValue(field, '43');
          } else {
            FormManager.setValue(field, custSubGrpCodeArray.indexOf(_pagemodel.custClass) > -1 ? _pagemodel.custClass : '43');
            pageModelFlag = 'Y';
          }
        } else if (custSubGrp == 'PRICU' || custSubGrp == 'XBLUM') {
          FormManager.setValue(field, '60');
          FormManager.readOnly('custClass');
        } else if (custSubGrp == 'GOVRN' || custSubGrp == 'CBVRN') {
          var custSubGrpCodeArray = [ '13', '14', '17' ];
          FormManager.limitDropdownValues(field, custSubGrpCodeArray);
          FormManager.enable(field);
          if (pageModelFlag == 'Y') {
            FormManager.setValue(field, '13');
          } else {
            FormManager.setValue(field, custSubGrpCodeArray.indexOf(_pagemodel.custClass) > -1 ? _pagemodel.custClass : '13');
            pageModelFlag = 'Y';
          }
        } else if (custSubGrp == 'INTER' || custSubGrp == 'CBTER') {
          FormManager.setValue(field, '81');
          FormManager.readOnly('custClass');
        } else if (custSubGrp == 'INTSO' || custSubGrp == 'CBTSO') {
          FormManager.setValue(field, '85');
          FormManager.readOnly('custClass');
        } else if (custSubGrp == 'LCFIN' || custSubGrp == 'CBFIN') {
          var custSubGrpCodeArray = [ '32', '33', '34', '35', '36' ];
          FormManager.limitDropdownValues(field, custSubGrpCodeArray);
          FormManager.enable(field);
          if (pageModelFlag == 'Y') {
            FormManager.setValue(field, '33');
          } else {
            FormManager.setValue(field, custSubGrpCodeArray.indexOf(_pagemodel.custClass) > -1 ? _pagemodel.custClass : '33');
            pageModelFlag = 'Y';
          }
        } else if (custSubGrp == 'IBMEM' || custSubGrp == 'CBIEM') {
          FormManager.setValue(field, '71');
          FormManager.readOnly('custClass');
        }

      } else {
        FormManager.setValue(field, '');
        FormManager.readOnly('custClass');
      }
    } else if (reqType == 'U') {
      FormManager.limitDropdownValues(field, [ '11', '13', '14', '17', '32', '33', '34', '35', '36', '41', '42', '43', '44', '45', '46', '47', '48', '60', '71', '81', '85' ]);
      FormManager.setValue(field, _pagemodel.custClass);
      FormManager.readOnly('custClass');
    }
  } else if (role == 'Processor') {
    if (reqType == 'C') {
      if (custSubGrp != '') {
        if (custSubGrp == 'COMME' || custSubGrp == 'CBMME' || custSubGrp == 'HOSTC' || custSubGrp == 'CBSTC' || custSubGrp == 'THDPT' || custSubGrp == 'CBDPT') {
          FormManager.setValue(field, '11');
          FormManager.readOnly('custClass');
        } else if (custSubGrp == 'BUSPR' || custSubGrp == 'XBUSP') {
          var custSubGrpCodeArray = [ '41', '42', '43', '44', '45', '46', '47', '48' ];
          FormManager.limitDropdownValues(field, custSubGrpCodeArray);
          FormManager.enable(field);
          if (pageModelFlag == 'Y') {
            FormManager.setValue(field, '43');
          } else {
            FormManager.setValue(field, custSubGrpCodeArray.indexOf(_pagemodel.custClass) > -1 ? _pagemodel.custClass : '43');
            pageModelFlag = 'Y';
          }
        } else if (custSubGrp == 'PRICU' || custSubGrp == 'XBLUM') {
          FormManager.setValue(field, '60');
          FormManager.readOnly('custClass');
        } else if (custSubGrp == 'GOVRN' || custSubGrp == 'CBVRN') {
          var custSubGrpCodeArray = [ '13', '14', '17' ];
          FormManager.limitDropdownValues(field, custSubGrpCodeArray);
          FormManager.enable(field);
          if (pageModelFlag == 'Y') {
            FormManager.setValue(field, '13');
          } else {
            FormManager.setValue(field, custSubGrpCodeArray.indexOf(_pagemodel.custClass) > -1 ? _pagemodel.custClass : '13');
            pageModelFlag = 'Y';
          }
        } else if (custSubGrp == 'INTER' || custSubGrp == 'CBTER') {
          FormManager.setValue(field, '81');
          FormManager.readOnly('custClass');
        } else if (custSubGrp == 'INTSO' || custSubGrp == 'CBTSO') {
          FormManager.setValue(field, '85');
          FormManager.readOnly('custClass');
        } else if (custSubGrp == 'LCFIN' || custSubGrp == 'CBFIN') {
          var custSubGrpCodeArray = [ '32', '33', '34', '35', '36' ];
          FormManager.limitDropdownValues(field, custSubGrpCodeArray);
          FormManager.enable(field);
          if (pageModelFlag == 'Y') {
            FormManager.setValue(field, '33');
          } else {
            FormManager.setValue(field, custSubGrpCodeArray.indexOf(_pagemodel.custClass) > -1 ? _pagemodel.custClass : '33');
            pageModelFlag = 'Y';
          }
        } else if (custSubGrp == 'IBMEM' || custSubGrp == 'CBIEM') {
          FormManager.setValue(field, '71');
          FormManager.readOnly('custClass');
        }
      } else {
        FormManager.setValue(field, '');
        FormManager.readOnly('custClass');
      }
    } else if (reqType == 'U') {
      FormManager.limitDropdownValues(field, [ '11', '13', '14', '17', '32', '33', '34', '35', '36', '41', '42', '43', '44', '45', '46', '47', '48', '60', '71', '81', '85' ]);
      FormManager.setValue(field, _pagemodel.custClass);
    }
  } else if (role == 'Viewer') {
    FormManager.limitDropdownValues(field, [ '11', '13', '14', '17', '32', '33', '34', '35', '36', '41', '42', '43', '44', '45', '46', '47', '48', '60', '71', '81', '85' ]);
    FormManager.setValue(field, _pagemodel.custClass);
    FormManager.readOnly('custClass');
  }

}

function setTaxCd() {
  FormManager.resetDropdownValues(FormManager.getField('taxCd2'));
  FormManager.limitDropdownValues(FormManager.getField('taxCd2'), [ '1', '0' ]);

  if (typeof (_pagemodel) != 'undefined') {
    if (_pagemodel.taxCd2 == null || _pagemodel.taxCd2 == 'null') {
      FormManager.setValue(FormManager.getField('taxCd2'), '1');
    } else {
      FormManager.setValue(FormManager.getField('taxCd2'), _pagemodel.taxCd2);
    }
  }

  var role = null;

  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }

  if (role == 'Processor') {
    cmr.showNode('taxCd2MandatoryFlag');
  } else {
    cmr.hideNode('taxCd2MandatoryFlag');
  }

}

function orderBlockCodeValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var cmrno = FormManager.getActualValue('enterCMRNo');
        var mandt = FormManager.getActualValue('mandt');
        var isscntry = FormManager.getActualValue('cmrIssuingCntry');
        // PayGo_check
        var qParams = {
          ZZKV_CUSNO : cmrno,
          MANDT : mandt,
          KATR6 : isscntry
        };
        var paygorecord = cmr.query('CMR_CHECK_PAYGO', qParams);
        var countpaygo = paygorecord.ret1;
        var orderBlockCd = FormManager.getActualValue('ordBlk');
        if (orderBlockCd == '94' || orderBlockCd == '88' || orderBlockCd == '' || (Number(countpaygo) == 1 && role == 'Processor')) {
          return new ValidationResult(null, true);
        } else {
          return new ValidationResult({
            id : 'ordBlk',
            type : 'text',
            name : 'ordBlk'
          }, false, 'Order Block Code value should be only 94 or 88 or empty.');
        }

        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function setIERPSitePartyIDForFR() {
  var role = null;
  var sapNo = FormManager.getActualValue('sapNo');
  var reqType = FormManager.getActualValue('reqType');
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }
  if (reqType == 'U') {
    if (sapNo != null) {
      FormManager.setValue('ierpSitePrtyId', 'S' + sapNo);
    }
  }
}

function setSensitiveFlag() {
  FormManager.readOnly('sensitiveFlag');
}

function addAddressFieldValidators() {

  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var custNm1 = FormManager.getActualValue('custNm1');
        if (custNm1 != null && custNm1 != '' && custNm1.length > 35) {
          return new ValidationResult({
            id : 'custNm1',
            type : 'text',
            name : 'custNm1'
          }, false, 'Customer legal name should not exceed 35 characters.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');

  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var custNm2 = FormManager.getActualValue('custNm2');
        if (custNm2 != null && custNm2 != '' && custNm2.length > 35) {
          return new ValidationResult({
            id : 'custNm2',
            type : 'text',
            name : 'custNm2'
          }, false, 'Legal name continued should not exceed 35 characters.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');

  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var custNm3 = FormManager.getActualValue('custNm3');
        if (custNm3 != null && custNm3 != '' && custNm3.length > 35) {
          return new ValidationResult({
            id : 'custNm3',
            type : 'text',
            name : 'custNm3'
          }, false, ' Division/Department should not exceed 35 characters.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');

  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var custNm4 = FormManager.getActualValue('custNm4');
        if (custNm4 != null && custNm4 != '' && custNm4.length > 35) {
          return new ValidationResult({
            id : 'custNm4',
            type : 'text',
            name : 'custNm4'
          }, false, 'Attention to/Building/Floor/Office should not exceed 35 characters.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');

  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var city1 = FormManager.getActualValue('city1');
        if (city1 != null && city1 != '' && city1.length > 35) {
          return new ValidationResult({
            id : 'city1',
            type : 'text',
            name : 'city1'
          }, false, 'City should not exceed 35 characters.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');

  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var addrTxt = FormManager.getActualValue('addrTxt');
        if (addrTxt != null && addrTxt != '' && addrTxt.length > 35) {
          return new ValidationResult({
            id : 'addrTxt',
            type : 'text',
            name : 'addrTxt'
          }, false, 'Street name and number should not exceed 35 characters.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');

}

function setInternalDept() {
  if (typeof (_pagemodel) != 'undefined') {
    if (_pagemodel.ibmDeptCostCenter == null || _pagemodel.ibmDeptCostCenter == 'null') {
      FormManager.setValue(FormManager.getField('ibmDeptCostCenter'), '');
    } else {
      FormManager.setValue(FormManager.getField('ibmDeptCostCenter'), _pagemodel.ibmDeptCostCenter);
    }
  }
}

function internalDeptValidate() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var ibmDeptCostCenter = FormManager.getActualValue('ibmDeptCostCenter');
        var alphanumeric = /^[0-9a-zA-Z]*$/;
        if (ibmDeptCostCenter == '') {
          return new ValidationResult(null, true);
        } else {
          if (ibmDeptCostCenter.length < 6) {
            return new ValidationResult({
              id : 'ibmDeptCostCenter',
              type : 'text',
              name : 'ibmDeptCostCenter'
            }, false, 'The value of internal department number is invalid, please input 6 chars length.');
          }

          if (!ibmDeptCostCenter.match(alphanumeric)) {
            return new ValidationResult({
              id : 'ibmDeptCostCenter',
              type : 'text',
              name : 'ibmDeptCostCenter'
            }, false, 'The value of internal department number is invalid, please input digitals or letter.');
          }

          return new ValidationResult(null, true);

        }
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function iSICAndSubScenarioTypeCreate() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        var isicCd = FormManager.getActualValue('isicCd');
        if (isicCd == '9500') {
          if (custSubGrp == 'PRICU' || custSubGrp == 'XBLUM' || custSubGrp == 'IBMEM' || custSubGrp == 'CBIEM') {
            return new ValidationResult(null, true);
          } else {
            return new ValidationResult({
              id : 'isicCd',
              type : 'text',
              name : 'isicCd'
            }, false, 'ISIC value 9500 is not allowed for other scenario than Private Person and IBM Employee.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');

}

function iSICAndSubScenarioTypeUpdate() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var custClass = FormManager.getField('custClass');
        var isicCd = FormManager.getActualValue('isicCd');
        if (isicCd == '9500') {
          if (custClass == '60' || custClass == '71') {
            return new ValidationResult(null, true);
          } else {
            return new ValidationResult({
              id : 'isicCd',
              type : 'text',
              name : 'isicCd'
            }, false, 'ISIC value 9500 is not allowed for other scenario than Private Person and IBM Employee.');
          }
        }

        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');

}

var _vatExemptHandler = null;
function addVatExemptHandler() {
  if (!dijit.byId('vatExempt')) {
    window.setTimeout('addVatExemptHandler()', 500);
  } else {
    if (_vatExemptHandler == null) {
      _vatExemptHandler = dojo.connect(FormManager.getField('vatExempt'), 'onClick', function(value) {
        if (dijit.byId('vatExempt').get('checked')) {
          FormManager.readOnly('vat');
          FormManager.removeValidator('vat', Validators.REQUIRED);
        } else if (FormManager.getActualValue('reqType') == 'U') {
          FormManager.enable('vat');
          FormManager.removeValidator('vat', Validators.REQUIRED);
        } else if (FormManager.getActualValue('custSubGrp') == '') {
          FormManager.enable('vat');
          FormManager.removeValidator('vat', Validators.REQUIRED);
        } else {
          setVATOnScenario(true);
        }
      });
    }
  }
}

function setPPSCEIDRequired() {
  var reqType = FormManager.getActualValue('reqType');
  var subGrp = FormManager.getActualValue('custSubGrp');
  if (reqType == 'U' || FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (subGrp == 'XBUSP' || subGrp == 'BUSPR') {
    FormManager.enable('ppsceid');
    FormManager.addValidator('ppsceid', Validators.REQUIRED, [ 'PPS CEID' ], 'MAIN_IBM_TAB');
  } else {
    FormManager.setValue('ppsceid', '');
    FormManager.readOnly('ppsceid');
    FormManager.removeValidator('ppsceid', Validators.REQUIRED);
  }
}

var _oldLandedCntry = '';
function setFieldsOnLandedCountryChange() {

  if (reqType == 'U' || FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  var addrType = FormManager.getActualValue('addrType');

  if (addrType == 'ZS01') {
    if (_oldLandedCntry == '') {
      // first load - Address modal load
      _oldLandedCntry = getSoldToLanded();
    } else {
      // second load - user clicks Save
      var currentLanded = FormManager.getActualValue('landCntry');
      var valueChanged = _oldLandedCntry != currentLanded;
      if (valueChanged) {
        var scenario = FormManager.getActualValue('custSubGrp');
        if (isCoverage34QCountry(currentLanded)) {
          // update only if the country is part of coverage
          setCoverageFieldsOnScenarioChange(true, scenario, true, currentLanded);
        } else {
          // other countries - set to scenario values
          setFieldsToDefaultScenarioValues();
        }

      }
    }
  }
}

function setCoverageFieldsOnScenarioChange(fromAddress, scenario, scenarioChanged, currentLanded) {
  if (FormManager.getActualValue('reqType') == 'C' && scenarioChanged) {
    // 2H21 Coverage Changes
    set2H21CoverageChanges(fromAddress, scenario, scenarioChanged, currentLanded)
  }
}

function set2H21CoverageChanges(fromAddress, scenario, scenarioChanged, currentLanded) {
  if (FormManager.getActualValue('reqType') == 'C' && scenarioChanged) {
    if (isExcludedScenario(scenario)) {
      return;
    }

    var custGrp = FormManager.getActualValue('custGrp');
    // Set ISU/CTC based on scenario and landed
    if (custGrp == 'CROSS') {
      setCoverageIsuCtcBasedOnLandCntry(fromAddress, currentLanded);

      // Set SBO Based on ISU/CTC
      setCoverageSBOBasedOnIsuCtc(currentLanded);
    }
  }
}

function setCoverageIsuCtcBasedOnLandCntry(fromAddress, currentLanded) {
  var landedCountry = getSoldToLanded();
  if (fromAddress && currentLanded != undefined) {
    landedCountry = currentLanded;
  } else {
    landedCountry = getSoldToLanded();
  }
  if (isCoverage34QCountry(landedCountry)) {
    FormManager.setValue('isuCd', '34');
    FormManager.setValue('clientTier', 'Q');
  }
}

function isCoverage34QCountry(country) {
  var countryList34Q = [ 'TF', 'RE', 'MQ', 'GP', 'GF', 'PM', 'YT', 'NC', 'VU', 'WF', 'PF', 'AD', 'MC', 'DZ' ];
  return countryList34Q.includes(country);
}

function isExcludedScenario(scenario) {
  var excludedScenarios = [ 'INTER', 'BUSPR', 'XBUSP', 'CBTER' ];
  if (excludedScenarios.includes(scenario)) {
    return true;
  }

  return false;
}

function setCoverageSBOBasedOnIsuCtc(currentLanded) {
  var isuCd = FormManager.getActualValue('isuCd');
  var clientTier = FormManager.getActualValue('clientTier');

  setCtcForIsu5K();
  if (FormManager.getActualValue('viewOnlyPage') == 'true' || FormManager.getActualValue('reqType') == 'U') {
    return;
  }

  if (isuCd == '5K') {
    FormManager.setValue('salesBusOffCd', '98F98F');
  } else if (isuCd == '14') {
    FormManager.setValue('salesBusOffCd', '14W14W');
  } else if (isuCd == '18') {
    FormManager.setValue('salesBusOffCd', '05W05W');
  } else if (isuCd == '19') {
    FormManager.setValue('salesBusOffCd', '5ES5ES');
  } else if (isuCd == '1R') {
    FormManager.setValue('salesBusOffCd', '090090');
  } else if (isuCd == '31') {
    FormManager.setValue('salesBusOffCd', '040040');
  } else if (isuCd == '3T') {
    FormManager.setValue('salesBusOffCd', '4DF4DF');
  } else if (isuCd == '4A') {
    FormManager.setValue('salesBusOffCd', '5EF5EF');
  } else if (isuCd == '32' && clientTier == 'T') {
    FormManager.setValue('salesBusOffCd', 'FSDFSD');
  } else if (isuCd == '34' && clientTier == 'Q') {
    var custSubGrp = FormManager.getActualValue('custSubGrp');
    var custGrp = FormManager.getActualValue('custGrp');
    if (isExcludedScenario(custSubGrp)) {
      return;
    }

    var landedCountry = '';
    landedCountry = getSoldToLanded();

    if (custSubGrp == 'INTSO' && custGrp == 'LOCAL') {
      FormManager.setValue('salesBusOffCd', '98F98F');
      return;
    }
    if (custGrp == 'CROSS') {
      if (landedCountry == 'AD') {
        FormManager.setValue('salesBusOffCd', 'IBOIBO');
      } else if (landedCountry == 'MC') {
        FormManager.setValue('salesBusOffCd', '18D18D');
      } else if (landedCountry == 'DZ' || landedCountry == 'LY') {
        FormManager.setValue('salesBusOffCd', '710710');
      } else if (landedCountry == 'RE' || landedCountry == "KM") {
        FormManager.setValue('salesBusOffCd', '860860');
      } else if (landedCountry == 'MQ') {
        FormManager.setValue('salesBusOffCd', '851851');
      } else if (landedCountry == 'GP') {
        FormManager.setValue('salesBusOffCd', '852852');
      } else if (landedCountry == 'GF' || landedCountry == 'PM') {
        FormManager.setValue('salesBusOffCd', '853853');
      } else if (landedCountry == 'YT') {
        FormManager.setValue('salesBusOffCd', '864864');
      } else if (landedCountry == 'NC' || landedCountry == 'WF' || landedCountry == 'VU') {
        FormManager.setValue('salesBusOffCd', '872872');
      } else if (landedCountry == 'PF') {
        FormManager.setValue('salesBusOffCd', '873873');
      }
    } else if (custGrp == 'LOCAL') {
      add34QPostalCdLogic();
    }
  }
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

function setFieldsToDefaultScenarioValues() {
  FormManager.setValue('isuCd', getScenarioValue('isuCd'));
  FormManager.setValue('clientTier', getScenarioValue('clientTier'));
  FormManager.setValue('salesBusOffCd', getScenarioValue('salesBusOffCd'));
}

function getScenarioValue(field) {
  var scenario = FormManager.getActualValue('custSubGrp');

  var scenarioParams = {
    LOC_CD : '706',
    CUST_TYP : scenario,
    FIELD_NAME : field
  };

  var scenarioValue = '';
  var queryResult = cmr.query('GET.SCENARIO_VALUE', scenarioParams);
  console.log(queryResult)
  if (queryResult.ret1 != undefined) {
    scenarioValue = queryResult.ret1;
  }
  return scenarioValue;
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

// CREATCMR-4293
function setCTCValues() {

  FormManager.removeValidator('clientTier', Validators.REQUIRED);

  var custSubGrp = FormManager.getActualValue('custSubGrp');

  var custSubGrpArray = [ 'BUSPR', 'INTER', 'CBDPT', 'CBFIN', 'CBIEM', 'CBMME', 'CBSTC', 'CBTER', 'CBTSO', 'CBVRN', 'XBLUM', 'XBUSP' ];

  // Business Partner OR Internal OR CROSS scenarios
  if (custSubGrpArray.includes(custSubGrp)) {
    FormManager.removeValidator('clientTier', Validators.REQUIRED);
    var isuCd = FormManager.getActualValue('isuCd');
    if (isuCd == '21') {
      FormManager.setValue('clientTier', '');
    }
  }

}

function setCtcByIsu(value) {
  if (!value) {
    value = FormManager.getActualValue('isuCd');
  }
  if (value == '32') {
    FormManager.setValue('clientTier', 'T');
  } else if (value == '34') {
    FormManager.setValue('clientTier', 'Q');
  } else if (value == '36') {
    FormManager.setValue('clientTier', 'Y');
  } else {
    FormManager.setValue('clientTier', '');
  }
}

function clientTierCodeValidator() {
  var isuCode = FormManager.getActualValue('isuCd');
  var clientTierCode = FormManager.getActualValue('clientTier');
  var reqType = FormManager.getActualValue('reqType');

  if (((isuCode == '21' || isuCode == '8B' || isuCode == '5K') && reqType == 'C') || ((isuCode != '34' && isuCode != '32' && isuCode != '36') && reqType == 'U')) {
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
      }, false, 'Client Tier can only accept \'Q\'\'.');
    }
  } else if (isuCode == '32') {
    if (clientTierCode == '') {
      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier code is Mandatory.');
    } else if (clientTierCode == 'T') {
      return new ValidationResult(null, true);
    } else {
      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier can only accept \'T\'\'.');
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
      }, false, 'Client Tier can only accept \'Y\'\'.');
    }
  } else if (isuCode != '36' || isuCode != '34' || isuCode != '32') {
    if (clientTierCode == '') {
      return new ValidationResult(null, true);
    } else {
      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier can only accept blank.');
    }
  } else {
    if (clientTierCode == 'Q' || clientTierCode == 'Y' || clientTierCode == 'Y' || clientTierCode == '') {
      $("#clientTierSpan").html('');

      return new ValidationResult(null, true);
    } else {
      $("#clientTierSpan").html('');
      $("#clientTierSpan").append('<span style="color:red" class="cmr-ast" id="ast-clientTier">* </span>');

      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier can only accept \'Q\', \'Y\', \'T\' or blank.');
    }
  }
}

function sboValidator() {
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
          valResult = sboCodeValidator();
        } else {
          qParams = {
            REQ_ID : requestId,
          };
          var result = cmr.query('GET.CLIENT_TIER_ISU_SBO_CD_OLD_BY_REQID', qParams);

          if (result != null && result != '') {
            oldClientTier = result.ret1 != null ? result.ret1 : '';
            oldSbo = result.ret3 != null ? result.ret3 : '';
            oldISU = result.ret2 != null ? result.ret2 : '';

            if (clientTier != oldClientTier || isuCd != oldISU || sbo != oldSbo) {
              valResult = sboCodeValidator();
            }
          }
        }
        return valResult;
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function sboCodeValidator() {
  var isuCd = FormManager.getActualValue('isuCd');
  var clientTier = FormManager.getActualValue('clientTier');
  var isuCtc = isuCd + clientTier;
  var sbo = FormManager.getActualValue('salesBusOffCd');
  var reqType = FormManager.getActualValue('reqType');
  var role = null;
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  } else if (role == 'Requester') {
    return new ValidationResult(null, true);
  }
  if (isuCtc == '36Y' && !(sbo == '09A09A' || sbo == '10A10A' || sbo == '11A11A')) {
    return new ValidationResult({
      id : 'salesBusOffCd',
      type : 'text',
      name : 'salesBusOffCd'
    }, false, 'Sbo can only accept \'09A09A\'\ \'10A10A\'\ \'10A10A\'\' for ISU CTC 36Y.');
  } else if (isuCtc == '5K' && sbo != '98F98F') {
    return new ValidationResult({
      id : 'salesBusOffCd',
      type : 'text',
      name : 'salesBusOffCd'
    }, false, 'Sbo can only accept \'98F98F\'\ for ISU CTC 5K.');
  } else if (isuCtc == '32T' && sbo != 'FSDFSD') {
    return new ValidationResult({
      id : 'salesBusOffCd',
      type : 'text',
      name : 'salesBusOffCd'
    }, false, 'Sbo can only accept \'FSDFSD\'\ for ISU CTC 32T.');
  } else if (isuCtc == '04' && !(sbo == '05F05F' || sbo == '12F12F' || sbo == '68T68T' || sbo == '035035' || sbo == '031031')) {
    return new ValidationResult({
      id : 'salesBusOffCd',
      type : 'text',
      name : 'salesBusOffCd'
    }, false, 'Sbo can only accept \'05F05F\'\ \'12F12F\'\ \'68T68T\'\' \'035035\'\' \'031031\'\' for ISU CTC 04.');
  } else if (isuCtc == '32T' && sbo != 'FSDFSD') {
    return new ValidationResult({
      id : 'salesBusOffCd',
      type : 'text',
      name : 'salesBusOffCd'
    }, false, 'Sbo can only accept \'FSDFSD\'\ for ISU CTC 32T.');
  } else if (isuCtc == '14' && sbo != '14W14W') {
    return new ValidationResult({
      id : 'salesBusOffCd',
      type : 'text',
      name : 'salesBusOffCd'
    }, false, 'Sbo can only accept \'14W14W\'\ for ISU CTC 14.');
  } else if (isuCtc == '18' && sbo != '05W05W') {
    return new ValidationResult({
      id : 'salesBusOffCd',
      type : 'text',
      name : 'salesBusOffCd'
    }, false, 'Sbo can only accept \'05W05W\'\ for ISU CTC 18.');
  } else if (isuCtc == '19' && sbo != '5ES5ES') {
    return new ValidationResult({
      id : 'salesBusOffCd',
      type : 'text',
      name : 'salesBusOffCd'
    }, false, 'Sbo can only accept \'5ES5ES\'\ for ISU CTC 19.');
  } else if (isuCtc == '3T' && sbo != '4DF4DF') {
    return new ValidationResult({
      id : 'salesBusOffCd',
      type : 'text',
      name : 'salesBusOffCd'
    }, false, 'Sbo can only accept \'4DF4DF\'\ for ISU CTC 3T.');
  } else if (isuCtc == '4A' && sbo != '5EF5EF') {
    return new ValidationResult({
      id : 'salesBusOffCd',
      type : 'text',
      name : 'salesBusOffCd'
    }, false, 'Sbo can only accept \'5EF5EF\'\ for ISU CTC 4A.');
  } else if (isuCtc == '31' && sbo != '040040') {
    return new ValidationResult({
      id : 'salesBusOffCd',
      type : 'text',
      name : 'salesBusOffCd'
    }, false, 'Sbo can only accept \'040040\'\ for ISU CTC 31.');
  }
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

function postBoxValidationBillTo() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var addrType = FormManager.getActualValue('addrType');
        var importInd = FormManager.getActualValue('importInd');
        var poBox = FormManager.getActualValue('poBox');
        var addrTxt = FormManager.getActualValue('addrTxt');

        if (addrType == 'ZP01' && importInd == 'N') {
          if (poBox == '' && addrTxt == '') {
            return new ValidationResult(null, false, 'Street Name and Number is required.');
          }
        }
      }
    };
  })(), null, 'frmCMR_addressModal');
}

// CREATCMR-8031 2H23 coverage changes
function isCoverage2H22MEACountry(country) {
  var meaCountryList = [ 'AE', 'BJ', 'BF', 'CM', 'CF', 'TD', 'GQ', 'GA', 'GM', 'GW', 'ML', 'MR', 'MA', 'NE', 'SN', 'TG', 'TN', 'AE', 'CG', 'CI', 'GN', 'LY' ];
  return meaCountryList.includes(country);
}

function isCoverage2H22Subregion(countryCd) {
  var subRegionList = [ 'FR', 'DZ', 'GF', 'KM', 'MC', 'NC', 'PF', 'PM', 'WF', 'YT', 'NC', 'VU', 'RE', 'MQ', 'GP', 'AD' ];
  return subRegionList.includes(countryCd);
}

function setCoverage2H23FieldBehaviour() {
  var role = null;
  var reqType = FormManager.getActualValue('reqType');
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }
  if (reqType == 'U' || FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var custGrp = FormManager.getActualValue('custGrp');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var custSubGrpArrayBP = [ 'BUSPR', 'XBUSP' ];
  custSubGrpArrayPriv = [ 'XBLUM', 'PRICU' ];
  var custSubGrpArrayLocalIN = [ 'INTER', 'INSTSO', 'IBMEM' ];
  var custSubGrpArrayCbIN = [ 'CBTER', 'CBTSO', 'CBIEM' ];
  var custSubGrpArrayCbComm = [ 'CBMME', 'CBFIN', 'CBVRN', 'CBSTC', 'CBDPT', 'XBLUM' ];
  var custSubGrpArrayLocalComm = [ 'COMME', 'LCFIN', 'GOVRN', 'HOSTC', 'PRICU', 'THDPT' ];
  var LandedCntryArray = [ 'TN', 'LY', 'DZ', 'RE', 'KM', 'MQ', 'GP', 'GF', 'PM', 'YT', 'NC', 'WF', 'VU', 'PF', 'AD', 'MC' ];
  if ((custSubGrpArrayCbComm.includes(custSubGrp) || custSubGrpArrayLocalComm.includes(custSubGrp)) && role == 'Processor' && !custSubGrpArrayPriv.includes(custSubGrp)) {
    FormManager.enable('isuCd');
    FormManager.enable('clientTier');
    FormManager.enable('salesBusOffCd');
  } else if ((custSubGrpArrayCbComm.includes(custSubGrp) || custSubGrpArrayLocalComm.includes(custSubGrp) && role == 'Requester') || custSubGrpArrayBP.includes(custSubGrp)
      || custSubGrpArrayLocalIN.includes(custSubGrp) || custSubGrpArrayCbIN.includes(custSubGrp) || custSubGrpArrayPriv.includes(custSubGrp)) {
    FormManager.readOnly('isuCd');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('salesBusOffCd');
  }
}

function setCoverage2H23Sbo(fromAddress, currentLanded) {
  var role = null;
  var reqType = FormManager.getActualValue('reqType');
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }
  if (reqType == 'U' || FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (fromAddress && currentLanded != undefined) {
    landedCountry = currentLanded;
  } else {
    landedCountry = getSoldToLanded();
  }
  var countyCd = null;
  var countryUse = FormManager.getActualValue('countryUse');
  if (countryUse.length > 3) {
    countyCd = countryUse.substring(3, 5);
  } else {
    countyCd = "FR";
  }
  var custGrp = FormManager.getActualValue('custGrp');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var custSubGrpArrayBP = [ 'BUSPR', 'XBUSP' ];
  var custSubGrpArrayLocalIN = [ 'INTER', 'INSTSO', 'IBMEM' ];
  var custSubGrpArrayCbIN = [ 'CBTER', 'CBTSO', 'CBIEM' ];
  var custSubGrpArrayCbComm = [ 'CBMME', 'CBFIN', 'CBVRN', 'CBSTC', 'CBDPT', 'XBLUM' ];
  var custSubGrpArrayLocalComm = [ 'COMME', 'LCFIN', 'GOVRN', 'HOSTC', 'PRICU', 'THDPT' ];
  var LandedCntryArray = [ 'TN', 'LY', 'DZ', 'RE', 'KM', 'MQ', 'GP', 'GF', 'PM', 'YT', 'NC', 'WF', 'VU', 'PF', 'AD', 'MC' ];

  if (custSubGrpArrayBP.includes(custSubGrp)) {
    FormManager.setValue('isuCd', '21');
    FormManager.setValue('clientTier', '');
    FormManager.setValue('salesBusOffCd', '441441');
  } else if (custSubGrpArrayLocalIN.includes(custSubGrp)) {
    FormManager.setValue('isuCd', '21');
    FormManager.setValue('clientTier', '');
    FormManager.setValue('salesBusOffCd', '98F98F');
  } else if (custSubGrpArrayCbIN.includes(custSubGrp)) {
    FormManager.setValue('isuCd', '21');
    FormManager.setValue('clientTier', '');
    FormManager.setValue('salesBusOffCd', '200200');
  } else if (custSubGrpArrayCbComm.includes(custSubGrp) && !isCoverage2H22Subregion(landedCountry) && !isCoverage2H22MEACountry(landedCountry) && !LandedCntryArray.includes(landedCountry)) {
    // FormManager.setValue('isuCd', '21');
    // FormManager.setValue('clientTier', '');
    FormManager.setValue('salesBusOffCd', '200200');
  } else if (custSubGrpArrayCbComm.includes(custSubGrp) && isCoverage2H22MEACountry(landedCountry) && !LandedCntryArray.includes(landedCountry)) {
    // FormManager.setValue('isuCd', '34');
    // FormManager.setValue('clientTier', 'Q');
    FormManager.setValue('salesBusOffCd', '730730');
  } else if (custSubGrpArrayCbComm.includes(custSubGrp) && LandedCntryArray.includes(landedCountry)) {
    // FormManager.setValue('isuCd', '34');
    // FormManager.setValue('clientTier', 'Q');
    if (landedCountry == 'TN') {
      FormManager.setValue('salesBusOffCd', '710710');
    }
    setSBOOnScenarioLD();
  } else if (custSubGrpArrayLocalComm.includes(custSubGrp)) {
    add34QPostalCdLogic();
  }
}

function set2H23CoverageChanges() {
  if (reqType == 'U' || FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var currentLandedCountry = getSoldToLanded();
  setCoverage2H23Sbo(true, currentLandedCountry);
}

var _oldLandedCountry = '';
function set2H23CoverageChangesOnLandedCoutrychange() {

  if (reqType == 'U' || FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  var addrType = FormManager.getActualValue('addrType');

  if (addrType == 'ZS01') {
    if (_oldLandedCountry == '') {
      // first load - Address modal load
      _oldLandedCountry = getSoldToLanded();
    } else {
      // second load - user clicks Save
      var currentLandedCountry = FormManager.getActualValue('landCntry');
      var valueChanged = _oldLandedCountry != currentLandedCountry;
      if (valueChanged) {
        setCoverage2H23Sbo(true, currentLandedCountry)
      }

    }
  }
}

dojo.addOnLoad(function() {
  GEOHandler.FR = [ SysLoc.FRANCE ];
  console.log('adding FR functions...');

  GEOHandler.setRevertIsicBehavior(false);
  GEOHandler.addAfterConfig(afterConfigForFR, '706');
  GEOHandler.addAfterConfig(addFRClientTierLogic, '706');
  // GEOHandler.addAfterConfig(add32PostCdCntrySBOlogicOnISUChange, '706');
  GEOHandler.addAfterConfig(addISUHandler, '706');
  GEOHandler.addAfterConfig(addPostBoxHandler, '706');
  GEOHandler.addAfterConfig(addIsicCdHandler, '706');
  // GEOHandler.addAfterConfig(addIBOlogic, '706');
  GEOHandler.registerValidator(addFRAddressTypeValidator, '706', null, true);
  // GEOHandler.registerValidator(addPpsceidValidator, '706', null, true);
  GEOHandler.addAddrFunction(addLatinCharValidatorFR, '706');
  GEOHandler.addAddrFunction(addPhoneValidatorFR, '706');
  GEOHandler.addAddrFunction(setAbbrevNmLocnOnAddressSave, '706');
  GEOHandler.addAddrFunction(hidePOBox, '706');
  GEOHandler.registerValidator(addCrossBorderValidatorForFR, '706', null, true);
  GEOHandler.addAddrFunction(updateMainCustomerNames, '706');
  GEOHandler.enableCustomerNamesOnAddress('706');
  GEOHandler.addAfterConfig(addALPHANUMSPACEValidatorFR, '706');
  // GEOHandler.addAfterConfig(setAbbrNmOnIbmDeptChng, '706');
  GEOHandler.addAfterTemplateLoad(addALPHANUMSPACEValidatorFR, '706');
  // GEOHandler.registerValidator(addAbbrevLocnValidator, [ '706' ], null,
  // true);
  // GEOHandler.registerValidator(addHostingInstallCustNm1Validator, '706',
  // null, true);
  // GEOHandler.registerValidator(addSIRETValidator, ['706'] , null, true);
  GEOHandler.registerValidator(addLengthValidatorForSIRET, [ '706' ], null, true);
  GEOHandler.registerValidator(addLengthValidatorForSR, [ '706' ], null, true);
  GEOHandler.registerValidator(addALPHANUMValidatorForSR, [ '706' ], null, true);
  // GEOHandler.addToggleAddrTypeFunction(toggleAddrTypesForFR, '706');
  GEOHandler.addAddrFunction(addFRLandedCountryHandler, '706');
  GEOHandler.enableCopyAddress('706', validateFRCopy, [ 'ZD01' ]);
  GEOHandler.registerValidator(addGenericVATValidator('706', 'MAIN_CUST_TAB', 'frmCMR', 'ZS01'), [ '706' ], null, true);
  GEOHandler.addAddrFunction(disableLandCntry, [ '706' ]);
  // GEOHandler.registerValidator(addIBOUpdate453Validator, '706', null, true);
  // GEOHandler.registerValidator(addPostalCodeLengthValidator, '706' , null,
  // true);
  GEOHandler.addAfterConfig(showAffacturageOnReqReason, '706');
  // GEOHandler.addAfterConfig(setAbbrevNameFrDSW, '706');
  // GEOHandler.registerValidator(addHostingInstallCustNmValidatorOnCheckReq,
  // '706', null, true);
  // GEOHandler.registerValidator(addIBMAbbrevNmValidator, '706', null, true);

  /* 1438717 - add DPL match validation for failed dpl checks */
  GEOHandler.registerValidator(addFailedDPLValidator, [ '706' ], GEOHandler.ROLE_PROCESSOR, true);
  GEOHandler.registerValidator(addSoldToAddressValidator, '706');
  // GEOHandler.addAfterTemplateLoad(unlockAbbrevNmForInternalScenario, '706');
  GEOHandler.addAfterConfig(lockIBMTabForFR, '706');
  GEOHandler.addAfterTemplateLoad(lockIBMTabForFR, '706');
  GEOHandler.addAfterConfig(setCtcForIsu5K, '706');
  GEOHandler.addAfterTemplateLoad(setCtcForIsu5K, '706');
  GEOHandler.addAfterConfig(resetVATValidationsForPayGo, [ '706' ]);
  GEOHandler.addAfterTemplateLoad(resetVATValidationsForPayGo, [ '706' ]);
  GEOHandler.registerValidator(restrictDuplicateAddr, [ '706' ], null, true);
  GEOHandler.addAfterTemplateLoad(filterCmrnoP, '706');
  GEOHandler.registerValidator(addCmrNoValidator, [ '706' ], null, true);
  GEOHandler.addAfterConfig(setTaxCd, '706');
  GEOHandler.addAfterTemplateLoad(setTaxCd, '706');
  GEOHandler.addAfterConfig(setSensitiveFlag, '706');
  GEOHandler.addAfterTemplateLoad(setSensitiveFlag, '706');
  GEOHandler.addAddrFunction(setIERPSitePartyIDForFR, '706');
  GEOHandler.addAddrFunction(addPostCdHandler, '706');
  GEOHandler.registerValidator(addAddressFieldValidators, [ '706' ], null, true);
  GEOHandler.addAddrFunction(showIGFOnOpen, '706');
  GEOHandler.addAfterTemplateLoad(setCoverageSBOBasedOnIsuCtc, '706');
  GEOHandler.addAfterTemplateLoad(setCoverageFieldsOnScenarioChange, '706');
  GEOHandler.addAddrFunction(setFieldsOnLandedCountryChange, '706');
  GEOHandler.registerValidator(checkCmrUpdateBeforeImport, '706', null, true);
  GEOHandler.registerValidator(postBoxValidationBillTo, '706');

  // CREATCMR-4293
  GEOHandler.addAfterTemplateLoad(setCTCValues, GEOHandler.FR);
  GEOHandler.registerValidator(clientTierValidator, [ '706' ], null, true);
  GEOHandler.registerValidator(sboValidator, [ '706' ], null, true);
  GEOHandler.addAddrFunction(set2H23CoverageChangesOnLandedCoutrychange, '706');
  GEOHandler.addAfterTemplateLoad(set2H23CoverageChanges, '706');
  GEOHandler.addAfterTemplateLoad(setCoverage2H23FieldBehaviour, '706');

});