/* Register KR Javascripts */
 var _isicHandler = null;
 var _searchTermHandler = null;
 var _addrTypesForKR = ['ZS01', 'ZP01', 'ZI01', 'ZD01'];
 var _addrTypeHandler = [];

/**
* No single byte characters for KR
*/
function addSingleByteValidatorKR(cntry, addressMode, details) {
  FormManager.addValidator('custNm3', Validators.NO_SINGLE_BYTE, ['Customer Name_Korean']);
  FormManager.addValidator('billingPstlAddr', Validators.NO_SINGLE_BYTE, ['Customer Name_Korean Continue']);
  FormManager.addValidator('custNm4', Validators.NO_SINGLE_BYTE, ['Street address_Korean']);
  FormManager.addValidator('divn', Validators.NO_SINGLE_BYTE, ['Street address_Korean Continue']);
}
 
function afterConfigKR() {
  if (FormManager.getActualValue('userRole').toUpperCase() == 'VIEWER') {
    FormManager.readOnly('ordBlk');
    return;
  }
  
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    FormManager.readOnly('ordBlk');
  }

  var role = null;
  var reqType = null;
  var _isuHandler = null;
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  
  _isicHandler = dojo.connect(FormManager.getField('isicCd'), 'onChange', function(value) {
    getIsuFromIsic();
    setKUKLAvaluesKR();
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
// FormManager.setValue('inacType','');
// FormManager.setValue('inacCd','');
    FormManager.enable('searchTerm');
    FormManager.enable('isicCd');
    FormManager.enable('inacType');
    FormManager.enable('inacCd');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('mrcCd');

    setKUKLAvaluesKR();
  });

  var _clusterHandler = dojo.connect(FormManager.getField('searchTerm'), 'onChange', function (value) {
    FormManager.resetDropdownValues(FormManager.getField('inacType'));
    FormManager.resetDropdownValues(FormManager.getField('inacCd'));
    FormManager.resetDropdownValues(FormManager.getField('isuCd'));
    // FormManager.setValue('inacType','');
    // FormManager.setValue('inacCd','');
    // LockDefaultISUClientTierMrcValues();
    setInacNacValues(value);
    if (reqType == 'C') {
      FormManager.readOnly('clientTier');
      FormManager.readOnly('isuCd');
      FormManager.readOnly('mrcCd');
    }
  });

  var _inacType = dojo.connect(FormManager.getField('inacType'), 'onChange', function (value) {
    setInacNacValues("inacChange");
  });
  
// var _inacType = dojo.connect(FormManager.getField('inacType'), 'onChange',
// function(value) {
// setInacNacValues();
// });

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

  FormManager.addValidator('abbrevNm', Validators.REQUIRED, ['Abbreviated Name (TELX1)'], 'MAIN_CUST_TAB');
  FormManager.addValidator('phone1', Validators.REQUIRED, ['Business License Type'], 'MAIN_CUST_TAB');
  FormManager.addValidator('installRep', Validators.REQUIRED, ['Tax Invoice Type'], 'MAIN_CUST_TAB');

  FormManager.addValidator('contactName3', Validators.REQUIRED, ['Product Type'], 'MAIN_IBM_TAB');
  // FormManager.addValidator('MrcCd', Validators.REQUIRED, [ 'Market
  // Responsibility Code (MRC)' ], 'MAIN_IBM_TAB');
  FormManager.addValidator('commercialFinanced', Validators.REQUIRED, ['ROL Code'], 'MAIN_IBM_TAB');

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
    FormManager.addValidator('MrcCd', Validators.REQUIRED, ['Market Responsibility Code (MRC)'], 'MAIN_IBM_TAB');

    FormManager.readOnly('custClass');
  }

  // story: attachment Company Proof required
  var custSubType = FormManager.getActualValue('custSubGrp');
  if ((reqType == 'C' || reqType == 'U') && (custSubType != 'INTER')) {
    // FormManager.addValidator('DocContent',Validators.REQUIRED,['Company Proof
    // required']);
    FormManager.addValidator('docContent', Validators.REQUIRED, ['${ui.content}'], 'MAIN_ATTACH_TAB');
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
  if (reqType == 'C') {
    FormManager.readOnly('clientTier');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('mrcCd');
  }
  
  for (var i = 0; i < _addrTypesForKR.length; i++) {
    _addrTypeHandler[i] = null;
    if (_addrTypeHandler[i] == null) {
      _addrTypeHandler[i] = dojo.connect(FormManager.getField('addrType_' + _addrTypesForKR[i]), 'onClick', function(value) {
        FormManager.clearValue('locationCode');
        setLockUnlockSeqNum();
      });
    }
  }
}

function replaceAndSymbol(value) {
  var modifiedVal = '';
  if (value != null && value.length > 0) {
    modifiedVal = value;
    modifiedVal = modifiedVal.replace(/&/g, '＆');
  }

  return modifiedVal;
};

function replaceCrossbarSymbol(value) {
  var modifiedVal = '';
  if (value != null && value.length > 0) {
    modifiedVal = value;
    modifiedVal = modifiedVal.replace(/-/g, '－');
    modifiedVal = modifiedVal.replace(/−/g, "－");
    modifiedVal = modifiedVal.replace(/･/g, '・');

    modifiedVal = modifiedVal.replace(/,/g, '，');
    modifiedVal = modifiedVal.replace(/:/g, '：');
    modifiedVal = modifiedVal.replace(/_/g, '＿');
    modifiedVal = modifiedVal.replace(/\(/g, '（');
    modifiedVal = modifiedVal.replace(/\)/g, '）');
  }

  return modifiedVal;
};

function convert2DBCSIgnoreCase(input) {
  var modifiedVal = '';
  if (input != null && input.length > 0) {
    modifiedVal = input;
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

function setLockUnlockSeqNum(cntry, addressMode, details) {
  var addrType = FormManager.getActualValue('addrType');
  
  if (addressMode == 'newAddress' || addressMode == 'copyAddress') {
    FormManager.clearValue('locationCode');
  } else if (addressMode == 'updateAddress') {
    addrType = details != null ? details.ret2 : '';
  }
  
  if(addrType == 'ZP01' && addressMode != 'updateAddress') {
    FormManager.enable('locationCode');
    FormManager.addValidator('locationCode', Validators.REQUIRED, [ 'Seq/Loc Code' ], '');
  } else {
    FormManager.readOnly('locationCode');
    FormManager.removeValidator('locationCode', Validators.REQUIRED);
  }
}

function setClientTierValues() {
  var reqType = FormManager.getActualValue('reqType');

  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  isuCd = FormManager.getActualValue('isuCd');
  if (isuCd == '5K') {
    FormManager.removeValidator('clientTier', Validators.REQUIRED);
    FormManager.readOnly('clientTier');
  } else {
    if (reqType != 'U') {
      FormManager.addValidator('clientTier', Validators.REQUIRED, ['Client Tier'], 'MAIN_IBM_TAB');
    }
  }
  handleObseleteExpiredDataForUpdate();
  if (reqType == 'C') {
    FormManager.readOnly('clientTier');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('mrcCd');
  }
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
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
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
          REQID: reqId
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
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
        var reqType = FormManager.getActualValue('reqType');
        var custSubType = FormManager.getActualValue('custSubGrp');
        var cntryUse = FormManager.getActualValue('cntryUse');
        // var docContent = FormManager.getActualValue('docContent');
        if (typeof (_pagemodel) != 'undefined') {
          if ((reqType == 'C' || reqType == 'U') && (custSubType != 'INTER')) {
            var id = FormManager.getActualValue('reqId');
            var ret = cmr.query('CHECK_DNB_MATCH_ATTACHMENT', {
              ID: id
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
    REQ_ID: zs01ReqId,
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
    FormManager.readOnly('repTeamMemberNo');
    FormManager.readOnly('covId');
    FormManager.readOnly('commercialFinanced');
    FormManager.readOnly('contactName2');
    FormManager.readOnly('contactName3');
    FormManager.readOnly('cmrNoPrefix');
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
  FormManager.addValidator('abbrevNm', Validators.NO_QUOTATION, ['Abbreviated Name (TELX1)'], 'MAIN_CUST_TAB');
  FormManager.addValidator('abbrevLocn', Validators.NO_QUOTATION, ['Abbreviated Location'], 'MAIN_CUST_TAB');
  FormManager.addValidator('custNm1', Validators.NO_QUOTATION, ['Customer Name 1']);
  FormManager.addValidator('custNm2', Validators.NO_QUOTATION, ['Customer Name Con\'t']);
  FormManager.addValidator('custNm3', Validators.NO_QUOTATION, ['Customer Name_Korean']);
  FormManager.addValidator('billingPstlAddr', Validators.NO_QUOTATION, ['Customer Name_Korean Continue']);

  FormManager.addValidator('addrTxt', Validators.NO_QUOTATION, ['Street Address 1']);
  FormManager.addValidator('addrTxt2', Validators.NO_QUOTATION, ['Address Con\'t']);
  FormManager.addValidator('custNm4', Validators.NO_QUOTATION, ['Street address_Korean']);
  FormManager.addValidator('divn', Validators.NO_QUOTATION, ['Street address_Korean Continue']);

  FormManager.addValidator('city1', Validators.NO_QUOTATION, ['City']);
  FormManager.addValidator('city2', Validators.NO_QUOTATION, ['City Korean']);
  FormManager.addValidator('postCd', Validators.NO_QUOTATION, ['Postal Code']);
  FormManager.addValidator('transportZone', Validators.NO_QUOTATION, ['Transport Zone']);

  FormManager.addValidator('contact', Validators.NO_QUOTATION, ['Name of person in charge of Invoice_1']);
  FormManager.addValidator('countyName', Validators.NO_QUOTATION, ['Name of person in charge of Invoice_2']);
  FormManager.addValidator('dept', Validators.NO_QUOTATION, ['Department Name_1']);
  FormManager.addValidator('poBoxCity', Validators.NO_QUOTATION, ['Department Name_2']);

  FormManager.addValidator('poBox', Validators.NO_QUOTATION, ['Telephone No_1']);
  FormManager.addValidator('taxOffice', Validators.NO_QUOTATION, ['Telephone No_2']);
  FormManager.addValidator('floor', Validators.NO_QUOTATION, ['E-mail_1']);
  FormManager.addValidator('office', Validators.NO_QUOTATION, ['E-mail_2']);
  FormManager.addValidator('bldg', Validators.NO_QUOTATION, ['E-mail_3']);
  FormManager.addValidator('custPhone', Validators.NO_QUOTATION, ['Company Phone #']);

}

function setInacNacValues(searchTermChange) {
  var _cluster = FormManager.getActualValue('searchTerm');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var isInacRetrieved = false;

  FormManager.addValidator('inacCd', Validators.REQUIRED, ['INAC/NAC Code'], 'MAIN_IBM_TAB');
  FormManager.addValidator('inacType', Validators.REQUIRED, ['INAC Type'], 'MAIN_IBM_TAB');

  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (!_cluster) {
    if (_cluster == '' || _cluster == undefined) {
      console.log('>>>> EMPTY INAC/INACTYPE when cluster is not valid >>>>');
      FormManager.limitDropdownValues(FormManager.getField('inacCd'), []);
      FormManager.limitDropdownValues(FormManager.getField('inacType'), []);
      FormManager.setValue('inacCd', '');
      FormManager.setValue('inacType', '');
    }
    return;
  }

  var qParams = {
    _qall: 'Y',
    ISSUING_CNTRY: cntry,
    CMT: '%' + _cluster + '%'
  };
  var inacList = cmr.query('GET.INAC_BY_CLUSTER', qParams);
  if (inacList != null && inacList.length > 0) {
    isInacRetrieved = true;
    var inacTypeSelected = '';
    var arr = inacList.map(inacList => inacList.ret1);
    inacTypeSelected = inacList.map(inacList => inacList.ret2);
    FormManager.limitDropdownValues(FormManager.getField('inacCd'), arr);
    console.log('>>> setInacByCluster >>> arr = ' + arr);
    if (inacList.length == 1) {
      FormManager.setValue('inacCd', arr[0]);
      FormManager.readOnly('inacType');
      FormManager.readOnly('inacCd');
    } else {
      FormManager.enable('inacType');
      FormManager.enable('inacCd');
    }
    if (inacType != '' && inacTypeSelected[0].includes(",I") && !inacTypeSelected[0].includes(',IN')) {
      FormManager.limitDropdownValues(FormManager.getField('inacType'), 'I');
      FormManager.setValue('inacType', 'I');
    } else if (inacType != '' && inacTypeSelected[0].includes(',N')) {
      FormManager.limitDropdownValues(FormManager.getField('inacType'), 'N');
      FormManager.setValue('inacType', 'N');
    } else if (inacType != '' && inacTypeSelected[0].includes(',IN')) {
      FormManager.resetDropdownValues(FormManager.getField('inacType'));
      var value = FormManager.getField('inacType');
      var cmt = value + ',' + _cluster + '%';
      var value = FormManager.getActualValue('inacType');
      var cntry = FormManager.getActualValue('cmrIssuingCntry');
      console.log('>>> setInacByCluster >>> value = ' + value);
      if (value != null) {
        var inacCdValue = [];
        var qParams = {
          _qall: 'Y',
          ISSUING_CNTRY: cntry,
          CMT: cmt,
        };
        var results = cmr.query('GET.INAC_CD', qParams);
        if (results != null) {
          for (var i = 0; i < results.length; i++) {
            inacCdValue.push(results[i].ret1);
          }
          FormManager.limitDropdownValues(FormManager.getField('inacCd'), inacCdValue);
          // FormManager.setValue('inacCd', inacCdValue[0]);
          if (inacCdValue.length == 1) {
            FormManager.setValue('inacCd', inacCdValue[0]);
          }
        }
      }
    } else {
      FormManager.resetDropdownValues(FormManager.getField('inacType'));
    }
  } else {
    if (!isInacRetrieved) {
      FormManager.resetDropdownValues(FormManager.getField('inacType'));
      FormManager.resetDropdownValues(FormManager.getField('inacCd'));
      FormManager.removeValidator('inacCd', Validators.REQUIRED);
      FormManager.removeValidator('inacType', Validators.REQUIRED);
      FormManager.enable('inacCd');
      FormManager.enable('inacType');
    }
    if (searchTermChange != "inacChange") {
      FormManager.clearValue('inacType');
    } else {
      var inacCodes = FormManager.getField('inacCd').loadedStore._arrayOfAllItems.filter(function checkInacFinal(inacNode) {
        return /^[0-9]+$/.test(inacNode.id[0]);
      });
      var nacCodes = FormManager.getField('inacCd').loadedStore._arrayOfAllItems.filter(function checkInacFinal(inacNode) {
        return /^[A-Z]/.test(inacNode.id[0]);
      });
      if (FormManager.getActualValue('inacType') == 'I') {
        var actualInacList = [];
        for (var i = 0; i < inacCodes.length; i++) {
          actualInacList.push(inacCodes[i].id[0]);
        }
        FormManager.limitDropdownValues(FormManager.getField('inacCd'), actualInacList);
      } else if (FormManager.getActualValue('inacType') == 'N') {
        var actualNacList = [];
        for (var i = 0; i < nacCodes.length; i++) {
          actualNacList.push(nacCodes[i].id[0]);
        }
        FormManager.limitDropdownValues(FormManager.getField('inacCd'), actualNacList);
      }
    }
    FormManager.clearValue('inacCd');
  }
}

function validateCustnameForKynd() {
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
        var errorMsg = '';
        var action = FormManager.getActualValue('yourAction');

// var custNm1 = FormManager.getActualValue('mainCustNm1').toUpperCase();
        var custNm1 = '';
        if (_allAddressData == undefined || _allAddressData == null || _allAddressData.length == 0) {
          return;
        }
        for (var i=0; i<_allAddressData.length; i++) {  
          if (_allAddressData[i]['addrType'][0] == 'ZS01') {
            var custNm1 = _allAddressData[i]['custNm1'][0].toUpperCase();
            break;
          }
        }
        if (custNm1.length == 0) {
          return;
        }

        var reqType = FormManager.getActualValue('reqType');
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        if (reqType == 'C' && custSubGrp == 'LKYN') {
          if (custNm1.indexOf('KYNDRYL') == -1) {
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

function getIsuFromIsic() {
  var searchTerm = FormManager.getActualValue('searchTerm');
  var isicCd = FormManager.getActualValue('isicCd');
  if (!(searchTerm == '04461' || searchTerm == '04466' || searchTerm == '05223')) {
    return;
  }
  var ISU = [];
  if (isicCd != '') {
    var qParams = {
      _qall: 'Y',
      ISSUING_CNTRY: '856',
      REP_TEAM_CD: '%' + isicCd + '%'
    };
    var results = cmr.query('GET.ISULIST.BYISIC', qParams);
    if (results != null) {
      for (var i = 0; i < results.length; i++) {
        ISU.push(results[i].ret1);
      }
      if (ISU != null) {
        FormManager.setValue('isuCd', '');
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

function setCTCIsuMrcByCluster() {
  console.log('>>>> setCTCIsuByClusterANZ >>>>');
  var reqType = FormManager.getActualValue('reqType');
  if (FormManager.getActualValue('viewOnlyPage') == 'true' || reqType != 'C') {
    return;
  }
  var _clusterHandler = dojo.connect(FormManager.getField('searchTerm'), 'onChange', function (value) {
    var role = FormManager.getActualValue('userRole').toUpperCase();
    var scenario = FormManager.getActualValue('custGrp');
    var custSubGrp = FormManager.getActualValue('custSubGrp');
    var clusterVal = FormManager.getActualValue('searchTerm');
    if (!clusterVal || clusterVal == '00003') {
      if (custSubGrp == 'CROSS') {
        FormManager.resetDropdownValues(FormManager.getField('clientTier'));
        FormManager.resetDropdownValues(FormManager.getField('isuCd'));
        FormManager.setValue('isuCd', '34');
        FormManager.setValue('clientTier', 'Z');
      }
      if (custSubGrp != 'INTER') {
        FormManager.setValue('mrcCd', '3');
      }
      return;
    }
    var _cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
    var _cluster = FormManager.getActualValue('searchTerm');
    var scenario = FormManager.getActualValue('custGrp');
    var custSubGrp = FormManager.getActualValue('custSubGrp');

    var apClientTierValue = [];
    var isuCdValue = [];
    var mrcCdValue = null;
    if (_cluster != '' && _cluster != '') {
      var qParams = {
        _qall: 'Y',
        ISSUING_CNTRY: _cmrIssuingCntry,
        CLUSTER: _cluster,
      };
      // cluster description
      var clusterDesc = cmr.query('GET.DESC_BY_CLUSTER', qParams);
      var qParams = {
        _qall: 'Y',
        ISSUING_CNTRY: _cmrIssuingCntry,
        CLUSTER: _cluster,
      };

      var results = cmr.query('GET.CTC_ISU_BY_CLUSTER_CNTRY', qParams);
      if (results != null) {
        for (var i = 0; i < results.length; i++) {
          apClientTierValue.push(results[i].ret1);
          isuCdValue.push(results[i].ret2);
        }
        if (apClientTierValue.length == 1) {
          FormManager.limitDropdownValues(FormManager.getField('clientTier'), apClientTierValue);
          FormManager.setValue('clientTier', apClientTierValue[0]);
          FormManager.readOnly('clientTier');
          if (isuCdValue.length == 1 && isuCdValue[0].trim().length > 0) {
            FormManager.limitDropdownValues(FormManager.getField('isuCd'), isuCdValue);
            FormManager.setValue('isuCd', isuCdValue[0]);
            FormManager.readOnly('isuCd');
          }
        } else if (apClientTierValue.length > 1) {
          FormManager.resetDropdownValues(FormManager.getField('clientTier'));
          FormManager.limitDropdownValues(FormManager.getField('clientTier'), apClientTierValue);
          FormManager.limitDropdownValues(FormManager.getField('isuCd'), isuCdValue);
        }
      }
      qParams = {
        _qall: 'Y',
        ISSUING_CNTRY: _cmrIssuingCntry,
        CD: _cluster
      };
      results = cmr.query('GET.MRC_BY_CLUSTER', qParams);
      if (results != null && results.length) {
        mrcCdValue = results[0].ret1;
      }
      if (mrcCdValue != null && mrcCdValue.length > 0) {
        FormManager.setValue('mrcCd', mrcCdValue.charAt(3));
        FormManager.readOnly('mrcCd');
      } else {
        FormManager.enable('mrcCd');
      }
    }

  });
  if (_clusterHandler && _clusterHandler[0]) {
    _clusterHandler[0].onChange();
  }
}

function setKUKLAvaluesKR() {
  var reqType = FormManager.getActualValue('reqType');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var isicCd = FormManager.getActualValue('isicCd');

  var subIndustryCd = FormManager.getActualValue('subIndustryCd');
  if (subIndustryCd != null && subIndustryCd.length > 1) {
    var industryClass = subIndustryCd.substr(0, 1);
  }

  if (FormManager.getActualValue('reqType') == 'U') {
    return
  }

  console.log('>>>> setKUKLAvaluesKR() >>>> set KUKLA values for KR >>>>');

  var custSubGrp1 = new Set(['BLUMX', 'ESA', 'ECOSY', 'LKYN', 'NRST', 'VAPAR']);
  var custSubGrp2 = new Set(['AQSTN', 'NRML', 'CROSS']);
  var custSubGrp3 = new Set(['CBBUS']);
  var custSubGrp4 = new Set(['PRIPE']);
  var custSubGrp5 = new Set(['MKTPC']);
  var custSubGrp6 = new Set(['INTER']);

  var industryClass1 = new Set(['G', 'H', 'Y']);
  var industryClass2 = new Set(['E']);

  var kuklaKR = [];
  if (reqType == 'C') {
    FormManager.readOnly('custClass');
    var qParams = {
      _qall: 'Y',
      ISSUING_CNTRY: cntry,
    };
    var results = cmr.query('GET.AP_KUKLA', qParams);
    if (results != null) {
      for (var i = 0; i < results.length; i++) {
        kuklaKR.push(results[i].ret1);
      }
    }

    if (results != null) {
      if (custSubGrp1.has(custSubGrp)) {
        FormManager.setValue('custClass', kuklaKR[0]);
      } else if (custSubGrp2.has(custSubGrp)) {
        if (industryClass1.has(industryClass)) {
          FormManager.setValue('custClass', kuklaKR[1]);
        } else if (industryClass2.has(industryClass)) {
          FormManager.setValue('custClass', kuklaKR[2]);
        } else {
          FormManager.setValue('custClass', kuklaKR[0]);
        }
      } else if (custSubGrp3.has(custSubGrp)) {
        FormManager.setValue('custClass', kuklaKR[3]);
      } else if (custSubGrp4.has(custSubGrp)) {
        FormManager.setValue('custClass', kuklaKR[4]);
      } else if (custSubGrp5.has(custSubGrp)) {
        if (isicCd == '9500') {
          FormManager.setValue('custClass', kuklaKR[4]);
        } else {
          FormManager.setValue('custClass', kuklaKR[0]);
        }
      } else if (custSubGrp6.has(custSubGrp)) {
        FormManager.setValue('custClass', kuklaKR[5]);
      }
    }
  }
}

function addSeqNumFormatValidator() {
  console.log(">>>>  addSeqNumFormatValidator");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var addrType = FormManager.getActualValue('addrType');
        if(addrType != 'ZP01') {
          return new ValidationResult(null, true);
        }
        
        var seqNum = FormManager.getActualValue('locationCode');
        var isValidSeqNumFormat = /^B\d{2}$/.test(seqNum);

        if (!isValidSeqNumFormat) {
          return new ValidationResult(null, false, 'Seq/Loc Code value should start with \'B\' followed by 2 numeric characters.');
        } 
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function addSeqNumDuplicateValidator() {
  console.log(">>>>  addSeqNumDuplicateValidator");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var addrType = FormManager.getActualValue('addrType');

        if(addrType != 'ZP01' || cmr.addressMode == 'updateAddress') {
          return new ValidationResult(null, true);
        }
        
        var requestId = FormManager.getActualValue('reqId');
        var seqNum = FormManager.getActualValue('locationCode');

        var addrSeqCount = cmr.query('ADDR_SEQ.COUNT', {
          REQ_ID : requestId,
          ADDR_TYPE : addrType,
          ADDR_SEQ : seqNum
        });
        
        if (addrSeqCount.ret1 > 0) {
          return new ValidationResult(null, false, 'Sec./Loc Code \'' + seqNum + '\' already exists. Please provide a different Sec./Loc Code.');
        } 
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function addKRAddressGridValidator() {
  console.log(">>>> addKRAddressGridValidator ");
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
        if (FormManager.getActualValue('cmrIssuingCntry') != SysLoc.KOREA) {
          return new ValidationResult(null, true);
        }

        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          var record = null;
          var type = null;

          var custNm3 = '';
          var custNm4 = '';

          for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
            record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
            if (record == null && _allAddressData != null && _allAddressData[i] != null) {
              record = _allAddressData[i];
            }
            type = record.addrType;
            if (typeof (type) == 'object') {
              type = type[0];
            }

            // Customer Name_Korean
            var isCustNm3Filled = (record.custNm3[0] != null && record.custNm3[0] != '');
            if (!isCustNm3Filled) {
              if (custNm3 != '') {
                custNm3 += ', ' + record.addrTypeText[0];
              } else {
                custNm3 += record.addrTypeText[0];
              }
            }

            // Street address_Korean
            var isCustNm4Filled = (record.custNm4[0] != null && record.custNm4[0] != '');
            if (!isCustNm4Filled) {
              if (custNm4 != '') {
                custNm4 += ', ' + record.addrTypeText[0];
              } else {
                custNm4 += record.addrTypeText[0];
              }
            }
          }

          if (custNm3 != '') {
            return new ValidationResult(null, false, 'Please fill out Customer Name_Korean for the following address: ' + custNm3);
          } else if (custNm4 != '') {
            return new ValidationResult(null, false, 'Please fill out Street address_Korean for the following address: ' + custNm4);
          }

          return new ValidationResult(null, true);

        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function setAddrFieldMandatory(id, fieldId, fieldLblDesc) {
  FormManager.show(fieldId, id);
  FormManager.addValidator(id, Validators.REQUIRED, [fieldLblDesc]);
}

function setAddrFieldsMandatoryForUpdtReq() {
  if (cmr.addressMode == 'newAddress' || cmr.addressMode == 'updateAddress') {
    setAddrFieldMandatory('custNm3', 'CustomerName3', 'Customer Name_Korean');
    setAddrFieldMandatory('custNm4', 'CustomerName4', 'Street address_Korean');
  }
}

function canRemoveAddress(value, rowIndex, grid) {
  console.log('>>>> canRemoveAddress >>>>');
  var rowData = grid.getItem(rowIndex);
  var importInd = rowData.importInd[0];
  var reqType = FormManager.getActualValue('reqType');
  if ('U' == reqType && 'Y' == importInd) {
    return false;
  }
  return true;
}

function ADDRESS_GRID_showCheck(value, rowIndex, grid) {
  return canRemoveAddress(value, rowIndex, grid);
}

// Customer Name_Korean
function convertCustNmKRToDBCS() {
  dojo.connect(FormManager.getField('custNm3'), 'onChange', function (value) {
    var custNmKr = FormManager.getActualValue('custNm3');

    FormManager.setValue('custNm3', convert2DBCSIgnoreCase(custNmKr));
    custNmKr = FormManager.getActualValue('custNm3');
  });
}

// Customer Name_Korean Continue
function convertCustNmKRContToDBCS() {
  dojo.connect(FormManager.getField('billingPstlAddr'), 'onChange', function (value) {
    var custNmKRCont = FormManager.getActualValue('billingPstlAddr');

    FormManager.setValue('billingPstlAddr', convert2DBCSIgnoreCase(custNmKRCont));
    custNmKRCont = FormManager.getActualValue('billingPstlAddr');
  });
}

// Street address_Korean
function convertStAddrKRToDBCS() {
  dojo.connect(FormManager.getField('custNm4'), 'onChange', function (value) {
    var stAddrKR = FormManager.getActualValue('custNm4');

    FormManager.setValue('custNm4', convert2DBCSIgnoreCase(stAddrKR));
    stAddrKR = FormManager.getActualValue('custNm4');
  });
}

// Street address_Korean Continue
function convertStAddrKRContContToDBCS() {
  dojo.connect(FormManager.getField('divn'), 'onChange', function (value) {
    var stAddrKRCont = FormManager.getActualValue('divn');

    FormManager.setValue('divn', convert2DBCSIgnoreCase(stAddrKRCont));
    stAddrKRCont = FormManager.getActualValue('divn');
  });
}

function addCustNmAndStAddrKRLenghtValidator() {
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
        var custNmKR = FormManager.getActualValue('custNm3');
        var custNmKRCont = FormManager.getActualValue('billingPstlAddr');

        var stAddrKR = FormManager.getActualValue('custNm4');
        var stAddrKRCont = FormManager.getActualValue('divn');

        if (custNmKR.length > 23) {
          return new ValidationResult(null, false, "Customer Name_Korean should be 23 double-byte characters max.");
        } else if (custNmKRCont.length > 23) {
          return new ValidationResult(null, false, "Customer Name_Korean Continue should be 23 double-byte characters max.");
        }

        if (stAddrKR.length > 23) {
          return new ValidationResult(null, false, "Street address_Korean should be 23 double-byte characters max.");
        } else if (stAddrKRCont.length > 23) {
          return new ValidationResult(null, false, "Street address_Korean Continue should be 23 double-byte characters max.");
        }

        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
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
  FormManager.skipByteChecks(['billingPstlAddr', 'divn', 'custNm3', 'custNm4', 'contact', 'dept', 'poBoxCity', 'countyName']);
  GEOHandler.addToggleAddrTypeFunction(setLockUnlockSeqNum, GEOHandler.KR);
  GEOHandler.registerValidator(addSeqNumFormatValidator, GEOHandler.KR);
  GEOHandler.registerValidator(addSeqNumDuplicateValidator, GEOHandler.KR);

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

  GEOHandler.registerValidator(addCustNmAndStAddrKRLenghtValidator, [SysLoc.KOREA], null, true);

  GEOHandler.addAddrFunction(setAddrFieldsMandatoryForUpdtReq, GEOHandler.KR);
  GEOHandler.addToggleAddrTypeFunction(addSingleByteValidatorKR, GEOHandler.KR);
  GEOHandler.addToggleAddrTypeFunction(convertCustNmKRToDBCS, GEOHandler.KR);
  GEOHandler.addToggleAddrTypeFunction(convertCustNmKRContToDBCS, GEOHandler.KR);
  GEOHandler.addToggleAddrTypeFunction(convertStAddrKRToDBCS, GEOHandler.KR);
  GEOHandler.addToggleAddrTypeFunction(convertStAddrKRContContToDBCS, GEOHandler.KR);
  
// GEOHandler.addAfterConfig(setInacNacValues, GEOHandler.KR);
// GEOHandler.addAfterTemplateLoad(setInacNacValues, GEOHandler.KR);
  GEOHandler.addAfterConfig(getIsuFromIsic, GEOHandler.KR);
  GEOHandler.addAfterTemplateLoad(getIsuFromIsic, GEOHandler.KR);
  GEOHandler.addAfterConfig(setCTCIsuMrcByCluster, GEOHandler.KR);
  GEOHandler.addAfterTemplateLoad(setCTCIsuMrcByCluster, GEOHandler.KR);

  // CREATCMR-8581
  GEOHandler.registerValidator(checkCmrUpdateBeforeImport, GEOHandler.KR, null, true);

  GEOHandler.registerValidator(addKRAddressGridValidator, [SysLoc.KOREA], null, true);
  
});
