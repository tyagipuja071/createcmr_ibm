/* Register IN Javascripts refactored by Jojo Durante */
var _isicHandlerAP = null;
var _clusterHandlerAP = null;
var _isicHandlerGCG = null;
var _clusterHandlerGCG = null;
var _vatExemptHandler = null;
var _bpRelTypeHandlerGCG = null;
var _isuHandler = null;
var _inacCdHandlerIN = null;
var _importIndIN = null;
var _custSubGrpHandler = null;

function addHandlersForAP() {
  if (_isicHandlerAP == null) {
    _isicHandlerAP = dojo.connect(FormManager.getField('isicCd'), 'onChange', function (value) {
      setIsuOnIsic();
    });
  }

  if (_inacCdHandlerIN == null) {
    _inacCdHandlerIN = dojo.connect(FormManager.getField('inacCd'), 'onChange', function (value) {
      lockInacTypeForIGF();
    });
  }

  if (_clusterHandlerAP == null && FormManager.getActualValue('reqType') != 'U') {
    _clusterHandlerAP = dojo.connect(FormManager.getField('apCustClusterId'), 'onChange', function (value) {
      setInacByCluster();
      setIsuOnIsic();
    });
  }

  handleObseleteExpiredDataForUpdate();
}

function custSubGrpHandler() {
  console.log('>>>> custSubGrpHandler >>>>');
  if (_custSubGrpHandler == null) {
    _custSubGrpHandler = dojo.connect(FormManager.getField('custSubGrp'), 'onChange', function (value) {
      onIsicChange();
      prospectFilterISBU();
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
      if (dijit.byId('vatExempt') != undefined && dijit.byId('vatExempt').get('checked')) {
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

function prospectFilterISBU() {
  var ifProspect = FormManager.getActualValue('prospLegalInd');
  if (dijit.byId('prospLegalInd')) {
    ifProspect = dijit.byId('prospLegalInd').get('checked') ? 'Y' : 'N';
  }
  if (ifProspect == 'Y') {
    setISBUScenarioLogic();
    FormManager.readOnly('isicCd');
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

  if (reqType == 'U') {
    FormManager.removeValidator('vat', Validators.REQUIRED);
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

  if (role == 'REQUESTER' && reqType == 'C') {

    if (custSubGrp == "CROSS" && _pagemodel.apCustClusterId == null) {
      FormManager.setValue('apCustClusterId', "012D999");
    }

    if (custSubGrp == "KYNDR") {
      FormManager.readOnly('inacType');
    }

  }
  if (reqType == 'C') {
    onInacTypeChange();
    setInacByCluster();
    setLockIsicfromDNB();
    setIsuOnIsic();
  }


  // CREATCMR-788
  addressQuotationValidatorAP();

  // CREATCMR - 9104
  modifyBusnTypeTerrCdFieldBehaviour();
  var clusterId = FormManager.getActualValue('apCustClusterId');
  FormManager.addValidator('apCustClusterId', Validators.REQUIRED, ['Cluster'], 'MAIN_IBM_TAB');
}

function modifyBusnTypeTerrCdFieldBehaviour() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var reqId = FormManager.getActualValue('reqId');

  if (custSubGrp == 'CROSS') {
    FormManager.setValue('busnType', '709');
    FormManager.setValue('territoryCd', '709');
    FormManager.setValue('collectionCd', 'I001');
  } else if (custSubGrp == 'IGF') {
    FormManager.setValue('busnType', '000');
    FormManager.setValue('territoryCd', '000');
    FormManager.setValue('collectionCd', 'NDUM');
  } else if (custSubGrp == 'INTER') {
    FormManager.setValue('busnType', '709');
    FormManager.setValue('territoryCd', '709');
    FormManager.setValue('collectionCd', 'RP01');
  } else {
    // retrieve values from DB
    var qParam = {
      REQ_ID: reqId,
    };
    var result = cmr.query('GET.PROVNM_CD', qParam);
    if (result) {
      FormManager.setValue('busnType', result.ret1);
      FormManager.setValue('territoryCd', result.ret2);
      FormManager.setValue('collectionCd', result.ret3);
    }

  }


  if (role == 'REQUESTER') {
    FormManager.hide('ProvinceName', 'busnType');
    FormManager.hide('ProvinceCode', 'territoryCd');
    FormManager.hide('CollectionCd', 'collectionCd');
  } else {
    FormManager.show('ProvinceName', 'busnType');
    FormManager.show('ProvinceCode', 'territoryCd');
    FormManager.show('CollectionCd', 'collectionCd');
  }

}


function setInacByCluster() {
  console.log(">>>> setInacByCluster >>>>");
  if (FormManager.getActualValue('reqType') != 'C' || FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var _cluster = FormManager.getActualValue('apCustClusterId');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var custSubGrp = FormManager.getActualValue('custSubGrp');


  var _clusterIndiaMrc2 = ['05224', '04477', '04490', '04467', '05225'];
  var _clusterIndiaMrc3R = ['09062', '10193', '10194', '10195', '10196', '10197', '10198', '10199', '10200', '10201', '10202',
    '10203', '10204', '10205', '10206', '10207', '10208', '10209', '10210', '10211', '10212', '10213', '10214', '10215'];

  var _clusterIndiaMrc3NR = ['10590', '10591', '10592', '10593', '10594', '10595', '10596', '10597', '10598', '10599', '10600', '10601', '10602', '10603', '10604',
    '10605', '10606', '10607', '10608', '10609', '10610', '10611', '10612', '10613', '10614', '10615', '10616', '10617', '10618', '10619', '10620', '10621',
    '10622', '10623', '10624', '10625', '10626', '10627', '10628', '10629', '10630', '10631', '10632', '10633', '10634', '10635', '10636', '10637', '10638',
    '10639', '10640', '10641', '10642', '10643', '10644', '10645', '10654', '10655', '10656', '10657'];


  if (!_cluster) {
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
  if (_clusterIndiaMrc2.includes(_cluster) || _clusterIndiaMrc3R.includes(_cluster)) {

    if (_clusterIndiaMrc2.includes(_cluster)) {
      FormManager.addValidator('inacCd', Validators.REQUIRED, ['INAC/NAC Code'], 'MAIN_IBM_TAB');
      FormManager.addValidator('inacType', Validators.REQUIRED, ['INAC Type'], 'MAIN_IBM_TAB');
      FormManager.setValue('mrcCd', '2');
    }
    if (_clusterIndiaMrc3R.includes(_cluster)) {
      FormManager.addValidator('inacCd', Validators.REQUIRED, ['INAC/NAC Code'], 'MAIN_IBM_TAB');
      FormManager.addValidator('inacType', Validators.REQUIRED, ['INAC Type'], 'MAIN_IBM_TAB');
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

      var isInacTypeReadOnlyFromScenarios = TemplateService.isFieldReadOnly('inacType');
      if (isInacTypeReadOnlyFromScenarios) {
        FormManager.readOnly('inacType');
      } else {
        FormManager.enable('inacType');
      }
      _oldClusterSelection = _cluster;
    }

    filterInacCd('744', '10215', 'N', 'I529');

  } else {

    console.log('>>>> setInacByCluster ELSE scenario >>>>');
    if (_clusterIndiaMrc3NR.includes(_cluster)) {
      FormManager.setValue('mrcCd', '3');
    }

    FormManager.removeValidator('inacCd', Validators.REQUIRED);
    FormManager.removeValidator('inacType', Validators.REQUIRED);

    FormManager.resetDropdownValues(FormManager.getField('inacCd'));
    FormManager.resetDropdownValues(FormManager.getField('inacType'));
    clearInacOnClusterChange(_cluster);
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

function setIsuOnIsic() {
  var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var isuClusterList = ['05224', '04477', '04490', '04467', '05225'];
  var scenarioList = ['NRML', 'ESOSW', 'CROSS'];
  var _cluster = FormManager.getActualValue('apCustClusterId');
  var mrcCd = FormManager.getActualValue('mrcCd');
  var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  if (cmrIssuingCntry != '744' || !isuClusterList.includes(_cluster) || mrcCd != '2' || !scenarioList.includes(custSubGrp)) {
    return;
  }

  console.log('>>>> setIsuOnIsic >>>>');
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
}


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

function addAttachmentValidator() {
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
        var reqType = FormManager.getActualValue('reqType');
        var custSubType = FormManager.getActualValue('custSubGrp');
        var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
        // var docContent = FormManager.getActualValue('docContent');
        if (typeof (_pagemodel) != 'undefined') {
          if (reqType == 'C' && (cmrIssuingCntry == '616' && custSubType == 'ESOSW') || (cmrIssuingCntry == '834' && custSubType == 'ASLOM')) {
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
            else if (cmrIssuingCntry != '616' && cmrIssuingCntry != '834') {
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

  if (cmrIssuingCntry == '744' && (custSubGrp == 'INTER' || custSubGrp == 'XINT')) {
    FormManager.show('CmrNoPrefix', 'cmrNoPrefix');
    FormManager.setValue('cmrNoPrefix', '996---');
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
    // if (FormManager.getActualValue('viewOnlyPage') != 'true')
    // FormManager.enable('isicCd');

    if (FormManager.getActualValue('reqType') == 'C') {
      setLockIsicfromDNB();
      if (value == "KYNDR") {
        FormManager.readOnly('inacType');
      }
    }
    var cntry = FormManager.getActualValue('cmrIssuingCntry');
    var custSubGrp = FormManager.getActualValue('custSubGrp');
    var custSubGrpInDB = _pagemodel.custSubGrp;
    var abbrevNm = null;
    if (custSubGrpInDB != null && custSubGrp == custSubGrpInDB) {
      FormManager.setValue('abbrevNm', _pagemodel.abbrevNm);
      FormManager.setValue('abbrevLocn', _pagemodel.abbrevLocn);
      FormManager.setValue('isbuCd', _pagemodel.isbuCd);
      return;
    }
    setISBUScenarioLogic();
    autoSetAbbrevNmLocnLogic();
    setCollectionCd();
    lockFieldsWithDefaultValuesByScenarioSubType();
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
  filterAvailableClustersByScenarioSubType('744', ['BLUMX', 'MKTPC', 'IGF', 'PRIV', 'INTER'], ['012D999']);
  filterAvailableClustersByScenarioSubType('744', ['NRMLC'], ['10590', '10591', '10592', '10593', '10594', '10595', '10596', '10597', '10598', '10599', '10600', '10601', '10602', '10603', '10604', '10605', '10606', '10607', '10608', '10609', '10610', '10611', '10612', '10613', '10614', '10615', '10616', '10617', '10618', '10619', '10620', '10621', '10622', '10623', '10624', '10625', '10626', '10627', '10628', '10629', '10630', '10631', '10632', '10633', '10634', '10635', '10636', '10637', '10638', '10639', '10640', '10641', '10642', '10643', '10644', '10645', '012D999']);
  filterAvailableClustersByScenarioSubType('744', ['AQSTN'], ['10590', '10591', '10592', '10593', '10594', '10595', '10596', '10597', '10598', '10599', '10600', '10601', '10602', '10603', '10604', '10605', '10606', '10607', '10608', '10609', '10610', '10611', '10612', '10613', '10614', '10615', '10616', '10617', '10618', '10619', '10620', '10621', '10622', '10623', '10624', '10625', '10626', '10627', '10628', '10629', '10630', '10631', '10632', '10633', '10634', '10635', '10636', '10637', '10638', '10639', '10640', '10641', '10642', '10643', '10644', '10645', '012D999']);

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


  if (['INTER', 'IGF', 'PRIV'].includes(custSubGrp)) {
    shouldLock = true;
    shouldClear = true;
  }


  if (shouldLock) {
    FormManager.readOnly('inacCd');
    FormManager.readOnly('inacType');
  }
  if (shouldClear) {
    FormManager.setValue('inacCd', '');
    FormManager.setValue('inacType', '');
  }
}

function setInacNacFieldsRequiredIN() {
  console.log(">>>> setInacNacFieldsRequiredIN >>>>");

  if (FormManager.getActualValue('reqType') != 'C' ||
    FormManager.getActualValue('viewOnlyPage') == 'true') { return; }

  var shouldBeOptional = false;
  var cmrIssuCntry = FormManager.getActualValue('cmrIssuingCntry');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var clusterid = FormManager.getActualValue('apCustClusterId');
  var inacCd = FormManager.getActualValue('inacCd');
  var inacType = FormManager.getActualValue('inacType');


  if (['ECOSY', 'BLUMX', 'MKTPC', 'IGF', 'PRIV', 'INTER'].includes(custSubGrp)) {
    shouldBeOptional = true;
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

  /*
   * For these two scenrios, the below mentioned fields will always be locked
   * regardless of any other condition
   */
  if (['NRMLC', 'AQSTN'].includes(custSubGrp)) {
    FormManager.setValue('isuCd', '34');
    FormManager.setValue('clientTier', 'Q');
    FormManager.readOnly('apCustClusterId');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('mrcCd');
  }

  dojo.connect(FormManager.getField('custSubGrp'), 'onChange', function (value) {
    if (value == 'CROSS' && window.loadDefaultCrossSettings == true) {
      FormManager.setValue('apCustClusterId', '012D999');
      FormManager.setValue('inacCd', '');
      FormManager.setValue('inacType', '');

      window.loadDefaultCrossSettings = false;
    }
  });

  if (['KYNDR'].includes(custSubGrp)) {
    FormManager.setValue('apCustClusterId', '09062');
    FormManager.readOnly('apCustClusterId');
    FormManager.setValue('clientTier', '0');
    FormManager.readOnly('clientTier');
    FormManager.setValue('isuCd', '5K');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('mrcCd');
    FormManager.readOnly('inacType');

  } else if (['BLUMX', 'MKTPC', 'PRIV'].includes(custSubGrp)) {
    FormManager.readOnly('apCustClusterId');

    FormManager.resetDropdownValues(FormManager.getField('clientTier'));
    FormManager.setValue('clientTier', 'Z');
    FormManager.readOnly('clientTier');

    FormManager.resetDropdownValues(FormManager.getField('isuCd'));
    FormManager.setValue('isuCd', '34');
    FormManager.readOnly('isuCd');

    FormManager.setValue('mrcCd', '3');
    FormManager.readOnly('mrcCd');

    if (['PRIV'].includes(custSubGrp)) {
      FormManager.setValue('inacCd', '');
      FormManager.readOnly('inacCd');
      FormManager.setValue('inacType', '');
      FormManager.readOnly('inacType');
    }
  } else if (custSubGrp == 'IGF') {
    FormManager.resetDropdownValues(FormManager.getField('isuCd'));
    FormManager.resetDropdownValues(FormManager.getField('clientTier'));
    FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['21']);
    FormManager.setValue('isuCd', '21');
    FormManager.readOnly('isuCd');
    FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Z']);
    FormManager.setValue('clientTier', 'Z');
    FormManager.readOnly('clientTier');
    FormManager.setValue('inacCd', '');
    FormManager.readOnly('inacCd');
    FormManager.setValue('inacType', '');
    FormManager.readOnly('inacType');
  } else if (['ECOSY'].includes(custSubGrp)) {
    FormManager.setValue('mrcCd', '3');
    FormManager.readOnly('mrcCd');


  } else if (custSubGrp == 'INTER') {
    FormManager.setValue('inacCd', '');
    FormManager.setValue('inacType', '');

    FormManager.readOnly('inacCd');
    FormManager.readOnly('inacType');

    FormManager.setValue('apCustClusterId', '012D999');
    FormManager.setValue('clientTier', '0');
    FormManager.setValue('isuCd', '60');
    FormManager.setValue('mrcCd', '2');

    FormManager.readOnly('apCustClusterId');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('mrcCd');

  } else if (custSubGrp == 'CROSS') {

    clusterid = FormManager.getActualValue('apCustClusterId');

    if (_pagemodel.apCustClusterId == '012D999' || clusterid == '012D999') {
      FormManager.setValue('clientTier', 'Z');
      FormManager.readOnly('clientTier');
      FormManager.setValue('isuCd', '34');
      FormManager.readOnly('isuCd');
      FormManager.setValue('mrcCd', '3');
      FormManager.readOnly('mrcCd');
    }
  } else if (['NRMLC', 'AQSTN'].includes(custSubGrp)) {
    FormManager.setValue('isuCd', '34');
    FormManager.setValue('clientTier', 'Q');
    FormManager.setValue('mrcCd', '3');
    FormManager.readOnly('apCustClusterId');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('mrcCd');
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
      copyTypes.forEach(function (input, i) {
        if (input.value == 'ZS01' && input.checked) {
          copyingToA = true;
        }
      });
    }
    var addrType = FormManager.getActualValue('addrType');
    if (addrType == 'ZS01' || copyingToA) {
      autoSetAbbrevNmLocnLogic();

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
  console.log(">>>> onSubIndustryChange >>>>");
  var reqType = null;
  reqType = FormManager.getActualValue('reqType');
  if (reqType == 'U') {
    console.log(">>>> Exit onSubIndustChange for Update.");
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
  var reqType = null;
  reqType = FormManager.getActualValue('reqType');
  if (reqType == 'C') {
    if (_inacCdHandler == null) {
      _inacCdHandler = dojo.connect(FormManager.getField('inacType'), 'onChange', function (value) {

        var cluster = FormManager.getActualValue('apCustClusterId');
        var cmt = value + ',' + cluster + '%';
        console.log('onInacTypeChange cluster=' + cluster);
        console.log('onInacTypeChange value=' + value);
        console.log('onInacTypeChange cmt=' + cmt);
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
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

          if (qParams != undefined) {
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
            }
          }
        }

      });
    }
  }

  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (custSubGrp == "KYNDR") {
    FormManager.readOnly('inacType');
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

// var _isicHandler = null;
// function onIsicChangeHandler() {
// console.log('>>>> onIsicChangeHandler >>>>');
// if (_isicHandler == null) {
// _isicHandler = dojo.connect(FormManager.getField('custSubGrp'), 'onChange',
// function(value) {
// onIsicChange();
// });
// }
// }

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
  var cntrySet = new Set(['744', '834', '616']);

  if (reqType != 'C' && role != 'REQUESTER' && !cntrySet.has(cmrIssuingCntry)) {
    return;
  }

  if (cmrResult != '' && cmrResult == 'Accepted') {
    setIsicCdIfCmrResultAccepted(value);
  } else if (dnbResult != '' && dnbResult == 'Accepted') {
    setIsicCdIfDnbResultAccepted(value);
  } else if (cmrResult == 'No Results' || cmrResult == 'Rejected' || dnbResult == 'No Results' || dnbResult == 'Rejected') {
    setIsicCdIfDnbAndCmrResultOther(value);
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

function setIsicCdIfDnbResultAccepted(value) {
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var cond2 = new Set(['AQSTN', 'BLUMX', 'ESOSW', 'ECSYS', 'MKTPC', 'NRML', 'CROSS', 'SPOFF', 'XBLUM', 'XAQST', 'XMKTP', 'BUSPR', 'ASLOM', 'NRMLC','KYNDR']);
  var cond3 = new Set(['INTER', 'PRIV', 'XPRIV', 'DUMMY', 'IGF']);
  if (cond2.has(custSubGrp)) {
    var oldISIC = getIsicDataRDCValue();
    FormManager.setValue('isicCd', oldISIC);
    FormManager.readOnly('isicCd');
  } else if (cond3.has(custSubGrp)) {
    FormManager.setValue('isicCd', value);
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
}

function updateIndustryClass() {
  console.log(">>>> updateIndustryClass >>>>");
  var subIndustryCd = FormManager.getActualValue('subIndustryCd');
  if (subIndustryCd != null && subIndustryCd.length > 1) {
    var _industryClass = subIndustryCd.substr(0, 1);
    FormManager.setValue('IndustryClass', _industryClass);
    updateCluster(_industryClass);
  }
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
  console.log(">>>> addSectorIsbuLogicOnSubIndu >>>>");
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
  // addSectorIsbuLogicOnSubIndu();
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
      isbuList = ['BPN1', 'BPN2'];
      console.log("isbuList = " + isbuList);
      FormManager.enable('isbuCd');
      FormManager.setValue('isbuCd', '');
      FormManager.limitDropdownValues(FormManager.getField('isbuCd'), isbuList);
    }
  }


  if (FormManager.getActualValue('viewOnlyPage') == 'true') {


    FormManager.removeValidator('inacCd', Validators.REQUIRED);
    FormManager.removeValidator('inacType', Validators.REQUIRED);
  }

}





function updateProvCd() {
  console.log(">>>> updateProvCd");
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
  console.log(">>>> updateRegionCd");
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
          if ((scenario == 'LOCAL' && ((_cmrIssuingCntry == '744' && (custSubGrp == 'BLUMX' || custSubGrp == 'MKTPC' || custSubGrp == 'IGF' || custSubGrp == 'PRIV')) || ((_cmrIssuingCntry == '615' || _cmrIssuingCntry == '652') && (custSubGrp == 'BLUMX' || custSubGrp == 'MKTPC' || custSubGrp == 'IGF' || custSubGrp == 'DUMMY')))) || (_cmrIssuingCntry == '834' && _cluster == '00000' && (custSubGrp == 'BLUMX' || custSubGrp == 'MKTPC' || custSubGrp == 'DUMMY' || custSubGrp == 'PRIV' || custSubGrp == 'XBLUM' || custSubGrp == 'XMKTP' || custSubGrp == 'XDUMM' || custSubGrp == 'XPRIV' || custSubGrp == 'SPOFF')) || (_cmrIssuingCntry == '852' && _cluster == '00000' && (custSubGrp == 'DUMMY' || custSubGrp == 'XDUMM'))) {
            FormManager.resetDropdownValues(FormManager.getField('clientTier'));
            FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Q', 'Y']);
            FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['34']);
            FormManager.setValue('clientTier', 'Q');
            FormManager.setValue('isuCd', '34');
            FormManager.enable('clientTier');

            if (_cmrIssuingCntry == '744' && custSubGrp == 'IGF') {
              FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['21']);
              FormManager.setValue('isuCd', '21');
              FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Z']);
              FormManager.setValue('clientTier', 'Z');
              FormManager.readOnly('clientTier');
            }

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

          } else if (scenario == 'LOCAL' && _cmrIssuingCntry == '744' && (custSubGrp == 'AQSTN' || custSubGrp == 'NRMLC') && _cluster == '012D999') {
            FormManager.resetDropdownValues(FormManager.getField('clientTier'));
            FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Q']);
            FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['34']);
            FormManager.setValue('clientTier', 'Q');
            FormManager.setValue('isuCd', '34');
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
              }
            }





          }
        }
        if (clusterDesc[0] != '' && (clusterDesc[0].ret1.includes('S1') || clusterDesc[0].ret1.includes('IA') || clusterDesc[0].ret1.includes('S&S') || clusterDesc[0].ret1.includes('Strategic'))) {
          setIsuOnIsic();
        }
      }

    }
  });
  if (_clusterHandler && _clusterHandler[0]) {
    _clusterHandler[0].onChange();
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
                if ((custName == null || streetAddr == null || postCd == null || city == null || state == null)) {
                  mandtDetails_2++;
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
  console.log('>>>> setISUDropDownValues >>>>');
  var reqType = FormManager.getActualValue('reqType');
  if (reqType != 'C') {
    return;
  }
  var cluster = FormManager.getActualValue('apCustClusterId');
  var ctc = FormManager.getActualValue('clientTier');
  var isuCd = FormManager.getActualValue('isuCd');
  var _cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');



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
      }
      else if (apClientTierValue.length > 1 && (_cmrIssuingCntry == '744' || _cmrIssuingCntry == '615' || _cmrIssuingCntry == '652' || _cmrIssuingCntry == '834' || _cmrIssuingCntry == '852')) {
        if ((scenario == 'LOCAL' && ((_cmrIssuingCntry == '744' && (custSubGrp == 'BLUMX' || custSubGrp == 'MKTPC' || custSubGrp == 'IGF' || custSubGrp == 'PRIV')) || ((_cmrIssuingCntry == '615' || _cmrIssuingCntry == '652') && (custSubGrp == 'BLUMX' || custSubGrp == 'MKTPC' || custSubGrp == 'IGF' || custSubGrp == 'DUMMY')))) || (_cmrIssuingCntry == '834' && _cluster == '00000' && (custSubGrp == 'BLUMX' || custSubGrp == 'MKTPC' || custSubGrp == 'DUMMY' || custSubGrp == 'PRIV' || custSubGrp == 'XBLUM' || custSubGrp == 'XMKTP' || custSubGrp == 'XDUMM' || custSubGrp == 'XPRIV' || custSubGrp == 'SPOFF')) || (_cmrIssuingCntry == '852' && _cluster == '00000' && (custSubGrp == 'DUMMY' || custSubGrp == 'XDUMM'))) {
          FormManager.resetDropdownValues(FormManager.getField('clientTier'));
          FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Q', 'Y']);
          FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['34']);
          FormManager.setValue('clientTier', 'Q');
          FormManager.setValue('isuCd', '34');
          FormManager.enable('clientTier');

          if (_cmrIssuingCntry == '744' && custSubGrp == 'IGF') {
            FormManager.resetDropdownValues(FormManager.getField('clientTier'));
            FormManager.resetDropdownValues(FormManager.getField('isuCd'));
            FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Z']);
            FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['21']);
            FormManager.setValue('clientTier', 'Z');
            FormManager.setValue('isuCd', '21');
            FormManager.readOnly('clientTier');
          }
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
        } else if (scenario == 'LOCAL' && _cmrIssuingCntry == '744' && (custSubGrp == 'AQSTN' || custSubGrp == 'NRMLC') && _cluster == '012D999') {
          FormManager.resetDropdownValues(FormManager.getField('clientTier'));
          FormManager.limitDropdownValues(FormManager.getField('clientTier'), ['Q']);
          FormManager.limitDropdownValues(FormManager.getField('isuCd'), ['34']);
          FormManager.setValue('clientTier', 'Q');
          FormManager.setValue('isuCd', '34');
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
            }
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
      if (clusterDesc[0] != '' && (clusterDesc[0].ret1.includes('S1') || clusterDesc[0].ret1.includes('IA') || clusterDesc[0].ret1.includes('S&S') || clusterDesc[0].ret1.includes('Strategic'))) {
        setIsuOnIsic();
      }
    }
  }
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

// API call for validating GST for India on Save Request and Send for Processing
function validateGSTForIndia() {

  FormManager.addFormValidator((function () {
    return {
      validate: function () {
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
                  REQ_ID: reqId
                };
              }
              var results = cmr.query('GET_ZS01_GST_VALIDATE', reqParam);
              if (results.ret1 != undefined) {

                var custNm1 = results.ret1;
                var custNm2 = results.ret2;
                var addrTxt = results.ret3;
                var postal = results.ret4;
                var city = results.ret5;
                var stateProv = results.ret6;
                var landCntry = results.ret7;
                // var country = '744';

                if (stateProv != null && stateProv != '') {
                  reqParam = {
                    STATE_PROV_CD: stateProv,
                  };
                  var stateResult = cmr.query('GET_STATE_DESC', reqParam);
                  if (stateResult != null) {
                    stateProv = stateResult.ret1;
                  }
                }
                var gstRet = cmr.validateGST(cntry, vat, custNm1, custNm2, addrTxt, postal, city, stateProv, landCntry);
                var ret = cmr.query('CHECK_DNB_MATCH_ATTACHMENT', {
                  ID: reqId
                });
                if (!gstRet.success && (ret == null || ret.ret1 == null)) {
                  return new ValidationResult({
                    id: 'vat',
                    type: 'text',
                    name: 'vat'
                  }, false, gstRet.errorMessage);
                } else {
                  return new ValidationResult(null, true);
                }
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

  if (reqType == 'C' && custSubGrp == 'KYNDR') {
    FormManager.readOnly('inacType');
  }
  // CREATCMR-8755
  if (role == 'REQUESTER' && (custSubGrp == 'AQSTN' || custSubGrp == 'ESOSW' || custSubGrp == 'BLUMX' || custSubGrp == 'MKTPC' || custSubGrp == 'IGF')) {
    FormManager.readOnly('abbrevNm');
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

        if (clientTier == "T" && FormManager.getActualValue('cmrIssuingCntry') == '744') {
          console.log('>>> Skip CTC Obsolete Validator clientTier = T for IN');
          return new ValidationResult(null, true);
        }

        if (clientTier == "T" && FormManager.getActualValue('cmrIssuingCntry') == '856') {
          console.log('>>> Skip CTC Obsolete Validator clientTier = T for TH');
          return new ValidationResult(null, true);
        }

        if (reqType == 'C' && (clientTier == "4" || clientTier == "6" || clientTier == "A" || clientTier == "B" || clientTier == "M" || clientTier == "V" || clientTier == "T" || clientTier == "S" || clientTier == "N" || clientTier == "C")) {
          // CREATCMR-7884
          var cntry = FormManager.getActualValue('cmrIssuingCntry');
          if (clientTier == "T" && cntry == '796') {
            var custSubGrp = FormManager.getActualValue('custSubGrp');
            var custSubGrpList = ['NRML', 'ESOSW', 'XESO', 'CROSS'];
            if (custSubGrpList.includes(custSubGrp)) {
              console.log('>>> Skip CTC Obsolete Validator for NRML/ESOSW/XESO/CROSS when clientTier = T');
              return new ValidationResult(null, true);
            }
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
          if (clientTier == "T" && (cntry == '736' || cntry == '738')) {
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

function handleObseleteExpiredDataForUpdate() {
  console.log('>>>> handleObseleteExpiredDataForUpdate >>>>');
  var reqType = FormManager.getActualValue('reqType');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  if (reqType != 'U' || FormManager.getActualValue('viewOnlyPage') == 'true' || cntry == SysLoc.HONG_KONG || cntry == SysLoc.MACAO) {
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

function setDefaultOnScenarioChange(fromAddress, scenario, scenarioChanged) {
  var viewOnly = FormManager.getActualValue('viewOnlyPage');
  if (viewOnly != '' && viewOnly == 'true') {
    return;
  }

  if (scenarioChanged && scenario == 'KYNDR') {
    var isInacTypeReadOnlyFromScenarios = TemplateService.isFieldReadOnly('inacType');
    if (isInacTypeReadOnlyFromScenarios) {
      FormManager.readOnly('inacType');
    } else {
      FormManager.enable('inacType');
    }
  }
}

function setLockIsicfromDNB() {
  var viewOnly = FormManager.getActualValue('viewOnlyPage');
  if (viewOnly != '' && viewOnly == 'true') {
    return;
  }
  var isDnbRecord = FormManager.getActualValue('findDnbResult');
  if (isDnbRecord == 'Accepted' && FormManager.getActualValue('isicCd') != '') {
    FormManager.readOnly('isicCd');
  }
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
var _oldClusterSelection = '';
function clearInacOnClusterChange(selectedCluster) {
  var cntry = FormManager.getActualValue('cmrIssuingCntry');

  var viewOnly = FormManager.getActualValue('viewOnlyPage');
  if (viewOnly != '' && viewOnly == 'true') {
    return;
  }
  var scenario = FormManager.getActualValue('custSubGrp');
  var clearInacScenarios = '';
  var noFilterInac = '';


  if (cntry == '744') {
    clearInacScenarios = ['NRMLC', 'AQSTN', 'ESOSW', 'BLUMX', 'IGF', 'PRIV', 'CROSS', 'INTER', 'MKTPC'];
    noFilterInac = ['012D999', '10590', '10591', '10592', '10593', '10594', '10595', '10596', '10597', '10598', '10599', '10600',
      '10601', '10602', '10603', '10604', '10605', '10606', '10607', '10608', '10609', '10610', '10611', '10612', '10613', '10614', '10615',
      '10616', '10617', '10618', '10619', '10620', '10621', '10622', '10623', '10624', '10625', '10626', '10627', '10628', '10629', '10630',
      '10631', '10632', '10633', '10634', '10635', '10636', '10637', '10638', '10639', '10640', '10641', '10642', '10643', '10644', '10645',
      '10654', '10655', '10656', '10657'];
  }

  var clusterValueChanged = _oldClusterSelection != selectedCluster && cmr.currentTab == 'IBM_REQ_TAB';
  if (clusterValueChanged && clearInacScenarios.includes(scenario) && noFilterInac.includes(selectedCluster)) {
    FormManager.setValue('inacCd', '');
    FormManager.setValue('inacType', '');
    FormManager.removeValidator('inacCd', Validators.REQUIRED);

  }
  _oldClusterSelection = selectedCluster;

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

function setClusterGlcCovIdMapNrmlc() {
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var glc = FormManager.getActualValue('geoLocationCd');
  if (custSubGrp == "NRMLC" || custSubGrp == "AQSTN") {
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

function lockInacType() {
  console.log('>>>> lockInacType >>>>');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var reqType = FormManager.getActualValue('reqType');

  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    FormManager.readOnly('inacType');
    FormManager.removeValidator('inacType', Validators.REQUIRED);
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
  GEOHandler.addAfterConfig(addAfterConfigAP, GEOHandler.AP);
  GEOHandler.addAfterTemplateLoad(addAfterConfigAP, GEOHandler.AP);
  GEOHandler.addAfterConfig(saveClusterAfterSave, [SysLoc.INDIA]);
  GEOHandler.addAfterConfig(savePreviousSubScenario, [SysLoc.INDIA]);
  GEOHandler.addAfterConfig(updateIndustryClass, GEOHandler.AP);
  GEOHandler.addAfterConfig(updateProvCd, GEOHandler.AP);
  GEOHandler.addAfterConfig(updateRegionCd, GEOHandler.AP);
  GEOHandler.addAfterConfig(setCollectionCd, GEOHandler.AP, [SysLoc.AUSTRALIA, SysLoc.INDIA]);
  GEOHandler.addAfterConfig(custSubGrpHandler, [SysLoc.INDIA]);

  GEOHandler.addAfterConfig(setCollCdFrIndia, [SysLoc.INDIA]);
  GEOHandler.addAfterConfig(onSubIndustryChange, GEOHandler.AP);

  GEOHandler.addAddrFunction(handleObseleteExpiredDataForUpdate, GEOHandler.AP);
  GEOHandler.addAddrFunction(setAbbrevNmLocnOnAddressSave, GEOHandler.AP);

  GEOHandler.addAfterConfig(onCustSubGrpChange, GEOHandler.AP);
  GEOHandler.addAfterConfig(setCTCIsuByCluster, [SysLoc.INDIA]);
  GEOHandler.addAfterTemplateLoad(setCTCIsuByCluster, [SysLoc.INDIA]);

  GEOHandler.addAfterTemplateLoad(setISUDropDownValues, GEOHandler.AP);

  GEOHandler.registerValidator(addCompanyProofAttachValidation, [SysLoc.INDIA]);
  GEOHandler.registerValidator(addressNameSimilarValidator, [SysLoc.INDIA]);

  GEOHandler.addAfterConfig(defaultCMRNumberPrefix, [SysLoc.HONG_KONG, SysLoc.MACAO, SysLoc.INDIA]);
  GEOHandler.addAfterTemplateLoad(defaultCMRNumberPrefix, [SysLoc.HONG_KONG, SysLoc.MACAO, SysLoc.INDIA]);


  // Address validations
  GEOHandler.registerValidator(addSoltToAddressValidator, GEOHandler.AP);
  GEOHandler.registerValidator(addAddressInstancesValidator, GEOHandler.AP, null, true);
  GEOHandler.registerValidator(addContactInfoValidator, GEOHandler.AP, GEOHandler.REQUESTER, true);
  GEOHandler.registerValidator(similarAddrCheckValidator, GEOHandler.AP, null, true);

  GEOHandler.registerValidator(addAttachmentValidator, [SysLoc.SRI_LANKA, SysLoc.BANGLADESH, SysLoc.NEPAL, SysLoc.AUSTRALIA, SysLoc.SINGAPORE], GEOHandler.REQUESTER, false, false);
  GEOHandler.registerValidator(setAttachmentOnCluster, [SysLoc.INDIA, SysLoc.SRI_LANKA, SysLoc.BANGLADESH], GEOHandler.REQUESTER, false, false);

  // double creates
  GEOHandler.addAfterConfig(setFieldsForDoubleCreates, [SysLoc.INDIA, SysLoc.BANGLADESH, SysLoc.SRI_LANKA, SysLoc.VIETNAM, SysLoc.CAMBODIA]);
  GEOHandler.addAfterTemplateLoad(setFieldsForDoubleCreates, [SysLoc.INDIA, SysLoc.BANGLADESH, SysLoc.SRI_LANKA, SysLoc.VIETNAM, SysLoc.CAMBODIA]);
  GEOHandler.registerValidator(addDoubleCreateValidator, [SysLoc.INDIA, SysLoc.BANGLADESH, SysLoc.SRI_LANKA, SysLoc.VIETNAM, SysLoc.CAMBODIA], null, true);

  GEOHandler.registerValidator(addFormatFieldValidator, GEOHandler.AP, null, true);

  GEOHandler.registerValidator(addFieldFormatValidator, GEOHandler.AP, null, true);



  GEOHandler.addAddrFunction(lockCustMainNames, GEOHandler.AP);

  GEOHandler.registerValidator(validateStreetAddrCont2, [SysLoc.BANGLADESH, SysLoc.BRUNEI, SysLoc.MYANMAR, SysLoc.SRI_LANKA, SysLoc.INDIA, SysLoc.INDONESIA, SysLoc.PHILIPPINES, SysLoc.SINGAPORE,
  SysLoc.VIETNAM, SysLoc.THAILAND, SysLoc.HONG_KONG, SysLoc.LAOS, SysLoc.MACAO, SysLoc.MALASIA, SysLoc.NEPAL, SysLoc.CAMBODIA], null, true);
  // CREATCMR-7589
  // GEOHandler.addAfterConfig(onIsicChangeHandler, [SysLoc.INDIA,
  // SysLoc.AUSTRALIA, SysLoc.SINGAPORE ]);
  GEOHandler.addAfterConfig(onIsicChange, [SysLoc.INDIA, SysLoc.AUSTRALIA, SysLoc.SINGAPORE]);
  GEOHandler.addAfterTemplateLoad(onIsicChange, [SysLoc.INDIA, SysLoc.AUSTRALIA, SysLoc.SINGAPORE]);

  GEOHandler.addAfterConfig(addHandlersForAP, GEOHandler.AP);


  GEOHandler.registerValidator(validateGSTForIndia, [SysLoc.INDIA], null, true);

  GEOHandler.addAfterTemplateLoad(afterConfigForIndia, SysLoc.INDIA);
  GEOHandler.addAfterConfig(resetGstExempt, [SysLoc.INDIA]);
  GEOHandler.addAfterTemplateLoad(resetGstExempt, SysLoc.INDIA);

  GEOHandler.addAfterConfig(lockFieldsForIndia, [SysLoc.INDIA]);
  GEOHandler.addAfterTemplateLoad(lockFieldsForIndia, SysLoc.INDIA);


  GEOHandler.registerValidator(addValidatorBasedOnCluster, GEOHandler.ASEAN, GEOHandler.ROLE_REQUESTER, true);

  GEOHandler.registerValidator(addCtcObsoleteValidator, GEOHandler.AP, null, true);

  GEOHandler.addAfterConfig(lockInacCodeForIGF, [SysLoc.INDIA]);
  GEOHandler.addAfterTemplateLoad(lockInacCodeForIGF, SysLoc.INDIA);
  GEOHandler.addAfterTemplateLoad(handleObseleteExpiredDataForUpdate, GEOHandler.AP);
  GEOHandler.addAfterConfig(handleObseleteExpiredDataForUpdate, GEOHandler.AP);
  // India Handler
  // CREATCMR-6825
  GEOHandler.addAfterConfig(setRepTeamMemberNo, GEOHandler.APAC);
  GEOHandler.addAfterTemplateLoad(setRepTeamMemberNo, GEOHandler.APAC);
  GEOHandler.addAfterConfig(addCustGrpHandler, GEOHandler.APAC_1);

  GEOHandler.registerValidator(addKyndrylValidator, [SysLoc.INDIA]);
  GEOHandler.addAfterTemplateLoad(setClusterGlcCovIdMapNrmlc, [SysLoc.INDIA]);
  GEOHandler.addAfterConfig(setClusterGlcCovIdMapNrmlc, [SysLoc.INDIA]);
  GEOHandler.registerValidator(validateRetrieveValues, [SysLoc.INDIA]);
  GEOHandler.addAfterTemplateLoad(applyClusterFilters, [SysLoc.INDIA]);
  GEOHandler.addAfterTemplateLoad(setInacNacFieldsRequiredIN, [SysLoc.INDIA]);
  GEOHandler.addAfterTemplateLoad(lockInacNacFieldsByScenarioSubType, [SysLoc.INDIA]);
  GEOHandler.addAfterTemplateLoad(setDefaultOnScenarioChange, [SysLoc.INDIA]);
  GEOHandler.addAfterConfig(lockInacType, [SysLoc.INDIA]);

});
