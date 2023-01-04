/* Register AP Javascripts */
var _isicHandlerAP = null;
var _clusterHandlerAP = null;
var _isicHandlerGCG = null;
var _clusterHandlerGCG = null;
var _vatExemptHandler = null;
var _vatExemptHandlerNZ = null;
var _bpRelTypeHandlerGCG = null;
var  _isuHandler = null;
var _clusterHandlerANZ = null;
var _inacCdHandlerIN = null;
var _importIndIN = null;
var _vatRegisterHandlerSG = null;

function addHandlersForAP() {
  if (_isicHandlerAP == null) {
    _isicHandlerAP = dojo.connect(FormManager.getField('isicCd'), 'onChange', function(value) {
      setIsuOnIsic();
    });
  }
  if (_inacCdHandlerIN == null) {
    _inacCdHandlerIN = dojo.connect(FormManager.getField('inacCd'), 'onChange', function(value) {
      lockInacTypeForIGF();
    });
  }
  if (_isuHandler == null) {
    _isuHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
      setCtcOnIsuCdChangeASEAN();
      setCtcOnIsuCdChangeANZ(value);
    });
  }
  
  if (_clusterHandlerAP == null && FormManager.getActualValue('reqType') != 'U') {
    _clusterHandlerAP = dojo.connect(FormManager.getField('apCustClusterId'), 'onChange', function(value) {
      setInacByCluster();
      setIsuOnIsic();
    });
  }
  if (_vatRegisterHandlerSG == null) {
    _vatRegisterHandlerSG = dojo.connect(FormManager.getField('taxCd1'), 'onChange', function(value) {
    cmr
    .showAlert(
        '<div align="center"><strong>VAT Registration Status validation </strong></div> <br/> Please note: <br/> <ul style="list-style-type:circle"> <li>You have to make sure the selection(Yes/No) of “VAT Registration Status” is correct for the Thailand VAT# you have filled. This is specific to the moment you submit this request.<br/>The status can be validated via VES Thailand: <a href="https://eservice.rd.go.th/rd-ves-web/search/vat" target="_blank" rel="noopener noreferrer"> https://eservice.rd.go.th/rd-ves-web/search/vat </a> </li><br/> <li> By selecting ‘No – VAT unapplicable’, you are confirming that this customer has no VAT# then “VAT Registration Status” is not applicable for the same.</li> </ul>', 'VAT Registration Status validation', 'vatRegistrationForSG()','VatRegistrationStatus' , {
          OK : 'I confirm',
        });
        });
        }
          handleObseleteExpiredDataForUpdate();
    }


function addHandlersForANZ() {
  if (_clusterHandlerANZ == null && FormManager.getActualValue('reqType') != 'U') {
    _clusterHandlerANZ = dojo.connect(FormManager.getField('apCustClusterId'), 'onChange', function(value) {
      lockFieldsForAU();
    });
  }
}

function addHandlersForISA() {
  if (_isuHandler == null) {
    _isuHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
      setCtcOnIsuCdChangeISA();
    });
  }
}

function addHandlersForGCG() {
  if (_isicHandlerGCG == null) {
    _isicHandlerGCG = dojo.connect(FormManager.getField('isicCd'), 'onChange', function(value) {
      setIsuOnIsic();
    });
  }
  
  if (_clusterHandlerGCG == null && FormManager.getActualValue('reqType') != 'U') {
    _clusterHandlerGCG = dojo.connect(FormManager.getField('apCustClusterId'), 'onChange', function(value) {
      setIsuOnIsic();
      setInacByClusterHKMO();
    });
  }
  
  if (_bpRelTypeHandlerGCG == null && FormManager.getActualValue('reqType') != 'U') {
    _bpRelTypeHandlerGCG = dojo.connect(FormManager.getField('bpRelType'), 'onChange', function(value) {
      setAbbrvNameBPScen();
    });
  }

  if (_isuHandler == null) {
    _isuHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
      setCtcOnIsuCdChangeGCG();
    });
  }

}

function afterConfigForIndia() { 
  console.log("----After config. for India----");
  if (_vatExemptHandler == null) {
    _vatExemptHandler = dojo.connect(FormManager.getField('vatExempt'), 'onClick', function(value) {
      console.log(">>> RUNNING!!!!");
      var custSubGrp = FormManager.getActualValue('custSubGrp');
      FormManager.resetValidations('vat');
      if (dijit.byId('vatExempt').get('checked')) {
        console.log(">>> Process gstExempt remove * >> ");
        FormManager.readOnly('vat');
        FormManager.setValue('vat', '');
      } else {      
        console.log(">>> Process gstExempt add * >> ");
        FormManager.enable('vat');
        if(!(custSubGrp == 'BLUMX' || custSubGrp == 'MKTPC' || custSubGrp == 'AQSTN' || custSubGrp == 'PRIV')){
          FormManager.addValidator('vat', Validators.REQUIRED, [ 'GST#' ], 'MAIN_CUST_TAB');
        }
        }
    });
  }
  
  // CREATCMR-7005
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if(custSubGrp == 'CROSS'){
    FormManager.readOnly('vat');
  }
}

function resetGstExempt() {
  if (dijit.byId('vatExempt') != undefined && dijit.byId('vatExempt').get('checked')) {
    console.log(">>> Process gstExempt remove * >> ");
    FormManager.resetValidations('vat');
    FormManager.readOnly('vat');
  }
}

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

  if (cntry == '834' && reqType == 'C' && role == 'REQUESTER' && custType == 'CROSS' && custSubGrp == 'SPOFF') {
    FormManager.addValidator('cmrNo', Validators.REQUIRED, [ 'CMR Number' ], 'MAIN_IBM_TAB');
  } else {
    FormManager.removeValidator('cmrNo', Validators.REQUIRED);
  }

 if (cntry == '616' && reqType == 'C' && (role == 'PROCESSOR' || role == 'REQUESTER') && (custSubGrp == 'INTER' || custSubGrp == 'XINT')) {
    FormManager.addValidator('cmrNoPrefix', Validators.REQUIRED, [ 'CmrNoPrefix' ], 'MAIN_IBM_TAB');
    }
    
  if (FormManager.getActualValue('viewOnlyPage') == 'true')
    FormManager.readOnly('repTeamMemberName');
    FormManager.readOnly('isbuCd');

  if (role == 'REQUESTER' || role == 'VIEWER') {
    FormManager.readOnly('mrcCd');
    FormManager.readOnly('isbuCd');
    if (role == 'VIEWER') {
      FormManager.readOnly('abbrevNm');
      FormManager.readOnly('clientTier');
    }
    FormManager.readOnly('sectorCd');
    FormManager.readOnly('abbrevLocn');
    FormManager.readOnly('territoryCd');
    FormManager.readOnly('IndustryClass');
    FormManager.readOnly('subIndustryCd');
  } else {
    FormManager.enable('mrcCd');
    FormManager.enable('isbuCd');
    FormManager.enable('abbrevNm');
    FormManager.enable('sectorCd');
    FormManager.enable('abbrevLocn');
    FormManager.enable('territoryCd');
    FormManager.enable('IndustryClass');
    FormManager.enable('subIndustryCd');
  }
  
  if (reqType == 'C' && custSubGrp == 'ECOSY' && (cntry == '738' || cntry == '736')) {
    FormManager.readOnly('apCustClusterId');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('mrcCd');
    FormManager.setValue('clientTier', 'Y');
    FormManager.setValue('isuCd', '34');
    FormManager.setValue('mrcCd', '3');
    if (cntry == '738'){
      FormManager.setValue('apCustClusterId', '08041');
      FormManager.removeValidator('isuCd', Validators.REQUIRED);
    }
    if (cntry == '736'){
      FormManager.setValue('apCustClusterId', '08045');
    }
  }

 if (reqType == 'C' && (custSubGrp && custSubGrp != '') && custSubGrp != 'INTER' && custSubGrp != 'XINT' && (cntry == '738' || cntry == '736')) {
    FormManager.setValue('repTeamMemberNo', _pagemodel.repTeamMemberNo == null ? '000000' : _pagemodel.repTeamMemberNo);
    FormManager.enable('repTeamMemberNo');
  }
  
  if (role != 'PROCESSOR' && (cntry == '643' || cntry == '749' || cntry == '778' || cntry == '818' || cntry == '834' || cntry == '852' || cntry == '856')) {
    FormManager.readOnly('miscBillCd');
  }

  if (reqType == 'C' && custType == 'LOCAL' && (cntry == '738' || cntry == '736')) {
    FormManager.readOnly('postCd');
  } else {
    if (reqType != 'C' && custType != 'LOCAL' && (cntry == '738' || cntry == '736')) {
      FormManager.enable('postCd');
    }
  }

  if (reqType == 'U' && (cntry == '738' || cntry == '736')) {
    FormManager.readOnly('postCd');
  }

  if (reqType == 'U' && cntry == '834') {
	    FormManager.readOnly('isicCd');
	  }

  if (reqType == 'C' && custGrp == 'CROSS' && cntry == '736') {
    FormManager.enable('postCd');
  }

  if (reqType == 'C' && custGrp == 'CROSS' && custSubGrp == 'SPOFF' && cntry == '834') {
    FormManager.enable('cmrNo');
  } else {
    FormManager.readOnly('cmrNo');
  }
  
  var aseanCntries = ['852', '818', '856', '643', '778', '749', '834'];
  if ((role == 'PROCESSOR' || role == 'VIEWER') && (custSubGrp.includes('DUM') || custSubGrp.includes('INT')) && aseanCntries.includes(cntry)) {
    FormManager.readOnly('mrcCd');
  }
  // CREATCMR-788
  if(cntry == '738' || cntry == '736'){
    addressQuotationValidatorGCG();
  }
  var streetAddressCont1 = FormManager.getActualValue('addrTxt2');
  if ((cntry == '738' || cntry == '736') && (streetAddressCont1 == '' || streetAddressCont1 == null)) {
    return new ValidationResult({
      id : 'addrTxt2',
      type : 'text',
      name : 'addrTxt2'
    }, false, 'Street Address Con' + "'" + 't1 is required ');
  }

  if (role == 'REQUESTER' && reqType == 'C') {
 /*
   * if (cntry == SysLoc.NEW_ZEALAND) { if (custSubGrp == 'INTER' || custSubGrp ==
   * 'XINT' || custSubGrp == 'DUMMY' || custSubGrp == 'XDUMM' || custSubGrp ==
   * 'BLUMX' || custSubGrp == 'XBLUM' || custSubGrp == 'MKTPC' || custSubGrp ==
   * 'XMKTP') FormManager.readOnly('clientTier'); else
   * FormManager.enable('clientTier'); }
   */
    if((cntry == SysLoc.NEW_ZEALAND || cntry == SysLoc.AUSTRALIA ||cntry == SysLoc.INDONESIA || cntry == SysLoc.PHILIPPINES ||cntry == SysLoc.SINGAPORE ||cntry == SysLoc.VIETNAM || cntry == SysLoc.THAILAND || cntry == SysLoc.MALASIA) && (custSubGrp == 'ECSYS' ||custSubGrp == 'XECO' )){
        FormManager.setValue('mrcCd', '3');
        FormManager.setValue('clientTier', 'Y');
        FormManager.readOnly('clientTier');
        FormManager.setValue('isuCd', '34');
        FormManager.readOnly('isuCd');
    }
  }
  if (reqType == 'C') {
    setIsuOnIsic();
    onInacTypeChange();
    setInacByCluster();
  }
  if (cntry == '834') {
    // FormManager.removeValidator('clientTier', Validators.REQUIRED);
    addVatValidationforSingapore();
  }
  if (cntry != SysLoc.HONG_KONG && cntry !=  SysLoc.MACAO && reqType == 'U') {
    handleObseleteExpiredDataForUpdate();
  }
  if (cntry == '616' && reqType == 'U' && (role == 'PROCESSOR' || role == 'REQUESTER')) {
    FormManager.readOnly('isicCd');
    FormManager.readOnly('subIndustryCd');
    FormManager.readOnly('apCustClusterId');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('mrcCd');
    FormManager.readOnly('inacType');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('inacCd');
  }
  // CREATCMR-788
  addressQuotationValidatorAP();
  
  if (cntry == '796' && reqType == 'C') { 
    setLockIsicNZfromDNB();
    // CREATCMR-7656
    setDefaultValueforCustomerServiceCode();
    // setLockStatusforSalesReqNo();
    // removeSalesReqNoValidation();
    
    // CREATCMR-7653
    setDefaultValueForNZCreate();
    setLockIsicNZfromDNB();
    
  }
  
  //CREATCMR-7929_NZ_LockISIC4Update
  if(cntry == '796' && reqType == 'U'){
    console.log(">>> NZ-796-U >>> Lock ISIC For UPDATE.");
    FormManager.readOnly('isicCd');
  }
}

function setInacByCluster() {
	console.log(">>>> setInacByCluster >>>>");
    var _cluster = FormManager.getActualValue('apCustClusterId');
    var cntry = FormManager.getActualValue('cmrIssuingCntry');
    if (cntry == '736' || cntry == '738') {
      return;
    }
    if (!_cluster) {
      return;
    }
    if (_cluster.includes('BLAN') || _cluster == '05224' || _cluster == '05225' || _cluster == '09062'  || _cluster == '09063'  || _cluster == '09064'  || _cluster == '04477' || _cluster == '04490' || _cluster == '04496' || _cluster == '04467' || _cluster == '04494' || _cluster == '04691' || _cluster == '00035' || _cluster == '00127' || _cluster == '00105' || _cluster == '04470' || _cluster == '04469' || _cluster == '04471' || _cluster == '04485' || _cluster == '04500' || _cluster == '04746' || _cluster == '04744' || _cluster == '04745' || _cluster == '04694'|| 
        ((_cluster.includes('04492') || _cluster.includes('04503') || _cluster.includes('04692') || _cluster.includes('04481') || _cluster.includes('04462') || _cluster.includes('04483') || _cluster.includes('00149') ) && (cntry == '856' || cntry == '834')) || (cntry == '818' && _cluster == '09054') || (cntry == '778' && _cluster == '09051') || (cntry == '856' && (['05220', '09053'].includes(_cluster))) || (cntry == '749' && _cluster == '09050') || (cntry == '834' && (_cluster == '09052' || _cluster == '05219')) || (cntry == '852' && _cluster == '09055') || 
        (cntry == '616' && (_cluster == '09057' || _cluster == '05222' || _cluster == '05221')) || (cntry == '796' && _cluster == '09056')) {
    
      FormManager.addValidator('inacCd', Validators.REQUIRED, [ 'INAC/NAC Code' ], 'MAIN_IBM_TAB');
      FormManager.addValidator('inacType', Validators.REQUIRED, [ 'INAC Type' ], 'MAIN_IBM_TAB');
      FormManager.setValue('mrcCd', '2');
      if( ((_cluster == '09062'  || _cluster == '09063'  || _cluster == '09064') && (cntry == '744' || cntry == '615' || cntry == '652')) ||
       (cntry == '818' && _cluster == '09054') || (cntry == '778' && _cluster == '09051') || (cntry == '856' && _cluster == '09053') || (cntry == '749' && _cluster == '09050') || (cntry == '834' && _cluster == '09052') || (cntry == '852' && _cluster == '09055') || (cntry == '616' && _cluster == '09057') || (cntry == '796' && _cluster == '09056')){
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
          var value = FormManager.getField('inacType');
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
      }
    } else {
      FormManager.removeValidator('inacCd', Validators.REQUIRED);
      FormManager.removeValidator('inacType', Validators.REQUIRED);
      FormManager.resetDropdownValues(FormManager.getField('inacCd'));
      FormManager.resetDropdownValues(FormManager.getField('inacType'));
      updateMRCAseanAnzIsa();
      return;
    }
}

function setInacByClusterHKMO() {
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var _cluster = FormManager.getActualValue('apCustClusterId');
  if (FormManager.getActualValue('reqType') != 'C' || FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (!_cluster) {
    return;
  }
  if (_cluster == '04501' || _cluster == '04683' || _cluster == '04690'|| _cluster == '09060'|| _cluster == '09059') {
    FormManager.addValidator('inacCd', Validators.REQUIRED, [ 'INAC/NAC Code' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('inacType', Validators.REQUIRED, [ 'INAC Type' ], 'MAIN_IBM_TAB');
    if (_cluster == '04501' || _cluster == '04683' || _cluster == '04690') {
      FormManager.setValue('mrcCd', '2');
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
    var custSubGrp = FormManager.getActualValue('custSubGrp');
    if (custSubGrp =='BUSPR' || custSubGrp =='XBUSP' || custSubGrp =='INTER' || custSubGrp =='DUMMY' || custSubGrp =='XDUMM' || custSubGrp =='XINT') {
      FormManager.setValue('mrcCd', '2');
      if (custSubGrp =='DUMMY' || custSubGrp =='XDUMM') {
      FormManager.removeValidator('cmrNoPrefix', Validators.REQUIRED);
      }
    } else {
      FormManager.setValue('mrcCd', '3');
    }
    return;
  }
}

/* ASEAN ANZ GCG ISIC MAPPING */
function setIsuOnIsic(){

  var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  if (FormManager.getActualValue('reqType') != 'C' || (FormManager.getActualValue('viewOnlyPage') == 'true' && !(cmrIssuingCntry == '738'))) {
    return;
  }
  
  var _cluster = FormManager.getActualValue('apCustClusterId');
  var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  var aseanCntries = ['852', '818', '856', '643', '778', '749', '834'];
  if (_cluster != '') {    
    var qParams = {
      _qall : 'Y',
      ISSUING_CNTRY : cmrIssuingCntry,
      CLUSTER : _cluster
    };
  
    var clusterDesc = cmr.query('GET.DESC_BY_CLUSTER', qParams);
    if((( cmrIssuingCntry == '796' ) &&(clusterDesc[0] != '' && (clusterDesc[0].ret1.includes('S2') || clusterDesc[0].ret1.includes('Default') || 
        clusterDesc[0].ret1.includes('EC') || clusterDesc[0].ret1.includes('Kyndryl')))) || (cmrIssuingCntry == '796' && (clusterDesc[0].ret2.includes('08037')))) {
      FormManager.setValue('repTeamMemberNo', '000000');
      FormManager.readOnly('repTeamMemberNo');
    } 
    if((cmrIssuingCntry == '738' || cmrIssuingCntry == '736' ) &&(clusterDesc[0] != '' && !(clusterDesc[0].ret1.includes('S1')))){
      return;
    } else if (cmrIssuingCntry == '856' && !(_cluster.includes('04483') || _cluster.includes('05220'))) {
      return;
    } else if (cmrIssuingCntry == '834' && !(_cluster.includes('04462') || _cluster.includes('05219'))) {
      return;
    }  else if ((cmrIssuingCntry == '738' || cmrIssuingCntry == '736' || cmrIssuingCntry == '616' || cmrIssuingCntry == '796' || aseanCntries.includes(cmrIssuingCntry) ) && (clusterDesc[0] != '' && !clusterDesc[0].ret1.includes('S&S'))) {
      return;
    } else if (!(cmrIssuingCntry == '738' || cmrIssuingCntry == '736' || cmrIssuingCntry == '616' || cmrIssuingCntry == '796' || aseanCntries.includes(cmrIssuingCntry) ) && (clusterDesc[0] != '' && !clusterDesc[0].ret1.includes('S&S'))) {
      return;
    } 
  }
  
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
        if (reqType == 'C' && role == 'PROCESSOR' && custGrp == 'CROSS' && custSubGrp == 'SPOFF') {
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
  FormManager.addFormValidator(
      (function() {
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
              }, false, 'Only the characters listed are allowed to input due to system limitation.' + '   (1) A to Z (uppercase & lowercase)  ' + ' (2) 0 to 9   ' + " (3) '   " + ' (4) ,   '
                  + ' (5) :   ' + ' (6) &   ' + '  (7) (   ' + ' (8) ;   ' + ' (9) )   ' + ' (10) -   ' + ' (11)  #   ' + ' (12) .  ' + ' (13) /   ');
            }

            if (custNm1 && custNm1.length > 0
                && !custNm1.match("([\\/]+$)|^([\\.]+$)|^([-]+$)|^([#]+$)|^([,]+$)|^([:]+$)|^([']+$)|^([&]+$)|^([;]+$)|^([0-9A-Za-z-#;:,'&()\\s+\\.\\/]+$)|^[(]+$|^[)]+$")) {
              return new ValidationResult({
                id : 'custNm1',
                type : 'text',
                name : 'custNm1'
              }, false, 'Only the characters listed are allowed to input due to system limitation.' + '   (1) A to Z (uppercase & lowercase)  ' + ' (2) 0 to 9   ' + " (3) '   " + ' (4) ,   '
                  + ' (5) :   ' + ' (6) &   ' + '  (7) (   ' + ' (8) ;   ' + ' (9) )   ' + ' (10) -   ' + ' (11)  #   ' + ' (12) .  ' + ' (13) /  ');
            }

            if (custNm2 && custNm2.length > 0
                && !custNm2.match("([\\/]+$)|^([\\.]+$)|^([-]+$)|^([#]+$)|^([,]+$)|^([:]+$)|^([']+$)|^([&]+$)|^([;]+$)|^([0-9A-Za-z-#;:,'&()\\s+\\.\\/]+$)|^[(]+$|^[)]+$")) {
              return new ValidationResult({
                id : 'custNm2',
                type : 'text',
                name : 'custNm2'
              }, false, 'Only the characters listed are allowed to input due to system limitation.' + '   (1) A to Z (uppercase & lowercase)  ' + ' (2) 0 to 9   ' + " (3) '   " + ' (4) ,   '
                  + ' (5) :   ' + ' (6) &   ' + '  (7) (   ' + ' (8) ;   ' + ' (9) )   ' + ' (10) -   ' + ' (11)  #   ' + ' (12) .  ' + ' (13) /  ');
            }

            if (addrTxt && addrTxt.length > 0
                && !addrTxt.match("([\\/]+$)|^([\\.]+$)|^([-]+$)|^([#]+$)|^([,]+$)|^([:]+$)|^([']+$)|^([&]+$)|^([;]+$)|^([0-9A-Za-z-#;:,'&()\\s+\\.\\/]+$)|^[(]+$|^[)]+$")) {
              return new ValidationResult({
                id : 'addrTxt',
                type : 'text',
                name : 'addrTxt'
              }, false, 'Only the characters listed are allowed to input due to system limitation.' + '   (1) A to Z (uppercase & lowercase)  ' + ' (2) 0 to 9   ' + " (3) '   " + ' (4) ,   '
                  + ' (5) :   ' + ' (6) &   ' + '  (7) (   ' + ' (8) ;   ' + ' (9) )   ' + ' (10) -   ' + ' (11)  #   ' + ' (12) .  ' + ' (13) /  ');
            }
            if (addrTxt2 && addrTxt2.length > 0
                && !addrTxt2.match("([\\/]+$)|^([\\.]+$)|^([-]+$)|^([#]+$)|^([,]+$)|^([:]+$)|^([']+$)|^([&]+$)|^([;]+$)|^([0-9A-Za-z-#;:,'&()\\s+\\.\\/]+$)|^[(]+$|^[)]+$")) {
              return new ValidationResult({
                id : 'addrTxt2',
                type : 'text',
                name : 'addrTxt2'
              }, false, 'Only the characters listed are allowed to input due to system limitation.' + '   (1) A to Z (uppercase & lowercase)  ' + ' (2) 0 to 9   ' + " (3) '   " + ' (4) ,   '
                  + ' (5) :   ' + ' (6) &   ' + '  (7) (   ' + ' (8) ;   ' + ' (9) )   ' + ' (10) -   ' + ' (11)  #   ' + ' (12) .  ' + ' (13) /  ');
            }
            if (stateProv && stateProv.length > 0
                && !stateProv.match("([\\/]+$)|^([\\.]+$)|^([-]+$)|^([#]+$)|^([,]+$)|^([:]+$)|^([']+$)|^([&]+$)|^([;]+$)|^([0-9A-Za-z-#;:,'&()\\s+\\.\\/]+$)|^[(]+$|^[)]+$")) {
              return new ValidationResult({
                id : 'stateProv',
                type : 'text',
                name : 'stateProv'
              }, false, 'Only the characters listed are allowed to input due to system limitation.' + '   (1) A to Z (uppercase & lowercase)  ' + ' (2) 0 to 9   ' + " (3) '   " + ' (4) ,   '
                  + ' (5) :   ' + ' (6) &   ' + '  (7) (   ' + ' (8) ;   ' + ' (9) )   ' + ' (10) -   ' + ' (11)  #   ' + ' (12) .  ' + ' (13) /  ');
            }
            if (dept && dept.length > 0 && !dept.match("([\\/]+$)|^([\\.]+$)|^([-]+$)|^([#]+$)|^([,]+$)|^([:]+$)|^([']+$)|^([&]+$)|^([;]+$)|^([0-9A-Za-z-#;:,'&()\\s+\\.\\/]+$)|^[(]+$|^[)]+$")) {
              return new ValidationResult({
                id : 'dept',
                type : 'text',
                name : 'dept'
              }, false, 'Only the characters listed are allowed to input due to system limitation.' + '   (1) A to Z (uppercase & lowercase)  ' + ' (2) 0 to 9   ' + " (3) '   " + ' (4) ,   '
                  + ' (5) :   ' + ' (6) &   ' + '  (7) (   ' + ' (8) ;   ' + ' (9) )   ' + ' (10) -   ' + ' (11)  #   ' + ' (12) .  ' + ' (13) /  ');
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

            if ((addrDept != null && addrDept.length > 0 && !addrDept
                .match("([\\/]+$)|^([\\.]+$)|^([-]+$)|^([#]+$)|^([,]+$)|^([:]+$)|^([']+$)|^([&]+$)|^([;]+$)|^([0-9A-Za-z-#;:,'&()\\s+\\.\\/]+$)|^[(]+$|^[)]+$"))
                || (addrCustNm1 != null && addrCustNm1.length > 0 && !addrCustNm1
                    .match("([\\/]+$)|^([\\.]+$)|^([-]+$)|^([#]+$)|^([,]+$)|^([:]+$)|^([']+$)|^([&]+$)|^([;]+$)|^([0-9A-Za-z-#;:,'&()\\s+\\.\\/]+$)|^[(]+$|^[)]+$"))
                || (addrCustNm2 != null && addrCustNm2.length > 0 && !addrCustNm2
                    .match("([\\/]+$)|^([\\.]+$)|^([-]+$)|^([#]+$)|^([,]+$)|^([:]+$)|^([']+$)|^([&]+$)|^([;]+$)|^([0-9A-Za-z-#;:,'&()\\s+\\.\\/]+$)|^[(]+$|^[)]+$"))
                || (addrAddrTxt != null && addrAddrTxt.length > 0 && !addrAddrTxt
                    .match("([\\/]+$)|^([\\.]+$)|^([-]+$)|^([#]+$)|^([,]+$)|^([:]+$)|^([']+$)|^([&]+$)|^([;]+$)|^([0-9A-Za-z-#;:,'&()\\s+\\.\\/]+$)|^[(]+$|^[)]+$"))
                || (addrAddrTxt2 != null && addrAddrTxt2.length > 0 && !addrAddrTxt2
                    .match("([\\/]+$)|^([\\.]+$)|^([-]+$)|^([#]+$)|^([,]+$)|^([:]+$)|^([']+$)|^([&]+$)|^([;]+$)|^([0-9A-Za-z-#;:,'&()\\s+\\.\\/]+$)|^[(]+$|^[)]+$"))
                || (addrCity1 != null && addrCity1.length > 0 && !addrCity1
                    .match("([\\/]+$)|^([\\.]+$)|^([-]+$)|^([#]+$)|^([,]+$)|^([:]+$)|^([']+$)|^([&]+$)|^([;]+$)|^([0-9A-Za-z-#;:,'&()\\s+\\.\\/]+$)|^[(]+$|^[)]+$"))) {
              return new ValidationResult(null, false, 'Only the characters listed are allowed to input due to system limitation.' + ' (1) A to Z (uppercase & lowercase) ' + ' (2) 0 to 9 '
                  + " (3) ' " + ' (4) , ' + ' (5) : ' + ' (6) & ' + ' (7) ( ' + ' (8) ; ' + ' (9) ) ' + ' (10) - ' + ' (11) # ' + ' (12) .  ' + ' (13) / ');
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
        var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
        // var docContent = FormManager.getActualValue('docContent');
        if (typeof (_pagemodel) != 'undefined') {
          if ( reqType == 'C' && ((cmrIssuingCntry == '796' && (custSubType == 'ESOSW' || custSubType == 'XESO')) || (cmrIssuingCntry == '616' && custSubType == 'ESOSW')) || (cmrIssuingCntry == '834' && custSubType == 'ASLOM')) {
            var id = FormManager.getActualValue('reqId');
            var ret = cmr.query('CHECK_ESA_MATCH_ATTACHMENT', {
            ID : id
            });
            if(ret == null || ret.ret1 == null){
              return new ValidationResult(null, false, 'ESA Enrollment Form Attachment tab is required.');
            }
          }
          if (reqType == 'C'
              && (custSubType != 'INTER' && custSubType != 'XINT' && custSubType != 'DUMMY' && custSubType != 'XDUMM' && custSubType != 'BLUMX' && custSubType != 'XBLUM' && custSubType != 'MKTPC'
                  && custSubType != 'XMKTP' && custSubType != 'IGF' && custSubType != 'XIGF')) {
          if(cmrIssuingCntry == '615' || cmrIssuingCntry == '652'){
                // For BL and SL check company proof
            var id = FormManager.getActualValue('reqId');
            var ret = cmr.query('CHECK_DNB_MATCH_ATTACHMENT', {
              ID : id
            });   
            if(ret == null || ret.ret1 == null){
              return new ValidationResult(null, false, 'Company Proof in Attachment tab is required.');
            } else {
              return new ValidationResult(null, true);
            }
           }
          else if(cmrIssuingCntry != '796' && cmrIssuingCntry != '616' && cmrIssuingCntry != '834') {                         
            var id = FormManager.getActualValue('reqId');
            var ret = cmr.query('CHECK_TERRITORY_ATTACHMENT', {
              ID : id
            });
            if (ret == null || ret.ret1 == null) {
              return new ValidationResult(null, false, 'TERRITORY Manager Approval in Attachment tab is required.');
            } else {
              return new ValidationResult(null, true);
            }
          }
         } else {
            return new ValidationResult(null, true);
          }
        }
      }
    };
  })(), 'MAIN_ATTACH_TAB', 'frmCMR');
  }

function setAttachmentOnCluster() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqType = FormManager.getActualValue('reqType');
        var role = FormManager.getActualValue('userRole').toUpperCase();
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        var cluster = FormManager.getActualValue('apCustClusterId');
        if (reqType == 'C' && role == 'REQUESTER' && (custSubGrp =='NRML'|| custSubGrp =='AQSTN'||custSubGrp =='CROSS'||custSubGrp =='ESOSW') && (cluster == '08033'||cluster == '08034'||cluster == '08035')) {
        var id = FormManager.getActualValue('reqId');
        var ret = cmr.query('CHECK_ECSYS_ATTACHMENT', {
              ID : id
            });
        if(ret == null || ret.ret1 == null){
        return new ValidationResult(null, false, 'Ecosystem AND Technology leader Approval in Attachment tab is required.');
         }
        else
        return new ValidationResult(null, true);
        }
        else
        return new ValidationResult(null, true);
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

  if ((role == 'PROCESSOR') && (custSubGrp == 'INTER' || custSubGrp == 'XINT')) {
    FormManager.addValidator('cmrNoPrefix', Validators.REQUIRED, [ 'CmrNoPrefix' ], 'MAIN_IBM_TAB');
  }
  // 2333
  if ((role != 'PROCESSOR' && cmrIssuingCntry != '834') && (custSubGrp == 'INTER' || custSubGrp == 'XINT')) {
    FormManager.hide('CmrNoPrefix', 'cmrNoPrefix');
  } else {
    FormManager.show('CmrNoPrefix', 'cmrNoPrefix');
  }

    // default cmrNoPrefix '994---' for PROCESSOR role
  if (role == 'PROCESSOR' && (custSubGrp == 'INTER' || custSubGrp == 'XINT')) {
    FormManager.setValue('cmrNoPrefix', '994---');
  }

  if (cmrIssuingCntry == '736' && role == 'REQUESTER' && (custSubGrp == 'INT' || custSubGrp == 'XINT')) {
    FormManager.resetValidations('cmrNoPrefix');
  }
  if (cmrIssuingCntry == '744' && (custSubGrp == 'INTER' || custSubGrp == 'XINT')) {
    FormManager.show('CmrNoPrefix', 'cmrNoPrefix');
    FormManager.setValue('cmrNoPrefix', '996---');
    }
}

// 2333
function defaultCMRNumberPrefixforSingapore() {
  if(custSubGrp == 'INTER' && cmrIssuingCntry == '834') {
    FormManager.show('CmrNoPrefix', 'cmrNoPrefix');
  }
}

// CREATCMR-7656
function defaultCMRNumberPrefixforNewZealand(){
  console.log(">>>> defaultCMRNumberPrefixforNewZealand >>>>");
  var reqType = FormManager.getActualValue('reqType');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if(reqType == 'C'){
    if(custSubGrp == 'INTER'){
      FormManager.enable('cmrNoPrefix');
      FormManager.setValue('cmrNoPrefix', _pagemodel.cmrNoPrefix == null ? '990---' : _pagemodel.cmrNoPrefix);
      FormManager.addValidator('cmrNoPrefix', Validators.REQUIRED, [ 'CMR Number Prefix' ], 'MAIN_IBM_TAB');
    }
    else{
      FormManager.setValue('cmrNoPrefix', '');
      FormManager.readOnly('cmrNoPrefix');
      FormManager.removeValidator('cmrNoPrefix', Validators.REQUIRED);
    }
  }
}

// CREATCMR-7656
function setDefaultValueforCustomerServiceCode(){
  console.log(">>>> setDefaultValueforCustomerServiceCode >>>>");
  var reqType = FormManager.getActualValue('reqType');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if(custSubGrp == 'NRML' || custSubGrp == 'INTER' || custSubGrp == 'DUMMY' || custSubGrp == 'AQSTN' || custSubGrp == 'BLUMX'
    || custSubGrp == 'MKTPC' || custSubGrp == 'ECSYS' || custSubGrp == 'ESOSW' || custSubGrp == 'CROSS' || custSubGrp == 'XAQST' || custSubGrp == 'XBLUM' ||
    custSubGrp == 'XMKTP' || custSubGrp == 'XESO' || custSubGrp == 'PRIV'){
    FormManager.setValue('engineeringBo', '9920');
    FormManager.readOnly('engineeringBo');
    FormManager.removeValidator('engineeringBo', Validators.REQUIRED);
  }
}

// CREATCMR-7656
// function setDefaultValueforSalesReqNo(){
// console.log(">>>> setDefaultValueforSalesReqNo >>>>");
// var reqType = FormManager.getActualValue('reqType');
// var cntry = FormManager.getActualValue('cmrIssuingCntry');
// var custSubGrp = FormManager.getActualValue('custSubGrp');
// if(reqType == 'C' && cntry == '796'&& (custSubGrp == 'NRML' || custSubGrp ==
// 'INTER' || custSubGrp == 'DUMMY' || custSubGrp == 'AQSTN' || custSubGrp ==
// 'BLUMX'
// || custSubGrp == 'MKTPC' || custSubGrp == 'ECSYS' || custSubGrp == 'ESOSW' ||
// custSubGrp == 'CROSS' || custSubGrp == 'XAQST' || custSubGrp == 'XBLUM' ||
// custSubGrp == 'XMKTP'
// || custSubGrp == 'XESO')){
// FormManager.setValue('repTeamMemberName', '000000');
// FormManager.readOnly('repTeamMemberName');
// }
// }

// CREATCMR-7656
// function removeSalesReqNoValidation(){
// console.log(">>>> removeSalesReqNoValidation >>>>");
// var reqType = FormManager.getActualValue('reqType');
// var cntry = FormManager.getActualValue('cmrIssuingCntry');
// var custSubGrp = FormManager.getActualValue('custSubGrp');
// if(reqType == 'C' && cntry == '796'&& (custSubGrp == 'NRML' || custSubGrp ==
// 'INTER' || custSubGrp == 'DUMMY' || custSubGrp == 'AQSTN' || custSubGrp ==
// 'BLUMX'
// || custSubGrp == 'MKTPC' || custSubGrp == 'ECSYS' || custSubGrp == 'ESOSW' ||
// custSubGrp == 'CROSS' || custSubGrp == 'XAQST' || custSubGrp == 'XBLUM' ||
// custSubGrp == 'XMKTP'
// || custSubGrp == 'XESO')){
// FormManager.removeValidator('repTeamMemberName', Validators.REQUIRED);
// FormManager.removeValidator('repTeamMemberNo', Validators.REQUIRED)
// }
// }

function setMrc4IntDumForASEAN() {
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  if (custSubGrp == 'INTER' || custSubGrp == 'XINT') {
    FormManager.setValue('mrcCd', '2');
    FormManager.enable('isuCd');
  } else if ((custSubGrp == 'DUMMY' || custSubGrp == 'XDUMM') || (['BLUMX', 'PRIV', 'MKTPC'].includes(custSubGrp) && cntry == '749') || (['BLUMX', 'PRIV', 'MKTPC', 'SPOFF'].includes(custSubGrp) && cntry == '834')) {
    FormManager.setValue('mrcCd', '3');
    FormManager.enable('isuCd');
  }
}

function setISBUforBPscenario() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubGrp = FormManager.getActualValue('custSubGrp');

  if (custSubGrp == 'BUSPR' || custSubGrp == 'XBUSP') {
    FormManager.addValidator('isbuCd', Validators.REQUIRED, [ 'ISBU' ], 'MAIN_IBM_TAB');
  }

  if (role != 'VIEWER' && (custSubGrp == 'BUSPR' || custSubGrp == 'XBUSP')) {
    FormManager.enable('isbuCd');
  } else if (role == 'REQUESTER' || role == 'VIEWER') {
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
    if (FormManager.getActualValue('cmrIssuingCntry') == '796' && FormManager.getActualValue('reqType') == 'C') {    
      setLockIsicNZfromDNB()
    }

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

function setCollectionCd() {
  var cmrIssuCntry = FormManager.getActualValue('cmrIssuingCntry');
  var custGrp = FormManager.getActualValue('custGrp');
  var role = FormManager.getActualValue('userRole').toUpperCase();

  if (cmrIssuCntry == '834' && custGrp == 'CROSS') {
    FormManager.setValue('collectionCd', 'S013');
    if (role == 'REQUESTER')
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
    if (result.length > 0) {
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
  console.log(">>>repTeamMemberNo<<<===" + FormManager.getActualValue('repTeamMemberNo'));
  console.log(">>>repTeamMemberNo---pID<<<===" + localStorage.getItem("pID"));

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

function included(cmrIssuCntry) {
  var excludedCntry = [ "615", "652", "744", "790", "736", "738", "643", "749", "778", "818", "834", "852", "856", "646", "714", "720", "616", "796" ];

  for (var i = 0; i < excludedCntry.length; i++) {
    console.log("---excluded Country is---" + excludedCntry[i]);
    console.log("---cmrIssuCntry Country is---" + cmrIssuCntry);
    if (cmrIssuCntry == excludedCntry[i])
      return false;
    else
      return true;
  }
  ;
}

// Different requirement for countries that don't exist on Bluepages:
// Myanmar,Cambodia,Loas,Brunei
function addSalesRepNameNoCntryValidatorBluepages() {
  console.log(">>>repTeamMemberNo<<<===" + FormManager.getActualValue('repTeamMemberNo'));
  console.log(">>>repTeamMemberNo---pID<<<===" + localStorage.getItem("pID"));
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
          if (cmrIssuCntry == '643' && FormManager.getActualValue('custSubGrp') == 'AQSTN') {
            FormManager.setValue('repTeamMemberNo', '000000');
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

    if (FormManager.getActualValue('cmrIssuingCntry') == '643' && (FormManager.getActualValue('custSubGrp') == 'AQSTN' || FormManager.getActualValue('custSubGrp') == 'XAQST')) {
      FormManager.setValue('repTeamMemberNo', '000000');
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
  if ((finalSave || force) && cmr.addressMode) {
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
      if (cmrCntry == '616') {
        console.log(">>>> set CollectionCode OnAddressSave >>>>");
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
  if (custNm1 == '')
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
      } else if (custSubGrp == "AQSTN") {
        _abbrevNm ="Acquisition Use Only";
      } else {
        if (custNm1)
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
        if (custNm1)
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
        if (custNm1)
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
        _abbrevNm = "IGF INTERNAL_" + custNm1;
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
      if (custSubGrp == "DUMMY" || custSubGrp == "XDUMM") {
        _abbrevNm = "IGF INTERNAL_" + custNm1;
      } else if (custSubGrp == "ASLOM" || custSubGrp == "XASLM") {
        _abbrevNm = "ESA_" + custNm1;
      } else if (custSubGrp == "SOFT" || custSubGrp == "XSOFT") {
        _abbrevNm = "Softlayer_" + custNm1;
      } else {
        if (custNm1)
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
      if (custSubGrp == "DUMMY" || custSubGrp == "XDUMM") {
        _abbrevNm = "IGF INTERNAL_" + custNm1;
      } else if (custSubGrp == "ASLOM" || custSubGrp == "XASLM") {
        _abbrevNm = "ESA_" + custNm1;
      } else if (custSubGrp == "SOFT" || custSubGrp == "XSOFT") {
        _abbrevNm = "Softlayer_" + custNm1;
      } else {
        if (custNm1)
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
      if (custSubGrp == "DUMMY" || custSubGrp == "XDUMM") {
        _abbrevNm = "IGF INTERNAL_" + custNm1;
      } else if (custSubGrp == "ASLOM" || custSubGrp == "XASLM") {
        _abbrevNm = "ESA_" + custNm1;
      } else if (custSubGrp == "SOFT" || custSubGrp == "XSOFT") {
        _abbrevNm = "Softlayer_" + custNm1;
      } else {
        if (custNm1)
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
      if (custSubGrp == "DUMMY" || custSubGrp == "XDUMM") {
        _abbrevNm = "IGF INTERNAL_" + custNm1;
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
        _abbrevNm = "IGF INTERNAL_" + custNm1;
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
      if (custSubGrp == "DUMMY" || custSubGrp == "XDUMM") {
        _abbrevNm = "IGF INTERNAL_" + custNm1;
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
      if (custSubGrp == "AQSTN" || custSubGrp == "XAQST") {
        _abbrevNm = "Acquisition use only";
      } else if (custSubGrp == "ASLOM" || custSubGrp == "XASLM") {
        _abbrevNm = "ASL use only";
      } else if (custSubGrp == "BLUMX" || custSubGrp == "XBLUM") {
        _abbrevNm = "Bluemix use only";
      } else if (custSubGrp == "MKTPC" || custSubGrp == "XMKTP") {
        _abbrevNm = "Market Place Order";
      } else if (custSubGrp == "SOFT" || custSubGrp == "XSOFT") {
        _abbrevNm = "Softlayer use only";
      } else if (custSubGrp == "BUSPR" || custSubGrp == "XBUSP") {
        _abbrevNm = getAbbrvNameForBP(custNm1, false);
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
      } else if (custSubGrp == "MKTPC" || custSubGrp == "XMKTP") {
        _abbrevNm = "Marketplace_" + custNm1;
      } else if (custSubGrp == "SOFT" || custSubGrp == "XSOFT") {
        _abbrevNm = "Softlayer Use Only";
      } else if (custSubGrp == "IGF" || custSubGrp == "XIGF") {
        _abbrevNm = "IGF_" + custNm1;
      } else if (custSubGrp == "DUMMY" || custSubGrp == "XDUMM") {
        _abbrevNm = "Dummy_" + custNm1;
      } else if (custSubGrp == "ESOSW" || custSubGrp == "XESO") {
        _abbrevNm = "ESA/OEM/SWG_" + custNm1;
      } else {
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
    var reqType = FormManager.getActualValue('reqType');
    if (custSubGrp != null && custSubGrp.length > 0 && custNm1) {
      if (custSubGrp == "BLUMX" || custSubGrp == "XBLUM") {
        if(reqType == 'C'){
          _abbrevNm = "Bluemix use only";
        } else {
          _abbrevNm = "Bluemix_" + custNm1;
        }
      } else if (custSubGrp == "MKTPC" || custSubGrp == "XMKTP") {
        if(reqType == 'C'){
          _abbrevNm = "Market place use only";
        } else {
          _abbrevNm = "Marketplace_" + custNm1;
        }
      } else if (custSubGrp == "SOFT" || custSubGrp == "XSOFT") {
        _abbrevNm = "Softlayer Use Only";
      } else if (custSubGrp == "IGF" || custSubGrp == "XIGF") {
        _abbrevNm = "IGF_" + custNm1;
      } else if (custSubGrp == "DUMMY" || custSubGrp == "XDUMM") {
        if(reqType == 'C' && custSubGrp == "DUMMY"){
          _abbrevNm = "IGF DUMMY use only";
        } else {
          _abbrevNm = "Dummy_" + custNm1;
        }
      } else if (custSubGrp == "ESOSW" || custSubGrp == "XESO") {
        if(reqType == 'C'){
          _abbrevNm = "ESA use only";
        } else {
          _abbrevNm = "ESA/OEM/SWG_" + custNm1;
        }
      // CREATCMR-7653 --START--
      } else if (reqType == 'C' && custSubGrp == "INTER") {
        _abbrevNm = "Internal use only";
      } else if (reqType == 'C' && (custSubGrp == "AQSTN" || custSubGrp == "XAQST")) {
        _abbrevNm = "Acquisition use only";
      // CREATCMR-7653 --END--
      } else {
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
function onInacTypeChange() {
  var cluster = FormManager.getActualValue('apCustClusterId');
  var reqType = null;
  reqType = FormManager.getActualValue('reqType');
  if (reqType == 'C') {
    if (_inacCdHandler == null) {
      _inacCdHandler = dojo.connect(FormManager.getField('inacType'), 'onChange', function(value) {
       
        var cluster = FormManager.getActualValue('apCustClusterId');
        var cmt = value + ','+ cluster +'%';
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
          console.log(value);
          if (value != null) {
            var inacCdValue = [];
            if(cluster.includes('BLAN') || 
                ((cluster == '04501' || cluster == '04683' || cluster == '04690') && 
                    (cntry == SysLoc.HONG_KONG || cntry == SysLoc.MACAO)) || ((cluster.includes('05224') || cluster.includes('05225') || cluster.includes('09062') || cluster.includes('09063') || cluster.includes('09064') || cluster.includes('04477') || cluster.includes('04496') || cluster.includes('04490') || cluster.includes('04494') || cluster.includes('04691') || cluster.includes('04467')) && (cntry == SysLoc.INDIA || cntry == SysLoc.BANGLADESH || cntry == SysLoc.SRI_LANKA)) ||
                     ((cluster.includes('04492') || cluster.includes('04503') || cluster.includes('04692') || cluster.includes('04481') || cluster.includes('04462') || cluster.includes('04483') || cluster.includes('00149') || cluster.includes('05220')) && (cntry == SysLoc.THAILAND || cntry == SysLoc.SINGAPORE))) {
                var qParams = {
                  _qall : 'Y',
                  ISSUING_CNTRY : cntry ,
                  CMT : cmt ,
                };
            } else if(cntry == '616') {
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
                // if (value == 'N' && !(cluster.includes('BLAN')) && cntry ==
                // '616') {
                if (value == 'N' && (!(cluster.includes('BLAN') || cluster.includes('00035') || cluster.includes('00127') || cluster.includes('00105') || cluster.includes('04470') || cluster.includes('04469') || cluster.includes('04471') || cluster.includes('04485') || cluster.includes('04500') || cluster.includes('04746') || cluster.includes('04744') || cluster.includes('04745')) && (cntry == SysLoc.AUSTRALIA))) { 
                  inacCdValue.push('new');
                }
                if ((value == 'N' && (!((cluster.includes('05224') || cluster.includes('05225') || cluster.includes('09062') || cluster.includes('09063') || cluster.includes('09064') || cluster.includes('04477') || cluster.includes('04496') || cluster.includes('04490') || cluster.includes('04494') || cluster.includes('04691') || cluster.includes('04467')) && (cntry == SysLoc.INDIA || cntry == SysLoc.BANGLADESH || cntry == SysLoc.SRI_LANKA)))) && cntry != SysLoc.AUSTRALIA && (cntry != SysLoc.THAILAND && cntry != SysLoc.SINGAPORE)) {
                  inacCdValue.push('new');
                }
                if (value == 'N' && (!(cluster.includes('04492') || cluster.includes('04503') || cluster.includes('04692') || cluster.includes('04481') || cluster.includes('04462') || cluster.includes('04483') || cluster.includes('00149') || cluster.includes('05220')) && (cntry == SysLoc.THAILAND || cntry == SysLoc.SINGAPORE))) {
                  inacCdValue.push('new');
                }
                FormManager.resetDropdownValues(FormManager.getField('inacCd'));
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

var _isicHandler = null;
function onIsicChangeHandler() {
  if (_isicHandler == null) {
    _isicHandler = dojo.connect(FormManager.getField('custSubGrp'), 'onChange', function(value) {
      onIsicChange();
    });
  }
}

function onIsicChange() {
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var custGrp = FormManager.getActualValue('custGrp');
  var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  var value = FormManager.getActualValue('isicCd');
  var cmrResult = FormManager.getActualValue('findCmrResult');
  var result = FormManager.getActualValue('findDnbResult');

  if (reqType == 'C' && role == 'REQUESTER' && (cmrIssuingCntry == '744' || cmrIssuingCntry == '834' || cmrIssuingCntry == '616')) {
    if (cmrResult != '' && cmrResult == 'Accepted') {
      FormManager.setValue('isicCd', '');
      FormManager.enable('isicCd');
    } else if (result != '' && result == 'Accepted') {
      console.log(value);
      var requestId = FormManager.getActualValue('reqId');
      qParams = {
        REQ_ID : requestId,
      };
      var result = cmr.query('GET.ISIC_OLD_BY_REQID', qParams);
      var oldISIC = result.ret1;
      if (custSubGrp == '' || custSubGrp == 'AQSTN' || custSubGrp == 'BLUMX' || custSubGrp == 'ESOSW' || custSubGrp == 'ECSYS' || custSubGrp == 'MKTPC' || custSubGrp == 'NRML'
          || custSubGrp == 'CROSS' || custSubGrp == 'SPOFF' || custSubGrp == 'XBLUM' || custSubGrp == 'XAQST' || custSubGrp == 'XMKTP' || custSubGrp == 'BUSPR' || custSubGrp == 'ASLOM') {
        FormManager.setValue('isicCd', oldISIC);
        FormManager.readOnly('isicCd');
      } else if (custSubGrp == 'INTER' || custSubGrp == 'PRIV' || custSubGrp == 'XPRIV' || custSubGrp == 'DUMMY' || custSubGrp == 'IGF') {
        FormManager.setValue('isicCd', value);
        FormManager.readOnly('isicCd');
      } else {
        FormManager.setValue('isicCd', '');
        FormManager.enable('isicCd');
      }
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
  for (var i = 0; i < arry.length; i++) {
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
  if (_mrcCd == '3' && _industryClass != '') {
    _isbuCd = 'GMB' + _industryClass;
    FormManager.setValue('isbuCd', _isbuCd);
  } else if (_mrcCd == '2' && _sectorCd != '' && _industryClass != '') {
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
  if (cmrIssuingCntry == '744' && custSubGrp == 'PRIV') {
    FormManager.readOnly('apCustClusterId');
  }
  if (custSubGrp == 'BLUMX' || custSubGrp == 'XBLUM') {
    if (cmrIssuingCntry == '615' || cmrIssuingCntry == '652' || cmrIssuingCntry == '744' || cmrIssuingCntry == '834') {
      FormManager.setValue('isbuCd', 'GMBW');
    }
  } else if (custSubGrp == 'DUMMY' || custSubGrp == 'XDUMM') {
    if (cmrIssuingCntry == '615' || cmrIssuingCntry == '643' || cmrIssuingCntry == '646' || cmrIssuingCntry == '652' || cmrIssuingCntry == '714' || cmrIssuingCntry == '720'
        || cmrIssuingCntry == '744' || cmrIssuingCntry == '749' || cmrIssuingCntry == '778' || cmrIssuingCntry == '818' || cmrIssuingCntry == '834' || cmrIssuingCntry == '852'
        || cmrIssuingCntry == '856') {
      FormManager.setValue('isbuCd', 'DUM1');
    }
  } else if (custSubGrp == 'IGF' || custSubGrp == 'XIGF') {
    if (cmrIssuingCntry == '744') {
      FormManager.setValue('isbuCd', 'DUM1');
    }
  } else if (custSubGrp == 'INTER' || custSubGrp == 'XINT') {
    if (cmrIssuingCntry == '615' || cmrIssuingCntry == '643' || cmrIssuingCntry == '646' || cmrIssuingCntry == '652' || cmrIssuingCntry == '714' || cmrIssuingCntry == '720'
        || cmrIssuingCntry == '744' || cmrIssuingCntry == '749' || cmrIssuingCntry == '778' || cmrIssuingCntry == '818' || cmrIssuingCntry == '834' || cmrIssuingCntry == '852'
        || cmrIssuingCntry == '856') {
      FormManager.setValue('isbuCd', 'INT1');
    }
  } else if (custSubGrp == 'MKTPC' || custSubGrp == 'XMKTP') {
    if (cmrIssuingCntry == '744' || cmrIssuingCntry == '834') {
      FormManager.setValue('isbuCd', 'GMBW');
    }
  } else if (custSubGrp == 'PRIV') {
    if (cmrIssuingCntry == '744') {
           FormManager.setValue('isbuCd', 'GMBW');
         }
  } else if (custSubGrp == 'BUSPR' || custSubGrp == 'XBUSP') {
    if (cmrIssuingCntry == '643' || cmrIssuingCntry == '646' || cmrIssuingCntry == '714' || cmrIssuingCntry == '720' || cmrIssuingCntry == '749' || cmrIssuingCntry == '778'
        || cmrIssuingCntry == '818' || cmrIssuingCntry == '834' || cmrIssuingCntry == '852' || cmrIssuingCntry == '856') {
      isbuList = [ 'BPN1', 'BPN2' ];
      console.log("isbuList = " + isbuList);
      FormManager.enable('isbuCd');
      FormManager.setValue('isbuCd', '');
      FormManager.limitDropdownValues(FormManager.getField('isbuCd'), isbuList);
    }
  }
}

function setCtcOnIsuCdChangeASEAN() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  isuCd = FormManager.getActualValue('isuCd');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var cluster = FormManager.getActualValue('apCustClusterId');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var reqType = FormManager.getActualValue('reqType');
  var clusterList = ['09050', '09051', '09052', '09053', '09054', '09055'];
  var cntryList = ['643', '749', '778', '818', '834', '856'];
  if (cntryList.includes(cntry) && clusterList.includes(cluster)) {
    return;
  }
  if (isuCd == '5K') {
    FormManager.removeValidator('clientTier', Validators.REQUIRED);
    FormManager.setValue('clientTier', '');
    FormManager.readOnly('clientTier');
  } else {
    if (reqType == 'U' || (reqType != 'U' && userRole == 'PROCESSOR')) {
      FormManager.enable('clientTier');
    }
  }
}

function setCtcOnIsuCdChangeANZ(isuCd) {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (isuCd == null) {
    isuCd = FormManager.getActualValue('isuCd');
  }
  if (isuCd == '5K') {
    FormManager.removeValidator('clientTier', Validators.REQUIRED);
  }
    handleObseleteExpiredDataForUpdate();  
}

function setCtcOnIsuCdChangeGCG() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  isuCd = FormManager.getActualValue('isuCd');
  if (isuCd == '5K') {
    FormManager.removeValidator('clientTier', Validators.REQUIRED);
  }
}

function setCtcOnIsuCdChangeISA() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  isuCd = FormManager.getActualValue('isuCd');
  if (isuCd == '5K') {
    FormManager.removeValidator('clientTier', Validators.REQUIRED);
  } else {
    FormManager.addValidator('clientTier', Validators.REQUIRED, [ 'Client Tier' ], 'MAIN_IBM_TAB');
    FormManager.enable('clientTier');
  }
  handleObseleteExpiredDataForUpdate();
}

function onIsuCdChangeAseanAnzIsa() {
  console.log(">>>> onIsuCdChangeAseanAnzIsa >>>>");
  var reqType = null;
  reqType = FormManager.getActualValue('reqType');
  var cmrIssuingCntry = dojo.byId('cmrIssuingCntry').value;
  var asean_isa_cntries = [ 'Bangladesh - 615', 'Sri Lanka - 652'];

  if (reqType == 'U') {
    console.log(">>>> Exit onIsuCdChangeAseanAnz for Update.");
    return;
  }
  // FormManager.enable('isuCd');
  _isuHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
    if (!value) {
      if (value == '') {
        if (asean_isa_cntries.indexOf(cmrIssuingCntry) >= 0)
          FormManager.removeValidator('inacCd', Validators.REQUIRED);
      } else if (asean_isa_cntries.indexOf(cmrIssuingCntry) >= 0) {
        setINACState();
      }

      return;
    }

    if (value != null && value.length > 1) {
      updateMRCAseanAnzIsa();
      // addSectorIsbuLogicOnISU();
      updateIsbuCd();
      if (asean_isa_cntries.indexOf(cmrIssuingCntry) >= 0) {
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
  var arryISUCdForMRC3 = [ '32', '34', '21', '60', '5K' ];
  var scenario = FormManager.getActualValue('custGrp');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var aseanCntries = ['852', '818', '856', '643', '778', '749', '834'];
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var _isuCd = FormManager.getActualValue('isuCd');
  var _clientTier = FormManager.getActualValue('clientTier');
  var cluster = FormManager.getActualValue('apCustClusterId');
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if ((cntry == '856' && ['01251', '08047' , '09053' , '00000'].includes(cluster)) || (cntry == '818' && ['01231', '08044', '00000' , '09054'].includes(cluster)) || (cntry == '778' && ['01222', '00000', '08042' ,'09051'].includes(cluster))  || (cntry == '643' && ['00000'].includes(cluster)) || (cntry == '749' && ['00000' , '09050' , '08040' , '09050'].includes(cluster)) || (cntry == '834' && ['01241' , '09052' , '00000' , '08038'].includes(cluster)) || (cntry == '852' && ['00000' , '01277' , '01273' , '09055' ,'08046'].includes(cluster)) || (cntry == '616' && ['01150' , '71100' , '00001' , '08039' ,'09057'].includes(cluster)) || (cntry == '796' && ['01147' , '71101' , '08037' , '00002' ,'09056'].includes(cluster))) {
    // CREATCMR-7653 : MRC = 2 - lock field
    if(cntry == '796' && custSubGrp == 'DUMMY' && ['00002'].includes(cluster) && _clientTier == 'Z' && _isuCd == '21'){
      FormManager.setValue('mrcCd', '2');
      FormManager.readOnly('mrcCd');
      return;
    }
    if((custSubGrp == 'INTER' || custSubGrp == 'XINT') && ((['00000'].includes(cluster) && (cntry == '856' || cntry == '818' || cntry == '778' || cntry == '643' || cntry == '749' || cntry == '834' || cntry == '852')) || (['00002'].includes(cluster) && cntry == '796') || (['00001'].includes(cluster) && cntry == '616')) && _isuCd == '21' &&  _clientTier == 'Z') {
      FormManager.setValue('mrcCd', '2');
      return;
    } 
    FormManager.setValue('mrcCd', '3');
    return;
  } else if ((cntry == '856' && ['04492', '04692', '04481' , '04483' , '05220'].includes(cluster)) || (cntry == '834' && ['04503', '04462', '05219'].includes(cluster)) || (cntry == '616' && ['00035' , '05221' , '00105' , '04485' ,'04500' , '04746' , '04744' , '04745' , '05222'].includes(cluster)) || (cntry == '796' && ['04694'].includes(cluster))) {
    FormManager.setValue('mrcCd', '2');
    return;
  }
  if((aseanCntries.includes(cntry) && (custSubGrp == 'DUMMY' || custSubGrp == 'XDUMM')) || ((cntry == '749' || cntry == '834') && (custSubGrp == 'MKTP' || custSubGrp == 'BLUMX' || custSubGrp == 'PRICU' || custSubGrp == 'SPOFF')) && ['00000'].includes(cluster)) {
    FormManager.setValue('mrcCd', '3');
  }
  if((aseanCntries.includes(cntry)) && (custSubGrp == '' || custSubGrp == 'INTER' || custSubGrp == 'XINT'|| custSubGrp == 'DUMMY' || custSubGrp == 'XDUMM')){
    return;
  }
  if(!(cntry == 744 && (custSubGrp == 'CROSS' || custSubGrp == 'AQSTN' || custSubGrp == 'NRML' || custSubGrp == 'ESOSW'))){
    FormManager.setValue('mrcCd', '');
    } 
  var _isuCd = FormManager.getActualValue('isuCd');
  if (_isuCd != null && _isuCd.length > 1) {
    FormManager.setValue('mrcCd', '3');
    var _exsitFlag = 0;
    for (var i = 0; i < arryISUCdForMRC3.length; i++) {
      if (arryISUCdForMRC3[i] == _isuCd) {
        _exsitFlag = 1;
      }
    }
    if (_exsitFlag == 0) {
      if(cntry == '744' && custSubGrp == 'PRIV'){
        FormManager.setValue('mrcCd', '3');
      } else{
        FormManager.setValue('mrcCd', '2');
      }
      FormManager.setValue('mrcCd', '2');
    }
    if(scenario == 'LOCAL' &&(cntry == '744' || cntry == '615' || cntry == '652') && custSubGrp == 'INTER'){
      FormManager.setValue('mrcCd', '3');
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
      if (FormManager.getActualValue('cmrIssuingCntry') == '744' && FormManager.getActualValue('reqType') == 'C')
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

function canRemoveAddress(value, rowIndex, grid) {
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

function setCTCIsuByCluster() {
var reqType = FormManager.getActualValue('reqType');
var role = FormManager.getActualValue('userRole').toUpperCase();
var issuingCntries = ['852', '818', '856', '643', '778', '749', '834', '616', '796'];
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
  var scenario = FormManager.getActualValue('custGrp');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (_cmrIssuingCntry == '616' || _cmrIssuingCntry == '796' || issuingCntries.includes(_cmrIssuingCntry)) {
   return;
   }
  var apClientTierValue = [];
  var isuCdValue = [];
  if (_cluster != '' && _cluster != '') {
    var qParams = {
      _qall : 'Y',
      ISSUING_CNTRY : _cmrIssuingCntry,
      CLUSTER : _cluster,
    };
 // cluster description
    var clusterDesc = cmr.query('GET.DESC_BY_CLUSTER', qParams);
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
      if (apClientTierValue.length == 1) {
        FormManager.limitDropdownValues(FormManager.getField('clientTier'), apClientTierValue);
        FormManager.limitDropdownValues(FormManager.getField('isuCd'), isuCdValue);
        FormManager.setValue('clientTier', apClientTierValue[0]);
        FormManager.setValue('isuCd', isuCdValue[0]);
      }
      else if (apClientTierValue.length > 1 && (_cmrIssuingCntry == '744' || _cmrIssuingCntry == '615' || _cmrIssuingCntry == '652'  || _cmrIssuingCntry == '834' || _cmrIssuingCntry == '852')) {
          if ((scenario == 'LOCAL' && ((_cmrIssuingCntry == '744' && (custSubGrp == 'BLUMX' || custSubGrp == 'MKTPC' || custSubGrp == 'IGF' || custSubGrp == 'PRIV')) || ((_cmrIssuingCntry == '615' || _cmrIssuingCntry == '652') && (custSubGrp == 'BLUMX' || custSubGrp == 'MKTPC' || custSubGrp == 'IGF' || custSubGrp == 'DUMMY')))) || (_cmrIssuingCntry == '834' && _cluster == '00000' && (custSubGrp == 'BLUMX' || custSubGrp == 'MKTPC' || custSubGrp == 'DUMMY' || custSubGrp == 'PRIV' || custSubGrp == 'XBLUM' || custSubGrp == 'XMKTP' || custSubGrp == 'XDUMM' || custSubGrp == 'XPRIV' || custSubGrp == 'SPOFF')) || (_cmrIssuingCntry == '852' && _cluster == '00000' && (custSubGrp == 'DUMMY' || custSubGrp == 'XDUMM')))  {
               FormManager.resetDropdownValues(FormManager.getField('clientTier'));
               FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Q','Y']);
               FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['34']);
               FormManager.setValue('clientTier', 'Q');
               FormManager.setValue('isuCd','34');
               FormManager.enable('clientTier');
            }
          else if (scenario == 'LOCAL' && custSubGrp == 'INTER'){
               FormManager.resetDropdownValues(FormManager.getField('clientTier'));
               FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['0']);
               FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['60']);
               FormManager.setValue('clientTier','0');
               FormManager.setValue('isuCd','60');
            }
          else {
               FormManager.resetDropdownValues(FormManager.getField('clientTier'));
               FormManager.setValue('clientTier','');
               FormManager.setValue('isuCd','');
               }
        }
    }
    if (clusterDesc[0] != '' && (clusterDesc[0].ret1.includes('S1') || clusterDesc[0].ret1.includes('IA') || clusterDesc[0].ret1.includes('S&S'))) {
      setIsuOnIsic();
    } 
  }
});
if (_clusterHandler && _clusterHandler[0]) {
  _clusterHandler[0].onChange();
}
}

function setCTCIsuByClusterANZ() {
  console.log(">>>> setCTCIsuByClusterANZ >>>>");
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var scenario = FormManager.getActualValue('custGrp');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var issuingCntries = ['852', '818', '856', '643', '778', '749', '834', '616', '796'];
  if (FormManager.getActualValue('viewOnlyPage') == 'true' || reqType != 'C') {
    return;
  }
  var _clusterHandler = dojo.connect(FormManager.getField('apCustClusterId'), 'onChange', function(value) {
    var clusterVal = FormManager.getActualValue('apCustClusterId');
    if (!clusterVal) {
      return;
    }
    var _cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
    var _cluster = FormManager.getActualValue('apCustClusterId');
    var scenario = FormManager.getActualValue('custGrp');
    var custSubGrp = FormManager.getActualValue('custSubGrp');

    var apClientTierValue = [];
    var isuCdValue = [];
    if ((scenario == 'LOCAL' && (custSubGrp == 'NRML' || custSubGrp == 'AQSTN' || custSubGrp == 'ESOSW' || custSubGrp == 'IGF' || custSubGrp == 'SOFT' || custSubGrp == 'PRIV' || custSubGrp == 'BLUMX' || custSubGrp == 'MKTPC' || custSubGrp == 'DUMMY' || custSubGrp == 'INTER')) || (scenario == 'CROSS' && (custSubGrp == 'XNRML' || custSubGrp == 'XAQST' || custSubGrp == 'XESO' || custSubGrp == 'XIGF' || custSubGrp == 'XSOFT' || custSubGrp == 'CROSS' || custSubGrp == 'XPRIV' || custSubGrp == 'XMKTP' || custSubGrp == 'XDUMM' || custSubGrp == 'XINT' || custSubGrp == 'XBLUM' ))) { 
    if (_cluster != '' && _cluster != '') {
      var qParams = {
        _qall : 'Y',
        ISSUING_CNTRY : _cmrIssuingCntry,
        CLUSTER : _cluster,
      };
      // cluster description
      var clusterDesc = cmr.query('GET.DESC_BY_CLUSTER', qParams);
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
        if (apClientTierValue.length == 1) {
          FormManager.limitDropdownValues(FormManager.getField('clientTier'), apClientTierValue);
          FormManager.limitDropdownValues(FormManager.getField('isuCd'), isuCdValue);
          FormManager.setValue('clientTier', apClientTierValue[0]);
          FormManager.setValue('isuCd', isuCdValue[0]);
        }  else if (apClientTierValue.length > 1) {
          if ((custSubGrp.includes('BLUMX') || custSubGrp.includes('MKTPC') || custSubGrp.includes('DUMMY') || custSubGrp.includes('XDUMM')) && (_cluster == '00001' || _cluster == '00002')) {
            // CREATCMR-7653 cluster/QTC/ISU: default as 00002/Z/21 - lock field
            if(_cmrIssuingCntry='796' && custSubGrp.includes('DUMMY') && _cluster == '00002'){
              FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Z']);
              FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['21']);
              FormManager.setValue('clientTier', 'Z');
              FormManager.setValue('isuCd','21');
              FormManager.readOnly('apCustClusterId');
              FormManager.readOnly('clientTier');
              FormManager.readOnly('isuCd');
            } else {
              FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Q' , 'Y']);
              FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['34']);
              FormManager.setValue('isuCd','34'); 
            }
          } else if(custSubGrp.includes('INTER') || custSubGrp.includes('XINT') ) {
            FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Z']);
            FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['21']);
            FormManager.setValue('clientTier', 'Z');
            FormManager.setValue('isuCd','21');
          } else {
            FormManager.resetDropdownValues(FormManager.getField('clientTier'));
            FormManager.limitDropdownValues(FormManager.getField('clientTier'), apClientTierValue);
            FormManager.limitDropdownValues(FormManager.getField('isuCd'), isuCdValue);
          }
       }
      }    
      if (clusterDesc[0] != '' && (clusterDesc[0].ret1.includes('S1') || clusterDesc[0].ret1.includes('IA') || _cluster.includes('BLAN') || clusterDesc[0].ret1.includes('S&S'))) {
		  setIsuOnIsic();
	  } 
    }
   } 
   
// if (custSubGrp == 'BLUMX' || custSubGrp == 'MKTPC' || custSubGrp == 'DUMMY'
// || custSubGrp == 'INTER' || custSubGrp == 'XBLUM' || custSubGrp == 'XMKTP' ||
// custSubGrp == 'XDUMM' || custSubGrp == 'XINT' || _cluster.includes('71101')
// || _cluster.includes('71100') || _cluster.includes('00001') ||
// _cluster.includes('00002')) {
// FormManager.setValue('isuCd', '34');
// FormManager.readOnly('isuCd');
// }
   
  });
  if (_clusterHandler && _clusterHandler[0]) {
    _clusterHandler[0].onChange();
  }
}

function setCTCIsuByClusterASEAN() {
  var reqType = FormManager.getActualValue('reqType');
  if (FormManager.getActualValue('viewOnlyPage') == 'true' || reqType != 'C') {
    return;
  }
  FormManager.addValidator('isuCd', Validators.REQUIRED, [ 'ISU Code' ], null);
  var _clusterHandler = dojo.connect(FormManager.getField('apCustClusterId'), 'onChange', function(value) {
    var clusterVal = FormManager.getActualValue('apCustClusterId');
    if (!clusterVal) {
      return;
    }
    var _cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
    var _cluster = FormManager.getActualValue('apCustClusterId');
    var scenario = FormManager.getActualValue('custGrp');
    var custSubGrp = FormManager.getActualValue('custSubGrp');

    var apClientTierValue = [];
    var isuCdValue = [];
    var issuingCntries = ['852', '818', '856', '643', '778', '749' , '834'];
    if (_cluster != '' && _cluster != '') {
      var qParams = {
        _qall : 'Y',
        ISSUING_CNTRY : _cmrIssuingCntry,
        CLUSTER : _cluster,
      };
      
      var clusterDesc = cmr.query('GET.DESC_BY_CLUSTER', qParams);
      var results = cmr.query('GET.CTC_ISU_BY_CLUSTER_CNTRY', qParams);
      if (results != null) {
        for (var i = 0; i < results.length; i++) {
          apClientTierValue.push(results[i].ret1);
          isuCdValue.push(results[i].ret2);
        }
        
        if (apClientTierValue.length == 1) {
          FormManager.limitDropdownValues(FormManager.getField('clientTier'), apClientTierValue);
          FormManager.limitDropdownValues(FormManager.getField('isuCd'), isuCdValue);
          FormManager.setValue('clientTier', apClientTierValue[0]);
          FormManager.setValue('isuCd', isuCdValue[0]);
        } else if (apClientTierValue.length > 1) {
          if(custSubGrp.includes('BLUM') || custSubGrp.includes('MKTP') || custSubGrp.includes('MKP') || ((custSubGrp.includes('DUMMY') || custSubGrp.includes('XDUMM')) && issuingCntries.includes(_cmrIssuingCntry)) || ((custSubGrp.includes('PRICU') || custSubGrp.includes('BLUMX') || custSubGrp.includes('SPOFF')) && (_cmrIssuingCntry == '749' || _cmrIssuingCntry == '834')) || (_cmrIssuingCntry == '834' && _cluster == '00000' && (custSubGrp == 'MKTPC' || custSubGrp == 'PRIV' || custSubGrp == 'XBLUM' || custSubGrp == 'XMKTP' || custSubGrp == 'XPRIV'))) {
            FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Q' , 'Y']);
            FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['34']);
            FormManager.setValue('isuCd','34');
          } else if(custSubGrp.includes('INTER') || custSubGrp.includes('XINT') ) {
            FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Z']);
            FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['21']);
            FormManager.setValue('clientTier', 'Z');
            FormManager.setValue('isuCd','21');
          } else {
            FormManager.resetDropdownValues(FormManager.getField('clientTier'));
            FormManager.limitDropdownValues(FormManager.getField('clientTier'), apClientTierValue);
            FormManager.limitDropdownValues(FormManager.getField('isuCd'), isuCdValue);
          }
       }
     }
      
     if (clusterDesc[0] != '' && (clusterDesc[0].ret1.includes('S1') || clusterDesc[0].ret1.includes('IA') || _cluster.includes('BLAN') || clusterDesc[0].ret1.includes('S&S'))) {
        setIsuOnIsic();
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
  setIsuOnIsic();
}

function addAbnValidatorForAU() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var abn = FormManager.getActualValue('vat');
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        if (custSubGrp == "AQSTN" || custSubGrp == "XAQST" || custSubGrp == "IGF" || custSubGrp == "XIGF" || custSubGrp == "NRML" || custSubGrp == "XNRML" || custSubGrp == "SOFT"
            || custSubGrp == "XSOFT") {
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

        if (FormManager.getActualValue('reqType') == 'C' && FormManager.getActualValue('Processor') == 'true' && custGrp == 'cross' && custSubType == 'SPOFF') {
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
    var custGrp = FormManager.getActualValue('custGrp');
    if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.AUSTRALIA) {
      if (custGrp == 'CROSS') {
        FormManager.resetValidations('stateProv');
      } else {
        FormManager.addValidator('stateProv', Validators.REQUIRED, [ 'State/Province' ], null);
      }
    }
    if (FormManager.getActualValue('cmrIssuingCntry') == '796') {
      if (landCntry == 'CK') {
        FormManager.resetValidations('stateProv');
        FormManager.resetValidations('city1');
        FormManager.resetValidations('postCd');

      } else {
        // FormManager.addValidator('stateProv', Validators.REQUIRED, [ 'State'
        // ], null);
        FormManager.removeValidator('stateProv', Validators.REQUIRED);
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

function setINACState() {
  var role = null;
  var isuCd = FormManager.getActualValue('isuCd');
  var cmrCntry = FormManager.getActualValue('cmrIssuingCntry');
  var isaCntries = [ ];
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }
  if (role == 'Requester' && (isuCd == '34' || isuCd == '04' || isuCd == '3T') && cmrCntry == '616') {
    FormManager.addValidator('inacCd', Validators.REQUIRED, [ 'INAC/NAC Code' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('inacType', Validators.REQUIRED, [ 'INAC Type' ], 'MAIN_IBM_TAB');
  } else {
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
function setCollCdFrIndia() {
  var reqType = FormManager.getActualValue('reqType');
  if (reqType == 'U') {
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

function setCollCdFrAU(cntry, addressMode, saving, finalSave, force) {
  var reqType = FormManager.getActualValue('reqType');
  var record = null;
  var addrType = null;
  var custNm1 = null;
  var provCd = null;
  if (reqType == 'U') {
    return;
  }
  if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {

    for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
      record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
      if (record == null && _allAddressData != null && _allAddressData[i] != null) {
        record = _allAddressData[i];
      }
      addrType = record.addrType[0];
      custNm1 = record.custNm1[0];
      provCd = record.stateProv[0];
    }
  }
  if (FormManager.getActualValue('addrType') == 'ZS01' || (addrType != null && addrType == 'ZS01')) {
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
    if (custNm1 == null || custNm1 == '' || cmr.addressMode == 'updateAddress')
      custNm1 = FormManager.getActualValue('custNm1');
    if (provCd == null || provCd == '' || cmr.addressMode == 'updateAddress')
      provCd = FormManager.getActualValue('stateProv');
    // }
    // else{
    // custNm1 = FormManager.getActualValue('custNm1');
    // provCd = FormManager.getActualValue('stateProv');
    // }
    if ([ 'NSW', 'NT', 'ACT' ].indexOf(provCd) >= 0) {
      if (regEx1.test(custNm1)) {
        FormManager.setValue('collectionCd', '00JC');
      }
      if (regEx2.test(custNm1)) {
        FormManager.setValue('collectionCd', '00JD');
      }
      if (regEx3.test(custNm1)) {
        FormManager.setValue('collectionCd', '00I2');
      }
      if (regEx4.test(custNm1)) {
        FormManager.setValue('collectionCd', '00JK');
      }
    }
    if ([ 'VIC', 'TAS' ].indexOf(provCd) >= 0) {
      if (regEx5.test(custNm1)) {
        FormManager.setValue('collectionCd', '00J1');
      }
      if (regEx6.test(custNm1)) {
        FormManager.setValue('collectionCd', '00A2');
      }
    }
    if (provCd == 'QLD') {
      if (regEx7.test(custNm1)) {
        FormManager.setValue('collectionCd', '00OS');
      }
    }
    if (provCd == 'SA') {
      if (regEx7.test(custNm1)) {
        FormManager.setValue('collectionCd', '00GG');
      }
    }
    if (provCd == 'WA') {
      if (regEx7.test(custNm1)) {
        FormManager.setValue('collectionCd', '00PZ');
      }
    }
  }
}

var _govIndcHandler = null;
function addGovIndcHanlder() {
  _govIndcHandler = dojo.connect(FormManager.getField('govType'), 'onClick', function(value) {
    var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
    if (viewOnlyPage != 'true' && FormManager.getActualValue('reqType') == 'C') {
      FormManager.resetValidations('taxCd2');
      if (dijit.byId('govType').get('checked')) {
        FormManager.addValidator('taxCd2', Validators.REQUIRED, [ 'Government Customer Type' ], 'MAIN_IBM_TAB');
      } else {
        FormManager.removeValidator('taxCd2', Validators.REQUIRED);
      }
    }
  });
}

var _govCustTypeHandler = null;
function addGovCustTypHanlder() {
  _govIndcHandler = dojo.connect(FormManager.getField('taxCd2'), 'onChange', function(value) {
    setAbbrevNameforGovType();
  });
}

function setAbbrevNameforGovType() {
  var govCustType = FormManager.getActualValue('taxCd2');
  var abbrevNmPrefix = null;
  var abbrevNm = FormManager.getActualValue('abbrevNm');

  if (FormManager.getActualValue('reqType') == 'C' && (govCustType && govCustType != '') && (abbrevNm && abbrevNm != '')) {
    switch (govCustType) {
    case '012':
      abbrevNmPrefix = 'ZC';
      break;
    case '013':
      abbrevNmPrefix = 'ZS';
      break;
    case '014':
      abbrevNmPrefix = 'ZL';
      break;
    default:
      abbrevNmPrefix = '';
    }
    if (abbrevNmPrefix != null)
      abbrevNm = abbrevNmPrefix + FormManager.getActualValue('abbrevNm');
    FormManager.setValue('abbrevNm', abbrevNm);
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

function addAddressInstancesValidator() {
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
          if (results != null) {
            for (var j = 0; j < results.length; j++) {
              var addrCnt = 0;
              for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
                record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
                if (record == null && _allAddressData != null && _allAddressData[i] != null) {
                  record = _allAddressData[i];
                }
                type = record.addrType;
                if (typeof (type) == 'object') {
                  type = type[0];
                }
                if (results[j].ret1 == type) {
                  // New requirement : Defect 1767113 : For HK and MO ->
                  // Multiple
                  // Billing & Billing CCR are to be allowed
                  if ((cmrCntry == SysLoc.MACAO || cmrCntry == SysLoc.HONG_KONG) && (record.addrType == 'ZP01' || record.addrType == 'ZP02')) {
                    continue;
                  }
                  addrCnt++;
                  if (addrCnt > 1)
                    duplicatesAddr.push(results[j].ret2);
                }
              }
            }
          }
          if (duplicatesAddr.length > 0) {
            return new ValidationResult(null, false, 'Only one instance of each address can be added.Please remove additional ' + duplicatesAddr + ' addresses');
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

          for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
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
              if ((custName == null || streetAddr == null || postCd == null || city == null )) {
                mandtDetails_1++;
              }
              break;
            case '616':
              var custGrp = FormManager.getActualValue('custGrp');
              if (custGrp != 'CROSS' && (custName == null || streetAddr == null || postCd == null || city == null || state == null)) {
                mandtDetails_2++;
              } else if (custGrp == 'CROSS' && (custName == null || streetAddr == null || postCd == null || city == null)) {
                mandtDetails_2++;
              }
              break;
            case '736':
            case '738':
              if ((custName == null || streetAddr == null)) {
                mandtDetails_3++;
              }
              break;
            }
          }
          if (mandtDetails > 0) {
            return new ValidationResult(null, false, "Customer Name  is required, Street Address is required, Postal Code is required.");
          } else if (mandtDetails_1 > 0) {
            return new ValidationResult(null, false, "Customer Name  is required, Street Address is required, Postal Code is required and City is required.");
          } else if (mandtDetails_2 > 0) {
            return new ValidationResult(null, false, "Customer Name  is required, Street Address is required, Postal Code is required, City is required and State is required.");
          } else if (mandtDetails_3 > 0) {
            return new ValidationResult(null, false, "Customer Name  is required, Street Address is required.");
          }
        }
        return new ValidationResult(null, true);

      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');

}

function similarAddrCheckValidator() {
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
          for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
            record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
            if (record == null && _allAddressData != null && _allAddressData[i] != null) {
              record = _allAddressData[i];
            }
            custNm1 = record.custNm1[0];
            custNm2 = record.custNm2[0] != null ? record.custNm2[0] : '';
            addrTxt = record.addrTxt[0];
            city1 = record.city1[0] != null ? record.city1[0] : '';
            stateProv = record.stateProv[0] != null ? record.stateProv[0] : '';
            addrTxt2 = record.addrTxt2[0] != null ? record.addrTxt2[0] : '';
            ;
            landCntry = record.landCntry[0];
            dept = record.dept[0] != null ? record.dept[0] : '';
            importIndc = record.importInd[0] != null ? record.importInd[0] : '';

            if (req_type == 'U' && importIndc == 'Y') {
              continue; // skip the unique check for addresses in case of
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
            if (results.ret1 > 1) {
              addrDupExists.push(record.addrTypeText);
            }
          }
          if (addrDupExists.length > 0) {
            return new ValidationResult(null, false, 'Duplicate address details exist for Addresses ' + addrDupExists + '. Please make sure every address detail should be a unique combination.');
          } else {
            return new ValidationResult(null, true);
          }
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function setFieldsForDoubleCreates() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var custGrp = FormManager.getActualValue('custGrp');
  if (reqType == 'U') {
    return new ValidationResult(null, true);
  }

  if (cntry == '615' && role == 'PROCESSOR' && (custGrp == 'LOCAL' || custGrp == 'CROSS')) {
    FormManager.enable('cmrNo');
    FormManager.enable('cmrNoPrefix');
  }
  if (cntry == '652' && (role == 'PROCESSOR' || role == 'REQUESTER') && (custGrp == 'LOCAL' || custGrp == 'CROSS')) {
    FormManager.enable('cmrNo');
    FormManager.enable('cmrNoPrefix');
  }
  if (cntry == '744' && role == 'PROCESSOR' && (custSubGrp == 'ESOSW' || custSubGrp == 'XESO')) {
    FormManager.enable('cmrNo');
    FormManager.enable('cmrNoPrefix');
  }
  if (cntry == '744' && role == 'REQUESTER' && custSubGrp == 'PRIV') {
    FormManager.readOnly('apCustClusterId');
    FormManager.removeValidator('repTeamMemberNo', Validators.REQUIRED);
    FormManager.removeValidator('repTeamMemberName', Validators.REQUIRED);
  }
  if (cntry == '744' && role == 'REQUESTER' && custSubGrp == 'CROSS') {
    FormManager.setValue('collectionCd', 'I001');
    FormManager.setValue('busnType', '709');
    FormManager.setValue('territoryCd', '709');
  }
  if (cntry == '852' && role == 'PROCESSOR' && (custSubGrp != 'BLUMX' || custSubGrp != 'MKTPC')) {
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
         * if (FormManager.getActualValue('cmrNo') != '' ) { showError = true;
         * }else{ showError = false; }
         */
        if (cntry == '834' && reqType == 'C' && role == 'PROCESSOR' && custGrp == 'CROSS' && custSubGrp == 'SPOFF') {
          if (FormManager.getActualValue('cmrNo') != '' && cmrNumber.length == 6) {
            var cmrNumber = FormManager.getActualValue('cmrNo');

            qParams = {
              KATR6 : '834',
              MANDT : cmr.MANDT,
              ZZKV_CUSNO : cmrNumber,
            };
            var recordSG = cmr.query('CHECK_CMR_RDC', qParams);
            var rdcCMRCountSG = recordSG.ret1;

            if (Number(rdcCMRCountSG) > 0) {

              return new ValidationResult(null, false, 'Please enter another CMR Number as entered CMR Number already exists in RDC.');
            }

            var resultWTAAS_SG = CmrServices.checkWTAAS(cmrNumber, '834');
            if ((resultWTAAS_SG.success && resultWTAAS_SG.data && resultWTAAS_SG.Status && resultWTAAS_SG.Status == 'F' && resultWTAAS_SG.Error_Msg != null && resultWTAAS_SG.Error_Msg.toLowerCase()
                .includes("does not exist"))) {
              return new ValidationResult(null, true);
            } else if (!resultWTAAS_SG.success) {
              return new ValidationResult(null, false, 'Cannot connect to WTAAS at the moment. Please try again after sometime.');
            } else {
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
        } else {
          showError = false;
        }

        if ((cntry == '615' && role == 'PROCESSOR' && (custGrp == 'LOCAL' || custGrp == 'CROSS'))
            || (cntry == '652' && (role == 'PROCESSOR' || role == 'REQUESTER') && (custGrp == 'LOCAL' || custGrp == 'CROSS'))
            || (cntry == '744' && role == 'PROCESSOR' && (custSubGrp == 'ESOSW' || custSubGrp == 'XESO'))
            || (cntry == '852' && role == 'PROCESSOR' && (custSubGrp != 'BLUMX' || custSubGrp != 'MKTPC'))) {
          if (showError)
            return new ValidationResult(null, false, 'For CMR Number and CMR Number Prefix in case of double creates, only one can be filled.');
          else if (FormManager.getActualValue('cmrNo') != '') {
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
            if (Number(rdcCMRCount) > 0 || Number(rdcCMRCountSG) > 0) {

              return new ValidationResult(null, false, 'Please enter another CMR Number as entered CMR Number already exists in RDC.');
            }

            var resultWTAAS = CmrServices.checkWTAAS(cmrNumber, katr6);
            var resultWTAAS_SG = CmrServices.checkWTAAS(cmrNumber, '834');
            if ((resultWTAAS.success && resultWTAAS.data && resultWTAAS.Status && resultWTAAS.Status == 'F' && resultWTAAS.Error_Msg != null && resultWTAAS.Error_Msg.toLowerCase().includes(
                "does not exist"))
                && (resultWTAAS_SG.success && resultWTAAS_SG.data && resultWTAAS_SG.Status && resultWTAAS_SG.Status == 'F' && resultWTAAS_SG.Error_Msg != null && resultWTAAS_SG.Error_Msg
                    .toLowerCase().includes("does not exist"))) {
              return new ValidationResult(null, true);
            } else if (!resultWTAAS.success || !resultWTAAS_SG.success) {
              return new ValidationResult(null, false, 'Cannot connect to WTAAS at the moment. Please try again after sometime.');
            } else {
              return new ValidationResult(null, false, 'Please enter another CMR Number as entered CMR Number already exists in WTAAS.');
            }
          } else if (FormManager.getActualValue('cmrNoPrefix') != '') {
            return new ValidationResult(null, true);
          } else {
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
  var asean_isa_cntries = [ SysLoc.BRUNEI, SysLoc.MALASIA, SysLoc.INDONESIA, SysLoc.SINGAPORE, SysLoc.PHILIPPINES, SysLoc.THAILAND, SysLoc.VIETNAM, SysLoc.INDIA, SysLoc.SRI_LANKA, SysLoc.BANGLADESH,
      SysLoc.HONG_KONG, SysLoc.MACAO ];
  if ((cmrIssuingCntry == SysLoc.AUSTRALIA || cmrIssuingCntry == SysLoc.NEW_ZEALAND)) {
    $('label[for="city1_view"]').text('Suburb:');
  }
  if (asean_isa_cntries.indexOf(cmrIssuingCntry) > -1) {
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
  if (role == 'REQUESTER' || FormManager.getActualValue('viewOnlyPage') == 'true')
    return;
  if (cmr.addressMode == 'updateAddress') {
    FormManager.readOnly('custNm1');
    FormManager.readOnly('custNm2');
  } else {
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
  var _cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  
  // CREATCMR-7653
  if(reqType == 'C' && _cmrIssuingCntry == '796'){
    console.log(">>> setISUDropDownValues() for {796 && CREATE} >>>");
    setDefaultValueForNZCreate();
  }
  
  if (!cluster || !ctc || !isuCd || _cmrIssuingCntry == '796' || _cmrIssuingCntry == '616'  || _cmrIssuingCntry == '834' || _cmrIssuingCntry == '852' || _cmrIssuingCntry == '818' || _cmrIssuingCntry == '856' || _cmrIssuingCntry == '749') {
    return;
  }
  var _cluster = FormManager.getActualValue('apCustClusterId');
  var scenario = FormManager.getActualValue('custGrp');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var issuingCntries = ['852', '818', '856', '643', '778', '749', '834'];

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
      
      if (apClientTierValue.length == 1) {
        FormManager.limitDropdownValues(FormManager.getField('clientTier'), apClientTierValue);
        FormManager.limitDropdownValues(FormManager.getField('isuCd'), isuCdValue);
        FormManager.setValue('clientTier', apClientTierValue[0]);
        FormManager.setValue('isuCd', isuCdValue[0]);
      }
      else if (apClientTierValue.length > 1 && (_cmrIssuingCntry == '744' || _cmrIssuingCntry == '615' || _cmrIssuingCntry == '652' || _cmrIssuingCntry == '834' || _cmrIssuingCntry == '852')) {
        if ((scenario == 'LOCAL' && ((_cmrIssuingCntry == '744' && (custSubGrp == 'BLUMX' || custSubGrp == 'MKTPC' || custSubGrp == 'IGF' || custSubGrp == 'PRIV')) || ((_cmrIssuingCntry == '615' || _cmrIssuingCntry == '652') && (custSubGrp == 'BLUMX' || custSubGrp == 'MKTPC' || custSubGrp == 'IGF' || custSubGrp == 'DUMMY')))) || (_cmrIssuingCntry == '834' && _cluster == '00000' && (custSubGrp == 'BLUMX' || custSubGrp == 'MKTPC' || custSubGrp == 'DUMMY' || custSubGrp == 'PRIV' || custSubGrp == 'XBLUM' || custSubGrp == 'XMKTP' || custSubGrp == 'XDUMM' || custSubGrp == 'XPRIV' || custSubGrp == 'SPOFF')) || (_cmrIssuingCntry == '852' && _cluster == '00000' && (custSubGrp == 'DUMMY' || custSubGrp == 'XDUMM'))) {
             FormManager.resetDropdownValues(FormManager.getField('clientTier'));
             FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Q','Y']);
             FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['34']);
             FormManager.setValue('clientTier', 'Q');
             FormManager.setValue('isuCd','34');
             FormManager.enable('clientTier');
          }
        else if (scenario == 'LOCAL' && custSubGrp == 'INTER'){
             FormManager.resetDropdownValues(FormManager.getField('clientTier'));
             FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['0']);
             FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['60']);
             FormManager.setValue('clientTier','0');
             FormManager.setValue('isuCd','60');
          }
        else {
             FormManager.resetDropdownValues(FormManager.getField('clientTier'));
             FormManager.setValue('clientTier','');
             FormManager.setValue('isuCd','');
             }
      } else if (apClientTierValue.length > 1) {
        if(issuingCntries.includes(_cmrIssuingCntry) && (custSubGrp.includes('BLUM') || custSubGrp.includes('MKTP') || custSubGrp.includes('MKP'))) {
          FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Z']);
          FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['34']);
          FormManager.setValue('clientTier', 'Z');
          FormManager.setValue('isuCd','34');
        } else {
           FormManager.resetDropdownValues(FormManager.getField('clientTier'));
           FormManager.limitDropdownValues(FormManager.getField('clientTier'), apClientTierValue);
           FormManager.limitDropdownValues(FormManager.getField('isuCd'), isuCdValue);
        }
      }
    }
  }
  setIsuOnIsic();
}

function setCollCdFrSGOnAddrSave(cntry, addressMode, saving, finalSave, force) {
  console.log(">>>> setCollCd for Singapore >>>>");
  var addrType = FormManager.getActualValue('addrType');
  var custSubType = FormManager.getActualValue('custSubGrp');
  if (custSubType != 'SPOFF')
    return;

  if (typeof (_pagemodel) != 'undefined') {
    reqType = FormManager.getActualValue('reqType');
  }
  if (reqType == 'U') {
    return;
  }
  if ((finalSave || force) && cmr.addressMode) {
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

function setCollCdFrSingapore() {
  var custSubType = FormManager.getActualValue('custSubGrp');
  if (custSubType != 'SPOFF')
    return;
  var zs01ReqId = FormManager.getActualValue('reqId');
  var collCd = null;
  var qParams = {
    REQ_ID : zs01ReqId,
  };
  var landCntry = FormManager.getActualValue('landCntry');
  if (landCntry == '' || !landCntry) {
    var result = cmr.query('ADDR.GET.LANDCNTRY.BY_REQID', qParams);
    landCntry = result.ret1;
  }
  switch (landCntry) {
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
  default:
    collCd = 'S013';
  }
  FormManager.setValue('collectionCd', collCd);
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

        if (addrTxt != '' && addrTxt2 != '') {
          if (addrTxt.length > 27) {
            return new ValidationResult(null, false, 'Street address should not exceed 27 characters when both Street and Street Con\'t present.');
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
          for (var i = 0; i < results.length; i++) {
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

function validateContractAddrAU() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var custNm1 = FormManager.getActualValue('custNm1').toUpperCase();;
        var custNm2 = FormManager.getActualValue('custNm2').toUpperCase();;
        var dept = FormManager.getActualValue('dept').toUpperCase();;
        var addrTxt1= FormManager.getActualValue('addrTxt').toUpperCase();;
        var addrTxt2 = FormManager.getActualValue('addrTxt2').toUpperCase();;
        var city1 = FormManager.getActualValue('city1').toUpperCase();;
        var addrType = FormManager.getActualValue('addrType');
        var address = custNm1 + custNm2 + dept + addrTxt1 + addrTxt2 + city1 ;
        if ((address.includes("PO BOX") || address.includes("POBOX")) && addrType == "ZS01") {
          return new ValidationResult(null, false, 'Contract address can not contain wording PO BOX');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function validateCustNameForNonContractAddrs() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var custNm1 = FormManager.getActualValue('custNm1').toUpperCase();
        var custNm2 = FormManager.getActualValue('custNm2').toUpperCase();
        var custNm = custNm1+custNm2 ;
        var reqId = FormManager.getActualValue('reqId');
        var addrType = FormManager.getActualValue('addrType');
        var reqType = FormManager.getActualValue('reqType');
        var contractCustNm = cmr.query('GET.CUSTNM_ADDR', {
          REQ_ID : reqId,
          ADDR_TYPE : 'ZS01'
        });
        if (contractCustNm != undefined) {
          zs01CustName = contractCustNm.ret1.toUpperCase() + contractCustNm.ret2.toUpperCase();
        }
        
        if (zs01CustName != custNm &&  addrType != "ZS01" && _pagemodel.reqType == 'U') {
          return new ValidationResult(null, false, 'customer name of additional address must be the same as the customer name of contract address');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function validateStreetAddrCont2() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var streetAddrCont1 = FormManager.getActualValue('addrTxt2');
        var cmrCntry = FormManager.getActualValue('cmrIssuingCntry');
        var streetAddrCont2 = "";
        if (cmrCntry == '738' || cmrCntry == '736') {
          streetAddrCont2 = FormManager.getActualValue('city1');
        } else {
          streetAddrCont2 = FormManager.getActualValue('dept');
        }
        if (streetAddrCont1 == '' && streetAddrCont2 != '') {
          return new ValidationResult(null, false, 'Street Address Con\'t2 cannot have a value as Street Address Con\'t1 is blank.');
        }

        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

// API call for validating GST for India on Save Request and Send for Processing
function validateGSTForIndia() {

  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        var reqTyp = FormManager.getActualValue('reqType');
        var vat = FormManager.getActualValue('vat');
        var reqId = FormManager.getActualValue('reqId');
        if (dijit.byId('vatExempt').get('checked')) { 
          return new ValidationResult(null, true); 
        }
        if (cntry != '744' || custSubGrp == 'CROSS') {
          return new ValidationResult(null, true);
        }
        var country = "";
        if (SysLoc.INDIA == FormManager.getActualValue('cmrIssuingCntry')) {
        country = "IN";
        if (country != '') {
          if (vat == '') {
            return new ValidationResult(null, true);
          } else {
            if (reqId != null) {
              reqParam = {
                REQ_ID : reqId
              };
            }
            var results = cmr.query('GET_ZS01', reqParam);
            if (results.ret1 != undefined) {
              
              var name = results.ret1;
              var address = results.ret2;
              var postal = results.ret3;
              var city = results.ret4;
              var state = results.ret5;
              var country = '';
              
              if (state != null && state != '') {
                reqParam = {
                    STATE_PROV_CD : state,
                  };
              var stateResult = cmr.query('GET_STATE_DESC', reqParam);
                if (stateResult != null) {
                  country = stateResult.ret1;
                }
              }
            var gstRet = cmr.validateGST(country, vat, name, address, postal, city);
            if (!gstRet.success) {
              return new ValidationResult({
                id : 'vat',
                type : 'text',
                name : 'vat'
              }, false, gstRet.errorMessage);
            } else {
              return new ValidationResult(null, true);
            }
            }else {
              return new ValidationResult(null, true);
            }
          }
        } else {
          return new ValidationResult(null, true);
        }
      }
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

// API call for validating ABN for Australia on Save Request and Send for
// Processing
function validateABNForAU() {

FormManager.addFormValidator((function() {
 return {
   validate : function() {
     var cntry = FormManager.getActualValue('cmrIssuingCntry');
     var reqTyp = FormManager.getActualValue('reqType');
     var vat = FormManager.getActualValue('vat');
     var reqId = FormManager.getActualValue('reqId');
     var formerAbn = getFormerVatAU(reqId);
     if (reqTyp == 'C') {
       return new ValidationResult(null, true);
     }
     var country = "";
     if (SysLoc.AUSTRALIA == FormManager.getActualValue('cmrIssuingCntry')) {
     country = "AU";
     if (country != '') {
       if (vat == '') {
         return new ValidationResult(null, true);
       } else {
         if (reqId != null) {
           reqParam = {
             REQ_ID : reqId
           };
         }              
         var abnRet = cmr.validateABN(vat, reqId, formerAbn);
         if (!abnRet.success) {
           return new ValidationResult({
             id : 'vat',
             type : 'text',
             name : 'vat'
           }, false, abnRet.errorMessage);
         } else {
           return new ValidationResult(null, true);
         }
       }
     } else {
       return new ValidationResult(null, true);
     }
   }
   }
 };
})(), 'MAIN_CUST_TAB', 'frmCMR');
}

function getFormerVatAU(reqId) {
  var formerVat = '';
  var qParams = {
    REQ_ID : reqId,
  };
  var result = cmr.query('GET.VAT_DATA_RDC', qParams);
  if (result != null) {
    formerVat = result.ret1;
  }
  return formerVat;
}

function getFormerCustNameAU(reqId) {
  var custNm1 = '';
  var custNM2 = '';
  var formerCustNm = '';
  var qParams = {
    REQ_ID : reqId,
    ADDR_TYPE : "ZS01"
  };
  var result = cmr.query('GET.CUSTNM_DATA_RDC', qParams);
  if (result != null) {
    formerCustNm = result.ret1.toUpperCase() + " " + result.ret2.toUpperCase();
  }
  return formerCustNm;
}

function lockFieldsForIndia(){
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if(reqType == 'U' && role == 'REQUESTER'){
  FormManager.readOnly('abbrevNm');
  FormManager.readOnly('abbrevLocn');
  FormManager.readOnly('custPrefLang');
  FormManager.readOnly('subIndustryCd');
  FormManager.readOnly('sensitiveFlag');
  FormManager.readOnly('isicCd');
  FormManager.readOnly('taxCd1');
  FormManager.readOnly('cmrNo');
  FormManager.readOnly('cmrOwner');
  FormManager.resetValidations('cmrOwner');

  FormManager.readOnly('apCustClusterId');
  FormManager.resetValidations('apCustClusterId');

  FormManager.readOnly('clientTier');
  FormManager.resetValidations('clientTier');

  FormManager.readOnly('isuCd');
  FormManager.readOnly('mrcCd');
  FormManager.readOnly('bpRelType');
  FormManager.readOnly('busnType');
  FormManager.resetValidations('busnType');

  FormManager.readOnly('cmrNoPrefix');
  FormManager.readOnly('collectionCd');
  FormManager.resetValidations('collectionCd');

  FormManager.readOnly('repTeamMemberNo');
  FormManager.resetValidations('repTeamMemberNo');

  FormManager.readOnly('miscBillCd');
  FormManager.readOnly('inacType');
  FormManager.readOnly('inacCd');
  FormManager.readOnly('restrictInd');
  FormManager.readOnly('govType');
  FormManager.readOnly('repTeamMemberName');
  FormManager.readOnly('covId');
  FormManager.resetValidations('covId');

  FormManager.readOnly('dunsNo');
  }
  if (reqType == 'C'  && !(custSubGrp == 'INTER' || custSubGrp == 'ESOSW')){
    FormManager.readOnly('cmrNoPrefix');
  }
  if (reqType == 'C'  && custSubGrp == 'IGF'){
    FormManager.setValue('isicCd', '8888');
  }

  if (reqType == 'C'  && custSubGrp == 'INTER'){
    FormManager.setValue('isicCd', '0000');
  }

  if (reqType == 'C'  && custSubGrp == 'PRIV'){
    FormManager.setValue('isicCd', '9500');
  }
}

function lockFieldsForAU() {
  var clusterCd = FormManager.getActualValue('apCustClusterId');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (['ESOSW', 'NRML', 'AQSTN', 'SOFT', 'XAQST', 'CROSS'].includes(custSubGrp) && ['04500', '01150', '08039', '09057'].includes(clusterCd)) {
    FormManager.readOnly('repTeamMemberName');
  } else {
    FormManager.enable('repTeamMemberName');
  }
}



// CMR-2830
function addCompanyProofAttachValidation() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqType = FormManager.getActualValue('reqType');
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount == 0 || reqType == 'C') {
         return new ValidationResult(null, true);  
         }
        
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0 ) {
          var record = null;
          var name = null;
          var count = 0;
          var updateInd = null;
                    
              for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
                record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
                if (record == null && _allAddressData != null && _allAddressData[i] != null) {
                  record = _allAddressData[i];
                }
                name = record.custNm1;
                updateInd = record.updateInd;
                              
                if (typeof (type) == 'object') {
                  updateInd = updateInd[0];
                }
                
                if((updateInd == 'U' || updateInd == 'N')){
                  count ++;
                }
                
              }         
          if (count > 0 &&  checkForCompanyProofAttachment() ) {
            return new ValidationResult(null, false, 'Company proof is mandatory since address has been updated or added.');
          } else {
            return new ValidationResult(null, true);
          }
        }
      }
    };
  })(), 'MAIN_ATTACH_TAB', 'frmCMR');
}

function checkForCompanyProofAttachment(){
  var id = FormManager.getActualValue('reqId');
  var ret = cmr.query('CHECK_DNB_MATCH_ATTACHMENT', {
    ID : id
  });
  if (ret == null || ret.ret1 == null) {
    return true;
  }else{
    return false;
  }
}

// CMR-2830
function addressNameSimilarValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount == 0) {
          return new ValidationResult(null, true);
        }
        
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          var record = null;
          var type = null;
          var name = null;
          var name1 = '';
          var name2 = '';
          var count = 0;
          var reqId = FormManager.getActualValue('reqId');
          var reqType = FormManager.getActualValue('reqType');
          var updateInd = null;
          qParams_z = {
              REQ_ID : reqId,
            };
            var record = cmr.query('GETZS01VALRECORDS', qParams_z);
            var zs01Reccount = record.ret1;   
            if(zs01Reccount == 0){
              return new ValidationResult(null, true);   
            }
            var qParams = {
            REQ_ID : reqId,
            ADDR_TYPE : "ZS01",
          };
          var result =  cmr.query('ADDR.GET.CUSTNM1.BY_REQID_ADDRTYP',qParams);
          var zs01Name = result != undefined ? result.ret1.concat(result.ret2): '';
              for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
                record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
                if (record == null && _allAddressData != null && _allAddressData[i] != null) {
                  record = _allAddressData[i];
                }
                type = record.addrType;
                name1 = record.custNm1;
                name2 = record.custNm2;
                updateInd = record.updateInd;
                if (typeof (type) == 'object') {
                  type = type[0];
                }
                if (typeof (type) == 'object') {
                  name1 = name1[0];
                }
                if (typeof (type) == 'object') {
                  name2 = name2[0];
                }
                if (typeof (type) == 'object') {
                  updateInd = updateInd[0];
                }
                name = name1 + name2;
                if(((reqType == 'U' && (updateInd == 'U' || updateInd == 'N')) || reqType == 'C') && type != 'ZS01' && zs01Name != name){
                  count ++;
                }               
              }         
          if (count > 0) {
            return new ValidationResult(null, false, 'All Updated / New address customer name should be same as Mailing address.');
          } else {
            return new ValidationResult(null, true);
          }
        }else{
          return new ValidationResult(null, true); 
        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function addValidatorBasedOnCluster() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var custSubType = FormManager.getActualValue('custSubGrp');
        var cluster = FormManager.getActualValue('apCustClusterId');
        if(FormManager.getActualValue('reqType') != 'C') {
          return new ValidationResult(null, true);
        }
        if ((custSubType != 'ECSYS' && custSubType != 'XECO' && custSubType != 'ASLOM' && custSubType != 'ESOSW' ) && (cluster == '08039' || cluster == '08037' || cluster == '08038' || cluster == '08040' || cluster == '08042' ||cluster == '08044' || cluster == '08047'|| cluster == '08046')) {
          return new ValidationResult(null, false, 'Ecosystem Partners Cluster is not allowed for selected scenario.');
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function getAbbrvNameForBP(custNm1, bpRelTypeValChange) {
  var abbvNmRegex = /^(Dis|Sol|Res)(1|2|3)_/;
  var abbvNmVal = FormManager.getActualValue('abbrevNm');
  var bpRelPrefix = '';

  if(!abbvNmRegex.test(abbvNmVal) || bpRelTypeValChange) {
    var bpRelTypeVal = FormManager.getActualValue('bpRelType');
    if(bpRelTypeVal == 'DS') {
      bpRelPrefix = 'Dis2_';
    } else if (bpRelTypeVal == 'SP') {
      bpRelPrefix = 'Sol1_';
    } else if(bpRelTypeVal == 'RS') {
      bpRelPrefix = 'Res3_';
    }
  }
  
  if(bpRelPrefix != '') {
    return bpRelPrefix + custNm1;  
  } else {
    return custNm1;
  }
}

function setAbbrvNameBPScen() {
  var scenario = FormManager.getActualValue('custSubGrp');
  
  if(scenario == 'BUSPR' || scenario == 'XBUSP') {
    var zs01ReqId = FormManager.getActualValue('reqId');
    var qParams = {
      REQ_ID : zs01ReqId,
    };
    var result = cmr.query('ADDR.GET.CUSTNM1.BY_REQID', qParams);
    var custNm1 = FormManager.getActualValue('custNm1');
    if (custNm1 == '') {
      custNm1 = result.ret1;
    }
    var abbvName = getAbbrvNameForBP(custNm1, true);
    if (abbvName && abbvName.length > 21) {
      abbvName = abbvName.substring(0, 21);
    }
    FormManager.setValue('abbrevNm', abbvName);
  }
}

function lockAbbvNameOnScenarioChangeGCG() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var reqType = FormManager.getActualValue('reqType');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');

  if (role == 'REQUESTER' && reqType == 'C' && (cntry == SysLoc.HONG_KONG || cntry == SysLoc.MACAO)) {
    FormManager.readOnly('abbrevNm');
  } 
}

function handleExpiredClusterGCG() {
  var reqType = FormManager.getActualValue('reqType');
  if (reqType != 'U' || FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  
  var clusterDataRdc = getAPClusterDataRdc();
  if (clusterDataRdc != null && clusterDataRdc != undefined && clusterDataRdc != '') {
    var clusterExpired = checkClusterExpired(clusterDataRdc);
    if (clusterExpired) {
      FormManager.readOnly('clientTier');
    }
  }
}

function getAPClusterDataRdc() {
  var clusterDataRdc = '';
  var reqId = FormManager.getActualValue('reqId');
  var qParams = {
    REQ_ID : reqId,
  };
  var result = cmr.query('GET.CLUSTER_DATA_RDC', qParams);
  if (result != null) {
    clusterDataRdc = result.ret1;
  }
  return clusterDataRdc;
}

function checkClusterExpired(clusterDataRdc) {
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var qParams = {
      ISSUING_CNTRY : cntry,
      AP_CUST_CLUSTER_ID : clusterDataRdc,
  };
  var results = cmr.query('CHECK.CLUSTER', qParams);
  if (results != null && results.ret1 == '1') {
    return false;
  }
  return true;
}

function checkExpiredData() {
  var reqId = FromManager.getActualValue('reqId')
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var DataRdc ={};
  
  var qParams = {
      REQ_ID : reqId,
  };
  var records = cmr.query('SUMMARY.OLDDATA', qParams);
  if (records != null && records.size()>0) {
    return false;
  }
  return true;
}

function addCtcObsoleteValidator() {
    FormManager.addFormValidator((function() {
        return {
            validate : function() {
              var reqType = FormManager.getActualValue('reqType');
              var reqId = FormManager.getActualValue('reqId');
              var clientTier = FormManager.getActualValue('clientTier');
              var oldCtc;
              var qParams = {
               REQ_ID : reqId
               };

        var result = cmr.query('GET.DATA_RDC.CLIENT_TIER_REQID', qParams);
        if (result != null && result != '') {
         var oldCtc = result.ret1;
        }

        if (reqType == 'C' && (clientTier == "4" ||clientTier == "6"|| clientTier == "A" || clientTier == "B"  ||clientTier == "M"|| clientTier == "V" || clientTier == "T" || clientTier == "S" || clientTier == "N" || clientTier == "C" )) {
           return new ValidationResult(null, false, 'Client tier is obsoleted. Please select valid value from list.');
          } else if (reqType == 'U' && oldCtc != null && oldCtc != clientTier && (clientTier == "4" ||clientTier == "6"|| clientTier == "A" || clientTier == "B" ||clientTier == "M"|| clientTier == "V" || clientTier == "T" || clientTier == "S" || clientTier == "N" || clientTier == "C")) {
           return new ValidationResult(null, false, 'Client tier is obsoleted. Please select valid Client tier value from list.');
           } else {
             return new ValidationResult(null, true);
            }
          }
        }
    })(), 'MAIN_IBM_TAB', 'frmCMR');
 }

function clusterCdValidatorAU() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var clusterCd = FormManager.getActualValue('apCustClusterId');
        var custSubGrp = FormManager.getActualValue('custSubGrp');

        if (['ESOSW', 'NRML', 'AQSTN', 'SOFT', 'XAQST', 'CROSS'].includes(custSubGrp)) {
          if (clusterCd == '00001') {
            return new ValidationResult({
              id : 'apCustClusterId',
              type : 'text',
              name : 'apCustClusterId'
            }, false, 'Please enter valid Cluster Code, for this scenario request should not be default \'00001\'.');
          } else {
            return new ValidationResult(null, true);
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function validateClusterBaseOnScenario() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var custSubType = FormManager.getActualValue('custSubGrp');
        var cluster = FormManager.getActualValue('apCustClusterId');
        if(FormManager.getActualValue('reqType') != 'C') {
          return new ValidationResult(null, true);
        }
        var applicableScenarios = [ "ASLOM", "BUSPR", "NRML", "CROSS", "AQSTN", "XAQST" ];
        if (applicableScenarios.includes(custSubType) && cluster == '00000') {
          return new ValidationResult(null, false, "Cluster '00000 - Singapore Default' is not allowed on ASL/OEM, Acquisition, Business Partner, Foreign and Normal scenario sub-type.");
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function validateCustNameForInternal() {
  console.log("running validateCustNameForInternal . . .");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var custSubType = FormManager.getActualValue('custSubGrp');
          if (custSubType == 'INTER') {
            var custNm = '';
            for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
              record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
              type = record.addrType;

              if (typeof (type) == 'object') {
                type = type[0];
              }
              
              custNm = record.custNm1 == null ? '' : record.custNm1;
              custNm = custNm.toString().toUpperCase();
              console.log("validateCustNameForInternal Customer name value is " + custNm);
            }
            var result = custNm.startsWith("IBM");
            if (!result){
              return new ValidationResult(null, false, "Customer Name should start with 'IBM' for Internal Sub-scenario.");
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

function getImportIndForIndia(reqId) {
  var results = cmr.query('VALIDATOR.IMPORTED_IN', {
    REQID : reqId
  });
  var importInd = 'N';
  if (results != null && results.ret1) {
    importInd = results.ret1;
  }
  console.log('import indicator value for request ID: ' + reqId + ' is ' + importInd);
  return importInd;
}

function lockInacTypeForIGF() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var reqId = FormManager.getActualValue('reqId');

  if (role == 'REQUESTER' && custSubGrp == 'IGF') {
    if (_importIndIN != null) {
      if (_importIndIN == 'Y') {
        FormManager.setValue('inacType','');
        FormManager.readOnly('inacType');
      }
    }
  }
}

function lockInacCodeForIGF() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var reqId = FormManager.getActualValue('reqId');

  if (role == 'REQUESTER' && custSubGrp == 'IGF') {
    var existingCmr = getImportIndForIndia(reqId);
    if (existingCmr == 'Y') {
      _importIndIN =  existingCmr;
      FormManager.setValue('inacCd', '');
      FormManager.readOnly('inacCd');
      lockInacTypeForIGF();
    }
  }
}

// CMR - 5258
function addVatValidationforSingapore() {
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var flag = "N";
  if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
    for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
      record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
      landCntry = record.landCntry;
      if (landCntry == 'TH') {
        flag = "Y";
      }
    }
  }
  if (cntry == '834' && flag == "Y") {
    FormManager.addValidator('vat', Validators.REQUIRED, [ 'UEN#' ], 'MAIN_CUST_TAB');
  }
}
// CREATCMR-5258
// CREATCMR -5269
function handleObseleteExpiredDataForUpdate() {
 var reqType = FormManager.getActualValue('reqType');
 var cntry = FormManager.getActualValue('cmrIssuingCntry');
 if (reqType != 'U' || FormManager.getActualValue('viewOnlyPage') == 'true' || cntry == SysLoc.HONG_KONG || cntry ==  SysLoc.MACAO) {
   return;
 }
 // lock all the coverage fields and remove validator
 if (reqType == 'U') {
   FormManager.readOnly('apCustClusterId');
   FormManager.readOnly('clientTier');
   FormManager.readOnly('mrcCd');
   FormManager.readOnly('inacType');
   FormManager.readOnly('isuCd');
   FormManager.readOnly('inacCd');
   FormManager.readOnly('repTeamMemberNo');
   FormManager.readOnly('repTeamMemberName');
   FormManager.readOnly('isbuCd');
   FormManager.readOnly('covId');
   FormManager.readOnly('cmrNoPrefix');
   FormManager.readOnly('collectionCd');
   FormManager.readOnly('engineeringBo');
   FormManager.readOnly('commercialFinanced');
   FormManager.readOnly('creditCd');
   FormManager.readOnly('contactName2');
   FormManager.readOnly('contactName3');
   FormManager.readOnly('busnType');
   FormManager.readOnly('taxCd2');
   FormManager.readOnly('cmrOwner');

// setting all fields as not Mandt for update Req
   FormManager.removeValidator('apCustClusterId', Validators.REQUIRED);
   FormManager.removeValidator('cmrNoPrefix', Validators.REQUIRED);
   FormManager.removeValidator('taxCd2', Validators.REQUIRED);
   FormManager.removeValidator('cmrOwner', Validators.REQUIRED);
   FormManager.removeValidator('clientTier', Validators.REQUIRED);
   FormManager.removeValidator('isuCd', Validators.REQUIRED);
   FormManager.removeValidator('mrcCd', Validators.REQUIRED);
   FormManager.removeValidator('inacType', Validators.REQUIRED);
   FormManager.removeValidator('inacCd', Validators.REQUIRED);   
   FormManager.removeValidator('repTeamMemberNo', Validators.REQUIRED);
   FormManager.removeValidator('repTeamMemberName', Validators.REQUIRED);
   FormManager.removeValidator('isbuCd', Validators.REQUIRED);
   FormManager.removeValidator('covId', Validators.REQUIRED);
   FormManager.removeValidator('collectionCd', Validators.REQUIRED);
   FormManager.removeValidator('engineeringBo', Validators.REQUIRED);
   FormManager.removeValidator('commercialFinanced', Validators.REQUIRED);
   FormManager.removeValidator('creditCd', Validators.REQUIRED);
   FormManager.removeValidator('contactName2', Validators.REQUIRED);
   FormManager.removeValidator('contactName3', Validators.REQUIRED);
   FormManager.removeValidator('busnType', Validators.REQUIRED);
 } 
}

function executeBeforeSubmit() {
  var reqType = FormManager.getActualValue('reqType');
  var action = FormManager.getActualValue('yourAction');

  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  if (cntry == SysLoc.SINGAPORE || cntry == SysLoc.INDIA) {
    if (reqType == 'U') {
      var errMsg = checkAnyChangesOnCustNameAddrGST(cntry);
      if (errMsg != '' && action == 'SFP') {
        cmr.showAlert(errMsg);
      } else {
        showVerificationModal();
      }
    } else {
      showVerificationModal();
    }
  }
}

function showVerificationModal() {
  cmr.showModal('addressVerificationModal');
}

function checkAnyChangesOnCustNameAddrGST(cntry) {
  var errorMsg = '';
  var isUpdated = false;
  
  if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
    var record = null;
    var updateInd = null;
    
    for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
      record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
      updateInd = record.updateInd;
      if (typeof (updateInd) == 'object') {
        updateInd = updateInd[0];
        if(updateInd == 'U' || updateInd == 'N') {
          isUpdated = true;
          break;
        }
      }
    }
  }
  if (!isUpdated) {
    var currentGst = FormManager.getActualValue('vat'); 
    var qParams = {
        REQ_ID : FormManager.getActualValue('reqId'),
    };
    
    var result = cmr.query('GET.OLD_VAT_BY_REQID', qParams);
    var oldGst = result.ret1;
    oldGst = oldGst == undefined ? '' : oldGst;
    
    if (result != null && oldGst != null && oldGst != currentGst) {
      isUpdated = true;
    }
  }
  if (!isUpdated) {
    if (cntry != '') {
      if (cntry == SysLoc.SINGAPORE) {
        errorMsg = 'You haven\'t updated anything on customer name/address or UEN#, please check and take relevant edit operation before submit this Update request.';
      } else if (cntry == SysLoc.INDIA) {
        errorMsg = 'You haven\'t updated anything on customer name/address or GST#, please check and take relevant edit operation before submit this Update request.';
      }
    }
  }
  return errorMsg;
}

// CREATCMR-6880
function vatRegistrationForSG() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var reqId = FormManager.getActualValue('reqId');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var addrType = FormManager.getActualValue('addrType');
  var landCntry ='';
  if (addrType == 'ZS01') {
    landCntry = FormManager.getActualValue('landCntry'); 
  }
  if (landCntry == '') {
  var params = {
      REQ_ID : reqId,
      ADDR_TYPE : "ZS01"
    };
    var landCntryResult = cmr.query('ADDR.GET.LAND_CNTRY.BY_REQID', params);
    landCntry = landCntryResult.ret1;
  }
  if (cntry == '834' && landCntry == 'TH') {
  FormManager.addValidator('taxCd1', Validators.REQUIRED, [ 'Vat Registration Status' ], 'MAIN_IBM_TAB');
  var isVatRegistered =  FormManager.getActualValue('taxCd1');
  if (isVatRegistered == 'NA') {
    FormManager.readOnly('vat');
    FormManager.setValue('vat', '');
    FormManager.removeValidator('vat', Validators.REQUIRED);
  } else {
    FormManager.addValidator('vat', Validators.REQUIRED, [ 'VAT' ], 'MAIN_IBM_TAB');
    FormManager.enable('vat');
  }
  }
}

function displayVatRegistrartionStatus() {
  console.log(">>> Executing displayVatRegistrationStatus");
  var reqId = FormManager.getActualValue('reqId');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var addrType = FormManager.getActualValue('addrType');
  var landCntry ='';
  if (addrType == 'ZS01') {
    landCntry = FormManager.getActualValue('landCntry'); 
  }
  if (landCntry == '') {
  var params = {
      REQ_ID : reqId,
      ADDR_TYPE : "ZS01"
    };
    var landCntryResult = cmr.query('ADDR.GET.LAND_CNTRY.BY_REQID', params);
    landCntry = landCntryResult.ret1;
  }
  if (cntry == '834' && landCntry == 'TH') {
    cmr.showNode('vatRegisterStatus');
  } else {
    cmr.hideNode('vatRegisterStatus');
  }
  vatRegistrationForSG();
}

// CREATCMR-6398
function businessParterValidator() {
  console.log("running businessParterValidator...");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqType = FormManager.getActualValue('reqType');
        var custSubType = FormManager.getActualValue('custSubGrp');
        var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
        if (typeof (_pagemodel) != 'undefined') {
          if (cmrIssuingCntry == '834' && reqType == 'C' && (custSubType == 'BUSPR' || custSubType == 'XBUSP')) {
            var id = FormManager.getActualValue('reqId');
            var ret = cmr.query('CHECK_BUSP_MATCH_ATTACHMENT', {
              ID : id
            });

            if (ret == null || ret.ret1 == null || ret.ret1 == 0) {
              return new ValidationResult(null, false, 'Business Partner Proof attachment is required for Scenario Sub-type \'Business Partner\'.');
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

// CREATCMR-6358
function addressNameSameValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount == 0) {
          return new ValidationResult(null, true);
        }
        
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          var record = null;
          var type = null;
          var name = null;
          var name1 = '';
          var name2 = '';
          var count = 0;
          var reqId = FormManager.getActualValue('reqId');
          var reqType = FormManager.getActualValue('reqType');
          // var updateInd = null;
          qParams_z = {
              REQ_ID : reqId,
            };
            var record = cmr.query('GETZS01VALRECORDS', qParams_z);
            var zs01Reccount = record.ret1;   
            if(zs01Reccount == 0){
              return new ValidationResult(null, true);   
            }
            var qParams = {
            REQ_ID : reqId,
            ADDR_TYPE : "ZS01",
          };
          var result =  cmr.query('ADDR.GET.CUSTNM1.BY_REQID_ADDRTYP',qParams);
          var zs01Name = result != undefined ? result.ret1.concat(result.ret2): '';
              for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
                record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
                if (record == null && _allAddressData != null && _allAddressData[i] != null) {
                  record = _allAddressData[i];
                }
                type = record.addrType;
                name1 = record.custNm1;
                name2 = record.custNm2;
                // updateInd = record.updateInd;
                if (typeof (type) == 'object') {
                  type = type[0];
                }
                if (typeof (type) == 'object') {
                  name1 = name1[0];
                }
                if (typeof (type) == 'object') {
                  name2 = name2[0];
                }
                // if (typeof (type) == 'object') {
                // updateInd = updateInd[0];
                // }
                name = name1 + name2;
                // if(((reqType == 'U' && (updateInd == 'U' || updateInd ==
                // 'N')) || reqType == 'C') && type != 'ZS01' && zs01Name !=
                // name){
                if((reqType == 'U' || reqType == 'C') && type != 'ZS01' && zs01Name != name){
                  count ++;
                }               
              }         
          if (count > 0) {
            return new ValidationResult(null, false, 'All Additional address customer name should be same as Mailing address.');
          } else {
            return new ValidationResult(null, true);
          }
        }else{
          return new ValidationResult(null, true); 
        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

// CREATCMR-6358
function addCompanyProofForSG() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqType = FormManager.getActualValue('reqType');
        var hasAdditionalAddr = false;
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount == 0) {
         return new ValidationResult(null, true);  
         }
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0 ) {
          for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
            if (CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i).addrType != 'ZS01') {
              hasAdditionalAddr = true;
              break;
            }
          }
        }
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0 ) {
          var record = null;
          var name = null;
          var count = 0;
          var updateInd = null;
                    
          for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
            record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
            if (record == null && _allAddressData != null && _allAddressData[i] != null) {
              record = _allAddressData[i];
            }
            name = record.custNm1;
            updateInd = record.updateInd;
                          
            if (typeof (type) == 'object') {
              updateInd = updateInd[0];
            }
            
            if((updateInd == 'U' || updateInd == 'N')){
              count ++;
            }
            
          }         
          if ((count > 0 || hasAdditionalAddr) &&  checkForCompanyProofAttachment() ) {
            return new ValidationResult(null, false, 'Company proof is mandatory since address has been updated or added.');
          } else {
            return new ValidationResult(null, true);
          }
        }
      }
    };
  })(), 'MAIN_ATTACH_TAB', 'frmCMR');
}

// CREATCMR-6825
function setRepTeamMemberNo() {
  var reqType = FormManager.getActualValue('reqType');
  if (reqType == 'C') {
    FormManager.setValue('repTeamMemberNo', '000000');
    FormManager.readOnly('repTeamMemberNo');
    FormManager.readOnly('repTeamMemberName');
  }
}

// CREATCMR-6358
function additionalAddrNmValidator(){
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var currentCustNm1 = FormManager.getActualValue('custNm1');
        var currentCustNm2 = FormManager.getActualValue('custNm2');
        var currentCustNm = currentCustNm1 + currentCustNm2;

        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          var record = null;
          var type = null;
          var name = null;
          var name1 = '';
          var name2 = '';
          var count = 0;
          var reqId = FormManager.getActualValue('reqId');
          qParams_z = {
              REQ_ID : reqId,
          };
          var record = cmr.query('GETZS01VALRECORDS', qParams_z);
          var zs01Reccount = record.ret1;   
          if(zs01Reccount == 0){
            return new ValidationResult(null, true);   
          }
          var qParams = {
          REQ_ID : reqId,
          ADDR_TYPE : "ZS01",
          };
          var result =  cmr.query('ADDR.GET.CUSTNM1.BY_REQID_ADDRTYP',qParams);
          var zs01Name = result != undefined ? result.ret1.concat(result.ret2): '';
          
          if(!FormManager.getField('addrType_ZS01').checked && currentCustNm1 != '' && currentCustNm != zs01Name){
            return new ValidationResult({
              id : 'custNm1',
              type : 'text',
              name : 'custNm1'
            }, false, 'Additional address customer name should be same as Mailing address.');
          }
          return new ValidationResult(null, true);
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

// CREATCMR-788
function addressQuotationValidatorAP() {
  
  var cntry = FormManager.getActualValue('cmrIssuingCntry')
  var AP01 = [ SysLoc.BRUNEI ,SysLoc.SRI_LANKA ,SysLoc.INDIA ,SysLoc.INDONESIA,SysLoc.MALASIA,SysLoc.PHILIPPINES,SysLoc.BANGLADESH,SysLoc.SINGAPORE,SysLoc.VIETNAM,SysLoc.THAILAND];
  if (AP01.indexOf(cntry) > -1) {
    FormManager.addValidator('custNm1', Validators.NO_QUOTATION, [ 'Customer Name' ]);
    FormManager.addValidator('custNm2', Validators.NO_QUOTATION, [ 'Customer Name Con\'t' ]);
    FormManager.addValidator('addrTxt', Validators.NO_QUOTATION, [ 'Address.(Flr,Bldg,lvl,Unit.)' ]);
    FormManager.addValidator('addrTxt2', Validators.NO_QUOTATION, [ 'Address.Cont1(Street Name, Street No.)' ]);
    FormManager.addValidator('dept', Validators.NO_QUOTATION, [ 'Address.Cont2(District,Town,Region)' ]);
    FormManager.addValidator('postCd', Validators.NO_QUOTATION, [ 'Postal Code' ]);
    if (cntry == SysLoc.INDONESIA || cntry == SysLoc.MALASIA  || cntry == SysLoc.PHILIPPINES) {
      FormManager.addValidator('city1', Validators.NO_QUOTATION, [ 'City/State/Province' ]);
    } else if(cntry == SysLoc.THAILAND) {
      FormManager.addValidator('city1', Validators.NO_QUOTATION, [ 'Street Address Cont\'2' ]);
    } else {
      FormManager.addValidator('city1', Validators.NO_QUOTATION, [ 'City' ]);
    }
  } else if (cntry == SysLoc.AUSTRALIA || cntry == SysLoc.NEW_ZEALAND) {
    FormManager.addValidator('custNm1', Validators.NO_QUOTATION, [ 'Customer Name' ]);
    FormManager.addValidator('custNm2', Validators.NO_QUOTATION, [ 'Customer Name Con\'t' ]);
    FormManager.addValidator('dept', Validators.NO_QUOTATION, [ 'Attn' ]);
    FormManager.addValidator('addrTxt', Validators.NO_QUOTATION, [ 'Street Address' ]);
    FormManager.addValidator('addrTxt2', Validators.NO_QUOTATION, [ 'Street Address Con\'t' ]);
    FormManager.addValidator('city1', Validators.NO_QUOTATION, [ 'Suburb' ]);
    FormManager.addValidator('postCd', Validators.NO_QUOTATION, [ 'Postal Code' ]);
  }
  FormManager.addValidator('abbrevNm', Validators.NO_QUOTATION, [ 'Abbreviated Name (TELX1)' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('abbrevLocn', Validators.NO_QUOTATION, [ 'Abbreviated Location' ], 'MAIN_CUST_TAB');
}
function setLockIsicNZfromDNB() {
  
  var isDnbRecord = FormManager.getActualValue('findDnbResult');
  if (isDnbRecord =='Accepted' && FormManager.getActualValue('isicCd') != '') {
    FormManager.readOnly('isicCd');
  } else {
    FormManager.enable('isicCd');
  }
  
}

function additionalAddrNmValidatorOldNZ(){
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount == 0) {
          return new ValidationResult(null, true);
        }
        
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          var record = null;
          var type = null;
          var name = null;
          var name1 = '';
          var name2 = '';
          var count = 0;
          var reqId = FormManager.getActualValue('reqId');
          var reqType = FormManager.getActualValue('reqType');
          var updateInd = null;
          qParams_z = {
              REQ_ID : reqId,
            };
            var record = cmr.query('GETZS01VALRECORDS', qParams_z);
            var zs01Reccount = record.ret1;   
            if(zs01Reccount == 0){
              return new ValidationResult(null, true);   
            }
            var qParams = {
            REQ_ID : reqId,
            ADDR_TYPE : "ZS01",
          };
          var result1 =  cmr.query('ADDR.GET.CUSTNM1.BY_REQID_ADDRTYP',qParams);
          var zs01Name = result1 != undefined ? result1.ret1.concat(result1.ret2): '';
              for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
                record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
                if (record == null && _allAddressData != null && _allAddressData[i] != null) {
                  record = _allAddressData[i];
                }
                type = record.addrType;
                name1 = record.custNm1;
                name2 = record.custNm2;
                updateInd = record.updateInd;
                if (typeof (type) == 'object') {
                  type = type[0];
                }
                if (typeof (type) == 'object') {
                  name1 = name1[0];
                }
                if (typeof (type) == 'object') {
                  name2 = name2[0];
                }
                if (typeof (type) == 'object') {
                  updateInd = updateInd[0];
                }
                name = name1 + name2;
                if ((reqType == 'U' || reqType == 'C') && type != 'ZS01' && zs01Name != name){
                  count ++;
                }               
              }         
          if (count > 0) {
            return new ValidationResult(null, false, 'All Updated / New address customer name should be same as Installing address.');
          } else {
            return new ValidationResult(null, true);
          }
        }else{
          return new ValidationResult(null, true); 
        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function additionalAddrNmValidatorNZ(){
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var addrTypeMod = FormManager.getActualValue('addrType');
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount == 0) {
          return new ValidationResult(null, true);
        }
        
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0 && addrTypeMod != 'ZS01') {
          var record = null;
          var name = ''
          var name1 = FormManager.getActualValue('custNm1');
          var name2 = FormManager.getActualValue('custNm2');;
          var reqId = FormManager.getActualValue('reqId');
          var reqType = FormManager.getActualValue('reqType');
          qParams_z = {
              REQ_ID : reqId,
            };
           var record = cmr.query('GETZS01VALRECORDS', qParams_z);
           var zs01Reccount = record.ret1;   
           if(zs01Reccount == 0){
              return new ValidationResult(null, true);   
           }
            var qParams = {
            REQ_ID : reqId,
            ADDR_TYPE : "ZS01",
          };
          var result1 =  cmr.query('ADDR.GET.CUSTNM1.BY_REQID_ADDRTYP',qParams);
          var zs01Name = result1 != undefined ? result1.ret1.concat(result1.ret2): '';
          name = name1 + name2;               
          if (zs01Name != name) {
            return new ValidationResult(null, false, 'All Updated / New address customer name should be same as Installing address.');
          } else {
            return new ValidationResult(null, true);
          }
        }else{
          return new ValidationResult(null, true); 
        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR_addressModal');
}

// CREATCMR-7658
function addValidatorforInstallingNZ(){
  FormManager.addFormValidator((function(){
    return {
      validate : function(){
        var addrType = FormManager.getActualValue('addrType');
        var name1 = FormManager.getActualValue('custNm1').toUpperCase();
        var name2 = FormManager.getActualValue('custNm2').toUpperCase();
        var attn = FormManager.getActualValue('dept').toUpperCase();
        var street = FormManager.getActualValue('addrTxt').toUpperCase();
        var street2 = FormManager.getActualValue('addrTxt2').toUpperCase();
        var suburb = FormManager.getActualValue('city1').toUpperCase();
        var address = name1 + name2 + attn + street + street2 + suburb;
        if((address.includes("PO BOX") || address.includes("POBOX")) && addrType == 'ZS01'){
          return new ValidationResult(null, false, "NZ Installing address can't contain \'PO BOX\'");
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function addressQuotationValidatorGCG() {
  
  var cntry = FormManager.getActualValue('cmrIssuingCntry')
  
  FormManager.addValidator('abbrevNm', Validators.NO_QUOTATION, [ 'Abbreviated Name (TELX1)' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('abbrevLocn', Validators.NO_QUOTATION, [ 'Abbreviated Location' ], 'MAIN_CUST_TAB');
  switch (cntry) {
  case SysLoc.MACAO: case SysLoc.HONG_KONG:
    FormManager.addValidator('custNm1', Validators.NO_QUOTATION, [ 'Customer Name' ]);
    FormManager.addValidator('custNm2', Validators.NO_QUOTATION, [ 'Customer Name Con\'t' ]);
    FormManager.addValidator('addrTxt', Validators.NO_QUOTATION, [ 'Address.(Flr,Bldg,lvl,Unit.)' ]);
    FormManager.addValidator('addrTxt2', Validators.NO_QUOTATION, [ 'Address. Cont1(Street Name, Street No.)' ]);
    FormManager.addValidator('city1', Validators.NO_QUOTATION, [ 'Address. Cont2(District,Town,Region)' ]);
    FormManager.addValidator('postCd', Validators.NO_QUOTATION, [ 'Postal Code' ]);
    break;
  }
}

var _customerTypeHandler = null;
function addCustGrpHandler() {
  if (_customerTypeHandler == null) {
    _customerTypeHandler = dojo.connect(FormManager.getField('custGrp'), 'onChange', function(value) {
      var cntry = FormManager.getActualValue('cmrIssuingCntry');
      var custGrp = FormManager.getActualValue('custGrp');
      var reqType = FormManager.getActualValue('reqType');
      var apaCntry = [ '834', '818', '856', '778', '749', '643', '852', '744', '615', '652', '616', '796', '641', '738', '736', '858', '766' ];
      if (reqType == 'C' && custGrp == 'CROSS' && apaCntry.includes(cntry)) {
        FormManager.setValue('custSubGrp', 'CROSS');
      }
    });
  }
}

// CREATCMR-7655
function resetNZNBExempt() {
  if (dijit.byId('vatExempt') != undefined && dijit.byId('vatExempt').get('checked')) {
    console.log(">>> Process nznbExempt remove * >> ");
    FormManager.setValue("vat", "");
    FormManager.resetValidations('vat');
    FormManager.readOnly('vat');
  } else {
  	console.log(">>> Process nzbnExempt add * >> ");
  	var custSubGrp = FormManager.getActualValue('custSubGrp');
    FormManager.enable('vat');
    if(!(custSubGrp == 'CROSS' || custSubGrp == 'DUMMY' || custSubGrp == 'INTER' || custSubGrp == 'PRIV')){
      FormManager.addValidator('vat', Validators.REQUIRED, [ 'New Zealand Business#' ], 'MAIN_CUST_TAB');
    } else {
      FormManager.removeValidator('vat', Validators.REQUIRED);
    }
  }
}

function afterConfigForNewZeaLand() { 
  console.log("----After config. for New Zealand----");
  if (_vatExemptHandlerNZ == null) {
    _vatExemptHandlerNZ = dojo.connect(FormManager.getField('vatExempt'), 'onClick', function(value) {
      console.log(">>> RUNNING!!!!");
      var custSubGrp = FormManager.getActualValue('custSubGrp');
      FormManager.resetValidations('vat');
      if (dijit.byId('vatExempt').get('checked')) {
        console.log(">>> Process nzbnExempt remove * >> ");
        FormManager.readOnly('vat');
        FormManager.setValue('vat', '');
      } else {      
        console.log(">>> Process nzbnExempt add * >> ");
        FormManager.enable('vat');
        if(!(custSubGrp == 'CROSS' || custSubGrp == 'DUMMY' || custSubGrp == 'INTER' || custSubGrp == 'PRIV')){
          FormManager.addValidator('vat', Validators.REQUIRED, [ 'New Zealand Business#' ], 'MAIN_CUST_TAB');
        } else {
          FormManager.removeValidator('vat', Validators.REQUIRED);
        }
      }
    });
  }  
}

function validatNZBNForNewZeaLand() {
  console.log('starting validatNZBNForNewZeaLand ...');
  FormManager.addFormValidator((function() {
	return {
	  validate: function() {
		var vat = FormManager.getActualValue('vat');
		var reqId = FormManager.getActualValue('reqId');
		var custNm = '';
		if (dijit.byId('vatExempt') && dijit.byId('vatExempt').get('checked')) {
		  return new ValidationResult(null, true);
		}
		
		if (vat == '') {
		  return new ValidationResult(null, true);
		} else {
		  var contractCustNm = cmr.query('GET.CUSTNM_ADDR', {
		    REQ_ID : reqId,
		    ADDR_TYPE : 'ZS01'
		  });
		  if (contractCustNm != undefined) {
		    custNm = contractCustNm.ret1.toUpperCase() + " " + contractCustNm.ret2.toUpperCase();
		  }
          custNm = custNm.trim();
		  var nzbnRet = cmr.validateNZBNFromAPI(vat, reqId, custNm);
		  console.log(nzbnRet);
          if (!nzbnRet.success || !nzbnRet.custNmMatch) {
          	return new ValidationResult({
              id : 'vat',
              type : 'text',
              name : 'vat'
          	}, false, nzbnRet.message);
	      } else {
	        return new ValidationResult(null, true);
	      }
		}
	  }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

// CREATCMR-7653 : NZ 2.0 - Customer/IBM tab assignments based on Scenario
function setDefaultValueForNZCreate(){
  console.log(">>>>setDefaultValueForNZCreate()>>>>");
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var reqId = FormManager.getActualValue('reqId');
  var isicCdInDB = '';
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var custSubGroups = ['BLUMX', 'MKTPC', 'AQSTN', 'NRML', 'ESOSW', 'ECSYS', 'CROSS', 'XAQST', 'XBLUM', 'XESO', 'XMKTP'];
  console.log(">>>>setDefaultValueForNZCreate()>>>> SubGrp="+custSubGrp);
  if(custSubGroups.includes(custSubGrp)){
    var reqIdParams = {
        REQ_ID : reqId,
      };
      var isicCdResult = cmr.query('GET.ISIC_CD_BY_REQID', reqIdParams);

      if (isicCdResult.ret1 != undefined) {
        isicCdInDB = isicCdResult.ret1;
      }
      console.log(">>>>setDefaultValueForNZCreate()>>>> ISIC_CD in DB ="+isicCdInDB); 
  }
  
    switch (custSubGrp) {
    case 'PRIV':
      // ISIC = 9500, - lock field
      FormManager.setValue('isicCd', '9500');
      FormManager.readOnly('isicCd');
      // cluster/QTC/ISU: default as 00002/Q/34 - lock field
      FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), [ '00002' ]);
      FormManager.limitDropdownValues(FormManager.getField('clientTier'), [ 'Q' ]);
      FormManager.limitDropdownValues(FormManager.getField('isuCd'), [ '34' ]);
      FormManager.setValue('apCustClusterId', '00002');
      FormManager.readOnly('apCustClusterId');
      FormManager.setValue('clientTier', 'Q');
      FormManager.readOnly('clientTier');
      FormManager.setValue('isuCd', '34');
      FormManager.readOnly('isuCd');
      // MRC = 3 - lock field
      FormManager.setValue('mrcCd', '3');
      FormManager.readOnly('mrcCd');
      // Collection code = 00JC - lock field
      FormManager.setValue('collectionCd', '00JC');
      FormManager.readOnly('collectionCd');
      break;
    case 'BLUMX':
      // cluster/QTC/ISU: default as 00002/Q/34 - lock field
      FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), [ '00002' ]);
      FormManager.limitDropdownValues(FormManager.getField('clientTier'), [ 'Q' ]);
      FormManager.limitDropdownValues(FormManager.getField('isuCd'), [ '34' ]);
      FormManager.setValue('apCustClusterId', '00002');
      FormManager.readOnly('apCustClusterId');
      FormManager.setValue('clientTier', 'Q');
      FormManager.readOnly('clientTier');
      FormManager.setValue('isuCd', '34');
      FormManager.readOnly('isuCd');
      // MRC = 3 - lock field
      FormManager.setValue('mrcCd', '3');
      FormManager.readOnly('mrcCd');
      // Collection code = 00JC - lock field
      FormManager.setValue('collectionCd', '00JC');
      FormManager.readOnly('collectionCd');
      
      FormManager.setValue('abbrevNm', 'Bluemix use only');
      FormManager.readOnly('abbrevNm');
      
      FormManager.setValue('isicCd', isicCdInDB); 
      break;
    case 'MKTPC':
      // cluster/QTC/ISU: default as 00002/Q/34 - lock field
      FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), [ '00002' ]);
      FormManager.limitDropdownValues(FormManager.getField('clientTier'), [ 'Q' ]);
      FormManager.limitDropdownValues(FormManager.getField('isuCd'), [ '34' ]);
      FormManager.setValue('apCustClusterId', '00002');
      FormManager.readOnly('apCustClusterId');
      FormManager.setValue('clientTier', 'Q');
      FormManager.readOnly('clientTier');
      FormManager.setValue('isuCd', '34');
      FormManager.readOnly('isuCd');
      // MRC = 3 - lock field
      FormManager.setValue('mrcCd', '3');
      FormManager.readOnly('mrcCd');
      // Collection code = 00JC - lock field
      FormManager.setValue('collectionCd', '00JC');
      FormManager.readOnly('collectionCd');
      
      FormManager.setValue('abbrevNm', 'Market place use only');
      FormManager.readOnly('abbrevNm');
      
      FormManager.setValue('isicCd', isicCdInDB);
      break;
    case 'INTER':
      // ISIC = 8888, - lock field
      FormManager.setValue('isicCd', '8888');
      FormManager.readOnly('isicCd');
      // cluster/QTC/ISU: default as 00002/Z/21 - lock field
      FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), [ '00002' ]);
      FormManager.limitDropdownValues(FormManager.getField('clientTier'), [ 'Z' ]);
      FormManager.limitDropdownValues(FormManager.getField('isuCd'), [ '21' ]);
      FormManager.setValue('apCustClusterId', '00002');
      FormManager.readOnly('apCustClusterId');
      FormManager.setValue('clientTier', 'Z');
      FormManager.readOnly('clientTier');
      FormManager.setValue('isuCd', '21');
      FormManager.readOnly('isuCd');
      // MRC = 2 - lock field
      FormManager.setValue('mrcCd', '2');
      FormManager.readOnly('mrcCd');
      // Collection code = 00IL - lock field
      FormManager.setValue('collectionCd', '00IL');
      FormManager.readOnly('collectionCd');
      
      FormManager.setValue('abbrevNm', 'Internal use only');
      FormManager.readOnly('abbrevNm');
      break;
    case 'DUMMY':
      // ISIC = 8888, - lock field
      FormManager.setValue('isicCd', '8888');
      FormManager.readOnly('isicCd');
      // Collection code = 00IL - lock field
      FormManager.setValue('collectionCd', '00IL');
      FormManager.readOnly('collectionCd');
      
      FormManager.setValue('abbrevNm', 'IGF DUMMY use only');
      FormManager.readOnly('abbrevNm');
      break;
    case 'AQSTN':
      // cluster/QTC/ISU: 01147/Q/34, 09056/blank/5K
      FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), [ '01147', '09056' ]);
      FormManager.limitDropdownValues(FormManager.getField('clientTier'), [ 'Q', ' ' ]);
      FormManager.limitDropdownValues(FormManager.getField('isuCd'), [ '34', '5K' ]);
      FormManager.setValue('apCustClusterId', '01147');
      FormManager.setValue('clientTier', 'Q');
      FormManager.setValue('isuCd', '34');
      // MRC = 3 - lock field
      FormManager.setValue('mrcCd', '3');
      FormManager.readOnly('mrcCd');
      // Collection code = 00JC - lock field
      FormManager.setValue('collectionCd', '00JC');
      FormManager.readOnly('collectionCd');
      
      FormManager.setValue('abbrevNm', 'Acquisition use only');
      FormManager.readOnly('abbrevNm');
      
      FormManager.setValue('isicCd', isicCdInDB);
      break;
    case 'NRML':
      // MRC = 3 - lock field
      FormManager.setValue('mrcCd', '3');
      FormManager.readOnly('mrcCd');
      // Collection code = 00JC - lock field
      FormManager.setValue('collectionCd', '00JC');
      FormManager.readOnly('collectionCd');
      // cluster/QTC/ISU: 01147/Q/34, 09056/blank/5K
      FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), [ '01147', '09056' ]);
      FormManager.limitDropdownValues(FormManager.getField('clientTier'), [ 'Q', ' ' ]);
      FormManager.limitDropdownValues(FormManager.getField('isuCd'), [ '34', '5K' ]);
      
      FormManager.setValue('apCustClusterId', '01147');
      FormManager.setValue('clientTier', 'Q');
      FormManager.setValue('isuCd', '34');
      
      FormManager.setValue('isicCd', isicCdInDB);
      break;
    case 'ESOSW':
      // MRC = 3 - lock field
      FormManager.setValue('mrcCd', '3');
      FormManager.readOnly('mrcCd');
      // Collection code = 00JC - lock field
      FormManager.setValue('collectionCd', '00JC');
      FormManager.readOnly('collectionCd');
      // cluster/QTC/ISU: 01147/Q/34, 09056/blank/5K, 08037/Y/34
      FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), [ '01147', '09056', '08037' ]);
      FormManager.limitDropdownValues(FormManager.getField('clientTier'), [ 'Q', ' ', 'Y' ]);
      FormManager.limitDropdownValues(FormManager.getField('isuCd'), [ '34', '5K' ]);
      FormManager.setValue('apCustClusterId', '01147');
      FormManager.setValue('clientTier', 'Q');
      FormManager.setValue('isuCd', '34');
      
      FormManager.setValue('abbrevNm', 'ESA use only');
      FormManager.readOnly('abbrevNm');
      
      FormManager.setValue('isicCd', isicCdInDB);
      break;
    case 'ECSYS':
      // cluster/QTC/ISU: 08037/Y/34 - lock field
      FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), [ '08037' ]);
      FormManager.limitDropdownValues(FormManager.getField('clientTier'), [ 'Y' ]);
      FormManager.limitDropdownValues(FormManager.getField('isuCd'), [ '34' ]);
      FormManager.setValue('apCustClusterId', '08037');
      FormManager.readOnly('apCustClusterId');
      FormManager.setValue('clientTier', 'Y');
      FormManager.readOnly('clientTier');
      FormManager.setValue('isuCd', '34');
      FormManager.readOnly('isuCd');
      // MRC = 3 - lock field
      FormManager.setValue('mrcCd', '3');
      FormManager.readOnly('mrcCd');
      // Collection code = 00JC - lock field
      FormManager.setValue('collectionCd', '00JC');
      FormManager.readOnly('collectionCd');
      
      FormManager.setValue('isicCd', isicCdInDB);
      break;
    case 'CROSS':
      // cluster/QTC/ISU: 01147/Q/34 (setting default and secletable ),
      // 09056/blank/5K
      FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), [ '01147', '09056' ]);
      FormManager.limitDropdownValues(FormManager.getField('clientTier'), [ 'Q', ' ' ]);
      FormManager.limitDropdownValues(FormManager.getField('isuCd'), [ '34', '5K' ]);
      FormManager.setValue('apCustClusterId', '01147');
      FormManager.setValue('clientTier', 'Q');
      FormManager.setValue('isuCd', '34');
      // MRC = 3 - lock field
      FormManager.setValue('mrcCd', '3');
      FormManager.readOnly('mrcCd');
      // Collection code = 00JC - lock field
      FormManager.setValue('collectionCd', '00JC');
      FormManager.readOnly('collectionCd');
      
      FormManager.setValue('isicCd', isicCdInDB);
      break;
    case 'XAQST':
      // cluster/QTC/ISU: 01147/Q/34, 09056/blank/5K
      FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), [ '01147', '09056' ]);
      FormManager.limitDropdownValues(FormManager.getField('clientTier'), [ 'Q', ' ' ]);
      FormManager.limitDropdownValues(FormManager.getField('isuCd'), [ '34', '5K' ]);
      FormManager.setValue('apCustClusterId', '01147');
      FormManager.setValue('clientTier', 'Q');
      FormManager.setValue('isuCd', '34');
      // MRC = 3 - lock field
      FormManager.setValue('mrcCd', '3');
      FormManager.readOnly('mrcCd');
      // Collection code = 00JC - lock field
      FormManager.setValue('collectionCd', '00JC');
      FormManager.readOnly('collectionCd');
      
      FormManager.setValue('abbrevNm', 'Acquisition use only');
      FormManager.readOnly('abbrevNm');
      
      FormManager.setValue('isicCd', isicCdInDB);
      break;
    case 'XBLUM':
      // cluster/QTC/ISU: default as 00002/Q/34 - lock field
      FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), [ '00002' ]);
      FormManager.limitDropdownValues(FormManager.getField('clientTier'), [ 'Q' ]);
      FormManager.limitDropdownValues(FormManager.getField('isuCd'), [ '34' ]);
      FormManager.setValue('apCustClusterId', '00002');
      FormManager.readOnly('apCustClusterId');
      FormManager.setValue('clientTier', 'Q');
      FormManager.readOnly('clientTier');
      FormManager.setValue('isuCd', '34');
      FormManager.readOnly('isuCd');
      // MRC = 3 - lock field
      FormManager.setValue('mrcCd', '3');
      FormManager.readOnly('mrcCd');
      // Collection code = 00JC - lock field
      FormManager.setValue('collectionCd', '00JC');
      FormManager.readOnly('collectionCd');
      
      FormManager.setValue('abbrevNm', 'Bluemix use only');
      FormManager.readOnly('abbrevNm');
      
      FormManager.setValue('isicCd', isicCdInDB);
      break;
    case 'XESO':
      // MRC = 3 - lock field
      FormManager.setValue('mrcCd', '3');
      FormManager.readOnly('mrcCd');
      // Collection code = 00JC - lock field
      FormManager.setValue('collectionCd', '00JC');
      FormManager.readOnly('collectionCd');
      // cluster/QTC/ISU: 01147/Q/34, 09056/blank/5K, 08037/Y/34
      FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), [ '01147', '09056', '08037' ]);
      FormManager.limitDropdownValues(FormManager.getField('clientTier'), [ 'Q', ' ', 'Y' ]);
      FormManager.limitDropdownValues(FormManager.getField('isuCd'), [ '34', '5K' ]);
      FormManager.setValue('apCustClusterId', '01147');
      FormManager.setValue('clientTier', 'Q');
      FormManager.setValue('isuCd', '34');
      
      FormManager.setValue('abbrevNm', 'ESA use only');
      FormManager.readOnly('abbrevNm');
      
      FormManager.setValue('isicCd', isicCdInDB);
      break;
    case 'XMKTP':
      // cluster/QTC/ISU: default as 00002/Q/34 - lock field
      FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), [ '00002' ]);
      FormManager.limitDropdownValues(FormManager.getField('clientTier'), [ 'Q' ]);
      FormManager.limitDropdownValues(FormManager.getField('isuCd'), [ '34' ]);
      FormManager.setValue('apCustClusterId', '00002');
      FormManager.readOnly('apCustClusterId');
      FormManager.setValue('clientTier', 'Q');
      FormManager.readOnly('clientTier');
      FormManager.setValue('isuCd', '34');
      FormManager.readOnly('isuCd');
      // MRC = 3 - lock field
      FormManager.setValue('mrcCd', '3');
      FormManager.readOnly('mrcCd');
      // Collection code = 00JC - lock field
      FormManager.setValue('collectionCd', '00JC');
      FormManager.readOnly('collectionCd');
      
      FormManager.setValue('abbrevNm', 'Market place use only');
      FormManager.readOnly('abbrevNm');
      
      FormManager.setValue('isicCd', isicCdInDB);
      break;
    }
}
// CREATCMR-7653
// Perform UI validation when'Send for processing':
// name can not contain 'Private Limited', 'Company', 'Corporation',
// 'incorporate', 'organization', 'Pvt Ltd'
function checkNZCustomerNameTextWhenSFP(){
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var errorMsg = '';
        var action = FormManager.getActualValue('yourAction');
        var custNm1 = FormManager.getActualValue('mainCustNm1').toUpperCase();
        
        var reqType = FormManager.getActualValue('reqType');
        var role = FormManager.getActualValue('userRole').toUpperCase();
        var custGrp = FormManager.getActualValue('custGrp');
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        
        if (reqType == 'C' && role == 'REQUESTER' && custGrp == 'LOCAL' && custSubGrp == 'PRIV' && action=='SFP') {
          if(custNm1.indexOf('PRIVATE LIMITED')>-1){
            errorMsg = 'Customer Name can not contain \'Private Limited\'';
          } else if(custNm1.indexOf('COMPANY')>-1){
            errorMsg = 'Customer Name can not contain \'Company\'';
          } else if(custNm1.indexOf('CORPORATION')>-1){
            errorMsg = 'Customer Name can not contain \'Corporation\'';
          } else if(custNm1.indexOf('INCORPORATE')>-1){
            errorMsg = 'Customer Name can not contain \'incorporate\'';
          } else if(custNm1.indexOf('ORGANIZATION')>-1){
            errorMsg = 'Customer Name can not contain \'organization\'';
          } else if(custNm1.indexOf('PVT LTD')>-1){
            errorMsg = 'Customer Name can not contain \'Pvt Ltd\'';
          }
          if (errorMsg != '') {
            return new ValidationResult({
              id : 'custNm1',
              type : 'text',
              name : 'custNm1'
            }, false, errorMsg);
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}
// CREATCMR-7653
// Perform UI validation when'Send for processing': Landed country can't be New
// Zealand
function checkNZLandedCountryForCrossWhenSFP(){
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var errorMsg = '';
        var action = FormManager.getActualValue('yourAction');
        var landCntry = '';
        var custLandCntry = FormManager.getActualValue('landCntry');
        if (custLandCntry != '') {
          landCntry = custLandCntry;
        } else {
          var _zs01ReqId = FormManager.getActualValue('reqId');
          var cntryCdParams = {
            REQ_ID : _zs01ReqId,
            ADDR_TYPE : 'ZS01',
          };
          var cntryCdResult = cmr.query('ADDR.GET.LANDEDCNTRY.BY_REQID_ADDRTYPE', cntryCdParams);

          if (cntryCdResult.ret1 != undefined) {
            landCntry = cntryCdResult.ret1;
          }
        }
        var reqType = FormManager.getActualValue('reqType');
        var role = FormManager.getActualValue('userRole').toUpperCase();
        var custGrp = FormManager.getActualValue('custGrp');
        
        if (reqType == 'C' && role == 'REQUESTER' && custGrp == 'CROSS' && action=='SFP' && landCntry == "NZ") {
          errorMsg = 'Landed country can\'t be New Zealand when \'Cross-Border Customer\' selected';
          return new ValidationResult({
            id : 'landCntry',
            type : 'text',
            name : 'landCntry'
          }, false, errorMsg);
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}
// CREATCMR-7653
function checkNZCustomerNameStartsForLocalInterDummy(){
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var errorMsg = '';
        var custNm1 = FormManager.getActualValue('mainCustNm1').toUpperCase();
        
        var reqType = FormManager.getActualValue('reqType');
        var role = FormManager.getActualValue('userRole').toUpperCase();
        var custGrp = FormManager.getActualValue('custGrp');
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        
        if (reqType == 'C' && role == 'REQUESTER' && custGrp == 'LOCAL') {
          if(custSubGrp == 'INTER' && custNm1.indexOf('IBM ')!=0){
            errorMsg = 'Customer name must start with \'IBM \'';
          } else if(custSubGrp == 'DUMMY' && custNm1.indexOf('IGF DUMMY ')!=0){
            errorMsg = 'Customer name must start with \'IGF DUMMY \'';
          }
          if (errorMsg != '') {
            return new ValidationResult({
              id : 'custNm1',
              type : 'text',
              name : 'custNm1'
            }, false, errorMsg);
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

dojo.addOnLoad(function() {
  GEOHandler.AP = [ SysLoc.AUSTRALIA, SysLoc.BANGLADESH, SysLoc.BRUNEI, SysLoc.MYANMAR, SysLoc.SRI_LANKA, SysLoc.INDIA, SysLoc.INDONESIA, SysLoc.PHILIPPINES, SysLoc.SINGAPORE, SysLoc.VIETNAM,
      SysLoc.THAILAND, SysLoc.HONG_KONG, SysLoc.NEW_ZEALAND, SysLoc.LAOS, SysLoc.MACAO, SysLoc.MALASIA, SysLoc.NEPAL, SysLoc.CAMBODIA ];
  GEOHandler.ISA = [ SysLoc.INDIA, SysLoc.SRI_LANKA, SysLoc.BANGLADESH, SysLoc.NEPAL ];
  GEOHandler.ASEAN = [ SysLoc.BRUNEI, SysLoc.MYANMAR, SysLoc.INDONESIA, SysLoc.PHILIPPINES, SysLoc.SINGAPORE, SysLoc.VIETNAM, SysLoc.THAILAND, SysLoc.LAOS, SysLoc.MALASIA, SysLoc.CAMBODIA ];
  GEOHandler.ANZ = [ SysLoc.AUSTRALIA, SysLoc.NEW_ZEALAND ];
  GEOHandler.GCG = [ SysLoc.HONG_KONG, SysLoc.MACAO ];
  GEOHandler.APAC_1 = [SysLoc.SINGAPORE, SysLoc.PHILIPPINES, SysLoc.THAILAND, SysLoc.MALASIA, SysLoc.INDONESIA, SysLoc.BRUNEI, SysLoc.VIETNAM, SysLoc.INDIA, SysLoc.BANGLADESH, SysLoc.SRI_LANKA, SysLoc.AUSTRALIA, SysLoc.NEW_ZEALAND, SysLoc.CHINA, SysLoc.HONG_KONG, SysLoc.MACAO, SysLoc.TAIWAN,SysLoc.KOREA ];
  // CREATCMR-6825
  GEOHandler.APAC = [ SysLoc.SINGAPORE, SysLoc.PHILIPPINES, SysLoc.THAILAND, SysLoc.MALASIA, SysLoc.INDONESIA, SysLoc.BRUNEI, SysLoc.VIETNAM, SysLoc.INDIA, SysLoc.BANGLADESH, SysLoc.SRI_LANKA, 
      SysLoc.AUSTRALIA, SysLoc.NEW_ZEALAND, SysLoc.HONG_KONG, SysLoc.MACAO  ];
  console.log('adding AP functions...');
  console.log('the value of person full id is ' + localStorage.getItem("pID"));
  GEOHandler.setRevertIsicBehavior(false);
  GEOHandler.registerValidator(addSalesRepNameNoCntryValidator, [ SysLoc.AUSTRALIA, SysLoc.INDONESIA, SysLoc.PHILIPPINES, SysLoc.SINGAPORE, SysLoc.VIETNAM, SysLoc.THAILAND, SysLoc.HONG_KONG,
      SysLoc.NEW_ZEALAND, SysLoc.MACAO, SysLoc.MALASIA, SysLoc.NEPAL ]);
  GEOHandler.addAfterTemplateLoad(addSalesRepNameNoCntryValidatorBluepages, [ SysLoc.BRUNEI, SysLoc.MYANMAR, SysLoc.LAOS, SysLoc.CAMBODIA ]);

  // 1558942: Update Address Details of different address types at one time
  GEOHandler.enableCopyAddress(GEOHandler.AP);

  GEOHandler.addAfterConfig(addAfterConfigAP, GEOHandler.AP);
  GEOHandler.addAfterTemplateLoad(addAfterConfigAP, GEOHandler.AP);
  
  GEOHandler.addAfterConfig(updateIndustryClass, GEOHandler.AP);
  GEOHandler.addAfterConfig(updateProvCd, GEOHandler.AP);
  GEOHandler.addAfterConfig(updateRegionCd, GEOHandler.AP);
  GEOHandler.addAfterConfig(setCollectionCd, GEOHandler.AP, [ SysLoc.AUSTRALIA, SysLoc.INDIA ]);
  GEOHandler.addAfterConfig(setCollCdFrAU, [ SysLoc.AUSTRALIA ]);
  GEOHandler.addAfterConfig(setCollCdFrIndia, [ SysLoc.INDIA ]);
  GEOHandler.addAfterConfig(onSubIndustryChange, GEOHandler.AP);
  GEOHandler.addAfterConfig(onIsuCdChangeAseanAnzIsa, GEOHandler.ASEAN);
  GEOHandler.addAfterConfig(onIsuCdChangeAseanAnzIsa, GEOHandler.ANZ);
  GEOHandler.addAfterConfig(onIsuCdChangeAseanAnzIsa, GEOHandler.ISA);
  GEOHandler.enableCustomerNamesOnAddress(GEOHandler.AP);
  GEOHandler.addAddrFunction(updateMainCustomerNames, GEOHandler.AP);
  GEOHandler.addAddrFunction(handleObseleteExpiredDataForUpdate, GEOHandler.AP);
  GEOHandler.addAddrFunction(setAbbrevNmLocnOnAddressSave, GEOHandler.AP);
  // GEOHandler.addAddrFunction(addMandateCmrNoForSG, [ SysLoc.SINGAPORE ]);

  GEOHandler.addAfterConfig(onCustSubGrpChange, GEOHandler.AP);
  GEOHandler.addAfterConfig(setCTCIsuByCluster, GEOHandler.AP);
  GEOHandler.addAfterTemplateLoad(setCTCIsuByCluster, GEOHandler.AP); 
  GEOHandler.addAfterConfig(setCTCIsuByClusterANZ, GEOHandler.ANZ);
  GEOHandler.addAfterTemplateLoad(setCTCIsuByClusterANZ, GEOHandler.ANZ);
  GEOHandler.addAfterConfig(setCTCIsuByClusterASEAN, GEOHandler.ASEAN);
  GEOHandler.addAfterTemplateLoad(setISUDropDownValues, GEOHandler.AP);
  // GEOHandler.addAfterConfig(setIsuByClusterCTC, GEOHandler.AP);
  GEOHandler.registerValidator(addAbnValidatorForAU, [ SysLoc.AUSTRALIA ]);

  // GEOHandler.registerValidator(addMandateCmrNoForSG, [SysLoc.SINGAPORE]);
  GEOHandler.registerValidator(addCmrNoValidator, [ SysLoc.SINGAPORE ]);
  GEOHandler.registerValidator(addCompanyProofAttachValidation, [ SysLoc.INDIA]);
  GEOHandler.registerValidator(addressNameSimilarValidator, [ SysLoc.INDIA]);
  GEOHandler.registerValidator(addressNameSameValidator, [ SysLoc.SINGAPORE]);
  GEOHandler.registerValidator(addCompanyProofForSG, [ SysLoc.SINGAPORE]);
  GEOHandler.registerValidator(additionalAddrNmValidator, [ SysLoc.SINGAPORE ]);
  
  
  GEOHandler.addAfterConfig(removeStateValidatorForHkMoNZ, [ SysLoc.AUSTRALIA, SysLoc.NEW_ZEALAND ]);
  GEOHandler.addAfterTemplateLoad(removeStateValidatorForHkMoNZ, [ SysLoc.AUSTRALIA, SysLoc.NEW_ZEALAND ]);

  // GEOHandler.addAfterConfig(addValidatorForSingapore, [SysLoc.SINGAPORE]);
  // GEOHandler.addAfterTemplateLoad(addValidatorForSingapore,
  // [SysLoc.SINGAPORE]);

  // setting collection code for ASEAN
  GEOHandler.addAfterConfig(onISBUCdChange, GEOHandler.ASEAN);

  GEOHandler.addAfterConfig(addGovIndcHanlder, [ SysLoc.AUSTRALIA ]);
  GEOHandler.addAfterConfig(addGovCustTypHanlder, [ SysLoc.AUSTRALIA ]);
  GEOHandler.addAfterConfig(onInacTypeChange, [ SysLoc.AUSTRALIA, SysLoc.NEW_ZEALAND, SysLoc.INDIA, SysLoc.SINGAPORE, SysLoc.THAILAND]);

  // ERO specific
  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.GCG);
  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.ASEAN);
  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.ISA);

  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.ANZ);
  GEOHandler.registerValidator(addDPLCheckValidator, GEOHandler.ANZ, GEOHandler.ROLE_REQUESTER, true);
  GEOHandler.registerValidator(addDPLCheckValidator, GEOHandler.GCG, GEOHandler.ROLE_REQUESTER, true);
  GEOHandler.registerValidator(addDPLCheckValidator, GEOHandler.ASEAN, GEOHandler.ROLE_REQUESTER, true);
  GEOHandler.registerValidator(addDPLCheckValidator, GEOHandler.ISA, GEOHandler.ROLE_REQUESTER, true);

  GEOHandler.addAfterConfig(defaultCMRNumberPrefix, [ SysLoc.HONG_KONG, SysLoc.MACAO, SysLoc.INDIA  ]);
  GEOHandler.addAfterTemplateLoad(defaultCMRNumberPrefix, [ SysLoc.HONG_KONG, SysLoc.MACAO, SysLoc.INDIA  ]);
  GEOHandler.addAfterTemplateLoad(defaultCMRNumberPrefixforSingapore, [ SysLoc.SINGAPORE ]);
  // CREATCMR-7656
  GEOHandler.addAfterConfig(defaultCMRNumberPrefixforNewZealand, [SysLoc.NEW_ZEALAND]);
  GEOHandler.addAfterTemplateLoad(defaultCMRNumberPrefixforNewZealand, [SysLoc.NEW_ZEALAND]);
  // 2333
  GEOHandler.addAfterConfig(setISBUforBPscenario, GEOHandler.ASEAN);
  GEOHandler.addAfterTemplateLoad(setISBUforBPscenario, GEOHandler.ASEAN);
  GEOHandler.addAfterConfig(addVatValidationforSingapore, [ SysLoc.SINGAPORE ]);
  GEOHandler.registerValidator(addressNameSameValidator, [ SysLoc.SINGAPORE]);
  GEOHandler.registerValidator(addCompanyProofForSG, [ SysLoc.SINGAPORE]);
  GEOHandler.registerValidator(additionalAddrNmValidator, [ SysLoc.SINGAPORE ]);
  GEOHandler.registerValidator(additionalAddrNmValidatorNZ, [ SysLoc.NEW_ZEALAND ], null, true);
  GEOHandler.registerValidator(additionalAddrNmValidatorOldNZ, [ SysLoc.NEW_ZEALAND ], null, true);


  // checklist
  GEOHandler.registerValidator(addChecklistValidator, [ SysLoc.VIETNAM, SysLoc.LAOS, SysLoc.CAMBODIA, SysLoc.HONG_KONG, SysLoc.SINGAPORE, SysLoc.MACAO, SysLoc.MYANMAR ]);
  GEOHandler.addAfterConfig(initChecklistMainAddress, [ SysLoc.VIETNAM, SysLoc.LAOS, SysLoc.CAMBODIA, SysLoc.HONG_KONG, SysLoc.SINGAPORE, SysLoc.MACAO, SysLoc.MYANMAR ]);
  // Address validations
  GEOHandler.registerValidator(addSoltToAddressValidator, GEOHandler.AP);
  GEOHandler.registerValidator(addAddressInstancesValidator, GEOHandler.AP, null, true);
  GEOHandler.registerValidator(addContactInfoValidator, GEOHandler.AP, GEOHandler.REQUESTER, true);
  GEOHandler.registerValidator(similarAddrCheckValidator, GEOHandler.AP, null, true);

  GEOHandler.registerValidator(addAttachmentValidator, [ SysLoc.SRI_LANKA, SysLoc.BANGLADESH, SysLoc.NEPAL, SysLoc.AUSTRALIA, SysLoc.SINGAPORE,SysLoc.NEW_ZEALAND ], GEOHandler.REQUESTER, false, false);
  GEOHandler.registerValidator(setAttachmentOnCluster, [ SysLoc.INDIA, SysLoc.SRI_LANKA, SysLoc.BANGLADESH], GEOHandler.REQUESTER, false, false);
  
  // double creates
  GEOHandler.addAfterConfig(setFieldsForDoubleCreates, [ SysLoc.INDIA, SysLoc.BANGLADESH, SysLoc.SRI_LANKA, SysLoc.VIETNAM, SysLoc.CAMBODIA ]);
  GEOHandler.addAfterTemplateLoad(setFieldsForDoubleCreates, [ SysLoc.INDIA, SysLoc.BANGLADESH, SysLoc.SRI_LANKA, SysLoc.VIETNAM, SysLoc.CAMBODIA ]);
  GEOHandler.registerValidator(addDoubleCreateValidator, [ SysLoc.INDIA, SysLoc.BANGLADESH, SysLoc.SRI_LANKA, SysLoc.VIETNAM, SysLoc.CAMBODIA ], null, true);
  GEOHandler.registerValidator(addDoubleCreateValidatorSG, [ SysLoc.SINGAPORE ], null, true);
  // GEOHandler.addAfterConfig(setAddressDetailsForView, GEOHandler.AP);

  GEOHandler.registerValidator(addFormatFieldValidator, GEOHandler.AP, null, true);

  GEOHandler.registerValidator(addFieldFormatValidator, GEOHandler.AP, null, true);

  GEOHandler.registerValidator(addFormatForCMRNumValidator, [ SysLoc.SINGAPORE ], null, true);

  // CREATCMR-6398
  GEOHandler.registerValidator(businessParterValidator, [ SysLoc.SINGAPORE ], null, true);
  
  GEOHandler.addAddrFunction(lockCustMainNames, GEOHandler.AP);
  // Story - 1781935 -> AR Code for Singapore
  GEOHandler.addAddrFunction(setCollCdFrSGOnAddrSave, [ SysLoc.SINGAPORE ]);
  GEOHandler.addAfterTemplateLoad(setCollCdFrSingapore, [ SysLoc.SINGAPORE ]);
  GEOHandler.registerValidator(addAddressLengthValidators, [ SysLoc.AUSTRALIA, SysLoc.NEW_ZEALAND ], null, true);
  GEOHandler.registerValidator(addStreetValidationChkReq, [ SysLoc.AUSTRALIA ], null, true);
  GEOHandler.registerValidator(validateContractAddrAU, [ SysLoc.AUSTRALIA ], null, true);
  GEOHandler.registerValidator(validateCustNameForNonContractAddrs, [ SysLoc.AUSTRALIA ], null, true);
  GEOHandler.registerValidator(validateStreetAddrCont2, [ SysLoc.BANGLADESH, SysLoc.BRUNEI, SysLoc.MYANMAR, SysLoc.SRI_LANKA, SysLoc.INDIA, SysLoc.INDONESIA, SysLoc.PHILIPPINES, SysLoc.SINGAPORE,
  SysLoc.VIETNAM, SysLoc.THAILAND, SysLoc.HONG_KONG, SysLoc.LAOS, SysLoc.MACAO, SysLoc.MALASIA, SysLoc.NEPAL, SysLoc.CAMBODIA ], null, true);
  // CREATCMR-7589
  GEOHandler.addAfterConfig(onIsicChangeHandler, [SysLoc.INDIA, SysLoc.AUSTRALIA, SysLoc.SINGAPORE ]);
  GEOHandler.addAfterConfig(onIsicChange, [SysLoc.INDIA, SysLoc.AUSTRALIA, SysLoc.SINGAPORE ]);
  GEOHandler.addAfterTemplateLoad(onIsicChange, [SysLoc.INDIA, SysLoc.AUSTRALIA, SysLoc.SINGAPORE ]);
  
  GEOHandler.addAfterConfig(addHandlersForAP, GEOHandler.AP);
  GEOHandler.addAfterConfig(addHandlersForISA, GEOHandler.ISA);
  GEOHandler.addAfterConfig(addHandlersForGCG, GEOHandler.GCG);
  GEOHandler.addAfterConfig(addHandlersForANZ, GEOHandler.ANZ);
  GEOHandler.addAfterTemplateLoad(setInacByClusterHKMO, GEOHandler.GCG);
  GEOHandler.registerValidator(validateGSTForIndia, [ SysLoc.INDIA ], null, true);
  GEOHandler.registerValidator(validateABNForAU, [ SysLoc.AUSTRALIA ], null, true);
  GEOHandler.addAfterConfig(afterConfigForIndia, [ SysLoc.INDIA ]);
  GEOHandler.addAfterTemplateLoad(afterConfigForIndia, SysLoc.INDIA);
  GEOHandler.addAfterConfig(resetGstExempt, [ SysLoc.INDIA ]);
  GEOHandler.addAfterTemplateLoad(resetGstExempt, SysLoc.INDIA);
  GEOHandler.addAfterTemplateLoad(setCTCIsuByClusterASEAN, GEOHandler.ASEAN);
  GEOHandler.addAfterTemplateLoad(setMrc4IntDumForASEAN, GEOHandler.ASEAN);
  GEOHandler.addAfterConfig(lockFieldsForIndia, [ SysLoc.INDIA ]);
  GEOHandler.addAfterTemplateLoad(lockFieldsForIndia, SysLoc.INDIA);
  GEOHandler.addAfterConfig(lockFieldsForAU, [ SysLoc.AUSTRALIA ]);
  GEOHandler.addAfterTemplateLoad(lockFieldsForAU, SysLoc.AUSTRALIA);
  GEOHandler.registerValidator(addValidatorBasedOnCluster, GEOHandler.ANZ, GEOHandler.ROLE_REQUESTER, true);
  GEOHandler.registerValidator(addValidatorBasedOnCluster, GEOHandler.ASEAN, GEOHandler.ROLE_REQUESTER, true);
  GEOHandler.addAfterTemplateLoad(lockAbbvNameOnScenarioChangeGCG, GEOHandler.GCG);
  GEOHandler.addAfterTemplateLoad(setAbbrvNameBPScen, GEOHandler.GCG);
	GEOHandler.addAfterConfig(handleExpiredClusterGCG, GEOHandler.GCG);
  GEOHandler.addAfterConfig(setCtcOnIsuCdChangeASEAN, GEOHandler.ASEAN);
  GEOHandler.addAfterTemplateLoad(setCtcOnIsuCdChangeASEAN, GEOHandler.ASEAN);
  GEOHandler.addAfterTemplateLoad(setCtcOnIsuCdChangeISA, GEOHandler.ISA);
  GEOHandler.addAfterConfig(setCtcOnIsuCdChangeISA, GEOHandler.ISA);
  GEOHandler.addAfterConfig(setCtcOnIsuCdChangeGCG, GEOHandler.GCG);
  GEOHandler.addAfterTemplateLoad(setCtcOnIsuCdChangeGCG, GEOHandler.GCG);
  GEOHandler.addAfterTemplateLoad(setCtcOnIsuCdChangeANZ, GEOHandler.ANZ);
  GEOHandler.addAfterConfig(setCtcOnIsuCdChangeANZ, GEOHandler.ANZ);
  GEOHandler.addAfterConfig(lockFieldsForIndia, [ SysLoc.INDIA ]);
  GEOHandler.registerValidator(validateCustNameForInternal, [ SysLoc.AUSTRALIA ], null, true);  
  GEOHandler.registerValidator(clusterCdValidatorAU, [ SysLoc.AUSTRALIA ], null, true);
  GEOHandler.registerValidator(addCtcObsoleteValidator, GEOHandler.AP, null, true);
  GEOHandler.registerValidator(validateClusterBaseOnScenario, [ SysLoc.SINGAPORE ], null, true);  
  GEOHandler.addAfterConfig(lockInacCodeForIGF, [ SysLoc.INDIA ]);
  GEOHandler.addAfterTemplateLoad(lockInacCodeForIGF, SysLoc.INDIA);
  GEOHandler.addAfterTemplateLoad(handleObseleteExpiredDataForUpdate,  GEOHandler.AP );
  GEOHandler.addAfterConfig(handleObseleteExpiredDataForUpdate, GEOHandler.AP );
  // India Handler
  // CREATCMR-6825
  GEOHandler.addAfterConfig(setRepTeamMemberNo, GEOHandler.APAC);
  GEOHandler.addAfterTemplateLoad(setRepTeamMemberNo, GEOHandler.APAC);
  GEOHandler.addAddrFunction(displayVatRegistrartionStatus, [ SysLoc.SINGAPORE ]);
  GEOHandler.addAfterConfig(displayVatRegistrartionStatus,  [ SysLoc.SINGAPORE ] );
  GEOHandler.addAfterTemplateLoad(displayVatRegistrartionStatus,   [ SysLoc.SINGAPORE ] );
	GEOHandler.addAfterConfig(addCustGrpHandler, GEOHandler.APAC_1);
  
  // CREATCMR-7655
  GEOHandler.registerValidator(validatNZBNForNewZeaLand, [ SysLoc.NEW_ZEALAND ], null, true);
  GEOHandler.addAfterConfig(afterConfigForNewZeaLand, [ SysLoc.NEW_ZEALAND]);
  GEOHandler.addAfterTemplateLoad(afterConfigForNewZeaLand, SysLoc.NEW_ZEALAND);
  GEOHandler.addAfterConfig(resetNZNBExempt, [ SysLoc.NEW_ZEALAND ]);
  GEOHandler.addAfterTemplateLoad(resetNZNBExempt, SysLoc.NEW_ZEALAND);
  
  // CREATCMR-7658
  GEOHandler.registerValidator(addValidatorforInstallingNZ, [SysLoc.NEW_ZEALAND], null, true);
  
  // CREATCMR-7653
  GEOHandler.registerValidator(checkNZCustomerNameTextWhenSFP, [ SysLoc.NEW_ZEALAND ], GEOHandler.ROLE_REQUESTER, true);
  GEOHandler.registerValidator(checkNZLandedCountryForCrossWhenSFP, [ SysLoc.NEW_ZEALAND ], GEOHandler.ROLE_REQUESTER, true);
  GEOHandler.registerValidator(checkNZCustomerNameStartsForLocalInterDummy, [ SysLoc.NEW_ZEALAND ], GEOHandler.ROLE_REQUESTER, true);
});
