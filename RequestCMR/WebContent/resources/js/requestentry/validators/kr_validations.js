/* Register KR Javascripts */

function afterConfigKR() {
  var role = null;
  var reqType = null;

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
  FormManager.addValidator('MrcCd', Validators.REQUIRED, [ 'Market Responsibility Code (MRC)' ], 'MAIN_IBM_TAB');
  FormManager.addValidator('commercialFinanced', Validators.REQUIRED, [ 'ROL Code' ], 'MAIN_IBM_TAB');

  FormManager.removeValidator('subIndustryCd', Validators.REQUIRED);
  FormManager.removeValidator('sensitiveFlag', Validators.REQUIRED);
  FormManager.removeValidator('LocalTax2', Validators.REQUIRED);

  // Non editable for requester role
  if (reqType == 'C' && role == 'Requester') {
    FormManager.readOnly('isuCd');
    FormManager.readOnly('cmrNoPrefix');
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
  }
  
  RemoveCrossAddressMandatory();
  setChecklistStatus();
  addKRChecklistValidator();
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
          return new ValidationResult(null, false,
              'Checklist has not been registered yet. Please execute a \'Save\' action before sending for processing to avoid any data loss.');
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
    //var copyingToA = false;          
        if (addrType == 'ZS01') {
          //copyingToA = true;        
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
   
	  if (custNm1 == '')	{
		    	custNm1 = result.ret1;
		    }
		  _abbrevNm = custNm1;
		  
      if ( _abbrevNm && _abbrevNm.length > 21) {
	    	_abbrevNm = _abbrevNm.substring(0, 21);
	      }
	  FormManager.setValue('abbrevNm', _abbrevNm);
      
      switch (_abbrevLocn) {
        case '1':
          FormManager.setValue('abbrevLocn', 'Seoul');
          break;

        case '2':
          FormManager.setValue('abbrevLocn', 'Busan');
          break;

          case '3':
            FormManager.setValue('abbrevLocn', 'Daegu');
          break;

          case '4':
            FormManager.setValue('abbrevLocn', 'Incheon');
          break;

          case '5':
            FormManager.setValue('abbrevLocn', 'Gwangju');
          break;

          case '6':
            FormManager.setValue('abbrevLocn', 'Daejeon');
          break;
          case '7':
            FormManager.setValue('abbrevLocn', 'Ulsan');
          break;
          case '8':
            FormManager.setValue('abbrevLocn', 'Sejong');
          break;
          case '9':
            FormManager.setValue('abbrevLocn', 'Gyeonggi-do');
          break;
          case '10':
            FormManager.setValue('abbrevLocn', 'Gangwon-do');
          break;
          case '11':
            FormManager.setValue('abbrevLocn', 'Chungcheongbuk-do');
          break;

          case '12':
            FormManager.setValue('abbrevLocn', 'Chungcheongnam-do');
          break;

          case '13':
            FormManager.setValue('abbrevLocn', 'Jeollabuk-do');
          break;

          case '14':
            FormManager.setValue('abbrevLocn', 'Jeollanam-do');
          break;

          case '15':
            FormManager.setValue('abbrevLocn', 'Gyeongsangbuk-do');
          break;

          case '16':
            FormManager.setValue('abbrevLocn', 'Gyeongsangnam-do');
          break;

          case '17':
            FormManager.setValue('abbrevLocn', 'Jeju');
          break;
      }
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
  
  // GEOHandler.ROLE_PROCESSOR, true);
  GEOHandler.registerValidator(addDPLCheckValidator, GEOHandler.KR, GEOHandler.ROLE_REQUESTER, true);
  GEOHandler.registerValidator(addAttachmentValidator, GEOHandler.KR);
  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.KR);
});