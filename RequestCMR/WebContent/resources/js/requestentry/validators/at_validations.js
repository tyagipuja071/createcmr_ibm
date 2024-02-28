/* Register AUSTRIA Javascripts */

// Exclusive countries for GBM/SBM 
var WEST_INCL = new Set(['101', '102', '103', '104', '105', '106', '107', '108', '109', '111', '115', '117', '119', '121', '123', '124', '125', '127', '129', '130', '135', '140', '141', '142',
  '143', '144', '150', '152', '153', '155', '156', '160', '161', '162', '163', '170', '173', '180', '183', '184', '185', '186', '187', '188', '190', '191', '192', '193', '194', '195', '196', '197',
  '198', '199', '214', '236', '241', '242', '243', '249', '295', '296', '297', '298', '299', '300', '301', '302', '305', '307', '308', '344', '346', '347', '350', '355', '356', '357', '358', '359',
  '360', '361', '362', '363', '364', '367', '368', '369', '385', '386', '390', '392', '394', '396', '397', '398', '400', '403', '404', '410', '414', '420', '421', '422', '423', '424', '425', '430',
  '431', '440', '442', '443', '445', '446', '454', '455', '456', '457', '600', '601', '602', '603', '606', '607', '610', '612', '613']);
var EAST_INCL = new Set(['166', '167', '168', '169', '426', '428', '429', '432', '433', '450', '452', '453', '460', '461', '462', '614', '617', '618', '619', '620', '622', '623', '624', '625',
  '626', '627', '628', '629', '630', '632', '633', '634', '636', '640', '641', '644', '646', '647', '648', '649', '650', '652', '654', '655', '656', '658', '659', '660', '664', '665', '667', '669',
  '670', '671', '672', '675', '677', '678', '679', '680', '683', '685', '687', '688', '689', '690', '693']);
var CEE_INCL = new Set(['603', '607', '626', '644', '651', '668', '693', '694', '695', '699', '704', '705', '707', '708', '740', '741', '787', '820', '821', '826', '889', '358', '359', '363']);
var isicCds = new Set(['6010', '6411', '6421', '7320', '7511', '7512', '7513', '7514', '7521', '7522', '7523', '7530', '7704', '7706', '7707', '7720', '8010', '8021', '8022', '8030', '8090', '8511',
  '8512', '8519', '8532', '8809', '8813', '8818', '9900']);
var isuCovHandler = false;
var ctcCovHandler = false;
var _custSubTypeHandler = null;
let firstTimeLoading = true;
const CTC_MAPPING = {
  '27': 'E',
  '34': 'Q',
  '36': 'Y'
}
function addAUSTRIALandedCountryHandler(cntry, addressMode, saving, finalSave) {
  if (!saving) {
    if (addressMode == 'newAddress') {
      FilteringDropdown['val_landCntry'] = FormManager.getActualValue('defaultLandedCountry');
      FormManager.setValue('landCntry', FormManager.getActualValue('defaultLandedCountry'));
    } else {
      FilteringDropdown['val_landCntry'] = null;
    }
  } else if (saving) {
    var landCntry = FormManager.getActualValue('landCntry');
    var postCode = FormManager.getActualValue('postCd');
    var stateProv = FormManager.getActualValue('stateProv');
    var reqType = FormManager.getActualValue('reqType');

    if (landCntry == 'SA' && postCode == '') {
      FormManager.setValue('postCd', '00000');
    } else if (landCntry == 'RO' && (reqType == 'C' || reqType == 'U')) {
      FormManager.setValue('custNm1', FormManager.getActualValue('custNm1').toUpperCase());
      FormManager.setValue('custNm2', FormManager.getActualValue('custNm2').toUpperCase());
      FormManager.setValue('custNm3', FormManager.getActualValue('custNm3').toUpperCase());
      FormManager.setValue('custNm4', FormManager.getActualValue('custNm4').toUpperCase());
      FormManager.setValue('addrTxt', FormManager.getActualValue('addrTxt').toUpperCase());
      FormManager.setValue('city1', FormManager.getActualValue('city1').toUpperCase());
    }

    /**
     * Defect 1525544: disable copy address pop-up for G
     */
    if (FormManager.getActualValue('addrType') == 'ZP02') {
      GEOHandler.disableCopyAddress();
    } else {
      GEOHandler.enableCopyAddress(GEOHandler.AUSTRIA_COPY, validateAUSTRIACopy, ['ZD01']);
    }
  }
}

const IS27ESCENARIO = () => {
  var clientTier = FormManager.getActualValue('clientTier');
  var isuCd = FormManager.getActualValue('isuCd');
  return isuCd == '27' && clientTier == 'E'
}

/**
 * lock OrdBlk field
 */
function lockOrdBlk() {
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var reqType = FormManager.getActualValue('reqType');

  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (role == 'REQUESTER') {
    FormManager.readOnly('ordBlk');
  } else {
    FormManager.enable('ordBlk');
  }
  // CMR - 3389
  if (cntry == SysLoc.AUSTRIA && reqType == 'U') {
    FormManager.enable('ordBlk');
  }
}

function orderBlockValidation() {
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
          var role = FormManager.getActualValue('userRole').toUpperCase();
          var ordBlk = FormManager.getActualValue('ordBlk');
          if (role == 'PROCESSOR') {
            if (ordBlk != '') {
              if (ordBlk == '88' || ordBlk == '94' || ordBlk == 'ST') {
              } else {
                return new ValidationResult(null, false, 'Only blank, 88, 94 are allowed.');
              }
            }
          }
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}


/**
 * After config for AUSTRIA
 */
function afterConfigForAUSTRIA() {
  // for all requests
  var viewOnly = FormManager.getActualValue('viewOnlyPage');
  if (viewOnly == 'true') {
    return;
  }

  FormManager.readOnly('cmrOwner');

  // Set abbrevLocn for Softlayer Scenario
  var custSubType = FormManager.getActualValue('custSubGrp');
  if (custSubType == 'SOFTL') {
    FormManager.setValue('abbrevLocn', 'Softlayer');
  }

  // set defaultLandedCountry except 707 - use 'CS' for RS/CS/ME
  var landCntry = '';
  var cntryRegion = FormManager.getActualValue('countryUse');
  if (cntryRegion && cntryRegion != '' && cntryRegion.length > 3 && FormManager.getActualValue('cmrIssuingCntry') != SysLoc.SERBIA) {
    landCntry = cntryRegion.substring(3, 5);
  }
  // Set 707 landed country base on sub region
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.SERBIA && (cntryRegion == undefined || cntryRegion == '' || cntryRegion == null)) {
    var result = cmr.query('GET_CNTRYUSED', {
      REQ_ID: FormManager.getActualValue('reqId'),
    });
    if (result && result.ret1 && result.ret1 != '') {
      cntryRegion = result.ret1;
    }
  }
  if (cntryRegion == '707ME') {
    landCntry = 'ME';
  } else if (cntryRegion == '707CS') {
    landCntry = 'RS';
  } else if (cntryRegion == '707') {
    landCntry = 'RS';
  }
  if (landCntry != '') {
    FormManager.setValue('defaultLandedCountry', landCntry);
  }

  FormManager.readOnly('capInd');
  if (CEE_INCL.has(FormManager.getActualValue('cmrIssuingCntry'))) {
    if (FormManager.getActualValue('reqType') == 'C') {
      FormManager.getField('capInd').set('checked', true);
    }
  } else {
    FormManager.setValue('capInd', true);
  }

  FormManager.readOnly('subIndustryCd');

  if (FormManager.getActualValue('cmrIssuingCntry') != SysLoc.AUSTRIA) {
    FormManager.readOnly('sensitiveFlag');
  }

    FormManager.removeValidator('repTeamMemberNo', Validators.REQUIRED);
    var role = FormManager.getActualValue('userRole').toUpperCase();
    if (role == 'REQUESTER') {
      FormManager.readOnly('custClass');
    } else {
      FormManager.enable('custClass');
    }
    // CREATCMR-6378
    retainVatValueAT();

  setExpediteReason();
  setTypeOfCustomerRequiredProcessor();
  // CREATCMR-788
  addressQuotationValidatorAUSTRIA();
}

/**
 * validates CMR number for selected scenarios only
 */
function addCmrNoValidator() {
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
        if (FormManager.getActualValue('reqType') != 'C') {
          return new ValidationResult(null, true);
        }
        if (FormManager.getActualValue('viewOnlyPage') == 'true') {
          return new ValidationResult(null, true);
        }

        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        var custSubType = FormManager.getActualValue('custSubGrp');
        var cmrNo = FormManager.getActualValue('cmrNo');

        if (cmrNo != '' && cmrNo.length != 6) {
          return new ValidationResult(null, false, 'CMR Number should be exactly 6 digits long.');
        } else if (cmrNo != '' && custSubType != '' && custSubType.includes('IN') && !cmrNo.startsWith('99')) {
          return new ValidationResult(null, false, 'CMR Number should be in 99XXXX format for internal scenarios');
        } else if (cntry != SysLoc.AUSTRIA && cmrNo != '' && custSubType != ''
          && (custSubType == 'BUSPR' || custSubType.includes('BP') || custSubType == 'CSBP' || custSubType.includes('MEBP') || custSubType == 'RSXBP' || custSubType.includes('RSBP'))
          && !(cmrNo >= 2000 && cmrNo <= 9999)) {
          return new ValidationResult(null, false, 'CMR Number should be within range: 002000 - 009999 for Business Partner scenarios');
        } else if (cmrNo != '' && custSubType != '' && custSubType == 'XCEM' && !(cmrNo >= 500000 && cmrNo <= 799999)) {
          return new ValidationResult(null, false, 'CMR Number should be within range: 500000 - 799999 for CEMEX scenarios');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function getSBOListByISU() {
  var postCd = getCurrentPostalCode();
  var isuCd = FormManager.getActualValue('isuCd');
  var ctc = FormManager.getActualValue('clientTier');
  var isuCtc = `${isuCd}${ctc}`
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  // CMR-710 use 34Q to replace 32S/32N
  checkPostCodeGroup = postCd.substring(0, 2).match(/(8[0-9])|(7[0-5]|(5[1-3])|(4[0-9])|(3[0-9])|(2[0-8])|(1[0-2]))/g)
  const getSalesBoDesc = () => {
  if (IS27ESCENARIO()) {
    if(checkPostCodeGroup || custSubGrp == 'XCOM'){
      return '1' 
    }
    return '2'
  }
  return ''
}

var salesBoDesc = getSalesBoDesc()

qParams = {
  _qall: 'Y',
  ISSUING_CNTRY: '618',
  ISU: '%' + isuCtc + '%',
  SALES_BO_DESC: '%' + salesBoDesc + '%'
};
queryResult = cmr.query('GET.SBOLIST.BYISU.AUSTRIA', qParams);

return queryResult.map(({ret1}) => ret1);
}

function setSBOInitialValue(values) {
if(values.length == 0) {
  FormManager.clearValue('salesBusOffCd')
} else {
  FormManager.setValue('salesBusOffCd', values[0])
}
}

function getSBOandAssignFirstValue() {
  setSBOInitialValue(getSBOListByISU())
}


function lockLandCntry() {
  var custType = FormManager.getActualValue('custGrp');
  var custSubType = FormManager.getActualValue('custSubGrp');
  var addrType = FormManager.getActualValue('addrType');
  var reqType = FormManager.getActualValue('reqType');
  if (addrType == 'ZP02') {
    /* Defect : 1590750 */
    // FormManager.disable('landCntry');
    return;
  }
  var local = false;
  if (custType && custType.includes('LOC')) {
    local = true;
  } else if (custSubType && (custSubType == 'ELCOM' || custSubType == 'ELBP')) {
    local = true;
  }
  if (local && FormManager.getActualValue('addrType') == 'ZS01') {
    var cntry = FormManager.getActualValue('cmrIssuingCntry');
    if (CEE_INCL.has(cntry)) {
      FormManager.setValue('landCntry', FormManager.getActualValue('defaultLandedCountry'));
    }
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

/**
 * After config handlers
 */
var _addrTypesForAUSTRIA = ['ZS01', 'ZP01', 'ZI01', 'ZD01', 'ZS02', 'ZP02'];
var _addrTypeHandler = [];
var _ISUHandler = null;
var _CTCHandler = null;
var _fiscalExemptHandler = null;
var _vatExemptHandler = null;
var _SalesRepHandler = null;
var _CTC2Handler = null;
var _SalesRep2Handler = null;
var _ExpediteHandler = null;
var _IMSHandler = null;
var _landCntryHandler = null;
function addHandlersForAUSTRIA() {
  var reqType = FormManager.getActualValue('reqType');
  for (var i = 0; i < _addrTypesForAUSTRIA.length; i++) {
    _addrTypeHandler[i] = null;
    if (_addrTypeHandler[i] == null) {
      _addrTypeHandler[i] = dojo.connect(FormManager.getField('addrType_' + _addrTypesForAUSTRIA[i]), 'onClick', function (value) {
        lockLandCntry();
      });
    }
  }

  if (_ISUHandler == null) {
    _ISUHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function (value) {
      var cntry = FormManager.getActualValue('cmrIssuingCntry');
      // CreateCMR-811
      // if (CEE_INCL.has(cntry)) {
      // setCompanyNoForCEE(value);
      // } else {
      // setClientTierValues(value);
      // }
      if (!CEE_INCL.has(cntry)) {
        setEnterpriseValues(value);
      }

      setIsuInitialValueBasedOnSubScenario();

      isuCovHandler = true;
          FormManager.removeValidator('clientTier', Validators.REQUIRED);
        // CREATCMR-4293

      setCTCBasedOnISUCode();
      getSBOandAssignFirstValue();
      setDropdownForScenarios();
    });
  }

  if (_CTCHandler == null) {
    _CTCHandler = dojo.connect(FormManager.getField('clientTier'), 'onChange', function (value) {
      var cntry = FormManager.getActualValue('cmrIssuingCntry');
      ctcCovHandler = true;
      if (!CEE_INCL.has(cntry)) {
        setEnterpriseValues(value);
      }

      // CMR-2101 Austria remove ISR
      if (FormManager.getActualValue('cmrIssuingCntry') != SysLoc.AUSTRIA) {
        setSalesRepValues(value);      
      }
    });
  }

  if (_ExpediteHandler == null) {
    _ExpediteHandler = dojo.connect(FormManager.getField('expediteInd'), 'onChange', function (value) {
      setExpediteReason();
    });
  }

  if (_IMSHandler == null && FormManager.getActualValue('cmrIssuingCntry') == SysLoc.AUSTRIA) {
    _IMSHandler = dojo.connect(FormManager.getField('subIndustryCd'), 'onChange', function (value) {
      // CMR-2101 Austria remove ISR
      // setSalesRepValues();
      setISUCTCOnIMSChange();
    });
  }

  if (_vatExemptHandler == null) {
    _vatExemptHandler = dojo.connect(FormManager.getField('vatExempt'), 'onClick', function (value) {
      setVatValidatorAUSTRIA();
      // customVATMandatoryForAT();
    });
  }

}

var _checklistBtnHandler = [];

function setClientTierValuesAT(isuCd) {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  isuCd = FormManager.getActualValue('isuCd');
  if (isuCd == '5K') {
    FormManager.removeValidator('clientTier', Validators.REQUIRED);
  }
}

function setISUCTCOnIMSChange() {
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var isuCd = FormManager.getActualValue('isuCd');
  var clientTier = FormManager.getActualValue('clientTier');
  var subIndustryCd = FormManager.getActualValue('subIndustryCd');
  var reqType = FormManager.getActualValue('reqType');
  if (reqType != 'C')
    return;
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  if (!(custSubGrp == 'INTER' || custSubGrp == 'INTSO' || custSubGrp == 'PRICU' || custSubGrp == 'IBMEM' || custSubGrp == 'BUSPR' || custSubGrp == 'XBP')) {
    if ('32' == isuCd && 'S' == clientTier && subIndustryCd.startsWith('B')) {
      FormManager.setValue('clientTier', 'N');
    } else if ('32' == isuCd && 'N' == clientTier && !subIndustryCd.startsWith('B')) {
      FormManager.setValue('clientTier', 'S');
    }
  }
}

function setVatValidatorAUSTRIA() {
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  if (viewOnlyPage != 'true' && FormManager.getActualValue('reqType') == 'C') {
    FormManager.resetValidations('vat');
    if (FormManager.getActualValue('custSubGrp').includes('IBM')) {
      FormManager.readOnly('vat');
    }
    if (dijit.byId('vatExempt').get('checked')) {
      FormManager.clearValue('vat');
    }
    if (!dijit.byId('vatExempt').get('checked')) {
      // checkAndAddValidator('vat', Validators.REQUIRED, [ 'VAT' ]);
      FormManager.enable('vat');
    }
  }
}

var _cisHandler = null;
function addCISHandler() {
  if (!FormManager.getField('cisServiceCustIndc')) {
    window.setTimeout('addCISHandler()', 500);
  } else {
    if (_cisHandler == null) {
      _cisHandler = dojo.connect(FormManager.getField('cisServiceCustIndc'), 'onClick', function (value) {
        setCountryDuplicateFields(value);
      });
    }
  }
}

var _DupIssuingCntryCdHandler = null;
var _ISU2Handler = null;
var _CTC2Handler = null;
// var _SalesRep2Handler = null;
function setCISFieldHandlers() {
  if (_DupIssuingCntryCdHandler == null) {
    _DupIssuingCntryCdHandler = dojo.connect(FormManager.getField('dupIssuingCntryCd'), 'onChange', function (value) {
      // setDropdownField2Values(value);
      setDupISUCTCValues(value);
      setEnterprise2Values(value);
    });
  }

  if (_ISU2Handler == null) {
    _ISU2Handler = dojo.connect(FormManager.getField('dupIsuCd'), 'onChange', function (value) {
      // setClientTier2Values(value);
      setEnterprise2Values(value);
    });
  }

  if (_CTC2Handler == null) {
    _CTC2Handler = dojo.connect(FormManager.getField('dupClientTierCd'), 'onChange', function (value) {
      setEnterprise2Values(value);
    });
  }

}

function setExpediteReason() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (FormManager.getActualValue('expediteInd') != 'Y') {
    FormManager.clearValue('expediteReason');
    FormManager.readOnly('expediteReason');
  }
}

/* Street or PO Box should be required (Austria) */

function addStreetAndPoBoxFormValidator() {
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
        if (FormManager.getActualValue('cmrIssuingCntry') != SysLoc.AUSTRIA) {
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

/**
 * Add Latin character validation for address fields
 */
function addLatinCharValidator() {
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.AUSTRIA) {
    return;
  }

  var restrictNonLatin = false;
  if (FormManager.getActualValue('addrType') != 'ZP02') {
    restrictNonLatin = true;
  }

  if (restrictNonLatin) {
    checkAndAddValidator('custNm1', Validators.LATIN, ['Customer Name (1)']);
    checkAndAddValidator('custNm2', Validators.LATIN, ['Customer Name (2)']);
    // CREATCMR-788
    if (FormManager.getActualValue('cmrIssuingCntry') == '740') {
      FormManager.addValidator('custNm2', Validators.NO_QUOTATION, ['Customer Name (2)/Local VAT']);
    }
    checkAndAddValidator('custNm3', Validators.LATIN, ['Customer Name (3)']);
    checkAndAddValidator('addrTxt', Validators.LATIN, ['Street Address']);
    checkAndAddValidator('city1', Validators.LATIN, ['City']);
//    checkAndAddValidator('postCd', Validators.LATIN, ['Postal Code']);
  } else {
    FormManager.removeValidator('custNm1', Validators.LATIN);
    FormManager.removeValidator('custNm2', Validators.LATIN);
    FormManager.removeValidator('custNm3', Validators.LATIN);
    FormManager.removeValidator('addrTxt', Validators.LATIN);
    FormManager.removeValidator('city1', Validators.LATIN);
//    FormManager.removeValidator('postCd', Validators.LATIN);
  }
}
/**
 * Validator for copy address
 */
function validateAUSTRIACopy(addrType, arrayOfTargetTypes) {
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.AUSTRIA) {
    return;
  }
  console.log('Addr Type: ' + addrType + " Targets: " + arrayOfTargetTypes);
  var localLang = addrType == 'ZP02';
  if (localLang
    && (arrayOfTargetTypes.indexOf('ZS01') >= 0 || arrayOfTargetTypes.indexOf('ZP01') >= 0 || arrayOfTargetTypes.indexOf('ZI01') >= 0 || arrayOfTargetTypes.indexOf('ZD01') >= 0 || arrayOfTargetTypes
      .indexOf('ZS02') >= 0)) {
    return 'Cannot copy local address to non-local addresses. Please select only local target addresses.';
  }
  if (!localLang && (arrayOfTargetTypes.indexOf('ZP02') >= 0)) {
    return 'Cannot copy non-local address to local addresses. Please select only non-local target addresses.';
  }
  return null;
}

/**
 * Validator for address fields
 */
function addAddressFieldValidators() {
  // CEEME: City + Postal Code should not exceed 30 characters
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        if (cntry == SysLoc.AUSTRIA) {
          return new ValidationResult(null, true);
        }
        var city = FormManager.getActualValue('city1');
        var postCd = FormManager.getActualValue('postCd');

        var val = city;
        if (postCd != '') {
          val += postCd;
        }
        if (val && val.length > 30) {
          return new ValidationResult(null, false, 'Total computed length of City and Postal Code should not exceed 30 characters.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');

  // phone + ATT should not exceed 30 characters
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
        if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.AUSTRIA) {
          return new ValidationResult(null, true);
        }
        var att = FormManager.getActualValue('custNm4');
        var custPhone = FormManager.getActualValue('custPhone');
        var val = att;

        if (custPhone != '') {
          val += custPhone;
          if (val != null && val.length > 30) {// CMR-816
            return new ValidationResult(null, false, 'Total computed length of Attention Person and Phone should not exceed 30 characters.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');

  // AT: addrTxt + poBox should not exceed 21 characters
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
        if (FormManager.getActualValue('cmrIssuingCntry') != SysLoc.AUSTRIA) {
          return new ValidationResult(null, true);
        }
        var addrTxt = FormManager.getActualValue('addrTxt');
        var poBox = FormManager.getActualValue('poBox');

        var val = addrTxt;
        if (poBox != null && poBox != '') {
          val += poBox;
          if (val != null && val.length > 21) {
            return new ValidationResult(null, false, 'Total computed length of Street Name And Number and PO BOX should not exceed 21 characters.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');

  // AT: phone + ATT should not exceed 30 characters
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
        if (FormManager.getActualValue('cmrIssuingCntry') != SysLoc.AUSTRIA) {
          return new ValidationResult(null, true);
        }
        var att = FormManager.getActualValue('custNm4');
        var custPhone = FormManager.getActualValue('custPhone');
        var val = att;

        if (custPhone != '') {
          val += custPhone;
          if (val != null && val.length > 30) {
            return new ValidationResult(null, false, 'Total computed length of Attention to/Building/Floor/Office and Phone should not exceed 30 characters.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');

  // ME: custNm2/custNm3 + poBox should not exceed 21 characters
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
        if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.AUSTRIA) {
          return new ValidationResult(null, true);
        }
        var custNm2 = FormManager.getActualValue('custNm2');
        var custNm3 = FormManager.getActualValue('custNm3');
        var poBox = FormManager.getActualValue('poBox');

        var val1 = custNm2;
        var val2 = custNm3;
        if (poBox != null && poBox != '') {
          val1 += poBox;
          val2 += poBox;
          if (val1.length > 21 && val2.length > 21) {
            return new ValidationResult(null, false, 'Customer Name (2)/Customer Name (3) and PO BOX should not exceed 21 characters.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function setDupISUCTCValues(custSubGrp) {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var isuCds = [];
  if (custSubGrp != '') {
    if (FormManager.getActualValue('custSubGrp') == 'XBP' || FormManager.getActualValue('custSubGrp') == 'BUSPR' || FormManager.getActualValue('custSubGrp') == 'EXBP'
      || FormManager.getActualValue('custSubGrp') == 'ELBP') {
      isuCds = ['8B'];
      FormManager.setValue('dupClientTierCd', '');
      FormManager.readOnly('dupClientTierCd');
    } else if (FormManager.getActualValue('custSubGrp') == 'XINT' || FormManager.getActualValue('custSubGrp') == 'INTER') {
      isuCds = ['21'];
      FormManager.setValue('dupClientTierCd', '');
      FormManager.readOnly('dupClientTierCd')
    } else if (FormManager.getActualValue('custSubGrp') == 'XCOM' || FormManager.getActualValue('custSubGrp') == 'XTP' || FormManager.getActualValue('custSubGrp') == 'COMME'
      || FormManager.getActualValue('custSubGrp') == 'PRICU' || FormManager.getActualValue('custSubGrp') == 'THDPT' || FormManager.getActualValue('custSubGrp') == 'EXCOM'
      || FormManager.getActualValue('custSubGrp') == 'ELCOM') {
      isuCds = ['04', '05', '11', '12', '14', '15', '18', '19', '1R', '21', '27', '28', '31', '32', '34', '3T', '40', '4A', '4D', '4F', '5B', '5E', '60', '8B', '8C'];// CreateCMR-811
      FormManager.setValue('dupClientTierCd', 'Q');
    }
  }

  if (isuCds != null && isuCds != '') {
    FormManager.limitDropdownValues(FormManager.getField('dupIsuCd'), isuCds);
    if (isuCds.length == 1) {
      FormManager.setValue('dupIsuCd', isuCds[0]);
      FormManager.readOnly('dupIsuCd')
    } else {
      FormManager.setValue('dupIsuCd', '34');
    }
  }
}

function setVatRequired(value) {
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  if (viewOnlyPage != 'true' && FormManager.getActualValue('reqType') == 'C') {
    if (!value && !dijit.byId('vatExempt')) {
      window.setTimeout('setVatRequired()', 500);
    } else {
      FormManager.resetValidations('vat');
      if (!dijit.byId('vatExempt').get('checked')) {
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        /*
         * if (cntry == SysLoc.AUSTRIA) { var custGroup =
         * FormManager.getActualValue('custGrp'); if (custGroup != 'CROSS') {
         * checkAndAddValidator('vat', Validators.REQUIRED, [ 'VAT' ]);
         * austriaCustomVATMandatory(); } } else
         */if (cntry == SysLoc.SERBIA) {
          var cntryUsed = '';
          var result = cmr.query('GET_CNTRYUSED', {
            REQ_ID: FormManager.getActualValue('reqId'),
          });
          if (result && result.ret1 && result.ret1 != '') {
            cntryUsed = result.ret1;
          }
          if (cntryUsed == '707ME' || cntryUsed == '707CS') {
            return;
          }

        }
        austriaCustomVATMandatory();
      }
    }
  }
}

function setCountryDuplicateFields(value) {
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  var role = FormManager.getActualValue('userRole');

  if (!value && !dijit.byId('cisServiceCustIndc')) {
    window.setTimeout('setCountryDuplicateFields()', 500);
  } else {
    FormManager.resetValidations('dupIssuingCntryCd');
    if (dijit.byId('cisServiceCustIndc').get('checked')) {
      FormManager.show('ISU2', 'dupIsuCd');
      FormManager.show('ClientTier2', 'dupClientTierCd');
      FormManager.show('Enterprise2', 'dupEnterpriseNo');
      FormManager.show('LocalTax3', 'taxCd3');
      FormManager.show('DupSalesBusOffCd', 'dupSalesBoCd');
      // Mark as hide for CMR-4606
      FormManager.hide('SalRepNameNo2', 'dupSalesRepNo');
      // if (role == GEOHandler.ROLE_PROCESSOR) {
      // FormManager.show('SalesBusOff2', 'dupSalesBoCd');
      // } else {
      // FormManager.hide('SalesBusOff2', 'dupSalesBoCd');
      // }
      // setDropdownField2Values();
      if (viewOnlyPage != 'true') {
        FormManager.enable('dupIssuingCntryCd');
        checkAndAddValidator('dupIssuingCntryCd', Validators.REQUIRED, ['Country of Duplicate CMR']);
        setCISFieldHandlers();

        if (FormManager.getActualValue('reqType') == 'C') {
          // CMR-4606 show alert message on tab
          checkAndAddValidator('dupIsuCd', Validators.REQUIRED, ['ISU Code 2'], 'MAIN_IBM_TAB');
          checkAndAddValidator('dupClientTierCd', Validators.REQUIRED, ['Client Tier 2'], 'MAIN_IBM_TAB');
          // checkAndAddValidator('dupSalesRepNo', Validators.REQUIRED, [ 'Sales
          // Rep 2' ]);
          if (role == 'Requester') {
            FormManager.readOnly('dupSalesBoCd');
          } else {
            FormManager.enable('dupSalesBoCd');
          }
        }
      } else {
        FormManager.readOnly('dupIsuCd');
        FormManager.readOnly('dupClientTierCd');
        FormManager.readOnly('dupEnterpriseNo');
        // FormManager.readOnly('dupSalesRepNo');
        FormManager.readOnly('taxCd3');
        FormManager.readOnly('dupSalesBoCd');
      }

    } else {
      FormManager.clearValue('dupIssuingCntryCd');
      FormManager.disable('dupIssuingCntryCd');

      FormManager.resetValidations('dupIsuCd');
      FormManager.resetValidations('dupClientTierCd');
      // FormManager.resetValidations('dupSalesRepNo');
      FormManager.hide('ISU2', 'dupIsuCd');
      FormManager.hide('ClientTier2', 'dupClientTierCd');
      FormManager.hide('Enterprise2', 'dupEnterpriseNo');
      FormManager.hide('LocalTax3', 'taxCd3');
      FormManager.hide('SalRepNameNo2', 'dupSalesRepNo');
      FormManager.hide('DupSalesBusOffCd', 'dupSalesBoCd');
    }
  }
}

/**
 * Austria - sets Sales rep based on isuCtc
 */
function setSalesRepValues(clientTier) {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  if (FormManager.getActualValue('cmrIssuingCntry') != SysLoc.AUSTRIA) {
    return;
  }
  if (!clientTier || clientTier == '') {
    clientTier = FormManager.getActualValue('clientTier');
  }

  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var isuCd = FormManager.getActualValue('isuCd');
  var ims = FormManager.getActualValue('subIndustryCd');

  var salesReps = [];
  if (isuCd != '') {
    var isuCtc = isuCd + clientTier;
    var qParams = null;
    var results = null;
    // SalRep will be based on IMS for 32S/32T
    // CMR-710 use 34Q to replace 32S
    if (ims != '' && ims.length > 1 && (isuCtc == '34Q' || isuCtc == '32T')) {
      qParams = {
        _qall: 'Y',
        ISSUING_CNTRY: cntry,
        ISU: '%' + isuCd + clientTier + '%',
        CLIENT_TIER: '%' + ims.substring(0, 1) + '%',
      };
      results = cmr.query('GET.SRLIST.BYISUCTC', qParams);
    } else {
      qParams = {
        _qall: 'Y',
        ISSUING_CNTRY: cntry,
        ISU: '%' + isuCd + clientTier + '%',
      };
      results = cmr.query('GET.SRLIST.BYISU', qParams);
    }

    if (results != null) {
      salesReps = results.filter(({ret1}) => ret1 != '000009')

      FormManager.limitDropdownValues(FormManager.getField('repTeamMemberNo'), salesReps);
      if (salesReps.length == 1) {
        FormManager.setValue('repTeamMemberNo', salesReps[0]);
        // setSBO();
      }
    }
  }
  // Defect 1705993: Austria - ISU logic based on Subindustry fix
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (custSubGrp != null && (custSubGrp == 'COMME' || custSubGrp == 'XCOM')) {
    if (ims != '' && ims.length > 1 && ims.substring(0, 1).toUpperCase() == 'B') {
      FormManager.setValue('isuCd', '32');
      FormManager.setValue('clientTier', 'N');
    }
  }
}

function validateSBOValuesForIsuCtc() {
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
        var sbos = getSBOListByISU()
        if(sbos.length == 0) {
          return new ValidationResult(null, false,
            'No SBO was found for the current ISU/CTC');
        }
          if (!sbos.includes(FormManager.getActualValue('salesBusOffCd'))) {
            return new ValidationResult(null, false,
              'The SBO provided is invalid. It should be from the list: ' + sbos.join(', '));
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

// CMR-2101 SBO is required for processor
function validateSBO() {
  if (FormManager.getActualValue('userRole') == GEOHandler.ROLE_PROCESSOR) {
    FormManager.addValidator('salesBusOffCd', Validators.REQUIRED, ['SBO'], 'MAIN_IBM_TAB');
  }

}


/**
 * CEEME - show CoF field for Update req and LOB=IGF and reason=COPT
 */
function setCommercialFinanced() {
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.AUSTRIA) {
    return;
  }
  processCoF();
  dojo.connect(FormManager.getField('requestingLob'), 'onChange', function (value) {
    processCoF();
  });
  dojo.connect(FormManager.getField('reqReason'), 'onChange', function (value) {
    processCoF();
  });
}

function processCoF() {
  var reqType = FormManager.getActualValue('reqType');
  var lob = FormManager.getActualValue('requestingLob');
  var reason = FormManager.getActualValue('reqReason');
  if (reqType == 'U') {
    FormManager.show('CommercialFinanced', 'commercialFinanced');
    if (lob == 'IGF' && reason == 'COPT') {
      FormManager.enable('commercialFinanced');
    } else {
      FormManager.readOnly('commercialFinanced');
    }
  } else {
    FormManager.hide('CommercialFinanced', 'commercialFinanced');
  }
}
/**
 * CEEME - show TeleCoverageRep for GBM and SBM scenarios
 */
function setTelecoverageRep() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.AUSTRIA) {
    return;
  }

  var custGrp = FormManager.getActualValue('custGrp');
  if (custGrp != null && (custGrp == 'GBM' || custGrp == 'SBM')) {
    checkAndAddValidator('bpSalesRepNo', Validators.REQUIRED, ['Tele-coverage rep.']);
    FormManager.show('TeleCoverageRep', 'bpSalesRepNo');
  } else {
    FormManager.resetValidations('bpSalesRepNo');
    FormManager.clearValue('bpSalesRepNo');
    FormManager.hide('TeleCoverageRep', 'bpSalesRepNo');
  }
}
/**
 * Validate Special Character for Abbreviated Name/Location
 */
function validateAbbrevNmLocn() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (role == 'PROCESSOR') {
    FormManager.addValidator('abbrevNm', Validators.REQUIRED, ['Abbreviated Name (TELX1)'], 'MAIN_CUST_TAB');
    FormManager.addValidator('abbrevLocn', Validators.REQUIRED, ['Abbreviated Location'], 'MAIN_CUST_TAB');
    /**
     * remove special chars check for CMR-811
     */
    // FormManager.addValidator('abbrevNm', Validators.NO_SPECIAL_CHAR, [
    // 'Abbreviated Name (TELX1)' ], 'MAIN_CUST_TAB');
    // FormManager.addValidator('abbrevLocn', Validators.NO_SPECIAL_CHAR, [
    // 'Abbreviated Location' ], 'MAIN_CUST_TAB');
  }
}

function executeBeforeSubmit() {
  var reqType = FormManager.getActualValue('reqType');
  if (reqType == 'U' && dijit.byId('cisServiceCustIndc').get('checked')) {
    cmr.showConfirm('showAddressVerificationModal()', 'You are updating record with duplicate, if you wish to continue click Yes, otherwise No.', null, 'cancelCIS()', {
      OK: 'Yes',
      CANCEL: 'No'
    });
  } else {
    proceedCIS();
  }
}

function proceedCIS() {
  cmr.showModal('addressVerificationModal');
}

function changeDupSBO() {
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }

  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var dupIssuingCntry = FormManager.getActualValue('dupIssuingCntryCd');

  var dupSbo = "";
  if (dupIssuingCntry != null && dupIssuingCntry != '') {
    if ('358' == dupIssuingCntry) {
      dupSbo = 'A02';
    } else if ('889' == dupIssuingCntry) {
      dupSbo = 'U02';
    } else if ('787' == dupIssuingCntry) {
      dupSbo = 'D02';
    } else if ('626' == dupIssuingCntry || '607' == dupIssuingCntry || '651' == dupIssuingCntry) {
      dupSbo = 'G02';
    } else if ('695' == dupIssuingCntry || '694' == dupIssuingCntry) {
      dupSbo = 'K02';
    } else if ('741' == dupIssuingCntry || '363' == dupIssuingCntry || '359' == dupIssuingCntry) {
      dupSbo = 'J02';
    }
  }
  FormManager.setValue('dupSalesBoCd', dupSbo);
}

function changeAbbrevNmLocn(cntry, addressMode, saving, finalSave, force) {
  if (finalSave || force || addressMode == 'COPY') {
    var copyTypes = document.getElementsByName('copyTypes');
    var copyingToA = false;
    if (copyTypes != null && copyTypes.length > 0) {
      copyTypes.forEach(function (input, i) {
        if (input.value == 'ZS01' && input.checked) {
          copyingToA = true;
        }
      });
    }
    var addrType = FormManager.getActualValue('addrType');
    if (addrType == 'ZS01' || copyingToA) {
      // generate Abbreviated Name/Location
      var role = FormManager.getActualValue('userRole').toUpperCase();
      var abbrevNm = FormManager.getActualValue('custNm1');
      var abbrevLocn = FormManager.getActualValue('city1');
      if (FormManager.getActualValue('reqType') != 'C') {
        return;
      }
      if (role == 'REQUESTER') {
        if (abbrevNm && abbrevNm.length > 30) {
          abbrevNm = abbrevNm.substring(0, 30);
        }
        if (abbrevLocn && abbrevLocn.length > 12) {
          abbrevLocn = abbrevLocn.substring(0, 12);
        }
        FormManager.setValue('abbrevNm', abbrevNm);
        FormManager.setValue('abbrevLocn', abbrevLocn);

      }
      // CMR-837
      changeBetachar();
    }
  }
}

function setAbbrvNmLoc() {
  console.log("setting abbrvName and location");
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
      REQ_ID: reqId,
    };
  }
  var custNm = cmr.query('ADDR.GET.CUSTNM1.BY_REQID', reqParam);
  var city = cmr.query('ADDR.GET.CITY1.BY_REQID', reqParam);
  var abbrvNm = custNm.ret1;
  var abbrevLocn = city.ret1;
  var cntry = FormManager.getActualValue('cmrIssuingCntry');

  if (FormManager.getActualValue('reqType') == 'C') {
    if (abbrvNm && abbrvNm.length > 22) {
      abbrvNm = abbrvNm.substring(0, 22);
    }
    if (abbrevLocn && abbrevLocn.length > 12) {
      abbrevLocn = abbrevLocn.substring(0, 12);
    }

    // CMR-4606 set up abbrvNm for Russia CIS dup CMR
    if (cntry == '821' && dijit.byId('cisServiceCustIndc').get('checked')) {
      if (abbrvNm && abbrvNm.length > 18) {
        abbrvNm = abbrvNm.substring(0, 18).trim() + ' CIS';
      } else {
        abbrvNm = abbrvNm + ' CIS';
      }
    }

  }

  if (abbrevLocn != null) {
    FormManager.setValue('abbrevLocn', abbrevLocn);
  }
  if (abbrvNm != null) {
    FormManager.setValue('abbrevNm', abbrvNm);
  }
}

function lockAbbrv() {
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (viewOnlyPage == 'true') {
    FormManager.readOnly('abbrevLocn');
    FormManager.readOnly('abbrevNm');
  } else {
    if (role == 'REQUESTER') {
      FormManager.readOnly('abbrevNm');
      FormManager.readOnly('abbrevLocn');
    }
  }
}

function reqReasonOnChangeAT() {
  var reqReason = FormManager.getActualValue('reqReason');
  if (reqReason == 'IGF' && isZD01OrZP01ExistOnCMR_AT()) {
    // FormManager.limitDropdownValues(FormManager.getField('custSubGrp'), [
    // 'BUSPR', 'COMME', 'GOVRN', 'IBMEM', 'XBP', 'XCOM', 'XGOV']);
    dojo.byId('radiocont_ZP02').style.display = 'inline-block';
    dojo.byId('radiocont_ZD02').style.display = 'inline-block';
  } else {
    dojo.byId('radiocont_ZP02').style.display = 'none';
    dojo.byId('radiocont_ZD02').style.display = 'none';
  }
  dojo.connect(FormManager.getField('reqReason'), 'onChange', function (value) {
    if (value == 'IGF' && isZD01OrZP01ExistOnCMR_AT()) {
      // FormManager.limitDropdownValues(FormManager.getField('custSubGrp'), [
      // 'BUSPR', 'COMME', 'GOVRN', 'IBMEM', 'XBP', 'XCOM', 'XGOV']);
      dojo.byId('radiocont_ZP02').style.display = 'inline-block';
      dojo.byId('radiocont_ZD02').style.display = 'inline-block';
    } else {
      // FormManager.resetDropdownValues(FormManager.getField('custSubGrp'));
      dojo.byId('radiocont_ZP02').style.display = 'none';
      dojo.byId('radiocont_ZD02').style.display = 'none';
    }
  });
}

function isZD01OrZP01ExistOnCMR_AT() {
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
    var reqType = FormManager.getActualValue('reqType');
    if ('U' == reqType && 'Y' == importInd && (type == 'ZD01' || type == 'ZP01')) {
      return true;
    }
  }
  return false;
}

function setEnterpriseValues(clientTier) {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  if (CEE_INCL.has(cntry)) { // CreateCMR-811
    return;
  }

  if (!CEE_INCL.has(cntry)) {
    if (FormManager.getActualValue('viewOnlyPage') == 'true' || custSubGrp == 'IBMEM' || custSubGrp == 'PRICU' || custSubGrp == 'BUSPR' || custSubGrp == 'XBP' || custSubGrp == 'INTER'
      || custSubGrp == 'INTSO') {
      return;
    } else {
      if (FormManager.getActualValue('viewOnlyPage') == 'true') {
        return;
      }
    }
  }
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }

  var isuCd = FormManager.getActualValue('isuCd');
  if (role != 'REQUESTER') {
    FormManager.enable('enterprise');
  }
  clientTier = FormManager.getActualValue('clientTier');

  var enterprises = [];
  if (isuCd != '' && clientTier != '') {
    if (SysLoc.SERBIA == cntry) {
      enterprises = [''];
    } else if (SysLoc.SLOVAKIA == cntry
      && (FormManager.getActualValue('custSubGrp') == 'XTP' || FormManager.getActualValue('custSubGrp') == 'THDPT' || FormManager.getActualValue('custSubGrp') == 'COMME'
        || FormManager.getActualValue('custSubGrp') == 'XCOM' || FormManager.getActualValue('custSubGrp') == 'XPC' || FormManager.getActualValue('custSubGrp') == 'PRICU')) {
      if (isuCd == '34' && clientTier == '6') {
        enterprises = ['985069', '985070'];
      } else if (isuCd == '34' && clientTier == 'V') {
        enterprises = ['985013'];
      }
    } else if ((SysLoc.AZERBAIJAN == cntry)
      && (FormManager.getActualValue('custSubGrp') == 'XTP' || FormManager.getActualValue('custSubGrp') == 'THDPT' || FormManager.getActualValue('custSubGrp') == 'XPC'
        || FormManager.getActualValue('custSubGrp') == 'PRICU' || FormManager.getActualValue('custSubGrp') == 'COMME' || FormManager.getActualValue('custSubGrp') == 'XCOM')) {
      if (isuCd == '34' && clientTier == 'V') {
        enterprises = [''];
      }
    } else if (SysLoc.CZECH_REPUBLIC == cntry
      && (FormManager.getActualValue('custSubGrp') == 'XTP' || FormManager.getActualValue('custSubGrp') == 'THDPT' || FormManager.getActualValue('custSubGrp') == 'XPC'
        || FormManager.getActualValue('custSubGrp') == 'PRICU' || FormManager.getActualValue('custSubGrp') == 'COMME' || FormManager.getActualValue('custSubGrp') == 'XCOM')) {
      if (isuCd == '34' && clientTier == '6') {
        enterprises = ['985069', '985070'];
      } else if (isuCd == '34' && clientTier == 'V') {
        enterprises = ['985013', '985014', '985015', '985055'];
      }
    } else if ((SysLoc.BOSNIA_HERZEGOVINA == cntry || SysLoc.SLOVENIA == cntry || SysLoc.MOLDOVA == cntry || SysLoc.ROMANIA == cntry)
      && (FormManager.getActualValue('custSubGrp') == 'XTP' || FormManager.getActualValue('custSubGrp') == 'THDPT' || FormManager.getActualValue('custSubGrp') == 'XPC'
        || FormManager.getActualValue('custSubGrp') == 'PRICU' || FormManager.getActualValue('custSubGrp') == 'COMME' || FormManager.getActualValue('custSubGrp') == 'XCOM')) {
      if (isuCd == '32' && clientTier == 'M') {
        enterprises = ['985069', '985070'];
      }
    } else if ((SysLoc.CROATIA == cntry)
      && (FormManager.getActualValue('custSubGrp') == 'XTP' || FormManager.getActualValue('custSubGrp') == 'THDPT' || FormManager.getActualValue('custSubGrp') == 'XPC'
        || FormManager.getActualValue('custSubGrp') == 'PRICU' || FormManager.getActualValue('custSubGrp') == 'COMME' || FormManager.getActualValue('custSubGrp') == 'XCOM')) {
      if (isuCd == '34' && clientTier == '6') {
        enterprises = ['985069', '985070'];
      }
    } else if (SysLoc.HUNGARY == cntry
      && (FormManager.getActualValue('custSubGrp') == 'XTP' || FormManager.getActualValue('custSubGrp') == 'THDPT' || FormManager.getActualValue('custSubGrp') == 'XPC'
        || FormManager.getActualValue('custSubGrp') == 'PRICU' || FormManager.getActualValue('custSubGrp') == 'COMME' || FormManager.getActualValue('custSubGrp') == 'XCOM')) {
      if (isuCd == '34' && clientTier == '6') {
        enterprises = ['985069', '985070'];
      } else if (isuCd == '34' && clientTier == 'V') {
        enterprises = ['985014', '985055'];
      }
    } else if ((SysLoc.UZBEKISTAN == cntry || SysLoc.TURKMENISTAN == cntry)
      && (FormManager.getActualValue('custSubGrp') == 'XTP' || FormManager.getActualValue('custSubGrp') == 'THDPT' || FormManager.getActualValue('custSubGrp') == 'XPC'
        || FormManager.getActualValue('custSubGrp') == 'PRICU' || FormManager.getActualValue('custSubGrp') == 'COMME' || FormManager.getActualValue('custSubGrp') == 'XCOM')) {
      if (isuCd == '32' && clientTier == 'S') {
        enterprises = ['985014'];
      }
    } else if (SysLoc.ALBANIA == cntry) {
      enterprises = [''];
    } else if (FormManager.getActualValue('custSubGrp') == 'XINT' || FormManager.getActualValue('custSubGrp') == 'INTER' || FormManager.getActualValue('custSubGrp') == 'CSINT'
      || FormManager.getActualValue('custSubGrp') == 'MEINT' || FormManager.getActualValue('custSubGrp') == 'RSXIN' || FormManager.getActualValue('custSubGrp') == 'RSINT'
      || FormManager.getActualValue('custSubGrp') == 'XBP' || FormManager.getActualValue('custSubGrp') == 'BUSPR' || FormManager.getActualValue('custSubGrp') == 'BP'
      || FormManager.getActualValue('custSubGrp') == 'CSBP' || FormManager.getActualValue('custSubGrp') == 'MEBP' || FormManager.getActualValue('custSubGrp') == 'RSXBP'
      || FormManager.getActualValue('custSubGrp') == 'RSBP') {
      enterprises = [''];
    } else {
      var qParams = {
        _qall: 'Y',
        ISSUING_CNTRY: cntry,
        ISU: '%' + isuCd + clientTier + '%'
      };
      var results = cmr.query('GET.ENTLIST.BYISU', qParams);
      if (results != null) {
        for (var i = 0; i < results.length; i++) {
          enterprises.push(results[i].ret1);
        }
      }
    }

    if (enterprises != null) {
      var field = FormManager.getField('enterprise');
      FormManager.limitDropdownValues(field, enterprises);
      if (enterprises.length == 1) {
        FormManager.setValue('enterprise', enterprises[0]);
      }
    }
  }
}

function setEnterprise2Values(dupClientTierCd) {
  // Russia not use dropdown value any more
  if (SysLoc.RUSSIA == FormManager.getActualValue('cmrIssuingCntry')) {
    return;
  }
  // CMR-4606
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }

  var dupIssuingCntryCd = FormManager.getActualValue('dupIssuingCntryCd');
  var dupIsuCd = FormManager.getActualValue('dupIsuCd');
  FormManager.enable('dupEnterpriseNo');
  dupClientTierCd = FormManager.getActualValue('dupClientTierCd');
  var enterprises = [];

  if (isuCd != '' && dupClientTierCd != '') {
    var qParams = {
      _qall: 'Y',
      ISSUING_CNTRY: dupIssuingCntryCd,
      ISU: '%' + dupIsuCd + dupClientTierCd + '%'
    };
    var results = cmr.query('GET.ENTLIST.BYISU', qParams);
    if (results != null) {
      for (var i = 0; i < results.length; i++) {
        enterprises.push(results[i].ret1);
      }
    }
  }

  if (enterprises != null) {
    FormManager.limitDropdownValues(FormManager.getField('dupEnterpriseNo'), enterprises);
    if (enterprises.length == 1) {
      FormManager.setValue('dupEnterpriseNo', enterprises[0]);
    }
    if (dupIsuCd == '34' && dupClientTierCd == 'Q') {
      if (SysLoc.UKRAINE == dupIssuingCntryCd) {
        FormManager.setValue('dupEnterpriseNo', '985024');
      } else if (SysLoc.MOLDOVA == dupIssuingCntryCd) {
        FormManager.setValue('dupEnterpriseNo', '985050');
      }
    }
  }
}

function populateBundeslandercode() {
  var subindustry = FormManager.getActualValue('subIndustryCd');
  var landCntry = FormManager.getActualValue('landCntry');

  var custGrp = FormManager.getActualValue('custGrp');
  if (custGrp == 'CROSS' || landCntry != 'AT') {
    if (subindustry != null) {
      FormManager.setValue('locationNumber', '001' + subindustry);
      return;
    }
  }

  var postCd = getPostCode();
  var reqParam = {
    CD: postCd
  };
  var results = cmr.query('GETTXT.BYPOSTCODE.AUSTRIA', reqParam);
  if (results != null) {
    var postCd = results.ret1;
    if (postCd != null && subindustry != null) {
      FormManager.setValue('locationNumber', postCd + subindustry);
    }
  }
}

function populateBundeslandercodeOnChange() {
  dojo.connect(FormManager.getField('postCd'), 'onChange', function (value) {
    populateBundeslandercode();
  });
  dojo.connect(FormManager.getField('subIndustryCd'), 'onChange', function (value) {
    populateBundeslandercode();
  });
}

function getPostCode() {
  var reqId = FormManager.getActualValue('reqId');
  if (reqId != null) {
    reqParam = {
      REQ_ID: reqId,
    };
  }
  var results = cmr.query('ADDR.GET.POSTCD.AUSTRIA', reqParam);
  if (results != null) {
    return results.ret1;
  }
}

var _addrTypesForMA = ['ZS01', 'ZP01', 'ZI01', 'ZD01', 'ZS02', 'ZP02'];
var addrTypeHandler = [];


function setVatValidator() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var custGroup = FormManager.getActualValue('custGrp');

  var excludeCountries = new Set(['644', '668', '693', '694', '704', '708', '740', '820', '821', '826', '889', '707']);
  var cntryRegion = FormManager.getActualValue('countryUse');
  if (excludeCountries.has(cntry) || cntryRegion == '707') {
    return;
  }

  if ((role == 'PROCESSOR' || role == 'REQUESTER') && (cntry == '620') && custGroup == 'LOCAL') {
    FormManager.addValidator('vat', Validators.REQUIRED, ['VAT'], 'MAIN_CUST_TAB');
  } else {
    FormManager.removeValidator('vat', Validators.REQUIRED);
  }
}
function resetVatExemptOnchange() {
  dojo.connect(FormManager.getField('vat'), 'onChange', function (value) {
    // resetVatExempt();
  });
}

function vatExemptOnScenario() {
  var custSubType = FormManager.getActualValue('custSubGrp');
  var subGrp = new Array();
  subGrp = ['COMME', 'GOVRN', 'BUSPR', 'THDPT', 'XCOM', 'XBP'];
  for (var i = 0; i < subGrp.length; i++) {
    if (custSubType == subGrp[i]) {
      if (dijit.byId('vatExempt').get('checked')) {
        FormManager.getField('vatExempt').set('checked', false);

      }
      break;
    }
  }

}


function lockLocationNo() {
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  var role = FormManager.getActualValue('userRole').toUpperCase();

  if (viewOnlyPage == 'true') {
    FormManager.readOnly('locationNumber');
  } else {
    if (role == 'REQUESTER') {
      FormManager.readOnly('locationNumber');
    }
  }
}

function austriaCustomVATValidator(cntry, tabName, formName, aType) {
  return function () {
    FormManager.addFormValidator((function () {
      var landCntry = cntry;
      var addrType = aType;
      return {
        validate: function () {
          var reqType = FormManager.getActualValue('reqType');
          var vat = FormManager.getActualValue('vat');
          var cntryUsed = '';
          var result = cmr.query('GET_CNTRYUSED', {
            REQ_ID: FormManager.getActualValue('reqId'),
          });
          if (result && result.ret1 && result.ret1 != '') {
            cntryUsed = result.ret1;
          }

          if (cntryUsed != null && cntryUsed.length > 0) {
            switch (cntryUsed) {
              /*
               * case '707ME': return new ValidationResult(null, true); break;
               * case '707CS': return new ValidationResult(null, true); break;
               * case '808AF': return new ValidationResult(null, true); break;
               */
            }
          }

          if (!vat || vat == '' || vat.trim() == '') {
            return new ValidationResult(null, true);
          } else if (reqType == 'U' && vat == '@') {
            // vat deletion for updates
            return new ValidationResult(null, true);
          }

          var zs01Cntry = landCntry;
          var addrExist = false;
          if (addrType != null && addrType != '') {
            var addrResult = cmr.query('GET.ADDR_BY_REQID_TYPE', {
              REQ_ID: FormManager.getActualValue('reqId'),
              ADDR_TYPE: addrType
            });
            if (addrResult && addrResult.ret1 && addrResult.ret1 != '') {
              addrExist = true;
            }
          }

          if (!addrExist) {
            addrType = 'ZS01';
          }

          var ret = cmr.query('VAT.GET_ZS01_CNTRY', {
            REQID: FormManager.getActualValue('reqId'),
            TYPE: addrType
          });
          if (ret && ret.ret1 && ret.ret1 != '') {
            zs01Cntry = ret.ret1;
          }
          console.log(addrType + ' VAT Country: ' + zs01Cntry);
          var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
          if (cmrIssuingCntry == '821' && zs01Cntry == 'RU' && vat.length == 12) {
            return new ValidationResult(null, true);
          }

          var result = cmr.validateVAT(zs01Cntry, vat);
          if (result && !result.success) {
            if (result.errorPattern == null) {
              return new ValidationResult({
                id: 'vat',
                type: 'text',
                name: 'vat'
              }, false, result.errorMessage + '.');
            } else {
              var msg = result.errorMessage + '. Format should be ' + result.errorPattern.formatReadable;
              return new ValidationResult({
                id: 'vat',
                type: 'text',
                name: 'vat'
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
function austriaCustomVATMandatory() {
  console.log('1austriaCustomVATMandatory ');
  var landCntry = '';
  var addrType = 'ZP01';
  var listVatReq = ['AT', 'AE', 'BG', 'HR', 'CS', 'CZ', 'EG', 'HU', 'KZ', 'PK', 'PL', 'RO', 'RU', 'SA', 'RS', 'SK', 'SI', 'UA'];
  if (CEE_INCL.has(FormManager.getActualValue('cmrIssuingCntry'))) {
    listVatReq = ['AT', 'BH', 'BE', 'BR', 'BG', 'HR', 'CY', 'CZ', 'DK', 'EG', 'EE', 'FI', 'FR', 'DE', 'GR', 'GL', 'HU', 'IS', 'IE', 'IL', 'IT', 'KZ', 'LV', 'LT', 'LU', 'MT', 'MA', 'NL', 'NO', 'PK',
      'PL', 'PT', 'RO', 'RU', 'SA', 'CS', 'SK', 'SI', 'ZA', 'ES', 'SE', 'CH', 'TR', 'UA', 'AE'];
  }

  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }

  // Internal, Softlayer, & Private scenario - set vat to optional
  var custSubType = FormManager.getActualValue('custSubGrp');
  if (custSubType != null && custSubType != '' && (custSubType.includes('IN') || custSubType == 'SOFTL' || custSubType.includes('SL') || custSubType == 'PRICU' || custSubType.includes('PC'))) {
    FormManager.resetValidations('vat');
    return;
  }

  /*
   * var result = cmr.query('GET_CNTRYUSED', { REQ_ID :
   * FormManager.getActualValue('reqId'), }); if (result && result.ret1 &&
   * result.ret1 != '') { cntryUsed = result.ret1; }
   */

  var zs01Cntry = landCntry;

  var ret = cmr.query('VAT.GET_ZS01_CNTRY', {
    REQID: FormManager.getActualValue('reqId'),
    TYPE: addrType ? addrType : 'ZS01'
  });
  if (ret && ret.ret1 && ret.ret1 != '') {
    zs01Cntry = ret.ret1;
  }
  console.log('ZP01 VAT Country: ' + zs01Cntry);

  var indx = listVatReq.indexOf(zs01Cntry);

  if (indx > -1 && !dijit.byId('vatExempt').get('checked')) {
    // Make Vat Mandatory
    FormManager.addValidator('vat', Validators.REQUIRED, ['VAT'], 'MAIN_CUST_TAB');
    console.log("Vat is Mandatory");
  }

};

/*
 * 1496135: Importing G address from SOF for Update Requests jz: add local
 * country name text box
 */
function toggleLocalCountryNameOnOpen() {
  if (reqType == 'U' && FormManager.getActualValue('addrType') == 'ZS01') {
    FormManager.readOnly('landCntry');
  } else {
    FormManager.enable('landCntry');
  }
  return;
}

/**
 * Override method on TemplateService
 */
function executeOnChangeOfAddrType() {
  return;
}

function changeBetachar() {
  var abbrvNm = FormManager.getActualValue('abbrevNm');
  var abbrevLocn = FormManager.getActualValue('abbrevLocn');
  if (abbrvNm.indexOf("β")) {
    abbrvNm = abbrvNm.replace(/β/g, "ss");
    FormManager.setValue('abbrevNm', abbrvNm);
  }
  if (abbrevLocn.indexOf("β")) {
    abbrevLocn = abbrevLocn.replace(/β/g, "ss");
    FormManager.setValue('abbrevLocn', abbrevLocn);
  }

}

function handleRequestLOBChange() {
  if (FormManager.getActualValue('reqType') == 'C') {
    dojo.connect(FormManager.getField('requestingLob'), 'onChange', function (value) {
      if (FormManager.getActualValue('requestingLob') == 'DSW') {
        var abbrvNm = FormManager.getActualValue('abbrevNm');
        if (abbrvNm == null || abbrvNm.length == 0 || abbrvNm.lastIndexOf(" DSW") > 0) {
          return;
        } else {
          if (abbrvNm.length > 30) {
            abbrvNm = abbrvNm.slice(0, 27) + ' DSW';
          } else {
            abbrvNm = abbrvNm + ' DSW';
            FormManager.setValue('abbrevNm', abbrvNm);
          }
        }
      }
    });
  }
}

function filterCmrnoForAT() {
  var cmrNo = FormManager.getActualValue('cmrNo');
  if (cmrNo.length > 0 && cmrNo.substr(0, 1).toUpperCase() == 'P') {
    FormManager.setValue('cmrNo', '');
  }

  dojo.connect(FormManager.getField('cmrNo'), 'onChange', function (value) {
    if (value.length > 0 && value.substr(0, 1).toUpperCase() == 'P') {
      FormManager.setValue('cmrNo', '');
    }
  });
}

function restrictDuplicateAddrAT(cntry, addressMode, saving, finalSave, force) {
  FormManager.addFormValidator(
    (function () {
      return {
        validate: function () {
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
              REQ_ID: requestId,
              ADDR_SEQ: addressSeq,
              ADDR_TYPE: addressType
            };
          } else {
            qParams = {
              REQ_ID: requestId,
              ADDR_SEQ: dummyseq,
              ADDR_TYPE: addressType
            };
          }
          var result = cmr.query('GETADDRECORDSBYTYPE', qParams);
          var addCount = result.ret1;
          if (addressType != undefined && addressType != '' && addressType == 'ZP02' && cmr.addressMode != 'updateAddress') {
            showDuplicateIGFBillToError = Number(addCount) >= 1 && addressType == 'ZP02';
            if (showDuplicateIGFBillToError) {
              return new ValidationResult(null, false,
                'Only one IGF Bill To address is allowed. If you still want to create new address , please delete the existing one and then create a new address.');
            }
          }

          if (addressType != undefined && addressType != '' && addressType == 'ZD02' && cmr.addressMode != 'updateAddress') {
            showDuplicateIGFInstallAtToError = Number(addCount) >= 1 && addressType == 'ZD02';
            if (showDuplicateIGFInstallAtToError) {
              return new ValidationResult(null, false,
                'Only one IGF Install At to address is allowed. If you still want to create new address , please delete the existing one and then create a new address.');
            }
          }

          return new ValidationResult(null, true);
        }
      };
    })(), null, 'frmCMR_addressModal');

}

function handleLocalLangCountryName(type) {
  FormManager.resetValidations('bldg');
  FormManager.resetValidations('landCntry');
  if (type == 'ZP02') {
    // local language
    /* Defect : 1590750 */
    // FormManager.disable('landCntry');
    FormManager.enable('bldg');
    FormManager.mandatory('bldg', 'LocalLangCountryName', null);
  } else {
    // FormManager.enable('landCntry');
    FormManager.disable('bldg');
    FormManager.mandatory('landCntry', 'LocalLangCountryName', null);
    lockLandCntry();
  }
  // CREATCMR-788
  FormManager.addValidator('bldg', Validators.NO_QUOTATION, ['Country Name (Local Language)']);
}

/**
 * Override from address.js
 * 
 * @param value
 * @param rowIndex
 * @returns
 */

function setTypeOfCustomerRequiredProcessor() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var bpAcctTyp = FormManager.getActualValue('bpAcctTyp');
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (bpAcctTyp == '' || bpAcctTyp == null) {
    FormManager.setValue('bpAcctTyp', 'N');
  }
  if (role == 'REQUESTER') {
    FormManager.removeValidator('bpAcctTyp', Validators.REQUIRED);
  } else if (role == 'PROCESSOR') {
    checkAndAddValidator('bpAcctTyp', Validators.REQUIRED, ['Type Of Customer']);
  }
}



function togglePPSCeidCEE() {
  var reqType = null;
  if (typeof (_pagemodel) != 'undefined') {
    reqType = FormManager.getActualValue('reqType');
  }
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var reqType = FormManager.getActualValue('reqType');
  var _custType = FormManager.getActualValue('custSubGrp');
  var isuCd = FormManager.getActualValue('isuCd');
  var clientTier = FormManager.getActualValue('clientTier');
  var cmrNo = FormManager.getActualValue('cmrNo');
  if (_custType == 'BUSPR' || _custType == 'XBP' || _custType == 'CSBP' || _custType == 'MEBP' || _custType == 'RSXBP' || _custType == 'RSBP') {
    FormManager.show('PPSCEID', 'ppsceid');
    FormManager.enable('ppsceid');
    FormManager.resetValidations('ppsceid');
    FormManager.addValidator('ppsceid', Validators.REQUIRED, ['PPS CEID'], 'MAIN_IBM_TAB');
  } else if (reqType == 'U') {
    FormManager.show('PPSCEID', 'ppsceid');
    FormManager.removeValidator('ppsceid', Validators.REQUIRED);
    FormManager.readOnly('ppsceid');
  } else {
    FormManager.clearValue('ppsceid');
    FormManager.hide('PPSCEID', 'ppsceid');
    FormManager.removeValidator('ppsceid', Validators.REQUIRED);
  }
}

function setICOAndDICMandatory() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (!dijit.byId('vatExempt')) {
    window.setTimeout('setICOAndDICMandatory()', 500);
  } else if (dijit.byId('vatExempt').get('checked')) {
    FormManager.removeValidator('company', Validators.REQUIRED);
  } else {
    var _custType = FormManager.getActualValue('custSubGrp');
    var role = FormManager.getActualValue('userRole').toUpperCase();
    if (role == 'REQUESTER' && (_custType == 'BUSPR' || _custType == 'COMME' || _custType == 'THDPT')) {
      FormManager.resetValidations('company');
      FormManager.addValidator('company', Validators.REQUIRED, ['IČO'], 'MAIN_CUST_TAB');
    } else {
      FormManager.removeValidator('company', Validators.REQUIRED);
    }
  }
}

function isZD01OrZP01ExistOnCMR(addressType) {
  if (addressType == 'ZP03') {
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
    var reqType = FormManager.getActualValue('reqType');
    if ('U' == reqType && 'Y' == importInd && type == addressType) {
      return true;
    }
  }
  return false;
}



var _importedSearchTerm = null;
function resetSortlValidator() {
  var reqId = FormManager.getActualValue('reqId');
  var reqType = FormManager.getActualValue('reqType');

  var qParams = {
    REQ_ID: reqId,
  };
  var result = cmr.query('GET.SEARCH_TERM_DATA_RDC', qParams);
  if (result != null) {
    _importedSearchTerm = result.ret1;
  }

  if (reqType == 'U' && (_importedSearchTerm == '' || _importedSearchTerm == null)) {
    console.log('Making Sortl optinal as it is empty in RDC');
    FormManager.resetValidations('salesBusOffCd');
  }
}

function validateSortl() {
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
        var reqId = FormManager.getActualValue('reqId');
        var searchTerm = FormManager.getActualValue('salesBusOffCd');
        var letterNumber = /^[0-9a-zA-Z]+$/;
        var qParams = {
          REQ_ID: reqId,
        };
        if (_importedSearchTerm != searchTerm) {
          console.log("validating Sortl..");
          if (searchTerm.length != 8) {
            return new ValidationResult(null, false, 'SBO should be 8 characters long.');
          }

          if (!searchTerm.match(letterNumber)) {
            return new ValidationResult({
              id: 'salesBusOffCd',
              type: 'text',
              name: 'searchTerm'
            }, false, 'SBO should be alpha numeric.');
          }
        }

        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}


function validateDeptBldg() {
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
        var reqType = FormManager.getActualValue('reqType');
        var custNm3 = FormManager.getActualValue('custNm3');
        var custNm4 = FormManager.getActualValue('custNm4');
        var bldg = FormManager.getActualValue('bldg');
        var dept = FormManager.getActualValue('dept');
        if ((custNm3 != '' && (custNm3 == bldg || custNm3 == dept)) || (custNm4 != '' && (custNm4 == bldg || custNm4 == dept))) {
          return new ValidationResult(null, false, 'Department_ext and Building_ext must contain unique information.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function setAddressDetailsForViewAT() {
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  if (viewOnlyPage == 'true' && cmrIssuingCntry == SysLoc.AUSTRIA) {
    $('label[for="custNm1_view"]').text('Customer Legal name:');
    $('label[for="custNm2_view"]').text('Legal Name Continued:');
    $('label[for="custNm3_view"]').text('Division/Department:');
    $('label[for="custNm4_view"]').text('Attention To/Building/Floor/Office:');
    $('label[for="addrTxt_view"]').text('Street Name And Number:');
    $('label[for="bldg_view"]').text('Building_Ext:');
    $('label[for="dept_view"]').text('Department_Ext:');
  }
}

function checkCmrUpdateBeforeImport() {
  FormManager.addFormValidator((function () {
    return {
      validate: function () {

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
          COUNTRY: cntry,
          CMRNO: cmrNo,
          MANDT: cmr.MANDT
        });

        if (resultsCC != null && resultsCC != undefined && resultsCC.ret1 != '') {
          uptsrdc = resultsCC.ret1;
          // console.log('lastupdatets in RDC = ' + uptsrdc);
        }

        var results11 = cmr.query('GETUPTSADDR', {
          REQ_ID: reqId
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

function clientTierCodeValidator() {
  var isuCode = FormManager.getActualValue('isuCd');
  var clientTierCode = FormManager.getActualValue('clientTier');
  var reqType = FormManager.getActualValue('reqType');

  if(!checkIfISUhasCorrectCTC(isuCode)) {
    return new ValidationResult({
      id : 'clientTier',
      type : 'text',
      name : 'clientTier'
    }, false, `Client Tier ${clientTierCode} not compatible with ISU ${isuCode}.`);
  }

  if (((isuCode == '21' || isuCode == '8B' || isuCode == '5K') && reqType == 'C') || ((isuCode != '34' && isuCode != '32' && isuCode != '36') && reqType == 'U')) {
    if (clientTierCode == '') {
      $("#clientTierSpan").html('');

      return new ValidationResult(null, true);
    }
  } else if (isuCode == '34') {
    if (clientTierCode == '') {
      return new ValidationResult({
        id: 'clientTier',
        type: 'text',
        name: 'clientTier'
      }, false, 'Client Tier code is Mandatory.');
    } else if (clientTierCode == 'Q') {
      return new ValidationResult(null, true);
    } else {
      return new ValidationResult({
        id: 'clientTier',
        type: 'text',
        name: 'clientTier'
      }, false, 'Client Tier can only accept \'Q\'\'.');
    }
  } else if (isuCode == '32') {
    if (clientTierCode == '') {
      return new ValidationResult({
        id: 'clientTier',
        type: 'text',
        name: 'clientTier'
      }, false, 'Client Tier code is Mandatory.');
    } else if (clientTierCode == 'T') {
      return new ValidationResult(null, true);
    } else {
      return new ValidationResult({
        id: 'clientTier',
        type: 'text',
        name: 'clientTier'
      }, false, 'Client Tier can only accept \'T\'\'.');
    }
  } else if (isuCode == '36') {
    if (clientTierCode == '') {
      return new ValidationResult({
        id: 'clientTier',
        type: 'text',
        name: 'clientTier'
      }, false, 'Client Tier code is Mandatory.');
    } else if (clientTierCode == 'Y') {
      return new ValidationResult(null, true);
    } else {
      return new ValidationResult({
        id: 'clientTier',
        type: 'text',
        name: 'clientTier'
      }, false, 'Client Tier can only accept \'Y\'\'.');
    }
  } else if (isuCode == '27') {
    if (clientTierCode == '') {
      return new ValidationResult({
        id: 'clientTier',
        type: 'text',
        name: 'clientTier'
      }, false, 'Client Tier code is Mandatory.');
    } else if (clientTierCode == 'E') {
      return new ValidationResult(null, true);
    } else {
      return new ValidationResult({
        id: 'clientTier',
        type: 'text',
        name: 'clientTier'
      }, false, 'Client Tier can only accept \'E\'\'.');
    }
  } else {
    let permittedValues = ['Q', 'Y', 'E', 'T', '']
    if (permittedValues.includes(clientTierCode)) {
      $("#clientTierSpan").html('');

      return new ValidationResult(null, true);
    } else {
      $("#clientTierSpan").html('');
      $("#clientTierSpan").append('<span style="color:red" class="cmr-ast" id="ast-clientTier">* </span>');

      return new ValidationResult({
        id: 'clientTier',
        type: 'text',
        name: 'clientTier'
      }, false, `Client Tier can only accept ${permittedValues.join(', ')} or blank.`);
    }
  }
}

function clientTierValidator() {
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
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
            REQ_ID: requestId,
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

function validateISUCTC() {
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
        var isuCd = FormManager.getActualValue('isuCd');
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        var scenarios = [
          {
            scn: ['COMME', 'GOVRN', 'THDPT', 'XCOM'],
            isu: ['27','34','36','5K'],
          },
          {
            scn: ['BUSPR', 'XBP'],
            isu: ['8B'],
          },
          {
            scn: ['INTER', 'INTSO', 'IBMEM'],
            isu: ['21'],
          }
        ]

        for (const scenario of scenarios) {
          if(scenario.scn.includes(custSubGrp) && !scenario.isu.includes(isuCd)) {
            return new ValidationResult({
              id: 'clientTier',
              type: 'text',
              name: 'clientTier'
            }, false, `ISU ${isuCd} is invalid for the current subscenario`);
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

// CREATCMR-6378
function retainVatValueAT() {
  var vat = FormManager.getActualValue('vat');
  var reqId = FormManager.getActualValue('reqId');
  var qParams = {
    REQ_ID: reqId,
  };
  if (vat == '' || vat == null || vat == undefined) {
    var result = cmr.query('ADDR.GET.VAT_REQID', qParams);
    var _vat = result.ret1;
    FormManager.setValue('vat', _vat);
  }
}

// CREATCMR-788
function addressQuotationValidatorAUSTRIA() {
  var cmrIssueCntry = FormManager.getActualValue('cmrIssuingCntry');
  FormManager.addValidator('abbrevLocn', Validators.NO_QUOTATION, ['Abbreviated Location'], 'MAIN_CUST_TAB');
  FormManager.addValidator('abbrevNm', Validators.NO_QUOTATION, ['Abbreviated Name (TELX1)'], 'MAIN_CUST_TAB');
  FormManager.addValidator('custNm1', Validators.NO_QUOTATION, ['Customer Legal name']);
  FormManager.addValidator('custNm2', Validators.NO_QUOTATION, ['Legal Name Continued']);
  FormManager.addValidator('custNm3', Validators.NO_QUOTATION, ['Division/Department']);
  FormManager.addValidator('custNm4', Validators.NO_QUOTATION, ['Attention To/Building/Floor/Office']);
  FormManager.addValidator('city1', Validators.NO_QUOTATION, ['City']);
  FormManager.addValidator('bldg', Validators.NO_QUOTATION, ['Building_Ext']);
  FormManager.addValidator('dept', Validators.NO_QUOTATION, ['Department_Ext']);
  FormManager.addValidator('addrTxt', Validators.NO_QUOTATION, ['Street Name And Number']);
  FormManager.addValidator('postCd', Validators.NO_QUOTATION, ['Postal Code']);
  FormManager.addValidator('custPhone', Validators.NO_QUOTATION, ['Phone number']);
  FormManager.addValidator('poBox', Validators.NO_QUOTATION, ['PO BOX']);
}

function setCTCBasedOnISUCode() {
  isuCd = FormManager.getActualValue('isuCd');

  FormManager.setValue('clientTier', CTC_MAPPING[isuCd] || '');
}


function setIsuInitialValueBasedOnSubScenario() {
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var isuCd = FormManager.getActualValue('isuCd');
  var scenarios = ['COMME', 'GOVRN', 'THDPT']

  // pre-select ISU 27 for commercial, government, third party and private
  // person.
  if (scenarios.includes(custSubGrp) && isuCd.length == 0) {
    FormManager.setValue('isuCd', '27');
  }
}

function setDropdownForScenarios() {
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var scenarios = [
    {
      scn: ['COMME', 'GOVRN', 'THDPT', 'XCOM'],
      isus: ['27','34','36','5K'],
    },
    {
      scn: ['BUSPR', 'XBP'],
      isus: ['8B'],
    },
    {
      scn: ['INTER', 'INTSO', 'IBMEM'],
      isus: ['21'],
    }
  ]

  for(const scenario of scenarios){
    if(scenario.scn.includes(custSubGrp)){
      FormManager.limitDropdownValues(FormManager.getField('isuCd'), scenario.isus)
      break;
    }
  }
}

function checkIfISUhasCorrectCTC(isuCd) {
  if(!(['27', '34', '36'].includes(isuCd))) return true

  if(CTC_MAPPING[isuCd].length == 0) {
    return false;
  }

  return true;
}

function getCurrentPostalCode() {
  var postCd = FormManager.getActualValue('postCd');
  if(postCd == '') {
    postCd = getPostCode()
  }
  return postCd;
}



function lockFields() {
  const doForThoseFieldsInSubscenarios = (fields, scenarios, action) => {
    var custSubGrp = FormManager.getActualValue('custSubGrp');
    for (scenario of scenarios){
      if(scenario == custSubGrp) {
        for (field of fields) {
          if(action == "LOCK") FormManager.readOnly(field);
          if(action == "UNLOCK") FormManager.enable(field);
        }
      }
    }
  }

  doForThoseFieldsInSubscenarios(['salesBusOffCd'], ['INTSO', 'INTER', 'BUSPR'], 'LOCK')
}

dojo.addOnLoad(function () {
  console.log('adding AUSTRIA functions...');
  GEOHandler.enableCustomerNamesOnAddress([SysLoc.AUSTRIA]);
  for (const func of [
    afterConfigForAUSTRIA,
    addHandlersForAUSTRIA,
    setAbbrvNmLoc,
    lockAbbrv,
    lockOrdBlk,
    setClientTierValuesAT,
    reqReasonOnChangeAT,
    setEnterpriseValues,
    setVatRequired,
    setCommercialFinanced,
    setTelecoverageRep,
    lockLandCntry,
    populateBundeslandercode,
    populateBundeslandercodeOnChange,
    setVatValidator,
    resetVatExemptOnchange,
    lockLocationNo,
    handleRequestLOBChange,
    filterCmrnoForAT,
    changeBetachar,
    validateAbbrevNmLocn,
    validateSBO,
    setISUCTCOnIMSChange,
    setAddressDetailsForViewAT,
    resetSortlValidator,
    setDropdownForScenarios,
    lockFields
  ]) {
    GEOHandler.addAfterConfig(func, [SysLoc.AUSTRIA]);
  }

  for (const func of [
    addAUSTRIALandedCountryHandler,
    changeAbbrevNmLocn,
    addLatinCharValidator,
    toggleLocalCountryNameOnOpen,
    getSBOandAssignFirstValue
  ]) {
    GEOHandler.addAddrFunction(func, [SysLoc.AUSTRIA]);
  }

  for (const func of [
    setClientTierValuesAT,
    setVatRequired,
    setCommercialFinanced,
    setTelecoverageRep,
    lockLandCntry,
    populateBundeslandercode,
    setVatValidator,
    afterConfigForAUSTRIA,
    filterCmrnoForAT,
    validateSBO,
    setISUCTCOnIMSChange,
    togglePPSCeidCEE,
    resetSortlValidator,
    setDropdownForScenarios,
    setCTCBasedOnISUCode,
    lockFields
  ]) {
    GEOHandler.addAfterTemplateLoad(func, [SysLoc.AUSTRIA]);
  }

  for (const func of [
    orderBlockValidation,
    addAddressFieldValidators,
    addCmrNoValidator,
    addStreetAndPoBoxFormValidator,
    checkCmrUpdateBeforeImport,
    clientTierValidator,
    validateSBOValuesForIsuCtc,
    validateSortl,
    validateISUCTC,
    austriaCustomVATValidator(SysLoc.AUSTRIA, 'MAIN_CUST_TAB', 'frmCMR', 'ZP01')
  ]) {
    GEOHandler.registerValidator(func, [SysLoc.AUSTRIA], null, true);
  }

  GEOHandler.registerValidator(restrictDuplicateAddrAT, [SysLoc.AUSTRIA]);
  GEOHandler.registerValidator(validateSBO, [SysLoc.AUSTRIA], GEOHandler.ROLE_PROCESSOR, true);
  GEOHandler.registerValidator(validateDeptBldg, SysLoc.AUSTRIA);
});
