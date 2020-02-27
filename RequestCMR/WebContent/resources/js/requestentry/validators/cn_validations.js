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
var _govTypeHandler = null;
var _goeTypeHandler = null;
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
  if (_searchTermHandler == null) {
    _searchTermHandler = dojo.connect(FormManager.getField('searchTerm'), 'onChange', function(value) {
      console.log(">>> RUNNING SORTL HANDLER!!!!");
      if (!value) {
        return;
      }
      filterISUOnChange();
      addValidationForParentCompanyNo();
    });
  }

  if (_vatExemptHandler == null) {
    _vatExemptHandler = dojo.connect(FormManager.getField('vatExempt'), 'onClick', function(value) {
      console.log(">>> RUNNING!!!!");
      if (dijit.byId('vatExempt').get('checked')) {
        console.log(">>> Process vatExempt remove * >> ");
        FormManager.resetValidations('vat');
      } else {
        console.log(">>> Process vatExempt add * >> ");
        FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
      }
    });
  }

  if (_govTypeHandler == null) {
    _govTypeHandler = dojo.connect(FormManager.getField('govType'), 'onClick', function(value) {
      addValidationForParentCompanyNo();
    });
  }

  if (_goeTypeHandler == null) {
    _goeTypeHandler = dojo.connect(FormManager.getField('goeType'), 'onClick', function(value) {
      addValidationForParentCompanyNo();
    });
  }

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
      FormManager.hide('RdcComment', 'rdcComment');
      FormManager.resetValidations('rdcComment');
    } else {
      FormManager.readOnly('custClass');
    }
  }

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
  if (_govTypeHandler && _govTypeHandler[0]) {
    _govTypeHandler[0].onChange();
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
    FormManager.getField('capInd').set('checked', true);
    FormManager.readOnly('capInd');
  }
}

function disableVatExemptForScenarios() {
  var _custSubGrp = FormManager.getActualValue('custSubGrp');
  // setValuesForScenarios();
  if (FormManager.getActualValue('reqType') == 'C' && _custSubGrp != 'undefined' && _custSubGrp != '') {
    if (_custSubGrp != 'COMME' && _custSubGrp != 'GOVMT' && _custSubGrp != 'BROKR' && _custSubGrp != 'CROSS') {
      FormManager.disable('vatExempt');
    } else {
      FormManager.enable('vatExempt');
      autoSetTax();
    }
  }
}
function autoSetTax() {
  var reqType = null;
  reqType = FormManager.getActualValue('reqType');
  if (reqType != 'C') {
    return;
  }
  if (dijit.byId('vatExempt').get('checked')) {
    FormManager.resetValidations('vat');
  } else {
    FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_CUST_TAB');
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
  console.log(isuCdResult);

  if (isuCdResult != null) {
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
    FormManager.hide('RdcComment', 'rdcComment');
    FormManager.resetValidations('rdcComment');
  }

  if (FormManager.getActualValue('reqType') == 'C' && _custSubGrp != 'undefined' && _custSubGrp != '') {
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
      if (_custSubGrp == 'NRML' || _custSubGrp == 'INTER' || _custSubGrp == 'AQSTN' || _custSubGrp == 'EMBSA' || _custSubGrp == 'CROSS') {
        FormManager.hide('PPSCEID', 'ppsceid');
        FormManager.hide('MembLevel', 'memLvl');
        FormManager.hide('BPRelationType', 'bpRelType');
      } else {
        FormManager.show('PPSCEID', 'ppsceid');
        FormManager.show('MembLevel', 'memLvl');
        FormManager.show('BPRelationType', 'bpRelType');
      }

      if (_custSubGrp == 'AQSTN' || _custSubGrp == 'CROSS') {
        FormManager.resetValidations('cnCustName1');
        FormManager.resetValidations('cnAddrTxt');
        FormManager.resetValidations('cnCity');
      }
    }

    if (_pagemodel.userRole.toUpperCase() == "PROCESSOR") {
      if (_custSubGrp == 'NRML' || _custSubGrp == 'AQSTN' || _custSubGrp == 'EMBSA' || _custSubGrp == 'CROSS') {
        FormManager.setValue('custClass', '11');
        FormManager.readOnly('custClass');
      }
      if (_custSubGrp == 'BUSPR') {
        FormManager.setValue('custClass', '45');
        FormManager.readOnly('custClass');
      }
      if (_custSubGrp == 'INTER') {
        var field = FormManager.getField('custClass');
        FormManager.limitDropdownValues(field, [ '81', '85' ]);
        field.set('value', '81');
      }
      if (_custSubGrp == 'NRML' || _custSubGrp == 'BUSPR' || _custSubGrp == 'INTER' || _custSubGrp == 'EMBSA') {
        FormManager.setValue('cnInterAddrKey', '6');
        FormManager.addValidator('cnInterAddrKey', Validators.REQUIRED, [ 'InterAddrKey' ], '');
        FormManager.readOnly('cnInterAddrKey');
      } else {
        FormManager.resetValidations('cnInterAddrKey');
      }
      if (_custSubGrp == 'AQSTN' || _custSubGrp == 'EMBSA') {
        FormManager.addValidator('rdcComment', Validators.REQUIRED, [ 'RdcComment' ], '');
        if (_custSubGrp == 'EMBSA') {
          FormManager.setValue('rdcComment', 'For ESA Use Only');
        } else if (_custSubGrp == 'AQSTN') {
          if (FormManager.getActualValue('custSubGrp') != _pagemodel.custSubGrp) {
            FormManager.setValue('rdcComment', 'Acquisition');
          }
        }
      } else {
        FormManager.resetValidations('rdcComment');
      }
    }
  }
}

function hideContactInfoFields() {
  var addrType = FormManager.getActualValue('addrType');

  if (addrType != '' && addrType == 'ZS01') {
    FormManager.show('CustomerCntPhone2', 'cnCustContPhone2');
    FormManager.show('CustomerCntJobTitle', 'cnCustContJobTitle');
    FormManager.show('ChinaCustomerCntName', 'cnCustContNm');
  } else {
    FormManager.hide('CustomerCntPhone2', 'cnCustContPhone2');
    FormManager.hide('CustomerCntJobTitle', 'cnCustContJobTitle');
    FormManager.hide('ChinaCustomerCntName', 'cnCustContNm');

    FormManager.resetValidations('cnCustContJobTitle');
    FormManager.resetValidations('cnCustContNm');
  }
}

function autoSetAddrFieldsForCN() {
  if (cmr.addressMode == 'newAddress' || cmr.addressMode == 'copyAddress') {
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
      FormManager.show('CustomerCntPhone2', 'cnCustContPhone2');
      FormManager.show('CustomerCntJobTitle', 'cnCustContJobTitle');
      FormManager.show('ChinaCustomerCntName', 'cnCustContNm');
    } else {
      FormManager.hide('CustomerCntPhone2', 'cnCustContPhone2');
      FormManager.hide('CustomerCntJobTitle', 'cnCustContJobTitle');
      FormManager.hide('ChinaCustomerCntName', 'cnCustContNm');

      FormManager.resetValidations('cnCustContJobTitle');
      FormManager.resetValidations('cnCustContNm');
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
      //
      FormManager.resetValidations('custPhone');
      FormManager.resetValidations('cnCustContJobTitle');
      FormManager.resetValidations('cnCustContNm');
    } else {
      FormManager.addValidator('cnCity', Validators.REQUIRED, [ 'City Chinese' ], null);
      FormManager.addValidator('cnAddrTxt', Validators.REQUIRED, [ 'Street Address Chinese' ], null);
      FormManager.addValidator('cnCustName1', Validators.REQUIRED, [ 'Customer Name Chinese' ], null);
      FormManager.addValidator('stateProv', Validators.REQUIRED, [ 'State/Province' ], null);
      //
      FormManager.addValidator('custPhone', Validators.REQUIRED, [ "Customer Contact's Phone Number 2" ], null);
      FormManager.addValidator('cnCustContJobTitle', Validators.REQUIRED, [ "Customer Contact's Job Title" ], null);
      FormManager.addValidator('cnCustContNm', Validators.REQUIRED, [ "Customer Contact's Name (include salutation)" ], null);
    }
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

function addContactInfoValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var custSubType = FormManager.getActualValue('custSubGrp');
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0 && FormManager.getActualValue('reqType') == 'C'
            && (custSubType == 'EMBSA' || custSubType == 'NRML' || custSubType == 'CROSS')) {
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
  if (custSubType == 'NRML') {
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

dojo.addOnLoad(function() {
  GEOHandler.CN = [ SysLoc.CHINA ];
  console.log('adding CN validators...');
  GEOHandler.addAfterConfig(afterConfigForCN, GEOHandler.CN);
  // GEOHandler.addAfterConfig(autoSetTax, GEOHandler.CN);
  GEOHandler.addAddrFunction(updateMainCustomerNames, GEOHandler.CN);
  GEOHandler.registerValidator(addDPLCheckValidator, GEOHandler.CN, GEOHandler.ROLE_REQUESTER, false, false);
  GEOHandler.enableCustomerNamesOnAddress(GEOHandler.CN);
  GEOHandler.addAfterTemplateLoad(autoSetIBMDeptCostCenter, GEOHandler.CN);
  GEOHandler.addAfterTemplateLoad(addValidationForParentCompanyNo, GEOHandler.CN);
  // GEOHandler.addAfterTemplateLoad(disableVatExemptForScenarios,
  // GEOHandler.CN);
  GEOHandler.addAfterTemplateLoad(setPrivacyIndcReqdForProc, GEOHandler.CN);
  // GEOHandler.addAfterTemplateLoad(limitClientTierValuesOnCreate,
  // GEOHandler.CN);
  GEOHandler.setRevertIsicBehavior(false);
  GEOHandler.addAfterTemplateLoad(setValuesForScenarios, GEOHandler.CN);
  // GEOHandler.addAfterConfig(limitClientTierValuesOnUpdate, GEOHandler.CN);
  GEOHandler.registerValidator(addGenericVATValidator(SysLoc.CHINA, 'MAIN_CUST_TAB', 'frmCMR'), [ SysLoc.CHINA ], null, true);
  GEOHandler.addAfterConfig(defaultCapIndicator, SysLoc.CHINA);
  GEOHandler.addAddrFunction(autoSetAddrFieldsForCN, GEOHandler.CN);
  GEOHandler.addAddrFunction(showHideCityCN, GEOHandler.CN);
  // Checklist
  GEOHandler.addAfterConfig(setChinaChecklistStatus, GEOHandler.CN);
  GEOHandler.addAfterConfig(addValidationForParentCompanyNo, GEOHandler.CN);
  GEOHandler.registerValidator(addChecklistValidator, GEOHandler.CN);
  // DENNIS: COMMENTED BECAUSE OF SCRIPT RUN TIME ISSUES
  GEOHandler.addAfterConfig(addDateValidator, GEOHandler.CN);
  // GEOHandler.registerValidator(isValidDate,GEOHandler.CN);
  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.CN, GEOHandler.REQUESTER, false, false);
  GEOHandler.registerValidator(addFastPassAttachmentValidator, GEOHandler.CN, GEOHandler.REQUESTER, false, false);
  GEOHandler.addAfterConfig(hideTDOFields, GEOHandler.CN);
  GEOHandler.addAfterTemplateLoad(setReadOnlyFields, GEOHandler.CN);
  GEOHandler.registerValidator(setTDOFlagToYesValidator, GEOHandler.CN, GEOHandler.PROCESSOR, false, false);
  GEOHandler.registerValidator(addSoltToAddressValidator, GEOHandler.CN, null, false, false);
  GEOHandler.registerValidator(addContactInfoValidator, GEOHandler.CN, GEOHandler.REQUESTER, false, false);
  GEOHandler.registerValidator(addCityRequiredOnUpdateValidatorAddrList, GEOHandler.CN, null, true);

});