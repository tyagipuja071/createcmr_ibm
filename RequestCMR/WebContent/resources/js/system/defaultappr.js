var app = angular.module('DefaultApprovals', [ 'ngRoute' ]);
app.service('ConditionsService', function() {
  var addrConditions = [];
  var dataConditions = [];
  var recipients = [];

  return {
    getAddrConditions : function() {
      return addrConditions;
    },
    setAddrConditions : function(conditions) {
      addrConditions = conditions;
    },
    getDataConditions : function() {
      return dataConditions;
    },
    setDataConditions : function(conditions) {
      dataConditions = conditions;
    },
    getRecipients : function() {
      return recipients;
    },
    setRecipients : function(recipientList) {
      recipients = recipientList;
    }
  };
});
app.directive('numbersonly', function() {
  return {
    require : 'ngModel',
    link : function(scope, element, attrs, modelCtrl) {
      var capitalize = function(inputValue) {
        if (inputValue == undefined)
          inputValue = '';
        var capitalized = inputValue.toUpperCase();
        capitalized = capitalized.replace(/[^0-9]/g, '');
        if (capitalized !== inputValue) {
          modelCtrl.$setViewValue(capitalized);
          modelCtrl.$render();
        }
        return capitalized;
      };
      modelCtrl.$parsers.push(capitalize);
      capitalize(scope[attrs.ngModel]); // capitalize initial value
    }
  };
});

app.controller('Preview', [ '$scope', '$document', '$http', '$timeout', 'ConditionsService', function($scope, $document, $http, $timeout, ConditionsService) {

  $scope.addrConditions = [];
  $scope.dataConditions = [];
  $scope.multipleDataCondLevels = [];
  $scope.multipleAddrCondLevels = [];
  $scope.recipients = [];
  $scope.reqType = _pagemodel ? _pagemodel.requestTyp : FormManager.getActualValue('requestTyp');
  $scope.showPrev = false;
  $scope.showPreview = function() {
    $scope.multipleAddrCondLevels = [];
    $scope.multipleDataCondLevels = [];
    $scope.addrConditions = ConditionsService.getAddrConditions();
    $scope.dataConditions = ConditionsService.getDataConditions();
    $scope.recipients = ConditionsService.getRecipients();

    var count = 0;
    var currLevel = 0;
    $scope.dataConditions.forEach(function(cond, i) {

      if (currLevel != cond.conditionLevel) {
        if (count > 1) {
          $scope.multipleDataCondLevels.push(currLevel);
        }
        currLevel = cond.conditionLevel;
        count = 1;
      } else {
        count++;
      }
    });
    if (count > 1) {
      $scope.multipleDataCondLevels.push(currLevel);
    }

    count = 0;
    currLevel = 0;
    $scope.addrConditions.forEach(function(cond, i) {

      if (currLevel != cond.conditionLevel) {
        if (count > 1) {
          $scope.multipleAddrCondLevels.push(currLevel);
        }
        currLevel = cond.conditionLevel;
        count = 1;
      } else {
        count++;
      }
    });
    if (count > 1) {
      $scope.multipleAddrCondLevels.push(currLevel);
    }

    $scope.multiplesAddedA = [];
    $scope.multiplesAddedB = [];
    $scope.showPrev = true;
  };
  $scope.hidePreview = function() {
    $scope.showPrev = false;
  };
} ]);

/*
 * Controller for the Recipients list
 */
app.controller('Recipients', [ '$scope', '$document', '$http', '$timeout', 'ConditionsService', function($scope, $document, $http, $timeout, ConditionsService) {
  $scope.recipients = [];
  $scope.loading = true;
  $scope.getRecipients = function() {
    $scope.recipients = [];
    $scope.loading = true;
    $http({
      url : cmr.CONTEXT_ROOT + '/query/SYSTEM.GET_APPR_RECIPIENTS.json',
      method : 'GET',
      params : {
        _qall : 'Y',
        ID : FormManager.getActualValue('defaultApprovalId')
      }
    }).then(function(response) {
      $scope.loading = false;
      if (response && response.data && response.data.result) {
        var result = null;
        for (var i = 0; i < response.data.result.length; i++) {
          result = response.data.result[i];
          $scope.recipients.push({
            defaultApprovalId : parseInt(result.ret1),
            intranetId : result.ret2,
            notesId : result.ret3,
            displayName : result.ret4,
            newEntry : false,
            seq : i + 1
          });
        }
        ConditionsService.setRecipients($scope.recipients);
      }
    }, function(response) {
      $scope.loading = false;
      console.log(response);
    });

  };

  $scope.removeRecipient = function(recipient) {
    if (!confirm('Remove ' + recipient.displayName + ' from the list?')) {
      return;
    }
    var processParam = recipient;
    processParam.action = 'REMOVE_RECIPIENT';
    cmr.showProgress('Processing..');
    $http({
      url : cmr.CONTEXT_ROOT + '/code/defaultappr/recipients.json',
      method : 'POST',
      params : processParam
    }).then(function(response) {
      cmr.hideProgress();
      if (response && response.data && response.data.success) {
        showSuccessNotif();
        $scope.getRecipients();
      } else {
        if (response && response.data && response.data.error) {
          cmr.showAlert('Error: ' + response.data.error);
        } else {
          cmr.showAlert('A general error occurred when processing.');
        }
      }
    }, function(response) {
      cmr.hideProgress();
      cmr.showAlert('A general error occurred when processing.');
    });
  };

  $scope.addRecipient = function() {
    var recipient = {
      intranetId : '',
      displayName : '',
      notesId : '',
      newEntry : true,
      seq : $scope.recipients.length + 1,
      defaultApprovalId : parseInt(FormManager.getActualValue('defaultApprovalId'))
    };
    $scope.recipients.push(recipient);
    window.setTimeout('registerBPInput(' + recipient.seq + ')', 1000);
  };

  $scope.addBPManager = function() {
    for (var i = 0; i < $scope.recipients.length; i++) {
      if ($scope.recipients[i].intranetId == 'BPMANAGER') {
        cmr.showAlert('Blue Pages Manager already added as recipient.');
        return;
      }
    }
    var recipient = {
      intranetId : 'BPMANAGER',
      displayName : 'Blue Pages Manager of Requester',
      notesId : 'BPMANAGER',
      newEntry : true,
      seq : $scope.recipients.length + 1,
      defaultApprovalId : parseInt(FormManager.getActualValue('defaultApprovalId'))
    };
    var processParam = recipient;
    processParam.action = 'ADD_RECIPIENT';
    cmr.showProgress('Processing..');
    $http({
      url : cmr.CONTEXT_ROOT + '/code/defaultappr/recipients.json',
      method : 'POST',
      params : processParam
    }).then(function(response) {
      cmr.hideProgress();
      if (response && response.data && response.data.success) {
        showSuccessNotif();
        // recipient.newEntry = false;
        $scope.getRecipients();
      } else {
        if (response && response.data && response.data.error) {
          cmr.showAlert('Error: ' + response.data.error);
        } else {
          cmr.showAlert('A general error occurred when processing.');
        }
      }
    }, function(response) {
      cmr.hideProgress();
      cmr.showAlert('A general error occurred when processing.');
    });
    // $scope.recipients.push(recipient);
  };

  $scope.saveRecipient = function(recipient) {
    var name = dojo.byId('recipient_' + recipient.seq).value;
    var intranetId = dojo.byId('recipientId_' + recipient.seq).value;
    var notesId = dojo.byId('recipientNotesId_' + recipient.seq).value;
    recipient.displayName = name;
    recipient.intranetId = intranetId;
    recipient.notesId = notesId;
    if (recipient.displayName == null || recipient.displayName.trim() == '') {
      cmr.showAlert('Name is required.');
      return;
    }
    if (recipient.intranetId == null || recipient.intranetId.trim() == '') {
      cmr.showAlert('Intranet ID is required.');
      return;
    }
    if (recipient.notesId == null || recipient.notesId.trim() == '') {
      cmr.showAlert('Notes ID is required.');
      return;
    }
    var processParam = recipient;
    processParam.action = 'ADD_RECIPIENT';
    cmr.showProgress('Processing..');
    $http({
      url : cmr.CONTEXT_ROOT + '/code/defaultappr/recipients.json',
      method : 'POST',
      params : processParam
    }).then(function(response) {
      cmr.hideProgress();
      if (response && response.data && response.data.success) {
        showSuccessNotif();
        recipient.newEntry = false;
        $scope.getRecipients();
      } else {
        if (response && response.data && response.data.error) {
          cmr.showAlert('Error: ' + response.data.error);
        } else {
          cmr.showAlert('A general error occurred when processing.');
        }
      }
    }, function(response) {
      cmr.hideProgress();
      cmr.showAlert('A general error occurred when processing.');
    });
  };

  $scope.getRecipients();
} ]);

app.controller('Conditions', [ '$scope', '$document', '$http', '$timeout', 'ConditionsService', function($scope, $document, $http, $timeout, ConditionsService) {
  $scope.nonAddrConditions = [];
  $scope.addrConditions = [];

  $scope.loadingA = true;
  $scope.loadingB = true;

  $scope.maxLevelNonAddr = 0;
  $scope.maxLevelAddr = 0;

  $scope.getNonAddrConditions = function() {
    $scope.nonAddrConditions = [];
    $scope.loadingA = true;
    $http({
      url : cmr.CONTEXT_ROOT + '/query/SYSTEM.GET_CONDITIONS.NONADDR.json',
      method : 'GET',
      params : {
        _qall : 'Y',
        ID : FormManager.getActualValue('defaultApprovalId')
      }
    }).then(function(response) {
      $scope.loadingA = false;
      if (response && response.data && response.data.result) {
        var result = null;
        for (var i = 0; i < response.data.result.length; i++) {
          result = response.data.result[i];
          if (parseInt(result.ret4) > $scope.maxLevelNonAddr) {
            $scope.maxLevelNonAddr = parseInt(result.ret4);
          }
          $scope.nonAddrConditions.push({
            defaultApprovalId : parseInt(result.ret1),
            sequenceNo : parseInt(result.ret2),
            addrIndc : result.ret3,
            conditionLevel : parseInt(result.ret4),
            field : {
              id : result.ret5,
              name : result.ret6
            },
            operator : result.ret7,
            value : result.ret8,
            previousValueIndc : result.ret9,
            edit : false,
            newEntry : false
          });
        }
        ConditionsService.setDataConditions($scope.nonAddrConditions);
      }
    }, function(response) {
      $scope.loadingA = false;
      console.log(response);
    });
  };

  $scope.getAddrConditions = function() {
    $scope.loadingB = true;
    $scope.addrConditions = [];
    $http({
      url : cmr.CONTEXT_ROOT + '/query/SYSTEM.GET_CONDITIONS.ADDR.json',
      method : 'GET',
      params : {
        _qall : 'Y',
        ID : FormManager.getActualValue('defaultApprovalId')
      }
    }).then(function(response) {
      $scope.loadingB = false;
      if (response && response.data && response.data.result) {
        var result = null;
        for (var i = 0; i < response.data.result.length; i++) {
          result = response.data.result[i];
          if (parseInt(result.ret4) > $scope.maxLevelAddr) {
            $scope.maxLevelAddr = parseInt(result.ret4);
          }
          $scope.addrConditions.push({
            defaultApprovalId : parseInt(result.ret1),
            sequenceNo : parseInt(result.ret2),
            addrIndc : result.ret3,
            conditionLevel : parseInt(result.ret4),
            field : {
              id : result.ret5,
              name : result.ret6
            },
            operator : result.ret7,
            value : result.ret8,
            previousValueIndc : result.ret9,
            edit : false,
            newEntry : false
          });
        }
        ConditionsService.setAddrConditions($scope.addrConditions);
      }
    }, function(response) {
      $scope.loadingB = false;
      console.log(response);
    });
  };

  $scope.addCondition = function(sequence, level, address) {
    var index = -1;
    var toUse = address ? $scope.addrConditions : $scope.nonAddrConditions;
    var condition = null;
    for (var i = 0; i < toUse.length; i++) {
      condition = toUse[i];
      if (condition.sequenceNo == sequence && condition.conditionLevel == level) {
        index = i;
        break;
      }
    }
    if (index >= 0) {
      var newCond = {
        defaultApprovalId : parseInt(FormManager.getActualValue(defaultApprovalId)),
        sequenceNo : toUse.length + 1,
        addrIndc : address ? 'Y' : null,
        conditionLevel : level,
        field : {
          id : '',
          name : ''
        },
        operator : '',
        value : '',
        previousValueIndc : '',
        edit : true,
        newEntry : true
      };
      toUse.splice(index + 1, 0, newCond);
      $scope.addressVars = [ {
        addr : false,
        conditions : $scope.nonAddrConditions
      }, {
        addr : true,
        conditions : $scope.addrConditions
      } ];
    }

  };

  $scope.removeCondition = function(condition, address) {
    if (!confirm('Remove the condition for ' + condition.field.id + '?')) {
      return;
    }
    var processParam = condition;
    processParam.action = 'REMOVE_CONDITION';
    cmr.showProgress('Processing..');
    $http({
      url : cmr.CONTEXT_ROOT + '/code/defaultappr/conditions.json',
      method : 'POST',
      params : processParam
    }).then(function(response) {
      cmr.hideProgress();
      if (response && response.data && response.data.success) {
        showSuccessNotif();
        if (address) {
          $scope.getAddrConditions();
        } else {
          $scope.getNonAddrConditions();
        }
        $scope.addressVars = [ {
          addr : false,
          conditions : $scope.nonAddrConditions
        }, {
          addr : true,
          conditions : $scope.addrConditions
        } ];
      } else {
        cmr.showAlert('An error occurred when saving.');
      }
    }, function(response) {
      cmr.hideProgress();
      cmr.showAlert('An error occurred when saving.');
    });
  };

  $scope.editCondition = function(condition) {
    condition.edit = true;
  };

  $scope.saveCondition = function(condition, address) {
    if (condition.conditionLevel == null || condition.conditionLevel <= 0) {
      cmr.showAlert('Level is required.');
      return;
    }
    if (condition.field == null || condition.field.id == null || condition.field.id.trim() == '') {
      cmr.showAlert('Field ID is required.');
      return;
    }
    if (condition.operator == null || condition.operator.trim() == '') {
      cmr.showAlert('Operator is required.');
      return;
    }
    if ((condition.operator != '$' && condition.operator != '*' && condition.operator != 'CHG') && (condition.value == null || condition.value.trim() == '')) {
      cmr.showAlert('Value is required.');
      return;
    }
    var processParam = {};
    angular.copy(condition, processParam);
    if (condition.operator == '*' || condition.operator == 'CHG' || condition.operator == '$') {
      processParam.value = '';
      condition.value = '';
    }
    var fieldId = condition.field.id;
    var dbName = condition.field.name;
    processParam.fieldId = fieldId;
    processParam.databaseFieldName = dbName;
    processParam.action = condition.newEntry ? 'ADD_CONDITION' : 'UPDATE_CONDITION';
    cmr.showProgress('Processing..');
    $http({
      url : cmr.CONTEXT_ROOT + '/code/defaultappr/conditions.json',
      method : 'POST',
      params : processParam
    }).then(function(response) {
      cmr.hideProgress();
      if (response && response.data && response.data.success) {
        showSuccessNotif();
        condition.sequenceNo = response.data.sequence;
        condition.edit = false;
        condition.newEntry = false;
        if (address) {
          $scope.getAddrConditions();
        } else {
          $scope.getNonAddrConditions();
        }
      } else {
        cmr.showAlert('An error occurred when saving.');
      }
    }, function(response) {
      cmr.hideProgress();
      cmr.showAlert('An error occurred when saving.');
    });
  };

  $scope.addNewCondition = function(address) {
    var toUse = address ? $scope.addrConditions : $scope.nonAddrConditions;
    var newCond = {
      defaultApprovalId : parseInt(FormManager.getActualValue(defaultApprovalId)),
      sequenceNo : toUse.length + 1,
      addrIndc : address ? 'Y' : null,
      conditionLevel : address ? $scope.maxLevelAddr + 1 : $scope.maxLevelNonAddr + 1,
      field : {
        id : '',
        name : ''
      },
      databaseFieldName : '',
      operator : '',
      value : '',
      previousValueIndc : '',
      edit : true,
      newEntry : true
    };
    toUse.push(newCond);
    var arrIndex = address ? 1 : 0;
    $scope.addressVars[arrIndex] = {
      addr : address,
      conditions : toUse
    };
  };

  $scope.cancelCondition = function(condition, address) {
    if (condition.newEntry) {
      var toUse = address ? $scope.addrConditions : $scope.nonAddrConditions;
      var conditionA = null;
      var index = -1;
      for (var i = 0; i < toUse.length; i++) {
        conditionA = toUse[i];
        if (conditionA.sequenceNo == condition.sequenceNo) {
          index = i;
          break;
        }
      }
      if (index >= 0) {
        toUse.splice(index, 1);
      }

      var arrIndex = address ? 1 : 0;
      $scope.addressVars[arrIndex] = {
        addr : address,
        conditions : toUse
      };
    } else {

    }
    condition.edit = true;
  };

  var requestType = FormManager.getActualValue('requestTyp');
  if (requestType == '' && typeof (_pagemodel) != 'undefined') {
    requestType = _pagemodel.requestTyp;
  }
  $scope.requestType = requestType;

  $scope.getNonAddrConditions();
  $scope.getAddrConditions();

  $scope.addrFields = [];
  $scope.dataFields = [];
  switch (requestType) {
  case 'C':
    $scope.addrFields = DB_ADDR_FIELDS;
    $scope.dataFields = DB_DATA_FIELDS;
    break;
  case 'U':
    $scope.addrFields = DB_ADDR_FIELDS;
    $scope.dataFields = DB_DATA_FIELDS;
    break;
  case 'R':
    $scope.dataFields = DB_DELETE_DATA_FIELDS;
    break;
  case 'D':
    $scope.dataFields = DB_DELETE_DATA_FIELDS;
    break;
  case 'M':
    $scope.addrFields = DB_MASS_UPDT_ADDR_FIELDS;
    $scope.dataFields = DB_MASS_UPDT_DATA_FIELDS;
    break;
  case 'N':
    $scope.addrFields = DB_MASS_CREATE_ADDR_FIELDS;
    $scope.dataFields = DB_MASS_CREATE_DATA_FIELDS;
    break;
  case 'E':
    $scope.dataFields = DB_UPDT_BY_ENT_DATA_FIELDS;
    break;
  }

} ]);

function registerBPInput(seq) {
  cmr.setBluePagesSearch('recipient_' + seq, 'recipientId_' + seq, false, 'recipientNotesId_' + seq);
}

function showSuccessNotif() {
  cmr.showNode('approval-notif');
  window.setTimeout('hideSuccessNotif()', 1000);
}

function hideSuccessNotif() {
  cmr.hideNode('approval-notif');
}
