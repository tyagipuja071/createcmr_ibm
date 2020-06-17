/* Register AP Javascripts */

function addAfterConfigAP() {
  
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var reqType = FormManager.getActualValue('reqType');
  var custType = FormManager.getActualValue('custGrp');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var custGrp = FormManager.getActualValue('custGrp');
  if (reqType == 'U') {
    FormManager.removeValidator('vat', Validators.REQUIRED);
  }
  
  if(cntry == '834' && reqType == 'C' && role == 'PROCESSOR' && custType == 'CROSS' && custSubGrp == 'SPOFF'){
	  FormManager.addValidator('cmrNo', Validators.REQUIRED, [ 'CMR Number' ], 'MAIN_IBM_TAB');
  }
   else{
	  FormManager.removeValidator('cmrNo', Validators.REQUIRED);
  }
  
  
  // Story 1681465 - Code reverted
  /*
   * if (custSubGrp == 'BLUMX' || custSubGrp == 'MKTPC' || custSubGrp == 'XBLUM' ||
   * custSubGrp == 'XMKTP'){ FormManager.show('RestrictedInd', 'restrictInd'); }
   * else { FormManager.hide('RestrictedInd','restrictInd'); }
   */
  
  if (FormManager.getActualValue('viewOnlyPage') == 'true')
    FormManager.readOnly('repTeamMemberName');
    FormManager.readOnly('isbuCd');
   
  if(role == 'REQUESTER' || role == 'VIEWER') {
    FormManager.readOnly('isbuCd');
   if( role == 'VIEWER')
    FormManager.readOnly('abbrevNm');
    FormManager.readOnly('sectorCd');
    FormManager.readOnly('abbrevLocn');
    FormManager.readOnly('territoryCd');
    FormManager.readOnly('IndustryClass');
    FormManager.readOnly('subIndustryCd');
  } else {
    FormManager.enable('isbuCd');
    FormManager.enable('abbrevNm');
    FormManager.enable('sectorCd');
    FormManager.enable('abbrevLocn');
    FormManager.enable('territoryCd');
    FormManager.enable('IndustryClass');
    FormManager.enable('subIndustryCd');
  }
  
  if(role != 'PROCESSOR' && (cntry == '643' || cntry == '749' || cntry == '778' || cntry == '818' || cntry == '834' || cntry == '852' || cntry == '856')){
    FormManager.readOnly('miscBillCd');
  }
  
  if(reqType == 'C' && custType == 'LOCAL' && (cntry == '738' || cntry == '736')){
    FormManager.readOnly('postCd');
  }else{
    if(reqType != 'C' && custType != 'LOCAL' && (cntry == '738' || cntry == '736')){
      FormManager.enable('postCd');
    }
  }
  
  if(reqType == 'U' && (cntry == '738' || cntry == '736')){
    FormManager.readOnly('postCd');
  }
  
  if(reqType == 'C' && custGrp == 'CROSS' && cntry == '736'){
    FormManager.enable('postCd');
  }
  
  if(reqType == 'C' && role == 'PROCESSOR' && custGrp == 'CROSS' && custSubGrp == 'SPOFF' && cntry == '834'){
	  FormManager.enable('cmrNo');
  }else{
	  FormManager.readOnly('cmrNo');
  }
  
  var streetAddressCont1 = FormManager.getActualValue('addrTxt2');
  if((cntry == '738' || cntry == '736') && (streetAddressCont1 == '' || streetAddressCont1 == null)){
    return new ValidationResult({
      id: 'addrTxt2',
      type: 'text',
      name: 'addrTxt2'
    },
    false, 'Street Address Con'+"'"+'t1 is required '
    );
  }
  
  if(role == 'REQUESTER' && reqType == 'C' ){
    if(cntry == SysLoc.SINGAPORE) {
      if(custSubGrp == 'DUMMY' || custSubGrp == 'BLUMX' || custSubGrp == 'MKTPC')
      FormManager.readOnly('clientTier');
      else
      FormManager.enable('clientTier');
      }
    if(cntry == SysLoc.PHILIPPINES){
     if(custSubGrp == 'DUMMY' || custSubGrp == 'XDUMM')
     FormManager.readOnly('clientTier');  
     else
     FormManager.enable('clientTier');
    }
    if(cntry == SysLoc.NEW_ZEALAND){
      if(custSubGrp == 'INTER' || custSubGrp == 'XINT' || custSubGrp == 'DUMMY' || custSubGrp == 'XDUMM' || custSubGrp == 'BLUMX' || custSubGrp == 'XBLUM' || custSubGrp == 'MKTPC' || custSubGrp == 'XMKTP')
      FormManager.readOnly('clientTier');  
      else
      FormManager.enable('clientTier');
      }
    if(cntry == SysLoc.MALASIA){
      if(custSubGrp == 'DUMMY' || custSubGrp == 'XDUMM')
      FormManager.readOnly('clientTier');  
      else
      FormManager.enable('clientTier');
      }
    }   
}

 /* SG defect : 1795335 */

function addFormatForCMRNumValidator() {	
  FormManager.addFormValidator((function() {
	  return {
	  validate : function() {
	  var cmrNo = FormManager.getActualValue('cmrNo');
	  var reqType = FormManager.getActualValue('reqType');
	  var role = FormManager.getActualValue('userRole').toUpperCase();
	  var custGrp = FormManager.getActualValue('custGrp');
	  var custSubGrp = FormManager.getActualValue('custSubGrp');
	  if(reqType == 'C' && role == 'PROCESSOR' && custGrp == 'CROSS' && custSubGrp == 'SPOFF'){
	  if (cmrNo && cmrNo.length > 0 && !cmrNo.match("^[0-9]+$")) {
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




/*
 * Story : 1787526 and Story 1813153 and defect 1792206 - APGCG Address Field
 * Format
 */

function addFormatFieldValidator() {
	  FormManager.addFormValidator((function() {
	  return {
	  validate : function() {
	  var city1 = FormManager.getActualValue('city1');
	  var custNm1 = FormManager.getActualValue('custNm1');
	  var custNm2 = FormManager.getActualValue('custNm2');
	  var addrTxt = FormManager.getActualValue('addrTxt');
	  var addrTxt2 = FormManager.getActualValue('addrTxt2');
	  var stateProv = FormManager.getActualValue('stateProv');
	  var dept = FormManager.getActualValue('dept');
	  // var reqType = FormManager.getActualValue('reqType');
	  // var role = FormManager.getActualValue('userRole').toUpperCase();
	
	  // if(role = 'REQUESTER' || role == 'PROCESSOR'){
	  
	  // if (city1 && city1.length > 0 &&
    // !city1.match("([0-9]{15})|^(X{3})$|^(x{3})$")) {
	  if (city1 && city1.length > 0 && !city1.match("([\\/]+$)|^([\\.]+$)|^([-]+$)|^([#]+$)|^([,]+$)|^([:]+$)|^([']+$)|^([&]+$)|^([;]+$)|^([0-9A-Za-z-#;:,'&()\\s+\\.\\/]+$)|^[(]+$|^[)]+$")) {
	  return new ValidationResult({
	  id : 'city1',
	  type : 'text',
	  name : 'city1'
	  }, false, 'Only the characters listed are allowed to input due to system limitation.' + 
      '   (1) A to Z (uppercase & lowercase)  ' + ' (2) 0 to 9   ' + " (3) '   " + ' (4) ,   ' + ' (5) :   ' + ' (6) &   ' + '  (7) (   ' + ' (8) ;   ' + ' (9) )   ' + ' (10) -   ' + ' (11)  #   ' + ' (12) .  '+' (13) /   ');	  
	  }
	  
	  if (custNm1 && custNm1.length > 0 && !custNm1.match("([\\/]+$)|^([\\.]+$)|^([-]+$)|^([#]+$)|^([,]+$)|^([:]+$)|^([']+$)|^([&]+$)|^([;]+$)|^([0-9A-Za-z-#;:,'&()\\s+\\.\\/]+$)|^[(]+$|^[)]+$")) {
		  return new ValidationResult({
		  id : 'custNm1',
		  type : 'text',
		  name : 'custNm1'
		  }, false, 'Only the characters listed are allowed to input due to system limitation.' + 
	      '   (1) A to Z (uppercase & lowercase)  ' + ' (2) 0 to 9   ' + " (3) '   " + ' (4) ,   ' + ' (5) :   ' + ' (6) &   ' + '  (7) (   ' + ' (8) ;   ' + ' (9) )   ' + ' (10) -   ' + ' (11)  #   ' + ' (12) .  '+' (13) /  ');
		  }
	  
	  if (custNm2 && custNm2.length > 0 && !custNm2.match("([\\/]+$)|^([\\.]+$)|^([-]+$)|^([#]+$)|^([,]+$)|^([:]+$)|^([']+$)|^([&]+$)|^([;]+$)|^([0-9A-Za-z-#;:,'&()\\s+\\.\\/]+$)|^[(]+$|^[)]+$")) {
		  return new ValidationResult({
		  id : 'custNm2',
		  type : 'text',
		  name : 'custNm2'
		  }, false, 'Only the characters listed are allowed to input due to system limitation.' + 
	      '   (1) A to Z (uppercase & lowercase)  ' + ' (2) 0 to 9   ' + " (3) '   " + ' (4) ,   ' + ' (5) :   ' + ' (6) &   ' + '  (7) (   ' + ' (8) ;   ' + ' (9) )   ' + ' (10) -   ' + ' (11)  #   ' + ' (12) .  '+' (13) /  ');
		  }
	 
	  if (addrTxt && addrTxt.length > 0 && !addrTxt.match("([\\/]+$)|^([\\.]+$)|^([-]+$)|^([#]+$)|^([,]+$)|^([:]+$)|^([']+$)|^([&]+$)|^([;]+$)|^([0-9A-Za-z-#;:,'&()\\s+\\.\\/]+$)|^[(]+$|^[)]+$")) {
		  return new ValidationResult({
		  id : 'addrTxt',
		  type : 'text',
		  name : 'addrTxt'
		  }, false, 'Only the characters listed are allowed to input due to system limitation.' + 
	      '   (1) A to Z (uppercase & lowercase)  ' + ' (2) 0 to 9   ' + " (3) '   " + ' (4) ,   ' + ' (5) :   ' + ' (6) &   ' + '  (7) (   ' + ' (8) ;   ' + ' (9) )   ' + ' (10) -   ' + ' (11)  #   ' + ' (12) .  '+' (13) /  ');
		  }
	  if (addrTxt2 && addrTxt2.length > 0 && !addrTxt2.match("([\\/]+$)|^([\\.]+$)|^([-]+$)|^([#]+$)|^([,]+$)|^([:]+$)|^([']+$)|^([&]+$)|^([;]+$)|^([0-9A-Za-z-#;:,'&()\\s+\\.\\/]+$)|^[(]+$|^[)]+$")) {
		  return new ValidationResult({
		  id : 'addrTxt2',
		  type : 'text',
		  name : 'addrTxt2'
		  }, false,  'Only the characters listed are allowed to input due to system limitation.' + 
	      '   (1) A to Z (uppercase & lowercase)  ' + ' (2) 0 to 9   ' + " (3) '   " + ' (4) ,   ' + ' (5) :   ' + ' (6) &   ' + '  (7) (   ' + ' (8) ;   ' + ' (9) )   ' + ' (10) -   ' + ' (11)  #   ' + ' (12) .  '+' (13) /  ');
		  }
	  if (stateProv && stateProv.length > 0 && !stateProv.match("([\\/]+$)|^([\\.]+$)|^([-]+$)|^([#]+$)|^([,]+$)|^([:]+$)|^([']+$)|^([&]+$)|^([;]+$)|^([0-9A-Za-z-#;:,'&()\\s+\\.\\/]+$)|^[(]+$|^[)]+$")) {
		  return new ValidationResult({
		  id : 'stateProv',
		  type : 'text',
		  name : 'stateProv'
		  }, false, 'Only the characters listed are allowed to input due to system limitation.' + 
	      '   (1) A to Z (uppercase & lowercase)  ' + ' (2) 0 to 9   ' + " (3) '   " + ' (4) ,   ' + ' (5) :   ' + ' (6) &   ' + '  (7) (   ' + ' (8) ;   ' + ' (9) )   ' + ' (10) -   ' + ' (11)  #   ' + ' (12) .  '+' (13) /  ');
		  }
	  if (dept && dept.length > 0 && !dept.match("([\\/]+$)|^([\\.]+$)|^([-]+$)|^([#]+$)|^([,]+$)|^([:]+$)|^([']+$)|^([&]+$)|^([;]+$)|^([0-9A-Za-z-#;:,'&()\\s+\\.\\/]+$)|^[(]+$|^[)]+$")) {
		  return new ValidationResult({
		  id : 'dept',
		  type : 'text',
		  name : 'dept'
		  }, false, 'Only the characters listed are allowed to input due to system limitation.' + 
	      '   (1) A to Z (uppercase & lowercase)  ' + ' (2) 0 to 9   ' + " (3) '   " + ' (4) ,   ' + ' (5) :   ' + ' (6) &   ' + '  (7) (   ' + ' (8) ;   ' + ' (9) )   ' + ' (10) -   ' + ' (11)  #   ' + ' (12) .  '+' (13) /  ');
		  }
	  
	  return new ValidationResult(null, true);
	  }
	  };
	  })(), null, 'frmCMR_addressModal');
	  } 


// Defect : 1815803

function addFieldFormatValidator() {
	console.log("addFieldFormatValidator..............");
		FormManager.addFormValidator((function() {
			return {
				validate : function() {

					if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
						var recordList = null;
						var addrType = null;
						var updateIndicator = null;
						var addrSequence = null;


						for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
							recordList = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
							if (recordList == null && _allAddressData != null && _allAddressData[i] != null) {
								recordList = _allAddressData[i];
							}
							addrType = recordList.addrType;
							addrSequence = recordList.addrSeq;
							addrDept = recordList.dept;
							addrCustNm1 = recordList.custNm1;
							addrCustNm2 = recordList.custNm2;
							addrAddrTxt = recordList.addrTxt;
							addrAddrTxt2 = recordList.addrTxt2;
							addrCity1 = recordList.city1;

							/*
               * if (typeof (addrSequence) == 'object') { addrSequence =
               * addrSequence[0]; }
               */
							if (typeof (addrDept) == 'object') {
								addrDept = addrDept[0];
							}
							if (typeof (addrCustNm1) == 'object') {
								addrCustNm1 = addrCustNm1[0];
							}
							if (typeof (addrCustNm2) == 'object') {
								addrCustNm2 = addrCustNm2[0];
							}
							if (typeof (addrAddrTxt) == 'object') {
								addrAddrTxt = addrAddrTxt[0];
							}
							if (typeof (addrAddrTxt2) == 'object') {
								addrAddrTxt2 = addrAddrTxt2[0];
							}
							if (typeof (addrCity1) == 'object') {
								addrCity1 = addrCity1[0];
							}


							if ((addrDept != null && !addrDept.match("([\\/]+$)|^([\\.]+$)|^([-]+$)|^([#]+$)|^([,]+$)|^([:]+$)|^([']+$)|^([&]+$)|^([;]+$)|^([0-9A-Za-z-#;:,'&()\\s+\\.\\/]+$)|^[(]+$|^[)]+$"))
							|| (addrCustNm1 != null && !addrCustNm1.match("([\\/]+$)|^([\\.]+$)|^([-]+$)|^([#]+$)|^([,]+$)|^([:]+$)|^([']+$)|^([&]+$)|^([;]+$)|^([0-9A-Za-z-#;:,'&()\\s+\\.\\/]+$)|^[(]+$|^[)]+$"))
							||  (addrCustNm2 != null && !addrCustNm2.match("([\\/]+$)|^([\\.]+$)|^([-]+$)|^([#]+$)|^([,]+$)|^([:]+$)|^([']+$)|^([&]+$)|^([;]+$)|^([0-9A-Za-z-#;:,'&()\\s+\\.\\/]+$)|^[(]+$|^[)]+$"))
							||  (addrAddrTxt != null && !addrAddrTxt.match("([\\/]+$)|^([\\.]+$)|^([-]+$)|^([#]+$)|^([,]+$)|^([:]+$)|^([']+$)|^([&]+$)|^([;]+$)|^([0-9A-Za-z-#;:,'&()\\s+\\.\\/]+$)|^[(]+$|^[)]+$"))
							||  (addrAddrTxt2 != null && !addrAddrTxt2.match("([\\/]+$)|^([\\.]+$)|^([-]+$)|^([#]+$)|^([,]+$)|^([:]+$)|^([']+$)|^([&]+$)|^([;]+$)|^([0-9A-Za-z-#;:,'&()\\s+\\.\\/]+$)|^[(]+$|^[)]+$"))
							||  (addrCity1 != null && !addrCity1.match("([\\/]+$)|^([\\.]+$)|^([-]+$)|^([#]+$)|^([,]+$)|^([:]+$)|^([']+$)|^([&]+$)|^([;]+$)|^([0-9A-Za-z-#;:,'&()\\s+\\.\\/]+$)|^[(]+$|^[)]+$"))) {
								return new ValidationResult(null, false, 'Only the characters listed are allowed to input due to system limitation.' + 
								' (1) A to Z (uppercase & lowercase) ' + ' (2) 0 to 9 ' + " (3) ' " + ' (4) , ' + ' (5) : ' + ' (6) & ' + ' (7) ( ' + ' (8) ; ' + ' (9) ) ' + ' (10) - ' + ' (11) # ' + ' (12) .  ' + ' (13) / ');
							}

							return new ValidationResult(null, true);


						}
					}				

					return new ValidationResult(null, true);
			}
		};
	})(), 'MAIN_NAME_TAB', 'frmCMR');
} 


function addAttachmentValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqType = FormManager.getActualValue('reqType');
        var custSubType = FormManager.getActualValue('custSubGrp');
        var cntryUse = FormManager.getActualValue('cntryUse');
        // var docContent = FormManager.getActualValue('docContent');
        if (typeof (_pagemodel) != 'undefined') {
          if (reqType == 'C' && (custSubType != 'INTER' && custSubType != 'XINT' && custSubType != 'DUMMY' && custSubType != 'XDUMM' && custSubType != 'BLUMX' && custSubType != 'XBLUM' && custSubType != 'MKTPC' && custSubType != 'XMKTP' && custSubType != 'IGF' && custSubType != 'XIGF')) {
            var id = FormManager.getActualValue('reqId');
            var ret = cmr.query('CHECK_TERRITORY_ATTACHMENT', {
              ID : id
            });

            if (ret == null || ret.ret1 == null) {
              return new ValidationResult(null, false, 'TERRITORY Manager Approval in Attachment tab is required.');
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

  
function defaultCMRNumberPrefix() {
  
  if (FormManager.getActualValue('reqType') == 'U') {
    return
  }
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  
  if((role == 'PROCESSOR') && (custSubGrp == 'INTER' || custSubGrp == 'XINT')){
    FormManager.addValidator('cmrNoPrefix', Validators.REQUIRED, [ 'CmrNoPrefix' ], 'MAIN_IBM_TAB');
  }
  if((role != 'PROCESSOR') && (custSubGrp == 'INTER' || custSubGrp == 'XINT')){
    FormManager.hide('CmrNoPrefix', 'cmrNoPrefix');
  } else {
    FormManager.show('CmrNoPrefix', 'cmrNoPrefix');
  }
  
  if (role == 'PROCESSOR' && (custSubGrp == 'INTER' || custSubGrp == 'XINT')){
    FormManager.setValue('cmrNoPrefix', '994---');
  }
  
  if(cmrIssuingCntry == '736' && role == 'REQUESTER' && (custSubGrp == 'INT' || custSubGrp == 'XINT')){
    FormManager.resetValidations('cmrNoPrefix');
  }
}

function setISBUforBPscenario(){
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  
  if (custSubGrp == 'BUSPR' || custSubGrp == 'XBUSP'){
    FormManager.addValidator('isbuCd', Validators.REQUIRED, [ 'ISBU' ], 'MAIN_IBM_TAB');
  }
  
  if(role != 'VIEWER' && (custSubGrp == 'BUSPR' || custSubGrp == 'XBUSP')){
    FormManager.enable('isbuCd');
  } else if (role == 'REQUESTER' || role == 'VIEWER'){
    FormManager.readOnly('isbuCd');
  } else if (role == 'PROCESSOR') {
    FormManager.enable('isbuCd');
  }
}

function onCustSubGrpChange() {
  
  if (FormManager.getActualValue('reqType') == 'U') {
    return
  }
  
  dojo.connect(FormManager.getField('custSubGrp'), 'onChange', function(value) {
    FormManager.readOnly('subIndustryCd');
   if (FormManager.getActualValue('viewOnlyPage') != 'true')
    FormManager.enable('isicCd');
    setISBUScenarioLogic();

    var custSubGrp = FormManager.getActualValue('custSubGrp');
    var custSubGrpInDB = _pagemodel.custSubGrp;
    if (custSubGrpInDB != null && custSubGrp == custSubGrpInDB) {
      FormManager.setValue('abbrevNm', _pagemodel.abbrevNm);
      FormManager.setValue('abbrevLocn', _pagemodel.abbrevLocn);
      FormManager.setValue('isbuCd', _pagemodel.isbuCd);
      return;
    }
    setISBUScenarioLogic();
    autoSetAbbrevNmLocnLogic();
    setCollectionCd();
  });
}

function setCollectionCd(){
  var cmrIssuCntry = FormManager.getActualValue('cmrIssuingCntry');
  var custGrp = FormManager.getActualValue('custGrp');
  var role = FormManager.getActualValue('userRole').toUpperCase();

  if(cmrIssuCntry == '834' && custGrp == 'CROSS'){
    FormManager.setValue('collectionCd','S013');
    if(role == 'REQUESTER')
     FormManager.readOnly('collectionCd');
    return;
  }  
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  } 
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  
  var isbuCd = FormManager.getActualValue('isbuCd');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var collCd = null;
  if (isbuCd != '') {
    var qParams = {
      _qall : 'Y',
      ISSUING_CNTRY : cntry,
      ISBU : '%' + isbuCd + '%'
    };
  var result = cmr.query('GET.ARCODELIST.BYISBU', qParams);
  if( result.length > 0){
    if (result != null && result[0].ret1 != result[0].ret2) {   
        collCd = result[0].ret1;
      if (collCd != null) {
          FormManager.setValue('collectionCd', collCd);
      }
    }
    }
  }
}

function addSalesRepNameNoCntryValidator() {
  console.log(">>>repTeamMemberNo<<<==="+FormManager.getActualValue('repTeamMemberNo'));
  console.log(">>>repTeamMemberNo---pID<<<==="+localStorage.getItem("pID"));
  
  if (localStorage.getItem("pID") != null && (FormManager.getActualValue('repTeamMemberNo'))) {
    FormManager.addFormValidator((function() {
      return {
        validate : function() {
          console.log(">>>> addSalesRepNameCntryValidator >>>>");
          var cmrIssuCntry = FormManager.getActualValue('cmrIssuingCntry');
          var lbl1 = FormManager.getLabel('SalRepNameNo');
          var temp = localStorage.getItem("pID").substring(6);
          var countryCd = (temp == 'PH1') ? '818' : temp;
          // if (cmrIssuCntry != countryCd && !(cmrIssuCntry == '615'
          // ||cmrIssuCntry ==
          // '652' ||cmrIssuCntry == '744' ||cmrIssuCntry == '790'
          // ||cmrIssuCntry == '736'
          // || cmrIssuCntry == '738' || cmrIssuCntry == '790')) {
            if (cmrIssuCntry != countryCd && !(included(cmrIssuCntry))) {
            return new ValidationResult({
              id : 'repTeamMemberNo',
              type : 'text',
              name : 'repTeamMemberNo'
            }, false, 'The value of ' + lbl1 + ' is invalid. CMR Issuing Country(' + cmrIssuCntry + ') and Sales Rep Country(' + countryCd + ') should be same.');
          }
          return new ValidationResult(null, true);
        }
      };
    })(), 'MAIN_IBM_TAB', 'frmCMR');
  } else {

  }
}

function included(cmrIssuCntry){
  var excludedCntry = ["615","652","744","790","736","738","643","749","778","818","834","852","856","646","714","720","616","796"];
  
  for(var i=0;i<excludedCntry.length;i++){
    console.log("---excluded Country is---"+excludedCntry[i]);
    console.log("---cmrIssuCntry Country is---"+cmrIssuCntry);
    if(cmrIssuCntry == excludedCntry[i])
      return false;
    else 
      return true;
  };
}


// Different requirement for countries that don't exist on Bluepages:
// Myanmar,Cambodia,Loas,Brunei
function addSalesRepNameNoCntryValidatorBluepages() {
  console.log(">>>repTeamMemberNo<<<==="+FormManager.getActualValue('repTeamMemberNo'));
    console.log(">>>repTeamMemberNo---pID<<<==="+localStorage.getItem("pID"));
  if (localStorage.getItem("pID") != null && (FormManager.getActualValue('repTeamMemberNo'))) {
    FormManager.addFormValidator((function() {
      return {
        validate : function() {
          console.log(">>>> addSalesRepNameCntryValidatorBluepages >>>>");
          var cmrIssuCntry = FormManager.getActualValue('cmrIssuingCntry');
          var lbl1 = FormManager.getLabel('SalRepNameNo');
          var countryCd = localStorage.getItem("pID").substring(6);
          var result = "";
          var temp_display = "";
          if(cmrIssuCntry == '643' && FormManager.getActualValue('custSubGrp') == 'AQSTN'){
            FormManager.setValue('repTeamMemberNo','000000');
            return true;
          }
          switch (cmrIssuCntry) {
          case '643':
            if (countryCd == '778' || FormManager.getActualValue('repTeamMemberNo') == "0DUMMY") {
              result = "true";
            } else {
              temp_display = 'Malaysia-778';
            }
            break;

          case '646':
            if (countryCd == '856') {
              result = "true";
            } else {
              temp_display = 'Thailand-856';
            }
            break;

          case '714':
            if (countryCd == '856') {
              result = "true";
            } else {
              temp_display = 'Thailand-856';
            }
            break;

          case '720':
            if (countryCd == '852') {
              result = "true";
            } else {
              temp_display = 'Vietnam-852';
            }
            break;

          default:

          }
          if (result != "true") {
            return new ValidationResult({
              id : 'repTeamMemberNo',
              type : 'text',
              name : 'repTeamMemberNo'
            }, false, 'The value of ' + lbl1 + ' is invalid. Sales Rep. Country should be (' + temp_display + ') for the CMR Issuing Country (' + cmrIssuCntry + ')');
          }
          return new ValidationResult(null, true);
        }
      };
    })(), 'MAIN_IBM_TAB', 'frmCMR');
  } else {
    
    if(FormManager.getActualValue('cmrIssuingCntry') == '643' && (FormManager.getActualValue('custSubGrp') == 'AQSTN' || FormManager.getActualValue('custSubGrp') == 'XAQST')){
      FormManager.setValue('repTeamMemberNo','000000');
    }
  }
}

function setAbbrevNmLocnOnAddressSave(cntry, addressMode, saving, finalSave, force) {
  console.log(">>>> setAbbrevNmLocnOnAddressSave >>>>");
  var reqType = null;
  var cmrCntry = FormManager.getActualValue('cmrIssuingCntry');
  if (typeof (_pagemodel) != 'undefined') {
    reqType = FormManager.getActualValue('reqType');
  }
// if (reqType == 'U') {
// return;
// }
  if ((finalSave || force)  && cmr.addressMode){
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
    if (addrType == 'ZS01' || copyingToA) {
      autoSetAbbrevNmLocnLogic();
    // setting collection code for AU(616)
      if(cmrCntry == '616')
      { console.log(">>>> set CollectionCode OnAddressSave >>>>");
      setCollCdFrAU();
      }
    }
  }
}

function autoSetAbbrevNmLocnLogic() {
  console.log("autoSetAbbrevNmLocnLogic");
  var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  var _abbrevNm = null;
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var _abbrevLocn = null;
  var cntryDesc = null;

  var zs01ReqId = FormManager.getActualValue('reqId');
  var qParams = {
    REQ_ID : zs01ReqId,
  };
  var result = cmr.query('ADDR.GET.CUSTNM1.BY_REQID', qParams);
  var custNm1 = FormManager.getActualValue('custNm1');
  if(custNm1 == '')
    custNm1 = result.ret1;
  _abbrevNm = custNm1;

  switch (cmrIssuingCntry) {
  case SysLoc.INDIA:
    if (custSubGrp != null && custSubGrp.length > 0) {
      if (custSubGrp == "IGF" || custSubGrp == "XIGF") {
        _abbrevNm = "IGF";
      } else if (custSubGrp == "INTER") {
        _abbrevNm = "IBM INDIA PVT LTD";
      } else if (custSubGrp == "BLUMX" || custSubGrp == "XBLUM") {
        _abbrevNm = "BLUEMIX";
      } else if (custSubGrp == "MKTPC" || custSubGrp == "XMKTP") {
        _abbrevNm = "MARKETPLACE";
      } else if (custSubGrp == "SOFT" || custSubGrp == "XSOFT") {
        _abbrevNm = "SOFTLAYER USE ONLY";
      } else if (custSubGrp == "ESOSW" || custSubGrp == "XESO") {
        _abbrevNm = "ESA/OEM/SWG_" + custNm1;
      } else {
        if(custNm1)
        _abbrevNm = custNm1;
      }
    }

    if (_abbrevNm && _abbrevNm.length > 21) {
      _abbrevNm = _abbrevNm.substring(0, 21);
    }
    FormManager.setValue('abbrevNm', _abbrevNm);

    if (FormManager.getActualValue('landCntry') == "IN") {
      _abbrevLocn = "India";
    }
    if (FormManager.getActualValue('landCntry') != "IN") {
      cntryDesc = getLandCntryDesc(FormManager.getActualValue('landCntry'));
      _abbrevLocn = cntryDesc;
    }

    if (_abbrevLocn && _abbrevLocn.length > 9) {
      _abbrevLocn = _abbrevLocn.substring(0, 9);
    }
    FormManager.setValue('abbrevLocn', _abbrevLocn);

    break;
  case SysLoc.SRI_LANKA:
    if (custSubGrp != null && custSubGrp.length > 0) {
      if (custSubGrp == "INTER" || custSubGrp == "XINT") {
        _abbrevNm = "IBM INDIA PVT LTD";
      } else if (custSubGrp == "BLUMX" || custSubGrp == "XBLUM") {
        _abbrevNm = "BLUEMIX";
      } else if (custSubGrp == "MKTPC" || custSubGrp == "XMKTP") {
        _abbrevNm = "MARKETPLACE";
      } else if (custSubGrp == "SOFT" || custSubGrp == "XSOFT") {
        _abbrevNm = "SOFTLAYER USE ONLY";
      } else {
        if(custNm1)
        _abbrevNm = custNm1;
      }
    }

    if (_abbrevNm && _abbrevNm.length > 21) {
      _abbrevNm = _abbrevNm.substring(0, 21);
    }
    FormManager.setValue('abbrevNm', _abbrevNm);

    if (FormManager.getActualValue('landCntry') == "LK") {
      _abbrevLocn = "Sri Lanka";
    }
    if (FormManager.getActualValue('landCntry') != "LK") {
      var cntryDesc = getLandCntryDesc(FormManager.getActualValue('landCntry'));
      _abbrevLocn = cntryDesc;
    }

    if (_abbrevLocn && _abbrevLocn.length > 9) {
      _abbrevLocn = _abbrevLocn.substring(0, 9);
    }
    FormManager.setValue('abbrevLocn', _abbrevLocn);

    break;
  case SysLoc.BANGLADESH:
    if (custSubGrp != null && custSubGrp.length > 0) {
      if (custSubGrp == "INTER" || custSubGrp == "XINT") {
        _abbrevNm = "IBM INDIA PVT LTD";
      } else if (custSubGrp == "BLUMX" || custSubGrp == "XBLUM") {
        _abbrevNm = "BLUEMIX";
      } else if (custSubGrp == "MKTPC" || custSubGrp == "XMKTP") {
        _abbrevNm = "MARKETPLACE";
      } else if (custSubGrp == "SOFT" || custSubGrp == "XSOFT") {
        _abbrevNm = "SOFTLAYER USE ONLY";
      } else {
        if(custNm1)
        _abbrevNm = custNm1;
      }
    }

    if (_abbrevNm && _abbrevNm.length > 21) {
      _abbrevNm = _abbrevNm.substring(0, 21);
    }
    FormManager.setValue('abbrevNm', _abbrevNm);

    if (FormManager.getActualValue('landCntry') == "BD") {
      _abbrevLocn = "Banglades";
    }
    if (FormManager.getActualValue('landCntry') != "BD") {
      var cntryDesc = getLandCntryDesc(FormManager.getActualValue('landCntry'));
      _abbrevLocn = cntryDesc;
    }

    if (_abbrevLocn && _abbrevLocn.length > 9) {
      _abbrevLocn = _abbrevLocn.substring(0, 9);
    }
    FormManager.setValue('abbrevLocn', _abbrevLocn);

    break;
  case SysLoc.NEPAL:
    _abbrevNm = custNm1;

    if (_abbrevNm && _abbrevNm.length > 21) {
      _abbrevNm = _abbrevNm.substring(0, 21);
    }
    FormManager.setValue('abbrevNm', _abbrevNm);

    if (FormManager.getActualValue('landCntry') == "NP") {
      _abbrevLocn = "Nepal";
    }
    if (FormManager.getActualValue('landCntry') != "NP") {
      var cntryDesc = getLandCntryDesc(FormManager.getActualValue('landCntry'));
      _abbrevLocn = cntryDesc;
    }

    if (_abbrevLocn && _abbrevLocn.length > 9) {
      _abbrevLocn = _abbrevLocn.substring(0, 9);
    }
    FormManager.setValue('abbrevLocn', _abbrevLocn);

    break;
  case SysLoc.SINGAPORE:
    if (custSubGrp != null && custSubGrp.length > 0 && custNm1) {
      if (custSubGrp == "DUMMY") {
        _abbrevNm = "DUMMY_" + custNm1;
      } else if (custSubGrp == "BLUMX" || custSubGrp == "XBLUM") {
        _abbrevNm = "BLUEMIX_" + custNm1;
      } else if (custSubGrp == "MKTPC" || custSubGrp == "XMKTP") {
        _abbrevNm = "MARKET PLACE_" + custNm1;
      } else if (custSubGrp == "ASLOM" || custSubGrp == "XASLM") {
        _abbrevNm = "ESA_" + custNm1;
      } else if (custSubGrp == "SOFT" || custSubGrp == "XSOFT") {
        _abbrevNm = "Softlayer_" + custNm1;
      } else {
        _abbrevNm = custNm1;
      }
    }

    if (_abbrevNm && _abbrevNm.length > 21) {
      _abbrevNm = _abbrevNm.substring(0, 21);
    }
    FormManager.setValue('abbrevNm', _abbrevNm);

    if (FormManager.getActualValue('landCntry') == "SG") {
      _abbrevLocn = "Singapore";
    }
    if (FormManager.getActualValue('landCntry') != "SG") {
      var cntryDesc = getLandCntryDesc(FormManager.getActualValue('landCntry'));
      _abbrevLocn = cntryDesc;
    }

    if (_abbrevLocn && _abbrevLocn.length > 9) {
      _abbrevLocn = _abbrevLocn.substring(0, 9);
    }
    FormManager.setValue('abbrevLocn', _abbrevLocn);

    break;
  case SysLoc.MALASIA:
    if (custSubGrp != null && custSubGrp.length > 0 && custNm1) {
      if (custSubGrp == "DUMMY"  || custSubGrp == "XDUMM") {
        _abbrevNm = "DUMMY_" + custNm1;
      } else if (custSubGrp == "ASLOM"  || custSubGrp == "XASLM") {
        _abbrevNm = "ESA_" + custNm1;
      }else if (custSubGrp == "SOFT" || custSubGrp == "XSOFT") {
        _abbrevNm = "Softlayer_" + custNm1;
      }  else {
        if(custNm1)
        _abbrevNm = custNm1;
      }
    }

    if (_abbrevNm && _abbrevNm.length > 21) {
      _abbrevNm = _abbrevNm.substring(0, 21);
    }
    FormManager.setValue('abbrevNm', _abbrevNm);

    if (FormManager.getActualValue('landCntry') == "MY") {
      if (FormManager.getActualValue('city1') != 'undefined') {
        _abbrevLocn = FormManager.getActualValue('city1');
      }
    }
    if (FormManager.getActualValue('landCntry') != "MY") {
      var cntryDesc = getLandCntryDesc(FormManager.getActualValue('landCntry'));
      _abbrevLocn = cntryDesc;
    }

    if (_abbrevLocn && _abbrevLocn.length > 9) {
      _abbrevLocn = _abbrevLocn.substring(0, 9);
    }
    FormManager.setValue('abbrevLocn', _abbrevLocn);

    break;
  case SysLoc.BRUNEI:
    if (custSubGrp != null && custSubGrp.length > 0 && custNm1) {
      if (custSubGrp == "DUMMY"  || custSubGrp == "XDUMM") {
        _abbrevNm = "DUMMY_" + custNm1;
      } else if (custSubGrp == "ASLOM"  || custSubGrp == "XASLM") {
        _abbrevNm = "ESA_" + custNm1;
      } else if (custSubGrp == "SOFT" || custSubGrp == "XSOFT") {
        _abbrevNm = "Softlayer_" + custNm1;
      } else {
        if(custNm1)
        _abbrevNm = custNm1;
      }
    }

    if (_abbrevNm && _abbrevNm.length > 21) {
      _abbrevNm = _abbrevNm.substring(0, 21);
    }
    FormManager.setValue('abbrevNm', _abbrevNm);

    if (FormManager.getActualValue('landCntry') == "BN") {
      _abbrevLocn = "Brunei";
    }
    if (FormManager.getActualValue('landCntry') != "BN") {
      var cntryDesc = getLandCntryDesc(FormManager.getActualValue('landCntry'));
      _abbrevLocn = cntryDesc;
    }

    if (_abbrevLocn && _abbrevLocn.length > 9) {
      _abbrevLocn = _abbrevLocn.substring(0, 9);
    }
    FormManager.setValue('abbrevLocn', _abbrevLocn);

    break;
  case SysLoc.VIETNAM:
    if (custSubGrp != null && custSubGrp.length > 0 && custNm1) {
      if (custSubGrp == "DUMMY"  || custSubGrp == "XDUMM") {
        _abbrevNm = "DUMMY_" + custNm1;
      } else if (custSubGrp == "ASLOM"  || custSubGrp == "XASLM") {
        _abbrevNm = "ESA_" + custNm1;
      } else if (custSubGrp == "SOFT" || custSubGrp == "XSOFT") {
        _abbrevNm = "Softlayer_" + custNm1;
      } else {
        if(custNm1)
        _abbrevNm = custNm1;
      }
    }

    if (_abbrevNm && _abbrevNm.length > 21) {
      _abbrevNm = _abbrevNm.substring(0, 21);
    }
    FormManager.setValue('abbrevNm', _abbrevNm);

    if (FormManager.getActualValue('landCntry') == "VN") {
      if (FormManager.getActualValue('city1') != 'undefined') {
        _abbrevLocn = FormManager.getActualValue('city1');
      }
    }
    if (FormManager.getActualValue('landCntry') != "VN") {
      var cntryDesc = getLandCntryDesc(FormManager.getActualValue('landCntry'));
      _abbrevLocn = cntryDesc;
    }

    if (_abbrevLocn && _abbrevLocn.length > 9) {
      _abbrevLocn = _abbrevLocn.substring(0, 9);
    }
    FormManager.setValue('abbrevLocn', _abbrevLocn);

    break;
  case SysLoc.INDONESIA:
    if (custSubGrp != null && custSubGrp.length > 0 && custNm1) {
      if (custSubGrp == "DUMMY"  || custSubGrp == "XDUMM") {
        _abbrevNm = "DUMMY_" + custNm1;
      } else if (custSubGrp == "ASLOM"  || custSubGrp == "XASLM") {
        _abbrevNm = "ESA_" + custNm1;
      }else if (custSubGrp == "SOFT" || custSubGrp == "XSOFT") {
        _abbrevNm = "Softlayer_" + custNm1;
      }  else {
        _abbrevNm = custNm1;
      }
    }

    if (_abbrevNm && _abbrevNm.length > 21) {
      _abbrevNm = _abbrevNm.substring(0, 21);
    }
    FormManager.setValue('abbrevNm', _abbrevNm);

    if (FormManager.getActualValue('landCntry') == "ID") {
      if (FormManager.getActualValue('city1') != 'undefined') {
        _abbrevLocn = FormManager.getActualValue('city1');
      }
    }
    if (FormManager.getActualValue('landCntry') != "ID") {
      var cntryDesc = getLandCntryDesc(FormManager.getActualValue('landCntry'));
      _abbrevLocn = cntryDesc;
    }

    if (_abbrevLocn && _abbrevLocn.length > 9) {
      _abbrevLocn = _abbrevLocn.substring(0, 9);
    }
    FormManager.setValue('abbrevLocn', _abbrevLocn);

    break;
  case SysLoc.PHILIPPINES:
    if (custSubGrp != null && custSubGrp.length > 0 && custNm1) {
      if (custSubGrp == "DUMMY" || custSubGrp == "XDUMM") {
        _abbrevNm = "DUMMY_" + custNm1;
      } else if (custSubGrp == "ASLOM" || custSubGrp == "XASLM") {
        _abbrevNm = "ESA_" + custNm1;
      } else if (custSubGrp == "SOFT" || custSubGrp == "XSOFT") {
        _abbrevNm = "Softlayer_" + custNm1;
      } else {
        _abbrevNm = custNm1;
      }
    }

    if (_abbrevNm && _abbrevNm.length > 21) {
      _abbrevNm = _abbrevNm.substring(0, 21);
    }
    FormManager.setValue('abbrevNm', _abbrevNm);

    if (FormManager.getActualValue('landCntry') == "PH") {
      if (FormManager.getActualValue('city1') != 'undefined') {
        _abbrevLocn = FormManager.getActualValue('city1');
      }
    }
    if (FormManager.getActualValue('landCntry') != "PH") {
      var cntryDesc = getLandCntryDesc(FormManager.getActualValue('landCntry'));
      _abbrevLocn = cntryDesc;
    }

    if (_abbrevLocn && _abbrevLocn.length > 9) {
      _abbrevLocn = _abbrevLocn.substring(0, 9);
    }
    FormManager.setValue('abbrevLocn', _abbrevLocn);

    break;
  case SysLoc.THAILAND:
    if (custSubGrp != null && custSubGrp.length > 0 && custNm1) {
      if (custSubGrp == "DUMMY"  || custSubGrp == "XDUMM") {
        _abbrevNm = "DUMMY_" + custNm1;
      } else if (custSubGrp == "ASLOM" || custSubGrp == "XASLM") {
        _abbrevNm = "ESA_" + custNm1;
      } else if (custSubGrp == "SOFT" || custSubGrp == "XSOFT") {
        _abbrevNm = "Softlayer_" + custNm1;
      } else {
        _abbrevNm = custNm1;
      }
    }

    if (_abbrevNm && _abbrevNm.length > 21) {
      _abbrevNm = _abbrevNm.substring(0, 21);
    }
    FormManager.setValue('abbrevNm', _abbrevNm);

    if (FormManager.getActualValue('landCntry') == "TH") {
      if (FormManager.getActualValue('city1') != 'undefined') {
        _abbrevLocn = FormManager.getActualValue('city1');
      }
    }
    if (FormManager.getActualValue('landCntry') != "TH") {
      var cntryDesc = getLandCntryDesc(FormManager.getActualValue('landCntry'));
      _abbrevLocn = cntryDesc;
    }

    if (_abbrevLocn && _abbrevLocn.length > 9) {
      _abbrevLocn = _abbrevLocn.substring(0, 9);
    }
    FormManager.setValue('abbrevLocn', _abbrevLocn);

    break;
  case SysLoc.MYANMAR:
    if (custSubGrp != null && custSubGrp.length > 0 && custNm1) {
      if (custSubGrp == "DUMMY" || custSubGrp == "XDUMM") {
        _abbrevNm = "DUMMY_" + custNm1;
      } else if (custSubGrp == "ASLOM" || custSubGrp == "XASLM") {
        _abbrevNm = "ESA_" + custNm1;
      } else if (custSubGrp == "SOFT" || custSubGrp == "XSOFT") {
        _abbrevNm = "Softlayer_" + custNm1;
      } else {
        _abbrevNm = custNm1;
      }
    }

    if (_abbrevNm && _abbrevNm.length > 21) {
      _abbrevNm = _abbrevNm.substring(0, 21);
    }
    FormManager.setValue('abbrevNm', _abbrevNm);

    if (FormManager.getActualValue('landCntry') == "MM") {
      _abbrevLocn = "Myanmar";
    }
    if (FormManager.getActualValue('landCntry') != "MM") {
      var cntryDesc = getLandCntryDesc(FormManager.getActualValue('landCntry'));
      _abbrevLocn = cntryDesc;
    }

    if (_abbrevLocn && _abbrevLocn.length > 9) {
      _abbrevLocn = _abbrevLocn.substring(0, 9);
    }
    FormManager.setValue('abbrevLocn', _abbrevLocn);

    break;
  case SysLoc.LAOS:
    if (custSubGrp != null && custSubGrp.length > 0 && custNm1) {
      if (custSubGrp == "DUMMY" || custSubGrp == "XDUMM") {
        _abbrevNm = "DUMMY_" + custNm1;
      } else if (custSubGrp == "ASLOM" || custSubGrp == "XASLM") {
        _abbrevNm = "ESA_" + custNm1;
      } else if (custSubGrp == "SOFT" || custSubGrp == "XSOFT") {
        _abbrevNm = "Softlayer_" + custNm1;
      } else {
        _abbrevNm = custNm1;
      }
    }

    if (_abbrevNm && _abbrevNm.length > 21) {
      _abbrevNm = _abbrevNm.substring(0, 21);
    }
    FormManager.setValue('abbrevNm', _abbrevNm);

    if (FormManager.getActualValue('landCntry') == "LA") {
      _abbrevLocn = "Laos";
    }
    if (FormManager.getActualValue('landCntry') != "LA") {
      var cntryDesc = getLandCntryDesc(FormManager.getActualValue('landCntry'));
      _abbrevLocn = cntryDesc;
    }

    if (_abbrevLocn && _abbrevLocn.length > 9) {
      _abbrevLocn = _abbrevLocn.substring(0, 9);
    }
    FormManager.setValue('abbrevLocn', _abbrevLocn);

    break;
  case SysLoc.CAMBODIA:
    if (custSubGrp != null && custSubGrp.length > 0 && custNm1) {
      if (custSubGrp == "DUMMY"  || custSubGrp == "XDUMM") {
        _abbrevNm = "DUMMY_" + custNm1;
      } else if (custSubGrp == "ASLOM" || custSubGrp == "XASLM") {
        _abbrevNm = "ESA_" + custNm1;
      } else if (custSubGrp == "SOFT" || custSubGrp == "XSOFT") {
        _abbrevNm = "Softlayer_" + custNm1;
      } else {
        _abbrevNm = custNm1;
      }
    }

    if (_abbrevNm && _abbrevNm.length > 21) {
      _abbrevNm = _abbrevNm.substring(0, 21);
    }
    FormManager.setValue('abbrevNm', _abbrevNm);

    if (FormManager.getActualValue('landCntry') == "KH") {
      _abbrevLocn = "Cambodia";
    }
    if (FormManager.getActualValue('landCntry') != "KH") {
      var cntryDesc = getLandCntryDesc(FormManager.getActualValue('landCntry'));
      _abbrevLocn = cntryDesc;
    }

    if (_abbrevLocn && _abbrevLocn.length > 9) {
      _abbrevLocn = _abbrevLocn.substring(0, 9);
    }
    FormManager.setValue('abbrevLocn', _abbrevLocn);

    break;
  case SysLoc.HONG_KONG:
  case SysLoc.MACAO:
    if (custSubGrp != null && custSubGrp.length > 0 && custNm1) {
      if (custSubGrp == "AQSTN"  || custSubGrp == "XAQST") {
        _abbrevNm = "Acquisition use only";
      } else if (custSubGrp == "ASLOM"  || custSubGrp == "XASLM") {
        _abbrevNm = "ASL use only";
      } else if (custSubGrp == "BLUMX"  || custSubGrp == "XBLUM") {
        _abbrevNm = "Bluemix use only";
      } else if (custSubGrp == "MKTPC"  || custSubGrp == "XMKTP") {
        _abbrevNm = "Market Place Order";
      } else if (custSubGrp == "SOFT"  || custSubGrp == "XSOFT") {
        _abbrevNm = "Softlayer use only";
      } else {
        _abbrevNm = custNm1;
      }
    }

    if (_abbrevNm && _abbrevNm.length > 21) {
      _abbrevNm = _abbrevNm.substring(0, 21);
    }
    FormManager.setValue('abbrevNm', _abbrevNm);

    _abbrevLocn = "00 HK";
    FormManager.setValue('abbrevLocn', _abbrevLocn);

    break;
  case SysLoc.AUSTRALIA:
    if (custSubGrp != null && custSubGrp.length > 0 && custNm1) {
      if (custSubGrp == "BLUMX" || custSubGrp == "XBLUM") {
        _abbrevNm = "Bluemix_" + custNm1;
      } else if (custSubGrp == "MKTPC"  || custSubGrp == "XMKTP") {
        _abbrevNm = "Marketplace_" +  custNm1;
      } else if (custSubGrp == "SOFT"  || custSubGrp == "XSOFT") {
        _abbrevNm = "Softlayer Use Only";
      } else if (custSubGrp == "IGF"  || custSubGrp == "XIGF") {
        _abbrevNm = "IGF_" + custNm1;
      } else if (custSubGrp == "DUMMY"  || custSubGrp == "XDUMM") {
        _abbrevNm = "Dummy_" + custNm1;
      }else if (custSubGrp == "ESOSW"  || custSubGrp == "XESO") {
        _abbrevNm = "ESA/OEM/SWG_" + custNm1;
      }else {
        _abbrevNm = custNm1;
      }
    }
    
    if (_abbrevNm && _abbrevNm.length > 21) {
      _abbrevNm = _abbrevNm.substring(0, 21);
    }
    FormManager.setValue('abbrevNm', _abbrevNm);

    if (FormManager.getActualValue('landCntry') == "AU") {
      if (FormManager.getActualValue('city1') != 'undefined') {
        _abbrevLocn = FormManager.getActualValue('city1');
      }
    }
    if (FormManager.getActualValue('landCntry') != "AU") {
      var cntryDesc = getLandCntryDesc(FormManager.getActualValue('landCntry'));
      _abbrevLocn = cntryDesc;
    }

    if (_abbrevLocn && _abbrevLocn.length > 12) {
      _abbrevLocn = _abbrevLocn.substring(0, 12);
    }
    FormManager.setValue('abbrevLocn', _abbrevLocn);

    break;
  case SysLoc.NEW_ZEALAND:
    if (custSubGrp != null && custSubGrp.length > 0 && custNm1) {
      if (custSubGrp == "BLUMX" || custSubGrp == "XBLUM") {
        _abbrevNm = "Bluemix_" + custNm1;
      } else if (custSubGrp == "MKTPC"  || custSubGrp == "XMKTP") {
        _abbrevNm = "Marketplace_" +  custNm1;
      } else if (custSubGrp == "SOFT"  || custSubGrp == "XSOFT") {
        _abbrevNm = "Softlayer Use Only";
      } else if (custSubGrp == "IGF"  || custSubGrp == "XIGF") {
        _abbrevNm = "IGF_" + custNm1;
      } else if (custSubGrp == "DUMMY"  || custSubGrp == "XDUMM") {
        _abbrevNm = "Dummy_" + custNm1;
      }else if (custSubGrp == "ESOSW"  || custSubGrp == "XESO") {
        _abbrevNm = "ESA/OEM/SWG_" + custNm1;
      }else {
        _abbrevNm = custNm1;
      }
    }
    
    if (_abbrevNm && _abbrevNm.length > 21) {
      _abbrevNm = _abbrevNm.substring(0, 21);
    }
    FormManager.setValue('abbrevNm', _abbrevNm);

    if (FormManager.getActualValue('landCntry') == "NZ") {
      if (FormManager.getActualValue('city1') != 'undefined') {
        _abbrevLocn = FormManager.getActualValue('city1');
      }
    }
    if (FormManager.getActualValue('landCntry') != "NZ") {
      var cntryDesc = getLandCntryDesc(FormManager.getActualValue('landCntry'));
      _abbrevLocn = cntryDesc;
    }

    if (_abbrevLocn && _abbrevLocn.length > 12) {
      _abbrevLocn = _abbrevLocn.substring(0, 12);
    }
    FormManager.setValue('abbrevLocn', _abbrevLocn);
    break;
  }
}

function getLandCntryDesc(cntryCd) {
  if (cntryCd != null) {
    reqParam = {
      COUNTRY : cntryCd,
    };
  }
  var results = cmr.query('GET.CNTRY_DESC', reqParam);
  _cntryDesc = results.ret1;
  return _cntryDesc;
}

function onSubIndustryChange() {
  console.log(">>>> onSubIndustryChange >>>>");
  var reqType = null;
  reqType = FormManager.getActualValue('reqType');
  if (reqType == 'U') {
    console.log(">>>> Exit onSubIndustChange for Update.");
    return;
  }
  _subIndCdHandler = dojo.connect(FormManager.getField('subIndustryCd'), 'onChange', function(value) {
    if (!value) {
      return;
    }
    if (value != null && value.length > 1) {
      updateIndustryClass();
      addSectorIsbuLogicOnSubIndu();
    }
  });
  if (_subIndCdHandler && _subIndCdHandler[0]) {
    _subIndCdHandler[0].onChange();
  }
}

// Story -2125 drop down list needed in INAC/NAC FIELD based on INAC Type
var _inacCdHandler = null;
function onInacTypeChange(){
  var reqType = null;
  reqType = FormManager.getActualValue('reqType');
  if(reqType == 'C')
    {
     if (_inacCdHandler == null){
    _inacCdHandler= dojo.connect(FormManager.getField('inacType'), 'onChange', function(value){
      var value = FormManager.getActualValue('inacType');
      console.log(value);
      if(value!= null){
        var inacCdValue = [];
        var qParams = {
          _qall : 'Y',
          CMT : value , 
           };
           var results = cmr.query('GET.INAC_CD', qParams);
           if (results != null) {
             for (var i = 0; i < results.length; i++) {
               inacCdValue.push(results[i].ret1);
               }
             if (value == 'N')
             {
             inacCdValue.push('new');
             }           
             FormManager.limitDropdownValues(FormManager.getField('inacCd'), inacCdValue);
             if (inacCdValue.length == 1) {
               FormManager.setValue('inacCd', inacCdValue[0]);
             }
           } 
      }
    });
   }
  } 
}

function updateIndustryClass() {
  console.log(">>>> updateIndustryClass >>>>");
  var subIndustryCd = FormManager.getActualValue('subIndustryCd');
  if (subIndustryCd != null && subIndustryCd.length > 1) {
    var _industryClass = subIndustryCd.substr(0, 1);
    FormManager.setValue('IndustryClass', _industryClass);
  }
}

function searchArryAndSetValue(arry, searchValue, fieldID, setValue) {
  for ( var i = 0; i < arry.length; i++) {
    if (arry[i] == searchValue) {
      FormManager.setValue(fieldID, setValue);
    }
  }
}

function addSectorIsbuLogicOnSubIndu() {
  console.log(">>>> addSectorIsbuLogicOnSubIndu >>>>");
  var arryIndCdForSectorCOM = [ 'K', 'U', 'A' ];
  var arryIndCdForSectorDIS = [ 'D', 'W', 'T', 'R' ];
  var arryIndCdForSectorFSS = [ 'F', 'S', 'N' ];
  var arryIndCdForSectorIND = [ 'M', 'P', 'J', 'V', 'L' ];
  var arryIndCdForSectorPUB = [ 'H', 'X', 'Y', 'G', 'E' ];
  var arryIndCdForSectorCSI = [ 'B', 'C' ];
  var arryIndCdForSectorEXC = [ 'Z' ];

  FormManager.setValue('sectorCd', '');
  var subIndustryCd = FormManager.getActualValue('subIndustryCd');
  if (subIndustryCd != null && subIndustryCd.length > 1) {
    var _industryClass = subIndustryCd.substr(0, 1);
    searchArryAndSetValue(arryIndCdForSectorCOM, _industryClass, 'sectorCd', 'COM');
    searchArryAndSetValue(arryIndCdForSectorDIS, _industryClass, 'sectorCd', 'DIS');
    searchArryAndSetValue(arryIndCdForSectorFSS, _industryClass, 'sectorCd', 'FSS');
    searchArryAndSetValue(arryIndCdForSectorIND, _industryClass, 'sectorCd', 'IND');
    searchArryAndSetValue(arryIndCdForSectorPUB, _industryClass, 'sectorCd', 'PUB');
    searchArryAndSetValue(arryIndCdForSectorCSI, _industryClass, 'sectorCd', 'CSI');
    searchArryAndSetValue(arryIndCdForSectorEXC, _industryClass, 'sectorCd', 'EXC');
  }

  updateIsbuCd();
}

function updateIsbuCd() {
  console.log(">>>> updateIsbuCd >>>>");
  var _mrcCd = FormManager.getActualValue('mrcCd');
  var _sectorCd = FormManager.getActualValue('sectorCd');
  var _industryClass = FormManager.getActualValue('IndustryClass');
  var _isbuCd = null;
  if (_sectorCd == null) {
    console.log(">>>> Error, _sectorCd is null");
  }
  if (_industryClass == null) {
    console.log(">>>> Error, _industryClass is null");
  }
  if (_mrcCd == null) {
    console.log(">>>> Error, _mrcCd is null");
  }
// FormManager.setValue('isbuCd', '');
  if (_mrcCd == '3' && FormManager.getActualValue('isbuCd') == '') {
    _isbuCd = 'GMB' + _industryClass;
    FormManager.setValue('isbuCd', _isbuCd);
  } else if (_mrcCd == '2') {
    _isbuCd = _sectorCd + _industryClass;
    FormManager.setValue('isbuCd', _isbuCd);
  }

  setISBUScenarioLogic();
}

function setISBUScenarioLogic() {
  console.log(">>>> setISBUScenarioLogic");
  var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var isbuList = null;
  
  if (custSubGrp == 'BLUMX' || custSubGrp == 'XBLUM') {
    if (cmrIssuingCntry == '615' || cmrIssuingCntry == '652' || cmrIssuingCntry == '744' || cmrIssuingCntry == '834') {
      FormManager.setValue('isbuCd', 'GMBW');
    }
  } else if (custSubGrp == 'DUMMY' || custSubGrp == 'XDUMM') {
    if (cmrIssuingCntry == '615' || cmrIssuingCntry == '643' || cmrIssuingCntry == '646' || cmrIssuingCntry == '652' || cmrIssuingCntry == '714' || cmrIssuingCntry == '720' || cmrIssuingCntry == '744' || cmrIssuingCntry == '749' || cmrIssuingCntry == '778' || cmrIssuingCntry == '818' || cmrIssuingCntry == '834' || cmrIssuingCntry == '852' || cmrIssuingCntry == '856') {
      FormManager.setValue('isbuCd', 'DUM1');
    }
  } else if (custSubGrp == 'IGF' || custSubGrp == 'XIGF') {
    if (cmrIssuingCntry == '744') {
      FormManager.setValue('isbuCd', 'DUM1');
    }
  } else if (custSubGrp == 'INTER' || custSubGrp == 'XINT') {
    if (cmrIssuingCntry == '615' || cmrIssuingCntry == '643' || cmrIssuingCntry == '646' || cmrIssuingCntry == '652' || cmrIssuingCntry == '714' || cmrIssuingCntry == '720' || cmrIssuingCntry == '744' || cmrIssuingCntry == '749' || cmrIssuingCntry == '778' || cmrIssuingCntry == '818' || cmrIssuingCntry == '834' || cmrIssuingCntry == '852' || cmrIssuingCntry == '856') {
      FormManager.setValue('isbuCd', 'INT1');
    }
  } else if (custSubGrp == 'MKTPC' ||  custSubGrp == 'XMKTP') {
    if (cmrIssuingCntry == '744' || cmrIssuingCntry == '834') {
      FormManager.setValue('isbuCd', 'GMBW');
    }
  } else if (custSubGrp == 'BUSPR' ||  custSubGrp == 'XBUSP') {
    if (cmrIssuingCntry == '643' || cmrIssuingCntry == '646' || cmrIssuingCntry == '714' || cmrIssuingCntry == '720' || cmrIssuingCntry == '749' || cmrIssuingCntry == '778' || cmrIssuingCntry == '818' || cmrIssuingCntry == '834' || cmrIssuingCntry == '852' || cmrIssuingCntry == '856') {
      isbuList = [ 'BPN1', 'BPN2' ];
      console.log("isbuList = " + isbuList);
      FormManager.enable('isbuCd');
     FormManager.setValue('isbuCd', '');
      FormManager.limitDropdownValues(FormManager.getField('isbuCd'), isbuList);
    }
  }
}

function onIsuCdChangeAseanAnzIsa() {
  console.log(">>>> onIsuCdChangeAseanAnzIsa >>>>");
  var reqType = null;
  reqType = FormManager.getActualValue('reqType');
  var cmrIssuingCntry =  dojo.byId('cmrIssuingCntry').value;
  var asean_isa_cntries = ['Indonesia - 749','Brunei Darussalam - 643','Thailand - 856','Philippines - 818','New Zealand - 749','Malaysia - 778','Singapore - 834','Vietnam - 852','Bangladesh - 615','India - 744','Sri Lanka - 652', 'Australia - 616'];
  
  if (reqType == 'U') {
    console.log(">>>> Exit onIsuCdChangeAseanAnz for Update.");
    return;
  }
  // FormManager.enable('isuCd');
  _isuHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
    if (!value) {
      if(value == ''){
        if(asean_isa_cntries.indexOf(cmrIssuingCntry) >= 0)
        FormManager.removeValidator('inacCd', Validators.REQUIRED);
      }
        else if(asean_isa_cntries.indexOf(cmrIssuingCntry) >= 0){
        setINACState();
      }
      
      return;
    }
    
    
    if (value != null && value.length > 1) {
      updateMRCAseanAnzIsa();
      // addSectorIsbuLogicOnISU();
      updateIsbuCd();
      if(asean_isa_cntries.indexOf(cmrIssuingCntry) >= 0){
        setINACState();
      }
    }
  });
  if (_isuHandler && _isuHandler[0]) {
    _isuHandler[0].onChange();
  }
}

function updateMRCAseanAnzIsa() {
  console.log(">>>> updateMRC >>>>");
  var arryISUCdForMRC3 = [ '32', '34', '21' ];
  FormManager.setValue('mrcCd', '');
  var _isuCd = FormManager.getActualValue('isuCd');
  if (_isuCd != null && _isuCd.length > 1) {
    FormManager.setValue('mrcCd', '3');
    var _exsitFlag = 0;
    for ( var i = 0; i < arryISUCdForMRC3.length; i++) {
      if (arryISUCdForMRC3[i] == _isuCd) {
        _exsitFlag = 1;
      }
    }
    if (_exsitFlag == 0) {
      FormManager.setValue('mrcCd', '2');
    }
  }
}

function addSectorIsbuLogicOnISU() {
  console.log(">>>> addSectorIsbuLogicOnISU >>>>");
  var arryISUCdForSectorCOM = [ '05', '12', '3T' ];
  var arryISUCdForSectorDIS = [ '18', '19', '1R' ];
  var arryISUCdForSectorFSS = [ '04', '4F', '31' ];
  var arryISUCdForSectorIND = [ '14', '15', '4A', '4D', '5E' ];
  var arryISUCdForSectorPUB = [ '11', '28', '40', '8C' ];
  var arryISUCdForSectorCSI = [ '5B' ];
  var arryISUCdForSectorEXC = [ '21' ];
  var arryISUCdForSectorGMB = [ '32', "34" ];

  FormManager.setValue('sectorCd', '');
  var _isuCd = FormManager.getActualValue('isuCd');
  if (_isuCd != null && _isuCd.length > 1) {
    searchArryAndSetValue(arryISUCdForSectorCOM, _isuCd, 'sectorCd', 'COM');
    searchArryAndSetValue(arryISUCdForSectorDIS, _isuCd, 'sectorCd', 'DIS');
    searchArryAndSetValue(arryISUCdForSectorFSS, _isuCd, 'sectorCd', 'FSS');
    searchArryAndSetValue(arryISUCdForSectorIND, _isuCd, 'sectorCd', 'IND');
    searchArryAndSetValue(arryISUCdForSectorPUB, _isuCd, 'sectorCd', 'PUB');
    searchArryAndSetValue(arryISUCdForSectorCSI, _isuCd, 'sectorCd', 'CSI');
    searchArryAndSetValue(arryISUCdForSectorEXC, _isuCd, 'sectorCd', 'EXC');
    searchArryAndSetValue(arryISUCdForSectorGMB, _isuCd, 'sectorCd', 'GMB');
  }
  updateIsbuCd();
}

function updateProvCd() {
  console.log(">>>> updateProvCd");
  FormManager.readOnly('territoryCd');
  _provNmHandler = dojo.connect(FormManager.getField('busnType'), 'onChange', function(value) {
    if (!value) {
      value = FormManager.getField('busnType');
    }
    if (!value) {
      FormManager.setValue('territoryCd', '');
    }
    if (value != null && value.length > 1) {
      var _ProvNm = FormManager.getActualValue('busnType');
      FormManager.setValue('territoryCd', _ProvNm);
      if(FormManager.getActualValue('cmrIssuingCntry') == '744' && FormManager.getActualValue('reqType') == 'C')
      setCollCdFrIndia();
    }
  });
  if (_provNmHandler && _provNmHandler[0]) {
    _provNmHandler[0].onChange();
  }
}

function updateRegionCd() {
  console.log(">>>> updateRegionCd");
  _provNmHandler = dojo.connect(FormManager.getField('busnType'), 'onChange', function(value) {
    if (!value) {
      return;
    }
    if (value != null && value.length > 1) {
      var provCd = FormManager.getActualValue('busnType');
      var _issuingCntry = FormManager.getActualValue('cmrIssuingCntry');
      var qParams = {
        CNTRY : _issuingCntry,
        PROV_CD : provCd,
      };
      var _result = cmr.query('GET.RAGION_CD', qParams);
      var _regionCd = _result.ret1;
      FormManager.setValue('miscBillCd', _regionCd);
    }
  });
  if (_provNmHandler && _provNmHandler[0]) {
    _provNmHandler[0].onChange();
  }
}

function canRemoveAddress(value, rowIndex, grid){ 
  var rowData = grid.getItem(rowIndex);
  var importInd = rowData.importInd[0];
  var reqType = FormManager.getActualValue('reqType');
  if ('U' == reqType && 'Y' == importInd){
    return false;
  }
  return true;
}

function ADDRESS_GRID_showCheck(value, rowIndex, grid){
  return canRemoveAddress(value, rowIndex, grid);
}

function setCTCIsuByCluster() { 
  var reqType = FormManager.getActualValue('reqType');	
  if (reqType != 'C') {
    return;
  }
  var _clusterHandler = dojo.connect(FormManager.getField('apCustClusterId'), 'onChange', function(value) {	
    var clusterVal = FormManager.getActualValue('apCustClusterId');
    if (!clusterVal) {
	    return;
	  }
 	  var _cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  	var _cluster = FormManager.getActualValue('apCustClusterId');
  	
  	var apClientTierValue = [];
  	var isuCdValue = [];
  	if (_cluster != '' && _cluster != '') {
  	  var qParams = {
  	    _qall : 'Y',
  	    ISSUING_CNTRY : _cmrIssuingCntry,
  	    CLUSTER : _cluster,
  	   };
  	   var results = cmr.query('GET.CTC_ISU_BY_CLUSTER_CNTRY', qParams);
  	   if (results != null) {
  	     for (var i = 0; i < results.length; i++) {
  	    	 apClientTierValue.push(results[i].ret1);
  	    	 isuCdValue.push(results[i].ret2);
  	     }
  	     FormManager.limitDropdownValues(FormManager.getField('clientTier'), apClientTierValue);
  	     FormManager.limitDropdownValues(FormManager.getField('isuCd'), isuCdValue);
  	     if (apClientTierValue.length == 1) {
  	       FormManager.setValue('clientTier', apClientTierValue[0]);
  	       FormManager.setValue('isuCd', isuCdValue[0]);
  	     }
  	   }
  	}
  });
  if (_clusterHandler && _clusterHandler[0]) {
	  _clusterHandler[0].onChange();
  }
}

function setIsuByClusterCTC() {
	  var reqType = FormManager.getActualValue('reqType');	
	  if (reqType != 'C') {
		return;
	  }
	  var _clientTierHandler = dojo.connect(FormManager.getField('clientTier'), 'onChange', function(value) {	
	    if (!value) {
		  return;
		}
		var _cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
		var _clientTier = FormManager.getActualValue('clientTier');
		var _cluster = FormManager.getActualValue('apCustClusterId');
		
		var isuCdValue = [];
		if (_clientTier != '' && _cluster != '') {
		  var qParams = {
		    _qall : 'Y',
			ISSUING_CNTRY : _cmrIssuingCntry,
			CLIENT_TIER_CD : _clientTier,
			CLUSTER : _cluster,
		   };
		   var results = cmr.query('GET.ISU_BY_CLUSTER_CNTRY_CTC', qParams);
		   if (results != null) {
		     for (var i = 0; i < results.length; i++) {
		    	 isuCdValue.push(results[i].ret1);
		     }
		     FormManager.limitDropdownValues(FormManager.getField('isuCd'), isuCdValue);
		     if (isuCdValue.length == 1) {
		       FormManager.setValue('isuCd', isuCdValue[0]);
		     }
		   }
		}
	  });
	  if (_clientTierHandler && _clientTierHandler[0]) {
		  _clientTierHandler[0].onChange();
	  }
	}

function addAbnValidatorForAU() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var abn = FormManager.getActualValue('vat');
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        if (custSubGrp == "AQSTN"  || custSubGrp == "XAQST" || custSubGrp == "IGF"  || custSubGrp == "XIGF"|| custSubGrp == "NRML"  || custSubGrp == "XNRML" || custSubGrp == "SOFT"  || custSubGrp == "XSOFT") {
          if (abn && abn.length != 11) {
            return new ValidationResult({
                id : 'vat',
                type : 'text',
                name : 'ABN#'
              }, false, 'The length of ABN# should be exactly 11.');
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

/*
 * function addCmrNoForSG() { FormManager.addFormValidator((function() { return {
 * validate : function() { var cmrNo = FormManager.getActualValue('cmrNo'); var
 * role = FormManager.getActualValue('userRole').toUpperCase(); var reqType =
 * FormManager.getActualValue('reqType'); var custSubGrp =
 * FormManager.getActualValue('custSubGrp'); var custGrp =
 * FormManager.getActualValue('custGrp'); var cntry =
 * FormManager.getActualValue('cmrIssuingCntry');
 * 
 * if (custGrp == 'cross' && custSubGrp == 'SNOFF' && role == 'PROCESSOR' &&
 * reqType == 'C') { if (cmrNo && cmrNo.length != 6) { return new
 * ValidationResult({ id : 'cmrNo', type : 'text', name : 'CMR No#' }, false,
 * 'The length of CMR No# should be exactly 6.'); } else { return new
 * ValidationResult(null, true); } } else { return new ValidationResult(null,
 * true); } } }; })(), 'MAIN_IBM_TAB', 'frmCMR'); }
 */



/* STory : 1782082 - Singapore */

function addCmrNoValidator() {
	  FormManager.addFormValidator((function() {
	    return {
	      validate : function() {
	    	  var cntry = FormManager.getActualValue('cmrIssuingCntry');
		        var custSubType = FormManager.getActualValue('custSubGrp');
		        var custGrp = FormManager.getActualValue('custGrp');
		        var cmrNo = FormManager.getActualValue('cmrNo');
		        var role = FormManager.getActualValue('userRole').toUpperCase();
		        
	        if (FormManager.getActualValue('reqType') == 'C' && FormManager.getActualValue('Processor') == 'true' &&  custGrp== 'cross' && custSubType== 'SPOFF' ) {
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

/*
 * function addMandateCmrNoForSG() { var role =
 * FormManager.getActualValue('userRole').toUpperCase(); var scenario =
 * FormManager.getActualValue('custGrp'); var custSubGrp =
 * FormManager.getActualValue('custSubGrp'); var reqType =
 * FormManager.getActualValue('reqType'); if(cmr.currentRequestType == 'C' &&
 * scenario == 'CROSS' && custSubGrp == 'SPOFF' && role == 'PROCESSOR'){
 * FormManager.addValidator('cmrNo', Validators.REQUIRED, [ 'CMR Number' ], '');
 * }else{ FormManager.removeValidator('cmrNo', Validators.REQUIRED); } }
 */


function removeStateValidatorForHkMoNZ() {
	var _landCntryHandler = dojo.connect(FormManager.getField('landCntry'), 'onChange', function(value) {
		var landCntry = FormManager.getActualValue('landCntry');
		if(FormManager.getActualValue('cmrIssuingCntry') == '616'){
		if (landCntry == 'HK' || landCntry == 'MO') {
			FormManager.resetValidations('stateProv');
		} else {
			FormManager.addValidator('stateProv', Validators.REQUIRED, [ 'State/Province' ], null);
		}
		}
		if(FormManager.getActualValue('cmrIssuingCntry') == '796'){
	    if (landCntry == 'CK') {
	      FormManager.resetValidations('stateProv');
	      FormManager.resetValidations('city1');
	      FormManager.resetValidations('postCd');

	    } else {
	      FormManager.addValidator('stateProv', Validators.REQUIRED, [ 'State' ], null);
	      if (landCntry == '' || FormManager.GETFIELD_VALIDATIONS['landCntry'].indexOf(Validators.REQUIRED) < 0)
	      FormManager.addValidator('postCd', Validators.REQUIRED, [ 'Postal Code' ], null);
	      FormManager.addValidator('city1', Validators.REQUIRED, [ 'Suburb' ], null);
	    }
	    }
	});
	if (_landCntryHandler && _landCntryHandler[0]) {
		_landCntryHandler[0].onChange();
	}
}

/*
 * function addValidatorForSingapore(){ //var role = null; var role =
 * FormManager.getActualValue('userRole').toUpperCase(); var cmrCntry =
 * FormManager.getActualValue('cmrIssuingCntry'); //var isaCntries = ['834'];
 * var reqType = FormManager.getActualValue('reqType'); var custGrp =
 * FormManager.getActualValue('custGrp'); var custSubGrp =
 * FormManager.getActualValue('custSubGrp'); if (typeof (_pagemodel) !=
 * 'undefined') { role = _pagemodel.userRole; }
 * 
 * if(role == 'PROCESSOR' && cmrCntry == '834' && custGrp== 'cross' &&
 * custSubGrp== 'SPOFF' && reqType== 'C'){ FormManager.addValidator('cmrNo',
 * Validators.REQUIRED, [ 'CMR No' ], 'MAIN_IBM_TAB'); }
 * 
 * else{ FormManager.removeValidator('cmrNo', Validators.REQUIRED); } }
 */

function setINACState(){
  var role = null;
  var isuCd = FormManager.getActualValue('isuCd');
  var cmrCntry = FormManager.getActualValue('cmrIssuingCntry');
  var isaCntries = ['615','652','744'];
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }
  if (role == 'Requester' && (isuCd == '34' || isuCd == '04' || isuCd == '3T') && cmrCntry == '616')
  { 
 FormManager.addValidator('inacCd',Validators.REQUIRED,[ 'INAC/NAC Code' ],'MAIN_IBM_TAB');
 FormManager.addValidator('inacType',Validators.REQUIRED,[ 'INAC Type' ],'MAIN_IBM_TAB');
  }
  else if(role == 'Requester' && isuCd == '34'){
    FormManager.addValidator('inacCd', Validators.REQUIRED, [ 'INAC/NAC Code' ], 'MAIN_IBM_TAB');
  }
  else if(role == 'Requester' && (isuCd == '34'|| isuCd == '3T' || isuCd == '5B') && (isaCntries.indexOf(cmrCntry) >= 0)){
    FormManager.addValidator('inacCd', Validators.REQUIRED, [ 'INAC/NAC Code' ], 'MAIN_IBM_TAB');
  } 
  else{
    FormManager.removeValidator('inacCd', Validators.REQUIRED);
  }
}

function onISBUCdChange() {
  console.log(">>>> onISBUCdChange >>>>");
  var reqType = null;
  reqType = FormManager.getActualValue('reqType');  
  if (reqType == 'U') {
    console.log(">>>> Exit onISBUCdChange for Update.");
    return;
  }
  _isbuHandler = dojo.connect(FormManager.getField('isbuCd'), 'onChange', function(value) {
    if (!value) {
      return;
    }
    if (value != null && value.length > 1) {
        setCollectionCd();
    }
  });
  if (_isuHandler && _isuHandler[0]) {
    _isuHandler[0].onChange();
  }
}

/* Setting Collection code for INDIA(744) based on Province Cd */
function setCollCdFrIndia(){
  var reqType = FormManager.getActualValue('reqType');
  if(reqType == 'U'){
    return;
  }
  var provCd = FormManager.getActualValue('territoryCd');
  var collCd = null;
  if (provCd != '') {
    var qParams = {
        CNTRY : '744',
        PROV_CD : provCd,
      };
    var result = cmr.query('GET.ARCODE.BYPROV_CD', qParams);
    if (result != null && result.ret1 != '') {   
        collCd = result.ret1;
      if (collCd != null) {
          FormManager.setValue('collectionCd', collCd);
      }
    }
  }        
}

function setCollCdFrAU(cntry, addressMode, saving, finalSave, force){
  var reqType = FormManager.getActualValue('reqType');
  var record = null;
  var addrType = null;
  var custNm1 = null;
  var provCd = null;
  if(reqType == 'U'){
    return;
  } 
  if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
   
    for ( var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
      record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
      if (record == null && _allAddressData != null && _allAddressData[i] != null) {
        record = _allAddressData[i];
      }
      addrType = record.addrType[0];  
      custNm1 = record.custNm1[0]; 
      provCd = record.stateProv[0]; 
  }   
  }
  if (FormManager.getActualValue('addrType') == 'ZS01' || (addrType != null && addrType == 'ZS01')){
  var regEx1 = /^[A-C]/;
  var regEx2 = /^[D-K]/;
  var regEx3 = /^[L-R]/;
  var regEx4 = /^[S-Z]/;
  var regEx5 = /^[A-Q]/;
  var regEx6 = /^[R-Z]/;
  var regEx7 = /^[A-Z]/;
  
// var result = null;
// var zs01ReqId = FormManager.getActualValue('reqId');
// var qParams = {
// REQ_ID : zs01ReqId,
// };
// if(cmr.addressMode == 'newAddress' || cmr.addressMode == 'copyAddress'){
// result = cmr.query('GET.CUSTNM_PROV_BYADRTYP', qParams);
// if(result != null && result.ret1 != null){
// custNm1 = result.ret1;
// provCd = result.ret2;
// }
    if(custNm1 == null || custNm1 == '' || cmr.addressMode == 'updateAddress')
    custNm1 = FormManager.getActualValue('custNm1');
    if(provCd == null || provCd == '' || cmr.addressMode == 'updateAddress')
    provCd = FormManager.getActualValue('stateProv');
// }
// else{
// custNm1 = FormManager.getActualValue('custNm1');
// provCd = FormManager.getActualValue('stateProv');
// }
  if(['NSW','NT','ACT'].indexOf(provCd) >= 0){
    if (regEx1.test(custNm1)) {
      FormManager.setValue('collectionCd','00JC');
    }
    if (regEx2.test(custNm1)) {
      FormManager.setValue('collectionCd','00JD');
    }
    if (regEx3.test(custNm1)) {
      FormManager.setValue('collectionCd','00I2');
    }
    if (regEx4.test(custNm1)) {
      FormManager.setValue('collectionCd','00JK');
    }
  }  
  if(['VIC','TAS'].indexOf(provCd) >= 0){
    if (regEx5.test(custNm1)) {
      FormManager.setValue('collectionCd','00J1');
    }
    if (regEx6.test(custNm1)) {
      FormManager.setValue('collectionCd','00A2');
    }
  }    
  if(provCd == 'QLD'){
    if (regEx7.test(custNm1)) {
      FormManager.setValue('collectionCd','00OS');
    }
  }  
  if(provCd == 'SA'){
    if (regEx7.test(custNm1)) {
      FormManager.setValue('collectionCd','00GG');
    }
  }  
  if(provCd == 'WA'){
    if (regEx7.test(custNm1)) {
      FormManager.setValue('collectionCd','00PZ');
    }
  }  
}
}

var _govIndcHandler = null;
function addGovIndcHanlder(){
  _govIndcHandler = dojo.connect(FormManager.getField('govType'), 'onClick', function(value) {
    var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
    if (viewOnlyPage != 'true' && FormManager.getActualValue('reqType') == 'C') {
      FormManager.resetValidations('taxCd2');
      if (dijit.byId('govType').get('checked')) {
        FormManager.addValidator('taxCd2', Validators.REQUIRED, [ 'Government Customer Type' ], 'MAIN_IBM_TAB');
      }
      else{
        FormManager.removeValidator('taxCd2', Validators.REQUIRED);
      }
    }  });  
}

 var _govCustTypeHandler = null;
function addGovCustTypHanlder(){
  _govIndcHandler = dojo.connect(FormManager.getField('taxCd2'), 'onChange', function(value) {
    setAbbrevNameforGovType();
  });  
}

function setAbbrevNameforGovType(){
  var govCustType = FormManager.getActualValue('taxCd2');
  var abbrevNmPrefix = null;
  var abbrevNm = FormManager.getActualValue('abbrevNm');
  
  if (FormManager.getActualValue('reqType') == 'C' && (govCustType && govCustType != '') && (abbrevNm && abbrevNm != '') ) {
    switch (govCustType)
    {
    case '012' : 
      abbrevNmPrefix = 'ZC';
      break;
    case '013' : 
      abbrevNmPrefix = 'ZS';
      break;
    case '014' : 
      abbrevNmPrefix = 'ZL';
      break;
      default:
      abbrevNmPrefix = '';
    }
    if(abbrevNmPrefix != null)
      abbrevNm = abbrevNmPrefix + FormManager.getActualValue('abbrevNm');
    FormManager.setValue('abbrevNm',abbrevNm);
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

function addAddressInstancesValidator(){
  FormManager.addFormValidator((function() {
     return {
       validate : function() {
         if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount == 0) {
           return new ValidationResult(null, false, 'One Sold-To Address is mandatory. Only one address for each address type should be defined when sending for processing.');
         }
         var cmrCntry = FormManager.getActualValue('cmrIssuingCntry');
         var qParams = {
             _qall : 'Y',
           CMR_ISSUING_CNTRY : cmrCntry,
         };
         var results = cmr.query('GETADDR_TYPES', qParams);
         var duplicatesAddr = [];
         if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
           var record = null;
           var type = null;  
           if (results != null){
           for(var j = 0; j < results.length; j++) {
             var addrCnt = 0;
           for ( var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
             record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
             if (record == null && _allAddressData != null && _allAddressData[i] != null) {
               record = _allAddressData[i];
             }
             type = record.addrType;
             if (typeof (type) == 'object') {
               type = type[0];
             }
             if (results[j].ret1 == type) {
             // New requirement : Defect 1767113 : For HK and MO -> Multiple
              // Billing & Billing CCR are to be allowed
               if((cmrCntry == SysLoc.MACAO || cmrCntry == SysLoc.HONG_KONG) && (record.addrType == 'ZP01' || record.addrType == 'ZP02')){
                 continue; 
               }
               addrCnt++;
               if(addrCnt > 1)
               duplicatesAddr.push(results[j].ret2);
             } 
           }
         }
       }
           if (duplicatesAddr.length > 0) {
             return new ValidationResult(null, false, 'Only one instance of each address can be added.Please remove additional '+duplicatesAddr+' addresses');
           } else {
             return new ValidationResult(null, true);
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
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0 && FormManager.getActualValue('reqType') == 'C') {
          var record = null;
          var type = null;
          var custName = null;
          var streetAddr = null;
          var streetAddrCont = null;
          var postCd = null;
          var city = null;
          var state = null;
          var cntry = FormManager.getActualValue('cmrIssuingCntry');
          var mandtDetails = 0;
          var mandtDetails_1 = 0;
          var mandtDetails_2 = 0;
          var mandtDetails_3 = 0;
          var mandtDetails_4 = 0;


          for ( var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
            record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
            if (record == null && _allAddressData != null && _allAddressData[i] != null) {
              record = _allAddressData[i];
            }
            type = record.addrType;
            custName = record.custNm1;
            streetAddr = record.addrTxt;
            streetAddrCont = record.addrTxt2;
            postCd = record.postCd;
            city = record.city1;
            state = record.stateProv;
            if (typeof (type) == 'object') {
              type = type[0];
            }
            if (typeof (custName) == 'object') {
              custName = custName[0];
            }
            if (typeof (streetAddr) == 'object') {
              streetAddr = streetAddr[0];
            }
            if (typeof (streetAddrCont) == 'object') {
              streetAddrCont = streetAddrCont[0];
            }
            if (typeof (postCd) == 'object') {
              postCd = postCd[0];
            }
            if (typeof (city) == 'object') {
              city = city[0];
            }
            if (typeof (state) == 'object') {
              state = state[0];
            }
 
            switch (cntry) {
            case '643':
            case '834':
              if ((custName == null || streetAddr == null || postCd == null)) {
                mandtDetails++;
              }
              break;
            case '778':
            case '818':
            case '852':
            case '856':
            case '749':
            case '615':
            case '744':
            case '752':
              if ((custName == null || streetAddr == null || postCd == null || city == null)) {
                mandtDetails_1++;
              }
              break; 
            case '796':
            case '616':
              if ((custName == null || streetAddr == null  || postCd == null || city == null || state == null)) {
                mandtDetails_2++;
              }
              break;
            case '736':
            case '738':
              if ((custName == null || streetAddr == null )) {
                mandtDetails_3++;
              }
              break;
            }          
          }
          if(mandtDetails > 0){
          return new ValidationResult(null, false, "Customer Name  is required, Street Address is required, Postal Code is required.");
          }
          else if(mandtDetails_1 > 0){
          return new ValidationResult(null, false, "Customer Name  is required, Street Address is required, Postal Code is required and City is required.");  
          }
          else if(mandtDetails_2 > 0 ){
            return new ValidationResult(null, false, "Customer Name  is required, Street Address is required, Postal Code is required, City is required and State is required.");
          }
          else if(mandtDetails_3 > 0){
            return new ValidationResult(null, false, "Customer Name  is required, Street Address is required.");   
            }  
        }
        return new ValidationResult(null, true);

      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');

}

function similarAddrCheckValidator(){
  FormManager.addFormValidator((function() {
     return {
       validate : function() {
         var req_id = FormManager.getActualValue('reqId');
         var req_type = FormManager.getActualValue('reqType');
         if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
           var record = null;
           var addrDupExists = [];
           var custNm1 = '';
           var custNm2 = '';
           var addrTxt = '';
           var city1 = '';
           var addrTxt2 = '';
           var stateProv = '';
           var landCntry = '';
           var importIndc = '';
           var dept = '';
           for ( var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
             record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
             if (record == null && _allAddressData != null && _allAddressData[i] != null) {
               record = _allAddressData[i];
             }
             custNm1 = record.custNm1[0];
             custNm2 = record.custNm2[0] != null ? record.custNm2[0] : '' ;
             addrTxt = record.addrTxt[0];
             city1 = record.city1[0] != null ? record.city1[0] : '' ;
             stateProv = record.stateProv[0] != null ? record.stateProv[0] : '' ;
             addrTxt2 = record.addrTxt2[0] != null ? record.addrTxt2[0] : '' ;;        
             landCntry = record.landCntry[0];    
             dept = record.dept[0] != null ? record.dept[0] : ''; 
             importIndc = record.importInd[0] != null ? record.importInd[0] : '';
            
             if(req_type == 'U' && importIndc == 'Y'){
               continue;   // skip the unique check for addresses in case of
                            // import for Update requests.
             }
             var qParams = {
               REQ_ID : req_id,
               CUST_NM1 : custNm1,
               CUST_NM2 : custNm2,
               ADDR_TXT : addrTxt,
               ADDR_TXT_2 : addrTxt2,
               CITY1 : city1,
               STATE_PROV : stateProv,
               DEPT : dept,
               LAND_CNTRY : landCntry
             };
             var results = cmr.query('GET.SME_ADDR_RECORDS_AP', qParams);
             if(results.ret1 > 1){
               addrDupExists.push(record.addrTypeText);
             }
           }
           if (addrDupExists.length > 0) {
             return new ValidationResult(null, false, 'Duplicate address details exist for Addresses ' +addrDupExists+'. Please make sure every address detail should be a unique combination.');
           } else {
             return new ValidationResult(null, true);
           }
         }
         else{
           return new ValidationResult(null, true);   
         }
       }
     };
   })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function setFieldsForDoubleCreates(){
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var custGrp = FormManager.getActualValue('custGrp');
  if (reqType == 'U') {
    return new ValidationResult(null, true);
  }
  
  if (cntry == '615' && role == 'PROCESSOR' && (custGrp == 'LOCAL' || custGrp == 'CROSS') ) {
    FormManager.enable('cmrNo');
    FormManager.enable('cmrNoPrefix');
  }
  if(cntry == '652' && (role == 'PROCESSOR' || role == 'REQUESTER') && (custGrp == 'LOCAL' || custGrp == 'CROSS') ){
    FormManager.enable('cmrNo');
    FormManager.enable('cmrNoPrefix');
  }
  if(cntry == '744' && role == 'PROCESSOR' && (custSubGrp == 'ESOSW' || custSubGrp == 'XESO') ){
    FormManager.enable('cmrNo');
    FormManager.enable('cmrNoPrefix');
  }
  if(cntry == '852' && role == 'PROCESSOR' && (custSubGrp != 'BLUMX' || custSubGrp != 'MKTPC') ){
    FormManager.enable('cmrNo');
    FormManager.enable('cmrNoPrefix');
  }
}

/*
 * function addDoubleCreateValidatorSG(){
 * FormManager.addFormValidator((function() { return { validate : function() {
 * var role = FormManager.getActualValue('userRole').toUpperCase(); var reqType =
 * FormManager.getActualValue('reqType'); var cntry =
 * FormManager.getActualValue('cmrIssuingCntry'); var custSubGrp =
 * FormManager.getActualValue('custSubGrp'); var custGrp =
 * FormManager.getActualValue('custGrp'); if (reqType == 'U') { return new
 * ValidationResult(null, true); } if (FormManager.getActualValue('cmrNo') != '' ) {
 * showError = true; }else{ showError = false; } if((cntry == '834' && reqType ==
 * 'C' && role == 'PROCESSOR' && custGrp == 'CROSS' && custSubGrp == 'SPOFF')){
 * if(showError) return new ValidationResult(null, false, 'For CMR Number and
 * CMR Number Prefix in case of double creates, only one can be filled.'); else
 * if(FormManager.getActualValue('cmrNo') != ''){ var cmrNumber =
 * FormManager.getActualValue('cmrNo'); var katr6 =
 * FormManager.getActualValue('cmrIssuingCntry'); qParams = { KATR6 : katr6,
 * MANDT : cmr.MANDT, ZZKV_CUSNO : cmrNumber, }; var record =
 * cmr.query('CHECK_CMR_RDC', qParams); var rdcCMRCount = record.ret1; qParams = {
 * KATR6 : '834', MANDT : cmr.MANDT, ZZKV_CUSNO : cmrNumber, }; var recordSG =
 * cmr.query('CHECK_CMR_RDC', qParams); var rdcCMRCountSG = recordSG.ret1; if
 * (Number(rdcCMRCount) > 0 || Number(rdcCMRCountSG) > 0 ) { return new
 * ValidationResult(null, false, 'Please enter another CMR Number as entered CMR
 * Number already exists in RDC.'); }
 * 
 * var resultWTAAS = CmrServices.checkWTAAS(cmrNumber,katr6); var resultWTAAS_SG =
 * CmrServices.checkWTAAS(cmrNumber,'834'); if ((resultWTAAS.success &&
 * resultWTAAS.data && resultWTAAS.Status && resultWTAAS.Status == 'F' &&
 * resultWTAAS.Error_Msg != null &&
 * resultWTAAS.Error_Msg.toLowerCase().includes("does not exist")) &&
 * (resultWTAAS_SG.success && resultWTAAS_SG.data && resultWTAAS_SG.Status &&
 * resultWTAAS_SG.Status == 'F' && resultWTAAS_SG.Error_Msg != null &&
 * resultWTAAS_SG.Error_Msg.toLowerCase().includes("does not exist"))) { return
 * new ValidationResult(null, true); }else if(!resultWTAAS.success ||
 * !resultWTAAS_SG.success){ } } } } } } }
 */


/* Story : 1782082 - Singapore and defct : 1801410 */ 

function addDoubleCreateValidatorSG() {
	  FormManager.addFormValidator((function() {
	    return {
	      validate : function() {
	        var role = FormManager.getActualValue('userRole').toUpperCase();
	        var reqType = FormManager.getActualValue('reqType');
	        var cntry = FormManager.getActualValue('cmrIssuingCntry');
	        var custSubGrp = FormManager.getActualValue('custSubGrp');
	        var custGrp = FormManager.getActualValue('custGrp');
	        var cmrNumber = FormManager.getActualValue('cmrNo');
	        if (reqType == 'U') {
	          return new ValidationResult(null, true);
	        }
/*
 * if (FormManager.getActualValue('cmrNo') != '' ) { showError = true; }else{
 * showError = false; }
 */  
	        if (cntry == '834' && reqType == 'C' && role == 'PROCESSOR' && custGrp == 'CROSS' && custSubGrp == 'SPOFF') {
			  if(FormManager.getActualValue('cmrNo') != '' && cmrNumber.length == 6){
	            var cmrNumber = FormManager.getActualValue('cmrNo');
	            
	            qParams = {
	                KATR6 : '834',	
	                MANDT : cmr.MANDT,
	                ZZKV_CUSNO : cmrNumber,
	            };
	            var recordSG = cmr.query('CHECK_CMR_RDC', qParams);
	            var rdcCMRCountSG = recordSG.ret1;
	            
	            if (Number(rdcCMRCountSG) > 0 ) {
	            	
	              return new ValidationResult(null, false, 'Please enter another CMR Number as entered CMR Number already exists in RDC.');
	            } 

	            var resultWTAAS_SG = CmrServices.checkWTAAS(cmrNumber,'834');
	            if ((resultWTAAS_SG.success && resultWTAAS_SG.data && resultWTAAS_SG.Status &&  resultWTAAS_SG.Status == 'F' && resultWTAAS_SG.Error_Msg != null && resultWTAAS_SG.Error_Msg.toLowerCase().includes("does not exist"))) {
	              return new ValidationResult(null, true);
	            }else if(!resultWTAAS_SG.success){
	              return new ValidationResult(null, false, 'Cannot connect to WTAAS at the moment. Please try again after sometime.');
	            }else {
	              return new ValidationResult(null, false, 'Please enter another CMR Number as entered CMR Number already exists in WTAAS.');
	            }
	          }
	        }
	        return new ValidationResult(null, true);
	      }
	    };
	  })(), 'MAIN_IBM_TAB', 'frmCMR');
	}


function addDoubleCreateValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var role = FormManager.getActualValue('userRole').toUpperCase();
        var reqType = FormManager.getActualValue('reqType');
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        var custGrp = FormManager.getActualValue('custGrp');
        if (reqType == 'U') {
          return new ValidationResult(null, true);
        }
        
        if (FormManager.getActualValue('cmrNo') != '' && FormManager.getActualValue('cmrNoPrefix') != '') {
          showError = true;
        }else{
          showError = false;
        }
        
        if ((cntry == '615' && role == 'PROCESSOR' && (custGrp == 'LOCAL' || custGrp == 'CROSS')) || (cntry == '652' && (role == 'PROCESSOR' || role == 'REQUESTER') && (custGrp == 'LOCAL' || custGrp == 'CROSS') ) || (cntry == '744' && role == 'PROCESSOR' && (custSubGrp == 'ESOSW' || custSubGrp == 'XESO') ) || (cntry == '852' && role == 'PROCESSOR' && (custSubGrp != 'BLUMX' || custSubGrp != 'MKTPC') )) {
          if(showError)
            return new ValidationResult(null, false, 'For CMR Number and CMR Number Prefix in case of double creates, only one can be filled.');
          else if(FormManager.getActualValue('cmrNo') != ''){
            var cmrNumber = FormManager.getActualValue('cmrNo');
            var katr6 = FormManager.getActualValue('cmrIssuingCntry');
            qParams = {
              KATR6 : katr6,
              MANDT : cmr.MANDT,
              ZZKV_CUSNO : cmrNumber,
            };
            var record = cmr.query('CHECK_CMR_RDC', qParams);
            var rdcCMRCount = record.ret1;
            qParams = {
                KATR6 : '834',
                MANDT : cmr.MANDT,
                ZZKV_CUSNO : cmrNumber,
            };
            var recordSG = cmr.query('CHECK_CMR_RDC', qParams);
            var rdcCMRCountSG = recordSG.ret1;
            if (Number(rdcCMRCount) > 0 || Number(rdcCMRCountSG) > 0 ) {
            	
              return new ValidationResult(null, false, 'Please enter another CMR Number as entered CMR Number already exists in RDC.');
            } 
            
                                      
            var resultWTAAS = CmrServices.checkWTAAS(cmrNumber,katr6);
            var resultWTAAS_SG = CmrServices.checkWTAAS(cmrNumber,'834');
            if ((resultWTAAS.success && resultWTAAS.data && resultWTAAS.Status &&  resultWTAAS.Status == 'F' && resultWTAAS.Error_Msg != null && resultWTAAS.Error_Msg.toLowerCase().includes("does not exist")) && (resultWTAAS_SG.success && resultWTAAS_SG.data && resultWTAAS_SG.Status &&  resultWTAAS_SG.Status == 'F' && resultWTAAS_SG.Error_Msg != null && resultWTAAS_SG.Error_Msg.toLowerCase().includes("does not exist"))) {
              return new ValidationResult(null, true);
            }else if(!resultWTAAS.success || !resultWTAAS_SG.success){
              return new ValidationResult(null, false, 'Cannot connect to WTAAS at the moment. Please try again after sometime.');
            }else{
              return new ValidationResult(null, false, 'Please enter another CMR Number as entered CMR Number already exists in WTAAS.');
            }
          }else if(FormManager.getActualValue('cmrNoPrefix') != ''){
            return new ValidationResult(null, true);
          }else{
            // FormManager.resetValidations('cmrNoPrefix');
            FormManager.enable('cmrNo');
            FormManager.enable('cmrNoPrefix');
            return new ValidationResult(null, false, 'Please enter either CMR Number or CMR Number Prefix in case of double creates.');
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}


function setAddressDetailsForViewAP() {
  var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  var asean_isa_cntries = [SysLoc.BRUNEI,SysLoc.MALASIA,SysLoc.INDONESIA,SysLoc.SINGAPORE,SysLoc.PHILIPPINES,SysLoc.THAILAND,SysLoc.VIETNAM,SysLoc.INDIA,SysLoc.SRI_LANKA,SysLoc.BANGLADESH,SysLoc.HONG_KONG,SysLoc.MACAO];
  if ( (cmrIssuingCntry == SysLoc.AUSTRALIA || cmrIssuingCntry == SysLoc.NEW_ZEALAND)) {
    $('label[for="city1_view"]').text('Suburb:');    
  }
 if ( asean_isa_cntries.indexOf(cmrIssuingCntry) > -1 ) {
 $('label[for="addrTxt_view"]').text("Address.(Flr,Bldg,lvl,Unit.):");
 $('label[for="addrTxt2_view"]').text("Address. Cont1(Street Name, Street No.):");
 $('label[for="dept_view"]').text("Address. Cont2(District,Town,Region):");

 } 
}
/*
 * Story 1734332 : Customer Name & Customer Name Con't in Address tab need to be
 * set as non-editable for processors
 */
function lockCustMainNames() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if(role == 'REQUESTER' ||  FormManager.getActualValue('viewOnlyPage') == 'true')
    return;
  if (cmr.addressMode == 'updateAddress') {
    FormManager.readOnly('custNm1');
    FormManager.readOnly('custNm2');
  }
  else{
    FormManager.enable('custNm1');
    FormManager.enable('custNm2');
  }
}

function setISUDropDownValues() { 
  var reqType = FormManager.getActualValue('reqType');  
  if (reqType != 'C') {
    return;
  }
  var cluster = FormManager.getActualValue('apCustClusterId');
  var ctc = FormManager.getActualValue('clientTier');
  var isuCd = FormManager.getActualValue('isuCd');
    if (!cluster || !ctc || !isuCd) {
    return;
  }
  var _cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  var _cluster = FormManager.getActualValue('apCustClusterId');
  
  var apClientTierValue = [];
  var isuCdValue = [];
  if (_cluster != '' && ctc != '') {
    var qParams = {
      _qall : 'Y',
      ISSUING_CNTRY : _cmrIssuingCntry,
      CLUSTER : _cluster,
     };
     var results = cmr.query('GET.CTC_ISU_BY_CLUSTER_CNTRY', qParams);
     if (results != null) {
       for (var i = 0; i < results.length; i++) {
         apClientTierValue.push(results[i].ret1);
         isuCdValue.push(results[i].ret2);
       }
       FormManager.limitDropdownValues(FormManager.getField('clientTier'), apClientTierValue);
       FormManager.limitDropdownValues(FormManager.getField('isuCd'), isuCdValue);
       if (apClientTierValue.length == 1) {
         FormManager.setValue('clientTier', apClientTierValue[0]);
         FormManager.setValue('isuCd', isuCdValue[0]);
       }
     }
  }
}

function setCollCdFrSGOnAddrSave(cntry, addressMode, saving, finalSave, force) {
  console.log(">>>> setCollCd for Singapore >>>>");
  var addrType = FormManager.getActualValue('addrType');
  var custSubType = FormManager.getActualValue('custSubGrp');
  if(custSubType != 'SPOFF')
	  return;
  
  if (typeof (_pagemodel) != 'undefined') {
    reqType = FormManager.getActualValue('reqType');
  }
  if (reqType == 'U') {
	  return;
  }
  if ((finalSave || force)  && cmr.addressMode){
    var copyTypes = document.getElementsByName('copyTypes');
    var copyingToA = false;
    if (copyTypes != null && copyTypes.length > 0) {
      copyTypes.forEach(function(input, i) {
        if (input.value == 'ZS01' && input.checked) {
          copyingToA = true;
        }
      });
    }
	if (addrType == 'ZS01' || copyingToA)
    setCollCdFrSingapore();
  }  
}

function setCollCdFrSingapore(){
	   var custSubType = FormManager.getActualValue('custSubGrp');
	  if(custSubType != 'SPOFF')
		  return;
		var zs01ReqId = FormManager.getActualValue('reqId');
		var collCd = null;
		var qParams = {
		REQ_ID : zs01ReqId,
		};
		var landCntry = FormManager.getActualValue('landCntry');
		if(landCntry == '' || !landCntry){
		var result = cmr.query('ADDR.GET.LANDCNTRY.BY_REQID', qParams);
		landCntry = result.ret1;
		}
		switch (landCntry){
		case 'BD':
		case 'IN':
		case 'LK':
		case 'MV':
			collCd = 'I001';
			break;
		case 'HK':
		case 'MO':
			collCd = 'S014';
			break;
		case 'TW':
			collCd = 'S015';
			break;
		case 'CN':
			collCd = 'C013';
			break;
		case 'MY':
			collCd = 'M001';
			break;
		case 'PH':
			collCd = 'P001';
			break;
		case 'TH':
			collCd = 'T001';
			break;
		case 'VN':
			collCd = 'V001';
			break;
		case 'ID':
			collCd = 'S001';
			break;
		default :
			collCd = 'S013';
		}
		FormManager.setValue('collectionCd',collCd);
	}

/*
 * Story CMR-1753 : For the condition, "When there is value in both street
 * address and street address con't. " Provide a validation on check request as
 * well as address level to ensure that length of 'street address' is 27
 */
function addAddressLengthValidators() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
          var addrTxt = FormManager.getActualValue('addrTxt');
          var addrTxt2 = FormManager.getActualValue('addrTxt2');
          
          if(addrTxt != '' && addrTxt2 != ''){
            if(addrTxt.length > 27){
              return new ValidationResult(null, false, 'Street address should not exceed 27 characters when both Street and Street Con\'t present.');
            }else{
              return new ValidationResult(null, true);
            }
          }else{
            return new ValidationResult(null, true);
          }
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function addStreetValidationChkReq() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqId = FormManager.getActualValue('reqId');
        var qParams = {
          _qall : 'Y',
          REQID : reqId
        };

        var results = cmr.query('GET_STREET_LENGTH_AU', qParams);
        if (results != null) {
          for ( var i = 0; i < results.length; i++) {
            if (results[i].ret1 != '' && results[i].ret2 != '' && results[i].ret1.length > 27) {
              return new ValidationResult(null, false, 'Street address should not exceed 27 characters when both Street and Street Con\'t present.');
            }
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function validateStreetAddrCont2(){
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var streetAddrCont1= FormManager.getActualValue('addrTxt2');
        var cmrCntry = FormManager.getActualValue('cmrIssuingCntry');
        var streetAddrCont2= "";
        if(cmrCntry == '738' || cmrCntry =='736'){
          streetAddrCont2= FormManager.getActualValue('city1');
        }else {
          streetAddrCont2= FormManager.getActualValue('dept');
        }
        if(streetAddrCont1 == '' && streetAddrCont2!=''){
          return new ValidationResult(null, false, 'Street Address Con\'t2 cannot have a value as Street Address Con\'t1 is blank.');
        }
        
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

dojo.addOnLoad(function() {
  GEOHandler.AP = [ SysLoc.AUSTRALIA, SysLoc.BANGLADESH, SysLoc.BRUNEI, SysLoc.MYANMAR, SysLoc.SRI_LANKA, SysLoc.INDIA, SysLoc.INDONESIA, SysLoc.PHILIPPINES, SysLoc.SINGAPORE, SysLoc.VIETNAM,
      SysLoc.THAILAND, SysLoc.HONG_KONG, SysLoc.NEW_ZEALAND, SysLoc.LAOS, SysLoc.MACAO, SysLoc.MALASIA, SysLoc.NEPAL, SysLoc.CAMBODIA ];
  GEOHandler.ISA = [ SysLoc.INDIA, SysLoc.SRI_LANKA, SysLoc.BANGLADESH, SysLoc.NEPAL ];
  GEOHandler.ASEAN = [ SysLoc.BRUNEI, SysLoc.MYANMAR, SysLoc.INDONESIA, SysLoc.PHILIPPINES, SysLoc.SINGAPORE, SysLoc.VIETNAM, SysLoc.THAILAND, SysLoc.LAOS, SysLoc.MALASIA, SysLoc.CAMBODIA ];
  GEOHandler.ANZ = [ SysLoc.AUSTRALIA, SysLoc.NEW_ZEALAND ];
  GEOHandler.GCG = [ SysLoc.HONG_KONG, SysLoc.MACAO ];

  console.log('adding AP functions...');
  console.log('the value of person full id is ' + localStorage.getItem("pID"));
  GEOHandler.setRevertIsicBehavior(false);
  GEOHandler.registerValidator(addSalesRepNameNoCntryValidator, [ SysLoc.AUSTRALIA,SysLoc.INDONESIA, SysLoc.PHILIPPINES, SysLoc.SINGAPORE,
      SysLoc.VIETNAM, SysLoc.THAILAND, SysLoc.HONG_KONG, SysLoc.NEW_ZEALAND, SysLoc.MACAO, SysLoc.MALASIA, SysLoc.NEPAL ]);
  GEOHandler.addAfterTemplateLoad(addSalesRepNameNoCntryValidatorBluepages, [ SysLoc.BRUNEI, SysLoc.MYANMAR, SysLoc.LAOS, SysLoc.CAMBODIA ]);

  // 1558942: Update Address Details of different address types at one time
  GEOHandler.enableCopyAddress(GEOHandler.AP);

  GEOHandler.addAfterConfig(addAfterConfigAP, GEOHandler.AP);
  GEOHandler.addAfterTemplateLoad(addAfterConfigAP, GEOHandler.AP);
  GEOHandler.addAfterConfig(updateIndustryClass, GEOHandler.AP);
  GEOHandler.addAfterConfig(updateProvCd, GEOHandler.AP);
  GEOHandler.addAfterConfig(updateRegionCd, GEOHandler.AP);
  GEOHandler.addAfterConfig(setCollectionCd, GEOHandler.AP,[SysLoc.AUSTRALIA,SysLoc.INDIA]);
  GEOHandler.addAfterConfig(setCollCdFrAU, [SysLoc.AUSTRALIA]);
  GEOHandler.addAfterConfig(setCollCdFrIndia, [SysLoc.INDIA]);
  GEOHandler.addAfterConfig(onSubIndustryChange, GEOHandler.AP);
  GEOHandler.addAfterConfig(onIsuCdChangeAseanAnzIsa, GEOHandler.ASEAN);
  GEOHandler.addAfterConfig(onIsuCdChangeAseanAnzIsa, GEOHandler.ANZ);
  GEOHandler.addAfterConfig(onIsuCdChangeAseanAnzIsa, GEOHandler.ISA);
  GEOHandler.enableCustomerNamesOnAddress(GEOHandler.AP);
  GEOHandler.addAddrFunction(updateMainCustomerNames, GEOHandler.AP);
  GEOHandler.addAddrFunction(setAbbrevNmLocnOnAddressSave, GEOHandler.AP);
  
 // GEOHandler.addAddrFunction(addMandateCmrNoForSG, [ SysLoc.SINGAPORE ]);
  
  GEOHandler.addAfterConfig(onCustSubGrpChange, GEOHandler.AP);
  GEOHandler.addAfterConfig(setCTCIsuByCluster, GEOHandler.AP);
  GEOHandler.addAfterTemplateLoad(setISUDropDownValues, GEOHandler.AP);
// GEOHandler.addAfterConfig(setIsuByClusterCTC, GEOHandler.AP);
  GEOHandler.registerValidator(addAbnValidatorForAU, [SysLoc.AUSTRALIA]);
  
 // GEOHandler.registerValidator(addMandateCmrNoForSG, [SysLoc.SINGAPORE]);
  GEOHandler.registerValidator(addCmrNoValidator, [SysLoc.SINGAPORE]);
  
  GEOHandler.addAfterConfig(removeStateValidatorForHkMoNZ, [SysLoc.AUSTRALIA,SysLoc.NEW_ZEALAND]);
  GEOHandler.addAfterTemplateLoad(removeStateValidatorForHkMoNZ, [SysLoc.AUSTRALIA,SysLoc.NEW_ZEALAND]);
  
  
  // GEOHandler.addAfterConfig(addValidatorForSingapore, [SysLoc.SINGAPORE]);
  // GEOHandler.addAfterTemplateLoad(addValidatorForSingapore,
  // [SysLoc.SINGAPORE]);

  // setting collection code for ASEAN
  GEOHandler.addAfterConfig(onISBUCdChange, GEOHandler.ASEAN);
  
  GEOHandler.addAfterConfig(addGovIndcHanlder, [SysLoc.AUSTRALIA]);
  GEOHandler.addAfterConfig(addGovCustTypHanlder, [SysLoc.AUSTRALIA]);
  GEOHandler.addAfterConfig(onInacTypeChange,[SysLoc.AUSTRALIA]);

  // ERO specific
  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.GCG);
  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.ASEAN);
  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.ISA);
  
  
  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.ANZ);
  GEOHandler.registerValidator(addDPLCheckValidator, GEOHandler.ANZ, GEOHandler.ROLE_REQUESTER, true);
  GEOHandler.registerValidator(addDPLCheckValidator, GEOHandler.GCG, GEOHandler.ROLE_REQUESTER, true);
  GEOHandler.registerValidator(addDPLCheckValidator, GEOHandler.ASEAN, GEOHandler.ROLE_REQUESTER, true);
  GEOHandler.registerValidator(addDPLCheckValidator, GEOHandler.ISA, GEOHandler.ROLE_REQUESTER, true);
  
  GEOHandler.addAfterConfig(defaultCMRNumberPrefix, GEOHandler.GCG);
  GEOHandler.addAfterTemplateLoad(defaultCMRNumberPrefix, GEOHandler.GCG);
  GEOHandler.addAfterConfig(setISBUforBPscenario, GEOHandler.ASEAN);
  GEOHandler.addAfterTemplateLoad(setISBUforBPscenario, GEOHandler.ASEAN);
  
  // checklist
  GEOHandler.registerValidator(addChecklistValidator, [ SysLoc.VIETNAM, SysLoc.LAOS, SysLoc.CAMBODIA, SysLoc.HONG_KONG, SysLoc.SINGAPORE, SysLoc.MACAO, SysLoc.MYANMAR ]);
  GEOHandler.addAfterConfig(initChecklistMainAddress, [ SysLoc.VIETNAM, SysLoc.LAOS, SysLoc.CAMBODIA, SysLoc.HONG_KONG, SysLoc.SINGAPORE, SysLoc.MACAO, SysLoc.MYANMAR ]);
 // Address validations
  GEOHandler.registerValidator(addSoltToAddressValidator, GEOHandler.AP);
  GEOHandler.registerValidator(addAddressInstancesValidator, GEOHandler.AP, null, true);
  GEOHandler.registerValidator(addContactInfoValidator, GEOHandler.AP, GEOHandler.REQUESTER,true);
  GEOHandler.registerValidator(similarAddrCheckValidator, GEOHandler.AP, null, true);

  GEOHandler.registerValidator(addAttachmentValidator, GEOHandler.ISA, GEOHandler.REQUESTER, false, false);
  // double creates
  GEOHandler.addAfterConfig(setFieldsForDoubleCreates, [ SysLoc.INDIA, SysLoc.BANGLADESH, SysLoc.SRI_LANKA, SysLoc.VIETNAM, SysLoc.CAMBODIA ]);
  GEOHandler.addAfterTemplateLoad(setFieldsForDoubleCreates, [ SysLoc.INDIA, SysLoc.BANGLADESH, SysLoc.SRI_LANKA, SysLoc.VIETNAM, SysLoc.CAMBODIA ]);
  GEOHandler.registerValidator(addDoubleCreateValidator, [ SysLoc.INDIA, SysLoc.BANGLADESH, SysLoc.SRI_LANKA, SysLoc.VIETNAM, SysLoc.CAMBODIA], null, true);
  GEOHandler.registerValidator(addDoubleCreateValidatorSG, [ SysLoc.SINGAPORE ], null, true);
  // GEOHandler.addAfterConfig(setAddressDetailsForView, GEOHandler.AP);
  
  GEOHandler.registerValidator(addFormatFieldValidator, GEOHandler.AP, null, true);
  
  GEOHandler.registerValidator(addFieldFormatValidator, GEOHandler.AP, null, true);
  
  
  GEOHandler.registerValidator(addFormatForCMRNumValidator, [ SysLoc.SINGAPORE ], null, true);
  
  
  GEOHandler.addAddrFunction(lockCustMainNames, GEOHandler.AP);
  // Story - 1781935 -> AR Code for Singapore
  GEOHandler.addAddrFunction(setCollCdFrSGOnAddrSave, [SysLoc.SINGAPORE]);
  GEOHandler.addAfterTemplateLoad(setCollCdFrSingapore, [SysLoc.SINGAPORE]);
  GEOHandler.registerValidator(addAddressLengthValidators, [ SysLoc.AUSTRALIA ], null, true);
  GEOHandler.registerValidator(addStreetValidationChkReq, [ SysLoc.AUSTRALIA ], null, true);
  GEOHandler.registerValidator(validateStreetAddrCont2,  [ SysLoc.BANGLADESH, SysLoc.BRUNEI, SysLoc.MYANMAR, SysLoc.SRI_LANKA, SysLoc.INDIA, SysLoc.INDONESIA, SysLoc.PHILIPPINES, SysLoc.SINGAPORE, SysLoc.VIETNAM,
                                                                  SysLoc.THAILAND, SysLoc.HONG_KONG, SysLoc.LAOS, SysLoc.MACAO, SysLoc.MALASIA, SysLoc.NEPAL, SysLoc.CAMBODIA ], null, true);

});
