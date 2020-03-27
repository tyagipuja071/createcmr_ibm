/**
 * 
 */
/* Register FR Javascripts */

var _addrTypesForFR = [ 'ZP01', 'ZS01', 'ZI01', 'ZD01', 'ZS02' ];
var _poBOXHandler = [];
var EU_COUNTRIES = ["AT", "BE", "BG", "HR", "CY", "CZ", "DE", "DK", "EE", "GR", "FI", "FR", "GB", "HU",
        "IE", "IT", "LT", "LU", "LV", "MT", "NL", "PL", "PT", "RO", "SE", "SI", "SK"];
var VAT_EU_COUNTRY = [ "AT", "BE", "BG", "HR", "CY", "CZ", "EG", "FR", "DE", "GR", "HU", "IE", "IL", "IT", "LU", "MT", "NL", "PK", "PL", "PT", "RO", "RU", "RS", "SK", "SI", "ZA", "ES", "TR", "GB", "UA"];

function afterConfigForFR() {
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
  
  affacturageLogic();
  setFieldsRequiredForCreateRequester();

  if (role == 'Requester') {
    FormManager.readOnly('abbrevNm');
    FormManager.readOnly('abbrevLocn');
    FormManager.readOnly('taxCd2');
    FormManager.readOnly('poBox');
    FormManager.readOnly('embargoCd');
    FormManager.readOnly('currencyCd');
    if(reqType == 'U')
    {
    FormManager.enable('taxCd2');
    FormManager.enable('currencyCd');
    }
    }
     else if (role == 'Processor') {
    FormManager.enable('abbrevNm');
    FormManager.enable('abbrevLocn');
    FormManager.enable('taxCd2');
    FormManager.enable('embargoCd');
    FormManager.enable('currencyCd');
    FormManager.addValidator('taxCd2', Validators.REQUIRED, [ 'Tax Code' ], 'MAIN_CUST_TAB');
  }

  if (role == 'Processor' && reqType == 'C') {
    FormManager.addValidator('isuCd', Validators.REQUIRED, [ 'ISU Code' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('clientTier', Validators.REQUIRED, [ 'Client Tier' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('salesBusOffCd', Validators.REQUIRED, [ 'Search Term/Sales Branch Office' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('installBranchOff', Validators.REQUIRED, [ 'Installing BO' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('repTeamMemberNo', Validators.REQUIRED, [ 'Sales Rep No' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('subIndustryCd', Validators.REQUIRED, [ 'Subindustry' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('abbrevNm', Validators.REQUIRED, [ 'Abbreviated Name (TELX1)' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('abbrevLocn', Validators.REQUIRED, [ 'Abbreviated Location' ], 'MAIN_CUST_TAB');
  }

  var _custSubGrpHandler = dojo.connect(FormManager.getField('custSubGrp'), 'onChange', function(value) {
    setAbbrevNmOnCustSubGrpChange();
    setAbbrevLocnOnCustSubGrpChange();
    setDummySIRETOnCustSubGrpChange();
    setHideFieldForFR();
    setINACOnScenario();
    setISICAndSubindustryOnScenario();
    setVATOnScenario();
    setSBOOnScenario();
    setSalesRepLogic();
    setTaxCdOnScnrio();
    affacturageLogic();
    setISUClientTierOnScenario();
    setFieldsRequiredForCreateRequester();
  });
  if (_custSubGrpHandler && _custSubGrpHandler[0]) {
    _custSubGrpHandler[0].onChange();
  }
  
  dojo.connect(FormManager.getField('isicCd'), 'onChange', function(value) {
    var isicCd = FormManager.getActualValue('isicCd');
    var countyCd = FormManager.getActualValue('countryUse');
    if(countyCd == '706NC'){
      if(isicCd == '1010' || isicCd == '1020' || isicCd == '1200' || isicCd == '1310' || isicCd == '1320' || isicCd == '1421' || isicCd == '1429'){
      FormManager.resetDropdownValues(FormManager.getField('taxCd2'));
      FormManager.setValue('taxCd2','42');  
      FormManager.readOnly('taxCd2');
      }else{
      FormManager.resetDropdownValues(FormManager.getField('taxCd2'));
      FormManager.setValue('taxCd2','41');  
      FormManager.readOnly('taxCd2');
    }
  }
  });
  
// set defaultLandedCountry
  var landCntry = '';
  var cntryRegion = FormManager.getActualValue('countryUse');
  if (cntryRegion && cntryRegion != '' && cntryRegion.length > 3) {
    landCntry = cntryRegion.substring(3, 5);
  }
  if (landCntry != '') {
    FormManager.setValue('defaultLandedCountry', landCntry);
  }
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
    countyCd = countryUse.substring(3,5);
  } else {
    countyCd = "FR";
  }
  if (countyCd == 'MC') {
    if (role == 'Requester') {
      FormManager.setValue('inacCd','MI80');
      FormManager.readOnly('inacCd');
      
    } else if (role == 'Processor') {
      FormManager.enable('inacCd');
    }
  }
  if(countyCd == 'FR' && subGrp == 'INTER'){
	  FormManager.setValue('inacCd','');
      FormManager.readOnly('inacCd');
  }
  if(countyCd == 'FR' && subGrp == 'IBMEM' || subGrp == 'CBIEM' || subGrp == 'PRICU' || subGrp == 'CBICU'){
	  FormManager.setValue('inacCd','');
      FormManager.readOnly('inacCd');
  }
}
function setISICAndSubindustryOnScenario() {
  var role = null;
  var reqType = FormManager.getActualValue('reqType');
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }
  if (reqType == 'U') {
    return;
  }
  
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  
  if (custSubGrp == 'PRICU' || custSubGrp == 'CBICU' || custSubGrp == 'IBMEM' || custSubGrp == 'CBIEM') {
    if (role == 'Requester') {
      FormManager.setValue('isicCd','9500');
      FormManager.setValue('subIndustryCd','WQ');
      FormManager.readOnly('isicCd');
      FormManager.readOnly('subIndustryCd');
      
    } else if (role == 'Processor') {
      FormManager.enable('isicCd');
      FormManager.enable('subIndustryCd');
    }
  } else {
    FormManager.enable('isicCd');
    FormManager.enable('subIndustryCd');
  }
}
function setVATOnScenario() {
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
  if (role == 'Processor') {
    return;
  }
  
  var countyCd = null;
  var countryUse = FormManager.getActualValue('countryUse');
  if (countryUse.length > 3) {
    countyCd = countryUse.substring(3,5);
  } else {
    countyCd = "FR";
  }
  
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (custSubGrp == '') {
    return
  } else {
    if (custSubGrp == 'PRICU' || custSubGrp == 'CBICU' || custSubGrp == 'IBMEM' || custSubGrp == 'CBIEM') {
      FormManager.readOnly('vat');
      FormManager.removeValidator('vat', Validators.REQUIRED);
      
    } else if (custSubGrp == 'COMME' || custSubGrp == 'FIBAB' || custSubGrp == 'BPIEU' || custSubGrp == 'BPUEU' || custSubGrp == 'GOVRN' || custSubGrp == 'INTER' || custSubGrp == 'INTSO' || custSubGrp == 'LCIFF' || custSubGrp == 'LCIFL' || custSubGrp == 'OTFIN' || custSubGrp == 'LEASE' || custSubGrp == 'LCOEM' || custSubGrp == 'HOSTC' || custSubGrp == 'THDPT') {
      if (countyCd == 'FR' || countyCd == 'AD') {
        FormManager.enable('vat');
        FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
      } else {
        FormManager.removeValidator('vat', Validators.REQUIRED);
        FormManager.enable('vat');
      }
      
    } else if (custSubGrp == 'CBMME' || custSubGrp == 'CBBAB' || custSubGrp == 'CBIEU' || custSubGrp == 'CBUEU' || custSubGrp == 'CBVRN' || custSubGrp == 'CBTER' || custSubGrp == 'CBTSO' || custSubGrp == 'CBIFF' || custSubGrp == 'CBIFL' || custSubGrp == 'CBFIN' || custSubGrp == 'CBASE' || custSubGrp == 'CBOEM' || custSubGrp == 'CBSTC' || custSubGrp == 'CBDPT') {
      var isVATEUCntry = false;
      var reqId = FormManager.getActualValue('reqId');
      var qParams = {
          REQ_ID : reqId,
          ADDR_TYPE : 'ZS01',
      };
      var _result = cmr.query('ADDR.GET.LANDCNTRY.BY_REQID_ADDRTYP', qParams);
      var landCntry = _result.ret2;
      for(var i=0;i<VAT_EU_COUNTRY.length;i++){
        if(VAT_EU_COUNTRY[i] == landCntry){
          isVATEUCntry = true;
          break;
        }
      }
      if(isVATEUCntry == true){
        FormManager.enable('vat');
        FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
      } else {
        FormManager.removeValidator('vat', Validators.REQUIRED);
        FormManager.enable('vat');
      }
    }
  }
}

function setSBOOnScenario() {
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
  var countyCd = null;
  var countryUse = FormManager.getActualValue('countryUse');
  if (countryUse.length > 3) {
    countyCd = countryUse.substring(3,5);
  } else {
    countyCd = "FR";
  }
  
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (custSubGrp == '') {
    return
  } else {
    if (custSubGrp == 'INTER' || custSubGrp == 'CBTER') {
      if (role == 'Requester') {
        FormManager.setValue('salesBusOffCd','98F');
        FormManager.readOnly('salesBusOffCd');
      } else if (role == 'Processor') {
        FormManager.setValue('salesBusOffCd','98F');
        FormManager.enable('salesBusOffCd');
      }
    } else if (custSubGrp == 'LCOEM' || custSubGrp == 'CBOEM') {
      if (role == 'Requester') {
        FormManager.setValue('salesBusOffCd','581');
        FormManager.readOnly('salesBusOffCd');
      } else if (role == 'Processor') {
        FormManager.enable('salesBusOffCd');
      }
    } else if (custSubGrp == 'CBMME' || custSubGrp == 'CBIEU' || custSubGrp == 'CBUEU' || custSubGrp == 'CBICU' || custSubGrp == 'CBVRN' || custSubGrp == 'CBIFF' || custSubGrp == 'CBIFL' || custSubGrp == 'CBFIN' || custSubGrp == 'CBASE' || custSubGrp == 'CBBAB' || custSubGrp == 'CBSTC' || custSubGrp == 'CBDPT' || custSubGrp == 'CBIEM') {
      if (role == 'Requester') {
        FormManager.setValue('salesBusOffCd','200');
        FormManager.readOnly('salesBusOffCd');
      } else if (role == 'Processor') {
        FormManager.enable('salesBusOffCd');
      }
    } else if(custSubGrp == 'PRICU' || custSubGrp == 'IBMEM' || custSubGrp == 'CBIEM'){
        FormManager.readOnly('salesBusOffCd');
    }
    else {
      if (countyCd == "FR" || countyCd == "KM" || countyCd == "WF") {
        FormManager.enable('salesBusOffCd');
      } else if (countyCd == "MC") {
        if (role == 'Requester') {
          FormManager.setValue('salesBusOffCd','02M');
          FormManager.readOnly('salesBusOffCd');
        } else if (role == 'Processor') {
          FormManager.enable('salesBusOffCd');
        }
      } else if (countyCd == "GP") {
        if (role == 'Requester') {
          FormManager.setValue('salesBusOffCd','852');
          FormManager.readOnly('salesBusOffCd');
        } else if (role == 'Processor') {
          FormManager.enable('salesBusOffCd');
        }
      } else if (countyCd == "GF") {
        if (role == 'Requester') {
          FormManager.setValue('salesBusOffCd','853');
          FormManager.readOnly('salesBusOffCd');
        } else if (role == 'Processor') {
          FormManager.enable('salesBusOffCd');
        }
      } else if (countyCd == "MQ") {
        if (role == 'Requester') {
          FormManager.setValue('salesBusOffCd','851');
          FormManager.readOnly('salesBusOffCd');
        } else if (role == 'Processor') {
          FormManager.enable('salesBusOffCd');
        }
      } else if (countyCd == "RE") {
        if (role == 'Requester') {
          FormManager.setValue('salesBusOffCd','860');
          FormManager.readOnly('salesBusOffCd');
        } else if (role == 'Processor') {
          FormManager.enable('salesBusOffCd');
        }
      } else if (countyCd == "PM") {
        if (role == 'Requester') {
          FormManager.setValue('salesBusOffCd','853');
          FormManager.readOnly('salesBusOffCd');
        } else if (role == 'Processor') {
          FormManager.enable('salesBusOffCd');
        }
      } else if (countyCd == "VU") {
        if (role == 'Requester') {
          FormManager.setValue('salesBusOffCd','876');
          FormManager.readOnly('salesBusOffCd');
        } else if (role == 'Processor') {
          FormManager.enable('salesBusOffCd');
        }
      } else if (countyCd == "PF") {
        if (role == 'Requester') {
          FormManager.setValue('salesBusOffCd','873');
          FormManager.readOnly('salesBusOffCd');
        } else if (role == 'Processor') {
          FormManager.enable('salesBusOffCd');
        }
      } else if (countyCd == "YT") {
        if (role == 'Requester') {
          FormManager.setValue('salesBusOffCd','864');
          FormManager.readOnly('salesBusOffCd');
        } else if (role == 'Processor') {
          FormManager.enable('salesBusOffCd');
        }
      } else if (countyCd == "NC") {
        if (role == 'Requester') {
          FormManager.setValue('salesBusOffCd','872');
          FormManager.readOnly('salesBusOffCd');
        } else if (role == 'Processor') {
          FormManager.enable('salesBusOffCd');
        }
      } else if (countyCd == "AD") {
        if (role == 'Requester') {
          FormManager.setValue('salesBusOffCd','03T');
          FormManager.readOnly('salesBusOffCd');
        } else if (role == 'Processor') {
          FormManager.enable('salesBusOffCd');
        }
      } else if (countyCd == "DZ") {
        if (role == 'Requester') {
          FormManager.setValue('salesBusOffCd','711');
          FormManager.readOnly('salesBusOffCd');
        } else if (role == 'Processor') {
          FormManager.enable('salesBusOffCd');
        }
      } else if (countyCd == "TN") {
        if (role == 'Requester') {
          FormManager.setValue('salesBusOffCd','721');
          FormManager.readOnly('salesBusOffCd');
        } else if (role == 'Processor') {
          FormManager.enable('salesBusOffCd');
        }
      }
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
    countyCd = countryUse.substring(3,5);
  } else {
    countyCd = "FR";
  }
  if(countyCd == "DZ" && custGrp == "LOCAL")
    FormManager.setValue('repTeamMemberNo', 'A99999');
  else
    FormManager.setValue('repTeamMemberNo', 'D99999');
}

function add32PostCdCntrySBOlogicOnISUChange() {
  var _isuHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
    add32PostCdCntrySBOlogic();
  });
  if (_isuHandler && _isuHandler[0]) {
    _isuHandler[0].onChange();
  }
  var _subIndustryHandler = dojo.connect(FormManager.getField('subIndustryCd'), 'onChange', function(value) {
    add32PostCdCntrySBOlogic();
  });
  if (_subIndustryHandler && _subIndustryHandler[0]) {
    _subIndustryHandler[0].onChange();
  }
}

function addIBOlogic() {
  var reqType = FormManager.getActualValue('reqType');
  var role = null;
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }

  if (role == 'Processor') {
    return;
  }

  if (reqType == 'C' && role == 'Requester') {
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
        iboCd = iboCd.substring(iboCd.length-3, iboCd.length);
      }
      FormManager.setValue('installBranchOff', iboCd);
    }
  });
  if (_sboHandler && _sboHandler[0]) {
    _sboHandler[0].onChange();
  }
}

/*
 * Mandatory addresses ZS01/ZP01/ZI01 *Billing (Sold-to) *Installing (Install
 * at) *Mailing - not flowing into RDC!!
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
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount == 0 && reqType=='C') {
          return new ValidationResult(null, false,
              'Billing, installing address are mandatory. Only one address for each address type should be defined when sending for processing.');
        }else if(CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount == 0 && reqType=='U'){
          return new ValidationResult(null, false,
          'installing address are mandatory. Only one address for each address type should be defined when sending for processing.');
        }
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          var record = null;
          var type = null;
          var installingCnt = 0;
          var billingCnt = 0;
          var mailingCnt = 0;
          var shippingCnt = 0;
          var softwareCnt = 0;
          var hAddressCnt = 0;

          for ( var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
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
            } else if (type == 'ZP01') {
              billingCnt++;
            } else if (type == 'ZS02') {
              mailingCnt++;
            } else if (type == 'ZD01') {
              shippingCnt++;
            } else if (type == 'ZI01') {
              softwareCnt++;
            } else if (type == 'ZD02') {
              hAddressCnt++;
            }
          }
          
          if (reqType=='C'&&(billingCnt == 0 || installingCnt == 0) ) {
            return new ValidationResult(null, false,
                'Billing, installing address are mandatory. Other addresses are optional. Only one address for each address type should be defined when sending for processing.');
          } else if (reqType=='U'&&installingCnt == 0) {
            return new ValidationResult(null, false, 'installing address are mandatory. Other addresses are optional. Only one address for each address type should be defined when sending for processing.');
          }else if (installingCnt > 1) {
            return new ValidationResult(null, false, 'Only one Installing address can be defined. Please remove the additional Installing address.');
          } else if (billingCnt > 1) {
            return new ValidationResult(null, false, 'Only one Billing address can be defined. Please remove the additional Billing address.');
          } else if (mailingCnt > 1) {
            return new ValidationResult(null, false, 'Only one Mailing address can be defined. Please remove the additional Mailing address.');
          } else if (shippingCnt > 1) {
            return new ValidationResult(null, false, 'Only one Shipping address can be defined. Please remove the additional Shipping address.');
          } else if (softwareCnt > 1) {
            return new ValidationResult(null, false, 'Only one EPL/Software mailing address can be defined. Please remove the additional EPL/Software mailing address.');
          } else if (hAddressCnt > 1) {
            return new ValidationResult(null, false, 'Only one H Address (IGF) address can be defined. Please remove the additional H Address (IGF) address.');
          } else if (hAddressCnt > 0 && FormManager.getActualValue('requestingLob') != 'IGF' && reqType!='U') {
            return new ValidationResult(null, false, 'H Address (IGF) available only for IGF LOB. Please remove the H Address (IGF) address.');
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
  FormManager.addValidator('custNm1', Validators.LATIN, [ 'Customer Name' ]);
  FormManager.addValidator('custNm2', Validators.LATIN, [ 'Customer Name Continuation' ]);
  FormManager.addValidator('custNm3', Validators.LATIN, [ 'Customer Name/ Additional Address Information' ]);
  FormManager.addValidator('addrTxt', Validators.LATIN, [ 'Street' ]);
  FormManager.addValidator('addrTxt2', Validators.LATIN, [ 'Street Continuation' ]);
  FormManager.addValidator('city1', Validators.LATIN, [ 'City' ]);
}

function addALPHANUMSPACEValidatorFR() { 
  var role = null;
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }
  if(role == 'Processor'){
    FormManager.addValidator('abbrevLocn',  Validators.CHECKALPHANUMDASH, ['Abbreviated Location' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('abbrevNm',  Validators.CHECKALPHANUMDASH, [ 'Abbreviated Name' ], 'MAIN_CUST_TAB');
  }
  
  FormManager.addValidator('custNm1',  Validators.CHECKALPHANUMDASH, [ 'Customer Name' ]);
  FormManager.addValidator('custNm2',  Validators.CHECKALPHANUMDASHDOT, [ 'Customer Name Continuation' ]);
  FormManager.addValidator('custNm3',  Validators.CHECKALPHANUMDASH, [ 'Customer Name/ Additional Address Information' ]);
  FormManager.addValidator('addrTxt',  Validators.CHECKALPHANUMDASH, [ 'Street' ]);
  FormManager.addValidator('addrTxt2', Validators.CHECKALPHANUMDASH, [ 'Street Continuation' ]);
  FormManager.addValidator('city1', Validators.CHECKALPHANUMDASH, [ 'City' ]);
  if(role == 'Processor'){
    FormManager.addValidator('abbrevLocn',  Validators.CHECKSPACEPLACE4DASH, [ 'Abbreviated Location' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('abbrevNm',  Validators.CHECKSPACEPLACE4DASH, [ 'Abbreviated Name' ], 'MAIN_CUST_TAB');
  }
  FormManager.addValidator('custNm1',  Validators.CHECKSPACEPLACE4DASH, [ 'Customer Name' ]);
  FormManager.addValidator('custNm2',  Validators.CHECKSPACEPLACE4DASH, [ 'Customer Name Continuation' ]);
  FormManager.addValidator('custNm3',  Validators.CHECKSPACEPLACE4DASH, [ 'Customer Name/ Additional Address Information' ]);
  FormManager.addValidator('addrTxt',  Validators.CHECKSPACEPLACE4DASH, [ 'Street' ]);
  FormManager.addValidator('addrTxt2', Validators.CHECKSPACEPLACE4DASH, [ 'Street Continuation' ]);
  FormManager.addValidator('city1', Validators.CHECKSPACEPLACE4DASH, [ 'City' ]);
  if(role == 'Processor'){
    FormManager.addValidator('abbrevLocn',  Validators.CHECKDASHSPACE, [ 'Abbreviated Location' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('abbrevNm',  Validators.CHECKDASHSPACE, [ 'Abbreviated Name' ], 'MAIN_CUST_TAB');
  }
  FormManager.addValidator('custNm1',  Validators.CHECKDASHSPACE, [ 'Customer Name' ]);
  FormManager.addValidator('custNm2',  Validators.CHECKDASHSPACE, [ 'Customer Name Continuation' ]);
  FormManager.addValidator('custNm3',  Validators.CHECKDASHSPACE, [ 'Customer Name/ Additional Address Information' ]);
  FormManager.addValidator('addrTxt',  Validators.CHECKDASHSPACE, [ 'Street' ]);
  FormManager.addValidator('addrTxt2', Validators.CHECKDASHSPACE, [ 'Street Continuation' ]);
  FormManager.addValidator('city1', Validators.CHECKDASHSPACE, [ 'City' ]);

}

/**
 * story 1567051 - Address H available only for IGF LOB
 */
function toggleAddrTypesForFR(cntry, addressMode) {
  var requestingLob = FormManager.getActualValue('requestingLob');
  if (requestingLob == 'IGF') {
    if (addressMode == 'newAddress' || addressMode == 'copyAddress') {
      cmr.showNode('radiocont_ZD02');
    }
  } else {
    cmr.hideNode('radiocont_ZD02');
  }
  dojo.connect(FormManager.getField('requestingLob'), 'onChange', function(value) {
    var _requestingLob = FormManager.getActualValue('requestingLob');
    if (_requestingLob == 'IGF') {
      if (addressMode == 'newAddress' || addressMode == 'copyAddress') {
        cmr.showNode('radiocont_ZD02');
      }
    } else {
      cmr.hideNode('radiocont_ZD02');
    }
  });
}
function included(cntryCd) {
  var includedCntryCd = [ 'AD', 'MC', 'MQ', 'GP', 'GF', 'PM', 'RE', 'TF', 'KM', 'YT', 'NC', 'VU', 'WF', 'PF' ];
  var includedInd = false;
  for ( var i = 0; i < includedCntryCd.length; i++) {
    if (cntryCd == includedCntryCd[i])
      includedInd = true;
  }
  if (includedInd == true) {
    return true;
  } else {
    return false;
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
  var countryUse = FormManager.getActualValue('countryUse');
  if (countryUse.length > 3) {
    countyCd = countryUse.substring(3,5);
  } else {
    countyCd = "FR";
  }
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (!((countyCd == "FR" || countyCd == "KM" || countyCd == "WF") && (custSubGrp == 'COMME' || custSubGrp == 'BPIEU' || custSubGrp == 'BPUEU' || custSubGrp == 'PRICU' || custSubGrp == 'GOVRN' || custSubGrp == 'INTER' || custSubGrp == 'LCIFF' || custSubGrp == 'LCIFL' || custSubGrp == 'OTFIN' || custSubGrp == 'LEASE' || custSubGrp == 'LCOEM' || custSubGrp == 'FIBAB' || custSubGrp == 'HOSTC' || custSubGrp == 'THDPT' || custSubGrp == 'CBTER'))) {
    return;
  }
  
  var isuCd = FormManager.getActualValue('isuCd');
  if (isuCd != '32') {
    return;
  }

  FormManager.readOnly('salesBusOffCd');
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
      FormManager.setValue('salesBusOffCd', sboValue);
    } else if (postCd == '75' || postCd == '77' || postCd == '78' || postCd == '91' || postCd == '92' || postCd == '93' || postCd == '94' || postCd == '95') {
      var insustry = FormManager.getActualValue('subIndustryCd').substring(0, 1);
      if (insustry == 'A' || insustry == 'B' || insustry == 'C' || insustry == 'D' || insustry == 'F' || insustry == 'H' || insustry == 'K' || insustry == 'N' || insustry == 'R' || insustry == 'S' || insustry == 'T' || insustry == 'W') {
        FormManager.setValue('salesBusOffCd', '01D');
      } else if (insustry == 'E' || insustry == 'G' || insustry == 'J' || insustry == 'L' || insustry == 'M' || insustry == 'P' || insustry == 'U' || insustry == 'V' || insustry == 'X') {
        FormManager.setValue('salesBusOffCd', '01M'); 
      }
    }
  }
  
  // SBO scenario logic should over write the 32 postal code logic
  setSBOOnScenario();
}

function addPhoneValidatorFR() {
  FormManager.addValidator('custPhone', Validators.DIGIT, [ 'Phone #' ]);
}

function setAbbrevNmOnCustSubGrpChange() {
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

  var abbrevNmValue = null;
  var cntryCd = null;
  var singleIndValue = null;
  var departmentNumValue = null;

  var reqId = FormManager.getActualValue('reqId');
  var qParams = {
    REQ_ID : reqId,
    ADDR_TYPE : "ZS01",
  };
  var abbrevNmResult = cmr.query('ADDR.GET.CUSTNM1.BY_REQID_ADDRTYP', qParams);
  abbrevNmValue = abbrevNmResult.ret1;

  var landCntryResult = cmr.query('ADDR.GET.LANDEDCNTRY.BY_REQID_ADDRTYPE', qParams);
  cntryCd = landCntryResult.ret1;

  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (custSubGrp == "INTER" || custSubGrp == "CBTER") {
    if (cntryCd == "DZ") {
      departmentNumValue = "0371";
    } else if (cntryCd == "TN") {
      departmentNumValue = "0382";
    } else if (cntryCd == "YT" || cntryCd == "RE" || cntryCd == "VU") {
      departmentNumValue = "0381";
    } else if (cntryCd == "MQ") {
      departmentNumValue = "0385";
    } else if (cntryCd == "GP") {
      departmentNumValue = "0392";
    } else if (cntryCd == "GF" || cntryCd == "PM") {
      departmentNumValue = "0388";
    } else if (cntryCd == "NC" || cntryCd == "PF") {
      departmentNumValue = "0386";
    } else {
      departmentNumValue = FormManager.getActualValue('ibmDeptCostCenter');
    }
    singleIndValue = departmentNumValue;
  } else if (custSubGrp == "BPIEU" || custSubGrp == "BPUEU" || custSubGrp == "CBIEU" || custSubGrp == "CBUEU") {
    singleIndValue = "R5";
  } else if (custSubGrp == "CBTSO" || custSubGrp == "INTSO") {
    singleIndValue = "FM";
  } else if (custSubGrp == "CBIFF" || custSubGrp == "CBIFL" || custSubGrp == "CBFIN" || custSubGrp == "LCIFF" || custSubGrp == "LCIFL" || custSubGrp == "OTFIN") {
    singleIndValue = "F3";
  } else if (custSubGrp == "LEASE" || custSubGrp == "CBASE") {
    singleIndValue = "L3";
  } else {
    singleIndValue = "D3";
  }
  if (abbrevNmValue != null && abbrevNmValue.length > 19 && (custSubGrp != "INTER" && custSubGrp != "CBTER")) {
    abbrevNmValue = abbrevNmValue.substring(0, 19);
  } else if (abbrevNmValue != null && abbrevNmValue.length < 19 && (custSubGrp != "INTER" && custSubGrp != "CBTER")) {
    for ( var i = abbrevNmValue.length; i < 19; i++) {
      abbrevNmValue += ' '; 
    }
  } else if (abbrevNmValue != null && abbrevNmValue.length > 17 && (custSubGrp == "INTER" || custSubGrp == "CBTER")) {
    abbrevNmValue = abbrevNmValue.substring(0, 17);
  } else if (abbrevNmValue != null && abbrevNmValue.length < 17 && (custSubGrp == "INTER" || custSubGrp == "CBTER")) {
    for ( var i = abbrevNmValue.length; i < 17; i++) {
      abbrevNmValue += ' '; 
    }
  }

  // story 1507805 - set single identificateur 'DF' if having address H
  var checkZD02QParams = {
    REQ_ID : reqId,
    ADDR_TYPE : "ZD02",
  };
  var checkZD02Result = cmr.query('GET.ADDR_BY_REQID_TYPE', checkZD02QParams);
  var checkZD02Value = checkZD02Result.ret1;
  if (checkZD02Value) {
    singleIndValue = 'DF';
  }

  if (singleIndValue != null && abbrevNmValue != null) {
    abbrevNmValue = abbrevNmValue + ' ' + singleIndValue;
    FormManager.setValue('abbrevNm', abbrevNmValue);
  }
}

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
    countyCd = countryUse.substring(3,5);
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
function setDummySIRETOnCustSubGrpChange() {
  var reqType = null;
  var role = null;
  if (typeof (_pagemodel) != 'undefined') {
    reqType = FormManager.getActualValue('reqType');
    role = _pagemodel.userRole;
  }
  if (reqType == 'U') {
    return;
  }
  
  var dummySIRETValue = null;
  var countyCd = "";
  var city = "";
  var postCd = "";
  var custLocNumValue = "";
  var countryUse = FormManager.getActualValue('countryUse');
  var custGrp = FormManager.getActualValue('custGrp'); 
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  
  // set siret mandatory for processor, sub region 'France', all scenarios
  // except government, private person and IBM employee.
  // if (role == 'Processor') {
// if (custSubGrp != "GOVRN"&&custSubGrp != "PRICU"&&custSubGrp !=
// "IBMEM"&&custSubGrp != "CBVRN"&&custSubGrp != "CBICU"&&custSubGrp != "CBMEM")
// if (countryUse == "706")
// FormManager.addValidator('taxCd1', Validators.REQUIRED, [ 'SIRET' ],
// 'MAIN_CUST_TAB');
// return;
// }
	   
  var reqId = FormManager.getActualValue('reqId');
  var qParams = {
    REQ_ID : reqId,
    ADDR_TYPE : "ZS01",
  };
  var landCntryResult = cmr.query('ADDR.GET.CNTRY_CITY_PSCD.BY_REQID_ADDRTYPE', qParams);
  countyCd = landCntryResult.ret1;
  city = landCntryResult.ret2;
  postCd = landCntryResult.ret3;
  
  if (typeof (countyCd) != 'undefined'&& countyCd!=null && countyCd != 'FR') {
    var cntyQParams = {
        LANDED_CNTRY : countyCd,
      };    
    var custLocByCntryResultCnty = cmr.query('CUSLOC.GET.LOCN_BY_CNTRY', cntyQParams);
    custLocNumValue = custLocByCntryResultCnty.ret1;
  }
  if ((custLocNumValue == '' || custLocNumValue == 'undefined' || custLocNumValue == null) && city != null && postCd != null ){
    var cityQParams = {
        CITY : city,
        POST_CD : postCd,
      };
    var custLocByCityResultCity = cmr.query('CUSLOC.GET.LOCN_BY_CITY_PSCD', cityQParams);
    custLocNumValue = custLocByCityResultCity.ret1;
  }
  if (typeof (custLocNumValue) == 'undefined'||custLocNumValue==null)
      custLocNumValue = "";
  
  FormManager.resetValidations('taxCd1');
  if (custSubGrp == "COMME" || custSubGrp == "FIBAB") {
    if (countryUse == "706MC" || countryUse == "706VU"|| countryUse == "706PF"|| countryUse == "706YT"|| countryUse == "706NC"
      || countryUse == "706AD" || countryUse == "706DZ" || countryUse == "706TN") {
      if(custLocNumValue!=''){
        dummySIRETValue = "SCxxxxxx0" + custLocNumValue;
        FormManager.setValue('taxCd1', dummySIRETValue); 
      }
    }
  } 
  
//  else if (custSubGrp == "PRICU") {
//    if(custLocNumValue!=''){
//        dummySIRETValue = "SCxxxxxx0" + custLocNumValue;
//        FormManager.setValue('taxCd1', dummySIRETValue);
//    }
  
  else if(custSubGrp == 'PRICU' || custSubGrp == 'CBICU' || custSubGrp == 'IBMEM' || custSubGrp == 'CBIEM'){
	  FormManager.setValue('taxCd1', '');
	  FormManager.readOnly('taxCd1');
  }

// else{
// FormManager.setValue('taxCd1', "");
// }
  if (custGrp == "CROSS"){
	  if(custLocNumValue!=''){
	        dummySIRETValue = "SCxxxxxx0" + custLocNumValue;
	        FormManager.setValue('taxCd1', dummySIRETValue);
	    }
  }
// if (custSubGrp != "GOVRN"&&custSubGrp != "PRICU"&&custSubGrp !=
// "IBMEM"&&custSubGrp != "CBVRN"&&custSubGrp != "CBICU"&&custSubGrp != "CBMEM")
// if (countryUse == "706")
// FormManager.addValidator('taxCd1', Validators.REQUIRED, [ 'SIRET' ],
// 'MAIN_CUST_TAB');
  if (role != 'Requester'){
	  return;
	  }
	  else{
	    if (countryUse == "706") {
	      if (custGrp == "LOCAL") {
	        if (custSubGrp == "PRICU" || custSubGrp == "IBMEM") {
	          FormManager.removeValidator('taxCd1', Validators.REQUIRED); 
	        } else {
	          FormManager.addValidator('taxCd1', Validators.REQUIRED, [ 'SIRET' ], 'MAIN_CUST_TAB');
	        }
	      } else if (custGrp == "CROSS") {
	        FormManager.removeValidator('taxCd1', Validators.REQUIRED); 
	      }
	    } else {
	      FormManager.removeValidator('taxCd1', Validators.REQUIRED); 
	    }
	  }
}
function setAbbrevNmLocnOnAddressSave(cntry, addressMode, saving, finalSave, force) {
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
    console.log("Processor, return");
    return;
  }
  if (finalSave || force || addressMode == 'ZS01' || addressMode == 'ZD02') {
    var copyTypes = document.getElementsByName('copyTypes');
    var copyingToA = false;
    var copyToAddrH = false;
    if (copyTypes != null && copyTypes.length > 0) {
      copyTypes.forEach(function(input, i) {
        if (input.value == 'ZS01' && input.checked) {
          copyingToA = true;
        }
        if (input.value == 'ZD02' && input.checked) {
          copyToAddrH = true;
        }
      });
    }
    var addrType = FormManager.getActualValue('addrType');
    if (addrType == 'ZS01' || copyingToA) {
      addAbbrevLocnlogic();
      add32SBODependcyOnPostCdOnAddrSave();
      addVATScenarioOnAddrSave();
      setTaxCdOnAddrSave();
    }
    if (addrType == 'ZS01' || addrType == 'ZS01' || copyingToA || copyToAddrH) {
      addAbbrevNmlogic();
    }
  }
}

function addAbbrevNmlogic() {
  var abbrevNmValue = null;
  var cntryCd = null;
  var singleIndValue = null;
  var departmentNumValue = null;

  var reqType = FormManager.getActualValue('reqType');
  var addrType = FormManager.getActualValue('addrType');
  var custSubGrp = FormManager.getActualValue('custSubGrp');

  var havingZS01 = false;
  var havingZD02 = false;
  if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
    var record = null;
    var type = null;
    for ( var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
      record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
      if (record == null && _allAddressData != null && _allAddressData[i] != null) {
        record = _allAddressData[i];
      }
      type = record.addrType;
      if (typeof (type) == 'object') {
        type = type[0];
      }
      if (type == 'ZS01') {
        havingZS01 = true;
      }
      if (type == 'ZD02') {
        havingZD02 = true;
      }
    }
  }
  console.log("havingZS01 = " + havingZS01);
  console.log("havingZD02 = " + havingZD02);

  var copyTypes = document.getElementsByName('copyTypes');
  var copyToZS01 = false;
  var copyToZD02 = false;
  if (copyTypes != null && copyTypes.length > 0) {
    copyTypes.forEach(function(input, i) {
      if (input.value == 'ZS01' && input.checked) {
        copyToZS01 = true;
      }
      if (input.value == 'ZD02' && input.checked) {
        copyToZD02 = true;
      }
    });
  }
  console.log("copyToZS01 = " + copyToZS01);
  console.log("copyToZD02 = " + copyToZD02);

  if (reqType == "C" && addrType == "ZS01") {
    abbrevNmValue = FormManager.getActualValue('custNm1');
    cntryCd = FormManager.getActualValue('landCntry');

    if (custSubGrp == "INTER" || custSubGrp == "CBTER") {
      if (cntryCd == "DZ") {
        departmentNumValue = "0371";
      } else if (cntryCd == "TN") {
        departmentNumValue = "0382";
      } else if (cntryCd == "YT" || cntryCd == "RE" || cntryCd == "VU") {
        departmentNumValue = "0381";
      } else if (cntryCd == "MQ") {
        departmentNumValue = "0385";
      } else if (cntryCd == "GP") {
        departmentNumValue = "0392";
      } else if (cntryCd == "GF" || cntryCd == "PM") {
        departmentNumValue = "0388";
      } else if (cntryCd == "NC" || cntryCd == "PF") {
        departmentNumValue = "0386";
      } else {
        departmentNumValue = FormManager.getActualValue('ibmDeptCostCenter');
      }
      singleIndValue = departmentNumValue;
    } else if (custSubGrp == "BPIEU" || custSubGrp == "BPUEU" || custSubGrp == "CBIEU" || custSubGrp == "CBUEU") {
      singleIndValue = "R5";
    } else if (custSubGrp == "CBTSO" || custSubGrp == "INTSO") {
      singleIndValue = "FM";
    } else if (custSubGrp == "CBIFF" || custSubGrp == "CBIFL" || custSubGrp == "CBFIN" || custSubGrp == "LCIFF" || custSubGrp == "LCIFL" || custSubGrp == "OTFIN") {
      singleIndValue = "F3";
    } else if (custSubGrp == "LEASE" || custSubGrp == "CBASE") {
      singleIndValue = "L3";
    } else {
      singleIndValue = "D3";
    }
    if (abbrevNmValue != null && abbrevNmValue.length > 19 && (custSubGrp != "INTER" && custSubGrp != "CBTER")) {
      abbrevNmValue = abbrevNmValue.substring(0, 19);
    } else if (abbrevNmValue != null && abbrevNmValue.length < 19 && (custSubGrp != "INTER" && custSubGrp != "CBTER")) {
      for ( var i = abbrevNmValue.length; i < 19; i++) {
        abbrevNmValue += ' ';
      }
    } else if (abbrevNmValue != null && abbrevNmValue.length > 17 && (custSubGrp == "INTER" || custSubGrp == "CBTER")) {
      abbrevNmValue = abbrevNmValue.substring(0, 17);
    } else if (abbrevNmValue != null && abbrevNmValue.length < 17 && (custSubGrp == "INTER" || custSubGrp == "CBTER")) {
      for ( var i = abbrevNmValue.length; i < 17; i++) {
        abbrevNmValue += ' ';
      }
    }

    // story 1507805 - set single identificateur 'DF' if having address H or
    // copy to address H
    if (havingZD02 || copyToZD02) {
      singleIndValue = "DF";
    }
    console.log("singleIndValue = " + singleIndValue);

    if (singleIndValue != null && abbrevNmValue != null) {
      abbrevNmValue = abbrevNmValue + ' ' + singleIndValue;
      FormManager.setValue('abbrevNm', abbrevNmValue);
    }
  } else if (reqType == "C" && addrType == "ZD02") {
    if (havingZS01) {
      var reqId = FormManager.getActualValue('reqId');
      var qParams = {
        REQ_ID : reqId,
        ADDR_TYPE : "ZS01",
      };
      var abbrevNmResult = cmr.query('ADDR.GET.CUSTNM1.BY_REQID_ADDRTYP', qParams);
      abbrevNmValue = abbrevNmResult.ret1;
    } else if (copyToZS01) {
      abbrevNmValue = FormManager.getActualValue('custNm1');
    }

    if (abbrevNmValue != null && abbrevNmValue.length > 19 && (custSubGrp != "INTER" && custSubGrp != "CBTER")) {
      abbrevNmValue = abbrevNmValue.substring(0, 19);
    } else if (abbrevNmValue != null && abbrevNmValue.length < 19 && (custSubGrp != "INTER" && custSubGrp != "CBTER")) {
      for ( var i = abbrevNmValue.length; i < 19; i++) {
        abbrevNmValue += ' ';
      }
    } else if (abbrevNmValue != null && abbrevNmValue.length > 17 && (custSubGrp == "INTER" || custSubGrp == "CBTER")) {
      abbrevNmValue = abbrevNmValue.substring(0, 17);
    } else if (abbrevNmValue != null && abbrevNmValue.length < 17 && (custSubGrp == "INTER" || custSubGrp == "CBTER")) {
      for ( var i = abbrevNmValue.length; i < 17; i++) {
        abbrevNmValue += ' ';
      }
    }
    singleIndValue = "DF";
    if (singleIndValue != null && abbrevNmValue != null) {
      abbrevNmValue = abbrevNmValue + ' ' + singleIndValue;
      FormManager.setValue('abbrevNm', abbrevNmValue);
    }
  }
}

function addAbbrevLocnlogic() {
  var abbrevLocnValue = null;
  var countryUse = null;
  var countyCd = null;
  var cntryDesc = null;

  var reqType = FormManager.getActualValue('reqType');
  var addrType = FormManager.getActualValue('addrType');
  if (reqType == "C" && addrType == "ZS01") {

    countryUse = FormManager.getActualValue('countryUse');
    if (countryUse.length > 3) {
      countyCd = countryUse.substring(3,5);
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
    return
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
  if (role == 'Processor') {
    return;
  }
  if (reqType == 'U') {
    return;
  }

  var countyCd = null;
  var countryUse = FormManager.getActualValue('countryUse');
  if (countryUse.length > 3) {
    countyCd = countryUse.substring(3,5);
  } else {
    countyCd = "FR";
  }
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (!((countyCd == "FR" || countyCd == "KM" || countyCd == "WF") && (custSubGrp == 'COMME' || custSubGrp == 'BPIEU' || custSubGrp == 'BPUEU' || custSubGrp == 'PRICU' || custSubGrp == 'GOVRN' || custSubGrp == 'INTER' || custSubGrp == 'LCIFF' || custSubGrp == 'LCIFL' || custSubGrp == 'OTFIN' || custSubGrp == 'LEASE' || custSubGrp == 'LCOEM' || custSubGrp == 'FIBAB' || custSubGrp == 'HOSTC' || custSubGrp == 'THDPT' || custSubGrp == 'CBTER'))) {
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
      FormManager.setValue('salesBusOffCd', sboValue);
    } else if (postCd == '75' || postCd == '77' || postCd == '78' || postCd == '91' || postCd == '92' || postCd == '93' || postCd == '94' || postCd == '95') {
      var insustry = FormManager.getActualValue('subIndustryCd').substring(0, 1);
      if (insustry == 'A' || insustry == 'B' || insustry == 'C' || insustry == 'D' || insustry == 'F' || insustry == 'H' || insustry == 'K' || insustry == 'N' || insustry == 'R' || insustry == 'S' || insustry == 'T' || insustry == 'W') {
        FormManager.setValue('salesBusOffCd', '01D');
      } else if (insustry == 'E' || insustry == 'G' || insustry == 'J' || insustry == 'L' || insustry == 'M' || insustry == 'P' || insustry == 'U' || insustry == 'V' || insustry == 'X') {
        FormManager.setValue('salesBusOffCd', '01M'); 
      }
    }
  }
  
  // SBO scenario logic should over write the 32 postal code logic
  setSBOOnScenario();
}
function addVATScenarioOnAddrSave() {
  var countyCd = null;
  var countryUse = FormManager.getActualValue('countryUse');
  if (countryUse.length > 3) {
    countyCd = countryUse.substring(3,5);
  } else {
    countyCd = "FR";
  }
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (custSubGrp == '') {
    return
  } else {
    if (custSubGrp == 'PRICU' || custSubGrp == 'CBICU') {
      FormManager.enable('vat');
      FormManager.removeValidator('vat', Validators.REQUIRED);
      
    } else if (custSubGrp == 'COMME' || custSubGrp == 'FIBAB' || custSubGrp == 'BPIEU' || custSubGrp == 'BPUEU' || custSubGrp == 'GOVRN' || custSubGrp == 'INTER' || custSubGrp == 'INTSO' || custSubGrp == 'LCIFF' || custSubGrp == 'LCIFL' || custSubGrp == 'OTFIN' || custSubGrp == 'LEASE' || custSubGrp == 'LCOEM' || custSubGrp == 'HOSTC' || custSubGrp == 'THDPT') {
      if (countyCd == 'FR' || countyCd == 'AD') {
        FormManager.enable('vat');
        FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
      } else {
        FormManager.removeValidator('vat', Validators.REQUIRED);
        FormManager.enable('vat');
      }
      
    } else if (custSubGrp == 'CBMME' || custSubGrp == 'CBBAB' || custSubGrp == 'CBIEU' || custSubGrp == 'CBUEU' || custSubGrp == 'CBVRN' || custSubGrp == 'CBTER' || custSubGrp == 'CBTSO' || custSubGrp == 'CBIFF' || custSubGrp == 'CBIFL' || custSubGrp == 'CBFIN' || custSubGrp == 'CBASE' || custSubGrp == 'CBOEM' || custSubGrp == 'CBSTC' || custSubGrp == 'CBDPT') {
      var isVATEUCntry = false;
      var landCntry = FormManager.getActualValue('landCntry');
      for(var i=0;i<VAT_EU_COUNTRY.length;i++){
        if(VAT_EU_COUNTRY[i] == landCntry){
          isVATEUCntry = true;
          break;
        }
      }
      if(isVATEUCntry == true){
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
  if(cmr.addressMode == 'newAddress' || cmr.addressMode == 'copyAddress'){
  for ( var i = 0; i < _addrTypesForFR.length; i++) {
    _poBOXHandler[i] = null;
    if (_poBOXHandler[i] == null) {
      _poBOXHandler[i] = dojo.connect(FormManager.getField('addrType_' + _addrTypesForFR[i]), 'onClick', function(value) {
        setPOBOX(value);
      });
     }
    }
   }
  if(cmr.addressMode == 'updateAddress'){
       if(FormManager.getActualValue('addrType') == 'ZP01'  ){
     FormManager.enable('poBox');
     }
     else{
     FormManager.disable('poBox');
    }
     }  
}

function setPOBOX(value) {
  if (FormManager.getField('addrType_ZP01').checked  ) {
   FormManager.enable('poBox');
  } else {
   FormManager.disable('poBox');
  }
}


function setHideFieldForFR(){
  var countryUse = FormManager.getActualValue('countryUse');
  if (countryUse.length > 3 &&countryUse.substring(3,5) == "DZ") {
      FormManager.show('DoubleCreate', 'DoubleCreate'); 
  }else{
     FormManager.hide('DoubleCreate','DoubleCreate');
  }
  var reqType = FormManager.getActualValue('custSubGrp');
  if ('INTER'== reqType || 'CBTER'== reqType || 'INTSO'== reqType || 'CBTSO'== reqType) {
    FormManager.addValidator('ibmDeptCostCenter', Validators.REQUIRED, [ 'Internal Department Number' ], 'MAIN_IBM_TAB');
    FormManager.show('InternalDept', 'InternalDept');
  } else {
    FormManager.resetValidations('ibmDeptCostCenter');
    FormManager.hide('InternalDept','InternalDept');
  } 
  if ('GOVRN'== reqType || 'CBVRN'== reqType) {
    FormManager.show('PrivIndc', 'PrivIndc');
  } else {
    FormManager.hide('PrivIndc','PrivIndc');
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

        var cntryRegion = FormManager.getActualValue('countryUse');
        var landCntry = 'FR'; // default to France
        if (cntryRegion != '' && cntryRegion.length > 3) {
          landCntry = cntryRegion.substring(3, 5);
        }

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

function canRemoveAddress(value, rowIndex, grid){ 
  var rowData = grid.getItem(rowIndex);
  var importInd = rowData.importInd[0];
  var reqType = FormManager.getActualValue('reqType');
  if ('U' == reqType && 'Y' == importInd){
    return false;
  }
  return true;
}

function canUpdateAddress(value, rowIndex, grid){ 
  var rowData = grid.getItem(rowIndex);
  var parCmrNo = rowData.parCmrNo[0];
  if (parCmrNo && parCmrNo.trim() != ''){
    return false;
  }
  return true;
}

function ADDRESS_GRID_showCheck(value, rowIndex, grid){
  return canRemoveAddress(value, rowIndex, grid);
}

/* End 1430539 */
function setTaxCdOnScnrio(){
  var role = null;
  var reqType = FormManager.getActualValue('reqType');
    if (typeof (_pagemodel) != 'undefined') {
      role = _pagemodel.userRole;
    }
   if(role == 'Requester' && reqType == 'C'){
        if(FormManager.getActualValue('custGrp') == 'LOCAL'){
        setTaxCdDropDownValuesLOCAL();
        }
        else if(FormManager.getActualValue('custGrp') == 'CROSS'){
        setTaxCdDropDownValuesCROSS();
        }
   }
}

function setTaxCdDropDownValuesLOCAL(){
  var countryUse = null;
  var countyCd = null;
  countryUse = FormManager.getActualValue('countryUse');
    if (countryUse.length > 3) {
      countyCd = countryUse.substring(3,5);
    } else {
      countyCd = "FR";
    }
   if (countyCd == "GF"){
    FormManager.enable('taxCd2');
    FormManager.limitDropdownValues(FormManager.getField('taxCd2'),['11', '69']);
    if (FormManager.getActualValue('taxCd2') == '') {
    FormManager.setValue('taxCd2','69');
      }
   }
   else if (countyCd == "PM" || countyCd == "YT" || countyCd == "AD" || countyCd == "DZ" || countyCd == "TN"){
    FormManager.enable('taxCd2');
    FormManager.limitDropdownValues(FormManager.getField('taxCd2'),['11', '37']);  
    if (FormManager.getActualValue('taxCd2') == '') {
      FormManager.setValue('taxCd2', '37');
    }
   }
   else if (countyCd == "VU" || countyCd == "WF"){
     FormManager.resetDropdownValues(FormManager.getField('taxCd2'));
     FormManager.setValue('taxCd2','36');  
     FormManager.readOnly('taxCd2');
   }
   else if (countyCd == "GP" || countyCd == "MQ" || countyCd == "RE" || countyCd == "KM"){
     FormManager.resetDropdownValues(FormManager.getField('taxCd2'));
     FormManager.setValue('taxCd2','02');  
     FormManager.readOnly('taxCd2');
   }
   else if (countyCd == "FR" || countyCd == "MC"){
     FormManager.resetDropdownValues(FormManager.getField('taxCd2'));
     FormManager.setValue('taxCd2','01');  
     FormManager.readOnly('taxCd2');
   }
   else if (countyCd == "PF"){
     FormManager.resetDropdownValues(FormManager.getField('taxCd2'));
     FormManager.setValue('taxCd2','34');  
     FormManager.readOnly('taxCd2');
   }
   else if (countyCd == "NC"){
     FormManager.resetDropdownValues(FormManager.getField('taxCd2'));
     FormManager.setValue('taxCd2','41');  
     FormManager.readOnly('taxCd2');
   }
}

function setTaxCdDropDownValuesCROSS(){
  var isEUCntry = false;
  var reqId = FormManager.getActualValue('reqId');
  var qParams = {
      REQ_ID : reqId,
      ADDR_TYPE : 'ZS01',
  };
  var _result = cmr.query('ADDR.GET.LANDCNTRY.BY_REQID_ADDRTYP', qParams);
  var landCntry = _result.ret2;
  if(landCntry != null){
    for(var i=0;i<EU_COUNTRIES.length;i++){
      if(EU_COUNTRIES[i] == landCntry){
        isEUCntry = true;
        break;
      }
    }
    if(isEUCntry == true){
      FormManager.enable('taxCd2');
      FormManager.limitDropdownValues(FormManager.getField('taxCd2'),['12', '15']);
        if (FormManager.getActualValue('taxCd2') == '') {
        FormManager.setValue('taxCd2', '15');
          }     
    } else{
      FormManager.enable('taxCd2');
      FormManager.limitDropdownValues(FormManager.getField('taxCd2'),['11', '37']);
        if (FormManager.getActualValue('taxCd2') == '') {
        FormManager.setValue('taxCd2', '37');
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
   if(role == 'Requester' && reqType == 'C'){
        if(FormManager.getActualValue('custGrp') == 'LOCAL'){
        setTaxCdDropDownValuesLOCAL();
        }
        else if(FormManager.getActualValue('custGrp') == 'CROSS'){
          setTaxCdOnAddrSaveCROSS();
        }
   }
}

function setTaxCdOnAddrSaveCROSS(){
  var isEUCntry = false;
  var landCntry = FormManager.getActualValue('landCntry');
  var addrType = FormManager.getActualValue('addrType');
  if(landCntry != null && addrType == 'ZS01'){
    for(var i=0;i<EU_COUNTRIES.length;i++){
      if(EU_COUNTRIES[i] == landCntry){
        isEUCntry = true;
        break;
      }
    }
    if(isEUCntry == true){
      FormManager.enable('taxCd2');
      FormManager.limitDropdownValues(FormManager.getField('taxCd2'),['12', '15']);
        if (FormManager.getActualValue('taxCd2') == '') {
        FormManager.setValue('taxCd2', '15');
          }     
    } else{
      FormManager.enable('taxCd2');
      FormManager.limitDropdownValues(FormManager.getField('taxCd2'),['11', '37']);
        if (FormManager.getActualValue('taxCd2') == '') {
        FormManager.setValue('taxCd2', '37');
       }      
     }      
  }  
}

function addAbbrevLocnValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqType = FormManager.getActualValue('reqType');
        var role = _pagemodel.userRole;
        var _abbrevLocn = FormManager.getActualValue('abbrevLocn');
        var reg = /[^\u0000-\u007f]/;
        if (reqType != 'U' && role == 'Requester' && reg.test(_abbrevLocn)) {
          return new ValidationResult({
            id : 'abbrevLocn',
            type : 'text',
            name : 'abbrevLocn'
          }, false, 'The value for Abbreviated location is invalid. Only Latin characters are allowed.');
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

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
function addHostingInstallCustNm1Validator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        var addrType = FormManager.getActualValue('addrType');
        var custNm1 = FormManager.getActualValue('custNm1');
        var custNm2 = FormManager.getActualValue('custNm2');
        var custName = null;
        if (custNm2 != '' && custNm2.length >= 1) {
          custName = custNm1.trim() + ' ' + custNm2.trim();
        } else {
          custName = custNm1.trim();
        }
        if ((custSubGrp == 'HOSTC' || custSubGrp == 'CBSTC')&& addrType == 'ZS01' ) {
          var validateResult = true;
          if (custName != '' && custName.length > 1) {
            if (patchCount(" chez ", custName) != 1) {
              validateResult = false; // 1)
              if (patchCount(" chez ", custName) == 0 && custName.toLowerCase().indexOf("chez ") == 0) {
                validateResult = true; // 4)
              }
            } else {
              if (custName.toLowerCase().indexOf("chez ") == 0) {
                validateResult = false; // 5)
              }
            }
            if (endWith(custName.toLowerCase()," chez")) {
              validateResult = false; // 3)
            }
            if (patchCount(" chez chez ", custName) > 0 || custName.toLowerCase().indexOf("chez chez ") == 0) {
              validateResult = false; // 6)
            }
          }
          if (validateResult == false) {
            return new ValidationResult({
              id : 'custNm1',
              type : 'text',
              name : 'custNm1'
            }, false, 'Correct format of the Customer Name should be: Name of end-user "chez" Name of host + Address of host.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}
function endWith(string, endString) {
  var result = false;
  if (string != null && endString != null) {
    var d = string.length - endString.length;
    if (d >= 0 && string.lastIndexOf(endString) == d) {
      result = true;
    };
  }
  return result;
}
function patchCount(re, str) {
  re=eval("/"+re+"/ig");
  if (str.match(re)) {
    return str.match(re).length;
  } else {
    return 0;
  }
  ;
}
function addSIRETValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        var _siret = FormManager.getActualValue('taxCd1');
        if("GOVRN"==custSubGrp){
          if (_siret && _siret.length > 0 && (_siret.substring(0, 1)=="1"||_siret.substring(0, 1)=="2")) {
            
          } else {
            return new ValidationResult(null, false, 'The value for SIRET should begins with 1 or 2.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR'); 
}

function addLengthValidatorForSIRET() {
	  console.log("register SIRET length validator . . .");
	  FormManager.addFormValidator((function() {
	    return {
	      validate : function() {
	    	  var _siret = FormManager.getActualValue('taxCd1');
		        if(_siret && _siret.length > 0 && _siret.length != 14){
	          return new ValidationResult(FormManager.getField('taxCd1'), false, 'The correct Siret format-length is exactly 14 characters.');
	        }
	        return new ValidationResult(null, true, null);
	      } 
	    }; 
	  })(), 'MAIN_CUST_TAB', 'frmCMR');
	}
function addHostingInstallCustNmValidatorOnCheckReq() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqId = FormManager.getActualValue('reqId');
        var qParams = {
            REQ_ID : reqId,
            ADDR_TYPE : 'ZS01',
        };
        var _result = cmr.query('ADDR.GET.CUSTNM1.BY_REQID_ADDRTYP', qParams);
        var custNm1 = _result.ret2;
        
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        
        if (custNm1 != null && (custSubGrp == 'HOSTC' || custSubGrp == 'CBSTC')) {
          if (custNm1.toLowerCase().indexOf("chez") == -1) {
            return new ValidationResult(null, false,
                'When scenario is Hosting case, address type is Installing, correct format of the Customer Name should be: Name of end-user "chez" Name of host + Address of host.');
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

function disableLandCntry() {
    var custType = FormManager.getActualValue('custGrp');
    if (custType == 'LOCAL' && FormManager.getActualValue('addrType') == 'ZS01') {
      FormManager.readOnly('landCntry');
    } else {
      FormManager.enable('landCntry');
    }
  }

function addFRLandedCountryHandler(cntry, addressMode, saving, finalSave) {
    if (!saving) {
      if (addressMode == 'newAddress') {
        FilteringDropdown['val_landCntry'] = FormManager.getActualValue('defaultLandedCountry');
        FormManager.setValue('landCntry', FormManager.getActualValue('defaultLandedCountry'));
      } else {
        FilteringDropdown['val_landCntry'] = null;
      }
    }
  }

function validateFRCopy(addrType, arrayOfTargetTypes){
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
        if(reqType == 'U' && role != 'Processor' && IBOActual == '453' && iboOld != '453'){
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
          countyCd = countryUse.substring(3,5);
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
      
      // Unlock ISU for Update and Processor
      if (FormManager.getActualValue('reqType') == 'U') {
        FormManager.enable('isuCd');
        return;
      }
      var role = null;
      if (typeof (_pagemodel) != 'undefined') {
        role = _pagemodel.userRole;
      }
      if (role == 'Processor') {
        FormManager.enable('isuCd');
        return;
      }
      if (FormManager.getActualValue('viewOnlyPage') == 'true') {
        return;
      }
      
      if (countryUse.length > 3) {
        countyCd = countryUse.substring(3,5);
      } else {
        countyCd = "FR";
      }
      
      value = FormManager.getActualValue('clientTier');
      FormManager.enable('isuCd');
      if (value == 'B' || value == 'M' || value == 'W' || value == 'T' || value == 'S' || value == 'C' || value == 'N') {
        FormManager.setValue('isuCd', '32');
        FormManager.readOnly('isuCd');
      } else if (value == 'V' || value == '4' || value == 'A' || value == '6' || value == 'E') {
        FormManager.setValue('isuCd', '34');
        FormManager.readOnly('isuCd');
      } else if (value == 'Z' || value == '7') {
        if(countyCd == "TN" && (custSubGrp == 'BPIEU' || custSubGrp == 'BPUEU' || custSubGrp == 'CBIEU' || custSubGrp == 'CBUEU')) {
          FormManager.setValue('isuCd', '8B');
        } else if(countyCd == "DZ" && (custSubGrp == 'BPIEU' || custSubGrp == 'BPUEU' || custSubGrp == 'CBIEU' || custSubGrp == 'CBUEU')) {
          FormManager.setValue('isuCd', '8B');
        } else {
          FormManager.setValue('isuCd', '21');
        }
        FormManager.readOnly('isuCd');
      } else {
        if (PageManager.isReadOnly()) {
          FormManager.readOnly('isuCd');
        } else {
          FormManager.enable('isuCd');
        }
      }
     
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
          if(_sr && _sr.length > 0 && _sr.length != 6){
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
          if(_sr && _sr.length > 0 && !_sr.match("^[0-9a-zA-Z]*$")){
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
  });
  if (_reqReasonHandler && _reqReasonHandler[0]) {
    _reqReasonHandler[0].onChange();
  }  
}
function affacturageLogic() {
  var reqType = FormManager.getActualValue('reqType');
  var reqReason = FormManager.getActualValue('reqReason');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (reqType == 'C') {
    if (custSubGrp == 'FIBAB' || custSubGrp == 'CBBAB') {
      FormManager.show('dupCmrIndc', 'dupCmrIndc');
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
  if (role != 'Requester') {
    return;
  }
  if (reqType == 'U') {
    return;
  }
  
  if (countryUse.length > 3) {
    countyCd = countryUse.substring(3,5);
  } else {
    countyCd = "FR";
  }
  if(countyCd == "TN" && (custSubGrp == 'BPIEU' || custSubGrp == 'BPUEU' || custSubGrp == 'CBIEU' || custSubGrp == 'CBUEU')) {
    FormManager.setValue('isuCd', '8B');
    FormManager.setValue('clientTier', '7');
  } else if(countyCd == "DZ" && (custSubGrp == 'BPIEU' || custSubGrp == 'BPUEU' || custSubGrp == 'CBIEU' || custSubGrp == 'CBUEU')) {
    FormManager.setValue('isuCd', '8B');
    FormManager.setValue('clientTier', '7');
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
        FormManager.addValidator('salesBusOffCd', Validators.REQUIRED, [ 'Search Term/Sales Branch Office' ], 'MAIN_IBM_TAB');
        FormManager.addValidator('installBranchOff', Validators.REQUIRED, [ 'Installing BO' ], 'MAIN_IBM_TAB');
      } else {
        FormManager.removeValidator('salesBusOffCd', Validators.REQUIRED);
        FormManager.removeValidator('installBranchOff', Validators.REQUIRED);
      }
    }
  }
}

// story 1640384 - Error message for processor in case 'IBM' is part of
// Abbreviated name for some scenarios
function addIBMAbbrevNmValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var role = _pagemodel.userRole;
  	    var abbrevNm = FormManager.getActualValue('abbrevNm');
  	    var custSubGrp = FormManager.getActualValue('custSubGrp');
  	    if(role == 'Processor' && (abbrevNm.toUpperCase().indexOf("IBM") != -1) && !(custSubGrp == 'INTER' || custSubGrp == 'INTSO' || custSubGrp == 'CBTER' || custSubGrp == 'CBTSO')){
  	    	var ibmIndex = abbrevNm.toUpperCase().indexOf("IBM");
  	    	var ibm = abbrevNm.substring(ibmIndex,ibmIndex+3);
  	      return new ValidationResult({
  	    	  id : 'abbrevNm',
  	        type : 'text',
  	        name : 'Abbreviated Name (TELX1)'
  	      }, false, 'Abbreviated Name (TELX1) should not contain "' + ibm + '" when scenario sub-type is other than Internal or Internal SO/FM');
  	    } else {
  	      return new ValidationResult(null, true);
  	    }
      }
    };
	})(), 'MAIN_CUST_TAB', 'frmCMR'); 
}

dojo.addOnLoad(function() {
  GEOHandler.FR = [ SysLoc.FRANCE ];
  console.log('adding FR functions...');

  GEOHandler.setRevertIsicBehavior(false);
  GEOHandler.addAfterConfig(afterConfigForFR, '706');
  GEOHandler.addAfterConfig(addFRClientTierLogic, '706');
  GEOHandler.addAfterConfig(add32PostCdCntrySBOlogicOnISUChange, '706');
  GEOHandler.addAfterConfig(addIBOlogic, '706');
  GEOHandler.registerValidator(addFRAddressTypeValidator, '706', null, true);
  GEOHandler.addAddrFunction(addLatinCharValidatorFR, '706');
  GEOHandler.addAddrFunction(addPhoneValidatorFR, '706');
  GEOHandler.addAddrFunction(setAbbrevNmLocnOnAddressSave, '706');
  GEOHandler.addAddrFunction(hidePOBox, '706');
  GEOHandler.registerValidator(addCrossBorderValidatorForFR, '706', null, true);
  GEOHandler.addAddrFunction(updateMainCustomerNames, '706');
  GEOHandler.enableCustomerNamesOnAddress('706');
  GEOHandler.addAfterConfig(addALPHANUMSPACEValidatorFR, '706');
  GEOHandler.addAfterTemplateLoad(addALPHANUMSPACEValidatorFR, '706');
  GEOHandler.registerValidator(addAbbrevLocnValidator, [ '706' ], null, true);
  GEOHandler.registerValidator(addHostingInstallCustNm1Validator, '706' , null, true);
// GEOHandler.registerValidator(addSIRETValidator, ['706'] , null, true);
  GEOHandler.registerValidator(addLengthValidatorForSIRET,['706'], null, true);
  GEOHandler.registerValidator(addLengthValidatorForSR,['706'], null, true);
  GEOHandler.registerValidator(addALPHANUMValidatorForSR,['706'], null, true);
  GEOHandler.addToggleAddrTypeFunction(toggleAddrTypesForFR, '706');
  GEOHandler.addAddrFunction(addFRLandedCountryHandler, '706');
  GEOHandler.enableCopyAddress('706', validateFRCopy, [ 'ZD01']);
  GEOHandler.registerValidator(addGenericVATValidator('706', 'MAIN_CUST_TAB', 'frmCMR','ZP01'), ['706'], null, true);
  GEOHandler.addAddrFunction(disableLandCntry, ['706']);
  GEOHandler.registerValidator(addIBOUpdate453Validator, '706' , null, true);
// GEOHandler.registerValidator(addPostalCodeLengthValidator, '706' , null,
// true);
  GEOHandler.addAfterConfig(showAffacturageOnReqReason, '706');
// GEOHandler.registerValidator(addHostingInstallCustNmValidatorOnCheckReq,
// '706', null, true);
  GEOHandler.registerValidator(addIBMAbbrevNmValidator, '706' , null, true);
  
  /* 1438717 - add DPL match validation for failed dpl checks */
  GEOHandler.registerValidator(addFailedDPLValidator, [ '706' ], GEOHandler.ROLE_PROCESSOR, true);

});