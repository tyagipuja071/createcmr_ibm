/* Register AP Javascripts */
var _isicHandlerAP = null;
var _clusterHandlerAP = null;
var _isicHandlerGCG = null;
var _clusterHandlerGCG = null;
var _vatExemptHandler = null;
var _vatExemptHandlerNZ = null;
var _bpRelTypeHandlerGCG = null;
var _isuHandler = null;
var _clusterHandlerANZ = null;
var _inacCdHandlerIN = null;
var _importIndIN = null;
var _vatRegisterHandlerSG = null;
var _clusterHandlerINDONESIA = 0;
var _inacHandlerANZSG = 0;
var oldClusterCd = null;
function addHandlersForAP() {
  if (_isicHandlerAP == null) {
    _isicHandlerAP = dojo.connect(FormManager.getField('isicCd'), 'onChange', function (value) {
      setIsuOnIsic();
    });
  }
  if (_inacCdHandlerIN == null) {
    _inacCdHandlerIN = dojo.connect(FormManager.getField('inacCd'), 'onChange', function (value) {
      lockInacTypeForIGF();
      setInacNacFieldsRequiredIN();
    });
  }
  if (_isuHandler == null) {
    _isuHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function (value) {
      setCtcOnIsuCdChangeASEAN();
      setCtcOnIsuCdChangeANZ(value);
    });
  }

  if (_clusterHandlerAP == null && FormManager.getActualValue('reqType') != 'U') {
    _clusterHandlerAP = dojo.connect(FormManager.getField('apCustClusterId'), 'onChange', function (value) {
      // CREATCMR-7883-7884
      _inacHandlerANZSG = _inacHandlerANZSG + 1;
// setInacByCluster();
      setInacNacValuesIN();
      setIsuOnIsic();

      filterInacCd('744', '10215', 'N', 'I529');
      applyClusterFilters();
    });
  }

  if (_vatRegisterHandlerSG == null) {
    _vatRegisterHandlerSG = dojo.connect(FormManager.getField('taxCd1'), 'onChange', function (value) {
      cmr
        .showAlert(
          '<div align="center"><strong>VAT Registration Status validation </strong></div> <br/> Please note: <br/> <ul style="list-style-type:circle"> <li>You have to make sure the selection(Yes/No) of "VAT Registration Status" is correct for the Thailand VAT# you have filled. This is specific to the moment you submit this request.<br/>The status can be validated via VES Thailand: <a href="https://eservice.rd.go.th/rd-ves-web/search/vat" target="_blank" rel="noopener noreferrer"> https://eservice.rd.go.th/rd-ves-web/search/vat </a> </li><br/> <li> By selecting \'No - VAT unapplicable\', you are confirming that this customer has no VAT# then "VAT Registration Status" is not applicable for the same.</li> </ul>', 'VAT Registration Status validation', 'vatRegistrationForSG()', 'VatRegistrationStatus', {
          OK: 'I confirm',
        });
    });
  }
  handleObseleteExpiredDataForUpdate();
}


function addHandlersForANZ() {
  if (_clusterHandlerANZ == null && FormManager.getActualValue('reqType') != 'U') {
    _clusterHandlerANZ = dojo.connect(FormManager.getField('apCustClusterId'), 'onChange', function (value) {
      lockFieldsForAU();
    });
  }
}

function addHandlersForISA() {
  if (_isuHandler == null) {
    _isuHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function (value) {
      setCtcOnIsuCdChangeISA();
    });
  }
}

function addHandlersForGCG() {
  if (_isicHandlerGCG == null) {
    _isicHandlerGCG = dojo.connect(FormManager.getField('isicCd'), 'onChange', function (value) {
      setIsuOnIsic();
    });
  }

  if (_clusterHandlerGCG == null && FormManager.getActualValue('reqType') != 'U') {
    _clusterHandlerGCG = dojo.connect(FormManager.getField('apCustClusterId'), 'onChange', function (value) {
      setIsuOnIsic();
      setInacByClusterHKMO();
    });
  }

  if (_bpRelTypeHandlerGCG == null && FormManager.getActualValue('reqType') != 'U') {
    _bpRelTypeHandlerGCG = dojo.connect(FormManager.getField('bpRelType'), 'onChange', function (value) {
      setAbbrvNameBPScen();
    });
  }

  if (_isuHandler == null) {
    _isuHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function (value) {
      setCtcOnIsuCdChangeGCG();
    });
  }

}

function afterConfigForIndia() {
  console.log("----After config. for India----");
  if (_vatExemptHandler == null) {
    _vatExemptHandler = dojo.connect(FormManager.getField('vatExempt'), 'onClick', function (value) {
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
        if (!(custSubGrp == 'BLUMX' || custSubGrp == 'MKTPC' || custSubGrp == 'AQSTN' || custSubGrp == 'PRIV')) {
          FormManager.addValidator('vat', Validators.REQUIRED, ['GST#'], 'MAIN_CUST_TAB');
        }
      }
    });


  }

  // CREATCMR-7005
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (custSubGrp == 'CROSS') {
    FormManager.readOnly('vat');
  }
  // CREATCMR-7005

  dojo.connect(FormManager.getField('custSubGrp'), 'onChange', function (value) {
    setInacNacValuesIN();
    filterInacCd('744', '10215', 'N', 'I529');
    applyClusterFilters();
    lockInacNacFieldsByScenarioSubType();
    lockFieldsWithDefaultValuesByScenarioSubType();

    if (cntry == SysLoc.INDIA && custSubGrp == "CROSS" && _pagemodel.apCustClusterId == null) {
      FormManager.setValue('apCustClusterId', "012D999");
    }

  });

  dojo.connect(FormManager.getField('apCustClusterId'), 'onChange', function (value) {
    lockFieldsWithDefaultValuesByScenarioSubType();
  });

  dojo.connect(FormManager.getField('custSubGrp'), 'onChange', function (value) {
    lockFieldsWithDefaultValuesByScenarioSubType();
  });

  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (custSubGrp == 'NRMLC' || custSubGrp == 'AQSTN') {
    dojo.connect(FormManager.getField('geoLocationCd'), 'onChange', function (value) {
      setClusterGlcCovIdMapNrmlc();
    });
  }

  var cntry = FormManager.getActualValue('cmrIssuingCntry');

}

function resetGstExempt() {
  console.log('>>>> resetGstExempt >>>>');
  if (dijit.byId('vatExempt') != undefined && dijit.byId('vatExempt').get('checked')) {
    console.log(">>> Process gstExempt remove * >> ");
    FormManager.resetValidations('vat');
    FormManager.readOnly('vat');
  }
}

function addAfterConfigAP() {
  console.log('>>>> addAfterConfigAP >>>>');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var reqType = FormManager.getActualValue('reqType');
  var custType = FormManager.getActualValue('custGrp');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var custGrp = FormManager.getActualValue('custGrp');
  _clusterHandlerINDONESIA = 0;
  // CREATCMR-7883-7884
  _inacHandlerANZSG = 0;
  if (reqType == 'U') {
    FormManager.removeValidator('vat', Validators.REQUIRED);
  }

  if (cntry == '834' && reqType == 'C' && role == 'REQUESTER' && custType == 'CROSS' && custSubGrp == 'SPOFF') {
    FormManager.addValidator('cmrNo', Validators.REQUIRED, ['CMR Number'], 'MAIN_IBM_TAB');
  } else {
    FormManager.removeValidator('cmrNo', Validators.REQUIRED);
  }

  if (cntry == '616' && reqType == 'C' && (role == 'PROCESSOR' || role == 'REQUESTER') && (custSubGrp == 'INTER' || custSubGrp == 'XINT')) {
    FormManager.addValidator('cmrNoPrefix', Validators.REQUIRED, ['CmrNoPrefix'], 'MAIN_IBM_TAB');
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
    FormManager.enable('abbrevNm');
    FormManager.enable('sectorCd');
    FormManager.enable('abbrevLocn');
    FormManager.enable('territoryCd');
    FormManager.enable('IndustryClass');
    FormManager.enable('subIndustryCd');
  }

  if ((cntry == '616' || cntry == '834') && custSubGrp == 'KYNDR') {
    if (role == 'REQUESTER' || role == 'VIEWER' || role == 'PROCESSOR') {
      FormManager.setValue('mrcCd', '3');
      FormManager.readOnly('apCustClusterId');
      FormManager.readOnly('clientTier');
      FormManager.readOnly('apCustClusterId');
      if (cntry == '616') {
// FormManager.setValue('apCustClusterId', "09057");
        FormManager.setValue('isuCd', '5K');
      } else if (cntry == '834') {
        FormManager.setValue('apCustClusterId', "09052");
        FormManager.setValue('isuCd', '5K');
      }
    }
  }
  if (reqType == 'C' && custSubGrp == 'ECOSY' && (cntry == '738' || cntry == '736')) {
    FormManager.readOnly('apCustClusterId');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('mrcCd');
    FormManager.setValue('clientTier', 'Y');
    FormManager.setValue('isuCd', '34');
    FormManager.setValue('mrcCd', '3');
    if (cntry == '738') {
      FormManager.setValue('apCustClusterId', '08041');
      FormManager.removeValidator('isuCd', Validators.REQUIRED);
    }
    if (cntry == '736') {
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

  var clusterId = FormManager.getActualValue('apCustClusterId');
  if (reqType == 'C' && cntry == '643' && clusterId == '00000') {
    FormManager.readOnly('apCustClusterId');
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
  if (cntry == '738' || cntry == '736') {
    addressQuotationValidatorGCG();
  }
  var streetAddressCont1 = FormManager.getActualValue('addrTxt2');
  if ((cntry == '738' || cntry == '736') && (streetAddressCont1 == '' || streetAddressCont1 == null)) {
    return new ValidationResult({
      id: 'addrTxt2',
      type: 'text',
      name: 'addrTxt2'
    }, false, 'Street Address Con' + "'" + 't1 is required ');
  }

  if (role == 'REQUESTER' && reqType == 'C') {
    if ((cntry == SysLoc.NEW_ZEALAND || cntry == SysLoc.AUSTRALIA || cntry == SysLoc.INDONESIA || cntry == SysLoc.PHILIPPINES || cntry == SysLoc.SINGAPORE || cntry == SysLoc.THAILAND || cntry == SysLoc.MALASIA) && (custSubGrp == 'ECSYS' || custSubGrp == 'XECO')) {
      FormManager.setValue('mrcCd', '3');
      FormManager.setValue('clientTier', 'Y');
      FormManager.readOnly('clientTier');
      FormManager.setValue('isuCd', '34');
      FormManager.readOnly('isuCd');
    }

    // CREATCMR-7883: set Cluster '00001' as default code
    if (cntry == SysLoc.AUSTRALIA && custSubGrp == "CROSS" && _pagemodel.apCustClusterId == null) {
      FormManager.setValue('apCustClusterId', "00001");
    }

    if (cntry == SysLoc.INDIA && custSubGrp == "CROSS" && _pagemodel.apCustClusterId == null) {
      FormManager.setValue('apCustClusterId', "012D999");
    } else if (cntry == SysLoc.INDIA && custSubGrp == "KYNDR") {
      FormManager.readOnly('apCustClusterId');
      FormManager.readOnly('clientTier');
      FormManager.setValue('isuCd', '5K');
      FormManager.readOnly('isuCd');
      FormManager.readOnly('mrcCd');
      FormManager.setValue('inacCd', '6272');
      FormManager.enable('inacCd');
      FormManager.setValue('inacType', 'I');
      FormManager.readOnly('inacType');
    }


  }
  if (reqType == 'C') {
    setIsuOnIsic();
    onInacTypeChange();
    // CREATCMR-7883-7884
    _inacHandlerANZSG = 0;
    // setInacByCluster();
    setInacNacValuesIN();
    filterInacCd('744', '10215', 'N', 'I529');
    applyClusterFilters();
  }
  if (cntry == '834') {
    // FormManager.removeValidator('clientTier', Validators.REQUIRED);
    addVatValidationforSingapore();
    // CREATCMR-7885
    if (reqType == 'C' && ['ASLOM', 'NRML', 'CROSS'].includes(custSubGrp)) {
      console.log('addAfterConfigAP >>> 834/ASLOM/NRML/CROSS >>> Set Cluster default as BLANK.');
      if (custSubGrp == 'CROSS' && _pagemodel.apCustClusterId == null) {
        FormManager.setValue('apCustClusterId', '00000');
      }
    }
  }
  // CREATCMR-5269
  if (reqType == 'U') {
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

    // CREATCMR-7653
    setDefaultValueForNZCreate();
    setLockIsicNZfromDNB();

  }

  // CREATCMR-7929_NZ_LockISIC4Update
  if (cntry == '796' && reqType == 'U') {
    console.log(">>> NZ-796-U >>> Lock ISIC For UPDATE.");

    FormManager.readOnly('isicCd');
  }
  // CREATCMR-7653
  if (cntry == '796' && reqType == 'C') {
    setDefaultValueForNZCreate();
    setLockIsicNZfromDNB();
  }
 // CREATCMR-10215
  var isDnbRecord = FormManager.getActualValue('findDnbResult');
  if (cntry == '834' && isDnbRecord == 'Accepted') {
    console.log(">>> SG-834 >>> Lock ISIC For D&B Import.");
    FormManager.readOnly('isicCd');
  }
  
  inacValidation();
}

function saveClusterVal() {
  console.log(">>>> saveClusterVal");
  if (oldClusterCd == null) {
    oldClusterCd = FormManager.getActualValue('apCustClusterId');
  }
}

function prospectFilter() {
  var ifProspect = FormManager.getActualValue('prospLegalInd');
  if (dijit.byId('prospLegalInd')) {
    ifProspect = dijit.byId('prospLegalInd').get('checked') ? 'Y' : 'N';
  }
  if (ifProspect == 'Y') {
    custSubGrpHandlerINAUSG();
  }
}

function setInacByCluster() {
  console.log('>>>> setInacByCluster >>>>');
  var _cluster = FormManager.getActualValue('apCustClusterId');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  // CREATCMR-7884
  var _clusterNZ = ['09056', '10114', '10115', '10116', '01147', '08037', '10662', '10663', '00002'];
  var _clusterNZWithAllInac = ['10662', '10663', '01147', '08037', '00002', '12298', '12297', '12299', '11993', '11992' ];
  var _custSubGrpNZWithEmptyInac = ['INTER', 'XPRIV', 'PRIV', 'DUMMY'];
  // CREATCMR-7883
  var _clusterAUWithAllInac = ['00001','08039','12278','12280','12279','12281','12296','12290','12294','12291','12295','12293','12286','12288','12289',
    '12287','12276','12274','12275','12277','12292','12282','12284','12285','12283','12476','12477','12478','12479','12481','08039','11989','11991',
    '11988','11990'];
  var _custSubGrpAUWithEmptyInac = ['INTER', 'XPRIV', 'PRIV', 'DUMMY'];

  // CREATCMR-7885
  var _clusterSG = ['09052', '10144', '10145', '10146', '10147', '10148', '10149', '10150', '10151', '10152', '10153', '10154', '10155', '10156', '10157'];
  var _clusterSGEmptyInac = (_cluster == '00000' && ['PRIV', 'XPRIV', 'BUSPR', 'DUMMY', 'INTER'].includes(custSubGrp));

  // CREATCMR-8504
  var _clusterIndiaMrc3 = ['05224', '04477', '04490', '04467', '05225', '09062', '10193', '10194', '10195', '10196', '10197', '10198', '10199',
    '10200', '10201', '10202', '10203', '10204', '10205', '10206', '10207', '10208', '10209', '10210', '10211', '10212', '10213', '10214',
    '10215'];

  if (cntry == '736' || cntry == '738') {
    return;
  }
  // CREATCMR-8458
  if ((cntry == '796' || cntry == '834' || cntry == '616') && FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (!_cluster) {
    // Empty INAC related when Cluster is not valid >>>> 002005 >>>>
    // CREATCMR-7884 : Empty INAC related when Cluster is not valid
    if (_cluster == '' || _cluster == undefined) {
      console.log('>>>> EMPTY INAC/INACTYPE when cluster is not valid >>>>');
      FormManager.limitDropdownValues(FormManager.getField('inacCd'), []);
      FormManager.limitDropdownValues(FormManager.getField('inacType'), []);
      FormManager.setValue('inacCd', '');
      FormManager.setValue('inacType', '');
    }
    return;
  }
  if (_cluster.includes('BLAN') || _cluster == '05224' || _cluster == '05225' || _cluster == '09062' || _cluster == '09063' || _cluster == '09064' || _cluster == '04477' || _cluster == '04490' || _cluster == '04496' || _cluster == '04467' || _cluster == '04494' || _cluster == '04691' || _cluster == '00035' || _cluster == '00127' || _cluster == '00105' || _cluster == '04470' || _cluster == '04469' || _cluster == '04471' || _cluster == '04485' || _cluster == '04500' || _cluster == '04746' || _cluster == '04744' || _cluster == '04745' || _cluster == '04694' ||
    ((_cluster.includes('04492') || _cluster.includes('04503') || _cluster.includes('04692') || _cluster.includes('04481') || _cluster.includes('04462') || _cluster.includes('04483') || _cluster.includes('00149')) && cntry == '856') || (cntry == '818' && _cluster == '09054') || (cntry == '778' && _cluster == '09194') || (cntry == '856' && (['10690', '09053'].includes(_cluster))) || (cntry == '749' && _cluster == '09050') || (cntry == '852' && _cluster == '09055') ||
    (cntry == '834' && _clusterSG.includes(_cluster)) ||
    (cntry == '616' && !_clusterAUWithAllInac.includes(_cluster)) ||
    (cntry == '796' && (_cluster == '09199' || _cluster == '08794' || _cluster == '08792')) ||
    (cntry == '856' && (['04483', '10690', '10686', '10687', '10688', '10689'].includes(_cluster))) ||
    (cntry == '744' && _clusterIndiaMrc3.includes(_cluster))) {

    FormManager.addValidator('inacCd', Validators.REQUIRED, ['INAC/NAC Code'], 'MAIN_IBM_TAB');
    FormManager.addValidator('inacType', Validators.REQUIRED, ['INAC Type'], 'MAIN_IBM_TAB');
    FormManager.setValue('mrcCd', '2');
    if (((_cluster == '09062' || _cluster == '09063' || _cluster == '09064') && (cntry == '744' || cntry == '615' || cntry == '652')) ||
      (cntry == '818' && _cluster == '09054') || (cntry == '778' && _cluster == '09194') || (cntry == '856' && _cluster == '09053') || (cntry == '749' && _cluster == '09050') || (cntry == '852' && _cluster == '09055') || (cntry == '616' && _cluster == '09057') ||
      (cntry == '834' && _clusterSG.includes(_cluster)) ||
      (cntry == '796' && _clusterNZ.includes(_cluster)) ||
      (cntry == '856' && (['10690', '10686', '10687', '10688', '10689'].includes(_cluster))) ||
      (cntry == '744' && _clusterIndiaMrc3.includes(_cluster))) {
      FormManager.setValue('mrcCd', '3');
    }
    var qParams = {
      _qall: 'Y',
      ISSUING_CNTRY: cntry,
      CMT: '%' + _cluster + '%'
    };
    var inacList = cmr.query('GET.INAC_BY_CLUSTER', qParams);
    if (inacList != null && inacList.length > 0) {
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
            FormManager.setValue('inacCd', inacCdValue[0]);
            if (inacCdValue.length == 1) {
              FormManager.setValue('inacCd', inacCdValue[0]);
            }
          }
        }
      } else {
        FormManager.resetDropdownValues(FormManager.getField('inacType'));
      }

      if (cntry == '856' || cntry == '852' || cntry == '818') {
        if (FormManager.getActualValue('viewOnlyPage') == 'true') {
          return;
        }
        var isInacTypeReadOnlyFromScenarios = TemplateService.isFieldReadOnly('inacType');
        if (isInacTypeReadOnlyFromScenarios) {
          FormManager.readOnly('inacType');
        } else {
          FormManager.enable('inacType');
        }
        _oldClusterSelection = _cluster;
      }

    }
    // CREATCMR-7883
    if (cntry == '616') {
      updateMRCAseanAnzIsa();
    }

    // CREATCMR-7884
    if (cntry == '796' && custSubGrp == 'KYND' && _cluster == '09199') {
      FormManager.readOnly('inacType');
    }

    // CREATCMR-7885
    setInacCdTypeStatus();
  } else {
    console.log('>>>> setInacByCluster ELSE scenario >>>>');
    if (cntry != '744') {
      FormManager.removeValidator('inacCd', Validators.REQUIRED);
      FormManager.removeValidator('inacType', Validators.REQUIRED);
    }
    FormManager.resetDropdownValues(FormManager.getField('inacCd'));
    FormManager.resetDropdownValues(FormManager.getField('inacType'));
    var cluster = FormManager.getActualValue('apCustClusterId');
    
    if ((custSubGrp == 'ASLOM' || custSubGrp == 'XASLO' || custSubGrp == 'CROSS' || custSubGrp == 'KYND') && cluster == '09197') {
      FormManager.readOnly('inacCd');
      FormManager.readOnly('inacType');
    } else {
      FormManager.enable('inacCd');
      FormManager.enable('inacType');
    }
       
    updateMRCAseanAnzIsa();
    // CREATCMR-7884 for 796
    // CREATCMR-7885 for 834
    // if(cntry == '796' && _custSubGrpNZWithEmptyInac.includes(custSubGrp) ||
    // (cntry == '834' && _clusterSGEmptyInac)){
    if ((cntry == '834' && _clusterSGEmptyInac)) {
      FormManager.setValue('inacCd', '');
      FormManager.setValue('inacType', '');
      FormManager.limitDropdownValues(FormManager.getField('inacCd'), []);
      FormManager.limitDropdownValues(FormManager.getField('inacType'), []);
    }
    if (cntry == '834') {
      FormManager.removeValidator('inacCd', Validators.NUMBER);
    }
    // CREATCMR-7885
    setInacCdTypeStatus();

    // CREATCMR-7883-7884
    if ((cntry == '616' && _custSubGrpAUWithEmptyInac.includes(custSubGrp)) || (cntry == '796' && _custSubGrpNZWithEmptyInac.includes(custSubGrp))) {
      FormManager.setValue('inacCd', '');
      FormManager.setValue('inacType', '');
      FormManager.limitDropdownValues(FormManager.getField('inacCd'), []);
      FormManager.limitDropdownValues(FormManager.getField('inacType'), []);
      FormManager.enable('inacType');
      FormManager.enable('inacCd');
    }
    if ((cntry == '616' || cntry == '796') && _inacHandlerANZSG > 2) {
      FormManager.setValue('inacCd', '');
      FormManager.setValue('inacType', '');
    }
    // CREATCMR-7883-7884
    clearInacOnClusterChange(_cluster);
    return;
  }
}
function setInacByClusterHKMO() {
  console.log('>>>> setInacByClusterHKMO >>>>');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var _cluster = FormManager.getActualValue('apCustClusterId');
  var HKClusterList = ['09059', '10175', '10176', '10177', '10178'];
  var MOClusterList = ['09060'];
  if (FormManager.getActualValue('reqType') != 'C' || FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (!_cluster) {
    return;
  }
  if (HKClusterList.includes(_cluster) || MOClusterList.includes(_cluster)) {
    FormManager.addValidator('inacCd', Validators.REQUIRED, ['INAC/NAC Code'], 'MAIN_IBM_TAB');
    FormManager.addValidator('inacType', Validators.REQUIRED, ['INAC Type'], 'MAIN_IBM_TAB');
    var qParams = {
      _qall: 'Y',
      ISSUING_CNTRY: cntry,
      CMT: '%' + _cluster + '%'
    };
    var inacList = cmr.query('GET.INAC_BY_CLUSTER', qParams);
    if (inacList != null) {
      var inacTypeSelected = '';
      var arr = inacList.map(inacList => inacList.ret1);
      inacTypeSelected = inacList.map(inacList => inacList.ret2);
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
      } else if (inacType != '' && inacTypeSelected[0].includes(',IN')) {
        FormManager.resetDropdownValues(FormManager.getField('inacType'));
        var value = FormManager.getField('inacType');
        var cmt = value + ',' + _cluster + '%';
        var value = FormManager.getActualValue('inacType');
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        console.log('>>> setInacByClusterHKMO >>> value = ' + value);
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
            if (inacCdValue.length == 1) {
              FormManager.setValue('inacCd', inacCdValue[0]);
            }
          }
        }
      } else {
        FormManager.clearValue('inacCd');
        FormManager.clearValue('inacType');
        FormManager.resetDropdownValues(FormManager.getField('inacType'));
      }
    }
  } else {
    FormManager.clearValue('inacCd');
    FormManager.clearValue('inacType');
    FormManager.removeValidator('inacCd', Validators.REQUIRED);
    FormManager.removeValidator('inacType', Validators.REQUIRED);
    FormManager.resetDropdownValues(FormManager.getField('inacCd'));
    FormManager.resetDropdownValues(FormManager.getField('inacType'));
    var custSubGrp = FormManager.getActualValue('custSubGrp');
    if (custSubGrp == 'BUSPR' || custSubGrp == 'INTER' || custSubGrp == 'DUMMY') {
      FormManager.setValue('mrcCd', '2');
      if (custSubGrp == 'DUMMY') {
        FormManager.removeValidator('cmrNoPrefix', Validators.REQUIRED);
      }
    } else {
      FormManager.setValue('mrcCd', '3');
    }
    return;
  }
}

var previousCluster = null;
var previousSubScenario = null;
function saveClusterAfterSave() {
  if (previousCluster == null) {
    previousCluster = FormManager.getActualValue('apCustClusterId');
  }
}
function savePreviousSubScenario() {
  if (previousSubScenario == null) {
    previousSubScenario = FormManager.getActualValue('custSubGrp');
  }
}

function checkClusterSubScenarioChanged() {
  if (previousCluster == null) {
    previousCluster = FormManager.getActualValue('apCustClusterId');
  }
  if (previousSubScenario == null) {
    previousSubScenario = FormManager.getActualValue('custSubGrp');
  }

  var currentCluster = FormManager.getActualValue('apCustClusterId');
  var currentSubScenario = FormManager.getActualValue('custSubGrp');

  if (previousCluster + previousSubScenario == currentCluster + currentSubScenario) {
    return false;
  }
  previousCluster = currentCluster;
  previousSubScenario = currentSubScenario;
  return true;
}

function savePreviousSubScenario() {
  if (previousSubScenario == null) {
    previousSubScenario = FormManager.getActualValue('custSubGrp');
  }
}

function checkClusterSubScenarioChanged() {
  if (previousCluster == null) {
    previousCluster = FormManager.getActualValue('apCustClusterId');
  }
  if (previousSubScenario == null) {
    previousSubScenario = FormManager.getActualValue('custSubGrp');
  }

  var currentCluster = FormManager.getActualValue('apCustClusterId');
  var currentSubScenario = FormManager.getActualValue('custSubGrp');

  if (previousCluster + previousSubScenario == currentCluster + currentSubScenario) {
    return false;
  }
  previousCluster = currentCluster;
  previousSubScenario = currentSubScenario;
  return true;
}

function setInacNacValuesIN() {
  console.log('>>>> setInacNacValuesIN >>>>');
  var _cluster = FormManager.getActualValue('apCustClusterId');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  /* Avoid INAC type/code override in page reload */
  if (cntry = '744') {
    if (FormManager.getActualValue('reqType') != 'C' || FormManager.getActualValue('viewOnlyPage') == 'true') {
      return;
    }

    if (previousCluster == null) {
      previousCluster = FormManager.getActualValue('apCustClusterId');
    }
    if (previousCluster == _cluster) {
      return;
    }

    FormManager.setValue('apCustClusterId', _cluster);

    var cntry = FormManager.getActualValue('cmrIssuingCntry');
    var qParams = {
      _qall: 'Y',
      ISSUING_CNTRY: cntry,
      CMT: '%' + _cluster + '%'
    };
    var inacList = cmr.query('GET.INAC_BY_CLUSTER', qParams);
    if (inacList != null && inacList.length > 0) {
      var inacTypeSelected = '';
      var arr = inacList.map(inacList => inacList.ret1);
      inacTypeSelected = inacList.map(inacList => inacList.ret2);
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
      } else if (inacType != '' && inacTypeSelected[0].includes(',IN')) {
        FormManager.resetDropdownValues(FormManager.getField('inacType'));
        var value = FormManager.getField('inacType');
        var cmt = value + ',' + _cluster + '%';
        var value = FormManager.getActualValue('inacType');
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        console.log(value);
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
            FormManager.setValue('inacCd', inacCdValue[0]);
            if (inacCdValue.length == 1) {
              FormManager.setValue('inacCd', inacCdValue[0]);
            }
          }
          FormManager.addValidator('inacCd', Validators.REQUIRED, ['INAC/NAC Code'], 'MAIN_IBM_TAB');
          FormManager.addValidator('inacType', Validators.REQUIRED, ['INAC Type'], 'MAIN_IBM_TAB');
        } else {
          FormManager.resetDropdownValues(FormManager.getField('inacType'));
        }
        FormManager.addValidator('inacCd', Validators.REQUIRED, ['INAC/NAC Code'], 'MAIN_IBM_TAB');
        FormManager.addValidator('inacType', Validators.REQUIRED, ['INAC Type'], 'MAIN_IBM_TAB');
      } else {
        FormManager.resetDropdownValues(FormManager.getField('inacType'));
        FormManager.resetDropdownValues(FormManager.getField('inacCd'));
        FormManager.clearValue('inacType');
        FormManager.clearValue('inacCd');
        FormManager.removeValidator('inacType', Validators.REQUIRED);
        FormManager.removeValidator('inacCd', Validators.REQUIRED);
      }
    }
  }
}
/* ASEAN ANZ GCG ISIC MAPPING */
function setIsuOnIsic() {
  console.log('>>>> setIsuOnIsic >>>>');
  var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  if (FormManager.getActualValue('reqType') != 'C' || (FormManager.getActualValue('viewOnlyPage') == 'true' && !(cmrIssuingCntry == '738'))) {
    return;
  }

  var _cluster = FormManager.getActualValue('apCustClusterId');
  var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  var aseanCntries = ['852', '818', '856', '643', '778', '749', '834'];
  if (_cluster != '') {
    if (_cluster.indexOf(" - ") > 0) {
      _cluster = _cluster.substring(0, _cluster.indexOf(" - "));
    }
    var qParams = {
      _qall: 'Y',
      ISSUING_CNTRY: cmrIssuingCntry,
      CLUSTER: _cluster
    };

    var clusterDesc = cmr.query('GET.DESC_BY_CLUSTER', qParams);
    if (((cmrIssuingCntry == '796') && (clusterDesc[0] != '' && (clusterDesc[0].ret1.includes('S2') || clusterDesc[0].ret1.includes('Default') ||
      clusterDesc[0].ret1.includes('EC') || clusterDesc[0].ret1.includes('Kyndryl')))) || (cmrIssuingCntry == '796' && (clusterDesc[0].ret2.includes('08037')))) {
      FormManager.setValue('repTeamMemberNo', '000000');
      FormManager.readOnly('repTeamMemberNo');
    }
    if ((cmrIssuingCntry == '738' || cmrIssuingCntry == '736') && (clusterDesc[0] != '' && !(clusterDesc[0].ret1.includes('S1')))) {
      return;
    } else if (cmrIssuingCntry == '856' && !(_cluster.includes('04483') || _cluster.includes('10690'))) {
      return;
    } else if (cmrIssuingCntry == '834' && !(_cluster.includes('04462') || _cluster.includes('05219'))) {
      return;
    } else if ((cmrIssuingCntry == '738' || cmrIssuingCntry == '736' || cmrIssuingCntry == '796' || aseanCntries.includes(cmrIssuingCntry)) && (clusterDesc[0] != '' && !(clusterDesc[0].ret1.includes('S&S') || clusterDesc[0].ret1.includes('Strategic')))) {
      // CREATCMR-7884
      var custSubGrp = FormManager.getActualValue('custSubGrp');
      var reqType = FormManager.getActualValue('reqType');
      if (reqType == 'C' && cmrIssuingCntry == '796' && custSubGrp == 'CROSS' && _cluster == '00002') {
        console.log('>>> SET ISU = 34 when C/796/CROSS/00002 condition.');
        FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Z']);
        FormManager.setValue('clientTier', 'Z');
        FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['34']);
        FormManager.setValue('isuCd', '34');
        FormManager.readOnly('isuCd');
      }
      return;
    } else if (cmrIssuingCntry == '616' && ((clusterDesc[0] != '' && !clusterDesc[0].ret1.startsWith('Strategic') && !clusterDesc[0].ret1.startsWith('Signature')) || custSubGrp == 'ECSYS')) {
      // CREATCMR-7883
      return;
    } else if (!(cmrIssuingCntry == '738' || cmrIssuingCntry == '736' || cmrIssuingCntry == '616' || cmrIssuingCntry == '796' || aseanCntries.includes(cmrIssuingCntry)) && (clusterDesc[0] != '' && !(clusterDesc[0].ret1.includes('S&S') || clusterDesc[0].ret1.includes('Strategic')))) {
      return;
    }
  }

  var isicCd = FormManager.getActualValue('isicCd');

  var ISU = [];
  if (isicCd != '') {
    var qParams = {
      _qall: 'Y',
      ISSUING_CNTRY: cmrIssuingCntry,
      REP_TEAM_CD: '%' + isicCd + '%'
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
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
        var cmrNo = FormManager.getActualValue('cmrNo');
        var reqType = FormManager.getActualValue('reqType');
        var role = FormManager.getActualValue('userRole').toUpperCase();
        var custGrp = FormManager.getActualValue('custGrp');
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        if (reqType == 'C' && role == 'PROCESSOR' && custGrp == 'CROSS' && custSubGrp == 'SPOFF') {
          if (cmrNo && cmrNo.length > 0 && !cmrNo.match("^[0-9]+$")) {
            return new ValidationResult({
              id: 'cmrNo',
              type: 'text',
              name: 'cmrNo'
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
    (function () {
      return {
        validate: function () {
          var city1 = FormManager.getActualValue('city1');
          var custNm1 = FormManager.getActualValue('custNm1');
          var custNm2 = FormManager.getActualValue('custNm2');
          var addrTxt = FormManager.getActualValue('addrTxt');
          var addrTxt2 = FormManager.getActualValue('addrTxt2');
          var stateProv = FormManager.getActualValue('stateProv');
          var dept = FormManager.getActualValue('dept');
          if (city1 && city1.length > 0 && !city1.match("([\\/]+$)|^([\\.]+$)|^([-]+$)|^([#]+$)|^([,]+$)|^([:]+$)|^([']+$)|^([&]+$)|^([;]+$)|^([0-9A-Za-z-#;:,'&()\\s+\\.\\/]+$)|^[(]+$|^[)]+$")) {
            return new ValidationResult({
              id: 'city1',
              type: 'text',
              name: 'city1'
            }, false, 'Only the characters listed are allowed to input due to system limitation.' + '   (1) A to Z (uppercase & lowercase)  ' + ' (2) 0 to 9   ' + " (3) '   " + ' (4) ,   '
            + ' (5) :   ' + ' (6) &   ' + '  (7) (   ' + ' (8) ;   ' + ' (9) )   ' + ' (10) -   ' + ' (11)  #   ' + ' (12) .  ' + ' (13) /   ');
          }

          if (custNm1 && custNm1.length > 0
            && !custNm1.match("([\\/]+$)|^([\\.]+$)|^([-]+$)|^([#]+$)|^([,]+$)|^([:]+$)|^([']+$)|^([&]+$)|^([;]+$)|^([0-9A-Za-z-#;:,'&()\\s+\\.\\/]+$)|^[(]+$|^[)]+$")) {
            return new ValidationResult({
              id: 'custNm1',
              type: 'text',
              name: 'custNm1'
            }, false, 'Only the characters listed are allowed to input due to system limitation.' + '   (1) A to Z (uppercase & lowercase)  ' + ' (2) 0 to 9   ' + " (3) '   " + ' (4) ,   '
            + ' (5) :   ' + ' (6) &   ' + '  (7) (   ' + ' (8) ;   ' + ' (9) )   ' + ' (10) -   ' + ' (11)  #   ' + ' (12) .  ' + ' (13) /  ');
          }

          if (custNm2 && custNm2.length > 0
            && !custNm2.match("([\\/]+$)|^([\\.]+$)|^([-]+$)|^([#]+$)|^([,]+$)|^([:]+$)|^([']+$)|^([&]+$)|^([;]+$)|^([0-9A-Za-z-#;:,'&()\\s+\\.\\/]+$)|^[(]+$|^[)]+$")) {
            return new ValidationResult({
              id: 'custNm2',
              type: 'text',
              name: 'custNm2'
            }, false, 'Only the characters listed are allowed to input due to system limitation.' + '   (1) A to Z (uppercase & lowercase)  ' + ' (2) 0 to 9   ' + " (3) '   " + ' (4) ,   '
            + ' (5) :   ' + ' (6) &   ' + '  (7) (   ' + ' (8) ;   ' + ' (9) )   ' + ' (10) -   ' + ' (11)  #   ' + ' (12) .  ' + ' (13) /  ');
          }

          if (addrTxt && addrTxt.length > 0
            && !addrTxt.match("([\\/]+$)|^([\\.]+$)|^([-]+$)|^([#]+$)|^([,]+$)|^([:]+$)|^([']+$)|^([&]+$)|^([;]+$)|^([0-9A-Za-z-#;:,'&()\\s+\\.\\/]+$)|^[(]+$|^[)]+$")) {
            return new ValidationResult({
              id: 'addrTxt',
              type: 'text',
              name: 'addrTxt'
            }, false, 'Only the characters listed are allowed to input due to system limitation.' + '   (1) A to Z (uppercase & lowercase)  ' + ' (2) 0 to 9   ' + " (3) '   " + ' (4) ,   '
            + ' (5) :   ' + ' (6) &   ' + '  (7) (   ' + ' (8) ;   ' + ' (9) )   ' + ' (10) -   ' + ' (11)  #   ' + ' (12) .  ' + ' (13) /  ');
          }
          if (addrTxt2 && addrTxt2.length > 0
            && !addrTxt2.match("([\\/]+$)|^([\\.]+$)|^([-]+$)|^([#]+$)|^([,]+$)|^([:]+$)|^([']+$)|^([&]+$)|^([;]+$)|^([0-9A-Za-z-#;:,'&()\\s+\\.\\/]+$)|^[(]+$|^[)]+$")) {
            return new ValidationResult({
              id: 'addrTxt2',
              type: 'text',
              name: 'addrTxt2'
            }, false, 'Only the characters listed are allowed to input due to system limitation.' + '   (1) A to Z (uppercase & lowercase)  ' + ' (2) 0 to 9   ' + " (3) '   " + ' (4) ,   '
            + ' (5) :   ' + ' (6) &   ' + '  (7) (   ' + ' (8) ;   ' + ' (9) )   ' + ' (10) -   ' + ' (11)  #   ' + ' (12) .  ' + ' (13) /  ');
          }
          if (stateProv && stateProv.length > 0
            && !stateProv.match("([\\/]+$)|^([\\.]+$)|^([-]+$)|^([#]+$)|^([,]+$)|^([:]+$)|^([']+$)|^([&]+$)|^([;]+$)|^([0-9A-Za-z-#;:,'&()\\s+\\.\\/]+$)|^[(]+$|^[)]+$")) {
            return new ValidationResult({
              id: 'stateProv',
              type: 'text',
              name: 'stateProv'
            }, false, 'Only the characters listed are allowed to input due to system limitation.' + '   (1) A to Z (uppercase & lowercase)  ' + ' (2) 0 to 9   ' + " (3) '   " + ' (4) ,   '
            + ' (5) :   ' + ' (6) &   ' + '  (7) (   ' + ' (8) ;   ' + ' (9) )   ' + ' (10) -   ' + ' (11)  #   ' + ' (12) .  ' + ' (13) /  ');
          }
          if (dept && dept.length > 0 && !dept.match("([\\/]+$)|^([\\.]+$)|^([-]+$)|^([#]+$)|^([,]+$)|^([:]+$)|^([']+$)|^([&]+$)|^([;]+$)|^([0-9A-Za-z-#;:,'&()\\s+\\.\\/]+$)|^[(]+$|^[)]+$")) {
            return new ValidationResult({
              id: 'dept',
              type: 'text',
              name: 'dept'
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
  FormManager.addFormValidator((function () {
    return {
      validate: function () {

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

function addEROAttachmentValidator() {
  FormManager.addFormValidator((function () {
    return {
      validate: function () {

        var reqType = FormManager.getActualValue('reqType');
        var custSubType = FormManager.getActualValue('custSubGrp');
        var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
        var reqReason = FormManager.getActualValue('reqReason');

        if (typeof (_pagemodel) != 'undefined') {
          if (reqType == 'U' && reqReason == 'TREC') {
            var id = FormManager.getActualValue('reqId');
            var ret = cmr.query('CHECK_ERO_ATTACHMENT', {
              ID: id
            });
            if (ret == null || ret.ret1 == null) {
              return new ValidationResult(null, false, 'ERO Approval Attachment is required for Temporary Reactivate Reqeusts.');
            } else {
              return new ValidationResult(null, true);
            }
          }
        }

      }
    };
  })(), 'MAIN_ATTACH_TAB', 'frmCMR');
}

function addAttachmentValidator() {
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
        var reqType = FormManager.getActualValue('reqType');
        var custSubType = FormManager.getActualValue('custSubGrp');
        var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
        var reqReason = FormManager.getActualValue('reqReason');
        // var docContent = FormManager.getActualValue('docContent');
        if (typeof (_pagemodel) != 'undefined') {
          if (reqType == 'C' && ((cmrIssuingCntry == '796' && (custSubType == 'ESOSW' || custSubType == 'XESO')) || (cmrIssuingCntry == '616' && custSubType == 'ESOSW')) || (cmrIssuingCntry == '834' && custSubType == 'ASLOM')) {
            var id = FormManager.getActualValue('reqId');
            var ret = cmr.query('CHECK_ESA_MATCH_ATTACHMENT', {
              ID: id
            });
            if (ret == null || ret.ret1 == null) {
              return new ValidationResult(null, false, 'ESA Enrollment Form Attachment tab is required.');
            }
          }
          if (reqType == 'C'
            && (custSubType != 'INTER' && custSubType != 'XINT' && custSubType != 'DUMMY' && custSubType != 'XDUMM' && custSubType != 'BLUMX' && custSubType != 'XBLUM' && custSubType != 'MKTPC'
              && custSubType != 'XMKTP' && custSubType != 'IGF' && custSubType != 'XIGF')) {
            if (cmrIssuingCntry == '615' || cmrIssuingCntry == '652') {
              // For BL and SL check company proof
              var id = FormManager.getActualValue('reqId');
              var ret = cmr.query('CHECK_DNB_MATCH_ATTACHMENT', {
                ID: id
              });
              if (ret == null || ret.ret1 == null) {
                return new ValidationResult(null, false, 'Company Proof in Attachment tab is required.');
              } else {
                return new ValidationResult(null, true);
              }
            }
            else if (cmrIssuingCntry != '796' && cmrIssuingCntry != '616' && cmrIssuingCntry != '834') {
              var id = FormManager.getActualValue('reqId');
              var ret = cmr.query('CHECK_TERRITORY_ATTACHMENT', {
                ID: id
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
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
        var reqType = FormManager.getActualValue('reqType');
        var role = FormManager.getActualValue('userRole').toUpperCase();
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        var cluster = FormManager.getActualValue('apCustClusterId');
        if (reqType == 'C' && role == 'REQUESTER' && (custSubGrp == 'NRML' || custSubGrp == 'AQSTN' || custSubGrp == 'CROSS' || custSubGrp == 'ESOSW') && (cluster == '08033' || cluster == '08034' || cluster == '08035')) {
          var id = FormManager.getActualValue('reqId');
          var ret = cmr.query('CHECK_ECSYS_ATTACHMENT', {
            ID: id
          });
          if (ret == null || ret.ret1 == null) {
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
  console.log('>>>> defaultCMRNumberPrefix >>>>');
  if (FormManager.getActualValue('reqType') == 'U') {
    return

  }
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');

  if ((role == 'PROCESSOR') && (custSubGrp == 'INTER' || custSubGrp == 'XINT')) {
    FormManager.addValidator('cmrNoPrefix', Validators.REQUIRED, ['CmrNoPrefix'], 'MAIN_IBM_TAB');
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
  console.log('>>>> defaultCMRNumberPrefixforSingapore >>>>');
  if (custSubGrp == 'INTER' && cmrIssuingCntry == '834') {
    FormManager.addValidator('cmrNoPrefix', Validators.REQUIRED, ['CmrNoPrefix'], 'MAIN_IBM_TAB');
    FormManager.show('CmrNoPrefix', 'cmrNoPrefix');
  }
}

// CREATCMR-7656
function defaultCMRNumberPrefixforANZ() {
  console.log('>>>> defaultCMRNumberPrefixforANZ >>>>');
  FormManager.removeValidator('cmrNoPrefix', Validators.REQUIRED);
  FormManager.hide('CmrNoPrefix', 'cmrNoPrefix');
  setAnzKuklaFor();
}

// CREATCMR-7656
function setDefaultValueforCustomerServiceCode() {
  console.log('>>>> setDefaultValueforCustomerServiceCode >>>>');
  var reqType = FormManager.getActualValue('reqType');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (custSubGrp == 'NRML' || custSubGrp == 'INTER' || custSubGrp == 'DUMMY' || custSubGrp == 'AQSTN' || custSubGrp == 'BLUMX'
    || custSubGrp == 'MKTPC' || custSubGrp == 'ECSYS' || custSubGrp == 'ESOSW' || custSubGrp == 'CROSS' || custSubGrp == 'XAQST' || custSubGrp == 'XBLUM' ||
    custSubGrp == 'XMKTP' || custSubGrp == 'XESO' || custSubGrp == 'PRIV' || custSubGrp == 'NRMLC' || custSubGrp == 'KYND') {
    FormManager.setValue('engineeringBo', '9920');
    FormManager.readOnly('engineeringBo');
    FormManager.removeValidator('engineeringBo', Validators.REQUIRED);
  }
}

function setMrc4IntDumForASEAN() {
  console.log('>>>> setMrc4IntDumForASEAN >>>>');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  if (cntry == '778' || cntry == '749') {
    return
  }
  if (custSubGrp == 'INTER' || custSubGrp == 'XINT') {
    if (cntry == '818' || cntry == '852') {
      return;
    }
    FormManager.setValue('mrcCd', '2');
    FormManager.enable('isuCd');
  } else if ((custSubGrp == 'DUMMY' || custSubGrp == 'XDUMM') || (['BLUMX', 'PRIV', 'MKTPC'].includes(custSubGrp) && cntry == '749') || (['BLUMX', 'PRIV', 'MKTPC', 'SPOFF'].includes(custSubGrp) && cntry == '834')) {
    FormManager.setValue('mrcCd', '3');
    if (custSubGrp == 'DUMMY' && (cntry == '818' || cntry == '852')) {
      return;
    }
    FormManager.enable('isuCd');
  }

  if (cntry == '856') {
    var isIsuCdeReadOnlyFromScenarios = TemplateService.isFieldReadOnly('isuCd');
    if (isIsuCdeReadOnlyFromScenarios) {
      FormManager.readOnly('isuCd');
    } else {
      FormManager.enable('isuCd');
    }
  }

  // CREATCMR-7885
  if (cntry == '834' && (custSubGrp == 'DUMMY' || custSubGrp == 'INTER' || custSubGrp == 'SPOFF')) {
    console.log('setMrc4IntDumForASEAN >>>> FormManager.readOnly(isuCd) for 834/DUMMY/INTER/SPOFF');
    FormManager.readOnly('isuCd');
  }
}

function setISBUforBPscenario() {
  console.log('>>>> setISBUforBPscenario >>>>');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubGrp = FormManager.getActualValue('custSubGrp');

  if (custSubGrp == 'BUSPR' || custSubGrp == 'XBUSP') {
    FormManager.addValidator('isbuCd', Validators.REQUIRED, ['ISBU'], 'MAIN_IBM_TAB');
  }

  if (role != 'VIEWER' && (custSubGrp == 'BUSPR' || custSubGrp == 'XBUSP')) {
    FormManager.enable('isbuCd');
  } else if (role == 'REQUESTER' || role == 'VIEWER') {
    FormManager.readOnly('isbuCd');
  } 
}

function onCustSubGrpChange() {
  console.log('>>>> onCustSubGrpChange >>>>');
  if (FormManager.getActualValue('reqType') == 'U') {
    return

  }

  dojo.connect(FormManager.getField('custSubGrp'), 'onChange', function (value) {
    console.log('custSubGrp CHANGED here >>>>');
    FormManager.readOnly('subIndustryCd');
    setISBUScenarioLogic();

    // CREATCMR-7653
    if (FormManager.getActualValue('cmrIssuingCntry') == '796' && FormManager.getActualValue('reqType') == 'C') {
      setLockIsicNZfromDNB();
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

    // CREATCMR-7885
    // CREATCMR-7878

    if (FormManager.getActualValue('cmrIssuingCntry') == '834' || FormManager.getActualValue('cmrIssuingCntry') == '615' || FormManager.getActualValue('cmrIssuingCntry') == '652') {
      console.log('No need to reset isuCd for 834/SG >>>>');
    } else {
      resetFieldsAfterCustSubGrpChange();
    }


  });
}

function applyClusterFilters() {
  if (FormManager.getActualValue('reqType') != 'C' || FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  filterAvailableClustersByScenarioSubType('744', ['ECOSY'], ['10654', '10655', '10656', '10657']);
  filterAvailableClustersByScenarioSubType('744', ['NRML'], ['05224', '04477', '04490', '04467', '05225', '10193', '10194', '10195', '10196', '10197', '10198', '10199', '10200', '10201', '10202', '10203', '10204', '10205', '10206', '10207', '10208', '10209', '10210', '10211', '10212', '10213', '10214', '10215']);
  filterAvailableClustersByScenarioSubType('744', ['ESOSW'], ['05224', '04477', '04490', '04467', '05225', '09062', '10193', '10194', '10195', '10196', '10197', '10198', '10199', '10200', '10201', '10202', '10203', '10204', '10205', '10206', '10207', '10208', '10209', '10210', '10211', '10212',
    '10213', '10214', '10215', '10590', '10591', '10592', '10593', '10594', '10595', '10596', '10597', '10598', '10599', '10600', '10601', '10602', '10603', '10604', '10605', '10606', '10607', '10608', '10609', '10610', '10611', '10612',
    '10613', '10614', '10615', '10616', '10617', '10618', '10619', '10620', '10621', '10622', '10623', '10624', '10625', '10626', '10627', '10628', '10629', '10630', '10631', '10632', '10633', '10634', '10635', '10636', '10637', '10638',
    '10639', '10640', '10641', '10642', '10643', '10644', '10645', '10654', '10655', '10656', '10657']);
  filterAvailableClustersByScenarioSubType('744', ['KYNDR'], ['09062']);
  filterAvailableClustersByScenarioSubType('744', ['BLUMX', 'MKTPC', 'IGF', 'PRIV', 'INTER'], ['012D999']);
  filterAvailableClustersByScenarioSubType('744', ['NRMLC'], ['10590', '10591', '10592', '10593', '10594', '10595', '10596', '10597', '10598', '10599', '10600', '10601', '10602', '10603', '10604', '10605', '10606', '10607', '10608', '10609', '10610', '10611', '10612', '10613', '10614', '10615', '10616', '10617', '10618', '10619', '10620', '10621', '10622', '10623', '10624', '10625', '10626', '10627', '10628', '10629', '10630', '10631', '10632', '10633', '10634', '10635', '10636', '10637', '10638', '10639', '10640', '10641', '10642', '10643', '10644', '10645', '012D999']);
  filterAvailableClustersByScenarioSubType('744', ['AQSTN'], ['10590', '10591', '10592', '10593', '10594', '10595', '10596', '10597', '10598', '10599', '10600', '10601', '10602', '10603', '10604', '10605', '10606', '10607', '10608', '10609', '10610', '10611', '10612', '10613', '10614', '10615', '10616', '10617', '10618', '10619', '10620', '10621', '10622', '10623', '10624', '10625', '10626', '10627', '10628', '10629', '10630', '10631', '10632', '10633', '10634', '10635', '10636', '10637', '10638', '10639', '10640', '10641', '10642', '10643', '10644', '10645', '012D999']);

}

function resetFieldsAfterCustSubGrpChange() {
  console.log(">>>> resetInacNacCdAfterCustSubGrpChange >>>>");
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  if ((cntry == '616' || cntry == '834') && custSubGrp == 'KYNDR') {
    FormManager.setValue('isuCd', '5K');
  }
}

function filterAvailableClustersByScenarioSubType(cmrIssuCntry, custSubGrpArray, clusterArray) {
  console.log(">>>> filterAvailableClustersByScenarioSubType >>>>");

  var actualCmrIssuCntry = FormManager.getActualValue('cmrIssuingCntry');
  var actualCustSubGrp = FormManager.getActualValue('custSubGrp');

  if (actualCmrIssuCntry == cmrIssuCntry && custSubGrpArray.includes(actualCustSubGrp)) {
    FormManager.resetDropdownValues(FormManager.getField('apCustClusterId'));
    FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), clusterArray);
    if (clusterArray.length == 1) {
      FormManager.setValue('apCustClusterId', clusterArray[0]);

      if (!['BLUMX', 'MKTPC', 'IGF', 'PRIV', 'INTER'].includes(actualCustSubGrp)) {
        FormManager.addValidator('inacCd', Validators.REQUIRED, ['INAC/NAC Code'], 'MAIN_IBM_TAB');
        FormManager.addValidator('inacType', Validators.REQUIRED, ['INAC Type'], 'MAIN_IBM_TAB');
      }
    }
  }
}

function lockInacNacFieldsByScenarioSubType() {
  console.log(">>>> lockInacNacFieldsByScenarioSubType >>>>");

  if (FormManager.getActualValue('reqType') != 'C' || FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  var shouldLock = false;
  var shouldClear = false;
  var cmrIssuCntry = FormManager.getActualValue('cmrIssuingCntry');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var clusterid = FormManager.getActualValue('apCustClusterId');

  if (cmrIssuCntry == '744') {
    if (['KYNDR'].includes(custSubGrp)) {
      // only inacType should be locked for Kyndryl India
      FormManager.readOnly('inacType');
    } else if (['INTER', 'IGF', 'PRIV'].includes(custSubGrp)) {
      shouldLock = true;
      shouldClear = true;
    }
  }

  if (shouldLock) {
    FormManager.readOnly('inacCd');
    FormManager.readOnly('inacType');
  } else {
    FormManager.enable('inacCd');
    FormManager.enable('inacType');
  }
  if (shouldClear) {
    FormManager.setValue('inacCd', '');
    FormManager.setValue('inacType', '');
  }
}

function setInacNacFieldsRequiredIN() {
  console.log(">>>> setInacNacFieldsRequiredIN >>>>");
  if (FormManager.getActualValue('reqType') != 'C' || FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var shouldBeOptional = false;
  var cmrIssuCntry = FormManager.getActualValue('cmrIssuingCntry');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var clusterid = FormManager.getActualValue('apCustClusterId');
  var inacCd = FormManager.getActualValue('inacCd');
  var inacType = FormManager.getActualValue('inacType');

  if (cmrIssuCntry == '744') {
    if (['ECOSY', 'BLUMX', 'MKTPC', 'IGF', 'PRIV', 'INTER'].includes(custSubGrp)) {
      shouldBeOptional = true;
    }
  }

  if (shouldBeOptional) {
    FormManager.removeValidator('inacCd', Validators.REQUIRED);
    FormManager.removeValidator('inacType', Validators.REQUIRED);
  }

  if (inacCd != '' && inacCd != undefined) {
    FormManager.addValidator('inacType', Validators.REQUIRED, ['INAC Type'], 'MAIN_IBM_TAB');
  } else {
    FormManager.removeValidator('inacType', Validators.REQUIRED);
  }

}

function lockFieldsWithDefaultValuesByScenarioSubType() {
  console.log(">>>> lockFieldsWithDefaultValuesByScenarioSubType >>>>");
  if (FormManager.getActualValue('reqType') != 'C' || FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  var cmrIssuCntry = FormManager.getActualValue('cmrIssuingCntry');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var clusterid = FormManager.getActualValue('apCustClusterId');
  if (['NRMLC', 'AQSTN'].includes(custSubGrp)) {
    FormManager.setValue('isuCd', '34');
    FormManager.setValue('clientTier', 'Q');
    FormManager.readOnly('apCustClusterId');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('mrcCd');
  }



  if (!checkClusterSubScenarioChanged()) {
    return;
  }
}

function setCollectionCd() {
  console.log('>>>> setCollectionCd >>>>');
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
      _qall: 'Y',
      ISSUING_CNTRY: cntry,
      ISBU: '%' + isbuCd + '%'
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
    var cmrResult = FormManager.getActualValue('findCmrResult');
    var custSubGrp = FormManager.getActualValue('custSubGrp');
    if (cmrResult != '' && cmrResult == 'Accepted') {
      FormManager.enable('custGrp');
      FormManager.enable('custSubGrp');
     }
    
    if(custSubGrp =='INTER') {
      FormManager.readOnly('clientTier'); 
    }
  }
}

function addSalesRepNameNoCntryValidator() {
  console.log(">>>repTeamMemberNo<<<===" + FormManager.getActualValue('repTeamMemberNo'));
  console.log(">>>repTeamMemberNo---pID<<<===" + localStorage.getItem("pID"));

  if (localStorage.getItem("pID") != null && (FormManager.getActualValue('repTeamMemberNo'))) {
    FormManager.addFormValidator((function () {
      return {
        validate: function () {
          console.log('>>>> addSalesRepNameCntryValidator >>>>');
          var cmrIssuCntry = FormManager.getActualValue('cmrIssuingCntry');
          var lbl1 = FormManager.getLabel('SalRepNameNo');
          var temp = localStorage.getItem("pID").substring(6);
          var countryCd = (temp == 'PH1') ? '818' : temp;
          if (cmrIssuCntry != countryCd && !(included(cmrIssuCntry))) {
            return new ValidationResult({
              id: 'repTeamMemberNo',
              type: 'text',
              name: 'repTeamMemberNo'
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
  var excludedCntry = ["615", "652", "744", "790", "736", "738", "643", "749", "778", "818", "834", "852", "856", "646", "714", "720", "616", "796"];

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
    FormManager.addFormValidator((function () {
      return {
        validate: function () {
          console.log('>>>> addSalesRepNameCntryValidatorBluepages >>>>');
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
              id: 'repTeamMemberNo',
              type: 'text',
              name: 'repTeamMemberNo'
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
  console.log('>>>> setAbbrevNmLocnOnAddressSave >>>>');
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
      copyTypes.forEach(function (input, i) {
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
        console.log('>>>> set CollectionCode OnAddressSave >>>>');
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
    REQ_ID: zs01ReqId,
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
          // _abbrevNm = "ESA/OEM/SWG_" + custNm1;
          _abbrevNm = "ESA Use Only";
        } else if (custSubGrp == "AQSTN") {
          _abbrevNm = "Acquisition Use Only";
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
          _abbrevNm = "ESA use only";
        } else if (custSubGrp == "BLUMX" || custSubGrp == "XBLUM") {
          _abbrevNm = "Consumer only";
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
          if (reqType == 'C') {
            // CREATCMR-7653
            _abbrevNm = "Bluemix use only";
          } else {
            _abbrevNm = "Bluemix_" + custNm1;
          }
        } else if (custSubGrp == "MKTPC" || custSubGrp == "XMKTP") {
          if (reqType == 'C') {
            // CREATCMR-7653
            _abbrevNm = "Market place use only";
          } else {
            _abbrevNm = "Marketplace_" + custNm1;
          }
        } else if (custSubGrp == "SOFT" || custSubGrp == "XSOFT") {
          _abbrevNm = "Softlayer Use Only";
        } else if (custSubGrp == "IGF" || custSubGrp == "XIGF") {
          _abbrevNm = "IGF_" + custNm1;
        } else if (custSubGrp == "DUMMY" || custSubGrp == "XDUMM") {
          if (reqType == 'C' && custSubGrp == "DUMMY") {
            // CREATCMR-7653
            _abbrevNm = "IGF DUMMY use only";
          } else {
            _abbrevNm = "Dummy_" + custNm1;
          }
        } else if (custSubGrp == "ESOSW" || custSubGrp == "XESO") {
          if (reqType == 'C') {
            // CREATCMR-7653
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
  console.log('>>>> getLandCntryDesc >>>>');
  if (cntryCd != null) {
    reqParam = {
      COUNTRY: cntryCd,
    };
  }
  var results = cmr.query('GET.CNTRY_DESC', reqParam);
  _cntryDesc = results.ret1;
  return _cntryDesc;
}

function onSubIndustryChange() {
  console.log('>>>> onSubIndustryChange >>>>');
  var reqType = null;
  reqType = FormManager.getActualValue('reqType');
  if (reqType == 'U') {
    console.log('>>>> Exit onSubIndustChange for Update.');
    return;
  }
  _subIndCdHandler = dojo.connect(FormManager.getField('subIndustryCd'), 'onChange', function (value) {
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
  console.log('>>>> onInacTypeChange >>>>');
  // CREATCMR-7883
  var _clusterAUWithAllInac = ['01150', '00001', '08039'];
  var _clusterNZWithAllInac = ['10662', '10663', '01147', '08037', '00002'];
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var reqType = null;
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  reqType = FormManager.getActualValue('reqType');
  if (reqType == 'C') {
    if (_inacCdHandler == null) {
      _inacCdHandler = dojo.connect(FormManager.getField('inacType'), 'onChange', function (value) {

        var cluster = FormManager.getActualValue('apCustClusterId');
        var cmt = value + ',' + cluster + '%';
        console.log('onInacTypeChange cluster=' + cluster);
        console.log('onInacTypeChange value=' + value);
        console.log('onInacTypeChange cmt=' + cmt);
        if (value != null && value.length > 0) {
          var inacCdValue = [];
          if (cluster.includes('BLAN') ||
            ((cluster == '04501' || cluster == '04683' || cluster == '04690') &&
              (cntry == SysLoc.HONG_KONG || cntry == SysLoc.MACAO)) || ((cluster.includes('05224') || cluster.includes('05225') || cluster.includes('09062') || cluster.includes('09063') || cluster.includes('09064') || cluster.includes('04477') || cluster.includes('04496') || cluster.includes('04490') || cluster.includes('04494') || cluster.includes('04691') || cluster.includes('04467') || cluster.includes('10195') || cluster.includes('10197')) && (cntry == SysLoc.INDIA || cntry == SysLoc.BANGLADESH || cntry == SysLoc.SRI_LANKA)) ||
            ((cluster.includes('04492') || cluster.includes('04503') || cluster.includes('04692') || cluster.includes('04481') || cluster.includes('04462') || cluster.includes('04483') || cluster.includes('00149') || cluster.includes('05220') || cluster.includes('10690')) && (cntry == SysLoc.THAILAND || cntry == SysLoc.SINGAPORE))) {
            var qParams = {
              _qall: 'Y',
              ISSUING_CNTRY: cntry,
              CMT: cmt,
            };
          } else if (cntry == '616') {
            if (_clusterAUWithAllInac.includes(cluster)) {
              cmt = value + '%';
            }
            var qParams = {
              _qall: 'Y',
              ISSUING_CNTRY: cntry,
              CMT: cmt,
            };
          } else if (cntry == '796') {
            if (_clusterNZWithAllInac.includes(cluster)) {
              cmt = value + '%';
            } else {
              // Get INAC for cluster related ONLY >>>> 002005 >>>>
              // CREATCMR-7884 : Get INAC for cluster related ONLY
              console.log('Get INAC for cluster [' + cluster + '] related ONLY for 796.');
              cmt = value + '%' + cluster + '%';
              console.log('onInacTypeChange >> NEW cmt=' + cmt);
            }
            var qParams = {
              _qall: 'Y',
              ISSUING_CNTRY: cntry,
              CMT: cmt,
            };
          } 
          
          var inacType = FormManager.getActualValue('inacType');
          var inacCd = FormManager.getActualValue('inacCd');
          var clientTier = FormManager.getActualValue('clientTier');
          var isuCd = FormManager.getActualValue('isuCd');
          if (cluster == '09197') {
            FormManager.readOnly('inacCd');
            FormManager.readOnly('inacType');
            FormManager.readOnly('clientTier');
            FormManager.readOnly('isuCd');
          }

          if (qParams != undefined) {
            var results = cmr.query('GET.INAC_CD', qParams);
            if (results != null && results.length > 0) {
              for (var i = 0; i < results.length; i++) {
                inacCdValue.push(results[i].ret1);
              }
              // if (value == 'N' && !(cluster.includes('BLAN')) && cntry ==
              // '616') {
              if (value == 'N' && (!(cluster.includes('BLAN') || cluster.includes('00035') || cluster.includes('00127') || cluster.includes('00105') || cluster.includes('04470') || cluster.includes('04469') || cluster.includes('04471') || cluster.includes('04485') || cluster.includes('04500') || cluster.includes('04746') || cluster.includes('04744') || cluster.includes('04745')) && (cntry == SysLoc.AUSTRALIA))) {
                inacCdValue.push('new');
              }
              if ((value == 'N' && (!((cluster.includes('05224') || cluster.includes('05225') || cluster.includes('09062') || cluster.includes('09063') || cluster.includes('09064') || cluster.includes('04477') || cluster.includes('04496') || cluster.includes('04490') || cluster.includes('04494') || cluster.includes('04691') || cluster.includes('04467') || cluster.includes('10195') || cluster.includes('10197')) && (cntry == SysLoc.INDIA || cntry == SysLoc.BANGLADESH || cntry == SysLoc.SRI_LANKA)))) && cntry != SysLoc.AUSTRALIA && (cntry != SysLoc.THAILAND && cntry != SysLoc.SINGAPORE)) {
                inacCdValue.push('new');
              }
              if (value == 'N' && (!(cluster.includes('04492') || cluster.includes('04503') || cluster.includes('04692') || cluster.includes('04481') || cluster.includes('04462') || cluster.includes('04483') || cluster.includes('00149') || cluster.includes('05220')) && (cntry == SysLoc.THAILAND || cntry == SysLoc.SINGAPORE))) {
                inacCdValue.push('new');
              }
              FormManager.resetDropdownValues(FormManager.getField('inacCd'));
              FormManager.limitDropdownValues(FormManager.getField('inacCd'), inacCdValue);
              FormManager.setValue('inacCd', inacCdValue[0]);
              if (inacCdValue.length == 1) {
                FormManager.setValue('inacCd', inacCdValue[0]);
              }
            } else {
              var inacType = FormManager.getActualValue('inacType');
              cmt = inacType + '%';
              qParams = {
                _qall: 'Y',
                ISSUING_CNTRY: cntry,
                CMT: cmt,
              };
              results = cmr.query('GET.INAC_CD', qParams);
              for (var i = 0; i < results.length; i++) {
                inacCdValue.push(results[i].ret1);
              }
              FormManager.resetDropdownValues(FormManager.getField('inacCd'));
              FormManager.limitDropdownValues(FormManager.getField('inacCd'), inacCdValue);
            }
          }
        }

      });
    }
  }
  var custSubGrpList = ['AQSTN', 'BLUMX', 'MKTPC', 'IGF', 'PRIV', 'NRMLC', 'MKTPC', 'ECOSY', 'ECOSW', 'ESOSY', 'INTER', 'KYNDR', 'CROSS'];
  if (!custSubGrpList.includes(custSubGrp) && cntry == SysLoc.INDIA) {
    FormManager.addValidator('inacType', Validators.REQUIRED, ['INAC Type'], 'MAIN_IBM_TAB');
  }

}

function filterInacCd(cmrIssuCntry, clusters, inacType, inacCd) {
  var actualCmrIssuCntry = FormManager.getActualValue('cmrIssuingCntry');
  var actualCluster = FormManager.getActualValue('apCustClusterId');
  if (FormManager.getActualValue('reqType') != 'C' || FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (actualCmrIssuCntry == cmrIssuCntry && actualCluster == clusters) {
    FormManager.limitDropdownValues(FormManager.getField('inacType'), inacType);
    FormManager.limitDropdownValues(FormManager.getField('inacCd'), inacCd);
    FormManager.setValue('inacCd', inacCd);
    FormManager.setValue('inacType', inacType);
    FormManager.addValidator('inacCd', Validators.REQUIRED, ['INAC/NAC Code'], 'MAIN_IBM_TAB');
    FormManager.addValidator('inacType', Validators.REQUIRED, ['INAC Type'], 'MAIN_IBM_TAB');
  }
}


function setIsicCdIfCmrResultAccepted(value) {
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  console.log(">>>>setDefaultValueForCreate()>>>> SubGrp=" + custSubGrp);
  var reqId = FormManager.getActualValue('reqId');
  var isicCdInDB = '';
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var custSubGroups = ['BLUMX', 'MKTPC', 'AQSTN', 'NRML', 'NRMLC', 'KYNDR', 'ESOSW', 'ECSYS', 'CROSS', 'XAQST', 'XBLUM', 'XESO', 'XMKTP', 'ASLOM'];
  if (custSubGroups.includes(custSubGrp)) {
    var reqIdParams = {
      REQ_ID: reqId,
    };
    var isicCdResult = cmr.query('GET.ISIC_CD_BY_REQID', reqIdParams);

    if (isicCdResult.ret1 != undefined) {
      isicCdInDB = isicCdResult.ret1;
    }
    console.log(">>>>setDefaultValueForCreate()>>>> ISIC_CD in DB =" + isicCdInDB);
    FormManager.setValue('isicCd', isicCdInDB);
    FormManager.enable('isicCd');
    FormManager.enable('subIndustryCd');
  } else {
    FormManager.readOnly('isicCd');
    FormManager.readOnly('subIndustryCd');
    switch (custSubGrp) {
      case 'PRIV':
        // ISIC = 9500, - lock field
        FormManager.setValue('isicCd', '9500');
        break;
      case 'INTER':
        // ISIC = 8888, - lock field
        FormManager.setValue('isicCd', '0000');
        break;
      case 'DUMMY':
        // ISIC = 8888, - lock field
        FormManager.setValue('isicCd', '8888');
        break;
    }
  }
}

function getIsicDataRDCValue() {
  var result = cmr.query('GET.ISIC_OLD_BY_REQID', {
    REQ_ID: FormManager.getActualValue('reqId')
  });
  return result.ret1;
}

function setIsicCdIfDnbResultAccepted(value) {
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var cond2 = new Set([ 'AQSTN', 'BLUMX', 'ESOSW', 'ECSYS', 'MKTPC', 'NRML', 'CROSS', 'SPOFF', 'XBLUM', 'XAQST', 'XMKTP', 'BUSPR', 'ASLOM', 'NRMLC', 'KYNDR' ]);
  var cond3 = new Set([ 'INTER', 'PRIV', 'XPRIV', 'DUMMY', 'IGF' ]);
  if (cond2.has(custSubGrp)) {
    var oldISIC = getIsicDataRDCValue();
    FormManager.setValue('isicCd', oldISIC);
    FormManager.readOnly('isicCd');
  } else if (cond3.has(custSubGrp)) {
    FormManager.setValue('isicCd', value);
    FormManager.readOnly('isicCd');
  } 
  if ((custSubGrp == '' || custSubGrp != '') && value != '') {  
    FormManager.readOnly('isicCd');
  }
}

function setIsicCdIfDnbAndCmrResultOther(value) {
  var value = FormManager.getActualValue('isicCd');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var cond4 = new Set(['INTER', 'PRIV', 'XPRIV', 'DUMMY', 'IGF']);
  if (cond4.has(custSubGrp)) {
    FormManager.setValue('isicCd', value);
    FormManager.readOnly('isicCd');
  } else if (custSubGrp != '') {
    FormManager.setValue('isicCd', '');
    FormManager.enable('isicCd');
  }
  FormManager.setValue('isicCd', _pagemodel.isicCd);
}

function onIsicChange() {
  console.log('>>>> onIsicChange >>>>');
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var custGrp = FormManager.getActualValue('custGrp');
  var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  var value = FormManager.getActualValue('isicCd');
  var cmrResult = FormManager.getActualValue('findCmrResult');
  var dnbResult = FormManager.getActualValue('findDnbResult');
  var dplCheck = FormManager.getActualValue('dplChkResult');
  var apCustClusterId = FormManager.getActualValue('apCustClusterId');
  var clientTier = FormManager.getActualValue('clientTier');
  var cntrySet = new Set(['744', '834', '616']);

  if (reqType != 'C' && role != 'REQUESTER' && !cntrySet.has(cmrIssuingCntry)) {
    return;
  }

  // FormManager.readOnly('isicCd');
  if (cmrResult != '' && cmrResult == 'Accepted') {
    setIsicCdIfCmrResultAccepted(value);
  } else if (dnbResult != '' && dnbResult == 'Accepted') {
    setIsicCdIfDnbResultAccepted(value);
  } else if (cmrResult == 'No Results' || cmrResult == 'Rejected' || dnbResult == 'No Results' || dnbResult == 'Rejected') {
    FormManager.readOnly('isicCd');
    setIsicCdIfDnbAndCmrResultOther(value);
  }
}

function setPrivate() {
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (custSubGrp == 'XAQST' || custSubGrp == 'XMKTP' || custSubGrp == 'XBLUM') {
    FormManager.setValue('custSubGrp', 'XPRIV');
  } else {
    FormManager.setValue('custSubGrp', 'PRIV');
  }
}

function updateIndustryClass() {
  console.log('>>>> updateIndustryClass >>>>');
  var subIndustryCd = FormManager.getActualValue('subIndustryCd');
  if (subIndustryCd != null && subIndustryCd.length > 1) {
    var _industryClass = subIndustryCd.substr(0, 1);
    FormManager.setValue('IndustryClass', _industryClass);
    if (_industryClass == 'Y' || _industryClass == 'G') {
      FormManager.enable('taxCd2');
      FormManager.addValidator('taxCd2', Validators.REQUIRED, ['Government Customer Type'], 'MAIN_IBM_TAB');
    } else {
      FormManager.removeValidator('taxCd2', Validators.REQUIRED);
      FormManager.readOnly('taxCd2');
      FormManager.setValue('taxCd2', '');
    }
    setAnzKuklaFor();
    updateCluster(_industryClass);
  }
}

function setAnzKuklaFor() {
  console.log('>>>> setAnzKukla >>>>');
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  var reqType = FormManager.getActualValue('reqType');
  var cmrIssuCntry = FormManager.getActualValue('cmrIssuingCntry');
  var govCustType = FormManager.getActualValue('taxCd2');
  var industryClass = FormManager.getActualValue('subIndustryCd');
  var subScenariotype = FormManager.getActualValue('custSubGrp');
  var sKukla = '11'

  if (viewOnlyPage == 'true' || reqType != 'C') {
    return;
  }
  if (cmrIssuCntry != '616' && cmrIssuCntry != '796') {
    return;
  }
  if (cmrIssuCntry == '796') {
    if (subScenariotype == 'INTER') {
      sKukla = '81';
    } else if (subScenariotype == 'PRIV') {
      sKukla = '60';
    } else if (industryClass != null && (industryClass.substring(0, 1) == 'Y' || industryClass.substring(0, 1) == 'G')
      && (subScenariotype == 'XESO' || subScenariotype == 'XAQST' || subScenariotype == 'AQSTN' || subScenariotype == 'CROSS'
        || subScenariotype == 'ESOSW' || subScenariotype == 'NRML')) {
      sKukla = '14';
    } else {
      sKukla = '11';
    }
  } else if (cmrIssuCntry == '616') {
    if (subScenariotype == 'INTER' || subScenariotype == 'XINT') {
      sKukla = '81';
    } else if (subScenariotype == 'PRIV' || subScenariotype == 'XPRIV') {
      sKukla = '60';
    } else if ((subScenariotype == 'AQSTN' || subScenariotype == 'NRML' || subScenariotype == 'ESOSW'
      || subScenariotype == 'CROSS' || subScenariotype == 'XAQST' || subScenariotype == 'NRMLC')
      && govCustType != null && govCustType.length == 3) {
      sKukla = govCustType.substring(1, 3);
    } else {
      sKukla = '11';
    }
  }
  FormManager.setValue('custClass', sKukla);
}

function updateCluster(value) {
  console.log('>>>> updateCluster >>>>');
  var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var reqType = FormManager.getActualValue('reqType');
  var cmrIssuCntry = FormManager.getActualValue('cmrIssuingCntry');

  if (viewOnlyPage == 'true' || reqType != 'C') {
    return;
  }
  if (role != 'REQUESTER') {
    return;
  }
  if (cmrIssuCntry != '738' && cmrIssuCntry != '736') {
    return;
  }
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  switch (custSubGrp) {
    case 'NRMLC':
    case 'AQSTN':
    case 'NRML':
      handleCluster(value);
      break;
    default:
    // do nothing
  }
}

function handleCluster(value) {
  console.log('>>>> handleCluster >>>>');
  var industryClass = FormManager.getActualValue('IndustryClass');
  var cmrIssuCntry = FormManager.getActualValue('cmrIssuingCntry');
  if (!value) {
    value = industryClass;
  }
  var clusterValues = [];
  var qParams = {
    CNTRY: cmrIssuCntry,
    TXT: '%' + value + '%'
  };
  var results = cmr.query('GET_CLUSTER_BY_INDUSTRYCLASS', qParams);
  if (results != null && results.ret1) {
    FormManager.setValue('apCustClusterId', results.ret1);
    // FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'),
    // clusterValues);
  }
}

function searchArryAndSetValue(arry, searchValue, fieldID, setValue) {
  console.log('>>>> searchArryAndSetValue >>>>');
  for (var i = 0; i < arry.length; i++) {
    if (arry[i] == searchValue) {
      FormManager.setValue(fieldID, setValue);
    }
  }
}

function addSectorIsbuLogicOnSubIndu() {
  console.log('>>>> addSectorIsbuLogicOnSubIndu >>>>');
  var arryIndCdForSectorCOM = ['K', 'U', 'A'];
  var arryIndCdForSectorDIS = ['D', 'W', 'T', 'R'];
  var arryIndCdForSectorFSS = ['F', 'S', 'N'];
  var arryIndCdForSectorIND = ['M', 'P', 'J', 'V', 'L'];
  var arryIndCdForSectorPUB = ['H', 'X', 'Y', 'G', 'E'];
  var arryIndCdForSectorCSI = ['B', 'C'];
  var arryIndCdForSectorEXC = ['Z'];

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
  console.log('>>>> updateIsbuCd >>>>');
  var _mrcCd = FormManager.getActualValue('mrcCd');
  var _sectorCd = FormManager.getActualValue('sectorCd');
  var _industryClass = FormManager.getActualValue('IndustryClass');
   var subScenariotype = FormManager.getActualValue('custSubGrp');
  var _isbuCd = null;
  if (_sectorCd == null) {
    console.log('>>>> Error, _sectorCd is null');
  }
  if (_industryClass == null) {
    console.log('>>>> Error, _industryClass is null');
  }
  if (_mrcCd == null) {
    console.log('>>>> Error, _mrcCd is null');
  }
  // FormManager.setValue('isbuCd', '');
  if (_mrcCd == '3' && _industryClass != '' && subScenariotype != 'BUSPR') {
    _isbuCd = 'GMB' + _industryClass;
    FormManager.setValue('isbuCd', _isbuCd);
  } else if (_mrcCd == '2' && _sectorCd != '' && _industryClass != '' && subScenariotype != 'BUSPR') {
    _isbuCd = _sectorCd + _industryClass;
    FormManager.setValue('isbuCd', _isbuCd);
  }

  setISBUScenarioLogic();
}

function setISBUScenarioLogic() {
  console.log('>>>> setISBUScenarioLogic >>>>');
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
  }
  
  var cluster = FormManager.getActualValue('apCustClusterId')
  if (cmrIssuingCntry == '818' && (custSubGrp == 'KYND' || custSubGrp == 'ASLOM' || custSubGrp == 'XASLO' || custSubGrp == 'CROSS')) {
    if (cluster == '09197') {
      FormManager.setValue('inacCd', '6272');
      FormManager.setValue('inacType', 'I');
      FormManager.readOnly('inacType');
      FormManager.readOnly('inacCd');
    } else {
      FormManager.setValue('inacCd', '');
      FormManager.setValue('inacType', '');
      FormManager.enable('inacType');
      FormManager.enable('inacCd');
    }
  } 
  if(cluster == '') {
    FormManager.setValue('clientTier', '');
    FormManager.setValue('isuCd', '');
  }
}

function setCtcOnIsuCdChangeASEAN() {
  console.log('>>>> setCtcOnIsuCdChangeASEAN >>>>');
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  isuCd = FormManager.getActualValue('isuCd');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var cluster = FormManager.getActualValue('apCustClusterId');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var reqType = FormManager.getActualValue('reqType');
  var clusterList = ['09050', '09194', '09052', '09053', '09054', '09055'];
  var cntryList = ['643', '749', '778', '818', '834', '856'];
  if (cntryList.includes(cntry) && clusterList.includes(cluster)) {
    return;
  }
  if (isuCd == '5K') {
    if (cntry == '818' || cntry == '852' || cntry == '736' || cntry == '738') {
      return;
    }
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
  console.log('>>>> setCtcOnIsuCdChangeANZ >>>>');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  if (isuCd == null) {
    isuCd = FormManager.getActualValue('isuCd');
  }
  if (isuCd == '5K') {
    if (cntry == '736' || cntry == '738') {
      return;
    }
    FormManager.removeValidator('clientTier', Validators.REQUIRED);
    FormManager.setValue('clientTier', '');
    FormManager.readOnly('clientTier');
  } else {
    if (cntry != '736' && cntry != '738' && cntry != '744') {
      FormManager.enable('clientTier');
    }
    if ((cntry == '616' || cntry == '834') && custSubGrp == 'KYNDR') {
      FormManager.readOnly('clientTier');
    }
    handleObseleteExpiredDataForUpdate();
  }
}
function setCtcOnIsuCdChangeGCG() {
  console.log('>>>> setCtcOnIsuCdChangeGCG >>>>');
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  isuCd = FormManager.getActualValue('isuCd');
  if (isuCd == '5K') {
    FormManager.removeValidator('clientTier', Validators.REQUIRED);
  }
}

function setCtcOnIsuCdChangeISA() {
  console.log('>>>> setCtcOnIsuCdChangeISA >>>>');
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  isuCd = FormManager.getActualValue('isuCd');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (isuCd == '5K' && custSubGrp != 'KYNDR') {
    FormManager.removeValidator('clientTier', Validators.REQUIRED);
    FormManager.setValue('clientTier', '');
    FormManager.readOnly('clientTier');
  } else {
    FormManager.addValidator('clientTier', Validators.REQUIRED, ['Client Tier'], 'MAIN_IBM_TAB');
    // CREATCMR-7878
    var _cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
    if (_cmrIssuingCntry != '615' && _cmrIssuingCntry != '652' && _cmrIssuingCntry != '744') {
      // CREATCMR-7878
      var _cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
      if (_cmrIssuingCntry != '615' && _cmrIssuingCntry != '652' && _cmrIssuingCntry != '744') {
        FormManager.enable('clientTier');
      }
      if (FormManager.getActualValue('reqType') == 'C') {
        FormManager.addValidator('isuCd', Validators.REQUIRED, ['ISU Code'], 'MAIN_IBM_TAB');
      }
    }
    if (FormManager.getActualValue('reqType') == 'C') {
      FormManager.addValidator('isuCd', Validators.REQUIRED, ['ISU Code'], 'MAIN_IBM_TAB');
    }
  }
  handleObseleteExpiredDataForUpdate();
}

function onIsuCdChangeAseanAnzIsa() {
  console.log('>>>> onIsuCdChangeAseanAnzIsa >>>>');
  var reqType = null;
  reqType = FormManager.getActualValue('reqType');
  var cmrIssuingCntry = dojo.byId('cmrIssuingCntry').value;
  var asean_isa_cntries = ['Bangladesh - 615', 'Sri Lanka - 652'];

  if (reqType == 'U') {
    console.log('>>>> Exit onIsuCdChangeAseanAnz for Update.');
    return;
  }
  // FormManager.enable('isuCd');
  _isuHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function (value) {
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
  console.log('>>>> updateMRCAseanAnzIsa >>>>');

  var arryISUCdForMRC3 = ['32', '34', '21', '60', '5K'];
  var scenario = FormManager.getActualValue('custGrp');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var aseanCntries = ['852', '818', '856', '643', '778', '749'];
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var _isuCd = FormManager.getActualValue('isuCd');
  var _clientTier = FormManager.getActualValue('clientTier');
  var cluster = FormManager.getActualValue('apCustClusterId');
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  // CREATCMR-7878
  if (cntry == '778' || cntry == '749' || cntry == '615' || cntry == '652' || cntry == '744') {
    return
  }
  if ((cntry == '856' && ['01251', '08047', '09053', '00000'].includes(cluster)) || (cntry == '818' && ['01231', '08044', '00000', '09054'].includes(cluster)) || (cntry == '778' && ['00000', '08042', '09194'].includes(cluster)) || (cntry == '643' && ['00000'].includes(cluster)) || (cntry == '749' && ['00000', '09050', '08040', '09050'].includes(cluster)) || (cntry == '852' && ['00000', '01277', '01273', '09055', '08046'].includes(cluster)) || (cntry == '616' && ['01150', '71100', '00001', '08039', '09057'].includes(cluster)) || (cntry == '796' && ['01147', '71101', '08037', '00002', '09199'].includes(cluster))) {
    // CREATCMR-7653 : MRC = 2 - lock field
    if (cntry == '796' && custSubGrp == 'DUMMY' && ['00002'].includes(cluster) && _clientTier == 'Z' && _isuCd == '21') {
      FormManager.setValue('mrcCd', '2');
      FormManager.readOnly('mrcCd');
      return;
    }
    if ((custSubGrp == 'INTER' || custSubGrp == 'XINT') && ((['00000'].includes(cluster) && (cntry == '856' || cntry == '818' || cntry == '778' || cntry == '643' || cntry == '749' || cntry == '852')) || (['00002'].includes(cluster) && cntry == '796') || (['00001'].includes(cluster) && cntry == '616')) && _isuCd == '21' && _clientTier == 'Z') {
      FormManager.setValue('mrcCd', '2');
      return;
    }
    if (cntry == '643' && (custSubGrp == 'INTER' || custSubGrp == 'DUMMY') && ['00000'].includes(cluster)) {
      FormManager.setValue('mrcCd', '2');
      return;
    }
    if (['818', '852', '856'].includes(cntry) && (custSubGrp == 'INTER' || custSubGrp == 'DUMMY')) {
      FormManager.setValue('mrcCd', '2');
      return;
    }
    // CREATCMR-7883
    if (cntry == '616' && custSubGrp == 'DUMMY' && ['00001'].includes(cluster)) {
      FormManager.setValue('mrcCd', '2');
      return;
    }
    FormManager.setValue('mrcCd', '3');
    return;
  } else if ((cntry == '856' && ['04483', '04492', '04692', '04481', '05220'].includes(cluster)) || (cntry == '616' && ['00035', '05221', '00105', '04485', '04500', '04746', '04744', '04745', '05222'].includes(cluster)) || (cntry == '796' && ['04694'].includes(cluster))) {
    FormManager.setValue('mrcCd', '2');
    return;
  } else if (cntry == '856' && ['10690', '10686', '10687', '10688', '10689'].includes(cluster) && custSubGrp == 'NRMLS') {
    FormManager.setValue('mrcCd', '3');
    return;
  }
  if ((aseanCntries.includes(cntry) && (custSubGrp == 'DUMMY' || custSubGrp == 'XDUMM')) || (cntry == '749' && (custSubGrp == 'MKTP' || custSubGrp == 'BLUMX' || custSubGrp == 'PRICU' || custSubGrp == 'SPOFF')) && ['00000'].includes(cluster)) {
    FormManager.setValue('mrcCd', '3');
  }
  if ((aseanCntries.includes(cntry)) && (custSubGrp == '' || custSubGrp == 'INTER' || custSubGrp == 'XINT' || custSubGrp == 'DUMMY' || custSubGrp == 'XDUMM')) {
    return;
  }
  if (!(cntry == 744 && (custSubGrp == 'CROSS' || custSubGrp == 'AQSTN' || custSubGrp == 'NRML' || custSubGrp == 'ESOSW'))) {
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
      if (cntry == '818' && (custSubGrp == 'ECSYS' || custSubGrp == 'CROSS' || custSubGrp == 'ASLOM' || custSubGrp == 'BUSPR')) {
        FormManager.setValue('mrcCd', '3');
      } 
    }
    if (scenario == 'LOCAL' && (cntry == '744' || cntry == '615' || cntry == '652') && custSubGrp == 'INTER') {
      FormManager.setValue('mrcCd', '3');
    }
    if (scenario == 'LOCAL' && cntry == '744' && custSubGrp == 'INTER' && cluster == '012D999') {
      FormManager.setValue('mrcCd', '2');
    } else if (scenario == 'LOCAL' && cntry == '744' && ['10654', '10655', '10656', '10657'].includes(cluster)) {
      FormManager.setValue('mrcCd', '3');
    } else if (scenario == 'CROSS' && custSubGrp == 'CROSS' && cntry == '744' && ['10654', '10655', '10656', '10657'].includes(cluster)) {
      FormManager.setValue('mrcCd', '3');
    }
  }

  // CREATCMR-7885
  if (cntry == '834' && ['01241', '00000', '08038'].includes(cluster)) {
    FormManager.setValue('mrcCd', '3');
    if (cluster == '00000' && (custSubGrp == 'INTER' || custSubGrp == 'DUMMY')) {
      console.log('>>>> updateMRCAseanAnzIsa >>>> 834/SG >>>> 00000/INTER/DUMMY >>>> SET mrcCd=2 >>>>');
      FormManager.setValue('mrcCd', '2');
    }
    FormManager.readOnly('mrcCd');
  }
}

function addSectorIsbuLogicOnISU() {
  console.log('>>>> addSectorIsbuLogicOnISU >>>>');
  var arryISUCdForSectorCOM = ['05', '12', '3T'];
  var arryISUCdForSectorDIS = ['18', '19', '1R'];
  var arryISUCdForSectorFSS = ['04', '4F', '31'];
  var arryISUCdForSectorIND = ['14', '15', '4A', '4D', '5E'];
  var arryISUCdForSectorPUB = ['11', '28', '40', '8C'];
  var arryISUCdForSectorCSI = ['5B'];
  var arryISUCdForSectorEXC = ['21'];
  var arryISUCdForSectorGMB = ['32', "34"];

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
  console.log('>>>> updateProvCd');
  FormManager.readOnly('territoryCd');
  _provNmHandler = dojo.connect(FormManager.getField('busnType'), 'onChange', function (value) {
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
  console.log('>>>> updateRegionCd');
  _provNmHandler = dojo.connect(FormManager.getField('busnType'), 'onChange', function (value) {
    if (!value) {
      return;
    }
    if (value != null && value.length > 1) {
      var provCd = FormManager.getActualValue('busnType');
      var _issuingCntry = FormManager.getActualValue('cmrIssuingCntry');
      var qParams = {
        CNTRY: _issuingCntry,
        PROV_CD: provCd,
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

function setCTCIsuByCluster() {
  console.log('>>>> setCTCIsuByCluster >>>>');
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var issuingCntries = ['852', '818', '856', '643', '778', '749', '834', '616', '796', '736', '738'];
  if (reqType != 'C' || FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var _clusterHandler = dojo.connect(FormManager.getField('apCustClusterId'), 'onChange', function (value) {
    var clusterVal = FormManager.getActualValue('apCustClusterId');
    if (!clusterVal) {
      return;
    }
    var _cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
    var _cluster = FormManager.getActualValue('apCustClusterId');
    var scenario = FormManager.getActualValue('custGrp');
    var custSubGrp = FormManager.getActualValue('custSubGrp');
    setInacByCluster();
    if (_cmrIssuingCntry == '616' || _cmrIssuingCntry == '796' || issuingCntries.includes(_cmrIssuingCntry)) {
      return;
    }
    var apClientTierValue = [];
    var isuCdValue = [];
    if (_cluster != '' && _cluster != '') {
      if (_cluster.indexOf(" - ") > 0) {
        _cluster = _cluster.substring(0, _cluster.indexOf(" - "));
      }
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

        if (_cmrIssuingCntry == '744' && (custSubGrp == 'KYNDR')) {
          FormManager.readOnly('apCustClusterId');
          FormManager.readOnly('clientTier');
          FormManager.setValue('isuCd', '5K');
          FormManager.readOnly('isuCd');
          FormManager.readOnly('mrcCd');
          FormManager.setValue('inacCd', '6272');
          FormManager.enable('inacCd');
          FormManager.setValue('inacType', 'I');
          FormManager.readOnly('inacType');
        }

        if (scenario == 'LOCAL' && custSubGrp == 'INTER') {
          FormManager.resetDropdownValues(FormManager.getField('clientTier'));
          FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['0']);
          FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['60']);
          FormManager.setValue('clientTier', '0');
          FormManager.setValue('isuCd', '60');
        }
        else if (apClientTierValue.length == 1) {
          FormManager.limitDropdownValues(FormManager.getField('clientTier'), apClientTierValue);
          FormManager.limitDropdownValues(FormManager.getField('isuCd'), isuCdValue);
          FormManager.setValue('clientTier', apClientTierValue[0]);
          FormManager.setValue('isuCd', isuCdValue[0]);
        }
        else if (apClientTierValue.length > 1 && (_cmrIssuingCntry == '744' || _cmrIssuingCntry == '615' || _cmrIssuingCntry == '652' || _cmrIssuingCntry == '834' || _cmrIssuingCntry == '852')) {
          if ((scenario == 'LOCAL' && ((_cmrIssuingCntry == '744' && (custSubGrp == 'BLUMX' || custSubGrp == 'MKTPC' || custSubGrp == 'IGF' || custSubGrp == 'PRIV')) || ((_cmrIssuingCntry == '615' || _cmrIssuingCntry == '652') && (custSubGrp == 'BLUMX' || custSubGrp == 'MKTPC' || custSubGrp == 'IGF')))) || (_cmrIssuingCntry == '834' && _cluster == '00000' && (custSubGrp == 'BLUMX' || custSubGrp == 'MKTPC' || custSubGrp == 'DUMMY' || custSubGrp == 'PRIV' || custSubGrp == 'XBLUM' || custSubGrp == 'XMKTP' || custSubGrp == 'XDUMM' || custSubGrp == 'XPRIV' || custSubGrp == 'SPOFF')) || (_cmrIssuingCntry == '852' && _cluster == '00000' && (custSubGrp == 'DUMMY' || custSubGrp == 'XDUMM'))) {
            FormManager.resetDropdownValues(FormManager.getField('clientTier'));
            FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Q', 'Y']);
            FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['34']);
            FormManager.setValue('clientTier', 'Q');
            FormManager.setValue('isuCd', '34');
            FormManager.enable('clientTier');

            // fixing issue 8513 for india
            // GB Segment values are not correct and it is not locked
            if (_cmrIssuingCntry == '744' && (custSubGrp == 'BLUMX' || custSubGrp ==
              'MKTPC' || custSubGrp == 'IGF' || custSubGrp == 'PRIV')) {

              FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Z']);
              FormManager.setValue('clientTier', 'Z');
              FormManager.readOnly('clientTier');

              // CREATCMR-7877
              // lock inacCd and inacType
              if (custSubGrp == 'IGF' || custSubGrp == 'PRIV') {
                FormManager.setValue('inacCd', '');
                FormManager.readOnly('inacCd');

                FormManager.setValue('inacType', '');
                FormManager.readOnly('inacType');
              }

            }

          } else if (scenario == 'LOCAL' && custSubGrp == 'INTER') {
            FormManager.resetDropdownValues(FormManager.getField('clientTier'));
            FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['0']);
            FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['60']);
            FormManager.setValue('clientTier', '0');
            FormManager.setValue('isuCd', '60');

            if (_cmrIssuingCntry == '744' && custSubGrp == 'INTER') {
              FormManager.readOnly('clientTier');
              FormManager.readOnly('inacCd');
              FormManager.readOnly('inacType');
            }

          } else {
            FormManager.resetDropdownValues(FormManager.getField('clientTier'));
            FormManager.setValue('clientTier', '');
            FormManager.setValue('isuCd', '');

            // fixing issue 8340 for india
            // GB Segment values are not correct and it is not locked by
            // default for CROSS scenario
            if (_cmrIssuingCntry == '744' && (custSubGrp == 'CROSS')) {
              if (FormManager.getField('apCustClusterId') == '' || FormManager.getField('apCustClusterId') == '012D999') {
                FormManager.setValue('apCustClusterId', '012D999');
                FormManager.resetDropdownValues(FormManager.getField('clientTier'))
                FormManager.setValue('clientTier', 'Z');
                FormManager.readOnly('clientTier');
                FormManager.resetDropdownValues(FormManager.getField('isuCd'))
                FormManager.setValue('isuCd', '34');
                FormManager.readOnly('isuCd');
                FormManager.setValue('mrcCd', '3');
                FormManager.readOnly('mrcCd');
              } else {
                FormManager.enable('clientTier');
                FormManager.enable('isuCd');
                FormManager.enable('inacType');
                FormManager.enable('inacCd');
              }
            } else if (_cmrIssuingCntry == '744' && (custSubGrp == 'KYNDR')) {
              FormManager.readOnly('apCustClusterId');
              FormManager.readOnly('clientTier');
              FormManager.setValue('isuCd', '5K');
              FormManager.readOnly('isuCd');
              FormManager.readOnly('mrcCd');
              FormManager.setValue('inacCd', '6272');
              FormManager.enable('inacCd');
              FormManager.setValue('inacType', 'I');
              FormManager.readOnly('inacType');
            }
          }
        }
      }
      if (clusterDesc[0] != '' && (clusterDesc[0].ret1.includes('S1') || clusterDesc[0].ret1.includes('IA') || clusterDesc[0].ret1.includes('S&S') || clusterDesc[0].ret1.includes('Strategic'))) {
        setIsuOnIsic();
      }
    }
  });
  if (_clusterHandler && _clusterHandler[0]) {
    _clusterHandler[0].onChange();
  }
}

function setCTCIsuByClusterANZ() {
  console.log('>>>> setCTCIsuByClusterANZ >>>>');
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var scenario = FormManager.getActualValue('custGrp');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var issuingCntries = ['852', '818', '856', '643', '778', '749', '834', '616', '796'];
  if (FormManager.getActualValue('viewOnlyPage') == 'true' || reqType != 'C') {
    return;
  }
  var _clusterHandler = dojo.connect(FormManager.getField('apCustClusterId'), 'onChange', function (value) {
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
    if ((scenario == 'LOCAL' && (custSubGrp == 'NRMLC' || custSubGrp == 'NRML' || custSubGrp == 'AQSTN' || custSubGrp == 'ESOSW' || custSubGrp == 'IGF' || custSubGrp == 'SOFT' || custSubGrp == 'PRIV' || custSubGrp == 'BLUMX' || custSubGrp == 'MKTPC' || custSubGrp == 'DUMMY' || custSubGrp == 'INTER'))
      || (scenario == 'CROSS' && (custSubGrp == 'XNRML' || custSubGrp == 'XAQST' || custSubGrp == 'XESO' || custSubGrp == 'XIGF' || custSubGrp == 'XSOFT' || custSubGrp == 'CROSS' || custSubGrp == 'XPRIV' || custSubGrp == 'XMKTP' || custSubGrp == 'XDUMM' || custSubGrp == 'XINT' || custSubGrp == 'XBLUM'))
      || (_cmrIssuingCntry == '616' && scenario == 'LOCAL' && (custSubGrp == 'ECSYS' || custSubGrp == 'KYND'))
    ) {
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
            if (_cmrIssuingCntry == '616' && _cluster == '00001') {
              // CREATCMR-7883
              if (custSubGrp.includes('BLUMX') || custSubGrp == 'CROSS' || custSubGrp == 'PRIV' || custSubGrp == 'XPRIV' || custSubGrp == 'MKTPC') {
                FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Z']);
                FormManager.setValue('clientTier', 'Z');
                FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['21']);
                FormManager.setValue('isuCd', '21');
                FormManager.setValue('mrcCd', '3');
                FormManager.readOnly('clientTier');
                FormManager.readOnly('isuCd');
              } else if (custSubGrp == 'DUMMY' || custSubGrp == 'INTER') {
                FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Z']);
                FormManager.setValue('clientTier', 'Z');
                FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['21']);
                FormManager.setValue('isuCd', '21');
                FormManager.setValue('mrcCd', '2');
                FormManager.readOnly('clientTier');
                FormManager.readOnly('isuCd');
              }
            } else if ((custSubGrp.includes('BLUMX') || custSubGrp.includes('MKTPC') || custSubGrp.includes('DUMMY') || custSubGrp.includes('XDUMM')) && (_cluster == '00001' || _cluster == '00002')) {
              // CREATCMR-7653 cluster/QTC/ISU: default as 00002/Z/21 - lock
              // field
              if (_cmrIssuingCntry == '796' && custSubGrp.includes('DUMMY') && _cluster == '00002') {
                FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Z']);
                FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['21']);
                FormManager.setValue('clientTier', 'Z');
                FormManager.setValue('isuCd', '21');
                FormManager.readOnly('apCustClusterId');
                FormManager.readOnly('clientTier');
                FormManager.readOnly('isuCd');
              } else if (_cmrIssuingCntry == '796' && _cluster == '00002' && (custSubGrp.includes('BLUMX') || custSubGrp.includes('MKTPC'))) {
                FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Z']);
                FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['21']);
                FormManager.setValue('clientTier', 'Z');
                FormManager.setValue('isuCd', '21');
                FormManager.readOnly('apCustClusterId');
                FormManager.readOnly('clientTier');
                FormManager.readOnly('isuCd');
              } else {
                FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Q', 'Y']);
                FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['34']);
                FormManager.setValue('isuCd', '34');
              }
            } else if (custSubGrp.includes('INTER') || custSubGrp.includes('XINT')) {
              FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Z']);
              FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['21']);
              FormManager.setValue('clientTier', 'Z');
              FormManager.setValue('isuCd', '21');
            } else if (_cmrIssuingCntry == '796' && custSubGrp.includes('CROSS') && _cluster == '00002') {
              // CREATCMR-7884
              console.log('>>> 796/CROSS/0002/Z/34 >>> lock QTC/ISU');
              FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Z']);
              FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['34']);
              FormManager.setValue('clientTier', 'Z');
              FormManager.setValue('isuCd', '34');
              FormManager.readOnly('clientTier');
              FormManager.readOnly('isuCd');
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
  });
  if (_clusterHandler && _clusterHandler[0]) {
    _clusterHandler[0].onChange();
  }
  // CREATCMR-7884
  if (FormManager.getActualValue('cmrIssuingCntry') == '796') {
    lockInacForIDMYSG();
  }
}

function setCTCIsuByClusterASEAN() {
  console.log('>>>> setCTCIsuByClusterASEAN >>>>');
  var reqType = FormManager.getActualValue('reqType');
  if (FormManager.getActualValue('viewOnlyPage') == 'true' || reqType != 'C') {
    return;
  }
  FormManager.addValidator('isuCd', Validators.REQUIRED, ['ISU Code'], null);
  var _clusterHandler = dojo.connect(FormManager.getField('apCustClusterId'), 'onChange', function (value) {
    var clusterVal = FormManager.getActualValue('apCustClusterId');
    if (!clusterVal) {
      return;
    }
    var _cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
    var _cluster = FormManager.getActualValue('apCustClusterId');
    var scenario = FormManager.getActualValue('custGrp');
    var custSubGrp = FormManager.getActualValue('custSubGrp');
    var issuingCnt3 = ['818', '856', '852'];
    var issuingCnt4 = ['643'];

    var apClientTierValue = [];
    var isuCdValue = [];
    var issuingCntries = ['852', '818', '856', '778', '749', '834'];
    if (_cluster != '' && _cluster != '') {
      var qParams = {
        _qall: 'Y',
        ISSUING_CNTRY: _cmrIssuingCntry,
        CLUSTER: _cluster,
      };

      var clusterDesc = cmr.query('GET.DESC_BY_CLUSTER', qParams);
      var results = cmr.query('GET.CTC_ISU_BY_CLUSTER_CNTRY', qParams);
      if (results != null) {
        for (var i = 0; i < results.length; i++) {
          // CREATCMR-7885 : Remove duplicate values for Cluster and GB segment
          // to fix bugs.
          if (!apClientTierValue.includes(results[i].ret1)) {
            apClientTierValue.push(results[i].ret1);
          }
          if (!isuCdValue.includes(results[i].ret2)) {
            isuCdValue.push(results[i].ret2);
          }
        }

        if (apClientTierValue.length == 1) {
          FormManager.limitDropdownValues(FormManager.getField('clientTier'), apClientTierValue);
          FormManager.limitDropdownValues(FormManager.getField('isuCd'), isuCdValue);
          FormManager.setValue('clientTier', apClientTierValue[0]);
          FormManager.setValue('isuCd', isuCdValue[0]);

          // CREATCMR-7885
          var sGReadonlyList = ['NRMLC', 'AQSTN', 'XAQST', 'KYND', 'ECSYS'];
          if (_cmrIssuingCntry == '834' && (sGReadonlyList.includes(custSubGrp))) {
            FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), _cluster);
            FormManager.readOnly('apCustClusterId');
          }
        } else if (apClientTierValue.length > 1) {
          // CREATCMR-7885 : ADD CLUSTER/QTC/ISU logic for SG.
          var clusterSgList = ['10144', '10145', '10146', '10147', '10148', '10149', '10150', '10151', '10152', '10153', '10154', '10155', '10156', '10157'];
          if (_cmrIssuingCntry == '834') {
            if (_cluster == '01241' && (custSubGrp == 'NRMLC' || custSubGrp == 'AQSTN' || custSubGrp == 'XAQST' || custSubGrp == 'ASLOM' || custSubGrp == 'CROSS')) {
              FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Q']);
              FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['34']);
              FormManager.setValue('clientTier', 'Q');
              FormManager.setValue('isuCd', '34');
            } else if (_cluster == '09052' && (custSubGrp == 'KYND' || custSubGrp == 'ASLOM' || custSubGrp == 'CROSS')) {
              FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['0']);
              FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['5K']);
              FormManager.setValue('clientTier', '0');
              FormManager.setValue('isuCd', '5K');
            } else if (_cluster == '00000' && (custSubGrp == 'BLUMX' || custSubGrp == 'XBLUM' || custSubGrp == 'MKTPC' || custSubGrp == 'XMKTP' || custSubGrp == 'PRIV' || custSubGrp == 'XPRIV' || custSubGrp == 'BUSPR' || custSubGrp == 'SPOFF' || custSubGrp == 'CROSS')) {
              FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Z']);
              FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['34']);
              FormManager.setValue('clientTier', 'Z');
              FormManager.setValue('isuCd', '34');
            } else if (_cluster == '00000' && custSubGrp == 'INTER') {
              FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['0']);
              FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['60']);
              FormManager.setValue('clientTier', '0');
              FormManager.setValue('isuCd', '60');
            } else if (_cluster == '00000' && custSubGrp == 'DUMMY') {
              FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Z']);
              FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['21']);
              FormManager.setValue('clientTier', 'Z');
              FormManager.setValue('isuCd', '21');
            } else if (_cluster == '08038' && (custSubGrp == 'ECSYS' || custSubGrp == 'ASLOM' || custSubGrp == 'CROSS')) {
              FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Y']);
              FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['36']);
              FormManager.setValue('clientTier', 'Y');
              FormManager.setValue('isuCd', '36');
            } else if (clusterSgList.includes(_cluster) && (custSubGrp == 'NRML' || custSubGrp == 'ASLOM' || custSubGrp == 'CROSS')) {
              FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['T']);
              FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['32']);
              FormManager.setValue('clientTier', 'T');
              FormManager.setValue('isuCd', '32');
            } else {
              FormManager.resetDropdownValues(FormManager.getField('clientTier'));
              FormManager.limitDropdownValues(FormManager.getField('clientTier'), apClientTierValue);
              FormManager.limitDropdownValues(FormManager.getField('isuCd'), isuCdValue);
            }
          } else if (custSubGrp.includes('BLUM') || custSubGrp.includes('MKTP') || custSubGrp.includes('MKP') || ((custSubGrp.includes('DUMMY') || custSubGrp.includes('XDUMM')) && issuingCntries.includes(_cmrIssuingCntry)) || ((custSubGrp.includes('PRICU') || custSubGrp.includes('BLUMX') || custSubGrp.includes('SPOFF')) && _cmrIssuingCntry == '749')) {
            FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Q', 'Y']);
            FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['34']);
            FormManager.setValue('isuCd', '34');
          } else if (custSubGrp.includes('INTER') || custSubGrp.includes('XINT')) {
            FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Z']);
            FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['21']);
            FormManager.setValue('clientTier', 'Z');
            FormManager.setValue('isuCd', '21');
          } else {
            FormManager.resetDropdownValues(FormManager.getField('clientTier'));
            FormManager.limitDropdownValues(FormManager.getField('clientTier'), apClientTierValue);
            FormManager.limitDropdownValues(FormManager.getField('isuCd'), isuCdValue);
          }
        }
        if (issuingCnt4.includes(_cmrIssuingCntry)) {
          setCTCIsuByClusterBrunei();
        }
      }

      if (issuingCnt3.includes(_cmrIssuingCntry)) {
        setCTCIsuByClusterPhVnTh(apClientTierValue, isuCdValue);
      }

      // CREATCMR-7887 Indonesia
      if (FormManager.getActualValue('cmrIssuingCntry') == '749') {
        setCTCIsuByClusterIndonesia();
      }
      // CREATCMR-7886 Malaysia
      if (FormManager.getActualValue('cmrIssuingCntry') == '778') {
        setCTCIsuByClusterMY();
      }
      if (FormManager.getActualValue('cmrIssuingCntry') == '749' || FormManager.getActualValue('cmrIssuingCntry') == '778' || FormManager.getActualValue('cmrIssuingCntry') == '834') {
        lockInacForIDMYSG();
      }

      if (clusterDesc[0] != '' && (clusterDesc[0].ret1.includes('S1') || clusterDesc[0].ret1.includes('IA') || _cluster.includes('BLAN') || clusterDesc[0].ret1.includes('S&S') || clusterDesc[0].ret1.includes('Strategic'))) {
        setIsuOnIsic();
      }

    }
  });
  if (_clusterHandler && _clusterHandler[0]) {
    _clusterHandler[0].onChange();
  }
  // CREATCMR-7887 Indonesia
  if (FormManager.getActualValue('cmrIssuingCntry') == '749') {
    setCTCIsuByClusterIndonesia();
    _clusterHandlerINDONESIA = 1;
  }
  // CREATCMR-7886 Malaysia
  if (FormManager.getActualValue('cmrIssuingCntry') == '778') {
    setCTCIsuByClusterMY();
    _clusterHandlerINDONESIA = 1;
  }
  if (FormManager.getActualValue('cmrIssuingCntry') == '749' || FormManager.getActualValue('cmrIssuingCntry') == '778' || FormManager.getActualValue('cmrIssuingCntry') == '834') {
    lockInacForIDMYSG();
  }

}

// CREATCMR-7878
function setCTCIsuByClusterISA() {
  console.log('>>>> setCTCIsuByClusterISA >>>>');
  var reqType = FormManager.getActualValue('reqType');
  if (FormManager.getActualValue('viewOnlyPage') == 'true' || reqType != 'C') {
    return;
  }
  FormManager.addValidator('isuCd', Validators.REQUIRED, ['ISU Code'], null);
  var _clusterHandler = dojo.connect(FormManager.getField('apCustClusterId'), 'onChange', function (value) {
    var clusterVal = FormManager.getActualValue('apCustClusterId');
    if (!clusterVal) {
      return;
    }
    var _cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
    var _cluster = FormManager.getActualValue('apCustClusterId');
    // Bangladesh && Sri Lanka
    if (FormManager.getActualValue('cmrIssuingCntry') == '615' || FormManager.getActualValue('cmrIssuingCntry') == '652') {
      setCTCIsuByClusterBDLK();
    }
    if (FormManager.getActualValue('cmrIssuingCntry') == '615' || FormManager.getActualValue('cmrIssuingCntry') == '652') {
      lockInacForIDMYSG();
    }


  });
  if (_clusterHandler && _clusterHandler[0]) {
    _clusterHandler[0].onChange();
  }
  // Bangladesh && Sri Lanka
  if (FormManager.getActualValue('cmrIssuingCntry') == '615' || FormManager.getActualValue('cmrIssuingCntry') == '652') {
    setCTCIsuByClusterBDLK();
    _clusterHandlerINDONESIA = 1;
  }

  if (FormManager.getActualValue('cmrIssuingCntry') == '615' || FormManager.getActualValue('cmrIssuingCntry') == '652') {
    lockInacForIDMYSG();
  }

}

function setCTCIsuByClusterBrunei() {
  console.log('>>>> setCTCIsuByClusterBrunei >>>>');
  var issuingCnt4 = ['643'];
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var _cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  if ((custSubGrp.includes('DUMMY') || custSubGrp.includes('BUSPR') || custSubGrp.includes('NRML') || custSubGrp.includes('AQSTN') || custSubGrp.includes('ASLOM') || custSubGrp.includes('CROSS') || custSubGrp.includes('INTER')) && issuingCnt4.includes(_cmrIssuingCntry)) {
    if (issuingCnt4.includes(_cmrIssuingCntry) && custSubGrp.includes('DUMMY')) {
      FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Z']);
      FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['21']);
      FormManager.setValue('isuCd', '21');
      FormManager.setValue('clientTier', 'Z');
    }
    else if (issuingCnt4.includes(_cmrIssuingCntry) && (custSubGrp.includes('BUSPR') || custSubGrp.includes('NRML') || custSubGrp.includes('AQSTN') || custSubGrp.includes('ASLOM') || custSubGrp.includes('CROSS'))) {
      FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Z']);
      FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['34']);
      FormManager.setValue('isuCd', '34');
      FormManager.setValue('clientTier', 'Z');
    }
    else if (issuingCnt4.includes(_cmrIssuingCntry) && custSubGrp.includes('INTER')) {
      FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['0']);
      FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['60']);
      FormManager.setValue('isuCd', '60');
      FormManager.setValue('clientTier', '0');
    }

  }
}
function setIsuByClusterCTC() {
  console.log('>>>> setIsuByClusterCTC >>>>');
  var reqType = FormManager.getActualValue('reqType');
  if (reqType != 'C') {
    return;
  }
  var _clientTierHandler = dojo.connect(FormManager.getField('clientTier'), 'onChange', function (value) {
    if (!value) {
      return;
    }
    var _cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
    var _clientTier = FormManager.getActualValue('clientTier');
    var _cluster = FormManager.getActualValue('apCustClusterId');

    var isuCdValue = [];
    if (_clientTier != '' && _cluster != '') {
      var qParams = {
        _qall: 'Y',
        ISSUING_CNTRY: _cmrIssuingCntry,
        CLIENT_TIER_CD: _clientTier,
        CLUSTER: _cluster,
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
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
        var abn = FormManager.getActualValue('vat');
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        if (custSubGrp == "AQSTN" || custSubGrp == "XAQST" || custSubGrp == "IGF" || custSubGrp == "XIGF" || custSubGrp == "NRML" || custSubGrp == "XNRML" || custSubGrp == "SOFT"
          || custSubGrp == "XSOFT") {
          if (abn && abn.length != 11) {
            return new ValidationResult({
              id: 'vat',
              type: 'text',
              name: 'ABN#'
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

/* STory : 1782082 - Singapore */

function addCmrNoValidator() {
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
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

function removeStateValidatorForHkMoNZ() {
  var _landCntryHandler = dojo.connect(FormManager.getField('landCntry'), 'onChange', function (value) {
    var landCntry = FormManager.getActualValue('landCntry');
    var custGrp = FormManager.getActualValue('custGrp');
    if (FormManager.getActualValue('cmrIssuingCntry') == SysLoc.AUSTRALIA) {
      if (custGrp == 'CROSS') {
        FormManager.resetValidations('stateProv');
      } else {
        FormManager.addValidator('stateProv', Validators.REQUIRED, ['State/Province'], null);
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
          FormManager.addValidator('postCd', Validators.REQUIRED, ['Postal Code'], null);
        FormManager.addValidator('city1', Validators.REQUIRED, ['Suburb'], null);
      }
    }
  });
  if (_landCntryHandler && _landCntryHandler[0]) {
    _landCntryHandler[0].onChange();
  }
}

function setINACState() {
  console.log('>>>> setINACState >>>>');
  var role = null;
  var isuCd = FormManager.getActualValue('isuCd');
  var cmrCntry = FormManager.getActualValue('cmrIssuingCntry');
  var isaCntries = [];
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }
  if (role == 'Requester' && (isuCd == '34' || isuCd == '04' || isuCd == '3T') && cmrCntry == '616') {
    FormManager.addValidator('inacCd', Validators.REQUIRED, ['INAC/NAC Code'], 'MAIN_IBM_TAB');
    FormManager.addValidator('inacType', Validators.REQUIRED, ['INAC Type'], 'MAIN_IBM_TAB');
  } else {
    FormManager.removeValidator('inacCd', Validators.REQUIRED);
  }
}

function onISBUCdChange() {
  console.log('>>>> onISBUCdChange >>>>');
  var reqType = null;
  reqType = FormManager.getActualValue('reqType');
  if (reqType == 'U') {
    console.log('>>>> Exit onISBUCdChange for Update.');
    return;
  }
  _isbuHandler = dojo.connect(FormManager.getField('isbuCd'), 'onChange', function (value) {
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
  console.log('>>>> setCollCdFrIndia >>>>');
  var reqType = FormManager.getActualValue('reqType');
  if (reqType == 'U') {
    return;
  }
  var provCd = FormManager.getActualValue('territoryCd');
  var collCd = null;
  if (provCd != '') {
    var qParams = {
      CNTRY: '744',
      PROV_CD: provCd,
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
  console.log('>>>> setCollCdFrAU >>>>');
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
    if (custNm1 == null || custNm1 == '' || cmr.addressMode == 'updateAddress')
      custNm1 = FormManager.getActualValue('custNm1');
    if (provCd == null || provCd == '' || cmr.addressMode == 'updateAddress')
      provCd = FormManager.getActualValue('stateProv');
    if (['NSW', 'NT', 'ACT'].indexOf(provCd) >= 0) {
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
    if (['VIC', 'TAS'].indexOf(provCd) >= 0) {
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
  _govIndcHandler = dojo.connect(FormManager.getField('govType'), 'onClick', function (value) {
    var viewOnlyPage = FormManager.getActualValue('viewOnlyPage');
    if (viewOnlyPage != 'true' && FormManager.getActualValue('reqType') == 'C') {
      FormManager.resetValidations('taxCd2');
      if (dijit.byId('govType').get('checked')) {
        FormManager.addValidator('taxCd2', Validators.REQUIRED, ['Government Customer Type'], 'MAIN_IBM_TAB');
      } else {
        FormManager.removeValidator('taxCd2', Validators.REQUIRED);
      }
    }
  });
}

var _govCustTypeHandler = null;
function addGovCustTypHanlder() {
  _govIndcHandler = dojo.connect(FormManager.getField('taxCd2'), 'onChange', function (value) {
    // setAbbrevNameforGovType();
    setAnzKuklaFor();
  });
}

function setAbbrevNameforGovType() {
  console.log('>>>> setAbbrevNameforGovType >>>>');
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
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
        var zs01ReqId = FormManager.getActualValue('reqId');
        var addrType = FormManager.getActualValue('addrType');
        qParams = {
          REQ_ID: zs01ReqId,
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
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount == 0) {
          return new ValidationResult(null, false, 'One Sold-To Address is mandatory. Only one address for each address type should be defined when sending for processing.');
        }
        var cmrCntry = FormManager.getActualValue('cmrIssuingCntry');
        var qParams = {
          _qall: 'Y',
          CMR_ISSUING_CNTRY: cmrCntry,
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
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
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
                if ((custName == null || streetAddr == null || postCd == null || city == null)) {
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
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
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
              REQ_ID: req_id,
              CUST_NM1: custNm1,
              CUST_NM2: custNm2,
              ADDR_TXT: addrTxt,
              ADDR_TXT_2: addrTxt2,
              CITY1: city1,
              STATE_PROV: stateProv,
              DEPT: dept,
              LAND_CNTRY: landCntry
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
  console.log('>>>> setFieldsForDoubleCreates >>>>');
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

/* Story : 1782082 - Singapore and defct : 1801410 */

function addDoubleCreateValidatorSG() {
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
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
              KATR6: '834',
              MANDT: cmr.MANDT,
              ZZKV_CUSNO: cmrNumber,
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
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
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
              KATR6: katr6,
              MANDT: cmr.MANDT,
              ZZKV_CUSNO: cmrNumber,
            };
            var record = cmr.query('CHECK_CMR_RDC', qParams);
            var rdcCMRCount = record.ret1;
            qParams = {
              KATR6: '834',
              MANDT: cmr.MANDT,
              ZZKV_CUSNO: cmrNumber,
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
  console.log('>>>> setAddressDetailsForViewAP >>>>');
  var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  var asean_isa_cntries = [SysLoc.BRUNEI, SysLoc.MALASIA, SysLoc.INDONESIA, SysLoc.SINGAPORE, SysLoc.PHILIPPINES, SysLoc.THAILAND, SysLoc.VIETNAM, SysLoc.INDIA, SysLoc.SRI_LANKA, SysLoc.BANGLADESH,
  SysLoc.HONG_KONG, SysLoc.MACAO];
  if ((cmrIssuingCntry == SysLoc.AUSTRALIA || cmrIssuingCntry == SysLoc.NEW_ZEALAND)) {
    $('label[for="city1_view"]').text('Suburb:');
  }
  if (asean_isa_cntries.indexOf(cmrIssuingCntry) > -1) {
    $('label[for="addrTxt_view"]').text("Address.(Flr,Bldg,lvl,Unit.):");
    $('label[for="addrTxt2_view"]').text("Address. Cont1(Street Name, Street No.):");
    $('label[for="dept_view"]').text("Address. Cont2(District,Town,Region):");

  }
}
function lockCustMainNames() {
  console.log('>>>> lockCustMainNames >>>>');
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
  console.log('>>>> setISUDropDownValues >>>>');
  var reqType = FormManager.getActualValue('reqType');
  if (reqType != 'C') {
    return;
  }
  var cluster = FormManager.getActualValue('apCustClusterId');
  var ctc = FormManager.getActualValue('clientTier');
  var isuCd = FormManager.getActualValue('isuCd');
  var _cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');

  // CREATCMR-7653
  if (reqType == 'C' && _cmrIssuingCntry == '796') {
    console.log(">>> setISUDropDownValues() for {796 && CREATE} >>>");
    setDefaultValueForNZCreate();
  }

  if (!cluster || !ctc || !isuCd || _cmrIssuingCntry == '796' || _cmrIssuingCntry == '616' || _cmrIssuingCntry == '834' || _cmrIssuingCntry == '852' || _cmrIssuingCntry == '818' || _cmrIssuingCntry == '856' || _cmrIssuingCntry == '749') {
    return;
  }
  var _cluster = FormManager.getActualValue('apCustClusterId');
  var scenario = FormManager.getActualValue('custGrp');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var issuingCntries = ['852', '818', '856', '643', '778', '749', '834'];

  var apClientTierValue = [];
  var isuCdValue = [];
  if (_cluster != '' && ctc != '') {
    if (_cluster.indexOf(" - ") > 0) {
      _cluster = _cluster.substring(0, _cluster.indexOf(" - "));
    }
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

      if (_cmrIssuingCntry == '744' && (custSubGrp == 'KYNDR')) {
        FormManager.readOnly('apCustClusterId');
        FormManager.readOnly('clientTier');
        FormManager.setValue('isuCd', '5K');
        FormManager.readOnly('isuCd');
        FormManager.readOnly('mrcCd');
        FormManager.setValue('inacCd', '6272');
        FormManager.enable('inacCd');
        FormManager.setValue('inacType', 'I');
        FormManager.readOnly('inacType');
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
          FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Q', 'Y']);
          FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['34']);
          FormManager.setValue('clientTier', 'Q');
          FormManager.setValue('isuCd', '34');
          FormManager.enable('clientTier');

          // fixing issue 8513 for india
          // GB Segment values are not correct and it is not locked
          if (_cmrIssuingCntry == '744' && (custSubGrp == 'BLUMX' || custSubGrp ==
            'MKTPC' || custSubGrp == 'IGF' || custSubGrp == 'PRIV')) {

            FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Z']);
            FormManager.setValue('clientTier', 'Z');
            FormManager.readOnly('clientTier');

            // CREATCMR-7877
            // lock inacCd and inacType
            if (custSubGrp == 'IGF' || custSubGrp == 'PRIV') {
              FormManager.setValue('inacCd', '');
              FormManager.readOnly('inacCd');

              FormManager.setValue('inacType', '');
              FormManager.readOnly('inacType');
            }

          }
        }
        else if (scenario == 'LOCAL' && custSubGrp == 'INTER') {
          FormManager.resetDropdownValues(FormManager.getField('clientTier'));
          FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['0']);
          FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['60']);
          FormManager.setValue('clientTier', '0');
          FormManager.setValue('isuCd', '60');
          // fixing issue 8513 for india
          // GB Segment values are not correct and it is not locked
          // inaccd and inacType should be read only for internal
          if (_cmrIssuingCntry == '744' && (custSubGrp == 'INTER')) {
            FormManager.readOnly('clientTier');
            FormManager.readOnly('inacCd');
            FormManager.readOnly('inacType');
          }
        }
        else {
          FormManager.resetDropdownValues(FormManager.getField('clientTier'));
          FormManager.setValue('clientTier', '');
          FormManager.setValue('isuCd', '');

          // fixing issue 8340 for india
          // GB Segment values are not correct and it is not locked by
          // default for CROSS scenario
          if (_cmrIssuingCntry == '744' && (custSubGrp == 'CROSS')) {
            if (FormManager.getField('apCustClusterId') == '' || FormManager.getField('apCustClusterId') == '012D999') {
              FormManager.setValue('apCustClusterId', '012D999');
              FormManager.resetDropdownValues(FormManager.getField('clientTier'))
              FormManager.setValue('clientTier', 'Z');
              FormManager.readOnly('clientTier');
              FormManager.resetDropdownValues(FormManager.getField('isuCd'))
              FormManager.setValue('isuCd', '34');
              FormManager.readOnly('isuCd');
              FormManager.setValue('mrcCd', '3');
              FormManager.readOnly('mrcCd');
            } else {
              FormManager.enable('clientTier');
              FormManager.enable('isuCd');
              FormManager.enable('inacType');
              FormManager.enable('inacCd');
            }
          } else if (_cmrIssuingCntry == '744' && (custSubGrp == 'KYNDR')) {
            FormManager.readOnly('apCustClusterId');
            FormManager.readOnly('clientTier');
            FormManager.setValue('isuCd', '5K');
            FormManager.readOnly('isuCd');
            FormManager.readOnly('mrcCd');
            FormManager.setValue('inacCd', '6272');
            FormManager.enable('inacCd');
            FormManager.setValue('inacType', 'I');
            FormManager.readOnly('inacType');
          }

        }

      } else if (apClientTierValue.length > 1) {
        if (issuingCntries.includes(_cmrIssuingCntry) && (custSubGrp.includes('BLUM') || custSubGrp.includes('MKTP') || custSubGrp.includes('MKP'))) {
          FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Z']);
          FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['34']);
          FormManager.setValue('clientTier', 'Z');
          FormManager.setValue('isuCd', '34');
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
  console.log('>>>> setCollCd for Singapore >>>>');
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
      copyTypes.forEach(function (input, i) {
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
  console.log('>>>> setCollCdFrSingapore >>>>');
  var custSubType = FormManager.getActualValue('custSubGrp');
  if (custSubType != 'SPOFF')
    return;
  var zs01ReqId = FormManager.getActualValue('reqId');
  var collCd = null;
  var qParams = {
    REQ_ID: zs01ReqId,
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
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
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
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
        var reqId = FormManager.getActualValue('reqId');
        var qParams = {
          _qall: 'Y',
          REQID: reqId
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
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
        var custNm1 = FormManager.getActualValue('custNm1').toUpperCase();;
        var custNm2 = FormManager.getActualValue('custNm2').toUpperCase();;
        var dept = FormManager.getActualValue('dept').toUpperCase();;
        var addrTxt1 = FormManager.getActualValue('addrTxt').toUpperCase();;
        var addrTxt2 = FormManager.getActualValue('addrTxt2').toUpperCase();;
        var city1 = FormManager.getActualValue('city1').toUpperCase();;
        var addrType = FormManager.getActualValue('addrType');
        var address = custNm1 + custNm2 + dept + addrTxt1 + addrTxt2 + city1;
        if ((address.includes("PO BOX") || address.includes("POBOX")) && addrType == "ZS01") {
          return new ValidationResult(null, false, 'Contract address can not contain wording PO BOX');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function validateCustNameForNonContractAddrs() {
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
        var custNm1 = FormManager.getActualValue('custNm1').toUpperCase();
        var custNm2 = FormManager.getActualValue('custNm2').toUpperCase();
        var custNm = custNm1 + custNm2;
        var reqId = FormManager.getActualValue('reqId');
        var addrType = FormManager.getActualValue('addrType');
        var reqType = FormManager.getActualValue('reqType');
        var contractCustNm = cmr.query('GET.CUSTNM_ADDR', {
          REQ_ID: reqId,
          ADDR_TYPE: 'ZS01'
        });
        if (contractCustNm != undefined) {
          zs01CustName = contractCustNm.ret1.toUpperCase() + contractCustNm.ret2.toUpperCase();
        }

        if (zs01CustName != custNm && addrType != "ZS01" && _pagemodel.reqType == 'U') {
          return new ValidationResult(null, false, 'customer name of additional address must be the same as the customer name of contract address');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function validateStreetAddrCont2() {
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
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

// API call for validating ABN for Australia on Save Request and Send for
// Processing
function validateABNForAU() {

  FormManager.addFormValidator((function () {
    return {
      validate: function () {
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
                  REQ_ID: reqId
                };
              }
              var abnRet = cmr.validateABN(vat, reqId, formerAbn);
              if (!abnRet.success) {
                return new ValidationResult({
                  id: 'vat',
                  type: 'text',
                  name: 'vat'
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
  console.log('>>>> getFormerVatAU >>>>');
  var formerVat = '';
  var qParams = {
    REQ_ID: reqId,
  };
  var result = cmr.query('GET.VAT_DATA_RDC', qParams);
  if (result != null) {
    formerVat = result.ret1;
  }
  return formerVat;
}

function getFormerCustNameAU(reqId) {
  console.log('>>>> getFormerCustNameAU >>>>');
  var custNm1 = '';
  var custNM2 = '';
  var formerCustNm = '';
  var qParams = {
    REQ_ID: reqId,
    ADDR_TYPE: "ZS01"
  };
  var result = cmr.query('GET.CUSTNM_DATA_RDC', qParams);
  if (result != null) {
    formerCustNm = result.ret1.toUpperCase() + " " + result.ret2.toUpperCase();
  }
  return formerCustNm;
}

function lockFieldsForIndia() {
  console.log('>>>> lockFieldsForIndia >>>>');
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (reqType == 'U' && role == 'REQUESTER') {
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
  if (reqType == 'C' && !(custSubGrp == 'INTER' || custSubGrp == 'ESOSW')) {
    FormManager.readOnly('cmrNoPrefix');
  }
  if (reqType == 'C' && custSubGrp == 'IGF') {
    FormManager.setValue('isicCd', '8888');
  }

  if (reqType == 'C' && custSubGrp == 'INTER') {
    FormManager.setValue('isicCd', '0000');
  }

  if (reqType == 'C' && custSubGrp == 'PRIV') {
    FormManager.setValue('isicCd', '9500');
  }
}

function lockFieldsForERO() {
  console.log('>>>> Lock Fields for ERO Temporary Reactivate >>>>');
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var reqReason = FormManager.getActualValue('reqReason');
  if (reqType == 'U' && role == 'REQUESTER' && reqReason == 'TREC') {
    FormManager.readOnly('abbrevNm');
    FormManager.readOnly('custPrefLang');
    FormManager.readOnly('isicCd');
    FormManager.readOnly('taxCd1');
    FormManager.readOnly('cmrNo');
    FormManager.readOnly('cmrOwner');
    FormManager.resetValidations('cmrOwner');
    FormManager.readOnly('bpRelType');
    FormManager.readOnly('bpName');
    FormManager.readOnly('busnType');
    FormManager.resetValidations('busnType');
    FormManager.readOnly('miscBillCd');
    FormManager.readOnly('inacType');
    FormManager.readOnly('inacCd');
    FormManager.readOnly('restrictInd');
    FormManager.readOnly('dunsNo');
  }
}

function releaseFieldsFromERO() {
  console.log('>>>> Releasing fields from ERO Temporary Reactivate >>>>');
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var reqReason = FormManager.getActualValue('reqReason');
  if (reqType == 'U' && role == 'REQUESTER' && reqReason != 'TREC') {
    FormManager.enable('abbrevNm');
    FormManager.enable('custPrefLang');
    FormManager.enable('isicCd');

    FormManager.enable('bpRelType');
    FormManager.enable('bpName');
    FormManager.enable('busnType');

    FormManager.enable('miscBillCd');
    FormManager.enable('inacType');
    FormManager.enable('inacCd');
    FormManager.enable('restrictInd');

    FormManager.enable('dunsNo');
  }
}

function lockFieldsForAU() {
  console.log('>>>> lockFieldsForAU >>>>');
  var clusterCd = FormManager.getActualValue('apCustClusterId');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (['ESOSW', 'NRML', 'AQSTN', 'SOFT', 'XAQST', 'CROSS'].includes(custSubGrp) && ['04500', '01150', '08039', '09057'].includes(clusterCd)) {
    FormManager.readOnly('repTeamMemberName');
  } else if (cntry = '796' && role == 'REQUESTER') {
    FormManager.readOnly('repTeamMemberName');
  } else {
    FormManager.enable('repTeamMemberName');
  }
}



// CMR-2830
function addCompanyProofAttachValidation() {
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
        var reqType = FormManager.getActualValue('reqType');
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount == 0 || reqType == 'C') {
          return new ValidationResult(null, true);
        }

        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
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

            if ((updateInd == 'U' || updateInd == 'N')) {
              count++;
            }

          }
          if (count > 0 && checkForCompanyProofAttachment()) {
            return new ValidationResult(null, false, 'Company proof is mandatory since address has been updated or added.');
          } else {
            return new ValidationResult(null, true);
          }
        }
      }
    };
  })(), 'MAIN_ATTACH_TAB', 'frmCMR');
}

function checkForCompanyProofAttachment() {
  var id = FormManager.getActualValue('reqId');
  var ret = cmr.query('CHECK_DNB_MATCH_ATTACHMENT', {
    ID: id
  });
  if (ret == null || ret.ret1 == null) {
    return true;
  } else {
    return false;
  }
}

// CMR-2830
function addressNameSimilarValidator() {
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
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
            REQ_ID: reqId,
          };
          var record = cmr.query('GETZS01VALRECORDS', qParams_z);
          var zs01Reccount = record.ret1;
          if (zs01Reccount == 0) {
            return new ValidationResult(null, true);
          }
          var qParams = {
            REQ_ID: reqId,
            ADDR_TYPE: "ZS01",
          };
          var result = cmr.query('ADDR.GET.CUSTNM1.BY_REQID_ADDRTYP', qParams);
          var zs01Name = result != undefined ? result.ret1.concat(result.ret2) : '';
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
            if (((reqType == 'U' && (updateInd == 'U' || updateInd == 'N')) || reqType == 'C') && type != 'ZS01' && zs01Name != name) {
              count++;
            }
          }
          if (count > 0) {
            return new ValidationResult(null, false, 'All Updated / New address customer name should be same as Mailing address.');
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

function addValidatorBasedOnCluster() {
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
        var custSubType = FormManager.getActualValue('custSubGrp');
        var cluster = FormManager.getActualValue('apCustClusterId');
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        if (FormManager.getActualValue('reqType') != 'C' || cntry == '616' || cntry == '778' || cntry == '749' || cntry == '834') {
          return new ValidationResult(null, true);
        }
        if ((cntry == '856' && (custSubType == 'CROSS' || custSubType == 'XASLM') && cluster == '08047') ||
          (cntry == '818' && (custSubType == 'CROSS' || custSubType == 'XASLO') && cluster == '08044') ||
          (cntry == '852' && (custSubType == 'CROSS' || custSubType == 'XASLM') && cluster == '08046')) {
          return new ValidationResult(null, true);
        }
        if ((custSubType != 'ECSYS' && custSubType != 'XECO' && custSubType != 'ASLOM' && custSubType != 'ESOSW') && (cluster == '08039' || cluster == '08037' || cluster == '08038' || cluster == '08040' || cluster == '08042' || cluster == '08044' || cluster == '08047' || cluster == '08046')) {
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

  if (!abbvNmRegex.test(abbvNmVal) || bpRelTypeValChange) {
    var bpRelTypeVal = FormManager.getActualValue('bpRelType');
    if (bpRelTypeVal == 'DS') {
      bpRelPrefix = 'Dis2_';
    } else if (bpRelTypeVal == 'SP') {
      bpRelPrefix = 'Sol1_';
    } else if (bpRelTypeVal == 'RS') {
      bpRelPrefix = 'Res3_';
    }
  }

  if (bpRelPrefix != '') {
    return bpRelPrefix + custNm1;
  } else {
    return custNm1;
  }
}

function setAbbrvNameBPScen() {
  console.log('>>>> setAbbrvNameBPScen >>>>');
  var scenario = FormManager.getActualValue('custSubGrp');

  if (scenario == 'BUSPR' || scenario == 'XBUSP') {
    var zs01ReqId = FormManager.getActualValue('reqId');
    var qParams = {
      REQ_ID: zs01ReqId,
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
  console.log('>>>> lockAbbvNameOnScenarioChangeGCG >>>>');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var reqType = FormManager.getActualValue('reqType');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');

  if (role == 'REQUESTER' && reqType == 'C' && (cntry == SysLoc.HONG_KONG || cntry == SysLoc.MACAO)) {
    FormManager.readOnly('abbrevNm');
  }
}

function handleExpiredClusterGCG() {
  console.log('>>>> handleExpiredClusterGCG >>>>');
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
  console.log('>>>> getAPClusterDataRdc >>>>');
  var clusterDataRdc = '';
  var reqId = FormManager.getActualValue('reqId');
  var qParams = {
    REQ_ID: reqId,
  };
  var result = cmr.query('GET.CLUSTER_DATA_RDC', qParams);
  if (result != null) {
    clusterDataRdc = result.ret1;
  }
  return clusterDataRdc;
}

function checkClusterExpired(clusterDataRdc) {
  console.log('>>>> checkClusterExpired >>>>');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var qParams = {
    ISSUING_CNTRY: cntry,
    AP_CUST_CLUSTER_ID: clusterDataRdc,
  };
  var results = cmr.query('CHECK.CLUSTER', qParams);
  if (results != null && results.ret1 == '1') {
    return false;
  }
  return true;
}

function checkExpiredData() {
  console.log('>>>> checkExpiredData >>>>');
  var reqId = FromManager.getActualValue('reqId')
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var DataRdc = {};

  var qParams = {
    REQ_ID: reqId,
  };
  var records = cmr.query('SUMMARY.OLDDATA', qParams);
  if (records != null && records.size() > 0) {
    return false;
  }
  return true;
}

function addCtcObsoleteValidator() {
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
        var reqType = FormManager.getActualValue('reqType');
        var reqId = FormManager.getActualValue('reqId');
        var clientTier = FormManager.getActualValue('clientTier');
        var oldCtc;
        var qParams = {
          REQ_ID: reqId
        };

        var result = cmr.query('GET.DATA_RDC.CLIENT_TIER_REQID', qParams);
        if (result != null && result != '') {
          var oldCtc = result.ret1;
        }

        if (clientTier == "T" && FormManager.getActualValue('cmrIssuingCntry') == '856') {
          console.log('>>> Skip CTC Obsolete Validator clientTier = T for TH');
          return new ValidationResult(null, true);
        }

        if (clientTier == "T" && FormManager.getActualValue('cmrIssuingCntry') == '744') {
          console.log('>>> Skip CTC Obsolete Validator clientTier = T for IN');
          return new ValidationResult(null, true);
        }

        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        if (reqType == 'C' && (clientTier == "4" || clientTier == "6" || clientTier == "A" || clientTier == "B" || clientTier == "M" || clientTier == "V" || clientTier == "T" || clientTier == "S" || clientTier == "N" || clientTier == "C")) {
          // CREATCMR-7884
          if (clientTier == "T" && cntry == '796') {
            var custSubGrp = FormManager.getActualValue('custSubGrp');
            var custSubGrpList = ['NRML', 'ESOSW', 'XESO', 'CROSS'];
            if (custSubGrpList.includes(custSubGrp)) {
              console.log('>>> Skip CTC Obsolete Validator for NRML/ESOSW/XESO/CROSS when clientTier = T');
              return new ValidationResult(null, true);
            }
            if (clientTier == "T" && (cntry == '736' || cntry == '738')) {
              return new ValidationResult(null, true);
            }
            if (clientTier == "T" && cntry == '616') {
              console.log('>>> Skip CTC Obsolete Validator clientTier = T for AU');
              return new ValidationResult(null, true);
            }
            // CREATCMR-7887
            if (cntry == '778' || cntry == '749' || cntry == '834') {
              return new ValidationResult(null, true);
            }
            return new ValidationResult(null, false, 'Client tier is obsoleted. Please select valid value from list.');
          } else if (reqType == 'U' && oldCtc != null && oldCtc != clientTier && (clientTier == "4" || clientTier == "6" || clientTier == "A" || clientTier == "B" || clientTier == "M" || clientTier == "V" || clientTier == "T" || clientTier == "S" || clientTier == "N" || clientTier == "C")) {
            if (clientTier == "T" && (cntry == '736' || cntry == '738' || cntry == '834')) {
              return new ValidationResult(null, true);
            }
            return new ValidationResult(null, false, 'Client tier is obsoleted. Please select valid Client tier value from list.');
          } else {
            return new ValidationResult(null, true);
          }
        } else if (reqType == 'U' && oldCtc != null && oldCtc != clientTier && (clientTier == "4" || clientTier == "6" || clientTier == "A" || clientTier == "B" || clientTier == "M" || clientTier == "V" || clientTier == "T" || clientTier == "S" || clientTier == "N" || clientTier == "C")) {
          if (clientTier == "T" && (cntry == '736' || cntry == '738' || cntry == '834')) {
            return new ValidationResult(null, true);
          }
          return new ValidationResult(null, false, 'Client tier is obsoleted. Please select valid Client tier value from list.');
        } else {
          return new ValidationResult(null, true);
        }
      }
    }
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

var _custSubGrpHandler = null;
function custSubGrpHandlerINAUSG() {
  console.log('>>>> custSubGrpHandler >>>>');
  if (_custSubGrpHandler == null) {
    _custSubGrpHandler = dojo.connect(FormManager.getField('custSubGrp'), 'onChange', function (value) {
      onIsicChange();
    });
  }
}


function clusterCdValidatorAU() {
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
        var clusterCd = FormManager.getActualValue('apCustClusterId');
        var custSubGrp = FormManager.getActualValue('custSubGrp');

        if (['ESOSW', 'NRML', 'AQSTN', 'SOFT', 'XAQST', 'CROSS'].includes(custSubGrp)) {
          if (clusterCd == '00001') {
            return new ValidationResult({
              id: 'apCustClusterId',
              type: 'text',
              name: 'apCustClusterId'
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
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
        var custSubType = FormManager.getActualValue('custSubGrp');
        var cluster = FormManager.getActualValue('apCustClusterId');
        if (FormManager.getActualValue('reqType') != 'C') {
          return new ValidationResult(null, true);
        }
        var applicableScenarios = ["ASLOM", "BUSPR", "NRML", "CROSS", "AQSTN", "XAQST"];
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
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
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
          if (!result) {
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
    REQID: reqId
  });
  var importInd = 'N';
  if (results != null && results.ret1) {
    importInd = results.ret1;
  }
  console.log('import indicator value for request ID: ' + reqId + ' is ' + importInd);
  return importInd;
}

function lockInacTypeForIGF() {
  console.log('>>>> lockInacTypeForIGF >>>>');
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
        FormManager.setValue('inacType', '');
        FormManager.readOnly('inacType');
      }
    }
  }
}

function lockInacCodeForIGF() {
  console.log('>>>> lockInacCodeForIGF >>>>');
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
      _importIndIN = existingCmr;
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
    FormManager.addValidator('vat', Validators.REQUIRED, ['UEN#'], 'MAIN_CUST_TAB');
  }
}
// CREATCMR-5258
// CREATCMR -5269
function handleObseleteExpiredDataForUpdate() {
  console.log('>>>> handleObseleteExpiredDataForUpdate >>>>');
  var reqType = FormManager.getActualValue('reqType');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  if (reqType != 'U' || FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  // lock all the coverage fields and remove validator
  if (reqType == 'U' && cntry != SysLoc.HONG_KONG || cntry != SysLoc.MACAO) {
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
  if (reqType == 'U' && cntry == SysLoc.HONG_KONG || cntry == SysLoc.MACAO) {
    FormManager.readOnly('apCustClusterId');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('inacType');
    FormManager.readOnly('inacCd');

    // setting all fields as not Mandt for update Req
    FormManager.removeValidator('apCustClusterId', Validators.REQUIRED);
    FormManager.removeValidator('clientTier', Validators.REQUIRED);
    FormManager.removeValidator('inacType', Validators.REQUIRED);
    FormManager.removeValidator('inacCd', Validators.REQUIRED);
  }
}

function executeBeforeSubmit() {
  console.log('>>>> executeBeforeSubmit >>>>');
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
  console.log('>>>> checkAnyChangesOnCustNameAddrGST >>>>');
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
        if (updateInd == 'U' || updateInd == 'N') {
          isUpdated = true;
          break;
        }
      }
    }
  }
  if (!isUpdated) {
    var currentGst = FormManager.getActualValue('vat');
    var qParams = {
      REQ_ID: FormManager.getActualValue('reqId'),
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
  console.log('>>>> vatRegistrationForSG >>>>');
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var reqId = FormManager.getActualValue('reqId');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var addrType = FormManager.getActualValue('addrType');
  var landCntry = '';
  if (addrType == 'ZS01') {
    landCntry = FormManager.getActualValue('landCntry');
  }
  if (landCntry == '') {
    var params = {
      REQ_ID: reqId,
      ADDR_TYPE: "ZS01"
    };
    var landCntryResult = cmr.query('ADDR.GET.LAND_CNTRY.BY_REQID', params);
    landCntry = landCntryResult.ret1;
  }
  if (cntry == '834' && landCntry == 'TH') {
    FormManager.addValidator('taxCd1', Validators.REQUIRED, ['Vat Registration Status'], 'MAIN_IBM_TAB');
    var isVatRegistered = FormManager.getActualValue('taxCd1');
    if (isVatRegistered == 'NA') {
      FormManager.readOnly('vat');
      FormManager.setValue('vat', '');
      FormManager.removeValidator('vat', Validators.REQUIRED);
    } else {
      FormManager.addValidator('vat', Validators.REQUIRED, ['VAT'], 'MAIN_IBM_TAB');
      FormManager.enable('vat');
    }
  }
}

function displayVatRegistrartionStatus() {
  console.log(">>> Executing displayVatRegistrationStatus");
  var reqId = FormManager.getActualValue('reqId');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var addrType = FormManager.getActualValue('addrType');
  var landCntry = '';
  if (addrType == 'ZS01') {
    landCntry = FormManager.getActualValue('landCntry');
  }
  if (landCntry == '') {
    var params = {
      REQ_ID: reqId,
      ADDR_TYPE: "ZS01"
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
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
        var reqType = FormManager.getActualValue('reqType');
        var custSubType = FormManager.getActualValue('custSubGrp');
        var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
        if (typeof (_pagemodel) != 'undefined') {
          if (cmrIssuingCntry == '834' && reqType == 'C' && (custSubType == 'BUSPR' || custSubType == 'XBUSP')) {
            var id = FormManager.getActualValue('reqId');
            var ret = cmr.query('CHECK_BUSP_MATCH_ATTACHMENT', {
              ID: id
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
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
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
            REQ_ID: reqId,
          };
          var record = cmr.query('GETZS01VALRECORDS', qParams_z);
          var zs01Reccount = record.ret1;
          if (zs01Reccount == 0) {
            return new ValidationResult(null, true);
          }
          var qParams = {
            REQ_ID: reqId,
            ADDR_TYPE: "ZS01",
          };
          var result = cmr.query('ADDR.GET.CUSTNM1.BY_REQID_ADDRTYP', qParams);
          var zs01Name = result != undefined ? result.ret1.concat(result.ret2) : '';
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
            name = name1 + name2;
            if ((reqType == 'U' || reqType == 'C') && type != 'ZS01' && zs01Name != name) {
              count++;
            }
          }
          if (count > 0) {
            return new ValidationResult(null, false, 'All Additional address customer name should be same as Mailing address.');
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

// CREATCMR-6358
function addCompanyProofForSG() {
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
        var reqType = FormManager.getActualValue('reqType');
        var hasAdditionalAddr = false;
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount == 0) {
          return new ValidationResult(null, true);
        }
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
            if (CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i).addrType != 'ZS01') {
              hasAdditionalAddr = true;
              break;
            }
          }
        }
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
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

            if ((updateInd == 'U' || updateInd == 'N')) {
              count++;
            }

          }
          if ((count > 0 || hasAdditionalAddr) && checkForCompanyProofAttachment()) {
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
  console.log('>>>> setRepTeamMemberNo >>>>');
  var reqType = FormManager.getActualValue('reqType');
  if (reqType == 'C') {
    FormManager.setValue('repTeamMemberNo', '000000');
    FormManager.readOnly('repTeamMemberNo');
    FormManager.readOnly('repTeamMemberName');
  }
}

// CREATCMR-6358
function additionalAddrNmValidator() {
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
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
            REQ_ID: reqId,
          };
          var record = cmr.query('GETZS01VALRECORDS', qParams_z);
          var zs01Reccount = record.ret1;
          if (zs01Reccount == 0) {
            return new ValidationResult(null, true);
          }
          var qParams = {
            REQ_ID: reqId,
            ADDR_TYPE: "ZS01",
          };
          var result = cmr.query('ADDR.GET.CUSTNM1.BY_REQID_ADDRTYP', qParams);
          var zs01Name = result != undefined ? result.ret1.concat(result.ret2) : '';

          if (!FormManager.getField('addrType_ZS01').checked && currentCustNm1 != '' && currentCustNm != zs01Name) {
            return new ValidationResult({
              id: 'custNm1',
              type: 'text',
              name: 'custNm1'
            }, false, 'Additional address customer name should be same as Mailing address.');
          }
          return new ValidationResult(null, true);
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}
var _customerTypeHandler = null;
function addCustGrpHandler() {
  if (_customerTypeHandler == null) {
    _customerTypeHandler = dojo.connect(FormManager.getField('custGrp'), 'onChange', function (value) {
      var cntry = FormManager.getActualValue('cmrIssuingCntry');
      var custGrp = FormManager.getActualValue('custGrp');
      var reqType = FormManager.getActualValue('reqType');
      var apaCntry = ['834', '818', '856', '778', '749', '643', '852', '744', '615', '652', '616', '796', '641', '738', '736', '858', '766'];
      if (reqType == 'C' && custGrp == 'CROSS' && apaCntry.includes(cntry)) {
        FormManager.setValue('custSubGrp', 'CROSS');
        window.loadDefaultCrossSettings = true;
      }
    });
  }
}

// CREATCMR-788
function addressQuotationValidatorAP() {

  var cntry = FormManager.getActualValue('cmrIssuingCntry')
  var AP01 = [SysLoc.BRUNEI, SysLoc.SRI_LANKA, SysLoc.INDIA, SysLoc.INDONESIA, SysLoc.MALASIA, SysLoc.PHILIPPINES, SysLoc.BANGLADESH, SysLoc.SINGAPORE, SysLoc.VIETNAM, SysLoc.THAILAND];
  if (AP01.indexOf(cntry) > -1) {
    FormManager.addValidator('custNm1', Validators.NO_QUOTATION, ['Customer Name']);
    FormManager.addValidator('custNm2', Validators.NO_QUOTATION, ['Customer Name Con\'t']);
    FormManager.addValidator('addrTxt', Validators.NO_QUOTATION, ['Address.(Flr,Bldg,lvl,Unit.)']);
    FormManager.addValidator('addrTxt2', Validators.NO_QUOTATION, ['Address.Cont1(Street Name, Street No.)']);
    FormManager.addValidator('dept', Validators.NO_QUOTATION, ['Address.Cont2(District,Town,Region)']);
    FormManager.addValidator('postCd', Validators.NO_QUOTATION, ['Postal Code']);
    if (cntry == SysLoc.INDONESIA || cntry == SysLoc.MALASIA || cntry == SysLoc.PHILIPPINES) {
      FormManager.addValidator('city1', Validators.NO_QUOTATION, ['City/State/Province']);
    } else if (cntry == SysLoc.THAILAND) {
      FormManager.addValidator('city1', Validators.NO_QUOTATION, ['Street Address Cont\'2']);
    } else {
      FormManager.addValidator('city1', Validators.NO_QUOTATION, ['City']);
    }
  } else if (cntry == SysLoc.AUSTRALIA || cntry == SysLoc.NEW_ZEALAND) {
    FormManager.addValidator('custNm1', Validators.NO_QUOTATION, ['Customer Name']);
    FormManager.addValidator('custNm2', Validators.NO_QUOTATION, ['Customer Name Con\'t']);
    FormManager.addValidator('dept', Validators.NO_QUOTATION, ['Attn']);
    FormManager.addValidator('addrTxt', Validators.NO_QUOTATION, ['Street Address']);
    FormManager.addValidator('addrTxt2', Validators.NO_QUOTATION, ['Street Address Con\'t']);
    FormManager.addValidator('city1', Validators.NO_QUOTATION, ['Suburb']);
    FormManager.addValidator('postCd', Validators.NO_QUOTATION, ['Postal Code']);
  }
  FormManager.addValidator('abbrevNm', Validators.NO_QUOTATION, ['Abbreviated Name (TELX1)'], 'MAIN_CUST_TAB');
  FormManager.addValidator('abbrevLocn', Validators.NO_QUOTATION, ['Abbreviated Location'], 'MAIN_CUST_TAB');
}
function setLockIsicNZfromDNB() {
  console.log('>>>> setLockIsicNZfromDNB >>>>');
  var isDnbRecord = FormManager.getActualValue('findDnbResult');
  if (isDnbRecord == 'Accepted' && FormManager.getActualValue('isicCd') != '') {
    FormManager.readOnly('isicCd');
  } else {
    FormManager.enable('isicCd');
  }

}

function additionalAddrNmValidatorOldNZ() {
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
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
            REQ_ID: reqId,
          };
          var record = cmr.query('GETZS01VALRECORDS', qParams_z);
          var zs01Reccount = record.ret1;
          if (zs01Reccount == 0) {
            return new ValidationResult(null, true);
          }
          var qParams = {
            REQ_ID: reqId,
            ADDR_TYPE: "ZS01",
          };
          var result1 = cmr.query('ADDR.GET.CUSTNM1.BY_REQID_ADDRTYP', qParams);
          var zs01Name = result1 != undefined ? result1.ret1.concat(result1.ret2) : '';
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
            if ((reqType == 'U' || reqType == 'C') && type != 'ZS01' && zs01Name != name) {
              count++;
            }
          }
          if (count > 0) {
            return new ValidationResult(null, false, 'All Updated / New address customer name should be same as Installing address.');
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

function additionalAddrNmValidatorNZ() {
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
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
            REQ_ID: reqId,
          };
          var record = cmr.query('GETZS01VALRECORDS', qParams_z);
          var zs01Reccount = record.ret1;
          if (zs01Reccount == 0) {
            return new ValidationResult(null, true);
          }
          var qParams = {
            REQ_ID: reqId,
            ADDR_TYPE: "ZS01",
          };
          var result1 = cmr.query('ADDR.GET.CUSTNM1.BY_REQID_ADDRTYP', qParams);
          var zs01Name = result1 != undefined ? result1.ret1.concat(result1.ret2) : '';
          name = name1 + name2;
          if (zs01Name != name) {
            return new ValidationResult(null, false, 'All Updated / New address customer name should be same as Installing address.');
          } else {
            return new ValidationResult(null, true);
          }
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR_addressModal');
}

// CREATCMR-7658
function addValidatorforInstallingNZ() {
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
        var addrType = FormManager.getActualValue('addrType');
        var name1 = FormManager.getActualValue('custNm1').toUpperCase();
        var name2 = FormManager.getActualValue('custNm2').toUpperCase();
        var attn = FormManager.getActualValue('dept').toUpperCase();
        var street = FormManager.getActualValue('addrTxt').toUpperCase();
        var street2 = FormManager.getActualValue('addrTxt2').toUpperCase();
        var suburb = FormManager.getActualValue('city1').toUpperCase();
        var address = name1 + name2 + attn + street + street2 + suburb;
        if ((address.includes("PO BOX") || address.includes("POBOX")) && addrType == 'ZS01') {
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

  FormManager.addValidator('abbrevNm', Validators.NO_QUOTATION, ['Abbreviated Name (TELX1)'], 'MAIN_CUST_TAB');
  FormManager.addValidator('abbrevLocn', Validators.NO_QUOTATION, ['Abbreviated Location'], 'MAIN_CUST_TAB');
  switch (cntry) {
    case SysLoc.MACAO: case SysLoc.HONG_KONG:
      FormManager.addValidator('custNm1', Validators.NO_QUOTATION, ['Customer Name']);
      FormManager.addValidator('custNm2', Validators.NO_QUOTATION, ['Customer Name Con\'t']);
      FormManager.addValidator('addrTxt', Validators.NO_QUOTATION, ['Address.(Flr,Bldg,lvl,Unit.)']);
      FormManager.addValidator('addrTxt2', Validators.NO_QUOTATION, ['Address. Cont1(Street Name, Street No.)']);
      FormManager.addValidator('city1', Validators.NO_QUOTATION, ['Address. Cont2(District,Town,Region)']);
      FormManager.addValidator('postCd', Validators.NO_QUOTATION, ['Postal Code']);
      break;
  }
}

var _customerTypeHandler = null;
function addCustGrpHandler() {
  console.log('>>>> addCustGrpHandler >>>>');
  if (_customerTypeHandler == null) {
    _customerTypeHandler = dojo.connect(FormManager.getField('custGrp'), 'onChange', function (value) {
      var cntry = FormManager.getActualValue('cmrIssuingCntry');
      var custGrp = FormManager.getActualValue('custGrp');
      var reqType = FormManager.getActualValue('reqType');
      var apaCntry = ['834', '818', '856', '778', '749', '643', '852', '744', '615', '652', '616', '796', '641', '738', '736', '858', '766'];
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
    var custGrp = FormManager.getActualValue('custGrp');
    var custSubGrp = FormManager.getActualValue('custSubGrp');
    FormManager.enable('vat');
    if (!(custGrp == 'CROSS' || custSubGrp == 'DUMMY' || custSubGrp == 'INTER' || custSubGrp == 'PRIV')) {
      FormManager.addValidator('vat', Validators.REQUIRED, ['New Zealand Business#'], 'MAIN_CUST_TAB');
    } else {
      FormManager.removeValidator('vat', Validators.REQUIRED);
    }
  }
}

function afterConfigForNewZeaLand() {
  console.log("----After config. for New Zealand----");
  if (_vatExemptHandlerNZ == null) {
    _vatExemptHandlerNZ = dojo.connect(FormManager.getField('vatExempt'), 'onClick', function (value) {
      console.log(">>> RUNNING!!!!");
      var custGrp = FormManager.getActualValue('custGrp');
      var custSubGrp = FormManager.getActualValue('custSubGrp');
      FormManager.resetValidations('vat');
      if (dijit.byId('vatExempt').get('checked')) {
        console.log(">>> Process nzbnExempt remove * >> ");
        FormManager.readOnly('vat');
        FormManager.setValue('vat', '');
      } else {
        console.log(">>> Process nzbnExempt add * >> ");
        FormManager.enable('vat');
        if (!(custGrp == 'CROSS' || custSubGrp == 'DUMMY' || custSubGrp == 'INTER' || custSubGrp == 'PRIV')) {
          FormManager.addValidator('vat', Validators.REQUIRED, ['New Zealand Business#'], 'MAIN_CUST_TAB');
        } else {
          FormManager.removeValidator('vat', Validators.REQUIRED);
        }
      }
    });
  }
}

function validatNZBNForNewZeaLand() {
  console.log('starting validatNZBNForNewZeaLand ...');
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
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
            REQ_ID: reqId,
            ADDR_TYPE: 'ZS01'
          });
          if (contractCustNm != undefined) {
            custNm = contractCustNm.ret1.toUpperCase() + " " + contractCustNm.ret2.toUpperCase();
          }
          custNm = custNm.trim();
          var nzbnRet = cmr.validateNZBNFromAPI(vat, reqId, custNm);
          console.log('>>>nzbnRet>>>');
          console.log(nzbnRet);
          if (!nzbnRet.success || !nzbnRet.custNmMatch) {
            return new ValidationResult({
              id: 'vat',
              type: 'text',
              name: 'vat'
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
function setDefaultValueForNZCreate() {
  console.log(">>>>setDefaultValueForNZCreate()>>>>");
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var reqId = FormManager.getActualValue('reqId');
  var isicCdInDB = '';
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var custSubGroups = ['BLUMX', 'MKTPC', 'AQSTN', 'NRML', 'NRMLC', 'KYND', 'ESOSW', 'ECSYS', 'CROSS', 'XAQST', 'XBLUM', 'XESO', 'XMKTP'];
  console.log(">>>>setDefaultValueForNZCreate()>>>> SubGrp=" + custSubGrp);
  if (custSubGroups.includes(custSubGrp)) {
    var reqIdParams = {
      REQ_ID: reqId,
    };
    var isicCdResult = cmr.query('GET.ISIC_CD_BY_REQID', reqIdParams);

    if (isicCdResult.ret1 != undefined) {
      isicCdInDB = isicCdResult.ret1;
    }
    console.log(">>>>setDefaultValueForNZCreate()>>>> ISIC_CD in DB =" + isicCdInDB);
  }

  switch (custSubGrp) {
    case 'PRIV':
      // ISIC = 9500, - lock field
      FormManager.setValue('isicCd', '9500');
      FormManager.readOnly('isicCd');
      // cluster/QTC/ISU: default as 00002/Z/34 - lock field
      // CREATCMR-7884
      FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), ['00002']);
      FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Z']);
      FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['21']);
      FormManager.setValue('apCustClusterId', '00002');
      FormManager.readOnly('apCustClusterId');
      FormManager.setValue('clientTier', 'Z');
      FormManager.readOnly('clientTier');
      FormManager.setValue('isuCd', '21');
      FormManager.readOnly('isuCd');
      // MRC = 3 - lock field
      FormManager.setValue('mrcCd', '3');
      FormManager.readOnly('mrcCd');
      // Collection code = 00JC - lock field
      FormManager.setValue('collectionCd', '00JC');
      FormManager.readOnly('collectionCd');
      break;
    case 'BLUMX':
      // cluster/QTC/ISU: default as 00002/Z/34 - lock field
      // CREATCMR-7884
      FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), ['00002']);
      FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Z']);
      FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['21']);
      FormManager.setValue('apCustClusterId', '00002');
      FormManager.readOnly('apCustClusterId');
      FormManager.setValue('clientTier', 'Z');
      FormManager.readOnly('clientTier');
      FormManager.setValue('isuCd', '21');
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
      // cluster/QTC/ISU: default as 00002/Z/34 - lock field
      // CREATCMR-7884
      FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), ['00002']);
      FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Z']);
      FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['21']);
      FormManager.setValue('apCustClusterId', '00002');
      FormManager.readOnly('apCustClusterId');
      FormManager.setValue('clientTier', 'Z');
      FormManager.readOnly('clientTier');
      FormManager.setValue('isuCd', '21');
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
      FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), ['00002']);
      FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Z']);
      FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['21']);
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
      // cluster/QTC/ISU: default as 00002/Z/21 - lock field
      // CREATCMR-7884
      FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), ['00002']);
      FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Z']);
      FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['21']);
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

      FormManager.setValue('abbrevNm', 'IGF DUMMY use only');
      FormManager.readOnly('abbrevNm');
      break;
    case 'AQSTN':
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
      FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['T']);
      FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['32']);

      // FormManager.setValue('apCustClusterId', '10114');
      FormManager.setValue('clientTier', 'T');
      FormManager.setValue('isuCd', '32');

      FormManager.setValue('isicCd', isicCdInDB);
      break;
    case 'NRMLC':
      // CREATCMR-7884
      // MRC = 3 - lock field
      FormManager.setValue('mrcCd', '3');
      FormManager.readOnly('mrcCd');
      // Collection code = 00JC - lock field
      FormManager.setValue('collectionCd', '00JC');
      FormManager.readOnly('collectionCd');

      FormManager.setValue('isicCd', isicCdInDB);
      break;
    case 'KYND':
      // CREATCMR-7884
      // MRC = 3 - lock field
      FormManager.setValue('mrcCd', '3');
      FormManager.readOnly('mrcCd');
      // Collection code = 00JC - lock field
      FormManager.setValue('collectionCd', '00JC');
      FormManager.readOnly('collectionCd');

      FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), ['09199']);
      FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['0']);
      FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['5K']);

      FormManager.setValue('apCustClusterId', '09199');
      FormManager.setValue('clientTier', '0');
      FormManager.setValue('isuCd', '5K');

      FormManager.readOnly('apCustClusterId');
      FormManager.readOnly('clientTier');
      FormManager.readOnly('isuCd');

      FormManager.setValue('inacType', 'I');
      FormManager.setValue('inacCd', '6272');
      FormManager.setValue('isicCd', isicCdInDB);
      break;
    case 'ESOSW':
      // MRC = 3 - lock field
      FormManager.setValue('mrcCd', '3');
      FormManager.readOnly('mrcCd');
      // Collection code = 00JC - lock field
      FormManager.setValue('collectionCd', '00JC');
      FormManager.readOnly('collectionCd');
      FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Q', '0', 'Y', 'T']);
      FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['34', '5K', '36', '32']);
      // FormManager.setValue('apCustClusterId', '01147');
      FormManager.setValue('clientTier', 'Q');
      FormManager.setValue('isuCd', '34');

      FormManager.setValue('abbrevNm', 'ESA use only');
      FormManager.readOnly('abbrevNm');

      FormManager.setValue('isicCd', isicCdInDB);
      break;
    case 'ECSYS':
      FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Y']);
      FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['36']);
      FormManager.setValue('clientTier', 'Y');
      FormManager.readOnly('clientTier');
      FormManager.setValue('isuCd', '36');
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
      FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Z', 'Q', '0', 'Y', 'T']);
      FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['34', '5K', '36', '32']);
      if (_pagemodel.apCustClusterId == null) {
        FormManager.setValue('apCustClusterId', '00002');
      }
      FormManager.setValue('clientTier', 'Z');
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
      FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), ['00002']);
      FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Z']);
      FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['34']);
      FormManager.setValue('apCustClusterId', '00002');
      FormManager.readOnly('apCustClusterId');
      FormManager.setValue('clientTier', 'Z');
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
      FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Q', '0', 'Y', 'T']);
      FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['34', '5K', '36', '32']);
      // FormManager.setValue('apCustClusterId', '01147');
      FormManager.setValue('clientTier', 'Q');
      FormManager.setValue('isuCd', '34');

      FormManager.setValue('abbrevNm', 'ESA use only');
      FormManager.readOnly('abbrevNm');

      FormManager.setValue('isicCd', isicCdInDB);
      break;
    case 'XMKTP':
      // cluster/QTC/ISU: default as 00002/Z/34 - lock field
      // CREATCMR-7884
      FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), ['00002']);
      FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Z']);
      FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['34']);
      FormManager.setValue('apCustClusterId', '00002');
      FormManager.readOnly('apCustClusterId');
      FormManager.setValue('clientTier', 'Z');
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
function checkNZCustomerNameTextWhenSFP() {
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
        var errorMsg = '';
        var action = FormManager.getActualValue('yourAction');
        var custNm1 = FormManager.getActualValue('mainCustNm1').toUpperCase() + ' ' + FormManager.getActualValue('mainCustNm2').toUpperCase();
        var reqType = FormManager.getActualValue('reqType');
        var role = FormManager.getActualValue('userRole').toUpperCase();
        var custGrp = FormManager.getActualValue('custGrp');
        var custSubGrp = FormManager.getActualValue('custSubGrp');

        if (reqType == 'C' && role == 'REQUESTER' && custGrp == 'LOCAL' && custSubGrp == 'PRIV' && action == 'SFP') {
          if (custNm1.indexOf('PRIVATE LIMITED') > -1) {
            errorMsg = 'Customer Name can not contain \'Private Limited\'';
          } else if (custNm1.indexOf('COMPANY') > -1) {
            errorMsg = 'Customer Name can not contain \'Company\'';
          } else if (custNm1.indexOf('CORPORATION') > -1) {
            errorMsg = 'Customer Name can not contain \'Corporation\'';
          } else if (custNm1.indexOf('INCORPORATE') > -1) {
            errorMsg = 'Customer Name can not contain \'incorporate\'';
          } else if (custNm1.indexOf('ORGANIZATION') > -1) {
            errorMsg = 'Customer Name can not contain \'organization\'';
          } else if (custNm1.indexOf('PVT LTD') > -1) {
            errorMsg = 'Customer Name can not contain \'Pvt Ltd\'';
          } else if (custNm1.indexOf('PRIVATE') > -1) {
            errorMsg = 'Customer Name can not contain \'Private\'';
          } else if (custNm1.indexOf('LIMITED') > -1) {
            errorMsg = 'Customer Name can not contain \'Limited\'';
          } else if (custNm1.indexOf('LTD') > -1) {
            errorMsg = 'Customer Name can not contain \'Ltd\'';
          }
        } else if (reqType == 'C' && role == 'REQUESTER' && custSubGrp == 'KYND' && (action == 'SFP' || action == 'VAL')) {
          // CREATCMR-7884
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
// CREATCMR-7653
// Perform UI validation when'Send for processing': Landed country can't be New
// Zealand
function checkNZLandedCountryForCrossWhenSFP() {
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
        var errorMsg = '';
        var action = FormManager.getActualValue('yourAction');
        var landCntry = '';
        var custLandCntry = FormManager.getActualValue('landCntry');
        if (custLandCntry != '') {
          landCntry = custLandCntry;
        } else {
          var _zs01ReqId = FormManager.getActualValue('reqId');
          var cntryCdParams = {
            REQ_ID: _zs01ReqId,
            ADDR_TYPE: 'ZS01',
          };
          var cntryCdResult = cmr.query('ADDR.GET.LANDEDCNTRY.BY_REQID_ADDRTYPE', cntryCdParams);

          if (cntryCdResult.ret1 != undefined) {
            landCntry = cntryCdResult.ret1;
          }
        }
        var reqType = FormManager.getActualValue('reqType');
        var role = FormManager.getActualValue('userRole').toUpperCase();
        var custGrp = FormManager.getActualValue('custGrp');

        if (reqType == 'C' && role == 'REQUESTER' && custGrp == 'CROSS' && action == 'SFP' && landCntry == "NZ") {
          errorMsg = 'Landed country can\'t be New Zealand when \'Cross-Border Customer\' selected';
          return new ValidationResult({
            id: 'landCntry',
            type: 'text',
            name: 'landCntry'
          }, false, errorMsg);
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}
// CREATCMR-7653
function checkNZCustomerNameStartsForLocalInterDummy() {
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
        var errorMsg = '';
        var custNm1 = FormManager.getActualValue('mainCustNm1').toUpperCase();

        var reqType = FormManager.getActualValue('reqType');
        var role = FormManager.getActualValue('userRole').toUpperCase();
        var custGrp = FormManager.getActualValue('custGrp');
        var custSubGrp = FormManager.getActualValue('custSubGrp');

        if (reqType == 'C' && role == 'REQUESTER' && custGrp == 'LOCAL') {
          if (custSubGrp == 'INTER' && custNm1.indexOf('IBM ') != 0) {
            errorMsg = 'Customer name must start with \'IBM \'';
          } else if (custSubGrp == 'DUMMY' && custNm1.indexOf('IGF DUMMY ') != 0) {
            errorMsg = 'Customer name must start with \'IGF DUMMY \'';
          }
          if (errorMsg != '') {
            return new ValidationResult({
              id: 'custNm1',
              type: 'text',
              name: 'custNm1'
            }, false, errorMsg);
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

// CREATCMR-7884
function addCovBGValidator() {
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
        var reqType = FormManager.getActualValue('reqType');
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        if (reqType == 'C' && (custSubGrp == 'NRMLC' || custSubGrp == 'AQSTN')) {
          var result = FormManager.getActualValue('covBgRetrievedInd');
          if (result == '' || result.toUpperCase() != 'Y') {
            return new ValidationResult(null, false, 'Coverage/Buying Group/GLC/DUNS values have not been retrieved yet.');
          } else {
            return new ValidationResult(null, true);
          }
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

// CREATCMR-7883
function checkCustomerNameForKYND() {
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
        var errorMsg = '';
        var action = FormManager.getActualValue('yourAction');
        var custNm1 = FormManager.getActualValue('mainCustNm1').toUpperCase();

        var reqType = FormManager.getActualValue('reqType');
        var role = FormManager.getActualValue('userRole').toUpperCase();
        var custSubGrp = FormManager.getActualValue('custSubGrp');

        if (reqType == 'C' && role == 'REQUESTER' && custSubGrp == 'KYNDR' && (action == 'SFP' || action == 'VAL')) {
          // CREATCMR-7884
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

// CREATCMR-7883
function lockCMRNumberPrefixforNoINTER() {
  console.log('>>>> lockCMRNumberPrefixforNoINTER >>>>');
  var issuingCnt = ['749', '778', '616', '834'];

  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var custSubGrp = FormManager.getActualValue('custSubGrp');

  if (issuingCnt.includes(cntry)) {
    if (custSubGrp != 'INTER') {
      FormManager.readOnly('cmrNoPrefix');
    } else {
      FormManager.enable('cmrNoPrefix');
    }
  }
}

// CREATCMR-7887
function setCTCIsuByClusterIndonesia() {
  console.log('>>>> setCTCIsuByClusterIndonesia >>>>');
  var scenario = FormManager.getActualValue('custGrp');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var _apCustClusterId = FormManager.getActualValue('apCustClusterId');

  if (_pagemodel.inacCd == null) {
    FormManager.setValue('inacCd', '');
    FormManager.setValue('inacType', '');
  }
  
  if (custSubGrp != 'KYND') {
    FormManager.enable('inacCd');
    FormManager.enable('inacType');
  }

  if (custSubGrp == 'AQSTN' || custSubGrp == 'XAQST' || custSubGrp == 'NRML') {
    FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Q', 'E']);
    FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), ['12328', '12330', '12332', '12327', '12329', '12331', '12334', '12338', '12342', '12346', '12336', '12340', '12344', '12348', '12333', '12337', '12345', '12335', '12339', '12343', '12347', '12452', '12451', '12450']);
    FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['34', '27']);
    FormManager.setValue('apCustClusterId', '12328');
    if (_apCustClusterId == '12328' || _apCustClusterId == '12330' || _apCustClusterId == '12332' || _apCustClusterId == '12327' ||  _apCustClusterId == '12329' || _apCustClusterId == '12331' ) {
      FormManager.setValue('clientTier', 'E');
      FormManager.setValue('isuCd', '27');
    } else {
      FormManager.setValue('clientTier', 'Q');
      FormManager.setValue('isuCd', '34'); 
    }
    FormManager.setValue('mrcCd', '3');
    // FormManager.readOnly('apCustClusterId');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('mrcCd');
  } else if (custSubGrp == 'BUSPR' || custSubGrp == 'BLUMX' || custSubGrp == 'MKPL' || custSubGrp == 'PRICU') {
    FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Z']);
    FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), ['00000']);
    FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['34']);
    FormManager.setValue('apCustClusterId', '00000');
    FormManager.setValue('clientTier', 'Z');
    FormManager.setValue('isuCd', '34');
    FormManager.setValue('mrcCd', '3');
    FormManager.readOnly('apCustClusterId');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('mrcCd');
  } else if (custSubGrp == 'BUSPR') {
    FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Z']);
    FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), ['00000', '00089']);
    FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['34']);
    FormManager.setValue('apCustClusterId', '00000');
    FormManager.setValue('mrcCd', '3');
    if (_apCustClusterId == '00089') {
      FormManager.setValue('clientTier', 'Z');
      FormManager.setValue('isuCd', '8B');
    } else {
      FormManager.setValue('clientTier', 'Z');
      FormManager.setValue('isuCd', '34');
    }
    // FormManager.readOnly('apCustClusterId');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('mrcCd');
  }  else if (custSubGrp == 'ASLOM' || custSubGrp == 'XASLM') {
    FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Q', '0', 'Y', 'E']);
    FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), ['09193', '08040', '11979', '11978', '11977', '11976', '12328', '12330', '12332', '12327', '12329', '12331', '12334', '12338', '12342', '12346', '12336', '12340', '12344', '12348', '12333', '12337', '12345', '12335', '12339', '12343', '12347', '12452', '12451', '12450' ]);
    FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['34', '5K', '36', '27']);
    FormManager.setValue('inacCd', '');
    FormManager.setValue('inacType', '');
    if (_clusterHandlerINDONESIA == 0 && _pagemodel.apCustClusterId == null) {
      FormManager.setValue('clientTier', '');
      FormManager.setValue('apCustClusterId', '');
      FormManager.setValue('isuCd', '');
      _apCustClusterId = '';
    }
    if ( _apCustClusterId == '12334' || _apCustClusterId == '12338' || _apCustClusterId == '12342' || _apCustClusterId == '12346'   || _apCustClusterId == '12336' || _apCustClusterId == '12340' || _apCustClusterId == '12344' || _apCustClusterId == '12348'   || _apCustClusterId == '12333' || _apCustClusterId == '12337' || _apCustClusterId == '12345'  || _apCustClusterId == '12335' || _apCustClusterId == '12339' || _apCustClusterId == '12343'  || _apCustClusterId == '12347'|| _apCustClusterId == '12452'|| _apCustClusterId == '12451'|| _apCustClusterId == '12450') {
      FormManager.setValue('clientTier', 'Q');
      FormManager.setValue('isuCd', '34');
    } else if (_apCustClusterId == '09193') {
      FormManager.limitDropdownValues(FormManager.getField('inacType'), ['I']);
      FormManager.setValue('clientTier', '0');
      FormManager.setValue('isuCd', '5K');
      FormManager.setValue('inacCd', '6272');
      FormManager.setValue('inacType', 'I');
      FormManager.readOnly('inacCd');
      FormManager.readOnly('inacType');
    } else if (_apCustClusterId == '08040' || _apCustClusterId == '11979' || _apCustClusterId == '11978' || _apCustClusterId =='11977' || _apCustClusterId =='11976') {
      FormManager.setValue('clientTier', 'Y');
      FormManager.setValue('isuCd', '36');
    } else if (_apCustClusterId == '12328' || _apCustClusterId == '12330' || _apCustClusterId == '12332' || _apCustClusterId == '12327' ||  _apCustClusterId == '12329' || _apCustClusterId == '12331' ) {
      FormManager.setValue('clientTier', 'E');
      FormManager.setValue('isuCd', '27');
    } 
    FormManager.setValue('mrcCd', '3');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('mrcCd');
  } else if (custSubGrp == 'CROSS') {
    FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Q', '0', 'Y', 'Z', 'E']);
    FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), [ '09193', '00000', '08040', '11979', '11978', '11977', '11976',  '12328', '12330', '12332', '12327', '12329', '12331', '12334', '12338', '12342', '12346', '12336', '12340', '12344', '12348', '12333', '12337', '12345', '12335', '12339', '12343', '12347', '12452', '12451', '12450' ]);
    FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['34', '5K', '36', '27']);
    FormManager.setValue('inacCd', '');
    FormManager.setValue('inacType', '');
    if (_clusterHandlerINDONESIA == 0 && _pagemodel.apCustClusterId == null) {
      FormManager.setValue('clientTier', '');
      FormManager.setValue('apCustClusterId', '');
      FormManager.setValue('isuCd', '');
      _apCustClusterId = '';
    }
    if (_apCustClusterId == '12334' || _apCustClusterId == '12338' || _apCustClusterId == '12342' || _apCustClusterId == '12346'   || _apCustClusterId == '12336' || _apCustClusterId == '12340' || _apCustClusterId == '12344' || _apCustClusterId == '12348'   || _apCustClusterId == '12333' || _apCustClusterId == '12337' || _apCustClusterId == '12345'  || _apCustClusterId == '12335' || _apCustClusterId == '12339' || _apCustClusterId == '12343'  || _apCustClusterId == '12347'|| _apCustClusterId == '12452'|| _apCustClusterId == '12451'|| _apCustClusterId == '12450') {
      FormManager.setValue('clientTier', 'Q');
      FormManager.setValue('isuCd', '34');
    } else if (_apCustClusterId == '09193') {
      FormManager.limitDropdownValues(FormManager.getField('inacType'), ['I']);
      FormManager.setValue('clientTier', '0');
      FormManager.setValue('isuCd', '5K');
      FormManager.setValue('inacCd', '6272');
      FormManager.setValue('inacType', 'I');
      FormManager.readOnly('inacCd');
      FormManager.readOnly('inacType');
    } else if (_apCustClusterId == '08040' || _apCustClusterId == '11979' || _apCustClusterId == '11978' || _apCustClusterId == '11977' || _apCustClusterId == '11976') {
      FormManager.setValue('clientTier', 'Y');
      FormManager.setValue('isuCd', '36');
    } else if (_apCustClusterId == '00000') {
      FormManager.setValue('clientTier', 'Z');
      FormManager.setValue('isuCd', '34');
    } else if (_apCustClusterId == '12328' || _apCustClusterId == '12330' || _apCustClusterId == '12332' || _apCustClusterId == '12327' ||  _apCustClusterId == '12329' || _apCustClusterId == '12331' ) {
      FormManager.setValue('clientTier', 'E');
      FormManager.setValue('isuCd', '27');
    }
    FormManager.setValue('mrcCd', '3');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('mrcCd');
  } else if (custSubGrp == 'ECSYS') {
    FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Y']);
    FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), ['08040','11979', '11978', '11977', '11976']);
    FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['36']);
    FormManager.setValue('apCustClusterId', '08040');
    FormManager.setValue('clientTier', 'Y');
    FormManager.setValue('isuCd', '36');
    FormManager.setValue('mrcCd', '3');
   // FormManager.readOnly('apCustClusterId');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('mrcCd');
  } else if (custSubGrp == 'DUMMY') {
    FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Z']);
    FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), ['00000']);
    FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['21']);
    FormManager.setValue('apCustClusterId', '00000');
    FormManager.setValue('clientTier', 'Z');
    FormManager.setValue('isuCd', '21');
    FormManager.setValue('mrcCd', '2');
    FormManager.readOnly('apCustClusterId');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('mrcCd');

  } else if (custSubGrp == 'INTER') {
    FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['0']);
    FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), ['00000']);
    FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['60']);
    FormManager.setValue('apCustClusterId', '00000');
    FormManager.setValue('clientTier', '0');
    FormManager.setValue('isuCd', '60');
    FormManager.setValue('mrcCd', '2');
    FormManager.readOnly('apCustClusterId');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('mrcCd');
  } else if (custSubGrp == 'KYND') {
    FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['0']);
    FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), ['09193']);
    FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['5K']);
    FormManager.limitDropdownValues(FormManager.getField('inacType'), ['I']);
    FormManager.setValue('apCustClusterId', '09193');
    FormManager.setValue('clientTier', '0');
    FormManager.setValue('isuCd', '5K');
    FormManager.setValue('mrcCd', '3');
    FormManager.setValue('inacCd', '6272');
    FormManager.setValue('inacType', 'I');
    FormManager.readOnly('apCustClusterId');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('mrcCd');
    FormManager.readOnly('inacCd');
    FormManager.readOnly('inacType');
  }
}

// CREATCMR-7878
function setCTCIsuByClusterBDLK() {
  console.log('>>>> setCTCIsuByClusterBDLK >>>>');
  var scenario = FormManager.getActualValue('custGrp');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var _apCustClusterId = FormManager.getActualValue('apCustClusterId');
  var _cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');

  if (_pagemodel.inacCd == null) {
    FormManager.setValue('inacCd', '');
    FormManager.setValue('inacType', '');
  }
  FormManager.enable('inacCd');
  FormManager.enable('inacType');

  if (custSubGrp == 'AQSTN') {
    FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Q']);
    if (_cmrIssuingCntry == '615') {
      FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), ['10658']);
      FormManager.setValue('apCustClusterId', '10658');
    } else {
      FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), ['10659']);
      FormManager.setValue('apCustClusterId', '10659');
    }
    FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['34']);
    FormManager.setValue('clientTier', 'Q');
    FormManager.setValue('isuCd', '34');
    FormManager.setValue('mrcCd', '3');
    FormManager.readOnly('apCustClusterId');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('mrcCd');
  } else if (custSubGrp == 'DUMMY') {
    FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Z']);
    FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), ['012D999']);
    FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['21']);
    FormManager.setValue('apCustClusterId', '012D999');
    FormManager.setValue('clientTier', 'Z');
    FormManager.setValue('isuCd', '21');
    FormManager.setValue('mrcCd', '2');
    FormManager.readOnly('apCustClusterId');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('mrcCd');
  } else if (custSubGrp == 'BLUMX' || custSubGrp == 'MKTPC') {
    FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Z']);
    FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), ['012D999']);
    FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['34']);
    FormManager.setValue('apCustClusterId', '012D999');
    FormManager.setValue('clientTier', 'Z');
    FormManager.setValue('isuCd', '34');
    FormManager.setValue('mrcCd', '3');
    FormManager.readOnly('apCustClusterId');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('mrcCd');
  } else if (custSubGrp == 'CROSS') {
    FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Q', '0', 'Y', 'Z']);
    if (_cmrIssuingCntry == '615') {
      FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), ['012D999', '08034', '09064', '10658']);
    } else {
      FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), ['012D999', '08035', '09063', '10659']);
    }
    FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['34', '5K', '36']);
    FormManager.setValue('inacCd', '');
    FormManager.setValue('inacType', '');
    if (_clusterHandlerINDONESIA == 0 && _pagemodel.apCustClusterId == null) {
      FormManager.setValue('clientTier', '');
      FormManager.setValue('apCustClusterId', '012D999');
      FormManager.setValue('isuCd', '');
      _apCustClusterId = '';
    }
    if (_apCustClusterId == '012D999') {
      FormManager.setValue('clientTier', 'Z');
      FormManager.setValue('isuCd', '34');
    } else if (_apCustClusterId == '09064' || _apCustClusterId == '09063') {
      FormManager.limitDropdownValues(FormManager.getField('inacType'), ['I']);
      FormManager.setValue('clientTier', '0');
      FormManager.setValue('isuCd', '5K');
      FormManager.setValue('inacCd', '6272');
      FormManager.setValue('inacType', 'I');
      FormManager.readOnly('inacCd');
      FormManager.readOnly('inacType');
    } else if (_apCustClusterId == '08034' || _apCustClusterId == '08035') {
      FormManager.setValue('clientTier', 'Y');
      FormManager.setValue('isuCd', '36');
    } else if (_apCustClusterId == '10658' || _apCustClusterId == '10659') {
      FormManager.setValue('clientTier', 'Q');
      FormManager.setValue('isuCd', '34');
    }
    FormManager.setValue('mrcCd', '3');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('mrcCd');
  } else if (custSubGrp == 'ECSYS') {
    FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Y']);
    if (_cmrIssuingCntry == '615') {
      FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), ['08034']);
      FormManager.setValue('apCustClusterId', '08034');
    } else {
      FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), ['08035']);
      FormManager.setValue('apCustClusterId', '08035');
    }
    FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['36']);
    FormManager.setValue('clientTier', 'Y');
    FormManager.setValue('isuCd', '36');
    FormManager.setValue('mrcCd', '3');
    FormManager.readOnly('apCustClusterId');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('mrcCd');
  } else if (custSubGrp == 'NRML') {
    FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Q']);
    if (_cmrIssuingCntry == '615') {
      FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), ['10658']);
      FormManager.setValue('apCustClusterId', '10658');
    } else {
      FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), ['10659']);
      FormManager.setValue('apCustClusterId', '10659');
    }
    FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['34']);
    FormManager.setValue('clientTier', 'Q');
    FormManager.setValue('isuCd', '34');
    FormManager.setValue('mrcCd', '3');
    FormManager.readOnly('apCustClusterId');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('mrcCd');
  } else if (custSubGrp == 'INTER') {
    FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['0']);
    FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), ['012D999']);
    FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['60']);
    FormManager.setValue('apCustClusterId', '012D999');
    FormManager.setValue('clientTier', '0');
    FormManager.setValue('isuCd', '60');
    FormManager.setValue('mrcCd', '2');
    FormManager.readOnly('apCustClusterId');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('mrcCd');
  } else if (custSubGrp == 'KYND') {
    FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['0']);
    if (_cmrIssuingCntry == '615') {
      FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), ['09064']);
      FormManager.setValue('apCustClusterId', '09064');
    } else {
      FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), ['09063']);
      FormManager.setValue('apCustClusterId', '09063');
    }
    FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['5K']);
    FormManager.limitDropdownValues(FormManager.getField('inacType'), ['I']);
    FormManager.setValue('clientTier', '0');
    FormManager.setValue('isuCd', '5K');
    FormManager.setValue('mrcCd', '3');
    FormManager.setValue('inacCd', '6272');
    FormManager.setValue('inacType', 'I');
    FormManager.readOnly('apCustClusterId');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('mrcCd');
    FormManager.readOnly('inacCd');
    FormManager.readOnly('inacType');
  }
}

// CREATCMR-7887
function lockInacForIDMYSG() {
  console.log(">>>>lockInacForIDMYSG()>>>>");
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (custSubGrp == 'DUMMY' || custSubGrp == 'PRIV' || custSubGrp == 'PRICU' || custSubGrp == 'INTER' || custSubGrp == 'XPRIV' || custSubGrp == 'BUSPR') {
    FormManager.limitDropdownValues(FormManager.getField('inacType'), ['']);
    FormManager.limitDropdownValues(FormManager.getField('inacCd'), ['']);
    FormManager.setValue('inacCd', '');
    FormManager.setValue('inacType', '');
    FormManager.readOnly('inacCd');
    FormManager.readOnly('inacType');
  }

  // CREATCMR-7885
  setInacCdTypeStatus();
}
// CREATCMR-7886
function setCTCIsuByClusterMY() {
  console.log('>>>> setCTCIsuByClusterMY >>>>');

  var scenario = FormManager.getActualValue('custGrp');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var _apCustClusterId = FormManager.getActualValue('apCustClusterId');
  if (_pagemodel.inacCd == null) {
    FormManager.setValue('inacCd', '');
    FormManager.setValue('inacType', '');
  }

  var cntry = FormManager.getActualValue('cmrIssuingCntry');

  FormManager.enable('inacCd');
  FormManager.enable('inacType');

  if (custSubGrp == 'NRMLC' || custSubGrp == 'AQSTN' || custSubGrp == 'XAQST') {
    FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Q','E']);
    FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), ['12354', '12349', '12352', '12350', '12351', '12353', '12464', '12453', '12482', '12483', '12484', '12485', '12486']);
    FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['34','27']);
    if(_apCustClusterId == '12464' || _apCustClusterId == '12453' ||_apCustClusterId == '12482' || _apCustClusterId == '12483' || _apCustClusterId == '12484'|| _apCustClusterId == '12485'|| _apCustClusterId == '12486') {
      FormManager.setValue('isuCd', '27');
      FormManager.setValue('clientTier', 'E');
    } else {
      FormManager.setValue('isuCd', '34');
      FormManager.setValue('clientTier', 'Q');
    }
    FormManager.setValue('apCustClusterId', '12354');
    FormManager.setValue('mrcCd', '3');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('mrcCd');
  } else if (custSubGrp == 'BUSPR') {
    FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Z']);
    FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), ['00089']);
    FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['8B']);
    FormManager.setValue('apCustClusterId', '00089');
    FormManager.setValue('clientTier', 'Z');
    FormManager.setValue('isuCd', '8B');
    FormManager.setValue('mrcCd', '3');
    FormManager.readOnly('apCustClusterId');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('mrcCd');
  }  else if (custSubGrp == 'PRIV') {
    FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Z']);
    FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), ['00000']);
    FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['34']);
    FormManager.setValue('apCustClusterId', '00000');
    FormManager.setValue('clientTier', 'Z');
    FormManager.setValue('isuCd', '34');
    FormManager.setValue('mrcCd', '3');
    FormManager.readOnly('apCustClusterId');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('mrcCd');
  } 
  else if (custSubGrp == 'ASLOM' || custSubGrp == 'XASLM' || custSubGrp == 'CROSS') {
    if (custSubGrp == 'CROSS') {
      FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Q', '0', 'Y', 'T', 'Z', 'E']);
      FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), ['09194', '08042', '00000',  '12354', '12349', '12352', '12350', '12351', '12353', '12464', '12453', '12482', '12483', '12484', '12485', '12486']);
    } else {
      FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Q', '0', 'Y', 'T', 'E']);
      FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), ['09194', '08042',  '12354', '12349', '12352', '12350', '12351', '12353', '12464', '12453', '12482', '12483', '12484', '12485', '12486']);
    }
    FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['32', '34', '5K', '36', '27']);
    FormManager.limitDropdownValues(FormManager.getField('inacType'), ['I']);
    FormManager.setValue('inacCd', '');
    FormManager.setValue('inacType', '');
    if (_clusterHandlerINDONESIA == 0 && _pagemodel.apCustClusterId == null) {
      FormManager.setValue('clientTier', '');
      FormManager.setValue('apCustClusterId', '');
      FormManager.setValue('isuCd', '');
      _apCustClusterId = '';
    }
    if (_apCustClusterId == '12464' || _apCustClusterId == '12453' || _apCustClusterId == '12482' || _apCustClusterId == '12483'
          || _apCustClusterId == '12484' || _apCustClusterId == '12485' || _apCustClusterId == '12486' ) {
      FormManager.setValue('clientTier', 'Q');
      FormManager.setValue('isuCd', '34');
    } else  if (_apCustClusterId == '12354' || _apCustClusterId == '12349' || _apCustClusterId == '12352' || _apCustClusterId == '12350' || _apCustClusterId == '12351' || _apCustClusterId == '12353' ) {
      FormManager.setValue('clientTier', 'E');
      FormManager.setValue('isuCd', '27');
    } else if (_apCustClusterId == '08042') {
      FormManager.setValue('clientTier', 'Y');
      FormManager.setValue('isuCd', '36');
    } else if (_apCustClusterId == '09194') {
      FormManager.limitDropdownValues(FormManager.getField('inacType'), ['I']);
      FormManager.setValue('clientTier', '0');
      FormManager.setValue('isuCd', '5K');
      FormManager.setValue('inacCd', '6272');
      FormManager.setValue('inacType', 'I');
      FormManager.readOnly('inacCd');
      FormManager.readOnly('inacType');
    } else if (_apCustClusterId == '00000') {
      FormManager.setValue('clientTier', 'Z');
      FormManager.setValue('isuCd', '34');
    }
    FormManager.setValue('mrcCd', '3');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('mrcCd');
  } else if (custSubGrp == 'ECSYS') {
    FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Y']);
    FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), ['08042']);
    FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['36']);
    FormManager.setValue('apCustClusterId', '08042');
    FormManager.setValue('clientTier', 'Y');
    FormManager.setValue('isuCd', '36');
    FormManager.setValue('mrcCd', '3');
    FormManager.readOnly('apCustClusterId');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('mrcCd');
  } else if (custSubGrp == 'DUMMY') {
    FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Z']);
    FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), ['00000']);
    FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['21']);
    FormManager.setValue('apCustClusterId', '00000');
    FormManager.setValue('clientTier', 'Z');
    FormManager.setValue('isuCd', '21');
    FormManager.setValue('mrcCd', '2');
    FormManager.readOnly('apCustClusterId');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('mrcCd');

  } else if (custSubGrp == 'INTER') {
    FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['0']);
    FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), ['00000']);
    FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['60']);
    FormManager.setValue('apCustClusterId', '00000');
    FormManager.setValue('clientTier', '0');
    FormManager.setValue('isuCd', '60');
    FormManager.setValue('mrcCd', '2');
    FormManager.readOnly('apCustClusterId');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('mrcCd');
  } else if (custSubGrp == 'KYND') {
    FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['0']);
    FormManager.limitDropdownValues(FormManager.getField('apCustClusterId'), ['09194']);
    FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['5K']);
    FormManager.limitDropdownValues(FormManager.getField('inacType'), ['I']);
    FormManager.setValue('apCustClusterId', '09194');
    FormManager.setValue('clientTier', '0');
    FormManager.setValue('isuCd', '5K');
    FormManager.setValue('mrcCd', '3');
    FormManager.setValue('inacCd', '6272');
    FormManager.setValue('inacType', 'I');
    FormManager.readOnly('apCustClusterId');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('mrcCd');
    FormManager.readOnly('inacCd');
    FormManager.readOnly('inacType');
  }
}

function validateGCGCustomerName() {
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
        var errorMsg = '';
        var action = FormManager.getActualValue('yourAction');
        var custNm1 = FormManager.getActualValue('mainCustNm1').toUpperCase();

        var reqType = FormManager.getActualValue('reqType');
        var role = FormManager.getActualValue('userRole').toUpperCase();
        var custGrp = FormManager.getActualValue('custGrp');
        var custSubGrp = FormManager.getActualValue('custSubGrp');

        if (reqType == 'C') {
          if (role == 'REQUESTER' && custGrp == 'LOCAL' && custSubGrp == 'KYND') {
            if (custNm1.indexOf('KYNDRYL') < 0) {
              errorMsg = 'Customer name must contain word \'Kyndryl\'';
            }
          }
        }

        if (errorMsg != '') {
          return new ValidationResult({
            id: 'custNm1',
            type: 'text',
            name: 'custNm1'
          }, false, errorMsg);
        }

        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function setCTCIsuByClusterGCG() {
  console.log('>>>> setCTCIsuByClusterGCG >>>>');
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (reqType != 'C') {
    return;
  }
  var _clusterHandler = dojo.connect(FormManager.getField('apCustClusterId'), 'onChange', function (value) {
    var clusterVal = FormManager.getActualValue('apCustClusterId');
    if (!clusterVal) {
      return;
    }
    var _cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
    var _cluster = FormManager.getActualValue('apCustClusterId');
    var custSubGrp = FormManager.getActualValue('custSubGrp');
    var apClientTierValue = [];
    var isuCdValue = [];
    if (_cluster != '' && _cluster != '') {
      if (_cluster.indexOf(" - ") > 0) {
        _cluster = _cluster.substring(0, _cluster.indexOf(" - "));
      }
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
          FormManager.limitDropdownValues(FormManager.getField('isuCd'), isuCdValue);
          FormManager.setValue('clientTier', apClientTierValue[0]);
          FormManager.setValue('isuCd', isuCdValue[0]);
        } else if (apClientTierValue.length > 1) {
          if (custSubGrp == 'MKTPC' || custSubGrp == 'BLUMX' || custSubGrp == 'CROSS') {
            FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Z']);
            FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['34']);
            FormManager.setValue('clientTier', 'Z');
            FormManager.setValue('isuCd', '34');
            FormManager.setValue('mrcCd', '3');
          } else if (custSubGrp == 'INTER' || custSubGrp == 'DUMMY') {
            FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Z']);
            FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['21']);
            FormManager.setValue('clientTier', 'Z');
            FormManager.setValue('isuCd', '21');
            FormManager.setValue('isuCd', '21');
            FormManager.setValue('mrcCd', '2');
          } else {
            FormManager.resetDropdownValues(FormManager.getField('clientTier'));
            FormManager.resetDropdownValues(FormManager.getField('isuCd'));
            FormManager.setValue('clientTier', '');
            FormManager.setValue('isuCd', '');
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

function setClusterOnScenarioChgGCG(fromAddress, scenario, scenarioChanged) {
  console.log('>>>> setClusterOnScenarioChgGCG >>>>');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var reqType = FormManager.getActualValue('reqType');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var viewOnly = FormManager.getActualValue('viewOnlyPage');

  if (viewOnly == 'true' || role != 'REQUESTER') {
    return;
  }

  if (reqType == 'C' && scenarioChanged) {
    switch (custSubGrp) {
      case 'CROSS':
        FormManager.setValue('apCustClusterId', '00000');
        break;
      default:
        // do nothing
        break;
    }
  }
}


function validateCustnameForKynd() {
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
        var errorMsg = '';
        var action = FormManager.getActualValue('yourAction');
        var custNm1 = FormManager.getActualValue('mainCustNm1').toUpperCase();
        var reqType = FormManager.getActualValue('reqType');
        var role = FormManager.getActualValue('userRole').toUpperCase();
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        if (reqType == 'C' && custSubGrp == 'KYND') {
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

function setDefaultOnScenarioChangeTH(fromAddress, scenario, scenarioChanged) {
  console.log('>>>> setDefaultOnScenarioChangeTH >>>>');
  var viewOnly = FormManager.getActualValue('viewOnlyPage');
  if (viewOnly != '' && viewOnly == 'true') {
    return;
  }

  if (scenarioChanged && scenario == 'DUMMY') {
    var isInacTypeReadOnlyFromScenarios = TemplateService.isFieldReadOnly('inacType');
    if (isInacTypeReadOnlyFromScenarios) {
      FormManager.readOnly('inacType');
    } else {
      FormManager.enable('inacType');
    }
  }
}

function setCTCIsuByClusterPhVnTh(apClientTierValue, isuCdValue) {
  console.log('>>>> setCTCIsuByClusterPhVnTh >>>>');
  var _cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  var scenario = FormManager.getActualValue('custGrp');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var cluster = FormManager.getActualValue('apCustClusterId');
  var issuingCnt3 = ['818', '856', '852'];


  if (custSubGrp == 'BLUM' || custSubGrp == 'MKTP' || custSubGrp == 'MKP'
    || ((custSubGrp == 'DUMMY' || custSubGrp == 'XDUMM') && issuingCnt3.includes(_cmrIssuingCntry))) {
    if (custSubGrp == 'DUMMY') {
      FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Z']);
      FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['21']);
      FormManager.setValue('isuCd', '21');
      FormManager.setValue('clientTier', 'Z');
      FormManager.readOnly('isuCd');
    } else {
      FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Q', 'Y']);
      FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['34']);
      FormManager.setValue('isuCd', '34');
      if (_cmrIssuingCntry == '856' && custSubGrp == 'BUSPR') {
        FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Z']);
      }
    }
  } else if (custSubGrp.includes('INTER')) {
    FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['0']);
    FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['60']);
    FormManager.setValue('clientTier', '0');
    FormManager.setValue('isuCd', '60');
  } else if (custSubGrp.includes('CROSS') && issuingCnt3.includes(_cmrIssuingCntry) && cluster == '00000') {
    FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Z']);
    FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['34']);
    FormManager.setValue('clientTier', 'Z');
    FormManager.setValue('isuCd', '34');
  } else {
    FormManager.resetDropdownValues(FormManager.getField('clientTier'));
    FormManager.limitDropdownValues(FormManager.getField('clientTier'), apClientTierValue);
    FormManager.limitDropdownValues(FormManager.getField('isuCd'), isuCdValue);
  }

  if (_cmrIssuingCntry == '856') {
    var isClientTierReadOnlyFromScenarios = TemplateService.isFieldReadOnly('clientTier');
    if (isClientTierReadOnlyFromScenarios) {
      FormManager.readOnly('clientTier');
    } else {
      FormManager.enable('clientTier');
    }
  }
}

function clearInacOnScenarioChange(fromAddress, scenario, scenarioChanged) {
  console.log('>>>> clearInacOnScenarioChange >>>>');
  var viewOnly = FormManager.getActualValue('viewOnlyPage');
  if (viewOnly != '' && viewOnly == 'true') {
    return;
  }

  if (scenarioChanged) {
    var hasDefaultVal = ['KYND'];
    var cntry = FormManager.getActualValue('cmrIssuingCntry');
    if (!hasDefaultVal.includes(scenario)) {
      FormManager.setValue('inacCd', '');
      FormManager.setValue('inacType', '');
    }
  }
}

var _oldClusterSelection = '';
function clearInacOnClusterChange(selectedCluster) {
  console.log('>>>> clearInacOnClusterChange >>>>');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');

  if (cntry == '856' || cntry == '852' || cntry == '818') {
    var viewOnly = FormManager.getActualValue('viewOnlyPage');
    if (viewOnly != '' && viewOnly == 'true') {
      return;
    }
    var scenario = FormManager.getActualValue('custSubGrp');
    var clearInacScenarios = ['XASLM', 'CROSS', 'ASLOM'];
    var noFilterInac = ['01251', '00000', '08047'];

    if (cntry == '818') {
      clearInacScenarios = ['ASLOM', 'CROSS', 'XASLO'];
      noFilterInac = ['01231', '08044', '00000'];
    }

    if (cntry == '852') {
      clearInacScenarios = ['ASLOM', 'CROSS', 'XASLM'];
      noFilterInac = ['01273', '01277', '08046', '00000'];
    }

    var clusterValueChanged = _oldClusterSelection != selectedCluster && cmr.currentTab == 'IBM_REQ_TAB';
    if (clusterValueChanged && clearInacScenarios.includes(scenario) && noFilterInac.includes(selectedCluster)) {
      FormManager.setValue('inacCd', '');
      FormManager.setValue('inacType', '');

      if (cntry == '856') {
        FormManager.removeValidator('inacCd', Validators.REQUIRED);
      }
    }
    _oldClusterSelection = selectedCluster;
  }
}

function clearClusterFieldsOnScenarioChange(fromAddress, scenario, scenarioChanged) {
  console.log('>>>> clearClusterFieldsOnScenarioChange >>>>');
  var cluster = FormManager.getActualValue('apCustClusterId');
  var clusterSGAllInac = (cluster == '01241' && ['NRMLC', 'XAQST', 'AQSTN', 'ASLOM', 'CROSS'].includes(custSubGrp)) ||
    (cluster == '00000' && ['XBLUM', 'BLUMX', 'XMKTP', 'MKTPC', 'SPOFF', 'CROSS'].includes(custSubGrp)) ||
    (cluster == '08038' && ['ECSYS', 'ASLOM', 'CROSS'].includes(custSubGrp));
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var issuingCnt = ['818', '856', '852', '616', '796', '834'];
  var inacCdValue = [];
  var cmt = '%' + cluster + '%';

  if (issuingCnt.includes(cntry)) {
    var viewOnly = FormManager.getActualValue('viewOnlyPage');
    if (viewOnly != '' && viewOnly == 'true') {
      return;
    } else if (clusterSGAllInac) {
      console.log('setInacCdTypeStatus() >>>> clusterSGAllInac.');
      cmt = '%';
    }

    var qParams = {
      _qall: 'Y',
      ISSUING_CNTRY: cntry,
      CMT: cmt,
    };

    var results = cmr.query('GET.INAC_CD', qParams);
    if (results != null) {
      for (var i = 0; i < results.length; i++) {
        if (!inacCdValue.includes(results[i].ret1)) {
          inacCdValue.push(results[i].ret1);
        }
      }
    }
    console.log('inacCd.length =' + inacCdValue.length);
    console.log('Find inacCd =' + inacCdValue);
    if (inacCdValue.length == 1 && inacCdValue[0] != '') {
      console.log('setInacCdTypeStatus() >>>> set INAC code/type readonly >>>');
      FormManager.readOnly('inacCd');
      FormManager.readOnly('inacType');
    } else if (inacCdValue.length > 1) {
      console.log('setInacCdTypeStatus() >>>> enable INAC related >>>>');
      FormManager.enable('inacCd');
      FormManager.enable('inacType');
    }

    if (cntry == '616') {
      clearClusterFieldsScenarios = ['ESOSW', 'NRML'];
      if (scenario == 'CROSS' && scenarioChanged) {
        FormManager.setValue('apCustClusterId', '00001');
      }
    }

    if (cntry == '834') {
      clearClusterFieldsScenarios = ['ASLOM', 'NRML'];
      if (scenario == 'CROSS' && scenarioChanged) {
        FormManager.setValue('apCustClusterId', '00000');
      }
    }
    // CREATCMR-7884
    if (cntry == '796') {
      clearClusterFieldsScenarios = ['ESOSW', 'NRML', 'NRMLC', 'AQSTN', 'XAQST', 'XESO'];
      if (scenario == 'CROSS' && scenarioChanged) {
        FormManager.setValue('apCustClusterId', '00002');
      }
    }
    
    if (cntry == '818') {
      clearClusterFieldsScenarios = ['ASLOM', 'AQSTN', 'ECSYS', 'NRML', 'XASLO', 'XAQST'];
      if (scenarioChanged && clearClusterFieldsScenarios.includes(scenario)) {
        FormManager.setValue('apCustClusterId', '');
        FormManager.setValue('clientTier', '');
        FormManager.setValue('isuCd', '');
        FormManager.setValue('mrcCd', '');
      }
    }
    // CREATCMR-7883-7884
    var _custSubGrpAUWithEmptyInac = ['INTER', 'XPRIV', 'PRIV', 'DUMMY'];
    var _custSubGrpNZWithEmptyInac = ['INTER', 'XPRIV', 'PRIV', 'DUMMY'];
    if (scenarioChanged && ((cntry == '616' && _custSubGrpAUWithEmptyInac.includes(scenario)) || cntry == '796' && _custSubGrpNZWithEmptyInac.includes(scenario))) {
      FormManager.setValue('inacCd', '');
      FormManager.setValue('inacType', '');
      FormManager.limitDropdownValues(FormManager.getField('inacCd'), []);
      FormManager.limitDropdownValues(FormManager.getField('inacType'), []);
      FormManager.readOnly('inacType');
      FormManager.readOnly('inacCd');
    }
    // LOCK GB Seg(QTC)/ISU
    FormManager.readOnly('clientTier');
    FormManager.readOnly('isuCd');
  }
}

// CREATCMR-7884
function lockClusterFieldsOnScenarioChange(scenario, scenarioChanged) {
  console.log('>>>> lockClusterFieldsOnScenarioChange >>>>');
  var viewOnly = FormManager.getActualValue('viewOnlyPage');
  var reqType = FormManager.getActualValue('reqType');
  var scenario = FormManager.getActualValue('custSubGrp');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');

  if (viewOnly != '' && viewOnly == 'true') {
    return;
  }

  var lockClusterScenarios = [];
  if (cntry == '796') {
    lockClusterScenarios = ['NRMLC', 'AQSTN'];
  }

  if (scenarioChanged && reqType == 'C' && lockClusterScenarios.includes(scenario)) {
    FormManager.readOnly('apCustClusterId');
  } else {
    FormManager.enable('apCustClusterId');
  }
}

function addKyndrylValidator() {
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
        var reqType = FormManager.getActualValue('reqType');
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        if (reqType == 'C' && custSubGrp == 'KYNDR') {
          var custName = FormManager.getActualValue('mainCustNm1');
          if (!custName.toLowerCase().includes('kyndryl')) {
            return new ValidationResult({
              id: 'mainCustNm1',
              type: 'text',
              name: 'mainCustNm1'
            }, false, 'Customer name must contain the word "Kyndryl".');
          }

        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function reqReasonHandler() {
  var _reqReasonHandler = dojo.connect(FormManager.getField('reqReason'), 'onChange', function (value) {
    var reqReason = FormManager.getActualValue('reqReason');
    checkEmbargoCd(reqReason);
  });
  if (_reqReasonHandler && _reqReasonHandler[0]) {
    _reqReasonHandler[0].onChange();
  }
}

function checkEmbargoCd(value) {
  releaseFieldsFromERO();
  if (value != 'TREC')
    return;
  var reqId = FormManager.getActualValue('reqId');
  var emabargoCd = FormManager.getActualValue('ordBlk');
  var qParams = {
    REQ_ID: reqId
  };

  lockFieldsForERO();

  var result = cmr.query('GET.DATA_RDC.EMBARGO_BY_REQID_SWISS', qParams);
  if (result.ret1 == '88') {

  } else {
    FormManager.clearValue('reqReason');
    cmr.showAlert('This Request reason can be chosen only if imported record has 88 embargo code.');
    return;
  }
}

// CREATCMR-7885
// CREATCMR-7885
function setInacCdTypeStatus() {
  console.log('>>>> setInacCdTypeStatus() >>>>');
  var reqType = FormManager.getActualValue('reqType');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');

  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var custSubGrpListSg = ['PRIV', 'XPRIV', 'BUSPR', 'DUMMY', 'INTER'];

  var cluster = FormManager.getActualValue('apCustClusterId');
  var clusterSGEmptyInac = (cluster == '00000' && custSubGrpListSg.includes(custSubGrp));
  var clusterSGAllInac = (cluster == '01241' && ['NRMLC', 'XAQST', 'AQSTN', 'ASLOM', 'CROSS'].includes(custSubGrp)) ||
    (cluster == '00000' && ['XBLUM', 'BLUMX', 'XMKTP', 'MKTPC', 'SPOFF', 'CROSS'].includes(custSubGrp)) ||
    (cluster == '08038' && ['ECSYS', 'ASLOM', 'CROSS'].includes(custSubGrp));

  var isInacRequired = (cluster == '09052' && ['KYND', 'ASLOM', 'CROSS'].includes(custSubGrp)) ||
    (['10144', '10145', '10146', '10147', '10148', '10149', '10150', '10151', '10152', '10153', '10154', '10155', '10156', '10157'].includes(cluster) && ['NRML', 'ASLOM', 'CROSS'].includes(custSubGrp));


  console.log('setInacCdTypeStatus() >>>> cmrIssuingCntry=' + cntry);

  var inacCdValue = [];
  var cmt = '';

  if (cntry == '834' && reqType == 'C') {
    if (clusterSGEmptyInac) {
      FormManager.removeValidator('inacCd', Validators.REQUIRED);
      FormManager.removeValidator('inacType', Validators.REQUIRED);
      FormManager.setValue('inacCd', '');
      FormManager.setValue('inacType', '');
      // LOCK GB Seg(QTC)/ISU
      FormManager.readOnly('clientTier');
      FormManager.readOnly('isuCd');
      console.log('remove REQUIRED of INAC related>>>>');
      console.log('setInacCdTypeStatus() >>>> clusterSGEmptyInac, return.');
      return;
    } else if (clusterSGAllInac) {
      console.log('setInacCdTypeStatus() >>>> clusterSGAllInac.');
      cmt = '%';
    } else if (!custSubGrpListSg.includes(custSubGrp)) {
      cmt = '%' + cluster + '%';
      console.log('setInacCdTypeStatus() >>>> cmt=' + cmt);
    }

    var qParams = {
      _qall: 'Y',
      ISSUING_CNTRY: cntry,
      CMT: cmt,
    };

    var results = cmr.query('GET.INAC_CD', qParams);
    if (results != null) {
      for (var i = 0; i < results.length; i++) {
        if (!inacCdValue.includes(results[i].ret1)) {
          inacCdValue.push(results[i].ret1);
        }
      }
    }
    console.log('inacCd.length =' + inacCdValue.length);
    console.log('Find inacCd =' + inacCdValue);
    if (inacCdValue.length == 1 && inacCdValue[0] != '') {
      console.log('setInacCdTypeStatus() >>>> set INAC code/type readonly >>>');
      FormManager.readOnly('inacCd');
      FormManager.readOnly('inacType');
    } else if (inacCdValue.length > 1) {
      console.log('setInacCdTypeStatus() >>>> enable INAC related >>>>');
      FormManager.enable('inacCd');
      FormManager.enable('inacType');
    }

    // Add/Remove REQUIRE mark for INAC related
    if (clusterSGAllInac) {
      console.log('remove REQUIRED of INAC TYPE/CODE for SG/834 >>>>');
      FormManager.removeValidator('inacCd', Validators.REQUIRED);
      FormManager.removeValidator('inacType', Validators.REQUIRED);
      FormManager.setValue('inacCd', '');
      FormManager.setValue('inacType', '');
    }

    if (isInacRequired) {
      console.log('add REQUIRED of INAC TYPE/CODE for SG/834 >>>>');
      FormManager.addValidator('inacCd', Validators.REQUIRED, ['INAC/NAC Code'], 'MAIN_IBM_TAB');
      FormManager.addValidator('inacType', Validators.REQUIRED, ['INAC Type'], 'MAIN_IBM_TAB');
    }
    // LOCK GB Seg(QTC)/ISU
    FormManager.readOnly('clientTier');
    FormManager.readOnly('isuCd');
  }
}

function setInacCdTypeStatus() {
  console.log('>>>> setInacCdTypeStatus() >>>>');
  var reqType = FormManager.getActualValue('reqType');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');

  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var custSubGrpListSg = ['PRIV', 'XPRIV', 'BUSPR', 'DUMMY', 'INTER'];

  var cluster = FormManager.getActualValue('apCustClusterId');
  var clusterSGEmptyInac = (cluster == '00000' && custSubGrpListSg.includes(custSubGrp));
  var clusterSGAllInac = (cluster == '01241' && ['NRMLC', 'XAQST', 'AQSTN', 'ASLOM', 'CROSS'].includes(custSubGrp)) ||
    (cluster == '00000' && ['XBLUM', 'BLUMX', 'XMKTP', 'MKTPC', 'SPOFF', 'CROSS'].includes(custSubGrp)) ||
    (cluster == '08038' && ['ECSYS', 'ASLOM', 'CROSS'].includes(custSubGrp));

  var isInacRequired = (cluster == '09052' && ['KYND', 'ASLOM', 'CROSS'].includes(custSubGrp)) ||
    (['10144', '10145', '10146', '10147', '10148', '10149', '10150', '10151', '10152', '10153', '10154', '10155', '10156', '10157'].includes(cluster) && ['NRML', 'ASLOM', 'CROSS'].includes(custSubGrp));


  console.log('setInacCdTypeStatus() >>>> cmrIssuingCntry=' + cntry);

  var inacCdValue = [];
  var cmt = '';

  if (cntry == '834' && reqType == 'C') {
    if (clusterSGEmptyInac) {
      FormManager.removeValidator('inacCd', Validators.REQUIRED);
      FormManager.removeValidator('inacType', Validators.REQUIRED);
      FormManager.setValue('inacCd', '');
      FormManager.setValue('inacType', '');
      // LOCK GB Seg(QTC)/ISU
      FormManager.readOnly('clientTier');
      FormManager.readOnly('isuCd');
      console.log('remove REQUIRED of INAC related>>>>');
      console.log('setInacCdTypeStatus() >>>> clusterSGEmptyInac, return.');
      return;
    } else if (clusterSGAllInac) {
      console.log('setInacCdTypeStatus() >>>> clusterSGAllInac.');
      cmt = '%';
    } else if (!custSubGrpListSg.includes(custSubGrp)) {
      cmt = '%' + cluster + '%';
      console.log('setInacCdTypeStatus() >>>> cmt=' + cmt);
    }

    var qParams = {
      _qall: 'Y',
      ISSUING_CNTRY: cntry,
      CMT: cmt,
    };

    var results = cmr.query('GET.INAC_CD', qParams);
    if (results != null) {
      for (var i = 0; i < results.length; i++) {
        if (!inacCdValue.includes(results[i].ret1)) {
          inacCdValue.push(results[i].ret1);
        }
      }
    }
    console.log('inacCd.length =' + inacCdValue.length);
    console.log('Find inacCd =' + inacCdValue);
    if (inacCdValue.length == 1 && inacCdValue[0] != '') {
      console.log('setInacCdTypeStatus() >>>> set INAC code/type readonly >>>');
      FormManager.readOnly('inacCd');
      FormManager.readOnly('inacType');
    } else if (inacCdValue.length > 1) {
      console.log('setInacCdTypeStatus() >>>> enable INAC related >>>>');
      FormManager.enable('inacCd');
      FormManager.enable('inacType');
    }

    // Add/Remove REQUIRE mark for INAC related
    if (clusterSGAllInac) {
      console.log('remove REQUIRED of INAC TYPE/CODE for SG/834 >>>>');
      FormManager.removeValidator('inacCd', Validators.REQUIRED);
      FormManager.removeValidator('inacType', Validators.REQUIRED);
      FormManager.setValue('inacCd', '');
      FormManager.setValue('inacType', '');
    }

    if (isInacRequired) {
      console.log('add REQUIRED of INAC TYPE/CODE for SG/834 >>>>');
      FormManager.addValidator('inacCd', Validators.REQUIRED, ['INAC/NAC Code'], 'MAIN_IBM_TAB');
      FormManager.addValidator('inacType', Validators.REQUIRED, ['INAC Type'], 'MAIN_IBM_TAB');
      FormManager.enable('inacCd');
      FormManager.enable('inacType');
    }
    // LOCK GB Seg(QTC)/ISU
    FormManager.readOnly('clientTier');
    FormManager.readOnly('isuCd');
  }
}

function setClusterGlcCovIdMapNrmlc() {
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var covId = FormManager.getActualValue('covId');
  if (custSubGrp == "NRMLC" || custSubGrp == 'AQSTN') {
    var glc = FormManager.getActualValue('geoLocationCd');
    var covId = FormManager.getActualValue('covId');
    var qParams = {
      TXT: glc,
    };
    var cluster = cmr.query('GET_CLUSTER_BY_GLC', qParams);
    if (cluster != null && cluster.ret1) {
      FormManager.setValue('apCustClusterId', cluster.ret1);
      FormManager.readOnly('apCustClusterId');
    }
  }
}

function validateRetrieveValues() {
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
        var errorMsg = '';
        var reqType = FormManager.getActualValue('reqType');
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        var hasRetrievedValue = FormManager.getActualValue('covBgRetrievedInd') == 'Y';
        if (reqType == 'C' && (custSubGrp == 'NRMLC' || custSubGrp == 'AQSTN')) {
          if (!hasRetrievedValue) {
            errorMsg = 'Clicking on retrieve value button is required action. Please click on Retrieve Values';
          }
        }
        if (errorMsg != '') {
          return new ValidationResult(null, false, errorMsg);
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'IBM_REQ_TAB', 'frmCMR');
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

function inacValidation(){
  var value = FormManager.getActualValue('inacType');
  var cluster = FormManager.getActualValue('apCustClusterId');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (value && dojo.string.trim(value) == 'I') {
    FormManager.addValidator('inacCd', Validators.NUMBER, [ 'INAC Code' ], 'MAIN_IBM_TAB');
  } else {
    FormManager.removeValidator('inacCd', Validators.NUMBER);
  }
  if (value && dojo.string.trim(value) == 'N') {
    FormManager.addValidator('inacCd', Validators.ALPHANUMONLY, [ 'NAC Code' ], 'MAIN_IBM_TAB');
  } else {
    FormManager.removeValidator('inacCd', Validators.ALPHANUMONLY);
  }
  if (custSubGrp == 'KYND' || custSubGrp == 'ASLOM' || custSubGrp == 'XASLM' || custSubGrp == 'CROSS') {
    if (cluster == '09197') {
      FormManager.setValue('inacCd', '6272');
      FormManager.setValue('inacType', 'I');
      FormManager.readOnly('inacType');
      FormManager.readOnly('inacCd');
    } else {
      FormManager.setValue('inacCd', '');
      FormManager.setValue('inacType', '');
      FormManager.enable('inacType');
      FormManager.enable('inacCd');
    }
  }
}

dojo.addOnLoad(function () {
  GEOHandler.AP = [SysLoc.AUSTRALIA, SysLoc.BANGLADESH, SysLoc.BRUNEI, SysLoc.MYANMAR, SysLoc.SRI_LANKA, SysLoc.INDIA, SysLoc.INDONESIA, SysLoc.PHILIPPINES, SysLoc.SINGAPORE, SysLoc.VIETNAM,
  SysLoc.THAILAND, SysLoc.HONG_KONG, SysLoc.NEW_ZEALAND, SysLoc.LAOS, SysLoc.MACAO, SysLoc.MALASIA, SysLoc.NEPAL, SysLoc.CAMBODIA];
  GEOHandler.ISA = [SysLoc.INDIA, SysLoc.SRI_LANKA, SysLoc.BANGLADESH, SysLoc.NEPAL];
  GEOHandler.ASEAN = [SysLoc.BRUNEI, SysLoc.MYANMAR, SysLoc.INDONESIA, SysLoc.PHILIPPINES, SysLoc.SINGAPORE, SysLoc.VIETNAM, SysLoc.THAILAND, SysLoc.LAOS, SysLoc.MALASIA, SysLoc.CAMBODIA];
  GEOHandler.ANZ = [SysLoc.AUSTRALIA, SysLoc.NEW_ZEALAND];
  GEOHandler.GCG = [SysLoc.HONG_KONG, SysLoc.MACAO];
  GEOHandler.APAC_1 = [SysLoc.SINGAPORE, SysLoc.PHILIPPINES, SysLoc.THAILAND, SysLoc.MALASIA, SysLoc.INDONESIA, SysLoc.BRUNEI, SysLoc.VIETNAM, SysLoc.INDIA, SysLoc.BANGLADESH, SysLoc.SRI_LANKA, SysLoc.AUSTRALIA, SysLoc.NEW_ZEALAND, SysLoc.CHINA, SysLoc.HONG_KONG, SysLoc.MACAO, SysLoc.TAIWAN, SysLoc.KOREA];
  // CREATCMR-6825
  GEOHandler.APAC = [SysLoc.SINGAPORE, SysLoc.PHILIPPINES, SysLoc.THAILAND, SysLoc.MALASIA, SysLoc.INDONESIA, SysLoc.BRUNEI, SysLoc.VIETNAM, SysLoc.INDIA, SysLoc.BANGLADESH, SysLoc.SRI_LANKA,
  SysLoc.AUSTRALIA, SysLoc.NEW_ZEALAND, SysLoc.HONG_KONG, SysLoc.MACAO];
  console.log('adding AP functions...');
  console.log('the value of person full id is ' + localStorage.getItem("pID"));
  GEOHandler.setRevertIsicBehavior(false);
  GEOHandler.registerValidator(addSalesRepNameNoCntryValidator, [SysLoc.AUSTRALIA, SysLoc.INDONESIA, SysLoc.PHILIPPINES, SysLoc.SINGAPORE, SysLoc.VIETNAM, SysLoc.THAILAND, SysLoc.HONG_KONG,
  SysLoc.NEW_ZEALAND, SysLoc.MACAO, SysLoc.MALASIA, SysLoc.NEPAL]);
  GEOHandler.addAfterTemplateLoad(addSalesRepNameNoCntryValidatorBluepages, [SysLoc.BRUNEI, SysLoc.MYANMAR, SysLoc.LAOS, SysLoc.CAMBODIA]);

  // 1558942: Update Address Details of different address types at one time
  GEOHandler.enableCopyAddress(GEOHandler.AP);
  GEOHandler.addAfterConfig(reqReasonHandler, [SysLoc.MACAO, SysLoc.HONG_KONG]);

  GEOHandler.addAfterConfig(addAfterConfigAP, GEOHandler.AP);
  GEOHandler.addAfterTemplateLoad(addAfterConfigAP, GEOHandler.AP);
  GEOHandler.addAfterConfig(saveClusterAfterSave, [SysLoc.INDIA]);
  GEOHandler.addAfterConfig(savePreviousSubScenario, [SysLoc.INDIA]);
  GEOHandler.addAfterConfig(updateIndustryClass, GEOHandler.AP);
  GEOHandler.addAfterConfig(updateProvCd, GEOHandler.AP);
  GEOHandler.addAfterConfig(updateRegionCd, GEOHandler.AP);
  GEOHandler.addAfterConfig(setCollectionCd, GEOHandler.AP, [SysLoc.AUSTRALIA, SysLoc.INDIA]);
  GEOHandler.addAfterConfig(setCollCdFrAU, [SysLoc.AUSTRALIA]);
  GEOHandler.addAfterConfig(setCollCdFrIndia, [SysLoc.INDIA]);
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
  GEOHandler.addAfterConfig(setCTCIsuByClusterGCG, GEOHandler.GCG);
  GEOHandler.addAfterTemplateLoad(setCTCIsuByClusterGCG, GEOHandler.GCG);
  GEOHandler.addAfterConfig(setCTCIsuByClusterANZ, GEOHandler.ANZ);
  GEOHandler.addAfterTemplateLoad(setCTCIsuByClusterANZ, GEOHandler.ANZ);
  GEOHandler.addAfterConfig(setCTCIsuByClusterASEAN, GEOHandler.ASEAN);
  // CREATCMR-7878
  GEOHandler.addAfterConfig(setCTCIsuByClusterISA, [SysLoc.SRI_LANKA, SysLoc.BANGLADESH]);
  GEOHandler.addAfterTemplateLoad(setISUDropDownValues, GEOHandler.AP);
  // GEOHandler.addAfterConfig(setIsuByClusterCTC, GEOHandler.AP);
  GEOHandler.registerValidator(addAbnValidatorForAU, [SysLoc.AUSTRALIA]);

  // GEOHandler.registerValidator(addMandateCmrNoForSG, [SysLoc.SINGAPORE]);
  GEOHandler.registerValidator(addCmrNoValidator, [SysLoc.SINGAPORE]);
  GEOHandler.registerValidator(addCompanyProofAttachValidation, [SysLoc.INDIA]);
  GEOHandler.registerValidator(addressNameSimilarValidator, [SysLoc.INDIA]);
  GEOHandler.registerValidator(addressNameSameValidator, [SysLoc.SINGAPORE]);
  GEOHandler.registerValidator(addCompanyProofForSG, [SysLoc.SINGAPORE]);
  GEOHandler.registerValidator(additionalAddrNmValidator, [SysLoc.SINGAPORE]);


  GEOHandler.addAfterConfig(removeStateValidatorForHkMoNZ, [SysLoc.AUSTRALIA, SysLoc.NEW_ZEALAND]);
  GEOHandler.addAfterTemplateLoad(removeStateValidatorForHkMoNZ, [SysLoc.AUSTRALIA, SysLoc.NEW_ZEALAND]);

  // setting collection code for ASEAN
  GEOHandler.addAfterConfig(onISBUCdChange, GEOHandler.ASEAN);

  GEOHandler.addAfterConfig(addGovIndcHanlder, [SysLoc.AUSTRALIA]);
  GEOHandler.addAfterConfig(addGovCustTypHanlder, [SysLoc.AUSTRALIA]);
  GEOHandler.addAfterConfig(onInacTypeChange, [SysLoc.AUSTRALIA, SysLoc.NEW_ZEALAND, SysLoc.INDIA, SysLoc.SINGAPORE, SysLoc.THAILAND]);

  // ERO specific
  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.GCG);
  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.ASEAN);
  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.ISA);

  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.ANZ);
  GEOHandler.registerValidator(addDPLCheckValidator, GEOHandler.ANZ, GEOHandler.ROLE_REQUESTER, true);
  GEOHandler.registerValidator(addDPLCheckValidator, GEOHandler.GCG, GEOHandler.ROLE_REQUESTER, true);
  GEOHandler.registerValidator(addDPLCheckValidator, GEOHandler.ASEAN, GEOHandler.ROLE_REQUESTER, true);
  GEOHandler.registerValidator(addDPLCheckValidator, GEOHandler.ISA, GEOHandler.ROLE_REQUESTER, true);

  GEOHandler.addAfterConfig(defaultCMRNumberPrefix, [SysLoc.HONG_KONG, SysLoc.MACAO, SysLoc.INDIA]);
  GEOHandler.addAfterTemplateLoad(defaultCMRNumberPrefix, [SysLoc.HONG_KONG, SysLoc.MACAO, SysLoc.INDIA]);
  GEOHandler.addAfterTemplateLoad(defaultCMRNumberPrefixforSingapore, [SysLoc.SINGAPORE]);
  // CREATCMR-7656
  GEOHandler.addAfterConfig(defaultCMRNumberPrefixforANZ, [SysLoc.NEW_ZEALAND]);
  GEOHandler.addAfterTemplateLoad(defaultCMRNumberPrefixforANZ, [SysLoc.NEW_ZEALAND]);
  GEOHandler.addAfterConfig(defaultCMRNumberPrefixforANZ, [SysLoc.AUSTRALIA]);
  GEOHandler.addAfterTemplateLoad(defaultCMRNumberPrefixforANZ, [SysLoc.AUSTRALIA]);
  // 2333
  GEOHandler.addAfterConfig(setISBUforBPscenario, GEOHandler.ASEAN);
  GEOHandler.addAfterTemplateLoad(setISBUforBPscenario, GEOHandler.ASEAN);
  GEOHandler.addAfterConfig(addVatValidationforSingapore, [SysLoc.SINGAPORE]);
  GEOHandler.registerValidator(addressNameSameValidator, [SysLoc.SINGAPORE]);
  GEOHandler.registerValidator(additionalAddrNmValidator, [SysLoc.SINGAPORE]);
  GEOHandler.registerValidator(additionalAddrNmValidatorNZ, [SysLoc.NEW_ZEALAND], null, true);
  GEOHandler.registerValidator(additionalAddrNmValidatorOldNZ, [SysLoc.NEW_ZEALAND], null, true);


  // checklist
  GEOHandler.registerValidator(addChecklistValidator, [SysLoc.VIETNAM, SysLoc.LAOS, SysLoc.CAMBODIA, SysLoc.HONG_KONG, SysLoc.SINGAPORE, SysLoc.MACAO, SysLoc.MYANMAR]);
  GEOHandler.addAfterConfig(initChecklistMainAddress, [SysLoc.VIETNAM, SysLoc.LAOS, SysLoc.CAMBODIA, SysLoc.HONG_KONG, SysLoc.SINGAPORE, SysLoc.MACAO, SysLoc.MYANMAR]);
  // Address validations
  GEOHandler.registerValidator(addSoltToAddressValidator, GEOHandler.AP);
  GEOHandler.registerValidator(addAddressInstancesValidator, GEOHandler.AP, null, true);
  GEOHandler.registerValidator(addContactInfoValidator, GEOHandler.AP, GEOHandler.REQUESTER, true);
  GEOHandler.registerValidator(similarAddrCheckValidator, GEOHandler.AP, null, true);

  GEOHandler.registerValidator(addAttachmentValidator, [SysLoc.SRI_LANKA, SysLoc.BANGLADESH, SysLoc.NEPAL, SysLoc.AUSTRALIA, SysLoc.SINGAPORE, SysLoc.NEW_ZEALAND], GEOHandler.REQUESTER, false, false);
  GEOHandler.registerValidator(addEROAttachmentValidator, [SysLoc.MACAO, SysLoc.HONG_KONG], GEOHandler.REQUESTER, false, false);

  // CREATCMR-7878
  // GEOHandler.registerValidator(setAttachmentOnCluster, [ SysLoc.INDIA,
  // SysLoc.SRI_LANKA, SysLoc.BANGLADESH], GEOHandler.REQUESTER, false, false);
  GEOHandler.registerValidator(setAttachmentOnCluster, [SysLoc.INDIA], GEOHandler.REQUESTER, false, false);

  // double creates
  GEOHandler.addAfterConfig(setFieldsForDoubleCreates, [SysLoc.INDIA, SysLoc.BANGLADESH, SysLoc.SRI_LANKA, SysLoc.VIETNAM, SysLoc.CAMBODIA]);
  GEOHandler.addAfterTemplateLoad(setFieldsForDoubleCreates, [SysLoc.INDIA, SysLoc.BANGLADESH, SysLoc.SRI_LANKA, SysLoc.VIETNAM, SysLoc.CAMBODIA]);
  GEOHandler.registerValidator(addDoubleCreateValidator, [SysLoc.INDIA, SysLoc.BANGLADESH, SysLoc.SRI_LANKA, SysLoc.VIETNAM, SysLoc.CAMBODIA], null, true);
  GEOHandler.registerValidator(addDoubleCreateValidatorSG, [SysLoc.SINGAPORE], null, true);
  // GEOHandler.addAfterConfig(setAddressDetailsForView, GEOHandler.AP);

  GEOHandler.registerValidator(addFormatFieldValidator, GEOHandler.AP, null, true);

  GEOHandler.registerValidator(addFieldFormatValidator, GEOHandler.AP, null, true);

  GEOHandler.registerValidator(addFormatForCMRNumValidator, [SysLoc.SINGAPORE], null, true);

  // CREATCMR-6398
  GEOHandler.registerValidator(businessParterValidator, [SysLoc.SINGAPORE], null, true);

  GEOHandler.addAddrFunction(lockCustMainNames, GEOHandler.AP);
  // Story - 1781935 -> AR Code for Singapore
  GEOHandler.addAddrFunction(setCollCdFrSGOnAddrSave, [SysLoc.SINGAPORE]);
  GEOHandler.addAfterTemplateLoad(setCollCdFrSingapore, [SysLoc.SINGAPORE]);
  GEOHandler.registerValidator(validateContractAddrAU, [SysLoc.AUSTRALIA], null, true);
  GEOHandler.registerValidator(validateCustNameForNonContractAddrs, [SysLoc.AUSTRALIA], null, true);
  GEOHandler.registerValidator(validateStreetAddrCont2, [SysLoc.BANGLADESH, SysLoc.BRUNEI, SysLoc.MYANMAR, SysLoc.SRI_LANKA, SysLoc.INDIA, SysLoc.INDONESIA, SysLoc.PHILIPPINES, SysLoc.SINGAPORE,
  SysLoc.VIETNAM, SysLoc.THAILAND, SysLoc.HONG_KONG, SysLoc.LAOS, SysLoc.MACAO, SysLoc.MALASIA, SysLoc.NEPAL, SysLoc.CAMBODIA], null, true);
  // CREATCMR-7589
  GEOHandler.addAfterConfig(custSubGrpHandlerINAUSG, [SysLoc.INDIA, SysLoc.AUSTRALIA, SysLoc.SINGAPORE]);
  GEOHandler.addAfterTemplateLoad(onIsicChange, [SysLoc.AUSTRALIA]);
  GEOHandler.addAfterConfig(onIsicChange, [SysLoc.AUSTRALIA]);
  GEOHandler.addAfterTemplateLoad(custSubGrpHandlerINAUSG, [SysLoc.AUSTRALIA, SysLoc.SINGAPORE]);
  GEOHandler.addAfterConfig(addHandlersForAP, GEOHandler.AP);
  GEOHandler.addAfterConfig(addHandlersForISA, GEOHandler.ISA);
  GEOHandler.addAfterConfig(addHandlersForGCG, GEOHandler.GCG);
  GEOHandler.addAfterConfig(addHandlersForANZ, GEOHandler.ANZ);
  GEOHandler.addAfterTemplateLoad(setInacByClusterHKMO, GEOHandler.GCG);
  // GEOHandler.registerValidator(validateGSTForIndia, [ SysLoc.INDIA ], null,
  // true);
  GEOHandler.registerValidator(validateABNForAU, [SysLoc.AUSTRALIA], null, true);
  GEOHandler.addAfterConfig(afterConfigForIndia, [SysLoc.INDIA]);
  GEOHandler.addAfterTemplateLoad(afterConfigForIndia, SysLoc.INDIA);
  GEOHandler.addAfterConfig(resetGstExempt, [SysLoc.INDIA]);
  GEOHandler.addAfterTemplateLoad(resetGstExempt, SysLoc.INDIA);
  GEOHandler.addAfterTemplateLoad(setCTCIsuByClusterASEAN, GEOHandler.ASEAN);
  // CREATCMR-7878
  GEOHandler.addAfterTemplateLoad(setCTCIsuByClusterISA, [SysLoc.SRI_LANKA, SysLoc.BANGLADESH]);
  GEOHandler.addAfterTemplateLoad(setMrc4IntDumForASEAN, GEOHandler.ASEAN);
  GEOHandler.addAfterConfig(lockFieldsForIndia, [SysLoc.INDIA]);
  GEOHandler.addAfterTemplateLoad(lockFieldsForIndia, SysLoc.INDIA);
  GEOHandler.addAfterConfig(lockFieldsForAU, [SysLoc.AUSTRALIA]);
  GEOHandler.addAfterTemplateLoad(lockFieldsForAU, SysLoc.AUSTRALIA);
  // GEOHandler.registerValidator(addValidatorBasedOnCluster, GEOHandler.ANZ,
  // GEOHandler.ROLE_REQUESTER, true);
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
  GEOHandler.addAfterConfig(lockFieldsForIndia, [SysLoc.INDIA]);
  GEOHandler.registerValidator(validateCustNameForInternal, [SysLoc.AUSTRALIA], null, true);
  // GEOHandler.registerValidator(clusterCdValidatorAU, [ SysLoc.AUSTRALIA ],
  // null, true);
  GEOHandler.registerValidator(addCtcObsoleteValidator, GEOHandler.AP, null, true);
  GEOHandler.addAfterConfig(lockInacCodeForIGF, [SysLoc.INDIA]);
  GEOHandler.addAfterTemplateLoad(lockInacCodeForIGF, SysLoc.INDIA);
  GEOHandler.addAfterTemplateLoad(handleObseleteExpiredDataForUpdate, GEOHandler.AP);
  GEOHandler.addAfterConfig(handleObseleteExpiredDataForUpdate, GEOHandler.AP);
  // India Handler
  // CREATCMR-6825
  GEOHandler.addAfterConfig(setRepTeamMemberNo, GEOHandler.APAC);
  GEOHandler.addAfterTemplateLoad(setRepTeamMemberNo, GEOHandler.APAC);
  GEOHandler.addAddrFunction(displayVatRegistrartionStatus, [SysLoc.SINGAPORE]);
  GEOHandler.addAfterConfig(displayVatRegistrartionStatus, [SysLoc.SINGAPORE]);
  GEOHandler.addAfterTemplateLoad(displayVatRegistrartionStatus, [SysLoc.SINGAPORE]);
  GEOHandler.addAfterConfig(addCustGrpHandler, GEOHandler.APAC_1);

  // CREATCMR-8581
  GEOHandler.registerValidator(checkCmrUpdateBeforeImport, GEOHandler.APAC_1, null, true);

  // CREATCMR-7655
  GEOHandler.registerValidator(validatNZBNForNewZeaLand, [SysLoc.NEW_ZEALAND], null, true);
  GEOHandler.addAfterConfig(afterConfigForNewZeaLand, [SysLoc.NEW_ZEALAND]);
  GEOHandler.addAfterTemplateLoad(afterConfigForNewZeaLand, SysLoc.NEW_ZEALAND);
  GEOHandler.addAfterConfig(resetNZNBExempt, [SysLoc.NEW_ZEALAND]);
  GEOHandler.addAfterTemplateLoad(resetNZNBExempt, SysLoc.NEW_ZEALAND);
  GEOHandler.addAfterTemplateLoad(onIsicChange, [SysLoc.AUSTRALIA]);
  // CREATCMR-7658
  GEOHandler.registerValidator(addValidatorforInstallingNZ, [SysLoc.NEW_ZEALAND], null, true);

  // CREATCMR-7653
  GEOHandler.registerValidator(checkNZCustomerNameTextWhenSFP, [SysLoc.NEW_ZEALAND, SysLoc.AUSTRALIA], GEOHandler.ROLE_REQUESTER, true);
  GEOHandler.registerValidator(checkNZLandedCountryForCrossWhenSFP, [SysLoc.NEW_ZEALAND], GEOHandler.ROLE_REQUESTER, true);
  GEOHandler.registerValidator(checkNZCustomerNameStartsForLocalInterDummy, [SysLoc.NEW_ZEALAND], GEOHandler.ROLE_REQUESTER, true);

  // CREATCMR-7884
  GEOHandler.registerValidator(addCovBGValidator, [SysLoc.NEW_ZEALAND], null, true);

  GEOHandler.addAfterTemplateLoad(setClusterOnScenarioChgGCG, GEOHandler.GCG);
  GEOHandler.registerValidator(validateGCGCustomerName, GEOHandler.GCG, null, true);

  // CREATCMR-7883
  // CREATCMR-7885 : add SysLoc.SINGAPORE
  // CREATCMR-7878
  GEOHandler.registerValidator(checkCustomerNameForKYND, [SysLoc.AUSTRALIA, SysLoc.MALASIA, SysLoc.INDONESIA, SysLoc.SINGAPORE, SysLoc.SRI_LANKA, SysLoc.BANGLADESH], null, true);

  GEOHandler.registerValidator(validateCustnameForKynd, [SysLoc.PHILIPPINES, SysLoc.VIETNAM, SysLoc.THAILAND], null, true);
  GEOHandler.addAfterTemplateLoad(setDefaultOnScenarioChangeTH, [SysLoc.THAILAND, SysLoc.PHILIPPINES]);
  GEOHandler.addAfterTemplateLoad(clearInacOnScenarioChange, [SysLoc.PHILIPPINES, SysLoc.VIETNAM, SysLoc.THAILAND]);
  // CREATCMR-7884 -- add NZ to clear cluster
  GEOHandler.addAfterTemplateLoad(clearClusterFieldsOnScenarioChange, [SysLoc.PHILIPPINES, SysLoc.VIETNAM, SysLoc.THAILAND, SysLoc.AUSTRALIA, SysLoc.NEW_ZEALAND, SysLoc.SINGAPORE]);
  // CREATCMR-7883
  GEOHandler.addAfterTemplateLoad(lockCMRNumberPrefixforNoINTER, [SysLoc.AUSTRALIA, SysLoc.MALASIA, SysLoc.INDONESIA, SysLoc.SINGAPORE], null, true);
  GEOHandler.registerValidator(addKyndrylValidator, [SysLoc.INDIA]);

  // CREATCMR-7884 -- lock cluster for NZ
  GEOHandler.addAfterTemplateLoad(lockClusterFieldsOnScenarioChange, [SysLoc.NEW_ZEALAND]);
  GEOHandler.registerValidator(addKyndrylValidator, [SysLoc.INDIA]);
  GEOHandler.addAfterConfig(lockFieldsWithDefaultValuesByScenarioSubType, [SysLoc.INDIA]);
  GEOHandler.addAfterTemplateLoad(lockFieldsWithDefaultValuesByScenarioSubType, [SysLoc.INDIA]);
  GEOHandler.addAfterTemplateLoad(setClusterGlcCovIdMapNrmlc, [SysLoc.INDIA]);
  GEOHandler.addAfterConfig(setClusterGlcCovIdMapNrmlc, [SysLoc.INDIA]);
  GEOHandler.registerValidator(validateRetrieveValues, [SysLoc.INDIA]);
  GEOHandler.addAfterTemplateLoad(applyClusterFilters, [SysLoc.INDIA]);
  GEOHandler.addAfterConfig(applyClusterFilters, [SysLoc.INDIA]);
  GEOHandler.addAfterTemplateLoad(applyClusterFilters, [SysLoc.INDIA]);
  GEOHandler.addAfterConfig(applyClusterFilters, [SysLoc.INDIA]);
  GEOHandler.addAfterConfig(lockFieldsWithDefaultValuesByScenarioSubType, [SysLoc.INDIA]);
  GEOHandler.addAfterTemplateLoad(lockFieldsWithDefaultValuesByScenarioSubType, [SysLoc.INDIA]);
  GEOHandler.addAfterTemplateLoad(setInacNacFieldsRequiredIN, [SysLoc.INDIA]);
  GEOHandler.addAfterTemplateLoad(prospectFilter, SysLoc.AUSTRALIA);
  GEOHandler.addAfterConfig(prospectFilter, SysLoc.AUSTRALIA);
});
