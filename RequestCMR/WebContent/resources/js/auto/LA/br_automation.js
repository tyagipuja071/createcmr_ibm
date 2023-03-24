function restrictScenarios() {
  if (!FilteringDropdown.pending()) {
    console.log('limiting main scenarios..');
    var custGrp = FormManager.getField('custGrp');
    if (custGrp) {
      FormManager.limitDropdownValues(custGrp, [ 'LOCAL' ]);
      FormManager.setValue('custGrp', 'LOCAL');
      FormManager.readOnly('custGrp');
      window.setTimeout(restrictSubScenarios, 500);
    }
  } else {
    window.setTimeout(restrictScenarios, 500);
  }
}

function restrictSubScenarios() {
  if (!FilteringDropdown.pending()) {
    console.log('limiting sub scenarios..');
    var custSubGrp = FormManager.getField('custSubGrp');
    if (custSubGrp) {
      FormManager.limitDropdownValues(custSubGrp, [ 'BUSPR', 'CC3CC', 'COMME', 'GOVDI', 'GOVIN', 'INTER', 'LEASI' ]);
    }
  } else {
    window.setTimeout(restrictSubScenarios, 500);
  }
}

var _scenarioHandler = null;
function afterConfigForBRV2() {
  window.setTimeout(restrictScenarios, 500);

  var reqType = FormManager.getActualValue('reqType');

  if (reqType == 'C') {
    var prompt = 'Are you creating a request for any of the following client/customer types?';
    prompt += '<br>';
    // commented as a fix for defect CMR - 688
    // prompt += '<br>SaaS/PaaS (Private Person or Company)';
    // prompt += '<br>Softlayer';
    prompt += '<br>Private Person';
    prompt += '<br>IBM Employee';
    prompt += '<br>Cross-Border';
    prompt += '<br>Convert Prospect CMR to Legal';
    // prompt += '<br>(placeholder for new scenario)';
    cmr.showConfirm(null, prompt, 'Client Type', 'Automation.goToOldRequestPage()', {
      OK : 'No',
      CANCEL : 'Yes'
    });
  }
  FormManager.hide('EndUserVAT', 'vatEndUser');
  FormManager.hide('EndUserFiscalCode', 'municipalFiscalCodeEndUser');

  if (_scenarioHandler == null && FormManager.getField('custSubGrp')) {
    _scenarioHandler = dojo.connect(FormManager.getField('custSubGrp'), 'onChange', function(value) {
      var val = FormManager.getActualValue('custSubGrp');
      if (val != 'LEASI') {
        FormManager.hide('EndUserVAT', 'vatEndUser');
        FormManager.hide('EndUserFiscalCode', 'municipalFiscalCodeEndUser');
        FormManager.removeValidator('vatEndUser', Validators.REQUIRED);
        FormManager.removeValidator('municipalFiscalCodeEndUser', Validators.REQUIRED);
        FormManager.clearValue('vatEndUser');
        FormManager.clearValue('municipalFiscalCodeEndUser');

      } else {
        FormManager.show('EndUserVAT', 'vatEndUser');
        FormManager.mandatory('vatEndUser', 'EndUserVAT', 'MAIN_GENERAL_TAB');
        FormManager.show('EndUserFiscalCode', 'municipalFiscalCodeEndUser');
        FormManager.mandatory('municipalFiscalCodeEndUser', 'EndUserFiscalCode', 'MAIN_GENERAL_TAB');
      }

      if (val == 'CC3CC') {
        FormManager.setValue('email1', 'bcibmnfe@br.ibm.com');
        FormManager.readOnly('email1');
        FormManager.mandatory('email2', 'Email2');
      } else if (val != 'CC3CC' && FormManager.getActualValue('email1') == 'bcibmnfe@br.ibm.com') {
        FormManager.clearValue('email1');
        FormManager.enable('email1');
        FormManager.removeValidator('email2', Validators.REQUIRED);
      }

      if (val == 'GOVDI' || val == 'GOVIN') {
        FormManager.show('GovernmentType', 'govType');
        FormManager.mandatory('govType', 'GovernmentType', 'MAIN_GENERAL_TAB');
        var govType = FormManager.getField('govType');
        if (govType) {
          FormManager.limitDropdownValues(govType, [ '', 'CM', 'PF', 'PM', 'PE' ]);
        }
      } else {
        var govType = FormManager.getField('govType');
        if (govType) {
          FormManager.limitDropdownValues(govType, [ '', 'OU', 'CM', 'PF', 'PM', 'PE' ]);
        }
        FormManager.setValue('govType', 'OU');
        FormManager.hide('GovernmentType', 'govType');
      }

      if (val == 'BUSPR') {
        // check for requester id bluegroup
        checkForBlueGroup(val);
      }
    });
  }
  FormManager.mandatory('email1', 'Email1');
  if (_pagemodel.reqType == 'C') {
    cmr.hideNode('updReason');
    FormManager.removeValidator('updateReason', Validators.REQUIRED);
    FormManager.mandatory('reqReason', 'RequestReason');
  } else {
    FormManager.readOnly('vat');
    FormManager.mandatory('reqReason', 'RequestReason');
    FormManager.mandatory('updateReason', 'Update Reason');
  }

  // digit validator
  FormManager.addValidator('vat', Validators.DIGIT, [ 'Vat' ]);
  FormManager.addValidator('vatEndUser', Validators.DIGIT, [ 'Vat End User' ]);
  FormManager.addValidator('municipalFiscalCode', Validators.ALPHANUM, [ 'Tax Code 2/Municipal Fiscal Code' ]);

  FormManager.hide('GovernmentType', 'govType');
  GEOHandler.registerValidator(registerGovernmentValidator, [ '631' ]);

  var val = FormManager.getActualValue('updateReason');
  if (val == 'UPIC') {
    FormManager.setValue('reqReason', 'UPIC');
    FormManager.readOnly('reqReason');

    FormManager.show('Company', 'company');
    FormManager.show('INACCode', 'inacCd');
    FormManager.show('ISU', 'isuCd');
    FormManager.show('CollectorNameNo', 'collectorNameNo');
    FormManager.show('SalesBusOff', 'salesBusOffCd');
  } else {
    FormManager.hide('Company', 'company');
    FormManager.hide('INACCode', 'inacCd');
    FormManager.hide('ISU', 'isuCd');
    FormManager.hide('CollectorNameNo', 'collectorNameNo');
    FormManager.hide('SalesBusOff', 'salesBusOffCd');
  }
}

var _updateReason1Handler = null;
var _updateReason2Handler = null;
var _updateReason3Handler = null;
var _updateReason4Handler = null;

function handleUpdateReason() {
  if (_updateReason1Handler == null) {
    _updateReason1Handler = dojo.connect(FormManager.getField('updateReason1'), 'onChange', function(value) {
      var val = FormManager.getActualValue('updateReason');
      if (val == 'AUCO') {
        FormManager.setValue('reqReason', 'AUCO');
        FormManager.readOnly('reqReason');
      }
    });
  }

  if (_updateReason2Handler == null) {
    _updateReasonHandler = dojo.connect(FormManager.getField('updateReason2'), 'onChange', function(value) {
      var val = FormManager.getActualValue('updateReason');
      if (val == 'UPCI') {
        FormManager.enable('reqReason');
        if ((_kukla == 33 || _kukla == 34 || _kukla == 35) && _vat == _vatEndUser) {
          FormManager.enable('vatEndUser');
        }
      } else {
        if (_vatEndUser != null && _vatEndUser != '') {
          FormManager.setValue('vatEndUser', _vatEndUser);
          FormManager.readOnly('vatEndUser');
        }
      }
    });
  }

  if (_updateReason3Handler == null) {
    _updateReasonHandler = dojo.connect(FormManager.getField('updateReason3'), 'onChange', function(value) {
      var val = FormManager.getActualValue('updateReason');
      if (val == 'REAC') {
        FormManager.setValue('reqReason', 'REAC');
        FormManager.readOnly('reqReason');
      }
    });
  }

  if (_updateReason4Handler == null) {
    _updateReasonHandler = dojo.connect(FormManager.getField('updateReason4'), 'onChange', function(value) {
      var val = FormManager.getActualValue('updateReason');
      if (val == 'UPIC') {
        FormManager.setValue('reqReason', 'UPIC');
        FormManager.readOnly('reqReason');

        FormManager.show('Company', 'company');
        FormManager.show('INACCode', 'inacCd');
        FormManager.show('ISU', 'isuCd');
        FormManager.show('CollectorNameNo', 'collectorNameNo');
        FormManager.show('SalesBusOff', 'salesBusOffCd');
      } else {
        FormManager.hide('Company', 'company');
        FormManager.hide('INACCode', 'inacCd');
        FormManager.hide('ISU', 'isuCd');
        FormManager.hide('CollectorNameNo', 'collectorNameNo');
        FormManager.hide('SalesBusOff', 'salesBusOffCd');
      }

    });
  }
}

function verifyRequestReasonUpdate() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var updateReason = FormManager.getActualValue('updateReason');
        var reqReason = FormManager.getActualValue('reqReason');
        if (updateReason == 'UPCI' && (reqReason == 'AUCO' || reqReason == 'REAC')) {
          return new ValidationResult(null, false, 'For Update Reason \'Update Company Information\', the Request Reason cannot be \'Add/Update Contacts Only\' or \'CMR Reactivation\'.');
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_GENERAL_TAB', 'frmCMR');
}

function registerGovernmentValidator() {
  FormManager.addValidator('govType', function(input) {
    var scenario = FormManager.getActualValue('custSubGrp');
    var govType = FormManager.getActualValue('govType');
    if (scenario == 'GOVDI' || scenario == 'GOVIN') {
      if (govType == 'OU') {
        return new ValidationResult(input, false, 'Please select a valid government type apart from OTHER.');
      }
    }
    return new ValidationResult(input, true);
  }, [ 'Government' ]);
}

function emailValidation() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var email1 = FormManager.getActualValue('email1');
        var email2 = FormManager.getActualValue('email2');
        var email3 = FormManager.getActualValue('email3');
        if (email1 || email2 || email3) {
          var mailFormatRegEx = /^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w+)+$/;

          var email1Res = (email1 != null && email1 != '') ? mailFormatRegEx.test(email1) : true;
          var email2Res = (email2 != null && email2 != '') ? mailFormatRegEx.test(email2) : true;
          var email3Res = (email3 != null && email3 != '') ? mailFormatRegEx.test(email3) : true;

          if (email1Res && email2Res && email3Res) {
            return new ValidationResult(null, true);
          } else {
            return new ValidationResult(null, false, 'Please enter valid format for email.');
          }
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_GENERAL_TAB', 'frmCMR');
}

var _vatEndUser = '';
var _vat = '';
var _kukla = '';
var _cmrNo = '';
var _imported = false;
var _company = '';
var _inacCd = '';
var _isuCd = '';
var _collectorNo = '';
var _salesBoCd = '';

function doImport() {
  var cmrNo = FormManager.getActualValue('cmrNo');
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  if (cmrNo == '' || cntry == '') {
    cmr.showAlert('Please input the both CMR Issuing Country and CMR Number to search for.');
    return;
  }

  cmr.showProgress('Importing values for CMR No. ' + cmrNo + ' into request..');

  var qParams = {
    ZZKV_CUSNO : cmrNo,
    MANDT : cmr.MANDT,
    KATR6 : cntry,
    KTOKD : 'ZS01'
  };
  console.log(qParams);
  var result = cmr.query('BR.AUTO.GET_UPDATE_INFO', qParams);
  if (result.ret1 != null) {
    resetFields();
    _vat = result.ret2;
    _kukla = result.ret1;
    _aufsd = result.ret4;
    _collectorNo = result.ret8;
    // validate for leasing
    if (_kukla == '33' || _kukla == '34' || _kukla == '35') {
      FormManager.setValue('custClass', _kukla);
      FormManager.show('EndUserVAT', 'vatEndUser');
      FormManager.mandatory('vatEndUser', 'EndUserVAT', 'MAIN_GENERAL_TAB');
      FormManager.show('EndUserFiscalCode', 'municipalFiscalCodeEndUser');
      FormManager.mandatory('municipalFiscalCodeEndUser', 'EndUserFiscalCode', 'MAIN_GENERAL_TAB');
      var result2 = cmr.query('BR.AUTO.GET_UPDATE_INFO', {
        ZZKV_CUSNO : cmrNo,
        MANDT : cmr.MANDT,
        KATR6 : cntry,
        KTOKD : 'ZI01'
      });
      if (result2.ret2 != null) {
        _vatEndUser = result2.ret2;
        if (FormManager.getActualValue('updateReason') == 'UPCI' && _vat == _vatEndUser) {
          FormManager.enable('vatEndUser');
        } else {
          FormManager.readOnly('vatEndUser');
        }
      }
    } else if (result.ret3.includes('SOFTLAYER')) {
      FormManager.enable('vat');
    } else if (result.ret3.includes('/CC3')) {
      FormManager.readOnly('email1');
    }
    dojo.xhrGet({
      url : cmr.CONTEXT_ROOT + '/getBRData.json',
      handleAs : 'json',
      method : 'GET',
      content : {
        'cmrNo' : cmrNo,
        'issuingCntry' : cntry
      },
      timeout : 50000,
      sync : false,
      load : function(data, ioargs) {
        cmr.hideProgress();
        console.log(data);
        if (!data.success) {
          cmr.showAlert('An error was encountered during BR CMR data retrieval.', 'Error');
          resetFields();
        } else {

          if (_aufsd == 93) {
            FormManager.setValue('updateReason', 'REAC');
            FormManager.readOnly('updateReason');
          }

          var record = data;

          // set RDC values
          FormManager.setValue('vat', _vat);

          // fiscal codes
          if (record.muniFiscalCodeLE != null) {
            FormManager.setValue('municipalFiscalCode', record.muniFiscalCodeLE)
          }
          if (_kukla == '33' || _kukla == '34' || _kukla == '35') {
            FormManager.setValue('vatEndUser', _vatEndUser);
            if (record.muniFiscalCodeUS != null) {
              FormManager.setValue('municipalFiscalCodeEndUser', record.muniFiscalCodeUS);
            }
          }

          FormManager.setValue('email1', record.email1);
          FormManager.setValue('email2', record.email2);
          FormManager.setValue('email3', record.email3);
          FormManager.setValue('taxCode', record.taxSepInd);
          FormManager.setValue('proxiLocnNo', record.proxiLocnNo);

          FormManager.setValue('company', record.company);
          FormManager.setValue('inacCd', record.inacCd);
          FormManager.setValue('isuCd', record.isuCd);
          FormManager.setValue('collectorNameNo', record.collectorNameNo);
          FormManager.setValue('salesBusOffCd', record.salesBusOffCd);

          _imported = true;
          _cmrNo = cmrNo;
        }
      },
      error : function(error, ioargs) {
        cmr.hideProgress();
        cmr.showAlert('An error occurred while importing the record. Please contact your system administrator', 'Error');
      }
    });

  } else {
    cmr.hideProgress();
    cmr.showAlert('No records were found corresponding to CMR No. ' + cmrNo, 'Error');
  }
}

function retrieveCMRData() {
  if (_imported) {
    cmr.showConfirm('checkAndImport()', 'Results from a previous CMR Search have already been accepted for this request. Importing will overwrite existing information. Continue importing the CMR?',
        null, 'cancelImport()', {
          OK : 'Yes',
          CANCEL : 'Cancel'
        });
  } else {
    checkAndImport();
  }
}

function checkAndImport() {
  var cmrNo = FormManager.getActualValue('cmrNo');
  if (cmrNo == '' || cmrNo == null) {
    cmr.showAlert('Please specify a valid CMR Number.');
    return;
  }
  var qParams = {
    ZZKV_CUSNO : cmrNo,
    MANDT : cmr.MANDT
  };
  var result = cmr.query('BR.CHECK_ACTIVE_CMR', qParams);
  console.log(result);
  if (result.ret1 != '1') {
    cmr.showConfirm('doImport()', 'Record with CMR Number ' + cmrNo
        + ' is a Deactivated CMR. The Request will be set to convert it into a Reactivated CMR. Any current data on the request will also be replaced. Proceed?', null, 'cancelImport()', {
      OK : 'Yes',
      CANCEL : 'Cancel'
    });
  } else {
    doImport();
  }
}

function resetFields() {
  _vat = '';
  _vatEndUser = '';
  _cmrNo = '';
  _kukla = '';
  _aufsd = '';
  _imported = false;
  FormManager.clearValue('email1');
  FormManager.clearValue('email2');
  FormManager.clearValue('email3');
  FormManager.clearValue('taxCode');
  FormManager.clearValue('vat');
  FormManager.clearValue('vatEndUser');
  FormManager.clearValue('municipalFiscalCode');
  FormManager.clearValue('municipalFiscalCodeEndUser');
  FormManager.clearValue('custClass');
  FormManager.readOnly('vat');
  FormManager.hide('EndUserVAT', 'vatEndUser');
  FormManager.hide('EndUserFiscalCode', 'municipalFiscalCodeEndUser');
  FormManager.removeValidator('vatEndUser', Validators.REQUIRED);
  FormManager.removeValidator('municipalFiscalCodeEndUser', Validators.REQUIRED);
  FormManager.enable('email1');
  FormManager.clearValue('updateReason');
  FormManager.enable('updateReason');
  FormManager.clearValue('reqReason');
  FormManager.enable('reqReason');
}

function cancelImport() {
  FormManager.setValue('cmrNo', _cmrNo);
}

function createRequestForCountry(formName) {
  // execute the AJAX chaining here before save
  console.log('executing Brazil save..');
  if (_pagemodel.reqType == 'C') {
    showModalDupReq(frmCMR).then(function(data) {
      showModalDupCMR(formName).then(function() {
        Automation.submitForCreation(formName);
      });
    });
  } else if (_pagemodel.reqType == 'U') {
    if (_imported) {

      if (FormManager.getActualValue('updateReason') == 'REAC' && _aufsd != '93') {
        cmr.showAlert('The imported CMR is already active. Please import an inactive CMR to the request for reactivation or change the Update Reason', 'Error');
      } else if (FormManager.getActualValue('updateReason') == 'REAC' && _aufsd == '93') {
        showModalDupReq(frmCMR).then(function(data) {
          showModalDupCMR(formName).then(function() {
            Automation.submitForCreation(formName);
          });
        });
      } else {
        Automation.submitForCreation(formName);
      }

    } else {
      cmr.showAlert('No CMR imported into the request. Please import a CMR to the request before proceeding for update.', 'Error');
    }
  }
}

function showModalDupCMR(frmCMR) {
  cmr.hideProgress();
  cmr.showProgress('Checking for duplicate CMRs..');
  var vat = FormManager.getActualValue('vat');
  var vatzi01 = FormManager.getActualValue('vatEndUser');
  return new Promise(function(resolve, reject) {
    dojo.xhrPost({
      url : cmr.CONTEXT_ROOT + '/auto/duplicate/cmrcheck.json',
      handleAs : 'json',
      method : 'POST',
      content : {
        'issuingCountry' : FormManager.getActualValue('cmrIssuingCntry'),
        'vat' : vat,
        'vatzi01' : vatzi01,
        'subscenario' : FormManager.getActualValue('custSubGrp')
      },
      timeout : 300000,
      sync : false,
      load : function(data, ioargs) {
        cmr.hideProgress();
        console.log(data);
        if (!data.success) {
          console.log('No Matches found, proceeding with duplicate request search...');
          resolve(frmCMR);
        } else {
          // when duplicate requests have been found successfully show them in
          // gird.
          cmr.showModal('dupCMR_modal');
          CmrGrid.refresh('dupCMRMatchGrid', cmr.CONTEXT_ROOT + '/auto/duplicate/cmrslist.json');
          reject('Duplicate CMR Found');
        }
      },
      error : function(error, ioargs) {
        success = false;
        cmr.hideProgress();
        cmr.showAlert('An error occurred while searching for duplicate CMRs. Please contact your system administrator', 'Error');
        reject('Error occurred in duplicate CMR check');
      }
    });
  });
}

function showModalDupReq(frmCMR) {
  cmr.showProgress('Checking for duplicate requests..');
  var vat = FormManager.getActualValue('vat');
  var vatzi01 = FormManager.getActualValue('vatEndUser');
  return new Promise(function(resolve, reject) {
    dojo.xhrPost({
      url : cmr.CONTEXT_ROOT + '/auto/duplicate/reqcheck.json',
      handleAs : 'json',
      method : 'POST',
      content : {
        'issuingCountry' : FormManager.getActualValue('cmrIssuingCntry'),
        'vat' : vat,
        'landedCountry' : "BR",
        'matchType' : 'V',
        'city' : "XXXX",
        'subscenario' : FormManager.getActualValue('custSubGrp'),
        'vatzi01' : vatzi01,
        'streetLine1' : "XXXX",
        'customerName' : "XXXX"
      },
      timeout : 300000,
      sync : false,
      load : function(data, ioargs) {
        cmr.hideProgress();
        console.log(data);
        if (!data.success) {
          console.log('No Matches found, proceeding with request creation...');
          resolve(frmCMR);
        } else {
          // when duplicate cmrs have been found successfully show them in gird.
          cmr.showModal('dupReq_modal');
          CmrGrid.refresh('dupReqMatchGrid', cmr.CONTEXT_ROOT + '/auto/duplicate/reqslist.json');
          reject('Duplicate Requests found');
        }
      },
      error : function(error, ioargs) {
        cmr.hideProgress();
        cmr.showAlert('An error occurred while searching for duplicate requests. Please contact your system administrator', 'Error');
        reject('An error occured while checking for duplicate requests.');
      }
    });
  });
}

function checkForBlueGroup(subScenario) {
  console.log("Checking if the requester exists in Bluegroup since Business partner is selected...");
  cmr.showProgress("Checking for user's existence in Bluegroup...");
  dojo
      .xhrGet({
        url : cmr.CONTEXT_ROOT + '/auto/br_bp/bluegroups/check.json',
        handleAs : 'json',
        method : 'GET',
        content : {
          'subscenario' : subScenario
        },
        timeout : 50000,
        sync : false,
        load : function(data, ioargs) {
          cmr.hideProgress();
          console.log(data);
          if (!data.success) {
            cmr.showAlert("This user is not allowed to submit BP request. Please contact Luciana Costa Romanetto/Brazil/IBM of Q2C Operations - LA BPCM.", 'Error',
                "FormManager.clearValue('custSubGrp')");
          } else {
            // continue
            console.log("User exists in BR_BP_BLUEGROUP.");
          }
        },
        error : function(error, ioargs) {
          cmr.hideProgress();
          // cmr.showAlert('An error occurred while adding to notification
          // list.',
          // 'Error','cmr.showModal("dupReq_modal")');
        }
      });

}

function closeDupCMRChkModal() {
  cmr.hideModal('dupCMR_modal');
  window.location = './autoreq?cmrIssuingCntry=631&reqType=C';
}

function addToNotifList(reqId) {
  console.log("Additng user to the notifiction list of request id -> " + reqId + "...");
  cmr.showProgress('Adding user to the notifiction list of request id -> ' + reqId + '...');
  dojo.xhrGet({
    url : cmr.CONTEXT_ROOT + '/auto/duplicate/reqcheck/notifList.json',
    handleAs : 'json',
    method : 'GET',
    content : {
      'reqId' : reqId
    },
    timeout : 50000,
    sync : false,
    load : function(data, ioargs) {
      cmr.hideProgress();
      console.log(data);
      if (!data.result.success) {
        cmr.showAlert(data.error, 'Error');
      } else {
        this.disabled = true;
        // cmr.showAlert('You have been added to the notfication list
        // successfully.', 'Success', true);
      }
    },
    error : function(error, ioargs) {
      cmr.hideProgress();
      // cmr.showAlert('An error occurred while adding to notification list.',
      // 'Error','cmr.showModal("dupReq_modal")');
    }
  });
}

var requestIdRsn = null;
function submitForCreationBR(formName) {
  cmr.hideModal('dupCMR_modal');
  cmr.showProgress('Saving request data..');
  dojo.xhrPost({
    url : cmr.CONTEXT_ROOT + '/auto/process.json',
    handleAs : 'json',
    method : 'POST',
    content : dojo.formToObject(formName),
    timeout : 60000,
    sync : false,
    load : function(data, ioargs) {
      cmr.hideProgress();
      console.log(data);
      if (!data.success) {
        cmr.showAlert(data.error, 'Error');
      } else {
        requestIdRsn = data.reqId;
        var dupCMRReason = FormManager.getActualValue('dupCmrRsn');
        saveDupCMRReason();
      }
    },
    error : function(error, ioargs) {
      cmr.hideProgress();
      cmr.showAlert('An error occurred while saving the request. Please contact your system administrator', 'Error');
    }
  });
}

function closeDupCMRReasonModal() {
  var dupCmrReason = dojo.byId('dupCmrRsn').value.trim();
  if (dupCmrReason != null && dupCmrReason != '') {
    FormManager.setValue('dupCmrReason', dupCmrReason);
    cmr.hideModal('dupCMRReasonModal');
    Automation.submitForCreation('frmCMR');
  } else {
    alert('Please provide a valid reason for creating duplicate CMR.');
  }
}

function saveDupCMRReason() {
  cmr.showProgress('Saving the request reason..');
  dojo.xhrPost({
    url : cmr.CONTEXT_ROOT + '/auto/duplicate/dupcmrreason.json',
    handleAs : 'json',
    method : 'POST',
    content : {
      reqId : requestIdRsn,
      dupCmrRsn : FormManager.getActualValue('dupCmrRsn')
    },
    timeout : 60000,
    sync : false,
    load : function(data, ioargs) {
      cmr.hideProgress();
      console.log(data);
      if (!data.success) {
        cmr.showAlert(data.error, 'Error');
      } else {
        cmr.showAlert('Request created with ID ' + requestIdRsn + ' and has been sent for processing.', 'Success', 'Automation.redirectToWorkflow()', true);
      }
    },
    error : function(error, ioargs) {
      cmr.hideProgress();
      cmr.showAlert('An error occurred while creating the request. Please contact your system administrator', 'Error');
    }
  });
}

function addBrVATValidator() {
  // length validator
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var vat = FormManager.getActualValue('vat');
        if ((vat != null && vat != '')) {
          console.log("Running addBrVATValidator");
          if (vat.length != 14) {
            return new ValidationResult({
              id : 'vat',
              type : 'text',
              name : 'vat'
            }, false, 'Vat length should be equal to 14.');
          } else {
            return new ValidationResult(null, true);
          }

        }

        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_GENERAL_TAB', 'frmCMR');

  // length validator
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var vatEndUser = FormManager.getActualValue('vatEndUser');
        if ((vatEndUser != null && vatEndUser != '')) {
          console.log("Running addBrVATEndUserValidator");
          if (vatEndUser.length != 14) {
            return new ValidationResult({
              id : 'vatEndUser',
              type : 'text',
              name : 'vatEndUser'
            }, false, 'Vat End User length should be equal to 14.');
          } else {
            return new ValidationResult(null, true);
          }

        }

        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_GENERAL_TAB', 'frmCMR');

}

function addBrFiscalCdValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var fiscalCd = FormManager.getActualValue('municipalFiscalCode');
        if ((fiscalCd != null && fiscalCd != '')) {
          console.log("Running addBrFiscalCdValidator");
          var regex = /[^0]+/gm;
          if (!regex.test(fiscalCd) || fiscalCd == 'na' || fiscalCd == 'NA' || fiscalCd == 'n/a' || fiscalCd == 'N/A') {
            return new ValidationResult(null, false, 'Invalid format for MunicipalFiscal Code.');
          } else {
            return new ValidationResult(null, true);
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_GENERAL_TAB', 'frmCMR');

  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var fiscalCdEndUsr = FormManager.getActualValue('municipalFiscalCodeEndUser');

        if ((fiscalCdEndUsr != null && fiscalCdEndUsr != '')) {
          console.log("Running addBrFiscalCdEndUserValidator");
          var regex = /[^0]+/gm;
          if (!regex.test(fiscalCdEndUsr) || fiscalCdEndUsr == 'na' || fiscalCdEndUsr == 'NA' || fiscalCdEndUsr == 'n/a' || fiscalCdEndUsr == 'N/A') {
            return new ValidationResult(null, false, 'Invalid format for MunicipalFiscalEndUser Code.');
          } else {
            return new ValidationResult(null, true);
          }
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_GENERAL_TAB', 'frmCMR');
}

dojo.addOnLoad(function() {

  GEOHandler.addAfterConfig(afterConfigForBRV2, [ '631' ]);
  GEOHandler.addAfterConfig(handleUpdateReason, [ '631' ]);
  GEOHandler.registerValidator(verifyRequestReasonUpdate, [ '631' ]);
  GEOHandler.registerValidator(emailValidation, [ '631' ]);
  GEOHandler.registerValidator(addBrVATValidator, [ '631' ]);
  GEOHandler.registerValidator(addBrFiscalCdValidator, [ '631' ]);

});