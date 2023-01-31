/* Register SWISS Javascripts */
function addAfterConfigForSWISS() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var impIndc = getImportedIndcForSwiss();

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
    setClientTierValues();

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
    // FormManager.enable('clientTier');
  } else if (reqType == 'C'
      && role == 'REQUESTER'
      && (custSubGrp == 'CHINT' || custSubGrp == 'XCHIN' || custSubGrp == 'LIINT' || custSubGrp == 'CHPRI' || custSubGrp == 'XCHPR' || custSubGrp == 'LIPRI' || custSubGrp == 'CHIBM'
          || custSubGrp == 'XCHIB' || custSubGrp == 'LIIBM' || custSubGrp == 'CHBUS' || custSubGrp == 'XCHBP' || custSubGrp == 'LIBUS')) {
    FormManager.readOnly('clientTier');
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
  
  // CREATCMR-6378
  if(reqType == 'C' && custSubGrp == 'CHBUS'){
    retainVatValueAT();
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
//  setMubotyOnPostalCodeIMS();
  showDeptNoForInternalsOnlySWISS();
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
      // FormManager.enable('vat');
      if (!dijit.byId('vatExempt').get('checked')) {
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
      setMubotyOnPostalCodeIMS(value);
      setSORTLOnIsuCtc();
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
      setSORTLOnIsuCtc();
    });
  }

  if (_PostalCodeHandler == null) {
    _PostalCodeHandler = dojo.connect(FormManager.getField('postCd'), 'onChange', function(value) {
      setMubotyOnPostalCodeIMS(value);
      setPreferredLangAddr(value);
    });
  }

  if (_IMSHandler == null) {
    _IMSHandler = dojo.connect(FormManager.getField('subIndustryCd'), 'onChange', function(value) {
      setISUCTCOnIMSChange();
      setMubotyOnPostalCodeIMS();
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
  var results = cmr.query('IMPORTED_ADDR_ZS01', {
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
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
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
  var vatInd = FormManager.getActualValue('vatInd');
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

function setSORTLOnIsuCtc() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var isuCd = FormManager.getActualValue('isuCd');
  var clientTier = FormManager.getActualValue('clientTier');
  var reqType = FormManager.getActualValue('reqType');
  var isuList = [ '34', '5K', '18', '28' ];
  if (!isuList.includes(isuCd)) {
    return;
  }
  if (isuCd == '34' && clientTier == 'Y') {
    FormManager.setValue('searchTerm', 'T0007971');
  } else if (isuCd == '5K' && clientTier == '') {
    FormManager.setValue('searchTerm', 'T0009084');
  } else if (isuCd == '18' && clientTier == '') {
    FormManager.setValue('searchTerm', 'A0004577');
  } else if (isuCd == '28' && clientTier == '') {
    FormManager.setValue('searchTerm', 'A0005227');
  }
  if ([ '18', '28' ].includes(isuCd) && reqType == 'C') {
    FormManager.resetValidations('clientTier');
  }
}

/*
 * Swiss - sets Client_Tier based on ISU
 */
function setClientTierValues(isuCd) {
  var reqType = FormManager.getActualValue('reqType');
  isuCd = FormManager.getActualValue('isuCd');
  if (FormManager.getActualValue('viewOnlyPage') == 'true' || reqType != 'C') {
    return;
  }
  var isuList = [ '18', '28' ];
  var reqType = FormManager.getActualValue('reqType');
  if (isuList.includes(isuCd)) {
    FormManager.removeValidator('clientTier', Validators.REQUIRED);
    FormManager.setValue('clientTier', '');
    FormManager.readOnly('clientTier');
  } else {
    var role = FormManager.getActualValue('userRole').toUpperCase();
    if (reqType == 'U') {
      FormManager.enable('clientTier');
    }
  }

  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }

  isuCd = FormManager.getActualValue('isuCd');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var clientT = FormManager.getActualValue('clientTier');// CMR-710
  var clientTiers = [];
  if (isuCd != '' && clientT == null) {
    var qParams = {
      _qall : 'Y',
      ISSUING_CNTRY : cntry,
      ISU : '%' + isuCd + '%'
    };
    var results = cmr.query('GET.CTCLIST.BYISU', qParams);
    if (results != null && results.length > 0) {
      for (var i = 0; i < results.length; i++) {
        clientTiers.push(results[i].ret1);
      }
    } else {
      qParams.ISU = '%*%';
      results = cmr.query('GET.CTCLIST.BYISU', qParams);
      if (results != null && results.length > 0) {
        for (var i = 0; i < results.length; i++) {
          clientTiers.push(results[i].ret1);
        }
      }
    }

    if (clientTiers != null && clientTiers.length > 0) {
      FormManager.limitDropdownValues(FormManager.getField('clientTier'), clientTiers);
      if (clientTiers.length == 1) {
        FormManager.setValue('clientTier', clientTiers[0]);
      }
    } else {
      FormManager.resetDropdownValues(FormManager.getField('clientTier'));
      FormManager.setValue('clientTier', '');
    }
  }
}

/*
 * Swiss - sets Muboty based on Postal Code and SubIndustry
 */
var mubotyvalues = [];
var postCdOld = '';
function setMubotyOnPostalCodeIMS(postCd, subIndustryCd, clientTier) {

  if (FormManager.getActualValue('reqType') != 'C' || (cmr.currentRequestType != undefined && cmr.currentRequestType != 'C') || FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (postCd == undefined && subIndustryCd == undefined && clientTier == undefined) {
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
  var ims = FormManager.getActualValue('subIndustryCd').substring(0, 1);
  var clientTier = FormManager.getActualValue('clientTier');

  if (isuCd == null || isuCd == undefined || isuCd == '') {
    return;
    // CMR-710 use 34Q to replace 32S/N
  } else if (isuCd == '34') {
    if (postCd >= 3000) {
      postCd = 2;
    } else {
      postCd = 1;
      ims = '';
    }
  } else {
    postCd = '';
    ims = '';
  }

  var result = cmr.query('SWISS.GET.SORTL_BY_ISUCTCIMS', {
    ISU_CD : '%' + isuCd + '%',
    CLIENT_TIER : '%' + clientTier + '%',
    IMS : '%' + ims + '%',
    POST_CD_RANGE : postCd
  });

  if (result != null && Object.keys(result).length > 0 && result.ret1) {
    FormManager.setValue('searchTerm', result.ret1);
    if (role == 'REQUESTER') {
      FormManager.readOnly('searchTerm');
    }
  } else {
    FormManager.clearValue('searchTerm');
    FormManager.enable('searchTerm');
  }
}

function validateSBOValuesForIsuCtc() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        var clientTier = FormManager.getActualValue('clientTier');
        var isuCd = FormManager.getActualValue('isuCd');
        var sbo = FormManager.getActualValue('salesBusOffCd');
        var validSboList = [];
        var qParams = null;
        
        if (isuCd != '') {
          var results = null;
          if (isuCd + clientTier != '34Q') {
            qParams = {
              _qall : 'Y',
              ISSUING_CNTRY : cntry,
              ISU : '%' + isuCd + '%',
              CTC : '%' + clientTier + '%'
            };
            results = cmr.query('GET.SBOLIST.BYISU', qParams);
          }
        }
        if (results == null || results.length == 0) {
          return new ValidationResult(null, true);
        } else {
          for (let i=0; i<results.length; i++) {
            validSboList.push(results[i].ret1);
          }
          if (!validSboList.includes(sbo)) {
            return new ValidationResult(null, false, 
                'The SBO provided is invalid. It should be from the list: ' + validSboList);
          }
        }
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
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
  // CREATCMR-4293
  // FormManager.addValidator('clientTier', Validators.REQUIRED, [ 'Client Tier'
  // ], 'MAIN_IBM_TAB');
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
      // FormManager.addValidator('clientTier', Validators.REQUIRED, [ 'Client
      // Tier Code' ], 'MAIN_IBM_TAB');
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
  // CMR-4715 -> muboty cannot be blank for update requests
  if (reqType == 'C' || (reqType == 'U' && role == 'REQUESTER')) {
    FormManager.addValidator('searchTerm', Validators.REQUIRED, [ 'SORTL' ], 'MAIN_IBM_TAB');
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
          if (contractCnt == 0) {
            return new ValidationResult(null, false, 'Contract(Sold-to) Address is mandatory.');
          } else if (contractCnt > 1) {
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
        var emabrgoCd = FormManager.getActualValue('ordBlk');
        if (typeof (_pagemodel) != 'undefined') {
          reqType = FormManager.getActualValue('reqType');
        }
        if (emabrgoCd == null || emabrgoCd == '88' || emabrgoCd == '94' || emabrgoCd == '' || (Number(countpaygo) == 1 && role == 'Processor')) {
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
    custLangCd = FormManager.getActualValue('custLangCd');
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
  var reqType = FormManager.getActualValue('reqType');
  if (reqType == 'U' && FormManager.getActualValue('addrType') == 'ZS01') {
    FormManager.readOnly('landCntry');
  } else {
    FormManager.enable('landCntry');
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

// function setPreferredLangAddr() {
// Based on the value of postal code Customer Language field should be
// populated on each address:
//
// 3000 - 6499 and 6999 - 9999 it is D (German)
// 6500 - 6999 it is I (Italian)
// 0000 - 3000 it is F (French)
//
// Cross Border it is E (English)

// var zs01ReqId = FormManager.getActualValue('reqId');
// var qParams = {
// REQ_ID : zs01ReqId,
// };
//
// var result = cmr.query('ADDR.GET.POST_CD.BY_REQID', qParams);
// var postCd = FormManager.getActualValue('postCd');
// postCd = postCd == undefined || postCd == '' ? result.ret1 : postCd;
//
// if ((postCd >= 3000 && postCd <= 6499) || (postCd >= 6999 && postCd <= 9999))
// {
// FormManager.setValue('custLangCd', 'D');
// } else if (postCd >= 6500 && postCd <= 6999) {
// FormManager.setValue('custLangCd', 'I');
// } else if (postCd >= 0000 && postCd <= 3000) {
// FormManager.setValue('custLangCd', 'F');
// }
// }

function lockIBMTabForSWISS() {
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubType = FormManager.getActualValue('custSubGrp');
  if (reqType == 'C' && role == 'REQUESTER') {
    FormManager.readOnly('cmrNo');
    FormManager.readOnly('cmrOwner');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('inacCd');
    FormManager.readOnly('searchTerm');
    FormManager.readOnly('enterprise');
    FormManager.readOnly('buyingGroupId');
    FormManager.readOnly('globalBuyingGroupId');
    FormManager.readOnly('covId');
    FormManager.readOnly('geoLocationCode');
    FormManager.readOnly('dunsNo');
    if (custSubType != 'CHBUS' && custSubType != 'LIBUS') {
      FormManager.readOnly('ppsceid');
    } else {
      FormManager.enable('ppsceid');
    }
    FormManager.readOnly('soeReqNo');
    if (custSubType != 'CHINT' && custSubType != 'LIINT') {
      FormManager.readOnly('ibmDeptCostCenter');
    } else {
      FormManager.enable('ibmDeptCostCenter');
    }
  }
  if (reqType == 'C' && role == 'PROCESSOR') {
    if (custSubType.includes('BUS') || custSubType.includes('INT') || custSubType.includes('PRI')) {
      FormManager.readOnly('isuCd');
      FormManager.readOnly('clientTier');
      FormManager.readOnly('searchTerm');
    } else {
      FormManager.enable('isuCd');
      FormManager.enable('clientTier');
      FormManager.enable('searchTerm');
    }
  }
}

function validateDeptAttnBldg() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var division = FormManager.getActualValue('divn');
        var attn = FormManager.getActualValue('city2');
        var bldg = FormManager.getActualValue('bldg');
        var dept = FormManager.getActualValue('dept');
        if ((division != '' && (division == bldg || division == dept)) || (attn != '' && (attn == bldg || attn == dept))) {
          return new ValidationResult(null, false, 'Department_ext and Building_ext must contain unique information.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function setAddressDetailsForView() {
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  if (viewOnlyPage == 'true') {
    $('label[for="custNm1_view"]').text('Customer legal name');
    $('label[for="custNm2_view"]').text('Legal name continued');
    $('label[for="divn_view"]').text('Division/Department');
    $('label[for="city2_view"]').text('Attention To /Building/Floor/Office');
    $('label[for="addrTxt_view"]').text('Street Name And Number');
    $('label[for="bldg_view"]').text('Building_ext');
    $('label[for="dept_view"]').text('Department_ext');
  }
}

// CREATCMR-4293
function setCTCValues() {

  FormManager.removeValidator('clientTier', Validators.REQUIRED);

  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var reqType = FormManager.getActualValue('reqType');

  // Business Partner
  var custSubGrpForBusinessPartner = [ 'CHBUS', 'LIBUS' ];

  // Business Partner
  if (custSubGrpForBusinessPartner.includes(custSubGrp)) {
    FormManager.removeValidator('clientTier', Validators.REQUIRED);
    var isuCd = FormManager.getActualValue('isuCd');
    if (isuCd == '8B') {
      FormManager.setValue('clientTier', _pagemodel.clientTier == null ? '' : _pagemodel.clientTier);
      if (reqType == 'U') {
        FormManager.enable('clientTier');
      }
    }
  }

  // Internal
  var custSubGrpForInternal = [ 'CHINT', 'LIINT' ];

  // Internal
  if (custSubGrpForInternal.includes(custSubGrp)) {
    FormManager.removeValidator('clientTier', Validators.REQUIRED);
    var isuCd = FormManager.getActualValue('isuCd');
    if (isuCd == '21') {
      FormManager.setValue('clientTier', _pagemodel.clientTier == null ? '' : _pagemodel.clientTier);
      if (reqType == 'U') {
        FormManager.enable('clientTier');
      }
    }
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

function validateSortl() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqId = FormManager.getActualValue('reqId');
        var searchTerm = FormManager.getActualValue('searchTerm');
        var letterNumber = /^[0-9a-zA-Z]+$/;
        var qParams = {
          REQ_ID : reqId,
        };
        if (_importedSearchTerm != searchTerm) {
          console.log("validating Sortl..");
          if (searchTerm.length != 8) {
            return new ValidationResult(null, false, 'SORTL should be 8 characters long.');
          }

          if (!searchTerm.match(letterNumber)) {
            return new ValidationResult({
              id : 'searchTerm',
              type : 'text',
              name : 'searchTerm'
            }, false, 'SORTL should be alpha numeric.');
          }
        }

        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

var _importedSearchTerm = null;
function resetSortlValidator() {
  var reqId = FormManager.getActualValue('reqId');
  var reqType = FormManager.getActualValue('reqType');

  var qParams = {
    REQ_ID : reqId,
  };
  var result = cmr.query('GET.SEARCH_TERM_DATA_RDC', qParams);
  if (result != null) {
    _importedSearchTerm = result.ret1;
  }

  if (reqType == 'U' && (_importedSearchTerm == '' || _importedSearchTerm == null)) {
    console.log('Making Sortl optinal as it is empty in RDC');
    FormManager.resetValidations('searchTerm');
  }
}
// CREATCMR-6378
function retainVatValueAT() {
  var vat = FormManager.getActualValue('vat');
  var reqId = FormManager.getActualValue('reqId');
  var qParams = {
    REQ_ID : reqId,
  };
  if (vat == '' || vat == null || vat == undefined) {
    var result = cmr.query('ADDR.GET.VAT_REQID', qParams);
    var _vat = result.ret1;
    FormManager.setValue('vat', _vat);
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
//  GEOHandler.addAddrFunction(setMubotyOnPostalCodeIMS, GEOHandler.SWISS);

  GEOHandler.addAfterConfig(reqReasonOnChange, GEOHandler.SWISS);
  GEOHandler.addAfterConfig(addHandlersForSWISS, GEOHandler.SWISS);
  GEOHandler.addAfterConfig(addAfterConfigForSWISS, GEOHandler.SWISS);

  // GEOHandler.addAfterTemplateLoad(setCurrencyCd, GEOHandler.SWISS);
  GEOHandler.addAfterTemplateLoad(setCustClassCd, GEOHandler.SWISS);
  GEOHandler.addAfterTemplateLoad(setVatValidatorSWISS, GEOHandler.SWISS);
  GEOHandler.addAfterTemplateLoad(addAfterConfigForSWISS, GEOHandler.SWISS);
//  GEOHandler.addAfterTemplateLoad(setMubotyOnPostalCodeIMS, GEOHandler.SWISS);
  GEOHandler.addAfterConfig(defaultCapIndicator, GEOHandler.SWISS);
  GEOHandler.addAfterConfig(setAbbrvNmSwiss, GEOHandler.SWISS);

  GEOHandler.registerValidator(addHwMstrInstFlgValidator, GEOHandler.SWISS, null, true);
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
  GEOHandler.registerValidator(restrictDuplicateAddr, GEOHandler.SWISS, null, true);

  // new phase 2
  GEOHandler.addAfterConfig(setISUCTCOnIMSChange, GEOHandler.SWISS);
  GEOHandler.addAfterTemplateLoad(setISUCTCOnIMSChange, GEOHandler.SWISS);
  GEOHandler.addAfterConfig(lockIBMTabForSWISS, GEOHandler.SWISS);
  GEOHandler.addAfterTemplateLoad(lockIBMTabForSWISS, GEOHandler.SWISS);
  GEOHandler.registerValidator(validateDeptAttnBldg, GEOHandler.SWISS);
  GEOHandler.addAfterConfig(setAddressDetailsForView, GEOHandler.SWISS);

  // CREATCMR-4293
  GEOHandler.addAfterTemplateLoad(setCTCValues, GEOHandler.SWISS);
  GEOHandler.registerValidator(clientTierValidator, GEOHandler.SWISS, null, true);

  GEOHandler.addAfterConfig(resetVATValidationsForPayGo, GEOHandler.SWISS);
  GEOHandler.addAfterTemplateLoad(resetVATValidationsForPayGo, GEOHandler.SWISS);
  GEOHandler.registerValidator(validateSortl, GEOHandler.SWISS, null, true);
  GEOHandler.addAfterConfig(resetSortlValidator, GEOHandler.SWISS);
  GEOHandler.addAfterTemplateLoad(resetSortlValidator, GEOHandler.SWISS);
  GEOHandler.registerValidator(checkCmrUpdateBeforeImport, GEOHandler.SWISS, null, true);
  GEOHandler.registerValidator(validateSBOValuesForIsuCtc, GEOHandler.SWISS, null, true);
  
});
