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
var _isuCdHandler = null;

function addHandlersForAU() {
  if (_isicHandlerAP == null) {
    _isicHandlerAP = dojo.connect(FormManager.getField('isicCd'), 'onChange', function (value) {
      setIsuOnIsic();
    });
  }
  
  if (_clusterHandlerANZ == null && FormManager.getActualValue('reqType') != 'U') {
    _inacHandlerANZSG = 0;
    _clusterHandlerANZ = dojo.connect(FormManager.getField('apCustClusterId'), 'onChange', function (value) {
      lockFieldsForAU();
      _inacHandlerANZSG = _inacHandlerANZSG + 1;
      setIsuOnIsic();
// if (_inacHandlerANZSG > 1) {
        setInacByCluster(value);
// }
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
  
  console.log('>>>> onCustSubGrpChange >>>>');
  if (_isuCdHandler == null) {
    _isuCdHandler = dojo.connect(FormManager.getField('custSubGrp'), 'onChange', function (value) {
    console.log('custSubGrp CHANGED here >>>>');
    if (FormManager.getActualValue('reqType') == 'U') {
      return;
    }
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
  });
  }
  
  _subIndCdHandler = dojo.connect(FormManager.getField('subIndustryCd'), 'onChange', function (value) {
    console.log('>>>> onSubIndustryChange >>>>');
    var reqType = null;
    reqType = FormManager.getActualValue('reqType');
    if (reqType == 'U') {
      console.log('>>>> Exit onSubIndustChange for Update.');
      return;
    }
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
  
  handleObseleteExpiredDataForUpdate();
}

function addAfterConfigAP() {
  console.log('>>>> addAfterConfigAP >>>>');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var reqType = FormManager.getActualValue('reqType');
  var custType = FormManager.getActualValue('custGrp');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var custGrp = FormManager.getActualValue('custGrp');
  var reqReason = FormManager.getActualValue('reqReason');
  _clusterHandlerINDONESIA = 0;
  _inacHandlerANZSG = 0;
  if (reqType == 'U' && reqReason!='PAYG') {
    FormManager.removeValidator('vat', Validators.REQUIRED);
  }
  FormManager.removeValidator('cmrNo', Validators.REQUIRED);

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
// FormManager.readOnly('abbrevLocn');
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

  if (cntry == '616' && custSubGrp == 'KYNDR') {
    if (role == 'REQUESTER' || role == 'VIEWER' || role == 'PROCESSOR') {
      FormManager.readOnly('apCustClusterId');
      FormManager.readOnly('clientTier');
      FormManager.readOnly('apCustClusterId');
      FormManager.setValue('isuCd', '5K');
    }
  }
  
  var clusterId = FormManager.getActualValue('apCustClusterId');
  
  FormManager.readOnly('cmrNo');

  if (role == 'REQUESTER' && reqType == 'C') {
    if ((cntry == SysLoc.NEW_ZEALAND || cntry == SysLoc.AUSTRALIA || cntry == SysLoc.INDONESIA || cntry == SysLoc.PHILIPPINES || cntry == SysLoc.SINGAPORE || cntry == SysLoc.THAILAND || cntry == SysLoc.MALASIA) && (custSubGrp == 'ECSYS' || custSubGrp == 'XECO')) {
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
    onInacTypeChange();
    _inacHandlerANZSG = 0;
  }
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

function setInacByCluster(searchTermChange) {
  var _cluster = FormManager.getActualValue('apCustClusterId');
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
          for (var i=0; i<inacCodes.length; i++) {
            actualInacList.push(inacCodes[i].id[0]);
          }
          FormManager.limitDropdownValues(FormManager.getField('inacCd'), actualInacList);
        } else if (FormManager.getActualValue('inacType') == 'N') {
          var actualNacList = [];
          for (var i=0; i<nacCodes.length; i++) {
            actualNacList.push(nacCodes[i].id[0]);
          }
          FormManager.limitDropdownValues(FormManager.getField('inacCd'), actualNacList);
        }
      }
      FormManager.clearValue('inacCd');
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
        setInacByCluster("inacChange");
      });
    }
  }
  var custSubGrpList = ['AQSTN', 'BLUMX', 'MKTPC', 'IGF', 'PRIV', 'NRMLC', 'MKTPC', 'ECOSY', 'ECOSW', 'ESOSY', 'INTER', 'KYNDR', 'CROSS'];
  if (!custSubGrpList.includes(custSubGrp) && cntry == SysLoc.INDIA) {
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
  FormManager.setValue('custClass', sKukla);
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
      }

      return;
    }

    if (value != null && value.length > 1) {
      updateIsbuCd();
    }
  });
  if (_isuHandler && _isuHandler[0]) {
    _isuHandler[0].onChange();
  }
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


function setCTCIsuByClusterANZ() {
  console.log('>>>> setCTCIsuByClusterANZ >>>>');
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var scenario = FormManager.getActualValue('custGrp');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
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
      if (_cluster != '' && _cluster != '') {
        var qParams = {
          _qall: 'Y',
          ISSUING_CNTRY: _cmrIssuingCntry,
          CLUSTER: _cluster,
        };

        var isuValRetrieved = false;
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
            FormManager.readOnly('isuCd');
            if (isuCdValue.length == 1 && isuCdValue[0].trim().length > 0) {
              FormManager.limitDropdownValues(FormManager.getField('isuCd'), isuCdValue);
              FormManager.setValue('isuCd', isuCdValue[0]);
              isuValRetrieved = true;
            }
          } else if (apClientTierValue.length > 1) {
            FormManager.resetDropdownValues(FormManager.getField('clientTier'));
            FormManager.limitDropdownValues(FormManager.getField('clientTier'), apClientTierValue);
            FormManager.limitDropdownValues(FormManager.getField('isuCd'), isuCdValue);
            isuValRetrieved = true;
          }
        }
        if (!isuValRetrieved){          
          setIsuOnIsic();
        }
      }

  });
  if (_clusterHandler && _clusterHandler[0]) {
    _clusterHandler[0].onChange();
  }
}

function addAbnValidatorForAU() {
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
        var abn = FormManager.getActualValue('vat');
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        /*
         * if (custSubGrp == "AQSTN" || custSubGrp == "XAQST" || custSubGrp ==
         * "IGF" || custSubGrp == "XIGF" || custSubGrp == "NRML" || custSubGrp ==
         * "XNRML" || custSubGrp == "SOFT" || custSubGrp == "XSOFT") {
         */
        if (abn && abn.length != 11) {
          return new ValidationResult({
            id: 'vat',
            type: 'text',
            name: 'ABN#'
          }, false, 'The length of ABN# should be exactly 11.');
        } else {
          return new ValidationResult(null, true);
        }
        // }

        /*
         * else { return new ValidationResult(null, true); }
         */
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
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

    // var result = null;
    // var zs01ReqId = FormManager.getActualValue('reqId');
    // var qParams = {
    // REQ_ID : zs01ReqId,
    // };
    // if(cmr.addressMode == 'newAddress' || cmr.addressMode ==
    // 'copyAddress'){
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
        var reqType = FormManager.getActualValue('reqType');
        var zs01ReqId = FormManager.getActualValue('reqId');
        var addrType = FormManager.getActualValue('addrType');
        qParams = {
          REQ_ID: zs01ReqId,
        };
        var record = cmr.query('GETZS01VALRECORDS', qParams);
        var zs01Reccount = record.ret1;
        if (addrType == 'ZS01' && Number(zs01Reccount) == 1 && cmr.addressMode != 'updateAddress' && reqType != 'U') {
          return new ValidationResult(null, false, 'Only one Sold-To Address can be defined.');
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function addBillToAddressValidator() {
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
        var zs01ReqId = FormManager.getActualValue('reqId');
        var addrType = FormManager.getActualValue('addrType');
        var reqType=FormManager.getActualValue('reqType');
        qParams = {
          REQ_ID: zs01ReqId,
        };
        var record = cmr.query('GETZP01VALRECORDS', qParams);
        var zp01Reccount = record.ret1;
        if (addrType == 'ZP01' && Number(zp01Reccount) >= 2 && cmr.addressMode != 'updateAddress' && reqType!='U') {
          return new ValidationResult(null, false, 'Only two Bill-To Addresses can be defined.');
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function addInstallAtAddressValidator() {
  FormManager.addFormValidator((function () {
    return {
      validate: function () {
        var zs01ReqId = FormManager.getActualValue('reqId');
        var addrType = FormManager.getActualValue('addrType');
        var reqType=FormManager.getActualValue('reqType');
        qParams = {
          REQ_ID: zs01ReqId,
        };
        var record = cmr.query('GETZI01VALRECORDS', qParams);
        var zi01Reccount = record.ret1;
        if (addrType == 'ZI01' && Number(zi01Reccount) >= 3 && cmr.addressMode != 'updateAddress' && reqType!='U') {
          return new ValidationResult(null, false, 'Only three Install-At Addresses can be defined.');
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
        var reqType1 = FormManager.getActualValue('reqType');
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount == 0 && reqType1 != 'U') {
          return new ValidationResult(null, false, 'One Sold-To Address is mandatory');
        }
        return new ValidationResult(null, true);
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
              // FormManager.readOnly('mrcCd');
            } else {
              FormManager.enable('clientTier');
              FormManager.enable('isuCd');
              FormManager.enable('inacType');
              FormManager.enable('inacCd');
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
    }
  }
  // setIsuOnIsic();
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
        
        var zs01CustName = '';
        
        if (contractCustNm != undefined && contractCustNm.ret1 != undefined && contractCustNm.ret2) {
          zs01CustName = contractCustNm.ret1.toUpperCase() + contractCustNm.ret2.toUpperCase();
        }

        if (zs01CustName != '' && zs01CustName != custNm && addrType != "ZS01" && _pagemodel.reqType == 'U') {
          return new ValidationResult(null, false, 'customer name of additional address must be the same as the customer name of contract address');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function validateABNForAU() {

  FormManager.addFormValidator((function () {
    return {
      validate: function () {
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        var reqTyp = FormManager.getActualValue('reqType');
        var vat = FormManager.getActualValue('vat');
        var reqReason = FormManager.getActualValue('reqReason');
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
    // FormManager.readOnly('mrcCd');
    FormManager.readOnly('inacType');
    FormManager.readOnly('isuCd');
    FormManager.readOnly('inacCd');
    FormManager.readOnly('repTeamMemberNo');
    FormManager.readOnly('repTeamMemberName');
    FormManager.readOnly('isbuCd');
    FormManager.readOnly('covId');
    FormManager.readOnly('cmrNoPrefix');
    
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
    // FormManager.removeValidator('mrcCd', Validators.REQUIRED);
    FormManager.removeValidator('inacType', Validators.REQUIRED);
    FormManager.removeValidator('inacCd', Validators.REQUIRED);
    FormManager.removeValidator('repTeamMemberNo', Validators.REQUIRED);
    FormManager.removeValidator('repTeamMemberName', Validators.REQUIRED);
    FormManager.removeValidator('isbuCd', Validators.REQUIRED);
    FormManager.removeValidator('covId', Validators.REQUIRED);
    
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

var _oldClusterSelection = '';

/* Tentatively to be removed */
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
    // else if(!custSubGrpListSg.includes(custSubGrp)){
    // cmt = '%'+ cluster +'%';
    // console.log('setInacCdTypeStatus() >>>> cmt='+cmt);
    // }

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

    if (scenarioChanged && clearClusterFieldsScenarios.includes(scenario)) {
      FormManager.setValue('apCustClusterId', '');
      FormManager.setValue('clientTier', '');
      FormManager.setValue('isuCd', '');
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
    // CREATCMR-7883-7884

    // if (isInacRequired) {
    // console.log('add REQUIRED of INAC TYPE/CODE for SG/834 >>>>');
    // FormManager.addValidator('inacCd', Validators.REQUIRED, ['INAC/NAC
    // Code'],
    // 'MAIN_IBM_TAB');
    // FormManager.addValidator('inacType', Validators.REQUIRED, ['INAC Type'],
    // 'MAIN_IBM_TAB');
    // }
    // LOCK GB Seg(QTC)/ISU
    FormManager.readOnly('clientTier');
    FormManager.readOnly('isuCd');
  }
}

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

// function setMrcCd() {
// if (FormManager.getActualValue('mrcCd') == '3') {
// return;
// }
// FormManager.setValue('mrcCd', '3');
// }

function addAfterConfigAU() {
  updateIndustryClass();
  updateProvCd();
  updateRegionCd();
  setCollCdFrAU();
  onIsuCdChangeAseanAnzIsa();
  // setCTCIsuByClusterANZ();
  removeStateValidatorForHkMoNZ();
  addGovIndcHanlder();
  addGovCustTypHanlder();
  defaultCMRNumberPrefixforANZ();
  prospectFilter();
  addCustGrpHandler();
  defaultCMRNumberPrefixforANZ();
  custSubGrpHandlerINAUSG();
  addHandlersForAU();
  handleObseleteExpiredDataForUpdate();
  setRepTeamMemberNo();
  // setMrcCd();
}

function addressFunctions() {
  setAbbrevNmLocnOnAddressSave();
  handleObseleteExpiredDataForUpdate();
  updateMainCustomerNames();
  lockCustMainNames();
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

function afterTemplateLoadFunctions() {
  setRepTeamMemberNo();
  // clearClusterFieldsOnScenarioChange();
  lockCMRNumberPrefixforNoINTER();
  prospectFilter();
  defaultCMRNumberPrefixforANZ();
  custSubGrpHandlerINAUSG();
  lockFieldsForAU();
  handleObseleteExpiredDataForUpdate();
  setISUDropDownValues();
  setCTCIsuByClusterANZ();
}

dojo.addOnLoad(function () {

  GEOHandler.setRevertIsicBehavior(false);
  GEOHandler.enableCustomerNamesOnAddress([SysLoc.AUSTRALIA]);
  GEOHandler.enableCopyAddress([SysLoc.AUSTRALIA]);

  GEOHandler.addAfterConfig(addHandlersForAU, [SysLoc.AUSTRALIA]);
  GEOHandler.addAfterConfig(addAfterConfigAU, [SysLoc.AUSTRALIA]);
  GEOHandler.addAfterConfig(addAfterConfigAP, [SysLoc.AUSTRALIA]);

  GEOHandler.addAfterTemplateLoad(afterTemplateLoadFunctions, [SysLoc.AUSTRALIA]);

  GEOHandler.addAddrFunction(addressFunctions, [SysLoc.AUSTRALIA]);

  GEOHandler.registerValidator(addSalesRepNameNoCntryValidator, [SysLoc.AUSTRALIA]);
  GEOHandler.registerValidator(addAbnValidatorForAU, [SysLoc.AUSTRALIA]);
  GEOHandler.registerValidator(addFailedDPLValidator, [SysLoc.AUSTRALIA]);
  GEOHandler.registerValidator(addFailedDPLValidator, [SysLoc.AUSTRALIA]);
  GEOHandler.registerValidator(addDPLCheckValidator, [SysLoc.AUSTRALIA], GEOHandler.ROLE_REQUESTER, true);
  GEOHandler.registerValidator(addSoltToAddressValidator, [SysLoc.AUSTRALIA]);
  // GEOHandler.registerValidator(addBillToAddressValidator,
  // [SysLoc.AUSTRALIA]);
  // GEOHandler.registerValidator(addInstallAtAddressValidator,
  // [SysLoc.AUSTRALIA]);

  GEOHandler.registerValidator(addAddressInstancesValidator, [SysLoc.AUSTRALIA], null, true);
  GEOHandler.registerValidator(addContactInfoValidator, [SysLoc.AUSTRALIA], GEOHandler.REQUESTER, true);
  GEOHandler.registerValidator(similarAddrCheckValidator, [SysLoc.AUSTRALIA], null, true);
  GEOHandler.registerValidator(addAttachmentValidator, [SysLoc.AUSTRALIA], GEOHandler.REQUESTER, false, false);
  GEOHandler.registerValidator(addFormatFieldValidator, [SysLoc.AUSTRALIA], null, true);
  GEOHandler.registerValidator(addFieldFormatValidator, [SysLoc.AUSTRALIA], null, true);
  GEOHandler.registerValidator(validateContractAddrAU, [SysLoc.AUSTRALIA], null, true);
  GEOHandler.registerValidator(validateCustNameForNonContractAddrs, [SysLoc.AUSTRALIA], null, true);
  GEOHandler.registerValidator(validateABNForAU, [SysLoc.AUSTRALIA], null, true);
  GEOHandler.registerValidator(validateCustNameForInternal, [SysLoc.AUSTRALIA], null, true);
  GEOHandler.registerValidator(addCtcObsoleteValidator, [SysLoc.AUSTRALIA], null, true);
  GEOHandler.registerValidator(checkCmrUpdateBeforeImport, [SysLoc.AUSTRALIA], null, true);
  GEOHandler.registerValidator(checkNZCustomerNameTextWhenSFP, [SysLoc.AUSTRALIA], GEOHandler.ROLE_REQUESTER, true);
  GEOHandler.registerValidator(checkCustomerNameForKYND, [SysLoc.AUSTRALIA], null, true);

});
