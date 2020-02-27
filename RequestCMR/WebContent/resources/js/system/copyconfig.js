var app = angular.module('CopyConfigApp', [ 'ngRoute' ]);

app.controller('CopyConfigController', [ '$scope', '$document', '$http', '$timeout', function($scope, $document, $http, $timeout) {

  $scope.loaded = false;
  var results = cmr.query('COPY_CNTRIES', {
    _qall : 'Y'
  });

  $scope.countries = new Array();
  if (results != null && results.length > 0) {
    results.forEach(function(result, index) {
      $scope.countries.push({
        id : result.ret1,
        name : result.ret2
      });
    });
  }

  results = cmr.query('GET_GEOS', {
    _qall : 'Y'
  });

  $scope.geos = new Array();
  if (results != null && results.length > 0) {
    results.forEach(function(result, index) {
      $scope.geos.push({
        id : result.ret1,
        name : result.ret1
      });
    });
  }

  $scope.copyConfigurations = function() {
    if (!$scope.sourceCountry) {
      cmr.showAlert('Please select the Source Country.');
      return;
    }
    if (!$scope.configFI && !$scope.configFL && !$scope.configLOV && !$scope.configSC) {
      cmr.showAlert('Please select at least 1 configuration to copy.');
      return;
    }
    if (!$scope.target) {
      cmr.showAlert('Please select the target Copy To value.');
      return;
    }

    var configs = $scope.configFI ? 'FI' : '';
    if ($scope.configFL) {
      configs += (configs != '' ? ',' : '') + 'FL';
    }
    if ($scope.configLOV) {
      configs += (configs != '' ? ',' : '') + 'LOV';
    }
    if ($scope.configSC) {
      configs += (configs != '' ? ',' : '') + 'SC';
    }
    var params = {
      sourceCountry : $scope.sourceCountry,
      configType : configs
    };

    if ($scope.target == 'G') {
      if (!$scope.targetGeo) {
        cmr.showAlert('Please select the target GEO to copy to.');
        return;
      }
      params.targetCountry = null;
      params.targetGeo = $scope.targetGeo;
    } else {
      var choices = '';
      $scope.countries.forEach(function(item, index) {
        if (item.selected) {
          if (item.id == $scope.sourceCountry) {
            cmr.showAlert('You cannot select a target country equal to the source.');
            return;
          }
          choices += (choices != '' ? ',' : '') + item.id;
        }
      });
      params.targetCountry = choices;
      params.targetGeo = null;
    }

    console.log(params);

    cmr.showProgress('Processing, please wait...');
    $http({
      url : cmr.CONTEXT_ROOT + '/code/copy/process.json',
      method : 'POST',
      params : params
    }).then(function(response) {
      cmr.hideProgress();
      if (response.data && response.data.result && response.data.result.success) {
        cmr.showAlert('Configuration(s) copied successfully.', 'Success', null, true);
      } else {
        cmr.showAlert('An error occurred while processing.');
      }
    }, function(response) {
      cmr.hideProgress();
      console.log('error: ');
      console.log(response);
      cmr.showAlert('An error occurred while processing.');
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
