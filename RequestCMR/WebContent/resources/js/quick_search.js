var app = angular.module('QuickSearchApp', [ 'ngSanitize' ]);
var _inscp = null;
var _currQuickDet = null;

app.filter('recFilter', function() {
  return function(items, search) {
    if (!search) {
      return items;
    }

    return items.filter(function(record, index, array) {
      var val = search;
      if (!val) {
        return true;
      }
      val = val.toUpperCase();
      if (record.cmrNo && record.cmrNo.toUpperCase().indexOf(val) >= 0) {
        return true;
      }
      if (record.dunsNo && record.dunsNo.toUpperCase().indexOf(val) >= 0) {
        return true;
      }
      if (record.name && record.name.toUpperCase().indexOf(val) >= 0) {
        return true;
      }
      if (record.countryCd && record.countryCd.toUpperCase().indexOf(val) >= 0) {
        return true;
      }
      if (record.stateProv && record.stateProv.toUpperCase().indexOf(val) >= 0) {
        return true;
      }
      if (record.streetAddress1 && record.streetAddress1.toUpperCase().indexOf(val) >= 0) {
        return true;
      }
      if (record.streetAddress2 && record.streetAddress2.toUpperCase().indexOf(val) >= 0) {
        return true;
      }
      if (record.postCd && record.postCd.toUpperCase().indexOf(val) >= 0) {
        return true;
      }
      if (record.vat && record.vat.toUpperCase().indexOf(val) >= 0) {
        return true;
      }
      if (record.recType && record.recType.toUpperCase() == val.toUpperCase()) {
        return true;
      }
      return false;
    });

  };
});

app.controller('QuickSearchController', [ '$scope', '$document', '$http', '$timeout', '$sanitize', '$filter', function($scope, $document, $http, $timeout, $sanitize, $filter) {

  $scope.searched = false;
  $scope.orgIdSearch = false;
  $scope.records = [];
  $scope.cmrNo = '';
  $scope.allowByModel = true;

  $scope.titles = {
    E1 : 'Exact matches against name, street line 1, street line 2, city, postal code, and country.',
    E2 : 'Exact matches against name, street line 1, city, postal code, and country.',
    E3 : 'Exact matches against name, street line 1, city, and country.',
    E4 : 'Exact matches against name, street line 2, city, postal code, and country.',
    E5 : 'Exact matches against name, street line 2, city, and country.',
    F1 : 'Fuzzy matches against name, street line 1, street line 2, city, postal code, and country.',
    F2 : 'Fuzzy matches against name, street line 1, city, postal code, and country.',
    F3 : 'Fuzzy matches against name, street line 1, city, and country.',
    F4 : 'Fuzzy matches against name, street line 2, city, postal code, and country.',
    F5 : 'Fuzzy matches against name, street line 2, city, and country.',
    VAT : 'CMRs found with the same VAT or Tax Code only.',
    A : 'Record an absolute match using CMR No.',
    LANG : 'Record matched against local language data.',
    DUNS : 'CMRs found for the related D&B Matches.',
  };

  $scope.findCompanies = function() {
    console.log(issuingCntry.value);
    $scope.issuingCntryText = issuingCntry.value;
    if (!FormManager.validate('frmCMR')) {
      return;
    }
    $scope.orgIdSearch = false;
    var crit = buildSearchCriteria();
    if (!crit.name || !crit.streetAddress1 || !crit.city) {
      $scope.orgIdSearch = true;
    }
    $scope.frozen = crit;
    if (crit.cmrNo && (crit.name || crit.stateProv || crit.city || crit.streetAddress1 || crit.streetAddress2 || crit.postCd || crit.vat)) {
      if (!confirm('CMR No. is specified so the values for the address fields will be ignored and only the records under the CMR No. will be retrieved. Proceed?')) {
        return;
      }
    }
    $scope.cmrNo = crit.cmrNo;
    $scope.highMatchGrade = false
    $scope.records = [];
    cmr.showProgress('Searching for records, please wait..');
    $scope.allowByModel = true;
    $scope.hideLocalLangData = false;
    dojo.xhrPost({
      url : cmr.CONTEXT_ROOT + '/quick_search/find.json',
      handleAs : 'json',
      method : 'POST',
      content : crit,
      timeout : 2 * 60000,
      sync : false,
      load : function(data, ioargs) {
        cmr.hideProgress();
        if (data && data.items) {
          var byModel = cmr.query('CREATE_BY_MODEL_DISABLED', {CNTRY_CD : FormManager.getActualValue('issuingCntry')});
          if (byModel && byModel.ret1 == 'Y'){
            $scope.allowByModel = false;
          } 
          
          var localLangDataFlg = cmr.query('HIDE_LOCAL_LANG_DATA', {CNTRY_CD : FormManager.getActualValue('issuingCntry')});
          if (localLangDataFlg && localLangDataFlg.ret1 == 'Y'){
            $scope.hideLocalLangData = true;
          } 
          if (data.items.length > 50) {
            alert('The search resulted to more than 50 matches. Only the top 50 matches will be shown. Please try to change the search parameters to get the other records you need.');
            console.log('splicing from 50, removing ' + (data.items.length - 50) + ' items');
            data.items.splice(50, data.items.length - 50);
          }
          data.items.forEach(function(item, i) {
            if (item.recType == 'DNB' && !item.countryCd) {
              item.countryCd = crit.countryCd;
            }
            if (item.recType == 'DNB' && !$scope.highMatchGrade && (item.matchGrade == '10' || item.matchGrade == '09')) {
              $scope.highMatchGrade = true;
            }
            if (item.recType == 'CMR' && item.name) {
              item.name = item.name.replace(/@/g, ' ');
              if (item.altName) {
                item.altName = item.altName.replace(/[\u00A0\u1680​\u180e\u2000-\u2009\u200a​\u200b​\u202f\u205f​\u3000]/g, '');
              }
              if (item.altStreet) {
                item.altStreet = item.altStreet.replace(/[\u00A0\u1680​\u180e\u2000-\u2009\u200a​\u200b​\u202f\u205f​\u3000]/g, '');
              }
              if (item.altCity) {
                item.altCity = item.altCity.replace(/[\u00A0\u1680​\u180e\u2000-\u2009\u200a​\u200b​\u202f\u205f​\u3000]/g, '');
              }
              
            }
          });
          $scope.records = data.items;
          $scope.searched = true;
          $scope.$apply();
        }
      },
      error : function(error, ioargs) {
        cmr.hideProgress();
        $scope.searched = false;
        console.log('error');
        console.log(error);
      }
    });

    $scope.openDetails = function(record) {
      var issuingCntry = FormManager.getActualValue('issuingCntry');
      if (issuingCntry.length > 3) {
        issuingCntry = issuingCntry.substring(0, 3);
      }
      if (record.recType == 'CMR') {
        WindowMgr.open('COMPDET', 'CMR' + record.cmrNo, 'company_details?issuingCountry=' + issuingCntry + '&cmrNo=' + record.cmrNo, null, 550);
      } else if (record.recType == 'DNB') {
        WindowMgr.open('COMPDET', 'DNB' + record.dunsNo, 'company_details?issuingCountry=' + issuingCntry + '&dunsNo=' + record.dunsNo, null, 550);
      } else if (record.recType == 'REQ') {
        WindowMgr.open('SUMMARY', record.cmrNo, 'summary?reqId=' + record.cmrNo + '&reqType=C', null, null);
      } else {
        cmr.showAlert('Cannot open details of the record.');
      }
    };

  };

  $scope.clearCriteria = function() {
    // FormManager.setValue('issuingCntry', '');
    FormManager.setValue('cmrNo', '');
    FormManager.setValue('name', '');
    // FormManager.setValue('countryCd', '');
    FormManager.setValue('stateProv', '');
    FormManager.setValue('city', '');
    FormManager.setValue('streetAddress1', '');
    FormManager.setValue('streetAddress2', '');
    FormManager.setValue('postCd', '');
    FormManager.setValue('vat', '');
    FormManager.setValue('taxCd1', '');
    $scope.records = [];
    $scope.searched = false;
  };

  $scope.confirmImport = function(rec, update) {
    var msg = 'A new ' + (update ? 'Update' : 'Create') + ' request will be started using this CMR. You will be redirected to the request screen after processing. Proceed?';
    if (rec.recType == 'DNB') {
      msg = 'A new Create request will be started using this D&B record. You will be redirected to the request screen after processing. Proceed?';
    }
    if (!confirm(msg)) {
      return;
    }
    $scope.importRecord(rec, update);
  };

  $scope.importRecord = function(rec, update) {
    var temp = JSON.stringify($scope.frozen);
    var model = JSON.parse(temp);
    model.subRegion = $scope.getSubRegion(model.issuingCntry, model.countryCd && model.countryCd.length == 2 ? model.countryCd : $scope.frozen.countryCd);
    model.reqType = update ? 'U' : 'C';
    model.hasCmr = false;
    model.hasDnb = false;
    model.cmrNo = rec.cmrNo;
    model.dunsNo = rec.dunsNo;
    model.recType = rec.recType;
    $scope.records.forEach(function(curr, i) {
      if (curr.recType == 'CMR') {
        model.hasCmr = true;
      }
      if (curr.recType == 'DNB') {
        model.hasDnb = true;
      }
    });

    if (update) {
      var cmrNo = model.cmrNo;
      var country = model.issuingCntry;
      var q = cmr.query('QUICKSEARCH.ASYNC.CHECK', {
        MANDT : cmr.MANDT,
        CNTRY : country,
        CMR_NO : cmrNo
      });
      if (q && !isNaN(q.ret1) && Number(q.ret1) > 100) {
        if (confirm('This CMR has more than 100 addresses. Do you want to create this request in the background to assure all information are loaded properly on the request?')) {
          dojo.xhrPost({
            url : cmr.CONTEXT_ROOT + '/quick_search/async.json',
            handleAs : 'json',
            method : 'POST',
            content : model,
            timeout : 3 * 60000,
            sync : false,
            load : function(data, ioargs) {
              console.log(data);
              if (data && data.success) {
                cmr.showAlert('Request has been queued for creation successfully. You will be notified via mail once the request is created.', null, null, true);
                $scope.clearCriteria();
              } else {
                cmr.showAlert('An unexpected error occurred during the processing, pls try again later.');
              }
            },
            error : function(error, ioargs) {
              cmr.hideProgress();
              $scope.searched = false;
              console.log('error');
              console.log(error);
              cmr.showAlert('An unexpected error occurred during the processing, pls try again later.');
            }
          });
          return;
        }
      }
    }
    cmr.showProgress('Creating new request from the record, please wait..');
    dojo.xhrPost({
      url : cmr.CONTEXT_ROOT + '/quick_search/process.json',
      handleAs : 'json',
      method : 'POST',
      content : model,
      timeout : 7 * 60000,
      sync : false,
      load : function(data, ioargs) {
        if (data && data.success) {
          var reqId = data.model.reqId;
          window.location = cmr.CONTEXT_ROOT + '/request/' + reqId + '?qs=Y&infoMessage=' + encodeURIComponent('Request created successfully from the chosen record.');
        } else {
          cmr.hideProgress();
          cmr.showAlert(data.msg ? data.msg : 'An unexpected error occurred during the processing, pls try again later.');
        }
      },
      error : function(error, ioargs) {
        cmr.hideProgress();
        $scope.searched = false;
        console.log('error');
        console.log(error);
        cmr.showAlert('An unexpected error occurred during the processing, pls try again later.');
      }
    });

  };

  $scope.confirmCreateNew = function() {
    var msg = 'A new request with blank data will be created and both CMR and D&B search will need to be done for that request. You will be directed to the request page. Proceed?';
    cmr.showConfirm('_inscp.goToBlankCreate()', msg);
  };

  $scope.goToBlankCreate = function() {
    var subRegion = $scope.getSubRegion($scope.frozen.issuingCntry, $scope.frozen.countryCd);
    var cntry = $scope.frozen.issuingCntry;
    if (subRegion) {
      cntry = subRegion;
    }
    var url = '/request?create=Y&newReqType=C&newReqCntry=' + cntry;

    var ret = cmr.query('GET.START_FROM_QUICK_SEARCH', {
      CNTRY : cntry.substring(0, 3)
    });
    if (ret && ret.ret1 && 'Y' == ret.ret1) {
      url += '&_f=Y';
    }
    goToUrl(cmr.CONTEXT_ROOT + url);
  }

  $scope.createNewCmr = function() {
    var model = $scope.frozen;

    if (model.issuingCntry == '760' || model.issuingCntry == '641') {
      var msg = 'Due to English and Local Language requirements of Japan/China addresses, only a blank request can be created. Proceed?';
      if (confirm(msg)) {
        $scope.goToBlankCreate();
      }
      return;
    }
    model.postCd = FormManager.getActualValue('postCd');
    var noCities = [ '736', '738', '834' ]; // MACAO, SINGAPORE, HONG KONG
    if (noCities.indexOf(model.issuingCntry) >= 0) {
      if (!model.name || !model.issuingCntry || !model.countryCd || !model.streetAddress1) {
        cmr.showAlert('Customer Name, Issuing Country, Landed Country, and Street Address 1 are all required.')
        return;
      }
    } else {
      if (!model.name || !model.issuingCntry || !model.countryCd || !model.city || !model.streetAddress1) {
        cmr.showAlert('Customer Name, Issuing Country, Landed Country, City, and Street Address 1 are all required.')
        return;
      }
    }
    var postCdRet = cmr.validateZIP(model.countryCd, model.postCd, model.issuingCntry);
    if (!postCdRet.success) {
      var pattern = postCdRet.errorPattern;
      if (!pattern) {
        cmr.showAlert(postCdRet.errorMessage);
      } else {
        cmr.showAlert(postCdRet.errorMessage + '. Value should be in the format of ' + pattern.formatReadable);
      }
      return;
    }

    model.stateProv = FormManager.getActualValue('stateProv');
    // var withStates = [ '613', '615', '616', '618', '619', '621', '624',
    // '629', '631', '641', '643', '655', '661', '663', '668', '678', '681',
    // '683', '694', '702', '706', '708', '718', '724',
    // '726', '731', '735', '740', '744', '754', '755', '758', '759', '760',
    // '762', '778', '781', '788', '796', '799', '806', '811', '813', '815',
    // '820', '821', '829', '834', '838', '839',
    // '846', '848', '849', '852', '856', '859', '862', '864', '866', '869',
    // '871', '889', '897' ];
    // if (withStates.indexOf(model.issuingCntry) >= 0 && !model.stateProv) {
    // cmr.showAlert('State / Province is required for an address under this
    // country.');
    // return;
    // }
    model.vat = FormManager.getActualValue('vat');
    if (model.vat) {
      var vatRet = cmr.validateVAT(model.countryCd, model.vat);
      if (!vatRet.success) {
        var pattern = vatRet.errorPattern;
        if (!pattern) {
          cmr.showAlert(vatRet.errorMessage);
        } else {
          cmr.showAlert(vatRet.errorMessage + '. Value should be in the format of ' + pattern.formatReadable);
        }
        return;
      }
    }
    model.taxCd1 = FormManager.getActualValue('taxCd1');
    // if (model.issuingCntry == '706' && (!model.taxCd1 || !model.vat)) {
    // cmr.showAlert('At least VAT or SIRET must be specified.');
    // return;
    // }

    var details = 'System Location: ' + model.issuingCntry;
    details += '<br>' + model.name;
    ;
    details += '<br>' + model.streetAddress1;
    if (model.streetAddress2) {
      details += '<br>' + model.streetAddress2;
    }
    details += '<br>' + model.city;
    if (model.stateProv) {
      details += ', ' + model.stateProv;
    }
    details += '<br>' + model.countryCd;
    if (model.postCd) {
      details += ' ' + model.postCd;
    }
    if (model.vat) {
      details += '<br>VAT: ' + model.vat;
    }
    if (model.taxCd1) {
      if (model.issuingCntry == '706') {
        details += '<br>SIRET: ' + model.taxCd1;
      } else {
        details += '<br>Tax Code: ' + model.taxCd1;
      }
    }

    _currQuickDet = details;
    cmr.showModal('quickSearchModal');
  };

  $scope.proceedCreateRequest = function() {
    cmr.hideModal('quickSearchModal');
    var model = $scope.frozen;
    model.postCd = FormManager.getActualValue('postCd');
    model.vat = FormManager.getActualValue('vat');
    model.reqType = 'X';
    model.hasCmr = false;
    model.hasDnb = false;
    $scope.records.forEach(function(curr, i) {
      if (curr.recType == 'CMR') {
        model.hasCmr = true;
      }
      if (curr.recType == 'DNB') {
        model.hasDnb = true;
      }
    });
    cmr.showProgress('Creating new request from the specified details, please wait..');
    model.subRegion = $scope.getSubRegion(model.issuingCntry, model.countryCd);
    dojo.xhrPost({
      url : cmr.CONTEXT_ROOT + '/quick_search/process.json',
      handleAs : 'json',
      method : 'POST',
      content : model,
      timeout : 7 * 60000,
      sync : false,
      load : function(data, ioargs) {
        if (data && data.success) {
          var reqId = data.model.reqId;
          window.location = cmr.CONTEXT_ROOT + '/request/' + reqId + '?qs=Y&infoMessage=' + encodeURIComponent('Request created successfully with address information.');
        } else {
          cmr.hideProgress();
          cmr.showAlert(data.msg ? data.msg : 'An unexpected error occurred during the processing, pls try again later.');
        }
      },
      error : function(error, ioargs) {
        cmr.hideProgress();
        $scope.searched = false;
        console.log('error');
        console.log(error);
        cmr.showAlert('An unexpected error occurred during the processing, pls try again later.');
      }
    });
  };

  $scope.getSubRegion = function(issuingCntry, countryCd) {
    var issuingCntry = FormManager.getActualValue('issuingCntry');
    if (issuingCntry && issuingCntry.length > 3) {
      return issuingCntry;
    }

    if (issuingCntry != "702" && issuingCntry != "678" && issuingCntry != "624") {
      var ret = cmr.query('QUICK.CHECK_SUBREGION', {
        CNTRY : issuingCntry,
        CD : issuingCntry + countryCd
      });
      if (ret && ret.ret1) {
        return issuingCntry + countryCd;
      }
    }
    ret = cmr.query('QUICK.CHECK_SUBREGION_DEFLT', {
      CNTRY : issuingCntry,
      CD : issuingCntry
    });
    if (ret && ret.ret1) {
      return issuingCntry;
    }
    return null;
  };

  $scope.parseVat = function(vat) {
    var list = [];
    if (!vat) {
      return list;
    }
    var split = vat.split(',');
    for (var i = 0; i < split.length; i++) {
      list.push(split[i]);
    }
    return list;
  };

  $scope.exportToPdf = function(rec) {
    cmr.showProgress('Exporting request details. Please wait...');
    var reqId = rec.cmrNo;
    var token = new Date().getTime();
    FormManager.setValue('pdfReqId', reqId);
    FormManager.setValue('pdfTokenId', token);
    document.forms['frmPDF'].submit();
    window.setTimeout('checkToken("' + token + '")', 1000);
  };

  _inscp = $scope;

} ]);

app.controller('DetailsController', [ '$scope', '$document', '$http', '$timeout', '$sanitize', function($scope, $document, $http, $timeout, $sanitize) {
  $scope.allowByModel = true;
  $scope.hideLocalLangData = false;
  $scope.getParameterByName = function(name, url) {
    if (!url) {
      url = window.location.href;
    }
    name = name.replace(/[\[\]]/g, '\\$&');
    var regex = new RegExp('[?&]' + name + '(=([^&#]*)|&|#|$)');
    var results = regex.exec(url);
    if (!results) {
      return null;
    }
    if (!results[2]) {
      return '';
    }
    return decodeURIComponent(results[2].replace(/\+/g, ' '));
  };

  $scope.expColl = function(rec, key) {
    switch (key) {
    case 'colLoc':
      rec.colLoc = !rec.colLoc;
      break;
    case 'colCust':
      rec.colCust = !rec.colCust;
      break;
    case 'colIbm':
      rec.colIbm = !rec.colIbm;
      break;
    case 'colAlt':
      rec.colAlt = !rec.colAlt;
      break;
    }
  }
  $scope.expCollAll = function(exp) {
    $scope.records.forEach(function(rec, i) {
      rec.colLoc = exp;
      rec.colCust = exp;
      rec.colIbm = exp;
      rec.colAlt = exp;
    });
  };
  $scope.records = [];
  $scope.dnb = {};
  $scope.cmrNo = $scope.getParameterByName('cmrNo');
  $scope.dunsNo = $scope.getParameterByName('dunsNo');
  $scope.issuingCountry = $scope.getParameterByName('issuingCountry');
  $scope.viewMode = 'S';
  var byModel = cmr.query('CREATE_BY_MODEL_DISABLED', {CNTRY_CD : $scope.issuingCountry});
  if (byModel && byModel.ret1 == 'Y'){
    $scope.allowByModel = false;
  } 
  
  var localLangDataFlg = cmr.query('HIDE_LOCAL_LANG_DATA', {CNTRY_CD : FormManager.getActualValue('issuingCntry')});
  if (localLangDataFlg && localLangDataFlg.ret1 == 'Y'){
    $scope.hideLocalLangData = true;
   } 

  $scope.loadDetails = function() {
    if ($scope.cmrNo) {
      cmr.showProgress('Getting details of CMR No. ' + $scope.cmrNo);
      dojo.xhrGet({
        url : cmr.CONTEXT_ROOT + '/quick_search/details.json?issuingCountry=' + $scope.issuingCountry + '&cmrNo=' + $scope.cmrNo,
        handleAs : 'json',
        method : 'GET',
        content : {},
        timeout : 60000,
        sync : false,
        load : function(data, ioargs) {
          cmr.hideProgress();
          if (data.success && data.data.items) {
            if (data.data.items.length > 50) {
              alert('The CMR contains more than 50 records. Only the top 50 records will be displayed.');
              data.data.items.splice(50, data.items.length - 50);
            }
            data.data.items.forEach(function(item, i) {
              item.colLoc = true;
              item.colIbm = false;
              item.colCust = false;
              item.colAlt = false;
            });
            $scope.records = data.data.items;
            $scope.$apply();
          } else {
            cmr.showAlert(data.msg ? data.msg : 'A system error occurred during the retrieval of the details. Please try again later.');
          }
        },
        error : function(error, ioargs) {
          cmr.hideProgress();
          $scope.searched = false;
          console.log('error');
          console.log(error);
          cmr.showAlert('A system error occurred during the retrieval of the details. Please try again later.');
        }
      });
    } else if ($scope.dunsNo) {
      cmr.showProgress('Getting details of DUNS No. ' + $scope.dunsNo);
      dojo.xhrGet({
        url : cmr.CONTEXT_ROOT + '/quick_search/details.json?dunsNo=' + $scope.dunsNo,
        handleAs : 'json',
        method : 'GET',
        content : {},
        timeout : 60000,
        sync : false,
        load : function(data, ioargs) {
          cmr.hideProgress();
          if (data.success && data.data.results) {
            $scope.dnb = data.data.results[0];
            $scope.$apply();
          } else {
            cmr.showAlert(data.msg ? data.msg : 'A system error occurred during the retrieval of the details. Please try again later.');
          }
        },
        error : function(error, ioargs) {
          cmr.hideProgress();
          $scope.searched = false;
          console.log('error');
          console.log(error);
          cmr.showAlert('A system error occurred during the retrieval of the details. Please try again later.');
        }
      });
    } else {
      cmr.showAlert('No key specified.');
    }
  };

  $scope.confirmImport = function(update) {
    console.log(window.opener ? window.opener._inscp : 'no parent');
    if (!window.opener || !window.opener._inscp) {
      cmr.showAlert('The Quick Search screen has been closed. Please do the import while the parent screen is open.');
      return;
    }
    var msg = 'A new ' + (update ? 'Update' : 'Create') + ' request will be started using this CMR. You will be redirected to the request screen after processing. Proceed?';
    if (!$scope.cmrNo) {
      msg = 'A new Create request will be started using this D&B record. You will be redirected to the request screen after processing. Proceed?';
    }
    if (!confirm(msg)) {
      return;
    }
    var rec = {
      recType : $scope.cmrNo ? 'CMR' : 'DNB',
      reqType : update ? 'U' : 'C',
      cmrNo : $scope.cmrNo ? $scope.cmrNo : null,
      dunsNo : $scope.dunsNo ? $scope.dunsNo : null,
      issuingCntry : $scope.issuingCountry
    }
    window.opener._inscp.importRecord(rec, update);
    WindowMgr.closeMe();
  };

  $timeout($scope.loadDetails, 500);
} ]);

function buildSearchCriteria() {
  var ret = {
    issuingCntry : FormManager.getActualValue('issuingCntry'),
    countryCd : FormManager.getActualValue('countryCd'),
    stateProv : FormManager.getActualValue('stateProv'),
    city : FormManager.getActualValue('city'),
    postCd : FormManager.getActualValue('postCd'),
    streetAddress1 : FormManager.getActualValue('streetAddress1'),
    streetAddress2 : FormManager.getActualValue('streetAddress2'),
    name : FormManager.getActualValue('name'),
    cmrNo : FormManager.getActualValue('cmrNo'),
    vat : FormManager.getActualValue('vat'),
    taxCd1 : FormManager.getActualValue('taxCd1'),
    restrictTo : FormManager.getActualValue('restrictTo'),
    addDnBMatches : FormManager.getActualValue('addDnBMatches'),
  };
  var issuingCntry = FormManager.getActualValue('issuingCntry');
  if (issuingCntry.length > 3) {
    ret.issuingCntry = issuingCntry.substring(0, 3);
  }
  return ret;
}

function quickSearchModal_onLoad() {
  console.log('on load');
  dojo.byId('quick-new-det').innerHTML = _currQuickDet;
}

var US_STATES = [ {
  code : 'AA',
  name : 'Armed Forces Americas'
}, {
  code : 'AE',
  name : 'Armed Forces Europe'
}, {
  code : 'AK',
  name : 'Alaska'
}, {
  code : 'AL',
  name : 'Alabama'
}, {
  code : 'AP',
  name : 'Armed Forces Pacific'
}, {
  code : 'AR',
  name : 'Arkansas'
}, {
  code : 'AS',
  name : 'American Samoa'
}, {
  code : 'AZ',
  name : 'Arizona'
}, {
  code : 'CA',
  name : 'California'
}, {
  code : 'CO',
  name : 'Colorado'
}, {
  code : 'CT',
  name : 'Connecticut'
}, {
  code : 'DC',
  name : 'District of Columbia'
}, {
  code : 'DE',
  name : 'Delaware'
}, {
  code : 'FL',
  name : 'Florida'
}, {
  code : 'GA',
  name : 'Georgia'
}, {
  code : 'GU',
  name : 'Guam'
}, {
  code : 'HI',
  name : 'Hawaii'
}, {
  code : 'IA',
  name : 'Iowa'
}, {
  code : 'ID',
  name : 'Idaho'
}, {
  code : 'IL',
  name : 'Illinois'
}, {
  code : 'IN',
  name : 'Indiana'
}, {
  code : 'KS',
  name : 'Kansas'
}, {
  code : 'KY',
  name : 'Kentucky'
}, {
  code : 'LA',
  name : 'Louisiana'
}, {
  code : 'MA',
  name : 'Massachusetts'
}, {
  code : 'MD',
  name : 'Maryland'
}, {
  code : 'ME',
  name : 'Maine'
}, {
  code : 'MH',
  name : 'Marshall Islands'
}, {
  code : 'MI',
  name : 'Michigan'
}, {
  code : 'MN',
  name : 'Minnesota'
}, {
  code : 'MO',
  name : 'Missouri'
}, {
  code : 'MP',
  name : 'Mariana Islands'
}, {
  code : 'MS',
  name : 'Mississippi'
}, {
  code : 'MT',
  name : 'Montana'
}, {
  code : 'NC',
  name : 'North Carolina'
}, {
  code : 'ND',
  name : 'North Dakota'
}, {
  code : 'NE',
  name : 'Nebraska'
}, {
  code : 'NH',
  name : 'New Hampshire'
}, {
  code : 'NJ',
  name : 'New Jersey'
}, {
  code : 'NM',
  name : 'New Mexico'
}, {
  code : 'NV',
  name : 'Nevada'
}, {
  code : 'NY',
  name : 'New York'
}, {
  code : 'OH',
  name : 'Ohio'
}, {
  code : 'OK',
  name : 'Oklahoma'
}, {
  code : 'OR',
  name : 'Oregon'
}, {
  code : 'PA',
  name : 'Pennsylvania'
}, {
  code : 'PR',
  name : 'Puerto Rico'
}, {
  code : 'RI',
  name : 'Rhode Island'
}, {
  code : 'SC',
  name : 'South Carolina'
}, {
  code : 'SD',
  name : 'South Dakota'
}, {
  code : 'TN',
  name : 'Tennessee'
}, {
  code : 'TX',
  name : 'Texas'
}, {
  code : 'UM',
  name : 'US Minor Outl. Isles'
}, {
  code : 'UT',
  name : 'Utah'
}, {
  code : 'VA',
  name : 'Virginia'
}, {
  code : 'VI',
  name : 'Virgin Islands'
}, {
  code : 'VT',
  name : 'Vermont'
}, {
  code : 'WA',
  name : 'Washington'
}, {
  code : 'WI',
  name : 'Wisconsin'
}, {
  code : 'WV',
  name : 'West Virginia'
}, {
  code : 'WY',
  name : 'Wyoming'
} ]
dojo.addOnLoad(function() {
  if (typeof(bypassqs) != 'undefined' && bypassqs){
    return;
  }
  if (typeof(_furl) == 'undefined' || !_furl) {
    return;
  }
  var findcmrUrl = _furl.substring(0, _furl.lastIndexOf('/')) + '/DnBSearch';
  // Bloodhound is used for the Twitter Typeahead suggestion engine
  var companies = new Bloodhound({
    datumTokenizer : function(datum) {
    },
    queryTokenizer : Bloodhound.tokenizers.whitespace,
    remote : {
      url : findcmrUrl + '/proxy/v1/search/typeahead?',
      replace : function(url, query) {
        var companyfield = FormManager.getActualValue('name');
        var country = FormManager.getActualValue('countryCd');
        var city = FormManager.getActualValue('city');
        var state = FormManager.getActualValue('stateProv');
        var postCd = FormManager.getActualValue('postCd');
        if (companyfield) {
          url = url + '&searchTerm=' + encodeURIComponent(companyfield);
        }
        if (country) {
          url = url + '&countryISOAlpha2Code=' + encodeURIComponent(country);
        }
        if (state && state != '\'\'') {
          url = url + '&addressRegion=' + encodeURIComponent(state)
        }
        if (city) {
          url = url + '&addressLocality=' + encodeURIComponent(city)
        }
        if (postCd) {
          url = url + '&postalCode=' + encodeURIComponent(postCd)
        }
        return url;
      },
      ajax : {
        beforeSend : function() {
          $('#name').addClass('loading')
        },
        complete : function(data) {
          $('#name').removeClass('loading')
        }
      },
      // filters the Direct 2.0 raw results into simple JSON
      filter : function(parsedResponse) {
        var results = getJsonElement(parsedResponse, 'searchCandidates');
        var toReturn = [];
        $.each(results, function(index, candidate) {
          var object = candidate.organization;
          var result = {
            'DunsNumber' : getJsonElement(object, 'duns'),
            'OrganizationName' : getJsonElement(object, 'primaryName'),
            'LocationType' : getJsonElement(object, 'FamilyTreeMemberRole[0].FamilyTreeMemberRoleText.$', 'Unknown Type'),
            'City' : getJsonElement(object, 'primaryAddress.addressLocality.name', 'Unknown'),
            'Territory' : getJsonElement(object, 'primaryAddress.addressRegion.name', 'Unknown'),
            'Street' : getJsonElement(object, 'primaryAddress.streetAddress.line1', ''),
            'Street2' : getJsonElement(object, 'primaryAddress.streetAddress.line2', ''),
            'PostalCode' : getJsonElement(object, 'primaryAddress.postalCode'),
            'Country' : getJsonElement(object, 'primaryAddress.addressCountry.isoAlpha2Code'),
            'MarketabilityIndicator' : getJsonElement(object, 'dunsControl.isMarketable', 'Unknown'),
            'TerritoryCode' : getJsonElement(object, 'primaryAddress.addressRegion.abbreviatedName', 'Unknown'),
          }
          toReturn.push(result);
        });
        return toReturn;
      }
    },
    limit : 6
  });

  companies.initialize();

  var $companyField = $("#name");

  // The actual typeahead declaration
  $companyField.typeahead(
      null,
      {
        name : 'company-search',
        displayKey : 'OrganizationName',
        source : companies.ttAdapter(),
        templates : {
          empty : [ '<div class="empty-message">', 'no results found', '</div>' ].join('\n'),
          suggestion : function(data) {
            var marketable = data.MarketabilityIndicator ? 'Yes' : 'No';
            var html = '<div title="Click to fill in the fields" class="typeahead-suggestion"><span style="margin-left:0"><strong>' + data.OrganizationName + '</strong></span><span class="sub-text">'
                + data.Street + '</span><span class="sub-text">' + data.City + ', ' + data.Territory + ' ' + data.PostalCode + '</span></div>';
            return html;
          }
        }

      }).on('typeahead:selected', handleSelection)

  $companyField.focus();
  $companyField.on("click", function() {
    $(this).select();
  });

  function handleSelection(event, selected, dataset) {
    var cntry = FormManager.getActualValue('issuingCntry');
    FormManager.setValue('name', selected.OrganizationName);
    FormManager.setValue('countryCd', selected.Country);
    FormManager.setValue('streetAddress1', selected.Street);
    if (selected.Street2) {
      FormManager.setValue('streetAddress2', selected.Street2);
    }
    FormManager.setValue('city', selected.City);
    FormManager.setValue('postCd', selected.PostalCode);
    if (cntry == '897' && selected.Country == 'US' && selected.Territory != 'Unknown') {
      var code = '';
      for (var i = 0; i < US_STATES.length; i++) {
        if (US_STATES[i].name.toUpperCase() == selected.Territory.toUpperCase()) {
          FormManager.setValue('stateProv', US_STATES[i].code);
        }
      }
    }
  }

});

app.controller('FindCMRController', [ '$scope', '$document', '$http', '$timeout', '$sanitize', '$filter', function($scope, $document, $http, $timeout, $sanitize, $filter) {
  $scope.hello = 'Update request from FindCMR';
  $scope.getParameterByName = function(name, url) {
    if (!url) {
      url = window.location.href;
    }
    name = name.replace(/[\[\]]/g, '\\$&');
    var regex = new RegExp('[?&]' + name + '(=([^&#]*)|&|#|$)');
    var results = regex.exec(url);
    if (!results) {
      return null;
    }
    if (!results[2]) {
      return '';
    }
    return decodeURIComponent(results[2].replace(/\+/g, ' '));
  };

  $scope.createRequest = function() {
    var cmrNo = $scope.getParameterByName('cmrNo');
    var cntry = $scope.getParameterByName('cntry');
    if (cmrNo && cntry) {
      $scope.hello = 'Update request from FindCMR for CMR ' + cmrNo;
      cmr.showProgress('Creating new update request for CMR ' + cmrNo + ', please wait..');
      var model = {
        recType : 'CMR',
        cmrNo : cmrNo,
        issuingCntry : cntry,
        reqType : 'U'
      };
      dojo.xhrPost({
        url : cmr.CONTEXT_ROOT + '/quick_search/process.json',
        handleAs : 'json',
        method : 'POST',
        content : model,
        timeout : 7 * 60000,
        sync : false,
        load : function(data, ioargs) {
          console.log(data);
          if (data && data.success) {
            var reqId = data.model.reqId;
            window.location = cmr.CONTEXT_ROOT + '/request/' + reqId + '?qs=Y&infoMessage=' + encodeURIComponent('Request created successfully.');
          } else {
            cmr.hideProgress();
            cmr.showAlert(data.msg ? data.msg : 'An unexpected error occurred during the processing, pls try again later.');
          }
        },
        error : function(error, ioargs) {
          cmr.hideProgress();
          $scope.searched = false;
          console.log('error');
          console.log(error);
          cmr.showAlert('An unexpected error occurred during the processing, pls try again later.');
        }
      });
    } else {
      cmr.showAlert('Please check the CMR input.');
    }
  }

  $scope.createRequest();
} ]);
