var app = angular.module('ScenariosApp', [ 'ngRoute' ]);

app.controller('ScenariosController', [ '$scope', '$document', '$http', '$timeout', function($scope, $document, $http, $timeout) {

  $scope.loaded = false;
  var results = cmr.query('SCENARIOS.ISSUING_CNTRY', {
    _qall : 'Y'
  });

  // fields
  $scope.allFields = SCENARIO_FIELDS;
  $scope.allFields.sort(scenariofieldSort);
  $scope.fieldMap = {};
  $scope.allFields.forEach(function(field, i) {
    field.internalId = field.id + (field.address ? '_addr' : '');
    $scope.fieldMap[field.internalId] = field;
  });

  // scenario info
  $scope.countries = new Array();
  $scope.types = new Array();
  $scope.addrTypes = new Array();
  $scope.addrMap = {};
  $scope.subtypes = new Array();
  if (results != null && results.length > 0) {
    results.forEach(function(result, index) {
      $scope.countries.push({
        id : result.ret1,
        name : result.ret2
      });
    });
  }

  $scope.getTypes = function(cntry) {

    if (cntry == '') {
      return;
    }

    $scope.custType = '';
    var types = cmr.query('SCENARIOS.TYPES', {
      _qall : 'Y',
      CNTRY : cntry
    });
    $scope.types = new Array();
    if (types != null && types.length > 0) {
      types.forEach(function(type, index) {
        $scope.types.push({
          id : type.ret1,
          name : type.ret2
        });
      });
    } else {
      $scope.types.push({
        id : '',
        name : 'No scenarios defined'
      });
    }
  };

  $scope.getSubTypes = function(cntry, type) {

    if (cntry == '' || type == '') {
      return;
    }
    var types = cmr.query('SCENARIOS.SUBTYPES', {
      _qall : 'Y',
      CNTRY : cntry,
      TYPE : type
    });
    $scope.subtypes = new Array();
    if (types != null && types.length > 0) {
      types.forEach(function(type, index) {
        $scope.subtypes.push({
          id : type.ret1,
          name : type.ret2
        });
      });
    } else {
      $scope.subtypes.push({
        id : '',
        name : 'No scenarios defined'
      });
    }
  };

  $scope.loadScenario = function(cntry, type, subtype) {
    cmr.showProgress();
    $timeout(function() {
      loadScenarioFinal(cntry, type, subtype);
    }, 500);
  };
  var loadScenarioFinal = function(cntry, type, subtype) {
    $scope.scenarios = new Array();

    var atypes = cmr.query('SCENARIOS.ADDRESS_TYPES', {
      _qall : 'Y',
      CNTRY : cntry
    });

    if (atypes.length == 0) {
      atypes = cmr.query('SCENARIOS.ADDRESS_TYPES.GEN', {
        _qall : 'Y'
      });
    }
    $scope.addrTypes = new Array();
    $scope.addrMap = {};
    atypes.forEach(function(a, t) {
      $scope.addrTypes.push({
        id : a.ret1,
        name : a.ret2
      });
    });

    $scope.addrTypes.forEach(function(type, i) {
      $scope.addrMap[type.id] = type.name;
    });
    var records = cmr.query('SCENARIOS.LOAD', {
      _qall : 'Y',
      CNTRY : cntry,
      SCENARIO : subtype
    });
    if (records != null && records.length > 0) {
      var currentField = '';
      var field = null;
      records.forEach(function(record, index) {
        if (currentField == '' || (currentField != (record.ret1 + '|' + record.ret2))) {
          if (field != null) {
            $scope.updateTags(field);
            $scope.scenarios.push(field);
          }
          field = {
            fieldId : record.ret1,
            internalId : record.ret2.trim() != 'XXX' ? record.ret1 + '_addr' : record.ret1,
            addrTyp : record.ret2 == 'XXX' ? '' : record.ret2,
            values : [],
            retainValInd : record.ret4 == 'Y' ? true : false,
            reqInd : record.ret5 == 'Y' ? 'R' : (record.ret5 == 'N' ? 'O' : record.ret5),
            lockedIndc : record.ret6,
            existing : true
          };
          if (record.ret3 != null && record.ret3.trim() != '') {
            field.values.push(record.ret3);
          }
        } else {
          if (record.ret3 != null && record.ret3.trim() != '') {
            field.values.push(record.ret3);
          }
        }
        currentField = record.ret1 + '|' + record.ret2;
      });
      if (field != null) {
        $scope.updateTags(field);
        $scope.scenarios.push(field);
      }
    }
    $scope.loaded = true;
    cmr.hideProgress();
  };

  $scope.selectScenario = function() {
    if (!confirm('Selecting another scenario will unload the current scenario and discard any changes made which were not saved. Proceed?')) {
      return;
    }
    $scope.loaded = false;
  };

  $scope.addValue = function(scenario) {

    cmr.showNode('addValueDiv');
    centerDiv('addValueDiv');
    var field = $scope.fieldMap[scenario.internalId];
    if (field && field.address) {
      cmr.showNode('addValueAddrTypeRow');
      dojo.byId('addValueType').value = scenario.addrTyp;
    } else {
      cmr.hideNode('addValueAddrTypeRow');
    }
    $scope.updateTags(scenario);
    $scope.currentScenario = scenario;
  };

  $scope.hideAddValue = function() {
    dojo.byId('addValueVal').value = '';
    dojo.byId('addValueType').value = '';
    cmr.hideNode('addValueDiv');
    $scope.currentScenario = null;
  };

  $scope.saveValue = function() {
    var scenario = $scope.currentScenario;
    if (!scenario) {
      $scope.hideAddValue();
      return;
    }
    var field = $scope.fieldMap[scenario.internalId];
    if (!field) {
      $scope.hideAddValue();
      return;
    }
    var value = dojo.byId('addValueVal').value;
    if ((!value || value.trim() == '') && !field.address) {
      alert('Please input a value.');
      return;
    }
    var type = dojo.byId('addValueType').value;
    if (field.address) {
      if (!type || type.trim() == '') {
        alert('Please input the address type.');
        return;
      }
    }
    if (scenario.addrTyp == '' && field.address) {
      scenario.addrTyp = type;
    }
    if (value.trim() != '') {
      scenario.values.push(value);
    }
    $scope.hideAddValue();
  };

  $scope.removeValue = function(scenario, value) {
    if (!confirm('Remove ' + value + '?')) {
      return;
    }
    var i = -1;
    if (scenario.values != null) {
      scenario.values.forEach(function(val, index) {
        if (val == value) {
          i = index;
        }
      });
    }
    if (i >= 0) {
      scenario.values.splice(i, 1);
    }
    $scope.updateTags(scenario);
  };

  $scope.updateTags = function(scenario) {
    var len = scenario.values.length;
    if (len == 0) {
      scenario.lockedIndc = '';
      scenario.retainValInd = false;
    } else if (len == 1) {
      scenario.reqInd = 'R';
    } else {
      scenario.lockedIndc = '';
      scenario.retainValInd = false;
      scenario.reqInd = 'R';
    }
  };

  $scope.removeRow = function(scenario) {
    if (!confirm('Remove the row for ' + scenario.fieldId + '?')) {
      return;
    }
    var i = -1;
    if ($scope.scenarios != null) {
      $scope.scenarios.forEach(function(s, index) {
        if (s.internalId == scenario.internalId && s.addrTyp == scenario.addrTyp) {
          i = index;
        }
      });
    }
    if (i >= 0) {
      $scope.scenarios.splice(i, 1);
    }
    if (!scenario.existing) {
      $scope.pending = false;
    }
  };

  $scope.addRow = function() {
    var field = {
      fieldId : '',
      internalId : '',
      addrTyp : '',
      values : [],
      retainValInd : false,
      reqInd : '',
      lockedIndc : '',
      existing : false
    };

    $scope.scenarios.push(field);

    $scope.pending = true;
  };

  $scope.commitRow = function(scenario) {
    if (scenario.internalId == '') {
      alert('Please select a field');
      return;
    }
    var field = $scope.fieldMap[scenario.internalId];
    if (field != null) {
      scenario.fieldId = field.id;
    }
    var found = false;
    var len = $scope.scenarios.length;
    $scope.scenarios.forEach(function(s, i) {
      if (s.internalId == scenario.internalId && s.addrTyp == scenario.addrTyp && i < len - 1) {
        if (!found) {
          alert('This field is already part of the scenarios. Please choose another field or add value to the existing field.');
        }
        found = true;
      }
    });
    if (found) {
      return;
    }
    scenario.existing = true;
    $scope.pending = false;
  };

  $scope.addType = function(cntry) {
    cmr.showNode('addTypeDiv');
    centerDiv('addTypeDiv');
    $scope.addMode = 'T';
  };
  $scope.addSubType = function(cntry, type) {
    cmr.showNode('addTypeDiv');
    centerDiv('addTypeDiv');
    $scope.addMode = 'S';
  };

  $scope.successAddType = function() {
    if ($scope.addMode == 'T') {
      $scope.custSubType = '';
      $scope.custType = '';
      $scope.getTypes($scope.issuingCntry);
    } else if ($scope.addMode == 'S') {
      $scope.custSubType = '';
      $scope.getSubTypes($scope.issuingCntry, $scope.custType);
    }
    $scope.hideAddType();
  };

  $scope.hideAddType = function() {
    dojo.byId('typeCode').value = '';
    dojo.byId('typeName').value = '';
    cmr.hideNode('addTypeDiv');
    $scope.addMode = null;
  };

  $scope.saveType = function() {
    var type = dojo.byId('typeCode').value;
    if (type.trim() == '') {
      alert('Please specify the Code.');
      return;
    }
    var name = dojo.byId('typeName').value;
    if (name.trim() == '') {
      alert('Please specify the Name.');
      return;
    }

    if ('T' == $scope.addMode) {
      var ret = cmr.query('SCENARIOS.CHECKTYPE', {
        cntry : $scope.issuingCntry,
        code : type.toUpperCase().trim(),
        desc : name.toUpperCase().trim()
      });
      if (ret != null && ret.ret1 != null) {
        alert('This code/name is already registered under ' + ret.ret1 + ' - ' + ret.ret2);
        return;
      }
    } else if ('S' == $scope.addMode) {
      var ret = cmr.query('SCENARIOS.CHECKSUBTYPE', {
        cntry : $scope.issuingCntry,
        type : $scope.custType,
        code : type.toUpperCase().trim(),
        desc : name.toUpperCase().trim()
      });
      if (ret != null && ret.ret1 != null) {
        alert('This code/name is already registered under ' + ret.ret1 + ' - ' + ret.ret2);
        return;
      }
    } else {
      alert('No mode specified.');
      return;
    }
    $scope.process('ADD_TYPE', {
      issuingCntry : $scope.issuingCntry,
      custTypeVal : $scope.custType,
      custSubTypeVal : $scope.custSubType,
      addMode : $scope.addMode,
      code : type.toUpperCase().trim(),
      desc : name.trim()
    }, 'Scenario ' + ($scope.addMode == 'T' ? 'Type' : 'Sub-type') + ' added successfully.', $scope.successAddType);
  };

  $scope.saveScenarios = function() {
    var error = '';
    var count = 0;
    $scope.scenarios.forEach(function(s, i) {
      var field = $scope.fieldMap[s.internalId];
      if (field != null) {
        if (field.address && s.addrTyp == '') {
          error += error == '' ? '\n' : '';
          error += field.lbl;
          count++;
        }
        s.fieldName = field.name;
        s.tabId = field.tab;
      }
    });

    if (count > 0) {
      alert('Address fields have no defined address types:' + error);
      return;
    }
    $scope.process('SAVE_SCENARIO', {
      issuingCntry : $scope.issuingCntry,
      custTypeVal : $scope.custType,
      custSubTypeVal : $scope.custSubType,
      fields : $scope.scenarios
    }, 'Scenario saved successfully.');
  };

  $scope.deleteScenario = function() {
    if (!confirm('This will delete the scenario fields as well as the sub type and cannot be undone. Proceed?')) {
      return;
    }
    var ret = cmr.query('SCENARIOS.CHECKUSE', {
      cntry : $scope.issuingCntry,
      code : $scope.custSubType
    });
    if (ret != null && ret.ret1 == 'Y') {
      alert('This scenario is active and used currently. This cannot be deleted.');
      return;
    }
    $scope.process('DELETE_SCENARIO', {
      issuingCntry : $scope.issuingCntry,
      custTypeVal : $scope.custType,
      custSubTypeVal : $scope.custSubType
    }, 'Field deleted successfully.', $scope.afterDelete);
  };

  $scope.afterDelete = function() {
    $scope.loaded = false;
    $scope.getTypes($scope.issuingCntry);
    $scope.custSubType = '';
  };

  $scope.propagate = function(field) {
    if (!confirm('This will copy this field to scenarios without this field defined. Proceed?')) {
      return;
    }
    var field1 = $scope.fieldMap[field.internalId];
    if (field1 != null) {
      field.fieldName = field1.name;
      field.tabId = field1.tab;
    }

    $scope.process('PROPAGATE', {
      issuingCntry : $scope.issuingCntry,
      custTypeVal : $scope.custType,
      custSubTypeVal : $scope.custSubType,
      fields : [ field ]
    }, 'Field propagated successfully.');
  };

  $scope.process = function(action, jsonObject, message, endFunc) {
    console.log(jsonObject);
    cmr.showProgress('Processing...');
    $http({
      url : cmr.CONTEXT_ROOT + '/code/scenarios/process.json',
      method : 'POST',
      params : {
        action : action,
        jsonInput : JSON.stringify(jsonObject)
      }
    }).then(function(response) {
      cmr.hideProgress();
      if (response.data && response.data.result && response.data.result.success) {
        $scope.existing = true;
        alert(message);
        if (endFunc) {
          endFunc();
        }
      } else {
        alert('An error occurred while processing.');
      }
    }, function(response) {
      cmr.hideProgress();
      console.log('error: ');
      console.log(response);
      alert('An error occurred while processing.');
    });
  };

} ]);

function centerDiv(id) {
  var div = document.getElementById(id);
  var screenH = Math.max(document.body.scrollHeight, document.body.offsetHeight, window.screen.height);
  screenH = window.innerHeight;
  var h = div.clientHeight;
  var scroll = 0;
  if (document.documentElement.scrollTopMax > 0) {
    scroll = document.documentElement.scrollTop;
  }
  var less = 0;
  if (screenH > 500) {
    less = 100;
  }
  div.style.top = (((screenH - h) / 2) + (scroll) - less) + 'px';

  var screenW = Math.max(document.body.scrollWidth, document.body.offsetWidth, window.screen.width);
  screenW = window.innerWidth;
  var w = div.clientWidth;
  var scrollW = 0;
  if (document.documentElement.scrollLeftMax > 0) {
    scrollW = document.documentElement.scrollLeft;
  }
  div.style.left = (((screenW - w) / 2) + (scrollW)) + 'px';
}
