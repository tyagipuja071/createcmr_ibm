/**
 * 
 */
/* Register IERP Javascript */
var _vatExemptHandler = null;
var _scenarioSubTypeHandler = null;
var _deClientTierHandler = null;
var _addrTypeCNHandler = [];
var _addrTypesForCN = [ 'ZS01', 'ZP01', 'ZI01', 'ZD01' ];
var _landCntryHandler = null;
var _isuHandler = null;
var _searchTermHandler = null;
// var _govTypeHandler = null;
var _isicHandlerCN = null;
var _inacCdHandler = null;
var CNHandler = {
  CN_NAME1_MAX_BYTE_LEN : 70,
  CN_NAME2_MAX_BYTE_LEN : 70,
  CN_ADDRTXT_MAX_BYTE_LEN : 70,
  CN_ADDRTXT2_MAX_BYTE_LEN : 70,

  lengthInUtf8Bytes : function(str) {
    var m = encodeURIComponent(str).match(/%[89ABab]/g);
    console.log(str + " >>> " + (str.length + (m ? m.length : 0)));
    return str.length + (m ? m.length : 0);
  },

  encode_utf8 : function(s) {
    return unescape(encodeURIComponent(s));
  },

  substr_utf8_bytes : function(str, startInBytes, lengthInBytes) {

    var resultStr = '';
    var startInChars = 0;

    for (bytePos = 0; bytePos < startInBytes; startInChars++) {
      ch = str.charCodeAt(startInChars);
      bytePos += (ch < 128) ? 1 : CNHandler.encode_utf8(str[startInChars]).length;
    }

    end = startInChars + lengthInBytes - 1;

    for (n = startInChars; startInChars <= end; n++) {
      // get numeric code of character (is >128 for multibyte character)
      // and decrease "end" for each byte of the character sequence
      ch = str.charCodeAt(n);
      end -= (ch < 128) ? 1 : CNHandler.encode_utf8(str[n]).length;

      resultStr += str[n];
    }

    return resultStr;
  },

  hasDoubleByte : function(str) {
    console.log(">> RUN hasDoubleByte");
    console.log(">> validate > " + str);
    for (var i = 0, n = str.length; i < n; i++) {
      if (str.charCodeAt(i) > 255) {
        return true;
      }
    }
    return false;
  }

};

function afterConfigForCN() {
  if (FormManager.getActualValue('isicCd') != 'undefined' && FormManager.getActualValue('isicCd') != '') {
    FormManager.readOnly('isicCd');
  }
  if (_isicHandlerCN == null) {
    _isicHandlerCN = dojo.connect(FormManager.getField('isicCd'), 'onChange', function(value) {
      setIsuOnIsic();
    });
  }
  
  if (_searchTermHandler == null) {
    _searchTermHandler = dojo.connect(FormManager.getField('searchTerm'), 'onChange', function(value) {
      console.log(">>> RUNNING SORTL HANDLER!!!!");
      if (!value) {
        return;
      }
      filterISUOnChange();
      setIsuOnIsic();
      setInacBySearchTerm();
      // addValidationForParentCompanyNo();
    });
  }

  if (_vatExemptHandler == null) {
    _vatExemptHandler = dojo.connect(FormManager.getField('vatExempt'), 'onClick', function(value) {
      setSocialCreditCdValidator();
    });
  }

// if (_govTypeHandler == null) {
// _govTypeHandler = dojo.connect(FormManager.getField('govType'), 'onClick',
// function(value) {
// addValidationForParentCompanyNo();
// });
// }

  if (FormManager.getActualValue('reqType') == 'U') {

    FormManager.hide('IbmDeptCostCenter', 'ibmDeptCostCenter');
    if (_pagemodel.userRole.toUpperCase() == "REQUESTER") {
      /*
       * Removing hiding custClass because it is setting it to null when saving
       * from requester view. For the time being we should set it to read only
       * for updates.
       */
      // FormManager.hide('ClassCode', 'custClass');
      FormManager.readOnly('custClass');
      FormManager.resetValidations('custClass');
      FormManager.hide('InterAddrKey', 'cnInterAddrKey');
      FormManager.resetValidations('cnInterAddrKey');
    } else {
      FormManager.readOnly('custClass');
    }
    FormManager.readOnly('isicCd');
    if(FormManager.getField('capInd').checked == true){
      FormManager.readOnly('subIndustryCd');
      FormManager.readOnly('searchTerm');
      FormManager.readOnly('isuCd');
      FormManager.readOnly('clientTier');
      FormManager.readOnly('inacType');
      FormManager.readOnly('inacCd');
      FormManager.readOnly('company');
    }
  }
  FormManager.show('DisableAutoProcessing', 'disableAutoProc');

  if (_landCntryHandler == null) {
    _landCntryHandler = dojo.connect(FormManager.getField('landCntry'), 'onChange', function(value) {
      showHideCityCN();
    });
  }

  if (_landCntryHandler && _landCntryHandler[0]) {
    _landCntryHandler[0].onChange();
  }

  if (_searchTermHandler && _searchTermHandler[0]) {
    _searchTermHandler[0].onChange();
  }
  
}

function setInacBySearchTerm() {
  if (FormManager.getActualValue('reqType') != 'C' || FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var searchTerm = FormManager.getActualValue('searchTerm');
  if ((searchTerm == '04687' || searchTerm == '04488' || searchTerm == '04630' || searchTerm == '04472' || searchTerm == '04474' || searchTerm == '04480' || searchTerm == '04484'
      || searchTerm == '04486' || searchTerm == '04491' || searchTerm == '04493' || searchTerm == '04495' || searchTerm == '04497' || searchTerm == '04499' || searchTerm == '04502'
      || searchTerm == '04629' || searchTerm == '04689' || searchTerm == '04489' || searchTerm == '04747' || searchTerm == '04748' || searchTerm == '04749')) {
    FormManager.addValidator('inacCd', Validators.REQUIRED, [ 'INAC/NAC Code' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('inacType', Validators.REQUIRED, [ 'INAC Type' ], 'MAIN_IBM_TAB');
    var qParams = {
      _qall : 'Y',
      ISSUING_CNTRY : cntry,
      CMT : '%' + searchTerm + '%'
    };
    var inacList = cmr.query('GET.INAC_BY_CLUSTER', qParams);
    if (inacList != null) {
      var inacTypeSelected ='';
      var arr =  inacList.map(inacList => inacList.ret1);
      inacTypeSelected  =  inacList.map(inacList => inacList.ret2);
      FormManager.limitDropdownValues(FormManager.getField('inacCd'), arr);
      if (inacList.length == 1) {
        FormManager.setValue('inacCd', arr[0]);
      }       
      if (inacType != '' && inacTypeSelected[0].includes(",I") && !inacTypeSelected[0].includes(',IN')) {
        FormManager.limitDropdownValues(FormManager.getField('inacType'), 'I');
        FormManager.setValue('inacType', 'I');
      } else if (inacType != '' && inacTypeSelected[0].includes(',N')) {
        FormManager.limitDropdownValues(FormManager.getField('inacType'), 'N');
        FormManager.setValue('inacType', 'N');
      } else if(inacType != '' && inacTypeSelected[0].includes(',IN')){
        FormManager.resetDropdownValues(FormManager.getField('inacType'));
        var value = FormManager.getField('inacType');
        var cmt = value + ','+ searchTerm +'%';
        var value = FormManager.getActualValue('inacType');
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
          console.log(value);
          if (value != null) {
            var inacCdValue = [];
            var qParams = {
              _qall : 'Y',
              ISSUING_CNTRY : cntry ,
              CMT : cmt ,
             };
            var results = cmr.query('GET.INAC_CD', qParams);
            if (results != null && results.length > 0) {
              for (var i = 0; i < results.length; i++) {
                inacCdValue.push(results[i].ret1);
              }
              FormManager.limitDropdownValues(FormManager.getField('inacCd'), inacCdValue);
              if (inacCdValue.length == 1) {
                FormManager.setValue('inacCd', inacCdValue[0]);
              }
            }
          }
      } else {
        FormManager.resetDropdownValues(FormManager.getField('inacType'));
      }
    }
    addSearchTerm04687Logic();
  } else {
    FormManager.resetDropdownValues(FormManager.getField('inacCd'));
    FormManager.resetDropdownValues(FormManager.getField('inacType'));
    FormManager.removeValidator('inacCd', Validators.REQUIRED);
    FormManager.removeValidator('inacType', Validators.REQUIRED);
    return;
  }
}

function setIsuOnIsic() {
  if (FormManager.getActualValue('reqType') != 'C' || FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  var searchTerm = FormManager.getActualValue('searchTerm');
  if (!(searchTerm == '04687' || searchTerm == '04488' || searchTerm == '04630' || searchTerm == '04472' || searchTerm == '04474' || searchTerm == '04480' || searchTerm == '04484'
      || searchTerm == '04486' || searchTerm == '04491' || searchTerm == '04493' || searchTerm == '04495' || searchTerm == '04497' || searchTerm == '04499' || searchTerm == '04502'
      || searchTerm == '04629' || searchTerm == '04689' || searchTerm == '04489' || searchTerm == '04747' || searchTerm == '04748' || searchTerm == '04749')) {
    return;
  }

  var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  var isicCd = FormManager.getActualValue('isicCd');

  var ISU = [];
  if (isicCd != '') {
    var qParams = {
      _qall : 'Y',
      ISSUING_CNTRY : cmrIssuingCntry,
      REP_TEAM_CD : '%' + isicCd + '%'
    };
    var results = cmr.query('GET.ISULIST.BYISIC', qParams);
    if (results != null) {
      for (var i = 0; i < results.length; i++) {
        ISU.push(results[i].ret1);
      }
      if (ISU != null) {
        FormManager.limitDropdownValues(FormManager.getField('isuCd'), ISU);
        if (ISU.length >= 1) {
          FormManager.setValue('isuCd', ISU[0]);
        }
      }
    }
    FormManager.readOnly('isicCd');
  }
}

function onInacTypeChange() {
  var searchTerm = FormManager.getActualValue('searchTerm');
  var reqType = null;
  reqType = FormManager.getActualValue('reqType');
  if (reqType == 'C') {
    if (_inacCdHandler == null) {
      _inacCdHandler = dojo.connect(FormManager.getField('inacType'), 'onChange', function(value) {
        var searchTerm = FormManager.getActualValue('searchTerm');
        var cmt = value + ','+ searchTerm +'%';
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
          console.log(value);
          if (value != null) {
            var inacCdValue = [];
            if(searchTerm == '04687' || searchTerm == '04488' || searchTerm == '04630' || searchTerm == '04472' || searchTerm == '04474' || searchTerm == '04480' || searchTerm == '04484'
              || searchTerm == '04486' || searchTerm == '04491' || searchTerm == '04493' || searchTerm == '04495' || searchTerm == '04497' || searchTerm == '04499' || searchTerm == '04502'
                || searchTerm == '04629' || searchTerm == '04689' || searchTerm == '04489' || searchTerm == '04747' || searchTerm == '04748' || searchTerm == '04749') {
              var qParams = {
              _qall : 'Y',
              ISSUING_CNTRY : cntry ,
              CMT : cmt ,
              };
            } 
            if(qParams != undefined){
              var results = cmr.query('GET.INAC_CD', qParams);
              if (results != null) {
                for (var i = 0; i < results.length; i++) {
                  inacCdValue.push(results[i].ret1);
                }
                FormManager.limitDropdownValues(FormManager.getField('inacCd'), inacCdValue);
                if (inacCdValue.length == 1) {
                  FormManager.setValue('inacCd', inacCdValue[0]);
                }
                addSearchTerm04687Logic();
              }
            }
          }
      });
    }
  }
}

function addSearchTerm04687Logic() {
  if (FormManager.getActualValue('searchTerm') == '04687'){
    if (FormManager.getActualValue('searchTerm') != _pagemodel.searchTerm) {
      FormManager.setValue('inacCd', 'XXXX');
      FormManager.setValue('inacType', 'I');
    }
  }
}

function autoSetIBMDeptCostCenter() {
  var _custSubGrp = FormManager.getActualValue('custSubGrp');
  var _custClass = FormManager.getActualValue('custClass');
  if (FormManager.getActualValue('cmrIssuingCntry') == '641' && FormManager.getActualValue('reqType') == 'C') {
    if (_custSubGrp != 'undefined' && _custClass != 'undefined' && _custClass != '') {
      if (_custSubGrp == 'INTAM' || _custSubGrp == 'INTSO' || _custSubGrp == 'INTIN') {
        FormManager.show('IbmDeptCostCenter', 'ibmDeptCostCenter');
        if (_custClass == '81') {
          FormManager.addValidator('ibmDeptCostCenter', Validators.REQUIRED, [ 'IbmDeptCostCenter' ], 'MAIN_IBM_TAB');
        } else if (_custClass == '85') {
          FormManager.resetValidations('ibmDeptCostCenter');
        }
      } else {
        FormManager.hide('IbmDeptCostCenter', 'ibmDeptCostCenter');
      }
    } else {
      FormManager.hide('IbmDeptCostCenter', 'ibmDeptCostCenter');
    }
  } else {
    FormManager.hide('IbmDeptCostCenter', 'ibmDeptCostCenter');
  }

}

function defaultCapIndicator() {
  if (FormManager.getActualValue('reqType') == 'C') {
    FormManager.getField('capInd').checked = true;
    FormManager.readOnly('capInd');
  }
}

function defaultGovernmentIndicator(){
  FormManager.getField('govType').checked = true;
  FormManager.readOnly('govType');
}

function disableVatExemptForScenarios() {
  var _custSubGrp = FormManager.getActualValue('custSubGrp');
  // setValuesForScenarios();
  if (_custSubGrp != 'undefined' && _custSubGrp != '') {
    if (_custSubGrp == 'INTER'  ||  _custSubGrp == 'PRIV' || _custSubGrp == 'CROSS') {
      FormManager.disable('vatExempt');
      FormManager.removeValidator('busnType', Validators.REQUIRED);
    } else {
      FormManager.enable('vatExempt');
      setSocialCreditCdValidator();
    }
  }
}

function setSocialCreditCdValidator(){
  if (dojo.byId('vatExempt').checked ) {
    console.log(">>> process Social Credit Code remove * >> ");
    FormManager.resetValidations('busnType');
  } else {
    console.log(">>> process Social Credit Code add * >> ");
    FormManager.addValidator('busnType', Validators.REQUIRED, [ 'Social Credit Code' ], 'MAIN_CUST_TAB');
  }
}

function setPrivacyIndcReqdForProc() {
  if (_pagemodel.userRole.toUpperCase() == "PROCESSOR" && FormManager.getActualValue('reqType') == 'C') {
    FormManager.addValidator('privIndc', Validators.REQUIRED, [ 'PrivacyIndc' ], 'MAIN_CUST_TAB');
  }
}

function filterISUOnChange() {
  console.log(">>> RUNNING filterISUOnChange!!");
  var searchTerm = FormManager.getActualValue('searchTerm');
  var searchTermParams = {
    SORTL : searchTerm,
    KATR6 : '641'
  };
  var isuCdResult = cmr.query('GET.MAPPED_ISU_BY_SORTL', searchTermParams);

  if (isuCdResult != null) {
    FormManager.resetDropdownValues(FormManager.getField('isuCd'));
    FormManager.setValue('isuCd', isuCdResult.ret2);
  }

  searchTermParams['_qall'] = 'Y';
  // FormManager.readOnly('isuCd');
  // var isuCd = isuCdResult.ret2;
  var clientTier = null;

  var mappedCtc = cmr.query('GET.MAPPED_CTC_BY_ISU', searchTermParams);
  if (mappedCtc && mappedCtc.length > 0) {
    clientTier = [];
    mappedCtc.forEach(function(ctc, index) {
      clientTier.push(ctc.ret1);
    });
  }

  // if (isuCd == "04") {
  // // filter the drop down
  // clientTier = [ 'BL', '7' ];
  // } else if (isuCd == "21") {
  // // filter the drop down
  // clientTier = [ 'Z' ];
  // } else if (isuCd == "32") {
  // // filter the drop down
  // if (searchTerm == '06757' || searchTerm == '06978' || searchTerm == '06979'
  // || searchTerm == '06981' || searchTerm == '06967' || searchTerm == '06968'
  // || searchTerm == '06980') {
  // clientTier = [ 'N' ];
  // } else {
  // clientTier = [ 'T', 'S' ];
  // }
  // } else if (isuCd == "34") {
  // // filter the drop down
  // if (searchTerm == '04617' || searchTerm == '04615' || searchTerm == '04613'
  // || searchTerm == '05485' || searchTerm == '05486' || searchTerm == '05487'
  // || searchTerm == '04603'
  // || searchTerm == '04605' || searchTerm == '04608' || searchTerm == '04609'
  // || searchTerm == '05483' || searchTerm == '05484') {
  // clientTier = [ 'A' ];
  // } else if (searchTerm == '06968') {
  // clientTier = [ 'V' ];
  // } else {
  // clientTier = [ 'V' ];
  // }
  // } else if (isuCd == "3T") {
  // // filter the drop down
  // clientTier = [ 'BL', '7' ];
  // } else if (isuCd == "5E") {
  // // filter the drop down
  // clientTier = [ 'BL', '7' ];
  // } else if (isuCd == "8B") {
  // // filter the drop down
  // clientTier = [ 'Z' ];
  // }

  if (clientTier != null) {
    FormManager.limitDropdownValues(FormManager.getField('clientTier'), clientTier);
    FormManager.enable('clientTier');
    if (clientTier.length == 1) {
      FormManager.setValue('clientTier', clientTier[0]);
      if (isuCd == "8B" && FormManager.getActualValue('custSubGrp') == 'BUSPR') {
        FormManager.readOnly('clientTier');
      }
      // FormManager.readOnly('clientTier');
    }
  } else {
    FormManager.resetDropdownValues(FormManager.getField('isuCd'));
  }

  if (_pagemodel.userRole.toUpperCase() == "PROCESSOR") {
    console.log("Enabling isuCd for PROCESSOR...");
    FormManager.enable('isuCd');
  }

  if (_pagemodel.userRole.toUpperCase() == "REQUESTER") {
    console.log("Disabling isuCd for REQUESTER...");
    FormManager.readOnly('isuCd');
  }

}

function filterClientTierOnChange() {
  console.log(">>> RUNNING filterClientTierOnChange!!");
  var isuCd = FormManager.getActualValue('isuCd');
  var searchTerm = FormManager.getActualValue('searchTerm');
  var clientTier = null;

  if (isuCd == "04") {
    // filter the drop down
    clientTier = [ 'BLANK', '7' ];
  } else if (isuCd == "21") {
    // filter the drop down
    clientTier = [ 'Z' ];
  } else if (isuCd == "32") {
    // filter the drop down
    if (searchTerm == '06757' || searchTerm == '06978' || searchTerm == '06979' || searchTerm == '06981' || searchTerm == '06967' || searchTerm == '06968' || searchTerm == '06980') {
      clientTier = [ 'N' ];
    } else {
      clientTier = [ 'T', 'S' ];
    }
  } else if (isuCd == "34") {
    // filter the drop down
    if (searchTerm == '04617' || searchTerm == '04615' || searchTerm == '04613' || searchTerm == '05485' || searchTerm == '05486' || searchTerm == '05487') {
      clientTier = [ 'A' ];
    } else if (searchTerm == '06968') {
      clientTier = [ 'V' ];
    } else {
      clientTier = [ 'V', '6' ];
    }
  } else if (isuCd == "3T") {
    // filter the drop down
    clientTier = [ 'BLANK', '7' ];
  } else if (isuCd == "5E") {
    // filter the drop down
    clientTier = [ 'BLANK', '7' ];
  } else if (isuCd == "8B") {
    // filter the drop down
    clientTier = [ 'Z' ];
  }

  if (clientTier != null) {
    FormManager.limitDropdownValues(FormManager.getField('clientTier'), clientTier);
    FormManager.enable('clientTier');
    if (clientTier.length == 1) {
      FormManager.setValue('clientTier', clientTier[0]);
      // FormManager.readOnly('clientTier');
    }
  } else {
    FormManager.resetDropdownValues(FormManager.getField('isuCd'));
  }

  if (FormManager.getField('searchTerm') != 'undefined' && FormManager.getField('searchTerm') != '') {
    FormManager.readOnly('isuCd');
  } else {
    if (_pagemodel.userRole.toUpperCase() == "PROCESSOR") {
      console.log("Enabling isuCd for PROCESSOR...");
      FormManager.enable('isuCd');
    }

    if (_pagemodel.userRole.toUpperCase() == "REQUESTER") {
      console.log("Disabling isuCd for REQUESTER...");
      FormManager.readOnly('isuCd');
    }
  }
}

function limitClientTierValuesOnCreate() {
  var reqType = null;
  reqType = FormManager.getActualValue('reqType');
  if (reqType != 'C') {
    return;
  }
  var _custSubGrp = FormManager.getActualValue('custSubGrp');
  if (_custSubGrp != 'undefined' && _custSubGrp != '') {
    if (_custSubGrp == 'COMME' || _custSubGrp == 'BROKR' || _custSubGrp == 'GOVMT' || _custSubGrp == 'SENSI') {
      var clientTierValues = [ 'A', 'B', 'V', 'Z', '6', '7', 'T', 'S', 'C', 'N' ];
      if (clientTierValues != null) {
        FormManager.limitDropdownValues(FormManager.getField('clientTier'), clientTierValues);
      } else {
        FormManager.resetDropdownValues(FormManager.getField('clientTier'));
      }
    }
  }
}

function limitClientTierValuesOnUpdate() {
  var reqType = null;
  reqType = FormManager.getActualValue('reqType');
  if (reqType != 'U') {
    return;
  }
  var clientTierValues = [ 'A', 'B', 'V', 'Z', '6', '7', 'T', 'S', 'C', 'N' ];
  if (clientTierValues != null) {
    FormManager.limitDropdownValues(FormManager.getField('clientTier'), clientTierValues);
  } else {
    FormManager.resetDropdownValues(FormManager.getField('clientTier'));
  }

}
// Defect 1370022: By Mukesh
function canRemoveAddress(value, rowIndex, grid) {
  console.log("Remove address button..");
  var rowData = grid.getItem(0);
  if (rowData == null) {
    return '';
  }
  var rowData = grid.getItem(rowIndex);
  var importInd = rowData.importInd;

  var reqType = FormManager.getActualValue('reqType');
  if (reqType == 'U') {
    if (importInd == 'Y') {
      return false;
    } else {
      return true;
    }
  } else {
    var addrType = rowData.addrType;
    if (addrType == 'ZS01') {
      return false;
    }
    return true;
  }
}
function canUpdateAddress(value, rowIndex, grid) {
  return true;
}
function canCopyAddress(value, rowIndex, grid) {
  return true;
}
function ADDRESS_GRID_showCheck(value, rowIndex, grid) {
  console.log(value + ' - ' + rowIndex);
  var rowData = grid.getItem(0);
  if (rowData == null) {
    return '';
  }
  rowData = grid.getItem(rowIndex);
  var importInd = rowData.importInd;
  var reqType = FormManager.getActualValue('reqType');
  if (reqType == 'U') {
    if (importInd == 'Y') {
      return false;
    } else {
      return true;
    }
  } else {
    var addrType = rowData.addrType;
    if (addrType == 'ZS01') {
      return false;
    }
    return true;
  }
}

function setValuesForScenarios() {
  var _custSubGrp = FormManager.getActualValue('custSubGrp');

  if (_pagemodel.userRole.toUpperCase() == "REQUESTER") {
    FormManager.hide('ClassCode', 'custClass');
    FormManager.resetValidations('custClass');
    FormManager.hide('InterAddrKey', 'cnInterAddrKey');
    FormManager.resetValidations('cnInterAddrKey');
  }

  if (FormManager.getActualValue('reqType') == 'C' && _custSubGrp != 'undefined' && _custSubGrp != '') {
    if (FormManager.getActualValue('isicCd') != 'undefined' && FormManager.getActualValue('isicCd') != '') {
      FormManager.readOnly('isicCd');
    }
    if (_custSubGrp == 'INTER') {
      FormManager.show('ClassCode', 'custClass');
      var field = FormManager.getField('custClass');
      FormManager.limitDropdownValues(field, [ '81', '85' ]);
    }
    if (_pagemodel.userRole.toUpperCase() == "REQUESTER") {
      // if (_custSubGrp == 'BUSPR') {
      // FormManager.hide('SearchTerm', 'searchTerm');
      // FormManager.setValue('searchTerm', '');

      // FormManager.setValue('clientTier', '');
      // FormManager.readOnly('clientTier');
      // FormManager.resetValidations('clientTier');
      // } else {
      // FormManager.show('SearchTerm', 'searchTerm');
      // }
      if (_custSubGrp == 'INTER') {
        FormManager.resetValidations('searchTerm');
        // FormManager.enable('clientTier');
        FormManager.readOnly('searchTerm');
        FormManager.resetValidations('isicCd');
      }
      if (_custSubGrp == 'NRML' || _custSubGrp == 'ECOSY' || _custSubGrp == 'INTER' || _custSubGrp == 'AQSTN'|| _custSubGrp == 'PRIV' || _custSubGrp == 'EMBSA' || _custSubGrp == 'CROSS') {
        FormManager.hide('PPSCEID', 'ppsceid');
        FormManager.hide('MembLevel', 'memLvl');
        FormManager.hide('BPRelationType', 'bpRelType');
      } else {
        FormManager.show('PPSCEID', 'ppsceid');
        FormManager.show('MembLevel', 'memLvl');
        FormManager.show('BPRelationType', 'bpRelType');
      }

      if (_custSubGrp == 'AQSTN'|| _custSubGrp == 'PRIV' || _custSubGrp == 'CROSS' || _custSubGrp == 'INTER') {
        FormManager.resetValidations('cnCustName1');
        FormManager.resetValidations('cnAddrTxt');
        FormManager.resetValidations('cnCity');
      }
    }

    if (_pagemodel.userRole.toUpperCase() == "PROCESSOR") {
      if (_custSubGrp == 'NRML' || _custSubGrp == 'ECOSY' || _custSubGrp == 'AQSTN'|| _custSubGrp == 'PRIV' || _custSubGrp == 'EMBSA' || _custSubGrp == 'CROSS' || _custSubGrp == 'MRKT' || _custSubGrp == 'BLUMX') {
        FormManager.setValue('custClass', '11');
        FormManager.readOnly('custClass');
      }
      if (_custSubGrp == 'BUSPR') {
        FormManager.setValue('custClass', '45');
        FormManager.readOnly('custClass');
      }
      if (_custSubGrp == 'NRML' || _custSubGrp == 'ECOSY' || _custSubGrp == 'BUSPR' || _custSubGrp == 'INTER' || _custSubGrp == 'EMBSA' || _custSubGrp == 'BLUMX' || _custSubGrp == 'MRKT') {
        FormManager.setValue('cnInterAddrKey', '6');
        FormManager.addValidator('cnInterAddrKey', Validators.REQUIRED, [ 'InterAddrKey' ], '');
        FormManager.readOnly('cnInterAddrKey');
      } else {
        FormManager.resetValidations('cnInterAddrKey');
      }
    }
  }
}

function hideContactInfoFields() {
  var addrType = FormManager.getActualValue('addrType');

  if (addrType != '' && addrType == 'ZS01') {
    // FormManager.hide('CustomerCntPhone2', 'cnCustContPhone2');
    FormManager.show('CustomerCntJobTitle', 'cnCustContJobTitle');
    FormManager.show('ChinaCustomerCntName', 'cnCustContNm');
    FormManager.addValidator('custPhone', Validators.REQUIRED, [ "Phone#" ], null);
    FormManager.addValidator('cnCustContJobTitle', Validators.REQUIRED, [ "Customer Contact's Job Title" ], null);
    FormManager.addValidator('cnCustContNm', Validators.REQUIRED, [ "Customer Contact's Name (include salutation)" ], null);
  } else {
    // FormManager.hide('CustomerCntPhone2', 'cnCustContPhone2');
    FormManager.show('CustomerCntJobTitle', 'cnCustContJobTitle');
    FormManager.show('ChinaCustomerCntName', 'cnCustContNm');

    FormManager.resetValidations('cnCustContJobTitle');
    FormManager.resetValidations('cnCustContNm');
    FormManager.resetValidations('custPhone');
  }
}

function autoSetAddrFieldsForCN() {
  if (cmr.addressMode == 'newAddress' || cmr.addressMode == 'copyAddress' || cmr.addressMode =='updateAddress' ) {
    for (var i = 0; i < _addrTypesForCN.length; i++) {
      _addrTypeCNHandler[i] = null;
      if (_addrTypeCNHandler[i] == null) {
        _addrTypeCNHandler[i] = dojo.connect(FormManager.getField('addrType_' + _addrTypesForCN[i]), 'onClick', function(value) {
          hideContactInfoFields();
        });
      }
    }

  }

  if (cmr.addressMode == 'updateAddress') {
    if (FormManager.getActualValue('addrType') == 'ZS01') {
      // FormManager.hide('CustomerCntPhone2', 'cnCustContPhone2');
      FormManager.show('CustomerCntJobTitle', 'cnCustContJobTitle');
      FormManager.show('ChinaCustomerCntName', 'cnCustContNm');
      FormManager.addValidator('custPhone', Validators.REQUIRED, [ "Phone#" ], null);
      FormManager.addValidator('cnCustContJobTitle', Validators.REQUIRED, [ "Customer Contact's Job Title" ], null);
      FormManager.addValidator('cnCustContNm', Validators.REQUIRED, [ "Customer Contact's Name (include salutation)" ], null);
    } else {
      // FormManager.hide('CustomerCntPhone2', 'cnCustContPhone2');
      FormManager.show('CustomerCntJobTitle', 'cnCustContJobTitle');
      FormManager.show('ChinaCustomerCntName', 'cnCustContNm');

      FormManager.resetValidations('cnCustContJobTitle');
      FormManager.resetValidations('cnCustContNm');
      FormManager.resetValidations('custPhone');
    }
  }
}

function setChinaChecklistStatus() {
  console.log('validating china checklist..');
  var checklist = dojo.query('table.checklist');
  document.getElementById("checklistStatus").innerHTML = "Not Done";
  var reqId = FormManager.getActualValue('reqId');
  var questions = checklist.query('input[type="radio"]');

  if (reqId != null && reqId.length > 0 && reqId != 0) {
    if (questions.length > 0) {
      var noOfQuestions = questions.length / 2;
      var checkCount = 0;
      for (var i = 0; i < questions.length; i++) {
        if (questions[i].checked) {
          checkCount++;
        }
      }
      if (noOfQuestions != checkCount) {
        document.getElementById("checklistStatus").innerHTML = "Incomplete";
        FormManager.setValue('checklistStatus', "Incomplete");
      } else {
        document.getElementById("checklistStatus").innerHTML = "Complete";
        FormManager.setValue('checklistStatus', "Complete");
      }
    } else {
      document.getElementById("checklistStatus").innerHTML = "Complete";
      FormManager.setValue('checklistStatus', "Complete");
    }
  }
}

function showHideCityCN() {
  console.log(">>> showHideCityCN for CN");
  var landCntryVal = FormManager.getActualValue('landCntry');
  var _custSubGrp = FormManager.getActualValue('custSubGrp');

  if (landCntryVal != 'CN' && landCntryVal != "") {
    FormManager.hide('DropDownCity', 'dropdowncity1');
    FormManager.resetValidations('dropdowncity1');
    FormManager.show('City1', 'city1');
    FormManager.addValidator('city1', Validators.REQUIRED, [ 'City' ], null);
    FormManager.resetValidations('cnCity');
    FormManager.resetValidations('cnAddrTxt');
    FormManager.resetValidations('cnCustName1');
    FormManager.resetValidations('stateProv');
    //
    FormManager.resetValidations('custPhone');
    FormManager.resetValidations('cnCustContJobTitle');
    FormManager.resetValidations('cnCustContNm');
  } else {
    FormManager.show('DropDownCity', 'dropdowncity1');
    FormManager.addValidator('dropdowncity1', Validators.REQUIRED, [ 'City' ], null);
    FormManager.hide('City1', 'city1');
    FormManager.resetValidations('city1');
    // if (_custSubGrp != 'undefined' && _custSubGrp != '' && (_custSubGrp ==
    // 'AQSTN' || _custSubGrp == 'CROSS') &&
    // FormManager.getActualValue('reqType') == 'C') {
    if (_custSubGrp != 'undefined' && _custSubGrp != '' && _custSubGrp == 'CROSS' && FormManager.getActualValue('reqType') == 'C') {
      FormManager.resetValidations('cnCustName1');
      FormManager.resetValidations('cnAddrTxt');
      FormManager.resetValidations('cnCity');
      FormManager.resetValidations('stateProv');
    } else {
      if (_custSubGrp != 'undefined' && _custSubGrp != '' && (_custSubGrp != 'PRIV' && _custSubGrp != 'INTER' && _custSubGrp != 'AQSTN')) {
        FormManager.addValidator('cnCity', Validators.REQUIRED, [ 'City Chinese' ], null);
        FormManager.addValidator('cnAddrTxt', Validators.REQUIRED, [ 'Street Address Chinese' ], null);
        FormManager.addValidator('cnCustName1', Validators.REQUIRED, [ 'Customer Name Chinese' ], null);
      }
      FormManager.addValidator('stateProv', Validators.REQUIRED, [ 'State/Province' ], null);
      //
      var addrType = FormManager.getActualValue('addrType');
      if (addrType != '' && addrType == 'ZS01') {
        FormManager.addValidator('custPhone', Validators.REQUIRED, [ "Phone#" ], null);
        FormManager.addValidator('cnCustContJobTitle', Validators.REQUIRED, [ "Customer Contact's Job Title" ], null);
        FormManager.addValidator('cnCustContNm', Validators.REQUIRED, [ "Customer Contact's Name (include salutation)" ], null);
      }
    }
  }
}

function addMandatoryOnlyForZS01CN(){
  var addrType = FormManager.getActualValue('addrType');
  if (addrType != '' && addrType == 'ZS01') {
    FormManager.addValidator('custPhone', Validators.REQUIRED, [ "Phone#" ], null);
    FormManager.addValidator('cnCustContJobTitle', Validators.REQUIRED, [ "Customer Contact's Job Title" ], null);
    FormManager.addValidator('cnCustContNm', Validators.REQUIRED, [ "Customer Contact's Name (include salutation)" ], null);
  } else {
    FormManager.removeValidator('custPhone', Validators.REQUIRED);
    FormManager.removeValidator('cnCustContJobTitle', Validators.REQUIRED);
    FormManager.removeValidator('cnCustContNm', Validators.REQUIRED);
  }
}
// DENNIS: COMMENTED BECAUSE OF SCRIPT RUN TIME ISSUES
// To Validate Date
// function dateValidator() {
//
// if (!isValidDate())
// /*
// * FormManager.addValidator('dateValidator', Validators.REQUIRED, [ 'Invalid
// * Date Format' ], null);
// */
// return new ValidationResult(input, false, 'Invalid Date Format');
// else {
// return new ValidationResult(input, true);
// }
// }
//
// function isValidDate() {
//
// var dateString = FormManager.getActualValue('exportCodesTDODate');
// if (dateString.length > 8)
// return false;
//
// if (!/^\d{8}$/.test(dateString))
// return false;
//
// var year = parseInt(dateString.substr(0, 4), 10);
// var month = parseInt(dateString.substr(4, 2), 10);
// var day = parseInt(dateString.substr(6, 2), 10);
//
// var currentTime = new Date();
// var cyear = currentTime.getFullYear();
// // Check the ranges of month and year
// if (year > cyear || year == 0 || month == 0 || month > 12)
// return false;
// if (month == 2) {
// if (year % 400 == 0 || (year % 100 != 0 && year % 4 == 0)) {
// if (day == 0 || day > 29)
// return false;
// } else {
// if (day == 0 || day > 28)
// return false;
// }
// }
// if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 ||
// month == 10 || month == 12) {
// if (day == 0 || day > 31)
// return false;
// }
// if (month == 4 || month == 6 || month == 9 || month == 11) {
// if (day == 0 || day > 30)
// return false;
// }
// return true;
// };
function isValidDate() {
  console.log('>>> Validating date');
  var dateString = FormManager.getActualValue('bioChemMissleMfg');
  if (dateString == '' || dateString == null)
    return true;
  if (dateString.length > 8)
    return false;

  if (!/^\d{8}$/.test(dateString))
    return false;

  var year = parseInt(dateString.substr(0, 4), 10);
  var month = parseInt(dateString.substr(4, 2), 10);
  var day = parseInt(dateString.substr(6, 2), 10);

  var currentTime = new Date();
  var cyear = currentTime.getFullYear();
  // Check the ranges of month and year
  if (year > cyear || year == 0 || month == 0 || month > 12)
    return false;
  if (month == 2) {
    if (year % 400 == 0 || (year % 100 != 0 && year % 4 == 0)) {
      if (day == 0 || day > 29)
        return false;
    } else {
      if (day == 0 || day > 28)
        return false;
    }
  }
  if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) {
    if (day == 0 || day > 31)
      return false;
  }
  if (month == 4 || month == 6 || month == 9 || month == 11) {
    if (day == 0 || day > 30)
      return false;
  }
  return true;
}
function addDateValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {

        if (!isValidDate())
          return new ValidationResult(null, false, 'Invalid Date Format');
        else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');

}

function addFastPassAttachmentValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var custSubType = FormManager.getActualValue('custSubGrp');
        if (typeof (_pagemodel) != 'undefined') {
          if (custSubType == 'EMBSA') {
            var id = FormManager.getActualValue('reqId');
            var ret = cmr.query('CHECK_FASTPASS_ATTACHMENT', {
              ID : id
            });

            if (ret == null || ret.ret1 == null) {
              return new ValidationResult(null, false, 'Fastpass screenshot has not been attached to the request. This is required since this is an Embedded Solution Agreement (ESA) request.');
            } else {
              return new ValidationResult(null, true);
            }
          } else {
            return new ValidationResult(null, true);
          }
        }
      }
    };
  })(), 'MAIN_ATTACH_TAB', 'frmCMR');
}

function setTDOFlagToYesValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var tdoFlag = FormManager.getActualValue('icmsInd');
        var overallStatus = FormManager.getActualValue('overallStatus');
        if (typeof (_pagemodel) != 'undefined') {
          var id = FormManager.getActualValue('reqId');
          var ret = cmr.query('GET_REQUEST_APPROVAL', {
            REQ_ID : id
          });

          if ((ret == null || ret.ret1 == null)) {
            return new ValidationResult(null, true);
          } else {
            if (tdoFlag != 'Y' && overallStatus == 'Processing Validation') {
              return new ValidationResult(null, false, 'TDO Indicator is not set to \'Yes\'. This request is conditionally approved and requires that TDO Indicator is set to \'Yes\'.');
            } else {
              return new ValidationResult(null, true);
            }
          }
        }
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}
function hideTDOFields() {
  if (_pagemodel.userRole.toUpperCase() == "REQUESTER") {
    FormManager.hide('ExportCodesCountry', 'custAcctType');
    FormManager.hide('ExportCodesTDOdate', 'bioChemMissleMfg');
    FormManager.hide('ExportCodesTDOIndicator', 'icmsInd');
  }
}

function setReadOnlyFields() {
  if (_pagemodel.userRole.toUpperCase() == "PROCESSOR") {
    console.log("Enabling isuCd for PROCESSOR...");
    FormManager.enable('isuCd');
  }

  if (_pagemodel.userRole.toUpperCase() == "REQUESTER") {
    console.log("Disabling isuCd for REQUESTER...");
    FormManager.readOnly('isuCd');
  }

}

function addSoltToAddressValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
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

function addPRIVCustNameValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
         var _custSubGrp = FormManager.getActualValue('custSubGrp');
         var englishName1 = FormManager.getActualValue('custNm1');
         var englishName2 = FormManager.getActualValue('custNm2');
         if (_custSubGrp != 'undefined' && _custSubGrp != '' && _custSubGrp == 'PRIV') {
           if (englishName1 != 'undefined' && englishName1 != ''){
             englishName1 = englishName1.toUpperCase();
             if (englishName1.indexOf("PRIVATE LIMITED") < 0 && englishName1.indexOf("COMPANY") < 0 && englishName1.indexOf("CORPORATION") < 0  && englishName1.indexOf("INCORPORATE") < 0 && englishName1.indexOf("ORGANIZATION") < 0 && englishName1.indexOf("LIMITED") < 0 && englishName1.indexOf("PVT LTD") < 0 && englishName1.indexOf("CO., LTD.") < 0 && englishName1.indexOf("LTD") < 0 && englishName1.indexOf("LTD.") < 0 && englishName1.indexOf("COM LTD") < 0){
               console.log("Customer Name English for Private Person validate is successful...");
             } else {
               return new ValidationResult(null, false, "Customer Name English can't contain 'Private Limited', 'Company', 'Corporation', 'incorporate', 'organization', 'Pvt Ltd','Limited','Co., Ltd.', 'ltd', 'com ltd' for Scenario Privte Person");
             }
           }
           if (englishName2 != 'undefined' && englishName2 != ''){
             englishName2 = englishName2.toUpperCase();
             if (englishName2.indexOf("PRIVATE LIMITED") < 0 && englishName2.indexOf("COMPANY") < 0 && englishName2.indexOf("CORPORATION") < 0  && englishName2.indexOf("INCORPORATE") < 0 && englishName2.indexOf("ORGANIZATION") < 0 && englishName2.indexOf("LIMITED") < 0 && englishName2.indexOf("PVT LTD") < 0 && englishName2.indexOf("CO.,LTD.") < 0 && englishName2.indexOf("LTD") < 0 && englishName2.indexOf("LTD.") < 0 && englishName2.indexOf("COM LTD") < 0 ){
               console.log("Customer Name Con't English for Private Person validate is successful...");
               return new ValidationResult(null, true);
             } else {
               return new ValidationResult(null, false, "Customer Name Con't English can't contain 'Private Limited', 'Company', 'Corporation', 'incorporate', 'organization', 'Pvt Ltd','Limited','Co., Ltd.', 'ltd', 'com ltd' for Scenario Privte Person");
           }
         }
       }
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function addPRIVCustNameSFPValidator(){
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var action = FormManager.getActualValue('yourAction');
        if (action == 'SFP' && CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount) {
          var record = null;
          for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
             record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
             var _custSubGrp = FormManager.getActualValue('custSubGrp');
             var englishName1 = record.custNm1[0];
             var englishName2 = record.custNm2[0];
             if (_custSubGrp != 'undefined' && _custSubGrp != '' && _custSubGrp == 'PRIV') {
               if (englishName1 != 'undefined' && englishName1 != null && englishName1 != ''){
                 englishName1 = englishName1.toUpperCase();
                 if (englishName1.indexOf("PRIVATE LIMITED") < 0 && englishName1.indexOf("COMPANY") < 0 && englishName1.indexOf("CORPORATION") < 0  && englishName1.indexOf("INCORPORATE") < 0 && englishName1.indexOf("ORGANIZATION") < 0 && englishName1.indexOf("LIMITED") < 0 && englishName1.indexOf("PVT LTD") < 0 && englishName1.indexOf("CO., LTD.") < 0 && englishName1.indexOf("LTD") < 0 && englishName1.indexOf("LTD.") < 0 && englishName1.indexOf("COM LTD") < 0){
                   console.log("Customer Name English for Private Person validate is successful...");
                 } else {
                   return new ValidationResult(null, false, "Customer Name English can't contain 'Private Limited', 'Company', 'Corporation', 'incorporate', 'organization', 'Pvt Ltd','Limited','Co., Ltd.', 'ltd', 'com ltd' for Scenario Privte Person");
                 }
               }
               if (englishName2 != 'undefined' && englishName2 != null && englishName2 != ''){
                 englishName2 = englishName2.toUpperCase();
                 if (englishName2.indexOf("PRIVATE LIMITED") < 0 && englishName2.indexOf("COMPANY") < 0 && englishName2.indexOf("CORPORATION") < 0  && englishName2.indexOf("INCORPORATE") < 0 && englishName2.indexOf("ORGANIZATION") < 0 && englishName2.indexOf("LIMITED") < 0 && englishName2.indexOf("PVT LTD") < 0 && englishName2.indexOf("CO.,LTD.") < 0 && englishName2.indexOf("LTD") < 0 && englishName2.indexOf("LTD.") < 0 && englishName2.indexOf("COM LTD") < 0 ){
                   console.log("Customer Name Con't English for Private Person validate is successful...");
                   return new ValidationResult(null, true);
                 } else {
                   return new ValidationResult(null, false, "Customer Name Con't English can't contain 'Private Limited', 'Company', 'Corporation', 'incorporate', 'organization', 'Pvt Ltd','Limited','Co., Ltd.', 'ltd', 'com ltd' for Scenario Privte Person");
               }
             }
           }
          }
        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function addContactInfoValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var custSubType = FormManager.getActualValue('custSubGrp');
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0 && FormManager.getActualValue('reqType') == 'C' && (custSubType == 'EMBSA' || custSubType == 'NRML' || custSubType == 'ECOSY')) {
          var record = null;
          var type = null;

          var custPhone = null;
          var custContJobTitle = null;
          var custContNm = null;
          for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
            record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
            if (record == null && _allAddressData != null && _allAddressData[i] != null) {
              record = _allAddressData[i];
            }
            type = record.addrType;
            custPhone = record.custPhone;
            custContJobTitle = record.cnCustContJobTitle;
            custContNm = record.cnCustContNm;
            if (typeof (type) == 'object') {
              type = type[0];
            }
            if (typeof (custPhone) == 'object') {
              custPhone = custPhone[0];
            }
            if (typeof (custContJobTitle) == 'object') {
              custContJobTitle = custContJobTitle[0];
            }
            if (typeof (custContNm) == 'object') {
              custContNm = custContNm[0];
            }

            if (type == 'ZS01' && (custPhone == null || custContJobTitle == null || custContNm == null)) {
              return new ValidationResult(null, false, "Phone # is required, Customer Contact's job title is required, Customer Contact's Name(include salutation )is required.");
            }
          }
        }
        return new ValidationResult(null, true);

      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');

}

function addCityRequiredOnUpdateValidatorAddrList() {
  console.log("running addCityRequiredOnUpdateValidatorAddrList . . .");
  FormManager
      .addFormValidator(
          (function() {
            return {
              validate : function() {
                var reqType = FormManager.getActualValue('reqType');
                if (typeof (CmrGrid.GRIDS.ADDRESS_GRID_GRID) != 'undefined' && CmrGrid.GRIDS.ADDRESS_GRID_GRID != null) {
                  var addressStore = CmrGrid.GRIDS.ADDRESS_GRID_GRID.store, addressItems = addressStore._arrayOfAllItems, addrGridRow = 0, rowString = '', errorCount = 0, genericMsg = 'City was changed to empty (see comments) and is required to be supplied a new value.';
                  if (addressItems != null && addressItems.length != 0) {
                    for ( var key in addressItems) {
                      addrGridRow++;
                      var currentAddr = addressItems[key], city = '';
                      city = currentAddr.city1[0];
                      if ((city == '' || city == null) && reqType == 'U') {
                        errorCount++;
                        rowString += addrGridRow + " ";
                      }
                    }
                    var beforeAnd = rowString.substring(0, rowString.length - 2), afterAnd = rowString.substring(rowString.length - 2, rowString.length), lastStr = rowString.substring(
                        rowString.length - 3, rowString.length);
                    if (Number(lastStr.trim().length) == 2) { // records >= 10
                      beforeAnd = rowString.substring(0, rowString.length - 3);
                      afterAnd = rowString.substring(rowString.length - 3, rowString.length);
                    }
                    if (Number(errorCount) > 0) {
                      if (beforeAnd != '' && beforeAnd != null) {
                        genericMsg += ' Please check address list rows ' + beforeAnd + ' and ' + afterAnd + '.';
                        return new ValidationResult(null, false, genericMsg);
                      }
                      genericMsg += ' Please check address list row ' + afterAnd + '.';
                      return new ValidationResult(null, false, genericMsg);
                    }
                  }
                } else {
                  console.log("CmrGrid.GRIDS.ADDRESS_GRID_GRID undefined/null");
                }
                return new ValidationResult(null, true);
              } // validate
            }; // return
          })(), 'MAIN_NAME_TAB', 'frmCMR'); // validator body
}// func end

function addValidationForParentCompanyNo() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  // var parentCompanyNo = FormManager.getActualValue('dealerNo');
  var custSubType = FormManager.getActualValue('custSubGrp');
  var isuCd = FormManager.getActualValue('isuCd');
  if (custSubType == 'NRML'  || custSubGrp == 'ECOSY') {
    if (isuCd != "32" && (false == FormManager.getField('govType').checked)) {
      FormManager.addValidator('dealerNo', Validators.REQUIRED, [ 'You can input"000000" when there is not existing one. Parent Company No' ], 'MAIN_IBM_TAB');
    } else {
      FormManager.removeValidator('dealerNo', Validators.REQUIRED);
    }
  }
}

function addEngNameFormatValidation() {
  // var format = /[!@#$%^&*()_+\-=\[\]{};':"\\|<>\/?]/;
  var name1, name2, genericMsg;
  name1 = FormManager.getActualValue('custNm1');
  name2 = FormManager.getAcrualValue('custNm2');

  if (CNHandler.hasDoubleByte(name1) || CNHandler.hasDoubleByte(name2)) {
    genericMsg = 'Customer Name 1 and Customer Name Con\'t must not contain non-Latin1 characters.';
    return new ValidationResult(null, false, genericMsg);
  } else {
    return new ValidationResult(null, true);
  }

}

function addSocialCreditCdLengthValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var _socialCreditCdLen = FormManager.getActualValue('busnType');
        if (_socialCreditCdLen && _socialCreditCdLen.length > 0 && _socialCreditCdLen.length != 18){
            return new ValidationResult({
              id : 'busnType',
              type : 'text',
              name : 'busnType'
            }, false, 'The length for Social Credit Code should be 18 characters.');
        }else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function addAddrUpdateValidator() {
  console.log("running addAddrUpdateValidator . . .");
  FormManager
      .addFormValidator(
          (function() {
            return {
              validate : function() {
                
                if (FormManager.getActualValue('reqType') != 'U') {
                  return new ValidationResult(null, true);
                }
                
                var addrList = [];
                var addrRdcList = [];
                var cnAddrList = [];
                var cnAddrRdcList = [];
                
                var failInd = false;
                var zs01AddressUpdated = false;
                var otherAddressUpdated = false;
                
                var zs01Count = 0;
                
                var addrTxtZS01 = null;
                var addrTxt2ZS01 = null;
                var cnAddrTxtZS01 = null;
                var cnAddrTxt2ZS01 = null;
                
                var addrTypeOther = null;
                var addrSeqOther = null;
                
                var addrTxtOther = null;
                var addrTxt2Other = null;
                var cnAddrTxtOther = null;
                var cnAddrTxt2Other = null;
                
                // get addr, addr_rdc, intl_addr, intl_addr_rdc
                var reqId = FormManager.getActualValue('reqId');
                var qParams = {
                    _qall : 'Y',
                    REQ_ID : reqId ,
                   };
                var addrResults = cmr.query('GET.ADDR_BY_REQID', qParams);
                if (addrResults != null) {
                  for (var i = 0; i < addrResults.length; i++) {
                    var addr  = {
                        reqId : addrResults[i].ret1,
                        addrType : addrResults[i].ret2,
                        addrSeq : addrResults[i].ret3,
                        custNm1 : addrResults[i].ret4,
                        custNm2 : addrResults[i].ret5,
                        addrTxt : addrResults[i].ret6,
                        addrTxt2 : addrResults[i].ret7,
                        city1 : addrResults[i].ret8,
                    };
                    addrList.push(addr);
                  }
                }
                
                var addrRdcResults = cmr.query('GET.ADDRRDC_BY_REQID', qParams);
                if (addrRdcResults != null) {
                  for (var i = 0; i < addrRdcResults.length; i++) {
                    var addrRdc  = {
                        reqId : addrRdcResults[i].ret1,
                        addrType : addrRdcResults[i].ret2,
                        addrSeq : addrRdcResults[i].ret3,
                        custNm1 : addrRdcResults[i].ret4,
                        custNm2 : addrRdcResults[i].ret5,
                        addrTxt : addrRdcResults[i].ret6,
                        addrTxt2 : addrRdcResults[i].ret7,
                        city1 : addrRdcResults[i].ret8,
                    };
                    addrRdcList.push(addrRdc);
                  }
                }
                
                var intlAddrResults = cmr.query('GET.INTLADDR_BY_REQID', qParams);
                if (intlAddrResults != null) {
                  for (var i = 0; i < intlAddrResults.length; i++) {
                    var cnAddr  = {
                        reqId : intlAddrResults[i].ret1,
                        addrType : intlAddrResults[i].ret2,
                        addrSeq : intlAddrResults[i].ret3,
                        cnCustName1 : intlAddrResults[i].ret4,
                        cnCustName2 : intlAddrResults[i].ret5,
                        cnAddrTxt : intlAddrResults[i].ret6,
                        cnAddrTxt2 : intlAddrResults[i].ret7,
                        cnCity1 : intlAddrResults[i].ret8,
                    };
                    cnAddrList.push(cnAddr);
                  }
                }
                
                var intlAddrRdcResults = cmr.query('GET.INTLADDRRDC_BY_REQID', qParams);
                if (intlAddrRdcResults != null) {
                  for (var i = 0; i < intlAddrRdcResults.length; i++) {
                    var cnAddrRdc  = {
                        reqId : intlAddrRdcResults[i].ret1,
                        addrType : intlAddrRdcResults[i].ret2,
                        addrSeq : intlAddrRdcResults[i].ret3,
                        cnCustName1 : intlAddrRdcResults[i].ret4,
                        cnCustName2 : intlAddrRdcResults[i].ret5,
                        cnAddrTxt : intlAddrRdcResults[i].ret6,
                        cnAddrTxt2 : intlAddrRdcResults[i].ret7,
                        cnCity1 : intlAddrRdcResults[i].ret8,
                    };
                    cnAddrRdcList.push(cnAddrRdc);
                  }
                }
                
                if (addrList != null) {
                  for (var i=0; i< addrList.length; i++) {
                    if (addrList[i].addrType == 'ZS01') {
                      addrTxtZS01 = addrList[i].addrTxt;
                      addrTxt2ZS01 = addrList[i].addrTxt2;
                      
                      zs01Count +=1;
                      
                      if (isChangedAddress('ZS01', addrList[i].addrSeq, addrList, addrRdcList, cnAddrList, cnAddrRdcList)) {
                        zs01AddressUpdated = true;
                      }
                    }
                  }
                }
                
                if (cnAddrList != null) {
                  for (var i=0; i< cnAddrList.length; i++) {
                    if (cnAddrList[i].addrType == 'ZS01') {
                      cnAddrTxtZS01 = cnAddrList[i].cnAddrTxt;
                      cnAddrTxt2ZS01 = cnAddrList[i].cnAddrTxt2;
                    }
                  }
                }
                
                if (zs01Count > 1) {
                  return new ValidationResult(null, false, 'Only one Sold-To Address can be defined.');
                }
                
                if (addrList.length > 1) {
                  for (var i=0; i< addrList.length; i++) {
                    if (addrList[i].addrType != 'ZS01') {
                      if (isChangedAddress(addrList[i].addrType, addrList[i].addrSeq, addrList, addrRdcList, cnAddrList, cnAddrRdcList)) {
                        otherAddressUpdated = true;
                        addrTxtOther = addrList[i].addrTxt;
                        addrTxt2Other = addrList[i].addrTxt2;
                        cnAddrTxtOther = getAddrValue('cnAddrTxt', addrList[i].addrType, addrList[i].addrSeq, cnAddrList);
                        cnAddrTxt2Other = getAddrValue('cnAddrTxt2', addrList[i].addrType, addrList[i].addrSeq, cnAddrList);
                      }
                      
                      if (otherAddressUpdated) {
                        if ((addrTxtZS01 != addrTxtOther) || (addrTxt2ZS01 != addrTxt2Other) || (cnAddrTxtZS01 != cnAddrTxtOther) || (cnAddrTxt2ZS01 != cnAddrTxt2Other)) {
                          failInd = true;
                        }
                      }
                    }
                  }
                }
                
                if (failInd) {
                  var id = FormManager.getActualValue('reqId');
                  var ret = cmr.query('CHECK_CN_API_ATTACHMENT', {
                    ID : id
                  });

                  if (ret == null || ret.ret1 == null) {
                    return new ValidationResult(null, false, 'Additional addresses types need to be updated same with Legal Address (Sold To). If you do not agree, please attach supporting document and click Disable automatic processing checkbox.');
                  } else {
                    return new ValidationResult(null, true);
                  }
                }
                return new ValidationResult(null, true);
              }
            };
          })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function isChangedAddress(addrType, addrSeq, addrList, addrRdcList, cnAddrList, cnAddrRdcList) {
  var result = false;
  if (isChangedField('addrTxt', addrType, addrSeq, addrList, addrRdcList) || isChangedField('addrTxt2', addrType, addrSeq, addrList, addrRdcList) || isChangedField('cnAddrTxt', addrType, addrSeq, cnAddrList, cnAddrRdcList) || isChangedField('cnAddrTxt2', addrType, addrSeq, cnAddrList, cnAddrRdcList)) {
    result = true;
  }
  return result;
}

function isChangedField(fieldName, addrType, addrSeq, list1, list2) {
  var result = false;
  var fieldValue1 = null;
  var fieldValue2 = null;
  
  // addr
  if (list1 != null && list1.length > 0) {
    for (var i = 0; i < list1.length; i ++) {
      if (list1[i].addrType == addrType && list1[i].addrSeq == addrSeq) {
        if (fieldName == 'addrTxt') {
          fieldValue1 = list1[i].addrTxt;
        } else if (fieldName == 'addrTxt2') {
          fieldValue1 = list1[i].addrTxt2;
        } else if (fieldName == 'cnAddrTxt') {
          fieldValue1 = list1[i].cnAddrTxt;
        } else if (fieldName == 'cnAddrTxt2') {
          fieldValue1 = list1[i].cnAddrTxt2;
        }
      }
    }
  }
  
  // addr_rdc
  if (list2 != null && list2.length > 0) {
    for (var j = 0; j < list2.length; j ++) {
      if (list2[j].addrType == addrType && list2[j].addrSeq == addrSeq) {
        if (fieldName == 'addrTxt') {
          fieldValue2 = list2[j].addrTxt;
        } else if (fieldName == 'addrTxt2') {
          fieldValue2 = list2[j].addrTxt2;
        } else if (fieldName == 'cnAddrTxt') {
          fieldValue2 = list2[j].cnAddrTxt;
        } else if (fieldName == 'cnAddrTxt2') {
          fieldValue2 = list2[j].cnAddrTxt2;
        }
      }
    }
  }
  
  if (convert2DBCSIgnoreCase(fieldValue1) != convert2DBCSIgnoreCase(fieldValue2)) {
    result = true;
  }
  return result;
}

function getAddrValue(fieldName, addrType, addrSeq, list) {
  var value = null;
  if (list!=null && list.length > 0) {
    for (var i = 0; i < list.length; i ++) {
      if (list[i].addrType == addrType && list[i].addrSeq == addrSeq) {
        if (fieldName == 'addrTxt') {
          value = list[i].addrTxt;
        } else if (fieldName == 'addrTxt2') {
          value = list[i].addrTxt2;
        } else if (fieldName == 'cnAddrTxt') {
          value = list[i].cnAddrTxt;
        } else if (fieldName == 'cnAddrTxt2') {
          value = list[i].cnAddrTxt2;
        }
      }
    }
  }
  return value;
}

// no use
function addAddrUpdateValidator0() {
  console.log("running addAddrUpdateValidator . . .");
  FormManager
      .addFormValidator(
          (function() {
            return {
              validate : function() {
                
                if (FormManager.getActualValue('reqType') != 'U') {
                  return new ValidationResult(null, true);
                }
                
                var failInd = false;
                var zs01Updated = false;
                var additionalAddrupdated = false;
                if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 1) {
                  var record = null;
                  var type = null;
                  var updateIndZS01 = null;
                  var custNm1ZS01 = null;
                  var custNm2ZS01 = null;
                  var addrTxtZS01 = null;
                  var addrTxt2ZS01 = null;
                  var cnCustName1ZS01 = null;
                  var cnCustName2ZS01 = null;
                  var cnAddrTxtZS01 = null;
                  var cnAddrTxt2ZS01 = null;
                  var updateIndOther = null;
                  var custNm1Other = null;
                  var custNm2Other = null;
                  var addrTxtOther = null;
                  var addrTxt2Other = null;
                  var cnCustName1Other = null;
                  var cnCustName2Other = null;
                  var cnAddrTxtOther = null;
                  var cnAddrTxt2Other = null;

                  // check ZS01 updated
                  for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
                    record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
                    type = record.addrType;
                    if (typeof (type) == 'object') {
                      type = type[0];
                    }
                    if (type == 'ZS01') {
                      updateIndZS01 = record.updateInd;
                      custNm1ZS01 = record.custNm1;
                      custNm2ZS01 = record.custNm2;
                      addrTxtZS01 = record.addrTxt;
                      addrTxt2ZS01 = record.addrTxt2;
                      cnCustName1ZS01 = record.cnCustName1;
                      cnCustName2ZS01 = record.cnCustName2;
                      cnAddrTxtZS01 = record.cnAddrTxt;
                      cnAddrTxt2ZS01 = record.cnAddrTxt2;
                      
                      if (typeof (updateIndZS01) == 'object') {
                        updateIndZS01 = updateIndZS01[0];
                      }
                      if (typeof (custNm1ZS01) == 'object') {
                        custNm1ZS01 = custNm1ZS01[0];
                      }
                      if (typeof (custNm2ZS01) == 'object') {
                        custNm2ZS01 = custNm2ZS01[0];
                      }
                      if (typeof (addrTxtZS01) == 'object') {
                        addrTxtZS01 = addrTxtZS01[0];
                      }
                      if (typeof (addrTxt2ZS01) == 'object') {
                        addrTxt2ZS01 = addrTxt2ZS01[0];
                      }
                      if (typeof (cnCustName1ZS01) == 'object') {
                        cnCustName1ZS01 = cnCustName1ZS01[0];
                      }
                      if (typeof (cnCustName2ZS01) == 'object') {
                        cnCustName2ZS01 = cnCustName2ZS01[0];
                      }
                      if (typeof (cnAddrTxtZS01) == 'object') {
                        cnAddrTxtZS01 = cnAddrTxtZS01[0];
                      }
                      if (typeof (cnAddrTxt2ZS01) == 'object') {
                        cnAddrTxt2ZS01 = cnAddrTxt2ZS01[0];
                      }
                      
                      if (updateIndZS01=='U' ) {
                        zs01Updated = true;
                      }
                    }
                    
                  }
                  
                  // check additional addr updated
                  for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
                    record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
                    type = record.addrType;
                    if (typeof (type) == 'object') {
                      type = type[0];
                    }
                    if (type != 'ZS01') {
                      updateIndOther = record.updateInd;
                      custNm1Other = record.custNm1;
                      custNm2Other = record.custNm2;
                      addrTxtOther = record.addrTxt;
                      addrTxt2Other = record.addrTxt2;
                      cnCustName1Other = record.cnCustName1;
                      cnCustName2Other = record.cnCustName2;
                      cnAddrTxtOther = record.cnAddrTxt;
                      cnAddrTxt2Other = record.cnAddrTxt2;
                      
                      if (typeof (updateIndOther) == 'object') {
                        updateIndOther = updateIndOther[0];
                      }
                      if (typeof (custNm1Other) == 'object') {
                        custNm1Other = custNm1Other[0];
                      }
                      if (typeof (custNm2Other) == 'object') {
                        custNm2Other = custNm2Other[0];
                      }
                      if (typeof (addrTxtOther) == 'object') {
                        addrTxtOther = addrTxtOther[0];
                      }
                      if (typeof (addrTxt2Other) == 'object') {
                        addrTxt2Other = addrTxt2Other[0];
                      }
                      if (typeof (cnCustName1Other) == 'object') {
                        cnCustName1Other = cnCustName1Other[0];
                      }
                      if (typeof (cnCustName2Other) == 'object') {
                        cnCustName2Other = cnCustName2Other[0];
                      }
                      if (typeof (cnAddrTxtOther) == 'object') {
                        cnAddrTxtOther = cnAddrTxtOther[0];
                      }
                      if (typeof (cnAddrTxt2Other) == 'object') {
                        cnAddrTxt2Other = cnAddrTxt2Other[0];
                      }
                      
                      if (updateIndOther=='U' || updateIndOther=='N') {
                        additionalAddrupdated = true;
                      }
                      
                      if (zs01Updated) {
                        if (additionalAddrupdated) {
                          if ((custNm1ZS01 && custNm1ZS01 != '') || (custNm1Other && custNm1Other != '')) {
                            if (custNm1ZS01 != custNm1Other) {
                              failInd = true;
                            }
                          } else if ((custNm2ZS01 && custNm2ZS01 != '') || (custNm2Other && custNm2Other != '')) {
                            if (custNm2ZS01 != custNm2Other) {
                              failInd = true;
                            }
                          } else if ((addrTxtZS01 && addrTxtZS01 != '') || (addrTxtOther && addrTxtOther != '')) {
                            if (addrTxtZS01 != addrTxtOther) {
                              failInd = true;
                            }
                          } else if ((addrTxt2ZS01 && addrTxt2ZS01 != '') || (addrTxt2Other && addrTxt2Other != '')) {
                            if (addrTxt2ZS01 != addrTxt2Other) {
                              failInd = true;
                            }
                          } else if ((cnCustName1ZS01 && cnCustName1ZS01 != '') || (cnCustName1Other && cnCustName1Other != '')) {
                            if (cnCustName1ZS01 != cnCustName1Other) {
                              failInd = true;
                            }
                          } else if ((cnCustName2ZS01 && cnCustName2ZS01 != '') || (cnCustName2Other && cnCustName2Other != '')) {
                            if (cnCustName2ZS01 != cnCustName2Other) {
                              failInd = true;
                            }
                          } else if ((cnAddrTxtZS01 && cnAddrTxtZS01 != '') || (cnAddrTxtOther && cnAddrTxtOther != '')) {
                            if (cnAddrTxtZS01 != cnAddrTxtOther) {
                              failInd = true;
                            }
                          } else if ((cnAddrTxt2ZS01 && cnAddrTxt2ZS01 != '') || (cnAddrTxt2Other && cnAddrTxt2Other != '')) {
                            if (cnAddrTxt2ZS01 != cnAddrTxt2Other) {
                              failInd = true;
                            }
                          }
                        } else  {
                          failInd = true;
                        }
                      } else {
                        if (additionalAddrupdated) {
                          failInd = true;
                        }
                      }
                      
                    }
                  }
                  
                }
                
                if (failInd) {
                  var id = FormManager.getActualValue('reqId');
                  var ret = cmr.query('CHECK_CN_API_ATTACHMENT', {
                    ID : id
                  });

                  if (ret == null || ret.ret1 == null) {
                    return new ValidationResult(null, false, 'Additional addresses types need to be updated same with Legal Address (Sold To). If you do not agree, please attach supporting document and click Disable automatic processing checkbox.');
                  } else {
                    return new ValidationResult(null, true);
                  }
                }
                return new ValidationResult(null, true);
              }
            };
          })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function setCompanyOnInacCd() {
  if (FormManager.getActualValue('reqType') != 'C' || FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (_pagemodel.userRole.toUpperCase() != "REQUESTER") {
    return;
  }
  dojo.connect(FormManager.getField('inacCd'), 'onChange', function(value) {
    var inacCd = FormManager.getActualValue('inacCd');
    if (inacCd == 'XXXX') {
      FormManager.setValue('company', '856105');
    }
  });
}

function addDoubleByteValidatorCN(cntry, details) {

  /* Address */
  FormManager.addValidator('cnCustName1', Validators.NO_SINGLE_BYTE, [ 'Customer Name Chinese' ]);
  FormManager.addValidator('cnCustName2', Validators.NO_SINGLE_BYTE, [ 'Customer Name Con' + '\'' + 't Chinese' ]);
  FormManager.addValidator('cnCustName3', Validators.NO_SINGLE_BYTE, [ 'Customer Name Con' + '\'' + 't Chinese 2' ]);
  FormManager.addValidator('cnAddrTxt', Validators.NO_SINGLE_BYTE, [ 'Street Address Chinese' ]);
  FormManager.addValidator('cnAddrTxt2', Validators.NO_SINGLE_BYTE, [ 'Street Address Con' + '\'' + 't Chinese' ]);
  FormManager.addValidator('cnCity', Validators.NO_SINGLE_BYTE, [ 'City Chinese' ]);
  FormManager.addValidator('cnDistrict', Validators.NO_SINGLE_BYTE, [ 'District Chinese' ]);

  FormManager.addValidator('custNm1', Validators.LATIN, [ 'Customer Name English' ]);
  FormManager.addValidator('custNm2', Validators.LATIN, [ 'Customer Name Con' + '\'' + 't English' ]);
  FormManager.addValidator('custNm3', Validators.LATIN, [ 'Customer Name Con' + '\'' + 't 2 English' ]);
  FormManager.addValidator('addrTxt', Validators.LATIN, [ 'Customer Address English' ]);
  FormManager.addValidator('addrTxt2', Validators.LATIN, [ 'Customer Address Con' + '\'' + 't English' ]);
  FormManager.addValidator('city2', Validators.LATIN, [ 'District English' ]);
  FormManager.addValidator('dropdowncity1', Validators.LATIN, [ 'City English' ]);
  FormManager.addValidator('dept', Validators.LATIN, [ 'Department English' ]);
  FormManager.addValidator('bldg', Validators.LATIN, [ 'Building English' ]);
  FormManager.addValidator('office', Validators.LATIN, [ 'Office English' ]);
  FormManager.addValidator('poBox', Validators.LATIN, [ 'PostBox English' ]);
}

function convert2DBCSIgnoreCase(input) {
  var modifiedVal = '';
  if (input != null && input.length > 0 && input != '') {
    modifiedVal = input;
    // modifiedVal = modifiedVal.replace(/[^\d]/g, '');
    modifiedVal = modifiedVal.replace(/1/g, '１');
    modifiedVal = modifiedVal.replace(/2/g, '２');
    modifiedVal = modifiedVal.replace(/3/g, '３');
    modifiedVal = modifiedVal.replace(/4/g, '４');
    modifiedVal = modifiedVal.replace(/5/g, '５');
    modifiedVal = modifiedVal.replace(/6/g, '６');
    modifiedVal = modifiedVal.replace(/7/g, '７');
    modifiedVal = modifiedVal.replace(/8/g, '８');
    modifiedVal = modifiedVal.replace(/9/g, '９');
    modifiedVal = modifiedVal.replace(/0/g, '０');
    modifiedVal = modifiedVal.replace(/a/g, 'ａ');
    modifiedVal = modifiedVal.replace(/b/g, 'ｂ');
    modifiedVal = modifiedVal.replace(/c/g, 'ｃ');
    modifiedVal = modifiedVal.replace(/d/g, 'ｄ');
    modifiedVal = modifiedVal.replace(/e/g, 'ｅ');
    modifiedVal = modifiedVal.replace(/f/g, 'ｆ');
    modifiedVal = modifiedVal.replace(/g/g, 'ｇ');
    modifiedVal = modifiedVal.replace(/h/g, 'ｈ');
    modifiedVal = modifiedVal.replace(/i/g, 'ｉ');
    modifiedVal = modifiedVal.replace(/j/g, 'ｊ');
    modifiedVal = modifiedVal.replace(/k/g, 'ｋ');
    modifiedVal = modifiedVal.replace(/l/g, 'ｌ');
    modifiedVal = modifiedVal.replace(/m/g, 'ｍ');
    modifiedVal = modifiedVal.replace(/n/g, 'ｎ');
    modifiedVal = modifiedVal.replace(/o/g, 'ｏ');
    modifiedVal = modifiedVal.replace(/p/g, 'ｐ');
    modifiedVal = modifiedVal.replace(/q/g, 'ｑ');
    modifiedVal = modifiedVal.replace(/r/g, 'ｒ');
    modifiedVal = modifiedVal.replace(/s/g, 'ｓ');
    modifiedVal = modifiedVal.replace(/t/g, 'ｔ');
    modifiedVal = modifiedVal.replace(/u/g, 'ｕ');
    modifiedVal = modifiedVal.replace(/v/g, 'ｖ');
    modifiedVal = modifiedVal.replace(/w/g, 'ｗ');
    modifiedVal = modifiedVal.replace(/x/g, 'ｘ');
    modifiedVal = modifiedVal.replace(/y/g, 'ｙ');
    modifiedVal = modifiedVal.replace(/z/g, 'ｚ');
    modifiedVal = modifiedVal.replace(/A/g, 'Ａ');
    modifiedVal = modifiedVal.replace(/B/g, 'Ｂ');
    modifiedVal = modifiedVal.replace(/C/g, 'Ｃ');
    modifiedVal = modifiedVal.replace(/D/g, 'Ｄ');
    modifiedVal = modifiedVal.replace(/E/g, 'Ｅ');
    modifiedVal = modifiedVal.replace(/F/g, 'Ｆ');
    modifiedVal = modifiedVal.replace(/G/g, 'Ｇ');
    modifiedVal = modifiedVal.replace(/H/g, 'Ｈ');
    modifiedVal = modifiedVal.replace(/I/g, 'Ｉ');
    modifiedVal = modifiedVal.replace(/J/g, 'Ｊ');
    modifiedVal = modifiedVal.replace(/K/g, 'Ｋ');
    modifiedVal = modifiedVal.replace(/L/g, 'Ｌ');
    modifiedVal = modifiedVal.replace(/M/g, 'Ｍ');
    modifiedVal = modifiedVal.replace(/N/g, 'Ｎ');
    modifiedVal = modifiedVal.replace(/O/g, 'Ｏ');
    modifiedVal = modifiedVal.replace(/P/g, 'Ｐ');
    modifiedVal = modifiedVal.replace(/Q/g, 'Ｑ');
    modifiedVal = modifiedVal.replace(/R/g, 'Ｒ');
    modifiedVal = modifiedVal.replace(/S/g, 'Ｓ');
    modifiedVal = modifiedVal.replace(/T/g, 'Ｔ');
    modifiedVal = modifiedVal.replace(/U/g, 'Ｕ');
    modifiedVal = modifiedVal.replace(/V/g, 'Ｖ');
    modifiedVal = modifiedVal.replace(/W/g, 'Ｗ');
    modifiedVal = modifiedVal.replace(/X/g, 'Ｘ');
    modifiedVal = modifiedVal.replace(/Y/g, 'Ｙ');
    modifiedVal = modifiedVal.replace(/Z/g, 'Ｚ');
    modifiedVal = modifiedVal.replace(/ /g, '　');
    modifiedVal = replaceAndSymbol(modifiedVal);
    modifiedVal = replaceCrossbarSymbol(modifiedVal);
  }
  return modifiedVal;
}
function replaceAndSymbol(value) {
  var modifiedVal = '';
  if (value != null && value.length > 0 && value != '') {
    modifiedVal = value;
    modifiedVal = modifiedVal.replace(/&/g, '＆');
  }
  return modifiedVal;
};
function replaceCrossbarSymbol(value) {
  var modifiedVal = '';
  if (value != null && value.length > 0 && value != '') {
    modifiedVal = value;
    modifiedVal = modifiedVal.replace(/-/g, '－');
    modifiedVal = modifiedVal.replace(/\./g, '．');
    modifiedVal = modifiedVal.replace(/,/g, '，');
    modifiedVal = modifiedVal.replace(/:/g, '：');
    modifiedVal = modifiedVal.replace(/_/g, '＿');
    modifiedVal = modifiedVal.replace(/\(/g, '（');
    modifiedVal = modifiedVal.replace(/\)/g, '）');
    
    modifiedVal = modifiedVal.replace(/\//g, '\／');
    modifiedVal = modifiedVal.replace(/\</g, '\＜');
    modifiedVal = modifiedVal.replace(/\>/g, '\＞');
    modifiedVal = modifiedVal.replace(/\?/g, '\？');
    modifiedVal = modifiedVal.replace(/\`/g, '\｀');
    modifiedVal = modifiedVal.replace(/\~/g, '\～');
    modifiedVal = modifiedVal.replace(/\!/g, '\！');
    modifiedVal = modifiedVal.replace(/\@/g, '\＠');
    modifiedVal = modifiedVal.replace(/\#/g, '\＃');
    modifiedVal = modifiedVal.replace(/\$/g, '\＄');
    modifiedVal = modifiedVal.replace(/\%/g, '\％');
    modifiedVal = modifiedVal.replace(/\^/g, '\＾');
    modifiedVal = modifiedVal.replace(/\*/g, '\＊');
    modifiedVal = modifiedVal.replace(/\+/g, '\＋');
    modifiedVal = modifiedVal.replace(/\=/g, '\＝');
    modifiedVal = modifiedVal.replace(/\[/g, '\［');
    modifiedVal = modifiedVal.replace(/\]/g, '\］');
    modifiedVal = modifiedVal.replace(/\{/g, '\｛');
    modifiedVal = modifiedVal.replace(/\}/g, '\｝');
    modifiedVal = modifiedVal.replace(/\|/g, '\｜');
    modifiedVal = modifiedVal.replace(/\;/g, '\；');
    modifiedVal = modifiedVal.replace(/\'/g, '\＇');
    modifiedVal = modifiedVal.replace(/\"/g, '\＂');
  }
  return modifiedVal;
};

function convert2SBCS(input) {
  var modifiedVal = '';
  if (input != null && input.length > 0 && input != '') {
    modifiedVal = input;
    // modifiedVal = modifiedVal.replace(/[^\d]/g, '');
    modifiedVal = modifiedVal.replace(/１/g, '1');
    modifiedVal = modifiedVal.replace(/２/g, '2');
    modifiedVal = modifiedVal.replace(/３/g, '3');
    modifiedVal = modifiedVal.replace(/４/g, '4');
    modifiedVal = modifiedVal.replace(/５/g, '5');
    modifiedVal = modifiedVal.replace(/６/g, '6');
    modifiedVal = modifiedVal.replace(/７/g, '7');
    modifiedVal = modifiedVal.replace(/８/g, '8');
    modifiedVal = modifiedVal.replace(/９/g, '9');
    modifiedVal = modifiedVal.replace(/０/g, '0');
    modifiedVal = modifiedVal.replace(/ａ/g, 'a');
    modifiedVal = modifiedVal.replace(/ｂ/g, 'b');
    modifiedVal = modifiedVal.replace(/ｃ/g, 'c');
    modifiedVal = modifiedVal.replace(/ｄ/g, 'd');
    modifiedVal = modifiedVal.replace(/ｅ/g, 'e');
    modifiedVal = modifiedVal.replace(/ｆ/g, 'f');
    modifiedVal = modifiedVal.replace(/ｇ/g, 'g');
    modifiedVal = modifiedVal.replace(/ｈ/g, 'f');
    modifiedVal = modifiedVal.replace(/ｉ/g, 'i');
    modifiedVal = modifiedVal.replace(/ｊ/g, 'j');
    modifiedVal = modifiedVal.replace(/ｋ/g, 'k');
    modifiedVal = modifiedVal.replace(/ｌ/g, 'l');
    modifiedVal = modifiedVal.replace(/ｍ/g, 'm');
    modifiedVal = modifiedVal.replace(/ｎ/g, 'n');
    modifiedVal = modifiedVal.replace(/ｏ/g, 'o');
    modifiedVal = modifiedVal.replace(/ｐ/g, 'p');
    modifiedVal = modifiedVal.replace(/ｑ/g, 'q');
    modifiedVal = modifiedVal.replace(/ｒ/g, 'r');
    modifiedVal = modifiedVal.replace(/ｓ/g, 's');
    modifiedVal = modifiedVal.replace(/ｔ/g, 't');
    modifiedVal = modifiedVal.replace(/ｕ/g, 'u');
    modifiedVal = modifiedVal.replace(/ｖ/g, 'v');
    modifiedVal = modifiedVal.replace(/ｗ/g, 'w');
    modifiedVal = modifiedVal.replace(/ｘ/g, 'x');
    modifiedVal = modifiedVal.replace(/ｙ/g, 'y');
    modifiedVal = modifiedVal.replace(/ｚ/g, 'z');
    modifiedVal = modifiedVal.replace(/Ａ/g, 'A');
    modifiedVal = modifiedVal.replace(/Ｂ/g, 'B');
    modifiedVal = modifiedVal.replace(/Ｃ/g, 'C');
    modifiedVal = modifiedVal.replace(/Ｄ/g, 'D');
    modifiedVal = modifiedVal.replace(/Ｅ/g, 'E');
    modifiedVal = modifiedVal.replace(/Ｆ/g, 'F');
    modifiedVal = modifiedVal.replace(/Ｇ/g, 'G');
    modifiedVal = modifiedVal.replace(/Ｈ/g, 'H');
    modifiedVal = modifiedVal.replace(/Ｉ/g, 'I');
    modifiedVal = modifiedVal.replace(/Ｊ/g, 'J');
    modifiedVal = modifiedVal.replace(/Ｋ/g, 'K');
    modifiedVal = modifiedVal.replace(/Ｌ/g, 'L');
    modifiedVal = modifiedVal.replace(/Ｍ/g, 'M');
    modifiedVal = modifiedVal.replace(/Ｎ/g, 'N');
    modifiedVal = modifiedVal.replace(/Ｏ/g, 'O');
    modifiedVal = modifiedVal.replace(/Ｐ/g, 'P');
    modifiedVal = modifiedVal.replace(/Ｑ/g, 'Q');
    modifiedVal = modifiedVal.replace(/Ｒ/g, 'R');
    modifiedVal = modifiedVal.replace(/Ｓ/g, 'S');
    modifiedVal = modifiedVal.replace(/Ｔ/g, 'T');
    modifiedVal = modifiedVal.replace(/Ｕ/g, 'U');
    modifiedVal = modifiedVal.replace(/Ｖ/g, 'V');
    modifiedVal = modifiedVal.replace(/Ｗ/g, 'W');
    modifiedVal = modifiedVal.replace(/Ｘ/g, 'X');
    modifiedVal = modifiedVal.replace(/Ｙ/g, 'Y');
    modifiedVal = modifiedVal.replace(/Ｚ/g, 'Z');
    modifiedVal = modifiedVal.replace(/　/g, ' ');
    modifiedVal = modifiedVal.replace(/＆/g, '&');
    modifiedVal = modifiedVal.replace(/－/g, '-');
    modifiedVal = modifiedVal.replace(/\．/g, '\.');
    modifiedVal = modifiedVal.replace(/，/g, ',');
    modifiedVal = modifiedVal.replace(/：/g, ':');
    modifiedVal = modifiedVal.replace(/＿/g, '_');
    
    modifiedVal = modifiedVal.replace(/（/g, '(');
    modifiedVal = modifiedVal.replace(/）/g, ')');
    modifiedVal = modifiedVal.replace(/／/g, '/');
    modifiedVal = modifiedVal.replace(/＜/g, '<');
    modifiedVal = modifiedVal.replace(/＞/g, '>');
    modifiedVal = modifiedVal.replace(/？/g, '?');
    modifiedVal = modifiedVal.replace(/｀/g, '`');
    modifiedVal = modifiedVal.replace(/～/g, '~');
    modifiedVal = modifiedVal.replace(/！/g, '!');
    modifiedVal = modifiedVal.replace(/＠/g, '@');
    modifiedVal = modifiedVal.replace(/＃/g, '#');
    modifiedVal = modifiedVal.replace(/＄/g, '$');
    modifiedVal = modifiedVal.replace(/％/g, '%');
    modifiedVal = modifiedVal.replace(/＾/g, '^');
    modifiedVal = modifiedVal.replace(/＊/g, '*');
    modifiedVal = modifiedVal.replace(/＋/g, '+');
    modifiedVal = modifiedVal.replace(/＝/g, '=');
    modifiedVal = modifiedVal.replace(/［/g, '[');
    modifiedVal = modifiedVal.replace(/］/g, ']');
    modifiedVal = modifiedVal.replace(/｛/g, '{');
    modifiedVal = modifiedVal.replace(/｝/g, '}');
    modifiedVal = modifiedVal.replace(/｜/g, '|');
    modifiedVal = modifiedVal.replace(/；/g, ';');
    modifiedVal = modifiedVal.replace(/＇/g, '\'');
    modifiedVal = modifiedVal.replace(/＂/g, '\"');
  }
  return modifiedVal;
}

function convertCnCustName1(cntry, addressMode, details) {
  convertCnCustName1InDetails();
  dojo.connect(FormManager.getField('cnCustName1'), 'onChange', function(value) {
    convertCnCustName1InDetails();
  });
}
function convertCnCustName1InDetails() {
  var cnCustName1 = FormManager.getActualValue('cnCustName1');
  FormManager.setValue('cnCustName1', convert2DBCSIgnoreCase(cnCustName1));
  cnCustName1 = FormManager.getActualValue('cnCustName1');
}

function convertCnCustName2(cntry, addressMode, details) {
  convertCnCustName2InDetails();
  dojo.connect(FormManager.getField('cnCustName2'), 'onChange', function(value) {
    convertCnCustName2InDetails();
  });
}
function convertCnCustName2InDetails() {
  var cnCustName2 = FormManager.getActualValue('cnCustName2');
  FormManager.setValue('cnCustName2', convert2DBCSIgnoreCase(cnCustName2));
  cnCustName2 = FormManager.getActualValue('cnCustName2');
}

function convertCnCustName3(cntry, addressMode, details) {
  convertCnCustName3InDetails();
  dojo.connect(FormManager.getField('cnCustName3'), 'onChange', function(value) {
    convertCnCustName3InDetails();
  });
}
function convertCnCustName3InDetails() {
  var cnCustName3 = FormManager.getActualValue('cnCustName3');
  FormManager.setValue('cnCustName3', convert2DBCSIgnoreCase(cnCustName3));
  cnCustName3 = FormManager.getActualValue('cnCustName3');
}

function convertCnAddrTxt(cntry, addressMode, details) {
  convertCnAddrTxtInDetails();
  dojo.connect(FormManager.getField('cnAddrTxt'), 'onChange', function(value) {
    convertCnAddrTxtInDetails();
  });
}
function convertCnAddrTxtInDetails() {
  var cnAddrTxt = FormManager.getActualValue('cnAddrTxt');
  FormManager.setValue('cnAddrTxt', convert2DBCSIgnoreCase(cnAddrTxt));
  cnAddrTxt = FormManager.getActualValue('cnAddrTxt');
}

function convertCnAddrTxt2(cntry, addressMode, details) {
  convertCnAddrTxt2InDetails();
  dojo.connect(FormManager.getField('cnAddrTxt2'), 'onChange', function(value) {
    convertCnAddrTxt2InDetails();
  });
}
function convertCnAddrTxt2InDetails() {
  var cnAddrTxt2 = FormManager.getActualValue('cnAddrTxt2');
  FormManager.setValue('cnAddrTxt2', convert2DBCSIgnoreCase(cnAddrTxt2));
  cnAddrTxt2 = FormManager.getActualValue('cnAddrTxt2');
}

function convertCnDistrict(cntry, addressMode, details) {
  convertCnDistrictInDetails();
  dojo.connect(FormManager.getField('cnDistrict'), 'onChange', function(value) {
    convertCnDistrictInDetails();
  });
}
function convertCnDistrictInDetails() {
  var cnDistrict = FormManager.getActualValue('cnDistrict');
  FormManager.setValue('cnDistrict', convert2DBCSIgnoreCase(cnDistrict));
  cnDistrict = FormManager.getActualValue('cnDistrict');
}
// TODO
function convertCustNm1(cntry, addressMode, details) {
  dojo.connect(FormManager.getField('custNm1'), 'onChange', function(value) {
    var custNm1 = FormManager.getActualValue('custNm1');
    FormManager.setValue('custNm1', convert2SBCS(custNm1));
  });
}

function convertCustNm2(cntry, addressMode, details) {
  dojo.connect(FormManager.getField('custNm2'), 'onChange', function(value) {
    var custNm2 = FormManager.getActualValue('custNm2');
    FormManager.setValue('custNm2', convert2SBCS(custNm2));
  });
}
function convertCustNm3(cntry, addressMode, details) {
  dojo.connect(FormManager.getField('custNm3'), 'onChange', function(value) {
    var custNm3 = FormManager.getActualValue('custNm3');
    FormManager.setValue('custNm3', convert2SBCS(custNm3));
  });
}
function convertAddrTxt(cntry, addressMode, details) {
  dojo.connect(FormManager.getField('addrTxt'), 'onChange', function(value) {
    var addrTxt = FormManager.getActualValue('addrTxt');
    FormManager.setValue('addrTxt', convert2SBCS(addrTxt));
  });
}
function convertAddrTxt2(cntry, addressMode, details) {
  dojo.connect(FormManager.getField('addrTxt2'), 'onChange', function(value) {
    var addrTxt2 = FormManager.getActualValue('addrTxt2');
    FormManager.setValue('addrTxt2', convert2SBCS(addrTxt2));
  });
}
function convertCity2(cntry, addressMode, details) {
  dojo.connect(FormManager.getField('city2'), 'onChange', function(value) {
    var city2 = FormManager.getActualValue('city2');
    FormManager.setValue('city2', convert2SBCS(city2));
  });
}
function convertDept(cntry, addressMode, details) {
  dojo.connect(FormManager.getField('dept'), 'onChange', function(value) {
    var dept = FormManager.getActualValue('dept');
    FormManager.setValue('dept', convert2SBCS(dept));
  });
}
function convertBldg(cntry, addressMode, details) {
  dojo.connect(FormManager.getField('bldg'), 'onChange', function(value) {
    var bldg = FormManager.getActualValue('bldg');
    FormManager.setValue('bldg', convert2SBCS(bldg));
  });
}
function convertOffice(cntry, addressMode, details) {
  dojo.connect(FormManager.getField('office'), 'onChange', function(value) {
    var office = FormManager.getActualValue('office');
    FormManager.setValue('office', convert2SBCS(office));
  });
}
function convertPoBox(cntry, addressMode, details) {
  dojo.connect(FormManager.getField('poBox'), 'onChange', function(value) {
    var poBox = FormManager.getActualValue('poBox');
    FormManager.setValue('poBox', convert2SBCS(poBox));
  });
}

function validateEnNameForInter() {
  console.log("running validateCnNameAndAddr . . .");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var custSubType = FormManager.getActualValue('custSubGrp');
          if (custSubType == 'INTER') {
            var custNm1ZS01 = '';
            var custNm2ZS01 = '';
            for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
              record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
              type = record.addrType;

              if (typeof (type) == 'object') {
                type = type[0];
              }
              if (type == 'ZS01') {
                custNm1ZS01 = record.custNm1;
                custNm2ZS01 = record.custNm2 == null ? '' : record.custNm2;
              }
            }
            var enName = custNm1ZS01 + ' ' + custNm2ZS01;
            var custSubType = FormManager.getActualValue('custSubGrp');
            if (enName.toUpperCase().indexOf("IBM CHINA") == -1){
              return new ValidationResult(null, false, "Customer Name English should include 'IBM China' for Internal Sub_scenario.");
            } else {
              return new ValidationResult(null, true);
            }
          } else {
            return new ValidationResult(null, true);
          }
      }
    }
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function validateEnNameInAddrTab() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {

        var custSubType = FormManager.getActualValue('custSubGrp');
          if (custSubType == 'INTER') {
            var custNm1 = FormManager.getActualValue('custNm1');
            var custNm2 = FormManager.getActualValue('custNm2');
            var enName = custNm1 + ' ' + custNm2;
            var custSubType = FormManager.getActualValue('custSubGrp');
            if (enName.toUpperCase().indexOf("IBM CHINA") == -1){
              return new ValidationResult(null, false, "Customer Name English should include 'IBM China' for Internal Sub_scenario.");
            } else {
              return new ValidationResult(null, true);
            }
          } else {
            return new ValidationResult(null, true);
          }
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function validateCnNameAndAddr() {
  console.log("running validateCnNameAndAddr . . .");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var custSubType = FormManager.getActualValue('custSubGrp');
        var action = FormManager.getActualValue('yourAction');
        if(action == 'SFP'){

          var cnCustName1ZS01 = '';
          var cnCustName2ZS01 = '';
          var cnAddrTxtZS01 = '';
          var intlCustNm4ZS01 = '';
          var cnCityZS01 = '';
          var cnDistrictZS01 = '';

          for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
            record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
            type = record.addrType;

            if (typeof (type) == 'object') {
              type = type[0];
            }

            if (type == 'ZS01') {
              cnCustName1ZS01 = record.cnCustName1;
              cnCustName2ZS01 = record.cnCustName2;
              cnAddrTxtZS01 = record.cnAddrTxt;
              cnCityZS01 = record.cnCity;
              cnDistrictZS01 = record.cnDistrict;
            }

            if (typeof (cnCustName1ZS01) == 'object') {
              if (cnCustName1ZS01[0] != '' && cnCustName1ZS01[0] != null) {
                cnCustName1ZS01 = cnCustName1ZS01[0];
              }
            }

            if (typeof (cnCustName2ZS01) == 'object') {
              if (cnCustName2ZS01[0] != '' && cnCustName2ZS01[0] != null) {
                cnCustName2ZS01 = cnCustName2ZS01[0];
              }
            }

            if (typeof (cnAddrTxtZS01) == 'object') {
              if (cnAddrTxtZS01[0] != '' && cnAddrTxtZS01[0] != null) {
                cnAddrTxtZS01 = cnAddrTxtZS01[0];
              }
            }
            
            if (typeof (cnCityZS01) == 'object') {
              if (cnCityZS01[0] != '' && cnCityZS01[0] != null) {
                cnCityZS01 = cnCityZS01[0];
              }
            }
            
            if (typeof (cnDistrictZS01) == 'object') {
              if (cnDistrictZS01[0] != '' && cnDistrictZS01[0] != null) {
                cnDistrictZS01 = cnDistrictZS01[0];
              }
            }

          }
          var ret = cmr.query('ADDR.GET.INTLCUSTNM4.BY_REQID', {
            REQ_ID : FormManager.getActualValue('reqId')
          });
          if (ret && ret.ret1 && ret.ret1 != '') {
            intlCustNm4ZS01 = ret.ret1;
          }
          
          var busnType = FormManager.getActualValue('busnType');
          var cnName = convert2SBCS(cnCustName1ZS01 + cnCustName2ZS01);
          var scenarioValidation = true;
          if (custSubType == 'INTER' || custSubType == 'CROSS' || custSubType == 'PRIV') {
            scenarioValidation = false;
          }else if(custSubType == 'AQSTN'){
            if(cnName == null || cnName == '' || cnName == '*'){
              scenarioValidation = false;
            }
          }
          if (scenarioValidation){
            var isValidate = false;
            if (FormManager.getActualValue('reqType') == 'U') {
              var intlAddrRdcResult = cmr.query('ADDR.GET.INTLINFO.BY_REQID',  {
                REQ_ID : FormManager.getActualValue('reqId')
              });
              
              var intlCustNm1 = '';
              var intlCustNm2 = '';
              var addrTxt = '';
              var intlCustNm4 = '';
              var city1 = '';
              var city2 = '';
              
              if(!$.isEmptyObject(intlAddrRdcResult)){
                intlCustNm1 = convert2SBCS(intlAddrRdcResult.ret1);
                intlCustNm2 = convert2SBCS(intlAddrRdcResult.ret2);
                addrTxt = convert2SBCS(intlAddrRdcResult.ret3);
                intlCustNm4 = convert2SBCS(intlAddrRdcResult.ret4);
                city1 = convert2SBCS(intlAddrRdcResult.ret5);
                city2 = convert2SBCS(intlAddrRdcResult.ret6);
              }
              
              if(intlCustNm1 != convert2SBCS(cnCustName1ZS01) || intlCustNm2 != convert2SBCS(cnCustName2ZS01) 
                  || addrTxt != convert2SBCS(cnAddrTxtZS01) || intlCustNm4 != convert2SBCS(intlCustNm4ZS01)
                  || city1 != convert2SBCS(cnCityZS01) || city2 != convert2SBCS(cnDistrictZS01)){
                isValidate = true;
              }
            } else if (FormManager.getActualValue('reqType') == 'C') {
              isValidate = true;
            }
            if (isValidate) {
              var busnType = FormManager.getActualValue('busnType');
              var cnName = convert2SBCS(cnCustName1ZS01 + cnCustName2ZS01);
              var result = {};
              dojo.xhrGet({
                url : cmr.CONTEXT_ROOT + '/cn/tyc.json',
                handleAs : 'json',
                method : 'GET',
                content : {
                  busnType : busnType,
                  cnName : cnName
                },
                timeout : 50000,
                sync : true,
                load : function(data, ioargs) {
                  if (data && data.result) {
                    result = data.result;
                  }
                },
                error : function(error, ioargs) {
                  result = {};
                }
              });
      
              var cnAddress = convert2SBCS(cnAddrTxtZS01 + intlCustNm4ZS01);
              var name2SBCS = convert2SBCS(result.name);
              var address2SBCS = convert2SBCS(result.regLocation);
              var apiCity = result.city;
              var apiDistrict = result.district;
              var nameEqualFlag = true;
              var addressEqualFlag = true;

              var correctName = '';
              var correctAddress = '';
              if (name2SBCS != cnName) {
                nameEqualFlag = false;
                if(!$.isEmptyObject(result)){
                  correctName = '<br/>Company Name: ' + result.name;
                } else {
                  correctName = '<br/>Company Name: No Data';
                }
              }
              if (address2SBCS != cnAddress) {
                if (address2SBCS.indexOf(cnAddress) >= 0 && apiCity.indexOf(cnCityZS01) >= 0 && apiDistrict.indexOf(cnDistrictZS01) >= 0){
                  addressEqualFlag = true;
                } else {
                  addressEqualFlag = false;
                  if(!$.isEmptyObject(result)){
                    correctAddress = '<br/>Company Address: ' + result.regLocation;
                  } else {
                    correctAddress = '<br/>Company Address: No Data';
                  }
                }
              }

              if(!nameEqualFlag || !addressEqualFlag){
                var id = FormManager.getActualValue('reqId');
                var ret = cmr.query('CHECK_CN_API_ATTACHMENT', {
                  ID : id
                });
      
                if ((ret == null || ret.ret1 == null)) {
                  return new ValidationResult(null, false, 'Your request are not allowed to send for processing if the Chinese company name '
                      + 'and address doesn\'t match with Tian Yan Cha 100%, or if you insist on using missmatched '
                      + 'company name or address, you need attach the screenshot of customer official website, '
                      + 'business license , government website,contract/purchase order with signature in attachment, '
                      + 'file content must be "Chinese Name And Address change", the correct company name and address '
                      + 'should be:'
                      + correctName + correctAddress);
                } else {
                  return new ValidationResult(null, true);
                }
              } else {
                return new ValidationResult(null, true);
              }
            } else {
              return new ValidationResult(null, true);
            }
          } else {
          return new ValidationResult(null, true);
          }
        }
      }
    }
  })(), 'MAIN_ATTACH_TAB', 'frmCMR');
}

function validateSearchTermForCROSS() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var custSubType = FormManager.getActualValue('custSubGrp');
        var subType = '';
        if (FormManager.getActualValue('reqType') == 'C' && (custSubType == 'CROSS' || custSubType == 'NRML' ||custSubType == 'EMBSA' ||custSubType == 'AQSTN')) {
          if (custSubType == 'CROSS'){
            subType = 'Foreign';
          } else if(custSubType == 'NRML') {
            subType = 'Normal';
          } else if(custSubType == 'EMBSA') {
            subType = 'Embedded Solution Agreement (ESA)';
          } else if(custSubType == 'AQSTN') {
            subType = 'Acquisition';
          }
          var searchTerm = FormManager.getActualValue('searchTerm');
          var searchTermTxt = $('#searchTerm').val();
          if (searchTerm == '00000' || searchTerm == '04182' || searchTerm == '08036') {
            return new ValidationResult(null, false, 'It is not allowed to apply for default search term for ' + subType + ' Sub_scenario.');
          } else if(searchTermTxt.indexOf('Expired') >= 0) { 
            return new ValidationResult(null, false, 'It is not allowed to apply for default or expired search term for ' + subType + ' Sub_scenario.');
          }else {
            return new ValidationResult(null, true);
          }
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function validateISICForCROSS() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var subType = '';
        var custSubType = FormManager.getActualValue('custSubGrp');
        if (FormManager.getActualValue('reqType') == 'C' && (custSubType == 'CROSS' || custSubType == 'NRML' ||custSubType == 'EMBSA' ||custSubType == 'AQSTN')) {
          if (custSubType == 'CROSS'){
            subType = 'Foreign';
          } else if(custSubType == 'NRML') {
            subType = 'Normal';
          } else if(custSubType == 'EMBSA') {
            subType = 'Embedded Solution Agreement (ESA)';
          } else if(custSubType == 'AQSTN') {
            subType = 'Acquisition';
          }
          var isicCd = FormManager.getActualValue('isicCd');
          if (isicCd == '0000' || isicCd == '8888' || isicCd == '9500') {
// FormManager.enable('isicCd');
            return new ValidationResult(null, false, 'It is not allowed to apply for default ISIC for ' + subType + ' Sub_scenario.');
          } else {
            return new ValidationResult(null, true);
          }
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

dojo.addOnLoad(function() {
  GEOHandler.CN = [ SysLoc.CHINA ];
  console.log('adding CN validators...');
  GEOHandler.enableCustomerNamesOnAddress(GEOHandler.CN);
  GEOHandler.setRevertIsicBehavior(false);
  
  GEOHandler.addAfterConfig(afterConfigForCN, GEOHandler.CN);
  GEOHandler.addAfterConfig(setSocialCreditCdValidator, GEOHandler.CN);
  // GEOHandler.addAfterConfig(limitClientTierValuesOnUpdate, GEOHandler.CN);
  GEOHandler.addAfterConfig(defaultCapIndicator, SysLoc.CHINA);
  GEOHandler.addAfterConfig(defaultGovernmentIndicator, GEOHandler.CN);
  // Checklist
  GEOHandler.addAfterConfig(setChinaChecklistStatus, GEOHandler.CN);
  // GEOHandler.addAfterConfig(addValidationForParentCompanyNo, GEOHandler.CN);
  // DENNIS: COMMENTED BECAUSE OF SCRIPT RUN TIME ISSUES
  GEOHandler.addAfterConfig(addDateValidator, GEOHandler.CN);
  GEOHandler.addAfterConfig(hideTDOFields, GEOHandler.CN);
  GEOHandler.addAfterConfig(onInacTypeChange, GEOHandler.CN);
  GEOHandler.addAfterConfig(setCompanyOnInacCd, GEOHandler.CN);
  
  GEOHandler.addAfterTemplateLoad(autoSetIBMDeptCostCenter, GEOHandler.CN);
  // GEOHandler.addAfterTemplateLoad(addValidationForParentCompanyNo,
  // GEOHandler.CN);
  GEOHandler.addAfterTemplateLoad(disableVatExemptForScenarios, GEOHandler.CN);
  GEOHandler.addAfterTemplateLoad(setPrivacyIndcReqdForProc, GEOHandler.CN);
  // GEOHandler.addAfterTemplateLoad(limitClientTierValuesOnCreate,
  // GEOHandler.CN);
  GEOHandler.addAfterTemplateLoad(setValuesForScenarios, GEOHandler.CN);
  GEOHandler.addAfterTemplateLoad(setReadOnlyFields, GEOHandler.CN);
  
  GEOHandler.addAddrFunction(updateMainCustomerNames, GEOHandler.CN);
  GEOHandler.addAddrFunction(autoSetAddrFieldsForCN, GEOHandler.CN);
  GEOHandler.addAddrFunction(showHideCityCN, GEOHandler.CN);
  GEOHandler.addAddrFunction(addMandatoryOnlyForZS01CN, GEOHandler.CN);
  GEOHandler.addAddrFunction(addDoubleByteValidatorCN, GEOHandler.CN);
  
  GEOHandler.addToggleAddrTypeFunction(convertCnCustName1, GEOHandler.CN);
  GEOHandler.addToggleAddrTypeFunction(convertCnCustName2, GEOHandler.CN);
  GEOHandler.addToggleAddrTypeFunction(convertCnCustName3, GEOHandler.CN);
  GEOHandler.addToggleAddrTypeFunction(convertCnAddrTxt, GEOHandler.CN);
  GEOHandler.addToggleAddrTypeFunction(convertCnAddrTxt2, GEOHandler.CN);
  GEOHandler.addToggleAddrTypeFunction(convertCnDistrict, GEOHandler.CN);
  GEOHandler.addToggleAddrTypeFunction(convertCustNm1, GEOHandler.CN);
  GEOHandler.addToggleAddrTypeFunction(convertCustNm2, GEOHandler.CN);
  GEOHandler.addToggleAddrTypeFunction(convertCustNm3, GEOHandler.CN); 
  GEOHandler.addToggleAddrTypeFunction(convertAddrTxt, GEOHandler.CN);
  GEOHandler.addToggleAddrTypeFunction(convertAddrTxt2, GEOHandler.CN);
  GEOHandler.addToggleAddrTypeFunction(convertCity2, GEOHandler.CN);
  GEOHandler.addToggleAddrTypeFunction(convertDept, GEOHandler.CN);
  GEOHandler.addToggleAddrTypeFunction(convertBldg, GEOHandler.CN);
  GEOHandler.addToggleAddrTypeFunction(convertOffice, GEOHandler.CN);
  GEOHandler.addToggleAddrTypeFunction(convertPoBox, GEOHandler.CN);
  
  GEOHandler.registerValidator(addDPLCheckValidator, GEOHandler.CN, GEOHandler.ROLE_REQUESTER, false, false);
  GEOHandler.registerValidator(addGenericVATValidator(SysLoc.CHINA, 'MAIN_CUST_TAB', 'frmCMR'), [ SysLoc.CHINA ], null, true);
  GEOHandler.registerValidator(addChecklistValidator, GEOHandler.CN);
// GEOHandler.registerValidator(isValidDate,GEOHandler.CN);
  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.CN, GEOHandler.REQUESTER, false, false);
  GEOHandler.registerValidator(addFastPassAttachmentValidator, GEOHandler.CN, GEOHandler.REQUESTER, false, false);
  GEOHandler.registerValidator(setTDOFlagToYesValidator, GEOHandler.CN, GEOHandler.PROCESSOR, false, false);
  GEOHandler.registerValidator(addSoltToAddressValidator, GEOHandler.CN, null, false, false);
  GEOHandler.registerValidator(addContactInfoValidator, GEOHandler.CN, GEOHandler.REQUESTER, false, false);
  GEOHandler.registerValidator(addCityRequiredOnUpdateValidatorAddrList, GEOHandler.CN, null, true);
  GEOHandler.registerValidator(addSocialCreditCdLengthValidator, GEOHandler.CN, GEOHandler.REQUESTER, true);
  GEOHandler.registerValidator(addAddrUpdateValidator, GEOHandler.CN, null, true);
  GEOHandler.registerValidator(validateCnNameAndAddr, GEOHandler.CN, null, false);
  GEOHandler.registerValidator(addPRIVCustNameValidator, GEOHandler.CN, null, false, false);
  GEOHandler.registerValidator(addPRIVCustNameSFPValidator, GEOHandler.CN, null, false, false);
  GEOHandler.registerValidator(validateEnNameForInter, GEOHandler.CN, null, false);
  GEOHandler.registerValidator(validateEnNameInAddrTab, GEOHandler.CN, null, false, false);
  GEOHandler.registerValidator(validateSearchTermForCROSS, GEOHandler.CN, null, false);
  GEOHandler.registerValidator(validateISICForCROSS, GEOHandler.CN, null, false);
});