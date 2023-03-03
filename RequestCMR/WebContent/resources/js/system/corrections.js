var app = angular.module('CorrectionsApp', [ 'ngRoute' ]);

var NO_EDIT = [ 'REQUESTER_ID', 'REQUESTER_NM', 'REQ_STATUS', 'CHILD_REQ_ID', 'LAST_UPDT_BY', 'POOL_CMR_INDC',
    'REQUESTING_LOB', 'REQ_REASON', 'REQ_TYPE', 'MAIN_ADDR_TYPE', 'LAST_PROC_CENTER_NM', 'INTERNAL_TYP', 'SEP_VAL_IND',
    'ITERATION_ID', 'FILE_NAME', 'SOURCE_SYST_ID', 'CMR_ISSUING_CNTRY', 'CMR_OWNER', 'SAVEINDAFTRTEMPLOAD', 'CUST_GRP', 'CUST_SUB_GRP',
    'REALCTY', ];

var ADDR_FIELDS = [ 'custNm1', 'custNm2', 'addrTxt', 'addrTxt2', 'city1', 'stateProv', 'postCd', 'custNm3', 'custNm4', 'divn', 'dept', 'importInd' ];

var CMR_ADDR_FIELDS = ['addrLine1', 'addrLine2', 'addrLine3', 'addrLine4', 'addrLine5', 'addrLine6', 'isAddrUseMailing', 'isAddrUseBilling','isAddrUseInstalling', 'isAddrUseShipping', 'isAddrUseEPL', 'isAddrUseLitMailing', 
  'addrLineI', 'addrLineN', 'addrLineO', 'addrLineT', 'addrLineU', 'itCompanyProvCd', 'itPostalAddrss', 'isAddressUseA', 'isAddressUseB', 'isAddressUseC', 'isAddressUseD','isAddressUseE', 'isAddressUseF', 'isAddressUseG', 'isAddressUseH'];
var globalRetrieve = function(){
  
}

var globalSaveAll = function(){
  
}
app.controller('CorrectionsController', [ '$scope', '$document', '$http', '$timeout', function($scope, $document, $http, $timeout) {

  
  $scope.countries = [];
  $scope.model = {
      reqId : '',
      cmrNo : '',
      cmrIssuingCntry : ''
  };
  
  $scope.getCountries = function() {
    var ret = cmr.query('LEGACY.SEARCH.CNTRY', {
      _qall : 'Y'
    });
    if (ret && ret.length > 0) {
      ret.forEach(function(item, i) {
        $scope.countries.push({
          id : item.ret1,
          name : item.ret2
        });
      });
    }
  };

  $scope.loadAnother = function() {
    $scope.current = null;
  }
  $scope.retrieveDetails = function() {
    if ($scope.model.correctionType == 'R') {
      if (!$scope.model.reqId) {
        cmr.showAlert('Please input the Request ID.');
        return;
      }
      if (isNaN($scope.model.reqId)){
        cmr.showAlert('Request ID should be numeric.');
        return;
      }
      $scope.model.reqId = $scope.model.reqId.trim();
    }
    if ($scope.model.correctionType == 'L') {
      if (!$scope.model.cmrNo || !$scope.model.cmrIssuingCntry) {
        cmr.showAlert('Please input the Issuing Country and CMR No.');
        return;
      }
      if (isNaN($scope.model.cmrNo) || isNaN($scope.model.cmrIssuingCntry)){
        cmr.showAlert('Issuing Country and CMR No. should be numeric.');
        return;
      }
      $scope.model.cmrNo = $scope.model.cmrNo.trim();
      $scope.model.cmrIssuingCntry = $scope.model.cmrIssuingCntry.trim();
    }

    var input = JSON.parse(JSON.stringify($scope.model));
    input.processType = 'R';
    console.log(input);
    cmr.showProgress('Retrieving record details..');
    $http.post(cmr.CONTEXT_ROOT + '/corrections/process.json', input).then(function(response) {
      cmr.hideProgress();
      console.log(response.data);
      if (response.data && response.data.success) {
        var data = response.data;
        $scope.fieldMap = data.fieldMap;
        $scope.current = data.model;
        if ($scope.model.correctionType == 'R') {
          if (!data.model.admin){
            cmr.showAlert('Request '+$scope.model.reqId+' does not exist.');
            $scope.current = null;
            return;
          }
          $scope.layoutRequest();
        } else {
          if (!data.model.cust){
            cmr.showAlert('CMR '+$scope.model.cmrNo+' does not exist.');
            $scope.current = null;
            return;
          }
          $scope.layoutCMR();
        }
      } else {
        var msg = 'An error occurred when retrieving values. Please try again later or contact your system administrator.';
        cmr.showAlert(msg);
      }
    }, function(error) {
      cmr.hideProgress();
      var msg = 'An error occurred when retrieving values. Please try again later or contact your system administrator.';
      cmr.showAlert(msg);
    });
  };
  
  globalRetrieve = $scope.retrieveDetails;

  $scope.layoutRequest = function() {
    $scope.admin = [];
    for ( var prop in $scope.current.admin) {
      var fName = $scope.fieldMap[prop];
      if ($scope.current.admin.hasOwnProperty(prop) && fName) {
        $scope.admin.push({
          prop : prop,
          field : fName,
          value : $scope.current.admin[prop],
          curr : $scope.current.admin[prop],
          noEdit : NO_EDIT.indexOf(fName) >= 0
        });
      }
    }
    $scope.data = [];
    for ( var prop in $scope.current.data) {
      var fName = $scope.fieldMap[prop];
      if ($scope.current.data.hasOwnProperty(prop) && fName) {
        $scope.data.push({
          prop : prop,
          field : fName,
          value : $scope.current.data[prop],
          curr : $scope.current.data[prop],
          noEdit : NO_EDIT.indexOf(fName) >= 0
        });
      }
    }
    $scope.addresses = [];
    for (var i = 0; i < $scope.current.addresses.length; i++) {
      var currAddr = $scope.current.addresses[i];
      var addr = [];
      addr.push({
        prop : 'addrType',
        field : 'ADDR_TYPE',
        value : currAddr.id.addrType,
        curr : currAddr.id.addrType,
        noEdit : true
      });
      addr.push({
        prop : 'addrSeq',
        field : 'ADDR_SEQ',
        value : currAddr.id.addrSeq,
        curr : currAddr.id.addrSeq,
        noEdit : false
      });
      for (var j = 0; j < ADDR_FIELDS.length; j++) {
        var prop = ADDR_FIELDS[j];
        var fName = $scope.fieldMap[prop];
        if (fName) {
          addr.push({
            prop : prop,
            field : fName,
            value : currAddr[prop],
            curr : currAddr[prop],
            noEdit : NO_EDIT.indexOf(fName) >= 0
          });
        }
      }
      $scope.addresses.push(addr);
    }
  };

  $scope.layoutCMR = function() {
    $scope.cust = [];
    for ( var prop in $scope.current.cust) {
      var fName = $scope.fieldMap[prop];
      if ($scope.current.cust.hasOwnProperty(prop) && fName) {
        $scope.cust.push({
          prop : prop,
          field : fName,
          value : $scope.current.cust[prop],
          curr : $scope.current.cust[prop],
          noEdit : NO_EDIT.indexOf(fName) >= 0
        });
      }
    }
    $scope.custExt = [];
    for ( var prop in $scope.current.custExt) {
      var fName = $scope.fieldMap[prop];
      if ($scope.current.custExt.hasOwnProperty(prop) && fName) {
        $scope.custExt.push({
          prop : prop,
          field : fName,
          value : $scope.current.custExt[prop],
          curr : $scope.current.custExt[prop],
          noEdit : NO_EDIT.indexOf(fName) >= 0
        });
      }
    }
    $scope.custAddresses = [];
    for (var i = 0; i < $scope.current.custAddresses.length; i++) {
      var currAddr = $scope.current.custAddresses[i];
      var addr = [];
      addr.push({
        prop : 'addrNo',
        field : 'ADDRNO',
        value : currAddr.id.addrNo,
        curr : currAddr.id.addrNo,
        noEdit : false
      });
      for (var j = 0; j < CMR_ADDR_FIELDS.length; j++) {
        var prop = CMR_ADDR_FIELDS[j];
        var fName = $scope.fieldMap[prop];
        if (fName) {
          addr.push({
            prop : prop,
            field : fName,
            value : currAddr[prop],
            curr : currAddr[prop],
            noEdit : NO_EDIT.indexOf(fName) >= 0
          });
        }
      }
      $scope.custAddresses.push(addr);
    }
  };
  
  
  $scope.saveAll = function() {
    var input = JSON.parse(JSON.stringify($scope.model));
    input.processType = 'C';
    if ($scope.model.correctionType == 'R') {
      var admin = {};
      for (var i = 0; i < $scope.admin.length; i++) {
        var curr = $scope.admin[i];
        if (curr.value != curr.curr) {
          admin[curr.prop] = curr.value ? curr.value : '#';
        }
      }
      input.admin = admin;
      var data = {};
      for (var i = 0; i < $scope.data.length; i++) {
        var curr = $scope.data[i];
        if (curr.value != curr.curr) {
          data[curr.prop] = curr.value ? curr.value : '#';
        }
      }
      input.data = data;
      var addresses = [];
      for (var i = 0; i < $scope.addresses.length; i++) {
        var addr = {
          id : {}
        };
        var hasChange = false;
        var currAddr = $scope.addresses[i];
        for (var j = 0; j < currAddr.length; j++) {
          var curr = currAddr[j];
          if (curr.prop == 'addrType' || curr.prop == 'addrSeq') {
            addr.id[curr.prop] = curr.curr;
            if (curr.prop == 'addrSeq' && curr.value != curr.curr) {
              addr['newAddrSeq'] = curr.value;
              hasChange = true;
            }
          } else {
            if (curr.value != curr.curr) {
              addr[curr.prop] = curr.value ? curr.value : '#';
              hasChange = true;
            }
          }
        }
        if (hasChange){
          addresses.push(addr);
        }
      }
      input.addresses = addresses;
    } else {
      var cust = {};
      for (var i = 0; i < $scope.cust.length; i++) {
        var curr = $scope.cust[i];
        if (curr.value != curr.curr) {
          cust[curr.prop] = curr.value ? curr.value : '#';
        }
      }
      input.cust = cust;
      var custExt = {};
      for (var i = 0; i < $scope.custExt.length; i++) {
        var curr = $scope.custExt[i];
        if (curr.value != curr.curr) {
          custExt[curr.prop] = curr.value ? curr.value : '#';
        }
      }
      input.custExt = custExt;
      var custAddresses = [];
      for (var i = 0; i < $scope.custAddresses.length; i++) {
        var addr = {
          id : {}
        };
        var hasChange = false;
        var currAddr = $scope.custAddresses[i];
        for (var j = 0; j < currAddr.length; j++) {
          var curr = currAddr[j];
          if (curr.prop == 'addrNo') {
            addr.id[curr.prop] = curr.curr;
            if (curr.prop == 'addrNo' && curr.value != curr.curr) {
              addr['newAddrSeq'] = curr.value;
              hasChange = true;
            }
          } else {
            if (curr.value != curr.curr) {
              addr[curr.prop] = curr.value ? curr.value : '#';
              hasChange = true;
            }
          }
        }
        if (hasChange){
          custAddresses.push(addr);
        }
      }
      input.custAddresses = custAddresses;
    }
    console.log(input);
    
    
    cmr.showProgress('Applying data corrections..');
    $http.post(cmr.CONTEXT_ROOT + '/corrections/process.json', input).then(function(response) {
      cmr.hideProgress();
      console.log(response.data);
      console.log(response.data);
      if (response.data && response.data.success) {
        var msg = 'The corrections were applied successfully on the records. The page will be refreshed with the latest values.';
        cmr.showAlert(msg, 'Success', 'globalRetrieve()', true);
      } else {
        var msg = 'An error occurred when saving the values. Please try again later or contact your system administrator.';
        cmr.showAlert(msg);
      }
    }, function(error) {
      cmr.hideProgress();
      var msg = 'An error occurred when saving the values. Please try again later or contact your system administrator.';
      cmr.showAlert(msg);
    });
    
    
  }
  
  globalSaveAll = $scope.saveAll;

  $scope.getCountries();
} ]);

function confirmSave(){
  var msg = 'The changes marked on the record(s) will be applied and necessary change logs will be created to track the updates.<br><br>';
  msg += 'If you are saving values which cannot be selected via normal process on the UI, please consider doing a Forced Status Change ';
  msg += 'to <strong>Processing Create/Updt Pending</strong> after to ensure the data updates are not overwritten.<br><br>Proceed?';
  cmr.showConfirm('globalSaveAll()',msg,'Confirm');
}
