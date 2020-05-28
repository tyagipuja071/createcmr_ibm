var app = angular.module('ConfigApp', [ 'ngRoute', 'ngSanitize' ]);

app.filter('selectCountryFilter', function() {
  return function(items, search) {
    if (!search) {
      return items;
    }

    return items.filter(function(country, index, array) {
      if (country.selected) {
        return true;
      }
      var val = search;
      if (!val) {
        return true;
      }
      if (country.cmrIssuingCntry.indexOf(val) >= 0) {
        return true;
      }
      if (country.name.toUpperCase().indexOf(val.toUpperCase()) >= 0) {
        return true;
      }
      if (country.geo.toUpperCase().indexOf(val.toUpperCase()) >= 0) {
        return true;
      }
      if (country.autoEngineIndc && country.autoEngineIndc.toUpperCase().indexOf(val.toUpperCase()) >= 0) {
        return true;
      }
      if (country.autoEngineIndcText && country.autoEngineIndcText.toUpperCase().indexOf(val.toUpperCase()) >= 0) {
        return true;
      }
      if (country.configId && country.configId.toUpperCase().indexOf(val.toUpperCase()) >= 0) {
        return true;
      }
      return false;
    });

  };
});

app.controller('ConfigController', [ '$scope', '$document', '$http', '$timeout', '$sanitize', '$filter', function($scope, $document, $http, $timeout, $sanitize, $filter) {

  $scope.config = {
    edit : false
  };
  $scope.existing = false;
  $scope.elements = [];
  $scope.countries = [];
  $scope.autoElements = [];
  $scope.mapping = false;
  $scope.mapFilter = '';
  $scope.selectAll = false;
  $scope.processOnCompletionVal = '';
  $scope.editProcessOnCompletion = false;

  $scope.configIds = [];

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

  $scope.getConfig = function(configId) {
    var rec = cmr.getRecord('GET_AUTO_ENGINE_CONFIG', 'AutoConfigDefn', {
      ID : configId
    });
    if (rec && rec.id) {
      $scope.config.configId = rec.id.configId;
      $scope.config.configDefn = rec.configDefn;
      $scope.config.shortDesc = rec.shortDesc;
      $scope.config.configDefnHtml = rec.configDefn.replace(/\n/g, '<br>');
      $scope.config.createBy = rec.createBy;
      $scope.config.lastUpdtBy = rec.lastUpdtBy;
      $scope.config.createTs = moment(new Date(new Number(rec.createTs))).format('YYYY-MM-DD HH:mm:ss');
      $scope.config.lastUpdtTs = moment(new Date(new Number(rec.lastUpdtTs))).format('YYYY-MM-DD HH:mm:ss');

      $scope.existing = true;
      $scope.getAutoElements();
    } else {
      cmr.showAlert('The configuration for ' + configId + ' cannot be loaded.', 'Not Found');
    }
  };

  $scope.getConfigElements = function() {
    var recs = cmr.query('GET_AUTO_ELEMENTS', {
      ID : $scope.config.configId,
      _qall : 'Y'
    });
    if (recs) {
      recs.forEach(function(rec, index) {
        var processDesc = rec.ret4;
        var processType = 'V';
        var nonImportable = false;
        $scope.autoElements.forEach(function(value, index) {
          if (value.processCd == rec.ret4) {
            processDesc = value.processDesc;
            processType = value.processType;
            nonImportable = value.nonImportable;
          }
        });
        $scope.elements.push({
          configId : rec.ret1,
          elementId : rec.ret2,
          execOrd : index + 1,
          processCd : rec.ret4,
          processDesc : processDesc,
          processType : processType,
          processPrefix : rec.ret4.substring(0, rec.ret4.indexOf('_')),
          requestTyp : processType == 'A' ? '*' : rec.ret5,
          actionOnError : !rec.ret6 || rec.ret6 == 'P' ? '' : rec.ret6.trim(),
          overrideDataIndc : rec.ret7 == 'Y' ? true : false,
          stopOnErrorIndc : rec.ret8 == 'Y' ? true : false,
          status : rec.ret9 == '1' ? true : false,
          existing : true,
          nonImportable : nonImportable
        });
      });
    }
  };

  $scope.editConfig = function() {
    $scope.config.edit = true;
  };
  $scope.undoSaveConfig = function() {
    $scope.config.edit = false;
  };

  $scope.saveConfig = function() {
    if (!$scope.config.configId || !$scope.config.shortDesc || !$scope.config.configDefn || $scope.config.configDefn.trim().length == 0) {
      alert('ID, description, and details are required');
      return;
    }
    $scope.config.configId = $scope.config.configId.toUpperCase();
    var notAllowed = $scope.config.configId.match(/[^A-Z_]/);
    if (notAllowed && notAllowed.length > 0) {
      alert('ID can only contain letters and underscores');
      return;
    }
    if ($scope.config.configDefn.length > 500) {
      alert('Details can only be up to 500 characters.');
      return;
    }
    if (!$scope.existing) {
      var rec = cmr.query('CHECK_AUTO_ID', {
        ID : $scope.config.configId
      });
      if (rec && rec.ret1) {
        alert('ID ' + $scope.config.configId + ' is already taken.');
        return;
      }
    }
    if ($scope.config.configDefn.length > 1000) {
      alert('Description of the configuration should be at most 1000 characters long.');
      return;
    }
    cmr.showProgress('Saving configuration..');
    dojo.xhrPost({
      url : cmr.CONTEXT_ROOT + '/auto/config/savedefn.json',
      handleAs : 'json',
      method : 'GET',
      content : {
        configId : $scope.config.configId.toUpperCase(),
        shortDesc : $scope.config.shortDesc,
        configDefn : $scope.config.configDefn,
        copyFrom : $scope.config.copyFrom,
        updateMode : $scope.existing ? 'Y' : null,
      },
      timeout : 50000,
      sync : false,
      load : function(data, ioargs) {
        cmr.hideProgress();
        if (data && data.result && data.result.success) {
          $scope.config.configDefnHtml = $scope.config.configDefn.replace(/\n/g, '<br>');
          $scope.config.edit = false;
          var closeFunc = $scope.existing ? null : 'openEditPage(\'' + $scope.config.configId + '\')';
          cmr.showAlert('Definition saved successfully.', null, closeFunc, true);
        } else {
          var msg = data.result && data.result.message ? data.result.message : 'An error occurred during saving.';
          $scope.config.edit = false;
          cmr.showAlert(msg);
        }
        $scope.$apply();
      },
      error : function(error, ioargs) {
        cmr.hideProgress();
        $scope.config.edit = false;
        var msg = 'An error occurred during saving.';
        cmr.showAlert(msg);
        $scope.$apply();
      }
    });
  };

  $scope.insertConfigDetails = function() {
    var details = '';
    $scope.elements.forEach(function(elem, i) {
      details += details ? '\n' : '';
      details += elem.processDesc.replace(/Global - /gi, '');
    });
    $scope.config.configDefn = $scope.config.configDefn + '\n\nElements:\n' + details;
    $scope.$apply();

  };
  $scope.deleteConfig = function() {
    if ($scope.countries && $scope.countries.length > 0) {
      alert('Countries are currently mapped to the configuration. Remove mapped countries before deleting the configuration.');
      return;
    }
    if (!confirm('Deleting the configuration removes all elements and definitions associated with the engine. The action cannot be undone. Proceed?')) {
      return;
    }
    cmr.showProgress('Deleting configuration..');
    dojo.xhrPost({
      url : cmr.CONTEXT_ROOT + '/auto/config/deleteconfig.json',
      handleAs : 'json',
      method : 'GET',
      content : {
        configId : $scope.config.configId
      },
      timeout : 50000,
      sync : false,
      load : function(data, ioargs) {
        cmr.hideProgress();
        if (data && data.result && data.result.success) {
          cmr.showAlert('Configuration deleted successfully.', null, 'openConfigListPage()', true);
        } else {
          var msg = data.result && data.result.message ? data.result.message : 'An error occurred when deleting the configuration.';
          $scope.config.edit = false;
          cmr.showAlert(msg);
        }
      },
      error : function(error, ioargs) {
        cmr.hideProgress();
        $scope.config.edit = false;
        var msg = 'An error occurred during when deleting the configuration.';
        cmr.showAlert(msg);
        $scope.$apply();
      }
    });
  };

  $scope.removeElement = function(index, rec) {
    if (rec.existing) {
      if (!confirm('Remove element ' + rec.processDesc + '?')) {
        return;
      }
    }
    $scope.elements.splice(index, 1);
    // $scope.$apply();
  };

  $scope.moveElementUp = function(index, rec) {
    var removed = $scope.elements.splice(index - 1, 1);
    $scope.elements.splice(index, 0, removed[0]);
    // $scope.$apply();
  };

  $scope.selectElement = function(index, rec) {
    if (!rec.processCd) {
      alert('Please choose an automation elmement.');
      return;
    }
    rec.existing = true;
    $scope.autoElements.forEach(function(value, index) {
      if (value.processCd == rec.processCd) {
        rec.processDesc = value.processDesc;
        rec.processType = value.processType;
        rec.nonImportable = value.nonImportable;
      }
    });
    if (rec.processType == 'A') {
      rec.actionOnError = 'W';
      rec.requestTyp = '*';
    }
    if (rec.nonImportable) {
      rec.overrideDataIndc = false;
    }
    rec.processPrefix = rec.processCd.substring(0, rec.processCd.indexOf('_'));
  };

  $scope.addElement = function() {
    $scope.elements.push({
      configId : $scope.configId,
      elementId : $scope.elements.length,
      execOrd : $scope.elements.length,
      processCd : '',
      processDesc : '',
      processPrefix : '',
      processType : 'V',
      requestTyp : 'C',
      actionOnError : '',
      overrideDataIndc : false,
      stopOnErrorIndc : false,
      status : true,
      existing : false,
      nonImportable : false
    });
  };

  $scope.getAutoElements = function() {
    dojo.xhrGet({
      url : cmr.CONTEXT_ROOT + '/auto/config/getelems.json',
      handleAs : 'json',
      method : 'GET',
      timeout : 50000,
      sync : true,
      load : function(data, ioargs) {
        if (data && data.items) {
          $scope.autoElements = data.items;
        }
      },
      error : function(error, ioargs) {
        console.log(error);
      }
    });
    $scope.getConfigElements();
    $scope.getCountries();
  };

  $scope.saveElements = function() {
    var error = false;
    $scope.elements.forEach(function(value, index) {
      if (!value.existing) {
        console.log(index + ' has error');
        console.log(value);
        error = true;
      }
    });
    if (error) {
      alert('Please complete the element configuration above. Some rows do not have Process Elements defined.');
      return;
    }

    cmr.showProgress('Saving element configuration..');
    var elems = JSON.parse(JSON.stringify($scope.elements));
    var params = {
      configId : $scope.config.configId,
      action : 'SAVE_CONFIG_ELEMENTS',
      elements : elems
    };

    $http.post(cmr.CONTEXT_ROOT + '/auto/config/saveelems.json', params).then(function(response) {
      cmr.hideProgress();
      var data = response.data;
      if (data && data.result && data.result.success) {
        cmr.showAlert('Elements configuration saved successfully.', null, null, true);
      } else {
        var msg = data.result && data.result.message ? data.result.message : 'An error occurred during saving.';
        cmr.showAlert(msg);
      }
    }, function(error) {
      cmr.hideProgress();
      var msg = 'An error occurred during saving.';
      cmr.showAlert(msg);
    });
  };

  $scope.getCountries = function() {
    $scope.countries = [];
    var recs = cmr.query('GET_AUTO_ENGINE_CNTRY', {
      ID : $scope.config.configId,
      _qall : 'Y'
    });
    if (recs) {
      recs.forEach(function(rec, index) {
        $scope.countries.push({
          cmrIssuingCntry : rec.ret1,
          configId : $scope.config.configId,
          processOnCompletion : rec.ret2,
          name : rec.ret3,
          geo : rec.ret4,
          selected : false,
          enablement : rec.ret5 == 'B' ? 'Full Enabled' : (rec.ret5 == 'R' ? 'Requesters Only' : (rec.ret5 == 'P' ? 'Processors Only' : 'Disabled')),
          exceptions : rec.ret6 == 'Y' ? 'Y' : 'N'
        });
      });
    }
  };

  $scope.toAdd = [];
  $scope.toMap = [];
  $scope.mapCountries = function() {
    $scope.toAdd = [];
    $scope.toMap = [];
    $scope.mapFilter = '';
    var recs = cmr.query('GET_AUTO_GET_COUNTRIES', {
      ID : $scope.config.configId,
      _qall : 'Y'
    });
    if (recs) {
      recs.forEach(function(rec, index) {
        $scope.toAdd.push({
          cmrIssuingCntry : rec.ret1,
          name : rec.ret2,
          geo : rec.ret3
        });
      });
    }
    if ($scope.toAdd.length > 0) {
      $scope.mapping = true;
    }
  };

  $scope.mapCountry = function(index, cntry) {
    var index = -1;
    $scope.toAdd.forEach(function(c, i) {
      if (c.cmrIssuingCntry == cntry.cmrIssuingCntry) {
        index = i;
      }
    });
    if (index >= 0) {
      $scope.toAdd.splice(index, 1);
      $scope.toMap.push(cntry);
      try {
        // $scope.$apply();
      } catch (e) {

      }
    }
  };

  $scope.unmapCountry = function(index, cntry) {
    $scope.toMap.splice(index, 1);
    $scope.toAdd.push(cntry);
    try {
      $scope.$apply();
    } catch (e) {

    }
  };

  $scope.undoMap = function() {
    $scope.toAdd = [];
    $scope.toMap = [];
    $scope.mapFilter = '';
    $scope.mapping = false;
  };

  $scope.assignCountries = function(processOnCompletion) {
    if ($scope.toMap.length == 0) {
      alert('No country chosen to map to the config.');
      return;
    }
    cmr.showProgress('Mapping countries to configuration..');
    var countriesToMap = [];
    $scope.toMap.forEach(function(country, index) {
      countriesToMap.push(country.cmrIssuingCntry);
    });
    var params = {
      configId : $scope.config.configId,
      directive : 'A',
      processOnCompletion : processOnCompletion,
      countries : countriesToMap
    };

    $http.post(cmr.CONTEXT_ROOT + '/auto/config/savemap.json', params).then(function(response) {
      cmr.hideProgress();
      var data = response.data;
      if (data && data.result && data.result.success) {
        $scope.getCountries();
        $scope.undoMap();
        $scope.countryFilter = '';
        $scope.selectAll = false;
        cmr.showAlert('Mappings saved successfully.', null, null, true);
      } else {
        var msg = data.result && data.result.message ? data.result.message : 'An error occurred during saving.';
        cmr.showAlert(msg);
      }
    }, function(error) {
      cmr.hideProgress();
      var msg = 'An error occurred during saving.';
      cmr.showAlert(msg);
    });
  };

  $scope.removeCountries = function() {
    var selectedCount = 0;
    $scope.countries.forEach(function(cntry, index) {
      if (cntry.selected) {
        selectedCount++;
      }
    });
    if (selectedCount == 0) {
      alert('No country selected.');
      return;
    }
    if (!confirm('Remove ' + selectedCount + ' countries from the configuration?')) {
      return;
    }
    cmr.showProgress('Removing countries from configuration..');
    var countriesToMap = [];
    $scope.countries.forEach(function(country, index) {
      if (country.selected) {
        countriesToMap.push(country.cmrIssuingCntry);
      }
    });
    var params = {
      configId : $scope.config.configId,
      directive : 'D',
      processOnCompletion : false,
      countries : countriesToMap
    };

    $http.post(cmr.CONTEXT_ROOT + '/auto/config/savemap.json', params).then(function(response) {
      cmr.hideProgress();
      var data = response.data;
      if (data && data.result && data.result.success) {
        $scope.getCountries();
        $scope.undoMap();
        $scope.countryFilter = '';
        $scope.selectAll = false;
        cmr.showAlert('Mappings removed successfully.', null, null, true);
      } else {
        var msg = data.result && data.result.message ? data.result.message : 'An error occurred during saving.';
        cmr.showAlert(msg);
      }
    }, function(error) {
      cmr.hideProgress();
      var msg = 'An error occurred during saving.';
      cmr.showAlert(msg);
    });
  };

  $scope.updateCountries = function(processOnCompletion) {
    var selectedCount = 0;
    $scope.countries.forEach(function(cntry, index) {
      if (cntry.selected) {
        selectedCount++;
      }
    });
    if (selectedCount == 0) {
      alert('No country selected.');
      return;
    }
    cmr.showProgress('Updating configuration for selected countries..');
    var countriesToMap = [];
    $scope.countries.forEach(function(country, index) {
      if (country.selected) {
        countriesToMap.push(country.cmrIssuingCntry);
      }
    });
    var params = {
      configId : $scope.config.configId,
      directive : 'U',
      processOnCompletion : processOnCompletion,
      countries : countriesToMap
    };

    $http.post(cmr.CONTEXT_ROOT + '/auto/config/savemap.json', params).then(function(response) {
      cmr.hideProgress();
      var data = response.data;
      if (data && data.result && data.result.success) {
        $scope.getCountries();
        $scope.countryFilter = '';
        $scope.selectAll = false;
        cmr.showAlert('Mappings updated successfully.', null, null, true);
      } else {
        var msg = data.result && data.result.message ? data.result.message : 'An error occurred during saving.';
        cmr.showAlert(msg);
      }
    }, function(error) {
      cmr.hideProgress();
      var msg = 'An error occurred during saving.';
      cmr.showAlert(msg);
    });
  };

  $scope.getAvailableConfigIds = function() {
    var recs = cmr.query('GET_ALL_AUTO_IDS', {
      _qall : 'Y'
    });
    if (recs) {
      recs.forEach(function(rec, index) {
        var flatDesc = rec.ret2.replace(/\n/g, ' ');
        if (flatDesc.length > 100) {
          flatDesc = flatDesc.substring(0, 97) + '...';
        }
        $scope.configIds.push({
          id : rec.ret1,
          desc : flatDesc
        });
      });
    }
  };
  $scope.doSelectAll = function(select) {
    $scope.countries.forEach(function(cntry, i) {
      cntry.selected = select;
    });
  };

  $scope.formatProcessOnCompletion = function(processOnCompletion) {
    if (processOnCompletion == 'Y') {
      return 'Yes';
    } else if (processOnCompletion == 'U') {
      return 'Updates Only';
    } else if (processOnCompletion == 'C') {
      return 'Creates Only';
    } else {
      return 'No';
    }
  };
  $scope.changeProcessOnCompletion = function(editProcessOnCompletion) {
    $scope.editProcessOnCompletion = editProcessOnCompletion;
  };

  // initialize
  var configId = $scope.getParameterByName('configId');
  if (configId) {
    $scope.getConfig(configId);
  } else {
    $scope.getAvailableConfigIds();
  }
} ]);

app.controller('ConfigCntryController', [ '$scope', '$document', '$http', '$timeout', '$sanitize', '$filter', function($scope, $document, $http, $timeout, $sanitize, $filter) {

  $scope.countries = [];
  $scope.configIds = [];
  $scope.editEnablement = false;
  $scope.editProcessOnCompletion = false;
  $scope.editMapping = false;

  $scope.getMappedCountries = function() {
    $scope.countries = [];
    var recs = cmr.query('GET_MAPPED_COUNTRIES', {
      _qall : 'Y'
    });
    if (recs) {
      recs.forEach(function(rec, index) {
        var enable = rec.ret3 == 'R' ? 'Requesters Only' : (rec.ret3 == 'P' ? 'Processors Only' : (rec.ret3 == 'B' ? 'Fully Enabled' : 'Disabled'));
        $scope.countries.push({
          cmrIssuingCntry : rec.ret1,
          name : rec.ret2,
          autoEngineIndc : rec.ret3,
          autoEngineIndcText : enable,
          configId : rec.ret4,
          configDefn : rec.ret5,
          geo : rec.ret6,
          // processOnCompletion : rec.ret7 && rec.ret7 == 'Y' ? 'Yes' : 'No',
          processOnCompletion : rec.ret7 ? rec.ret7 : 'N',
          edit : false,
          exceptions : rec.ret8,
          shortDesc : rec.ret9
        });
      });
    }
  };

  $scope.process = {};

  $scope.initProcess = function() {
    $scope.process = {
      saveConfig : false,
      saveEnablement : false,
      saveProcessOnCompletion : true,
      processOnCompletion : '',
      removeCountry : false,
      configId : '',
      autoEngineIndc : '',
      countries : []
    };
  };

  $scope.editCountry = function(country) {
    country.edit = true;
    country.oldConfigId = country.configId;
    country.oldAutoEngineIndc = country.autoEngineIndc;
  };
  $scope.undoEditCountry = function(country) {
    country.edit = false;
    country.configId = country.oldConfigId;
    country.autoEngineIndc = country.oldAutoEngineIndc;
  };
  $scope.deleteCountry = function(country) {
    if (!confirm('Unmap ' + country.name + ' from automation engines?')) {
      return;
    }
    $scope.initProcess();
    $scope.process.saveEnablement = false;
    $scope.process.saveProcessOnCompletion = false;
    $scope.process.saveConfig = false;
    $scope.process.removeCountry = true;
    $scope.process.countries.push(country.cmrIssuingCntry);
    $scope.saveAllChanges(country);
  };
  $scope.saveCountry = function(country) {
    $scope.initProcess();
    $scope.process.saveConfig = true;
    $scope.process.saveEnablement = true;
    $scope.process.saveProcessOnCompletion = true;
    $scope.process.processOnCompletion = country.processOnCompletion;
    $scope.process.removeCountry = false;
    $scope.process.configId = country.configId;
    $scope.process.autoEngineIndc = country.autoEngineIndc;
    $scope.process.countries.push(country.cmrIssuingCntry);
    $scope.saveAllChanges(country);
  };
  $scope.unmapCountries = function() {
    var selectedCount = 0;
    var countryList = [];
    $scope.countries.forEach(function(cntry, index) {
      if (cntry.selected) {
        selectedCount++;
        countryList.push(cntry.cmrIssuingCntry);
      }
    });
    if (selectedCount == 0) {
      alert('No country selected.');
      return;
    }
    if (!confirm('Unmap ' + selectedCount + ' countries from automation engines?')) {
      return;
    }
    $scope.initProcess();
    $scope.process.saveEnablement = false;
    $scope.process.saveProcessOnCompletion = false;
    $scope.process.saveConfig = false;
    $scope.process.removeCountry = true;
    $scope.process.countries = countryList;
    $scope.saveAllChanges();
  };
  $scope.enablementVal = '';
  $scope.saveEnablement = function() {
    var selectedCount = 0;
    var countryList = [];
    var hasSubRegion = false;
    $scope.countries.forEach(function(cntry, index) {
      if (cntry.selected) {
        selectedCount++;
        countryList.push(cntry.cmrIssuingCntry);
        if (cntry.cmrIssuingCntry.length > 3) {
          hasSubRegion = true;
        }
      }
    });
    if (selectedCount == 0) {
      alert('No country selected.');
      return;
    }

    if (hasSubRegion) {
      if (!confirm('Some selected countries are subregions. Modifying this value changes the value of the parent country. Update enablement for ' + selectedCount + ' countries?')) {
        return;
      }
    } else {
      if (!confirm('Update enablement for ' + selectedCount + ' countries?')) {
        return;
      }
    }
    $scope.initProcess();
    $scope.process.saveEnablement = true;
    $scope.process.saveProcessOnCompletion = false;
    $scope.process.saveConfig = false;
    $scope.process.removeCountry = false;
    $scope.process.autoEngineIndc = $scope.enablementVal;
    $scope.process.countries = countryList;
    $scope.saveAllChanges();
  };

  $scope.configMappingVal = '';
  $scope.saveMapping = function() {
    if (!$scope.configMappingVal) {
      alert('Please choose an automation engine configuration.');
      return;
    }
    var selectedCount = 0;
    var countryList = [];
    $scope.countries.forEach(function(cntry, index) {
      if (cntry.selected) {
        selectedCount++;
        countryList.push(cntry.cmrIssuingCntry);
      }
    });
    if (selectedCount == 0) {
      alert('No country selected.');
      return;
    }
    if (!confirm('Update automation engine mapping for ' + selectedCount + ' countries?')) {
      return;
    }
    $scope.initProcess();
    $scope.process.saveEnablement = false;
    $scope.process.saveProcessOnCompletion = false;
    $scope.process.saveConfig = true;
    $scope.process.removeCountry = false;
    $scope.process.configId = $scope.configMappingVal;
    $scope.process.countries = countryList;
    $scope.saveAllChanges();
  };

  $scope.processOnCompletionVal = '';
  $scope.saveProcessOnCompletion = function() {
    var selectedCount = 0;
    var countryList = [];
    $scope.countries.forEach(function(cntry, index) {
      if (cntry.selected) {
        selectedCount++;
        countryList.push(cntry.cmrIssuingCntry);
      }
    });
    if (selectedCount == 0) {
      alert('No country selected.');
      return;
    }
    if (!confirm('Update process on completion setting for ' + selectedCount + ' countries?')) {
      return;
    }
    $scope.initProcess();
    $scope.process.saveEnablement = false;
    $scope.process.saveProcessOnCompletion = true;
    $scope.process.saveConfig = false;
    $scope.process.removeCountry = false;
    $scope.process.processOnCompletion = $scope.processOnCompletionVal ? $scope.processOnCompletionVal : null;
    $scope.process.countries = countryList;
    $scope.saveAllChanges();
  };

  $scope.saveAllChanges = function(country) {
    var params = JSON.parse(JSON.stringify($scope.process));

    cmr.showProgress('Saving configurations..');
    $http.post(cmr.CONTEXT_ROOT + '/auto/config/savecntry.json', params).then(function(response) {
      cmr.hideProgress();
      var data = response.data;
      if (data && data.result && data.result.success) {
        $scope.getMappedCountries();
        $scope.initProcess();
        if (country) {
          country.edit = false;
        } else {
          $scope.undoEdit();
        }
        cmr.showAlert('Configuration saved successfully.', null, null, true);
      } else {
        var msg = data.result && data.result.message ? data.result.message : 'An error occurred during saving.';
        cmr.showAlert(msg);
      }
    }, function(error) {
      cmr.hideProgress();
      var msg = 'An error occurred during saving.';
      cmr.showAlert(msg);
    });
  };

  $scope.editConfig = function(configId) {
    openEditPage(configId);
  };

  $scope.selectAll = false;
  $scope.doSelectAll = function(select) {
    $scope.countries.forEach(function(cntry, i) {
      cntry.selected = select;
    });
  };

  $scope.getAvailableConfigIds = function() {
    var recs = cmr.query('GET_ALL_AUTO_IDS', {
      _qall : 'Y'
    });
    if (recs) {
      recs.forEach(function(rec, index) {
        var flatDesc = rec.ret2.replace(/\n/g, ' ');
        if (flatDesc.length > 100) {
          flatDesc = flatDesc.substring(0, 97) + '...';
        }
        $scope.configIds.push({
          id : rec.ret1,
          desc : flatDesc
        });
      });
    }
  };

  $scope.changeEnablement = function() {
    $scope.editEnablement = true;
  };
  $scope.changeProcessOnCompletion = function() {
    $scope.editProcessOnCompletion = true;
  };
  $scope.mapToEngine = function() {
    $scope.editMapping = true;
  };
  $scope.undoEdit = function() {
    $scope.editEnablement = false;
    $scope.editMapping = false;
    $scope.editProcessOnCompletion = false;
  };

  $scope.getMappedCountries();
  $scope.getAvailableConfigIds();

} ]);

app
    .controller(
        'ExceptionsController',
        [
            '$scope',
            '$document',
            '$http',
            '$timeout',
            '$sanitize',
            '$filter',
            function($scope, $document, $http, $timeout, $sanitize, $filter) {

              $scope.countries = [];
              $scope.currCountry = null;
              $scope.currRegion = null;
              $scope.configuring = false;
              $scope.mainScenarios = [];
              $scope.scenarioMap = {};
              $scope.subScenarios = [];
              $scope.cmrTypes = [];
              $scope.exceptions = [];
              $scope.viewMode = 'N';
              $scope.categories = [];

              $scope.title = {
                skipDupChecksIndc : 'Controls whether duplicate checks should be skipped for this particular scenario. For example, IBM Internal records can have duplicates so the check can be skipped.\n\nDefault: No',
                importDnbInfoIndc : 'Specifies whether D&B processing will import only Subindustry, ISIC, and DUNS No., or whether all address information will be imported.\n\nDefault: Subindustry, ISIC, and DUNS No. only',
                checkVatIndc : 'Indicates whether VAT will be checked during D&B Matching. If VAT is included, if no high quality D&B matches are found with the provided VAT, the request will be redirected to the processors for validation.\n\nDefault: No',
                skipChecksIndc : 'Controls whether automatic checks and computations for this scenario will be skipped. If skipped, the request only undergoes standard processes like DPL check and no data will be computed.\n\nDefault: No',
                skipAddressChecks : 'For update requests, requests with updates/new instances of ONLY the specified addresses here will not be checked anymore by the system. For some countries for example, new Shipping addresses should just be processed directly.\n\nDefault: Check all addresses',
                dupAddressChecks : 'Maps the CMR address types against the RDc address types which will be used in duplicate checks. By default, the system uses only Main address vs RDc Sold-to to determine duplicates.\n\nDefault:Main address vs RDc Sold-to',
                skipCompanyVerificationChecks : 'Controls whether automatic checks for company verification (Eg. External API vat validation, D&B check, etc.) for this scenario will be skipped. If skipped, the request only undergoes standard processes like DPL check and company information will not be validated.\n\nDefault: No'
              };

              $scope.getCountries = function() {
                var results = cmr.query('EXCEP.GET_COUNTRIES', {
                  _qall : 'Y'
                });
                if (results) {
                  results.forEach(function(country, index) {
                    $scope.countries.push({
                      id : country.ret1,
                      name : country.ret2,
                      desc : country.ret3
                    });
                  });
                }
                var cntry = $scope.getParameterByName('cntry');
                console.log('country: ' + cntry);
                if (cntry) {
                  $scope.currCountry = cntry;
                  $scope.configure(true);
                }
              };
              $scope.configure = function(start) {
                if (start && !$scope.currCountry) {
                  alert('Please select a country.');
                  return;
                }
                if (start) {
                  $scope.cmrTypes = [];
                  $scope.getCmrTypes();
                } else {
                  var hasUnsaved = false;
                  $scope.exceptions.forEach(function(exc, i) {
                    if (exc.status == 'N' || exc.status == 'M') {
                      hasUnsaved = true;
                    }
                  });
                  if (hasUnsaved) {
                    if (!confirm('There are unsaved scenario exceptions that will be lost. Ignore the changes and load another country?')) {
                      return;
                    }
                  }
                  $scope.configuring = false;
                  $scope.currCountry = '';
                }

              }

              $scope.getCmrTypes = function() {
                console.log('getting CMR types..');
                var cntry = $scope.currCountry;
                if (cntry.length > 3) {
                  $scope.currRegion = cntry;
                  cntry = cntry.substring(0, 3);
                }
                var types = cmr.query('EXCEP.GET_ADDRESS_TYPES', {
                  _qall : 'Y',
                  country : cntry
                });
                if (types) {
                  $scope.cmrTypes.push({
                    id : '',
                    name : ''
                  });
                  types.forEach(function(t, i) {
                    $scope.cmrTypes.push({
                      id : t.ret1,
                      name : t.ret2
                    });
                  });
                }
                if ($scope.cmrTypes.length == 1) {
                  $scope.cmrTypes = [ {
                    id : '',
                    name : ''
                  }, {
                    id : 'ZS01',
                    name : 'Sold-to'
                  }, {
                    id : 'ZP01',
                    name : 'Billing'
                  }, {
                    id : 'ZI01',
                    name : 'Installing'
                  }, {
                    id : 'ZD01',
                    name : 'Shipping'
                  } ]
                }
                $scope.getScenarios();
              }

              $scope.getScenarios = function() {
                console.log('getting configured scenarios..');
                var cntry = $scope.currCountry;
                $scope.currRegion = cntry;
                if (cntry.length > 3) {
                  cntry = cntry.substring(0, 3);
                }
                var queryKey = 'EXCEP.GET_SCENARIOS';
                if (cntry == '897') {
                  queryKey = 'EXCEP.GET_SCENARIOS.US';
                }
                var scenarios = cmr.query(queryKey, {
                  country : cntry,
                  geoCd : $scope.currRegion ? $scope.currRegion : cntry,
                  _qall : 'Y'
                });
                $scope.mainScenarios = [ {
                  id : '*',
                  name : '-All Scenarios-'
                } ];
                $scope.subScenarios = [];
                $scope.scenarioMap = {};
                $scope.exceptions = [];
                $scope.currSubScenario = '';
                $scope.currScenario = '';
                if (scenarios) {
                  var types = [];
                  scenarios.forEach(function(s, i) {
                    if (types.indexOf(s.ret1) < 0) {
                      $scope.mainScenarios.push({
                        id : s.ret1,
                        name : s.ret2
                      });
                      types.push(s.ret1);
                    }
                    if (!$scope.scenarioMap[s.ret1]) {
                      $scope.scenarioMap[s.ret1] = [ {
                        id : '*',
                        name : '-All Sub Scenarios-'
                      } ];
                    }
                    $scope.scenarioMap[s.ret1].push({
                      id : s.ret3,
                      name : s.ret4
                    });
                  });
                  console.log(types);
                  $scope.configuring = true;
                  $scope.loadExceptions();
                } else {
                  alert('No scenarios found for ' + $scope.currCountry);
                }
              };
              $scope.getSubScenarios = function() {
                if (!$scope.currScenario) {
                  alert('Select a main scenario type.');
                  return;
                }
                if ($scope.currScenario == '*') {
                  $scope.subScenarios = [ {
                    id : '*',
                    name : '-All Sub Scenarios-'
                  } ];
                  $scope.currSubScenario = '*';
                } else {
                  $scope.currSubScenario = '';
                  $scope.subScenarios = $scope.scenarioMap[$scope.currScenario];
                }
              };

              // load and add exceptions
              $scope.loadExceptions = function() {
                var cntry = $scope.currCountry;
                if (cntry.length > 3) {
                  cntry = cntry.substring(0, 3);
                }
                var exceptions = cmr.getAllRecords('EXCEP_LOAD', 'ScenarioExceptions', {
                  country : cntry,
                  geoCd : $scope.currCountry,
                });
                if (exceptions && exceptions.length > 0) {
                  exceptions.forEach(function(r, i) {
                    if (r.id.custTyp) {
                      r.id.custTyp = r.id.custTyp.trim();
                    }
                    if (r.id.custSubTyp) {
                      r.id.custSubTyp = r.id.custSubTyp.trim();
                    }
                    var skipTypes = [];
                    if (r.skipCheckAddrTypes) {
                      var addresses = r.skipCheckAddrTypes.split(',');
                      addresses.forEach(function(a, i) {
                        skipTypes.push({
                          code : a,
                          description : $scope.getCmrTypeDesc(a)
                        });
                      });
                    }
                    var dupTypes = [];
                    if (r.dupCheckAddrTypes) {
                      var addresses = r.dupCheckAddrTypes.split(',');
                      addresses.forEach(function(a, i) {
                        var cr = a.split('-');
                        if (cr.length == 2) {
                          dupTypes.push({
                            cmrType : cr[0],
                            rdcType : cr[1],
                            cmrTypeDesc : $scope.getCmrTypeDesc(cr[0]),
                            rdcTypeDesc : $scope.getRdcTypeDesc(cr[1]),
                          })
                        }
                      });
                    }
                    var exc = {
                      cmrIssuingCntry : r.id.cmrIssuingCntry,
                      custTyp : r.id.custTyp,
                      custSubTyp : r.id.custSubTyp,
                      region : r.id.subregionCd,
                      skipDupChecksIndc : r.skipDupChecksIndc ? r.skipDupChecksIndc.trim() : '',
                      importDnbInfoIndc : r.importDnbInfoIndc ? r.importDnbInfoIndc.trim() : '',
                      skipVerificationIndc : r.skipVerificationIndc ? r.skipVerificationIndc.trim() : '',
                      checkVatIndc : r.checkVatIndc ? r.checkVatIndc.trim() : '',
                      skipChecksIndc : r.skipChecksIndc ? r.skipChecksIndc.trim() : '',
                      skipAddressChecks : skipTypes,
                      dupAddressChecks : dupTypes,
                      status : 'E'
                    };
                    $scope.cleanException(exc);
                  });
                }
              };

              $scope.addException = function() {
                if ($scope.exceptions) {
                  var matched = false;
                  $scope.exceptions.forEach(function(e, i) {
                    if ($scope.currScenario == e.custTyp && $scope.currSubScenario == e.custSubTyp) {
                      alert('Exception for this scenario already has been defined. Please check exceptions below');
                      matched = true;
                    }
                  });
                  if (matched) {
                    return;
                  }
                }
                if (!$scope.currSubScenario) {
                  alert('Select a sub scenario type.');
                  return;
                }
                var exc = {
                  cmrIssuingCntry : $scope.currCountry.length > 3 ? $scope.currCountry.substring(0, 3) : $scope.currCountry,
                  region : $scope.currCountry,
                  custTyp : $scope.currScenario,
                  custSubTyp : $scope.currSubScenario,
                  skipDupChecksIndc : '',
                  importDnbInfoIndc : '',
                  checkVatIndc : '',
                  skipChecksIndc : '',
                  skipAddressChecks : [],
                  dupAddressChecks : [],
                  skipChecksIndc : '',
                  status : 'N'
                };
                $scope.cleanException(exc);
              };

              $scope.cleanException = function(exc) {
                if (exc.custTyp.trim() == '*') {
                  exc.description = 'All Scenarios';
                  exc.typeDesc = 'All Scenarios';
                } else {
                  $scope.mainScenarios.forEach(function(s, i) {
                    if (s.id == exc.custTyp) {
                      exc.description = s.name;
                      exc.typeDesc = s.name;
                    }
                  });
                  if (exc.custSubTyp == '*') {
                    exc.description += ' (All Sub Scenarios)';
                    exc.subTypeDesc = '- All -';
                  } else {
                    if ($scope.scenarioMap[exc.custTyp]) {
                      $scope.scenarioMap[exc.custTyp].forEach(function(s, i) {
                        if (s.id == exc.custSubTyp) {
                          exc.description += ' (' + s.name + ')';
                          exc.subTypeDesc = s.name;
                        }
                      });
                    }
                  }
                }
                if (!exc.subTypeDesc && exc.custSubTyp == '*') {
                  exc.subTypeDesc = ' - All -';
                }
                $scope.exceptions.push(exc);
                $scope.exceptions.sort(excComparator);
                $scope.categories = [];
                $scope.exceptions.forEach(function(exc, index) {
                  if ($scope.categories.indexOf(exc.typeDesc) < 0) {
                    $scope.categories.push(exc.typeDesc);
                  }
                });
              };
              $scope.dirtyException = function(exc) {
                if (exc.status == 'E') {
                  exc.status = 'M';
                }
              }

              $scope.getCmrTypeDesc = function(type) {
                for (var i = 0; i < $scope.cmrTypes.length; i++) {
                  if (type == $scope.cmrTypes[i].id) {
                    return $scope.cmrTypes[i].name;
                  }
                }
                return type;
              };
              $scope.getRdcTypeDesc = function(type) {
                switch (type) {
                case 'ZS01':
                  return 'Sold-To (ZS01)';
                case 'ZP01':
                  return 'Bill-To (ZP01)';
                case 'ZI01':
                  return 'Install-at (ZI01)';
                case 'ZD01':
                  return 'Ship-to (ZD01)';
                case 'ZS02':
                  return 'Secondary Sold-to (ZS02)';
                default:
                  return type;
                }
              };

              $scope.addDupMapping = function(exc) {
                if (!exc.dupCmrType || !exc.dupRdcType) {
                  alert('Please specify both CMR Type and RDC Type to map.');
                  return;
                }
                if (exc.dupAddressChecks) {
                  var found = false;
                  exc.dupAddressChecks.forEach(function(a, i) {
                    if (a.cmrType == exc.dupCmrType || a.rdcType == exc.dupRdcType) {
                      found = true;
                    }
                  });
                  if (found) {
                    alert('This mapping already exists.');
                    return;
                  }
                }
                exc.dupAddressChecks.push({
                  cmrType : exc.dupCmrType,
                  rdcType : exc.dupRdcType,
                  cmrTypeDesc : $scope.getCmrTypeDesc(exc.dupCmrType),
                  rdcTypeDesc : $scope.getRdcTypeDesc(exc.dupRdcType),
                })
                $scope.dirtyException(exc);
              };
              $scope.removeDupMapping = function(exc, dup) {
                var index = exc.dupAddressChecks.indexOf(dup);
                if (index >= 0) {
                  exc.dupAddressChecks.splice(index, 1);
                }
                $scope.dirtyException(exc);
              };

              $scope.addSkipMapping = function(exc) {
                if (!exc.skipCmrType) {
                  alert('Please specify the CMR Type to add.');
                  return;
                }
                if (exc.skipAddressChecks) {
                  var found = false;
                  exc.skipAddressChecks.forEach(function(a, i) {
                    if (a.code == exc.skipCmrType) {
                      found = true;
                    }
                  });
                  if (found) {
                    alert('Type already added.');
                    return;
                  }
                }
                exc.skipAddressChecks.push({
                  code : exc.skipCmrType,
                  description : $scope.getCmrTypeDesc(exc.skipCmrType)
                })
                $scope.dirtyException(exc);
              };
              $scope.removeSkipMapping = function(exc, addr) {
                var index = exc.skipAddressChecks.indexOf(addr);
                if (index >= 0) {
                  exc.skipAddressChecks.splice(index, 1);
                }
                $scope.dirtyException(exc);
              };

              // save functions
              $scope.saveException = function(exc) {
                var params = JSON.parse(JSON.stringify(exc));
                var skips = [];
                if (params.skipAddressChecks) {
                  params.skipAddressChecks.forEach(function(a, i) {
                    skips.push(a.code);
                  });
                }
                delete (params.skipCmrType);
                delete (params.dupCmrType);
                delete (params.dupRdcType);
                delete (params.description);

                params.skipAddressChecks = skips;
                cmr.showProgress('Processing, please wait..');
                $http.post(cmr.CONTEXT_ROOT + '/auto/config/saveexception.json', params).then(function(response) {
                  cmr.hideProgress();
                  var data = response.data;
                  if (data && data.result && data.result.success) {
                    exc.status = 'E';
                    cmr.showAlert('Scenario Exception saved successfully.', null, null, true);
                  } else {
                    var msg = data.result && data.result.message ? data.result.message : 'An error occurred during saving.';
                    cmr.showAlert(msg);
                  }
                }, function(error) {
                  console.log(error);
                  cmr.hideProgress();
                  var msg = 'An error occurred during saving.';
                  cmr.showAlert(msg);
                });
              };
              $scope.removeException = function(exc) {
                if (confirm('Remove exception for ' + exc.description + '?')) {
                  if (exc.status == 'N') {
                    $scope.exceptions.splice($scope.exceptions.indexOf(exc), 1);
                    $scope.exceptions.sort(excComparator);
                  } else {
                    var params = JSON.parse(JSON.stringify(exc));
                    var skips = [];
                    if (params.skipAddressChecks) {
                      params.skipAddressChecks.forEach(function(a, i) {
                        skips.push(a.code);
                      });
                    }
                    params.skipAddressChecks = skips;
                    params.status = 'D';

                    delete (params.skipCmrType);
                    delete (params.dupCmrType);
                    delete (params.dupRdcType);
                    delete (params.description);

                    cmr.showProgress('Processing, please wait..');
                    $http.post(cmr.CONTEXT_ROOT + '/auto/config/saveexception.json', params).then(function(response) {
                      cmr.hideProgress();
                      var data = response.data;
                      if (data && data.result && data.result.success) {
                        $scope.exceptions.splice($scope.exceptions.indexOf(exc), 1);
                        $scope.exceptions.sort(excComparator);
                        cmr.showAlert('Scenario Exception removed successfully.', null, null, true);
                      } else {
                        var msg = data.result && data.result.message ? data.result.message : 'An error occurred during saving.';
                        cmr.showAlert(msg);
                      }
                    }, function(error) {
                      console.log(error);
                      cmr.hideProgress();
                      var msg = 'An error occurred during saving.';
                      cmr.showAlert(msg);
                    });
                  }
                }
              };
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

              $scope.getCountries();

            } ]);

function excComparator(a, b) {
  if (a.custTyp == '*' && b.custTyp != '*') {
    return -1;
  }
  if (b.custTyp == '*' && a.custTyp != '*') {
    return 1;
  }
  if (a.custSubTyp == '*' && b.custSubTyp != '*') {
    return -1;
  }
  if (b.custSubTyp == '*' && a.custSubTyp != '*') {
    return 1;
  }
  if (a.description < b.description) {
    return -1;
  }
  if (a.description > b.description) {
    return -1;
  }
  return 0;
}

function openEditPage(configId) {
  window.location.href = cmr.CONTEXT_ROOT + '/auto/config/maint?configId=' + configId;
}

function openConfigListPage() {
  window.location.href = cmr.CONTEXT_ROOT + '/auto/config/list';
}