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

  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
    taxLocation = _pagemodel.mktgDept;
  }

  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    FormManager.readOnly('ordBlk');
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

      FormManager.readOnly('custClass');
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

      FormManager.removeValidator('footnoteTxt1', Validators.REQUIRED);
      FormManager.removeValidator('contactName3', Validators.REQUIRED);
      FormManager.removeValidator('company', Validators.REQUIRED);
      FormManager.removeValidator('email3', Validators.REQUIRED);
      FormManager.removeValidator('customerIdCd', Validators.REQUIRED);
      FormManager.removeValidator('inacType', Validators.REQUIRED);
      FormManager.removeValidator('inacCd', Validators.REQUIRED);
      FormManager.removeValidator('covId', Validators.REQUIRED);
      FormManager.removeValidator('dunsNo', Validators.REQUIRED);
      FormManager.removeValidator('cmrNoPrefix', Validators.REQUIRED);
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

    FormManager.removeValidator('dupCmrIndc', Validators.REQUIRED);
    FormManager.removeValidator('bgId', Validators.REQUIRED);
    FormManager.removeValidator('gbgId', Validators.REQUIRED);
    FormManager.removeValidator('geoLocationCd', Validators.REQUIRED);
    FormManager.removeValidator('bgRuleId', Validators.REQUIRED);
  }

  if (custSubGrp == 'LOECO' || custSubGrp == 'LOINT' || custSubGrp == 'LOBLU' || custSubGrp == 'LOMAR' || custSubGrp == 'LOOFF' || custSubGrp == 'LOPRI') {
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

  setVatValidator();
  handleObseleteExpiredDataForUpdate();
  // CREATCMR-788
  addressQuotationValidator();
  addCoverageFieldsValidator();
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
var _subIndCdHandler =null;

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
      addCoverageFieldsValidator();
    });
  }

  // CREATCMR-7882
  if (_ISICHandler == null) {
    _ISICHandler = dojo.connect(FormManager.getField('isicCd'), 'onChange', function(value) {
      setISUCodeBasedOnISIC();
    });
  }

  if (_searchTermHandler == null) {
    _searchTermHandler = dojo.connect(FormManager.getField('searchTerm'), 'onChange', function(value) {
      console.log(">>> RUNNING SORTL HANDLER!!!!");
      if (!value) {
        return;
      }
      filterISUOnChange();
      setISUCodeBasedOnISIC();
      setISUForDefaultSearchTerm();
      setInacBySearchTerm();
      setMrcCd();
      addCoverageFieldsValidator();
    });
  }
  
  if (_subIndCdHandler == null) {
    _subIndCdHandler = dojo.connect(FormManager.getField('subIndustryCd'), 'onChange', function(value) {
      if (!value) {
        return;
      }
      if (value != null && value.length > 1) {
        updateIndustryClass();
        setKUKLAvaluesTW();
      }
    });
  }
}

function onCustSubGrpChange() {
  console.log('>>>> onCustSubGrpChange >>>>');
  if (FormManager.getActualValue('reqType') == 'U') {
    return;
  }

  dojo.connect(FormManager.getField('custSubGrp'), 'onChange', function (value) {
    setKUKLAvaluesTW();
  });
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
    FormManager.readOnly('repTeamMemberNo');
    FormManager.readOnly('covId');
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
    FormManager.removeValidator('cmrNoPrefix', Validators.REQUIRED);
    FormManager.removeValidator('covId', Validators.REQUIRED);
    FormManager.removeValidator('collectionCd', Validators.REQUIRED);
    FormManager.removeValidator('sitePartyId', Validators.REQUIRED);
    FormManager.removeValidator('dunsNo', Validators.REQUIRED);
    FormManager.removeValidator('customerIdCd', Validators.REQUIRED);
    FormManager.removeValidator('geoLocationId', Validators.REQUIRED);

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
  }
  handleObseleteExpiredDataForUpdate();
}

function setISUCodeBasedOnISIC() {

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
  var searchTerm = FormManager.getActualValue('searchTerm');

  if (custSubGrp == '') {
    return;
  }

  if ((isicCd == _pagemodel.isicCd) && (custSubGrp == _pagemodel.custSubGrp)) {
    return;
  }

  var isuCode = [];


  if (isicCd != '' && (searchTerm == '04476' || searchTerm == '71300')) {
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
  if (addrType == 'ZS01') {
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

function chineseAddrMandtValidator() {
  var zs01ReqId = FormManager.getActualValue('reqId');
  var qParams = {
    REQ_ID : zs01ReqId,
  };
  var result = cmr.query('ADDR.GET.CUSTNM3_DEPT.BY_REQID', qParams);
  var custNm3 = result.ret1;
  var dept = result.ret2;
   FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if(custNm3 == '' || custNm3 == undefined){
          return new ValidationResult(null, false, "Chinese Name in address is required.");
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
  
   FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if(dept == '' || dept == undefined){
          return new ValidationResult(null, false, "Chinese Address in address is required.");
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

// CREATCMR-7882
function setCluster(){
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (custSubGrp == 'CBFOR') {
    FormManager.setValue('searchTerm', (_pagemodel.searchTerm == '' || _pagemodel.searchTerm == null) ? '00000' : _pagemodel.searchTerm);
  }
}

// CREATCMR-7882
function addCoverageFieldsValidator() {
  FormManager.removeValidator('dunsNo', Validators.REQUIRED);
  FormManager.removeValidator('isuCd', Validators.REQUIRED);
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
  
  var searchTerm = FormManager.getActualValue('searchTerm');
  if (searchTerm == '04476') {
    FormManager.setValue('isuCd', '');
  }
  
  searchTermParams['_qall'] = 'Y';
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
    // FormManager.enable('clientTier');
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

function setISUForDefaultSearchTerm() {
  const cluster = FormManager.getActualValue('searchTerm');
  if (cluster != '00000') {
    return;
  }
  
  const custSubGrp = FormManager.getActualValue('custSubGrp');
  const custSubGrpIsu34 = ['LOBLU', 'LOPRI', 'LOMAR', 'LOOFF', 'CBFOR'];
  
  if (custSubGrpIsu34.includes(custSubGrp)) {
    FormManager.setValue('isuCd', '34');
  } else {
    FormManager.setValue('isuCd', '21');
  }
}

function setMrcCd() {
  var reqType = FormManager.getActualValue('reqType');
  var _cluster = FormManager.getActualValue('searchTerm');
  var _clusterWithMrcCD2 = ['04476', '09154','09151','09153','09147','09152','09155','71300'];
  var _custSubGrp = FormManager.getActualValue('custSubGrp');
  if (_clusterWithMrcCD2.includes(_cluster) || _custSubGrp == 'LOINT'){
    FormManager.setValue('mrcCd', '2');
  } else {
    FormManager.setValue('mrcCd', '3');
  }

  if (reqType == 'C') {
    FormManager.readOnly('mrcCd');
  }
}

// CREATCMR-7882
function setInacBySearchTerm() {
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var _cluster = FormManager.getActualValue('searchTerm');
  var _clusterTWWithAllInac = ['09208','04476','09154','09151','09153','09147','09152','09155'];
  
  if (FormManager.getActualValue('reqType') != 'C' || FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (!_cluster) {
    return;
  }
  if (!_clusterTWWithAllInac.includes(_cluster)) {
    FormManager.removeValidator('inacCd', Validators.REQUIRED);
    FormManager.removeValidator('inacType', Validators.REQUIRED);
    FormManager.resetDropdownValues(FormManager.getField('inacCd'));
    FormManager.resetDropdownValues(FormManager.getField('inacType'));
    
    if(!_clusterTWWithAllInac.includes(_cluster)){
      FormManager.setValue('inacCd','');
      FormManager.setValue('inacType', '');
    }
    
    return;
  }
  
  FormManager.addValidator('inacCd', Validators.REQUIRED, [ 'INAC/NAC Code' ], 'MAIN_IBM_TAB');
  FormManager.addValidator('inacType', Validators.REQUIRED, [ 'INAC Type' ], 'MAIN_IBM_TAB');
  
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

}
// CREATCMR-7882
function onInacTypeChange() {
  console.log(">>> onInacTypeChange <<<")
  var searchTerm = FormManager.getActualValue('searchTerm');
  var reqType = null;

  reqType = FormManager.getActualValue('reqType');
  if (reqType == 'C') {
    var custSubT = FormManager.getActualValue('custSubGrp');
    if (_inacCdHandler == null) {
      _inacCdHandler = dojo.connect(FormManager.getField('inacType'), 'onChange', function(value) {

          if (value != null) {
            var searchTerm = FormManager.getActualValue('searchTerm');
            var _clusterTWWithAllInac = ['09208','04476','09154','09151','09153','09147','09152','09155'];

            var cmt = value + ','+ searchTerm +'%';
            var cntry = FormManager.getActualValue('cmrIssuingCntry');
            var inacCdValue = [];
            var qParams = {
              _qall : 'Y',
              ISSUING_CNTRY : cntry ,
              CMT : cmt ,
            };
            var results = cmr.query('GET.INAC_CD', qParams);
            if (results != null && _clusterTWWithAllInac.includes(searchTerm)) {
              for (var i = 0; i < results.length; i++) {
                inacCdValue.push(results[i].ret1);
              }
              FormManager.limitDropdownValues(FormManager.getField('inacCd'), inacCdValue);
              if (inacCdValue.length == 1) {
                FormManager.setValue('inacCd', inacCdValue[0]);
              }
            } else {
              FormManager.resetDropdownValues(FormManager.getField('inacType'));
              FormManager.resetDropdownValues(FormManager.getField('inacCd'));
              FormManager.removeValidator('inacCd', Validators.REQUIRED);
              FormManager.removeValidator('inacType', Validators.REQUIRED);
              FormManager.enable('inacCd');
              FormManager.enable('inacType');

              limitInacNacCodeByInacNacType();
            }
          }
      });
    }
  }
}

function limitInacNacCodeByInacNacType() {
  var limitedInacNacCodeList = [];
  var inacNacCodes = []
  if (FormManager.getActualValue('inacType') == 'I') {
    var inacNacCodes = FormManager.getField('inacCd').loadedStore._arrayOfAllItems.filter(function checkInacFinal(inacNode) {
      return /^[0-9]+$/.test(inacNode.id[0]);
    });
  } else if (FormManager.getActualValue('inacType') == 'N') {
    var inacNacCodes = FormManager.getField('inacCd').loadedStore._arrayOfAllItems.filter(function checkInacFinal(inacNode) {
      return /^[A-Z]/.test(inacNode.id[0]);
    });
  }

  var ids = inacNacCodes.map(elem => elem.id[0]);
  
  FormManager.sortDropdownElements(FormManager.getField('inacCd'), ids, true);
}

// CREATCMR-7882
function checkCustomerNameForKYND() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var errorMsg = '';
        var action = FormManager.getActualValue('yourAction');
        var custNm1 = FormManager.getActualValue('mainCustNm1').toUpperCase();
        var custNm2 = FormManager.getActualValue('mainCustNm2').toUpperCase();
        var enName = custNm1 + ' ' + custNm2;

        var reqType = FormManager.getActualValue('reqType');
        var role = FormManager.getActualValue('userRole').toUpperCase();
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        if(reqType == 'C' && role == 'REQUESTER' && custSubGrp == 'KYND' && (action=='SFP' || action=='VAL')){
          if(enName.indexOf('KYNDRYL')==-1) {
            errorMsg = 'Customer name must contain word \'Kyndryl\'';
          }
        }
        if (errorMsg != '') {
          return new ValidationResult(null, false, errorMsg);
        }
        
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function clearDescriptionOnScenarioChange() {
  var bgId = FormManager.getActualValue('bgId');
  var gbgId = FormManager.getActualValue('gbgId');
  var covId = FormManager.getActualValue('covId');
  var geoLocationCd = FormManager.getActualValue('geoLocationCd');
  if (bgId == null || bgId == '') {
    $('#bgDescCont').html("(no description available)");
  }
  if (gbgId == null || gbgId == '') {
    $('#gbgDescCont').html("(no description available)");
  }
  if (covId == null || covId == '') {
    $('#covDescCont').html("(no description available)");
  }
  if (geoLocationCd == null || geoLocationCd == '') {
    $('#geoLocDescCont').html("(no description available)");
  }
}

// CREATCMR-7882
function updateIndustryClass() {
  console.log('>>>> updateIndustryClass >>>>');
  var subIndustryCd = FormManager.getActualValue('subIndustryCd');
  if (subIndustryCd != null && subIndustryCd.length > 1) {
    var _industryClass = subIndustryCd.substr(0, 1);
    FormManager.setValue('IndustryClass', _industryClass);
  }
}

// CREATCMR-8581

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

function setAddrFieldsValidation() {
  console.log(" >>> Set Address Fields validation TW! <<<");
  
  FormManager.addValidator('postCd', Validators.REQUIRED, [ 'Postal Code' ]);
}

function sortingSearchTerm() {
  console.log(">>> sortingSearchTerm <<<");
  var values = TemplateService.getCurrentTemplate().fields.find(element => element.fieldName == 'searchTerm').values;
  var field = FormManager.getField('searchTerm');
  FormManager.sortDropdownElements(field, values, true);
}

function setDefaultCustPrefLanguage() {
  console.log (">>> Set Default Cust Pref Language <<<");
  if (FormManager.getValue('reqType') == 'C' && FormManager.getValue('reqStatus') == 'DRA') {
    FormManager.setValue('custPrefLang', 'M');
  }
}

function setKUKLAvaluesTW() {
  var reqType = FormManager.getActualValue('reqType');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var industryClass = FormManager.getActualValue('IndustryClass');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var isicCd = FormManager.getActualValue('isicCd');

  if (FormManager.getActualValue('reqType') == 'U') {
    return
  }

  console.log('>>>> setKUKLAvaluesTW() >>>> set KUKLA values for TW >>>>');

  var custSubGrp1 = new Set(['LOBLU', 'ECOSY', 'KYND', 'LOMAR', 'NRMLD']);
  var custSubGrp2 = new Set(['LOACQ', 'NRMLC', 'LOOFF', 'CBFOR']);
  var custSubGrp3 = new Set(['LOECO']);
  var custSubGrp4 = new Set(['LOPRI']);
  var custSubGrp5 = new Set(['LOINT']);

  var industryClass1 = new Set(['G', 'H', 'Y']);
  var industryClass2 = new Set(['E']);

  var kuklaTW = [];
  if (reqType == 'C') {
    FormManager.readOnly('custClass');
    var qParams = {
      _qall: 'Y',
      ISSUING_CNTRY: cntry,
    };
    var results = cmr.query('GET.AP_KUKLA', qParams);
    if (results != null) {
      for (var i = 0; i < results.length; i++) {
        kuklaTW.push(results[i].ret1);
      }
    }

    if (results != null) {
      if (custSubGrp1.has(custSubGrp)) {
        FormManager.setValue('custClass', kuklaTW[0]);
      } else if (custSubGrp2.has(custSubGrp)) {
        if (industryClass1.has(industryClass)) {
          FormManager.setValue('custClass', kuklaTW[1]);
        } else if (industryClass2.has(industryClass)) {
          FormManager.setValue('custClass', kuklaTW[2]);
        } else {
          FormManager.setValue('custClass', kuklaTW[0]);
        }
      } else if (custSubGrp3.has(custSubGrp)) {
        FormManager.setValue('custClass', kuklaTW[3]);
      } else if (custSubGrp4.has(custSubGrp)) {
        FormManager.setValue('custClass', kuklaTW[4]);
      } else if (custSubGrp5.has(custSubGrp)) {
        FormManager.setValue('custClass', kuklaTW[5]);
      }
    }
  }
}

function afterConfigCallsTW() {
  afterConfigTW();
  addHandlersForTW();
  addSingleByteValidatorTW();
  setDupCmrIndcWarning();
  onInacTypeChange();
  addCoverageFieldsValidator();
  updateIndustryClass();
  setClientTierValuesTW();
  setRepTeamMemberNo();
  setMrcCd();
  handleObseleteExpiredDataForUpdate();
  setAddrFieldsValidation();
  onCustSubGrpChange();
}

function afterTemplateLoadTW() {
  afterConfigTW();
  addHandlersForTW();
  setDupCmrIndcWarning();
  addSingleByteValidatorTW();
  setClientTierValuesTW();
  setRepTeamMemberNo();
  handleObseleteExpiredDataForUpdate();
  setCluster();
  addCoverageFieldsValidator();
  setMrcCd();
  clearDescriptionOnScenarioChange();
  sortingSearchTerm();
  setAddrFieldsValidation();
}

dojo.addOnLoad(function() {
  GEOHandler.TW = [ '858' ];
  GEOHandler.TW_CHECKLIST = [ '858' ];
  GEOHandler.enableCopyAddress(GEOHandler.TW);

  console.log('adding Taiwan functions...');
  GEOHandler.enableCustomerNamesOnAddress(GEOHandler.TW);
  GEOHandler.setRevertIsicBehavior(false);

  // after config
  GEOHandler.addAfterConfig(afterConfigCallsTW, GEOHandler.TW);
  GEOHandler.addAfterConfig(setChecklistStatus, GEOHandler.TW_CHECKLIST);

  // after template
  GEOHandler.addAfterTemplateLoad(afterTemplateLoadTW, GEOHandler.TW);

  // addr funtion
  GEOHandler.addAddrFunction(updateMainCustomerNames, GEOHandler.TW);
  GEOHandler.addAddrFunction(addSingleByteValidatorTW, GEOHandler.TW);
  GEOHandler.addAddrFunction(setAbbrevNmLocnOnAddressSave, GEOHandler.TW);
  GEOHandler.addAddrFunction(handleObseleteExpiredDataForUpdate, GEOHandler.TW);
  GEOHandler.addAddrFunction(setAddrFieldsValidation, GEOHandler.TW);
  
  // validators
  GEOHandler.registerValidator(addTWChecklistValidator, GEOHandler.TW_CHECKLIST);
  GEOHandler.registerValidator(addDPLCheckValidator, GEOHandler.TW, GEOHandler.ROLE_REQUESTER, true);
  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.TW);
  
  // CREATCMR-8581
  GEOHandler.registerValidator(checkCmrUpdateBeforeImport, GEOHandler.TW,null,true);
  
  // CREATCMR-6825
  GEOHandler.registerValidator(checkCustomerNameForKYND, GEOHandler.TW, null, true);
  GEOHandler.registerValidator(chineseAddrMandtValidator, GEOHandler.TW, null, true);

  FormManager.skipByteChecks([ 'cmt', 'bldg', 'dept', 'custNm3', 'custNm4', 'footnoteTxt1', 'contactName3' ]);

});
