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

  FormManager.addValidator('requesterId',Validators.REQUIRED, ['Requested For Name (Originator)'], 'MAIN_GENERAL_TAB');
  FormManager.addValidator('originatorNm', Validators.REQUIRED, [ 'Requested For Name (Originator)' ], 'MAIN_GENERAL_TAB');
  FormManager.addValidator('abbrevNm', Validators.REQUIRED, [ 'Abbreviated Name (TELX1)' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('phone1', Validators.REQUIRED, ['Business License Type'], 'MAIN_CUST_TAB');
  FormManager.addValidator('Phone1', Validators.REQUIRED, ['Business License Type'], 'MAIN_CUST_TAB');
  FormManager.addValidator('installRep', Validators.REQUIRED, ['Tax Invoice Type'], 'MAIN_CUST_TAB');
  
  FormManager.addValidator('contactName3', Validators.REQUIRED,['Product Type'],'MAIN_IBM_TAB');
  FormManager.addValidator('MrcCd', Validators.REQUIRED,['Market Responsibility Code (MRC)'],'MAIN_IBM_TAB');
  FormManager.addValidator('commercialFinanced', Validators.REQUIRED,['ROL Code'],'MAIN_IBM_TAB');
  
  FormManager.removeValidator('subIndustryCd', Validators.REQUIRED);  
  FormManager.removeValidator('sensitiveFlag', Validators.REQUIRED);
  FormManager.removeValidator('LocalTax2', Validators.REQUIRED);
  
  // Non editable for requester role
  if (reqType == 'C' && role == 'Requester') {
    FormManager.readOnly('isuCd');
    FormManager.readOnly('cmrNoPrefix');
  }

  RemoveCrossAddressMandatory();
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

function RemoveCrossAddressMandatory() {
  var custSubScnrio = FormManager.getActualValue('custSubGrp');
  if (custSubScnrio == 'CROSS') {
    FormManager.removeValidator('city2', Validators.REQUIRED);
    FormManager.removeValidator('custNm3', Validators.REQUIRED);
    FormManager.removeValidator('custNm4', Validators.REQUIRED);
  }
}

dojo.addOnLoad(function() {
  GEOHandler.KR = [ '766' ];
  console.log('adding KOREA functions...');
  GEOHandler.enableCustomerNamesOnAddress(GEOHandler.KR);
  GEOHandler.setRevertIsicBehavior(false);
  GEOHandler.addAfterConfig(afterConfigKR, GEOHandler.KR);
  GEOHandler.addAddrFunction(updateMainCustomerNames, GEOHandler.KR);
  GEOHandler.addAfterTemplateLoad(afterConfigKR, GEOHandler.KR);
  // FormManager.skipByteChecks([ 'billingPstlAddr', 'divn', 'custNm3',
  // 'custNm4' ]);
  GEOHandler.addAfterConfig(setChecklistStatus, GEOHandler.KR);
  GEOHandler.registerValidator(addKRChecklistValidator, GEOHandler.KR);
  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.KR, GEOHandler.ROLE_PROCESSOR, true);
  GEOHandler.registerValidator(addDPLCheckValidator, GEOHandler.KR, GEOHandler.ROLE_REQUESTER, true);
});