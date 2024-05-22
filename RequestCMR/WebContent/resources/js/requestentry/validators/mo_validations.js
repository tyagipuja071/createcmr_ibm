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
var _inacCdHandler = null;
var _importIndIN = null;
var _vatRegisterHandlerSG = null;
var _clusterHandlerINDONESIA = 0;
var _inacHandlerANZSG = 0;
var oldClusterCd = null;
var _searchTermHandler = null;
var _custSubGrpHandler = null;
var _inacTypeHandler = null;

function addHandlersForAP() {
  console.log(">>> Add Handlers for AP <<<");
  if (_isicHandlerAP == null) {
    _isicHandlerAP = dojo.connect(FormManager.getField('isicCd'), 'onChange', function (value) {
      setIsuOnIsic();
    });
  }

  if (_isuHandler == null) {
    _isuHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function (value) {
    });
  }

  if (_searchTermHandler == null) {
    _searchTermHandler = dojo.connect(FormManager.getField('apCustClusterId'), 'onChange', function (value) {
      console.log(">>> RUNNING AP CLUSTER ID HANDLER!!!!");
      if (!value) {
        return;
      }
      filterISUOnChangeMO();
      setISUAndCTCForDefaultApClusterIdMO();
      setInacByClusterHKMO();

    });
  }

  if (_custSubGrpHandler == null) {
    _custSubGrpHandler = dojo.connect(FormManager.getField('custSubGrp'), 'onChange', function (value) {
      console.log(">>> RUNNING CUST SUB GRP HANDLER!!!!");
      if (!value) {
        return;
      }
      filterISUOnChangeMO();
      setISUAndCTCForDefaultApClusterIdMO();
      setInacByClusterHKMO();

    });
  }

  handleObseleteExpiredDataForUpdate();
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
  var inacCd = FormManager.getActualValue('inacCd');
  var inacType = FormManager.getActualValue('inacType');
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

  if (reqType == 'C' && (custSubGrp && custSubGrp != '') && custSubGrp != 'INTER' && custSubGrp != 'XINT' && cntry == '736') {
    FormManager.setValue('repTeamMemberNo', _pagemodel.repTeamMemberNo == null ? '000000' : _pagemodel.repTeamMemberNo);
    FormManager.enable('repTeamMemberNo');
  }

  if (reqType == 'C' && custType == 'LOCAL') {
    FormManager.readOnly('stateProv');
    FormManager.readOnly('postCd');
  } else {
    if (reqType != 'C' && custType != 'LOCAL' && cntry == '736') {
      FormManager.enable('postCd');
    }
  }

  if (reqType == 'U') {
    FormManager.readOnly('postCd');
  }

  var clusterId = FormManager.getActualValue('apCustClusterId');

  if (reqType == 'C' && custGrp == 'CROSS' && cntry == '736') {
    FormManager.enable('postCd');
  }


  FormManager.readOnly('cmrNo');

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
    /*
     * if (cntry == SysLoc.NEW_ZEALAND) { if (custSubGrp == 'INTER' ||
     * custSubGrp == 'XINT' || custSubGrp == 'DUMMY' || custSubGrp == 'XDUMM' ||
     * custSubGrp == 'BLUMX' || custSubGrp == 'XBLUM' || custSubGrp == 'MKTPC' ||
     * custSubGrp == 'XMKTP') FormManager.readOnly('clientTier'); else
     * FormManager.enable('clientTier'); }
     */
    if ((cntry == SysLoc.NEW_ZEALAND || cntry == SysLoc.AUSTRALIA || cntry == SysLoc.INDONESIA || cntry == SysLoc.PHILIPPINES || cntry == SysLoc.THAILAND || cntry == SysLoc.MALASIA) && (custSubGrp == 'ECSYS' || custSubGrp == 'XECO')) {
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


  }
  if (reqType == 'C') {
    setIsuOnIsic();
    // CREATCMR-7883-7884
    _inacHandlerANZSG = 0;
    // setInacByCluster();
  }

  // CREATCMR-5269
  if (reqType == 'U') {
    handleObseleteExpiredDataForUpdate();
  }


  // CREATCMR-788
  addressQuotationValidatorAP();

}

function saveClusterVal() {
  console.log(">>>> saveClusterVal");
  if (oldClusterCd == null) {
    oldClusterCd = FormManager.getActualValue('apCustClusterId');
  }
}

function filterISUOnChangeMO() {
  if (FormManager.getActualValue('reqType') != 'C' || FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  console.log(">>> RUNNING filterISUOnChangeMO!!");
  var searchTerm = FormManager.getActualValue('apCustClusterId');

  var searchTermParams = {
    SORTL: searchTerm,
    KATR6: '736'
  };

  var isuCdResult = cmr.query('GET.MAPPED_ISU_BY_SORTL', searchTermParams);

  if (isuCdResult != null && isuCdResult.ret2 != '') {
    FormManager.resetDropdownValues(FormManager.getField('isuCd'));
    FormManager.setValue('isuCd', isuCdResult.ret2);
  } else {
    FormManager.enable('isuCd');
  }

  searchTermParams['_qall'] = 'Y';
  var clientTier = null;

  var mappedCtc = cmr.query('GET.MAPPED_CTC_BY_ISU', searchTermParams);
  if (mappedCtc && mappedCtc.length > 0) {
    clientTier = [];
    mappedCtc.forEach(function (ctc, index) {
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

function setISUAndCTCForDefaultApClusterIdMO() {
  const cluster = FormManager.getActualValue('apCustClusterId');
  if (cluster != '00000') {
    return;
  }

  const custSubGrp = FormManager.getActualValue('custSubGrp');
  const custSubGrpIsu21 = ['DUMMY', 'INTER'];

  if (custSubGrpIsu21.includes(custSubGrp)) {
    FormManager.setValue('isuCd', '21');
    FormManager.setValue('clientTier', 'Z')
  } else {
    FormManager.setValue('isuCd', '34');
    FormManager.setValue('clientTier', 'Z');
  }
  FormManager.readOnly('isuCd');
}


function setInacByClusterHKMO() {
  console.log('>>>> setInacByClusterHKMO >>>>');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var _cluster = FormManager.getActualValue('apCustClusterId');
  var HKClusterList = ['09059', '10175', '10176', '10177', '10178'];
  var MOClusterList = ['09207'];
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

  }
}

function filterInacCdBasedInacTypeChange() {
  console.log(">>> onInacTypeChange <<<")
  var searchTerm = FormManager.getActualValue('searchTerm');
  var reqType = null;

  reqType = FormManager.getActualValue('reqType');
  if (reqType == 'C') {
    var custSubT = FormManager.getActualValue('custSubGrp');
    if (_inacCdHandler == null) {
      _inacCdHandler = dojo.connect(FormManager.getField('inacType'), 'onChange', function (value) {

        if (value != null) {
          var searchTerm = FormManager.getActualValue('apCustClusterId');
          var _clusterTWWithAllInac = ['09207'];

          var cmt = value + ',' + searchTerm + '%';
          var cntry = FormManager.getActualValue('cmrIssuingCntry');
          var inacCdValue = [];
          var qParams = {
            _qall: 'Y',
            ISSUING_CNTRY: cntry,
            CMT: cmt,
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

  FormManager.limitDropdownValues(FormManager.getField('inacCd'), ids, true);
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

/* ASEAN ANZ GCG ISIC MAPPING */
function setIsuOnIsic() {
  if (FormManager.getActualValue('cmrIssuingCntry') == '834') {
    return;
  }
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
          // var reqType = FormManager.getActualValue('reqType');
          // var role = FormManager.getActualValue('userRole').toUpperCase();

          // if(role = 'REQUESTER' || role == 'PROCESSOR'){

          // if (city1 && city1.length > 0 &&
          // !city1.match("([0-9]{15})|^(X{3})$|^(x{3})$")) {
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


function addrSeqFormatter(value, rowIndex) {
  var rowData = this.grid.getItem(rowIndex);
  var curAddrSeq = rowData.addrSeq[0];
  var importInd = rowData.importInd[0];
  var validSeq = ["A", "B", "C", "D", "E"];
  var reqType = FormManager.getActualValue('reqType');
  var newAddressInUpdate = ('U' == reqType && importInd == 'N') ;
  if ((reqType == 'C' || newAddressInUpdate ) && !validSeq.includes(curAddrSeq)) {
    return 'N/A';
  }
  return value;
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
  } else if (role == 'PROCESSOR') {
    FormManager.enable('isbuCd');
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


    var custSubGrp = FormManager.getActualValue('custSubGrp');
    var custSubGrpInDB = _pagemodel.custSubGrp;
    if (custSubGrpInDB != null && custSubGrp == custSubGrpInDB) {
      FormManager.setValue('abbrevNm', _pagemodel.abbrevNm);
      FormManager.setValue('abbrevLocn', _pagemodel.abbrevLocn);
      FormManager.setValue('isbuCd', _pagemodel.isbuCd);
      return;
    }

    autoSetAbbrevNmLocnLogic();
    setCollectionCd();

    // CREATCMR-7885
    // CREATCMR-7878

    resetFieldsAfterCustSubGrpChange();

  });
}

function resetFieldsAfterCustSubGrpChange() {
  console.log(">>>> resetInacNacCdAfterCustSubGrpChange >>>>");
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  if ((cntry == '616' || cntry == '834') && custSubGrp == 'KYNDR') {
    FormManager.setValue('isuCd', '5K');
  }
  // else {
  // FormManager.setValue('isuCd', '');
  // FormManager.setValue('inacType', '');
  // FormManager.setValue('inacCd', '');
  // }
}



function setCollectionCd() {
  console.log('>>>> setCollectionCd >>>>');
  var cmrIssuCntry = FormManager.getActualValue('cmrIssuingCntry');
  var custGrp = FormManager.getActualValue('custGrp');
  var role = FormManager.getActualValue('userRole').toUpperCase();

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
          // if (cmrIssuCntry != countryCd && !(cmrIssuCntry == '615'
          // ||cmrIssuCntry ==
          // '652' ||cmrIssuCntry == '744' ||cmrIssuCntry == '790'
          // ||cmrIssuCntry == '736'
          // || cmrIssuCntry == '738' || cmrIssuCntry == '790')) {
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
      setKUKLAvaluesMO();
    }
  });
  if (_subIndCdHandler && _subIndCdHandler[0]) {
    _subIndCdHandler[0].onChange();
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
  var cond2 = new Set(['AQSTN', 'BLUMX', 'ESOSW', 'ECSYS', 'MKTPC', 'NRML', 'CROSS', 'SPOFF', 'XBLUM', 'XAQST', 'XMKTP', 'BUSPR', 'ASLOM', 'NRMLC', 'KYNDR']);
  var cond3 = new Set(['INTER', 'PRIV', 'XPRIV', 'DUMMY', 'IGF']);
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
  // if (dplCheck == 'AF' && isicCd != null && isicCd != undefined && isicCd !=
  // '') {
  // FormManager.readOnly('isicCd');
  // } else {
  // FormManager.enable('isicCd');
  // }
}

function setPrivate() {
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (custSubGrp == 'XAQST' || custSubGrp == 'XMKTP' || custSubGrp == 'XBLUM') {
    FormManager.setValue('custSubGrp', 'XPRIV');
  } else {
    FormManager.setValue('custSubGrp', 'PRIV');
  }
}

function setIsic() {
  FormManager.setValue('isicCd', '');
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
  if (_mrcCd == '3' && _industryClass != '') {
    _isbuCd = 'GMB' + _industryClass;
    FormManager.setValue('isbuCd', _isbuCd);
  } else if (_mrcCd == '2' && _sectorCd != '' && _industryClass != '') {
    _isbuCd = _sectorCd + _industryClass;
    FormManager.setValue('isbuCd', _isbuCd);
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
  if (FormManager.getActualValue('cmrIssuingCntry') == '834') {
    return;
  }
  console.log('>>>> setCTCIsuByCluster >>>>');
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var issuingCntries = ['852', '818', '856', '643', '778', '749', '616', '796', '736', '738', '834'];
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

function setIsuByClusterCTC() {
  if (FormManager.getActualValue('cmrIssuingCntry') == '834') {
    return;
  }
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
                  if ((cmrCntry == SysLoc.MACAO || cmrCntry == SysLoc.HONG_KONG) && (record.addrType == 'ZP01' || record.addrType == 'ZP02' || record.addrType == 'MAIL' || record.addrType == 'ZD01')) {
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
            if (typeof (city) == 'object') {
              city = city[0];
            }
            if (typeof (state) == 'object') {
              state = state[0];
            }
          }
          if (mandtDetails > 0) {
            return new ValidationResult(null, false, "Customer Name  is required, Street Address is required");
          } else if (mandtDetails_1 > 0) {
            return new ValidationResult(null, false, "Customer Name  is required, Street Address is required, City is required.");
          } else if (mandtDetails_2 > 0) {
            return new ValidationResult(null, false, "Customer Name  is required, Street Address is required, , City is required and State is required.");
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
/*
 * Story 1734332 : Customer Name & Customer Name Con't in Address tab need to be
 * set as non-editable for processors
 */
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
  if (FormManager.getActualValue('cmrIssuingCntry') == '834') {
    return;
  }
  console.log('>>>> setISUDropDownValues >>>>');
  var reqType = FormManager.getActualValue('reqType');
  if (reqType != 'C') {
    return;
  }
  var cluster = FormManager.getActualValue('apCustClusterId');
  var ctc = FormManager.getActualValue('clientTier');
  var isuCd = FormManager.getActualValue('isuCd');
  var _cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');


  var _cluster = FormManager.getActualValue('apCustClusterId');
  var scenario = FormManager.getActualValue('custGrp');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var issuingCntries = ['852', '818', '856', '643', '778', '749'];

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

      if (apClientTierValue.length == 1) {
        FormManager.limitDropdownValues(FormManager.getField('clientTier'), apClientTierValue);
        FormManager.limitDropdownValues(FormManager.getField('isuCd'), isuCdValue);
        FormManager.setValue('clientTier', apClientTierValue[0]);
        FormManager.setValue('isuCd', isuCdValue[0]);
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
        var streetAddrCont2 = FormManager.getActualValue('city1');

        if (streetAddrCont1 == '' && streetAddrCont2 != '') {
          return new ValidationResult(null, false, 'Street Address Con\'t2 cannot have a value as Street Address Con\'t1 is blank.');
        }

        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
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

function handleObseleteExpiredDataForUpdate() {
  console.log('>>>> handleObseleteExpiredDataForUpdate >>>>');
  var reqType = FormManager.getActualValue('reqType');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  if (reqType != 'U' || FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (reqType == 'U' && cntry == SysLoc.MACAO) {
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
            // if (typeof (type) == 'object') {
            // updateInd = updateInd[0];
            // }
            name = name1 + name2;
            // if(((reqType == 'U' && (updateInd == 'U' || updateInd ==
            // 'N')) || reqType == 'C') && type != 'ZS01' && zs01Name !=
            // name){
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
          // todo: remove
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
  FormManager.clearValue('reqReason');
  cmr.showAlert('This Request reason can be chosen only if imported record has 88 embargo code.');
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

function setFieldToReadyOnly() {
  var role = FormManager.getActualValue('userRole').toUpperCase();

  if (role == 'REQUESTER') {
    FormManager.readOnly('custClass');
  }
}

function setKuklaAfterConfigMO() {
  if (_bpRelTypeHandlerGCG == null && FormManager.getActualValue('reqType') != 'U') {
    _bpRelTypeHandlerGCG = dojo.connect(FormManager.getField('bpRelType'), 'onChange', function (value) {
      setKUKLAvaluesMO();
    });
  }
}

function setKUKLAvaluesMO() {
  var reqType = FormManager.getActualValue('reqType');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var industryClass = FormManager.getActualValue('IndustryClass');
  var bpRelType = FormManager.getActualValue('bpRelType');
  var custSubGrp = FormManager.getActualValue('custSubGrp');

  if (FormManager.getActualValue('reqType') == 'U') {
    return
  }

  console.log('setKUKLAvaluesMO() >>>> set KUKLA values for MO >>>>');

  var cond1 = new Set(['AQSTN', 'ECOSY', 'ASLOM', 'KYND', 'MKTPC', 'NRML', 'CROSS']);
  var cond2 = new Set(['DUMMY', 'INTER']);

  var kuklaMO = [];
  if (reqType == 'C') {
    var qParams = {
      _qall: 'Y',
      ISSUING_CNTRY: cntry,
    };
    var results = cmr.query('GET.HK_MO_KUKLA', qParams);
    if (results != null) {
      for (var i = 0; i < results.length; i++) {
        kuklaMO.push(results[i].ret1);
      }
    }

    if (results != null) {
      if (cond1.has(custSubGrp)) {
        if ((industryClass == 'G' || industryClass == 'H' || industryClass == 'Y')) {
          FormManager.setValue('custClass', kuklaMO[1]);
        } else if (industryClass == 'E') {
          FormManager.setValue('custClass', kuklaMO[2]);
        } else {
          FormManager.setValue('custClass', kuklaMO[0]);
        }
      } else if (custSubGrp == 'BUSPR') {
        if (bpRelType == 'DS') {
          FormManager.setValue('custClass', kuklaMO[5]);
        } else if (bpRelType == 'SP') {
          FormManager.setValue('custClass', kuklaMO[3]);
        } else if (bpRelType == 'RS') {
          FormManager.setValue('custClass', kuklaMO[4]);
        }
      } else if (cond2.has(custSubGrp)) {
        FormManager.setValue('custClass', kuklaMO[7]);
      } else if (custSubGrp == 'BLUMX') {
        FormManager.setValue('custClass', kuklaMO[6]);
      }
    }
  }
}

function onChangeMOlandCntryStateProvPostCd() {
  var _landCntryHandler = dojo.connect(FormManager.getField('landCntry'), 'onChange', function (value) {
    var landCntry = FormManager.getActualValue('landCntry');
    var custGrp = FormManager.getActualValue('custGrp');
    if (landCntry == 'MO') {
      FormManager.readOnly('stateProv');
      FormManager.readOnly('postCd');
    } else {
      FormManager.enable('stateProv');
      FormManager.enable('postCd');
    }
  });
  if (_landCntryHandler && _landCntryHandler[0]) {
    _landCntryHandler[0].onChange();
  }
}


function afterConfigMO() {
  addAfterConfigAP();
  addHandlersForAP();
  updateIndustryClass();
  updateProvCd();
  updateRegionCd();
  setCollectionCd();
  onSubIndustryChange();
  handleObseleteExpiredDataForUpdate();
  setAbbrvNameBPScen();
  setCtcOnIsuCdChangeGCG();
  reqReasonHandler();
  defaultCMRNumberPrefix();
  filterInacCdBasedInacTypeChange();
  setKuklaAfterConfigMO();
  onChangeMOlandCntryStateProvPostCd();
}

function afterTemplateLoadMO() {
  updateIndustryClass();
  updateProvCd();
  updateRegionCd();
  setCollectionCd();
  onSubIndustryChange();
  addCustGrpHandler();
  handleObseleteExpiredDataForUpdate();
  setAbbrvNameBPScen();
  setCtcOnIsuCdChangeGCG();
  defaultCMRNumberPrefix();
  initChecklistMainAddress();
  onChangeMOlandCntryStateProvPostCd();
}

dojo.addOnLoad(function () {
  GEOHandler.AP = [SysLoc.MACAO];
  GEOHandler.GCG = [SysLoc.MACAO];
  GEOHandler.APAC_1 = [SysLoc.MACAO];
  GEOHandler.APAC = [SysLoc.MACAO];
  GEOHandler.setRevertIsicBehavior(false);
  GEOHandler.registerValidator(addSalesRepNameNoCntryValidator, [SysLoc.MACAO]);
  GEOHandler.enableCopyAddress(SysLoc.MACAO);
  GEOHandler.addAfterConfig(afterConfigMO, SysLoc.MACAO);
  GEOHandler.addAfterTemplateLoad(afterTemplateLoadMO, SysLoc.MACAO);
  GEOHandler.enableCustomerNamesOnAddress(SysLoc.MACAO);
  GEOHandler.addAddrFunction(updateMainCustomerNames, SysLoc.MACAO);
  GEOHandler.addAddrFunction(handleObseleteExpiredDataForUpdate, SysLoc.MACAO);
  GEOHandler.addAddrFunction(setAbbrevNmLocnOnAddressSave, SysLoc.MACAO);
  GEOHandler.addAddrFunction(lockCustMainNames, SysLoc.MACAO);
  GEOHandler.registerValidator(validateGCGCustomerName, GEOHandler.GCG, null, true);
  GEOHandler.registerValidator(checkCmrUpdateBeforeImport, GEOHandler.APAC_1, null, true);
  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.GCG);
  GEOHandler.registerValidator(addSoltToAddressValidator, GEOHandler.AP);
  GEOHandler.registerValidator(addAddressInstancesValidator, GEOHandler.AP, null, true);
  GEOHandler.registerValidator(addContactInfoValidator, GEOHandler.AP, GEOHandler.REQUESTER, true);
  GEOHandler.registerValidator(similarAddrCheckValidator, GEOHandler.AP, null, true);
  GEOHandler.registerValidator(addFormatFieldValidator, GEOHandler.AP, null, true);
  GEOHandler.registerValidator(addFieldFormatValidator, GEOHandler.AP, null, true);
  GEOHandler.registerValidator(addChecklistValidator, [SysLoc.MACAO]);
  GEOHandler.registerValidator(addEROAttachmentValidator, [SysLoc.MACAO], GEOHandler.REQUESTER, false, false);
  GEOHandler.registerValidator(validateStreetAddrCont2, [SysLoc.MACAO], null, true);
  GEOHandler.registerValidator(validateGCGCustomerName, GEOHandler.GCG, null, true);

  GEOHandler.addAfterTemplateLoad(setFieldToReadyOnly, SysLoc.MACAO);
  GEOHandler.addAfterConfig(setFieldToReadyOnly, SysLoc.MACAO);
});
