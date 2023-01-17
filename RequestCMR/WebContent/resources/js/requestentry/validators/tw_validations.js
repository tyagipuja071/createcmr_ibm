/* Register TW Javascripts */

function afterConfigTW() {
  var reqType = FormManager.getActualValue('reqType');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var custGrp = FormManager.getActualValue('custGrp');
  var invoiceSplitCd = FormManager.getActualValue('invoiceSplitCd');
  var custPrefLang = FormManager.getField('custPrefLang');
  var collectionCd = FormManager.getField('collectionCd');
  var role = null;
  var taxLocation = null;
  FormManager.readOnly('capInd');
  FormManager.setValue('capInd', true);
  FormManager.readOnly('cmrOwner');
  FormManager.resetValidations('enterprise');
  FormManager.readOnly('isuCd');
  // FormManager.removeValidator('affiliate', Validators.DIGIT);

  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
    taxLocation = _pagemodel.mktgDept;
  }

  if (!FormManager.getField('reqType') || reqType == '') {
    window.setTimeout('afterConfigTW()', 500);
  } else {
    if (role == 'Processor' && reqType == 'C') {
      FormManager.addValidator('cmrNo', Validators.REQUIRED, [ 'CMR Number' ], 'MAIN_IBM_TAB');
    }
    if (!FormManager.getField('custPrefLang') || custPrefLang == '') {
      FormManager.setValue('custPrefLang', 'M');
    }
    if (reqType == 'C') {
      if (!FormManager.getField('collectionCd') || collectionCd == '') {
        FormManager.setValue('collectionCd', '00FO');
      }
      FormManager.addValidator('custAcctType', Validators.REQUIRED, [ 'Custome Type' ], 'MAIN_CUST_TAB');
      FormManager.addValidator('mktgDept', Validators.REQUIRED, [ 'Tax Location' ], 'MAIN_CUST_TAB');
    } else if (reqType == 'U') {
      if (taxLocation == null || taxLocation == '') {
        FormManager.clearValue('mktgDept');
      }
      FormManager.removeValidator('vat', Validators.REQUIRED);
      FormManager.removeValidator('mktgDept', Validators.REQUIRED);
      FormManager.removeValidator('invoiceSplitCd', Validators.REQUIRED);
      FormManager.removeValidator('custAcctType', Validators.REQUIRED);
      FormManager.removeValidator('abbrevLocn', Validators.REQUIRED);
      FormManager.removeValidator('orgNo', Validators.REQUIRED);
      FormManager.removeValidator('restrictTo', Validators.REQUIRED);
      // FormManager.removeValidator('commercialFinanced', Validators.REQUIRED);
      // FormManager.removeValidator('csBo', Validators.REQUIRED);
      // FormManager.removeValidator('sectorCd', Validators.REQUIRED);
      // FormManager.removeValidator('busnType', Validators.REQUIRED);

      FormManager.removeValidator('footnoteTxt1', Validators.REQUIRED);
      // FormManager.removeValidator('bioChemMissleMfg', Validators.REQUIRED);
      // FormManager.removeValidator('contactName2', Validators.REQUIRED);
      // FormManager.removeValidator('email1', Validators.REQUIRED);
      // FormManager.removeValidator('contactName1', Validators.REQUIRED);
      // FormManager.removeValidator('footnoteTxt2', Validators.REQUIRED);
      FormManager.removeValidator('contactName3', Validators.REQUIRED);
      // FormManager.removeValidator('email2', Validators.REQUIRED);
      FormManager.removeValidator('company', Validators.REQUIRED);
      // FormManager.removeValidator('affiliate', Validators.REQUIRED);
      FormManager.removeValidator('email3', Validators.REQUIRED);
      FormManager.removeValidator('customerIdCd', Validators.REQUIRED);
      FormManager.removeValidator('inacType', Validators.REQUIRED);
      FormManager.removeValidator('inacCd', Validators.REQUIRED);
      FormManager.removeValidator('covId', Validators.REQUIRED);
      FormManager.removeValidator('dunsNo', Validators.REQUIRED);
      FormManager.removeValidator('cmrNoPrefix', Validators.REQUIRED);
      // FormManager.removeValidator('bpName', Validators.REQUIRED);
      FormManager.removeValidator('collectionCd', Validators.REQUIRED);
      FormManager.removeValidator('mrcCd', Validators.REQUIRED);

      FormManager.removeValidator('dupCmrIndc', Validators.REQUIRED);
      FormManager.removeValidator('bgId', Validators.REQUIRED);
      FormManager.removeValidator('gbgId', Validators.REQUIRED);
      FormManager.removeValidator('bgRuleId', Validators.REQUIRED);
      FormManager.removeValidator('geoLocationCd', Validators.REQUIRED);

      FormManager.removeValidator('sitePartyId', Validators.REQUIRED);
      FormManager.removeValidator('isuCd', Validators.REQUIRED);
      FormManager.removeValidator('clientTier', Validators.REQUIRED);
      FormManager.removeValidator('searchTerm', Validators.REQUIRED);
    }
    // CREATCMR-6823
    FormManager.removeValidator('footnoteTxt1', Validators.REQUIRED);
    FormManager.removeValidator('contactName3', Validators.REQUIRED);
    FormManager.removeValidator('email3', Validators.REQUIRED);
    FormManager.removeValidator('orgNo', Validators.REQUIRED);
    FormManager.removeValidator('restrictTo', Validators.REQUIRED);
  }

  if (custSubGrp == 'LOECO' || custSubGrp == 'LOINT' || custSubGrp == 'LOBLU' || custSubGrp == 'LOMAR' || custSubGrp == 'LOOFF') {
    FormManager.addValidator('searchTerm', Validators.REQUIRED, [ 'Cluster ID' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('clientTier', Validators.REQUIRED, [ 'GB Segment' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('mrcCd', Validators.REQUIRED, [ 'Market Responsibility Code (MRC)' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('repTeamMemberNo', Validators.REQUIRED, [ 'Sales Rep No' ], 'MAIN_IBM_TAB');
  }
  if (custSubGrp == 'CBFOR') {
    FormManager.addValidator('invoiceSplitCd', Validators.REQUIRED, [ 'Tax Type' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('abbrevLocn', Validators.REQUIRED, [ 'Customer Location' ], 'MAIN_CUST_TAB');
  }
  if (custSubGrp == 'LOINT') {
    FormManager.addValidator('isicCd', Validators.REQUIRED, [ 'ISIC' ], 'MAIN_CUST_TAB');
  }
  if (reqType == 'C' && custSubGrp == 'ECOSY') {
    FormManager.readOnly('searchTerm');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('mrcCd');
    FormManager.setValue('apCustClusterId', '08043');
    FormManager.setValue('clientTier', 'Y');
    FormManager.setValue('isuCd', '36');
    FormManager.setValue('mrcCd', '3');
  }
  if (reqType == 'C' && custSubGrp == 'LOECO') {
    FormManager.readOnly('searchTerm');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('mrcCd');
  }

  setVatValidator();
  handleObseleteExpiredDataForUpdate();
  // CREATCMR-788
  addressQuotationValidator();
}

/**
 * After config handlers
 */
var _taxTypeHandler = null;
var _ISICHandler = null;
var _dupCmrIndcHandler = null;
var _ISUHandler = null;
var _searchTermHandler = null;
var _inacCdHandler = null;

function addHandlersForTW() {
  if (_taxTypeHandler == null) {
    _taxTypeHandler = dojo.connect(FormManager.getField('invoiceSplitCd'), 'onChange', function(value) {
      setVatValidator();
    });
  }

  if (_dupCmrIndcHandler == null) {
    _dupCmrIndcHandler = dojo.connect(FormManager.getField('dupCmrIndc'), 'onChange', function(value) {
      setDupCmrIndcWarning();
    });
  }

  if (_ISUHandler == null) {
    _isuHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
      setClientTierValuesTW();
    });
  }

  // CREATCMR-7882
  if (_ISICHandler == null) {
    _ISICHandler = dojo.connect(FormManager.getField('isicCd'), 'onChange', function(value) {
      setISUCodeValues();
    });
  }

  if (_searchTermHandler == null) {
    _searchTermHandler = dojo.connect(FormManager.getField('searchTerm'), 'onChange', function(value) {
      console.log(">>> RUNNING SORTL HANDLER!!!!");
      if (!value) {
        return;
      }
      setISUCodeValues();
      filterISUOnChange();
      setInacBySearchTerm();
    });
  }
}

// CREATCMR -5269
function handleObseleteExpiredDataForUpdate() {
  var reqType = FormManager.getActualValue('reqType');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  if (reqType != 'U' || FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  // lock all the coverage fields and remove validator
  if (reqType == 'U') {
    FormManager.readOnly('searchTerm');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('mrcCd');
    FormManager.readOnly('inacType');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('inacCd');
    FormManager.readOnly('repTeamMemberNo');
    FormManager.readOnly('covId');
    FormManager.readOnly('collectionCd');
    FormManager.readOnly('cmrNoPrefix');
    FormManager.readOnly('dupCmrIndc');
    FormManager.readOnly('sitePartyId');
    FormManager.readOnly('bgId');
    FormManager.readOnly('gbgId');
    FormManager.readOnly('bgRuleId');
    FormManager.readOnly('geoLocationCd');
    FormManager.readOnly('dunsNo');
    FormManager.readOnly('customerIdCd');

    FormManager.removeValidator('searchTerm', Validators.REQUIRED);
    FormManager.removeValidator('clientTier', Validators.REQUIRED);
    FormManager.removeValidator('isuCd', Validators.REQUIRED);
    FormManager.removeValidator('mrcCd', Validators.REQUIRED);
    FormManager.removeValidator('inacType', Validators.REQUIRED);
    FormManager.removeValidator('inacCd', Validators.REQUIRED);
    FormManager.removeValidator('repTeamMemberNo', Validators.REQUIRED);
    FormManager.removeValidator('dupCmrIndc', Validators.REQUIRED);
    FormManager.removeValidator('cmrNoPrefix', Validators.REQUIRED);
    FormManager.removeValidator('covId', Validators.REQUIRED);
    FormManager.removeValidator('collectionCd', Validators.REQUIRED);
    FormManager.removeValidator('sitePartyId', Validators.REQUIRED);
    FormManager.removeValidator('bgId', Validators.REQUIRED);
    FormManager.removeValidator('gbgId', Validators.REQUIRED);
    FormManager.removeValidator('geoLocationId', Validators.REQUIRED);
    FormManager.removeValidator('dunsNo', Validators.REQUIRED);
    FormManager.removeValidator('bgRuleId', Validators.REQUIRED);
    FormManager.removeValidator('customerIdCd', Validators.REQUIRED);
  }
}

/*
 * Set CMR Double Creation warning when its value is Y
 */
function setDupCmrIndcWarning() {
  var dupCmrIndc = FormManager.getActualValue('dupCmrIndc');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (custSubGrp == null) {
    return;
  }
  var duplicateCMR = $('#duplicateCMR');
  if (duplicateCMR != null) {
    if (dupCmrIndc == 'Y') {
      $('#duplicateCMR').text('Singapore Offshore CMR request must be submitted also in CreateCMR');
      $('#duplicateCMR').css({
        "color" : "red",
        "font-weight" : "bold"
      });
    } else {
      $('#duplicateCMR').text('');
    }
  }
}

function setClientTierValuesTW() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  isuCd = FormManager.getActualValue('isuCd');
  if (isuCd == '5K') {
    FormManager.removeValidator('clientTier', Validators.REQUIRED);
  } else {
    FormManager.enable('clientTier');
  }
  handleObseleteExpiredDataForUpdate();
}

/*
 * Set ISU Code Values based On ISIC
 */
function setISUCodeValues() {

  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }

  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var isicCd = FormManager.getActualValue('isicCd');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var isuCd = FormManager.getActualValue('isuCd');

  if (custSubGrp == '') {
    return;
  }

  if ((isicCd == _pagemodel.isicCd) && (custSubGrp == _pagemodel.custSubGrp)) {
    return;
  }

  var isuCode = [];

  if (cntry == '858') {
    if (isicCd != '') {
      var qParams = {
        _qall : 'Y',
        ISSUING_CNTRY : cntry,
        REP_TEAM_CD : '%' + isicCd + '%'
      };
      var results = cmr.query('GET.ICLIST.BYISIC', qParams);
      if (results != null) {
        for (var i = 0; i < results.length; i++) {
          isuCode.push(results[i].ret1);
        }
        if (isuCode != null) {
          FormManager.limitDropdownValues(FormManager.getField('isuCd'), isuCode);

          if (isuCode.length == 1) {
            FormManager.setValue('isuCd', isuCode[0]);
          }
        }
      }
    }
  }
}

/* Unify Number Handler */
function setVatValidator() {
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  var invoiceSplitCd = FormManager.getActualValue('invoiceSplitCd');
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  if (invoiceSplitCd == 'TN') {
    FormManager.removeValidator('vat', Validators.REQUIRED);
  } else {
    FormManager.addValidator('vat', Validators.REQUIRED, [ 'Unify Number' ], 'MAIN_CUST_TAB');
  }
}
function setChecklistStatus() {
  console.log('validating checklist..');
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

function addTWChecklistValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        console.log('validating checklist..');
        var checklist = dojo.query('table.checklist');
        var questions = checklist.query('input[type="radio"]');
        if (questions.length > 0) {
          var noOfQuestions = questions.length / 2;
          var checkCount = 0;
          for (var i = 0; i < questions.length; i++) {
            if (questions[i].checked) {
              checkCount++;
            }
          }
          if (noOfQuestions != checkCount) {
            return new ValidationResult(null, false, 'Checklist has not been fully accomplished. All items are required.');
          }
          // add check for checklist on DB
          var reqId = FormManager.getActualValue('reqId');
          var record = cmr.getRecord('GBL_CHECKLIST', 'ProlifChecklist', {
            REQID : reqId
          });
          if (!record || !record.sectionA1) {
            return new ValidationResult(null, false, 'Checklist has not been registered yet. Please execute a \'Save\' action before sending for processing to avoid any data loss.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CHECKLIST_TAB', 'frmCMR');
}

function addSingleByteValidatorTW(cntry, details) {

  /* Address */
  FormManager.addValidator('custNm1', Validators.LATIN, [ 'Customer English Name' ]);
  FormManager.addValidator('custNm2', Validators.LATIN, [ 'Customer English Name Con' + '\'' + 't' ]);
  FormManager.addValidator('custNm3', Validators.NO_SINGLE_BYTE, [ 'Customer Chinese Name' ]);
  FormManager.addValidator('custNm4', Validators.NO_SINGLE_BYTE, [ 'Customer Chinese Name Con' + '\'' + 't' ]);
  FormManager.addValidator('addrTxt', Validators.LATIN, [ 'Customer English Address' ]);
  FormManager.addValidator('addrTxt2', Validators.LATIN, [ 'Customer English Address Con' + '\'' + 't' ]);
  FormManager.addValidator('dept', Validators.NO_SINGLE_BYTE, [ 'Customer Chinese Address' ]);
  FormManager.addValidator('bldg', Validators.NO_SINGLE_BYTE, [ 'Customer Chinese Address Con' + '\'' + 't' ]);

  /* Customer */
  // FormManager.addValidator('footnoteTxt2', Validators.NO_SINGLE_BYTE, [
  // 'Chief Executive Officer Name' ], 'MAIN_CUST_TAB');
  // FormManager.addValidator('busnType', Validators.NO_SINGLE_BYTE, [ 'Chief
  // Executive Officer Job Title' ], 'MAIN_CUST_TAB');
  // FormManager.addValidator('bioChemMissleMfg', Validators.LATIN, [ 'Chief
  // Executive Officer Telephone' ], 'MAIN_CUST_TAB');
  // FormManager.addValidator('email1', Validators.LATIN, [ 'Chief Executive
  // Officer Email' ], 'MAIN_CUST_TAB');
  // FormManager.addValidator('contactName2', Validators.LATIN, [ 'Chief
  // Executive Officer Fax' ], 'MAIN_CUST_TAB');
  // FormManager.addValidator('contactName1', Validators.NO_SINGLE_BYTE, [
  // 'Chief Information Officer Name' ], 'MAIN_CUST_TAB');
  // FormManager.addValidator('bpName', Validators.NO_SINGLE_BYTE, [ 'Chief
  // Information Officer Job Title' ], 'MAIN_CUST_TAB');
  // FormManager.addValidator('affiliate', Validators.LATIN, [ 'Chief
  // Information Officer Telephone' ], 'MAIN_CUST_TAB');
  // FormManager.addValidator('email2', Validators.LATIN, [ 'Chief Information
  // Officer Email' ], 'MAIN_CUST_TAB');
  // FormManager.addValidator('commercialFinanced', Validators.LATIN, [ 'Chief
  // Information Officer Fax' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('footnoteTxt1', Validators.NO_SINGLE_BYTE, [ 'Contact Name' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('contactName3', Validators.NO_SINGLE_BYTE, [ 'Contact Job Title' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('email3', Validators.LATIN, [ 'Goods Receiver Telephone Number' ], 'MAIN_CUST_TAB');

  FormManager.addValidator('orgNo', Validators.LATIN, [ 'Customer Telephone Number' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('restrictTo', Validators.LATIN, [ 'Customer Fax Number' ], 'MAIN_CUST_TAB');

}

function setAbbrevNmLocnOnAddressSave(cntry, addressMode, saving, finalSave, force) {
  console.log(">>>> setAbbrevNmLocnOnAddressSave >>>>");
  var reqType = null;
  var cmrCntry = FormManager.getActualValue('cmrIssuingCntry');
  if (typeof (_pagemodel) != 'undefined') {
    reqType = FormManager.getActualValue('reqType');
  }
  var addrType = FormManager.getActualValue('addrType');
  var copyTypes = document.getElementsByName('copyTypes');
  // var copyingToA = false;
  if (addrType == 'ZS01') {
    // copyingToA = true;
    autoSetAbbrevNmLocnLogic();
  }
}

function autoSetAbbrevNmLocnLogic() {
  console.log("autoSetAbbrevNmLocnLogic");
  var _abbrevNm = null;
  var custSubGrp = FormManager.getActualValue('custSubGrp');

  var zs01ReqId = FormManager.getActualValue('reqId');
  var qParams = {
    REQ_ID : zs01ReqId,
  };
  var result = cmr.query('ADDR.GET.CUSTNM1.BY_REQID', qParams);
  var custNm1 = FormManager.getActualValue('custNm1');

  if (custNm1 == '') {
    custNm1 = result.ret1;
  }
  _abbrevNm = custNm1;

  if (_abbrevNm && _abbrevNm.length > 21) {
    _abbrevNm = _abbrevNm.substring(0, 21);
  }
  FormManager.setValue('abbrevNm', _abbrevNm);

}
// CREATCMR-6825
function setRepTeamMemberNo() {
  var reqType = FormManager.getActualValue('reqType');
  if (reqType == 'C') {
    FormManager.setValue('repTeamMemberNo', '000000');
    FormManager.readOnly('repTeamMemberNo');
  }
}
function addressQuotationValidator() {
  // CREATCMR-788
  FormManager.addValidator('abbrevNm', Validators.NO_QUOTATION, [ 'Customer Abbreviated Name' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('abbrevLocn', Validators.NO_QUOTATION, [ 'Customer Location' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('custNm1', Validators.NO_QUOTATION, [ 'Customer English Name' ]);
  FormManager.addValidator('custNm2', Validators.NO_QUOTATION, [ 'Customer English Name Con\'t' ]);
  FormManager.addValidator('custNm3', Validators.NO_QUOTATION, [ 'Customer Chinese Name' ]);
  FormManager.addValidator('custNm4', Validators.NO_QUOTATION, [ 'Customer Chinese Name Con\'t' ]);
  FormManager.addValidator('addrTxt', Validators.NO_QUOTATION, [ 'Customer English Address' ]);
  FormManager.addValidator('addrTxt2', Validators.NO_QUOTATION, [ 'Customer English Address Con\'t' ]);
  FormManager.addValidator('dept', Validators.NO_QUOTATION, [ 'Customer Chinese Address' ]);
  FormManager.addValidator('bldg', Validators.NO_QUOTATION, [ 'Customer Chinese Address Con\'t' ]);
  FormManager.addValidator('postCd', Validators.NO_QUOTATION, [ 'Postal Code' ]);

}

// CREATCMR-7882
function filterISUOnChange() {
  if (FormManager.getActualValue('reqType') != 'C' || FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  console.log(">>> RUNNING filterISUOnChange!!");
  var searchTerm = FormManager.getActualValue('searchTerm');
  var searchTermParams = {
    SORTL : searchTerm,
    KATR6 : '858'
  };
  var isuCdResult = cmr.query('GET.MAPPED_ISU_BY_SORTL', searchTermParams);

  if (isuCdResult != null && isuCdResult.ret2 != '') {
    FormManager.resetDropdownValues(FormManager.getField('isuCd'));
    FormManager.setValue('isuCd', isuCdResult.ret2);
  } else {
    FormManager.enable('isuCd');
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

   if (_pagemodel.userRole.toUpperCase() == "PROCESSOR") {
   console.log("Enabling isuCd for PROCESSOR...");
   FormManager.enable('isuCd');
   }
  
   if (_pagemodel.userRole.toUpperCase() == "REQUESTER") {
   console.log("Disabling isuCd for REQUESTER...");
   FormManager.readOnly('isuCd');
   }
}

function setInacBySearchTerm() {
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var _cluster = FormManager.getActualValue('searchTerm');
  if (FormManager.getActualValue('reqType') != 'C' || FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (!_cluster) {
    return;
  }
  if (_cluster == '09061' || _cluster == '04476' || _cluster == '10179' || _cluster == '10180' || _cluster == '10181' 
    || _cluster == '10182' || _cluster == '10183' || _cluster == '10184' || _cluster == '10185' || _cluster == '10186' 
      || _cluster == '10187') {
    FormManager.addValidator('inacCd', Validators.REQUIRED, [ 'INAC/NAC Code' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('inacType', Validators.REQUIRED, [ 'INAC Type' ], 'MAIN_IBM_TAB');
    if (_cluster == '04476') {
      FormManager.setValue('mrcCd', '2');
    } else {
      FormManager.setValue('mrcCd', '3');
    }
    var qParams = {
      _qall : 'Y',
      ISSUING_CNTRY : cntry,
      CMT : '%' + _cluster + '%'
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
        var cmt = value + ','+ _cluster +'%';
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
            if (results != null) {
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
  } else {
    FormManager.removeValidator('inacCd', Validators.REQUIRED);
    FormManager.removeValidator('inacType', Validators.REQUIRED);
    FormManager.resetDropdownValues(FormManager.getField('inacCd'));
    FormManager.resetDropdownValues(FormManager.getField('inacType'));
    return;
  }
}

function onInacTypeChange() {
  var searchTerm = FormManager.getActualValue('searchTerm');
  var reqType = null;
  reqType = FormManager.getActualValue('reqType');
  if (reqType == 'C') {
    var custSubT = FormManager.getActualValue('custSubGrp');
    if (_inacCdHandler == null) {
      _inacCdHandler = dojo.connect(FormManager.getField('inacType'), 'onChange', function(value) {
        var searchTerm = FormManager.getActualValue('searchTerm');
        var cmt = value + ','+ searchTerm +'%';
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
          console.log(value);
          if (value != null) {
            var inacCdValue = [];
            if(searchTerm == '09061' || searchTerm == '04476' || searchTerm == '10179' || searchTerm == '10180' || searchTerm == '10181' 
              || searchTerm == '10182' || searchTerm == '10183' || searchTerm == '10184' || searchTerm == '10185' || searchTerm == '10186' 
                || searchTerm == '10187') {
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
              }
            }
          }
      });
    }
  }
}

dojo.addOnLoad(function() {
  GEOHandler.TW = [ '858' ];
  GEOHandler.TW_CHECKLIST = [ '858' ];
  GEOHandler.enableCopyAddress(GEOHandler.TW);

  console.log('adding Taiwan functions...');
  GEOHandler.enableCustomerNamesOnAddress(GEOHandler.TW);
  GEOHandler.setRevertIsicBehavior(false);

  GEOHandler.addAfterConfig(afterConfigTW, GEOHandler.TW);
  GEOHandler.addAfterConfig(addHandlersForTW, GEOHandler.TW);
  GEOHandler.addAfterConfig(addSingleByteValidatorTW, GEOHandler.TW);
  // GEOHandler.addAfterConfig(setISUCodeValues, GEOHandler.TW);
  GEOHandler.addAfterConfig(setDupCmrIndcWarning, GEOHandler.TW);
  GEOHandler.addAfterConfig(setChecklistStatus, GEOHandler.TW_CHECKLIST);
  GEOHandler.addAfterConfig(onInacTypeChange, GEOHandler.TW);

  GEOHandler.addAfterTemplateLoad(afterConfigTW, GEOHandler.TW);
  GEOHandler.addAfterTemplateLoad(addHandlersForTW, GEOHandler.TW);
  // GEOHandler.addAfterTemplateLoad(setISUCodeValues, GEOHandler.TW);
  GEOHandler.addAfterTemplateLoad(setDupCmrIndcWarning, GEOHandler.TW);
  GEOHandler.addAfterTemplateLoad(addSingleByteValidatorTW, GEOHandler.TW);

  GEOHandler.addAddrFunction(updateMainCustomerNames, GEOHandler.TW);
  GEOHandler.addAddrFunction(addSingleByteValidatorTW, GEOHandler.TW);
  GEOHandler.addAddrFunction(setAbbrevNmLocnOnAddressSave, GEOHandler.TW);

  GEOHandler.registerValidator(addTWChecklistValidator, GEOHandler.TW_CHECKLIST);
  // GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.TW,
  // GEOHandler.ROLE_PROCESSOR, true);
  GEOHandler.registerValidator(addDPLCheckValidator, GEOHandler.TW, GEOHandler.ROLE_REQUESTER, true);
  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.TW);
  GEOHandler.addAfterConfig(setClientTierValuesTW, GEOHandler.TW);
  GEOHandler.addAfterTemplateLoad(setClientTierValuesTW, GEOHandler.TW);
  // CREATCMR-6825
  GEOHandler.addAfterConfig(setRepTeamMemberNo, GEOHandler.TW);
  GEOHandler.addAfterTemplateLoad(setRepTeamMemberNo, GEOHandler.TW);
  GEOHandler.addAddrFunction(handleObseleteExpiredDataForUpdate, GEOHandler.TW);
  GEOHandler.addAfterConfig(handleObseleteExpiredDataForUpdate, GEOHandler.TW);
  GEOHandler.addAfterTemplateLoad(handleObseleteExpiredDataForUpdate, GEOHandler.TW);
  // skip byte checks
  // FormManager.skipByteChecks([ 'cmt', 'bldg', 'dept', 'custNm3', 'custNm4',
  // 'busnType', 'footnoteTxt2', 'contactName1', 'bpName', 'footnoteTxt1',
  // 'contactName3' ]);
  FormManager.skipByteChecks([ 'cmt', 'bldg', 'dept', 'custNm3', 'custNm4', 'footnoteTxt1', 'contactName3' ]);

});
