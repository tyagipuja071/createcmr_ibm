/* Register KR Javascripts */
 var _isicHandler = null;
 
function afterConfigKR() {
  var role = null;
  var reqType = null;
  var _isuHandler = null;

  
  _isicHandler = dojo.connect(FormManager.getField('isicCd'), 'onChange', function(value) {
    getIsuFromIsic();
  });
  
  if (_isuHandler == null) {
    _isuHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
      setClientTierValues();
    });
  }
  var _scenarioHandler = dojo.connect(FormManager.getField('custSubGrp'), 'onChange', function(value) {
    FormManager.resetDropdownValues(FormManager.getField('searchTerm'));
    FormManager.resetDropdownValues(FormManager.getField('inacType'));
    FormManager.resetDropdownValues(FormManager.getField('inacCd'));
    FormManager.setValue('inacType','');
    FormManager.setValue('inacCd','');
    FormManager.enable('searchTerm');
    FormManager.enable('isicCd');
    FormManager.enable('inacType');
    FormManager.enable('inacCd');
    setSearchTermDropdownValues();
    LockDefaultISUClientTierMrcValues();
    FormManager.readOnly('clientTier');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('mrcCd');
    if (custSubGrp.value == 'Internal') {
      FormManager.setValue('isuCd', '21');
      FormManager.readOnly('isuCd');
    }
  });
  
  var _clusterHandler = dojo.connect(FormManager.getField('searchTerm'), 'onChange', function(value) {
    FormManager.resetDropdownValues(FormManager.getField('inacType'));
    FormManager.resetDropdownValues(FormManager.getField('inacCd')); 
    FormManager.resetDropdownValues(FormManager.getField('isuCd')); 
    FormManager.setValue('inacType','');
    FormManager.setValue('inacCd','');
    LockDefaultISUClientTierMrcValues();
    setInacNacValues();
    FormManager.readOnly('clientTier');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('mrcCd');
  });

  var _inacType = dojo.connect(FormManager.getField('inacType'), 'onChange', function(value) {
    setInacNacValues();
  });
  
  var _inacType = dojo.connect(FormManager.getField('inacType'), 'onChange', function(value) {
    setInacNacValues();
  });

  reqType = FormManager.getActualValue('reqType');
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }
  FormManager.readOnly('capInd');
  FormManager.setValue('capInd', true);
  FormManager.readOnly('cmrOwner');
  FormManager.resetValidations('enterprise');
  FormManager.enable('memLvl');
  FormManager.enable('sitePartyId');

  FormManager.addValidator('abbrevNm', Validators.REQUIRED, [ 'Abbreviated Name (TELX1)' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('phone1', Validators.REQUIRED, [ 'Business License Type' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('installRep', Validators.REQUIRED, [ 'Tax Invoice Type' ], 'MAIN_CUST_TAB');

  FormManager.addValidator('contactName3', Validators.REQUIRED, [ 'Product Type' ], 'MAIN_IBM_TAB');
  //FormManager.addValidator('MrcCd', Validators.REQUIRED, [ 'Market Responsibility Code (MRC)' ], 'MAIN_IBM_TAB');
  FormManager.addValidator('commercialFinanced', Validators.REQUIRED, [ 'ROL Code' ], 'MAIN_IBM_TAB');

  FormManager.removeValidator('subIndustryCd', Validators.REQUIRED);
  FormManager.removeValidator('sensitiveFlag', Validators.REQUIRED);
  FormManager.removeValidator('LocalTax2', Validators.REQUIRED);

  // Non editable for requester role
  if (reqType == 'C' && role == 'Requester') {
    FormManager.readOnly('isuCd');
    if (custSubGrp != 'INTER') {
      FormManager.readOnly('cmrNoPrefix');
    }
  }

  if (reqType == 'C') {
    FormManager.addValidator('MrcCd', Validators.REQUIRED, [ 'Market Responsibility Code (MRC)' ], 'MAIN_IBM_TAB');
  }

  // story: attachment Company Proof required
  var custSubType = FormManager.getActualValue('custSubGrp');
  if ((reqType == 'C' || reqType == 'U') && (custSubType != 'INTER')) {
    // FormManager.addValidator('DocContent',Validators.REQUIRED,['Company Proof
    // required']);
    FormManager.addValidator('docContent', Validators.REQUIRED, [ '${ui.content}' ], 'MAIN_ATTACH_TAB');
  }

  if (reqType == 'U') {
    FormManager.removeValidator('custNm3', Validators.REQUIRED);
    FormManager.removeValidator('custNm4', Validators.REQUIRED);
    FormManager.removeValidator('divn', Validators.REQUIRED);
    FormManager.removeValidator('abbrevLocn', Validators.REQUIRED);
    FormManager.removeValidator('contactName1', Validators.REQUIRED);
    FormManager.removeValidator('searchTerm', Validators.REQUIRED);
    FormManager.removeValidator('clientTier', Validators.REQUIRED);
    FormManager.removeValidator('repTeamMemberNo', Validators.REQUIRED);
    FormManager.removeValidator('commercialFinanced', Validators.REQUIRED);
    FormManager.removeValidator('contactName2', Validators.REQUIRED);
    FormManager.removeValidator('contactName3', Validators.REQUIRED);
    FormManager.removeValidator('creditCd', Validators.REQUIRED);
    FormManager.removeValidator('installRep', Validators.REQUIRED);
    FormManager.removeValidator('MrcCd', Validators.REQUIRED);
    FormManager.removeValidator('phone1', Validators.REQUIRED);
  }

  RemoveCrossAddressMandatory();
  setChecklistStatus();
  addKRChecklistValidator();
  handleObseleteExpiredDataForUpdate();
  // CREATCMR-788
  addressQuotationValidator();
  FormManager.readOnly('clientTier');
  FormManager.readOnly('isuCd');
  FormManager.readOnly('mrcCd');
}

function setClientTierValues() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  isuCd = FormManager.getActualValue('isuCd');
  if (isuCd == '5K') {
    FormManager.removeValidator('clientTier', Validators.REQUIRED);
    FormManager.readOnly('clientTier');
  } else {
    var reqType = FormManager.getActualValue('reqType');
    if (reqType != 'U') {
      FormManager.addValidator('clientTier', Validators.REQUIRED, [ 'Client Tier' ], 'MAIN_IBM_TAB');
    }
  }
  handleObseleteExpiredDataForUpdate();
  FormManager.readOnly('clientTier');
  FormManager.readOnly('isuCd');
  FormManager.readOnly('mrcCd');
}

function setChecklistStatus() {

  reqType = FormManager.getActualValue('reqType');
  var custSubScnrio = FormManager.getActualValue('custSubGrp');
  if (reqType == 'U') {
    return;
  }
  if (custSubScnrio != 'CROSS') {
    return;
  }
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

function addKRChecklistValidator() {

  reqType = FormManager.getActualValue('reqType');
  var custSubScnrio = FormManager.getActualValue('custSubGrp');
  if (reqType == 'U') {
    return;
  }
  if (custSubScnrio != 'CROSS') {
    return;
  }
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        console.log('validating checklist..');
        var checklist = dojo.query('table.checklist');

        // local address name if found
        var localAddr = checklist.query('input[name="localAddr"]');
        if (localAddr.length > 0 && localAddr[0].value.trim() == '') {
          return new ValidationResult(null, false, 'Checklist has not been fully accomplished. All items are required.');
        }

        var freeTxtField1 = checklist.query('input[name="freeTxtField1"]');
        if (freeTxtField1.length > 0 && freeTxtField1[0].value.trim() == '') {
          return new ValidationResult(null, false, 'Checklist has not been fully accomplished. All items are required.');
        }

        var freeTxtField2 = checklist.query('input[name="freeTxtField2"]');
        if (freeTxtField2.length > 0 && freeTxtField2[0].value.trim() == '') {
          return new ValidationResult(null, false, 'Checklist has not been fully accomplished. All items are required.');
        }

        var freeTxtField3 = checklist.query('input[name="freeTxtField3"]');
        if (freeTxtField3.length > 0 && freeTxtField3[0].value.trim() == '') {
          return new ValidationResult(null, false, 'Checklist has not been fully accomplished. All items are required.');
        }

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
        }

        // add check for checklist on DB
        var reqId = FormManager.getActualValue('reqId');
        var record = cmr.getRecord('GBL_CHECKLIST', 'ProlifChecklist', {
          REQID : reqId
        });
        if (!record || !record.sectionA1) {
          return new ValidationResult(null, false, 'Checklist has not been registered yet. Please execute a \'Save\' action before sending for processing to avoid any data loss.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CHECKLIST_TAB', 'frmCMR');
}

function RemoveCrossAddressMandatory() {
  var custSubScnrio = FormManager.getActualValue('custSubGrp');
  if (custSubScnrio == 'CROSS') {
    FormManager.removeValidator('city2', Validators.REQUIRED);
    FormManager.removeValidator('custNm3', Validators.REQUIRED);
    FormManager.removeValidator('custNm4', Validators.REQUIRED);
  }
}
// story:2139 Except Internal scenario, for all the other scenarios requester
// needs to attach a file of COMP(Company Proof)
function addAttachmentValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqType = FormManager.getActualValue('reqType');
        var custSubType = FormManager.getActualValue('custSubGrp');
        var cntryUse = FormManager.getActualValue('cntryUse');
        // var docContent = FormManager.getActualValue('docContent');
        if (typeof (_pagemodel) != 'undefined') {
          if ((reqType == 'C' || reqType == 'U') && (custSubType != 'INTER')) {
            var id = FormManager.getActualValue('reqId');
            var ret = cmr.query('CHECK_DNB_MATCH_ATTACHMENT', {
              ID : id
            });

            if (ret == null || ret.ret1 == null) {
              return new ValidationResult(null, false, 'Company Proof  in Attachment tab is required.');
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
  var _abbrevLocn = null;
  var custSubGrp = FormManager.getActualValue('custSubGrp');

  var zs01ReqId = FormManager.getActualValue('reqId');
  var qParams = {
    REQ_ID : zs01ReqId,
  };
  var result = cmr.query('ADDR.GET.CUSTNM1.BY_REQID', qParams);
  var custNm1 = FormManager.getActualValue('custNm1');
  var _abbrevLocn = FormManager.getActualValue('city1')

  if (custNm1 == '') {
    custNm1 = result.ret1;
  }
  _abbrevNm = custNm1;

  if (_abbrevNm && _abbrevNm.length > 21) {
    _abbrevNm = _abbrevNm.substring(0, 21);
  }
  FormManager.setValue('abbrevNm', _abbrevNm);

  if (_abbrevLocn && _abbrevLocn.length > 12) {
    _abbrevLocn = _abbrevLocn.substring(0, 12);
  }
  FormManager.setValue('abbrevLocn', _abbrevLocn);

  // switch (_abbrevLocn) {
  // case '1':
  // FormManager.setValue('abbrevLocn', 'Seoul');
  // break;

  // case '2':
  // FormManager.setValue('abbrevLocn', 'Busan');
  // break;

  // case '3':
  // FormManager.setValue('abbrevLocn', 'Daegu');
  // break;

  // case '4':
  // FormManager.setValue('abbrevLocn', 'Incheon');
  // break;

  // case '5':
  // FormManager.setValue('abbrevLocn', 'Gwangju');
  // break;

  // case '6':
  // FormManager.setValue('abbrevLocn', 'Daejeon');
  // break;
  // case '7':
  // FormManager.setValue('abbrevLocn', 'Ulsan');
  // break;
  // case '8':
  // FormManager.setValue('abbrevLocn', 'Sejong');
  // break;
  // case '9':
  // FormManager.setValue('abbrevLocn', 'Gyeonggi-do');
  // break;
  // case '10':
  // FormManager.setValue('abbrevLocn', 'Gangwon-do');
  // break;
  // case '11':
  // FormManager.setValue('abbrevLocn', 'Chungcheongbuk-do');
  // break;

  // case '12':
  // FormManager.setValue('abbrevLocn', 'Chungcheongnam-do');
  // break;

  // case '13':
  // FormManager.setValue('abbrevLocn', 'Jeollabuk-do');
  // break;

  // case '14':
  // FormManager.setValue('abbrevLocn', 'Jeollanam-do');
  // break;

  // case '15':
  // FormManager.setValue('abbrevLocn', 'Gyeongsangbuk-do');
  // break;

  // case '16':
  // FormManager.setValue('abbrevLocn', 'Gyeongsangnam-do');
  // break;

  // case '17':
  // FormManager.setValue('abbrevLocn', 'Jeju');
  // break;
  // }
}
// CREATCMR-6825
function setRepTeamMemberNo() {
  var reqType = FormManager.getActualValue('reqType');
  if (reqType == 'C') {
    FormManager.setValue('repTeamMemberNo', '000000');
    FormManager.readOnly('repTeamMemberNo');
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
    FormManager.readOnly('commercialFinanced');
    FormManager.readOnly('contactName2');
    FormManager.readOnly('contactName3'); 
    if (custSubGrp != 'INTER') {
      FormManager.readOnly('cmrNoPrefix');
    }
    FormManager.readOnly('bgId');
    FormManager.readOnly('gbgId');
    FormManager.readOnly('bgRuleId');
    FormManager.readOnly('geoLocationCd');
    FormManager.readOnly('dunsNo');
    FormManager.readOnly('orgNo');
    FormManager.readOnly('creditCd');
    FormManager.readOnly('dealerNo');

    FormManager.removeValidator('searchTerm', Validators.REQUIRED);
    FormManager.removeValidator('clientTier', Validators.REQUIRED);
    FormManager.removeValidator('isuCd', Validators.REQUIRED);
    FormManager.removeValidator('mrcCd', Validators.REQUIRED);
    FormManager.removeValidator('inacType', Validators.REQUIRED);
    FormManager.removeValidator('inacCd', Validators.REQUIRED);
    FormManager.removeValidator('repTeamMemberNo', Validators.REQUIRED);
    FormManager.removeValidator('cmrNoPrefix', Validators.REQUIRED);
    FormManager.removeValidator('covId', Validators.REQUIRED);
    FormManager.removeValidator('commercialFinanced', Validators.REQUIRED);
    FormManager.removeValidator('contactName2', Validators.REQUIRED);
    FormManager.removeValidator('contactName3', Validators.REQUIRED);
    FormManager.removeValidator('bgId', Validators.REQUIRED);
    FormManager.removeValidator('gbgId', Validators.REQUIRED);
    FormManager.removeValidator('geoLocationId', Validators.REQUIRED);
    FormManager.removeValidator('dunsNo', Validators.REQUIRED);
    FormManager.removeValidator('bgRuleId', Validators.REQUIRED);
    FormManager.removeValidator('orgNo', Validators.REQUIRED);
    FormManager.removeValidator('creditCd', Validators.REQUIRED);
    FormManager.removeValidator('dealerNo', Validators.REQUIRED);

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
    FormManager.readOnly('commercialFinanced');
    FormManager.readOnly('contactName2');
    FormManager.readOnly('contactName3');
    if (custSubGrp != 'INTER') {
      FormManager.readOnly('cmrNoPrefix');
    }
    FormManager.readOnly('bgId');
    FormManager.readOnly('gbgId');
    FormManager.readOnly('bgRuleId');
    FormManager.readOnly('geoLocationCd');
    FormManager.readOnly('dunsNo');
    FormManager.readOnly('orgNo');
    FormManager.readOnly('creditCd');
    FormManager.readOnly('dealerNo');

    FormManager.removeValidator('searchTerm', Validators.REQUIRED);
    FormManager.removeValidator('clientTier', Validators.REQUIRED);
    FormManager.removeValidator('isuCd', Validators.REQUIRED);
    FormManager.removeValidator('mrcCd', Validators.REQUIRED);
    FormManager.removeValidator('inacType', Validators.REQUIRED);
    FormManager.removeValidator('inacCd', Validators.REQUIRED);
    FormManager.removeValidator('repTeamMemberNo', Validators.REQUIRED);
    FormManager.removeValidator('cmrNoPrefix', Validators.REQUIRED);
    FormManager.removeValidator('covId', Validators.REQUIRED);
    FormManager.removeValidator('commercialFinanced', Validators.REQUIRED);
    FormManager.removeValidator('contactName2', Validators.REQUIRED);
    FormManager.removeValidator('contactName3', Validators.REQUIRED);
    FormManager.removeValidator('bgId', Validators.REQUIRED);
    FormManager.removeValidator('gbgId', Validators.REQUIRED);
    FormManager.removeValidator('geoLocationId', Validators.REQUIRED);
    FormManager.removeValidator('dunsNo', Validators.REQUIRED);
    FormManager.removeValidator('bgRuleId', Validators.REQUIRED);
    FormManager.removeValidator('orgNo', Validators.REQUIRED);
    FormManager.removeValidator('creditCd', Validators.REQUIRED);
    FormManager.removeValidator('dealerNo', Validators.REQUIRED);

  }
}

function addressQuotationValidator() {
  // CREATCMR-788
  FormManager.addValidator('abbrevNm', Validators.NO_QUOTATION, [ 'Abbreviated Name (TELX1)' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('abbrevLocn', Validators.NO_QUOTATION, [ 'Abbreviated Location' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('custNm1', Validators.NO_QUOTATION, [ 'Customer Name 1' ]);
  FormManager.addValidator('custNm2', Validators.NO_QUOTATION, [ 'Customer Name Con\'t' ]);
  FormManager.addValidator('custNm3', Validators.NO_QUOTATION, [ 'Customer Name_Korean' ]);
  FormManager.addValidator('billingPstlAddr', Validators.NO_QUOTATION, [ 'Customer Name_Korean Continue' ]);

  FormManager.addValidator('addrTxt', Validators.NO_QUOTATION, [ 'Street Address 1' ]);
  FormManager.addValidator('addrTxt2', Validators.NO_QUOTATION, [ 'Address Con\'t' ]);
  FormManager.addValidator('custNm4', Validators.NO_QUOTATION, [ 'Street address_Korean' ]);
  FormManager.addValidator('divn', Validators.NO_QUOTATION, [ 'Street address_Korean Continue' ]);

  FormManager.addValidator('city1', Validators.NO_QUOTATION, [ 'City' ]);
  FormManager.addValidator('city2', Validators.NO_QUOTATION, [ 'City Korean' ]);
  FormManager.addValidator('postCd', Validators.NO_QUOTATION, [ 'Postal Code' ]);
  FormManager.addValidator('transportZone', Validators.NO_QUOTATION, [ 'Transport Zone' ]);

  FormManager.addValidator('contact', Validators.NO_QUOTATION, [ 'Name of person in charge of Invoice_1' ]);
  FormManager.addValidator('countyName', Validators.NO_QUOTATION, [ 'Name of person in charge of Invoice_2' ]);
  FormManager.addValidator('dept', Validators.NO_QUOTATION, [ 'Department Name_1' ]);
  FormManager.addValidator('poBoxCity', Validators.NO_QUOTATION, [ 'Department Name_2' ]);

  FormManager.addValidator('poBox', Validators.NO_QUOTATION, [ 'Telephone No_1' ]);
  FormManager.addValidator('taxOffice', Validators.NO_QUOTATION, [ 'Telephone No_2' ]);
  FormManager.addValidator('floor', Validators.NO_QUOTATION, [ 'E-mail_1' ]);
  FormManager.addValidator('office', Validators.NO_QUOTATION, [ 'E-mail_2' ]);
  FormManager.addValidator('bldg', Validators.NO_QUOTATION, [ 'E-mail_3' ]);
  FormManager.addValidator('custPhone', Validators.NO_QUOTATION, [ 'Company Phone #' ]);

}

function setSearchTermDropdownValues() {
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var searchTerm = FormManager.getField('searchTerm');
  if (searchTerm) {
    switch (custSubGrp) {
      case "NRST":
        FormManager.limitDropdownValues(searchTerm, [ '04461', '04466', '05223', '10139' ]);
        break;
      case "BLUMX":
        FormManager.limitDropdownValues(searchTerm, [ '00003' ]);
        FormManager.setValue('searchTerm', '00003');
        FormManager.readOnly('searchTerm');
        FormManager.setValue('isuCd', '34');
        FormManager.readOnly('isuCd');
        FormManager.setValue('clientTier', 'Z');
        FormManager.readOnly('clientTier');
        break;
      case "MKTPC":
        FormManager.limitDropdownValues(searchTerm, [ '00003' ]);
        FormManager.setValue('searchTerm', '00003');
        FormManager.readOnly('searchTerm');
        break;    
      case "VAPAR":
        FormManager.limitDropdownValues(searchTerm, [ '00003' ]);
        FormManager.setValue('searchTerm', '00003');
        FormManager.readOnly('searchTerm');
        break;  
      case "CROSS":
        FormManager.limitDropdownValues(searchTerm, [ '01545' , '00003' , '09065' , '08016' , '04461' , '04466' , '05223' , '10139' ]);
        FormManager.setValue('searchTerm', '00003');
        break;
      case "CBBUS":
        FormManager.limitDropdownValues(searchTerm, [ '71500' ]); 
        FormManager.setValue('searchTerm', '71500');
        FormManager.readOnly('searchTerm');
        FormManager.readOnly('inacCd');
        FormManager.readOnly('inacType');
        break;
      case "ECOSY":
        FormManager.limitDropdownValues(searchTerm, [ '08016' ]);
        FormManager.setValue('searchTerm', '08016');
        FormManager.readOnly('searchTerm');
        FormManager.setValue('isuCd', '36');
        FormManager.readOnly('isuCd');
        FormManager.setValue('clientTier', 'Y');
        FormManager.readOnly('clientTier');
        break;
      case "ESA":
    	  FormManager.setValue('isuCd', '36');
          FormManager.readOnly('isuCd');
          FormManager.setValue('clientTier', 'Y');
          FormManager.readOnly('clientTier');
        FormManager.limitDropdownValues(searchTerm, [ '08016', '09065', '01545','04461','04466','05223','10139' ]);
        break;
      case "INTER":
        FormManager.setValue('isuCd', '21');
        FormManager.readOnly('isuCd');
        FormManager.limitDropdownValues(searchTerm, [ '00003' ]);
        FormManager.setValue('searchTerm', '00003');
        FormManager.readOnly('searchTerm');
        FormManager.setValue('clientTier', 'Z');
        FormManager.readOnly('clientTier');
        FormManager.setValue('mrcCd', '2');
        FormManager.readOnly('mrcCd');
        FormManager.setValue('isicCd', '8888');
        FormManager.readOnly('isicCd');
        FormManager.enable('cmrNoPrefix');
        FormManager.readOnly('inacCd');
        FormManager.readOnly('inacType');
        break;
      case "LKYN":  
        FormManager.limitDropdownValues(searchTerm, [ '09065' ]);
        FormManager.setValue('searchTerm', '09065');
        FormManager.readOnly('searchTerm');
        break;
      case "NRML":
        FormManager.limitDropdownValues(searchTerm, [ '01545' ]);
        FormManager.setValue('searchTerm', '01545');
        FormManager.readOnly('searchTerm');
        break;
      case "AQSTN":       
        FormManager.limitDropdownValues(searchTerm, [ '01545' ]);
        FormManager.setValue('searchTerm', '01545');
        FormManager.readOnly('searchTerm');
        break;
    }
  }
}

function LockDefaultISUClientTierMrcValues() {
//  FormManager.setValue('clientTier','');
//  FormManager.setValue('isuCd','');
//  FormManager.setValue('mrcCd','');
  var searchTerm = FormManager.getActualValue('searchTerm');
  var clientTier = FormManager.getField('clientTier');
  if (searchTerm == "00003") {
    FormManager.setValue('clientTier', 'Z');
    FormManager.readOnly('clientTier');
  } else if (searchTerm == "08016") {
    FormManager.setValue('clientTier', 'Y');
    FormManager.readOnly('clientTier');
    FormManager.setValue('isuCd', '36');
    FormManager.readOnly('isuCd');
  } else if (searchTerm == "10139") {
    FormManager.setValue('clientTier', 'T');
    FormManager.readOnly('clientTier');
  } else if (searchTerm == "01545") {
    FormManager.setValue('clientTier', 'Q');
    FormManager.readOnly('clientTier');
  } else if (searchTerm == '71500' || searchTerm == '09065' || searchTerm =='04461' || searchTerm == '04466' || searchTerm == '05223') {
    FormManager.setValue('clientTier', '0');
    FormManager.readOnly('clientTier');
  }

  if (searchTerm == '71500' || searchTerm == '04461' || searchTerm == '04466' || searchTerm == '05223') {
    FormManager.setValue('mrcCd', '2');
    FormManager.readOnly('mrcCd');
  } else if ((searchTerm == '10139' || searchTerm == '08016' || searchTerm == '09065' || searchTerm == '00003' || searchTerm == '01545') && FormManager.getField('custSubGrp') != "INTER") {  
    FormManager.setValue('mrcCd', '3');
    FormManager.readOnly('mrcCd');
  }
  if (searchTerm == '00003' && FormManager.getField('custSubGrp') != "INTER") {
    FormManager.setValue('isuCd', '34');
    FormManager.readOnly('isuCd');
  } else if (searchTerm == '08016') {
    FormManager.setValue('isuCd', '36');
    FormManager.readOnly('isuCd');
  } else if (searchTerm == '10139') {
    FormManager.setValue('isuCd', '32');
    FormManager.readOnly('isuCd');
  } else if (searchTerm == '01545') {
    FormManager.setValue('isuCd', '34');
    FormManager.readOnly('isuCd');
  } else if (searchTerm == '71500') {
    FormManager.setValue('isuCd', '5B');
    FormManager.readOnly('isuCd');
  } else if (searchTerm == '09065') {
    FormManager.setValue('isuCd', '5K');
    FormManager.readOnly('isuCd');
  } else if (searchTerm == '00003' && FormManager.getField('custSubGrp') == "INTER") {
    FormManager.setValue('isuCd', '21');
    FormManager.readOnly('isuCd');
  }
  
  if (searchTerm == '04461' || searchTerm == '04466' || searchTerm == '05223' ) {
    FormManager.readOnly('isuCd');
    getIsuFromIsic();
  }
  
  FormManager.readOnly('clientTier');
  FormManager.readOnly('isuCd');
  FormManager.readOnly('mrcCd');  
}

function setInacNacValues(){
  var _cluster = FormManager.getActualValue('searchTerm');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var qParams = {
      _qall : 'Y',
      ISSUING_CNTRY : cntry,
      CMT : '%' + _cluster + '%'
    };
    var inacList = cmr.query('GET.INAC_BY_CLUSTER', qParams);
    if (inacList != null && inacList.length>0) {
      var inacTypeSelected ='';
      var arr =  inacList.map(inacList => inacList.ret1);
      inacTypeSelected  =  inacList.map(inacList => inacList.ret2);
      FormManager.limitDropdownValues(FormManager.getField('inacCd'), arr);
      console.log(arr);
      if (inacList.length == 1) {
        FormManager.setValue('inacCd', arr[0]);
      }
      if (inacType != '' && inacTypeSelected[0].includes(",I") && !inacTypeSelected[0].includes(',IN')) {
        FormManager.limitDropdownValues(FormManager.getField('inacType'), 'I');
        FormManager.setValue('inacType', 'I');
      } else if (inacType != '' && inacTypeSelected[0].includes(',N')) {
        FormManager.limitDropdownValues(FormManager.getField('inacType'), 'N');
        FormManager.setValue('inacType', 'N');
      } else if(inacType != '' && inacTypeSelected[0].includes(',IN')) {
        FormManager.resetDropdownValues(FormManager.getField('inacType'));
        var value = FormManager.getActualValue('inacType');
        var cmt = value + ','+ _cluster +'%';
        var value = FormManager.getActualValue('inacType');
        var cntry =  FormManager.getActualValue('cmrIssuingCntry');
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
      FormManager.addValidator('inacType', Validators.REQUIRED, [ 'INAC Type' ], 'MAIN_IBM_TAB');
      FormManager.addValidator('inacCd', Validators.REQUIRED, [ 'INAC/NAC Code' ], 'MAIN_IBM_TAB');
    } else {
      FormManager.removeValidator('inacType', Validators.REQUIRED);
      FormManager.removeValidator('inacCd', Validators.REQUIRED);      
    }
}

function validateCustnameForKynd() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var errorMsg = '';
        var action = FormManager.getActualValue('yourAction');
        var custNm1 = FormManager.getActualValue('mainCustNm1').toUpperCase();        
        var reqType = FormManager.getActualValue('reqType');
        var custSubGrp = FormManager.getActualValue('custSubGrp');        
        if(reqType == 'C' && custSubGrp == 'LKYN'){
          if(custNm1.indexOf('KYNDRYL')==-1){
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

function getIsuFromIsic(){
  var searchTerm = FormManager.getActualValue('searchTerm');
  var isicCd = FormManager.getActualValue('isicCd');
  if (!(searchTerm == '04461' || searchTerm == '04466' || searchTerm == '05223')) {
    return;
  }
  var ISU = [];
  if (isicCd != '') {
    var qParams = {
      _qall : 'Y',
      ISSUING_CNTRY : '856',
      REP_TEAM_CD : '%' + isicCd + '%'
    };
    var results = cmr.query('GET.ISULIST.BYISIC', qParams);
    if (results != null) {
      for (var i = 0; i < results.length; i++) {
        ISU.push(results[i].ret1);
      }
      if (ISU != null) {
        FormManager.setValue('isuCd','');
        FormManager.limitDropdownValues(FormManager.getField('isuCd'), ISU);
        if (ISU.length >= 1) {
          FormManager.setValue('isuCd', ISU[0]);
        }
      }
    }
  } else {
    FormManager.readOnly('isuCd');
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
  GEOHandler.KR = [ '766' ];
  console.log('adding KOREA functions...');
  GEOHandler.enableCustomerNamesOnAddress(GEOHandler.KR);
  GEOHandler.setRevertIsicBehavior(false);

  GEOHandler.addAfterConfig(afterConfigKR, GEOHandler.KR);
  GEOHandler.addAfterConfig(setChecklistStatus, GEOHandler.KR);

  GEOHandler.addAfterTemplateLoad(afterConfigKR, GEOHandler.KR);
  GEOHandler.addAddrFunction(updateMainCustomerNames, GEOHandler.KR);
  GEOHandler.addAddrFunction(setAbbrevNmLocnOnAddressSave, GEOHandler.KR);
  FormManager.skipByteChecks([ 'billingPstlAddr', 'divn', 'custNm3', 'custNm4', 'contact', 'dept', 'poBoxCity', 'countyName' ]);

  GEOHandler.registerValidator(addKRChecklistValidator, GEOHandler.KR);
  GEOHandler.registerValidator(validateCustnameForKynd, GEOHandler.KR);

  // GEOHandler.ROLE_PROCESSOR, true);
  GEOHandler.registerValidator(addDPLCheckValidator, GEOHandler.KR, GEOHandler.ROLE_REQUESTER, true);
  GEOHandler.registerValidator(addAttachmentValidator, GEOHandler.KR);
  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.KR);
  GEOHandler.addAfterConfig(setClientTierValues, GEOHandler.KR);
  GEOHandler.addAfterTemplateLoad(setClientTierValues, GEOHandler.KR);
  // CREATCMR-6825
  GEOHandler.addAfterConfig(setRepTeamMemberNo, GEOHandler.KR);
  GEOHandler.addAfterTemplateLoad(setRepTeamMemberNo, GEOHandler.KR);
  GEOHandler.addAddrFunction(handleObseleteExpiredDataForUpdate, GEOHandler.KR);
  GEOHandler.addAfterConfig(handleObseleteExpiredDataForUpdate, GEOHandler.KR);
  GEOHandler.addAfterTemplateLoad(handleObseleteExpiredDataForUpdate, GEOHandler.KR);
  
  GEOHandler.addAfterConfig(setSearchTermDropdownValues, GEOHandler.KR);
  GEOHandler.addAfterTemplateLoad(setSearchTermDropdownValues, GEOHandler.KR);
  GEOHandler.addAfterConfig(LockDefaultISUClientTierMrcValues, GEOHandler.KR);
  GEOHandler.addAfterTemplateLoad(LockDefaultISUClientTierMrcValues, GEOHandler.KR);
  GEOHandler.addAfterConfig(setInacNacValues, GEOHandler.KR);
  GEOHandler.addAfterTemplateLoad(setInacNacValues, GEOHandler.KR);
  GEOHandler.addAfterConfig(getIsuFromIsic, GEOHandler.KR);
  GEOHandler.addAfterTemplateLoad(getIsuFromIsic, GEOHandler.KR);
  
   //  CREATCMR-8581
  GEOHandler.registerValidator(checkCmrUpdateBeforeImport, GEOHandler.KR,null,true);

});
