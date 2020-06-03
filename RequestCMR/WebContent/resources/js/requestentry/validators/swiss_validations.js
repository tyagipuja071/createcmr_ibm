/* Register SWISS Javascripts */
function addAfterConfigForSWISS() {

  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var impIndc = getImportedIndcForSwiss();
  if (role == 'REQUESTER') {
    FormManager.removeValidator('custLangCd', Validators.REQUIRED);
  } else {
    FormManager.addValidator('custLangCd', Validators.REQUIRED, [ 'Prefered Langauge' ], null);
  }

  if (role == 'REQUESTER') {
    FormManager.readOnly('subIndustryCd');
  } else {
    FormManager.enable('subIndustryCd');
  }

  // lock EmbargoCd for creates
  if (!PageManager.isReadOnly() && FormManager.getActualValue('reqType') == 'C') {
    if (role == 'REQUESTER') {
      FormManager.readOnly('ordBlk');
    } else {
      FormManager.enable('ordBlk');
    }
  }

  if (reqType == 'C' && role == 'PROCESSOR' && cntry == '848') {
    FormManager.enable('cmrNo');
  } else {
    FormManager.readOnly('cmrNo');
  }

  if (reqType == 'U') {
    FormManager.enable('clientTier');
    FormManager.readOnly('custLangCd');

    // FormManager.enable('currencyCd');
  } else {
    // FormManager.readOnly('currencyCd');
  }
  if (role == 'PROCESSOR') {
    // FormManager.enable('currencyCd');
  }

  if (reqType == 'C'
      && role == 'REQUESTER'
      && (custSubGrp == 'CHCOM' || custSubGrp == 'LICOM' || custSubGrp == 'CHGOV' || custSubGrp == 'LIGOV' || custSubGrp == 'CHSOF' || custSubGrp == 'LISOF' || custSubGrp == 'CH3PA'
          || custSubGrp == 'LI3PA' || custSubGrp == 'XCHCM' || custSubGrp == 'XCHGV' || custSubGrp == 'XCHSF' || custSubGrp == 'XCH3P')) {
    FormManager.enable('clientTier');
  } else if (reqType == 'C'
      && role == 'REQUESTER'
      && (custSubGrp == 'CHINT' || custSubGrp == 'XCHIN' || custSubGrp == 'LIINT' || custSubGrp == 'CHPRI' || custSubGrp == 'XCHPR' || custSubGrp == 'LIPRI' || custSubGrp == 'CHIBM'
          || custSubGrp == 'XCHIB' || custSubGrp == 'LIIBM' || custSubGrp == 'CHBUS' || custSubGrp == 'XCHBP' || custSubGrp == 'LIBUS')) {
    FormManager.readOnly('clientTier');
  }
  if (reqType == 'C' && (role == 'PROCESSOR')) {
    FormManager.enable('clientTier');
  }

  if (reqType == 'C') {
    FormManager.readOnly('custNm3');
    FormManager.readOnly('custLangCd');
  }

  if (reqType == 'C' && (custSubGrp == 'CHBUS' || custSubGrp == 'LIBUS')) {
    FormManager.setValue("inacCd", "");
    FormManager.readOnly("inacCd");
    FormManager.setValue("custClass", "45");
    FormManager.readOnly("custClass");
    FormManager.resetValidations('ppsceid');
    FormManager.enable('ppsceid');
    FormManager.addValidator('ppsceid', Validators.REQUIRED, [ 'PPS CEID' ], 'MAIN_IBM_TAB');
  } else if (reqType == 'C' && !(custSubGrp == 'CHBUS' || custSubGrp == 'LIBUS')) {
    FormManager.resetValidations('ppsceid');
    FormManager.clearValue('ppsceid');
    FormManager.readOnly('ppsceid');
  }

  if (reqType == 'C' && (custSubGrp == 'CHPRI' || custSubGrp == 'LIPRI')) {
    FormManager.setValue("inacCd", "");
    FormManager.readOnly("inacCd");
    FormManager.setValue("custClass", "60");
    FormManager.readOnly("custClass");
  }

  if (reqType == 'C' && (custSubGrp == 'CHIBM' || custSubGrp == 'LIIBM')) {
    FormManager.setValue("inacCd", "");
    FormManager.readOnly("inacCd");
    FormManager.setValue("custClass", "71");
    FormManager.readOnly("custClass");
  }

  // Lock vat,inac,kukla for internal
  if (reqType == 'C' && (custSubGrp == 'CHINT' || custSubGrp == 'LIINT')) {
    FormManager.setValue("custClass", "81");
    FormManager.readOnly("custClass");
    FormManager.setValue("inacCd", "");
    FormManager.readOnly("inacCd");

  }
  // Lock for customer class code for local govt.
  if (reqType == 'C' && (custSubGrp == 'CHGOV' || custSubGrp == 'LIGOV')) {
    FormManager.setValue('custClass', '12');
    FormManager.readOnly('custClass');
  }

  if (FormManager.getActualValue('custGrp') == 'CROSS') {
    FormManager.setValue('custLangCd', 'E');
  }

  // abbrev name locked optional for requester
  if (role == 'REQUESTER') {
    FormManager.readOnly('abbrevNm')
    FormManager.removeValidator('abbrevNm', Validators.REQUIRED);
  } else {
    FormManager.addValidator('abbrevNm', Validators.REQUIRED, [ 'Abbreviated Name' ], 'MAIN_CUST_TAB');
  }

  if (reqType == 'C') {
    FormManager.getField('capInd').set('checked', true);
    FormManager.readOnly('capInd');
  }

  // disable copy address
  GEOHandler.disableCopyAddress();

  setTaxCdFrCustClass();
  setClientTierValues();
  setVatValidatorSWISS();
  setFieldsMandtStatus();
  // setVatValueOnPrefLang();
  setMubotyOnPostalCodeIMS();
  showDeptNoForInternalsOnlySWISS();
  setMubotyOnPostalCodeIMS32N();
  if (impIndc != 'N') {
    // setPreferredLangAddr();
    addVatSuffixForCustLangCd();
  }
  if (reqType == 'C') {
    var vatLockedCustSubGrpList = [ 'CHIBM', 'LIIBM', 'CHPRI', 'LIPRI', 'CHINT', 'LIINT' ];
    if (vatLockedCustSubGrpList.includes(custSubGrp)) {
      FormManager.readOnly('vat');
      FormManager.setValue('vat', '');
    } else {
      FormManager.enable('vat');
      if (dijit.byId('vatExempt').get('checked')) {
        dijit.byId('vatExempt').set('checked', false);
        setVatValidatorSWISS();
      }
    }
  }
}

function resetAddrTypeValidation() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var addrType = FormManager.getActualValue('addrType');
        if (cmr.addressMode == 'copyAddress' && FormManager.GETFIELD_VALIDATIONS['addrType'] == null && (addrType == null || addrType == '')) {
          return new ValidationResult(null, false, 'Address Type is required.');
        } else {
          return new ValidationResult(null, true);
        }

      }
    };
  })(), null, 'frmCMR_addressModal');
}

function name3LengthValidation() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var dept = FormManager.getActualValue('dept');
        var buidling = FormManager.getActualValue('bldg');
        var floor = FormManager.getActualValue('floor');
        var addrDetailsArr = [];
        var details = "";
        if (dept != null && dept != '')
          addrDetailsArr.push(dept);
        if (buidling != null && buidling != '')
          addrDetailsArr.push(buidling);
        if (floor != null && floor != '')
          addrDetailsArr.push(floor);

        if (addrDetailsArr.length > 0)
          details = addrDetailsArr.join(", ");

        var length = details.length;
        if (cmr.addressMode == 'newAddress' || cmr.addressMode == 'updateAddress' || cmr.addressMode == 'copyAddress') {
          if (length > 30)
            return new ValidationResult(null, false, 'Computed length of Customer Name 3(Department,Building & Floor) cannot exceed 30 chars.');
        } else {
          return new ValidationResult(null, true);
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

var _custCdHandler = null;
function setTaxCdFrCustClass() {
  if (_custCdHandler == null) {
    _custCdHandler = dojo.connect(FormManager.getField('custClass'), 'onChange', function(value) {
      if (value != null) {
        switch (value) {
        case '24':
          FormManager.setValue('taxCd1', '0');
          break;
        default:
          FormManager.setValue('taxCd1', '1');
        }
      }
    });
  }
}

/*
 * After config handlers
 */
var _ISUHandler = null;
var _IMSHandler = null;
var _PostalCodeHandler = null;
var _TaxCdHandler = null;
var _custCodeHanlder = null;
var _CTCHandler = null;
var _reqReasonHandler = null;
var _vatExemptHandler = null;
var addrTypeHandler = [];
var _hwMstrInstallFlagHandler = null;
var _vatHandler = null;
var _addrTypesForSWISS = [ 'ZD01', 'ZP01', 'ZI01', 'ZS01', 'ZS02' ];

function displayHwMstrInstallFlag() {
  if (cmr.addressMode == 'newAddress' || cmr.addressMode == 'copyAddress') {
    for (var i = 0; i < _addrTypesForSWISS.length; i++) {
      if (addrTypeHandler[i] == null) {
        addrTypeHandler[i] = dojo.connect(FormManager.getField('addrType_' + _addrTypesForSWISS[i]), 'onClick', function(value) {
          if (FormManager.getField('addrType_ZI01').checked || FormManager.getField('addrType_ZS01').checked || FormManager.getField('addrType_ZS02').checked) {
            cmr.showNode('hwFlag');
          } else {
            FormManager.getField('hwInstlMstrFlg').set('checked', false);
            cmr.hideNode('hwFlag');
          }
        });
      }

    }
  } else if (cmr.addressMode == 'updateAddress'
      && (FormManager.getActualValue('addrType') == 'ZI01' || FormManager.getActualValue('addrType') == 'ZS01' || FormManager.getActualValue('addrType') == 'ZS02')) {
    cmr.showNode('hwFlag');
  } else {
    cmr.hideNode('hwFlag');
  }
}

function checkHwMstrInstallFlag() {
  var addrType = FormManager.getActualValue('addrType');
  var siteId = '';
  var addrSeq = '';
  var reqId = FormManager.getActualValue('reqId');

  if (_hwMstrInstallFlagHandler == null && (addrType != null || addrType != '')) {
    _hwMstrInstallFlagHandler = dojo.connect(FormManager.getField('hwInstlMstrFlg'), 'onClick', function(value) {
      if (FormManager.getField('hwInstlMstrFlg').checked == true) {
        var qParams = {
          REQ_ID : reqId,
        };
        var result = cmr.query('ADDR.GET.HW_INSTL_MSTR_FLG', qParams);
        var result2 = cmr.query('ADDR.GET.HW_INSTL_MSTR_FLG_DETAILS', qParams);
        if (result.ret1 != null && result2 != null) {
          if (result.ret1 >= 1) {
            addrSeq = result2.ret1;
            siteId = result2.ret2;
            if (siteId != null && siteId != '') {
              cmr.showAlert("Address with Site ID: " + siteId + " is already a hardware master.");
              FormManager.getField('hwInstlMstrFlg').set('checked', false);
              return;
            }
            if (addrSeq != null && addrSeq != '') {
              cmr.showAlert("Address with Sequence: " + addrSeq + " is already a hardware master.");
              FormManager.getField('hwInstlMstrFlg').set('checked', false);
              return;
            }

          }
        }
      }
    });
  }
}

function addHwMstrInstFlgValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          var recordList = null;
          var isInstMstrFlgValid = true;
          var adddrInstMstrFlg = "";
          var addrType = "";
          var adddrInstMstrFlgCount = 0;

          for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
            recordList = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
            if (recordList == null && _allAddressData != null && _allAddressData[i] != null) {
              recordList = _allAddressData[i];
            }
            adddrInstMstrFlg = recordList.hwInstlMstrFlg;
            addrType = recordList.addrType;

            if (typeof (adddrInstMstrFlg) == 'object') {
              adddrInstMstrFlg = adddrInstMstrFlg[0];
            }

            if (typeof (addrType) == 'object') {
              addrType = addrType[0];
            }

            if ((addrType == 'ZI01' || addrType == 'ZS01' || addrType == 'ZS02') && adddrInstMstrFlg == 'Y') {
              isInstMstrFlgValid = true;
              adddrInstMstrFlgCount++;
            } else if (addrType != 'ZI01' && addrType != 'ZS01' && addrType != 'ZS02' && adddrInstMstrFlg == 'Y') {
              isInstMstrFlgValid = false;
            }
          }

          if (isInstMstrFlgValid == false) {
            return new ValidationResult(null, false, 'Hardware Install Master address can only be selected for Install-At/Sold-To Address.');
          } else if (isInstMstrFlgValid == true && adddrInstMstrFlgCount > 1) {
            return new ValidationResult(null, false, 'Hardware Install Master address can  be selected only for one Install-At/Sold-To Address. Please remove additional ones.');
          }

          return new ValidationResult(null, true);
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function addHandlersForSWISS() {

  if (_ISUHandler == null) {
    _ISUHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
      setClientTierValues(value);
    });
  }

  /*
   * if (_CustLangCdHandler == null) { _CustLangCdHandler =
   * dojo.connect(FormManager.getField('custLangCd'), 'onChange',
   * function(value) { setVatValueOnPrefLang(value); }); }
   */

  if (_CTCHandler == null) {
    _CTCHandler = dojo.connect(FormManager.getField('clientTier'), 'onChange', function(value) {
      setMubotyOnPostalCodeIMS(value);
      setMubotyOnPostalCodeIMS32N(value);
    });
  }

  if (_PostalCodeHandler == null) {
    _PostalCodeHandler = dojo.connect(FormManager.getField('postCd'), 'onChange', function(value) {
      setMubotyOnPostalCodeIMS(value);
      setMubotyOnPostalCodeIMS32N(value);
      setPreferredLangAddr(value);
    });
  }

  if (_IMSHandler == null) {
    _IMSHandler = dojo.connect(FormManager.getField('subIndustryCd'), 'onChange', function(value) {
      setISUCTCOnIMSChange();
      setMubotyOnPostalCodeIMS();
      setMubotyOnPostalCodeIMS32N(value);
    });
  }

  if (_reqReasonHandler == null) {
    _reqReasonHandler = dojo.connect(FormManager.getField('reqReason'), 'onChange', function(value) {
      checkEmbargoCd(value);
    });
  }

  if (_vatExemptHandler == null) {
    _vatExemptHandler = dojo.connect(FormManager.getField('vatExempt'), 'onClick', function(value) {
      setVatValidatorSWISS();
    });
  }

  if (_vatHandler == null) {
    _vatHandler = dojo.connect(FormManager.getField('vat'), 'onChange', function(value) {
      if (value.length >= 15) {
        addVatSuffixForCustLangCdScrtch();
      }
    });
  }

}

var _importedIndc = null;
function getImportedIndcForSwiss() {
  if (_importedIndc) {
    console.log('Returning imported indc = ' + _importedIndc);
    return _importedIndc;
  }
  var results = cmr.query('IMPORTED_ADDR_SWISS', {
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
function setISUCTCOnIMSChange() {
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var isuCd = FormManager.getActualValue('isuCd');
  var clientTier = FormManager.getActualValue('clientTier');
  var subIndustryCd = FormManager.getActualValue('subIndustryCd');
  if (!(custSubGrp == 'CHINT' || custSubGrp == 'LIINT' || custSubGrp == 'CHPRI' || custSubGrp == 'LIPRI' || custSubGrp == 'CHIBM' || custSubGrp == 'LIIBM' || custSubGrp == 'CHBUS' || custSubGrp == 'LIBUS')) {
    if ('32' == isuCd && 'S' == clientTier && subIndustryCd.startsWith('B')) {
      FormManager.setValue('clientTier', 'N');
    } else if ('32' == isuCd && 'N' == clientTier && !subIndustryCd.startsWith('B')) {
      FormManager.setValue('clientTier', 'S');
    }
  }
}

function addVatSuffixForCustLangCdScrtch() {
  var reqType = FormManager.getActualValue('reqType');
  var reqId = FormManager.getActualValue('reqId');
  var result = cmr.query('ADDR.GET.LAND_CNTRY.BY_REQID', {
    REQ_ID : reqId,
    ADDR_TYPE : 'ZS01'
  });
  var landCntry = result.ret1;
  if (landCntry != 'CH') {
    return;
  }
  // get custlangCd
  var reqId = FormManager.getActualValue('reqId');
  var qParams = {
    REQ_ID : reqId
  };
  var result = cmr.query('GET.CUSTLANGCD.ZS01', qParams);
  if (result != null) {
    var custLang = result.ret1;
    // set vat suffix
    var vat = FormManager.getActualValue('vat');
    if (vat != '' && vat != null && vat != undefined && vat.length >= 15) {
      var vatOnly = vat.substring(0, 15);
      if ((custLang == 'E' || custLang == 'D') && vat.substring(16, 20) != 'Mwst') {
        FormManager.setValue('vat', vatOnly.concat(" Mwst"));
      } else if ((custLang == 'I') && vat.substring(16, 19) != 'IVA') {
        FormManager.setValue('vat', vatOnly.concat(" IVA"));
      } else if (custLang == 'F' && vat.substring(16, 19) != 'TVA') {
        FormManager.setValue('vat', vatOnly.concat(" TVA"));
      }
    }
  }
}

/* Vat Exempt Handler */
function setVatValidatorSWISS() {
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  if (viewOnlyPage != 'true' && FormManager.getActualValue('reqType') == 'C') {
    FormManager.resetValidations('vat');
    if (!dijit.byId('vatExempt').get('checked')) {
      FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
    }
  }
}

/*
 * function setVatValueOnPrefLang(custLangCd) { if
 * (FormManager.getActualValue('reqType') != 'C' ||
 * FormManager.getActualValue('addrType') != 'ZS01') { return; } var cntry =
 * FormManager.getActualValue('cmrIssuingCntry'); var zs01ReqId =
 * FormManager.getActualValue('reqId'); var qParams = { REQ_ID : zs01ReqId, };
 * var custLangCd = FormManager.getActualValue('custLangCd'); var result =
 * cmr.query('ADDR.GET.CUST_LANG_CD.BY_REQID', qParams); if (custLangCd == '')
 * custLangCd = result.ret1;
 * 
 * var vatSuffix = []; if (custLangCd != '') { var qParams = { _qall : 'Y',
 * ISSUING_CNTRY : cntry, REP_TEAM_CD : '%' + custLangCd + '%' };
 * 
 * var results = cmr.query('GET.VATSUFFIX.BYCUSTLANGCD', qParams); if (results !=
 * null) { for ( var i = 0; i < results.length; i++) {
 * vatSuffix.push(results[i].ret1); } if (vatSuffix != null) {
 * FormManager.limitDropdownValues(FormManager.getField('vat'), vatSuffix); if
 * (vatSuffix.length == 1) { FormManager.setValue('vat', vatSuffix[0]); } if
 * (vatSuffix.length == 0) { FormManager.setValue('vat', ''); } } } } }
 */

function checkEmbargoCd(value) {
  if (value != 'TREC')
    return;
  var reqId = FormManager.getActualValue('reqId');
  var emabargoCd = FormManager.getActualValue('ordBlk');
  var qParams = {
    REQ_ID : reqId
  };

  var result = cmr.query('GET.DATA_RDC.EMBARGO_BY_REQID_SWISS', qParams);
  if ((result.ret1 == '88' || result.ret1 == '94') && (emabargoCd == '' || emabargoCd == null || emabargoCd == undefined)) {

  } else {
    FormManager.clearValue('reqReason');
    cmr.showAlert('This Request reason can be chosen only if imported record has 88 or 94 embargo code and Embargo code field has blank value.');
    return;
  }
}

/*
 * Swiss - sets Client_Tier based on ISU
 */
function setClientTierValues(isuCd) {
  var reqType = FormManager.getActualValue('reqType');
  var isuCd = FormManager.getActualValue('isuCd');
  if (reqType != 'C') {
    return;
  }
  var tierValues = null;
  if (isuCd == '32') {
    tierValues = [ 'S', 'N' ];
  } else if (isuCd == '34') {
    tierValues = [ 'A', 'V', '6' ];
  } else if (isuCd != '') {
    tierValues = [ '7' ];
  }

  if (tierValues != null) {
    FormManager.limitDropdownValues(FormManager.getField('clientTier'), tierValues);
  } else {
    FormManager.resetDropdownValues(FormManager.getField('clientTier'));
  }
  if (tierValues != null && tierValues.length == 1) {
    FormManager.setValue('clientTier', tierValues[0]);
  }
}

/*
 * Swiss - sets Muboty based on Postal Code and SubIndustry
 */
var mubotyvalues = [];
var postCdOld = '';
function setMubotyOnPostalCodeIMS(postCd, subIndustryCd, clientTier) {

  if (FormManager.getActualValue('reqType') != 'C' || FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if ((custSubGrp == 'CHBUS') || (custSubGrp == 'XCHBP') || (custSubGrp == 'CHINT') || (custSubGrp == 'XCHIN') || (custSubGrp == 'LIINT') || (custSubGrp == 'LIBUS')) {
    return;
  }

  var zs01ReqId = FormManager.getActualValue('reqId');
  var qParams = {
    REQ_ID : zs01ReqId,
    ADDR_TYPE : 'ZS01'
  };

  var result = cmr.query('ADDR.GET.POST_CD.BY_REQID', qParams);
  var postCd = FormManager.getActualValue('postCd');
  if (postCd == postCdOld) {
    postCd = result.ret1;
  }
  if (postCd != '') {
    postCdOld = postCd;
  }
  if (((custSubGrp != 'CHBUS') || (custSubGrp != 'XCHBP') || (custSubGrp != 'CHINT') || (custSubGrp != 'XCHIN')) && (postCd == '')) {
    postCd = result.ret1;
  }
  // for cross use post cd 3000 values
  if (custSubGrp == 'XCHCM') {
    postCd = 3000;
  }

  var isuCd = FormManager.getActualValue('isuCd');
  var ims = FormManager.getActualValue('subIndustryCd');
  var clientTier = FormManager.getActualValue('clientTier');

  var isuCtc = isuCd + clientTier;

  var postalCode = [ {
    'postalCode' : {
      'min' : 3000,
      'max' : 9999
    },
    'img' : 'U,K,A,T',
    'isuCtc' : '32S',
    'muboty' : '0214231'
  }, {
    'postalCode' : {
      'min' : 1000,
      'max' : 2999
    },
    'img' : 'U,K,A,T',
    'isuCtc' : '32S',
    'muboty' : '0214332'
  }, {
    'postalCode' : {
      'min' : 3000,
      'max' : 9999
    },
    'img' : 'D,R,W,Z',
    'isuCtc' : '32S',
    'muboty' : '0214235'
  }, {
    'postalCode' : {
      'min' : 1000,
      'max' : 2999
    },
    'img' : 'D,R,W,Z',
    'isuCtc' : '32S',
    'muboty' : '0214336' // old value - 0214243 - changed on 10-02-2020
  }, {
    'postalCode' : {
      'min' : 3000,
      'max' : 9999
    },
    'img' : 'C',
    'isuCtc' : '32S',
    'muboty' : '0214237'
  }, {
    'postalCode' : {
      'min' : 1000,
      'max' : 2999
    },
    'img' : 'C',
    'isuCtc' : '32S',
    'muboty' : '0214338'
  }, {
    'postalCode' : {
      'min' : 3000,
      'max' : 9999
    },
    'img' : 'V,J,P,L,M',
    'isuCtc' : '32S',
    'muboty' : '0214239'
  }, {
    'postalCode' : {
      'min' : 1000,
      'max' : 2999
    },
    'img' : 'V,J,P,L,M',
    'isuCtc' : '32S',
    'muboty' : '0214340'
  }, {
    'postalCode' : {
      'min' : 3000,
      'max' : 9999
    },
    'img' : 'F,S,N',
    'isuCtc' : '32S',
    'muboty' : '0214241'
  }, {
    'postalCode' : {
      'min' : 1000,
      'max' : 2999
    },
    'img' : 'F,S,N',
    'isuCtc' : '32S',
    'muboty' : '0214342'
  }, {
    'postalCode' : {
      'min' : 3000,
      'max' : 9999
    },
    'img' : 'E,Y,G',
    'isuCtc' : '32S',
    'muboty' : '0214243'
  }, {
    'postalCode' : {
      'min' : 1000,
      'max' : 2999
    },
    'img' : 'E,Y,G',
    'isuCtc' : '32S',
    'muboty' : '0214344'
  }, {
    'postalCode' : {
      'min' : 3000,
      'max' : 9999
    },
    'img' : 'H,X',
    'isuCtc' : '32S',
    'muboty' : '0214245'
  }, {
    'postalCode' : {
      'min' : 1000,
      'max' : 2999
    },
    'img' : 'H,X',
    'isuCtc' : '32S',
    'muboty' : '0214346'
  } ];

  if (mubotyvalues.length == 0) {
    var fieldId = 'searchTerm';
    var spinnerId = 'searchTerm_spinner';
    var queryId = 'lov';
    var params = 'fieldId=SearchTerm&cmrIssuingCntry=_cmrIssuingCntry';
    if (fieldId) {
      var queryParams = {};
      queryParams.queryId = queryId;
      if (params) {
        var pairs = params.split('&');
        for (var i = 0; i < pairs.length; i++) {
          var pair = pairs[i].split('=');
          if (pair.length == 2) {
            if (pair[1].substring(0, 1) == '_') {
              var field = pair[1].substring(1);
              var dynamicValue = FormManager.getActualValue(field);
              // no other way to force CMR Issuing Country :(
              if (field == 'cmrIssuingCntry' && dynamicValue == '' && typeof (_pagemodel) != 'undefined') {
                dynamicValue = _pagemodel.cmrIssuingCntry;
              }
              eval('queryParams.' + pair[0] + ' = "' + dynamicValue + '";');
            } else {
              eval('queryParams.' + pair[0] + ' = "' + pair[1] + '";');
            }
          } else if (pair == 'nocache') {
            queryParams.nocache = 'Y';
          }
        }
      }
    }
    $.post(cmr.CONTEXT_ROOT + "/dropdown/lov/list.json", queryParams, function(response) {
      for (i = 0; i < response.listItems.items.length; i++) {
        mubotyvalues.push(response.listItems.items[i].id);
      }
    });
  }

  for (var i = 0; i < postalCode.length; i++) {
    var data = postalCode[i];
    if (postCd >= data.postalCode.min && postCd <= data.postalCode.max) {
      var chars = data.img.split(",");
      for (var j = 0; j < chars.length; j++) {
        if (ims != '' && ims.length >= 1 && ims.substring(0, 1) == chars[j]) {
          if (mubotyvalues.length != 0) {
            if (isuCtc == '32S') {
              var muboty = [];
              var postCd = FormManager.getActualValue('postCd');
              muboty.push(data.muboty);
              FormManager.limitDropdownValues(FormManager.getField('searchTerm'), muboty);
              if (muboty.length == 1) {
                if (role == 'REQUESTER' && (custSubGrp == 'CHIBM' || custSubGrp == 'LIIBM' || custSubGrp == 'CHPRI' || custSubGrp == 'LIPRI')) {
                  FormManager.readOnly('searchTerm');
                }
                FormManager.setValue('searchTerm', muboty[0]);
              }
              if (muboty.length == 0 || postCd.length > 4) {
                FormManager.setValue('searchTerm', '');
              }
            } else {
              FormManager.limitDropdownValues(FormManager.getField('searchTerm'), mubotyvalues);
              FormManager.setValue('searchTerm', '');
            }
          }
        }
      }
    }
  }
}

var postCdOldN = '';
var mubotyvalues = [];
function setMubotyOnPostalCodeIMS32N(postCd, subIndustryCd, clientTier) {

  if (FormManager.getActualValue('reqType') != 'C' || FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if ((custSubGrp == 'CHBUS') || (custSubGrp == 'XCHBP') || (custSubGrp == 'CHINT') || (custSubGrp == 'XCHIN') || (custSubGrp == 'LIINT') || (custSubGrp == 'LIBUS')) {
    return;
  }

  var zs01ReqId = FormManager.getActualValue('reqId');
  var qParams = {
    REQ_ID : zs01ReqId,
    ADDR_TYPE : 'ZS01'
  };

  var result = cmr.query('ADDR.GET.POST_CD.BY_REQID', qParams);
  var postCd = FormManager.getActualValue('postCd');
  if (postCd == postCdOldN) {
    postCd = result.ret1;
  }
  if (postCd != '') {
    postCdOldN = postCd;
  }
  if (((custSubGrp != 'CHBUS') || (custSubGrp != 'XCHBP') || (custSubGrp != 'CHINT') || (custSubGrp != 'XCHIN')) && (postCd == '')) {
    postCd = result.ret1;
  }
  if (custSubGrp == 'XCHCM') {
    postCd = 3000;
  }

  var isuCd = FormManager.getActualValue('isuCd');
  var ims = FormManager.getActualValue('subIndustryCd');
  var clientTier = FormManager.getActualValue('clientTier');

  var isuCtc = isuCd + clientTier;

  var postalCode1 = [ {
    'postalCode1' : {
      'min' : 1000,
      'max' : 2999
    },
    'img' : 'B',
    'isuCtc' : '32N',
    'muboty' : '0421336'
  }, {
    'postalCode1' : {
      'min' : 3000,
      'max' : 9999
    },
    'img' : 'B',
    'isuCtc' : '32N',
    'muboty' : '0421435'
  } ];

  if (mubotyvalues.length == 0) {
    var fieldId = 'searchTerm';
    var spinnerId = 'searchTerm_spinner';
    var queryId = 'lov';
    var params = 'fieldId=SearchTerm&cmrIssuingCntry=_cmrIssuingCntry';
    if (fieldId) {
      var queryParams = {};
      queryParams.queryId = queryId;
      if (params) {
        var pairs = params.split('&');
        for (var i = 0; i < pairs.length; i++) {
          var pair = pairs[i].split('=');
          if (pair.length == 2) {
            if (pair[1].substring(0, 1) == '_') {
              var field = pair[1].substring(1);
              var dynamicValue = FormManager.getActualValue(field);
              // no other way to force CMR Issuing Country :(
              if (field == 'cmrIssuingCntry' && dynamicValue == '' && typeof (_pagemodel) != 'undefined') {
                dynamicValue = _pagemodel.cmrIssuingCntry;
              }
              eval('queryParams.' + pair[0] + ' = "' + dynamicValue + '";');
            } else {
              eval('queryParams.' + pair[0] + ' = "' + pair[1] + '";');
            }
          } else if (pair == 'nocache') {
            queryParams.nocache = 'Y';
          }
        }
      }
    }
    $.post(cmr.CONTEXT_ROOT + "/dropdown/lov/list.json", queryParams, function(response) {
      for (i = 0; i < response.listItems.items.length; i++) {
        mubotyvalues.push(response.listItems.items[i].id);
      }
    });
  }

  for (var i = 0; i < postalCode1.length; i++) {
    var data = postalCode1[i];
    if (postCd >= data.postalCode1.min && postCd <= data.postalCode1.max) {
      var chars = data.img.split(",");
      for (var j = 0; j < chars.length; j++) {
        if (ims != '' && ims.length >= 1 && ims.substring(0, 1) == chars[j]) {
          if (mubotyvalues.length != 0) {
            if (isuCtc == '32N') {
              var muboty = [];
              muboty.push(data.muboty);
              FormManager.limitDropdownValues(FormManager.getField('searchTerm'), muboty);
              if (muboty.length == 1) {
                FormManager.setValue('searchTerm', muboty[0]);
                if (role == 'REQUESTER' && (custSubGrp == 'CHIBM' || custSubGrp == 'LIIBM' || custSubGrp == 'CHPRI' || custSubGrp == 'LIPRI')) {
                  FormManager.readOnly('searchTerm');
                }
              }
              if (muboty.length == 0 || postCd.length > 4) {
                FormManager.setValue('searchTerm', '');
              }
            } else {
              FormManager.limitDropdownValues(FormManager.getField('searchTerm'), mubotyvalues);
              FormManager.setValue('searchTerm', '');
            }
          }
        }
      }
    }
  }
}

function onSavingAddress(cntry, addressMode, saving, finalSave, force) {
  console.log(">>>> onSavingAddress ");
  var reqType = null;
  if (typeof (_pagemodel) != 'undefined') {
    reqType = FormManager.getActualValue('reqType');
  }
  if ((finalSave || force) && cmr.addressMode) {
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
    if ((addrType == 'ZS01' || copyingToA)) {
      if (reqType == 'C')
        autoSetAbbrevNmLogic();
      // setCurrencyCd();
      addVatSuffixForCustLangCd();
    }

  }
}

function autoSetAbbrevNmLogic() {
  console.log("autoSetAbbrevNmLogic");
  var _abbrevNm = null;
  var zs01ReqId = FormManager.getActualValue('reqId');
  var qParams = {
    REQ_ID : zs01ReqId,
  };
  var result = cmr.query('ADDR.GET.CUSTNM1.BY_REQID', qParams);
  var custNm1 = FormManager.getActualValue('custNm1');
  if (custNm1 == '')
    custNm1 = result.ret1;
  _abbrevNm = custNm1;

  if (_abbrevNm && _abbrevNm.length > 30) {
    _abbrevNm = _abbrevNm.substring(0, 30);
  }
  FormManager.setValue('abbrevNm', _abbrevNm);
}

/* defect : 1844770 - set value when DnB search */
function setAbbrvNmSwiss() {
  var reqType = FormManager.getActualValue('reqType');
  var reqId = FormManager.getActualValue('reqId');
  var role = FormManager.getActualValue('userRole').toUpperCase();

  if (reqType == 'C' && role == 'REQUESTER') {
    if (reqId != null) {
      reqParam = {
        REQ_ID : reqId,
      };
    }
    var custNm = cmr.query('ADDR.GET.CUSTNM1.BY_REQID', reqParam);
    var abbrvNm = custNm.ret1;
    if (abbrvNm && abbrvNm.length > 30) {
      abbrvNm = abbrvNm.substring(0, 30);
    }
    if (abbrvNm != null) {
      FormManager.setValue('abbrevNm', abbrvNm);
    }
  }
}

function setFieldsMandtStatus() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custGrp = FormManager.getActualValue('custGrp');
  var custSubGrp = FormManager.getActualValue('custSubGrp').substring(2, 5);
  var landCntry = FormManager.getActualValue('landCntry');
  var countryUse = FormManager.getActualValue('countryUse');
  var reqType = null;
  if (typeof (_pagemodel) != 'undefined') {
    reqType = FormManager.getActualValue('reqType');
  }
  FormManager.readOnly('cmrOwner');
  FormManager.addValidator('sensitiveFlag', Validators.REQUIRED, [ 'Sensitive Flag' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('cmrOwner', Validators.REQUIRED, [ 'CMR Owner' ], 'MAIN_IBM_TAB');
  FormManager.addValidator('clientTier', Validators.REQUIRED, [ 'Client Tier' ], 'MAIN_IBM_TAB');
  FormManager.addValidator('isicCd', Validators.REQUIRED, [ 'ISIC' ], 'MAIN_CUST_TAB');

  if (role == 'REQUESTER') {
    FormManager.readOnly('sensitiveFlag');
    // FormManager.readOnly('currencyCd');
    if (reqType == 'U') {
      FormManager.enable('clientTier');
    }
  } else {
    // FormManager.enable('currencyCd');
    FormManager.enable('abbrevNm');
    FormManager.enable('sensitiveFlag');
  }
  if (reqType == 'C') {
    if (role == 'REQUESTER') {
      FormManager.readOnly('taxCd1');
    } else {
      FormManager.enable('taxCd1');
    }
  }

  if (reqType == 'C') {
    FormManager.addValidator('custClass', Validators.REQUIRED, [ 'Customer Class' ], 'MAIN_CUST_TAB');
    if (custSubGrp) {
      FormManager.addValidator('isuCd', Validators.REQUIRED, [ 'ISU' ], 'MAIN_IBM_TAB');
      FormManager.addValidator('clientTier', Validators.REQUIRED, [ 'Client Tier Code' ], 'MAIN_IBM_TAB');
      FormManager.addValidator('taxCd1', Validators.REQUIRED, [ 'Tax Code' ], 'MAIN_CUST_TAB');
    }
  } else {
    FormManager.enable('embargoCd');
    FormManager.removeValidator('custClass', Validators.REQUIRED);
    FormManager.removeValidator('isuCd', Validators.REQUIRED);
    FormManager.removeValidator('clientTier', Validators.REQUIRED);

    if (role == 'REQUESTER') {
      FormManager.readOnly('custClass');
    } else {
      FormManager.enable('custClass');
    }
  }
  if (custGrp == 'CROSS' || (reqType == 'U' && custGrp == '') || (countryUse == '848LI' && landCntry != 'LI')) {
    // FormManager.removeValidator('currencyCd', Validators.REQUIRED);
  } else if (custGrp == 'LILOC' || custGrp == 'CHLOC') {
    // FormManager.addValidator('currencyCd', Validators.REQUIRED, [ 'Currency
    // Code' ], 'MAIN_CUST_TAB');
  }

  // set Muboty mandt status
  var custSubGrp1 = [ 'COM', 'HCM', 'GOV', 'HGV', 'SOF', 'HSF', '3PA', 'H3P', 'PRI', 'HPR', 'IBM', 'HIB' ];
  var custSubGrp2 = [ 'BUS', 'HBP', 'HIN', 'INT' ];
  if (reqType == 'C' && custSubGrp) {
    if (custSubGrp2.indexOf(custSubGrp) >= 0) {
      if (role == 'REQUESTER') {
        FormManager.readOnly('searchTerm');
      } else {
        FormManager.enable('searchTerm');
      }
    }
  }
  if (reqType == 'C') {
    FormManager.addValidator('searchTerm', Validators.REQUIRED, [ 'MUBOTY' ], 'MAIN_IBM_TAB');
  } else {
    FormManager.removeValidator('searchTerm', Validators.REQUIRED);
    FormManager.enable('isuCd');
  }
}

function addAddressTypeValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqType = null;
        if (typeof (_pagemodel) != 'undefined') {
          reqType = FormManager.getActualValue('reqType');
        }
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount == 0) {
          if (reqType == 'C') {
            return new ValidationResult(null, false, 'Contract(Sold-to) Address is mandatory.');
          } else {
            return new ValidationResult(null, true);
          }
        }

        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          var record = null;
          var type = null;
          var installingCnt = 0;
          var billingCnt = 0;
          var contractCnt = 0;

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
              contractCnt++;
            } else if (type == 'ZP01') {
              billingCnt++;
            } else if (type == 'ZI01') {
              installingCnt++;
            }
          }
          if (contractCnt > 1) {
            return new ValidationResult(null, false, 'Only one Contract address can be defined. Please remove the additional Contract address.');
          } else {
            return new ValidationResult(null, true);
          }
        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function addEmbargoCdValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqType = null;
        var emabrgoCd = FormManager.getActualValue('ordBlk');
        if (typeof (_pagemodel) != 'undefined') {
          reqType = FormManager.getActualValue('reqType');
        }
        if (emabrgoCd == null || emabrgoCd == '88' || emabrgoCd == '94' || emabrgoCd == '') {
          return new ValidationResult(null, true);
        } else {
          return new ValidationResult(null, false, 'Value of Emabrgo code can only be 88, 94 or blank.');
        }
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function addVatSuffixForCustLangCd() {
  var reqId = FormManager.getActualValue('reqId');
  var reqType = FormManager.getActualValue('reqType');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var custSubGrpList = [ 'CHIBM', 'LIIBM', 'CHPRI', 'LIPRI', 'CHINT', 'LIINT' ];
  if (reqType != 'C') {
    return;
  }
  var result = cmr.query('ADDR.GET.LAND_CNTRY.BY_REQID', {
    REQ_ID : reqId,
    ADDR_TYPE : 'ZS01'
  });
  var landCntry = result.ret1;
  if (landCntry != 'CH') {
    return;
  }
  var qParams = {
    REQ_ID : reqId,
  };

  var custLangCd = '';
  if ("ZS01" == FormManager.getActualValue('addrType')) {
    custangCd = FormManager.getActualValue('custLangCd');
  }
  if (custLangCd == null || custLangCd == '' || custLangCd == undefined) {
    var result = cmr.query('ADDR.GET.CUST_LANG_CD.BY_REQID', qParams);
    if (result.ret1 != null && result.ret1 != '') {
      custLangCd = result.ret1;
    }
  }

  if (!custSubGrpList.includes(custSubGrp)) {
    var result = cmr.query('ADDR.GET.VAT_REQID', qParams);
    var vat = result.ret1;
    if (vat != '' && vat != null && vat != undefined && vat.length >= 15) {
      var vatOnly = vat.substring(0, 15);
      if ((custLangCd == 'E' || custLangCd == 'D') && vat.substring(16, 20) != 'Mwst') {
        FormManager.setValue('vat', vatOnly.concat(" Mwst"));
      } else if ((custLangCd == 'I') && vat.substring(16, 19) != 'IVA') {
        FormManager.setValue('vat', vatOnly.concat(" IVA"));
      } else if (custLangCd == 'F' && vat.substring(16, 19) != 'TVA') {
        FormManager.setValue('vat', vatOnly.concat(" TVA"));
      }
    }
  } else {
    FormManager.readOnly('vat');
    FormManager.setValue('vat', '');
  }
}

function setCurrencyCd() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (role == 'PROCESSOR')
    return;

  var reqType = FormManager.getActualValue('reqType');
  if (reqType == 'U') {
    return;
  }
  console.log(">>>> setCurrencyCd ");

  var custGrp = FormManager.getActualValue('custGrp');
  var zs01LandCntry = FormManager.getActualValue('landCntry');
  var reqId = FormManager.getActualValue('reqId');
  if (reqId != null) {
    reqParam = {
      REQ_ID : reqId,
    };
  }
  var results = cmr.query('ADDR.GET.ZS01LANDCNTRY.BY_REQID', reqParam);
  if (zs01LandCntry == '')
    zs01LandCntry = results.ret2;
  var currencyCd = null;
  if (custGrp == 'CROSS') {
    currencyCd = 'CHF';
    FormManager.removeValidator('currencyCd', Validators.REQUIRED);
  } else if (custGrp == 'CHLOC' && zs01LandCntry == 'CH') {
    currencyCd = 'CHF';
  } else if (custGrp == 'LILOC' && zs01LandCntry == 'LI') {
    currencyCd = 'CHF';

  }
  FormManager.setValue('currencyCd', currencyCd);
}

function setCustClassCd() {
  var reqType = null;
  var custSubGrp = FormManager.getActualValue('custSubGrp');

  if (typeof (_pagemodel) != 'undefined') {
    reqType = FormManager.getActualValue('reqType');
  }

  if (reqType == 'C') {
    switch (custSubGrp) {
    case 'CHCOM':
    case 'LICOM':
    case 'XCHCM':
      FormManager.limitDropdownValues(FormManager.getField('custClass'), [ '11', '47' ]);
      break;
    case 'CHIBM':
    case 'LIIBM':
    case 'XCHIB':
      FormManager.limitDropdownValues(FormManager.getField('custClass'), [ '71' ]);
      break;
    case 'CHSOF':
    case 'LISOF':
    case 'XCHSF':
    case 'CH3PA':
    case 'LI3PA':
    case 'XCH3P':
      FormManager.limitDropdownValues(FormManager.getField('custClass'), [ '11' ]);
      break;
    case 'CHINT':
    case 'LIINT':
    case 'XCHIN':
      FormManager.limitDropdownValues(FormManager.getField('custClass'), [ '81' ]);
      break;
    case 'CHGOV':
    case 'LIGOV':
    case 'XCHGV':
      FormManager.limitDropdownValues(FormManager.getField('custClass'), [ '12' ]);
      break;

    case 'CHBUS':
    case 'XCHBP':
    case 'LIBUS':
      FormManager.limitDropdownValues(FormManager.getField('custClass'), [ '41', '43', '45' ]);
      break;
    case 'CHPRI':
    case 'LIPRI':
    case 'XCHPR':
      FormManager.limitDropdownValues(FormManager.getField('custClass'), [ '60' ]);
      break;
    default:
    }
  }
}

function showDeptNoForInternalsOnlySWISS() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var subCustGrp = FormManager.getActualValue('custSubGrp');
  if (subCustGrp == 'CHINT' || subCustGrp == 'LIINT' || subCustGrp == 'XCHIN') {
    FormManager.show('InternalDept', 'ibmDeptCostCenter');
  } else {
    FormManager.clearValue('ibmDeptCostCenter');
    FormManager.resetValidations('ibmDeptCostCenter');
    FormManager.hide('InternalDept', 'ibmDeptCostCenter');
  }
}

function addCrossBorderValidatorFrSWISS() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqType = FormManager.getActualValue('reqType');
        // only Create type will be validated
        if (reqType != 'C') {
          return new ValidationResult(null, true);
        }
        var reqId = FormManager.getActualValue('reqId');
        var defaultlandCntry = 'CH';
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        var countryUse = FormManager.getActualValue('countryUse');
        var custGrp = FormManager.getActualValue('custGrp');
        var result = cmr.query('VALIDATOR.CROSSBORDER', {
          REQID : reqId
        });
        if (countryUse.length > 3) {
          defaultlandCntry = 'LI';
        }
        var defaultlandCntryList = [ 'CH', 'LI' ];
        if (result && result.ret1 && result.ret1 != defaultlandCntry && (custGrp == 'CHLOC' || custGrp == 'LILOC')) {
          return new ValidationResult(null, false, 'Landed Country value of the Contract (Main) Address should be \'' + defaultlandCntry + '\' for Non Cross-Border customers.');
        } else if (result && result.ret1 && defaultlandCntryList.indexOf(result.ret1) >= 0 && custGrp == 'CROSS') {
          return new ValidationResult(null, false, 'Landed Country value of the Contract (Main) Address should not be \'' + defaultlandCntryList + '\' for Cross-Border customers.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

/* defect : 1836129, 1846364 */

/*
 * function addFormatFieldsValidator(){
 * FormManager.addFormValidator((function(){ return{ validate : function(){ var
 * dept = FormManager.getActualValue('dept'); var floor =
 * FormManager.getActualValue('floor'); var bldg =
 * FormManager.getActualValue('bldg'); var custNm1 =
 * FormManager.getActualValue('custNm1'); var custNm2 =
 * FormManager.getActualValue('custNm2'); var addrTxt =
 * FormManager.getActualValue('addrTxt'); var city1 =
 * FormManager.getActualValue('city1'); var poBox =
 * FormManager.getActualValue('poBox'); var custPhone =
 * FormManager.getActualValue('custPhone');
 * 
 * if(custNm1 && custNm1.length > 0 && !custNm1.match("^[0-9A-Za-z\\s]+$")){
 * return new ValidationResult({ id : 'custNm1', type : 'text', name : 'custNm1'
 * },false, 'Only the characters listed are allowed to input due to system
 * limitation.' + ' (1) A to Z (uppercase & lowercase) ' + ' (2) 0 to 9 ' + '
 * (3) Alphanumeric '); }
 * 
 * if(custNm2 && custNm2.length > 0 && !custNm2.match("^[0-9A-Za-z\\s]+$")){
 * return new ValidationResult({ id : 'custNm2', type : 'text', name : 'custNm2'
 * },false, 'Only the characters listed are allowed to input due to system
 * limitation.' + ' (1) A to Z (uppercase & lowercase) ' + ' (2) 0 to 9 ' + '
 * (3) Alphanumeric '); }
 * 
 * if(addrTxt && addrTxt.length > 0 && !addrTxt.match("^[0-9A-Za-z\\s]+$")){
 * return new ValidationResult({ id : 'addrTxt', type : 'text', name : 'addrTxt'
 * },false, 'Only the characters listed are allowed to input due to system
 * limitation.' + ' (1) A to Z (uppercase & lowercase) ' + ' (2) 0 to 9 ' + '
 * (3) Alphanumeric '); }
 * 
 * if(city1 && city1.length > 0 && !city1.match("^[0-9A-Za-z\\s]+$")){ return
 * new ValidationResult({ id : 'city1', type : 'text', name : 'city1' },false,
 * 'Only the characters listed are allowed to input due to system limitation.' + '
 * (1) A to Z (uppercase & lowercase) ' + ' (2) 0 to 9 ' + ' (3) Alphanumeric
 * '); }
 * 
 * if(poBox && poBox.length > 0 && !poBox.match("^[0-9A-Za-z\\s]+$")){ return
 * new ValidationResult({ id : 'poBox', type : 'text', name : 'poBox' },false,
 * 'Only the characters listed are allowed to input due to system limitation.' + '
 * (1) A to Z (uppercase & lowercase) ' + ' (2) 0 to 9 ' + ' (3) Alphanumeric
 * '); }
 * 
 * if(custPhone && custPhone.length > 0 &&
 * !custPhone.match("^[0-9A-Za-z\\s]+$")){ return new ValidationResult({ id :
 * 'custPhone', type : 'text', name : 'custPhone' },false, 'Only the characters
 * listed are allowed to input due to system limitation.' + ' (1) A to Z
 * (uppercase & lowercase) ' + ' (2) 0 to 9 ' + ' (3) Alphanumeric '); }
 * 
 * if(dept && dept.length > 0 && !dept.match("^[0-9A-Za-z\\s]+$")){ return new
 * ValidationResult({ id : 'dept', type : 'text', name : 'dept' },false, 'Only
 * the characters listed are allowed to input due to system limitation.' + ' (1)
 * A to Z (uppercase & lowercase) ' + ' (2) 0 to 9 ' + ' (3) Alphanumeric '); }
 * if(floor && floor.length > 0 && !floor.match("^[0-9A-Za-z\\s]+$")){ return
 * new ValidationResult({ id : 'floor', type : 'text', name : 'floor' },false,
 * 'Only the characters listed are allowed to input due to system limitation.' + '
 * (1) A to Z (uppercase & lowercase) ' + ' (2) 0 to 9 ' + ' (3) Alphanumeric
 * '); } if(bldg && bldg.length > 0 && !bldg.match("^[0-9A-Za-z\\s]+$")){ return
 * new ValidationResult({ id : 'bldg', type : 'text', name : 'bldg' },false,
 * 'Only the characters listed are allowed to input due to system limitation.' + '
 * (1) A to Z (uppercase & lowercase) ' + ' (2) 0 to 9 ' + ' (3) Alphanumeric
 * '); } return new ValidationResult(null, true); } }; })(), null,
 * 'frmCMR_addressModal'); }
 */

/* defect : 1837169 */

function addCMRNumberValidation() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var role = FormManager.getActualValue('userRole').toUpperCase();
        var reqType = FormManager.getActualValue('reqType');
        var cmrNumber = FormManager.getActualValue('cmrNo');
        if (reqType == 'U') {
          return new ValidationResult(null, true);
        }
        if (reqType == 'C' && role == 'PROCESSOR') {
          if (FormManager.getActualValue('cmrNo') != '' && cmrNumber.length == 6) {
            var cmrNumber = FormManager.getActualValue('cmrNo');
            var ifProspect = cmrNumber.startsWith("P") ? true : false;
            qParams = {
              KATR6 : '848',
              MANDT : cmr.MANDT,
              ZZKV_CUSNO : cmrNumber,
            };
            var record = cmr.query('CHECK_CMR_RDC', qParams);
            var rdcCMRCount = record.ret1;

            if (Number(rdcCMRCount) > 0) {
              if (ifProspect) {
                return new ValidationResult(null, false, 'CMR number field can be blanked out or enter a unique cmr no. value.');
              } else {
                return new ValidationResult(null, false, 'Please enter another CMR Number as entered CMR Number already exists in RDC.');
              }
            }
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

/* defect : 1837169 */

function addFormatForCMRNumValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var cmrNo = FormManager.getActualValue('cmrNo');
        var reqType = FormManager.getActualValue('reqType');
        var role = FormManager.getActualValue('userRole').toUpperCase();
        if (reqType == 'C' && role == 'PROCESSOR') {
          if (cmrNo && cmrNo.length > 0 && !cmrNo.match("^[0-9]+$") && !cmrNo.startsWith("P")) {
            return new ValidationResult({
              id : 'cmrNo',
              type : 'text',
              name : 'cmrNo'
            }, false, 'Invalid format of CMR Number. Format should be NNNNNN');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

/* defect : 1837169 */
function addCmrNoLengthValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var cmrNo = FormManager.getActualValue('cmrNo');
        var role = FormManager.getActualValue('userRole').toUpperCase();

        if (FormManager.getActualValue('reqType') == 'C' && FormManager.getActualValue('Processor') == 'true') {
          return new ValidationResult(null, true);
        }
        if (FormManager.getActualValue('Processor') == 'true') {
          return new ValidationResult(null, true);
        }
        if (cmrNo != '' && cmrNo.length != 6) {
          return new ValidationResult(null, false, 'CMR Number should be exactly 6 digits long.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

/* defect : 1853292 */
var _addrTypesForSwiss = [ 'ZD01', 'ZI01', 'ZP01', 'ZS01' ];
function addPreferedLangValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          var recordList = null;
          var reqType = FormManager.getActualValue('reqType')
          var role = FormManager.getActualValue('userRole').toUpperCase();
          var islangCdEmpty = false;

          for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
            recordList = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
            if (recordList == null && _allAddressData != null && _allAddressData[i] != null) {
              recordList = _allAddressData[i];
            }
            addrCustLangCd = recordList.custLangCd;

            if (typeof (addrCustLangCd) == 'object') {
              addrCustLangCd = addrCustLangCd[0];
            }
            if (role == 'PROCESSOR' && (addrCustLangCd == null || addrCustLangCd == '')) {

              islangCdEmpty = true;
            }
          }

          if (islangCdEmpty == true) {
            return new ValidationResult(null, false, 'Prefered Langauge should not be empty.');
          }
          return new ValidationResult(null, true);
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

/* defect : 1837046 */
function defaultCapIndicator() {
  var reqType = FormManager.getActualValue('reqType');
  if (reqType == 'C' || reqType == 'U') {
    FormManager.getField('capInd').set('checked', true);
    FormManager.readOnly('capInd');
  }
}

function updateAddrTypeList(cntry, addressMode) {
  if (cmr.currentRequestType == 'C' && (addressMode == 'newAddress' || addressMode == 'copyAddress' || addressMode == 'updateAddress')) {
    cmr.hideNode('radiocont_ZS02');
  } else if (cmr.currentRequestType == 'U' && (addressMode == 'newAddress' || addressMode == 'copyAddress')) {
    cmr.hideNode('radiocont_ZS02');
  } else {
    cmr.showNode('radiocont_ZS02');
  }
}

function addLandedCountryHandler(cntry, addressMode, saving, finalSave) {
  if (!saving) {
    var counrtyUse = FormManager.getActualValue('countryUse');
    var deflandCntry = counrtyUse == '848LI' ? 'LI' : 'CH';
    if (addressMode == 'newAddress') {
      FilteringDropdown['val_landCntry'] = deflandCntry;
      FormManager.setValue('landCntry', deflandCntry);
    } else {
      FilteringDropdown['val_landCntry'] = null;
    }
  }
}

function canRemoveAddress(value, rowIndex, grid) {
  var rowData = grid.getItem(rowIndex);
  var importInd = rowData.importInd[0];
  var reqType = FormManager.getActualValue('reqType');
  if ('U' == reqType && 'Y' == importInd) {
    return false;
  }
  return true;
}

function canCopyAddress(value, rowIndex, grid) {
  return false;
}
function ADDRESS_GRID_showCheck(value, rowIndex, grid) {
  return canRemoveAddress(value, rowIndex, grid);
}

function reqReasonOnChange() {
  var reqReason = FormManager.getActualValue('reqReason');
  if (reqReason == 'IGF' && isZD01OrZP01ExistOnCMR()) {
    dojo.byId('radiocont_ZP02').style.display = 'inline-block';
    dojo.byId('radiocont_ZD02').style.display = 'inline-block';
  } else {
    dojo.byId('radiocont_ZP02').style.display = 'none';
    dojo.byId('radiocont_ZD02').style.display = 'none';
  }
  dojo.connect(FormManager.getField('reqReason'), 'onChange', function(value) {
    if (value == 'IGF' && isZD01OrZP01ExistOnCMR()) {
      dojo.byId('radiocont_ZP02').style.display = 'inline-block';
      dojo.byId('radiocont_ZD02').style.display = 'inline-block';
    } else {
      dojo.byId('radiocont_ZP02').style.display = 'none';
      dojo.byId('radiocont_ZD02').style.display = 'none';
    }
  });
}

function isZD01OrZP01ExistOnCMR() {
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

function setPreferredLangAddr() {
  // Based on the value of postal code Customer Language field should be
  // populated on each address:
  //
  // 3000 - 6499 and 6999 - 9999 it is D (German)
  // 6500 - 6999 it is I (Italian)
  // 0000 - 3000 it is F (French)
  //
  // Cross Border it is E (English)
  var reqType = FormManager.getActualValue('reqType');
  // if (reqType != 'C') {
  // return;
  // }
  var zs01ReqId = FormManager.getActualValue('reqId');

  var addrType = FormManager.getActualValue('addrType');
  if (addrType == null || addrType == '' || addrType == undefined) {
    addrType = 'ZS01';
  }

  if (reqType == 'U' && addrType == 'ZS01') {
    return;
  }

  var landCntry = FormManager.getActualValue('landCntry');
  if (landCntry == null || landCntry == '' || landCntry == undefined) {
    var result = cmr.query('ADDR.GET.LAND_CNTRY.BY_REQID', {
      REQ_ID : reqId,
      ADDR_TYPE : addrType
    });
    landCntry = result.ret1;
  }
  if (landCntry == 'CH' || landCntry == 'LI') {
    var qParams = {
      REQ_ID : zs01ReqId,
      ADDR_TYPE : addrType
    };
    var result = cmr.query('ADDR.GET.POST_CD.BY_REQID', qParams);
    var postCd = FormManager.getActualValue('postCd');
    postCd = postCd == undefined || postCd == '' ? result.ret1 : postCd;

    if ((postCd >= 3000 && postCd <= 6499) || (postCd >= 6999 && postCd <= 9999)) {
      FormManager.setValue('custLangCd', 'D');
    } else if (postCd >= 6500 && postCd <= 6999) {
      FormManager.setValue('custLangCd', 'I');
    } else if (postCd >= 0000 && postCd <= 3000) {
      FormManager.setValue('custLangCd', 'F');
    }
  } else {
    FormManager.setValue('custLangCd', 'E');
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

function isZD01OrZP01ExistOnCMR(addressType) {
  addressType = addressType.replace('2', '1');
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

dojo.addOnLoad(function() {
  GEOHandler.SWISS = [ '848' ];
  console.log('adding SWISS functions...');
  GEOHandler.setRevertIsicBehavior(false);
  GEOHandler.enableCustomerNamesOnAddress(GEOHandler.SWISS);
  GEOHandler.addAddrFunction(updateMainCustomerNames, GEOHandler.SWISS);
  GEOHandler.addAddrFunction(onSavingAddress, GEOHandler.SWISS);
  GEOHandler.addAddrFunction(updateAddrTypeList, GEOHandler.SWISS);
  GEOHandler.addAddrFunction(addLandedCountryHandler, GEOHandler.SWISS);
  GEOHandler.addAddrFunction(displayHwMstrInstallFlag, GEOHandler.SWISS);
  GEOHandler.addAddrFunction(checkHwMstrInstallFlag, GEOHandler.SWISS);
  GEOHandler.addAddrFunction(setMubotyOnPostalCodeIMS, GEOHandler.SWISS);
  GEOHandler.addAddrFunction(setMubotyOnPostalCodeIMS32N, GEOHandler.SWISS);

  GEOHandler.addAfterConfig(reqReasonOnChange, GEOHandler.SWISS);
  GEOHandler.addAfterConfig(addHandlersForSWISS, GEOHandler.SWISS);
  GEOHandler.addAfterConfig(addAfterConfigForSWISS, GEOHandler.SWISS);

  // GEOHandler.addAfterTemplateLoad(setCurrencyCd, GEOHandler.SWISS);
  GEOHandler.addAfterTemplateLoad(setCustClassCd, GEOHandler.SWISS);
  GEOHandler.addAfterTemplateLoad(setVatValidatorSWISS, GEOHandler.SWISS);
  GEOHandler.addAfterTemplateLoad(addAfterConfigForSWISS, GEOHandler.SWISS);
  GEOHandler.addAfterTemplateLoad(setMubotyOnPostalCodeIMS, GEOHandler.SWISS);
  GEOHandler.addAfterTemplateLoad(setMubotyOnPostalCodeIMS32N, GEOHandler.SWISS);
  GEOHandler.addAfterConfig(defaultCapIndicator, GEOHandler.SWISS);
  GEOHandler.addAfterConfig(setAbbrvNmSwiss, GEOHandler.SWISS);

  GEOHandler.registerValidator(addHwMstrInstFlgValidator, GEOHandler.SWISS, null, true);
  GEOHandler.registerValidator(addPreferedLangValidator, GEOHandler.SWISS, null, true);
  GEOHandler.registerValidator(addCMRNumberValidation, GEOHandler.SWISS, null, true);
  GEOHandler.registerValidator(addFormatForCMRNumValidator, GEOHandler.SWISS, null, true);
  GEOHandler.registerValidator(addCmrNoLengthValidator, GEOHandler.SWISS, null, true);
  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.SWISS, GEOHandler.ROLE_PROCESSOR, true);
  GEOHandler.registerValidator(addAddressTypeValidator, GEOHandler.SWISS, null, true);
  GEOHandler.registerValidator(addEmbargoCdValidator, GEOHandler.SWISS, null, true);
  // GEOHandler.registerValidator(addVatValidatorForCustLangCd,
  // GEOHandler.SWISS, null, true);
  var countryUse = null;
  if (typeof (_pagemodel) != 'undefined') {
    countryUse = _pagemodel.countryUse;
  }
  if (countryUse != '848LI') {

    GEOHandler.registerValidator(addGenericVATValidator(SysLoc.SWITZERLAND, 'MAIN_CUST_TAB', 'frmCMR', 'ZS01'), [ SysLoc.SWITZERLAND ], null, true);
  }
  GEOHandler.registerValidator(addCrossBorderValidatorFrSWISS, SysLoc.SWITZERLAND, null, true);
  GEOHandler.registerValidator(resetAddrTypeValidation, GEOHandler.SWISS, null, true);
  GEOHandler.registerValidator(name3LengthValidation, GEOHandler.SWISS, null, true);
  GEOHandler.registerValidator(restrictDuplicateAddr, GEOHandler.SWISS, null, true);

  // new phase 2
  GEOHandler.addAfterConfig(setISUCTCOnIMSChange, GEOHandler.SWISS);
  GEOHandler.addAfterTemplateLoad(setISUCTCOnIMSChange, GEOHandler.SWISS);
});