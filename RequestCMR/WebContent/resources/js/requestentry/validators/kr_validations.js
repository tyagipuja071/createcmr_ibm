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
  FormManager.disable('cmrNoPrefix');

  // Non editable for requester role
  if (reqType == 'C' && role == 'Requester') {
    FormManager.readOnly('isuCd');
  }
  // if (role == 'Requester') {
  // FormManager.readOnly('isuCd');
  // }
  RemoveCrossAddressMandatory();
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
  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.KR, GEOHandler.ROLE_PROCESSOR, true);
  GEOHandler.addAfterTemplateLoad(afterConfigKR, GEOHandler.KR);
  // FormManager.skipByteChecks([ 'billingPstlAddr', 'divn', 'custNm3', 'custNm4' ]);
});