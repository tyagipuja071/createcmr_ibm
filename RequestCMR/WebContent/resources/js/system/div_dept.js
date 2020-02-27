var app = angular.module('DivDeptApp', [ 'ngRoute' ]);

app.controller('DivDeptController', [ '$scope', '$document', '$http', '$timeout', function($scope, $document, $http, $timeout) {

  $scope.checkStatus = function() {
    if (!$scope.div || !$scope.dept) {
      alert('Please input both Division and Department.');
      return;
    }
    if ($scope.div.length != 2 || $scope.dept.length != 3) {
      alert('Division should be 2 characters long and Department should be 3 characters long.');
      return;
    }
    $scope.div = $scope.div.toUpperCase();
    $scope.dept = $scope.dept.toUpperCase();
    var ret = cmr.query('DIV_DEPT.CHECK', {
      DEPT : $scope.dept
    });
    if (ret && ret.ret1) {
      console.log(ret);
      var map = ret.ret1.trim();
      if (map.indexOf($scope.div) == 0) {
        $scope.mapStatus = 'Y';
      } else {
        $scope.currDiv = map.substring(0, 2);
        $scope.mapStatus = 'X';
      }
    } else {
      $scope.mapStatus = 'N';
    }
  };

  $scope.mapDivDept = function() {
    var confirmTxt = null;
    if ($scope.mapStatus == 'N') {
      confirmTxt = 'This will create a mapping between the Division/Department ' + $scope.div + '-' + $scope.dept + '. Any missing record will be created. Proceed?';
    } else if ($scope.mapStatus == 'X') {
      confirmTxt = 'This will create a mapping between the Division/Department ' + $scope.div + '-' + $scope.dept + ' and unmap the Department from Division ' + $scope.currDiv + '. Proceed?';
    }
    if (!confirm(confirmTxt)) {
      return;
    }
    var params = {
      action : 'MAP_DEPT',
      div : $scope.div,
      dept : $scope.dept
    };
    cmr.showProgress('Mapping division and department, please wait..');
    $http.post(cmr.CONTEXT_ROOT + '/code/div_dept/process.json', params).then(function(response) {
      cmr.hideProgress();
      var data = response.data;
      if (data && data.result && data.result.success) {
        cmr.showAlert('Mapping of Division/Department <strong>' + $scope.div + '-' + $scope.dept + '</strong> created successfully.', null, null, true);
        $scope.mapStatus = '';
        $scope.currDiv = '';
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

} ]);
